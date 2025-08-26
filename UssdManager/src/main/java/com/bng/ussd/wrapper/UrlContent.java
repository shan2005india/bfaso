package com.bng.ussd.wrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class UrlContent {

	private Map<String, UssdAlias> ussdAlias;
	private String msisdn;
	private String sessionId;
	private String userInput;

	public UrlContent() { }
	
	public UrlContent(String msisdn, String sessionId, String userInput, String urlContent) {
		this.msisdn = msisdn;
		this.sessionId = sessionId;
		this.userInput = userInput;
		
		addUssdAliases(urlContent);
	}
	
	public UrlContent(String msisdn, String sessionId, String userInput, Map<String, UssdAlias> ussdAlias) {
		this.msisdn = msisdn;
		this.sessionId = sessionId;
		this.userInput = userInput;
		this.ussdAlias = ussdAlias;
	}
	
	public Map<String, UssdAlias> getUssdAlias() {
		return ussdAlias;
	}

	public void setUssdAlias(Map<String, UssdAlias> ussdAlias) {
		this.ussdAlias = ussdAlias;
	}
	
	public void addUssdAliases(String urlContent) {
		String[] lines = urlContent.split("\n");
		int i=0;
		for(String line:lines) {
			String[] cols = line.split("-");
			if(cols.length<2) continue;
			
			UssdAlias ussdAlias = new UssdAlias();
			ussdAlias.setDialedCode(String.valueOf(i+1));
			ussdAlias.setAliasCode(cols[0]);
			ussdAlias.setDisplay(cols[1]);
			
			addUssdAlias(ussdAlias);
			i++;
		}
	}
	
	public void addUssdAlias(UssdAlias ussdAlias) {
		if(this.ussdAlias==null) this.ussdAlias = new HashMap<String, UrlContent.UssdAlias>();
		this.ussdAlias.put(ussdAlias.getDialedCode(), ussdAlias);
	}
	
	public UssdAlias fetchUssdAlias(String dialedCode) {
		if(this.ussdAlias==null) return null;
		return this.ussdAlias.get(dialedCode);
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getUserInput() {
		return userInput;
	}

	public void setUserInput(String userInput) {
		this.userInput = userInput;
	}
	
	public UssdAlias instantiateUssdAlias() {
		return new UssdAlias();
	}
	
	public String redisSerializer() {
		if(this.ussdAlias == null) return null;
		return this.ussdAlias.values().stream()
				.map(a -> a.getDialedCode().concat("###").concat(a.getAliasCode()).concat("###").concat(a.getDisplay()))
				.collect(Collectors.joining("\n"));
	}
	
	public Map<String, UssdAlias> redisDeSerializer(String redisContent) {
		Map<String, UssdAlias> uAlias = new HashMap<>();
		String[] lines = redisContent.split("\n");
		for(String line:lines) {
			String[] cols = line.split("###");
			UssdAlias ussdAlias = new UssdAlias();
			ussdAlias.setDialedCode(cols[0]);
			ussdAlias.setAliasCode(cols[1]);
			ussdAlias.setDisplay(cols[2]);
			
			uAlias.put(cols[0], ussdAlias);
		}
		
		return uAlias;
	}
	
	public String redisKey() {
		return "contenturl:"+this.getSessionId()+":"+this.getMsisdn();
	}
	
	public String ussdResponse() {
		if(this.ussdAlias == null) return null;
		return this.ussdAlias.values().stream()
				.map(a -> a.getDialedCode().concat("- ").concat(a.getDisplay()))
				.collect(Collectors.joining("\n"));
	}
	
	public class UssdAlias {
		private String aliasCode;
		private String dialedCode;
		private String display;
		public String getAliasCode() {
			return aliasCode;
		}
		public void setAliasCode(String aliasCode) {
			this.aliasCode = aliasCode;
		}
		public String getDialedCode() {
			return dialedCode;
		}
		public void setDialedCode(String dialedCode) {
			this.dialedCode = dialedCode;
		}
		public String getDisplay() {
			return display;
		}
		public void setDisplay(String display) {
			this.display = display;
		}
	}
}
