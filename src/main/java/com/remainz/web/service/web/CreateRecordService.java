package com.remainz.web.service.web;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.remainz.common.db.DbInterface;
import com.remainz.common.exception.ApplicationInternalException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.service.ServiceInterface;
import com.remainz.common.util.Cu;
import com.remainz.common.util.InputCheckUtil;
import com.remainz.common.util.LogUtil;
import com.remainz.common.util.Mu;

public class CreateRecordService implements ServiceInterface {

	/** ロガー */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	@Override
	public void doService(GenericParam input, GenericParam output) {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkDb(input);
		inputCheckUtil.checkParam(input, "accountId");
		inputCheckUtil.checkParam(input, "tableName");

		try {
			// DBレコードを作成する
			doCreateRecord(input, output);

		} catch (Exception e) {
			throw new ApplicationInternalException(new LogUtil().handleException(e));
		}
	}

	private void doCreateRecord(GenericParam input, GenericParam output) throws Exception {

		// 入力パラメータ中の除外対象文字列を定義する
		String accountId = input.getString("accountId");
		String tableName = input.getString("tableName");
		String[] excludes = new String[] {"tableName", "recordId", "accountId", "requestKind",
				"requestUri", "scriptId", "sessionId", "errMsgKey", "requireRuledNumber"};
		var excludeList = Arrays.asList(excludes);

		// テーブル定義を取得する
		var tableDef = getTableDef(input.getDb(), tableName);

		// 入力パラメータにサロゲートキー(tableName + "_ID")を追加する
		boolean requireRuledNumber = Cu.isNotEmpty(input.getString("requireRuledNumber")) ? true : false;
		input.putString(tableName + "_ID", getNextId(input.getDb(), tableName, requireRuledNumber));

		// DBレコード作成のSQLを生成する
		StringBuilder sql = new StringBuilder(getInsertColumnsString(
				input, tableName, excludeList, tableDef));
		sql.append(") VALUES(");
		ArrayList<String> paramList = new ArrayList<String>();
		sql.append(getInsertValueString(input, tableName, accountId, excludeList, tableDef, paramList));
		sql.append(")");

		// SQLをログに記録する
		logger.info(new Mu().msg("msg.common.sql", sql));
		logger.info(new Mu().msg("msg.common.sqlParam", paramList.toString()));	

		// DBレコードを更新する
		Integer updateCnt = input.getDb().update(sql.toString(), paramList);

		// テーブル名とレコードIDを出力パラメータに設定する
		output.putString("tableName", tableName);
		output.putString("updateCnt", updateCnt.toString());
	}

	private ArrayList<LinkedHashMap<String, String>> getTableDef(DbInterface db, String tableName) throws SQLException {

		// テーブル定義を取得する
		String sql = "SELECT TABLE_NAME, FIELD_NAME FROM TBL_DEF WHERE TABLE_NAME = ? ORDER BY TBL_DEF_ID";
		var paramList = new ArrayList<String>();
		paramList.add(tableName);
		return db.select(sql, paramList);
	}

	private String getNextId(DbInterface db, String tableName, boolean requireRuledNumber) throws SQLException {

		// サロゲートキー(tableName + "_ID")の最大値を取得する
		var recordList = db.select("SELECT MAX(" + tableName + "_ID) FROM " + tableName);
		if (recordList.get(0).get("MAX(" + tableName + "_ID)") == null) {
			return "1";
		}
		int maxId = Integer.parseInt(recordList.get(0).get("MAX(" + tableName + "_ID)"));
		if (requireRuledNumber) {
			return Integer.toString((maxId / 100 * 100) + 101);
		} else {
			return Integer.toString(maxId + 1);
		}
	}

	private String getInsertColumnsString(GenericParam input, String tableName, List<String> excludeList,
			ArrayList<LinkedHashMap<String, String>> tableDef) throws SQLException {

		// DBレコード作成のSQLを生成する
		StringBuilder parts = new StringBuilder();
		for (String key : input.getStringMapKeySet()) {

			// 除外対象の入力パラメータを検知した場合は、ループの先頭に戻る
			if (excludeList.contains(key)) {
				continue;
			}

			// INSERT文に列挙するカラム名を追加する
			if (parts.length() == 0) {
				parts.append("INSERT INTO " + tableName + "(" + key);
			} else {
				parts.append(", " + key);
			}
		}

		// 固定カラムの情報を追加する
		if (hasColumn(tableDef, "VERSION")) {
			parts.append(", VERSION");
		}
		if (hasColumn(tableDef, "IS_DELETED")) {
			parts.append(", IS_DELETED");
		}
		if (hasColumn(tableDef, "CREATED_BY")) {
			parts.append(", CREATED_BY");
		}
		if (hasColumn(tableDef, "CREATED_AT")) {
			parts.append(", CREATED_AT");
		}
		if (hasColumn(tableDef, "UPDATED_BY")) {
			parts.append(", UPDATED_BY");
		}
		if (hasColumn(tableDef, "UPDATED_AT")) {
			parts.append(", UPDATED_AT");
		}

		// 生成したINSERT対象カラムを記述したSQLの部分文字列を呼び出し側に返却する
		return parts.toString();
	}

	private boolean hasColumn(ArrayList<LinkedHashMap<String, String>> tableDef, String columnName) {
		for (LinkedHashMap<String, String> columnMap : tableDef) {
			for (Map.Entry<String, String> entry : columnMap.entrySet()) {
				if (entry.getValue().equals(columnName)) {
					return true;
				}
			}
		}
		return false;
	}

	private String getInsertValueString(GenericParam input, String tableName, String accountId,
			List<String> excludeList, ArrayList<LinkedHashMap<String, String>> tableDef,
			ArrayList<String> paramList) throws SQLException {

		// 現在日付を文字列として準備する
		var dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = dateFormat.format(new Date());

		// DBレコード作成のSQLを生成する
		StringBuilder parts = new StringBuilder();
		for (String key : input.getStringMapKeySet()) {

			// 除外対象の入力パラメータを検知した場合は、ループの先頭に戻る
			if (excludeList.contains(key)) {
				continue;
			}

			// INSERT文に列挙するカラム名を追加する
			if (parts.length() == 0) {
				parts.append("?");
			} else {
				parts.append(", ?");
			}
			paramList.add(input.getString(key));
		}

		// 固定カラムの情報を追加する
		if (hasColumn(tableDef, "VERSION")) {
			parts.append(", ?");
			paramList.add("1");
		}
		if (hasColumn(tableDef, "IS_DELETED")) {
			parts.append(", ?");
			paramList.add("0");
		}
		if (hasColumn(tableDef, "CREATED_BY")) {
			parts.append(", ?");
			paramList.add(accountId);
		}
		if (hasColumn(tableDef, "CREATED_AT")) {
			parts.append(", ?");
			paramList.add(dateString);
		}
		if (hasColumn(tableDef, "UPDATED_BY")) {
			parts.append(", ?");
			paramList.add(accountId);
		}
		if (hasColumn(tableDef, "UPDATED_AT")) {
			parts.append(", ?");
			paramList.add(dateString);
		}

		// 生成したINSERT対象カラムを記述したSQLの部分文字列を呼び出し側に返却する
		return parts.toString();
	}
}
