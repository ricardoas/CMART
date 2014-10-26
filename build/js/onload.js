debug = 1;

window.onload = function (){
	// Get the current page identifier
	var curPage = $.urlPage();
	if(curPage=="0"){
		curPage="index";}
	
	// Init the DB connection
	database.initDb();
	
	// Set the userID to -1 if the user does not have one
	if(isEmpty(localStorage.getItem("userID"))){
		localStorage.setItem("userID",-1);
		setLogInHeader();
	}
	// Automatically forward logged in users away from the register or login pages
	else if(localStorage.getItem("userID")!=-1){
		setLogOutHeader();
		
		//John put this to autoforward if logged in, removed for now - Andy
		/*switch(curPage){
			case "login":
				window.location.href="./myaccount.html";
				break;
			case "register":
				window.location.href="./myaccount.html";
				break;
			default:
				setLogOutHeader();
				break;
		}*/
	}else{
		setLogInHeader();
	}
	
	// Decide which actions to perfrom based on the current page
	switch(curPage){
		case "index":
			// If the user is logged in display their reccommended items
			if(!isEmpty(localStorage['userID']) && localStorage['userID'] !== "-1"){
				$("#userID").val(localStorage['userID']);
				
				addRecommendation();
			}
			if(!isEmpty(localStorage['authToken']) && localStorage['authToken'] !== "-1")
				$("#authToken").val(localStorage['authToken']);
			makeRequest(0);
			break;
		case "sell":
			var now = new Date();
			now.setDate(now.getDate()+7);
			$("#sell_endDate").val(now.format("yyyy-mm-dd HH:MM:ss"));
			//database.populateCategoriesSelect();
			
			database.testcatsselect();
			$('#categoryID1').hide();
			$('#categoryID2').hide();
			$('#categoryID3').hide();
			$('#categoryID4').hide();
			
			break;
		case "register":
			database.populateStatesSelect();
			
			$("#state").attr("onchange", "checkStateSelected();");
			$("#state").attr("onblur", "checkStateSelected();");
			checkStateSelected();
			
			break;
		case "buyitem":
			// Populate the states
			database.populateStatesSelect($.urlParam("state"));
			
			$("#street").val(decodeURIComponent($.urlParam("street")));
			$("#town").val(decodeURIComponent($.urlParam("town")));
			$("#zip").val($.urlParam("zip"));
			$("#addressID").val($.urlParam("addressid"));
			$("#itemID").val($.urlParam("itemID"));
			 
			break;
		case "browse":
			//showLoading();
			var categoryID = $.urlParam("categoryID");
			var itemsPP = $.urlParam("itemsPP");
			var pageNo = $.urlParam("pageNo");
			if(!isEmpty(itemsPP) || itemsPP<1){itemsPP=GV.itemsPP;};
			var pageNoPlus=parseInt(pageNo)+1;
			if(pageNo>0){
				var pageNoMinus=parseInt(pageNo)-1;
				$("#links").html("<a href=\"browse.html?categoryID="+categoryID+"&pageNo="+pageNoMinus+"&itemsPP="+itemsPP+"\" >< Previous Page </a>|<a href=\"browse.html?categoryID="+categoryID+"&pageNo="+pageNoPlus+"&itemsPP="+itemsPP+"\" > Next Page ></a>");
			}else{
				$("#links").html("< Previous Page | <a href=\"browse.html?categoryID="+categoryID+"&pageNo="+pageNoPlus+"&itemsPP="+itemsPP+"\" > Next Page ></a>");
			}
			browseCategory(categoryID,pageNo,itemsPP);
			break;
		case "viewitem":
			viewItem($.urlParam("itemID"));
			break;
		case "myaccount":
			userAccount();
			break;
		case "viewuser":
			viewUser($.urlParam("viewUserID"));
			break;
		case "updateuserdetails":
			getAccountInfo();
			break;
		case "logout":
			userLogout();
			break;
		case "commentitem":
			commentPage($.urlParam("itemID"));
			break;
		case "askquestion":
			questionPage($.urlParam("itemID"));
			break;
		case "answerquestion":
			answerPage($.urlParam("itemID"),$.urlParam("questionID"));
			break;
		case "bidhistory":
			bidHistory($.urlParam("itemID"));
			break;
		case "search":
			var itemsPP = $.urlParam("itemsPP");
			var pageNo = $.urlParam("pageNo");
			if(!isEmpty(itemsPP) || itemsPP<1){itemsPP=GV.itemsPP;};
			var pageNoPlus=parseInt(pageNo)+1;
			database.getSearchItems($.urlParam("searchTerm"), pageNo, itemsPP);
			
			break;
		case "sellitemimages":
			$("#userID").val(localStorage.getItem("userID"));
			$("#authToken").val(localStorage.getItem("authToken"));
			$("#itemID").val($.urlParam("itemID"));
			$("#useHTML5").val("1");
			
			// Set the action of the upload form to the correct URL
			$("#upload_img_form").attr("action",$.urldomain()+"/sellitemimages?useHTML5=1; false;");
			
			// set the upload button to upload the images
			$("#uploadBt").click(function() {
				// create a dynamic iframe, or use existing one
				var iframe = $("<iframe id='hidf' name='hidf' src='' >");
				// style='display: none;'
				//iframe.load(uploadItemImages());
				
				
				// attach a load event to handle response/ know about completion
				iframe.load(function() {
					var data = this.contentDocument.body.innerText;
					
					if(isEmpty(data) || !data) {
						//return;
					}
					

					try {
						data = jQuery.parseJSON(data);

						this.contentDocument.location.href = "";
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

					
				});
				
				iframe.appendTo('body');
				
				// change form's target to the iframe (this is what simulates ajax)
				$('#upload_img_form').attr('target', 'hidf');
				$('#upload_img_form').submit();
			});

			//$("#upComplete").click(al());
			
			break;
		//case "viewvideo":
			// $("#body_content").html("<div class=\"video-js-box\"><video id=\"video\" class=\"video-js\" width=\"640\" height=\"264\" controls=\"controls\" preload=\"auto\" \"><source src=\"../videos/"+$.urlParam("video")+"\" type='video/mp4; codecs=\"avc1.42E01E, mp4a.40.2\"' /><object id=\"flash_fallback_1\" class=\"vjs-flash-fallback\" width=\"640\" height=\"264\" type=\"application/x-shockwave-flash\" data=\"http://releases.flowplayer.org/swf/flowplayer-3.2.1.swf\"><param name=\"movie\" value=\"http://releases.flowplayer.org/swf/flowplayer-3.2.1.swf\" /><param name=\"allowfullscreen\" value=\"true\" /><param name=\"flashvars\" value='config={\"playlist\":[\"http://video-js.zencoder.com/oceans-clip.png\", {\"url\": \"../videos/"+$.urlParam("video")+"\",\"autoPlay\":false,\"autoBuffering\":true}]}' /><img src=\"http://video-js.zencoder.com/oceans-clip.png\" width=\"640\" height=\"264\" alt=\"Poster Image\" title=\"No video playback capabilities.\" /></object></video></div>");
		default:
			break;
	}
};

function al(){
	alert("did the alert only");
};

var GV = {
		itemsPP: 25,
		debug: true,
		freshTime: 300000,
};