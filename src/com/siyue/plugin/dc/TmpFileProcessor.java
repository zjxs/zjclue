package com.siyue.plugin.dc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.curator.shaded.com.google.common.collect.Maps;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import com.actionsoft.bpms.server.fs.AbstFileProcessor;
import com.actionsoft.bpms.server.fs.DCContext;
import com.actionsoft.bpms.server.fs.FileProcessorListener;
import com.actionsoft.bpms.server.fs.dc.DCMessage;
import com.actionsoft.bpms.util.UtilFile;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.siyue.util.ExcelUtils;
import com.siyue.util.StringUtils;

public class TmpFileProcessor extends AbstFileProcessor implements FileProcessorListener {

	public void uploadSuccess(Map<String, Object> paramsMap) {
		DCContext context = (DCContext) paramsMap.get("DCContext");
		String filePath = context.getFilePath();
		UtilFile tmpFile = new UtilFile(filePath);
		// 模拟处理过程，比如用POI处理Excel文件
		byte[] data = tmpFile.readBytes();

		Map map = Maps.newHashMap();
		try {
			List<Map<String, Object>> columnsTitle = getColumnsTitle(data);
			map.put("columnsTitle", columnsTitle);
			map.put("filePath", tmpFile.getAbsolutePath());
			map.put("uuid", UUID.randomUUID().toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		context.setDCMessage(DCMessage.OK, JSONObject.toJSONString(map));
	}

	public List<Map<String, Object>> getColumnsTitle(byte[] data) throws IOException {
		List<Map<String, Object>> columnsList = Lists.newArrayList();
		// 解析excel表头
		Workbook workbook = ExcelUtils.readExcel(new ByteArrayInputStream(data));
		Sheet sheet = workbook.getSheetAt(0);

		int rowNum = 0;

		int numMergedRegions = sheet.getNumMergedRegions();
		if (numMergedRegions > 0) {
			CellRangeAddress mergedRegion = sheet.getMergedRegion(numMergedRegions - 1);
			int lastRow = mergedRegion.getLastRow();
			rowNum = lastRow + 1;
		}
		Row row = sheet.getRow(rowNum);

		for (int cellNum = 0; cellNum < row.getPhysicalNumberOfCells(); cellNum++) {
			Cell cell = row.getCell(cellNum);
			String stringCellValue = cell.getStringCellValue();
			if (StringUtils.isNotBlank(stringCellValue)) {
				Map<String, Object> columnMap = Maps.newHashMap();
				columnMap.put("value", cellNum);
				columnMap.put("label", stringCellValue);
				columnsList.add(columnMap);
			}
		}

		return columnsList;
	}
}
