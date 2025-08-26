package com.bng.ussd.cdr;

import org.springframework.beans.factory.annotation.Autowired;

import com.bng.ussd.exception.coreException;
import com.bng.ussd.queue.CdrQueueSender;
import com.bng.ussd.util.LogValues;
import com.bng.ussd.util.Logger;
import com.google.gson.Gson;

public class SendCdr {
	
	@Autowired
	private CdrQueueSender cdrSender;
	
	public void sendUssdCdr(UssdCdr ussdCdr){
		String cdrJson=convertObjectToJsonStr(ussdCdr);
		try{
			Logger.sysLog(LogValues.APP_DEBUG, this.getClass().getName(), "Going to send Cdr to Queue");
		cdrSender.sendToQ(cdrJson);
		}catch(Exception ex){
			Logger.sysLog(LogValues.error, this.getClass().getName(),coreException.GetStack(ex) );
		}
		Logger.sysLog(LogValues.info, this.getClass().getName(), ussdCdr.getMsisdn() + " : Cdr send to queue successfully");
	}
	
	public synchronized static String convertObjectToJsonStr(Object object)
    {    	
		 Gson gson = new Gson();
    	return gson.toJson(object);
    }


}
