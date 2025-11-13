package com.remainz.web.util;

import com.remainz.common.util.InnerClassPathProp;

/**
 * JavaWebプロパティ
 */
public class RwProp extends InnerClassPathProp {

	/**
	 * コンストラクタ
	 */
	public RwProp() {

		// プロパティファイル名を固定としてプロパティを取得する
		super("rw.properties");
	}
}
