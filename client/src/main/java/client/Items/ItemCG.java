package client.Items;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import client.clientMain.Client;
import client.clientMain.RunSettings;

/**
 * An item on CMART
 * 
 * @author afox1
 *
 */

public class ItemCG {
	private Client client; // client viewing item
	private Random rand = new Random(); // random seed
	// item properties
	private long id; // item id number in cmart database
	private String name; // name of item
	private String description; // description of item
	private long quantity; // quantity of item available
	private double startPrice; // initial price of item
	private double reservePrice; // item reserve price
	private double buyNowPrice = 0; // item buy now price
	private double currentBid = 0; // current bid on item
	private double maxBid = 0; // maximum bid on item
	private long noOfBids; // number of bids on item
	private Date startDate; // date item was put up for auction
	private Date endDate; // end date of auction
	private Date bidDate; // date of last bid
	private long sellerID; // id of seller
	private double sellerRating; // seller rating
	private long categoryID; // item category id
	private int numPics = 0; // number of pictures on item page
	private int listRank; // place on the list where the item ranks
	private int commonSearchTerms = 0; // number of words in common with the
										// search query and the title
	private int categoryDepth = 0; // category depth where item was found on
									// browse page
	private boolean forAuction = false; // if the item is up for auction
	private boolean forBuyNow = false; // if the item is available for buyNow
	private boolean isOwner = false; // if the client is the owner of the item
	private boolean bidder = false; // if the client has bid on the item
	private boolean onList = false; // if this item was found from a search or
									// browse list
	private boolean recommended = false; // if the item is a recommended item on
											// the home page
	private ArrayList<String> images = new ArrayList<String>();
	private Boolean reserveMet = Boolean.FALSE;
	private int numQuestions = 0; // number of questions asked on item page
	private int numAnswers = 0; // number of questions answered on item page
	private boolean myAccount = false; // if the item was seen on the my account
										// page
	private long ts = 0; // for HTML5 - time stamp that the item was accessed
							// from server

	// rating properties
	private double itemRating = 0;
	private double titleLength = 8.123; // normalization terms for each factor
										// of the rating
	private double descriptionLength = 42;
	private double numPicsAvg = 1.72;
	private double sellerRatingLog = 3.513;
	private double endDateDiff = 259200000;
	private double auctionAvg = 44.5 * (1. + ((this.rand.nextDouble() - 0.5) * 0.55));
	private double buyNowAvg = 88.1 * (1. + ((this.rand.nextDouble() - 0.5) * 0.55));
	// private double auctionAvg=54.5*(1.+(rand.nextDouble()-0.5)*0.55);
	// private double buyNowAvg=118.1*(1.+(rand.nextDouble()-0.5)*0.55);
	private double priceDiff = 43.;
	private double listRankMax = 25.;
	private double commonSearchTermsAvg = 2.4 + 0.5;

	private double wantRating = 14;

	public ItemCG(Client client) {
		this.client = client;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return this.id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the quantity
	 */
	public long getQuantity() {
		return this.quantity;
	}

	/**
	 * @param quantity
	 *            the quantity to set
	 */
	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}

	/**
	 * @return the startPrice
	 */
	public double getStartPrice() {
		return this.startPrice;
	}

	/**
	 * @param startPrice
	 *            the startPrice to set
	 */
	public void setStartPrice(double startPrice) {
		this.startPrice = startPrice;
		if (startPrice != 0) {
			this.forAuction = true;
		}
	}

	/**
	 * @return the reservePrice
	 */
	public double getReservePrice() {
		return this.reservePrice;
	}

	/**
	 * @param reservePrice
	 *            the reservePrice to set
	 */
	public void setReservePrice(double reservePrice) {
		this.reservePrice = reservePrice;
	}

	/**
	 * Gets if the reserve price has been met
	 * 
	 * @return
	 */
	public boolean isReserveMet() {
		return this.reserveMet;
	}

	/**
	 * Sets if the reserve has been met
	 * 
	 * @param reserveMet
	 */
	public void setReserveMet(Boolean reserveMet) {
		this.reserveMet = reserveMet;
	}

	/**
	 * @return the buyNowPrice
	 */
	public double getBuyNowPrice() {
		return this.buyNowPrice;
	}

	/**
	 * @param buyNowPrice
	 *            the buyNowPrice to set
	 */
	public void setBuyNowPrice(double buyNowPrice) {
		this.buyNowPrice = buyNowPrice;
		if (buyNowPrice != 0) {
			this.forBuyNow = true;
		}
	}

	/**
	 * @return the currentBid
	 */
	public double getCurrentBid() {
		return this.currentBid;
	}

	/**
	 * @param currentBid
	 *            the currentBid to set
	 */
	public void setCurrentBid(double currentBid) {
		this.currentBid = currentBid;
		this.forAuction = true;
	}

	/**
	 * @return the maxBid
	 */
	public double getMaxBid() {
		return this.maxBid;
	}

	/**
	 * @param maxBid
	 *            the maxBid to set
	 */
	public void setMaxBid(double maxBid) {
		this.maxBid = maxBid;
	}

