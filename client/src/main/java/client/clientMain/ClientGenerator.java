package client.clientMain;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.awt.Image;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLEncoder;
import java.net.UnknownHostException;

import java.lang.StringBuffer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import client.Tools.*;

/**
 * Generates clients
 * Stores important distributions for all clients
 * @author Andrew Fox
 *
 */

public class ClientGenerator extends Thread{

	private Random rand=new Random();		// random seed generator
	private double newClientProb=0.1;		// the probability that the next client activated in the system is a new client
	private ArrayList<ClientInfo>activeClients=new ArrayList<ClientInfo>();		// ArrayList of the client info of the clients active in the system
	private ArrayList<ClientInfo>inactiveClients=new ArrayList<ClientInfo>();	// ArrayList of the client info of the clients inactive in the system
	private ArrayList<Client>aC=new ArrayList<Client>();	// ArrayList of the active clients
	private Stats stats=new Stats(20000,5,10000,this);		// creates histogram of response times, 10 second period
	private Histogram thinkTimeHist;						// histogram of think times
	private ArrayList<Histogram> pageRTHistograms=new ArrayList<Histogram>();	// histograms of response times for each page
	private ClientSessionStats csd;
	private int activeUsers=0;								// number of active users in the system
	private boolean exit=false;								// if the system is to exit
	ExecutorService threadExecutorC = Executors.newCachedThreadPool();		// thread pool for the clients
	ScheduledExecutorService threadExecutorRC = Executors.newScheduledThreadPool(10) ;		// thread pool for RunClients
	private ArrayList<Long> hotItems=new ArrayList<Long>();			// list of "hot" items in CMART
	private ChangePopularity cp=new ChangePopularity(40,470000,300000);
	private CMARTurl cmarturl;				// set of URLs for C-MART application
	private int origClientNum=0;			// number of inactive clients initially loaded from C-MART
	private Timer timer=new Timer();		// timer for creating clients
	private long startTime;					// time client generator started
	private Document xmlDocument;			// xml document indicating which clients start and when
	private Element root;					// the root node of xmlDocument
	private long clientIndex=0;				// index for each created client - used for xmlDocument tracking
	private Document readXmlDocument;		// xml document of all clients to be read in for repeated run

	/**
	 * Runs a client on the website
	 * @author Andrew Fox
	 *
	 */
	private class RunClient extends Thread{
		ClientInfo clientToRun=null;		// information of the client to run
		CMARTurl cmarturl=RunSettings.getCMARTurl();
		StringBuffer startPage=new StringBuffer(cmarturl.getAppURL()).append("/index"); 	// page the client starts on (home page)
		ClientGenerator cg;					// the client generator that created the client

