API.withLoad=true;
function certUploadToggle() {
	let el = $("#certFiles")[0];
	if((el.style.display=="none")|((el.style.display+"").trim().length<1)) {
		el.style.display="block";
	} else {
		el.style.display="none";
	}
}
function certUpload() {
	var ajaxData = new FormData();
	ajaxData.append("crt", $("#fileCertCrt")[0].files[0]);
	ajaxData.append("key", $("#fileCertKey")[0].files[0]);
	uploadCertAPI(ajaxData,x=>{
		let response = JSON.parse(x);
		if (response.error == false) {
			note({
				content: ("Перезагрузка сервера.."),
				type: "info",
				time: 4
			});
		} else {
			note({
				content: "Ошибка[" + response.code + "] | " + response.codeDescRu,
				type: "error",
				time: 3
			});
		}
	},
	x=>{
		note({
			content: "Ошибка | Сервер недоступен",
			type: "error",
			time: 4
		});
	});
}
function changeWebPort() {
	    bootbox.prompt({
        title: "Изменение WEB Порта",
		inputType: 'number',
		min: 0,
		max: 65535,
        message: "Введите новый порт[0-65535], сервер будет перезагружен",
        callback: function(result) {
			if(result==undefined)
				return;
			if((result+"").trim().length<1) {
				note({
					content: ("Вы ничего не ввели"),
					type: "warn",
					time: 3
				});
				return;
			}
			webPort(result, x => {
				let response = JSON.parse(x);
				if (response.error == false) {
					note({
						content: ("Сервер будет перезапущен.."),
						type: "info",
						time: 4
					});
				} else {
					note({
						content: "Ошибка[" + response.code + "] | " + response.codeDescRu,
						type: "error",
						time: 3
					});
				}
			}, x => {
				note({
					content: "Ошибка | Сервер недоступен",
					type: "error",
					time: 4
				});
			});
        }
    });
}

function changeAgentsPort() {
	    bootbox.prompt({
        title: "Изменение WEB Порта",
		inputType: 'number',
		min: 0,
		max: 65535,
        message: "Введите новый порт[0-65535], сервер будет перезагружен",
        callback: function(result) {
			if(result==undefined)
				return;
			if((result+"").trim().length<1) {
				note({
					content: ("Вы ничего не ввели"),
					type: "warn",
					time: 3
				});
				return;
			}
			agentsPort(result, x => {
				let response = JSON.parse(x);
				if (response.error == false) {
					note({
						content: ("Порт изменен"),
						type: "info",
						time: 4
					});
				} else {
					note({
						content: "Ошибка[" + response.code + "] | " + response.codeDescRu,
						type: "error",
						time: 3
					});
				}
			}, x => {
				note({
					content: "Ошибка | Сервер недоступен",
					type: "error",
					time: 4
				});
			});
        }
    });
}

function modalStartCondition(el) {
    let condId = getCondId(el);
    startCondition(condId,x => {
		let response = JSON.parse(x);
		if (response.error == false) {
			//
		} else {
			note({
				content: "Ошибка[" + response.code + "] | " + response.codeDescRu,
				type: "error",
				time: 3
			});
		}
		}, x => {
			note({
				content: "Ошибка | Сервер недоступен",
				type: "error",
				time: 4
			});
		}
    );
}
function modalStopCondition(el) {
    let condId = getCondId(el);
    stopCondition(condId, x => {
		let response = JSON.parse(x);
		if (response.error == false) {
			//
		} else {
			note({
				content: "Ошибка[" + response.code + "] | " + response.codeDescRu,
				type: "error",
				time: 3
			});
		}
		}, x => {
			note({
				content: "Ошибка | Сервер недоступен",
				type: "error",
				time: 4
			});
		}
    );
}

