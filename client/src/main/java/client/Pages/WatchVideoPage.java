package client.Pages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.w3c.dom.Element;

import client.clientMain.*;


/**
 * Watch Video Page
 * @author Andrew Fox
 *
 */


public class WatchVideoPage extends Page {
	TreeMap<Double,StringBuilder> nextPageProbabilities=new TreeMap<Double,StringBuilder>();	// map of probabilities of what the next page will be
	double pageRTFactor=1.5;

	public WatchVideoPage(Page page){
		super(page.url,page.html,page.client,page.pageType,page.pageOpenTime,page.lastPageType,page.lastURL,page.cg);
		client.pageSpecificRT(pageRTFactor);	// change RT threshold depending on page type

		updateProbabilities();					// update the next move probabilities
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
		StringBuilder nextURL=new StringBuilder(client.getCMARTurl().getAppURL());	// the URL of the next link to be opened

		StringBuilder nextLink=getRandomStringBuilderFromDist(nextPageProbabilities);		// randomly chooses the next link

		
		
		if(nextLink.toString().equals("Watch Video")){
			
			//TODO: INSERT WATCH VIDEO CODE HERE

		}

		

		// Think Time
		try{Thread.sleep(getThinkTime());}
		catch(InterruptedException e){
			client.setExit(true);
			return null;
		}

		return nextURL;	
	}

	/**
	 * Updates the probabilities of which page to transition to
	 */
	private void updateProbabilities(){
		double homeProb=0.;
		double browseProb=0.;
		double sellProb=0.;
		double myAccountProb=0.;
		double logoutProb=0.;
		double searchProb=0.;
		double watchVideoProb=1.;

		if(RunSettings.getWorkloadType()==1){
			homeProb=0.;
			browseProb=0.;
			sellProb=0.;
			myAccountProb=0.;
			logoutProb=0.;
			searchProb=0.;
			watchVideoProb=1.;
		}
		else if(RunSettings.getWorkloadType()==3){
			homeProb=0.;
			browseProb=0.;
			sellProb=0.;
			myAccountProb=0.;
			logoutProb=0.;
			searchProb=0.;
			watchVideoProb=1.;		
		}

		double probSum=1;

		TreeMap<String,Double>allOptions=new TreeMap<String,Double>();

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
		allOptions.put(("Watch Video"),watchVideoProb);


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
			cg.getThinkTimeHist().add(thinkTime);
		pageThinkTime=thinkTime;
		return Math.max((int) ((thinkTime-(new Date().getTime()-pageOpenTime))/RunSettings.getThinkTimeSpeedUpFactor()),0);
	}
}