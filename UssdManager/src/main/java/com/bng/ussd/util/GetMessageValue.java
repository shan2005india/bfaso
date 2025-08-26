package com.bng.ussd.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;

public class GetMessageValue {
	InputStream inputStream;
	@Autowired
	Properties prop;
	 
	public Properties  getMessageValues(){
 
		try {
			String propFileName ="message.properties";
			inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
			if (inputStream != null) {
				prop.load(inputStream);
				Logger.sysLog(LogValues.info, this.getClass().getName(), "Message properties file loaded successfully");
				return prop;
			}
		}catch(IOException ex){
			Logger.sysLog(LogValues.info, this.getClass().getName(), ex.toString());
		}
		return null;
}
	
}
