package com.bng.ussd.controller;

import java.net.URLEncoder;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bng.ussd.exception.coreException;
import com.bng.ussd.util.CacheEntry;
import com.bng.ussd.util.LogValues;
import com.bng.ussd.util.Logger;
import com.bng.ussd.util.Service;
import com.bng.ussd.util.Utilities;
import com.bng.ussd.wrapper.RequestXml;
import com.bng.ussd.wrapper.Response;
import com.bng.ussd.wrapper.UssdRequest;

@Controller
public class UssdController {

	@Lazy
	@Autowired
	private Utilities utilities;
	@Autowired
	private Service service;
	
    private static final ConcurrentMap<String, CacheEntry> sessionCache = new ConcurrentHashMap<>();
    private static final long EXPIRY_DURATION = 2 * 60 * 1000; // 2 minutes in milliseconds

	@RequestMapping(value = "/Ussd/mtnjob", method = RequestMethod.POST, consumes = "text/xml", produces = "text/xml; charset=utf-8")
	public @ResponseBody
	String ussdstring(@RequestBody String ussdstring) throws Exception {
		String response = null;
		Response resp=new Response();
		Logger.sysLog(LogValues.info, this.getClass().getName(),
				" Request xml : " + ussdstring);
		//parses request string to generate object of RequestXml type
		RequestXml requestxml = utilities.getParsedString(ussdstring);
		
	    resp = service.processUserRequest(requestxml);
		response=service.getXmlResponse(resp);
		Logger.sysLog(LogValues.info, this.getClass().getName(),
				"Final Response MSG  " + response);
		return response;
	}

	@RequestMapping(value = "/Ussd/Bngapli", method = RequestMethod.POST, consumes = "text/xml", produces = "text/xml; charset=utf-8")
	public @ResponseBody
	String ussdReponse(@RequestBody String usernotification) throws Exception {
		String response = null;
		Response resp=new Response();
		Logger.sysLog(LogValues.info, this.getClass().getName(),
				" USSD String: " + usernotification);
		RequestXml requestxml = utilities.getParsedString(usernotification);
		resp = service.processUserRequest(requestxml);
		response=service.getXmlResponse(resp);
		Logger.sysLog(LogValues.info, this.getClass().getName(),
				"Final Response MSG  " + response);
		return response;
	}
	
	@RequestMapping(value="/ussd/bngapi",method=RequestMethod.POST,consumes="text/xml",produces="text/xml;charset=utf-8")
	public @ResponseBody
	String UssdResponse(@RequestBody String ussdString) throws Exception{
		RequestXml requestxml = null;
		Logger.sysLog(LogValues.info, this.getClass().getName(),
				" USSD String : " + ussdString);
		String response = null;
		int type=0;
		UssdRequest ussdReq = utilities.getUssdString(ussdString);
		if(ussdReq.getMsg().equalsIgnoreCase("#")){
			ussdReq.setMsg("9");
		}
		if(ussdReq.getType().equals("1")||ussdReq.getType().equals("2")){
			 requestxml = new RequestXml(ussdReq.getMsisdn(), ussdReq.getSessionid(), "_E", ussdReq.getMsg().replaceAll("#", ""), "pull");
			//requestxml.getRequest(ussdReq.getMsisdn(), ussdReq.getSessionid(), "_E", ussdReq.getMsg().replaceAll("#", ""), "pull");
		}
		if(requestxml != null && (requestxml.getType().equals("3")||requestxml.getType().equals("4"))){
			requestxml = new RequestXml(ussdReq.getMsisdn(), ussdReq.getSessionid(), "_E", ussdReq.getMsg(), "cleanUp");
			//requestxml.getRequest(ussdReq.getMsisdn(), ussdReq.getSessionid(), "_E", ussdReq.getMsg(), "cleanUp");
		}
		Response resp=service.processUserRequest(requestxml);
		if(resp.getFreeflow().equalsIgnoreCase("<freeflowState>FC</freeflowState><freeflowCharging>N</freeflowCharging><freeflowChargingAmount>0.0</freeflowChargingAmount>")){
			response="<ussd><type>2</type><msg>"+resp.getApplicationResponse()+"</msg></ussd>";
		}
		else{
			response="<ussd><type>3</type><msg>"+resp.getApplicationResponse()+"</msg></ussd>";
		}
		Logger.sysLog(LogValues.info, this.getClass().getName(), "Final Response is : "+response);
		return response;
	}

