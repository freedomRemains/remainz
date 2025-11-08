package com.remainz.common.service.proc;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.TestUtil;
import com.remainz.common.param.GenericParam;
import com.remainz.common.util.FileUtil;

public class CreateSqlByDefProcTest {

	private static String dbName;

	@BeforeEach
	void beforeEach() {

		// テストに必要なフォルダを作成する
		dbName = TestUtil.getDbName();
		new FileUtil().createDirIfNotExists(TestUtil.OUTPUT_PATH + "dbmng/" + dbName + "/10_dbdef/20_auto_created");
		new FileUtil().createDirIfNotExists(TestUtil.OUTPUT_PATH + "dbmng/" + dbName + "/30_sql/20_auto_created");	
	}

	@AfterEach
	void afterEach() {

		// テストフォルダを削除する
		TestUtil.clearOutputDir();
	}

	//
	// 各テストメソッドはstatic、private禁止、戻り値も返却してはならない
	//

	@Test
	void test01() throws Exception {

		// 入力パラメータを作成する
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";

		// サイズが0のファイルを配置
		String filePath = dirPath + "/" + defPath + "/test.txt";
		try (var file = new FileUtil().getBufferedOutputStream(filePath)) {

			// カバレッジ(ファイルサイズが0のファイルを処理)
			var proc = new CreateSqlByDefProc();
			var input = new GenericParam();
			var output = new GenericParam();
			input.setDb(TestUtil.getDb());
			input.putString("dirPath", dirPath);
			input.putString("defPath", defPath);
			input.putString("sqlPath", sqlPath);
			proc.doProc(input, output, input.getString("dirPath"));

			// 処理したファイルについて確認を行う
			File blankFile = new File(filePath);
			assertTrue(blankFile.exists());
			assertEquals(0, blankFile.length());
		}
	}

	@Test
	void test02() throws Exception {

		// 入力パラメータを作成する
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";

		// ".keep"ファイルを配置
		String filePath = dirPath + "/" + defPath + "/.keep";
		try (var file = new FileUtil().getBufferedOutputStream(filePath)) {

			// カバレッジ(".keep"ファイルを処理)
			var proc = new CreateSqlByDefProc();
			var input = new GenericParam();
			var output = new GenericParam();
			input.setDb(TestUtil.getDb());
			input.putString("dirPath", dirPath);
			input.putString("defPath", defPath);
			input.putString("sqlPath", sqlPath);
			proc.doProc(input, output, input.getString("dirPath"));

			// 処理したファイルについて確認を行う
			File blankFile = new File(filePath);
			assertTrue(blankFile.exists());
			assertEquals(0, blankFile.length());
		}
	}

	@Test
	void test03() throws Exception {

		// 入力パラメータを作成する
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";

		// "tableNameList.txt"ファイルを配置
		String filePath = dirPath + "/" + defPath + "/tableNameList.txt";
		try (var file = new FileUtil().getBufferedOutputStream(filePath)) {

			// カバレッジ("tableNameList.txt"ファイルを処理)
			var proc = new CreateSqlByDefProc();
			var input = new GenericParam();
			var output = new GenericParam();
			input.setDb(TestUtil.getDb());
			input.putString("dirPath", dirPath);
			input.putString("defPath", defPath);
			input.putString("sqlPath", sqlPath);
			proc.doProc(input, output, input.getString("dirPath"));

			// 処理したファイルについて確認を行う
			File blankFile = new File(filePath);
			assertTrue(blankFile.exists());
			assertEquals(0, blankFile.length());
		}
	}
}
