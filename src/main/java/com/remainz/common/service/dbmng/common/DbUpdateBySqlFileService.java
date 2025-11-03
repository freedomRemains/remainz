package com.remainz.common.service.dbmng.common;

import com.remainz.common.db.DbInterface;
import com.remainz.common.param.GenericParam;
import com.remainz.common.service.ServiceInterface;
import com.remainz.common.service.fs.TextFileProcedureService;
import com.remainz.common.util.InputCheckUtil;

/**
 * ファイルとして用意されているupdateのSQLを実行するサービスです。<br>
 * <br>
 * [input][必須][sqlFilePath]SQLファイルパス<br>
 * [input][必須][resultKey]結果を格納するためのキー(outputにこのキーで結果が格納される)<br>
 * [output][inputのresultKeyで指定したキー]updateの結果(int値を文字列化したもの)が格納される<br>
 */
public class DbUpdateBySqlFileService implements ServiceInterface {

	@Override
	public void doService(GenericParam input, GenericParam output) throws Exception {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkDb(input);
		inputCheckUtil.checkParam(input, "sqlFilePath");
		inputCheckUtil.checkParam(input, "resultKey");

		// SQLファイルに基づくselectを実行する
		output.putString(input.getString("resultKey"),
				updateBySqlFile(input.getDb(), input.getString("sqlFilePath")));
	}

	private String updateBySqlFile(DbInterface db, String sqlFilePath) throws Exception {

		// SQLファイルからSQLを取得する
		String sqlOrSqls = getSqlFromFile(sqlFilePath);

		// セミコロンが含まれている場合は、SQLを分割する
		String[] sqls = sqlOrSqls.split(";");

		// SQLファイルからのSQL実行の場合は、セミコロンで区切られた複数のSQL実行を許容する
		// (当該サービスはそもそも、信頼できるファイルを指定する前提であるため)
		int ret = 0;
		if (sqls.length <= 1) {

			// SQLにセミコロンが含まれていない場合は、単純にSQLを実行する
			ret = db.update(sqlOrSqls);

		} else {

			// SQLにセミコロンが含まれている場合は、複数のSQLを順番に実行する
			for (String sql : sqls) {
				ret = db.update(sql);
			}
		}

		// 結果を呼び出し側に返却する
		return Integer.toString(ret);
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
