package com.remainz.common.exception;

/**
 * ビジネスルール違反例外
 */
public class BusinessRuleViolationException extends RuntimeException {

	/** デフォルトシリアルバージョン */
	private static final long serialVersionUID = 1L;

	/**
	 * ビジネスルール違反例外を初期化します。
	 *
	 * @param e
	 *            例外
	 */
	public BusinessRuleViolationException(Exception e) {

		// スーパークラスのコンストラクタを呼び出す
		super(e);
	}

	/**
	 * ビジネスルール違反例外を初期化します。
	 *
	 * @param msg
	 *            エラーメッセージ
	 */
	public BusinessRuleViolationException(String msg) {

		// スーパークラスのコンストラクタを呼び出す
		super(msg);
	}
}
