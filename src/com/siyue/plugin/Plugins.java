package com.siyue.plugin;

import java.util.ArrayList;
import java.util.List;

import com.actionsoft.apps.listener.PluginListener;
import com.actionsoft.apps.resource.AppContext;
import com.actionsoft.apps.resource.plugin.profile.AWSPluginProfile;
import com.actionsoft.apps.resource.plugin.profile.AtFormulaPluginProfile;
import com.actionsoft.apps.resource.plugin.profile.DCPluginProfile;
import com.actionsoft.apps.resource.plugin.profile.OauthPluginProfile;
import com.siyue.at.GetTopLevelProcessId;
import com.siyue.plugin.dc.AttachmentProcessor;
import com.siyue.plugin.dc.TmpFileProcessor;
import com.siyue.sso.IworkerOauth;

public class Plugins implements PluginListener {

	@Override
	public List<AWSPluginProfile> register(AppContext context) {
		// 存放本应用的全部插件扩展点描述
        List<AWSPluginProfile> list = new ArrayList<AWSPluginProfile>();
        // 注册DC
        list.add(new DCPluginProfile("tmp", TmpFileProcessor.class.getName(), "存放用户上传的临时文件，模拟处理过程", false));
        list.add(new DCPluginProfile("sy", AttachmentProcessor.class.getName(), "存放用户上传附件", false));
        list.add(new AtFormulaPluginProfile("流程执行", "@sy-getTopLevelProcessId(*nowProcessID)", GetTopLevelProcessId.class.getName(), "获取顶级流程", "根据当前流程实例ID，获取顶级流程"));
        list.add(new OauthPluginProfile("oauth",IworkerOauth.class.getName(), ""));
        return list;
	}

}
