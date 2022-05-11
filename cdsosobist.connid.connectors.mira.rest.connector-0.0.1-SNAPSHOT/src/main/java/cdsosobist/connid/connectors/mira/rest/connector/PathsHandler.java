package cdsosobist.connid.connectors.mira.rest.connector;

public class PathsHandler {
	
	static final String MAINPATH = "/mira/service/v2";
	static final String APPIDPATH = "appid=";
	static final String SKEYPATH = "&secretkey=";
	static final String SIGNPATH = "&sign=";
	
	//Module Persons (Физические лица)
	static final String PATHTOPERSONS = "/persons";
	static final String PERSMIRAID = "personid";					//Идентификатор физического лица Число
	static final String PERSLASTNAME = "plastname";					//Фамилия Строка (100)
	static final String PERSFIRSTNAME = "pfirstname";				//Имя (для физического лица всегда должно быть заполнено) Строка (100)
	static final String PERSSURNAME = "psurname";					//Отчество Строка (100)
	static final String PERSISUSER = "isuser";						//Является ли физическое лицо пользователем (false – нет, true -да) Логическое
	static final String PERSMIRALOGIN = "pilogin";					//Логин Строка (150)
	static final String PERSMIRAPWD = "pipassword";					//Пароль Строка (150)
	static final String PERSOUID = "caid";							//Идентификатор организации Число
	static final String PERSOUNAME = "caidname";					//Название организации Строка (255)
	static final String PERSTITLEID = "rspostid";					//Идентификатор должности Число
	static final String PERSTITLENAME = "rspostidname";				//Название должности Строка (255)
	static final String PERSSEX = "ppsex";							//Пол (0 – Мужской, 1 – Женский). Если не задан, используется мужской. Малое
	static final String PERSMAIL = "personemail";					//E-mail (основной) Строка (255)
	static final String PERSSTATUS = "pstatus";						//Статус пользователя (0 – Активен, 1 – Архив, 2 – Гость, 4 - Кандидат) Малое
	static final String PERSEXTID = "pextcode";						//Код внешней системы Строка (255)
	static final String PERSBIRTHDATE = "ppbirthdate";				//Дата рождения
	static final String PERSWORKBEGINDATE = "pwcaworkbegindate";	//Дата начала работы в компании
	static final String PERSTITLEBEGINDATE = "pwbegindate";			//Дата приема на должность
	static final String PERSTYPE = "typersid";						//Тип физлица
	static final String PERSPHONE = "personworktel";				//Внутренний телефон физлица
	
	
	
	

	
	//Module PersonGroups (Группы физических лиц)
	static final String PATHTOPERSGROUPS = "/personGroups";
	static final String PERSGROUPMIRAID = "gcid";				//Идентификатор группы Число
	static final String PERSGROUPNAME = "gcname";				//Название группы Строка(100)
	static final String PERSGROUPDESCR = "gcdesc";				//Описание группы Строка(255)
	static final String PERSGROUPPARENTID = "gcparentid";		//Идентификатор родительской группы Число
	static final String PERSGROUPPARENTNAME = "gcparentidname";	//Название родительской группы Строка(100)
	static final String PERSGROUPKIND = "grkind";				//Вид группы (0 – динамическая, 1 – полудинамическая, 2 – статическая) Малое
	
	
	//Module Roles (Доступ)
	static final String PATHTOROLES = "/roles";
	static final String ROLEMIRAID = "roleid";				//Идентификатор роли Число
	static final String ROLEPROFILEID = "profileid";		//Идентификатор профиля роли Число
	static final String ROLENAME = "rolename";				//Название Строка (100)
	static final String SYSROLENAME = "sysrolename";		//Системное название Строка (255)
	static final String ROLEISDEFAULT = "roleisdefault";	//Присваивается данная роль по умолчанию пользователю при создании (true- да или false - нет) Логическое
	
	
	//Module cas (Организации)
	static final String PATHTOORGS = "/cas";
	static final String ORGMIRAID = "caid";							//Идентификатор организации Число
	static final String ORGNAME = "caname";							//Название Строка (255)
	static final String ORGPARENTID = "caparentid";					//Идентификатор родительской организации Число
	static final String ORGSHORTNAME = "cashortname";				//Короткое название организации Строка (100)
	static final String ORGEXTID = "castringcode";					//Код организации Строка (255)
	static final String ORGDIRECTORPERSID = "directorpersonid";		//ID руководителя организации
	
	
	//Module caGroups (Группы организаций)
	static final String PATHTOORGGROUPS = "/caGroups";
	static final String ORGGROUPMIRAID = "gcid";				//Идентификатор группы Число
	static final String ORGGROUPNAME = "gcname";				//Название группы Строка(100)
	static final String ORGGROUPDESCR = "gcdesc";				//Описание группы Строка(255)
	static final String ORGGROUPPARENTID = "gcparentid";		//Идентификатор родительской группы Число
	static final String ORGGROUPPARENTNAME = "gcparentidname";	//Название родительской группы Строка(100)
	static final String ORGGROUPKIND = "grkind";				//Вид группы (0 – Динамическая, 1 – Полудинамическая, 2 – Статическая) Малое

}
