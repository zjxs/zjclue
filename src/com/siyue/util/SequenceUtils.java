package com.siyue.util;

import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.bpms.util.UtilString;

public class SequenceUtils {
	
	private static final String DB_SUPPLY = "postgresql";
	
	/**
	 * 
	 * @param seqName
	 * @param prefix
	 * @param gxs
	 * @param dateFormat
	 * @param seqLength
	 * @return
	 */
	public static String getSequenceVal(String seqName, String prefix, String gxs, String dateFormat, int seqLength) {
		StringBuilder builder = new StringBuilder();
		if("postgresql".equalsIgnoreCase(DB_SUPPLY)){
			builder.append("select '").append(prefix).append("'");// 序列前缀
			
			if (UtilString.isNotEmpty(gxs)) {//
				builder.append("||'").append(gxs).append("'");
			}
			if (UtilString.isNotEmpty(dateFormat)) {// 日期格式
				builder.append("||to_char(now(),'" + dateFormat + "')");
			}
			if (Integer.valueOf(seqLength) > 0) {// 序列长度传大于0的时候取
				int padLength = Integer.valueOf(seqLength) - prefix.length() - gxs.length() - dateFormat.length();
//				builder.append("||lpad('").append(DBSql.getString("select nextval('"+seqName+"')")).append("',").append(padLength).append(",'0')");
				
				builder.append("||lpad(fn_get_36_seq('").append(gxs).append("','").append(seqName.toUpperCase()).append("'),")
						.append(padLength).append(",'0')");
			} else {
				builder.append("||nextval('"+seqName+"')");
			}
			builder.append(" as SEQ");
		}else if("oracle".equalsIgnoreCase(DB_SUPPLY)){
			builder.append("select '").append(prefix).append("'");// 序列前缀
			
			if (UtilString.isNotEmpty(gxs)) {//
				builder.append("||'").append(gxs).append("'");
			}
			if (UtilString.isNotEmpty(dateFormat)) {// 日期格式
				builder.append("||to_char(sysdate,'" + dateFormat + "')");
			}
			if (Integer.valueOf(seqLength) > 0) {// 序列长度传大于0的时候取
				int padLength = Integer.valueOf(seqLength) - prefix.length() - gxs.length() - dateFormat.length();
				builder.append("||lpad(fn_get_36_seq('").append(gxs).append("','").append(seqName.toUpperCase()).append("'),")
				.append(padLength).append(",0)");
			} else {
				builder.append("||").append(seqName).append(".nextval");
			}
			builder.append(" as SEQ from dual");
			
		}
		return DBSql.getString(builder.toString());
	}
}
