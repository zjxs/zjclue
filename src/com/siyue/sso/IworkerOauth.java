package com.siyue.sso;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.curator.shaded.com.google.common.collect.Maps;

import com.actionsoft.bpms.commons.oauth.AbstractOauth;
import com.actionsoft.bpms.org.model.UserModel;
import com.actionsoft.bpms.server.RequestParams;
import com.actionsoft.sdk.local.SDK;
import com.alibaba.fastjson.JSONObject;
import com.siyue.util.HttpClientUtils;
import com.siyue.util.StringUtils;

public class IworkerOauth extends AbstractOauth {

	@Override
	public String getOauthPage(RequestParams params) {// 如果hasOauthPage()方法返回为true 则需要进行编写，反之为null即可
		// TODO Auto-generated method stub
		return "http://10.118.5.30/sso/login.jsp";
	}

	@Override
	public boolean hasOauthPage() {// 是否需要登录授权页 需要则为true,反之为false
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String validate(RequestParams params) throws IOException {
		try {
			SDK.getLogAPI().consoleInfo("进入单点测试");
			Map<String, Object> user = this.getUser(params.get("cookiename"));
			if (user != null) {
				SDK.getLogAPI().consoleInfo("----------认证用户信息----------" + user + "  结束");
				return StringUtils.nvlString(user.get("SFZH"));
			}
			if (params.get("uid") != null) {
				SDK.getLogAPI().consoleInfo("----------认证用户信息----------" + params.get("uid") + "  结束");
				return params.get("uid");
			}

		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
		}
		/*
		 * String user = params.get("cookiename"); if(StringUtils.isNotEmpty(user)) {
		 * return user; }
		 */
		return null;

	}

	@Override
	public long getCookieTime(UserModel user) {
		return 0;
	}

	public Map<String, Object> getUser(String cookiename) throws URISyntaxException {
		String apiServer = "http://10.118.5.30/sso/SSOAuth";
		Map<String, Object> args = Maps.newHashMap();
		args.put("action", "authcookie");
		args.put("contextpath", "portal");
		args.put("cookiename", cookiename);
		String httpGetRequest = HttpClientUtils.httpGetRequest(apiServer, args);
		if (!"error".equals(httpGetRequest) && StringUtils.isNotEmpty(httpGetRequest)) {
			Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse(httpGetRequest);
			return jsonMap;
		}

		return null;
	}

}
