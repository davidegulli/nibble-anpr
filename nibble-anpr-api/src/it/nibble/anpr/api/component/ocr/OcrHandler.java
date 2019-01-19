package it.nibble.anpr.api.component.ocr;

import it.nibble.anpr.api.config.OcrConfig;
import it.nibble.anpr.api.model.CharSegment;
import it.nibble.anpr.api.model.Plate;
import it.nibble.anpr.api.util.AnprLogger;
import it.nibble.anpr.api.util.OpenCvUtil;
import it.nibble.anpr.api.util.PathUtil;
import it.nibble.anpr.api.util.StorageUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.imgrec.ImageRecognitionPlugin;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class OcrHandler {

	private static AnprLogger anprLogger = AnprLogger.getInstane(OcrHandler.class);
		
	private OcrConfig ocrConfig;	
	
	private static NeuralNetwork nnet;
	private static ImageRecognitionPlugin imageRecognition;
	
	private String srcFileName;
	
	public void setup() throws FileNotFoundException {
		if(nnet == null){
			
			/* Parameter: ocr.nnet.file.location */
			nnet = NeuralNetwork.load(new FileInputStream(PathUtil.getHomeDirectoryPath() + File.separator + ocrConfig.getNnetFileLocation()));
//			nnet = NeuralNetwork.load(OcrHandler.class.getResourceAsStream("OCR4ANPR_NNP.nnet")); // load trained neural network saved with Neuroph Studio
			imageRecognition = (ImageRecognitionPlugin)nnet.getPlugin(ImageRecognitionPlugin.class); // get the image recognition plugin from neural network
			
		}
	}
	
	public String recognize(Plate plate) throws Exception {
	    
	    //Segment chars of plate
	    List<CharSegment> charSegments = segment(plate);
	    /* Parameter: ocr.min.segment.accepted*/
	    if(charSegments.size() >= ocrConfig.getMinSegmentAccepted()){
		    Collections.sort(charSegments);
		    
		    double nntResultAverage = 0;
		    
		    int characterIndex = 0;
		    for(CharSegment charSegment : charSegments){
		    	
		    	charSegment.setCharacterIndex(characterIndex);
		    			    			    			    
//		    	recognizeCharWithTesseract(charSegment, charSegments.size());		    	
		        recognizeChar(charSegment, charSegments.size());
		    	StorageUtil.storeOutputChar(charSegment.getImage(), srcFileName, String.valueOf(charSegment.getCharacter()), characterIndex);
		        nntResultAverage += charSegment.getNntResultPercentage();
		        characterIndex++;
		    }
		    
		    plate.setNntResultAverage(nntResultAverage / charSegments.size());
	    }
	    
	    plate.setCharSegments(charSegments);
	    
	    return plate.getPlateNumber();
	}
		
	public String recognizeForCreateTrainingSet(Plate plate) throws Exception {
	    
	    //Segment chars of plate
	    List<CharSegment> charSegments = segment(plate);
	    Collections.sort(charSegments);
	    
	    double nntResultAverage = 0;
	    int characterIndex = 0;
	    for(CharSegment charSegment : charSegments){
	        recognizeChar(charSegment, charSegments.size());	        
	    	
	        /*
	         * Le istruzioni seguenti vengono intrdotte per la creazione dei training set in maniera automatica
	         */
	        if(plate.getActualPlate() != null && !"".equals(plate.getActualPlate()) && charSegments.size() == 7){
	        	charSegment.setCharacter(plate.getActualPlate().charAt(characterIndex));
	        }
	        
	    	StorageUtil.storeOutputChar(charSegment.getImage(), srcFileName, String.valueOf(charSegment.getCharacter()), characterIndex);
	    	nntResultAverage += charSegment.getNntResultPercentage();
	    	characterIndex++;
	    }
	    
	    plate.setNntResultAverage(nntResultAverage / charSegments.size());
	    plate.setCharSegments(charSegments);
	    
	    return plate.getPlateNumber();
	}
	
	public List<CharSegment> segment(Plate plate) throws Exception {
		
		List<CharSegment> output = new ArrayList<CharSegment>();
		Mat input = plate.getImg();				    
		    
		StorageUtil.storeDebugImage(input, "THRESHOLD_PLATE");
		        
		Mat img_contours = new Mat();
		input.copyTo(img_contours);
		
		//Find contours of possibles characters
		Mat hierarchy = new Mat();
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();		    		
		Imgproc.findContours(img_contours,
				       		 contours, // a vector of contours
				       		 hierarchy,
				       		 Imgproc.RETR_EXTERNAL, // retrieve the external contours
				       		 Imgproc.CHAIN_APPROX_NONE); // all pixels of each contours		    	
		
		int charIndex = 0;
	    //Start to iterate to each contour founded		    
	    //Remove patch that are no inside limits of aspect ratio and area.    
	    for (MatOfPoint contour : contours) {
	    	
	    	charIndex++;
	    	
	        //Create bounding rect of object
	        Rect mr = Imgproc.boundingRect(contour);
//	        Core.rectangle(result, mr.tl(), mr.br(), new Scalar(0,255,0));
	        
	        //Crop image
	        Mat auxRoi = new Mat(input, mr);
	        StorageUtil.storeDebugImage(auxRoi, "CHAR_" + charIndex);
	        
	        if(verifyCharSize(auxRoi, charIndex)){
	        	auxRoi=preprocessChar(auxRoi);
	        	/*
	        	 * Parameters: ocr.char.image.threwshold.value, ocr.char.image.threwshold.maxvalue
	        	 */
	        	Imgproc.threshold(auxRoi, auxRoi, 60, 255, Imgproc.THRESH_BINARY_INV);	        	
	        	
	        	CharSegment charSegment = new CharSegment();
	        	charSegment.setImage(auxRoi);
	        	charSegment.setPosition(mr);
	        	output.add(charSegment);		        
	        }		        
	    }
	    
	    anprLogger.debug("Number of chars: " + output.size());
//	    StorageUtil.storeDebugImage(result, "CHAR_SEGMENTATION_RESULT");
	
		return output;
	}
	
	private boolean verifyCharSize(Mat charImg, int charIndex){
			    	    	    
	    //Char Aspect
	    float charAspect = (float)charImg.cols() / (float)charImg.rows();
	    
	    //area of pixels
	    float area = Core.countNonZero(charImg);	    
	    //bb area
	    float bbArea = charImg.cols() * charImg.rows();
	    //% of pixel in area
	    float percPixels = area / bbArea;

	    anprLogger.trace("Char: " + charIndex + " - Char aspect " + charAspect + 
	    		         " [" + ocrConfig.getCharAcceptedDimensionsAspectMin() + "," + ocrConfig.getCharAcceptedDimensionsAspectMax() + "] " + 
	    		         "Area " + percPixels + " Height char " + charImg.rows());
	    
		/*
		 * Parameter: ocr.char.accepted.dimensions.nonzero.pixel.limit
		 * 			  ocr.char.accepted.dimensions.height.min
		 * 			  ocr.char.accepted.dimensions.height.max
		 * 			  ocr.char.accepted.dimensions.aspect.min
		 * 			  ocr.char.accepted.dimensions.aspect.max
		 */
	    if(percPixels < ocrConfig.getCharAcceptedDimensionsNonZeroPixelLimit() 
	    	&& charAspect > ocrConfig.getCharAcceptedDimensionsAspectMin() 
	    	&& charAspect < ocrConfig.getCharAcceptedDimensionsAspectMax() 
	    	&& charImg.rows() >= ocrConfig.getCharAcceptedDimensionsHeightMin() 
	    	&& charImg.rows() <= ocrConfig.getCharAcceptedDimensionsHeightMax()) {
	        return true;
	    } else {
	        return false;
	    }
		
	}
	
	private Mat preprocessChar(Mat charImg) throws Exception {
	
	    //Remap image
	    int h = charImg.rows();
	    int w = charImg.cols();
	    Mat transformMat = Mat.eye(2,3,CvType.CV_32F);
	    int m = (int)Math.max((w*1.6),(h*1.6));
	    transformMat.put(0,2,new float[]{(m/2 - w/2)});
	    transformMat.put(1,2,new float[]{(m/2 - w/2)});	    
	    	   
	    Mat warpImage = new Mat(m, m, charImg.type());
	    Imgproc.warpAffine(charImg, warpImage, transformMat, warpImage.size(), Imgproc.INTER_LINEAR, /*Imgproc.BORDER_CONSTANT*/ 0, new Scalar(0) );
//	    StorageUtil.storeDebugImage(charImg, "warpAffine");
	    	    
	    Mat out = new Mat();
	    /*
	     * Parameters: ocr.image.char.dimensions.width, 
	     * 			   ocr.image.char.dimensions.height 
	     */
	    Imgproc.resize(warpImage, out, new Size(ocrConfig.getImageCharDimensionsWidth(), 
	    										 ocrConfig.getImageCharDimensionsHeight()) );	    

	    return out;
	}

	private CharSegment recognizeChar(CharSegment charSegment, int charsNumber) throws Exception {
		
		String output = null;				
	    
         // image recognition is done here (specify some existing image file)
        HashMap<String, Double> outputMap = imageRecognition.recognizeImage(OpenCvUtil.convert(charSegment.getImage()));
        
        double bestResult = 0;
        
        Iterator<String> keysIterator = outputMap.keySet().iterator();
        while(keysIterator.hasNext()){
        	String key = keysIterator.next();
        	double result = outputMap.get(key);
        	if(bestResult < result){
        		bestResult = result;
        		output = key;
        	}
        }
         
        charSegment.setCharacter(output.charAt(0));
        charSegment.setNntResultPercentage(bestResult);       
        PlatePattern.applyCharPattern(charSegment, outputMap, charsNumber);        
        
        anprLogger.trace("Char Recognition - OutputMap: " + outputMap.toString());
        anprLogger.debug("Char Recognition - Original Output Char: " + output);
        if(charSegment.getCharacter() != null && !"".equals(charSegment.getCharacter())){
        	anprLogger.debug("Char Recognition - Checked Output Char: " + charSegment.getCharacter());
        }
	    
        //Debug
        StorageUtil.storeOutputChar(charSegment.getImage(), srcFileName, String.valueOf(charSegment.getCharacter()), charsNumber);
        
		return charSegment;
		
	}
	
	public String getSrcFileName() {
		return srcFileName;
	}

	public void setSrcFileName(String srcFileName) {
		this.srcFileName = srcFileName;
	}

	public OcrConfig getOcrConfig() {
		return ocrConfig;
	}

	public void setOcrConfig(OcrConfig ocrConfig) {
		this.ocrConfig = ocrConfig;
	}	
		
	
//	private Mat projectedHistogram(Mat img, int type) throws Exception {
//		
//	    int size = (type == HORIZONTAL) ? img.rows() : img.cols();
//	    Mat mhist = Mat.zeros(1,size,CvType.CV_32F);
//
//	    for(int j=0; j < size; j++){
//	        Mat data = (type == HORIZONTAL) ? img.row(j) : img.col(j);
//	        mhist.put(0, size, Core.countNonZero(data));
//	    }
//
//
//	    MinMaxLocResult mmlr = Core.minMaxLoc(mhist);
//	    
//	    if(mmlr.maxVal > 0) {
//	        mhist.convertTo(mhist,-1 , 1.0f / mmlr.maxVal, 0);
//	    }
//
//	    return mhist;
//	}
//
//	private Mat features(Mat in, int sizeData) throws Exception {
//		
//	    //Histogram features
//	    Mat vhist = projectedHistogram(in,VERTICAL);
//	    Mat hhist = projectedHistogram(in,HORIZONTAL);
//	    
//	    //Low data feature
//	    Mat lowData = new Mat();
//	    Imgproc.resize(in, lowData, new Size(sizeData, sizeData));
//
//	    //Debug
//        drawVisualFeatures(in, hhist, vhist, lowData);
//	    	    
//	    //Last 10 is the number of moments components
//	    int numCols = vhist.cols() + hhist.cols() + lowData.cols() * lowData.cols();
//	    
//	    Mat out = Mat.zeros(1,numCols, CvType.CV_32F);
//	    
//	    //Asign values to feature
//	    int j=0;
//	    for(int i=0; i < vhist.cols(); i++){
//	        out.put(0, j, vhist.get(0, i));
//	        j++;
//	    }
//	    
//	    for(int i=0; i<hhist.cols(); i++) {
//	    	out.put(0, j, hhist.get(0, i));
//	        j++;
//	    }
//	    
//	    for(int x=0; x<lowData.cols(); x++){
//	        for(int y=0; y<lowData.rows(); y++){
//	        	out.put(0, j, lowData.get(x, y));	            
//	            j++;
//	        }
//	    }
//
//	    return out;
//	}
//
//	private void drawVisualFeatures(Mat character, Mat hhist, Mat vhist, Mat lowData) throws Exception {
//	    
//		Mat img = new Mat(121, 121, CvType.CV_8UC3, new Scalar(0,0,0));
//	    Mat ch = new Mat();
//	    Mat ld = new Mat();
//	    
//	    Imgproc.cvtColor(character, ch, Imgproc.COLOR_GRAY2RGB);
//
//	    Imgproc.resize(lowData, ld, new Size(100, 100), 0, 0, Imgproc.INTER_NEAREST);
//	    Imgproc.cvtColor(ld,ld, Imgproc.COLOR_GRAY2RGB);
//	
//
//	    Mat hh = getVisualHistogram(hhist, HORIZONTAL);
//	    Mat hv = getVisualHistogram(vhist, VERTICAL);
//
//	    Mat subImg = new Mat(img, new Rect(0,101,20,20));
//	    ch.copyTo(subImg);
//
//	    subImg = new Mat(img, new Rect(21,101,100,20));
//	    hh.copyTo(subImg);
//
//	    subImg = new Mat(img, new Rect(0,0,20,100));
//	    hv.copyTo(subImg);
//
//	    subImg = new Mat(img, new Rect(21,0,100,100));
//	    ld.copyTo(subImg);
//
//	    Core.line(img, new Point(0,100), new Point(121,100), new Scalar(0,0,255));
//	    Core.line(img, new Point(20,0), new Point(20,121), new Scalar(0,0,255));
//
//	    StorageUtil.storeDebugImage(img, "Features");
//
//	}
//
//	private Mat getVisualHistogram(Mat hist, int type) throws Exception {
//
//	    int size=100;
//	    Mat imHist = new Mat();
//
//	    if(type==HORIZONTAL){
//	        imHist.create(new Size(size, hist.cols()), CvType.CV_8UC3);
//	    }else{
//	        imHist.create(new Size(hist.cols(), size), CvType.CV_8UC3);
//	    }
//	    
//	    imHist.setTo(new Scalar(55,55,55));
//
//	    for(int i=0; i < hist.cols();i++){
//	    	
//	        float value = (float)(hist.get(0, i))[0];
//	        
//	        int maxval=(int)(value*size);
//
//	        Point pt1 = new Point();
//	        Point pt2 = new Point(); 
//	        Point pt3 = new Point();
//	        Point pt4 = new Point();
//
//	        if(type==HORIZONTAL){
//	        	
//	            pt1.x=pt3.x=0;
//	            pt2.x=pt4.x=maxval;
//	            pt1.y=pt2.y=i;
//	            pt3.y=pt4.y=i+1;
//
//	            Core.line(imHist, pt1, pt2, new Scalar(220,220,220),1,8,0);
//	            Core.line(imHist, pt3, pt4, new Scalar(34,34,34),1,8,0);
//
//	            pt3.y=pt4.y=i+2;
//	            Core.line(imHist, pt3, pt4, new Scalar(44,44,44),1,8,0);
//	            pt3.y=pt4.y=i+3;
//	            Core.line(imHist, pt3, pt4, new Scalar(50,50,50),1,8,0);
//	            
//	        }else{
//
//                pt1.x=pt2.x=i;
//                pt3.x=pt4.x=i+1;
//                pt1.y=pt3.y=100;
//                pt2.y=pt4.y=100-maxval;
//
//	            Core.line(imHist, pt1, pt2, new Scalar(220,220,220),1,8,0);
//	            Core.line(imHist, pt3, pt4, new Scalar(34,34,34),1,8,0);
//
//	            pt3.x=pt4.x=i+2;
//	            Core.line(imHist, pt3, pt4, new Scalar(44,44,44),1,8,0);
//	            pt3.x=pt4.x=i+3;
//	            Core.line(imHist, pt3, pt4, new Scalar(50,50,50),1,8,0);
//
//	        }
//	    }
//
//	    return imHist ;
//	}	
}
