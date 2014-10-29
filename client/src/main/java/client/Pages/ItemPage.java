package client.Pages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import client.Items.*;
import client.clientMain.*;

/**
 * Item Page
 * @author Andrew Fox
 *
 */

public class ItemPage extends Page{
	ItemCG item=new ItemCG(client);		// the item on the current page
	boolean reservePriceMet=true;		// if the reserve price for the item has been met
	HashMap<String, StringBuilder> bidData=new HashMap<String,StringBuilder>();				// data for the POST if a bid is made
	HashMap<String, StringBuilder> buyNowData=new HashMap<String, StringBuilder>();			// data for the POST if a buyNow is made
	TreeMap<Double,StringBuilder> nextPageProbabilities=new TreeMap<Double,StringBuilder>();	// page transition probabilities
	long itemID;									// id of item on page
	double origBidPrice=0;							// the current bid price of the item when the page is first loaded
	StringBuilder sellerName=new StringBuilder();		// name of the seller of the item
	StringBuilder nextLink=new StringBuilder();		// next action to be made
	boolean tabbed=false;							// if the item page was opened in a tab
	Timer timer;									// timer for the AJAX requests
	long ajaxRequestPeriod=5000;					// time period of the AJAX requests to update the bid price

	public ItemPage(Page page) throws ParseException, JsonParseException, JsonMappingException, IOException{
		super(page.url,page.html,page.client,page.pageType,page.pageOpenTime,page.lastPageType,page.lastURL,page.cg);
		//TODO: What is this?
		if(lastURL.indexOf("/askquestion?")!=-1){
			this.url=new StringBuilder(client.getCMARTurl().getAppURL()).append("/viewitem").append(lastURL.substring(lastURL.indexOf("?")));
		}
		if((url.indexOf("202 The bid is less than the current bid")!=-1||url.indexOf("201 The max bid is less than the bid")!=-1)&&HTML4){
			int start=html.indexOf("Bidding History</td><td><a href=\"./bidhistory")+"Bidding History</td><td><a href=\"./bidhistory".length();
			int end=html.indexOf("\">",start);
			this.url=new StringBuilder(client.getCMARTurl().getAppURL()).append("/viewitem").append(html.subSequence(start, end));
		}

		timer=new Timer();		// starts AJAX requests if the item is still for sale/valid
		if(url.indexOf("old=1")==-1&&html.indexOf("The itemID is not for a valid item You can only bid for items that are currently in the database")==-1&&html.indexOf("\"item\":\"null\"")==-1){
			AJAXRequest ar=new AJAXRequest();
			timer.scheduleAtFixedRate(ar, 0, ajaxRequestPeriod);
		}

		/**
		 * Gets the item details and form data
		 */
		if(HTML4){
			if (html.indexOf("<label for=\"name\">")!=-1)
				getItemDetails();
			if(html.indexOf("action=\"viewitem\"")!=-1){
				bidData=getFormData("viewitem");
				item.setForAuction(true);
			}
			else
				item.setForAuction(false);
			if(html.indexOf("action=\"buyitem\"")!=-1){
				buyNowData=getFormData("buyitem");
				item.setForBuyNow(true);
			}
			else
				item.setForBuyNow(false);

			searchData=getFormData("search");
		}
		else{// if HTML5
			getItemDetails();
		}
	}

