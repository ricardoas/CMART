
function addRecommendation(){
	var rec = new StringBuffer();

	rec.append('<br />We recommend below items for you!<br />');
	rec.append('<br />');
	rec.append('<input type="hidden" name="pageNo" id="pageNo" value="0" />');
	rec.append('<input type="hidden" name="userID" id="userID" value="-1" />');
	rec.append('<input type="hidden" name="authToken" id="authToken" value="" />');
	rec.append('<input type="hidden" name="itemsPP" id="itemsPP" value="3" />');

	rec.append('<a id="recommendation_prev" href = "javascript:makeRequest(0)">Prev</a>');

	rec.append('<div class="home_recommendation">');
	rec.append('<ul>');

	rec.append('<li>');
	rec.append('<div class="pic"><img id="recommendedImage_0" src="" alt="Now loading" /></div>');
	rec.append('<div class="txt"><a id="recommendationItem_0" href=""></a></div>');
	rec.append('</li>');
	rec.append('<li>');
	rec.append('<div class="pic"><img id="recommendedImage_1" src="" alt="Now loading" /></div>');
	rec.append('<div class="txt"><a id="recommendationItem_1" href=""></a></div>');
	rec.append('</li>');
	rec.append('<li>');
	rec.append('<div class="pic"><img id="recommendedImage_2" src="" alt="Now loading" /></div>');
	rec.append('<div class="txt"><a id="recommendationItem_2" href=""></a></div>');
	rec.append('</li>');

	rec.append('</ul>');
	rec.append('</div>');

	rec.append('<a id="recommendation_next" href = "javascript:makeRequest(1)">Next</a>');

	$("#body_content").append(rec.toString());
};

/*
 * Runs when the user clicks the button on the login page
 * Will forward the user to the myaccount page if the logon was successful
 * otherwise it will display why they are not forwarded
 * Used by login.html
 *///ANDY
function checkLogin(){
	$.ajax({
		async: false,
		url:$.urldomain()+"/login?useHTML5=1&username="+$('#login_username').val()+"&password="+$('#password').val(),
		type: "GET",
		beforeSend: function(xhr){
			xhr.setRequestHeader('Access-Control-Allow-Origin', "*");
		},
		dataType: "json",
		success:function(data){
			if(!isEmpty(data.userID) && parseInt(data.userID)>0){
				localStorage.setItem("username",$('#login_username').val());
				localStorage.setItem("userID",data.userID);
				localStorage.setItem("authToken",data.authToken);
				location.href = "./myaccount.html";
			}else{
				printErrorsAndStats(data);
				localStorage.setItem("username","example@example.com");
				localStorage.setItem("userID",-1);
				localStorage.setItem("authToken",null);
			}
		},
		error:function(jqXHR,textStatus,errorThrown){
			if(debug) $('#scriptError').append("<br />checkLogin: failed " +textStatus +" " + errorThrown + " " + new Date().getTime());
		}
	});
};

/*
 * Checks if a variable is undefined, null, empty
 * Used by many functions
 */
function isEmpty(value) {
	if(typeof value === undefined
			|| typeof value === 'undefined'
				|| value === null
				|| value === 'NULL'
					|| value === 'null'
						|| value === ''
							|| value === 0
							|| value === '0'
								|| value === false
								|| !(value)
	) return true;
	else return false;
};

/*
 * This prints the page errors and stats at the bottom of the page
 * This is called by most pages
 *///ANDY
function printErrorsAndStats(data) {
	if(!isEmpty(data)) {
		if(!isEmpty(data.errors)) {
			$("#numOfErrors").html("Errors: " + data.errors.length);
			var $errorBody = $("#errorBody");
			$errorBody.html("");

			$.each(data.errors, function(i, er) {
				$errorBody.append('<tr>	<div class="row"' + i % 2 + 1 + 'id="entry"> <td> <div class="nocol">' + er.errorNumber + '</label> </div></td>' + '<td><div class="msgcol">' + er.errorMessage + '</div></td></div> </tr>');
			});
		}
		if(!isEmpty(data.stats)) {
			$("#statsServerName").html(data.stats.server);
			$("#statsPramTime").html(data.stats.parameterTime);
			$("#statsDBTime").html(data.stats.databaseTime);
			$("#statsProcTime").html(data.stats.processingTime);
			$("#statsRenderTime").html(data.stats.renderTime);
			$("#statsTotalTime").html(data.stats.totalTime);
		}
	}
}

/*
 * Print the header with the logout and myaccount buttons
 * Used by all pages
 *///ANDY
function setLogOutHeader(){
	var strVar=new StringBuffer();
	strVar.append("<br /><div class=\"container\"><div class=\"row\"><div class=\"twelve columns\">");
	strVar.append("<div class=\"two columns\">");
	strVar.append("<div id=\"header\" class=\"header\"><img src=\"images/header/application.png\" alt=\"C-MART App\" width=\"30\"/>");
	strVar.append("<img src=\"cmart.jpg\" alt=\"C-MART\" width=\"90\">");
	strVar.append("</div>");
	strVar.append("</div>");
	strVar.append("<div class=\"seven columns\">");
	strVar.append("<dl class=\"nice contained tabs\">");
	strVar.append("<dd>");
	strVar.append("<a href=\"index.html\">");
	strVar.append("<img src=\"images/header/home.png\" alt=\"home\" width=\"25\"/>Home");
	strVar.append("</a>");
	strVar.append("</dd>");
	strVar.append("<dd >");
	strVar.append("<a href=\"browse.html\">");
	strVar.append("<img src=\"images/header/browse.png\" alt=\"browse\" width=\"25\"/>Browse");
	strVar.append("</a>");
	strVar.append("</dd>");
	strVar.append("<dd>");
	strVar.append("<a href=\"sell.html\">");
	strVar.append("<img src=\"images/header/sell.png\" alt=\"sell\" width=\"25\"/>Sell");
	strVar.append("</a>");
	strVar.append("</dd>");
	strVar.append("<dd>");
	strVar.append("<a href=\"myaccount.html\">");
	strVar.append("<img src=\"images/header/browse.png\" alt=\"My Account\" width=\"25\"/>My Account");
	strVar.append("</a>");
	strVar.append("</dd>");
	strVar.append("<dd>");
	strVar.append("<a href=\"logout.html\">");
	strVar.append("<img src=\"images/header/logout.png\" alt=\"logout\" width=\"25\"/>Logout");
	strVar.append("</a>");
	strVar.append("</dd>");
	strVar.append("</dl>");
	strVar.append("</div>");

	$("#header").html(strVar.toString());
};

