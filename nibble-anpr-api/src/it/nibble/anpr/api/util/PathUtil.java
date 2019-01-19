package it.nibble.anpr.api.util;

import java.io.File;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

public class PathUtil {

	private static final String DEBUG_DIR = "debug";
	private static final String OUTPUT_DIR = "output";
	private static final String CHAR_LEARNING_SET_DIR = "char-learing-set";
	
	public static String getHomeDirectoryPath(){
		return System.getProperty("user.dir");
	}
	
	public static String getDebugPath(){
		StringBuilder result = new StringBuilder();
		result.append(getHomeDirectoryPath());
		result.append(File.separator);
		result.append(DEBUG_DIR);
		return result.toString();
	}
	
	public static String getOutputPath(){
		StringBuilder result = new StringBuilder();
		result.append(getHomeDirectoryPath());
		result.append(File.separator);
		result.append(OUTPUT_DIR);
		return result.toString();
	}
	
	public static String getDebugFilePath(String label){
		StringBuilder result = new StringBuilder();
		result.append(getHomeDirectoryPath());
		result.append(File.separator);
		result.append(DEBUG_DIR);
		result.append(File.separator);
		result.append(new Date().getTime());
		result.append("_");
		result.append(UUID.randomUUID());
		result.append("_");
		result.append(label);
		result.append(".jpg");
		return result.toString();
	}
	
	public static String getOutputFilePath(String label){
		StringBuilder result = new StringBuilder();
		result.append(getHomeDirectoryPath());
		result.append(File.separator);
		result.append(OUTPUT_DIR);
		result.append(File.separator);
		result.append(new Date().getTime());
		result.append("_");
		result.append(UUID.randomUUID());
		result.append("_");
		result.append(label);
		result.append(".jpg");
		return result.toString();
	}
	
	public static String getOutputCharFilePath(String fileName, String character, int index){
		StringBuilder result = new StringBuilder();
		result.append(getHomeDirectoryPath());
		result.append(File.separator);
		result.append(OUTPUT_DIR);
		result.append(File.separator);
		result.append(CHAR_LEARNING_SET_DIR);
		result.append(File.separator);
		result.append(character);
		result.append("_");
		result.append(index);
		result.append("_");
		result.append(fileName);
		result.append(".jpg");
		return result.toString();
	}
}
