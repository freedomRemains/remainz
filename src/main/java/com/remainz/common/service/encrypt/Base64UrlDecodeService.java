package com.remainz.common.service.encrypt;

import java.nio.charset.Charset;
import java.util.Base64;

import com.remainz.common.param.GenericParam;
import com.remainz.common.service.ServiceInterface;
import com.remainz.common.util.InputCheckUtil;
import com.remainz.common.util.RcProp;

public class Base64UrlDecodeService implements ServiceInterface {

	@Override
	public void doService(GenericParam input, GenericParam output) {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkParam(input, "target");

		// Base64のURLエンコードを実行する
		doBase64UrlDecoce(input, output);
	}

	private void doBase64UrlDecoce(GenericParam input, GenericParam output) {

		// 入力パラメータにchaset指定がない場合は、デフォルト値を適用する
		String charset = input.getString("charset");
		if (charset == null) {
			charset = new RcProp().get("default.charset");
		}

		// Base64のURLデコードを実行し、出力パラメータに結果を設定する
		byte[] urlDecodeResult = Base64.getUrlDecoder().decode(
				input.getString("target").getBytes(Charset.forName(charset)));
		output.putString("target", new String(urlDecodeResult, Charset.forName(charset)));
	}
}
