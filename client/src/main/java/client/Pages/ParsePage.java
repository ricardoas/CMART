package client.Pages;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;

import client.Items.ItemCG;

public class ParsePage {
	private static DateFormat df =new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
	// A single item, for pages such as view item page
	private ItemCG item = null;

	// The page header information
	private TreeMap<String, String> js = new TreeMap<String, String>();
	private TreeMap<String, String> css = new TreeMap<String, String>();
	private TreeMap<String, String> a = new TreeMap<String, String>();
	private TreeMap<String, String> img = new TreeMap<String, String>();
	private TreeMap<String, String> error = new TreeMap<String, String>();
	private String pageTitle = "";

	// The previous bids and items from the my account page
	private TreeMap<Long,ItemCG> currentBids = new TreeMap<Long,ItemCG>();			// tracks items from the client is currently bidding on
	private TreeMap<Long,ItemCG> endedAuctions = new TreeMap<Long,ItemCG>();		// tracks items that the user has won
	private TreeMap<Long,ItemCG> purchasedItems = new TreeMap<Long,ItemCG>();		// tracks items the user has purchased (buyNow)
	private TreeMap<Long,ItemCG> sellingItems = new TreeMap<Long,ItemCG>();			// tracks items the user is selling
	private TreeMap<Long,ItemCG> soldItems = new TreeMap<Long,ItemCG>();			// tracks items the user has sold

	// Lists of items for the browse and search page
	private TreeMap<Long,ItemCG> itemList = new TreeMap<Long,ItemCG>();

	private static final String hasMetReserveKeyword = "has";
	private static final String titleKeyword = "<title>";
	private static final String endTitleKeyword = "</title>";

	public void parseHeader(String html){
		TreeMap<String, String> keys = new TreeMap<String, String>();

		// Get page header
		int titleIndex = html.indexOf(titleKeyword);
		int endTitleIndex = html.indexOf(endTitleKeyword);

		if(titleIndex == -1) titleIndex = html.indexOf(titleKeyword.toUpperCase());
		if(endTitleIndex == -1)	endTitleIndex = html.indexOf(endTitleKeyword.toUpperCase());

		if(titleIndex != -1 && endTitleIndex != -1 && titleIndex < endTitleIndex)
			pageTitle = html.substring(titleIndex + titleKeyword.length(), endTitleIndex);

		System.out.println("******** page title: " + pageTitle);

		// Get all js
		keys.put("<script", "src");
		TreeMap<String, String> js = parseAllData(html, keys);
		this.js = js;

		// Get all css
		keys.clear();
		keys.put("<link", "href");
		TreeMap<String, String> css = parseAllData(html, keys);
		this.css = css;

		// Get all a (hyper links)
		keys.clear();
		keys.put("<a", "href");
		TreeMap<String, String> a = parseAllData(html, keys);
		this.a = a;

		// Get all img
		keys.clear();
		keys.put("<img", "src");
		TreeMap<String, String> img = parseAllData(html, keys);
		this.img = img;

		// Get all error
		keys.clear();
		keys.put("<span", "id");
		TreeMap<String, String> error = parseAllData(html, keys);
		this.error = error;
	}

