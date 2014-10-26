var database = {
	// declare and initalize global variables
	db : null,
	
	/*
	 * Create the C-MART database to hold website data
	 * Will insert base info such as categories and states at start
	 *///ANDY APPROVED
	initDb: function () {
			if (window.openDatabase){
				this.db = openDatabase("CMART4","0.02","CMART Local Database",500000);
				this.createTables();
				this.insertCategories();
				//this.insertBulkData();
				//this.insertStates();
			}
	},

	/*
	 * Create the database tables if they do not exist
	 *///ANDY APPROVED
	 //TODO: make indexes, make tims stamps
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
			tx.executeSql("CREATE TABLE IF NOT EXISTS items (id INT PRIMARY KEY,itemnumber INT,myaccount INT,name TEXT,description TEXT, quantity INT, startPrice FLOAT, reserve BOOLEAN, buyNowPrice FLOAT, currentBid FLOAT, noOfBids INT, startDate TIMESTAMP, endDate TIMESTAMP, sellerID INT, sellerName TEXT, categoryID INT, images TEXT, thumbnail TEXT)", [],
					function (tx, rs) { },
					function (tx, err) { }
					);
			tx.executeSql("CREATE TABLE IF NOT EXISTS categories (id INT PRIMARY KEY,parent INT,name TEXT, ts TIMESTAMP)", [],
					function (tx, rs) { },
					function (tx, err) { }
					);
			tx.executeSql("CREATE TABLE IF NOT EXISTS itemcache (id TEXT PRIMARY KEY, categoryid INT, items INT, range INT,sync TIMESTAMP)", [],
					function (tx, rs) { },
					function (tx, err) { }
					);
			tx.executeSql("CREATE TABLE IF NOT EXISTS sellers (id INT PRIMARY KEY, username TEXT, rating INT)", [],
					function (tx, rs) { },
					function (tx, err) { }
					);
			tx.executeSql("CREATE INDEX p1 ON items (categoryID, itemnumber)", [],
					function (tx, rs) { },
					function (tx, err) { }
					);
			tx.executeSql("CREATE INDEX p2 ON itemcache (categoryid, items)", [],
					function (tx, rs) { },
					function (tx, err) { }
					);
			});
			
			
	},
	
	/*
	 * Inserts the categories and states
	 *///ANDY APPROVED
	insertBulkData: function(){
		$.ajax({
			async: false,
			url:$.urldomain()+"/getbulkdata?useHTML5=1",
			type: "GET",
			dataType: "json",
			success:function(data){
				database.insertCategories(data.categories);
				
				// Insert the categories
				if(typeof data.categories != "undefined") {
					//database.insertCategories(data.categories);
					alert("insertBulkData: finished inserting categories" );
				}
				else{
					alert("insertBulkData: no categories" );
				}
				
				// Insert the states
				if(typeof data.states != "undefined") {
					//insertSates(data.states);
				}
				else{
					alert("insertBulkData: no states" );
				}
					
			},
			error:function(jqXHR,textStatus,errorThrown){
				alert("insertBulkData: " + textStatus + " " + errorThrown);
				}
		});
	},
	
	/*
	 * Inserts categories
	 *///ANDY APPROVED
	insertCategories: function(categories) {
		alert("Starting DB transaction");
		//var buf = new StringBuffer();
		//buf.append("INSERT INTO categoriess (id,parent,name) VALUES (");
		/*			buf.append("1");
					buf.append(",");
					buf.append("0");
					buf.append(",\"")
					buf.append("test");
					buf.append("\");");*/
			alert("about to do DB transaction ");		
		
				database.db.transaction(function(tx) {
					//buf = new StringBuffer();

					// Crete SQL statement
					var stri = ("INSERT INTO categories (id,parent,name) VALUES (0,1,\"test\")");
					//buf.append("1");
					//buf.append(",");
					//buf.append("0");
					//buf.append(",\"")
					//buf.append("test");
					//buf.append("\");");
					
					//alert(buf.toString());
					
					// Insert category
					tx.executeSql(stri,[],
						function (tx, rs) {alert("success : " + rs.length);},
						function (tx, err) {alert("error: " + err.message);}
						);
				
				});
		alert("Finish DB transaction ");
		/*this.db.transaction(function(tx) {
			for(var i = 0; i < categories.length; i = i + 1) {
				var buf = new StringBuffer();
				
				// Crete SQL statement
				buf.append("INSERT INTO categories (id,parent,name) VALUES (");
				buf.append(categories[i].categoryID);
				buf.append(",");
				buf.append(categories[i].parent);
				buf.append(",\"")
				buf.append(categories[i].name);
				buf.append("\");");
				
				// Insert category
				tx.executeSql(buf.toString(), [], function(tx, rs) {
				}, function(tx, err) {alert("insertCategories: " + err.code);
				});
			}
		});*/
	},
	
	/*
	 * Insert states
	 *///ANDY APPROVED
	insertStates: function(states) {
		this.db.transaction(function(tx) {
			for(var i = 0; i < states.length; i = i + 1) {
				var buf = new StringBuffer();
				
				// Create SQL statement
				buf.append("INSERT INTO states (id, shortName,longName) VALUES  (");
				buf.append(states[i].id);
				buf.append(",");
				buf.append(states[i].shortName);
				buf.append(",\"")
				buf.append(cstates[i].longName);
				buf.append("\");");
				
				// Insert state
				tx.executeSql(buf.toString(), [], function(tx, rs) {
				}, function(tx, err) {alert("insertStates: " + err.code);
				});
			}
		});
	},
	
	//TODO: make a server function to return these!!
	/*insertStates: function (){
		this.db.transaction(function(tx) {
			tx.executeSql('SELECT id FROM states where id==1', [], 
					function (tx, rs) {
						if(rs.rows.length<=0){
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (1,\"AL\",\"Alabama\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (2,\"AK\",\"Alaska\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (3,\"AZ\",\"Arkansas\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (4,\"CA\",\"California\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (5,\"CO\",\"Colorado\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (6,\"CT\",\"Connecticut\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (7,\"DE\",\"Delaware\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (8,\"FL\",\"Florida\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (9,\"GA\",\"Georgia\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (10,\"HI\",\"Hawaii\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (11,\"ID\",\"Idaho\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (12,\"IL\",\"Illinois\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (13,\"IN\",\"Indiana\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (14,\"IA\",\"Iowa\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (15,\"KS\",\"Kansas\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (16,\"KY\",\"Kentucky\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (17,\"LA\",\"Louisiana\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (18,\"ME\",\"Maine\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (19,\"MD\",\"Maryland\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (20,\"MA\",\"Massachusetts\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (21,\"MI\",\"Michigan\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (22,\"MN\",\"Minnesota\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (23,\"MS\",\"Mississippi\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (24,\"MO\",\"Missouri\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (25,\"MT\",\"Montana\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (26,\"NE\",\"Nebraska\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (27,\"NV\",\"Nevada\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (28,\"NH\",\"New Hampshire\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (29,\"NJ\",\"New Jersey\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (31,\"NM\",\"New Mexico\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (32,\"NY\",\"New York\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (33,\"NC\",\"North Carolina\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (34,\"OH\",\"Ohio\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (35,\"OK\",\"Oklahoma\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (36,\"OR\",\"Oregon\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (37,\"PA\",\"Pennsylvania\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (38,\"RI\",\"Rhode Island\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (39,\"SC\",\"South Carolina\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (40,\"SD\",\"South Dakota\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (41,\"TN\",\"Tennessee\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (42,\"TX\",\"Texas\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (43,\"UT\",\"Utah\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (44,\"VT\",\"Vermont\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (45,\"VA\",\"Virginia\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (46,\"WA\",\"Washington\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (47,\"WV\",\"West Virginia\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (48,\"WI\",\"Wisconsin\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (49,\"WY\",\"Wyoming\")");
							tx.executeSql("INSERT INTO states (id, shortName,longName) VALUES (50,\"ND\",\"North Dakota\")");
						}
					},
					function (tx, err) {});
		});
	},*/

	populateStatesSelect: function(defaultstate){
		this.db.transaction(function(tx) {
			tx.executeSql("SELECT * FROM states ORDER BY shortName", [],
				function (tx, rs) {
					var states_data="";//"<option value=\"0\" SELECTED>Please Select...</option>";
					if(rs.rows.length>0){
						if(defaultstate==null){
							for(var i=0;i<rs.rows.length;i=i+1){
								states_data=states_data+"<option value=\"" + rs.rows.item(i)["id"] + "\">" + rs.rows.item(i)["shortName"]+" - "+rs.rows.item(i)["longName"] + "</option>";
							}
						}else{
							for(var i=0;i<rs.rows.length;i=i+1){
								if(rs.rows.item(i)["id"]==defaultstate){
								states_data=states_data+"<option value=\"" + rs.rows.item(i)["id"] + "\" SELECTED>" + rs.rows.item(i)["shortName"]+" - "+rs.rows.item(i)["longName"] + "</option>";
								}else{
								states_data=states_data+"<option value=\"" + rs.rows.item(i)["id"] + "\">" + rs.rows.item(i)["shortName"]+" - "+rs.rows.item(i)["longName"] + "</option>";
							}
							}
						}
						
					}
					$("#state").append(states_data);
					$("#state").selectmenu('refresh');
			},function (tx, err) {});});
	},
	
	populateCategoriesSelect: function(){
		this.db.readTransaction(function(tx) {
			tx.executeSql("SELECT * FROM categories", [],
				function (tx, rs) {
					var data="";//"<option value=\"0\" SELECTED>Please Select...</option>";
					if(rs.rows.length>0){
						for(var i=0;i<rs.rows.length;i=i+1){
							data=data+"<option value=\"" + rs.rows.item(i)["id"] + "\">" + decodeURIComponent(rs.rows.item(i)["name"]) + "</option>";
						}
						$("#sell_category").append(data);
						$("#sell_category").selectmenu('refresh');
					}else{
						var response=$.ajax({
							async: false,
							url:$.urldomain()+"/sellitem?useHTML5=1",
							type: "GET",
							dataType: "json",
							success:function(data){
								//database.insertcategories(data.categories);
							},
							error:function(jqXHR,textStatus,errorThrown){
								alert(textStatus+" , "+errorThrown);
								}
						});
					}
					
			},function (tx, err) {});});
	},
	
	//TODO:this should re-try to get the user if the user is not present
	getitem: function(id){
		this.db.readTransaction(function(tx) {
			tx.executeSql("SELECT * FROM items INNER JOIN sellers ON items.sellerID = sellers.id WHERE items.id=?", [id],
			//tx.executeSql("SELECT * FROM items WHERE items.id=?", [id],
				function (tx, rs) {
					if(rs.rows.length>0){
						$('#thumbnail').html("<img src=\"..//img/"+rs.rows.item(0)["thumbnail"]+"\" height=\"40\" width=\"40\" alt=\"\">");
						$('#name').html(rs.rows.item(0)["name"]);
						$('#seller').html("<a href=\"viewuser.html?viewUserID="+rs.rows.item(0)["sellerID"]+"\">"+rs.rows.item(0)["username"]+"</a>");
						$('#endDate').html(rs.rows.item(0)["endDate"]);
						$('#noOfBids').html(rs.rows.item(0)["noOfBids"]);
						$('#quantityAvailable').html(rs.rows.item(0)["quantity"]);
						if(rs.rows.item(0)["reserve"]=='true'){
							$('#currentBid').html(rs.rows.item(0)["currentBid"]+" Reserve Not Yet Met");
						}else{
							$('#currentBid').html(rs.rows.item(0)["currentBid"]);
						}
						$('#currentBid2').html(rs.rows.item(0)["currentBid"]);
						$('#currentBid3').html(rs.rows.item(0)["currentBid"]);
						$('#name2').html(rs.rows.item(0)["name"]);
						$('#description').html(rs.rows.item(0)["description"]);
						
						if(rs.rows.item(0)["buyNowPrice"]>0){
							$('#buynowcost').html("$"+rs.rows.item(0)["buyNowPrice"]);
							$('#buynowform').css('display','block');
						}
						
						$('#description').html(rs.rows.item(0)["description"]);
						
						$('#images').html(decodeURI(rs.rows.item(0)["images"]));
					}
					else{
						$('#endDate').html("rows not greater than zero");
					}
			},function (tx, err) {
				alert(tx + " " + err.message);
				$('#endDate').html("ERROR on transaction");
			});});
	},
	
	insertitem: function(id,itemnumber,name,description,quantity,startPrice,reserve,buyNowPrice,currentBid,noOfBids,startDate,endDate,sellerID,sellerName,categoryID,images,thumbnail){
		if(typeof categoryID == "undefined"){
			
			categoryID=0;
		}
		
		var tempimages = images;
		images="";
		for(i=0;i<tempimages.length;i++){
			j=i+1;
			images=images+"<label for=\"image"+j+"\"><img src=\"..//img/"+tempimages[i].url+"\" width=\"300\" alt=\""+tempimages[i].description+"\"></label>";
		}
		images=encodeURI(images); 
		if(itemnumber==null){	
			this.db.transaction(function(tx) {
				tx.executeSql("UPDATE items SET name=\""+name+"\",description=\""+description+"\",quantity="+quantity+",startPrice="+startPrice+",reserve=\""+reserve + "\",buyNowPrice="+buyNowPrice+",currentBid="+currentBid+",noOfBids="+noOfBids+",startDate=\""+startDate+"\",endDate=\""+endDate +"\",sellerID="+sellerID+",categoryID="+categoryID+",images=\""+images+"\",thumbnail=\""+thumbnail+"\" WHERE id="+id,[],
						function (tx, rs) {},
						function (tx, err) {alert(err.message);}
						);
				tx.executeSql("INSERT OR IGNORE INTO items (id,itemnumber,name,description,quantity,startPrice,reserve,buyNowPrice,currentBid,noOfBids,startDate,endDate,sellerID,sellerName,categoryID,images,thumbnail) VALUES ("+id+",0,\""+name+"\",\""+description+"\","+quantity+","+startPrice+",\""+reserve+"\"" +
						","+buyNowPrice+","+currentBid+","+noOfBids+",\""+startDate+"\",\""+endDate+"\","+sellerID+",\""+sellerName+"\","+categoryID+",\""+images+"\",\""+thumbnail+"\")",[],
						function (tx, rs) {},
						function (tx, err) {alert(err.message);}
						);
			});
		}else{
			this.db.transaction(function(tx) {
				tx.executeSql("UPDATE items SET name=\""+name+"\",itemnumber="+itemnumber+",description=\""+description+"\",quantity="+quantity+",startPrice="+startPrice+",reserve=\""+reserve + "\",buyNowPrice="+buyNowPrice+",currentBid="+currentBid+",noOfBids="+noOfBids+",startDate=\""+startDate+"\",endDate=\""+endDate +"\",sellerID="+sellerID+",categoryID="+categoryID+",images=\""+images+"\",thumbnail=\""+thumbnail+"\" WHERE id="+id,[],
						function (tx, rs) {},
						function (tx, err) {alert(err.message);}
						);
				tx.executeSql("INSERT OR IGNORE INTO items (id,itemnumber,name,description,quantity,startPrice,reserve,buyNowPrice,currentBid,noOfBids,startDate,endDate,sellerID,sellerName,categoryID,images,thumbnail) VALUES ("+id+","+itemnumber+",\""+name+"\",\""+description+"\","+quantity+","+startPrice+",\""+reserve+"\"" +
						","+buyNowPrice+","+currentBid+","+noOfBids+",\""+startDate+"\",\""+endDate+"\","+sellerID+",\""+sellerName+"\","+categoryID+",\""+images+"\",\""+thumbnail+"\")",[],
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
							window.location.reload();
							alert("Bid failed");
						}
					},
					function (tx, err) {alert(err.message);}
					);
			
			
		});
	},
	
	findaddress: function(userid){
		this.db.transaction(function(tx) {
			tx.executeSql("SELECT * FROM addresses WHERE userid ="+userid+" AND isDefault='true'", [],
				function (tx, rs) {
					if(rs.rows.length>0){
						populateBuyConfirmation(rs.rows.item(0)["id"],rs.rows.item(0)["street"],rs.rows.item(0)["town"],rs.rows.item(0)["zip"],rs.rows.item(0)["state"]);
					}else{
						downloadAddress();
					}
			},function (tx, err) {});});
	},
	
	insertaddress: function(addressid,userid,street,town,zip,state,isdefault){
		this.db.transaction(function(tx) {
			tx.executeSql("UPDATE addresses SET id=\""+addressid+"\",userID=\""+userid+"\",street=\""+street+"\",town=\""+town+"\",zip=\""+zip + "\",state=\""+state+"\",isDefault=\""+isdefault+"\" WHERE id="+addressid,[],
					function (tx, rs) {},
					function (tx, err) {alert(err.message);}
					);
			tx.executeSql("INSERT OR IGNORE INTO addresses (id,userID,street,town,state,zip,isDefault) VALUES (\""+addressid+"\",\""+userid+"\",\""+street+"\",\""+town+"\",\""+state+"\",\""+zip+"\",\""+isdefault+"\")",[],
					function (tx, rs) {},
					function (tx, err) {alert(err.message);}
					);
			
		});
	},
	
	

	syncInfo: function(categoryID,pageNo,itemsPP){
		this.db.transaction(function(tx) {
			tx.executeSql("SELECT * FROM itemcache WHERE categoryid=? AND items>=? AND items<? ORDER BY items",[categoryID,parseInt(pageNo)*parseInt(itemsPP),parseInt(pageNo+1)*parseInt(itemsPP)],
				function (tx, rs) {
				var currenttime=new Date();
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
					}
				},
				function (tx, err) {alert(err.message);}
				);
		});
	},
	
	getItemInfo: function(categoryID,pageNo,itemsPP,timestamp){
		$.ajax({
			async: false,
			url:$.urldomain()+"/browsecategory?useHTML5=1&categoryID="+categoryID+"&pageNo="+pageNo+"&itemsPP="+itemsPP+"&userID="+localStorage["userID"],
			type: "GET",
			dataType: "json",
			success:function(data){
				browseItemsArray(data.items);
				browseCategoriesArray(data.categories);
				
				database.insertsellers(data.sellers);	
				
				///database.insertcategory(data.categories);
				//database.insertcategories(data.categories);
				/*for(var i=0;i<data.categories.length;i=i+1){
					database.insertcategory(data.categories[i].categoryID,data.categories[i].parent,data.categories[i].name);
				
				}*/
				
				for(var i=0;i<data.items.length;i=i+1){
					database.insertitem(data.items[i].id,pageNo*itemsPP+i,data.items[i].name,data.items[i].description,data.items[i].quantity,data.items[i].startPrice,data.items[i].reserve,data.items[i].buyNowPrice,data.items[i].currentBid,data.items[i].noOfBids,data.items[i].startDate,data.items[i].endDate,data.items[i].sellerID,data.items[i].sellerName,data.items[i].categoryID,data.items[i].images,data.items[i].thumbnail);
					//database.insertseller(data.items[i].sellerID,data.items[i].sellerName);
				}
				
				
				/*for(var i=0;i<data.sellers.length;i=i+1){
					if(data.sellers[i].id!="-1"){
					 database.insertseller(data.sellers[i].id,data.sellers[i].name);
					}
				}*/
				
				
				database.db.transaction(function(tx) {
					tx.executeSql("UPDATE itemcache SET items="+itemsPP*(pageNo)+", range="+itemsPP+", sync="+timestamp.getTime()+" WHERE id='"+categoryID+" "+itemsPP*(pageNo)+"'");
					tx.executeSql("INSERT OR IGNORE INTO itemcache (id,categoryid,items,range,sync) VALUES ('"+categoryID+" "+itemsPP*(pageNo)+"',"+categoryID+","+itemsPP*(pageNo)+","+itemsPP+","+timestamp.getTime()+")");
				});			
			},
			error:function(jqXHR,textStatus,errorThrown){
				}
		});
	},
	

	/*insertcategories: function(categories) {
		this.db.transaction(function(tx) {
			for(var i = 0; i < categories.length; i = i + 1) {
				var buf = new StringBuffer();
				buf.append("INSERT OR IGNORE INTO categories (id,parent,name) VALUES (");
				buf.append(categories[i].categoryID);
				buf.append(",");
				buf.append(categories[i].parent);
				buf.append(",\"")
				buf.append(categories[i].name);
				buf.append("\");");
				tx.executeSql(buf.toString(), [], function(tx, rs) {
				}, function(tx, err) {alert(err.code);
				});
			}
		});
	},*/

	
	/*insertcategories: function(categories){
		var now = new Date();
		var buf = new StringBuffer();
		buf.append("<div id=\"floatright >Category: </div><div id=\"floatcenter\"><select id=\"sell_category\">")
		for(var i=0;i<categories.length;i=i+1){
			buf.append("<option value=\"").append(categories[i].categoryID).append("\">").append(decodeURIComponent(categories[i].name)).append("</option>");
		}
		buf.append("</select></div>");
		var now2 = new Date();
		$("#categorydiv").html(buf.toString());
		
		var now3 = new Date();
		alert("Time to create SELECT input box: "+(now2.getMilliseconds()-now.getMilliseconds())+"\nTime to set HTML of SELECT inpu box: "+(now3.getMilliseconds()-now2.getMilliseconds()));
		/*
		this.db.transaction(function(tx) {
			for(var i=0;i<categories.length;i=i+1){
				var buf = new StringBuffer();
				buf.append("INSERT OR IGNORE INTO categories (id,parent,name) VALUES (");
				buf.append(categories[i].categoryID);
				buf.append(",");
				buf.append(categories[i].parent);
				buf.append(",\"")
				buf.append(categories[i].name);
				buf.append("\");");
				tx.executeSql(buf.toString(),[],
						function (tx, rs) {},
						function (tx, err) {alert(err.code);}
						);			
			
			}
			
		});
			
	},*/
	
	
	loadCategories: function(parentID){
		this.db.transaction(function(tx) {
			tx.executeSql("SELECT * FROM categories WHERE parent="+parentID,[],
				function (tx, rs) {
					var categories="<div class=\"cats\">";
					if(rs.rows.length>0){
						for(var i=0;i<rs.rows.length;i=i+1){
							categories=categories+"<div class=\"row\"><a href=\"browse.html?categoryID="+rs.rows.item(i)["id"]+"&pageNo=0&itemsPP="+GV.itemsPP+"\" >"+decodeURIComponent(rs.rows.item(i)["name"])+"</a></div>";
						}
					}
					categories=categories+"</div>";
					$("#category_content").html(categories);
				},
				function (tx, err) {alert(err.message);}
				);
		});
		
	},
	
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
	
	clearDatabase: function (){
		this.db.transaction(function(tx) {
			tx.executeSql("DELETE FROM users");
			tx.executeSql("DELETE FROM categories");
			tx.executeSql("DELETE FROM addresses");
			tx.executeSql("DELETE FROM states");
			tx.executeSql("DELETE FROM items");
			tx.executeSql("DELETE FROM categories");
			tx.executeSql("DELETE FROM itemcache");
			tx.executeSql("DELETE FROM sellers");
		});
	},
	
	/*insertsellers: function(vals){
		alert(vals[2].name);
	},*/
	
	insertsellers: function(sellers) {

		database.db.transaction(function(tx) {
			for(var i = 0; i < sellers.length; i = i + 1) {
				var buf = new StringBuffer();
				buf.append("INSERT OR IGNORE INTO sellers (id,username,rating) VALUES (");
				buf.append(sellers[i].id);
				buf.append(",\"");
				buf.append(sellers[i].name);
				buf.append("\",")
				buf.append(sellers[i].rating);
				buf.append(");");
				//alert("new 4 " + buf.toString());

				tx.executeSql(buf.toString(), [], function(tx, rs) {
				}, function(tx, err) {alert(tx + " " + err.toString + " " + err.message);
				});
			};
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
	
	getseller: function(id){
		this.db.transaction(function(tx) {
			tx.executeSql("SELECT * FROM sellers WHERE id=?", [id],
				function (tx, rs) {
					if(rs.rows.length>0){
						$('#username').html(rs.rows.item(0)["username"]);
					};
			},function (tx, err) {});});
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
			},
			error:function(jqXHR,textStatus,errorThrown){
				}
		});
	},
	
};