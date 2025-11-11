package com.remainz.common.service.dbmng.common;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.sql.SQLSyntaxErrorException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.TestUtil;
import com.remainz.common.exception.BusinessRuleViolationException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.util.FileUtil;
import com.remainz.common.util.Mu;

/**
 * テーブル物理名取得が実行可能なDBで、全テーブルの名前を取得するクラスです。
 */
public class GetTableNameListServiceTest {

	private TestUtil testUtil;

	@BeforeEach
	void beforeEach() {

		// DB接続を取得し、トランザクションを開始する
		testUtil = new TestUtil();
		testUtil.getDb();
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

		// 必須パラメータなしのパターン
		GetTableNameListService service = new GetTableNameListService();
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "db"), e.getLocalizedMessage());
		}

		try {
			input.setDb(testUtil.getDb());
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "dirPath"), e.getLocalizedMessage());
		}

		try {
			input.putString("dirPath", TestUtil.OUTPUT_PATH + "dbmng/mysql");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "defPath"), e.getLocalizedMessage());
		}

		try {
			input.putString("defPath", "10_dbdef/20_auto_created");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "getTableNameListSql"), e.getLocalizedMessage());
		}
	}

	@Test
	void test02() throws Exception {

		// SQLエラー
		String getTableNameListSql = """
				SELECT NOT_EXIST_COLUMN
				  FROM NOT_EXIST_TABLE
				  ;
				""";

		// 必要なディレクトリがなければ作成する
		new FileUtil().createDirIfNotExists(TestUtil.OUTPUT_PATH + "dbmng/h2/10_dbdef/20_auto_created");

		// SQLエラーのパターン
		GetTableNameListService service = new GetTableNameListService();
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		input.setDb(testUtil.getDb());
		input.putString("dirPath", TestUtil.OUTPUT_PATH + "dbmng/h2");
		input.putString("defPath", "10_dbdef/20_auto_created");
		input.putString("getTableNameListSql", getTableNameListSql);
		try {
			service.doService(input, output);
			fail();
		} catch (SQLSyntaxErrorException e) {
			assertTrue(e.getLocalizedMessage().contains("NOT_EXIST_TABLE"));
		}

		// 生成したディレクトリを削除する
		new FileUtil().deleteDirIfExists(TestUtil.OUTPUT_PATH);
	}

	@Test
	void test03() throws Exception {

		// MySQLかH2かによってSQLを分ける
		String dbName = "h2";
		String getTableNameListSql = testUtil.createGetTableNameListSql();

		// 必要なディレクトリがなければ作成する
		new FileUtil().createDirIfNotExists(TestUtil.OUTPUT_PATH + "dbmng/" + dbName + "/10_dbdef/20_auto_created");

		// 正常系のパターン
		GetTableNameListService service = new GetTableNameListService();
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		input.setDb(testUtil.getDb());
		input.putString("dirPath", TestUtil.OUTPUT_PATH + "dbmng/" + dbName);
		input.putString("defPath", "10_dbdef/20_auto_created");
		input.putString("getTableNameListSql", getTableNameListSql);
		service.doService(input, output);
		assertTrue(new File(TestUtil.OUTPUT_PATH + "dbmng/" + dbName + "/10_dbdef/20_auto_created/tableNameList.txt").exists());

		// 生成したディレクトリを削除する
		new FileUtil().deleteDirIfExists(TestUtil.OUTPUT_PATH);
	}
}
