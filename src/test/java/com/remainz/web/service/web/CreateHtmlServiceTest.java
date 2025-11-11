package com.remainz.web.service.web;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.TestUtil;
import com.remainz.common.exception.ApplicationInternalException;
import com.remainz.common.exception.BusinessRuleViolationException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.util.Mu;

public class CreateHtmlServiceTest {

	private TestUtil testUtil;

	@BeforeEach
	void beforeEach() throws Exception {

		// DB接続を取得し、トランザクションを開始する
		testUtil = new TestUtil();
		testUtil.getDb();

		// テストに必要な準備処理を実行する
		testUtil.restoreDb();
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
		var service = new CreateHtmlService();
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

		// 正常系
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new CreateHtmlService();
		input.setDb(testUtil.getDb());
		input.putString("requestKind", "GET");
		input.putString("requestUri", "/jl/service/top.html");

		service.doService(input, output);
		assertEquals("forward", output.getString("respKind"));
		assertEquals("10000_contents.jsp", output.getString("destination"));
	}

	@Test
	void test03() throws Exception {

		// カバレッジ(ITEM_QUERYでSQLエラーが発生)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new CreateHtmlService();
		input.setDb(testUtil.getDb());
		String requestKind = "GET";
		input.putString("requestKind", requestKind);
		String requestUri = "/jl/service/top.html";
		input.putString("requestUri", requestUri);

		// リクエストを受けられるよう、DBレコードを更新する
		String sql = """
				UPDATE PARTS_ITEM SET
					ITEM_QUERY = 'SELECT ERR_SQL FROM NOT_EXIST_TABLE'
					WHERE PARTS_ITEM_ID = 1000001
				""";
		testUtil.getDb().update(sql);

		try {
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains(
					"SQLException"));

			// DBをロールバックする
			testUtil.getDb().rollback();
		}
	}

	@Test
	void test04() throws Exception {

		// カバレッジ(ITEM_QUERYが存在しない)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new CreateHtmlService();
		input.setDb(testUtil.getDb());
		String requestKind = "GET";
		input.putString("requestKind", requestKind);
		String requestUri = "/jl/service/top.html";
		input.putString("requestUri", requestUri);

		// リクエストを受けられるよう、DBレコードを更新する
		String sql = """
				UPDATE PARTS_ITEM SET
					ITEM_QUERY = ''
					WHERE PARTS_ITEM_ID = 1000001
				""";
		testUtil.getDb().update(sql);

		service.doService(input, output);
		assertEquals("forward", output.getString("respKind"));
		assertEquals("10000_contents.jsp", output.getString("destination"));
	}

	@Test
	void test05() {

		// カバレッジ(limit、offsetの指定あり)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new CreateHtmlService();
		input.setDb(testUtil.getDb());
		input.putString("requestKind", "GET");
		input.putString("requestUri", "/jl/service/tableDataMainte.html");
		input.putString("tableName", "TBL_DEF");
		input.putString("limit", "10");
		input.putString("offset", "10");

		service.doService(input, output);
		assertEquals("forward", output.getString("respKind"));
		assertEquals("10000_contents.jsp", output.getString("destination"));
	}

	@Test
	void test06() {

		// カバレッジ(errMsgKey指定あり)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new CreateHtmlService();
		input.setDb(testUtil.getDb());
		input.putString("requestKind", "GET");
		input.putString("requestUri", "/jl/service/top.html");
		input.putString("errMsgKey", "9999999");

		service.doService(input, output);
		assertEquals("forward", output.getString("respKind"));
		assertEquals("10000_contents.jsp", output.getString("destination"));
	}
}
