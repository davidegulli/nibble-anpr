package it.nibble.anpr.api.component.detection;

import it.nibble.anpr.api.config.DetectionConfig;
import it.nibble.anpr.api.util.AnprLogger;
import it.nibble.anpr.api.util.StorageUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class PlateDetectorReNew {
	
	private static AnprLogger anprLogger = AnprLogger.getInstane(PlateDetectorReNew.class);
		
	private DetectionConfig detectionConfig;
	
	private String uuid;
	private boolean storeOutput;
	private String storeOutputPath;
			
	private int detectionIndex;	
	
    public List<Mat> detectPlateRegions(Mat src) throws Exception {
    	
        Mat dest = new Mat();
        Mat mask = null;
        
        try {
        
	        Imgproc.cvtColor(src, dest, Imgproc.COLOR_BGR2GRAY);	        	        
	        //Debug
	        StorageUtil.storeDebugImage(dest, "COLOR_BGR2GRAY");	       
	        
	        Imgproc.blur(dest, dest, new Size(5,5));
	        //Debug
	        StorageUtil.storeDebugImage(dest, "blur");
	        
//	        CLAHE clahe = Imgproc.createCLAHE(2.0, new Size(8,8));
//	        clahe.apply(dest, dest);
//	        Imgproc.equalizeHist(dest, dest);
	        //Debug
//	        StorageUtil.storeDebugImage(dest, "equalizeHist");        	        	        	       
	        
	        Imgproc.Sobel(dest, dest, CvType.CV_8U, 1, 0, 3, 1, 0);	        
	        //Debug
	        StorageUtil.storeDebugImage(dest, "sobel");        
	               
	        Imgproc.threshold(dest, dest, 0, 255, Imgproc.THRESH_OTSU+Imgproc.THRESH_BINARY); 
	        //Debug
	        StorageUtil.storeDebugImage(dest, "threshold");
	        
	        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(17, 3));        
	        Imgproc.morphologyEx(dest, dest, Imgproc.MORPH_CLOSE, element);	        
	        //Debug
	        StorageUtil.storeDebugImage(dest, "morphologyEx");
	        
	        List<MatOfPoint> contours = new ArrayList<>();
	        Imgproc.findContours(dest, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
	        Imgproc.drawContours(dest, contours, -1, new Scalar(255,255,0));        
	        
	        //Debug
	        StorageUtil.storeDebugImage(dest, "D4");                            	    	        	        
	        
	        /*
	         * Logging
	         */
	        if(uuid != null && !"".equals(uuid)){
	        	anprLogger.trace("-------- Plate Detection: " + uuid + " --------");
	        }
	        
	        List<Mat> results = new ArrayList<Mat>();	       
	         
	        int iterationCounter = 0;	        	       
	        
	        for (MatOfPoint contour : contours) {
	        	
	        	detectionIndex++;
	        	iterationCounter++;
	        	
//	        	Mat test = new Mat(dest.rows(), dest.cols(), CvType.CV_8UC1);
//	        	for(Point point : contour.toArray()){
//	        		Core.line(test, point, point, new Scalar(255));
//	        	}
//	        	StorageUtil.storeDebugImage(test, "CONTOURS_TEST");
	        	
		        /*
		         * Se e' stato rilevato un numero eccessivo di contorni evito l'elaborazione
		         * a causa di un eccessivo deterioramento delle performance
		         * (Dai test e' emerso che la targa viene rilevata correttamente ma con tempi molto lunghi)
		         * 
		         * Parameter: detection.contour.max.iteration
		         */
	        	if(detectionConfig.getContourMaxIterations() != -1 
	        		&& iterationCounter > detectionConfig.getContourMaxIterations()){
	        		
	        		anprLogger.trace("Image dirty the iteration has been stopped ");
	        		break;
	        	}
	        	
	        	anprLogger.trace("-------- Region Detection - uuid-index:" + uuid + " - " + detectionIndex + " --------");        
	        	MatOfPoint2f mop2f = new MatOfPoint2f(contour.toArray());	        		        		        	
	        	RotatedRect plateRectCandidate = Imgproc.minAreaRect(mop2f);        	        	        	
	        	
//	        	Core.rectangle(test, plateRectCandidate.boundingRect().tl(), plateRectCandidate.boundingRect().br(), new Scalar(255));
//	        	StorageUtil.storeDebugImage(test, "BOUNDING_TEST");
	        	
	        	mask = floodFill(plateRectCandidate, src, dest);        	        	
	        	
	        	
	        	if(mask == null){
	    			continue;
	    		}	        	
	        	
	        	Mat img_crop = new Mat();
				//	        	img_crop.submat(new Rect(plateRectCandidate.boundingRect().x-50, plateRectCandidate.boundingRect().y-50, 
				//				plateRectCandidate.boundingRect().width+50, plateRectCandidate.boundingRect().height+50));
				Imgproc.getRectSubPix(mask, new Size(plateRectCandidate.size.width+100, plateRectCandidate.size.height+100), plateRectCandidate.center, img_crop);								
				
				//Debug
				StorageUtil.storeDebugImage(img_crop, "img_crop_" + detectionIndex);
	        	
	        	//Debug
        		StorageUtil.storeDebugImage(mask, "MASK_" + detectionIndex); 
	        	
//        		if(detectionIndex == 61){
//        			//Debug
//            		StorageUtil.storeDebugImage(mask, "MASK_" + detectionIndex);
//        		}
        		
	        	/*
	        	 * Viene calcolata l'esatta dimensione della mask trovata 
	        	 * (la dimensione del RotatedRect non sembra essere corretta)
	        	 */
	        	Size candidateSize = getMaskDimensions(mask, plateRectCandidate.center);
	        	
	        	/*
	        	 * Viene verificato se la dimensione della mask corrisponde alle proporzioni
	        	 * della forma geometrica di una targa        	
	        	 */
	        	if(verifyDimension(candidateSize)){        		        		        		        		
	        		
	        		//Debug
	        		StorageUtil.storeDebugImage(mask, "MASK_" + uuid + "-" + detectionIndex);
	                
	        		Mat plateRegion = detectPlateRegion(mask, src);
	        		if(plateRegion != null){        			        
	            		results.add(plateRegion);
	            		if(storeOutput){
	            			StorageUtil.storeOutputImage(plateRegion, new Date().getTime()+"_"+uuid);
	            		}
	        		}
	        	}
	        	
			}	    
	        
	        return results;
	        
        } catch (Exception exc) {
        	throw exc;
        } finally {
        	if(mask != null){
        		mask.release();
        	}
        	dest.release();
        }
	                        
    }
    
    private Mat floodFill(RotatedRect candidate, Mat src, Mat result) throws Exception {
    	
    	//For better rect cropping for each possible box
    	//Make floodfill algorithm because the plate has white background
    	//And then we can retrieve more clearly the contour box
    	
    	if(candidate.size.width*candidate.size.height == 0){
    		return null;
    	}       
    	
    	//get the min size between width and height
    	double minSize=(candidate.size.width < candidate.size.height) ? candidate.size.width : candidate.size.height;    	
    	minSize = minSize - minSize * 0.5;
    	if(minSize < 1){
    		return null;
    	}
    	
    	//initialize rand and get 5 points around center for floodfill
    	//Initialize floodfill parameters and variables
    	Mat mask = new Mat(src.rows() + 2, src.cols() + 2, CvType.CV_8UC1);    	
    	
    	/*
    	 * Parameter: detection.floodfill.lower.difference
    	 */
    	int loDiff = detectionConfig.getFloodfillLowerDifference();
    	
    	/*
    	 * Parameter: detection.floodfill.upper.difference
    	 */
    	int upDiff = detectionConfig.getFloodfillUpperDifference();
    	
    	int connectivity = 4;
    	int newMaskVal = 255;
    	Rect ccomp = new Rect();    
    	
    	int flags = connectivity + (newMaskVal << 8 ) + Imgproc.FLOODFILL_FIXED_RANGE + Imgproc.FLOODFILL_MASK_ONLY;
    		
    	Point seed = new Point(candidate.center.x, candidate.center.y);
	    	
    	Imgproc.floodFill(src, mask, seed, new Scalar(255,0,0), ccomp,
				new Scalar(loDiff, loDiff, loDiff), new Scalar(upDiff, upDiff, upDiff),flags);
    	    	    	
    	return mask;
    }
    
    private Mat detectPlateRegion(Mat mask, Mat src) throws Exception {
    	    	    	
    	//Check new floodfill mask match for a correct patch.
    	//Get all points detected for minimal rotated Rect    	

    	/*
    	 * Scorrendo l'oggeto mask viene creata la lista di Point di interesse 
    	 */
    	List<Point> pointsInterest = new ArrayList<Point>();    	    	
    	for(int colIndex = 0, colsSize = mask.cols(); colIndex < colsSize; colIndex++){
    		for(int rowIndex = 0, rowsSize = mask.rows(); rowIndex < rowsSize; rowIndex++){    		    	
    			double[] matPoint = mask.get(rowIndex, colIndex);    			
    			if(matPoint[0] == 255){    				
        			Point point = new Point(colIndex, rowIndex);
        			pointsInterest.add(point);
    			} 	
    		}    		
    	}
    	    	
    	if(pointsInterest.size() > 0){
    		
    		Point[] pointsInterestArray = new Point[pointsInterest.size()];
    		for(int index = 0, sizei = pointsInterestArray.length; index < sizei; index++){
    			pointsInterestArray[index] = pointsInterest.get(index);
    		}    	    		
    		
			RotatedRect minRect = Imgproc.minAreaRect(new MatOfPoint2f(pointsInterestArray));					
			
			anprLogger.trace("candidate.size.width:" + minRect.size.width);
			anprLogger.trace("candidate.size.height:" + minRect.size.height);
			
			if(verifyDimension(minRect)){
				
				double r = minRect.size.width / minRect.size.height;
				double angle = minRect.angle;
				anprLogger.trace("Plate rect angle:" + angle);
				if(r < 1){
					angle=90+angle;
				}
				
				Mat rotmat = Imgproc.getRotationMatrix2D(minRect.center, angle,1.0);						       
				
				Mat img_rotated = new Mat();
				Imgproc.warpAffine(src, img_rotated, rotmat, src.size(), Imgproc.INTER_CUBIC);
				
				rotmat.release();
				
				//Debug
		        StorageUtil.storeDebugImage(img_rotated, "IMG_ROTATED");    				
				
				//Crop image			
				if(r < 1) {
					double tmp = minRect.size.width;				
					minRect.size.width = minRect.size.height;
					minRect.size.height = tmp;
				}
				
	            //Crop image
	            Size rect_size=minRect.size;
	            
	            Mat img_crop = new Mat();
	            Imgproc.getRectSubPix(img_rotated, rect_size, minRect.center, img_crop);
	            
	            img_rotated.release();
	            
	            //Debug
		        StorageUtil.storeDebugImage(img_crop, "IMG_CROPED");	            
	            
	            Mat resultResized = new Mat();
	            resultResized.create(33,144, CvType.CV_8UC3);
	            Imgproc.resize(img_crop, resultResized, resultResized.size(), 0, 0, Imgproc.INTER_CUBIC);
	            
	            img_crop.release();
	            
	            //Equalize croped image
	            Mat grayResult = new Mat();
	            Imgproc.cvtColor(resultResized, grayResult, Imgproc.COLOR_BGR2GRAY); 
	            Imgproc.blur(grayResult, grayResult, new Size(3,3));
	            	            
	            grayResult = histeq(grayResult);
	            StorageUtil.storeDebugImage(grayResult, "PLATE_Clahe_applyed");
	            	            
	    		/*
	    		 * Threshold input image
	    		 * Viene applicato il Thresholding in questo punto per megliorare le performance
	    		 * eseguendo questo algoritmo una sola volta  
	    		 * parameter: detection.result.threwshold.value, detection.result.threwshold.maxvalue
	    		 */
	    		Mat img_threshold = new Mat();
	    		Imgproc.threshold(grayResult, img_threshold, detectionConfig.getResultThresholdValue(), 
	    							detectionConfig.getResultThresholdMaxvalue(), Imgproc.THRESH_BINARY_INV);
	    		
	    		StorageUtil.storeDebugImage(img_threshold, "PLATE_PRE_GRAPH");	            
	            
	            if(isPlate(img_threshold)){
	            	return img_threshold;
	            }	            	            
	            
			}
    	}
	    
    	return null;
    	
    }
    
    private Mat histeq(Mat in) {

    	Mat out = new Mat(in.size(), in.type());
        if(in.channels()==3){
            Mat hsv = new Mat();
            List<Mat> hsvSplit = new ArrayList<Mat>();
            Imgproc.cvtColor(in, hsv, Imgproc.COLOR_BGR2HSV);
            Core.split(hsv, hsvSplit);
                        
            Imgproc.equalizeHist(hsvSplit.get(2), hsvSplit.get(2));
//            CLAHE clahe = Imgproc.createCLAHE(2.0, new Size(2,2));
//            clahe.apply(hsvSplit.get(2), hsvSplit.get(2));
            
            Core.merge(hsvSplit, hsv);
            Imgproc.cvtColor(hsv, out, Imgproc.COLOR_BGR2HSV);
        }else if(in.channels()==1){
            Imgproc.equalizeHist(in, out);
//        	CLAHE clahe = Imgproc.createCLAHE(2.0, new Size(2,2));
//            clahe.apply(in, out);
        }

        return out;    	
    }
 
    private boolean verifyDimension(RotatedRect candidate){
    	    	    	
    	double factorA = Math.max(candidate.size.width, candidate.size.height);
    	double factorB = Math.min(candidate.size.width, candidate.size.height);
    	double candidateRatio = factorA / factorB;
    	
    	anprLogger.trace("plate ratio: " + candidateRatio);
    	anprLogger.trace("plate ratio min: " + detectionConfig.getPlateMinRatio());
    	anprLogger.trace("plate ratio max: " + detectionConfig.getPlateMaxRatio());
    	
    	/*
    	 * Parameter: detection.plate.min.ratio, detection.plate.max.ratio
    	 */
    	if(candidateRatio < detectionConfig.getPlateMinRatio() || candidateRatio > detectionConfig.getPlateMaxRatio()){
    		return false;
    	}else{
    		return true;
    	}    	
    }
 
    private boolean verifyDimension(Size candidateSize){
    	    	
    	double candidateArea = candidateSize.width * candidateSize.height;
    	anprLogger.trace("Candidate Width: " + candidateSize.width);
    	anprLogger.trace("Candidate Height: " + candidateSize.height);
    	anprLogger.trace("Candidate Area: " + candidateArea);
    	
    	/*
    	 * Parameter: detection.plate.min.area, detection.plate.max.area
    	 */
    	if(!(candidateArea > detectionConfig.getPlateMinArea() 
    		&& candidateArea < detectionConfig.getPlateMaxArea())){
    		return false;
    	}    	
    	
    	anprLogger.trace("Candidate ratio: " + (float)candidateSize.width / (float)candidateSize.height);
    	anprLogger.trace("Ratio min: " + detectionConfig.getPlateMinRatio());
    	anprLogger.trace("Ratio max: " + detectionConfig.getPlateMaxRatio());

    	/*
    	 * Parameter: detection.plate.min.ratio, detection.plate.max.ratio
    	 */
    	float r = (float)candidateSize.width / (float)candidateSize.height;
    	
    	if(r < detectionConfig.getPlateMinRatio() || r > detectionConfig.getPlateMaxRatio()){
    		return false;
    	}else{
    		return true;
    	}    	
    }
 
    
    private Size getMaskDimensions(Mat mask, Point center){
    	
    	//can be improved
    	
    	int leftLimit = 0;
    	int rightLimit = 0;
    	int topLimit = (int)center.y;
    	int bottomLimit = (int)center.y;
    	
    	int iterationCount = 0;    	
    	for(int index = (int)center.x, size = 0; index > size; index--){
    		if(mask.get((int)center.y, index) != null){
	    		double px = (mask.get((int)center.y, index))[0];
	    		if(px == 255){
	    			leftLimit = index;
	    			iterationCount = 0;
	    		} else {
	    			iterationCount++;
	    		}
    		}
    		if(iterationCount == 100){
    			break;
    		}
    	}
    	
    	iterationCount = 0;    	
    	for(int index = (int)center.x, size = mask.cols(); index < size; index++){
    		if(mask.get((int)center.y, index) != null){
	    		double px = (mask.get((int)center.y, index))[0];
	    		if(px == 255){
	    			rightLimit = index;
	    			iterationCount = 0;
	    		} else {
	    			iterationCount++;
	    		}
    		}
    		if(iterationCount == 100){
    			break;
    		}
    	}
    	
    	for(int mainIndex = 0; mainIndex < 30; mainIndex+=5){
    		iterationCount = 0;
	    	for(int index = (int)center.y, size = 0; index > size; index--){
	    		if(mask.get(index, (int)center.x+mainIndex) != null){
		    		double px = (mask.get(index, (int)center.x+mainIndex))[0];
		    		if(px == 255 && topLimit > index){
		    			topLimit = index;
		    			iterationCount = 0;
		    		} else {
		    			iterationCount++;
		    		}
	    		}
	    		if(iterationCount == 100){
	    			break;
	    		}
	    	}
	    	
	    	iterationCount = 0;
	    	for(int index = (int)center.y, size = mask.rows(); index < size; index++){
	    		if(mask.get(index, (int)center.x+mainIndex) != null){
		    		double px = (mask.get(index, (int)center.x+mainIndex))[0];
		    		if(px == 255 && bottomLimit < index){
		    			bottomLimit = index;
		    			iterationCount = 0;
		    		} else {
		    			iterationCount++;
		    		}
	    		}
	    		if(iterationCount == 100){
	    			break;
	    		}
	    	}
    	}
    	
    	return new Size((rightLimit - leftLimit), (bottomLimit - topLimit));
    	
//    	Viene commentata la gestione della distorzione causata dall'angolazione con 
//    	la quale viene scattata l'immagine in quanto si  verificato che non e' sempre possibile
//    	calcolarla in maniera corretta ed inoltre risulta impossibile rilevare correttamente 
//    	le targhe quando l'angolazione non rientra in un determinato intervallo
//    	int angleDistorsion = 0;
//    	
//    	for(int index = (int)center.y, size = bottomLimit; index < size; index++){
//    		if(mask.get(index, leftLimit) != null){
//	    		double px = (mask.get(index, rightLimit-10))[0];
//	    		if(px != 255){
//	    			angleDistorsion++;
//	    		} else {
//	    			angleDistorsion = 0;
//	    		}
//    		}
//    	}
//    	
//    	return new Size((rightLimit - leftLimit), 
//						((bottomLimit - topLimit) - angleDistorsion));    	
    	
    }

    private boolean isPlate(Mat plate) throws Exception {
    	    	
		List<Double> peaks = new ArrayList<>();
		
		double maxPeak = 0;
		double minPeak = 100;
		
		for(int colIndex = 0, colsSize = plate.cols(); colIndex < colsSize; colIndex++){
		
			double peak = 0;
			for(int rowIndex = 0, rowsSize = plate.rows(); rowIndex < rowsSize; rowIndex++){	    		    		        			
				double[] matPoint = plate.get(rowIndex, colIndex);
        		peak += matPoint[0];    			 	
    		}   
			
			peak = (peak * 100) / (plate.rows() * 255);
			
			if(peak > maxPeak && peak < 55){
				maxPeak = peak;
			}
			
			if(peak < minPeak){
				minPeak = peak;
			}
			
			peaks.add(peak);
    	}								
		
		/*
		 * Aggiungo una tollerazza sui picchi massimi e minimi del 10%
		 */		
		double mediumPeak = maxPeak - ((maxPeak - minPeak) / 2);
		maxPeak = mediumPeak+10;
		minPeak = mediumPeak-5;
		
		drawPlatePeackGraph(peaks, mediumPeak, minPeak, maxPeak);
		
		int countMaxPeak = 0;
		int countMinPeak = 0;
		
		boolean isMaxPeak = false;
		boolean isMinPeak = false;		
		
		for(double peak : peaks){					
			if(peak > maxPeak && !isMaxPeak){
				countMaxPeak++;
				isMaxPeak = true;
			} else if(!(peak > maxPeak) && isMaxPeak){				
				isMaxPeak = false;				
			}
			
			if(peak < minPeak && !isMinPeak){
				countMinPeak++;
				isMinPeak = true;				
			} else if (!(peak < minPeak) && isMinPeak) {						
				isMinPeak = false;
			}			
		}
		
		anprLogger.trace("Plate - Max Peak Counting: " + countMaxPeak);
		anprLogger.trace("Plate - Min Peak Counting: " + countMinPeak);
		
		/*
		 * TODO Parameter: detection.plate.min.peaks, detection.plate.max.peaks
		 */
		if(countMaxPeak >= detectionConfig.getPlateMinPeaks() 
			&& countMaxPeak <= detectionConfig.getPlateMaxPeaks() 
			&& countMinPeak >= detectionConfig.getPlateMinPeaks() 
			&& countMinPeak <= detectionConfig.getPlateMaxPeaks()){
			return true;
		}
		
		return false;
    }
    
    private void drawPlatePeackGraph(List<Double> peaks, double mediumPeak, double minPeak, double maxPeak) throws Exception {
    
    	Mat graphImage = new Mat(100, peaks.size() ,CvType.CV_8UC3);
    	graphImage.setTo(new Scalar(0,0,0));
    	
    	int rowIndex = 0;
    	for(double peak : peaks){    		
//    		Core.line(graphImage, new Point(rowIndex, 100), new Point(rowIndex, 100-peak), new Scalar(255,255,255));
    		rowIndex++;
    	}
    	
//    	Core.line(graphImage, new Point(0, 100-mediumPeak), new Point(peaks.size(), 100-mediumPeak), new Scalar(0,255,0));
//    	Core.line(graphImage, new Point(0, 100-minPeak), new Point(peaks.size(), 100-minPeak), new Scalar(0,0,255));
//    	Core.line(graphImage, new Point(0, 100-maxPeak), new Point(peaks.size(), 100-maxPeak), new Scalar(0,0,255));
    	
    	StorageUtil.storeDebugImage(graphImage, "PLATE_GRAPH");    
    	graphImage.release();    
    }
    
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public boolean isStoreOutput() {
		return storeOutput;
	}

	public void setStoreOutput(boolean storeOutput) {
		this.storeOutput = storeOutput;
	}

	public String getStoreOutputPath() {
		return storeOutputPath;
	}

	public void setStoreOutputPath(String storeOutputPath) {
		this.storeOutputPath = storeOutputPath;
	}

	public DetectionConfig getDetectionConfig() {
		return detectionConfig;
	}

	public void setDetectionConfig(DetectionConfig detectionConfig) {
		this.detectionConfig = detectionConfig;
	}       
		
}