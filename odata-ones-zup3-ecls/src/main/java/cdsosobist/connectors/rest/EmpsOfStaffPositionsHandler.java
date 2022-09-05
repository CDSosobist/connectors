package cdsosobist.connectors.rest;

public class EmpsOfStaffPositionsHandler {
	static final String EMP_OF_STAFF_REC_TYPE = "Recorder_Type";
	static final String EMP_OF_STAFF_IS_ACTIVE = "Active";
	static final String EMP_OF_STAFF_EMP_KEY = "Сотрудник_Key";
	static final String EMP_OF_STAFF_HEAD_ORG_KEY = "ГоловнаяОрганизация_Key";
	static final String EMP_OF_STAFF_IND_KEY = "ФизическоеЛицо_Key";
	static final String EMP_OF_STAFF_STAFF_KEY = "ПозицияШтатногоРасписания_Key";
	static final String EMP_OF_STAFF_END_DATE = "ДействуетДо";
	static final String EMP_OF_STAFF_REPLACE_EMP_KEY = "ЗамещаемыйСотрудник_Key";
	static final String EMP_OF_STAFF_PLANNED_END_DATE = "ПланируемаяДатаЗавершения";
	
	static final String FIRST_PART_OF_EOSP_REQ = "/InformationRegister_ЗанятостьПозицийШтатногоРасписания_RecordType/SliceLast(Condition='ВидЗанятостиПозиции%20eq%20'Занята'%20and%20(ДействуетДо%20gt%20datetime'";
	static final String SECOND_PART_OF_EOSP_REQ = "'%20or%20ДействуетДо%20eq%20datetime'0001-01-01T00:00:00')%20and%20ПозицияШтатногоРасписания_Key%20eq%20guid'";
	static final String THIRD_PART_OF_EOSP_REQ = "'')?$format=json&$orderby=Period%20desc&$top=1";
}