/*
 * Set the header with the login and register buttons
 * Used by all pages
 *///ANDY
function setLogInHeader(){
	var strVar=new StringBuffer();
	strVar.append("<br /><div class=\"container\"><div class=\"row\"><div class=\"twelve columns\">");
	strVar.append("<div class=\"two columns\">");
	strVar.append("<div id=\"header\" class=\"header\"><img src=\"images/header/application.png\" alt=\"C-MART App\" width=\"30\"/>");
	strVar.append("<img src=\"cmart.jpg\" alt=\"C-MART\" width=\"90\">");
	strVar.append("</div>");
	strVar.append("</div>");
	strVar.append("<div class=\"seven columns\">");
	strVar.append("<dl class=\"nice contained tabs\">");
	strVar.append("<dd>");
	strVar.append("<a href=\"index.html\">");
	strVar.append("<img src=\"images/header/home.png\" alt=\"home\" width=\"25\"/>Home");
	strVar.append("</a>");
	strVar.append("</dd>");
	strVar.append("<dd >");
	strVar.append("<a href=\"browse.html\">");
	strVar.append("<img src=\"images/header/browse.png\" alt=\"browse\" width=\"25\"/>Browse");
	strVar.append("</a>");
	strVar.append("</dd>");
	strVar.append("<dd>");
	strVar.append("<a href=\"sell.html\">");
	strVar.append("<img src=\"images/header/sell.png\" alt=\"sell\" width=\"25\"/>Sell");
	strVar.append("</a>");
	strVar.append("</dd>");
	strVar.append("<dd>");
	strVar.append("<a href=\"login.html\">");
	strVar.append("<img src=\"images/header/login.png\" alt=\"login\" width=\"25\"/>Login");
	strVar.append("</a>");
	strVar.append("</dd>");
	strVar.append("<dd>");
	strVar.append("<a href=\"register.html\">");
	strVar.append("<img src=\"images/header/register.png\" alt=\"browse\" width=\"25\"/>Register");
	strVar.append("</a>");
	strVar.append("</dd>");
	strVar.append("</dl>");
	strVar.append("</div>");

	$("#header").html(strVar.toString());
};

/*
 * Logout the user if the user has the correct userID and authToken
 * If the userID and authToken are wrong then just clear the local values
 *///ANDY
function userLogout(){
	// Set what userID we will send
	var userID = localStorage['userID'];
	if(!isEmpty(userID) && userID!="-1") userID = "&userID=" + userID;
	else userID="";

	// set the auth token we will send
	var authToken = localStorage['authToken'];
	if(!isEmpty(authToken)) authToken = "&authToken=" + authToken;
	else authToken="";

	// Send the logout request to the server
	$.ajax({
		async: false,
		url: $.urldomain()+"/logout?useHTML5=1"+userID+authToken,
		type: "GET",
		dataType: "json",
		success:function(data){
			// If the logout was successful set succcess, otherwise display the error messages
			if(!isEmpty(data) && (data.success===true||data.success==='true')){
				$("#logout_msg").html("You have been logged out!");	

				if(!isEmpty(data))
					printErrorsAndStats(data);	
			}
			else{
				$("#logout_msg").html("You were not logged out");

				if(!isEmpty(data))
					printErrorsAndStats(data);			
			}
		},
		error:function(jqXHR,textStatus,errorThrown){
			$('#scriptError').append("<br />userLogout could not contact server " + err.message + " " + new Date().getTime());
		}
	});

	// Clear the user's local variables
	localStorage.removeItem("username");
	localStorage.removeItem("userID");
	localStorage.removeItem("authToken");

	//localStorage.setItem("username","example@example.com");
	//localStorage.setItem("userID",-1);
	//localStorage.setItem("authToken",null);
	setLogInHeader();
	database.clearDatabase();
};

/*
 * Registers a new user
 * used by register.html
 */
function registerUser(){
	$.ajax({
		async: false,
		url: $.urldomain()+"/registeruser?useHTML5=1&username="+$('#username').val()+"&password1="+$('#password1').val()+"&password2="+$('#password2').val()+"&email1="+$('#email1').val()+"&email2="+$('#email2').val()+"&firstname="+$('#firstname').val()+"&lastname="+$('#lastname').val()+"&street="+$('#street').val()+"&town="+$('#town').val()+"&zip="+$('#zip').val()+"&state="+$('#state').val(),
		type: "GET",
		dataType: "json",
		success:function(data){

			if(!isEmpty(data) && parseInt(data.userID)>0){
				//$('#register_modal').dialog( "close" );
				localStorage.setItem("username",$('#login_username').val());
				localStorage.setItem("userID",data.userID);
				localStorage.setItem("authToken",data.authToken);
				//setLogOutHeader();
				location.href = "./myaccount.html";
			}else{
				/*$('#register_modal').dialog( "close" );
				var errormsgs = "<div class=\"errors\"><div class=\"title\">Errors: "+data.errors.length+"</div>";
				for(var i=0;i<data.errors.length;i=i+1){
					var j=(i+1)%2+1;
					errormsgs=errormsgs+"<div class=\"row"+j+"\" id=\"entry\"><div class=\"nocol\">"+data.errors[i].errorNumber+"</div><div class=\"msgcol\">"+data.errors[i].errorMessage+"</div></div>";
				}
				errormsgs=errormsgs+"</div>";
				$("#errors").html(errormsgs);*/

				if(!isEmpty(data))
					printErrorsAndStats(data);
			}
		},
		error:function(jqXHR,textStatus,errorThrown){
			$('#registerUser').append("<br />registerUser could not send request " + err.message + " " + new Date().getTime());

			alert(textStatus+" , "+errorThrown);
		}
	});

	return false;
};

