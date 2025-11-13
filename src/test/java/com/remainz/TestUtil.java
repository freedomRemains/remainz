package com.remainz;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Paths;

import com.remainz.common.db.DbInterface;
import com.remainz.common.param.GenericParam;
import com.remainz.common.service.dbmng.common.DbUpdateBySqlFileService;
import com.remainz.common.service.dbmng.common.GetAllTableDefService;
import com.remainz.common.service.dbmng.common.GetTableNameListService;
import com.remainz.common.service.script.ScriptService;
import com.remainz.common.util.DbUtil;
import com.remainz.common.util.FileUtil;
import com.remainz.common.util.InnerClassPathProp;
import com.remainz.common.util.RcProp;

/**
 * JUnitテスト用のユーティリティクラスです。
 */
public class TestUtil {

	public static final String RESOURCE_PATH = "src/test/resources/";

	public static final String OUTPUT_PATH = "output/";

	private DbInterface db;
	private String dbName;

	/** 初期化済みフラグ */
	private static boolean isInitialized;

	/**
	 * コンストラクタ1
	 */
	public TestUtil() throws Exception {

		// DB名取得は必ず行うものとする
		getDbName();

		// 初期化済みの場合は何もしない
		if (isInitialized) {
			return;
		}

		// 初期化済みフラグを初期化済みに変更する
		isInitialized = true;

		// 別インスタンスでDB復元処理を実行し、そのインスタンスはすぐにDBクローズして解放する
		// (コンストラクタの段階ではまだDB接続されていないため、このような挙動が可能となっている)
		var testUtil = new TestUtil();
		testUtil.restoreDb();
		testUtil.getDb().commit();
		testUtil.closeDb();
		testUtil = null;
	}

	/**
	 * DB名を取得します。<br>
	 * 多くのJUnitでDB名が必要となるため、プロパティファイル経由でDB名を特定する<br>
	 * メソッド自体を共通化します。<br>
	 * 
	 * @return DB名("mysql", "h2")
	 */
	public String getDbName() {

		// プロパティファイルからDB名を取得する
		dbName = "mysql";
		if ("com.remainz.common.db.H2Db".equals(new InnerClassPathProp("rc.properties").get("db.type"))) {
			dbName = "h2";
		}

		// DB名を呼び出し側に返却する
		return dbName;
	}

	/**
	 * DBを取得します。<br>
	 * 多くのJUnitテストに必要となるDBインスタンスを取得します。<br>
	 * staticな領域に実体を持つシングルトンの構成で、DBインスタンスを提供します。<br>
	 * DBのクローズメソッドも用意し、任意のタイミングでDBのオープン／クローズができるようにします。<br>
	 * 
	 * @return DBインスタンス
	 */
	public DbInterface getDb() {

		// インスタンスがまだ生成されていない場合のみ生成する
		if (db == null) {
			db = new DbUtil().getDb(new InnerClassPathProp("rc.properties"));
		}

		// DBインスタンスを呼び出し側に返却する
		return db;
	}

	/**
	 * staticな領域で管理しているDBをクローズします。<br>
	 * DBクローズ後はインスタンスをnullに戻し、再度getDb()呼び出しでDB生成できるようにします。<br>
	 * 
	 * @throws Exception 例外
	 */
	public void closeDb() throws Exception {

		// DBをクローズし、変数もnullとする
		if (db != null) {
			db.close();
			db = null;
		}
	}

	/**
	 * テスト用のファイル出力用フォルダを削除します。
	 */
	public void clearOutputDir() {

		// テストフォルダを削除する
		new FileUtil().deleteDirIfExists(OUTPUT_PATH);
	}

	/**
	 * ファイル内に対象となる文字列が存在するかどうかを検証します。<br>
	 * 
	 * @param filePath ファイルパス
	 * @param targetString 検索対象文字列
	 * @throws Exception 例外
	 */
	public void assertFileContains(String filePath, String targetString) throws Exception {
		assertTrue(Files.readString(Paths.get(filePath)).contains(targetString));
	}

