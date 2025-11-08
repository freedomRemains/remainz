package com.remainz.common.service.encrypt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.remainz.common.exception.ApplicationInternalException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.service.ServiceInterface;
import com.remainz.common.util.FileUtil;
import com.remainz.common.util.InputCheckUtil;
import com.remainz.common.util.LogUtil;

public class DecryptService implements ServiceInterface {

	@Override
	public void doService(GenericParam input, GenericParam output) {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkParam(input, "secretKeyFilePath");
		inputCheckUtil.checkParam(input, "ivFilePath");
		inputCheckUtil.checkParam(input, "encryptResultFilePath");
		inputCheckUtil.checkParam(input, "outputDir");
		inputCheckUtil.checkParam(input, "encryptKind");

		// SHA256による復号化を実行する
		doDecryptFile(input, output);
	}

	private void doDecryptFile(GenericParam input, GenericParam output) {

		// ファイルから秘密鍵を取得する
		SecretKey secretKey = getSecretKeyFromFile(input.getString("secretKeyFilePath"),
				input.getString("encryptKind"));

		// ファイルからivを取得する
		IvParameterSpec iv = getIvFromFile(input.getString("ivFilePath"), input.getString("encryptKind"));

		// 暗号化結果ファイルを開く
		String encryptResultFilePath = input.getString("encryptResultFilePath");

		// ファイルから暗号化データを読み込む
		try (BufferedInputStream encryptResultFile = new FileUtil().getBufferedInputStream(
				encryptResultFilePath)) {
			decryptFile(encryptResultFilePath, encryptResultFile, secretKey, iv,
					input.getString("encryptKind"));
		} catch (Exception e) {
			throw new ApplicationInternalException(new LogUtil().handleException(e));
		}
	}

	private SecretKey getSecretKeyFromFile(String secretKeyFilePath, String encryptKind) {

		// 秘密鍵ファイルを開く
		try (BufferedInputStream secretKeyFileStream = new FileUtil().getBufferedInputStream(
				secretKeyFilePath)) {

			// ファイルから秘密鍵を読み込む
			byte[] base64SecretKey = secretKeyFileStream.readAllBytes();

			// 秘密鍵を復元する
			SecretKey secretKey = new SecretKeySpec(base64SecretKey, encryptKind);

			// 秘密鍵を呼び出し側に返却する
			return secretKey;

		} catch (IOException e) {
			throw new ApplicationInternalException(new LogUtil().handleException(e));
		}
	}

	private IvParameterSpec getIvFromFile(String ivFilePath, String encryptKind) {

		// ivファイルを開く
		try (BufferedInputStream secretKeyFileStream = new FileUtil().getBufferedInputStream(
				ivFilePath)) {

			// ファイルからivを読み込む
			byte[] iv = secretKeyFileStream.readAllBytes();

			// ivを復元する
			IvParameterSpec ivSpec = new IvParameterSpec(iv);

			// ivを呼び出し側に返却する
			return ivSpec;

		} catch (IOException e) {
			throw new ApplicationInternalException(new LogUtil().handleException(e));
		}
	}

	private void decryptFile(String encryptResultFilePath, BufferedInputStream encryptResultFile,
			SecretKey secretKey, IvParameterSpec iv, String encryptKind) throws Exception {

		// 暗号化結果ファイル名から出力ファイル名を生成する
		String outputFilePath = encryptResultFilePath + "_decrypt.bin";
		if (encryptResultFilePath.endsWith("_encrypt.bin")) {
			outputFilePath = encryptResultFilePath.substring(0,
					encryptResultFilePath.lastIndexOf("_encrypt.bin"));
		}

		// 出力ファイルを開く
		try (BufferedOutputStream outputFile = new FileUtil().getBufferedOutputStream(outputFilePath)) {

			// メモリが枯渇しないよう、一定のデータ量ごとに処理を行う
			byte[] buf = new byte[8192];
			int bytesRead = 0;
			while ((bytesRead = encryptResultFile.read(buf, 0, buf.length)) != -1) {

				// 復号対象のbyte配列を新たに作成し、ファイルから読み込んだデータをコピーする
				byte[] target = new byte[bytesRead];
				System.arraycopy(buf, 0, target, 0, bytesRead);

				// 復号対象のデータをログに記録する
				new LogUtil().recordBytesLog("[decryptTarget]", target);

				// 暗号化データを復号する
				byte[] decryptResult = decrypt(secretKey, iv, encryptKind, target);

				// 復号したデータをログに記録する
				new LogUtil().recordBytesLog("[decryptResult]", decryptResult);

				// 復号したデータを出力ファイルに書き込む
				outputFile.write(decryptResult);
			}
		}
	}

	private byte[] decrypt(SecretKey secretKey, IvParameterSpec iv, String encryptKind, byte[] target)
			throws Exception {

		// "アルゴリズム/ブロックモード/パディング方式"の書式でデクリプタを取得し、初期化する
		Cipher decrypter = Cipher.getInstance("AES/CBC/PKCS5Padding");
		decrypter.init(Cipher.DECRYPT_MODE, secretKey, iv);

		// 復号化を実行し、結果を呼び出し側に返却する
		return decrypter.doFinal(target);
	}
}
