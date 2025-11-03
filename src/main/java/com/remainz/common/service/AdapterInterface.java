package com.remainz.common.service;

import com.remainz.common.param.GenericParam;

public interface AdapterInterface {

	/**
	 * 入力パラメータと出力パラメータのアダプターです。
	 *
	 * @param input 入力パラメータ
	 * @param output 出力パラメータ
	 * @throws Exception 例外
	 */
	void doAdapt(GenericParam input, GenericParam output) throws Exception;
}
