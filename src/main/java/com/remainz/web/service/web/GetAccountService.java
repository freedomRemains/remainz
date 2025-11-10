package com.remainz.web.service.web;

import java.util.ArrayList;

import com.remainz.common.db.DbInterface;
import com.remainz.common.exception.ApplicationInternalException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.service.ServiceInterface;
import com.remainz.common.util.Cu;
import com.remainz.common.util.InputCheckUtil;
import com.remainz.common.util.LogUtil;
import com.remainz.web.exception.RoleRestrictionException;
import com.remainz.web.util.AuthUtil;

public class GetAccountService implements ServiceInterface {

	/** デフォルトアカウントID(ゲストアカウント) */
	private static final String DEFAULT_ACCNT_ID = "1000001";

	@Override
	public void doService(GenericParam input, GenericParam output) {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkDb(input);
		inputCheckUtil.checkParam(input, "requestKind");
		inputCheckUtil.checkParam(input, "requestUri");

		try {
			// HTMLを生成する
			doGetAccount(input, output);

		} catch (RoleRestrictionException e) {

			// ロール制約例外を検出した場合は、ログを記録して例外をそのままスローする
			new LogUtil().handleException(e);
			throw e;

		} catch (Exception e) {
			throw new ApplicationInternalException(new LogUtil().handleException(e));
		}
	}

	private void doGetAccount(GenericParam input, GenericParam output) throws Exception {

		// 入力パラメータにアカウントIDがない場合は、デフォルト値を適用する
		if (Cu.isEmpty(input.getString("accountId"))) {
			input.putString("accountId", DEFAULT_ACCNT_ID);
		}

		// アカウント情報を取得する
		getAccount(input.getDb(), input.getString("accountId"), output);

		// アカウントに紐づく権限を取得し、出力パラメータに設定する
		var authList = new AuthUtil().getAuthByAccountId(input.getDb(), input.getString("accountId"));
		output.putRecordList("authList", authList);

		// アクセスしようとしているURLにロールによる制約があるか確認する
		checkRequireRole(input.getDb(), input.getString("requestUri"), input.getString("accountId"),
				output);
	}

	private void getAccount(DbInterface db, String accountId, GenericParam output) throws Exception {

		// アカウント情報を取得する
		String sql = """
				SELECT
					A.ACCNT_ID, A.ACCOUNT_NAME, A.MAIL_ADDRESS,
					A.VERSION, A.IS_DELETED, A.CREATED_BY, A.CREATED_AT,
					A.UPDATED_BY, A.UPDATED_AT
				FROM ACCNT A
				WHERE A.ACCNT_ID = ?
				""";
		var paramList = new ArrayList<String>();
		paramList.add(accountId);
		var recordList = db.select(sql, paramList);

		// 出力パラメータにアカウント情報を設定する
		output.putRecordList("account", recordList);
	}

	private void checkRequireRole(DbInterface db, String requestUri, String accountId,
			GenericParam output) throws Exception {

		// ページに対するロール制約を取得する
		String sql = """
				SELECT
					B.APROLE_ID
				FROM HTML_PAGE A
				LEFT JOIN REQUIRE_APROLE B ON A.HTML_PAGE_ID = B.HTML_PAGE_ID
				LEFT JOIN URI_PATTERN C ON A.URI_PATTERN_ID = C.URI_PATTERN_ID
				WHERE C.URI_PATTERN = ?
				GROUP BY B.APROLE_ID
				ORDER BY B.APROLE_ID
				""";
		var paramList = new ArrayList<String>();
		paramList.add(requestUri);
		var recordList = db.select(sql, paramList);

		// アカウントに紐づくロールを取得する
		var authUtil = new AuthUtil();
		var roleList = authUtil.getRoleByAccountId(db, accountId);

		// 取得した全てのロール制約のうち、いずれかのロールを持っている場合は即時終了する
		for (var columnMap : recordList) {

			// そもそもロール制約がない場合はロール制約違反なしと判断し、即時終了する
			if (Cu.isEmpty(columnMap.get("APROLE_ID"))) {
				return;
			}

			// アカウントがロールを持っていればロール制約違反なしと判断し、即時終了する
			if (authUtil.hasRole(columnMap.get("APROLE_ID"), roleList)) {
				return;
			}
		}

		// ロール制約内のいずれのロールも持っていない場合は、例外をスローする
		throw new RoleRestrictionException(recordList.toString());
	}
}