	@RequestMapping(value = "/Ussd/bngmanager", method = RequestMethod.POST, consumes = "text/xml", produces = "text/xml; charset=utf-8")
	public @ResponseBody
	String ussdString(@RequestBody String ussdstring) throws Exception {
		Logger.sysLog(LogValues.info, this.getClass().getName(),
				" USSD String : " + ussdstring);
		String response = null;
		RequestXml requestxml = utilities.getParsedString(ussdstring);
		Response resp=new Response();
		resp = service.processUserRequest(requestxml);
		response=service.getXmlResponse(resp);
		Logger.sysLog(LogValues.info, this.getClass().getName(),
				"Final Response msg : " + response);
		return response;
	}

	//for MTN Ghana
	@RequestMapping(value = "/Ussd/manager", method = RequestMethod.POST, consumes = "text/xml", produces = "text/xml;charset=utf-8")
	public @ResponseBody
	String ussdController(@RequestBody String ussdstring) throws Exception {
		Logger.sysLog(LogValues.info, this.getClass().getName(),
				"Received Ussd String : " + ussdstring);
		String response=null;
		RequestXml requestxml = utilities.getParser().parseRequestString(ussdstring);
		Logger.sysLog(LogValues.APP_DEBUG, this.getClass().getName(), "NotifyUssdReception is : "+utilities.getParser().isNotifyReception());
		if (utilities.getParser().isNotifyReception()) {
			utilities.sendUssd(requestxml);
			response = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:loc=\"http://www.csapi.org/schema/parlayx/ussd/notification/v1_0/local\"> <soapenv:Header/> <soapenv:Body> <loc:notifyUssdReceptionResponse> <loc:result>0</loc:result> </loc:notifyUssdReceptionResponse> </soapenv:Body> </soapenv:Envelope>";
			Logger.sysLog(LogValues.info, this.getClass().getName(), requestxml.getMsisdn()+" | Response to notifyUssd : "+response);
			return response;
		} else {
			response = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:loc=\"http://www.csapi.org/schema/parlayx/ussd/notification/v1_0/local\"><soapenv:Header/><soapenv:Body><loc:notifyUssdAbortResponse/></soapenv:Body></soapenv:Envelope>";
			Logger.sysLog(LogValues.info, this.getClass().getName(), requestxml.getMsisdn()+" | Session Abort request received");
			Logger.sysLog(LogValues.info, this.getClass().getName(), requestxml.getMsisdn()+" | Response to notifyAbort : "+response);
			return response;
		}
	}
	
	@RequestMapping(value="/bng/ussd",method=RequestMethod.GET)
	@ResponseBody
	public String ussdGetController(
			@RequestParam("sc") String shortCode,
			@RequestParam("msisdn") String msisdn,
			@RequestParam("user_input") String userInput,
			@RequestParam("session_id") String sessionId,
			@RequestParam("lang") String language,
			@RequestParam("req_no") int reqNum)
	{
		if(!userInput.equals("") && reqNum==1){
			userInput=shortCode+"*"+userInput;
		}
		
		if(userInput==null||userInput.equals("")){
			utilities.cleanupRequest(msisdn,msisdn);
			userInput=shortCode;
		}
		String response=null;
		RequestXml requestxml = new RequestXml(msisdn, sessionId, language, userInput,"pull");
		//requestxml.getRequest(msisdn, sessionId, language, userInput,"pull");
		Response resp=new Response();
		resp=service.processUserRequest(requestxml);
		response=service.getStringResp(resp);
		Logger.sysLog(LogValues.info, this.getClass().getName(),"Final response is : "+response);
		return response;	
	}
	
