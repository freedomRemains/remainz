package com.remainz.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

/**
 * ファイルに関するユーティリティクラスです。
 */
public class FileUtil {

	/** ロガー */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * ファイルパスを指定し、デフォルトエンコーディングでテキストファイルの読み込みインスタンスを取得します。
	 *
	 * @param filePath ファイルパス
	 * @return テキストファイルの読み込みインスタンス
	 * @throws UnsupportedEncodingException サポートされていないエンコーディング例外
	 * @throws FileNotFoundException        ファイルが見つからない例外
	 */
	public BufferedReader getBufferedReader(String filePath)
			throws UnsupportedEncodingException, FileNotFoundException {

		// デフォルトエンコーディングでテキストファイルの読み込みインスタンスを生成して呼び出し側に戻す
		return getBufferedReader(filePath, new RcProp().get("default.charset"));
	}

	/**
	 * ファイルパスとエンコーディングを指定し、テキストファイルの読み込みインスタンスを取得します。
	 *
	 * @param filePath ファイルパス
	 * @param encoding エンコーディング
	 * @return テキストファイルの読み込みインスタンス
	 * @throws UnsupportedEncodingException サポートされていないエンコーディング例外
	 * @throws FileNotFoundException        ファイルが見つからない例外
	 */
	public BufferedReader getBufferedReader(String filePath, String encoding)
			throws UnsupportedEncodingException, FileNotFoundException {

		// システムのエンコーディングと異なる場合はログを出力する
		String systemEncoding = System.getProperty("file.encoding");
		if (!systemEncoding.equalsIgnoreCase(encoding)) {
			logger.info("Specified encoding and system encoding are different. [encoding]" + encoding + " [systemEncoding]" + systemEncoding);
		}

		// テキストファイルの読み込みインスタンスを生成して呼び出し側に戻す
		return new BufferedReader(new InputStreamReader(new FileInputStream(filePath), encoding));
	}

	/**
	 * ファイルパスを指定し、デフォルトエンコーディングでテキストファイルの書き込みインスタンスを取得します。 上書き作成のメソッドです。
	 *
	 * @param filePath ファイルパス
	 * @param append   追記ならtrueを指定します
	 * @return テキストファイルの書き込みインスタンス
	 * @throws UnsupportedEncodingException サポートされていないエンコーディング例外
	 * @throws FileNotFoundException        ファイルが見つからない例外
	 */
	public BufferedWriter getBufferedWriter(String filePath)
			throws UnsupportedEncodingException, FileNotFoundException {
		return getBufferedWriter(filePath, false);
	}

	/**
	 * ファイルパスを指定し、デフォルトエンコーディングでテキストファイルの書き込みインスタンスを取得します。
	 *
	 * @param filePath ファイルパス
	 * @param append   追記ならtrueを指定します
	 * @return テキストファイルの書き込みインスタンス
	 * @throws UnsupportedEncodingException サポートされていないエンコーディング例外
	 * @throws FileNotFoundException        ファイルが見つからない例外
	 */
	public BufferedWriter getBufferedWriter(String filePath, boolean append)
			throws UnsupportedEncodingException, FileNotFoundException {
		return new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(filePath, append), new RcProp().get("default.charset")));
	}

	/**
	 * ファイルパス、追記モード、エンコーディングを指定し、テキストファイルの書き込みインスタンスを取得します。
	 *
	 * @param filePath ファイルパス
	 * @param append   追記ならtrueを指定します
	 * @param encoding エンコーディング
	 * @return テキストファイルの書き込みインスタンス
	 * @throws UnsupportedEncodingException サポートされていないエンコーディング例外
	 * @throws FileNotFoundException        ファイルが見つからない例外
	 */
	public BufferedWriter getBufferedWriter(String filePath, boolean append, String encoding)
			throws UnsupportedEncodingException, FileNotFoundException {
		return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath, append), encoding));
	}

	/**
	 * ファイルパスを指定し、zip出力ストリームのインスタンスを取得します。
	 *
	 * @param filePath ファイルパス
	 * @return zip出力ストリームのインスタンス
	 * @throws FileNotFoundException ファイルが見つからない例外
	 */
	public ZipOutputStream getZipOutputStream(String filePath) throws FileNotFoundException {
		return new ZipOutputStream(new FileOutputStream(new File(filePath)));
	}

	/**
	 * ファイルパスを指定し、zip入力ストリームのインスタンスを取得します。
	 *
	 * @param filePath ファイルパス
	 * @return zip入力ストリームのインスタンス
	 * @throws FileNotFoundException ファイルが見つからない例外
	 */
	public ZipInputStream getZipInputStream(String filePath) throws FileNotFoundException {
		return new ZipInputStream(new FileInputStream(new File(filePath)));
	}

	/**
	 * ファイルパスを指定し、zip出力ストリームのインスタンスを取得します。
	 *
	 * @param filePath ファイルパス
	 * @return zip出力ストリームのインスタンス
	 * @throws FileNotFoundException ファイルが見つからない例外
	 */
	public BufferedInputStream getBufferedInputStream(String filePath) throws FileNotFoundException {
		return new BufferedInputStream(new FileInputStream(new File(filePath)));
	}

	/**
	 * ファイルパスを指定し、zip出力ストリームのインスタンスを取得します。
	 *
	 * @param filePath ファイルパス
	 * @return zip出力ストリームのインスタンス
	 * @throws FileNotFoundException ファイルが見つからない例外
	 */
	public BufferedOutputStream getBufferedOutputStream(String filePath) throws FileNotFoundException {
		return new BufferedOutputStream(new FileOutputStream(new File(filePath)));
	}

	public void createDirIfNotExists(String dirPath) {

		// ディレクトリが存在する場合は即時終了する
		File dir = new File(dirPath);
		if (dir.exists()) {
			return;
		}

		// ディレクトリを作成する
		mkdirs(dir);
	}

	public void mkdirs(File targetDir) {

		// 最大3回リトライして、ディレクトリ作成を試す
		for (int i = 0; i < 3; i++) {
			if (targetDir.mkdirs()) {
				break;
			}
		}
	}

	public void deleteDirIfExists(String dirPath) {

		// ディレクトリが存在しないか、ファイルの場合は削除する
		File dir = new File(dirPath);
		if (!dir.exists() || dir.isFile()) {
			return;
		}

		// ディレクトリを削除する
		deleteDirRecursive(dir);
	}

	public void deleteDirRecursive(File parentDir) {

		// 親ディレクトリの子のリストを取得する
		File[] files = parentDir.listFiles();

		// 子のリストがない場合は、親ディレクトリを削除して即時終了する
		if (files == null || files.length == 0) {
			deleteFileOrDir(parentDir);
			return;
		}

		// 子のリストを全て処理するまでループ
		for (File file : files) {

			if (file.isDirectory()) {

				// 子がディレクトリならば、再帰的に処理を呼び出す
				deleteDirRecursive(file);

			} else {

				// 子がファイルならば、削除する
				deleteFileOrDir(file);
			}
		}

		// 子のディレクトリ／ファイルを全て削除した後は、親ディレクトリを削除する
		deleteFileOrDir(parentDir);
	}

	public void deleteFileOrDir(File fileOrDir) {

		// 最大3回リトライして、ファイル or ディレクトリの削除を試す
		for (int i = 0; i < 3; i++) {
			if (fileOrDir.delete()) {
				break;
			}
		}
	}

	public File[] listFiles(File dir) {

		// ディレクトリ配下の子ディレクトリ及び子ファイルを取得する
		File[] files = dir.listFiles();
		if (files == null) {
			return new File[0];
		}
		return files;
	}
}
