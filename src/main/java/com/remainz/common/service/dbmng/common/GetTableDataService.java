package com.remainz.common.service.dbmng.common;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.remainz.common.db.DbInterface;
import com.remainz.common.param.GenericParam;
import com.remainz.common.service.ServiceInterface;
import com.remainz.common.util.Cu;
import com.remainz.common.util.FileUtil;
import com.remainz.common.util.InputCheckUtil;

/**
 * 各テーブルのデータを取得するクラスです。
 */
public class GetTableDataService implements ServiceInterface {

	@Override
	public void doService(GenericParam input, GenericParam output) throws Exception {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkDb(input);
		inputCheckUtil.checkParam(input, "dirPath");
		inputCheckUtil.checkParam(input, "dataPath");
		inputCheckUtil.checkParam(input, "sqlPath");
		inputCheckUtil.checkParam(input, "tableName");

		// テーブルのデータを取得する
		getTableData(input, output);
	}

	private void getTableData(GenericParam input, GenericParam output) throws Exception {

		// 入力パラメータからテーブル名を取得する
		String tableName = input.getString("tableName");

		// DBデータを書き込むTSVファイルを作成する
		String filePath = input.getString("dirPath") + PATH_DELM + input.getString("dataPath") + PATH_DELM
				+ tableName + ".txt";
		try (BufferedWriter tableTsvFile = new FileUtil().getBufferedWriter(filePath)) {

			// SQLを実行し、テーブルデータを取得する
			getTableDataBySql(input, tableTsvFile, tableName);
		}
	}

	private void getTableDataBySql(GenericParam input, BufferedWriter tableTsvFile, String tableName)
			throws Exception {

		// レコードが1件も存在しない場合は、即時終了する
		ArrayList<LinkedHashMap<String, String>> recordList = executeSelectBySqlFile(input.getDb(),
				input.getString("dirPath"), input.getString("sqlPath"), tableName);
		if (recordList.size() == 0) {
			return;
		}

		// TSVのヘッダ行を書き込む
		StringBuffer tsvHeader = new StringBuffer();
		for (String key : recordList.get(0).keySet()) {
			if (tsvHeader.length() > 0) {
				tsvHeader.append("\t");
			}
			tsvHeader.append(key);
		}
		tableTsvFile.write(tsvHeader.toString());
		tableTsvFile.newLine();

		// SELECT結果をTSV形式にしてファイルに書き込む
		for (LinkedHashMap<String, String> columnMap : recordList) {
			StringBuffer tsvLine = new StringBuffer();
			for (Map.Entry<String, String> entry : columnMap.entrySet()) {
				if (tsvLine.length() > 0) {
					tsvLine.append("\t");
				}

				// シングルクオートのエスケープがある場合は補完する
				String columnValue = columnMap.get(entry.getKey());
				if (Cu.isNotEmpty(columnValue)) {
					columnValue = columnValue.replaceAll("'", "''");
				}
				tsvLine.append(columnValue);
			}
			tableTsvFile.write(tsvLine.toString());
			tableTsvFile.newLine();
		}
	}

	private ArrayList<LinkedHashMap<String, String>> executeSelectBySqlFile(DbInterface db,
			String dirPath, String sqlPath, String tableName) throws Exception {

		// サービス実行に必要なパラメータを設定する
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		String sqlFilePath = dirPath + PATH_DELM + sqlPath + PATH_DELM + "/SELECT_" + tableName + ".txt";
		input.setDb(db);
		input.putString("sqlFilePath", sqlFilePath);
		input.putString("recordListKey", tableName);

		// リソースパス配下の "30_sql/20_auto_created" にあるSELECTのSQLを実行する
		DbSelectBySqlFileService service = new DbSelectBySqlFileService();
		service.doService(input, output);

		// SELECT結果を呼び出し側に返却する
		return output.getRecordList(tableName);
	}
}
