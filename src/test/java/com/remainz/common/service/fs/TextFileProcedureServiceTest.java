package com.remainz.common.service.fs;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remainz.common.exception.BusinessRuleViolationException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.util.Mu;

/**
 * テキストファイルを1行ずつ処理していくサービスです。<br>
 * このサービスはテキストファイル1行ごとにinput、outputの内容を取り回して処理するため、<br>
 * 呼び出し側でinputとoutputを上位から引き継がずに呼び出すと、混乱やバグを防ぎやすいです。<br>
 * 
 * [inputに必要なパラメータ]<br>
 * filePath ファイルパス<br>
 * dirPath ディレクトリパス<br>
 * fileName ファイル名<br>
 * procName プロシージャ名(1行ごとの処理を記述するクラスの名前)<br>
 * <br>
 * "filePath"単独で指定した場合は"dirPath"と"fileName"の組み合わせは無視される。<br>
 * "filePath"が空で"dirPath"と"fileName"の両方がある場合は、組み合わせて"filePath"と扱う。<br>
 * "procName"はテキストファイル読み込み処理クラスの完全修飾名を指定する。<br>
 */
public class TextFileProcedureServiceTest {

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

	@Test
	void test01() throws Exception {

		// 必須パラメータなしのパターン
		TextFileProcedureService service = new TextFileProcedureService();
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "procName"), e.getLocalizedMessage());
		}

		try {
			input.putString("procName", "com.remainz.common.service.proc.SqlFromFileProc");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "filePath or (dirPath, fileName)"), e.getLocalizedMessage());
		}
	}

	@Test
	void test02() throws Exception {

		// 存在しないファイルを指定するパターン
		TextFileProcedureService service = new TextFileProcedureService();
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		try {
			input.putString("procName", "com.remainz.common.service.proc.SqlFromFileProc");
			input.putString("filePath", "nowhere.txt");
			service.doService(input, output);
			fail();
		} catch (FileNotFoundException e) {
			assertTrue(e.getLocalizedMessage().contains("nowhere.txt"));
		}
	}

	private static final String RESOURCE_PATH = "src/test/resources/";

	@Test
	void test03() throws Exception {

		// 該当プロシージャなし
		TextFileProcedureService service = new TextFileProcedureService();
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		try {
			input.putString("procName", "notExist");
			input.putString("dirPath", RESOURCE_PATH + "service/fs/TextFileProcedureServiceTest");
			input.putString("fileName", "10_dropTable.sql");
			service.doService(input, output);
			fail();
		} catch (ClassNotFoundException e) {
			assertTrue(e.getLocalizedMessage().contains("notExist"));
		}
	}

	@Test
	void test04() throws Exception {

		// 正常系パターン
		TextFileProcedureService service = new TextFileProcedureService();
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		input.putString("procName", "com.remainz.common.service.proc.SqlFromFileProc");
		input.putString("dirPath", RESOURCE_PATH + "service/fs/TextFileProcedureServiceTest");
		input.putString("fileName", "10_dropTable.sql");
		service.doService(input, output);
		assertTrue(output.getString("sqlFromFile").contains("DROP TABLE IF EXISTS JLDB.GNR_GRP;"));
		assertTrue(output.getString("sqlFromFile").contains("DROP TABLE IF EXISTS JLDB.GNR_KEY_VAL;"));
		assertTrue(output.getString("sqlFromFile").contains("DROP TABLE IF EXISTS JLDB.ACCNT;"));
		assertTrue(output.getString("sqlFromFile").contains("DROP TABLE IF EXISTS JLDB.SCR;"));
		assertTrue(output.getString("sqlFromFile").contains("DROP TABLE IF EXISTS JLDB.SCR_ELM;"));
	}

	@Test
	void test05() throws Exception {

		// カバレッジを100%にするための項目(dirPathあり、fileNameなし)
		TextFileProcedureService service = new TextFileProcedureService();
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		try {
			input.putString("procName", "com.remainz.common.service.proc.SqlFromFileProc");
			input.putString("dirPath", "nowhereDir");
			input.putString("filePath", "nowhere.txt");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertTrue(e.getLocalizedMessage().contains("[parameter not exist]filePath or (dirPath, fileName)"));
		}
	}

	@Test
	void test06() throws Exception {

		// カバレッジを100%にするための項目(dirPathなし、fileNameあり、filePathなし)
		TextFileProcedureService service = new TextFileProcedureService();
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		try {
			input.putString("procName", "com.remainz.common.service.proc.SqlFromFileProc");
			input.putString("fileName", "notExist.txt");
			service.doService(input, output);
			fail();
		} catch (FileNotFoundException e) {
			assertTrue(e.getLocalizedMessage().contains("notExist.txt"));
		}
	}
}
