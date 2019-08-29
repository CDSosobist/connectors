package CDSOsobist.connectors.rest.ones.ZUP2;

class OrganisationsHandler {

    //Организации
    static final String ORG_NAME = "НаименованиеСокращенное"; //Сокращенное наименование организации
    static final String ORG_UID = "Ref_Key"; //Ключ ссылки на организацию, UID
    static final String ORG_DEL_MARK = "DeletionMark"; //Пометка на удаление
    static final String ORG_PREDEF = "Predefined"; //Предопределенная
    static final String ORG_CODE = "Code"; //Код организации в базе
    static final String ORG_DESCR = "Description"; //Опсиание, отображаемое им организации
    static final String ORG_PREFIX = "Префикс"; //Префикс организации
    static final String ORG_HEAD_ORG_KEY = "ГоловнаяОрганизация_Key"; //Ключ ссылки на головную организацию
    static final String ORG_FULLNAME = "НаименованиеПолное"; //Полное наименование организации

    //Обособленные подразделения
    static final String OP_NAME = "НаименованиеСокращенное";
    static final String OP_UID = "Ref_Key"; //Ключ ссылки на организацию, UID
    static final String OP_DEL_MARK = "DeletionMark"; //Пометка на удаление
    static final String OP_PREDEF = "Predefined"; //Предопределенная
    static final String OP_CODE = "Code"; //Код организации в базе
    static final String OP_DESCR = "Description"; //Опсиание, отображаемое им организации
    static final String OP_PREFIX = "Префикс"; //Префикс организации
    static final String OP_HEAD_ORG_KEY = "ГоловнаяОрганизация_Key"; //Ключ ссылки на головную организацию
    static final String OP_FULLNAME = "НаименованиеПолное"; //Полное наименование организации


    //Текущие обособленные подразделения
    static final String CURR_OP_NAME =  "НаименованиеСокращенное";
    static final String CURR_OP_UID = "Ref_Key"; //Ключ ссылки на организацию, UID
    static final String CURR_OP_DEL_MARK = "DeletionMark"; //Пометка на удаление
    static final String CURR_OP_PREDEF = "Predefined"; //Предопределенная
    static final String CURR_OP_CODE = "Code"; //Код организации в базе
    static final String CURR_OP_DESCR = "Description"; //Опсиание, отображаемое им организации
    static final String CURR_OP_PREFIX = "Префикс"; //Префикс организации
    static final String CURR_OP_HEAD_ORG_KEY = "ГоловнаяОрганизация_Key"; //Ключ ссылки на головную организацию
    static final String CURR_OP_FULLNAME = "НаименованиеПолное"; //Полное наименование организации


    //Подразделения организаций
    static final String ORGUNIT_NAME = "Description"; // __NAME__
    static final String ORGUNIT_UID = "Ref_Key"; //Ключ ссылки на подразделение организации
    static final String ORGUNIT_DEL_MARK = "DeletionMark"; //Пометка удаления
    static final String ORGUNIT_PREDEF = "Predefined"; //Предопределенное
    static final String ORGUNIT_OWNER_KEY = "Owner_Key"; //Ключ ссылки на владельца (организацию)
    static final String ORGUNIT_PARENT_KEY = "Parent_Key"; //Ключ ссылки на родительский элемент
    static final String ORGUNIT_CODE = "Code"; // Код подразделения организации в базе
    static final String ORGUNIT_ORDER = "Порядок"; //Порядок подразделения организации в иерархии
    static final String ORGUNIT_STATUS = "Актуальность"; //Статус активности подразделения организации  (OperationalAttributes.ENABLE)
    static final String ORGUNIT_OWNER_LINK = "Owner"; //Прямая ссылка на владельца (организацию)
    static final String ORGUNIT_PARENT_LINK = "Parent"; //Прямая ссылка на вышестоящее подразделение
}
