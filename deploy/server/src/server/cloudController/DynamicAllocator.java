package server.cloudController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SFTPv3Client;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

import server.XMLReader.HostInfo;
import server.XMLReader.VMInfo;

public class DynamicAllocator implements Runnable {
	public String ip;
    String userName;
    String password;
    
	Connection connection = null;
	SCPClient scpClient = null;
	SFTPv3Client sftpClient;
	
	String output;
	String errmsg;
	
    public static final int MAX_RETRIES = 10;
	// The constructor to give all thse parameters needed
	public DynamicAllocator(String serverIP, String userName, String password) {
		this.ip = serverIP;
		this.userName = userName;
		this.password = password;
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
			System.out.println("I am on the " + retries + " retry to connect to host: " + ip);
			if (startConnection()) {
				succeed = true;
				break;
			}
			
			Thread.sleep(200);
		}
		
		if (!succeed) {
			throw new IOException("Cannot connect to the host " + ip + " after 10 time retries - Possible situation: wrong username/password.");
		}
	}
	
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
			System.out.format("Connection to %s setup correctly.\n", this.ip);
			return true;
		} catch (IOException e) {
			return false;
		}
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
        	System.out.format("Error message:\n%s", errmsg);
        	session.close();
        	throw new Exception(errmsg);
        }
        
        session.close();
        return output;
	}
	
	public void endConnection() {
		System.out.format("Connection to %s terminated.\n", ip);
		connection.close();
	}
	
	public static boolean end = false;
	public static void endThis() {
		end = true;
	}
	/**
	 * The main route for fedora controller (SSH into the client)
	 */
	@Override
	public void run() {
		try {
			startConnectionWithRetry();
			System.out.println("connection set up!");
			
			// Going to run the client generator
			while (!end) {
				System.out.println("I am alloctor - output all the backup apps");
				for (VMInfo vm : ControllerServer.finalVMStates) {
					if (vm.type.equals("Application") && vm.backup != 0) {
						System.out.println(vm);
					}
				}
				
				Thread.sleep(20000);
			}
			
			System.out.println("Going to end the dynamic allocator");
			this.endConnection();
		} catch (Exception e) {
			e.printStackTrace();	
			return;
		}
	}
}
