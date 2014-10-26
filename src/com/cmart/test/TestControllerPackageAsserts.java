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
import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import com.cmart.PageControllers.*;
/*
 * Enable assertions before running
 * -ea
 */

public class TestControllerPackageAsserts {

	public static void main(String[] args) {
		System.out.println("Running controller assert package tests");
		new TestControllerPackageAsserts().runTests();
		System.out.println("controller util assert package tests");
	}
	
	@Test
	public void runTests(){
		Result result = JUnitCore.runClasses(AnswerQuestionController.class);
		for (Failure failure : result.getFailures()) System.out.println(failure.toString());
		
		result = JUnitCore.runClasses(AskQuestionController.class);
		for (Failure failure : result.getFailures()) System.out.println(failure.toString());
	}
}
