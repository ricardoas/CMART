package client.deployment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.TreeMap;

import client.main.ClientDeployment;

/**
 * This class reads in the cmart config file, and also outputs a new one with the values configured
 * while deploying the VMs. It is also used for application configs, which are also typically key/value
 * 
 * @author Ji Yang
 * @author Andy Turner
 *
 */
public class ConfigModifier {
	private TreeMap<String, String> config;
	private String commentLine = "#";
	private String filePath;
	
	/**
	 * Creates the ConfigModifier and reads in the key/value pairs from the file.
	 * 
	 * @param filename
	 */
	public ConfigModifier(String filename){
		try {
			// Setup the vars needed to read in the key/value pairs
			FileInputStream fin = new FileInputStream(filename);
			DataInputStream din = new DataInputStream(fin);
			BufferedReader bin = new BufferedReader(new InputStreamReader(din));
			String line;
			config = new TreeMap<String, String>();
			StringBuffer valueBuffer = new StringBuffer();
			this.filePath = filename;
			
			// Loop through the file and read the key/value pairs
			while (bin.ready()) {
				// Read in a line, if it is a comment ignore, otherwise we'll
				// parse it for a value/key pair
				line = bin.readLine();
				if (line != null) {
					if (!line.startsWith(commentLine)) {
						// Split the line at an '=', delete while space and add
						// the value to the config map
						String values[] = line.split("=");

						if (values.length >= 2) {
							String key = values[0].trim();

							valueBuffer.setLength(0);
							for (int i = 1; i < values.length; i++)
								valueBuffer.append(values[i].trim());

							config.put(key.toLowerCase(), valueBuffer.toString());
						}
					}
				}
			}
			
			fin.close();
			din.close();
		} catch (Exception e) {
			ClientDeployment.print("ConfigModifier", 0, e.toString());
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the value of a key
	 * 
	 * @param key the key to get the value of
	 * @return
	 */
	public String getValue(String key) {
		String value = config.get(key);
		if (value != null) {
			return value;
		}

		return null;
	}

	/**
	 *  Given a set of key/value pairs we will copy the original config file and replace values
	 *  with the ones passed and save it as a new file
	 *  
	 * @throws Exception 
	 * 
	 */
	public void setKeyValue(ArrayList<String> keys, ArrayList<String> valueList, String outputFilePath) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFilePath)));
		
		ClientDeployment.print("ConfigModifier: setKeyValue", 3, "Outputting the new config file " +outputFilePath);		
		
		StringBuilder sb = new StringBuilder();
		String line;
		
		while ((line = reader.readLine()) != null) {
			if (!line.startsWith(commentLine)) {
				// If it is a line with meaning, going to get the key value
				// Split the line at an '=', delete while space and add
				// the value to the config map
				String values[] = line.split("=");

				if (values.length >= 2) {
					String originkey = values[0].trim();
					
					boolean find = false;
					// If the key matches on the given keys list, just replace the value with the given value in the value list
					for (int i=0;i<keys.size();i++) {
						String key = keys.get(i);
						
						if (key.equals(originkey)) {
							ClientDeployment.print("ConfigModifier: setKeyValue", 4, "Found key " + key + " in file " + filePath);		
							
							find = true;
							
							// going to replace the original value
							String value = valueList.get(i);
							sb.append(key + " = " + value + "\n");
							break;
						}
					}
					
					if (!find) {
						// If cannot find the key....
						sb.append(line + "\n");
					}
				} 
				else {
					sb.append(line + "\n");
				}
			}
			else {
				// If it is a commentLine, just copy it to output
				sb.append(line + "\n");
			}
		}
		
		// After all write to the file....
		writer.write(sb.toString());
		reader.close();
		writer.flush();
		writer.close();
	}
	
	/*
	 * Append to the config file
	 */
	public void append(ArrayList<String> keys, ArrayList<String> valueList, String outputFilePath) throws Exception {
		// First remove any dups
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
		String line;
		
		while ((line = reader.readLine()) != null) {
			for(int i=0;i<keys.size();){
				if(line.contains(keys.get(i)) && line.contains(valueList.get(i))){
					keys.remove(i);
					valueList.remove(i);
				}
				else i++;
			}
		}
		reader.close();
		
		// Write out the values
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputFilePath, true)));
		for(int i=0; i<keys.size(); i++){
			out.println(keys.get(i) + "=" + valueList.get(i));
		}
		out.close();	
	}
}