	@RequestMapping(value="/ussd/smpp",method=RequestMethod.GET)
	@ResponseBody
	public String ussdSmsController(
			@RequestParam("sc")String shortCode,
			@RequestParam("msisdn")String msisdn,
			@RequestParam("input")String userInput,
			@RequestParam("sessionId")String sessionId,
			@RequestParam("serviceOp")String serviceOp,
	        @RequestParam("type")String type){
		String response=null;
		boolean sessionEnd=false;
		if(userInput.equals("")){
			   utilities.cleanupRequest(msisdn,msisdn);
	           userInput=shortCode;
		}
		if(userInput!=null && userInput.startsWith("*")) userInput=userInput.substring(1);
		if(userInput!=null && userInput.endsWith("#")) userInput=userInput.substring(0,userInput.length()-1);
		
		RequestXml requestxml = new RequestXml(msisdn, msisdn, "_E", userInput, type);
		//requestxml.getRequest(msisdn, msisdn, "_E", userInput, type);

		// check for postpaid
		int userAllowed = 0;
		if(utilities.getCheckAllowedUrl()!=null && !utilities.getCheckAllowedUrl().equals("")) {
			userAllowed = utilities.checkForAllowedUser(requestxml);
		}
		
		switch (userAllowed) {
		
		case 0:

			String serviceid="";
			if(serviceid==null || serviceid.trim().equals("")) {
				serviceid = utilities.checkForServiceId(requestxml);
			}
			requestxml.setServiceid(serviceid);
			Logger.sysLog(LogValues.info, this.getClass().getName(), msisdn+", zzz serviceid: "+serviceid);
			
			if(requestxml.getServiceid()!=null && !requestxml.getServiceid().equals("")) utilities.checkForActiveUser(requestxml);
			Response resp=service.processUserRequest(requestxml);
			sessionEnd=utilities.checkSessionEnd(resp.getFreeflow());
			response="response="+resp.getApplicationResponse()+"&sessionId="+sessionId+"&serviceOp="+serviceOp+"&sessionEnd="+sessionEnd;
			break;
		
		case 1:
			response = "response="+service.getProp().getProperty("usernotallowed")+"&sessionId="+sessionId+"&serviceOp="+serviceOp+"&sessionEnd=true";
			break;
			
		default:
			response = "response="+service.getProp().getProperty("error")+"&sessionId="+sessionId+"&serviceOp="+serviceOp+"&sessionEnd=true";
		
		}

		Logger.sysLog(LogValues.info, this.getClass().getName(),msisdn+ "Final response is : "+response);
		return response;
	}
	
	@RequestMapping(value="/ussd/smpp1",method=RequestMethod.GET)
	@ResponseBody
	public String ussdSmsController1(
			@RequestParam("sc")String shortCode,
			@RequestParam("msisdn")String msisdn,
			@RequestParam("input")String userInput,
			@RequestParam("sessionId")String sessionId,
			@RequestParam("serviceOp")String serviceOp,
	        @RequestParam("type")String type){
		String response=null;
		boolean sessionEnd=false;
		if(userInput.equals("")){
			   utilities.cleanupRequest(msisdn,msisdn);
	           userInput=shortCode;
		}
		if(userInput!=null && userInput.startsWith("*")) userInput=userInput.substring(1);
		if(userInput!=null && userInput.endsWith("#")) userInput=userInput.substring(0,userInput.length()-1);
		
		if(userInput!=null && userInput.toLowerCase().startsWith("unknown")) {
			response = "response="+service.getProp().getProperty("unknownerror", service.getProp().getProperty("error"))+"&sessionId="+sessionId+"&serviceOp="+serviceOp+"&sessionEnd=true";
		}
		
		RequestXml requestxml = new RequestXml(msisdn, msisdn, "_E", userInput, type);
		//requestxml.getRequest(msisdn, msisdn, "_E", userInput, type);

		// check for postpaid
		int userAllowed = 0;
		if(utilities.getCheckAllowedUrl()!=null && !utilities.getCheckAllowedUrl().equals("") && !utilities.getCheckAllowedUrl().equals("NA")) {
			userAllowed = utilities.checkForAllowedUser(requestxml);
		}
		
		switch (userAllowed) {
		
		case 0:
			
			utilities.checkForActiveUser(requestxml);
			Response resp=service.processUserRequest(requestxml);
			sessionEnd=utilities.checkSessionEnd(resp.getFreeflow());
			response="response="+resp.getApplicationResponse()+"&sessionId="+sessionId+"&serviceOp="+serviceOp+"&sessionEnd="+sessionEnd;
			break;
		
		case 1:
			response = "response="+service.getProp().getProperty("usernotallowed")+"&sessionId="+sessionId+"&serviceOp="+serviceOp+"&sessionEnd=true";
			break;
			
		default:
			response = "response="+service.getProp().getProperty("error")+"&sessionId="+sessionId+"&serviceOp="+serviceOp+"&sessionEnd=true";
		
		}

		Logger.sysLog(LogValues.info, this.getClass().getName(),msisdn+ "Final response is : "+response);
		return response;
	}
	
