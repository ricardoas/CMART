package client.Pages;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import client.Items.ItemCG;
import client.Items.QuestionCG;
import client.Items.SellerCG;
import client.Tools.DateParser;
import client.clientMain.RunSettings;



/**
 * Search Page
 * @author Andrew Fox
 *
 */

public class SearchPage extends Page{
	TreeMap<Double,StringBuilder> nextPageProbabilities=new TreeMap<Double,StringBuilder>();
	TreeMap<Long,ItemCG>listedItems=new TreeMap<Long,ItemCG>();
	ExecutorService threadExecutor = Executors.newCachedThreadPool();
	ArrayList<Long>openTabs=new ArrayList<Long>();
	TreeMap<Double,Long>unopenedItemsProb=new TreeMap<Double,Long>();
	TreeMap<Long,Double>itemsProb=new TreeMap<Long,Double>();
	int completedTabs=0;
	int pageNum=0;
	double pageRTFactor=1.2;

	private static final String ITEMLINK_LABEL="<label for=\"itemlink";
	private static final String HREF_TEXT="href=\".";


	public SearchPage(Page page) throws ParseException, JsonParseException, JsonMappingException, IOException{
		super(page.url,page.html,page.client,page.pageType,page.pageOpenTime,page.lastPageType,page.lastURL);
		client.pageSpecificRT(pageRTFactor);	// change RT threshold depending on page type
		searchData=getFormData("search");
		getSearchTermWords();
		updateItemRatings();
		if(!HTML4){
			getSellers();
			getQuestions();
		}
		updateProbabilities();
	}

