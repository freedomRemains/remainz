package com.remainz.common.service.dbmng.common;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.TestUtil;
import com.remainz.common.exception.BusinessRuleViolationException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.util.Mu;

/**
 * 各テーブルのSQLを取得するクラスです。
 */
public class GetTableCreateSqlServiceTest {

	private String dbName;

	private TestUtil testUtil;

	@BeforeEach
	void beforeEach() {

		// DB接続を取得し、トランザクションを開始する
		testUtil = new TestUtil();
		testUtil.getDb();

		// DB名を取得する
		dbName = testUtil.getDbName();
	}

	@AfterEach
	void afterEach() throws Exception {

		// 必ず最後にロールバックし、DBをクローズする
		testUtil.getDb().rollback();
		testUtil.closeDb();

		// テストフォルダを削除する
		testUtil.clearOutputDir();
	}

	//
	// 各テストメソッドはstatic、private禁止、戻り値も返却してはならない
	//

	@Test
	void test01() throws Exception {

		// 必須パラメータがないパターン
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		GetTableCreateSqlService service = new GetTableCreateSqlService();
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
		String tableName = "SCR";

		// (カバレッジ)存在しない出力先を指定するパターン
		GenericParam input = new GenericParam();
		input.setDb(testUtil.getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("sqlPath", "noexistpath"); // 存在しない出力先
		input.putString("tableName", tableName);
		GenericParam output = new GenericParam();
		GetTableCreateSqlService service = new GetTableCreateSqlService();
		try {
			service.doService(input, output);
			fail();
		} catch (FileNotFoundException e) {
			assertTrue(e.getLocalizedMessage().contains("SCR"));
		}
	}

	@Test
	void test03() throws Exception {

		// どんな場合でも必ず同じテスト結果となるよう、共通の固定ダミーテーブル定義を適用する
		testUtil.prepare();

		// 必要なパラメータを準備する
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";
		String tableName = "SCR";

		// 正常系パターン
		GenericParam input = new GenericParam();
		input.setDb(testUtil.getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("sqlPath", sqlPath);
		input.putString("tableName", tableName);
		GenericParam output = new GenericParam();
		GetTableCreateSqlService service = new GetTableCreateSqlService();
		service.doService(input, output);

		// ファイルが生成されていることを確認する
		String outputPath = dirPath + "/" + sqlPath + "/";
		assertTrue(new File(outputPath + "CREATE_SCR.txt").exists());
		assertTrue(new File(outputPath + "CREATE_SCR.txt").length() > 0);
	}

	@Test
	void test04() throws Exception {

		// どんな場合でも必ず同じテスト結果となるよう、共通の固定ダミーテーブル定義を適用する
		testUtil.prepare();

		// 必要なパラメータを準備する
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";
		String tableName = "SCR";

		// カバレッジ(オンメモリにテーブルリストがある状態で実行)
		GenericParam input = new GenericParam();
		input.setDb(testUtil.getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("sqlPath", sqlPath);
		input.putString("tableName", tableName);
		GenericParam output = new GenericParam();
		GetTableDefByFileService preExecService = new GetTableDefByFileService();
		preExecService.doService(input, output);
		var tableDef = output.getRecordList("tableDef" + tableName);
		input.putRecordList("tableDef" + tableName, tableDef);
		GetTableCreateSqlService service = new GetTableCreateSqlService();
		service.doService(input, output);

		// ファイルが生成されていることを確認する
		String outputPath = dirPath + "/" + sqlPath + "/";
		assertTrue(new File(outputPath + "CREATE_SCR.txt").exists());
		assertTrue(new File(outputPath + "CREATE_SCR.txt").length() > 0);

		// カバレッジ(サイズが0のtableDef)
		tableDef.clear();
		input.putRecordList("tableDef" + tableName, tableDef);
		service.doService(input, output);

		// オンメモリの空情報は使わず、ファイルが生成されていることを確認する
		assertTrue(new File(outputPath + "CREATE_SCR.txt").exists());
		assertTrue(new File(outputPath + "CREATE_SCR.txt").length() > 0);
	}

	@Test
	void test05() throws Exception {

		// どんな場合でも必ず同じテスト結果となるよう、共通の固定ダミーテーブル定義を適用する
		testUtil.prepare();

		// 必要なパラメータを準備する
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";
		String tableName = "SCR";

		// カバレッジ(プライマリキーが2つ以上)
		GenericParam input = new GenericParam();
		input.setDb(testUtil.getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("sqlPath", sqlPath);
		input.putString("tableName", tableName);
		GenericParam output = new GenericParam();
		GetTableDefByFileService preExecService = new GetTableDefByFileService();
		preExecService.doService(input, output);
		var tableDef = output.getRecordList("tableDef" + tableName);

		// プライマリキーを2つにする
		tableDef.get(1).put("Key", "PRI");

		input.putRecordList("tableDef" + tableName, tableDef);
		GetTableCreateSqlService service = new GetTableCreateSqlService();
		service.doService(input, output);

		// ファイルが生成されていることを確認する
		String outputPath = dirPath + "/" + sqlPath + "/";
		assertTrue(new File(outputPath + "CREATE_SCR.txt").exists());
		assertTrue(new File(outputPath + "CREATE_SCR.txt").length() > 0);
		testUtil.assertFileContains(outputPath + "CREATE_SCR.txt", "PRIMARY KEY(SCR_ID, SCR_NAME)");

		// カバレッジ(プライマリキーなし)
		tableDef.get(0).put("Key", "");
		tableDef.get(1).put("Key", "");
		input.putRecordList("tableDef" + tableName, tableDef);
		service.doService(input, output);

		// オンメモリの空情報は使わず、ファイルが生成されていることを確認する
		assertTrue(new File(outputPath + "CREATE_SCR.txt").exists());
		assertTrue(new File(outputPath + "CREATE_SCR.txt").length() > 0);
		testUtil.assertFileNotContains(outputPath + "CREATE_SCR.txt", "PRIMARY KEY(");

		// カバレッジ(デフォルト値が空文字列)
		tableDef.get(0).put("Key", "PRI");
		tableDef.get(0).put("Default", "");
		input.putRecordList("tableDef" + tableName, tableDef);
		service.doService(input, output);

		// オンメモリの空情報は使わず、ファイルが生成されていることを確認する
		assertTrue(new File(outputPath + "CREATE_SCR.txt").exists());
		assertTrue(new File(outputPath + "CREATE_SCR.txt").length() > 0);

		// カバレッジ(デフォルト値指定あり)
		tableDef.get(0).put("Key", "PRI");
		tableDef.get(1).put("Default", "test");
		input.putRecordList("tableDef" + tableName, tableDef);
		service.doService(input, output);

		// オンメモリの空情報は使わず、ファイルが生成されていることを確認する
		assertTrue(new File(outputPath + "CREATE_SCR.txt").exists());
		assertTrue(new File(outputPath + "CREATE_SCR.txt").length() > 0);
		testUtil.assertFileContains(outputPath + "CREATE_SCR.txt", "DEFAULT 'test'");

		// カバレッジ(デフォルト値指定あり、CURRENT_DATE)
		tableDef.get(0).put("Key", "PRI");
		tableDef.get(5).put("Default", "CURRENT_DATE");
		input.putRecordList("tableDef" + tableName, tableDef);
		service.doService(input, output);

		// オンメモリの空情報は使わず、ファイルが生成されていることを確認する
		assertTrue(new File(outputPath + "CREATE_SCR.txt").exists());
		assertTrue(new File(outputPath + "CREATE_SCR.txt").length() > 0);
		testUtil.assertFileContains(outputPath + "CREATE_SCR.txt", "DEFAULT CURRENT_DATE");

		// カバレッジ(デフォルト値指定あり、CURRENT_TIME)
		tableDef.get(0).put("Key", "PRI");
		tableDef.get(5).put("Default", "CURRENT_TIME");
		input.putRecordList("tableDef" + tableName, tableDef);
		service.doService(input, output);

		// オンメモリの空情報は使わず、ファイルが生成されていることを確認する
		assertTrue(new File(outputPath + "CREATE_SCR.txt").exists());
		assertTrue(new File(outputPath + "CREATE_SCR.txt").length() > 0);
		testUtil.assertFileContains(outputPath + "CREATE_SCR.txt", "DEFAULT CURRENT_TIME");

		// カバレッジ(デフォルト値指定あり、CURRENT_TIMESTAMP)
		tableDef.get(0).put("Key", "PRI");
		tableDef.get(5).put("Default", "CURRENT_TIMESTAMP");
		input.putRecordList("tableDef" + tableName, tableDef);
		service.doService(input, output);

		// オンメモリの空情報は使わず、ファイルが生成されていることを確認する
		assertTrue(new File(outputPath + "CREATE_SCR.txt").exists());
		assertTrue(new File(outputPath + "CREATE_SCR.txt").length() > 0);
		testUtil.assertFileContains(outputPath + "CREATE_SCR.txt", "DEFAULT CURRENT_TIMESTAMP");
	}
}
