package com.siyue.at;

import com.actionsoft.bpms.commons.at.AbstExpression;
import com.actionsoft.bpms.commons.at.ExpressionContext;
import com.actionsoft.exception.AWSExpressionException;
import com.siyue.util.ProcessUtils;

public class GetTopLevelProcessId extends AbstExpression{

	public GetTopLevelProcessId(ExpressionContext atContext, String expressionValue) {
		super(atContext, expressionValue);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String execute(String expression) throws AWSExpressionException {
		//当前流程实例ID
		String processInstId = getParameter(expression, 1);
		return ProcessUtils.getProcessInfo(processInstId);
	}

}