	/**
	 * ファイル内に対象となる文字列が存在しないことを検証します。<br>
	 * 
	 * @param filePath ファイルパス
	 * @param targetString 検索対象文字列
	 * @throws Exception 例外
	 */
	public void assertFileNotContains(String filePath, String targetString) throws Exception {
		assertFalse(Files.readString(Paths.get(filePath)).contains(targetString));
	}

	/**
	 * JUnitテストのための準備を行います。<br>
	 * テストに必要なフォルダを準備し、どんな場合でも同じ結果となるよう固定のSQLを<br>
	 * を実行してTBL_DEFのレコードを生成します。またテスト用の固定ファイルを使って<br>
	 * ダミーテーブル定義を出力するためのパラメータを生成します。<br>
	 * 
	 * @return DB名
	 * @throws Exception 例外
	 */
	public String prepare() throws Exception {

		// DB名を取得する
		dbName = getDbName();

		// テストに必要なフォルダを作成する
		prepareOutputDir();

		// どんな場合でも必ず同じテスト結果となるよう、固定のSQLを実行してTBL_DEFのレコードを生成する
		createTableDefBySqlFile();

		// テスト用に固定で用意されているファイルを使って、ダミーテーブル定義を出力するためのパラメータを生成する
		loadTableDef();

		// DB名を呼び出し側に返却する
		return dbName;
	}

	public void prepareOutputDir() {

		// テストに必要なフォルダを作成する
		new FileUtil().createDirIfNotExists(OUTPUT_PATH + "dbmng/" + dbName + "/10_dbdef/20_auto_created");
		new FileUtil().createDirIfNotExists(OUTPUT_PATH + "dbmng/" + dbName + "/20_dbdata/20_auto_created");
		new FileUtil().createDirIfNotExists(OUTPUT_PATH + "dbmng/" + dbName + "/30_sql/20_auto_created");	
	}

