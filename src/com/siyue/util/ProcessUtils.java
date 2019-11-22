package com.siyue.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.curator.shaded.com.google.common.collect.Maps;

import com.actionsoft.bpms.bpmn.constant.TaskRuntimeConst;
import com.actionsoft.bpms.bpmn.engine.core.delegate.SimulationPath;
import com.actionsoft.bpms.bpmn.engine.model.def.UserTaskModel;
import com.actionsoft.bpms.bpmn.engine.model.run.delegate.ProcessInstance;
import com.actionsoft.bpms.bpmn.engine.model.run.delegate.TaskInstance;
import com.actionsoft.bpms.form.engine.FormEngineHelper;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.actionsoft.sdk.local.api.ProcessAPI;
import com.actionsoft.sdk.local.api.ProcessExecuteQuery;
import com.google.common.collect.Lists;

public class ProcessUtils {
	/**
	 * 启动一个流程
	 * 
	 * @param userContext     用户上下文
	 * @param processDefId    流程定义ID
	 * @param title           流程标题
	 * @param startEventDefId 流程开始节点
	 * @return Map 集合包含流程实例对象（ProcessInstance）和BPMN引擎执行结果（ProcessExecuteQuery）
	 */
	public static Map<String, Object> startProcess(UserContext userContext, String processDefId, String title, String startEventDefId, String user) {
		ProcessAPI processAPI = SDK.getProcessAPI();
		ProcessExecuteQuery start = null;
		ProcessInstance processInstance = processAPI.createProcessInstance(processDefId, userContext.getUID(), title);
		SDK.getProcessAPI().setVariable(processInstance, "handleUser", user);// 设置流程参与者（流程变量）
		if (org.apache.commons.lang.StringUtils.isNotBlank(startEventDefId)) {
			start = processAPI.start(processInstance, startEventDefId);
		}
		Map<String, Object> map = Maps.newHashMap();
		if (start.fetchAllTasks() != null) {
			// 设置流程变量
			Map<String, Object> variables = SDK.getProcessAPI().getVariables(processInstance);
			if (variables.containsKey("processBindId")) {
				SDK.getProcessAPI().setVariable(processInstance, "processBindId", processInstance.getId());
			}
			SDK.getLogAPI().consoleInfo("[" + processDefId + "]" + "流程开启成功");
			map.put("ProcessInstance", processInstance);
			map.put("ProcessExecuteQuery", start);
			return map;
		} else {
			return null;
		}
	}

	/**
	 * 启动指定节点任务
	 * 
	 * @param processInstanceId 流程实例ID
	 * @param taskInstance      要产生任务的当前人工任务实例对象，如果是流程启动时第1个产生的任务允许为null
	 * @param userContext       操作者对象
	 * @param activityDefId     目标节点定义Id(UserTask)
	 * @param participant       参与者,一个或多个账户（多个用空格隔开）
	 * @param title             任务标题
	 * @return TaskInstance 集合，每个参与者对应List结果的一个任务实例
	 */
	public static List<TaskInstance> createUserTaskInstance(UserContext userContext, String processInstanceId, TaskInstance taskInstance, String activityDefId, String participant, String title) {
		ProcessInstance ProcessInstance = SDK.getProcessAPI().getInstanceById(processInstanceId);
		return SDK.getTaskAPI().createUserTaskInstance(ProcessInstance, taskInstance, userContext, activityDefId, participant, title);
	}

	public static List<TaskInstance> createUserTaskInstance(UserContext userContext, ProcessInstance processInstance, TaskInstance taskInstance, String activityDefId, String participant, String title) {
		return SDK.getTaskAPI().createUserTaskInstance(processInstance, taskInstance, userContext, activityDefId, participant, title);
	}

	/**
	 * 获取当前节点的后续路线
	 * 
	 * @param userContext
	 * @param processInstance
	 * @param taskInstance
	 * @return
	 */
	public static List<SimulationPath> getNextPath(UserContext userContext, ProcessInstance processInstance, TaskInstance taskInstance) {
		return SDK.getTaskAPI().simulationNextPath(userContext, processInstance, taskInstance);
	}

	/**
	 * 获取流程任务实例
	 * 
	 * @param paramsMap 包含BPMN引擎执行结果对象（ProcessExecuteQuery）的集合
	 * @return TaskInstance 活动的任务实例对象
	 */
	public static TaskInstance getFirstTaskInstance(Map<String, Object> paramsMap) {
		TaskInstance task = null;
		ProcessExecuteQuery peq = (ProcessExecuteQuery) paramsMap.get("ProcessExecuteQuery");
		List<TaskInstance> tasks = peq.fetchActiveTasks();// 查询该次操作产生的活动任务，通常指该操作创建的新任务。该类任务一定处于中断等待状态，例如一个人工任务、一个捕获时间的中间事件任务
		if (tasks != null && tasks.size() > 0) {
			for (TaskInstance taskInstance : tasks) {
				if (taskInstance.getActivityType().equals(TaskRuntimeConst.ACTIVITY_TYPE_USERTASK)) {// 匹配人工任务
					task = taskInstance;
					break;
				}
			}
		}
		return task;
	}

	/**
	 * 完成任务，任务类型可以是普通人工任务、服务任务、子流程任务及 加签这类的特殊任务。如果评估可以达成向后推进的条件，自动向下推进。
	 * 
	 * @param userContext 操作者对象
	 * @param taskInstance 任务实例对象
	 * @return ProcessExecuteQuery 由ProcessExecuteQuery包装的引擎执行结果
	 */
	public static ProcessExecuteQuery completeTask(UserContext userContext, TaskInstance taskInstance) {
		return SDK.getTaskAPI().completeTask(taskInstance, userContext, true, true);
	}

