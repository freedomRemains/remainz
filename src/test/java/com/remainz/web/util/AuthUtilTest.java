package com.remainz.web.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.TestUtil;

public class AuthUtilTest {

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

		// 存在しないアカウントID
		var authList = new AuthUtil().getAuthByAccountId(TestUtil.getDb(), "1");
		assertEquals(0, authList.size());
	}

	@Test
	void test02() throws Exception {

		// 存在するアカウントID
		var authList = new AuthUtil().getAuthByAccountId(TestUtil.getDb(), "1000201");
		assertEquals("1000001", authList.get(0).get("HTML_PARTS_ID"));
		assertEquals("read", authList.get(0).get("AUTH_KIND"));
		assertEquals("1000101", authList.get(1).get("HTML_PARTS_ID"));
		assertEquals("read", authList.get(1).get("AUTH_KIND"));
		assertEquals("1000201", authList.get(2).get("HTML_PARTS_ID"));
		assertEquals("edit", authList.get(2).get("AUTH_KIND"));
		assertEquals("1000701", authList.get(3).get("HTML_PARTS_ID"));
		assertEquals("read", authList.get(3).get("AUTH_KIND"));
	}

	@Test
	void test03() throws Exception {

		// 権限を持っている判定
		var authUtil = new AuthUtil();
		var authList = authUtil.getAuthByAccountId(TestUtil.getDb(), "1000201");
		assertTrue(authUtil.hasAuth("1000001", authList));

		// 権限を持っていない判定
		authList = authUtil.getAuthByAccountId(TestUtil.getDb(), "1000201");
		assertFalse(authUtil.hasAuth("1000301", authList));
	}

	@Test
	void test04() throws Exception {
		
		// 権限を持っている判定
		var authUtil = new AuthUtil();
		var authList = authUtil.getAuthByAccountId(TestUtil.getDb(), "1000201");
		assertTrue(authUtil.hasReadAuth("1000001", authList));

		// 権限を持っていない判定
		authList = authUtil.getAuthByAccountId(TestUtil.getDb(), "1000201");
		assertFalse(authUtil.hasReadAuth("1000201", authList));
	}

	@Test
	void test05() throws Exception {

		// 権限を持っている判定
		var authUtil = new AuthUtil();
		var authList = authUtil.getAuthByAccountId(TestUtil.getDb(), "1000201");
		assertTrue(authUtil.hasEditAuth("1000201", authList));

		// 権限を持っていない判定
		authList = authUtil.getAuthByAccountId(TestUtil.getDb(), "1000201");
		assertFalse(authUtil.hasEditAuth("1000001", authList));
	}

	@Test
	void test06() throws Exception {

		// 存在しないアカウントID
		var roleList = new AuthUtil().getRoleByAccountId(TestUtil.getDb(), "1");
		assertEquals(0, roleList.size());
	}

	@Test
	void test07() throws Exception {

		// 存在するアカウントID
		var roleList = new AuthUtil().getRoleByAccountId(TestUtil.getDb(), "1000201");
		assertEquals("1000201", roleList.get(0).get("APROLE_ID"));
	}

	@Test
	void test08() throws Exception {

		// ロールを持っている判定
		var authUtil = new AuthUtil();
		var roleList = authUtil.getRoleByAccountId(TestUtil.getDb(), "1000201");
		assertTrue(authUtil.hasRole("1000201", roleList));

		// ロールを持っていない判定
		roleList = authUtil.getRoleByAccountId(TestUtil.getDb(), "1000201");
		assertFalse(authUtil.hasRole("1000001", roleList));
	}
}
