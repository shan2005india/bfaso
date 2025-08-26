package com.bng.ussd.wrapper;

import java.util.Properties;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.context.annotation.Scope;



@XmlRootElement(name="request")
@Scope(value="prototype")
public class RequestXml {
	
	private String msisdn;
	private String transactionId;
	private String newRequest;
	private String sessionId;
	private String dateFormat;
	private String language;
	private String subscriberInput;
	private String type;
	private String serviceid;

	
	public String getServiceid() {
		return serviceid;
	}

	@XmlAttribute(name="serviceid")
	public void setServiceid(String serviceid) {
		this.serviceid = serviceid;
	}

	@XmlAttribute(name="type")
	public void setType(String type){
		this.type=type;
	}
	
	public String getType() {
		return type;
	}
	
	public String getMsisdn() {
		return msisdn;
	}

	
	public String getSubscriberInput() {
		return subscriberInput;
	}
	
	@XmlElement
	public void setSubscriberInput(String subscriberInput) {
		this.subscriberInput = subscriberInput;
	}
	@XmlElement
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}
	public String getTransactionId() {
		return transactionId;
	}
	@XmlElement
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	public String getNewRequest() {
		return newRequest;
	}
	@XmlElement
	public void setNewRequest(String newRequest) {
		this.newRequest = newRequest;
	}
	/*public String getMode() {
		return mode;
	}
	@XmlElement
	public void setMode(String mode) {
		this.mode = mode;
	}*/
	public String getSessionId() {
		return sessionId;
	}
	@XmlElement
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	public String getDateFormat() {
		return dateFormat;
	}
	@XmlElement
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}
	public String getLanguage() {
		return language;
	}
	@XmlElement
	public void setLanguage(String language) {
		this.language = language;
	}
	
	public RequestXml(){
		
	}
	
	public RequestXml(String msisdn,String sessionId,String lang,String input,String type){
		this.msisdn=msisdn;
		this.sessionId=sessionId;
		this.language=lang;
        this.type=type;
		this.subscriberInput=input;
	}

	public RequestXml getRequest(String msisdn,String sessionId,String lang,String input,String type){
		this.msisdn=msisdn;
		this.sessionId=sessionId;
		this.language=lang;
        this.type=type;
		this.subscriberInput=input;
		return this;
	}
	
	public RequestXml(String msisdn,String sessionId,String lang,String input,String type,String serviceid){
		this.msisdn=msisdn;
		this.sessionId=sessionId;
		this.language=lang;
        this.type=type;
		this.subscriberInput=input;
		this.serviceid=serviceid;
	}

	public RequestXml getRequest(String msisdn,String sessionId,String lang,String input,String type,String serviceid){
		this.msisdn=msisdn;
		this.sessionId=sessionId;
		this.language=lang;
        this.type=type;
		this.subscriberInput=input;
		this.serviceid=serviceid;
		return this;
	}
	
	public String toString() {
		Properties p = new Properties();
		p.setProperty("msisdn", msisdn);
		p.setProperty("transactionId", transactionId);
		p.setProperty("newRequest", newRequest);
		p.setProperty("sessionId", sessionId);
		p.setProperty("dateFormat", dateFormat);
		p.setProperty("language", language);
		p.setProperty("subscriberInput", subscriberInput);
		p.setProperty("type", type);
		
		return ""+p;
	}
	
}
