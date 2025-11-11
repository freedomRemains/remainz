package com.remainz.common.service.dbmng.common;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.TestUtil;
import com.remainz.common.exception.BusinessRuleViolationException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.util.Mu;

/**
 * ファイルとして用意されているupdateのSQLを実行するサービスです。
 */
public class DbUpdateBySqlFileServiceTest {

	//
	// 各テストメソッドはstatic、private禁止、戻り値も返却してはならない
	//

	private static final String TEST_RESOURCE_PATH = TestUtil.RESOURCE_PATH + "service/dbmng/common/DbUpdateBySqlFileServiceTest/";

	private TestUtil testUtil;

	@BeforeEach
	void beforeEach() {

		// DB接続を取得し、トランザクションを開始する
		testUtil = new TestUtil();
		testUtil.getDb();
	}

	@AfterEach
	void afterEach() throws Exception {

		// 必ず最後にロールバックし、DBをクローズする
		testUtil.getDb().rollback();
		testUtil.closeDb();
	}

	@Test
	void test01() throws Exception {

		// パラメータ指定なしパターン
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		DbUpdateBySqlFileService service = new DbUpdateBySqlFileService();
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "db"), e.getLocalizedMessage());
		}

		try {
			input.setDb(testUtil.getDb());
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "sqlFilePath"), e.getLocalizedMessage());
		}

		try {
			input.setDb(testUtil.getDb());
			input.putString("sqlFilePath", TEST_RESOURCE_PATH + "10_ng.sql");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "resultKey"), e.getLocalizedMessage());
		}
	}

	@Test
	void test02() throws Exception {

		// SQLエラーパターン
		GenericParam input = new GenericParam();
		input.setDb(testUtil.getDb());
		input.putString("sqlFilePath", TEST_RESOURCE_PATH + "10_ng.sql");
		input.putString("resultKey", "updateResult");
		GenericParam output = new GenericParam();
		DbUpdateBySqlFileService service = new DbUpdateBySqlFileService();
		try {
			service.doService(input, output);
			fail();
		} catch (SQLSyntaxErrorException e) {
			assertTrue(e.getLocalizedMessage().contains("NOT_EXIST_TABLE"));
		}
	}

	@Test
	void test03() throws Exception {

		// 正常系パターン
		GenericParam input = new GenericParam();
		input.setDb(testUtil.getDb());
		input.putString("sqlFilePath", TEST_RESOURCE_PATH + "20_ok.sql");
		input.putString("resultKey", "updateResult");
		GenericParam output = new GenericParam();
		DbUpdateBySqlFileService service = new DbUpdateBySqlFileService();

		String sql = "SELECT * FROM GNR_GRP WHERE GNR_GRP_ID = 1000002";
		service.doService(input, output);

		ArrayList<LinkedHashMap<String, String>> recordList = input.getDb().select(sql);
		assertEquals(1, recordList.size());
		input.getDb().rollback();
		recordList = input.getDb().select(sql);
		assertEquals(0, recordList.size());
	}
}
