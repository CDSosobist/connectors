package cdsosobist.connectors.rest;

public class resourceHandler {

    static final String COMPANYSTRUCTURE = "/Catalog_СтруктураПредприятия";
    static final String CURRENT_EMP_DATA = "/InformationRegister_КадроваяИсторияСотрудников_RecordType/SliceLast()";
    static final String EMPLOYEES = "/Catalog_Сотрудники";
    static final String MANAGERS = "/InformationRegister_ПозицииРуководителейПодразделений";
    static final String EMPLOYERS = "/Catalog_Работодатели";
    static final String EMP_ROLES = "/InformationRegister_РолиСотрудников";
    static final String INDIVIDUALS = "/Catalog_ФизическиеЛица";
    static final String MAIN_EMP_OF_INDIVIDUALS = "/InformationRegister_ОсновныеСотрудникиФизическихЛиц";
    static final String ORGANIZATIONS = "/Catalog_Организации";
    static final String ORGUNITS = "/Catalog_ПодразделенияОрганизаций";
    static final String POSITIONS = "/Catalog_Должности";
    static final String REQ_FORMAT = "?$format=json";
    static final String STAFFLIST = "/Catalog_ШтатноеРасписание";
    static final String STAFF_IN_CS = "/InformationRegister_МестоПозицииШтатногоРасписанияВСтруктуреПредприятия";
    static final String SUBORDINATION_OF_ORGANIZATIONS = "/InformationRegister_ПодчиненностьПодразделенийОрганизаций";
    static final String USERS = "/Catalog_Пользователи";
    static final String CONTACT_INFO = "/Catalog_ФизическиеЛица_КонтактнаяИнформация";
    static final String PHOTOS = "/InformationRegister_ФотографииФизическихЛиц";
    static final String MAIN_JOB = "/InformationRegister_ВидыЗанятостиСотрудников_RecordType/SliceLast()";

    static final String INFOREG_REQ_1 = "&$filter=";
    static final String INFOREG_REQ_2 = "%20eq%20guid'";
    static final String INFOREG_REQ_3 = "'";
    static final String LASTEVENT_REQ_1 = "&Condition=ФизическоеЛицо_Key";
    static final String FIND_LAST_EVENT = "%27&$top=1&$orderby=Period%20desc";
    static final String GUID_PART_1 = "(guid'";
    static final String GUID_PART_2 = "')";
}
