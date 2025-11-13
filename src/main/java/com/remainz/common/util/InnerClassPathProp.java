package com.remainz.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

import com.remainz.common.exception.ApplicationInternalException;

/**
 * クラスパス内(クラスローダーでリソースとして取得できる範囲)にあるプロパティファイルを読み込むクラスです。
 * warの中に含まれるプロパティを読み込むときに使用します。
 * warに含まれるJavaクラスは通常src配下にソースコードを置き、eclipseの自動ビルド等によりbinやclasses、build
 * といったフォルダにクラスファイルが生成されます。 C:\(中略)\webapps\remainz\src
 * C:\(中略)\webapps\remainz\src\application.properties C:\(中略)\webapps\remainz\bin
 * C:\(中略)\webapps\remainz\bin\application.properties
 * プロパティファイルは通常「src」直下に配置し、ビルドすると「bin」配下などの決まった出力先に出力されます。
 * このクラスでは「src」直下に配置し、ビルドにより「bin」や「classes」、「build」などに出力されたプロパティ
 * ファイルを読み込むことができます。 warファイル内に含まれているプロパティファイルを読み込むときは、本クラスを使用してください。
 */
public class InnerClassPathProp {

	/** プロパティファイル名 */
	private String propFileName;

	/** プロパティ */
	private Properties properties;

	/** 最後に検出したエラー */
	private String lastError;

	/**
	 * コンストラクタ(引数なし)
	 */
	public InnerClassPathProp() {

		// メンバを初期化する
		lastError = null;
	}

	/**
	 * コンストラクタ(引数あり)
	 *
	 * @param propertyFileName プロパティファイル名
	 */
	public InnerClassPathProp(String propertyFileName) {

		// 引数なしコンストラクタを実行する
		this();

		try {
			// 初期化処理を実行する
			init(propertyFileName);

		} catch (ApplicationInternalException e) {

			// エラー発生時はログを記録する
			new LogUtil().handleException(e);
		}
	}

	/**
	 * プロパティファイルをロードします。
	 * 
	 * @param propertyFileName プロパティファイル名
	 */
	public void init(String propertyFileName) {

		// プロパティファイル名をメンバに保存する
		propFileName = propertyFileName;

		// 引数がnullもしくはから文字列の場合は例外とする
		if (propertyFileName == null || propertyFileName.length() == 0) {
			throw new ApplicationInternalException(
					MessageFormat.format("[No property file name]{0}", propertyFileName));
		}

		try {
			// プロパティをロードする
			load(propertyFileName);

		} catch (Exception e) {

			// 例外発生時はログを記録し、エラー内容をメンバに保存する
			LogUtil logUtil = new LogUtil();
			lastError = logUtil.getStackTrace(e);
			logUtil.handleException(e);
		}

		// エラーを検出している場合はアプリケーション内部例外をスローする
		if (!Cu.isEmpty(lastError)) {
			throw new ApplicationInternalException(lastError);
		}
	}

	/**
	 * プロパティをロードします。
	 *
	 * @param propertyFileName プロパティファイル名
	 */
	private void load(String propertyFileName) throws IOException {

		properties = new Properties();
		InputStream is = null;
		try {
			// プロパティファイルのリソースを取得する
			is = getClass().getClassLoader().getResourceAsStream(propertyFileName);

			// プロパティファイルが見つからない場合は、例外とする
			if (is == null) {
				throw new ApplicationInternalException(
						MessageFormat.format("[Property file not found]{0}", propertyFileName));
			}

			// プロパティをロードする
			properties.load(is);

		} finally {

			// ファイルをクローズする
			if (is != null) {
				is.close();
			}
		}
	}

	/**
	 * プロパティを取得します。
	 *
	 * @param name キー
	 * @return 値
	 */
	public String get(String key) {
		return properties.getProperty(key);
	}

	/**
	 * プロパティファイル名を取得します。
	 * 
	 * @return プロパティファイル名
	 */
	public String getPropFileName() {
		return propFileName;
	}

	/**
	 * キーが示すメッセージを取得し、 可変引数を織り込んだ文字列を呼び出し側に返却します。
	 * 
	 * @param key  キー
	 * @param args 可変引数
	 * @return 可変引数を織り込んだメッセージを返却します。
	 */
	public String msg(String key, Object... args) {
		return MessageFormat.format(get(key), args);
	}
}
