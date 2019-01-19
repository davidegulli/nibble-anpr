package it.nibble.anpr.api.util;

import org.apache.log4j.Logger;
import org.apache.log4j.pattern.LogEvent;

public class AnprLogger {

	private Logger logger;
	
	private AnprLogger(Class<?> c){
		this.logger = Logger.getLogger(c);
	}
	
	public static AnprLogger getInstane(Class<?> c){
		return new AnprLogger(c);  
	}

	public void trace(String message){
		logger.trace(message);
	}
	
	public void error(String message){
		logger.error(message);
	}
	
	public void info(String message){
		logger.info(message);
	}
	
	public void debug(String message){
		logger.debug(message);
	}
	
	public void warn(String message, Throwable throwable){
		logger.warn(message, throwable);
	}
	
	public void warn(String message){
		logger.warn(message);
	}
		
	public void error(String message, Throwable throwable){
		logger.error(message, throwable);
	}
	
	public void fatal(String message){
		logger.fatal(message);
	}
	
	public void fatal(String message, Throwable throwable){
		logger.fatal(message, throwable);		
	}
}
