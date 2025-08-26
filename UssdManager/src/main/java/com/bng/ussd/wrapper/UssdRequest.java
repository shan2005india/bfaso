package com.bng.ussd.wrapper;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="ussd")
public class UssdRequest {
	
	private String msisdn;
	private String type;
	private String sessionid;
	private String msg;
	
	public String getMsisdn() {
		return msisdn;
	}
	
	@XmlElement
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}
	public String getType() {
		return type;
	}
	
	@XmlElement
	public void setType(String type) {
		this.type = type;
	}
	public String getSessionid() {
		return sessionid;
	}
	
	@XmlElement
	public void setSessionid(String sessionid) {
		this.sessionid = sessionid;
	}
	public String getMsg() {
		return msg;
	}
	
	@XmlElement
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	

}
