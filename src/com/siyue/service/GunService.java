package com.siyue.service;

import com.actionsoft.bpms.bo.engine.BO;
import com.actionsoft.bpms.bpmn.engine.model.run.delegate.ProcessInstance;
import com.actionsoft.bpms.commons.database.RowMap;
import com.actionsoft.bpms.org.model.UserModel;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.server.conf.server.AWSServerConf;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.bpms.util.UtilFile;
import com.actionsoft.sdk.local.SDK;
import com.google.common.collect.Lists;
import com.siyue.util.ExcelUtils;
import com.siyue.util.SQLUtils;
import com.siyue.util.SequenceUtils;
import com.siyue.util.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.ByteArrayInputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 贩枪线索
 */
public class GunService extends BaseService{

    //电话号码正则表达式
    private static String REGEX = "^1(?:3\\d|4[4-9]|5[0-35-9]|6[67]|7[013-8]|8\\d|9\\d)\\d{8}$";

    private static final String dbSupply = AWSServerConf.getDatabaseProvider();

    public static boolean isValidDate(String str) {
        boolean convertSuccess = true;
        // 指定日期格式为四位年/两位月份/两位日期，注意yyyy/MM/dd区分大小写；
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 设置lenient为false.
        // 否则SimpleDateFormat会比较宽松地验证日期，比如2007/02/29会被接受，并转换成2007/03/01
        try {
            format.setLenient(false);
            format.parse(str);
        } catch (Exception e) {
            // 如果throw java.text.ParseException或者NullPointerException，就说明格式不对
            convertSuccess = false;
        }
        return convertSuccess;
    }

    private String getCellValue(Cell cell) {
        String stringCellValue;
        DecimalFormat format = new DecimalFormat("#");
        if(cell == null){
            stringCellValue = "";
        } else if (cell.getCellType() == cell.CELL_TYPE_BOOLEAN) {
            stringCellValue = String.valueOf(cell.getBooleanCellValue());
        } else if (cell.getCellType() == cell.CELL_TYPE_NUMERIC) {
            if (HSSFDateUtil.isCellDateFormatted(cell)) {
                Date date = cell.getDateCellValue();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                stringCellValue = simpleDateFormat.format(date);
            } else {
                stringCellValue = format.format(cell.getNumericCellValue());
//                stringCellValue = NumberFormat.getInstance().format(Float.parseFloat(String.valueOf(cell.getNumericCellValue()).trim()));
            }
        } else if (cell.getCellType() == cell.CELL_TYPE_BLANK) {
            stringCellValue = "";
        } else {
            stringCellValue = String.valueOf(cell.getStringCellValue());
        }
        return stringCellValue;
    }

