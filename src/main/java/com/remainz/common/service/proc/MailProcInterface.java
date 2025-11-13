package com.remainz.common.service.proc;

import javax.mail.Message;

import com.remainz.common.param.GenericParam;

/**
 * メールプロシージャのインターフェースです。
 */
public interface MailProcInterface {

	void doMailProc(GenericParam input, GenericParam output, Message mail) throws Exception;
}
