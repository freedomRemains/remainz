package com.remainz.common.service.dbmng.common;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.remainz.common.exception.BusinessRuleViolationException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.service.ServiceInterface;
import com.remainz.common.util.Cu;
import com.remainz.common.util.FileUtil;
import com.remainz.common.util.InputCheckUtil;
import com.remainz.common.util.Mu;

/**
 * テーブルの定義情報を取得するクラスです。<br>
 * <br>
 * [input][必須][db]接続済みDBのインスタンス<br>
 * [input][必須][tableName]テーブル名<br>
 * [input][必須][tableDefFilePath]テーブル定義ファイルのパス<br>
 * [input][必須][getTableDefSql]テーブル定義取得用SQL<br>
 * [output][tableDef]テーブル定義取得用SQLで取得したテーブル定義の内容<br>
 * <br>
 * 取得したテーブル定義をoutputのtableDefにオンメモリで持つと同時に、<br>
 * inputのtableDefFilePathで指定されたテーブル定義ファイルに書き込む。<br>
 * <br>
 * 本クラスは2024/03/24時点で、MySQLにしか対応していない。<br>
 * SQLでテーブル定義を取得できないDBは、そもそも本クラスは使用できない。<br>
 * その場合、テーブル定義ファイルは自前で用意すること。<br>
 */
public class GetTableDefService implements ServiceInterface {

	/** ロガー */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	@Override
	public void doService(GenericParam input, GenericParam output) throws Exception {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkDb(input);
		inputCheckUtil.checkParam(input, "tableName");
		inputCheckUtil.checkParam(input, "tableDefFilePath");
		inputCheckUtil.checkParam(input, "getTableDefSql");

		// テーブル定義を取得する
		getTableDef(input, output);
	}

	private void getTableDef(GenericParam input, GenericParam output) throws Exception {

		// 入力パラメータからテーブル名を取得する
		String tableName = input.getString("tableName");

		// テーブル定義ディレクトリ配下にテーブル名のテキストファイルを生成する
		String tableDefFilePath = input.getString("tableDefFilePath");
		try (BufferedWriter tableDefFile = new FileUtil().getBufferedWriter(tableDefFilePath)) {

			// DB定義を取得してファイルに書き込む
			writeTableDef(input, output, tableName, tableDefFile);
		}
	}

	private void writeTableDef(GenericParam input, GenericParam output, String tableName, BufferedWriter tableDefFile)
			throws SQLException, IOException {

		// テーブル定義を取得するSQLを生成し、実行する
		String sql = input.getString("getTableDefSql").replace("#TABLE_NAME#", tableName);
		logger.info(new Mu().msg("msg.common.sql", sql));
		ArrayList<LinkedHashMap<String, String>> recordList = input.getDb().select(sql);

		// レコードが取得できなかった場合はエラーとする
		if (recordList.size() == 0) {
			throw new BusinessRuleViolationException(
					new Mu().msg("msg.err.common.invalidTableDef", tableName));
		}

		// 変換を行う
		var modifiedRecordList = modifyRecordList(recordList);

		// テーブル定義は出力パラメータにも書き込む
		output.putRecordList("tableDef" + tableName, modifiedRecordList);

		// テーブル定義のヘッダを書き込む
		StringBuffer headerLine = new StringBuffer();
		for (String key : modifiedRecordList.get(0).keySet()) {
			if (headerLine.length() > 0) {
				headerLine.append("\t");
			}
			headerLine.append(key);
		}
		tableDefFile.write(headerLine.toString());
		tableDefFile.newLine();

		// 取得したテーブル定義をファイルに書き込む(大文字変換した文字列で書き込みを行う)
		for (LinkedHashMap<String, String> columnMap : modifiedRecordList) {
			StringBuffer tableDefLine = new StringBuffer();
			for (Map.Entry<String, String> entry : columnMap.entrySet()) {
				if (tableDefLine.length() > 0) {
					tableDefLine.append("\t");
				}
				if (Cu.isEmpty(columnMap.get(entry.getKey()))) {
					tableDefLine.append(columnMap.get(entry.getKey()));
				} else {
					tableDefLine.append(columnMap.get(entry.getKey()).toUpperCase());
				}
			}
			tableDefFile.write(tableDefLine.toString());
			tableDefFile.newLine();
		}
	}

	//---------------------------------------------------------------------//
	// 以降のコードは、本来なら派生クラスに持っていかなければならない。
	//---------------------------------------------------------------------//

	/** テーブル定義ファイルのヘッダ行に記述するキーを変換するためのマップ */
	private Map<String, String> convertMap;

	/**
	 * コンストラクタ
	 */
	public GetTableDefService() {

		// テーブル定義ファイルのヘッダ行に記述するキーを、所定の規則で変換するためのマップを生成する
		convertMap = new HashMap<String, String>();
		convertMap.put("FIELD_NAME", "Field");
		convertMap.put("TYPE_NAME", "Type");
		convertMap.put("ALLOW_NULL", "Null");
		convertMap.put("KEY_DIV", "Key");
		convertMap.put("DEFAULT_VALUE", "Default");
		convertMap.put("EXTRA", "Extra");
	}

	/**
	 * テーブル定義のカラム名の変換規則を記述したマップ。
	 *
	 * @param convertMap 変換マップ
	 */
	public void setConvertMap(Map<String, String> convertMap) {
		this.convertMap.clear();
		this.convertMap.putAll(convertMap);
	}

	private ArrayList<LinkedHashMap<String, String>> modifyRecordList(
			ArrayList<LinkedHashMap<String, String>> recordList) {

		// 変更後のレコードリストを作成する
		var modifiedRecordList = new ArrayList<LinkedHashMap<String, String>>();

		// 変更前のレコードリストを全て処理するまでループ
		for (LinkedHashMap<String, String> columnMap : recordList) {

			// カラムマップを変換し、変更後のレコードリストに追加する
			modifiedRecordList.add(modifyColumnMap(columnMap));
		}

		// 変更後のレコードリストを呼び出し側に返却する
		return modifiedRecordList;
	}

	private LinkedHashMap<String, String> modifyColumnMap(LinkedHashMap<String, String> columnMap) {

		// 変更後のマップを作成する
		var modifiedMap = new LinkedHashMap<String, String>();

		// 変更前のカラムマップに存在するデータを全て処理するまでループ
		for (Map.Entry<String, String> entry : columnMap.entrySet()) {

			// テーブル定義ファイルのヘッダ行に書き込むキーを既に持っている場合
			if (convertMap.containsValue(entry.getKey())) {

				// 変換後のマップにエントリを追加する
				modifiedMap.put(entry.getKey(), entry.getValue());

				// ループの先頭に戻る
				continue;
			}

			// テーブル定義ファイルのヘッダ行に書き込むキーを変換できた(変換結果が存在する)場合
			String newKey = convertMap.get(entry.getKey());
			if (Cu.isNotEmpty(newKey)) {

				// 変更後のマップに変換後のキーでエントリを追加する
				modifiedMap.put(newKey, entry.getValue());
			}
		}

		// 変換後のマップを呼び出し側に返却する
		return modifiedMap;
	}
}
