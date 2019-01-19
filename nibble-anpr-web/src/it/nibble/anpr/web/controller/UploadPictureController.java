package it.nibble.anpr.web.controller;

import it.nibble.anpr.api.component.NibbleAnpr;
import it.nibble.anpr.api.component.detection.PlateDetector;
import it.nibble.anpr.api.component.ocr.OcrHandler;
import it.nibble.anpr.api.config.DetectionConfig;
import it.nibble.anpr.api.config.OcrConfig;
import it.nibble.anpr.api.model.Plate;
import it.nibble.anpr.api.util.OpenCvUtil;
import it.nibble.anpr.api.util.StorageUtil;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * Handles requests for the application home page.
 */
@Controller
public class UploadPictureController {
	
	private static final Logger logger = LoggerFactory.getLogger(UploadPictureController.class);
	
	@Autowired
	private NibbleAnpr anpr;
	
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/upload", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {		
		return "pitcureUpload";
	}
	
	
	@RequestMapping(value = "/uploadPitcure", method = RequestMethod.POST)
    public @ResponseBody String uploadFileHandler(@RequestParam("name") String name,
            @RequestParam("file") MultipartFile file) {
		System.out.println("Upload File");
        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                
                /* da valutare se deve essere rimosso in un ambiente di produzione*/
                String rootPath = System.getProperty("catalina.home");
                System.setProperty("java.library.path", rootPath + "\\shared\\lib");                                
                Field fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
                fieldSysPath.setAccessible( true );
                fieldSysPath.set( null, null );
                
                System.loadLibrary(Core.NATIVE_LIBRARY_NAME); 
                long initTime = new Date().getTime();
                List<Plate> result = anpr.recognize(bytes);
                long endTime = new Date().getTime();
                
                String actualNumberPlate = (file.getName().indexOf('.') != -1) ? 
                							file.getName().substring(0, file.getName().indexOf('.')) : 
                							(file.getName().indexOf('_') != -1) ? file.getName().substring(0, file.getName().indexOf('_')) : "";
                
                StringBuilder resultString = new StringBuilder();
                resultString.append("Actual Number Plate: " + actualNumberPlate + "<br/>");                
                for(Plate plate : result){
                	resultString.append("Number Plate Recognized: " + plate.getPlateNumber() + " - Confidende:  " + plate.getNntResultAverage() + "<br/>");
                }
 
                resultString.append("Elapsed Time: " + ((float)(endTime-initTime)/1000) + "sec");
                
                return resultString.toString();
            } catch (Exception e) {
            	e.printStackTrace();
                return "You failed to upload " + name + " => " + e.getMessage();
            }
        } else {
            return "You failed to upload " + name
                    + " because the file was empty.";
        }
    }
		
}