/*
 * Used to check that the password fields match
 * used by register.html
 */
function checkPasswords(){
	var p1 = $("#password1").val();
	var p2 = $("#password2").val();

	if(p1 !== p2)
		$("#passwdmsg").html("The passwords do not match");
	else
		$("#passwdmsg").html("");
};

/*
 * Used to check that the E-mail fields match
 * used by register.html
 */
function checkEmails(){
	var p1 = $("#email1").val();
	var p2 = $("#email2").val();

	if(p1 !== p2)
		$("#emailmsg").html("The e-mails do not match");
	else
		$("#emailmsg").html("");
};

/*
 * Used to make sure a state is selected
 * used by register.html
 */
function checkStateSelected(){
	if($("#state").val() == 0)
		$("#statemsg").html("You must choose a state");
	else
		$("#statemsg").html("");
}

/*
 * Loads an item to the viewitem page
 * used by viewitem.html
 */
function viewItem(itemID){
	if(isEmpty(itemID) || itemID < 0){
		$("#body_content").html("Error: itemID not present");
	}
	else database.getitem(itemID);
};

function itemJSON(item){
	return "'"+item.sellerID+"','"+item.name.replace(/'/g,"#")+"','"+item.description.replace(/'/g,"#")+"','"+item.quantity+"','"+item.startPrice+"','"+item.reservePrice+"','"+item.buyNowPrice+"','"+item.currentBid+"','"+item.maxBid+"','"+item.noOfBids+"','"+item.startDate+"','"+item.endDate+"','"+item.sellerID+"','"+item.categoryID+"'";
};

/*
 * Sends a user's bid for an item to the server
 * used by viewitem.html
 */
function bidItem(){
	// Set what userID we will send
	var userID = localStorage['userID'];
	if(!isEmpty(userID) && userID!="-1") userID = "&userID=" + userID;
	else userID="";

	// set the auth token we will send
	var authToken = localStorage['authToken'];
	if(!isEmpty(authToken)) authToken = "&authToken=" + authToken;
	else authToken="";

	var response=$.ajax({
		async: false,
		url: $.urldomain()+"/viewitem?useHTML5=1"+userID+authToken+"&itemID="+$.urlParam("itemID")+"&bid="+$('#bid').val()+"&maxBid="+$('#maxBid').val()+"&quantity="+$('#quantity').val(),
		type: "GET",
		dataType: "json",

		success:function(data) {
			// Update the item and seller in the database
			if(!isEmpty(data)) {
				if(!isEmpty(data.item)) {
					// Update or insert the item
					database.insertItem(data.item.id, data.item.name, data.item.description, data.item.quantity, data.item.startPrice, data.item.reserve, data.item.buyNowPrice, data.item.currentBid, data.item.noOfBids, data.item.startDate, data.item.endDate, data.item.sellerID, data.item.sellerName, data.item.categoryID, data.item.images, data.item.thumbnail)
				}

				if(!isEmpty(data.seller)) {
					database.insertSeller(data.seller);
				}

				if(!isEmpty(data.success) && data.success) {
					// Now tell the use if they were successful
					window.location.href = "./confirmbid.html?itemID=" + data.item.id;
				} else {
					// fail, update screen and print errors
					database.getitem(data.item.id);
					if(!isEmpty(data))
						printErrorsAndStats(data);
				}
				/*if(data.success==false||data.success==-1){
				 database.updateitem(data.item.id,data.item.quantity,data.item.startPrice,data.item.reserve,data.item.buyNowPrice,data.item.currentBid,data.item.noOfBids);
				 }else{
				 updateitems(data.item.id);
				 }*/
			}
		},

		error:function(jqXHR,textStatus,errorThrown){
			alert(textStatus+" , "+errorThrown);
		}
	});
};

//God i hope this is unused
function updateitems(id){
	var response=$.ajax({
		async: false,
		url: $.urldomain()+"/viewitem?useHTML5=1&userID="+localStorage['userID']+"&authToken="+localStorage['authToken']+"&itemID="+id,
		type: "GET",
		dataType: "json",
		success:function(data){
			database.updateitem(data.item.id,data.item.quantity,data.item.startPrice,data.item.reserve,data.item.buyNowPrice,data.item.currentBid,data.item.noOfBids);
		},
		error:function(jqXHR,textStatus,errorThrown){
			alert(jqXHR.status+" , "+jqXHR.getallResponseHeaders());
		}
	});
	return false;
};

//This should not be used!
function buyItem(){
	database.findaddress(localStorage.getItem("userID"));	
};

/*
 * Forward the user to the buy item page
 * used by viewitem.html
 */
function forwardBuyItem(){
	window.location.href="./buyitem.html";
};

/*
 * This send a users buy request to the server to see if they have bought the item
 * used by buyitem.html
 */
function confirmBuyItem(){
	// Set what userID we will send
	var userID = localStorage['userID'];
	if(!isEmpty(userID) && userID!="-1") userID = "&userID=" + userID;
	else userID="";

	// set the auth token we will send
	var authToken = localStorage['authToken'];
	if(!isEmpty(authToken)) authToken = "&authToken=" + authToken;
	else authToken="";

	var response=$.ajax({
		async: false,
		url: $.urldomain()+"/buyitem?useHTML5=1"+userID+authToken+"&itemID="+$.urlParam("itemID")+"&quantity="+$("#quantity").val()+"&street="+$("#street").val()+"&town="+$("#town").val()+"&zip="+$("#zip").val()+"&state="+$("#state").val()+"&nameOnCard="+$("#nameOnCard").val()+"&creditCardNo="+$("#creditCardNo").val()+"&cvv="+$("#cvv").val()+"&expirationDate="+$("#expirationDate").val(),
		type: "GET",
		dataType: "json",
		success:function(data){
			// If success is returned then forward to confirm buy, otherwise display errors
			if(!isEmpty(data) && data.success){
				window.location.href="./confirmbuy.html?itemID="+data.itemID;
			}else{
				printErrorsAndStats(data);
			}
		},
		error:function(jqXHR,textStatus,errorThrown){
		}
	});

};

