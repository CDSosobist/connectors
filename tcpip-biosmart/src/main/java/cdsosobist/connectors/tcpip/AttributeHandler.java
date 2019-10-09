package cdsosobist.connectors.tcpip;

public class AttributeHandler {


    //Accounts
    public static final String ACC_UID = "id"; //идентификатор сотрудника
    public static final String ACC_FNAME = "first_name"; //имя
    public static final String ACC_LNAME = "last_name"; //фамилия
    public static final String ACC_MNAME = "middle_name"; //отчество
    public static final String ACC_CNUM = "clock_num"; //табельный номер
    public static final String ACC_ORG_ID = "org_id"; //идентификатор предприятия
    public static final String ACC_DEP_ID = "dep_id"; //идентификатор подразделения
    public static final String ACC_JOB_ID = "job_id"; //идентификатор должности
    public static final String ACC_BEG_DATE = "begin_date"; //дата начала работы
    public static final String ACC_END_DATE = "end_date"; //дата окончания работы
    public static final String ACC_BIRTH_DATE = "birthdate"; //дата рождения
    public static final String ACC_GENDER = "gender"; //пол (1 – мужской, 2 – женский, 0 – не задан)
    public static final String ACC_PHOTO = "photo"; //фотография (base64 )

    //AccessGroups
    public static final String AG_UID = "id"; //идентификатор группы доступа
    public static final String AG_NAME = "name"; //имя группы доступа

    //Organization
    public static final String ORG_UID = "id"; //идентификатор предприятия
    public static final String ORG_NAME = "name"; //наименование предприятия

    //Position
    public static final String POS_UID = "id"; //идентификатор должности
    public static final String POS_NAME = "name"; //наименование должности


    //Generic
    public static final String EXT_ID = "ext_id"; //Внешний идентификатор
    public static final String POLICY = "policy"; //Группа доступа сотрудника
}
