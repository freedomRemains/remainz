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

public class ZipAndBase64EncodeServiceTest {

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
		var service = new ZipAndBase64EncodeService();
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "targetDirOrFile"), e.getLocalizedMessage());
		}

		try {
			input.putString("targetDirOrFile", TestUtil.OUTPUT_PATH + "ZipAndBase64EncodeServiceTest/" + "dirToZip");
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
		var service = new ZipAndBase64EncodeService();
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
		var service = new ZipAndBase64EncodeService();
		service.doService(input, output);
		assertNormal(output, targetDirOrFile);
	}

	@Test
	void test04() {

		String targetDirOrFile = "dirToZip";

		// カバレッジ(存在しないcharset)
		var input = getNormalInput(targetDirOrFile);
		var output = new GenericParam();
		var service = new ZipAndBase64EncodeService();
		try {
			input.putString("charset", "notExistCharset");
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains("UnsupportedCharsetException"));
		}
	}

	private GenericParam getNormalInput(String targetDirOrFile) {
		var input = new GenericParam();
		input.putString("targetDirOrFile", TestUtil.RESOURCE_PATH + "service/encrypt/ZipServiceTest/" + targetDirOrFile);
		input.putString("outputDir", TestUtil.OUTPUT_PATH + "ZipAndBase64EncodeServiceTest");
		input.putString("encryptKind", "AES");
		return input;
	}

	private void assertNormal(GenericParam output, String targetDirOrFile) {

		// zipファイルが存在しないことを確認する
		assertFalse(existsInOutputDir(".zip"));

		// エンコードファイル及びキーファイルが存在することを確認する
		assertTrue(output.getString("encodeResultFilePath").contains(targetDirOrFile));
		assertTrue(output.getString("encodeResultFilePath").contains("Encode.txt"));
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
}
