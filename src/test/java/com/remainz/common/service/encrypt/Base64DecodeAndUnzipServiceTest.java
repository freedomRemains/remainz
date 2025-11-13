package com.remainz.common.service.encrypt;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.TestUtil;
import com.remainz.common.exception.ApplicationInternalException;
import com.remainz.common.exception.BusinessRuleViolationException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.util.FileUtil;
import com.remainz.common.util.Mu;

public class Base64DecodeAndUnzipServiceTest {

	@BeforeEach
	void beforeEach() {

		// テストフォルダを作成する
		new FileUtil().createDirIfNotExists(TestUtil.RESOURCE_PATH + "service/encrypt/ZipServiceTest/dirToZip/subDir1");
		new FileUtil().createDirIfNotExists(TestUtil.OUTPUT_PATH + "ZipAndBase64EncodeServiceTest");
		new FileUtil().createDirIfNotExists(TestUtil.OUTPUT_PATH + "ZipAndBase64EncodeServiceTest/unzip");
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
		var service = new Base64DecodeAndUnzipService();
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "encodeResultFilePath"), e.getLocalizedMessage());
		}

		try {
			input.putString("encodeResultFilePath", TestUtil.OUTPUT_PATH + "ZipAndBase64EncodeServiceTest/" + "dirToZipEncode.txt");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "outputDir"), e.getLocalizedMessage());
		}
	}

	@Test
	void test02() {

		String targetDirOrFile = "dirToZip";

		// 正常系
		var input = getNormalInput(targetDirOrFile);
		var output = new GenericParam();
		var service = new Base64DecodeAndUnzipService();
		service.doService(input, output);
		assertNormal(output, targetDirOrFile);
	}

	@Test
	void test03() {

		String targetDirOrFile = "dirToZip";

		// カバレッジ(charsetを明示的に指定)
		var input = getNormalInput(targetDirOrFile);
		input.putString("charset", "UTF-8");
		var output = new GenericParam();
		var service = new Base64DecodeAndUnzipService();
		service.doService(input, output);
		assertNormal(output, targetDirOrFile);
	}

	@Test
	void test04() {

		String targetDirOrFile = "dirToZip";

		// カバレッジ(存在しないcharset)
		var input = getNormalInput(targetDirOrFile);
		var output = new GenericParam();
		var service = new Base64DecodeAndUnzipService();
		try {
			input.putString("charset", "notExistCharset");
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains("UnsupportedEncodingException"));
		}
	}

	private GenericParam getNormalInput(String targetDirOrFile) {

		// エンコードを行う
		var input = new GenericParam();
		input.putString("targetDirOrFile", TestUtil.RESOURCE_PATH + "service/encrypt/ZipServiceTest/" + targetDirOrFile);
		input.putString("outputDir", TestUtil.OUTPUT_PATH + "ZipAndBase64EncodeServiceTest");
		var output = new GenericParam();
		var service = new ZipAndBase64EncodeService();
		service.doService(input, output);

		// 出力パラメータをデコードの入力パラメータとするため、必要な入力パラメータを設定する
		output.putString("outputDir", TestUtil.OUTPUT_PATH + "ZipAndBase64EncodeServiceTest/unzip");

		// 出力パラメータを呼び出し側に返却する
		return output;
	}

	private void assertNormal(GenericParam output, String targetDirOrFile) {

		// zipファイルが存在しないことを確認する
		assertFalse(existsInOutputDir(".zip"));

		// エンコードファイル及びキーファイルが存在することを確認する
		String outputDir = TestUtil.OUTPUT_PATH + "ZipAndBase64EncodeServiceTest/unzip/";
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

	private boolean existsInOutputDir(String targetFileNamePart) {

		// 出力フォルダにファイルがない場合は、戻り値falseで呼び出し側に復帰する
		File outputDir = new File(TestUtil.OUTPUT_PATH + "ZipAndBase64EncodeServiceTest");
		File[] outputFiles = outputDir.listFiles();
		if (outputFiles == null) {
			return false;
		}

		// ファイル名が部分一致するファイルを検出した場合は、戻り値trueで呼び出し側に復帰する
		for (File outputFile : outputFiles) {
			if (outputFile.getName().contains(targetFileNamePart)) {
				return true;
			}
		}

		// パターンに合致するファイルがない場合は、戻り値falseで呼び出し側に復帰する
		return false;
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
