package client.Pages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import client.Items.*;
import client.Tools.DateParser;
import client.clientMain.*;


/**
 * My Account Page
 * @author Andrew Fox
 *
 */


public class MyAccountPage extends Page{
	TreeMap<Double,StringBuilder> nextPageProbabilities=new TreeMap<Double,StringBuilder>();
	double pageRTFactor=1.;

	public MyAccountPage(Page page) throws JsonParseException, JsonMappingException, IOException, ParseException{
		super(page.url,page.html,page.client,page.pageType,page.pageOpenTime,page.lastPageType,page.lastURL,page.cg);
		if(lastPageType==LOGIN_PAGE_NUM || lastPageType==REGISTER_PAGE_NUM){
			pageRTFactor=1.5;
			if(HTML4)
				client.setClientID(Long.parseLong(url.substring(url.indexOf("userID=")+"userID=".length(),url.indexOf("&",url.indexOf("userID=")))));
		}
		client.pageSpecificRT(pageRTFactor);	// change RT threshold depending on page type
		if(HTML4){
			client.resetMyAccountItems();		// resets items loaded on my account page as they will be reloaded and updated
			searchData=getFormData("search");	// loads search form data
			int start=html.indexOf("&authToken=")+"&authToken=".length();
			// sets the authToken for repeated runs as it will change from the readXmlDocument
			if(RunSettings.isRepeatedRun()&&client.getClientInfo().getAuthToken()==null)
				client.getClientInfo().setAuthToken(new StringBuilder(html.substring(start, html.indexOf("\">",start))));
		}
		if(html.indexOf("Currently Bidding Items")!=-1||!HTML4)
			updateAuctions();		// update the list of items that are currently being bid on
		if(!HTML4){					// for HTML5, update list of sellers from JSON response
			getSellers();
			getQuestions();
		}
		updateProbabilities();		// update the page transition probabilities
	}

