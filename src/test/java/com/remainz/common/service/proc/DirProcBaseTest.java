package com.remainz.common.service.proc;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.common.exception.BusinessRuleViolationException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.util.Mu;

public class DirProcBaseTest {

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
	void test01() throws Exception {

		// ディレクトリが存在しないパターン
		DirProcBase proc = new DirProcBase();
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		try {
			input.putString("procName", "com.remainz.common.service.proc.SqlFromFileProc");
			input.putString("dirPath", "nowhere");
			proc.doProc(input, output, input.getString("dirPath"));
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noDir", input.getString("dirPath")),
					e.getLocalizedMessage());
		}
	}

	@Test
	void test02() throws Exception {

		// ディレクトリではなくファイルを指定するパターン
		DirProcBase proc = new DirProcBase();
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		input.putString("procName", "com.remainz.common.service.proc.SqlFromFileProc");
		input.putString("dirPath", "src/test/resources/util/DbUtilTest/remainzNg.properties");
		proc.doProc(input, output, input.getString("dirPath"));
		assertNull(output.getDb());
	}

	private static final String RESOURCE_PATH = "src/test/resources/";

	@Test
	void test03() throws Exception {

		File createDir = new File(RESOURCE_PATH + "service/proc/DirProcBaseTest");
		createDir.mkdir();

		// 正常系
		DirProcBase proc = new DirProcBase();
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		input.putString("procName", "com.remainz.common.service.proc.SqlFromFileProc");
		input.putString("dirPath", RESOURCE_PATH);
		proc.doProc(input, output, input.getString("dirPath"));
		assertNull(output.getDb());

		createDir.delete();
	}
}
