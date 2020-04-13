package com.tsdata.model.factor.service.impl;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.bfd.facade.MerchantServer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tsdata.model.factor.common.CommonUtil;
import com.tsdata.model.factor.common.HttpClientUtils;
import com.tsdata.model.factor.common.HttpConnectionManager4;
import com.tsdata.model.factor.common.PropertiesUtil;
import com.tsdata.model.factor.service.GetInterfaceInfoService;

import net.sf.json.JSONObject;
/**
 * 百融贷前策略接口
 * @author 31716
 *
 */
@Service("BRQueryHandler")
public class GetYongHuiQueryResultServiceImpl implements GetInterfaceInfoService{
	public static Logger logger = LoggerFactory.getLogger(GetYongHuiQueryResultServiceImpl.class);

	@Value("${BR.userName}")
	private String userName;
	
	@Value("${BR.password}")
	private String password;
	
	@Value("${BR.apiCode}")
	private String apiCode;
	//private static String loginName="SandboxLoginApi";
	//登陆地址
	@Value("${BR.loginApiUrl}")
	private String loginApiUrl;
	//贷前策略接口地址
	@Value("${BR.hxQueryApiUrl}")
	private String hxQueryApiUrl;
    @Value("${BR.apiName}")
    private String apiName;

	//贷前策略编号
	@Value("${BR.strategy_id}")
	private String strategy_id;
    private static MerchantServer ms = new MerchantServer();
    String tokenId="";
	/**
	 * 甬汇接口
	 * @param param
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	@Override
	public JSONObject getResult(JSONObject param){
		logger.info(">>>>>百融贷前策略接口>>>>>>");
		JSONObject jsonData = new JSONObject();
        jsonData.put("tokenid",generateToken());
        jsonData.put("apiCode",apiCode);
        jsonData.put("apiName",apiName);
		JSONObject reqData = new JSONObject();
		reqData.put("id",param.getString("id_no"));
        reqData.put("cell",param.getString("mobile"));
        reqData.put("name",param.getString("name"));

        String md = "";
        String checkCode = "";
		try {
			md = CommonUtil.getMD5(apiCode+generateToken());
			checkCode = CommonUtil.getMD5(reqData+md);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        jsonData.put("checkCode", checkCode);
		//TODO 贷前的策略编号(请查看策略配置表)
        reqData.put("strategy_id",strategy_id);
        jsonData.put("reqData",reqData);
       // String result = HttpClientUtils.doPostForm(hxQueryApiUrl, jsonData);
        String result = getBrData(jsonData.toString());
        param.put("BRQuery", result);
        logger.info("百融返回结果，{}",result);
		return param;
	}

    public String generateToken(){

        try{
            String login_res_str = login(userName,password,loginApiUrl,apiCode);
            if(StringUtils.isNotBlank(login_res_str)){
                JSONObject loginJson = JSONObject.fromObject(login_res_str);
                if(loginJson.containsKey("tokenid")){
                    tokenId = loginJson.getString("tokenid");
                }else {
                    System.out.println("返回结果异常，无token!结果为:"+login_res_str);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return tokenId;
    }
	
    public String login(String userName, String pwd, String loginName, String apiCode) throws Exception {
        String login_res_str = null;
        
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("userName", userName);
        paramMap.put("password", pwd);
        paramMap.put("apiCode", apiCode);
        System.out.println(userName+">>"+pwd+">>"+apiCode+">>"+loginName);
       // login_res_str = HttpConnectionManager4.post(PropertiesUtil.containsKey(loginName) ? PropertiesUtil.getStringValue(loginName) : loginName, paramMap);
        login_res_str = ms.login(userName,pwd,loginName,apiCode);
        System.out.println(login_res_str);
        return login_res_str;
      }

    public  String getBrData(String jsonStr){
        String res="";
        try {
            res = ms.getApiData(jsonStr,apiCode);
            if(StringUtils.isNotBlank(res)){
                JSONObject json = JSONObject.fromObject(res);
                if(json.containsKey("code")&&json.getString("code").equals("100007")){
                    tokenId = null;
                    JSONObject jsonData = JSONObject.fromObject(jsonStr);
                    jsonData.put("tokenid",generateToken());
                    res = ms.getApiData(jsonData.toString(),apiCode);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return res;
    }


}
