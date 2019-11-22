package com.siyue.service;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.shaded.com.google.common.collect.Maps;
import org.dom4j.Document;
import org.dom4j.Element;

import com.actionsoft.bpms.commons.database.RowMap;
import com.actionsoft.bpms.server.UserContext;
import com.google.common.collect.Lists;
import com.siyue.util.ResourceUtils;

public class BaseService {

	/**
	 * 创建用户信息
	 * @param userContext
	 * @param prefixes	前缀,如XXDJ、XXCZ
	 * @return
	 */
	public static Map<String, Object> createUserMap4DB(UserContext userContext, String[] prefixes) {
		Map<String, Object> userMap = Maps.newHashMap();
		for(String prefix :prefixes) {
			userMap.put(prefix + "DW_GAJGJGDM", userContext.getDepartmentModel().getId());
			userMap.put(prefix + "DW_GAJGMC", userContext.getDepartmentModel().getName());
			userMap.put(prefix + "RY_GMSFHM", userContext.getUID());
			userMap.put(prefix + "RY_XM", userContext.getUserName());
			userMap.put(prefix + "RY_LXDH", userContext.getUserModel().getMobile());
		}
		return userMap;
	}
	
	/**
	 * 获取页面可执行操作
	 * @param processDefId	流程Id
	 * @param processDefId 流程定义ID
	 * @return
	 */
	public static List<Map<String,String>> getPageActions(String processDefId,String activityId) {
		Document doc = ResourceUtils.getProcessDoc(processDefId);
		List<Map<String,String>> actionList = Lists.newArrayList();
		List<Element> userTaskList = doc.getRootElement().element("process").elements("userTask");
        for (Element element : userTaskList) {
            String id = element.attribute("id").getValue();
            if (id.equals(activityId)) {
                List<Element> buttonList = element.element("extensionElements").element("extendUserTask").elements("button");
                for (Element buttonPropertyElement : buttonList) {
                    //按钮名称
                    String actionName = buttonPropertyElement.attribute("label").getValue();
                    //按钮调用方法
                    String functionName = buttonPropertyElement.attribute("clazz").getValue();
                    Map<String,String> actionMap = Maps.newHashMap();
//                    actionMap.put("id",actionName.split("-")[0]);
                    actionMap.put("name",actionName);
                    actionMap.put("handler",functionName);
                    actionList.add(actionMap);
                }
            }
        }
		return actionList;
	}

	/**
	 * 将指定空值替换成指定字符
	 * @param rows
	 * @param field
	 * @param str
	 */
	public static void replaceNullForStr(List<RowMap> rows,String field,String str){
		for(RowMap rowMap:rows){
			String value = rowMap.getString(field);
			if(StringUtils.isBlank(value)){
				rowMap.put(field,str);
			}
		}
	}

	/**
	 * 将所有结果替换成指定字符
	 */
	public static void replaceNullForStr(List<RowMap> rows,String str){
		for(RowMap rowMap:rows){
			for(String key:rowMap.keySet()){
				String value = rowMap.getString(key);
				if(StringUtils.isBlank(com.siyue.util.StringUtils.nvlString(value))){
					rowMap.put(key,str);
				}
			}
		}
	}
}
