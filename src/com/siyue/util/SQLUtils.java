package com.siyue.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class SQLUtils {
	
	private static final String DB_SUPPLY = "postgresql";
	
	/**
	 * AWSServerConf.getDatabaseProvider()
	 * @param paramsMap
	 * @param tableName
	 * @return
	 */
	public static String getCreateInsertSQL(Map<String, Object> paramsMap, String tableName) {
		Set<String> fieldsSet = paramsMap.keySet();
		List<Object> valueList = Lists.newLinkedList(paramsMap.values());
		
		if("postgresql".equalsIgnoreCase(DB_SUPPLY)){
			for (int i = 0; i < valueList.size(); i++) {
				String val = StringUtils.nvlString(valueList.get(i));
				if (StringUtils.isNotBlank(val)) {
					if (val.contains("-") && val.contains(":") && DateUtils.isValidDate(val)) {// 时间包含时分秒
						val = val.replaceAll(":", "").replaceAll(" ", "").replaceAll("-", "");
						valueList.set(i, "TO_TIMESTAMP('" + val + "','YYYYMMDDHH24MISS')");
					} else if (val.contains("-") && DateUtils.isValidDate(val)) {// 时间
						val = val.replaceAll("-", "");
						valueList.set(i, "TO_TIMESTAMP('" + val + "','YYYYMMDD')");
					} else if ("SYSDATE".equals(val)) {
						valueList.set(i, "current_timestamp");
					} else {
						valueList.set(i, "'" + val + "'");
					}
				} else {
					valueList.set(i, "''");
				}
			}
		}else if("oracle".equalsIgnoreCase(DB_SUPPLY)){
			for (int i = 0; i < valueList.size(); i++) {
				String val = StringUtils.nvlString(valueList.get(i));
				if (StringUtils.isNotBlank(val)) {
					if (val.contains("-") && val.contains(":") && DateUtils.isValidDate(val)) {// 时间包含时分秒
						val = val.replaceAll(":", "").replaceAll(" ", "").replaceAll("-", "");
						valueList.set(i, "TO_DATE('" + val + "','YYYYMMDDHH24MISS')");
					} else if (val.contains("-") && DateUtils.isValidDate(val)) {// 时间
						val = val.replaceAll("-", "");
						valueList.set(i, "TO_DATE('" + val + "','YYYYMMDD')");
					} else if ("SYSDATE".equals(val)) {
						valueList.set(i, "SYSDATE");
					} else {
						valueList.set(i, "'" + val + "'");
					}
				} else {
					valueList.set(i, "''");
				}
			}
		}

		Map<String, String> replaceMap = Maps.newHashMap();
		replaceMap.put("fields", StringUtils.join(fieldsSet, ","));
		replaceMap.put("values", StringUtils.join(valueList, ","));
		replaceMap.put("tableName", tableName);

		String sql = "INSERT INTO #{tableName} (#{fields}) VALUES (#{values})";
		return StringUtils.replaceVariable(sql, replaceMap);
	}

	public static String getCreateUpdateSQL(Map<String, Object> paramsMap, String tableName, String[] conditionFields) {
		Map<String,Object> finalParamsMap = Maps.newHashMap(paramsMap);
		Map<String,Object> queryParamsMap = Maps.newHashMap();
		for (String conditionField : conditionFields) {
			queryParamsMap.put(conditionField, finalParamsMap.remove(conditionField));
		}

		List<String> mappedList = Lists.newArrayList();
		if("postgresql".equalsIgnoreCase(DB_SUPPLY)){
			for (String field : finalParamsMap.keySet()) {
				String val = StringUtils.nvlString(finalParamsMap.get(field));
				if (val.contains("-") && val.contains(":") && DateUtils.isValidDate(val)) {// 时间包含时分秒
					val = val.replaceAll(":", "").replaceAll(" ", "").replaceAll("-", "");
					mappedList.add(field + "=" + "TO_TIMESTAMP('" + val + "','YYYYMMDDHH24MISS')");
				} else if (val.contains("-") && DateUtils.isValidDate(val)) {// 时间
					val = val.replaceAll("-", "");
					mappedList.add(field + "=" + "TO_TIMESTAMP('" + val + "','YYYYMMDD')");
				} else if ("SYSDATE".equals(val)) {
					mappedList.add(field + " = current_timestamp ");
				} else {
					mappedList.add(field + "=" + "'" + val + "'");
				}
			}
		}else if("oracle".equalsIgnoreCase(DB_SUPPLY)){
			for (String field : finalParamsMap.keySet()) {
				String val = StringUtils.nvlString(finalParamsMap.get(field));
				if (val.contains("-") && val.contains(":") && DateUtils.isValidDate(val)) {// 时间包含时分秒
					val = val.replaceAll(":", "").replaceAll(" ", "").replaceAll("-", "");
					mappedList.add(field + "=" + "TO_DATE('" + val + "','YYYYMMDDHH24MISS')");
				} else if (val.contains("-") && DateUtils.isValidDate(val)) {// 时间
					val = val.replaceAll("-", "");
					mappedList.add(field + "=" + "TO_DATE('" + val + "','YYYYMMDD')");
				} else if ("SYSDATE".equals(val)) {
					mappedList.add(field + " = SYSDATE ");
				} else {
					mappedList.add(field + "=" + "'" + val + "'");
				}
			}
		}
		
		Map<String, String> replaceMap = Maps.newHashMap();
		replaceMap.put("mapped", StringUtils.join(mappedList, ","));
		replaceMap.put("tableName", tableName);
		String sql = "UPDATE #{tableName} SET #{mapped} WHERE 1=1 " + spellWhere(queryParamsMap);
		return StringUtils.replaceVariable(sql, replaceMap);
	}

	public static String getCreateDeleteSQL(Map<String, Object> paramsMap, String tableName) {
		Map<String, String> replaceMap = Maps.newHashMap();
		replaceMap.put("condition", SQLUtils.spellWhere(paramsMap));
		replaceMap.put("tableName", tableName);
		String sql = "DELETE FROM #{tableName} WHERE 1=1 #{condition}";
		return StringUtils.replaceVariable(sql, replaceMap);
	}
	
    public static String spellWhere(Map<String, Object> paramsMap) {
        StringBuffer querySqlBuffer = new StringBuffer();
        if (paramsMap != null && paramsMap.size() > 0) {
            for (String column : paramsMap.keySet()) {
                String value = StringUtils.nvlString(paramsMap.get(column));
                if (column.contains("~")) {
                    String[] split = column.split("~");
                    String col = split[0];
                    String mark = split[1];
                    if ("2".equals(mark)) {// 开始时间
                        if (value.contains("-")) {// 时间格式的
                            value = value.replaceAll("-", "");
                            if (value.length() == 8) {
                                querySqlBuffer.append(" AND " + col + ">=TO_TIMESTAMP('" + value + "000000','YYYYMMDDHH24MISS')");
                            } else {
                                value = value.replaceAll(":", "").replaceAll(" ", "");
                                querySqlBuffer.append(" AND " + col + ">=TO_TIMESTAMP('" + value + "','YYYYMMDDHH24MISS')");
                            }
                        } else {// 年龄段格式(字段类型为字符串)
                            int start = Integer.parseInt(value);
                            querySqlBuffer.append(" AND " + col + "<=TO_CHAR(ADD_MONTHS(TRUNC(SYSDATE,'Y'),-12*" + start + "),'YYYYMMDD')");
                        }
                    } else if ("3".equals(mark)) {// 结束时间
                        if (value.contains("-")) {
                            value = value.replaceAll("-", "");
                            if (value.length() == 8) {
                                querySqlBuffer.append(" AND " + col + "<=TO_TIMESTAMP('" + value + "235959','YYYYMMDDHH24MISS')");
                            } else {
                                value = value.replaceAll(":", "").replaceAll(" ", "");
                                querySqlBuffer.append(" AND " + col + "<=TO_TIMESTAMP('" + value + "','YYYYMMDDHH24MISS')");
                            }
                        } else {// 年龄段格式(字段类型为字符串)
                            int end = Integer.parseInt(value);
                            querySqlBuffer.append(" AND " + col + ">=TO_CHAR(ADD_MONTHS(TRUNC(SYSDATE,'Y'),-12*" + end + "),'YYYYMMDD')");
                        }
                    } else if ("4".equals(mark)) {// 模糊条件
                        if (value.contains(",") || value.contains("，")) {
                            querySqlBuffer.append(" AND REGEXP_LIKE (" + col + ",'" + value.replaceAll(",|，", "|") + "')");
                        } else {
                            querySqlBuffer.append(" AND " + col + " LIKE '" + value + "%'");
                        }
                    } else if ("5".equals(mark)) {// code
                        if (value.contains(",")) {// 多值code且满足like查询
                            String[] split2 = value.split(",");
                            List<String> asList = Arrays.asList(split2);
                            List<String> strList = new ArrayList<String>();
                            for (String val : asList) {
                                strList.add(col + " LIKE '" + StringUtils.cutLastZero(val) + "%'");
                            }
                            querySqlBuffer.append(" AND (" + StringUtils.join(strList, " OR ") + ")");
                        } else {
                            querySqlBuffer.append(" AND " + col + " LIKE '" + StringUtils.cutLastZero(value) + "%'");
                        }
                    } else if ("6".equals(mark)) {// code多值，查询字段列多值
                        querySqlBuffer.append(" AND REGEXP_LIKE(" + col + ", '" + value.replaceAll(",", "|").trim() + "')");
                    } else if ("7".equals(mark)) {
                        querySqlBuffer.append(" AND " + col + " >=TO_NUMBER (" + value + ")");
                    } else if ("8".equals(mark)) {
                        querySqlBuffer.append(" AND " + col + " <=TO_NUMBER (" + value + ")");
                    } else if ("9".equals(mark)) {// 模糊条件
                    	if (value.contains(",")) {
                            String[] split2 = value.split(",");
                            List<String> asList = Arrays.asList(split2);
                            List<String> strList = new ArrayList<String>();
                            for (String val : asList) {
                                strList.add(col + " LIKE '" + StringUtils.cutLastZero(val) + "%'");
                            }
                            querySqlBuffer.append(" AND (" + StringUtils.join(strList, " OR ") + ")");
                        } else {
                            querySqlBuffer.append(" AND " + col + " LIKE '%" + StringUtils.cutLastZero(value) + "%'");
                        }
                        querySqlBuffer.append(" AND " + col + " LIKE '%" + value + "%'");
                    } else if ("10".equals(mark)) {// not in
                    	 String[] split2 = value.replaceAll("，", ",").split(",");
                         List<String> asList = Arrays.asList(split2);
                         querySqlBuffer.append(" AND (");
                         List<String> sqlList = Lists.newArrayList();
                     	 List<List<String>> cutList = CommonUtils.cutList(asList, 1000);
                     	for (List<String> list : cutList) {
                     		String values = "'" + StringUtils.join(list, "','") + "'";
                     		sqlList.add(col + " not IN (" + values + ") ");
     					}
                         querySqlBuffer.append(StringUtils.join(sqlList," OR ")).append(")");
                    } else if ("11".equals(mark)) {
                    	List<String> valueList = (List<String>) JSONObject.parse(value);
                    	String startTime = valueList.get(0);
                    	if (startTime.contains("-")) {// 时间格式的
                    		startTime = startTime.replaceAll("-", "");
                            if (startTime.length() == 8) {
                                querySqlBuffer.append(" AND " + col + ">=TO_TIMESTAMP('" + startTime + "000000','YYYYMMDDHH24MISS')");
                            } else {
                            	startTime = startTime.replaceAll(":", "").replaceAll(" ", "");
                                querySqlBuffer.append(" AND " + col + ">=TO_TIMESTAMP('" + startTime + "','YYYYMMDDHH24MISS')");
                            }
                        }
                    	String endTime = valueList.get(1);
                    	if (endTime.contains("-")) {
                    		endTime = endTime.replaceAll("-", "");
                            if (endTime.length() == 8) {
                                querySqlBuffer.append(" AND " + col + "<=TO_TIMESTAMP('" + endTime + "235959','YYYYMMDDHH24MISS')");
                            } else {
                            	endTime = endTime.replaceAll(":", "").replaceAll(" ", "");
                                querySqlBuffer.append(" AND " + col + "<=TO_TIMESTAMP('" + endTime + "','YYYYMMDDHH24MISS')");
                            }
                        }
                    }
                } else if (value.contains(",") || value.contains("，")) {// 多值处理
                    String[] split = value.replaceAll("，", ",").split(",");
                    List<String> asList = Arrays.asList(split);
                    querySqlBuffer.append(" AND (");
                    List<String> sqlList = Lists.newArrayList();
                	List<List<String>> cutList = CommonUtils.cutList(asList, 1000);
                	for (List<String> list : cutList) {
                		String values = "'" + StringUtils.join(list, "','") + "'";
                		sqlList.add(column + " IN (" + values + ") ");
					}
                    querySqlBuffer.append(StringUtils.join(sqlList," OR ")).append(")");
                } else {// 一般条件
                    querySqlBuffer.append(" AND " + column + "='" + value + "'");
                }
            }
        }
        return querySqlBuffer.toString();
    }
    
	/**
	 * 返回带分页的sql查询语句
	 * @param sql
	 * @param currPage
	 * @param pageSize
	 * @return
	 */
	public static String buildPaginationSQL(String sql, int currPage, int pageSize) {
		StringBuilder builder = new StringBuilder();
		if("postgresql".equalsIgnoreCase(DB_SUPPLY)){
			builder.append("SELECT * FROM (");
			builder.append(sql);
			builder.append(") T LIMIT ");
			builder.append(String.valueOf(pageSize));
			builder.append(" OFFSET ");
			builder.append(String.valueOf((currPage - 1) * pageSize));
		}else if("oracle".equalsIgnoreCase(DB_SUPPLY)) {
			builder.append("SELECT * FROM (SELECT T.*, ROWNUM RN FROM (");
			builder.append(sql);
			builder.append(") T WHERE ROWNUM <= ");
			builder.append(String.valueOf(currPage * pageSize));
			builder.append(") WHERE RN > ");
			builder.append(String.valueOf((currPage - 1) * pageSize));
		}
		return builder.toString();
	}

	/**
	 * 返回求count的sql语句
	 * @param sql
	 * @return
	 */
	public static String buildCountSQL(String sql) {
		StringBuilder buildSql = new StringBuilder();
		buildSql.append("SELECT COUNT(*) FROM (");
		buildSql.append(sql);
		buildSql.append(") C");
		return buildSql.toString();
	}
}
