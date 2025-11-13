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

public class CreatePageServiceTest {

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
		var service = new CreatePageService();
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
			assertEquals(new Mu().msg("msg.common.noParam", "accountId"), e.getLocalizedMessage());
		}

		try {
			input.putString("accountId", "data_loader");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "tableName"), e.getLocalizedMessage());
		}

		try {
			input.putString("tableName", "HTML_PAGE");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "PAGE_NAME"), e.getLocalizedMessage());
		}

		try {
			input.putString("PAGE_NAME", "新規パーツ追加");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "URI_PATTERN"), e.getLocalizedMessage());
		}
	}

	@Test
	void test02() throws SQLException {

		// 正常系
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new CreatePageService();
		input.setDb(testUtil.getDb());
		input.putString("accountId", "data_loader");
		input.putString("tableName", "HTML_PAGE");
		input.putString("PAGE_NAME", "新規パーツ追加");
		input.putString("URI_PATTERN", "/newHtmlParts");

		service.doService(input, output);
		var recordList = input.getDb().select("SELECT * FROM URI_PATTERN WHERE URI_PATTERN = '/newHtmlParts'");
		assertEquals(1, recordList.size());

		// DB更新をロールバックする
		input.getDb().rollback();
	}

	@Test
	void test03() throws SQLException {

		// カバレッジ(キー重複)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new CreatePageService();
		input.setDb(testUtil.getDb());
		input.putString("accountId", "data_loader");
		input.putString("tableName", "HTML_PAGE");
		input.putString("PAGE_NAME", "TOP");
		input.putString("URI_PATTERN", "/remainz/service/top.html");

		// サービスを実行する
		service.doService(input, output);

		// エラーメッセージキーを含むリダイレクトのレスポンスになっていることを確認する
		assertEquals("redirect", output.getString("respKind"));
		assertTrue(output.getString("destination").startsWith("editPage.html?errMsgKey="));

		// DB更新をロールバックする
		input.getDb().rollback();
	}

	@Test
	void test04() throws SQLException {

		// カバレッジ(予期せぬ例外)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new CreatePageService();
		input.setDb(testUtil.getDb());
		input.putString("accountId", "data_loader");
		input.putString("tableName", "HTML_PAGE");
		input.putString("PAGE_NAME", "新規パーツ追加");
		input.putString("URI_PATTERN", "/newHtmlParts");

		// LNKのレコードを消し、予期せぬ例外を発生させる
		input.getDb().update("DELETE FROM LNK");

		// サービスを実行する
		try {
			service.doService(input, output);
			fail();

		} catch (ApplicationInternalException e) {

			// エラーの内容が想定通りか確認する
			assertTrue(e.getMessage().contains("NumberFormatException"));
		}

		// DB更新をロールバックする
		input.getDb().rollback();
	}
}
