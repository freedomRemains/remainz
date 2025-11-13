package com.remainz.web.service.web;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.remainz.common.db.DbInterface;
import com.remainz.common.exception.ApplicationInternalException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.service.ServiceInterface;
import com.remainz.common.util.Cu;
import com.remainz.common.util.InputCheckUtil;
import com.remainz.common.util.LogUtil;
import com.remainz.common.util.Mu;
import com.remainz.web.util.ErrMsgUtil;

public class GetRelatedRecordService implements ServiceInterface {

	/** ロガー */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	@Override
	public void doService(GenericParam input, GenericParam output) {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkDb(input);
		inputCheckUtil.checkParam(input, "tableName");
		inputCheckUtil.checkParam(input, "recordId");

		try {
			// HTMLを生成する
			if (!doGetRelatedRecord(input, output)) {

				// エラーメッセージIDを取得する
				String errMsgKey = new ErrMsgUtil().getErrMsgKey(input.getDb(),
						input.getString("sessionId"), input.getString("accountId"),
						"1000301");

				// 処理対象レコードが見つからない場合は、DBメンテナンスページにリダイレクトする
				String tableName = input.getString("tableName");
				String uri = "tableDataMainte.html?tableName=" + tableName
						+ "&errMsgKey=" + errMsgKey;
				output.putString("respKind", "redirect");
				output.putString("destination", uri);
			}

		} catch (Exception e) {
			throw new ApplicationInternalException(new LogUtil().handleException(e));
		}
	}

	private boolean doGetRelatedRecord(GenericParam input, GenericParam output) throws SQLException {

		// 処理対象のレコード及びそのレコードのDB定義を取得する
		String targetTableName = input.getString("tableName");
		String recordId = input.getString("recordId");
		var targetRecord = getRecord(input.getDb(), targetTableName, recordId);
		if (targetRecord == null) {
			return false; // 処理対象レコードが存在しない場合は、戻り値falseで呼び出し側に復帰する
		}
		var targetRecordDefList = getTableDefList(input.getDb(), targetTableName);

		// 出力パラメータに関連テーブルを設定する
		output.putRecordList("relatedTableList", new ArrayList<LinkedHashMap<String, String>>());

		// 関連テーブルを取得する
		getRelatedTable(input, output, targetTableName, recordId, targetRecord, targetRecordDefList);

		// 処理がここまで到達した場合は正常終了と判断し、戻り値trueで呼び出し側に復帰する
		return true;
	}

	private LinkedHashMap<String, String> getRecord(
			DbInterface db, String tableName, String recordId) throws SQLException {

		String sql = "SELECT * FROM " + tableName + " WHERE " + tableName + "_ID = ?";
		var paramList = new ArrayList<String>();
		paramList.add(recordId);
		var recordList = db.select(sql, paramList);
		if (recordList.size() == 0) {
			return null;
		}
		return recordList.get(0);
	}

	private ArrayList<LinkedHashMap<String, String>> getTableDefList(
			DbInterface db, String tableName) throws SQLException {

		String sql = "SELECT * FROM TBL_DEF WHERE TABLE_NAME = ?";
		var paramList = new ArrayList<String>();
		paramList.add(tableName);
		return db.select(sql, paramList);
	}

	private void getRelatedTable(GenericParam input, GenericParam output,
			String targetTableName, String recordId,
			LinkedHashMap<String, String> targetRecord,
			ArrayList<LinkedHashMap<String, String>> targetRecordDefList) throws SQLException {

		// 関連テーブルを取得する
		List<String> relatedTableNameList = getRelatedTableNameList(input.getDb(), targetTableName);

		// 関連テーブルを全て処理するまでループ
		for (String relatedTableName : relatedTableNameList) {

			// 関連テーブルの詳細を取得する
			getRelatedTableDetail(output, input.getDb(), targetTableName, recordId, relatedTableName);
		}
	}

	private List<String> getRelatedTableNameList(
			DbInterface db, String tableName) throws SQLException {

		// 関連テーブルを取得する
		String sql = "SELECT * FROM TBL_DEF WHERE FOREIGN_TABLE = ?";
		var paramList = new ArrayList<String>();
		paramList.add(tableName);
		var recordList = db.select(sql, paramList);

		// 関連テーブル名のリストを呼び出し側に返却する
		List<String> relatedTableNameList = new ArrayList<String>();
		for (LinkedHashMap<String, String> columnMap : recordList) {
			if (!relatedTableNameList.contains(columnMap.get("TABLE_NAME"))) {
				relatedTableNameList.add(columnMap.get("TABLE_NAME"));
			}
		}
		return relatedTableNameList;
	}

