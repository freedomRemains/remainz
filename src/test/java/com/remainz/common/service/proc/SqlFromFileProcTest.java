package com.remainz.common.service.proc;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.common.param.GenericParam;

/**
 * SQLを記述したテキストファイルからSQLを抽出するためのプロシージャです。
 */
public class SqlFromFileProcTest {

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

	private static final String RESOURCE_PATH = "src/test/resources/";

	@Test
	void test01() throws Exception {

		// 正常系
		SqlFromFileProc proc = new SqlFromFileProc();
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		input.putString("filePath", RESOURCE_PATH + "service/fs/TextFileProcedureServiceTest/10_dropTable.sql");
		proc.doProc(input, output, input.getString("filePath"));
		assertNull(output.getDb());
		assertTrue(output.getString("sqlFromFile").contains("DROP TABLE IF EXISTS JLDB.GNR_GRP;"));
		assertTrue(output.getString("sqlFromFile").contains("DROP TABLE IF EXISTS JLDB.GNR_KEY_VAL;"));
		assertTrue(output.getString("sqlFromFile").contains("DROP TABLE IF EXISTS JLDB.ACCNT;"));
		assertTrue(output.getString("sqlFromFile").contains("DROP TABLE IF EXISTS JLDB.SCR;"));
		assertTrue(output.getString("sqlFromFile").contains("DROP TABLE IF EXISTS JLDB.SCR_ELM;"));
	}
}
