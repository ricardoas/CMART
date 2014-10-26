var database = {
		// declare and initalize global variables
		db : null,

		/*
		 * Create the C-MART database to hold website data
		 * Will insert base info such as categories and states at start
		 * Used by all pages
		 *///ANDY
		initDb: function () {
			if (window.openDatabase){
				this.db = openDatabase("CMART1117","0.02","CMART Local Database",500000);

				// Try to speed things up a bit
				this.db.transaction(function(tx) {
					tx.executeSql("PRAGMA synchronous = 0");
					tx.executeSql("PRAGMA journal_mode = OFF");
					tx.executeSql("PRAGMA page_size = 4096");
					tx.executeSql("PRAGMA default_cache_size = 10000");
				});



				// If the local storage has not recorded that the database is populated, repopulate it
				if(isEmpty(localStorage['populated'])){
					if(debug) $('#scriptDebug').append("<br />initDB: starting to populate " + new Date().getTime());

					this.createTables();
					this.insertBulkData();
					localStorage.setItem("populated",1);

					if(debug) $('#scriptDebug').append("<br />initDB: finished populate " + new Date().getTime());
				}
				else
					if(debug) $('#scriptDebug').append("<br />initDB: already populated " + new Date().getTime());
			}
		},

		/*
		 * Create the database tables if they do not exist
		 * Used by all pages (called by initDB)
		 *///ANDY
		createTables: function () {
			database.db.transaction(function(tx) {
				tx.executeSql("CREATE TABLE IF NOT EXISTS users (id INT PRIMARY KEY,firstname TEXT ,lastname TEXT,username TEXT,email TEXT)", [],
						function (tx, rs) { },
						function (tx, err) { }
				);
				tx.executeSql("CREATE TABLE IF NOT EXISTS addresses (id INT PRIMARY KEY,userID INT,street TEXT,town TEXT,state INT,zip TEXT,isDefault BOOLEAN)", [],
						function (tx, rs) { },
						function (tx, err) { }
				);
				tx.executeSql("CREATE TABLE IF NOT EXISTS states (id INT PRIMARY KEY,shortName TEXT,longName TEXT)", [],
						function (tx, rs) { },
						function (tx, err) { }
				);
				tx.executeSql("CREATE TABLE IF NOT EXISTS items (id INT PRIMARY KEY,itemnumber INT,myaccount INT,name TEXT,description TEXT, quantity INT, startPrice FLOAT, reserve BOOLEAN, buyNowPrice FLOAT, currentBid FLOAT, noOfBids INT, startDate TIMESTAMP, endDate TIMESTAMP, sellerID INT, sellerName TEXT, categoryID INT, images TEXT, thumbnail TEXT, ts TIMESTAMP)", [],
						function (tx, rs) { },
						function (tx, err) { }
				);
				tx.executeSql("CREATE TABLE IF NOT EXISTS bids (id INT PRIMARY KEY,quantity INT,bid FLOAT, maxBid FLOAT, bidDate TIMESTAMP, itemID INT, isOld INT)", [],
						function (tx, rs) { },
						function (tx, err) { }
				);
				tx.executeSql("CREATE TABLE IF NOT EXISTS purchases (id INT PRIMARY KEY,quantity INT,price FLOAT, itemID INT, paid BOOLEAN,name TEXT)", [],
						function (tx, rs) { },
						function (tx, err) { }
				);
				tx.executeSql("CREATE TABLE IF NOT EXISTS categories (id INT PRIMARY KEY,parent INT,name TEXT, ts LONG)", [],
						function (tx, rs) { },
						function (tx, err) { }
				);
				tx.executeSql("CREATE TABLE IF NOT EXISTS itemcache (id TEXT PRIMARY KEY, categoryid INT, items INT, range INT,sync TIMESTAMP)", [],
						function (tx, rs) { },
						function (tx, err) { }
				);
				tx.executeSql("CREATE TABLE IF NOT EXISTS sellers (id INT PRIMARY KEY, username TEXT, rating INT, ts TIMESTAMP)", [],
						function (tx, rs) { },
						function (tx, err) { }
				);
				tx.executeSql("CREATE TABLE IF NOT EXISTS questions (id INT PRIMARY KEY, fromUserID LONG, toUserID LONG, itemID LONG, isQuestion BOOLEAN, postDate TEXT, responseTo LONG, content LONG)", [],
						function (tx, rs) { },
						function (tx, err) { }
				);
				tx.executeSql("CREATE TABLE IF NOT EXISTS comments (id INT PRIMARY KEY, fromUserID LONG, toUserID LONG, itemID LONG, rating INT, date TEXT, content LONG)", [],
						function (tx, rs) { },
						function (tx, err) { }
				);
				tx.executeSql("CREATE INDEX p1 ON items (categoryID, itemnumber)", [],
						function (tx, rs) { },
						function (tx, err) { }
				);
				
				/*tx.executeSql("CREATE INDEX p2 ON itemcache (categoryid, items)", [],
					function (tx, rs) { },
					function (tx, err) { }
					);*/
			});
			//TODO: make indexes
		},

		/*
		 * Inserts the categories and states
		 * Used by all pages (called by initDB)
		 *///ANDY
		//TODO: allow only updates of cat or state, allow ts to be passed
		insertBulkData: function(){
			$.ajax({ 
				async: false,
				url:$.urldomain()+"/getbulkdata?useHTML5=1",
				type: "GET",
				dataType: "json",
				success:function(data){
					// Insert the categories
					if(!isEmpty(data.categories)) {
						database.insertCategories(data.categories);
					}

					// Insert the states
					if(!isEmpty(data.states)) {
						database.insertStates(data.states);
					}

				},
				error:function(jqXHR,textStatus,errorThrown){
					$('#scriptError').append("<br />insertBulkData " + errorThrown + " " + new Date().getTime());
				}
			});		
		},


		decodeHTML: function(value){
			return $('<div></div>').html(value).text();
		},
		
		/*
		 * Inserts categories
		 * Used by all pages (called by insertBuldData)
		 *///ANDY
		insertCategories: function(categories) {
			if(!isEmpty(categories))
				// Insert the categories
				this.db.transaction(function(tx) {
					var ts = new Date().getTime();
					var sqlInsert = "INSERT INTO categories (id,parent,name,ts) VALUES (?,?,?,?)";

					for(var i = 0; i < categories.length; i = i + 1) {
						// Insert category
						tx.executeSql(sqlInsert, [categories[i].categoryID, categories[i].parent, database.decodeHTML(categories[i].name), ts],
								function(tx, rs) { },
								function(tx, err) { }
						);
					}

				});
		},

		/*//TO DELETE
		 * Inserts a bid
		 *///ANDY
		insertBid: function(bid) {
			if(!isEmpty(bid))
				// Insert the bid
				this.db.transaction(function(tx) {
					//var ts = new Date().getTime();
					var sqlInsert = "INSERT INTO bids (id, quantity, bid, maxBid, bidDate, itemID) VALUES (?,?,?,?,?,?)";

					tx.executeSql(sqlInsert, [bid.bidItem.id, bid.quantity, bid.bid, bid.maxBid, bid.bidDate, bid.bidItem.id],
							function(tx, rs) { },
							function(tx, err) {
								$('#scriptError').append("<br />insertBid " + err.message + " " + new Date().getTime());
							}
					);

				});
		},
		/*
		 * Inserts a JSON array of bids
		 * Called for myaccount page
		 *///ANDY
		insertBids: function(bids, isOld) {
			if(!isEmpty(bids))
				// Insert the bid
				this.db.transaction(function(tx) {
					var sqlInsert = "INSERT INTO bids (id, quantity, bid, maxBid, bidDate, itemID, isOld) VALUES (?,?,?,?,?,?,?)";

					for(var i=0; i<bids.length; i++)
						tx.executeSql(sqlInsert, [bids[i].bidItem.id, bids[i].quantity, bids[i].bid, bids[i].maxBid, bids[i].bidDate, bids[i].bidItem.id, isOld],
								function(tx, rs) { },
								function(tx, err) {
									$('#scriptError').append("<br />insertBids " + err.message + " " + new Date().getTime());
								}
						);

				});
		},
		
		/*
		 * Inserts a JSON array of purchases
		 * Called for myaccount page
		 *///FOX
		insertPurchase: function(purchase,name,itemID) {
			if(!isEmpty(purchase))
				// Insert the bid
				this.db.transaction(function(tx) {
					//var ts = new Date().getTime();
					var sqlInsert = "INSERT INTO purchases (id, quantity, price, itemID, paid,name) VALUES (?,?,?,?,?,?)";

					tx.executeSql(sqlInsert, [itemID, purchase.quantity, purchase.price, itemID,purchase.paid,name],
							function(tx, rs) { },
							function(tx, err) {
								$('#scriptError').append("<br />insertPurchase " + err.message + " " + new Date().getTime());
							}
					);

				});
		},

		/*
		 * Insert states. Typically when the page is loaded for the first time so that
		 * the user has the data on hand for the rest of the session
		 * Used by all pages (called by insertBuldData)
		 *///ANDY
		insertStates: function(states) {
			if(!isEmpty(states))
				this.db.transaction(function(tx) {
					var sqlInsert = "INSERT INTO states (id, shortName, longName) VALUES  (?,?,?)";

					//Loop through all states and insert them
					for(var i = 0; i < states.length; i = i + 1) {
						// Insert state
						tx.executeSql(sqlInsert, [states[i].id, states[i].shortName, states[i].longName],
								function(tx, rs) { },
								function(tx, err) { }
						);
					}
				});
		},

		/*
		 * Populate the states box on forms where you can select
		 *///ANDY APPROVED

		populateStatesSelect: function(defaultstate) {
			// First make sure there are states in the local DB, otherwise we'll refresh them
			this.db.transaction(function(tx) {
				tx.executeSql("SELECT * FROM states LIMIT 1", [], function(tx, rs) {
					if(rs.rows.length == 0) {
						database.insertBulkData();
					}
				}, function(tx, err) {
					database.insertBulkData();
					$('#scriptError').append("<br />populateStatesSelect " + err.message + " " + new Date().getTime());

				});
			});

			if(debug) $('#scriptDebug').append("<br />populateStatesSelect: populating " + new Date().getTime());

			// We should now have the data, so populate the boxes
			this.db.transaction(function(tx) {
				tx.executeSql("SELECT * FROM states ORDER BY shortName", [], function(tx, rs) {
					var states_data = new StringBuffer();


					// If the states get returned, fill the box
					if(rs.rows.length > 0) {
						if(isEmpty(defaultstate)) {
							states_data.append('<select id="state"><option value="0" SELECTED>Please Select...</option>');

							for(var i = 0; i < rs.rows.length; i = i + 1) {
								states_data.append("<option value=\"")
								states_data.append(rs.rows.item(i)["id"])
								states_data.append("\">");
								states_data.append(rs.rows.item(i)["shortName"]);
								states_data.append(" - ");
								states_data.append(rs.rows.item(i)["longName"]);
								states_data.append("</option>");
							}
						} else {
							states_data.append('<select id="state"><option value="0">Please Select...</option>');

							for(var i = 0; i < rs.rows.length; i = i + 1) {
								// Set the default state
								if(rs.rows.item(i)["id"] == defaultstate) {
									states_data.append("<option value=\"");
									states_data.append(rs.rows.item(i)["id"]);
									states_data.append("\" SELECTED>");
									states_data.append(rs.rows.item(i)["shortName"]);
									states_data.append(" - ");
									states_data.append(rs.rows.item(i)["longName"]);
									states_data.append("</option>");
								}
								// Add the state as another option
								else {
									states_data.append("<option value=\"");
									states_data.append(rs.rows.item(i)["id"]);
									states_data.append("\">");
									states_data.append(rs.rows.item(i)["shortName"]);
									states_data.append(" - ");
									states_data.append(rs.rows.item(i)["longName"]);
									states_data.append("</option>");
								}
							}
						}

					}
					else{
						states_data.append('<select id="state"><option value="0">Please Select...</option>');
					}

					states_data.append('</select>');

					// Add the states to the box
					$('#state').replaceWith(states_data.toString());

					//$("#sell_category").html(cat_data.toString());
					//$("#sell_category").selectmenu('refresh');


				}, function(tx, err) {
					$('#scriptError').append("<br />populateStatesSelect " + err.message + " " + new Date().getTime());
				});
			});

		},


		/*
		 * Populate the categories select box. This is used when adding an item to sell
		 *///ANDY APPROVED
		populateCategoriesSelect: function() {
			// First make sure there are categories in the local DB, otherwise we'll refresh them
			this.db.transaction(function(tx) {
				tx.executeSql("SELECT * FROM categories LIMIT 1", [], function(tx, rs) {
					if(rs.rows.length == 0) {
						database.insertBulkData();
						if(debug) $('#scriptDebug').append("<br />populateCategoriesSelect: re-populating DB " + new Date().getTime());
						localStorage.removeItem("populatedCategories");
					}
				}, function(tx, err) {
					database.insertBulkData();
					localStorage.removeItem("populatedCategories");
					if(debug) $('#scriptDebug').append("<br />populateCategoriesSelect: re-populating DB " + new Date().getTime());
				});
			});

			//if(isEmpty(localStorage['populatedCategories']))
			// Read the categories for real
			this.db.readTransaction(function(tx) {
				tx.executeSql("SELECT * FROM categories", [], function(tx, rs) {

					if(rs.rows.length > 0) {
						/*var cat_data = new StringBuffer();
					var line_cat = new StringBuffer();
					cat_data.append("<select id=\"sell_category\">");

					// Append each category
					for(var i = 0; i < rs.rows.length; i = i + 1) {
						line_cat.append("<option value=\"");
						line_cat.append(rs.rows.item(i)["id"]);
						line_cat.append("\">");
						line_cat.append(decodeURIComponent(rs.rows.item(i)["name"]));
						line_cat.append("</option>");

						if(i%100==1){
							cat_data.append(line_cat.toString());
							line_cat = new StringBuffer();
						}
					}
					cat_data.append(line_cat.toString());
					cat_data.append("</select>");

					//var cats = $('sell_category');
					$('#sell_category').replaceWith(cat_data.toString());

					//$("#sell_category").html(cat_data.toString());
					//$("#sell_category").selectmenu('refresh');

					localStorage.setItem("populatedCategories",cat_data.toString());
						 */
						//statesSelect=document.getElementById("states");
						var fragment = document.createDocumentFragment(), op;
						for(var i = 0; i < rs.rows.length; i++) {
							op = document.createElement('option');
							//a.textContent = i;
							text=document.createTextNode(decodeURIComponent(rs.rows.item(i)["name"]));
							op.appendChild(text);
							op.setAttribute("value",rs.rows.item(i)["id"]);
							//li.appendChild(a);
							fragment.appendChild(op);
						}
						document.getElementById('sell_category').appendChild(fragment);	

					}

					if(debug) $('#scriptDebug').append("<br />populateCategoriesSelect building new" + new Date().getTime());

				}, function(tx, err) {
					$('#scriptError').append("<br />populateCategoriesSelect " + err.message + " " + new Date().getTime());
				});
			});
			/*else{
			//var cats = $('sell_category');
					$('#sell_category').replaceWith(localStorage['populatedCategories']);
					//$('#sell_category').replaceWith("<div>Hi</div>");
					//$("#sell_category").html(cat_data.toString());
					//$("#sell_category").selectmenu('refresh');
			//$("#sell_category").html(localStorage['populatedCategories']);
			//$("#sell_category").selectmenu('refresh');
			if(debug) $('#scriptDebug').append("<br />populateCategoriesSelect using cache " + new Date().getTime());

		}*/
		},



		/*
		 * NO LONGER USED- FOX
		 */
		reloadCategoriesArray: function() {
			var cats = [];
			$('#scriptError').append("<br />val " + $("#catagories").val() + " " + new Date().getTime());

			this.db.readTransaction(function(tx) {
				tx.executeSql("SELECT * FROM categories WHERE name LIKE '" + $("#catagories").val() + "%' LIMIT 20", [], function(tx, rs) {

					if(rs.rows.length > 0) {
						for(var i = 0; i < rs.rows.length; i++) {
							var obj = {
									label : rs.rows.item(i)["name"],
									value : rs.rows.item(i)["name"],
									id : rs.rows.item(i)["id"],
							};

							cats.push(obj);


							$("#catagories").autocomplete("option", "source", cats);
						}
					}
				}, function(tx, err) {
					$('#scriptError').append("<br />populateCategoriesSelect " + err.message + " " + new Date().getTime());
				});
			});
		},


		/*
		 * Populates the autocomplete categories
		 * used by sell.html
		 * NO LONGER USED- FOX
		 *///ANDY
		testcats: function() {

			var categories = [{
				value : "cooking",
				label : "Cooking",
				id : "1"
			}, {
				value : "C++",
				label : "C++",
				id : "2"
			}, {
				value : "craftsmanship",
				label : "Software Craftsmanship",
				id : "3"
			}];


			/*var obj = {
			label : "Please select",
			value : "Please select",
			id : 0,
		};
		categories.push(obj);*/

			/*$( "#categories" ).autocomplete({
        minLength: 0,
        source: categories,
        focus: function( event, ui ) {
            $( "#categories" ).val( ui.item.label );
            return false;
        },
        select: function( event, ui ) {
            $( "#categories" ).val( ui.item.label );
            $("#categoryID").val(ui.item.id);   
            return false;
    }
});*/

			var cats = [{
				value : "Please select",
				label : "Please select",
				id : "0"
			}];



			$( "#catagories" ).autocomplete({
				minLength: 0,
				source: cats,
				focus: function( event, ui ) {
					$( "#catagories" ).val( ui.item.label );
					return false;
				},
				select: function( event, ui ) {
					$( "#catagories" ).val( ui.item.label );
					$("#categoryID").val(ui.item.id);
					//$( "#results").text($("#categoryID").val());    
					return false;
				},
				search : function(event, ui) {
					//alert("doing search");
					database.reloadCategoriesArray();
					return true;
				}
			});


			$('#catagories').blur(function() {
				if($('#catagories').val() == "")
					$("#categoryID").val("");
			});



			/*$("#categories").autocomplete({
			minLength : 0,
			source : categories,
			focus : function(event, ui) {
				$("#catagories").val(ui.item.label);
				return false;
			},
			select : function(event, ui) {
				$("#catagories").val(ui.item.label);
				$("#categoryID").val(ui.item.id);
				return false;
			},
			search : function(event, ui) {
				//alert("doing search");
				database.reloadCategoriesArray();
				return true;
			}
		}).keypress(database.reloadCategoriesArray());*/




			/*this.db.readTransaction(function(tx) {
			tx.executeSql("SELECT * FROM categories WHERE name LIKE '" + $("#topics").val() + "a%' LIMIT 20", [], function(tx, rs) {
				/*
				 if(rs.rows.length > 0) {
				 for(var i = 0; i < rs.rows.length; i++) {
				 var obj = {
				 label : rs.rows.item(i)["name"],
				 value : rs.rows.item(i)["name"],
				 id : rs.rows.item(i)["id"],
				 };
				 /*var obj2 = {
				 label : "l" + rs.rows.item(i)["name"],
				 value : "v" + rs.rows.item(i)["id"]
				 };*/

			//topics.push(obj);
			/*topics[i][0] = rs.rows.item(i)["name"];
				 topics[i][1] = rs.rows.item(i)["id"];*
				 }
				 }

				 $('#scriptError').append("<br />length: " + topics.length + " " + new Date().getTime());
				 for(var i = 0; i < topics.length; i++) {
				 $('#scriptError').append("<br />" + topics[i][0] + " " + new Date().getTime());
				 }*



				//$('#scriptError').append("<br />val" + topics[100].label + " " + new Date().getTime());
			}, function(tx, err) {
				$('#scriptError').append("<br />populateCategoriesSelect " + err.message + " " + new Date().getTime());
			});
		});*/

		},
		
		
		/*
		 * Populates the categories on the sell page
		 * used by sell.html
		 *///FOX
		testcatsselect: function() {

			this.db.readTransaction(function(tx) {
				tx.executeSql("SELECT * FROM categories WHERE parent = 0", [], function(tx, rs) {
					var options=new StringBuffer();
					if(rs.rows.length > 0) {
						for(var i = 0; i < rs.rows.length; i++) {
							var obj = {
									name : rs.rows.item(i)["name"],
									id : rs.rows.item(i)["id"],
							};
							options.append("<option value=\""+obj.id+"\">"+obj.name+"</option>");

						}
					}
					$('#categoryID0').html(options.toString());
					
				}, function(tx, err) {
					$('#scriptError').append("<br />populateCategoriesSelect " + err.message + " " + new Date().getTime());
				});	
								
			});
			
			$('#categoryID0').change(function() {
				database.loadcategory(0,1,"#categoryID0","#categoryID1");
			});
			$('#categoryID1').change(function() {
				database.loadcategory(1,2,"#categoryID1","#categoryID2");		
			});
			$('#categoryID2').change(function() {
				database.loadcategory(2,3,"#categoryID2","#categoryID3");
			});
			$('#categoryID3').change(function() {
				database.loadcategory(3,4,"#categoryID3","#categoryID4");
			});



		},
		
		loadcategory: function(origLevel,newLevel,origLevelS,newLevelS) {		
			this.db.readTransaction(function(tx) {
				tx.executeSql("SELECT * FROM categories WHERE parent = "+$(origLevelS).val(), [], function(tx, rs) {
					var options=new StringBuffer();

					if(rs.rows.length > 0) {
						options.append("<option value=\"-1\">Please Select</option>");
						for(var i = 0; i < rs.rows.length; i++) {
							options.append("<option value=\""+rs.rows.item(i)["id"]+"\">"+rs.rows.item(i)["name"]+"</option>");
						}
						if(origLevel<=3)
						$(newLevelS).show();
					}
					$(newLevelS).html(options.toString());
					if(origLevel==0){
						$("#categoryID2").html("").hide();
						$("#categoryID3").html("").hide();
						$("#categoryID4").html("").hide();
					}
					else if(origLevel==1){
						$("#categoryID3").html("").hide();
						$("#categoryID4").html("").hide();
					}
					else if(origLevel==2){
						$("#categoryID4").html("").hide();
					}
				}, function(tx, err) {
					$('#scriptError').append("<br />loadcategory " + err.message + " " + new Date().getTime());
				});	
								
			});

		},




		/*
		 * Gets an item and its seller and inserts it to the local DB
		 * TODO: needs better errors
		 *///ANDY
		updateItemAndSeller: function(id){
			$.ajax({
				async: false,
				url:$.urldomain()+"/viewitem?useHTML5=1&itemID="+id,
				type: "GET",
				dataType: "json",
				success:function(data){
					if(!isEmpty(data) && !isEmpty(data.item)) {
						// Update or insert the item
						database.insertItem(data.item.id,data.item.name,data.item.description,data.item.quantity,
								data.item.startPrice,data.item.reserve,data.item.buyNowPrice,data.item.currentBid,data.item.noOfBids,
								data.item.startDate,data.item.endDate,data.item.sellerID,data.item.sellerName,data.item.categoryID,
								data.item.images,data.item.thumbnail);

					}

					if(!isEmpty(data) && !isEmpty(data.seller)) {
						database.insertSeller(data.seller);
					}
					
					if(!isEmpty(data) && !isEmpty(data.questions)) {
						database.insertQuestions(data.questions);
					}
					
					if(!isEmpty(data) && !isEmpty(data.comments)) {
						database.insertComments(data.comments);
					}

					printErrorsAndStats(data);
					
					// TODO: may need to wait for requests to finish
					database.putHTMLItemPage(id);

					// Insert the categories
					/*if(typeof data.categories != "undefined") {
					database.insertCategories(data.categories);
				}

				// Insert the states
				if(typeof data.states != "undefined") {
					database.insertStates(data.states);
				}*/

				},
				error:function(jqXHR,textStatus,errorThrown){
					var ts = new Date().getTime();
					localStorage.setItem("Error in updateItemAndSeller","AJAX did not succeed " + ts + " " + textStatus);
				}
			});
		},

		/*
		 * Gets the item from the local DB and displays it
		 * used by viewitem.html
		 * TODO: needs better error handling
		 *///ANDY
		getitem: function(id){
			var sellerID = 0;
			var updated = 0;
			var ts = new Date().getTime();
var sentRequest=false;
			// Check the item and the seller are present. If they are not we'll get them.
			// If the are more than 5 minutes old we'll also update them
			this.db.readTransaction(function(tx) {
				tx.executeSql("SELECT id, sellerID, ts FROM items WHERE id=?", [id], function(tx, rs) {

					if(rs.rows.length == 0 || rs.rows.item(0)["ts"] < ts-300000){
						sentRequest=true;
						database.updateItemAndSeller(id);
						updated = 1;
					}
					else{
						sellerID = rs.rows.item(0)["sellerID"];
					}

				}, function(tx, err) {
				});
			});

			if(typeof sellerID != undefined && sellerID != null && sellerID >= 0 && updated==0)
				this.db.readTransaction(function(tx) {
					tx.executeSql("SELECT id FROM sellers WHERE id=?", [sellerID], function(tx, rs) {

						if(rs.rows.length==0) {
							database.updateItemAndSeller(id);
						}
						else{
							database.putHTMLItemPage(id);
						}

					}, function(tx, err) {
					});
				});
			else{
				if(sentRequest==false){
				database.updateItemAndSeller(id);
				}
			}

			
		},
		
		putHTMLItemPage: function(id){
			this.db.readTransaction(function(tx) {
				tx.executeSql("SELECT * FROM items INNER JOIN sellers ON items.sellerID = sellers.id WHERE items.id=?", [id],
						//tx.executeSql("SELECT * FROM items WHERE items.id=?", [id],
						function (tx, rs) {
					if(rs.rows.length>0){
						$('#thumbnail').html("<img src=\"..//img/"+rs.rows.item(0)["thumbnail"]+"\" height=\"40\" width=\"40\" alt=\"\">");
						$('#name').html(rs.rows.item(0)["name"]);
						$('#seller').html("<a href=\"viewuser.html?viewUserID="+rs.rows.item(0)["sellerID"]+"\">"+rs.rows.item(0)["username"]+"</a>");
						$('#sellerRating').html(rs.rows.item(0)["rating"]);
						$('#endDate').html(""+rs.rows.item(0)["endDate"]);
						$('#noOfBids').html("<a href=\"bidhistory.html?itemID="+id+"\">"+rs.rows.item(0)["noOfBids"]+"</a>");



						$('#quantityAvailable').html(rs.rows.item(0)["quantity"]);
						if(rs.rows.item(0)["reserve"]=='true'){
							$('#currentBid').html("$"+rs.rows.item(0)["currentBid"]+" Reserve Not Yet Met");
						}else{
							$('#currentBid').html("$"+rs.rows.item(0)["currentBid"]);
						}

						$('#currentBid2').html("$"+rs.rows.item(0)["currentBid"]);
						$('#currentBid3').html("$"+rs.rows.item(0)["currentBid"]);
						$('#name2').html(rs.rows.item(0)["name"]);
						$('#description').html(rs.rows.item(0)["description"]);

						// If the bidding has finished do not display buy now or bidding forms
						if(new Date(rs.rows.item(0)["endDate"]) < new Date()){
							$("#bidform").html("The bidding on this item has finished<br />Sale price: $" + rs.rows.item(0)["currentBid"]);

						}
						else if(rs.rows.item(0)["buyNowPrice"]>0){
							$('#buynowcost').html("$"+rs.rows.item(0)["buyNowPrice"]);
							$('#buynowform').css('display','block');
						}

						$('#description').html(rs.rows.item(0)["description"]);

						//$('#images').html(decodeURI(rs.rows.item(0)["images"]));
						var images=new Array();
						
						var imageLine=rs.rows.item(0)["images"];
						var k=0;
						if(imageLine.length>0){
							var imageURLs = new Array();
							do{
								var end=imageLine.length;
								if(imageLine.indexOf(",")!=-1)
									end=imageLine.indexOf(",");
								images[k]=decodeURI(imageLine.substring(0,end));
								if(end==imageLine.indexOf(","))
									imageLine=imageLine.slice(end+1,imageLine.length);
								else
									imageLine="";
								k++;
							}while(imageLine.length>0);
						}
						var imageHTML=new StringBuffer();
						for(var i=0;i<images.length;i++){
							imageHTML.append("<div class=\"panel\"><img src=\"..//img/").append(images[i]).append("\" width=\"300\" alt=\"\"></div>");
						}
						if(images.length>0)
							imageHTML.append("<ul class=\"filmstrip\">");
						for(var i=0;i<images.length;i++){
							imageHTML.append("<li><img src=\"..//img/").append(images[i]).append("\" height=\"100\" alt=\"\"></li>");
						}
						if(images.length>0)
							imageHTML.append("</ul>");
						$('#photos').html(imageHTML.toString());
								$(document).ready(function(){
								$('#photos').galleryView({
								panel_width: 400, panel_height: 300, frame_width: 100, frame_height: 100
								});
								});


						// TODO: Written by Andrew
						// Andy, you should probably check this sequence of calls 
						// to make sure that errors are handled properly
						// This was me implementing Bo's update code
						if(new Date(rs.rows.item(0)["endDate"]) > new Date()){
							database.updateItemPrice(id);
						}

					}
					else{
						$("#body_content").html("Error: The item is not present. Perhaps reload the page, or the itemID is incorrect");
					}
				},function (tx, err) {
					alert(tx + " " + err.message);
					$('#endDate').html("ERROR on transaction");
				});});
			
		},
		
		
		/*
		 * Gets the item from the local DB and displays it as well as the bids on the bid history page
		 * used by bidhistory.html
		 * TODO: needs better error handling
		 *///FOX
		loadBids: function(bids,itemID){
			// Check the item and the seller are present. If they are not we'll get them.
			// If the are more than 5 minutes old we'll also update them
			this.db.readTransaction(function(tx) {
				tx.executeSql("SELECT * FROM items WHERE id=?", [itemID], function(tx, rs) {
					if(rs.rows.length > 0){	
						$('#itemName').html(rs.rows.item(0)["name"]);
					}
					$('#itemLink').html("<a href=\"viewitem.html?itemID="+itemID+"\">Return to View Item Page</a>");
					$('#noBids').html("Number of Bids: "+bids.length);
					if(bids.length>0){
						var bidsTable=new StringBuffer();
						bidsTable.append("<table><thead><tr><th>Bidder</th><th>Bid Amount</th><th>Bid Time</th></tr></thead><tbody>");
						for(var i=0;i<bids.length;i++){
							bidsTable.append("<tr><td>");
							bidsTable.append(bids[i].bidderName);
							bidsTable.append("</td><td>");
							bidsTable.append("$"+bids[i].bid);
							bidsTable.append("</td><td>");
							bidsTable.append(bids[i].bidDate);
							bidsTable.append("</td></tr>");
						}
						
						
						bidsTable.append("</tbody></table>");
						$('#bidTable').html(bidsTable.toString());
					}


				}, function(tx, err) {
				});
			});

		},
		
		/*
		 * Gets the item from the local DB and displays it
		 * used by askQuestion.html
		 * TODO: needs better error handling
		 *///FOX
		getitemQuestionPage: function(id){

			this.db.readTransaction(function(tx) {
				tx.executeSql("SELECT * FROM items WHERE id=?", [id], function(tx, rs) {
					if(rs.rows.length==1){
						$('#itemName').html(rs.rows.item(0)["name"]);
						$('#itemDescription').html(rs.rows.item(0)["description"]);
					}
						

				}, function(tx, err) {
				});
			});

		},
		
		
		/*
		 * Gets the item and question from the local DB and displays it
		 * used by answerquestion.html
		 * TODO: needs better error handling
		 *///FOX
		getDataAnswerPage: function(itemID,questionID){

			this.db.readTransaction(function(tx) {
				tx.executeSql("SELECT * FROM items WHERE id=?", [itemID], function(tx, rs) {
					if(rs.rows.length==1){
						$('#itemName').html(rs.rows.item(0)["name"]);
					}
					
				}, 
				function(tx, err) {
				});
				tx.executeSql("SELECT * FROM questions WHERE id=?", [questionID], function(tx, rs) {
					if(rs.rows.length==1){
						$('#itemQuestion').html(rs.rows.item(0)["content"]);
					}
						
				}, 
				function(tx, err) {
				});
			});

		},
		
		
		/*
		 * Gets the item data for the comment page
		 * used by commentitem.html
		 * TODO: needs better error handling
		 *///FOX
		getDataCommentPage: function(itemID){

			this.db.readTransaction(function(tx) {
				tx.executeSql("SELECT * FROM items WHERE id=?", [itemID], function(tx, rs) {
					if(rs.rows.length==1){
						$('#itemName').html(rs.rows.item(0)["name"]);
						$('#itemDescription').html(rs.rows.item(0)["description"]);
						database.getSellerCommentPage(rs.rows.item(0)["sellerID"]);
					}
					
				}, 
				function(tx, err) {
				});
				
			});

		},
		
		
		/*
		 * Gets the seller data for the comment page
		 * used by commentitem.html and called from database.getCommentPage
		 * TODO: needs better error handling
		 *///FOX
		getSellerCommentPage: function(sellerID){

			this.db.readTransaction(function(tx) {
				tx.executeSql("SELECT * FROM sellers WHERE id=?", [seller], function(tx, rs) {
					if(rs.rows.length==1){
						$('#sellerName').html("<a href=\"/viewuser?useHTML5=1&viewUserID=" + rs.rows.item(0)["id"]+"\">"+rs.rows.item(0)["username"]+"</a>");
					}
					
				}, 
				function(tx, err) {
				});
				
			});

		},

		updateItemPrice:function(id){
			// FOX
			// Bo's code edited - probably needs better error handlnig
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

			http_request.onreadystatechange = database.handleStateChangeItemPrice;

			//var itemIsNew = document.getElementById("itemIsOld").value;
			// Only send get itemCurrentBid request if the item is not old
			if(true){
				var url = "viewitem?&itemID=" + id + "&itemCurrentBid=1"; 
				http_request.open('GET', url, true);
				http_request.send(null);
				// Send get itemCurrentBid request every 5 seconds
				setTimeout("database.updateItemPrice("+id+")", 5000);
			}
		},

		handleStateChangeItemPrice:function() {
			if (http_request.readyState == 4) {
				if (http_request.status == 0 || http_request.status == 200) {
					var result = http_request.responseText;
					// For debug use, enable: alert("Current Price: " + result);
					//alert("Current Price: " + result);
					document.getElementById("currentBid").innerHTML = result;
					document.getElementById("currentBid2").innerHTML = result;
					document.getElementById("currentBid3").innerHTML = result;
				}
				else {
					//http_request.status != 200
					document.getElementById("currentBid").innerHTML = "cannot get.";
					document.getElementById("currentBid2").innerHTML = "cannot get.";	
					document.getElementById("currentBid3").innerHTML = "cannot get.";	
				}
			}
		},

		/*
		 * Insert a set of items in to the database
		 */
		insertItems: function(items){

			if(!isEmpty(items)){


				// Make the image URLs a comma sperated list


				var ts = new Date().getTime();
				var localUserID = localStorage['userID'];

				// We'll do an update/insert, as we don't know if the item already exists
				var updateSql = "UPDATE items SET name=?, description=?, quantity=?, myaccount=?, startPrice=?," +
				"reserve=?, buyNowPrice=?, currentBid=?, noOfBids=?, startDate=?, endDate=?, sellerID=?," +
				"categoryID=?, images=?, thumbnail=?, ts=? WHERE id=?";

				var insertSql = "INSERT OR IGNORE INTO items (id, myaccount, name, description, quantity, startPrice," +
				"reserve, buyNowPrice, currentBid, noOfBids, startDate, endDate, sellerID, sellerName, categoryID, "+
				"images, thumbnail, ts) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

				this.db.transaction(function(tx) {
					// Loop through and insert all of the items
					for(var i=0; i<items.length; i++){
						// See if we should mark it for myAccount
						var myAccount = 0;
						if(items[i].sellerID == localUserID) myAccount=1;

						// Make the image list an array
						var imageList = new StringBuffer();
						for(var j=0; j<items[i].images.length; j++){
							imageList.append(items[i].images[j].url);
							if(j<(items[i].images.length-1))
								imageList.append(",");
						}

						tx.executeSql(updateSql, [items[i].name,items[i].description,items[i].quantity,myAccount,items[i].startPrice,items[i].reserve,items[i].buyNowPrice,items[i].currentBid,items[i].noOfBids,items[i].startDate,items[i].endDate,items[i].sellerID,items[i].categoryID,imageList.toString(),items[i].thumbnail,ts,items[i].id],
								function (tx, rs) {},
								function (tx, err) {
									if(debug) $('#scriptError').append("<br />insertItem: update " +err.message + " " + new Date().getTime());
								}
						);
						tx.executeSql(insertSql,[items[i].id,myAccount,items[i].name,items[i].description,items[i].quantity,items[i].startPrice,items[i].reserve,items[i].buyNowPrice,items[i].currentBid,items[i].noOfBids,items[i].startDate,items[i].endDate,items[i].sellerID,items[i].sellerName,items[i].categoryID,imageList.toString(),items[i].thumbnail,ts],
								function (tx, rs) {},
								function (tx, err) {
									if(debug) $('#scriptError').append("<br />insertItem: insert " +err.message + " " + new Date().getTime());
								}
						);
					}
				});
			}
		},


		/*
		 * Inserts an item into the database
		 * used by upadteItemAndSeller
		 *///ANDY
		insertItem: function(id,name,description,quantity,startPrice,reserve,buyNowPrice,currentBid,noOfBids,startDate,endDate,sellerID,sellerName,categoryID,images,thumbnail){
			if(isEmpty(categoryID)){	
				categoryID=0;
			}

			var myAccount = 0;
			if(sellerID == localStorage['userID']) myAccount=1;

			// Make the image URLs a comma sperated list
			var imageList = new StringBuffer();
			for(var j=0; j<images.length; j++){
				imageList.append(images[j].url);
				if(j<(images.length-1))
					imageList.append(",");
			}

			var ts = new Date().getTime();

			// We'll do an update/insert, as we don't know if the item already exists
			var updateSql = "UPDATE items SET name=?, description=?, quantity=?, myaccount=?, startPrice=?," +
			"reserve=?, buyNowPrice=?, currentBid=?, noOfBids=?, startDate=?, endDate=?, sellerID=?," +
			"categoryID=?, images=?, thumbnail=?, ts=? WHERE id=?";

			var insertSql = "INSERT OR IGNORE INTO items (id, myaccount, name, description, quantity, startPrice," +
			"reserve, buyNowPrice, currentBid, noOfBids, startDate, endDate, sellerID, sellerName, categoryID, "+
			"images, thumbnail, ts) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			
			

			this.db.transaction(function(tx) {
				tx.executeSql(updateSql, [name,description,quantity,myAccount,startPrice,reserve,buyNowPrice,currentBid,noOfBids,startDate,endDate,sellerID,categoryID,imageList,thumbnail,ts,id],
						function (tx, rs) {},
						function (tx, err) {
							if(debug) $('#scriptError').append("<br />insertItem: update " +err.message + " " + new Date().getTime());
						}
				);
				tx.executeSql(insertSql,[id,myAccount,name,description,quantity,startPrice,reserve,buyNowPrice,currentBid,noOfBids,startDate,endDate,sellerID,sellerName,categoryID,imageList,thumbnail,ts],
						function (tx, rs) {},
						function (tx, err) {
							if(debug) $('#scriptError').append("<br />insertItem: insert " +err.message + " " + new Date().getTime());
						}
				);
			});
		},


		// John's version delete
		insertitem: function(id,itemnumber,name,description,quantity,startPrice,reserve,buyNowPrice,currentBid,noOfBids,startDate,endDate,sellerID,sellerName,categoryID,images,thumbnail){
			if(isEmpty(categoryID)){	
				categoryID=0;
			}

			var myAccount = 0;
			if(sellerID == localStorage['userID']) myAccount=1;

			// Make the image URLs in to the correct format
			var tempimages = images;
			images="";
			for(i=0;i<tempimages.length;i++){
				j=i+1;
				images=images+"<label for=\"image"+j+"\"><img src=\"..//img/"+tempimages[i].url+"\" width=\"300\" alt=\""+tempimages[i].description+"\"></label>";
			}
			images=encodeURI(images);

			var ts = new Date().getTime();

			// If the item number is not passed then it is a view item page
			if(itemnumber==null){	
				this.db.transaction(function(tx) {
					tx.executeSql("UPDATE items SET name=\""+name+"\",description=\""+description+"\",quantity="+quantity+",myaccount="+myAccount+",startPrice="+startPrice+",reserve=\""+reserve + "\",buyNowPrice="+buyNowPrice+",currentBid="+currentBid+",noOfBids="+noOfBids+",startDate=\""+startDate+"\",endDate=\""+endDate +"\",sellerID="+sellerID+",categoryID="+categoryID+",images=\""+images+"\",thumbnail=\""+thumbnail+"\", ts=" + ts +" WHERE id="+id,[],
							function (tx, rs) {},
							function (tx, err) {alert(err.message);}
					);
					tx.executeSql("INSERT OR IGNORE INTO items (id,itemnumber,myaccount,name,description,quantity,startPrice,reserve,buyNowPrice,currentBid,noOfBids,startDate,endDate,sellerID,sellerName,categoryID,images,thumbnail, ts) VALUES ("+id+",0,"+myAccount+",\""+name+"\",\""+description+"\","+quantity+","+startPrice+",\""+reserve+"\"" +
							","+buyNowPrice+","+currentBid+","+noOfBids+",\""+startDate+"\",\""+endDate+"\","+sellerID+",\""+sellerName+"\","+categoryID+",\""+images+"\",\""+thumbnail+"\",\""+ts +"\")",[],
							function (tx, rs) {},
							function (tx, err) {alert(err.message);}
					);
				});
				// Otherwise we insert the item with its position
			}else{
				this.db.transaction(function(tx) {
					tx.executeSql("UPDATE items SET name=\""+name+"\",itemnumber="+itemnumber+",description=\""+description+"\",quantity="+quantity+",myaccount="+myAccount+",startPrice="+startPrice+",reserve=\""+reserve + "\",buyNowPrice="+buyNowPrice+",currentBid="+currentBid+",noOfBids="+noOfBids+",startDate=\""+startDate+"\",endDate=\""+endDate +"\",sellerID="+sellerID+",categoryID="+categoryID+",images=\""+images+"\",thumbnail=\""+thumbnail+"\", ts="+ ts +" WHERE id="+id,[],
							function (tx, rs) {},
							function (tx, err) {alert(err.message);}
					);
					tx.executeSql("INSERT OR IGNORE INTO items (id,itemnumber,myaccount,name,description,quantity,startPrice,reserve,buyNowPrice,currentBid,noOfBids,startDate,endDate,sellerID,sellerName,categoryID,images,thumbnail, ts) VALUES ("+id+","+itemnumber+","+myAccount+",\""+name+"\",\""+description+"\","+quantity+","+startPrice+",\""+reserve+"\"" +
							","+buyNowPrice+","+currentBid+","+noOfBids+",\""+startDate+"\",\""+endDate+"\","+sellerID+",\""+sellerName+"\","+categoryID+",\""+images+"\",\""+thumbnail+"\",\""+ts +"\")",[],
							function (tx, rs) {},
							function (tx, err) {alert(err.message);}
					);

				});
			}
		},

		updateitem: function(id,quantity,startPrice,reserve,buyNowPrice,currentBid,noOfBids,redirect){
			this.db.transaction(function(tx) {
				tx.executeSql("UPDATE items SET quantity="+quantity+",startPrice="+startPrice+",reserve=\""+reserve + "\",buyNowPrice="+buyNowPrice+",currentBid="+currentBid+",noOfBids="+noOfBids+" WHERE id="+id,[],
						function (tx, rs) {
					if(redirect==true){
						window.location.href="./confirmbid.html?itemID="+id;
					}else{
						//window.location.reload();
						//alert("Bid failed");
					}
				},
				function (tx, err) {alert(err.message);}
				);


			});
		},

		/*
		 * Gets the users default address from the local DB, if not present request it from the server
		 *///ANDY
		findaddress: function(userid){
			this.db.transaction(function(tx) {
				tx.executeSql("SELECT * FROM addresses WHERE userid =? AND isDefault='true'", [userid],
						function (tx, rs) {
					if(rs.rows.length>0){
						populateBuyConfirmation(rs.rows.item(0)["id"],rs.rows.item(0)["street"],rs.rows.item(0)["town"],rs.rows.item(0)["zip"],rs.rows.item(0)["state"]);
					}else{
						downloadAddress();
					}
				},function (tx, err) {
					alert("findaddress :" + err);
				});
			});
		},

		/*
		 * Insert the user's address in to the local database
		 * used by downloadAddress()<-finaddress<-buyitem<-viewitem.html
		 *///ANDY
		insertaddress: function(addressid,userid,street,town,zip,state,isdefault,callback){
			//alert("inserting address");
			this.db.transaction(function(tx) {
				var updateSql = "UPDATE addresses SET userID=?,street=?,town=?,zip=?,state=?,isDefault=? WHERE id=?";
				var insertSql = "INSERT OR IGNORE INTO addresses (id,userID,street,town,zip,state,isDefault) VALUES (?,?,?,?,?,?,?)";

				tx.executeSql(updateSql,[userid, street, town, zip, state, isdefault, addressid],
						function (tx, rs) {},
						function (tx, err) {alert(err.message);}
				);
				tx.executeSql(insertSql,[addressid, userid, street, town, zip, state, isdefault],
						function (tx, rs) {
					if(!isEmpty(callback))
						callback(addressid, street, town, zip, state);
				},
				function (tx, err) {alert(err.message);}
				);

			});
		},

		/*
		 * seems pointless to me
		 * Called by browseCategory
		 */

		syncInfo: function(categoryID,pageNo,itemsPP){
			var currenttime=new Date();
			database.getItemInfo(categoryID,pageNo,itemsPP,currenttime);

			//TODO: only use item is timestamp is new(ish)
			// Get the most recent items we have for the category the user is looking at
			/*this.db.transaction(function(tx) {
			tx.executeSql("SELECT * FROM itemcache WHERE categoryid=? AND items>=? AND items<? ORDER BY items",[categoryID,parseInt(pageNo)*parseInt(itemsPP),parseInt(pageNo+1)*parseInt(itemsPP)],
				function (tx, rs) {

				//var timestamp=new Date(rs.rows.item(0)["ts"]);
				var currenttime=new Date();
				database.getItemInfo(categoryID,pageNo,itemsPP,currenttime);

				/*var currenttime=new Date();
					if(rs.rows.length>0){
						var timestamp=new Date(rs.rows.item(0)["sync"]);
						if(currenttime.getTime()-timestamp.getTime()<30){
							if(parseInt(rs.rows.item(0)["items"])==itemsPP*(pageNo)){
								if(parseInt(rs.rows.item(0)["range"])<=itemsPP){
								database.loadCategories(categoryID);
								database.browseItems(categoryID, pageNo, itemsPP);
								}else{
									database.getItemInfo(categoryID,pageNo,itemsPP,currenttime);
								}
							}else{
								database.getItemInfo(categoryID,pageNo,itemsPP,currenttime);
							}

						}else{
							database.getItemInfo(categoryID,pageNo,itemsPP,currenttime);
						}
					}else{
						database.getItemInfo(categoryID,pageNo,itemsPP,currenttime);
					}*
				},
				function (tx, err) {alert(err.message);}
				);
		});*/
		},

		/*
		 * Used by browse page to get which pages should be displayed
		 * used by browse.html<-syncInfo<-
		 */
		getItemInfo: function(categoryID,pageNo,itemsPP,timestamp) {
			// TODO: need to get the correct category ts
			var ts = new Date().getTime();
			var itemIDs = new StringBuffer();

			// If the categoryID is 0 then we will do all categories
			var catStr = "";
			if(!isEmpty(categoryID) && categoryID != 0) catStr = "categoryID="+categoryID+" AND";

			this.db.transaction(function(tx) {
				// Get the item IDs we want to send back to the server
				tx.executeSql("SELECT id FROM items WHERE "+catStr+" ts>? ORDER BY ts DESC LIMIT 250 ", [(ts-GV.freshTime)], function(tx, rs) {
					// Get all of the up-to-date IDs we have
					if(rs.rows.length > 0) {
						for(var i = 0; i < rs.rows.length; i++) {
							if(i > 0)
								itemIDs.append(",");
							itemIDs.append(rs.rows.item(i)["id"]);
						}

					}

					// Get the seller IDs we want to send back to the server

					// Now send the request for updated info, i.e. items we don't have that we need
					$.ajax({
						async : false,
						url : $.urldomain() + "/browsecategory?useHTML5=1&categoryID=" + categoryID + "&catTs=" + ts + "&pageNo=" + pageNo + "&itemsPP=" + itemsPP + "&userID=" + localStorage["userID"] +"&authToken=" + localStorage["authToken"]+ "&hasItems=" + itemIDs.toString(),
						type : "GET",
						dataType : "json",
						success : function(data) {
							//TODO: this should really all be donw with callback functions
							// Insert the new categories 
							if(!isEmpty(data.categories))
								database.insertCategories(data.categories);		

							// Insert items
							if(debug) $('#scriptDebug').append("<br />getItemInfo inserting items" + new Date().getTime());

							if(!isEmpty(data.items))
								database.insertItems(data.items);

							// Display the categories on the side bar
							database.loadCategories(categoryID);

							// Display the items in the order the server says
							if(!isEmpty(data.order))
								database.displayItems(data.order);

							// Display the page errors
							printErrorsAndStats(data);

							// Insert and update the sellers related to the items
							if(debug) $('#scriptDebug').append("<br />getItemInfo inserting sellers" + new Date().getTime());

							if(!isEmpty(data.sellers))
								database.insertSellers(data.sellers);
							
							// Insert and update the questions related to the items
							if(debug) $('#scriptDebug').append("<br />getItemInfo inserting questions" + new Date().getTime());

							if(!isEmpty(data.questions))
								database.insertQuestions(data.questions);
							
							// Insert and update the comments related to the items
							if(debug) $('#scriptDebug').append("<br />getItemInfo inserting comments" + new Date().getTime());

							if(!isEmpty(data.comments))
								database.insertComments(data.comments);
							
							// Prefetches images for next page
							if(!isEmpty(data.prefetch))
								database.prefetchCommand(data.prefetch);



							// Update categories timestamps that have the current parent as the must be up-to-date
							// otherwise they would have been returned in the requets


							//TODO: read categories from DB
							//TODO: update category times with new TS
							//TODO: update items with new TS


						},
						error : function(jqXHR, textStatus, errorThrown) {
							if(debug) $('#scriptDebug').append("<br />getItemInfo error " + textStatus + new Date().getTime())
						}
					});

				}, function(tx, err) {
					alert(err.message);
				});
			});
		},

		/*browseItemsArrayLink: function(items){
		browseItemsArray(items);
	},*/

		/*
		 * Used to load the categories for the browse and search pages
		 */ 


		loadCategories: function(parentID) {
			this.db.transaction(function(tx) {
				tx.executeSql("SELECT * FROM categories WHERE parent=" + parentID, [], function(tx, rs) {
					/*var categories="<div class=\"cats\">";
				 if(rs.rows.length>0){
				 for(var i=0;i<rs.rows.length;i=i+1){
				 categories=categories+"<div class=\"row\"><a href=\"browse.html?categoryID="+rs.rows.item(i)["id"]+"&pageNo=0&itemsPP="+GV.itemsPP+"\" >"+decodeURIComponent(rs.rows.item(i)["name"])+"</a></div>";
				 }
				 }
				 categories=categories+"</div>";
				 $("#category_content").html(categories);*/
					var categories = new StringBuffer();
					categories.append("<div class=\"three columns\">");
					//categories.append("<div class=\"cats\">");
					categories.append("<ul STYLE=\"list-style-image: url(images/orbit/bullets.jpg)\">");

					if(rs.rows.length > 0) {
						for(var i = 0; i < rs.rows.length; i = i + 1) {
							var cat = new StringBuffer();
							cat.append("<li><a href=\"browse.html?categoryID=");
							cat.append(rs.rows.item(i)["id"]);
							cat.append("&pageNo=0&itemsPP=");
							cat.append(GV.itemsPP);
							cat.append("\" >");
							cat.append(decodeURIComponent(rs.rows.item(i)["name"]));
							cat.append("</a></li>");
							categories.append(cat);
						}
					}
					categories.append("</ul></div>");
					$("#category_content").html(categories.toString());

				}, function(tx, err) {
					alert(err.message);
				});
			});
		},

		/*
		 * Gets the questions associated with an itemID for the item page
		 * FOX
		 */
		getQuestionsItemPage: function(itemID) {
			this.db.transaction(function(tx) {
				tx.executeSql("SELECT * FROM questions WHERE itemID=" + itemID, [], function(tx, rs) {

					var qtable = new StringBuffer();
					$("#questionLink").html("<a href=\"./askquestion.html?itemID="+itemID+"\">Ask Question</a> <BR />");
					if(rs.rows.length > 0) {
						qtable.append("<table><thead><tr><th colspan=\"1\">");
						qtable.append("Previous questions:");
						qtable.append("</th></tr></thead><tbody>");
						for(var i = 0; i < rs.rows.length; i = i + 1) {
							qtable.append("<tr><td><div id=\"qtable"+i+"\">");
							qtable.append("</div></td></tr>");
						}
						qtable.append("</table>");
						$("#questionTable").html(qtable.toString());
						for(var i = 0; i < rs.rows.length; i = i + 1) {
							var question=new Object();
							question.id=rs.rows.item(i)["id"];
							question.fromUserID=rs.rows.item(i)["fromUserID"];
							question.toUserID=rs.rows.item(i)["toUserID"];
							question.itemID=rs.rows.item(i)["itemID"];
							question.isQuestion=rs.rows.item(i)["isQuestion"];
							question.postDate=rs.rows.item(i)["postDate"];
							question.responseTo=rs.rows.item(i)["responseTo"];
							question.content=rs.rows.item(i)["content"];
							database.getsellernameQuestion(question,i);
				
						}
					}

				}, function(tx, err) {
					alert(err.message);
				});
			});
		},
		
		
		/*
		 * Gets the comments associated with an itemID for the item page
		 * FOX
		 */
		getCommentsItemPage: function(itemID) {
			this.db.transaction(function(tx) {
				tx.executeSql("SELECT * FROM comments WHERE itemID=" + itemID, [], function(tx, rs) {

					var qtable = new StringBuffer();
					$("#commentLink").html("<a href=\"./commentitem.html?itemID="+itemID+"\">Leave Comment</a> <BR />");
					if(rs.rows.length > 0) {
						qtable.append("<table><thead><tr><th colspan=\"1\">");
						qtable.append("Previous Comments:");
						qtable.append("</th></tr></thead><tbody>");
						for(var i = 0; i < rs.rows.length; i = i + 1) {
							qtable.append("<tr><td><div id=\"ctable"+i+"\">");
							qtable.append("</div></td></tr>");
						}
						qtable.append("</table>");
						$("#commentTable").html(qtable.toString());
						for(var i = 0; i < rs.rows.length; i = i + 1) {
							var comment=new Object();
							comment.id=rs.rows.item(i)["id"];
							comment.fromUserID=rs.rows.item(i)["fromUserID"];
							comment.toUserID=rs.rows.item(i)["toUserID"];
							comment.itemID=rs.rows.item(i)["itemID"];
							comment.rating=rs.rows.item(i)["rating"];
							comment.date=rs.rows.item(i)["date"];
							comment.comment=rs.rows.item(i)["comment"];
							database.getsellernameComment(comment,i);
				
						}
					}

				}, function(tx, err) {
					alert(err.message);
				});
			});
		},
		/*displayItemsAddRow: function(items, row){

	},

	diaplayItemsAddToPage: function(items){

	},*/

		/*
		 * Displays the items that are needed on the browse or search page
		 * used by browse.html
		 */
		displayItems: function(itemIDs) {
			var items = new StringBuffer();

			this.db.transaction(function(tx) {
				selectSql = "SELECT * FROM items WHERE id=?";

				// Add the table to the page
				items.append("<div class=\"nine columns\">");
				items.append("<table>");
				items.append("<div class=\"items\"><thead><tr><th><div class=\"title\"><div class=\"img\">Name</div></th><th><div class=\"desc\">Description</div></th><th><div class=\"bid\"><img src=\"images/arrow_down.png\" alt=\"sort by price\" width=\"15\"/>Bid Price</div></th><th><div class=\"endDate\"><img src=\"images/arrow_down.png\" alt=\"sort by end date\" width=\"15\"/>End date</div></th></tr></thead></div>");
				items.append("<tbody id=\"items_body\">");

				if(itemIDs.length == 0) {
					items.append("<tr id=\"entry\">");
					items.append("<td colspan=4>");
					items.append("<BR /> Sorry, there are not items to view");
					items.append("</td>");
					items.append("</tr>");
				}

				items.append("</tbody></table></div></div>");			
				$("#body_content").html(items.toString());

				// Loop through each item and add it
				for(var i = 0; i < itemIDs.length; i++) {
					tx.executeSql(selectSql, [itemIDs[i]], function(tx, rs) {
						if(rs.rows.length >=1){

							var row = new StringBuffer();
							row.append("<tr><div class=\"entry\" id=\"entry\">");
							row.append("<td><div class=\"img\"><img height=\"80\" width=\"80\" src=\"../img/");
							row.append(rs.rows.item(0)["thumbnail"]);
							row.append("\" alt=\"");
							row.append(rs.rows.item(0)["description"]);
							row.append("\" /></div></td>");
							row.append("<td><div class=\"desc\"><label for=\"itemlink");
							row.append(rs.rows.item(0)["id"]);
							row.append("\"><a href=viewitem.html?itemID=");
							row.append(rs.rows.item(0)["id"]);
							row.append(" >");
							row.append(rs.rows.item(0)["name"]);
							row.append("</a></label></div></td>");
							row.append("<td><div class=\"bid\"><label for=\"itemBid");
							row.append(rs.rows.item(0)["id"]);
							row.append("\">$");
							row.append(parseFloat(rs.rows.item(0)["currentBid"]).toFixed(2));
							row.append("</label></div></td>");
							row.append("<td><div class=\"endDate\"><label for=\"itemEndDate");
							row.append(rs.rows.item(0)["id"]);
							row.append("\">");
							row.append(rs.rows.item(0)["endDate"]);
							row.append("</label></div></td>");
							row.append("</div></tr>");

							$('#items_body').append(row.toString());
						}
					}, function(tx, err) {
						alert("in syncitems " + err.message);
					});
				}
			});	
		},

		// Do not use this. delete
		browseItemsArray: function(categoryID,pageNo,itemsPP) {
			this.db.transaction(function(tx) {
				var SQL = "";
				if(categoryID == 0) {
					SQL = "SELECT * FROM items WHERE itemnumber >= " + parseInt(pageNo) * parseInt(itemsPP) + " AND itemnumber< " + parseInt(pageNo + 1) * parseInt(itemsPP);
				} else {
					SQL = "SELECT * FROM items WHERE categoryID=" + categoryID + " AND itemnumber >= " + parseInt(pageNo) * parseInt(itemsPP) + " AND itemnumber< " + parseInt(pageNo + 1) * parseInt(itemsPP);
				}
				//TODO: must sort!!!!
				//TODO: must sort!!!!
				//TODO: must sort!!!!


				tx.executeSql(SQL, [], function(tx, rs) {

					var items = new StringBuffer();
					items.append("<div class=\"nine columns\">");
					items.append("<table>");
					items.append("<div class=\"items\"><thead><tr><th><div class=\"title\"><div class=\"img\">Name</div></th><th><div class=\"desc\">Description</div></th><th><div class=\"bid\"><img src=\"images/arrow_down.png\" alt=\"sort by price\" width=\"15\"/>Bid Price</div></th><th><div class=\"endDate\"><img src=\"images/arrow_down.png\" alt=\"sort by end date\" width=\"15\"/>End date</div></th></tr></thead></div>");
					items.append("<tbody>");

					if(rs.rows.length > 0) {
						for(var i = 0; i < Math.min(parseInt(itemsPP), rs.rows.length); i = i + 1) {
							var row = new StringBuffer();
							row.append("<tr><div class=\"entry\" id=\"entry\">");
							row.append("<td><div class=\"img\"><img height=\"80\" width=\"80\" src=\"../img/");
							row.append(rs.rows.item(i)["thumbnail"]);
							row.append("\" alt=\"");
							row.append(rs.rows.item(i)["description"]);
							row.append("\" /></div></td>");
							row.append("<td><div class=\"desc\"><label for=\"itemlink");
							row.append(rs.rows.item(i)["id"]);
							row.append("\"><a href=viewitem.html?itemID=");
							row.append(rs.rows.item(i)["id"]);
							row.append(" >");
							row.append(rs.rows.item(i)["name"]);
							row.append("</a></label></div></td>");
							row.append("<td><div class=\"bid\"><label for=\"itemBid");
							row.append(rs.rows.item(i)["id"]);
							row.append("\">$");
							row.append(parseFloat(rs.rows.item(i)["currentBid"]).toFixed(2));
							row.append("</label></div></td>");
							row.append("<td><div class=\"endDate\"><label for=\"itemEndDate");
							row.append(rs.rows.item(i)["id"]);
							row.append("\">");
							row.append(rs.rows.item(i)["endDate"]);
							row.append("</label></div></td>");
							row.append("</div></tr>");
							items.append(row);
						}
					}
					else{
						items.append("<tr id=\"entry\">");
						items.append("<td colspan=4>");
						items.append("<BR /> Sorry, there are not items to view");
						items.append("</td>");
						items.append("</tr>");
					}
					items.append("</tbody></table></div></div>");

					$("#body_content").html(items.toString());
					hideLoading();

				}, function(tx, err) {
					alert("in syncitems " + err.message);
				});
			});
		},

		
		//TODO: delete this. new version above
		browseItems: function(categoryID,pageNo,itemsPP){
			this.db.transaction(function(tx) {
				var SQL = "";
				if(categoryID==0){
					SQL = "SELECT * FROM items WHERE itemnumber >= "+parseInt(pageNo)*parseInt(itemsPP) + " AND itemnumber< " +parseInt(pageNo+1)*parseInt(itemsPP);
				}else{
					SQL= "SELECT * FROM items WHERE categoryID="+categoryID+" AND itemnumber >= "+parseInt(pageNo)*parseInt(itemsPP) + " AND itemnumber< " +parseInt(pageNo+1)*parseInt(itemsPP);
				}
				tx.executeSql(SQL,[],
						function (tx, rs) {

					var items= new StringBuffer();
					items.append("<div class=\"items\"><div class=\"title\"><div class=\"img\">Name</div><div class=\"desc\">Description</div><div class=\"bid\">Bid Price</div><div class=\"endDate\">End date</div></div>");
					if(rs.rows.length>0){
						for(var i=0;i<Math.min(parseInt(itemsPP),rs.rows.length);i=i+1){
							items.append("<div class=\"entry\" id=\"entry\">");
							items.append("<div class=\"img\"><img height=\"80\" width=\"80\" src=\"../img/");
							items.append(rs.rows.item(i)["thumbnail"]);
							items.append("\" alt=\"");
							items.append(rs.rows.item(i)["description"]);
							items.append("\" /></div>");
							items.append("<div class=\"desc\"><label for=\"itemlink");
							items.append(rs.rows.item(i)["id"]);
							items.append("\"><a href=viewitem.html?itemID=");
							items.append(rs.rows.item(i)["id"]);
							items.append(" >");
							items.append(rs.rows.item(i)["name"]);
							items.append("</a></label></div>");
							items.append("<div class=\"bid\"><label for=\"itemBid");
							items.append(rs.rows.item(i)["id"]);
							items.append("\">$");
							items.append(parseFloat(rs.rows.item(i)["currentBid"]).toFixed(2));
							items.append("</label></div>");
							items.append("<div class=\"endDate\"><label for=\"itemEndDate");
							items.append(rs.rows.item(i)["id"]);
							items.append("\">");
							items.append(rs.rows.item(i)["endDate"]);
							items.append("</label></div>");
							items.append("</div>");
						}
					}
					items.append("</div>");
					$("#body_content").html(items.toString());
					hideLoading();

				},
				function (tx, err) {alert(err.message);}
				);
			});
		},

		bidToPurchase: function (id){
			this.db.transaction(function(tx) {
				tx.executeSql("DELETE FROM bids WHERE id="+id);
			});
		},
		
		
		/*
		 * prefetches item images for next page
		 * used by browse.html
		 */
		prefetchCommand: function(itemIDs) {
			if(itemIDs.length>0){
				this.db.transaction(function(tx) {
					selectSql = "SELECT * FROM items WHERE id=?";

					var totalImg=0;
					// Loop through each item and add it
					for(var i = 0; i < itemIDs.length; i++) {
						tx.executeSql(selectSql, [itemIDs[i]], function(tx, rs) {
							if(rs.rows.length >=1){
								var images=new Array();
								var imageLine=rs.rows.item(0)["images"];
								var k=0;
								if(imageLine.length>0){
									var imageURLs = new Array();
									do{
										var end=imageLine.length;
										if(imageLine.indexOf(",")!=-1)
											end=imageLine.indexOf(",");
										images[k]=decodeURI(imageLine.substring(0,end));
										if(end==imageLine.indexOf(","))
											imageLine=imageLine.slice(end+1,imageLine.length);
										else
											imageLine="";
										k++;
									}while(imageLine.length>0);
									for(var j=0;j<images.length;j++){
										imageURLs[totalImg]=("..//img/"+images[j]);
										totalImg++;
									}
									preloadImages(imageURLs);
								}

							}
						}, function(tx, err) {
							alert("in syncitems " + err.message);
						});
					}

				});	

			}
		},
		
		
		/*
		 * Deletes the data from the database. This happens after a user logs out as they
		 * will probably not use the site soon afterwards, so we should refresh the data anyway
		 */
		clearDatabase: function (){
			this.db.transaction(function(tx) {
				tx.executeSql("DELETE FROM users");
				tx.executeSql("DELETE FROM bids");
				tx.executeSql("DELETE FROM purchases");
				tx.executeSql("DELETE FROM categories");
				tx.executeSql("DELETE FROM addresses");
				tx.executeSql("DELETE FROM states");
				tx.executeSql("DELETE FROM items");
				tx.executeSql("DELETE FROM categories");
				tx.executeSql("DELETE FROM itemcache");
				tx.executeSql("DELETE FROM sellers");
				ts.executeSql("DELETE FROM questions");
				ts.executeSql("DELETE FROM comments");
			});
		},

		/*
		 * Insert an array of sellers in to the database
		 * used by myaccount(userAccount)
		 *///ANDY
		insertSellers: function(sellers) {
			var ts = new Date().getTime();

			if(debug) $('#scriptDebug').append("<br />insertSellers got time" + new Date().getTime());

			if(!isEmpty(sellers))
				database.db.transaction(function(tx) {
					if(debug) $('#scriptDebug').append("<br />insertSellers running updates" + new Date().getTime());

					// Update users already present
					var updateSql = "UPDATE items SET username=?, rating=?, ts=? WHERE id=?";
					for(var i = 0; i < sellers.length; i = i + 1) {

						tx.executeSql(updateSql, [sellers[i].name, sellers[i].rating, ts, sellers[i].id], function(tx, rs) {
						}, function(tx, err) {
							if(debug) $('#scriptDebug').append("<br />insertSellers update" + err.message + " " + new Date().getTime());
						});
					};

					if(debug) $('#scriptDebug').append("<br />insertSellers inserting new" + new Date().getTime());

					// Insert new users
					var sql = "INSERT OR IGNORE INTO sellers (id,username,rating,ts) VALUES (?,?,?,?)";
					for(var i = 0; i < sellers.length; i = i + 1) {

						tx.executeSql(sql, [sellers[i].id, sellers[i].name, sellers[i].rating, ts], function(tx, rs) {
						}, function(tx, err) {
							if(debug) $('#scriptDebug').append("<br />insertSellers insert " + err.message + " " + new Date().getTime());
						});
					};
				});
		},


		/*
		 * Insert a seller
		 *///ANDY

		insertSeller: function(seller) {
			var ts = new Date().getTime();

			database.db.transaction(function(tx) {
				var updateSql = "UPDATE sellers SET username=?, rating=?, ts=? WHERE id=?";

				tx.executeSql(updateSql, [seller.name, seller.rating, ts, seller.id], function(tx, rs) {
				}, function(tx, err) {
					$('#scriptError').append("<br />insertSeller update error " + err.message + " " + new Date().getTime());
				});
				
				var sql = "INSERT OR IGNORE INTO sellers (id,username,rating,ts) VALUES (?,?,?,?)";

				tx.executeSql(sql, [seller.id, seller.name, seller.rating, ts], function(tx, rs) {
				}, function(tx, err) {
					$('#scriptError').append("<br />insertSeller insert error " + err.message + " " + new Date().getTime());
					//alert(tx + " " + err.toString + " " + err.message);
				});
			});
		},




		//insertsellers: function(sellers){

		//alert(ok);
		/*this.db.transaction(function(tx) {
			tx.executeSql("UPDATE sellers SET username=\""+sellers[1].name+"\", rating=\""+sellers[1].rating+"\"  WHERE id="+sellers[1].id+"\"",[],
					function (tx, rs) {},
					function (tx, err) {alert(err.message);}
					);
		}
		//this.db.transaction(function(tx) {
			//for(int i=0; i<sellers.length; i=i+1){
			/*tx.executeSql("UPDATE sellers SET username=\""+sellers[i].name+"\", rating=\""+sellers[i].rating+"\"  WHERE id="+sellers[i].id+"\"",[],
					function (tx, rs) {},
					function (tx, err) {alert(err.message);}
					);
			tx.executeSql("INSERT OR IGNORE INTO sellers (id, username, rating) VALUES ("+sellers[i].id+",\""+sellers[i].name+",\""+sellers[i].rating+"\")",[],
					function (tx, rs) {},
					function (tx, err) {alert(err.message);}
					);*/
		//}
		//});
		//},

		/*
		 * Gets a user from the local database. If it is not present download it
		 * called by viewUser
		 *///ANDY
		getseller: function(id){
			// Check the user is present and display the data
			database.db.transaction(function(tx) {
				tx.executeSql("SELECT id FROM sellers WHERE id=?", [id], function(tx, rs) {

					if(rs.rows.length == 0) {
						// If the user is not present download it
						$.ajax({
							async : false,
							url : $.urldomain() + "/viewuser?useHTML5=1&viewUserID=" + id,
							type : "GET",
							dataType : "json",
							success : function(data) {
								if(!isEmpty(data) && !isEmpty(data.seller)) {
									database.insertSeller(data.seller);
								}

								printErrorsAndStats(data);

							},
							error : function(jqXHR, textStatus, errorThrown) {
								var ts = new Date().getTime();
								localStorage.setItem("Error in updateItemAndSeller", "AJAX did not succeed " + ts + " " + textStatus);
							}
						});
					};

					// Now update the screen
					database.db.transaction(function(tx) {
						tx.executeSql("SELECT * FROM sellers WHERE id=?", [id], function(tx, rs) {
							if(rs.rows.length > 0) {
								$('#username').html(rs.rows.item(0)["username"]);
								$('#rating').html(rs.rows.item(0)["rating"]);
							};
						}, function(tx, err) {
						});
					});


				}, function(tx, err) {
				});
			});
			
		},
		
		/*
		 * Gets a user from the local database. If it is not present download it
		 * Used on item page to fill question table
		 *///FOX
		getsellernameQuestion: function(question,qtableIndex){
			// Check the user is present and display the data
			var username='test';
			database.db.transaction(function(tx) {
				tx.executeSql("SELECT * FROM sellers WHERE id=?", [question.fromUserID], function(tx, rs) {
					if(rs.rows.length == 0) {
						// If the user is not present download it
						$.ajax({
							async : false,
							url : $.urldomain() + "/viewuser?useHTML5=1&viewUserID=" + question.fromUserID,
							type : "GET",
							dataType : "json",
							success : function(data) {
								if(!isEmpty(data) && !isEmpty(data.seller)) {
									database.insertSeller(data.seller);
								}

								printErrorsAndStats(data);

							},
							error : function(jqXHR, textStatus, errorThrown) {
								var ts = new Date().getTime();
								localStorage.setItem("Error in updateItemAndSeller", "AJAX did not succeed " + ts + " " + textStatus);
							}
						});
					}
					else{
					username= rs.rows.item(0)["username"];
					var qtable=new StringBuffer();
					if(question.isQuestion=="true"||question.isQuestion==true){
						qtable.append("Question from user: ");
						qtable.append(username);
						qtable.append("; Date: ");
						qtable.append(question.postDate);
						qtable.append("<BR />");
						qtable.append(question.content);
						qtable.append("<BR />");
						if(question.toUserID==localStorage["userID"]){
							qtable.append("<a href=\"./answerquestion.html?questionID=").append(question.id).append("&itemID=").append(question.itemID).append("\">Answer Question</a><BR />");
						}
						$('#qtable'+qtableIndex).html(qtable.toString());
					}
					else{
						database.db.transaction(function(tx) {
							tx.executeSql("SELECT * FROM sellers WHERE id=?", [question.toUserID], function(tx, rs) {
								if(rs.rows.length > 0) {
									qtable.append("Answer from user: <a href=\"viewuser.html?viewUserID="+rs.rows.item(0)["id"]+"\">");
									qtable.append(rs.rows.item(0)["username"]);
									qtable.append("</a> ");
									qtable.append("for User: ");
									qtable.append(username);
									qtable.append(", Date: ");
									qtable.append(question.postDate);
									qtable.append("<BR />");
									qtable.append(question.content);
									qtable.append("<BR />");
									$('#qtable'+qtableIndex).html(qtable.toString());
								};
							}, function(tx, err) {
							});
						});
					}
					
					}

				}, function(tx, err) {
				});
			});
		},
		
		
		
		/*
		 * Gets a user from the local database. If it is not present download it
		 * Used on item page to fill comment table
		 *///FOX
		getsellernameComment: function(comment,ctableIndex){
			// Check the user is present and display the data
			var username='test';
			database.db.transaction(function(tx) {
				tx.executeSql("SELECT * FROM sellers WHERE id=?", [comment.fromUserID], function(tx, rs) {
					if(rs.rows.length == 0) {
						// If the user is not present download it
						$.ajax({
							async : false,
							url : $.urldomain() + "/viewuser?useHTML5=1&viewUserID=" + comment.fromUserID,
							type : "GET",
							dataType : "json",
							success : function(data) {
								if(!isEmpty(data) && !isEmpty(data.seller)) {
									database.insertSeller(data.seller);
								}

								printErrorsAndStats(data);

							},
							error : function(jqXHR, textStatus, errorThrown) {
								var ts = new Date().getTime();
								localStorage.setItem("Error in updateItemAndSeller", "AJAX did not succeed " + ts + " " + textStatus);
							}
						});
					}
					else{
					username= rs.rows.item(0)["username"];
					var ctable=new StringBuffer();
						ctable.append("Comment from user: ");
						ctable.append(username);
						ctable.append("; Date: ");
						ctable.append(comment.postDate);
						ctable.append("<BR />");
						ctable.append("Rating: ");
						ctable.append(comment.rating);
						ctable.append(", Comment: ");
						ctable.append(comment.comment);
						ctable.append("<BR />");
						$('#ctable'+ctableIndex).html(ctable.toString());
					
					}

				}, function(tx, err) {
				});
			});
		},
		
		/*
		 * Insert an array of questions in to the database
		 *///FOX
		insertQuestions: function(questions) {

			if(!isEmpty(questions))
				database.db.transaction(function(tx) {
					if(debug) $('#scriptDebug').append("<br />insertQuestions running updates " + new Date().getTime());

					// Update users already present
					var updateSql = "UPDATE questions SET fromUserID=?, toUserID=?, itemID=?, isQuestion=?,postDate=?,responseTo=?,content=? WHERE id=?";
					for(var i = 0; i < questions.length; i = i + 1) {

						tx.executeSql(updateSql, [questions[i].fromUserID,questions[i].toUserID,questions[i].itemID,questions[i].isQuestion,questions[i].postDate,questions[i].responseTo,questions[i].content,questions[i].id], function(tx, rs) {
						}, function(tx, err) {
							if(debug) $('#scriptDebug').append("<br />insertQuestions update " + err.message + " " + new Date().getTime());
						});
					};

					if(debug) $('#scriptDebug').append("<br />insertQuestions inserting new " + new Date().getTime());

					// Insert new users
					var sql = "INSERT OR IGNORE INTO questions (id,fromUserID,toUserID,itemID,isQuestion,postDate,responseTo,content) VALUES (?,?,?,?,?,?,?,?)";
					for(var i = 0; i < questions.length; i = i + 1) {

						tx.executeSql(sql, [questions[i].id, questions[i].fromUserID,questions[i].toUserID,questions[i].itemID,questions[i].isQuestion,questions[i].postDate,questions[i].responseTo,questions[i].content], function(tx, rs) {
						}, function(tx, err) {
							if(debug) $('#scriptDebug').append("<br />insertQuestions insert " + err.message + " " + new Date().getTime());
						});
					};
				});
		},
		
		/*
		 * Insert a question or an answer to a question in to the database
		 * Called from login.submitQuestion() and login.submitAnswer()
		 *///FOX
		insertQuestion: function(question,success) {

			if(!isEmpty(question))
				database.db.transaction(function(tx) {
					if(debug) $('#scriptDebug').append("<br />insertQuestion running updates " + new Date().getTime());


					// Insert new question
					var sql = "INSERT OR IGNORE INTO questions (id,fromUserID,toUserID,itemID,isQuestion,postDate,responseTo,content) VALUES (?,?,?,?,?,?,?,?)";


						tx.executeSql(sql, [question.id, question.fromUserID,question.toUserID,question.itemID,question.isQuestion,question.postDate,question.content], function(tx, rs) {
							if(!isEmpty(success) && success) {
								// Now tell the use if they were successful
								window.location.href = "./viewitem.html?itemID=" + question.itemID;
							} else {
								// fail, update screen and print errors
								database.getitem(question.itemID);
								if(!isEmpty(data))
									printErrorsAndStats(data);
							}
						}, function(tx, err) {
							if(debug) $('#scriptDebug').append("<br />insertQuestions insert " + err.message + " " + new Date().getTime());
						});

				});
		},
		
		
		/*
		 * Insert an array of questions in to the database
		 *///FOX
		insertComments: function(comments) {

			if(!isEmpty(comments))
				database.db.transaction(function(tx) {
					if(debug) $('#scriptDebug').append("<br />insertComments running updates " + new Date().getTime());

					// Update users already present
					var updateSql = "UPDATE comments SET fromUserID=?, toUserID=?, itemID=?, rating=?,date=?,comment=? WHERE id=?";
					for(var i = 0; i < comments.length; i = i + 1) {

						tx.executeSql(updateSql, [comments[i].fromUserID,comments[i].toUserID,comments[i].itemID,comments[i].rating,comments[i].date,comments[i].content,comments[i].id], function(tx, rs) {
						}, function(tx, err) {
							if(debug) $('#scriptDebug').append("<br />insertComments update " + err.message + " " + new Date().getTime());
						});
					};

					if(debug) $('#scriptDebug').append("<br />insertComments inserting new " + new Date().getTime());

					// Insert new users
					var sql = "INSERT OR IGNORE INTO comments (id,fromUserID,toUserID,itemID,rating,date,content) VALUES (?,?,?,?,?,?,?)";
					for(var i = 0; i < comments.length; i = i + 1) {

						tx.executeSql(sql, [comments[i].id, comments[i].fromUserID,comments[i].toUserID,comments[i].itemID,comments[i].rating,comments[i].date,comments[i].content], function(tx, rs) {
						}, function(tx, err) {
							if(debug) $('#scriptDebug').append("<br />insertComments insert " + err.message + " " + new Date().getTime());
						});
					};
				});
		},
		
		/*
		 * Insert a comment on an item into the database
		 * Called from login.submitComment()
		 *///FOX
		insertComment: function(comment,success) {

			if(!isEmpty(comment))
				database.db.transaction(function(tx) {
					if(debug) $('#scriptDebug').append("<br />insertComment running updates " + new Date().getTime());


					// Insert new question
					var sql = "INSERT OR IGNORE INTO comments (id,fromUserID,toUserID,itemID,rating,date,comment) VALUES (?,?,?,?,?,?,?)";


						tx.executeSql(sql, [comment.id, comment.fromUserID,comment.toUserID,comment.itemID,comment.rating,comment.date,comment.comment], function(tx, rs) {
							if(!isEmpty(success) && success) {
								// Now tell the use if they were successful
								window.location.href = "./confirmcomment.html";
							} else {
								// fail, update screen and print errors
								database.getitem(comment.itemID);
								if(!isEmpty(data))
									printErrorsAndStats(data);
							}
						}, function(tx, err) {
							if(debug) $('#scriptDebug').append("<br />insertComment insert " + err.message + " " + new Date().getTime());
						});

				});
		},
		
			

		syncInfoSearch: function(searchTerm,pageNo,itemsPP){
			$.ajax({
				async: false,
				url:$.urldomain()+"/search?useHTML5=1&searchTerm="+searchTerm+"&pageNo="+pageNo+"&itemsPP="+itemsPP+"&userID="+localStorage["userID"],
				type: "GET",
				dataType: "json",
				success:function(data){
					browseItemsArray(data.items);
					browseCategoriesArray(data.categories);
					//database.insertcategories(data.categories);
					for(var i=0;i<data.items.length;i=i+1){
						database.insertitem(data.items[i].id,pageNo*itemsPP+i,data.items[i].name,data.items[i].description,data.items[i].quantity,data.items[i].startPrice,data.items[i].reserve,data.items[i].buyNowPrice,data.items[i].currentBid,data.items[i].noOfBids,data.items[i].startDate,data.items[i].endDate,data.items[i].sellerID,data.items[i].sellerName,data.items[i].categoryID,data.items[i].images,data.items[i].thumbnail);
						database.insertseller(data.items[i].sellerID,data.items[i].sellerName);
					}
					insertQuestions(data.questions);
					insertComments(data.comments);
				},
				error:function(jqXHR,textStatus,errorThrown){
				}
			});
		},

		getSearchItems: function(searchTerm,pageNo,itemsPP) {
			// TODO: need to get the correct category ts
			var ts = new Date().getTime();
			var itemIDs = new StringBuffer();

			this.db.transaction(function(tx) {
				// Get the item IDs we want to send back to the server
				tx.executeSql("SELECT id FROM items WHERE ts>? ORDER BY ts DESC LIMIT 250 ", [(ts-GV.freshTime)], function(tx, rs) {
					// Get all of the up-to-date IDs we have
					if(rs.rows.length > 0) {
						for(var i = 0; i < rs.rows.length; i++) {
							if(i > 0)
								itemIDs.append(",");
							itemIDs.append(rs.rows.item(i)["id"]);
						}

					}

					// Get the seller IDs we want to send back to the server

					// Now send the request for updated info, i.e. items we don't have that we need
					$.ajax({
						async : false,
						url : $.urldomain() + "/search?useHTML5=1&searchTerm="+searchTerm+ "&pageNo=" + pageNo + "&itemsPP=" + itemsPP + "&userID=" + localStorage["userID"] +"&authToken=" + localStorage["authToken"]+ "&hasItems=" + itemIDs.toString(),
						type : "GET",
						dataType : "json",
						success : function(data) {
							//TODO: this should really all be donw with callback functions


							// Insert items
							if(debug) $('#scriptDebug').append("<br />getSearchItems inserting items" + new Date().getTime());

							if(!isEmpty(data.items))
								database.insertItems(data.items);

							// Display the items in the order the server says
							if(!isEmpty(data.order))
								database.displayItems(data.order);

							// Display the page errors
							printErrorsAndStats(data);

							// Insert and update the sellers related to the items
							if(debug) $('#scriptDebug').append("<br />getSearchItems inserting sellers" + new Date().getTime());

							if(!isEmpty(data.sellers))
								database.insertSellers(data.sellers);

							
							// Insert and update the questions related to the items
							if(debug) $('#scriptDebug').append("<br />getSearchItems inserting questions" + new Date().getTime());

							if(!isEmpty(data.questions))
								database.insertQuestions(data.questions);
							
							// Insert and update the comments related to the items
							if(debug) $('#scriptDebug').append("<br />getSearchItems inserting comments" + new Date().getTime());
							
							if(!isEmpty(data.comments))
								database.insertComments(data.comments);
							
							// Prefetches images for next page
							if(!isEmpty(data.prefetch))
								database.prefetchCommand(data.prefetch);


							// Update categories timestamps that have the current parent as the must be up-to-date
							// otherwise they would have been returned in the requets


							//TODO: read categories from DB
							//TODO: update category times with new TS
							//TODO: update items with new TS


						},
						error : function(jqXHR, textStatus, errorThrown) {
							if(debug) $('#scriptDebug').append("<br />getSearchItems error " + textStatus + new Date().getTime())
						}
					});

				}, function(tx, err) {
					alert(err.message);
				});
			});
		},
};