	/**
	 * Determines the next link to go to in the application
	 * @return the URL or HTML of the next link, depending on whether
	 * 		   the previous entry contained a form or not
	 */
	public StringBuilder makeDecision() throws UnsupportedEncodingException, IOException, InterruptedException{
		StringBuilder nextURL=new StringBuilder(client.getCMARTurl().getAppURL());
		StringBuilder nextLink=getRandomStringBuilderFromDist(nextPageProbabilities);

		if(verbose)System.out.println("Next Link: "+nextLink);
		String nextLinkS=nextLink.toString();
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

			if(HTML4==true){
				int start=0;
				int end=0;
				if (nextLinkS.equals(HOME_TEXT)||nextLinkS.equals(BROWSE_TEXT)||nextLinkS.equals(SELL_TEXT)||nextLinkS.equals(MY_ACCOUNT_TEXT)||nextLinkS.equals(LOGOUT_TEXT)){
					end=html.indexOf((new StringBuilder("\"/>").append(nextLink)).toString());
					start=html.lastIndexOf("href=\"./",end)+("href=\"./").length();
					end=html.indexOf("\">",start);
				}
				else if(nextLinkS.equals(UPDATE_DETAILS_TEXT)){
					end=html.indexOf((new StringBuilder("\">").append(nextLink)).toString());
					start=html.lastIndexOf("<a href=\"./",end)+("<a href=\"./").length();
				}
				else{
					if (nextLinkS.equals(SEARCH_TEXT)){
						return search(searchData,action);
					}
					else{
						//System.out.println(nextLinkS);
						start=html.indexOf("<label for=\""+nextLink+"\">");
						start=html.indexOf("<a href=\"",start)+"<a href=\"".length();
						end=html.indexOf("\">",start);
					}
				}
				nextURL.append("/").append(html.subSequence(start,end));
				if(verbose)System.out.println("NEXTURL "+nextURL);
			}
			else{
				if(nextLinkS.equals(BROWSE_TEXT)){
					nextURL.append("/browsecategory?useHTML5=1&categoryID=0&pageNo=0&itemsPP=25&userID=").append(client.getClientInfo().getHTML5Cache().get("userID")).append("&authToken=").append(client.getClientInfo().getHTML5Cache().get("authToken")).append("&catTs=").append(new Date().getTime()).append("&hasItems=").append(getHasItems());
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
				else if(nextLinkS.equals(SELL_TEXT)){
					nextURL.append("/sell.html");
				}
				else if(nextLinkS.equals(LOGOUT_TEXT)){
					nextURL.append("/logout.html");
				}
				else if(nextLinkS.equals(HOME_TEXT)){
					nextURL.append("/index.html");
				}
				else if(nextLinkS.equals(UPDATE_DETAILS_TEXT)){
					HashMap<String,StringBuilder> updateData=new HashMap<String,StringBuilder>();
					updateData.put("userID", new StringBuilder(client.getClientInfo().getHTML5Cache().get("userID")));
					updateData.put("authToken", new StringBuilder(client.getClientInfo().getHTML5Cache().get("authToken")));
					updateData.put("useHTML5", new StringBuilder("1"));
					nextURL.append("/updateuserdetails?").append(createURL(updateData));
				}
				else if(nextLinkS.startsWith("currentBids")){
					long itemID=Long.parseLong(nextLink.substring("currentBids".length(),nextLink.length()));
					if(client.getCurrentBids().get(itemID).getTs()>=(new Date().getTime()-300000))				
						nextURL=new StringBuilder("ITEM").append(itemID);
					else
						nextURL.append("/viewitem?useHTML5=1&itemID=").append(itemID);
				}
				else if(nextLinkS.startsWith("previousBids")){
					long itemID=Long.parseLong(nextLink.substring("previousBids".length(),nextLink.length()));
					if(client.getEndedAuctions().get(itemID).getTs()>=(new Date().getTime()-300000))				
						nextURL=new StringBuilder("ITEM").append(itemID);
					else
						nextURL.append("/viewitem?useHTML5=1&itemID=").append(itemID);			
				}
				else if(nextLinkS.startsWith("purchases")){
					long itemID=Long.parseLong(nextLink.substring("purchases".length(),nextLink.length()));
					if(client.getPurchasedItems().get(itemID).getTs()>=(new Date().getTime()-300000))				
						nextURL=new StringBuilder("ITEM").append(itemID);
					else
						nextURL.append("/viewitem?useHTML5=1&itemID=").append(itemID);			
				}
				else if(nextLinkS.startsWith("currentlySelling")){
					long itemID=Long.parseLong(nextLink.substring("currentlySelling".length(),nextLink.length()));
					if(client.getSellingItems().get(itemID).getTs()>=(new Date().getTime()-300000))				
						nextURL=new StringBuilder("ITEM").append(itemID);
					else
						nextURL.append("/viewitem?useHTML5=1&itemID=").append(itemID);		
				}
				else if(nextLinkS.startsWith("previouslySold")){
					long itemID=Long.parseLong(nextLink.substring("previouslySold".length(),nextLink.length()));
					if(client.getSoldItems().get(itemID).getTs()>=(new Date().getTime()-300000))				
						nextURL=new StringBuilder("ITEM").append(itemID);
					else
						nextURL.append("/viewitem?useHTML5=1&itemID=").append(itemID);	
				}

			}

			Element child=xmlDocument.createElement("type");
			child.setTextContent("GET");
			request.appendChild(child);
			child=xmlDocument.createElement("url");
			child.setTextContent(nextURL.toString());
			request.appendChild(child);

		}else{
			findAction();
			if(pageType!=Integer.parseInt(action.getElementsByTagName("currentPage").item(0).getTextContent())){
				client.setExit(true);
				client.setExitDueToRepeatChange(true);
				return null;
			}
			nextLinkS=action.getElementsByTagName("nextPage").item(0).getTextContent();
			nextLink=new StringBuilder(nextLinkS);
			request=(Element)((Element)action).getElementsByTagName("request").item(0);
			nextURL=new StringBuilder(request.getElementsByTagName("url").item(0).getTextContent());
			if(nextURL.indexOf("authToken=")!=-1){
				int start=nextURL.indexOf("&authToken=")+"&authToken=".length();
				int end=nextURL.indexOf("&",start);
				if(end==-1)
					end=nextURL.length();
				nextURL.replace(start, end, client.getClientInfo().getAuthToken().toString());
			}
			if(!nextPageProbabilities.containsValue(nextLinkS)){
				client.setExit(true);
				client.setExitDueToRepeatChange(true);
				return null;
			}

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
			if(nextLink.toString().equals(SEARCH_TEXT)){
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
			Element child=xmlDocument.createElement("thinkTime");
			child.setTextContent(Integer.toString(pageThinkTime));
			action.appendChild(child);
			action.appendChild(request);
			client.addXMLAction(action);
		}

		return nextURL;
	}

	/**
	 * Updates the clinet's listings of items up for auction and items bidded on
	 */
	private void updateAuctions() throws JsonParseException, JsonMappingException, IOException, ParseException{
		if(HTML4==true){
			int sectionEnd=html.indexOf("Currently Bidding Items");
			int start=sectionEnd;
			for (int i=0;i<5;i++){
				//int start=html.indexOf("<div id=\"entry\">",sectionEnd)+("<div id=\"entry\">").length();
				ItemCG item=new ItemCG(client);
				if (i==0){
					sectionEnd=html.indexOf("Previous Bids",sectionEnd);
				}
				else if (i==1){
					start=sectionEnd;
					sectionEnd=html.indexOf("Purchases",sectionEnd);
				}
				else if (i==2){
					start=sectionEnd;
					sectionEnd=html.indexOf("Items Currently Selling",sectionEnd);
				}
				else if (i==3){
					start=sectionEnd;
					sectionEnd=html.indexOf("Items Previously Sold",sectionEnd);
				}
				else if (i==4){
					start=sectionEnd;
					sectionEnd=html.indexOf("</HTML>",sectionEnd);
				}

				long itemID=0;
				int itemEnd=html.indexOf("</a>",start);

				while(itemEnd<sectionEnd&&itemEnd!=-1){
					start=html.lastIndexOf("\">",itemEnd)+("\">").length();
					item.setName(html.substring(start,itemEnd));
					if (i<2)
						start=html.indexOf("<label for=\"bid",start)+("<label for=\"bid").length();
					else if (i>2)
						start=html.indexOf("<label for=\"maxBid",start)+("<label for=\"maxBid").length();
					else
						start=html.indexOf("<label for=\"price",start)+("<label for=\"price").length();
					int end=html.indexOf("\">",start);
					itemID=Long.parseLong(html.substring(start,end));
					if(client.getItemsOfInterest().containsKey(itemID))
						item=client.getItemsOfInterest().get(itemID);
					item.setId(itemID);
					start=html.indexOf("\">$",start)+"\">$".length();
					end=html.indexOf("</label>",start);
					if(i<1){
						item.setCurrentBid(Double.parseDouble(html.substring(start,end).replace(",", "")));
						item.setBidder(true);
					}
					else if(i==2){
						item.setCurrentBid(Double.parseDouble(html.substring(start,end).replace(",", "")));
						item.setBidder(true);
					}
					else{
						item.setMaxBid(Double.parseDouble(html.substring(start,end).replace(",", "")));
						item.setOwner(true);
					}

					if(i==0){
						start=html.lastIndexOf("src=\"",end)+"src=\"".length();
						end=html.indexOf("\"",start);
						if(html.substring(start,end).contains("blank"))
							item.setNumPics(0);
						else{
							if(item.getNumPics()<=1)
								item.setNumPics(1);
						}

					}
					if(i!=2){
						start=html.indexOf("<label for=\"endDate",end);
						end=html.indexOf("</label>",start);
						start=html.lastIndexOf("\">",end)+2;
						item.setEndDate(DateParser.stringToDate(html.substring(start,end)));
					}

					item.calcItemRating();
					if (i==0&&html.indexOf("No Current Bids")==-1){
						client.addToCurrentBids(itemID,item);
						client.addToItemsOfInterest(itemID, item);
					}
					else if(i==1&&html.indexOf("No Old Bids")==-1){
						client.addToEndedAuctions(itemID,item);
						client.removeItemOfInterest(itemID);
					}
					else if(i==2&&html.indexOf("No Purchases")==-1){
						client.addToPurchasedItems(itemID,item);
						client.removeItemOfInterest(itemID);
					}
					else if(i==3&&html.indexOf("Not Currently Selling")==-1){
						client.addToSellingItems(itemID,item);
					}
					else if(i==4&&html.indexOf("No Previously Sold Items")==-1){
						client.addToSoldItems(itemID,item);
					}
					itemEnd=html.indexOf("</a>",start);
					//	start=html.indexOf("\">",start)+("\">").length();
				}
			}
		}
		else{
			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = mapper.readValue(html.toString(), JsonNode.class);
			long itemID;
			String itemHeading=null;
			String bidItemHeading=null;
			for (int j=0;j<=4;j++){
				if (j==0)
					itemHeading="newbids";
				else if(j==1)
					itemHeading="oldbids";
				else if(j==2)
					itemHeading="purchases";
				else if(j==3)
					itemHeading="newitems";
				else if(j==4)
					itemHeading="olditems";

				if(j<=1)
					bidItemHeading="bidItem";
				else
					bidItemHeading="purchaseItem";

				for (int i=0;i<node.get(itemHeading).size();i++){
					ItemCG item=new ItemCG(client);
					if(j<=2)
						itemID=node.get(itemHeading).get(i).get(bidItemHeading).get("id").getLongValue();
					else
						itemID=node.get(itemHeading).get(i).get("id").getLongValue();
					if(client.getItemsOfInterest().containsKey(itemID))
						item=client.getItemsOfInterest().get(itemID);
					item.setId(itemID);

					item.setMyAccount(true);
					item.setTs(new Date().getTime());
					if(i<=2)
						item.setBidder(true);
					if(i>=2)
						item.setOwner(true);
					if(j<=2){
						item.setName(node.get(itemHeading).get(i).get(bidItemHeading).get("name").getTextValue());
						item.setEndDate(DateParser.stringToDate(node.get(itemHeading).get(i).get(bidItemHeading).get("endDate").getTextValue()));
						item.setCurrentBid(node.get(itemHeading).get(i).get(bidItemHeading).get("currentBid").getDoubleValue());
						if(i!=2)
							item.setMaxBid(node.get(itemHeading).get(i).get("maxBid").getDoubleValue());
						if(node.get(itemHeading).get(i).get(bidItemHeading).get("thumbnail").getValueAsText().contains("blank"))
							item.setNumPics(0);
						else{
							if(item.getNumPics()<=1)
								item.setNumPics(node.get(itemHeading).get(i).get(bidItemHeading).get("images").size());
						}

						item.calcItemRating();
						if (j==0){
							client.addToCurrentBids(itemID,item);
							client.addToItemsOfInterest(itemID,item);
						}
						else if(j==1){
							client.addToEndedAuctions(itemID,item);
							client.removeItemOfInterest(itemID);
						}


						item.setQuantity(node.get(itemHeading).get(i).get("quantity").getLongValue());
						item.setBidDate(DateParser.stringToDate(node.get(itemHeading).get(i).get("bidDate").getTextValue()));
						item.setDescription(node.get(itemHeading).get(i).get(bidItemHeading).get("description").getTextValue());
						item.setStartPrice(node.get(itemHeading).get(i).get(bidItemHeading).get("startPrice").getDoubleValue());
						item.setBuyNowPrice(node.get(itemHeading).get(i).get(bidItemHeading).get("buyNowPrice").getDoubleValue());
						if(item.getBuyNowPrice()==0)
							item.setForBuyNow(false);
						item.setNoOfBids(node.get(itemHeading).get(i).get(bidItemHeading).get("noOfBids").getLongValue());
						item.setStartDate(DateParser.stringToDate(node.get(itemHeading).get(i).get(bidItemHeading).get("startDate").getTextValue()));
						item.setSellerID(node.get(itemHeading).get(i).get(bidItemHeading).get("sellerID").getLongValue());

						if(j==2){
							client.addToPurchasedItems(itemID,item);
							client.removeItemOfInterest(itemID);
							if(client.getCurrentBids().containsKey(node.get(itemHeading).get(i).get("id")))
								client.getCurrentBids().remove(node.get(itemHeading).get(i).get("id"));
						}

						if(j==1||j==2){
							if(client.getCurrentBids().containsKey(itemID))
								client.getCurrentBids().remove(itemID);
						}

					}
					else{
						item.setName(node.get(itemHeading).get(i).get("name").getTextValue());
						item.setEndDate(DateParser.stringToDate(node.get(itemHeading).get(i).get("endDate").getTextValue()));
						item.setCurrentBid(node.get(itemHeading).get(i).get("currentBid").getDoubleValue());
						if(node.get(itemHeading).get(i).get("thumbnail").getTextValue().contains("blank"))
							item.setNumPics(0);
						else{
							if(item.getNumPics()<=1)
								item.setNumPics(node.get(itemHeading).get(i).get("images").size());
						}

						if(j==2){
							client.addToSellingItems(itemID,item);
						}
						else if(j==3){
							client.addToSoldItems(itemID,item);
							if(client.getSellingItems().containsKey(itemID))
								client.getSoldItems().remove(itemID);
						}

						item.setQuantity(node.get(itemHeading).get(i).get("quantity").getLongValue());
						item.setDescription(node.get(itemHeading).get(i).get("description").getValueAsText());
						item.setStartPrice(node.get(itemHeading).get(i).get("startPrice").getDoubleValue());
						item.setBuyNowPrice(node.get(itemHeading).get(i).get("buyNowPrice").getDoubleValue());
						if(item.getBuyNowPrice()==0)
							item.setForBuyNow(false);
						item.setNoOfBids(node.get(itemHeading).get(i).get("noOfBids").getLongValue());
						item.setStartDate(DateParser.stringToDate(node.get(itemHeading).get(i).get("startDate").getTextValue()));
						item.setSellerID(node.get(itemHeading).get(i).get("sellerID").getLongValue());

					}

					client.getClientInfo().addHTML5ItemCache(item);
				}

			}
		}
	}

	/**
	 * For HTML5 runs, loads the list of sellers returned in the JSOn response
	 */
	public void getSellers() throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readValue(html.toString(), JsonNode.class);
		if(html.indexOf("\"sellers\"")!=-1){
			for (int i=0;i<node.get("sellers").size();i++){
				SellerCG seller=new SellerCG(node.get("sellers").get(i));
				client.getClientInfo().addHTML5SellerCache(seller);
			}
		}
	}
	
