package CDSOsobist.connectors.rest.ones.ZUP2;

public class EmployysHandler {

    // Сотрудники

    static final String EMP_NAME = "Description"; //Описание, отображаемое имя (__NAME__)
    static final String EMP_UID = "Ref_Key"; //Ключ ссылки на сотрудника (__UID__)
    static final String EMP_DEL_MARK = "DeletionMark"; //Пометка на удаление
    static final String EMP_PARENT_KEY = "Parent_Key"; //Ключ ссылки на родительский элемент
    static final String EMP_NUMBER = "Code"; //Табельный номер сотрудника
    static final String EMP_PERS_KEY = "Физлицо_Key"; //Ключ ссылки на физлицо
    static final String EMP_STATUS = "Актуальность"; //Актуальность (активность) сотрудника (OperationalAttributes.ENABLE)
    static final String EMP_ORG_KEY = "Организация_Key"; //Ключ ссылки на организацию сотрудника
    static final String EMP_OP_KEY = "ОбособленноеПодразделение_Key"; //Ключ ссылки на обособленное подразделение сотрудника
    static final String EMP_CONTR_TYPE = "ВидДоговора"; //Вид договора сотрудника
    static final String EMP_EMPLT_TYPE = "ВидЗанятости"; //Вид занятости
    static final String EMP_CONTR_NUMB = "НомерДоговора"; //Номер договора сотрудника
    static final String EMP_CONTR_DATE = "ДатаДоговора"; //Дата заключения договора с сотрудником
    static final String EMP_SHEDULE_KEY = "ГрафикРаботы_Key"; //Ключ ссылки на график работы сотрудника
    static final String EMP_ORG_UNIT_KEY = "ПодразделениеОрганизации_Key"; //Ключ ссылки на подразделение организации
    static final String EMP_POSITION_KEY = "Должность_Key"; //Ключ ссылки на должность сотрудника
    static final String EMP_RATES_NUM = "ЗанимаемыхСтавок"; //Количество занимаемых ставок
    static final String EMP_START_DATE = "ДатаНачала"; //Дата начала работы сотрудника
    static final String EMP_END_DATE = "ДатаОкончания"; //Дата окончания работы сотрудника
    static final String EMP_PROBATION = "ИспытательныйСрок"; //Испытательный срок, в месяцах
    static final String EMP_NAME_POSTFIX = "ПостфиксНаименования"; //Постфикс наименования сотрудника
    static final String EMP_CURR_OP_KEY = "ТекущееОбособленноеПодразделение_Key"; //Ключ ссылки на текущее обособленное подразделение
    static final String EMP_CURR_ORG_UNIT_KEY = "ТекущееПодразделениеОрганизации_Key"; //Ключ ссылки на текущее подразделение организации
    static final String EMP_CURR_POSITION_KEY = "ТекущаяДолжностьОрганизации_Key"; //Ключ ссылки на текущую должность организации
    static final String EMP_EMPLT_DATE = "ДатаПриемаНаРаботу"; //Дата приема на работу
    static final String EMP_DISM_DATE = "ДатаУвольнения"; //Дата увольнения
    static final String EMP_CURR_COMPANY_UNIT_KEY = "ТекущееПодразделениеКомпании_Key"; //Ключ ссылки на текущее подразделение компании
    static final String EMP_CURR_COMPANY_POSITION_KEY = "ТекущаяДолжностьКомпании_Key"; //Ключ ссылки на текущую должность компании
    static final String EMP_COMPANY_EMPLT_DATE = "ДатаПриемаНаРаботуВКомпанию"; //Дата приема на работу в компанию
    static final String EMP_COMPANY_DISM_DATE = "ДатаУвольненияИзКомпании"; //Дата увольнения из компании
    static final String EMP_ANNUAL_HOLIDAYS = "ЕжегодныеОтпуска"; //Перечень ежегодных отпусков
    static final String PERS_EMAIL = "Представление";

    //Контактная информация
    static final String CONTACTS = "/InformationRegister_КонтактнаяИнформация";
    static final String CONTACTS_TYPE_EMAIL_VALUE = "Представление";
}
