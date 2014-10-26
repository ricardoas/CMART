package com.cmart.PageControllers;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.cmart.Data.Error;
import com.cmart.Data.GlobalErrors;
import com.cmart.Data.GlobalVars;
import com.cmart.util.CheckInputs;
import com.cmart.util.StopWatch;

/**
 * This controller uploads the images for an item that the user is selling
 * 
 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
 * @since 0.1
 * @version 1.0
 * @date 23rd Aug 2012
 * 
 * C-MART Benchmark
 * Copyright (C) 2011-2012 theONE Networking Group, Carnegie Mellon University, Pittsburgh, PA 15213, U.S.A
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

public class SellItemImagesController extends PageController{
	private static final GlobalVars GV = GlobalVars.getInstance();
	
	// Variables passed in the request
	private long userID = -1;
	private String authToken = "";
	private long itemID;
	private ArrayList<FileItem> images = new ArrayList<FileItem>();
	
	// Structures to hold the DB data
	
	// Structures to hold the parsed page data

	String redirectURL = null;

	DiskFileItemFactory factory = null;
	ServletFileUpload upload = null;
	List<FileItem> items = null;
	
	/**
	 * This method checks the page for any input errors that may have come from Client generator error
	 * These would need to be check in real life to stop users attempting to hack and mess with things
	 * 
	 * @param request
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void checkInputs(HttpServletRequest request){
		super.startTimer();
		
		if(request != null){
			// If there is a multiform there are probably pictures
			if(ServletFileUpload.isMultipartContent(request)){		
				// Get the parameters
				try {
					// Create the objects needed to read the parameters
					factory = new DiskFileItemFactory();
					factory.setRepository(GV.LOCAL_TEMP_DIR);
					upload = new ServletFileUpload(factory);
					items = upload.parseRequest(request);	
					Iterator<FileItem> iter = items.iterator();
					TreeMap<String, String> params = new TreeMap<String, String>();
					
					// Go through all the parameters and get the ones that are form fields
					while (iter.hasNext()) {
					    FileItem item = iter.next();
					    
					    // If the item is a parameter, read it
					    if(item.isFormField()){
					        params.put(item.getFieldName(), item.getString());
					    }
					    else {
					    	this.images.add(item);
						}
					}	
					
					/*
					 *  Get the parameters
					 */
					// Get the userID
					if(params.containsKey("userID")){
						try{
							this.userID = Long.parseLong(params.get("userID"));
							
							if(this.userID < 0)
								if(!errors.contains(GlobalErrors.userIDLessThanZero))
									errors.add(GlobalErrors.userIDLessThanZero);
						}
						catch(NumberFormatException e){
							if(!errors.contains(GlobalErrors.userIDNotAnInteger))
								errors.add(GlobalErrors.userIDNotAnInteger);
							
						}
					}
					else{
						if(!errors.contains(GlobalErrors.userIDNotPresent))
							errors.add(GlobalErrors.userIDNotPresent);
					}
					
					// We nned to get the html5 tag as the parent cannot do the normal parsing
					if(params.containsKey("useHTML5")){
						try{
							int u5 = Integer.parseInt(params.get("useHTML5"));
							if(u5 == 1) this.useHTML5 = true;
							else this.useHTML5 = false;
						}catch(Exception e){
							this.useHTML5 = false;
						}
					}
					
					// Get the authToken
					if(params.containsKey("authToken")){
							this.authToken = params.get("authToken");
							
							if(this.authToken.equals(EMPTY))
								if(!errors.contains(GlobalErrors.authTokenEmpty))
									errors.add(GlobalErrors.authTokenEmpty);
					}
					else{
						if(!errors.contains(GlobalErrors.authTokenNotPresent))
							errors.add(GlobalErrors.authTokenNotPresent);
					}
					
					// Get the itemID
					if(params.containsKey("itemID")){
						try{
							this.itemID = Long.parseLong(params.get("itemID"));
							
							if(this.itemID <= 0)
								if(!errors.contains(GlobalErrors.itemIDLessThanZero))
									errors.add(GlobalErrors.itemIDLessThanZero);
						}
						catch(NumberFormatException e){
							if(!errors.contains(GlobalErrors.itemIDNotAnInteger))
								errors.add(GlobalErrors.itemIDNotAnInteger);
						}
					}
					else{
						if(!errors.contains(GlobalErrors.itemIDNotPresent))
							errors.add(GlobalErrors.itemIDNotPresent);
					}
				}
				catch (FileUploadException e1) {
					// TODO Auto-generated catch block
					//System.out.println("SellItemImageController (checkInputs): There was an error in the multi-form");
					e1.printStackTrace();
				}
			}
			// Do normal request processing
			else{
				super.checkInputs(request);
				
				// Get the userID (if exists), we will pass it along to the next pages
				try{
					this.userID = CheckInputs.checkUserID(request);
				}
				catch(Error e){
					// The user must be logged in to upload the images
					if(!errors.contains(e))
						errors.add(e);
				}
				
				// Get the authToken (if exists), we will pass it along to the next pages
				try{
					this.authToken = CheckInputs.checkAuthToken(request);
				}
				catch(Error e){
					if(!errors.contains(e))
						errors.add(e);
				}
				
				// Get the itemID 
				try{
					this.itemID = CheckInputs.checkItemID(request);
					
					if(this.itemID <= 0)
						if(!errors.contains(GlobalErrors.itemIDLessThanZero))
							errors.add(GlobalErrors.itemIDLessThanZero);
				}
				catch(Error e){
					if(!errors.contains(e))
						errors.add(e);
					
					this.itemID = -1;
				}
			}
		}
		
		// Calculate how long that took
		super.stopTimerAddParam();
	}

	/**
	 * This method get the data needed for the HTML4 page from the database
	 * 
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void getHTML4Data() {	
		super.startTimer();
		
		// Calculate how long that took
		super.stopTimerAddDB();
	}

	/**
	 * This method processes all of the data that was read from the database such that it is ready to be printed
	 * on to the page. We try to do as much of the page logic here as possible
	 * 
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void processHTML4() {
		super.startTimer();
		
		// Calculate how long that took
		super.stopTimerAddProcessing();
	}
	
	/**
	 * Gets the HTML5 data from the database
	 * 
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void getHTML5Data(){
		super.startTimer();
		
		// Calculate how long that took
		super.stopTimerAddDB();
	}
	
	/**
	 * Processes the HTML5 data that is needed to create the page
	 * 
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void processHTML5(){
		super.startTimer();
		
		// Calculate how long that took
		super.stopTimerAddProcessing();
	}
	
	public void saveImages(String baseURL){
		//System.out.println("sellitemimagecont: looking for image to upload!");
		//System.out.println("saving images :" + baseURL);
		baseURL = baseURL + "/" + GV.REMOTE_IMAGE_DIR + "/";
		
		// Special case for the thumbnail
		/*if(this.images.size()>1){
			FileItem image = this.images.get(0);
			
			//TODO: compress an image
			String[] ext = image.getName().split("\\.");
			int extIndex = ext.length>0 ? ext.length-1 : 0;
			String filename = this.itemID + "_" + 0 + "." + ext[extIndex];
			String URL = filename;
			
			// Setup the thumbnail file
			File file = new File(GlobalVars.localImageDir, filename);
			file.setReadable(true, false);
			file.setWritable(true, false);
			
			try {
				image.write(file);
				
				GlobalVars.db.insertThumbnail(this.itemID, URL);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				this.errors.add(new Error("SellItemImagesController (saveImages): Could not save thumbnail", e));
				e.printStackTrace();
			}
		}*/
		boolean thumbnail = true;
		
		// Loop through all the images
		for(int i=0; i< this.images.size(); i++){
			FileItem image = this.images.get(i);
			
			//TODO: make number start from one and only count real images
			
			if(image.getSize()>0){
				// Make the file name and path
				String[] ext = image.getName().split("\\.");
				int extIndex = ext.length>0 ? ext.length-1 : 0;
				String filename = this.itemID + "_" + (i+1) + "." + ext[extIndex];
				//String URL = filename;
				
				// Setup the image file
				//System.out.println("setting temp dir as the image");
				File file = new File(GV.LOCAL_TEMP_DIR, filename+"tmp");
				file.setReadable(true, false);
				file.setWritable(true, false);
				
				//System.out.println("URL :" + URL);
				//System.out.println("name :" + filename);
				//System.out.println("local :" + GV.LOCAL_IMAGE_DIR);
				//System.out.println("remote :" + GV.REMOTE_IMAGE_DIR);
				
				
				
				try {
					//System.out.println("doing db insert");
					GV.DB.insertImage(this.itemID, i+1, filename, "");
					
					//System.out.println("saving image");
					image.write(file);
					
					
					//System.out.println("mkaing file in img dir");
					File file2 = new File(GV.LOCAL_IMAGE_DIR, filename);
					
					//System.out.println("doing the image resize");
					BufferedImage originalImage2 = ImageIO.read(file);
					int type2 = originalImage2.getType() == 0? BufferedImage.TYPE_INT_ARGB : originalImage2.getType();
					
					//System.out.println("doing the image resize second step");
					BufferedImage resizeImageHintJpg2 = resizeImageWithHint(originalImage2, type2, 500, 450);
					ImageIO.write(resizeImageHintJpg2, "jpg", file2);
					
					try{
						file.delete();
					}
					catch(Exception e){
						
					}
					
					//System.out.println("sellitemimagecont: inserted an image!");
					
					if(thumbnail){
						//TODO: some image compression
						String thumbName = this.itemID + "_" + 0 + "." + ext[extIndex];
						GV.DB.insertThumbnail(this.itemID, thumbName);
						
						//System.out.println("doing thumbnail");
						
						File thumbFile = new File(GV.LOCAL_IMAGE_DIR, thumbName);
						
						// Get a JPEG writer
						// TODO: other formats??
						/*ImageWriter writer = null;
				        Iterator iter = ImageIO.getImageWritersByFormatName("jpg");
				        if (iter.hasNext()) {
				            writer = (ImageWriter)iter.next();
				        }
						
				        // Set the output file
				        ImageOutputStream ios = ImageIO.createImageOutputStream(thumbFile);
				        writer.setOutput(ios);
				        
				        // Set the compression level
				        JPEGImageWriteParam imgparams = new JPEGImageWriteParam(Locale.getDefault());
				        imgparams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT) ;
				        imgparams.setCompressionQuality(128);
				        
				        // Write the compressed file
				        RenderedImage rendFile = ImageIO.read(file);
				        writer.write(null, new IIOImage(rendFile, null, null), imgparams);
				        
				        
				        
						
						// copy file
						InputStream fin = new FileInputStream(file);
						OutputStream fout = new FileOutputStream(thumbFile);
						byte[] buff = new byte[1024];
						int len;
						
						while((len = fin.read(buff)) > 0)
							  fout.write(buff, 0, len);
						
						fin.close();
						fout.close();
						*/
						
						BufferedImage originalImage = ImageIO.read(file2);
						int type = originalImage.getType() == 0? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
						
						BufferedImage resizeImageHintJpg = resizeImageWithHint(originalImage, type, 100, 100);
						ImageIO.write(resizeImageHintJpg, "jpg", thumbFile);
						
						thumbnail = false;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					this.errors.add(new Error("SellItemImagesController (saveImages): Could not save image", e));
					e.printStackTrace();
				}
			}
		}
		
		if(this.errors.size() == 0 && !this.useHTML5()){
			createRedirectURL();
		}
		
		// Try to save the uploaded files
		/*try {
			
			while(images.hasNext()) {
				FileItem item = (FileItem) images.next();
				System.out.println("doing item 1");
				/*
				 * Handle Form Fields.
				 *
				if(item.isFormField()) {
					System.out.println("File Name = "+item.getFieldName()+", Value = "+item.getString());
				} else {
					//Handle Uploaded files.
					System.out.println("Field Name = "+item.getFieldName()+
						", File Name = "+item.getName()+
						", Content type = "+item.getContentType()+
						", File Size = "+item.getSize());
					/*
					 * Write file to the ultimate location.
					 *
					File file = new File(GlobalVars.imageDir,item.getName());
					item.write(file);
				}
				//System.out.close();
			}
		}catch(Exception ex) {
			System.out.println("Error encountered while uploading file");
		}*/
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
	
	private void createRedirectURL(){
		this.redirectURL = "./confirmsellitem?userID="+this.getUserIDString()+"&authToken="+this.getAuthTokenString()+"&itemID="+this.itemID;
	}
	
	/**
	 * Returns the current userID as a String
	 * 
	 * @return String the userID
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public String getUserIDString(){
		return Long.toString(this.userID);
	}
	
	/**
	 * Returns the authToken sent to the page
	 * 
	 * @return string the authToken
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public String getAuthTokenString(){
		return this.authToken;
	}
	
	public long getItemID(){
		return this.itemID;
	}
	
	/**
	 * Returns the URL to be redirected to if the item's images are inserted
	 * 
	 * @return String the next URL to redirect to
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public String getRedirectURL(){
		return this.redirectURL;
	}
}
