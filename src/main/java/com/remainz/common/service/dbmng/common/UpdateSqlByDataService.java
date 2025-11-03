package com.remainz.common.service.dbmng.common;

import com.remainz.common.db.DbInterface;
import com.remainz.common.param.GenericParam;
import com.remainz.common.service.ServiceInterface;
import com.remainz.common.service.fs.DirRecursiveService;
import com.remainz.common.util.InputCheckUtil;

public class UpdateSqlByDataService implements ServiceInterface {

	@Override
	public void doService(GenericParam input, GenericParam output) throws Exception {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkDb(input);
		inputCheckUtil.checkParam(input, "dirPath");
		inputCheckUtil.checkParam(input, "defPath");
		inputCheckUtil.checkParam(input, "dataPath");
		inputCheckUtil.checkParam(input, "sqlPath");
		inputCheckUtil.checkParam(input, "authorizedPath");
		inputCheckUtil.checkParam(input, "autoCreatedPath");
		inputCheckUtil.checkParam(input, "forUpdatePath");

		// ディレクトリ再帰処理を実行する
		executeDirRecursiveService(input.getDb(), input.getString("dirPath"), input.getString("defPath"),
				input.getString("dataPath"), input.getString("sqlPath"), input.getString("autoCreatedPath"));
		executeDirRecursiveService(input.getDb(), input.getString("dirPath"), input.getString("defPath"),
				input.getString("dataPath"), input.getString("sqlPath"), input.getString("authorizedPath"));
		executeDirRecursiveService(input.getDb(), input.getString("dirPath"), input.getString("defPath"),
				input.getString("dataPath"), input.getString("sqlPath"), input.getString("forUpdatePath"));
	}

	private void executeDirRecursiveService(DbInterface db, String dirPath, String defPath, 
			String dataPath, String sqlPath, String subPath) throws Exception {

		// ディレクトリ再帰サービスを実行する
		var input = new GenericParam();
		input.setDb(db);
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath + PATH_DELM + subPath);
		input.putString("dataPath", dataPath + PATH_DELM + subPath);
		input.putString("sqlPath", sqlPath + PATH_DELM + subPath);
		input.putString("procName", "com.remainz.common.service.proc.CreateSqlByDataProc");
		var output = new GenericParam();
		var service = new DirRecursiveService();
		service.doService(input, output);
	}
}
