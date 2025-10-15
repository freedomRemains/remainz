package com.remainz.common.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

//
//テストクラスはprivateを付けてはならず、publicも通常は付けない。
//
class FileUtilTest {

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
	void test01() {

		// getBufferedReaderの検証
		try (BufferedReader bufferedReader = new FileUtil().getBufferedReader(
				RESOURCE_PATH + "util/FileUtilTest/test.txt")) {
			assertNotNull(bufferedReader);

		} catch (Exception e) {
			fail();
		}
	}

	@Test
	void test02() {

		// getBufferedReaderの検証(エンコーディング指定あり)
		try (BufferedReader bufferedReader = new FileUtil().getBufferedReader(
				RESOURCE_PATH + "util/FileUtilTest/test.txt", "SJIS")) {
			assertNotNull(bufferedReader);

		} catch (Exception e) {
			fail();
		}
	}

	@Test
	void test03() {

		// getBufferedWriterの検証
		try (BufferedWriter bufferedWriter = new FileUtil().getBufferedWriter(
				RESOURCE_PATH + "util/FileUtilTest/write.txt")) {
			bufferedWriter.write("This is test.");

		} catch (Exception e) {
			fail();
		}

		assertEquals("This is test.".length(),
				new File(RESOURCE_PATH + "util/FileUtilTest/write.txt").length());

		// getBufferedWriterの検証(追記モード)
		try (BufferedWriter bufferedWriter = new FileUtil().getBufferedWriter(
				RESOURCE_PATH + "util/FileUtilTest/write.txt", true)) {
			bufferedWriter.write("This is test2.");

		} catch (Exception e) {
			fail();
		}

		assertEquals("This is test.".length() + "This is test2.".length(),
				new File(RESOURCE_PATH + "util/FileUtilTest/write.txt").length());

		// getBufferedWriterの検証(追記モード、エンコーディング指定)
		try (BufferedWriter bufferedWriter = new FileUtil().getBufferedWriter(
				RESOURCE_PATH + "util/FileUtilTest/write.txt", true, "SJIS")) {
			bufferedWriter.write("This is test3.");

		} catch (Exception e) {
			fail();
		}

		assertEquals("This is test.".length() + "This is test2.".length() + "This is test3.".length(),
				new File(RESOURCE_PATH + "util/FileUtilTest/write.txt").length());

		// テストで作成したファイルは削除する
		new File(RESOURCE_PATH + "util/FileUtilTest/write.txt").delete();
	}

	@Test
	void test04() {

		// getZipOutputStreamの検証
		try (ZipOutputStream ZipOutputStream = new FileUtil().getZipOutputStream(
				RESOURCE_PATH + "util/FileUtilTest/write.zip")) {
			assertNotNull(ZipOutputStream);

		} catch (Exception e) {
			fail();
		}

		// テストで作成したファイルは削除する
		new File(RESOURCE_PATH + "util/FileUtilTest/write.zip").delete();
	}

	@Test
	void test05() {

		// getZipInputStreamの検証
		try (ZipInputStream zipInputStream = new FileUtil().getZipInputStream(
				RESOURCE_PATH + "util/FileUtilTest/test.zip")) {
			assertNotNull(zipInputStream);

		} catch (Exception e) {
			fail();
		}
	}

	@Test
	void test06() {

		// getBufferedInputStreamの検証
		try (BufferedInputStream bufferedInputStream = new FileUtil().getBufferedInputStream(
				RESOURCE_PATH + "util/FileUtilTest/test.txt")) {
			assertNotNull(bufferedInputStream);

		} catch (Exception e) {
			fail();
		}
	}

	@Test
	void test07() {

		// getBufferedOutputStreamの検証
		try (BufferedOutputStream bufferedOutputStream = new FileUtil().getBufferedOutputStream(
				RESOURCE_PATH + "util/FileUtilTest/write.txt")) {
			assertNotNull(bufferedOutputStream);

		} catch (Exception e) {
			fail();
		}

		// テストで作成したファイルは削除する
		new File(RESOURCE_PATH + "util/FileUtilTest/write.txt").delete();
	}

	@Test
	void test08() throws Exception {

		// テストのためにディレクトリを作成する
		FileUtil fileUtil = new FileUtil();
		fileUtil.createDirIfNotExists(RESOURCE_PATH + "util/FileUtilTest/output");

		// 存在するディレクトリを指定してcreate
		fileUtil.createDirIfNotExists(RESOURCE_PATH + "util/FileUtilTest/output");
		assertTrue(new File(RESOURCE_PATH + "util/FileUtilTest/output").exists());
		assertTrue(new File(RESOURCE_PATH + "util/FileUtilTest/output").isDirectory());

		// ファイルを作成する
		try (BufferedWriter bufferedWriter = new FileUtil().getBufferedWriter(
				RESOURCE_PATH + "util/FileUtilTest/output/write.txt")) {
			bufferedWriter.write("This is test.");
		}

		// 存在するファイルと同名を指定してディレクトリ作成
		fileUtil.createDirIfNotExists(RESOURCE_PATH + "util/FileUtilTest/output/write.txt");
		assertTrue(new File(RESOURCE_PATH + "util/FileUtilTest/output/write.txt").exists());
		assertTrue(new File(RESOURCE_PATH + "util/FileUtilTest/output/write.txt").isFile());

		// 存在するファイルと同名を指定してディレクトリ削除
		fileUtil.deleteDirIfExists(RESOURCE_PATH + "util/FileUtilTest/output/write.txt");
		assertTrue(new File(RESOURCE_PATH + "util/FileUtilTest/output/write.txt").exists());
		assertTrue(new File(RESOURCE_PATH + "util/FileUtilTest/output/write.txt").isFile());

		// テストのために作成したディレクトリを削除する
		fileUtil.deleteDirIfExists(RESOURCE_PATH + "util/FileUtilTest/output");

		// 存在しないディレクトリを指定してdelete
		fileUtil.deleteDirIfExists(RESOURCE_PATH + "util/FileUtilTest/output");
		assertFalse(new File(RESOURCE_PATH + "util/FileUtilTest/output").exists());
	}

