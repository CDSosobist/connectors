package CDSOsobist.connectors.rest.ones.ZUP2;

public class ResourceHandler {
    //Справочники, регистры

    static final String EMPLOYEES = "/Catalog_СотрудникиОрганизаций";
    static final String POSITIONS = "/Catalog_ДолжностиОрганизаций";
    static final String ORGANISATIONS = "/Catalog_Организации";
    static final String ORGUNITS = "/Catalog_ПодразделенияОрганизаций";
    static final String MANAGERS = "/InformationRegister_ОтветственныеЛицаОрганизаций";
    static final String REQ_FORMAT = "?$format=json";
    static final String FIND_MAIL_REF_1 = "&$filter=Тип%20eq%20\'АдресЭлектроннойПочты\'%20and%20Объект%20eq%20cast(guid\'";
    static final String FIND_MAIL_REF_2 = "\',%20\'Catalog_ФизическиеЛица\')";
    static final String FIND_MANAGE_REF_1 = "&$filter=ФизическоеЛицо_Key%20eq%20guid\'";
    static final String FIND_MANAGE_REF_2 = "\'";
    static final String EMP_DETAILS_1 = "(guid'";
    static final String EMP_DETAILS_2 = "')";
    static final String SEPARATOR = "/";
    static final String MANAFEROF_OU_VALUE = "СтруктурнаяЕдиница";
    static final String MANAGEROF_TYPE = "ОтветственноеЛицо";
}
