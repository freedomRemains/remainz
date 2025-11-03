package com.remainz.common.service.proc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeUtility;

import org.apache.log4j.Logger;

import com.remainz.common.db.DbInterface;
import com.remainz.common.param.GenericParam;
import com.remainz.common.util.Cu;
import com.remainz.common.util.FileUtil;
import com.remainz.common.util.InputCheckUtil;
import com.remainz.common.util.RcProp;

public class MailProcBase implements MailProcInterface {

	/** パス区切り文字 */
	public static final String PATH_DELM = "/";

	/** 改行文字 */
	protected static final String LINE_SEPR = System.getProperty("line.separator");

	/** ロガー */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	protected Logger getLogger() {
		return logger;
	}

	/** メールのシーケンス番号 */
	private String seqNum;

	protected String getSeqNum() {
		return seqNum;
	}

	/** メールID */
	private String mailId;

	protected String getMailId() {
		return mailId;
	}

	@Override
	public void doMailProc(GenericParam input, GenericParam output, Message mail) throws Exception {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkDb(input);
		inputCheckUtil.checkParam(input, "seqNum");

		// 入力パラメータからシーケンス番号を取得する
		seqNum = input.getString("seqNum");

		// マルチパートを扱うため、メールそのものをパートにキャストする
		Part part = (Part) mail;

		// メールは階層となっていることがあるため、階層を記憶する変数を作成する
		int layer = 1;

		// メールIDを取得する
		mailId = getMailId(mail);

		// マルチパートを再帰的に解析し、メール本文も取得する
		StringBuffer analyzeLog = new StringBuffer("[analyze start]" + seqNum + LINE_SEPR);
		StringBuffer honbun = new StringBuffer();
		analyzeMultipartRecursive(part, layer, analyzeLog, honbun);

		// メールを保存する
		saveMail(mail, analyzeLog, honbun, input.getDb());
	}

	protected String getMailId(Message mail) throws Exception {

		// メールを一意に識別するための文字列を生成し、呼び出しがwに返却する
		return seqNum + "_" + Cu.convertForFileName(mail.getSubject());
	}

	protected void analyzeMultipartRecursive(Part part, int layer, StringBuffer analyzeLog, StringBuffer honbun)
			throws Exception {

		if (part.isMimeType("multipart/*")) {

			// メールがマルチパートである場合は、解析中のパートをマルチパート型として再取得する
			Multipart multipart = (Multipart) part.getContent();

			// マルチパート内の全ての要素を処理するまでループ
			for (int i = 0; i < multipart.getCount(); i++) {

				// ボディパートを取得する
				Part bodyPart = multipart.getBodyPart(i);

				if (bodyPart.isMimeType("multipart/*")) {

					// ボディパートがマルチパートとなっている場合は、本処理を再帰的に実行する
					analyzeLog.append("[layer]" + layer + " [multipart]" + (i + 1)
							+ " is multipart/* -> analyzeRecursive" + LINE_SEPR);
					analyzeMultipartRecursive(bodyPart, layer + 1, analyzeLog, honbun);

				} else if (bodyPart.isMimeType("text/plain")) {

					// ボディパートがテキストである場合は、本文をメンバ変数に設定する
					analyzeLog.append("[layer]" + layer + " [multipart]" + (i + 1) + " is text/plain" + LINE_SEPR);
					honbun.append(bodyPart.getContent().toString());

				} else if (bodyPart.getDisposition() == null || bodyPart.getDisposition().equalsIgnoreCase(Part.ATTACHMENT)) {

					// 添付ファイルと見られるパートである場合は、ログを記録して添付ファイルを保存する
					if (bodyPart.getFileName() != null) {
						analyzeLog.append("[layer]" + layer + " [disposition]" + bodyPart.getDisposition());
						analyzeLog.append("  [fileName]" + MimeUtility.decodeText(bodyPart.getFileName()));
						analyzeLog.append(LINE_SEPR);
						saveAttachmentFile(bodyPart, analyzeLog);
					}

				} else {
					analyzeLog.append(
							"[layer]" + layer + " [multipart]" + (i + 1) + " is " + bodyPart.getContentType() + " です。"
									+ LINE_SEPR);
				}
			}

		} else if (part.isMimeType("message/rfc822")) {

			// メールが通常のRFC822メッセージである場合は、本文をメンバ変数に設定する
			analyzeLog.append("[layer]" + layer + " is message/rfc822" + LINE_SEPR);
			honbun.append(part.getContent().toString());

		} else if (part.isMimeType("text/*")) {

			// メールがプレーンテキストである場合は、本文をメンバ変数に設定する
			analyzeLog.append("[layer]" + layer + "is text/*" + LINE_SEPR);
			honbun.append(part.getContent().toString());

		} else {

			// それ以外の場合はエラーとする
			String editMsg = "[layer]" + layer + " [unknown contentType]" + part.getContentType() + LINE_SEPR;
			analyzeLog.append(editMsg);
			honbun.append(editMsg);
		}
	}

