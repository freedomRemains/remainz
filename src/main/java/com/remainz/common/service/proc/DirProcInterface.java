package com.remainz.common.service.proc;

import com.remainz.common.param.GenericParam;

/**
 * ディレクトリを1つずつ処理するためのインターフェースです。
 */
public interface DirProcInterface {

	void doProc(GenericParam input, GenericParam output, String dirPath) throws Exception;
}
