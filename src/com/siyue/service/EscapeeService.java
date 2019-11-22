package com.siyue.service;

import com.actionsoft.bpms.bo.engine.BO;
import com.actionsoft.bpms.bpmn.engine.model.run.delegate.ProcessInstance;
import com.actionsoft.bpms.commons.database.RowMap;
import com.actionsoft.bpms.org.model.UserModel;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.actionsoft.sdk.local.api.BOAPI;
import com.actionsoft.sdk.local.api.ProcessAPI;
import com.google.common.collect.Maps;
import com.siyue.util.ResourceUtils;
import com.siyue.util.SQLUtils;
import com.siyue.util.SequenceUtils;
import com.siyue.util.StringUtils;
import org.apache.curator.shaded.com.google.common.collect.Lists;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 逃犯线索
 */
public class EscapeeService extends BaseService {

    /**
     * 新增逃犯线索
     */
    public void addEscapee(){
        List<RowMap> rows = DBSql.getMaps(ResourceUtils.getSQL("select_ztryxx"));
        if(rows != null && rows.size() > 0 ) {
            for(RowMap rowMap : rows) {
                Connection connection = DBSql.open();
                try {
                    connection.setAutoCommit(false);
                    UserContext userContext = UserContext.fromUID("admin");
                    //信息登记单位
                    String xxdjdw_gajgjgdm = rowMap.getString("XXDJDW_GAJGJGDM");
                    //户籍地址行政区划代 码
                    String hjdz_xzqhdm = rowMap.getString("HJDZ_XZQHDM");
                    //立案单位代码
                    String ladw_gajgjgdm = rowMap.getString("LADW_GAJGJGDM");
                    //证件号码
                    String zjhm = rowMap.getString("ZJHM");
                    //在逃人员编号
                    String ztrybh = rowMap.getString("ZTRYBH");
                    //根据在逃人员编号判断是否已生成线索
                    Map<String,Object> paramsMap = Maps.newHashMap();
                    paramsMap.put("tf.ZTRYBH",ztrybh);
                    String sql = ResourceUtils.getSQL("select_xsgltfxx");
                    RowMap xsgltfMap = DBSql.getMap(sql+SQLUtils.spellWhere(paramsMap));
                    String xsbh = "";
                    String xsmc = "";
                    if(xsgltfMap != null){
                        xsbh = xsgltfMap.getString("XSBH");
                        xsmc = xsgltfMap.getString("XSMC");
                    }else{
                        //创建线索
                        xsbh = SequenceUtils.getSequenceVal("SEQ_BO_EU_XSHB_XS", "X", "330000050000", "yyyymm", 23);
                        xsmc = "关于逃犯" + rowMap.getString("XM") + "的线索";
                        BO xsBo = new BO();
                        xsBo.set("GXSJ", new Date());
                        xsBo.set("XSBH", xsbh);
                        xsBo.set("XSMC", xsmc);
                        xsBo.set("XSLXDM", "03");
                        xsBo.set("XSMS", xsmc);
                        xsBo.set("SFSMDM", "01");
                        xsBo.set("XSJBDM", "01");
                        xsBo.setAll(super.createUserMap4DB(userContext, new String[]{"XXDJ"}));
                        SDK.getBOAPI().createDataBO("BO_EU_XSHB_XS", xsBo, userContext, connection);
                        //创建线索关联逃犯
                        BO gltfBo = new BO();
                        gltfBo.set("ZTRYBH", ztrybh);
                        gltfBo.set("XSBH", xsbh);
                        gltfBo.setAll(super.createUserMap4DB(userContext, new String[]{"XXDJ"}));
                        SDK.getBOAPI().createDataBO("BO_EU_XSHB_GLTF", gltfBo, userContext, connection);
                    }
                    //线索流转表
                    BO xslzBO = null;
                    String rwbh = SequenceUtils.getSequenceVal("SEQ_BO_EU_XSHB_RW_XF", "R", "330000050000", "yyyymm", 23);
                    List<BO> xslzLBOist = Lists.newArrayList();
                    List<String> userLists = Lists.newArrayList();
                    if (!xxdjdw_gajgjgdm.startsWith("33") && hjdz_xzqhdm.startsWith("33")) {
                        xslzBO = new BO();
                        String code = zjhm.substring(0, 6) + "050000";
                        xslzBO.set("JSDW_GAJGJGDM", code);
                        Map<String, Object> map = Maps.newHashMap();
                        map.put("CODE", code);
                        RowMap dwMap = DBSql.getMap(ResourceUtils.getSQL("select_gxsxx") + SQLUtils.spellWhere(map));
                        if (dwMap != null) {
                            xslzBO.set("JSDW_GAJGMC", dwMap.getString("NAME"));
                        }
                        xslzBO.set("JSDXLXDM", "02");
                        List<RowMap> userList = DBSql.getMaps(ResourceUtils.getSQL("select_user"), new Object[]{code});
                        List<String> list = com.google.common.collect.Lists.newArrayList();
                        for (RowMap r : userList) {
                            String userid = r.getString("USERID");
                            list.add(userid);
                        }
                        String user = StringUtils.join(list, " ");
                        userLists.add(user);
                        xslzLBOist.add(xslzBO);
                    } else if (xxdjdw_gajgjgdm.startsWith("33")) {
                        xslzBO = new BO();
                        xslzBO.set("JSDW_GAJGJGDM", xxdjdw_gajgjgdm);
                        Map<String, Object> map = Maps.newHashMap();
                        map.put("CODE", xxdjdw_gajgjgdm);
                        RowMap dwMap = DBSql.getMap(ResourceUtils.getSQL("select_gxsxx") + SQLUtils.spellWhere(map));
                        if (dwMap != null) {
                            xslzBO.set("JSDW_GAJGMC", dwMap.getString("NAME"));
                        }
                        xslzBO.set("JSDXLXDM", "02");
                        List<RowMap> userList = DBSql.getMaps(ResourceUtils.getSQL("select_user"), new Object[]{xxdjdw_gajgjgdm});
                        List<String> list = Lists.newArrayList();
                        for (RowMap r : userList) {
                            String userid = r.getString("USERID");
                            list.add(userid);
                        }
                        String user = StringUtils.join(list, " ");
                        userLists.add(user);
                        xslzLBOist.add(xslzBO);
                    }
                    if (!ladw_gajgjgdm.startsWith("33")) {
                        xslzBO = new BO();
                        xslzBO.set("JSDW_GAJGJGDM", hjdz_xzqhdm + "050000");
                        Map<String, Object> map = Maps.newHashMap();
                        map.put("CODE", hjdz_xzqhdm + "050000");
                        RowMap dwMap = DBSql.getMap(ResourceUtils.getSQL("select_gxsxx") + SQLUtils.spellWhere(map));
                        if (dwMap != null) {
                            xslzBO.set("JSDW_GAJGMC", dwMap.getString("NAME"));
                        }
                        xslzBO.set("JSDXLXDM", "02");
                        List<RowMap> userList = DBSql.getMaps(ResourceUtils.getSQL("select_user"), new Object[]{hjdz_xzqhdm + "050000"});
                        List<String> list = com.google.common.collect.Lists.newArrayList();
                        for (RowMap r : userList) {
                            String userid = r.getString("USERID");
                            list.add(userid);
                        }
                        String user = StringUtils.join(list, " ");
                        userLists.add(user);
                        xslzLBOist.add(xslzBO);
                    }
                    if (!xslzLBOist.isEmpty()) {
                        //创建下发任务
                        BO xfBo = new BO();
                        xfBo.set("RWMC", xsmc);
                        xfBo.set("RWBH", rwbh);
                        xfBo.set("SFSMDM", "01");
                        xfBo.set("RWMS", xsmc);
                        xfBo.set("SFFK_PDBZ", "1");
                        xfBo.set("RWZTDM", "01");
                        xfBo.set("XFLXDM", "03");
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DATE, 30);
                        Date date = calendar.getTime();
                        xfBo.set("FKQX", date);
                        xfBo.set("JJCDDM", "A");
                        xfBo.setAll(super.createUserMap4DB(userContext, new String[]{"XF"}));
                        SDK.getBOAPI().createDataBO("BO_EU_XSHB_RW_XF", xfBo, userContext, connection);
                        //创建任务关联线索
                        BO rwglxsBo = new BO();
                        rwglxsBo.set("RWBH", rwbh);
                        rwglxsBo.set("XSBH", xsbh);
                        rwglxsBo.setAll(super.createUserMap4DB(userContext, new String[]{"XXDJ"}));
                        SDK.getBOAPI().createDataBO("BO_EU_XSHB_RW_GLXS", rwglxsBo, userContext, connection);
                        //开启下发流程
                        for (int i=0;i<xslzLBOist.size();i++) {
                            String rwlzbh = SequenceUtils.getSequenceVal("SEQ_BO_EU_XSHB_RW_LZ", "", userContext.getUserModel().getDepartmentId(), "yyyymmdd", 30);
                            BO bo = xslzLBOist.get(i);
                            bo.set("RWLZBH",rwlzbh);
                            bo.set("RWBH", rwbh);
                            bo.setAll(super.createUserMap4DB(userContext, new String[]{"XXDJ"}));
                            String user = userLists.get(i);
                            ProcessAPI processAPI = SDK.getProcessAPI();
                            BOAPI boapi = SDK.getBOAPI();
                            ProcessInstance processInst = processAPI.createProcessInstance("obj_188b84fe70714d38ae3ecc0c5ac2bf6f", "admin", xsmc);
                            boapi.create("BO_EU_XSHB_RW_LZ", bo, processInst, userContext, connection);
                            processAPI.setVariable(processInst.getId(),"processBindId",processInst.getId());
                            processAPI.setVariable(processInst.getId(), "handleUser", user);
                            processAPI.start(processInst, null);
                        }
                    }
                    connection.commit();
                }catch (Exception e){
                    e.printStackTrace();
                    try {
                        connection.rollback();
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                }finally {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }
}
