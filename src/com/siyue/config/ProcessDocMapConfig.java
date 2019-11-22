package com.siyue.config;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.dom4j.Document;

import com.actionsoft.apps.resource.AppContext;
import com.actionsoft.sdk.local.SDK;
import com.google.common.collect.Maps;
import com.siyue.util.StringUtils;
import com.siyue.util.XmlUtils;

public class ProcessDocMapConfig {

	private static final Map<String, Document> PROCESS_ALL_MAP = Maps.newHashMap();
	private static final String RESOURCE_BUNDLE_REPOSITORY_PROCESS_DIRECTORY = "repository/process";
	
	public static Map<String, Document> getProcessAllMap() {
		return PROCESS_ALL_MAP;
	}

	public static void init(AppContext context) {
		try {
			String directoryName = context.getPath() + RESOURCE_BUNDLE_REPOSITORY_PROCESS_DIRECTORY;
			loadProcessDoc(directoryName);
			addFileListener(directoryName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void addFileListener(String directoryName) {

		FileAlterationObserver observer = new FileAlterationObserver(directoryName,new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return true;
			}
		});

		observer.addListener(new FileAlterationListenerAdaptor() {
			@Override
			public void onFileChange(File file) {
				try {
					String absolutePath = file.getAbsolutePath();
					String extension = FilenameUtils.getExtension(absolutePath);
					
					if (StringUtils.countMatches(file.getName(), "_") == 1 && "bpmn".equals(extension)) {
						SDK.getLogAPI().consoleInfo("检测到"+file.getName()+"文件修改,已重新加载");
						loadProcessDoc(absolutePath);
					}
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

	private static void loadProcessDoc(String pathname) {
		File[] listFiles = new File(pathname).listFiles();
		if (listFiles == null) {
			return;
		}
		for (File file : listFiles) {
			if (file.isFile()) {
				String absolutePath = file.getAbsolutePath();
				String extension = FilenameUtils.getExtension(absolutePath);
				if (file.getName().contains("_") && "bpmn".equals(extension)) {
					String processDefId = FilenameUtils.getBaseName(file.getAbsolutePath());
					try {
						InputStream is = new FileInputStream(absolutePath);
						Document doc = XmlUtils.readXml(is);
						PROCESS_ALL_MAP.put(processDefId, doc);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}
			} else if (file.isDirectory()) {
				loadProcessDoc(file.getPath());
			}
		}

	}
}
