package com.petya136900.raccoonvpn.rest.v1.codes;

public enum ResponseCodes {
	
	BAD_TASK_ID(145l,"Неправильный taskId"),	
	CANT_CONVERT_CERT(144l,"Не удалось конвертировать сертификат"),
	ERROR_WHILE_CREATING_FILES(143l,"Ошибка при создании файлов"),
	BAD_FILES(142l,"Неправильные или пустые файлы"),
	DEVICE_NOT_CONNECTED(141l,"Устройство не подключено"),
	USER_CHANGE_DISABLED(140l,"Редактирование данных отключено"),
	CREATE_RULES_BY_USER_DISABLED(139l,"Создание правил пользователями отключено"),
	REGISTRATION_DISABLED(138l,"Регистрация отключена"),
	PORT_ALREADY_IN_USE(137l,"Этот порт уже используется"),
	PORT_BUSY(136l,"Порт занят"),
	BAD_PORT(135l,"Неправильный порт"),
	PORT_80_BUSY(134l,"80 Порт занят"),
	CANT_CREATE_LE_ACCOUNT(133l,"Не удалось создать аккаунт Let's Encrypt'a"),
	OPEN_SSL_ERROR(132l,"OpenSSL не установлен"),
	BAD_DOMAIN_NAME(131l,"Неправильное имя домена"),
	CANT_DISCONNECT_LOCAL_DEVICE(130l,"Нельзя удалить локальное устройство"),
	BAD_USER_ID(129l,"Неправильный ID пользователя"),
	CANT_DELETE_LOCAL_DEVICE(128l,"Нельзя удалить локальное устройство"),
	AGENT_TOKEN_NOT_ALLOWED(127l,"Токен агента не разрешен"),
	CONDITION_NOT_RUNNING(126l,"Условие не активно"),
	CONDITION_NOT_FOUND(125l, "Условие не найдено"),
	BAD_CONDITION_ID(124l, "Неправильное ID условия"),
	CONDITION_ALREADY_EXIST(123l,"Условие уже существует"),
	BAD_JSON(122l,"Неправильный JSON"),
	DEVICE_NOT_FOUND(121l, "Устройство с таким ID не найдено"),
	BAD_DEVICE_ID(120l,"Неправильный ID устройства"),
	NOT_ENOUGH_RIGHTS(119l,"Недостаточно прав"),
	FAILED_TO_DELETE_USER(118l,"Не удалось удалить пользователя"),
	CANT_DELETE_LAST_ADMIN(117l,"Нельзя удалить единственного администратора"),
	CANT_UPDATE_PASSWORD(116l,"Не удалось обновить пароль"),
	CANT_UPDATE_LOGIN(115l,"Не удалось обновить логин"),
	CANT_CREATE_USER(114l,"Не удалось создать пользователя"),
	USER_ALREADY_EXIST(113l,"Пользователь уже существует"),
	ADMIN_REQUIRED(112l,"Требуется токен администратора"),
	TOKEN_NOT_FOUND(111l,"Токен не найден"),
	UNKNOWN_ERROR(110l,"Неизвестная ошибка"),
	IP_CHANGED(109l,"Вход из нового местоположения"),
	TOKEN_EXPIRED(108l,"Токен истек"),
	CANT_GENERATE_TOKEN(107l,"Не удается сгенерировать токен"),
	WRONG_PASS(106l,"Неправильный пароль"),
	USER_NOT_FOUND(105l,"Пользователь не найден"),
	BAD_LOGIN(104l,"Неправильный логин, минимум 5 символов, [a-z0-9]"),
	BAD_PASS(103l,"Некорректный пароль"),
	BAD_TOKEN(100l,"Неправильный токен"),
	
	SERVER_RELOADING(19l,"Сервер перезапускается"),
	PORT_UPDATED(18l,"Порт обновлен"),
	SETTING_CHANGED(17l,"Настройка изменена"),
	DEVICE_DELETED(16l,"Устройство удалено"),
	DEVICE_DISCONNECTED(15l,"Устройство отключено"),
	CONDITION_RUNNING(14l,"Запускается"),
	CONDITION_DELETED(13l,"Условие удалено"),
	CONDITION_UPDATED(12l,"Условие обновлено"),
	CONDITION_ADDED(11l,"Условие добавлено"),
	LONGPOLL_HISTORY_ADDED(10l,"Добавлено"),
	USER_DELETED(9l,"Пользователь удален"),
	LOGGED_OUT(8l,"Выход выполнен"),
	USER_NOT_CHANGED(7l,"Пользователь не изменен"),
	LOGIN_AND_PASSWORD_UPDATED(6l,"Логин и пароль обновлены"),
	LOGIN_UPDATED(5l,"Логин обновлен"),
	PASSWORD_UPDATED(4l,"Пароль обновлен"),
	USER_CREATED(3l,"Пользователь создан"),
	LOGGED_IN(2l,"Вход выполнен"),
	GOOD_TOKEN(1l,"Правильный токен");

	private Long code;
	private String descRu;
	
	ResponseCodes(Long code) {
		this.setCode(code);
	}

	ResponseCodes(Long code, String descRu) {
		this.setCode(code);
		this.setDescRu(descRu);
	}
	
	public Long getCode() {
		return code;
	}

	private void setCode(Long code) {
		this.code = code;
	}

	public String getDescRu() {
		return descRu;
	}

	public void setDescRu(String descRu) {
		this.descRu = descRu;
	}
}
