package com.remainz.common.service.dbmng.common;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.junit.jupiter.api.Test;

import com.remainz.TestUtil;
import com.remainz.common.exception.BusinessRuleViolationException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.util.Mu;

/**
 * ファイルとして用意されているselectのSQLを実行するサービスです。
 */
public class DbSelectBySqlFileServiceTest {

	//
	// 各テストメソッドはstatic、private禁止、戻り値も返却してはならない
	//

	private static final String TEST_RESOURCE_PATH = TestUtil.RESOURCE_PATH + "service/dbmng/common/DbSelectBySqlFileServiceTest/";

	@Test
	void test01() throws Exception {

		// パラメータ指定なしパターン
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		DbSelectBySqlFileService service = new DbSelectBySqlFileService();
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
			assertEquals(new Mu().msg("msg.common.noParam", "sqlFilePath"), e.getLocalizedMessage());
		}

		try {
			input.putString("sqlFilePath", TEST_RESOURCE_PATH + "10_ng.sql");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "recordListKey"), e.getLocalizedMessage());
		}
	}

	@Test
	void test02() throws Exception {

		// SQLエラーパターン
		GenericParam input = new GenericParam();
		input.setDb(TestUtil.getDb());
		input.putString("sqlFilePath", TEST_RESOURCE_PATH + "10_ng.sql");
		input.putString("recordListKey", "selectResult");
		GenericParam output = new GenericParam();
		DbSelectBySqlFileService service = new DbSelectBySqlFileService();
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
		input.setDb(TestUtil.getDb());
		input.putString("sqlFilePath", TEST_RESOURCE_PATH + "20_ok.sql");
		input.putString("recordListKey", "selectResult");
		GenericParam output = new GenericParam();
		DbSelectBySqlFileService service = new DbSelectBySqlFileService();

		service.doService(input, output);

		String sql = "SELECT * FROM SCR";
		ArrayList<LinkedHashMap<String, String>> recordList = input.getDb().select(sql);
		assertEquals(recordList.size(), output.getRecordList("selectResult").size());
	}
}
