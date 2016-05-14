function getSocialLinks() {
	var url = "http://localhost:8888/user-service/socialLinks/";
	$.ajax({
		type : 'GET',
		url : url,
		async : false,
		encode : true
	}).success(function(data) {
			document.getElementById('facebook').setAttribute("href",data.fbLogin);
			document.getElementById('google').setAttribute("href",data.googleLogin);
			document.getElementById('linkedin').setAttribute("href",data.linkedInLogin);
			document.getElementById('instagramInLogin').setAttribute("href",data.instagramInLogin);
			document.getElementById('twitter').setAttribute("href",data.twitterLogin);
			document.getElementById('instagramInLoginAlternate').setAttribute("href",data.instagramInLoginAlternate);
	}).error(function( errorThrown) {
			alert(errorThrown);
	});
}