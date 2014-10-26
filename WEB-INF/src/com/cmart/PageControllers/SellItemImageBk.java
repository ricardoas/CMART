package com.cmart.PageControllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

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
 * @version 0.1
 * @since 0.1
 * @date 04/05/2011
 */

public class SellItemImageBk extends PageController{
	private static final GlobalVars GV = GlobalVars.getInstance();
	
	// Variables passed in the request
	private long userID = -1;
	private String authToken = null;
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
			//TODO: this isn't going to work correctly as it's a multiple data form
			// Do multi data processing
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
							
							if(this.itemID < 0)
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
					e1.printStackTrace();
				}
			}
			// Do normal request processing
			else{	
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
		super.stopTimerAddDB();
		
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
			
			//TODO: make number start from one and onyl count real images
			
			if(image.getSize()>0){
				// Make the file name and path
				String[] ext = image.getName().split("\\.");
				int extIndex = ext.length>0 ? ext.length-1 : 0;
				String filename = this.itemID + "_" + (i+1) + "." + ext[extIndex];
				//String URL = filename;
				
				// Setup the image file
				File file = new File(GV.LOCAL_IMAGE_DIR, filename);
				file.setReadable(true, false);
				file.setWritable(true, false);
				
				//System.out.println("URL :" + URL);
				//System.out.println("name :" + filename);
				//System.out.println("local :" + GV.LOCAL_IMAGE_DIR);
				//System.out.println("remote :" + GV.REMOTE_IMAGE_DIR);
				//System.out.println("DO NOT USE ME!!!");
				try {
					GV.DB.insertImage(this.itemID, i+1, filename, "");
					
					image.write(file);
					
					
					
					if(thumbnail){
						//TODO: some image compression
						String thumbName = this.itemID + "_" + 0 + "." + ext[extIndex];
						GV.DB.insertThumbnail(this.itemID, thumbName);
						
						File thumbFile = new File(GV.LOCAL_IMAGE_DIR, thumbName);
						
						// copy file
						InputStream fin = new FileInputStream(file);
						OutputStream fout = new FileOutputStream(thumbFile);
						byte[] buff = new byte[1024];
						int len;
						
						while((len = fin.read(buff)) > 0)
							  fout.write(buff, 0, len);
						
						fin.close();
						fout.close();
						
						
						thumbnail = false;
					}
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
