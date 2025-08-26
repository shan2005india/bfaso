package com.bng.ussd.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.beans.factory.annotation.Autowired;

import com.bng.ussd.exception.coreException;
import com.bng.ussd.interfaces.Parser;
import com.bng.ussd.redis.JedisClientImpl;
import com.bng.ussd.wrapper.NotifySoapHeader;
import com.bng.ussd.wrapper.NotifyUssdReception;
import com.bng.ussd.wrapper.RequestXml;
import com.bng.ussd.wrapper.SoapResponse;



public class SoapParser implements Parser {
	
	@Autowired
	private RequestXml requestXml;
	@Autowired
	private NotifyUssdReception notifyUssd;
	@Autowired
	private JedisClientImpl jedis;
	@Autowired 
	private SoapResponse soapResponse;
	private boolean isNotifyReception;


	public RequestXml parseRequestString(String xmlFile){
		Logger.sysLog(LogValues.info, this.getClass().getName(), "In unmarshalling soap request");
		try{
		InputStream is = new ByteArrayInputStream(xmlFile.getBytes());
		SOAPMessage request = MessageFactory.newInstance().createMessage(null,is);
		SOAPHeader sh=request.getSOAPHeader();
		DOMSource source= new DOMSource(sh);
		StringWriter stringResult = new StringWriter();
		TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(stringResult));
		String header = stringResult.toString();
		XMLInputFactory xif = XMLInputFactory.newInstance();
		InputStream xml =new ByteArrayInputStream(header.getBytes()); 
		XMLStreamReader xsr = xif.createXMLStreamReader(xml); 
		xsr.nextTag();
		while(!xsr.getLocalName().equals("NotifySOAPHeader")){
			xsr.nextTag();
		}
		 JAXBContext jax = JAXBContext.newInstance(NotifySoapHeader.class);
		 Unmarshaller unmarshaller = jax.createUnmarshaller();
		  JAXBElement<NotifySoapHeader> jaxb = unmarshaller.unmarshal(xsr,NotifySoapHeader.class); 
		  try{
		 soapResponse.setLinkId(jaxb.getValue().getLinkid()==null?"123456":jaxb.getValue().getLinkid());
		  }catch(Exception ex){
			  coreException.GetStack(ex);
		  }
		SOAPBody sb = request.getSOAPBody();
	    source = new DOMSource(sb);
	    stringResult = new StringWriter();
		TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(stringResult));
		String message = stringResult.toString();
		 xif = XMLInputFactory.newInstance();
		 xml =new ByteArrayInputStream(message.getBytes()); 
		 xsr = xif.createXMLStreamReader(xml); 
		xsr.nextTag();
		xsr.nextTag();
		 if(xsr.getLocalName().equals("notifyUssdAbort")){
			 Logger.sysLog(LogValues.info, this.getClass().getName(), "In notify ussd abort");
			 setNotifyReception(false);
			 return requestXml;
		 }
		 Logger.sysLog(LogValues.info, this.getClass().getName(), "In notify ussd response");
		 setNotifyReception(true);
		 JAXBContext jc = JAXBContext.newInstance(NotifyUssdReception.class);
		  unmarshaller = jc.createUnmarshaller();
		  JAXBElement<NotifyUssdReception> jb = unmarshaller.unmarshal(xsr,NotifyUssdReception.class); 
		  notifyUssd = jb.getValue();
		 
		}catch(Exception ex){
			Logger.sysLog(LogValues.error, this.getClass().getName(), coreException.GetStack(ex));
		}
		
		  requestXml.setMsisdn(notifyUssd.getMsIsdn());
		  requestXml.setTransactionId(notifyUssd.getTransactionId());
		  requestXml.setSessionId(notifyUssd.getSessionId());
		  Logger.sysLog(LogValues.info, this.getClass().getName(), "String is : "+notifyUssd.getUssdString());
		  requestXml.setSubscriberInput(manipulateInput(notifyUssd.getUssdString(),notifyUssd.getMsIsdn(),notifyUssd.getSessionId()));
		 if( notifyUssd.getMsgType()==2){
			 requestXml.setType("cleanUp");
			 soapResponse.setOpType("2");
		 }
		 else{
			 requestXml.setType("pull");
		 }
		 Logger.sysLog(LogValues.info,this.getClass().getName() , "After unmarshalling : "+requestXml.getType()+" : msisdn : "+requestXml.getMsisdn()+" : msg type : "+notifyUssd.getMsgType());
		 return requestXml; 
		
	}
	
	public String manipulateInput(String ussd,String msisdn,String sessionId){
		ussd=ussd.split("#")[0].replaceFirst("\\*", "").trim();
		boolean existingUser = jedis.exists(sessionId + "_"
				+ msisdn);
		if(existingUser){
			Logger.sysLog(LogValues.info, this.getClass().getName(), "Already exists in database");
			if(ussd.contains("*")){
				 ussd=ussd.split("\\*")[1];
			 }
		}
			soapResponse.setOpType("1");
			soapResponse.setMsgType("1");
		 return ussd;
	}

	public boolean isNotifyReception() {
		return isNotifyReception;
	}

	public void setNotifyReception(boolean isNotifyReception) {
		this.isNotifyReception = isNotifyReception;
	}
	

}