/*
 * Used to download a user's address if it is not present in the local DB
 * used by database.findaddress<-buyItem()<-viewitem.html
 *///ANDY
function downloadAddress(){
	// Set what userID we will send
	var userID = localStorage['userID'];
	if(!isEmpty(userID) && userID!="-1") userID = "&userID=" + userID;
	else userID="";

	// set the auth token we will send
	var authToken = localStorage['authToken'];
	if(!isEmpty(authToken)) authToken = "&authToken=" + authToken;
	else authToken="";

	var response=$.ajax({
		async: false,
		url:$.urldomain()+"/buyitem?useHTML5=1&getAddress=1"+userID+authToken,
		type: "GET",
		dataType: "json",
		success:function(data){
			// If an address is returned, insert it locally and forward the user to the buy page
			if(!isEmpty(data) && !isEmpty(data.address)){
				database.insertaddress(data.address.addressid,data.address.userid,data.address.street,data.address.town,data.address.zip,data.address.state,data.address.isdefault, populateBuyConfirmation);
				//populateBuyConfirmation(data.address.addressid,data.address.street,data.address.town,data.address.zip,data.address.state);
			}
			// Otherwise just forward without the address
			else{
				window.location.href="./buyitem.html?itemID="+$.urlParam("itemID");
			}
			//database.insertaddress(data.address.addressid,data.address.userid,data.address.street,data.address.town,data.address.zip,data.address.state,data.address.isdefault);
			//populateBuyConfirmation(data.address.addressid,data.address.street,data.address.town,data.address.zip,data.address.state);
		},
		error:function(jqXHR,textStatus,errorThrown){
		}
	});
	return false;
};

/*
 * Forwards the user to the buyitem page with the address filled in
 * called back by insertaddess<-downloadaddress
 */
function populateBuyConfirmation(addressid,street,town,zip,state){
	window.location.href="./buyitem.html?itemID="+$.urlParam("itemID")+"&addressid="+addressid+"&street="+encodeURIComponent(street)+"&town="+encodeURIComponent(town)+"&zip="+zip+"&state="+state;
};


/*
 * Makes the user account page. Asks for all new data after the last updated time stamp. Inserts the returned data.
 * Then renders the page.
 * Called my onload->myaccount
 */
