package com.remainz.common.service.dbmng.common;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
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
import com.remainz.common.util.Mu;

/**
 * テーブルデータを取得するクラスです。
 */
public class GetAllTableDataServiceTest {

	private String dbName;

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

		// DB構成取得を実行し、前提ファイルを取得する
		testUtil.prepare();

		// テーブルデータを取得するために必要なSELECTのSQLを生成する
		GetAllTableInsertSqlServiceTest.createAllTableSelectSql(dbName);
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
		GetAllTableDataService service = new GetAllTableDataService();
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
	}

	@Test
	void test02() throws Exception {

		// テーブル名リストファイルのパスが指定されているケース
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String dataPath = "20_dbdata/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";
		String tableNameListFilePath = TestUtil.RESOURCE_PATH + "service/dbmng/common/GetAllTableDefServiceTest/tableNameList.txt";
		doService(dirPath, dataPath, sqlPath, tableNameListFilePath);

		// DB定義ファイルが出力されていることを確認する
		assertFileOutput();
	}

	@Test
	void test03() throws Exception {

		// テーブル名リストが指定されているケース
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String dataPath = "20_dbdata/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";
		var tableNameList = new ArrayList<LinkedHashMap<String, String>>();
		addMapToList(tableNameList, "GNR_GRP");
		addMapToList(tableNameList, "GNR_KEY_VAL");
		addMapToList(tableNameList, "ACCNT");
		addMapToList(tableNameList, "SCR");
		addMapToList(tableNameList, "SCR_ELM");
		doServiceByTableNameList(dirPath, dataPath, sqlPath, tableNameList);

		// DB定義ファイルが出力されていることを確認する
		assertFileOutput();
	}

	@Test
	void test04() throws Exception {

		// (カバレッジ)テーブル名リストがnullのケース
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String dataPath = "20_dbdata/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";
		doServiceByTableNameList(dirPath, dataPath, sqlPath, null);

		// DB定義ファイルが出力されていないことを確認する
		assertNoFileOutput();
	}

	@Test
	void test05() throws Exception {

		// (カバレッジ)テーブル名リストが空のケース
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String dataPath = "20_dbdata/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";
		var tableNameList = new ArrayList<LinkedHashMap<String, String>>();
		doServiceByTableNameList(dirPath, dataPath, sqlPath, tableNameList);

		// DB定義ファイルが出力されていないことを確認する
		assertNoFileOutput();
	}

	@Test
	void test06() throws Exception {

		// (カバレッジ)存在しないテーブル名リストファイルのパスが指定されているケース
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String dataPath = "20_dbdata/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";
		String tableNameListFilePath = "nowhere.txt";
		try {
			doService(dirPath, dataPath, sqlPath, tableNameListFilePath);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains("FileNotFoundException"));
		}
	}

	private void doService(String dirPath, String dataPath, String sqlPath,
			String tableNameListFilePath) throws Exception {

		GenericParam input = new GenericParam();
		input.setDb(testUtil.getDb());
		input.putString("dirPath", dirPath);
		input.putString("dataPath", dataPath);
		input.putString("sqlPath", sqlPath);
		input.putString("tableNameListFilePath", tableNameListFilePath);
		GenericParam output = new GenericParam();
		GetAllTableDataService service = new GetAllTableDataService();
		service.doService(input, output);
	}

	private void doServiceByTableNameList(String dirPath, String dataPath, String sqlPath,
			ArrayList<LinkedHashMap<String, String>> tableNameList) throws Exception {

		GenericParam input = new GenericParam();
		input.setDb(testUtil.getDb());
		input.putString("dirPath", dirPath);
		input.putString("dataPath", dataPath);
		input.putString("sqlPath", sqlPath);
		input.putRecordList("tableNameList", tableNameList);
		GenericParam output = new GenericParam();
		GetAllTableDataService service = new GetAllTableDataService();
		service.doService(input, output);
	}

	private void addMapToList(ArrayList<LinkedHashMap<String, String>> tableNameList, String tableName) {
		var tableNameMap = new LinkedHashMap<String, String>();
		tableNameMap.put("", tableName);
		tableNameList.add(tableNameMap);
	}

	private void assertFileOutput() {

		// ファイルが出力されていることを確認する
		String outputPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName + "/20_dbdata/20_auto_created/";
		assertTrue(new File(outputPath + "GNR_GRP.txt").exists());
		assertTrue(new File(outputPath + "GNR_GRP.txt").length() > 0);
		assertTrue(new File(outputPath + "GNR_KEY_VAL.txt").exists());
		assertTrue(new File(outputPath + "GNR_KEY_VAL.txt").length() > 0);
		assertTrue(new File(outputPath + "ACCNT.txt").exists());
		assertTrue(new File(outputPath + "ACCNT.txt").length() > 0);
		assertTrue(new File(outputPath + "SCR.txt").exists());
		assertTrue(new File(outputPath + "SCR.txt").length() > 0);
		assertTrue(new File(outputPath + "SCR_ELM.txt").exists());
		assertTrue(new File(outputPath + "SCR_ELM.txt").length() > 0);
	}

	private void assertNoFileOutput() {

		// ファイルが出力されていないことを確認する
		String outputPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName + "/20_dbdata/20_auto_created/";
		assertFalse(new File(outputPath + "GNR_GRP.txt").exists());
		assertFalse(new File(outputPath + "GNR_GRP.txt").length() > 0);
		assertFalse(new File(outputPath + "GNR_KEY_VAL.txt").exists());
		assertFalse(new File(outputPath + "GNR_KEY_VAL.txt").length() > 0);
		assertFalse(new File(outputPath + "ACCNT.txt").exists());
		assertFalse(new File(outputPath + "ACCNT.txt").length() > 0);
		assertFalse(new File(outputPath + "SCR.txt").exists());
		assertFalse(new File(outputPath + "SCR.txt").length() > 0);
		assertFalse(new File(outputPath + "SCR_ELM.txt").exists());
		assertFalse(new File(outputPath + "SCR_ELM.txt").length() > 0);
	}
}
