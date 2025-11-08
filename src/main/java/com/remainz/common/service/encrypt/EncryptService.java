package com.remainz.common.service.encrypt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.remainz.common.exception.ApplicationInternalException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.service.ServiceInterface;
import com.remainz.common.util.FileUtil;
import com.remainz.common.util.InputCheckUtil;
import com.remainz.common.util.LogUtil;

public class EncryptService implements ServiceInterface {

	private SecureRandom secureRandom;

	public EncryptService() {

		// メンバ変数を初期化する
		secureRandom = new SecureRandom();
	}

	@Override
	public void doService(GenericParam input, GenericParam output) {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkParam(input, "filePath");
		inputCheckUtil.checkParam(input, "outputDir");
		inputCheckUtil.checkParam(input, "encryptKind");

		// SHA256による暗号化を実行する
		doEncryptFile(input, output);
	}

	private void doEncryptFile(GenericParam input, GenericParam output) {

		// ファイルを読み込む
		String filePath = input.getString("filePath");
		try (BufferedInputStream zipFile = new FileUtil().getBufferedInputStream(filePath)) {

			// ファイルパスから出力ファイルパスを作成する
			String encryptResultFilePath = filePath + "_encrypt.bin";

			// 出力ファイルを作成する
			try (BufferedOutputStream encryptResultFile = new FileUtil().getBufferedOutputStream(
					encryptResultFilePath)) {

				// 暗号化種別を取得する
				String encryptKind = input.getString("encryptKind");

				// 秘密鍵を生成する
				SecretKey secretKey = generateSecretKey(encryptKind);

				// 初期化ベクトルを生成する
				IvParameterSpec iv = generateIv();

				// 暗号化を実行する
				doEncrypt(zipFile, encryptResultFile, encryptKind, secretKey, iv);

				// 秘密鍵をファイルとして保存する
				String secretKeyFilePath = saveSecretKeyAsFile(filePath, secretKey);

				// ivをファイルとして保存する
				String ivFilePath = saveIvAsFile(filePath, iv);

				// 出力パラメータに秘密鍵ファイルとivファイル、暗号化ファイルのファイルパスを設定する
				output.putString("secretKeyFilePath", secretKeyFilePath);
				output.putString("ivFilePath", ivFilePath);
				output.putString("encryptResultFilePath", encryptResultFilePath);
			}

		} catch (Exception e) {
			throw new ApplicationInternalException(new LogUtil().handleException(e));
		}
	}

	private SecretKey generateSecretKey(String encryptKind) throws Exception {

		// キージェネレータを取得する
		KeyGenerator keyGenerator = KeyGenerator.getInstance(encryptKind);

		// キージェネレータを初期化する(方式ごとに指定できる数値が決まっており、間違ったものを指定すると例外となる)
		keyGenerator.init(128);

		// 秘密鍵を生成し、呼び出し側に返却する
		return keyGenerator.generateKey();
	}

	private IvParameterSpec generateIv() {

		// 乱数を生成する
		byte[] iv = new byte[16];
		secureRandom.nextBytes(iv);

		// 生成した乱数を元に初期化ベクトル(iv)を生成して、呼び出し側に返却する
		return new IvParameterSpec(iv);
	}

	private void doEncrypt(BufferedInputStream targetFile, BufferedOutputStream encryptResultFile,
			String encryptKind, SecretKey secretKey, IvParameterSpec iv) throws Exception {

		// メモリが枯渇しないよう、一定のデータ量ごとに処理を行う
		byte[] buf = new byte[8192];
		int bytesRead = 0;
		while ((bytesRead = targetFile.read(buf, 0, buf.length)) != -1) {

			// 暗号化対象のbyte配列を新たに作成し、ファイルから読み込んだデータをコピーする
			byte[] target = new byte[bytesRead];
			System.arraycopy(buf, 0, target, 0, bytesRead);

			// ファイルから読み込んだデータをログに記録する
			new LogUtil().recordBytesLog("[target]", target);

			// 暗号化を実行する
			byte[] encryptResult = encrypt(secretKey, iv, encryptKind, target);

			// 暗号化後のデータをログに記録する
			new LogUtil().recordBytesLog("[encryptResult]", encryptResult);

			// 出力ファイルに暗号化後の文字列を書き込む
			encryptResultFile.write(encryptResult);
		}
	}

	private byte[] encrypt(SecretKey secretKey, IvParameterSpec iv, String encryptKind, byte[] target)
			throws Exception {

		// "アルゴリズム/ブロックモード/パディング方式"の書式でエンクリプタを取得し、初期化する
		Cipher encrypter = Cipher.getInstance("AES/CBC/PKCS5Padding");
		encrypter.init(Cipher.ENCRYPT_MODE, secretKey, iv);

		// 暗号化を実行し、結果を呼び出し側に返却する
		return encrypter.doFinal(target);
	}

	private String saveSecretKeyAsFile(String filePath, SecretKey secretKey)
			throws Exception {

		// 秘密鍵をbyte配列として取得する
		byte[] secretKeyEncoded = secretKey.getEncoded();

		// 秘密鍵ファイルのファイルパスを生成する
		String secretKeyFilePath = filePath + "_encrypt.key";

		// 秘密鍵をファイルに書き込む
		try (BufferedOutputStream secretKeyFile = new FileUtil().getBufferedOutputStream(secretKeyFilePath)) {
			secretKeyFile.write(secretKeyEncoded);
		}

		// 秘密鍵ファイルのパスを呼び出し側に返却する
		return secretKeyFilePath;
	}

	private String saveIvAsFile(String filePath, IvParameterSpec iv) throws Exception {

		// ivをbyte配列として取得する
		byte[] ivBytes = iv.getIV();

		// ivファイルのファイルパスを生成する
		String ivFilePath = filePath + "_encrypt.iv";

		// 秘密鍵をファイルに書き込む
		try (BufferedOutputStream ivFile = new FileUtil().getBufferedOutputStream(ivFilePath)) {
			ivFile.write(ivBytes);
		}

		// 秘密鍵ファイルのパスを呼び出し側に返却する
		return ivFilePath;
	}
}