function userAccount(){
	// Get the last update time variable
	var ts = "0";
	if(!isEmpty(localStorage['myAccountTS']))
		ts = localStorage['myAccountTS'];
	var tsnow = new Date().getTime();
	localStorage.setItem("myAccountTS", tsnow);



	// Send request for new info
	var response=$.ajax({
		async: false,
		url:$.urldomain()+"/myaccount?useHTML5=1&userID="+localStorage['userID']+"&authToken="+localStorage['authToken']+"&ts="+ts,
		type: "GET",
		dataType: "json",
		success:function(data){
			// Add the update user details link
			//var browse="<a class='nav' href=\"updateuserdetails.html\">Update details</a><br>";
			//$("#body_content").append(browse);

			// Insert the items that the bids need
			if(!isEmpty(data.newbids))
				for(var i=0;i<data.newbids.length;i=i+1)
					if(data.newbids[i].bidItem.id!="-1")
						database.insertItem(data.newbids[i].bidItem.id,data.newbids[i].bidItem.name,data.newbids[i].bidItem.description,data.newbids[i].bidItem.quantity,data.newbids[i].bidItem.startPrice,data.newbids[i].bidItem.reserve,data.newbids[i].bidItem.buyNowPrice,data.newbids[i].bidItem.currentBid,data.newbids[i].bidItem.noOfBids,data.newbids[i].bidItem.startDate,data.newbids[i].bidItem.endDate,data.newbids[i].bidItem.sellerID,data.newbids[i].bidItem.sellerName,data.newbids[i].bidItem.categoryID,data.newbids[i].bidItem.images,data.newbids[i].bidItem.thumbnail);

			if(!isEmpty(data.oldbids))
				for(var i=0;i<data.oldbids.length;i=i+1)
					if(data.oldbids[i].bidItem.id!="-1")
						database.insertItem(data.oldbids[i].bidItem.id,data.oldbids[i].bidItem.name,data.oldbids[i].bidItem.description,data.oldbids[i].bidItem.quantity,data.oldbids[i].bidItem.startPrice,data.oldbids[i].bidItem.reserve,data.oldbids[i].bidItem.buyNowPrice,data.oldbids[i].bidItem.currentBid,data.oldbids[i].bidItem.noOfBids,data.oldbids[i].bidItem.startDate,data.oldbids[i].bidItem.endDate,data.oldbids[i].bidItem.sellerID,data.oldbids[i].bidItem.sellerName,data.oldbids[i].bidItem.categoryID,data.oldbids[i].bidItem.images,data.oldbids[i].bidItem.thumbnail);

			if(!isEmpty(data.purchases))
				for(var i=0;i<data.purchases.length;i=i+1)
					if(data.purchases[i].purchaseItem.id!="-1")
						database.insertItem(data.purchases[i].purchaseItem.id,data.purchases[i].purchaseItem.name,data.purchases[i].purchaseItem.description,data.purchases[i].purchaseItem.quantity,data.purchases[i].purchaseItem.startPrice,data.purchases[i].purchaseItem.reserve,data.purchases[i].purchaseItem.buyNowPrice,data.purchases[i].purchaseItem.currentBid,data.purchases[i].purchaseItem.noOfBids,data.purchases[i].purchaseItem.startDate,data.purchases[i].purchaseItem.endDate,data.purchases[i].purchaseItem.sellerID,data.purchases[i].purchaseItem.sellerName,data.purchases[i].purchaseItem.categoryID,data.purchases[i].purchaseItem.images,data.purchases[i].purchaseItem.thumbnail);

			// Insert bids
			if(!isEmpty(data.newbids)) database.insertBids(data.newbids, '0');
			if(!isEmpty(data.oldbids)) database.insertBids(data.oldbids, '1');

			// Insert new items			
			if(!isEmpty(data.newitems))
				for(var i=0;i<data.newitems.length;i=i+1)
					if(data.newitems[i].id!="-1")
						database.insertItem(data.newitems[i].id,data.newitems[i].name,data.newitems[i].description,data.newitems[i].quantity,data.newitems[i].startPrice,data.newitems[i].reserve,data.newitems[i].buyNowPrice,data.newitems[i].currentBid,data.newitems[i].noOfBids,data.newitems[i].startDate,data.newitems[i].endDate,data.newitems[i].sellerID,data.newitems[i].sellerName,data.newitems[i].categoryID,data.newitems[i].images,data.newitems[i].thumbnail);	

			// Insert old items
			if(!isEmpty(data.olditems))
				for(var i=0;i<data.olditems.length;i=i+1)
					if(data.olditems[i].id!="-1")
						database.insertItem(data.olditems[i].id,data.olditems[i].name,data.olditems[i].description,data.olditems[i].quantity,data.olditems[i].startPrice,data.olditems[i].reserve,data.olditems[i].buyNowPrice,data.olditems[i].currentBid,data.olditems[i].noOfBids,data.olditems[i].startDate,data.olditems[i].endDate,data.olditems[i].sellerID,data.olditems[i].sellerName,data.olditems[i].categoryID,data.olditems[i].images,data.olditems[i].thumbnail);

			// Insert purchases
			if(!isEmpty(data.purchases))
				for(var i=0;i<data.purchases.length;i=i+1)
					if(data.purchases[i].id!="-1"){
						database.insertPurchase(data.purchases[i],data.purchases[i].purchaseItem.name,data.purchases[i].purchaseItem.id);
						database.bidToPurchase(data.purchases[i].purchaseItem.id);
					}


			// Insert sellers
			if(!isEmpty(data.sellers))
				database.insertSellers(data.sellers);

			// Make SBs to hold the bids and items HTML
			var bids = new StringBuffer();
			var oldBids = new StringBuffer();
			var items=new StringBuffer();
			var oldItems=new StringBuffer();
			var purchases=new StringBuffer();

			// Get the bids and items to display
			database.db.readTransaction(function(tx) {
				tx.executeSql("SELECT * FROM bids INNER JOIN items ON bids.id=items.id", [], function(tx, rs) {

					if(rs.rows.length > 0) {
						// Append each bid
						for(var i = 0; i < rs.rows.length; i = i + 1) {
							// If the item is old
							if(rs.rows.item(i)["isOld"] === 1) {
								oldBids.append("<tr><div class=\"row1\" id=\"entry\"><label for=\"currentBids\">");
								oldBids.append("<td><div class=\"img\"><img height=\"80\" width=\"80\" src=\"").append(rs.rows.item(i).thumbnail).append("\" alt=\"").append(rs.rows.item(i).id).append("\" /></div></td>");
								oldBids.append("<td><div class=\"description\"><a href=\"viewitem.html?itemID=").append(rs.rows.item(i).id).append("\">").append(rs.rows.item(i).name).append("</a></div</td>");
								oldBids.append("<td><div class=\"bid\">").append(rs.rows.item(i).bid).append("</div></td>");
								oldBids.append("<td><div class=\"maxbid\">").append(rs.rows.item(i).maxBid).append("</div></td>");
								oldBids.append("<td><div class=\"enddate\">").append(rs.rows.item(i).endDate).append("</div></td>");
								oldBids.append("</label></div></tr>");
							} else {
								bids.append("<tr><div class=\"row1\" id=\"entry\"><label for=\"currentBids\">");
								bids.append("<td><div class=\"img\"><img height=\"80\" width=\"80\" src=\"").append(rs.rows.item(i).thumbnail).append("\" alt=\"").append(rs.rows.item(i).id).append("\" /></div></td>");
								bids.append("<td><div class=\"description\"><a href=\"viewitem.html?itemID=").append(rs.rows.item(i).id).append("\">").append(rs.rows.item(i).name).append("</a></div></td>");
								bids.append("<td><div class=\"bid\">").append(rs.rows.item(i).bid).append("</div></td>");
								bids.append("<td><div class=\"maxbid\">").append(rs.rows.item(i).maxBid).append("</div></td>");
								bids.append("<td><div class=\"enddate\">").append(rs.rows.item(i).endDate).append("</div></td>");
								bids.append("</label></div></tr>");
							}
						}

					}

					if(bids.toString() == "") bids.append("<tr><td colspan=\"5\">No Current Bids</td></tr>");
					if(oldBids.toString() == "") oldBids.append("<tr><td colspan=\"5\">No Previous Bids</td></tr>");

					// Set the items on the page
					$("#bidsbody").html(bids.toString());
					$("#oldbidsbody").html(oldBids.toString());

				}, function(tx, err) {
					if(debug) $('#scriptError').append("<br />userAccount " + err.message + " " + new Date().getTime());
				});
			});

			// Get the items to display
			database.db.readTransaction(function(tx) {
				tx.executeSql("SELECT * FROM items WHERE myaccount=1", [], function(tx, rs) {

					if(rs.rows.length > 0) {

						//items.append("<table><thead><div class=\"itemtable\"><div class=\"title\"><div class=\"desc\"><tr><th colspan=\"3\">Items Currently Selling</th></tr></div><tr><th>Item Name</th><th>Max Bid</th><th>End Date</th></tr></thead></div>");
						//items.append("<tbody>");

						//oldItems.append("<table><thead><div class=\"itemtable\"><div class=\"title\"><div class=\"desc\"><tr><th colspan=\"3\">Items Previously Sold</th></tr></div><tr><th>Item Name</th><th>Max Bid</th><th>End Date</th></tr></thead></div>");
						//oldItems.append("<tbody>");

						// Append each bid
						for(var i = 0; i < rs.rows.length; i = i + 1) {
							// If the item is old
							if(Date.parse(rs.rows.item(i)["endDate"]) < tsnow) {
								items.append("<tr><div class=\"row1\" id=\"entry\"><label for=\"currentlySelling\">");
								items.append("<td><div class=\"description\"><a href=\"viewitem.html?itemID=").append(rs.rows.item(i)["id"]).append("\">").append(rs.rows.item(i)["name"]).append("</a></div></td>");
								items.append("<td><div class=\"maxbid\">").append(rs.rows.item(i)["currentBid"]).append("</div></td>");
								items.append("<td><div class=\"enddate\">").append(rs.rows.item(i)["endDate"]).append("</div></td>");
								items.append("</label></div></tr>");
							} else {
								oldItems.append("<tr><div class=\"row1\" id=\"entry\"><label for=\"oldSelling\">");
								oldItems.append("<td><div class=\"description\"><a href=\"viewitem.html?itemID=").append(rs.rows.item(i)["id"]).append("\">").append(rs.rows.item(i)["name"]).append("</a></div></td>");
								oldItems.append("<td><div class=\"maxbid\">").append(rs.rows.item(i)["currentBid"]).append("</div></td>");
								oldItems.append("<td><div class=\"enddate\">").append(rs.rows.item(i)["endDate"]).append("</div></td>");
								oldItems.append("</label></div></tr>");
							}
						}

					}

					if(items.toString() == "") items.append("<tr><td colspan=\"3\">Not Currently Selling</td></tr>");
					if(oldItems.toString() == "") oldItems.append("<tr><td colspan=\"3\">No Previously Sold Items</td></tr>");


					//items.append("</tbody></div></table>");
					//oldItems.append("</tbody></div></table>");

					$("#itemsbody").html(items.toString());
					$("#olditemsbody").html(oldItems.toString());
					//$("#body").append(items.toString());
					//$("#body").append(oldItems.toString());

				}, function(tx, err) {
					$('#scriptError').append("<br />userAccounts " + err.message + " " + new Date().getTime());
				});
			});

			// Get the purcheses to display
			database.db.readTransaction(function(tx) {
				tx.executeSql("SELECT * FROM purchases", [], function(tx, rs) {

					if(rs.rows.length > 0) {

						//items.append("<table><thead><div class=\"itemtable\"><div class=\"title\"><div class=\"desc\"><tr><th colspan=\"3\">Items Currently Selling</th></tr></div><tr><th>Item Name</th><th>Max Bid</th><th>End Date</th></tr></thead></div>");
						//items.append("<tbody>");

						//oldItems.append("<table><thead><div class=\"itemtable\"><div class=\"title\"><div class=\"desc\"><tr><th colspan=\"3\">Items Previously Sold</th></tr></div><tr><th>Item Name</th><th>Max Bid</th><th>End Date</th></tr></thead></div>");
						//oldItems.append("<tbody>");

						// Append each bid
						for(var i = 0; i < rs.rows.length; i = i + 1) {
							// If the item is old
							purchases.append("<tr><div class=\"row1\" id=\"entry\"><label for=\"purchases\">");
							purchases.append("<td></td>");
							purchases.append("<td><div class=\"description\"><a href=\"viewitem.html?itemID=").append(rs.rows.item(i)["itemID"]).append("\">").append(rs.rows.item(i)["name"]).append("</a></div></td>");
							purchases.append("<td><div class=\"quantity\">").append(rs.rows.item(i)["quantity"]).append("</div></td>");
							purchases.append("<td><div class=\"price\">").append(rs.rows.item(i)["price"]).append("</div></td>");
							if(rs.rows.item(i)["paid"]=="true")
								purchases.append("<td><div class=\"price\">").append("Paid").append("</div></td>");
							else
								purchases.append("<td><div class=\"price\">").append("Not Paid").append("</div></td>");

							purchases.append("</label></div></tr>");
						}

					}

					if(purchases.toString() == "") purchases.append("<tr><td colspan=\"5\">No Purchases</td></tr>");


					//items.append("</tbody></div></table>");
					//oldItems.append("</tbody></div></table>");

					$("#purchasesbody").html(purchases.toString());
					//$("#body").append(items.toString());
					//$("#body").append(oldItems.toString());

				}, function(tx, err) {
					$('#scriptError').append("<br />userAccounts " + err.message + " " + new Date().getTime());
				});
			});


			/*items.append("<table><thead><div class=\"itemtable\"><div class=\"title\"><div class=\"desc\"><tr><th colspan=\"3\">Items Currently Selling</th></tr></div><tr><th>Item Name</th><th>Max Bid</th><th>End Date</th></tr></thead></div>");
			items.append("<tbody>");
			for(var i=0;i<data.newitems.length;i=i+1){
				if(data.newitems[i].id!="-1"){
					items.append("<tr><div class=\"row1\" id=\"entry\"><label for=\"currentlySelling\">");
					items.append("<td><div class=\"description\"><a href=\"viewitem.html?itemID=").append(data.newitems[i].id).append("\">").append(data.newitems[i].name).append("</a></div></td>");
					items.append("<td><div class=\"maxbid\"><label for=\"maxBid39646\">").append(data.newitems[i].currentBid).append("<label></div></td>");
					items.append("<td><div class=\"enddate\"><label for=\"endDate39646\">").append(data.newitems[i].endDate).append("</label></div></td>");
					items.append("</label></div></tr>");
				}
			}
			if(data.newitems.length==0){
				items.append("<tr><td colspan=\"3\">Not Currently Selling</td></tr>");
			}
			items.append("</tbody></div></table>");

			items.append("<table><thead><div class=\"itemtable\"><div class=\"title\"><div class=\"desc\"><tr><th colspan=\"3\">Items Previously Sold</th></tr></div><tr><th>Item Name</th><th>Max Bid</th><th>End Date</th></tr></thead></div>");
			items.append("<tbody>");
			for(var i=0;i<data.olditems.length;i=i+1){
				if(data.olditems[i].id!="-1"){
					items.append("<tr><div class=\"row1\" id=\"entry\"><label for=\"currentlySelling\">");
					items.append("<td><div class=\"description\"><a href=\"viewitem.html?itemID=").append(data.olditems[i].id).append("\">").append(data.olditems[i].name).append("</a></div></td>");
					items.append("<td><div class=\"maxbid\"><label for=\"maxBid39646\">").append(data.olditems[i].currentBid).append("<label></div></td>");
					items.append("<td><div class=\"enddate\"><label for=\"endDate39646\">").append(data.olditems[i].endDate).append("</label></div></td>");
					items.append("</label></div></tr>");
				}
			}
			if(data.olditems.length==0){
				items.append("<tr><td colspan=\"3\">No Previously Sold Items</td></tr>");
			}
			items.append("</tbody></div></table>");*/





			/*for(var i=0;i<data.sellers.length;i=i+1){
				if(data.sellers[i].id!="-1"){
					database.insertseller(data.sellers[i].id,data.sellers[i].name);
				}
			}*/

		},
		error:function(jqXHR,textStatus,errorThrown){
			if(debug) $('#scriptError').append("<br />userAccount: Could not write/read bids and items " + new Date().getTime());
		}
	});
	return false;
};

