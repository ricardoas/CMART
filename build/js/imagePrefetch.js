preloadImages=function(arrayOfImages){
	$.each(arrayOfImages,function(){
		$('<img/>')[0].src=this;
	});
};