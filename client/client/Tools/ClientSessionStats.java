package client.Tools;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import client.clientMain.RunSettings;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

/**
 * Outputs statistics describing the user experience on the website
 * After a client has left the website a .csv is produced giving
 * Number of pages opened, Response Time Average, Number of Request Errors, Length of Session (ms), If the client left due to an error, the probability that the client left because they were annoyed with the user experience
 * @author Andrew Fox
 *
 */

public class ClientSessionStats {
	ArrayList<ClientSessionData> csd=new ArrayList<ClientSessionData>();

	public ClientSessionStats(){
	}

	public void addClientSession(int numPagesOpened,long totalRT,int requestErrors,long startTime,boolean exitDueToError,double annoyedLeaveProb){
		this.csd.add(new ClientSessionData(numPagesOpened,totalRT,requestErrors,startTime,exitDueToError,annoyedLeaveProb));
	}


	public void output(String outputFile) throws IOException{
		FileWriter fstreamA = new FileWriter(outputFile.concat("ClientSessionStats.csv"),true);
		BufferedWriter out = new BufferedWriter(fstreamA);

		out = new BufferedWriter(fstreamA);
		for(ClientSessionData c:csd){
			out.write(c.numPagesOpened+","+c.RTavg+","+c.requestErrors+","+c.sessionLength+","+c.exitDueToError+","+c.annoyedLeaveProb);
			out.newLine();
		}
		out.close();
		fstreamA.close();

		if(RunSettings.isOutputMatlab()){
			double[][] src=new double[csd.size()][6];
			int i=0;
			for (ClientSessionData c:csd){
				src[i][0]=c.numPagesOpened;
				src[i][1]=c.RTavg;
				src[i][2]=c.requestErrors;
				src[i][3]=c.sessionLength;
				if(c.exitDueToError)
					src[i][4]=1;
				else
					src[i][4]=0;
				src[i][5]=c.annoyedLeaveProb;
				i++;
			}
			MLDouble mlDouble=new MLDouble("ClientSessionStats",src);
			ArrayList<MLArray> list=new ArrayList<MLArray>();
			list.add(mlDouble);
			MatFileWriter mfw=new MatFileWriter(outputFile.concat("ClientSessionStats.mat"),list);
		}

	}


	public class ClientSessionData{
		int numPagesOpened;
		double RTavg;
		int requestErrors;
		long sessionLength;
		boolean exitDueToError;
		double annoyedLeaveProb;

		public ClientSessionData(int numPagesOpened,long totalRT,int requestErrors, long startTime, boolean exitDueToError,double annoyedLeaveProb){
			this.numPagesOpened=numPagesOpened;
			this.requestErrors=requestErrors;
			this.RTavg=((double)totalRT)/((double)numPagesOpened);
			this.sessionLength=new Date().getTime()-startTime;
			this.exitDueToError=exitDueToError;
			this.annoyedLeaveProb=annoyedLeaveProb;
		}
	}
}
