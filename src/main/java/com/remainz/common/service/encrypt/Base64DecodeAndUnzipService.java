package com.remainz.common.service.encrypt;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.util.Base64;

import com.remainz.common.exception.ApplicationInternalException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.service.ServiceInterface;
import com.remainz.common.util.FileUtil;
import com.remainz.common.util.InputCheckUtil;
import com.remainz.common.util.LogUtil;
import com.remainz.common.util.RcProp;

public class Base64DecodeAndUnzipService implements ServiceInterface {

	@Override
	public void doService(GenericParam input, GenericParam output) {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkParam(input, "encodeResultFilePath");
		inputCheckUtil.checkParam(input, "outputDir");

		// Base64デコードしてzipファイルを生成し、更にzipファイルを解凍する
		doDecryptAndUnzipFile(input, output);
	}

	private void doDecryptAndUnzipFile(GenericParam input, GenericParam output) {

		// 入力パラメータにchaset指定がない場合は、デフォルト値を適用する
		String charset = input.getString("charset");
		if (charset == null) {
			charset = new RcProp().get("default.charset");
		}

		// 暗号化結果ファイルのファイルパスからzipファイルパスを作成する
		String encodeResultFilePath = input.getString("encodeResultFilePath");
		String zipFilePath = encodeResultFilePath.substring(0, encodeResultFilePath.lastIndexOf(
				"Encode.txt")) + ".zip";

		// ファイルから暗号化データを読み込み、zipファイルを復元する
		try (BufferedOutputStream zipFile = new FileUtil().getBufferedOutputStream(zipFilePath)) {
			getZipFileFromFile(charset, zipFile, encodeResultFilePath);
		} catch (Exception e) {
			throw new ApplicationInternalException(new LogUtil().handleException(e));
		}

		// zipファイルを解凍する
		unzip(zipFilePath, input.getString("outputDir"));

		// zipファイルを削除する
		new FileUtil().deleteFileOrDir(new File(zipFilePath));
	}

	private void getZipFileFromFile(String charset, BufferedOutputStream zipFile,
			String encodeResultFilePath) throws Exception {

		// 暗号化結果ファイルを開く
		try (BufferedReader encryptResultFile = new FileUtil().getBufferedReader(
				encodeResultFilePath)) {

			// メモリが枯渇しないよう、一定のデータ量ごとに処理を行う
			String line = "";
			while ((line = encryptResultFile.readLine()) != null) {

				// Base64デコードにより、zipデータを復元する
				byte[] urlDecodeResult = Base64.getUrlDecoder().decode(line.getBytes(charset));

				// 復元したzipデータをファイルに書き込む
				zipFile.write(urlDecodeResult);
			}

		}
	}

	private void unzip(String targetZip, String outputDir) {

		// unzipサービスを実行する
		var input = new GenericParam();
		input.putString("targetZip", targetZip);
		input.putString("outputDir", outputDir);
		var output = new GenericParam();
		var service = new UnzipService();
		service.doService(input, output);
	}
}