	/**
	 * Gets the details of the item:
	 * Item Name, Number of Pictures, Item Description, Seller Name, 
	 * Current Price, Buy Now Price, Number of Bids
	 */
	private void getItemDetails() throws ParseException{
		if(HTML4==true){
			int start=html.indexOf("&itemID=")+"&itemID=".length();
			int end=html.indexOf("\">",start);
			itemID=Long.parseLong(html.substring(start,end));
			if(client.getItemsOfInterest().containsKey(itemID))
				item=client.getItemsOfInterest().get(itemID);
			item.setId(itemID);
			if(html.indexOf("action=\"viewitem\"")!=-1)
				item.setForAuction(true);
			else
				item.setForAuction(false);
			if(html.indexOf("action=\"buyitem\"")!=-1)
				item.setForBuyNow(true);
			else
				item.setForBuyNow(false);
			item.setNumPics(0);		// reset number of pictures
			start=html.indexOf("<label for=\"name\">")+("<label for=\"name\">").length();
			end=html.indexOf("</label>",start);
			item.setName(html.substring(start,end));
			if(html.indexOf("Seller</td><td>Not present")==-1){
				start=html.indexOf("<label for=\"seller\">",end)+"<label for=\"seller\">".length();
				end=html.indexOf("</a>",start);
				start=html.lastIndexOf("\">",end)+"\">".length();
				sellerName=new StringBuilder(html.subSequence(start,end));
				start=html.indexOf("Seller rating",end);
				start=html.indexOf("<td>",start)+4;
				end=html.indexOf("</label>",start);
				double sellerRating=Double.parseDouble(html.substring(start, end));
				item.setSellerRating(sellerRating);
				if (sellerName.equals(client.getClientInfo().getUsername())){
					item.setOwner(true);
				}
			}
			if (html.indexOf("The bidding on this item has finished.<BR />")==-1&&item.isOwner()==false){
				start=html.indexOf("Bidding Ends",end)+("Bidding Ends").length();
				start=html.indexOf("<td>",start)+4;
				end=html.indexOf("</label>",start);
				item.setEndDate(stringToDate(html.substring(start,end)));
				if (start!=13)
					item.setForAuction(true);
				start=html.indexOf("Bidding History",start);
				start=html.indexOf("\">",start)+"\">".length();
				end=html.indexOf(" bid",start);
				item.setNoOfBids(Long.parseLong(html.substring(start,end)));
				start=html.indexOf("<span id=\"itemCurrentPrice1\">$",end)+"<span id=\"itemCurrentPrice1\">$".length();
				end=html.indexOf("</span>",start);
				item.setCurrentBid(Double.parseDouble(html.substring(start,end).replace(",", "")));
				origBidPrice=item.getCurrentBid();
				if(item.getNoOfBids()>1&&item.isForAuction())
					item.setStartPrice(item.getCurrentBid());
				start=html.indexOf("The reserve ",end)+("The reserve ").length();
				end=html.indexOf("been met",start);
				if (html.substring(start,end).equals("has"))
					reservePriceMet=true;
				else
					reservePriceMet=false;
				start=html.indexOf("Quantity",end);
				start=html.indexOf("<td>(",start)+ "<td>(".length();
				end=html.indexOf(" available)",start);
				item.setQuantity(Long.parseLong(html.substring(start,end)));
				if(item.isForBuyNow()==true){
					end=html.indexOf(" <input type=\"submit\" class=\"nice small radius white button\" value=\"Buy Now\">",end);
					start=html.lastIndexOf("$",end)+1;
					item.setBuyNowPrice(Double.parseDouble(html.substring(start,end).replace(",", "")));
				}
				start=html.indexOf("<label for=\"description\">",end)+("<label for=\"description\">").length();
				end=html.indexOf("<BR />",start);
				item.setDescription(html.substring(start,end));
				for (String s:searchTermWords){
					if (item.getName().indexOf(s)!=-1)
						item.incCommonSearchTerms();
					else if (item.getDescription().indexOf(s)!=-1)
						item.incCommonSearchTerms();
				}
				item.resetNumQuestions();
				start=html.indexOf("Question from user: ",start)+"Question from user: ".length();
				while(start!=("Question from user: ".length()-1)){
					item.incQuestionNum();
					start=html.indexOf("Question from user: ",start)+"Question from user: ".length();
				}


			}
			start=html.indexOf("<div class=\"panel\">",end)+"<div class=\"panel\">".length();
			start=html.indexOf("<div class=\"panel\">",start)+"<div class=\"panel\">".length();
			while(start!="<div class=\"panel\">".length()-1){
				item.setNumPics(item.getNumPics()+1);
				start=html.indexOf("<div class=\"panel\">",start)+"<div class=\"panel\">".length();
			}
			item.calcItemRating();
			client.addToItemsOfInterest(itemID, item);
		}
		else{
			if(html.substring(0,4).equals("ITEM")){
				itemID=Long.parseLong(html.substring(4));
				item=client.getClientInfo().getHTML5ItemCache().get(itemID);
				item.setNumPics(item.getImages().size());
				item.setSellerRatingHTML5();
				item.resetNumQuestions();
				for(Entry<Long,QuestionCG> e:client.getClientInfo().getHTML5QuestionCache().entrySet()){
					if(e.getValue().getItemID()==itemID&&e.getValue().isQuestion())
						item.incQuestionNum();
				}
			}
			else{
				try {
					ObjectMapper mapper = new ObjectMapper();
					JsonNode node = mapper.readValue(html.toString(), JsonNode.class);

					item=new ItemCG(client);
					if(html.indexOf("\"item\":\"null\"")==-1){
						itemID=node.get("itemID").getLongValue();
						if(client.getItemsOfInterest().containsKey(itemID))
							item=client.getItemsOfInterest().get(itemID);
						item.setId(itemID);

						item.setName(node.get("item").get("name").getTextValue());
						item.setEndDate(stringToDate(node.get("item").get("endDate").getTextValue()));
						item.setCurrentBid(node.get("item").get("currentBid").getDoubleValue());
						origBidPrice=item.getCurrentBid();
						item.setSellerID(node.get("item").get("sellerID").getLongValue());
						item.setDescription(node.get("item").get("description").getTextValue());
						item.setQuantity(node.get("item").get("quantity").getLongValue());
						item.setStartPrice(node.get("item").get("startPrice").getDoubleValue());
						item.setBuyNowPrice(node.get("item").get("buyNowPrice").getDoubleValue());
						if(item.getBuyNowPrice()==0)
							item.setForBuyNow(false);
						item.setNoOfBids(node.get("item").get("noOfBids").getLongValue());
						item.setStartDate(stringToDate(node.get("item").get("startDate").getTextValue()));
						item.setNumPics(node.get("item").get("images").size());
						for(int k=0;k<item.getNumPics();k++){
							item.addImage(node.get("item").get("images").get(k).get("url").getTextValue());
						}

						SellerCG seller=new SellerCG();
						seller.setId(node.get("seller").get("id").getLongValue());
						seller.setName(node.get("seller").get("name").getTextValue());
						seller.setRating(node.get("seller").get("rating").getLongValue());
						client.getClientInfo().addHTML5SellerCache(seller);

						item.resetNumQuestions();
						for (int i=0;i<node.get("questions").size();i++){
							QuestionCG question=new QuestionCG();
							question.setId(node.get("questions").get(i).get("id").getLongValue());
							question.setFromUserID(node.get("questions").get(i).get("fromUserID").getLongValue());
							question.setToUserID(node.get("questions").get(i).get("toUserID").getLongValue());
							question.setItemID(node.get("questions").get(i).get("itemID").getLongValue());
							question.setQuestion(node.get("questions").get(i).get("isQuestion").getBooleanValue());
							question.setPostDate(stringToDate(node.get("questions").get(i).get("postDate").getTextValue()));
							question.setResponseTo(node.get("questions").get(i).get("responseTo").getLongValue());
							question.setContent(node.get("questions").get(i).get("content").getTextValue());
							client.getClientInfo().addHTML5QuestionCache(question);
							if(question.isQuestion())
								item.incQuestionNum();
						}
					}
				} catch (JsonParseException e) {
					e.printStackTrace();
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				catch (NullPointerException e) {
					e.printStackTrace();
					System.err.println(html);
					System.err.println(url);
				}
			}

			// calculates the item rating and adds it to the cache and itemsOfInterest
			item.calcItemRating();
			client.addToItemsOfInterest(itemID, item);
			client.getClientInfo().addHTML5ItemCache(item);
		}
	}

	/**
	 * Sets if the page is a tab
	 * @param tabbed
	 */
	public void setTabbed(boolean tabbed){
		this.tabbed=tabbed;
	}
	/**
	 * Returns if this page is a tab
	 * @return
	 */
	public boolean isTabbed(){
		return this.tabbed;
	}

	/**
	 * Determines the next link to go to in the application
	 * @return the URL or HTML of the next link, depending on whether
	 * 		   the previous entry contained a form or not
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws UnsupportedEncodingException 
	 * @throws ParseException 
	 */
	public StringBuilder makeDecision() throws UnsupportedEncodingException, IOException, InterruptedException, ParseException{

		client.setLastItemID(itemID);
		StringBuilder nextPage=new StringBuilder(client.getCMARTurl().getAppURL());

		updateProbabilities();

		if(RunSettings.isRepeatedRun()==false){

			nextLink=new StringBuilder(getRandomStringBuilderFromDist(nextPageProbabilities));
			String nextLinkS=nextLink.toString();
			if(verbose)System.out.println("Next Link: "+nextLink);

			action=xmlDocument.createElement("action");
			action.setAttribute("id",client.getActionNum());
			Element currentPage=xmlDocument.createElement("currentPage");
			currentPage.setTextContent(Integer.toString(pageType));
			action.appendChild(currentPage);
			Element nextPagee=xmlDocument.createElement("nextPage");
			nextPagee.setTextContent(nextLinkS);
			action.appendChild(nextPagee);
			request=xmlDocument.createElement("request");

			// Think Time
			try{Thread.sleep(getThinkTime());}
			catch(InterruptedException e){
				client.setExit(true);
				timer.cancel();
				return null;
			}


			if (nextLinkS.equals("bid")){
				if(item.getCurrentBid()!=origBidPrice){
					origBidPrice=item.getCurrentBid();
					return makeDecision();
				}

				bidData.put("quantity", new StringBuilder("1"));
				bidData.put("bid", new StringBuilder(Double.toString(((double)Math.ceil((item.getCurrentBid()*1.1+0.01)/100))/100)));
				if(rand.nextDouble()>0.5)
					bidData.put("maxBid", new StringBuilder(Double.toString(((double)Math.ceil((item.getCurrentBid()*1.1+0.01)/100))/100)));
				else
					bidData.put("maxBid", new StringBuilder(Double.toString(((double)Math.ceil((item.getCurrentBid()*1.15+0.01)/100))/100)));
				if(HTML4==false){
					bidData.put("useHTML5",new StringBuilder("1"));
					bidData.put("userID", client.getClientInfo().getHTML5Cache().get("userID"));
					bidData.put("authToken", client.getClientInfo().getHTML5Cache().get("authToken"));
					bidData.put("itemID", new StringBuilder(Long.toString(itemID)));
				}

				Element child=xmlDocument.createElement("url");
				child.setTextContent(new StringBuilder(nextPage).append("/viewitem").toString());
				request.appendChild(child);
				for (Entry<String,StringBuilder> e:bidData.entrySet()){
					child=xmlDocument.createElement("data");
					child.setAttribute("name", e.getKey());
					child.setTextContent(e.getValue().toString());
					request.appendChild(child);
				}
				if (HTML4){
					child=xmlDocument.createElement("type");
					child.setTextContent("POST");
					request.appendChild(child);
					nextPage=doSubmit(nextPage.append("/viewitem"),bidData);
					if(nextPage.indexOf("<TITLE>View Item</TITLE>")==-1){
						item.setBidder(true);
						item.calcItemRating();
						client.addToCurrentBids(itemID,item);
						client.addToItemsOfInterest(itemID, item);
					}

				}
				else{
					child=xmlDocument.createElement("type");
					child.setTextContent("GET");
					request.appendChild(child);
					nextPage=openHTML5PageWithRedirect(nextPage.append("/viewitem?").append(createURL(bidData)));
				}

			}
			else if(nextLinkS.equals("buyNow")){
				if(HTML4){
					Element child=xmlDocument.createElement("url");
					child.setTextContent(new StringBuilder(nextPage).append("/buyitem").toString());
					request.appendChild(child);
					for (Entry<String,StringBuilder> e:buyNowData.entrySet()){
						child=xmlDocument.createElement("data");
						child.setAttribute("name", e.getKey());
						child.setTextContent(e.getValue().toString());
						request.appendChild(child);
					}
					child=xmlDocument.createElement("type");
					child.setTextContent("POST");
					request.appendChild(child);
					nextPage=doSubmit(nextPage.append("/buyitem"),buyNowData);
				}else{
					Element child=xmlDocument.createElement("url");
					child.setTextContent(new StringBuilder(nextPage).append("/buyitem.html?itemID=").append(itemID).toString());
					request.appendChild(child);
					child=xmlDocument.createElement("type");
					child.setTextContent("GET");
					request.appendChild(child);
					//				buyNowData.put("useHTML5",new StringBuilder("1"));
					//				buyNowData.put("userID", client.getClientInfo().getHTML5Cache().get("userID"));
					//				buyNowData.put("authToken", client.getClientInfo().getHTML5Cache().get("authToken"));
					//				buyNowData.put("itemID", new StringBuilder(Long.toString(itemID)));
					//
					//				nextPage=openHTML5PageWithRedirect(nextPage.append("/buyitem?").append(createURL(buyNowData)));
					nextPage.append("/buyitem.html?itemID=").append(itemID);
				}
			}
			else if (nextLinkS.equals("back")){
				nextPage=lastURL;
			}
			else if(nextLinkS.equals("Refresh")){
				nextPage=url;
			}
			else if(nextLinkS.equals(ASK_QUESTION_TEXT)||nextLinkS.equals(LEAVE_COMMENT_TEXT)){
				int end=html.indexOf(nextLinkS);
				int start=html.lastIndexOf("href=\".",end)+"href=\".".length();
				end=html.indexOf("\">",start);
				nextPage.append(html.subSequence(start,end));			
			}
			else{
				if(HTML4){
					if (tabbed==true){
						Element child=xmlDocument.createElement("type");
						child.setTextContent("CLOSE_TAB");
						request.appendChild(child);
						child=xmlDocument.createElement("url");
						child.setTextContent("CLOSE_TAB");
						request.appendChild(child);
						child=xmlDocument.createElement("thinkTime");
						child.setTextContent(Integer.toString(pageThinkTime));
						action.appendChild(child);
						action.appendChild(request);
						client.addXMLAction(action);

						timer.cancel();
						return new StringBuilder(CLOSE_TAB);
					}
					else if (nextLinkS.equals(SEARCH_TEXT)){
						timer.cancel();
						return search(searchData,action);
					}
					else if(nextLinkS.equals("bidHistory")){
						int start=html.indexOf("Bidding History: <a href=\"")+"Bidding History: <a href=\"".length();
						int end=html.indexOf("\">",start);
						nextPage.append(html.subSequence(start,end));
					}
					else{
						int end=html.indexOf("\"/>"+nextLink);
						int start=html.lastIndexOf("href=\".",end)+"href=\".".length();
						end=html.indexOf("\">",start);
						nextPage.append(html.subSequence(start,end));
					}
				}
				else{
					if (tabbed==true){
						Element child=xmlDocument.createElement("type");
						child.setTextContent("CLOSE_TAB");
						request.appendChild(child);
						child=xmlDocument.createElement("url");
						child.setTextContent("CLOSE_TAB");
						request.appendChild(child);
						child=xmlDocument.createElement("thinkTime");
						child.setTextContent(Integer.toString(pageThinkTime));
						action.appendChild(child);
						action.appendChild(request);
						client.addXMLAction(action);

						timer.cancel();
						return new StringBuilder(CLOSE_TAB);
					}
					else if(nextLinkS.equals(MY_ACCOUNT_TEXT)){
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
						timer.cancel();
						return search(searchData,action);
					}
				}

			}

			if(!nextLinkS.equals("bid")&& !nextLinkS.equals("buyNow")){
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
			HashMap<String, StringBuilder> data=new HashMap<String,StringBuilder>();
			//action=(Element)((Element)client.getReadXmlDocument().getElementsByTagName("action")).getAttributeNode(client.getActionNum());
			findAction();
			if(pageType!=Integer.parseInt(action.getElementsByTagName("currentPage").item(0).getTextContent())){
				client.setExit(true);
				client.setExitDueToRepeatChange(true);
				timer.cancel();
				return null;
			}
			String nextLinkS=(action.getElementsByTagName("nextPage").item(0).getTextContent());
			//StringBuilder nextLink=new StringBuilder(nextLinkS);
			request=(Element)((Element)action).getElementsByTagName("request").item(0);
			StringBuilder nextURL=new StringBuilder(request.getElementsByTagName("url").item(0).getTextContent());

			if(!nextPageProbabilities.containsValue(nextLinkS)){
				client.setExit(true);
				client.setExitDueToRepeatChange(true);
				threadExecutor.shutdownNow();
				return null;
			}

			String reqType=request.getElementsByTagName("type").item(0).getTextContent();

			NodeList dataList=request.getElementsByTagName("data");
			for(int i=0;i<dataList.getLength();i++){
				Node n=dataList.item(i);
				String key=n.getAttributes().item(0).getTextContent();
				StringBuilder value=new StringBuilder(((Element)n).getTextContent());
				data.put(key, value);
			}
			if(data.containsKey("authToken"))
				data.put("authToken",client.getClientInfo().getAuthToken());
			if(data.containsKey("itemID")){
				if(itemID!=Long.parseLong(data.get("itemID").toString())){
					data.put("itemID", new StringBuilder(Long.toString(itemID)));
					client.setChangeDueToRepeatChange(true);
				}
			}
			if(nextLinkS.equals("bid")){
				if(item.getCurrentBid()>=Double.parseDouble(data.get("bid").toString())){
					data.put("bid", new StringBuilder(Double.toString(((double)Math.ceil((item.getCurrentBid()*1.1+0.01)/100))/100)));	
					client.setChangeDueToRepeatChange(true);
				}
				if(item.getCurrentBid()>=Double.parseDouble(data.get("maxBid").toString())){
					data.put("maxBid", data.get("bid"));	
					client.setChangeDueToRepeatChange(true);
				}
			}
			if(nextURL.indexOf("itemID=")!=-1){
				int start=nextURL.indexOf("&itemID=")+"&itemID=".length();
				String urlItemID;
				if(nextURL.indexOf("&",start)==-1)
					urlItemID=(nextURL.substring(start));
				else
					urlItemID=(nextURL.substring(start,nextURL.indexOf("&",start)));
				if(itemID!=Long.parseLong(urlItemID)){
					nextURL.replace(start, start+urlItemID.length(), Long.toString(itemID));
					client.setChangeDueToRepeatChange(true);
				}
			}

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

			if(nextLinkS.equals(SEARCH_TEXT)){
				return search(data,action);
			}

			// Think Time
			try{Thread.sleep(getThinkTime());}
			catch(InterruptedException e){
				client.setExit(true);
				timer.cancel();
				return null;
			}

			if(nextLinkS.equals("bid")||nextLinkS.equals("buyNow")){
				if (HTML4){
					nextPage=doSubmit(nextURL,data);
				}
				else{
					nextPage=openHTML5PageWithRedirect(nextURL.append("?").append(createURL(data)));
				}
			}else if(reqType.equals("CLOSE_TAB")){
				nextPage=new StringBuilder(CLOSE_TAB);
			}else{
				nextPage=nextURL;
			}


		}

		timer.cancel();
		return nextPage;

	}



	/**
	 * Updates the probabilities of which page to transition to
	 */
	private void updateProbabilities(){
		double probSum=1;

		double bidProb=RunSettings.getTransitionProb(pageType,0);
		double buyNowProb=RunSettings.getTransitionProb(pageType,1);
		double homeProb=RunSettings.getTransitionProb(pageType,2);
		double browseProb=RunSettings.getTransitionProb(pageType,3);
		double searchProb=RunSettings.getTransitionProb(pageType,4);
		double sellProb=RunSettings.getTransitionProb(pageType,5);
		double myAccountProb=RunSettings.getTransitionProb(pageType,6);
		double logOutProb=RunSettings.getTransitionProb(pageType,7);
		double bidHistoryProb=RunSettings.getTransitionProb(pageType,8);
		double refreshPageProb=RunSettings.getTransitionProb(pageType,9);
		double askQuestionProb=RunSettings.getTransitionProb(pageType,10);
		double leaveCommentProb=RunSettings.getTransitionProb(pageType,11);

		if(RunSettings.getWorkloadType()==1){
			bidProb*=0.6;
			buyNowProb*=0.6;
			browseProb*=1.35;
			searchProb*=1.35;
			sellProb*=0.65;
			myAccountProb*=1.15;
			askQuestionProb*=0.65;
		}
		else if(RunSettings.getWorkloadType()==3){
			bidProb*=1.4;
			buyNowProb*=1.4;
			browseProb*=0.65;
			searchProb*=0.65;
			sellProb*=1.35;
			myAccountProb*=0.85;
			askQuestionProb*=1.35;
		}
		if(!HTML4){
			askQuestionProb=0.;
			leaveCommentProb=0.;
		}

		if(html.indexOf("The itemID is not for a valid item You can only bid for items that are currently in the database")!=-1||html.indexOf("The item is not present. Perhaps reload the page, or the itemID is incorrect")!=-1||html.indexOf("\"item\":\"null\"")!=-1){
			bidProb=0;
			buyNowProb=0;
			bidHistoryProb=0;
			refreshPageProb=0.0;
			askQuestionProb=0.0;
			leaveCommentProb=0.00;
		}

		if (RunSettings.isMarkovTransitions()==false){
			bidProb*=(1.+(rand.nextDouble()-0.5)*.3);
			buyNowProb*=(1.+(rand.nextDouble()-0.5)*.3);
			homeProb*=(1.+(rand.nextDouble()-0.5)*.3);
			browseProb*=(1.+(rand.nextDouble()-0.5)*.3);
			searchProb*=(1.+(rand.nextDouble()-0.5)*.3);
			sellProb*=(1.+(rand.nextDouble()-0.5)*.3);
			myAccountProb*=(1.+(rand.nextDouble()-0.5)*.3);
			logOutProb*=(1.+(rand.nextDouble()-0.5)*.3);
			bidHistoryProb*=(1.+(rand.nextDouble()-0.5)*.3);
			refreshPageProb*=(1.+(rand.nextDouble()-0.5)*.3);
			askQuestionProb*=(1.+(rand.nextDouble()-0.5)*.3);
			leaveCommentProb*=(1.+(rand.nextDouble()-0.5)*.3);

			if(item.getEndDate()==null){
				refreshPageProb=0;
				askQuestionProb=0;
			}
			else if(item.getEndDate().getTime()<new Date().getTime()){
				refreshPageProb=0;
				bidProb=0;
				buyNowProb=0;		
				askQuestionProb=0;
			}
			else{
				long timeToEnd=item.getEndDate().getTime()-new Date().getTime();
				refreshPageProb*=(Math.exp(1.5*(3600000.-(double)timeToEnd)/3600000.)+1);
			}

			if((client.getEndedAuctions().containsKey(item.getId())||client.getPurchasedItems().containsKey(item.getId()))&&html.indexOf("Comment from user: "+client.getClientInfo().getUsername())==-1){
				leaveCommentProb*=20;	
			}
			else
				leaveCommentProb=0;
		}
		TreeMap<String,Double>allOptions=new TreeMap<String,Double>();

		if(verbose)System.out.println("Item Rating: "+item.getItemRating()+" Item Name: "+item.getName());

		if (RunSettings.isMarkovTransitions()==false){
			if(bidProb!=0)
				bidProb*=item.getItemRating()/item.getWantRating();
			if(buyNowProb!=0)
				buyNowProb*=item.getItemRating()/item.getWantRating();
			askQuestionProb*=item.getItemRating()/item.getWantRating();
			if(item.getNumQuestions()>1)
				askQuestionProb/=item.getNumQuestions();
			if(lastPageType==ASKQUESTION_PAGE_NUM){
				askQuestionProb/=4;
				bidProb/=5;
				buyNowProb/=5;
			}
		}
		if (item.isForAuction()==false)
			bidProb=0;
		if (item.isForBuyNow()==false)
			buyNowProb=0;
		if(item.isForAuction()&&item.isForBuyNow()&&RunSettings.isMarkovTransitions()==false)
			if(item.getBuyNowPrice()<item.getCurrentBid()){
				buyNowProb+=bidProb;
				bidProb=0;
			}
		if (RunSettings.isMarkovTransitions()==false){
			if (item.isOwner()==true){
				bidProb=0;
				buyNowProb=0;
			}

			client.setOrigLogoutProb(logOutProb);
			logOutProb=adjustLogOutProb(logOutProb);
			client.setFinalLogoutProb(logOutProb);
		}

		allOptions.put(("bid"),bidProb);
		allOptions.put(("buyNow"), buyNowProb);
		allOptions.put((HOME_TEXT), homeProb);
		allOptions.put((MY_ACCOUNT_TEXT),myAccountProb);
		allOptions.put((SELL_TEXT),sellProb);
		allOptions.put((BROWSE_TEXT),browseProb);
		allOptions.put((SEARCH_TEXT),searchProb);
		allOptions.put((LOGOUT_TEXT), logOutProb);
		allOptions.put(("bidHistory"), bidHistoryProb);
		allOptions.put(("Refresh"), refreshPageProb);
		allOptions.put((ASK_QUESTION_TEXT),askQuestionProb);
		allOptions.put((LEAVE_COMMENT_TEXT),leaveCommentProb);


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
	 * Creates the AJAX requests for updating the bid price
	 * @author afox1
	 *
	 */
	private class AJAXRequest extends TimerTask{

		private AJAXRequest(){
		}

		public void run(){
			if(html.indexOf("The bidding on this item has finished.")==-1&&url.indexOf("old=1")==-1){
				StringBuilder AJAXurl=new StringBuilder(url).append("&itemCurrentBid=1");
				if(!HTML4&&url.charAt(0)=='{'){
					try {
						ObjectMapper mapper = new ObjectMapper();
						JsonNode node = mapper.readValue(html.toString(), JsonNode.class);
						long id=node.get("itemID").getLongValue();
						AJAXurl=new StringBuilder(client.getCMARTurl().getAppURL()).append("/viewitem?itemID=").append(id).append("&itemCurrentBid=1");
					} catch (JsonParseException e) {
						e.printStackTrace();
					} catch (JsonMappingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}						
				}

				//TODO: replace this - i think it comes up when there is a bad bid made
				if(AJAXurl.indexOf("</HTML>")==-1){
					StringBuilder itemPrice=openAJAXRequest(AJAXurl);

					if(itemPrice!=null&&itemPrice.length()>0){
						if(itemPrice.charAt(0)=='$'){
							itemPrice.deleteCharAt(0);
							double price=Double.parseDouble(itemPrice.toString().replace(",",""));
							if(price!=item.getCurrentBid()){
								synchronized(item){
									if(price!=item.getCurrentBid()){
										item.setCurrentBid(price);
										item.calcItemRating();
										client.addToItemsOfInterest(item.getCategoryID(), item);
									}
								}
							}
						}
						else if(itemPrice.indexOf("cannot get current bid now")!=-1){	// if there is an error returning the bid price of the item
							synchronized(item){
								item.setForAuction(false);
								item.setForBuyNow(false);
								item.setCurrentBid(item.getCurrentBid()+1);
							}
						}
					}
					else{		// if there is an error on returning the bid price then cancel the AJAX requests
						this.cancel();
					}
				}
				else
					this.cancel();
			}
		}
	}

	/**
	 * Cancels the AJAX requests
	 * Called when closing tabs
	 */
	public void cancelTimer(){
		timer.cancel();
	}

	/**
	 * Determines the think time before going to the next page.
	 * Deducts from the think time the amount of time since the 
	 * page opened until this calculation is performed
	 * @return Think time in ms
	 */
	private int getThinkTime(){
		int thinkTime=(int)(initialThinkTime);
		if(RunSettings.isRepeatedRun()==false){
			thinkTime+=typingErrorThinkTime;

			String nextLinkS=nextLink.toString();
			if (nextLinkS.equals("bid")){
				thinkTime+=20000;
			}
			else if(nextLinkS.equals("buyNow")){
				thinkTime+=12000;
			}
			else{
				thinkTime+=5000;
			}

			if(item.isForAuction()||item.isForBuyNow()){
				thinkTime=(int)expDist(thinkTime);
				if(item.getDescription()!=null)
					thinkTime+=Math.ceil((item.getName().length()+item.getDescription().length())/(client.getTypingSpeed()*(4.+(rand.nextDouble()-0.5)/2)));
				else if(item.getName()!=null)
					thinkTime+=Math.ceil((item.getName().length())/(client.getTypingSpeed()*(4.+(rand.nextDouble()-0.5)/2)));
				thinkTime+=item.getNumPics()*expDist(4000);
			}
			else{
				thinkTime+=expDist(10000);
				thinkTime=(int)expDist(thinkTime);
			}

			if(nextLinkS.equals("bid")||nextLinkS.equals("buyNow")){
				while(new Date().getTime()+thinkTime>=item.getEndDate().getTime()){
					thinkTime/=1.5;	
				}				
			}
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