package com.remainz.web.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.remainz.common.db.DbInterface;
import com.remainz.common.param.GenericParam;
import com.remainz.common.util.DbUtil;
import com.remainz.common.util.InnerClassPathProp;
import com.remainz.common.util.LogUtil;
import com.remainz.common.util.Mu;
import com.remainz.web.service.web.AnalyzeUriService;
import com.remainz.web.util.RwProp;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * URIパターンに基づいてサービス制御を行うサーブレットクラスです。
 */
@WebServlet("/service/*")
public class ServiceControlServlet extends HttpServlet {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** ロガー */
	private transient Logger logger = Logger.getLogger(this.getClass().getName());

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// サービス制御を行う
		controllService(request, response, "GET");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// サービス制御を行う
		controllService(request, response, "POST");
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// サービス制御を行う
		controllService(request, response, "PUT");
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// サービス制御を行う
		controllService(request, response, "DELETE");
	}

	private void controllService(HttpServletRequest request, HttpServletResponse response,
			String requestKind) throws IOException, ServletException {

		// URI解析によりサービスを実行する
		GenericParam output = analyzeUri(request, response, requestKind);

		// 次のページに遷移する
		moveToNextPage(request, response, output);
	}

	private GenericParam analyzeUri(HttpServletRequest request, HttpServletResponse response,
			String requestKind) {

		// サーブレットが動作している位置を取得する場合は、次のコードを有効にしてログを確認する
		String port = Integer.toString(request.getServerPort());
		logger.info(new Mu().msg("msg.ServiceControlServlet.currentPos", request.getPathTranslated()));
		logger.info(new Mu().msg("msg.ServiceControlServlet.scheme", request.getScheme()));
		logger.info(new Mu().msg("msg.ServiceControlServlet.server", request.getServerName()));
		logger.info(new Mu().msg("msg.ServiceControlServlet.port", port));
		logger.info(new Mu().msg("msg.ServiceControlServlet.contextPath", request.getContextPath()));
		logger.info(new Mu().msg("msg.ServiceControlServlet.servletPath", request.getServletPath()));

		// リクエスト内のURIを取得する
		String requestUri = request.getRequestURI();
		logger.info(new Mu().msg("msg.ServiceControlServlet.requestUri", requestUri));

		// DBに接続する
		DbInterface db = new DbUtil().getDb(new InnerClassPathProp("rc.properties"));
		try {
			// リクエストパラメータを入力パラメータに設定する
			var input = new GenericParam();
			setRequestParameterAsInput(request, requestKind, input);

			// セッションからアカウントIDを取得して設定する
			input.putString("accountId", (String) request.getSession().getAttribute("accountId"));

			// リクエスト種別に応じたサービスを呼び出す
			input.setDb(db);
			input.putString("requestKind", requestKind);
			input.putString("requestUri", requestUri);
			input.putString("sessionId", request.getSession().getId());
			var output = new GenericParam();
			var service = new AnalyzeUriService();
			service.doService(input, output);

			// DBをコミットする
			commitDb(db);

			// 出力パラメータを呼び出し側に返却する
			return output;

		} catch (Exception e) {

			// ロールバックする
			rollbackDb(db);

			// エラー発生時は、エラーページに遷移させる
			var output = doErrPageScript(db, requestKind, e);

			// 出力パラメータを呼び出し側に返却する
			return output;

		} finally {

			// DBをクローズする
			closeDb(db);
		}
	}

	private void setRequestParameterAsInput(HttpServletRequest request, String requestKind,
			GenericParam input) {

		// リクエストパラメータを入力パラメータに設定する
		String lineSepr = input.getLineSepr();
		StringBuilder msg = new StringBuilder("[HttpServletRequest]" + lineSepr);
		msg.append("[Attributes]" + lineSepr);
		Enumeration<String> attributeNames = request.getAttributeNames();
		while (attributeNames.hasMoreElements()) {
			String attributeName = attributeNames.nextElement();
			msg.append("\t" + attributeName + ": " + request.getAttribute(attributeName).toString()
					+ "," + lineSepr);
		}

		msg.append("[Headers]" + lineSepr);
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			msg.append("\t" + headerName + ": " + request.getHeader(headerName).toString()
					+ "," + lineSepr);
		}

		msg.append("[Parameters]" + lineSepr);
		Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String parameterName = parameterNames.nextElement();
			if ("PASSWORD".equals(parameterName)) {
				msg.append("\t" + parameterName + ": *****," + lineSepr);
			} else {
				msg.append("\t" + parameterName + ": " + request.getParameter(parameterName).toString()
						+ "," + lineSepr);
			}

			// リクエストパラメータを入力パラメータに設定する
			input.putString(parameterName, request.getParameter(parameterName).toString());
		}

		// リクエストパラメータのログを記録する
		logger.info(msg);
	}

	private void commitDb(DbInterface db) {

		try {
			// DBをコミットする
			db.commit();

		} catch (SQLException e) {

			// 通常起きえない例外のため、カバレッジ確認対象外とする
			new LogUtil().handleException(e);
		}
	}

	private void rollbackDb(DbInterface db) {

		try {
			// ロールバックする
			db.rollback();

		} catch (SQLException e) {

			// 通常起きえない例外のため、カバレッジ確認対象外とする
			new LogUtil().handleException(e);
		}
	}

	private void closeDb(DbInterface db) {

		// DBをクローズする
		try {
			db.close();

		} catch (SQLException e) {

			// 通常起きえない例外のため、カバレッジ確認対象外とする
			new LogUtil().handleException(e);
		}
	}

	private GenericParam doErrPageScript(DbInterface db, String requestKind, Exception e) {

		var output = new GenericParam();
		try {
			// エラーページのスクリプトIDを指定して、HTML生成サービスを実行する
			var input = new GenericParam();
			input.setDb(db);
			input.putString("requestKind", "GET");
			input.putString("requestUri", "/remainz/service/error.html");
			var service = new AnalyzeUriService();
			service.doService(input, output);

			// 画面表示用にスタックトレースを出力パラメータに設定する
			output.putString("stackTrace", new LogUtil().handleException(e));

			// 出力パラメータのログを記録する
			output.recordLog(logger, new Mu().msg("msg.ServiceControlServlet.doErrPageScript"));

		} catch (Exception e1) {

			// 通常起きえない、このエラーが起きた場合はデバッグすること
			new LogUtil().handleException(e1);
		}

		// 出力パラメータを呼び出し側に返却する
		return output;
	}

	private void moveToNextPage(HttpServletRequest request, HttpServletResponse response,
			GenericParam output) throws IOException, ServletException {

		// アカウントIDをセッション属性として設定する
		setAccountIdIfExists(request.getSession(), output);

		// 出力パラメータ内の文字列をリクエストの属性として設定する
		for (String key : output.getStringMapKeySet()) {
			request.setAttribute(key, output.getString(key));
		}

		// 出力パラメータ内のレコードリストをリクエストの属性として設定する
		for (String key : output.getRecordListMapKeySet()) {
			request.setAttribute(key, output.getRecordList(key));
		}

		// サービス出力パラメータから応答種別を取得する
		String respKind = output.getString("respKind");

		if ("redirect".equals(respKind)) {

			// 応答種別がリダイレクトの場合は、サービス出力パラメータの設定に基づいてリダイレクトを行う
			String redirectUrl = new RwProp().get("servlet.redirect.basepath") + output.getString("destination");
			logger.info(new Mu().msg("msg.ServiceProvider.redirectUrl", redirectUrl));
			response.sendRedirect(redirectUrl);

		} else if ("forward".equals(respKind)) {

			// 応答種別がフォワードの場合は、サービス出力パラメータの設定に基づいてフォワード処理を行う
			String forwardPath = new RwProp().get("servlet.forward.basepath") + output.getString("destination");
			logger.info(new Mu().msg("msg.ServiceProvider.forwardPath", forwardPath));
			RequestDispatcher requestDispatcher = request.getRequestDispatcher(forwardPath);
			requestDispatcher.forward(request, response);

		} else {

			// それ以外の場合は警告のログを記録してデフォルトページにリダイレクトする
			logger.warn("unknown respKind.  respKind=" + respKind);
			RwProp rwProp = new RwProp();
			String redirectUrl = rwProp.get("servlet.redirect.basepath") + rwProp.get("servlet.defaultPage");
			response.sendRedirect(redirectUrl);
		}
	}

	private void setAccountIdIfExists(HttpSession session, GenericParam output) {

		// 出力パラメータにアカウント情報が存在しない場合は何もしない
		var account = output.getRecordList("account");
		if (account == null) {
			return;
		}

		// セッションにアカウントIDを設定する
		session.setAttribute("accountId", account.get(0).get("ACCNT_ID"));
	}
}
