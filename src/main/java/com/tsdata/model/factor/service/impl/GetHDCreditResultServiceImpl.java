package com.tsdata.model.factor.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tsdata.model.factor.common.HttpClientUtils;
import com.tsdata.model.factor.service.GetInterfaceInfoService;

import net.sf.json.JSONObject;
/**
 * 华道信贷频次接口
 * @author 31716
 *
 */
@Service("HDCreditHandler")
public class GetHDCreditResultServiceImpl implements GetInterfaceInfoService{
	public static Logger logger = LoggerFactory.getLogger(GetHDCreditResultServiceImpl.class);
	@Value("${HD.AppID}")
	private String AppID; 
	
	@Value("${HD.AppSecret}")
	private String AppSecret;
	
	@Value("${HD.Key}")
	private String  Key;
	
	@Value("${HD.tokenUrl}")
	private String  tokenUrl;
	
	@Value("${HD.creditUrl}")
	private String  creditUrl;
	@Override
	public JSONObject getResult(JSONObject param) {
		String token = getToken();
		String mobile = param.getString("mobile");
		JSONObject reqData = new JSONObject();
		reqData.put("ACCESS_TOKEN", token);
		reqData.put("Phone", mobile);
		String result = HttpClientUtils.doPostJson(creditUrl, reqData);
		param.put("HDBlack", result);
		return param;
	}

	 public String getToken(){
		JSONObject reqData = new JSONObject();
		reqData.put("AppID", AppID);
		reqData.put("AppSecret", AppSecret);
		reqData.put("Key", Key);
		String result = HttpClientUtils.doPostJson(tokenUrl, reqData);
		return result;
	 }
	
}
