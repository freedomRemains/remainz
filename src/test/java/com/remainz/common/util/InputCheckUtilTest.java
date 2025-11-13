package com.remainz.common.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.TestUtil;
import com.remainz.common.exception.BusinessRuleViolationException;
import com.remainz.common.param.GenericParam;

//
//テストクラスはprivateを付けてはならず、publicも通常は付けない。
//
class InputCheckUtilTest {

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
	}

	//
	// 各テストメソッドはstatic、private禁止、戻り値も返却してはならない
	//

	@Test
	void test01() {

		// inputエラーの検証
		try {
			new InputCheckUtil().checkDb(null);
			fail();

		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "input"), e.getMessage());
		}
	}

	@Test
	void test02() {

		// DBチェックエラーの検証
		try {
			new InputCheckUtil().checkDb(new GenericParam());
			fail();

		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "db"), e.getMessage());
		}
	}

	@Test
	void test03() {

		// paramチェックエラーの検証
		String key = "notExistKey";
		try {
			new InputCheckUtil().checkParam(new GenericParam(), key);
			fail();

		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", key), e.getMessage());
		}
	}

	@Test
	void test04() {

		// paramチェックエラーの検証
		String key = "notExistKey";
		try {
			GenericParam genericParam = new GenericParam();
			genericParam.putString(key, "");
			new InputCheckUtil().checkParam(genericParam, key);
			fail();

		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", key), e.getMessage());
		}
	}

	@Test
	void test05() {

		// 正常系の検証
		String key = "key";
		try {
			GenericParam genericParam = new GenericParam();
			genericParam.setDb(testUtil.getDb());
			genericParam.putString(key, "value");
			genericParam.putStringArray("arrayKey", new String[] {"test", "test2"});
			InputCheckUtil inputCheckUtil = new InputCheckUtil();
			inputCheckUtil.checkDb(genericParam);
			inputCheckUtil.checkParam(genericParam, key);
			inputCheckUtil.checkArrayParam(genericParam, "arrayKey");

		} catch (BusinessRuleViolationException e) {
			fail();
		}
	}

	@Test
	void test06() {

		// arrayParamチェックエラーの検証(キーに対応する文字列配列が存在しない場合)
		String key = "notExistArrayKey";
		try {
			GenericParam genericParam = new GenericParam();
			genericParam.putString(key, "");
			new InputCheckUtil().checkArrayParam(genericParam, key);
			fail();

		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", key), e.getMessage());
		}
	}

	@Test
	void test07() {

		// arrayParamチェックエラーの検証(キーに対応する文字列配列が存在しない場合)
		String key = "arrayKey";
		try {
			GenericParam genericParam = new GenericParam();
			genericParam.putStringArray(key, new String[] {});
			new InputCheckUtil().checkArrayParam(genericParam, key);
			fail();

		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", key), e.getMessage());
		}
	}
}
