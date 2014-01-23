$(document).ready(function () {
    $('#plus').click(function () {
        console.log('clicked +');
        $('img').each(function () {
            $(this).width($(this).width() + 25)
        });
    });
    $('#minus').click(function () {
        console.log('clicked -');
        $('img').each(function () {
            $(this).width($(this).width() - 25)
        });
    })
    
     
 
    $(window).scroll(function(){ 				// scroll event  
 		$('#scaleWidget').css("top", ($(window).scrollTop() + 10) + px);
    });
    	//var stickyTop = $('.stay').offset().top; // returns number 
 
	    //if (stickyTop < windowTop) {
	    //	var windowTop = $(window).scrollTop(); // returns number
	     // $('.sticky').css({ position: 'fixed', top: 0 });
	    //}
	    //else {
	     // $('.sticky').css('position','static');
	   // }
 
 	 //});
});