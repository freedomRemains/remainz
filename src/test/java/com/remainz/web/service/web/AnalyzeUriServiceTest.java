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

public class AnalyzeUriServiceTest {

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
		var service = new AnalyzeUriService();
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
		var service = new AnalyzeUriService();
		input.setDb(testUtil.getDb());
		input.putString("requestKind", "GET");
		input.putString("requestUri", "/remainz/service/top.html");

		service.doService(input, output);
		assertEquals("forward", output.getString("respKind"));
		assertEquals("10000_contents.jsp", output.getString("destination"));
	}

	@Test
	void test03() {

		// 権限のないURLにアクセス
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new AnalyzeUriService();
		input.setDb(testUtil.getDb());
		input.putString("requestKind", "GET");
		input.putString("requestUri", "/remainz/service/dbMainte.html");

		try {
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains("RoleRestrictionException"));
		}
	}

	@Test
	void test04() {

		// 存在しないURLにアクセス
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new AnalyzeUriService();
		input.setDb(testUtil.getDb());
		input.putString("requestKind", "GET");
		String requestUri = "/remainz/service/nowhere.html";
		input.putString("requestUri", requestUri);

		try {
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains(
					new Mu().msg("msg.err.invalidRequestUri", requestUri)));
		}
	}

	@Test
	void test05() {

		// 許可されていないリクエスト種別
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new AnalyzeUriService();
		input.setDb(testUtil.getDb());
		String requestKind = "PATCH";
		String requestUri = "/remainz/service/top.html";
		input.putString("requestKind", requestKind);
		input.putString("requestUri", requestUri);

		try {
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains(
					new Mu().msg("msg.err.invalidRequestKind", requestUri, requestKind)));
		}
	}

	@Test
	void test06() throws Exception {

		// カバレッジ(該当レコードが2件以上)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new AnalyzeUriService();
		input.setDb(testUtil.getDb());
		input.putString("requestKind", "GET");
		String requestUri = "/remainz/service/top.html";
		input.putString("requestUri", requestUri);

		// insertにより2件以上エラーが出るようにする
		String sql = """
				INSERT INTO HTML_PAGE(
					PAGE_NAME, URI_PATTERN_ID, SCR_ID_GET, RESP_KIND_GET, DESTINATION_GET,
					SCR_ID_POST, RESP_KIND_POST, DESTINATION_POST,
					SCR_ID_PUT, RESP_KIND_PUT, DESTINATION_PUT,
					SCR_ID_DELETE, RESP_KIND_DELETE, DESTINATION_DELETE,
					VERSION, IS_DELETED
				) VALUES(
					'TOP', 1000001, 1100001, 'forward', '10000_contents.jsp',
					1100001, 'forward', '10000_contents.jsp',
					1100001, 'forward', '10000_contents.jsp',
					1100001, 'forward', '10000_contents.jsp',
					1, 0
				)
				""";
		testUtil.getDb().update(sql);

		try {
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains(
					new Mu().msg("msg.err.invalidRequestUri", requestUri)));

			// DBをロールバックする
			testUtil.getDb().rollback();
		}
	}

	@Test
	void test07() throws Exception {

		// カバレッジ(POST)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new AnalyzeUriService();
		input.setDb(testUtil.getDb());
		input.putString("requestKind", "POST");
		String requestUri = "/remainz/service/top.html";
		input.putString("requestUri", requestUri);

		// リクエストを受けられるよう、DBレコードを更新する
		String sql = """
				UPDATE HTML_PAGE SET
					SCR_ID_POST = 1100001,
					RESP_KIND_POST = 'forward',
					DESTINATION_POST = '10000_contents.jsp'
					WHERE HTML_PAGE_ID = 1000001
				""";
		testUtil.getDb().update(sql);

		service.doService(input, output);
		assertEquals("forward", output.getString("respKind"));
		assertEquals("10000_contents.jsp", output.getString("destination"));

		// DBをロールバックする
		testUtil.getDb().rollback();
	}

	@Test
	void test08() throws Exception {

		// カバレッジ(PUT)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new AnalyzeUriService();
		input.setDb(testUtil.getDb());
		input.putString("requestKind", "PUT");
		String requestUri = "/remainz/service/top.html";
		input.putString("requestUri", requestUri);

		// リクエストを受けられるよう、DBレコードを更新する
		String sql = """
				UPDATE HTML_PAGE SET
					SCR_ID_PUT = 1100001,
					RESP_KIND_PUT = 'forward',
					DESTINATION_PUT = '10000_contents.jsp'
					WHERE HTML_PAGE_ID = 1000001
				""";
		testUtil.getDb().update(sql);

		service.doService(input, output);
		assertEquals("forward", output.getString("respKind"));
		assertEquals("10000_contents.jsp", output.getString("destination"));

		// DBをロールバックする
		testUtil.getDb().rollback();
	}

	@Test
	void test09() throws Exception {

		// カバレッジ(DELETE)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new AnalyzeUriService();
		input.setDb(testUtil.getDb());
		input.putString("requestKind", "DELETE");
		String requestUri = "/remainz/service/top.html";
		input.putString("requestUri", requestUri);

		// リクエストを受けられるよう、DBレコードを更新する
		String sql = """
				UPDATE HTML_PAGE SET
					SCR_ID_DELETE = 1100001,
					RESP_KIND_DELETE = 'forward',
					DESTINATION_DELETE = '10000_contents.jsp'
					WHERE HTML_PAGE_ID = 1000001
				""";
		testUtil.getDb().update(sql);

		service.doService(input, output);
		assertEquals("forward", output.getString("respKind"));
		assertEquals("10000_contents.jsp", output.getString("destination"));

		// DBをロールバックする
		testUtil.getDb().rollback();
	}

	@Test
	void test10() throws Exception {

		// カバレッジ(URLパターンはあるが GET の許可がない)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new AnalyzeUriService();
		input.setDb(testUtil.getDb());
		String requestKind = "GET";
		input.putString("requestKind", requestKind);
		String requestUri = "/remainz/service/top.html";
		input.putString("requestUri", requestUri);

		// リクエストを受けられるよう、DBレコードを更新する
		String sql = """
				UPDATE HTML_PAGE SET
					SCR_ID_GET = 0,
					RESP_KIND_DELETE = 'redirect',
					DESTINATION_DELETE = 'top.html'
					WHERE HTML_PAGE_ID = 1000001
				""";
		testUtil.getDb().update(sql);

		try {
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains(
					new Mu().msg("msg.err.invalidRequestKind", requestUri, requestKind)));

			// DBをロールバックする
			testUtil.getDb().rollback();
		}
	}

	@Test
	void test11() throws Exception {

		// カバレッジ(URLパターンはあるが POST の許可がない)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new AnalyzeUriService();
		input.setDb(testUtil.getDb());
		String requestKind = "POST";
		input.putString("requestKind", requestKind);
		String requestUri = "/remainz/service/top.html";
		input.putString("requestUri", requestUri);

		try {
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains(
					new Mu().msg("msg.err.invalidRequestKind", requestUri, requestKind)));

			// DBをロールバックする
			testUtil.getDb().rollback();
		}
	}

	@Test
	void test12() throws Exception {

		// カバレッジ(URLパターンはあるが PUT の許可がない)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new AnalyzeUriService();
		input.setDb(testUtil.getDb());
		String requestKind = "PUT";
		input.putString("requestKind", requestKind);
		String requestUri = "/remainz/service/top.html";
		input.putString("requestUri", requestUri);

		try {
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains(
					new Mu().msg("msg.err.invalidRequestKind", requestUri, requestKind)));

			// DBをロールバックする
			testUtil.getDb().rollback();
		}
	}

	@Test
	void test13() throws Exception {

		// カバレッジ(URLパターンはあるが DELETE の許可がない)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new AnalyzeUriService();
		input.setDb(testUtil.getDb());
		String requestKind = "DELETE";
		input.putString("requestKind", requestKind);
		String requestUri = "/remainz/service/top.html";
		input.putString("requestUri", requestUri);

		try {
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains(
					new Mu().msg("msg.err.invalidRequestKind", requestUri, requestKind)));

			// DBをロールバックする
			testUtil.getDb().rollback();
		}
	}
}
