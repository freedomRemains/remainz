package com.remainz.common.service.dbmng.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.remainz.common.exception.ApplicationInternalException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.service.ServiceInterface;
import com.remainz.common.util.Cu;
import com.remainz.common.util.FileUtil;
import com.remainz.common.util.InputCheckUtil;
import com.remainz.common.util.Mu;

/**
 * 各テーブルのSQLを取得するクラスです。
 */
public class GetTableInsertSqlService implements ServiceInterface {

	/** ロガー */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	@Override
	public void doService(GenericParam input, GenericParam output) throws Exception {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkDb(input);
		inputCheckUtil.checkParam(input, "dirPath");
		inputCheckUtil.checkParam(input, "defPath");
		inputCheckUtil.checkParam(input, "dataPath");
		inputCheckUtil.checkParam(input, "sqlPath");
		inputCheckUtil.checkParam(input, "tableName");

		// テーブルのデータを取得する
		getTableData(input, output);
	}

	private void getTableData(GenericParam input, GenericParam output) throws Exception {

		// 入力パラメータからテーブル名を取得する
		String tableName = input.getString("tableName");

		// テーブル定義を取得する
		ArrayList<LinkedHashMap<String, String>> tableDef = GetTableCreateSqlService.getTableDef(
				input, tableName, logger);

		// SQLファイルを作成する
		String filePath = input.getString("dirPath") + PATH_DELM + input.getString("sqlPath") + "/INSERT_"
				+ tableName + ".txt";
		try (BufferedWriter tableSqlFile = new FileUtil().getBufferedWriter(filePath)) {

			// テーブル定義に基づき、INSERTのSQLを生成する
			createInsertSqlByTableData(input, tableSqlFile, tableName, tableDef);
		}
	}

	private void createInsertSqlByTableData(GenericParam input, BufferedWriter tableSqlFile,
			String tableName, ArrayList<LinkedHashMap<String, String>> tableDef) throws Exception {

		// データファイルに基づき、INSERTの元となるDBレコードを生成する
		ArrayList<LinkedHashMap<String, String>> recordList = createRecordListByTableName(input, tableName);

		// DBレコードを全て処理するまでループ
		for (LinkedHashMap<String, String> columnMap : recordList) {

			// INSERTのSQLを生成する
			StringBuffer sql = new StringBuffer("INSERT INTO " + tableName + " (");
			StringBuffer part = new StringBuffer();
			for (String key : columnMap.keySet()) {
				if (part.length() > 0) {
					part.append(", ");
				}
				part.append(key);
			}
			sql.append(part + ") VALUES (");
			part = new StringBuffer();
			for (Map.Entry<String, String> entry : columnMap.entrySet()) {

				// カラムの型に応じたINSERTの文字列を取得する
				appendColumnValueByType(tableDef, part, entry.getKey(), entry.getValue());
			}
			sql.append(part + ");");

			// 生成したINSERTのSQLをファイルに書き込む
			tableSqlFile.write(sql.toString());
			tableSqlFile.newLine();
		}
	}

	private ArrayList<LinkedHashMap<String, String>> createRecordListByTableName(GenericParam input,
			String tableName) throws Exception {

		// 戻り値変数を作成する
		ArrayList<LinkedHashMap<String, String>> recordList = new ArrayList<LinkedHashMap<String, String>>();

		// データファイルを開く
		String dataFilePath = input.getString("dirPath") + PATH_DELM + input.getString("dataPath")
				+ PATH_DELM + tableName + ".txt";
		try (BufferedReader dataFile = new FileUtil().getBufferedReader(dataFilePath)) {
			createRecordListByDataFile(recordList, dataFile);
		}

		return recordList;
	}

	private void createRecordListByDataFile(ArrayList<LinkedHashMap<String, String>> recordList,
			BufferedReader dataFile) throws Exception {

		// 最初の行はヘッダ行と扱う
		String line = dataFile.readLine();

		// ファイルに何も書かれていない場合はレコードなしと扱う(空の戻り値を返却)
		if (Cu.isEmpty(line)) {
			return;
		}

		// TSV値となっているので分割する(空の場合は、空の戻り値を返却)
		String[] headerTsvValues = line.split("\t");

		// 残りの全ての行を処理するまでループ
		while ((line = dataFile.readLine()) != null) {

			// 戻り値変数にレコードを追加する
			LinkedHashMap<String, String> columnMap = new LinkedHashMap<String, String>();
			String[] tsvValues = line.split("\t");
			for (int i = 0; i < headerTsvValues.length; i++) {
				if (i >= tsvValues.length) {
					columnMap.put(headerTsvValues[i], "");
				} else {
					columnMap.put(headerTsvValues[i], tsvValues[i]);
				}
			}
			recordList.add(columnMap);
		}
	}

	private void appendColumnValueByType(ArrayList<LinkedHashMap<String, String>> tableDef,
			StringBuffer part, String columnName, String columnValue) {

		if (isQuoteTarget(getTypeByColumnName(tableDef, columnName))) {

			// カラムがクオーテーション対象の場合は、シングルクオートで囲む
			part.append(", ");
			if ("null".equals(columnValue)) {
				part.append("NULL");
			} else {
				part.append("'" + columnValue + "'");
			}

		} else {

			// カラムがクオーテーション対象でない場合は、シングルクオートで囲まない
			if (part.length() > 0) {
				part.append(", ");
			}
			if ("null".equals(columnValue)) {
				part.append("0");
			} else if (Cu.isEmpty(columnValue)) {
				part.append("0");
			} else {
				part.append(columnValue);
			}
		}
	}

	private boolean isQuoteTarget(String columnType) {
		if ("INT".equalsIgnoreCase(columnType)) {
			return false;
		}
		return true;
	}

	private String getTypeByColumnName(ArrayList<LinkedHashMap<String, String>> tableDef,
			String columnName) {

		// テーブル定義を全て処理するまでループ
		for (LinkedHashMap<String, String> columnMap : tableDef) {
			if (columnMap.get("Field").equals(columnName)) {
				return columnMap.get("Type");
			}
		}

		// カラム名が見つからない場合はエラーとする
		throw new ApplicationInternalException(new Mu().msg("msg.err.common.noColumnInfo",
				columnName));
	}
}
