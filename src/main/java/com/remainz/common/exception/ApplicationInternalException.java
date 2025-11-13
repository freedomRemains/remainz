package com.remainz.common.exception;

/**
 * アプリケーション内部例外
 */
public class ApplicationInternalException extends RuntimeException {

	/** デフォルトシリアルバージョン */
	private static final long serialVersionUID = 1L;

	/**
	 * アプリケーション内部例外を初期化します。
	 *
	 * @param e
	 *            例外
	 */
	public ApplicationInternalException(Exception e) {

		// スーパークラスのコンストラクタを呼び出す
		super(e);
	}

	/**
	 * アプリケーション内部例外を初期化します。
	 *
	 * @param msg
	 *            エラーメッセージ
	 */
	public ApplicationInternalException(String msg) {

		// スーパークラスのコンストラクタを呼び出す
		super(msg);
	}
}
