package it.nibble.anpr.tools.ocr.neuroph;

import it.nibble.anpr.api.component.ocr.OcrHandler;
import it.nibble.anpr.api.config.OcrConfig;
import it.nibble.anpr.api.util.AnprLogger;
import it.nibble.anpr.api.util.PathUtil;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.imgrec.ImageRecognitionPlugin;
import org.opencv.core.Core;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class NeurophOcrAccurancy {

	private static AnprLogger anprLogger = AnprLogger.getInstane(OcrHandler.class);
			
	public static void main(String[] args) {
		
		try {
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
			ApplicationContext applicationContext = new ClassPathXmlApplicationContext("application-context.xml");
			
			OcrConfig ocrConfig = applicationContext.getBean(OcrConfig.class);
			
			NeuralNetwork nnet = NeuralNetwork.load(new FileInputStream(PathUtil.getHomeDirectoryPath() + File.separator + ocrConfig.getNnetFileLocation()));
			ImageRecognitionPlugin imageRecognitionPlugin = (ImageRecognitionPlugin)nnet.getPlugin(ImageRecognitionPlugin.class); // get the image recognition plugin from neural network

			File testCasesDir = new File("C:\\Developing\\Projects\\ANPR\\Learning-Sets-2015-20x33\\Chars-Training");
			
			int positiveResult = 0;
			int totalTest = testCasesDir.listFiles().length;
						
			for(File imgFile : testCasesDir.listFiles()){
				
				String actualChar = String.valueOf(imgFile.getName().charAt(0));
				String recognizedChar = "";
				
				double tmpValue = 0;
				System.out.println("------------------------------------------------------------------");
				HashMap<String, Double> result = imageRecognitionPlugin.recognizeImage(imgFile);
				Iterator<String> iterator = result.keySet().iterator();				
				while(iterator.hasNext()){
					String charKey = iterator.next();
					double value = result.get(charKey);
					if(tmpValue < value){
						tmpValue = value;
						recognizedChar = charKey;
					}
				}
				
				if(actualChar.equals(recognizedChar)){
					positiveResult++;
				}
				System.out.println("Actual Char: " + actualChar);
				System.out.println("Result Char: " + recognizedChar + " - " + tmpValue);
				System.out.println("------------------------------------------------------------------");
			}
			System.out.println("------------------------------------------------------------------");
			System.out.println("Test Result: " + ((double)((double)100/totalTest)*positiveResult) + "%");
			System.out.println("Positive Result: " + positiveResult);
			System.out.println("Total Test: " + totalTest);
			System.out.println("------------------------------------------------------------------");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.exit(0);

	}
		

}