	/**
	 * Determines the next link to go to in the application
	 * @return the URL or HTML of the next link, depending on whether
	 * 		   the previous entry contained a form or not
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 * @throws ParseException 
	 * @throws InterruptedException 
	 * @throws URISyntaxException 
	 */
	@Override public StringBuilder makeDecision() throws JsonParseException, JsonMappingException, IOException, ParseException, InterruptedException, URISyntaxException{
		StringBuilder nextURL=new StringBuilder(client.getCMARTurl().getAppURL());
		StringBuilder nextLink=new StringBuilder(getRandomStringBuilderFromDist(nextPageProbabilities));
		String nextLinkS=nextLink.toString();
		boolean oneTab=false;
		if(verbose)System.out.println("Next Link: "+nextLink);

		if(RunSettings.isRepeatedRun()==false){
			action=xmlDocument.createElement("action");
			action.setAttribute("id",client.getActionNum());
			Element currentPage=xmlDocument.createElement("currentPage");
			currentPage.setTextContent(Integer.toString(pageType));
			action.appendChild(currentPage);
			Element nextPage=xmlDocument.createElement("nextPage");
			nextPage.setTextContent(nextLinkS);
			action.appendChild(nextPage);

			if (nextLinkS.equals(SEARCH_TEXT)){
				if(!HTML4){
					searchData.put("userID", new StringBuilder(client.getClientInfo().getHTML5Cache().get("userID")));
					searchData.put("authToken", new StringBuilder(client.getClientInfo().getHTML5Cache().get("authToken")));
					searchData.put("useHTML5", new StringBuilder("1"));
					searchData.put("pageNo", new StringBuilder("0"));
					searchData.put("itemsPP",new StringBuilder("25"));
					searchData.put("hasItems", getHasItems());
				}
				threadExecutor.shutdownNow();
				return search(searchData,action);
			}

			if(!nextLinkS.equals("tabs")){
				try{Thread.sleep(getThinkTime());}
				catch(InterruptedException e){
					client.setExit(true);
					threadExecutor.shutdownNow();
					return null;
				}
				Element child=xmlDocument.createElement("thinkTime");
				child.setTextContent(Integer.toString(pageThinkTime));
				action.appendChild(child);
			}

			int start=0;
			int end=0;

			if(nextLinkS.equals("tabs")){
				int numTabs=0;
				double numTabsMeanOrig=numTabsMean;
				if(RunSettings.isTabbedBrowsing()){
					numTabsMean+=((double)(client.getRTThreshold()-client.getRTavg()))/((double)client.getRTThreshold()/5);
					if(numTabsMean>2*numTabsMeanOrig)
						numTabsMean=2*numTabsMeanOrig;
					if (numTabsMean>0){
						while(numTabs<1||numTabs>listedItems.size())
							numTabs=(int)Math.round(expDist(numTabsMean));
					}
					else
						numTabs=0;
				}
				else{
					numTabs=1;
					oneTab=true;
				}
				if(verbose)System.out.println("Number of tabs to open = "+numTabs);
				if (numTabs!=1)
					openTabs(numTabs,action);
				else{
					long thinkTime=(long)expDist(8000);
					try{Thread.sleep((long)(thinkTime/RunSettings.getThinkTimeSpeedUpFactor()));}
					catch(InterruptedException e){
						e.printStackTrace();
					}
					if (RunSettings.isOutputThinkTimes()==true)
						client.getCg().getThinkTimeHist().add(thinkTime);
					Element child=xmlDocument.createElement("thinkTime");
					child.setTextContent(Long.toString(thinkTime));
					action.appendChild(child);
					long itemID=getRandomLongFromDist(unopenedItemsProb);
					if(HTML4){
						int numChars=(ITEMLINK_LABEL+itemID+"\"><a href=\".").length();
						start=html.indexOf(ITEMLINK_LABEL+itemID+"\"><a href=\".")+numChars;
						end=html.indexOf("\">",start);
						nextURL.append(html.subSequence(start, end));
					}
					else{
						if(listedItems.get(itemID).getTs()>=(new Date().getTime()-300000))				
							nextURL=new StringBuilder("ITEM").append(itemID);
						else
							nextURL.append("/viewitem?useHTML5=1&itemID=").append(itemID);	
					}
					request=xmlDocument.createElement("request");
					child=xmlDocument.createElement("type");
					child.setTextContent("GET");
					request.appendChild(child);
					child=xmlDocument.createElement("url");
					child.setTextContent(nextURL.toString());
					request.appendChild(child);

					action.appendChild(request);
					client.addXMLAction(action);
					oneTab=true;
					threadExecutor.shutdownNow();
					return nextURL;
				}
				if(!oneTab){
					while(client.getOpenTabs().size()>0){
						boolean repeat;
						do{
							repeat=false;
							nextURL=(client.getOpenTabs().get(0)).makeDecision();
							if(nextURL==null){
								for(int i=0;i<client.getOpenTabs().size();i++)
									client.getOpenTabs().get(i).cancelTimer();
								client.getOpenTabs().clear();
								threadExecutor.shutdownNow();
								return null;
							}
							if(verbose)System.out.println("NextURL: "+nextURL+ " "+client.getOpenTabs().size());
							if (nextURL.toString().equals(CLOSE_TAB))
								client.getOpenTabs().remove(0);
							else if(nextURL.equals(client.getOpenTabs().get(0).getURL())==false){
								for(int i=0;i<client.getOpenTabs().size();i++)
									client.getOpenTabs().get(i).cancelTimer();
								client.getOpenTabs().clear();
								threadExecutor.shutdownNow();
								return nextURL;
							}
							else{
								repeat=true;
								ItemPage page=(ItemPage)new Page(nextURL,pageType,nextURL,client).toPageType();
								page.setTabbed(true);
								client.getOpenTabs().remove(0);
								client.getOpenTabs().add(0, page);
							}
						}while(repeat);
					}
					do{
						nextLink=new StringBuilder(getRandomStringBuilderFromDist(nextPageProbabilities));
						nextLinkS=nextLink.toString();
						if(verbose)System.out.println("Next Link After Tabs: "+nextLinkS);
					}while(nextLinkS.equals("tabs"));
					try{Thread.sleep(getThinkTime());}
					catch(InterruptedException e){
						for(int i=0;i<client.getOpenTabs().size();i++)
							client.getOpenTabs().get(i).cancelTimer();
						client.setExit(true);
						threadExecutor.shutdownNow();
						return null;
					}
					if (nextLinkS.equals(SEARCH_TEXT)){
						if(!HTML4){
							searchData.put("userID", new StringBuilder(client.getClientInfo().getHTML5Cache().get("userID")));
							searchData.put("authToken", new StringBuilder(client.getClientInfo().getHTML5Cache().get("authToken")));
							searchData.put("useHTML5", new StringBuilder("1"));
							searchData.put("pageNo", new StringBuilder("0"));
							searchData.put("itemsPP",new StringBuilder("25"));
							searchData.put("hasItems", getHasItems());
						}
						threadExecutor.shutdownNow();
						return search(searchData,action);
					}
					else{
						nextURL=new StringBuilder(client.getCMARTurl().getAppURL());
						if(HTML4){
							if (nextLinkS.equals(SEARCH_TEXT)){
								threadExecutor.shutdownNow();
								return search(searchData,action);
							}
							else if(nextLinkS.equals("End Date")||nextLinkS.equals("Bid Price")){
								end=html.indexOf("\">"+nextLink);
								start=html.lastIndexOf(HREF_TEXT,end)+(HREF_TEXT).length();
							}
							else if(nextLinkS.equals("Next Page")||nextLinkS.equals("< Previous Page")){
								end=html.indexOf("\">"+nextLink);
								start=html.lastIndexOf(HREF_TEXT,end)+(HREF_TEXT).length();
							}
							else if(nextLinkS.equals("Clarify Search")){
								threadExecutor.shutdownNow();
								return search(client.getPreviousSearchTerm(),searchData,action);
							}
							else{
								end=html.indexOf("\"/>"+nextLink);
								start=html.lastIndexOf(HREF_TEXT,end)+(HREF_TEXT).length();
								end=html.indexOf("\">",start);
							}
							nextURL.append(html.subSequence(start,end));

						}
						else{
							if(nextLinkS.equals(SELL_TEXT)){
								nextURL.append("/sell.html");
							}
							else if(nextLinkS.equals(BROWSE_TEXT)){
								nextURL.append("/browsecategory?useHTML5=1&categoryID=0&pageNo=0&itemsPP=25&userID=").append(client.getClientInfo().getHTML5Cache().get("userID")).append("&authToken=").append(client.getClientInfo().getHTML5Cache().get("authToken")).append("&catTs=").append(new Date().getTime()).append("&hasItems=").append(getHasItems());
							}
							else if(nextLinkS.equals(MY_ACCOUNT_TEXT)){
								nextURL.append("/myaccount?useHTML5=1&userID=").append(client.getClientInfo().getHTML5Cache().get("userID")).append("&authToken=").append(client.getClientInfo().getHTML5Cache().get("authToken")).append("&ts=").append(new StringBuilder(Long.toString(new Date().getTime())));
							}
							else if(nextLinkS.equals(LOGOUT_TEXT)){
								nextURL.append("/logout.html");
							}
							else if(nextLinkS.equals(HOME_TEXT)){
								nextURL.append("/index.html");
							}
							else if(nextLinkS.equals("Next Page")){
								nextURL.append("/search?useHTML5=1").append("&pageNo=").append(pageNum+1).append("&userID=").append(client.getClientInfo().getHTML5Cache().get("userID")).append("&authToken=").append(client.getClientInfo().getHTML5Cache().get("authToken")).append("&itemsPP=25").append("&searchTerm=").append(client.getPreviousSearchTerm()).append("&hasItems=").append(getHasItems());
							}
							else if(nextLinkS.equals("< Previous Page")){
								nextURL.append("/search?useHTML5=1").append("&pageNo=").append(pageNum-1).append("&userID=").append(client.getClientInfo().getHTML5Cache().get("userID")).append("&authToken=").append(client.getClientInfo().getHTML5Cache().get("authToken")).append("&itemsPP=25").append("&searchTerm=").append(client.getPreviousSearchTerm()).append("&hasItems=").append(getHasItems());
							}
							else if(nextLinkS.equals(SEARCH_TEXT)||nextLinkS.equals("Clarify Search")){
								searchData.put("userID", new StringBuilder(client.getClientInfo().getHTML5Cache().get("userID")));
								searchData.put("authToken", new StringBuilder(client.getClientInfo().getHTML5Cache().get("authToken")));
								searchData.put("useHTML5", new StringBuilder("1"));
								searchData.put("pageNo", new StringBuilder("0"));
								searchData.put("itemsPP",new StringBuilder("25"));
								searchData.put("hasItems", getHasItems());
								threadExecutor.shutdownNow();
								if(nextLinkS.equals(SEARCH_TEXT))
									return search(searchData,action);
								else
									return search(client.getPreviousSearchTerm(),searchData,action);
							}
							else if(nextLinkS.equals("End Date")||nextLinkS.equals("Bid Price")){
								int sortCol=1;
								if(nextLinkS.equals("Bid Price"))
									sortCol=2;
								int sortDec=0;
								start=url.indexOf("&sortDec=");
								if(start!=-1){
									start+="&sortDec=".length();
									sortDec=1-Integer.parseInt(url.substring(start,start+1));
								}
								nextURL.append("/search?useHTML5=1").append("&itemsPP=25").append("&sortCol=").append(sortCol).append("&sortDec=").append(sortDec).append("&searchTerm=").append(client.getPreviousSearchTerm());
							}

						}
					}
					threadExecutor.shutdownNow();
					return nextURL;
				}
			}
			if(HTML4==true){
				if (nextLinkS.equals(SEARCH_TEXT)){
					threadExecutor.shutdownNow();
					return search(searchData,action);
				}
				else if(nextLinkS.equals("End Date")||nextLinkS.equals("Bid Price")){
					end=html.indexOf("\">"+nextLink);
					start=html.lastIndexOf(HREF_TEXT,end)+(HREF_TEXT).length();
				}
				else if(nextLinkS.equals("Next Page")||nextLinkS.equals("< Previous Page")){
					end=html.indexOf("\">"+nextLink);
					start=html.lastIndexOf(HREF_TEXT,end)+(HREF_TEXT).length();
				}
				else if(nextLinkS.equals("Clarify Search")){
					threadExecutor.shutdownNow();
					return search(client.getPreviousSearchTerm(),searchData,action);
				}
				else{
					end=html.indexOf("\"/>"+nextLink);
					start=html.lastIndexOf(HREF_TEXT,end)+(HREF_TEXT).length();
					end=html.indexOf("\">",start);
				}
				nextURL.append(html.subSequence(start,end));

				request=xmlDocument.createElement("request");
				Element child=xmlDocument.createElement("type");
				child.setTextContent("GET");
				request.appendChild(child);
				child=xmlDocument.createElement("url");
				child.setTextContent(nextURL.toString());
				request.appendChild(child);

				action.appendChild(request);
				client.addXMLAction(action);			
			}
			else{ // if HTML5
				if(nextLinkS.equals(SELL_TEXT)){
					nextURL.append("/sell.html");
				}
				else if(nextLinkS.equals(BROWSE_TEXT)){
					nextURL.append("/browsecategory?useHTML5=1&categoryID=0&pageNo=0&itemsPP=25&userID=").append(client.getClientInfo().getHTML5Cache().get("userID")).append("&authToken=").append(client.getClientInfo().getHTML5Cache().get("authToken")).append("&catTs=").append(new Date().getTime()).append("&hasItems=").append(getHasItems());
				}
				else if(nextLinkS.equals(MY_ACCOUNT_TEXT)){
					nextURL.append("/myaccount?useHTML5=1&userID=").append(client.getClientInfo().getHTML5Cache().get("userID")).append("&authToken=").append(client.getClientInfo().getHTML5Cache().get("authToken")).append("&ts=").append(new StringBuilder(Long.toString(new Date().getTime())));
				}
				else if(nextLinkS.equals(LOGOUT_TEXT)){
					nextURL.append("/logout.html");
				}
				else if(nextLinkS.equals(HOME_TEXT)){
					nextURL.append("/index.html");
				}
				else if(nextLinkS.equals("Next Page")){

					nextURL.append("/search?useHTML5=1").append("&pageNo=").append(pageNum+1).append("&itemsPP=25").append("&searchTerm=").append(client.getPreviousSearchTerm()).append("&hasItems=").append(getHasItems());
				}
				else if(nextLinkS.equals("< Previous Page")){

					nextURL.append("/search?useHTML5=1").append("&pageNo=").append(pageNum-1).append("&itemsPP=25").append("&searchTerm=").append(client.getPreviousSearchTerm()).append("&hasItems=").append(getHasItems());
				}
				else if(nextLinkS.equals(SEARCH_TEXT)||nextLinkS.equals("Clarify Search")){
					searchData.put("userID", new StringBuilder(client.getClientInfo().getHTML5Cache().get("userID")));
					searchData.put("authToken", new StringBuilder(client.getClientInfo().getHTML5Cache().get("authToken")));
					searchData.put("useHTML5", new StringBuilder("1"));
					searchData.put("pageNo", new StringBuilder("0"));
					searchData.put("itemsPP",new StringBuilder("25"));
					searchData.put("hasItems", getHasItems());
					threadExecutor.shutdownNow();
					if(nextLinkS.equals(SEARCH_TEXT))
						return search(searchData,action);
					else
						return search(client.getPreviousSearchTerm(),searchData,action);
				}
				else if(nextLinkS.equals("End Date")||nextLinkS.equals("Bid Price")){
					int sortCol=1;
					if(nextLinkS.equals("Bid Price"))
						sortCol=2;
					int sortDec=0;
					start=url.indexOf("&sortDec=");
					if(start!=-1){
						start+="&sortDec=".length();
						sortDec=1-Integer.parseInt(url.substring(start,start+1));
					}
					nextURL.append("/search?useHTML5=1").append("&itemsPP=25").append("&sortCol=").append(sortCol).append("&sortDec=").append(sortDec).append("&searchTerm=").append(client.getPreviousSearchTerm());
				}

				request=xmlDocument.createElement("request");
				Element child=xmlDocument.createElement("type");
				child.setTextContent("GET");
				request.appendChild(child);
				child=xmlDocument.createElement("url");
				child.setTextContent(nextURL.toString());
				request.appendChild(child);

				action.appendChild(request);
				client.addXMLAction(action);


			}
		}else{
			HashMap<String, StringBuilder> data=new HashMap<String,StringBuilder>();
			findAction();
			if(pageType!=Integer.parseInt(action.getElementsByTagName("currentPage").item(0).getTextContent())){
				client.setExit(true);
				client.setExitDueToRepeatChange(true);
				threadExecutor.shutdownNow();
				return null;
			}
			nextLinkS=action.getElementsByTagName("nextPage").item(0).getTextContent();
			request=(Element)(action).getElementsByTagName("request").item(0);
			nextURL=new StringBuilder(request.getElementsByTagName("url").item(0).getTextContent());
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
			if(!nextPageProbabilities.containsValue(nextLinkS)){
				client.setExit(true);
				client.setExitDueToRepeatChange(true);
				threadExecutor.shutdownNow();
				return null;
			}

			if(nextLinkS.equals(SEARCH_TEXT)||nextLinkS.equals("Clarify Search")){
				return search(data,action);
			}
			else if(nextLinkS.equals("tabs")){
				NodeList requests=(action).getElementsByTagName("request");
				if(requests.getLength()>1){
					openTabs(0,action);

					while(client.getOpenTabs().size()>0){
						boolean repeat;
						do{
							repeat=false;
							nextURL=(client.getOpenTabs().get(0)).makeDecision();
							if(nextURL==null){
								for(int i=0;i<client.getOpenTabs().size();i++)
									client.getOpenTabs().get(i).cancelTimer();
								threadExecutor.shutdownNow();
								return null;
							}
							if (nextURL.toString().equals(CLOSE_TAB)){
								client.getOpenTabs().get(0).cancelTimer();
								client.getOpenTabs().remove(0);
							}
							else if(nextURL.equals(client.getOpenTabs().get(0).getURL())==false){
								for(int i=0;i<client.getOpenTabs().size();i++)
									client.getOpenTabs().get(i).cancelTimer();
								threadExecutor.shutdownNow();
								return nextURL;
							}
							else{
								repeat=true;
								ItemPage page=(ItemPage)new Page(nextURL,pageType,nextURL,client).toPageType();
								page.setTabbed(true);
								client.getOpenTabs().get(0).cancelTimer();
								client.getOpenTabs().remove(0);
								client.getOpenTabs().add(0, page);
							}
						}while(repeat);
					}
					Element req=(Element)requests.item(requests.getLength()-1);
					nextURL=new StringBuilder(req.getElementsByTagName("url").item(0).getTextContent());
					if(nextURL.indexOf("authToken=")!=-1){
						int start=nextURL.indexOf("&authToken=")+"&authToken=".length();
						int end=nextURL.indexOf("&",start);
						if(end==-1)
							end=nextURL.length();
						nextURL.replace(start, end, client.getClientInfo().getAuthToken().toString());
					}
					if(nextLink.equals(SEARCH_TEXT)||nextLinkS.equals("Clarify Search")){
						data.clear();
						dataList=req.getElementsByTagName("data");
						for(int i=0;i<dataList.getLength();i++){
							Node n=dataList.item(i);
							String key=n.getAttributes().item(0).getTextContent();
							StringBuilder value=new StringBuilder(((Element)n).getTextContent());
							data.put(key, value);
						}
						if(data.containsKey("authToken"))
							data.put("authToken",client.getClientInfo().getAuthToken());
						return search(new StringBuilder("1"),data,action);
					}
					int newThinkTime=Integer.parseInt(action.getElementsByTagName("thinkTime").item(1).getTextContent());
					if (RunSettings.isOutputThinkTimes()==true)
						client.getCg().getThinkTimeHist().add(newThinkTime);
					try{Thread.sleep((long) Math.max(newThinkTime/RunSettings.getThinkTimeSpeedUpFactor(),0));}
					catch(InterruptedException e){
						for(int i=0;i<client.getOpenTabs().size();i++)
							client.getOpenTabs().get(i).cancelTimer();
						client.setExit(true);
						threadExecutor.shutdownNow();
						return null;
					}


				}else{
					long itemID=Long.parseLong(nextURL.substring(nextURL.indexOf("&itemID=")+"&itemID=".length()));
					if(!itemsProb.containsValue(itemID)){
						client.setExit(true);
						client.setExitDueToRepeatChange(true);
						threadExecutor.shutdownNow();
						return null;
					}else{
						OpenNewTab ont=new OpenNewTab(nextURL);
						threadExecutor.execute(ont);
					}
					
					// Think Time
					try{Thread.sleep(getThinkTime());}
					catch(InterruptedException e){
						for(int i=0;i<client.getOpenTabs().size();i++)
							client.getOpenTabs().get(i).cancelTimer();
						client.setExit(true);
						threadExecutor.shutdownNow();
						return null;
					}
				}
			}
			else{
				// Think Time
				try{Thread.sleep(getThinkTime());}
				catch(InterruptedException e){
					for(int i=0;i<client.getOpenTabs().size();i++)
						client.getOpenTabs().get(i).cancelTimer();
					client.setExit(true);
					threadExecutor.shutdownNow();
					return null;
				}
			}
		}

		threadExecutor.shutdownNow();
		return nextURL;
	}

