package com.siyue.util;

import java.io.IOException;

import org.dom4j.Document;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.siyue.config.ProcessDocMapConfig;
import com.siyue.config.SQLMapConfig;

public class ResourceUtils {

	/**
	 * 根据给定路径获取资源
	 * 
	 * @param path
	 * @return
	 */
	public static Resource[] getPathResource(String path) {
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = null;
		try {
			resources = resolver.getResources(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resources;
	}

	public static String getSQL(String key) {
		return SQLMapConfig.getSQLAllMap().get(key);
	}
	
	public static Document getProcessDoc(String processDefId) {
		return ProcessDocMapConfig.getProcessAllMap().get(processDefId);
	}
}
