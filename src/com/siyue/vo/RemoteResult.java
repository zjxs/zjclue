package com.siyue.vo;

import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.commons.mvc.view.ResponseObject;

public class RemoteResult<T> {
	
	/**
	 * 结果状态
	 */
	private boolean status;

	/**
	 * 结果数据信息
	 */
	private Object resultData;

	/**
	 * 结果提示信息
	 */
	private String resultMsg;

	/**
	 * 结果信息编号，对应字典
	 */
	private String resultCode;

	public RemoteResult() {
		super();
	}

	public RemoteResult(boolean status, Object resultData, String resultMsg, String resultCode) {
		super();
		this.status = status;
		this.resultData = resultData;
		this.resultMsg = resultMsg;
		this.resultCode = resultCode;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public Object getResultData() {
		replaceNull4Result(resultData,null);
		return resultData;
	}

	public void setResultData(Object resultData) {
		this.resultData = resultData;
	}

	public String getResultMsg() {
		return resultMsg;
	}

	public void setResultMsg(String resultMsg) {
		this.resultMsg = resultMsg;
	}

	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	@Override
	public String toString() {
		ResponseObject ro;
		if(this.isStatus()) {
			ro = ResponseObject.newOkResponse();
		}else {
			ro = ResponseObject.newErrResponse();
		}
		
		ro.msg(this.getResultMsg());
		ro.setData(this.getResultData());
		return ro.toString();
	}
	
	/**
	 * 替换查询结果集中的null为""
	 * @param result
	 * @param row
	 */
	private void replaceNull4Result(Object result,Map<String,Object> row) {
		if(result instanceof List) {
			List<Object> resultList = (List<Object>) result;
			for(Object subResult : resultList) {
				replaceNull4Result(subResult,null);
			}
		}else if(result instanceof Map) {
			Map<String,Object> resultMap = (Map<String, Object>) result;
			for(Object subResult : resultMap.values()) {
				replaceNull4Result(subResult,resultMap);
			}
		}else {
			if(row != null) {
				for(String key : row.keySet()) {
					if (row.get(key) == null || row.get(key) instanceof java.sql.Date) {
						row.put(key, "");
					}
				}
			}
		}
	}
}
