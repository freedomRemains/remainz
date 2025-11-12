package com.remainz.common.param;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.TestUtil;
import com.remainz.common.db.GenericDb;

//
//テストクラスはprivateを付けてはならず、publicも通常は付けない。
//
class GenericParamTest {

	/** ロガー */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private TestUtil testUtil;

	private 
	@BeforeEach
	void beforeEach() throws Exception {

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

	//
	// 各テストメソッドはstatic、private禁止、戻り値も返却してはならない
	//

	@Test
	void test01() {

		// setterとgetterのテスト(DB)
		GenericParam genericParam = new GenericParam();
		assertNull(genericParam.getDb());
		genericParam.setDb(new GenericDb());
		assertNotNull(genericParam.getDb());
	}

	@Test
	void test02() {

		// getStringとputStringのテスト
		GenericParam genericParam = new GenericParam();
		assertNull(genericParam.getString("abc"));
		genericParam.putString("abc", "def");
		assertEquals("def", genericParam.getString("abc"));
	}

	@Test
	void test03() {

		// addStringのテスト
		GenericParam genericParam = new GenericParam();
		String lineSepr = System.lineSeparator();
		genericParam.putString("abc", "def");
		assertEquals("def", genericParam.getString("abc"));
		genericParam.addString("abc", "ghi");
		assertEquals("def" + lineSepr + "ghi", genericParam.getString("abc"));
	}

	@Test
	void test04() {

		// getStringMapKeySetのテスト
		GenericParam genericParam = new GenericParam();
		genericParam.putString("abc", "ABC");
		genericParam.putString("def", "DEF");
		assertEquals(2, genericParam.getStringMapKeySet().size());
		Iterator<String> itrString = genericParam.getStringMapKeySet().iterator();
		assertEquals("abc", itrString.next());
		assertEquals("def", itrString.next());
		assertFalse(itrString.hasNext());
	}

	@Test
	void test05() {

		// putStringArrayとgetStringArrayのテスト
		GenericParam genericParam = new GenericParam();
		String[] stringArray1 = new String[] {"1", "2"};
		String[] stringArray2 = new String[] {"3", "4"};
		genericParam.putStringArray("ghi", stringArray1);
		genericParam.putStringArray("jkl", stringArray2);
		assertEquals(2, genericParam.getStringArray("ghi").length);
		assertEquals("1", genericParam.getStringArray("ghi")[0]);
		assertEquals("2", genericParam.getStringArray("ghi")[1]);
		assertEquals(2, genericParam.getStringArray("jkl").length);
		assertEquals("3", genericParam.getStringArray("jkl")[0]);
		assertEquals("4", genericParam.getStringArray("jkl")[1]);

		// getStringArrayMapKeySetのテスト
		assertEquals(2, genericParam.getStringArrayMapKeySet().size());
		Iterator<String> itrStringArray = genericParam.getStringArrayMapKeySet().iterator();
		assertEquals("ghi", itrStringArray.next());
		assertEquals("jkl", itrStringArray.next());
		assertFalse(itrStringArray.hasNext());
	}

	@Test
	void test06() {

		// getRecordListとputRecordListのテスト
		GenericParam genericParam = new GenericParam();
		ArrayList<LinkedHashMap<String, String>> recordList1 = new ArrayList<LinkedHashMap<String, String>>();
		LinkedHashMap<String, String> map1 = new LinkedHashMap<String, String>();
		map1.put("a", "1");
		map1.put("b", "2");
		recordList1.add(map1);
		LinkedHashMap<String, String> map2 = new LinkedHashMap<String, String>();
		map2.put("c", "3");
		map2.put("d", "4");
		recordList1.add(map2);
		genericParam.putRecordList("recordList1", recordList1);
		ArrayList<LinkedHashMap<String, String>> recordList2 = new ArrayList<LinkedHashMap<String, String>>();
		LinkedHashMap<String, String> map3 = new LinkedHashMap<String, String>();
		map3.put("e", "5");
		map3.put("f", "6");
		recordList2.add(map3);
		LinkedHashMap<String, String> map4 = new LinkedHashMap<String, String>();
		map4.put("g", "7");
		map4.put("h", "8");
		recordList2.add(map4);
		genericParam.putRecordList("recordList2", recordList2);
		assertEquals(2, genericParam.getRecordList("recordList1").size());
		assertEquals("1", genericParam.getRecordList("recordList1").get(0).get("a"));
		assertEquals("2", genericParam.getRecordList("recordList1").get(0).get("b"));
		assertEquals("3", genericParam.getRecordList("recordList1").get(1).get("c"));
		assertEquals("4", genericParam.getRecordList("recordList1").get(1).get("d"));
		assertEquals("5", genericParam.getRecordList("recordList2").get(0).get("e"));
		assertEquals("6", genericParam.getRecordList("recordList2").get(0).get("f"));
		assertEquals("7", genericParam.getRecordList("recordList2").get(1).get("g"));
		assertEquals("8", genericParam.getRecordList("recordList2").get(1).get("h"));

		// getRecordListMapKeySetのテスト
		Iterator<String> itrRecordList = genericParam.getRecordListMapKeySet().iterator();
		assertEquals("recordList1", itrRecordList.next());
		assertEquals("recordList2", itrRecordList.next());
		assertFalse(itrRecordList.hasNext());
	}

	@Test
	void test07() {

		// addStringの特殊ルートテスト
		GenericParam genericParam = new GenericParam();
		String lineSepr = System.lineSeparator();
		assertNull(genericParam.getString("abc"));
		genericParam.addString("abc", null);
		genericParam.addString("abc", "1");
		genericParam.addString("def", "");
		genericParam.addString("def", "2");
		genericParam.addString("ghi", "3");
		genericParam.addString("ghi", "4");
		assertEquals("1", genericParam.getString("abc"));
		assertEquals("2", genericParam.getString("def"));
		assertEquals("3" + lineSepr + "4", genericParam.getString("ghi"));
	}

	@Test
	void test08() throws SQLException {

		// recordLogのテスト(パラメータなし)
		var genericParam = new GenericParam();
		genericParam.recordLog(logger, "test");

		// recordLogのテスト(パラメータあり、1つ)
		genericParam.putString("key", "value");
		genericParam.putStringArray("key", new String[] {"testArray1", "testArray2"});
		var recordList = testUtil.getDb().select("SELECT * FROM TBL_DEF");
		genericParam.putRecordList("key", recordList);
		genericParam.recordLog(logger, "test");

		// recordLogのテスト(パラメータあり、2つ以上、パスワードあり)
		genericParam.putString("key2", "value2");
		genericParam.putStringArray("key2", new String[] {"testArray2_1", "testArray2_2"});
		var recordList2 = testUtil.getDb().select("SELECT * FROM ACCNT");
		genericParam.putRecordList("key2", recordList2);
		genericParam.recordLog(logger, "test");
	}

	@Test
	void test09() throws SQLException {

		// カバレッジ(putStringIfNotExists)
		var genericParam = new GenericParam();
		genericParam.putStringIfNotExists("key", "value");
		genericParam.putStringIfNotExists("key", "value2");
		assertEquals("value", genericParam.getString("key"));
	}
}
