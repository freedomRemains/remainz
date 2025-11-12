package com.remainz.web.service.web;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.TestUtil;
import com.remainz.common.exception.ApplicationInternalException;
import com.remainz.common.exception.BusinessRuleViolationException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.util.Mu;

public class UpdateRecordServiceTest {

	private TestUtil testUtil;

	@BeforeEach
	void beforeEach() throws Exception {

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

	@Test
	void test01() {

		// パラメータ指定なしパターン
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new UpdateRecordService();
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
			assertEquals(new Mu().msg("msg.common.noParam", "tableName"), e.getLocalizedMessage());
		}

		try {
			input.putString("tableName", "VIEW_DEF");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "recordId"), e.getLocalizedMessage());
		}
	}

	@Test
	void test02() throws SQLException {

		// 正常系
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new UpdateRecordService();
		input.setDb(testUtil.getDb());
		input.putString("tableName", "VIEW_DEF");
		input.putString("recordId", "1000020");
		input.putString("FOREIGN_TABLE", "TEST_TABLE");

		service.doService(input, output);
		assertEquals("VIEW_DEF", output.getString("tableName"));
		assertEquals("1000020", output.getString("recordId"));
		assertEquals("1", output.getString("updateCnt"));
		var recordList = input.getDb().select("SELECT * FROM VIEW_DEF WHERE VIEW_DEF_ID = 1000020");
		assertEquals("TEST_TABLE", recordList.get(0).get("FOREIGN_TABLE"));

		// DB更新をロールバックする
		input.getDb().rollback();
	}

	@Test
	void test03() throws SQLException {

		// カバレッジ(SQLで例外発生)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new UpdateRecordService();
		input.setDb(testUtil.getDb());
		input.putString("tableName", "NOTEXISTTABLE");
		input.putString("recordId", "1");

		try {
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains("SQLException"));
		}

		// DB更新をロールバックする
		input.getDb().rollback();
	}

	@Test
	void test04() throws SQLException {

		// カバレッジ(VERSIONカラムあり)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new UpdateRecordService();
		input.setDb(testUtil.getDb());
		input.putString("tableName", "GNR_GRP");
		input.putString("recordId", "1000001");
		input.putString("GNR_GRP_NAME", "システムプロパティ2");
		input.putString("VERSION", "1");

		service.doService(input, output);
		assertEquals("GNR_GRP", output.getString("tableName"));
		assertEquals("1000001", output.getString("recordId"));
		assertEquals("1", output.getString("updateCnt"));
		var recordList = input.getDb().select("SELECT * FROM GNR_GRP WHERE GNR_GRP_ID = 1000001");
		assertEquals("システムプロパティ2", recordList.get(0).get("GNR_GRP_NAME"));
		assertEquals("2", recordList.get(0).get("VERSION"));

		// DB更新をロールバックする
		input.getDb().rollback();
	}

	@Test
	void test05() throws SQLException {

		// カバレッジ(UPDATED_ATカラムあり)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new UpdateRecordService();
		input.setDb(testUtil.getDb());
		input.putString("tableName", "GNR_GRP");
		input.putString("recordId", "1000001");
		input.putString("GNR_GRP_NAME", "システムプロパティ3");
		input.putString("UPDATED_AT", "2021-07-12 00:00:00");

		service.doService(input, output);
		assertEquals("GNR_GRP", output.getString("tableName"));
		assertEquals("1000001", output.getString("recordId"));
		assertEquals("1", output.getString("updateCnt"));
		var recordList = input.getDb().select("SELECT * FROM GNR_GRP WHERE GNR_GRP_ID = 1000001");
		assertEquals("システムプロパティ3", recordList.get(0).get("GNR_GRP_NAME"));

		// DB更新をロールバックする
		input.getDb().rollback();
	}

	@Test
	void test06() throws SQLException {

		// カバレッジ(UPDATED_BYカラムあり)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new UpdateRecordService();
		input.setDb(testUtil.getDb());
		input.putString("tableName", "GNR_GRP");
		input.putString("recordId", "1000001");
		input.putString("accountId", "dbadmin");
		input.putString("GNR_GRP_NAME", "システムプロパティ4");
		input.putString("UPDATED_BY", "dbadmin");

		service.doService(input, output);
		assertEquals("GNR_GRP", output.getString("tableName"));
		assertEquals("1000001", output.getString("recordId"));
		assertEquals("1", output.getString("updateCnt"));
		var recordList = input.getDb().select("SELECT * FROM GNR_GRP WHERE GNR_GRP_ID = 1000001");
		assertEquals("システムプロパティ4", recordList.get(0).get("GNR_GRP_NAME"));
		assertEquals("dbadmin", recordList.get(0).get("UPDATED_BY"));

		// DB更新をロールバックする
		input.getDb().rollback();
	}

	@Test
	void test07() throws SQLException {

		// カバレッジ(楽観ロックエラー)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new UpdateRecordService();
		input.setDb(testUtil.getDb());
		input.putString("tableName", "GNR_GRP");
		input.putString("recordId", "1000001");
		input.putString("GNR_GRP_NAME", "システムプロパティ2");
		input.putString("VERSION", "1");

		service.doService(input, output);
		assertEquals("GNR_GRP", output.getString("tableName"));
		assertEquals("1000001", output.getString("recordId"));
		assertEquals("1", output.getString("updateCnt"));
		var recordList = input.getDb().select("SELECT * FROM GNR_GRP WHERE GNR_GRP_ID = 1000001");
		assertEquals("システムプロパティ2", recordList.get(0).get("GNR_GRP_NAME"));
		assertEquals("2", recordList.get(0).get("VERSION"));

		// バージョンを変えずにもう一度更新(楽観ロックエラーを起こさせる)
		input.putString("errMsgKey", "1000301");
		input.putString("GNR_GRP_NAME", "システムプロパティ2");
		service.doService(input, output);
		assertEquals("redirect", output.getString("respKind"));
		assertEquals("tableDataMainte/editRecord.html?tableName=GNR_GRP&recordId=1000001&errMsgKey=1",
				output.getString("destination"));

		// DB更新をロールバックする
		input.getDb().rollback();
	}
}
