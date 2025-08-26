package com.bng.ussd.wrapper;

import org.springframework.beans.factory.annotation.Autowired;

import com.bng.ussd.interfaces.UssdResponse;
import com.bng.ussd.util.LogValues;
import com.bng.ussd.util.Logger;
import com.bng.ussd.util.Utilities;

public class SoapResponse implements UssdResponse {
	
	@Autowired
	private Response response;
	@Autowired
	private Utilities utilities;
	private String spId;
	private String serviceId;
	private String shortCode;
	private String opType="1";
	private String msgType="1";
	private String linkId;
	
    public String getResponse(String res,RequestXml requestxml){
		String resp=res;
		resp=resp.replaceAll("<<msisdn>>", requestxml.getMsisdn());
		resp=resp.replaceAll("<<linkid>>", getLinkId());
		resp=resp.replaceAll("<<sessionid>>", requestxml.getSessionId());
		resp=resp.replaceAll("<<transactionid>>", requestxml.getTransactionId());
		resp=resp.replaceAll("<<optype>>", getOpType());
		resp=resp.replaceAll("<<msgtype>>",getMsgType() );
		Logger.sysLog(LogValues.info, this.getClass().getName(), "Final response is : "+resp);
		return resp;
    }
	
	

	public String getOpType() {
		return opType;
	}

	public void setOpType(String opType) {
		this.opType = opType;
	}
	
	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getSpId() {
		return spId;
	}

	public void setSpId(String spId) {
		this.spId = spId;
	}

	public String getShortCode() {
		return shortCode;
	}

	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}


	public String getLinkId() {
		return linkId;
	}


	public void setLinkId(String linkId) {
		this.linkId = linkId;
	}


	public String getMsgType() {
		return msgType;
	}


	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}


}
