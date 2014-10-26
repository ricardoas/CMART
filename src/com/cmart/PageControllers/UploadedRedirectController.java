  package com.cmart.PageControllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.cmart.util.*;
import com.cmart.Data.Error;
import com.cmart.Data.GlobalErrors;
import com.cmart.Data.GlobalVars;
import com.cmart.util.Category;
import com.cmart.util.CheckInputs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.cmart.util.StopWatch;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;


import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import com.xuggle.mediatool.*;


public class UploadedRedirectController extends PageController{

	
	//variables passed in the request
	private long userID = -1;
	//private int videoID = -1;
	private String authToken = null;

	private String videoName = null;
	private String videoDescription = null;
    private long videoID = -1;
	//Structure to hold the DB data

	
	//Structure to hold the parsed page data
	private String[] videoCategorySelections;
	private String redirectURL=null;

	
	
	private FileItem video;
	DiskFileItemFactory factory = null;
	ServletFileUpload uploadVideo = null;
	List<FileItem> items = null;
	
	private String currentVideoUrl = null;

	
	public void checkInputs(HttpServletRequest request){

		super.startTimer();
		
		if(request != null){
			super.checkInputs(request);
			
			
			// if there is a multiform there are probably videos being uploaded
			if(ServletFileUpload.isMultipartContent(request)) {
				//get the parameter
				try{
					//create the object needed to read the parameter
					factory = new DiskFileItemFactory();
					File tmpDir = new File(GlobalVars.localUploadDir);
					
					System.out.println("in uploadredirectedcontroller.checkInput() Dir exist: "+ tmpDir.exists());
					System.out.println("in uploadredirectedcontroller.checkInput() Write Permission: "+tmpDir.canWrite());
					factory.setRepository(tmpDir);
					uploadVideo = new ServletFileUpload(factory);
					items = uploadVideo.parseRequest(request);
					
					Iterator<FileItem> iter = items.iterator();
					TreeMap<String, String> params = new TreeMap<String, String>();
					
					while(iter.hasNext()){
						FileItem item = iter.next();
						if(item.isFormField()) {
							params.put(item.getFieldName(), item.getString());
						}
						else if(item.getName()==""){ 
							System.out.println("no file, item name:"+item.getName());
							
							if(!errors.contains(GlobalErrors.noVideoUploaded))
								errors.add(GlobalErrors.noVideoUploaded);
						}
						else{
							System.out.println("in uploadredirectedcontroller.checkInput() video appears, original name: "+item.getName());
							System.out.println("in uploadredirectedcontroller.checkInput() video appears, video size: "+item.getSize());
							System.out.println("in uploadredirectedcontroller.checkInput() video appears, field name: "+item.getFieldName());
							
							this.video = item;
							}
						}
					
					if(params.containsKey("userID")) {
						try{
							this.userID = Long.parseLong(params.get("userID"));
							System.out.println("in UploadRedirected Controller, checkinput,userID="+this.userID);
							
						if(this.userID < 0)
								if(!errors.contains(GlobalErrors.userIDLessThanZero))
									errors.add(GlobalErrors.userIDLessThanZero);
							 
							 
						}
						catch (NumberFormatException e){
							if(!errors.contains(GlobalErrors.userIDNotAnInteger))
								errors.add(GlobalErrors.userIDNotAnInteger);
						}					
					}
					
					
					// Get the authToken
					if(params.containsKey("authToken")){
							this.authToken = params.get("authToken");
							System.out.println("in UploadRedirected Controller, checkinput,authToken+"+this.authToken);
						
							if(this.authToken.equals(EMPTY))
								if(!errors.contains(GlobalErrors.authTokenEmpty))
									errors.add(GlobalErrors.authTokenEmpty);
					}
		
					
					// Get the itemID
					if(params.containsKey("videoname")){
					
					    this.videoName = params.get("videoname");
					}
					
					if(params.containsKey("videodescription")){
						this.videoDescription = params.get("videodescription");
					}
						
				}
				catch (FileUploadException el){
					el.printStackTrace();
				}
	
			}
			else {
				
				errors.add(GlobalErrors.noVideoUploaded);
				// Get the userID (if exists), we will pass it along to the next pages
				
				/*
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
				*/
				
			}

			
		}
		
		// Calculate how long that took
		super.stopTimerAddParam();
	}


	
	/*
	public boolean insertVideo(int userID) {
		
		boolean videoInserted = false;

		System.out.println("in uploadredirectcontroller, insertVideo");
		
		try{
			int maxID = GlobalVars.DB.getMaxVideoID();
			String localVideoName = "video_"+(maxID+1)+".flv";
			String localVideoPath = GlobalVars.localUploadDir+"/"+localVideoName;
			
			videoInserted = GlobalVars.DB.insertVideo(localVideoName, videoName, videoDescription,userID);
			
			System.out.println("for debug, inserted video: " +videoInserted);
			
			
			if (videoInserted) {
				File videoFile = new File(GlobalVars.localUploadDir,localVideoName);
				videoFile.setReadable(true,false);
				videoFile.setWritable(true,false);
				
				System.out.println("Message for debug");
				System.out.println("local:  "+GlobalVars.localUploadDir);
				//System.out.println("remote:  "+ GlobalVars.remoteVideoDir);
				
				video.write(videoFile);
				
				currentVideoUrl = localVideoName;
			}
		}
		catch (Exception e){
			this.errors.add(new Error("VideoUploadController: could not upload file",e));
		}
	
		return videoInserted;
		
	}
	*/
	
	
public long insertVideoCon(long userID) {
	
	
	    System.out.println("in uploadredirectcontroller, insertVideo, userID= "+userID);
	    
	    

		
		// only attempt to insert if there userID and authToken match
		if(!GlobalVars.DB.checkAuthToken(this.userID, this.authToken)){
			System.out.println(":( :( :(error!!,userID dont match authToken");
			return videoID;
		}

		
		try{
			
			//videoID = GlobalVars.DB.insertVideo(videoName, videoDescription,userID);
			//int maxID = GlobalVars.DB.getMaxVideoID();
			
			if (videoID>0) {
				String localVideoName = "video_"+(videoID)+".flv";
				String localImageName = "image_"+(videoID);
				String localVideoPath = GlobalVars.localUploadDir+"/"+localVideoName;
			
				System.out.println("in UploadRedirectController.insertVideo() videoID= "+videoID);
				System.out.println("in UploadRedirectController.insertVideo() localVideoName= "+localVideoName);
				System.out.println("in UploadRedirectController.insertVideo() localVideoPath= "+localVideoPath);
			
			
			
			//if successfully insert into database

				File videoFile = new File(GlobalVars.localUploadDir,localVideoName);
				videoFile.setReadable(true,false);
				videoFile.setWritable(true,false);
				
				System.out.println("Message for debug");
				System.out.println("local:  "+GlobalVars.localUploadDir);
				//System.out.println("remote:  "+ GlobalVars.remoteVideoDir);
				
				video.write(videoFile);
				
				currentVideoUrl = localVideoName;
				System.out.println("in UploadRedirectController.insertVideo(),before videothumbnails localVideoName+"+localVideoName);
				//VideoThumbnails thumbnail = new VideoThumbnails(localVideoName,localImageName);
				VideoThumbnails.makeVideoThumbnail(localVideoName, localImageName);
				System.out.println("in UploadRedirectController.insertVideo(),after videothumbnails");
			}
		}
		catch (Exception e){
			System.err.println("in UploadRedirectController.insertVideoCon(), exception: "+e);
		}

		return videoID;
		
	}
		
		
		
	

