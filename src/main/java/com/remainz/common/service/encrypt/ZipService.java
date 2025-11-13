package com.remainz.common.service.encrypt;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

import com.remainz.common.exception.ApplicationInternalException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.service.ServiceInterface;
import com.remainz.common.util.Cu;
import com.remainz.common.util.FileUtil;
import com.remainz.common.util.InputCheckUtil;
import com.remainz.common.util.LogUtil;

/**
 * ファイルもしくはディレクトリをzip圧縮します。
 * "targetDirOrFile"で対象を指定します。(ディレクトリかファイルか判別して再帰的に圧縮します)
 */
public class ZipService implements ServiceInterface {

	/** ロガー */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	@Override
	public void doService(GenericParam input, GenericParam output) {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkParam(input, "targetDirOrFile");
		inputCheckUtil.checkParam(input, "outputDir");

		// zip圧縮を実行する
		doCompress(input, output);
	}

	private void doCompress(GenericParam input, GenericParam output) {

		// zip圧縮対象を開く
		File targetDirOrFile = new File(input.getString("targetDirOrFile"));

		// zipファイル名を取得する
		String zipFileName = getZipFileName(targetDirOrFile);

		// 基準ディレクトリを取得する
		String baseDir = getBaseDir(targetDirOrFile);

		// zip出力ストリームを作成する
		String zipFilePath = input.getString("outputDir") + PATH_DELM + zipFileName + ".zip";
		try (ZipOutputStream zipOutputStream = new FileUtil().getZipOutputStream(zipFilePath)) {

			// 再帰的にフォルダをたどってzip圧縮する
			doCompressDirRecursive(targetDirOrFile, zipOutputStream, baseDir);

			// 出力パラメータにzipファイルパスを設定する
			output.putString("zipFilePath", zipFilePath);

		} catch (IOException e) {

			// 例外をスローする 
			throw new ApplicationInternalException(new LogUtil().handleException(e));
		}
	}

	private void doCompressDirRecursive(File parent, ZipOutputStream zipOutputStream, String baseDir)
			throws IOException {

		if (parent.isDirectory()) {

			// 親がディレクトリの場合は、zip出力ストリームにディレクトリのエントリを追加する
			zipOutputStream.putNextEntry(new ZipEntry(
					getRelativePath(parent.getPath(), baseDir) + "/"));

			// 子のファイルリストがなければ、即時終了する
			File[] childFiles = new FileUtil().listFiles(parent);
			if (childFiles.length == 0) {
				return;
			}

			// 子のファイルリストを全て処理するまでループ
			for (File child : childFiles) {
				doCompressDirRecursive(child, zipOutputStream, baseDir);
			}

		} else {

			// 親がファイルの場合は、zip出力ストリームにファイルのエントリを追加する
			zipOutputStream.putNextEntry(new ZipEntry(
					getRelativePath(parent.getPath(), baseDir)));

			// ファイル入力ストリームを生成する
			try (BufferedInputStream bufferedInputStream = new FileUtil().getBufferedInputStream(
					parent.getPath())) {

				// zipファイルにファイルの内容を書き込み、zip圧縮する
				byte[] buf = new byte[8192];
				int bytesRead = 0;
				while ((bytesRead = bufferedInputStream.read(buf, 0, buf.length)) != -1) {
					zipOutputStream.write(buf, 0, bytesRead);
				}
			}
		}
	}

	private String getZipFileName(File targetDirOrFile) {

		String zipFileName = null;
		if (targetDirOrFile.isAbsolute()) {

			// 対象がディレクトリの場合は、ディレクトリ名を呼び出し側に返却する
			zipFileName = targetDirOrFile.getName();

		} else {

			// 対象がファイルの場合は拡張子を除くファイル名の部分を呼び出し側に返却する
			zipFileName = targetDirOrFile.getName();
			if (zipFileName.contains(".")) {
				zipFileName = targetDirOrFile.getName().substring(0, targetDirOrFile.getName().lastIndexOf("."));
			}
		}
		logger.info("[zipFileName]" + zipFileName);

		// 生成したzipファイル名を呼び出し側に返却する
		return zipFileName;
	}

	private String getBaseDir(File targetDirOrFile) {

		// zip圧縮対象が親ディレクトリありの場合は、親ディレクトリを基準ディレクトリとする
		String baseDir = "";
		if (Cu.isNotEmpty(targetDirOrFile.getParent())) {
			baseDir = targetDirOrFile.getParentFile().getPath();
		}
		logger.info("[baseDir]" + baseDir);

		// 基準ディレクトリを呼び出し側に返却する
		return baseDir;
	}

	private String getRelativePath(String targetPath, String baseDir) {

		// baseDirからの相対パスを取得し、呼び出し側に返却する
		String relativePath = targetPath;
		if (baseDir.length() > 0) {
			relativePath = targetPath.substring(baseDir.length() + 1, targetPath.length());
		}
		logger.info("[relativePath]" + relativePath);
		return relativePath;
	}
}
