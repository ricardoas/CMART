package server.main;

import java.awt.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import server.cloudController.*;
import server.XMLReader.HostInfo;
import server.XMLReader.VMConfig;
import server.XMLReader.VMImageSetting;
import server.XMLReader.VMInfo;

/**
 * The main class for the deployment controller (on the server side)
 * @author XySx
 *
 */
public class DeploymentController {
	static String serverIP;
	public static String serverGateway;
	static int port;
	static String serverOS;
	public static String deployPath;
	public static String cmartNewConfig = null;
	
	static ArrayList<HostInfo> clientList = new ArrayList<HostInfo>();
	static Hashtable<String, VMImageSetting> VMImageSettings = new Hashtable<String, VMImageSetting>();
	
	public static String beginIP;
	public static int totalVMNumber = 0;
	
	/**
	 * The main route of the problem!!!
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			startTime = System.currentTimeMillis();
			
			System.out.println("The current time is " + Calendar.getInstance().getTime());
			// First we have to try understand the parse in Config xml file
			if (args.length == 0) {
				System.err.println("Please pass the deployment configuration file as a parameter");
				throw new Exception("The given config path is null!");
			}

			readXMLConfig(args[0]);	
			
			if(args.length>1){
				cmartNewConfig = args[1];
				System.out.println("Got a new config file to upload to VMs");
			}
			
			/* As for here..... install the collectd and config it.... */
			try{
				executeLinuxCommandWithoutException("yum install -y wget");
				executeLinuxCommandWithoutException("wget http://dl.fedoraproject.org/pub/epel/6/x86_64/epel-release-6-8.noarch.rpm");
				executeLinuxCommandWithoutException("wget http://rpms.famillecollet.com/enterprise/remi-release-6.rpm");
				executeLinuxCommandWithoutException("rpm -Uvh remi-release-6*.rpm epel-release-6*.rpm");
				executeLinuxCommandWithoutException("yum install -y collectd");
				executeLinuxCommandWithException("rm -rf /collectdData/");
				executeLinuxCommandWithException("mkdir -p /collectdData");
				executeLinuxCommandWithException("mkdir -p /collectdData_history");
				System.out.println("List the current folder...");
				executeLinuxCommandWithException("ls /collectdData");
			}
			catch(Exception e){
				System.err.println("Please make sure you are running on Redhat Linux");
				throw e;
			}		
			
