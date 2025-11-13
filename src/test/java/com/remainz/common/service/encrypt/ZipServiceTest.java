package com.remainz.common.service.encrypt;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedWriter;
import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.TestUtil;
import com.remainz.common.exception.ApplicationInternalException;
import com.remainz.common.exception.BusinessRuleViolationException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.util.Cu;
import com.remainz.common.util.FileUtil;
import com.remainz.common.util.Mu;

/**
 * ファイルもしくはディレクトリをzip圧縮します。
 * "targetDirOrFile"で対象を指定します。(ディレクトリかファイルか判別して再帰的に圧縮します)
 */
public class ZipServiceTest {

	@BeforeEach
	void beforeEach() {

		// テストフォルダを作成する
		new FileUtil().createDirIfNotExists(TestUtil.RESOURCE_PATH + "service/encrypt/ZipServiceTest/dirToZip/subDir1");
		new FileUtil().createDirIfNotExists(TestUtil.OUTPUT_PATH + "ZipServiceTest");
		new FileUtil().createDirIfNotExists(TestUtil.OUTPUT_PATH + "ZipServiceTest/unzip");
	}

	@AfterEach
	void afterEach() {

		// テストフォルダを削除する
		new FileUtil().deleteDirIfExists(TestUtil.OUTPUT_PATH);
	}

	//
	// 各テストメソッドはstatic、private禁止、戻り値も返却してはならない
	//

