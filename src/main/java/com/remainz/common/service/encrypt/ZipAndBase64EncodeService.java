package com.remainz.common.service.encrypt;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Base64;

import com.remainz.common.exception.ApplicationInternalException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.service.ServiceInterface;
import com.remainz.common.util.FileUtil;
import com.remainz.common.util.InputCheckUtil;
import com.remainz.common.util.LogUtil;
import com.remainz.common.util.RcProp;

public class ZipAndBase64EncodeService implements ServiceInterface {

	@Override
	public void doService(GenericParam input, GenericParam output) {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkParam(input, "targetDirOrFile");
		inputCheckUtil.checkParam(input, "outputDir");

		// zipファイルを生成し、更にBase64エンコードしたテキストファイルとする
		doEncryptDirOrFileAsTextFile(input, output);
	}

	private void doEncryptDirOrFileAsTextFile(GenericParam input, GenericParam output) {

		// 入力パラメータにchaset指定がない場合は、デフォルト値を適用する
		String charset = input.getString("charset");
		if (charset == null) {
			charset = new RcProp().get("default.charset");
		}

		// zipファイルを作成する
		String zipFilePath = zip(input.getString("targetDirOrFile"), input.getString("outputDir"));

		// zipファイルを読み込む
		try (BufferedInputStream zipFile = new FileUtil().getBufferedInputStream(zipFilePath)) {

			// zipファイルパスから出力ファイルパスを作成する
			String encodeResultFilePath = zipFilePath.substring(0, zipFilePath.lastIndexOf(".zip"))
					+ "Encode.txt";

			// 出力ファイルを作成する
			try (BufferedWriter encodeResultFile = new FileUtil().getBufferedWriter(encodeResultFilePath)) {

				// 暗号化を実行する
				doEncrypt(zipFile, encodeResultFile, charset);

				// 出力パラメータに秘密鍵ファイルと暗号化ファイルのファイルパスを設定する
				output.putString("encodeResultFilePath", encodeResultFilePath);
			}

		} catch (Exception e) {
			throw new ApplicationInternalException(new LogUtil().handleException(e));
		}

		// zipファイルを削除する
		new FileUtil().deleteFileOrDir(new File(zipFilePath));
	}

	private String zip(String targetDirOrFile, String outputDir) {

		// zipサービスを実行する
		var input = new GenericParam();
		input.putString("targetDirOrFile", targetDirOrFile);
		input.putString("outputDir", outputDir);
		var output = new GenericParam();
		var service = new ZipService();
		service.doService(input, output);

		// 生成されたzipファイルのファイルパスを呼び出し側に返却する
		return output.getString("zipFilePath");
	}

	private void doEncrypt(BufferedInputStream zipFile, BufferedWriter encodeResultFile,
			String charset) throws Exception {

		// メモリが枯渇しないよう、一定のデータ量ごとに処理を行う
		byte[] buf = new byte[8192];
		int bytesRead = 0;
		while ((bytesRead = zipFile.read(buf, 0, buf.length)) != -1) {

			// エンコード対象のbyte配列を新たに作成し、ファイルから読み込んだデータをコピーする
			byte[] target = new byte[bytesRead];
			System.arraycopy(buf, 0, target, 0, bytesRead);

			// Base64URLエンコードを実行する
			byte[] urlEncodeResult = Base64.getUrlEncoder().encode(target);

			// 出力ファイルにBase64URLエンコード結果の文字列を書き込む
			encodeResultFile.write(new String(urlEncodeResult, Charset.forName(charset)));

			// デコード単位が分かるよう、改行を入れる
			encodeResultFile.newLine();
		}
	}
}
