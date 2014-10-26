package server.cloudController;

import java.util.Hashtable;

/**
 * A class to assign VM name by given the Type (will be FrontEnd - FrontEnd_0)
 * Will prevent two VM have the same type result in the same name (otherwise libvirt won't like it)
 * 
 * @author Ji Yang
 * @author Andy Turner
 *
 */
public class VMNameAssigner {
	private static Hashtable<String, Integer> table = new Hashtable<String, Integer>();
	
	/**
	 * Get's the next available name for the VM type.
	 * i.e. first "app" = app_0
	 * next app = app_1
	 * etc
	 * 
	 * @param vmType
	 * @return
	 */
	public static synchronized String getVMName(String vmType) {
		// If the VM type is present then we need to get the +1 number
		if (table.containsKey(vmType)) {
			int number = table.get(vmType);
			number++;
			table.remove(vmType);
			table.put(vmType, number);
			return vmType+ "_" + number;
		}
		// else it's the first time we've seen this VM type, so it's 0
		else {
			table.put(vmType, 0);
			return vmType+"_" + 0;
		}
	}
}