	/**
	 * 流程走向
	 * 
	 * @param userContext   操作者对象
	 * @param taskInstId    任务实例对象
	 * @param processInstId 流程实例ID
	 * @return
	 */
	public static int processTrend(UserContext userContext, String taskInstId, String processInstId, String user) {
		TaskInstance taskInstance = SDK.getTaskAPI().getInstanceById(taskInstId);
		ProcessInstance processInstance = SDK.getProcessAPI().getInstanceById(processInstId);
		UserTaskModel userTaskModel = SDK.getRepositoryAPI().getUserTaskModel(processInstance.getProcessDefId(), taskInstance.getActivityDefId());
		boolean parallelReceiveTask = FormEngineHelper.getInstance().isParallelReceiveTask(processInstance, taskInstance, userTaskModel);
		if (parallelReceiveTask) {
			SDK.getTaskAPI().receiveTask(userContext, taskInstance);
		}
		ProcessUtils.completeTask(userContext, taskInstance);// 完成节点任务
		List<SimulationPath> listPath = SDK.getTaskAPI().simulationNextPath(userContext.getUID(), processInstId, taskInstId);
		if ("endEvent".equals(listPath.get(0).getType())) {
			return 1;
		} else {
			List<TaskInstance> list = null;
			for (SimulationPath path : listPath) {
				list = ProcessUtils.createUserTaskInstance(userContext, processInstId, taskInstance, path.getId(), user, "");// 启动节点任务
			}
			if (list != null) {
				return 1;
			}
			return 0;
		}
	}

	/**
	 * 流程走向(启动子流程Loading)
	 * 
	 * @param userContext   操作者对象
	 * @param taskInstId    任务实例对象
	 * @param processInstId 流程实例ID
	 * @return
	 */
	public static int processTrendCreateChild(UserContext userContext, String taskInstId, String processInstId) {
		SDK.getTaskAPI().completeTask(taskInstId, userContext.getUID(), true, true);// 完成节点任务
		TaskInstance startChild = null;
		String oldBindId = getProcessInfo(processInstId);
		List<SimulationPath> listPath = SDK.getTaskAPI().simulation2End(userContext.getUID(), processInstId, taskInstId);
		List<String> newBindIdList = Lists.newArrayList();
		for (SimulationPath path : listPath) {
			startChild = SDK.getTaskAPI().createCallActivityTaskInstance(processInstId, taskInstId, path.getId(), "");
			ProcessInstance childProcess = SDK.getProcessQueryAPI().parentProcessInstId(startChild.getProcessInstId()).detail();
			List<ProcessInstance> list = SDK.getProcessQueryAPI().parentProcessInstId(childProcess.getId()).list();
			if (list.size() > 0) {
				for (ProcessInstance processInstance : list) {
					newBindIdList.add(processInstance.getId());
				}
			} else {
				newBindIdList.add(childProcess.getId());
			}
		}
		Map<String, Object> variables = SDK.getProcessAPI().getVariables(SDK.getProcessAPI().getInstanceById(oldBindId));
		if (variables.containsKey("processBindId")) {
			String oldVar = StringUtils.nvlString(SDK.getProcessAPI().getVariable(oldBindId, "processBindId"));
			List<String> oldValList = new ArrayList<String>(Arrays.asList(oldVar.split(",")));
			int position = oldValList.indexOf(processInstId);
			if (position > -1) {
				oldValList.set(position, String.join(",", newBindIdList));
				SDK.getProcessAPI().setVariable(oldBindId, "processBindId", String.join(",", oldValList));// 设置流程变量
			} else {
				SDK.getProcessAPI().setVariable(oldBindId, "processBindId", String.join(",", newBindIdList));// 设置流程变量
			}

		}
		if (startChild != null) {
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * 查询顶级流程实例ID
	 * 
	 * @param processInstId
	 * @return
	 */
	public static String getProcessInfo(String processInstId) {
		String parentProcessInstId;
		String id;
		do {
			parentProcessInstId = DBSql.getString("SELECT PARENTPROCESSINSTID FROM WFC_PROCESS WHERE ID =?", new String[] { processInstId });
			if (org.apache.commons.lang.StringUtils.isEmpty(parentProcessInstId.trim())) {
				id = processInstId;
				break;
			} else {
				processInstId = parentProcessInstId;
			}
		} while (true);
		return id;
	}

	/**
	 *  获取当前线索活动的任务编号
	 * @param xsbh
	 * @return
	 */
	public static String getActiviteRwbh(String xsbh){
		//TODO 线索中的任务列表
		/*String rwbhs = DBSql.getString("select string_agg(rwbh ,',') from  BO_EU_XSHB_RW_GLXS where xsbh=?", new String[]{xsbh});
		List<String> rwbhList = new ArrayList<String>(Arrays.asList(rwbhs.split(",")));

		for (int i = 0; i < rwbhList.size(); i++) {
			String controlState = DBSql.getString("select controlstate from wfc_process where id=(select bindid from bo_eu_xshb_rw_lz where rwbh=?)", new String[]{rwbhList.get(i)});
			if("active".equalsIgnoreCase(controlState)){
				return rwbhList.get(i);
			}
		}*/
		return DBSql.getString("select lz.rwbh from BO_EU_XSHB_RW_GLXS glxs,bo_eu_xshb_rw_lz lz,wfc_process wfc where glxs.rwbh = lz.rwbh and wfc.id = lz.bindid and wfc.controlstate='active' and (glxs.rwbh != '' or glxs.rwbh !=null) and glxs.xsbh=? order by lz.createdate desc limit 1", new String[]{ xsbh });
	}

}
