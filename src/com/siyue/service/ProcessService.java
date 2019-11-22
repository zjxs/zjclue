package com.siyue.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.curator.shaded.com.google.common.collect.Maps;

import com.actionsoft.bpms.bo.engine.BO;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.siyue.util.ProcessUtils;
import com.siyue.util.ResourceUtils;
import com.siyue.util.StringUtils;

/**
 * 流程
 * 
 * @author MCH
 */
public class ProcessService {
	/*
	 * 流程变量 gatewayVariable： 签收：0 回退：1 反馈：2 转派：3 批复：4 评分：5 审批通过：6 审批驳回：7 签收无需反馈：8
	 */

	/**
	 * 01 常规线索 CGXS 02 贩枪线索 FQXS 03 任务续报 03
	 */
	public Map<String, Object> startProcess(UserContext userContext, String codeXslx, String user) {
		Map<String, String> map = getNoteId(codeXslx);
		return ProcessUtils.startProcess(userContext, map.get("processDefId"), "流程标题", map.get("startEventDefId"), user);
	}

	/**
	 * 签收
	 */
	public int signProcess(UserContext userContext, String taskInstId, String processInstId, Map<String, Object> paramsMap) {
		Map<String, Object> lzParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_LZ");
		String SFFK_PDBZ = DBSql.getString(ResourceUtils.getSQL("select_rw_sffk"), new String[] { StringUtils.nvlString(lzParamsMap.get("ID")) });
		if ("1".equals(SFFK_PDBZ) || !org.apache.commons.lang.StringUtils.isNotBlank(SFFK_PDBZ)) {
			SDK.getProcessAPI().setVariable(processInstId, "gatewayVariable", "0");// 设置流程变量
		} else {
			SDK.getProcessAPI().setVariable(processInstId, "gatewayVariable", "8");// 设置流程变量
		}
		BO lxBo = SDK.getBOAPI().get("BO_EU_XSHB_RW_LZ", StringUtils.nvlString(lzParamsMap.get("ID")));
		String rwlxdm = lxBo.getString("RWLXDM");
		String pid = lxBo.getString("PID");
		String SFZP_PDBZ = null;
		if (org.apache.commons.lang.StringUtils.isNotBlank(lxBo.getString("PID"))) {
			SFZP_PDBZ = DBSql.getString("SELECT SFZP_PDBZ FROM BO_EU_XSHB_RW_LZ WHERE RWLZBH =?", new String[] { pid });
		}
		if ("01".equals(rwlxdm) && "1".equals(SFZP_PDBZ)) {
			return ProcessUtils.processTrendCreateChild(userContext, taskInstId, processInstId);
		} else if ("02".equals(rwlxdm) && "1".equals(SFZP_PDBZ)) {
			return ProcessUtils.processTrendCreateChild(userContext, taskInstId, processInstId);
		} else {
			return ProcessUtils.processTrend(userContext, taskInstId, processInstId, userContext.getUID());
		}
	}

	/**
	 * 回退
	 */
	public int back(UserContext userContext, String taskInstId, String processInstId, String user, Map<String, Object> paramsMap) {
		Map<String, Object> lzParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_LZ");
		String SFFK_PDBZ = DBSql.getString(ResourceUtils.getSQL("select_rw_sffk"), new String[] { StringUtils.nvlString(lzParamsMap.get("ID")) });
		if (StringUtils.isNotEmpty(SFFK_PDBZ)) {// 该任务为下发任务
			if ("0".equals(SFFK_PDBZ)) {
				SDK.getProcessAPI().setVariable(processInstId, "gatewayVariable", "8");// 设置流程变量
			} else {
				SDK.getProcessAPI().setVariable(processInstId, "gatewayVariable", "1");// 设置流程变量
			}
		} else {
			SDK.getProcessAPI().setVariable(processInstId, "gatewayVariable", "1");// 设置流程变量
		}
		return ProcessUtils.processTrend(userContext, taskInstId, processInstId, DBSql.getString("SELECT CREATEUSER from WFC_PROCESS where id =? ", new String[] { processInstId }));
	}

	/**
	 * 签收无需反馈
	 */
	public int signNoFeedback(UserContext userContext, String taskInstId, String processInstId) {
		SDK.getProcessAPI().setVariable(processInstId, "gatewayVariable", "8");// 设置流程变量
		return ProcessUtils.processTrend(userContext, taskInstId, processInstId, userContext.getUID());
	}

