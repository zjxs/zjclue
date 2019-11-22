package com.siyue.service;

import java.io.File;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.curator.shaded.com.google.common.collect.Lists;
import org.apache.curator.shaded.com.google.common.collect.Maps;

import com.actionsoft.apps.resource.plugin.profile.DCPluginProfile;
import com.actionsoft.bpms.bo.engine.BO;
import com.actionsoft.bpms.commons.database.RowMap;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.server.fs.DCContext;
import com.actionsoft.bpms.server.fs.dc.DCProfileManager;
import com.actionsoft.bpms.server.fs.dc.DCUtil;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.siyue.util.HttpClientUtils;
import com.siyue.util.ResourceUtils;
import com.siyue.util.SQLUtils;
import com.siyue.util.StringUtils;

public class CommonService extends BaseService {

	private final static String OS_NAME = System.getProperties().getProperty("os.name");

	/**
	 * 查询关联附件 ww
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	public List<RowMap> getRelevanceAttachment(UserContext userContext, Map<String, Object> paramsMap) {
		String sql = ResourceUtils.getSQL("select_fjxx") + SQLUtils.spellWhere(paramsMap);
		List<RowMap> rows = DBSql.getMaps(sql, new Object[] {});
		for (int i = 0; i < rows.size(); i++) {
			if (StringUtils.isEmpty(StringUtils.nvlString(rows.get(i).get("FJLJ")))) {
				rows.remove(i);
			}
		}
		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("DJSJ", "DATETIME");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		return rows;
	}

	/**
	 * 附件上传
	 * 
	 * @param userContext
	 * @param paths
	 * @param paramsMap
	 * @return
	 */
	public int[] uploadAttachment(UserContext userContext, String[] paths, Map<String, Object> paramsMap,
			Connection connection) {
		List<BO> listData = Lists.newArrayList();
		for (String path : paths) {
			File file = new File(path);
			long size = file.length();
			String fileName = file.getName();
			String fileType = FilenameUtils.getExtension(fileName);
			BO bo = new BO();
			bo.setAll(super.createUserMap4DB(userContext, new String[] { "XXDJ" }));
			bo.set("GLYWBH", StringUtils.nvlString(paramsMap.get("GLYWBH")));
			if (paramsMap.containsKey("XBBH")) {
				bo.set("XBBH", StringUtils.nvlString(paramsMap.get("XBBH")));
			}
			if (paramsMap.containsKey("XXZJBH")) {
				bo.set("XXZJBH", StringUtils.nvlString(paramsMap.get("XXZJBH")));
			}
			if (paramsMap.containsKey("TYPE")) {
				bo.set("TYPE", StringUtils.nvlString(paramsMap.get("TYPE")));
			}
			bo.set("FJMC", fileName);
			bo.set("FJDX", String.valueOf(size));
			bo.set("FJLX", fileType);
			bo.set("FJLJ", path.replaceAll("\\\\", "/"));
			listData.add(bo);
		}
		return SDK.getBOAPI().createDataBO("BO_EU_XSHB_FJ", listData, userContext);
	}

	/**
	 * 删除附件(id)
	 * 
	 * @param paramsMap
	 */
	public void deleteAttachment(Map<String, Object> paramsMap) {
		RowMap map = DBSql.getMap(ResourceUtils.getSQL("select_fjxx") + SQLUtils.spellWhere(paramsMap));
		File file = new File(StringUtils.nvlString(map.get("FJLJ")));
		int i = DBSql.update(SQLUtils.getCreateDeleteSQL(paramsMap, "BO_EU_XSHB_FJ"));
		if (i == 1 && file.exists()) {
			file.delete();
		}
	}