	/**
	 * @return the noOfBids
	 */
	public long getNoOfBids() {
		return this.noOfBids;
	}

	/**
	 * @param noOfBids
	 *            the noOfBids to set
	 */
	public void setNoOfBids(long noOfBids) {
		this.noOfBids = noOfBids;
	}

	/**
	 * @return the startDate
	 */
	public Date getStartDate() {
		return this.startDate;
	}

	/**
	 * @param startDate
	 *            the startDate to set
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the endDate
	 */
	public Date getEndDate() {
		return this.endDate;
	}

	/**
	 * @param endDate
	 *            the endDate to set
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	/**
	 * @return the bidDate
	 */
	public Date getBidDate() {
		return this.bidDate;
	}

	/**
	 * @param bidDate
	 *            the bidDate to set
	 */
	public void setBidDate(Date bidDate) {
		this.bidDate = bidDate;
	}

	/**
	 * @return the sellerID
	 */
	public long getSellerID() {
		return this.sellerID;
	}

	/**
	 * @param sellerID
	 *            the sellerID to set
	 */
	public void setSellerID(long sellerID) {
		this.sellerID = sellerID;
		if (sellerID == this.client.getClientID()) {
			this.isOwner = true;
		}
	}

	/**
	 * @return the sellerRating
	 */
	public double getSellerRating() {
		return this.sellerRating;
	}

	/**
	 * @param sellerRating
	 *            the sellerRating to set
	 */
	public void setSellerRating(double sellerRating) {
		this.sellerRating = sellerRating;
	}

	/**
	 * Sets the seller rating for the HTML5 version
	 */
	public void setSellerRatingHTML5() {
		if (!RunSettings.isHTML4()) {
			this.sellerRating = this.client.getClientInfo().getHTML5SellerCache().get(this.sellerID).getRating();
		}
	}

	/**
	 * @return the categoryID
	 */
	public long getCategoryID() {
		return this.categoryID;
	}

	/**
	 * @param categoryID
	 *            the categoryID to set
	 */
	public void setCategoryID(long categoryID) {
		this.categoryID = categoryID;
	}

	/**
	 * @return the numPics
	 */
	public int getNumPics() {
		return this.numPics;
	}

	/**
	 * @param numPics
	 *            the numPics to set
	 */
	public void setNumPics(int numPics) {
		this.numPics = numPics;
	}

	/**
	 * @return the listRank
	 */
	public int getListRank() {
		return this.listRank;
	}

	/**
	 * @param listRank
	 *            the listRank to set
	 */
	public void setListRank(int listRank) {
		this.listRank = listRank;
		this.onList = true;
	}

	/**
	 * @return the commonSearchTerms
	 */
	public int getCommonSearchTerms() {
		return this.commonSearchTerms;
	}

	/**
	 * Increases the amount of common search terms by 1
	 */
	public void incCommonSearchTerms() {
		this.commonSearchTerms++;
	}

	/**
	 * Gives a bonus to an item for appearing on a search page
	 */
	public void isOnSearchPage() {
		this.commonSearchTerms += 0.5;
	}

	/**
	 * Increments the category depth of the item by one
	 */
	public void incCategoryDepth() {
		this.categoryDepth += 1;
	}

	/**
	 * Resets the item category depth to zero
	 */
	public void resetCategoryDepth() {
		this.categoryDepth = 0;
	}

	/**
	 * @param commonSearchTerms
	 *            the commonSearchTerms to set
	 */
	public void setCommonSearchTerms(int commonSearchTerms) {
		this.commonSearchTerms = commonSearchTerms;
	}

	/**
	 * @return the forAuction
	 */
	public boolean isForAuction() {
		return this.forAuction;
	}

	/**
	 * @param forAuction
	 *            the forAuction to set
	 */
	public void setForAuction(boolean forAuction) {
		this.forAuction = forAuction;
	}

	/**
	 * @return the forBuyNow
	 */
	public boolean isForBuyNow() {
		return this.forBuyNow;
	}

	/**
	 * @param forBuyNow
	 *            the forBuyNow to set
	 */
	public void setForBuyNow(boolean forBuyNow) {
		this.forBuyNow = forBuyNow;
	}

	/**
	 * @return the isOwner
	 */
	public boolean isOwner() {
		return this.isOwner;
	}

	/**
	 * @param isOwner
	 *            the isOwner to set
	 */
	public void setOwner(boolean isOwner) {
		this.isOwner = isOwner;
	}

	/**
	 * @return the itemRating
	 */
	public double getItemRating() {
		return this.itemRating;
	}

	/**
	 * @param itemRating
	 *            the itemRating to set
	 */
	public void setItemRating(double itemRating) {
		this.itemRating = itemRating;
	}

	/**
	 * Gets if the client is a bidder on the item
	 * 
	 * @return
	 */
	public boolean isBidder() {
		return this.bidder;
	}

	/**
	 * Sets if the client is a bidder on the item
	 * 
	 * @param val
	 */
	public void setBidder(boolean val) {
		this.bidder = val;
	}

	/**
	 * Gets the rating at which the client wants the item
	 * 
	 * @return
	 */
	public double getWantRating() {
		return this.wantRating;
	}

