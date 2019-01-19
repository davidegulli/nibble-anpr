package it.nibble.anpr.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

//@Configuration
//@ImportResource("classpath:application-context.xml")
public class DetectionConfig implements Config {

//	@Value("${detection.contour.max.iterations}")
	public int contourMaxIterations = 2000;
	
//	@Value("${detection.floodfill.lower.difference}")
	public int floodfillLowerDifference = 50;
	
//	@Value("${detection.floodfill.upper.difference}")
	public int floodfillUpperDifference = 50;
	
//	@Value("${detection.result.threwshold.value}")
	public int resultThresholdValue = 70;
	
//	@Value("${detection.result.threwshold.maxvalue}")
	public int resultThresholdMaxvalue = 240;
	
//	@Value("${detection.plate.min.area}")
	public int plateMinArea = 3000;
	
//	@Value("${detection.plate.max.area}")
	public int plateMaxArea = 500000;
	
//	@Value("${detection.plate.min.ratio}")
	public double plateMinRatio = 2.271;
		
//	@Value("${detection.plate.max.ratio}")
	public double plateMaxRatio = 7.186;
	
//	@Value("${detection.plate.min.peaks}")
	public int plateMinPeaks = 5;
	
//	@Value("${detection.plate.max.peaks}")
	public int plateMaxPeaks = 16;

	public int getContourMaxIterations() {
		return contourMaxIterations;
	}

	public void setContourMaxIterations(int contourMaxIterations) {
		this.contourMaxIterations = contourMaxIterations;
	}

	public int getFloodfillLowerDifference() {
		return floodfillLowerDifference;
	}

	public void setFloodfillLowerDifference(int floodfillLowerDifference) {
		this.floodfillLowerDifference = floodfillLowerDifference;
	}

	public int getFloodfillUpperDifference() {
		return floodfillUpperDifference;
	}

	public void setFloodfillUpperDifference(int floodfillUpperDifference) {
		this.floodfillUpperDifference = floodfillUpperDifference;
	}

	public int getResultThresholdValue() {
		return resultThresholdValue;
	}

	public void setResultThresholdValue(int resultThresholdValue) {
		this.resultThresholdValue = resultThresholdValue;
	}

	public int getResultThresholdMaxvalue() {
		return resultThresholdMaxvalue;
	}

	public void setResultThresholdMaxvalue(int resultThresholdMaxvalue) {
		this.resultThresholdMaxvalue = resultThresholdMaxvalue;
	}

	public int getPlateMinArea() {
		return plateMinArea;
	}

	public void setPlateMinArea(int plateMinArea) {
		this.plateMinArea = plateMinArea;
	}

	public int getPlateMaxArea() {
		return plateMaxArea;
	}

	public void setPlateMaxArea(int plateMaxArea) {
		this.plateMaxArea = plateMaxArea;
	}

	public double getPlateMinRatio() {
		return plateMinRatio;
	}

	public void setPlateMinRatio(int plateMinRatio) {
		this.plateMinRatio = plateMinRatio;
	}

	public double getPlateMaxRatio() {
		return plateMaxRatio;
	}

	public void setPlateMaxRatio(int plateMaxRatio) {
		this.plateMaxRatio = plateMaxRatio;
	}

	public int getPlateMinPeaks() {
		return plateMinPeaks;
	}

	public void setPlateMinPeaks(int plateMinPeaks) {
		this.plateMinPeaks = plateMinPeaks;
	}

	public int getPlateMaxPeaks() {
		return plateMaxPeaks;
	}

	public void setPlateMaxPeaks(int plateMaxPeaks) {
		this.plateMaxPeaks = plateMaxPeaks;
	}

	@Override
	public String toString() {
		return "DetectionConfig [contourMaxIterations=" + contourMaxIterations
				+ ", floodfillLowerDifference=" + floodfillLowerDifference
				+ ", floodfillUpperDifference=" + floodfillUpperDifference
				+ ", resultThresholdValue=" + resultThresholdValue
				+ ", resultThresholdMaxvalue=" + resultThresholdMaxvalue
				+ ", plateMinArea=" + plateMinArea + ", plateMaxArea="
				+ plateMaxArea + ", plateMinRatio=" + plateMinRatio
				+ ", plateMaxRatio=" + plateMaxRatio + ", plateMinPeaks="
				+ plateMinPeaks + ", plateMaxPeaks=" + plateMaxPeaks + "]";
	}		
	
}
