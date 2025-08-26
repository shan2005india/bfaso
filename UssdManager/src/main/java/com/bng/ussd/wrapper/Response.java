package com.bng.ussd.wrapper;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="response")
public class Response {
	
	
	private String msisdn;
	private String applicationResponse;
	private String freeflow;
	private String type;
	private String msg;

	public String getMsisdn() {
		return msisdn;
	}
	
	@XmlElement
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getApplicationResponse() {
		return applicationResponse;
	}
    
	@XmlElement
	public void setApplicationResponse(String applicationResponse) {
		this.applicationResponse=applicationResponse;
	}

	public String getFreeflow() {
		return freeflow;
	}

	@XmlElement
	public void setFreeflow(String freeflow) {
		this.freeflow = freeflow;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
	
}