	@RequestMapping(value="/ussd", method=RequestMethod.GET, produces = "text/html;charset=utf-8")
	public @ResponseBody String ussdController1(
			@RequestParam("msisdn")String msisdn,
			@RequestParam("userinput")String userInput,
			@RequestParam("sessionid")String sessionId,
			@RequestParam("newrequest")String newrequest,
			@RequestParam(value = "serviceid", required = false)String serviceid,
			@RequestParam(value = "setpid", required = false)String setpid,
			HttpServletRequest request){
		
		String type = "pull";
		Logger.sysLog(LogValues.info, this.getClass().getName(), msisdn+", zzz Got Request");

		if(userInput!=null && userInput.startsWith("*")) userInput=userInput.substring(1);
		if(userInput!=null && userInput.endsWith("#")) userInput=userInput.substring(0,userInput.length()-1);
		
		String response=null;
		boolean sessionEnd=false;
		
		if(!newrequest.equals("0")){
			   utilities.cleanupRequest(msisdn,msisdn);
		}
		RequestXml requestxml = new RequestXml(msisdn, msisdn, "_E", userInput, type);
		//requestxml.getRequest(msisdn, msisdn, "_E", userInput, type);

		// check for postpaid
		int userAllowed = 0;
		if(utilities.getCheckAllowedUrl()!=null && !utilities.getCheckAllowedUrl().equals("") && !utilities.getCheckAllowedUrl().equals("NA")) {
			Logger.sysLog(LogValues.info, this.getClass().getName(), msisdn+", zzz checking if user is allowed");
			userAllowed = utilities.checkForAllowedUser(requestxml);
		}
		
		Logger.sysLog(LogValues.info, this.getClass().getName(), msisdn+", zzz userAllowed: "+userAllowed);
		switch (userAllowed) {
		
		case 0:
			
			if(serviceid==null || serviceid.trim().equals("")) {
				serviceid = utilities.checkForServiceId(requestxml);
			}
			requestxml.setServiceid(serviceid);
			Logger.sysLog(LogValues.info, this.getClass().getName(), msisdn+", zzz serviceid: "+serviceid);
			
			if(requestxml.getServiceid()!=null && !requestxml.getServiceid().equals("")) utilities.checkForActiveUser(requestxml);
			Response resp=service.processUserRequest(requestxml);
			sessionEnd=utilities.checkSessionEnd(resp.getFreeflow());
			response=resp.getApplicationResponse();
			
			break;
		
		case 1:
			response = service.getProp().getProperty("usernotallowed");
			break;
			
		default:
			response = service.getProp().getProperty("error");
		
		}

//		if(sessionEnd) response=response.concat("#1");
//		else response=response.concat("#0");
		
		Logger.sysLog(LogValues.info, this.getClass().getName(),msisdn+ "Final response is : "+response+", sessionEnd: "+sessionEnd);
		return response;
	}
	
	@RequestMapping(value="/ussdpost", method = { RequestMethod.GET, RequestMethod.POST }, produces = "text/html;charset=utf-8")
	public @ResponseBody String ussdControllerPost(
			@RequestHeader(value = "MSISDN", required = false) String xmsisdn,
			@RequestHeader(value = "REQDATA", required = false) String xuserInput,
			@RequestHeader(value = "SESSIONID", required = false) String xsessionId,
			@RequestHeader(value = "REQUESTTYPE", required = false)String xnewrequest,
			@RequestHeader(value = "serviceid", required = false)String xserviceid,
			@RequestHeader(value = "DIALOGID", required = false) String xsetpid,
			
			@RequestParam(value = "msisdn", required = false)String msisdn,
			@RequestParam(value = "userinput", required = false)String userInput,
			@RequestParam(value = "sessionid", required = false)String sessionId,
			@RequestParam(value = "newrequest", required = false)String newrequest,
			@RequestParam(value = "serviceid", required = false)String serviceid,
	        @RequestParam(value = "setpid", required = false)String setpid,
	        HttpServletRequest request,
			HttpServletResponse response){
		
		Logger.sysLog(LogValues.info, this.getClass().getName(), msisdn+", Got Request on Post..");
		String respData = "";
		
		try {
		
			if(msisdn==null || msisdn.trim().equals("")) msisdn = xmsisdn;
			if(userInput==null || userInput.trim().equals("")) userInput = xuserInput;
			if(sessionId==null || sessionId.trim().equals("")) sessionId = xsessionId;
			if(newrequest==null || newrequest.trim().equals("")) newrequest = xnewrequest;
			if(serviceid==null || serviceid.trim().equals("")) serviceid = xserviceid;
			if(setpid==null || setpid.trim().equals("")) setpid = xsetpid;
			
			//Customisation for Telecom Niger
			if(newrequest==null || newrequest.trim().equals("") || newrequest.trim().equals("1")) newrequest = "1";
			else newrequest = "0";
			
			
			respData = respUssd(msisdn, userInput, sessionId, newrequest, serviceid);
			String reqType = respData.endsWith("#1")?"4":"2";
			
			respData = respData.substring(0, respData.length()-2);
			
			respData=URLEncoder.encode(respData,"UTF-8");
			response.setHeader("RESPDATA", respData);
			response.setHeader("REQTYPE", reqType);
		} catch(Exception e) {
			Logger.sysLog(LogValues.error, this.getClass().getName(), msisdn+", Exception: "+coreException.GetStack(e));
		}
		return respData;
	}
	
