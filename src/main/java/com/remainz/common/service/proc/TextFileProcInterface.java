package com.remainz.common.service.proc;

import com.remainz.common.param.GenericParam;

/**
 * テキストファイルを1行ずつ処理するためのインターフェースです。
 */
public interface TextFileProcInterface {

	void doProc(GenericParam input, GenericParam output, String filePath) throws Exception;
}
