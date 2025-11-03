package com.remainz.common.service.dbmng.common;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.remainz.common.db.DbInterface;
import com.remainz.common.param.GenericParam;
import com.remainz.common.service.ServiceInterface;
import com.remainz.common.util.FileUtil;
import com.remainz.common.util.InputCheckUtil;
import com.remainz.common.util.Mu;

/**
 * テーブル物理名取得が実行可能なDBで、全テーブルの名前を取得するクラスです。<br>
 * <br>
 * [input][必須][db]接続済みDBのインスタンス<br>
 * [input][必須][dirPath]ディレクトリパス<br>
 * [input][必須][defPath]DB定義格納先パス<br>
 * [input][必須][getTableNameListSql]テーブル名リスト取得用SQL<br>
 * [output][tableNameList]テーブル名リスト<br>
 * [output][tableNameListFilePath]生成したテーブル名リストファイルのパス<br>
 */
public class GetTableNameListService implements ServiceInterface {

	/** ロガー */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	@Override
	public void doService(GenericParam input, GenericParam output) throws Exception {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkDb(input);
		inputCheckUtil.checkParam(input, "dirPath");
		inputCheckUtil.checkParam(input, "defPath");
		inputCheckUtil.checkParam(input, "getTableNameListSql");

		// 全テーブルの名前を取得する
		output.putRecordList("tableNameList", getTableNameList(input.getDb(), input.getString("getTableNameListSql")));

		// テーブル名リストをファイルに書き込む
		String tableNameListFilePath = input.getString("dirPath") + PATH_DELM + input.getString("defPath") + PATH_DELM
				+ "tableNameList.txt";
		writeTableNameList(output.getRecordList("tableNameList"), tableNameListFilePath);
		output.putString("tableNameListFilePath", tableNameListFilePath);
	}

	private ArrayList<LinkedHashMap<String, String>> getTableNameList(DbInterface db, String sql) throws Exception {

		// 全テーブルを取得するためのSQLを生成し、ログに記録する
		logger.info(new Mu().msg("msg.common.sql", sql));

		// SQLを実行し、結果を戻り値変数に格納する
		ArrayList<LinkedHashMap<String, String>> recordList = db.select(sql);

		// レコードリストを呼び出し側に戻す
		return recordList;
	}

	private void writeTableNameList(ArrayList<LinkedHashMap<String, String>> recordList, String filePath) throws Exception {

		// テーブル名リストファイルを開く
		try (BufferedWriter tableNameListFile = new FileUtil().getBufferedWriter(filePath)) {

			// 全てのテーブル名を処理するまでループ
			for (LinkedHashMap<String, String> columnMap : recordList) {
				for (Map.Entry<String, String> entry : columnMap.entrySet()) {
					String tableName = columnMap.get(entry.getKey()).toUpperCase();
					tableNameListFile.write(tableName);
					tableNameListFile.newLine();
				}
			}
		}
	}
}
