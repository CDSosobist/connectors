package cdsosobist.connid.connectors.naumen_rest_connector;

public class PathsHandler {
	
	//Пользователь
	static final String PERSEMAIL = "email";							//Адрес электронной почты	(Строка)
	static final String PERSISACTIVE = "isEmployeeActive";				//Активный	(Логический)
	static final String PERSINTERNALPHONE = "internalPhoneNumber";		//Внутренний телефон	(Строка)
	static final String PERSCITYPHONE = "cityPhoneNumber";				//Городской телефон	(Строка)
	static final String PERSREMOVALDATE = "removalDate";				//Дата архивирования	(Дата/время)
	static final String PERSMODIFDATE = "lastModifiedDate";				//Дата изменения	(Дата/время)
	static final String PERSBIRTHDATE = "dateOfBirth";					//Дата рождения	(Дата)
	static final String PERSCREATIONDATE = "creationDate";				//Дата создания	(Дата/время)
	static final String PERSPOSITION = "post";							//Должность	(Строка)
	static final String PERSHOMEPHONE = "homePhoneNumber";				//Домашний телефон	(Строка)
	static final String PERSISLOCKED = "isEmployeeLocked";				//Заблокирован	(Логический)
	static final String PERSFIRSTNAME = "firstName";					//Имя	(Строка)
	static final String PERSISPERFORMER = "performer";					//Исполнитель	(Логический)
	static final String PERSPRIVCODE = "privateCode";					//Личный код	(Строка)
	static final String PERSLOGIN = "login";							//Логин	(Строка)
	static final String PERSMOBILEPHONE = "mobilePhoneNumber";			//Мобильный телефон	(Строка)
	static final String PERSNUMBER = "number";							//Номер	(Целое число)
	static final String PERSPARENT = "parent";							//Отдел	(Ссылка на бизнес-объект)
	static final String PERSMIDDLENAME = "middleName";					//Отчество	(Строка)
	static final String PERSPASSWORD = "password";						//Пароль	(Строка)
//	static final String PERSSECGROUPS = "employeeSecGroups";			//Права	(Набор групп пользователей)
	static final String PERSISREMOVED = "removed";						//Признак архивирования	(Логический)
	static final String PERSALIAS = "commentAuthorAlias";				//Псевдоним	(Строка)
	static final String PERSMANAGER = "immediateSupervisor";			//Руководитель	(Ссылка на бизнес-объект)
	static final String PERSISINTEGRATION = "employeeForIntegration";	//Служебный для интеграций	(Логический)
	static final String PERSPHONESINDEX = "phonesIndex";				//Телефоны	(Строка)
	static final String PERSMETACLASS = "metaClass";					//Тип	(Метакласс)
	static final String PERSUUID = "UUID";								//Уникальный идентификатор	(Строка)
	static final String PERSFULLNAME = "title";							//ФИО	(Строка)
	static final String PERSLASTNAME = "lastName";						//Фамилия	(Строка)
	static final String PERSIMAGE = "image";							//Фотография	(Файл)
	static final String PERSSTORAGE = "sysUserStorage";					//System user storage	(Текст)
	static final String PERSIDHOLDER = "idHolder";						//idHolder	(Строка)
	static final String PERSEXTLINKS = "externalLinks";					//Боты/каналы	(Гиперссылка)
	static final String PERSFORRESTPASS = "forResetPass";				//Восстановить пароль	(Логический)
	static final String PERSTELEGRAMID = "telegramId";					//Идентификатор в Telegram	(Строка)
	static final String PERSICON = "icon";								//Иконка	(Элемент справочника)
	static final String PERSTELEGRAMLOGIN = "telegram";					//Имя пользователя Telegram (username)	(Строка)
	static final String PERSNEEDGENPASS = "isGenPass";					//Сгенерировать пароль и отправить сотруднику	(Логический)
	static final String PERSEMPID = "employeeID";						//Табельный номер (Строка)
	static final String PERSFLID = "fl1C";								//GUID физлица в 1С
	static final String PERSCORPPHONE = "cPhForAddrBook";				//Корпоративный номер для адресных книг
	static final String PERSCITYPHONEFORSIGN = "cityPhoneAddrB";		//Общий городской номер для адресной книги
	static final String PERSPHONENUMBER = "persNumAddrB";				//Личный номер телефона для указания в адресной книге

	
	//Отдел
	static final String OUPARENT = "parent";							//Вышестоящий отдел	(Ссылка на бизнес-объект)
	static final String OUREMOVDATE = "removalDate";					//Дата архивирования	(Дата/время)
	static final String OUMODIFYDATE = "lastModifiedDate";				//Дата изменения	(Дата/время)
	static final String OUCREATIONDATE = "creationDate";				//Дата создания	(Дата/время)
	static final String OUNAME = "title";								//Название	(Строка)
	static final String OUNUMBER = "number";							//Номер	(Целое число)
	static final String OUISREMOVED = "removed";						//Признак архивирования	(Логический)
	static final String OUMANAGER = "head";								//Руководитель	(Ссылка на бизнес-объект)
	static final String OUMETACLASS = "metaClass";						//Тип объекта	(Метакласс)
	static final String OUUUID = "UUID";								//Уникальный идентификатор	(Строка)
	static final String OUDN = "dstngshd";								//distingushedName	(Строка)
	static final String OUIDHOLDER = "idHolder";						//idHolder	(Строка)
	static final String OUADDRESS = "adress";							//Адрес	(Строка)
	static final String OUICON = "icon";								//Иконка	(Элемент справочника)

	//Пути запросов
	static final String createOU = "/services/rest/create-m2m/ou$ou";
	static final String createAccount = "/services/rest/create-m2m/employee$employee";
	static final String deleteObject = "/services/rest/delete/";
	static final String updateObject = "/services/rest/edit-m2m/";
	
	static final String getByUUID = "/services/rest/get/";
	static final String getAllAccs = "/services/rest/find/employee/";
	static final String getAllOUs = "/services/rest/find/ou/";
	static final String accessKeyPath = "accessKey=";

}
