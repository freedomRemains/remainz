package com.remainz.web.exception;

import com.remainz.common.exception.BusinessRuleViolationException;

public class RoleRestrictionException extends BusinessRuleViolationException {

	/** デフォルトシリアルバージョン */
	private static final long serialVersionUID = 1L;

	/**
	 * ロール制約例外を初期化します。
	 *
	 * @param e
	 *            例外
	 */
	public RoleRestrictionException(Exception e) {

		// スーパークラスのコンストラクタを呼び出す
		super(e);
	}

	/**
	 * ロール制約例外を初期化します。
	 *
	 * @param msg
	 *            エラーメッセージ
	 */
	public RoleRestrictionException(String msg) {

		// スーパークラスのコンストラクタを呼び出す
		super(msg);
	}
}
