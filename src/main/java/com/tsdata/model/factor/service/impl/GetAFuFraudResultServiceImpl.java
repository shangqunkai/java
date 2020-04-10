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
 * 阿福甄别进阶版接口
 * @author 31716
 *
 */
@Service("AFuFraudHandler")
public class GetAFuFraudResultServiceImpl implements GetInterfaceInfoService{
	public static Logger logger = LoggerFactory.getLogger(GetAFuFraudResultServiceImpl.class);
	@Value("${AFu.url}")
	private String url;
	
	@Value("${Afu.userName}")
	private String userName;
	
	@Value("${Afu.sign}")
	private String sign;
	
	@Value("${Afu.queryReason}")
	private String queryReason;
	
	@Value("${Afu.amountBusiness}")
	private String amountBusiness;
	@Override
	public JSONObject getResult(JSONObject param) {
		logger.info(">>>>调用阿福甄别进阶版接口>>>>>");
		JSONObject reqData = new JSONObject();
		JSONObject params = new JSONObject();
		reqData.put("user_name", userName);
		reqData.put("sign", sign);
		reqData.put("query_reason", queryReason);
		reqData.put("api_name", "fraud.screening.advance.api");
		params.put("id_no", param.getString("id_no"));
		params.put("name", param.getString("name"));
		params.put("mobile", param.getString("mobile"));
		params.put("bank_no", param.has("bank_no")?param.getString("bank_no"):null);
		params.put("addr_detection_types", param.has("addr_detection_types")? param.getString("addr_detection_types"):null);
		params.put("query_corp_addr_detection", param.has("query_corp_addr_detection")?param.getString("query_corp_addr_detection"):null);
		params.put("amount_business", amountBusiness);
		params.put("corp_addr", param.has("corp_addr")?param.getString("corp_addr"):null);
		params.put("corp_name", param.has("corp_name")?param.getString("corp_name"):null);
		params.put("corp_tel", param.has("corp_tel")?param.getString("corp_tel"):null);
		params.put("contacts", param.has("contacts")?param.getString("contacts"):null);
		params.put("email", param.has("email")?param.getString("email"):null);
		params.put("family_addr", param.has("family_addr")?param.getString("family_addr"):null);
		params.put("family_tel", param.has("family_tel")?param.getString("family_tel"):null);
		params.put("qq", param.has("qq")?param.getString("qq"):null);
		reqData.put("params", params);
		String result = HttpClientUtils.doPostForm(url, reqData);
		param.put("AFuFraudInfo",result);
		return param;
	}

}
