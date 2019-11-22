package com.siyue.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.actionsoft.bpms.commons.database.RowMap;
import com.actionsoft.bpms.commons.htmlframework.HtmlPageTemplate;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.server.bind.annotation.Controller;
import com.actionsoft.bpms.server.bind.annotation.Mapping;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.siyue.service.ClueService;
import com.siyue.service.CommonService;
import com.siyue.service.TaskService;
import com.siyue.util.ResourceUtils;
import com.siyue.util.StringUtils;

@Controller
public class PageController {

	private ClueService clueService = new ClueService();

	private CommonService commonService = new CommonService();

	private TaskService taskService = new TaskService();

	@Mapping(value = "com.awspaas.user.apps.clue.router")
	public String router(UserContext userContext, String cookiename, String classify, String action, String extendParams, String rwglxsId) throws SQLException {
		SDK.getLogAPI().consoleInfo("----------进入路由控制----------" + "[" + classify + "_" + action + "]" + "--------  帐户:" + userContext.getUID() + "  姓名：" + userContext.getUserName());
		Map<String, Object> extendParamsMap = JSONObject.parse(extendParams) == null ? Maps.newHashMap() : (Map<String, Object>) JSONObject.parse(extendParams);
		Map<String, Object> macroLibraries = Maps.newHashMap();
		Map<String, Object> userMap = Maps.newHashMap();
		userMap.put("userName", userContext.getUserName());
		userMap.put("userId", userContext.getUID());
		userMap.put("userGxsCode", userContext.getDepartmentModel().getId());
		userMap.put("userGxsName", userContext.getDepartmentModel().getName());
		userMap.put("userGxsLev", userContext.getDepartmentModel().getLayer());
		userMap.put("parentGxsCode", userContext.getDepartmentModel().getParentDepartmentId());
		userMap.put("role", "");
		macroLibraries.put("user", JSONObject.toJSONString(userMap));
		macroLibraries.put("UUID", UUID.randomUUID().toString());
		macroLibraries.put("sid", userContext.getSessionId());
		String templateFileName = classify + "_" + action + ".html";

		if (!StringUtils.isNotEmpty(userContext.getSessionId())) {
			return HtmlPageTemplate.merge("com.awspaas.user.apps.clue", "login.html", macroLibraries);
		}

		// 线索
		if ("clue".equals(classify)) {
			if ("search".equals(action)) {

			} else if ("add".equals(action)) {
				if (extendParamsMap.containsKey("XSBH")) {
					String xsbh = StringUtils.nvlString(extendParamsMap.get("XSBH"));
					// 线索基本信息
					Map<String, Object> xsParamsMap = Maps.newHashMap();
					xsParamsMap.put("XSBH", xsbh);
					macroLibraries.put("xs", JSONObject.toJSONString(clueService.getClue(userContext, xsParamsMap, false)));
					macroLibraries.put("clueIds", JSONObject.toJSONString(clueService.copyClue(userContext, xsbh, StringUtils.nvlString(extendParamsMap.get("XSLXDM")))));
				}
				if (extendParamsMap.containsKey("ASJBH")) {
					String asjbh = StringUtils.nvlString(extendParamsMap.get("ASJBH"));
					String xsbh = clueService.addClue(userContext);
					clueService.addGlyw(xsbh, asjbh, userContext);
					Map<String, Object> xsParamsMap = Maps.newHashMap();
					xsParamsMap.put("XSBH", xsbh);
					macroLibraries.put("xs", JSONObject.toJSONString(clueService.getClue(userContext, xsParamsMap, false)));
					macroLibraries.put("clueIds", JSONObject.toJSONString(clueService.copyClue(userContext, xsbh, "01")));
				}
			} else if ("update".equals(action)) {
				String xsbh = StringUtils.nvlString(extendParamsMap.get("XSBH"));
				// 线索基本信息
				Map<String, Object> xsParamsMap = Maps.newHashMap();
				xsParamsMap.put("XSBH", xsbh);
				Map<String, Object> xsMap = clueService.getClue(userContext, xsParamsMap, false);

				macroLibraries.put("xs", JSONObject.toJSONString(xsMap));
				macroLibraries.put("glywId", xsMap.get("GLYWID"));
				// 附件
				Map<String, Object> fjParamsMap = Maps.newHashMap();
				fjParamsMap.put("GLYWBH", xsbh);
				macroLibraries.put("fj", JSONObject.toJSONString(commonService.getRelevanceAttachment(userContext, fjParamsMap)));
				macroLibraries.put("clueIds", JSONObject.toJSONString(clueService.copyClue(userContext, xsbh, StringUtils.nvlString(extendParamsMap.get("XSLXDM")))));
				// TODO 贩枪-参照页面

			} else if ("detail".equals(action)) {
				String xsbh = StringUtils.nvlString(extendParamsMap.get("XSBH"));
				// 线索基本信息
				Map<String, Object> xsParamsMap = Maps.newHashMap();
				xsParamsMap.put("XSBH", xsbh);
				macroLibraries.put("xs", JSONObject.toJSONString(clueService.getClue(userContext, xsParamsMap, true)));
				// 附件
				Map<String, Object> fjParamsMap = Maps.newHashMap();
				fjParamsMap.put("GLYWBH", xsbh);
				macroLibraries.put("fj", JSONObject.toJSONString(commonService.getRelevanceAttachment(userContext, fjParamsMap)));
			} else if ("resubmit".equals(action)) {// 续报操作
				String xsbh = StringUtils.nvlString(extendParamsMap.get("XSBH"));
				// 线索基本信息
				Map<String, Object> xsParamsMap = Maps.newHashMap();
				xsParamsMap.put("XSBH", xsbh);
				Map<String, Object> xsMap = clueService.getClue(userContext, xsParamsMap, false);

				macroLibraries.put("xs", JSONObject.toJSONString(xsMap));
				macroLibraries.put("XSBH", xsbh);
				macroLibraries.put("glywId", xsMap.get("GLYWID"));
				// 附件
				Map<String, Object> fjParamsMap = Maps.newHashMap();
				fjParamsMap.put("GLYWBH", xsbh);
				macroLibraries.put("fj", JSONObject.toJSONString(commonService.getRelevanceAttachment(userContext, fjParamsMap)));
				macroLibraries.put("clueIds", JSONObject.toJSONString(clueService.copyClue(userContext, xsbh, StringUtils.nvlString(extendParamsMap.get("XSLXDM")))));
			} else if ("resubmitProcess".equals(action)) {
				String rwbh = StringUtils.nvlString(extendParamsMap.get("RWBH"));
				String xsbh = StringUtils.nvlString(extendParamsMap.get("XSBH"));
				macroLibraries.put("XSBH", xsbh);
				// TODO 续报的详细内容 以及审批操作
				Map<String, Object> sbParamsMap = Maps.newHashMap();
				sbParamsMap.put("XBBH", rwbh);
				macroLibraries.put("rw", JSONObject.toJSONString(taskService.getClueResubmitTask(userContext, sbParamsMap, true)));
				// 附件
				Map<String, Object> fjParamsMap = Maps.newHashMap();
				fjParamsMap.put("XBBH", rwbh);
				macroLibraries.put("fj", JSONObject.toJSONString(commonService.getRelevanceAttachment(userContext, fjParamsMap)));
				// 关联线索
				Map<String, Object> glxsParamsMap = Maps.newHashMap();
				glxsParamsMap.put("RWBH", rwbh);
				macroLibraries.put("glxs", JSONObject.toJSONString(taskService.getTaskRelevanceClue(userContext, glxsParamsMap, 1, 5, "DJSJ", "DESC")));
				// 沟通人员信息
				macroLibraries.put("gtryxx", JSONObject.toJSONString(taskService.getTalkingPerson(userContext, sbParamsMap)));
			} else if ("resubmitUpdate".equals(action)) {
				String xbbh = StringUtils.nvlString(extendParamsMap.remove("XBBH"));
				macroLibraries.put("processInstId", StringUtils.nvlString(extendParamsMap.remove("processInstId")));
				macroLibraries.put("taskInstId", StringUtils.nvlString(extendParamsMap.remove("taskInstId")));
				extendParamsMap.remove("XBBH");
				String xsbh = StringUtils.nvlString(extendParamsMap.get("XSBH"));
				// 线索基本信息
				Map<String, Object> xsParamsMap = Maps.newHashMap();
				xsParamsMap.put("XSBH", xsbh);
				Map<String, Object> xsMap = clueService.getClue(userContext, xsParamsMap, false);

				macroLibraries.put("xs", JSONObject.toJSONString(xsMap));
				macroLibraries.put("glywId", xsMap.get("GLYWID"));
				// 附件
				Map<String, Object> fjParamsMap = Maps.newHashMap();
				fjParamsMap.put("GLYWBH", xsbh);
				macroLibraries.put("fj", JSONObject.toJSONString(commonService.getRelevanceAttachment(userContext, fjParamsMap)));
				extendParamsMap.remove("XSBH");
				extendParamsMap.put("XBBH", xbbh);
				extendParamsMap.put("XSLXDM", xsMap.get("XSLXDM"));
				macroLibraries.put("XBBH", xbbh);
				macroLibraries.put("rwlzID", StringUtils.nvlString(extendParamsMap.remove("rwlzID")));
				macroLibraries.put("RWLZBH", StringUtils.nvlString(extendParamsMap.remove("RWLZBH")));
				macroLibraries.put("clueIds", JSONObject.toJSONString(clueService.getResubmitYw(userContext, xbbh, StringUtils.nvlString(extendParamsMap.get("XSLXDM")))));
				// TODO 贩枪-参照页面

			}
		} else if ("issue".equals(classify)) {
			if ("search".equals(action)) {
				List<RowMap> issCountByMe = DBSql.getMaps("select RWZTDM from (select a.* from(" + ResourceUtils.getSQL("select_xfrw") + ") a where 1=1 ) c where c.RWZTDM in ('06')");
				List<RowMap> issCountByUp = DBSql.getMaps(
						"select RWZTDM from (" + ResourceUtils.getSQL("select_sjxfrw") + " and (a.SFSMDM='01' or(a.SFSMDM='02' and ((a.JSDW_GAJGJGDM = '" + userContext.getDepartmentModel().getId()
								+ "' or a.JSRY_GMSFHM = '" + userContext.getUID() + "'))))" + ") c where c.RWZTDM in ('01')",
						new Object[] { userContext.getUID(), userContext.getDepartmentModel().getId(), userContext.getUID(), userContext.getDepartmentModel().getId() });
				macroLibraries.put("issCountByMe", issCountByMe.size());
				macroLibraries.put("issCountByUp", issCountByUp.size());
				if (StringUtils.isNotEmpty(rwglxsId)) {
					SDK.getBOAPI().remove("BO_EU_XSHB_RW_GLXS", rwglxsId);
				}
			} else if ("add".equals(action) || "update".equals(action)) {
				if (extendParamsMap.containsKey("RWBH")) {
					String rwbh = StringUtils.nvlString(extendParamsMap.get("RWBH"));
					// 下发基本信息
					Map<String, Object> rwParamsMap = Maps.newHashMap();
					rwParamsMap.put("RWBH", rwbh);
					macroLibraries.put("rw", JSONObject.toJSONString(taskService.getIssueTask(userContext, rwParamsMap, false)));
					if (macroLibraries.get("rw").equals("null")) {
						macroLibraries.put("rw", JSONObject.toJSONString(taskService.getReportTask(userContext, rwParamsMap, false)));
					}
					// 附件
					Map<String, Object> fjParamsMap = Maps.newHashMap();
					fjParamsMap.put("GLYWBH", rwbh);
					macroLibraries.put("fj", JSONObject.toJSONString(commonService.getRelevanceAttachment(userContext, fjParamsMap)));
					// 任务关联线索
					Map<String, Object> glxsParamsMap = Maps.newHashMap();
					glxsParamsMap.put("RWBH", rwbh);
					macroLibraries.put("glxs", JSONObject.toJSONString(taskService.getTaskRelevanceClue(userContext, glxsParamsMap, 1, 5, "DJSJ", "DESC")));
				}
				macroLibraries.put("issusIds", JSONObject.toJSONString(clueService.copyTask(userContext, extendParamsMap)));
			} else if ("detail".equals(action)) {
				String rwbh = StringUtils.nvlString(extendParamsMap.get("RWBH"));
				// 下发基本信息
				Map<String, Object> rwParamsMap = Maps.newHashMap();
				rwParamsMap.put("RWBH", rwbh);
				macroLibraries.put("rw", JSONObject.toJSONString(taskService.getIssueTask(userContext, rwParamsMap, true)));
				// 附件
				Map<String, Object> fjParamsMap = Maps.newHashMap();
				fjParamsMap.put("GLYWBH", rwbh);
				macroLibraries.put("fj", JSONObject.toJSONString(commonService.getRelevanceAttachment(userContext, fjParamsMap)));
				// 任务关联线索
				Map<String, Object> glxsParamsMap = Maps.newHashMap();
				glxsParamsMap.put("RWBH", rwbh);
				macroLibraries.put("glxs", taskService.getTaskRelevanceClue(userContext, glxsParamsMap, 1, 5, "DJSJ", "DESC"));
			} else if ("process".equals(action)) {
				String rwbh = StringUtils.nvlString(extendParamsMap.get("RWBH"));
				// 下发基本信息
				Map<String, Object> rwParamsMap = Maps.newHashMap();
				rwParamsMap.put("RWBH", rwbh);
				macroLibraries.put("rw", JSONObject.toJSONString(taskService.getIssueTask(userContext, rwParamsMap, true)));
				// 附件
				Map<String, Object> fjParamsMap = Maps.newHashMap();
				fjParamsMap.put("GLYWBH", rwbh);
				macroLibraries.put("fj", JSONObject.toJSONString(commonService.getRelevanceAttachment(userContext, fjParamsMap)));
				// 任务关联线索
				Map<String, Object> glxsParamsMap = Maps.newHashMap();
				glxsParamsMap.put("RWBH", rwbh);
				macroLibraries.put("glxs", JSONObject.toJSONString(taskService.getTaskRelevanceClue(userContext, glxsParamsMap, 1, 5, "DJSJ", "DESC")));
				// 沟通人员信息
				macroLibraries.put("gtryxx", JSONObject.toJSONString(taskService.getTalkingPerson(userContext, rwParamsMap)));
			} else if ("add_other".equals(action)) {
				// testData(extendParamsMap);
				if (extendParamsMap.containsKey("XXZJBH")) {
					macroLibraries.put("XXZJBH", StringUtils.nvlString(extendParamsMap.get("XXZJBH")));
				}
				// TODO 其他平台对接任务
				Map<String, Object> returnMap = Maps.newHashMap();
				if (extendParamsMap.containsKey("XXLY")) {
					returnMap.put("XXLYDM", extendParamsMap.get("XXLY"));
				}
				macroLibraries.put("rw", JSONObject.toJSONString(returnMap));
				// 临时任务编号
				String lsRwbh = taskService.getLsRwbh(userContext, "SEQ_BO_EU_XSHB_RW_SB", StringUtils.nvlString(extendParamsMap.get("XXLY")));
				if (extendParamsMap.containsKey("FJLJ")) {
					// 新增附件
					taskService.addFj(userContext, lsRwbh, extendParamsMap);
				}
				if (extendParamsMap.containsKey("BO_EU_XSHB_GLYW")) {
					// 新增关联业务
					taskService.addGlyw(userContext, lsRwbh, extendParamsMap);
				}
				// 查询附件
				Map<String, Object> fjParamsMap = Maps.newHashMap();
				fjParamsMap.put("GLYWBH", lsRwbh);
				// issusIds 对象中存放了附件ID和glywIds
				macroLibraries.put("issusIds", JSONObject.toJSONString(taskService.getFjIds(userContext, commonService.getRelevanceAttachment(userContext, fjParamsMap), lsRwbh)));
				// 查询关联线索
				Map<String, Object> glxsParamsMap = Maps.newHashMap();
				glxsParamsMap.put("RWBH", lsRwbh);
				macroLibraries.put("glxs", JSONObject.toJSONString(taskService.getTaskRelevanceClue(userContext, glxsParamsMap, 1, 5, "DJSJ", "DESC")));

			}
		} else if ("report".equals(classify)) {
			if ("search".equals(action)) {
				List<RowMap> reportCountByMe = DBSql.getMaps("select c.RWZTDM from ( select a.* from(" + ResourceUtils.getSQL("select_sbrw") + ") a where 1=1 ) c where c.RWZTDM in ('06')");
				List<RowMap> reportCountByDown = DBSql.getMaps(
						"select c.RWZTDM from (select a.* from(" + ResourceUtils.getSQL("select_sbrw")
								+ "and sb.SBDW_GAJGJGDM in (select ID from ORGDEPARTMENT where PARENTDEPARTMENTID = ?)) a) c where c.RWZTDM in ('01')",
						new Object[] { userContext.getDepartmentModel().getId() });
				// 续报
				List<RowMap> SubResubByDown = DBSql.getMaps(
						"select c.RWZTDM from (select a.* from(" + ResourceUtils.getSQL("select_xbrw")
								+ " and xb.XBDJDW_GAJGJGDM in (select ID from ORGDEPARTMENT where PARENTDEPARTMENTID = ?) ) a) c where c.RWZTDM in ('01')",
						new Object[] { userContext.getDepartmentModel().getId() });
				List<RowMap> SubResubByMe = DBSql.getMaps("select c.lz_RWZTDM from (select a.* from(" + ResourceUtils.getSQL("select_xbrw") + ") a) c where c.lz_RWZTDM in ('06')");

				macroLibraries.put("reportCountByMe", reportCountByMe.size());
				macroLibraries.put("reportCountByDown", reportCountByDown.size());
				macroLibraries.put("SubResubByDown", SubResubByDown.size());
				macroLibraries.put("SubResubByMe", SubResubByMe.size());
				if (StringUtils.isNotEmpty(rwglxsId)) {
					SDK.getBOAPI().remove("BO_EU_XSHB_RW_GLXS", rwglxsId);
				}
			} else if ("add".equals(action) || "update".equals(action)) {
				if (extendParamsMap.containsKey("RWBH")) {
					String rwbh = StringUtils.nvlString(extendParamsMap.get("RWBH"));
					// 上报基本信息
					Map<String, Object> sbParamsMap = Maps.newHashMap();
					sbParamsMap.put("RWBH", rwbh);
					macroLibraries.put("rw", JSONObject.toJSONString(taskService.getReportTask(userContext, sbParamsMap, false)));
					// 附件
					Map<String, Object> fjParamsMap = Maps.newHashMap();
					fjParamsMap.put("GLYWBH", rwbh);
					macroLibraries.put("fj", JSONObject.toJSONString(commonService.getRelevanceAttachment(userContext, fjParamsMap)));
					// 关联线索
					Map<String, Object> glxsParamsMap = Maps.newHashMap();
					glxsParamsMap.put("RWBH", rwbh);
					macroLibraries.put("glxs", JSONObject.toJSONString(taskService.getTaskRelevanceClue(userContext, glxsParamsMap, 1, 5, "DJSJ", "DESC")));
				}
				macroLibraries.put("reportIds", JSONObject.toJSONString(clueService.copyTask(userContext, extendParamsMap)));

			} else if ("detail".equals(action)) {
				String rwbh = StringUtils.nvlString(extendParamsMap.get("RWBH"));
				// 上报基本信息
				Map<String, Object> sbParamsMap = Maps.newHashMap();
				sbParamsMap.put("RWBH", rwbh);
				macroLibraries.put("rw", JSONObject.toJSONString(taskService.getReportTask(userContext, sbParamsMap, true)));
				// 附件
				Map<String, Object> fjParamsMap = Maps.newHashMap();
				fjParamsMap.put("GLYWBH", rwbh);
				macroLibraries.put("fj", JSONObject.toJSONString(commonService.getRelevanceAttachment(userContext, fjParamsMap)));
				// 关联线索
				Map<String, Object> glxsParamsMap = Maps.newHashMap();
				glxsParamsMap.put("RWBH", rwbh);
				macroLibraries.put("glxs", JSONObject.toJSONString(taskService.getTaskRelevanceClue(userContext, glxsParamsMap, 1, 5, "DJSJ", "DESC")));
			} else if ("process".equals(action)) {
				String rwbh = StringUtils.nvlString(extendParamsMap.get("RWBH"));
				// 上报基本信息
				Map<String, Object> sbParamsMap = Maps.newHashMap();
				sbParamsMap.put("RWBH", rwbh);
				macroLibraries.put("rw", JSONObject.toJSONString(taskService.getReportTask(userContext, sbParamsMap, true)));
				// 附件
				Map<String, Object> fjParamsMap = Maps.newHashMap();
				fjParamsMap.put("GLYWBH", rwbh);
				macroLibraries.put("fj", JSONObject.toJSONString(commonService.getRelevanceAttachment(userContext, fjParamsMap)));
				// 关联线索
				Map<String, Object> glxsParamsMap = Maps.newHashMap();
				glxsParamsMap.put("RWBH", rwbh);
				macroLibraries.put("glxs", JSONObject.toJSONString(taskService.getTaskRelevanceClue(userContext, glxsParamsMap, 1, 5, "DJSJ", "DESC")));
				// 沟通人员信息
				macroLibraries.put("gtryxx", JSONObject.toJSONString(taskService.getTalkingPerson(userContext, sbParamsMap)));
				// 更新未读标识
				DBSql.update("update BO_EU_XSHB_RW_LZ set wdry='' where rwbh =?", new String[] { rwbh });
			} else if ("add_other".equals(action)) {
				// TODO 其他平台对接任务
				// testData(extendParamsMap);
				if (extendParamsMap.containsKey("XXZJBH")) {
					macroLibraries.put("XXZJBH", StringUtils.nvlString(extendParamsMap.get("XXZJBH")));
				}
				Map<String, Object> returnMap = Maps.newHashMap();
				if (extendParamsMap.containsKey("XXLY")) {
					returnMap.put("XXLYDM", extendParamsMap.get("XXLY"));
				}
				macroLibraries.put("rw", JSONObject.toJSONString(returnMap));
				// 临时任务编号
				String lsRwbh = taskService.getLsRwbh(userContext, "SEQ_BO_EU_XSHB_RW_SB", StringUtils.nvlString(extendParamsMap.get("XXLY")));
				if (extendParamsMap.containsKey("FJLJ")) {
					// 新增附件
					taskService.addFj(userContext, lsRwbh, extendParamsMap);
				}
				if (extendParamsMap.containsKey("BO_EU_XSHB_GLYW")) {
					// 新增关联业务
					taskService.addGlyw(userContext, lsRwbh, extendParamsMap);
				}
				// 查询附件
				Map<String, Object> fjParamsMap = Maps.newHashMap();
				fjParamsMap.put("GLYWBH", lsRwbh);
				// issusIds 对象中存放了附件ID和glywIds
				macroLibraries.put("reportIds", JSONObject.toJSONString(taskService.getFjIds(userContext, commonService.getRelevanceAttachment(userContext, fjParamsMap), lsRwbh)));
				// 查询关联线索
				Map<String, Object> glxsParamsMap = Maps.newHashMap();
				glxsParamsMap.put("RWBH", lsRwbh);
				macroLibraries.put("glxs", JSONObject.toJSONString(taskService.getTaskRelevanceClue(userContext, glxsParamsMap, 1, 5, "DJSJ", "DESC")));
			} else if ("jcpb".equals(classify)) {// 浙江省刑侦违法人员纠错屏蔽上报系统
				SDK.getLogAPI().consoleInfo("进入纠错系统");
				if ("report".equals(action)) {
					String sid = userContext.getSessionId();
					macroLibraries.put("sid", sid);
				}
				if ("mydriver".equals(action)) {
					String sid = userContext.getSessionId();
					macroLibraries.put("sid", sid);
				}
			}
		}

		return HtmlPageTemplate.merge("com.awspaas.user.apps.clue", templateFileName, macroLibraries);
	}