	/**
	 * When you pass the keys to parse, the parser will get all html tags with "<keys.contains(x)". It will return a treemap
	 * containing <keys.get(x), "value between html tags">
	 * 
	 * e.g. keys = <("<span","id")>
	 * html = "<span id="t1">hello</span>other text<span id="t2">world</span>"
	 * returns <("t1","hello"), ("t2","world");
	 * 
	 * @param html
	 * @param keys
	 * @return
	 */
	private TreeMap<String, String> parseAllData(String html, TreeMap<String, String> keys){
		TreeMap<String, String> ret = new TreeMap<String, String>();

		Iterator<String> keyStrs = keys.keySet().iterator();
		Integer place = 0;

		while(keyStrs.hasNext()){
			String key = keyStrs.next();
			String id = keys.get(key);

			String endKey;
			if(key.startsWith("<")) endKey = "</" + key.substring(1) + ">";
			else endKey = "</" + key + ">";

			int lastStartIndex = -1;
			int startIndex = -2;
			int endIndex = -1;

			System.out.println("***** key: " + key + " endKey: " + endKey);

			/*
			 * Search for all of the places where the key exists
			 */
			while(-1 != startIndex){
				//startIndex++;

				boolean selfEnding = false;

				// Look for the start of a key
				startIndex = html.indexOf(key, lastStartIndex);
				System.out.println("**** " + startIndex);

				// If we find the key, get the end of the key and look for values
				if(startIndex != -1){
					// The key could be self ending />, or end with a </key>
					endIndex = html.indexOf(endKey, startIndex);
					int endIndex2 = html.indexOf("/>", startIndex);
					System.out.println("location of i2: " + endIndex2);


					if((endIndex2 != -1 && endIndex2 < endIndex) || endIndex == -1){
						endIndex = endIndex2;
						selfEnding = true;
					}

					// If the user passed another value to read from the tag then read it
					int startIdIndex;
					int endIdIndex;
					String idValue = null;

					if(id != null){
						startIdIndex = html.indexOf(id+"=", startIndex);

						System.out.println("getting the id val, start=" +startIndex + " end="+endIndex + " id=" +startIdIndex);
						//System.out.println(html.substring(startIndex,endIndex));


						// Make sure the id is between the start and end of the key
						if(startIdIndex > startIndex && startIdIndex < endIndex){
							// We want the value between the quote marks
							startIdIndex = html.indexOf("\"", startIdIndex) + 1;
							endIdIndex = html.indexOf("\"", startIdIndex);

							if(endIdIndex > startIdIndex && endIdIndex < endIndex)
								idValue = html.substring(startIdIndex, endIdIndex).trim();
						}
					}

					// If the tag doesn't close itself, read the value in the tag
					String value = null;
					if(!selfEnding){

						// Get the value from the first '>' after the key start and the start of the span end '</key>'
						int closeKeyStartIndex = html.indexOf(">", startIndex) + 1;

						System.out.println("found span, endIndex=" +endIndex  + " clspan=" +closeKeyStartIndex);

						if(closeKeyStartIndex > startIndex && closeKeyStartIndex <= endIndex){
							System.out.println("value: " + html.substring(closeKeyStartIndex, endIndex) + " key " + idValue);
							value = html.substring(closeKeyStartIndex, endIndex).trim();
							//keys.put(spanID, );
						}
					}

					if(idValue != null)
						ret.put(idValue, value);
					else{
						ret.put(place.toString(), value);
						place++;
					}
				}

				lastStartIndex = startIndex+1;
			}
		}

		return ret;
	}

	private void parseData(String html, TreeMap<String, String> keys){
		int lastStartIndex = 0;
		int startIndex = -2;
		int endIndex = -1;

		/*
		 * Search for all spans that may contain data we are looking for
		 */
		while(startIndex != -1){
			// Look for the start of a span
			startIndex = html.indexOf("<span", lastStartIndex);

			// If we find a span, get the end of the span and look for values
			if(startIndex != -1){
				endIndex = html.indexOf("</span>", startIndex);

				int startIdIndex = html.indexOf("id=", startIndex);

				System.out.println("found span, start=" +startIndex + " end="+endIndex + " id=" +startIdIndex);

				// Make sure the id is between the start and end of the span
				if(startIdIndex > startIndex && startIdIndex < endIndex){
					// We want the value between the quote marks
					startIdIndex = html.indexOf("\"", startIdIndex) + 1;
					int endIdIndex = html.indexOf("\"", startIdIndex);

					if(endIdIndex > startIdIndex && endIdIndex < endIndex){
						String spanID = html.substring(startIdIndex, endIdIndex).trim();
						System.out.println("span id is: " + spanID);

						// See if the id is in the values we are trying to parse, if it is we will save the value
						if(keys.containsKey(spanID)){
							// Get the value from the first '>' after the span id and the start of the span end '</span>
							int closeSpanStartIndex = html.indexOf(">", endIdIndex) + 1;

							System.out.println("found span, endIndex=" +endIndex + " endid="+endIdIndex + " clspan=" +closeSpanStartIndex);

							if(closeSpanStartIndex > endIdIndex && closeSpanStartIndex <= endIndex){
								System.out.println("value: " + html.substring(closeSpanStartIndex, endIndex));
								keys.put(spanID, html.substring(closeSpanStartIndex, endIndex).trim());
							}
						}
					}
				}
			}

			lastStartIndex = startIndex+1;
		}
	}

