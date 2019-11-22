package com.siyue.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.bo.engine.BO;
import com.actionsoft.bpms.bpmn.engine.model.run.delegate.ProcessInstance;
import com.actionsoft.bpms.commons.database.RowMap;
import com.actionsoft.bpms.org.model.DepartmentModel;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.siyue.util.ProcessUtils;
import com.siyue.util.ResourceUtils;
import com.siyue.util.SQLUtils;
import com.siyue.util.SequenceUtils;
import com.siyue.util.StringUtils;

public class TaskService extends BaseService {

	private CommonService commonService = new CommonService();
	private ProcessService processService = new ProcessService();

	/**
	 * 查询当前单位其他用户
	 * 
	 * @param userContext
	 * @return
	 */
	public List<RowMap> getUnitOtherUser(UserContext userContext) {
		String sql = ResourceUtils.getSQL("select_bdwyh");
		List<RowMap> rows = DBSql.getMaps(sql,
				new Object[] { userContext.getUID(), userContext.getDepartmentModel().getId() });
		return rows;
	}

	/**
	 * 查询线索来源
	 */
	public List<RowMap> getClueSource(UserContext userContext) {
		String sql = ResourceUtils.getSQL("select_xsly");
		List<RowMap> rows = DBSql.getMaps(sql);
		return rows;
	}

