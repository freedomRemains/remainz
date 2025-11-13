package com.remainz.common.service;

import com.remainz.common.param.GenericParam;

public interface ServiceInterface {

	/** パス区切り文字 */
	public static final String PATH_DELM = "/";

	/**
	 * サービスを実行します。
	 *
	 * @param input 入力パラメータ
	 * @param output 出力パラメータ
	 * @throws Exception 例外
	 */
	void doService(GenericParam input, GenericParam output) throws Exception;
}
