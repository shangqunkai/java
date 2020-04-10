package com.tsdata.model.factor.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.tsdata.model.factor.common.HttpClientUtils;
import com.tsdata.model.factor.common.HttpUtil;
import com.tsdata.model.factor.common.SecretUtils;
import com.tsdata.model.factor.service.GetInterfaceInfoService;

import net.sf.json.JSONObject;
/**
 * 中青综合风险查询接口
 * @author 31716
 *
 */
@Service("ZQCheckHandler")
public class GetZQCheckResultServiceImpl implements GetInterfaceInfoService{
	public static Logger logger = LoggerFactory.getLogger(GetZQCheckResultServiceImpl.class);
	
	@Value("${ZQ.appKey}")
	public String appKey;
	
	@Value("${ZQ.appSecret}")
	public String appSecret;
	
	@Value("${ZQ.checkUrl}")
	public String checkUrl;
	@Override
	public JSONObject getResult(JSONObject param) {
		String idCard = param.getString("id_no");
		String name = param.getString("name");
		String mobile = param.getString("mobile");
		String requestParam = composeRequestParam(name,idCard,mobile);
		String responseBody = HttpUtil.doDispatcher(checkUrl, HttpUtil.Request.METHOD_POST, requestParam ,header()).responseBody;
		param.put("ZQCheckResult", responseBody);
		return param;
	}

	private String composeRequestParam(String name, String idCard, String mobile) {
		Map<String, String> params = new TreeMap<>();
		params.put("mobile", SecretUtils.getAESSecret(mobile, appSecret));
		return composeRequestBody(params);
	}

	private String composeRequestBody(Map<String, String> paramMap) {
		paramMap.put("app_key", appKey);
		paramMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
		String digitalSignature = SecretUtils.getSignatureZqxy(appSecret, paramMap);
		paramMap.put("digital_signature", digitalSignature);
//		paramMap.put("encrypt_mode", "MD5");//姓名/身份证/手机号任一个入参如要支持MD5，需传该参数，如不需要请忽略不传该参数
		return convertMapToUrlString(paramMap);
	}

	private String convertMapToUrlString(Map<String, String> params) {
		StringBuilder requestParam = new StringBuilder();

		if (params != null && params.size() > 0) {
			for (Entry<String, String> kvs : params.entrySet()) {
				requestParam.append(kvs.getKey() + "=" + kvs.getValue() + "&");
			}
		}

		if (!StringUtils.isEmpty(requestParam.toString())) {
			if (requestParam.toString().endsWith("&")) {
				return requestParam.substring(0, requestParam.length() - 1);
			}
		}
		return requestParam.toString();
	}
	private Map<String, String> header() {
		Map<String, String> header = new HashMap<>();
		header.put("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
		return header;
	}
}
