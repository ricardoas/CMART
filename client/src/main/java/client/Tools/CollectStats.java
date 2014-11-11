package client.Tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimerTask;

import client.clientMain.ClientGenerator;
import client.clientMain.RunSettings;

import com.jmatio.io.MatFileReader;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

/**
 * Obtains and outputs the response time stats
 *
 * @author Andrew Fox
 *
 */

public class CollectStats extends TimerTask {
	private Stats parent; // the stats page creating this
	private Histogram oldHistogram; // the old histogram
	private ClientGenerator cg; // client generator creating the stats page
	private String outputFile = RunSettings.getOutputSiteDataFile();

	public CollectStats(Stats parent, ClientGenerator cg) {
		this.parent = parent;
		this.cg = cg;

	}

	@Override
	public void run() {
		this.oldHistogram = this.parent.getActiveHistogram(); // moves the
		// current
		// histogram to
		// the
		// old histogram
		this.parent.clearActiveHistogram(); // clears the old histogram
		if (this.parent.getRefreshPeriod() > 0) {
			try {
				if (RunSettings.isOutputStats()) {
					outputStats();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void outputStats() throws IOException {
		StringBuilder histStats = new StringBuilder("Hist ");
		System.out.println("Number of Active Clients: " + this.cg.getNumberOfActiveClients());
		System.out.println("Number of Requests: " + this.oldHistogram.getNumEntries() + ", 90% RT: " + this.oldHistogram.getPercentile(0.9));

//		if (RunSettings.isVerbose()) {
//			for (int i = 0; i < 200; i++) {
//				histStats.append(this.oldHistogram.getHistElem(i)).append(" ");
//			}
//			System.out.println(histStats);
//		}

		if (RunSettings.isOutputThinkTimes()) {
			this.outputReqs(this.cg.getNumberOfActiveClients(), this.oldHistogram.getNumEntries());
		}

		ArrayList<Long> RTDist = new ArrayList<Long>();
		for (double i = 0.05; i < 1; i += 0.05) {
			RTDist.add(this.oldHistogram.getPercentile(i));
		}

		this.outputRT(this.oldHistogram.getNumEntries(), RTDist);

		StringBuilder regularSiteStats = openURL((new StringBuilder(RunSettings.getCMARTurl().getFullURL()).append("/statistics")).toString());
		ArrayList<StringBuilder> metrics = new ArrayList<StringBuilder>();
		int start = 0;
		int end = 0;
		for (int i = 0; i <= 18; i++) {
			switch (i) {
			case 0:
				start = regularSiteStats.indexOf("Browse Category,<br>param,") + "Browse Category,<br>param,".length();
				break;
			case 1:
				start = regularSiteStats.indexOf("Buy Item,<br>param,") + "Buy Item,<br>param,".length();
				break;
			case 2:
				start = regularSiteStats.indexOf("Confirm Bid,<br>param,") + "Confirm Bid,<br>param,".length();
				break;
			case 3:
				start = regularSiteStats.indexOf("Confirm Buy,<br>param,") + "Confirm Buy,<br>param,".length();
				break;
			case 4:
				start = regularSiteStats.indexOf("Confirm Sell,<br>param,") + "Confirm Sell,<br>param,".length();
				break;
			case 5:
				start = regularSiteStats.indexOf("Home,<br>param,") + "Home,<br>param,".length();
				break;
			case 6:
				start = regularSiteStats.indexOf("Login,<br>param,") + "Login,<br>param,".length();
				break;
			case 7:
				start = regularSiteStats.indexOf("Logout,<br>param,") + "Logout,<br>param,".length();
				break;
			case 8:
				start = regularSiteStats.indexOf("My Account,<br>param,") + "My Account,<br>param,".length();
				break;
			case 9:
				start = regularSiteStats.indexOf("Register User,<br>param,") + "Register user,<br>param,".length();
				break;
			case 10:
				start = regularSiteStats.indexOf("Search,<br>param,") + "Search,<br>param,".length();
				break;
			case 11:
				start = regularSiteStats.indexOf("Sell Item,<br>param,") + "Sell Item,<br>param,".length();
				break;
			case 12:
				start = regularSiteStats.indexOf("Sell Item Images,<br>param,") + "Sell Item Images,<br>param,".length();
				break;
			case 13:
				start = regularSiteStats.indexOf("Update User Details,<br>param,") + "Update User Details,<br>param,".length();
				break;
			case 14:
				start = regularSiteStats.indexOf("View Item,<br>param,") + "View Item,<br>param,".length();
				break;
			case 15:
				start = regularSiteStats.indexOf("Ask Question,<br>param,") + "Ask Question,<br>param,".length();
				break;
			case 16:
				start = regularSiteStats.indexOf("Leave Comment,<br>param,") + "Leave Comment,<br>param,".length();
				break;
			case 17:
				start = regularSiteStats.indexOf("Confirm Comment,<br>param,") + "Confirm Comment,<br>param,".length();
				break;
			case 18:
				start = regularSiteStats.indexOf("Total,<br>param,") + "Total,<br>param,".length();
				break;
			}
			end = regularSiteStats.indexOf(",", start);
			metrics.add(new StringBuilder(regularSiteStats.substring(start, end)));
		}

		outputPageFreq(metrics);

	}

	/**
	 * Opens a url
	 *
	 * @param urlString
	 *            - url of page to be opened
	 */
	private StringBuilder openURL(String urlString) {
		StringBuilder newPage = new StringBuilder();
		try {
			URL url = new URL(urlString);
			try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));) {
				String pageLine = null;
				while ((pageLine = br.readLine()) != null) {
					newPage.append(pageLine);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return newPage;
	}

	/**
	 * Outputs the number of requests made in the time interval
	 *
	 * @param activeClients
	 *            - number of active clients at the end of the time interval
	 * @param numReqs
	 *            - number of requests made during the time interval
	 * @throws IOException
	 */
	private void outputReqs(int activeClients, long numReqs) throws IOException {
		try (BufferedWriter out = new BufferedWriter(new FileWriter(this.outputFile.concat("numReqs.csv"), true))) {
			out.write(activeClients + "," + numReqs);
			out.newLine();
		}

		if (RunSettings.isOutputMatlab()) {
			File file = new File(this.outputFile.concat("numReqs.mat"));
			MLDouble mldouble = null;
			if (file.exists()) {
				MatFileReader mfr = new MatFileReader(file);
				MLDouble dataValue = null;
				Map<String, MLArray> content = mfr.getContent();
				for (Entry<String, MLArray> e : content.entrySet()) {
					dataValue = (MLDouble) e.getValue();
				}
				double[][] newdata = new double[dataValue.getM() + 1][dataValue.getN()];
				double[][] dataValueArray = dataValue.getArray();
				for (int i = 0; i < dataValue.getM(); i++) {
					for (int j = 0; j < dataValue.getN(); j++) {
						newdata[i][j] = dataValueArray[i][j];
					}
				}
				newdata[dataValue.getM()][0] = activeClients;
				newdata[dataValue.getM()][1] = numReqs;
				mldouble = new MLDouble(dataValue.getName(), newdata);
			} else {
				double[][] src = new double[1][2];
				src[0][0] = activeClients;
				src[0][1] = numReqs;
				mldouble = new MLDouble("numReqs", src);
			}
			ArrayList<MLArray> list = new ArrayList<MLArray>();
			list.add(mldouble);
			new MatFileWriter(this.outputFile.concat("numReqs.mat"), list);
		}
	}

	/**
	 * Outputs Response Time data to a csv file
	 *
	 * @param RT
	 *            - The response time data from the current time interval
	 * @throws IOException
	 */
	private void outputRT(long numReqs, ArrayList<Long> RT) throws IOException {

		try (BufferedWriter out = new BufferedWriter(new FileWriter(this.outputFile.concat("responseTimes.csv"), true))) {

			StringBuilder outputLine = new StringBuilder();
			outputLine.append(numReqs);
			for (long rt : RT) {
				outputLine.append(",").append(rt);
			}
			out.write(outputLine.toString());
			out.newLine();
		}

		if (RunSettings.isOutputMatlab()) {
			File file = new File(this.outputFile.concat("responseTimes.mat"));
			MLDouble mldouble = null;
			if (file.exists()) {
				MatFileReader mfr = new MatFileReader(file);
				MLDouble dataValue = null;
				Map<String, MLArray> content = mfr.getContent();
				for (Entry<String, MLArray> e : content.entrySet()) {
					dataValue = (MLDouble) e.getValue();
				}
				double[][] newdata = new double[dataValue.getM() + 1][dataValue.getN()];
				double[][] dataValueArray = dataValue.getArray();
				for (int i = 0; i < dataValue.getM(); i++) {
					for (int j = 0; j < dataValue.getN(); j++) {
						newdata[i][j] = dataValueArray[i][j];
					}
				}
				newdata[dataValue.getM()][0] = numReqs;
				int i = 1;
				for (long rt : RT) {
					newdata[dataValue.getM()][i] = rt;
					i++;
				}
				mldouble = new MLDouble(dataValue.getName(), newdata);
			} else {
				double[][] src = new double[1][RT.size() + 1];
				src[0][0] = numReqs;
				int i = 1;
				for (long rt : RT) {
					src[0][i] = rt;
					i++;
				}
				mldouble = new MLDouble("numReqs", src);
			}
			ArrayList<MLArray> list = new ArrayList<MLArray>();
			list.add(mldouble);
			new MatFileWriter(this.outputFile.concat("responseTimes.mat"), list);
		}
	}

	/**
	 * Outputs number of page accesses if each page type in each interval
	 *
	 * @throws IOException
	 */
	private void outputPageFreq(ArrayList<StringBuilder> metrics) throws IOException {
		try (BufferedWriter out = new BufferedWriter(new FileWriter(this.outputFile.concat("pageFreq.csv"), true))) {

			StringBuilder outputLine = new StringBuilder();

			for (StringBuilder m : metrics) {
				outputLine.append(m).append(",");
			}
			outputLine.deleteCharAt(outputLine.length() - 1);

			out.write(outputLine.toString());
			out.newLine();
		}
		;

		if (RunSettings.isOutputMatlab()) {
			File file = new File(this.outputFile.concat("pageFreq.mat"));
			MLDouble mldouble = null;
			if (file.exists()) {
				MatFileReader mfr = new MatFileReader(file);
				MLDouble dataValue = null;
				Map<String, MLArray> content = mfr.getContent();
				for (Entry<String, MLArray> e : content.entrySet()) {
					dataValue = (MLDouble) e.getValue();
				}
				double[][] newdata = new double[dataValue.getM() + 1][dataValue.getN()];
				double[][] dataValueArray = dataValue.getArray();
				for (int i = 0; i < dataValue.getM(); i++) {
					for (int j = 0; j < dataValue.getN(); j++) {
						newdata[i][j] = dataValueArray[i][j];
					}
				}
				int i = 0;
				for (StringBuilder m : metrics) {
					newdata[dataValue.getM()][i] = Double.parseDouble(m.toString());
					i++;
				}
				mldouble = new MLDouble(dataValue.getName(), newdata);
			} else {
				double[][] src = new double[1][metrics.size()];
				int i = 0;
				for (StringBuilder m : metrics) {
					src[0][i] = Double.parseDouble(m.toString());
					i++;
				}
				mldouble = new MLDouble("numReqs", src);
			}
			ArrayList<MLArray> list = new ArrayList<MLArray>();
			list.add(mldouble);
			new MatFileWriter(this.outputFile.concat("pageFreq.mat"), list);
		}
	}

}