	private void getRelatedTableDetail(GenericParam output,DbInterface db,
			String targetTableName, String recordId, String relatedTableName)
					throws SQLException {

		// 関連テーブルのDB定義を取得する
		var tableDefList = getTableDefList(db, relatedTableName);

		// 関連テーブルが「DESC_FIELD」を持っていない場合
		if (Cu.isEmpty(tableDefList.get(0).get("DESC_FIELD"))) {

			// 組み合わせテーブルとみなし、1つ先のテーブルまで追跡する
			traceForeignTable(output, db, targetTableName, recordId, relatedTableName, tableDefList);

		} else {

			// 関連テーブルが「DESC_FIELS」を持っている場合は、詳細情報を取得する
			getRelatedTableDetail(output, db, targetTableName, recordId, relatedTableName, tableDefList);
		}
	}

	private void traceForeignTable(GenericParam output, DbInterface db, String targetTableName,
			String recordId, String relatedTableName,
			ArrayList<LinkedHashMap<String, String>> tableDefList) throws SQLException {

		// 出力パラメータから関連テーブルを取得する
		var relatedTableList = output.getRecordList("relatedTableList");

		// DB定義から読み取れる全ての外部テーブルを処理するまでループ
		for (String foreignTableName : getForeignTableNameList(tableDefList, targetTableName)) {

			// 外部テーブルの定義を取得する
			var foreignTableDefList = getTableDefList(db, foreignTableName);

			// 関連テーブルのレコードを取得する
			var relatedTableRecordList = getRelatedTableRecordList(
					db, targetTableName, recordId, relatedTableName);

			// 外部テーブルのレコードを取得する
			var foreignTableRecordList = getForeignTableRecordList(
					db, targetTableName, foreignTableDefList, relatedTableRecordList);

			// 関連テーブルのレコードリストを作成する
			var relatedTable = new LinkedHashMap<String, String>();
			relatedTable.put("TABLE_NAME", tableDefList.get(0).get("TABLE_NAME"));
			relatedTable.put("TABLE_LOGICAL_NAME", tableDefList.get(0).get("TABLE_LOGICAL_NAME"));
			relatedTableList.add(relatedTable);

			// 出力パラメータに外部テーブルのDB定義及びレコードリストを設定する
			output.putRecordList("foreignTableDefList_" + tableDefList.get(0).get("TABLE_NAME"),
					foreignTableDefList);
			output.putRecordList("foreignTableRecordList_" + tableDefList.get(0).get("TABLE_NAME"),
					foreignTableRecordList);
		}
	}

	private List<String> getForeignTableNameList(ArrayList<LinkedHashMap<String, String>> tableDefList,
			String targetTableName) {

		// DB定義を全て処理するまでループ
		String tableName = tableDefList.get(0).get("TABLE_NAME");
		var foreignTableNameList = new ArrayList<String>();
		for (LinkedHashMap<String, String> tableDef : tableDefList) {

			// 主キーとVERSION以降のカラムは処理しない
			if (tableDef.get("FIELD_NAME").equals(tableName + "_ID")
					|| "VERSION".equals(tableDef.get("FIELD_NAME"))
					|| "IS_DELETED".equals(tableDef.get("FIELD_NAME"))
					|| "CREATED_BY".equals(tableDef.get("FIELD_NAME"))
					|| "CREATED_AT".equals(tableDef.get("FIELD_NAME"))
					|| "UPDATED_BY".equals(tableDef.get("FIELD_NAME"))
					|| "UPDATED_AT".equals(tableDef.get("FIELD_NAME"))) {
				continue;
			}

			// 処理対象テーブルは外部テーブルのリストから除外する
			if (tableDef.get("FOREIGN_TABLE").equals(targetTableName)) {
				continue;
			}

			// 外部テーブルの定義を見つけた場合はリストに追加する
			if (Cu.isNotEmpty(tableDef.get("FOREIGN_TABLE"))) {
				foreignTableNameList.add(tableDef.get("FOREIGN_TABLE"));
			}
		}

		// 外部テーブル名のリストを呼び出し側に返却する
		return foreignTableNameList;
	}