	/**
	 * 获取附件URL
	 * 
	 * @param userContext
	 * @param paramsMap
	 *            包括{GLBH: "",FJMC: ""}
	 * @return
	 */
	public String getAttachmentDownloadURL(UserContext userContext, Map<String, Object> paramsMap) {
		RowMap rowMap = DBSql.getMap(ResourceUtils.getSQL("select_fjxx") + SQLUtils.spellWhere(paramsMap));
		String fileName = StringUtils.nvlString(paramsMap.get("FJMC"));
		String[] pathArray = rowMap.getString("FJLJ").split("/");
		String fileValue = pathArray[pathArray.length - 2];
		String groupValue = pathArray[pathArray.length - 3];
		String repositoryValue = pathArray[pathArray.length - 4];
		DCContext dcContext = new DCContext(userContext,
				DCProfileManager.getDCProfile("com.awspaas.user.apps.clue", repositoryValue),
				"com.awspaas.user.apps.clue", groupValue, fileValue, fileName);
		return dcContext.getDownloadURL();
	}

	/**
	 * 获取附件下载URL（ZIP）
	 * 
	 * @param userContext
	 * @param paramsMap
	 *            包括{ID:多值逗号分隔}
	 * @return
	 */
	public String batchAttachmentDownloadURL(UserContext userContext, Map<String, Object> paramsMap) {
		List<RowMap> rows = DBSql.getMaps(ResourceUtils.getSQL("select_fjxx") + SQLUtils.spellWhere(paramsMap));
		String regex = OS_NAME.equalsIgnoreCase("linux") ? "/" : "\\\\";
		List<DCContext> dcList = Lists.newArrayList();
		for (RowMap row : rows) {
			String fileName = row.getString("FJMC");
			String filePath = row.getString("FJLJ");
			String[] pathArray = filePath.split(regex);

			String fileValue = pathArray[pathArray.length - 2];
			String groupValue = pathArray[pathArray.length - 3];
			String repositoryValue = pathArray[pathArray.length - 4];
			DCContext dcContext = new DCContext(userContext,
					DCProfileManager.getDCProfile("com.awspaas.user.apps.clue", repositoryValue),
					"com.awspaas.user.apps.clue", groupValue, fileValue, fileName);
			dcList.add(dcContext);
		}
		String url = "";
		try {
			long times = System.currentTimeMillis();
			DCPluginProfile dcProfile = DCProfileManager.getDCProfile("_bpm.platform", "tmp");
			String zipName = times + ".zip";
			DCContext zipDcContext = new DCContext(userContext, dcProfile, "_bpm.platform", "groupPackage",
					"zip" + times, zipName);
			String targetDir = zipDcContext.getPath();
			return DCUtil.downloadPackage(userContext, dcList, targetDir, zipName, "tmp").getMsg();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return url;
	}

	/**
	 * 添加串并业务
	 *
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	public int[] addSeriesBiz(UserContext userContext, Map<String, Object> paramsMap, Map<String, Object> returnMap) {
		List<Map<String, Object>> cbParamsList = (List<Map<String, Object>>) paramsMap.get("BO_EU_XSHB_GLYW");
		List<BO> recordDatas = Lists.newArrayList();
		List<String> idList = Lists.newArrayList();
		for (Map<String, Object> bizParamsMap : cbParamsList) {
			BO recordData = new BO();
			recordData.setAll(bizParamsMap);
			recordData.setAll(super.createUserMap4DB(userContext, new String[] { "XXDJ" }));
			recordDatas.add(recordData);
		}
		int[] insert = SDK.getBOAPI().createDataBO("BO_EU_XSHB_GLYW", recordDatas, userContext);
		for (BO recordData : recordDatas) {
			idList.add(recordData.getId());
		}
		returnMap.put("ID", idList);
		return insert;
	}

	/**
	 * 删除串并业务
	 *
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	public int deleteSeriesBiz(UserContext userContext, Map<String, Object> paramsMap) {
		Map<String, Object> glywParamsMap = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_GLYW");
		String sql = SQLUtils.getCreateDeleteSQL(glywParamsMap, "BO_EU_XSHB_GLYW");
		return DBSql.update(sql);
	}

	/**
	 * 已串并案件
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	public Map<String, Object> getSeriesCase(UserContext userContext, Map<String, Object> paramsMap, int currPage,
			int pageSize, String sortFiled, String sortType) {
		StringBuffer sql = new StringBuffer("");
		if (paramsMap.containsKey("YWBH")) {
			String ywbh = StringUtils.nvlString(paramsMap.remove("YWBH"));
			sql.append(ResourceUtils.getSQL("select_ycbasj"))
					.append(" AND YWBH in (select ysxsbh ywbh from bo_eu_xshb_xs where xsbh='" + ywbh
							+ "' and ysxsbh != '' and ysxsbh is not null union select xsbh ywbh from bo_eu_xshb_xs where ysxsbh='"
							+ ywbh + "' union select xsbh ywbh from bo_eu_xshb_xs where xsbh='" + ywbh + "') ");
		} else {
			sql.append(ResourceUtils.getSQL("select_ycbasj") + SQLUtils.spellWhere(paramsMap));
		}
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql.toString()), new Object[] {});
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortFiled)
				&& org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql.append(" ORDER BY " + sortFiled + " " + sortType);
			// sql += " ORDER BY " + sortFiled + " " + sortType;
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql.toString(), currPage, pageSize),
				new Object[] {});

		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("AJLBDM", "CODE_AJLB");
		columnConfigMap.put("SLSJ", "DATETIME");
		columnConfigMap.put("ZATZ_JYQK", "CODE_AJLB_XL");
		columnConfigMap.put("LARQ", "DATETIME");
		columnConfigMap.put("ASJFSSJFX_ASJFSKSSJ", "DATETIME");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 已关联串并信息
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	public Map<String, Object> getSeriesCb(UserContext userContext, Map<String, Object> paramsMap, int currPage,
			int pageSize, String sortFiled, String sortType) {
		String sql = ResourceUtils.getSQL("select_ycbcb") + SQLUtils.spellWhere(paramsMap);
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql), new Object[] {});
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortFiled)
				&& org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql += " ORDER BY " + sortFiled + " " + sortType;
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql, currPage, pageSize), new Object[] {});

		Map<String, String> columnConfigMap = Maps.newHashMap();
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 已串并人员
	 *
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	public Map<String, Object> getSeriesPerson(UserContext userContext, Map<String, Object> paramsMap, int currPage,
			int pageSize, String sortFiled, String sortType) {
		StringBuffer sql = new StringBuffer("");
		if (paramsMap.containsKey("YWBH")) {
			String ywbh = StringUtils.nvlString(paramsMap.remove("YWBH"));
			sql.append(ResourceUtils.getSQL("select_ycbry"))
					.append(" AND YWBH in (select ysxsbh ywbh from bo_eu_xshb_xs where xsbh='" + ywbh
							+ "' and ysxsbh != '' and ysxsbh is not null union select xsbh ywbh from bo_eu_xshb_xs where ysxsbh='"
							+ ywbh + "' union select xsbh ywbh from bo_eu_xshb_xs where xsbh='" + ywbh + "') ");
		} else {
			sql.append(ResourceUtils.getSQL("select_ycbry") + SQLUtils.spellWhere(paramsMap));
		}
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql.toString()), new Object[] {});
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortFiled)
				&& org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql.append(" ORDER BY " + sortFiled + " " + sortType);
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql.toString(), currPage, pageSize),
				new Object[] {});

		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("DJSJ", "DATETIME");
		columnConfigMap.put("HJDZ_XZQHDM", "CODE_XZQH");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 已串并线索
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @param currPage
	 * @param pageSize
	 * @param sortField
	 * @param sortType
	 * @return
	 */
	public Map<String, Object> getSeriesClue(UserContext userContext, Map<String, Object> paramsMap, int currPage,
			int pageSize, String sortField, String sortType) {
		StringBuffer sql = new StringBuffer("");
		if (paramsMap.containsKey("YWBH")) {
			String ywbh = StringUtils.nvlString(paramsMap.remove("YWBH"));
			sql.append(ResourceUtils.getSQL("select_ycbxs"))
					.append(" AND YWBH in (select ysxsbh ywbh from bo_eu_xshb_xs where xsbh='" + ywbh
							+ "' and ysxsbh != '' and ysxsbh is not null union select xsbh ywbh from bo_eu_xshb_xs where ysxsbh='"
							+ ywbh + "' union select xsbh ywbh from bo_eu_xshb_xs where xsbh='" + ywbh + "') ");
		} else {
			sql.append(ResourceUtils.getSQL("select_ycbxs") + SQLUtils.spellWhere(paramsMap));
		}
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql.toString()), new Object[] {});
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortField)
				&& org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql.append(" ORDER BY " + sortField + " " + sortType);
			// sql += " ORDER BY " + sortField + " " + sortType;
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql.toString(), currPage, pageSize),
				new Object[] {});

		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("DJSJ", "DATETIME");
		columnConfigMap.put("XSLXDM", "CODE_XSHB_XSLX");
		columnConfigMap.put("XSJBDM", "CODE_XSHB_JB");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 已串并物品
	 *
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	public Map<String, Object> getSeriesGoods(UserContext userContext, Map<String, Object> paramsMap, int currPage,
			int pageSize, String sortFiled, String sortType) {
		StringBuffer sql = new StringBuffer("");
		if (paramsMap.containsKey("YWBH")) {
			String ywbh = StringUtils.nvlString(paramsMap.remove("YWBH"));
			sql.append(ResourceUtils.getSQL("select_ycbwp"))
					.append(" AND YWBH in (select ysxsbh ywbh from bo_eu_xshb_xs where xsbh='" + ywbh
							+ "' and ysxsbh != '' and ysxsbh is not null union select xsbh ywbh from bo_eu_xshb_xs where ysxsbh='"
							+ ywbh + "' union select xsbh ywbh from bo_eu_xshb_xs where xsbh='" + ywbh + "') ");
		} else {
			sql.append(ResourceUtils.getSQL("select_ycbwp") + SQLUtils.spellWhere(paramsMap));
		}
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql.toString()), new Object[] {});
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortFiled)
				&& org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql.append(" ORDER BY " + sortFiled + " " + sortType);
			// sql += " ORDER BY " + sortFiled + " " + sortType;
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql.toString(), currPage, pageSize),
				new Object[] {});

		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("XXDJDW_GAJGJGDM", "CODE_GXS");
		columnConfigMap.put("DJSJ", "DATETIME");
		columnConfigMap.put("SAWP_SAWPDM", "CODE_SAWP");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 查询下级接收单位
	 */
	public List<RowMap> getSubReceiveUnit(UserContext userContext) {
		Map<String, Object> paramsMap = Maps.newHashMap();
		String departmentId = userContext.getDepartmentModel().getId();
		int lev = userContext.getDepartmentModel().getLayer();
		paramsMap.put("CODE_LEV" + lev, departmentId);
		paramsMap.put("LEV", lev + 1);
		String sql = ResourceUtils.getSQL("select_dwxx") + SQLUtils.spellWhere(paramsMap);
		// if (StringUtils.isNotBlank(search)) {
		// sql += " and (CODE like '%" + search + "%' or NAME like '%" + search + "%')";
		// }
		sql += " order by CODE";
		List<RowMap> rows = DBSql.getMaps(sql);

		return rows;
	}

	/**
	 * 根据单位ID查询单位用户
	 */
	public List<RowMap> getDepartmentUser(UserContext userContext, String gxs) {
		String sql = ResourceUtils.getSQL("select_user");
		// if (StringUtils.isNotBlank(search)) {
		// sql += " and( a.USERNAME like '%" + search + "%' or a.USERID like '%" +
		// search + "%' )";
		// }
		List<RowMap> rows = DBSql.getMaps(sql, new Object[] { gxs });

		return rows;
	}

	/**
	 * 查询权限ID
	 */
	public String getRoleID(String rolename) {
		return DBSql.getString(ResourceUtils.getSQL("select_qxid"), new String[] { rolename });
	}

	/**
	 * 已关联线索
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @param currPage
	 * @param pageSize
	 * @param sortField
	 * @param sortType
	 * @return
	 */
	public Map<String, Object> getRelevanceClue(UserContext userContext, Map<String, Object> paramsMap, int currPage,
			int pageSize, String sortField, String sortType) {
		String rwbh = StringUtils.nvlString(paramsMap.remove("RWBH"));
		String sql = ResourceUtils.getSQL("select_yglxs") + SQLUtils.spellWhere(paramsMap);
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql), new Object[] { rwbh });
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortField)
				&& org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql += " ORDER BY " + sortField + " " + sortType;
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql, currPage, pageSize), new Object[] { rwbh });

		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("DJSJ", "DATETIME");
		columnConfigMap.put("XSLXDM", "CODE_XSHB_XSLX");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 查询外部平台任务关联的案件
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @param currPage
	 * @param pageSize
	 * @param sortField
	 * @param sortType
	 * @return
	 */
	public Map<String, Object> getRelevanceAJByOther(UserContext userContext, Map<String, Object> paramsMap,
			int currPage, int pageSize, String sortField, String sortType) {
		String rwbh = StringUtils.nvlString(paramsMap.remove("RWBH"));
		String sql = ResourceUtils.getSQL("select_yglAjByOther") + SQLUtils.spellWhere(paramsMap);
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql), new Object[] { rwbh });
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortField)
				&& org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql += " ORDER BY " + sortField + " " + sortType;
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql, currPage, pageSize), new Object[] { rwbh });

		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("DJSJ", "DATETIME");
		columnConfigMap.put("AJLBDM", "CODE_AJLB");
		columnConfigMap.put("SLSJ", "DATETIME");
		columnConfigMap.put("ZATZ_JYQK", "CODE_AJLB_XL");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 查询外部平台任务关联的串并信息
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @param currPage
	 * @param pageSize
	 * @param sortField
	 * @param sortType
	 * @return
	 */
	public Map<String, Object> getRelevanceCBByOther(UserContext userContext, Map<String, Object> paramsMap,
			int currPage, int pageSize, String sortField, String sortType) {
		String rwbh = StringUtils.nvlString(paramsMap.remove("RWBH"));
		String sql = ResourceUtils.getSQL("select_yglCbByOther") + SQLUtils.spellWhere(paramsMap);
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql), new Object[] { rwbh });
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortField)
				&& org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql += " ORDER BY " + sortField + " " + sortType;
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql, currPage, pageSize), new Object[] { rwbh });

		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("DJSJ", "DATETIME");
		columnConfigMap.put("XSLXDM", "CODE_XSHB_XSLX");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 查询全部案件
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	public Map<String, Object> getAllCase(UserContext userContext, Map<String, Object> paramsMap, int currPage,
			int pageSize, String sortField, String sortType) {
		Map<String, String> replaceMap = Maps.newHashMap();
		replaceMap.put("ID", SQLUtils.spellWhere(ImmutableMap.of("ID", StringUtils.nvlString(paramsMap.remove("ID")))));
		String sql = ResourceUtils.getSQL("select_asj");
		sql = StringUtils.replaceAllVariable(sql, replaceMap) + SQLUtils.spellWhere(paramsMap);
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql), new Object[] {});
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortField)
				&& org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql += " ORDER BY " + sortField + " " + sortType;
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql, currPage, pageSize), new Object[] {});

		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("AJLBDM", "CODE_AJLB");
		columnConfigMap.put("SLSJ", "DATETIME");
		columnConfigMap.put("ZATZ_JYQK", "CODE_AJLB_XL");
		columnConfigMap.put("LARQ", "DATETIME");
		columnConfigMap.put("ASJFSSJFX_ASJFSKSSJ", "DATETIME");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 查询全部人员
	 *
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	public Map<String, Object> getAllPerson(UserContext userContext, Map<String, Object> paramsMap, int currPage,
			int pageSize, String sortField, String sortType) {
		Map<String, String> replaceMap = Maps.newHashMap();
		replaceMap.put("ID", SQLUtils.spellWhere(ImmutableMap.of("ID", StringUtils.nvlString(paramsMap.remove("ID")))));
		String sql = ResourceUtils.getSQL("select_ry");
		if (paramsMap.containsKey("ASJXGRYBH")) {
			String code = StringUtils.nvlString(paramsMap.remove("ASJXGRYBH"));
			sql += " and (asjxgrybh ='" + code + "' or cyzj_zjhm='" + code + "') ";
		}
		sql = StringUtils.replaceAllVariable(sql, replaceMap) + SQLUtils.spellWhere(paramsMap);
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql), new Object[] {});
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortField)
				&& org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql += " ORDER BY " + sortField + " " + sortType;
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql, currPage, pageSize), new Object[] {});

		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("HJDZ_XZQHDM", "CODE_XZQH");
		columnConfigMap.put("DJSJ", "DATETIME");
		columnConfigMap.put("XXDJDW_GAJGJGDM", "CODE_GXS");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 查询线索关联业务数据
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @param currPage
	 * @param pageSize
	 * @param sortField
	 * @param sortType
	 * @return
	 */
	public Map<String, Object> getAllGywByCle(UserContext userContext, Map<String, Object> paramsMap, int currPage,
			int pageSize, String sortField, String sortType) {
		Map<String, String> replaceMap = Maps.newHashMap();
		replaceMap.put("ID", SQLUtils.spellWhere(ImmutableMap.of("ID", StringUtils.nvlString(paramsMap.remove("ID")))));
		String sql = ResourceUtils.getSQL("selectClueAllGlyw");
		sql = StringUtils.replaceAllVariable(sql, replaceMap) + SQLUtils.spellWhere(paramsMap);
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql), new Object[] {});
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortField)
				&& org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql += " ORDER BY " + sortField + " " + sortType;
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql, currPage, pageSize), new Object[] {});
		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("YWZLDM", "CODE_XSHB_GLYWLX");
		columnConfigMap.put("SJ", "DATETIME");
		columnConfigMap.put("XSLXDM", "CODE_XSHB_XSLX");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 查询全部物品
	 *
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	public Map<String, Object> getAllGoods(UserContext userContext, Map<String, Object> paramsMap, int currPage,
			int pageSize, String sortField, String sortType) {
		Map<String, String> replaceMap = Maps.newHashMap();
		replaceMap.put("ID", SQLUtils.spellWhere(ImmutableMap.of("ID", StringUtils.nvlString(paramsMap.remove("ID")))));
		String sql = ResourceUtils.getSQL("select_wp");
		sql = StringUtils.replaceAllVariable(sql, replaceMap) + SQLUtils.spellWhere(paramsMap);
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql), new Object[] {});
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortField)
				&& org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql += " ORDER BY " + sortField + " " + sortType;
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql, currPage, pageSize), new Object[] {});

		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("XXDJDW_GAJGJGDM", "CODE_GXS");
		columnConfigMap.put("DJSJ", "DATETIME");
		columnConfigMap.put("SAWP_SAWPDM", "CODE_SAWP");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * 更新未读人员数据
	 */
	public int updateWdry(UserContext userContext, Map<String, Object> paramsMap) {
		BO bo = SDK.getBOAPI().get("BO_EU_XSHB_RW_LZ", StringUtils.nvlString(paramsMap.get("ID")));
		List<String> arrayList = new ArrayList<String>(Arrays.asList(bo.getString("WDRY").split(",")));
		arrayList.remove(userContext.getUID());
		HashMap<String, Object> hashMap = Maps.newHashMap();
		hashMap.put("ID", StringUtils.nvlString(paramsMap.get("ID")));
		hashMap.put("WDRY", String.join(",", arrayList));
		return DBSql.update(SQLUtils.getCreateUpdateSQL(hashMap, "BO_EU_XSHB_RW_LZ", new String[] { "ID" }));
	}

	/**
	 * 新增流程记录
	 */
	public int addProcessRecord(UserContext userContext, Map<String, Object> paramsMap, String czlxdm) {
		BO lcBo = new BO();

		Map<String, Object> rwlz = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_LZ");
		BO lzBo = SDK.getBOAPI().get("BO_EU_XSHB_RW_LZ", StringUtils.nvlString(rwlz.get("ID")));

		lcBo.set("RWBH", lzBo.get("RWBH"));
		lcBo.set("RWLZBH", lzBo.get("RWLZBH"));
		lcBo.set("DJSJ", lzBo.get("GXSJ", Date.class));

		JSONObject jsonObject = new JSONObject();
		if ("01".equals(czlxdm)) { // 签收

		} else if ("02".equals(czlxdm)) { // 回退
			jsonObject.put("回退理由：", lzBo.get("HTYJ_JYQK"));
		} else if ("03".equals(czlxdm)) { // 反馈
			Map<String, Object> rwfk = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_FK");
			lcBo.set("GTBH", rwfk.get("FKBH"));
			jsonObject.put("", StringUtils.nvlString(rwfk.get("FKNR_JYQK")));
		} else if ("04".equals(czlxdm)) { // 批复
			Map<String, Object> rwpf = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_PF");
			lcBo.set("GTBH", rwpf.get("PFBH"));
			jsonObject.put("", StringUtils.nvlString(rwpf.get("PFYJ_JYQK")));
		} else if ("05".equals(czlxdm)) { // 审批通过
			jsonObject.put("通过理由：", lzBo.get("HTSPYJ_JYQK"));
		} else if ("06".equals(czlxdm)) { // 审批驳回
			jsonObject.put("驳回理由：", lzBo.get("HTSPYJ_JYQK"));
		} else if ("07".equals(czlxdm)) { // 评分
			jsonObject.put("评分理由：", lzBo.get("PFYJ_JYQK"));
			jsonObject.put("评分等级", lzBo.get("PFDJ"));
		} else if ("08".equals(czlxdm)) { // 转派
			jsonObject.put("转派对象", lzBo.get("JSRY_XM"));
			jsonObject.put("转派理由：", lzBo.get("ZPYY"));
		} else if ("09".equals(czlxdm)) { // 转派成功

		} else if ("10".equals(czlxdm)) { // 转派失败
			jsonObject.put("驳回理由：", lzBo.get("HTYJ_JYQK"));
		} else if ("12".equals(czlxdm)) { // 续报驳回
			jsonObject.put("驳回理由：", lzBo.get("XBYJ_JYQK"));
		} else if ("13".equals(czlxdm)) { // 续报修改

		}

		lcBo.set("RYTXDZ", SDK.getPortalAPI().getUserPhoto(userContext, userContext.getUID()));// 用户头像URL
		lcBo.set("CZLXDM", czlxdm);
		lcBo.set("CZNRMS", jsonObject.isEmpty() ? "" : jsonObject.toJSONString());
		lcBo.setAll(super.createUserMap4DB(userContext, new String[] { "XXDJ" }));

		return SDK.getBOAPI().createDataBO("BO_EU_XSHB_LCJL", lcBo, userContext);
	}

	/**
	 * 更新任务状态（任务表）
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	public void updateTaskStatus(UserContext userContext, Map<String, Object> paramsMap, String statusCode) {
		Map<String, Object> rwlz = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_LZ");
		Map<String, Object> map = Maps.newHashMap();
		map.put("RWBH", rwlz.get("RWBH"));
		map.put("RWZTDM", statusCode);
		int update = DBSql.update(SQLUtils.getCreateUpdateSQL(map, "BO_EU_XSHB_RW_XF", new String[] { "RWBH" }));
		if (update == 0) {
			update = DBSql.update(SQLUtils.getCreateUpdateSQL(map, "BO_EU_XSHB_RW_SB", new String[] { "RWBH" }));
			if (update == 0) {
				DBSql.update(SQLUtils.getCreateUpdateSQL(map, "BO_EU_XSHB_RW_XB", new String[] { "RWBH" }));
			}
		}

	}

	/**
	 * 更新任务流转状态（任务流转表）
	 * 
	 * @param userContext
	 * @param paramsMap
	 * @param statusCode
	 */
	public void updateTaskProcessStatus(UserContext userContext, Map<String, Object> paramsMap, String statusCode) {
		Map<String, Object> rwlz = (Map<String, Object>) paramsMap.get("BO_EU_XSHB_RW_LZ");
		Map<String, Object> map = Maps.newHashMap();
		map.put("ID", rwlz.get("ID"));
		map.put("LZZTDM", statusCode);
		map.put("GXSJ", "SYSDATE");
		DBSql.update(SQLUtils.getCreateUpdateSQL(map, "BO_EU_XSHB_RW_LZ", new String[] { "ID" }));
	}

	/**
	 * 查询全部在逃人员
	 *
	 * @param userContext
	 * @param paramsMap
	 * @return
	 */
	public Map<String, Object> getAllEscapee(UserContext userContext, Map<String, Object> paramsMap, int currPage,
			int pageSize, String sortField, String sortType) {
		String id = StringUtils.nvlString(paramsMap.remove("ID"));
		String sql = ResourceUtils.getSQL("select_ztry");
		if (paramsMap.containsKey("YWSTBH")) {
			String ywstbh = StringUtils.nvlString(paramsMap.remove("YWSTBH"));
			sql += " and ry.ZTRYBH ='" + ywstbh + "' or ry.ZJHM='" + ywstbh + "'";
		}
		sql += SQLUtils.spellWhere(paramsMap);
		int total = DBSql.getInt(SQLUtils.buildCountSQL(sql), new Object[] { id });
		if (org.apache.commons.lang.StringUtils.isNotBlank(sortField)
				&& org.apache.commons.lang.StringUtils.isNotBlank(sortType)) {
			sql += " ORDER BY " + sortField + " " + sortType;
		} else {
			sql += " ORDER BY DJSJ DESC";
		}
		List<RowMap> rows = DBSql.getMaps(SQLUtils.buildPaginationSQL(sql, currPage, pageSize), new Object[] { id });

		Map<String, String> columnConfigMap = Maps.newHashMap();
		columnConfigMap.put("HJDZ_XZQHDM", "CODE_GXS");
		columnConfigMap.put("DJSJ", "DATETIME");
		CodeService.resultOfTransaction(columnConfigMap, rows);
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("rows", rows);
		resultMap.put("total", total);
		return resultMap;
	}

	/**
	 * <p>
	 * httpClient 手动新增人员
	 * </p>
	 * 
	 * @param userContext
	 * @param json
	 * @return
	 * @throws URISyntaxException
	 */
	public int handAdd(UserContext userContext, String json) throws URISyntaxException {
		String apiServer = "http://41.188.66.51/rkfw/dubboTest";// 请求地址
		String apiMethod = "test";// 请求方法
		Map<String, Object> args = Maps.newHashMap();
		args.put("method", apiMethod);
		args.put("json", json);

		String httpGetRequest = HttpClientUtils.httpGetRequest(apiServer, args);
		String substringBetween = StringUtils.substringBetween(httpGetRequest, "success_jsonpCallback(", ")");
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse(substringBetween);
		if ("SUCCESS".equals(StringUtils.nvlString(jsonMap.get("SUCCESS")))) {
			return 1;
		}
		return 0;

	}

	/**
	 * 查询单位下的人员
	 * 
	 * @param departmentid
	 * @return
	 */
	public List<RowMap> getUserByDepartmentid(String departmentid) {
		List<RowMap> rows;
		if (StringUtils.isNotEmpty(departmentid)) {
			rows = DBSql.getMaps(ResourceUtils.getSQL("select_orguser") + "and departmentid=?",
					new String[] { departmentid });
		} else {
			rows = DBSql.getMaps(ResourceUtils.getSQL("select_orguser"));
		}
		return rows;
	}

	/**
	 * 查询CODE表
	 * 
	 * @param table
	 * @return
	 */
	public List<RowMap> selectCode(String table) {
		String sql = "select * from " + table + "";
		List<RowMap> rows = DBSql.getMaps(sql);
		return rows;
	}

}
