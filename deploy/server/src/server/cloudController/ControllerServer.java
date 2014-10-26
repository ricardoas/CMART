package server.cloudController;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.TreeMap;

import server.XMLReader.HostInfo;
import server.XMLReader.VMImageSetting;
import server.XMLReader.VMInfo;

import server.cloudController.ThreadInfo.ThreadStatus;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SFTPv3Client;

/**
 * The server that listen to all the requests from the client
 * 
 * Client will interact with the deployment server using the TCP connection...
 */
public class ControllerServer implements Runnable {
	int port;
	int clientCount;
	ServerSocket serverSocket = null;
	String ip;
	static final long totalWaitingTime = 60 * 60 * 1000;	// Stands for 60 min
	final int MAX_RETRIES = 10;
	static ArrayList<HostInfo> clientList;
	static Hashtable<String, VMImageSetting> VMImageSettings;
	// Define a thread status array
	public static ThreadInfo[] clientStatus;
	public static String beginIP;
	public static int totalVMNumber = 0;
	public static ArrayList<VMInfo> finalVMStates = new ArrayList<VMInfo>();
	
	public static TreeMap<String, String> appExtraConfig = new TreeMap<String, String>();
	public static TreeMap<String, String> sqlExtraConfig = new TreeMap<String, String>();
	public static TreeMap<String, String> clientExtraConfig = new TreeMap<String, String>();

	public ControllerServer(String ip, int port, int clientCount, ArrayList<HostInfo> clientList, Hashtable<String, VMImageSetting> VMImageSettings, String beginIP, int totalVMNumber) throws UnknownHostException {
		this.ip = ip;
		this.port = port;
		this.clientCount = clientCount;
		this.clientStatus = new ThreadInfo[clientCount];
		this.clientList = clientList;
		this.VMImageSettings = VMImageSettings;
		this.beginIP = beginIP;
		this.totalVMNumber = totalVMNumber;
		System.out.println("The total VM Number I got in ControllerServer is " + this.totalVMNumber);
		
		for (int i=0; i < clientCount; i++) {
			clientStatus[i] = new ThreadInfo();
		}
	}
	
	/**
	 * Start the socket
	 */
	public void startServerSocketWithRetry() throws IOException, InterruptedException {
		int retries = 0;
		boolean succeed = false;
		
		// Retry 10 times to do the connect
		while ((++retries) <= MAX_RETRIES) {
			//System.out.println("I am on the " + retries + " retry to connect to host: " + hostname);
			if (startServerSocket()) {
				succeed = true;
				break;
			}
			
			Thread.sleep(200);
		}
		
		if (!succeed) {
			throw new IOException("Cannot establish server socket for port " + port);
		}
	}
	
	/**
	 * Return 10 times to start a connection
	 */
	public boolean startServerSocket(){
		try {
			serverSocket = new ServerSocket(port);
			System.out.format("Server socket established at %s:%d\n", serverSocket.getInetAddress().getHostAddress(), port);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	/**
	 * The main routine of the server 
	 * 
	 * Just listen all the request from each client and then open a thread to deal with each client socket. 
	 * Client here means all the physical machines you want to do the deployment.
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			System.out.println("Going to open a server socket for port " + port);
			startServerSocket();
			
			System.out.println("Going to create different threads for the client.");
			
			int acceptedClientCount = 0;
			// Add a time out mechanism to avoid the situation that one client is dead
			serverSocket.setSoTimeout(60000);
			long beginTime = Calendar.getInstance().getTimeInMillis();
			System.out.format("Server begin waiting at %s: %s clients needed to be created.\n", beginTime, clientCount);
			
			// Listen to all the client request and open a new thread for each client
			while (acceptedClientCount < clientCount) {
				try {
					System.out.println("Controller in the loop of checking for clients! ********************");
					System.out.println("Hosts are probably still copying VM images!     ********************");
					Socket socket = serverSocket.accept();
					System.out.println("--------------------------------------------I got a socket from " + socket.getInetAddress().getHostAddress());
					new Thread(new socketServerThread(socket, acceptedClientCount++)).start();
					System.out.println("**************************");
					System.out.println("&&&&&&&&&&&&&&&&&&& Already get " + acceptedClientCount + " clients.");
				} catch (SocketTimeoutException ex) {
					long currentTime = Calendar.getInstance().getTimeInMillis();
					if (currentTime - beginTime < totalWaitingTime) {
						System.out.format("Current time %s, begin time %s, Altough timeout still waiting as not all clients have connected yet...\n", currentTime, beginTime);
					}
					else {
						System.out.format("Time out! Goign to return, and I only receive %s clients.\n", acceptedClientCount);
						System.out.format("Time out! We waited for clients by they did not arrive - perhaps edit deployment.server.ControllerServer timeout if needed.\n");
						for (int i=acceptedClientCount; i < clientCount; i++) {
							clientStatus[i].setStatus(ThreadStatus.CANNOT_ESTABLISH);
						}
						break;
					}
				}
			}
			
			System.out.println("************* I got all the clients connection I need!");
			// In here... we have to check how many threads are going to be wrong!
			// If anything is wrong, will print out the message
			boolean allDone = true;
			while(true) {
				allDone = true;
				for (int i=0;i<clientCount;i++) {
					System.out.println("Status: " + clientStatus[i].getStatus());
					if (clientStatus[i].getStatus() != ThreadStatus.FINISH)
						allDone = false;
				}
				
				if (allDone)
					break;
				Thread.sleep(10000);
			}
			
			System.out.println("Going to finish this server!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}