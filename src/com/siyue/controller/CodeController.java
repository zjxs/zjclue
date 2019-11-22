package com.siyue.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.server.bind.annotation.Controller;
import com.actionsoft.bpms.server.bind.annotation.Mapping;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.siyue.service.CodeService;
import com.siyue.util.StringUtils;
import com.siyue.vo.RemoteResult;

@Controller
public class CodeController {

	private CodeService codeService = new CodeService();

	/**
	 * 包装Code
	 * 
	 * @param table
	 * @return
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.packageCode4Select")
	public String packageCode4Select(String table) {
		Map<String, Map<String, String>> codeMap = CodeService.getCodeMap(table);
		List<Map<String, String>> dataList = Lists.newArrayList();
		for (String code : codeMap.keySet()) {
			String name = codeMap.get(code).get("NAME");
			Map<String, String> dataMap = Maps.newHashMap();
			dataMap.put("label", name);
			dataMap.put("value", code);
			dataList.add(dataMap);
		}
		RemoteResult remoteResult = new RemoteResult();
		try {
			remoteResult.setResultData(dataList);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	@Mapping(value = "com.awspaas.user.apps.clue.packageCode4Tree")
	public String packageCode4Tree(Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		String code = StringUtils.nvlString(jsonMap.get("code"));
		String table = StringUtils.nvlString(jsonMap.get("table"));
		String search = StringUtils.nvlString(jsonMap.get("search"));
		String lev = StringUtils.emptyConvert((jsonMap.get("lev")), "0");
		List<Map<String, Object>> resultList = Collections.EMPTY_LIST;
		if (StringUtils.isBlank(search)) {
			resultList = codeService.getTreeCombo(code, table, lev);
		} else {
			resultList = codeService.searchTreeCombo(table, search);
		}
		RemoteResult remoteResult = new RemoteResult();
		try {
			remoteResult.setResultData(resultList);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	@Mapping(value = "com.awspaas.user.apps.clue.getTreeComboForTreeSelect")
	public String getTreeComboForTreeSelect(Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		String code = StringUtils.nvlString(jsonMap.get("code"));
		String table = StringUtils.nvlString(jsonMap.get("table"));
		String search = StringUtils.nvlString(jsonMap.get("search"));
		String lev = StringUtils.emptyConvert((jsonMap.get("lev")), "0");
		List<Map<String, Object>> resultList = Collections.EMPTY_LIST;
		if (StringUtils.isBlank(search)) {
			resultList = codeService.getTreeComboForTreeSelect(code, table, lev);
		} else {
			resultList = codeService.searchTreeCombo(table, search);
		}
		RemoteResult remoteResult = new RemoteResult();
		try {
			remoteResult.setResultData(resultList);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	@Mapping(value = "com.awspaas.user.apps.clue.getTreeComboForTreeSelectAll")
	public String getTreeComboForTreeSelectAll(Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		String code = StringUtils.nvlString(jsonMap.get("code"));
		String table = StringUtils.nvlString(jsonMap.get("table"));
		String search = StringUtils.nvlString(jsonMap.get("search"));
		String lev = StringUtils.emptyConvert((jsonMap.get("lev")), "0");
		List<Map<String, Object>> resultList = Collections.EMPTY_LIST;
		if (StringUtils.isBlank(search)) {
			resultList = codeService.getTreeComboForTreeSelectAll(code, table, lev);
		} else {
			resultList = codeService.searchTreeCombo(table, search);
		}
		RemoteResult remoteResult = new RemoteResult();
		try {
			remoteResult.setResultData(resultList);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	@Mapping(value = "com.awspaas.user.apps.clue.packageCode4Tree_new")
	public String packageCode4Tree(String tableName, String parentCode, String parentLev, String searchWord) {
		List<Map<String, Object>> resultList = Collections.EMPTY_LIST;
		if (StringUtils.isBlank(searchWord)) {
			resultList = codeService.getTreeCombo(parentCode, tableName, StringUtils.emptyConvert((parentLev), "0"));
		} else {
			resultList = codeService.searchTreeCombo(tableName, searchWord);
		}
		RemoteResult remoteResult = new RemoteResult();
		try {
			remoteResult.setResultData(resultList);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	@Mapping(value = "com.awspaas.user.apps.clue.packageCode4Tree_select")
	public String packageCode4Tree_select(Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		String table = StringUtils.nvlString(jsonMap.get("table"));
		String search = StringUtils.nvlString(jsonMap.get("search"));
		List<Map<String, Object>> resultList = Collections.EMPTY_LIST;
		if (StringUtils.isBlank(search)) {
			resultList = codeService.searchData(table, search);
		} else {
			resultList = codeService.searchData(table, search);
		}
		RemoteResult remoteResult = new RemoteResult();
		try {
			remoteResult.setResultData(resultList);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

}
