package com.remainz.web.service.web;

import org.apache.log4j.Logger;

import com.remainz.common.exception.ApplicationInternalException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.service.ServiceInterface;
import com.remainz.common.util.InputCheckUtil;
import com.remainz.common.util.LogUtil;

public class BulkDeleteRecordService implements ServiceInterface {

	/** ロガー */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	@Override
	public void doService(GenericParam input, GenericParam output) {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkDb(input);
		inputCheckUtil.checkParam(input, "tableName");

		try {
			// DBレコードを一括削除する
			doBulkDeleteRecord(input, output);

		} catch (Exception e) {
			throw new ApplicationInternalException(new LogUtil().handleException(e));
		}
	}

	private void doBulkDeleteRecord(GenericParam input, GenericParam output) throws Exception {

		// 入力パラメータにある文字列を全て処理するまでループ
		for (String key : input.getStringMapKeySet()) {

			// チェックボックス ON のマップエントリを見つけた場合
			if ("on".equals(input.getString(key))) {

				// キーがレコードIDとなっているので、入力パラメータに設定する
				GenericParam in = new GenericParam();
				in.setDb(input.getDb());
				in.putString("tableName", input.getString("tableName"));
				in.putString("recordId", key);

				// DBレコードを削除する
				GenericParam out = new GenericParam();
				DeleteRecordService.doDeleteRecord(in, out, logger);

				// 出力パラメータを設定する
				String recordId = output.getString("recordId");
				if (recordId == null) {
					recordId = key;
				} else {
					recordId += ", " + key;
				}
				output.putString("recordId", recordId);
				Integer updateCnt = null;
				if (output.getString("updateCnt") == null) {
					updateCnt = Integer.valueOf(1);
				} else {
					updateCnt = Integer.parseInt(output.getString("updateCnt"));
					updateCnt++;
				}
				output.putString("updateCnt", updateCnt.toString());
			}
		}

		// テーブル名とレコードIDを出力パラメータに設定する
		output.putString("tableName", input.getString("tableName"));
	}
}
