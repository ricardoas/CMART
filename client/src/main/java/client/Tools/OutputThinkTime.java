package client.Tools;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

import client.clientMain.RunSettings;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

/**
 * Outputs a histogram of all think times after the client generator run is
 * completed
 *
 * @author Andrew Fox
 */

public class OutputThinkTime {
	private String outputFileLocation; // the location to output the csv file
	private Histogram hist;

	public OutputThinkTime(Histogram hist, String outputFileLocation) {
		this.hist = hist;
		this.outputFileLocation = outputFileLocation.concat("thinkTimes.csv");
	}

	public void outputThinkTimes() {
		try (BufferedWriter out = new BufferedWriter(new FileWriter(this.outputFileLocation, true));) {

			for (int i = 0; i < this.hist.getNumBins(); i++) {
				out.write(Long.toString((i + 1) * this.hist.getBinSize()));
				out.write(",");
				out.write(Long.toString(this.hist.getHistElem(i)));
				out.newLine();
			}

			if (RunSettings.isOutputMatlab()) {
				double[][] src = new double[this.hist.getNumBins()][2];
				for (int i = 0; i < this.hist.getNumBins(); i++) {
					src[i][0] = (i + 1) * this.hist.getBinSize();
					src[i][1] = this.hist.getHistElem(i);
				}
				MLDouble mlDouble = new MLDouble("thinkTimes", src);
				ArrayList<MLArray> list = new ArrayList<MLArray>();
				list.add(mlDouble);
				new MatFileWriter(this.outputFileLocation.replace(".csv", ".mat"), list);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Could not output think time data");
		}

	}

}
