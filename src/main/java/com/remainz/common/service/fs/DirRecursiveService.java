package com.remainz.common.service.fs;

import com.remainz.common.param.GenericParam;
import com.remainz.common.service.ServiceInterface;
import com.remainz.common.service.proc.DirProcInterface;
import com.remainz.common.util.InputCheckUtil;

/**
 * ディレクトリを再帰的に処理していくサービスです。<br>
 * このサービスはディレクトリごとにinput、outputの内容を取り回して処理するため、<br>
 * 呼び出し側でinputとoutputを上位から引き継がずに呼び出すと、混乱やバグを防ぎやすいです。<br>
 * <br>
 * [input][必須][dirPath]ディレクトリパス<br>
 * [input][必須][procName]プロシージャ名(ディレクトリごとの処理を記述するクラスの名前)<br>
 * [output]inputのprocNameで指定したクラスにより、出力内容が変わる。<br>
 */
public class DirRecursiveService implements ServiceInterface {

	@Override
	public void doService(GenericParam input, GenericParam output) throws Exception {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkParam(input, "dirPath");
		inputCheckUtil.checkParam(input, "procName");

		// プロシージャ名からプロシージャを取得できなかった場合は、エラーとする
		DirProcInterface proc = (DirProcInterface) Class.forName(input.getString("procName")).getConstructor().newInstance();

		// ファイルプロシージャを呼び出す
		proc.doProc(input, output, input.getString("dirPath"));
	}
}
