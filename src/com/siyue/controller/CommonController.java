package com.siyue.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.commons.database.RowMap;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.server.bind.annotation.Controller;
import com.actionsoft.bpms.server.bind.annotation.Mapping;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.siyue.service.CommonService;
import com.siyue.util.SequenceUtils;
import com.siyue.util.StringUtils;
import com.siyue.vo.RemoteResult;

/**
 * Created by Administrator on 2019/1/3.
 */
@Controller
public class CommonController {

	private CommonService commonService = new CommonService();

	/**
	 * 生成业务编号
	 * 
	 * @param userContext
	 * @param json
	 * @return
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.generatorBizNumber")
	public String generatorBizNumber(UserContext userContext, Object json) {
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> jsonMap = (Map<String, Object>) JSON.parse((String) json);
			String bizNumber = SequenceUtils.getSequenceVal(StringUtils.nvlString(jsonMap.get("seqName")), StringUtils.nvlString(jsonMap.get("prefix")), userContext.getUserModel().getDepartmentId(), StringUtils.nvlString(jsonMap.get("dateFormat")), Integer.valueOf(StringUtils.nvlString(jsonMap.get("seqLength"))));
			Map<String, Object> resultMap = Maps.newHashMap();
			resultMap.put("bizNumber", bizNumber);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
		} catch (Exception e) {
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("发生异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 查询已关联线索
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getRelevanceClue")
	public String getRelevanceClue(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = commonService.getRelevanceClue(userContext, paramsMap, currPage, pageSize, sortField, sortType);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 查询外部平台任务关联的串并信息
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getRelevanceCBByOther")
	public String getRelevanceCBByOther(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = commonService.getRelevanceCBByOther(userContext, paramsMap, currPage, pageSize, sortField, sortType);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 查询外部平台任务关联的案件
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getRelevanceAJByOther")
	public String getRelevanceAJByOther(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = commonService.getRelevanceAJByOther(userContext, paramsMap, currPage, pageSize, sortField, sortType);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 新增串并业务
	 *
	 * @param userContext
	 * @param json
	 * @return
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.addSeriesBiz")
	public String addSeriesBiz(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSON.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		RemoteResult remoteResult = new RemoteResult();
		Map<String, Object> returnMap = Maps.newHashMap();
		try {
			int batch[] = commonService.addSeriesBiz(userContext, paramsMap, returnMap);
			if (!Arrays.asList(batch).contains(0)) {
				remoteResult.setResultMsg("新增成功！");
				remoteResult.setResultData(returnMap);
				remoteResult.setStatus(true);
			} else {
				remoteResult.setResultMsg("新增失败！");
				remoteResult.setStatus(false);
			}
		} catch (Exception e) {
			remoteResult.setResultMsg("新增异常！");
			remoteResult.setStatus(false);
		}
		return remoteResult.toString();
	}

	/**
	 * 删除串并业务
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.deleteSeriesBiz")
	public String deleteSeriesBiz(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		RemoteResult remoteResult = new RemoteResult();
		try {
			int delete = commonService.deleteSeriesBiz(userContext, paramsMap);
			if (delete > 0) {
				remoteResult.setResultMsg("删除成功！");
				remoteResult.setStatus(true);
			} else {
				remoteResult.setResultMsg("删除失败！");
				remoteResult.setStatus(false);
			}
		} catch (Exception e) {
			remoteResult.setResultMsg("删除异常！");
			remoteResult.setStatus(false);
		}
		return remoteResult.toString();
	}

	/**
	 * 查询全部案件
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getAllCase")
	public String getAllCase(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = commonService.getAllCase(userContext, paramsMap, currPage, pageSize, sortField, sortType);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 查询全部人员
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getAllPerson")
	public String getAllPerson(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = commonService.getAllPerson(userContext, paramsMap, currPage, pageSize, sortField, sortType);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 查询全部物品
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getAllGoods")
	public String getAllGoods(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = commonService.getAllGoods(userContext, paramsMap, currPage, pageSize, sortField, sortType);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 查询线索关联业务
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getAllGywByCle")
	public String getAllGywByCle(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = commonService.getAllGywByCle(userContext, paramsMap, currPage, pageSize, sortField, sortType);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 查询全部在逃人员
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getAllEscapee")
	public String getAllEscapee(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = commonService.getAllEscapee(userContext, paramsMap, currPage, pageSize, sortField, sortType);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 查询已串并人员
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getSeriesPerson")
	public String getSeriesPerson(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = commonService.getSeriesPerson(userContext, paramsMap, currPage, pageSize, sortField, sortType);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 查询已串并案件
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getSeriesCase")
	public String getSeriesCase(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = commonService.getSeriesCase(userContext, paramsMap, currPage, pageSize, sortField, sortType);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 已关联串并信息
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getSeriesCb")
	public String getSeriesCb(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = commonService.getSeriesCb(userContext, paramsMap, currPage, pageSize, sortField, sortType);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 查询下级接收单位
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getSubReceiveUnit")
	public String getSubReceiveUnit(UserContext userContext) {
		RemoteResult remoteResult = new RemoteResult();
		try {
			List<RowMap> resultList = commonService.getSubReceiveUnit(userContext);
			remoteResult.setResultData(resultList);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 根据单位ID查询单位用户
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getDepartmentUser")
	public String getDepartmentUser(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		String gxs = StringUtils.nvlString(jsonMap.get("gxs"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			List<RowMap> resultList = commonService.getDepartmentUser(userContext, gxs);
			remoteResult.setResultData(resultList);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 获取附件URL
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getAttachmentDownloadURL")
	public String getAttachmentDownloadURL(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		RemoteResult remoteResult = new RemoteResult();
		try {
			String url = commonService.getAttachmentDownloadURL(userContext, paramsMap);
			remoteResult.setResultData(url);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 获取附件下载URL（ZIP
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.batchAttachmentDownloadURL")
	public String batchAttachmentDownloadURL(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		RemoteResult remoteResult = new RemoteResult();
		try {
			String url = commonService.batchAttachmentDownloadURL(userContext, paramsMap);
			remoteResult.setResultData(url);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 查询已串并线索
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getSeriesClue")
	public String getSeriesClue(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = commonService.getSeriesClue(userContext, paramsMap, currPage, pageSize, sortField, sortType);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 查询已串并物品
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getSeriesGoods")
	public String getSeriesGoods(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		Map<String, Object> paramsMap = (Map<String, Object>) jsonMap.get("data");
		int currPage = StringUtils.emptyConvert(jsonMap.get("page"), 1);
		int pageSize = StringUtils.emptyConvert(jsonMap.get("rows"), 10);
		String sortField = StringUtils.nvlString(jsonMap.get("sortField"));
		String sortType = StringUtils.nvlString(jsonMap.get("sortType"));
		RemoteResult remoteResult = new RemoteResult();
		try {
			Map<String, Object> resultMap = commonService.getSeriesGoods(userContext, paramsMap, currPage, pageSize, sortField, sortType);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 查询附件信息
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getRelevanceAttachment")
	public String getRelevanceAttachment(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		RemoteResult remoteResult = new RemoteResult();
		try {
			List<RowMap> list = commonService.getRelevanceAttachment(userContext, jsonMap);
			Map<String, Object> resultMap = Maps.newHashMap();
			resultMap.put("rows", list);
			remoteResult.setResultData(resultMap);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * <p>
	 * httpClient 信息采集
	 * </p>
	 * 
	 * @param userContext
	 * @param json
	 * @return
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.handAdd")
	public String handAddPerson(UserContext userContext, Object json) {
		RemoteResult remoteResult = new RemoteResult();
		try {
			int handAdd = commonService.handAdd(userContext, (String) json);
			if (1 == handAdd) {
				remoteResult.setResultMsg("新增成功！");
				remoteResult.setStatus(true);
			} else {
				remoteResult.setResultMsg("新增失败！");
				remoteResult.setStatus(false);
			}
		} catch (Exception e) {
			remoteResult.setResultMsg("新增异常！");
			remoteResult.setStatus(false);
		}
		return remoteResult.toString();
	}

	/**
	 * 查询单位下的人员
	 * 
	 * @param departmentid
	 * @return
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getOrguserMap")
	public String getOrguserMap(String departmentid) {
		RemoteResult remoteResult = new RemoteResult();
		try {
			List<RowMap> resultList = commonService.getUserByDepartmentid(departmentid);
			remoteResult.setResultData(resultList);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

	/**
	 * 查询code表
	 * 
	 * @param userContext
	 * @param json
	 * @return
	 */
	@Mapping(value = "com.awspaas.user.apps.clue.getCodeMap")
	public String getCodeMap(UserContext userContext, Object json) {
		Map<String, Object> jsonMap = (Map<String, Object>) JSONObject.parse((String) json);
		RemoteResult remoteResult = new RemoteResult();
		try {
			List<RowMap> resultList = commonService.selectCode(StringUtils.nvlString(jsonMap.get("table")));
			remoteResult.setResultData(resultList);
			remoteResult.setStatus(true);
			remoteResult.setResultMsg("查询成功！");
		} catch (Exception e) {
			e.printStackTrace();
			remoteResult.setStatus(false);
			remoteResult.setResultMsg("查询异常！");
		}
		return remoteResult.toString();
	}

}