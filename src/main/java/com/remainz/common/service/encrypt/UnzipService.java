package com.remainz.common.service.encrypt;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

import com.remainz.common.exception.ApplicationInternalException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.service.ServiceInterface;
import com.remainz.common.util.FileUtil;
import com.remainz.common.util.InputCheckUtil;
import com.remainz.common.util.LogUtil;

/**
 * zipを解凍します。
 */
public class UnzipService implements ServiceInterface {

	/** ロガー */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	@Override
	public void doService(GenericParam input, GenericParam output) {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkParam(input, "targetZip");
		inputCheckUtil.checkParam(input, "outputDir");

		// zip解凍を実行する
		doUncompressDir(input, output);
	}

	private void doUncompressDir(GenericParam input, GenericParam output) {

		// zip解凍対象のフォルダを開く
		File outputDir = new File(input.getString("outputDir"));

		// zip入力ストリームを作成する
		try (ZipInputStream zipInputStream = new FileUtil().getZipInputStream(
				input.getString("targetZip"))) {

			// 再帰的にフォルダをたどってzip解凍する
			doUncompressDir(outputDir, zipInputStream);

		} catch (IOException e) {

			// 例外をスローする 
			throw new ApplicationInternalException(new LogUtil().handleException(e));
		}
	}

	private void doUncompressDir(File outputDir, ZipInputStream zipInputStream) throws IOException {

		// zipファイル内のエントリを全て処理するまでループ
		ZipEntry zipEntry = null;
		while ((zipEntry = zipInputStream.getNextEntry()) != null) {

			// 解凍対象のファイルを取得する
			File uncompressFile = new File(outputDir + PATH_DELM + zipEntry.getName());
			logger.info("[uncompressFile]" + uncompressFile.getPath());

			// zipエントリがディレクトリである場合
			if (zipEntry.isDirectory()) {

				// ディレクトリを作成する
				new FileUtil().mkdirs(uncompressFile);

			} else {

				// zipエントリがファイルである場合は、ファイル出力ストリームを生成する
				try (BufferedOutputStream bufferedOutputStream = new FileUtil().getBufferedOutputStream(
						uncompressFile.getPath())) {

					// zipエントリの内容をファイルに書き込む
					byte[] buf = new byte[8192];
					int bytesRead = 0;
					while ((bytesRead = zipInputStream.read(buf)) != -1) {
						bufferedOutputStream.write(buf, 0, bytesRead);
					}
				}
			}
		}
	}
}
