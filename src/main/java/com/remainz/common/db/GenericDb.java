package com.remainz.common.db;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * 一般的なデータベースクラスです。
 */
public class GenericDb implements DbInterface {

	/** DB接続 */
	private Connection dbConnection = null;

	protected Connection getDbConnection() {
		return dbConnection;
	}

	@Override
	public void connect(String jdbcDriverName, String url, String dbUser, String dbPassword)
			throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

		// DBに接続する
		Class.forName(jdbcDriverName).getDeclaredConstructor().newInstance();
		dbConnection = DriverManager.getConnection(url, dbUser, dbPassword);

		// コミット及びロールバックを有効にする
		dbConnection.setAutoCommit(false);
	}

	@Override
	public ArrayList<LinkedHashMap<String, String>> select(String sql) throws SQLException {

		// SQL実行のためのPreparedStatementを生成する
		ArrayList<LinkedHashMap<String, String>> recordList = null;
		try (PreparedStatement preparedStatement = dbConnection.prepareStatement(sql);) {

			// SELECTを実行する
			recordList = selectByPreparedStatement(preparedStatement);
		}

		// SELECT結果を呼び出し側に戻す
		return recordList;
	}

	@Override
	public ArrayList<LinkedHashMap<String, String>> select(String sql, ArrayList<String> paramList)
			throws SQLException {

		ArrayList<LinkedHashMap<String, String>> recordList = null;
		try (PreparedStatement preparedStatement = dbConnection.prepareStatement(sql)) {

			// SQL実行のためのPreparedStatementを生成する
			for (int index = 0; index < paramList.size(); index++) {
				preparedStatement.setString((index + 1), paramList.get(index));
			}

			// SELECTを実行する
			recordList = selectByPreparedStatement(preparedStatement);

		}

		// SELECT結果を呼び出し側に戻す
		return recordList;
	}

	private ArrayList<LinkedHashMap<String, String>> selectByPreparedStatement(PreparedStatement preparedStatement)
			throws SQLException {

		// 戻り値変数を作成する
		ArrayList<LinkedHashMap<String, String>> recordList = new ArrayList<LinkedHashMap<String, String>>();

		// SQLを実行し、結果を戻り値変数に格納する
		try (ResultSet resultSet = preparedStatement.executeQuery()) {
			while (resultSet.next()) {
				LinkedHashMap<String, String> columnMap = new LinkedHashMap<String, String>();
				ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
				for (int index = 1; index <= resultSetMetaData.getColumnCount(); index++) {
					columnMap.put(resultSetMetaData.getColumnLabel(index),
							resultSet.getString(resultSetMetaData.getColumnLabel(index)));
				}
				recordList.add(columnMap);
			}
		}

		// SELECT結果を呼び出し側に戻す
		return recordList;
	}

	@Override
	public int update(String sql) throws SQLException {

		int ret = 0;
		try (PreparedStatement preparedStatement = dbConnection.prepareStatement(sql)) {
			ret = preparedStatement.executeUpdate();
		}
		return ret;
	}

	@Override
	public int update(String sql, ArrayList<String> paramList) throws SQLException {

		int ret = 0;
		try (PreparedStatement preparedStatement = dbConnection.prepareStatement(sql)) {
			for (int index = 0; index < paramList.size(); index++) {
				preparedStatement.setString((index + 1), paramList.get(index));
			}
			ret = preparedStatement.executeUpdate();
		}
		return ret;
	}

	@Override
	public void commit() throws SQLException {
		dbConnection.commit();
	}

	@Override
	public void rollback() throws SQLException {
		dbConnection.rollback();
	}

	@Override
	public void close() throws SQLException {
		dbConnection.close();
	}
}
