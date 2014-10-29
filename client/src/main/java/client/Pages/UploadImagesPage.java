package client.Pages;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.http.ParseException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import client.clientMain.*;


/**
 * Upload Images Page
 * @author Andrew Fox
 *
 */

public class UploadImagesPage extends Page {
	HashMap<String, StringBuilder> data=new HashMap<String, StringBuilder>();	// form data, not including images
	ArrayList<File> pics=new ArrayList<File>();	// pictures to be uploaded in multipart form
	ArrayList<Integer> picNums=new ArrayList<Integer>();
	int numPicsToSend=0;	// number of pictures to upload
	double pageRTFactor=1.8;

	public UploadImagesPage(Page page){
		super(page.url,page.html,page.client,page.pageType,page.pageOpenTime,page.lastPageType,page.lastURL,page.cg);
		client.pageSpecificRT(pageRTFactor);	// change RT threshold depending on page type
		if(HTML4)
			searchData=getFormData("search");
	}

	/**
	 * Determines the next link to go to in the application
	 * @return the URL or HTML of the next link, depending on whether
	 * 		   the previous entry contained a form or not
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws ParseException 
	 */
	public StringBuilder makeDecision() throws ParseException, IOException, InterruptedException{
		StringBuilder nextPage;		// the response after the HTTP request

		if(HTML4)
			data=getFormData("sellitemimages");		// brings in any form data
		else{
			data.put("userID",new StringBuilder(client.getClientInfo().getHTML5Cache().get("userID")));
			data.put("authToken",new StringBuilder(client.getClientInfo().getHTML5Cache().get("authToken")));
			data.put("itemID",new StringBuilder(Long.toString(client.getLastItemID())));
			data.put("useHTML5",new StringBuilder("1"));
		}
		
		if(RunSettings.isRepeatedRun()==false){
			numPicsToSend=getRandomIntFromDist(RunSettings.getNumHeaderImages());		// determines the number of images to send from a distribution

			action=xmlDocument.createElement("action");
			action.setAttribute("id",client.getActionNum());
			Element currentPage=xmlDocument.createElement("currentPage");
			currentPage.setTextContent(Integer.toString(pageType));
			action.appendChild(currentPage);
			Element nextPagee=xmlDocument.createElement("nextPage");
			nextPagee.setTextContent("uploadImages");
			action.appendChild(nextPagee);
			request=xmlDocument.createElement("request");

			if(verbose)System.out.println("Number of Item Images: "+numPicsToSend);

			// adds the pictures to be sent to the pics array
			for (int i=0;i<numPicsToSend;i++){
				int picNum=rand.nextInt(RunSettings.getItemPics().size());
				picNums.add(picNum);
				pics.add(new File(RunSettings.getItemPics().get(picNum)));
			}

			// Think Time
			try{Thread.sleep(getThinkTime());}
			catch(InterruptedException e){
				client.setExit(true);
				return null;
			}

			if(verbose)System.out.println("SELLITEMIMAGES DATA: "+data);
			// submits the form and receives the response

			Element child=xmlDocument.createElement("url");
			child.setTextContent((new StringBuilder(client.getCMARTurl().getAppURL()).append("/sellitemimages")).toString());
			request.appendChild(child);
			for (Entry<String,StringBuilder> e:data.entrySet()){
				child=xmlDocument.createElement("data");
				child.setAttribute("name", e.getKey());
				child.setTextContent(e.getValue().toString());
				request.appendChild(child);
			}
			for (Integer i:picNums){
				child=xmlDocument.createElement("pic");
				child.setTextContent(Integer.toString(i));
				request.appendChild(child);
			}
			child=xmlDocument.createElement("type");
			child.setTextContent("POST");
			request.appendChild(child);

			nextPage=doSubmitPic((new StringBuilder(client.getCMARTurl().getAppURL()).append("/sellitemimages")).toString(),data,pics);


			if(nextPage.indexOf("Sell Item Confirmed")==-1){
				System.err.println("Problem after image upload");
				System.err.println(new StringBuilder(client.getCMARTurl().getAppURL()).append("/sellitemimages").toString());
				System.err.println(data);
				System.err.println(pics);
				System.err.println(nextPage);
			}

			child=xmlDocument.createElement("thinkTime");
			child.setTextContent(Integer.toString(pageThinkTime));
			action.appendChild(child);
			action.appendChild(request);
			client.addXMLAction(action);
		}else{
			findAction();
			if(pageType!=Integer.parseInt(action.getElementsByTagName("currentPage").item(0).getTextContent())){
				client.setExit(true);
				client.setExitDueToRepeatChange(true);
				return null;
			}
			//	StringBuilder nextLink=new StringBuilder(action.getElementsByTagName("nextPage").item(0).getTextContent());
			request=(Element)((Element)action).getElementsByTagName("request").item(0);
			StringBuilder nextURL=new StringBuilder(request.getElementsByTagName("url").item(0).getTextContent());
//			data.clear();
			pics.clear();
//			NodeList dataList=request.getElementsByTagName("data");
//			for(int i=0;i<dataList.getLength();i++){
//				Node n=dataList.item(i);
//				String key=n.getAttributes().item(0).getTextContent();
//				StringBuilder value=new StringBuilder(((Element)n).getTextContent());
//				data.put(key, value);
//			}
//			if(data.containsKey("authToken"))
//				data.put("authToken",client.getClientInfo().getAuthToken());
			if(nextURL.indexOf("authToken=")!=-1){
				int start=nextURL.indexOf("&authToken=")+"&authToken=".length();
				int end=nextURL.indexOf("&",start);
				if(end==-1)
					end=nextURL.length();
				nextURL.replace(start, end, client.getClientInfo().getAuthToken().toString());
			}
			NodeList picsList=request.getElementsByTagName("pic");
			for(int i=0;i<picsList.getLength();i++){
				Node n=picsList.item(i);
				pics.add(new File(RunSettings.getItemPics().get(Integer.parseInt(((Element)n).getTextContent()))));
			}

			// Think Time
			try{Thread.sleep(getThinkTime());}
			catch(InterruptedException e){
				client.setExit(true);
				return null;
			}

			nextPage=doSubmitPic(nextURL.toString(),data,pics);

		}

		return nextPage;
	}

	/**
	 * Determines the think time before going to the next page
	 * Deducts from the think time the amount of time since the 
	 * page opened until this calculation is performed
	 * @return Think time in ms
	 */
	private int getThinkTime(){
		int thinkTime=(int)initialThinkTime;
		if(RunSettings.isRepeatedRun()==false){
			int firstPictureFindTime=10000;
			int lastPictureFindTime=3000;

			if (numPicsToSend==1)
				thinkTime+=firstPictureFindTime;
			else if(numPicsToSend>1)
				thinkTime+=firstPictureFindTime+lastPictureFindTime*(numPicsToSend-1);
			thinkTime=(int)expDist(thinkTime);
		}else{
			thinkTime=Integer.parseInt(((Element)action).getElementsByTagName("thinkTime").item(0).getTextContent());
		}

		if (verbose)System.out.println("User: "+client.getClientInfo().getUsername()+" - Think Time: "+thinkTime+" ms");
		if (RunSettings.isOutputThinkTimes()==true)
			cg.getThinkTimeHist().add(thinkTime);
		pageThinkTime=thinkTime;
		return Math.max((int) ((thinkTime-(new Date().getTime()-pageOpenTime))/RunSettings.getThinkTimeSpeedUpFactor()),0);
	}
}