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
 * Update User Page
 * @author Andrew Fox
 *
 */

public class UpdateUserPage extends Page{

	boolean updateEmail=false;
	boolean updatePassword=false;
	boolean updateAddress=false;
	HashMap<String, StringBuilder>data=new HashMap<String,StringBuilder>();
	double pageRTFactor=1.1;
	int bonusCharacters=0;

	public UpdateUserPage(Page page){
		super(page.url,page.html,page.client,page.pageType,page.pageOpenTime,page.lastPageType,page.lastURL);
		client.pageSpecificRT(pageRTFactor);	// change RT threshold depending on page type
		if(HTML4)
			searchData=getFormData("search");
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
		StringBuilder nextPage=new StringBuilder();		// the HTTP response after submitting a request
		if(RunSettings.isRepeatedRun()==false){
			action=xmlDocument.createElement("action");
			action.setAttribute("id",client.getActionNum());
			Element currentPage=xmlDocument.createElement("currentPage");
			currentPage.setTextContent(Integer.toString(pageType));
			action.appendChild(currentPage);
			Element nextPagee=xmlDocument.createElement("nextPage");
			nextPagee.setTextContent("update");
			action.appendChild(nextPagee);
			request=xmlDocument.createElement("request");


			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = null;
			if(!HTML4)
				node=mapper.readValue(html.toString(), JsonNode.class);		
			String formAction=null;;		// action field of the form
			if(rand.nextDouble()<0.3){
				updateEmail=true;
			}
			if(rand.nextDouble()<0.3){
				updatePassword=true;
			}
			if(rand.nextDouble()<0.3){
				updateAddress=true;
				client.getClientInfo().createAddress();
			}
			if(HTML4){		// gets data in the form
				int start=html.indexOf("<form name=\"register_user\" action=\"")+("<form name=\"register_user\" action=\"").length();
				int end=html.indexOf("\" class=\"nice\"",start);
				formAction=html.substring(start,end);
				data=getFormData(formAction);
			}
			else{
				data.put("password1", new StringBuilder(node.get("user").get("password").getTextValue()));
				data.put("password2", new StringBuilder(node.get("user").get("password").getTextValue()));
				data.put("email1",new StringBuilder(node.get("user").get("email").getTextValue()));
				data.put("email2",new StringBuilder(node.get("user").get("email").getTextValue()));
				data.put("firstname",new StringBuilder(node.get("user").get("firstname").getTextValue()));
				data.put("lastname",new StringBuilder(node.get("user").get("lastname").getTextValue()));
				data.put("street",new StringBuilder(node.get("address").get("street").getTextValue()));
				data.put("town",new StringBuilder(node.get("address").get("town").getTextValue()));
				data.put("zip",new StringBuilder(node.get("address").get("zip").getTextValue()));
				data.put("state",new StringBuilder(node.get("address").get("state").getTextValue()));

				data.put("userID",new StringBuilder(client.getClientInfo().getHTML5Cache().get("userID")));
				data.put("authToken",new StringBuilder(client.getClientInfo().getHTML5Cache().get("authToken")));

				data.put("useHTML5",new StringBuilder("1"));

			}
			ArrayList<String>changes=new ArrayList<String>();		// determines and fields which need to be updated

			if(verbose)System.out.println(data);
			if(HTML4==true){
				if(html.indexOf("The passwords you entered are different Make sure both of the passwords are the same")!=-1)
					updatePassword=true;
				if(html.indexOf("The email addresses you entered are different Make sure both of the email addresses are the same")!=-1)
					updateEmail=true;
			}
			else{
				for(int i=0;i<node.get("errors").size();i++){
					if(node.get("errors").get(i).get("errorMessage").getTextValue().contains("The passwords you entered are different Make sure both of the passwords are the same"))
						updatePassword=true;
					if(node.get("errors").get(i).get("errorMessage").getTextValue().contains("The email addresses you entered are different Make sure both of the email addresses are the same"))
						updateEmail=true;
				}
			}


			// if the password is to be updated
			if (updatePassword==true){
				long numCharNewPassword=8+rand.nextInt(5);
				StringBuilder newPassword=new StringBuilder();
				for (int i=0;i<numCharNewPassword;i++){
					newPassword.append(((char)(rand.nextInt(78)+48)));
				}
				if (verbose)System.out.println(newPassword);
				client.getClientInfo().setPassword(newPassword);
				data.put("password1", typingError(newPassword));
				data.put("password2",  typingError(newPassword));
				changes.add("password1");
				changes.add("password2");

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

			if (updateEmail==true){
				data.put("email1", typingError(client.getClientInfo().getEmail()));
				data.put("email2",  typingError(client.getClientInfo().getEmail()));
				changes.add("email1");
				changes.add("email2");

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

			changes.addAll(confirmDetails());


			// Think Time
			try{Thread.sleep(getThinkTime(changes));}
			catch(InterruptedException e){
				client.setExit(true);
				return null;
			}

			Element child;
			for (Entry<String,StringBuilder> e:data.entrySet()){
				child=xmlDocument.createElement("data");
				child.setAttribute("name", e.getKey());
				child.setTextContent(e.getValue().toString());
				request.appendChild(child);
			}
			// submits the HTTP request
			if(HTML4){
				child=xmlDocument.createElement("url");
				child.setTextContent(new StringBuilder(client.getCMARTurl().getAppURL()).append("/").append(formAction).toString());
				request.appendChild(child);
				child=xmlDocument.createElement("type");
				child.setTextContent("POST");
				request.appendChild(child);
				nextPage=doSubmit(new StringBuilder(client.getCMARTurl().getAppURL()).append("/").append(formAction),data);
			}
			else{
				child=xmlDocument.createElement("url");
				child.setTextContent(new StringBuilder(client.getCMARTurl().getAppURL()).append("/updateuserdetails").toString());
				request.appendChild(child);
				child=xmlDocument.createElement("type");
				child.setTextContent("GET");
				request.appendChild(child);
				nextPage=openHTML5PageWithRedirect(new StringBuilder(client.getCMARTurl().getAppURL()).append("/updateuserdetails?").append(createURL(data)));
			}

			child=xmlDocument.createElement("thinkTime");
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
			//	StringBuilder nextLink=new StringBuilder(action.getElementsByTagName("nextPage").item(0).getTextContent());
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

			// Think Time
			try{Thread.sleep(getThinkTime(null));}
			catch(InterruptedException e){
				client.setExit(true);
				return null;
			}

			if(HTML4){
				nextPage=doSubmit(nextURL,data);
			}
			else{
				nextPage=openHTML5PageWithRedirect(nextURL.append("?").append(createURL(data)));
			}


		}
		//	client.exit=true;
		return nextPage;

	}

	/**
	 * Confirms that the details given in the form match the actual data
	 * If any data in the form is incorrect it is added to an array list
	 * @return Returns all fields which need to be changed
	 */
	private ArrayList<String> confirmDetails(){
		ArrayList<String>changes=new ArrayList<String>();
		if (data.get("password1").equals(client.getClientInfo().getPassword())==false){
			changes.add(("password1"));
			data.put("password1",typingError(client.getClientInfo().getPassword()));}
		if (data.get("password2").equals(client.getClientInfo().getPassword())==false){
			changes.add(("password2"));
			data.put("password2",typingError(client.getClientInfo().getPassword()));}
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
		if (data.get("email1").equals(client.getClientInfo().getEmail())==false){
			changes.add("email1");
			data.put(("email1"),typingError(client.getClientInfo().getEmail()));}
		if (data.get("email2").equals(client.getClientInfo().getEmail())==false){
			changes.add(("email2"));
			data.put(("email2"),typingError(client.getClientInfo().getEmail()));}
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
		if (data.get("firstname").equals(client.getClientInfo().getFirstName())==false){
			changes.add(("firstname"));
			data.put(("firstname"),typingError(client.getClientInfo().getFirstName()));}
		if (data.get("lastname").equals(client.getClientInfo().getLastName())==false){
			changes.add(("lastname"));
			data.put(("lastname"),typingError(client.getClientInfo().getLastName()));}
		
		if(data.get("street") == null){
			System.err.println("asdas");
		}

		if(client == null){
			System.err.println("asdas");
		}

		if(client.getClientInfo() == null){
			System.err.println("asdas");
		}

		if(client.getClientInfo().getAddress() == null){
			System.err.println("asdas");
		}

		if (data.get("street").equals(client.getClientInfo().getAddress())==false){
			changes.add(("street"));
			data.put(("street"),typingError(client.getClientInfo().getAddress()));}
		if (data.get("town").equals(client.getClientInfo().getCity())==false){
			changes.add(("town"));
			data.put(("town"),typingError(client.getClientInfo().getCity()));}
		if (data.get("zip").equals(client.getClientInfo().getZipcode())==false){
			changes.add(("zip"));
			data.put(("zip"),typingError(client.getClientInfo().getZipcode()));}
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
		if (data.get("state").equals(client.getClientInfo().getUSStateCode())==false){
			changes.add(("state"));
			data.put(("state"),client.getClientInfo().getUSStateCode());}

		return changes;
	}

	/**
	 * Determines the think time before going to the next page
	 * Deducts from the think time the amount of time since the 
	 * page opened until this calculation is performed
	 * @return Think time in ms
	 */
	private int getThinkTime(ArrayList<String>changes){
		int thinkTime=(int)expDist(initialThinkTime);
		if(RunSettings.isRepeatedRun()==false){
			thinkTime+=typingErrorThinkTime;
			thinkTime+=bonusCharacters/client.getTypingSpeed();
			for (int i=0;i<changes.size();i++){
				thinkTime+=(int)(data.get(changes.get(i)).length()/client.getTypingSpeed());
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
