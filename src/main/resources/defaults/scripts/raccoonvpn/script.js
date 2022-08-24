function loadAllSettings(onSuccess, onError) {
	API.send("/setting/getall","",onSuccess,onError);
}
function agentsPort(port, onSuccess, onError) {
	API.send("/setting/agentsport","port="+port,onSuccess,onError);
}
function webPort(port, onSuccess, onError) {
	API.send("/setting/webport","port="+port,onSuccess,onError);
}
function registerAllowToggle(onSuccess, onError) {
	API.send("/setting/toggleregister","",onSuccess,onError);
}
function changeUserByUserToggle(onSuccess, onError) {
	API.send("/setting/togglecubu","",onSuccess,onError);
}
function createRulesByUserToggle(onSuccess, onError) {
	API.send("/setting/togglecrbu","",onSuccess,onError);
}
function uploadCertAPI(data, onSuccess, onError) {
	API.sendPostFiles("/setting/uploadcert", data, onSuccess, onError);
}
function deleteAllConditionsAPI(id, onSuccess, onError) {
	API.send("/conditions/deleteallbydevid","id="+id,onSuccess,onError);
}
function deleteDevice(id, onSuccess, onError) {
	API.send("/devices/deletebyid","id="+id,onSuccess,onError);
}
function requestCert(domain, onSuccess, onError) {
	API.send("/tools/reqcert","domain="+domain,onSuccess,onError);
}
function discDevice(id, onSuccess, onError) {
	API.send("/devices/disconnectbyid","id="+id,onSuccess,onError);
}
function deleteCondition(id, onSuccess, onError) {
	API.send("/conditions/deletebyid","id="+id,onSuccess,onError);
}
function startCondition(id, onSuccess, onError) {
	API.send("/conditions/startbyid","id="+id,onSuccess,onError);
}
function stopCondition(id, onSuccess, onError) {
	API.send("/conditions/stopbyid","id="+id,onSuccess,onError);
}
async function getRulesByDeviceId(id,onSuccess,onError) {
	API.send("/conditions/getbydeviceid","id="+id,onSuccess,onError);
}
async function getDevicesByUserId(id,onSuccess,onError) {
	API.send("/devices/getbyuserid","id="+id,onSuccess,onError);
}
async function getUsers(onSuccess,onError) {
	API.send("/user/getall","",onSuccess,onError);
}
function deleteUser(id, onSuccess, onError) {
	API.send("/user/deletebyid","id="+id,onSuccess,onError);
}
async function updateUser(oldPassword,newPassword,newLogin,onSuccess,onError) {
	let oldPasswordHash = await sha256(oldPassword);
	let newPasswordHash = ((newPassword==undefined)||newPassword.trim().length==0)?(""):(await sha256(newPassword));
	API.send("/user/current/update","login="+newLogin+"&oldPasswordHash="+oldPasswordHash+"&newPasswordHash="+newPasswordHash,onSuccess,onError);
}
async function checkLogin(onSuccess,onError) {
	API.send("/user/current","",onSuccess,onError);
}
async function register(login,password,onSuccess,onError) {
	let passHash = await sha256(password);
	API.send("register","login="+login+"&passwordHash="+passHash,onSuccess,onError);
}
async function login(login,password,onSuccess,onError) {
	let passHash = await sha256(password);
	API.send("login","login="+login+"&passwordHash="+passHash,onSuccess,onError);
}
async function logout(onSuccess,onError) {
	API.send("/logout","",x=>{
		let response = JSON.parse(x);
		if(response.error==false) {
			setCookie("raccoontoken","");
		}
		if(onSuccess!=undefined)
			onSuccess(x);
	},onError);
}
async function deleteCurrent(oldPassword,onSuccess,onError) {
	let oldPasswordHash = await sha256(oldPassword);
	API.send("/user/current/delete","oldPasswordHash="+oldPasswordHash,x=>{
		let response = JSON.parse(x);
		if(response.error==false) {
			setCookie("raccoontoken","");
		}
		if(onSuccess!=undefined)
			onSuccess(x);
	},onError);
}
//________________//
//   LONGPOLL     //
var subscriptions = {};
function unsubscribeAll() {
	for (var subscription in subscriptions) {
		unsubscribe(subscription);
	}	
	while(subscriptions.length > 0) {
		subscriptions.pop();
	}
}
function subscribe(key, url, params, onresponse) {
	subscription = subscriptions[key];
	if(subscription==undefined) {
		subscriptions[key] = {
			lastTs:-1
		}
		subscriptions[key].to = setTimeout(function t() {
			checkSubscription(key, url, params, onresponse);
		}, 100);
	} 
}
function checkSubscription(key, url, params, onresponse) {
	if(subscriptions[key]!=undefined) {
		subscriptions[key].xhr = 
		API.send(url,"ts="+subscriptions[key].lastTs+"&"+params,x=>{
			if(subscriptions[key]==undefined) {
				return;
			}
			let response = JSON.parse(x);
			if(response.error) {
				subscriptions[key].lastTs = -1;
				intCheckLogin();
				subscriptions[key].to = setTimeout(function t() {
					checkSubscription(key, url, params, onresponse);
				}, 2500);
				return;
			} else {
				if(!(response.timeout)) {
					if(response.event.ts!=subscriptions[key].lastTs) {
						onresponse(response);
					}
				} else {
				}
				subscriptions[key].lastTs = response.event.ts;
			}
			subscriptions[key].to = setTimeout(function t() {
				checkSubscription(key, url, params, onresponse);
			}, 100);
		},x=>{
			if(subscriptions[key]==undefined) {
				return;
			}
			subscriptions[key].lastTs = -1;
			subscriptions[key].to = setTimeout(function t() {
				checkSubscription(key, url, params, onresponse);
			}, 7000);
		});
	}
}

function unsubscribe(key) {
	subscription = subscriptions[key];
	if(subscription!=undefined) {
		try {
			subscriptions[key].xhr.abort();
		}catch (e) {}
		clearTimeout(subscription.to);
		subscriptions[key]=undefined;
	} 
}
//                //
//________________//
async function checkLoginDefault() {
	checkLogin(x=>{
		let response = JSON.parse(x);
		if(response.error==false) {
			window.location.href='manage.html';
		}
	});
}
function getCookie(name) {
  let matches = document.cookie.match(new RegExp(
    "(?:^|; )" + name.replace(/([\.$?*|{}\(\)\[\]\\\/\+^])/g, '\\$1') + "=([^;]*)"
  ));
  return matches ? decodeURIComponent(matches[1]) : undefined;
}
function api(url, onSuccess, onError) {
	let raccoontoken = getCookie("raccoontoken");
	let endpoint = document.raccoon?.endpoint;
	$.ajax((endpoint==undefined?"":endpoint)+url+(raccoontoken==undefined?"":("?raccoontoken="+raccoontoken)), {
		 method: 'GET',
		 crossDomain: true,
		 success: onSuccess==undefined?{}:onSuccess,
		 error: onError==undefined?{}:onError
	});
}
function setCookie(name, value, options = {}) {
	options = {
		path: '/',
		...options
	};
	if (options.expires instanceof Date) {
		options.expires = options.expires.toUTCString();
	}
	let updatedCookie = encodeURIComponent(name) + "=" + encodeURIComponent(value);
	for (let optionKey in options) {
		updatedCookie += "; " + optionKey;
		let optionValue = options[optionKey];
		if (optionValue !== true) {
			updatedCookie += "=" + optionValue;
		}
	}
	document.cookie = updatedCookie;
}