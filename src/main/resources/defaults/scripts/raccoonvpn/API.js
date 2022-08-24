class API {
	static withLoad = false;
	static nogui = false;
	static connected = false;
	static errorShown =  false;
	static send(method, args, onSuccess, onError) {
		let endpoint = document.raccoon?.endpoint;
		return $.ajax(endpoint+method+(args==undefined?"":("?"+args)), {
			 method: 'GET',
			 crossDomain: true,
			 success: x=>{
				 if(!API.connected) {
					API.connected=true;
					console.log("Соединение с сервером установлено");
					API.errorShown=false;
					if(!API.nogui) {
						$("footer")[0].classList.remove("server-message");
						let sm = $("#serverMessage")[0];
						sm.textContent = "";
						sm.classList.remove("alert","alert-danger");
					}
				 }
				 if(API.withLoad) {
					 setTimeout(function t() {
						hideLoad();
					}, 200);
				 }
				 if(typeof onSuccess !== 'undefined') {
					onSuccess(x);
				 }
			 },
			 error: (x,y,z)=>{
				if(API.withLoad) {
					setTimeout(function t() {
						hideLoad();
					}, 200);
				}
				if(y!="abort") {
					if(API.connected) {
						API.connected=false;
					}
					if(!API.errorShown) {
						console.log("Сервер недоступен");
						API.errorShown=true;
						if(!API.nogui) {
							$("footer")[0].classList.add("server-message");
							let sm = $("#serverMessage")[0];
							sm.textContent = "Сервер недоступен";
							sm.classList.add("alert","alert-danger");
						}
					}
					if(typeof onError !== 'undefined') {
						onError(x,y,z);
					}
				}
			 }
		});
	}
	static sendPost(method, data, onSuccess, onError) {
		let endpoint = document.raccoon?.endpoint;
		return $.ajax({
			type: 'POST',
			url: endpoint+method,
			crossDomain: true,
			data: JSON.stringify(data),
			contentType: "application/json; charset=utf-8",
			dataType: 'json',
			success: onSuccess==undefined?{}:onSuccess,
			error: onError==undefined?{}:onError
		});
	}
	static sendPostFiles(method, data, onSuccess, onError) {
		let endpoint = document.raccoon?.endpoint;
		return jQuery.ajax({
			url: endpoint+method,
			data: data,
			cache: false,
			contentType: false,
			processData: false,
			method: 'POST',
			success: onSuccess==undefined?{}:onSuccess,
			error: onError==undefined?{}:onError
		});		
	}
}