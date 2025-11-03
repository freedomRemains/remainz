package com.remainz.common.service.mail;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.TestUtil;
import com.remainz.common.exception.BusinessRuleViolationException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.service.script.ScriptService;
import com.remainz.common.util.Mu;
import com.remainz.common.util.RcProp;

public class GetAllMailServiceTest {

	private String dbName;

	@BeforeEach
	void beforeEach() {

	}

	@AfterEach
	void afterEach() {

		// テストフォルダを削除する
		TestUtil.clearOutputDir();
	}

	@Test
	void test01() throws Exception {

		// 必須パラメータなしのパターン
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new GetAllMailService();
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "db"), e.getLocalizedMessage());
		}

		try {
			input.setDb(TestUtil.getDb());
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "mailStoreKind"), e.getLocalizedMessage());
		}

		try {
			input.putString("mailStoreKind", "imap");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "mailServer"), e.getLocalizedMessage());
		}

		try {
			input.putString("mailServer", "mail.ecosys.co.jp");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "mailAccount"), e.getLocalizedMessage());
		}

		try {
			input.putString("mailAccount", "user");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "mailPassword"), e.getLocalizedMessage());
		}

		try {
			input.putString("mailPassword", "pass");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "mailProcs"), e.getLocalizedMessage());
		}
	}

	@Test
	void test02() {

		// 正常系パターン

		// 全メール取得サービスを実行する
		//doGetAllMailService("user", "pass");

		// DB構成取得のスクリプトを実行し、データを取得する
		//doGetDbStructure(true);
	}

	/**
	 * メール取得
	 * scriptId : 1000003
	 * mailStoreKind : "imap"固定(現状固定だが、メールサーバによって変わりうる)
	 * mailServer : [メールサーバ](＜例＞mail.ecosys.co.jp)
	 * mailAccount : [メールアカウント]
	 * mailPassword : [メールパスワード]
	 */
	// このメソッドはprotectedを維持する(全メール取得はtest07のコメントを外したときのみ動かす)
	protected void doGetAllMailService(String account, String password) throws Exception {

		// 全メール取得サービスを実行する
		var input = new GenericParam();
		var prop = new RcProp();
		input.setDb(TestUtil.getDb());
		input.putString("mailStoreKind", prop.get("mail.storeKind"));
		input.putString("mailServer", prop.get("mail.server"));
		input.putString("mailAccount", account);
		input.putString("mailPassword", password);
		String[] mailProcs = new String[] {"com.remainz.common.service.proc.MailProcBase"};
		input.putStringArray("mailProcs", mailProcs);
		var output = new GenericParam();
		var service = new GetAllMailService();
		service.doService(input, output);
	}

	// このメソッドはprotectedを維持する(test07のコメントを外したときのみ動かせるよう)
	protected void doGetDbStructure(boolean useResourcePath) throws Exception {

		// resources配下を直接変更する場合は、 "dirPath" の設定を変える
		var input = createNormalInput();
		if (useResourcePath) {
			String dirPath = TestUtil.RESOURCE_PATH + "service/script/dbmng/" + dbName;
			input.putString("dirPath", dirPath);
		}

		// 正常系
		var output = new GenericParam();
		var service = new ScriptService();
		service.doService(input, output);
	}

	private GenericParam createNormalInput() {

		// 必要なパラメータを準備する
		String scriptId = "1000001";
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String dataPath = "20_dbdata/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";
		String getTableNameListSql = TestUtil.createGetTableNameListSql();
		String getTableDefSql = TestUtil.createGetTableDefSql();

		// 正常系動作確認に必要なパラメータを作成する
		GenericParam input = new GenericParam();
		input.setDb(TestUtil.getDb());
		input.putString("scriptId", scriptId);
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("dataPath", dataPath);
		input.putString("sqlPath", sqlPath);
		input.putString("getTableNameListSql", getTableNameListSql);
		input.putString("getTableDefSql", getTableDefSql);

		// 正常系動作確認に必要な入力パラメータを呼び出し側に返却する
		return input;
	}
}
