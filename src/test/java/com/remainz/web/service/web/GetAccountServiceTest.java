package com.remainz.web.service.web;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.TestUtil;
import com.remainz.common.db.GenericDb;
import com.remainz.common.exception.ApplicationInternalException;
import com.remainz.common.exception.BusinessRuleViolationException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.util.Mu;
import com.remainz.web.exception.RoleRestrictionException;

public class GetAccountServiceTest {

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
		var service = new GetAccountService();
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
			assertEquals(new Mu().msg("msg.common.noParam", "requestKind"), e.getLocalizedMessage());
		}

		try {
			input.putString("requestKind", "GET");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "requestUri"), e.getLocalizedMessage());
		}
	}

	@Test
	void test02() {

		// 正常系(アカウントID指定なし)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new GetAccountService();
		input.setDb(TestUtil.getDb());
		input.putString("requestKind", "GET");
		input.putString("requestUri", "/jl/service/top.html");

		service.doService(input, output);
		assertEquals("1000001", output.getRecordList("account").get(0).get("ACCNT_ID"));
		assertEquals("ゲスト", output.getRecordList("account").get(0).get("ACCOUNT_NAME"));
	}

	@Test
	void test03() {

		// 正常系(ロール制約を受けるURLにアクセス)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new GetAccountService();
		input.setDb(TestUtil.getDb());
		input.putString("requestKind", "GET");
		input.putString("requestUri", "/jl/service/dbMainte.html");

		try {
			service.doService(input, output);
			fail();
		} catch (RoleRestrictionException e) {
			assertTrue(e.getLocalizedMessage().contains(
					"[{APROLE_ID=1000301}, {APROLE_ID=1000401}]"));
		}
	}

	@Test
	void test04() {

		// カバレッジ(ロール制約をクリアできるアカウントID)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new GetAccountService();
		input.setDb(TestUtil.getDb());
		input.putString("requestKind", "GET");
		input.putString("requestUri", "/jl/service/dbMainte.html");
		input.putString("accountId", "1000301");

		service.doService(input, output);
		assertEquals("1000301", output.getRecordList("account").get(0).get("ACCNT_ID"));
		assertEquals("マスタ", output.getRecordList("account").get(0).get("ACCOUNT_NAME"));
	}

	@Test
	void test05() {

		// カバレッジ(DBアクセス時に例外発生)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new GetAccountService();
		input.setDb(new GenericDb()); // 単にnewしただけではDB接続していないので、例外が発生する
		input.putString("requestKind", "GET");
		input.putString("requestUri", "/jl/service/nowhere.html");

		try {
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains(
					"NullPointerException"));
		}
	}
}
