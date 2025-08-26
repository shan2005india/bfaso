package com.bng.ussd.wrapper;

public class UssdConfiguration {
	private int id;
	private String ussdCode;
	public String operator;
	public String protocol;
	private String service;
	private String pack;
	private String message;
	private String responseurl;
	private String contenturl;
	private int session;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String  getUssdCode() {
		return ussdCode;
	}
	public void setUssdCode(String ussdCode) {
		this.ussdCode = ussdCode;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public void setService(String service){
		this.service=service;
	}
	public String getService(){
		return service;
	}
	public void setPack(String pack){
		this.pack=pack;
	}
	public String getPack(){
		return pack;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getResponseurl() {
		return responseurl;
	}
	public void setResponseurl(String responseurl) {
		this.responseurl = responseurl;
	}
	public String getContenturl() {
		return contenturl;
	}
	public void setContenturl(String contenturl) {
		this.contenturl = contenturl;
	}
	public int getSession() {
		return session;
	}
	public void setSession(int session) {
		this.session = session;
	}
	
	@Override
	public String toString() {
	    return "UssdConfiguration {" +
	            "id=" + id +
	            ", ussdCode='" + ussdCode + '\'' +
	            ", operator='" + operator + '\'' +
	            ", protocol='" + protocol + '\'' +
	            ", service='" + service + '\'' +
	            ", pack='" + pack + '\'' +
	            ", message='" + message + '\'' +
	            ", responseurl='" + responseurl + '\'' +
	            ", contenturl='" + contenturl + '\'' +
	            ", session=" + session +
	            '}';
	}

}
