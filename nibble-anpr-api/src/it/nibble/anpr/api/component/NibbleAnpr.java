package it.nibble.anpr.api.component;

import it.nibble.anpr.api.component.detection.PlateDetector;
import it.nibble.anpr.api.component.detection.PlateDetectorCascadeClassifier;
import it.nibble.anpr.api.component.ocr.OcrHandler;
import it.nibble.anpr.api.config.DetectionConfig;
import it.nibble.anpr.api.config.OcrConfig;
import it.nibble.anpr.api.model.Plate;
import it.nibble.anpr.api.util.OpenCvUtil;
import it.nibble.anpr.api.util.StorageUtil;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NibbleAnpr {

	@Autowired
	private PlateDetector plateDetector;
	
	@Autowired
	private PlateDetectorCascadeClassifier plateDetectorCascadeClassifier;
	
	@Autowired
	private OcrHandler ocrHandler;
	
	public void initialize() throws Exception {
		
		StorageUtil.setDebug(false);
		
		plateDetector.setDetectionConfig(new DetectionConfig());
		plateDetector.setStoreOutput(false);
		
		ocrHandler.setOcrConfig(new OcrConfig());
    	ocrHandler.setup();
		
	}
	
	public List<Plate> recognize(byte[] input) throws Exception {
						        
    	InputStream in = new ByteArrayInputStream(input);
		BufferedImage bufferedImage = ImageIO.read(in);
    	return recognize(OpenCvUtil.convert(bufferedImage));	    		       
	}

	public List<Plate> recognize(Mat input) throws Exception {
		
		List<Plate> result = new ArrayList<Plate>();
		
        try{            		    		    	
    		
    		plateDetector.setUuid(UUID.randomUUID().toString());
    		        	        	        	
        	List<Mat> plateRegions = plateDetector.detectPlateRegions(input);        	
        	for(Mat plateRegion : plateRegions){
        		
        		Plate plate = new Plate();
        		plate.setImg(plateRegion);
        		ocrHandler.recognize(plate);
        		
        		if(plate.getPlateNumber() != null && !"".equals(plate.getPlateNumber())){
        			result.add(plate);
        		}        		        		        	
        	}
        	
        	Collections.sort(result);
        	
        	return result;
        	
        } catch(Exception exc) {
        	throw exc;	
        }
		       
        
	}

	public List<Plate> recognizeWCC(Mat input) throws Exception {
		
		List<Plate> result = new ArrayList<Plate>();
		
        try{            		    		    	
    		
    		plateDetector.setUuid(UUID.randomUUID().toString());
    		        	        	        	
        	List<Mat> plateRegions = plateDetectorCascadeClassifier.detectPlateRegions(input);        	
        	for(Mat plateRegion : plateRegions){
        		
        		Plate plate = new Plate();
        		plate.setImg(plateRegion);
        		ocrHandler.recognize(plate);
        		
        		if(plate.getPlateNumber() != null && !"".equals(plate.getPlateNumber())){
        			result.add(plate);
        		}        		        		        	
        	}
        	
        	Collections.sort(result);
        	
        	return result;
        	
        } catch(Exception exc) {
        	throw exc;	
        }
		       
        
	}
	
}
