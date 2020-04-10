package com.tsdata.model.factor.service.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.tsdata.model.factor.common.CommonUtil;
import com.tsdata.model.factor.common.HttpClientUtils;
import com.tsdata.model.factor.service.GetInterfaceInfoService;

import net.sf.json.JSONObject;
/**
 * 阿福共享版接口
 * @author 31716
 *
 */
@Service("AFuCreditHandler")
public class GetAFuCreditResultServiceImpl implements GetInterfaceInfoService{
	public static Logger logger = LoggerFactory.getLogger(GetAFuCreditResultServiceImpl.class);
	@Value("${AFu.url}")
	private String url;
	
	@Value("${Afu.userName}")
	private String userName;
	
	@Value("${Afu.sign}")
	private String sign;
	
	@Value("${Afu.queryReason}")
	private String queryReason;
	@Override
	public JSONObject getResult(JSONObject param) {
		logger.info(">>>>调用阿福共享版接口>>>>>");
		JSONObject reqData = new JSONObject();
		JSONObject params = new JSONObject();
		reqData.put("user_name", userName);
		reqData.put("sign", sign);
		reqData.put("query_reason", queryReason);
		reqData.put("api_name", "credit.evaluation.share.api");
		params.put("id_no", param.getString("id_no"));
		params.put("name", param.getString("name"));
		reqData.put("params", params);
		String result = HttpClientUtils.doPostForm(url, reqData);
        param.put("AFuCreditInfo",result);
		return param;
	}

}
