package server.cloudController;

import java.io.*;
import java.util.ArrayList;

import server.XMLReader.HostInfo;

import ch.ethz.ssh2.*;

/**
 * The base class for Deployment Controller (ssh into the given machine and execute the client program on the given machine)
 * Currently we only implement the FedoraController.
 * 
 * Using the external ganymed-ssh2 package
 * @author XySx
 *
 */
public abstract class Controller implements Runnable {
    String ip;
    String userName;
    String password;
    String threadName;
    String host;
    String controllerIP;
    String baseDirectory;	// The directionary containing all the files for this client...
    int clientID;
    ArrayList<String> vmTypes;
    
    final int MAX_RETRIES = 10;
    
	Connection connection = null;
	SCPClient scpClient = null;
	SFTPv3Client sftpClient;
	
	String output;
	String errmsg;
	
	// The constructor to give all thse parameters needed
	public Controller(String controllerIP, HostInfo client, int clientID) {
		this.ip = client.ip;
		this.userName = client.userName;
		this.password = client.password;
		this.host = userName + "@" + ip;
		this.controllerIP = controllerIP;
		this.baseDirectory = client.baseFolder;
		this.clientID = clientID;
		this.vmTypes = client.VMTypes;
	}
	
	/**
	 * Start a connection to the server (using java-ssh2)
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void startConnectionWithRetry() throws IOException, InterruptedException {
		int retries = 0;
		boolean succeed = false;
		
		// Retry 10 times to do the connect
		while ((++retries) <= MAX_RETRIES) {
			System.out.println("I am on the " + retries + " retry to connect to host: " + host);
			if (startConnection()) {
				succeed = true;
				break;
			}
			
			Thread.sleep(200);
		}
		
		if (!succeed) {
			throw new IOException("Cannot connect to the host " + host + " after 10 time retries - Possible situation: wrong username/password or incorrect IP address.");
		}
	}
	
	/**
	 * Return 10 times to start a connection
	 */
	public boolean startConnection(){
		try {
			// Open a connection to the host
			connection = new Connection(ip);
			connection.connect();
			
			// Then give in the user name and password
			boolean isConnected = connection.authenticateWithPassword(userName, password);
			
			if (!isConnected) {
				throw new IOException();
			}
			
			scpClient = connection.createSCPClient();
			sftpClient = new SFTPv3Client(connection);
			System.out.format("Thread %s: Connection to %s setup correctly.\n", threadName, host);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	public void executeCommandWithoutWaiting(String cmd) throws Exception {
		// Call execCommand on session
		Session session = connection.openSession();
		session.execCommand(cmd);
        session.close();
        return;
	}
	
	/**
	 * Open a new session and execute the command (using java-ssh)
	 * @param The command going to execute
	 * @throws IOException
	 */
	public String executeCommand(String cmd) throws Exception {
		// Call execCommand on session
		Session session = connection.openSession();
		session.execCommand(cmd);
		//System.out.println("Going to execute " + cmd);
		// And then observe all possible output !!!!!!!!!!!Still have to re-write
		// Get the stdout, stderr from the session
		InputStream stdoutStream = new StreamGobbler(session.getStdout());
		InputStream stderrStream = new StreamGobbler(session.getStderr());
		
        // User a reader to read the I/O output
        BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(stdoutStream));
        BufferedReader stderrReader = new BufferedReader(new InputStreamReader(stderrStream));
        
        // Output stdout
        output = "";
        while (true)
        {
            String stdoutLine = stdoutReader.readLine();
            if (stdoutLine == null)
            {
                break;
            }
            
            output += stdoutLine + "\n";
            System.out.println(stdoutLine);
        }
        
        // Output stderr
        errmsg = "";
        while (true)
        {
            String errLine = stderrReader.readLine();

            if (errLine == null)
            {
                break;
            }
            
            errmsg += errLine + "\n";
            System.out.println("Error: " + errLine);
        }
        
        if (!errmsg.isEmpty()) {
        	System.out.format("Thread %s: Error message:\n%s", threadName, errmsg);
        	session.close();
        	throw new Exception(errmsg);
        }
        
        session.close();
        return output;
	}
	
	public String executeCommandWithoutException(String cmd) throws Exception {
		// Call execCommand on session
		Session session = connection.openSession();
		session.execCommand(cmd);
		//System.out.println("Going to execute " + cmd);
		// And then observe all possible output !!!!!!!!!!!Still have to re-write
		// Get the stdout, stderr from the session
		InputStream stdoutStream = new StreamGobbler(session.getStdout());
		InputStream stderrStream = new StreamGobbler(session.getStderr());
		
        // User a reader to read the I/O output
        BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(stdoutStream));
        BufferedReader stderrReader = new BufferedReader(new InputStreamReader(stderrStream));
        
        // Output stdout
        output = "";
        while (true)
        {
            String stdoutLine = stdoutReader.readLine();
            if (stdoutLine == null)
            {
                break;
            }
            
            output += stdoutLine + "\n";
            System.out.println(stdoutLine);
        }
        
        // Output stderr
        errmsg = "";
        while (true)
        {
            String errLine = stderrReader.readLine();

            if (errLine == null)
            {
                break;
            }
            
            errmsg += errLine + "\n";
            System.out.println("Error: " + errLine);
        }
        
        session.close();
        return output;
	}
	
	/**
	 * Transfer file from the local machine to the host
	 */
	public void transferFile(String filePath, String destinationPath) throws IOException{
		System.out.println("SCPing file from "+filePath+ " to " +destinationPath);
		scpClient.put(filePath, destinationPath);
	}
	
	public void endConnection() {
		System.out.format("Thread %s: Connection to %s terminated.\n", threadName, host);
		connection.close();
	}

	public abstract void run();
	
	public abstract void installJava() throws Exception;
	public abstract boolean checkJavaAvailability() throws Exception;
}
