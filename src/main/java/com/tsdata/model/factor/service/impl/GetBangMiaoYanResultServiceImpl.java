package com.tsdata.model.factor.service.impl;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tsdata.model.factor.common.HttpClientUtils;
import com.tsdata.model.factor.common.SecretUtils;
import com.tsdata.model.factor.service.GetInterfaceInfoService;

import net.sf.json.JSONObject;
/**
 * 电话邦邦秒验接口
 * @author 31716
 *
 */
@Service("BMYHandler")
public class GetBangMiaoYanResultServiceImpl implements GetInterfaceInfoService{
	public static Logger logger = LoggerFactory.getLogger(GetBangMiaoYanResultServiceImpl.class);
	
	@Value("${DHB.BMY.url}")
	private String url;
	
	@Value("${DHB.apikey}")
	private String apikey;
	
	@Value("${DHB.apiscrect}")
	private String apiscrect;
	
	@Value("${DHB.BMY.products}")
	private String products;
	@Override
	public JSONObject getResult(JSONObject param) {
		JSONObject reqData = new JSONObject();
		String timestamp = System.currentTimeMillis()/1000L+"";
		String nonce = (int)(Math.random()*(9999-1000+1)+1000)+"";
		ArrayList<String> list=new ArrayList<String>();
		list.add(timestamp);
		list.add(apikey);
		list.add(apiscrect);
		list.add(nonce);
		Collections.sort(list);
		
		String signature = DigestUtils.shaHex(list.get(0)+list.get(1)+list.get(2)+list.get(3));
		
		String mobile = param.getString("mobile");
		reqData.put("timestamp", timestamp);
		reqData.put("apikey", apikey);
		reqData.put("nonce", nonce);
		reqData.put("signature", signature);
		reqData.put("apiscrect", apiscrect);
		reqData.put("tels", SecretUtils.getSHA256Str(mobile));
		reqData.put("products", products);
		reqData.put("name", param.getString("name"));
		reqData.put("idnum", param.getString("id_no"));
		String result = HttpClientUtils.doPostJson(url, reqData);
	    param.put("BangMiaoYan"+mobile,result);
		
		return param;
	}

}