function addOrEditCond() {
    let newCond = getCondObject();
    if (addNewCondBool) {
        disableSubmitButtons();
        API.sendPost("/conditions/add", newCond,
            x => {
                enableSubmitButtons();
                if (x.error) {
                    note({
                        content: x.codeDescRu,
                        type: "warn",
                        time: 3
                    });
                } else {
                    note({
                        content: x.codeDescRu,
                        type: "success",
                        time: 3
                    });
                    $('#addNewCondition').modal('hide');
                }
            },
            x => {
                enableSubmitButtons();
                note({
                    content: "Ошибка | Сервер недоступен",
                    type: "error",
                    time: 4
                });
            });
    } else {
        if (checkConds(oldCond, newCond)) {
            note({
                content: "Вы ничего не изменили",
                type: "warn",
                time: 4
            });
        } else {
            disableSubmitButtons();
            newCond.id = oldCondId;
            API.sendPost("/conditions/update", newCond,
                (x) => {
                    enableSubmitButtons();
                    if (x.error) {
                        note({
                            content: x.codeDescRu,
                            type: "warn",
                            time: 3
                        });
                    } else {
                        note({
                            content: x.codeDescRu,
                            type: "success",
                            time: 3
                        });
                        $('#addNewCondition').modal('hide');
                    }
                },
                (x) => {
                    enableSubmitButtons();
                    note({
                        content: "Ошибка | Сервер недоступен",
                        type: "error",
                        time: 4
                    });
                });
        }
    }
}
var dialog;
function setDialog(dialog) {
	this.dialog = dialog;
}
function getDialog() {
	return this.dialog;
}
function showLoad() {
	hideLoad();
	setDialog(bootbox.dialog({
		centerVertical: true,    
		message: '<p class="text-center mb-0" style="display: grid";><span>Загрузка данных...</span><span style="margin: auto;" class="spinner-grow text-primary"></span></i></p>',
			closeButton: false
	}));
}
function hideLoad() {
	$( ".bootbox" ).not(".bootbox-confirm").not(".bootbox-prompt").each(function( index ) {
		$( this ).modal("hide");
	});
	if($( ".bootbox" ).not(".bootbox-confirm").not(".bootbox-prompt").length>0) {
		setTimeout(hideLoad,50);
	}
}
function disableSubmitButtons() {
    $('.btn[type="submit"]').each(function() {
        this.setAttribute('disabled', '');
    });
}

function enableSubmitButtons() {
    $('.btn[type="submit"]').each(function() {
        this.removeAttribute('disabled', '');
    });
}
async function subscribeSettings() {
	subscribe("settings", "/longpoll/updates/settings", "", x => {
        loadSettings();
    });
}
async function subscribeConditions(hash) {
    let devId = hash.replaceAll(/[^\d]/gi, "");
    subscribe("conditions-" + devId, "/longpoll/updates/deviceconditions", "devId="+devId, x => {
		loadConditions(devId)
    });
}

async function loadSettings(hash) {
    loadAllSettings(x => {
        let response = JSON.parse(x);
        if (response.error) {
            intCheckLogin();
            note({
                content: ("Ошибка[" + response.code + "] | " + response.codeDescRu),
                type: "error",
                time: 4
            });
            return;
        } else {
            // GOOD
            console.log(response);
            appendSettingsGui(response);
        }
    }, x => {
        // SERVER UNAVAILABLE
        note({
            content: "Ошибка | Сервер недоступен",
            type: "error",
            time: 4
        });
    });
}

async function loadConditions(hash) {
    let deviceId = hash.replaceAll(/[^\d]/gi, "");
    getRulesByDeviceId(deviceId, x => {
        let response = JSON.parse(x);
        //console.log(response);
        if (response.error) {
            intCheckLogin();
            note({
                content: ("Ошибка[" + response.code + "] | " + response.codeDescRu),
                type: "error",
                time: 4
            });
            console.log(response);
            return;
        } else {
            // GOOD
            console.log(response);
            appendConditionsGui(response);
        }
    }, x => {
        // SERVER UNAVAILABLE
        note({
            content: "Ошибка | Сервер недоступен",
            type: "error",
            time: 4
        });
    });
}

function checkConds(a, b) {
    console.log("OLD");
    console.log(a);
    console.log("NEW");
    console.log(b);
    if (a.name == b.name &
        a.protocol == b.protocol &
        a.extPort == b.extPort &
        a.autorun == b.autorun &
        a.firewall == b.firewall &
        a.condType == b.condType &
        a.condData == b.condData &
        a.targetHost == b.targetHost &
        a.targetPort == b.targetPort)
        return true;
    return false;
}
var addNewCondBool;
var oldCond;
var newCondObject = {
    name: "",
    protocol: "tcp",
    extPort: NaN,
    autorun: true,
    firewall: false,
    condType: "default",
    condData: "",
    targetHost: "",
    targetPort: NaN
}

