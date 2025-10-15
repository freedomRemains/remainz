package com.remainz.common.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

//
// テストクラスはprivateを付けてはならず、publicも通常は付けない。
//
class CuTest {

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

		// 文字列がnullのケースをテストする
		assertTrue(Cu.isEmpty(null));
	}

	@Test
	void test02() {

		// 文字列が空のケースをテストする
		assertTrue(Cu.isEmpty(""));
	}

	@Test
	void test03() {

		// 文字列が空でないケースをテストする
		assertFalse(new Cu().isEmptyString("abc"));
	}

	@Test
	void test04() {

		// 文字列が空でないケースをテストする
		assertTrue(Cu.isNotEmpty("abc"));
		assertTrue(new Cu().isNotEmptyString("abc"));
	}

	@Test
	void test05() {

		// ファイル名として使用できない文字列が変換されることをテストする
		assertEquals("￥／：＊？”＜＞｜？／＞：＊”＜｜￥",
				Cu.convertForFileName("\\/:*?\"<>|?/>:*\"<|\\"));
	}

	@Test
	void test06() throws Exception {

		// 制御文字が検出されることをテストする
		assertFalse(Cu.hasCtrlCode(new String(new char[] {(char)-1})));
		assertTrue(Cu.hasCtrlCode(new String(new char[] {0x00})));
		assertTrue(Cu.hasCtrlCode(new String(new char[] {0x1F})));
		assertTrue(Cu.hasCtrlCode(new String(new char[] {0x7F})));

		assertTrue(Cu.hasCtrlCode(new String(new char[] {'a', 0x00, 'b'})));
		assertTrue(Cu.hasCtrlCode(new String(new char[] {'c', 0x1F, 'd'})));
		assertTrue(Cu.hasCtrlCode(new String(new char[] {'e', 0x7F, 'f'})));
		assertFalse(Cu.hasCtrlCode(new String(new char[] {'g', 0x20, 'h'})));
		assertFalse(Cu.hasCtrlCode(new String(new char[] {'i', 0x7E, 'j'})));
		assertFalse(Cu.hasCtrlCode(new String(new char[] {'k', 0x80, 'l'})));

		assertFalse(Cu.hasCtrlCode(new String(new char[] {0x09})));
		assertFalse(Cu.hasCtrlCode(new String(new char[] {0x0A})));
		assertFalse(Cu.hasCtrlCode(new String(new char[] {0x0D})));
	}
}
