package com.remainz.common;

import com.remainz.common.db.DbInterface;
import com.remainz.common.util.DbUtil;
import com.remainz.common.util.InnerClassPathProp;

public class TestUtil {

	public static final String RESOURCE_PATH = "src/test/resources/";

	public static final String OUTPUT_PATH = "output/";

	private static DbInterface db;

	public static DbInterface getDb() {

		// インスタンスがまだ生成されていない場合のみ生成する
		if (db == null) {
			db = new DbUtil().getDb(new InnerClassPathProp("rc.properties"));
		}

		// DBインスタンスを呼び出し側に返却する
		return db;
	}

	public static void closeDb() throws Exception {

		// DBをクローズし、変数もnullとする
		if (db != null) {
			db.close();
			db = null;
		}
	}
}
