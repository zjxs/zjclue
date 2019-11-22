package com.siyue.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.map.CaseInsensitiveMap;

public class StringUtils extends org.apache.commons.lang.StringUtils {
	
	public static String emptyObject(Object object) {
		return emptyObject(object, "");
	}

	public static boolean isNotEmpty(String str){
		if(str!=null && str.length()>0){
			return true;
		}
		return false;
	}

	public static boolean isNotEmpty(StringBuilder sb){
		if(sb!=null && sb.length()>0){
			return true;
		}
		return false;
	}

	public static String emptyObject(Object obj, String defaultVal) {
		if (obj == null) {
			return defaultVal;
		} else {
			return String.valueOf(obj);
		}
	}
	
	/**
	 * 编码文件名  根据request来判断类型返回
	 * @param fileName		文件名字
	 * @param request		request对象
	 * @return				编码后的文件名
	 */
	public static String encodeFileName(String fileName,HttpServletRequest request) {
		String agent = request.getHeader("USER-AGENT");
		try{
			if (agent != null && agent.indexOf("MSIE") == -1) { //FF
				fileName="=?UTF-8?B?"+(new String(Base64.encodeBase64(fileName.getBytes("UTF-8"))))+"?=";
			} else {//ie
				fileName=new String(fileName.getBytes("GBK"),"iso-8859-1");
			}  
		}catch (Exception e) {
			e.printStackTrace();
		}
		return fileName;
	}
	
	
	public static Integer emptyConvert(Object obj,int target){
		return (obj==null)?target:Integer.valueOf(obj.toString());
	}
	
	/**
	 * 功能:判断字符串是否为数字
	 * @param srcString
	 *            源字符串
	 * @return
	 */
	public static boolean isNumeric(Object srcString){
		return isNumeric(StringUtils.nvlString(srcString));
	}
	public static boolean isNumeric(String srcString){
		boolean returnVal = false;
		if(isNotBlank(srcString)){
			Pattern pattern = Pattern.compile("[0-9]*");
			Matcher isNum = pattern.matcher(srcString);
			if( !isNum.matches() ){
				returnVal = false;
			}else{
				returnVal = true;
			}
		}else{
			returnVal = false;
		}
		return returnVal;
	}	
	
	/**
	 * @param srcString
	 *            源字符串
	 * @param replaceMap
	 *            变量对应表
	 * @return
	 */
	public static String replaceVariable(String srcString, Map<String, String> replaceMap) {
		replaceMap = formatMap(replaceMap);
		//replaceMap = new CaseInsensitiveMap(replaceMap);
		String resultStr = nvlString(srcString).trim();
		//List<String> subColumn = getSubColumn(resultStr);
		for (String name : replaceMap.keySet()) {
			resultStr = resultStr.replace("#{" + name + "}",nvlString(replaceMap.get(name)));
		}
		return resultStr;
	}
	/**
	 * @param srcString
	 *            源字符串
	 * @param replaceMap
	 *            变量对应表
	 * @return
	 */
	public static String replaceAllVariable(String srcString, Map<String, String> replaceMap) {
		replaceMap = formatMap(replaceMap);
		replaceMap = new CaseInsensitiveMap(replaceMap);
		String resultStr = nvlString(srcString).trim();
		List<String> subColumn = getSubColumn(resultStr);
		for (String name : subColumn) {
			resultStr = resultStr.replace("#{" + name + "}",nvlString(replaceMap.get(name)));
		}
		return resultStr;
	}

	/**
	 * Map的value是list类型转为'A','b'格式
	 * 
	 * @param jsonMap
	 * @return
	 */
	public static Map formatMap(Map jsonMap) {
		Map resMap = new HashMap();
		Set<String> set = jsonMap.keySet();
		for (String key : set) {
			if (jsonMap.get(key) instanceof String) {
				resMap.put(key, jsonMap.get(key));
			}
			if (jsonMap.get(key) instanceof List) {
				List list = (List) jsonMap.get(key);
				resMap.put(key, "'" + StringUtils.join(list, "','") + "'");
			}
		}
		return resMap;
	}

	public static String emptyConvert(Object obj, String target) {
		return isBlank(nvlString(obj)) ? target : nvlString(obj);
	}

	public static String nvlString(Object obj) {
		return obj == null ? EMPTY : String.valueOf(obj).trim();
	}

	/**
	 * 
	 * @param srcString
	 *            待替换的字符串 如: srcString "hello ${name},say ${word}"
	 * @param replaceString
	 *            替换的方式 如: replaceString "name=hyk","word=good" 注意是可变数组
	 * @return
	 */
	public static String replaceVariable(String srcString,
			String... replaceString) {
		String resultStr = srcString.trim();
		for (String str : replaceString) {
			String name = str.substring(0, str.indexOf("="));
			String value = str.substring(str.indexOf("=") + 1, str.length());
			resultStr = resultStr.replace("#{" + name + "}", value);
		}
		return resultStr;
	}

