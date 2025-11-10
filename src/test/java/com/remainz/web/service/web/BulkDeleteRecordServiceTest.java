package com.remainz.web.service.web;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.remainz.TestUtil;
import com.remainz.common.exception.ApplicationInternalException;
import com.remainz.common.exception.BusinessRuleViolationException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.util.Mu;

public class BulkDeleteRecordServiceTest {

	@BeforeAll
	static void beforeAll() throws Exception {

		// DBの準備を行う
		TestUtil.restoreDbIfNotYet();
	}

	@Test
	void test01() {

		// パラメータ指定なしパターン
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new BulkDeleteRecordService();
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
	}

	@Test
	void test02() throws SQLException {

		// 正常系
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new BulkDeleteRecordService();
		input.setDb(TestUtil.getDb());
		input.putString("tableName", "VIEW_DEF");
		input.putString("1000018", "on");
		input.putString("1000020", "on");

		service.doService(input, output);
		assertEquals("VIEW_DEF", output.getString("tableName"));
		assertEquals("1000018, 1000020", output.getString("recordId"));
		assertEquals("2", output.getString("updateCnt"));
		var recordList = input.getDb().select("SELECT * FROM VIEW_DEF WHERE VIEW_DEF_ID IN(1000018, 1000020)");
		assertEquals(0, recordList.size());

		// DB更新をロールバックする
		input.getDb().rollback();
	}

	@Test
	void test03() throws SQLException {

		// カバレッジ(SQLで例外発生)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new BulkDeleteRecordService();
		input.setDb(TestUtil.getDb());
		input.putString("tableName", "NOTEXISTTABLE");
		input.putString("1000018", "on");

		try {
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains("SQLException"));
		}

		// DB更新をロールバックする
		input.getDb().rollback();
	}
}
