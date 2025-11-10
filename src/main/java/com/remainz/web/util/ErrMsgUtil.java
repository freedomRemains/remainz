package com.remainz.web.util;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.remainz.common.db.DbInterface;
import com.remainz.common.util.Cu;

public class ErrMsgUtil {

	public String getErrMsgKey(DbInterface db, String sessionId, String accountId,
			String mgnrkeyvalId, Object... args) {

		String errMsg = "0";
		try {
			// 汎用グループIDをキーとして、エラーメッセージを取得する
			errMsg = getErrMsg(db, mgnrkeyvalId, args);

		} catch (SQLException e) {

			// 固定値を返却する
			return errMsg;
		}

		// エラーメッセージキーを取得する
		return getErrMsgKeyByMsg(db, sessionId, accountId, errMsg);
	}

	public String getErrMsg(DbInterface db, String mgnrkeyvalId, Object... args) throws SQLException {

		// 汎用グループIDをキーとして、エラーメッセージを取得する
		var errMsgList = db.select("SELECT GNR_VAL FROM GNR_KEY_VAL WHERE GNR_KEY_VAL_ID = " + mgnrkeyvalId);
		return MessageFormat.format(errMsgList.get(0).get("GNR_VAL"), args);
	}

	public String getErrMsgKeyByMsg(DbInterface db, String sessionId, String accountId,
			String errMsg) {

		try {
			// エラーメッセージIDの ( 最大値 + 1 ) を取得する
			var maxIdRecord = db.select("SELECT MAX(ERR_MSG_ID) FROM ERR_MSG");
			String maxErrMsgIdStr = maxIdRecord.get(0).get("MAX(ERR_MSG_ID)");
			if (Cu.isEmpty(maxErrMsgIdStr)) {
				maxErrMsgIdStr = "0";
			}
			int maxErrMsgId = Integer.parseInt(maxErrMsgIdStr) + 1;

			// 現在日付を文字列として取得する
			var dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String currentDate = dateFormat.format(new Date());

			// エラーメッセージテーブルにレコードを追加する
			StringBuilder sql = new StringBuilder();
			sql.append("INSERT INTO ERR_MSG(ERR_MSG_ID, SESSION_ID, ACCNT_ID, ERR_MSG, VERSION, IS_DELETED, CREATED_BY, CREATED_AT, UPDATED_BY, UPDATED_AT) VALUES(");
			sql.append(Integer.toString(maxErrMsgId) + ", '" + sessionId + "', " + accountId + ", '" + errMsg + "', 1, 0, '" + accountId + "', '" + currentDate + "', '" + accountId + "', '" + currentDate + "')");
			db.update(sql.toString());

			// エラーメッセージテーブルに登録したレコードのIDを呼び出し側に返却する
			return Integer.toString(maxErrMsgId);

		} catch (SQLException e) {

			// 固定値を返却する
			return "0";
		}
	}
}
