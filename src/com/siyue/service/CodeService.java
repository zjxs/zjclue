package com.siyue.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.curator.shaded.com.google.common.collect.Maps;
import org.dom4j.Document;
import org.dom4j.Element;

import com.actionsoft.apps.resource.AppContext;
import com.actionsoft.bpms.commons.database.RowMap;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.siyue.util.DateUtils;
import com.siyue.util.StringUtils;
import com.siyue.util.XmlUtils;

public class CodeService {

	private static Map<String, Map<String, Map<String, String>>> CODE_ALL_MAP = Maps.newHashMap();
	private static final String RESOURCE_BUNDLE_REPOSITORY_CODE_CONFIG = "repository/code/codeConfig.xml";

	public static Map<String, Map<String, Map<String, String>>> getCodeAllMap() {
		return CODE_ALL_MAP;
	}

	public static Map<String, Map<String, String>> getCodeMap(String codeName) {
		return getCodeAllMap().get(codeName);
	}

	/**
	 * 从xml初始化code
	 */
	public static void init(AppContext context) {
		try {
			InputStream is = new FileInputStream(context.getPath() + RESOURCE_BUNDLE_REPOSITORY_CODE_CONFIG);
			Document doc = XmlUtils.readXml(is);
			List<Element> elements = doc.selectNodes("codes/code");
			for (Element element : elements) {
				String codeTableName = element.attributeValue("name");
				loadCodeContainer(codeTableName);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void resultOfTransaction(Map<String, String> columnConfigMap, RowMap row) {
		Map<String, Object> aMap = Maps.newLinkedHashMap();
		for (String field : row.keySet()) {
			String fieldConfig = StringUtils.nvlString(columnConfigMap.get(field));
			Object value = row.get(field);
			if (!fieldConfig.equals("") && !fieldConfig.equalsIgnoreCase("date") && !fieldConfig.equalsIgnoreCase("datetime")) {
				loadCodeContainer(fieldConfig);
				Map<String, Map<String, String>> codeMap = CODE_ALL_MAP.get(fieldConfig);
				String columnVal = "";
				String[] valSplit = StringUtils.nvlString(value).split(",|/|，");
				boolean isFirst = false;
				for (String val : valSplit) {
					val = StringUtils.nvlString(val).trim();
					if (!val.equals("")) {
						Map<String, String> code = codeMap.get(val);
						if (code != null) {
							if (isFirst) {
								columnVal += ",";
							}
							isFirst = true;
							columnVal += code.get("NAME") == null ? StringUtils.nvlString(val) : code.get("NAME");
						} else {
							if (isFirst) {
								columnVal += ",";
							}
							isFirst = true;
							columnVal += StringUtils.nvlString(val);
						}
					}
				}
				row.put(field, columnVal);
				aMap.put(field + "__CODE", value);
			}
			if (fieldConfig.equalsIgnoreCase("date") || fieldConfig.equalsIgnoreCase("datetime")) {
				String parseDate = parseDate(fieldConfig, value);
				row.put(field, parseDate);
			}
		}
		row.putAll(aMap);
	}

	public static void resultOfTransaction(Map<String, String> columnConfigMap, List<RowMap> rows) {
		for (RowMap row : rows) {
			resultOfTransaction(columnConfigMap, row);
		}
	}

	/**
	 * 转换日期
	 * 
	 * @param fieldConfig
	 * @param value
	 * @return
	 */
	private static String parseDate(String fieldConfig, Object value) {
		String format = "";
		if ("date".equalsIgnoreCase(fieldConfig)) {
			format = "yyyy-MM-dd";
		} else if ("dateTime".equalsIgnoreCase(fieldConfig)) {
			format = "yyyy-MM-dd HH:mm:ss";
		}
		String parseDate = "";
		if (value instanceof Date || value instanceof Timestamp) {
			try {
				parseDate = DateUtils.parseDate((Date) value, format);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return parseDate;
	}

	private static void loadCodeContainer(String codeTableName) {
		codeTableName = StringUtils.nvlString(codeTableName).toUpperCase();
		if ("".equals(codeTableName)) {
			return;
		}
		if (!CODE_ALL_MAP.containsKey(codeTableName)) {
			Map<String, Map<String, String>> codeMap = CODE_ALL_MAP.get(codeTableName);
			if (codeMap == null) {
				codeMap = Maps.newLinkedHashMap();
				CODE_ALL_MAP.put(codeTableName, codeMap);
				try {
					String sql = "SELECT * FROM " + codeTableName;
					List<RowMap> codeQueryData = DBSql.getMaps(sql);
					for (Map rowMap : codeQueryData) {
						String code = (String) rowMap.get("CODE");
						codeMap.put(code, rowMap);
					}
					SDK.getLogAPI().consoleInfo("[CodeContainer]加载" + codeTableName + "代码[成功]");
				} catch (Exception e) {
					SDK.getLogAPI().consoleErr("[CodeContainer]加载" + codeTableName + "代码[失败]");
				}
			}
		}
	}

	public List<Map<String, Object>> getTreeCombo(String code, String table, String lev) {
		String sql = "select * from " + table + " where LEV = ? ";
		if (StringUtils.isNotBlank(code)) {
			sql += " and CODE_LEV" + lev + " = '" + code + "'";
		}
		sql += " order by CODE ";

		List<RowMap> rows;
		try {
			rows = DBSql.getMaps(sql, new Object[] { StringUtils.nvlString((Integer.valueOf(lev) + 1)) });
		} catch (Exception e) {
			SDK.getLogAPI().consoleInfo("[CODE lev 字段类型错误为Integer类型]");
			rows = DBSql.getMaps(sql, new Object[] { (Integer.valueOf(lev) + 1) });
		}
		List<Map<String, Object>> resultList = Lists.newArrayList();
		for (RowMap row : rows) {
			Map<String, Object> resultMap = Maps.newHashMap();
			String child = row.getString("CHILD");
			resultMap.put("code", row.getString("CODE"));
			resultMap.put("title", row.getString("NAME"));
			resultMap.put("lev", row.getString("LEV"));
			if ("1".equals(child)) {
				resultMap.put("children", Lists.newArrayList());
				resultMap.put("loading", false);
			}
			resultList.add(resultMap);
		}
		return resultList;
	}

	public List<Map<String, Object>> getTreeComboForTreeSelect(String code, String table, String lev) {
		String sql = "select * from " + table + " where LEV = ? ";
		if (StringUtils.isNotBlank(code)) {
			sql += " and CODE_LEV" + lev + " = '" + code + "'";
		}
		sql += " order by CODE ";

		List<RowMap> rows;
		try {
			rows = DBSql.getMaps(sql, new Object[] { StringUtils.nvlString((Integer.valueOf(lev) + 1)) });
		} catch (Exception e) {
			SDK.getLogAPI().consoleInfo("[CODE lev 字段类型错误为Integer类型]");
			rows = DBSql.getMaps(sql, new Object[] { (Integer.valueOf(lev) + 1) });
		}
		List<Map<String, Object>> resultList = Lists.newArrayList();
		for (RowMap row : rows) {
			Map<String, Object> resultMap = Maps.newHashMap();
			String child = row.getString("CHILD");
			resultMap.put("value", row.getString("CODE"));
			resultMap.put("label", row.getString("NAME"));
			resultMap.put("lev", row.getString("LEV"));
			if ("1".equals(child)) {
				resultMap.put("children", Lists.newArrayList());
				resultMap.put("loading", false);
			}
			resultList.add(resultMap);
		}
		return resultList;
	}

	public List<Map<String, Object>> getTreeComboForTreeSelectAll(String code, String table, String lev) {
		String sql = "select * from " + table + " where LEV = ? ";
		if (StringUtils.isNotBlank(code)) {
			sql += " and CODE_LEV" + lev + " = '" + code + "'";
		}
		sql += " order by CODE ";

		List<RowMap> rows;
		try {
			rows = DBSql.getMaps(sql, new Object[] { StringUtils.nvlString((Integer.valueOf(lev) + 1)) });
		} catch (Exception e) {
			SDK.getLogAPI().consoleInfo("[CODE lev 字段类型错误为Integer类型]");
			rows = DBSql.getMaps(sql, new Object[] { (Integer.valueOf(lev) + 1) });
		}
		List<Map<String, Object>> resultList = Lists.newArrayList();
		for (RowMap row : rows) {
			Map<String, Object> resultMap = Maps.newHashMap();
			String child = row.getString("CHILD");
			resultMap.put("value", row.getString("CODE"));
			resultMap.put("label", row.getString("NAME"));
			resultMap.put("lev", row.getString("LEV"));
			if ("1".equals(child)) {
				resultMap.put("children", getTreeComboForTreeSelect(row.getString("CODE"), table, row.getString("LEV")));
				resultMap.put("loading", false);
			}
			resultList.add(resultMap);
		}
		return resultList;
	}

	public List<Map<String, Object>> searchTreeCombo(String table, String searchWord) {
		String sql = "select * from " + table + " t where CODE like '" + searchWord + "%'" + "or NAME like '%" + searchWord + "%' or SPELL like '" + searchWord.toUpperCase() + "%' order by LEV,CODE ";
		List<RowMap> rows = DBSql.getMaps(sql, new Object[] {});

		if (rows == null) {
			return Lists.newArrayList();
		}
		Set<String> codeSet = Sets.newHashSet();
		for (RowMap row : rows) {
			int lev = row.getInt("LEV");
			String code = row.getString("CODE");
			while (lev != 0) {
				String key = "CODE_LEV" + lev;
				String value = row.getString(key);
				if (StringUtils.isNotBlank(value)) {
					codeSet.add(value);
				}
				lev--;
			}
			codeSet.add(code);
		}

		MultiValueMap multiValueMap = new MultiValueMap();
		List<RowMap> codeList = this.getCodeList(table, codeSet);
		if (codeList != null) {
			for (RowMap row : codeList) {
				int lev = row.getInt("LEV");
				multiValueMap.put(lev, row);
			}
		}

		return recursionPackageCode4Tree(null, 1, multiValueMap);
	}

	private List<RowMap> getCodeList(String table, Set<String> codeSet) {
		String sql = "select * from " + table + " where CODE in('" + StringUtils.join(codeSet, "','") + "')";
		return DBSql.getMaps(sql, new Object[] {});
	}

	/**
	 * 递归包装Code
	 * 
	 * @param parentCode
	 * @param lev
	 * @param multiValueMap
	 * @return
	 */
	public List<Map<String, Object>> recursionPackageCode4Tree(String parentCode, int lev, MultiValueMap multiValueMap) {
		List<Map<String, Object>> resultList = Lists.newArrayList();
		Iterator iterator = multiValueMap.getCollection(lev).iterator();
		while (iterator.hasNext()) {
			RowMap row = (RowMap) iterator.next();
			String child = row.getString("CHILD");
			String code = row.getString("CODE");

			if (StringUtils.isBlank(parentCode) || parentCode.equals(row.getString("CODE_LEV" + (lev - 1)))) {
				Map<String, Object> resultMap = Maps.newHashMap();
				resultMap.put("title", row.getString("NAME"));
				resultMap.put("code", row.getString("CODE"));
				resultMap.put("lev", row.getString("LEV"));

				if ("1".equals(child)) {
					// 遍历下级节点
					Iterator subIterator = multiValueMap.getCollection(lev + 1).iterator();
					boolean containsValue = false;
					while (subIterator.hasNext()) {
						RowMap subRowMap = (RowMap) subIterator.next();
						if (code.equals(subRowMap.getString("CODE_LEV" + lev))) {
							containsValue = true;
							break;
						}
					}
					if (containsValue) {
						resultMap.put("children", recursionPackageCode4Tree(code, lev + 1, multiValueMap));
						resultMap.put("expand", true);
					} else {
						resultMap.put("loading", false);
						resultMap.put("children", Lists.newArrayList());
					}
				}
				resultList.add(resultMap);
			}
		}
		return resultList;
	}

	/**
	 * new tree
	 */

	public List<Map<String, Object>> getData(String tableName, String parentCode, int parentLev) {
		String sql = "select * from " + tableName + " where LEV = '" + (parentLev + 1) + "'";
		if (StringUtils.isNotBlank(parentCode)) {
			sql += " and CODE_LEV" + parentLev + " = '" + parentCode + "'";
		}
		sql += " order by CODE ";

		List<RowMap> rows = DBSql.getMaps(sql);
		Iterator<RowMap> iterator = rows.iterator();

		List<Map<String, Object>> resultList = Lists.newArrayList();
		while (iterator.hasNext()) {
			Map<String, Object> row = iterator.next();
			Map<String, Object> resultMap = Maps.newHashMap();
			String child = row.get("CHILD").toString();
			String code = row.get("CODE").toString();
			String lev = row.get("LEV").toString();
			String name = row.get("NAME").toString();
			resultMap.put("code", code);
			resultMap.put("title", name + "[" + code + "]");
			resultMap.put("name", name);
			resultMap.put("lev", lev);
			if ("1".equals(child)) {
				resultMap.put("children", Lists.newArrayList());
				resultMap.put("loading", false);
			}
			resultList.add(resultMap);
		}
		return resultList;
	}

	public List<Map<String, Object>> searchData(String table, String searchWord) {
		String sql = "select * from " + table + " t where CODE like '" + searchWord + "%'" + "or NAME like '%" + searchWord + "%' or SPELL like '" + searchWord.toUpperCase() + "%'order by LEV,CODE ";

		List<RowMap> rows = DBSql.getMaps(sql);
		if (rows == null || rows.isEmpty()) {
			return Lists.newArrayList();
		}
		Set<String> codeSet = Sets.newHashSet();
		for (Map<String, Object> row : rows) {
			int lev = Integer.valueOf(row.get("LEV").toString());
			String code = row.get("CODE").toString();
			while (lev != 0) {
				String key = "CODE_LEV" + lev;
				if (!row.containsKey(key))
					break;
				String value = row.get(key).toString();
				if (StringUtils.isNotBlank(value)) {
					codeSet.add(value);
				}
				lev--;
			}
			codeSet.add(code);
		}

		Map<Integer, List> multiValueMap = Maps.newHashMap();
		List<RowMap> codeList = getCodeList(table, codeSet);
		if (codeList != null) {
			for (Map<String, Object> codeMap : codeList) {
				int lev = Integer.valueOf(codeMap.get("LEV").toString());
				List list = Lists.newArrayList();
				if (multiValueMap.get(lev) != null) {
					list = multiValueMap.get(lev);
				}
				list.add(codeMap);
				multiValueMap.put(lev, list);
			}
		}

		return recursionPackageCode(null, 1, multiValueMap);
	}

	/**
	 * 递归包装Code
	 * 
	 * @param parentCode
	 * @param lev
	 * @param multiValueMap
	 * @return
	 */
	private List<Map<String, Object>> recursionPackageCode(String parentCode, int lev, Map<Integer, List> multiValueMap) {
		List<Map<String, Object>> resultList = Lists.newArrayList();
		Iterator iterator = multiValueMap.get(lev).iterator();
		while (iterator.hasNext()) {
			Map row = (Map) iterator.next();
			String child = row.get("CHILD").toString();
			String code = row.get("CODE").toString();

			if (StringUtils.isBlank(parentCode) || parentCode.equals(row.get("CODE_LEV" + (lev - 1)))) {
				Map<String, Object> resultMap = Maps.newHashMap();
				resultMap.put("title", row.get("NAME").toString());
				resultMap.put("code", row.get("CODE").toString());
				resultMap.put("lev", row.get("LEV").toString());

				if ("1".equals(child)) {
					// 遍历下级节点
					Iterator subIterator = multiValueMap.get(lev + 1).iterator();
					boolean containsValue = false;
					while (subIterator.hasNext()) {
						Map subRowMap = (Map) subIterator.next();
						if (code.equals(subRowMap.get("CODE_LEV" + lev))) {
							containsValue = true;
							break;
						}
					}
					if (containsValue) {
						resultMap.put("children", recursionPackageCode(code, lev + 1, multiValueMap));
					} else {
						resultMap.put("loading", false);
						resultMap.put("children", Lists.newArrayList());
					}
				}
				resultList.add(resultMap);
			}
		}
		return resultList;
	}

}
