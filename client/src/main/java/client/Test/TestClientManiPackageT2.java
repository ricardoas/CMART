package client.Test;

import java.util.ArrayList;

import org.junit.Test;

import client.clientMain.CMARTurl;
import client.clientMain.ClientGenerator;
import client.clientMain.ClientInfo;
import client.clientMain.RunSettings;

public class TestClientManiPackageT2 {
	public static void main(String[] args) {
		new TestClientManiPackageT2().runTests();
	}
	
	@Test
	public void runTests(){
		ArrayList<String> tests = new ArrayList<String>();
		tests.add(" --nmax=20000 --lenexec=1 --violmax=1 --ownclassonly");
		tests.add(" --nmax=50000 --lenexec=5 --violmax=1  --ownclassonly  --inclprivate --exclfield");
		tests.add(" --nmax=500000 --lenexec=20 --violmax=1 --searchmode=10  --ownclassonly --inclprivate --timeout=60000 --exclfield");
		//tests.add(" --nmax=100000 --lenexec=20 --violmax=1 --searchmode=10  --ownclassonly --inclprivate --timeout=60000");	
		
		for(String test: tests){
			//Sequenic.T2.Main.Junit(Client.class.getName() + test);
			Sequenic.T2.Main.Junit(ClientGenerator.class.getName() + test);
			Sequenic.T2.Main.Junit(ClientInfo.class.getName() + test);
			Sequenic.T2.Main.Junit(CMARTurl.class.getName() + test);
			Sequenic.T2.Main.Junit(RunSettings.class.getName() + test);
		}

	}
}