	@RequestMapping(value="/ussdreq", method = { RequestMethod.GET, RequestMethod.POST }, produces = {"application/xhtml; charset=UTF-8", MediaType.ALL_VALUE})
	public @ResponseBody String ussdRequestPost(
			@RequestHeader(value = "MSISDN", required = false) String xmsisdn,
			@RequestHeader(value = "REQDATA", required = false) String xuserInput,
			@RequestHeader(value = "SESSIONID", required = false) String xsessionId,
			@RequestHeader(value = "REQUESTTYPE", required = false)String xnewrequest,
			@RequestHeader(value = "serviceid", required = false)String xserviceid,
			@RequestHeader(value = "DIALOGID", required = false) String xsetpid,
			
			@RequestParam(value = "msisdn", required = false)String msisdn,
			@RequestParam(value = "user_input", required = false)String userInput,
			@RequestParam(value = "sessionid", required = false)String sessionId,
			@RequestParam(value = "newrequest", required = false)String newrequest,
			@RequestParam(value = "serviceid", required = false)String serviceid,
	        @RequestParam(value = "setpid", required = false)String setpid,
	        
	        @RequestParam(value = "userid", required = false)String userid,
	        @RequestParam(value = "password", required = false)String password,
	        @RequestParam(value = "MSC", required = false)String msc,
	        
	        @RequestHeader(value = "User-SessionId", required = false) String xUserSessionId,
	        @RequestHeader(value = "User-Id", required = false) String xUserId,
	        @RequestParam(value = "input", required = false)String input,
	        
	        HttpServletRequest request,
			HttpServletResponse response){
		
		Logger.sysLog(LogValues.info, this.getClass().getName(), msisdn+", Got Request on ussdreq.. "+xUserId+", input: "+input+", newrequest: "+newrequest);
		
		newrequest = "";
		
		Logger.sysLog(LogValues.info, this.getClass().getName(), msisdn+", second line respUssd - msisdn: "+xUserId+", input: "+input+", sessionId: "+xUserSessionId+", newrequest: "+newrequest+", serviceId: "+serviceid);
		String respData = "";
		
		try {
		
			if(msisdn==null || msisdn.trim().equals("")) msisdn = xmsisdn;
			if(userInput==null || userInput.trim().equals("")) userInput = xuserInput;
			if(sessionId==null || sessionId.trim().equals("")) sessionId = xsessionId;
//			if(newrequest==null || newrequest.trim().equals("")) newrequest = xnewrequest;
			if(serviceid==null || serviceid.trim().equals("")) serviceid = xserviceid;
			if(setpid==null || setpid.trim().equals("")) setpid = xsetpid;
			
			if(xUserSessionId != null && xUserSessionId.length()>0) sessionId = xUserSessionId;
			if(xUserId != null && xUserId.length()>0) {
				msisdn = xUserId;
				if(msisdn.startsWith("tel:")) msisdn = msisdn.substring(5);
			}
			if(input != null && input.length()>0) userInput = input;
			
			if(sessionId==null || sessionId.trim().equals("")) sessionId = "1";
			
			//Customisation for Telecom Niger
//			if(newrequest==null || newrequest.trim().equals("") || newrequest.trim().equals("1")) newrequest = "1";
//			else newrequest = "0";
			
			Instant now = Instant.now();
			CacheEntry existingEntry = sessionCache.get(msisdn);
	        boolean isExpired = existingEntry == null || now.minusMillis(EXPIRY_DURATION).isAfter(existingEntry.getTimestamp());
			
			int nr = 1;
			if (newrequest != null && newrequest.trim().equals("1")) {
				sessionCache.remove(msisdn);
				sessionCache.put(msisdn, new CacheEntry(sessionId, now));
	            nr = 1;
	        } else {
	        	if (isExpired || !existingEntry.getSessionId().equals(sessionId)) {
	                nr = 1;
	            } else {
	            	nr = 0;
	            }
	        	sessionCache.put(msisdn, new CacheEntry(sessionId, now));
	        	
//	            nr = sessionCache.containsKey(msisdn) && sessionCache.get(msisdn).equals(isessionId) ? 0 : 1;
//	            sessionCache.compute(msisdn, (key, oldEntry) -> new CacheEntry(isessionId, now));
	        }
			newrequest = String.valueOf(nr);
			
			Logger.sysLog(LogValues.info, this.getClass().getName(), msisdn+", before respUssd - msisdn: "+msisdn+", input: "+input+", sessionId: "+sessionId+", newrequest: "+newrequest+", serviceId: "+serviceid);
			
			respData = respUssd(msisdn, userInput, sessionId, newrequest, serviceid);
			String reqType = respData.endsWith("#1")?"FB":"FC";
			
			respData = respData.substring(0, respData.length()-2);
			
			if(service.getProp().getProperty("encode_response","0").trim().equals("1")) respData=URLEncoder.encode(respData,"UTF-8");
//			response.setHeader("RESPDATA", respData);
			response.setHeader("Freeflow", reqType);
			response.setHeader("charge", "N");
			response.setHeader("amount", "0");
			response.setHeader("cpRefId", "0");
			
			if(reqType.equalsIgnoreCase("fb") && !respData.contains("<html>")) {
				respData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"
						+ "<html> \n"
						+ " <head> \n"
						+ " <title></title>\n"
						+ " <meta name=\"nav\" content=\"end\"/> \n"
						+ " </head> \n"
						+ " <body> "+respData +"</body> \n"
								+ "</html>";
			}
			
		} catch(Exception e) {
			Logger.sysLog(LogValues.error, this.getClass().getName(), msisdn+", Exception: "+coreException.GetStack(e));
		}
		return respData;
	}
	
