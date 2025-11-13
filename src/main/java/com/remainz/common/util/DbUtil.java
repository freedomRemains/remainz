package com.remainz.common.util;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.remainz.common.db.DbInterface;
import com.remainz.common.exception.ApplicationInternalException;

/**
 * DBユーティリティクラスです。
 */
public class DbUtil {

	/** ロガー */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * DBに接続できるかテストします
	 */
	public DbInterface getDb(InnerClassPathProp prop) {

		DbInterface db = null;
		try {
			// プロパティファイルから、DB接続設定を読み込む
			String dbType = prop.get("db.type");
			String jdbcDriverName = prop.get("db.jdbcDriverName");
			String dbUrl = prop.get("db.url");
			String dbUser = prop.get("db.dbUser");
			String dbPassword = prop.get("db.dbPassword");

			// プロパティファイルから読み込んだ設定値をログに記録する
			StringBuffer msg = new StringBuffer(prop.getPropFileName());
			msg.append("  db.type=" + dbType);
			msg.append("  db.jdbcDriverName=" + jdbcDriverName);
			msg.append("  db.url=" + dbUrl);
			msg.append("  db.dbUser=" + dbUser);
			msg.append("  db.dbPassword=****");
			logger.info(msg);

			// DBに接続できるかテストする
			db = (DbInterface) Class.forName(dbType).getConstructor().newInstance();
			db.connect(jdbcDriverName, dbUrl, dbUser, dbPassword);

		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | ClassNotFoundException | SQLException e) {
			throw new ApplicationInternalException(new LogUtil().handleException(e));
		}

		// 接続したDBを戻り値とする
		return db;
	}
}