	/**
	 * MD5加密方法
	 * 
	 * @param src
	 *            源字符串
	 * @return
	 */
	public static String securityMD5(String src) {
		String resultString = null;
		if(src==null)return "";
		resultString = new String(src);
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(src.getBytes());
			byte[] digest = md.digest();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < digest.length; i++) {
				sb.append(Integer.toHexString((digest[i]) & 0xFF));
			}
			resultString = sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return resultString;
	}

	/**
	 * bean 转string 方便调试使用
	 * 
	 * @param obj
	 * @return
	 */
	public static Map beanToString(Object obj) {
		try {
			return BeanUtils.describe(obj);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String substring(Object str,int start,int end){
		return substring(nvlString(str), start, end);
	}
	
	public static Date convertStrToDate(String strdate){
		if(strdate==null)return null;
	    strdate = strdate.replaceAll("\\W", "");
	    if(StringUtils.isNumeric(strdate)){
			List<String> list =new ArrayList();
			list.add("yyyyMMddHHmmss");
			list.add("yyyyMMddHHmm");
			list.add("yyyyMMddHH");
			list.add("yyyyMMdd");
			list.add("yyyyMM");
			list.add("yyyy");
		for (String format : list) {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			try {
				Date parse = sdf.parse(strdate);
				return parse;
			} catch (Exception e) {
				// TODO Auto-generated catch block
			}
	    }
	    }
		return null;
	}
	/**
	 * @param date
	 * @return
	 */
	public static String convertDateToStrUTC(Date date,String target){
		if(date==null){
			return target;
		}
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		String format = sdf.format(date);
		return format;
	}
	
	public static String findStrByReg (String findStr,String regEx)
	{
	    Pattern pat = Pattern.compile(regEx);  
	    Matcher mat = pat.matcher(findStr);
	    String str="";
	    while(mat.find())
	    {
	    	str+=mat.group();
	    }
	    return str;
	}
	public static List getSubColumn(String str){
		List list = new ArrayList();
		int index = 0;
		while(true){
			if(str.indexOf("#{",index)==-1){
				break;
			}
			int start = str.indexOf("#{",index);
			int end = str.indexOf("}",index);
			index = end+1;
			String substring = str.substring(start+2,end);
			list.add(substring);
		}
		return list;
	}
	public static String replaceSql(String sql,List<String> columnList){
		for (String field : columnList) {
			sql = sql.replace("#{"+field+"}", "?");
		}
		return sql;
	}
	
	public static String dateTimeProcessor(String dateStr,boolean isStartTime){
		String returnDateStr = null;
		if(dateStr.length()==8){
			if(isStartTime){
				dateStr = dateStr + "000000";
			}else{
				dateStr = dateStr + "235959";
			}
          }  
    	Date date = StringUtils.convertStrToDate(dateStr);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        if (date != null){
        	returnDateStr = sdf.format(date);
        }
       return returnDateStr;
	}
	/**压缩字符串
	 * @param str
	 * @return
	 * @throws Exception
	 */
	public static String compress(String str) throws Exception{
		if(str ==null || str.length()==0){
			return str;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(out);
		gzip.write(str.getBytes());
		gzip.close();
		out.toByteArray();
		return out.toString("ISO-8859-1");
	}
	
	public static byte[] compressToByte(String str) throws Exception{
		if(str ==null || str.length()==0){
			return null;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(out);
		gzip.write(str.getBytes());
		gzip.close();
		return out.toByteArray();
	}
	
	public static String uncompressToByte(byte[] b) throws Exception{
		if(b ==null ){
			return null;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayInputStream in = new ByteArrayInputStream(b);
		GZIPInputStream ip = new GZIPInputStream(in);
		byte[] buffer = new byte[2560];
		int n;
		while((n=ip.read(buffer))>=0){
			out.write(buffer,0,n);
		}
		return new String(out.toByteArray(),"GBK");
	}
	
	
	/**解压字符串
	 * @param str
	 * @return
	 * @throws Exception
	 */
	public static String uncompress(String str) throws Exception{
		if(str==null||str.length()==0)
		{
			return str;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes("ISO-8859-1"));
		GZIPInputStream ip = new GZIPInputStream(in);
		byte[] buffer = new byte[2560];
		int n;
		while((n=ip.read(buffer))>=0){
			out.write(buffer,0,n);
		}
		return out.toString();
   }
	
	public static int stringToSeconds(String date_time){
		int second = 0 ;
		int index_h = date_time.indexOf('时');
		int index_m = date_time.indexOf('分');
		int index_s = date_time.indexOf('秒');
		if(index_h>0 && index_m>0 && index_s>0){
			second= Integer.parseInt(date_time.substring(0,index_h))*3600+Integer.parseInt(date_time.substring(index_h+1,index_m-index_h+1))*60+Integer.parseInt(date_time.substring(index_m+1,index_s))*1;
		}
		else if(index_h<0 && index_m>0 && index_s>0){
			second = Integer.parseInt(date_time.substring(0,index_m))*60+Integer.parseInt(date_time.substring(index_m+1,index_s))*1;
		}
		else if(index_h>0 && index_m<0 && index_s>0){
			second = Integer.parseInt(date_time.substring(0,index_h))*3600+Integer.parseInt(date_time.substring(index_h+1,index_s))*1;
		}
		else if(index_h>0 && index_m>0 && index_s<0){
			second = Integer.parseInt(date_time.substring(0,index_h))*3600+Integer.parseInt(date_time.substring(index_h+1,index_m))*60;
		}
		else if(index_h>0 && index_m<0 && index_s<0){
			second = Integer.parseInt(date_time.substring(0,index_h))*3600;
		}
		else if(index_h<0 && index_m>0 && index_s<0){
			second = Integer.parseInt(date_time.substring(0,index_m))*60;
		}
		else if(index_m<0 && index_h<0 && index_s>0){
			second = Integer.parseInt(date_time.substring(0,index_s))*1;
		}else{
			second =Integer.parseInt(date_time)*1;
		}
		return second;
	}
	public static Date stringToDate(String time,boolean isStartTime){ 
		if(StringUtils.nvlString(time).length()<=4){
			return null;
		}
	    SimpleDateFormat formatter; 
	    time=time.trim() ; 
	    formatter = new SimpleDateFormat ("yyyy.MM.dd G 'at' hh:mm:ss z"); 
	    int tempPos=time.indexOf("-"); 
	    if((time.indexOf("/")>-1) &&(time.indexOf(" ")>-1)){ 
	      formatter = new SimpleDateFormat ("yyyy/MM/dd HH:mm:ss"); 
	    } 
	    else if((time.indexOf("-")>-1) &&(time.indexOf(" ")>-1)){ 
	      formatter = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss"); 
	    } 
	    else if((time.indexOf("/")>-1) &&(time.indexOf("am")>-1) ||(time.indexOf("pm")>-1)){ 
	      formatter = new SimpleDateFormat ("yyyy-MM-dd KK:mm:ss a"); 
	    } 
	    else if((time.indexOf("-")>-1) &&(time.indexOf("am")>-1) ||(time.indexOf("pm")>-1)){ 
	      formatter = new SimpleDateFormat ("yyyy-MM-dd KK:mm:ss a"); 
	    }else if((time.indexOf("/")==3) &&(time.lastIndexOf("/")==7) ||((time.lastIndexOf("/")==8))){ 
	    	formatter = new SimpleDateFormat ("yyyy/MM/dd"); 
	    }else{
	    	time = time.replaceAll("\\W", "");
	    	System.out.println("time::::::::::::::"+time);
	    	if(time.length()==10){
	    		formatter = new SimpleDateFormat ("MMddHHmmss"); 
			}else if(time.length()==4){
				if(isStartTime){
					time = time + "0101000000";
				}else{
					time = time + "1231235959";
				}
				formatter = new SimpleDateFormat ("yyyyMMddHHmmss"); 
			}else if(time.length()==6){
				if(isStartTime){
					time = time + "01000000";
				}else{
					time = time + "31235959";
				}
				formatter = new SimpleDateFormat ("yyyyMMddHHmmss"); 
			} if(time.length()==8){
				if(isStartTime){
					time = time + "000000";
				}else{
					time = time + "235959";
				}
				formatter = new SimpleDateFormat ("yyyyMMddHHmmss"); 
			}else if(time.length()==10){
				if(isStartTime){
					time = time + "0000";
				}else{
					time = time + "5959";
				}
				formatter = new SimpleDateFormat ("yyyyMMddHHmmss"); 
			}else if(time.length()==12){
				if(isStartTime){
					time = time + "00";
				}else{
					time = time + "59";
				}
				formatter = new SimpleDateFormat ("yyyyMMddHHmmss"); 
			}else if(time.length()==14){
				formatter = new SimpleDateFormat ("yyyyMMddHHmmss"); 
			}
	    }
	    ParsePosition pos = new ParsePosition(0); 
	    Date ctime = formatter.parse(time, pos);

	    return ctime; 
	}

	public static String getIp(){
		boolean isWindows=isWindowsOS();  //判断是否是windows系统
	    String ip="";
		try {
			if(isWindows){
			   InetAddress address=InetAddress.getLocalHost();
			   ip=address.getHostAddress();  //获取ip地址
			   System.out.println("windows ip地址："+ip);
			}else{    //如果是Linux系统
			   ip=getLinuxIP();
			   System.out.println("linux ip地址："+ip);
   }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ip;
	}
	public static boolean isWindowsOS(){
	    boolean isWindowsOS = false;
	    String osName = System.getProperty("os.name");
	    if(osName.toLowerCase().indexOf("windows")>-1){
	      isWindowsOS = true;
	    }
	    return isWindowsOS;
	 }

	public static String getLinuxIP() {
		String sIP = "";
		InetAddress ip = null;
		try {
			boolean bFindIP = false;
			Enumeration<NetworkInterface> netInterfaces = (Enumeration<NetworkInterface>) NetworkInterface
					.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()) {
				if (bFindIP) {
					break;
				}
				NetworkInterface ni = (NetworkInterface) netInterfaces
						.nextElement();

				Enumeration<InetAddress> ips = ni.getInetAddresses();
				while (ips.hasMoreElements()) {
					ip = (InetAddress) ips.nextElement();
					if (!ip.isLoopbackAddress()
							&& ip.getHostAddress().matches(
							"(\\d{1,3}\\.){3}\\d{1,3}")) {
						bFindIP = true;
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (null != ip) {
			sIP = ip.getHostAddress();
		}
		return sIP;
	}

	/**
	 * 全角转半角
	 * 全角空格为12288, 半角空格为32, 其他字符半角(33-126)与全角(65281-65374)的对应关系是:均相差65248
	 * @param input
	 * @return
	 */
	public static String toDBC(String input){
		char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if(c[i]==12288){
				c[i] = (char) 32;
				continue;
			}
			if(c[i]>65280 && c[i]<65375){
				c[i] = (char) (c[i]-65248);
			}
		}
		return new String(c);
	}
	
	 public static String cutLastZero(String src){
		 int length = src.length();
		 if(length <= 2){
			 src = src.replace("[0]*$", "");
		 }else{
			 src = src.replaceAll("[0]{2}$", "")
					 .replaceAll("[0]{2}$", "")
					 .replaceAll("[0]{2}$", "")
					 .replaceAll("[0]{2}$", "")
					 .replaceAll("[0]{2}$", "");
		 }
		 return src;
	 }
	 /**
	     * 获取当前GXS的层级用于检索下级数据
	     * 待完善...
	     * @param gxs
	     * @return
	     */
	    public static String getLikeGxs(String gxs){
			if(StringUtils.isNotBlank(gxs)){
				if("110000".equals(gxs.substring(0, 6))||"120100".equals(gxs.substring(0, 6))||"310000".equals(gxs.substring(0, 6))||"500000".equals(gxs.substring(0, 6))||"120000".equals(gxs.substring(0, 6))){//如果为直辖市
					gxs=gxs.substring(0, 2);
				}else if("0100".equals(gxs.substring(0, 4))){//如果为部级
					
				}else if(("11".equals(gxs.substring(0, 2))||"12".equals(gxs.substring(0, 2))||"31".equals(gxs.substring(0, 2))||"50".equals(gxs.substring(0, 2)))&&!"0000".equals(gxs.substring(2,6))){//如果为直辖市分局
					gxs=gxs.substring(0, 6);
				}else if((!"11".equals(gxs.substring(0, 2))&&!"12".equals(gxs.substring(0, 2))&&!"31".equals(gxs.substring(0, 2))&&!"50".equals(gxs.substring(0, 2)))&&"0000".equals(gxs.substring(2, 6))){//如果为省级
					gxs=gxs.substring(0, 2);
				}else if((!"11".equals(gxs.substring(0, 2))&&!"12".equals(gxs.substring(0, 2))&&!"31".equals(gxs.substring(0, 2))&&!"50".equals(gxs.substring(0, 2)))&&"00".equals(gxs.substring(4,6))){//如果为市级
					gxs=gxs.substring(0, 4);
				}else {//如果为县级
					gxs=cutLastZero(gxs);
				}
			}
			return gxs;
		}
	 public static String getBirthdateByAge(String age,boolean isLow){
		 int year = Calendar.getInstance().get(Calendar.YEAR);
		 String str = null;
		 str = StringUtils.nvlString(year-Integer.parseInt(age));
		 if(isLow){
			 str += "0101000000";
		 }else{
			 str += "1231235959";
		 }
		 return str;
	 }
}