	/**
	 * Returns the number of words in a string
	 * 
	 * @param str
	 *            string to determine the number of words in
	 * @return
	 */
	private int getNumWords(String str) {
		int numWords = 0;
		int index = str.length();
		while (index > 0) {
			index = str.lastIndexOf(" ", index);
			index -= 1;
			numWords++;
		}
		return numWords;
	}

	/**
	 * Adds the image name to the list of item images
	 * 
	 * @param img
	 */
	public void addImage(String img) {
		this.images.add(img);
	}

	/**
	 * Gets a list of images corresponding to the item
	 * 
	 * @return
	 */
	public ArrayList<String> getImages() {
		return this.images;
	}

	public void resetNumQuestions() {
		this.numQuestions = 0;
	}

	/**
	 * Increments the number of questions asked about the item
	 */
	public void incQuestionNum() {
		this.numQuestions++;
	}

	/**
	 * Returns the number of questions asked about the item
	 * 
	 * @return
	 */
	public int getNumQuestions() {
		return this.numQuestions;
	}

	public void resetNumAnswers() {
		this.numAnswers = 0;
	}

	/**
	 * Increments the number of Answers asked about the item
	 */
	public void incAnswerNum() {
		this.numAnswers++;
	}

	/**
	 * Returns the number of Answers asked about the item
	 * 
	 * @return
	 */
	public int getNumAnswers() {
		return this.numAnswers;
	}

	/**
	 * Sets if the item is on the my account page
	 * 
	 * @param b
	 */
	public void setMyAccount(boolean b) {
		this.myAccount = b;
	}

	/**
	 * Returns if the item is on the my account page
	 * 
	 * @return
	 */
	public boolean isMyAccount() {
		return this.myAccount;
	}

	/**
	 * Sets a time stamp of when the item was last accessed
	 * 
	 * @param ts
	 */
	public void setTs(Long ts) {
		this.ts = ts;
	}

	/**
	 * Returns the time stamp of the item when it was last accessed
	 * 
	 * @return
	 */
	public long getTs() {
		return this.ts;
	}

	/**
	 * Calculates the item rating based on all the potential factors
	 */
	public void calcItemRating() {
		double itemRating = 0;
		if (this.name != null) {
			itemRating += ((getNumWords((this.name))) * this.client.getTitleWordsFactor()) / this.titleLength;
		}
		if (this.description != null) {
			itemRating += ((getNumWords((this.description))) * this.client.getDescriptionWordsFactor()) / this.descriptionLength;
		}
		itemRating += ((this.numPics) * this.client.getNumPicsFactor()) / this.numPicsAvg;
		if (this.sellerRating != 0) {
			double ratingChange = ((Math.log10(Math.abs(this.sellerRating))) * this.client.getSellerRatingFactor()) / this.sellerRatingLog;
			if (this.sellerRating > 0) {
				itemRating += ratingChange;
			} else {
				itemRating -= ratingChange;
			}
		} else {
			itemRating += this.client.getSellerRatingFactor() / this.sellerRatingLog;
		}
		// itemRating endDate calculation
		if (this.endDate != null) {
			double x = (new Date().getTime() - (this.endDate.getTime() - this.endDateDiff)) / ((this.endDateDiff));
			itemRating += ((Math.exp(2 * x) - 1) / (Math.exp(2) - 1)) * this.client.getEndDateDiffFactor();
		}

		if (this.forAuction && !this.forBuyNow) {
			itemRating += Math.exp(-this.currentBid / this.auctionAvg) * this.client.getStartPriceFactor();
		} else if (!this.forAuction && this.forBuyNow) {
			itemRating += Math.exp(-this.buyNowPrice / this.buyNowAvg) * this.client.getStartPriceFactor();
		} else if (this.forAuction && this.forBuyNow) {
			itemRating += Math.exp(-this.currentBid / this.auctionAvg) * this.client.getStartPriceFactor();
			this.priceDiff = this.buyNowPrice - this.startPrice;
			if (this.priceDiff > 0) {
				itemRating += ((this.priceDiff - (this.buyNowPrice - this.currentBid)) / (this.priceDiff)) * this.client.getPriceDiffFactor();
			}
		}
		if (this.onList) {
			itemRating += ((this.listRankMax - (this.listRank)) * this.client.getListRankingFactor()) / this.listRankMax;
		}
		itemRating += (this.categoryDepth) * this.client.getCategoryDepthFactor();
		itemRating += ((this.commonSearchTerms) * this.client.getCommonSearchTermsFactor()) / this.commonSearchTermsAvg;
		if (this.bidder) {
			itemRating += this.client.getAlreadyBidFactor();
		}

//		if (this.client.getCg().getHotItems().contains(this.id)) {
		if (this.client.getCg().isHot(this.id)) {
			itemRating += this.client.getHotItemFactor();
		}
		if (this.recommended) {
			itemRating += this.client.getRecommendedFactor();
		}

		itemRating += ((2 * this.numAnswers) - this.numQuestions) * this.client.getAnswersFactor();

		this.setItemRating(itemRating);
	}
}
