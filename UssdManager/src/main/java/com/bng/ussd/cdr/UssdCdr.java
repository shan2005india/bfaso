package com.bng.ussd.cdr;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;



@XmlRootElement(name="ussdcdr")
@XmlAccessorType(XmlAccessType.FIELD)
public class UssdCdr implements Serializable{
	
	
	@XmlElement(name="msisdn")
	private String msisdn;
	
	@XmlElement(name="subscriberInput")
	private String input;
	
	@XmlElement(name="txnId")
	private String txnId="";
	
	public String getTxnId() {
		return txnId;
	}

	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}

	@XmlElement(name="language")
	private String language;
	
	@XmlElement(name="sessionId")
	private String sessionId;
	
	@XmlElement(name="date")
	private String date;
	
	@XmlElement(name="service")
	private String service;
	
	@XmlElement(name="pack")
	private String pack;
	
	@XmlElement(name="url")
	private String url;
	
	@XmlElement(name="response")
	private String response;

	@XmlElement(name = "shortCode")
	private String shortCode;
	
	private String masterId;
	
	public UssdCdr(){
	}
	
	public UssdCdr(String msisdn,String language,String sessionId,String date,String service,String url,String response,String shortCode){
		this.msisdn=msisdn;
		this.input=input;
		this.language=language;
		this.sessionId=sessionId;
		this.date=date;
		this.service=service;
		this.url=url;
		this.response=response;
		this.shortCode=shortCode;
	}
	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
	

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getPack() {
		return pack;
	}

	public void setPack(String pack) {
		this.pack = pack;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}
	
	public String getShortCode() {
		return shortCode;
	}

	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}

	public String getMasterId() {
		return masterId;
	}

	public void setMasterId(String masterId) {
		this.masterId = masterId;
	}
	
	

}