	private ArrayList<LinkedHashMap<String, String>> getRelatedTableRecordList(
			DbInterface db, String targetTableName, String recordId,
			String relatedTableName) throws SQLException {

		// 関連テーブルの定義を取得し、WHERE句に列挙するカラム名のリストを作成する
		var relatedTableDefList = getTableDefList(db, relatedTableName);
		var columnNameList = new ArrayList<String>();
		for (var relatedTableDef : relatedTableDefList) {
			if (relatedTableDef.get("FOREIGN_TABLE").equals(targetTableName)) {
				columnNameList.add(relatedTableDef.get("FIELD_NAME"));
			}
		}

		// 関連テーブルのレコードを取得し、呼び出し側に返却する
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM " + relatedTableName);
		StringBuilder part = new StringBuilder();
		for (String columnName : columnNameList) {
			if (part.length() == 0) {
				part.append(" WHERE ");
			} else {
				part.append(" OR ");
			}
			part.append(columnName + " = ?");
		}
		sql.append(part);
		var paramList = new ArrayList<String>();
		for (int index = 0; index < columnNameList.size(); index++) {
			paramList.add(recordId);
		}
		logger.info(new Mu().msg("msg.common.sql", sql));
		logger.info(new Mu().msg("msg.common.sqlParam", paramList.toString()));
		return db.select(sql.toString(), paramList);
	}

	private ArrayList<LinkedHashMap<String, String>> getForeignTableRecordList(
			DbInterface db, String targetTableName,
			ArrayList<LinkedHashMap<String, String>> foreignTableDefList,
			ArrayList<LinkedHashMap<String, String>> relatedTableRecordList) throws SQLException {

		// 関連レコードリストが存在しない場合は、空のレコードリストを呼び出し側に返却する
		if (relatedTableRecordList.isEmpty()) {
			return new ArrayList<LinkedHashMap<String, String>>();
		}

		//　外部テーブルからデータを取得するためのSELECT文を作成する
		String foreignTableName = foreignTableDefList.get(0).get("TABLE_NAME");
		String primaryKeyFieldName = foreignTableDefList.get(0).get("FIELD_NAME");
		String descFieldName = foreignTableDefList.get(0).get("DESC_FIELD");
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT " + primaryKeyFieldName + ", " + descFieldName);
		sql.append(" FROM " + foreignTableName);
		sql.append(" WHERE " + foreignTableName + "_ID IN(");
		StringBuilder part = new StringBuilder();
		for (LinkedHashMap<String, String> relatedTableRecord : relatedTableRecordList) {
			if (part.length() > 0) {
				part.append(", ");
			}
			part.append(relatedTableRecord.get(foreignTableName + "_ID"));
		}
		sql.append(part);
		sql.append(")");

		// ログを記録した上でSQLを実行し、結果を呼び出し側に返却する
		logger.info(new Mu().msg("msg.common.sql", sql));
		return db.select(sql.toString());
	}

	private void getRelatedTableDetail(GenericParam output, DbInterface db, String targetTableName,
			String recordId, String relatedTableName,
			ArrayList<LinkedHashMap<String, String>> tableDefList) throws SQLException {

		// 出力パラメータから関連テーブルを取得する
		var relatedTableList = output.getRecordList("relatedTableList");

		// 関連テーブルのレコードを取得する
		var relatedTableRecordList = getRelatedTableRecordList(
				db, targetTableName, recordId, relatedTableName);

		// 関連テーブルのレコードリストを作成する
		var relatedTable = new LinkedHashMap<String, String>();
		relatedTable.put("TABLE_NAME", tableDefList.get(0).get("TABLE_NAME"));
		relatedTable.put("TABLE_LOGICAL_NAME", tableDefList.get(0).get("TABLE_LOGICAL_NAME"));
		relatedTableList.add(relatedTable);

		// 出力パラメータに外部テーブルのDB定義及びレコードリストを設定する
		output.putRecordList("foreignTableDefList_" + tableDefList.get(0).get("TABLE_NAME"),
				tableDefList);
		output.putRecordList("foreignTableRecordList_" + tableDefList.get(0).get("TABLE_NAME"),
				relatedTableRecordList);
	}
}
