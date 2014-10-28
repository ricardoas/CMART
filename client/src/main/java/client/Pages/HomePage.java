package client.Pages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.w3c.dom.*;

import client.Items.ItemCG;
import client.clientMain.*;


/**
 * Home Page
 * @author Andrew Fox
 *
 */


public class HomePage extends Page {
	TreeMap<Double,StringBuffer> nextPageProbabilities=new TreeMap<Double,StringBuffer>();
	double pageRTFactor=1.;
	ArrayList<ItemCG> recommendedItems=new ArrayList<ItemCG>();		// list of items recommended by C-MART to the client

	private static final String HREF_TEXT="href=\".";

	public HomePage(Page page){
		super(page.url,page.html,page.client,page.pageType,page.pageOpenTime,page.lastPageType,page.lastURL,page.cg);
		client.pageSpecificRT(pageRTFactor);	// change RT threshold depending on page type
		searchData=getFormData("search");
		if(client.isLoggedIn())
			getRecommendedItems();
		updateProbabilities();
	}

	/**
	 * Determines the next link to go to in the application
	 * @return the URL or HTML of the next link, depending on whether
	 * 		   the previous entry contained a form or not
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws UnsupportedEncodingException 
	 */
	public StringBuffer makeDecision() throws UnsupportedEncodingException, IOException, InterruptedException{
		StringBuffer nextURL=new StringBuffer(client.getCMARTurl().getAppURL());
		StringBuffer nextLink=new StringBuffer();
		String nextLinkS=null;

		if(RunSettings.isRepeatedRun()==false){
			nextLink=getRandomStringBufferFromDist(nextPageProbabilities);
			nextLinkS=nextLink.toString();

			if(verbose)System.out.println("Next Link: "+nextLink);

			action=xmlDocument.createElement("action");
			action.setAttribute("id",client.getActionNum());
			Element currentPage=xmlDocument.createElement("currentPage");
			currentPage.setTextContent(Integer.toString(pageType));
			action.appendChild(currentPage);
			Element nextPage=xmlDocument.createElement("nextPage");
			nextPage.setTextContent(nextLinkS);
			action.appendChild(nextPage);
			request=xmlDocument.createElement("request");


			if(HTML4){
				if (nextLink.toString().equals(SEARCH_TEXT)){
					return search(searchData,action);
				}
				else if(nextLinkS.startsWith("recommend")){
					int start=html.indexOf("/index?")+"/index?".length();
					int end=html.indexOf("\">",start);
					nextURL.append("/viewitem?").append(html.subSequence(start, end)).append("&itemID=").append(nextLink.substring(nextLink.lastIndexOf("d")+1));
				}
				else{
					int end=html.indexOf("\"/>"+nextLink);
					int start=html.lastIndexOf(HREF_TEXT,end)+(HREF_TEXT).length();
					end=html.indexOf("\">",start);
					nextURL.append(html.subSequence(start,end));
				}
			}
			else{
				if(nextLinkS.equals(LOGIN_TEXT)){
					nextURL.append("/login.html");
				}
				else if(nextLinkS.equals(REGISTER_TEXT)){
					nextURL.append("/register.html");
				}
				else if(nextLinkS.equals(BROWSE_TEXT)){
					nextURL.append("/browsecategory?useHTML5=1&categoryID=0&pageNo=0&itemsPP=25&userID=").append(client.getClientInfo().getHTML5Cache().get("userID")).append("&authToken=").append(client.getClientInfo().getHTML5Cache().get("authToken")).append("&catTs=").append(new Date().getTime()).append("&hasItems=").append(getHasItems());
				}
				else if(nextLinkS.equals(MY_ACCOUNT_TEXT)){
					nextURL.append("/myaccount?useHTML5=1&userID=").append(client.getClientInfo().getHTML5Cache().get("userID")).append("&authToken=").append(client.getClientInfo().getHTML5Cache().get("authToken")).append("&ts=").append(new StringBuffer(Long.toString(new Date().getTime())));
				}
				else if(nextLinkS.equals(SELL_TEXT)){
					nextURL.append("/sell.html");
				}
				else if(nextLinkS.equals(LOGOUT_TEXT)){
					nextURL.append("/logout.html");
				}
				else if(nextLinkS.startsWith("recommend")){
					String itemID=nextLinkS.substring(nextLinkS.lastIndexOf("d")+1);
					nextURL.append("/viewitem?useHTML5=1&itemID=").append(itemID);
				}
				else if(nextLinkS.equals(SEARCH_TEXT)){
					searchData.put("userID", new StringBuffer(client.getClientInfo().getHTML5Cache().get("userID")));
					searchData.put("authToken", new StringBuffer(client.getClientInfo().getHTML5Cache().get("authToken")));
					searchData.put("useHTML5", new StringBuffer("1"));
					searchData.put("pageNo", new StringBuffer("0"));
					searchData.put("itemsPP",new StringBuffer("25"));
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
			nextLinkS=action.getElementsByTagName("nextPage").item(0).getTextContent();
			nextLink=new StringBuffer(nextLinkS);
			request=(Element)((Element)action).getElementsByTagName("request").item(0);
			nextURL=new StringBuffer(request.getElementsByTagName("url").item(0).getTextContent());
			if(nextURL.indexOf("authToken=")!=-1){
				int start=nextURL.indexOf("&authToken=")+"&authToken=".length();
				int end=nextURL.indexOf("&",start);
				if(end==-1)
					end=nextURL.length();
				nextURL.replace(start, end, client.getClientInfo().getAuthToken().toString());
			}
			
				HashMap<String, StringBuffer> data=new HashMap<String,StringBuffer>();
				NodeList dataList=request.getElementsByTagName("data");
				for(int i=0;i<dataList.getLength();i++){
					Node n=dataList.item(i);
					String key=n.getAttributes().item(0).getTextContent();
					StringBuffer value=new StringBuffer(((Element)n).getTextContent());
					data.put(key, value);
				}
				if(data.containsKey("authToken"))
					data.put("authToken",client.getClientInfo().getAuthToken());
				if(data.containsKey("userID"))
					data.put("userID",new StringBuffer(Long.toString(client.getClientID())));
				if(nextURL.indexOf("userID=")!=-1){
					int start=nextURL.indexOf("&userID=")+"&userID=".length();
					int end=nextURL.indexOf("&",start);
					if(end==-1)
						end=nextURL.length();
					nextURL.replace(start, end, client.getClientInfo().getAuthToken().toString());
				}
				if(nextLink.toString().equals(SEARCH_TEXT)){
				return search(data,action);
			}
			if(nextLinkS.startsWith("recommend")){
				if(!nextPageProbabilities.containsValue(nextLink)){
					if(recommendedItems.isEmpty()){
						client.setExit(true);
						client.setExitDueToRepeatChange(true);
						return null;
					}else{
						client.setChangeDueToRepeatChange(true);
						int start=nextURL.indexOf("&itemID=")+"&itemID=".length();
						String itemID;
						if(nextURL.indexOf("&",start)==-1)
							itemID=(nextURL.substring(start));
						else
							itemID=(nextURL.substring(start,nextURL.indexOf("&",start)));
						nextURL.replace(start, start+itemID.length(), Long.toString(recommendedItems.get(0).getId()));
					}
				}
			}
		}
		// Think Time
		try{Thread.sleep(getThinkTime(nextLinkS));}
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
	 * Populates the page with the items on the home page recommended by C-MART
	 */
	private void getRecommendedItems(){
		StringBuffer XMLString=new StringBuffer();
		if (HTML4){
			XMLString=new StringBuffer(client.getMessage());
		}
		else{
			XMLString.append(html);
		}

		int start=XMLString.indexOf("<item>");
		int itemEnd=XMLString.indexOf("</item>",start);
		while(itemEnd!=-1&&start!=-1){
			ItemCG item=new ItemCG(client);;
			start=XMLString.indexOf("<id>",start)+"<id>".length();
			int end=XMLString.indexOf("</id>",start);
			long itemID=Long.parseLong(XMLString.substring(start, end));
			if(client.getItemsOfInterest().containsKey(itemID))
				item=client.getItemsOfInterest().get(itemID);
			else
				item.setId(itemID);
			start=XMLString.indexOf("<name>",end)+"<name>".length();
			end=XMLString.indexOf("</name>",start);
			item.setName(XMLString.substring(start,end));
			start=XMLString.indexOf("<thumbnailURL>",end)+"<thumbnailURL>".length();
			end=XMLString.indexOf("</thumbnailURL>",start);
			if(XMLString.substring(start,end).indexOf("blank.jpg")!=-1)
				item.setNumPics(0);
			else
				if(item.getNumPics()<1)
					item.setNumPics(1);
			item.calcItemRating();
			recommendedItems.add(item);
			client.addToItemsOfInterest(itemID, item);
			start=XMLString.indexOf("<item>",itemEnd);
			itemEnd=XMLString.indexOf("</item>",start);
		}

	}


	private void updateProbabilities(){
		double probSum=1;

		double myAccountProb=RunSettings.getTransitionProb(pageType,0);
		double sellProb=RunSettings.getTransitionProb(pageType,1);
		double browseProb=RunSettings.getTransitionProb(pageType,2);
		double loginProb=RunSettings.getTransitionProb(pageType,3);
		double registerProb=RunSettings.getTransitionProb(pageType,4);
		double searchProb=RunSettings.getTransitionProb(pageType,5);
		double logOutProb=RunSettings.getTransitionProb(pageType,6);
		double recommendedItemsProb=RunSettings.getTransitionProb(pageType,7);

		if(RunSettings.getWorkloadType()==1){
			sellProb*=0.45;
			browseProb*=1.2;	
			searchProb*=1.2;
		}
		else if(RunSettings.getWorkloadType()==3){
			sellProb*=1.8;
			browseProb*=0.8;
			searchProb*=0.8;
		}

		if (client.isLoggedIn()==false){
			searchProb=0;
			myAccountProb=0;
			sellProb=0;
			browseProb=0;
			searchProb=0;
			logOutProb=0;
			recommendedItemsProb=0;
			if (client.getClientInfo().isRegistered()==false){
				loginProb=0;
				registerProb=1;
			}
			else{
				loginProb=1;
				registerProb=0;
			}
		}
		else{
			loginProb=0;
			registerProb=0;
		}

		double ratingSum=0;


		if (RunSettings.isMarkovTransitions()==false){
			browseProb*=(1.+(rand.nextDouble()-0.5)*.3);
			sellProb*=(1.+(rand.nextDouble()-0.5)*.3);
			myAccountProb*=(1.+(rand.nextDouble()-0.5)*.3);
			logOutProb*=(1.+(rand.nextDouble()-0.5)*.3);
			searchProb*=(1.+(rand.nextDouble()-0.5)*.3);
			recommendedItemsProb*=(1.+(rand.nextDouble()-0.5)*.3);

			sellProb*=Math.exp(client.getExpSoldRate()*client.getItemsSold());
			browseProb*=Math.exp(client.getExpBuyRate()*(client.getItemsBid()+client.getItemsBought()));
			searchProb*=Math.exp(client.getExpBuyRate()*(client.getItemsBid()+client.getItemsBought()));
			myAccountProb*=Math.exp(client.getExpBuyRate()*client.getItemsBid());
			recommendedItemsProb*=Math.exp(client.getExpBuyRate()*(client.getItemsBid()+client.getItemsBought()));
			logOutProb*=Math.exp(client.getExpSoldRate()*client.getItemsSold()+client.getExpBuyRate()*(client.getItemsBid()+client.getItemsBought()));

			double standardRating=4*recommendedItems.size();
			for (ItemCG icg:recommendedItems){
				ratingSum+=icg.getItemRating();
			}

			if(recommendedItems.size()>0){
				recommendedItemsProb*=ratingSum/standardRating;
				if(recommendedItemsProb<0)
					recommendedItemsProb=0;
			}
			else{
				recommendedItemsProb=0;
			}

			client.setOrigLogoutProb(logOutProb);
			logOutProb=adjustLogOutProb(logOutProb);
			client.setFinalLogoutProb(logOutProb);
		}
		else{
			if (recommendedItems.size()==0)
				recommendedItemsProb=0;
		}

		TreeMap<String,Double>allOptions=new TreeMap<String,Double>();

		allOptions.put((SEARCH_TEXT),searchProb);
		allOptions.put((MY_ACCOUNT_TEXT),myAccountProb);
		allOptions.put((SELL_TEXT),sellProb);
		allOptions.put((BROWSE_TEXT),browseProb);
		allOptions.put((SEARCH_TEXT),searchProb);
		allOptions.put((LOGIN_TEXT),loginProb);
		allOptions.put((REGISTER_TEXT),registerProb);
		allOptions.put((LOGOUT_TEXT),logOutProb);
		for(ItemCG icg:recommendedItems){
			allOptions.put("recommend"+icg.getId(),recommendedItemsProb*icg.getItemRating()/(ratingSum));
		}


		double actualProbSum=0;
		for(Entry<String,Double> e:allOptions.entrySet()){
			actualProbSum+=e.getValue();
		}
		client.setRestProb(actualProbSum-logOutProb);

		for (Entry<String, Double> e:allOptions.entrySet()){
			nextPageProbabilities.put(probSum, new StringBuffer(e.getKey()));
			probSum-=(e.getValue()/actualProbSum);
		}

	}

	/**
	 * Determines the think time before going to the next page
	 * Deducts from the think time the amount of time since the 
	 * page opened until this calculation is performed
	 * @return Think time in ms
	 */
	private int getThinkTime(String next){
		int thinkTime=(int)(initialThinkTime);
		if(RunSettings.isRepeatedRun()==false){
			if (next.equals(LOGIN_TEXT)||next.equals(REGISTER_TEXT))
				thinkTime+=5000;
			else
				thinkTime+=7000;
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