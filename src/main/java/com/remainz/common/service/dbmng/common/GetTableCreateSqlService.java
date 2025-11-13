package com.remainz.common.service.dbmng.common;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

import com.remainz.common.param.GenericParam;
import com.remainz.common.service.ServiceInterface;
import com.remainz.common.util.Cu;
import com.remainz.common.util.FileUtil;
import com.remainz.common.util.InputCheckUtil;
import com.remainz.common.util.Mu;

/**
 * 各テーブルのSQLを取得するクラスです。
 */
public class GetTableCreateSqlService implements ServiceInterface {

	/** ロガー */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	@Override
	public void doService(GenericParam input, GenericParam output) throws Exception {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkDb(input);
		inputCheckUtil.checkParam(input, "dirPath");
		inputCheckUtil.checkParam(input, "defPath");
		inputCheckUtil.checkParam(input, "sqlPath");
		inputCheckUtil.checkParam(input, "tableName");

		// テーブルのSQLを取得する
		getTableSql(input, output);
	}

	private void getTableSql(GenericParam input, GenericParam output) throws Exception {

		// 入力パラメータからテーブル名を取得する
		String tableName = input.getString("tableName");

		// リソースパス配下の "10_dbdef/20_auto_created" にあるテーブル定義ファイルを読み込む
		ArrayList<LinkedHashMap<String, String>> recordList = getTableDef(input, tableName, logger);

		// リソースパス配下の "30_sql/20_auto_created" にテキストファイルを生成する
		String sqlFilePath = input.getString("dirPath") + PATH_DELM + input.getString("sqlPath") + "/CREATE_"
				+ tableName + ".txt";
		try (BufferedWriter tableSqlFile = new FileUtil().getBufferedWriter(sqlFilePath)) {

			// DB定義を取得してファイルに書き込む
			writeTableSql(output, tableName, tableSqlFile, recordList);
		}
	}

	public static ArrayList<LinkedHashMap<String, String>> getTableDef(GenericParam input,
			String tableName, Logger logger) throws Exception {

		// テーブル定義をオンメモリで持っている場合は、呼び出し側に返却する
		ArrayList<LinkedHashMap<String, String>> recordList = input.getRecordList("tableDef" + tableName);
		if (recordList != null && recordList.size() > 0) {
			logger.info(new Mu().msg("msg.detectedTableDefOnMemory", "tableDef" + tableName));
			return recordList;
		}

		// テーブル定義をファイルから読み込む
		GetTableDefByFileService getTableDefByFileService = new GetTableDefByFileService();
		GenericParam output = new GenericParam();
		getTableDefByFileService.doService(input, output);
		recordList = output.getRecordList("tableDef" + tableName);

		return recordList;
	}

	private void writeTableSql(GenericParam output, String tableName, BufferedWriter tableSqlFile,
			ArrayList<LinkedHashMap<String, String>> recordList) throws SQLException, IOException {

		// SQLを生成する
		String lineSepr = output.getLineSepr();
		StringBuffer sql = new StringBuffer();
		sql.append("CREATE TABLE IF NOT EXISTS " + tableName + "(" + lineSepr + "\t");
		StringBuffer columnPart = new StringBuffer();
		StringBuffer primaryKeyPart = new StringBuffer();
		for (LinkedHashMap<String, String> columnMap : recordList) {
			if (columnPart.length() > 0) {
				columnPart.append("," + lineSepr + "\t");
			}
			columnPart.append(columnMap.get("Field").toUpperCase() + " " + columnMap.get("Type").toUpperCase());
			if ("NO".equals(columnMap.get("Null"))) {
				columnPart.append(" NOT NULL");
			}
			if ("PRI".equals(columnMap.get("Key"))) {
				if (primaryKeyPart.length() > 0) {
					primaryKeyPart.append(", ");
				}
				primaryKeyPart.append(columnMap.get("Field").toUpperCase());
			}
			if (Cu.isNotEmpty(columnMap.get("Default")) && !"null".equals(columnMap.get("Default"))) {
				if ("CURRENT_DATE".equals(columnMap.get("Default").toUpperCase())
						|| "CURRENT_TIME".equals(columnMap.get("Default").toUpperCase())
						|| "CURRENT_TIMESTAMP".equals(columnMap.get("Default").toUpperCase())) {
					columnPart.append(" DEFAULT " + columnMap.get("Default"));
				} else {
					columnPart.append(" DEFAULT '" + columnMap.get("Default") + "'");
				}
			}
			if (Cu.isNotEmpty(columnMap.get("Extra"))) {
				columnPart.append(" " + columnMap.get("Extra").toUpperCase());
			}
		}
		sql.append(columnPart);
		if (primaryKeyPart.length() > 0) {
			sql.append("," + lineSepr + "\tPRIMARY KEY(" + primaryKeyPart + ")");
		}
		sql.append(lineSepr + ");");
		logger.info(new Mu().msg("msg.common.sql", sql));

		// SQLをファイルに書き込む
		tableSqlFile.write(sql.toString());
		tableSqlFile.newLine();
	}
}
