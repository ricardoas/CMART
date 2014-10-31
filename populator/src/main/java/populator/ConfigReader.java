package populator;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.TreeMap;
/**
 * Reads in the configuration file for the rest of the populator
 * 
 * @author andy
 *
 */

public class ConfigReader {
	private TreeMap<String, String> config;
	private StringBuffer errorBuffer;
	private String commentLine = "#";
	
	public ConfigReader(String filename){
		if(filename==null) System.out.println("Error: config file not specified!");
		
		try {
			FileInputStream fin = new FileInputStream(filename);
			DataInputStream din = new DataInputStream(fin);
			BufferedReader bin = new BufferedReader(new InputStreamReader(din));
			String line;
			config = new TreeMap<String, String>();
			StringBuffer valueBuffer = new StringBuffer();
			errorBuffer = new StringBuffer();

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
		} catch (Exception e) {
			System.out.println(e);
		}

	}
	
	public String getString(String key, String defaultValue){
		String value = config.get(key.toLowerCase());
		if (value != null) {
			return value;
		}

		errorBuffer.append("error: '" + key + "' was set incorrectly. Using the default value: " + defaultValue + "\n");
		return defaultValue;
	}
	
	public int getInt(String key, int defaultValue){
		String value = config.get(key);
		
		if (value != null) {
			try{
				int ret = Integer.parseInt(value);
				return ret;
			}
			catch(Exception e){
			}
		}
		
		errorBuffer.append("error: '"+key+"' was set incorrectly. Using the default value: " + defaultValue +"\n");
		return defaultValue;
	}
	
	public double getDouble(String key, double defaultValue){
		String value = config.get(key);
		
		if (value != null) {
			try{
				double ret = Double.parseDouble(value);
				return ret;
			}
			catch(Exception e){
			}
		}
		
		errorBuffer.append("error: '"+key+"' was set incorrectly. Using the default value: " + defaultValue +"\n");
		return defaultValue;
	}
	
	public Boolean getBoolean(String key, Boolean defaultValue){
		String value = config.get(key);
		
		if (value != null) {
			try{
				if (value.equals("1") || value.toLowerCase().equals("true"))
					return Boolean.TRUE;
				else if (value.equals("0") || value.toLowerCase().equals("false"))
					return Boolean.FALSE;
			}
			catch(Exception e){
			}
		}
		
		errorBuffer.append("error: '"+key+"' was set incorrectly. Using the default value: " + defaultValue +"\n");
		return defaultValue;
	}
	
	public void printErrors(){
		System.out.println(errorBuffer.toString());
	}
	
	public void setCommentString(String comment){
		this.commentLine = comment;
	}
}