function getCondObject() {
    return {
        devId: $('#devNameForCond')[0].getAttribute("devid"),
        name: $('#inputConditionName')[0].value,
        protocol: $('#gridRadios1')[0].checked ? "tcp" : "udp",
        extPort: parseInt($('#inputConditionExtPort')[0].value),
        autorun: $('#inputCheckBoxAutorun')[0].checked,
        firewall: $('#inputCheckBoxFirewall')[0].checked,
        condType: $('#selectCondType')[0].value,
        condData: $('#inputConditionData')[0].value,
        targetHost: $('#inputConditionTargetHost')[0].value,
        targetPort: parseInt($('#inputConditionTargetPort')[0].value)
    };
}

function condObjectFromGui(condDom) {
    return {
        devId: $('#devNameForCond')[0].getAttribute("devid"),
        name: condDom.getAttribute("cond-name"),
        protocol: condDom.getAttribute("cond-protocol"),
        extPort: parseInt(condDom.getAttribute("cond-extPort")),
        autorun: condDom.getAttribute("cond-autorun") == "true" ? true : false,
        firewall: condDom.getAttribute("cond-firewall") == "true" ? true : false,
        condType: condDom.getAttribute("cond-type"),
        condData: condDom.getAttribute("cond-data"),
        targetHost: condDom.getAttribute("cond-target-host"),
        targetPort: parseInt(condDom.getAttribute("cond-target-port"))
    };
}
async function setCondGui(condObj) {
    $('#inputConditionName')[0].value = condObj.name;
    $('#gridRadios1')[0].checked = condObj.protocol == "tcp";
    $('#gridRadios2')[0].checked = !(condObj.protocol == "tcp");
    $('#inputConditionExtPort')[0].value = condObj.extPort;
    $('#inputCheckBoxAutorun')[0].checked = condObj.autorun;
    $('#inputCheckBoxFirewall')[0].checked = condObj.firewall;
    $('#selectCondType')[0].value = condObj.condType;
    updateGuiCondType(condObj.condType);
    if (condObj.condType.toLowerCase() == "user") {
        let login = await getLoginById(condObj.condData);
        oldCond.condData = login;
        $('#inputConditionData')[0].value = login;
    } else {
        $('#inputConditionData')[0].value = condObj.condData;
    }
    $('#inputConditionTargetHost')[0].value = condObj.targetHost;
    $('#inputConditionTargetPort')[0].value = condObj.targetPort;
}

function modalAddNewCondition() {
    addNewCondBool = true;
    $('#addNewConditionLabel')[0].textContent = "Новое правило";
    $('#modalSumbitAdd')[0].style.display = 'flex';
    $('#modalSumbitEdit')[0].style.display = 'none';
    $('#addNewCondition').modal('show');
    setCondGui(newCondObject);
}

function deleteAllConditions() {
    let devId = $('#devNameForCond')[0].getAttribute("devid");
    bootbox.confirm({
        title: "Удалить все правила?",
        message: "Вы точно хотите удалить все правила?",
        buttons: {
            cancel: {
                label: '<i class="fa col-4 fa-times fix-bootbox"></i> Нет'
            },
            confirm: {
                label: '<i class="fa col-4 fa-check"></i> Да'
            }
        },
        callback: function(result) {
            if (result) {
                deleteAllConditionsAPI(devId, x => {
                    let response = JSON.parse(x);
                    if (response.error == false) {
                        note({
                            content: ("Правила удалены"),
                            type: "info",
                            time: 4
                        });
                    } else {
                        note({
                            content: "Ошибка[" + response.code + "] | " + response.codeDescRu,
                            type: "error",
                            time: 3
                        });
                    }
                }, x => {
                    note({
                        content: "Ошибка | Сервер недоступен",
                        type: "error",
                        time: 4
                    });
                });
            }
        }
    });
}

