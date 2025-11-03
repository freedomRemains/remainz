package com.remainz.common.service.dbmng.common;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.TestUtil;
import com.remainz.common.exception.ApplicationInternalException;
import com.remainz.common.exception.BusinessRuleViolationException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.util.Mu;

/**
 * テーブル削除のSQLを生成するクラスです。
 */
public class GetAllTableDropSqlServiceTest {

	private String dbName;

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
		GetAllTableDropSqlService service = new GetAllTableDropSqlService();
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
	}

	@Test
	void test02() throws Exception {

		// DB構成取得を実行し、前提ファイルを取得する
		TestUtil.restoreDbIfNotYet();
		TestUtil.prepareOutputDir();
		TestUtil.getAllTable(TestUtil.OUTPUT_PATH);

		// テーブル名リストファイルのパスが指定されているケース
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String sqlPath = "30_sql/20_auto_created";
		String tableNameListFilePath = TestUtil.RESOURCE_PATH + "service/dbmng/common/GetAllTableDefServiceTest/tableNameList.txt";
		doService(dirPath, sqlPath, tableNameListFilePath);

		// DB定義ファイルが出力されていることを確認する
		assertFileOutput();
	}

	@Test
	void test03() throws Exception {

		// DB構成取得を実行し、前提ファイルを取得する
		TestUtil.restoreDbIfNotYet();
		TestUtil.prepareOutputDir();
		TestUtil.getAllTable(TestUtil.OUTPUT_PATH);

		// テーブル名リストが指定されているケース
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String sqlPath = "30_sql/20_auto_created";
		var tableNameList = new ArrayList<LinkedHashMap<String, String>>();
		addMapToList(tableNameList, "GNR_GRP");
		addMapToList(tableNameList, "GNR_KEY_VAL");
		addMapToList(tableNameList, "ACCNT");
		addMapToList(tableNameList, "SCR");
		addMapToList(tableNameList, "SCR_ELM");
		doServiceByTableNameList(dirPath, sqlPath, tableNameList);

		// DB定義ファイルが出力されていることを確認する
		assertFileOutput();
	}

	@Test
	void test04() throws Exception {

		// (カバレッジ)テーブル名リストがnullのケース
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String sqlPath = "30_sql/20_auto_created";
		doServiceByTableNameList(dirPath, sqlPath, null);

		// DB定義ファイルが出力されていないことを確認する
		assertNoFileOutput();
	}

	@Test
	void test05() throws Exception {

		// (カバレッジ)テーブル名リストが空のケース
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String sqlPath = "30_sql/20_auto_created";
		var tableNameList = new ArrayList<LinkedHashMap<String, String>>();
		doServiceByTableNameList(dirPath, sqlPath, tableNameList);

		// DB定義ファイルが出力されていないことを確認する
		assertNoFileOutput();
	}

	@Test
	void test06() throws Exception {

		// (カバレッジ)存在しないテーブル名リストファイルのパスが指定されているケース
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String sqlPath = "30_sql/20_auto_created";
		String tableNameListFilePath = "nowhere.txt";
		try {
			doService(dirPath, sqlPath, tableNameListFilePath);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains("FileNotFoundException"));
		}
	}

	private void doService(String dirPath, String sqlPath, String tableNameListFilePath) throws Exception {

		GenericParam input = new GenericParam();
		input.setDb(TestUtil.getDb());
		input.putString("dirPath", dirPath);
		input.putString("sqlPath", sqlPath);
		input.putString("tableNameListFilePath", tableNameListFilePath);
		GenericParam output = new GenericParam();
		GetAllTableDropSqlService service = new GetAllTableDropSqlService();
		service.doService(input, output);
	}

	private void doServiceByTableNameList(String dirPath, String sqlPath,
			ArrayList<LinkedHashMap<String, String>> tableNameList) throws Exception {

		GenericParam input = new GenericParam();
		input.setDb(TestUtil.getDb());
		input.putString("dirPath", dirPath);
		input.putString("sqlPath", sqlPath);
		input.putRecordList("tableNameList", tableNameList);
		GenericParam output = new GenericParam();
		GetAllTableDropSqlService service = new GetAllTableDropSqlService();
		service.doService(input, output);
	}

	private void addMapToList(ArrayList<LinkedHashMap<String, String>> tableNameList, String tableName) {
		var tableNameMap = new LinkedHashMap<String, String>();
		tableNameMap.put("", tableName);
		tableNameList.add(tableNameMap);
	}

	private void assertFileOutput() {

		// ファイルが出力されていることを確認する
		String outputPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName + "/30_sql/20_auto_created/";
		assertTrue(new File(outputPath + "DROP_GNR_GRP.txt").exists());
		assertTrue(new File(outputPath + "DROP_GNR_GRP.txt").length() > 0);
		assertTrue(new File(outputPath + "DROP_GNR_KEY_VAL.txt").exists());
		assertTrue(new File(outputPath + "DROP_GNR_KEY_VAL.txt").length() > 0);
		assertTrue(new File(outputPath + "DROP_ACCNT.txt").exists());
		assertTrue(new File(outputPath + "DROP_ACCNT.txt").length() > 0);
		assertTrue(new File(outputPath + "DROP_SCR.txt").exists());
		assertTrue(new File(outputPath + "DROP_SCR.txt").length() > 0);
		assertTrue(new File(outputPath + "DROP_SCR_ELM.txt").exists());
		assertTrue(new File(outputPath + "DROP_SCR_ELM.txt").length() > 0);
	}

	private void assertNoFileOutput() {

		// ファイルが出力されていないことを確認する
		String outputPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName + "/30_sql/20_auto_created/";
		assertFalse(new File(outputPath + "DROP_GNR_GRP.txt").exists());
		assertFalse(new File(outputPath + "DROP_GNR_GRP.txt").length() > 0);
		assertFalse(new File(outputPath + "DROP_GNR_KEY_VAL.txt").exists());
		assertFalse(new File(outputPath + "DROP_GNR_KEY_VAL.txt").length() > 0);
		assertFalse(new File(outputPath + "DROP_ACCNT.txt").exists());
		assertFalse(new File(outputPath + "DROP_ACCNT.txt").length() > 0);
		assertFalse(new File(outputPath + "DROP_SCR.txt").exists());
		assertFalse(new File(outputPath + "DROP_SCR.txt").length() > 0);
		assertFalse(new File(outputPath + "DROP_SCR_ELM.txt").exists());
		assertFalse(new File(outputPath + "DROP_SCR_ELM.txt").length() > 0);
	}
}
