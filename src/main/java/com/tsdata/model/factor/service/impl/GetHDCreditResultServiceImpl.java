package com.tsdata.model.factor.service.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tsdata.model.factor.common.CommonUtil;
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
		String name = param.getString("name");
		String idty = param.getString("id_no");
		JSONObject reqData = new JSONObject();
		reqData.put("ACCESS_TOKEN", token);
		reqData.put("PHONE", mobile);
		reqData.put("NAME", name);
		reqData.put("IDCARD", idty);
		JSONObject result = CommonUtil.doGet(creditUrl, reqData);
		param.put("HDCredit", result);
		return param;
	}

	 public String getToken(){
		String result = null;
		JSONObject reqData = new JSONObject();
		reqData.put("AppID", AppID);
		reqData.put("AppSecret", AppSecret);
		reqData.put("Key", Key);
		JSONObject resultMap = CommonUtil.doGet(tokenUrl, reqData);
		System.out.println(resultMap);
		if (null != resultMap&&resultMap.has("access_token")&&resultMap.getString("access_token")!=null) {
			result = resultMap.getString("access_token");
		}
		System.out.println(result);
		return result;
	 }
	
}