/*
 * Sends the sell data to the server and sees if all the items information is correct
 * used by: sell.html
 *///ANDY
function sellitem(){
	// Set what userID we will send
	var userID = localStorage['userID'];
	if(!isEmpty(userID) && userID!="-1") userID = "&userID=" + userID;
	else userID="";

	// set the auth token we will send
	var authToken = localStorage['authToken'];
	if(!isEmpty(authToken)) authToken = "&authToken=" + authToken;
	else authToken="";

	var response=$.ajax({
		async: false,
		url:$.urldomain()+"/sellitem?useHTML5=1"+userID+authToken+"&name="+$("#sell_name").val()+"&description="+$("#sell_description").val()+"&startPrice="+$("#sell_startPrice").val()+"&reservePrice="+$("#sell_reservePrice").val()+"&buyNowPrice="+$("#sell_buyNowPrice").val()+"&categoryID="+$("#categoryID").val()+"&endDate="+$("#sell_endDate").val()+"&quantity="+$("#sell_quantity").val(),
		type: "GET",
		dataType: "json",
		success:function(data){
			// If the item was successfully entered then forward to the images page
			if(!isEmpty(data) && (data.success===true||data.success==='true')){
				window.location.href="./sellitemimages.html?itemID="+data.itemid;				
			}
			// else display why the item was not sold
			else{
				if(!isEmpty(data))
					printErrorsAndStats(data);
				/*var errormsgs = "<div class=\"errors\"><div class=\"title\">Errors: "+data.errors.length+"</div>";
				for(var i=0;i<data.errors.length;i=i+1){
					errormsgs=errormsgs+"<div class=\"row"+(i+1)+"\" id=\"entry\"><div class=\"nocol\">"+data.errors[i].errorNumber+"</div><div class=\"msgcol\">"+data.errors[i].errorMessage+"</div></div>";
				}
				errormsgs=errormsgs+"</div>";
				$("#body_content").html(errormsgs);	*/				
			}		
		},
		error:function(jqXHR,textStatus,errorThrown){
			$('#scriptError').append("<br />sellitem: could not send request " + new Date().getTime());

		}
	});
};


