package com.remainz.web.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.TestUtil;

public class ErrMsgUtilTest {

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
	void test01() throws Exception {

		// 正常系
		String errMsgKey = new ErrMsgUtil().getErrMsgKey(testUtil.getDb(), "dummySessionId", "1000001", "1000401");
		assertEquals("1", errMsgKey);

		// 正常系(最大IDありパターンテストのため、2連続でメソッドを実行)
		errMsgKey = new ErrMsgUtil().getErrMsgKey(testUtil.getDb(), "dummySessionId", "1000001", "1000401");
		assertEquals("2", errMsgKey);
	}

	@Test
	void test02() throws Exception {

		// カバレッジ(SQLException)
		String errMsgKey = new ErrMsgUtil().getErrMsgKey(testUtil.getDb(), "dummySessionId", "1000001", "dummmy");
		assertEquals("0", errMsgKey);
	}

	@Test
	void test03() throws Exception {

		// カバレッジ(SQLException)
		String errMsgKey = new ErrMsgUtil().getErrMsgKeyByMsg(testUtil.getDb(), "dummySessionId", "dummyAccountId", "dummmy");
		assertEquals("0", errMsgKey);
	}
}