	@RequestMapping(value="/ussd/headers", method=RequestMethod.GET, produces = "text/html;charset=utf-8")
	public @ResponseBody String ussdController2(
			@RequestHeader(value = "X-Msisdn", required = false) String xmsisdn,
			@RequestHeader(value = "X-Content", required = false) String xuserInput,
			@RequestHeader(value = "X-Session-Id", required = false) String xsessionId,
			@RequestHeader(value = "uuid", required = false) String xsetpid,
			
			@RequestParam(value = "msisdn", required = false)String msisdn,
			@RequestParam(value = "userinput", required = false)String userInput,
			@RequestParam(value = "sessionid", required = false)String sessionId,
			@RequestParam(value = "newrequest", required = false)String newrequest,
	        @RequestParam(value = "setpid", required = false)String setpid){
		
		if(msisdn==null || msisdn.trim().equals("")) msisdn = xmsisdn;
		if(userInput==null || userInput.trim().equals("")) userInput = xuserInput;
		if(sessionId==null || sessionId.trim().equals("")) sessionId = xsessionId;
		if(setpid==null || setpid.trim().equals("")) setpid = xsetpid;
		
		if(userInput!=null && userInput.startsWith("*")) userInput=userInput.substring(1);
		if(userInput!=null && userInput.endsWith("#")) userInput=userInput.substring(0,userInput.length()-1);
		
		String type = "pull";
		Logger.sysLog(LogValues.info, this.getClass().getName(), msisdn+", zzz Got Request");

		String response=null;
		boolean sessionEnd=false;
		
		if(newrequest==null || !newrequest.equals("0")){
			   utilities.cleanupRequest(msisdn,msisdn);
		}
		RequestXml requestxml = new RequestXml(msisdn, msisdn, "_E", userInput, type);
		//requestxml.getRequest(msisdn, msisdn, "_E", userInput, type);

		// check for postpaid
		int userAllowed = 0;
		if(utilities.getCheckAllowedUrl()!=null && !utilities.getCheckAllowedUrl().equals("") && !utilities.getCheckAllowedUrl().equals("NA")) {
			Logger.sysLog(LogValues.info, this.getClass().getName(), msisdn+", zzz checking if user is allowed");
			userAllowed = utilities.checkForAllowedUser(requestxml);
		}
		
		Logger.sysLog(LogValues.info, this.getClass().getName(), msisdn+", zzz userAllowed: "+userAllowed);
		switch (userAllowed) {
		
		case 0:
			
			utilities.checkForActiveUser(requestxml);
			Response resp=service.processUserRequest(requestxml);
			sessionEnd=utilities.checkSessionEnd(resp.getFreeflow());
			response=resp.getApplicationResponse();
			break;
		
		case 1:
			response = service.getProp().getProperty("usernotallowed");
			break;
			
		default:
			response = service.getProp().getProperty("error");
		
		}

		Logger.sysLog(LogValues.info, this.getClass().getName(),msisdn+ "Final response is : "+response);
		return response;
	}
	
