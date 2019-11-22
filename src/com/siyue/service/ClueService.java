package com.siyue.service;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.curator.shaded.com.google.common.collect.Maps;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import com.actionsoft.bpms.bo.engine.BO;
import com.actionsoft.bpms.bpmn.engine.model.run.delegate.ProcessInstance;
import com.actionsoft.bpms.commons.database.RowMap;
import com.actionsoft.bpms.org.model.DepartmentModel;
import com.actionsoft.bpms.org.model.UserModel;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.bpms.util.UtilFile;
import com.actionsoft.sdk.local.SDK;
import com.actionsoft.sdk.local.api.BOCopyAPI;
import com.actionsoft.sdk.local.api.BOQueryAPI;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.siyue.util.ConnectionUtils;
import com.siyue.util.ExcelUtils;
import com.siyue.util.ProcessUtils;
import com.siyue.util.ResourceUtils;
import com.siyue.util.SQLUtils;
import com.siyue.util.SequenceUtils;
import com.siyue.util.StringUtils;

/**
 * 线索
 */
public class ClueService extends BaseService {

	private CommonService commonService = new CommonService();
	private ProcessService processService = new ProcessService();

	/**
	 * 我的线索
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @param currPage
	 * @param pageSize
	 * @param sortField
	 * @param sortType
	 * @return
	 */
	public Map<String, Object> getMyClue(UserContext userContext, Map<String, Object> paramsMap, int currPage, int pageSize, String sortField, String sortType) {
		paramsMap.put("XXDJRY_GMSFHM", userContext.getUID());
		String sql = ResourceUtils.getSQL("select_xs") + SQLUtils.spellWhere(paramsMap);
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql), new Object[] {});
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortField) && org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql += " ORDER BY " + sortField + " " + sortType;
		} else {
			sql += " ORDER BY DJSJ DESC";
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql, currPage, pageSize), new Object[] {});

		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("XSLXDM", "CODE_XSHB_XSLX");
		columnConfigMap.put("XSJBDM", "CODE_XSHB_JB");
		columnConfigMap.put("SFSMDM", "CODE_XSHB_SFSM");
		columnConfigMap.put("DJSJ", "DATETIME");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 辖区线索
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @param currPage
	 * @param pageSize
	 * @param sortField
	 * @param sortType
	 * @return
	 */
	public Map<String, Object> getAreaClue(UserContext userContext, Map<String, Object> paramsMap, int currPage, int pageSize, String sortField, String sortType) {
		DepartmentModel departmentModel = userContext.getDepartmentModel();
		String sql = ResourceUtils.getSQL("select_xs");
		if (paramsMap.containsKey("ID")) {
			String subCondition = SQLUtils.spellWhere(ImmutableMap.of("ID", StringUtils.nvlString(paramsMap.remove("ID"))));
			sql += " and XSBH not in (select XSBH from BO_EU_XSHB_RW_GLXS where 1=1 " + subCondition + " union all select GLYWBH from BO_EU_XSHB_GLYW where 1=1 " + subCondition + ")";
		}
		if (paramsMap.containsKey("ID~10")) {
			String subCondition = SQLUtils.spellWhere(ImmutableMap.of("ID", StringUtils.nvlString(paramsMap.remove("ID~10"))));
			sql += " and XSBH not in (select XSBH from BO_EU_XSHB_RW_GLXS where 1=1 " + subCondition + " union all select GLYWBH from BO_EU_XSHB_GLYW where 1=1 " + subCondition + ")";
		}
		sql += "and (sfsmdm='01' or (sfsmdm='02' and xxdjry_gmsfhm='" + userContext.getUID() + "'))  and XXDJDW_GAJGJGDM in (select CODE from CODE_GXS_BIZ where CODE_LEV" + departmentModel.getLayer()
				+ "='" + departmentModel.getId() + "')";
		sql += SQLUtils.spellWhere(paramsMap);
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql), new Object[] {});
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortField) && org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql += " ORDER BY " + sortField + " " + sortType;
		} else {
			sql += " ORDER BY DJSJ DESC";
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql, currPage, pageSize), new Object[] {});

		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("XSLXDM", "CODE_XSHB_XSLX");
		columnConfigMap.put("XSJBDM", "CODE_XSHB_JB");
		columnConfigMap.put("SFSMDM", "CODE_XSHB_SFSM");
		columnConfigMap.put("DJSJ", "DATETIME");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 查询线索关联任务
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @param currPage
	 * @param pageSize
	 * @param sortField
	 * @param sortType
	 * @return
	 */
	public Map<String, Object> getClueRelevanceTask(UserContext userContext, Map<String, Object> paramsMap, int currPage, int pageSize, String sortField, String sortType) {
		String xsbh = StringUtils.nvlString(paramsMap.remove("XSBH"));
		String sql = ResourceUtils.getSQL("select_xsglrw") + SQLUtils.spellWhere(paramsMap);
		System.out.println(sql);
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql), new Object[] { xsbh, xsbh });
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortField) && org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql += " ORDER BY " + sortField + " " + sortType;
		} else {
			sql += " ORDER BY DJSJ DESC";
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql, currPage, pageSize), new Object[] { xsbh, xsbh });

		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("DJSJ", "DATETIME");
		columnConfigMap.put("RWJBDM", "CODE_XSHB_JB");
		columnConfigMap.put("RWZTDM", "CODE_XSHB_RWZT");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 保存线索
	 * 
	 * @throws Exception
	 */
	public int addClue(UserContext userContext, Map<String, Object> paramsMap, Map<String, Object> returnMap, Connection connection) {
		Map<String, Object> xsParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_XS");
		String xslx = StringUtils.nvlString(xsParamsMap.get("XSLXDM"));
		int insert = 0;
		if (xslx.equals("02")) {
			Map<String, Object> glParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_GLWL");
			String pch = StringUtils.nvlString(glParamsMap.get("PCH"));
			String mc = StringUtils.nvlString(xsParamsMap.remove("XSMC"));
			List<RowMap> list = DBSql.getMaps("select * from BO_EU_XSHB_GLWL where pch=?", new Object[] { pch });
			int n = 0;
			for (RowMap rowMap : list) {
				// 更新关联物流
				String id = rowMap.getString("ID");
				String xsbh = SequenceUtils.getSequenceVal("SEQ_BO_EU_XSHB_XS", "X", userContext.getUserModel().getDepartmentId(), "yyyymm", 23);
				String xsmc = mc + "(" + (n++) + ")";
				Map<String, Object> map = Maps.newHashMap();
				map.put("ID", id);
				map.put("XSBH", xsbh);
				String sql = SQLUtils.getCreateUpdateSQL(map, "BO_EU_XSHB_GLWL", new String[] { "ID" });
				int i = DBSql.update(connection, sql);
				// 保存线索表
				BO xsBO = new BO();
				xsBO.setAll(xsParamsMap);
				xsBO.set("XSMC", xsmc);
				xsBO.set("GXSJ", new Date());
				xsBO.set("XSBH", xsbh);
				xsBO.setAll(super.createUserMap4DB(userContext, new String[] { "XXDJ", "XXCZ" }));
				int j = SDK.getBOAPI().createDataBO("BO_EU_XSHB_XS", xsBO, userContext, connection);
				// 存取附件
				Map<String, Object> fjParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_FJ");
				int[] z = null;
				if (fjParamsMap != null && !fjParamsMap.isEmpty()) {
					String[] paths = StringUtils.nvlString(fjParamsMap.get("FJLJ")).split(",");
					Map<String, Object> fjParams = Maps.newHashMap();
					fjParams.put("GLYWBH", xsbh);
					z = commonService.uploadAttachment(userContext, paths, fjParams, connection);
				}

				// 更新关联业务表
				if (paramsMap.containsKey("BO_EU_XSHB_GLYW")) {
					Map<String, Object> glyw = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_GLYW");
					if (isUpdate(StringUtils.nvlString(glyw.get("ID")))) {
						copyGlyw(xsbh, StringUtils.nvlString(glyw.get("ID")));
					} else {
						glyw.put("YWBH", xsbh);
						String insertGlyw = SQLUtils.getCreateUpdateSQL(glyw, "BO_EU_XSHB_GLYW", new String[] { "ID" });
						int update = DBSql.update(insertGlyw);
					}
				}
				if (i == 1 && j == 1 && !Arrays.asList(z).contains("0")) {
					insert = 1;
				} else {
					insert = 0;
				}
			}
			return insert;
		}

		String xsbh = SequenceUtils.getSequenceVal("SEQ_BO_EU_XSHB_XS", "X", userContext.getUserModel().getDepartmentId(), "yyyymm", 23);
		// 保存线索表
		BO xsBO = new BO();
		xsBO.setAll(xsParamsMap);
		xsBO.set("XSBH", xsbh);
		xsBO.setAll(super.createUserMap4DB(userContext, new String[] { "XXDJ", "XXCZ" }));
		insert = SDK.getBOAPI().createDataBO("BO_EU_XSHB_XS", xsBO, userContext, connection);
		// 更新关联业务表
		if (paramsMap.containsKey("BO_EU_XSHB_GLYW")) {
			Map<String, Object> glParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_GLYW");
			glParamsMap.put("YWBH", xsbh);
			String sql = SQLUtils.getCreateUpdateSQL(glParamsMap, "BO_EU_XSHB_GLYW", new String[] { "ID" });
			DBSql.update(connection, sql);

			if (xsParamsMap.containsKey("YSXSBH")) {
				StringBuffer updateSql = new StringBuffer("");
				updateSql.append(
						"update bo_eu_xshb_glyw set ywbh=(select ysxsbh from bo_eu_xshb_xs where xsbh=?) where glywbh in (select glywbh from bo_eu_xshb_glyw where ywbh=(select ysxsbh from bo_eu_xshb_xs where xsbh=?))");
				DBSql.update(connection, updateSql.toString(), new String[] { xsbh, xsbh });
			}

		}
		// 更新关联逃犯表
		if (paramsMap.containsKey("BO_EU_XSHB_GLTF")) {
			Map<String, Object> glParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_GLTF");
			glParamsMap.put("XSBH", xsbh);
			String sql = SQLUtils.getCreateUpdateSQL(glParamsMap, "BO_EU_XSHB_GLTF", new String[] { "ID" });
			DBSql.update(connection, sql);
		}
		// 存取附件
		if (paramsMap.containsKey("BO_EU_XSHB_FJ")) {
			Map<String, Object> fjParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_FJ");
			String[] paths = StringUtils.nvlString(fjParamsMap.get("FJLJ")).split(",");
			Map<String, Object> fjParams = Maps.newHashMap();
			fjParams.put("GLYWBH", xsbh);
			commonService.uploadAttachment(userContext, paths, fjParams, connection);
		}
		returnMap.put("XSBH", xsbh);
		return insert;
	}

	/**
	 * 线索续报新增
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @param returnMap
	 * @param connection
	 * @return
	 */
	public int[] resubmit(UserContext userContext, Map<String, Object> paramsMap, Map<String, Object> returnMap, Connection connection) {
		Map<String, Object> xsParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_XS");
		String xsbh = StringUtils.nvlString(xsParamsMap.get("XSBH"));
		// 续报编号
		String xbbh = SequenceUtils.getSequenceVal("SEQ_BO_EU_XSHB_RW_SB", "R", userContext.getUserModel().getDepartmentId(), "yyyymm", 23);
		System.out.println(xbbh);
		int[] insert = { 0, 0, 0 };
		// 新增续报信息
		BO rwxb = new BO();
		rwxb.set("XSBH", xsbh);
		rwxb.set("XBBH", xbbh);
		rwxb.setAll(super.createUserMap4DB(userContext, new String[] { "XBDJ" }));
		insert[0] = SDK.getBOAPI().createDataBO("BO_EU_XSHB_RW_XB", rwxb, userContext, connection);

		// 更新关联业务表
		if (paramsMap.containsKey("BO_EU_XSHB_GLYW")) {
			Map<String, Object> glParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_GLYW");
			glParamsMap.put("YWBH", xbbh);
			glParamsMap.put("XBBH", xbbh);
			String sql = SQLUtils.getCreateUpdateSQL(glParamsMap, "BO_EU_XSHB_GLYW", new String[] { "ID" });
			insert[1] = DBSql.update(connection, sql);
		}
		// 存取附件
		if (paramsMap.containsKey("BO_EU_XSHB_FJ")) {
			Map<String, Object> fjParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_FJ");
			String[] paths = StringUtils.nvlString(fjParamsMap.get("FJLJ")).split(",");
			Map<String, Object> fjParams = Maps.newHashMap();
			fjParams.put("GLYWBH", xbbh);
			fjParams.put("XBBH", xbbh);
			int[] ints = commonService.uploadAttachment(userContext, paths, fjParams, connection);
			if (!Arrays.asList(ints).contains(0)) {
				insert[2] = 1;
			} else {
				insert[2] = 0;
			}
		}
		// 新增流转数据
		String user = null;
		BO lzBO = new BO();
		lzBO.set("RWBH", xbbh);
		lzBO.set("JSDXLXDM", "01");
		lzBO.set("RWLXDM", "03");
		lzBO.set("LZZTDM", "01");
		lzBO.set("RWLZBH", SequenceUtils.getSequenceVal("SEQ_BO_EU_XSHB_RW_LZ", "", userContext.getUserModel().getDepartmentId(), "yyyymmdd", 30));
		if (lzBO.getString("JSDXLXDM").equals("02")) {
			String parentDepartmentId = userContext.getDepartmentModel().getParentDepartmentId();
			Map<String, Object> map = com.google.common.collect.Maps.newHashMap();
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
			lzBO.set("WDRY", commonService.getDepartmentUser(userContext, parentDepartmentId));
		} else if (lzBO.getString("JSDXLXDM").equals("01")) {
			user = DBSql.getString(ResourceUtils.getSQL("select_signUser"), new String[] { ProcessUtils.getActiviteRwbh(xsbh) });
			lzBO.set("JSDW_GAJGMC", SDK.getORGAPI().getDepartmentByUser(user).getName());
			lzBO.set("JSDW_GAJGJGDM", SDK.getORGAPI().getDepartmentByUser(user).getId());
			lzBO.set("JSRY_XM", SDK.getORGAPI().getUser(user).getUserName());
			lzBO.set("JSRY_GMSFHM", user);
			lzBO.set("JSRY_LXDH", "");
			String wdry = DBSql.getString(
					"select string_agg(lz.qsry_gmsfhm,',') from bo_eu_xshb_rw_sb sb,bo_eu_xshb_rw_glxs gl,bo_eu_xshb_rw_lz lz where gl.rwbh = sb.rwbh and sb.rwbh = lz.rwbh and lz.sfzzlz_pdbz ='1' and gl.xsbh =? and (gl.rwbh != '' or gl.rwbh !=null)",
					new String[] { xsbh });
			lzBO.set("WDRY", wdry);
		}
		lzBO.setAll(super.createUserMap4DB(userContext, new String[] { "XXDJ", "XXCZ" }));
		Map<String, Object> startProcess = processService.startProcess(userContext, "03", user);
		ProcessInstance processInstance = (ProcessInstance) startProcess.get("ProcessInstance");
		int create = SDK.getBOAPI().create("BO_EU_XSHB_RW_LZ", lzBO, processInstance, userContext, connection);
		if (create < 0) {
			SDK.getProcessAPI().delete(processInstance, userContext);
		}
		String rwbh = ProcessUtils.getActiviteRwbh(xsbh);
		DBSql.update("update bo_eu_xshb_rw_lz set wdry =? where rwlzbh = (select rwlzbh from bo_eu_xshb_rw_lz where rwbh =? and sfzzlz_pdbz = '1')", new String[] { user, rwbh });

		return insert;
	}

	/**
	 * 修改续报
	 *
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	public int[] updateResubmite(UserContext userContext, Map<String, Object> paramsMap, Connection connection, Map<String, Object> returnMap) {
		int[] update = new int[2];
		// 附件更新,先删在增
		Map<String, Object> clueParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_XS");
		String xsbh = StringUtils.nvlString(clueParamsMap.get("XSBH"));
		Map<String, Object> fjParams = Maps.newHashMap();
		Map<String, Object> fjParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_FJ");
		fjParams.put("GLYWBH", xsbh);
		update[0] = DBSql.update(connection, SQLUtils.getCreateDeleteSQL(fjParams, "BO_EU_XSHB_FJ"));
		if (fjParamsMap != null && !fjParamsMap.isEmpty()) {
			String[] paths = StringUtils.nvlString(fjParamsMap.get("FJLJ")).split(",");
			commonService.uploadAttachment(userContext, paths, fjParams, connection);
		}

		// 关联业务更新 先删后增
		Map<String, Object> glywParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_GLYW");
		Map<String, Object> glywParams = Maps.newHashMap();
		glywParams.put("YWBH", xsbh);
		DBSql.update(connection, SQLUtils.getCreateDeleteSQL(glywParams, "BO_EU_XSHB_GLYW"));
		List<String> glywIdList = Arrays.asList(StringUtils.nvlString(glywParamsMap.get("ID")).split(","));
		System.out.println(glywIdList.toString());
		String sql = "update BO_EU_XSHB_GLYW set ywbh=? where id in('" + org.apache.commons.lang.StringUtils.join(glywIdList, "','") + "')";
		update[2] = DBSql.update(connection, sql, new String[] { xsbh });
		return update;
	}

	public static void copyGlyw(String xsbh, String ids) {
		List<String> id = Arrays.asList(ids.split(","));
		BOQueryAPI query = SDK.getBOAPI().query("BO_EU_XSHB_GLYW", true).addQuery("id in ('" + org.apache.commons.lang.StringUtils.join(id, "','") + "')", null);
		BOCopyAPI copyAPI = query.copyTo("BO_EU_XSHB_GLYW", UUID.randomUUID().toString());
		copyAPI.addNewData("YWBH", xsbh);
		copyAPI.exec();
	}

	public static boolean isUpdate(String ids) {
		List<String> id = Arrays.asList(ids.split(","));
		String sql = ResourceUtils.getSQL("isUpdate_glyw") + "and id in ('" + org.apache.commons.lang3.StringUtils.join(id, "','") + "')";
		int count = DBSql.getInt(sql, new String[] {});
		return count > 0 ? true : false;
	}

	/**
	 * 修改线索
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	public int[] updateClue(UserContext userContext, Map<String, Object> paramsMap, Connection connection, Map<String, Object> returnMap) {
		int[] update = new int[3];
		// 附件更新,先删在增
		Map<String, Object> clueParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_XS");
		String xsbh = StringUtils.nvlString(clueParamsMap.get("XSBH"));
		Map<String, Object> fjParams = Maps.newHashMap();
		Map<String, Object> fjParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_FJ");
		fjParams.put("GLYWBH", xsbh);
		update[0] = DBSql.update(connection, SQLUtils.getCreateDeleteSQL(fjParams, "BO_EU_XSHB_FJ"));
		if (fjParamsMap != null && !fjParamsMap.isEmpty()) {
			String[] paths = StringUtils.nvlString(fjParamsMap.get("FJLJ")).split(",");
			commonService.uploadAttachment(userContext, paths, fjParams, connection);
		}
		// 线索更新
		clueParamsMap.putAll(super.createUserMap4DB(userContext, new String[] { "XXCZ" }));
		clueParamsMap.put("GXSJ", "SYSDATE");
		returnMap.put("XSBH", xsbh);
		update[1] = DBSql.update(connection, SQLUtils.getCreateUpdateSQL(clueParamsMap, "BO_EU_XSHB_XS", new String[] { "XSBH" }));

		// 关联业务更新 先删后增
		Map<String, Object> glywParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_GLYW");
		Map<String, Object> glywParams = Maps.newHashMap();
		glywParams.put("YWBH", xsbh);
		DBSql.update(connection, SQLUtils.getCreateDeleteSQL(glywParams, "BO_EU_XSHB_GLYW"));
		List<String> glywIdList = Arrays.asList(StringUtils.nvlString(glywParamsMap.get("ID")).split(","));
		System.out.println(glywIdList.toString());
		String sql = "update BO_EU_XSHB_GLYW set ywbh=? where id in('" + org.apache.commons.lang.StringUtils.join(glywIdList, "','") + "')";
		update[2] = DBSql.update(connection, sql, new String[] { xsbh });
		return update;
	}

	/**
	 * 更新续报任务
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @param connection
	 * @param returnMap
	 * @return
	 */
	public int[] updateResubmit(UserContext userContext, Map<String, Object> paramsMap, Connection connection, Map<String, Object> returnMap) {
		String xbbh = StringUtils.nvlString(paramsMap.get("XBBH"));
		int[] update = new int[2];
		// 附件更新,先删在增
		Map<String, Object> fjParams = Maps.newHashMap();
		Map<String, Object> fjParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_FJ");
		fjParams.put("GLYWBH", xbbh);
		update[0] = DBSql.update(connection, SQLUtils.getCreateDeleteSQL(fjParams, "BO_EU_XSHB_FJ"));
		if (fjParamsMap != null && !fjParamsMap.isEmpty()) {
			String[] paths = StringUtils.nvlString(fjParamsMap.get("FJLJ")).split(",");
			commonService.uploadAttachment(userContext, paths, fjParams, connection);
		}

		// 关联业务更新 先删后增
		Map<String, Object> glywParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_GLYW");
		Map<String, Object> glywParams = Maps.newHashMap();
		glywParams.put("YWBH", xbbh);
		String deletSql = SQLUtils.getCreateDeleteSQL(glywParams, "BO_EU_XSHB_GLYW");
		DBSql.update(deletSql);
		List<String> glywIdList = Arrays.asList(StringUtils.nvlString(glywParamsMap.get("ID")).split(","));
		String sql = "update BO_EU_XSHB_GLYW set ywbh=?,xbbh=? where id in('" + org.apache.commons.lang.StringUtils.join(glywIdList, "','") + "')";
		update[1] = DBSql.update(connection, sql, new String[] { xbbh, xbbh });

		commonService.updateTaskProcessStatus(userContext, paramsMap, "12");
		commonService.updateTaskStatus(userContext, paramsMap, "12");
		commonService.addProcessRecord(userContext, paramsMap, "13");

		return update;
	}

	/**
	 * 删除线索
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	public int deleteClue(UserContext userContext, Map<String, Object> paramsMap) {
		Map<String, Object> clueParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_XS");
		clueParamsMap.put("XXSC_PDBZ", "1");
		return DBSql.update(SQLUtils.getCreateUpdateSQL(clueParamsMap, "BO_EU_XSHB_XS", new String[] { "XSBH" }));
	}

	/**
	 * 线索关联任务
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @param currPage
	 * @param pageSize
	 * @param sortField
	 * @param sortType
	 * @return
	 */
	public Map<String, Object> clueRelevanceTask(UserContext userContext, Map<String, Object> paramsMap, int currPage, int pageSize, String sortField, String sortType) {
		String sql = ResourceUtils.getSQL("select_xsglrw") + SQLUtils.spellWhere(paramsMap);
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql), new Object[] {});
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortField) && org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql += " ORDER BY " + sortField + " " + sortType;
		} else {
			sql += " ORDER BY DJSJ DESC";
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql, currPage, pageSize), new Object[] {});

		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("DJSJ", "DATETIME");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 根据线索编号查询线索基本信息
	 */
	public RowMap getClue(UserContext userContext, Map<String, Object> paramsMap, boolean needTransaction) {
		String sql = ResourceUtils.getSQL("select_xs") + SQLUtils.spellWhere(paramsMap);
		RowMap rowMap = DBSql.getMap(sql);
		Map<String, String> columnConfigMap = Maps.newHashMap();
		if (needTransaction) {
			columnConfigMap.put("DJSJ", "DATETIME");
			columnConfigMap.put("XSLXDM", "CODE_XSHB_XSLX");
			columnConfigMap.put("XSLYDM", "CODE_XSHB_XSLY");
			columnConfigMap.put("SFSMDM", "CODE_XSHB_SFSM");
			columnConfigMap.put("XSJBDM", "CODE_XSHB_JB");
			CodeService.resultOfTransaction(columnConfigMap, rowMap);
		}
		return rowMap;
	}

	public static boolean isValidDate(String str) {
		boolean convertSuccess = true;
		// 指定日期格式为四位年/两位月份/两位日期，注意yyyy/MM/dd区分大小写；
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// 设置lenient为false.
		// 否则SimpleDateFormat会比较宽松地验证日期，比如2007/02/29会被接受，并转换成2007/03/01
		try {
			format.setLenient(false);
			format.parse(str);
		} catch (Exception e) {
			// 如果throw java.text.ParseException或者NullPointerException，就说明格式不对
			convertSuccess = false;
		}
		return convertSuccess;
	}

	private String getCellValue(Cell cell) {
		String stringCellValue;
		DecimalFormat format = new DecimalFormat("#");
		if (cell == null) {
			stringCellValue = "";
		} else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
			stringCellValue = String.valueOf(cell.getBooleanCellValue());
		} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			if (DateUtil.isCellDateFormatted(cell)) {
				Date date = cell.getDateCellValue();
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				stringCellValue = simpleDateFormat.format(date);
			} else {
				stringCellValue = format.format(cell.getNumericCellValue());
				// stringCellValue =
				// NumberFormat.getInstance().format(Float.parseFloat(String.valueOf(cell.getNumericCellValue()).trim()));
			}
		} else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
			stringCellValue = "";
		} else {
			stringCellValue = String.valueOf(cell.getStringCellValue());
		}
		return stringCellValue;
	}

	/**
	 * 导入关联物流数据
	 * 
	 * @return
	 * @throws Exception
	 */
	public int[] importRelevanceLogistics(UserContext userContext, Map<String, Object> paramsMap, Map<String, Object> returnMap) throws Exception {
		Map<String, String> contrastMap = (Map<String, String>) paramsMap.get("contrastMap");
		String uuid = StringUtils.nvlString(paramsMap.get("uuid")); // 批次号
		String filePath = StringUtils.nvlString(paramsMap.get("filePath"));
		UserModel userModel = userContext.getUserModel();
		// excel入库数据list
		List<BO> recordDatas = Lists.newArrayList();
		// excel表头序号list
		UtilFile tmpFile = new UtilFile(filePath);
		byte[] data = tmpFile.readBytes();

		Workbook workbook = ExcelUtils.readExcel(new ByteArrayInputStream(data));
		Sheet sheet = workbook.getSheetAt(0);

		int lastMergeRowNum = 0;
		// 判断跨行
		int numMergedRegions = sheet.getNumMergedRegions();
		if (numMergedRegions > 0) {
			CellRangeAddress mergedRegion = sheet.getMergedRegion(numMergedRegions - 1);
			lastMergeRowNum = mergedRegion.getLastRow();
		}

		for (int rowNum = lastMergeRowNum + 2; rowNum <= sheet.getLastRowNum(); rowNum++) {
			Row row = sheet.getRow(rowNum);
			if (row == null) {
				continue;
			}
			// 判断整行是否为空
			int m = 0;
			for (; m < sheet.getRow(rowNum).getPhysicalNumberOfCells(); m++) {
				String value = getCellValue(row.getCell(m)).trim();
				if (org.apache.commons.lang.StringUtils.isNotBlank(value)) {
					break;
				}
			}
			if (m == sheet.getRow(rowNum).getPhysicalNumberOfCells()) {
				continue;
			}
			// 0,无效;1,有效
			// int[] validateArr = { 0, 0, 0, 0 };
			int[] validateArr = { 0, 0 };

			BO bo = new BO();
			for (int cellNum = 0; cellNum < sheet.getRow(rowNum).getPhysicalNumberOfCells(); cellNum++) {
				Cell cell = row.getCell(cellNum);
				// 循环要入库的列,并入库每个对应的字段
				if (contrastMap.containsKey(String.valueOf(cellNum))) {
					String value = getCellValue(cell).trim();
					String field = contrastMap.get(String.valueOf(cellNum));
					bo.set(field, value);
				}
			}
			// 关键信息数据过滤(收件地址)
			if (bo.containsKey("SJR_XZZ_DZMC")) {
				String value = StringUtils.nvlString(bo.get("SJR_XZZ_DZMC"));
				if (org.apache.commons.lang.StringUtils.isNotBlank(value)) {
					validateArr[0] = 1;
				}
			}
			// 联系电话
			if (bo.containsKey("SJR_LXDH")) {
				String regex = "^1(?:3\\d|4[4-9]|5[0-35-9]|6[67]|7[013-8]|8\\d|9\\d)\\d{8}$";
				String value = StringUtils.nvlString(bo.get("SJR_LXDH"));
				boolean matches = value.matches(regex);
				if (org.apache.commons.lang.StringUtils.isNotBlank(value) && matches) {
					validateArr[1] = 1;
				}
			}
			// 地市验证
			// String dwSql = "";
			// if (bo.containsKey("SJR_CS")) {
			// String cs_value = StringUtils.nvlString(bo.get("SJR_CS"));
			// if (StringUtils.isNotBlank(cs_value)) {
			// validateArr[2] = 1;
			// }
			// dwSql = "select DW_CODE from CODE_WLFQXDXZDWZH where SHIJ_NAME like '%" +
			// StringUtils.nvlString(bo.get("SJR_CS")) + "%'";
			// RowMap rowMap = DBSql.getMap(dwSql);
			// if (rowMap != null && !rowMap.isEmpty()) {
			// String code = rowMap.getString("DW_CODE");
			// if (code.startsWith("33")) {
			// bo.set("SJRQH_SSDW_GAJGJGDM", code.substring(0, 4) + "00050000");
			// validateArr[3] = 1;
			// }
			// }
			// }
			String SFYX_PDBZ = "0";
			// if (validateArr[2] == 1 && validateArr[3] == 1) {
			if (validateArr[0] == 1 || validateArr[1] == 1) {
				SFYX_PDBZ = "1";
			}
			// }
			bo.set("PCH", uuid);
			bo.set("SFYX_PDBZ", SFYX_PDBZ);
			bo.set("SFCF_PDBZ", "0");
			bo.setAll(super.createUserMap4DB(userContext, new String[] { "XXCZ", "XXCZ" }));
			// bo.setProcessDefId(paramsMap.get("processDefId"));
			// bo.set("XSBH", SequenceUtils.getSequenceVal("SEQ_BO_EU_XSHB_XS", "X",
			// userModel.getDepartmentId(), "yyyymm", 23));
			// 把这一行的值存入list
			recordDatas.add(bo);
		}
		recordDatas = validateRepeatData(recordDatas);
		returnMap.put("PCH", uuid);
		return SDK.getBOAPI().createDataBO("BO_EU_XSHB_GLWL", recordDatas, userContext);
	}

	// 重复数据验证
	public List<BO> validateRepeatData(List<BO> recordDatas) throws Exception {
		// 0,不重复;1,重复
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// 规则1:快递单号相同,则直接判定为重复数据(excel数据比较)
		List<String> wldhList = Lists.newArrayList();
		for (int i = 0; i < recordDatas.size(); i++) {
			BO bo = recordDatas.get(i);
			if ("1".equals(StringUtils.nvlString(bo.get("SFCF_PDBZ")))) {
				continue;
			}
			String wldh = StringUtils.nvlString(bo.get("WLDH"));
			if (org.apache.commons.lang.StringUtils.isNotBlank(wldh)) {
				wldhList.add(wldh);
			}
			for (int j = 0; j < recordDatas.size(); j++) {
				if (i == j) {
					continue;
				}
				BO _bo = recordDatas.get(j);
				if ("1".equals(StringUtils.nvlString(_bo.get("SFCF_PDBZ")))) {
					continue;
				}
				String _wldh = StringUtils.nvlString(_bo.get("WLDH"));
				if (org.apache.commons.lang.StringUtils.isNotBlank(wldh) && org.apache.commons.lang.StringUtils.isNotBlank(_wldh) && wldh.equals(_wldh)) {
					// 置为重复
					_bo.set("SFCF_PDBZ", "1");
				}
			}
		}
		// 规则1:数据库数据比较
		try {
			String sql = "";
			if ("PGSQL".equals(ConnectionUtils.getConnectionType())) {
				sql = "select string_agg(WLDH,',') from BO_EU_XSHB_GLWL where WLDH in('" + org.apache.commons.lang.StringUtils.join(wldhList, "','") + "') and SFYX_PDBZ = '1'";
			} else if ("ORACLE".equals(ConnectionUtils.getConnectionType())) {
				sql = "select listagg(WLDH,',') within group(order by WLDH) from BO_EU_XSHB_GLWL where WLDH in('" + org.apache.commons.lang.StringUtils.join(wldhList, "','")
						+ "') and SFYX_PDBZ = '1'";
			}
			String wldhs = DBSql.getString(sql);
			for (int i = 0; i < recordDatas.size(); i++) {
				BO bo = recordDatas.get(i);
				if ("1".equals(StringUtils.nvlString(bo.get("SFCF_PDBZ")))) {
					continue;
				}
				String wldh = StringUtils.nvlString(bo.get("WLDH"));
				if (org.apache.commons.lang.StringUtils.isNotBlank(wldhs) && org.apache.commons.lang.StringUtils.isNotBlank(wldh) && wldhs.contains(wldh)) {
					// 置为重复
					bo.set("SFCF_PDBZ", "1");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 规则2:如果快递单号为空或不同,则判定买家信息一致情况下且交易时间符合要求,则判定重复
		for (int i = 0; i < recordDatas.size(); i++) {
			BO bo = recordDatas.get(i);
			if ("1".equals(StringUtils.nvlString(bo.get("SFCF_PDBZ")))) {
				continue;
			}
			String sjr_xm = StringUtils.nvlString(bo.get("SJR_XM"));
			String sjr_xzz_dzmc = StringUtils.nvlString(bo.get("SJR_XZZ_DZMC"));
			String sjr_lxdh = StringUtils.nvlString(bo.get("SJR_LXDH"));
			String ddsj = StringUtils.nvlString(bo.get("DDSJ"));

			for (int j = 0; j < recordDatas.size(); j++) {
				if (i == j) {
					continue;
				}
				BO _bo = recordDatas.get(j);
				if ("1".equals(StringUtils.nvlString(_bo.get("SFCF_PDBZ")))) {
					continue;
				}
				String sjr_xm2 = StringUtils.nvlString(_bo.get("SJR_XM"));
				String sjr_xzz_dzmc2 = StringUtils.nvlString(_bo.get("SJR_XZZ_DZMC"));
				String sjr_lxdh2 = StringUtils.nvlString(_bo.get("SJR_LXDH"));
				String ddsj2 = StringUtils.nvlString(_bo.get("DDSJ"));
				if (org.apache.commons.lang.StringUtils.isNotBlank(sjr_xm) && org.apache.commons.lang.StringUtils.isNotBlank(sjr_xm2) && sjr_xm.equals(sjr_xm2)
						&& org.apache.commons.lang.StringUtils.isNotBlank(sjr_xzz_dzmc) && org.apache.commons.lang.StringUtils.isNotBlank(sjr_xzz_dzmc2) && sjr_xzz_dzmc.equals(sjr_xzz_dzmc2)
						&& org.apache.commons.lang.StringUtils.isNotBlank(sjr_lxdh) && org.apache.commons.lang.StringUtils.isNotBlank(sjr_lxdh2) && sjr_lxdh.equals(sjr_lxdh2)) {
					if (org.apache.commons.lang.StringUtils.isNotBlank(ddsj) && org.apache.commons.lang.StringUtils.isNotBlank(ddsj2) && isValidDate(ddsj) && isValidDate(ddsj2)) {
						double hourNum = Math.abs(simpleDateFormat.parse(ddsj).getTime() - simpleDateFormat.parse(ddsj2).getTime()) / (1000 * 60 * 60);
						if (hourNum <= 24) {
							// 置为重复
							_bo.set("SFCF_PDBZ", "1");
						}
					}
				}
			}
		}
		// 规则3:买家信息和交易信息中,有两条内容相同则可以认定该分组信息一致
		for (int i = 0; i < recordDatas.size(); i++) {
			BO bo = recordDatas.get(i);
			if ("1".equals(StringUtils.nvlString(bo.get("SFCF_PDBZ")))) {
				continue;
			}
			String sjr_xm = StringUtils.nvlString(bo.get("SJR_XM"));
			String sjr_xzz_dzmc = StringUtils.nvlString(bo.get("SJR_XZZ_DZMC"));
			String sjr_lxdh = StringUtils.nvlString(bo.get("SJR_LXDH"));
			String sjsj = StringUtils.nvlString(bo.get("SHSJ"));
			String jysj = StringUtils.nvlString(bo.get("DDSJ"));
			String sdsj = StringUtils.nvlString(bo.get("SDSJ"));
			for (int j = 0; j < recordDatas.size(); j++) {
				if (i == j) {
					continue;
				}
				BO _bo = recordDatas.get(j);
				if ("1".equals(StringUtils.nvlString(_bo.get("SFCF_PDBZ")))) {
					continue;
				}
				// 买家信息分组判断标志
				int mjxx = 0;
				// 交易信息分组判断标志
				int jyxx = 0;
				String sjr_xm2 = StringUtils.nvlString(_bo.get("SJR_XM"));
				String sjr_xzz_dzmc2 = StringUtils.nvlString(_bo.get("SJR_XZZ_DZMC"));
				String sjr_lxdh2 = StringUtils.nvlString(_bo.get("SJR_LXDH"));
				String sjsj2 = StringUtils.nvlString(_bo.get("SHSJ"));
				String jysj2 = StringUtils.nvlString(_bo.get("DDSJ"));
				String sdsj2 = StringUtils.nvlString(_bo.get("SDSJ"));
				if (org.apache.commons.lang.StringUtils.isNotBlank(sjr_xm) && org.apache.commons.lang.StringUtils.isNotBlank(sjr_xm2) && sjr_xm.equals(sjr_xm2)) {
					mjxx++;
				}
				if (org.apache.commons.lang.StringUtils.isNotBlank(sjr_xzz_dzmc) && org.apache.commons.lang.StringUtils.isNotBlank(sjr_xzz_dzmc2) && sjr_xzz_dzmc.equals(sjr_xzz_dzmc2)) {
					mjxx++;
				}
				if (org.apache.commons.lang.StringUtils.isNotBlank(sjr_lxdh) && org.apache.commons.lang.StringUtils.isNotBlank(sjr_lxdh2) && sjr_lxdh.equals(sjr_lxdh2)) {
					mjxx++;
				}
				if (mjxx >= 2) {
					if (org.apache.commons.lang.StringUtils.isNotBlank(sjsj) && org.apache.commons.lang.StringUtils.isNotBlank(sjsj2) && isValidDate(sjsj) && isValidDate(sjsj2)) {
						if (Math.abs(simpleDateFormat.parse(sjsj).getTime() - simpleDateFormat.parse(sjsj2).getTime()) / (1000 * 60 * 60) <= 24) {
							jyxx++;
						}
					}
					if (org.apache.commons.lang.StringUtils.isNotBlank(jysj) && org.apache.commons.lang.StringUtils.isNotBlank(jysj2) && isValidDate(jysj) && isValidDate(jysj2)) {
						if (Math.abs(simpleDateFormat.parse(jysj).getTime() - simpleDateFormat.parse(jysj2).getTime()) / (1000 * 60 * 60) <= 24) {
							jyxx++;
						}
					}
					if (org.apache.commons.lang.StringUtils.isNotBlank(sdsj) && org.apache.commons.lang.StringUtils.isNotBlank(sdsj2) && isValidDate(sdsj) && isValidDate(sdsj2)) {
						if (Math.abs(simpleDateFormat.parse(sdsj).getTime() - simpleDateFormat.parse(sdsj2).getTime()) / (1000 * 60 * 60) <= 24) {
							jyxx++;
						}
					}
					if (jyxx >= 2) {
						// 置为重复
						_bo.set("SFCF_PDBZ", "1");
					}
				}
			}
		}
		return recordDatas;
	}

	/**
	 * 更新关联物流数据
	 */
	public int updateRelevanceLogistics(UserContext userContext, Map<String, Object> paramsMap) throws Exception {
		Map<String, Object> wlParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_GLWL");
		int[] validateArr = { 0, 0 };
		// 判断是否有效
		if (wlParamsMap.containsKey("SJR_XZZ_DZMC")) {
			String value = StringUtils.nvlString(wlParamsMap.get("SJR_XZZ_DZMC"));
			if (org.apache.commons.lang.StringUtils.isNotBlank(value)) {
				validateArr[0] = 1;
			}
		}
		if (wlParamsMap.containsKey("SJR_LXDH")) {
			String value = StringUtils.nvlString(wlParamsMap.get("SJR_LXDH"));
			String regex = "^1(?:3\\d|4[4-9]|5[0-35-9]|6[67]|7[013-8]|8\\d|9\\d)\\d{8}$";
			boolean matches = value.matches(regex);
			if (org.apache.commons.lang.StringUtils.isNotBlank(value) && matches) {
				validateArr[1] = 1;
			}
		}
		wlParamsMap.put("SFYX_PDBZ", "0");
		if (validateArr[0] == 1 || validateArr[1] == 1) {
			wlParamsMap.put("SFYX_PDBZ", "1");
		}
		// 验证是否重复
		String pch = StringUtils.nvlString(wlParamsMap.get("PCH"));
		List<RowMap> gunDatas = DBSql.getMaps("select * from BO_EU_XSHB_GLWL where pch=?", new Object[] { pch });
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		wlParamsMap.put("SFCF_PDBZ", "0");

		String sjr_xm = StringUtils.nvlString(wlParamsMap.get("SJR_XM"));
		String sjr_xzz_dzmc = StringUtils.nvlString(wlParamsMap.get("SJR_XZZ_DZMC"));
		String sjr_lxdh = StringUtils.nvlString(wlParamsMap.get("SJR_LXDH"));
		String ddsj = StringUtils.nvlString(wlParamsMap.get("DDSJ"));
		String sjsj = StringUtils.nvlString(wlParamsMap.get("SHSJ"));
		String sdsj = StringUtils.nvlString(wlParamsMap.get("SDSJ"));

		for (int i = 0; i < gunDatas.size(); i++) {
			RowMap rowMap = gunDatas.get(i);
			String id = rowMap.getString("ID");
			if (id != null && id.equals(StringUtils.nvlString(wlParamsMap.get("ID")))) {
				continue;
			}
			String wldh = rowMap.getString("WLDH");
			// 规则1:快递单号相同,则直接判定为重复数据(excel数据比较)
			if (wldh != null && wldh.equals(StringUtils.nvlString(wlParamsMap.get("WLDH")))) {
				wlParamsMap.put("SFCF_PDBZ", "1");
				break;
			}
			// 规则2:如果快递单号为空或不同,则判定买家信息一致情况下且交易时间符合要求,则判定重复
			String sjr_xm2 = rowMap.getString("SJR_XM");
			String sjr_xzz_dzmc2 = rowMap.getString("SJR_XZZ_DZMC");
			String sjr_lxdh2 = rowMap.getString("SJR_LXDH");
			String ddsj2 = rowMap.getString("DDSJ");
			String sjsj2 = rowMap.getString("SHSJ");
			String sdsj2 = rowMap.getString("SDSJ");
			if (org.apache.commons.lang.StringUtils.isNotBlank(sjr_xm) && org.apache.commons.lang.StringUtils.isNotBlank(sjr_xm2) && sjr_xm.equals(sjr_xm2)
					&& org.apache.commons.lang.StringUtils.isNotBlank(sjr_xzz_dzmc) && org.apache.commons.lang.StringUtils.isNotBlank(sjr_xzz_dzmc2) && sjr_xzz_dzmc.equals(sjr_xzz_dzmc2)
					&& org.apache.commons.lang.StringUtils.isNotBlank(sjr_lxdh) && org.apache.commons.lang.StringUtils.isNotBlank(sjr_lxdh2) && sjr_lxdh.equals(sjr_lxdh2)) {
				if (org.apache.commons.lang.StringUtils.isNotBlank(ddsj) && org.apache.commons.lang.StringUtils.isNotBlank(ddsj2) && isValidDate(ddsj) && isValidDate(ddsj2)) {
					double hourNum = Math.abs(simpleDateFormat.parse(ddsj).getTime() - simpleDateFormat.parse(ddsj2).getTime()) / (1000 * 60 * 60);
					if (hourNum <= 24) {
						wlParamsMap.put("SFCF_PDBZ", "1");
						break;
					}
				}
			}
			// 规则3:买家信息和交易信息中,有两条内容相同则可以认定该分组信息一致
			// 买家信息分组判断标志
			int mjxx = 0;
			// 交易信息分组判断标志
			int jyxx = 0;
			if (org.apache.commons.lang.StringUtils.isNotBlank(sjr_xm) && org.apache.commons.lang.StringUtils.isNotBlank(sjr_xm2) && sjr_xm.equals(sjr_xm2)) {
				mjxx++;
			}
			if (org.apache.commons.lang.StringUtils.isNotBlank(sjr_xzz_dzmc) && org.apache.commons.lang.StringUtils.isNotBlank(sjr_xzz_dzmc2) && sjr_xzz_dzmc.equals(sjr_xzz_dzmc2)) {
				mjxx++;
			}
			if (org.apache.commons.lang.StringUtils.isNotBlank(sjr_lxdh) && org.apache.commons.lang.StringUtils.isNotBlank(sjr_lxdh2) && sjr_lxdh.equals(sjr_lxdh2)) {
				mjxx++;
			}
			if (mjxx >= 2) {
				if (org.apache.commons.lang.StringUtils.isNotBlank(sjsj) && org.apache.commons.lang.StringUtils.isNotBlank(sjsj2) && isValidDate(sjsj) && isValidDate(sjsj2)) {
					if (Math.abs(simpleDateFormat.parse(sjsj).getTime() - simpleDateFormat.parse(sjsj2).getTime()) / (1000 * 60 * 60) <= 24) {
						jyxx++;
					}
				}
				if (org.apache.commons.lang.StringUtils.isNotBlank(ddsj) && org.apache.commons.lang.StringUtils.isNotBlank(ddsj2) && isValidDate(ddsj) && isValidDate(ddsj2)) {
					if (Math.abs(simpleDateFormat.parse(ddsj).getTime() - simpleDateFormat.parse(ddsj2).getTime()) / (1000 * 60 * 60) <= 24) {
						jyxx++;
					}
				}
				if (org.apache.commons.lang.StringUtils.isNotBlank(sdsj) && org.apache.commons.lang.StringUtils.isNotBlank(sdsj2) && isValidDate(sdsj) && isValidDate(sdsj2)) {
					if (Math.abs(simpleDateFormat.parse(sdsj).getTime() - simpleDateFormat.parse(sdsj2).getTime()) / (1000 * 60 * 60) <= 24) {
						jyxx++;
					}
				}
				if (jyxx >= 2) {
					wlParamsMap.put("SFCF_PDBZ", "1");
					break;
				}
			}
		}
		// 规则1:数据库数据比较
		if (StringUtils.nvlString(wlParamsMap.get("SFCF_PDBZ")).equals("0")) {
			int n = DBSql.getInt("select count(1) from BO_EU_XSHB_GLWL where wldh=? and id !=?",
					new Object[] { StringUtils.nvlString(wlParamsMap.get("WLDH")), StringUtils.nvlString(wlParamsMap.get("ID")) });
			if (n > 0) {
				wlParamsMap.put("SFCF_PDBZ", "1");
			}
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));

		wlParamsMap.put("GXSJ", sdf.format(new Date()));
		String sql = SQLUtils.getCreateUpdateSQL(wlParamsMap, "BO_EU_XSHB_GLWL", new String[] { "ID" });
		System.out.println("更新>>>>>" + sql);
		return DBSql.update(sql);
	}

	/**
	 * 查询关联逃犯
	 * 
	 * @return
	 */
	public Map<String, Object> getClueRelevanceEscapee(UserContext userContext, Map<String, Object> paramsMap, int currPage, int pageSize, String sortField, String sortType) {
		String sql = ResourceUtils.getSQL("select_xsgltf") + SQLUtils.spellWhere(paramsMap);
		sql = "select t.* from (" + sql + ") t where t.row_number = 1";
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql), new Object[] {});
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortField) && org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql += " order by " + sortField + " " + sortType;
		} else {
			sql += " order by JYSJ desc";
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql, currPage, pageSize), new Object[] {});
		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("DJSJ", "DATETIME");
		columnConfigMap.put("JYSJ", "DATETIME");
		columnConfigMap.put("ZTRYLXDM", "CODE_ZTRYLX");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 新增关联逃犯
	 * 
	 * @return
	 */
	public int[] addRelevanceEscapee(UserContext userContext, Map<String, Object> paramsMap, Map<String, Object> returnMap) {
		List<Map<String, Object>> tfParamsList = (List<Map<String, Object>>) paramsMap.get("BO_EU_XSHB_GLTF");
		List<BO> recordDatas = Lists.newArrayList();
		List<String> idList = Lists.newArrayList();
		for (Map<String, Object> tfParamsMap : tfParamsList) {
			BO recordData = new BO();
			recordData.setAll(tfParamsMap);
			recordData.setAll(super.createUserMap4DB(userContext, new String[] { "XXDJ" }));
			recordDatas.add(recordData);
		}
		int[] insert = SDK.getBOAPI().createDataBO("BO_EU_XSHB_GLTF", recordDatas, userContext);
		for (BO recordData : recordDatas) {
			idList.add(recordData.getId());
		}
		returnMap.put("ID", idList);
		return insert;
	}

	/**
	 * 查询关联物流
	 * 
	 * @return
	 */
	public Map<String, Object> getClueRelevanceLogistics(UserContext userContext, Map<String, Object> paramsMap, int currPage, int pageSize, String sortField, String sortType) {
		if (paramsMap.containsKey("XSBH")) {
			paramsMap.remove("PCH");
		}
		String sql = ResourceUtils.getSQL("select_xsglwl") + SQLUtils.spellWhere(paramsMap);
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql), new Object[] {});
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortField) && org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql += " ORDER BY " + sortField + " " + sortType;
		} else {
			sql += " ORDER BY GXSJ DESC";
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql, currPage, pageSize), new Object[] {});
		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("DJSJ", "DATETIME");
		columnConfigMap.put("DDSJ", "DATETIME");
		columnConfigMap.put("SDSJ", "DATETIME");
		columnConfigMap.put("SHSJ", "DATETIME");
		columnConfigMap.put("SFYX_PDBZ", "CODE_IF");
		columnConfigMap.put("SFCF_PDBZ", "CODE_IF");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 删除关联逃犯
	 *
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	public int deleteRelevanceEscapee(UserContext userContext, Map<String, Object> paramsMap) {
		Map<String, Object> glywParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_GLTF");
		String sql = SQLUtils.getCreateDeleteSQL(glywParamsMap, "BO_EU_XSHB_GLTF");
		return DBSql.update(sql);
	}

	/**
	 * 查询逃犯轨迹
	 * 
	 * @return
	 */
	public Map<String, Object> getEscapeeTrail(UserContext userContext, Map<String, Object> paramsMap, int currPage, int pageSize, String sortField, String sortType) {
		String sql = ResourceUtils.getSQL("select_tfgjxx") + SQLUtils.spellWhere(paramsMap);
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql), new Object[] {});
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortField) && org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql += " ORDER BY " + sortField + " " + sortType;
		} else {
			sql += " ORDER BY DJSJ DESC";
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql, currPage, pageSize), new Object[] {});

		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("DJSJ", "DATETIME");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 删除关联物流数据
	 *
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	public int deleteRelevanceLogistics(UserContext userContext, Map<String, Object> paramsMap) {
		Map<String, Object> glywParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_GLWL");
		String sql = SQLUtils.getCreateDeleteSQL(glywParamsMap, "BO_EU_XSHB_GLWL");
		return DBSql.update(sql);
	}

	/**
	 * 获取续报任务中的关联业务
	 *
	 * @param userContext
	 * @return
	 */
	public Map<String, Object> getResubmitYw(UserContext userContext, String xbbh, String xslxdm) {
		Map<String, Object> resultMap = Maps.newHashMap();
		Map<String, Object> paramsMap = Maps.newHashMap();
		if (org.apache.commons.lang.StringUtils.isBlank(xslxdm)) {
			return resultMap;
		}
		if ("02".equals(xslxdm)) {
			paramsMap.put("XBBH", xbbh);
			String sql = "select * from BO_EU_XSHB_GLWL where 1=1 " + SQLUtils.spellWhere(paramsMap);
			RowMap rowMap = DBSql.getMap(sql);
			rowMap.remove("ID");
			rowMap.remove("ORGID");
			rowMap.remove("BINDID");
			rowMap.remove("PROCESSDEFID");
			rowMap.remove("XSBH");
			BO glwlBo = new BO();
			glwlBo.setAll(rowMap);
			glwlBo.setAll(super.createUserMap4DB(userContext, new String[] { "XXDJ" }));
			SDK.getBOAPI().createDataBO("BO_EU_XSHB_GLWL", glwlBo, userContext);
			resultMap.put("glwlID", glwlBo.getId());
			paramsMap.remove("XSBH");
			paramsMap.put("YWBH", xbbh);
		} else if ("01".equals(xslxdm)) {
			paramsMap.put("YWBH", xbbh);
		} else if ("03".equals(xslxdm)) {
			paramsMap.put("XBBH", xbbh);
			String sql = "select * from BO_EU_XSHB_GLTF where 1=1 " + SQLUtils.spellWhere(paramsMap);
			List<RowMap> list = DBSql.getMaps(sql);
			List<BO> dataBo = Lists.newArrayList();
			List<String> gltfIDs = Lists.newArrayList();
			for (RowMap rowMap : list) {
				rowMap.remove("ID");
				rowMap.remove("ORGID");
				rowMap.remove("BINDID");
				rowMap.remove("PROCESSDEFID");
				rowMap.remove("XSBH");
				BO gltfBo = new BO();
				gltfBo.setAll(rowMap);
				gltfBo.setAll(super.createUserMap4DB(userContext, new String[] { "XXDJ" }));
				dataBo.add(gltfBo);
			}
			SDK.getBOAPI().createDataBO("BO_EU_XSHB_GLTF", dataBo, userContext);
			for (BO bo : dataBo) {
				gltfIDs.add(bo.getId());
			}
			resultMap.put("gltfIDs", gltfIDs);
			paramsMap.remove("XSBH");
			paramsMap.put("YWBH", xbbh);
		}
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
			glywbo.set("GLYWBH", rowMap.getString("xbbh"));
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
		SDK.getBOAPI().createDataBO("BO_EU_XSHB_GLYW", dataList, userContext);
		for (BO bo : dataList) {
			glywIDs.add(bo.getId());
		}
		resultMap.put("glywIDs", glywIDs);
		dataList = Lists.newArrayList();
		paramsMap.remove("YWBH");
		paramsMap.put("GLYWBH", xbbh);
		sql = ResourceUtils.getSQL("select_fjxx") + SQLUtils.spellWhere(paramsMap);
		list = DBSql.getMaps(sql);
		for (RowMap rowMap : list) {
			String fjmc = rowMap.getString("FJMC");
			String fjdx = rowMap.getString("FJDX");
			String fjlj = rowMap.getString("FJLJ");
			String fjlx = rowMap.getString("FJLX");
			if (org.apache.commons.lang.StringUtils.isBlank(fjlj)) {
				continue;
			}
			BO fjbo = new BO();
			fjbo.set("FJMC", fjmc);
			fjbo.set("FJDX", fjdx);
			fjbo.set("FJLJ", fjlj);
			fjbo.set("FJLX", fjlx);
			fjbo.setAll(super.createUserMap4DB(userContext, new String[] { "XXDJ" }));
			dataList.add(fjbo);
		}
		SDK.getBOAPI().createDataBO("BO_EU_XSHB_FJ", dataList, userContext);
		for (BO bo : dataList) {
			fjIDs.add(bo.getId());
		}
		resultMap.put("fjIDs", fjIDs);
		return resultMap;
	}

	/**
	 * 线索复制
	 *
	 * @param userContext
	 * @return
	 */
	public Map<String, Object> copyClue(UserContext userContext, String xsbh, String xslxdm) {
		Map<String, Object> resultMap = Maps.newHashMap();
		Map<String, Object> paramsMap = Maps.newHashMap();
		if (org.apache.commons.lang.StringUtils.isBlank(xslxdm)) {
			return resultMap;
		}
		if ("02".equals(xslxdm)) {
			paramsMap.put("XSBH", xsbh);
			String sql = "select * from BO_EU_XSHB_GLWL where 1=1 " + SQLUtils.spellWhere(paramsMap);
			RowMap rowMap = DBSql.getMap(sql);
			rowMap.remove("ID");
			rowMap.remove("ORGID");
			rowMap.remove("BINDID");
			rowMap.remove("PROCESSDEFID");
			rowMap.remove("XSBH");
			BO glwlBo = new BO();
			glwlBo.setAll(rowMap);
			glwlBo.setAll(super.createUserMap4DB(userContext, new String[] { "XXDJ" }));
			SDK.getBOAPI().createDataBO("BO_EU_XSHB_GLWL", glwlBo, userContext);
			resultMap.put("glwlID", glwlBo.getId());
			paramsMap.remove("XSBH");
			paramsMap.put("YWBH", xsbh);
		} else if ("01".equals(xslxdm) || "0101".equals(xslxdm) || "0102".equals(xslxdm) || "0103".equals(xslxdm) || "04".equals(xslxdm) || "0403".equals(xslxdm) || "0402".equals(xslxdm)
				|| "0401".equals(xslxdm)) {
			paramsMap.put("YWBH", xsbh);
		} else if ("03".equals(xslxdm)) {
			paramsMap.put("XSBH", xsbh);
			String sql = "select * from BO_EU_XSHB_GLTF where 1=1 " + SQLUtils.spellWhere(paramsMap);
			List<RowMap> list = DBSql.getMaps(sql);
			List<BO> dataBo = Lists.newArrayList();
			List<String> gltfIDs = Lists.newArrayList();
			for (RowMap rowMap : list) {
				rowMap.remove("ID");
				rowMap.remove("ORGID");
				rowMap.remove("BINDID");
				rowMap.remove("PROCESSDEFID");
				rowMap.remove("XSBH");
				BO gltfBo = new BO();
				gltfBo.setAll(rowMap);
				gltfBo.setAll(super.createUserMap4DB(userContext, new String[] { "XXDJ" }));
				dataBo.add(gltfBo);
			}
			SDK.getBOAPI().createDataBO("BO_EU_XSHB_GLTF", dataBo, userContext);
			for (BO bo : dataBo) {
				gltfIDs.add(bo.getId());
			}
			resultMap.put("gltfIDs", gltfIDs);
			paramsMap.remove("XSBH");
			paramsMap.put("YWBH", xsbh);
		}
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
		SDK.getBOAPI().createDataBO("BO_EU_XSHB_GLYW", dataList, userContext);
		for (BO bo : dataList) {
			glywIDs.add(bo.getId());
		}
		resultMap.put("glywIDs", glywIDs);
		dataList = Lists.newArrayList();
		paramsMap.remove("YWBH");
		paramsMap.put("GLYWBH", xsbh);
		sql = ResourceUtils.getSQL("select_fjxx") + SQLUtils.spellWhere(paramsMap);
		list = DBSql.getMaps(sql);
		for (RowMap rowMap : list) {
			String fjmc = rowMap.getString("FJMC");
			String fjdx = rowMap.getString("FJDX");
			String fjlj = rowMap.getString("FJLJ");
			String fjlx = rowMap.getString("FJLX");
			if (org.apache.commons.lang.StringUtils.isBlank(fjlj)) {
				continue;
			}
			BO fjbo = new BO();
			fjbo.set("FJMC", fjmc);
			fjbo.set("FJDX", fjdx);
			fjbo.set("FJLJ", fjlj);
			fjbo.set("FJLX", fjlx);
			fjbo.setAll(super.createUserMap4DB(userContext, new String[] { "XXDJ" }));
			dataList.add(fjbo);
		}
		SDK.getBOAPI().createDataBO("BO_EU_XSHB_FJ", dataList, userContext);
		for (BO bo : dataList) {
			fjIDs.add(bo.getId());
		}
		resultMap.put("fjIDs", fjIDs);
		return resultMap;
	}

	/**
	 * 任务复制
	 * 
	 * @param userContext
	 * @param parmasMap
	 * @return
	 */
	public Map<String, Object> copyTask(UserContext userContext, Map<String, Object> parmasMap) {
		Map<String, Object> resultMap = Maps.newHashMap();
		// 线索保存成功进行上报/下发的跳转时做关联复制
		if (parmasMap.containsKey("XSBH")) {
			// 复制任务关联线索
			String xsbh = StringUtils.nvlString(parmasMap.get("XSBH"));
			BO rwglxs = new BO();
			rwglxs.set("XSBH", xsbh);
			rwglxs.setAll(super.createUserMap4DB(userContext, new String[] { "XXDJ" }));
			SDK.getBOAPI().createDataBO("BO_EU_XSHB_RW_GLXS", rwglxs, userContext);
			resultMap.put("GLXSID", rwglxs.getId());
		} else if (parmasMap.containsKey("RWBH")) {
			// 复制附件,任务关联线索
			String rwbh = StringUtils.nvlString(parmasMap.get("RWBH"));
			Map<String, Object> paramsMap = Maps.newHashMap();
			List<BO> dataList = Lists.newArrayList();
			List<String> rwglxsIds = Lists.newArrayList();
			List<String> fjIDs = Lists.newArrayList();
			paramsMap.put("RWBH", rwbh);
			String sql = ResourceUtils.getSQL("select_rwglxs") + SQLUtils.spellWhere(paramsMap);
			List<RowMap> list = DBSql.getMaps(sql);
			for (RowMap r : list) {
				String xsbh = r.getString("XSBH");
				BO bo = new BO();
				bo.set("XSBH", xsbh);
				bo.setAll(super.createUserMap4DB(userContext, new String[] { "XXDJ" }));
				dataList.add(bo);
			}
			if (dataList != null && dataList.size() > 0) {
				SDK.getBOAPI().createDataBO("BO_EU_XSHB_RW_GLXS", dataList, userContext);
			}
			for (BO bo : dataList) {
				rwglxsIds.add(bo.getId());
			}
			resultMap.put("GLXSID", rwglxsIds);
			dataList = Lists.newArrayList();
			paramsMap.remove("RWBH");
			paramsMap.put("GLYWBH", rwbh);
			sql = ResourceUtils.getSQL("select_fjxx") + SQLUtils.spellWhere(paramsMap);
			list = DBSql.getMaps(sql);
			for (RowMap rowMap : list) {
				String fjmc = rowMap.getString("FJMC");
				String fjdx = rowMap.getString("FJDX");
				String fjlj = rowMap.getString("FJLJ");
				String fjlx = rowMap.getString("FJLX");
				if (org.apache.commons.lang.StringUtils.isBlank(fjlj)) {
					continue;
				}
				BO fjbo = new BO();
				fjbo.set("FJMC", fjmc);
				fjbo.set("FJDX", fjdx);
				fjbo.set("FJLJ", fjlj);
				fjbo.set("FJLX", fjlx);
				fjbo.setAll(super.createUserMap4DB(userContext, new String[] { "XXDJ" }));
				dataList.add(fjbo);
			}
			SDK.getBOAPI().createDataBO("BO_EU_XSHB_FJ", dataList, userContext);
			for (BO bo : dataList) {
				fjIDs.add(bo.getId());
			}
			resultMap.put("fjIDs", fjIDs);

		}
		return resultMap;
	}

	/**
	 * 案件形成线索
	 * 
	 * 新建线索BO 返回线索编号 创建关联业务数据 案事件编号
	 * 
	 * @param context
	 * @return
	 */
	public String addClue(UserContext context) {
		String xsbh = SequenceUtils.getSequenceVal("SEQ_BO_EU_XSHB_XS", "X", context.getUserModel().getDepartmentId(), "yyyymm", 23);
		BO clue = new BO();
		clue.set("XSBH", xsbh);
		clue.setAll(super.createUserMap4DB(context, new String[] { "XXDJ", "XXCZ" }));
		SDK.getBOAPI().createDataBO("BO_EU_XSHB_XS", clue, context);
		return xsbh;
	}

	/**
	 * 线索关联案事件
	 * 
	 * @param ywbh
	 *            eg:线索编号
	 * @param glywbh
	 *            eg:案事件编号
	 * @param context
	 */
	public void addGlyw(String ywbh, String glywbh, UserContext context) {
		BO glyw = new BO();
		glyw.set("YWBH", ywbh);
		glyw.set("GLYWBH", glywbh);
		glyw.set("GLYWLXDM", "02");
		glyw.setAll(super.createUserMap4DB(context, new String[] { "XXDJ" }));
		SDK.getBOAPI().createDataBO("BO_EU_XSHB_GLYW", glyw, context);
	}

	/**
	 * 撤销任务
	 * 
	 * @param context
	 * @param rwbh
	 *            任务编号
	 * @param rwlx
	 *            任务类型 （上报、下发）
	 */
	public int[] revokeProcess(UserContext context, String rwbh, String rwlx) {
		Map<String, Object> paramsMap = Maps.newHashMap();
		int[] update = new int[2];
		paramsMap.put("RWBH", rwbh);
		paramsMap.put("RWZTDM", "11");
		String rwlzID = DBSql.getString(ResourceUtils.getSQL("select_rwlzbh"), new String[] { rwbh });
		BO rwlzBo = SDK.getBOAPI().get("BO_EU_XSHB_RW_LZ", rwlzID);
		if (StringUtils.equals("issue", rwlx)) {
			String sql = SQLUtils.getCreateUpdateSQL(paramsMap, "BO_EU_XSHB_RW_XF", new String[] { "RWBH" });
			update[0] = DBSql.update(sql);
			update[1] = DBSql.update("update BO_EU_XSHB_RW_LZ set XXSC_PDBZ = '1' where rwlzbh=?", new String[] { rwlzBo.getString("RWLZBH") });
			SDK.getProcessAPI().cancelById(rwlzBo.getString("BINDID"), context.getUID());
		} else if (StringUtils.equals("report", rwlx)) {
			String sql = SQLUtils.getCreateUpdateSQL(paramsMap, "BO_EU_XSHB_RW_SB", new String[] { "RWBH" });
			update[0] = DBSql.update(sql);
			update[1] = DBSql.update("update BO_EU_XSHB_RW_LZ set XXSC_PDBZ = '1' where rwlzbh=?", new String[] { rwlzBo.getString("rwlzbh") });
			SDK.getProcessAPI().cancelById(rwlzBo.getString("BINDID"), context.getUID());
		}
		return update;
	}

	/**
	 * 更新任务
	 * 
	 * @param userContext
	 * @param paramsMap
	 *            传输数据
	 * @param connection
	 *            连接
	 * @return
	 */
	public int[] updateRw(UserContext userContext, Map<String, Object> paramsMap, Connection connection, Map<String, Object> returnMap) {
		int[] update = new int[4];
		// TODO 附件、任务的基本信息、关联业务、
		// 附件更新,先删在增
		String table, cz;
		if (paramsMap.containsKey("BO_EU_XSHB_RW_XF")) {
			table = "BO_EU_XSHB_RW_XF";
			cz = "XF";
		} else {
			table = "BO_EU_XSHB_RW_SB";
			cz = "SB";
		}
		Map<String, Object> rwParamsMap = (Map<String, Object>) paramsMap.get(table);
		String rwbh = StringUtils.nvlString(rwParamsMap.get("RWBH"));
		Map<String, Object> fjParams = Maps.newHashMap();
		Map<String, Object> fjParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_FJ");
		fjParams.put("GLYWBH", rwbh);
		update[0] = DBSql.update(connection, SQLUtils.getCreateDeleteSQL(fjParams, "BO_EU_XSHB_FJ"));
		if (fjParamsMap != null && !fjParamsMap.isEmpty()) {
			String[] paths = StringUtils.nvlString(fjParamsMap.get("FJLJ")).split(",");
			commonService.uploadAttachment(userContext, paths, fjParams, connection);
		}
		// 任务基本信息更新
		rwParamsMap.putAll(super.createUserMap4DB(userContext, new String[] { cz }));
		rwParamsMap.put("RWZTDM", "01");
		returnMap.put("RWBH", rwbh);
		update[1] = DBSql.update(connection, SQLUtils.getCreateUpdateSQL(rwParamsMap, table, new String[] { "RWBH" }));

		// 关联业务更新 先删后增
		Map<String, Object> glywParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_GLYW");
		Map<String, Object> glywParams = Maps.newHashMap();
		glywParams.put("YWBH", rwbh);
		DBSql.update(connection, SQLUtils.getCreateDeleteSQL(glywParams, "BO_EU_XSHB_GLYW"));
		if (glywParamsMap != null && glywParamsMap.containsKey("ID") && StringUtils.isNotEmpty(StringUtils.nvlString(glywParamsMap.get("ID")))) {
			List<String> glywIdList = Arrays.asList(StringUtils.nvlString(glywParamsMap.get("ID")).split(","));
			System.out.println(glywIdList.toString());
			String sql = "update BO_EU_XSHB_GLYW set ywbh=? where id in('" + org.apache.commons.lang.StringUtils.join(glywIdList, "','") + "')";
			update[2] = DBSql.update(connection, sql, new String[] { rwbh });
		} else {
			update[2] = 1;
		}
		// 新增任务流转表
		List<Map<String, Object>> lzList = (List<Map<String, Object>>) paramsMap.get("BO_EU_XSHB_RW_LZ");
		for (int i = 0; i < lzList.size(); i++) {
			Map<String, Object> lzParamsMap = lzList.get(i);
			String jsdxlxdm = StringUtils.nvlString(lzParamsMap.get("JSDXLXDM"));
			String jsdm_gajgjgdm = StringUtils.nvlString(lzParamsMap.get("JSDW_GAJGJGDM"));
			String user = null;
			if (jsdxlxdm.equals("02")) {
				List<RowMap> userList = DBSql.getMaps(ResourceUtils.getSQL("select_user"), new Object[] { jsdm_gajgjgdm });
				List<String> list = Lists.newArrayList();
				for (RowMap rowMap : userList) {
					String userid = rowMap.getString("USERID");
					list.add(userid);
				}
				user = org.apache.commons.lang.StringUtils.join(list, " ");
			} else if (jsdxlxdm.equals("01")) {
				user = StringUtils.nvlString(lzParamsMap.get("JSRY_GMSFHM"));
			}
			BO lzBO = new BO();
			lzBO.setAll(lzParamsMap);
			lzBO.set("RWBH", rwbh);
			lzBO.set("RWLXDM", "02");
			lzBO.set("LZZTDM", "01");
			lzBO.set("RWLZBH", SequenceUtils.getSequenceVal("SEQ_BO_EU_XSHB_RW_LZ", "", userContext.getUserModel().getDepartmentId(), "yyyymmdd", 30));

			if ("02".equals((StringUtils.nvlString(lzParamsMap.get("JSDXLXDM"))))) {
				lzBO.set("WDRY", commonService.getDepartmentUser(userContext, StringUtils.nvlString(lzParamsMap.get("JSDW_GAJGJGDM"))));
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
		update[3] = 1;
		return update;
	}

}
