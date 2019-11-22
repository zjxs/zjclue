package com.siyue.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.bpmn.engine.model.run.delegate.ProcessInstance;
import com.actionsoft.bpms.commons.database.RowMap;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.server.bind.annotation.Controller;
import com.actionsoft.bpms.server.bind.annotation.Mapping;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.siyue.service.BaseService;
import com.siyue.service.CommonService;
import com.siyue.service.ProcessService;
import com.siyue.service.TaskService;
import com.siyue.util.StringUtils;
import com.siyue.vo.RemoteResult;

@Controller
public class TaskController extends BaseService {

	private TaskService taskService = new TaskService();

	private CommonService commonService = new CommonService();

	private ProcessService processService = new ProcessService();

	@Mapping(value = "com.awspaas.user.apps.clue.getUnitOtherUser")
	public String getUnitOtherUser(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		RemoteResult remoteResult = new RemoteResult();
		try {
			List<RowMap> resultList = taskService.getUnitOtherUser(userContext);
			remoteResult.setResultData(resultList);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	@Mapping(value = "com.awspaas.user.apps.clue.getClueSource")
	public String getClueSource(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		RemoteResult remoteResult = new RemoteResult();
		try {
			List<RowMap> resultList = taskService.getClueSource(userContext);
			remoteResult.setResultData(resultList);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 新增上报任务
	 *
	 * @param userContext
	 * @param json
	 * @return
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.addReportTask")
	public String addReportTask(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		RemoteResult remoteResult = new RemoteResult();
		Connection connection = null;
		try {
			connection = DBSql.open();
			connection.setAutoCommit(false);

			int[] insert = taskService.addReportTask(userContext, paramsMap, connection);
			if (!Arrays.asList(insert).contains(0)) {
				remoteResult.setStatus(true);
				remoteResult.setResultMsg("保存成功！");
			} else {
				remoteResult.setStatus(false);
				remoteResult.setResultMsg("保存失败！");
			}
			connection.commit();
		} catch (Exception e) {
			try {
				e.printStackTrace();
				remoteResult.setResultMsg("保存异常！");
				remoteResult.setStatus(false);
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}

		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return remoteResult.toString();
	}

	/**
	 * 新增下发任务
	 *
	 * @param userContext
	 * @param json
	 * @return
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.addIssueTask")
	public String addIssueTask(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		Map<String, Object> xfParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_XF");
		RemoteResult remoteResult = new RemoteResult();
		Connection connection = null;
		try {
			connection = DBSql.open();
			connection.setAutoCommit(false);
			int[] insert = taskService.addIssueTask(userContext, paramsMap, connection);
			if (!Arrays.asList(insert).contains(0)) {
				remoteResult.setStatus(true);
				remoteResult.setResultMsg("保存成功！");
			} else {
				remoteResult.setResultMsg("保存失败！");
				remoteResult.setStatus(false);
			}
			connection.commit();
		} catch (Exception e) {
			try {
				connection.rollback();
				e.printStackTrace();
				remoteResult.setResultMsg("保存异常！");
				remoteResult.setStatus(false);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return remoteResult.toString();
	}

	/**
	 * 我的上报任务
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getMyReportTask")
	public String getMyReportTask(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = taskService.getMyReportTask(userContext, paramsMap, currPage, pageSize,
					sortField, sortType);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 下级上报任务
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getSubReportTask")
	public String getSubReportTask(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = taskService.getSubReportTask(userContext, paramsMap, currPage, pageSize,
					sortField, sortType);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 下级续报
	 * 
	 * @param userContext
	 * @param json
	 * @return
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getSubResubmitTask")
	public String getSubResubmitTask(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = taskService.getSubResubmitTask(userContext, paramsMap, currPage, pageSize,
					sortField, sortType);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 我的续报
	 * 
	 * @param userContext
	 * @param json
	 * @return
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getMyResubmitTask")
	public String getMyResubmitTask(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = taskService.getMyResubmitTask(userContext, paramsMap, currPage, pageSize,
					sortField, sortType);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 单位上报任务
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getUnitReportTask")
	public String getUnitReportTask(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = taskService.getUnitReportTask(userContext, paramsMap, currPage, pageSize,
					sortField, sortType);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 查询辖区上报任务
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getAreaReportTask")
	public String getAreaReportTask(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = taskService.getAreaReportTask(userContext, paramsMap, currPage, pageSize,
					sortField, sortType);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 我的下发任务
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getMyIssueTask")
	public String getMyIssueTask(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = taskService.getMyIssueTask(userContext, paramsMap, currPage, pageSize,
					sortField, sortType);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 上级下发任务
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getSupIssueTask")
	public String getSupIssueTask(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = taskService.getSupIssueTask(userContext, paramsMap, currPage, pageSize,
					sortField, sortType);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 单位下发任务
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getUnitIssueTask")
	public String getUnitIssueTask(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = taskService.getUnitIssueTask(userContext, paramsMap, currPage, pageSize,
					sortField, sortType);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 查询辖区下发任务
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getAreaIssueTask")
	public String getAreaIssueTask(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = taskService.getAreaIssueTask(userContext, paramsMap, currPage, pageSize,
					sortField, sortType);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 任务关联线索
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getTaskRelevanceClue")
	public String getTaskRelevanceClue(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = taskService.getTaskRelevanceClue(userContext, paramsMap, currPage, pageSize,
					sortField, sortType);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 新增任务关联线索表
	 *
	 * @param userContext
	 * @param json
	 * @return
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.addTaskRelevanceClue")
	public String addTaskRelevanceClue(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> returnMap = Maps.newHashMap();
			int[] insert = taskService.addTaskRelevanceClue(userContext, paramsMap, returnMap);
			if (!Arrays.asList(insert).contains(0)) {
				remoteResult.setStatus(true);
				remoteResult.setResultMsg("保存成功！");
				remoteResult.setResultData(returnMap);
			} else {
				remoteResult.setStatus(false);
				remoteResult.setResultMsg("保存失败！");
			}
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setResultMsg("保存异常！");
			remoteResult.setStatus(false);
		}
		return remoteResult.toString();
	}

	/**
	 * 删除任务关联线索表
	 *
	 * @param userContext
	 * @return
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.deleteTaskRelevanceClue")
	public String deleteTaskRelevanceClue(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		RemoteResult remoteResult = new RemoteResult();
		try {
			int delete = taskService.deleteTaskRelevanceClue(userContext, paramsMap);
			if (delete > 0) {
				remoteResult.setResultMsg("删除成功！");
				remoteResult.setStatus(true);
			} else {
				remoteResult.setStatus(false);
				remoteResult.setResultMsg("删除失败！");
			}
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setResultMsg("删除异常！");
			remoteResult.setStatus(false);
		}
		return remoteResult.toString();
	}

	/**
	 * 沟通内容
	 * 
	 * @return
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getCommunicationContent")
	public String getCommunicationContent(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = Maps.newHashMap();
			// 附件
			resultMap.put("fj", commonService.getRelevanceAttachment(userContext, paramsMap));
			// 串并案件
			resultMap.put("cbasj", commonService.getSeriesCase(userContext, paramsMap, 1, 5, "DJSJ", "DESC"));
			// 串并线索
			resultMap.put("cbxs", commonService.getSeriesClue(userContext, paramsMap, 1, 5, "DJSJ", "DESC"));
			remoteResult.setResultMsg("查询成功！");
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setResultMsg("查询异常！");
			remoteResult.setStatus(false);
		}
		return remoteResult.toString();
	}

	/**
	 * 签收(更新数据)
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.sign")
	public String sign(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		String processInstId = StringUtils.nvlString(jsonMap.get("processInstId"));
		String taskInstId = StringUtils.nvlString(jsonMap.get("taskInstId"));
		RemoteResult remoteResult = new RemoteResult();
		try {

			int[] update = taskService.sign(userContext, paramsMap);
			if (!Arrays.asList(update).contains(0)) {
				processService.signProcess(userContext, taskInstId, processInstId, paramsMap);
				remoteResult.setStatus(true);
				remoteResult.setResultMsg("签收成功！");
			} else {
				remoteResult.setStatus(false);
				remoteResult.setResultMsg("任务已被签收，签收失败！");
			}
		} catch (Exception e) {
			remoteResult.setResultMsg("签收异常！");
			remoteResult.setStatus(false);
		}
		return remoteResult.toString();
	}

	/**
	 * 续报签收(更新数据)
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.resubmit_sign")
	public String resubmit_sign(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		String processInstId = StringUtils.nvlString(jsonMap.get("processInstId"));
		String taskInstId = StringUtils.nvlString(jsonMap.get("taskInstId"));
		RemoteResult remoteResult = new RemoteResult();
		try {

			int[] update = taskService.sign(userContext, paramsMap);
			if (!Arrays.asList(update).contains(0)) {
				processService.resubmit_sign(userContext, taskInstId, processInstId);
				remoteResult.setStatus(true);
				remoteResult.setResultMsg("签收成功！");
			} else {
				remoteResult.setStatus(false);
				remoteResult.setResultMsg("任务已被签收，签收失败！");
			}
		} catch (Exception e) {
			remoteResult.setResultMsg("签收异常！");
			remoteResult.setStatus(false);
		}
		return remoteResult.toString();
	}

	/**
	 * 添加反馈信息
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.feedback")
	public String feedback(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		String processInstId = StringUtils.nvlString(jsonMap.get("processInstId"));
		String taskInstId = StringUtils.nvlString(jsonMap.get("taskInstId"));
		RemoteResult remoteResult = new RemoteResult();
		Connection connection = null;
		try {
			connection = DBSql.open();
			connection.setAutoCommit(false);
			int[] batchInsert = taskService.feedback(userContext, paramsMap, connection);
			if (!Arrays.asList(batchInsert).contains(0)) {
				processService.feedBack(userContext, taskInstId, processInstId);
				remoteResult.setStatus(true);
				remoteResult.setResultMsg("保存成功！");
			} else {
				remoteResult.setResultMsg("保存失败！");
				remoteResult.setStatus(false);
			}
			connection.commit();
		} catch (Exception e) {
			try {
				connection.rollback();
				e.printStackTrace();
				remoteResult.setResultMsg("保存异常！");
				remoteResult.setStatus(false);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return remoteResult.toString();

	}

	/**
	 * 新增批复信息
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.reply")
	public String reply(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		String processInstId = StringUtils.nvlString(jsonMap.get("processInstId"));
		String taskInstId = StringUtils.nvlString(jsonMap.get("taskInstId"));
		RemoteResult remoteResult = new RemoteResult();
		Connection connection = null;
		try {
			connection = DBSql.open();
			connection.setAutoCommit(false);
			int[] batchInsert = taskService.reply(userContext, paramsMap, connection);
			if (!Arrays.asList(batchInsert).contains(0)) {
				processService.reply(userContext, taskInstId, processInstId);
				remoteResult.setStatus(true);
				remoteResult.setResultMsg("保存成功！");
			} else {
				remoteResult.setResultMsg("保存失败！");
				remoteResult.setStatus(false);
			}
			connection.commit();
		} catch (Exception e) {
			try {
				connection.rollback();
				e.printStackTrace();
				remoteResult.setResultMsg("保存异常！");
				remoteResult.setStatus(false);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return remoteResult.toString();
	}

	/**
	 * 回退 (流程走向+更新数据)
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.back")
	public String back(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		String processInstId = StringUtils.nvlString(jsonMap.get("processInstId"));
		String taskInstId = StringUtils.nvlString(jsonMap.get("taskInstId"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			int update = taskService.back(userContext, paramsMap);
			if (update > 0) {
				remoteResult.setStatus(true);
				remoteResult.setResultMsg("操作成功！");
				processService.back(userContext, taskInstId, processInstId,
						SDK.getProcessAPI().getInstanceById(processInstId).getCreateUser(), paramsMap);
			} else {
				remoteResult.setStatus(false);
				remoteResult.setResultMsg("操作失败！");
			}
		} catch (Exception e) {
			remoteResult.setResultMsg("操作异常！");
			remoteResult.setStatus(false);
		}
		return remoteResult.toString();
	}

	/**
	 * 续报回退 (流程走向+更新数据)
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.resubmit_back")
	public String resubmit_back(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		String processInstId = StringUtils.nvlString(jsonMap.get("processInstId"));
		String taskInstId = StringUtils.nvlString(jsonMap.get("taskInstId"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			int update = taskService.back(userContext, paramsMap);
			if (update > 0) {
				remoteResult.setStatus(true);
				remoteResult.setResultMsg("操作成功！");
				processService.resubmit_back(userContext, taskInstId, processInstId);
			} else {
				remoteResult.setStatus(false);
				remoteResult.setResultMsg("操作失败！");
			}
		} catch (Exception e) {
			remoteResult.setResultMsg("操作异常！");
			remoteResult.setStatus(false);
		}
		return remoteResult.toString();
	}

	/**
	 * 转派回退 (流程走向+更新数据)
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.transferBack")
	public String transferBack(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		String processInstId = StringUtils.nvlString(jsonMap.get("processInstId"));
		String taskInstId = StringUtils.nvlString(jsonMap.get("taskInstId"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			int update = taskService.back(userContext, paramsMap);
			if (update > 0) {
				remoteResult.setStatus(true);
				remoteResult.setResultMsg("操作成功！");
				processService.transferBack(userContext, taskInstId, processInstId,
						SDK.getProcessAPI().getInstanceById(processInstId).getCreateUser());
			} else {
				remoteResult.setStatus(false);
				remoteResult.setResultMsg("操作失败！");
			}
		} catch (Exception e) {
			remoteResult.setResultMsg("操作异常！");
			remoteResult.setStatus(false);
		}
		return remoteResult.toString();
	}

	/**
	 * 转派
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.transfer")
	public String transfer(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSON.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		String processInstId = StringUtils.nvlString(jsonMap.get("processInstId"));
		String taskInstId = StringUtils.nvlString(jsonMap.get("taskInstId"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			processService.transfer(userContext, taskInstId, processInstId, StringUtils
					.nvlString(((Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_LZ")).get("JSRY_GMSFHM")));
			ProcessInstance childProcess = SDK.getProcessQueryAPI().parentProcessInstId(processInstId).detail();
			int insert[] = taskService.transfer(userContext, paramsMap,
					SDK.getProcessAPI().getInstanceById(processInstId));
			if (!Arrays.asList(insert).contains(0)) {
				remoteResult.setStatus(true);
				remoteResult.setResultMsg("操作成功！");
			} else {
				SDK.getProcessAPI().delete(childProcess, userContext);
				remoteResult.setStatus(false);
				remoteResult.setResultMsg("操作失败！");
			}
		} catch (Exception e) {
			remoteResult.setResultMsg("操作异常！");
			remoteResult.setStatus(false);
		}
		return remoteResult.toString();
	}

	/**
	 * 回退审批通过
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.backApprovePass")
	public String backApprovePass(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		String processInstId = StringUtils.nvlString(jsonMap.get("processInstId"));
		String taskInstId = StringUtils.nvlString(jsonMap.get("taskInstId"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			int update = taskService.backApprove(userContext, paramsMap);
			if (update > 0) {
				remoteResult.setStatus(true);
				remoteResult.setResultMsg("操作成功！");
				processService.passProcess(userContext, taskInstId, processInstId);
			} else {
				remoteResult.setStatus(false);
				remoteResult.setResultMsg("操作失败！");
			}
		} catch (Exception e) {
			remoteResult.setResultMsg("操作异常！");
			remoteResult.setStatus(false);
		}
		return remoteResult.toString();
	}

	/**
	 * 回退审批驳回
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.backApproveEnd")
	public String backApproveEnd(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		String processInstId = StringUtils.nvlString(jsonMap.get("processInstId"));
		String taskInstId = StringUtils.nvlString(jsonMap.get("taskInstId"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			int i = taskService.backApprove(userContext, paramsMap);
			if (i > 0) {
				remoteResult.setStatus(true);
				remoteResult.setResultMsg("操作成功！");
				processService.rejectProcess(userContext, taskInstId, processInstId,
						SDK.getTaskAPI().getInstanceById(taskInstId).getOwner(), paramsMap);
			} else {
				remoteResult.setStatus(false);
				remoteResult.setResultMsg("操作失败！");
			}
		} catch (Exception e) {
			remoteResult.setResultMsg("操作异常！");
			remoteResult.setStatus(false);
		}
		return remoteResult.toString();
	}

	/**
	 * 上报驳回
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.overrule")
	public String overrule(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		String processInstId = StringUtils.nvlString(jsonMap.get("processInstId"));
		String taskInstId = StringUtils.nvlString(jsonMap.get("taskInstId"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			int i = taskService.overrule(userContext, paramsMap);
			if (i > 0) {
				remoteResult.setStatus(true);
				remoteResult.setResultMsg("操作成功！");
				processService.rejectProcess(userContext, taskInstId, processInstId, userContext.getUID(), paramsMap);
			} else {
				remoteResult.setStatus(false);
				remoteResult.setResultMsg("操作失败！");
			}
		} catch (Exception e) {
			remoteResult.setResultMsg("操作异常！");
			remoteResult.setStatus(false);
		}
		return remoteResult.toString();
	}

	/**
	 * 任务评分(流程走向+更新数据)
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.assess")
	public String assess(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		String processInstId = StringUtils.nvlString(jsonMap.get("processInstId"));
		String taskInstId = StringUtils.nvlString(jsonMap.get("taskInstId"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			int i = taskService.assess(userContext, paramsMap, processInstId, taskInstId);
			if (i > 0) {
				remoteResult.setStatus(true);
				remoteResult.setResultMsg("评分成功！");
				processService.taskAssess(userContext, taskInstId, processInstId);
				processService.endProcess(userContext, paramsMap, processInstId);
			} else {
				remoteResult.setStatus(false);
				remoteResult.setResultMsg("评分失败！");
			}
		} catch (Exception e) {
			remoteResult.setResultMsg("评分异常！");
			remoteResult.setStatus(false);
		}
		return remoteResult.toString();
	}

	/**
	 * 续报审批通过(流程走向+更新数据)
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.resubmit_pass")
	public String resubmit_pass(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		String processInstId = StringUtils.nvlString(jsonMap.get("processInstId"));
		String taskInstId = StringUtils.nvlString(jsonMap.get("taskInstId"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			int i = taskService.pass(userContext, paramsMap, processInstId, taskInstId);
			if (i > 0) {
				remoteResult.setStatus(true);
				remoteResult.setResultMsg("操作成功！");
				processService.pass(userContext, taskInstId, processInstId);
				// processService.endProcess(userContext, paramsMap, processInstId);
			} else {
				remoteResult.setStatus(false);
				remoteResult.setResultMsg("操作失败！");
			}
		} catch (Exception e) {
			remoteResult.setResultMsg("操作异常！");
			remoteResult.setStatus(false);
		}
		return remoteResult.toString();
	}

	/**
	 * 续报审批驳回
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.rejected")
	public String rejected(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		String processInstId = StringUtils.nvlString(jsonMap.get("processInstId"));
		String taskInstId = StringUtils.nvlString(jsonMap.get("taskInstId"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			int i = taskService.rejected(userContext, paramsMap);
			if (i > 0) {
				remoteResult.setStatus(true);
				remoteResult.setResultMsg("操作成功！");
				processService.rejected(userContext, taskInstId, processInstId);
			} else {
				remoteResult.setStatus(false);
				remoteResult.setResultMsg("操作失败！");
			}
		} catch (Exception e) {
			remoteResult.setResultMsg("操作异常！");
			remoteResult.setStatus(false);
		}
		return remoteResult.toString();
	}

	/**
	 * 续报放弃
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.abandon")
	public String abandon(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		String processInstId = StringUtils.nvlString(jsonMap.get("processInstId"));
		String taskInstId = StringUtils.nvlString(jsonMap.get("taskInstId"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			int i = taskService.abandon(userContext, paramsMap);
			if (i > 0) {
				remoteResult.setStatus(true);
				remoteResult.setResultMsg("操作成功！");
				processService.abandon(userContext, taskInstId, processInstId);
			} else {
				remoteResult.setStatus(false);
				remoteResult.setResultMsg("操作失败！");
			}
		} catch (Exception e) {
			remoteResult.setResultMsg("操作异常！");
			remoteResult.setStatus(false);
		}
		return remoteResult.toString();
	}

	/**
	 * 查询对话人
	 * 
	 * @param userContext
	 * @param json
	 * @return
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getTalkingPerson")
	public String getTalkingPerson(UserContext userContext, Object json) {
		Map<String, Object> paramsMap = (Map<String, Object>) JSONObject.parse((String) json);
		RemoteResult remoteResult = new RemoteResult();
		try {
			List<Map<String, Object>> resultList = taskService.getTalkingPerson(userContext, paramsMap);
			remoteResult.setResultData(resultList);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 查看流程记录
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getProcessRecord")
	public String getProcessRecord(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		String processDefId = StringUtils.nvlString(jsonMap.get("processDefId"));
		String activityId = StringUtils.nvlString(jsonMap.get("activityId"));
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = Maps.newHashMap();
			resultMap.put("record", taskService.getProcessRecord_new(userContext, paramsMap));
			if (StringUtils.isNotEmpty(processDefId) && StringUtils.isNotEmpty(activityId)) {
				resultMap.put("action", BaseService.getPageActions(processDefId, activityId));
			}
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 查看沟通记录详情
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getTalkingDetail")
	public String getTalkingDetail(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = taskService.getTalkingDetail(userContext, paramsMap);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 获取起诉线索编号
	 * 
	 * @param userContext
	 * @param rwbh
	 * @return
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getClueSueXsbh")
	public String getClueSueXsbh(UserContext userContext, String rwbh) {
		return taskService.getReportRw_clue(userContext, rwbh);
	}

	/**
	 * updateClueSue
	 * 
	 * @param userContext
	 * @param json
	 * @return
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.updateClueSue")
	public String updateClueSue(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		RemoteResult remoteResult = new RemoteResult();
		try {
			int update = taskService.updateClueSue(userContext, paramsMap);
			if (!(update < 0)) {
				remoteResult.setStatus(true);
				remoteResult.setResultMsg("填写成功！");
			} else {
				remoteResult.setStatus(false);
				remoteResult.setResultMsg("填写失败");
			}
		} catch (Exception e) {
			remoteResult.setResultMsg("填写异常！");
			remoteResult.setStatus(false);
		}
		return remoteResult.toString();
	}

	/**
	 * 查询线索起诉情况
	 * 
	 * @param userContext
	 * @param json
	 * @return
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getClueSue")
	public String getClueSue(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = taskService.getClueSue(userContext, jsonMap);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 外部平台下发指令查询任务列表
	 * 
	 * @param userContext
	 * @param json
	 * @return
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getOtherReportTask")
	public String getOtherReportTask(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = taskService.getOtherReportTask(userContext, jsonMap, currPage, pageSize,
					sortField, sortType);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

}
