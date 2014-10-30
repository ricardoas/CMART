package client.Pages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import client.clientMain.RunSettings;


/**
 * Bid Confirm Page
 * @author Andrew Fox
 *
 */


public class BidConfirmPage extends Page {
	TreeMap<Double,StringBuilder> nextPageProbabilities=new TreeMap<Double,StringBuilder>();	// map of probabilities of what the next page will be
	double pageRTFactor=1.5;

	public BidConfirmPage(Page page){
		super(page.url,page.html,page.client,page.pageType,page.pageOpenTime,page.lastPageType,page.lastURL);
		client.pageSpecificRT(pageRTFactor);	// change RT threshold depending on page type
		client.incItemsBid();					// indicate that an item has been bid on
		searchData=getFormData("search");		// get the search form data
		updateProbabilities();					// update the next move probabilities
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
		StringBuilder nextURL=new StringBuilder(client.getCMARTurl().getAppURL());	// the URL of the next link to be opened

		StringBuilder nextLink=getRandomStringBuilderFromDist(nextPageProbabilities);		// randomly chooses the next link

		if(RunSettings.isRepeatedRun()==false){
			action=xmlDocument.createElement("action");
			action.setAttribute("id",client.getActionNum());
			Element currentPage=xmlDocument.createElement("currentPage");
			currentPage.setTextContent(Integer.toString(pageType));
			action.appendChild(currentPage);
			Element nextPage=xmlDocument.createElement("nextPage");
			nextPage.setTextContent(nextLink.toString());
			action.appendChild(nextPage);
			request=xmlDocument.createElement("request");

			if (HTML4==true){
				if (nextLink.toString().equals(SEARCH_TEXT)){	// if the next move is to search
					return search(searchData,action);					// search for data
				}
				else{											// otherwise parse the appropriate link from the page
					int end=html.indexOf("\"/>"+nextLink);
					int start=html.lastIndexOf("href=\".",end)+("href=\".").length();
					end=html.indexOf("\">",start);
					nextURL.append(html.subSequence(start,end));
				}
			}
			else{
				String nextLinkS=nextLink.toString();
				if(nextLinkS.equals(MY_ACCOUNT_TEXT)){
					nextURL.append("/myaccount?useHTML5=1&userID=").append(client.getClientInfo().getHTML5Cache().get("userID")).append("&authToken=").append(client.getClientInfo().getHTML5Cache().get("authToken")).append("&ts=").append(new StringBuilder(Long.toString(new Date().getTime())));
				}
				else if(nextLinkS.equals(BROWSE_TEXT)){
					nextURL.append("/browsecategory?useHTML5=1&categoryID=0&pageNo=0&itemsPP=25&userID=").append(client.getClientInfo().getHTML5Cache().get("userID")).append("&authToken=").append(client.getClientInfo().getHTML5Cache().get("authToken")).append("&catTs=").append(new Date().getTime()).append("&hasItems=").append(getHasItems());
				}
				else if(nextLinkS.equals(SELL_TEXT)){
					nextURL.append("/sell.html");
				}
				else if(nextLinkS.equals(LOGOUT_TEXT)){
					nextURL.append("/logout.html");
				}
				else if(nextLinkS.equals(HOME_TEXT)){
					nextURL.append("/index.html");
				}
				else if(nextLinkS.equals(SEARCH_TEXT)){
					searchData.put("userID", new StringBuilder(client.getClientInfo().getHTML5Cache().get("userID")));
					searchData.put("authToken", new StringBuilder(client.getClientInfo().getHTML5Cache().get("authToken")));
					searchData.put("useHTML5", new StringBuilder("1"));
					searchData.put("pageNo", new StringBuilder("0"));
					searchData.put("itemsPP",new StringBuilder("25"));
					searchData.put("hasItems", getHasItems());
					return search(searchData,action);
				}
			}
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
			if(nextURL.indexOf("authToken=")!=-1){
				int start=nextURL.indexOf("&authToken=")+"&authToken=".length();
				int end=nextURL.indexOf("&",start);
				if(end==-1)
					end=nextURL.length();
				nextURL.replace(start, end, client.getClientInfo().getAuthToken().toString());
			}
			
			if(nextLink.toString().equals(SEARCH_TEXT)){
				HashMap<String, StringBuilder> data=new HashMap<String,StringBuilder>();
				NodeList dataList=request.getElementsByTagName("data");
				for(int i=0;i<dataList.getLength();i++){
					Node n=dataList.item(i);
					String key=n.getAttributes().item(0).getTextContent();
					StringBuilder value=new StringBuilder(((Element)n).getTextContent());
					data.put(key, value);
				}
				if(data.containsKey("authToken"))
					data.put("authToken",client.getClientInfo().getAuthToken());
				if(data.containsKey("userID"))
					data.put("userID",new StringBuilder(Long.toString(client.getClientID())));
				if(nextURL.indexOf("userID=")!=-1){
					int start=nextURL.indexOf("&userID=")+"&userID=".length();
					int end=nextURL.indexOf("&",start);
					if(end==-1)
						end=nextURL.length();
					nextURL.replace(start, end, client.getClientInfo().getAuthToken().toString());
				}
				return search(data,action);
			}
		}


		// Think Time
		try{Thread.sleep(getThinkTime());}
		catch(InterruptedException e){
			client.setExit(true);
			return null;
		}

		if(RunSettings.isRepeatedRun()==false){
			Element child=xmlDocument.createElement("type");
			child.setTextContent("GET");
			request.appendChild(child);
			child=xmlDocument.createElement("url");
			child.setTextContent(nextURL.toString());
			request.appendChild(child);

			child=xmlDocument.createElement("thinkTime");
			child.setTextContent(Integer.toString(pageThinkTime));
			action.appendChild(child);
			action.appendChild(request);
			client.addXMLAction(action);
		}

		return nextURL;	
	}

