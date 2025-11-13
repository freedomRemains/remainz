package com.remainz.common.util;

/**
 * 共通ユーティリティクラスです。
 * staticメソッドを配置し、なるべく短い名前で呼び出せるようクラス名を短くしています。
 */
public class Cu {

	public static boolean isEmpty(String target) {
		if (target == null || target.length() == 0) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isNotEmpty(String target) {
		return !isEmpty(target);
	}

	public boolean isEmptyString(String target) {
		return isEmpty(target);
	}

	public boolean isNotEmptyString(String target) {
		return isNotEmpty(target);
	}

	public static String convertForFileName(String fileName) {

		// ファイル名として使用できない文字を別の文字に置き換える
		String trueFileName = fileName;
		trueFileName = trueFileName.replaceAll("\\\\", "￥");
		trueFileName = trueFileName.replaceAll("/", "／");
		trueFileName = trueFileName.replaceAll(":", "：");
		trueFileName = trueFileName.replaceAll("\\*", "＊");
		trueFileName = trueFileName.replaceAll("\\?", "？");
		trueFileName = trueFileName.replaceAll("\"", "”");
		trueFileName = trueFileName.replaceAll("\\<", "＜");
		trueFileName = trueFileName.replaceAll("\\>", "＞");
		trueFileName = trueFileName.replaceAll("\\|", "｜");

		// 変換後のファイル名を呼び出し側に返却する
		return trueFileName;
	}

	public static boolean hasCtrlCode(String target) throws Exception {

		// String型のデータに含まれるbyte配列を全て検査するまでループ
		for (int index = 0; index < target.length(); index++) {

			// 全角文字は読み飛ばす
			String targetChar = target.substring(index, index + 1);
			if (targetChar.getBytes(new RcProp().get("default.charset")).length > 1) {
				continue;
			}

			// 改行(0x0D、0x0A)、タブ(0x09)は許容する
			char charAt = target.charAt(index);
			if (charAt == 0x0D || charAt == 0x0A || charAt == 0x09) {
				continue;
			}

			// 制御文字が含まれている場合は、戻り値trueで呼び出し側に復帰する
			if (charAt <= 0x1F || charAt == 0x7F) {
				return true;
			}
		}

		// ここまで制御が到達した場合は制御文字なしと判断し、戻り値falseで呼び出し側に復帰する
		return false;
	}
}
