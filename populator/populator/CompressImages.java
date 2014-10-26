package populator;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.imageio.ImageIO;

/**
 * Create a set of compressed images. The compressed images are need to populate the data
 * as the website will automatically compress the images that user's upload
 */
public class CompressImages {
	private static ArrayList<String> images = new ArrayList<String>(100);
	
	/**
	 * Compress the images that are found in the image file
	 */
	public CompressImages(){
		
		/*
		 * Read in the images
		 */
		try {
			FileInputStream fin = new FileInputStream(CreateAll.IMAGES_FILE);
			DataInputStream din = new DataInputStream(fin);
			BufferedReader in = new BufferedReader(new InputStreamReader(din));
			
			while(in.ready()){
				String text = in.readLine();
				images.add(text);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*
		 * Compress each image
		 */
		for(int i=0; i<images.size(); i++){
			String imageName = images.get(i);
			System.out.println("Compressing : " + imageName);
			try {
			BufferedImage originalImage = ImageIO.read(new File(CreateAll.IN_IMAGE_PATH + imageName+".jpg"));
			int type = originalImage.getType() == 0? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
		
			BufferedImage resizeImageHintJpg = resizeImageWithHint(originalImage, type, 75, 75);
			ImageIO.write(resizeImageHintJpg, "jpg", new File(CreateAll.OUT_IMAGE_PATH  + imageName + " thumb.jpg"));
			
			resizeImageHintJpg = resizeImageWithHint(originalImage, type, 550, 430);
			ImageIO.write(resizeImageHintJpg, "jpg", new File(CreateAll.OUT_IMAGE_PATH  + imageName +".jpg"));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Could not compress: "+ CreateAll.IN_IMAGE_PATH +imageName);
		}
		}
	}

	// http://www.mkyong.com/java/how-to-resize-an-image-in-java/
	/**
	 * resize the images
	 * 
	 * @param originalImage
	 * @param type
	 * @param width
	 * @param height
	 * @return
	 */
		private static BufferedImage resizeImageWithHint(BufferedImage originalImage, int type, int width, int height) {

			BufferedImage resizedImage = new BufferedImage(width, width, type);
			Graphics2D g = resizedImage.createGraphics();
			g.drawImage(originalImage, 0, 0, width, width, null);
			g.dispose();
			g.setComposite(AlphaComposite.Src);

			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			return resizedImage;
		}
}