    /**
     * 处理导入数据
     *
     * @param uuid
     * @param filePath
     * @param contrastMap
     * @return
     * @throws Exception
     */
    public List<BO> processImportData(UserContext userContext, String uuid, String filePath, Map<String, String> contrastMap, Map<String, String> paramsMap) throws Exception {
        UserModel userModel = userContext.getUserModel();
        //excel入库数据list
        List<BO> recordDatas = Lists.newArrayList();
        //excel表头序号list
        UtilFile tmpFile = new UtilFile(filePath);
        byte[] data = tmpFile.readBytes();

        Workbook workbook = ExcelUtils.readExcel(new ByteArrayInputStream(data));
        Sheet sheet = workbook.getSheetAt(0);

        int lastMergeRowNum = 0;
        //判断跨行
        int numMergedRegions = sheet.getNumMergedRegions();
        if (numMergedRegions > 0) {
            CellRangeAddress mergedRegion = sheet.getMergedRegion(numMergedRegions - 1);
            lastMergeRowNum = mergedRegion.getLastRow();
        }

        for (int rowNum = lastMergeRowNum + 2; rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null) {
                continue;
            }
            //判断整行是否为空
            int m = 0;
            for(;m < sheet.getRow(rowNum).getPhysicalNumberOfCells(); m++){
                String value = getCellValue(row.getCell(m)).trim();
                if(StringUtils.isNotBlank(value)){
                    break;
                }
            }
            if(m==sheet.getRow(rowNum).getPhysicalNumberOfCells()){
                continue;
            }
            //0,无效;1,有效
            int[] validateArr = {0, 0, 0, 0};
            BO bo = new BO();
            for (int cellNum = 0; cellNum < sheet.getRow(rowNum).getPhysicalNumberOfCells(); cellNum++) {
                Cell cell = row.getCell(cellNum);
                //循环要入库的列,并入库每个对应的字段
                if (contrastMap.containsKey(String.valueOf(cellNum))) {
                    String value = getCellValue(cell).trim();
                    String field = contrastMap.get(String.valueOf(cellNum));
                    bo.set(field, value);
                }
            }
            //关键信息数据过滤(收件地址)
            if (bo.containsKey("SJR_XZZ_DZMC")) {
                String value = StringUtils.nvlString(bo.get("SJR_XZZ_DZMC"));
                if (StringUtils.isNotBlank(value)) {
                    validateArr[0] = 1;
                }
            }
            //联系电话
            if (bo.containsKey("SJR_LXDH")) {
                String value = StringUtils.nvlString(bo.get("SJR_LXDH"));
                boolean matches = value.matches(REGEX);
                if (StringUtils.isNotBlank(value) && matches) {
                    validateArr[1] = 1;
                }
            }
            //地市验证
            String dwSql = "";
            if (bo.containsKey("SJR_CS")) {
                String cs_value = StringUtils.nvlString(bo.get("SJR_CS"));
                if (StringUtils.isNotBlank(cs_value)) {
                    validateArr[2] = 1;
                }
                dwSql="select DW_CODE from CODE_WLFQXDXZDWZH where SHIJ_NAME like '%"+StringUtils.nvlString(bo.get("SJR_CS"))+"%'";
                RowMap rowMap = DBSql.getMap(dwSql);
                if(rowMap!=null && !rowMap.isEmpty()){
                    String code = rowMap.getString("DW_CODE");
                    if(code.startsWith("33")){
                        bo.set("SJRQH_SSDW_GAJGJGDM",code.substring(0,4)+"00050000");
                        validateArr[3] = 1;
                    }
                }
            }
            String SFYX_PDBZ = "0";
            if(validateArr[2]==1 && validateArr[3] == 1){
                if(validateArr[0]==1 || validateArr[1]==1){
                    SFYX_PDBZ = "1";
                }
            }
            bo.set("KDXXPCH", uuid);
            bo.set("SFYX_PDBZ",SFYX_PDBZ);
            bo.set("SFCF_PDBZ","0");
            bo.set("XXDJDW_GAJGJGDM", userModel.getDepartmentId());
            bo.set("XXDJRY_XM", userModel.getUserName());
            bo.set("XXDJRY_GMSFHM", userModel.getUID());
            bo.set("XXDJRY_LXDH", userModel.getMobile());
            bo.setProcessDefId(paramsMap.get("processDefId"));
            bo.set("XSBH", SequenceUtils.getSequenceVal("SEQ_BO_EU_XS", "X", userModel.getDepartmentId(),"yyyymm", 23));
            //把这一行的值存入list
            recordDatas.add(bo);
        }
        recordDatas = validateRepeatData(recordDatas);
        return recordDatas;
    }

    /**
     * 保存数据
     *
     * @param userContext
     * @param recordDatas
     * @param processId
     * @return
     */
    public int[] saveData(UserContext userContext, List<BO> recordDatas, String processId) {
        ProcessInstance processInstance = SDK.getProcessAPI().getInstanceById(processId);
        return SDK.getBOAPI().create("BO_EU_XS", recordDatas, processInstance, userContext);
    }

    //重复数据验证
    public List<BO> validateRepeatData(List<BO> recordDatas) throws Exception {
        //0,不重复;1,重复
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //规则1:快递单号相同,则直接判定为重复数据(excel数据比较)
        List<String> wldhList = Lists.newArrayList();
        for (int i = 0; i < recordDatas.size(); i++) {
            BO bo = recordDatas.get(i);
            if ("1".equals(StringUtils.nvlString(bo.get("SFCF_PDBZ")))) {
                continue;
            }
            String wldh = StringUtils.nvlString(bo.get("WLDH"));
            if(StringUtils.isNotBlank(wldh)){
                wldhList.add(wldh);
            }
            for (int j = 0; j < recordDatas.size(); j++) {
                if (i == j) {
                    continue;
                }
                BO _bo = recordDatas.get(j);
                if ("1".equals(StringUtils.nvlString(_bo.get("SFCF_PDBZ")))) {
                    continue;
                }
                String _wldh = StringUtils.nvlString(_bo.get("WLDH"));
                if (StringUtils.isNotBlank(wldh) && StringUtils.isNotBlank(_wldh) && wldh.equals(_wldh)) {
                    //置为重复
                    _bo.set("SFCF_PDBZ", "1");
                }
            }
        }
        //规则1:数据库数据比较
        try {
            String sql = "";
            if("oracle".equalsIgnoreCase(dbSupply)){
                sql = "select listagg(WLDH,',') within group(order by XSBH) from BO_EU_TB_YW_XSHB_FQXS where WLDH in('" + StringUtils.join(wldhList, "','") + "') and SFYX_PDBZ = '1'";
            }else if("postgresql".equalsIgnoreCase(dbSupply)){
                sql = "select string_agg(WLDH,',') from BO_EU_TB_YW_XSHB_FQXS where WLDH in('" + StringUtils.join(wldhList, "','") + "') and SFYX_PDBZ = '1'";
            }
            String wldhs = DBSql.getString(sql);
            for (int i = 0; i < recordDatas.size(); i++) {
                BO bo = recordDatas.get(i);
                if ("1".equals(StringUtils.nvlString(bo.get("SFCF_PDBZ")))) {
                    continue;
                }
                String wldh = StringUtils.nvlString(bo.get("WLDH"));
                if (StringUtils.isNotBlank(wldhs) && StringUtils.isNotBlank(wldh) && wldhs.contains(wldh)) {
                    //置为重复
                    bo.set("SFCF_PDBZ", "1");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //规则2:如果快递单号为空或不同,则判定买家信息一致情况下且交易时间符合要求,则判定重复
        for (int i = 0; i < recordDatas.size(); i++) {
            BO bo = recordDatas.get(i);
            if ("1".equals(StringUtils.nvlString(bo.get("SFCF_PDBZ")))) {
                continue;
            }
            String sjr_xm = StringUtils.nvlString(bo.get("SJR_XM"));
            String sjr_xzz_dzmc = StringUtils.nvlString(bo.get("SJR_XZZ_DZMC"));
            String sjr_lxdh = StringUtils.nvlString(bo.get("SJR_LXDH"));
            String ddsj = StringUtils.nvlString(bo.get("DDSJ"));

            for (int j = 0; j < recordDatas.size(); j++) {
                if (i == j) {
                    continue;
                }
                BO _bo = recordDatas.get(j);
                if ("1".equals(StringUtils.nvlString(_bo.get("SFCF_PDBZ")))) {
                    continue;
                }
                String sjr_xm2 = StringUtils.nvlString(_bo.get("SJR_XM"));
                String sjr_xzz_dzmc2 = StringUtils.nvlString(_bo.get("SJR_XZZ_DZMC"));
                String sjr_lxdh2 = StringUtils.nvlString(_bo.get("SJR_LXDH"));
                String ddsj2 = StringUtils.nvlString(_bo.get("DDSJ"));
                if (StringUtils.isNotBlank(sjr_xm) && StringUtils.isNotBlank(sjr_xm2) && sjr_xm.equals(sjr_xm2) && StringUtils.isNotBlank(sjr_xzz_dzmc) && StringUtils.isNotBlank(sjr_xzz_dzmc2) && sjr_xzz_dzmc.equals(sjr_xzz_dzmc2) && StringUtils.isNotBlank(sjr_lxdh) && StringUtils.isNotBlank(sjr_lxdh2) && sjr_lxdh.equals(sjr_lxdh2)) {
                    if (StringUtils.isNotBlank(ddsj) && StringUtils.isNotBlank(ddsj2) && isValidDate(ddsj) && isValidDate(ddsj2)) {
                        double hourNum = Math.abs(simpleDateFormat.parse(ddsj).getTime() - simpleDateFormat.parse(ddsj2).getTime()) / (1000 * 60 * 60);
                        if (hourNum <= 24) {
                            //置为重复
                            _bo.set("SFCF_PDBZ", "1");
                        }
                    }
                }
            }
        }
        //规则3:买家信息和交易信息中,有两条内容相同则可以认定该分组信息一致
        for (int i = 0; i < recordDatas.size(); i++) {
            BO bo = recordDatas.get(i);
            if ("1".equals(StringUtils.nvlString(bo.get("SFCF_PDBZ")))) {
                continue;
            }
            String sjr_xm = StringUtils.nvlString(bo.get("SJR_XM"));
            String sjr_xzz_dzmc = StringUtils.nvlString(bo.get("SJR_XZZ_DZMC"));
            String sjr_lxdh = StringUtils.nvlString(bo.get("SJR_LXDH"));
            String sjsj = StringUtils.nvlString(bo.get("SHSJ"));
            String jysj = StringUtils.nvlString(bo.get("DDSJ"));
            String sdsj = StringUtils.nvlString(bo.get("SDSJ"));
            for (int j = 0; j < recordDatas.size(); j++) {
                if (i == j) {
                    continue;
                }
                BO _bo = recordDatas.get(j);
                if ("1".equals(StringUtils.nvlString(_bo.get("SFCF_PDBZ")))) {
                    continue;
                }
                //买家信息分组判断标志
                int mjxx = 0;
                //交易信息分组判断标志
                int jyxx = 0;
                String sjr_xm2 = StringUtils.nvlString(_bo.get("SJR_XM"));
                String sjr_xzz_dzmc2 = StringUtils.nvlString(_bo.get("SJR_XZZ_DZMC"));
                String sjr_lxdh2 = StringUtils.nvlString(_bo.get("SJR_LXDH"));
                String sjsj2 = StringUtils.nvlString(_bo.get("SHSJ"));
                String jysj2 = StringUtils.nvlString(_bo.get("DDSJ"));
                String sdsj2 = StringUtils.nvlString(_bo.get("SDSJ"));
                if (StringUtils.isNotBlank(sjr_xm) && StringUtils.isNotBlank(sjr_xm2) && sjr_xm.equals(sjr_xm2)) {
                    mjxx++;
                }
                if (StringUtils.isNotBlank(sjr_xzz_dzmc) && StringUtils.isNotBlank(sjr_xzz_dzmc2) && sjr_xzz_dzmc.equals(sjr_xzz_dzmc2)) {
                    mjxx++;
                }
                if (StringUtils.isNotBlank(sjr_lxdh) && StringUtils.isNotBlank(sjr_lxdh2) && sjr_lxdh.equals(sjr_lxdh2)) {
                    mjxx++;
                }
                if (mjxx >= 2) {
                    if (StringUtils.isNotBlank(sjsj) && StringUtils.isNotBlank(sjsj2) && isValidDate(sjsj) && isValidDate(sjsj2)) {
                        if (Math.abs(simpleDateFormat.parse(sjsj).getTime() - simpleDateFormat.parse(sjsj2).getTime()) / (1000 * 60 * 60) <= 24) {
                            jyxx++;
                        }
                    }
                    if (StringUtils.isNotBlank(jysj) && StringUtils.isNotBlank(jysj2) && isValidDate(jysj) && isValidDate(jysj2)) {
                        if (Math.abs(simpleDateFormat.parse(jysj).getTime() - simpleDateFormat.parse(jysj2).getTime()) / (1000 * 60 * 60) <= 24) {
                            jyxx++;
                        }
                    }
                    if (StringUtils.isNotBlank(sdsj) && StringUtils.isNotBlank(sdsj2) && isValidDate(sdsj) && isValidDate(sdsj2)) {
                        if (Math.abs(simpleDateFormat.parse(sdsj).getTime() - simpleDateFormat.parse(sdsj2).getTime()) / (1000 * 60 * 60) <= 24) {
                            jyxx++;
                        }
                    }
                    if (jyxx >= 2) {
                        //置为重复
                        _bo.set("SFCF_PDBZ", "1");
                    }
                }
            }
        }
        return recordDatas;
    }

    /**
     * 修改贩枪线索
     * @return
     */
    public int updateGun(UserContext userContext,Map<String,Object> paramsMap){
        paramsMap.putAll(super.createUserMap4DB(userContext,new String[]{"XXCZ"}));
        return DBSql.update(SQLUtils.getCreateUpdateSQL(paramsMap,"BO_EU_XSHB",new String[]{"ID"}));
    }

    /**
     * 删除贩枪线索
     * @return
     */
    public int deleteGun(UserContext userContext,Map<String,Object> paramsMap){
        return DBSql.update(SQLUtils.getCreateDeleteSQL(paramsMap,"BO_EU_XSHB"));
    }
}
