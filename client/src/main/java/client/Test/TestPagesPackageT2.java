package client.Test;

import java.util.ArrayList;
import client.Pages.*;
import org.junit.Test;

public class TestPagesPackageT2 {
	public static void main(String[] args) {
		new TestPagesPackageT2().runTests();
	}
	
	@Test
	public void runTests(){
		ArrayList<String> tests = new ArrayList<String>();
		tests.add(" --nmax=20000 --lenexec=1 --violmax=1 --ownclassonly");
		tests.add(" --nmax=50000 --lenexec=5 --violmax=1  --ownclassonly  --inclprivate --exclfield");
		tests.add(" --nmax=500000 --lenexec=20 --violmax=1 --searchmode=10  --ownclassonly --inclprivate --timeout=60000 --exclfield");
		//tests.add(" --nmax=100000 --lenexec=20 --violmax=1 --searchmode=10  --ownclassonly --inclprivate --timeout=60000");	
		
		for(String test: tests){
			Sequenic.T2.Main.Junit(AskQuestionPage.class.getName() + test);
			Sequenic.T2.Main.Junit(BidConfirmPage.class.getName() + test);
			Sequenic.T2.Main.Junit(BidHistoryPage.class.getName() + test);
			Sequenic.T2.Main.Junit(BrowsePage.class.getName() + test);
			Sequenic.T2.Main.Junit(BuyItemPage.class.getName() + test);
			Sequenic.T2.Main.Junit(ConfirmBuyPage.class.getName() + test);
			Sequenic.T2.Main.Junit(ConfirmCommentPage.class.getName() + test);
			Sequenic.T2.Main.Junit(HomePage.class.getName() + test);
			Sequenic.T2.Main.Junit(ItemPage.class.getName() + test);
			Sequenic.T2.Main.Junit(LeaveCommentPage.class.getName() + test);
			Sequenic.T2.Main.Junit(LoginPage.class.getName() + test);
			Sequenic.T2.Main.Junit(LogOutPage.class.getName() + test);
			Sequenic.T2.Main.Junit(MyAccountPage.class.getName() + test);
			Sequenic.T2.Main.Junit(Page.class.getName() + test);
			Sequenic.T2.Main.Junit(ParsePage.class.getName() + test);
			Sequenic.T2.Main.Junit(RegisterUserPage.class.getName() + test);
			Sequenic.T2.Main.Junit(SearchPage.class.getName() + test);
			Sequenic.T2.Main.Junit(SellItemConfirmPage.class.getName() + test);
			Sequenic.T2.Main.Junit(SellItemPage.class.getName() + test);
			Sequenic.T2.Main.Junit(UpdateUserPage.class.getName() + test);
			Sequenic.T2.Main.Junit(UploadImagesPage.class.getName() + test);
			Sequenic.T2.Main.Junit(ViewUserPage.class.getName() + test);
			Sequenic.T2.Main.Junit(WatchVideoPage.class.getName() + test);
		}

	}
}
