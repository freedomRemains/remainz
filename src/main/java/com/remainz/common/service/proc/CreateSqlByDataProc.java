package com.remainz.common.service.proc;

import java.io.File;

import com.remainz.common.db.DbInterface;
import com.remainz.common.param.GenericParam;
import com.remainz.common.service.dbmng.common.GetTableInsertSqlService;
import com.remainz.common.util.InputCheckUtil;

public class CreateSqlByDataProc extends DirProcBase {

	protected void doFileProc(GenericParam input, GenericParam output, String dirPath, File parentDir,
			File currentFile) throws Exception {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkDb(input);
		inputCheckUtil.checkParam(input, "dirPath");
		inputCheckUtil.checkParam(input, "defPath");
		inputCheckUtil.checkParam(input, "dataPath");
		inputCheckUtil.checkParam(input, "sqlPath");

		// 親ディレクトリがDBデータディレクトリでない場合は何もしない
		String parentPath = parentDir.getPath().replaceAll("\\\\", "/");
		if (!parentPath.contains(input.getString("dataPath"))) {
			return;
		}

		// ".keep"は処理対象から除外する
		if (".keep".equals(currentFile.getName())) {
			return;
		}

		// ファイルサイズが0の場合は、何もせず即時終了する
		if (currentFile.length() == 0) {
			return ;
		}

		// ファイル名からテーブル名を求める
		String tableName = currentFile.getName().substring(0, currentFile.getName().lastIndexOf(".txt"));

		// INSERTのSQLを生成する
		createInsertSql(input.getDb(), currentFile, input.getString("dirPath"), input.getString("defPath"),
				input.getString("dataPath"), input.getString("sqlPath"), tableName);
	}

	private void createInsertSql(DbInterface db, File targetFile, String dirPath, String defPath,
			String dataPath, String sqlPath, String tableName) throws Exception {

		// SQLファイルの内容に基づき、INSERTのSQLを生成する
		var input = new GenericParam();
		input.setDb(db);
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("dataPath", dataPath);
		input.putString("sqlPath", sqlPath);
		input.putString("tableName", tableName);
		var output = new GenericParam();
		var service = new GetTableInsertSqlService();
		service.doService(input, output);
	}
}