/*function uploadItemImages() {
	//var data = contentDocument.body.innerText;
	var data = $('#hidf').contents().find('body').html();
	alert("fun " + data);

	if(isEmpty(data) || !data) {
		return false;
	}

	try {
		data = jQuery.parseJSON(data);

		contentDocument.location.href = "";
		if(!isEmpty(data) && (data.success === true || data.success === 'true')) {
			window.location.href = "./confirmsellitem.html";
		}
		// else display why the item was not sold
		else {
			if(!isEmpty(data))
				printErrorsAndStats(data);
		}

		return false;
	} catch(err) {
		// Sometimes the page reloads the HTML
	}

};*/


/*
 * Upload images for items
 * used by sellitemimages.html
 *///ANDY
//TODO: delete this!
function sellitemimagesupload(){

	// Set what userID we will send
	/*var userID = localStorage['userID'];
	if(!isEmpty(userID) && userID!="-1") userID = "&userID=" + userID;
	else userID="";

	// set the uath token we will send
	var authToken = localStorage['authToken'];
	if(!isEmpty(authToken)) authToken = "&authToken=" + authToken;
	else authToken="";*/


	var formData = new FormData();
	formData.append("image1", $("#image1")[0].files[0]);
	formData.append("image2", $("#image2")[0].files[0]);
	formData.append("image3", $("#image3")[0].files[0]);
	formData.append("image4", $("#image4")[0].files[0]);
	formData.append("itemID", $("#itemID").val());
	formData.append("useHTML5", "1");


	var userID = localStorage['userID'];
	if(!isEmpty(userID) && userID!="-1") formData.append("userID", userID);

	var authToken = localStorage['authToken'];
	if(!isEmpty(authToken)) formData.append("authToken", authToken);

	/*jQuery.each($('#file')[0].files, function(i, file) {
		data.append('file-' + i, file);
	});*/

	/*type: "POST",
		data: data,
		    	cache: false, multipart/form-data, boundary=
    	contentType: false,type: ppiFormMethod,
    	processData: false,*/
	//var fmdta2 = $("#upload_img_form").serializeArray();

	var response=$.ajax({
		async: false,
		url:$.urldomain()+"/sellitemimages?useHTML5=1",
		type: "POST",
		data: formData,
		cache: false,
		contentType: false,
		processData: false,
		dataType: "json",
		success:function(data){

			// If the item was successfully entered then forward to the images page
			if(!isEmpty(data) && (data.success===true||data.success==='true')){
				/*if(!isEmpty(data))
					printErrorsAndStats(data);*/

				window.location.href="./confirmsellitem.html";			
			}
			// else display why the item was not sold
			else{
				if(!isEmpty(data))
					printErrorsAndStats(data);
				/*var errormsgs = "<div class=\"errors\"><div class=\"title\">Errors: "+data.errors.length+"</div>";
				for(var i=0;i<data.errors.length;i=i+1){
					errormsgs=errormsgs+"<div class=\"row"+(i+1)+"\" id=\"entry\"><div class=\"nocol\">"+data.errors[i].errorNumber+"</div><div class=\"msgcol\">"+data.errors[i].errorMessage+"</div></div>";
				}
				errormsgs=errormsgs+"</div>";
				$("#body_content").html(errormsgs);	*/				
			}		
		},
		error:function(jqXHR,textStatus,errorThrown){
			$('#scriptError').append("<br />sellitemimage: could not upload images " + new Date().getTime());
		}
	});


};

