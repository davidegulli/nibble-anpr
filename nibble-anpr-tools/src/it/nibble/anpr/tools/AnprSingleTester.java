package it.nibble.anpr.tools;

import it.nibble.anpr.api.component.detection.PlateDetector;
import it.nibble.anpr.api.component.detection.PlateDetectorCascadeClassifier;
import it.nibble.anpr.api.component.ocr.OcrHandler;
import it.nibble.anpr.api.config.DetectionConfig;
import it.nibble.anpr.api.config.OcrConfig;
import it.nibble.anpr.api.model.Plate;
import it.nibble.anpr.api.util.AnprLogger;
import it.nibble.anpr.api.util.StorageUtil;

import java.util.List;
import java.util.UUID;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AnprSingleTester {

	private static AnprLogger anprLogger = AnprLogger.getInstane(AnprSingleTester.class);
	
    public static void main(String[] args) {
    	    	
    	anprLogger.trace("Start Main");
    		
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);        
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("application-context.xml");

//        Mat src = Highgui.imread("C:\\Developing\\Projects\\ANPR\\Test\\DR650HX.jpg");
//        Mat src = Highgui.imread("C:\\Developing\\Projects\\ANPR\\Tesseract-Training\\Learning-Images\\BV607ZK.jpg");
        
        Mat src = Imgcodecs.imread("C:\\Developing\\Projects\\ANPR\\Images\\Testcases-notlearned\\ES936XP.jpg");
//        Mat src = Imgcodecs.imread("C:\\Users\\Sw-Developing\\Desktop\\NewImage\\BW028XN.jpg");
        
//        Mat src = Imgcodecs.imread("C:\\Developing\\Projects\\ANPR\\Images\\Plate-Not-Recognize\\DP152YA.jpg");
        
        
        PlateDetector rectagleDetection = applicationContext.getBean(PlateDetector.class);
        PlateDetectorCascadeClassifier plateDetectorCascadeClassifier = new PlateDetectorCascadeClassifier();
        
        rectagleDetection.setDetectionConfig(new DetectionConfig());
        rectagleDetection.setUuid(UUID.randomUUID().toString());
        rectagleDetection.setStoreOutput(true);
        
        try{                	
        	
        	StorageUtil.setDebug(true);
        	
        	plateDetectorCascadeClassifier.detectPlateRegions(src);
        	
        	OcrHandler ocr = applicationContext.getBean(OcrHandler.class);
        	ocr.setOcrConfig(new OcrConfig());
        	ocr.setup();
//        	List<Mat> plateRegions = rectagleDetection.detectPlateRegions(src);
        	List<Mat> plateRegions = plateDetectorCascadeClassifier.detectPlateRegions(src);
        	for(Mat plateRegion : plateRegions){
        		
        		Plate plate = new Plate();
        		plate.setImg(plateRegion);
        		String result = ocr.recognize(plate);
        		
        		anprLogger.info("---------- Recognition Result: " + result + " - " + plate.getNntResultAverage() + " ---------");        		
        	}
        	
        } catch(Exception exc) {
        	exc.printStackTrace();        	
        }
         
        System.exit(0);
    }
    
	
}
