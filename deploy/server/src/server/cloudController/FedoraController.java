package server.cloudController;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

import server.cloudController.ThreadInfo.ThreadStatus;
import server.main.DeploymentController;

import server.XMLReader.HostInfo;
import server.XMLReader.VMImageSetting;

import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

/**
 * The implemented fedora deployment server.
 * @author XySx
 *
 */
public class FedoraController extends Controller {

	public FedoraController(String controllerIP, HostInfo client, int clientID) {
		super(controllerIP, client, clientID);
	}

	@Override
	/**
	 * Since fedora automatically got java...
	 * But also write something here...
	 */
	public void installJava() throws Exception {
		executeCommandWithoutException("sh " + baseDirectory + "/client_deploy/scripts/Fedora/install_java.sh");
	}
	
	/**
	 * The function to check if Java is installed in the client... (so we can run the client program on the client).
	 */
	@Override
	public boolean checkJavaAvailability() throws Exception {
		int retry = 0;
		// Retry twice
		while (++retry < 5) {
			Session session = connection.openSession();
			session.execCommand("java -version");
			
			// Get the stdout, stderr from the session
			InputStream stdoutStream = new StreamGobbler(session.getStdout());
			InputStream stderrStream = new StreamGobbler(session.getStderr());
			
	        // User a reader to read the I/O output
	        BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(stdoutStream));
	        BufferedReader stderrReader = new BufferedReader(new InputStreamReader(stderrStream));
	        
	        // Output stdout
	        String output = "";
	        while (true)
	        {
	            String stdoutLine = stdoutReader.readLine();
	            if (stdoutLine == null)
	            {
	                break;
	            }
	            
	            output += stdoutLine;
	            //System.out.println("Stdout: " + stdoutLine);
	        }
	        
	        // Output stderr
	        String errmsg = "";
	        while (true)
	        {
	            String errLine = stderrReader.readLine();

	            if (errLine == null)
	            {
	                break;
	            }
	            
	            errmsg += errLine;
	            //System.out.println("Error: " + errLine);
	        }
	        
	        session.close();
	        stdoutReader.close();
			stderrReader.close();
	        
	        errmsg = errmsg.toLowerCase();
	        output = output.toLowerCase();
			if(errmsg.startsWith("java version") || output.startsWith("java version")){	
				System.out.println("Java is installed");
				return true;
			}
			
			// otherwise Install Java
			System.out.format("I am on my %d retry to install Java for client %s\n", retry, host);
			installJava();
		}
		
