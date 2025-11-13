package com.remainz.common.param;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.remainz.common.db.DbInterface;
import com.remainz.common.util.Cu;

/**
 * 汎用的なパラメータクラスです。
 */
public class GenericParam {

	/** DB */
	private DbInterface db = null;

	public DbInterface getDb() {
		return db;
	}

	public void setDb(DbInterface db) {
		this.db = db;
	}

	/** パラメータ名(キー)と文字列(値)のマップ */
	private LinkedHashMap<String, String> stringMap = null;

	/** パラメータ名(キー)と文字列配列(値)のマップ */
	private LinkedHashMap<String, String[]> stringArrayMap = null;

	/** パラメータ名(キー)とDB検索結果(値)のマップ */
	private LinkedHashMap<String, ArrayList<LinkedHashMap<String, String>>> recordListMap = null;

	/** 改行文字 */
	private String lineSepr;

	/**
	 * コンストラクタ
	 */
	public GenericParam() {

		// メンバを初期化する
		stringMap = new LinkedHashMap<String, String>();
		stringArrayMap = new LinkedHashMap<String, String[]>();
		recordListMap = new LinkedHashMap<String, ArrayList<LinkedHashMap<String, String>>>();
		lineSepr = System.lineSeparator();
	}

	/**
	 * 改行文字を呼び出し側に返却します。
	 * 
	 * @return 改行文字
	 */
	public String getLineSepr() {
		return lineSepr;
	}

	/**
	 * キーに対応する値を呼び出し側に返却します。
	 *
	 * @param key キー
	 * @return キーに対応する値(キーに対応する値が存在しない場合はnullが戻る)
	 */
	public String getString(String key) {
		return stringMap.get(key);
	}

	/**
	 * キーと値をメンバのマップに設定します。
	 *
	 * @param key キー
	 * @param value キーに対応する値
	 */
	public void putString(String key, String value) {
		stringMap.put(key, value);
	}

	/**
	 * キーが存在しない場合に限り、キーと値をメンバのマップに設定します。
	 *
	 * @param key キー
	 * @param value キーに対応する値
	 */
	public void putStringIfNotExists(String key, String value) {
		if (Cu.isNotEmpty(stringMap.get(key))) {
			return;
		}
		stringMap.put(key, value);
	}

	/**
	 * キーに対応する値をマップに設定します。
	 * 既にキーに対応する値がマップにある場合は、追記します。
	 *
	 * @param key キー
	 * @param value キーに対応する値
	 */
	public void addString(String key, String value) {
		String currentValue = stringMap.get(key);
		if (currentValue == null || currentValue.length() == 0) {
			stringMap.put(key, value);
		} else {
			stringMap.put(key, currentValue + lineSepr + value);
		}
	}

	/**
	 * パラメータ名(キー)と文字列(値)のマップのキーセットを取得します。
	 *
	 * @return パラメータ名(キー)と文字列(値)のマップのキーセット
	 */
	public Set<String> getStringMapKeySet() {
		return stringMap.keySet();
	}

	/**
	 * キーに対応する文字列配列を取得します。
	 *
	 * @param key 文字列配列を取得するためのキー文字列
	 * @return 文字列配列
	 */
	public String[] getStringArray(String key) {
		return stringArrayMap.get(key);
	}

	/**
	 * 文字列配列をマップに追加します。
	 *
	 * @param key キー
	 * @param recordList 文字列配列
	 */
	public void putStringArray(String key, String[] stringArray) {
		stringArrayMap.put(key, stringArray);
	}

	/**
	 * 文字列配列のマップのキーセットを取得します。
	 *
	 * @return 文字列配列のマップのキーセット
	 */
	public Set<String> getStringArrayMapKeySet() {
		return stringArrayMap.keySet();
	}

	/**
	 * キーに対応するDB検索結果を取得します。
	 *
	 * @param key DB検索結果を取得するためのキー文字列
	 * @return DB検索結果
	 */
	public ArrayList<LinkedHashMap<String, String>> getRecordList(String key) {
		return recordListMap.get(key);
	}

	/**
	 * DB検索結果をマップに追加します。
	 *
	 * @param key キー
	 * @param recordList DB検索結果
	 */
	public void putRecordList(String key, ArrayList<LinkedHashMap<String, String>> recordList) {
		recordListMap.put(key, recordList);
	}

	/**
	 * DB検索結果のマップのキーセットを取得します。
	 *
	 * @return DB検索結果のマップのキーセット
	 */
	public Set<String> getRecordListMapKeySet() {
		return recordListMap.keySet();
	}

	public void recordLog(Logger logger, String title) {

		// タイトルをメッセージに追加
		String lineSepr = getLineSepr();
		StringBuilder msg = new StringBuilder(title + lineSepr);

		// 文字列マップの内容をメッセージに追加
		msg.append("[StringMap]" + lineSepr);
		StringBuilder part = new StringBuilder();
		for (String key : getStringMapKeySet()) {
			if (part.length() > 0) {
				part.append(", ");
			}
			appendPart(part, key, getString(key));
		}
		if (part.length() > 0) {
			msg.append("\t" + part + lineSepr);
		}

		// 文字列配列マップの内容をメッセージに追加
		msg.append("[StringArrayMap]" + lineSepr);
		part = new StringBuilder();
		for (String key : getStringArrayMapKeySet()) {
			if (part.length() > 0) {
				part.append(", ");
			}
			part.append(key + ": [");
			StringBuilder temp = new StringBuilder();
			for (String value : getStringArray(key)) {
				if (temp.length() > 0) {
					temp.append(", ");
				}
				temp.append(getFilterdValue(key, value));
			}
			part.append(temp);
			part.append("]");
		}
		if (part.length() > 0) {
			msg.append("\t" + part + lineSepr);
		}

		// レコードリストマップの内容をメッセージに追加
		msg.append("[RecordListMap]" + lineSepr);
		part = new StringBuilder();
		for (String key : getRecordListMapKeySet()) {
			if (part.length() > 0) {
				part.append("\t");
			}
			part.append(key + ": " + toString(getRecordList(key)));
		}
		if (part.length() > 0) {
			msg.append("\t" + part + lineSepr);
		}

		// ログを記録する
		logger.info(msg);
	}

	private String toString(ArrayList<LinkedHashMap<String, String>> recordList) {
		StringBuilder msg = new StringBuilder();
		msg.append("[" + lineSepr);
		for (LinkedHashMap<String, String> columnMap : recordList) {
			msg.append("\t\t{");
			StringBuilder part = new StringBuilder();
			for (Map.Entry<String, String> entry : columnMap.entrySet()) {
				if (part.length() > 0) {
					part.append(", ");
				}
				appendPart(part, entry.getKey(), entry.getValue());
			}
			part.append("}," + lineSepr);
			msg.append(part);
		}
		msg.append("\t]," + lineSepr);
		return msg.toString();
	}

	private void appendPart(StringBuilder part, String key, String value) {
		part.append(key + ": " + getFilterdValue(key, value));
	}

	private String getFilterdValue(String key, String value) {
		if (key.equalsIgnoreCase("PASSWORD")) {
			return "*****";
		}
		return value;
	}
}
