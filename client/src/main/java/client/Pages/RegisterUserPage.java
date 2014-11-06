package client.Pages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import client.clientMain.RunSettings;


/**
 * Register User Page
 * @author Andrew Fox
 *
 */

public class RegisterUserPage extends Page {
	HashMap<String, StringBuilder> data=new HashMap<String, StringBuilder>();	// map of data to be posted to the register form
	ArrayList<String>changedEntries =new ArrayList<String>();	// entries in the registeruser form that have changed since last entry (may be all)
	double pageRTFactor=1.0;
	int bonusCharacters=0;

	public RegisterUserPage(Page page){
		super(page.url,page.html,page.client,page.pageType,page.pageOpenTime,page.lastPageType,page.lastURL);
		client.pageSpecificRT(pageRTFactor);	// change RT threshold depending on page type
	}

	/**
	 * Determines the next link to go to in the application
	 * @return the URL or HTML of the next link, depending on whether
	 * 		   the previous entry contained a form or not
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 * @throws InterruptedException 
	 */
	@Override public StringBuilder makeDecision() throws JsonParseException, JsonMappingException, IOException, InterruptedException{
		try{
		StringBuilder newPage=new StringBuilder();		// the page to be returned after the registration post
		StringBuilder nextLink=new StringBuilder(client.getCMARTurl().getAppURL());	// the link to send the data to
		String threeDigits=Integer.toString(rand.nextInt(999));	// used if username or email is taken
		boolean success=false;	// if the registration worked
		JsonNode node = null;	// to read in the JSON data

		if(RunSettings.isRepeatedRun()==false){

			action=xmlDocument.createElement("action");
			action.setAttribute("id",client.getActionNum());
			Element currentPage=xmlDocument.createElement("currentPage");
			currentPage.setTextContent(Integer.toString(pageType));
			action.appendChild(currentPage);
			Element nextPage=xmlDocument.createElement("nextPage");
			request=xmlDocument.createElement("request");

			// only gives 3 attempts to register
			if(client.getLoginAttempts()<3){
				nextPage.setTextContent("register");
				action.appendChild(nextPage);
				client.incLoginAttempts();	// adds an attempt at registration
				boolean changeUserName=false;	// if any of these values need to be changed based on a registration error
				boolean changeEmail=false;
				boolean changePassword=false;
				boolean changeZip=false;
				if (HTML4==false){
					if(html.charAt(0)=='{'){
						ObjectMapper mapper = new ObjectMapper();
						if(verbose)System.out.println(html);
						node = mapper.readValue(html.toString(), JsonNode.class);
						success=node.get("registeruser").getBooleanValue();
						if(success==false){
							for(String s:client.getErrors()){
								if (s.contains("The username is already taken")){
									client.getClientInfo().setUsername(new StringBuilder(client.getClientInfo().getUsername()).append(new StringBuilder("_").append(threeDigits)));
									changeUserName=true;
								}
								if (s.contains("The email address is already taken")){
									int atSign=client.getClientInfo().getEmail().indexOf("@");
									client.getClientInfo().setEmail(client.getClientInfo().getEmail().insert(atSign,new StringBuilder("_").append(threeDigits)));
									changeEmail=true;
								}
								if (s.contains("The email addresses you entered are different Make sure both of the email addresses are the same")){
									changeEmail=true;
								}
								if (s.contains("The passwords you entered are different Make sure both of the passwords are the same")){
									changePassword=true;
								}
								if(s.contains("The zip you entered is not valid")){
									changeZip=true;
								}							
							}
						}
					}
				}

				if(HTML4){			// gets previous form data from the HTML4 page
					searchData=getFormData("search");
					data=getFormData("registeruser");
				}
				// error checking on the form entry
				if (client.getLoginAttempts()>1){
					if (html.indexOf("The username is already taken")!=-1){
						client.getClientInfo().setUsername(new StringBuilder(client.getClientInfo().getUsername()).append(new StringBuilder("_").append(threeDigits)));
						changeUserName=true;
					}
					if (html.indexOf("The email address is already taken")!=-1){
						int atSign=client.getClientInfo().getEmail().indexOf("@");
						client.getClientInfo().setEmail(client.getClientInfo().getEmail().insert(atSign,new StringBuilder("_").append(threeDigits)));
						changeEmail=true;
					}
					if (html.indexOf("The email addresses you entered are different Make sure both of the email addresses are the same")!=-1){
						changeEmail=true;
					}
					if (html.indexOf("The passwords you entered are different Make sure both of the passwords are the same")!=-1){
						changePassword=true;
					}
					if(html.indexOf("The zip you entered is not valid")!=-1){
						changeZip=true;
					}
				}

				//enters the form data into the data map, only changes fields which need to be changed
				if(client.getLoginAttempts()==1||changeUserName==true||!HTML4){
					if(client.getLoginAttempts()==1||changeUserName==true){
						data.put("username",typingError(client.getClientInfo().getUsername()));
						changedEntries.add("username");
					}
					else
						data.put("username",(client.getClientInfo().getUsername()));
				}
				if(client.getLoginAttempts()==1||changePassword==true||!HTML4){
					if(client.getLoginAttempts()==1||changePassword==true){
						data.put("password1",typingError(client.getClientInfo().getPassword()));
						changedEntries.add("password1");
						data.put("password2",typingError(client.getClientInfo().getPassword()));
						changedEntries.add("password2");
					}
					else{
						data.put("password1",(client.getClientInfo().getPassword()));
						data.put("password2",(client.getClientInfo().getPassword()));
					}
					if(!HTML4){
						int attempts=0;
						while(!data.get("password1").equals(data.get("password2"))){
							if(attempts>=4){
								client.setExit(true);
								break;
							}
							data.put("password1",typingError(client.getClientInfo().getPassword()));
							data.put("password2",typingError(client.getClientInfo().getPassword()));
							bonusCharacters=data.get("password1").length()+data.get("password2").length();
							attempts++;
						}
					}
				}
				if(client.getLoginAttempts()==1||changeEmail==true||!HTML4){
					if(client.getLoginAttempts()==1||changeEmail==true){
						data.put("email1",typingError(client.getClientInfo().getEmail()));
						changedEntries.add("email1");
						data.put("email2",typingError(client.getClientInfo().getEmail()));
						changedEntries.add("email2");
					}
					else{
						data.put("email1",(client.getClientInfo().getEmail()));
						data.put("email2",(client.getClientInfo().getEmail()));
					}
					if(!HTML4){
						int attempts=0;
						while(!data.get("email1").equals(data.get("email2"))||!data.get("email1").toString().matches("[^ @]*@[^ @]*")||!data.get("email2").toString().matches("[^ @]*@[^ @]*")){
							if(attempts>=4){
								client.setExit(true);
								break;
							}
							data.put("email1",typingError(client.getClientInfo().getEmail()));
							data.put("email2",typingError(client.getClientInfo().getEmail()));
							bonusCharacters=data.get("email1").length()+data.get("email2").length();
							attempts++;
						}
					}
				}
				if(client.getLoginAttempts()==1||changeZip==true||!HTML4){
					if(client.getLoginAttempts()==1||changeZip==true){
						data.put("zip",typingError(client.getClientInfo().getZipcode()));
						changedEntries.add("zip");
					}
					else
						data.put("zip",typingError(client.getClientInfo().getZipcode()));
					if(!HTML4){
						int attempts=0;
						while(!data.get("zip").toString().matches("^\\d{5}(-\\d{4})?$")){
							if(attempts>=4){
								client.setExit(true);
								break;
							}
							data.put("zip",typingError(client.getClientInfo().getZipcode()));
							bonusCharacters=data.get("zip").length();
							attempts++;
						}
					}
				}
				if(client.getLoginAttempts()==1||!HTML4){
					data.put("firstname",typingError(client.getClientInfo().getFirstName()));
					data.put("lastname",typingError(client.getClientInfo().getLastName()));
					data.put("street",typingError(client.getClientInfo().getAddress()));
					data.put("town",typingError(client.getClientInfo().getCity()));
					data.put("state",(client.getClientInfo().getUSStateCode()));
					if(client.getLoginAttempts()==1){
						changedEntries.add("firstname");
						changedEntries.add("lastname");
						changedEntries.add("street");
						changedEntries.add("town");
						changedEntries.add("state");
					}
				}
				if(!HTML4)
					data.put("useHTML5",new StringBuilder("1"));


				// Think Time
				try{Thread.sleep(getThinkTime());}
				catch(InterruptedException e){
					client.setExit(true);
					return null;
				}
				// submits the form and gets the response page
				Element child=xmlDocument.createElement("url");
				child.setTextContent(new StringBuilder(nextLink).append("/registeruser").toString());
				request.appendChild(child);
				for (Entry<String,StringBuilder> e:data.entrySet()){
					child=xmlDocument.createElement("data");
					child.setAttribute("name", e.getKey());
					child.setTextContent(e.getValue().toString());
					request.appendChild(child);
				}
				if (HTML4){
					child=xmlDocument.createElement("type");
					child.setTextContent("POST");
					request.appendChild(child);
					newPage=doSubmit(nextLink.append("/registeruser"),data);
				}
				else{
					child=xmlDocument.createElement("type");
					child.setTextContent("GET");
					request.appendChild(child);
					newPage=openHTML5PageWithRedirect(nextLink.append("/registeruser?").append(createURL(data)));
				}


				if ((HTML4&&newPage.indexOf("<HEAD><TITLE>Register User</TITLE></HEAD>")==-1)){
					client.getClientInfo().setRegistered(true);
					client.setLoggedIn(true);
				}

			}
			else{ // if the client has attempted to register more than 3 times, he exits the website
				nextPage.setTextContent("exit");
				action.appendChild(nextPage);
				client.setExit(true);
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
			nextLink=new StringBuilder(action.getElementsByTagName("nextPage").item(0).getTextContent());
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
			if(nextURL.indexOf("authToken=")!=-1){
				int start=nextURL.indexOf("&authToken=")+"&authToken=".length();
				int end=nextURL.indexOf("&",start);
				if(end==-1)
					end=nextURL.length();
				nextURL.replace(start, end, client.getClientInfo().getAuthToken().toString());
			}

			if(!nextLink.toString().equals("exit")){
				try{Thread.sleep(getThinkTime());}
				catch(InterruptedException e){
					client.setExit(true);
					return null;
				}

				if (HTML4){
					newPage=doSubmit(nextURL,data);
				}
				else{
					newPage=openHTML5PageWithRedirect(nextURL.append("?").append(createURL(data)));
				}
			}else{
				client.setExit(true);
			}
		}

		return newPage;
		}catch(Exception e ){
			e.printStackTrace();
			return null;
		}
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
			if (client.isFormFiller()==false){
				thinkTime+=bonusCharacters/client.getTypingSpeed();
				for (String m:changedEntries)
					thinkTime+=(int)expDist(initialThinkTime)+(int)((data.get(m).length())/client.getTypingSpeed())*client.getLoginAttempts();
			}
			else{
				thinkTime+=(int)expDist(initialThinkTime+4000);
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
