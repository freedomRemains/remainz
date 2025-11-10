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

public class CreateRecordServiceTest {

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
		var service = new CreateRecordService();
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
			assertEquals(new Mu().msg("msg.common.noParam", "accountId"), e.getLocalizedMessage());
		}

		try {
			input.putString("accountId", "data_loader");
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
		var service = new CreateRecordService();
		input.setDb(TestUtil.getDb());
		input.putString("accountId", "data_loader");
		input.putString("tableName", "GNR_GRP");
		input.putString("GNR_GRP_NAME", "newGroup");

		service.doService(input, output);
		assertEquals("GNR_GRP", output.getString("tableName"));
		assertEquals("1", output.getString("updateCnt"));
		var recordList = input.getDb().select("SELECT * FROM GNR_GRP WHERE GNR_GRP_NAME = 'newGroup'");
		assertEquals(1, recordList.size());

		// DB更新をロールバックする
		input.getDb().rollback();
	}

	@Test
	void test03() throws SQLException {

		// カバレッジ(SQLで例外発生)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new CreateRecordService();
		input.setDb(TestUtil.getDb());
		input.putString("accountId", "data_loader");
		input.putString("tableName", "NOTEXISTTABLE");
		input.putString("GNR_GRP_NAME", "newGroup");

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

		// カバレッジ(VERSION他のカラムあり)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new CreateRecordService();
		input.setDb(TestUtil.getDb());
		input.putString("accountId", "data_loader");
		input.putString("tableName", "GNR_GRP");
		input.putString("GNR_GRP_NAME", "newGroup");

		service.doService(input, output);
		assertEquals("GNR_GRP", output.getString("tableName"));
		assertEquals("1", output.getString("updateCnt"));
		var recordList = input.getDb().select("SELECT * FROM GNR_GRP WHERE GNR_GRP_NAME = 'newGroup'");
		assertEquals(1, recordList.size());

		// DB更新をロールバックする
		input.getDb().rollback();
	}

	@Test
	void test05() throws SQLException {

		// カバレッジ(VERSION他のカラムなし)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new CreateRecordService();
		input.setDb(TestUtil.getDb());
		input.putString("accountId", "data_loader");
		input.putString("tableName", "VIEW_DEF");
		input.putString("TABLE_NAME", "TNEWTBL1");
		input.putString("FIELD_NAME", "NEW_FIELD_1");
		input.putString("TYPE_NAME", "INT");
		input.putString("ALLOW_NULL", "YES");
		input.putString("KEY_DIV", "");
		input.putString("DEFAULT_VALUE", "null");
		input.putString("EXTRA", "");
		input.putString("TABLE_LOGICAL_NAME", "新規テーブル1");
		input.putString("FIELD_LOGICAL_NAME", "新規フィールド1");
		input.putString("FOREIGN_TABLE", "TEXISTTBL1");
											

		service.doService(input, output);
		assertEquals("VIEW_DEF", output.getString("tableName"));
		assertEquals("1", output.getString("updateCnt"));
		var recordList = input.getDb().select("SELECT * FROM VIEW_DEF WHERE TABLE_NAME = 'TNEWTBL1'");
		assertEquals(1, recordList.size());

		// DB更新をロールバックする
		input.getDb().rollback();
	}

	@Test
	void test06() throws SQLException {

		// カバレッジ(1レコードもないテーブルにレコードを追加)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new CreateRecordService();
		input.setDb(TestUtil.getDb());
		input.putString("accountId", "data_loader");
		input.putString("tableName", "MAIL");
		input.putString("MAIL_FROM", "mailFrom");

		service.doService(input, output);
		assertEquals("MAIL", output.getString("tableName"));
		assertEquals("1", output.getString("updateCnt"));
		var recordList = input.getDb().select("SELECT * FROM MAIL WHERE MAIL_FROM = 'mailFrom'");
		assertEquals(1, recordList.size());

		// DB更新をロールバックする
		input.getDb().rollback();
	}

	@Test
	void test07() throws SQLException {

		// カバレッジ(規則的採番要求)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new CreateRecordService();
		input.setDb(TestUtil.getDb());
		input.putString("accountId", "data_loader");
		input.putString("tableName", "GNR_GRP");
		input.putString("requireRuledNumber", "true");
		input.putString("GNR_GRP_NAME", "newGroup");

		service.doService(input, output);
		assertEquals("GNR_GRP", output.getString("tableName"));
		assertEquals("1", output.getString("updateCnt"));
		var recordList = input.getDb().select("SELECT * FROM GNR_GRP WHERE GNR_GRP_NAME = 'newGroup'");
		assertEquals(1, recordList.size());

		// DB更新をロールバックする
		input.getDb().rollback();
	}
}
