package com.bng.ussd.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import com.bng.ussd.wrapper.RequestXml;

public class UrlHitter {
	
	@Autowired
	private Service service;
	
	@Async
	public void sendSubscriptionRequest(RequestXml rx,String subUrl){
		Logger.sysLog(LogValues.info, this.getClass().getName(), "Going to hit subscription url");
		service.subscribehttp(rx,subUrl);
	}

}
