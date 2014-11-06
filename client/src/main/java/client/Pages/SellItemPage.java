package client.Pages;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import client.clientMain.RunSettings;

/**
 * Sell Item Page
 * @author Andrew Fox
 *
 */

public class SellItemPage extends Page {
	TreeMap<Double,StringBuilder> nextPageProbabilities=new TreeMap<Double,StringBuilder>();
	double startPrice=0;
	double reservePrice=0;
	double buyNowPrice=0;
	Calendar sellTime=Calendar.getInstance();
	double pageRTFactor=1.;
	int bonusCharacters=0;

	HashMap<String,StringBuilder>data=new HashMap<String,StringBuilder>();

	public SellItemPage(Page page){
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
	@Override public StringBuilder makeDecision() throws UnsupportedEncodingException, IOException, InterruptedException{
		StringBuilder nextPage=new StringBuilder(client.getCMARTurl().getAppURL());


		if(HTML4){
			data=getFormData("sellitem");
			searchData=getFormData("search");
			System.out.println("  \tSellItemPage.makeDecision() ");
			System.out.println("  \t" + data);
			System.out.println("  \t" + searchData);
		}

		updateProbabilities();

		if(RunSettings.isRepeatedRun()==false){
			StringBuilder nextLink=getRandomStringBuilderFromDist(nextPageProbabilities);

			action=xmlDocument.createElement("action");
			action.setAttribute("id",client.getActionNum());
			Element currentPage=xmlDocument.createElement("currentPage");
			currentPage.setTextContent(Integer.toString(pageType));
			action.appendChild(currentPage);
			Element nextPagee=xmlDocument.createElement("nextPage");
			nextPagee.setTextContent(nextLink.toString());
			action.appendChild(nextPagee);
			request=xmlDocument.createElement("request");

			if (nextLink.toString().equals(SEARCH_TEXT)){
				if(!HTML4){
					searchData.put("userID", new StringBuilder(client.getClientInfo().getHTML5Cache().get("userID")));
					searchData.put("authToken", new StringBuilder(client.getClientInfo().getHTML5Cache().get("authToken")));
					searchData.put("useHTML5", new StringBuilder("1"));
					searchData.put("pageNo", new StringBuilder("0"));
					searchData.put("itemsPP",new StringBuilder("25"));
					searchData.put("hasItems", getHasItems());
				}
				return search(searchData,action);
			}
			else if (nextLink.toString().equals("Sell")){
				StringBuilder auctionType=getRandomStringBuilderFromDist(RunSettings.getAuctionType());
				String auctionTypeS=auctionType.toString();

				SimpleDateFormat obDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				double sellDateProb=rand.nextDouble();
				if (sellDateProb<0.25)
					sellTime.add(Calendar.DAY_OF_YEAR,1);
				else if(sellDateProb<0.5)
					sellTime.add(Calendar.DAY_OF_YEAR,3);
				else if(sellDateProb<0.75)
					sellTime.add(Calendar.DAY_OF_YEAR,7);
				else 
					sellTime.add(Calendar.DAY_OF_YEAR,14);


				int numWords=getRandomIntFromDist(RunSettings.getTitleTotalWords());;


				StringBuilder itemTitle=new StringBuilder();
				for (int i=0;i<numWords;i++){
					itemTitle.append(getRandomStringBuilderFromDist(RunSettings.getTitleWords()));
					if (i!=numWords-1)
						itemTitle.append(" ");
				}
				if(verbose)System.out.println("Item Title: "+itemTitle);
				if (auctionTypeS.equals("auction")){
					this.startPrice=getRandomDoubleFromDist(RunSettings.getStartingBid())-5*rand.nextDouble();
					if(rand.nextDouble()>0.5)
						this.reservePrice=1.1*this.startPrice;
				}
				else if(auctionTypeS.equals("buyNow")){
					this.buyNowPrice=getRandomDoubleFromDist(RunSettings.getBuyNowPrice())-5*rand.nextDouble();
				}
				else{
					this.startPrice=getRandomDoubleFromDist(RunSettings.getStartingBid())-5*rand.nextDouble();
					TreeMap<Double,Double>buyNowOptions=new TreeMap<Double,Double>();
					if(rand.nextDouble()>0.5){
						this.reservePrice=1.1*this.startPrice;
						buyNowOptions=fixMapProbs(RunSettings.getBuyNowPriceReverse().tailMap(this.reservePrice));
					}
					else
						buyNowOptions=fixMapProbs(RunSettings.getBuyNowPriceReverse().tailMap(this.startPrice));

					this.buyNowPrice=getRandomDoubleFromDist(buyNowOptions)-5*rand.nextDouble();
				}

				int numWordsDescription=getRandomIntFromDist(RunSettings.getItemSpecificsDesc());
				StringBuilder itemSpecifics=new StringBuilder();
				for (int i=0;i<numWordsDescription;i++){
					itemSpecifics.append(getRandomStringBuilderFromDist(RunSettings.getItemSpecificsDescWords()));
					if (i!=numWords-1)
						itemSpecifics.append(" ");
				}


				data.put("name", typingError(itemTitle));
				data.put("description", typingError(itemSpecifics));
				data.put("startPrice", new StringBuilder(Double.toString(((double)Math.round(100*startPrice))/100)));
				data.put("reservePrice", new StringBuilder(Double.toString(((double)Math.round(100*reservePrice))/100)));
				data.put("buyNowPrice", new StringBuilder(Double.toString(((double)Math.round(100*buyNowPrice))/100)));
				data.put("categoryID", new StringBuilder(Integer.toString(getRandomIntFromDist(RunSettings.getCategories()))));
				data.put("quantity", new StringBuilder("1"));
				data.put("endDate",new StringBuilder(obDateFormat.format(sellTime.getTime())));
				if(!HTML4){
					data.put("useHTML5",new StringBuilder("1"));
					data.put("userID", client.getClientInfo().getHTML5Cache().get("userID"));
					data.put("authToken", client.getClientInfo().getHTML5Cache().get("authToken"));
				}
				if(verbose)System.out.println("SELLDATA "+data);

			}
			else{
				if(HTML4){
					int end=html.indexOf("\"/>"+nextLink);
					int start=html.lastIndexOf("href=\".",end)+("href=\".").length();
					end=html.indexOf("\">",start);
					nextPage.append(html.subSequence(start,end));
				}
				else{
					String nextLinkS=nextLink.toString();
					if(nextLinkS.equals(BROWSE_TEXT)){
						nextPage.append("/browsecategory?useHTML5=1&categoryID=0&pageNo=0&itemsPP=25&userID=").append(client.getClientInfo().getHTML5Cache().get("userID")).append("&authToken=").append(client.getClientInfo().getHTML5Cache().get("authToken")).append("&catTs=").append(new Date().getTime()).append("&hasItems=").append(getHasItems());
					}
					else if(nextLinkS.equals(MY_ACCOUNT_TEXT)){
						nextPage.append("/myaccount?useHTML5=1&userID=").append(client.getClientInfo().getHTML5Cache().get("userID")).append("&authToken=").append(client.getClientInfo().getHTML5Cache().get("authToken")).append("&ts=").append(new StringBuilder(Long.toString(new Date().getTime())));
					}
					else if(nextLinkS.equals(HOME_TEXT)){
						nextPage.append("/index.html");
					}
					else if(nextLinkS.equals(LOGOUT_TEXT)){
						nextPage.append("/logout.html");
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
			}

			// Think Time
			try{Thread.sleep(getThinkTime(nextLink.toString()));}
			catch(InterruptedException e){
				client.setExit(true);
				return null;
			}

			if(nextLink.toString().equals("Sell")){
				Element child=xmlDocument.createElement("url");
				child.setTextContent(new StringBuilder(client.getCMARTurl().getAppURL()).append("/sellitem").toString());
				request.appendChild(child);
				for (Entry<String,StringBuilder> e:data.entrySet()){
					child=xmlDocument.createElement("data");
					child.setAttribute("name", e.getKey());
					child.setTextContent(e.getValue().toString());
					request.appendChild(child);
				}
				if(HTML4){
					child=xmlDocument.createElement("type");
					child.setTextContent("POST");
					request.appendChild(child);
					nextPage=doSubmit(new StringBuilder(client.getCMARTurl().getAppURL()).append("/sellitem?"),data);
				}
				else{
					child=xmlDocument.createElement("type");
					child.setTextContent("GET");
					request.appendChild(child);
					nextPage=openHTML5PageWithRedirect(new StringBuilder(client.getCMARTurl().getAppURL()).append("/sellitem?").append(createURL(data)));
				}
			}else{
				Element child=xmlDocument.createElement("type");
				child.setTextContent("GET");
				request.appendChild(child);
				child=xmlDocument.createElement("url");
				child.setTextContent(nextPage.toString());
				request.appendChild(child);
			}

			Element child=xmlDocument.createElement("thinkTime");
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
			String nextLink=action.getElementsByTagName("nextPage").item(0).getTextContent();
			request=(Element)((Element)action).getElementsByTagName("request").item(0);
			StringBuilder nextURL=new StringBuilder(request.getElementsByTagName("url").item(0).getTextContent());
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
				if(data.containsKey("userID"))
					data.put("userID",new StringBuilder(Long.toString(client.getClientID())));
				if(nextURL.indexOf("userID=")!=-1){
					int start=nextURL.indexOf("&userID=")+"&userID=".length();
					int end=nextURL.indexOf("&",start);
					if(end==-1)
						end=nextURL.length();
					nextURL.replace(start, end, client.getClientInfo().getAuthToken().toString());
				}
			if(nextURL.indexOf("authToken=")!=-1){
				int start=nextURL.indexOf("&authToken=")+"&authToken=".length();
				int end=nextURL.indexOf("&",start);
				if(end==-1)
					end=nextURL.length();
				nextURL.replace(start, end, client.getClientInfo().getAuthToken().toString());
			}
			
			if(nextLink.toString().equals(SEARCH_TEXT)){
				return search(data,action);
			}

			// Think Time
			try{Thread.sleep(getThinkTime(null));}
			catch(InterruptedException e){
				client.setExit(true);
				return null;
			}

			if(nextLink.equals(SELL_TEXT)){
				if(HTML4){
					nextPage=doSubmit(nextURL,data);
				}
				else{
					nextPage=openHTML5PageWithRedirect(nextURL.append("?").append(createURL(data)));
				}
			}else{
				nextPage=nextURL;
			}

		}

		if(verbose)System.out.println(nextPage);
		return nextPage;
	}


	/**
	 * Updates the probabilities of which page to transition to
	 */
	private void updateProbabilities(){
		double probSum=1;

		double homeProb=RunSettings.getTransitionProb(pageType,0);
		double browseProb=RunSettings.getTransitionProb(pageType,1);
		double sellProb=RunSettings.getTransitionProb(pageType,2);
		double myAccountProb=RunSettings.getTransitionProb(pageType,3);
		double logOutProb=RunSettings.getTransitionProb(pageType,4);
		double searchProb=RunSettings.getTransitionProb(pageType,5);


		if(RunSettings.getWorkloadTypeCode()==1){
			homeProb*=1.33;
			searchProb*=1.67;
			myAccountProb*=1.33;
			sellProb*=0.87;
			browseProb*=1.6;
		}
		else if(RunSettings.getWorkloadTypeCode()==3){
			homeProb*=0.67;
			searchProb*=0.33;
			myAccountProb*=0.67;
			sellProb*=1.13;
			browseProb*=0.4;
		}

		if (RunSettings.isMarkovTransitions()==false){
			homeProb*=(1.+(rand.nextDouble()-0.5)*.3);
			browseProb*=(1.+(rand.nextDouble()-0.5)*.3);
			sellProb*=(1.+(rand.nextDouble()-0.5)*.3);
			myAccountProb*=(1.+(rand.nextDouble()-0.5)*.3);
			logOutProb*=(1.+(rand.nextDouble()-0.5)*.3);
			searchProb*=(1.+(rand.nextDouble()-0.5)*.3);

			client.setOrigLogoutProb(logOutProb);
			logOutProb=adjustLogOutProb(logOutProb);
			client.setFinalLogoutProb(logOutProb);
		}

		TreeMap<String,Double>allOptions=new TreeMap<String,Double>();

		allOptions.put((HOME_TEXT),homeProb);
		allOptions.put((SEARCH_TEXT),searchProb);
		allOptions.put((MY_ACCOUNT_TEXT),myAccountProb);
		allOptions.put((SELL_TEXT),sellProb);
		allOptions.put((BROWSE_TEXT),browseProb);
		allOptions.put((LOGOUT_TEXT),logOutProb);

		double actualProbSum=0;
		for(Entry<String,Double> e:allOptions.entrySet()){
			actualProbSum+=e.getValue();
		}
		client.setRestProb(actualProbSum-logOutProb);

		for (Entry<String, Double> e:allOptions.entrySet()){
			nextPageProbabilities.put(probSum, new StringBuilder(e.getKey()));
			probSum-=(e.getValue()/actualProbSum);
		}

	}


	/**
	 * Recalculates probabilities of a truncated distribution to
	 * normalize all probabilities
	 * @param map1
	 * @return Truncated distribution with normalized probabilities
	 */
	private TreeMap<Double,Double> fixMapProbs(SortedMap <Double,Double> map1){
		TreeMap<Double,Double> map2 = new TreeMap<Double,Double>();
		double lastValue=map1.get(map1.lastKey());
		for (double m:map1.keySet()){
			map2.put((map1.get(m)-lastValue)/(1-lastValue), m);
		}

		return map2;
	}

	/**
	 * Determines the think time before going to the next page
	 * Deducts from the think time the amount of time since the 
	 * page opened until this calculation is performed
	 * @return Think time in ms
	 */
	private int getThinkTime(String nextMove){
		int thinkTime=typingErrorThinkTime;
		if(RunSettings.isRepeatedRun()==false){
			if (nextMove.equals(SELL_TEXT)){
				thinkTime+=bonusCharacters/client.getTypingSpeed();
				thinkTime+=(int)expDist(initialThinkTime)+(int)((data.get("name").length()+data.get("description").length()+Double.toString(startPrice).length()+Double.toString(reservePrice).length()+Double.toString(buyNowPrice).length()+data.get("endDate").length())/client.getTypingSpeed());
			}
			else
				thinkTime+=(int)expDist(initialThinkTime+5000);
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