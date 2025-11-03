package com.remainz.common.service.proc;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.common.param.GenericParam;

public class TextFileProcBaseTest {

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

		// ファイルが存在しないパターン
		TextFileProcBase proc = new TextFileProcBase();
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		try {
			input.putString("filePath", "nowhere");
			proc.doProc(input, output, input.getString("filePath"));
			fail();
		} catch (FileNotFoundException e) {
			assertTrue(e.getLocalizedMessage().contains("nowhere"));
		}
	}

	private static final String RESOURCE_PATH = "src/test/resources/";

	@Test
	void test02() throws Exception {

		// ファイルではなくディレクトリを指定するパターン
		TextFileProcBase proc = new TextFileProcBase();
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		try {
			input.putString("filePath", RESOURCE_PATH);
			proc.doProc(input, output, input.getString("filePath"));
			fail();
		} catch (FileNotFoundException e) {
			assertTrue(e.getLocalizedMessage().contains("test"));
		}
	}

	@Test
	void test03() throws Exception {

		// 正常系
		TextFileProcBase proc = new TextFileProcBase();
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		input.putString("filePath", RESOURCE_PATH + "service/proc/TextFileProcBaseTest/test03.txt");
		proc.doProc(input, output, input.getString("filePath"));
		assertNull(output.getDb());
	}
}
