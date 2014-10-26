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

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import com.cmart.test.util.*;
import com.cmart.util.*;

/*
 * Enable assertions before running
 * -ea
 */

public class TestUtilPackageAsserts {

	public static void main(String[] args) {
		Bid.suppressImmutableWarning();
		Category.suppressImmutableWarning();
		Item.suppressImmutableWarning();
		PageStatistic.suppressWarnings();
		
		System.out.println("Running util assert package tests");
		new TestUtilPackageAsserts().runTests();
		System.out.println("Finished util assert package tests");
	}
	
	public void runTests(){
		Result result = JUnitCore.runClasses(AccountTest.class);
		for (Failure failure : result.getFailures()) System.out.println(failure.toString());

		result = JUnitCore.runClasses(AddressTest.class);
		for (Failure failure : result.getFailures()) System.out.println(failure.toString());
		
		result = JUnitCore.runClasses(BidTest.class);
		for (Failure failure : result.getFailures()) System.out.println(failure.toString());
		
		result = JUnitCore.runClasses(BlackHoleOutputTest.class);
		for (Failure failure : result.getFailures()) System.out.println(failure.toString());
		
		result = JUnitCore.runClasses(CategoryTest.class);
		for (Failure failure : result.getFailures()) System.out.println(failure.toString());
		
		result = JUnitCore.runClasses(CommentTest.class);
		for (Failure failure : result.getFailures()) System.out.println(failure.toString());
		
		result = JUnitCore.runClasses(ImageTest.class);
		for (Failure failure : result.getFailures()) System.out.println(failure.toString());
		
		result = JUnitCore.runClasses(ItemTest.class);
		for (Failure failure : result.getFailures()) System.out.println(failure.toString());
		
		result = JUnitCore.runClasses(NullStopWatchTest.class);
		for (Failure failure : result.getFailures()) System.out.println(failure.toString());
		
		result = JUnitCore.runClasses(PageStatisticTest.class);
		for (Failure failure : result.getFailures()) System.out.println(failure.toString());
		
		result = JUnitCore.runClasses(PurchaseTest.class);
		for (Failure failure : result.getFailures()) System.out.println(failure.toString());
		
		result = JUnitCore.runClasses(QuestionTest.class);
		for (Failure failure : result.getFailures()) System.out.println(failure.toString());
		
		result = JUnitCore.runClasses(StopWatchTest.class);
		for (Failure failure : result.getFailures()) System.out.println(failure.toString());
		
		result = JUnitCore.runClasses(UserTest.class);
		for (Failure failure : result.getFailures()) System.out.println(failure.toString());
	}
}
