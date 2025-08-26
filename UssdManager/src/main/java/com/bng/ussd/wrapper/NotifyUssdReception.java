package com.bng.ussd.wrapper;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="notifyUssdReception")
public class NotifyUssdReception {
	
	private int msgType;
	private String sessionId;
	private String msIsdn;
	private String ussdString;
	private String transactionId;
	
	public int getMsgType() {
		return msgType;
	}
	
	@XmlElement(name="msgType",namespace="http://www.csapi.org/schema/parlayx/ussd/notification/v1_0/local")
	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}
	public String getSessionId() {
		return sessionId;
	}
	
	@XmlElement(name="senderCB",namespace="http://www.csapi.org/schema/parlayx/ussd/notification/v1_0/local")
	public void setSessionId(String senderCB) {
		this.sessionId = senderCB;
	}
	public String getMsIsdn() {
		return msIsdn;
	}
	
	@XmlElement(name="msIsdn",namespace="http://www.csapi.org/schema/parlayx/ussd/notification/v1_0/local")
	public void setMsIsdn(String msIsdn) {
		this.msIsdn = msIsdn;
	}
	public String getUssdString() {
		return ussdString;
	}
	
	@XmlElement(name="ussdString",namespace="http://www.csapi.org/schema/parlayx/ussd/notification/v1_0/local")
	public void setUssdString(String ussdString) {
		this.ussdString = ussdString;
	}

	public String getTransactionId() {
		return transactionId;
	}

	@XmlElement(name="receiveCB",namespace="http://www.csapi.org/schema/parlayx/ussd/notification/v1_0/local")
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	

}