	@RequestMapping(value="/ussdapi", method = { RequestMethod.GET, RequestMethod.POST }, produces = "application/xhtml; charset=UTF-8")
	public @ResponseBody String ussdRequestDashPost(
			@RequestHeader(value = "User-Id", required = false) String xmsisdn,
			@RequestHeader(value = "REQDATA", required = false) String xuserInput,
			@RequestHeader(value = "User-SessionId", required = false) String xsessionId,
			@RequestHeader(value = "REQUESTTYPE", required = false)String xnewrequest,
			@RequestHeader(value = "serviceid", required = false)String xserviceid,
			@RequestHeader(value = "DIALOGID", required = false) String xsetpid,
			@RequestHeader(value = "User-Language", required = false) String xlanguage,
			
			@RequestParam(value = "msisdn", required = false)String msisdn,
			@RequestParam(value = "user_input", required = false)String userInput,
			@RequestParam(value = "sessionid", required = false)String sessionId,
			@RequestParam(value = "newrequest", required = false)String newrequest,
			@RequestParam(value = "serviceid", required = false)String serviceid,
	        @RequestParam(value = "setpid", required = false)String setpid,
	        @RequestParam(value = "language", required = false)String language,
	        
	        @RequestParam(value = "userid", required = false)String userid,
	        @RequestParam(value = "password", required = false)String password,
	        @RequestParam(value = "MSC", required = false)String msc,
	        
	        HttpServletRequest request,
			HttpServletResponse response){
		
		Logger.sysLog(LogValues.info, this.getClass().getName(), msisdn+", Got Request on ussdapi..");
		String respData = "";
		
		try {
		
			if(msisdn==null || msisdn.trim().equals("")) msisdn = xmsisdn;
			if(userInput==null || userInput.trim().equals("")) userInput = xuserInput;
			if(sessionId==null || sessionId.trim().equals("")) sessionId = xsessionId;
			if(newrequest==null || newrequest.trim().equals("")) newrequest = xnewrequest;
			if(serviceid==null || serviceid.trim().equals("")) serviceid = xserviceid;
			if(setpid==null || setpid.trim().equals("")) setpid = xsetpid;
			if(language==null || language.trim().equals("")) language = xlanguage;
			
			//Customisation for Telecom Niger
			if(newrequest==null || newrequest.trim().equals("") || newrequest.trim().equals("1")) newrequest = "1";
			else newrequest = "0";
			
			
			respData = respUssd(msisdn, userInput, sessionId, newrequest, serviceid);
			String reqType = respData.endsWith("#1")?"FB":"FC";
			
			respData = respData.substring(0, respData.length()-2);
			
			if(service.getProp().getProperty("encode_response","0").trim().equals("1")) respData=URLEncoder.encode(respData,"UTF-8");
//			response.setHeader("RESPDATA", respData);
			response.setHeader("Freeflow", reqType);
			response.setHeader("charge", "N");
			response.setHeader("amount", "0");
			response.setHeader("cpRefId", "0");
			
		} catch(Exception e) {
			Logger.sysLog(LogValues.error, this.getClass().getName(), msisdn+", Exception: "+coreException.GetStack(e));
		}
		return respData;
	}
	
	@RequestMapping(value="/ussd/btc",method=RequestMethod.GET)
	@ResponseBody
	public String ussdBtcBotswana(
			@RequestParam("msisdn")String msisdn,
			@RequestParam("sessionid")String sessionId,
			@RequestParam("type")String type,
			@RequestParam("msg")String userInput){
		Logger.sysLog(LogValues.info, this.getClass().getName(),msisdn+ "user input  is : "+userInput);
		RequestXml requestxml=new RequestXml(msisdn,sessionId,"_E",userInput,(type.equals("1")||type.equals("2"))?"pull":"cleanup");
		Response resp=new Response();
		String response=null;
		resp=service.processUserRequest(requestxml);
		response="<ussd><type>"+(type.equals("1")||type.equals("2")?"2":"3")+"</type><msg>"+resp.getApplicationResponse()+"</msg></ussd>";
		Logger.sysLog(LogValues.info, this.getClass().getName(),msisdn+ "Final response is : "+response);
		return response;
	}
	
