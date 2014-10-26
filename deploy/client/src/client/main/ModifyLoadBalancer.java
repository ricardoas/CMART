package client.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import client.deployment.VMInfo;
import client.deployment.DeploymentManager;

/**
 * This class modifies the load balancers VMs to have the IP addresses of the app servers
 * @author andy
 *
 */
public class ModifyLoadBalancer {
	public static void modify(ClientDeployment parent) throws Exception{
		PrintWriter out = parent.getServerOut();
		BufferedReader in = parent.getServerIn();
		DeploymentManager deploymentManager = parent.getDeploymentManager();
				
		out.println("Give me all avaiable appServer IP!");
		String appList = in.readLine();
		System.out.println("For the current environment, I have all the following appServer IPs: " + appList);
		String[] apps = appList.split(";");
		
		for (VMInfo vm : parent.getVMList()) {
			if (!vm.type.equals("LoadBalancer"))
				continue;
			
			System.out.println("Going to configure load balancer on vm name " + vm.name);
			String lb_path = deploymentManager.baseDict+"client_deploy/VMs/" + vm.name + "/lb.conf";
			String new_lb_path = deploymentManager.baseDict+"client_deploy/VMs/" + vm.name + "/new_lb.conf";
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
						sb.append("BalancerMember http://" + app + ":8080/cmart-0.1\n");
					}
				}
			}
			
			// After all write to the file....
			writer.write(sb.toString());
			reader.close();
			writer.flush();
			writer.close();
			
			String cmd = "sh " + deploymentManager.baseDict + "client_deploy/scripts/modify_qcow_file.sh " + deploymentManager.baseDict+"client_deploy/VMs/" + vm.name + "/" + " " + vm.name+".qcow2" + " ";
			cmd += new_lb_path + " " + "/etc/httpd/conf.d/lb.conf";
			System.out.println("Going to enter the script for modifying the lb.conf for each VM, the cmd is " + cmd);
			deploymentManager.executeLinuxCommandWithoutException(cmd);
		}
	}
}
