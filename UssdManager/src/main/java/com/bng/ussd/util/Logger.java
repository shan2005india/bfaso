package com.bng.ussd.util;

import java.util.Enumeration;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

public class Logger {
	
	public static void setLogLevel(int level)
	{
		try 
		{
			switch(level)
			{
			case LogValues.trace:
				setLogLevel(Level.TRACE);
				break;					
			case LogValues.debug:
				setLogLevel(Level.DEBUG);
				break;
			case LogValues.info:
				setLogLevel(Level.INFO);
				break;
			case LogValues.warn:
				setLogLevel(Level.WARN);
				break;
			case LogValues.error:
				setLogLevel(Level.ERROR);
				break;
			case LogValues.fatal:
				setLogLevel(Level.FATAL);
				break;	
			case LogValues.APP_INFO:
				setLogLevel(MyTraceLevel.APP_INFO);
				break;
			case LogValues.APP_DEBUG:
				setLogLevel(MyTraceLevel.APP_APP_DEBUG);
				break;
			case LogValues.APP_TRACE:
				setLogLevel(MyTraceLevel.APP_TRACE);
				break;
			}			
		} 
		catch (Exception e) 
		{
			System.err.println("Error while change log level: "+level);
		}		 
	}

	private static void setLogLevel(Level level)
	{
		LogManager.getRootLogger().setLevel(level);
		@SuppressWarnings("unchecked")
		Enumeration<Category> allLoggers = LogManager.getRootLogger().getLoggerRepository().getCurrentCategories();
		while (allLoggers.hasMoreElements())
		{
			Category category = (Category) allLoggers.nextElement();
			category.setLevel(level);			
		}
	}	 

	public static void sysLog(int severity, String applicationName, String message)
	{
		try
		{
			org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(applicationName);
			switch (severity) 
			{       	


			case LogValues.APP_INFO:
				log.log(MyTraceLevel.APP_INFO,message); 
				break;
			case LogValues.APP_DEBUG:
				log.log(MyTraceLevel.APP_APP_DEBUG,message); 
				break;
			case LogValues.APP_TRACE:
				log.log(MyTraceLevel.APP_TRACE,message); 
				break;
			case LogValues.fatal:
				log.fatal(message); 
				break;
			case LogValues.error:
				log.error(message); 
				break;
			case LogValues.warn: 
				log.warn(message); 
				break;
			case LogValues.info:
				log.info(message); 
				break;
			case LogValues.debug:
				log.debug(message); 
				break; 
			default:log.trace(message); 
				
			}
		}
		catch( Throwable e )
		{
			System.err.println("jHub: Error doing log severity: "+severity+" Application: "+applicationName+" Message: "+message );
			e.printStackTrace();
		}
	}

}
