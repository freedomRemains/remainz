package com.remainz.common.service.encrypt;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.common.exception.BusinessRuleViolationException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.util.Mu;

public class Base64UrlDecodeServiceTest {

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

		// 必須パラメータなしのパターン
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new Base64UrlDecodeService();
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "target"), e.getLocalizedMessage());
		}
	}

	@Test
	void test02() {

		// 入力パラメータを準備する
		String target = "全角文字を元に戻せることを確認するテスト。";
		var input = new GenericParam();
		input.putString("target", getEncodString(target));

		// 正常系(エンコードとデコードの整合性が取れていることを確認する)
		var output = new GenericParam();
		var service = new Base64UrlDecodeService();
		service.doService(input, output);
		assertEquals(target, output.getString("target"));
	}

	@Test
	void test03() {

		// 入力パラメータを準備する
		String target = "全角文字を元に戻せることを確認するテスト。";
		var input = new GenericParam();
		input.putString("target", getEncodString(target));

		// カバレッジ(charset指定あり)
		input.putString("charset", "UTF-8");
		var output = new GenericParam();
		var service = new Base64UrlDecodeService();
		service.doService(input, output);
		assertEquals(target, output.getString("target"));
	}

	private String getEncodString(String target) {

		// エンコードした文字列を呼び出し側に返却する
		var input = new GenericParam();
		input.putString("target", target);
		var output = new GenericParam();
		var service = new Base64UrlEncodeService();
		service.doService(input, output);
		return output.getString("target");
	}
}
