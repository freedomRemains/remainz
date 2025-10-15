package com.remainz.common.db;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * DBインターフェースです。
 *
 * DBとの接続はドライバの挙動に依存している部分が多いため、例外は変換せずにそのまま上位にスロー
 * する方式としています。
 */
public interface DbInterface {

	/**
	 * DBに接続します。
	 *
	 * @param jdbcDriverName JDBCドライバ名
	 * @param url URL
	 * @param userName ユーザ名
	 * @param dbPassword パスワード
	 * @throws ClassNotFoundException クラスが見つからない例外
	 * @throws SQLException SQL例外
	 * @throws SecurityException セキュリティ例外
	 * @throws NoSuchMethodException メソッドがない例外
	 * @throws InvocationTargetException 呼び出しターゲットがない例外
	 * @throws IllegalArgumentException 引数不正例外
	 * @throws IllegalAccessException 不正アクセス例外
	 * @throws InstantiationException インスタンス化例外
	 */
	void connect(String jdbcDriverName, String url, String userName, String dbPassword)
			throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException;

	/**
	 * SELECTを実行します。
	 *
	 * @param sql SQL
	 * @return SELECT結果
	 * @throws SQLException SQL例外
	 */
	ArrayList<LinkedHashMap<String, String>> select(String sql) throws SQLException;

	/**
	 * SELECTを実行します。
	 *
	 * @param sql SQL
	 * @param paramList パラメータリスト
	 * @return SELECT結果
	 * @throws SQLException SQL例外
	 */
	ArrayList<LinkedHashMap<String, String>> select(String sql, ArrayList<String> paramList) throws SQLException;

	/**
	 * UPDATEを実行します。
	 *
	 * @param sql SQL
	 * @return UPDATE結果
	 * @throws SQLException SQL例外
	 */
	int update(String sql) throws SQLException;

	/**
	 * UPDATEを実行します。
	 *
	 * @param sql SQL
	 * @param paramList パラメータリスト
	 * @return UPDATE結果
	 * @throws SQLException SQL例外
	 */
	int update(String sql, ArrayList<String> paramList) throws SQLException;

	/**
	 * コミットします。
	 *
	 * @throws SQLException SQL例外
	 */
	void commit() throws SQLException;

	/**
	 * ロールバックします。
	 *
	 * @throws SQLException SQL例外
	 */
	void rollback() throws SQLException;

	/**
	 * DB接続をクローズします。
	 *
	 * @throws SQLException SQL例外
	 */
	void close() throws SQLException;
}