	public void parseMyAccount(String html){
		TreeMap<String, String> keys = new TreeMap<String, String>();
		keys.put("<span", "id");

		TreeMap<String, String> allItemValues = parseAllData(html, keys);

		/*
		 *  Get all of the current bids
		 */
		int i=0;
		while(allItemValues.containsKey("currItemName"+i)){
			ItemCG currItem = new ItemCG(null);

			// ID
			if(allItemValues.get("currItemID"+i) != null){
				try{
					long val = Long.parseLong(allItemValues.get("currItemID"+i));
					currItem.setId(val);
				}
				catch(Exception e){ }
			}

			// Name
			if(allItemValues.get("currItemName"+i) != null) currItem.setName(allItemValues.get("currItemName"+i));

			// Current bid
			if(allItemValues.get("currBid"+i) != null){
				try{
					double val = Double.parseDouble(allItemValues.get("currBid"+i).replace("$", ""));
					currItem.setCurrentBid(val);
				}
				catch(Exception e){ }
			}

			// Max Bid
			if(allItemValues.get("currMaxBid"+i) != null){
				try{
					double val = Double.parseDouble(allItemValues.get("currMaxBid"+i).replace("$", ""));
					currItem.setMaxBid(val);
				}
				catch(Exception e){ }
			}

			// End date
			if(allItemValues.get("currEndDate"+i) != null){
				try{
					Date val = df.parse(allItemValues.get("currEndDate"+i));
					currItem.setStartDate(val);
				}
				catch(Exception e){ }
			}

			this.currentBids.put(currItem.getId(), currItem);

			i++;
		}

		/*
		 *  Get all of the old bids
		 */
		i=0;
		while(allItemValues.containsKey("prevItemName"+i)){
			ItemCG oldItem = new ItemCG(null);

			// ID
			if(allItemValues.get("prevItemID"+i) != null){
				try{
					long val = Long.parseLong(allItemValues.get("prevItemID"+i));
					oldItem.setId(val);
				}
				catch(Exception e){ }
			}

			// Name
			if(allItemValues.get("prevItemName"+i) != null) oldItem.setName(allItemValues.get("prevItemName"+i));

			// Current bid
			if(allItemValues.get("prevBid"+i) != null){
				try{
					double val = Double.parseDouble(allItemValues.get("prevBid"+i).replace("$", ""));
					oldItem.setCurrentBid(val);
				}
				catch(Exception e){ }
			}

			// Max Bid
			if(allItemValues.get("prevMaxBid"+i) != null){
				try{
					double val = Double.parseDouble(allItemValues.get("prevMaxBid"+i).replace("$", ""));
					oldItem.setMaxBid(val);
				}
				catch(Exception e){ }
			}

			// End date
			if(allItemValues.get("prevEndDate"+i) != null){
				try{
					Date val = df.parse(allItemValues.get("prevEndDate"+i));
					oldItem.setStartDate(val);
				}
				catch(Exception e){ }
			}

			this.endedAuctions.put(oldItem.getId(), oldItem);

			i++;
		}

		/*
		 * Get purchases
		 */
		i=0;
		while(allItemValues.containsKey("purchItemName"+i)){
			ItemCG purchItem = new ItemCG(null);

			// ID
			if(allItemValues.get("purchItemID"+i) != null){
				try{
					long val = Long.parseLong(allItemValues.get("purchItemID"+i));
					purchItem.setId(val);
				}
				catch(Exception e){ }
			}

			// Name
			if(allItemValues.get("purchItemName"+i) != null) purchItem.setName(allItemValues.get("purchItemName"+i));

			// Current bid
			if(allItemValues.get("purchPrice"+i) != null){
				try{
					double val = Double.parseDouble(allItemValues.get("purchPrice"+i).replace("$", ""));
					purchItem.setCurrentBid(val);
				}
				catch(Exception e){ }
			}

			// Max Bid
			if(allItemValues.get("purchQuantity"+i) != null){
				try{
					long val = Long.parseLong(allItemValues.get("purchQuantity"+i));
					purchItem.setQuantity(val);
				}
				catch(Exception e){ }
			}

			// End date
			if(allItemValues.get("purchEndDate"+i) != null){
				try{
					Date val = df.parse(allItemValues.get("purchEndDate"+i));
					purchItem.setStartDate(val);
				}
				catch(Exception e){ }
			}

			this.endedAuctions.put(purchItem.getId(), purchItem);

			i++;
		}

		/*
		 * selling items
		 */
		i=0;
		while(allItemValues.containsKey("currSellItemName"+i)){
			ItemCG currSellItem = new ItemCG(null);

			// ID
			if(allItemValues.get("currSellItemID"+i) != null){
				try{
					long val = Long.parseLong(allItemValues.get("currSellItemID"+i));
					currSellItem.setId(val);
				}
				catch(Exception e){ }
			}

			// Name
			if(allItemValues.get("currSellItemName"+i) != null) currSellItem.setName(allItemValues.get("currSellItemName"+i));

			// Max Bid
			if(allItemValues.get("currSellMaxBid"+i) != null){
				try{
					double val = Double.parseDouble(allItemValues.get("currSellMaxBid"+i).replace("$", ""));
					currSellItem.setMaxBid(val);
				}
				catch(Exception e){ }
			}

			// End date
			if(allItemValues.get("currSellEndDate"+i) != null){
				try{
					Date val = df.parse(allItemValues.get("currSellEndDate"+i));
					currSellItem.setStartDate(val);
				}
				catch(Exception e){ }
			}

			this.sellingItems.put(currSellItem.getId(), currSellItem);

			i++;
		}

		/*
		 * previous selling items
		 */
		i=0;
		while(allItemValues.containsKey("prevSellItemName"+i)){
			ItemCG prevSellItem = new ItemCG(null);

			// ID
			if(allItemValues.get("prevSellItemID"+i) != null){
				try{
					long val = Long.parseLong(allItemValues.get("prevSellItemID"+i));
					prevSellItem.setId(val);
				}
				catch(Exception e){ }
			}

			// Name
			if(allItemValues.get("prevSellItemName"+i) != null) prevSellItem.setName(allItemValues.get("prevSellItemName"+i));

			// Max Bid
			if(allItemValues.get("prevSellMaxBid"+i) != null){
				try{
					double val = Double.parseDouble(allItemValues.get("prevSellMaxBid"+i).replace("$", ""));
					prevSellItem.setMaxBid(val);
				}
				catch(Exception e){ }
			}

			// End date
			if(allItemValues.get("prevSellEndDate"+i) != null){
				try{
					Date val = df.parse(allItemValues.get("prevSellEndDate"+i));
					prevSellItem.setStartDate(val);
				}
				catch(Exception e){ }
			}

			this.soldItems.put(prevSellItem.getId(), prevSellItem);

			i++;
		}
	}

