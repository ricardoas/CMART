package client.clientMain;

import java.awt.Image;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import client.Tools.ClientSessionStats;
import client.Tools.Histogram;
import client.Tools.OutputPageRT;
import client.Tools.OutputThinkTime;
import client.Tools.SiteData;
import client.Tools.Stats;

/**
 * Generates clients
 * Stores important distributions for all clients
 * @author Andrew Fox
 *
 */

public class ClientGenerator extends Thread{

	private Random rand;
	private Set<Client> clients;
	private Semaphore semaphore;
	
	
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
	private ChangePopularity cp;
	private CMARTurl cmarturl;				// set of URLs for C-MART application
	private int origClientNum=0;			// number of inactive clients initially loaded from C-MART
	private ScheduledExecutorService timer;
	private long startTime;					// time client generator started
	private Document xmlDocument;			// xml document indicating which clients start and when
	private Element root;					// the root node of xmlDocument
	private long clientIndex=0;				// index for each created client - used for xmlDocument tracking
	private Document readXmlDocument;		// xml document of all clients to be read in for repeated run

	public ClientGenerator(CMARTurl cmarturl) throws ClientGeneratorException {
		this.rand = new Random();
		this.clients = new HashSet<>();
		this.semaphore = new Semaphore(RunSettings.getStableUsers());
		this.cp =new ChangePopularity(40,470000,300000);
		this.cmarturl = cmarturl;
		this.timer = Executors.newSingleThreadScheduledExecutor();
		try {
			// prepare the xml document to record when clients are created
			xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			root = xmlDocument.createElement("ClientGenerator");
			xmlDocument.appendChild(root);
			Element child = xmlDocument.createElement("HTML4");
			child.setTextContent(Boolean.toString(RunSettings.isHTML4()));
			root.appendChild(child);

			// if the run is repeated and read from XML files
			if (RunSettings.isRepeatedRun()) {
				readXmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder()
						.parse(new File(new StringBuilder(RunSettings.getRepeatedXmlFolder()).append("clientGenerator.xml").toString()));
			}

		} catch (IOException | SAXException | ParserConfigurationException e) {
			throw new ClientGeneratorException("Problem creating/reading client xml files", e);
		}

		// more database writes = more new clients (unregistered)
		newClientProb = RunSettings.getWorkloadType().getNewClientProb();

		if (RunSettings.isOutputThinkTimes()) {
			setThinkTimeHist(new Histogram(600000, 100)); 
		}
		for (int i = 0; i <= 20; i++) {
			pageRTHistograms.add(new Histogram(20000, 2));
		}
		if (RunSettings.isOutputSiteData()) {
			new SiteData(cmarturl).collectStats(); 
		}

		try {
			populateUsers();
		} catch (IOException | URISyntaxException e) {
			throw new ClientGeneratorException("Problem populating users from database.", e);
		}
		origClientNum = inactiveClients.size();
	}