	// 对接测试数据方法
	public Map<String, Object> testData(Map<String, Object> extendParamsMap) {
		Map<String, Object> data = Maps.newHashMap();
		data.put("XXLY", "05");
		data.put("XXZJBH", "C33000000000019091100FT");
		data.put("FJLJ",
				"E:\\BPM\\bin\\..\\doccenter\\com.awspaas.user.apps.clue\\sy\\clue\\787b84c4-cce1-45a2-b30f-c4e621a3b563\\.project,E:\\BPM\\bin\\..\\doccenter\\com.awspaas.user.apps.clue\\sy\\clue\\787b84c4-cce1-45a2-b30f-c4e621a3b563\\manifest.xml");

		ArrayList<Object> BO_EU_XSHB_GLYW = Lists.newArrayList();
		Map<String, Object> glyw1 = Maps.newHashMap();
		Map<String, Object> glyw2 = Maps.newHashMap();
		glyw1.put("GLYWBH", "C33000000000019091100FT");
		glyw1.put("GLYWLXDM", "05");
		// glyw2.put("GLYWBH", "A3301025600002016030017");
		// glyw2.put("GLYWLXDM", "02");
		BO_EU_XSHB_GLYW.add(glyw1);
		// BO_EU_XSHB_GLYW.add(glyw2);
		data.put("BO_EU_XSHB_GLYW", BO_EU_XSHB_GLYW);
		extendParamsMap.putAll(data);
		return extendParamsMap;
	}

}
