package com.cmart.PageControllers;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import com.cmart.Data.Error;
import com.cmart.Data.GlobalVars;
import com.cmart.util.Category;
import com.cmart.util.CheckInputs;
import com.cmart.util.Item;
import com.cmart.util.StopWatch;
import com.cmart.util.VideoItem;


public class BrowseVideoItemController extends PageController {

	private long userID = -1;
	private String authToken = null;
	
	
	//structure to hold the DB data
	private ArrayList<VideoItem> videoItems = null;
	private String[] videoItemURLs = null;
	
	
	
	public void checkInputs(HttpServletRequest request) {
		
		super.startTimer();
		
		if(request != null){
			super.checkInputs(request);
			
			//get user ID
			try{
				this.userID = CheckInputs.checkUserID(request);			
			}
			catch(Error e){
				if(!errors.contains(e)){
					errors.add(e);
				}
			}
			
			// get authToken
			try{
				this.authToken = CheckInputs.checkAuthToken(request);
			}
			catch(Error e){
				if(!errors.contains(e)){
					errors.add(e);
				}
			}
	
		}
		
		super.stopTimerAddDB();	
	}

	
	
	
	public void getHTML4Data() {
		super.startTimer();
		
		/*try{
			videoItems = GlobalVars.DB.GetAllVideos();
		}
		catch (Exception e){
			errors.add(new Error("BrowseVideoController: getHTML4Data: Could not read from database when getting the videos", e));
		}*/
		
		//calculate how long it took
		super.stopTimerAddDB();
	}

	
	public void processHTML4() {
		super.startTimer();
		
		if(this.videoItems != null){
			int videoSize = this.videoItems.size();
			
			this.videoItemURLs = new String[videoSize];
			
			for (int i=0; i<videoSize; i++){
				this.videoItemURLs[i] = videoItems.get(i).getUrl();
			}
		}
		
	}

	@Override
	public void getHTML5Data() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processHTML5() {
		// TODO Auto-generated method stub
		
	}

	
	public String getUserIDString() {
		return Long.toString(this.userID);
	}

	public Boolean hasUserID(){
		return this.userID >0;
	}
	
	public String getAuthTokenString() {
		return this.authToken;
	}

	public Boolean hasAuthToken(){
		return !(this.authToken == null || this.authToken.equals(EMPTY));
	}
	
	
	public ArrayList<VideoItem> getVideoItems() {
		return this.videoItems;
	}

	public String[] getVideoItemURLs() {
		return this.videoItemURLs;
	}

}

