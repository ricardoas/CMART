package com.cmart.Data;

/**
 * This class contains the errors that pages could throw. I called them 'Error' but they are not the Java Error class
 * 
 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
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
public class GlobalErrors {
	private GlobalErrors() {}
	
	/*
	 * Each page will produce a certain number of error. Many of these errors will be common, we'll make them
	 * here and use the static references on each page
	 */
	// userID errors
	public final static Error userIDNotAnInteger= new Error(11, "The userID you have entered is not a vaild integer", "The userID is an integer. No other value can be passed for the userID.");
	public final static Error userIDLessThanZero = new Error(12, "The userID you have entered is less than zero", "The userID must be greater than zero");
	public final static Error userIDEmpty = new Error(13, "The userID you have entered is blank", "The userID must be an interger, it cannot be blank.");
	public final static Error userIDNotPresent = new Error(14, "The userID is not present", "This page requires that a userID be passed.");
	
	// authToken errors
	public final static Error authTokenNotPresent = new Error(21, "The authToken is not present", "This page requires that an authToken be passed.");
	public final static Error authTokenEmpty = new Error(22, "The authToken you have entered is blank", "The authToken must be a string, not blank");
	
	// Category errors
	public final static Error categoryIDNotAnInteger = new Error(31, "The categoryID is not an integer", "The categoryID must be an integer");
	public final static Error categoryIDLessThanZero = new Error(32, "The categoryID is less than zero", "The categoryID must be greater than zero");
	public final static Error categoryIDEmpty = new Error(33, "The categoryID you have entered is blank", "The categoryID must be an interger, it cannot be blank.");
	public final static Error categoryIDNotPresent = new Error(34, "The categoryID is not present", "This page requires a categoryID to be passed");
	
	// Page Number errors
	public final static Error pageNoNotAnInteger = new Error(41, "The page is not an integer", "The page must be an integer");
	public final static Error pageNoLessThanZero = new Error(42, "The page is less than zero", "The page must be greater than zero");
	public final static Error pageNoEmpty = new Error(43, "The page you have entered is blank", "The page must be an interger, it cannot be blank.");
	public final static Error pageNoNotPresent = new Error(44, "The page is not present", "This page requires a page to be passed");
	
	// Number of items per page errors
	public final static Error itemsPPNotAnInteger = new Error(51, "The itemsPP is not an integer", "The itemsPP must be an integer");
	public final static Error itemsPPLessThanZero = new Error(52, "The itemsPP is less than zero", "The itemsPP must be greater than zero");
	public final static Error itemsPPEmpty = new Error(53, "The itemsPP you have entered is blank", "The itemsPP must be an interger, it cannot be blank.");
	public final static Error itemsPPNotPresent = new Error(54, "The itemsPP is not present", "This page requires a itemsPP to be passed");
	
	// Total Users errors
	public final static Error totalUsersNotAnInteger = new Error(51, "The totalUsers is not an integer", "The totalUsers must be an integer");
	public final static Error totalUsersLessThanZero = new Error(52, "The totalUsers is less than zero", "The totalUsers must be greater than zero");
	public final static Error totalUsersEmpty = new Error(53, "The totalUsers you have entered is blank", "The totalUsers must be an interger, it cannot be blank.");
	public final static Error totalUsersNotPresent = new Error(54, "The totalUsers is not present", "This page requires a totalUsers to be passed");
	
	
	// Username errors
	public final static Error usernameEmpty = new Error(61, "The username you have entered is blank", "The username must be present, it cannot be blank.");
	public final static Error usernameNotPresent = new Error(62, "The username is not present", "This page requires a username to be passed");
	public final static Error usernameTaken = new Error(63, "The username is already taken", "The username you have chosen is already taken. Please choose a different username");
	
	// Email errors
	public final static Error emailEmpty = new Error(71, "The email address you have entered is blank", "The email address must be present, it cannot be blank.");
	public final static Error emailNotPresent = new Error(72, "The email address is not present", "This page requires an email address to be passed");
	public final static Error emailTaken = new Error(73, "The email address is already taken", "The email address you have chosen is already taken. Please choose a different email address");
	public final static Error emailDifferent = new Error(74, "The email addresses you entered are different", "Make sure both of the email addresses are the same");
	public final static Error emailNotVaild = new Error(74, "The email addresses you entered is not valid", "Make sure you enter a real email address like andrew@cmu.edu");
	
	// Password errors
	public final static Error passwordEmpty = new Error(81, "The password you have entered is blank", "The password must be present, it cannot be blank.");
	public final static Error passwordDifferent = new Error(92, "The passwords you entered are different", "Make sure both of the passwords are the same");
	
	// Firstname error
	public final static Error firstnameEmpty = new Error(91, "The firstname you have entered is blank", "The firstname must be present, it cannot be blank.");
	
	// lastname error
	public final static Error lastnameEmpty = new Error(101, "The lastname you have entered is blank", "The lastname must be present, it cannot be blank.");
	
	// street error
	public final static Error streetEmpty = new Error(111, "The street you have entered is blank", "The street must be present, it cannot be blank.");
	
	// town error
	public final static Error townEmpty = new Error(121, "The town you have entered is blank", "The town must be present, it cannot be blank.");
	
	// Zip errors
	public final static Error zipEmpty = new Error(131, "The zip address you have entered is blank", "The zip must be present, it cannot be blank.");
	public final static Error zipNotPresent = new Error(132, "The zip is not present", "This page requires an zip to be passed");
	public final static Error zipInvalid = new Error(133, "The zip you entered is not valid", "The zip must be a vilid US zip code");
	
	// State ID error
	public final static Error stateNotAnInteger = new Error(141, "The state is not an integer", "The state must be an integer");
	public final static Error stateLessThanZero = new Error(142, "The state is less than zero", "The state must be greater than zero");
	public final static Error stateEmpty = new Error(143, "The state you have entered is blank", "The state must be an interger, it cannot be blank.");
	public final static Error stateNotPresent = new Error(144, "The state is not present", "This page requires a state to be passed");
	public final static Error stateNotSelected = new Error(145, "The state is not selected", "Please select the state that you live in");
	
	// Login Errors
	public final static Error usernamePasswordIncorrect = new Error(151, "The username and password are incorrect", "The username and password do not match those in the database");
	public final static Error cannotAuthenticate = new Error(152, "Cannot create authToken", "There was an error that prevented us from creating the authToken in the database");
	public final static Error incorrectAuthToken = new Error(153, "Your authToken is incorrect", "The userID and authToken you passed do not match those in the database");
	
	// Bid (parameter) errors
	public final static Error bidNotADouble = new Error(161, "The bid is not an Double", "The bid must be a Double");
	public final static Error bidLessThanZero = new Error(162, "The bid is less than zero", "The bid must be greater than zero. Some one isn't going to pay you!");
	public final static Error bidEmpty = new Error(163, "The bid you have entered is blank", "The bid must be a Double, it cannot be blank.");
	public final static Error bidNotPresent = new Error(164, "The bid is not present", "This page requires a bid to be passed");
	
	// Max Bid errors
	public final static Error maxBidNotADouble = new Error(171, "The max bid is not an Double", "The max bid must be a Double");
	public final static Error maxBidLessThanZero = new Error(172, "The max bid is less than zero", "The max bid must be greater than zero. Some one isn't going to pay you!");
	public final static Error maxBidEmpty = new Error(173, "The max bid you have entered is blank", "The max bid must be a Double, it cannot be blank.");
	public final static Error maxBidNotPresent = new Error(174, "The max bid is not present", "This page requires a max bid to be passed");
	
	// Quantity Errors
	public final static Error quantityNotAnInteger = new Error(181, "The quantity is not an integer", "The quantity must be an integer");
	public final static Error quantityLessThanZero = new Error(182, "The quantity is less than zero", "The quantity must be greater than zero");
	public final static Error quantityEmpty = new Error(183, "The quantity you have entered is blank", "The quantity must be an interger, it cannot be blank.");
	public final static Error quantityNotPresent = new Error(184, "The quantity is not present", "This page requires a quantity to be passed");
	public final static Error quantityIsZero = new Error(185, "The quantity is zero", "The quantity must be greater than zero");
	
	// ItemID Errors
	public final static Error itemIDNotAnInteger = new Error(261, "The itemID is not an integer", "The itemID must be an integer");
	public final static Error itemIDLessThanZero = new Error(262, "The itemID is zero or less", "The itemID must be greater than zero");
	public final static Error itemIDEmpty = new Error(263, "The itemID you have entered is blank", "The itemID must be an interger, it cannot be blank.");
	public final static Error itemIDNotPresent = new Error(264, "The itemID is not present", "This page requires an itemID to be passed");
		
	// Bid ( errors
	public final static Error bidGreaterThanMaxBid = new Error(201, "The max bid is less than the bid", "The maximum you are willing to pay must be greater than or equal to the minimum you are will to pay");
	public final static Error bidLessThanCurrent = new Error(202, "The bid is less than the current bid", "You cannot bid less for an item than someone else already has");
	public final static Error bidQuantityTooHigh = new Error(203, "The quantity is too high", "You can only buy as many items are the vendor has for sale");
	public final static Error bidInvalidItem = new Error(204, "The itemID is not for a valid item", "You can only bid for items that are currently in the database");
	public final static Error bidOnFinishedAuction = new Error(205, "The auction has ended", "You can only bid for items where the auction is still in progress");
	
	// Sell errors
	public final static Error sellNoName = new Error(211, "The item you are selling must have a name", "You cannot sell an item without giving the item a name");
	public final static Error sellStartPriceGreaterThanReserve = new Error(212, "The start price must be less than the reserve price", "You cannot sell an item where the start price is greater than the reserve. Leave the reserve blank if you don't want a reserve price");
	public final static Error sellReservePriceGreaterThanBuyNow = new Error(213, "The reserve price must be less than the buy now price", "People cannot buy an item where the buy now price is less than the reserve price");
	public final static Error sellEndDateInPast = new Error(214, "The end time is in the past", "The end time for the item is in the past, it must be in the future to allow people to bid");
	public final static Error sellCategoryIDInvalid = new Error(215, "The category is invalid", "The categoryID chosen is not valid. Please choose one that is valid in the database and greater than zero");
	
	// Starting price
	public final static Error startPriceNotADouble = new Error(221, "The start price is not an Double", "The start price must be a Double");
	public final static Error startPriceLessThanZero = new Error(222, "The start price is less than zero", "The start price must be greater than zero.");
	public final static Error startPriceEmpty = new Error(223, "The start price you have entered is blank", "The start price must be a Double, it cannot be blank.");
	public final static Error startPriceNotPresent = new Error(224, "The start price is not present", "This page requires a start price to be passed");
	
	// Reserve price
	public final static Error reservePriceNotADouble = new Error(231, "The reserve price is not an Double", "The reserve price  must be a Double");
	public final static Error reservePriceLessThanZero = new Error(232, "The reserve price  is less than zero", "The reserve price  must be greater than or equal to zero.");
	public final static Error reservePriceEmpty = new Error(233, "The reserve price  you have entered is blank", "The reserve price must be a Double, it cannot be blank.");
	public final static Error reservePriceNotPresent = new Error(234, "The reserve price  is not present", "This page requires a reserve price  to be passed");
	
	// buy now price
	public final static Error buyNowPriceNotADouble = new Error(241, "The buy now price is not an Double", "The max bid must be a Double");
	public final static Error buyNowPriceLessThanZero = new Error(242, "The buy now price is less than zero", "The buy now price must be greater than or equal to zero.");
	public final static Error buyNowPriceEmpty = new Error(243, "The buy now price you have entered is blank", "The buy now price must be a Double, it cannot be blank.");
	public final static Error buyNowPriceNotPresent = new Error(244, "The buy now price is not present", "This page requires a buy now price to be passed");
	
	// end date errors
	public final static Error endDateInvalid = new Error(251, "The end date is not valid", "The end date that was passed could not be parsed. It is in the incorrect format.");
	public final static Error endDateEmpty = new Error(252, "The end date you have entered is blank", "The end date must be a date, it cannot be blank.");
	public final static Error endDateNotPresent = new Error(253, "The end date is not present", "This page requires a end date to be passed");

	// AddressID Errors
	public final static Error addressIDNotAnInteger = new Error(261, "The addressID is not an integer", "The addressID must be an integer");
	public final static Error addressIDLessThanZero = new Error(262, "The addressID is less than zero", "The addressID must be greater than zero");
	public final static Error addressIDEmpty = new Error(263, "The addressID you have entered is blank", "The addressID must be an interger, it cannot be blank.");
	public final static Error addressIDNotPresent = new Error(264, "The addressID is not present", "This page requires a addressID to be passed");
	
	// Credit card number errors
	public final static Error creditCardNoNotAnInteger = new Error(271, "The credit card number is not an integer", "Please enter the credit card number without the -'s");
	public final static Error creditCardNoLessThanZero = new Error(272, "The credit card number is less than zero", "Negative credit card numbers don't exist. Please enter without dashes etc");
	public final static Error creditCardNoEmpty = new Error(273, "The credit card number you have entered is blank", "The credit card number must be an interger, it cannot be blank.");
	public final static Error creditCardNoNotPresent = new Error(274, "The credit card number is not present", "This page requires a credit card number to be passed");
	public final static Error creditCardNoInvalid = new Error(275, "The credit card number or CVV is invalid", "The credit card number did not pass the Luhn and CVV algorithm test");
	public final static Error creditCardNoName = new Error(276, "The name on the card is blank", "You must enter the name as it appers on your credit card");
	
	// accountID Errors
	public final static Error accountIDNotAnInteger = new Error(281, "The accountID is not an integer", "The accountID must be an integer");
	public final static Error accountIDLessThanZero = new Error(282, "The accountID is less than zero", "The accountID must be greater than zero");
	public final static Error accountIDEmpty = new Error(283, "The accountID you have entered is blank", "The accountID must be an interger, it cannot be blank.");
	public final static Error accountIDNotPresent = new Error(284, "The accountID is not present", "This page requires a accountID to be passed");
	
	// Credit card CVV errors
	public final static Error creditCardCvvLessThanZero = new Error(291, "The cvv is less than zero", "The cvv must be greater than zero");
	public final static Error creditCardCvvInvalid = new Error(292, "The cvv must be 3 or 4 numbers", "The cvv must be 3 or 4 numbers long. 3 for master card, visa, etc. 4 for AmEx");
	
	// View user errors
	public final static Error viewUserIDNotAnInteger= new Error(301, "The viewUserID you have entered is not a vaild integer", "The viewUserID is an integer. No other value can be passed for the viewUserID.");
	public final static Error viewUserIDLessThanZero = new Error(302, "The viewUserID you have entered is less than zero", "The viewUserID must be greater than zero");
	public final static Error viewUserIDEmpty = new Error(303, "The viewUserID you have entered is blank", "The viewUserID must be an interger, it cannot be blank.");
	public final static Error viewUserIDNotPresent = new Error(304, "The viewUserID is not present", "This page requires that a viewUserID be passed.");
	
	// expiration date errors
	public final static Error expirationDateInvalid = new Error(311, "The expiration date is not valid", "The expiration date that was passed could not be parsed. It is in the incorrect format.");
	public final static Error expirationDateEmpty = new Error(312, "The expiration date you have entered is blank", "The expiration date must be a date, it cannot be blank.");
	public final static Error expirationDateNotPresent = new Error(313, "The expiration date is not present", "This page requires a expiration date to be passed");
	public final static Error expirationDateInPast = new Error(313, "The expiration date is in the past", "This page requires a currently vaild credit card");

	// Rating Errors
	public final static Error ratingNotAnInteger = new Error(321, "The rating is not an integer", "The rating must be an integer");
	public final static Error ratingNotInRange = new Error(322, "The rating is not in range [0,5]", "The rating must be greater than or equal to zero and less than or equal to five");
	public final static Error ratingEmpty = new Error(323, "The rating you have entered is blank", "The rating must be an interger, it cannot be blank.");
	public final static Error ratingNotPresent = new Error(324, "The rating is not present", "This page requires an rating to be passed");
		
	// comment errors
	public final static Error commentNotPresent = new Error(331, "The comment is not present", "This page requires that an comment be passed.");
	public final static Error commentEmpty = new Error(332, "The comment you have entered is blank", "The comment must be a string, not blank");
	
	//comment item errors
	public final static Error commentInvalidItem = new Error(333, "The itemID is not for a valid item", "You can only comment for items that are currently in the database");
	
	// questions errors
	public final static Error questionNotPresent = new Error(341, "The question is not present", "This page requires that an question be passed.");
	public final static Error questionEmpty = new Error(342, "The question you have entered is blank", "The question must be a string, not blank");
	
	// ask questions of item errors
	public final static Error questionInvalidItem = new Error(343, "The itemID is not for a valid item", "You can only question for items that are currently in the database");

	// questionID Errors
	public final static Error questionIDNotAnInteger = new Error(351, "The questionID is not an integer", "The questionID must be an integer");
	public final static Error questionIDLessThanZero = new Error(352, "The questionID is less than zero", "The questionID must be greater than zero");
	public final static Error questionIDEmpty = new Error(353, "The questionID you have entered is blank", "The questionID must be an interger, it cannot be blank.");
	public final static Error questionIDNotPresent = new Error(354, "The questionID is not present", "This page requires a questionID to be passed");
	
	// answers errors
	public final static Error answerNotPresent = new Error(361, "The answer is not present", "This page requires that an answer be passed.");
	public final static Error answerEmpty = new Error(362, "The answer you have entered is blank", "The answer must be a string, not blank");
	public final static Error answerInvalidItem = new Error(363, "The itemID is not for a valid item", "You can only answer for items that are currently in the database");
	
	public final static Error noVideoUploaded = new Error(371,"No Video File is Selected","In upload video part, no file is selected :(");
	
	public final static Error itemAlreadyGone = new Error(381,"The item you are looking at has gone","You got scooped");
	
	// Prefetch Images errors
	public final static Error prefetchImageValueNotPresent = new Error (391,"The prefetch Images value is not present", "You must enter valid positive integer for prefetchImageValue");
	
}
