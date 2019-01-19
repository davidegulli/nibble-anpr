package it.nibble.anpr.api.component.ocr;

import it.nibble.anpr.api.model.CharSegment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class PlatePattern {

	public static final String PLATE_PATTERN = "[A-Z]{2}[0-9]{3}[A-Z]{2}"; 	
	
	public List<String> getBestPlate(List<String> plates) throws Exception {
		
		List<String> result = new ArrayList<>();
		
		
		
		return result;
		
	}
	
	/*
	 * Il metodo effettua verifiche sulla correttezza del carattere rispetto la sua posizione
	 * all'interno della targa, e qualora riscontri errori cerca di apportare delle correzioni.	 
	 */
	public static CharSegment applyCharPattern(CharSegment charSegment, Map<String, Double> nntResults, int charsNumber) throws Exception {
				
		if(charsNumber == 7){
			if(Character.isAlphabetic(charSegment.getCharacter()) 
					&& (!(charSegment.getCharacterIndex() < 2) 
					&&  !(charSegment.getCharacterIndex() >= 5 && charSegment.getCharacterIndex() <= 6 ))){
				
//				boolean charModified = false;				
				
				if(applyStandardCorretionToDigitError(charSegment)){
		        	return charSegment;
		        }
				
				double bestResult = 0;
				
				Iterator<String> keysIterator = nntResults.keySet().iterator();
		        while(keysIterator.hasNext()){
		        	String key = keysIterator.next();
		        	double result = nntResults.get(key);
		        	if(bestResult < result && !key.equals(charSegment.getCharacter())
		        			&& Character.isDigit(key.charAt(0)) && Double.compare(result, 0) > 0){
		        		
		        		bestResult = result;
		        		charSegment.setCharacter(key.charAt(0));
		        		charSegment.setNntResultPercentage(result);
//		        		charModified = true;
		        	}
		        }						        		        
			}
			
			if(Character.isDigit(charSegment.getCharacter()) 
					&& !(charSegment.getCharacterIndex() >= 2 && charSegment.getCharacterIndex() <= 4 )){
				
//				boolean charModified = false;
				
		        if(applyStandardCorretionToAlphabeticError(charSegment)){
		        	return charSegment;
		        }

				double bestResult = 0;
		        
				Iterator<String> keysIterator = nntResults.keySet().iterator();
		        while(keysIterator.hasNext()){
		        	String key = keysIterator.next();
		        	double result = nntResults.get(key);
		        	if(bestResult < result && !key.equals(charSegment.getCharacter())
		        			&& Character.isAlphabetic(key.charAt(0)) && Double.compare(result, 0) > 0){
		        		
		        		bestResult = result;
		        		charSegment.setCharacter(key.charAt(0));
		        		charSegment.setNntResultPercentage(result);
//		        		charModified = true;
		        	}
		        }					        
			}
			
		} else {
			//TODO da analizzare e implementare
		}
		
		return charSegment;
		
	}
	
	private static boolean applyStandardCorretionToAlphabeticError(CharSegment charSegment){
		
		boolean result = false;
		
		switch (charSegment.getCharacter()) {
		case '0':
			charSegment.setCharacter('D');
			result = true;
			break;
		case '8':
			charSegment.setCharacter('B');
			result = true;
			break;
		default:
			break;
		}
		
		if(result){
			charSegment.setNntResultPercentage(0.50);
		}
		
		return result;
	}

	private static boolean applyStandardCorretionToDigitError(CharSegment charSegment){
		
		boolean result = false;
		
		switch (charSegment.getCharacter()) {
		case 'D':
			charSegment.setCharacter('0');
			result = true;
			break;
		case 'B':
			charSegment.setCharacter('8');
			result = true;
			break;
		default:
			break;
		}
		
		if(result){
			charSegment.setNntResultPercentage(0.50);
		}
		
		return result;
	}

}
