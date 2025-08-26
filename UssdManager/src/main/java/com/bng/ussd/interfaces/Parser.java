package com.bng.ussd.interfaces;

import com.bng.ussd.wrapper.RequestXml;

public interface Parser {
	
	public RequestXml parseRequestString(String request);

	public boolean isNotifyReception();
	

}
