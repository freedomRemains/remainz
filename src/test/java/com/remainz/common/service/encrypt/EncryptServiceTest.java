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

public class EncryptServiceTest {

	@BeforeEach
	void beforeEach() {

		// テストフォルダを作成する
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
		var service = new EncryptService();
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "filePath"), e.getLocalizedMessage());
		}

		try {
			input.putString("filePath", TestUtil.OUTPUT_PATH + "ZipServiceTest/" + "dirToZip");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "outputDir"), e.getLocalizedMessage());
		}

		try {
			input.putString("outputDir", TestUtil.OUTPUT_PATH + "ZipServiceTest/");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "encryptKind"), e.getLocalizedMessage());
		}
	}

	@Test
	void test02() {

		String targetDirOrFile = "dirToZip";

		// 正常系
		var input = getNormalInput(targetDirOrFile);
		var output = new GenericParam();
		var service = new EncryptService();
		service.doService(input, output);

		assertNormal(targetDirOrFile + ".zip");
	}

	@Test
	void test03() {

		String targetDirOrFile = "dirToZip";

		// カバレッジ(拡張子なしファイル)
		var input = getNormalInput(targetDirOrFile);
		String renameToFilePath = rename(input.getString("filePath"), targetDirOrFile);
		input.putString("filePath", renameToFilePath);
		var output = new GenericParam();
		var service = new EncryptService();
		service.doService(input, output);

		assertNormal(targetDirOrFile);
	}

	@Test
	void test04() {

		String targetDirOrFile = "dirToZip";

		// カバレッジ(存在しないファイル)
		var input = getNormalInput(targetDirOrFile);
		input.putString("filePath", "notExistDir/notExist.txt");
		var output = new GenericParam();
		var service = new EncryptService();
		try {
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains("FileNotFoundException"));
		}
	}

	private GenericParam getNormalInput(String targetDirOrFile) {

		// zipファイルを生成する
		String zipFilePath = zip(targetDirOrFile, TestUtil.OUTPUT_PATH + "ZipServiceTest");

		var input = new GenericParam();
		input.putString("filePath", zipFilePath);
		input.putString("outputDir", TestUtil.OUTPUT_PATH + "ZipServiceTest");
		input.putString("encryptKind", "AES");
		return input;
	}

	private String zip(String targetDirOrFile, String outputDir) {

		// zipサービスを実行する
		var input = new GenericParam();
		input.putString("targetDirOrFile", TestUtil.RESOURCE_PATH + "service/encrypt/ZipServiceTest/"
				+ targetDirOrFile);
		input.putString("outputDir", outputDir);
		var output = new GenericParam();
		var service = new ZipService();
		service.doService(input, output);

		// 生成されたzipファイルのファイルパスを呼び出し側に返却する
		return output.getString("zipFilePath");
	}

	private String rename(String filePath, String renameTo) {
		File target = new File(filePath);
		File renameToFile = new File(target.getParent() + "/" + renameTo);
		target.renameTo(renameToFile);
		return renameToFile.getAbsolutePath();
	}

	private void assertNormal(String targetDirOrFile) {

		// 結果ファイルが出力されていることを確認する
		String outputDir = TestUtil.OUTPUT_PATH + "ZipServiceTest/";
		assertTrue(new File(outputDir + targetDirOrFile + "_encrypt.bin").exists());
		assertTrue(new File(outputDir + targetDirOrFile + "_encrypt.key").exists());
		assertTrue(new File(outputDir + targetDirOrFile + "_encrypt.iv").exists());
	}
}
