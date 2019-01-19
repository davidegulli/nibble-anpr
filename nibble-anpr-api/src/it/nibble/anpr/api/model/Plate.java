package it.nibble.anpr.api.model;

import java.util.List;

import org.opencv.core.Mat;

public class Plate implements Comparable<Plate> {

	private Mat img;
	private List<Mat> imgChar;
	
	private List<CharSegment> charSegments;
	private double nntResultAverage;
	
	//usato per testing e training
	private String actualPlate;

	public Mat getImg() {
		return img;
	}

	public void setImg(Mat img) {
		this.img = img;
	}

	public List<Mat> getImgChar() {
		return imgChar;
	}

	public void setImgChar(List<Mat> imgChar) {
		this.imgChar = imgChar;
	}
	
	public List<CharSegment> getCharSegments() {
		return charSegments;
	}

	public void setCharSegments(List<CharSegment> charSegments) {
		this.charSegments = charSegments;
	}		
	
	public String getActualPlate() {
		return actualPlate;
	}

	public void setActualPlate(String actualPlate) {
		this.actualPlate = actualPlate;
	}	
	
	public double getNntResultAverage() {
		return nntResultAverage;
	}

	public void setNntResultAverage(double nntResultAverage) {
		this.nntResultAverage = nntResultAverage;
	}

	public String getPlateNumber(){
		if(charSegments != null && charSegments.size() > 0){
			StringBuilder plateNumber = new StringBuilder();
			for(CharSegment charSegment : charSegments){
				if(charSegment != null && charSegment.getCharacter() != null){
					plateNumber.append(charSegment.getCharacter());
				}
			}
			
			return plateNumber.toString();
		}
		
		return null;
	}	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((charSegments == null) ? 0 : charSegments.hashCode());
		result = prime * result + ((img == null) ? 0 : img.hashCode());
		result = prime * result + ((imgChar == null) ? 0 : imgChar.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Plate other = (Plate) obj;
		if (charSegments == null) {
			if (other.charSegments != null)
				return false;
		} else if (!charSegments.equals(other.charSegments))
			return false;
		if (img == null) {
			if (other.img != null)
				return false;
		} else if (!img.equals(other.img))
			return false;
		if (imgChar == null) {
			if (other.imgChar != null)
				return false;
		} else if (!imgChar.equals(other.imgChar))
			return false;
		return true;
	}

	@Override
	public int compareTo(Plate plate){		
		return Double.compare(nntResultAverage, plate.getNntResultAverage());
	}
}
