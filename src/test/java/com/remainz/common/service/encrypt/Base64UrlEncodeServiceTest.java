package com.remainz.common.service.encrypt;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.common.exception.BusinessRuleViolationException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.util.Mu;

public class Base64UrlEncodeServiceTest {

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
		var service = new Base64UrlEncodeService();
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
		input.putString("target", target);

		// 正常系(エンコードとデコードの整合性が取れていることを確認する)
		var output = new GenericParam();
		var service = new Base64UrlEncodeService();
		service.doService(input, output);
		assertByDecodeString(target, output.getString("target"));
	}

	@Test
	void test03() {

		// 入力パラメータを準備する
		String target = "全角文字を元に戻せることを確認するテスト。";
		var input = new GenericParam();
		input.putString("target", target);

		// カバレッジ(charset指定あり)
		input.putString("charset", "UTF-8");
		var output = new GenericParam();
		var service = new Base64UrlEncodeService();
		service.doService(input, output);
		assertByDecodeString(target, output.getString("target"));
	}

	private void assertByDecodeString(String target, String encodeString) {

		// デコードを行う
		var input = new GenericParam();
		input.putString("target", encodeString);
		var output = new GenericParam();
		var service = new Base64UrlDecodeService();
		service.doService(input, output);

		// デコード後の文字列が引数と一致することを確認する
		assertEquals(target, output.getString("target"));
	}
}
