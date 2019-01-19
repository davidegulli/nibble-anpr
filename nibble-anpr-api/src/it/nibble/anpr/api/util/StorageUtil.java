package it.nibble.anpr.api.util;

import java.io.File;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class StorageUtil {

	private static boolean debug = false; 
	
	public synchronized static void storeDebugImage(Mat img, String label) throws Exception {
		
		if(debug){
			String filePath = PathUtil.getDebugFilePath(label);
			if(filePath != null && img != null){
				Imgcodecs.imwrite(filePath, img);
			}
		}
		
	}
	
	public synchronized static void storeOutputImage(Mat img, String label) throws Exception {
		
		String filePath = PathUtil.getOutputFilePath(label);
		Imgcodecs.imwrite(filePath, img);
		
	}
	
	public synchronized static void storeOutputChar(Mat img, String fileName, String character, int index) throws Exception {
		
		String filePath = PathUtil.getOutputCharFilePath(fileName, character, index);
		Imgcodecs.imwrite(filePath, img);
		
	}
	
	public synchronized static void storeOutputImage(Mat img, File path) throws Exception {
				
		Imgcodecs.imwrite(path.getAbsolutePath(), img);
		
	}

	public static boolean isDebug() {
		return debug;
	}

	public static void setDebug(boolean debug) {
		StorageUtil.debug = debug;
	}
		
}