	/**
	 * 驳回
	 */
	public int rejectProcess(UserContext userContext, String taskInstId, String processInstId, String user, Map<String, Object> paramsMap) {
//		Map<String, Object> lzParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_LZ");
//		String SFFK_PDBZ = DBSql.getString(ResourceUtils.getSQL("select_rw_sffk"), new String[] { StringUtils.nvlString(lzParamsMap.get("ID")) });
//		// 有值的话是下发任务为0则是不需要进行反馈
//		if (StringUtils.isNotEmpty(SFFK_PDBZ)) {// 该任务为下发任务
//			if ("0".equals(SFFK_PDBZ)) {
//				SDK.getProcessAPI().setVariable(processInstId, "gatewayVariable", "10");// 设置流程变量
//			}
//		} else {
//			SDK.getProcessAPI().setVariable(processInstId, "gatewayVariable", "7");// 设置流程变量
//		}
		SDK.getProcessAPI().setVariable(processInstId, "gatewayVariable", "7");// 设置流程变量
		return ProcessUtils.processTrend(userContext, taskInstId, processInstId, user);
	}

	/**
	 * 通过
	 */
	public int passProcess(UserContext userContext, String taskInstId, String processInstId) {
		SDK.getProcessAPI().setVariable(processInstId, "gatewayVariable", "6");// 设置流程变量
		return ProcessUtils.processTrend(userContext, taskInstId, processInstId, DBSql.getString("select OWNER from  WFC_TASK where id =? ", new String[] { taskInstId }));
	}

	/**
	 * 评分
	 */
	public int taskAssess(UserContext userContext, String taskInstId, String processInstId) {
		SDK.getProcessAPI().setVariable(processInstId, "gatewayVariable", "5");// 设置流程变量
		return ProcessUtils.processTrend(userContext, taskInstId, processInstId, userContext.getUID());
	}

	/**
	 * 转派
	 */
	public int transfer(UserContext userContext, String taskInstId, String processInstId, String user) {
		SDK.getProcessAPI().setVariable(processInstId, "gatewayVariable", "3");// 设置流程变量
		return ProcessUtils.processTrendCreateChild(userContext, taskInstId, processInstId);
	}

	/**
	 * 转派回退
	 */
	public int transferBack(UserContext userContext, String taskInstId, String processInstId, String user) {
		SDK.getProcessAPI().setVariable(processInstId, "gatewayVariable", "9");// 设置流程变量
		return ProcessUtils.processTrendCreateChild(userContext, taskInstId, processInstId);
	}

	/**
	 * 反馈
	 */
	public int feedBack(UserContext userContext, String taskInstId, String processInstId) {
		SDK.getProcessAPI().setVariable(processInstId, "gatewayVariable", "2");// 设置流程变量
		return ProcessUtils.processTrendCreateChild(userContext, taskInstId, processInstId);

	}

	/**
	 * 批复
	 */
	public int reply(UserContext userContext, String taskInstId, String processInstId) {
		SDK.getProcessAPI().setVariable(processInstId, "gatewayVariable", "4");// 设置流程变量
		return ProcessUtils.processTrendCreateChild(userContext, taskInstId, processInstId);

	}

	/**
	 *  任务续报 续报操作
	 * @param userContext
	 * @param taskInstId
	 * @param processInstId
	 * @return
	 */
	public int resubmit(UserContext userContext, String taskInstId, String processInstId){
		SDK.getProcessAPI().setVariable(processInstId, "gatewayVariable", "1");// 设置流程变量
		String user = DBSql.getString("SELECT TARGET from WFH_TASK WHERE PROCESSINSTID=? AND ACTIVITYDEFID=? AND CONTROLSTATE='complete'",new String[]{processInstId,"obj_c88d37f218300001856dc2c64f301089"});
		return ProcessUtils.processTrend(userContext, taskInstId, processInstId,user);
	}

	/**
	 *  任务续报 放弃操作
	 * @param userContext
	 * @param taskInstId
	 * @param processInstId
	 * @return
	 */
	public int abandon(UserContext userContext, String taskInstId, String processInstId){
		SDK.getProcessAPI().setVariable(processInstId, "gatewayVariable", "0");// 设置流程变量
		return ProcessUtils.processTrend(userContext, taskInstId, processInstId,userContext.getUID());
	}

	/**
	 * 任务续报 审批通过
	 * @param userContext
	 * @param taskInstId
	 * @param processInstId
	 * @return
	 */
	public int pass(UserContext userContext, String taskInstId, String processInstId){
		SDK.getProcessAPI().setVariable(processInstId, "gatewayVariable", "2");// 设置流程变量
		return ProcessUtils.processTrend(userContext, taskInstId, processInstId,userContext.getUID());
	}

