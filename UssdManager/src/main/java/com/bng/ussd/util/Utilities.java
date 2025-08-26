package com.bng.ussd.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;

import com.bng.ussd.cdr.UssdCdr;
import com.bng.ussd.daoImpl.UssdDaoImpl;
import com.bng.ussd.exception.coreException;
import com.bng.ussd.interfaces.Parser;
import com.bng.ussd.redis.JedisClientImpl;
import com.bng.ussd.wrapper.RequestXml;
import com.bng.ussd.wrapper.Response;
import com.bng.ussd.wrapper.SoapResponse;
import com.bng.ussd.wrapper.SpecialCharacterEscapeHandler;
import com.bng.ussd.wrapper.SubscriptionResponse;
import com.bng.ussd.wrapper.UssdConfiguration;
import com.bng.ussd.wrapper.UssdRequest;
import com.google.gson.Gson;
import com.sun.xml.bind.marshaller.DataWriter;

@Lazy
public class Utilities {
	
	@Autowired
	private UssdDaoImpl ussdDaoimpl;
	@Autowired
	private SpecialCharacterEscapeHandler specialcharacterescapehandler;
	@Autowired
	private RequestXml reqXml;
	@Autowired
	private RequestXml requestxml;
	@Autowired
	private Service service;
	@Autowired
	private SoapResponse soapResponse;
	@Autowired
	private Response resp;
	private Parser parser;
	private String sendUssdUrl;
	@Autowired
	private JedisClientImpl jedis;
	@Autowired
	private SubscriptionResponse subsResp;
	@Value("${check.subscription.url}")
	private String checkSubUrl;
	@Value("${check.allowed.url}")
	private String checkAllowedUrl;
	
	public String getCheckAllowedUrl() {
		return checkAllowedUrl;
	}
	
	public Parser getParser() {
		return parser;
	}

	public void setParser(Parser parser) {
		this.parser = parser;
	}
	public static HashMap<String,UssdConfiguration> configuration = new HashMap<String,UssdConfiguration>();
	
	private static Gson gson = new Gson();
	
	
	//for xml parsing
	public RequestXml getParsedString(String requestString){
		RequestXml request=null;
		try{
			JAXBContext jaxbcontext=JAXBContext.newInstance(RequestXml.class);
			Unmarshaller unmarshal=jaxbcontext.createUnmarshaller();
			StringReader reader=new StringReader(requestString);
		    request=(RequestXml)unmarshal.unmarshal(reader);
		}catch(Exception ex){
			Logger.sysLog(LogValues.info, this.getClass().getName(), coreException.GetStack(ex));
		}
		return request;
	}
	
	public HashMap<String, UssdConfiguration> getHashMap(List<UssdConfiguration> results){
		for(UssdConfiguration config:results){
			configuration.put(config.getUssdCode(), config);
		}
		Logger.sysLog(LogValues.info, this.getClass().getName()," Configuration loaded into hash map");
	    return configuration;
}

	public  String  convertToXML(Response response){
		 String xmlString="";
		    try {
		        JAXBContext context = JAXBContext.newInstance(Response.class);
		        Marshaller m = context.createMarshaller();
		        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		        StringWriter sw = new StringWriter();
               PrintWriter printWriter = new PrintWriter(sw);
               DataWriter dataWriter = new DataWriter(printWriter, "UTF-8", specialcharacterescapehandler);
		        m.marshal(response, dataWriter);
                xmlString=sw.toString();
		    } catch (JAXBException e) {
		    	Logger.sysLog(LogValues.error, this.getClass().getName(), coreException.GetStack(e));
		    }
		    return xmlString;

	}
	
	public synchronized static <T> T convertJsonStrToObject(String json, Class<T> classOfT)
	    {    	 
	    	return gson.fromJson(json,classOfT);
	    }
	 
	
		 

