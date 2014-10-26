/*
 * Author: Bo (bol1@andrew.cmu.edu)
 * 
 * This updateItemPrice() is used to update itemCurrentBid price every five seconds through AJAX
 * when a user is viewing an item which allows bidding
 * */

function updateItemPrice(){
	http_request = false;
	// create XML http request
	if (window.XMLHttpRequest) {
		http_request = new XMLHttpRequest();
		if (http_request.overrideMimeType){
	 		http_request.overrideMimeType('text/xml');
		}
	} 
	else if (window.ActiveXObject) {
		try{
			http_request = new ActiveXObject("Msxml2.XMLHTTP");
		} 
		catch (e) {
 			try {
				http_request = new ActiveXObject("Microsoft.XMLHTTP");
 			} 
 			catch (e) {
 			}
		}
	}
	if (!http_request) {
		alert("Your browser cannot support such operation");
		return false;
	}
	
	http_request.onreadystatechange = handleStateChange;
	var userid = document.getElementById("userID").value;
	var authToken = document.getElementById("authToken").value;
	var itemID = document.getElementById("itemID").value;
	var itemIsNew = document.getElementById("itemIsOld").value;
	// Only send get itemCurrentBid request if the item is not old
	if(itemIsNew != null){
		var url = "viewitem?userID=" + userid +"&authToken=" + authToken + "&itemID=" + itemID + "&itemCurrentBid=1"; 
		http_request.open('GET', url, true);
		http_request.send(null);
		// Send get itemCurrentBid request every 5 seconds
		setTimeout("updateItemPrice()", 5000);
	}
	
}


function handleStateChange() {
	if (http_request.readyState == 4) {
		if (http_request.status == 0 || http_request.status == 200) {
			var result = http_request.responseText;
	 		// For debug use, enable: alert("Current Price: " + result);
			//alert("Current Price: " + result);
			document.getElementById("itemCurrentPrice1").innerHTML = result;
	 		document.getElementById("itemCurrentPrice2").innerHTML = result;
	 		document.getElementById("itemCurrentPrice3").innerHTML = result;
		}
	 		else {
			//http_request.status != 200
			document.getElementById("itemCurrentPrice1").innerHTML = "cannot get.";
			document.getElementById("itemCurrentPrice2").innerHTML = "cannot get.";	
			document.getElementById("itemCurrentPrice3").innerHTML = "cannot get.";	
		}
	}
}