function modalDiscDevice(el) {
    let devId = getDevId(el);
    console.log("trying disc: " + devId);
    bootbox.confirm({
        title: "Отключить устройство?",
        message: "Вы точно хотите отключить это устройство?",
        buttons: {
            cancel: {
                label: '<i class="fa col-4 fa-times fix-bootbox"></i> Нет'
            },
            confirm: {
                label: '<i class="fa col-4 fa-check"></i> Да'
            }
        },
        callback: function(result) {
            if (result) {
                discDevice(devId, x => {
                    let response = JSON.parse(x);
                    if (response.error == false) {
                        note({
                            content: ("Устройство отключено"),
                            type: "info",
                            time: 4
                        });
                    } else {
                        note({
                            content: "Ошибка[" + response.code + "] | " + response.codeDescRu,
                            type: "error",
                            time: 3
                        });
                    }
                }, x => {
                    note({
                        content: "Ошибка | Сервер недоступен",
                        type: "error",
                        time: 4
                    });
                });
            }
        }
    });
}
function modalDeleteUser(el) {
	let userId = getUserId(el);
	bootbox.confirm({
		title: "Удалить пользователя?",
		message: "Вы точно хотите удалить этого пользователя?",
		buttons: {
			cancel: {
				label: '<i class="fa col-4 fa-times fix-bootbox"></i> Нет'
			},
			confirm: {
				label: '<i class="fa col-4 fa-check"></i> Да'
			}
		},
		callback: function(result) {
			if (result) {
				deleteUser(userId, x => {
					let response = JSON.parse(x);
					if (response.error == false) {
						note({
							content: ("Пользователь удален"),
							type: "info",
							time: 4
						});
					} else {
						note({
							content: "Ошибка[" + response.code + "] | " + response.codeDescRu,
							type: "error",
							time: 3
						});
					}
				}, x => {
					note({
						content: "Ошибка | Сервер недоступен",
						type: "error",
						time: 4
					});
				});
			}
		}
	});
}
function modalDeleteDevice(el) {
    let devId = getDevId(el);
    console.log("trying delete: " + devId);
    bootbox.confirm({
        title: "Удалить устройство?",
        message: "Вы точно хотите удалить это устройство?",
        buttons: {
            cancel: {
                label: '<i class="fa col-4 fa-times fix-bootbox"></i> Нет'
            },
            confirm: {
                label: '<i class="fa col-4 fa-check"></i> Да'
            }
        },
        callback: function(result) {
            if (result) {
                deleteDevice(devId, x => {
                    let response = JSON.parse(x);
                    if (response.error == false) {
                        note({
                            content: ("Устройство удалено"),
                            type: "info",
                            time: 4
                        });
                    } else {
                        note({
                            content: "Ошибка[" + response.code + "] | " + response.codeDescRu,
                            type: "error",
                            time: 3
                        });
                    }
                }, x => {
                    note({
                        content: "Ошибка | Сервер недоступен",
                        type: "error",
                        time: 4
                    });
                });
            }
        }
    });
}

function modalDeleteCondition(el) {
    let condId = getCondId(el);
    bootbox.confirm({
        title: "Удалить правило?",
        message: "Вы точно хотите удалить это правило?",
        buttons: {
            cancel: {
                label: '<i class="fa col-4 fa-times fix-bootbox"></i> Нет'
            },
            confirm: {
                label: '<i class="fa col-4 fa-check"></i> Да'
            }
        },
        callback: function(result) {
            if (result) {
                deleteCondition(condId, x => {
                    let response = JSON.parse(x);
                    if (response.error == false) {
                        note({
                            content: ("Правило удалено"),
                            type: "info",
                            time: 4
                        });
                    } else {
                        note({
                            content: "Ошибка[" + response.code + "] | " + response.codeDescRu,
                            type: "error",
                            time: 3
                        });
                    }
                }, x => {
                    note({
                        content: "Ошибка | Сервер недоступен",
                        type: "error",
                        time: 4
                    });
                });
            }
        }
    });
}

function getDevId(el) {
    let trDev = el.parentNode.parentNode;
    return trDev.getAttribute('device-id');
}

function getCondId(el) {
    let trCond = el.parentNode.parentNode;
    return trCond.getAttribute('cond-id');
}
function getUserId(el) {
    let trUser = el.parentNode.parentNode;
    return trUser.getAttribute('user-id');
}

var oldCondId;

function modalEditCondition(el) {
    addNewCondBool = false;
    let trCond = el.parentNode.parentNode;
    let condId = getCondId(el);
    oldCondId = condId;
    let loadedCond = condObjectFromGui(trCond);
    oldCond = loadedCond;
    setCondGui(loadedCond);
    $('#addNewConditionLabel')[0].textContent = "Редактировать правило";
    $('#modalSumbitAdd')[0].style.display = 'none';
    $('#modalSumbitEdit')[0].style.display = 'flex';
    $('#addNewCondition').modal('show');
}
async function subscribeUsers() {
    subscribe("users", "/longpoll/updates/users", "", x => {
        updateUsers(false);
    });
}