	/**
	 * Updates the item ratings for all items on the page
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	private void updateItemRatings() throws ParseException, JsonParseException, JsonMappingException, IOException{
		if(HTML4==true&&html.indexOf("Sorry, there are not items to view")==-1){
			int start=0;
			int end=0;
			int numPics=0;
			int i=0;
			start=html.indexOf("<div class=\"entry\" id=\"entry\">",start);
			while(start!=-1){
				ItemCG item=new ItemCG(client);
				start=html.indexOf(ITEMLINK_LABEL,start)+(ITEMLINK_LABEL).length();
				end=html.indexOf("\">",start);
				long itemID=Long.parseLong(html.substring(start,end));
				if(client.getItemsOfInterest().containsKey(itemID))
					item=client.getItemsOfInterest().get(itemID);
				item.setId(itemID);
				start=html.lastIndexOf("/img3/",start)+"/img3/".length();
				end=html.indexOf("\" alt=",start);
				if(html.substring(start,end).contains("blank.jpg"))
					numPics=0;
				else
					numPics=1;
				item.setNumPics(numPics);
				start=html.indexOf("itemID="+itemID+"\">",end)+("itemID="+itemID+"\">").length();
				end=html.indexOf("</a>",start);
				item.setName(html.substring(start,end));
				item.isOnSearchPage();
				for (String s:searchTermWords){
					if (item.getName().indexOf(s)!=-1)
						item.incCommonSearchTerms();
				}
				start=html.indexOf("<label for=\"itemBid"+itemID+"\">$",end)+("<label for=\"itemBid"+itemID+"\">$").length();
				end=html.indexOf("</label>",start);
				item.setCurrentBid(Double.parseDouble(html.substring(start,end).replace(",", "")));
				start=html.indexOf("<label for=\"itemEndDate"+itemID+"\">",end)+("<label for=\"itemEndDate"+itemID+"\">").length();
				end=html.indexOf("</label>",start);
				item.setEndDate(DateParser.stringToDate(html.substring(start,end)));
				item.setListRank(i);
				item.calcItemRating();
				listedItems.put(itemID,item);

				client.addToItemsOfInterest(itemID,item);
				i++;
				start=html.indexOf("<div class=\"entry\" id=\"entry\">",start);
			}
		}
		else if (!HTML4){
			long itemID;
			ObjectMapper mapper = new ObjectMapper();
			if(verbose)System.out.println(html);
			JsonNode node=null;
			try{
				node = mapper.readValue(html.toString(), JsonNode.class);
			}catch(Exception e){
				System.err.println(html);
				e.printStackTrace();
			}
			for (int i=0;i<node.get("items").size();i++){
				ItemCG item=new ItemCG(client);
				item.setName(node.get("items").get(i).get("name").getTextValue());
				itemID=node.get("items").get(i).get("id").getLongValue();
				if(client.getItemsOfInterest().containsKey(itemID))
					item=client.getItemsOfInterest().get(itemID);
				item.setId(itemID);
				item.setEndDate(DateParser.stringToDate(node.get("items").get(i).get("endDate").getTextValue()));
				item.setCurrentBid(node.get("items").get(i).get("currentBid").getDoubleValue());
				String thumbImage=node.get("items").get(i).get("thumbnail").getTextValue();
				if (thumbImage.equals("blank.jpg"))
					item.setNumPics(0);
				else
					item.setNumPics(1);
				item.setSellerID(node.get("items").get(i).get("sellerID").getLongValue());

				if(item.getEndDate().after(new Date())){
					item.calcItemRating();
					client.addToItemsOfInterest(itemID,item);

					item.setDescription(node.get("items").get(i).get("description").getTextValue());
					item.setQuantity(node.get("items").get(i).get("quantity").getLongValue());
					item.setStartPrice(node.get("items").get(i).get("startPrice").getDoubleValue());
					item.setBuyNowPrice(node.get("items").get(i).get("buyNowPrice").getDoubleValue());
					if(item.getBuyNowPrice()==0)
						item.setForBuyNow(false);
					item.setNoOfBids(node.get("items").get(i).get("noOfBids").getLongValue());
					item.setStartDate(DateParser.stringToDate(node.get("items").get(i).get("startDate").getTextValue()));
					item.setNumPics(node.get("items").get(i).get("images").size());
					for(int k=0;k<item.getNumPics();k++){
						item.addImage(node.get("items").get(i).get("images").get(k).get("url").getTextValue());
					}

					item.setTs(new Date().getTime());
					client.getClientInfo().addHTML5ItemCache(item);
				}
			}
			for (int i=0;i<node.get("order").size();i++){
				itemID=node.get("order").get(i).getLongValue();
				if(client.getItemsOfInterest().containsKey(itemID))
					listedItems.put(itemID, client.getItemsOfInterest().get(itemID));
			}
		}
		if(verbose)System.out.println("Item Ratings: "+client.getItemsOfInterestRatings());
	}

	public void getSellers() throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readValue(html.toString(), JsonNode.class);
		for (int i=0;i<node.get("sellers").size();i++){
			SellerCG seller=new SellerCG(node.get("sellers").get(i));
			client.getClientInfo().addHTML5SellerCache(seller);
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
		double browseProb=RunSettings.getTransitionProb(pageType,1);
		double sellProb=RunSettings.getTransitionProb(pageType,2);
		double myAccountProb=RunSettings.getTransitionProb(pageType,3);
		double logOutProb=RunSettings.getTransitionProb(pageType,4);
		double searchProb=RunSettings.getTransitionProb(pageType,5);
		double previousPageProb=RunSettings.getTransitionProb(pageType,6);
		double nextPageProb=RunSettings.getTransitionProb(pageType,7);
		double clarifySearchProb=RunSettings.getTransitionProb(pageType,8);
		double tabbedBrowsingProb=RunSettings.getTransitionProb(pageType,9);
		double sortDateProb=RunSettings.getTransitionProb(pageType,10);
		double sortPriceProb=RunSettings.getTransitionProb(pageType,11);


		if(RunSettings.getWorkloadTypeCode()==1){
			browseProb*=1.15;
			sellProb*=0.45;
			searchProb*=1.15;
			nextPageProb*=1.15;
			clarifySearchProb*=1.4;
			tabbedBrowsingProb*=0.83;
			sortDateProb*=1.2;
			sortPriceProb*=1.2;
		}
		else if(RunSettings.getWorkloadTypeCode()==3){
			browseProb*=0.85;
			sellProb*=1.55;
			searchProb*=0.85;
			nextPageProb*=0.85;
			clarifySearchProb*=0.6;
			tabbedBrowsingProb*=1.17;
			sortDateProb*=0.8;
			sortPriceProb*=0.8;
		}


		double ratingSum=0;
		double tabStandardRating=(double)5.2*listedItems.size();
		if (RunSettings.isMarkovTransitions()==false){
			homeProb*=(1.+(rand.nextDouble()-0.5)*.3);
			browseProb*=(1.+(rand.nextDouble()-0.5)*.3);
			sellProb*=(1.+(rand.nextDouble()-0.5)*.3);
			myAccountProb*=(1.+(rand.nextDouble()-0.5)*.3);
			logOutProb*=(1.+(rand.nextDouble()-0.5)*.3);
			searchProb*=(1.+(rand.nextDouble()-0.5)*.3);
			previousPageProb*=(1.+(rand.nextDouble()-0.5)*.3);
			nextPageProb*=(1.+(rand.nextDouble()-0.5)*.3);
			tabbedBrowsingProb*=(1.+(rand.nextDouble()-0.5)*.3);
			clarifySearchProb*=(1.+(rand.nextDouble()-0.5)*.3);
			sortDateProb*=(1.+(rand.nextDouble()-0.5)*.3);
			sortPriceProb*=(1.+(rand.nextDouble()-0.5)*.3);

			for (Entry<Long,ItemCG> e:listedItems.entrySet()){
				ratingSum+=e.getValue().getItemRating();
			}
			if(verbose)System.out.println("Rating Sum: "+ratingSum);
			if(listedItems.size()>0){
				tabbedBrowsingProb*=ratingSum/tabStandardRating;
				if(ratingSum>0)
					clarifySearchProb/=ratingSum/tabStandardRating;
				if(tabbedBrowsingProb<0)
					tabbedBrowsingProb=0;
				if(clarifySearchProb<0)
					clarifySearchProb=0;
			}
			else{
				tabbedBrowsingProb=0;
			}

			sellProb*=Math.exp(client.getExpSoldRate()*client.getItemsSold());
			searchProb*=Math.exp(client.getExpBuyRate()*(client.getItemsBid()+client.getItemsBought()));
			browseProb*=Math.exp(client.getExpBuyRate()*(client.getItemsBid()+client.getItemsBought()));
			myAccountProb*=Math.exp(client.getExpBuyRate()*client.getItemsBid());
			logOutProb*=Math.exp(client.getExpSoldRate()*client.getItemsSold()+client.getExpBuyRate()*(client.getItemsBid()+client.getItemsBought()));

			if(verbose)System.out.println(ratingSum+","+listedItems.size());
		}
		else{
			if(listedItems.size()==0)
				tabbedBrowsingProb=0;
		}

		TreeMap<String,Double>allOptions=new TreeMap<String,Double>();

		if(HTML4==true){
			if (html.indexOf("Previous Page</a>")==-1)
				previousPageProb=0;
			if (html.indexOf("Next Page ></a>")==-1)
				nextPageProb=0;
		}
		else{
			int start=url.indexOf("pageNo=");
			if(start==-1){
				pageNum=0;
				previousPageProb=0;
			}
			else{
				start=start+"pageNo=".length();
				int end=url.indexOf("&",start);
				pageNum=Integer.parseInt(url.substring(start,end));
				if(pageNum==0)
					previousPageProb=0;
			}
		}
		int numItems=listedItems.size();
		if(numItems<25)
			nextPageProb=0;
		if(verbose)System.out.println("Number of listed items: "+numItems);
		for (Entry<Long,ItemCG> m:listedItems.entrySet()){
			if(m.getKey()!=0)
				if (RunSettings.isMarkovTransitions()==false)
					itemsProb.put(m.getKey(),(tabbedBrowsingProb*m.getValue().getItemRating())/(ratingSum));
				else
					itemsProb.put(m.getKey(),tabbedBrowsingProb/((double)listedItems.size()));
		}
		unopenedItemsProb=normalizeItemProbs();

		client.setOrigLogoutProb(logOutProb);
		logOutProb=adjustLogOutProb(logOutProb);
		client.setFinalLogoutProb(logOutProb);


		allOptions.put((HOME_TEXT),homeProb);
		allOptions.put((MY_ACCOUNT_TEXT),myAccountProb);
		allOptions.put((SELL_TEXT),sellProb);
		allOptions.put((BROWSE_TEXT),browseProb);
		allOptions.put((LOGOUT_TEXT),logOutProb);
		allOptions.put((SEARCH_TEXT),searchProb);
		allOptions.put("< Previous Page",previousPageProb);
		allOptions.put("Next Page",nextPageProb);
		allOptions.put("Clarify Search",clarifySearchProb);
		allOptions.put("tabs",tabbedBrowsingProb);
		allOptions.put("Bid Price",sortPriceProb);
		allOptions.put("End Date",sortDateProb);


		// normalizes the probabilities
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
	 * Opens a set amount of tabs of item pages
	 * @param numTabs - number of tabs to open
	 * @throws InterruptedException
	 */
	private void openTabs(int numTabs, Element action){
		String nextTab=null;
		if(RunSettings.isRepeatedRun()==false){
			long thinkTime=(long)expDist(8000);
			try{Thread.sleep((long)(thinkTime/RunSettings.getThinkTimeSpeedUpFactor()));}
			catch(InterruptedException e){
				e.printStackTrace();
			}
			if (RunSettings.isOutputThinkTimes()==true)
				client.getCg().getThinkTimeHist().add(thinkTime);
			Element child=xmlDocument.createElement("thinkTime");
			child.setTextContent(Long.toString(thinkTime));
			action.appendChild(child);
			for (int i=0;i<numTabs;i++){
				Element request=xmlDocument.createElement("request");
				long itemID=getRandomLongFromDist(unopenedItemsProb);
				if(HTML4){
					int numChars=(ITEMLINK_LABEL+itemID+"\"><a href=\".").length();
					int start=html.indexOf(ITEMLINK_LABEL+itemID+"\"><a href=\".")+numChars;
					int end=html.indexOf("\">",start);
					nextTab=html.substring(start,end);
				}

				// Think Time
				long newThinkTime=(long)expDist(2500);
				try{Thread.sleep((long)(newThinkTime/RunSettings.getThinkTimeSpeedUpFactor()));}
				catch(InterruptedException e){
					e.printStackTrace();
				}
				child=xmlDocument.createElement("thinkTime");
				child.setTextContent(Long.toString(newThinkTime));
				request.appendChild(child);
				if(HTML4){
					child=xmlDocument.createElement("type");
					child.setTextContent("GET"); 
					request.appendChild(child);
					child=xmlDocument.createElement("url");
					child.setTextContent(new StringBuilder(client.getCMARTurl().getAppURL()).append(nextTab).toString());
					request.appendChild(child);
					OpenNewTab ont=new OpenNewTab(new StringBuilder(client.getCMARTurl().getAppURL()).append(nextTab));
					threadExecutor.execute(ont);
				}
				else{
					StringBuilder nextURL=new StringBuilder();
					if(listedItems.get(itemID).getTs()>=(new Date().getTime()-300000))				
						nextURL=new StringBuilder("ITEM").append(itemID);
					else
						nextURL=new StringBuilder(client.getCMARTurl().getAppURL()).append("/viewitem?useHTML5=1&itemID=").append(itemID);	
					child=xmlDocument.createElement("type");
					child.setTextContent("GET");
					request.appendChild(child);
					child=xmlDocument.createElement("url");
					child.setTextContent(nextURL.toString());
					request.appendChild(child);
					OpenNewTab ont=new OpenNewTab(nextURL);
					threadExecutor.execute(ont);
				}

				action.appendChild(request);
				openTabs.add(itemID);
				itemsProb.remove(itemID);
				unopenedItemsProb=normalizeItemProbs();

			}
			client.addXMLAction(action);
		}else{
			NodeList requests=(action).getElementsByTagName("request");
			try{Thread.sleep(getThinkTime());}
			catch(InterruptedException e){
				e.printStackTrace();
			}
			for(int i=0;i<requests.getLength();i++){
				Element req=(Element)requests.item(i);
				if(req.getElementsByTagName("thinkTime").getLength()>0){
					try{Thread.sleep((long)(Long.parseLong(req.getElementsByTagName("thinkTime").item(0).getTextContent())/RunSettings.getThinkTimeSpeedUpFactor()));}
					catch(InterruptedException e){
						e.printStackTrace();
					}
					StringBuilder nextURL=new StringBuilder(req.getElementsByTagName("url").item(0).getTextContent());
					if(nextURL.indexOf("authToken=")!=-1){
						int start=nextURL.indexOf("&authToken=")+"&authToken=".length();
						int end=nextURL.indexOf("&",start);
						if(end==-1)
							end=nextURL.length();
						nextURL.replace(start, end, client.getClientInfo().getAuthToken().toString());
					}
					int start=nextURL.indexOf("&itemID=")+"&itemID=".length();
					long itemID;
					if(nextURL.indexOf("&",start)==-1)
						itemID=Long.parseLong(nextURL.substring(start));
					else
						itemID=Long.parseLong(nextURL.substring(start,nextURL.indexOf("&",start)));
					if(!itemsProb.containsKey(itemID)){
						client.setChangeDueToRepeatChange(true);
					}else{
						OpenNewTab ont=new OpenNewTab(nextURL);
						threadExecutor.execute(ont);
					}
				}
			}

		}
		synchronized(this){
			while(getCompletedTabs()<numTabs){try {
				wait();
			} catch (InterruptedException e) {
				for(int i=0;i<client.getOpenTabs().size();i++)
					client.getOpenTabs().get(i).cancelTimer();
				threadExecutor.shutdownNow();
				break;
			}}
		}
	}