	public long getVideoID(){
		return videoID;
	}
	
	public String getCurrentVideoURL(){
		return currentVideoUrl;
	}
	
	public long getUserID(){
		return this.userID;
	}
	
	public String getUserIDString(){
		return Long.toString(this.userID);
	}

	


	public String getAuthTokenString() {
		return this.authToken;
	}
	
	
	
	public String getVideoName(){
		return this.videoName;
	}



	public String getVideoDescription() {
		return this.videoDescription;
	}


	public String getRedirectURL() {
		return this.redirectURL;
	}




	/*
	public int getVideoCategoryID(){
		return this.videoCategoryID;
	}
	
	 */
	

	@Override
	public void getHTML4Data() {
		super.startTimer();
		/*
		// Get the categories the item can belong to
		try{
			categories = GlobalVars.db.getAllCategories();
		}
		catch(Exception e){
			errors.add(new Error("SellItemController: getHTML4Data: Could not get the categories", e));
		}
		*/
		// Calculate how long that took
		super.stopTimerAddDB();
		
	}


	@Override
	public void processHTML4() {
		super.startTimer();
		/*
		// Make the category list items
		if(this.categories != null){
			categorySelections = new String[this.categories.size()];
			int length = this.categories.size();
			
			for(int i=0; i<length; i++)
				categorySelections[i] = "<option value=\"" + this.categories.get(i).getCategoryID() +"\""+ (this.categoryID==this.categories.get(i).getCategoryID()?" SELECTED":"") + ">" + this.categories.get(i).getName() + "</option>";
		}
		*/
		// Calculate how long that took
		super.stopTimerAddProcessing();
		
	}


	@Override
	public void getHTML5Data() {
	super.startTimer();
		/*
		// Get the categories the item can belong to
		try{
			categories = GlobalVars.db.getAllCategories();
		}
		catch(Exception e){
			errors.add(new Error("SellItemController: getHTML4Data: Could not get the categories", e));
		}
		*/
		// Calculate how long that took
		super.stopTimerAddDB();
		
	}


	@Override
	public void processHTML5() {
		super.startTimer();
		/*
		// Make the category list items
		if(this.categories != null){
			categorySelections = new String[this.categories.size()];
			int length = this.categories.size();
			
			for(int i=0; i<length; i++)
				categorySelections[i] = "<option value=\"" + this.categories.get(i).getCategoryID() +"\""+ (this.categoryID==this.categories.get(i).getCategoryID()?" SELECTED":"") + ">" + this.categories.get(i).getName() + "</option>";
		}
		*/
		// Calculate how long that took
		super.stopTimerAddProcessing();
	}
		




}
