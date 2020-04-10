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
	public JSONObject getYongHuiResult(){
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
		
		String bean9 = "HDBlackHandler";
		String bean10 = "HDCreditHandler";
		
		//String bean11 = "BRInfoVerifyHandler";
		String bean12 = "BRQueryHandler";
		GetInterfaceInfoService beanService1 = myBeanFactory.getBeanServiceByname(bean9);
		JSONObject params1 = beanService1.getResult(param);
		
		GetInterfaceInfoService beanService2 = myBeanFactory.getBeanServiceByname(bean10);
		JSONObject params2 = beanService2.getResult(params1);
		
		/*GetInterfaceInfoService beanService3 = myBeanFactory.getBeanServiceByname(bean11);
		JSONObject params3 = beanService3.getResult(params2);*/
		
		GetInterfaceInfoService beanService4 = myBeanFactory.getBeanServiceByname(bean12);
		JSONObject params4 = beanService4.getResult(params2);
		System.out.println("");
		return params4;
	}
}