	public String respUssd(String msisdn,String userInput,String sessionId,String newrequest,String serviceid) {
		
		String type = "pull";
		Logger.sysLog(LogValues.info, this.getClass().getName(), msisdn+", zzz Got Request");

		if(msisdn==null||userInput==null) {
			return service.getProp().getProperty("mandatoryparammissing","Mandatory parameters are missing!");
		}
		
		if(userInput!=null && userInput.startsWith("*")) userInput=userInput.substring(1);
		if(userInput!=null && userInput.endsWith("#")) userInput=userInput.substring(0,userInput.length()-1);
		
		String response=null;
		boolean sessionEnd=false;
		
		if(newrequest==null || !newrequest.equals("0")){
			   utilities.cleanupRequest(msisdn,msisdn);
		}
		RequestXml requestxml = new RequestXml(msisdn, sessionId, "_E", userInput, type);
		//requestxml.getRequest(msisdn, msisdn, "_E", userInput, type);

		// check for postpaid
		int userAllowed = 0;
		if(utilities.getCheckAllowedUrl()!=null && !utilities.getCheckAllowedUrl().equals("") && !utilities.getCheckAllowedUrl().equals("NA")) {
			Logger.sysLog(LogValues.info, this.getClass().getName(), msisdn+", zzz checking if user is allowed");
			userAllowed = utilities.checkForAllowedUser(requestxml);
		}
		
		Logger.sysLog(LogValues.info, this.getClass().getName(), msisdn+", zzz userAllowed: "+userAllowed);
		switch (userAllowed) {
		
		case 0:
			
			if(serviceid==null || serviceid.trim().equals("")) {
				serviceid = utilities.checkForServiceId(requestxml);
			}
			if(serviceid==null || serviceid.trim().equals("")) {
				serviceid = utilities.checkForServiceId2(requestxml,requestxml.getSubscriberInput());
			}
			requestxml.setServiceid(serviceid);
			Logger.sysLog(LogValues.info, this.getClass().getName(), msisdn+", zzz serviceid: "+serviceid+" subscriberInput: "+requestxml.getSubscriberInput());
			
			if(requestxml.getServiceid()!=null && !requestxml.getServiceid().equals("")) utilities.checkForActiveUser(requestxml);
			Response resp=service.processUserRequest(requestxml);
			sessionEnd=utilities.checkSessionEnd(resp.getFreeflow());
			response=resp.getApplicationResponse();
			
			break;
		
		case 1:
			response = service.getProp().getProperty("usernotallowed");
			break;
			
		default:
			response = service.getProp().getProperty("error");
		
		}

		if(sessionEnd) response=response.concat("#1");
		else response=response.concat("#0");
		
		Logger.sysLog(LogValues.info, this.getClass().getName(),msisdn+ "Final response is : "+response+", sessionEnd: "+sessionEnd);
		return response;
	}
	
	
	@RequestMapping(value = "/reload", method = RequestMethod.GET)
	public @ResponseBody String reloadProperties(HttpServletRequest request, HttpServletResponse response) {
		String resp=null;
		try{
			service.initConfig();
			resp="Properties Reloaded";
		}catch(Exception ex){
			Logger.sysLog(LogValues.error, this.getClass().getName(), coreException.GetStack(ex));
			resp="Unable to reload properties";
		}
		return resp;
	}

	@RequestMapping(value = "/changeLogLevel/{level}")
	public void changeLogLevel(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("level") int level)
			throws Exception {
		try {
			Logger.sysLog(LogValues.APP_INFO, this.getClass().getName(),
					"Changing Log Level to " + level);
			Logger.setLogLevel(level);
			Logger.sysLog(LogValues.fatal, this.getClass().getName() + "("
					+ Thread.currentThread().getStackTrace()[1].getMethodName()
					+ ")", "Testing LogLevel Fatal");
			Logger.sysLog(LogValues.error, this.getClass().getName() + "("
					+ Thread.currentThread().getStackTrace()[1].getMethodName()
					+ ")", "Testing LogLevel Error");
			Logger.sysLog(LogValues.warn, this.getClass().getName() + "("
					+ Thread.currentThread().getStackTrace()[1].getMethodName()
					+ ")", "Testing LogLevel Warn");
			Logger.sysLog(LogValues.info, this.getClass().getName() + "("
					+ Thread.currentThread().getStackTrace()[1].getMethodName()
					+ ")", "Testing LogLevel Info");
			Logger.sysLog(LogValues.APP_INFO, this.getClass().getName() + "("
					+ Thread.currentThread().getStackTrace()[1].getMethodName()
					+ ")", "Testing LogLevel APP_INFO");
			Logger.sysLog(LogValues.APP_DEBUG, this.getClass().getName() + "("
					+ Thread.currentThread().getStackTrace()[1].getMethodName()
					+ ")", "Testing LogLevel APP_DEBUG");
			Logger.sysLog(LogValues.APP_TRACE, this.getClass().getName() + "("
					+ Thread.currentThread().getStackTrace()[1].getMethodName()
					+ ")", "Testing LogLevel APP_TRACE");
			Logger.sysLog(LogValues.debug, this.getClass().getName() + "("
					+ Thread.currentThread().getStackTrace()[1].getMethodName()
					+ ")", "Testing LogLevel Debug");
			Logger.sysLog(LogValues.trace, this.getClass().getName() + "("
					+ Thread.currentThread().getStackTrace()[1].getMethodName()
					+ ")", "Testing LogLevel Trace");
			response.sendRedirect(request.getContextPath());
		} catch (Exception e) {
			Logger.sysLog(
					LogValues.error,
					this.getClass().getName()
							+ "("
							+ Thread.currentThread().getStackTrace()[1]
									.getMethodName(), coreException.GetStack(e));
		}
	}
	

	
}
