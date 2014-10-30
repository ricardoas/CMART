package client.Test;

import java.util.ArrayList;

import org.junit.Test;

import client.Items.ItemCG;
import client.Items.SellerCG;

public class TestItemsPackageT2 {
	public static void main(String[] args) {
		new TestItemsPackageT2().runTests();
	}
	
	@Test
	public void runTests(){
		ArrayList<String> tests = new ArrayList<String>();
		tests.add(" --nmax=20000 --lenexec=1 --violmax=1 --ownclassonly");
		tests.add(" --nmax=50000 --lenexec=5 --violmax=1  --ownclassonly  --inclprivate --exclfield");
		tests.add(" --nmax=500000 --lenexec=20 --violmax=1 --searchmode=10  --ownclassonly --inclprivate --timeout=60000 --exclfield");
		//tests.add(" --nmax=100000 --lenexec=20 --violmax=1 --searchmode=10  --ownclassonly --inclprivate --timeout=60000");	
		
		for(String test: tests){
			Sequenic.T2.Main.Junit(ItemCG.class.getName() + test);
			Sequenic.T2.Main.Junit(SellerCG.class.getName() + test);
		}

	}
}
