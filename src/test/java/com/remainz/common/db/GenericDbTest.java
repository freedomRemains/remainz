package com.remainz.common.db;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.junit.jupiter.api.Test;

import com.remainz.common.util.InnerClassPathProp;

//
//テストクラスはprivateを付けてはならず、publicも通常は付けない。
//
class GenericDbTest {

	//
	// 各テストメソッドはstatic、private禁止、戻り値も返却してはならない
	//

	private GenericDb getConnectedDb() throws Exception {

		// プロパティファイルから接続情報を読み込む
		InnerClassPathProp prop = new InnerClassPathProp("rc.properties");
		String jdbcDriverName = prop.get("db.jdbcDriverName");
		String dbUrl = prop.get("db.url");
		String dbUser = prop.get("db.dbUser");
		String dbPassword = prop.get("db.dbPassword");

		// DBに接続する
		GenericDb db = new GenericDb();
		db.connect(jdbcDriverName, dbUrl, dbUser, dbPassword);
		assertNotNull(db.getDbConnection());

		// GenericDbのインスタンスを呼び出し側に返却する
		return db;
	}

	private void closeDbIfNotNull(DbInterface db) {

		// 引数のDBインスタンスがnullでなければ、DBをクローズする
		if (db != null) {
			try {

				// テーブル作成はロールバックされないため、テーブルが存在している場合は削除する
				db.update("DROP TABLE IF EXISTS TEST;");

				// DBをクローズする
				db.close();

			} catch (Exception e) {
				fail();
			}
		}
	}

	@Test
	void test01() {

		// connectの検証
		GenericDb db = null;
		try {
			db = getConnectedDb();
			assertNotNull(db);
		} catch (Exception e) {
			fail();
		} finally {
			closeDbIfNotNull(db);
		}
	}

	@Test
	void test02() {

		// update、rollback、selectの検証
		GenericDb db = null;
		try {

			// テーブルを作成してレコードを投入する
			db = getConnectedDb();
			db.update("DROP TABLE IF EXISTS TEST;");
			db.update("CREATE TABLE TEST(ID INT AUTO_INCREMENT, VAL VARCHAR(256), PRIMARY KEY(ID));");
			db.update("INSERT INTO TEST(ID, VAL) VALUES(1, 'TEST');");

			// テーブルにレコードが投入されたことを確認する
			ArrayList<LinkedHashMap<String, String>> recordList = db.select("SELECT * FROM TEST;");
			assertEquals(1, recordList.size());
			assertEquals("1", recordList.get(0).get("ID"));
			assertEquals("TEST", recordList.get(0).get("VAL"));

			// ロールバックする
			db.rollback();

			// レコードがなくなっていることを確認する
			recordList = db.select("SELECT ID, VAL FROM TEST;");
			assertEquals(0, recordList.size());

			// テーブル作成はロールバックされないため、テーブルは自力で削除する
			db.update("DROP TABLE TEST;");

		} catch (Exception e) {
			fail();
		} finally {
			closeDbIfNotNull(db);
		}
	}

	@Test
	void test03() {

		// update、commit、selectの検証
		GenericDb db = null;
		try {

			// テーブルを作成してレコードを投入する
			db = getConnectedDb();
			db.update("DROP TABLE IF EXISTS TEST;");
			db.update("CREATE TABLE TEST(ID INT AUTO_INCREMENT, VAL VARCHAR(256), PRIMARY KEY(ID));");
			db.update("INSERT INTO TEST(ID, VAL) VALUES(1, 'TEST');");
			ArrayList<String> paramList = new ArrayList<String>();
			paramList.add("1");
			db.update("UPDATE TEST SET VAL = 'TEST2' WHERE ID = ?", paramList);

			// テーブルにレコードが投入されたことを確認する
			ArrayList<LinkedHashMap<String, String>> recordList = db.select("SELECT * FROM TEST;");
			assertEquals(1, recordList.size());
			assertEquals("1", recordList.get(0).get("ID"));
			assertEquals("TEST2", recordList.get(0).get("VAL"));

			// コミットする
			db.commit();

			// レコードが残っていることを確認する
			recordList = db.select("SELECT * FROM TEST;");
			assertEquals(1, recordList.size());
			assertEquals("1", recordList.get(0).get("ID"));
			assertEquals("TEST2", recordList.get(0).get("VAL"));

			// ID指定でクエリしても同一の結果になるか確認する
			recordList = db.select("SELECT ID, VAL FROM TEST WHERE ID = ?;", paramList);
			assertEquals(1, recordList.size());
			assertEquals("1", recordList.get(0).get("ID"));
			assertEquals("TEST2", recordList.get(0).get("VAL"));

			// テーブルを削除する
			db.update("DROP TABLE TEST;");

		} catch (Exception e) {
			fail();
		} finally {
			closeDbIfNotNull(db);
		}
	}

	@Test
	void test04() {

		// SQLエラーとなるパターンを検証
		GenericDb db = null;
		try {

			// 接続せずにselect
			db = new GenericDb();
			db.select("SELECT * FROM TEST;");
			fail();

		} catch (Exception e) {
			assertEquals("class java.lang.NullPointerException", e.getClass().toString());
		}
	}

	@Test
	void test05() {

		// SQLエラーとなるパターンを検証
		GenericDb db = null;
		try {

			// 接続せずにupdate(パラメータリスト付き)
			db = new GenericDb();
			ArrayList<String> paramList = new ArrayList<String>();
			paramList.add("1");
			db.update("UPDATE TEST SET VAL = 'TEST2' WHERE ID = ?", paramList);
			fail();

		} catch (Exception e) {
			assertEquals("class java.lang.NullPointerException", e.getClass().toString());
		}
	}

	@Test
	void test06() {

		// SQLエラーとなるパターンを検証
		GenericDb db = null;
		try {

			// 接続せずにinsert
			db = new GenericDb();
			db.update("INSERT INTO TEST(ID, VAL) VALUES(1, 'TEST');");
			fail();

		} catch (Exception e) {
			assertEquals("class java.lang.NullPointerException", e.getClass().toString());
		}
	}

	@Test
	void test07() {

		// SQLエラーとなるパターンを検証
		GenericDb db = null;
		try {

			// 接続せずにselect(パラメータリスト付き)
			db = new GenericDb();
			ArrayList<String> paramList = new ArrayList<String>();
			paramList.add("1");
			db.select("SELECT ID, VAL FROM TEST WHERE ID = ?;", paramList);
			fail();

		} catch (Exception e) {
			assertEquals("class java.lang.NullPointerException", e.getClass().toString());
		}
	}

	@Test
	void test08() {

		// selectでSQLエラー
		GenericDb db = null;
		try {

			// テーブルを作成してレコードを投入する
			db = getConnectedDb();
			db.update("DROP TABLE IF EXISTS TEST;");
			db.update("CREATE TABLE TEST(ID INT AUTO_INCREMENT, VAL VARCHAR(256), PRIMARY KEY(ID));");
			db.update("INSERT INTO TEST(ID, VAL) VALUES(1, 'TEST');");

			// テーブルにレコードが投入されたことを確認するSQLでテーブル名にミスがあり、エラーとなることを検証
			db.select("SELECT * FROM TEST2;");
			fail();

		} catch (Exception e) {
			assertTrue(e.getClass().toString().contains("SQLSyntaxErrorException"));
		} finally {
			closeDbIfNotNull(db);
		}
	}
}