	/**
	 * 任务关联线索
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @param currPage
	 * @param pageSize
	 * @param sortField
	 * @param sortType
	 * @return
	 */
	public Map<String, Object> getTaskRelevanceClue(UserContext userContext, Map<String, Object> paramsMap,
			int currPage, int pageSize, String sortField, String sortType) {
		String sql = ResourceUtils.getSQL("select_rwglxs") + SQLUtils.spellWhere(paramsMap);
		System.out.println(sql);
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql), new Object[] {});
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortField)
				&& org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql += " order by " + sortField + " " + sortType;
		} else {
			sql += " ORDER BY DJSJ DESC";
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql, currPage, pageSize), new Object[] {});

		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("DJSJ", "DATE");
		columnConfigMap.put("XSJBDM", "CODE_XSHB_JB");
		columnConfigMap.put("XSLXDM", "CODE_XSHB_XSLX");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 上报任务
	 * 
	 * @param userContext
	 * @return
	 */
	public RowMap getReportTask(UserContext userContext, Map<String, Object> paramsMap, boolean needTransaction) {
		String sql = ResourceUtils.getSQL("select_sbrw") + SQLUtils.spellWhere(paramsMap);
		RowMap row = DBSql.getMap(sql);
		if (needTransaction) {
			Map<String, String> columnConfigMap = Maps.newHashMap();
			columnConfigMap.put("SBSJ", "DATETIME");
			columnConfigMap.put("SFSMDM", "CODE_XSHB_SFSM");
			columnConfigMap.put("SBLXDM", "CODE_XSHB_SBLX");
			columnConfigMap.put("RWZTDM", "CODE_XSHB_RWZT");
			columnConfigMap.put("RWJBDM", "CODE_XSHB_JB");
			CodeService.resultOfTransaction(columnConfigMap, row);
		}
		return row;
	}

	/**
	 * 上报任务
	 *
	 * @param userContext
	 * @return
	 */
	public RowMap getResubmitTask(UserContext userContext, Map<String, Object> paramsMap, boolean needTransaction) {
		String sql = ResourceUtils.getSQL("select_sbrw") + SQLUtils.spellWhere(paramsMap);
		RowMap row = DBSql.getMap(sql);
		if (needTransaction) {
			Map<String, String> columnConfigMap = Maps.newHashMap();
			columnConfigMap.put("DJSJ", "DATETIME");
			columnConfigMap.put("SFSMDM", "CODE_XSHB_SFSM");
			columnConfigMap.put("SBLXDM", "CODE_XSHB_SBLX");
			columnConfigMap.put("RWZTDM", "CODE_XSHB_RWZT");
			columnConfigMap.put("RWJBDM", "CODE_XSHB_JB");
			CodeService.resultOfTransaction(columnConfigMap, row);
		}
		return row;
	}

	/**
	 * 续报任务
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @param needTransaction
	 * @return
	 */
	public RowMap getClueResubmitTask(UserContext userContext, Map<String, Object> paramsMap, boolean needTransaction) {
		String sql = ResourceUtils.getSQL("select_clueResubmit");
		String rwbh = StringUtils.nvlString(paramsMap.get("XBBH"));
		RowMap row = DBSql.getMap(sql, new String[] { rwbh, rwbh });
		if (needTransaction) {
			Map<String, String> columnConfigMap = Maps.newHashMap();
			columnConfigMap.put("DJSJ", "DATETIME");
			columnConfigMap.put("RWZTDM", "CODE_XSHB_RWZT");
			columnConfigMap.put("RWJBDM", "CODE_XSHB_JB");
			columnConfigMap.put("XSJBDM", "CODE_XSHB_JB");
			CodeService.resultOfTransaction(columnConfigMap, row);
		}
		return row;
	}

	/**
	 * 我的上报任务
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @param currPage
	 * @param pageSize
	 * @param sortField
	 * @param sortType
	 * @return
	 */
	public Map<String, Object> getMyReportTask(UserContext userContext, Map<String, Object> paramsMap, int currPage,
			int pageSize, String sortField, String sortType) {
		paramsMap.put("SBRY_GMSFHM", userContext.getUID());
		String sql = ResourceUtils.getSQL("select_sbrw");
		sql = "select a.* from(" + sql + ") a where 1=1 ";
		if (paramsMap.containsKey("JSDW_GAJGJGDM")) {
			sql += " and JSDW_GAJGJGDM like '%" + StringUtils.nvlString(paramsMap.remove("JSDW_GAJGJGDM")) + "%' ";
		}
		if (paramsMap.containsKey("JSRY_XM")) {
			sql += " and JSRY_XM like '%" + StringUtils.nvlString(paramsMap.remove("JSRY_XM")) + "%' ";
		}
		sql += SQLUtils.spellWhere(paramsMap);
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql), new Object[] {});
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortField)
				&& org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql += " order by " + sortField + " " + sortType;
		} else {
			sql += " order by SBSJ desc ";
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql, currPage, pageSize), new Object[] {});
		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("SBLXDM", "CODE_XSHB_SBLX");
		columnConfigMap.put("RWDJ", "CODE_XSHB_JB");
		columnConfigMap.put("SFSMDM", "CODE_XSHB_SFSM");
		columnConfigMap.put("SBSJ", "DATETIME");
		columnConfigMap.put("RWZTDM", "CODE_XSHB_RWZT");
		columnConfigMap.put("JSDW_GAJGJGDM", "CODE_GXS");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 下级上报任务
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @param currPage
	 * @param pageSize
	 * @param sortField
	 * @param sortType
	 * @return
	 */
	public Map<String, Object> getSubReportTask(UserContext userContext, Map<String, Object> paramsMap, int currPage,
			int pageSize, String sortField, String sortType) {
		String sql = ResourceUtils.getSQL("select_sbrw");
		sql += " and sb.SBDW_GAJGJGDM in (select ID from ORGDEPARTMENT where PARENTDEPARTMENTID = ?) ";
		sql = "select a.* from(" + sql + ") a";
		// 过滤是否回退
		// sql += " where a.RWBH not in (select DISTINCT lz.rwbh from bo_eu_xshb_rw_sb
		// sb, bo_eu_xshb_rw_lz lz where sb.rwbh=lz.rwbh and lz.sfht_pdbz='1' and
		// (lz.HTRY_GMSFHM = ? or (lz.JSDXLXDM = '02' and lz.HTDW_GAJGJGDM = ?)))";
		// 过滤是否涉密
		sql += " where (a.SFSMDM='01' or(a.SFSMDM='02' and ((a.JSDW_GAJGJGDM LIKE '%"
				+ userContext.getDepartmentModel().getId() + "%' and a.JSDXLXDM like '%02%') or '"
				+ userContext.getUID() + "' in (select JSRY_GMSFHM from BO_EU_XSHB_RW_LZ lz where lz.rwbh=a.rwbh)))) ";
		if (paramsMap.containsKey("JSDW_GAJGJGDM")) {
			sql += " and JSDW_GAJGJGDM like '%" + StringUtils.nvlString(paramsMap.remove("JSDW_GAJGJGDM")) + "%' ";
		}
		if (paramsMap.containsKey("JSRY_XM")) {
			sql += " and JSRY_XM like '%" + StringUtils.nvlString(paramsMap.remove("JSRY_XM")) + "%' ";
		}
		sql += SQLUtils.spellWhere(paramsMap);
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql),
				new Object[] { userContext.getDepartmentModel().getId() });
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortField)
				&& org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql += " order by " + sortField + " " + sortType;
		} else {
			sql += " order by SBSJ desc ";
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql, currPage, pageSize),
				new Object[] { userContext.getDepartmentModel().getId() });
		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("SBLXDM", "CODE_XSHB_SBLX");
		columnConfigMap.put("RWDJ", "CODE_XSHB_JB");
		columnConfigMap.put("SFSMDM", "CODE_XSHB_SFSM");
		columnConfigMap.put("SBSJ", "DATETIME");
		columnConfigMap.put("RWZTDM", "CODE_XSHB_RWZT");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 下级续报任务
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @param currPage
	 * @param pageSize
	 * @param sortField
	 * @param sortType
	 * @return
	 */
	public Map<String, Object> getSubResubmitTask(UserContext userContext, Map<String, Object> paramsMap, int currPage,
			int pageSize, String sortField, String sortType) {
		String sql = ResourceUtils.getSQL("select_xbrw");
		sql += " and xb.XBDJDW_GAJGJGDM in (select ID from ORGDEPARTMENT where PARENTDEPARTMENTID = ?) ";
		sql = "select a.* from(" + sql + ") a";
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql),
				new Object[] { userContext.getDepartmentModel().getId() });
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortField)
				&& org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql += " order by " + sortField + " " + sortType;
		} else {
			sql += " order by a.DJSJ desc ";
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql, currPage, pageSize),
				new Object[] { userContext.getDepartmentModel().getId() });
		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("DJSJ", "DATETIME");
		columnConfigMap.put("RWZTDM", "CODE_XSHB_RWZT");
		columnConfigMap.put("LZ_RWZTDM", "CODE_XSHB_RWZT");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 我的续报任务
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @param currPage
	 * @param pageSize
	 * @param sortField
	 * @param sortType
	 * @return
	 */
	public Map<String, Object> getMyResubmitTask(UserContext userContext, Map<String, Object> paramsMap, int currPage,
			int pageSize, String sortField, String sortType) {
		paramsMap.put("XBDJRY_GMSFHM", userContext.getUID());
		String sql = ResourceUtils.getSQL("select_xbrw");
		sql = "select a.* from(" + sql + ") a where 1=1 ";
		sql += SQLUtils.spellWhere(paramsMap);
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql), new Object[] {});
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortField)
				&& org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql += " order by " + sortField + " " + sortType;
		} else {
			sql += " order by DJSJ desc ";
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql, currPage, pageSize), new Object[] {});
		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("DJSJ", "DATETIME");
		columnConfigMap.put("RWZTDM", "CODE_XSHB_RWZT");
		columnConfigMap.put("LZ_RWZTDM", "CODE_XSHB_RWZT");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 单位上报任务
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @param currPage
	 * @param pageSize
	 * @param sortField
	 * @param sortType
	 * @return
	 */
	public Map<String, Object> getUnitReportTask(UserContext userContext, Map<String, Object> paramsMap, int currPage,
			int pageSize, String sortField, String sortType) {
		paramsMap.put("SFSMDM", "01");
		paramsMap.put("SBDW_GAJGJGDM", userContext.getDepartmentModel().getId());
		String sql = ResourceUtils.getSQL("select_sbrw");
		sql = "select a.* from(" + sql + ") a where 1=1 ";
		if (paramsMap.containsKey("JSDW_GAJGJGDM")) {
			sql += " and JSDW_GAJGJGDM like '%" + StringUtils.nvlString(paramsMap.remove("JSDW_GAJGJGDM")) + "%' ";
		}
		if (paramsMap.containsKey("JSRY_XM")) {
			sql += " and JSRY_XM like '%" + StringUtils.nvlString(paramsMap.remove("JSRY_XM")) + "%' ";
		}
		sql += SQLUtils.spellWhere(paramsMap);
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql), new Object[] {});
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortField)
				&& org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql += " order by " + sortField + " " + sortType;
		} else {
			sql += " order by SBSJ desc ";
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql, currPage, pageSize));

		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("SBLXDM", "CODE_XSHB_SBLX");
		columnConfigMap.put("RWDJ", "CODE_XSHB_JB");
		columnConfigMap.put("SFSMDM", "CODE_XSHB_SFSM");
		columnConfigMap.put("SBSJ", "DATETIME");
		columnConfigMap.put("RWZTDM", "CODE_XSHB_RWZT");
		columnConfigMap.put("JSDW_GAJGJGDM", "CODE_GXS");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 辖区上报任务
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @param currPage
	 * @param pageSize
	 * @param sortField
	 * @param sortType
	 * @return
	 */
	public Map<String, Object> getAreaReportTask(UserContext userContext, Map<String, Object> paramsMap, int currPage,
			int pageSize, String sortField, String sortType) {
		DepartmentModel departmentModel = userContext.getDepartmentModel();
		String sql = ResourceUtils.getSQL("select_xqsbrw");
		// if (paramsMap.containsKey("JSDW_GAJGJGDM")) {
		// sql += " and JSDW_GAJGJGDM = '" +
		// StringUtils.nvlString(paramsMap.remove("JSDW_GAJGJGDM")) + "' ";
		// }
		// if (paramsMap.containsKey("JSRY_XM")) {
		// sql += " and JSRY_XM like '" +
		// StringUtils.nvlString(paramsMap.remove("JSRY_XM")) + "%' ";
		// }
		Map<String, String> replaceMap = Maps.newHashMap();
		// replaceMap.put("queryParams", SQLUtils.spellWhere(paramsMap));
		replaceMap.put("queryCode", " CODE_LEV" + departmentModel.getLayer() + "='" + departmentModel.getId() + "'");
		sql = StringUtils.replaceAllVariable(sql, replaceMap);
		sql = "select a.* from(" + sql + ") a where 1=1 ";
		if (paramsMap.containsKey("JSDW_GAJGJGDM")) {
			sql += " and JSDW_GAJGJGDM like '%" + StringUtils.nvlString(paramsMap.remove("JSDW_GAJGJGDM")) + "%' ";
		}
		if (paramsMap.containsKey("JSRY_XM")) {
			sql += " and JSRY_XM like '%" + StringUtils.nvlString(paramsMap.remove("JSRY_XM")) + "%' ";
		}
		sql += SQLUtils.spellWhere(paramsMap);
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql),
				new Object[] { userContext.getUID(), userContext.getUID() });
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortField)
				&& org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql += " order by " + sortField + " " + sortType;
		} else {
			sql += " order by SBSJ desc ";
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql, currPage, pageSize),
				new Object[] { userContext.getUID(), userContext.getUID() });

		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("SBLXDM", "CODE_XSHB_SBLX");
		columnConfigMap.put("RWDJ", "CODE_XSHB_JB");
		columnConfigMap.put("SFSMDM", "CODE_XSHB_SFSM");
		columnConfigMap.put("SBSJ", "DATETIME");
		columnConfigMap.put("RWZTDM", "CODE_XSHB_RWZT");
		columnConfigMap.put("JSDW_GAJGJGDM", "CODE_GXS");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 新增下发任务
	 * 
	 * @param userContext
	 * @param paramsMap
	 */
	public int[] addIssueTask(UserContext userContext, Map<String, Object> paramsMap, Connection connection) {
		int[] insert = new int[4];
		String rwbh = SequenceUtils.getSequenceVal("SEQ_BO_EU_XSHB_RW_XF", "R",
				userContext.getUserModel().getDepartmentId(), "yyyymm", 23);
		// 新增任务下发表
		Map<String, Object> xfParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_XF");
		BO xfBO = new BO();
		xfBO.setAll(xfParamsMap);
		xfBO.set("RWBH", rwbh);
		xfBO.set("RWZTDM", "01");
		xfBO.setAll(super.createUserMap4DB(userContext, new String[] { "XF" }));
		insert[0] = SDK.getBOAPI().createDataBO("BO_EU_XSHB_RW_XF", xfBO, userContext, connection);
		// 更新任务关联线索表
		if (paramsMap.containsKey("BO_EU_XSHB_RW_GLXS")) {
			Map<String, Object> glParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_GLXS");
			glParamsMap.put("RWBH", rwbh);
			String sql = SQLUtils.getCreateUpdateSQL(glParamsMap, "BO_EU_XSHB_RW_GLXS", new String[] { "ID" });
			insert[1] = DBSql.update(connection, sql);
		} else if (paramsMap.containsKey("BO_EU_XSHB_GLYW")) {
			Map<String, Object> glParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_GLYW");
			glParamsMap.put("YWBH", rwbh);
			String sql = SQLUtils.getCreateUpdateSQL(glParamsMap, "BO_EU_XSHB_GLYW", new String[] { "ID" });
			insert[1] = DBSql.update(connection, sql);
		} else {
			insert[1] = 1;
		}
		// 存取附件
		if (paramsMap.containsKey("BO_EU_XSHB_FJ")) {
			Map<String, Object> fjParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_FJ");
			String[] paths = StringUtils.nvlString(fjParamsMap.get("FJLJ")).split(",");
			Map<String, Object> fjParams = Maps.newHashMap();
			fjParams.put("GLYWBH", rwbh);
			if (fjParamsMap.containsKey("XXZJBH")) {
				fjParams.put("XXZJBH", StringUtils.nvlString(fjParamsMap.get("XXZJBH")));
			}
			int[] uploadAttachment = commonService.uploadAttachment(userContext, paths, fjParams, connection);
			if (!Arrays.asList(uploadAttachment).contains(0)) {
				insert[2] = 1;
			} else {
				insert[2] = 0;
			}
		} else {
			insert[2] = 1;
		}
		// 新增任务流转表
		List<Map<String, Object>> lzList = (List<Map<String, Object>>) paramsMap.get("BO_EU_XSHB_RW_LZ");
		for (int i = 0; i < lzList.size(); i++) {
			Map<String, Object> lzParamsMap = lzList.get(i);
			String jsdxlxdm = StringUtils.nvlString(lzParamsMap.get("JSDXLXDM"));
			String jsdm_gajgjgdm = StringUtils.nvlString(lzParamsMap.get("JSDW_GAJGJGDM"));
			String user = null;
			if (jsdxlxdm.equals("02")) {
				List<RowMap> userList = DBSql.getMaps(ResourceUtils.getSQL("select_user"),
						new Object[] { jsdm_gajgjgdm });
				List<String> list = Lists.newArrayList();
				for (RowMap rowMap : userList) {
					String userid = rowMap.getString("USERID");
					list.add(userid);
				}
				user = org.apache.commons.lang.StringUtils.join(list, " ");
				// 权限用户
				// user = TaskService.getRoleUsers(userContext, jsdm_gajgjgdm, "线索联络员");
			} else if (jsdxlxdm.equals("01")) {
				user = StringUtils.nvlString(lzParamsMap.get("JSRY_GMSFHM"));
			}
			BO lzBO = new BO();
			lzBO.setAll(lzParamsMap);
			lzBO.set("RWBH", rwbh);
			lzBO.set("RWLXDM", "02");
			lzBO.set("LZZTDM", "01");
			lzBO.set("RWLZBH", SequenceUtils.getSequenceVal("SEQ_BO_EU_XSHB_RW_LZ", "",
					userContext.getUserModel().getDepartmentId(), "yyyymmdd", 30));

			if ("02".equals((StringUtils.nvlString(lzParamsMap.get("JSDXLXDM"))))) {
				lzBO.set("WDRY", user);
				// lzBO.set("WDRY", commonService.getDepartmentUser(userContext,
				// StringUtils.nvlString(lzParamsMap.get("JSDW_GAJGJGDM"))));
			} else {
				lzBO.set("WDRY", StringUtils.nvlString(lzParamsMap.get("JSRY_GMSFHM")));
			}
			lzBO.setAll(super.createUserMap4DB(userContext, new String[] { "XXDJ", "XXCZ" }));
			Map<String, Object> startProcess = processService.startProcess(userContext, "01", user);
			ProcessInstance processInstance = (ProcessInstance) startProcess.get("ProcessInstance");
			int create = SDK.getBOAPI().create("BO_EU_XSHB_RW_LZ", lzBO, processInstance, userContext, connection);
			if (create < 0) {
				SDK.getProcessAPI().delete(processInstance, userContext);
			}
		}

		insert[3] = 1;
		return insert;
	}

	/**
	 * 下发任务
	 * 
	 * @param userContext
	 * @return
	 */
	public RowMap getIssueTask(UserContext userContext, Map<String, Object> paramsMap, boolean needTransaction) {
		String sql = ResourceUtils.getSQL("select_xfrw") + SQLUtils.spellWhere(paramsMap);
		RowMap row = DBSql.getMap(sql);
		if (needTransaction) {
			Map<String, String> columnConfigMap = Maps.newHashMap();
			columnConfigMap.put("XFLXDM", "CODE_XSHB_XFLX");
			columnConfigMap.put("XFSJ", "DATETIME");
			columnConfigMap.put("XFLXDM", "CODE_XSHB_XFLX");
			columnConfigMap.put("JJCDDM", "CODE_XSHB_JB");
			columnConfigMap.put("SFSMDM", "CODE_XSHB_SFSM");
			columnConfigMap.put("RWZTDM", "CODE_XSHB_RWZT");
			columnConfigMap.put("FKQX", "datetime");
			CodeService.resultOfTransaction(columnConfigMap, row);
		}
		return row;
	}

	/**
	 * 我的下发任务
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @param currPage
	 * @param pageSize
	 * @param sortField
	 * @param sortType
	 * @return
	 */
	public Map<String, Object> getMyIssueTask(UserContext userContext, Map<String, Object> paramsMap, int currPage,
			int pageSize, String sortField, String sortType) {
		paramsMap.put("XFRY_GMSFHM", userContext.getUID());
		String sql = ResourceUtils.getSQL("select_xfrw");
		sql = "select a.* from(" + sql + ") a where 1=1 ";
		if (paramsMap.containsKey("JSDW_GAJGJGDM")) {
			sql += " and JSDW_GAJGJGDM like '%" + StringUtils.nvlString(paramsMap.remove("JSDW_GAJGJGDM")) + "%' ";
		}
		if (paramsMap.containsKey("JSRY_XM")) {
			sql += " and JSRY_XM like '%" + StringUtils.nvlString(paramsMap.remove("JSRY_XM")) + "%' ";
		}
		sql += SQLUtils.spellWhere(paramsMap);
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql), new Object[] {});
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortField)
				&& org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql += " order by " + sortField + " " + sortType;
		} else {
			sql += " order by XFSJ desc ";
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql, currPage, pageSize));
		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("XFLXDM", "CODE_XSHB_XFLX");
		columnConfigMap.put("RWDJ", "CODE_XSHB_JB");
		columnConfigMap.put("SFSMDM", "CODE_XSHB_SFSM");
		columnConfigMap.put("XFSJ", "DATETIME");
		columnConfigMap.put("RWZTDM", "CODE_XSHB_RWZT");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 上级下发任务
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @param currPage
	 * @param pageSize
	 * @param sortField
	 * @param sortType
	 * @return
	 */
	public Map<String, Object> getSupIssueTask(UserContext userContext, Map<String, Object> paramsMap, int currPage,
			int pageSize, String sortField, String sortType) {
		String sql = ResourceUtils.getSQL("select_sjxfrw");
		// 过滤是否涉密
		sql += " and (a.SFSMDM='01' or(a.SFSMDM='02' and ((a.JSDW_GAJGJGDM = '"
				+ userContext.getDepartmentModel().getId() + "' or a.JSRY_GMSFHM = '" + userContext.getUID() + "'))))";
		if (paramsMap.containsKey("JSDW_GAJGJGDM")) {
			sql += " and JSDW_GAJGJGDM = '" + StringUtils.nvlString(paramsMap.remove("JSDW_GAJGJGDM")) + "' ";
		}
		if (paramsMap.containsKey("JSRY_XM")) {
			sql += " and JSRY_XM like '%" + StringUtils.nvlString(paramsMap.remove("JSRY_XM")) + "%' ";
		}
		sql += SQLUtils.spellWhere(paramsMap);
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql),
				new Object[] { userContext.getUID(), userContext.getDepartmentModel().getId(), userContext.getUID(),
						userContext.getDepartmentModel().getId() });
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortField)
				&& org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql += " order by " + sortField + " " + sortType;
		} else {
			sql += " order by XFSJ desc ";
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql, currPage, pageSize),
				new Object[] { userContext.getUID(), userContext.getDepartmentModel().getId(), userContext.getUID(),
						userContext.getDepartmentModel().getId() });

		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("XFLXDM", "CODE_XSHB_XFLX");
		columnConfigMap.put("RWDJ", "CODE_XSHB_JB");
		columnConfigMap.put("SFSMDM", "CODE_XSHB_SFSM");
		columnConfigMap.put("XFSJ", "DATETIME");
		columnConfigMap.put("RWZTDM", "CODE_XSHB_RWZT");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 单位下发任务
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @param currPage
	 * @param pageSize
	 * @param sortField
	 * @param sortType
	 * @return
	 */
	public Map<String, Object> getUnitIssueTask(UserContext userContext, Map<String, Object> paramsMap, int currPage,
			int pageSize, String sortField, String sortType) {
		// paramsMap.put("SFSMDM", "01");
		paramsMap.put("XFDW_GAJGJGDM", userContext.getDepartmentModel().getId());
		String sql = ResourceUtils.getSQL("select_xfrw");
		sql = "select a.* from(" + sql + ") a";
		// 过滤是否涉密
		sql += " where (a.SFSMDM='01' or(a.SFSMDM='02' and ((a.XFDW_GAJGJGDM LIKE '%"
				+ userContext.getDepartmentModel().getId() + "%' and a.JSDXLXDM like '%02%') or '"
				+ userContext.getUID() + "' in (select JSRY_GMSFHM from BO_EU_XSHB_RW_LZ lz where lz.rwbh=a.rwbh)))) ";
		// sql = "select a.* from(" + sql + ") a where 1=1 and a.XFDW_GAJGJGDM ='" +
		// userContext.getDepartmentModel().getId() + "' ";
		if (paramsMap.containsKey("JSDW_GAJGJGDM")) {
			sql += " and JSDW_GAJGJGDM like '%" + StringUtils.nvlString(paramsMap.remove("JSDW_GAJGJGDM")) + "%' ";
		}
		if (paramsMap.containsKey("JSRY_XM")) {
			sql += " and JSRY_XM like '%" + StringUtils.nvlString(paramsMap.remove("JSRY_XM")) + "%' ";
		}
		sql += SQLUtils.spellWhere(paramsMap);
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql), new Object[] {});
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortField)
				&& org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql += " order by " + sortField + " " + sortType;
		} else {
			sql += " order by XFSJ desc ";
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql, currPage, pageSize), new Object[] {});

		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("XFLXDM", "CODE_XSHB_XFLX");
		columnConfigMap.put("RWDJ", "CODE_XSHB_JB");
		columnConfigMap.put("SFSMDM", "CODE_XSHB_SFSM");
		columnConfigMap.put("XFSJ", "DATETIME");
		columnConfigMap.put("RWZTDM", "CODE_XSHB_RWZT");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 辖区下发任务
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @param currPage
	 * @param pageSize
	 * @param sortField
	 * @param sortType
	 * @return
	 */
	public Map<String, Object> getAreaIssueTask(UserContext userContext, Map<String, Object> paramsMap, int currPage,
			int pageSize, String sortField, String sortType) {
		DepartmentModel departmentModel = userContext.getDepartmentModel();
		String sql = ResourceUtils.getSQL("select_xqxfrw");
		// if (paramsMap.containsKey("JSDW_GAJGJGDM")) {
		// sql += " and JSDW_GAJGJGDM = '" +
		// StringUtils.nvlString(paramsMap.remove("JSDW_GAJGJGDM")) + "' ";
		// }
		// if (paramsMap.containsKey("JSRY_XM")) {
		// sql += " and JSRY_XM like '" +
		// StringUtils.nvlString(paramsMap.remove("JSRY_XM")) + "%' ";
		// }

		Map<String, String> replaceMap = Maps.newHashMap();
		// replaceMap.put("queryParams", SQLUtils.spellWhere(paramsMap));
		replaceMap.put("queryCode", " CODE_LEV" + departmentModel.getLayer() + "='" + departmentModel.getId() + "'");
		sql = StringUtils.replaceAllVariable(sql, replaceMap);
		sql = "select a.* from(" + sql + ") a where 1=1 and a.sfsmdm='01' ";
		if (paramsMap.containsKey("JSDW_GAJGJGDM")) {
			sql += " and JSDW_GAJGJGDM like '%" + StringUtils.nvlString(paramsMap.remove("JSDW_GAJGJGDM")) + "%' ";
		}
		if (paramsMap.containsKey("JSRY_XM")) {
			sql += " and JSRY_XM like '%" + StringUtils.nvlString(paramsMap.remove("JSRY_XM")) + "%' ";
		}
		sql += SQLUtils.spellWhere(paramsMap);
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql),
				new Object[] { userContext.getUID(), userContext.getUID() });
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortField)
				&& org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql += " order by " + sortField + " " + sortType;
		} else {
			sql += " order by XFSJ desc ";
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql, currPage, pageSize),
				new Object[] { userContext.getUID(), userContext.getUID() });

		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("XFLXDM", "CODE_XSHB_XFLX");
		columnConfigMap.put("RWZTDM", "CODE_XSHB_RWZT");
		columnConfigMap.put("SFSMDM", "CODE_XSHB_SFSM");
		columnConfigMap.put("RWDJ", "CODE_XSHB_JB");
		columnConfigMap.put("XFSJ", "DATETIME");
		columnConfigMap.put("RWZTDM", "CODE_XSHB_RWZT");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 更新下发任务
	 */
	@Deprecated
	public int[] updateIssueTask(UserContext userContext, Map<String, Object> paramsMap) {
		int[] update = { 0, 0, 0 };
		Map<String, Object> xfParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_XF");
		xfParamsMap.put("GXSJ", "SYSDATE");
		String rwbh = StringUtils.nvlString(xfParamsMap.get("RWBH"));
		update[0] = DBSql.update(SQLUtils.getCreateUpdateSQL(xfParamsMap, "BO_EU_XSHB_RW_XF", new String[] { "ID" }));
		List<Map<String, Object>> lzList = (List<Map<String, Object>>) paramsMap.get("BO_EU_XSHB_RW_LZ");
		String jsdxlxdm = "";
		String sql = "";
		List<String> jsList = Lists.newArrayList();
		if (lzList.size() > 0) {
			jsdxlxdm = StringUtils.nvlString(lzList.get(0).get("JSDXLXDM"));
		}
		if ("01".equals(jsdxlxdm)) {
			for (Map<String, Object> map : lzList) {
				Map<String, Object> params = Maps.newHashMap();
				params.put("JSRY_GMSFHM", StringUtils.nvlString(map.get("JSRY_GMSFHM")));
				params.put("RWBH", rwbh);
				Map m = DBSql.getMap(ResourceUtils.getSQL("select_rw_lz") + SQLUtils.spellWhere(params));
				if (m == null) {
					BO lzBO = new BO();
					lzBO.setAll(map);
					lzBO.set("RWBH", rwbh);
					lzBO.setAll(super.createUserMap4DB(userContext, new String[] { "XXDJ", "XXCZ" }));
					SDK.getBOAPI().createDataBO("BO_EU_XSHB_RW_LZ", lzBO, userContext);
				}
				jsList.add(StringUtils.nvlString(map.get("JSRY_GMSFHM")));
			}
			Map<String, Object> param = Maps.newHashMap();
			param.put("JSRY_GMSFHM~10", org.apache.commons.lang.StringUtils.join(jsList.toArray()));
			param.put("RWBH", rwbh);
			param.put("XXSC_PDBZ", "1");
			sql = SQLUtils.getCreateUpdateSQL(param, "BO_EU_XSHB_RW_LZ", new String[] { "JSRY_GMSFHM", "RWBH" });
		} else {
			for (Map<String, Object> map : lzList) {
				Map<String, Object> params = Maps.newHashMap();
				params.put("JSDW_GAJGJGDM", StringUtils.nvlString(map.get("JSDW_GAJGJGDM")));
				params.put("RWBH", rwbh);
				RowMap m = DBSql.getMap(ResourceUtils.getSQL("select_rw_lz") + SQLUtils.spellWhere(params));
				if (m == null) {
					BO lzBO = new BO();
					lzBO.setAll(map);
					lzBO.set("RWBH", rwbh);
					lzBO.setAll(super.createUserMap4DB(userContext, new String[] { "XXDJ", "XXCZ" }));
					SDK.getBOAPI().createDataBO("BO_EU_XSHB_RW_LZ", lzBO, userContext);
				}
				jsList.add(StringUtils.nvlString(map.get("JSRY_GMSFHM")));
			}
			Map<String, Object> param = Maps.newHashMap();
			param.put("JSDW_GAJGJGDM~10", org.apache.commons.lang.StringUtils.join(jsList.toArray()));
			param.put("RWBH", rwbh);
			param.put("XXSC_PDBZ", "1");
			sql = SQLUtils.getCreateUpdateSQL(param, "BO_EU_XSHB_RW_LZ", new String[] { "JSDW_GAJGJGDM", "RWBH" });
		}
		update[1] = DBSql.update(sql);
		// 附件更新,先删在增
		Map<String, Object> fjParams = Maps.newHashMap();
		fjParams.put("GLYWBH", rwbh);
		DBSql.update(SQLUtils.getCreateDeleteSQL(fjParams, "BO_EU_XSHB_FJ"));
		Map<String, Object> fjParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_FJ");
		if (fjParamsMap != null && !fjParamsMap.isEmpty()) {
			String[] paths = StringUtils.nvlString(fjParamsMap.get("FJLJ")).split(",");
			// int[] i = commonService.uploadAttachment(userContext, paths, fjParams);
			// if (!Arrays.asList(i).contains("0")) {
			// update[2] = 1;
			// }
		}
		return update;
	}

	/**
	 * 删除下发任务
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	@Deprecated
	public int[] deleteIssueTask(UserContext userContext, Map<String, Object> paramsMap) {
		Map<String, Object> xfParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_XF");
		xfParamsMap.put("XXSC_PDBZ", "1");
		String rwbh = StringUtils.nvlString(xfParamsMap.get("RWBH"));
		Map<String, Object> fjParams = Maps.newHashMap();
		fjParams.put("GLYWBH", rwbh);
		int insert[] = new int[3];
		insert[0] = DBSql.update(SQLUtils.getCreateUpdateSQL(xfParamsMap, "BO_EU_XSHB_RW_XF", new String[] { "RWBH" }));
		insert[1] = DBSql.update(SQLUtils.getCreateUpdateSQL(xfParamsMap, "BO_EU_XSHB_RW_LZ", new String[] { "RWBH" }));
		insert[2] = DBSql.update(SQLUtils.getCreateDeleteSQL(fjParams, "BO_EU_XSHB_FJ"));
		return insert;
	}

	/**
	 * 新增上报任务
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	public int[] addReportTask(UserContext userContext, Map<String, Object> paramsMap, Connection connection) {
		int[] insert = new int[4];
		Map<String, Object> sbxxParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_SB");
		String rwbh = SequenceUtils.getSequenceVal("SEQ_BO_EU_XSHB_RW_SB", "R",
				userContext.getUserModel().getDepartmentId(), "yyyymm", 23);
		BO reportBO = new BO();
		reportBO.setAll(sbxxParamsMap);
		reportBO.set("RWBH", rwbh);
		reportBO.set("RWZTDM", "01");
		reportBO.setAll(super.createUserMap4DB(userContext, new String[] { "SB" }));
		insert[0] = SDK.getBOAPI().createDataBO("BO_EU_XSHB_RW_SB", reportBO, userContext, connection);
		// 更新任务关联线索表
		if (paramsMap.containsKey("BO_EU_XSHB_RW_GLXS")) {
			Map<String, Object> glParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_GLXS");
			System.out.println(glParamsMap.get("ID"));
			glParamsMap.put("RWBH", rwbh);
			String sql = SQLUtils.getCreateUpdateSQL(glParamsMap, "BO_EU_XSHB_RW_GLXS", new String[] { "ID" });
			insert[1] = DBSql.update(connection, sql);
		} else if (paramsMap.containsKey("BO_EU_XSHB_GLYW")) {
			Map<String, Object> glParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_GLYW");
			glParamsMap.put("YWBH", rwbh);
			String sql = SQLUtils.getCreateUpdateSQL(glParamsMap, "BO_EU_XSHB_GLYW", new String[] { "ID" });
			insert[1] = DBSql.update(connection, sql);
		} else {
			insert[1] = 1;
		}
		// 存取附件
		Map<String, Object> fjParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_FJ");
		if (paramsMap.containsKey("BO_EU_XSHB_FJ")) {
			String[] paths = StringUtils.nvlString(fjParamsMap.get("FJLJ")).split(",");
			Map<String, Object> fjParams = Maps.newHashMap();
			fjParams.put("GLYWBH", rwbh);
			if (fjParamsMap.containsKey("XXZJBH")) {
				fjParams.put("XXZJBH", StringUtils.nvlString(fjParamsMap.get("XXZJBH")));
			}
			int[] uploadAttachment = commonService.uploadAttachment(userContext, paths, fjParams, connection);
			if (!Arrays.asList(uploadAttachment).contains(0)) {
				insert[2] = 1;
			} else {
				insert[2] = 0;
			}
		} else {
			insert[2] = 1;
		}

		// 新增任务流转表
		List<Map<String, Object>> lzList = (List<Map<String, Object>>) paramsMap.get("BO_EU_XSHB_RW_LZ");
		for (int i = 0; i < lzList.size(); i++) {
			Map<String, Object> lzParamsMap = lzList.get(i);
			if (!lzParamsMap.containsKey("JSDXLXDM")) {
				lzParamsMap.put("JSDXLXDM", StringUtils.nvlString(lzParamsMap.remove("JSLX")));
			}
			String jsdxlxdm = StringUtils.nvlString(lzParamsMap.get("JSDXLXDM"));
			String user = null;
			BO lzBO = new BO();
			lzBO.setAll(lzParamsMap);
			lzBO.set("RWBH", rwbh);
			lzBO.set("RWLXDM", "01");
			lzBO.set("LZZTDM", "01");
			lzBO.set("RWLZBH", SequenceUtils.getSequenceVal("SEQ_BO_EU_XSHB_RW_LZ", "",
					userContext.getUserModel().getDepartmentId(), "yyyymmdd", 30));
			if (jsdxlxdm.equals("02")) {
				String parentDepartmentId = userContext.getDepartmentModel().getParentDepartmentId();
				Map<String, Object> map = Maps.newHashMap();
				map.put("CODE", parentDepartmentId);
				RowMap r = DBSql.getMap(ResourceUtils.getSQL("select_dwxx") + SQLUtils.spellWhere(map));
				lzBO.set("JSDW_GAJGMC", r.getString("NAME"));
				lzBO.set("JSDW_GAJGJGDM", parentDepartmentId);
				List<RowMap> userList = commonService.getDepartmentUser(userContext, parentDepartmentId);
				List<String> list = Lists.newArrayList();
				for (RowMap rowMap : userList) {
					String userid = rowMap.getString("USERID");
					list.add(userid);
				}
				user = org.apache.commons.lang.StringUtils.join(list, " ");
				// user = TaskService.getRoleUsers(userContext, parentDepartmentId, "线索联络员");
			} else if (jsdxlxdm.equals("01")) {
				user = StringUtils.nvlString(lzParamsMap.get("JSRY_GMSFHM"));
			}
			if ("02".equals((StringUtils.nvlString(lzParamsMap.get("JSDXLXDM"))))) {
				lzBO.set("WDRY", commonService.getDepartmentUser(userContext,
						StringUtils.nvlString(lzParamsMap.get("JSDW_GAJGJGDM"))));
			} else {
				lzBO.set("WDRY", StringUtils.nvlString(lzParamsMap.get("JSRY_GMSFHM")));
			}
			lzBO.setAll(super.createUserMap4DB(userContext, new String[] { "XXDJ", "XXCZ" }));
			Map<String, Object> startProcess = processService.startProcess(userContext, "02", user);
			ProcessInstance processInstance = (ProcessInstance) startProcess.get("ProcessInstance");
			int create = SDK.getBOAPI().create("BO_EU_XSHB_RW_LZ", lzBO, processInstance, userContext, connection);
			if (create < 0) {
				SDK.getProcessAPI().delete(processInstance, userContext);
			}

		}
		insert[3] = 1;

		return insert;
	}

	/**
	 * 更新上报任务
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	@Deprecated
	public int updateReportTask(UserContext userContext, Map<String, Object> paramsMap) {
		Map<String, Object> sbParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_SB");
		sbParamsMap.put("GXSJ", "SYSDATE");
		// 附件更新
		String rwbh = StringUtils.nvlString(sbParamsMap.get("RWBH"));
		Map<String, Object> fjParams = Maps.newHashMap();
		fjParams.put("GLYWBH", rwbh);
		DBSql.update(SQLUtils.getCreateDeleteSQL(fjParams, "BO_EU_XSHB_FJ"));
		Map<String, Object> fjParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_FJ");
		if (fjParamsMap != null && !fjParamsMap.isEmpty()) {
			String[] paths = StringUtils.nvlString(fjParamsMap.get("FJLJ")).split(",");
			// commonService.uploadAttachment(userContext, paths, fjParams);
		}
		// 上报更新
		sbParamsMap.putAll(super.createUserMap4DB(userContext, new String[] { "XXCZ" }));
		sbParamsMap.put("GXSJ", "SYSDATE");
		String sql = SQLUtils.getCreateUpdateSQL(sbParamsMap, "BO_EU_XSHB_RW_SB", new String[] { "ID" });
		return DBSql.update(sql);
	}

	/**
	 * 删除上报任务
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	@Deprecated
	public int deleteReportTask(UserContext userContext, Map<String, Object> paramsMap) {
		Map<String, Object> sbParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_SB");
		sbParamsMap.put("XXSC_PDBZ", "1");
		String sql = SQLUtils.getCreateUpdateSQL(sbParamsMap, "BO_EU_XSHB_RW_SB", new String[] { "ID" });
		return DBSql.update(sql);
	}

	/**
	 * 添加任务关联线索
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	public int[] addTaskRelevanceClue(UserContext userContext, Map<String, Object> paramsMap,
			Map<String, Object> returnMap) {
		List<Map<String, Object>> glxsParamsList = (List<Map<String, Object>>) paramsMap.get("BO_EU_XSHB_RW_GLXS");
		List<BO> recordDatas = Lists.newArrayList();
		for (Map<String, Object> map : glxsParamsList) {
			BO glxsBO = new BO();
			// glxsBO.set("ID", SequenceUtils.getSequenceVal("SEQ_BO_EU_XSHB_XS", "",
			// userContext.getUserModel().getDepartmentId(), "yyyymmdd", 30));
			glxsBO.setAll(map);
			glxsBO.setAll(super.createUserMap4DB(userContext, new String[] { "XXDJ" }));
			recordDatas.add(glxsBO);
		}
		List<String> idList = Lists.newArrayList();
		int[] insert = SDK.getBOAPI().createDataBO("BO_EU_XSHB_RW_GLXS", recordDatas, userContext);
		for (BO recordData : recordDatas) {
			idList.add(recordData.getId());
		}
		returnMap.put("ID", idList);
		return insert;
	}

	/**
	 * 删除任务关联线索
	 */
	public int deleteTaskRelevanceClue(UserContext userContext, Map<String, Object> paramsMap) {
		Map<String, Object> glxsParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_GLXS");
		glxsParamsMap.put("XXSC_PDBZ", "1");
		String sql = SQLUtils.getCreateUpdateSQL(glxsParamsMap, "BO_EU_XSHB_RW_GLXS", new String[] { "XSBH" });
		return DBSql.update(sql);
	}

	/**
	 * 作废（ 取消流程&删除线索数据）----暂定
	 */
	public int cancelClue(UserContext userContext, Map<String, Object> paramsMap) {
		SDK.getProcessAPI().cancelById((String) paramsMap.get("processInstId"), userContext.getUID());
		return DBSql.update(SQLUtils.getCreateDeleteSQL(paramsMap, "BO_EU_XSHB"));
	}

	/**
	 * 签收
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	public int[] sign(UserContext userContext, Map<String, Object> paramsMap) {
		Map<String, Object> lzParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_LZ");
		BO bo = SDK.getBOAPI().get("BO_EU_XSHB_RW_LZ", StringUtils.nvlString(lzParamsMap.get("ID")));
		int[] update = { 0, 0 };
		if ("1".equals(bo.getString("SFQS_PDBZ"))) {
			return update;
		}
		// 判断是否最终流转
		if (org.apache.commons.lang.StringUtils.isNotBlank(bo.getString("PID"))) {
			lzParamsMap.put("SFZZLZ_PDBZ", "1");
			update[0] = DBSql.update(ResourceUtils.getSQL("update_sign"), new String[] {
					StringUtils.nvlString(lzParamsMap.get("RWBH")), StringUtils.nvlString(lzParamsMap.get("ID")) });
		} else {
			update[0] = 1;
		}
		lzParamsMap.putAll(super.createUserMap4DB(userContext, new String[] { "QS", "XXCZ" }));
		lzParamsMap.put("SFQS_PDBZ", "1");
		// lzParamsMap.put("GXSJ", "SYSDATE");
		update[1] = DBSql.update(SQLUtils.getCreateUpdateSQL(lzParamsMap, "BO_EU_XSHB_RW_LZ", new String[] { "ID" }));

		if (org.apache.commons.lang.StringUtils.isNotBlank(bo.getString("PID"))) {// 转派数据
			// 转派接收人状态
			commonService.updateTaskProcessStatus(userContext, paramsMap, "09");
			commonService.updateTaskStatus(userContext, paramsMap, "09");
			commonService.addProcessRecord(userContext, paramsMap, "01");

			Map<String, Object> _paramsMap = Maps.newHashMap();
			_paramsMap.putAll(paramsMap);
			_paramsMap.put("ID", bo.getString("PID"));
			commonService.updateTaskProcessStatus(userContext, _paramsMap, "09");
		} else {
			if ("02".equals(bo.getString("RWLXDM"))) {
				String sffk = DBSql.getString("select sffk_pdbz from bo_eu_xshb_rw_xf where rwbh=? ",
						new String[] { bo.getString("RWBH") });
				if ("0".equals(sffk)) {
					commonService.updateTaskProcessStatus(userContext, paramsMap, "10");
					commonService.updateTaskStatus(userContext, paramsMap, "10");
					commonService.addProcessRecord(userContext, paramsMap, "01");
				} else {
					commonService.updateTaskProcessStatus(userContext, paramsMap, "05");
					commonService.updateTaskStatus(userContext, paramsMap, "05");
					commonService.addProcessRecord(userContext, paramsMap, "01");
				}
			}

		}
		return update;
	}

	/**
	 * 新增反馈信息
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	public int[] feedback(UserContext userContext, Map<String, Object> paramsMap, Connection connection) {
		int[] insert = new int[3];
		String fkbh = SequenceUtils.getSequenceVal("SEQ_BO_EU_XSHB_RW_FK", "",
				userContext.getUserModel().getDepartmentId(), "yyyymmdd", 30);
		Map<String, Object> fkParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_FK");
		fkParamsMap.put("FKBH", fkbh);
		fkParamsMap.putAll(super.createUserMap4DB(userContext, new String[] { "FK" }));
		insert[0] = SDK.getBOAPI().createDataBO("BO_EU_XSHB_RW_FK", new BO().setAll(fkParamsMap), userContext);
		// 关联业务
		if (paramsMap.containsKey("BO_EU_XSHB_GLYW")) {
			Map<String, Object> ywParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_GLYW");
			ywParamsMap.put("YWBH", fkbh);
			insert[1] = DBSql
					.update(SQLUtils.getCreateUpdateSQL(ywParamsMap, "BO_EU_XSHB_GLYW", new String[] { "ID" }));
		} else {
			insert[1] = 1;
		}
		// 存取附件
		if (paramsMap.containsKey("BO_EU_XSHB_FJ")) {
			Map<String, Object> fjParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_FJ");
			String[] paths = StringUtils.nvlString(fjParamsMap.remove("FJLJ")).split(",");
			fjParamsMap.put("GLYWBH", fkbh);
			int[] uploadAttachment = commonService.uploadAttachment(userContext, paths, fjParamsMap, connection);
			if (!Arrays.asList(uploadAttachment).contains(0)) {
				insert[2] = 1;
			} else {
				insert[2] = 0;
			}
		} else {
			insert[2] = 1;
		}
		commonService.updateTaskProcessStatus(userContext, paramsMap, "06");
		commonService.updateTaskStatus(userContext, paramsMap, "06");
		commonService.addProcessRecord(userContext, paramsMap, "03");
		return insert;
	}

	/**
	 * 新增批复信息
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	public int[] reply(UserContext userContext, Map<String, Object> paramsMap, Connection connection) {
		int[] insert = new int[3];
		String pfbh = SequenceUtils.getSequenceVal("SEQ_BO_EU_XSHB_RW_PF", "",
				userContext.getUserModel().getDepartmentId(), "yyyymmdd", 30);
		Map<String, Object> pfParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_PF");
		pfParamsMap.put("PFBH", pfbh);
		pfParamsMap.putAll(super.createUserMap4DB(userContext, new String[] { "PF" }));

		insert[0] = SDK.getBOAPI().createDataBO("BO_EU_XSHB_RW_PF", new BO().setAll(pfParamsMap), userContext);
		// 关联业务
		if (paramsMap.containsKey("BO_EU_XSHB_GLYW")) {
			Map<String, Object> ywParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_GLYW");
			ywParamsMap.put("YWBH", pfbh);
			insert[1] = DBSql
					.update(SQLUtils.getCreateUpdateSQL(ywParamsMap, "BO_EU_XSHB_GLYW", new String[] { "ID" }));
		} else {
			insert[1] = 1;
		}
		// 存取附件
		if (paramsMap.containsKey("BO_EU_XSHB_FJ")) {
			Map<String, Object> fjParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_FJ");
			String[] paths = StringUtils.nvlString(fjParamsMap.remove("FJLJ")).split(",");
			fjParamsMap.put("GLYWBH", pfbh);
			int[] uploadAttachment = commonService.uploadAttachment(userContext, paths, fjParamsMap, connection);
			if (!Arrays.asList(uploadAttachment).contains(0)) {
				insert[2] = 1;
			} else {
				insert[2] = 0;
			}
		} else {
			insert[2] = 1;
		}

		commonService.updateTaskProcessStatus(userContext, paramsMap, "06");
		commonService.updateTaskStatus(userContext, paramsMap, "06");
		commonService.addProcessRecord(userContext, paramsMap, "04");
		return insert;
	}

	/**
	 * 回退（流程回退）
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	public int back(UserContext userContext, Map<String, Object> paramsMap) {
		Map<String, Object> lzParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_LZ");
		lzParamsMap.putAll(super.createUserMap4DB(userContext, new String[] { "XXCZ", "HT" }));
		lzParamsMap.put("HTSJ", "SYSDATE");
		lzParamsMap.put("GXSJ", "SYSDATE");
		lzParamsMap.put("SFHT_PDBZ", "1");
		int update = DBSql.update(SQLUtils.getCreateUpdateSQL(lzParamsMap, "BO_EU_XSHB_RW_LZ", new String[] { "ID" }));
		// 转派回退
		String pid = SDK.getBOAPI().get("BO_EU_XSHB_RW_LZ", StringUtils.nvlString(lzParamsMap.get("ID")))
				.getString("PID");
		if (org.apache.commons.lang.StringUtils.isNotBlank(pid)) {
			commonService.updateTaskProcessStatus(userContext, paramsMap, "08");
			commonService.updateTaskStatus(userContext, paramsMap, "08");
			commonService.addProcessRecord(userContext, paramsMap, "02");

			Map<String, Object> _paramsMap = Maps.newHashMap();
			_paramsMap.putAll(paramsMap);
			_paramsMap.put("ID", pid);
			commonService.updateTaskProcessStatus(userContext, _paramsMap, "08");
		} else {
			commonService.updateTaskProcessStatus(userContext, paramsMap, "02");
			commonService.updateTaskStatus(userContext, paramsMap, "02");
			commonService.addProcessRecord(userContext, paramsMap, "02");
		}
		return update;
	}

	/**
	 * 回退审批
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	public int backApprove(UserContext userContext, Map<String, Object> paramsMap) {
		Map<String, Object> lzParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_LZ");
		lzParamsMap.putAll(super.createUserMap4DB(userContext, new String[] { "XXCZ", "HTSP" }));
		lzParamsMap.put("HTSPSJ", "SYSDATE");
		lzParamsMap.put("GXSJ", "SYSDATE");
		int update;
		if ("1".equals(StringUtils.nvlString(lzParamsMap.get("HTSPSFTG_PDBZ")))) {
			// 强制签收
			BO lzBo = SDK.getBOAPI().get("BO_EU_XSHB_RW_LZ", StringUtils.nvlString(lzParamsMap.get("ID")));
			lzParamsMap.put("QSRY_XM", lzBo.get("HTRY_XM"));
			lzParamsMap.put("QSRY_GMSFHM", lzBo.get("HTRY_GMSFHM"));
			lzParamsMap.put("QSRY_LXDH", lzBo.get("HTRY_LXDH"));
			lzParamsMap.put("QSDW_GAJGMC", lzBo.get("HTDW_GAJGMC"));
			lzParamsMap.put("QSDW_GAJGJGDM", lzBo.get("HTDW_GAJGJGDM"));
			lzParamsMap.put("SFQS_PDBZ", "1");
			update = DBSql.update(SQLUtils.getCreateUpdateSQL(lzParamsMap, "BO_EU_XSHB_RW_LZ", new String[] { "ID" }));
			commonService.updateTaskProcessStatus(userContext, paramsMap, "04");
			commonService.updateTaskStatus(userContext, paramsMap, "04");
			commonService.addProcessRecord(userContext, paramsMap, "05");
		} else {
			commonService.updateTaskProcessStatus(userContext, paramsMap, "03");
			update = DBSql.update(SQLUtils.getCreateUpdateSQL(lzParamsMap, "BO_EU_XSHB_RW_LZ", new String[] { "ID" }));
			commonService.updateTaskStatus(userContext, paramsMap, "03");
			commonService.addProcessRecord(userContext, paramsMap, "06");
		}
		return update;
	}

	/**
	 * 上报驳回
	 */
	public int overrule(UserContext userContext, Map<String, Object> paramsMap) {
		Map<String, Object> lzParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_LZ");
		lzParamsMap.putAll(super.createUserMap4DB(userContext, new String[] { "XXCZ", "HTSP" }));
		lzParamsMap.put("HTSPSJ", "SYSDATE");
		lzParamsMap.put("GXSJ", "SYSDATE");
		int update = DBSql.update(SQLUtils.getCreateUpdateSQL(lzParamsMap, "BO_EU_XSHB_RW_LZ", new String[] { "ID" }));
		commonService.updateTaskProcessStatus(userContext, paramsMap, "03");
		commonService.updateTaskStatus(userContext, paramsMap, "03");
		commonService.addProcessRecord(userContext, paramsMap, "06");
		return update;
	}

	/**
	 * 任务评分 （反馈审批通过）
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @param processInstId
	 * @param taskInstId
	 * @return
	 */
	public int assess(UserContext userContext, Map<String, Object> paramsMap, String processInstId, String taskInstId) {
		Map<String, Object> lzParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_LZ");
		lzParamsMap.putAll(super.createUserMap4DB(userContext, new String[] { "PF", "XXCZ" }));
		lzParamsMap.put("GXSJ", "SYSDATE");
		int update = DBSql.update(SQLUtils.getCreateUpdateSQL(lzParamsMap, "BO_EU_XSHB_RW_LZ", new String[] { "ID" }));
		commonService.updateTaskProcessStatus(userContext, paramsMap, "10");
		commonService.updateTaskStatus(userContext, paramsMap, "10");
		commonService.addProcessRecord(userContext, paramsMap, "07");
		return update;
	}

	/**
	 * 续报任务审批通过
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @param processInstId
	 * @param taskInstId
	 * @return
	 */
	public int pass(UserContext userContext, Map<String, Object> paramsMap, String processInstId, String taskInstId) {
		Map<String, Object> lzParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_LZ");
		String xsbh = StringUtils.nvlString(lzParamsMap.remove("XSBH"));
		String xbbh = StringUtils.nvlString(lzParamsMap.get("RWBH"));
		lzParamsMap.putAll(super.createUserMap4DB(userContext, new String[] { "XXCZ" }));
		lzParamsMap.put("GXSJ", "SYSDATE");
		int update = DBSql.update(SQLUtils.getCreateUpdateSQL(lzParamsMap, "BO_EU_XSHB_RW_LZ", new String[] { "ID" }));
		int upddaGlyw = DBSql.update(ResourceUtils.getSQL("update_xbGlyw"), new String[] { xsbh, xbbh, xbbh });
		int upddaGlfj = DBSql.update(ResourceUtils.getSQL("update_xbGlFj"), new String[] { xsbh, xbbh, xbbh });
		commonService.updateTaskProcessStatus(userContext, paramsMap, "10");
		commonService.updateTaskStatus(userContext, paramsMap, "10");
		commonService.addProcessRecord(userContext, paramsMap, "11");
		changeWdry(xsbh);
		return update;
	}

	/**
	 * 续报 更新未读人员
	 * 
	 * @param xsbh
	 */
	public void changeWdry(String xsbh) {
		String rwlzId = DBSql.getString(ResourceUtils.getSQL("xb_getReportLzId"), new String[] { xsbh });
		BO reportLzBo = SDK.getBOAPI().get("BO_EU_XSHB_RW_LZ", rwlzId);
		String rwlzbh = reportLzBo.getString("RWLZBH");
		String wdry = DBSql.getString(
				"select string_agg(lz.qsry_gmsfhm,',') from bo_eu_xshb_rw_sb sb,bo_eu_xshb_rw_glxs gl,bo_eu_xshb_rw_lz lz where gl.rwbh = sb.rwbh and sb.rwbh = lz.rwbh and lz.sfzzlz_pdbz ='1' and gl.xsbh =? and (gl.rwbh != '' or gl.rwbh !=null)",
				new String[] { xsbh });
		String sql = "update BO_EU_XSHB_RW_LZ set WDRY=? where id =?";
		DBSql.update(sql, new String[] { wdry, rwlzId });
	}

	/**
	 * 续报任务审批驳回 需要传XBHTYJ_JYQK 驳回原因
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	public int rejected(UserContext userContext, Map<String, Object> paramsMap) {
		Map<String, Object> lzParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_LZ");
		lzParamsMap.putAll(super.createUserMap4DB(userContext, new String[] { "XXCZ" }));
		lzParamsMap.put("GXSJ", "SYSDATE");
		int update = DBSql.update(SQLUtils.getCreateUpdateSQL(lzParamsMap, "BO_EU_XSHB_RW_LZ", new String[] { "ID" }));
		commonService.updateTaskProcessStatus(userContext, paramsMap, "03");
		commonService.updateTaskStatus(userContext, paramsMap, "03");
		commonService.addProcessRecord(userContext, paramsMap, "12");
		return update;
	}

	/**
	 * 放弃续报任务
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	public int abandon(UserContext userContext, Map<String, Object> paramsMap) {
		Map<String, Object> lzParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_LZ");
		lzParamsMap.putAll(super.createUserMap4DB(userContext, new String[] { "XXCZ" }));
		lzParamsMap.put("GXSJ", "SYSDATE");
		int update = DBSql.update(SQLUtils.getCreateUpdateSQL(lzParamsMap, "BO_EU_XSHB_RW_LZ", new String[] { "ID" }));
		commonService.updateTaskProcessStatus(userContext, paramsMap, "04");
		commonService.updateTaskStatus(userContext, paramsMap, "04");
		commonService.addProcessRecord(userContext, paramsMap, "05");
		return update;
	}

	/**
	 * 查看流程记录（新）
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	public List<RowMap> getProcessRecord_new(UserContext userContext, Map<String, Object> paramsMap) {
		String RWLZBH = getRootId(StringUtils.nvlString(paramsMap.get("RWLZBH")));
		RowMap lz_data = DBSql.getMap("select * from BO_EU_XSHB_RW_LZ where RWLZBH=?",
				new String[] { StringUtils.nvlString(paramsMap.get("RWLZBH")) });
		String RWBH = lz_data.getString("RWBH");
		String pid = lz_data.getString("PID");
		String SFZZLZ_PDBZ = lz_data.getString("SFZZLZ_PDBZ");
		String XXDJRY_GMSFHM = lz_data.getString("XXDJRY_GMSFHM");

		List<RowMap> rows = Lists.newArrayList();
		List<RowMap> lcjl_all = DBSql.getMaps(ResourceUtils.getSQL("select_lcjl_new"), new String[] { RWLZBH });
		// 获取任务状态
		String rwzt = DBSql.getString(ResourceUtils.getSQL("select_rwzt"), new String[] { RWBH, RWBH });
		if ("04".equals(rwzt) || "10".equals(rwzt) || (!"1".equals(SFZZLZ_PDBZ) && StringUtils.isNotEmpty(pid))
				|| ("1".equals(SFZZLZ_PDBZ) && !StringUtils.isNotEmpty(pid)) || ("1".equals(SFZZLZ_PDBZ)
						&& StringUtils.isNotEmpty(pid) && !userContext.getUID().equals(XXDJRY_GMSFHM))) {// 任务结束或者终止
			rows = lcjl_all;
		}
		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("CZLXDM", "CODE_XSHB_CZLX");
		columnConfigMap.put("DJSJ", "DATETIME");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		return rows;
	}

	/**
	 * 根据任务流转编号递归查询跟节点ID(MCH)
	 * 
	 * @return
	 */
	public String getRootId(String RWLZBH) {
		String pid;
		String rid;
		String sql = ResourceUtils.getSQL("select_rw_lz") + " and RWLZBH = ? ";
		do {
			RowMap rowMap = DBSql.getMap(sql, new Object[] { RWLZBH });
			pid = rowMap.getString("PID");
			if (org.apache.commons.lang.StringUtils.isBlank(pid)) {
				rid = RWLZBH;
				break;
			} else {
				RWLZBH = pid;
			}
		} while (true);
		return rid;
	}

	/**
	 * 查看流程记录
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	public List<RowMap> getProcessRecord(UserContext userContext, Map<String, Object> paramsMap) {
		List<RowMap> rows = null;
		String sql = ResourceUtils.getSQL("select_lcjl");
		String rwlzbh = StringUtils.nvlString(paramsMap.get("RWLZBH"));
		String rid = getRootId(rwlzbh);
		List<RowMap> list = DBSql.getMaps(ResourceUtils.getSQL("select_rw_lzxx"),
				new Object[] { userContext.getUID(), userContext.getUID(), rwlzbh });
		if (list.size() > 0) {
			String sfzzlz_pdbz = list.get(0).getString("SFZZLZ_PDBZ");
			if ("0".equals(sfzzlz_pdbz)) {
				rows = DBSql.getMaps(sql, new String[] { rid, rid, rwlzbh });
			} else {
				rows = DBSql.getMaps(sql, new String[] { rid, rwlzbh, rwlzbh });
			}
		} else {
			rows = DBSql.getMaps(sql, new String[] { rid, rwlzbh, rwlzbh });
		}
		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("CZLXDM", "CODE_XSHB_CZLX");
		columnConfigMap.put("DJSJ", "DATETIME");
		CodeService.resultOfTransaction(columnConfigMap, rows);

		return rows;
	}

	/**
	 * 查询对话人
	 * 
	 * @param userContext
	 * @return
	 */
	public List<Map<String, Object>> getTalkingPerson(UserContext userContext, Map<String, Object> paramsMap) {
		String rwbh = StringUtils.nvlString(paramsMap.get("RWBH"));
		if (!StringUtils.isNotEmpty(rwbh)) {
			rwbh = StringUtils.nvlString(paramsMap.get("XBBH"));
		}
		// String rwzt = DBSql.getString(ResourceUtils.getSQL("select_rwzt"),new
		// String[] { rwbh , rwbh });
		/*
		 * if("04".equals(rwzt) || "10".equals(rwzt)) { String xxdjry =
		 * DBSql.getString(ResourceUtils.getSQL("select_rw_djry"),new String[] { rwbh
		 * }); userContext = UserContext.fromUID(xxdjry); }
		 */
		// 根据任务编号查询是发给我的还是我下发的
		String role = DBSql.getString(ResourceUtils.getSQL("select_rw_wdjs"),
				new Object[] { rwbh, userContext.getUID() });
		;
		List<Map<String, Object>> resultList = Lists.newArrayList();

		if ("receive".equals(role)) {
			// 办理人看到的是任务发起人的头像且只能看到一个头像

			String sql = ResourceUtils.getSQL("select_rw_lz_receive");
			List<RowMap> rows = DBSql.getMaps(sql, new Object[] { userContext.getUID(),
					userContext.getDepartmentModel().getId(), userContext.getUID(), rwbh });
			for (RowMap row : rows) {

				Map<String, Object> resultMap = Maps.newHashMap();
				resultMap.put("ID", row.getString("ID"));
				resultMap.put("RWBH", row.getString("RWBH"));
				resultMap.put("RWLZBH", row.getString("RWLZBH"));
				resultMap.put("LZZTDM", row.getString("LZZTDM"));
				resultMap.put("BLRY_XM", row.getString("BLRY_XM"));
				resultMap.put("BLRY_GMSFHM", row.getString("BLRY_GMSFHM"));
				resultMap.put("BLDW_GAJGMC", row.getString("BLDW_GAJGMC"));
				resultMap.put("BLDW_GAJGJGDM", row.getString("BLDW_GAJGJGDM"));
				resultMap.put("RYTXDZ", SDK.getPortalAPI().getUserPhoto(userContext, row.getString("BLRY_GMSFHM")));
				addWFCTaskData(resultMap, row.getString("BINDID"), userContext);
				resultList.add(resultMap);
			}
		} else {
			// 发送人查看头像，必须最终接收人或者单位有人签收或者回退时才能看见相应头像
			String sql = ResourceUtils.getSQL("select_rw_lz_send");
			List<RowMap> rows = DBSql.getMaps(sql, new Object[] { rwbh, rwbh });
			for (RowMap row : rows) {
				Map<String, Object> resultMap = Maps.newHashMap();
				resultMap.put("ID", row.getString("ID"));
				resultMap.put("RWBH", row.getString("RWBH"));
				resultMap.put("RWLZBH", row.getString("RWLZBH"));
				resultMap.put("LZZTDM", row.getString("LZZTDM"));
				resultMap.put("BLRY_XM", row.getString("BLRY_XM"));
				resultMap.put("BLRY_GMSFHM", row.getString("BLRY_GMSFHM"));
				resultMap.put("BLDW_GAJGMC", row.getString("BLDW_GAJGMC"));
				resultMap.put("BLDW_GAJGJGDM", row.getString("BLDW_GAJGJGDM"));
				resultMap.put("RYTXDZ", SDK.getPortalAPI().getUserPhoto(userContext, row.getString("BLRY_GMSFHM")));
				addWFCTaskData(resultMap, row.getString("BINDID"), userContext);
				resultList.add(resultMap);
			}
		}
		return resultList;
	}

	private void addWFCTaskData(Map<String, Object> resultMap, String bindId, UserContext userContext) {
		String processInstId = StringUtils
				.nvlString(SDK.getProcessAPI().getVariable(ProcessUtils.getProcessInfo(bindId), "processBindId"));
		List<String> processIntsIdList = Arrays.asList(processInstId.split(","));
		// WFC_TASK 任务实例表中 target 字段值仅只有一个值无需使用like语法
		String sql = ResourceUtils.getSQL("select_wfc_task") + " and PROCESSINSTID in('"
				+ org.apache.commons.lang.StringUtils.join(processIntsIdList, "','") + "') and target = '"
				+ userContext.getUID() + "'";
		RowMap rowMap = DBSql.getMap(sql);
		if (rowMap != null) {
			resultMap.putAll(rowMap);
		}
	}

	/**
	 * 转派
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @return
	 * @throws InterruptedException
	 */
	public int[] transfer(UserContext userContext, Map<String, Object> paramsMap, ProcessInstance processInst)
			throws InterruptedException {
		String rwlzbh = SequenceUtils.getSequenceVal("SEQ_BO_EU_XSHB_RW_LZ", "",
				userContext.getUserModel().getDepartmentId(), "yyyymmdd", 30);
		Map<String, Object> lzParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_LZ");
		String id = StringUtils.nvlString(lzParamsMap.remove("ID"));

		int[] update = new int[3];

		// 修改转派发起人状态信息
		BO _lzBO = SDK.getBOAPI().get("BO_EU_XSHB_RW_LZ", id);
		Map<String, Object> _lzparamsMap = Maps.newHashMap();
		_lzparamsMap.put("SFZP_PDBZ", "1");
		_lzparamsMap.put("LZZTDM", "07");
		_lzparamsMap.put("ID", id);
		_lzparamsMap.put("GXSJ", "SYSDATE");
		_lzparamsMap.putAll(super.createUserMap4DB(userContext, new String[] { "XXCZ" }));
		update[0] = DBSql.update(SQLUtils.getCreateUpdateSQL(_lzparamsMap, "BO_EU_XSHB_RW_LZ", new String[] { "ID" }));

		Thread.sleep(1000);

		// 新增转派接收流转信息
		lzParamsMap.putAll(super.createUserMap4DB(userContext, new String[] { "XXDJ", "XXCZ" }));
		lzParamsMap.put("RWLZBH", rwlzbh);// 任务流转编号
		lzParamsMap.put("RWBH", _lzBO.get("RWBH"));// 任务编号
		lzParamsMap.put("PID", _lzBO.get("RWLZBH"));//
		lzParamsMap.put("LZZTDM", "07");
		lzParamsMap.put("RWLXDM", _lzBO.get("RWLXDM"));
		lzParamsMap.put("SFZZLZ_PDBZ", "0");
		BO lzBO = new BO().setAll(lzParamsMap);
		ProcessInstance childProcess = SDK.getProcessQueryAPI().parentProcessInstId(processInst.getId()).detail();
		update[1] = SDK.getBOAPI().create("BO_EU_XSHB_RW_LZ", lzBO, childProcess, userContext);
		lzParamsMap.put("ID", lzBO.getId());
		update[2] = DBSql.update("update WFC_TASK set TARGET=? where processinstid=?",
				new String[] { StringUtils.nvlString(lzParamsMap.get("JSRY_GMSFHM")), childProcess.getId() });

		commonService.updateTaskProcessStatus(userContext, paramsMap, "07");
		commonService.updateTaskStatus(userContext, paramsMap, "07");
		commonService.addProcessRecord(userContext, paramsMap, "08");
		return update;
	}

	public Map<String, Object> getTalkingDetail(UserContext userContext, Map<String, Object> paramsMap) {
		String gtbh = StringUtils.nvlString(paramsMap.get("GTBH"));
		Map<String, Object> glywParamsMap = Maps.newHashMap();
		glywParamsMap.put("YWBH", gtbh);
		Map<String, Object> resultMap = Maps.newHashMap();
		// 串并案件
		resultMap.put("cbasj",
				JSON.toJSONString(commonService.getSeriesCase(userContext, glywParamsMap, 1, 5, "gl.DJSJ", "DESC")));
		// 串并人员
		resultMap.put("cbry",
				JSON.toJSONString(commonService.getSeriesPerson(userContext, glywParamsMap, 1, 5, "gl.DJSJ", "DESC")));
		// 串并线索
		resultMap.put("cbxs",
				JSON.toJSONString(commonService.getSeriesClue(userContext, glywParamsMap, 1, 5, "gl.DJSJ", "DESC")));
		// 附件
		Map<String, Object> fjParamsMap = Maps.newHashMap();
		fjParamsMap.put("GLYWBH", gtbh);
		resultMap.put("fj", JSON.toJSONString(commonService.getRelevanceAttachment(userContext, fjParamsMap)));
		return resultMap;
	}

	/**
	 * 上报线索 查询线索编号
	 * 
	 * @param userContext
	 * @param rwbh
	 * @return
	 */
	public String getReportRw_clue(UserContext userContext, String rwbh) {
		String xsbh = DBSql.getString(
				"select xsbh from BO_EU_XSHB_RW_GLXS glxs, BO_EU_XSHB_RW_SB sb where sb.rwbh = glxs.rwbh and sb.rwbh = ? ",
				new String[] { rwbh });
		return xsbh;
	}

	/**
	 * updateSue 线索起诉数
	 * 
	 * @param userContext
	 * @param param
	 * @return
	 * @throws SQLException
	 */
	public int updateClueSue(UserContext userContext, Map<String, Object> param) throws SQLException {
		Connection connection = null;
		connection = DBSql.open();
		connection.setAutoCommit(false);
		Map<String, Object> map = (Map<String, Object>) param.get("BO_EU_XSHB_XS");
		map.put("QS_PDBZ", "1");
		String qsfile_path = StringUtils.nvlString(map.remove("QS_FILE"));
		String sql = SQLUtils.getCreateUpdateSQL(map, "BO_EU_XSHB_XS", new String[] { "xsbh" });
		// TODO 线索存取法律文书
		Map<String, Object> fjParams = Maps.newHashMap();
		fjParams.put("GLYWBH", StringUtils.nvlString(map.get("xsbh")));
		fjParams.put("TYPE", "02");
		int[] uploadAttachment = commonService.uploadAttachment(userContext, new String[] { qsfile_path }, fjParams,
				connection);
		return DBSql.update(sql);
	}

	/**
	 * 获取线索起诉详情
	 * 
	 * @param userContext
	 * @param param
	 * @return
	 */
	public Map<String, Object> getClueSue(UserContext userContext, Map<String, Object> param) {
		List<RowMap> rowMaps = null;
		if (param.containsKey("XSBH")) {
			rowMaps = DBSql.getMaps(
					"select XSBH, QS_TIME, QSAJ_SN,QSAJ_SW,QSAJ_BD,QSRY from BO_EU_XSHB_XS where xsbh=? ",
					new String[] { StringUtils.nvlString(param.get("XSBH")) });
		} else if (param.containsKey("RWBH")) {
			rowMaps = DBSql.getMaps(
					"select XSBH, QS_TIME, QSAJ_SN,QSAJ_SW,QSAJ_BD,QSRY from BO_EU_XSHB_XS where xsbh=? ",
					new String[] { getReportRw_clue(userContext, StringUtils.nvlString(param.get("RWBH"))) });
		}
		HashMap<String, Object> fjpargarms = Maps.newHashMap();
		fjpargarms.put("GLYWBH", rowMaps.get(0).get("XSBH"));
		fjpargarms.put("TYPE", "02");
		Map<String, Object> map = Maps.newHashMap();
		map.put("XSBH", rowMaps.get(0).get("XSBH"));
		map.put("suein", rowMaps.get(0).get("QSAJ_SN"));
		map.put("sueout", rowMaps.get(0).get("QSAJ_SW"));
		map.put("suelocal", rowMaps.get(0).get("QSAJ_BD"));
		map.put("suery", rowMaps.get(0).get("QSRY"));
		map.put("sufile", JSONObject.toJSONString(commonService.getRelevanceAttachment(userContext, fjpargarms)));
		map.put("sutime", rowMaps.get(0).get("QS_TIME"));
		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("sutime", "datetime");
		return map;
	}

	/**
	 * 其他平台对接创建任务临时编号
	 * 
	 * @return
	 */
	public String getLsRwbh(UserContext userContext, String rwlx, String rwly) {
		String rwbh = SequenceUtils.getSequenceVal(rwlx, "RLS" + rwly + "-",
				userContext.getUserModel().getDepartmentId(), "yyyymm", 28);
		return rwbh;
	}

	/**
	 * 其他平台对接创建任务--新增附件
	 * 
	 * @param userContext
	 * @param lsRwbh
	 * @param extendParamsMap
	 * @return
	 * @throws SQLException
	 */
	public int[] addFj(UserContext userContext, String lsRwbh, Map<String, Object> extendParamsMap)
			throws SQLException {
		String[] paths = StringUtils.nvlString(extendParamsMap.get("FJLJ")).split(",");
		// 存取附件
		Map<String, Object> fjParams = Maps.newHashMap();
		fjParams.put("GLYWBH", lsRwbh);
		fjParams.put("XXZJBH", StringUtils.nvlString(extendParamsMap.get("XXZJBH")));// 信息来源主键编号
		Connection connection = DBSql.open();
		connection.setAutoCommit(false);
		return commonService.uploadAttachment(userContext, paths, fjParams, connection);
	}

	/**
	 * 其他平台对接创建任务--获取附件ID 关联业务ID
	 * 
	 * @param userContext
	 * @param fjRows
	 * @return
	 */
	public Map<String, Object> getFjIds(UserContext userContext, List<RowMap> fjRows, String lsRwbh) {
		Map<String, Object> result = Maps.newHashMap();
		List<String> fjIDs = Lists.newArrayList();
		for (RowMap rowMap : fjRows) {
			fjIDs.add(StringUtils.nvlString(rowMap.get("ID")));
		}

		Map<String, Object> paramsMap = Maps.newHashMap();
		paramsMap.put("YWBH", lsRwbh);
		List<RowMap> list = Collections.EMPTY_LIST;
		String sql = "";
		// 查询关联人员
		sql = ResourceUtils.getSQL("select_ycbry") + SQLUtils.spellWhere(paramsMap);
		list = DBSql.getMaps(sql);
		List<String> glywIDs = Lists.newArrayList();
		List<BO> dataList = Lists.newArrayList();
		for (RowMap rowMap : list) {
			String asjxgrybh = rowMap.getString("ASJXGRYBH");
			BO glywbo = new BO();
			glywbo.set("GLYWBH", asjxgrybh);
			glywbo.set("GLYWLXDM", "04");
			glywbo.setAll(super.createUserMap4DB(userContext, new String[] { "XXDJ" }));
			dataList.add(glywbo);
		}
		// 查询关联物品
		sql = ResourceUtils.getSQL("select_ycbwp") + SQLUtils.spellWhere(paramsMap);
		list = DBSql.getMaps(sql);
		for (RowMap rowMap : list) {
			BO glywbo = new BO();
			glywbo.set("GLYWBH", rowMap.getString("SAWPBH"));
			glywbo.set("GLYWLXDM", "03");
			glywbo.setAll(super.createUserMap4DB(userContext, new String[] { "XXDJ" }));
			dataList.add(glywbo);
		}
		// 查询关联线索
		sql = ResourceUtils.getSQL("select_ycbxs") + SQLUtils.spellWhere(paramsMap);
		list = DBSql.getMaps(sql);
		for (RowMap rowMap : list) {
			BO glywbo = new BO();
			glywbo.set("GLYWBH", rowMap.getString("XSBH"));
			glywbo.set("GLYWLXDM", "01");
			glywbo.setAll(super.createUserMap4DB(userContext, new String[] { "XXDJ" }));
			dataList.add(glywbo);
		}
		// 查询关联案件
		sql = ResourceUtils.getSQL("select_ycbasj") + SQLUtils.spellWhere(paramsMap);
		list = DBSql.getMaps(sql);
		for (RowMap rowMap : list) {
			String asjbh = rowMap.getString("ASJBH");
			BO glywbo = new BO();
			glywbo.set("GLYWBH", asjbh);
			glywbo.set("GLYWLXDM", "02");
			glywbo.set("XXZJBH", rowMap.getString("XXZJBH"));
			glywbo.setAll(super.createUserMap4DB(userContext, new String[] { "XXDJ" }));
			dataList.add(glywbo);
		}
		// 查询关联串并中心（串并中心）
		sql = ResourceUtils.getSQL("select_ycbcb") + SQLUtils.spellWhere(paramsMap);
		list = DBSql.getMaps(sql);
		for (RowMap rowMap : list) {
			String cbbh = rowMap.getString("cbbh");
			BO glywbo = new BO();
			glywbo.set("GLYWBH", cbbh);
			glywbo.set("GLYWLXDM", "05");
			glywbo.set("XXZJBH", rowMap.getString("XXZJBH"));
			glywbo.setAll(super.createUserMap4DB(userContext, new String[] { "XXDJ" }));
			dataList.add(glywbo);
		}
		// TODO 查询串并中心信息
		SDK.getBOAPI().createDataBO("BO_EU_XSHB_GLYW", dataList, userContext);
		for (BO bo : dataList) {
			glywIDs.add(bo.getId());
		}
		result.put("fjIDs", fjIDs);

		result.put("GLXSID", glywIDs);
		return result;
	}

	/**
	 * 其他平台对接创建任务--关联业务数据
	 * 
	 * @param userContext
	 * @param lsRwbh
	 * @param extendParamsMap
	 * @return
	 */
	public int addGlyw(UserContext userContext, String lsRwbh, Map<String, Object> extendParamsMap) {
		// 新增关联业务数据
		Map<String, Object> tempMap = Maps.newHashMap();
		int[] addSeriesBiz = commonService.addSeriesBiz(userContext, extendParamsMap, tempMap);
		String ids = org.apache.commons.lang.StringUtils.join((List<String>) tempMap.get("ID"), ",");// 关联业务IDs
		tempMap.put("ID", ids);
		// 更新关联业务中的YWBH
		tempMap.put("YWBH", lsRwbh);
		tempMap.put("XXZJBH", StringUtils.nvlString(extendParamsMap.get("XXZJBH")));// 外部任务来源信息主键编号
		String sql = SQLUtils.getCreateUpdateSQL(tempMap, "BO_EU_XSHB_GLYW", new String[] { "ID" });
		return DBSql.update(sql);
	}

	public List<String> getGlywIds(UserContext userContext, String lsRwbh) {
		Map<String, Object> paramsMap = Maps.newHashMap();
		paramsMap.put("YWBH", lsRwbh);
		List<RowMap> list = Collections.EMPTY_LIST;
		String sql = "";
		// 查询关联人员
		sql = ResourceUtils.getSQL("select_ycbry") + SQLUtils.spellWhere(paramsMap);
		list = DBSql.getMaps(sql);
		List<String> glywIDs = Lists.newArrayList();
		List<String> fjIDs = Lists.newArrayList();
		List<BO> dataList = Lists.newArrayList();
		for (RowMap rowMap : list) {
			String asjxgrybh = rowMap.getString("ASJXGRYBH");
			BO glywbo = new BO();
			glywbo.set("GLYWBH", asjxgrybh);
			glywbo.set("GLYWLXDM", "04");
			glywbo.setAll(super.createUserMap4DB(userContext, new String[] { "XXDJ" }));
			dataList.add(glywbo);
		}
		// 查询关联物品
		sql = ResourceUtils.getSQL("select_ycbwp") + SQLUtils.spellWhere(paramsMap);
		list = DBSql.getMaps(sql);
		for (RowMap rowMap : list) {
			BO glywbo = new BO();
			glywbo.set("GLYWBH", rowMap.getString("SAWPBH"));
			glywbo.set("GLYWLXDM", "03");
			glywbo.setAll(super.createUserMap4DB(userContext, new String[] { "XXDJ" }));
			dataList.add(glywbo);
		}
		// 查询关联线索
		sql = ResourceUtils.getSQL("select_ycbxs") + SQLUtils.spellWhere(paramsMap);
		list = DBSql.getMaps(sql);
		for (RowMap rowMap : list) {
			BO glywbo = new BO();
			glywbo.set("GLYWBH", rowMap.getString("XSBH"));
			glywbo.set("GLYWLXDM", "01");
			glywbo.setAll(super.createUserMap4DB(userContext, new String[] { "XXDJ" }));
			dataList.add(glywbo);
		}
		// 查询关联案件
		sql = ResourceUtils.getSQL("select_ycbasj") + SQLUtils.spellWhere(paramsMap);
		list = DBSql.getMaps(sql);
		for (RowMap rowMap : list) {
			String asjbh = rowMap.getString("ASJBH");
			BO glywbo = new BO();
			glywbo.set("GLYWBH", asjbh);
			glywbo.set("GLYWLXDM", "02");
			glywbo.setAll(super.createUserMap4DB(userContext, new String[] { "XXDJ" }));
			dataList.add(glywbo);
		}
		// TODO 查询串并中心信息
		SDK.getBOAPI().createDataBO("BO_EU_XSHB_GLYW", dataList, userContext);
		for (BO bo : dataList) {
			glywIDs.add(bo.getId());
		}
		return glywIDs;

	}

	/**
	 * 外部平台下发指令查询任务列表
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @param currPage
	 * @param pageSize
	 * @param sortField
	 * @param sortType
	 * @return
	 */
	public Map<String, Object> getOtherReportTask(UserContext userContext, Map<String, Object> paramsMap, int currPage,
			int pageSize, String sortField, String sortType) {
		String xxzjbh = StringUtils.nvlString(paramsMap.get("XXZJBH"));
		String sql = ResourceUtils.getSQL("selectRwListByOther");
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql, currPage, pageSize),
				new Object[] { xxzjbh, xxzjbh, xxzjbh, xxzjbh });
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql), new String[] { xxzjbh, xxzjbh, xxzjbh, xxzjbh });
		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("XFDW_GAJGJGDM", "CODE_GXS");
		columnConfigMap.put("SBDW_GAJGJGDM", "CODE_GXS");
		columnConfigMap.put("XFDW", "CODE_GXS");
		columnConfigMap.put("XFSJ", "DATE");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 查询拥有xxx权限的人员账户
	 * 
	 * @param UserContext
	 * @param deptId
	 * @param roleName
	 * @return
	 */
	public static String getRoleUsers(UserContext UserContext, String deptId, String roleName) {
		List<String> deptIds = Arrays.asList(deptId.split(","));
		List<String> roleNames = Arrays.asList(roleName.split(","));
		StringBuilder sql = new StringBuilder();
		sql.append(ResourceUtils.getSQL("getRoleBy_deptAroleName")).append(" and dept.dept_id in('")
				.append(StringUtils.join(deptIds, "','")).append("')").append(" and roler.role_name in ('")
				.append(StringUtils.join(roleNames, "','")).append("')");
		String string = DBSql.getString(sql.toString());
		System.out.println(string);
		return string;
	}

}
