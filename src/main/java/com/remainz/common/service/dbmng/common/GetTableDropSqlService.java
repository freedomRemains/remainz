package com.remainz.common.service.dbmng.common;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.remainz.common.param.GenericParam;
import com.remainz.common.service.ServiceInterface;
import com.remainz.common.util.FileUtil;
import com.remainz.common.util.InputCheckUtil;
import com.remainz.common.util.Mu;

/**
 * 各テーブルのSQLを取得するクラスです。 <br>
 * <br>
 * [inputに必要なパラメータ]<br>
 * dirPath ディレクトリパス<br>
 * sqlPath SQLパス<br>
 * tableName テーブル名<br>
 * <br>
 * 各パラメータの例<br>
 * dirPath new JwProp().get("base.dir") + PATH_DELM + getResourcePath()<br>
 * sqlPath "30_sql/20_auto_created"<br>
 * tableName "TDICTITEM"<br>
 * + input.getString("defPath") + PATH_DELM + tableName + ".txt";<br>
 */
public class GetTableDropSqlService implements ServiceInterface {

	/** ロガー */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	@Override
	public void doService(GenericParam input, GenericParam output) throws Exception {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkDb(input);
		inputCheckUtil.checkParam(input, "dirPath");
		inputCheckUtil.checkParam(input, "sqlPath");
		inputCheckUtil.checkParam(input, "tableName");

		// テーブルのSQLを取得する
		getTableSql(input, output);
	}

	private void getTableSql(GenericParam input, GenericParam output) throws Exception {

		// 入力パラメータからテーブル名を取得する
		String tableName = input.getString("tableName");

		// リソースパス配下の "30_sql/20_auto_created" にテキストファイルを生成する
		String sqlFilePath = input.getString("dirPath") + PATH_DELM + input.getString("sqlPath")
				+ "/DROP_" + tableName + ".txt";
		try (BufferedWriter tableSqlFile = new FileUtil().getBufferedWriter(sqlFilePath)) {

			// DB定義を取得してファイルに書き込む
			writeTableSql(output, tableName, tableSqlFile);
		}
	}

	private void writeTableSql(GenericParam output, String tableName, BufferedWriter tableSqlFile)
			throws SQLException, IOException {

		// SQLをファイルに書き込む
		String sql = "DROP TABLE IF EXISTS " + tableName + ";";
		logger.info(new Mu().msg("msg.common.sql", sql));
		tableSqlFile.write(sql);
		tableSqlFile.newLine();
	}
}
