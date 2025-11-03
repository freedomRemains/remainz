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
public class GetTableSelectSqlServiceTest {

	private static String dbName;

	@BeforeEach
	void beforeEach() throws Exception {

		// テストに必要な準備処理を実行する
		dbName = TestUtil.getDbName();

		// どんな場合でも必ず同じテスト結果となるよう、共通の固定ダミーテーブル定義を適用する
		TestUtil.prepare();
	}

	@AfterEach
	void afterEach() {

		// テストフォルダを削除する
		TestUtil.clearOutputDir();
	}

	//
	// 各テストメソッドはstatic、private禁止、戻り値も返却してはならない
	//

	@Test
	void test01() throws Exception {

		// 必須パラメータがないパターン
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		GetTableSelectSqlService service = new GetTableSelectSqlService();
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "db"), e.getLocalizedMessage());
		}

		input.setDb(TestUtil.getDb());
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
		input.setDb(TestUtil.getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("sqlPath", "noexistpath"); // 存在しない出力先
		input.putString("tableName", tableName);
		GenericParam output = new GenericParam();
		GetTableSelectSqlService service = new GetTableSelectSqlService();
		try {
			service.doService(input, output);
			fail();
		} catch (FileNotFoundException e) {
			assertTrue(e.getLocalizedMessage().contains("SELECT_SCR.txt"));
		}
	}

	@Test
	void test03() throws Exception {

		// 必要なパラメータを準備する
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";
		String tableName = "SCR";

		// 正常系パターン
		GenericParam input = new GenericParam();
		input.setDb(TestUtil.getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("sqlPath", sqlPath);
		input.putString("tableName", tableName);
		GenericParam output = new GenericParam();
		GetTableSelectSqlService service = new GetTableSelectSqlService();
		service.doService(input, output);

		// ファイルが生成されていることを確認する
		String outputPath = dirPath + "/" + sqlPath + "/";
		assertTrue(new File(outputPath + "SELECT_SCR.txt").exists());
		assertTrue(new File(outputPath + "SELECT_SCR.txt").length() > 0);
	}

	@Test
	void test04() throws Exception {

		// 必要なパラメータを準備する
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";
		String tableName = "SCR";

		// カバレッジ(オンメモリにテーブルリストがある状態で実行)
		GenericParam input = new GenericParam();
		input.setDb(TestUtil.getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("sqlPath", sqlPath);
		input.putString("tableName", tableName);
		GenericParam output = new GenericParam();
		GetTableDefByFileService preExecService = new GetTableDefByFileService();
		preExecService.doService(input, output);
		var tableDef = output.getRecordList("tableDef" + tableName);
		input.putRecordList("tableDef" + tableName, tableDef);
		GetTableSelectSqlService service = new GetTableSelectSqlService();
		service.doService(input, output);

		// ファイルが生成されていることを確認する
		String outputPath = dirPath + "/" + sqlPath + "/";
		assertTrue(new File(outputPath + "SELECT_SCR.txt").exists());
		assertTrue(new File(outputPath + "SELECT_SCR.txt").length() > 0);

		// カバレッジ(サイズが0のtableDef)
		tableDef.clear();
		input.putRecordList("tableDef" + tableName, tableDef);
		service.doService(input, output);

		// オンメモリの空情報は使わず、ファイルが生成されていることを確認する
		assertTrue(new File(outputPath + "SELECT_SCR.txt").exists());
		assertTrue(new File(outputPath + "SELECT_SCR.txt").length() > 0);
	}
}