	public UssdCdr getUssdCdr(RequestXml request, String message, UssdConfiguration ussdConfiguration,String shortCode) {
		UssdCdr cdr=new UssdCdr();
		if((request==null)||(message==null)){
			cdr=null;
		}
		if(ussdConfiguration!=null){
			cdr.setPack(ussdConfiguration.getPack());
			cdr.setService(ussdConfiguration.getService());
			cdr.setUrl(ussdConfiguration.getResponseurl());
		}
		cdr.setDate(getCurrentDate("yyyy-MM-dd HH:mm:ss.SSS"));
		cdr.setInput(request.getSubscriberInput());
		cdr.setLanguage(request.getLanguage());
		cdr.setMsisdn(request.getMsisdn());
		cdr.setResponse(message);
		cdr.setSessionId(request.getSessionId());
		cdr.setTxnId(request.getTransactionId());
	    cdr.setShortCode(shortCode);
	    cdr.setMasterId(request.getMsisdn() +"_"+ request.getSessionId() +"_"+ System.currentTimeMillis());
		// TODO Auto-generated method stub
		return cdr;
	}
	public static String getCurrentDate(String dateFormat)
    {    	
	    SimpleDateFormat sdfDate = new SimpleDateFormat(dateFormat);
	    Date now = new Date();
	    String strDate = sdfDate.format(now);
	    return strDate;    	
    }
	
