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

public class DecryptServiceTest {

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
		var service = new DecryptService();
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "secretKeyFilePath"), e.getLocalizedMessage());
		}

		try {
			input.putString("secretKeyFilePath", TestUtil.OUTPUT_PATH + "ZipServiceTest/" + "dirToZip_encrypt.key");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "ivFilePath"), e.getLocalizedMessage());
		}

		try {
			input.putString("ivFilePath", TestUtil.OUTPUT_PATH + "ZipServiceTest/" + "dirToZip_encrypt.iv");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "encryptResultFilePath"), e.getLocalizedMessage());
		}

		try {
			input.putString("encryptResultFilePath", TestUtil.OUTPUT_PATH + "ZipServiceTest/" + "dirToZip_encrypt.bin");
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
		var service = new DecryptService();
		service.doService(input, output);

		assertNormal(targetDirOrFile + ".zip");
	}

	@Test
	void test03() {

		String targetDirOrFile = "dirToZip";

		// カバレッジ(拡張子なしファイル)
		var input = getNormalInput(targetDirOrFile);
		String renameToFilePath = rename(input.getString("encryptResultFilePath"), targetDirOrFile);
		input.putString("encryptResultFilePath", renameToFilePath);
		var output = new GenericParam();
		var service = new DecryptService();
		service.doService(input, output);

		assertNormal(targetDirOrFile + "_decrypt.bin");
	}

	@Test
	void test04() {

		String targetDirOrFile = "dirToZip";

		// カバレッジ(存在しない暗号化結果ファイル)
		var input = getNormalInput(targetDirOrFile);
		input.putString("encryptResultFilePath", input.getString("encryptResultFilePath") + "/notExist.txt");
		var output = new GenericParam();
		var service = new DecryptService();
		try {
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains("FileNotFoundException"));
		}
	}

	@Test
	void test05() {

		String targetDirOrFile = "dirToZip";

		// カバレッジ(存在しない秘密鍵ファイル)
		var input = getNormalInput(targetDirOrFile);
		input.putString("secretKeyFilePath", input.getString("secretKeyFilePath") + "/notExist.txt");
		var output = new GenericParam();
		var service = new DecryptService();
		try {
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains("FileNotFoundException"));
		}
	}

	@Test
	void test06() {

		String targetDirOrFile = "dirToZip";

		// カバレッジ(存在しないivファイル)
		var input = getNormalInput(targetDirOrFile);
		input.putString("ivFilePath", input.getString("ivFilePath") + "/notExist.txt");
		var output = new GenericParam();
		var service = new DecryptService();
		try {
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains("FileNotFoundException"));
		}
	}

	private GenericParam getNormalInput(String targetDirOrFile) {

		// zipファイルを生成する
		String outputDir = TestUtil.OUTPUT_PATH + "ZipServiceTest";
		String zipFilePath = zip(targetDirOrFile, outputDir);

		// 暗号化を行う
		var input = encrypt(zipFilePath, outputDir);

		// 暗号化サービスの出力パラメータを復号サービスの入力パラメータとするため、必要な設定を追加する
		input.putString("outputDir", TestUtil.OUTPUT_PATH + "ZipServiceTest");
		input.putString("encryptKind", "AES");

		// 生成した復号サービスの入力パラメータを呼び出し側に返却する
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

	private GenericParam encrypt(String filePath, String outputDir) {

		// 暗号化サービスを実行する
		var input = new GenericParam();
		input.putString("filePath", filePath);
		input.putString("outputDir", outputDir);
		input.putString("encryptKind", "AES");
		var output = new GenericParam();
		var service = new EncryptService();
		service.doService(input, output);

		// 出力パラメータを呼び出し側に返却する
		return output;
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
		assertTrue(new File(outputDir + targetDirOrFile).exists());
	}
}
