package com.cmart.test.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.cmart.util.PageStatistic;

/**
 * 
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

public class PageStatisticTest {
	PageStatistic ps1;
	PageStatistic ps2;
	
	@Before
	public void setUp() throws Exception {
		ps1 = new PageStatistic(1, "page", 10, 20);
		ps2 = new PageStatistic(-5, null, -4, -6);
	}

	@Test
	public void testAddReading() {
		assertTrue("As bin size was nagative it should have defaulted to 1", ps2.getDBHist().length==1);
		
		ps1.addReading(20, 30, 40, 50, 60);
		ps2.addReading(20, 30, 40, 50, 60);
	}

	@Test
	public void testClear() {
		ps1.addReading(20, 30, 40, 50, 60);
		
		assertTrue("The page stats should have had data", sum(ps1.getDBHist())>=1);
		
		ps1.clear();
		
		assertTrue("The page stats should have no data", sum(ps1.getDBHist())==0l);
		assertTrue("The page stats should have no data", sum(ps1.getParamHist())==0l);
		assertTrue("The page stats should have no data", sum(ps1.getProcHist())==0l);
		assertTrue("The page stats should have no data", sum(ps1.getRenderHist())==0l);
		assertTrue("The page stats should have no data", sum(ps1.getTotalHist())==0l);
	}
	
	private long sum(long[] arr){
		long ret = 0l;
		for(int i=0; i<arr.length; i++)
			ret += arr[i];
		return ret;
	}
	
	@Test
	public void testGetPageName() {
		assertTrue("The page name is incorrect", ps1.getPageName().equals("page"));
		assertTrue("The page name is incorrect", ps2.getPageName()==null);
	}

	@Test
	public void testGetParamHist() {
		ps1.clear();
		
		assertTrue("The param hist is the incorrect length", ps1.getParamHist().length==20);
		
		ps1.addReading(9, 30, 40, 50, 60);
		assertTrue("The param hist value at index 0 is incorrect", ps1.getParamHist()[0]==1 && sum(ps1.getParamHist())==1);
		
		ps1.addReading(20, 30, 40, 50, 60);
		assertTrue("The param hist value at index 2 is incorrect", ps1.getParamHist()[2]==1 && sum(ps1.getParamHist())==2);
		
		ps1.addReading(200000, 30, 40, 50, 60);
		assertTrue("The param hist value at index 19 is incorrect", ps1.getParamHist()[19]==1 && sum(ps1.getParamHist())==3);
	}

	@Test
	public void testGetDBHist() {
		ps1.clear();
		
		assertTrue("The db hist is the incorrect length", ps1.getParamHist().length==20);
		
		ps1.addReading(20, 9, 40, 50, 60);
		assertTrue("The db hist value at index 0 is incorrect", ps1.getDBHist()[0]==1 && sum(ps1.getDBHist())==1);
		
		ps1.addReading(30, 20, 40, 50, 60);
		assertTrue("The db hist value at index 2 is incorrect", ps1.getDBHist()[2]==1 && sum(ps1.getDBHist())==2);
		
		ps1.addReading(20, 200000, 40, 50, 60);
		assertTrue("The db hist value at index 19 is incorrect", ps1.getDBHist()[19]==1 && sum(ps1.getDBHist())==3);
	}

	@Test
	public void testGetProcHist() {
		ps1.clear();
		
		assertTrue("The proc hist is the incorrect length", ps1.getProcHist().length==20);
		
		ps1.addReading(20, 30, 9, 50, 60);
		assertTrue("The proc hist value at index 0 is incorrect", ps1.getProcHist()[0]==1 && sum(ps1.getProcHist())==1);
		
		ps1.addReading(30, 40, 20, 50, 60);
		assertTrue("The proc hist value at index 2 is incorrect", ps1.getProcHist()[2]==1 && sum(ps1.getProcHist())==2);
		
		ps1.addReading(20, 30, 200000, 50, 60);
		assertTrue("The proc hist value at index 19 is incorrect", ps1.getProcHist()[19]==1 && sum(ps1.getProcHist())==3);
	}

	@Test
	public void testGetRenderHist() {
		ps1.clear();
		
		assertTrue("The render hist is the incorrect length", ps1.getRenderHist().length==20);
		
		ps1.addReading(20, 30, 40, 9, 60);
		assertTrue("The render hist value at index 0 is incorrect", ps1.getRenderHist()[0]==1 && sum(ps1.getRenderHist())==1);
		
		ps1.addReading(30, 40, 40, 20, 60);
		assertTrue("The render hist value at index 2 is incorrect", ps1.getRenderHist()[2]==1 && sum(ps1.getRenderHist())==2);
		
		ps1.addReading(20, 30, 40, 200000, 60);
		assertTrue("The render hist value at index 19 is incorrect", ps1.getRenderHist()[19]==1 && sum(ps1.getRenderHist())==3);
	
	}

	@Test
	public void testGetTotalHist() {
		ps1.clear();
		
		assertTrue("The total hist is the incorrect length", ps1.getTotalHist().length==20);
		
		ps1.addReading(20, 30, 40, 50, 9);
		assertTrue("The total hist value at index 0 is incorrect", ps1.getTotalHist()[0]==1 && sum(ps1.getTotalHist())==1);
		
		ps1.addReading(30, 40, 40, 50, 20);
		assertTrue("The total hist value at index 2 is incorrect", ps1.getTotalHist()[2]==1 && sum(ps1.getTotalHist())==2);
		
		ps1.addReading(20, 30, 40, 50, 200000);
		assertTrue("The total hist value at index 19 is incorrect", ps1.getTotalHist()[19]==1 && sum(ps1.getTotalHist())==3);
	
	}

	@Test
	public void testGetHits() {
		ps1.clear();
		assertTrue("The hits incorrect", ps1.getHits()==0);
		
		ps1.addReading(30, 40, 40, 50, 20);
		ps1.addReading(20, 30, 40, 50, 200000);
		
		assertTrue("The hits incorrect", ps1.getHits()==2);
	}

}
