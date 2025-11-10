package com.remainz.web.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.remainz.common.db.DbInterface;

/**
 * 権限ユーティリティ
 */
public class AuthUtil {

	/**
	 * アカウントIDをキーとして、権限を取得します。
	 * 
	 * @param db DB
	 * @param accountId アカウントID
	 * @return アカウントに紐づく権限のリスト
	 * @throws Exception 例外
	 */
	public ArrayList<LinkedHashMap<String, String>> getAuthByAccountId(
			DbInterface db, String accountId) throws Exception {

		// アカウントに紐づく全ての権限を取得する
		String sql = """
				SELECT
					B.HTML_PARTS_ID, B.AUTH_KIND
				FROM APROLE_IN_ACCNT A
				LEFT JOIN HTML_PARTS_IN_APROLE B ON A.APROLE_ID = B.APROLE_ID
				WHERE A.ACCNT_ID = ?
				ORDER BY B.HTML_PARTS_ID
				""";
		var paramList = new ArrayList<String>();
		paramList.add(accountId);
		return db.select(sql, paramList);
	}

	/**
	 * ユーザが権限を持っているかどうか判定します。
	 * 
	 * @param mhtmlpartsId HTMLパーツマスタID
	 * @param authList ユーザに紐づく権限のリスト
	 * @return ユーザが権限を持っている場合はtrue、そうでない場合はfalse
	 */
	public boolean hasAuth(String mhtmlpartsId, ArrayList<LinkedHashMap<String, String>> authList) {
		for (LinkedHashMap<String, String> columnMap : authList) {
			if (columnMap.get("HTML_PARTS_ID").equals(mhtmlpartsId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * ユーザがread権限を持っているかどうか判定します。
	 * 
	 * @param mhtmlpartsId HTMLパーツマスタID
	 * @param authList ユーザに紐づく権限のリスト
	 * @return ユーザが権限を持っている場合はtrue、そうでない場合はfalse
	 */
	public boolean hasReadAuth(String mhtmlpartsId, ArrayList<LinkedHashMap<String, String>> authList) {
		for (LinkedHashMap<String, String> columnMap : authList) {
			if (columnMap.get("HTML_PARTS_ID").equals(mhtmlpartsId)
					&& columnMap.get("AUTH_KIND").equals("read")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * ユーザがedit権限を持っているかどうか判定します。
	 * 
	 * @param mhtmlpartsId HTMLパーツマスタID
	 * @param authList ユーザに紐づく権限のリスト
	 * @return ユーザが権限を持っている場合はtrue、そうでない場合はfalse
	 */
	public boolean hasEditAuth(String mhtmlpartsId, ArrayList<LinkedHashMap<String, String>> authList) {
		for (LinkedHashMap<String, String> columnMap : authList) {
			if (columnMap.get("HTML_PARTS_ID").equals(mhtmlpartsId)
					&& columnMap.get("AUTH_KIND").equals("edit")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * アカウントIDをキーとして、ロールを取得します。
	 * 
	 * @param db DB
	 * @param accountId アカウントID
	 * @return アカウントに紐づくロールのリスト
	 * @throws Exception 例外
	 */
	public ArrayList<LinkedHashMap<String, String>> getRoleByAccountId(
			DbInterface db, String accountId) throws Exception {

		// アカウントに紐づく全ての権限を取得する
		String sql = """
				SELECT
					A.APROLE_ID, B.ROLE_NAME
				FROM APROLE_IN_ACCNT A
				LEFT JOIN APROLE B ON A.APROLE_ID = B.APROLE_ID
				WHERE A.ACCNT_ID = ?
				ORDER BY A.APROLE_ID
				""";
		var paramList = new ArrayList<String>();
		paramList.add(accountId);
		return db.select(sql, paramList);
	}

	/**
	 * ユーザがロールを持っているかどうか判定します。
	 * 
	 * @param roleId HTMLパーツマスタID
	 * @param roleList ユーザに紐づく権限のリスト
	 * @return ユーザが権限を持っている場合はtrue、そうでない場合はfalse
	 */
	public boolean hasRole(String roleId, ArrayList<LinkedHashMap<String, String>> roleList) {
		for (LinkedHashMap<String, String> columnMap : roleList) {
			if (columnMap.get("APROLE_ID").equals(roleId)) {
				return true;
			}
		}
		return false;
	}
}
