package com.siyue.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.time.DateFormatUtils;


public class DateUtils {
	private static final Set<String> DATE_FORMATS = new LinkedHashSet<String>();
	/**
	 * yyyyMMddHHmmss
	 */
	public static final String FORMAT_1 = "yyyyMMddHHmmss";
	/**
	 * yyyy-MM-dd HH:mm:ss
	 */
	public static final String FORMAT_2 = "yyyy-MM-dd HH:mm:ss";
	/**
	 * yyyy-MM-dd
	 */
	public static final String FORMAT_3 = "yyyy-MM-dd";
	/**
	 * yyyyMMdd
	 */
	public static final String FORMAT_4 = "yyyyMMdd";
	/**
	 * yyyyMM
	 */
	public static final String FORMAT_5 = "yyyyMM";
	/**
	 * yyyy
	 */
	public static final String FORMAT_6 = "yyyy";
	
	static{
		DATE_FORMATS.add(FORMAT_1);
		DATE_FORMATS.add(FORMAT_2);
		DATE_FORMATS.add("yyyyMMddHHmm");
		DATE_FORMATS.add("yyyyMMddHH");
		DATE_FORMATS.add(FORMAT_4);
		DATE_FORMATS.add(FORMAT_3);
		DATE_FORMATS.add(FORMAT_5);
		DATE_FORMATS.add(FORMAT_6);
	}
	
	public static Date toDate(String dateString) throws ParseException{
		Date parse = toDate(dateString,false);
		return parse;
	}
	
	public static Date toDate(String dateString,boolean isEndDate) throws ParseException{
		dateString = StringUtils.nvlString(dateString);
		dateString = dateString.replaceAll("-", "");
		dateString = dateString.replaceAll(":", "");
		dateString = dateString.replaceAll(" ", "");
		dateString = dateString.replaceAll("/", "");
		dateString = dateString.replaceAll("\\.", "");
		Date parse = org.apache.commons.lang.time.DateUtils.parseDate(dateString, DATE_FORMATS.toArray(new String[]{}));
		if(isEndDate){
			if(dateString.length() == 4){
				parse.setYear(parse.getYear()+1);
				parse.setSeconds(parse.getSeconds()-1);
			}else if(dateString.length() == 6){
				parse.setMonth(parse.getMonth()+1);
				parse.setSeconds(parse.getSeconds()-1);
			}else if(dateString.length() == 8){
				parse.setDate(parse.getDate()+1);
				parse.setSeconds(parse.getSeconds()-1);
			}else if(dateString.length() == 10){
				parse.setHours(parse.getHours()+1);
				parse.setSeconds(parse.getSeconds()-1);
			}else if(dateString.length() == 12){
				parse.setMinutes(parse.getMinutes()+1);
				parse.setSeconds(parse.getSeconds()-1);
			}
		}
		return parse;
	}
	
	public static String parseDate(Date date,String dateFormat){
		String format = DateFormatUtils.format(date, dateFormat);
		return format;
	}
	
	public static boolean isValidDate(String dateStr) {

		String pattern = "";
		if (dateStr.length() == DateUtils.FORMAT_2.length()) {
			pattern = DateUtils.FORMAT_2;
		} else if (dateStr.length() == DateUtils.FORMAT_3.length()) {
			pattern = DateUtils.FORMAT_3;
		}

		boolean convertSuccess = false;
		// 指定日期格式为四位年/两位月份/两位日期，注意yyyy/MM/dd区分大小写；
		// 设置lenient为false.
		// 否则SimpleDateFormat会比较宽松地验证日期，比如2007/02/29会被接受，并转换成2007/03/01
		if (dateStr.length() == pattern.length()) {
			String[] array = dateStr.split("-");

			if (array.length == 3) {
				int year = Integer.valueOf(array[0]);
				int month = Integer.valueOf(array[1]);
				int day = Integer.valueOf(array[2].substring(0, 2));

				int[] monthLengths = new int[] { 0, 31, -1, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
				// 是否闰年
				if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
					monthLengths[2] = 29;
				} else {
					monthLengths[2] = 28;
				}
				if (month >= 1 && month <= 12) {
					if (day >= 1 && day <= monthLengths[month]) {
						SimpleDateFormat sdf = new SimpleDateFormat(pattern);
						try {
							sdf.setLenient(true);
							sdf.parse(dateStr);
							convertSuccess = true;
						} catch (ParseException e) {
							// 如果throw
							// java.text.ParseException或者NullPointerException，就说明格式不对
						}
					}
				}
			}
		}
		return convertSuccess;
	}
}
