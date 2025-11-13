package com.remainz.web.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * ロール制約例外
 */
public class RoleRestrictionExceptionTest {

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

		// 例外クラスを引数とするパターンのテスト
		try {
			throw new RoleRestrictionException(new Exception("一般例外"));
		} catch (RuntimeException e) {
			assertEquals("java.lang.Exception: 一般例外", e.getMessage());
		}
	}

	@Test
	void test02() {

		// 文字列を引数とするパターンのテスト
		try {
			throw new RoleRestrictionException("文字列例外");
		} catch (RuntimeException e) {
			assertEquals("文字列例外", e.getMessage());
		}
	}

}
