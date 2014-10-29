package client.Pages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import org.w3c.dom.*;

import client.clientMain.*;


/**
 * Ask Question Page
 * @author Andrew Fox
 *
 */

public class AnswerQuestionPage extends Page{
	HashMap<String, StringBuilder> data = new HashMap<String, StringBuilder>();	// data to be sent to the login page
	StringBuilder answer=new StringBuilder();
	double pageRTFactor=1.;

	public AnswerQuestionPage(Page page){
		super(page.url,page.html,page.client,page.pageType,page.pageOpenTime,page.lastPageType,page.lastURL,page.cg);
		client.pageSpecificRT(pageRTFactor);	// change RT threshold depending on page type
	}

	/**
	 * Determines the next link to go to in the application
	 * @return the URL or HTML of the next link, depending on whether
	 * 		   the previous entry contained a form or not
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws UnsupportedEncodingException 
	 * @throws URISyntaxException 
	 */
	public StringBuilder makeDecision() throws UnsupportedEncodingException, IOException, InterruptedException, URISyntaxException{
		StringBuilder nextLink=new StringBuilder(client.getCMARTurl().getAppURL());	// link to send the login data to
		StringBuilder nextURL=new StringBuilder();	// the response page returned after the login attempt

		if(RunSettings.isRepeatedRun()==false){
			if(HTML4){
			searchData=getFormData("search");
			data=getFormData("answerquestion");
			}
			
			action=xmlDocument.createElement("action");
			action.setAttribute("id",client.getActionNum());
			Element currentPage=xmlDocument.createElement("currentPage");
			currentPage.setTextContent(Integer.toString(pageType));
			action.appendChild(currentPage);
			Element nextPage=xmlDocument.createElement("nextPage");
			nextPage.setTextContent("answerQuestion");
			action.appendChild(nextPage);
			request=xmlDocument.createElement("request");


			int numWordsInQuestion;
			do{
				numWordsInQuestion=(int)Math.round(rand.nextGaussian()*5+10);
			}while(numWordsInQuestion<=0);
			for (int i=0;i<numWordsInQuestion;i++){
				answer.append(getRandomStringBuilderFromDist(RunSettings.getTitleWords()));
				if (i!=numWordsInQuestion-1)
					answer.append(" ");
				else
					answer.append(".");
			}

			data.put("answer", typingError(answer));
			
			if(!HTML4){
				long questionID = 0;
				int start=url.indexOf("questionID=");
				int end;
				if(start!=-1){
					start+="questionID=".length();
					end=url.indexOf("&",start);
					if(end==-1)
						end=url.length();
					questionID=Long.parseLong(url.substring(start, end));
				}
				else{
					//TODO: get questionID from JSON
				}
				
				data.put("useHTML5", new StringBuilder("1"));
				data.put("userID", client.getClientInfo().getHTML5Cache().get("userID"));
				data.put("authToken", client.getClientInfo().getHTML5Cache().get("authToken"));
				data.put("itemID",new StringBuilder().append(client.getLastItemID()));
				data.put("questionID", new StringBuilder().append(questionID));
			}
			
		}else{
			findAction();
			if(pageType!=Integer.parseInt(action.getElementsByTagName("currentPage").item(0).getTextContent())){
				client.setExit(true);
				client.setExitDueToRepeatChange(true);
				return null;
			}
			//nextLink=new StringBuilder(action.getElementsByTagName("nextPage").item(0).getTextContent());
			request=(Element)action.getElementsByTagName("request").item(0);
			nextLink=new StringBuilder(request.getElementsByTagName("url").item(0).getTextContent());
			nextURL=nextLink;
			data.clear();
			NodeList dataList=request.getElementsByTagName("data");
			for(int i=0;i<dataList.getLength();i++){
				Node n=dataList.item(i);
				String key=n.getAttributes().item(0).getTextContent();
				StringBuilder value=new StringBuilder(((Element)n).getTextContent());
				data.put(key, value);
			}
			if(data.containsKey("authToken"))
				data.put("authToken",client.getClientInfo().getAuthToken());
			if(nextURL.indexOf("authToken=")!=-1){
				int start=nextURL.indexOf("&authToken=")+"&authToken=".length();
				int end=nextURL.indexOf("&",start);
				if(end==-1)
					end=nextURL.length();
				nextURL.replace(start, end, client.getClientInfo().getAuthToken().toString());
			}
			if(data.containsKey("userID"))
				data.put("userID",new StringBuilder(Long.toString(client.getClientID())));
			if(nextURL.indexOf("userID=")!=-1){
				int start=nextURL.indexOf("&userID=")+"&userID=".length();
				int end=nextURL.indexOf("&",start);
				if(end==-1)
					end=nextURL.length();
				nextURL.replace(start, end, client.getClientInfo().getAuthToken().toString());
			}
			
			int start=url.indexOf("&itemID=")+"&itemID=".length();
			String itemID;
			if(url.indexOf("&",start)==-1)
				itemID=(url.substring(start));
			else
				itemID=(url.substring(start,nextURL.indexOf("&",start)));
			if(data.containsKey("itemID")){
				if(!itemID.equals(data.get("itemID").toString())){
					data.put("itemID", new StringBuilder(itemID));
					client.setChangeDueToRepeatChange(true);
				}
			}
			if(nextURL.indexOf("itemID=")!=-1){
				start=nextURL.indexOf("&itemID=")+"&itemID=".length();
				String urlItemID;
				if(nextURL.indexOf("&",start)==-1)
					urlItemID=(nextURL.substring(start));
				else
					urlItemID=(nextURL.substring(start,nextURL.indexOf("&",start)));
				if(!itemID.equals(urlItemID)){
					nextURL.replace(start, start+urlItemID.length(), itemID);
					client.setChangeDueToRepeatChange(true);
				}
			}
		}


		// Think Time
		try{Thread.sleep(getThinkTime());}
		catch(InterruptedException e){
			client.setExit(true);
			return null;
		}

		if(RunSettings.isRepeatedRun()==false){
			Element child=xmlDocument.createElement("url");
			child.setTextContent(new StringBuilder(nextLink).append("/answerquestion").toString());
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
				nextURL=doSubmit(nextLink.append("/answerquestion"),data);
			}else{
				child=xmlDocument.createElement("type");
				child.setTextContent("GET");
				request.appendChild(child);
				nextURL=openHTML5PageWithRedirect(nextLink.append("/answerquestion?").append(createURL(data)));
			}

			child=xmlDocument.createElement("thinkTime");
			child.setTextContent(Integer.toString(pageThinkTime));
			action.appendChild(child);
			action.appendChild(request);
			client.addXMLAction(action);
		}else{
			if (HTML4){
				nextURL=doSubmit(nextLink,data);
			}else{
				nextURL=openHTML5PageWithRedirect(nextLink.append("?").append(createURL(data)));
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
			thinkTime+=(int)expDist(initialThinkTime)+(int)(((answer.length())/client.getTypingSpeed()));
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