	/**
	 * Opens a new tab 
	 * @author Andrew Fox
	 *
	 */
	private class OpenNewTab extends Thread{
		StringBuilder itemUrl;		// url of the page to be opened
		/**
		 * Openes a tab with a specified url
		 * @param url - url of the page to be opened
		 */
		private OpenNewTab(StringBuilder itemUrl){
			this.itemUrl=new StringBuilder(itemUrl);
		}
		public void run(){
			if(verbose)System.out.println("Tab to open: "+itemUrl);
			try {
				//	client.addOpenTab(openURL(url,client));		// adds the tab to the client list of open tabs
				ItemPage page=(ItemPage)new Page(itemUrl,pageType,url,client).toPageType();
				page.setTabbed(true);
				client.addOpenTab(page);
				addFinishedTab();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (ClassCastException e) {
				System.err.println(html);
				System.err.println(url);
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Adds to the count of the number of tabs that have finished loading
	 */
	private synchronized void addFinishedTab(){
		completedTabs++;
		notify();
	}
	/**
	 * Gets the number of tabs that have completely loaded
	 * @return
	 */
	private synchronized int getCompletedTabs(){
		return completedTabs;
	}

	/**
	 * Normalizes the probability of each item to one based on the number of items still available to be opened
	 * @return New probability map with the remaining items
	 */
	private TreeMap<Double,Long>normalizeItemProbs(){
		TreeMap<Double,Long> map2 = new TreeMap<Double,Long>();
		double probSum=0;
		for (long m:itemsProb.keySet()){
			probSum+=itemsProb.get(m);
		}
		double newProbSum=probSum;
		for (long m:itemsProb.keySet()){
			map2.put(newProbSum/probSum,m);
			newProbSum-=itemsProb.get(m);
		}
		return map2;
	}

	/**
	 * Determines the think time before going to the next page
	 * Deducts from the think time the amount of time since the 
	 * page opened until this calculation is performed
	 * @return Think time in ms
	 */
	private int getThinkTime(){
		int thinkTime=(int)expDist(initialThinkTime+15000);
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