function unsubscribeUsers() {
    unsubscribe("users");
}
async function subscribeDevices(hash) {
    let userId = hash.replaceAll(/[^\d]/gi, "");
    subscribe("devices-" + userId, "/longpoll/updates/userdevices", "userId=" + userId, x => {
        loadDevices(userId);
    });
}
var notyiff = false;
async function loadDevices(hash) {
    let userId = hash.replaceAll(/[^\d]/gi, "");
    getDevicesByUserId(userId, x => {
        let response = JSON.parse(x);
        //console.log(response);
        if (response.error) {
            intCheckLogin();
            note({
                content: ("Ошибка[" + response.code + "] | " + response.codeDescRu),
                type: "error",
                time: 4
            });
            return;
        } else {
            // GOOD
            appendDevicesGui(response.devices);
        }
    }, x => {
        // SERVER UNAVAILABLE
        note({
            content: "Ошибка | Сервер недоступен",
            type: "error",
            time: 4
        });
    });
}

function guiSetLogin(login) {
    userName.textContent = login;
    newLogin.value = login;
}

function intCheckLogin() {
    checkLogin(x => {
        let response = JSON.parse(x);
        if (response.error == true) {
            window.location.href = 'manage.html';
            // SEND /api/deleteToken
            setCookie("raccoontoken", "");
            window.location.href = 'index.html';
        } else {
            guiSetLogin(response.login);
            isAdminLabel.textContent = response.admin ? "Администратор" : "Пользователь";
            if (!response.admin) {
                let nodes = document.querySelectorAll(".adminonly");
                for (let node of nodes) {
                    node.parentElement.removeChild(node);
                }
            }
        }
    });
}

function updateUsers(showmessage) {
    getUsers(x => {
        let response = JSON.parse(x);
        if (response.error == false) {
            if (showmessage) {
                note({
                    content: "Обновлено",
                    type: "success",
                    time: 2
                });
            }
            $("#userListInner").html("");
            $.each(response.users, function(key, value) {
                var html = '<tr user-id="' + value.id + '">' +
                    '<td>' + value.login + '</td>' +
                    (value.admin ? '<td class="uadmin"><i class="fas fa-check"></i> Да</td>' : '<td></td>') +
                    '<td>' +
                    '<a href="#devices-' + value.id + '"><button type="button" class="btn btn-primary col-2"><i class="fas fa-network-wired"></i> Агенты</button></a>' +
                    (value.admin ? '' : '<button type="button" onclick="modalDeleteUser(this)" class="btn btn-danger col-2"><i class="fas fa-trash"></i> Удалить</button>') +
                    '</td>' +
                    '</tr>';
                $("#userListInner").append(html);
            });
        } else {
            note({
                content: ("Ошибка[" + response.code + "] | " + response.codeDescRu),
                type: "error",
                time: 4
            });
        }
    }, x => {
        note({
            content: "Ошибка | Сервер недоступен",
            type: "error",
            time: 4
        });
        console.log(x);
    });
}

function appendSettingsGui(response) {
	$("#settingCommonName")[0].textContent = response.settings.certCN;
	$("#settingIssuer")[0].textContent = response.settings.certIssuer;
	$("#settingSelfSigned")[0].textContent = (response.settings.selfSigned?"Да":"");
	$("#settingWebPortValue")[0].textContent = response.settings.webPort; // label
	$("#settingAgentsPortValue")[0].textContent = response.settings.agentsPort; // label

	let labelAllowReg = $("#settingRegisterAllowValue")[0];
	labelAllowReg.textContent = (response.settings.allowRegister?"Включена":"Запрещено"); // label
	let btnReg = $("#btnRegToggle")[0]; // button
	if(response.settings.allowRegister) {
		labelAllowReg.classList.add("text-success");
		labelAllowReg.classList.remove("text-danger");
		btnReg.classList.remove("btn-success");
		btnReg.classList.add("btn-warning");
		btnReg.innerHTML = '<i class="fas fa-power-off"></i> Выкл';
	} else {
		labelAllowReg.classList.add("text-danger");
		labelAllowReg.classList.remove("text-success");
		btnReg.classList.add("btn-success");
		btnReg.classList.remove("btn-warning");
		btnReg.innerHTML = '<i class="fas fa-power-off"></i> Разрешить';
	}
	let labelAllowCUBU = $("#settingChangeUserByUserValue")[0];
	labelAllowCUBU.textContent = (response.settings.allowCUBU?"Включена":"Запрещено"); // label
	let btnCubu = $("#btnCUBUToggle")[0]; // button
	if(response.settings.allowCUBU) {
		labelAllowCUBU.classList.add("text-success");
		labelAllowCUBU.classList.remove("text-danger");
		btnCubu.classList.remove("btn-success");
		btnCubu.classList.add("btn-warning");
		btnCubu.innerHTML = '<i class="fas fa-power-off"></i> Выкл';
	} else {
		labelAllowCUBU.classList.add("text-danger");
		labelAllowCUBU.classList.remove("text-success");
		btnCubu.classList.add("btn-success");
		btnCubu.classList.remove("btn-warning");
		btnCubu.innerHTML = '<i class="fas fa-power-off"></i> Разрешить';
	}
	let labelAllowCRBU = $("#settingCreateRulesByUsersValue")[0];
	labelAllowCRBU.textContent = (response.settings.allowCRBU?"Включена":"Запрещено"); // label
	let btnCRBU = $("#btnCRBUToggle")[0]; // button
	if(response.settings.allowCRBU) {
		labelAllowCRBU.classList.add("text-success");
		labelAllowCRBU.classList.remove("text-danger");
		btnCRBU.classList.remove("btn-success");
		btnCRBU.classList.add("btn-warning");
		btnCRBU.innerHTML = '<i class="fas fa-power-off"></i> Выкл';
	} else {
		labelAllowCRBU.classList.add("text-danger");
		labelAllowCRBU.classList.remove("text-success");
		btnCRBU.classList.remove("btn-warning");
		btnCRBU.classList.add("btn-success");
		btnCRBU.innerHTML = '<i class="fas fa-power-off"></i> Разрешить';
	}
}

