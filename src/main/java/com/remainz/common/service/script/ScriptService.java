package com.remainz.common.service.script;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.remainz.common.param.GenericParam;
import com.remainz.common.service.AdapterInterface;
import com.remainz.common.service.PrepareInterface;
import com.remainz.common.service.ServiceInterface;
import com.remainz.common.util.Cu;
import com.remainz.common.util.InputCheckUtil;
import com.remainz.common.util.Mu;

/**
 * スクリプトで複数のサービスを連続実行します。
 */
public class ScriptService implements ServiceInterface {

	/** ロガー */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	@Override
	public void doService(GenericParam input, GenericParam output) throws Exception {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkDb(input);
		inputCheckUtil.checkParam(input, "scriptId");

		// DBからスクリプト定義を取得し、スクリプトを実行する
		doScript(input, output);
	}

	private void doScript(GenericParam input, GenericParam output) throws Exception {

		// DBからスクリプト定義を取得する
		String sql = """
				SELECT A.SCR_NAME, B.SCR_ELM_ID, B.SERVICE_NAME, B.ADAPTER, B.PREPARE_INPUT, B.ORD_IN_GRP
				  FROM SCR A
				  INNER JOIN SCR_ELM B ON A.SCR_ID = B.SCR_ID
				    AND A.SCR_ID = #{scriptId}
				  ORDER BY B.ORD_IN_GRP
				""";
		sql = sql.replace("#{scriptId}", input.getString("scriptId"));
		logger.info(new Mu().msg("msg.common.sql", sql));

		// スクリプトパラメータを取得する
		getScriptParam(input, input.getString("scriptId"));

		// SQLを実行する
		ArrayList<LinkedHashMap<String, String>> recordList = input.getDb().select(sql);

		// 読み込んだ全てのスクリプト定義を処理するまでループ
		for (LinkedHashMap<String, String> columnMap : recordList) {

			// サービスインスタンスを生成し、サービスを実行する
			String className = columnMap.get("SERVICE_NAME");
			ServiceInterface service = (ServiceInterface) Class.forName(className).getConstructor().newInstance();
			logger.info(new Mu().msg("msg.common.service", className));
			service.doService(input, output);

			// 指定されたアダプタ処理を呼び出す(なければデフォルトのGenericAdapter)
			className = columnMap.get("ADAPTER");
			if (Cu.isEmpty(className)) {
				className = "com.remainz.common.service.adapter.GenericAdapter";
			}
			AdapterInterface adapter = (AdapterInterface) Class.forName(className).getConstructor().newInstance();
			logger.info(new Mu().msg("msg.common.adapter", className));
			adapter.doAdapt(output, input);

			// 指定された入力パラメータ準備処理を呼び出す(なければ何もしない)
			className = columnMap.get("PREPARE_INPUT");
			if (Cu.isEmpty(className)) {
				continue;
			}
			PrepareInterface prepare = (PrepareInterface) Class.forName(className).getConstructor().newInstance();
			logger.info(new Mu().msg("msg.common.prepare", className));
			prepare.doPrepare(input, output);
		}
	}

	private void getScriptParam(GenericParam input, String scriptId) throws Exception {

		// DBからスクリプトパラメータを取得する
		String sql = """
				SELECT A.SCR_NAME, B.SCR_PRM_ID, B.PARAM_KEY, B.PARAM_VALUE, B.ORD_IN_GRP
				  FROM SCR A
				  INNER JOIN SCR_PRM B ON A.SCR_ID = B.SCR_ID
				    AND A.SCR_ID = #{scriptId}
				  ORDER BY B.ORD_IN_GRP
				""";
		sql = sql.replace("#{scriptId}", input.getString("scriptId"));
		logger.info(new Mu().msg("msg.common.sql", sql));

		// SQLを実行する
		ArrayList<LinkedHashMap<String, String>> recordList = input.getDb().select(sql);

		// 読み込んだ全てのスクリプトパラメータを処理するまでループ
		for (LinkedHashMap<String, String> columnMap : recordList) {

			// パラメータキーとパラメータ値を取得する
			String paramKey = columnMap.get("PARAM_KEY");
			String paramValue = columnMap.get("PARAM_VALUE");

			// パラメータ値については変数部分を変換する
			paramValue = convertVariable(input, paramValue, logger);

			// 入力パラメータに上書きとなる場合は、警告ログを記録し、上書きしないようにする
			if (Cu.isNotEmpty(input.getString(paramKey))) {
				logger.warn(new Mu().msg("msg.warn.valueAlreadyExists", paramKey, paramValue));
				continue;
			}

			// 入力パラメータにパラメータキーとパラメータ値を設定する
			input.putString(paramKey, paramValue);
		}
	}

	public static String convertVariable(GenericParam input, String paramValue, Logger logger) {

		// パラメータ値がnullの場合は何もしない
		if (paramValue == null) {
			return null;
		}

		// パラメータ値から #{} で囲まれた文字列を抽出する
		String after = paramValue;
		String regex = "[^#]*(#\\{([0-9a-zA-Z]*)\\})";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(paramValue);

		// 抽出した文字列を全て処理するまでループ
		while (matcher.find()) {

			// #{} で囲まれた変数部分(inputのキー)を取得する(グループカウント最大のものが目的の値)
			String key = matcher.group(matcher.groupCount());

			// 入力パラメータからキーに対応する値を取得する
			String value = input.getString(key);
			if (Cu.isEmpty(value)) {

				// 入力パラメータにデータがない場合は、警告のログを出力する
				logger.warn(new Mu().msg("msg.warn.valueNotFound", key));
				continue;
			}

			// パラメータ値を変換する
			after = after.replaceAll("#\\{" + key + "\\}", value);
		}

		// 変換後の文字列を呼び出し側に返却する
		return after;
	}
}
