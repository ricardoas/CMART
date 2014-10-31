package populator;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;

/**
 * Gets the images from the database and creates the files. This can be used if the images were not originally
 * created but are now needed
 * 
 * @author andy
 *
 */
public class ImagesFromDB implements PopulateFormat{
	private static ArrayList<String> images = new ArrayList<String>(100);

	private CountDownLatch finishedLatch;
	private static Boolean localPopulated = false;
	private static Boolean inMemoryImages = true;
	private static TreeMap<String, byte[]> bufferedImages;
	
	private static boolean writeFiles = true;
	
	public ImagesFromDB(CountDownLatch finishedLatch){
		this.finishedLatch = finishedLatch;
		
		if(!localPopulated){
			synchronized(localPopulated){
				if(!localPopulated){
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
	
	public boolean makeThing(DBPopulator db, int itemID) {
			
			
			//File test = new File("sdfs");
			
			/*
			 * Go through each item and insert some images
			 */
				long i=itemID;
				int noOfImages = this.getNumberOfImages(db, itemID);
				
				// Make the thumbnail if needed
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
				}
				
				// Make the other files
				for(int imgNo=1; imgNo < noOfImages; imgNo++){
					if(writeFiles)
					if(inMemoryImages){
						this.writeMemoryImage(bufferedImages.get(this.getImage()), i + "_" + (imgNo+1) + ".jpg");
					}
					else{
						this.copyImage(this.getImage()+ ".jpg", i + "_" + (imgNo+1) + ".jpg");
					}
				}
		
		return false;
	}
	
	private int getNumberOfImages(DBPopulator db, long itemID){
		return db.getNoOfImages(itemID);
	}
	
	/**
	 * Return random image
	 * 
	 * @return
	 */
	private String getImage(){
		if(images.size()==0) return null;
		
		int index = (int)(Math.random() * images.size());
		if(index > images.size()) index = images.size()-1;
		if(index < 0) index = 0;
		
		return images.get(index);
	}
	
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
		System.out.println("finished");
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
	 * Write a file from the copy in the memory
	 * @param inImage
	 * @param outFileName
	 */
	private void writeMemoryImage(byte[] inImage, String outFileName){
		try {
			FileOutputStream fout = new FileOutputStream(CreateAll.OUT_IMAGE_PATH +outFileName);
			IOUtils.write(inImage, fout);
			fout.close();
			//ImageIO.write(img, "jpg", new File(CreateAll.OUT_IMAGE_PATH + outFileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Write a file that is copied from the original file
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
		return true;
	}
	
	public static void writeFiles(boolean writeFilesToDisk){
		writeFiles = writeFilesToDisk;
	}
}