function appendConditionsGui(response) {
    $('#devNameForCond')[0].textContent = response.device.name;
    $('#devNameForCond')[0].setAttribute("devid", response.device.id);
    if (response.device.connected) {
        $('#devConnectedForCond')[0].innerHTML = '<span class="m-1 text-success"><i class="fas fa-plug"></i> Подключен </span> | ';
        $('#devIpForCond')[0].innerHTML = '<span class="m-1" style="color: #0d6efd">' + response.device.ip + '</span>';
    } else {
        $('#devConnectedForCond')[0].innerHTML = '<span class="text-danger"> <i class="fas fa-times-circle"></i> Отключен</span>';
        $('#devIpForCond')[0].innerHTML = "";
    }
    $("#rulesListInner").html("");
    $.each(response.conditions, async (key, value) => {
        var status;
        var condType;
        switch (value.condType.toLowerCase()) {
            case ("default"):
                condType = "По умолчанию";
                break;
            case ("ip"):
                condType = "IP";
                break;
            case ("network"):
                condType = "Подсеть";
                break;
            case ("user"):
                condType = "Пользователь";
                break;
            case ("sni"):
                condType = "SNI";
                break;
            default:
                condType = "UNKNOWN";
                break;
        }

        switch (value.status) {
            case (1):
                status = '<td class="btn-r-tgl"><span> Выключено</span></td>';
                break;
            case (2):
                status = '<td class="btn-r-tgl">' +
                    '<span class="text-warning"><i class="fas fa-spinner"></i> Запуск</span>' +
                    '</td>';
                break;
            case (3):
                status = '<td class="text-success btn-r-tgl">' +
                    '<span><i class="fas fa-check"></i> Работает</span>' +
                    '</td>';
                break;
            case (4):
                status = '<td class="text-danger btn-r-tgl"><span><i class="fas fa-times"></i> Порт занят</span></td>';
                break;
            case (5):
                status = '<td class="text-danger btn-r-tgl"><span><i class="fas fa-times"></i> Конфликт правил</span></td>'
                break;
            case (6):
                status = '<td class="text-danger btn-r-tgl"><span><i class="fas fa-times"></i> Ошибка</span></td>';
                break;
            default:
                status = '<td>UNKNOWN</td>'
                break;
        }
        let condData;
        if (value.condType.toLowerCase() == "user") {
            condData = await getLoginById(value.condData);
        } else {
            condData = value.condData;
        }
        var html = '<tr cond-id="' + value.id + '" ' +
            'cond-name="' + value.name + '" ' +
            'cond-protocol="' + value.protocol.toLowerCase() + '" ' +
            'cond-extport="' + value.extPort + '" ' +
            'cond-autorun="' + value.autorun + '" ' +
            'cond-firewall="' + value.firewall + '" ' +
            'cond-type="' + value.condType.toLowerCase() + '" ' +
            'cond-data="' + value.condData + '" ' +
            'cond-target-host="' + value.targetHost + '" ' +
            'cond-target-port="' + value.targetPort + '">' +
            '<td>' + value.name + '</td>' +
            '<td>' + value.protocol.toUpperCase() + '</td>' +
            '<td>' + value.extPort + '</td>' +
            (value.firewall ? '<td class="uadmin">Включен</td>' : '<td></td>') +
            '<td><span class="condtype">' + condType + '</span> | <span class="conddata">' + condData + '</span></td>' +
            '<td>' + value.targetHost + ':' + value.targetPort + '</td>' +
            status +
            '<td>' +
            (value.status == 3 ? '<button type="button" onclick="modalStopCondition(this);" class="btn-warning btn"><i class="fas fa-power-off"></i> Выкл</button>' :
                (value.status == 2 ? '<button disabled type="button" onclick="modalStartCondition(this);" class="btn-success btn"><i class="fas fa-power-off"></i> Запуск</button>' :
                    '<button type="button" onclick="modalStartCondition(this);" class="btn-success btn"><i class="fas fa-power-off"></i> Запуск</button>')) +
            '<button type="button" onclick="modalEditCondition(this);" class="btn btn-primary col-2"><i class="fas fa-cog"></i> Настройка</button>' +
            '<button type="button" onclick="modalDeleteCondition(this);" class="btn btn-danger col-2"><i class="fas fa-trash"></i> Удалить</button>' +
            '</td>' +
            '</tr>';
        $("#rulesListInner").append(html);
    });
}

