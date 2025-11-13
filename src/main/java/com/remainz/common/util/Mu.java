package com.remainz.common.util;

import java.text.MessageFormat;

/**
 * メッセージユーティリティクラスです。
 */
public class Mu {

	public String msg(String key, Object... args) {
		return MessageFormat.format(new MsgProp().get(key), args);
	}
}
