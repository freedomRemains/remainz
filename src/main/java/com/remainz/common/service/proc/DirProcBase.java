package com.remainz.common.service.proc;

import java.io.File;

import com.remainz.common.exception.BusinessRuleViolationException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.util.Mu;

public class DirProcBase implements DirProcInterface {

	@Override
	public void doProc(GenericParam input, GenericParam output, String dirPath) throws Exception {

		// ディレクトリ再帰処理を実行する
		doParentDirProc(input, output, dirPath);
	}

	protected void doParentDirProc(GenericParam input, GenericParam output, String dirPath) throws Exception {

		// dirPathが示すディレクトリ(処理対象となる親ディレクトリ)が存在しなければエラーとする
		File dir = new File(dirPath);
		if (!dir.exists()) {
			throw new BusinessRuleViolationException(new Mu().msg("msg.common.noDir", dirPath));
		}

		// ディレクトリを再帰的に処理する
		doDirProcRecursive(input, output, dirPath, dir, dir);
	}

	protected void doDirProcRecursive(GenericParam input, GenericParam output, String dirPath, File parentDir,
			File currentDir) throws Exception {

		// ディレクトリ内にファイルがない場合は、即時終了する
		File[] childFiles = currentDir.listFiles();
		if (childFiles == null) {
			return ;
		}

		// ディレクトリ内の全ての要素(ディレクトリ、ファイル)を処理するまでループ
		for (File file : childFiles) {

			if (file.isDirectory()) {

				// 子ディレクトリを検出した場合は、子ディレクトリの処理を呼び出す
				doDirProcRecursive(input, output, dirPath, currentDir, file);

			} else {

				// ファイルを検出した場合は、ファイルの処理を呼び出す
				doFileProc(input, output, dirPath, currentDir, file);
			}
		}
	}

	protected void doFileProc(GenericParam input, GenericParam output, String dirPath, File parentDir,
			File currentFile) throws Exception {
	}
}
