package com.siyue.controller;

import java.io.IOException;
import java.util.Map;

import com.actionsoft.bpms.server.bind.annotation.Controller;
import com.actionsoft.bpms.server.bind.annotation.Mapping;
import com.actionsoft.sdk.local.SDK;
import com.actionsoft.sdk.local.api.cc.HttpAPI;
import com.alibaba.fastjson.JSONObject;
import com.siyue.util.StringUtils;

@Controller
public class OutAskController {

	@Mapping(value = "com.awspaas.user.apps.clue.getSid", session = false, noSessionEvaluate = "无安全隐患", noSessionReason = "无用户返回sid")
	public String getSid(String uid, String pwd, String type) throws IOException {
		if ("console".equals(type)) {
			return getConsoleSid(uid, pwd);
		}
		String createClientSession = SDK.getPortalAPI().createClientSession(uid, pwd);
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse(createClientSession);
		Map<String, Object> data = (Map<String, Object>) jsonMap.get("data");
		return StringUtils.nvlString(data.get("sid"));

	}

	public String getConsoleSid(String uid, String pwd) throws IOException {
		String portalUrl = SDK.getPlatformAPI().getPortalUrl();
		String apiServer = portalUrl + "/r/jd?cmd=CONSOLE_ADMIN_LOGIN&lang=cn&userid=" + uid + "&pwd=" + pwd + "";
		HttpAPI api = SDK.getCCAPI().getHttpAPI();
		String result = api.get(apiServer);
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse(result);
		Map<String, Object> data = (Map<String, Object>) jsonMap.get("data");
		return StringUtils.nvlString(data.get("sid"));
	}
}