	public void parseItemsList(String html){
		TreeMap<String, String> keys = new TreeMap<String, String>();
		keys.put("<span", "id");

		TreeMap<String, String> allItemValues = parseAllData(html, keys);


		/*
		 * Parse the items data
		 */
		int i=0;
		while(allItemValues.containsKey("itemID"+i)){
			ItemCG item = new ItemCG(null);

			// ID
			if (allItemValues.get("itemID" + i) != null) {
				try {
					long val = Long.parseLong(allItemValues.get("itemID" + i));
					item.setId(val);
				} catch (Exception e) {
				}
			}

			// Name
			if (allItemValues.get("itemName" + i) != null)
				item.setName(allItemValues.get("itemName" + i));

			// Current Bid
			if (allItemValues.get("currentBid" + i) != null) {
				try {
					double val = Double.parseDouble(allItemValues.get("currentBid" + i).replace("$", ""));
					item.setMaxBid(val);
				} catch (Exception e) {
				}
			}

			// End date
			if (allItemValues.get("endDate" + i) != null) {
				try {
					Date val = df.parse(allItemValues.get("endDate" + i));
					item.setStartDate(val);
				} catch (Exception e) {
				}
			}

			itemList.put(item.getId(), item);
		}
	}

	public void parseViewItem(String html){
		TreeMap<String, String> itemValues = new TreeMap<String, String>();
		itemValues.put("itemID", null);
		itemValues.put("name", null);
		itemValues.put("condition", null);
		itemValues.put("seller", null);
		itemValues.put("sellerRating", null);
		itemValues.put("startDate", null);
		itemValues.put("endDate", null);
		itemValues.put("noOfBids", null);
		itemValues.put("currentBid", null);
		itemValues.put("quantity", null);
		itemValues.put("description", null);
		itemValues.put("reserveMet", null);
		itemValues.put("buyNowPrice", null);

		parseData(html, itemValues);

		/*
		 * Now we have the items values we can create the item object
		 */
		ItemCG item = new ItemCG(null);

		if(itemValues.get("itemID") != null){
			try{
				long val = Long.parseLong(itemValues.get("itemID"));
				item.setId(val);
			}
			catch(Exception e){ }
		}

		if(itemValues.get("quantity") != null){
			try{
				long val = Long.parseLong(itemValues.get("quantity"));
				item.setId(val);
			}
			catch(Exception e){ }
		}

		if(itemValues.get("name") != null) item.setName(itemValues.get("name"));
		if(itemValues.get("description") != null) item.setName(itemValues.get("description"));

		if(itemValues.get("currentBid") != null){
			try{
				double val = Double.parseDouble(itemValues.get("currentBid").replace("$", ""));
				item.setCurrentBid(val);
			}
			catch(Exception e){ }
		}

		if(itemValues.get("reserveMet") != null){
			if(itemValues.get("reserveMet").contains(hasMetReserveKeyword)) item.setReserveMet(true);
			else item.setReserveMet(false);
		}

		if(itemValues.get("buyNowPrice") != null){
			try{
				double val = Double.parseDouble(itemValues.get("buyNowPrice").replace("$", ""));
				item.setBuyNowPrice(val);
			}
			catch(Exception e){ }
		}

		if(itemValues.get("noOfBids") != null){
			try{
				long val = Long.parseLong(itemValues.get("noOfBids"));
				item.setNoOfBids(val);
			}
			catch(Exception e){ }
		}

		if(itemValues.get("sellerID") != null){
			try{
				long val = Long.parseLong(itemValues.get("sellerID"));
				item.setSellerID(val);
			}
			catch(Exception e){ }
		}

		if(itemValues.get("sellerRating") != null){
			try{
				long val = Long.parseLong(itemValues.get("sellerRating"));
				item.setSellerID(val);
			}
			catch(Exception e){ }
		}

		if(itemValues.get("startDate") != null){
			try{
				Date val = df.parse(itemValues.get("startDate"));
				item.setStartDate(val);
			}
			catch(Exception e){ }
		}

		if(itemValues.get("endDate") != null){
			try{
				Date val = df.parse(itemValues.get("endBids"));
				item.setStartDate(val);
			}
			catch(Exception e){ }
		}

		System.out.println(itemValues.get("itemID"));
		this.item = item;
	}

