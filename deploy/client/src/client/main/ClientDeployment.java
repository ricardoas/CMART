package client.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.sql.rowset.spi.XmlWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import client.deployment.ConfigModifier;
import client.deployment.DeploymentManager;
import client.deployment.FedoraManager;
import client.deployment.VMInfo;

/**
 * The main routine for the client deployment program running on each physical machine
 * 
 * Just basically communicate with the server deployment controller, and then deploy VMs for each physical machine...
 * @author XySx
 *
 */
public class ClientDeployment {
	String serverIP;
	Socket client;
	int clientID;
	DeploymentManager deploymentManager;
	BufferedReader in;
	PrintWriter out;
	String input;
	String hostIP;
	String newCmartconfig = null;
	
	ArrayList<VMInfo> vmList = new ArrayList<VMInfo>();
	ArrayList<String> VMTypes = new ArrayList<String>();
	
	final static int CHECK_VM_STATE_RETRIES = 30;
	final static int MAX_RETRIES = 100;
	
	public ClientDeployment(String serverIP) {
		this.serverIP = serverIP;
		startTime = System.currentTimeMillis();
	}
	
	/**
	 * The main route of the client....
	 * @throws Exception
	 */
	public void run() throws Exception {
		// First going to connect to the socket from the server
		System.out.println("Going to connect the server at 4444, the server IP is "+ serverIP);
		
		try{
			client = new Socket(serverIP, 4444);
		}
		catch(Exception e){
			System.err.println("Could not connect to server! Make sure server IP is correct in deployment XML file");
			throw e;
		}
		
		System.out.println("Connect succeed!");
		
		client.setSoTimeout(30 * 60 * 1000);	// Set the time out to 30 min
		
		in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		out = new PrintWriter(client.getOutputStream(), true);
		System.out.println(" &&&&&&&&&&&&&&&&&&&&&&&&& My connecting host is " + client.getInetAddress());
		
		// Get the client ID
		input = in.readLine();
		clientID = Integer.parseInt(input.substring(11));
		System.out.println("&&&&&&&&&&&&&&&&&&&&&& I am client " + clientID);
		
		// After I already got the client ID
		// Going to do the actual deployment.
		out.println("OS Type?");
		input = in.readLine();
		if ((deploymentManager = CreateDeploymentManager(input)) == null) {
			endConnectionWithError("Cannot create Deployment Manager for the specified OSType");
			return;
		}
		
		// See if there is a new cmartconfig
		out.println("New cmartconfig?");
		input = in.readLine();
		if(input==null && !input.equalsIgnoreCase("no")){
			newCmartconfig = input;
			
			// Just get the filename
			String[] split = newCmartconfig.split("/");
			newCmartconfig = split[split.length-1];
			split = newCmartconfig.split("\\\\");
			newCmartconfig = split[split.length-1];
			
			System.out.println("Got new cmart config file " + newCmartconfig);
		}else{
			System.out.println("No new cmart config file, will update current VM config");
		}
		
		// After the Deployment Manager is created, going to install different softwares and check if installed corrected
		System.out.println("Going to install the following software - KVM Libvirt, Collectd");
		out.println("Base directory?");
		input = in.readLine();
		deploymentManager.baseDict = input + "/";
		
		try {
			deploymentManager.installKVMWithCheck();
			deploymentManager.installLibvirtWithCheck();
			deploymentManager.installCollectdWithCheck();
			deploymentManager.installLibGuestFSWithCheck();
			
			// Start the collectd (set the output file to be in /client_deploy/collectd_data/*)
			// Copy the setting files........
			String newPath = deploymentManager.baseDict + "/client_deploy/scripts/collectd_client_new.conf";
			String localPath = deploymentManager.baseDict + "/client_deploy/scripts/collectd_client.conf";
			
			// Modify the hostname and network setting...
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(localPath)));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newPath)));
			
			// Then read in all the files
			StringBuilder sb = new StringBuilder();
			String line;
			int curLine = 0;
			while ((line = reader.readLine()) != null) {
				// If the curLine is 1,we will going to add all the proxy settings....
				if (line.trim().startsWith("Hostname")) {
					sb.append("Hostname \"" + this.clientID + "\"" +"\n");
				}
				else if (line.trim().startsWith("Server \"10."))
					sb.append("    Server \"" + this.serverIP + "\" \"12344\"\n");
				else {
					sb.append(line + "\n");
				}
			}
			
			// After all write to the file....
			writer.write(sb.toString());
			reader.close();
			writer.flush();
			writer.close();
			
			System.out.println("Copying collectd config and restarting");
			deploymentManager.executeLinuxCommandWithException("cp " + deploymentManager.baseDict + "/client_deploy/scripts/collectd_client_new.conf" + " /etc/collectd.conf");
			deploymentManager.executeLinuxCommandWithoutException("service collectd restart");
		} catch (Exception e) {
			endConnectionWithError(e.toString());
			return;
		}

		// Then we will get the VM images for all the VMs supposed to run on this client host
		// And then setup all the VMs.
		try {
			System.out.println("End installing all software, going to start VM!");
			// Query the server to see what type of VM is needed
			out.println("How many VMs?");
			input = in.readLine();
			
			System.out.println("!!!! Going to config all VMs.");
			
			// First get all the VM infomation in vmList;
			configVirtualMachines();
			
			// Then we have to start all the VMs
			System.out.println("!!!! Going to modify XML and start each VM!");
			processVMImages();
			hostIP = client.getLocalAddress().getHostAddress();
			
			defineVMs();
			// We used to start the VM to check they work, now we'll just skip it
			//startVMs();
						
			//System.out.println("Check to make sure all VMs are running! (pingable)");
			//if (!checkVMsRunning()) {
			//	System.out.println("Are all the VM images present on the host?");
			//	throw new Exception("Not all VMs are up on the host " + client.getLocalAddress().getHostAddress());
			//}
			
			// Report the result to the server!!!!!!!
			out.println("Going to report all VM state!" + ";" + vmList.size());
			for (VMInfo vm : vmList) {
				System.out.println("I am going to report : " + vm.name + ";" + vm.type + ";" + vm.ip + ";" + hostIP + ";" + vm.state + "; " + vm.backup);
				out.println(vm.name + ";" + vm.type + ";" + vm.ip + ";" + hostIP + ";" + vm.state + ";" + vm.backup);
			}
			input = in.readLine();		
			
			// After all the Vms are start up and stable... we will shut it down and do the modifying to the files inside each VM....
			// Use a loop to continue check if all VMs are stable....
			// The VMs used to actually be up, now we just wait for hosts to copy the images
			loopCheckingAllVMsAreSetUp();
			
			// After all VMs is running, we will going to stop all the VMs and modify the information in it.....
			//stopAllVMs();
		
			// Then modify all the information correspondingly....
			Thread.sleep(15000);
			modifyLoadBalancerSettings();
			modifyAppSettings();
			modifySqlSettings();
			modifyClientSettings();
			
			// Then re-start all the VMs
			rebootAllVMs();
			System.out.println("Make sure these vms are upon running again....! (pingable)");
			
			// Need to pull out the data from ..?
			ShutdownColdBackupVMs();
			
			if (!checkVMsRunning()) {
				throw new Exception("Not all VMs are up on the host " + client.getLocalAddress().getHostAddress());
			}	
		} catch (Exception e) {
			endConnectionWithError(e.toString());
			return;
		}

		System.out.println(" &&&&&&&&&&&&&&&&&&&&&&&&& Finished running the client on " + client.getInetAddress());
		endConnection();
	}

	private void ShutdownColdBackupVMs() throws Exception {
		for (VMInfo vm : vmList) {
			System.out.println("Client - I am vm:" + vm);
			
			// If it is a backup?
			if (vm.backup == VMInfo.COLD) {
				// Shut down VMs
				deploymentManager.executeLinuxCommandWithException("virsh destroy " + vm.name);
				System.out.println("**** Vm " + vm.name + "stopped...!");
			}
		}
		
	}

	private void modifyClientSettings() throws Exception {
		// TODO Auto-generated method stub
		for (VMInfo vm : vmList) {
			if (!vm.type.equals("Client"))
				continue;
			
			System.out.println("************** Modifying setting for " + vm.name);
			String configPath = "/cmart/client/CGconfig.txt";
			
			String filename = "/cmart/client/CGconfig.txt";
			String localPath = deploymentManager.baseDict+"/client_deploy/VMs/" + vm.name + "/CGconfig.txt";
			String newPath = deploymentManager.baseDict+"/client_deploy/VMs/" + vm.name + "/new_CGconfig.txt";
			
			// Get the config
			String cmd = "sh " + deploymentManager.baseDict + "/client_deploy/scripts/get_qcow_file.sh " + deploymentManager.baseDict+"/client_deploy/VMs/" + vm.name + "/" + " " + vm.name+".qcow2" + " ";
			cmd += localPath + " " + filename;
			deploymentManager.executeLinuxCommandWithoutException(cmd);
			
			// Change the full url...
			ConfigModifier config = new ConfigModifier(localPath);
			ArrayList<String> keys = new ArrayList<String>();
			ArrayList<String> values = new ArrayList<String>();
			
			// Change the full url!!!!
			out.println("Give me first avaiable LoadBalancer IP!");
			input = in.readLine();
			String lbIP = input;
			
			if (input.equals(null))
				throw new Exception("Now available load balancer IP!\n");
			
			// Going to modify the file...
			keys.add("full_url");
			values.add("http://" + lbIP + ":80/cmart-1/");
			
			// Extra settings???????????
			out.println("Give me extra settings for client");
			String setting = in.readLine();
			String[] settings = setting.split(";");
			
			if (!setting.isEmpty()) {
				for (int i=0;i<settings.length;i+=2) {
					String key = settings[i];
					String value = settings[i+1];
					
					// Check if this key is already in keys
					if (keys.contains(key)) {
						// Going to replace the ...
						System.out.println("External config replace setting for key " + key);
						int index = keys.indexOf(key);
						values.set(index, value);
					}
					
					else {
						keys.add(key);
						values.add(value);
					}
				}
			}
			
			System.out.println("Output all key-value pairs need to modify for " + vm.name);
			for (int i=0;i<keys.size();i++)
				System.out.println("Key: " + keys.get(i) + " - value: " + values.get(i));
			
			config.setKeyValue(keys, values, newPath);
			
			// Then upload this file to the server to replace the CGconfig....
			cmd = "sh " + deploymentManager.baseDict + "/client_deploy/scripts/modify_qcow_file.sh " + deploymentManager.baseDict+"/client_deploy/VMs/" + vm.name + "/" + " " + vm.name+".qcow2" + " ";
			cmd += newPath + " " + filename;
			deploymentManager.executeLinuxCommandWithoutException(cmd);
		}
	}
	
	private void modifySqlSettings() throws Exception {
		for (VMInfo vm : vmList) {
			if (!vm.type.equals("SQL"))
				continue;
		
			// TODO Auto-generated method stub
			out.println("SQL ram size for ;" + vm.name);
			input = in.readLine();
			System.out.println("The ram size for " + vm.name + " is " + input);
			int ramsize = Integer.parseInt(input);
			String localPath = deploymentManager.baseDict+"/client_deploy/VMs/" + vm.name + "/my.cnf";
			String newPath = deploymentManager.baseDict+"/client_deploy/VMs/" + vm.name + "/new_my.cnf";
			
			// According to the ramsize - use the different file name
			String filename;
			if (ramsize >= 4096)
				filename = "/etc/my_4096.cnf";
			else if (ramsize >= 2048)
				filename = "/etc/my_2048.cnf";
			else if (ramsize >= 1024)
				filename = "/etc/my_1024.cnf";
			else 
				filename = "/etc/my_1024.cnf";
			
			System.out.println("For vm " + vm.name + "; the ramsize is " + ramsize + "; and the config file I am using is " + filename);
			
			// Download this file from the server
			String cmd = "sh " + deploymentManager.baseDict + "client_deploy/scripts/get_qcow_file.sh " + deploymentManager.baseDict+"/client_deploy/VMs/" + vm.name + "/" + " " + vm.name+".qcow2" + " ";
			cmd += localPath + " " + filename;
			deploymentManager.executeLinuxCommandWithoutException(cmd);
			
			// Then modify the file.. use config modifier
			ConfigModifier config = new ConfigModifier(localPath);
			ArrayList<String> keys = new ArrayList<String>();
			ArrayList<String> values = new ArrayList<String>();
			
			// Add personal features from XML config
			out.println("Give me extra settings for sql");
			String setting = in.readLine();
			System.out.println("The extra setting for sql I got is " +  setting);
			String[] settings = setting.split(";");
			
			if (!setting.isEmpty()) {
				for (int i=0;i<settings.length;i+=2) {
					String key = settings[i];
					String value = settings[i+1];
					
					// Check if this key is already in keys
					if (keys.contains(key)) {
						// Going to replace the ...
						System.out.println("External config replace setting for key " + key);
						int index = keys.indexOf(key);
						values.set(index, value);
					}
					
					else {
						keys.add(key);
						values.add(value);
					}
				}
			}
			
			System.out.println("Output all key-value pairs need to modify for " + vm.name);
			for (int i=0;i<keys.size();i++)
				System.out.println("Key: " + keys.get(i) + " - value: " + values.get(i));
			
			config.setKeyValue(keys, values, newPath);
			
			// Then upload this file to the server to replace the my.cnf....
			cmd = "sh " + deploymentManager.baseDict + "client_deploy/scripts/modify_qcow_file.sh " + deploymentManager.baseDict+"client_deploy/VMs/" + vm.name + "/" + " " + vm.name+".qcow2" + " ";
			cmd += newPath + " " + "/etc/my.cnf";
			deploymentManager.executeLinuxCommandWithoutException(cmd);
		}
	}

	private void modifyAppSettings() throws Exception {
		// First get the image IP...
		out.println("Give me first avaiable image IP!");
		String imageIP = in.readLine();
		System.out.println("For the current environment, I have all image ip: " + imageIP);
		
		// Get the SQL, solr, and cassandra IPs
		out.println("Give me all avaiable sqlServer IP!");
		String sqlList = in.readLine();
		System.out.println("For the current environment, I have all the following sqlServer IPs: " + sqlList);
		String[] sql = sqlList.split(";");
		out.println("Give me all avaiable casServer IP!");
		String casList = in.readLine();
		System.out.println("For the current environment, I have all the following casServer IPs: " + casList);
		String[] cas = casList.split(";");
		out.println("Give me all avaiable solrServer IP!");
		String solrList = in.readLine();
		System.out.println("For the current environment, I have all the following solrServer IPs: " + solrList);
		String[] solr = solrList.split(";");		
		
		for (VMInfo vm : vmList) {
			if (!vm.type.equals("Application"))
				continue;
		
			System.out.println("Going to config app server " + vm.name);
			
			// First change the /etc/fstab to map the local img _dir
			String local_image_dir =  "/usr/share/tomcat7/webapps/img";
			CharSequence check = "/usr/share/tomcat7/webapps/img";
			String localPath = deploymentManager.baseDict+"client_deploy/VMs/" + vm.name + "/fstab";
			String newPath = deploymentManager.baseDict+"client_deploy/VMs/" + vm.name + "/new_fstab";
			String cmd = "sh " + deploymentManager.baseDict + "client_deploy/scripts/get_qcow_file.sh " + deploymentManager.baseDict+"client_deploy/VMs/" + vm.name + "/" + " " + vm.name+".qcow2" + " ";
			cmd += localPath + " " + "/etc/fstab";
			deploymentManager.executeLinuxCommandWithoutException(cmd);
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(localPath)));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newPath)));
			
			// Then read in all the files
			StringBuilder sb = new StringBuilder();
			String line;
			boolean find = false;
			while ((line = reader.readLine()) != null) {

				// If begin with .....
				if (line.contains(check)) {
					find = true;
					sb.append(imageIP+":/img " + local_image_dir + " nfs tcp 0 0\n");
				}
				else {
					sb.append(line + "\n");
				}
			}		
			
			// Append if we got to the end and it was not present
			if (!find)
				sb.append(imageIP+":/img " + local_image_dir + " nfs tcp 0 0\n");
			
			// After all write to the file....
			writer.write(sb.toString());
			reader.close();
			writer.flush();
			writer.close();
			
			cmd = "sh " + deploymentManager.baseDict + "client_deploy/scripts/modify_qcow_file.sh " + deploymentManager.baseDict+"client_deploy/VMs/" + vm.name + "/" + " " + vm.name+".qcow2" + " ";
			cmd += newPath + " " + "/etc/fstab";
			deploymentManager.executeLinuxCommandWithoutException(cmd);
			
			// Until here .... the fstab modification is complete....
			
			// Going to modify the cmartconfig.txt.....
			localPath = deploymentManager.baseDict+"/client_deploy/VMs/" + vm.name + "/cmartconfig.txt";
			newPath = deploymentManager.baseDict+"/client_deploy/VMs/" + vm.name + "/new_cmartconfig.txt";
			
			// See if there is a new one to replace the old one
			if(newCmartconfig==null){
				// Copy the file from the VM
				cmd = "sh " + deploymentManager.baseDict + "client_deploy/scripts/get_qcow_file.sh " + deploymentManager.baseDict+"client_deploy/VMs/" + vm.name + "/" + " " + vm.name+".qcow2" + " ";
				cmd += localPath + " " + "/cmartconfig.txt";			
			} else{
				// Copy the new file
				cmd = "/bin/cp -f "+deploymentManager.baseDict+"/"+ newCmartconfig + " " + localPath;
			}
			
			// Get the file
			deploymentManager.executeLinuxCommandWithoutException(cmd);
			
			// Then modify the file.. use config modifier
			ConfigModifier config = new ConfigModifier(localPath);
			ArrayList<String> keys = new ArrayList<String>();
			ArrayList<String> values = new ArrayList<String>();
			
			// Add all the key-value pair for IP settings....
			keys.add("linux_local_image_dir");
			values.add(local_image_dir);
			
			keys.add("remote_image_ip");
			values.add(imageIP + ":/");
			//keys.add("remote_image_dir");
			//values.add("netimg/");
			
			// Used to update conn strings
			
			out.println("Give me first avaiable SQL IP!");
			String sqlIP = in.readLine();
			System.out.println("For the current environment, I have sql ip: " + sqlIP);
			
			if (sqlIP != null && !sqlIP.equals("null")) {
				keys.add("my_database_url");
				
				values.add("jdbc:mysql://" + sqlIP + "/cmart?cachePrepStmts=true&rewriteBatchedStatements=true&autoReconnect=true");
			}		
			
			out.println("Give me first avaiable cas master IP!");
			String casMaSterIP = in.readLine();
			System.out.println("For the current environment, I have cas master ip: " + casMaSterIP);
			
			if (casMaSterIP != null  && !casMaSterIP.equals("null")) {
				keys.add("cassandra_database_url");
				values.add("jdbc:cassandra://" + casMaSterIP + ":9160/CMARTv1");
			}
			
			out.println("Give me first avaiable solr IP!");
			String solrIP = in.readLine();
			System.out.println("For the current environment, I have solr ip: " + solrIP);
			
			if (solrIP != null && !solrIP.equals("null")) {
				keys.add("solr_enabled");
				values.add("1");
				keys.add("solr_url");
				values.add("http://" + solrIP + ":8983/solr");
			}
			else {
				keys.add("solr_enabled");
				values.add("0");
			}
			
			// Add personal features from XML config
			out.println("Give me extra settings for app");
			String setting = in.readLine();
			System.out.println("The extra setting I got is " +  setting);
			String[] settings = setting.split(";");
			
			if (!setting.isEmpty()) {
				for (int i=0;i<settings.length;i+=2) {
					String key = settings[i];
					String value = settings[i+1];
					
					// Check if this key is already in keys
					if (keys.contains(key)) {
						// Going to replace the ...
						System.out.println("External config replace setting for key " + key);
						int index = keys.indexOf(key);
						values.set(index, value);
					}
					
					else {
						keys.add(key);
						values.add(value);
					}
				}
			}
			
			
			
			System.out.println("Output all key-value pairs need to modify for " + vm.name);
			for (int i=0;i<keys.size();i++)
				System.out.println("Key: " + keys.get(i) + " - value: " + values.get(i));
			
			config.setKeyValue(keys, values, newPath);
			
			
			// Cat all of the db ips to the file
			keys = new ArrayList<String>();
			values = new ArrayList<String>();
			for(String sqlip : sql)
				if(!sqlip.equals("")){
					System.out.println("Appending SQL IP " + sqlip);
					keys.add("IPSQL");
					values.add(sqlip);
				}
			for(String casip : cas)	
				if(!casip.equals("")){
					System.out.println("Appending Cassandra IP " + casip);
					keys.add("IPCASS");
					values.add(casip);
				}
			for(String solrip : solr)
				if(!solrip.equals("")){
					System.out.println("Appending Solr IP " + solrip);
					keys.add("IPSOLR");
					values.add(solrip);
				}
			config.append(keys, values, newPath);
			
			// Then upload with the new path
			cmd = "sh " + deploymentManager.baseDict + "client_deploy/scripts/modify_qcow_file.sh " + deploymentManager.baseDict+"client_deploy/VMs/" + vm.name + "/" + " " + vm.name+".qcow2" + " ";
			cmd += newPath + " " + "/cmartconfig.txt";
			deploymentManager.executeLinuxCommandWithoutException(cmd);
		}
	}

	/* The method used to modify the /etc/httpd/conf.d/lb.conf file..... on the VM */
	private void modifyLoadBalancerSettings() throws Exception {
		out.println("Give me all avaiable appServer IP!");
		String appList = in.readLine();
		System.out.println("For the current environment, I have all the following appServer IPs: " + appList);
		String[] apps = appList.split(";");
		
		for (VMInfo vm : vmList) {
			if (!vm.type.equals("LoadBalancer"))
				continue;
			
			System.out.println("Going to configure load balancer on vm name " + vm.name);
			String lb_path = deploymentManager.baseDict+"/client_deploy/VMs/" + vm.name + "/lb.conf";
			String new_lb_path = deploymentManager.baseDict+"/client_deploy/VMs/" + vm.name + "/new_lb.conf";
			deploymentManager.executeLinuxCommandWithException("cp " + deploymentManager.baseDict+"client_deploy/scripts/config/lb.conf " + deploymentManager.baseDict+"client_deploy/VMs/" + vm.name);
			System.out.format("The lb conf I am going to modify %s\n", lb_path);
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(lb_path)));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new_lb_path)));
			
			// Then read in all the files
			StringBuilder sb = new StringBuilder();
			String line;
			int curLine = 0;
			while ((line = reader.readLine()) != null) {
				curLine++;
				sb.append(line + "\n");
				
				// If the curLine is 1,we will going to add all the proxy settings....
				if (curLine == 1) {
					for (String app : apps) {
						sb.append("BalancerMember http://" + app + "/\n");
					}
				}
			}
			
			// After all write to the file....
			writer.write(sb.toString());
			reader.close();
			writer.flush();
			writer.close();
			
			String cmd = "sh " + deploymentManager.baseDict + "/client_deploy/scripts/modify_qcow_file.sh " + deploymentManager.baseDict+"client_deploy/VMs/" + vm.name + "/" + " " + vm.name+".qcow2" + " ";
			cmd += new_lb_path + " " + "/usr/share/apache2/conf.d/lb.conf";
			
			System.out.println("Going to enter the script for modifying the lb.conf for each VM, the cmd is " + cmd);
			deploymentManager.executeLinuxCommandWithoutException(cmd);
		}
	}

	private void rebootAllVMs() throws Exception {
		// TODO Auto-generated method stub
		for (VMInfo vm : vmList) {
			System.out.format("Going to reboot VM %s - imagePath: %s, xmlPath: %s, IP - %s\n", vm.name, vm.imagePath, vm.xmlPath, vm.ip);
			// start VMs
			deploymentManager.executeLinuxCommandWithException("virsh start " + vm.name);
			System.out.println("**** Vm " + vm.name + "reboot...!");
		}
	}

	private void stopAllVMs() throws Exception {
		// TODO Auto-generated method stub
		for (VMInfo vm : vmList) {
			System.out.format("Going to stop VM %s - imagePath: %s, xmlPath: %s, IP - %s\n", vm.name, vm.imagePath, vm.xmlPath, vm.ip);
			
			// Shut down VMs
			deploymentManager.executeLinuxCommandWithException("virsh destroy " + vm.name);
			System.out.println("**** Vm " + vm.name + "shutdowned...!");
		}
	}

	/**
	 * Use a loop to continues ask the server if all VMs are set up....
	 */
	private void loopCheckingAllVMsAreSetUp() throws Exception {
		// TODO Auto-generated method stub
		int retry = 0;
		while (true) {
			out.println("Is all VMs running?");
			input = in.readLine();
			
			retry++;
			if (input.equals("True"))
			{
				System.out.println("ALL vms is running now.");
				return;
			}
			else {
				System.out.println("Still waiting for all Vms to be running...");
				Thread.sleep(30000);
			}
			
			if (retry >= MAX_RETRIES) {
				System.out.println("Some VMs are still not running after max_retries, error!");
				throw new Exception("Some VM is still not running after MAX_RETRIES");
			}
		}
	}

	private void defineVMs() throws Exception {
		for (VMInfo vm : vmList) {
			System.out.format("Going to start VM %s - imagePath: %s, xmlPath: %s, IP - %s\n", vm.name, vm.imagePath, vm.xmlPath, vm.ip);
			
			// Define and start the VMs
			deploymentManager.executeLinuxCommandWithException("virsh define " + vm.xmlPath);
			System.out.println("**** Vm " + vm.name + "defined!");
		}
	}
	
	/**
	 * Start all the VM given the information in vm list...
	 */
	private void startVMs() throws Exception {
		for (VMInfo vm : vmList) {
			System.out.format("Going to start VM %s - imagePath: %s, xmlPath: %s, IP - %s\n", vm.name, vm.imagePath, vm.xmlPath, vm.ip);
			
			// Define and start the VMs
			deploymentManager.executeLinuxCommandWithException("virsh start " + vm.name);
			System.out.println("**** Vm " + vm.name + "started!");
		}
	}

	/**
	 * Function to check that all VMs are upon running.
	 */
	private boolean checkVMsRunning() throws Exception {
		System.out.println("Currently there are " + vmList.size() + " in host " + client.getLocalAddress().getHostAddress());
		
		int retry = 0;
		
		// Change all the state to something else
		for (VMInfo vm : vmList)
			vm.state = "Stopped";
		
		boolean fin = false;
		while ((++retry) < CHECK_VM_STATE_RETRIES) {
			if(!fin) System.out.println("I am on the " + retry + " retry to check VMs are running");
			
			boolean needToGoNextRound = false;
			
			for (VMInfo vm : vmList) {
				// If the state is already running....
				if (vm.state.equals("Running")){
					fin=true;
					continue;
				}
				
				// Ping to check if the vm is running....
				if (pingOK(vm.ip)) {
					vm.state = "Running";
				}
				
				needToGoNextRound = true;
			}
			
			if (needToGoNextRound)
				Thread.sleep(10000);	// sleep 10s.
		}
		
		System.out.println("Output all information for VMs in host " + client.getLocalAddress().getHostAddress());
		boolean allOK = true;
		for (VMInfo vm : vmList) {
			System.out.format("VM Name: %s - ip: %s - State: %s\n", vm.name, vm.ip, vm.state);
			
			if (!vm.state.equals("Running"))
				allOK = false;
		}
		
		return allOK;
	}
	
	/**
	 *  See if we can ping the current given IP
	 * @throws Exception 
	 */
	private boolean pingOK(String beginIP) throws Exception {
		System.out.println("Going to see if  " + beginIP + " is pingable.");
		deploymentManager.executeLinuxCommandWithException("ping -c 2 -s 16 " + beginIP);
		CharSequence ping = "bytes from " + beginIP;
		if (deploymentManager.output.contains(ping)) {
			System.out.println("This ip " + beginIP + " is pingable!");
			return true;
		}
		else {
			System.out.println("This ip " + beginIP + " is not pingable!");
			return false;
		}
	}
	
	/**
	 * Copy the blank image to different places and modify the XML
	 * Then start the VMs
	 * @throws Exception 
	 */
	private void processVMImages() throws Exception {
		out.println("Base directory?");
		input = in.readLine();
		
		String baseFolder = input + "/";
		if (baseFolder.equals("null")) {
			throw new Exception("The base directory cannot be null!");			
		}
		//Create a folder to put the information for each VM....
		deploymentManager.executeLinuxCommandWithException("mkdir -p " + input +"/client_deploy/VMs");
		
		// Create a fold for each VMs
		String vmFolderPath = input + "/client_deploy/VMs";
		String clientIfcfgPath = input + "/client_deploy/vmImages/ifcfg-eth0";
		for (VMInfo vmInfo : vmList) {
			// Create a folder
			String vmPath = vmFolderPath + "/" + vmInfo.name + "/";
			String imageName = vmInfo.name + ".qcow2";
			String xmlName = vmInfo.name + ".xml";
			deploymentManager.executeLinuxCommandWithException("mkdir -p " + vmPath);
			
			System.out.println("!!!! Going to copy the VM image files - take long time :)");		
			
			// As for the vm images files... will only copy is the vm don't present...
			System.out.println("Host " + this.hostIP + " - " + "Before VM local -copy - The current time is " + Calendar.getInstance().getTime());
			deploymentManager.executeLinuxCommandWithoutException("stat -c %s " + vmPath + imageName);
			System.out.format("As for this vm %s - the quiry for vm size will get output: %s, and err: %s\n", vmInfo.name, deploymentManager.output, deploymentManager.errmsg);
			
			// Copy the VM if the image is not present
			if (!deploymentManager.errmsg.isEmpty())
				deploymentManager.executeLinuxCommandWithException("/bin/cp -f " + vmInfo.imagePath + " " + vmPath + imageName);
			
			System.out.println("Host " + this.hostIP + " - " + "After vm local copy - The current time is " + Calendar.getInstance().getTime());
			deploymentManager.executeLinuxCommandWithException("/bin/cp -f " + vmInfo.xmlPath + " " + vmPath + xmlName);
			
			// Change the actual content in the XML to reflect this VM!
			modifyXML(vmPath + xmlName, vmPath + imageName, vmInfo);
			
			// Then config the VM IP....
			out.println("A IP that I can use;" + vmInfo.name);
			input = in.readLine();
			vmInfo.ip = input;
			
			// Get thw gateway
			out.println("Gateway?");
			String gateway = in.readLine();
			
			// Change the file path
			vmInfo.imagePath = vmPath + imageName;
			vmInfo.xmlPath = vmPath + xmlName;
			
			System.out.println("Ip Assignment - " + vmInfo);
			
			System.out.format("encode the ip (ifcfg-eth0) from %s to %s\n", clientIfcfgPath, vmPath + "ifcfg-eth0");
			
			// Then modify the ifcfg-eth0 file to contain the assigned IP information!!!!
			changeIfcfgEth0(clientIfcfgPath, vmPath + "ifcfg-eth0", vmInfo.ip, gateway);
			
			// Then copy the modified version of ifcfg-eth0 into the image file....
			System.out.format("Copying ifcfg-eth0 (%s) to image file %s\n", vmPath + "ifcfg-eth0", vmInfo.imagePath);
			copyIfcfgEth0(baseFolder, vmPath, imageName);
			
		}
	}

	/**
	 * The method used to change the ifcfg-eth0 (given by the filePath), using the given ip...
	 * Basically just append a line "IPADDR=$ip";
	 * @param string
	 * @param ip
	 * @throws FileNotFoundException 
	 */
	private void changeIfcfgEth0(String fromPath, String toPath, String ip, String gateway) throws Exception {
		// Get the input stream of this file and also a output stream
		InputStream input = new FileInputStream(fromPath);
		OutputStream output = new FileOutputStream(toPath);
		
		// First copy everything...
		BufferedReader inputReader = new BufferedReader(new InputStreamReader(input));
		PrintWriter outputWriter = new PrintWriter(new OutputStreamWriter(output));
		
		String line;
		while ((line = inputReader.readLine()) != null) {
			outputWriter.println(line);
		}
		
		// Append the ip info
		String ipInfo = "IPADDR=\"" + ip + "\"" + "\n" + "GATEWAY=\""+gateway+"\"";
		
		System.out.println("Going to append " + ipInfo + " to " + toPath);
		outputWriter.print(ipInfo);
		
		outputWriter.flush();
		
		outputWriter.close();
		inputReader.close();
		input.close();
		output.close();
	}

	/**
	 * Copy the revised ifcfg-eth0 into the VM image...
	 * @throws Exception 
	 */
	private void copyIfcfgEth0(String baseFolder, String vmPath, String imageName) throws Exception {
		System.out.println("Running script to edit files, this can take sometime");
		String modifyScriptPath = baseFolder + "/client_deploy/vmImages/modify_vm_image_ip.sh";
		String cmd = "sh " + modifyScriptPath + " " + vmPath + " " + imageName;
		System.out.println("Going to run " + cmd);
		deploymentManager.executeLinuxCommandWithoutException(cmd);
	}
	
	/** 
	 * Modify the actual content in the XML...
	 * Name and image location....
	 */
	private void modifyXML(String xmlPath, String imagePath, VMInfo vmInfo) throws Exception {
		// Open the XML and change two locations...
		System.out.println("Dealing with xmlPath " + xmlPath);
		File xmlFile = new File(xmlPath);
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document xmlDoc = db.parse(xmlFile);
		xmlDoc.getDocumentElement().normalize();
		
		Element root = xmlDoc.getDocumentElement();

		out.println("SQL ram size for ;" + vmInfo.name);
		input = in.readLine();
		System.out.println("The ram size for " + vmInfo.name + " is " + input);
		int ramsize = Integer.parseInt(input);
			
		// Modify the ram in xml
		root.getElementsByTagName("memory").item(0).getFirstChild().setNodeValue(String.valueOf(ramsize * 1024));
		
		//System.out.println("The got xml Name is " + root.getElementsByTagName("name").item(0).getFirstChild().getNodeValue());
		root.getElementsByTagName("name").item(0).getFirstChild().setNodeValue(vmInfo.name);
		
		// Change the image location
		Node devices = root.getElementsByTagName("devices").item(0);
		Node disk = ((Element)devices).getElementsByTagName("disk").item(0);
		
		//System.out.println("For disk..." + ((Element)disk).getAttribute("type"));
		Node source = ((Element)disk).getElementsByTagName("source").item(0);
		Element sourceNode = (Element)source;
		sourceNode.setAttribute("file", imagePath);
		
		Transformer transFormer = TransformerFactory.newInstance().newTransformer();
		File newFile = new File(xmlPath);
		FileOutputStream fileOutputStream = new FileOutputStream(newFile);
		transFormer.transform(new DOMSource(xmlDoc), new StreamResult(fileOutputStream));
	}

	private void endConnectionWithError(String errMsg) throws IOException {
		out.println("Error so end connection!");
		out.println(errMsg);
		System.out.println("End the socket with errMsg!");
		in.close();
		out.close();
		client.close();
	}
	
	private void endConnection() throws IOException {
		// End the connection.
		out.println("End Connection");
		System.out.println("End the socket!");
		in.close();
		out.close();
		client.close();
	}
	
	/**
	 * The only implemented deployment manager now is for Fedora.
	 */
	private DeploymentManager CreateDeploymentManager(String osType) {
		if (osType.equals("Fedora")) {
			System.out.println("Going to create a Fedora Deployment Manager for this machine");
			return new FedoraManager();
		}
		return null;
	}
	
	/**
	 * Config the VM info for all the VMs going to run on this client host.
	 * @throws Exception
	 */
	private void configVirtualMachines() throws Exception {
		int vmNumber = Integer.parseInt(input);
		System.out.println("For this machine, we are going to install " + vmNumber + " VMs");
		
		for (int i=0; i<vmNumber;i++) {
			out.println("VMInfo for ;" + i);
			input = in.readLine();
			
			// Parse this info into the VMInfo structure
			String[] info = input.split(";");
			VMInfo vm = new VMInfo(info[0], info[1], info[2]);
			vmList.add(vm);
			System.out.format("For i %d, I got %s\n", i, vm.toString());
			
			// Keep track of all the vm type
			if (!VMTypes.contains(vm.type)) {
				VMTypes.add(vm.type);
			}
		}
		
		// Then ask the server to give you all the image and xml need for each ....
		// Ask for the file path and edit it in the VMInfo
		for (String type : VMTypes) {
			out.println("VM Image Path for ;" + type);
			input = in.readLine();
			
			String[] info = input.split(";");
			String imagePath = info[0];
			String xmlPath = info[1];
			System.out.println("For type " + type +"; the imagepath and xml path is " + imagePath +"; " + xmlPath);
			
			for (VMInfo vmInfo : vmList) {
				if (vmInfo.type.equals(type)) {
					vmInfo.imagePath = imagePath;
					vmInfo.xmlPath = xmlPath;
				}
			}
		}
		
		System.out.println("Print out all the vm INFO!!!");
		for (VMInfo vmInfo : vmList) {
			System.out.println(vmInfo);
		}
		
		// First, see if there is need to destroy and undefine any existing VMs!!!!!!
		for (VMInfo info : this.vmList) {
			System.out.println("Checking status of VM " + info.name);
			deploymentManager.executeLinuxCommandWithoutException("virsh domstate " + info.name);
			if (deploymentManager.errmsg.isEmpty()) {
				System.out.println("Going to undefine VM " + info.name);
				deploymentManager.executeLinuxCommandWithoutException("virsh destroy " + info.name);
				deploymentManager.executeLinuxCommandWithException("virsh undefine " + info.name);
			}
		}
		
		Thread.sleep(10000);
	}
	
	public PrintWriter getServerOut(){
		return this.out;
	}
	
	public BufferedReader getServerIn(){
		return this.in;
	}
	
	public DeploymentManager getDeploymentManager(){
		return this.deploymentManager;
	}
	
	public ArrayList<VMInfo> getVMList(){
		return this.vmList;
	}
	
	private static long startTime=0;
	private static int debugLevel=0;
	public static void print(String module, int level, String text){
		if(debugLevel>=level){
			if(module==null) module="";
			if(text==null) text = "";
			
			System.out.println((System.currentTimeMillis()-startTime)/1000 + " sec: (" + module + "): " + text);
		}
	}
	
	public static void main(String[] args) {
		try {
			ClientDeployment deployment = new ClientDeployment(args[0]);
			
			//ClientDeployment deployment = new ClientDeployment("127.0.0.1");
			
			// Do the actual deployment
			deployment.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
