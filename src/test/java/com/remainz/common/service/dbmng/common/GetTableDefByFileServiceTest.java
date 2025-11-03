package com.remainz.common.service.dbmng.common;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.TestUtil;
import com.remainz.common.exception.BusinessRuleViolationException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.util.Mu;

/**
 * テーブルの定義情報を取得するクラスです。
 */
public class GetTableDefByFileServiceTest {

	private static final String TEST_PATH = TestUtil.RESOURCE_PATH + "service/dbmng/common";

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
		GetTableDefByFileService service = new GetTableDefByFileService();
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
			assertEquals(new Mu().msg("msg.common.noParam", "tableName"), e.getLocalizedMessage());
		}
	}

	@Test
	void test02() throws Exception {

		// 必要なパラメータを準備する
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created/notExistPath";
		String tableName = "SCR";

		// (カバレッジ)存在しないファイルを指定するパターン
		GenericParam input = new GenericParam();
		input.setDb(TestUtil.getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("tableName", tableName);
		GenericParam output = new GenericParam();
		GetTableDefByFileService service = new GetTableDefByFileService();
		try {
			service.doService(input, output);
			fail();
		} catch (FileNotFoundException e) {
			assertTrue(e.getLocalizedMessage().contains("notExistPath"));
		}
	}

	@Test
	void test03() throws Exception {

		// 必要なパラメータを準備する
		String dirPath = TEST_PATH;
		String defPath = "GetTableDefByFileServiceTest";
		String tableName = "NO_HEADER";
		String tableDefFilePath = dirPath + "/" + defPath + "/" + tableName + ".txt";

		// (カバレッジ)ヘッダなしの処理ルートを通るパターン
		GenericParam input = new GenericParam();
		input.setDb(TestUtil.getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("tableName", tableName);
		GenericParam output = new GenericParam();
		GetTableDefByFileService service = new GetTableDefByFileService();
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.err.common.invalidTableDef", tableDefFilePath),
					e.getLocalizedMessage());
		}
	}

	@Test
	void test04() throws Exception {

		// 必要なパラメータを準備する
		String dirPath = TEST_PATH;
		String defPath = "GetTableDefByFileServiceTest";
		String tableName = "BLANK_LINE";

		// (カバレッジ)ヘッダなしの処理ルートを通るパターン
		GenericParam input = new GenericParam();
		input.setDb(TestUtil.getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("tableName", tableName);
		GenericParam output = new GenericParam();
		GetTableDefByFileService service = new GetTableDefByFileService();
		service.doService(input, output);
		assertNotNull(output.getRecordList("tableDef" + tableName));
		assertEquals(8, output.getRecordList("tableDef" + tableName).size());
	}

	@Test
	void test05() throws Exception {

		// どんな場合でも必ず同じテスト結果となるよう、共通の固定ダミーテーブル定義を適用する
		TestUtil.prepare();

		// 必要なパラメータを準備する
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String tableName = "SCR";

		// 正常系
		GenericParam input = new GenericParam();
		input.setDb(TestUtil.getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("tableName", tableName);
		GenericParam output = new GenericParam();
		GetTableDefByFileService service = new GetTableDefByFileService();
		service.doService(input, output);
		assertNotNull(output.getRecordList("tableDef" + tableName));
		assertEquals(8, output.getRecordList("tableDef" + tableName).size());
	}
}
