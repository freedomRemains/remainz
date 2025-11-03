package com.remainz.common.service.dbmng.common;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.TestUtil;
import com.remainz.common.db.DbInterface;
import com.remainz.common.exception.ApplicationInternalException;
import com.remainz.common.exception.BusinessRuleViolationException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.util.FileUtil;
import com.remainz.common.util.Mu;
import com.remainz.common.util.RcProp;

/**
 * テーブルの定義情報を取得するクラスです。
 */
public class GetAllTableDefServiceTest {

	private String dbName;

	@BeforeEach
	void beforeEach() {

		// テストに必要なフォルダを作成する
		dbName = TestUtil.getDbName();
		new FileUtil().createDirIfNotExists(TestUtil.OUTPUT_PATH + "dbmng/" + dbName + "/10_dbdef/20_auto_created");
	}

	@AfterEach
	void afterEach() {

		// テストフォルダを削除する
		TestUtil.clearOutputDir();
	}

	//
	// 各テストメソッドはstatic、private禁止、戻り値も返却してはならない
	//

	private static final String TEST_RESOURCE_PATH = TestUtil.RESOURCE_PATH + "service/dbmng/common/GetAllTableDefServiceTest/";

	@Test
	void test01() throws Exception {

		// 必須パラメータがないパターン
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		GetAllTableDefService service = new GetAllTableDefService();
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

		input.putString("dirPath", TestUtil.OUTPUT_PATH + "dbmng/" + dbName);
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "defPath"), e.getLocalizedMessage());
		}

		input.putString("defPath", "10_dbdef/20_auto_created");
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "getTableDefSql"), e.getLocalizedMessage());
		}
	}

	@Test
	void test02() throws Exception {

		// DB定義取得用SQLを生成する
		String getTableDefSql = TestUtil.createGetTableDefSql();

		// DBの準備を行う
		prepareDb(TestUtil.getDb());

		// 存在しないテーブル名リストのパスが指定されているケース
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String ngTableNameListFilePath = "nowhere.txt";
		try {
			doService(dirPath, defPath, getTableDefSql, ngTableNameListFilePath);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains("FileNotFoundException"));
		}

		// テーブル名リストファイルのパスが指定されているケース
		String tableNameListFilePath = TEST_RESOURCE_PATH + "tableNameList.txt";
		doService(TestUtil.OUTPUT_PATH + "dbmng/" + dbName, "10_dbdef/20_auto_created", getTableDefSql,
				tableNameListFilePath);

		// DB定義ファイルが出力されていることを確認する
		String outputPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName + "/10_dbdef/20_auto_created/";
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

	@Test
	void test03() throws Exception {

		// DB定義取得用SQLを生成する
		String getTableDefSql = TestUtil.createGetTableDefSql();

		// DBの準備を行う
		prepareDb(TestUtil.getDb());

		// テーブル名リストに存在しないテーブル名が指定されているケース
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		var tableNameList = new ArrayList<LinkedHashMap<String, String>>();
		addMapToList(tableNameList, "NOT_EXIST_TABLE");
		try {
			doServiceByTableNameList(dirPath, defPath, getTableDefSql, tableNameList);
			fail();
		} catch (Exception e) {

			// DB定義ファイルが出力されていないことを確認する
			assertNoFileOutput();
		}
	}

	@Test
	void test04() throws Exception {

		// DB定義取得用SQLを生成する
		String getTableDefSql = TestUtil.createGetTableDefSql();

		// DBの準備を行う
		prepareDb(TestUtil.getDb());

		// テーブル名リストが指定されているケース
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		var tableNameList = new ArrayList<LinkedHashMap<String, String>>();
		addMapToList(tableNameList, "GNR_GRP");
		addMapToList(tableNameList, "GNR_KEY_VAL");
		addMapToList(tableNameList, "ACCNT");
		addMapToList(tableNameList, "SCR");
		addMapToList(tableNameList, "SCR_ELM");
		doServiceByTableNameList(dirPath, defPath, getTableDefSql, tableNameList);

		// DB定義ファイルが出力されていることを確認する
		String outputPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName + "/10_dbdef/20_auto_created/";
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

	@Test
	void test05() throws Exception {

		// DB定義取得用SQLを生成する
		String getTableDefSql = TestUtil.createGetTableDefSql();

		// DBの準備を行う
		prepareDb(TestUtil.getDb());

		// (カバレッジ)テーブル名リストにnullが指定されているケース
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		doServiceByTableNameList(dirPath, defPath, getTableDefSql, null);

		// DB定義ファイルが出力されていないことを確認する
		assertNoFileOutput();
	}

	@Test
	void test06() throws Exception {

		// DB定義取得用SQLを生成する
		String getTableDefSql = TestUtil.createGetTableDefSql();

		// DBの準備を行う
		prepareDb(TestUtil.getDb());

		// (カバレッジ)テーブル名リストに空のリストが指定されているケース
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		doServiceByTableNameList(dirPath, defPath, getTableDefSql,
				new ArrayList<LinkedHashMap<String, String>>());

		// DB定義ファイルが出力されていないことを確認する
		assertNoFileOutput();
	}

	private void prepareDb(DbInterface db) throws Exception {

		// DBがH2の場合は、追加のSQLを実行する
		if (new RcProp().get("db.type").equals("com.remainz.common.db.H2Db")) {
			GenericParam input = new GenericParam();
			input.setDb(db);
			input.putString("sqlFilePath", TEST_RESOURCE_PATH + "10_addSqlForH2.sql");
			input.putString("resultKey", "ret");
			GenericParam output = new GenericParam();
			DbUpdateBySqlFileService service = new DbUpdateBySqlFileService();
			service.doService(input, output);
		}
	}

	private void doService(String dirPath, String defPath, String getTableDefSql,
			String tableNameListFilePath) throws Exception {

		GenericParam input = new GenericParam();
		input.setDb(TestUtil.getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("getTableDefSql", getTableDefSql);
		input.putString("tableNameListFilePath", tableNameListFilePath);
		GenericParam output = new GenericParam();
		GetAllTableDefService service = new GetAllTableDefService();
		service.doService(input, output);
	}

	private void doServiceByTableNameList(String dirPath, String defPath, String getTableDefSql,
			ArrayList<LinkedHashMap<String, String>> tableNameList) throws Exception {

		GenericParam input = new GenericParam();
		input.setDb(TestUtil.getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("getTableDefSql", getTableDefSql);
		input.putRecordList("tableNameList", tableNameList);
		GenericParam output = new GenericParam();
		GetAllTableDefService service = new GetAllTableDefService();
		service.doService(input, output);
	}

	private void addMapToList(ArrayList<LinkedHashMap<String, String>> tableNameList, String tableName) {
		var tableNameMap = new LinkedHashMap<String, String>();
		tableNameMap.put("", tableName);
		tableNameList.add(tableNameMap);
	}

	private void assertNoFileOutput() {
		String outputPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName + "/10_dbdef/20_auto_created/";
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
