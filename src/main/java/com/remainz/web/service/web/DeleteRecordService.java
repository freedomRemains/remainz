package com.remainz.web.service.web;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.remainz.common.exception.ApplicationInternalException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.service.ServiceInterface;
import com.remainz.common.util.InputCheckUtil;
import com.remainz.common.util.LogUtil;
import com.remainz.common.util.Mu;
import com.remainz.web.util.ErrMsgUtil;

public class DeleteRecordService implements ServiceInterface {

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
			// DBレコードを削除する
			if (!doDeleteRecord(input, output, logger)) {

				// エラーメッセージIDを取得する
				String errMsgKey = new ErrMsgUtil().getErrMsgKey(input.getDb(),
						input.getString("sessionId"), input.getString("accountId"),
						"1000301");

				// DBメンテナンスページにリダイレクトする
				String tableName = input.getString("tableName");
				String uri = "tableDataMainte.html?tableName=" + tableName
						+ "&errMsgKey=" + errMsgKey;
				output.putString("respKind", "redirect");
				output.putString("destination", uri);
			}

		} catch (Exception e) {
			throw new ApplicationInternalException(new LogUtil().handleException(e));
		}
	}

	public static boolean doDeleteRecord(GenericParam input, GenericParam output,
			Logger logger) throws Exception {

		// DBレコード削除のSQLを生成する
		String tableName = input.getString("tableName");
		String sql = "DELETE FROM " + tableName + " WHERE " + tableName + "_ID = ?";
		var paramList = new ArrayList<String>();
		paramList.add(input.getString("recordId"));

		// SQLをログに記録する
		logger.info(new Mu().msg("msg.common.sql", sql));
		logger.info(new Mu().msg("msg.common.sqlParam", paramList.toString()));	

		// DBレコードを削除する
		Integer updateCnt = input.getDb().update(sql, paramList);

		if (updateCnt == 0) {

			// レコードを更新できていない場合は、エラーとみなす
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
