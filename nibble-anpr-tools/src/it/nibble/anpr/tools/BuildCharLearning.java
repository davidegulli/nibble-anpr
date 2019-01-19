package it.nibble.anpr.tools;

import it.nibble.anpr.api.component.detection.PlateDetector;
import it.nibble.anpr.api.component.ocr.OcrHandler;
import it.nibble.anpr.api.model.Plate;
import it.nibble.anpr.api.util.AnprLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BuildCharLearning {

	private static AnprLogger anprLogger = AnprLogger.getInstane(BuildCharLearning.class); 
	
    public static void main(String[] args) {
    	
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("application-context.xml");
        
        File inputPath = null;        
        if(args.length == 1){
        	inputPath = new File(args[0]);
        } else {
//        	inputPath = new File("C:\\Developing\\Projects\\Anpr\\Testing\\basic-test\\snapshot");
        	inputPath = new File("C:\\Developing\\Projects\\ANPR\\Tesseract-Training\\Learning-Images");
        }
        
        File outputPath = null;
        if(args.length == 2){
        	outputPath = new File(args[1]);
        } else {
        	outputPath = new File("C:\\Developing\\Projects\\ANPR\\Tesseract-Training\\Learing-Set");
        }
        
        OcrHandler ocr = applicationContext.getBean(OcrHandler.class);
        try {
        	ocr.setup();
		} catch (Exception e) {
			// TODO: handle exception
		}
        
        
        Map<String,ArrayList<Plate>> resultMap = new LinkedHashMap<String,ArrayList<Plate>>();
        
        for(String fileName : inputPath.list()){        	        	
        	
        	anprLogger.trace("---- Processing File: " + fileName);
        	ocr.setSrcFileName(fileName);
        	
        	Mat src = Imgcodecs.imread(inputPath.getAbsolutePath() + File.separator + fileName);
            
            PlateDetector rectagleDetection = applicationContext.getBean(PlateDetector.class);           
            rectagleDetection.setUuid(UUID.randomUUID().toString());
            rectagleDetection.setStoreOutput(true);
            
            rectagleDetection.setStoreOutputPath(outputPath.getAbsolutePath());
            
            try{            	            	
            	
            	ArrayList<Plate> resultList = new ArrayList<Plate>();
            	
            	List<Mat> candidates = rectagleDetection.detectPlateRegions(src);        	            	
            	for(Mat candidate : candidates){
                	Plate plate = new Plate();
                	plate.setImg(candidate);
                	if(fileName.indexOf("_") != -1){
                		plate.setActualPlate(fileName.substring(0, fileName.indexOf("_")));
                	} else {
                		plate.setActualPlate(fileName.substring(0, fileName.indexOf(".")));
                	}
                	ocr.recognize(plate);
//                	ocr.recognizeForCreateTrainingSet(plate);
                	if(plate.getPlateNumber() != null && !"".equals(plate.getPlateNumber())){
                		resultList.add(plate);
                	}
                	
            	}
            	
            	Collections.sort(resultList);
            	
            	resultMap.put(fileName, resultList);            	            	
            	
            } catch(Exception exc) {
            	exc.printStackTrace();
            } finally {
            	src.release();
            }        	                       
        }                                                                        
        
        int positiveCounter = 0;
        int negativeCounter = 0;
        
        anprLogger.info("Massive Recognition Test Report");
        
        Iterator<String> keysIterator = resultMap.keySet().iterator();
        while(keysIterator.hasNext()){
        	String fileName = keysIterator.next();
        	ArrayList<Plate> results = resultMap.get(fileName);
        	
        	String actualPlate = null;
        	
        	if(fileName.indexOf("_") != -1){
        		actualPlate = fileName.substring(0, fileName.indexOf("_"));
        	} else {
        		actualPlate = fileName.substring(0, fileName.indexOf("."));
        	}
        	
        	StringBuilder output = new StringBuilder();
        	output.append("File: " + fileName);
        	output.append(" - Actual Plate: " + actualPlate);
        	output.append(" - Recognized Plate: ");
        	boolean result = false;
        	for(Plate recPlate : results){
        		output.append(" - " + recPlate.getPlateNumber());
        		if(!result && actualPlate.equals(recPlate.getPlateNumber())){
        			result = true;
        		}
        	}
        	output.append(" - Recognition Result: ");
        	if(result){
        		output.append(" Positive");
        		positiveCounter++;
        	} else {
        		output.append(" Negative");
        		negativeCounter++;
        	}    
        	
        	anprLogger.info(output.toString());
        }                
        
        anprLogger.info("Total Recognition Result: ");
        anprLogger.info("Positive Recognition: " + positiveCounter);
        anprLogger.info("Negative Recognition: " + negativeCounter);
        anprLogger.info("Total Recognition: " + resultMap.keySet().size());
        anprLogger.info("Positive Recognition Percentage: " + (100*positiveCounter)/resultMap.keySet().size());
        
    }
    

	
}
