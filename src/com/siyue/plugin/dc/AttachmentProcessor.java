package com.siyue.plugin.dc;

import java.io.File;
import java.util.Map;

import com.actionsoft.bpms.server.fs.AbstFileProcessor;
import com.actionsoft.bpms.server.fs.DCContext;
import com.actionsoft.bpms.server.fs.FileProcessorListener;
import com.actionsoft.bpms.server.fs.dc.DCMessage;
import com.actionsoft.bpms.util.UtilFile;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;

public class AttachmentProcessor extends AbstFileProcessor implements FileProcessorListener {

    public void uploadSuccess(Map<String, Object> paramsMap) {
        DCContext context = (DCContext) paramsMap.get("DCContext");
        String filePath = context.getFilePath();
        UtilFile tmpFile = new UtilFile(filePath);
        String path = tmpFile.getAbsolutePath();
        //验重
//        String currentFileName = tmpFile.getName();
//        String parentPath = path.substring(0,path.lastIndexOf("\\"));
//        File files = new File(parentPath);
//        if(files.exists()){
//            File[] fileList = files.listFiles();
//            for(File file:fileList){
//                String name = file.getName();
//                if(currentFileName.equals(name)){
//                    //文件重复
//                    context.setDCMessage(DCMessage.ERROR, "文件重复,上传失败");
//                    return;
//                }
//            }
//        }
        Map<String,Object> resultMap = Maps.newHashMap();
        resultMap.put("filePath",path);
        context.setDCMessage(DCMessage.OK, JSONObject.toJSONString(resultMap));
    }
}
