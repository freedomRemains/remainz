package com.remainz.common.service.dbmng.common;

import java.io.BufferedReader;
import java.io.IOException;
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
import com.remainz.common.util.LogUtil;
import com.remainz.common.util.Mu;

/**
 * テーブルデータ取得のSQLを生成するクラスです。
 */
public class GetAllTableSelectSqlService implements ServiceInterface {

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

		// テーブル定義を取得する
		getTableSql(input, output);
	}

	private void getTableSql(GenericParam input, GenericParam output) throws Exception {

		// テーブル名リストファイルパスが指定されている場合は、ファイルからテーブル名リストを取得して処理する
		String tableNameListFilePath = input.getString("tableNameListFilePath");
		if (Cu.isNotEmpty(tableNameListFilePath)) {
			ArrayList<String> tableNameList = getTableNameList(tableNameListFilePath);

			// テーブル名リストファイルのパスをログに記録する
			logger.info(new Mu().msg("msg.detectTableNameListFilePath", tableNameListFilePath));

			// テーブル名リスト内の全てのデータを処理するまでループ
			for (String tableName : tableNameList) {

				// テーブル定義を取得する(テーブル名は大文字で取得する)
				tableName = tableName.toUpperCase();
				getTableSelectSqlByTableName(input, output, tableName);
			}

			// 呼び出し側に復帰する
			return;
		}

		// 入力パラメータから全テーブル名を取得する
		ArrayList<LinkedHashMap<String, String>> recordList = input.getRecordList("tableNameList");
		if (recordList != null && recordList.size() > 0) {

			// オンメモリにテーブル名リストがある旨をログに記録する
			logger.info(new Mu().msg("msg.detectedTableNameListOnMemory"));

			// テーブル名リスト内の全てのデータを処理するまでループ
			for (LinkedHashMap<String, String> columnMap : recordList) {
				for (Map.Entry<String, String> entry : columnMap.entrySet()) {

					// テーブル定義を取得する(テーブル名は大文字で取得する)
					String tableName = columnMap.get(entry.getKey()).toUpperCase();
					getTableSelectSqlByTableName(input, output, tableName);
				}
			}

			// 呼び出し側に復帰する
			return;
		}
	}

	private ArrayList<String> getTableNameList(String tableNameListFilePath) {

		// 戻り値変数を作成する
		ArrayList<String> tableNameList = new ArrayList<String>();

		// テーブル名リストファイルを開く
		try (BufferedReader tableNameListFile = new FileUtil().getBufferedReader(tableNameListFilePath)) {

			// 全ての行を処理するまでループ
			String line = "";
			while ((line = tableNameListFile.readLine()) != null) {
				tableNameList.add(line);
			}

		} catch (IOException e) {
			throw new ApplicationInternalException(new LogUtil().handleException(e));
		}

		// テーブル名リストを呼び出し側に戻す
		return tableNameList;
	}

	private void getTableSelectSqlByTableName(GenericParam input, GenericParam output, String tableName)
			throws Exception{

		// テーブル名に基づいて、サービスを呼び出す
		String tableDefFilePath = input.getString("dirPath") + PATH_DELM + input.getString("defPath") + PATH_DELM
				+ tableName + ".txt";
		GetTableSelectSqlService getTableSelectSqlService = new GetTableSelectSqlService();
		input.putString("tableName", tableName);
		input.putString("tableDefFilePath", tableDefFilePath);
		getTableSelectSqlService.doService(input, output);
	}
}
