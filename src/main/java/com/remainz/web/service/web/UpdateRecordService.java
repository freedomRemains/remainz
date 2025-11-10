package com.remainz.web.service.web;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.apache.log4j.Logger;

import com.remainz.common.exception.ApplicationInternalException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.service.ServiceInterface;
import com.remainz.common.util.InputCheckUtil;
import com.remainz.common.util.LogUtil;
import com.remainz.common.util.Mu;
import com.remainz.web.util.ErrMsgUtil;

public class UpdateRecordService implements ServiceInterface {

	/** ロガー */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	@Override
	public void doService(GenericParam input, GenericParam output) {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkDb(input);
		inputCheckUtil.checkParam(input, "tableName");
		inputCheckUtil.checkParam(input, "recordId");

		try {
			// DBレコードを更新する
			if (!doUpdateRecord(input, output)) {

				// エラーメッセージIDを取得する
				String errMsgKey = new ErrMsgUtil().getErrMsgKey(input.getDb(),
						input.getString("sessionId"), input.getString("accountId"),
						"1000301");

				// レコード編集ページにリダイレクトする
				String tableName = input.getString("tableName");
				String recordId = input.getString("recordId");
				String uri = "tableDataMainte/editRecord.html?tableName=" + tableName
						+ "&recordId=" + recordId + "&errMsgKey=" + errMsgKey;
				output.putString("respKind", "redirect");
				output.putString("destination", uri);
			}

		} catch (Exception e) {
			throw new ApplicationInternalException(new LogUtil().handleException(e));
		}
	}

	private boolean doUpdateRecord(GenericParam input, GenericParam output) throws Exception {

		// 現在日付を文字列として準備する
		var dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = dateFormat.format(new Date());

		// 入力パラメータ中の除外対象文字列を定義する
		String tableName = input.getString("tableName");
		String accountId = input.getString("accountId");
		String[] excludes = new String[] {"tableName", "recordId", "accountId", "requestKind",
				"requestUri", "scriptId", tableName + "_ID", "sessionId", "errMsgKey"};
		var excludeList = Arrays.asList(excludes);

		// DBレコード更新のSQLを生成する
		StringBuilder sql = new StringBuilder("UPDATE " + tableName + " SET ");

		// 入力パラメータを元に、更新内容のSQLを作成する
		StringBuilder temp = new StringBuilder();
		var paramList = new ArrayList<String>();
		boolean hasVersion = false;
		boolean hasUpdateDate = false;
		for (String key : input.getStringMapKeySet()) {

			// 除外対象の入力パラメータを検知した場合は、ループの先頭に戻る
			if (excludeList.contains(key)) {
				continue;
			}

			// UPDATEのSQLを作る
			if (temp.length() > 0) {
				temp.append(", ");
			}
			temp.append(key + " = ?");

			if ("VERSION".equals(key)) {

				// VERSIONの場合は、プラス1した値を設定する
				hasVersion = true;
				Integer version = Integer.valueOf(input.getString(key));
				version = version + 1;
				paramList.add(version.toString());

			} else if ("UPDATED_AT".equals(key)) {

				// UPDATED_ATの場合は、現在日時をパラメータに設定する
				hasUpdateDate = true;
				paramList.add(dateString);

			} else if ("UPDATED_BY".equals(key)) {

				// UPDATED_BYの場合は、アカウント名をパラメータに設定する
				paramList.add(accountId);

			} else {
				paramList.add(input.getString(key));
			}
		}
		sql.append(temp);

		// 楽観ロック条件を追加する
		sql.append(" WHERE " + tableName + "_ID = ?");
		paramList.add(input.getString("recordId"));
		if (hasVersion) {
			sql.append(" AND VERSION = ?");
			paramList.add(input.getString("VERSION"));
		} else if (hasUpdateDate) {
			sql.append(" AND UPDATED_AT = ?");
			paramList.add(input.getString("UPDATED_AT"));
		}

		// SQLをログに記録する
		logger.info(new Mu().msg("msg.common.sql", sql));
		logger.info(new Mu().msg("msg.common.sqlParam", paramList.toString()));	

		// DBレコードを更新する
		Integer updateCnt = input.getDb().update(sql.toString(), paramList);

		if (updateCnt == 0) {

			// レコードを更新できていない場合は、楽観ロックエラーとみなす
			return false;

		} else {

			// テーブル名とレコードIDを出力パラメータに設定する
			output.putString("tableName", tableName);
			output.putString("recordId", input.getString("recordId"));
			output.putString("updateCnt", updateCnt.toString());

			// レコードを更新できている場合は、戻り値trueで呼び出し側に復帰する
			return true;
		}
	}
}
