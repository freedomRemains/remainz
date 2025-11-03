package com.remainz.common.service.proc;

import java.io.File;

import com.remainz.common.db.DbInterface;
import com.remainz.common.param.GenericParam;
import com.remainz.common.service.dbmng.common.GetTableCreateSqlService;
import com.remainz.common.service.dbmng.common.GetTableDropSqlService;
import com.remainz.common.util.InputCheckUtil;

public class CreateSqlByDefProc extends DirProcBase {

	protected void doFileProc(GenericParam input, GenericParam output, String dirPath, File parentDir,
			File currentFile) throws Exception {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkDb(input);
		inputCheckUtil.checkParam(input, "dirPath");
		inputCheckUtil.checkParam(input, "defPath");
		inputCheckUtil.checkParam(input, "sqlPath");

		// 親ディレクトリがDB定義ディレクトリでない場合は何もしない
		String parentPath = parentDir.getPath().replaceAll("\\\\", "/");
		if (!parentPath.contains(input.getString("defPath"))) {
			return;
		}

		// ".keep"と"tableNameList.txt"は処理対象から除外する
		if (".keep".equals(currentFile.getName()) || "tableNameList.txt".equals(currentFile.getName())) {
			return;
		}

		// ファイルサイズが0の場合は、何もせず即時終了する
		if (currentFile.length() == 0) {
			return;
		}

		// ファイル名からテーブル名を求める
		String tableName = currentFile.getName().substring(0, currentFile.getName().lastIndexOf(".txt"));

		// DROPのSQLを生成する
		createDropSql(input.getDb(), input.getString("dirPath"), input.getString("sqlPath"),
				tableName);

		// CREATEのSQLを生成する
		createCreateSql(input.getDb(), input.getString("dirPath"), input.getString("defPath"),
				input.getString("sqlPath"), tableName);
	}

	private void createDropSql(DbInterface db, String dirPath, String sqlPath, String tableName)
			throws Exception {

		// SQLファイルの内容に基づき、DROPのSQLを生成する
		var input = new GenericParam();
		input.setDb(db);
		input.putString("dirPath", dirPath);
		input.putString("sqlPath", sqlPath);
		input.putString("tableName", tableName);
		var output = new GenericParam();
		var service = new GetTableDropSqlService();
		service.doService(input, output);
	}

	private void createCreateSql(DbInterface db, String dirPath, String defPath, String sqlPath, 
			String tableName) throws Exception {

		// SQLファイルの内容に基づき、DROPのSQLを生成する
		var input = new GenericParam();
		input.setDb(db);
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("sqlPath", sqlPath);
		input.putString("tableName", tableName);
		var output = new GenericParam();
		var service = new GetTableCreateSqlService();
		service.doService(input, output);
	}
}
