package it.nibble.anpr.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

//@Configuration
//@ImportResource("classpath:application-context.xml")
public class OcrConfig implements Config {

	@Value("${ocr.nnet.file.location}")
	private String nnetFileLocation = "nnet\\NNTANPR_11_3224.nnet";
	
	@Value("${ocr.min.segment.accepted}")
	private int minSegmentAccepted = 7;
	
	@Value("${ocr.image.char.dimensions.width}")
	private int imageCharDimensionsWidth = 20;
			
	@Value("${ocr.image.char.dimensions.height}")
	private int imageCharDimensionsHeight = 33;
	
	@Value("${ocr.char.accepted.dimensions.height.min}")
	private int charAcceptedDimensionsHeightMin = 15;
	
	@Value("${ocr.char.accepted.dimensions.height.max}")
	private int charAcceptedDimensionsHeightMax = 30;
	
	@Value("${ocr.char.accepted.dimensions.aspect.min}")
	private double charAcceptedDimensionsAspectMin = 0.2;
	
	@Value("${ocr.char.accepted.dimensions.aspect.max}")
	private double charAcceptedDimensionsAspectMax = 0.91;
	
	@Value("${ocr.char.accepted.dimensions.nonzero.pixel.limit}")
	private double charAcceptedDimensionsNonZeroPixelLimit = 0.8;

	@Value("${ocr.char.image.threwshold.value}")
	private int charImageThrewsholdValue = 60;
	
	@Value("${ocr.char.image.threwshold.maxvalue}")
	private int charImageThrewsholdMaxValue = 255;

	public String getNnetFileLocation() {
		return nnetFileLocation;
	}

	public void setNnetFileLocation(String nnetFileLocation) {
		this.nnetFileLocation = nnetFileLocation;
	}

	public int getMinSegmentAccepted() {
		return minSegmentAccepted;
	}

	public void setMinSegmentAccepted(int minSegmentAccepted) {
		this.minSegmentAccepted = minSegmentAccepted;
	}

	public int getImageCharDimensionsWidth() {
		return imageCharDimensionsWidth;
	}

	public void setImageCharDimensionsWidth(int imageCharDimensionsWidth) {
		this.imageCharDimensionsWidth = imageCharDimensionsWidth;
	}

	public int getImageCharDimensionsHeight() {
		return imageCharDimensionsHeight;
	}

	public void setImageCharDimensionsHeight(int imageCharDimensionsHeight) {
		this.imageCharDimensionsHeight = imageCharDimensionsHeight;
	}

	public int getCharAcceptedDimensionsHeightMin() {
		return charAcceptedDimensionsHeightMin;
	}

	public void setCharAcceptedDimensionsHeightMin(
			int charAcceptedDimensionsHeightMin) {
		this.charAcceptedDimensionsHeightMin = charAcceptedDimensionsHeightMin;
	}

	public int getCharAcceptedDimensionsHeightMax() {
		return charAcceptedDimensionsHeightMax;
	}

	public void setCharAcceptedDimensionsHeightMax(
			int charAcceptedDimensionsHeightMax) {
		this.charAcceptedDimensionsHeightMax = charAcceptedDimensionsHeightMax;
	}

	public double getCharAcceptedDimensionsAspectMin() {
		return charAcceptedDimensionsAspectMin;
	}

	public void setCharAcceptedDimensionsAspectMin(
			double charAcceptedDimensionsAspectMin) {
		this.charAcceptedDimensionsAspectMin = charAcceptedDimensionsAspectMin;
	}

	public double getCharAcceptedDimensionsAspectMax() {
		return charAcceptedDimensionsAspectMax;
	}

	public void setCharAcceptedDimensionsAspectMax(
			int charAcceptedDimensionsAspectMax) {
		this.charAcceptedDimensionsAspectMax = charAcceptedDimensionsAspectMax;
	}

	public double getCharAcceptedDimensionsNonZeroPixelLimit() {
		return charAcceptedDimensionsNonZeroPixelLimit;
	}

	public void setCharAcceptedDimensionsNonZeroPixelLimit(
			int charAcceptedDimensionsNonZeroPixelLimit) {
		this.charAcceptedDimensionsNonZeroPixelLimit = charAcceptedDimensionsNonZeroPixelLimit;
	}

	public int getCharImageThrewsholdValue() {
		return charImageThrewsholdValue;
	}

	public void setCharImageThrewsholdValue(int charImageThrewsholdValue) {
		this.charImageThrewsholdValue = charImageThrewsholdValue;
	}

	public int getCharImageThrewsholdMaxValue() {
		return charImageThrewsholdMaxValue;
	}

	public void setCharImageThrewsholdMaxValue(int charImageThrewsholdMaxValue) {
		this.charImageThrewsholdMaxValue = charImageThrewsholdMaxValue;
	}

	@Override
	public String toString() {
		return "OcrConfig [nnetFileLocation=" + nnetFileLocation
				+ ", minSegmentAccepted=" + minSegmentAccepted
				+ ", imageCharDimensionsWidth=" + imageCharDimensionsWidth
				+ ", imageCharDimensionsHe=" + imageCharDimensionsHeight
				+ ", charAcceptedDimensionsHeightMin="
				+ charAcceptedDimensionsHeightMin
				+ ", charAcceptedDimensionsHeightMax="
				+ charAcceptedDimensionsHeightMax
				+ ", charAcceptedDimensionsAspectMin="
				+ charAcceptedDimensionsAspectMin
				+ ", charAcceptedDimensionsAspectMax="
				+ charAcceptedDimensionsAspectMax
				+ ", charAcceptedDimensionsNonZeroPixelLimit="
				+ charAcceptedDimensionsNonZeroPixelLimit
				+ ", charImageThrewsholdValue=" + charImageThrewsholdValue
				+ ", charImageThrewsholdMaxValue="
				+ charImageThrewsholdMaxValue + "]";
	}
		
}
