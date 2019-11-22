package com.siyue.util;

import java.io.InputStream;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

public class XmlUtils {

	public static Document readXml(InputStream is) {
		Document document = null;
		SAXReader sx = new SAXReader();
		try {
			document = sx.read(is);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return document;
	}

	public static Document readXml(String xmlStr) {
		InputStream is = XmlUtils.class.getResourceAsStream(xmlStr);
		return readXml(is);
	}
}
