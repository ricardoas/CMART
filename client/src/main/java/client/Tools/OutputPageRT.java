package client.Tools;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import client.clientMain.RunSettings;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

/**
 * Outputs a histogram of all think times after the client generator run is completed
 * @author Andrew Fox
 */

public class OutputPageRT {
	String outputFileLocation;				// the location to output the csv file
	ArrayList<Histogram> hists;

	public OutputPageRT(ArrayList<Histogram> hists, String outputFileLocation){
		this.hists=hists;
		this.outputFileLocation=outputFileLocation.concat("pageRTHistograms.csv");

		try {
			outputData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void outputData() throws IOException{
		try{
			FileWriter fstreamA = new FileWriter(outputFileLocation,true);
			BufferedWriter out = new BufferedWriter(fstreamA);

			for (Histogram h:hists){
				StringBuilder outputLine=new StringBuilder();
				for (int i=0;i<h.getNumBins();i++){
					outputLine.append(h.getHistElem(i)).append(",");
				}
				outputLine.deleteCharAt(outputLine.length()-1);
				out.write(outputLine.toString());
				out.newLine();
			}
			out.close();

			if(RunSettings.isOutputMatlab()){
				double[][] src=new double[hists.size()][hists.get(0).getNumBins()];
				int i=0;
				for (Histogram h:hists){
					for (int j=0;j<h.getNumBins();j++){
						src[i][j]=h.getHistElem(j);
					}
					i++;
				}
				MLDouble mlDouble=new MLDouble("pageRTHistograms",src);
				ArrayList<MLArray> list=new ArrayList<MLArray>();
				list.add(mlDouble);
				MatFileWriter mfw=new MatFileWriter(outputFileLocation.replace(".csv", ".mat"),list);
			}

		}catch(Exception e){
			e.printStackTrace();
			System.err.println("Could not output page response time data");
		}

	}

}
