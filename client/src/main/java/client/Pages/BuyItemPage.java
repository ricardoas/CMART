package client.Pages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import client.clientMain.RunSettings;

/**
 * Buy Item Page
 * @author Andrew Fox
 *
 */

public class BuyItemPage extends Page{
	HashMap<String, StringBuilder> data=new HashMap<String,StringBuilder>();					// data to be submitted when buying an item
	TreeMap<Double,StringBuilder> nextPageProbabilities=new TreeMap<Double,StringBuilder>();	// map of probabilities of what the next page will be

	ArrayList<String> changedData=new ArrayList<String>();
	double pageRTFactor=1.2;

	public BuyItemPage(Page page){
		super(page.url,page.html,page.client,page.pageType,page.pageOpenTime,page.lastPageType,page.lastURL);
		client.pageSpecificRT(pageRTFactor);	// change RT threshold depending on page type
		if(HTML4)
			searchData=getFormData("search");
		updateProbabilities();
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
	@Override public StringBuilder makeDecision() throws UnsupportedEncodingException, IOException, InterruptedException, URISyntaxException{
		StringBuilder nextPage=new StringBuilder(client.getCMARTurl().getAppURL());
		String nextLinkS=getRandomStringBuilderFromDist(nextPageProbabilities).toString();		// randomly chooses the next link

		if(RunSettings.isRepeatedRun()==false){
			// prepares the xml document for output
			action=xmlDocument.createElement("action");
			action.setAttribute("id",client.getActionNum());
			Element currentPage=xmlDocument.createElement("currentPage");
			currentPage.setTextContent(Integer.toString(pageType));
			action.appendChild(currentPage);
			Element nextPagee=xmlDocument.createElement("nextPage");
			nextPagee.setTextContent(nextLinkS);
			action.appendChild(nextPagee);
			request=xmlDocument.createElement("request");

			if(nextLinkS.equals("buy")){
				if(HTML4){
					data=getFormData("buyitem");
				}

				/**
				 * Checks if form data matches the client's profile
				 */
				if(data.containsKey("town")==false){
					data.put("town",new StringBuilder(client.getClientInfo().getCity()));
					changedData.add("town");
				}
				else if (data.get("town").equals(client.getClientInfo().getCity())==false){
					data.put("town",new StringBuilder(client.getClientInfo().getCity()));
					changedData.add("town");
				}

				if(data.containsKey("street")==false){
					data.put("street",new StringBuilder(client.getClientInfo().getAddress()));
					changedData.add("street");
				}
				else if (data.get("street").equals(client.getClientInfo().getAddress())==false){
					data.put("street",new StringBuilder(client.getClientInfo().getAddress()));
					changedData.add("street");
				}

				if(data.containsKey("zip")==false){
					data.put("zip",new StringBuilder(client.getClientInfo().getZipcode()));
					changedData.add("zip");
				}
				else if (data.get("zip").equals(client.getClientInfo().getZipcode())==false){
					data.put("zip",new StringBuilder(client.getClientInfo().getZipcode()));
					changedData.add("zip");
				}

				if(data.containsKey("state")==false){
					data.put("state",new StringBuilder(client.getClientInfo().getUSStateCode()));
					changedData.add("state");
				}
				else if (data.get("state").equals(client.getClientInfo().getUSStateCode())==false){
					data.put("state",new StringBuilder(client.getClientInfo().getUSStateCode()));
					changedData.add("state");
				}



				data.put("nameOnCard", new StringBuilder(client.getClientInfo().getFirstName()).append(" ").append(client.getClientInfo().getLastName()));
				data.put("creditCardNo",new StringBuilder(client.getClientInfo().getCreditCardNum()));
				data.put("cvv",new StringBuilder(client.getClientInfo().getCvv2()));
				data.put("expirationDate",new StringBuilder(client.getClientInfo().getCreditCardExpiry()));
				if(!HTML4){
					data.put("useHTML5",new StringBuilder("1"));
					data.put("quantity",new StringBuilder("1"));
					data.put("userID",client.getClientInfo().getHTML5Cache().get("userID"));
					data.put("authToken",client.getClientInfo().getHTML5Cache().get("authToken"));
					//	data.put("itemID", new StringBuilder(Long.toString(client.getLastItemID())));
					data.put("itemID", new StringBuilder(url.substring(url.indexOf("itemID=")+"itemID=".length())));
				}

				// if the client information data in the form is different than saved then the user should next update their information
				if (changedData.isEmpty()==false&&HTML4)
					client.setMessage("update");

				// Think Time
				try{Thread.sleep(getThinkTime());}
				catch(InterruptedException e){
					client.setExit(true);
					return null;
				}

				Element child=xmlDocument.createElement("url");
				child.setTextContent(new StringBuilder(client.getCMARTurl().getAppURL()).append("/buyitem").toString());
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
					nextPage=doSubmit(new StringBuilder(client.getCMARTurl().getAppURL()).append("/buyitem"),data);
				}
				else{
					child=xmlDocument.createElement("type");
					child.setTextContent("GET");
					request.appendChild(child);
					nextPage=openHTML5PageWithRedirect(new StringBuilder(client.getCMARTurl().getAppURL()).append("/buyitem?").append(createURL(data)));
				}
			}
			else{
				if (HTML4==true){
					if (nextLinkS.equals(SEARCH_TEXT)){	// if the next move is to search
						return search(searchData,action);					// search for data
					}
					else{											// otherwise parse the appropriate link from the page
						int end=html.indexOf("\"/>"+nextLinkS);
						int start=html.lastIndexOf("href=\".",end)+("href=\".").length();
						end=html.indexOf("\">",start);
						nextPage.append(html.subSequence(start,end));
					}
				}
				else{
					if(nextLinkS.equals(MY_ACCOUNT_TEXT)){
						nextPage.append("/myaccount?useHTML5=1&userID=").append(client.getClientInfo().getHTML5Cache().get("userID")).append("&authToken=").append(client.getClientInfo().getHTML5Cache().get("authToken")).append("&ts=").append(new StringBuilder(Long.toString(new Date().getTime())));
					}
					else if(nextLinkS.equals(BROWSE_TEXT)){
						nextPage.append("/browsecategory?useHTML5=1&categoryID=0&pageNo=0&itemsPP=25&userID=").append(client.getClientInfo().getHTML5Cache().get("userID")).append("&authToken=").append(client.getClientInfo().getHTML5Cache().get("authToken")).append("&catTs=").append(new Date().getTime()).append("&hasItems=").append(getHasItems());
					}
					else if(nextLinkS.equals(SELL_TEXT)){
						nextPage.append("/sell.html");
					}
					else if(nextLinkS.equals(LOGOUT_TEXT)){
						nextPage.append("/logout.html");
					}
					else if(nextLinkS.equals(HOME_TEXT)){
						nextPage.append("/index.html");
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
		}else{	// repeated run
			findAction();		// gets the action for the page
			// if the page type in the repeated run does not match the current page then exit
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
			// makes sure the data in the URL and POST matches the client's info
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

			if(nextLink.toString().equals(SEARCH_TEXT)){
				return search(data,action);
			}

			// Think Time
			try{Thread.sleep(getThinkTime());}
			catch(InterruptedException e){
				client.setExit(true);
				return null;
			}

			if(nextLink.equals("buy")){
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

		return nextPage;
	}


	private void updateProbabilities(){
		double homeProb=RunSettings.getTransitionProb(pageType,0);
		double browseProb=RunSettings.getTransitionProb(pageType,1);
		double sellProb=RunSettings.getTransitionProb(pageType,2);
		double myAccountProb=RunSettings.getTransitionProb(pageType,3);
		double logoutProb=RunSettings.getTransitionProb(pageType,4);
		double searchProb=RunSettings.getTransitionProb(pageType,5);
		double buyItemProb=RunSettings.getTransitionProb(pageType,6);

		if(RunSettings.getWorkloadTypeCode()==1){
			homeProb*=0.85;
			browseProb*=1.2;
			sellProb*=0.5;
			myAccountProb*=0.88;
			searchProb*=1.17;
		}
		else if(RunSettings.getWorkloadTypeCode()==3){
			homeProb*=1.15;
			browseProb*=0.8;
			sellProb*=2;
			myAccountProb*=1.12;
			searchProb*=0.83;			
		}

		if(html.indexOf("Sorry. You could not buy the item.")==-1){
			homeProb=0.;
			browseProb=0.;
			sellProb=0.;
			myAccountProb=0.;
			logoutProb=0.;
			searchProb=0.;
		}
		else{
			buyItemProb=0;
		}

		double probSum=1;

		TreeMap<String,Double>allOptions=new TreeMap<String,Double>();

		homeProb*=(1.+(rand.nextDouble()-0.5)*.3);
		browseProb*=(1.+(rand.nextDouble()-0.5)*.3);
		sellProb*=(1.+(rand.nextDouble()-0.5)*.3);
		myAccountProb*=(1.+(rand.nextDouble()-0.5)*.3);
		logoutProb*=(1.+(rand.nextDouble()-0.5)*.3);
		searchProb*=(1.+(rand.nextDouble()-0.5)*.3);

		if (RunSettings.isMarkovTransitions()==false){
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
		allOptions.put(("buy"),buyItemProb);


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
		int thinkTime=(int) expDist(initialThinkTime+2500);
		if(RunSettings.isRepeatedRun()==false){
			thinkTime+=typingErrorThinkTime;
			thinkTime+=Math.round((data.get("nameOnCard").length()+data.get("creditCardNo").length()+data.get("cvv").length()+data.get("expirationDate").length())/client.getTypingSpeed());
			for (int i=0;i<changedData.size();i++){
				thinkTime+=Math.round((data.get(changedData.get(i)).length())/client.getTypingSpeed());
			}
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
