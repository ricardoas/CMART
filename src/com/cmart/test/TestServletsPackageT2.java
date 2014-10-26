package com.cmart.test;

/**
 * @author Andy (turner.andy@gmail.com)
 * @since 0.1
 * @version 1.0
 * @date 23rd Aug 2012
 * 
 * C-MART Benchmark
 * Copyright (C) 2011-2012 theONE Networking Group, Carnegie Mellon University, Pittsburgh, PA 15213, U.S.A
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
import java.util.ArrayList;
import org.junit.Test;
import com.cmart.PageServlets.*;

/*
 * Enable assertions before running
 * -ea
 */

public class TestServletsPackageT2 {
	public static void main(String[] args) {
		new TestServletsPackageT2().runTests();
	}
	
	@Test
	public void runTests(){
		ArrayList<String> tests = new ArrayList<String>();
		tests.add(" --nmax=20000 --lenexec=1 --violmax=1 --ownclassonly --timeout=60000");
		//tests.add(" --nmax=10000 --lenexec=5 --violmax=1  --ownclassonly  --inclprivate --exclfield --timeout=400000");
		//tests.add(" --nmax=20000 --lenexec=20 --violmax=1 --searchmode=10  --ownclassonly --inclprivate --timeout=800000 --exclfield");
		//tests.add(" --nmax=100000 --lenexec=20 --violmax=1 --searchmode=10  --ownclassonly --inclprivate --timeout=60000");	
		
		for(String test: tests){
			Sequenic.T2.Main.Junit(AnswerQuestionServlet.class.getName() + test);
			Sequenic.T2.Main.Junit(AskQuestionServlet.class.getName() + test);
			Sequenic.T2.Main.Junit(BidHistoryServlet.class.getName() + test);
			Sequenic.T2.Main.Junit(BrowseCategoryServlet.class.getName() + test);
			Sequenic.T2.Main.Junit(BuyItemServlet.class.getName() + test);
			Sequenic.T2.Main.Junit(CommentItemServlet.class.getName() + test);
			Sequenic.T2.Main.Junit(ConfirmBidServlet.class.getName() + test);
			Sequenic.T2.Main.Junit(ConfirmBuyServlet.class.getName() + test);
			Sequenic.T2.Main.Junit(ConfirmCommentServlet.class.getName() + test);
			Sequenic.T2.Main.Junit(ConfirmSellItemServlet.class.getName() + test);
			//Sequenic.T2.Main.Junit(DBSpeedTestServlet.class.getName() + test); // Will run a database test
			Sequenic.T2.Main.Junit(ExampleBlankServlet.class.getName() + test);
			Sequenic.T2.Main.Junit(FaultServlet.class.getName() + test);
			//Sequenic.T2.Main.Junit(GetBulkDataServlet.class.getName() + test); // Takes EXTREMELY long time - tries to transfer GBs of data
			Sequenic.T2.Main.Junit(GetUsersServlet.class.getName() + test); // Takes EXTREMELY long time - tries to transfer GBs of data
			Sequenic.T2.Main.Junit(HomeServlet.class.getName() + test);
			Sequenic.T2.Main.Junit(LoginServlet.class.getName() + test);
			Sequenic.T2.Main.Junit(LogoutServlet.class.getName() + test);
			Sequenic.T2.Main.Junit(MoveOldItemsServlet.class.getName() + test);
			Sequenic.T2.Main.Junit(MyAccountServlet.class.getName() + test);
			//Sequenic.T2.Main.Junit(PageController.class.getName() + test); // Cannot be instantiated
			Sequenic.T2.Main.Junit(RegisterUserServlet.class.getName() + test);
			Sequenic.T2.Main.Junit(SearchServlet.class.getName() + test);
			Sequenic.T2.Main.Junit(SellItemServlet.class.getName() + test);
			Sequenic.T2.Main.Junit(SellItemImagesServlet.class.getName() + test);
			Sequenic.T2.Main.Junit(StatisticsServlet.class.getName() + test);
			//Sequenic.T2.Main.Junit(TestDBServlet.class.getName() + test); // Will run a database test
			Sequenic.T2.Main.Junit(UpdateUserDetailsServlet.class.getName() + test);
			Sequenic.T2.Main.Junit(ViewItemServlet.class.getName() + test);
			Sequenic.T2.Main.Junit(ViewUserServlet.class.getName() + test);
		}
		
		
	}
}
