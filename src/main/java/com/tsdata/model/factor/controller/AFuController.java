package com.tsdata.model.factor.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tsdata.model.factor.common.MyBeanFactory;
import com.tsdata.model.factor.common.SecretUtils;
import com.tsdata.model.factor.service.GetInterfaceInfoService;

import net.sf.json.JSONObject;


@Controller
@RequestMapping(path = "/AFu" , name = "信息共享")
public class AFuController {

	@Value("${Afu.sign}")
	private String sign;
	
	@Autowired
	private MyBeanFactory myBeanFactory;
	
	@RequestMapping(path = "/getDetails" , name = "阿福信息共享")
	@ResponseBody
	public JSONObject getYongHuiResult(HttpServletRequest request){
		JSONObject result = new JSONObject();
		try {
            String params = request.getParameter("params");
            String req=SecretUtils.decrypt(params,sign);
            JSONObject reqParam = JSONObject.fromObject(req);
            System.out.println(reqParam);
            
            GetInterfaceInfoService beanService = myBeanFactory.getBeanServiceByname("AFuShareHandler");
            result = beanService.getResult(reqParam);  
        } catch (IOException e) {
            e.printStackTrace();
        }
		return result;
	}
}
