package com.remainz.common.service.dbmng.common;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.TestUtil;
import com.remainz.common.exception.ApplicationInternalException;
import com.remainz.common.exception.BusinessRuleViolationException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.util.FileUtil;
import com.remainz.common.util.Mu;

/**
 * 各テーブルのSQLを取得するクラスです。
 */
public class GetTableInsertSqlServiceTest {

	private static final String TEST_PATH = TestUtil.RESOURCE_PATH + "service/dbmng/common";

	private static String dbName;

	private static TestUtil testUtil;

	@BeforeAll
	static void beforeAll() throws Exception {

		// 必ず最初に一度、DB復元を実施する
		testUtil = new TestUtil();
		testUtil.restoreDb();
		testUtil.getDb().commit();
		testUtil.closeDb();
		testUtil = null;
	}

	@BeforeEach
	void beforeEach() throws Exception {

		// DB接続を取得し、トランザクションを開始する
		testUtil = new TestUtil();
		testUtil.getDb();

		// DB名を取得する
		dbName = testUtil.getDbName();

		// どんな場合でも必ず同じテスト結果となるよう、共通の固定ダミーテーブル定義を適用する
		testUtil.prepare();

		// テーブルデータを取得するために必要なSELECTのSQLを生成する
		createTableSelectSql("SCR");
		createTableDataFile("SCR");
	}

	@AfterEach
	void afterEach() throws Exception {

		// 必ず最後にロールバックし、DBをクローズする
		testUtil.getDb().rollback();
		testUtil.closeDb();

		// テストフォルダを削除する
		testUtil.clearOutputDir();
	}