	@Test
	void test01() {

		// 必須パラメータなしのパターン
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new ZipService();
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "targetDirOrFile"), e.getLocalizedMessage());
		}

		try {
			input.putString("targetDirOrFile", TestUtil.OUTPUT_PATH + "ZipServiceTest/" + "dirToZip");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "outputDir"), e.getLocalizedMessage());
		}
	}

	@Test
	void test02() {

		// 正常系
		var input = getNormalInput("dirToZip");
		var output = new GenericParam();
		var service = new ZipService();
		service.doService(input, output);

		// 正常系のassertを行う
		assertNormal(output);
	}

	@Test
	void test03() {

		// カバレッジ(存在しないzip出力先を指定)
		var input = getNormalInput("dirToZip");
		input.putString("outputDir", TestUtil.OUTPUT_PATH + "ZipServiceTest/nowhere");
		var output = new GenericParam();
		var service = new ZipService();
		try {
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains("FileNotFoundException"));
		}
	}

	@Test
	void test04() {

		// カバレッジ(絶対パスでzip圧縮対象を指定)
		var input = getNormalInput("dirToZip");
		File dir = new File(input.getString("targetDirOrFile"));
		input.putString("targetDirOrFile", dir.getAbsolutePath());
		var output = new GenericParam();
		var service = new ZipService();
		service.doService(input, output);

		// 正常系のassertを行う
		assertNormal(output);
	}

	@Test
	void test05() {

		// カバレッジ(拡張子ありのファイルを圧縮)
		var input = getNormalInput("dirToZip");
		input.putString("targetDirOrFile", TestUtil.RESOURCE_PATH + "service/encrypt/ZipServiceTest/fileToZip.txt");
		var output = new GenericParam();
		var service = new ZipService();
		service.doService(input, output);

		// zipファイルが存在することを確認する
		String outputDir = TestUtil.OUTPUT_PATH + "ZipServiceTest/";
		assertNotBlankFile(outputDir + "fileToZip.zip");

		// zipファイルを解凍する
		unzip(outputDir + "fileToZip.zip", outputDir + "unzip");

		outputDir = outputDir + "/unzip/";
		assertNotBlankFile(outputDir + "fileToZip.txt");
	}

	@Test
	void test06() throws Exception {

		// 親ディレクトリなしのテストを行うため、一時ファイルを作成する
		File temp = new File("blankFileForTestZipService");
		try (BufferedWriter file = new FileUtil().getBufferedWriter("blankFileForTestZipService")) {
			file.close();
		}
		assertTrue(Cu.isEmpty(temp.getParent()));

		// カバレッジ(親ディレクトリなし)
		var input = getNormalInput("dirToZip");
		input.putString("targetDirOrFile", "blankFileForTestZipService");
		var output = new GenericParam();
		var service = new ZipService();
		service.doService(input, output);

		// zipファイルが存在することを確認する
		String outputDir = TestUtil.OUTPUT_PATH + "ZipServiceTest/";
		assertNotBlankFile(outputDir + "blankFileForTestZipService.zip");

		// zipファイルを解凍する
		unzip(outputDir + "blankFileForTestZipService.zip", outputDir + "unzip");

		outputDir = outputDir + "/unzip/";
		assertBlankFile(outputDir + "blankFileForTestZipService");

		// 一時ファイルを削除する
		new FileUtil().deleteFileOrDir(temp);
	}

	private GenericParam getNormalInput(String targetDirOrFile) {

		// 正常系の入力パラメータを生成し、呼び出し側に返却する
		var input = new GenericParam();
		input.putString("targetDirOrFile", TestUtil.RESOURCE_PATH + "service/encrypt/ZipServiceTest/" + targetDirOrFile);
		input.putString("outputDir", TestUtil.OUTPUT_PATH + "ZipServiceTest");
		return input;
	}

	private void assertNormal(GenericParam output) {

		// zipファイルが存在することを確認する
		String outputDir = TestUtil.OUTPUT_PATH + "ZipServiceTest/";
		assertEquals(outputDir + "dirToZip.zip", output.getString("zipFilePath"));
		assertNotBlankFile(outputDir + "dirToZip.zip");

		// zipファイルを解凍する
		unzip(outputDir + "dirToZip.zip", outputDir + "unzip");

		// zipファイルを解凍した結果の構成を確認する
		outputDir = outputDir + "unzip/";
		assertDir(outputDir + "dirToZip");
		assertDir(outputDir + "dirToZip/subDir1");
		assertDir(outputDir + "dirToZip/subDir2");
		assertDir(outputDir + "dirToZip/subDir3");
		assertDir(outputDir + "dirToZip/subDir3/subDir3_1");
		assertBlankFile(outputDir + "dirToZip/subBlankFile1.txt");
		assertBlankFile(outputDir + "dirToZip/subBlankFile2.txt");
		assertNotBlankFile(outputDir + "dirToZip/subFile1.txt");
		assertNotBlankFile(outputDir + "dirToZip/subFile2.txt");
		assertBlankFile(outputDir + "dirToZip/subDir2/subBlankFile2_1.txt");
		assertBlankFile(outputDir + "dirToZip/subDir2/subBlankFile2_2.txt");
		assertNotBlankFile(outputDir + "dirToZip/subDir2/subFile2_1.txt");
		assertNotBlankFile(outputDir + "dirToZip/subDir2/subFile2_2.txt");
		assertBlankFile(outputDir + "dirToZip/subDir3/subBlankFile3_1.txt");
		assertBlankFile(outputDir + "dirToZip/subDir3/subBlankFile3_2.txt");
		assertNotBlankFile(outputDir + "dirToZip/subDir3/subFile3_1.txt");
		assertNotBlankFile(outputDir + "dirToZip/subDir3/subFile3_2.txt");
		assertBlankFile(outputDir + "dirToZip/subDir3/subDir3_1/subBlankFile3_1_1.txt");
		assertBlankFile(outputDir + "dirToZip/subDir3/subDir3_1/subBlankFile3_1_2.txt");
		assertNotBlankFile(outputDir + "dirToZip/subDir3/subDir3_1/subFile3_1_1.txt");
		assertNotBlankFile(outputDir + "dirToZip/subDir3/subDir3_1/subFile3_1_2.txt");
	}

	private void unzip(String targetZip, String outputDir) {

		// zipファイルを解凍する
		var input = new GenericParam();
		input.putString("targetZip", targetZip);
		input.putString("outputDir", outputDir);
		var output = new GenericParam();
		var service = new UnzipService();
		service.doService(input, output);
	}

	private void assertBlankFile(String filePath) {
		File file = new File(filePath);
		assertTrue(file.exists());
		assertTrue(file.isFile());
		assertTrue(file.length() == 0);
	}

	private void assertNotBlankFile(String filePath) {
		File file = new File(filePath);
		assertTrue(file.exists());
		assertTrue(file.isFile());
		assertTrue(file.length() > 0);
	}

	private void assertDir(String filePath) {
		File dir = new File(filePath);
		assertTrue(dir.exists());
		assertTrue(dir.isDirectory());
	}
}
