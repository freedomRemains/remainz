package com.remainz.common.service.adapter;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.common.param.GenericParam;

/**
 * 汎用パラメータのアダプターの機能を提供するクラスです。
 */
public class GenericAdapterTest {

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

		// パラメータなしで実行するパターン
		GenericAdapter adapter = new GenericAdapter();
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		adapter.doAdapt(input, output);
		assertNull(output.getDb());
	}

	@Test
	void test02() {

		// 既にoutputが持っているパラメータばかりで、何もコピーされないパターン
		GenericAdapter adapter = new GenericAdapter();
		GenericParam input = new GenericParam();
		input.putString("keyString", "valueString");
		input.putStringArray("keyStringArray", new String[] {"valueStringArray"});
		input.putRecordList("keyRecordList", new ArrayList<LinkedHashMap<String, String>>());
		GenericParam output = new GenericParam();
		output.putString("keyString", "valueString");
		output.putStringArray("keyStringArray", new String[] {"valueStringArray"});
		output.putRecordList("keyRecordList", new ArrayList<LinkedHashMap<String, String>>());
		adapter.doAdapt(input, output);
		assertEquals("valueString", output.getString("keyString"));
		assertEquals(1, output.getStringArray("keyStringArray").length);
		assertEquals(0, output.getRecordList("keyRecordList").size());
	}

	@Test
	void test03() {

		// 正常系パターン
		GenericAdapter adapter = new GenericAdapter();
		GenericParam input = new GenericParam();
		input.putString("keyString", "valueString");
		input.putStringArray("keyStringArray", new String[] {"valueStringArray"});
		input.putRecordList("keyRecordList", new ArrayList<LinkedHashMap<String, String>>());
		GenericParam output = new GenericParam();
		adapter.doAdapt(input, output);
		assertEquals("valueString", output.getString("keyString"));
		assertEquals(1, output.getStringArray("keyStringArray").length);
		assertEquals(0, output.getRecordList("keyRecordList").size());
	}
}
