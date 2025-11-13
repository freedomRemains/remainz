package com.remainz.common.service.proc;

import java.io.BufferedReader;

import com.remainz.common.param.GenericParam;
import com.remainz.common.util.FileUtil;

public class TextFileProcBase implements TextFileProcInterface {

	@Override
	public void doProc(GenericParam input, GenericParam output, String filePath) throws Exception {

		// テキストファイルを読み込む
		readTextFile(input, output, filePath);
	}

	protected void readTextFile(GenericParam input, GenericParam output, String filePath) throws Exception {

		// ファイルを開く
		try (BufferedReader file = new FileUtil().getBufferedReader(filePath)) {

			// テキストファイル内の全ての行を処理するまでループ
			int lineNumber = 1;
			String line = "";
			while ((line = file.readLine()) != null) {

				// 1行ごとに実施する処理を呼び出す
				doProcByLine(input, output, filePath, line, lineNumber);

				// 行番号をインクリメントする
				lineNumber++;
			}

			// ファイルを読み終わったときの処理を呼び出す
			doProcOnTextFileEnd(input, output, filePath);
		}
	}

	protected void doProcByLine(GenericParam input, GenericParam output, String filePath, String line, int lineNumber) {
	}

	protected void doProcOnTextFileEnd(GenericParam input, GenericParam output, String filePath) {
	}
}
