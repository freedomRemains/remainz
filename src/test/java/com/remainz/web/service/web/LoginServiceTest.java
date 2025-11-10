package com.remainz.web.service.web;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.TestUtil;
import com.remainz.common.exception.BusinessRuleViolationException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.util.Mu;

public class LoginServiceTest {

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
		var service = new LoginService();
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
			assertEquals(new Mu().msg("msg.common.noParam", "MAIL_ADDRESS"), e.getLocalizedMessage());
		}

		try {
			input.putString("MAIL_ADDRESS", "test@nowhere.com");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "PASSWORD"), e.getLocalizedMessage());
		}
	}

	@Test
	void test02() {

		// 正常系
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new LoginService();
		input.setDb(TestUtil.getDb());
		input.putString("MAIL_ADDRESS", "master@account.com");
		input.putString("PASSWORD", "password");

		service.doService(input, output);
		assertEquals("1000301", input.getString("accountId"));
	}

	@Test
	void test03() throws SQLException {

		// 存在しないユーザ
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new LoginService();
		input.setDb(TestUtil.getDb());
		input.putString("MAIL_ADDRESS", "master2@account.com");
		input.putString("PASSWORD", "password");

		service.doService(input, output);
		assertEquals("redirect", output.getString("respKind"));
		assertEquals("myPage.html?errMsgKey=1", output.getString("destination"));

		// DB更新をロールバックする
		input.getDb().rollback();
	}

	@Test
	void test04() throws SQLException {

		// パスワード誤り
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new LoginService();
		input.setDb(TestUtil.getDb());
		input.putString("MAIL_ADDRESS", "master@account.com");
		input.putString("PASSWORD", "ngPassword");

		service.doService(input, output);
		assertEquals("redirect", output.getString("respKind"));
		assertEquals("myPage.html?errMsgKey=1", output.getString("destination"));

		// DB更新をロールバックする
		input.getDb().rollback();
	}

	@Test
	void test05() throws Exception {

		// カバレッジ(DBに同一メールアドレスが2つ存在)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new LoginService();
		input.setDb(TestUtil.getDb());
		input.putString("MAIL_ADDRESS", "master@account.com");
		input.putString("PASSWORD", "password");

		// リクエストを受けられるよう、DBレコードを更新する
		String sql = """
				INSERT INTO ACCNT(
					ACCNT_ID, ACCOUNT_NAME, MAIL_ADDRESS, PASSWORD, VERSION, IS_DELETED
				) VALUES(
					'1000501', 'マスタ2', 'master@account.com', 'password', 1, 0
				)
				""";
		TestUtil.getDb().update(sql);

		service.doService(input, output);
		assertEquals("redirect", output.getString("respKind"));
		assertEquals("myPage.html?errMsgKey=1", output.getString("destination"));

		// DBをロールバックする
		TestUtil.getDb().rollback();
	}
}
