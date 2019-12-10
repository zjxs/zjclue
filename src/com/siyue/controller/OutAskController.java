package com.siyue.controller;

import java.io.IOException;
import java.util.Map;

import com.actionsoft.bpms.server.bind.annotation.Controller;
import com.actionsoft.bpms.server.bind.annotation.Mapping;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.actionsoft.sdk.local.api.cc.HttpAPI;
import com.alibaba.fastjson.JSONObject;
import com.siyue.util.StringUtils;
import java.io.File;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.io.SAXReader;

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

	@Mapping(value = "com.awspaas.user.apps.clue.getxml")
	public void getXml() throws Exception{
		SAXReader reader = new SAXReader();
		org.dom4j.Document doc = reader.read(new File("E:\\city.xml"));
		Element rootElem = doc.getRootElement();
		System.out.println(rootElem.getName());
		List<Element> CountryRegions = rootElem.elements("CountryRegion");
		for (Element CountryRegion : CountryRegions) {
			String countryName = CountryRegion.attribute("Name").getValue();
			String countryCode = CountryRegion.attribute("Code").getValue();
			System.out.println("国家--" + countryName + "---" + countryCode);

			Element element = CountryRegion.element("State");
			if (element == null) {
				DBSql.update("INSERT into code_city (code,name,lev,child,code_lev1,dsc_lev1,code_lev2,dsc_lev2,isvalid)VALUES (?,?,?,?,?,?,?,?,?)"
						,new String[]{countryCode,countryName,"1","0",countryCode,countryName,"","","1"});
				continue;
			}else{
				DBSql.update("INSERT into code_city (code,name,lev,child,code_lev1,dsc_lev1,code_lev2,dsc_lev2,isvalid)VALUES (?,?,?,?,?,?,?,?,?)"
						,new String[]{countryCode,countryName,"1","1",countryCode,countryName,"","","1"});
			}
			List<Element> list = element.elements("City");
			for (Element personsElem1 : list) {
				String name = personsElem1.attribute("Name").getValue();
				String code = personsElem1.attribute("Code").getValue();
				DBSql.update("INSERT into code_city (code,name,lev,child,code_lev1,dsc_lev1,code_lev2,dsc_lev2,isvalid)VALUES (?,?,?,?,?,?,?,?,?)"
						,new String[]{code,name,"2","0",countryCode,countryName,code,name,"1"});
				System.out.println(name + "----" + code);
			}
		}

	}
}