	/**
	 * Updates the probabilities of which page to transition to
	 */
	private void updateProbabilities(){
		double homeProb=RunSettings.getTransitionProb(pageType,0);
		double browseProb=RunSettings.getTransitionProb(pageType,1);
		double sellProb=RunSettings.getTransitionProb(pageType,2);
		double myAccountProb=RunSettings.getTransitionProb(pageType,3);
		double logoutProb=RunSettings.getTransitionProb(pageType,4);
		double searchProb=RunSettings.getTransitionProb(pageType,5);

		if(RunSettings.getWorkloadTypeCode()==1){
			homeProb*=0.8;
			browseProb*=1.2;
			sellProb*=0.5;
			myAccountProb*=0.88;
			searchProb*=1.17;
		}
		else if(RunSettings.getWorkloadTypeCode()==3){
			homeProb*=1.2;
			browseProb*=0.8;
			sellProb*=1.5;
			myAccountProb*=1.11;
			searchProb*=0.83;			
		}

		double probSum=1;

		TreeMap<String,Double>allOptions=new TreeMap<String,Double>();

		if (RunSettings.isMarkovTransitions()==false){
			homeProb*=(1.+(rand.nextDouble()-0.5)*.3);
			browseProb*=(1.+(rand.nextDouble()-0.5)*.3);
			sellProb*=(1.+(rand.nextDouble()-0.5)*.3);
			myAccountProb*=(1.+(rand.nextDouble()-0.5)*.3);
			logoutProb*=(1.+(rand.nextDouble()-0.5)*.3);
			searchProb*=(1.+(rand.nextDouble()-0.5)*.3);

			sellProb*=Math.exp(client.getExpSoldRate()*client.getItemsSold());
			browseProb*=Math.exp(client.getExpBuyRate()*(client.getItemsBid()+client.getItemsBought()));
			searchProb*=Math.exp(client.getExpBuyRate()*(client.getItemsBid()+client.getItemsBought()));
			myAccountProb*=Math.exp(client.getExpBuyRate()*client.getItemsBid());
			logoutProb*=Math.exp(client.getExpSoldRate()*client.getItemsSold()+client.getExpBuyRate()*(client.getItemsBid()+client.getItemsBought()));

			client.setOrigLogoutProb(logoutProb);
			logoutProb=adjustLogOutProb(logoutProb);
			client.setFinalLogoutProb(logoutProb);
		}		
		allOptions.put((HOME_TEXT),homeProb);
		allOptions.put((MY_ACCOUNT_TEXT),myAccountProb);
		allOptions.put((SELL_TEXT),sellProb);
		allOptions.put((BROWSE_TEXT),browseProb);
		allOptions.put((LOGOUT_TEXT),logoutProb);
		allOptions.put((SEARCH_TEXT),searchProb);


		// normalizes the probabilities
		double actualProbSum=0;
		for(Entry<String,Double> e:allOptions.entrySet()){
			actualProbSum+=e.getValue();
		}

		client.setRestProb(actualProbSum-logoutProb);

		for (Entry<String, Double> e:allOptions.entrySet()){
			nextPageProbabilities.put(probSum, new StringBuilder(e.getKey()));
			probSum-=(e.getValue()/actualProbSum);
		}

	}

	/**
	 * Determines the think time before going to the next page
	 * Deducts from the think time the amount of time since the 
	 * page opened until this calculation is performed
	 * @return Think time in ms
	 */
	private int getThinkTime(){
		int thinkTime=(int)expDist(initialThinkTime+5000);
		if(RunSettings.isRepeatedRun()){
			thinkTime=Integer.parseInt(((Element)action).getElementsByTagName("thinkTime").item(0).getTextContent());
		}
		if (verbose)System.out.println("User: "+client.getClientInfo().getUsername()+" - Think Time: "+thinkTime+" ms");
		if (RunSettings.isOutputThinkTimes()==true)
			client.getCg().getThinkTimeHist().add(thinkTime);
		pageThinkTime=thinkTime;
		return Math.max((int) ((thinkTime-(new Date().getTime()-pageOpenTime))/RunSettings.getThinkTimeSpeedUpFactor()),0);
	}
}