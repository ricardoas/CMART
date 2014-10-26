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
 * @version 0.1
 * @since 0.1
 * @date 04/05/2011
 */

public class UploadVideoController extends PageController{
	private static final GlobalVars GV = GlobalVars.getInstance();
	
	// Variables passed in the request
	private Boolean useHTML5 = Boolean.FALSE;
	private long userID = -1;
	private String authToken = null;
	private ArrayList<FileItem> images = new ArrayList<FileItem>();
	
	// Structures to hold the DB data
	
	// Structures to hold the parsed page data

	String redirectURL = null;

	DiskFileItemFactory factory = null;
	ServletFileUpload upload = null;
	List<FileItem> items = null;
	String videoname = null;
	
	/**
	 * This method checks the page for any input errors that may have come from Client generator error
	 * These would need to be check in real life to stop users attempting to hack and mess with things
	 * 
	 * @param request
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
							this.userID = Integer.parseInt(params.get("userID"));
							
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
					
					// Get the userID
					System.err.println(params.containsKey("useHTML5"));
					System.err.println(params.get("useHTML5"));
					if(params.containsKey("useHTML5")){
						try{
							this.useHTML5 = (1==Integer.parseInt(params.get("useHTML5")));
						}catch(Exception e){
							
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
				}
				catch (FileUploadException e1) {
					// TODO Auto-generated catch block
					System.out.println("SellItemImageController (checkInputs): There was an error in the multi-form");
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
			}
		}
		
		// Calculate how long that took
		super.stopTimerAddParam();
	}

	/**
	 * This method get the data needed for the HTML4 page from the database
	 * 
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
	 */
	public void processHTML4() {
		super.startTimer();
		
		// Calculate how long that took
		super.stopTimerAddProcessing();
	}
	
	/**
	 * Gets the HTML5 data from the database
	 * 
	 */
	public void getHTML5Data(){
		super.stopTimerAddDB();
		
		// Calculate how long that took
		super.stopTimerAddDB();
	}
	
	/**
	 * Processes the HTML5 data that is needed to create the page
	 * 
	 */
	public void processHTML5(){
		super.startTimer();
		
		// Calculate how long that took
		super.stopTimerAddProcessing();
	}
	
	public void saveImages(String baseURL){
		
		baseURL = baseURL + "/" + GV.REMOTE_IMAGE_DIR + "/";
		
		boolean thumbnail = true;
		
		// Loop through all the images
		for(int i=0; i< this.images.size(); i++){
			FileItem image = this.images.get(i);
			
			//TODO: make number start from one and only count real images
			
			if(image.getSize()>0){
				// Make the file name and path
				String filename = image.getName();
				//String URL = filename;
				this.videoname=filename;
				// Setup the image file
				//System.out.println("setting temp dir as the image");
				File file = new File("/usr/share/tomcat7/webapps/videos", filename);
				file.setReadable(true, false);
				file.setWritable(true, false);
				
				//System.out.println("URL :" + URL);
				//System.out.println("name :" + filename);
				//System.out.println("local :" + GV.LOCAL_IMAGE_DIR);
				//System.out.println("remote :" + GV.REMOTE_IMAGE_DIR);
				
				try {
					//System.out.println("doing db insert");
					GV.DB.insertVideo(filename);
					
					//System.out.println("saving image");
					image.write(file);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					this.errors.add(new Error("SellItemImagesController (saveImages): Could not save image", e));
					e.printStackTrace();
				}
			}
		}
		
		if(this.errors.size() == 0){
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
	
	private void createRedirectURL(){
		this.redirectURL = "./viewvideo.html?userID="+this.getUserIDString()+"&authToken="+this.getAuthTokenString()+"&video="+this.videoname;
	}
	
	/**
	 * Returns the current userID as a String
	 * 
	 * @return String the userID
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String getUserIDString(){
		return Long.toString(this.userID);
	}
	
	/**
	 * Returns the authToken sent to the page
	 * 
	 * @return string the authToken
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String getAuthTokenString(){
		return this.authToken;
	}
	/**
	 * Returns the URL to be redirected to if the item's images are inserted
	 * 
	 * @return String the next URL to redirect to
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String getRedirectURL(){
		return this.redirectURL;
	}
	
	public Boolean userHTML5Images(){
		return this.useHTML5;
	}
}
