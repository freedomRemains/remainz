package com.remainz.common.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.common.exception.ApplicationInternalException;

//
//テストクラスはprivateを付けてはならず、publicも通常は付けない。
//
class InnerClassPathPropTest {

	@BeforeEach
	void beforeEach() {

		//
		// テストメソッドはprivateを付けてはいけない。
		// 各テストを実施する前の開始処理を記述する。
		//
	}

	@AfterEach
	void afterEach() {

		//
		// テストメソッドはprivateを付けてはいけない。
		// 各テストを実施する前の開始処理を記述する。
		//
	}

	//
	// 各テストメソッドはstatic、private禁止、戻り値も返却してはならない
	//

	@Test
	void test01() {

		// プロパティファイル名がnullのケースを検証
		try {
			new InnerClassPathProp().init(null);
			fail();
		} catch (ApplicationInternalException e) {
			assertEquals("[No property file name]null", e.getMessage());
		}
	}

	@Test
	void test02() {

		// プロパティファイル名が空のケースを検証
		try {
			new InnerClassPathProp().init("");
			fail();
		} catch (ApplicationInternalException e) {
			assertEquals("[No property file name]", e.getMessage());
		}
	}

	@Test
	void test03() {

		// プロパティファイル名が存在しないケースを検証
		try {
			new InnerClassPathProp().init("notExist.properties");
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getMessage().startsWith("[Property file not found]notExist.properties"));
		}
	}

	@Test
	void test04() {

		// プロパティ値を正常に読み込めるケースを検証
		InnerClassPathProp innerClassPathProp = new InnerClassPathProp();
		innerClassPathProp.init("message.properties");
		assertEquals("message.properties", innerClassPathProp.getPropFileName());
		assertEquals("detected tableNameList on memory.", innerClassPathProp.msg("msg.detectedTableNameListOnMemory"));
		assertEquals("value already exists. [key]test [value]1", innerClassPathProp.msg("msg.warn.valueAlreadyExists", "test", Integer.valueOf(1)));
		assertEquals("value already exists. [key]test2 [value]2", innerClassPathProp.msg("msg.warn.valueAlreadyExists", "test2", Integer.parseInt("2")));
	}

	@Test
	void test05() {

		// 引数付きコンストラクタを検証
		InnerClassPathProp innerClassPathProp = new InnerClassPathProp("notExist.properties");
		assertThrows(NullPointerException.class, () -> innerClassPathProp.msg("msg.detectedTableNameListOnMemory"));
		assertEquals("detected tableNameList on memory.", new InnerClassPathProp("message.properties").msg("msg.detectedTableNameListOnMemory"));
	}
}
