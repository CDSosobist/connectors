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
    static final String GPH = "/InformationRegister_ПериодыДействияДоговоровГражданскоПравовогоХарактера_RecordType";
    static final String FIO = "/InformationRegister_ФИОФизическихЛиц/SliceLast()";
    static final String EMPSTATUS = "/InformationRegister_ДанныеСостоянийСотрудников";
    static final String EMPS_OF_STAFF_POSITIONS_FIRST_PART = "/InformationRegister_ЗанятостьПозицийШтатногоРасписания_RecordType/SliceLast(Condition='ВидЗанятостиПозиции%20eq%20'Занята'%20and%20(ДействуетДо%20gt%20datetime'";
    static final String EMPS_OF_STAFF_POSITIONS_SECOND_PART = "'%20or%20ДействуетДо%20eq%20datetime'0001-01-01T00:00:00')')?$format=json&$orderby=Period%20desc";

    static final String INFOREG_REQ_1 = "&$filter=";
    static final String INFOREG_REQ_2 = "%20eq%20guid'";
    static final String INFOREG_REQ_3 = "'";
    static final String LASTEVENT_REQ_1 = "&Condition=Сотрудник_Key";
    static final String MAIN_JOB_REQ_1 = "&Condition=ФизическоеЛицо_Key";
    static final String MAIN_EMP_REQ_1 = "&$filter=ФизическоеЛицо_Key";
    static final String FIND_LAST_EVENT = "%27&$top=1&$orderby=Period%20desc";
    static final String FIND_EMP_LAST_EVENT = "&$top=1&$orderby=Period%20desc";
    static final String FILTER_MAIN_JOB = "%20and%20(Period%20gt%20datetime'2019-01-02T00:00:00'%20or%20Period%20lt%20datetime'1950-01-02T00:00:00')";
    static final String GUID_PART_1 = "(guid'";
    static final String GUID_PART_2 = "')";
    
    static final String MAIN_JOB_FINDER_PART_1 = "/InformationRegister_ВидыЗанятостиСотрудников_RecordType/SliceLast(,Condition=ФизическоеЛицо_Key%20eq%20guid'";
    static final String MAIN_JOB_FINDER_PART_2 = "')?$format=json&$filter=ВидЗанятости%20eq%20'ОсновноеМестоРаботы'%20or%20ВидЗанятости%20eq%20'Совместительство'&$orderby=ВидЗанятости";
    static final String MAIN_JOB_STATUS_PART_1 = "/InformationRegister_ДанныеСостоянийСотрудников?$format=json&$filter=RecordSet/any(d:%20d/Сотрудник_Key%20eq%20guid'";
    static final String MAIN_JOB_STATUS_PART_2 = "'%20and%20d/Состояние%20eq%20'Увольнение')";
    static final String DISMISSED_EMPS_FULL_REQ = "http://test-odata1c01.cds.spb/SVOD/odata/standard.odata/InformationRegister_ДанныеСостоянийСотрудников?$format=json&$filter=RecordSet/any(d:%20d/Состояние%20eq%20'Увольнение')&$select=RecordSet/Сотрудник_Key";
}
