package com.tsdata.model.factor.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.tsdata.model.factor.service.GetInterfaceInfoService;


@Service
public class MyBeanFactory{

	@Autowired
	ApplicationContext applicationContext;
	
	public GetInterfaceInfoService getBeanServiceByname(String name) {
		return (GetInterfaceInfoService) applicationContext.getBean(name);
	}
}
