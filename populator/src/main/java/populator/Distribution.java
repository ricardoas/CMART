package populator;

/**
 * A generic distribution that the database data is made from
 * 
 * @author Andy (andrewtu@cmu.edu)
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
public class Distribution {
	private Type type;
	private double min;
	private double max;
	private double mean;
	private double sd;
	private double range;
	private double alpha;
	private double lambda;
	
	public enum Type {
	    UNIFORM, NORMAL
	} 
	
	/**
	 * Make a distribution to draw data from. Used to make database data
	 * @param type
	 * @param min
	 * @param max
	 * @param mean
	 * @param sd
	 */
	public Distribution(int type, double min, double max, double mean, double sd, double alpha, double lambda){
		switch(type){
			case 0: setData(Type.UNIFORM, min, max, mean, sd, alpha, lambda);
			case 1: setData(Type.NORMAL, min, max, mean, sd, alpha, lambda);
		}
	}
	
	public Distribution(Type type, double min, double max, double mean, double sd, double alpha, double lambda){
		setData(type, min, max, mean, sd, alpha, lambda);
	}
	
	/**
	 * Remember the distribution variables
	 * 
	 * @param type
	 * @param min
	 * @param max
	 * @param mean
	 * @param sd
	 */
	private void setData(Type type, double min, double max, double mean, double sd, double alpha, double lambda){
		this.type = type;
		this.min = min;
		this.max = max;
		this.mean = mean;
		this.sd = sd;
		this.range = max - min;
		this.alpha = alpha;
		this.lambda = lambda;
	}
	
	/**
	 * Get the next number from the distribution
	 * 
	 * @return
	 */
	public double getNext(){
		switch(this.type){
		case UNIFORM:
			return CreateAll.rand.nextInt((int)Math.round(range)) + min;
		case NORMAL:
			double temp = (CreateAll.rand.nextGaussian() * sd) + mean;
			if(temp < min) return min;
			else if(temp > max) return max;
			
			else return temp;
		default:
			return -1.0;
		}
	}
	
}
