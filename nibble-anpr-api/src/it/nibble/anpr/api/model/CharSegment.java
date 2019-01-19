package it.nibble.anpr.api.model;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

public class CharSegment implements Comparable<CharSegment>{

	private Mat image;
	private Rect position;
	private int characterIndex;
	private Character character;
	private double nntResultPercentage;
	
	public Mat getImage() {
		return image;
	}
	public void setImage(Mat image) {
		this.image = image;
	}
	public Rect getPosition() {
		return position;
	}
	public void setPosition(Rect position) {
		this.position = position;
	}		
	public Character getCharacter() {
		return character;
	}
	public void setCharacter(Character character) {
		this.character = character;
	}		
	public double getNntResultPercentage() {
		return nntResultPercentage;
	}
	public void setNntResultPercentage(double nntResultPercentage) {
		this.nntResultPercentage = nntResultPercentage;
	}		
	public int getCharacterIndex() {
		return characterIndex;
	}
	public void setCharacterIndex(int characterIndex) {
		this.characterIndex = characterIndex;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((image == null) ? 0 : image.hashCode());
		result = prime * result
				+ ((position == null) ? 0 : position.hashCode());
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
		CharSegment other = (CharSegment) obj;
		if (image == null) {
			if (other.image != null)
				return false;
		} else if (!image.equals(other.image))
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		return true;
	}

	@Override
	public int compareTo(CharSegment charSegment){		
		return (int)(this.position.tl().x - charSegment.getPosition().tl().x);
	}
	
}
