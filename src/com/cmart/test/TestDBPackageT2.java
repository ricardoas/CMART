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
import com.cmart.DB.*;
import com.cmart.Data.GlobalVars;

/*
 * Enable assertions before running
 * -ea
 */

public class TestDBPackageT2 {
	public static void main(String[] args) {
		new TestDBPackageT2().runTests();
	}
	
	@Test
	public void runTests(){
		GlobalVars.getInstance();
		
		ArrayList<String> tests = new ArrayList<String>();
		tests.add(" --nmax=20000 --lenexec=1 --violmax=1 --ownclassonly --timeout=400000");
		tests.add(" --nmax=3000 --lenexec=5 --violmax=1  --ownclassonly  --inclprivate --exclfield --timeout=400000");
		tests.add(" --nmax=2000 --lenexec=20 --violmax=1 --searchmode=10  --ownclassonly --inclprivate --timeout=600000 --exclfield");
		//tests.add(" --nmax=100000 --lenexec=20 --violmax=1 --searchmode=10  --ownclassonly --inclprivate --timeout=60000");	
		
		for(String test: tests){
			//Sequenic.T2.Main.Junit(DBQuery.class.getName() + test); // Cannot be instantiated
			
			//Sequenic.T2.Main.Junit(CassandraDBQuery.class.getName() + test); // Cannot be instantiated
			
			//Sequenic.T2.Main.Junit(MySQLDBQuery.class.getName() + test); // Cannot be instantiated
			Sequenic.T2.Main.Junit(MySQLINNODBQuery.class.getName() + test); // Cannot be instantiated
		}
		
		
	}
}