	@Test
	void test09() throws Exception {

		// テストのためにディレクトリを作成する
		FileUtil fileUtil = new FileUtil();
		fileUtil.createDirIfNotExists(RESOURCE_PATH + "util/FileUtilTest/output");

		// カバレッジ(空のディレクトリ削除)
		fileUtil.deleteDirIfExists(RESOURCE_PATH + "util/FileUtilTest/output");

		// 存在しないディレクトリを指定してdelete
		fileUtil.deleteDirIfExists(RESOURCE_PATH + "util/FileUtilTest/output");
		assertFalse(new File(RESOURCE_PATH + "util/FileUtilTest/output").exists());
	}

	@Test
	void test10() throws Exception {

		// テストのためにディレクトリを作成する
		FileUtil fileUtil = new FileUtil();
		fileUtil.createDirIfNotExists(RESOURCE_PATH + "util/FileUtilTest/output");

		// カバレッジ(存在するディレクトリのため、mkdirを最大リトライまで繰り返す)
		fileUtil.mkdirs(new File(RESOURCE_PATH + "util/FileUtilTest/output"));
		assertTrue(new File(RESOURCE_PATH + "util/FileUtilTest/output").exists());
		assertTrue(new File(RESOURCE_PATH + "util/FileUtilTest/output").isDirectory());

		// ファイルを作成する
		try (BufferedWriter bufferedWriter = new FileUtil().getBufferedWriter(
				RESOURCE_PATH + "util/FileUtilTest/output/write.txt")) {
			bufferedWriter.write("This is test.");
		}

		// ファイルを開いてつかんだ状態とする
		try (BufferedReader bufferedReader = new FileUtil().getBufferedReader(
				RESOURCE_PATH + "util/FileUtilTest/output/write.txt")) {

			// ファイルを削除する(カバレッジ、つかんでいるので削除できず最大までリトライする)
			fileUtil.deleteFileOrDir(new File(RESOURCE_PATH + "util/FileUtilTest/output/write.txt"));
		}

		// Linuxの場合、rootだとファイルが開かれていても消せるため、このassertは行わないこととした
		//assertTrue(new File(RESOURCE_PATH + "util/FileUtilTest/output/write.txt").exists());
		//assertTrue(new File(RESOURCE_PATH + "util/FileUtilTest/output/write.txt").isFile());

		// 存在しないディレクトリを指定してdelete
		fileUtil.deleteDirIfExists(RESOURCE_PATH + "util/FileUtilTest/output");
		assertFalse(new File(RESOURCE_PATH + "util/FileUtilTest/output").exists());
	}

	@Test
	void test11() throws Exception {

		// テストのためにディレクトリを作成する
		FileUtil fileUtil = new FileUtil();
		fileUtil.createDirIfNotExists(RESOURCE_PATH + "util/FileUtilTest/output");

		// ファイルを作成する
		try (BufferedWriter bufferedWriter = new FileUtil().getBufferedWriter(
				RESOURCE_PATH + "util/FileUtilTest/output/write.txt")) {
			bufferedWriter.write("This is test.");
		}

		// カバレッジ(ファイル削除、listFilesがnullになる条件)
		fileUtil.deleteDirRecursive(new File(RESOURCE_PATH + "util/FileUtilTest/output/write.txt"));

		// 存在しないディレクトリを指定してdelete
		fileUtil.deleteDirIfExists(RESOURCE_PATH + "util/FileUtilTest/output");
		assertFalse(new File(RESOURCE_PATH + "util/FileUtilTest/output").exists());
	}

	@Test
	void test12() throws Exception {

		// カバレッジ(ディレクトリ配下の子要素を取得)
		FileUtil fileUtil = new FileUtil();
		File[] listFiles = fileUtil.listFiles(new File(RESOURCE_PATH + "util/FileUtilTest"));
		assertNotNull(listFiles);
		assertTrue(listFiles.length == 2);
	}

	@Test
	void test13() throws Exception {

		// カバレッジ(ファイルを指定してディレクトリ配下の子要素を取得)
		FileUtil fileUtil = new FileUtil();
		File[] listFiles = fileUtil.listFiles(new File(RESOURCE_PATH + "util/FileUtilTest/test.txt"));
		assertNotNull(listFiles);
		assertTrue(listFiles.length == 0);
	}
}