		System.out.format("Install java fails for client %s after 5 retries\n", host);
		return false;
	}

	/**
	 * The main route for fedora controller (SSH into the client)
	 */
	@Override
	public void run() {
		this.threadName = Thread.currentThread().getName();
		
		try {
			startConnectionWithRetry();
			System.out.println("connection set up!");
			
			executeCommand("mkdir -p " + baseDirectory);
			executeCommand("cd " + baseDirectory);
			executeCommand("mkdir -p " + baseDirectory + "/client_deploy");
			executeCommand("mkdir -p " + baseDirectory + "/client_deploy/collectd_data");
			executeCommand("rm -rf " + baseDirectory + "/client_deploy/collectd/*");
			executeCommand("mkdir -p " + baseDirectory + "/client_deploy/scripts");
			executeCommand("mkdir -p " + baseDirectory + "/client_deploy/scripts/config");
			executeCommand("mkdir -p " + baseDirectory + "/client_deploy/vmImages");

			System.out.println("Transfer the scrips and client java file");
			transferFile(DeploymentController.deployPath+"/scripts/Fedora.tar", baseDirectory + "/client_deploy/scripts");
			transferFile(DeploymentController.deployPath+"/scripts/collectd_client.conf", baseDirectory + "/client_deploy/scripts");
			
			// Copy the new cmart config if needed
			if(DeploymentController.cmartNewConfig!=null)
				transferFile(DeploymentController.cmartNewConfig, baseDirectory);
			
			// Unzip the scripts....
			String script_path = baseDirectory + "/client_deploy/scripts/";
			executeCommand("tar xvf " + script_path + "/Fedora.tar -C " + script_path);
			transferFile(DeploymentController.deployPath+"/client/deployclient.jar", baseDirectory + "/client_deploy");

			System.out.println("Going to check for the client Java environment");
			if (!checkJavaAvailability()) {
				throw new Exception("Cannot install Java on client " + host);
			}
			
			System.out.println("Check java installation on the client done!");
			
			System.out.println("Going to transfer the images file to this client...");
			
			HostInfo machineInfo = null;
			for (HostInfo info : ControllerServer.clientList) {
				if (this.ip.equals(info.ip)) {
					machineInfo = info;
					break;
				}
			}
			
			if (machineInfo == null)
				throw new Exception("Cannot find machine Info specified in the XML for client " + this.host);
			
			// Transfer the ifcfg-eth0 file for modifying the IP....
			System.out.println("Transfering the ifcfg-eth0 file for VM IP assignment!");
			String ifcfgPath = baseDirectory + "/client_deploy/vmImages/";
			transferFile(DeploymentController.deployPath+"/vm/ifcfg-eth0", ifcfgPath);
			transferFile(DeploymentController.deployPath+"/vm/modify_vm_image_ip.sh", ifcfgPath);
			scpClient.put(DeploymentController.deployPath+"/scripts/modify_qcow_file.sh", baseDirectory+"/client_deploy/scripts/");
			scpClient.put(DeploymentController.deployPath+"/scripts/get_qcow_file.sh", baseDirectory+"/client_deploy/scripts/");
			scpClient.put(DeploymentController.deployPath+"/scripts/config/lb.conf", baseDirectory+"/client_deploy/scripts/config");	
					
			// Copy the VM images and XML files
			System.out.println("Transfering the VM base images");
			for (String vmType : vmTypes) {
				// Get the path for the disk file and xml file
				String imagePath = null;
				String xmlPath = null;
				
				try{
					imagePath = ControllerServer.VMImageSettings.get(vmType).imagePath+ "/";
					xmlPath = ControllerServer.VMImageSettings.get(vmType).xmlPath+ "/";
				}catch(Exception e){
					System.err.println("Error!: The VMType specified for a VM does not exist " + vmType);
					System.err.println("Error!: Please correct this in the deployment XML file. Only specify VM types where you have provided an image");
					throw e;
				}
				
				String vmPath = baseDirectory + "/client_deploy/vmImages/" + vmType + "/";
				
				System.out.println("Making the vmImages folder");
				executeCommand("mkdir -p " + vmPath);
				
				System.out.format("For Type %s - transfer image from %s to %s, and xml from %s to %s\n", vmType, imagePath, vmPath, xmlPath, vmPath);
				
				// As for the vm image, since it is too big...we only consider put the VM only if the vm is not present in the path....
				System.out.println("Host " + this.ip + " - " + "Before remote copy - The current time is " + Calendar.getInstance().getTime());
				this.executeCommandWithoutException("stat -c %s " + vmPath + vmType + ".qcow2");
				System.out.println("As for checking if the current image exist, the msg I got is : " + output);
				System.out.println("The errmsg is " + errmsg);
				
				// If we could not stat the file then copy it
				if (!errmsg.isEmpty()){
					try{
						System.out.println("As the file does not exist we are going to copy it, this will take some time...");
						scpClient.put(imagePath+vmType+".qcow2", vmPath, "0755");
					}catch(Exception e){
						System.err.println("Make sure the file exists " + imagePath+vmType+".qcow2");
						throw e;
					}
				}
				
				System.out.println("Host " + this.ip + " - " + "After remote copy - The current time is " + Calendar.getInstance().getTime());
				scpClient.put(xmlPath+vmType+".xml", vmPath, "0755");
				
				// Store the client file path
				machineInfo.vmTypeSettings.put(vmType, new VMImageSetting(vmType, vmPath + vmType + ".qcow2", vmPath + vmType + ".xml"));
				System.out.println("I am client " + this.host + "; and my vm image setting is " + machineInfo.vmTypeSettings.get(vmType));
			}
			
			// Going to run the client program
			executeCommand("cd " + baseDirectory + "/client_deploy");
			
			collectdServerSetting();		
			
			System.out.println(" ******************** I am going to run the client for " + this.ip);
			//executeCommandWithoutWaiting("java -jar " + baseDirectory + "client_deploy/client.jar " + controllerIP + " &");
			executeCommand("java -jar " + baseDirectory + "/client_deploy/deployclient.jar " + controllerIP);
			
			System.exit(0);
			
			// Keep checking the state...
			// Get the client status entry...
			int rety = 0;
			/*
			System.out.println("Going to query the ThreadInfo for the host " + this.ip);
			ThreadInfo threadInfo = null;
			while (true) {
				rety++;
				for (ThreadInfo info : ControllerServer.clientStatus) {
					if (info.info == null)
						continue;
					
					if (info.info.ip.equals(this.ip)) {
						threadInfo = info;
						break;
					}
				}
				
				if (threadInfo != null)
					break;
				
				Thread.sleep(10000);
			}
			
			System.out.println("Keep monitoring state for " + threadInfo.info.ip + "; I am in the rety" + rety);
			
			rety = 0;
			while (true) {
				rety++;
				if (threadInfo.getStatus() == ThreadStatus.FINISH)
					break;
				else if (threadInfo.getStatus() == ThreadStatus.ERROR)
					throw new Exception("Thread status error!");
				
				// Pul data ..??
				
				Thread.sleep(30000);
			}
			*/
			
	
			//executeLinuxCommandWithException("ls /collectdData");
		} catch (Exception e) {
			e.printStackTrace();	
			return;
		}
	}

	private void executeLinuxCommandWithException(String string) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Set up collectd on the server side, all the host will just report the status to this ....
	 */
	private void collectdServerSetting() {
		
	}
}
