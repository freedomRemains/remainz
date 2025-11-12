package com.remainz.common.service.proc;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.TestUtil;
import com.remainz.common.param.GenericParam;
import com.remainz.common.util.FileUtil;

public class CreateSqlByDataProcTest {

	private String dbName;

	private TestUtil testUtil;

	@BeforeEach
	void beforeEach() throws Exception {

		// DB接続を取得し、トランザクションを開始する
		testUtil = new TestUtil();
		testUtil.getDb();

		// DB名を取得する
		dbName = testUtil.getDbName();

		// テストに必要なフォルダを作成する
		new FileUtil().createDirIfNotExists(TestUtil.OUTPUT_PATH + "dbmng/" + dbName + "/20_dbdata/20_auto_created");
		new FileUtil().createDirIfNotExists(TestUtil.OUTPUT_PATH + "dbmng/" + dbName + "/30_sql/20_auto_created");	
	}

	@AfterEach
	void afterEach() throws Exception {

		// 必ず最後にロールバックし、DBをクローズする
		testUtil.getDb().rollback();
		testUtil.closeDb();

		// テストフォルダを削除する
		testUtil.clearOutputDir();
	}

	//
	// 各テストメソッドはstatic、private禁止、戻り値も返却してはならない
	//

	@Test
	void test01() throws Exception {

		// 入力パラメータを作成する
		String dirPath = TestUtil.OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String dataPath = "20_dbdata/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";

		// サイズが0のファイルを配置
		String filePath = dirPath + "/" + dataPath + "/test.txt";
		try (var file = new FileUtil().getBufferedOutputStream(filePath)) {

			// カバレッジ(ファイルサイズが0のファイルを処理)
			var proc = new CreateSqlByDataProc();
			var input = new GenericParam();
			var output = new GenericParam();
			input.setDb(testUtil.getDb());
			input.putString("dirPath", dirPath);
			input.putString("defPath", defPath);
			input.putString("dataPath", dataPath);
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
		String dataPath = "20_dbdata/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";

		// ".keep"ファイルを配置
		String filePath = dirPath + "/" + dataPath + "/.keep";
		try (var file = new FileUtil().getBufferedOutputStream(filePath)) {

			// カバレッジ(".keep"ファイルを処理)
			var proc = new CreateSqlByDataProc();
			var input = new GenericParam();
			var output = new GenericParam();
			input.setDb(testUtil.getDb());
			input.putString("dirPath", dirPath);
			input.putString("defPath", defPath);
			input.putString("dataPath", dataPath);
			input.putString("sqlPath", sqlPath);
			proc.doProc(input, output, input.getString("dirPath"));

			// 処理したファイルについて確認を行う
			File blankFile = new File(filePath);
			assertTrue(blankFile.exists());
			assertEquals(0, blankFile.length());
		}
	}
}
