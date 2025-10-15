package com.remainz.common.util;

import org.apache.log4j.Logger;

/**
 * log4jを使ったログ記録を行うユーティリティクラスです。
 */
public class LogUtil {

	/** ロガー */
	private Logger logger = null;

	/**
	 * コンストラクタ。
	 */
	public LogUtil() {

		// ロガーを取得する
		logger = Logger.getLogger(this.getClass().getName());
	}

	/**
	 * 例外を処理します。
	 *
	 * @param e 例外
	 * @return 例外の内容を示す文字列
	 */
	public String handleException(Exception e) {

		// スタックトレースを取得する
		String stackTrace = getStackTrace(e);

		// スタックトレースをログに記録し、呼び出し側にも戻す
		logger.error(stackTrace);
		return stackTrace;
	}

	/**
	 * 例外のスタックトレースを取得します。
	 *
	 * @param e 例外
	 * @return 例外のスタックトレース文字列
	 */
	public String getStackTrace(Exception e) {

		// パラメータチェックを行う
		if (e == null) {
			return "Exception class is null.";
		}

		// 改行コードを取得する
		String lineSepr = System.getProperty("line.separator");

		// 例外のメッセージを取得し、改行を追加する
		StringBuffer msg = new StringBuffer();
		if (!Cu.isEmpty(e.getMessage())) {
			msg.append(e.getMessage());
			msg.append(lineSepr);
		}
		msg.append(e.getClass().getName());
		msg.append(lineSepr);

		// 全てのスタックトレースを例外のメッセージに追加する
		for (StackTraceElement stackTraceElement : e.getStackTrace()) {
			msg.append(stackTraceElement.toString());
			msg.append(lineSepr);
		}

		// 作成した文字列を呼び出し側に戻す
		return msg.toString();
	}

	public void recordBytesLog(String title, byte[] bytes) {

		// 16進数文字列に変換したbyte配列のデータをログに記録する
		StringBuilder msg = new StringBuilder();
		for (int index = 0; index < bytes.length; index++) {
			if (msg.length() > 0) {
				msg.append(" ");
			}
			msg.append(String.format("%02x", bytes[index]));
		}
		logger.info(title + msg);
	}
}