	/**
	 * DB名に応じて、テーブル名のリストを取得します。<br>
	 * 既にDB上にテーブルが存在していることが前提です。<br>
	 * 
	 * @throws Exception 例外
	 */
	public void getTableNameList() throws Exception {

		// テーブル定義を出力するためのパラメータを生成する
		String dirPath = OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String getTableNameListSql = createGetTableNameListSql();

		// 処理の前提となるテーブル定義ファイルを出力する
		var input = new GenericParam();
		input.setDb(getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("getTableNameListSql", getTableNameListSql);
		var output = new GenericParam();
		var service = new GetTableNameListService();
		service.doService(input, output);
	}

	/**
	 * どんな場合でも必ず同じテスト結果となるよう、固定のSQLを実行して<br>
	 * TBL_DEFのレコードを生成します。<br>
	 * 以前は次の位置にある固定のSQLを実行していました。<br>
	 * String dummyTableDefFilePath = RESOURCE_PATH + "service/dbmng/common/GetAllTableDefServiceTest/10_addSqlForH2.sql";<br>
	 * 最新版では本物のテーブル構成のTBL_DEFのファイルを使用しています。<br>
	 * 本メソッドは最終的に不要となる見込みですが、削除可能と判断できるまでは残します。<br>
	 * 
	 * @throws Exception 例外
	 */
	public void createTableDefBySqlFile() throws Exception {

		// DBがh2の場合のみ実行する(MySQLの場合は実テーブルから直接DB定義を取得できるため、次の処理は不要)
		String dbName = getDbName();
		if ("h2".equals(dbName)) {

			// どんな場合でも必ず同じテスト結果となるよう、固定のSQLを実行してTBL_DEFのレコードを生成する
			String filePath = RESOURCE_PATH + "service/script/dbmng/" + dbName + "/30_sql/10_authorized/DROP_TBL_DEF.txt";
			doDbUpdateBySqlFileService(filePath, "dropResult");
			filePath = RESOURCE_PATH + "service/script/dbmng/" + dbName + "/30_sql/10_authorized/CREATE_TBL_DEF.txt";
			doDbUpdateBySqlFileService(filePath, "createResult");
			filePath = RESOURCE_PATH + "service/script/dbmng/" + dbName + "/30_sql/10_authorized/INSERT_TBL_DEF.txt";
			doDbUpdateBySqlFileService(filePath, "createResult");
		}
	}

	/**
	 * テスト用に固定で用意されているファイルを使って、テーブル定義を出力するための<br>
	 * パラメータを生成します。<br>
	 * 最後に GetAllTableDefService を実行し、全テーブル定義を取得します。<br>
	 * 以前は次の位置にある固定のSQLを実行していました。<br>
	 * String tableNameListFilePath = RESOURCE_PATH + "service/dbmng/common/GetAllTableDefServiceTest/tableNameList.txt";<br>
	 * 最新版では本物のテーブル構成のtableNameList.txtを使用しています。<br>
	 * 本メソッドは最終的に不要となる見込みですが、削除可能と判断できるまでは残します。<br>
	 * 
	 * @throws Exception 例外
	 */
	public void loadTableDef() throws Exception {

		// テスト用に固定で用意されているファイルを使って、テーブル定義を出力するためのパラメータを生成する
		String dirPath = OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String getTableDefSql = createGetTableDefSql();
		// 元々のコードを念のため残す(次のように所定の位置にある固定ファイルを指定していた)
		//String tableNameListFilePath = RESOURCE_PATH + "service/dbmng/common/GetAllTableDefServiceTest/tableNameList.txt";
		String tableNameListFilePath = RESOURCE_PATH + "service/script/dbmng/" + dbName + "/10_dbdef/10_authorized/tableNameList.txt";

		// 処理の前提となるテーブル定義ファイルを出力する
		var input = new GenericParam();
		input.setDb(getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("getTableDefSql", getTableDefSql);
		input.putString("tableNameListFilePath", tableNameListFilePath);
		var output = new GenericParam();
		var service = new GetAllTableDefService();
		service.doService(input, output);
	}

	/**
	 * テーブル名リストを取得するためのSQLを生成します。<br>
	 * h2の場合は INFORMATION_SCHEMA から取得します。<br>
	 * mysqlの場合は show tables でテーブル名リストを取得します。<br>
	 * 
	 * @return テーブル名リストを取得するためのSQL
	 */
	public String createGetTableNameListSql() {

		// MySQLかH2かによってSQLを分ける
		String getTableNameListSql = """
				SELECT TABLE_NAME
				  FROM INFORMATION_SCHEMA.TABLES
				  WHERE TABLE_TYPE = 'BASE TABLE'
				  AND TABLE_SCHEMA = 'PUBLIC'
				  ORDER BY TABLE_NAME
				  ;
				""";
		if ("com.remainz.common.db.MysqlDb".equals(new RcProp().get("db.type"))) {
			getTableNameListSql = "show tables;";
		}

		return getTableNameListSql;
	}

	/**
	 * DB名に応じ、DBテーブル定義を読み込むSQLを生成します。<br>
	 * h2の場合ば TBL_DEF からテーブル定義を取得します。<br>
	 * mysqlの場合は desc でテーブル定義を取得します。<br>
	 * 
	 * @param dbName DB名
	 * @return TBL_DEFテーブルから、DBテーブル定義を読み込むSQL
	 */
	public String createGetTableDefSql(String dbName) {

		// DB定義取得用SQLを生成する
		String getTableDefSql = "desc #TABLE_NAME#";
		if ("h2".equals(dbName)) {
			getTableDefSql = "SELECT * FROM TBL_DEF WHERE TABLE_NAME = '#TABLE_NAME#'";
		}

		// DB定義取得用SQLを呼び出し側に返却する
		return getTableDefSql;
	}

	/**
	 * DB名に応じ、DBテーブル定義を読み込むSQLを生成します。<br>
	 * h2の場合ば TBL_DEF からテーブル定義を取得します。<br>
	 * mysqlの場合は desc でテーブル定義を取得します。<br>
	 * 
	 * @return TBL_DEFテーブルから、DBテーブル定義を読み込むSQL
	 */
	public String createGetTableDefSql() {
		return createGetTableDefSql(getDbName());
	}

	/**
	 * テスト用にDBを復元します。<br>
	 * ScriptServiceを実行する場合、h2では毎回DB再構築が必要となります。<br>
	 * DB構成更新サービス実行に必要な最小限のテーブル定義及びレコードをSQLで流し、<br>
	 * "src/test/resources/service/script/dbmng"配下にあるDB名フォルダの資材で<br>
	 * 実際にDB構成更新を実行し、最新のDB定義に従ったDB再構築を行います。<br>
	 * 
	 * @throws Exception 例外
	 */
	public void restoreDb() throws Exception {

		// 初回はDB名が取得されていない場合があるため、おまじない
		getDbName();

		// テストに必要なフォルダを作成する
		prepareOutputDir();

		// DB構成更新サービス実行に必要な最小限のテーブル定義及びレコードをSQLで流す
		initDbAndRecord();

		// DB構成更新サービスを実行する
		updateAllTable(RESOURCE_PATH + "service/script/dbmng/");
	}

	/**
	 * DB復元のため、DB構成更新サービス実行に必要な最小限のテーブル定義と<br>
	 * 及びレコードを投入します。<br>
	 * 
	 * @throws Exception 例外
	 */
	public void initDbAndRecord() throws Exception {

		// DB構成更新サービス実行に必要な最小限のテーブル定義及びレコードを投入する
		String initSqlPath = RESOURCE_PATH + "service/script/init/10_init.sql";
		doDbUpdateBySqlFileService(initSqlPath, "initResult");
	}

	private void doDbUpdateBySqlFileService(String sqlFilePath, String resultKey) throws Exception {

		//　ファイルとして用意されているupdateのSQLを実行する
		var input = new GenericParam();
		input.setDb(getDb());
		input.putString("sqlFilePath", sqlFilePath);
		input.putString("resultKey", "resultKey");
		var output = new GenericParam();
		var service = new DbUpdateBySqlFileService();
		service.doService(input, output);
	}

	/**
	 * 基準パスで示されるディレクトリにある資材を使い、DB構成更新を実行します。<br>
	 * 
	 * @param basePath 基準パス
	 * @throws Exception 例外
	 */
	public void updateAllTable(String basePath) throws Exception {

		// 必要なパラメータを準備する
		String scriptId = "1000002"; // DB構成更新
		String dirPath = basePath + dbName;
		String defPath = "10_dbdef";
		String dataPath = "20_dbdata";
		String sqlPath = "30_sql";
		String authorizedPath = "10_authorized";
		String autoCreatedPath = "20_auto_created";
		String forUpdatePath = "30_for_update";

		// 正常系動作確認に必要なパラメータを作成する
		var input = new GenericParam();
		input.setDb(getDb());
		input.putString("scriptId", scriptId);
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("dataPath", dataPath);
		input.putString("sqlPath", sqlPath);
		input.putString("authorizedPath", authorizedPath);
		input.putString("autoCreatedPath", autoCreatedPath);
		input.putString("forUpdatePath", forUpdatePath);

		// DB構成更新サービスを実行する
		var output = new GenericParam();
		var service = new ScriptService();
		service.doService(input, output);
	}

	/**
	 * 基準パスで示されるディレクトリにある資材を使い、DB構成取得を実行します。<br>
	 * 
	 * @param basePath 基準パス
	 * @throws Exception 例外
	 */
	public void getAllTable(String basePath) throws Exception {

		// 必要なパラメータを準備する
		String scriptId = "1000001"; // DB構成取得
		String dirPath = basePath + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String dataPath = "20_dbdata/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";
		String getTableNameListSql = createGetTableNameListSql();
		String getTableDefSql = createGetTableDefSql();

		// 正常系動作確認に必要なパラメータを作成する
		GenericParam input = new GenericParam();
		input.setDb(getDb());
		input.putString("scriptId", scriptId);
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("dataPath", dataPath);
		input.putString("sqlPath", sqlPath);
		input.putString("getTableNameListSql", getTableNameListSql);
		input.putString("getTableDefSql", getTableDefSql);

		// DB構成更新サービスを実行する
		var output = new GenericParam();
		var service = new ScriptService();
		service.doService(input, output);
	}
}
