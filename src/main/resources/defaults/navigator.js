function showView(viewId) {
	let views = document.querySelectorAll(".content-view");
	if(document.querySelector("#"+viewId)==undefined) {
		showView("blockStart");	
		return;
	}		
	for(view of views) {
		view.style.display=(viewId==view.id)?"block":"none";
	}
}
function selectView() {
	let navs = document.querySelectorAll(".nav-link[href]");
	for(let nav of navs) {
		if(nav.getAttribute("href")==location.hash) {
			nav.classList.add("active");
		} else {
			nav.classList.remove("active");
		}
	}	
	unsubscribeAll();
	switch(location.hash.replaceAll(/(-.*$)/gi,"")) {
		case("#start"):
			showView("blockStart");
		break;
		case("#user"):
			if(location.hash="user-settings")
				showView("blockUserSettings");
		break;
		case("#logout"):
			console.log("Bye!");
			logout(x=>{window.location.href='index.html';},x=>{});
		break;
		case("#devices"):
			showLoad();
			subscribeDevices(location.hash);
			showView("blockDevices");
		break;
		case("#settings"):
			showLoad();
			subscribeSettings();
			showView("blockSettings");
		break;
		case("#users"):
			showLoad();
			subscribeUsers();
			showView("blockUsers");
		break;			
		case("#rules"):
			showLoad();
			subscribeConditions(location.hash);
			showView("blockRules");
		break;
		default:
			showView("blockStart");
		break;
	}
}
document.addEventListener('DOMContentLoaded', function() {
	try {
		selectView();
	} catch(e){
		console.log(e);
		showView("blockStart");
	}
	window.onhashchange = selectView;
});