	public void hitUrl(String url,String urlParameters,String msisdn){
		HttpURLConnection con = null;
		try
		{
			URL obj = new URL(url); 
			URLConnection connection=obj.openConnection();
		    con=(HttpURLConnection)connection;			
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "text/xml");
			con.setDoOutput(true);
			Logger.sysLog(LogValues.info, this.getClass().getName(), "Going to hit url : "+url);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			int responseCode = con.getResponseCode();
			Logger.sysLog(LogValues.info, this.getClass().getName(), "["+msisdn+"]Response Code for msisdn "+" : "+responseCode);
			
			 
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
	 
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			Logger.sysLog(LogValues.info, this.getClass().getName(), "["+msisdn+"]Response for msisdn is : "+response);
		}catch(Exception ex){
			Logger.sysLog(LogValues.error, this.getClass().getName(), coreException.GetStack(ex));
		}
	}
	
	@Async
	public void sendUssd(RequestXml requestxml){
		Logger.sysLog(LogValues.info, this.getClass().getName(), requestxml.getMsisdn()+" | Going to send request to sendUssd");
		resp=service.processUserRequest(requestxml);
		String response = soapResponse.getResponse(
				service.getMsgResponse(resp), requestxml);
		String url = getSendUssdUrl();
		hitUrl(url, response, requestxml.getMsisdn());
	}

	public String getSendUssdUrl() {
		return sendUssdUrl;
	}

	public void setSendUssdUrl(String url) {
		this.sendUssdUrl = url;
	}
	
	public int checkForAllowedUser(RequestXml rx) {
		boolean existingUser = jedis.exists(rx.getSessionId() + "_"+ rx.getMsisdn());
		
		if(!existingUser){
			String subsresponse = service.subscribehttp(rx,checkAllowedUrl);
			Logger.sysLog(LogValues.info,
					this.getClass().getName(), rx.getSessionId()+"_"+rx.getMsisdn()+"Allowed User Response is : "
							+ subsresponse);
			
			//unable to get response from url
			if (subsresponse.equalsIgnoreCase("Error")) {
				Logger.sysLog(LogValues.error, this.getClass()
						.getName(),rx.getSessionId()+"_"+rx.getMsisdn()+
						"Unable to get response from url");
				return 2;
			} 
			
			else {
				JSONObject jsonstring = new JSONObject(subsresponse);
				String status = jsonstring.isNull("status")?jsonstring.getString("reqStatus"):jsonstring.getString("status");
				Logger.sysLog(LogValues.info, this.getClass()
						.getName(),rx.getSessionId()+"_"+rx.getMsisdn()+
						"response from url : "
								+ status);
				
				if (status.equalsIgnoreCase("failure")) {
					
//					message = service.getProp().getProperty("usernotallowed");
					Logger.sysLog(
							LogValues.info,
							this.getClass().getName(),
							rx.getSessionId()
									+ "_"
									+ rx.getMsisdn()
									+ "user not allowed to use service"
									+ status);
					return 1;
				} 
				else {
					Logger.sysLog(LogValues.info,this.getClass().getName(),rx.getSessionId()+ "_"+ rx.getMsisdn()+ "user allowed");
					return 0;
				}
			}
		} else
			return 0;
		
	}
	
	public String checkForServiceId(RequestXml rx) {
		boolean existingUser = jedis.exists(rx.getSessionId() + "_"
				+ rx.getMsisdn());
		String scode = "";
		
		if(!existingUser){
			scode = rx.getSubscriberInput();
		} else {
			scode = jedis.get(rx.getSessionId() + "_" + rx.getMsisdn());
		}
		
		Logger.sysLog(LogValues.info,this.getClass().getName(),"zzz scode: "+scode+", existingUser: "+existingUser);
		
		Properties pp = service.serviceIdMapping();
		Logger.sysLog(LogValues.info,this.getClass().getName(),"zzz pp: "+pp);
		
		for(Object p:pp.keySet()) {
			String sKey = (String) p;
			if(scode.startsWith(sKey)) return pp.getProperty(sKey);
		}
		return "";
	}
	
	public String checkForServiceId2(RequestXml rx,String sinput) {
		boolean existingUser = jedis.exists(rx.getSessionId() + "_"
				+ rx.getMsisdn());
		String scode = "";
		
		if(!existingUser){
			scode = rx.getSubscriberInput();
		} else {
			scode = jedis.get(rx.getSessionId() + "_" + rx.getMsisdn())+"*"+sinput;
		}
		
		Logger.sysLog(LogValues.info,this.getClass().getName(),"zzz scode: "+scode+", existingUser: "+existingUser);
		
		Properties pp = service.serviceIdMapping();
		Logger.sysLog(LogValues.info,this.getClass().getName(),"zzz pp: "+pp);
		
		for(Object p:pp.keySet()) {
			String sKey = (String) p;
			if(scode.startsWith(sKey)) return pp.getProperty(sKey);
		}
		return "";
	}

	public void checkForActiveUser(RequestXml rx) {
		boolean existingUser = jedis.exists(rx.getSessionId() + "_"
				+ rx.getMsisdn());

		Logger.sysLog(LogValues.info, this.getClass().getName(), rx.getMsisdn()+" | existingUser : "+existingUser);
		
		if(!existingUser){
		Logger.sysLog(LogValues.info, this.getClass().getName(), rx.getMsisdn()+" | Checking for user status for url : "+checkSubUrl);
		String userStatus="new";
		String checkSubResp=service.subscribehttp(rx, checkSubUrl);
		
		Logger.sysLog(LogValues.info, this.getClass().getName(), rx.getMsisdn()+" | checkSubResp : "+checkSubResp);
		
		if(!checkSubResp.equalsIgnoreCase("error")){
			subsResp=convertJsonStrToObject(checkSubResp, SubscriptionResponse.class);
			String response=subsResp.getCurrentStatus();
			userStatus=response;
		}
		
		if(userStatus==null||userStatus.equalsIgnoreCase("null")) userStatus="new";
		
		if(userStatus.equalsIgnoreCase("active") || userStatus.equalsIgnoreCase("demo")) {
			rx.setSubscriberInput(rx.getSubscriberInput()+"_A");
		} else if(userStatus.equalsIgnoreCase("pending")) {
			rx.setSubscriberInput(rx.getSubscriberInput()+"_P");
		} else if(userStatus.equalsIgnoreCase("unsub")) {
			rx.setSubscriberInput(rx.getSubscriberInput()+"_U");
		} else if(!userStatus.equalsIgnoreCase("new") && !userStatus.equalsIgnoreCase("unsub"))
			rx.setSubscriberInput(rx.getSubscriberInput()+"_G");
		}
		
	}

	public void cleanupRequest(String msisdn,String sessionId) {
		// TODO Auto-generated method stub
		Logger.sysLog(LogValues.info, this.getClass().getName(), msisdn+" | sessionId : "+sessionId+" | Cleanup called");
		boolean existingUser=jedis.exists(sessionId+"_"+msisdn);
		if(existingUser){
			jedis.remove(sessionId+ "_" + msisdn);
			jedis.remove("contenturl:"+sessionId+ ":" + msisdn);
			Logger.sysLog(LogValues.info, this.getClass().getName(),
					sessionId+"_"+msisdn
							+ ":user cleanup");
		}
	}
	
	public Response utilRespUssd(String msisdn,String userInput,String sessionId,String newrequest,String serviceid) {
		
		String type = "pull";
		Logger.sysLog(LogValues.info, this.getClass().getName(), msisdn+", zzz Got Request");

		if(msisdn==null||userInput==null) {
			Logger.sysLog(LogValues.info, this.getClass().getName(), msisdn+", Mandatory parameters are missing!");
			return null;
		}
		
		if(userInput!=null && userInput.startsWith("*")) userInput=userInput.substring(1);
		if(userInput!=null && userInput.endsWith("#")) userInput=userInput.substring(0,userInput.length()-1);
		
		String response=null;
		boolean sessionEnd=false;
		
		if(newrequest==null || !newrequest.equals("0")){
			   this.cleanupRequest(msisdn,msisdn);
		}
		RequestXml requestxml = new RequestXml(msisdn, msisdn, "_E", userInput, type);
		//requestxml.getRequest(msisdn, msisdn, "_E", userInput, type);

		// check for postpaid
		int userAllowed = 0;
		if(this.getCheckAllowedUrl()!=null && !this.getCheckAllowedUrl().equals("") && !this.getCheckAllowedUrl().equals("NA")) {
			Logger.sysLog(LogValues.info, this.getClass().getName(), msisdn+", zzz checking if user is allowed");
			userAllowed = this.checkForAllowedUser(requestxml);
		}
		
		Logger.sysLog(LogValues.info, this.getClass().getName(), msisdn+", zzz userAllowed: "+userAllowed);
		switch (userAllowed) {
		
		case 0:
			
			if(serviceid==null || serviceid.trim().equals("")) {
				serviceid = this.checkForServiceId(requestxml);
			}
			if(serviceid==null || serviceid.trim().equals("")) {
				serviceid = this.checkForServiceId2(requestxml,requestxml.getSubscriberInput());
			}
			requestxml.setServiceid(serviceid);
			Logger.sysLog(LogValues.info, this.getClass().getName(), msisdn+", zzz serviceid: "+serviceid+" subscriberInput: "+requestxml.getSubscriberInput());
			
			if(requestxml.getServiceid()!=null && !requestxml.getServiceid().equals("")) this.checkForActiveUser(requestxml);
			
			return service.processUserRequest(requestxml);
			
//			sessionEnd=this.checkSessionEnd(resp.getFreeflow());
//			response=resp.getApplicationResponse();
//			break;
		
		case 1:
			response = service.getProp().getProperty("usernotallowed");
			break;
			
		default:
			response = service.getProp().getProperty("error");
		
		}

		if(sessionEnd) response=response.concat("#1");
		else response=response.concat("#0");
		
		Logger.sysLog(LogValues.info, this.getClass().getName(),msisdn+ "--Final response is-- : "+response+", sessionEnd: "+sessionEnd);
		return null;
	}

	public boolean checkSessionEnd(String freeflow) {
		// TODO Auto-generated method stub
		if(freeflow.equalsIgnoreCase("<freeflowState>FB</freeflowState><freeflowCharging>N</freeflowCharging><freeflowChargingAmount>0.0</freeflowChargingAmount>"))
		return true;
		else
			return false;
	}

	public UssdRequest getUssdString(String ussdString) {
		// TODO Auto-generated method stub
		UssdRequest request=null;
		try{
			JAXBContext jaxbcontext=JAXBContext.newInstance(UssdRequest.class);
			Unmarshaller unmarshal=jaxbcontext.createUnmarshaller();
			StringReader reader=new StringReader(ussdString);
		    request=(UssdRequest)unmarshal.unmarshal(reader);
		}catch(Exception ex){
			Logger.sysLog(LogValues.info, this.getClass().getName(), coreException.GetStack(ex));
		}
		return request;
	}

	
	
	

}
