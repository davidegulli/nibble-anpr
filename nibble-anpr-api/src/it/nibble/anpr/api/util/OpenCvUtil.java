package it.nibble.anpr.api.util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class OpenCvUtil {
	
	private static AnprLogger anprLogger = AnprLogger.getInstane(OpenCvUtil.class);
	
    public static BufferedImage convert(Mat m){
    	
        Mat image_tmp = m;
        MatOfByte matOfByte = new MatOfByte();

        Imgcodecs.imencode(".png", image_tmp, matOfByte); 

        byte[] byteArray = matOfByte.toArray();
        BufferedImage bufImage = null;

        try {

            InputStream in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return bufImage;
    }
    
    public static Mat convert(BufferedImage i){
        BufferedImage image = i;
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(image.getHeight(),image.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, data);
        return mat;
    }

	public static Mat resizeMat(Mat imgSrc, Point tlPoint, Point brPoint) {
		
		try {
			
			Rect interestRect = new Rect(tlPoint, brPoint);
			Point center = new Point((interestRect.x + interestRect.width / 2),
											(interestRect.y + interestRect.height / 2));
			
			Mat imgCrop = new Mat();
            Imgproc.getRectSubPix(imgSrc, interestRect.size(), center, imgCrop);
            
            Mat resultResized = new Mat();
            resultResized.create((int)interestRect.size().height, 
            						(int)interestRect.size().width, imgSrc.type());
            Imgproc.resize(imgCrop, resultResized, resultResized.size(), 0, 0, Imgproc.INTER_CUBIC);
			
            imgCrop.release();
            
            StorageUtil.storeDebugImage(resultResized, "RESULT_RESIZED_FRAME");
            
            return resultResized;
            
		} catch (Exception exc) {
			anprLogger.error("Problem with frame cropping", exc);
		}
		
		return null;
	}
	
	public static Point getCenter(Rect rect){
		
		return new Point((rect.x+(rect.width/2)), (rect.y+(rect.height/2)));
		
	}
}
