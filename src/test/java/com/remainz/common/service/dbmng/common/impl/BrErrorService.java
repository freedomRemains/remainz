package com.remainz.common.service.dbmng.common.impl;

import com.remainz.common.exception.BusinessRuleViolationException;
import com.remainz.common.param.GenericParam;
import com.remainz.common.service.ServiceInterface;

public class BrErrorService implements ServiceInterface {

	@Override
	public void doService(GenericParam input, GenericParam output) {
		throw new BusinessRuleViolationException("BusinessRuleViolationErrorDetected");
	}
}
