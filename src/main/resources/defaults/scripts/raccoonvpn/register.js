function blockForm() {
	changeFormDisabledState(true);
}
function changeFormDisabledState(state) {
	if(state) {
		floatingInput.setAttribute("disabled",'');
		floatingPassword.setAttribute("disabled",'');
		floatingPasswordConfirm.removeAttribute("disabled");
		regButton.setAttribute("disabled",'');
	} else {
		floatingInput.removeAttribute("disabled");
		floatingPassword.removeAttribute("disabled");
		floatingPasswordConfirm.removeAttribute("disabled");
		regButton.removeAttribute("disabled");
	}
}
function unBlockForm() {
	changeFormDisabledState(false);
}
function startAnimate() {
	regButton.setAttribute("data-loading","");
}
function stopAnimate() {
	regButton.removeAttribute("data-loading");	
}
document.addEventListener('DOMContentLoaded', function() {
	checkLoginDefault();
	var regButton = document.getElementById("regButton");
	var floatingInput = document.getElementById("floatingInput");
	var floatingPassword = document.getElementById("floatingPassword");
	var floatingPasswordConfirm = document.getElementById("floatingPasswordConfirm");
	regButton.addEventListener( 'click', function() {		
		if(floatingInput.value.trim().length>2&&floatingPassword.value.trim().length>5&&floatingPasswordConfirm.value.trim().length) {
			if(!(floatingPassword.value.trim()==floatingPasswordConfirm.value.trim())) {
				note({
					content: "Пароли не совпадают",
					type: "warn",
					time: 3
				});
				return;
			}
			blockForm();
			startAnimate();
			register(floatingInput.value,floatingPassword.value,
			 x=>{
				unBlockForm();
				let response = JSON.parse(x);
				console.log(response);
				stopAnimate();
				if(response.error==false) {
					note({
						content: "Аккаунт успешно создан",
						type: "success",
						time: 3
					});
					setTimeout(()=>{window.location.href='index.html'}, 1200);
				} else {
					note({
						content: "Ошибка["+response.code+"] | "+response.codeDescRu,
						type: "error",
						time: 4
					});
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