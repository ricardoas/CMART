package client.Tools;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import client.clientMain.RunSettings;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.*;

/**
 * Outputs a histogram of all think times after the client generator run is completed
 * @author Andrew Fox
 */

public class OutputThinkTime {
	String outputFileLocation;				// the location to output the csv file
	Histogram hist;

	public OutputThinkTime(Histogram hist, String outputFileLocation){
		this.hist=hist;
		this.outputFileLocation=outputFileLocation.concat("thinkTimes.csv");

		try {
			outputThinkTimes();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void outputThinkTimes() throws IOException{
		try{
			FileWriter fstreamA = new FileWriter(outputFileLocation,true);
			BufferedWriter out = new BufferedWriter(fstreamA);

			out = new BufferedWriter(fstreamA);
			for (int i=0;i<hist.getNumBins();i++){
				out.write(Long.toString((i+1)*hist.getBinSize()));
				out.write(",");
				out.write(Long.toString(hist.getHistElem(i)));
				out.newLine();
			}
			out.close();
			fstreamA.close();

			if(RunSettings.isOutputMatlab()){
			double[][] src=new double[hist.getNumBins()][2];
			for (int i=0;i<hist.getNumBins();i++){
				src[i][0]=(i+1)*hist.getBinSize();
				src[i][1]=hist.getHistElem(i);
			}
			MLDouble mlDouble=new MLDouble("thinkTimes",src);
			ArrayList<MLArray> list=new ArrayList<MLArray>();
			list.add(mlDouble);
			MatFileWriter mfw=new MatFileWriter(outputFileLocation.replace(".csv", ".mat"),list);
			}
		}catch(Exception e){
			e.printStackTrace();
			System.err.println("Could not output think time data");
		}


	}

}
