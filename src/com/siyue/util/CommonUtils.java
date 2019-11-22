package com.siyue.util;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * 各种通用工具类
 */
public class CommonUtils {
	
	/**
	 * 按给定数量，把List分为多个小份
	 * @param list
	 * @param rows
	 * @return
	 */
	public static <T> List<List<T>> cutList(List<T> list,int rows){
		if(list == null || rows == 0){
			return Collections.emptyList();
		}
		List<List<T>> resultList = Lists.newArrayList();
		int batchCount = (list.size() % rows == 0) ? (list.size() / rows) : ((list.size() / rows) + 1);
		for (int i = 0; i < batchCount; i++) {
			int startIndex = i * rows;
			int endIndex = (i + 1) * rows;
			if (i == batchCount - 1) {
				endIndex = list.size();
			}
			List<T> subList = list.subList(startIndex, endIndex);
			resultList.add(subList);
		}
		return resultList;
	}
}
