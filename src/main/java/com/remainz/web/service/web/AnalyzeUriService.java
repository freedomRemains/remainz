package com.remainz.web.service.web;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

import com.remainz.common.db.DbInterface;
import com.remainz.common.exception.ApplicationInternalException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.service.ServiceInterface;
import com.remainz.common.service.script.ScriptService;
import com.remainz.common.util.InputCheckUtil;
import com.remainz.common.util.LogUtil;
import com.remainz.common.util.Mu;

public class AnalyzeUriService implements ServiceInterface {

	/** ロガー */
	private transient Logger logger = Logger.getLogger(this.getClass().getName());

	@Override
	public void doService(GenericParam input, GenericParam output) {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkDb(input);
		inputCheckUtil.checkParam(input, "requestKind");
		inputCheckUtil.checkParam(input, "requestUri");

		try {
			// URIを解析し、サービスを実行する
			doAnalyzeUri(input, output);

			// ロール制約がかかっているか確認するため、しばらくこのコードはコメントとする
			// (ロール制約のエラーページ表示になれば、ロール制約がかかっている)
//		} catch (RoleRestrictionException e) {
//
//			// ロール制約違反の場合は、トップページに遷移させる
//			output = new GenericParam();
//			output.putString("respKind", "redirect");
//			output.putString("destination", "/remainz/service/top.html");

		} catch (Exception e) {

			// 例外をスローする
			throw new ApplicationInternalException(new LogUtil().handleException(e));
		}
	}

	private void doAnalyzeUri(GenericParam input, GenericParam output) throws Exception {

		// 入力パラメータのログを記録する
		input.recordLog(logger, new Mu().msg("msg.AnalyzeUriService.inputLog"));

		// リスエスト種別及びリクエストURIをキーとして、スクリプトIDを取得する
		String scriptId = getScriptId(input.getDb(), input.getString("requestKind"),
				input.getString("requestUri"));

		// スクリプトサービスにより、業務ロジックを実行する
		input.putString("scriptId", scriptId);
		var service = new ScriptService();
		service.doService(input, output);

		// 出力パラメータのログを記録する
		output.recordLog(logger, new Mu().msg("msg.AnalyzeUriService.outputLog"));
	}

	private String getScriptId(DbInterface db, String requestKind, String requestUri)
			throws Exception {

		// URIパターンをキーとしてページ情報を検索し、スクリプトIDを取得する
		String sql = """
				SELECT
					A.HTML_PAGE_ID, A.PAGE_NAME, A.SCR_ID_GET, A.SCR_ID_POST,
					A.SCR_ID_PUT, A.SCR_ID_DELETE
				FROM HTML_PAGE A
				LEFT JOIN URI_PATTERN B ON A.URI_PATTERN_ID = B.URI_PATTERN_ID
				WHERE B.URI_PATTERN = ?
				""";
		var paramList = new ArrayList<String>();
		paramList.add(requestUri);
		ArrayList<LinkedHashMap<String, String>> recordList = db.select(sql, paramList);

		// レコードが0件、もしくは2件以上の場合は、エラー画面に遷移する
		if (recordList.size() == 0 || recordList.size() > 1) {
			throw new ApplicationInternalException(new Mu().msg("msg.err.invalidRequestUri", requestUri));
		}

		// リクエスト種別に応じたスクリプトIDを呼び出し側に返却する
		if ("GET".equals(requestKind) && !"0".equals(recordList.get(0).get("SCR_ID_GET"))) {
			return recordList.get(0).get("SCR_ID_GET");
		} else  if ("POST".equals(requestKind) && !"0".equals(recordList.get(0).get("SCR_ID_POST"))) {
			return recordList.get(0).get("SCR_ID_POST");
		} else  if ("PUT".equals(requestKind) && !"0".equals(recordList.get(0).get("SCR_ID_PUT"))) {
			return recordList.get(0).get("SCR_ID_PUT");
		} else  if ("DELETE".equals(requestKind) && !"0".equals(recordList.get(0).get("SCR_ID_DELETE"))) {
			return recordList.get(0).get("SCR_ID_DELETE");
		} else {
			throw new ApplicationInternalException(new Mu().msg("msg.err.invalidRequestKind",
					requestUri, requestKind));
		}
	}
}
