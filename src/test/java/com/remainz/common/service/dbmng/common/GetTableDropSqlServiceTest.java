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
 * 各テーブルのSQLを取得するクラスです。 <br>
 * <br>
 * [inputに必要なパラメータ]<br>
 * dirPath ディレクトリパス<br>
 * sqlPath SQLパス<br>
 * tableName テーブル名<br>
 * tableDefFilePath テーブル定義ファイルパス<br>
 * <br>
 * 各パラメータの例<br>
 * dirPath new JwProp().get("base.dir") + PATH_DELM + getResourcePath()<br>
 * sqlPath "30_sql/20_auto_created"<br>
 * tableName "TDICTITEM"<br>
 * tableDefFilePath = input.getString("dirPath") + PATH_DELM<br>
 * + input.getString("defPath") + PATH_DELM + tableName + ".txt";<br>
 */
public class GetTableDropSqlServiceTest {

	private static String dbName;

	@BeforeEach
	void beforeEach() throws Exception {

		// テストに必要な準備処理を実行する
		dbName = TestUtil.getDbName();
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
		GetTableDropSqlService service = new GetTableDropSqlService();
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
		String tableName = "SCR";

		// (カバレッジ)存在しない出力先を指定するパターン
		GenericParam input = new GenericParam();
		input.setDb(TestUtil.getDb());
		input.putString("dirPath", dirPath);
		input.putString("sqlPath", "noexistpath"); // 存在しない出力先
		input.putString("tableName", tableName);
		GenericParam output = new GenericParam();
		GetTableDropSqlService service = new GetTableDropSqlService();
		try {
			service.doService(input, output);
			fail();
		} catch (FileNotFoundException e) {
			assertTrue(e.getLocalizedMessage().contains("noexistpath"));
		}
	}

	@Test
	void test03() throws Exception {

		// どんな場合でも必ず同じテスト結果となるよう、共通の固定ダミーテーブル定義を適用する
		TestUtil.prepare();

		// 必要なパラメータを準備する
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String sqlPath = "30_sql/20_auto_created";
		String tableName = "SCR";

		// 正常系パターン
		GenericParam input = new GenericParam();
		input.setDb(TestUtil.getDb());
		input.putString("dirPath", dirPath);
		input.putString("sqlPath", sqlPath);
		input.putString("tableName", tableName);
		GenericParam output = new GenericParam();
		GetTableDropSqlService service = new GetTableDropSqlService();
		service.doService(input, output);

		// ファイルが生成されていることを確認する
		String outputPath = dirPath + "/" + sqlPath + "/";
		assertTrue(new File(outputPath + "DROP_SCR.txt").exists());
		assertTrue(new File(outputPath + "DROP_SCR.txt").length() > 0);
	}
}
