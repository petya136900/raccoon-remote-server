function blockForm() {
	changeFormDisabledState(true);
}
function changeFormDisabledState(state) {
	if(state) {
		floatingInput.setAttribute("disabled",'');
		floatingPassword.setAttribute("disabled",'');
		submitLogin.setAttribute("disabled",'');
		rememberMeCheckbox.setAttribute("disabled",'');	
	} else {
		floatingInput.removeAttribute("disabled");
		floatingPassword.removeAttribute("disabled");
		submitLogin.removeAttribute("disabled");
		rememberMeCheckbox.removeAttribute("disabled");			
	}
}
function unBlockForm() {
	changeFormDisabledState(false);
}
function startAnimate() {
	submitLogin.setAttribute("data-loading","");
}
function stopAnimate() {
	submitLogin.removeAttribute("data-loading");	
}
document.addEventListener('DOMContentLoaded', function() {
	checkLoginDefault();
	var submitLogin = document.getElementById("submitLogin");
	var floatingInput = document.getElementById("floatingInput");
	var floatingPassword = document.getElementById("floatingPassword");
	var rememberMeCheckbox = document.getElementById("rememberMeCheckbox");
	submitLogin.addEventListener( 'click', function() {		
		if(floatingInput.value.trim().length>2&&floatingPassword.value.trim().length>5) {
			blockForm();
			startAnimate();
			login(floatingInput.value.trim(),floatingPassword.value.trim(),
			 x=>{
				unBlockForm();
				let response = JSON.parse(x);
				console.log(response);
				stopAnimate();
				if(response.error==false) {
					note({
						content: "Вход выполнен",
						type: "success",
						time: 3
					});
					let options = {};
					if(location.protocol=="https:") {
						options.secure=true;
					}
					if(rememberMeCheckbox.checked) {
						options['max-age']=response.maxAge;
					}
					setCookie("raccoontoken",response.raccoonToken,options);
					window.location.href='manage.html';
				} else {
					note({
						content: "Ошибка["+response.code+"] | "+response.codeDescRu,
						type: "error",
						time: 4
					});
					floatingPassword.value="";
					floatingPassword.focus();
				}
			},x=>{
				unBlockForm();
				note({
					content: "Ошибка | Сервер недоступен",
					type: "error",
					time: 4
				});
				console.log(x);
				stopAnimate();
			});
		}
	}, false );
}, false);