	public void getQuestions() throws JsonParseException, JsonMappingException, IOException, ParseException{
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readValue(html.toString(), JsonNode.class);
		for (int i=0;i<node.get("questions").size();i++){
			QuestionCG question=new QuestionCG(node.get("question").get(i));
			client.getClientInfo().addHTML5QuestionCache(question);
		}
	}

	/**
	 * Updates the probabilities of which page to transition to
	 */
	private void updateProbabilities(){
		double probSum=1;

		double homeProb=RunSettings.getTransitionProb(pageType,0);
		double searchProb=RunSettings.getTransitionProb(pageType,1);
		double sellProb=RunSettings.getTransitionProb(pageType,2);
		double browseProb=RunSettings.getTransitionProb(pageType,3);
		double logOutProb=RunSettings.getTransitionProb(pageType,4);
		double updateProb=RunSettings.getTransitionProb(pageType,5);
		double checkCurrentBidProb=RunSettings.getTransitionProb(pageType,6);
		double checkPreviousBidProb=RunSettings.getTransitionProb(pageType,7);
		double checkPurchasesProb=RunSettings.getTransitionProb(pageType,8);
		double checkCurrentSaleProb=RunSettings.getTransitionProb(pageType,9);
		double checkPreviouslySoldProb=RunSettings.getTransitionProb(pageType,10);

		if(RunSettings.getWorkloadType()==1){
			searchProb*=1.2;
			sellProb*=0.65;
			browseProb*=1.15;
			logOutProb*=0.88;
			updateProb*=0.83;
			checkCurrentBidProb*=1.05;
		}
		else if(RunSettings.getWorkloadType()==3){
			searchProb*=0.8;
			sellProb*=1.35;
			browseProb*=0.85;
			logOutProb*=1.12;
			updateProb*=1.17;
			checkCurrentBidProb*=0.95;
		}

		double activeRatingSum=0;
		double ratingStandard=5;
		if (RunSettings.isMarkovTransitions()==false){
			homeProb*=(1.+(rand.nextDouble()-0.5)*.3);
			searchProb*=(1.+(rand.nextDouble()-0.5)*.3);
			sellProb*=(1.+(rand.nextDouble()-0.5)*.3);
			browseProb*=(1.+(rand.nextDouble()-0.5)*.3);
			logOutProb*=(1.+(rand.nextDouble()-0.5)*.3);
			updateProb*=(1.+(rand.nextDouble()-0.5)*.3);
			checkCurrentBidProb*=(1.+(rand.nextDouble()-0.5)*.3);
			checkPreviousBidProb*=(1.+(rand.nextDouble()-0.5)*.3);
			checkPurchasesProb*=(1.+(rand.nextDouble()-0.5)*.3);
			checkCurrentSaleProb*=(1.+(rand.nextDouble()-0.5)*.3);
			checkPreviouslySoldProb*=(1.+(rand.nextDouble()-0.5)*.3);

			if(html.indexOf("Update details")==-1||lastPageType==UPDATEUSER_PAGE_NUM)
				updateProb/=10;

			for (Entry<Long,ItemCG> e:client.getCurrentBids().entrySet()){
				activeRatingSum+=e.getValue().getItemRating();
			}
			checkCurrentBidProb*=(activeRatingSum/(double)client.getCurrentBids().size())/ratingStandard;
		}
		TreeMap<String,Double>allOptions=new TreeMap<String,Double>();

		if (RunSettings.isMarkovTransitions()==false){
			if (client.getMessage().equals("update")){
				client.setMessage("");
				updateProb=1;
				homeProb=0;
				searchProb=0;
				sellProb=0;
				browseProb=0;
				logOutProb=0;
				updateProb=0;
				checkCurrentBidProb=0;
				checkPreviousBidProb=0;
				checkPurchasesProb=0;
				checkCurrentSaleProb=0;
				checkPreviouslySoldProb=0;
			}

			if ((lastPageType==LOGIN_PAGE_NUM||lastPageType==REGISTER_PAGE_NUM)){
				logOutProb/=3;	
			}


			sellProb*=Math.exp(client.getExpSoldRate()*client.getItemsSold());
			browseProb*=Math.exp(client.getExpBuyRate()*(client.getItemsBid()+client.getItemsBought()));
			searchProb*=Math.exp(client.getExpBuyRate()*(client.getItemsBid()+client.getItemsBought()));
			logOutProb*=Math.exp(client.getExpSoldRate()*client.getItemsSold()+client.getExpBuyRate()*(client.getItemsBid()+client.getItemsBought()));

			client.setOrigLogoutProb(logOutProb);
			logOutProb=adjustLogOutProb(logOutProb);
			client.setFinalLogoutProb(logOutProb);
		}

		allOptions.put((HOME_TEXT),homeProb);
		allOptions.put((SEARCH_TEXT),searchProb);
		allOptions.put((SELL_TEXT),sellProb);
		allOptions.put((BROWSE_TEXT),browseProb);
		allOptions.put((LOGOUT_TEXT),logOutProb);
		allOptions.put((UPDATE_DETAILS_TEXT),updateProb);


		if (client.getCurrentBids().isEmpty())
			checkCurrentBidProb=0;
		else
			for(Entry<Long,ItemCG> e:client.getCurrentBids().entrySet()){
				if (RunSettings.isMarkovTransitions()==false)
					allOptions.put(new StringBuilder("currentBids").append(e.getKey()).toString(), checkCurrentBidProb*e.getValue().getItemRating()/activeRatingSum);
				else
					allOptions.put(new StringBuilder("currentBids").append(e.getKey()).toString(),checkCurrentBidProb/((double)client.getCurrentBids().size()));
			}
		if (client.getEndedAuctions().isEmpty())
			checkPreviousBidProb=0;
		else
			for (Long m:client.getEndedAuctions().keySet()){
				allOptions.put(new StringBuilder("previousBids").append(m).toString(), checkPreviousBidProb/((double)client.getEndedAuctions().size()));
			}
		if (client.getPurchasedItems().isEmpty())
			checkPurchasesProb=0;
		else
			for (Long m:client.getPurchasedItems().keySet()){
				allOptions.put(new StringBuilder("purchases").append(m).toString(), checkPurchasesProb/((double)client.getPurchasedItems().size()));
			}
		if (client.getSellingItems().isEmpty())
			checkCurrentSaleProb=0;
		else
			for (Long m:client.getSellingItems().keySet()){
				allOptions.put(new StringBuilder("currentlySelling").append(m).toString(), checkCurrentSaleProb/((double)client.getSellingItems().size()));
			}
		if (client.getSoldItems().isEmpty())
			checkPreviouslySoldProb=0;
		else
			for (Long m:client.getSoldItems().keySet()){
				allOptions.put(new StringBuilder("previouslySold").append(m).toString(), checkPreviouslySoldProb/((double)client.getSoldItems().size()));
			}

		double actualProbSum=0;
		for(Entry<String,Double> e:allOptions.entrySet()){
			actualProbSum+=e.getValue();
		}
		client.setRestProb(actualProbSum-logOutProb);

		for (Entry<String, Double> e:allOptions.entrySet()){
			nextPageProbabilities.put(probSum, new StringBuilder(e.getKey()));
			probSum-=(e.getValue()/actualProbSum);
		}
		if(verbose)System.out.println(nextPageProbabilities);

	}

	/**
	 * Determines the think time before going to the next page
	 * Deducts from the think time the amount of time since the 
	 * page opened until this calculation is performed
	 * @return Think time in ms
	 */
	private int getThinkTime(){
		int thinkTime=(int)expDist(initialThinkTime+10000);
		if(RunSettings.isRepeatedRun()){
			thinkTime=Integer.parseInt(((Element)action).getElementsByTagName("thinkTime").item(0).getTextContent());
		}
		if (verbose)System.out.println("User: "+client.getClientInfo().getUsername()+" - Think Time: "+thinkTime+" ms");
		if (RunSettings.isOutputThinkTimes()==true)
			cg.getThinkTimeHist().add(thinkTime);
		pageThinkTime=thinkTime;
		return Math.max((int) ((thinkTime-(new Date().getTime()-pageOpenTime))/RunSettings.getThinkTimeSpeedUpFactor()),0);
	}

}
