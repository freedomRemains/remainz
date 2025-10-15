package com.remainz.common.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.remainz.common.db.DbInterface;
import com.remainz.common.exception.ApplicationInternalException;

//
//テストクラスはprivateを付けてはならず、publicも通常は付けない。
//
class DbUtilTest {

	//
	// 各テストメソッドはstatic、private禁止、戻り値も返却してはならない
	//

	@Test
	void test01() throws Exception {

		// getDbのテストを実施
		DbInterface db = new DbUtil().getDb(new InnerClassPathProp("rc.properties"));
		assertNotNull(db);
		db.close();
	}

	@Test
	void test02() {

		// 接続情報が書かれていないプロパティファイルを指定し、エラーとなるテストを実施
		try {
			new DbUtil().getDb(new InnerClassPathProp("util/DbUtilTest/jlNg.properties"));
			fail();
			
		} catch (ApplicationInternalException e) {

			// WindowsとLinuxでメッセージが異なるため、空でなければOKと判断する
			assertTrue(!Cu.isEmpty(e.getMessage()));
		}
	}
}
