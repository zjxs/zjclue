package com.siyue.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.dom4j.Document;
import org.dom4j.Element;

import com.actionsoft.apps.resource.AppContext;
import com.actionsoft.sdk.local.SDK;
import com.google.common.collect.Maps;
import com.siyue.util.StringUtils;
import com.siyue.util.XmlUtils;

public class SQLMapConfig {

	private static final Map<String, String> SQL_ALL_MAP = Maps.newHashMap();
	private static final String RESOURCE_BUNDLE_REPOSITORY_SQLMAP_DIRECTORY = "repository/sql";

	public static Map<String, String> getSQLAllMap() {
		return SQL_ALL_MAP;
	}

	public static void init(AppContext context) {
		try {
			String directoryName = context.getPath() + RESOURCE_BUNDLE_REPOSITORY_SQLMAP_DIRECTORY;
			loadSqlMapXml(directoryName);
			addFileListener(directoryName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void addFileListener(String directoryName) {

		FileAlterationObserver observer = new FileAlterationObserver(directoryName);

		observer.addListener(new FileAlterationListenerAdaptor() {
			@Override
			public void onFileChange(File file) {
				try {
					SDK.getLogAPI().consoleInfo("检测到sql文件修改,已重新加载");
					loadSqlMapXml(directoryName);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		// 注册观察者
		FileAlterationMonitor monitor = new FileAlterationMonitor(5000);
		monitor.addObserver(observer);
		try {
			// 启动监听
			monitor.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 根据xml加载sqlMap
	 * 
	 * @param appId
	 * @param document
	 */
	@SuppressWarnings("unchecked")
	private static void loadSqlMapXml(String filePath) {
		try {
			InputStream is = new FileInputStream(filePath + "/sqlMap.xml");
			Document doc = XmlUtils.readXml(is);
			List<Element> entryEl = doc.selectNodes("root/entry");
			for (Element el : entryEl) {
				String key = el.attributeValue("key");
				String sql = StringUtils.nvlString(el.selectSingleNode("value").getText());
				SQL_ALL_MAP.put(key, sql);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 根据给定的sqlKey获取Sql语句
	 * 
	 * @param sqlKey
	 * @return
	 */
	public static String getSQL(String key) {
		return SQL_ALL_MAP.get(key);
	}
}
