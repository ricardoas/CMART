package client.Test;

import java.util.ArrayList;

import org.junit.Test;

import client.Tools.ClientSessionStats;
import client.Tools.CollectStats;
import client.Tools.Histogram;
import client.Tools.OutputPageRT;
import client.Tools.OutputThinkTime;
import client.Tools.PageTimePair;
import client.Tools.SiteData;
import client.Tools.Stats;
import client.Tools.Stopwatch;

public class TestToolsPackageT2 {
	public static void main(String[] args) {
		new TestToolsPackageT2().runTests();
	}
	
	@Test
	public void runTests(){
		ArrayList<String> tests = new ArrayList<String>();
		tests.add(" --nmax=20000 --lenexec=1 --violmax=1 --ownclassonly");
		tests.add(" --nmax=50000 --lenexec=5 --violmax=1  --ownclassonly  --inclprivate --exclfield");
		tests.add(" --nmax=500000 --lenexec=20 --violmax=1 --searchmode=10  --ownclassonly --inclprivate --timeout=60000 --exclfield");
		//tests.add(" --nmax=100000 --lenexec=20 --violmax=1 --searchmode=10  --ownclassonly --inclprivate --timeout=60000");	
		
		for(String test: tests){
			Sequenic.T2.Main.Junit(ClientSessionStats.class.getName() + test);
			Sequenic.T2.Main.Junit(CollectStats.class.getName() + test);
			Sequenic.T2.Main.Junit(Histogram.class.getName() + test);
			Sequenic.T2.Main.Junit(OutputPageRT.class.getName() + test);
			Sequenic.T2.Main.Junit(OutputThinkTime.class.getName() + test);
			Sequenic.T2.Main.Junit(PageTimePair.class.getName() + test);
			Sequenic.T2.Main.Junit(SiteData.class.getName() + test);
			Sequenic.T2.Main.Junit(Stats.class.getName() + test);
			Sequenic.T2.Main.Junit(Stopwatch.class.getName() + test);
		}

	}
}
