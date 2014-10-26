package populator;

import java.util.Date;

/**
 * Used by PopulateItems to make the bids
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
public class makeBid {
	
	/**
	 * Given a bid, make amount that the next user will bid
	 * @param previousBid
	 * @return
	 */
	public static Bid makeBid(Bid previousBid){
		double newBid = roundCurrency((previousBid.getMaxBid() * (CreateAll.rand.nextDouble()+CreateAll.BIDS_OUTBID_FRACTION)));
		if(newBid <= previousBid.getBid())
			newBid = previousBid.getBid()+0.01;
		
		double newMaxBid = roundCurrency( (previousBid.getMaxBid() * (CreateAll.rand.nextDouble()+CreateAll.BIDS_MAX_OUTBID_FRACTION)));
		if(newMaxBid < newBid)
			newMaxBid = newBid;
		
		Date bidDate = new Date(previousBid.getBidDate().getTime() + (long)((CreateAll.START_TIME - previousBid.getBidDate().getTime())*CreateAll.rand.nextDouble()));
		
		return new Bid(-1,
				PopulateOldItems.getSellerID(CreateAll.NO_OF_USERS),
				previousBid.getQuantity(),
				newBid,
				newMaxBid,
				bidDate,
				null,
				null);
	}
	
	public static double roundCurrency(double num){
		return  Math.floor(num*100.0 + 0.5) / 100.0;
	}
}