function appendDevicesGui(devices) {
    $("#devicesListInner").html("");
    $.each(devices, function(key, value) {
        var html = '<tr device-id="' + value.id + '">' +
            '<td>' + value.name + '</td>' +
            (value.connected ? '<td class="text-success"><i class="fas fa-plug"></i> Подключен</td>' : '<td class="text-danger"><i class="fas fa-times"></i> Отключен</td>') +
            (value.local ? '<td class="text-success"><i class="fas fa-check"></i> Да</td>' : '<td></td>') +
            '<td>' + (value.connected ? value.ip : 'Неизвестно') + '</td>' +
            '<td>' +
            '<a href="#rules-' + value.id + '"><button type="button" class="btn btn-primary col-2"><i class="fas fa-list"></i> Правила</button></a>' +
            (((value.local) || !(value.connected)) ? '' : '<button type="button" onclick="modalDiscDevice(this);" class="btn btn-warning col-2"><i class="fas fa-times"></i> Отключить</button>') +
            (value.local ? '' : '<button type="button" onclick="modalDeleteDevice(this);" class="btn btn-danger col-2"><i class="fas fa-trash"></i> Удалить</button>') +
            '</td>' +
            '</tr>';
        $("#devicesListInner").append(html);
    });
}
document.addEventListener('DOMContentLoaded', function() {
    var userName = document.getElementById("userName");
    var isAdminLabel = document.getElementById("isAdminLabel");
    var newLogin = document.getElementById("newLogin");
    intCheckLogin();
    var submitUpdateAcc = document.getElementById("submitUpdateAcc");
    var submitDeleteAcc = document.getElementById("submitDeleteAcc");
    var oldPassword = document.getElementById("oldPassword");
    var newPassword = document.getElementById("newPassword");
    submitUpdateAcc.addEventListener('click', function() {
        if (newLogin.value.trim().length > 2 && oldPassword.value.trim().length > 5) {
            if ((newPassword.value.trim().length == 0) | (newPassword.value.trim().length > 5)) {
                // block gui
                updateUser(oldPassword.value.trim(), newPassword.value.trim(), newLogin.value.trim(),
                    x => {
                        let response = JSON.parse(x);
                        //stopAnimate();
                        if (response.error == false) {
                            note({
                                content: response.codeDescRu,
                                type: "success",
                                time: 3
                            });
                            oldPassword.value = "";
                            newPassword.value = "";
                            guiSetLogin(response.login);
                        } else {
                            note({
                                content: ("Ошибка[" + response.code + "] | " + response.codeDescRu),
                                type: "error",
                                time: 4
                            });
                            newPassword.value = "";
                            if (response.code == 106) {
                                oldPassword.value = "";
                                oldPassword.focus();
                            }
                        }
                    }, x => {
                        //unBlockForm();
                        note({
                            content: "Ошибка | Сервер недоступен",
                            type: "error",
                            time: 4
                        });
                        //stopAnimate();
                    });
            }
        }
    });
    submitDeleteAcc.addEventListener('click', function() {
        if (oldPassword.value.trim().length > 5) {
            bootbox.confirm({
                title: "Удалить аккаунт?",
                message: "Вы точно хотите удалить аккаунт? Это действие невозможно отменить.",
                buttons: {
                    cancel: {
                        label: '<i class="fa col-4 fa-times fix-bootbox"></i> Нет'
                    },
                    confirm: {
                        label: '<i class="fa col-4 fa-check"></i> Да'
                    }
                },
                callback: function(result) {
                    if (result) {
                        deleteCurrent(oldPassword.value.trim(), x => {
                            let response = JSON.parse(x);
                            if (response.error == false) {
                                note({
                                    content: ("Пользователь удален"),
                                    type: "info",
                                    time: 4
                                });
                                setTimeout(() => {
                                    window.location.href = 'index.html'
                                }, 1400);
                            } else {
                                note({
                                    content: "Ошибка[" + response.code + "] | " + response.codeDescRu,
                                    type: "error",
                                    time: 3
                                });
                                oldPassword.value = "";
                            }
                        }, x => {
                            note({
                                content: "Ошибка | Сервер недоступен",
                                type: "error",
                                time: 4
                            });
                        });
                    }
                }
            });
        }
    });
    $('#selectCondType').on('change', function() {
        updateGuiCondType(this.value);
    });
}, false);

