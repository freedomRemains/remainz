package com.remainz.common.service.dbmng.common;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.remainz.common.db.DbInterface;
import com.remainz.common.param.GenericParam;
import com.remainz.common.service.ServiceInterface;
import com.remainz.common.service.fs.TextFileProcedureService;
import com.remainz.common.util.InputCheckUtil;

/**
 * ファイルとして用意されているselectのSQLを実行するサービスです。
 */
public class DbSelectBySqlFileService implements ServiceInterface {

	@Override
	public void doService(GenericParam input, GenericParam output) throws Exception {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkDb(input);
		inputCheckUtil.checkParam(input, "sqlFilePath");
		inputCheckUtil.checkParam(input, "recordListKey");

		// SQLファイルに基づくselectを実行する
		output.putRecordList(input.getString("recordListKey"),
				selectBySqlFile(input.getDb(), input.getString("sqlFilePath")));
	}

	private ArrayList<LinkedHashMap<String, String>> selectBySqlFile(
			DbInterface db, String sqlFilePath) throws Exception {

		// SQLファイルからSQLを取得する
		String sql = getSqlFromFile(sqlFilePath);

		// SQLを実行し、結果を呼び出し側に返却する
		return db.select(sql);
	}

	private String getSqlFromFile(String sqlFilePath) throws Exception {

		// SQLファイルからSQLを取得する
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		TextFileProcedureService service = new TextFileProcedureService();
		input.putString("filePath", sqlFilePath);
		input.putString("procName", "com.remainz.common.service.proc.SqlFromFileProc");
		service.doService(input, output);
		return output.getString("sqlFromFile");
	}
}