	public static void main(String[] args){
		/*
		 * Example of parsing an items data
		 */
		String html  = "<html>test<span id=\"itemID\">val</span>ignored<span id=\"itemID2\">val2</span><span id=\"seller\">5</span>";
		ParsePage p = new ParsePage();

		p.parseViewItem(html);
		System.out.println("Finished");


		/*
		 * example of parsing all '<test' tags
		 */
		System.out.println("\n\n");
		html = "<span id=\"t1\">hello</span>other text<span id=\"t2\">world</span><span id=\"self end\" />";
		TreeMap<String, String> keys = new TreeMap<String, String>();

		keys.put("<span", "id");

		TreeMap<String, String> ret = p.parseAllData(html, keys);

		Iterator<String> strs = ret.keySet().iterator();
		while(strs.hasNext()){
			String str = strs.next();

			System.out.println("key: " + str + " value: " + ret.get(str));
		}

		/*
		 * example of parsing all '<test' tags with not other value to read
		 */
		System.out.println("\n\n");
		html = "<span id=\"t1\">hello</span>other text<span id=\"t2\">world</span><span id=\"self end\" /><span id=\"test\">next</span>";
		keys = new TreeMap<String, String>();

		keys.put("<span", "id");

		ret = p.parseAllData(html, keys);

		strs = ret.keySet().iterator();
		while(strs.hasNext()){
			String str = strs.next();

			System.out.println("key: " + str + " value: " + ret.get(str));
		}

		html = "<script src=\"test.js\" /><span id=\"t1\">hello</span>other text<TITLE>the title</title><span id=\"t2\">world</span><a href=\"test.html?user=8\">next page</a><span id=\"self end\" /><span id=\"test\">next</span>";

		p.parseHeader(html);
	}
}
