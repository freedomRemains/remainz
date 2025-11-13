package com.remainz.common.service.adapter;

import com.remainz.common.param.GenericParam;
import com.remainz.common.service.AdapterInterface;

/**
 * 汎用パラメータのアダプターの機能を提供するクラスです。
 */
public class GenericAdapter implements AdapterInterface {

	/**
	 * from -> to へのコピーを行うアダプターです。
	 *
	 * @param from コピー元パラメータ
	 * @param to コピー先パラメータ
	 */
	public void doAdapt(GenericParam from, GenericParam to) {

		// コピー元パラメータのマップ(パラメータ名(キー)と文字列(値)のマップ)を全て処理するまでループ
		for (String key : from.getStringMapKeySet()) {

			// コピー先パラメータが持っていない値だけをコピーする
			if (to.getString(key) == null) {
				to.putString(key, from.getString(key));
			}
		}

		// コピー元パラメータのマップ(文字列配列のマップ)を全て処理するまでループ
		for (String key : from.getStringArrayMapKeySet()) {

			// コピー先パラメータが持っていない値だけをコピーする
			if (to.getStringArray(key) == null) {
				to.putStringArray(key, from.getStringArray(key));
			}
		}

		// コピー元パラメータのマップ(DB検索結果のマップ)を全て処理するまでループ
		for (String key : from.getRecordListMapKeySet()) {

			// コピー先パラメータが持っていない値だけをコピーする
			if (to.getRecordList(key) == null) {
				to.putRecordList(key, from.getRecordList(key));
			}
		}
	}
}
