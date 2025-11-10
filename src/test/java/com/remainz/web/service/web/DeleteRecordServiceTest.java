package com.remainz.web.service.web;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.TestUtil;
import com.remainz.common.exception.ApplicationInternalException;
import com.remainz.common.exception.BusinessRuleViolationException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.util.Mu;

public class DeleteRecordServiceTest {

	@BeforeAll
	static void beforeAll() throws Exception {
	}

	@AfterAll
	static void afterAll() throws Exception {
	}

	@BeforeEach
	void beforeEach() throws Exception {

		// テストに必要な準備処理を実行する
		TestUtil.restoreDb();
	}

	@Test
	void test01() {

		// パラメータ指定なしパターン
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new DeleteRecordService();
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "db"), e.getLocalizedMessage());
		}

		try {
			input.setDb(TestUtil.getDb());
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
		var service = new DeleteRecordService();
		input.setDb(TestUtil.getDb());
		input.putString("tableName", "VIEW_DEF");
		input.putString("recordId", "1000020");

		service.doService(input, output);
		assertEquals("VIEW_DEF", output.getString("tableName"));
		assertEquals("1000020", output.getString("recordId"));
		assertEquals("1", output.getString("updateCnt"));
		var recordList = input.getDb().select("SELECT * FROM VIEW_DEF WHERE VIEW_DEF_ID = 1000020");
		assertEquals(0, recordList.size());

		// DB更新をロールバックする
		input.getDb().rollback();
	}

	@Test
	void test03() throws SQLException {

		// カバレッジ(SQLで例外発生)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new DeleteRecordService();
		input.setDb(TestUtil.getDb());
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

		// カバレッジ(削除エラー)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new DeleteRecordService();
		input.setDb(TestUtil.getDb());
		input.putString("tableName", "VIEW_DEF");
		input.putString("recordId", "1000020");

		service.doService(input, output);
		assertEquals("VIEW_DEF", output.getString("tableName"));
		assertEquals("1000020", output.getString("recordId"));
		assertEquals("1", output.getString("updateCnt"));
		var recordList = input.getDb().select("SELECT * FROM VIEW_DEF WHERE VIEW_DEF_ID = 1000020");
		assertEquals(0, recordList.size());

		// もう一度削除(削除エラーを起こさせる)
		input.putString("errMsgKey", "9999999");
		service.doService(input, output);
		assertEquals("redirect", output.getString("respKind"));
		assertEquals("tableDataMainte.html?tableName=VIEW_DEF&errMsgKey=1",
				output.getString("destination"));

		// DB更新をロールバックする
		input.getDb().rollback();
	}
}
