package client.Pages;

import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;

import org.w3c.dom.Element;

import client.clientMain.RunSettings;


/**
 * View User Page
 * @author Andrew Fox
 *
 */


public class ViewUserPage extends Page {
	HashMap<String, StringBuilder> data=new HashMap<String, StringBuilder>();
	String userName;
	int sellerRating=0;
	TreeMap<Double,StringBuilder> nextPageProbabilities=new TreeMap<Double,StringBuilder>();


	public ViewUserPage(Page page){
		super(page.url,page.html,page.client,page.pageType,page.pageOpenTime,page.lastPageType,page.lastURL);
		searchData=getFormData("search");
	}

	/**
	 * Determines the next link to go to in the application
	 * @return the URL or HTML of the next link, depending on whether
	 * 		   the previous entry contained a form or not
	 */
	@Override public StringBuilder makeDecision(){
		StringBuilder nextLink=getRandomStringBuilderFromDist(nextPageProbabilities);

		if (HTML4==true){
			int start=html.indexOf("<label for=\"username\">")+"<label for=\"username\">".length();
			start=html.indexOf("Username: ",start)+"Username: ".length();
			int end=html.indexOf("<\"br />",start);
			userName=html.substring(start,end);
		}

		// Think Time
		try{Thread.sleep(getThinkTime());}
		catch(InterruptedException e){
			client.setExit(true);
			return null;
		}

		return nextLink;
	}

	/**
	 * Determines the think time before going to the next page
	 * Deducts from the think time the amount of time since the 
	 * page opened until this calculation is performed
	 * @return Think time in ms
	 */
	private int getThinkTime(){
		int thinkTime=(int)expDist(initialThinkTime);
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