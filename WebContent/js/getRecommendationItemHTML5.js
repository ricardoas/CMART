/*
 * Author: Bo (bol1@andrew.cmu.edu)
 * 
 * This makeRequest() is used to roll recommended items on the welcome page through AJAX
 * */
var http_request;
function makeRequest(pageNo) {
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

	var url = "index?userID=" + localStorage['userID'] +"&authToken=" + localStorage['authToken'] + "&getRecommendation=1&recommendationPageNo=" + pageNo; 
	http_request.open('GET', url, true);
	http_request.send(null);	
}


function handleStateChange() {
	if (http_request.readyState == 4) {
		if (http_request.status == 0 || http_request.status == 200) {
			var pageNo = readXML();
			var pageNum = 0;
			// If pageNo is greater than zero, show prev button
			if(pageNo > 0){
				pageNum = parseInt(pageNo) - 1;
				document.getElementById("recommendation_prev").href = "javascript:makeRequest(" + pageNum + ")";
			}else if(pageNo == 0){
				document.getElementById("recommendation_prev").href = "javascript:makeRequest(0)";
			}else{
				pageNum = parseInt((-1) * pageNo) - 1;
				document.getElementById("recommendation_prev").href = "javascript:makeRequest(" + pageNum + ")";
			}
			// If there is more items to show, display next button
			if(pageNo >= 0){
				pageNum = parseInt(pageNo) + 1;
				document.getElementById("recommendation_next").href = "javascript:makeRequest(" + pageNum + ")" ;
			}else{
				document.getElementById("recommendation_next").href = "javascript:makeRequest(0)";
			}
		}	 
		else {
			//http_request.status != 200
			alert("Error: Cannot get recommendation item!\n");
		}
	}
}

function readXML() {
	if(!isEmpty(http_request)){
	var userid = document.getElementById("userID").value;
	var authToken = document.getElementById("authToken").value;
	var itemsPP = document.getElementById("itemsPP").value;
	var items = http_request.responseXML.getElementsByTagName("item");
	
	
	if(items == null ){
		return 0;
	}
	var pageNum = 0;
	var i = 0;
	for(i = 0; i < items.length; i++) {
		// Get XML data
		var item = items[i];
		var id = item.getElementsByTagName("id")[0].firstChild.data;
		var name = item.getElementsByTagName("name")[0].firstChild.data;
		var thumbnailURL = item.getElementsByTagName("thumbnailURL")[0].firstChild.data;
		pageNum = item.getElementsByTagName("pageNo")[0].firstChild.data;
		
		// Modify recommendation item 
		var imageID = "recommendedImage_" + i;
		var itemID = "recommendationItem_" + i;
		var itemURL = "viewitem.html?itemID=" + id;
		
		if(!isEmpty(document.getElementById(imageID)))
				document.getElementById(imageID).src = thumbnailURL;
		
		if(!isEmpty(document.getElementById(itemID))){
			document.getElementById(itemID).href = itemURL;
			document.getElementById(itemID).innerHTML = name;
		}
	}
	for(; i < itemsPP; i++){
		var imageID = "recommendedImage_" + i;
		var itemID = "recommendationItem_" + i;
		document.getElementById(imageID).src = "";
		document.getElementById(itemID).href = "";
		document.getElementById(itemID).innerHTML = "";
	}
	
	if(items.length < itemsPP){
		return (-1) * pageNum;
	}
	
	return pageNum;
	}
}

function isEmpty(value) {
	if(typeof value === undefined
		|| typeof value === 'undefined'
		|| value === null
		|| value === 'NULL'
		|| value === ''
		|| value === 0
		|| value === '0'
		|| value === false
		|| !(value)
	) return true;
	else return false;
}