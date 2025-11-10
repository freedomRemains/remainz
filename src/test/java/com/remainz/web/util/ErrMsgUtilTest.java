package com.remainz.web.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.TestUtil;

public class ErrMsgUtilTest {

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
	void test01() throws Exception {

		// 正常系
		String errMsgKey = new ErrMsgUtil().getErrMsgKey(TestUtil.getDb(), "dummySessionId", "1000001", "1000401");
		assertEquals("1", errMsgKey);

		// 正常系(最大IDありパターンテストのため、2連続でメソッドを実行)
		errMsgKey = new ErrMsgUtil().getErrMsgKey(TestUtil.getDb(), "dummySessionId", "1000001", "1000401");
		assertEquals("2", errMsgKey);
	}

	@Test
	void test02() throws Exception {

		// カバレッジ(SQLException)
		String errMsgKey = new ErrMsgUtil().getErrMsgKey(TestUtil.getDb(), "dummySessionId", "1000001", "dummmy");
		assertEquals("0", errMsgKey);
	}

	@Test
	void test03() throws Exception {

		// カバレッジ(SQLException)
		String errMsgKey = new ErrMsgUtil().getErrMsgKeyByMsg(TestUtil.getDb(), "dummySessionId", "dummyAccountId", "dummmy");
		assertEquals("0", errMsgKey);
	}
}