	public void run(){
		stats.start(); // start collecting response time data
		cp.start(); // schedules popularity of different items to change
		csd = new ClientSessionStats();

		startTime = System.currentTimeMillis(); 

		if(!RunSettings.isRepeatedRun()){
			// schedule changes in the rate of client generation and peak user load
			for (Entry<Long, Integer> e : RunSettings.getChangeStableUsers().entrySet()) {
				ChangeStableUsers csu = new ChangeStableUsers(e.getValue());
				timer.schedule(csu, startTime + e.getKey() * 1000, TimeUnit.MILLISECONDS);
			}
			for (Entry<Long, Double> e : RunSettings.getChangeMeanClientsPerMinute().entrySet()) {
				ChangeMeanClientsAdded cmca = new ChangeMeanClientsAdded(e.getValue());
				timer.schedule(cmca, startTime + e.getKey() * 1000, TimeUnit.MILLISECONDS);
			}

			//		ArrayList<Timer>peakusersTimer=new ArrayList<Timer>();
			//		for(int i=0;i<RunSettings.getChangePeakUsersSlope().size();i++)
			//			peakusersTimer.add(new Timer());
			for (Long m : RunSettings.getChangePeakUsersSlope().keySet()) {
				long end = -1;
				if (m != RunSettings.getChangePeakUsersSlope().lastKey())
					end = RunSettings.getChangePeakUsersSlope().higherKey(m) * 1000 + startTime;
				ChangePeakUsersSlope cpus = new ChangePeakUsersSlope(RunSettings.getChangePeakUsersSlope().get(m), end, this);
				timer.schedule(cpus, startTime + m * 1000, TimeUnit.MILLISECONDS);
			}

			/*
			 * Creates random bursts in client generation
			 * Burst schedules on top of existing generation method
			 * Defined by 3 time periods, generation increase, plateau, decrease to original level
			 */
			if (RunSettings.isAllowBursts()) {
				long endTime = RunSettings.getTimeToRun() * 60000 + startTime;
				int numSpikes = getPoisson(((double) RunSettings.getTimeToRun()) / 15.);
				double lambda;
				double timeLeft = RunSettings.getTimeToRun() * 60000;
				double t0 = startTime;
				long currentTime = startTime;
				for (int i = 0; i < numSpikes; i++) {
					do {
						lambda = -timeLeft / Math.log(0.01);
						t0 += expDist(lambda);
					} while ((t0 + currentTime) > endTime);

					timeLeft = endTime - t0;
					double t3 = expDist(2 * 60000) + t0;
					double t1 = rand.nextDouble() * (t3 - t0) + t0;
					double t2 = rand.nextDouble() * (t3 - t1) + t1;

					double M = getPareto(3.);

					timer.schedule(new CreateSpike((long) t0, (long) t1, (long) t2, (long) t3, M, this, timer), 0, TimeUnit.MILLISECONDS);
				}
			}

			if(!RunSettings.isStaticUserload()){
				while(!exit){
					try {
						// delay is an exponential function dependent on the average number of clients to be generated per minute
						int delay = RunSettings.getMeanClientsPerMinute()==0? 0:(int)getPareto(60000/RunSettings.getMeanClientsPerMinute());	// delay between generating clients
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					if(!exit){
						RunClient rc = new RunClient(this);
						addUser();
						threadExecutorRC.execute(rc);
					}
				}
			} else{
				double rampupTime=RunSettings.getRampupTime()*1000;
				while(!exit && System.currentTimeMillis() < (startTime + rampupTime)){
					try {
						Thread.sleep((long) (rampupTime / RunSettings.getStableUsers()));
					}catch (InterruptedException e) {
						e.printStackTrace();
					}
					if(!exit){
						try {
							semaphore.acquire();
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						RunClient rc = new RunClient(this);
						addUser();
						threadExecutorRC.execute(rc);
						while(clients.size() < RunSettings.getStableUsers()/rampupTime*(System.currentTimeMillis()-startTime)){
							try {
								semaphore.acquire();
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
							RunClient nrc = new RunClient(this);	
							addUser();	
							threadExecutorRC.execute(nrc);
						}
					}
				}
				System.out.println("OUT OF RAMP UP");
				
				while (!exit) {
					try {
						semaphore.acquire();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}

					if (!exit) {
						RunClient rc = new RunClient(this);
						addUser();
						threadExecutorRC.execute(rc);
						if ((inactiveClients.size() - 100) > origClientNum) {
							cutInactiveClients();
						}
					}
				}
			}

		} else {
			NodeList listOfClients = readXmlDocument.getElementsByTagName("client");
			// Schedules all clients to run at the same time as in the previous
			// run
			for (int i = 0; i < listOfClients.getLength(); i++) {
				if (exit){
					break;
				}
				Node client = listOfClients.item(i);
				long runTime = Long.parseLong(((Element) client).getElementsByTagName("runTime").item(0).getTextContent());
				RunClient rc = new RunClient(this, client); 
				threadExecutorRC.schedule(rc, runTime - (new Date().getTime() - startTime), TimeUnit.MILLISECONDS);
			}
		}
		timer.shutdownNow();			
	}

	/**
	 * Loads already registered users from C-MART
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	public void populateUsers() throws IOException, URISyntaxException{
		ArrayList<Long> chosenPages = new ArrayList<Long>();

		HashMap<String, String> data = new HashMap<>();
		data.put("totalUsers", Integer.toString(1));
		
		long numberOfUsers = Long.parseLong(openPopulateUser(cmarturl.build(cmarturl.getAppURL().append("/getusers?").toString(), data)).toString());
		int pageLimit = (int) (Math.floor((numberOfUsers - 1) / 25) - 1);
		
		for (int k = 0; k < 3; k++) {
			long pageNo;
			do {
				pageNo = rand.nextInt(pageLimit);
			} while (chosenPages.contains(pageNo));
			chosenPages.add(pageNo);
			
			data = new HashMap<>();
			data.put("pageNo", Long.toString(pageNo));
			data.put("itemsPP", Integer.toString(25));

			StringBuilder ret = openPopulateUser(cmarturl.build(cmarturl.getAppURL().append("/getusers?").toString(), data));
			try (Scanner input = new Scanner(ret.toString());) {

				while (input.hasNextLine()) {
					String[] strings = input.nextLine().split(",");
					ClientInfo clientInfo = new ClientInfo(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5], strings[10],
							strings[11], strings[12], strings[13]);
					boolean alreadyAdded = false;
					for (ClientInfo c : inactiveClients) {
						if (c.getUserID().toString().equals(strings[0])) {
							alreadyAdded = true;
							break;
						}
					}
					if (!alreadyAdded) {
						addInactiveClient(clientInfo);
					}
				}
			}
		}
	}

	/**
	 * Opens a url and returns the String used to populate the inactive users
	 * @param uri - url to open
	 * @return CSV of the returned users
	 * @throws IOException 
	 */
	private StringBuilder openPopulateUser(URI uri) throws IOException{

		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.setConnectionManager(new BasicHttpClientConnectionManager());
		try(CloseableHttpClient client = builder.build();
				CloseableHttpResponse response = client.execute(new HttpGet(uri));
				InputStream inputStream = response.getEntity().getContent();
				Scanner scanner = new Scanner(inputStream);
			){
			StringBuilder content = new StringBuilder();
			while(scanner.hasNextLine()){
				content.append(scanner.nextLine());
			}
			return content;
		}
	}

	/**
	 * Indicates a connection has been added
	 */
	private synchronized void addUser(){
		activeUsers++;
	}

	/**
	 * Indicates a connection has been removed and notifies the parent that the connection is finished
	 */
	private synchronized void delUser(){
//		System.out.println("\t\tClientGenerator.delUser()");
		activeUsers--;
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
			if(!inactiveClients.isEmpty()){
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
	@Deprecated
	public ArrayList<ClientInfo> getActiveClients() {
		return activeClients;
	}

	/**
	 * Returns a list of the active clients
	 * @return
	 */
	public int getNumberOfActiveClients() {
		return clients.size();
//		return aC.size();
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
		if(clients.remove(c)){
			semaphore.release();
//			System.err.println(remove+ info.getUserID().toString());
		}
		synchronized(aC){
			if(aC.indexOf(c)!=-1){
//				System.out.println("\t\tClientGenerator.activeToInactive() REMOVED FROM AC");
				aC.remove(aC.indexOf(c));
			}
		}
		synchronized(activeClients){
			if(activeClients.indexOf(info)!=-1){
				activeClients.remove(activeClients.indexOf(info));
//				System.out.println("\t\tClientGenerator.activeToInactive() REMOVED FROM ACTIVECLIENTS " + activeClients.size());
			}
		}
	
		synchronized(inactiveClients){
//			System.out.println("\t\tClientGenerator.activeToInactive() ADDED TO INACTIVE");
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
		if(clients.remove(c)){
			semaphore.release();
//			System.err.println(remove+ info.getUserID().toString());
		}
		if(aC.indexOf(c)!=-1){
//			System.out.println("\t\tClientGenerator.activeToRemove() REMOVED FROM AC");
			aC.remove(aC.indexOf(c));
		}
		if(activeClients.indexOf(info)!=-1){
			activeClients.remove(activeClients.indexOf(info));
//			System.out.println("\t\tClientGenerator.activeToRemove() REMOVED FROM ACTIVECLIENTS " + activeClients.size());
		}
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
		if(RunSettings.isOutputThinkTimes()){
			new OutputThinkTime(thinkTimeHist,RunSettings.getOutputSiteDataFile()).outputThinkTimes();
		}
		cp.interrupt();
		this.exit=true;
		threadExecutorC.shutdown();				// shuts down the client generation process
		threadExecutorRC.shutdown();
		int i=0;
		while(activeClients.size()>0&&i<20){	// while there are still active clients and 20 interrupt attempts have been tried FIXME CHANGE TO WATCH CLIENTS
			i++;
			
			for (Client client : clients) {
				client.setExit(true);
				client.interrupt();
			}

//			for (int m=0;m<aC.size();m++){			// tells all clients to exit after next request
//				aC.get(m).setExit(true);
//				aC.get(m).interrupt();				// if the client is thinking
//				try {
//					Thread.sleep(25);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}

			//stops taking stats once there are no more clients in the system

			try {
				Thread.sleep(3500);
			} catch (InterruptedException e) {
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
			new SiteData(RunSettings.getOutputSiteDataFile(), true, cmarturl).collectStats();		// output data from Statistics page to csv
		}
		new OutputPageRT(pageRTHistograms,RunSettings.getOutputSiteDataFile()).outputData();;
		stats.exitStats();		// exit stats - no more collecting response times
		csd.output(RunSettings.getOutputSiteDataFile().toString());

		outputClientGeneratorXML();

		timer.shutdownNow();
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
		if(!RunSettings.isRepeatedRun()){
			String fileName = new StringBuilder(RunSettings.getRepeatedXmlFolder()).append("clientGenerator.xml").toString();
			try(BufferedWriter out = new BufferedWriter(new FileWriter(fileName,true));){
	
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	
				//initialize StreamResult with File object to save to file
				StreamResult result = new StreamResult(new StringWriter());
				DOMSource source = new DOMSource(xmlDocument);
				transformer.transform(source, result);
	
				String xmlString = result.getWriter().toString();
				out.write(xmlString);
			}catch(Exception e){
				e.printStackTrace();
				System.err.println("Could not output page response time data");
			}
		}
	}

	/**
	 * Schedules a change in the value of the stable users
	 * @author Andrew Fox
	 *
	 */
	private static class ChangeStableUsers extends Thread{
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
	private static class ChangeMeanClientsAdded extends Thread{
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
	private static class ChangePeakUsersSlope extends Thread{
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
			long startTime=System.currentTimeMillis();
			int startValue=RunSettings.getStableUsers();
			if(endTime!=-1){
				while(System.currentTimeMillis()<endTime){
					RunSettings.setStableUsers(startValue+(int)(((double)(System.currentTimeMillis()-startTime))*newSlope));
					try{Thread.sleep(2000);} 	
					catch(InterruptedException e){
						e.printStackTrace();
					}
				}
			}
			else{
				while(!cg.exit){
					RunSettings.setStableUsers(startValue+(int)(((double)(System.currentTimeMillis()-startTime))*newSlope));
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
	private static class CreateSpike extends Thread{
		long t0;
		long t1;
		long t2;
		long t3;
		double M;
		ClientGenerator cg;
		ScheduledExecutorService timer;

		/**
		 * Creates a burst in clients for the closed loop client
		 * @param t0 - time spike starts
		 * @param t1 - time spike peaks
		 * @param t2 - time of the end of the peak
		 * @param t3 - time of end of spike
		 * @param M - relative increase in workload volume
		 */
		public CreateSpike(long t0,long t1,long t2,long t3,double M,ClientGenerator cg,ScheduledExecutorService timer){
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

			if(RunSettings.isStaticUserload()){
				double origUsers=(double)RunSettings.getStableUsers();
				double increaseSlope=(origUsers*(M-1.))/(((double)(t1-t0))/60000);
				ChangePeakUsersSlope cpus=new ChangePeakUsersSlope(increaseSlope,t1,cg);
				timer.schedule(cpus,t0, TimeUnit.MILLISECONDS);
				cpus=new ChangePeakUsersSlope(0,t2,cg);
				timer.schedule(cpus,t1, TimeUnit.MILLISECONDS);
				double decreaseSlope=(origUsers*(1.-M))/(((double)(t3-t2))/60000);
				cpus=new ChangePeakUsersSlope(decreaseSlope,t3,cg);
				timer.schedule(cpus,t2, TimeUnit.MILLISECONDS);
			} else{
				double origStableUsers=(double)RunSettings.getStableUsers();
				double origUsers=(double)cg.aC.size();//FIXME CHANGE TO CLIENTS.SIZE()
				double origClientsAdded=(double)RunSettings.getMeanClientsPerMinute();


				ChangeStableUsers csu=new ChangeStableUsers((int)(M*origStableUsers));
				double increaseRate=(M-1.)*(origUsers)/((double)(t1-t0))+origClientsAdded;
				ChangeMeanClientsAdded cmca=new ChangeMeanClientsAdded((int)increaseRate);
				timer.schedule(csu,t0, TimeUnit.MILLISECONDS);
				timer.schedule(cmca,t0, TimeUnit.MILLISECONDS);

				cmca=new ChangeMeanClientsAdded((int)(origClientsAdded));
				timer.schedule(cmca,t1, TimeUnit.MILLISECONDS);

				double decreaseRate=(1.-M)*(origUsers)/((double)(t3-t2))+origClientsAdded;
				cmca=new ChangeMeanClientsAdded((int)decreaseRate);
				timer.schedule(cmca,t2, TimeUnit.MILLISECONDS);

				csu=new ChangeStableUsers((int)(origStableUsers));
				cmca=new ChangeMeanClientsAdded((int)(origClientsAdded));
				timer.schedule(csu,t3, TimeUnit.MILLISECONDS);
				timer.schedule(cmca,t3, TimeUnit.MILLISECONDS);

			}
		}
	}

	/**
	 * Runs a client on the website
	 * @author Andrew Fox
	 *
	 */
	private class RunClient extends Thread{
		private ClientInfo clientToRun;
		private StringBuilder startPage;
		
		private RunClient(){
			this.startPage = new StringBuilder(RunSettings.getCMARTurl().getAppURL()).append("/index").append(RunSettings.isHTML4()?"":".html");
		}
	
		public void run(){
			if(RunSettings.isRepeatedRun()){
				addActiveClient(clientToRun);
				addUser();
			}
			
			if (clientToRun!=null){				// if the client info exists
				try {
					
					Client p = new Client(clientToRun,startPage,RunSettings.getCMARTurl(), ClientGenerator.this);
					synchronized(aC){
						aC.add(p);
					}
					synchronized (clients) {
						clients.add(p);
					}
					System.out.println("Starting Client: "+clientToRun.getUsername()+", Number of Active Clients = "+clients.size());
					threadExecutorC.execute(p);
					//p.start();
				} catch (IOException e) {
					System.err.println("Error creating Client " + clientToRun);
					if(RunSettings.isVerbose()){
						e.printStackTrace();
					}
				}
			}else{
				System.err.println("Cannot create user!");
				//semaphore.release(); FIXME should i uncomment this?
			}
		}
		private RunClient(ClientGenerator cg){
			this();
//			this.cg=cg;							// client generator creating the client
			// creates a client node in the xmlDocument for the client about to be run
			Element client=xmlDocument.createElement("client");
			Element child=xmlDocument.createElement("clientIndex");
			long index=getClientIndex();
			child.setTextContent(Long.toString(index));
			client.appendChild(child);
			child=xmlDocument.createElement("runTime");
			child.setTextContent(Long.toString(System.currentTimeMillis()-startTime));
			client.appendChild(child);
			Element cI=xmlDocument.createElement("clientInfo");
			if(rand.nextDouble()>newClientProb){	// using a client whose info already exists
				if (!inactiveClients.isEmpty()){		// if there are existing clients available
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
			for(Entry<String,StringBuilder> e: clientToRun.getJscssCache().entrySet()){
				child=xmlDocument.createElement("jscss");
				child.setTextContent(e.getKey());
				cache.appendChild(child);
			}
			cI.appendChild(cache);
			cache=xmlDocument.createElement("HTML5Cache");
			for(Entry<String,StringBuilder> e: clientToRun.getHTML5Cache().entrySet()){
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
			this();
//			this.cg=cg;
			this.clientToRun=new ClientInfo();
			// Sets the client info according to that in the readXmlDocument
			clientToRun.setClientIndex(Long.parseLong(((Element)client).getElementsByTagName("clientIndex").item(0).getTextContent()));
			Element clientInfo=(Element)((Element)client).getElementsByTagName("clientInfo").item(0);
			clientToRun.setUserID(new StringBuilder(clientInfo.getElementsByTagName("userID").item(0).getTextContent()));
			clientToRun.setUsername(new StringBuilder(clientInfo.getElementsByTagName("username").item(0).getTextContent()));
			clientToRun.setPassword(new StringBuilder(clientInfo.getElementsByTagName("password").item(0).getTextContent()));
			clientToRun.setRegistered(Boolean.parseBoolean(clientInfo.getElementsByTagName("registered").item(0).getTextContent()));
		}
	
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
			}while(!exit);
		}
	}


}