		public void run(){
			if(RunSettings.isRepeatedRun()){
				addActiveClient(clientToRun);
				addUser();
			}
			if(RunSettings.isHTML4()==false)	// if HTML5
				startPage.append(".html");		// switch to HTML5 home page
			if (clientToRun!=null){				// if the client info exists
				Client p;						// create the client
				try {
					//start the client running in system
					System.out.println("Starting Client: "+clientToRun.getUsername()+", Number of Active Clients = "+activeClients.size());
					p = new Client(clientToRun,startPage,cmarturl,cg);
					synchronized(aC){
						aC.add(p);
					}
					threadExecutorC.execute(p);
					//p.start();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
		private RunClient(ClientGenerator cg){
			this.cg=cg;							// client generator creating the client
			// creates a client node in the xmlDocument for the client about to be run
			Element client=xmlDocument.createElement("client");
			Element child=xmlDocument.createElement("clientIndex");
			long index=getClientIndex();
			child.setTextContent(Long.toString(index));
			client.appendChild(child);
			child=xmlDocument.createElement("runTime");
			child.setTextContent(Long.toString(new Date().getTime()-startTime));
			client.appendChild(child);
			Element cI=xmlDocument.createElement("clientInfo");
			if(rand.nextDouble()>newClientProb){	// using a client whose info already exists
				if (inactiveClients.isEmpty()==false){		// if there are existing clients available
					// pick a random client from the inactive set

					this.clientToRun=getInactiveClient();

					if(this.clientToRun==null)
						this.clientToRun=new ClientInfo();
					clientToRun.setClientIndex(index);
					addActiveClient(this.clientToRun);
					//		inactiveToActive(this.clientToRun);		// remove the client as an inactive client and add it to the active set
				}
				else{									// if no existing clients are available, create a new one
					this.clientToRun=new ClientInfo();
					clientToRun.setClientIndex(index);
					addActiveClient(this.clientToRun);
				}
			}
			else{		// generating a new client
				this.clientToRun=new ClientInfo();
				clientToRun.setClientIndex(index);
				addActiveClient(this.clientToRun);			// add the new client to the active set
			}
			/**
			 * Puts client information into xmlDocument
			 */
			child=xmlDocument.createElement("userID");
			child.setTextContent(clientToRun.getUserID().toString());
			cI.appendChild(child);
			child=xmlDocument.createElement("username");
			child.setTextContent(clientToRun.getUsername().toString());
			cI.appendChild(child);
			child=xmlDocument.createElement("password");
			child.setTextContent(clientToRun.getPassword().toString());
			cI.appendChild(child);
			child=xmlDocument.createElement("registered");
			child.setTextContent(Boolean.toString(clientToRun.isRegistered()));
			cI.appendChild(child);
			Element cache=xmlDocument.createElement("imageCache");
			for(Entry<String,Image> e: clientToRun.getImageCache().entrySet()){
				child=xmlDocument.createElement("image");
				child.setTextContent(e.getKey());
				cache.appendChild(child);
			}
			cI.appendChild(cache);
			cache=xmlDocument.createElement("jscssCache");
			for(Entry<String,StringBuffer> e: clientToRun.getJscssCache().entrySet()){
				child=xmlDocument.createElement("jscss");
				child.setTextContent(e.getKey());
				cache.appendChild(child);
			}
			cI.appendChild(cache);
			cache=xmlDocument.createElement("HTML5Cache");
			for(Entry<String,StringBuffer> e: clientToRun.getHTML5Cache().entrySet()){
				child=xmlDocument.createElement("html5");
				child.setTextContent(e.getKey());
				cache.appendChild(child);
			}
			cI.appendChild(cache);

			client.appendChild(cI);
			root.appendChild(client);
		}

		/**
		 * RunClient for repeated runs
		 * @param cg - Client generator
		 * @param client - Node of the client from readXmlDocument that is to be run
		 */
		private RunClient(ClientGenerator cg, Node client){
			this.cg=cg;
			this.clientToRun=new ClientInfo();
			// Sets the client info according to that in the readXmlDocument
			clientToRun.setClientIndex(Long.parseLong(((Element)client).getElementsByTagName("clientIndex").item(0).getTextContent()));
			Element clientInfo=(Element)((Element)client).getElementsByTagName("clientInfo").item(0);
			clientToRun.setUserID(new StringBuffer(clientInfo.getElementsByTagName("userID").item(0).getTextContent()));
			clientToRun.setUsername(new StringBuffer(clientInfo.getElementsByTagName("username").item(0).getTextContent()));
			clientToRun.setPassword(new StringBuffer(clientInfo.getElementsByTagName("password").item(0).getTextContent()));
			clientToRun.setRegistered(Boolean.parseBoolean(clientInfo.getElementsByTagName("registered").item(0).getTextContent()));
		}

	}

	public void run(){
		stats.start();		// start collecting response time data
		cp.start();			// schedules popularity of different items to change
		csd=new ClientSessionStats();

		startTime=new Date().getTime();		// sets the time the client generator was started

		if(RunSettings.isRepeatedRun()==false){
			// schedule changes in the rate of client generation and peak user load
			for (Entry<Long,Integer> e:RunSettings.getChangeStableUsers().entrySet()){
				ChangeStableUsers csu=new ChangeStableUsers(e.getValue());
				timer.schedule(csu,new Date(startTime+e.getKey()*1000));
			}
			for (Entry<Long, Double> e:RunSettings.getChangeMeanClientsPerMinute().entrySet()){
				ChangeMeanClientsAdded cmca=new ChangeMeanClientsAdded(e.getValue());
				timer.schedule(cmca,new Date(startTime+e.getKey()*1000));
			}

			//		ArrayList<Timer>peakusersTimer=new ArrayList<Timer>();
			//		for(int i=0;i<RunSettings.getChangePeakUsersSlope().size();i++)
			//			peakusersTimer.add(new Timer());
			for(Long m:RunSettings.getChangePeakUsersSlope().keySet()){
				long end=-1;
				if(m!=RunSettings.getChangePeakUsersSlope().lastKey())
					end=RunSettings.getChangePeakUsersSlope().higherKey(m)*1000+startTime;
				ChangePeakUsersSlope cpus=new ChangePeakUsersSlope(RunSettings.getChangePeakUsersSlope().get(m),end,this);
				timer.schedule(cpus,new Date(startTime+m*1000));
			}

			/**
			 * Creates random bursts in client generation
			 * Burst schedules on top of existing generation method
			 * Defined by 3 time periods, generation increase, plateau, decrease to original level
			 */
			if(RunSettings.isAllowBursts()){
				long endTime=RunSettings.getTimeToRun()*60000+startTime;
				int numSpikes=getPoisson(((double)RunSettings.getTimeToRun())/15.);
				double lambda;
				double timeLeft=RunSettings.getTimeToRun()*60000;
				double t0=startTime;
				long currentTime=startTime;
				for(int i=0;i<numSpikes;i++){
					do{
						lambda=-timeLeft/Math.log(0.01);
						t0+=expDist(lambda);
					}while((t0+currentTime)>endTime);

					timeLeft=endTime-t0;
					double t3=expDist(2*60000)+t0;
					double t1=rand.nextDouble()*(t3-t0)+t0;
					double t2=rand.nextDouble()*(t3-t1)+t1;

					double M=getPareto(3.);

					CreateSpike cs=new CreateSpike((long)t0,(long)t1,(long)t2,(long)t3,M,this,timer);
					timer.schedule(cs,0);
				}
			}

			if(RunSettings.isStaticUserload()==false){
				while(exit==false){
					int delay=0;	// delay between generating clients

					// delay is an exponential function dependent on the average number of clients to be generated per minute
					if (RunSettings.getMeanClientsPerMinute()!=0)
						delay=(int)getPareto(60000/RunSettings.getMeanClientsPerMinute());

					try{Thread.sleep(delay);} 			// wait for the specified delay
					catch(InterruptedException e){
						e.printStackTrace();
					}
					//				synchronized(this){
					//					while(getUsers()>=RunSettings.getStableUsers()){		// if there are more users than the desired plateau
					//						try {
					//							wait();											// wait for a user to leave the system
					//						} catch (InterruptedException e) {
					//							e.printStackTrace();
					//						}
					//					}
					if(exit==false){
						RunClient rc=new RunClient(this);		// create a new run client
						addUser();								// indicate that a user has been added to the system
						threadExecutorRC.execute(rc);
					}
					//				}
				}
			}
			else{
				double rampupTime=RunSettings.getRampupTime()*1000;
				while(exit==false){
					if(new Date().getTime()<(startTime+rampupTime)){
						try{Thread.sleep((long)(rampupTime/RunSettings.getStableUsers()));} 			// wait for the specified delay
						catch(InterruptedException e){
							e.printStackTrace();
						}
					}
					synchronized(this){
						while(getUsers()>=RunSettings.getStableUsers()){		// if there are more users than the desired plateau
							try {
								wait();											// wait for a user to leave the system
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
					if(exit==false){
						RunClient rc=new RunClient(this);		// create a new run client
						addUser();								// indicate that a user has been added to the system
						threadExecutorRC.execute(rc);
						if(new Date().getTime()<(startTime+rampupTime)){
							while(getUsers()<RunSettings.getStableUsers()/rampupTime*(new Date().getTime()-startTime)){
								RunClient nrc=new RunClient(this);	
								addUser();	
								threadExecutorRC.execute(nrc);
							}
						}
						if((inactiveClients.size()-100)>origClientNum)
							cutInactiveClients();
					}


				}
			}
		}else{	// repeated run
			NodeList listOfClients=readXmlDocument.getElementsByTagName("client");
			// Schedules all clients to run at the same time as in the previous run
			for(int i=0;i<listOfClients.getLength();i++){
				if(exit==true)
					break;
				Node client=listOfClients.item(i);
				long runTime=Long.parseLong(((Element)client).getElementsByTagName("runTime").item(0).getTextContent());
				RunClient rc=new RunClient(this,client);		// create a new run client
				threadExecutorRC.schedule(rc, runTime-(new Date().getTime()-startTime),TimeUnit.MILLISECONDS);
			}
		}
		timer.cancel();			// cancel any schedule when the system is told to exit
	}

	public ClientGenerator(CMARTurl cmarturl){
		this.cmarturl=cmarturl;

		try {
			// prepare the xml document to record when clients are created
			xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			root = xmlDocument.createElement("ClientGenerator");
			xmlDocument.appendChild(root);
			Element child=xmlDocument.createElement("HTML4");
			child.setTextContent(Boolean.toString(RunSettings.isHTML4()));
			root.appendChild(child);

			// if the run is repeated and read from XML files
			if(RunSettings.isRepeatedRun()){
				readXmlDocument=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(new StringBuffer(RunSettings.getRepeatedXmlFolder()).append("clientGenerator.xml").toString()));
			}

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}


		// more database writes = more new clients (unregistered)
		if(RunSettings.getWorkloadType()==1)
			newClientProb=0.05;
		else if(RunSettings.getWorkloadType()==2)
			newClientProb=0.1;
		else if(RunSettings.getWorkloadType()==3)
			newClientProb=0.15;

		if (RunSettings.isOutputThinkTimes()==true)			// if think times should be output
			setThinkTimeHist(new Histogram(600000,100));	// create a histogram to track think times
		for (int i=0;i<=20;i++)
			pageRTHistograms.add(new Histogram(20000,2));
		if(RunSettings.isOutputSiteData()){
			try {
				new SiteData(cmarturl).collectStats();				// clears the data from the statistics page
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			populateUsers();
			origClientNum=inactiveClients.size();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}


	}

	/**
	 * Loads already registered users from C-MART
	 * @throws UnsupportedEncodingException
	 */
	public void populateUsers() throws UnsupportedEncodingException{
		StringBuffer ret;		// returned page from C-MART
		int pageLimit=600;		// highest page number which can be loaded (25 users per page)
		int comma;				// index of commas (returned page is in CSV form)
		ArrayList<String> userInfo=new ArrayList<String>();		// user info of clients loaded
		ArrayList<Long>chosenPages=new ArrayList<Long>();		// tracks loaded user pages so there are no duplicates
		long pageNo;			// page number of page to be opened
		HashMap<String,StringBuffer> data=new HashMap<String,StringBuffer>();		// data for the URL of the page to open

		for(int k=0;k<31;k++){
			do{
				pageNo=rand.nextInt(pageLimit);
			}while(chosenPages.contains(pageNo)==true);
			chosenPages.add(pageNo);
			data.clear();
			data.put("pageNo",new StringBuffer(Long.toString(pageNo)));
			data.put("itemsPP",new StringBuffer(Integer.toString(25)));
			if(k==0){
				data.put("totalUsers",new StringBuffer(Integer.toString(1)));
			}

			try{Thread.sleep(100);}
			catch(InterruptedException e){
			}

			ret=openPopulateUser(new StringBuffer(cmarturl.getAppURL()).append("/getusers?").append(createURL(data)));
			if(k==0){
				try{
				pageLimit=(int)(Math.floor((Long.parseLong(ret.toString())-1)/25)-1);
				}catch(Exception e){
					System.err.println("Warning: Could not load any clients from database");
					break;
				}
			}
			else{
				while(ret.indexOf(",")!=-1){
					for(int i=0;i<15;i++){
						comma=ret.indexOf(",");
						userInfo.add(ret.substring(0,comma).trim());
						ret.delete(0,comma+1);
					}
					ClientInfo clientInfo=new ClientInfo(userInfo.get(0),userInfo.get(1),userInfo.get(2),userInfo.get(3),userInfo.get(4),userInfo.get(5),userInfo.get(10),userInfo.get(11),userInfo.get(12),userInfo.get(13));
					boolean alreadyAdded=false;
					for(ClientInfo c:inactiveClients){
						if(c.getUserID().toString().equals(userInfo.get(0))){
							alreadyAdded=true;
							break;
						}
					}
					if(alreadyAdded==false)
						addInactiveClient(clientInfo);
					userInfo.clear();
				}
			}


		}
	}

	/**
	 * Creates a URL query in UTF-8 format out of the information from a HashMap
	 * @param data - map of the data needed to be turned into a query string
	 * @return The query in UTF-8 form
	 * @throws UnsupportedEncodingException
	 */
	private String createURL(HashMap<String, StringBuffer> data) throws UnsupportedEncodingException{
		StringBuffer content = new StringBuffer();
		int i=0;
		for(Entry<String,StringBuffer> e:data.entrySet()){
			if(i!=0)
				content.append("&");
			content.append(e.getKey()).append("=").append(URLEncoder.encode(e.getValue().toString(), "UTF-8"));
			i++;
		}
		return content.toString();
	}
	/**
	 * Opens a url and returns the String used to populate the inactive users
	 * @param urlString - url to open
	 * @return CSV of the returned users
	 */
	private StringBuffer openPopulateUser(StringBuffer urlString){
		StringBuffer ret = new StringBuffer();		// the source code of the page
		String inputLine;	// each line being read in
		String urlStringS=urlString.toString().replace(" ", "%20");
		if(RunSettings.isVerbose())System.out.println("PopulateUser "+urlString);

		Socket socket;
		try {
			socket = new Socket(cmarturl.getIpURL().toString(),cmarturl.getAppPort());


			PrintStream out=new PrintStream(socket.getOutputStream());		// opens an output PrintStream to send the HTTP request
			out.println(new StringBuffer("GET ").append(urlStringS).append(" HTTP/1.0\r\n"));
			out.flush();

			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));	// opens a BufferedReader to read the response of the HTTP request

			while((inputLine=br.readLine())!=null){
				ret.append(inputLine);	// creates the response
			}
			// deletes the header information of the HTTP response
			if(ret.indexOf("Connection: close")!=-1)
				ret.delete(0, ret.indexOf("Connection: close")+"Connection: close".length());

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ret;
	}

	/**
	 * Randomly assigns itemIDs to be artificially popular.
	 * Rolling list of popular items.
	 * @author Andrew Fox
	 *
	 */
	private class ChangePopularity extends Thread{
		long maxItemId;
		long minItemId;

		public ChangePopularity(int numItems, long maxItemId,long minItemId){
			this.maxItemId=maxItemId;
			this.minItemId=minItemId;

			long itemId;
			for (int i=0;i<numItems;i++){
				do{
					itemId=(long)rand.nextInt((int)(maxItemId-minItemId))+minItemId;
				}while(hotItems.contains(itemId));
				hotItems.add(itemId);
			}
		}

		public void run(){
			long itemId;
			do{
				try{Thread.sleep((long) expDist(120000));}
				catch(InterruptedException e){
					break;
				}
				do{
					itemId=(long)(rand.nextInt((int)(maxItemId-minItemId))+minItemId);
				}while(hotItems.contains(itemId));
				hotItems.remove(0);
				hotItems.add(itemId);
			}while(exit==false);
		}
	}

	/**
	 * Indicates a connection has been added
	 */
	private void addUser(){
		activeUsers+=1;
	}

	/**
	 * Indicates a connection has been removed and notifies the parent that the connection is finished
	 */
	private synchronized void delUser(){
		activeUsers-=1;
		notify();
	}
	/**
	 * Gets the count of the number of connections
	 * @return the number of active connections
	 */
	private synchronized int getUsers(){
		return activeUsers;
	}


	/**
	 * Removes a client info from the active set and calls the delUser function to decrease the number of active users
	 * @param info	- client to be removed
	 */
	public void removeActiveClient(ClientInfo info){
		activeClients.remove(activeClients.indexOf(info));
		delUser();
	}
	/**
	 * Removes a client info from the inactive set
	 * @param info - client info to be removed
	 */
	public void removeInactiveClient(ClientInfo info){
		inactiveClients.remove(inactiveClients.indexOf(info));
	}
	/**
	 * Gets a client info from the inactive set
	 * @param info - client info to be removed
	 */
	public ClientInfo getInactiveClient(){
		ClientInfo clientInfo=null;
		synchronized (inactiveClients){
			if(inactiveClients.size()>0){
				int client=rand.nextInt(inactiveClients.size());
				clientInfo=inactiveClients.remove(client);
			}
		}
		return clientInfo;
	}
	/** 
	 * Adds a client info to the active set
	 * @param info - client info to be added
	 */
	public void addActiveClient(ClientInfo info){
		activeClients.add(info);
	}
	/**
	 * Adds a client info to the inactive set
	 * @param info - client info to be added
	 */
	public void addInactiveClient(ClientInfo info){
		inactiveClients.add(info);
	}

	/**
	 * Eliminates inactiveClients to save memory space
	 * Removes 100 inactiveClients
	 */
	public void cutInactiveClients(){
		synchronized(inactiveClients){
			if(inactiveClients.size()>0)
				for (int i=0;i<100;i++){
					inactiveClients.remove(rand.nextInt(inactiveClients.size()));
				}
		}
	}

	/**
	 * Returns a list of the active clients
	 * @return
	 */
	public ArrayList<ClientInfo> getActiveClients() {
		return activeClients;
	}
	/**
	 * Returns a list of the inactive clients
	 * @return inactiveClients
	 */
	public ArrayList<ClientInfo> getInactiveClients() {
		return inactiveClients;
	}
	/**
	 * Moves a client info from the inactive to active set
	 * @param info client info to be moved
	 */
	public synchronized void inactiveToActive(ClientInfo info){
		inactiveClients.remove(inactiveClients.indexOf(info));
		activeClients.add(info);
	}
	/**
	 * Moves a client info from the active to inactive set
	 * @param info - ClientInfo of the client to move
	 * @param c - the client to move
	 */
	public void activeToInactive(ClientInfo info,Client c){
		synchronized(aC){
			if(aC.indexOf(c)!=-1)
				aC.remove(aC.indexOf(c));
		}
		synchronized(activeClients){
			if(activeClients.indexOf(info)!=-1)
				activeClients.remove(activeClients.indexOf(info));
		}
		synchronized(inactiveClients){
			inactiveClients.add(info);
		}
		delUser();
	}
	/**
	 * Moves a client and their information from the active ArrayList to being removed entirely
	 * Caused by an error so that client is entirely removed
	 * @param info
	 * @param c
	 */
	public synchronized void activeToRemove(ClientInfo info,Client c){
		if(aC.indexOf(c)!=-1)
			aC.remove(aC.indexOf(c));
		if(activeClients.indexOf(info)!=-1)
			activeClients.remove(activeClients.indexOf(info));
		delUser();
	}

	/**
	 * Returns a random value from a Poisson distribution with a given mean
	 * @param mean - mean of the Poisson distribution
	 * @return Random value from Poisson distribution
	 */
	private int getPoisson(double mean){
		double L=Math.exp(-mean);
		int k=0;
		double p=1;
		do{
			k+=1;
			p*=rand.nextDouble();
		}while(p>L);
		return (k-1);
	}

	/**
	 * Returns a random value from a Pareto distribution
	 * @param avg - average of the distribution
	 * @return
	 */
	public double getPareto(double avg){
		double alpha=1.5;
		double xm=avg*(alpha-1)/alpha;
		return(xm/(Math.pow(rand.nextDouble(),1/alpha)));
	}

	/**
	 * Returns the think time histogram distribution
	 * @return thinkTimeHist
	 */
	public Histogram getThinkTimeHist() {
		return thinkTimeHist;
	}

	/**
	 * Sets the think time histogram distribution
	 * @param thinkTimeHist
	 */
	public void setThinkTimeHist(Histogram thinkTimeHist) {
		this.thinkTimeHist = thinkTimeHist;
	}
	/**
	 * Returns the response time stats
	 * @return
	 */
	public  Stats getStats(){
		return stats;
	}

	/**
	 * Randomly selects a number from an exponential distribution
	 * @param mean - mean of the distribution
	 * @return Random Number from distribution
	 */
	public double expDist(double mean) {
		return (-Math.log(1 - rand.nextDouble()) * mean);
	}

	/**
	 * Returns the Client Session Stats
	 * @return
	 */
	public ClientSessionStats getClientSessionStats(){
		return csd;
	}

	/**
	 * Exits the system
	 * Gracefully exits all clients
	 * Outputs final statistics on experiment
	 */
	public void exitAllClients(){
		System.out.println("EXITING SYSTEM");
		if(RunSettings.isOutputThinkTimes())
			new OutputThinkTime(thinkTimeHist,RunSettings.getOutputSiteDataFile());
		cp.interrupt();
		this.exit=true;
		threadExecutorC.shutdown();				// shuts down the client generation process
		threadExecutorRC.shutdown();
		int i=0;
		while(activeClients.size()>0&&i<20){	// while there are still active clients and 20 interrupt attempts have been tried
			i++;

			for (int m=0;m<aC.size();m++){			// tells all clients to exit after next request
				aC.get(m).setExit(true);
				aC.get(m).interrupt();				// if the client is thinking
				try{Thread.sleep(25);} 				// wait for the specified delay
				catch(InterruptedException e){
					e.printStackTrace();
				}
			}

			//stops taking stats once there are no more clients in the system

			try{Thread.sleep(3500);} 			// wait for the specified delay
			catch(InterruptedException e){
				e.printStackTrace();
			}
			threadExecutorC.shutdown();			// reattempt to shutdown client threads
			threadExecutorRC.shutdown();
		}
		cp.interrupt();


		threadExecutorC.shutdownNow();			// forcibly shutdown any remaining client threads
		threadExecutorRC.shutdownNow();
		System.out.println("ALL CLIENTS SHUTDOWN");
		if(RunSettings.isOutputSiteData()){
			try {
				new SiteData(RunSettings.getOutputSiteDataFile(),cmarturl).collectStats();		// output data from Statistics page to csv
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		new OutputPageRT(pageRTHistograms,RunSettings.getOutputSiteDataFile());
		stats.exitStats();		// exit stats - no more collecting response times
		try {
			csd.output(RunSettings.getOutputSiteDataFile().toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

		outputClientGeneratorXML();

		timer.cancel();
	}

	/**
	 * Schedules a change in the value of the stable users
	 * @author Andrew Fox
	 *
	 */
	private static class ChangeStableUsers extends TimerTask{
		int stableUsers;		// new number of stable users
		private ChangeStableUsers(int stableUsers){
			this.stableUsers=stableUsers;
		}
		public void run(){
			System.out.println("Changing stableUsers to "+stableUsers);
			RunSettings.setStableUsers(stableUsers);
		}

	}

	/**
	 * Schedules a change in the value of the mean clients added per minute
	 * @author Andrew Fox
	 *
	 */
	private static class ChangeMeanClientsAdded extends TimerTask{
		double meanClients;		// new value of mean clients per minute to add
		private ChangeMeanClientsAdded(double meanClients){
			this.meanClients=meanClients;
		}
		public void run(){
			System.out.println("Changing meanClientsPerMinute to "+meanClients);
			RunSettings.setMeanClientsPerMinute(meanClients);
		}

	}

	/**
	 * Changes the of the peak users in the system
	 * @author Andrew Fox
	 *
	 */
	private static class ChangePeakUsersSlope extends TimerTask{
		double newSlope;		// in clients added per ms
		long endTime;
		ClientGenerator cg;
		private ChangePeakUsersSlope(double newSlope, long endTime,ClientGenerator cg){
			this.newSlope=newSlope/60000;
			this.endTime=endTime;
			this.cg=cg;
		}
		public void run(){
			System.out.println("Increasing the Peak User value by "+newSlope*60000+" clients per minute");
			long startTime=new Date().getTime();
			int startValue=RunSettings.getStableUsers();
			if(endTime!=-1){
				while(new Date().getTime()<endTime){
					RunSettings.setStableUsers(startValue+(int)(((double)(new Date().getTime()-startTime))*newSlope));
					try{Thread.sleep(2000);} 	
					catch(InterruptedException e){
						e.printStackTrace();
					}
				}
			}
			else{
				while(cg.exit==false){
					RunSettings.setStableUsers(startValue+(int)(((double)(new Date().getTime()-startTime))*newSlope));
					try{Thread.sleep(2000);} 	
					catch(InterruptedException e){
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Creates a spike in the number of clients
	 * @author Andrew Fox
	 *
	 */
	private static class CreateSpike extends TimerTask{
		long t0;
		long t1;
		long t2;
		long t3;
		double M;
		ClientGenerator cg;
		Timer timer;

		/**
		 * Creates a burst in clients for the closed loop client
		 * @param t0 - time spike starts
		 * @param t1 - time spike peaks
		 * @param t2 - time of the end of the peak
		 * @param t3 - time of end of spike
		 * @param M - relative increase in workload volume
		 */
		public CreateSpike(long t0,long t1,long t2,long t3,double M,ClientGenerator cg,Timer timer){
			this.t0=t0;
			this.t1=t1;
			this.t2=t2;
			this.t3=t3;
			this.M=M;
			this.cg=cg;
			this.timer=timer;
		}

		public void run(){
			System.out.println("Creating a volume spike");

			if(RunSettings.isStaticUserload()==true){
				double origUsers=(double)RunSettings.getStableUsers();
				double increaseSlope=(origUsers*(M-1.))/(((double)(t1-t0))/60000);
				ChangePeakUsersSlope cpus=new ChangePeakUsersSlope(increaseSlope,t1,cg);
				timer.schedule(cpus,t0);
				cpus=new ChangePeakUsersSlope(0,t2,cg);
				timer.schedule(cpus,t1);
				double decreaseSlope=(origUsers*(1.-M))/(((double)(t3-t2))/60000);
				cpus=new ChangePeakUsersSlope(decreaseSlope,t3,cg);
				timer.schedule(cpus,t2);
			}
			else{
				double origStableUsers=(double)RunSettings.getStableUsers();
				double origUsers=(double)cg.aC.size();
				double origClientsAdded=(double)RunSettings.getMeanClientsPerMinute();


				ChangeStableUsers csu=new ChangeStableUsers((int)(M*origStableUsers));
				double increaseRate=(M-1.)*(origUsers)/((double)(t1-t0))+origClientsAdded;
				ChangeMeanClientsAdded cmca=new ChangeMeanClientsAdded((int)increaseRate);
				timer.schedule(csu,t0);
				timer.schedule(cmca,t0);

				cmca=new ChangeMeanClientsAdded((int)(origClientsAdded));
				timer.schedule(cmca,t1);

				double decreaseRate=(1.-M)*(origUsers)/((double)(t3-t2))+origClientsAdded;
				cmca=new ChangeMeanClientsAdded((int)decreaseRate);
				timer.schedule(cmca,t2);

				csu=new ChangeStableUsers((int)(origStableUsers));
				cmca=new ChangeMeanClientsAdded((int)(origClientsAdded));
				timer.schedule(csu,t3);
				timer.schedule(cmca,t3);

			}
		}
	}

	/**
	 * Returns a list of the Hot Item IDs
	 * @return
	 */
	public ArrayList<Long> getHotItems(){
		return this.hotItems;
	}

	/**
	 * Adds the response time to a histogram of page response times
	 * @param pageType the page type number
	 * @param RT response time of the page
	 */
	public void addPageRT(int pageType, long RT){
		pageRTHistograms.get(pageType).add(RT);
	}

	/**
	 * Returns the set of CMART urls
	 * @return
	 */
	public CMARTurl getCMARTurl(){
		return this.cmarturl;
	}

	/**
	 * Returns the clientIndex and increments the index by 1
	 * @return clientIndex
	 */
	private synchronized long getClientIndex(){
		clientIndex++;
		return (clientIndex-1);
	}

	/**
	 * Outputs the Client Generator XML when the client generator is exiting
	 */
	private void outputClientGeneratorXML(){
		if(RunSettings.isRepeatedRun()==false){
			try{
				FileWriter fstreamA = new FileWriter(new StringBuffer(RunSettings.getRepeatedXmlFolder()).append("clientGenerator.xml").toString(),true);
				BufferedWriter out = new BufferedWriter(fstreamA);

				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");

				//initialize StreamResult with File object to save to file
				StreamResult result = new StreamResult(new StringWriter());
				DOMSource source = new DOMSource(xmlDocument);
				transformer.transform(source, result);

				String xmlString = result.getWriter().toString();
				out.write(xmlString);

				out.close();
			}catch(Exception e){
				e.printStackTrace();
				System.err.println("Could not output page response time data");
			}
		}
	}


}