	private static void createTableSelectSql(String tableName) throws Exception {

		// 必要なパラメータを準備する
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";
		String tableDefFilePath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName + "/10_dbdef/20_auto_created/SCR.txt";

		// 正常系パターン
		GenericParam input = new GenericParam();
		input.setDb(testUtil.getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("sqlPath", sqlPath);
		input.putString("tableName", tableName);
		input.putString("tableDefFilePath", tableDefFilePath);
		GenericParam output = new GenericParam();
		GetTableSelectSqlService service = new GetTableSelectSqlService();
		service.doService(input, output);
	}

	private static void createTableDataFile(String tableName) throws Exception {

		// 必要なパラメータを準備する
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String dataPath = "20_dbdata/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";

		// 正常系パターン
		GenericParam input = new GenericParam();
		input.setDb(testUtil.getDb());
		input.putString("dirPath", dirPath);
		input.putString("dataPath", dataPath);
		input.putString("sqlPath", sqlPath);
		input.putString("tableName", tableName);
		GenericParam output = new GenericParam();
		GetTableDataService service = new GetTableDataService();
		service.doService(input, output);
	}

	//
	// 各テストメソッドはstatic、private禁止、戻り値も返却してはならない
	//

	@Test
	void test01() throws Exception {

		// 必須パラメータがないパターン
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		GetTableInsertSqlService service = new GetTableInsertSqlService();
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "db"), e.getLocalizedMessage());
		}

		input.setDb(testUtil.getDb());
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "dirPath"), e.getLocalizedMessage());
		}

		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		input.putString("dirPath", dirPath);
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "defPath"), e.getLocalizedMessage());
		}

		String defPath = "10_dbdef/20_auto_created";
		input.putString("defPath", defPath);
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "dataPath"), e.getLocalizedMessage());
		}

		String dataPath = "20_dbdata/20_auto_created";
		input.putString("dataPath", dataPath);
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "sqlPath"), e.getLocalizedMessage());
		}

		String sqlPath = "30_sql/20_auto_created";
		input.putString("sqlPath", sqlPath);
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "tableName"), e.getLocalizedMessage());
		}
	}

	@Test
	void test02() throws Exception {

		// 必要なパラメータを準備する
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String dataPath = "20_dbdata/20_auto_created";
		String tableName = "SCR";

		// (カバレッジ)存在しない出力先を指定するパターン
		GenericParam input = new GenericParam();
		input.setDb(testUtil.getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("dataPath", dataPath);
		input.putString("sqlPath", "noexistpath"); // 存在しない出力先
		input.putString("tableName", tableName);
		GenericParam output = new GenericParam();
		GetTableInsertSqlService service = new GetTableInsertSqlService();
		try {
			service.doService(input, output);
			fail();
		} catch (FileNotFoundException e) {
			assertTrue(e.getLocalizedMessage().contains("noexistpath"));
		}
	}

	@Test
	void test03() throws Exception {

		// 必要なパラメータを準備する
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String dataPath = "20_dbdata/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";
		String tableName = "SCR";

		// 正常系パターン
		GenericParam input = new GenericParam();
		input.setDb(testUtil.getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("dataPath", dataPath);
		input.putString("sqlPath", sqlPath);
		input.putString("tableName", tableName);
		GenericParam output = new GenericParam();
		GetTableInsertSqlService service = new GetTableInsertSqlService();
		service.doService(input, output);

		// ファイルが生成されていることを確認する
		String outputPath = dirPath + "/" + sqlPath + "/";
		File insertSqlFile = new File(outputPath + "INSERT_" + tableName + ".txt");
		assertTrue(insertSqlFile.exists());
		assertTrue(insertSqlFile.length() > 0);
	}

	@Test
	void test04() throws Exception {

		// 必要なパラメータを準備する
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";
		String tableName = "SCR";

		// (カバレッジ)テーブルデータファイルが存在しないパターン
		GenericParam input = new GenericParam();
		input.setDb(testUtil.getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("dataPath", "notExist"); // 存在しないフォルダを指定
		input.putString("sqlPath", sqlPath);
		input.putString("tableName", tableName);
		GenericParam output = new GenericParam();
		GetTableInsertSqlService service = new GetTableInsertSqlService();
		try {
			service.doService(input, output);
			fail();
		} catch (FileNotFoundException e) {
			assertTrue(e.getLocalizedMessage().contains("notExist"));
		}
	}

	@Test
	void test05() throws Exception {

		// 必要なパラメータを準備する
		String dirPath = TEST_PATH;
		String defPath = "10_dbdef/20_auto_created";
		String dataPath = "GetTableDefByFileServiceTest";
		String sqlPath = "GetTableDefByFileServiceTest";
		String tableName = "NO_HEADER";
		var tableDef = new ArrayList<LinkedHashMap<String, String>>();
		tableDef.add(new LinkedHashMap<String, String>());

		// (カバレッジ)ヘッダなしの処理ルートを通るパターン
		GenericParam input = new GenericParam();
		input.setDb(testUtil.getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("dataPath", dataPath);
		input.putString("sqlPath", sqlPath);
		input.putString("tableName", tableName);
		input.putRecordList("tableDef" + tableName, tableDef);
		GenericParam output = new GenericParam();
		GetTableInsertSqlService service = new GetTableInsertSqlService();
		service.doService(input, output);

		// 例外が起きず、空のファイルが生成されていることを確認する
		String outputPath = dirPath + "/" + sqlPath + "/";
		File insertSqlFile = new File(outputPath + "INSERT_" + tableName + ".txt");
		assertTrue(insertSqlFile.exists());
		assertTrue(insertSqlFile.length() == 0);

		// 生成したINSERTのSQLファイルを削除する(不要ファイルが残ってしまうため)
		new FileUtil().deleteFileOrDir(insertSqlFile);
	}

	@Test
	void test06() throws Exception {

		// 必要なパラメータを準備する
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String dataPath = "20_dbdata/20_auto_created/notExistPath";
		String sqlPath = "30_sql/20_auto_created";
		String tableName = "SCR";

		// (カバレッジ)存在しないデータファイル
		GenericParam input = new GenericParam();
		input.setDb(testUtil.getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("dataPath", dataPath);
		input.putString("sqlPath", sqlPath);
		input.putString("tableName", tableName);
		GenericParam output = new GenericParam();
		GetTableInsertSqlService service = new GetTableInsertSqlService();
		try {
			service.doService(input, output);
			fail();
		} catch (FileNotFoundException e) {
			assertTrue(e.getLocalizedMessage().contains("notExistPath"));
		}
	}

	@Test
	void test07() throws Exception {

		// 必要なパラメータを準備する
		String dirPath = TEST_PATH;
		String defPath = "10_dbdef/20_auto_created";
		String dataPath = "GetTableDefByFileServiceTest";
		String sqlPath = "GetTableDefByFileServiceTest";
		String tableName = "NO_COLUMN";
		var tableDef = new ArrayList<LinkedHashMap<String, String>>();
		var columnMap = new LinkedHashMap<String, String>();
		columnMap.put("Field", "notExistField");
		columnMap.put("Type", "INT");
		tableDef.add(columnMap);

		// (カバレッジ)存在しないカラム
		GenericParam input = new GenericParam();
		input.setDb(testUtil.getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("dataPath", dataPath);
		input.putString("sqlPath", sqlPath);
		input.putString("tableName", tableName);
		input.putRecordList("tableDef" + tableName, tableDef);
		GenericParam output = new GenericParam();
		GetTableInsertSqlService service = new GetTableInsertSqlService();
		try {
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains(new Mu().msg("msg.err.common.noColumnInfo",
					"NOT_EXIST_COLUMN")));
		}

		// 生成したINSERTのSQLファイルを削除する(不要ファイルが残ってしまうため)
		String outputPath = dirPath + "/" + sqlPath + "/";
		File insertSqlFile = new File(outputPath + "INSERT_" + tableName + ".txt");
		new FileUtil().deleteFileOrDir(insertSqlFile);
	}

	@Test
	void test08() throws Exception {

		// 必要なパラメータを準備する
		String dirPath = TEST_PATH;
		String defPath = "GetTableInsertSqlServiceTest";
		String dataPath = "GetTableDefByFileServiceTest";
		String sqlPath = "GetTableDefByFileServiceTest";
		String tableName = "GNR_KEY_VAL";

		// (カバレッジ)存在しないカラム
		GenericParam input = new GenericParam();
		input.setDb(testUtil.getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("dataPath", dataPath);
		input.putString("sqlPath", sqlPath);
		input.putString("tableName", tableName);
		GenericParam output = new GenericParam();
		GetTableInsertSqlService service = new GetTableInsertSqlService();
		service.doService(input, output);

		// 生成したINSERTのSQLファイルを削除する(不要ファイルが残ってしまうため)
		String outputPath = dirPath + "/" + sqlPath + "/";
		File insertSqlFile = new File(outputPath + "INSERT_" + tableName + ".txt");
		new FileUtil().deleteFileOrDir(insertSqlFile);
	}
}
