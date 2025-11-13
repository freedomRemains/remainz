package com.remainz.common.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.apache.log4j.PropertyConfigurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.common.exception.ApplicationInternalException;

//
//テストクラスはprivateを付けてはならず、publicも通常は付けない。
//
class LogUtilTest {

	private static String logPath;

	@BeforeAll
	static void beforeAll() {

		// ログファイルパスを取得する
		logPath = new InnerClassPathProp("log4j.properties").get("log4j.appender.LOGFILE.File");

		// ログファイルが存在する場合は、削除する
		deleteLogFileIfExists();
	}

	@BeforeEach
	void beforeEach() {

		//
		// テストメソッドはprivateを付けてはいけない。
		// 各テストを実施する前の開始処理を記述する。
		//

		// Linuxでは "log4j.properties" を読み込まない問題が出るため、手動で読み込ませる
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		PropertyConfigurator.configure(classLoader.getResource("log4j.properties"));
	}

	@AfterEach
	void afterEach() {

		//
		// テストメソッドはprivateを付けてはいけない。
		// 各テストを実施する前の開始処理を記述する。
		//
	}

	static void deleteLogFileIfExists() {

		// ログファイルが存在しない場合は、即時終了する
		File logFile = new File(logPath);
		if (!logFile.exists()) {
			return;
		}

		// 所定の回数リトライして、ファイル削除を試行する
		for (int retryCnt = 0; retryCnt < 3; retryCnt++) {
			if (logFile.delete()) {
				break;
			}
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	//
	// 各テストメソッドはstatic、private禁止、戻り値も返却してはならない
	//

	@Test
	void test01() {


		// 例外のログ記録を検証する
		new LogUtil().handleException(new ApplicationInternalException("アプリケーション例外"));
		File logFile = new File(logPath);
		assertTrue(logFile.exists());
	}

	@Test
	void test02() {

		// 例外がnullのパターンを検証する
		new LogUtil().handleException(null);
		File logFile = new File(logPath);
		assertTrue(logFile.exists());
	}

	@Test
	void test03() {

		// 例外メッセージが空文字列のパターンを検証する
		new LogUtil().handleException(new Exception(""));
		File logFile = new File(logPath);
		assertTrue(logFile.exists());
	}

	@Test
	void test04() {

		// byte配列のログパターンを検証する
		byte[] bytes = new byte[] {0x01, 0x02};
		new LogUtil().recordBytesLog(logPath, bytes);
		File logFile = new File(logPath);
		assertTrue(logFile.exists());
	}
}
