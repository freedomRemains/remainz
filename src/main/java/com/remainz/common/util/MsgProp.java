package com.remainz.common.util;

/**
 * メッセージプロパティ
 */
public class MsgProp extends InnerClassPathProp {

	/**
	 * コンストラクタ
	 */
	public MsgProp() {

		// プロパティファイル名を固定としてプロパティを取得する
		super("message.properties");
	}
}
