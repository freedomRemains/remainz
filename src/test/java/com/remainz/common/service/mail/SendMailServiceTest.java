package com.remainz.common.service.mail;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.remainz.common.exception.BusinessRuleViolationException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.util.Mu;

public class SendMailServiceTest {

	@Test
	void test01() throws Exception {

		// 必須パラメータなしのパターン
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new SendMailService();
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "username"), e.getLocalizedMessage());
		}

		try {
			input.putString("username", "user");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "password"), e.getLocalizedMessage());
		}

		try {
			input.putString("password", "pass");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "subject"), e.getLocalizedMessage());
		}

		try {
			input.putString("subject", "件名");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "honbun"), e.getLocalizedMessage());
		}

		try {
			input.putString("honbun", "本文");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "to"), e.getLocalizedMessage());
		}
	}

	@Test
	void test02() throws Exception {

		// 正常系パターン
		var input = new GenericParam();
		input.putString("username", "user");
		input.putString("password", "pass");
		input.putString("subject", "件名");
		input.putString("honbun", "本文");
		input.putString("to", "to@jc.com");
		var output = new GenericParam();
		var service = new SendMailService();
		service.doService(input, output);

		// メール送信は現状、特段outputに何かを設定していない(assertは適当)
		assertNotNull(output);
	}

	@Test
	void test03() throws Exception {

		// メール送信時に例外が起きるパターン
		var input = new GenericParam();
		input.putString("username", "user");
		input.putString("password", "pass");
		input.putString("subject", "件名");
		input.putString("honbun", "本文");
		input.putString("to", "to@jc.com");
		input.putString("mail.smtp.host", "http://nowhere.com");
		input.putString("mail.smtp.port", "9999");
		var output = new GenericParam();
		var service = new SendMailService();

		try {
			service.doService(input, output);
			fail();
		} catch (Exception e) {
			assertTrue(e.getLocalizedMessage().contains("http://nowhere.com"));
		}
	}
}
