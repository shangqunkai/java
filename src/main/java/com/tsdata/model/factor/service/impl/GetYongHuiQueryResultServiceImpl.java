package com.tsdata.model.factor.service.impl;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tsdata.model.factor.common.CommonUtil;
import com.tsdata.model.factor.service.GetInterfaceInfoService;

import net.sf.json.JSONObject;
/**
 * 百融贷前策略接口
 * @author 31716
 *
 */
@Service("BRQueryHandler")
public class GetYongHuiQueryResultServiceImpl implements GetInterfaceInfoService{
	
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
	
	//private static String apiName= "SandboxstrategyApi";
	//贷前策略编号
	@Value("${BR.strategy_id}")
	private String strategy_id= "";
	/**
	 * 甬汇接口
	 * @param param
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	@Override
	public JSONObject getResult(JSONObject param){

		JSONObject jsonData = new JSONObject();
        jsonData.put("tokenid",generateToken());
        jsonData.put("apiCode",apiCode);
       
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
        JSONObject result = CommonUtil.httpsRequest(hxQueryApiUrl, "POST", jsonData.toString());
        param.put("", result);
	
		return param;
	}

    public String generateToken(){
        String token="";
        try{
            String login_res_str = login(userName,password,loginApiUrl,apiCode);
            if(StringUtils.isNotBlank(login_res_str)){
                JSONObject loginJson = JSONObject.fromObject(login_res_str);
                if(loginJson.containsKey("tokenid")){
                    token = loginJson.getString("tokenid");
                }else {
                    System.out.println("返回结果异常，无token!结果为:"+login_res_str);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return token;
    }
	
    public String login(String userName, String pwd, String loginName, String apiCode) throws Exception {
        String login_res_str = null;
        
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("userName", userName);
        paramMap.put("password", pwd);
        paramMap.put("apiCode", apiCode);
        JSONObject result =CommonUtil.httpsRequest(loginApiUrl, "POST", paramMap.toString());
        System.out.println(result);
        if (null!= result) {
        	login_res_str = result.toString();
		}
        return login_res_str;
      }

    
}
