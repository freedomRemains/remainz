package com.remainz.common.service.proc;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.common.param.GenericParam;

/**
 * ディレクトリ内のDBテーブル名を探すためのプロシージャです。
 */
public class TableNameSearchProcTest {

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
		TableNameSearchProc proc = new TableNameSearchProc();
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		input.putString("dirPath", RESOURCE_PATH + "service/proc/TableNameSearchProcTest");
		proc.doProc(input, output, input.getString("dirPath"));
		assertNull(output.getDb());
		assertEquals(5, output.getRecordList("tableNameList").size());
		assertEquals("ACCNT", output.getRecordList("tableNameList").get(0).get("Tables_in_db"));
		assertEquals("GNR_GRP", output.getRecordList("tableNameList").get(1).get("Tables_in_db"));
		assertEquals("GNR_KEY_VAL", output.getRecordList("tableNameList").get(2).get("Tables_in_db"));
		assertEquals("SCR", output.getRecordList("tableNameList").get(3).get("Tables_in_db"));
		assertEquals("SCR_ELM", output.getRecordList("tableNameList").get(4).get("Tables_in_db"));
	}
}
