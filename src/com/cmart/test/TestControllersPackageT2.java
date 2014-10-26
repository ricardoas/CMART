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
import com.cmart.PageControllers.*;

/*
 * Enable assertions before running
 * -ea
 */

public class TestControllersPackageT2 {
	public static void main(String[] args) {
		System.out.println("Running controller T2 package tests");
		new TestControllersPackageT2().runTests();
		System.out.println("Finished controller T2 package tests");
	}
	
	@Test
	public void runTests(){
		ArrayList<String> tests = new ArrayList<String>();
		tests.add(" --nmax=20000 --lenexec=1 --violmax=1 --ownclassonly --timeout=60000");
		tests.add(" --nmax=10000 --lenexec=5 --violmax=1  --ownclassonly  --inclprivate --exclfield --timeout=400000");
		tests.add(" --nmax=15000 --lenexec=20 --violmax=1 --searchmode=10  --ownclassonly --inclprivate --timeout=800000 --exclfield");
		//tests.add(" --nmax=100000 --lenexec=20 --violmax=1 --searchmode=10  --ownclassonly --inclprivate --timeout=60000");	
		
		for(String test: tests){
			Sequenic.T2.Main.Junit(AnswerQuestionController.class.getName() + test);
			Sequenic.T2.Main.Junit(AskQuestionController.class.getName() + test);
			Sequenic.T2.Main.Junit(BidHistoryController.class.getName() + test);
			Sequenic.T2.Main.Junit(BrowseCategoryController.class.getName() + test);
			Sequenic.T2.Main.Junit(BuyItemController.class.getName() + test);
			Sequenic.T2.Main.Junit(CommentItemController.class.getName() + test);
			Sequenic.T2.Main.Junit(ConfirmBidController.class.getName() + test);
			Sequenic.T2.Main.Junit(ConfirmBuyController.class.getName() + test);
			Sequenic.T2.Main.Junit(ConfirmCommentController.class.getName() + test);
			Sequenic.T2.Main.Junit(ConfirmSellItemController.class.getName() + test);
			//Sequenic.T2.Main.Junit(DBSpeedTestController.class.getName() + test); // Will run a database test
			Sequenic.T2.Main.Junit(ExampleBlankController.class.getName() + test);
			Sequenic.T2.Main.Junit(FaultController.class.getName() + test);
			//Sequenic.T2.Main.Junit(GetBulkDataController.class.getName() + test); // Takes EXTREMELY long time - tries to transfer GBs of data
			Sequenic.T2.Main.Junit(GetUsersController.class.getName() + test); // Takes EXTREMELY long time - tries to transfer GBs of data
			Sequenic.T2.Main.Junit(HomeController.class.getName() + test);
			Sequenic.T2.Main.Junit(LoginController.class.getName() + test);
			Sequenic.T2.Main.Junit(LogoutController.class.getName() + test);
			Sequenic.T2.Main.Junit(MoveOldItemsController.class.getName() + test);
			Sequenic.T2.Main.Junit(MyAccountController.class.getName() + test);
			//Sequenic.T2.Main.Junit(PageController.class.getName() + test); // Cannot be instantiated
			Sequenic.T2.Main.Junit(RegisterUserController.class.getName() + test);
			Sequenic.T2.Main.Junit(SearchController.class.getName() + test);
			Sequenic.T2.Main.Junit(SellItemController.class.getName() + test);
			Sequenic.T2.Main.Junit(SellItemImagesController.class.getName() + test);
			Sequenic.T2.Main.Junit(StatisticsController.class.getName() + test);
			//Sequenic.T2.Main.Junit(TestDBController.class.getName() + test); // Will run a database test
			Sequenic.T2.Main.Junit(UpdateUserDetailsController.class.getName() + test);
			Sequenic.T2.Main.Junit(ViewItemController.class.getName() + test);
			Sequenic.T2.Main.Junit(ViewUserController.class.getName() + test);
		}
		
		
	}
}
