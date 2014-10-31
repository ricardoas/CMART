package populator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Read a csv file. Used to read in data files
 * 
 * @author andy
 *
 */
public class ReadCSV {
	BufferedReader in;
	
	public ReadCSV(String filename){
		FileReader ins = null;
		try {
			ins = new FileReader(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		in = new BufferedReader(ins);
	}
	
	public ReadCSV(InputStream inStream){
		InputStreamReader ins = new InputStreamReader(inStream);
		in = new BufferedReader(ins);
	}
	
	public String[] readLine() throws IOException{
		return in.readLine().split(",");
	}
	
	public boolean ready() throws IOException{
		return in.ready();
	}
	
	public void close() throws IOException{
		in.close();
	}
}
