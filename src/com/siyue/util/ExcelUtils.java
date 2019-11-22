package com.siyue.util;

import java.io.InputStream;
import java.io.PushbackInputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.DocumentFactoryHelper;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtils {
	
	public static Workbook readExcel(InputStream is) {
		if (!is.markSupported()) {
			is = new PushbackInputStream(is, 8);
		}
		try {
			if (POIFSFileSystem.hasPOIFSHeader(is)) {
				return new HSSFWorkbook(is);
			} else if (DocumentFactoryHelper.hasOOXMLHeader(is)) {
				return new XSSFWorkbook(OPCPackage.open(is));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new IllegalArgumentException("您的excel版本目前POI解析不了");
	}
}