			/* Going to modify the ip address...... of the scripts /collectd_server.conf */
			String newPath = deployPath+"/scripts/collectd_server_new.conf";
			String localPath = deployPath+"/scripts/collectd_server.conf";
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(localPath)));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newPath)));	
			
			// Then read in all the files
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				// If the curLine is 1,we will going to add all the proxy settings....
				if (line.trim().startsWith("Listen ")) {
					sb.append("    Listen \"" + serverIP + "\" \"12344\"" + "\n");
				}
				else {
					sb.append(line + "\n");
				}
			}
			
			// After all write to the file....
			writer.write(sb.toString());
			reader.close();
			writer.flush();
			writer.close();
			
			// Going to modify the /etc/collectd.conf file...
			executeLinuxCommandWithException("cp "+deployPath+"/scripts/collectd_server_new.conf /etc/collectd.conf");
			executeLinuxCommandWithException("service collectd restart");
			
			// Then reate a list of thread
			ArrayList<Thread> threadList = new ArrayList<Thread>();
			
			// Open a thread here to open a server socket
			// The first two param is the host IP and port number, and the third param is the client size
			Thread serverThread = new Thread(new ControllerServer(serverIP, port, clientList.size(), clientList, VMImageSettings, beginIP, totalVMNumber));
			threadList.add(serverThread);
			serverThread.start();
			Thread.sleep(100);
			
			
			/* Open different thread for each client to do the deployment */
			for (int i=0; i<clientList.size(); i++) {
				Thread thread = null;
				
				// See what kind of images are needed for this client.......
				HostInfo client = clientList.get(i);
				
				for (VMConfig config : client.vmConfigs) {
					String type = config.vmType;
					if (!client.VMTypes.contains(type))
						client.VMTypes.add(type);
				}
				
				if (serverOS.equals("Fedora")) {
					System.out.println("Going to create a fedora controller for client " + clientList.get(i).ip);
					thread = new Thread(new FedoraController(serverIP, clientList.get(i), i));
				}
				else
				{
					throw new Exception("The current host OS type " + serverOS +" is not supported!");
				}
				
				// start the thread
				threadList.add(thread);
				thread.start();
				Thread.sleep(100);
			}
			
			
			// Do a join for all the thread
			for (Thread thread : threadList) {
				thread.join();
			}
			
			System.out.println("I am out of the main thread!");
			System.out.println("Output all VM State!!!!");
			for (VMInfo vm : ControllerServer.finalVMStates) {
				System.out.format("Name: %s, Type: %s, IP: %s, hostIP: %s, State: %s, backup?: %s\n", vm.name, vm.type, vm.ip, vm.hostIP, vm.state, vm.backup);
			}
			
			System.out.println("Finished running the deployment server");
			
			//TODO: next part
			System.exit(0);
			
			
			
			
			
			// BELOW IS THE CLIENT TO ACCESS CMART
				
			
			// When it is finished.... just get the collectd file
			System.out.println("All the install and Checking has done for ");
			executeLinuxCommandWithException("rm -rf /collectdData/");
			executeLinuxCommandWithException("mkdir -p /collectdData");
			System.out.println("List the current folder...");
			Thread.sleep(10000);
			
			System.out.println("Before running experiment - current time " + Calendar.getInstance().getTime());
			threadList = new ArrayList<Thread>();
			/* Going to run the experiment!!!!! */
			String lbIp = null;
			for (VMInfo vm : ControllerServer.finalVMStates) {
				if (vm.type.equals("LoadBalancer")) {
					lbIp = vm.ip;
					break;
				}
			}
			Thread dynamic = new Thread(new DynamicAllocator(lbIp, "root", "test"));
			dynamic.start();
			
			System.out.println("Going to run the experiment!");
			for (VMInfo vm : ControllerServer.finalVMStates) {
				if (vm.type.equals("Client")) {
					System.out.println("Going to run experiment on " + vm.ip);
					Thread s = new Thread(new ExperimentConductor(vm.ip, "root", "testtest"));
					threadList.add(s);
					s.start();
				}
			}
			
			// Do a join for all the thread
			for (Thread thread : threadList) {
				thread.join();
			}
			
			DynamicAllocator.end = true;
			System.out.println("After running experiment - current time " + Calendar.getInstance().getTime());
			System.out.println("Going to get the data!");
			// Copy all the data
			String fold = "/collectdData_history/" + Calendar.getInstance().getTimeInMillis();
			executeLinuxCommandWithoutException("sh scripts/copy_result.sh");
			executeLinuxCommandWithException("mv /collectdData.tar " + fold + ".tar");
			System.out.println("The current time is " + Calendar.getInstance().getTime());
		} catch(Exception e) {
			System.out.println(e.toString());
			System.out.println(e.getStackTrace());
		}
	}
	
	/**
	 * The function to read in the xml config file and understand everything in it
	 */
	private static void readXMLConfig(String configPath) throws Exception {
		// Make sure the deployment file is given
		System.out.println("The given config path is " + configPath);
		if (configPath == null) {
			System.err.println("Please pass the deployment configuration file as a parameter");
			throw new Exception("The given config path is null!");
		}

		// Read the config file - will throw exception if incorrect
		File xmlFile = new File(configPath);
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document xmlDoc = db.parse(xmlFile);
		xmlDoc.getDocumentElement().normalize();
		
		// Get the root element of the file - the host for this deployment server
		Element root = xmlDoc.getDocumentElement();
		serverIP = root.getAttribute("IP");
		serverGateway = root.getAttribute("Gateway");
		serverOS = root.getAttribute("OS");
		deployPath = root.getAttribute("deployPath");
		port = Integer.parseInt(root.getAttribute("port"));
		beginIP = root.getAttribute("beginIP");
		
		// Output the information I read
		System.out.println("I am host " + serverIP +"; and my host OS is "+ serverOS);
		
		// Get information for each Server
		NodeList machineList = root.getElementsByTagName("Host");
		for (int i=0; i<machineList.getLength(); i++) {
			Element server = (Element)machineList.item(i);
			
			// Parse the server info
			HostInfo client = new HostInfo(server);
			clientList.add(client);
			
			// Now how many VMs are under this machine.....
			totalVMNumber += client.vmConfigs.size();
			
			// Output the information I read
			System.out.format("Now understanding client %s\n", client.toString());
		}
		
		System.out.println("Going to read the VM Image Setting");
		// Get information for each VM Setting
		NodeList VMImageSettingList = root.getElementsByTagName("VMImageSetting");
		for (int i=0; i<VMImageSettingList.getLength(); i++) {
			Element VMImageSetting = (Element)VMImageSettingList.item(i);
			
			// Get the Image and XML path....
			VMImageSetting setting = new VMImageSetting(VMImageSetting);
			VMImageSettings.put(setting.type, setting);
			System.out.format("Now understanding image setting %s\n", setting.toString());
		}
		
		// Understand the additional config setting for app - sql and - client
		System.out.println("Going to understand additional config settings");
		NodeList extraSettings = root.getElementsByTagName("ExtraConfigSetting");
		
		for (int i=0;i<extraSettings.getLength();i++) {
			Element node = (Element)extraSettings.item(i);
			String type = node.getAttribute("Type");
			System.out.println("For this setting, the type is " + type);
			
			TreeMap<String, String> map = null;
			if (type.equals("Application"))
				map = ControllerServer.appExtraConfig;
			else if (type.equals("SQL"))
				map = ControllerServer.sqlExtraConfig;
			else if (type.equals("Client"))
				map = ControllerServer.clientExtraConfig;
			else
				continue;
			
			// Going to understand each setting under.....
			NodeList settings = node.getElementsByTagName("Setting");
			for (int k=0;k<settings.getLength();k++) {
				Element setting = (Element)settings.item(k);
				String key = setting.getAttribute("Key");
				String value = setting.getAttribute("Value");
				System.out.println("Understanding key " + key + " - value " + value);
				
				if (type.equals("Client")) {
					String per = setting.getAttribute("perClient");
					//System.out.println("Client - Understanding key " + key + " - value " + value + " - per " +  per);
					// See if we need to divide the number
					if (per.equals("True")) {
						int valNum = Integer.parseInt(value);
						int perVal = valNum / VMConfig.clientNum;
						value = String.valueOf(perVal);
					}
				}
				
				map.put(key, value);
			}
		}
		
		System.out.println("As for the app extra config I got :");
		Iterator<String> itr = ControllerServer.appExtraConfig.keySet().iterator();
		while (itr.hasNext()) {
			String key = itr.next();
			String value =  ControllerServer.appExtraConfig.get(key);
			System.out.println("AppExtraConfig - key - " + key + " - value " + value);
		}
		
		System.out.println("As for the sql extra config I got :");
		itr = ControllerServer.sqlExtraConfig.keySet().iterator();
		while (itr.hasNext()) {
			String key = itr.next();
			String value =  ControllerServer.sqlExtraConfig.get(key);
			System.out.println("SQLExtraConfig - key - " + key + " - value " + value);
		}
		
		System.out.println("As for the client extra config I got :");
		itr = ControllerServer.clientExtraConfig.keySet().iterator();
		while (itr.hasNext()) {
			String key = itr.next();
			String value =  ControllerServer.clientExtraConfig.get(key);
			System.out.println("clientExtraConfig - key - " + key + " - value " + value);
		}
	}
	
	public static String errmsg;
	public static String output;
	
	/**
	 * Function to execute a linux command - will throw a exception when the stderr is not empty
	 * @param cmd
	 * @throws Exception
	 */
	public static void executeLinuxCommandWithException(String cmd) throws Exception {
		executeLinuxCommandWithoutException(cmd);
		
		if (!errmsg.isEmpty()) {
			System.out.println("Stderr: " + errmsg + " when executing " + cmd);
			throw new Exception(errmsg);
		}
	}

	/**
	 * Use Runtime().execute and update the stdout and stderr
	 */
	public static void executeLinuxCommandWithoutException(String cmd) throws Exception {
		String[] cmds = cmd.split(" ");
		Process ps = Runtime.getRuntime().exec(cmds);
		
		// Get stdout and stderr
		BufferedReader out = new BufferedReader(new InputStreamReader(ps.getInputStream()));
		BufferedReader err = new BufferedReader(new InputStreamReader(ps.getErrorStream()));
		
        // Output stdout
        output = "";
        while (true)
        {
            String stdoutLine = out.readLine();
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
            String errLine = err.readLine();

            if (errLine == null)
            {
                break;
            }
            
            errmsg += errLine + "\n";
            System.out.println("Error: " + errLine);
        }
	}

	private static long startTime=0;
	private static int debugLevel=1000;
	public static void print(String module, int level, String text){
		if(debugLevel>=level){
			if(module==null) module="";
			if(text==null) text = "";
			
			System.out.println((System.currentTimeMillis()-startTime)/1000 + " sec: (" + module + "): " + text);
		}
	}
	
}