function sellitemimagespage(itemid){
	window.location.href=$.urldomain()+"/sellitemimages.html?itemID="+itemid;

};

function sellitemimages(){
	return false;
};

function getAccountInfo(){
	var response=$.ajax({
		async: false,
		url:$.urldomain()+"/updateuserdetails?useHTML5=1&userID="+localStorage['userID']+"&authToken="+localStorage['authToken'],
		type: "GET",
		dataType: "json",
		success:function(data){

			// If the users data comes back set the form values
			if(!isEmpty(data) && !isEmpty(data.user)) {
				$('#password1').val(data.user.password);
				$('#password2').val(data.user.password);
				$('#email1').val(data.user.email);
				$('#email2').val(data.user.email);
				$('#firstname').val(data.user.firstname);
				$('#lastname').val(data.user.lastname);
				$('#addressIsDefault').val(data.address.isdefault);
				$('#addressID').val(data.address.addressid);
				$('#street').val(data.address.street);
				$('#town').val(data.address.town);
				$('#zip').val(data.address.zip);

				database.populateStatesSelect(data.address.state);
			}

			printErrorsAndStats(data);
		},
		error:function(jqXHR,textStatus,errorThrown){
			alert(textStatus+" , "+errorThrown);
		}
	});
};

/*
 * Used to update user's datails
 * used by updateuserdetails.html
 */
function updateAccountInfo(){
	// Set what userID we will send
	var userID = localStorage['userID'];
	if(!isEmpty(userID) && userID!="-1") userID = "&userID=" + userID;
	else userID="";

	// set the auth token we will send
	var authToken = localStorage['authToken'];
	if(!isEmpty(authToken)) authToken = "&authToken=" + authToken;
	else authToken="";

	var response=$.ajax({
		async: false,
		url:$.urldomain()+"/updateuserdetails?useHTML5=1"+userID+authToken+"&password1="+$('#password1').val()+"&password2="+$('#password2').val()+"&email1="+$('#email1').val()+"&email2="+$('#email2').val()+"&firstname="+$('#firstname').val()+"&lastname="+$('#lastname').val()+"&street="+$('#street').val()+"&town="+$('#town').val()+"&zip="+$('#zip').val()+"&state="+$('#state').val()+"&addressID="+$('#addressID').val()+"&addressIsDefault="+$('addressIsDefault').val(),
		type: "POST",
		dataType: "json",
		success:function(data){
			// If the user was updated forward to myaccount
			if(!isEmpty(data) && data.success==true){
				window.location.href="./myaccount.html";
			}else{
				printErrorsAndStats(data);
			}
		},
		error:function(jqXHR,textStatus,errorThrown){
			alert(textStatus+" , "+errorThrown);
		}
	});
};

//TODO: remove
function browseItemsArray(itemarray){
	var items= new StringBuffer();
	items.append("<div class=\"nine columns\">");
	items.append("<table>");
	items.append("<div class=\"items\"><thead><tr><th><div class=\"title\"><div class=\"img\">Name</div></th><th><div class=\"desc\">Description</div></th><th><div class=\"bid\"><img src=\"images/arrow_down.png\" alt=\"sort by price\" width=\"15\"/>Bid Price</div></th><th><div class=\"endDate\"><img src=\"images/arrow_down.png\" alt=\"sort by end date\" width=\"15\"/>End date</div></th></tr></thead></div>");
	items.append("<tbody>");
	if(itemarray.length>0){
		for(var i=0;i<itemarray.length;i=i+1){
			var row = new StringBuffer();
			row.append("<tr><div class=\"entry\" id=\"entry\">");
			row.append("<td><div class=\"img\"><img height=\"80\" width=\"80\" src=\"../img/");
			row.append(itemarray[i].thumbnail);
			row.append("\" alt=\"");
			row.append(itemarray[i].description);
			row.append("\" /></div></td>");
			row.append("<td><div class=\"desc\"><label for=\"itemlink");
			row.append(itemarray[i].id);
			row.append("\"><a href=viewitem.html?itemID=");
			row.append(itemarray[i].id);
			row.append(" >");
			row.append(itemarray[i].name);
			row.append("</a></label></div></td>");
			row.append("<td><div class=\"bid\"><label for=\"itemBid");
			row.append(itemarray[i].id);
			row.append("\">$");
			row.append(parseFloat(itemarray[i].currentBid).toFixed(2));
			row.append("</label></div></td>");
			row.append("<td><div class=\"endDate\"><label for=\"itemEndDate");
			row.append(itemarray[i].id);
			row.append("\">");
			row.append(itemarray[i].endDate);
			row.append("</label></div></td>");
			row.append("</div></tr>");
			items.append(row);
		}
	}
	items.append("</tbody></table></div></div>");
	$("#body_content").html(items.toString());
	hideLoading();
};

function browseCategoriesArray(categoriesarray){
	var categories= new StringBuffer();
	categories.append("<div class=\"three columns\">");
	//categories.append("<div class=\"cats\">");
	categories.append("<ul STYLE=\"list-style-image: url(images/orbit/bullets.jpg)\">");
	var itemsPP = GV.itemsPP;

	if(categoriesarray.length>0){
		for(var i=0;i<categoriesarray.length;i=i+1){
			var cat = new StringBuffer();
			cat.append("<li><a href=\"browse.html?categoryID=");
			cat.append(categoriesarray[i].categoryID);
			cat.append("&pageNo=0&itemsPP=");
			cat.append(itemsPP);
			cat.append("\" >");
			cat.append(categoriesarray[i].name);
			cat.append("</a></li>");
			categories.append(cat);
		}
	}
	categories.append("</ul></div>");
	$("#category_content").html(categories.toString());
};

/*
 * Called when the browse category page is loaded
 * browse.html
 */
function browseCategory(categoryID,pageNo,itemsPP){
	database.syncInfo(categoryID,pageNo,itemsPP);
};

function browseSearch(searchTerm,pageNo,itemsPP){
	database.syncInfoSearch(searchTerm,pageNo,itemsPP);
};

/*
 * Used to display another user's details
 * used by viewuser.html
 */
function viewUser(id){
	database.getseller(id);
};
