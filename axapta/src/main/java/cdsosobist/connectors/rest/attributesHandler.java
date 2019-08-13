package cdsosobist.connectors.rest;

public class attributesHandler {

    //Провайдеры
    static final String PROVIDERS = "/api/ProviderTable";
    static final String PROV_UID = "ProviderCodeId";
    static final String PROV_NAME = "ProviderName";

    //Организации
    static final String ORGANIZATIONS = "/api/VendProviderTable";
    static final String ORG_UID = "VendCodeId";
    static final String ORG_NAME = "VendName";

    //Сотрудники
    static final String EMPLOYEES = "/api/ProviderEmployee";
    static final String EMP_1C_UID = "ProviderCodeId";
    // --Commented out by Inspection (20.06.2019 19:55):static final String EMP_1C_PERS_UID = "GuidPhysicalPerson";
    static final String EMP_BIRTH_DATE = "BirthDate";
    static final String EMP_AX_UID = "ProviderEmplCodeId";
    static final String EMP_FIRST_NAME = "ProviderEmplFirstName";
    static final String EMP_LAST_NAME = "ProviderEmplLastName";
    static final String EMP_MIDDLE_NAME = "ProviderEmplMiddleName";
    static final String EMP_FULL_NAME = "ProviderEmplName";
    static final String EMP_INN = "INN";
    static final String EMP_SNILS = "PFRegNum";
    static final String EMP_MUNID = "mUnId";
    static final String EMP_1CDATA = "1сData";

    //Договора
    static final String CONTRACTS = "/api/ProviderCivilContract";
    static final String CONTR_AX_UID = "ProviderCivilContractCodeId";
    static final String CONTR_ORG_UID = "VendCodeId";
    static final String CONTR_AX_EMP_UID = "ProviderEmplCodeId";
    static final String CONTR_PROV_UID = "ProviderCodeId";
    static final String CONTR_FROM = "ValidFrom";
    static final String CONTR_TO = "ValidTo";
    static final String CONTR_UID = "CivilContractId";
    static final String CONTR_NUM = "CivilContractNum";
    static final String CONTR_DATE = "CivilContractData";
    static final String CONTR_1C_UID = "GuidContract";
    static final String CONTR_1CDATA = "1сData";

}
