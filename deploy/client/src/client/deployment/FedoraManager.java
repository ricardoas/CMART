package client.deployment;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import client.main.ClientDeployment;

/**
 * This class checks that the required software is installed on the host
 * 
 * @author Ji Yang
 * @author Andy Turner
 *
 */
public class FedoraManager extends DeploymentManager {
	@Override
	/**
	 * Function to check KVM is installed
	 * @throws Exception
	 */
	protected boolean checkKVM() throws Exception {
		CharSequence errStr = "virsh: command not found";
		if (checkSoftwareInstall("virsh -version", errStr)) {
			ClientDeployment.print("FedoraManager: checkKVM", 3, "KVM was found on the host");
			return true;
		}
		else {
			ClientDeployment.print("FedoraManager: checkKVM", 2, "KVM was NOT found on the host");
			return false;
		}
	}

	@Override
	/*
	 * Installs KVM on to the host (and libvirt)
	 * 
	 * (non-Javadoc)
	 * @see client.deployment.DeploymentManager#installKVM()
	 */
	protected void installKVM() throws Exception {
		executeLinuxCommandWithoutException("sh " + baseDict + "client_deploy/scripts/Fedora/install_kvm.sh");
	}

	@Override
	/*
	 * Checks that libvirt is installed
	 * 
	 * (non-Javadoc)
	 * @see client.deployment.DeploymentManager#checkLibvirt()
	 */
	protected boolean checkLibvirt() throws Exception {
		// I am sure it is already covered by KVM
		return true;
	}

	@Override
	/*
	 * Installs libvirt on the host
	 * 
	 * (non-Javadoc)
	 * @see client.deployment.DeploymentManager#installLibvirt()
	 */
	protected void installLibvirt() throws Exception {
		// I am sure it is already covered by KVM
		return;
	}

	@Override
	/*
	 * Checks that collectd is installed on the host. This is needed to collect statistics
	 * 
	 * (non-Javadoc)
	 * @see client.deployment.DeploymentManager#checkCollectd()
	 */
	protected boolean checkCollectd() throws Exception {
		// The error strings if collectd is not present
		CharSequence errStr = "collectd: command not found";
		CharSequence errStr1 = "collectd: No such file";
		
		// Check if the software is present
		if (checkSoftwareInstall("collectd -h", errStr) || checkSoftwareInstall("collectd -h", errStr1)) {
			ClientDeployment.print("FedoraManager: checkCollectd", 3, "Collectd was found on the host");
			return true;
		}
		else {
			ClientDeployment.print("FedoraManager: checkCollectd", 2, "Collectd was NOT found on the host");
			return false;
		}
	}

	@Override
	/*
	 * Installs collectd on the hosts
	 * 
	 * (non-Javadoc)
	 * @see client.deployment.DeploymentManager#installCollectd()
	 */
	protected void installCollectd() throws Exception {
		executeLinuxCommandWithoutException("sh " + baseDict + "client_deploy/scripts/Fedora/install_collectd.sh");
	}

	@Override
	/*
	 * Check that guest FS is found
	 * 
	 * (non-Javadoc)
	 * @see client.deployment.DeploymentManager#checkLibGuestFS()
	 */
	protected boolean checkLibGuestFS() throws Exception {
		// The error message is guest FS is not found
		CharSequence errStr = "guestfish: command not found";
		
		// Check if guest FS is installed on the host
		if (checkSoftwareInstall("guestfish --version", errStr)) {
			ClientDeployment.print("FedoraManager: checkLibGuestFS", 3, "Guestfish was found on the host");
			return true;
		}
		else {
			ClientDeployment.print("FedoraManager: checkLibGuestFS", 2, "Guestfish was NOT found on the host");
			return false;
		}
	}

	@Override
	/*
	 * Install guestFS on the host
	 * 
	 * (non-Javadoc)
	 * @see client.deployment.DeploymentManager#installLibGuestFS()
	 */
	protected void installLibGuestFS() throws Exception {
		executeLinuxCommandWithoutException("sh " + baseDict + "client_deploy/scripts/Fedora/install_libguestfs.sh");
	}

}
