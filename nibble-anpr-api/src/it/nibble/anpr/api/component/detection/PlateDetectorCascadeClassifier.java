package it.nibble.anpr.api.component.detection;

import it.nibble.anpr.api.config.DetectionConfig;
import it.nibble.anpr.api.util.AnprLogger;
import it.nibble.anpr.api.util.OpenCvUtil;
import it.nibble.anpr.api.util.PathUtil;
import it.nibble.anpr.api.util.StorageUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class PlateDetectorCascadeClassifier {

	private static AnprLogger anprLogger = AnprLogger.getInstane(PlateDetectorCascadeClassifier.class);
	DetectionConfig detectionConfig = new DetectionConfig();
	
	public List<Mat> detectPlateRegions(Mat src) throws Exception {

		Mat dest = new Mat();
		Mat mask = null;

		try {

			List<Mat> results = new ArrayList<Mat>();
			
			Imgproc.cvtColor(src, dest, Imgproc.COLOR_BGR2GRAY);
			// Debug
			StorageUtil.storeDebugImage(dest, "COLOR_BGR2GRAY");

			CascadeClassifier cascadeClassifier = new CascadeClassifier(
					PathUtil.getHomeDirectoryPath() + File.separator + "nnet"
							+ File.separator + "eu.xml");
			
			MatOfRect candidatePlates = new MatOfRect();
			if (cascadeClassifier != null) {
				cascadeClassifier.detectMultiScale(dest, candidatePlates, 1.1, 2, 0, new Size(52, 11), new Size(1400,220));
			}

//			Imgproc.blur(dest, dest, new Size(5,5));
//	        //Debug
//	        StorageUtil.storeDebugImage(dest, "blur");
	        
	        CLAHE clahe = Imgproc.createCLAHE(2.0, new Size(8,8));
	        clahe.apply(dest, dest);
//	        Imgproc.equalizeHist(dest, dest);
//	        //Debug
	        StorageUtil.storeDebugImage(dest, "equalizeHist");        	        	        	       
	        
//	        Imgproc.Sobel(dest, dest, CvType.CV_8U, 1, 0, 3, 1, 0);	        
//	        //Debug
//	        StorageUtil.storeDebugImage(dest, "sobel");        
	               
	        Imgproc.threshold(dest, dest, 180, 255, Imgproc.THRESH_OTSU+Imgproc.THRESH_BINARY); 
	        
	        //Debug
	        StorageUtil.storeDebugImage(dest, "threshold");
			
			Rect[] candidatePlatesArray = candidatePlates.toArray();
			for (int i = 0; i < candidatePlatesArray.length; i++) {
//				Imgproc.rectangle(src, candidatePlatesArray[i].tl(), candidatePlatesArray[i].br(), new Scalar(110, 220, 0), 3);
//				// Debug
//				StorageUtil.storeDebugImage(src, "CascadeClass");
				
				List<Rect> maskRectsCandidateList = new ArrayList<Rect>();
				List<Mat> maskCandidateList = new ArrayList<Mat>();
				getMasks(src, candidatePlatesArray[i], maskRectsCandidateList, maskCandidateList);
				for (int ii = 0, xx = maskCandidateList.size(); ii < xx; ii++) {
					
					//Debug da rimuovere
//					Rect rect = maskRectsCandidateList.get(ii);					
//					Imgproc.rectangle(src, rect.tl(), rect.br(), new Scalar(110, 220, 0), 3);
//					StorageUtil.storeDebugImage(src, "CascadeClass");
					
					Mat plate = detectPlateRegion(maskCandidateList.get(ii), src, maskRectsCandidateList.get(ii));		
					if(plate != null){
						results.add(plate);
					}
				}
				
			}
									
			return results;

		} catch (Exception exc) {
			throw exc;
		} finally {
			if (mask != null) {
				mask.release();
			}
			dest.release();
		}

	}
	
    private Mat detectPlateRegion(Mat mask, Mat src, Rect rect) throws Exception {
    	
    	//Check new floodfill mask match for a correct patch.
    	//Get all points detected for minimal rotated Rect    	

    	/*
    	 * Scorrendo l'oggeto mask viene creata la lista di Point di interesse 
    	 */
    	List<Point> pointsInterest = new ArrayList<Point>();    	    	
    	for(int colIndex = ((int)rect.tl().x), colsSize = ((int)rect.tl().x)+rect.width; colIndex < colsSize; colIndex++){
    		for(int rowIndex = ((int)rect.tl().y), rowsSize = ((int)rect.tl().y)+rect.height; rowIndex < rowsSize; rowIndex++){    		    	
    			double[] matPoint = mask.get(rowIndex, colIndex);    			
    			if(matPoint != null && matPoint[0] == 255){    				
        			Point point = new Point(colIndex, rowIndex);        			
        			pointsInterest.add(point);
    			} 	
    		}    		
    	}
    	    	    
//    	List<Point> pointsInterest = new ArrayList<Point>();    	    	
//    	for(int colIndex = 0, colsSize = mask.cols(); colIndex < colsSize; colIndex++){
//    		for(int rowIndex = 0, rowsSize = mask.rows(); rowIndex < rowsSize; rowIndex++){    		    	
//    			double[] matPoint = mask.get(rowIndex, colIndex);    			
//    			if(matPoint[0] == 255){    				
//        			Point point = new Point(colIndex, rowIndex);
//        			Imgproc.rectangle(src, point, point, new Scalar(110, 220, 0), 3);
//        			pointsInterest.add(point);
//    			} 	
//    		}    		
//    	}    
    	
    	if(pointsInterest.size() > 0){
    		
    		Point[] pointsInterestArray = new Point[pointsInterest.size()];
    		for(int index = 0, sizei = pointsInterestArray.length; index < sizei; index++){
    			pointsInterestArray[index] = pointsInterest.get(index);
    		}    	    		
    		
			RotatedRect minRect = Imgproc.minAreaRect(new MatOfPoint2f(pointsInterestArray));					
			
//			anprLogger.trace("candidate.size.width:" + minRect.size.width);
//			anprLogger.trace("candidate.size.height:" + minRect.size.height);
			
			if(verifyDimension(minRect)){
				
				double r = minRect.size.width / minRect.size.height;
				double angle = minRect.angle;
//				anprLogger.trace("Plate rect angle:" + angle);
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

	
	private void getMasks(Mat src, Rect candidatePlate, List<Rect> maskRectsList, List<Mat> masksList) throws Exception {
				
		List<Point> listOfSeedPoint = new ArrayList<Point>();
		
		Point centerPoint = OpenCvUtil.getCenter(candidatePlate);
		for(int rowIndex = (int)centerPoint.x, rowsSize = (candidatePlate.width/2)+rowIndex; (rowIndex < rowsSize && listOfSeedPoint.size() != 5); rowIndex+=7){    		    	
			double[] matPoint = src.get((int)centerPoint.y, rowIndex);    			
			if(matPoint != null && matPoint[0] != 0){    				
    			Point point = new Point(rowIndex, (int)centerPoint.y);
    			listOfSeedPoint.add(point);
			} 	
		}    	
				
		for(int rowIndex = (int)centerPoint.x; (rowIndex > candidatePlate.x && listOfSeedPoint.size() != 10); rowIndex-=7){    		    	
			double[] matPoint = src.get((int)centerPoint.y, rowIndex);    			
			if(matPoint != null && matPoint[0] != 0){    				
    			Point point = new Point(rowIndex, (int)centerPoint.y);
    			listOfSeedPoint.add(point);
			} 	
		}    	
		
    	/*
    	 * Parameter: detection.floodfill.lower.difference
    	 */
    	int loDiff = detectionConfig.getFloodfillLowerDifference();
    	
    	/*
    	 * Parameter: detection.floodfill.upper.difference
    	 */
    	int upDiff = detectionConfig.getFloodfillUpperDifference();
    	
    	for(Point seed : listOfSeedPoint){
    		
//    		Imgproc.rectangle(src, seed, seed, new Scalar(110, 220, 0), 3);
//    		StorageUtil.storeDebugImage(src, "SeedPoint");
    		
        	int connectivity = 4;
        	int newMaskVal = 255;
        	Rect ccomp = new Rect();    
        	
        	int flags = connectivity + (newMaskVal << 8 ) + Imgproc.FLOODFILL_FIXED_RANGE + Imgproc.FLOODFILL_MASK_ONLY;
        	Mat mask = new Mat(src.rows() + 2, src.cols() + 2, CvType.CV_8UC1);
        	Imgproc.floodFill(src, mask, seed, new Scalar(255,0,0), ccomp,
    				new Scalar(25, 25, 25), new Scalar(240, 240, 240),flags);
        	if(verifyDimension(ccomp.size())){
        		masksList.add(mask);
        		maskRectsList.add(ccomp);
	        	StorageUtil.storeDebugImage(mask, "MASK");
        	}
    	}				
		
	}

    private Mat histeq(Mat in) {

    	Mat out = new Mat(in.size(), in.type());
        if(in.channels()==3){
            Mat hsv = new Mat();
            List<Mat> hsvSplit = new ArrayList<Mat>();
            Imgproc.cvtColor(in, hsv, Imgproc.COLOR_BGR2HSV);
            Core.split(hsv, hsvSplit);
            Imgproc.equalizeHist(hsvSplit.get(2), hsvSplit.get(2));
            Core.merge(hsvSplit, hsv);
            Imgproc.cvtColor(hsv, out, Imgproc.COLOR_BGR2HSV);
        }else if(in.channels()==1){
            Imgproc.equalizeHist(in, out);
        }

        return out;    	
    }
 
    private boolean verifyDimension(RotatedRect candidate){
    	    	    	
    	double factorA = Math.max(candidate.size.width, candidate.size.height);
    	double factorB = Math.min(candidate.size.width, candidate.size.height);
    	double candidateRatio = factorA / factorB;
    	
//    	anprLogger.trace("plate ratio: " + candidateRatio);
//    	anprLogger.trace("plate ratio min: " + detectionConfig.getPlateMinRatio());
//    	anprLogger.trace("plate ratio max: " + detectionConfig.getPlateMaxRatio());
    	
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
    	
//    	double candidateArea = candidateSize.width * candidateSize.height;
//    	anprLogger.trace("Candidate Width: " + candidateSize.width);
//    	anprLogger.trace("Candidate Height: " + candidateSize.height);
//    	anprLogger.trace("Candidate Area: " + candidateArea);
    	
    	/*
    	 * Parameter: detection.plate.min.area, detection.plate.max.area
    	 */
//    	if(!(candidateArea > detectionConfig.getPlateMinArea() 
//    		&& candidateArea < detectionConfig.getPlateMaxArea())){
//    		return false;
//    	}    	
    	
//    	anprLogger.trace("Candidate ratio: " + (float)candidateSize.width / (float)candidateSize.height);
//    	anprLogger.trace("Ratio min: " + detectionConfig.getPlateMinRatio());
//    	anprLogger.trace("Ratio max: " + detectionConfig.getPlateMaxRatio());

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
		
//		anprLogger.trace("Plate - Max Peak Counting: " + countMaxPeak);
//		anprLogger.trace("Plate - Min Peak Counting: " + countMinPeak);
		
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
    		Imgproc.line(graphImage, new Point(rowIndex, 100), new Point(rowIndex, 100-peak), new Scalar(255,255,255));
    		rowIndex++;
    	}
    	
    	Imgproc.line(graphImage, new Point(0, 100-mediumPeak), new Point(peaks.size(), 100-mediumPeak), new Scalar(0,255,0));
    	Imgproc.line(graphImage, new Point(0, 100-minPeak), new Point(peaks.size(), 100-minPeak), new Scalar(0,0,255));
    	Imgproc.line(graphImage, new Point(0, 100-maxPeak), new Point(peaks.size(), 100-maxPeak), new Scalar(0,0,255));
    	
    	StorageUtil.storeDebugImage(graphImage, "PLATE_GRAPH");    
    	graphImage.release();    
    }
	
}
