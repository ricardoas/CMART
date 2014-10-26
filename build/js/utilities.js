$.urlParam = function(name){
    var results = new RegExp('[\\?&]' + name + '=([^&#]*)').exec(window.location.href);
    if(!results){
    	var results=[0,0];
    }
    if(typeof results[1] == 'undefined'){
    	var results=[0,0];
    };
    return results[1] || 0;
}
$.urlPage = function(){
    var results = new RegExp('[/]([^&#/]*).html').exec(window.location.href);
    if(results == null){
    	var results=[0,0];
    };
    return results[1] || 0;
}

$.urldomain = function(){
    var results = new RegExp('http://([^&#/]*)[/]([^&#/]*)[/]').exec(window.location.href);
    if(results == null){
    	var results=[0,0];
    };
    
    return "http://"+results[1]+"/"+results[2] || 0;
}

function showLoading(){
	$("#body").css("display:none");
	$("#header").css("display:none");
	//document.body.style.overflow='hidden';
	//document.body.scroll="no";
	//Get the screen height and width
    var maskHeight = $(document).height();
    var maskWidth = $(window).width();
 
    //Set height and width to mask to fill up the whole screen
    $('#mask').css({'width':maskWidth,'height':maskHeight,'display':'block' });
     
 
    //Get the window height and width
    var winH = $(window).height();
    var winW = $(window).width();
           
    //Set the popup window to center
    $('#dialog').css('top',  winH/2-$('#dialog').height()/2);
    $('#dialog').css('left', winW/2-$('#dialog').width()/2);
 
    //transition effect
    $('#dialog').fadeIn(750); 
}

function hideLoading(){
	//document.body.style.overflow='scroll';
	//document.body.scroll="yes";
	$("#body").css("display:block");
	$("#header").css("display:block");
	$('#mask, .window').hide();
};

function StringBuffer() { 
	   this.buffer = []; 
	 } 
	 function StringBuffer(size) { 
	   this.buffer = [size]; 
	 } 

	 StringBuffer.prototype.append = function append(string) { 
	   this.buffer.push(string); 
	   return this; 
	 }; 

	 StringBuffer.prototype.toString = function toString() { 
	   return this.buffer.join(""); 
	 }; 
