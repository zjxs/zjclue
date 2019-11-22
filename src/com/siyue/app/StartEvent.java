package com.siyue.app;

import com.actionsoft.apps.listener.AppListener;
import com.actionsoft.apps.resource.AppContext;
import com.siyue.config.ProcessDocMapConfig;
import com.siyue.config.SQLMapConfig;
import com.siyue.service.CodeService;

public class StartEvent implements AppListener{

	@Override
	public void after(AppContext context) {
		CodeService.init(context);
		ProcessDocMapConfig.init(context);
		SQLMapConfig.init(context);
	}

	@Override
	public boolean before(AppContext context) {
		return true;
	}

}