function updateGuiCondType(type) {
    var dataForCondition = $('#dataForCondition')[0];
    var labelForCondData = $('#dataForCondition label')[0];
    var inputConditionData = $('#inputConditionData')[0];
    inputConditionData.value = "";
    if (type.toLowerCase() == "default") {
        dataForCondition.style.display = "none";
        inputConditionData.removeAttribute('required');
    } else {
        dataForCondition.style.display = "flex";
        inputConditionData.setAttribute('required', '');
    }
    switch (type.toLowerCase()) {
        case ("default"):
            dataForCondition.style.display = "none";
            inputConditionData.removeAttribute('pattern');
            inputConditionData.required = false;
            break;
        case ("ip"):
            inputConditionData.required = true;
            labelForCondData.textContent = "IP-Адрес";
            inputConditionData.placeholder = "IP-Адрес источника, например 192.168.1.1";
            inputConditionData.setAttribute('pattern', "^((\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.){3}(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])$");
            break;
        case ("network"):
            inputConditionData.required = true;
            labelForCondData.textContent = "Подсеть";
            inputConditionData.placeholder = "Подсеть источника, like 192.168.1.0/24";
            inputConditionData.setAttribute('pattern', "^((((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|(\\/([0-9]|[1-2][0-9]|3[0-2]))$)){4}))$");
            break;
        case ("user"):
            inputConditionData.required = true;
            labelForCondData.textContent = "Пользователь";
            inputConditionData.placeholder = "Имя пользователя, на основе его IP";
            inputConditionData.removeAttribute('pattern');
            break;
        case ("sni"):
            inputConditionData.required = true;
            labelForCondData.textContent = "SNI";
            inputConditionData.placeholder = "SNI, имя домена, только для TLS";
            inputConditionData.removeAttribute('pattern');
            break;
    }
}
function newCertRequest() {
	bootbox.prompt({
		title: "Let's Encrypt - Выпуск сертификата",
		message: "Введите доменное имя</br> На сервере должен быть установлен OpenSSL",
		callback: function(result){ 
				note({
					content: "В Разработке",
					type: "info",
					time: 4
				});
				return;
			if(result.trim().length<3) {
				note({
					content: "Ошибка | Слишком короткий домен",
					type: "error",
					time: 4
				});
			} else {
				console.log(result); 
				requestCert(result.trim(),x => {
				let response = JSON.parse(x);
				if (response.error == false) {
					note({
						content: "Сертификат сохранен в "+response.path,
						type: "success",
						time: 3
					});
				} else {
					note({
						content: "Ошибка[" + response.code + "] | " + response.codeDescRu,
						type: "error",
						time: 3
					});
				}
				}, x => {
					note({
						content: "Ошибка | Сервер недоступен",
						type: "error",
						time: 4
					});
				});
			}
		}
	});
}

function privGetLoginById(id) {
    let endpoint = document.raccoon?.endpoint;
    return $.ajax({
        type: 'GET',
        url: endpoint + "/tools/getloginbyid" + "?id=" + id,
        crossDomain: true
    });
};
async function getLoginById(id) {
    try {
        const x = await privGetLoginById(id);
        console.log(x);
        return JSON.parse(x).login;
    } catch (err) {
        console.log(err);
        return "SERVER_ERROR";
    }
}