package com.bng.ussd.util;

import org.apache.log4j.Level;


public class MyTraceLevel extends Level{
	
	public static final int APP_INFO_INT = 19000; 
	public static final int APP_APP_DEBUG_INT= 18000; 
	public static final int APP_TRACE_INT = 17000; 
	public static final Level APP_INFO = new MyTraceLevel(APP_INFO_INT,"APP_INFO",7);
	public static final Level APP_APP_DEBUG = new MyTraceLevel(APP_APP_DEBUG_INT,"APP_APP_DEBUG",8);
	public static final Level APP_TRACE = new MyTraceLevel(APP_TRACE_INT,"APP_TRACE",9);
	 
 

 protected MyTraceLevel(int arg0, String arg1, int arg2) {
         super(arg0, arg1, arg2);

     }

}
