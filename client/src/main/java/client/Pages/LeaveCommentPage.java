package client.Pages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import org.w3c.dom.Element;

import client.clientMain.RunSettings;


/**
 * Leave Comment Page
 * @author Andrew Fox
 *
 */

public class LeaveCommentPage extends Page{
	HashMap<String, StringBuilder> data = new HashMap<String, StringBuilder>();	// data to be sent to the login page
	StringBuilder comment=new StringBuilder();
	double pageRTFactor=1.0;

	public LeaveCommentPage(Page page){
		super(page.url,page.html,page.client,page.pageType,page.pageOpenTime,page.lastPageType,page.lastURL);
		client.pageSpecificRT(pageRTFactor);	// change RT threshold depending on page type
	}

	/**
	 * Determines the next link to go to in the application
	 * @return the URL or HTML of the next link, depending on whether
	 * 		   the previous entry contained a form or not
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws UnsupportedEncodingException 
	 */
	public StringBuilder makeDecision() throws UnsupportedEncodingException, IOException, InterruptedException{
		StringBuilder nextLink=new StringBuilder(client.getCMARTurl().getAppURL());	// link to send the login data to
		StringBuilder nextURL=new StringBuilder();	// the response page returned after the login attempt

		data=getFormData("commentitem");
		
		if(RunSettings.isRepeatedRun()==false){
			searchData=getFormData("search");
			

			action=xmlDocument.createElement("action");
			action.setAttribute("id",client.getActionNum());
			Element currentPage=xmlDocument.createElement("currentPage");
			currentPage.setTextContent(Integer.toString(pageType));
			action.appendChild(currentPage);
			Element nextPage=xmlDocument.createElement("nextPage");
			nextPage.setTextContent("leaveComment");
			action.appendChild(nextPage);
			request=xmlDocument.createElement("request");

			int numWordsInQuestion;
			do{
				numWordsInQuestion=(int)Math.round(rand.nextGaussian()*5+10);
			}while(numWordsInQuestion<=0);
			for (int i=0;i<numWordsInQuestion;i++){
				comment.append(getRandomStringBuilderFromDist(RunSettings.getTitleWords()));
				if (i!=numWordsInQuestion-1)
					comment.append(" ");
			}

			data.put("comment", typingError(comment));
			data.put("rating", new StringBuilder(Integer.toString(rand.nextInt(6))));


			// Think Time
			try{Thread.sleep(getThinkTime());}
			catch(InterruptedException e){
				client.setExit(true);
				return null;
			}

			Element child=xmlDocument.createElement("url");
			child.setTextContent(new StringBuilder(nextLink).append("/commentitem").toString());
			request.appendChild(child);
			for (Entry<String,StringBuilder> e:data.entrySet()){
				child=xmlDocument.createElement("data");
				child.setAttribute("name", e.getKey());
				child.setTextContent(e.getValue().toString());
				request.appendChild(child);
			}
			// submits the ask question request and receives the response
			if (HTML4){
				child=xmlDocument.createElement("type");
				child.setTextContent("POST");
				request.appendChild(child);
				nextURL=doSubmit(nextLink.append("/commentitem"),data);
			}
			else{
				child=xmlDocument.createElement("type");
				child.setTextContent("GET");
				request.appendChild(child);
				nextURL=openHTML5PageWithRedirect(nextLink.append("/commentitem?").append(createURL(data)));
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
			nextLink=new StringBuilder(action.getElementsByTagName("nextPage").item(0).getTextContent());
			request=(Element)((Element)action).getElementsByTagName("request").item(0);
			nextURL=new StringBuilder(request.getElementsByTagName("url").item(0).getTextContent());
//			data.clear();
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
//			if(data.containsKey("userID"))
//				data.put("userID",new StringBuilder(Long.toString(client.getClientID())));
			if(nextURL.indexOf("userID=")!=-1){
				int start=nextURL.indexOf("&userID=")+"&userID=".length();
				int end=nextURL.indexOf("&",start);
				if(end==-1)
					end=nextURL.length();
				nextURL.replace(start, end, client.getClientInfo().getAuthToken().toString());
			}

			// Think Time
			try{Thread.sleep(getThinkTime());}
			catch(InterruptedException e){
				client.setExit(true);
				return null;
			}

			if (HTML4){
				nextURL=doSubmit(nextURL,data);
			}
			else{
				nextURL=openHTML5PageWithRedirect(nextURL.append("?").append(createURL(data)));
			}			
		}

		return nextURL;
	}

	/**
	 * Determines the think time before going to the next page
	 * Deducts from the think time the amount of time since the 
	 * page opened until this calculation is performed
	 * @return Think time in ms
	 */
	private int getThinkTime(){
		int thinkTime=typingErrorThinkTime;
		if(RunSettings.isRepeatedRun()==false){
			thinkTime+=(int)expDist(initialThinkTime+3000)+(int)(((comment.length())/client.getTypingSpeed()));
		}else{
			thinkTime=Integer.parseInt(((Element)action).getElementsByTagName("thinkTime").item(0).getTextContent());
		}
		if (verbose)System.out.println("User: "+client.getClientInfo().getUsername()+" - Think Time: "+thinkTime+" ms");
		if (RunSettings.isOutputThinkTimes()==true)
			client.getCg().getThinkTimeHist().add(thinkTime);
		pageThinkTime=thinkTime;
		return Math.max((int) ((thinkTime-(new Date().getTime()-pageOpenTime))/RunSettings.getThinkTimeSpeedUpFactor()),0);
	}


}