	protected void saveAttachmentFile(Part part, StringBuffer analyzeLog) throws Exception {

		// 添付ファイルの入力ストリームを取得する
		InputStream inputStream = part.getInputStream();

		// 添付ファイルを保存するフォルダのパスを生成する
		RcProp prop = new RcProp();
		String mailDirPath = prop.get("base.dir") + PATH_DELM + prop.get("mail.output.dir")
				+ PATH_DELM + mailId;

		// フォルダが存在しない場合は作成する
		File mailDir = new File(mailDirPath);
		if (!mailDir.exists()) {
			analyzeLog.append("[saveAttachmentFile][createDir] " + mailDirPath + LINE_SEPR);
			new FileUtil().createDirIfNotExists(mailDirPath);
		}

		// 添付ファイルパスを生成する
		String fileName = MimeUtility.decodeText(part.getFileName());
		fileName = Cu.convertForFileName(fileName);
		String filePath = mailDirPath + PATH_DELM + fileName;
		try (var fileOutputStream = new FileOutputStream(new File(filePath))) {

			// 入力ストリームから添付ファイルの内容を全て読み込むまでループ
			byte[] buf = new byte[8192];
			int bytesRead;
			while ((bytesRead = inputStream.read(buf)) != -1) {

				// 入力ストリームから読み込んだデータをファイルに書き込む
				fileOutputStream.write(buf, 0, bytesRead);
			}
		}
	}

	protected void saveMail(Message mail, StringBuffer analyzeLog, StringBuffer honbun,
			DbInterface db) throws Exception {

		// 件名もしくは本文に制御文字が含まれている場合は、ログを記録して即時終了する
		if (Cu.hasCtrlCode(mail.getSubject()) || Cu.hasCtrlCode(honbun.toString())) {

			// メール解析のログはここで記録する
			analyzeLog.append("[hasCtrlCode]saveMail aborted." + LINE_SEPR);
			logger.info(analyzeLog);
			return;
		}

		// メールをファイルとして保存し、DBにも保存する
		saveMailAsFile(mail, analyzeLog, honbun);
		saveMailAsDbRecord(mail, honbun, db);

		// メール解析のログはここで記録する
		logger.info(analyzeLog);
	}

	private void saveMailAsFile(Message mail, StringBuffer analyzeLog, StringBuffer honbun)
			throws Exception {

		// 送信元、件名、送信日時を取得する
		String mailFrom = MimeUtility.decodeText(mail.getFrom()[0].toString());
		String subject = mail.getSubject();
		Date sentDate = mail.getSentDate();

		// メールファイルを保存するフォルダのパスを生成する
		RcProp prop = new RcProp();
		String mailDirPath = prop.get("base.dir") + PATH_DELM + prop.get("mail.output.dir")
				+ PATH_DELM + mailId;

		// フォルダが存在しない場合は作成する
		File mailDir = new File(mailDirPath);
		if (!mailDir.exists()) {
			analyzeLog.append("[saveMailAsFile][createDir] " + mailDirPath + LINE_SEPR);
			new FileUtil().createDirIfNotExists(mailDirPath);
		}

		// メールIDに件名と送信日時を付加してファイル名とする
		var dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String fileName = mailId + Cu.convertForFileName(subject)
				+ Cu.convertForFileName(dateFormat.format(sentDate))
				+ ".txt";
		String filePath = mailDirPath + PATH_DELM + fileName;
		try (var mailFile = new FileUtil().getBufferedWriter(filePath)) {

			// メールの内容をファイルに書き込む
			StringBuffer msg = new StringBuffer();
			msg.append("【送信元】" + mailFrom + LINE_SEPR);
			msg.append("【件名】" + subject + LINE_SEPR);
			msg.append("【送信日時】" + sentDate + LINE_SEPR);
			msg.append("【本文】" + LINE_SEPR + honbun + LINE_SEPR + LINE_SEPR);
			mailFile.write(msg.toString());
		}
	}

	private void saveMailAsDbRecord(Message mail, StringBuffer honbun, DbInterface db)
			throws Exception {

		// TODO 未実装
//		// 送信元、件名、送信日時を取得する
//		String mailFrom = MimeUtility.decodeText(mail.getFrom()[0].toString());
//		String subject = mail.getSubject();
//		Date sentDate = mail.getSentDate();
//
//		//+
//		StringBuffer msg = new StringBuffer();
//		msg.append("【送信元】" + mailFrom + LINE_SEPR);
//		msg.append("【件名】" + subject + LINE_SEPR);
//		msg.append("【送信日時】" + sentDate + LINE_SEPR);
//		msg.append("【本文】" + LINE_SEPR + honbun + LINE_SEPR + LINE_SEPR);
//		logger.info(msg);
//		//-
	}
}
