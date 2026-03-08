package com.remainz.common.util;

import java.text.MessageFormat;

/**
 * メッセージユーティリティクラスです。
 */
public class Mu {

	/**
	 * インスタンス化禁止を明示するため、privateコンストラクタを定義する。
	 */
	private Mu() {
	}

	public static String msg(String key, Object... args) {
		return MessageFormat.format(new MsgProp().get(key), args);
	}
}
