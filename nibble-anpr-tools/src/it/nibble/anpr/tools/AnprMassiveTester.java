package it.nibble.anpr.tools;

import it.nibble.anpr.api.component.NibbleAnpr;
import it.nibble.anpr.api.component.ocr.PlatePattern;
import it.nibble.anpr.api.model.Plate;
import it.nibble.anpr.api.util.AnprLogger;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AnprMassiveTester {

	private static AnprLogger anprLogger = AnprLogger.getInstane(BuildCharLearning.class);
	
	public static void main(String[] args){
		
		try {
							
	        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("application-context.xml");
	        
	        NibbleAnpr nibbleAnpr = applicationContext.getBean(NibbleAnpr.class);
	        nibbleAnpr.initialize();
	        
	        File inputPath = null;        
	        if(args.length == 1){
	        	inputPath = new File(args[0]);
	        } else {
	        	inputPath = new File("C:\\Developing\\Projects\\ANPR\\Images\\Testcases-notlearned");
	        }                       
	        
	        int positiveCounter = 0;
	        int negativeCounter = 0;                
	        int plateNotDetedtedCounter = 0;
	        int plateRecognizedCounter = 0;
	        
	        long startMassiveTime = new Date().getTime(); 
	        
	        for(String fileName : inputPath.list()){        	        	
	        	
	        	anprLogger.trace("---- Processing File: " + fileName);        	
	        	
	        	Mat src = Imgcodecs.imread(inputPath.getAbsolutePath() + File.separator + fileName);                       
	            
	            try{            	            	
	            	
	            	String actualPlate = null;
	            	if(fileName.indexOf("_") != -1){
	            		actualPlate = fileName.substring(0, fileName.indexOf("_"));
	            	} else {
	            		actualPlate = fileName.substring(0, fileName.indexOf("."));
	            	}
	            		            	
	            	long startTime = new Date().getTime();
//	            	List<Plate> results = nibbleAnpr.recognize(src);
	            	List<Plate> results = nibbleAnpr.recognizeWCC(src);
	            	long endTime = new Date().getTime();
	            	
	            	StringBuilder output = new StringBuilder();
	            	output.append("File: " + fileName);
	            	output.append(" - Actual Plate: " + actualPlate);
	            	output.append(" - Recognized Plate: ");
	            	
	            	if(results.size() > 0){
	            	
	            		String plateRecognized = null;
	            		boolean isPlateRecognized = false; 
		            	
		            	for(int i = results.size()-1; i > -1; i--){
		            		Plate recPlate = results.get(i);
		                        		
		            		output.append(" - " + recPlate.getPlateNumber() + ": " + (double)Math.round(recPlate.getNntResultAverage()*100000)/1000  + "% ");
		            		if(Pattern.matches(PlatePattern.PLATE_PATTERN, recPlate.getPlateNumber().subSequence(0, recPlate.getPlateNumber().length()))
		            			&& plateRecognized == null){
		            			plateRecognized = recPlate.getPlateNumber();
		            			output.append("*");
		            		}
		            		
		            		if(actualPlate.equals(recPlate.getPlateNumber()) && !isPlateRecognized){
		            			output.append("**");
		            			isPlateRecognized = true;
		            			plateRecognizedCounter++;
		            		}
		            		output.append(";");
		            	}
	            		
		            	output.append(" - Recognition Result: ");
		            	if(actualPlate.equals(plateRecognized)){
		            		output.append(" Positive");
		            		positiveCounter++;
		            	} else {
		            		output.append(" Negative");
		            		negativeCounter++;
		            	}    
		            	
	            	} else {
	            		plateNotDetedtedCounter++;	  
	            		output.append("Plate Not Detected ");
	            	}
	            		            		            		            	
	            	output.append(" - Processing Time: " + (double)(endTime-startTime)/1000 + "sec");
	            	
	            	anprLogger.info(output.toString());        	            	
	            	
	            } catch(Exception exc) {
	            	exc.printStackTrace();
	            } finally {
	            	src.release();
	            }        	                       
	        }                                                                                      
	        long endMassiveTime = new Date().getTime();	        
	        
	        int totalProcessedImages = positiveCounter+negativeCounter+plateNotDetedtedCounter;
	        
	        anprLogger.info("------ Recognition Result -------------------------------------------------- ");
	        anprLogger.info("Processed Images : " + (totalProcessedImages));
	        anprLogger.info("Positive Recognition: " + positiveCounter);
	        anprLogger.info("Negative Recognition: " + negativeCounter);	        
	        anprLogger.info("Positive Recognition Percentage: " + (Math.round(((double)((double)100/totalProcessedImages) * positiveCounter)*100)/100) + "%");
	        anprLogger.info("---------------------------------------------------------------------------- ");
	        anprLogger.info("Plate Not Found: " + (Math.round(((double)((double)100/totalProcessedImages) * plateNotDetedtedCounter)*100)/100) + "% - " + plateNotDetedtedCounter + "/" + totalProcessedImages);
	        anprLogger.info("Plate Found but Not Recognized: " + (Math.round(((double)((double)100/(totalProcessedImages-plateNotDetedtedCounter))*100)/100) * (negativeCounter - (plateRecognizedCounter-positiveCounter))) 
	        					+ "% - " + (negativeCounter - (plateRecognizedCounter-positiveCounter)) + "/" + (totalProcessedImages-plateNotDetedtedCounter));
	        anprLogger.info("Plate Found and Recognized as Not Primary: " + (Math.round(((double)((double)100/(totalProcessedImages-plateNotDetedtedCounter))*100)/100) * (plateRecognizedCounter-positiveCounter)) 
								+ "% - " + (plateRecognizedCounter-positiveCounter) + "/" + (totalProcessedImages-plateNotDetedtedCounter));
	        anprLogger.info("Plate Found and Recognized: " + (Math.round(((double)((double)100/(totalProcessedImages-plateNotDetedtedCounter)) * positiveCounter)*100)/100) 
								+ "% - " + (positiveCounter) + "/" + (totalProcessedImages-plateNotDetedtedCounter));
	        anprLogger.info("---------------------------------------------------------------------------- ");
	        anprLogger.info("Total Elaboration Time: " + (double)(endMassiveTime-startMassiveTime)/1000);
	        anprLogger.info("Average Elaboration Time: " + (double)((endMassiveTime-startMassiveTime)/(positiveCounter+negativeCounter))/1000);
	        anprLogger.info("---------------------------------------------------------------------------- ");
	        
		} catch (Exception e) {
			anprLogger.error(e.getMessage(), e);
		}
	}
	
}
