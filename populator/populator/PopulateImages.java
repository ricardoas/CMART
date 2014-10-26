package populator;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import org.apache.commons.io.*;

import javax.imageio.ImageIO;

/**
 * Inserts item images to the database and creates the files if it is set to true
 * 
 * @author andy
 *
 */
public class PopulateImages implements PopulateFormat{
	private static ArrayList<String> images = new ArrayList<String>(100);

	private CountDownLatch finishedLatch;
	private static Boolean localPopulated = false;
	private static Boolean inMemoryImages = true;
	private static TreeMap<String, byte[]> bufferedImages;
	
	private static boolean writeFiles = true;
	
	private static Distribution imagesDist = null;
	private static RandomSelector<Integer, String> imagesDistRS;
	
	/**
	 * Read in the initial images
	 * 
	 * @param finishedLatch
	 */
	public PopulateImages(CountDownLatch finishedLatch){
		this.finishedLatch = finishedLatch;
		
		init();	
	}
	
	public synchronized void init(){
		if(!localPopulated){
			synchronized(localPopulated){
				if(!localPopulated){
					// If the dist is negative then use the values from the file, not a Java distribution
					if(CreateAll.IMAGES_DIST_TYPE<0)
						try {
							imagesDistRS = new RandomSelector<Integer, String>(new FileInputStream(CreateAll.IMAGES_DIST_FILE), Integer.class);
						} catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					else
						imagesDist = new Distribution(CreateAll.IMAGES_DIST_TYPE, CreateAll.IMAGES_MIN, CreateAll.IMAGES_MAX, CreateAll.IMAGES_DIST_MEAN, CreateAll.IMAGES_DIST_SD, CreateAll.IMAGES_ALPHA, CreateAll.IMAGES_LAMBDA);
					
					
					/*
					 * Read in the images
					 */
					try {
						InputStreamReader ins = new InputStreamReader(new FileInputStream(CreateAll.IMAGES_FILE));
						BufferedReader in = new BufferedReader(ins);
						
						while(in.ready()){
							String text = in.readLine();
							images.add(text);
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if(inMemoryImages){
						bufferedImages = new TreeMap<String, byte[]>();
						try {
							for(int i=0; i<images.size(); i++){
								String imgName = images.get(i);
								
								// Read full size
								FileInputStream fin = new FileInputStream(CreateAll.IN_IMAGE_PATH + imgName+ ".jpg");
								bufferedImages.put(imgName,IOUtils.toByteArray(fin));
								fin.close();
								
								// read thumb
								fin = new FileInputStream(CreateAll.IN_IMAGE_PATH + imgName+ " thumb.jpg");
								bufferedImages.put(imgName+ "thumb",IOUtils.toByteArray(fin));
								fin.close();	
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					localPopulated = true;
				}
			}
		}
	}
	
	/**
	 * Make a random number of images, insert the rows to the db and write the file if needed
	 */
	// TODO: pass item ID through the DB to convert cass->real id
	public boolean makeThing(DBPopulator db, int itemID) {
			
			/*
			 * Go through each item and insert some images
			 */
			int i=itemID;
				int noOfImages = this.getNumberOfImages();
				
				if(noOfImages>0){
					String imageName = this.getImage();
					
					if(writeFiles)
					if(inMemoryImages){
						writeMemoryImage(bufferedImages.get(imageName + "thumb"), i + "_0.jpg");
						writeMemoryImage(bufferedImages.get(imageName), i + "_1.jpg");
					}
					else{
						copyImage(imageName+ " thumb.jpg", i + "_0.jpg");
						copyImage(imageName+ ".jpg", i + "_1.jpg");
					}
					
					db.insertImage(i, 0, i + "_0.jpg", getDescription());
					db.insertImage(i, 1, i + "_1.jpg", getDescription());
				}
				
				for(int imgNo=1; imgNo < noOfImages; imgNo++){
					if(writeFiles)
					if(inMemoryImages){
						this.writeMemoryImage(bufferedImages.get(this.getImage()), i + "_" + (imgNo+1) + ".jpg");
					}
					else{
						this.copyImage(this.getImage()+ ".jpg", i + "_" + (imgNo+1) + ".jpg");
					}
					
					db.insertImage(i, imgNo+1, i + "_" + (imgNo+1) + ".jpg", getDescription());
				}
				
			
			
		
		return false;
	}
	
	/**
	 * Get the number of images for the item
	 * @return
	 */
	public int getNumberOfImages(){
		if(imagesDist!=null)
			return (int) Math.round(imagesDist.getNext());
		else
			return imagesDistRS.getRandomKey();
		

	}
	
	/**
	 * Get an image to write
	 * @return
	 */
	private String getImage(){
		if(images.size()==0) return null;
		
		int index = (int)(Math.random() * images.size());
		if(index > images.size()) index = images.size()-1;
		if(index < 0) index = 0;
		
		return images.get(index);
	}
	
	/**
	 * Get the description of the image
	 * 
	 * @return
	 */
	private String getDescription(){
		StringBuilder strBuf = new StringBuilder(100);
		
		int noOfWords = CreateAll.rand.nextInt(10);
		
		for(int i=0; i<noOfWords; i++){
			strBuf.append(PopulateOldItems.getTitleWordsRS().getRandomKey());
			strBuf.append(" ");
		}
		
		return strBuf.toString();
	}
	
	public void finished(){
		finishedLatch.countDown();
	}
	
	// http://www.mkyong.com/java/how-to-resize-an-image-in-java/
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
	
	/**
	 * Write an image from memory
	 * 
	 * @param inImage
	 * @param outFileName
	 */
	private void writeMemoryImage(byte[] inImage, String outFileName){
		try {
			FileOutputStream fout = new FileOutputStream(CreateAll.OUT_IMAGE_PATH +outFileName);
			IOUtils.write(inImage, fout);
			fout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Write an image from the file
	 * 
	 * @param inFileName
	 * @param outFileName
	 * @return
	 */
	private boolean copyImage(String inFileName, String outFileName){
		FileInputStream fin = null;
		FileOutputStream fout = null;
		
		try {
			fin = new FileInputStream(CreateAll.IN_IMAGE_PATH + inFileName);
			fout = new FileOutputStream(CreateAll.OUT_IMAGE_PATH +outFileName);
			
			byte[] buffer = new byte[4096];
			int bytesRead;

			while ((bytesRead = fin.read(buffer)) != -1)
				fout.write(buffer, 0, bytesRead);
			
			if (fin != null)
				try {
					fin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (fout != null)
				try {
					fout.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		} 
		catch(Exception e){
			e.printStackTrace();
		}
		
		
		return true;
	}
	
	public static boolean remakeTable(DBPopulator db) {
		db.dropAddImages();
		return db.dropAddItemImage();
	}
	
	public static void writeFiles(boolean writeFilesToDisk){
		writeFiles = writeFilesToDisk;
	}
}
