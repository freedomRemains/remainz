package com.remainz.common.service.proc;

import org.apache.log4j.Logger;

import com.remainz.common.param.GenericParam;
import com.remainz.common.util.Mu;

/**
 * SQLを記述したテキストファイルからSQLを抽出するためのプロシージャです。
 */
public class SqlFromFileProc extends TextFileProcBase {

	/** ロガー */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/** SQL */
	private StringBuffer sql;

	public SqlFromFileProc() {
		sql = new StringBuffer();
	}

	protected void doProcByLine(GenericParam input, GenericParam output, String filePath, String line, int lineNumber) {

		// 行データのタブを半角スペースに、スペース複数を1つのスペースに変換した上で、SQLに追加する
		line = line.replaceAll("\t", " ");
		line = line.replaceAll(" +", " ");
		sql.append(line);
	}

	protected void doProcOnTextFileEnd(GenericParam input, GenericParam output, String filePath) {

		// SQLを出力パラメータに設定する
		output.putString("sqlFromFile", sql.toString());

		// ログを記録する
		logger.info(new Mu().msg("msg.common.sql", output.getString("sqlFromFile")));
	}
}
