package client.deployment;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import client.main.ClientDeployment;

/**
 * The abstract class for the deployment manager running on this client host. It checks and configures
 * the software required to run the C-MART VMs. For specific OS's this class should be extended with
 * custom install scripts
 * 
 * The current implementation is only FedoraManager
 * @author Ji Yang
 * @author Andy Turner
 *
 */
public abstract class DeploymentManager {
	final static int MAX_RETRIES = 5;
	public String output = "";
	public String errmsg = "";
	public String baseDict = "";
	
	/**
	 * Function to install/check KVM
	 * 
	 * @throws Exception
	 */
	public void installKVMWithCheck() throws Exception {
		int retry = 0;
		ClientDeployment.print("DeploymentManager: installKVMWithCheck", 4, "Enter kvm checking!");		
		
		// Check if KVM is installed
		while (++retry < MAX_RETRIES) {
			if (!checkKVM()) {
				ClientDeployment.print("DeploymentManager: installKVMWithCheck", 3, "Going to install KVM for the " + retry + " retries");		
				installKVM();
				continue;
			}
			
			ClientDeployment.print("DeploymentManager: installKVMWithCheck", 3, "KVM has installed correctly!");	
			
			return;
		}
		
		// Check the final time
		if (!checkKVM())
			throw new Exception("Cannot install KVM after MAX_RETRIES");
		else {
			ClientDeployment.print("DeploymentManager: installKVMWithCheck", 4, "KVM has installed correctly!");	

			return;
		}
	}
	
	/**
	 * This method should check that KVM is installed on the host. Return true if KVM is installed,
	 * false otherwise
	 * 
	 * @return True if KVM is installed
	 * @throws Exception
	 */
	protected abstract boolean checkKVM() throws Exception;
	
	/**
	 * This method should install KVM for the given OS
	 * 
	 * @throws Exception
	 */
	protected abstract void installKVM() throws Exception;
	
	/**
	 * Function to install/check libvirt
	 * 
	 * @throws Exception
	 */
	public void installLibvirtWithCheck() throws Exception {
		int retry = 0;
		
		ClientDeployment.print("DeploymentManager: installLibvirtWithCheck", 4, "Entering libvirt checking");		
		
		// Check libvirt is installed
		while (++retry < MAX_RETRIES) {
			if (!checkLibvirt()) {
				ClientDeployment.print("DeploymentManager: installLibvirtWithCheck", 3, "Going to install Libvirt for the " + retry + " retries");		
				
				installLibvirt();
				continue;
			}
			
			ClientDeployment.print("DeploymentManager: installLibvirtWithCheck", 3, "Libvirt has installed correctly!");		

			return;
		}
		
		// Check the final time
		if (!checkLibvirt())
			throw new Exception("Cannot install Libvirt after MAX_RETRIES");
		else {
			ClientDeployment.print("DeploymentManager: installLibvirtWithCheck", 4, "Libvirt has installed correctly!");		

			return;
		}
	}
	
	/**
	 * This method should check that libvirt is installed
	 * 
	 * @return True if libvirt is installed
	 * @throws Exception
	 */
	protected abstract boolean checkLibvirt() throws Exception;
	
	/**
	 * This method should install libvirt for the given OS
	 * 
	 * @throws Exception
	 */
	protected abstract void installLibvirt() throws Exception;
	
	/**
	 * Function to install/check collectd
	 * 
	 * @throws Exception
	 */
	public void installCollectdWithCheck() throws Exception {
		int retry = 0;
		ClientDeployment.print("DeploymentManager: installCollectdWithCheck", 4, "Enter collectd checking!");		
		
		// Check that collectd is installed
		while (++retry < MAX_RETRIES) {
			
			if (!checkCollectd()) {
				ClientDeployment.print("DeploymentManager: installCollectdWithCheck", 3, "Installing collectd");			
				
				installCollectd();
				continue;
			}
			
			ClientDeployment.print("DeploymentManager: installCollectdWithCheck", 3, "Installing collectd");			
			
			return;
		}
		
		// Check the final time
		if (!checkCollectd())
			throw new Exception("Cannot install Collectd after MAX_RETRIES");
		else {
			ClientDeployment.print("DeploymentManager: installCollectdWithCheck", 4, "Collectd has installed correctly!");			

			return;
		}

	}
	
	/**
	 * This method should check that collectd is installed on the host
	 * 
	 * @return True if collectd is installed
	 * @throws Exception
	 */
	protected abstract boolean checkCollectd() throws Exception;
	
	/**
	 * This method should install collectd on the host
	 * 
	 * @throws Exception
	 */
	protected abstract void installCollectd() throws Exception;
	
	/**
	 * Install libguestfs for modifying the disk image
	 * 
	 * @throws Exception
	 */
	public void installLibGuestFSWithCheck() throws Exception {
		int retry = 0;
		
		ClientDeployment.print("DeploymentManager: installLibGuestFSWithCheck", 4, "Enter libGuestFS checking!");			

		while (++retry < MAX_RETRIES) {
			if (!checkLibGuestFS()) {
				ClientDeployment.print("DeploymentManager: installLibGuestFSWithCheck", 3, "Going to install LibGuestFS for the " + retry + " retries");			

				installLibGuestFS();
				continue;
			}
			
			ClientDeployment.print("DeploymentManager: installLibGuestFSWithCheck", 3, "LibGuestFS has installed correctly!");			

			return;
		}
		
		// Check the final time
		if (!checkLibGuestFS())
			throw new Exception("Cannot install LibGuestFS after MAX_RETRIES");
		else {
			ClientDeployment.print("DeploymentManager: installLibGuestFSWithCheck", 4, "LibGuestFS has installed correctly!");			

			return;
		}
	}
	
	/**
	 * This method should check that guest fs is installed on the host
	 * 
	 * @return True if guest fs is installed
	 * @throws Exception
	 */
	protected abstract boolean checkLibGuestFS() throws Exception;
	
	/**
	 * Installs guest fs for a given OS on a host
	 * 
	 * @throws Exception
	 */
	protected abstract void installLibGuestFS() throws Exception;
	
	/** 
	 * Function to test if the given piece of software is installed
	 * 
	 * @param cmd The command to check
	 * @param errStr The error returned if the software is not installed
	 * @return
	 */
	public boolean checkSoftwareInstall(String cmd, CharSequence errStr) {
		// Test if executing the cmd can result in the errStr?
		try {
			executeLinuxCommandWithoutException(cmd);
			
			if (output.contains(errStr) || errmsg.contains(errStr))
				return false;
			
			return true;
		} catch (Exception e) {
			System.out.println("Exception caught when testing software installation - executing command " + cmd);
			return false;
		}
	}

	/**
	 * Function to execute a linux command - will throw a exception when the stderr is not empty
	 * 
	 * @param cmd
	 * @throws Exception
	 */
	public void executeLinuxCommandWithException(String cmd) throws Exception {
		executeLinuxCommandWithoutException(cmd);
		
		if (!errmsg.isEmpty()) {
			System.out.println("Stderr: " + errmsg + " when executing " + cmd);
			throw new Exception(errmsg);
		}
	}

	/**
	 * Use Runtime().execute and update the stdout and stderr
	 */
	public void executeLinuxCommandWithoutException(String cmd) throws Exception {
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

}
