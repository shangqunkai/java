package com.tsdata.model.factor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tsdata.model.factor.common.MyBeanFactory;
import com.tsdata.model.factor.service.GetInterfaceInfoService;

import net.sf.json.JSONObject;

@Controller
@RequestMapping(path = "/test" , name = "测试")
public class testController {

	@Autowired
	private MyBeanFactory myBeanFactory;
	
	@RequestMapping(path = "/yonghui" , name = "测试")
	@ResponseBody
	public String getYongHuiResult(){
		JSONObject param = new JSONObject();
		param.put("name", "尚群凯");
		param.put("id_no", "412822199203056612");
		param.put("AFuApiName", "AFuCredit");
		param.put("YongHuiApiName", "hxQuery");
		param.put("mobile", "13817456274");
		
		param.put("imei", null);
		param.put("bank_no", "6222600640011810331");
		param.put("corp_addr", null);
		param.put("corp_name", null);
		param.put("corp_tel", null);
		param.put("contacts", null);
		param.put("email", null);
		param.put("family_addr", null);
		param.put("family_tel", null);
		param.put("qq", null);
		
		param.put("addr_detection_types", null);
		param.put("query_corp_addr_detection", null);
		param.put("amount_business", null);
		
		String bean1 = "GetAFuResultServiceImpl";
		String bean2 = "GetJinDunResultServiceImpl";
		String bean3 = "GetBangMiaoYanResultServiceImpl";
		String bean4 = "ZQMultHandler";
		String bean5 = "ZQCheckHandler";
		String bean6 = "AFuCreditHandler";
		String bean7 = "AFuFraudHandler";
		String bean8 = "AFuDecisionHandler";
		
		GetInterfaceInfoService beanService = myBeanFactory.getBeanServiceByname(bean8);
		JSONObject params = beanService.getResult(param);
		
		System.out.println("");
		return "";
	}
}