	/**
	 * 任务续报 审批驳回
	 * @param userContext
	 * @param taskInstId
	 * @param processInstId
	 * @return
	 */
	public int rejected(UserContext userContext, String taskInstId, String processInstId){
		SDK.getProcessAPI().setVariable(processInstId, "gatewayVariable", "3");// 设置流程变量
		return ProcessUtils.processTrend(userContext, taskInstId, processInstId,DBSql.getString("SELECT CREATEUSER from WFC_PROCESS where id =? ", new String[] { processInstId }));
	}

	/**
	 *  任务续报 回退操作
	 * @param userContext
	 * @param taskInstId
	 * @param processInstId
	 * @return
	 */
	public int resubmit_back(UserContext userContext, String taskInstId, String processInstId){
		SDK.getProcessAPI().setVariable(processInstId, "gatewayVariable", "4");// 设置流程变量
		return ProcessUtils.processTrend(userContext, taskInstId, processInstId,userContext.getUID());
	}

	/**
	 *  任务续报 签收操作
	 * @param userContext
	 * @param taskInstId
	 * @param processInstId
	 * @return
	 */
	public int resubmit_sign(UserContext userContext, String taskInstId, String processInstId){
		SDK.getProcessAPI().setVariable(processInstId, "gatewayVariable", "5");// 设置流程变量
		return ProcessUtils.processTrend(userContext, taskInstId, processInstId,userContext.getUID());
	}

	/**
	 * 流程ID和节点ID（启动）
	 * 
	 * @param codeXslx
	 * @return
	 */
	public Map<String, String> getNoteId(String codeXslx) {
		String processDefId = null;
		String startEventDefId = null;
		switch (codeXslx) {
		case "01":// 下发
			processDefId = "obj_188b84fe70714d38ae3ecc0c5ac2bf6f";
			startEventDefId = "obj_c84b91465fd000011f97146649fc1c34";
			break;
		case "02":// 上报
			processDefId = "obj_b3755ed9eef149dbac839b9d3ec12556";
			startEventDefId = "obj_c84b93a4f8f000013dcc1330aed0b020";
			break;
		case "03":// 续报
			processDefId = "obj_05f7017133f048078367d3cada30f85c";
			startEventDefId = "obj_c88ba124ad5000017bb25c8ae009109a";
			break;
		}
		Map<String, String> map = Maps.newHashMap();
		map.put("processDefId", processDefId);
		map.put("startEventDefId", startEventDefId);
		return map;
	}

	/**
	 * 关闭同一逃犯线索的其他任务
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @param processInstID
	 */
	public static void endProcess(UserContext userContext, Map<String, Object> paramsMap, String processInstID) {
		Map<String, Object> rwlz = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_LZ");
		String rwbh = StringUtils.nvlString(rwlz.get("RWBH"));
		String xflxdm = DBSql.getString("select xflxdm from BO_EU_XSHB_RW_XF where rwbh=?", new String[] { rwbh });
		if ("03".equals(xflxdm)) {
			String arryRwbh = DBSql.getString(ResourceUtils.getSQL("select_escapeeRwbh"), new String[] { rwbh, rwbh });
			String[] split;
			if (StringUtils.isNotEmpty(arryRwbh)) {
				split = arryRwbh.split(",");
			} else {
				split = new String[] { rwbh };
			}
			List<String> rwbhList = Arrays.asList(split);
			List<String> bindidList = Arrays.asList(DBSql.getString(ResourceUtils.getSQL("select_escapeeRwlzbh"), new String[] { StringUtils.join(rwbhList, "','"), ProcessUtils.getProcessInfo(processInstID) }));

			for (String binid : bindidList) {
				if (!SDK.getProcessAPI().isEndById(binid)) {
					SDK.getProcessAPI().cancelById(binid, userContext.getUID());// 删除流程
				}
			}
			// 更新下发任务状态
			DBSql.update("update BO_EU_XSHB_RW_XF set RWZTDM=? where rwbh  in(?)", new String[] { "10", StringUtils.join(rwbhList, "','") });
			// 更新任务流转状态
			DBSql.update("update BO_EU_XSHB_RW_LZ set LZZTDM=? where rwbh  in(?)", new String[] { "05", StringUtils.join(bindidList, "','") });
		}
	}

}
