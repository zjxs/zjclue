package com.siyue.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.commons.database.RowMap;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.server.bind.annotation.Controller;
import com.actionsoft.bpms.server.bind.annotation.Mapping;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.siyue.service.ClueService;
import com.siyue.service.ProcessService;
import com.siyue.util.ProcessUtils;
import com.siyue.util.SQLUtils;
import com.siyue.util.StringUtils;
import com.siyue.vo.RemoteResult;

@Controller
public class ClueController {

	private ClueService clueService = new ClueService();

	private ProcessService processService = new ProcessService();

	/**
	 * 查询我的线索
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getMyClue")
	public String getMyClue(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = clueService.getMyClue(userContext, paramsMap, currPage, pageSize, sortField, sortType);
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
	 * 查询辖区线索
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getAreaClue")
	public String getAreaClue(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = clueService.getAreaClue(userContext, paramsMap, currPage, pageSize, sortField, sortType);
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
	 * 删除线索
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.deleteClue")
	public String deleteClue(UserContext userContext, Object json) {
		Map<String, Object> map = (Map<String, Object>) JSON.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) map.get("data");
		RemoteResult remoteResult = new RemoteResult();
		try {
			int delete = clueService.deleteClue(userContext, paramsMap);
			if (delete > 0) {
				remoteResult.setStatus(true);
				remoteResult.setResultMsg("线索删除成功！");
			} else {
				remoteResult.setStatus(false);
				remoteResult.setResultMsg("线索删除失败！");
			}
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setResultMsg("线索删除异常！");
			remoteResult.setStatus(false);
		}
		return remoteResult.toString();
	}

	/**
	 * 更新线索
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.updateClue")
	public String updateClue(UserContext userContext, Object json) {
		Map<String, Object> map = (Map<String, Object>) JSON.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) map.get("data");
		RemoteResult remoteResult = new RemoteResult();
		Map<String, Object> returnMap = Maps.newHashMap();
		Connection connection = null;
		try {
			connection = DBSql.open();
			connection.setAutoCommit(false);
			int[] update = clueService.updateClue(userContext, paramsMap, connection, returnMap);
			if (!Arrays.asList(update).contains(0)) {
				remoteResult.setResultData(returnMap);
				remoteResult.setStatus(true);
				remoteResult.setResultMsg("线索更新成功！");
			} else {
				remoteResult.setStatus(false);
				remoteResult.setResultMsg("线索更新失败！");
			}
			connection.commit();
		} catch (Exception e) {
			try {
				remoteResult.setResultMsg("线索更新异常！");
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
	 * 更新续报任务
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.updateResubmit")
	public String updateResubmit(UserContext userContext, Object json) {
		Map<String, Object> map = (Map<String, Object>) JSON.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) map.get("data");
		String processInstId = StringUtils.nvlString(paramsMap.remove("processInstId"));
		String taskInstId = StringUtils.nvlString(paramsMap.remove("taskInstId"));
		RemoteResult remoteResult = new RemoteResult();
		Map<String, Object> returnMap = Maps.newHashMap();
		Connection connection = null;
		try {
			connection = DBSql.open();
			connection.setAutoCommit(false);
			int[] update = clueService.updateResubmit(userContext, paramsMap, connection, returnMap);
			if (!Arrays.asList(update).contains(0)) {
				remoteResult.setResultData(returnMap);
				remoteResult.setStatus(true);
				remoteResult.setResultMsg("续报内容更新成功！");
				processService.resubmit(userContext,taskInstId,processInstId);
			} else {
				remoteResult.setStatus(false);
				remoteResult.setResultMsg("续报内容更新失败！");
			}
			connection.commit();
		} catch (Exception e) {
			try {
				remoteResult.setResultMsg("续报内容更新异常！");
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
	 * 更新续报任务
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.updateResubmite")
	public String updateResubmite(UserContext userContext, Object json) {
		Map<String, Object> map = (Map<String, Object>) JSON.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) map.get("data");
		String processInstId = StringUtils.nvlString(map.get("processInstId"));
		String taskInstId = StringUtils.nvlString(map.get("taskInstId"));
		RemoteResult remoteResult = new RemoteResult();
		Map<String, Object> returnMap = Maps.newHashMap();
		Connection connection = null;
		try {
			connection = DBSql.open();
			connection.setAutoCommit(false);
			int[] update = clueService.updateResubmite(userContext, paramsMap, connection, returnMap);
			if (!Arrays.asList(update).contains(0)) {
				processService.resubmit(userContext,taskInstId,processInstId);
				remoteResult.setResultData(returnMap);
				remoteResult.setStatus(true);
				remoteResult.setResultMsg("续报内容更新成功！");
			} else {
				remoteResult.setStatus(false);
				remoteResult.setResultMsg("续报内容更新失败！");
			}
			connection.commit();
		} catch (Exception e) {
			try {
				remoteResult.setResultMsg("续报内容更新异常！");
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
	 * 新增线索
	 *
	 * @param userContext
	 * @param json
	 * @return
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.addClue")
	public String addClue(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		RemoteResult remoteResult = new RemoteResult();
		Map<String, Object> returnMap = Maps.newHashMap();
		Connection connection = null;
		try {
			connection = DBSql.open();
			connection.setAutoCommit(false);
			int insert = clueService.addClue(userContext, paramsMap, returnMap, connection);
			if (insert > 0) {
				remoteResult.setResultData(returnMap);
				remoteResult.setStatus(true);
				remoteResult.setResultMsg("保存成功！");
			} else {
				remoteResult.setStatus(false);
				remoteResult.setResultMsg("保存失败！");
			}
			connection.commit();
		} catch (Exception e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			remoteResult.setResultMsg("保存异常！");
			remoteResult.setStatus(false);
		}
		return remoteResult.toString();
	}

	/**
	 * 新建线索续报功能
	 * @param userContext
	 * @param json
	 * @return
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.addClueResubmit")
	public String addClueResubmit(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		RemoteResult remoteResult = new RemoteResult();
		Map<String, Object> returnMap = Maps.newHashMap();
		Connection connection = null;
		try {
			connection = DBSql.open();
			connection.setAutoCommit(false);
			int[] insert = clueService.resubmit(userContext, paramsMap, returnMap, connection);
			if (!Arrays.asList(insert).contains(0)) {
				remoteResult.setResultData(returnMap);
				remoteResult.setStatus(true);
				remoteResult.setResultMsg("保存成功！");
			} else {
				remoteResult.setStatus(false);
				remoteResult.setResultMsg("保存失败！");
			}
			connection.commit();
		} catch (Exception e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			remoteResult.setResultMsg("保存异常！");
			remoteResult.setStatus(false);
		}
		return remoteResult.toString();
	}

	/**
	 * 查询线索关联任务
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getClueRelevanceTask")
	public String getClueRelevanceTask(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = clueService.getClueRelevanceTask(userContext, paramsMap, currPage, pageSize, sortField, sortType);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 导入关联物流数据
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.importRelevanceLogistics")
	public String importRelevanceLogistics(UserContext userContext, Object json) {
		Map<String, Object> paramsMap = (Map<String, Object>) JSONObject.parse((String) json);
		RemoteResult remoteResult = new RemoteResult();
		Map<String, Object> returnMap = Maps.newHashMap();
		try {
			int[] batch = clueService.importRelevanceLogistics(userContext, paramsMap, returnMap);
			if (!Arrays.asList(batch).contains(0)) {
				remoteResult.setResultData(returnMap);
				remoteResult.setStatus(true);
				remoteResult.setResultMsg("保存成功！");
			}
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("保存异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 更新关联物流数据
	 * 
	 * @param userContext
	 * @param json
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.updateRelevanceLogistics")
	public String updateRelevanceLogistics(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		RemoteResult remoteResult = new RemoteResult();
		try {
			int update = clueService.updateRelevanceLogistics(userContext, paramsMap);
			if (update > 0) {
				remoteResult.setStatus(true);
				remoteResult.setResultMsg("更新成功！");
			} else {
				remoteResult.setStatus(false);
				remoteResult.setResultMsg("更新失败！");
			}
		} catch (Exception e) {
			remoteResult.setResultMsg("更新异常！");
			remoteResult.setStatus(false);
		}
		return remoteResult.toString();
	}

	/**
	 * 删除关联物流数据
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.deleteRelevanceLogistics")
	public String deleteRelevanceLogistics(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		RemoteResult remoteResult = new RemoteResult();
		try {
			int delete = clueService.deleteRelevanceLogistics(userContext, paramsMap);
			if (delete > 0) {
				remoteResult.setResultMsg("删除成功！");
				remoteResult.setStatus(true);
			} else {
				remoteResult.setResultMsg("删除失败！");
				remoteResult.setStatus(false);
			}
		} catch (Exception e) {
			remoteResult.setResultMsg("删除异常！");
			remoteResult.setStatus(false);
		}
		return remoteResult.toString();
	}

	/**
	 * 查询关联物流
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getClueRelevanceLogistics")
	public String getClueRelevanceLogistics(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = clueService.getClueRelevanceLogistics(userContext, paramsMap, currPage, pageSize, sortField, sortType);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 删除关联逃犯
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.deleteRelevanceEscapee")
	public String deleteRelevanceEscapee(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		RemoteResult remoteResult = new RemoteResult();
		try {
			int delete = clueService.deleteRelevanceEscapee(userContext, paramsMap);
			if (delete > 0) {
				remoteResult.setResultMsg("删除成功！");
				remoteResult.setStatus(true);
			} else {
				remoteResult.setResultMsg("删除失败！");
				remoteResult.setStatus(false);
			}
		} catch (Exception e) {
			remoteResult.setResultMsg("删除异常！");
			remoteResult.setStatus(false);
		}
		return remoteResult.toString();
	}

	/**
	 * 新增关联逃犯
	 *
	 * @param userContext
	 * @param json
	 * @return
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.addRelevanceEscapee")
	public String addRelevanceEscapee(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSON.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		RemoteResult remoteResult = new RemoteResult();
		Map<String, Object> returnMap = Maps.newHashMap();
		try {
			int batch[] = clueService.addRelevanceEscapee(userContext, paramsMap, returnMap);
			if (!Arrays.asList(batch).contains(0)) {
				remoteResult.setResultMsg("新增成功！");
				remoteResult.setResultData(returnMap);
				remoteResult.setStatus(true);
			} else {
				remoteResult.setResultMsg("新增失败！");
				remoteResult.setStatus(false);
			}
		} catch (Exception e) {
			remoteResult.setResultMsg("新增异常！");
			remoteResult.setStatus(false);
		}
		return remoteResult.toString();
	}

	/**
	 * 查询关联逃犯
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getClueRelevanceEscapee")
	public String getClueRelevanceEscapee(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = clueService.getClueRelevanceEscapee(userContext, paramsMap, currPage, pageSize, sortField, sortType);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 查询逃犯轨迹
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getEscapeeTrail")
	public String getEscapeeTrail(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = clueService.getEscapeeTrail(userContext, paramsMap, currPage, pageSize, sortField, sortType);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}
	/**
	 * 撤回操作
	 * @param userContext
	 * @param rwbh 
	 * @param classify
	 * @param action
	 * @return
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.revokeProcess")
	public String revokeProcess(UserContext userContext, String rwbh,String classify, String action) {
		RemoteResult remoteResult = new RemoteResult();
		try {
			int[] batch = clueService.revokeProcess(userContext, rwbh, classify);
			if (!Arrays.asList(batch).contains(0)) {
				remoteResult.setStatus(true);
				remoteResult.setResultMsg("撤回成功！");
			}
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("撤回异常！");
		}
		return remoteResult.toString();
	}
	
	
	/**
	 * 更新任务
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.updateRw")
	public String updateRw(UserContext userContext, Object json) {
		Map<String, Object> map = (Map<String, Object>) JSON.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) map.get("data");
		RemoteResult remoteResult = new RemoteResult();
		Map<String, Object> returnMap = Maps.newHashMap();
		Connection connection = null;
		try {
			connection = DBSql.open();
			connection.setAutoCommit(false);
			int[] update = clueService.updateRw(userContext, paramsMap, connection, returnMap);
			if (!Arrays.asList(update).contains(0)) {
				remoteResult.setResultData(returnMap);
				remoteResult.setStatus(true);
				remoteResult.setResultMsg("任务更新成功！");
			} else {
				remoteResult.setStatus(false);
				remoteResult.setResultMsg("任务更新失败！");
			}
			connection.commit();
		} catch (Exception e) {
			try {
				remoteResult.setResultMsg("任务更新异常！");
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
	 * 更新用户密码
	 * @param context
	 * @return
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.updateUser")
	public String updateUser(UserContext context,String type){
		List<RowMap> rows = DBSql.getMaps("select userid,password from orguser");
		for (int i = 0; i < rows.size(); i++) {
			String uid = rows.get(i).getString("userid");
			System.out.println(uid+">>>>>>>>>"+i+">>>>>>>>>"+ uid);
			if("init".equals(type) ){
				System.out.println("初始化用户密码");
				//初始化用户密码
				SDK.getORGAPI().initUserPWD(uid);
			}else if("update".equals(type)){
				//修改用户密码
				System.out.println("修改用户密码");
				SDK.getORGAPI().changeUserPWD(uid, "1", "2");
			}
		}
		return rows.toString();
	}
	
}
