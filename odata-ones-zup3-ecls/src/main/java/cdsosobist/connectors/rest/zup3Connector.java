/*
 * Copyright (c) 2010-2014 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cdsosobist.connectors.rest;

import com.evolveum.polygon.rest.AbstractRestConnector;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.identityconnectors.framework.common.exceptions.ConnectorIOException;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.PoolableConnector;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static cdsosobist.connectors.rest.EmpStatusHandler.*;
import static cdsosobist.connectors.rest.FIOChangeHandler.*;
import static cdsosobist.connectors.rest.GphHandler.*;
import static cdsosobist.connectors.rest.MainJobHandler.*;
import static cdsosobist.connectors.rest.ManagerHandler.MAN_OU_KEY;
import static cdsosobist.connectors.rest.ManagerHandler.MAN_STAF_POS_KEY;
import static cdsosobist.connectors.rest.PhotoHandler.PHOTO_DATA;
import static cdsosobist.connectors.rest.PhotoHandler.PHOTO_IND_KEY;
import static cdsosobist.connectors.rest.StaffInCompStructureHandler.StuffInCSOU;
import static cdsosobist.connectors.rest.StaffInCompStructureHandler.StuffInCSPosition;
import static cdsosobist.connectors.rest.companyStructureHandler.*;
import static cdsosobist.connectors.rest.contactInfoHandler.*;
import static cdsosobist.connectors.rest.currentEmpDataHandler.*;
import static cdsosobist.connectors.rest.empRolesHandler.ER_EMP_KEY;
import static cdsosobist.connectors.rest.empRolesHandler.ER_EMP_ROLE;
import static cdsosobist.connectors.rest.employeesHandler.*;
import static cdsosobist.connectors.rest.individualsHandler.*;
import static cdsosobist.connectors.rest.mainEmpOfIndividualsHandler.*;
import static cdsosobist.connectors.rest.orgUnitsHandler.*;
import static cdsosobist.connectors.rest.organizationsHandler.*;
import static cdsosobist.connectors.rest.positionsHandler.*;
import static cdsosobist.connectors.rest.resourceHandler.*;
import static cdsosobist.connectors.rest.staffListHandler.*;
import static cdsosobist.connectors.rest.subordinationOfOrgHandler.*;
import static cdsosobist.connectors.rest.usersHandler.*;

@SuppressWarnings("unused")
@ConnectorClass(displayNameKey = "zup3.connector.display", configurationClass = zup3Configuration.class)
public class zup3Connector extends AbstractRestConnector<zup3Configuration>
		implements PoolableConnector, TestOp, SchemaOp, SearchOp<zup3Filter> {

	private static final Log LOG = Log.getLog(zup3Connector.class);

	public zup3Connector() throws IOException {

	}

	private final zup3Configuration configuration = (zup3Configuration) this.getConfiguration();

//    public CurrentEmpDataCache currentEmpDataCache;

	ZoneId timeZone = ZoneId.systemDefault();
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
	DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd.MM.yyyy'T'HH:mm:ss");
	
	LocalDateTime now = LocalDateTime.now();
	String currentDateTime = formatter.format(now);

	int indCounter = 0;
	int ADAccountCount = 0;

	ZonedDateTime borderDate = LocalDateTime.parse("2019-12-01T00:00:00", formatter).atZone(ZoneId.systemDefault());

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";

	@Override
	public void test() {
		LOG.ok("This getConfiguration: {0}, and (zup3Configuration)this.getConfiguration(): {1}", configuration,
				(zup3Configuration) this.getConfiguration());
		if (((zup3Configuration) this.getConfiguration()).getSkipTestConnection())
			LOG.ok("Тест отменен в конфигурации", new Object[0]);
		else {
			LOG.ok("Тестируем ресурс, запрашиваем пользователя по умолчанию", new Object[0]);

			try {
				LOG.ok("getConfiguration: {0}", (zup3Configuration) this.getConfiguration());
				HttpGet request = new HttpGet(
						((zup3Configuration) this.getConfiguration()).getServiceAddress() + REQ_FORMAT);
				LOG.ok("Запрос: {0}", request);
				this.callORequest(request);
			} catch (IOException ex) {
				throw new ConnectorIOException("Ошибка при попытке тестирования ресурса" + ex.getMessage(), ex);
			}
		}
	}

	@Override
	public void init(Configuration configuration) {
		super.init(configuration);

	}

	ZonedDateTime currZoneDT = ZonedDateTime.now();

	public Schema schema() {
		SchemaBuilder schemaBuilder = new SchemaBuilder(zup3Connector.class);
		this.buildAccountObjectClass(schemaBuilder);
		this.buildEmpHistorySliceObjectClass(schemaBuilder);
		this.buildCompanyStructureObjectClass(schemaBuilder);
		this.buildContactInfoObjectClass(schemaBuilder);
		this.buildEmpRoleObjectClass(schemaBuilder);
		this.buildIndividualObjectClass(schemaBuilder);
		this.buildMainEmpOfIndividualsObjectClass(schemaBuilder);
		this.buildOrganizationObjectClass(schemaBuilder);
		this.buildOrgUnitObjectClass(schemaBuilder);
		this.buildPositionObjectClass(schemaBuilder);
		this.buildStaffListObjectClass(schemaBuilder);
		this.buildStaffInCSObjectClass(schemaBuilder);
		this.buildSubOfOrgObjectClass(schemaBuilder);
		this.buildUserObjectClass(schemaBuilder);
		this.buildManagerObjectClass(schemaBuilder);
		this.buildPhotoObjectClass(schemaBuilder);
		this.buildMainJobObjectClass(schemaBuilder);
		this.buildGphObjectClass(schemaBuilder);
		this.buildFIOChangeClass(schemaBuilder);
		this.buildEmpStatusClass(schemaBuilder);
		return schemaBuilder.build();
	}

	private void buildAccountObjectClass(SchemaBuilder schemaBuilder) {
		ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();

		AttributeInfoBuilder attrEmpUidBuilder = new AttributeInfoBuilder(EMP_UID);
		ociBuilder.addAttributeInfo(attrEmpUidBuilder.build());

		AttributeInfoBuilder attrEmpDelMarkBuilder = new AttributeInfoBuilder(EMP_DEL_MARK);
		attrEmpDelMarkBuilder.setType(boolean.class);
		ociBuilder.addAttributeInfo(attrEmpDelMarkBuilder.build());

		AttributeInfoBuilder attrEmpCodeBuilder = new AttributeInfoBuilder(EMP_CODE);
		ociBuilder.addAttributeInfo(attrEmpCodeBuilder.build());

		AttributeInfoBuilder attrEmpDescriptionBuilder = new AttributeInfoBuilder(EMP_DESCRIPTION);
		ociBuilder.addAttributeInfo(attrEmpDescriptionBuilder.build());

		AttributeInfoBuilder attrEmpPersKeyBuilder = new AttributeInfoBuilder(EMP_PERS_KEY);
		ociBuilder.addAttributeInfo(attrEmpPersKeyBuilder.build());

		AttributeInfoBuilder attrEmpHeadOrgKeyBuilder = new AttributeInfoBuilder(EMP_HEAD_ORG_KEY);
		ociBuilder.addAttributeInfo(attrEmpHeadOrgKeyBuilder.build());

		AttributeInfoBuilder attrEmpInArchiveBuilder = new AttributeInfoBuilder(EMP_IN_ARCHIVE);
		attrEmpInArchiveBuilder.setType(boolean.class);
		ociBuilder.addAttributeInfo(attrEmpInArchiveBuilder.build());

		AttributeInfoBuilder attrEmpHeadEmpKeyBuilder = new AttributeInfoBuilder(EMP_HEAD_EMP_KEY);
		ociBuilder.addAttributeInfo(attrEmpHeadEmpKeyBuilder.build());

		AttributeInfoBuilder attrEmpPredefBuilder = new AttributeInfoBuilder(EMP_PREDEF);
		attrEmpPredefBuilder.setType(boolean.class);
		ociBuilder.addAttributeInfo(attrEmpPredefBuilder.build());

		AttributeInfoBuilder attrEmpNeedADBuilder = new AttributeInfoBuilder("УЗДляAD");
		ociBuilder.addAttributeInfo(attrEmpNeedADBuilder.build());

		schemaBuilder.defineObjectClass(ociBuilder.build());
	}

	private void buildEmpHistorySliceObjectClass(SchemaBuilder schemaBuilder) {
		ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
		ociBuilder.setType("EmpHistorySlice");

		AttributeInfoBuilder attrCedPersKeyBuilder = new AttributeInfoBuilder(CED_PERS_KEY);
		ociBuilder.addAttributeInfo(attrCedPersKeyBuilder.build());

		AttributeInfoBuilder attrCedEmpKeyBuilder = new AttributeInfoBuilder(CED_EMP_KEY);
		ociBuilder.addAttributeInfo(attrCedEmpKeyBuilder.build());

		AttributeInfoBuilder attrCedHeadOrgKeyBuilder = new AttributeInfoBuilder(CED_HEAD_ORG_KEY);
		ociBuilder.addAttributeInfo(attrCedHeadOrgKeyBuilder.build());

		AttributeInfoBuilder attrCedCurOrgKeyBuilder = new AttributeInfoBuilder(CED_CUR_ORG_KEY);
		ociBuilder.addAttributeInfo(attrCedCurOrgKeyBuilder.build());

		AttributeInfoBuilder attrCedCurOuKeyBuilder = new AttributeInfoBuilder(CED_CUR_OU_KEY);
		ociBuilder.addAttributeInfo(attrCedCurOuKeyBuilder.build());

		AttributeInfoBuilder attrCedCurPosKeyBuilder = new AttributeInfoBuilder(CED_CUR_POS_KEY);
		ociBuilder.addAttributeInfo(attrCedCurPosKeyBuilder.build());

		AttributeInfoBuilder attrCedCurPosInStaffKeyBuilder = new AttributeInfoBuilder(CED_CUR_POS_IN_STAFF_KEY);
		ociBuilder.addAttributeInfo(attrCedCurPosInStaffKeyBuilder.build());

		AttributeInfoBuilder attrCedIsHeadEmpBuilder = new AttributeInfoBuilder(CED_IS_HEAD_EMP);
		attrCedIsHeadEmpBuilder.setType(boolean.class);
		ociBuilder.addAttributeInfo(attrCedIsHeadEmpBuilder.build());

		AttributeInfoBuilder attrCedHeadEmpBuilder = new AttributeInfoBuilder(CED_HEAD_EMP);
		ociBuilder.addAttributeInfo(attrCedHeadEmpBuilder.build());

		AttributeInfoBuilder attrCedEventTypeBuilder = new AttributeInfoBuilder(CED_EVENT_TYPE);
		ociBuilder.addAttributeInfo(attrCedEventTypeBuilder.build());

		AttributeInfoBuilder attrCedEventDateBuilder = new AttributeInfoBuilder(CED_EVENT_DATE);
		attrCedEventDateBuilder.setType(ZonedDateTime.class);
		ociBuilder.addAttributeInfo(attrCedEventDateBuilder.build());

		AttributeInfoBuilder attrFakeDismissBuilder = new AttributeInfoBuilder("УволенДляПеревода");
		attrFakeDismissBuilder.setType(boolean.class);
		ociBuilder.addAttributeInfo(attrFakeDismissBuilder.build());

		schemaBuilder.defineObjectClass(ociBuilder.build());
	}

	private void buildManagerObjectClass(SchemaBuilder schemaBuilder) {
		ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
		ociBuilder.setType("Manager");

		AttributeInfoBuilder attrManOuKeyBuilder = new AttributeInfoBuilder(MAN_OU_KEY);
		ociBuilder.addAttributeInfo(attrManOuKeyBuilder.build());

		AttributeInfoBuilder attrManStafPosKeyBuilder = new AttributeInfoBuilder(MAN_STAF_POS_KEY);
		ociBuilder.addAttributeInfo(attrManStafPosKeyBuilder.build());

		schemaBuilder.defineObjectClass(ociBuilder.build());
	}

	private void buildCompanyStructureObjectClass(SchemaBuilder schemaBuilder) {
		ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
		ociBuilder.setType("CompanyStructure");

		AttributeInfoBuilder attrCsCompanyUidBuilder = new AttributeInfoBuilder(CS_COMPANY_UID);
		ociBuilder.addAttributeInfo(attrCsCompanyUidBuilder.build());

		AttributeInfoBuilder attrCsDelMarkBuilder = new AttributeInfoBuilder(CS_DEL_MARK);
		ociBuilder.addAttributeInfo(attrCsDelMarkBuilder.build());

		AttributeInfoBuilder attrCsParentKeyBuilder = new AttributeInfoBuilder(CS_PARENT_KEY);
		ociBuilder.addAttributeInfo(attrCsParentKeyBuilder.build());

		AttributeInfoBuilder attrCsCodeBuilder = new AttributeInfoBuilder(CS_CODE);
		ociBuilder.addAttributeInfo(attrCsCodeBuilder.build());

		AttributeInfoBuilder attrCsDescriptionBuilder = new AttributeInfoBuilder(CS_DESCRIPTION);
		ociBuilder.addAttributeInfo(attrCsDescriptionBuilder.build());

		AttributeInfoBuilder attrCsSourceBuilder = new AttributeInfoBuilder(CS_SOURCE);
		ociBuilder.addAttributeInfo(attrCsSourceBuilder.build());

		AttributeInfoBuilder attrCsCorrUlStructureBuilder = new AttributeInfoBuilder(CS_CORR_UL_STRUCTURE);
		attrCsCorrUlStructureBuilder.setType(boolean.class);
		ociBuilder.addAttributeInfo(attrCsCorrUlStructureBuilder.build());

		AttributeInfoBuilder attrCsPredefBuilder = new AttributeInfoBuilder(CS_PREDEF);
		ociBuilder.addAttributeInfo(attrCsPredefBuilder.build());

		AttributeInfoBuilder attrCsNestingBuilder = new AttributeInfoBuilder("Nesting");
		ociBuilder.addAttributeInfo(attrCsNestingBuilder.build());

		ociBuilder.addAttributeInfo(OperationalAttributeInfos.ENABLE);

		schemaBuilder.defineObjectClass(ociBuilder.build());
	}

	private void buildContactInfoObjectClass(SchemaBuilder schemaBuilder) {
		ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
		ociBuilder.setType("ContactInfo");

		AttributeInfoBuilder attrCifUidBuilder = new AttributeInfoBuilder(CIF_UID);
		ociBuilder.addAttributeInfo(attrCifUidBuilder.build());

		AttributeInfoBuilder attrCifTypeBuilder = new AttributeInfoBuilder(CIF_TYPE);
		ociBuilder.addAttributeInfo(attrCifTypeBuilder.build());

		AttributeInfoBuilder attrCifValueBuilder = new AttributeInfoBuilder(CIF_VALUE);
		ociBuilder.addAttributeInfo(attrCifValueBuilder.build());

		schemaBuilder.defineObjectClass(ociBuilder.build());
	}

	private void buildEmpRoleObjectClass(SchemaBuilder schemaBuilder) {
		ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
		ociBuilder.setType("EmpRole");

		AttributeInfoBuilder attrErEmpKeyBuilder = new AttributeInfoBuilder(ER_EMP_KEY);
		ociBuilder.addAttributeInfo(attrErEmpKeyBuilder.build());

		AttributeInfoBuilder attrErEmpRoleBuilder = new AttributeInfoBuilder(ER_EMP_ROLE);
		ociBuilder.addAttributeInfo(attrErEmpRoleBuilder.build());

		schemaBuilder.defineObjectClass(ociBuilder.build());
	}

	private void buildIndividualObjectClass(SchemaBuilder schemaBuilder) {
		ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
		ociBuilder.setType("Individual");

		AttributeInfoBuilder attrParentKeyBuilder = new AttributeInfoBuilder(IND_PARENT_KEY);
		ociBuilder.addAttributeInfo(attrParentKeyBuilder.build());

		AttributeInfoBuilder attrIsFolderBuilder = new AttributeInfoBuilder(IND_IS_FOLDER);
		attrIsFolderBuilder.setType(boolean.class);
		ociBuilder.addAttributeInfo(attrIsFolderBuilder.build());

		AttributeInfoBuilder attrIndUidBuilder = new AttributeInfoBuilder(IND_UID);
		ociBuilder.addAttributeInfo(attrIndUidBuilder.build());

		AttributeInfoBuilder attrIndDescriptionBuilder = new AttributeInfoBuilder(IND_DESCRIPTION);
		ociBuilder.addAttributeInfo(attrIndDescriptionBuilder.build());

		AttributeInfoBuilder attrIndAccessGroupKeyBuilder = new AttributeInfoBuilder(IND_ACCESS_GROUP_KEY);
		ociBuilder.addAttributeInfo(attrIndAccessGroupKeyBuilder.build());

		AttributeInfoBuilder attrIndGenderBuilder = new AttributeInfoBuilder(IND_GENDER);
		ociBuilder.addAttributeInfo(attrIndGenderBuilder.build());

		AttributeInfoBuilder attrIndFullNameBuilder = new AttributeInfoBuilder(IND_FULLNAME);
		ociBuilder.addAttributeInfo(attrIndFullNameBuilder.build());

		AttributeInfoBuilder attrIndMiddleNameBuilder = new AttributeInfoBuilder(IND_MIDDLE_NAME);
		ociBuilder.addAttributeInfo(attrIndMiddleNameBuilder.build());

		AttributeInfoBuilder attrIndSurNameBuilder = new AttributeInfoBuilder(IND_SURNAME);
		ociBuilder.addAttributeInfo(attrIndSurNameBuilder.build());

		AttributeInfoBuilder attrIndNameBuilder = new AttributeInfoBuilder(IND_NAME);
		ociBuilder.addAttributeInfo(attrIndNameBuilder.build());

		AttributeInfoBuilder attrIndBirthdateBuilder = new AttributeInfoBuilder(IND_BIRTHDATE);
		attrIndBirthdateBuilder.setType(ZonedDateTime.class);
		ociBuilder.addAttributeInfo(attrIndBirthdateBuilder.build());

		AttributeInfoBuilder attrIndINNBuilder = new AttributeInfoBuilder(IND_INN);
		ociBuilder.addAttributeInfo(attrIndINNBuilder.build());

		AttributeInfoBuilder attrIndSNILSBuilder = new AttributeInfoBuilder(IND_SNILS);
		ociBuilder.addAttributeInfo(attrIndSNILSBuilder.build());

		AttributeInfoBuilder attrBorderDateBuilder = new AttributeInfoBuilder("borderDate");
		attrBorderDateBuilder.setType(ZonedDateTime.class);
		ociBuilder.addAttributeInfo(attrBorderDateBuilder.build());

		AttributeInfoBuilder attrIndNeedADAccBuilder = new AttributeInfoBuilder("ADAccount");
		attrIndNeedADAccBuilder.setType(Boolean.class);
		ociBuilder.addAttributeInfo(attrIndNeedADAccBuilder.build());

		schemaBuilder.defineObjectClass(ociBuilder.build());
	}

	private void buildMainEmpOfIndividualsObjectClass(SchemaBuilder schemaBuilder) {
		ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
		ociBuilder.setType("MainEmpOfIndividuals");

		AttributeInfoBuilder attrMeiHeadOrgKeyBuilder = new AttributeInfoBuilder(MEI_HEAD_ORG_KEY);
		ociBuilder.addAttributeInfo(attrMeiHeadOrgKeyBuilder.build());

		AttributeInfoBuilder attrMeiPersKeyBuilder = new AttributeInfoBuilder(MEI_PERS_KEY);
		ociBuilder.addAttributeInfo(attrMeiPersKeyBuilder.build());

		AttributeInfoBuilder attrMeiStartDateBuilder = new AttributeInfoBuilder(MEI_START_DATE);
		attrMeiStartDateBuilder.setType(ZonedDateTime.class);
		ociBuilder.addAttributeInfo(attrMeiStartDateBuilder.build());

		AttributeInfoBuilder attrMeiEndDateBuilder = new AttributeInfoBuilder(MEI_END_DATE);
		attrMeiEndDateBuilder.setType(ZonedDateTime.class);
		ociBuilder.addAttributeInfo(attrMeiEndDateBuilder.build());

		AttributeInfoBuilder attrMeiEmpKeyBuilder = new AttributeInfoBuilder(MEI_EMP_KEY);
		ociBuilder.addAttributeInfo(attrMeiEmpKeyBuilder.build());

		schemaBuilder.defineObjectClass(ociBuilder.build());
	}

	private void buildOrganizationObjectClass(SchemaBuilder schemaBuilder) {
		ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
		ociBuilder.setType("Org");

		AttributeInfoBuilder attrOrgUidBuilder = new AttributeInfoBuilder(ORG_UID);
		ociBuilder.addAttributeInfo(attrOrgUidBuilder.build());

		AttributeInfoBuilder attrOrgDelMarkBuilder = new AttributeInfoBuilder(ORG_DEL_MARK);
		attrOrgDelMarkBuilder.setType(boolean.class);
		ociBuilder.addAttributeInfo(attrOrgDelMarkBuilder.build());

		AttributeInfoBuilder attrOrgDescriptionBuilder = new AttributeInfoBuilder(ORG_DESCRIPTION);
		ociBuilder.addAttributeInfo(attrOrgDescriptionBuilder.build());

		AttributeInfoBuilder attrOrgHeadOrgKeyBuilder = new AttributeInfoBuilder(ORG_HEAD_ORG_KEY);
		ociBuilder.addAttributeInfo(attrOrgHeadOrgKeyBuilder.build());

		AttributeInfoBuilder attrOrgHaveSeparateDivisionBuilder = new AttributeInfoBuilder(ORG_HAVE_SEPARATE_DIVISION);
		attrOrgHaveSeparateDivisionBuilder.setType(boolean.class);
		ociBuilder.addAttributeInfo(attrOrgHaveSeparateDivisionBuilder.build());

		AttributeInfoBuilder attrOrgFullnameBuilder = new AttributeInfoBuilder(ORG_FULLNAME);
		ociBuilder.addAttributeInfo(attrOrgFullnameBuilder.build());

		AttributeInfoBuilder attrOrgShortnameBuilder = new AttributeInfoBuilder(ORG_SHORTNAME);
		ociBuilder.addAttributeInfo(attrOrgShortnameBuilder.build());

		AttributeInfoBuilder attrOrgIsSeparateDivisionBuilder = new AttributeInfoBuilder(ORG_IS_SEPARATE_DIVISION);
		attrOrgIsSeparateDivisionBuilder.setType(boolean.class);
		ociBuilder.addAttributeInfo(attrOrgIsSeparateDivisionBuilder.build());

		AttributeInfoBuilder attrOrgPrefixBuilder = new AttributeInfoBuilder(ORG_PREFIX);
		ociBuilder.addAttributeInfo(attrOrgPrefixBuilder.build());

		AttributeInfoBuilder attrOrgLegalPhysicalPersonBuilder = new AttributeInfoBuilder(ORG_LEGAL_PHYSICAL_PERSON);
		ociBuilder.addAttributeInfo(attrOrgLegalPhysicalPersonBuilder.build());

		schemaBuilder.defineObjectClass(ociBuilder.build());
	}

	private void buildOrgUnitObjectClass(SchemaBuilder schemaBuilder) {
		ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
		ociBuilder.setType("OrgUnit");

		AttributeInfoBuilder attrOuUidBuilder = new AttributeInfoBuilder(OU_UID);
		ociBuilder.addAttributeInfo(attrOuUidBuilder.build());

		AttributeInfoBuilder attrOuDelMarkBuilder = new AttributeInfoBuilder(OU_DEL_MARK);
		attrOuDelMarkBuilder.setType(boolean.class);
		ociBuilder.addAttributeInfo(attrOuDelMarkBuilder.build());

		AttributeInfoBuilder attrOuOwnerKeyBuilder = new AttributeInfoBuilder(OU_OWNER_KEY);
		ociBuilder.addAttributeInfo(attrOuOwnerKeyBuilder.build());

		AttributeInfoBuilder attrOuParentKeyBuilder = new AttributeInfoBuilder(OU_PARENT_KEY);
		ociBuilder.addAttributeInfo(attrOuParentKeyBuilder.build());

		AttributeInfoBuilder attrOuCodeBuilder = new AttributeInfoBuilder(OU_CODE);
		ociBuilder.addAttributeInfo(attrOuCodeBuilder.build());

		AttributeInfoBuilder attrOuDescriptionBuilder = new AttributeInfoBuilder(OU_DESCRIPTION);
		ociBuilder.addAttributeInfo(attrOuDescriptionBuilder.build());

		AttributeInfoBuilder attrOuIsSeparateDivisionBuilder = new AttributeInfoBuilder(OU_IS_SEPARATE_DIVISION);
		attrOuIsSeparateDivisionBuilder.setType(boolean.class);
		ociBuilder.addAttributeInfo(attrOuIsSeparateDivisionBuilder.build());

		AttributeInfoBuilder attrOuFormedBuilder = new AttributeInfoBuilder(OU_FORMED);
		ociBuilder.addAttributeInfo(attrOuFormedBuilder.build());

		AttributeInfoBuilder attrOuDisbandedBuilder = new AttributeInfoBuilder(OU_DISBANDED);
		ociBuilder.addAttributeInfo(attrOuDisbandedBuilder.build());

		AttributeInfoBuilder attrOuHeadOrgBuilder = new AttributeInfoBuilder(OU_HEAD_ORG);
		ociBuilder.addAttributeInfo(attrOuHeadOrgBuilder.build());

		AttributeInfoBuilder attrOuPredefBuilder = new AttributeInfoBuilder(OU_PREDEF);
		attrOuPredefBuilder.setType(boolean.class);
		ociBuilder.addAttributeInfo(attrOuPredefBuilder.build());

		schemaBuilder.defineObjectClass(ociBuilder.build());
	}

	private void buildPositionObjectClass(SchemaBuilder schemaBuilder) {
		ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
		ociBuilder.setType("Position");

		AttributeInfoBuilder attrPosUidBuilder = new AttributeInfoBuilder(POS_UID);
		ociBuilder.addAttributeInfo(attrPosUidBuilder.build());

		AttributeInfoBuilder attrPosDelMarkBuilder = new AttributeInfoBuilder(POS_DEL_MARK);
		attrPosDelMarkBuilder.setType(boolean.class);
		ociBuilder.addAttributeInfo(attrPosDelMarkBuilder.build());

		AttributeInfoBuilder attrPosDescriptionBuilder = new AttributeInfoBuilder(POS_DESCRIPTION);
		ociBuilder.addAttributeInfo(attrPosDescriptionBuilder.build());

		AttributeInfoBuilder attrPosExcludedFromStafflistBuilder = new AttributeInfoBuilder(
				POS_EXCLUDED_FROM_STAFFLIST);
		ociBuilder.addAttributeInfo(attrPosExcludedFromStafflistBuilder.build());

		AttributeInfoBuilder attrPosPredefBuilder = new AttributeInfoBuilder(POS_PREDEF);
		attrPosPredefBuilder.setType(boolean.class);
		ociBuilder.addAttributeInfo(attrPosPredefBuilder.build());

		schemaBuilder.defineObjectClass(ociBuilder.build());
	}

	private void buildStaffListObjectClass(SchemaBuilder schemaBuilder) {
		ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
		ociBuilder.setType("StaffList");

		AttributeInfoBuilder attrSlUidBuilder = new AttributeInfoBuilder(SL_UID);
		ociBuilder.addAttributeInfo(attrSlUidBuilder.build());

		AttributeInfoBuilder attrSlDelMarkBuilder = new AttributeInfoBuilder(SL_DEL_MARK);
		attrSlDelMarkBuilder.setType(boolean.class);
		ociBuilder.addAttributeInfo(attrSlDelMarkBuilder.build());

		AttributeInfoBuilder attrSlOwnerKeyBuilder = new AttributeInfoBuilder(SL_OWNER_KEY);
		ociBuilder.addAttributeInfo(attrSlOwnerKeyBuilder.build());

		AttributeInfoBuilder attrSlParentKeyBuilder = new AttributeInfoBuilder(SL_PARENT_KEY);
		ociBuilder.addAttributeInfo(attrSlParentKeyBuilder.build());

		AttributeInfoBuilder attrSlDescriptionBuilder = new AttributeInfoBuilder(SL_DESCRIPTION);
		ociBuilder.addAttributeInfo(attrSlDescriptionBuilder.build());

		AttributeInfoBuilder attrSlOuKeyBuilder = new AttributeInfoBuilder(SL_OU_KEY);
		ociBuilder.addAttributeInfo(attrSlOuKeyBuilder.build());

		AttributeInfoBuilder attrSlPositionKeyBuilder = new AttributeInfoBuilder(SL_POSITION_KEY);
		ociBuilder.addAttributeInfo(attrSlPositionKeyBuilder.build());

		schemaBuilder.defineObjectClass(ociBuilder.build());
	}

	private void buildStaffInCSObjectClass(SchemaBuilder schemaBuilder) {
		ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
		ociBuilder.setType("StaffInCS");

		AttributeInfoBuilder attrSlUidBuilder = new AttributeInfoBuilder(StuffInCSPosition);
		ociBuilder.addAttributeInfo(attrSlUidBuilder.build());

		AttributeInfoBuilder attrSlOwnerKeyBuilder = new AttributeInfoBuilder(StuffInCSOU);
		ociBuilder.addAttributeInfo(attrSlOwnerKeyBuilder.build());

		AttributeInfoBuilder attrSlDescriptionBuilder = new AttributeInfoBuilder("Description");
		ociBuilder.addAttributeInfo(attrSlDescriptionBuilder.build());

		schemaBuilder.defineObjectClass(ociBuilder.build());
	}

	private void buildSubOfOrgObjectClass(SchemaBuilder schemaBuilder) {
		ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
		ociBuilder.setType("SubOfOrg");

		AttributeInfoBuilder attrSuboOuKeyBuilder = new AttributeInfoBuilder(SUBO_OU_KEY);
		ociBuilder.addAttributeInfo(attrSuboOuKeyBuilder.build());

		AttributeInfoBuilder attrSuboParOrgKeyBuilder = new AttributeInfoBuilder(SUBO_PAR_ORG_KEY);
		ociBuilder.addAttributeInfo(attrSuboParOrgKeyBuilder.build());

		AttributeInfoBuilder attrSuboOrgKeyBuilder = new AttributeInfoBuilder(SUBO_ORG_KEY);
		ociBuilder.addAttributeInfo(attrSuboOrgKeyBuilder.build());

		schemaBuilder.defineObjectClass(ociBuilder.build());
	}

	private void buildUserObjectClass(SchemaBuilder schemaBuilder) {
		ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
		ociBuilder.setType("User");

		AttributeInfoBuilder attrUserUidBuilder = new AttributeInfoBuilder(USER_UID);
		ociBuilder.addAttributeInfo(attrUserUidBuilder.build());

		AttributeInfoBuilder attrUserDelMarkBuilder = new AttributeInfoBuilder(USER_DEL_MARK);
		attrUserDelMarkBuilder.setType(boolean.class);
		ociBuilder.addAttributeInfo(attrUserDelMarkBuilder.build());

		AttributeInfoBuilder attrUserDescriptionBuilder = new AttributeInfoBuilder(USER_DESCRIPTION);
		ociBuilder.addAttributeInfo(attrUserDescriptionBuilder.build());

		AttributeInfoBuilder attrUserNotValidBuilder = new AttributeInfoBuilder(USER_NOT_VALID);
		attrUserNotValidBuilder.setType(boolean.class);
		ociBuilder.addAttributeInfo(attrUserNotValidBuilder.build());

		AttributeInfoBuilder attrUserOuKeyBuilder = new AttributeInfoBuilder(USER_OU_KEY);
		ociBuilder.addAttributeInfo(attrUserOuKeyBuilder.build());

		AttributeInfoBuilder attrUserPersKeyBuilder = new AttributeInfoBuilder(USER_PERS_KEY);
		ociBuilder.addAttributeInfo(attrUserPersKeyBuilder.build());

		AttributeInfoBuilder attrUserServiceBuilder = new AttributeInfoBuilder(USER_SERVICE);
		ociBuilder.addAttributeInfo(attrUserServiceBuilder.build());

		AttributeInfoBuilder attrUserPreparedBuilder = new AttributeInfoBuilder(USER_PREPARED);
		attrUserPreparedBuilder.setType(boolean.class);
		ociBuilder.addAttributeInfo(attrUserPreparedBuilder.build());

		AttributeInfoBuilder attrUserBdIdBuilder = new AttributeInfoBuilder(USER_BD_ID);
		ociBuilder.addAttributeInfo(attrUserBdIdBuilder.build());

		AttributeInfoBuilder attrUserPredefBuilder = new AttributeInfoBuilder(USER_PREDEF);
		attrUserPredefBuilder.setType(boolean.class);
		ociBuilder.addAttributeInfo(attrUserPredefBuilder.build());

		schemaBuilder.defineObjectClass(ociBuilder.build());
	}

	private void buildPhotoObjectClass(SchemaBuilder schemaBuilder) {
		ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
		ociBuilder.setType("Photo");

		AttributeInfoBuilder attrPhotoIndKeyBuilder = new AttributeInfoBuilder(PHOTO_IND_KEY);
		ociBuilder.addAttributeInfo(attrPhotoIndKeyBuilder.build());

		AttributeInfoBuilder attrPhotoDataBuilder = new AttributeInfoBuilder(PHOTO_DATA);
		attrPhotoDataBuilder.setType(byte.class);
		ociBuilder.addAttributeInfo(attrPhotoDataBuilder.build());

		schemaBuilder.defineObjectClass(ociBuilder.build());
	}

	private void buildMainJobObjectClass(SchemaBuilder schemaBuilder) {
		ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
		ociBuilder.setType("MainJob");

		AttributeInfoBuilder attrMainJobEmpBuilder = new AttributeInfoBuilder(MAIN_JOB_EMP);
		ociBuilder.addAttributeInfo(attrMainJobEmpBuilder.build());

		AttributeInfoBuilder attrMainJobEmpTypeBuilder = new AttributeInfoBuilder(MAIN_JOB_EMP_TYPE);
		ociBuilder.addAttributeInfo(attrMainJobEmpTypeBuilder.build());

		AttributeInfoBuilder attrMainJobHeadOrgBuilder = new AttributeInfoBuilder(MAIN_JOB_HEAD_ORG);
		ociBuilder.addAttributeInfo(attrMainJobHeadOrgBuilder.build());

		AttributeInfoBuilder attrMainJobIndividualBuilder = new AttributeInfoBuilder(MAIN_JOB_INDIVIDUAL);
		ociBuilder.addAttributeInfo(attrMainJobIndividualBuilder.build());

		AttributeInfoBuilder attrMainJobStartDateBuilder = new AttributeInfoBuilder("Period");
		attrMainJobStartDateBuilder.setType(ZonedDateTime.class);
		ociBuilder.addAttributeInfo(attrMainJobStartDateBuilder.build());

		schemaBuilder.defineObjectClass(ociBuilder.build());
	}

	private void buildGphObjectClass(SchemaBuilder schemaBuilder) {
		ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
		ociBuilder.setType("GPH");

		AttributeInfoBuilder attrGphOrgKeyBuilder = new AttributeInfoBuilder(GPH_ORG);
		ociBuilder.addAttributeInfo(attrGphOrgKeyBuilder.build());

		AttributeInfoBuilder attrGphEmpKeyBuilder = new AttributeInfoBuilder(GPH_EMP_KEY);
		ociBuilder.addAttributeInfo(attrGphEmpKeyBuilder.build());

		AttributeInfoBuilder attrGphIndKeyBuilder = new AttributeInfoBuilder(GPH_IND_KEY);
		ociBuilder.addAttributeInfo(attrGphIndKeyBuilder.build());

		AttributeInfoBuilder attrGphValidFromBuilder = new AttributeInfoBuilder(GPH_VALID_FROM);
		attrGphValidFromBuilder.setType(ZonedDateTime.class);
		ociBuilder.addAttributeInfo(attrGphValidFromBuilder.build());

		AttributeInfoBuilder attrGphValidToBuilder = new AttributeInfoBuilder(GPH_VALID_TO);
		attrGphValidToBuilder.setType(ZonedDateTime.class);
		ociBuilder.addAttributeInfo(attrGphValidToBuilder.build());

		schemaBuilder.defineObjectClass(ociBuilder.build());
	}

	private void buildFIOChangeClass(SchemaBuilder schemaBuilder) {
		ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
		ociBuilder.setType("FIO");

		AttributeInfoBuilder attrFIOPeriodBuilder = new AttributeInfoBuilder(FIO_PERIOD);
		attrFIOPeriodBuilder.setType(ZonedDateTime.class);
		ociBuilder.addAttributeInfo(attrFIOPeriodBuilder.build());

		AttributeInfoBuilder attrFIOPersonaBuilder = new AttributeInfoBuilder(FIO_PERSONA);
		ociBuilder.addAttributeInfo(attrFIOPersonaBuilder.build());

		AttributeInfoBuilder attrFIOFNameBuilder = new AttributeInfoBuilder(FIO_FNAME);
		ociBuilder.addAttributeInfo(attrFIOFNameBuilder.build());

		AttributeInfoBuilder attrFIOGNameBuilder = new AttributeInfoBuilder(FIO_GNAME);
		ociBuilder.addAttributeInfo(attrFIOGNameBuilder.build());

		AttributeInfoBuilder attrFIOANameBuilder = new AttributeInfoBuilder(FIO_ANAME);
		ociBuilder.addAttributeInfo(attrFIOANameBuilder.build());

		schemaBuilder.defineObjectClass(ociBuilder.build());
	}

	private void buildEmpStatusClass(SchemaBuilder schemaBuilder) {
		ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
		ociBuilder.setType("EmpStatus");

		AttributeInfoBuilder attrEmpStatusRecorderBuilder = new AttributeInfoBuilder(EMP_STATUS_RECORDER);
		ociBuilder.addAttributeInfo(attrEmpStatusRecorderBuilder.build());

		AttributeInfoBuilder attrEmpStatusRecorderTypeBuilder = new AttributeInfoBuilder(EMP_STATUS_RECORDER_TYPE);
		ociBuilder.addAttributeInfo(attrEmpStatusRecorderTypeBuilder.build());

		AttributeInfoBuilder attrEmpStatusKeyBuilder = new AttributeInfoBuilder(EMP_STATUS_KEY);
		ociBuilder.addAttributeInfo(attrEmpStatusKeyBuilder.build());

		AttributeInfoBuilder attrEmpStatusValueBuilder = new AttributeInfoBuilder(EMP_STATUS_VALUE);
		ociBuilder.addAttributeInfo(attrEmpStatusValueBuilder.build());

		AttributeInfoBuilder attrEmpStatusStartDateBuilder = new AttributeInfoBuilder(EMP_STATUS_START_DATE);
//    	attrEmpStatusStartDateBuilder.setType(ZonedDateTime.class);
		ociBuilder.addAttributeInfo(attrEmpStatusStartDateBuilder.build());

		AttributeInfoBuilder attrEmpStatusEndDateBuilder = new AttributeInfoBuilder(EMP_STATUS_END_DATE);
//    	attrEmpStatusEndDateBuilder.setType(ZonedDateTime.class);
		ociBuilder.addAttributeInfo(attrEmpStatusEndDateBuilder.build());

		AttributeInfoBuilder attrEmpStatusEstimatedEndDateBuilder = new AttributeInfoBuilder(
				EMP_STATUS_ESTIMATED_END_DATE);
//    	attrEmpStatusEstimatedEndDateBuilder.setType(ZonedDateTime.class);
		ociBuilder.addAttributeInfo(attrEmpStatusEstimatedEndDateBuilder.build());

		schemaBuilder.defineObjectClass(ociBuilder.build());
	}

	private JSONObject callORequest(HttpRequestBase request) throws IOException {
		request.setHeader("Content-Type", "application/json");
		this.authHeader(request);
		CloseableHttpResponse response;
		response = this.execute(request);
		this.processYResponseErrors(response);
		String result = EntityUtils.toString(response.getEntity());
		this.closeResponse(response);
		return new JSONObject(result);
	}

	private void authHeader(HttpRequestBase request) {
		final StringBuilder sb = new StringBuilder();
		if (((zup3Configuration) this.getConfiguration()).getPassword() != null) {
			((zup3Configuration) this.getConfiguration()).getPassword().access(chars -> sb.append(new String(chars)));
			byte[] credentials = Base64.getEncoder()
					.encode((((zup3Configuration) this.getConfiguration()).getUsername() + ":" + sb.toString())
							.getBytes(StandardCharsets.UTF_8));
			request.setHeader("Authorization", "Basic " + new String(credentials, StandardCharsets.UTF_8));
		} else
			LOG.error("getPassword: {0}", ((zup3Configuration) this.getConfiguration()).getPassword());
	}

	JSONArray callRequest(HttpRequestBase request) throws IOException {
		request.setHeader("Content-Type", "application/json");
		this.authHeader(request);
		CloseableHttpResponse response = this.execute(request);
		this.processYResponseErrors(response);
		String result = EntityUtils.toString(response.getEntity());
		this.closeResponse(response);
		return new JSONObject(result).getJSONArray("value");
	}

	private void processYResponseErrors(CloseableHttpResponse response) {
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode == 406) {
			String result;

			try {
				result = EntityUtils.toString(response.getEntity());
			} catch (IOException var9) {
				throw new ConnectorIOException("Error when trying to get response entity: " + response, var9);
			}

			if (!result.contains("There is no user with ID") && !result.contains("There is no term with ID")
					&& !result.contains("There is no ")) {
				JSONObject err;
				try {
					JSONObject jo = new JSONObject(result);
					err = jo.getJSONObject("form_errors");
				} catch (JSONException var8) {
					this.closeResponse(response);
					throw new ConnectorIOException(var8.getMessage() + " when parsing result: " + result, var8);
				}

				if (err.has("name")) {
					this.closeResponse(response);
					throw new AlreadyExistsException(err.getString("name"));
				} else if (err.has(EMP_UID)) {
					this.closeResponse(response);
					throw new AlreadyExistsException(err.getString(EMP_UID));
				} else if (err.has(EMP_UID)) {
					this.closeResponse(response);
					throw new AlreadyExistsException(err.getString(EMP_UID));
				} else {
					if (err.length() > 0) {

						for (String key : err.keySet()) {
							String value = err.getString(key);
							if (value != null && value.contains("field is required.")) {
								this.closeResponse(response);
								throw new InvalidAttributeValueException(
										"Missing mandatory attribute " + key + ", full message: " + err);
							}
						}
					}

					this.closeResponse(response);
					throw new ConnectorIOException("Error when process response: " + result);
				}
			} else {
				this.closeResponse(response);
				throw new UnknownUidException(result);
			}
		} else {
			super.processResponseErrors(response);
		}
	}

	public FilterTranslator<zup3Filter> createFilterTranslator(ObjectClass objectClass,
			OperationOptions operationOptions) {
		return new zup3FilterTranslator();
	}

	public void executeQuery(ObjectClass objectClass, zup3Filter query, ResultsHandler handler,
			OperationOptions options) {
		LOG.info("\n\n\nObjectClass: {0}\n\n\n", objectClass);
		if (objectClass.is("__ACCOUNT__")) {
			HttpGet request = new HttpGet(
					((zup3Configuration) this.getConfiguration()).getServiceAddress() + EMPLOYEES + REQ_FORMAT);
			try {
				this.handleEmployees(request, query, handler, options);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else if (objectClass.is("EmpHistorySlice")) {
			HttpGet request = new HttpGet(
					((zup3Configuration) this.getConfiguration()).getServiceAddress() + CURRENT_EMP_DATA + REQ_FORMAT + "&Condition=ДействуетДо%20gt%20datetime'" + currentDateTime + "'%20or%20ДействуетДо%20eq%20datetime'0001-01-01T00:00:00'");
			try {
				this.handleEmpHistorySlice(request, query, handler, options);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else if (objectClass.is("Position")) {
			HttpGet request = new HttpGet(
					((zup3Configuration) this.getConfiguration()).getServiceAddress() + POSITIONS + REQ_FORMAT);
			try {
				this.handlePositions(request, query, handler, options);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else if (objectClass.is("Manager")) {
			HttpGet request = new HttpGet(
					((zup3Configuration) this.getConfiguration()).getServiceAddress() + MANAGERS + REQ_FORMAT);
			try {
				this.handleManagers(request, query, handler, options);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else if (objectClass.is("Org")) {

			HttpGet request = new HttpGet(
					((zup3Configuration) this.getConfiguration()).getServiceAddress() + ORGANIZATIONS + REQ_FORMAT);
			try {
				this.handleOrganisations(request, query, handler, options);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else if (objectClass.is("OrgUnit")) {

			HttpGet request = new HttpGet(
					((zup3Configuration) this.getConfiguration()).getServiceAddress() + ORGUNITS + REQ_FORMAT);
			try {
				this.handleOrgunits(request, query, handler, options);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else if (objectClass.is("CompanyStructure")) {

			HttpGet request = new HttpGet(
					((zup3Configuration) this.getConfiguration()).getServiceAddress() + COMPANYSTRUCTURE + REQ_FORMAT);
			try {
				this.handleCompanyStructure(request, query, handler, options);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else if (objectClass.is("ContactInfo")) {

			HttpGet request = new HttpGet(
					((zup3Configuration) this.getConfiguration()).getServiceAddress() + CONTACT_INFO + REQ_FORMAT);
			try {
				this.handleContactInfo(request, handler, options);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else if (objectClass.is("EmpRole")) {

			HttpGet request = new HttpGet(
					((zup3Configuration) this.getConfiguration()).getServiceAddress() + EMP_ROLES + REQ_FORMAT);
			try {
				this.handleEmpRoles(request, handler, options);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else if (objectClass.is("Individual")) {

			HttpGet request = new HttpGet(
					((zup3Configuration) this.getConfiguration()).getServiceAddress() + INDIVIDUALS + REQ_FORMAT);
			try {
				this.handleIndividuals(request, query, handler, options);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else if (objectClass.is("MainEmpOfIndividuals")) {

			HttpGet request = new HttpGet(((zup3Configuration) this.getConfiguration()).getServiceAddress()
					+ MAIN_EMP_OF_INDIVIDUALS + REQ_FORMAT);
			try {
				this.handleMainEmpOfIndividuals(request, query, handler, options);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else if (objectClass.is("StaffList")) {

			HttpGet request = new HttpGet(
					((zup3Configuration) this.getConfiguration()).getServiceAddress() + STAFFLIST + REQ_FORMAT);
			try {
				this.handleStaffList(request, query, handler, options);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else if (objectClass.is("StaffInCS")) {

			HttpGet request = new HttpGet(
					((zup3Configuration) this.getConfiguration()).getServiceAddress() + STAFF_IN_CS + REQ_FORMAT);
			try {
				this.handleStaffInCS(request, query, handler, options);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else if (objectClass.is("SubOfOrg")) {

			HttpGet request = new HttpGet(((zup3Configuration) this.getConfiguration()).getServiceAddress()
					+ SUBORDINATION_OF_ORGANIZATIONS + REQ_FORMAT);
			try {
				this.handleSubOfOrgs(request, handler, options);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else if (objectClass.is("User")) {

			HttpGet request = new HttpGet(
					((zup3Configuration) this.getConfiguration()).getServiceAddress() + USERS + REQ_FORMAT);
			try {
				this.handleUsers(request, query, handler, options);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else if (objectClass.is("Photo")) {

			HttpGet request = new HttpGet(
					((zup3Configuration) this.getConfiguration()).getServiceAddress() + PHOTOS + REQ_FORMAT);
			try {
				this.handlePhotos(request, query, handler, options);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (objectClass.is("MainJob")) {

			HttpGet request = new HttpGet(
					((zup3Configuration) this.getConfiguration()).getServiceAddress() + MAIN_JOB + REQ_FORMAT);
			try {
				this.handleMainJobs(request, query, handler, options);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (objectClass.is("GPH")) {

			HttpGet request = new HttpGet(
					((zup3Configuration) this.getConfiguration()).getServiceAddress() + GPH + REQ_FORMAT);
			try {
				this.handleGph(request, query, handler, options);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (objectClass.is("FIO")) {

			HttpGet request = new HttpGet(
					((zup3Configuration) this.getConfiguration()).getServiceAddress() + FIO + REQ_FORMAT);
			try {
				this.handleFIO(request, query, handler, options);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (objectClass.is("EmpStatus")) {

			HttpGet request = new HttpGet(
					((zup3Configuration) this.getConfiguration()).getServiceAddress() + EMPSTATUS + REQ_FORMAT);
			try {
				this.handleEmpStatus(request, query, handler, options);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private void handleEmployees(HttpGet request, zup3Filter filter, ResultsHandler handler, OperationOptions options)
			throws IOException {

		CurrentEmpDataCache currentEmpDataCache = new CurrentEmpDataCache(zup3Connector.this);

		if (filter != null && filter.byUid != null) {
			HttpGet filterRequest = new HttpGet(((zup3Configuration) this.getConfiguration()).getServiceAddress()
					+ EMPLOYEES + GUID_PART_1 + filter.byUid + GUID_PART_2 + REQ_FORMAT);
			JSONObject employee = this.callORequest(filterRequest);

			String currEmpUid = employee.getString(EMP_UID);
			JSONObject currentEmpData = (JSONObject) currentEmpDataCache.cacheByEmpKey.get(currEmpUid);

			boolean notInArchive = employee.get(EMP_IN_ARCHIVE).toString().matches("false");
			boolean currentEmpDataNotNull = currentEmpData != null;

			if (notInArchive) {
				if (currentEmpDataNotNull) {
					ConnectorObject connectorObject = this.convertEmployeeToConnectorObject(employee, currentEmpData);
					boolean finish = !handler.handle(connectorObject);
					if (finish) {
						return;
					}
				} else {
					ConnectorObject connectorObject = this.convertEmployeeToConnectorObject(employee);
					boolean finish = !handler.handle(connectorObject);
					if (finish) {
						return;
					}
				}
			}
		} else {

			JSONArray employees = null;
			try {
				employees = this.callRequest(request);
			} catch (JSONException e) {
				throw new ConnectorIOException(e.toString());
			}

			for (int i = 0; i < employees.length(); ++i) {
				JSONObject employee = employees.getJSONObject(i);
				String currEmpUid = employee.getString(EMP_UID);
				JSONObject currentEmpData = (JSONObject) currentEmpDataCache.cacheByEmpKey.get(currEmpUid);

				boolean notInArchive = employee.get(EMP_IN_ARCHIVE).toString().matches("false");
				boolean currentEmpDataNotNull = currentEmpData != null;

				if (notInArchive) {
					if (currentEmpDataNotNull) {
						ConnectorObject connectorObject = this.convertEmployeeToConnectorObject(employee,
								currentEmpData);
						boolean finish = !handler.handle(connectorObject);
						if (finish) {
							return;
						}
					} else {
						ConnectorObject connectorObject = this.convertEmployeeToConnectorObject(employee);
						boolean finish = !handler.handle(connectorObject);
						if (finish) {
							return;
						}
					}
				}
			}
			getConfiguration();
			employees.length();
		}
	}

	private void handlePositions(HttpGet request, zup3Filter filter, ResultsHandler handler, OperationOptions options)
			throws IOException {

		if (filter != null && filter.byUid != null) {

			HttpGet filteredRequest = new HttpGet(((zup3Configuration) this.getConfiguration()).getServiceAddress()
					+ POSITIONS + GUID_PART_1 + filter.byUid + GUID_PART_2 + REQ_FORMAT);
			JSONObject position = this.callORequest(filteredRequest);
			ConnectorObject connectorObject = this.convertPositionToConnectorObject(position);
			boolean finish = !handler.handle(connectorObject);
			if (finish) {
				return;
			}
		} else {

			JSONArray positions = null;
			try {
				positions = this.callRequest(request);
			} catch (JSONException e) {
				throw new ConnectorIOException(e.toString());
			}

			for (int i = 0; i < positions.length(); ++i) {
				JSONObject position = positions.getJSONObject(i);
				ConnectorObject connectorObject = this.convertPositionToConnectorObject(position);
				boolean finish = !handler.handle(connectorObject);
				if (finish) {
					return;
				}
			}
		}
	}

	private void handleManagers(HttpGet request, zup3Filter filter, ResultsHandler handler, OperationOptions options)
			throws IOException {

		if (filter != null && filter.byUid != null) {

			HttpGet filteredRequest = new HttpGet(((zup3Configuration) this.getConfiguration()).getServiceAddress()
					+ MANAGERS + GUID_PART_1 + filter.byUid + GUID_PART_2 + REQ_FORMAT);
			JSONObject manager = this.callORequest(filteredRequest);
			ConnectorObject connectorObject = this.convertManagerToConnectorObject(manager);
			boolean finish = !handler.handle(connectorObject);
			if (finish) {
				return;
			}

		} else {

			JSONArray managers = null;
			try {
				managers = this.callRequest(request);
			} catch (JSONException e) {
				throw new ConnectorIOException(e.toString());
			}

			for (int i = 0; i < managers.length(); ++i) {
				JSONObject manager = managers.getJSONObject(i);
				ConnectorObject connectorObject = this.convertManagerToConnectorObject(manager);
				boolean finish = !handler.handle(connectorObject);
				if (finish) {
					return;
				}
			}
		}
	}

	private void handleEmpHistorySlice(HttpGet request, zup3Filter filter, ResultsHandler handler,
			OperationOptions options) throws IOException {
		boolean fakeDismiss = false;
		String fakeDismissString;
		if (filter != null && filter.byUid != null) {
			HttpGet lastEventRequest = new HttpGet(
					(((zup3Configuration) this.getConfiguration()).getServiceAddress() + CURRENT_EMP_DATA + REQ_FORMAT
							 + "&Condition=(ДействуетДо%20gt%20datetime'" + currentDateTime + "'%20or%20ДействуетДо%20eq%20datetime'0001-01-01T00:00:00')%20and%20Сотрудник_Key" + INFOREG_REQ_2 + filter.byUid + "'"));
			LOG.ok("\n\nlastEventRequest - {0}\n\n", lastEventRequest);
			JSONObject event = null;
			try {
				event = this.callRequest(lastEventRequest).getJSONObject(0);
			} catch (JSONException e) {
				throw new ConnectorIOException(e.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ZonedDateTime eventDate = LocalDateTime.parse(event.getString(CED_EVENT_DATE), formatter).atZone(timeZone);
			boolean isDismiss = event.getString(CED_EVENT_TYPE).contains("Увольнение");

			HttpGet fakeDissmissRequest = new HttpGet((((zup3Configuration) this.getConfiguration()).getServiceAddress()
					+ EMPLOYEES + EMP_DETAILS_1 + filter.byUid + EMP_DETAILS_2 + '/' + EMP_ADD_REQ + REQ_FORMAT));
			JSONArray additionalAttributes = null;
			try {
				additionalAttributes = this.callRequest(fakeDissmissRequest);
			} catch (JSONException e) {
				throw new ConnectorIOException(e.toString());
			}
			for (int i = 0; i < additionalAttributes.length(); i++) {
				JSONObject addAttribute = additionalAttributes.getJSONObject(i);
				if (addAttribute.getInt("LineNumber") == 2) {
					fakeDismissString = addAttribute.getString("Значение");
					if(fakeDismissString.contains("ba4577a4-4fbb-11e9-80d8-005056bf070a")) {
						fakeDismiss = true;
					}
				}
			}
			
			LOG.ok("\n\nisDismiss - {0}\neventDate - {1}\nis before today - {2}\n\n", isDismiss, eventDate, eventDate.isBefore(ZonedDateTime.now().minusDays(1)));
			
			if (!isDismiss && (eventDate.isBefore(ZonedDateTime.now().minusDays(-1)))) {
				ConnectorObject connectorObject = this.convertEventToConnectorObject(event);
				boolean finish = !handler.handle(connectorObject);
				if (finish) {
					return;
				}
			} else if (isDismiss && (eventDate.isBefore(ZonedDateTime.now()))) {
				ConnectorObject connectorObject = this.convertEventToConnectorObject(event, fakeDismiss);
				boolean finish = !handler.handle(connectorObject);
				if (finish) {
					return;
				}
			}

		} else {
			JSONArray events = null;
			try {
				events = this.callRequest(request);
			} catch (JSONException e) {
				throw new ConnectorIOException(e.toString());
			}

			for (int i = 0; i < events.length(); ++i) {
				String mainJobIndKey = events.getJSONObject(i).getString(MAIN_JOB_EMP);
				HttpGet lastEventRequest = new HttpGet(
						(((zup3Configuration) this.getConfiguration()).getServiceAddress() + CURRENT_EMP_DATA + REQ_FORMAT
								 + "&Condition=(ДействуетДо%20gt%20datetime'" + currentDateTime + "'%20or%20ДействуетДо%20eq%20datetime'0001-01-01T00:00:00')%20and%20Сотрудник_Key" + INFOREG_REQ_2 + mainJobIndKey + "'"));
				JSONObject event = null;
				try {
					event = this.callRequest(lastEventRequest).getJSONObject(0);
				} catch (JSONException e) {
					throw new ConnectorIOException(e.toString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ZonedDateTime eventDate = LocalDateTime.parse(event.getString(CED_EVENT_DATE), formatter)
						.atZone(timeZone);
				boolean isDismiss = event.getString(CED_EVENT_TYPE).contains("Увольнение");

				HttpGet fakeDissmissRequest = new HttpGet(
						(((zup3Configuration) this.getConfiguration()).getServiceAddress() + EMPLOYEES + EMP_DETAILS_1
								+ mainJobIndKey + EMP_DETAILS_2 + '/' + EMP_ADD_REQ + REQ_FORMAT));
				JSONArray additionalAttributes = null;
				try {
					additionalAttributes = this.callRequest(fakeDissmissRequest);
				} catch (JSONException e) {
					throw new ConnectorIOException(e.toString());
				}
				for (int i1 = 0; i1 < additionalAttributes.length(); i1++) {
					JSONObject addAttribute = additionalAttributes.getJSONObject(i1);
					if (addAttribute.getInt("LineNumber") == 2) {
						fakeDismissString = addAttribute.getString("Значение");
						if(fakeDismissString.contains("ba4577a4-4fbb-11e9-80d8-005056bf070a")) {
							fakeDismiss = true;
						}
					}
				}

				if (!isDismiss && (eventDate.isBefore(ZonedDateTime.now().minusDays(-1)))) {
					ConnectorObject connectorObject = this.convertEventToConnectorObject(event);
					boolean finish = !handler.handle(connectorObject);
					if (finish) {
						return;
					}
				} else if (isDismiss && (eventDate.isBefore(ZonedDateTime.now()))) {
					ConnectorObject connectorObject = this.convertEventToConnectorObject(event, fakeDismiss);
					boolean finish = !handler.handle(connectorObject);
					if (finish) {
						return;
					}
				}
			}

		}
	}

	private void handleOrganisations(HttpGet request, zup3Filter filter, ResultsHandler handler,
			OperationOptions options) throws IOException {

		if (filter != null && filter.byUid != null) {

			HttpGet filteredRequest = new HttpGet(((zup3Configuration) this.getConfiguration()).getServiceAddress()
					+ ORGANIZATIONS + GUID_PART_1 + filter.byUid + GUID_PART_2 + REQ_FORMAT);
			JSONObject organization = this.callORequest(filteredRequest);
			ConnectorObject connectorObject = this.convertOrganizationToConnectorObject(organization);
			boolean finish = !handler.handle(connectorObject);
			if (finish) {
				return;
			}

		} else {

			JSONArray organizations = null;
			try {
				organizations = this.callRequest(request);
			} catch (JSONException e) {
				throw new ConnectorIOException(e.toString());
			}

			for (int i = 0; i < organizations.length(); i++) {
				JSONObject organization = organizations.getJSONObject(i);
				ConnectorObject connectorObject = this.convertOrganizationToConnectorObject(organization);
				boolean finish = !handler.handle(connectorObject);
				if (finish) {
					return;
				}
			}
		}
	}

	private void handleOrgunits(HttpGet request, zup3Filter filter, ResultsHandler handler, OperationOptions options)
			throws IOException {

		if (filter != null && filter.byUid != null) {

			HttpGet filteredRequest = new HttpGet(((zup3Configuration) this.getConfiguration()).getServiceAddress()
					+ ORGUNITS + GUID_PART_1 + filter.byUid + GUID_PART_2 + REQ_FORMAT);
			JSONObject orgUnit = this.callORequest(filteredRequest);
			ConnectorObject connectorObject = this.convertOrgUnitToConnectorObject(orgUnit);
			boolean finish = !handler.handle(connectorObject);
			if (finish) {
				return;
			}

		} else {

			JSONArray orgUnits = null;
			try {
				orgUnits = this.callRequest(request);
			} catch (JSONException e) {
				throw new ConnectorIOException(e.toString());
			}

			for (int i = 0; i < orgUnits.length(); i++) {
				JSONObject orgUnit = orgUnits.getJSONObject(i);
				ConnectorObject connectorObject = this.convertOrgUnitToConnectorObject(orgUnit);
				boolean finish = !handler.handle(connectorObject);
				if (finish) {
					return;
				}
			}
		}
	}

	private void handleCompanyStructure(HttpGet request, zup3Filter filter, ResultsHandler handler,
			OperationOptions options) throws IOException {

		if (filter != null && filter.byUid != null) {

			HttpGet filteredRequest = new HttpGet(((zup3Configuration) this.getConfiguration()).getServiceAddress()
					+ COMPANYSTRUCTURE + GUID_PART_1 + filter.byUid + GUID_PART_2 + REQ_FORMAT);
			JSONObject company = this.callORequest(filteredRequest);
			ConnectorObject connectorObject = this.convertCompanyToConnectorObject(company);
			boolean finish = !handler.handle(connectorObject);
			if (finish) {
				return;
			}

		} else {

			JSONArray companies = null;
			try {
				companies = this.callRequest(request);
			} catch (JSONException e) {
				throw new ConnectorIOException(e.toString());
			}

			for (int i = 0; i < companies.length(); i++) {
				JSONObject company = companies.getJSONObject(i);
				ConnectorObject connectorObject = this.convertCompanyToConnectorObject(company);
				boolean finish = !handler.handle(connectorObject);
				if (finish) {
					return;
				}
			}
		}
	}

	private void handleContactInfo(HttpGet request, ResultsHandler handler, OperationOptions options)
			throws IOException {
		JSONArray contacts = null;
		try {
			contacts = this.callRequest(request);
		} catch (JSONException e) {
			throw new ConnectorIOException(e.toString());
		}

		for (int i = 0; i < contacts.length(); i++) {
			JSONObject contactInfo = contacts.getJSONObject(i);
			getConfiguration();
			HttpGet requestContactInfoDetail = new HttpGet(
					((zup3Configuration) this.getConfiguration()).getServiceAddress() + CONTACT_INFO + REQ_FORMAT
							+ INFOREG_REQ_1 + CIF_UID + INFOREG_REQ_2 + contactInfo.getString(CIF_UID) + INFOREG_REQ_3);
			JSONArray contactInfos = null;
			try {
				contactInfos = this.callRequest(requestContactInfoDetail);
			} catch (JSONException e) {
				throw new ConnectorIOException(e.toString());
			}
			for (int j = 0; j < contactInfos.length(); j++) {
				JSONObject contact = contactInfos.getJSONObject(j);
				getConfiguration();
				ConnectorObject connectorObject1 = this.convertContactInfoToConnectorObject(contact);
				boolean finish = !handler.handle(connectorObject1);
				if (finish) {
					return;
				}
			}
		}
	}

	private void handleEmpRoles(HttpGet request, ResultsHandler handler, OperationOptions options) throws IOException {
		JSONArray empRoles = null;
		try {
			empRoles = this.callRequest(request);
		} catch (JSONException e) {
			throw new ConnectorIOException(e.toString());
		}

		for (int i = 0; i < empRoles.length(); i++) {
			JSONObject empRole = empRoles.getJSONObject(i);
			getConfiguration();
			HttpGet requestEmpRolesDetail = new HttpGet(
					((zup3Configuration) this.getConfiguration()).getServiceAddress() + EMP_ROLES + REQ_FORMAT
							+ INFOREG_REQ_1 + ER_EMP_KEY + INFOREG_REQ_2 + empRole.getString(ER_EMP_KEY)
							+ INFOREG_REQ_3);
			JSONArray roles = null;
			try {
				roles = this.callRequest(requestEmpRolesDetail);
			} catch (JSONException e) {
				throw new ConnectorIOException(e.toString());
			}
			for (int j = 0; j < roles.length(); j++) {
				JSONObject role = roles.getJSONObject(j);
				ConnectorObject connectorObject = this.convertEmpRoleToConnectorObject(role);
				boolean finish = !handler.handle(connectorObject);
				if (finish) {
					return;
				}
			}
		}
	}

	private void handleIndividuals(HttpGet request, zup3Filter filter, ResultsHandler handler, OperationOptions options)
			throws IOException {

		if (filter != null && filter.byUid != null) {

			HttpGet filteredRequest = new HttpGet(((zup3Configuration) this.getConfiguration()).getServiceAddress()
					+ INDIVIDUALS + GUID_PART_1 + filter.byUid + GUID_PART_2 + REQ_FORMAT);
			LOG.info("\n\nАдрес запроса с фильтром: {0}\n", filteredRequest.toString());
			JSONObject individual = this.callORequest(filteredRequest);

			if (individual.get(IND_IS_FOLDER).toString().matches("false")
//    				&& !individual.get(IND_PARENT_KEY).toString().contains("18f43c58-18e8-11e9-80d8-005056bf070a")
					&& !individual.get(IND_PARENT_KEY).toString().contains("18f43c57-18e8-11e9-80d8-005056bf070a")
					&& individual.get(IND_SURNAME).toString().length() > 2
					&& individual.get(IND_DEL_MARK).toString().contains("false")
					&& !individual.get(IND_UID).toString().contains("fff64e9e-ef23-11e5-83a1-089e0115534b")) {
				ConnectorObject connectorObject = this.convertIndividualToConnectorObject(individual);
				boolean finish = !handler.handle(connectorObject);
				if (finish) {
					return;
				}
			} else
				return;
		} else {

			JSONArray individuals = null;
			try {
				individuals = this.callRequest(request);
			} catch (JSONException e) {
				throw new ConnectorIOException(e.toString());
			}

			for (int i = 0; i < individuals.length(); i++) {
				JSONObject individual = individuals.getJSONObject(i);

				if (individual.get(IND_IS_FOLDER).toString().matches("true")
//						|| individual.get(IND_PARENT_KEY).toString().contains("18f43c58-18e8-11e9-80d8-005056bf070a")
						|| individual.get(IND_PARENT_KEY).toString().contains("18f43c57-18e8-11e9-80d8-005056bf070a")
						|| individual.get(IND_SURNAME).toString().length() < 3
						|| individual.get(IND_DEL_MARK).toString().contains("true")
						|| individual.get(IND_UID).toString().contains("fff64e9e-ef23-11e5-83a1-089e0115534b")) {
					continue;
				} else {
					ConnectorObject connectorObject = this.convertIndividualToConnectorObject(individual);
					boolean finish = !handler.handle(connectorObject);
					if (finish) {
						return;
					}
				}
			}
		}
	}

	private void handleMainEmpOfIndividuals(HttpGet request, zup3Filter filter, ResultsHandler handler,
			OperationOptions options) throws IOException {
		if (filter != null && filter.byUid != null) {
			HttpGet requestEmpDetail = new HttpGet(((zup3Configuration) this.getConfiguration()).getServiceAddress()
					+ MAIN_EMP_OF_INDIVIDUALS + REQ_FORMAT + MAIN_EMP_REQ_1 + INFOREG_REQ_2 + filter.byUid
					+ INFOREG_REQ_3 + FIND_EMP_LAST_EVENT);
			JSONArray empTypes = null;
			try {
				empTypes = this.callRequest(requestEmpDetail);
			} catch (JSONException e) {
				throw new ConnectorIOException(e.toString());
			}
			for (int j = 0; j < empTypes.length(); j++) {
				JSONObject mainEmp = empTypes.getJSONObject(j);
				if (mainEmp.getString(MEI_EMP_TYPE).equals("ОсновноеМестоРаботы") || mainEmp.getString(MEI_EMP_TYPE).equals("Совместительство")) {
					ConnectorObject connectorObject = this.convertMainEmpOfIndividualToConnectorObject(mainEmp);
					boolean finish = !handler.handle(connectorObject);
					if (finish) {
						return;
					}
					return;
				}
			}
		} else {
			JSONArray mainEmps = this.callRequest(request);

			for (int i = 0; i < mainEmps.length(); i++) {
				String indKey = mainEmps.getJSONObject(i).getString(MEI_PERS_KEY);
				HttpGet requestEmpDetail = new HttpGet(((zup3Configuration) this.getConfiguration()).getServiceAddress()
						+ MAIN_EMP_OF_INDIVIDUALS + REQ_FORMAT + MAIN_EMP_REQ_1 + INFOREG_REQ_2 + indKey + INFOREG_REQ_3
						+ FIND_EMP_LAST_EVENT);

				JSONArray empTypes = this.callRequest(requestEmpDetail);

				for (int j = 0; j < empTypes.length(); j++) {
					JSONObject mainEmp = empTypes.getJSONObject(j);
					if (mainEmp.getString(MEI_EMP_TYPE).equals("ОсновноеМестоРаботы") || mainEmp.getString(MEI_EMP_TYPE).equals("Совместительство")) {
						ConnectorObject connectorObject = this.convertMainEmpOfIndividualToConnectorObject(mainEmp);
						boolean finish = !handler.handle(connectorObject);
						if (finish) {
							return;
						}
						return;
					}
				}

			}

//            JSONObject mainEmp = this.callRequest(requestEmpDetail).getJSONObject(0);
//            ConnectorObject connectorObject = this.convertMainEmpOfIndividualToConnectorObject(mainEmp);
		}
	}

	private void handleStaffList(HttpGet request, zup3Filter filter, ResultsHandler handler, OperationOptions options)
			throws IOException {

		if (filter != null && filter.byUid != null) {

			HttpGet filteredRequest = new HttpGet(((zup3Configuration) this.getConfiguration()).getServiceAddress()
					+ STAFFLIST + GUID_PART_1 + filter.byUid + GUID_PART_2 + REQ_FORMAT);
			JSONObject staff = this.callORequest(filteredRequest);
			ConnectorObject connectorObject = this.convertStaffToConnectorObject(staff);
			boolean finish = !handler.handle(connectorObject);
			if (finish) {
				return;
			}

		} else {

			JSONArray staffList = this.callRequest(request);

			for (int i = 0; i < staffList.length(); i++) {
				JSONObject staff = staffList.getJSONObject(i);
				ConnectorObject connectorObject = this.convertStaffToConnectorObject(staff);
				boolean finish = !handler.handle(connectorObject);
				if (finish) {
					return;
				}
			}
		}
	}

	private void handleStaffInCS(HttpGet request, zup3Filter filter, ResultsHandler handler, OperationOptions options)
			throws IOException {

		if (filter != null && filter.byUid != null) {

			HttpGet filteredRequest = new HttpGet(((zup3Configuration) this.getConfiguration()).getServiceAddress()
					+ STAFF_IN_CS + GUID_PART_1 + filter.byUid + GUID_PART_2 + REQ_FORMAT);
			JSONObject staff1 = this.callORequest(filteredRequest);
			ConnectorObject connectorObject = this.convertStaffInCsToConnectorObject(staff1);
			boolean finish = !handler.handle(connectorObject);
			if (finish) {
				return;
			}

		} else {

			JSONArray staffInCS = this.callRequest(request);

			for (int i = 0; i < staffInCS.length(); i++) {
				JSONObject staff1 = staffInCS.getJSONObject(i);
				ConnectorObject connectorObject = this.convertStaffInCsToConnectorObject(staff1);
				boolean finish = !handler.handle(connectorObject);
				if (finish) {
					return;
				}
			}
		}
	}

	private void handleSubOfOrgs(HttpGet request, ResultsHandler handler, OperationOptions options) throws IOException {
		JSONArray subordinations = this.callRequest(request);

		for (int i = 0; i < subordinations.length(); i++) {
			JSONObject subordination = subordinations.getJSONObject(i);
			ConnectorObject connectorObject = this.convertSubordinationToConnectorObject(subordination);
			boolean finish = !handler.handle(connectorObject);
			if (finish) {
				return;
			}
		}
	}

	private void handleUsers(HttpGet request, zup3Filter filter, ResultsHandler handler, OperationOptions options)
			throws IOException {

		if (filter != null && filter.byUid != null) {

			HttpGet filteredRequest = new HttpGet(((zup3Configuration) this.getConfiguration()).getServiceAddress()
					+ USERS + GUID_PART_1 + filter.byUid + GUID_PART_2 + REQ_FORMAT);
			JSONObject user = this.callORequest(filteredRequest);
			ConnectorObject connectorObject = this.convertUserToConnectorObject(user);
			boolean finish = !handler.handle(connectorObject);
			if (finish) {
				return;
			}

		} else {

			JSONArray users = this.callRequest(request);

			for (int i = 0; i < users.length(); i++) {
				JSONObject user = users.getJSONObject(i);
				ConnectorObject connectorObject = this.convertUserToConnectorObject(user);
				boolean finish = !handler.handle(connectorObject);
				if (finish) {
					return;
				}
			}
		}
	}

	private void handlePhotos(HttpGet request, zup3Filter filter, ResultsHandler handler, OperationOptions options)
			throws IOException {
		if (filter != null && filter.byUid != null) {
			HttpGet filteredRequest = new HttpGet(((zup3Configuration) this.getConfiguration()).getServiceAddress()
					+ PHOTOS + GUID_PART_1 + filter.byUid + GUID_PART_2 + REQ_FORMAT);
			JSONObject photo = this.callORequest(filteredRequest);
			ConnectorObject connectorObject = this.convertPhotoToConnectorObject(photo);
			boolean finish = !handler.handle(connectorObject);
			if (finish) {
				return;
			}
		} else {

			JSONArray photos = this.callRequest(request);

			for (int i = 0; i < photos.length(); i++) {
				JSONObject photo = photos.getJSONObject(i);
				ConnectorObject connectorObject = this.convertPhotoToConnectorObject(photo);
				boolean finish = !handler.handle(connectorObject);
				if (finish) {
					return;
				}
			}
		}
	}

	private void handleMainJobs(HttpGet request, zup3Filter filter, ResultsHandler handler, OperationOptions options)
			throws IOException {
		if (filter != null && filter.byUid != null) {			
			HttpGet requestJobDetail = new HttpGet(((zup3Configuration) this.getConfiguration()).getServiceAddress()
					+ MAIN_JOB_FINDER_PART_1 + filter.byUid.substring(0,36) + MAIN_JOB_FINDER_PART_2);
//			LOG.ok("\n\n\nСтрока запроса основного места работы: {0}", requestJobDetail.toString());
			JSONArray empTypes = this.callRequest(requestJobDetail);

			for (int j = 0; j < empTypes.length(); j++) {
				JSONObject mainEmp = empTypes.getJSONObject(j);
				
				String empGuid = mainEmp.getString(MAIN_JOB_EMP);
				HttpGet requestEmpStatusIsFiredReq = new HttpGet(((zup3Configuration) this.getConfiguration()).getServiceAddress()
						+ MAIN_JOB_STATUS_PART_1 + empGuid + MAIN_JOB_STATUS_PART_2);
				
				JSONObject empStatus = this.callORequest(requestEmpStatusIsFiredReq);
				JSONArray empStatusValue = empStatus.getJSONArray("value");
//				LOG.ok("\n\nEmpstatusvalue is empty - {0}, like {1}\n\n", empStatusValue.isEmpty(), empStatusValue);
				
				
				if ((mainEmp.getString(MEI_EMP_TYPE).equals("ОсновноеМестоРаботы") || mainEmp.getString(MEI_EMP_TYPE).equals("Совместительство")) && empStatusValue.isEmpty()) {
					ConnectorObject connectorObject = this.convertMainJobToConnectorObject(mainEmp);
					boolean finish = !handler.handle(connectorObject);
					if (finish) {
						return;
					}
				}
			}
		} else {
			int mainCount = 0;
			int firedCount = 0;
			int usedCount = 0;
			int handledCount = 0;
			ArrayList<String> uuids = new ArrayList<>();
			ArrayList<String> usedUUIDs = new ArrayList<>();
			
			HttpGet firedEmpsReq = new HttpGet(resourceHandler.DISMISSED_EMPS_FULL_REQ);
			JSONArray firedEmps = this.callRequest(firedEmpsReq);
			
			for (int i = 0; i < firedEmps.length(); i++) {
				JSONArray currentRecordSet = firedEmps.getJSONObject(i).getJSONArray("RecordSet");
				for (int j = 0; j < currentRecordSet.length(); j++) {
					JSONObject currEntry = currentRecordSet.getJSONObject(j);
					String currUUID = currEntry.getString("Сотрудник_Key");
					uuids.add(currUUID);
				}
			}
			
//			LOG.error("Fired emps: {0}", uuids);
			String currReqStr = request.toString().substring(4, (request.toString().length() -9)) + "&$orderby=ВидЗанятости";
			HttpGet currRequest = new HttpGet(currReqStr);
			JSONArray mainJobs = this.callRequest(currRequest);
			
			for (int i = 0; i < mainJobs.length(); i++) {
				mainCount ++;
				JSONObject currEmp = mainJobs.getJSONObject(i);
				
				String empGuid = currEmp.getString(MAIN_JOB_EMP);
				String persGuid = currEmp.getString(MAIN_JOB_INDIVIDUAL);
				Boolean empNotInFiredEmps = !uuids.contains(empGuid);
				Boolean guidIsNotUsed = !usedUUIDs.contains(persGuid);
				
				if ((currEmp.getString(MEI_EMP_TYPE).equals("ОсновноеМестоРаботы") || currEmp.getString(MEI_EMP_TYPE).equals("Совместительство")) && empNotInFiredEmps && guidIsNotUsed) {
					handledCount ++;
					ConnectorObject connectorObject = this.convertMainJobToConnectorObject(currEmp);
					usedUUIDs.add(persGuid);
					boolean finish = !handler.handle(connectorObject);
					if (finish) {
						return;
					}
				} else if (!empNotInFiredEmps) {
					firedCount ++;
				} else if (!guidIsNotUsed) {
					usedCount ++;
				}
				
			}

//			for (int i = 0; i < mainJobs.length(); i++) {
//				mainCount ++;
//				String indKey = mainJobs.getJSONObject(i).getString(MAIN_JOB_INDIVIDUAL);
//				HttpGet requestJobDetail = new HttpGet(((zup3Configuration) this.getConfiguration()).getServiceAddress()
//						+ MAIN_JOB_FINDER_PART_1 + indKey + MAIN_JOB_FINDER_PART_2);
//				JSONArray empTypes = this.callRequest(requestJobDetail);
//
//				for (int j = 0; j < empTypes.length(); j++) {
//					
//					JSONObject mainEmp = empTypes.getJSONObject(j);
//					
//					
//					String empGuid = mainEmp.getString(MAIN_JOB_EMP);
//					Boolean empNotInFiredEmps = !uuids.contains(empGuid);
//					Boolean guidIsNotUsed = !usedUUIDs.contains(empGuid);
////					HttpGet requestEmpStatusIsFiredReq = new HttpGet(((zup3Configuration) this.getConfiguration()).getServiceAddress()
////							+ MAIN_JOB_STATUS_PART_1 + empGuid + MAIN_JOB_STATUS_PART_2);
////					
////					JSONObject empStatus = this.callORequest(requestEmpStatusIsFiredReq);
////					JSONArray empStatusValue = empStatus.getJSONArray("value");
////
////					
//					if ((mainEmp.getString(MEI_EMP_TYPE).equals("ОсновноеМестоРаботы") || mainEmp.getString(MEI_EMP_TYPE).equals("Совместительство")) && empNotInFiredEmps && guidIsNotUsed) {
//						ConnectorObject connectorObject = this.convertMainJobToConnectorObject(mainEmp);
//						usedUUIDs.add(empGuid);
//						boolean finish = !handler.handle(connectorObject);
//						if (finish) {
//							return;
//						}
//					}
//				}
//			}
//		LOG.error("Итого: \n\tВсего записей - {0}\n\tОбработано - {1}\n\tУволено - {2}\n\tПовторяется - {3}\n\nВ массиве использованных - {4}", mainCount, handledCount, firedCount, usedCount, usedUUIDs.size());
		}
		
	}

	private void handleGph(HttpGet request, zup3Filter filter, ResultsHandler handler, OperationOptions options)
			throws IOException {
		if (filter != null && filter.byUid != null) {
			HttpGet requestGphDetail = new HttpGet(((zup3Configuration) this.getConfiguration()).getServiceAddress()
					+ GPH + REQ_FORMAT + INFOREG_REQ_1 + GPH_IND_KEY + INFOREG_REQ_2 + filter.byUid + INFOREG_REQ_3
					+ "&$orderby=ДатаНачала%20desc");
			JSONObject gphContract = null;
			try {
				gphContract = this.callRequest(requestGphDetail).getJSONObject(0);
			} catch (JSONException e) {
				throw new ConnectorIOException(e.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			ZonedDateTime endofContract = LocalDateTime.parse(gphContract.getString(GPH_VALID_TO), formatter)
					.atZone(ZoneId.systemDefault());

			if (currZoneDT.isBefore(endofContract)) {

				ConnectorObject connectorObject = this.convertGphContractToConnectorObject(gphContract);
				boolean finish = !handler.handle(connectorObject);
				if (finish) {
					return;
				}
			}

		} else {
			JSONArray gphArray = this.callRequest(request);

			for (int i = 0; i < gphArray.length(); i++) {
				String indKey = gphArray.getJSONObject(i).getString(GPH_IND_KEY);
				HttpGet requestGphDetail = new HttpGet(((zup3Configuration) this.getConfiguration()).getServiceAddress()
						+ GPH + REQ_FORMAT + INFOREG_REQ_1 + GPH_IND_KEY + INFOREG_REQ_2 + indKey + INFOREG_REQ_3
						+ "&$orderby=ДатаНачала%20desc");
				JSONObject gphContract = this.callRequest(requestGphDetail).getJSONObject(0);

				ZonedDateTime endofContract = LocalDateTime.parse(gphContract.getString(GPH_VALID_TO), formatter)
						.atZone(ZoneId.systemDefault());

				if (currZoneDT.isBefore(endofContract)) {
					ConnectorObject connectorObject = this.convertGphContractToConnectorObject(gphContract);
					boolean finish = !handler.handle(connectorObject);
					if (finish) {
						return;
					}
				}
			}
		}
	}

	private void handleFIO(HttpGet request, zup3Filter query, ResultsHandler handler, OperationOptions options)
			throws IOException {
		if (query != null && query.byUid != null) {
			HttpGet requestFIODetail = new HttpGet(((zup3Configuration) this.getConfiguration()).getServiceAddress()
					+ FIO + REQ_FORMAT + "&Condition=cast(ФизическоеЛицо,%20'Catalog_ФизическиеЛица')%20eq%20guid'"
					+ query.byUid + "'&$top=1&$orderby=Period%20desc");
			JSONObject fio = this.callRequest(requestFIODetail).getJSONObject(0);

			ZonedDateTime fioChangeDate = LocalDateTime.parse(fio.getString(FIO_PERIOD), formatter)
					.atZone(ZoneId.systemDefault());
			if (borderDate.isBefore(fioChangeDate)) {

				ConnectorObject connectorObject = this.convertFIOToConnectorObject(fio);
				boolean finish = !handler.handle(connectorObject);
				if (finish) {
					return;
				}
			}

		} else {
			JSONArray fioArray = this.callRequest(request);

			for (int i = 0; i < fioArray.length(); i++) {
				JSONObject fio = fioArray.getJSONObject(i);
				ZonedDateTime fioChangeDate = LocalDateTime.parse(fio.getString(FIO_PERIOD), formatter)
						.atZone(ZoneId.systemDefault());

				if (borderDate.isBefore(fioChangeDate)) {

					ConnectorObject connectorObject = this.convertFIOToConnectorObject(fio);
					boolean finish = !handler.handle(connectorObject);
					if (finish) {
						return;
					}
				}

			}
		}

	}

	private void handleEmpStatus(HttpGet request, zup3Filter query, ResultsHandler handler, OperationOptions options)
			throws JSONException, IOException {
		if (query != null && query.byName != null) {
			HttpGet requestEmpStatusDetail = new HttpGet(
					((zup3Configuration) this.getConfiguration()).getServiceAddress() + EMPSTATUS + "(Recorder='"
							+ query.byName.split("--")[0] + "', Recorder_Type='" + query.byName.split("--")[1] + "')"
							+ REQ_FORMAT);
			JSONObject statusRecordSet = this.callRequest(requestEmpStatusDetail).getJSONObject(0)
					.getJSONArray("RecordSet").getJSONObject(0);

			ZonedDateTime empStatusStartDate = LocalDateTime
					.parse(statusRecordSet.getString(EMP_STATUS_START_DATE), formatter).atZone(ZoneId.systemDefault());
			ZonedDateTime empStatusEndDate = LocalDateTime
					.parse(statusRecordSet.getString(EMP_STATUS_END_DATE), formatter).atZone(ZoneId.systemDefault());
			ZonedDateTime empStatusEstimEndDate = LocalDateTime
					.parse(statusRecordSet.getString(EMP_STATUS_ESTIMATED_END_DATE), formatter)
					.atZone(ZoneId.systemDefault());

			String EmpStatusValue = statusRecordSet.getString(EMP_STATUS_VALUE);

			if (EmpStatusValue.contains("Увольнение") && empStatusStartDate.isBefore(ZonedDateTime.now())
					|| (empStatusStartDate.isBefore(ZonedDateTime.now())
							&& (empStatusEndDate.isAfter(ZonedDateTime.now())
									|| empStatusEstimEndDate.isAfter(ZonedDateTime.now())))) {
				ConnectorObject connectorObject = this.convertEmpStatusToConnectorObject(statusRecordSet,
						empStatusStartDate, empStatusEndDate, empStatusEstimEndDate);
				boolean finish = !handler.handle(connectorObject);
				if (finish) {
					return;
				}
			}
		} else {
			JSONArray statusRecords = this.callRequest(request);

			for (int i = 0; i < statusRecords.length(); i++) {
				JSONObject statusRecordSet = statusRecords.getJSONObject(i).getJSONArray("RecordSet").getJSONObject(0);

				ZonedDateTime empStatusStartDate = LocalDateTime
						.parse(statusRecordSet.getString(EMP_STATUS_START_DATE), formatter)
						.atZone(ZoneId.systemDefault());
				ZonedDateTime empStatusEndDate = LocalDateTime
						.parse(statusRecordSet.getString(EMP_STATUS_END_DATE), formatter)
						.atZone(ZoneId.systemDefault());
				ZonedDateTime empStatusEstimEndDate = LocalDateTime
						.parse(statusRecordSet.getString(EMP_STATUS_ESTIMATED_END_DATE), formatter)
						.atZone(ZoneId.systemDefault());

				String EmpStatusValue = statusRecordSet.getString(EMP_STATUS_VALUE);

				if ((EmpStatusValue.contains("Увольнение") && empStatusStartDate.isBefore(ZonedDateTime.now()))
						|| (empStatusStartDate.isBefore(ZonedDateTime.now())
								&& (empStatusEndDate.isAfter(ZonedDateTime.now())
										|| empStatusEstimEndDate.isAfter(ZonedDateTime.now())))) {
					ConnectorObject connectorObject = this.convertEmpStatusToConnectorObject(statusRecordSet,
							empStatusStartDate, empStatusEndDate, empStatusEstimEndDate);
					boolean finish = !handler.handle(connectorObject);
					if (finish) {
						return;
					}
				}
			}
		}

	}

	private ConnectorObject convertEmployeeToConnectorObject(JSONObject employee, JSONObject currentEmpData) {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setUid(new Uid(employee.getString(EMP_UID)));
		builder.setName(employee.getString(EMP_UID));

		this.getIfExists(employee, EMP_UID, builder);
		this.getIfExists(employee, EMP_DEL_MARK, builder);
		this.getIfExists(employee, EMP_CODE, builder);
		this.getIfExists(employee, EMP_DESCRIPTION, builder);
		this.getIfExists(employee, EMP_PERS_KEY, builder);
		this.getIfExists(employee, EMP_HEAD_ORG_KEY, builder);
		this.getIfExists(employee, EMP_IN_ARCHIVE, builder);
		this.getIfExists(employee, EMP_HEAD_EMP_KEY, builder);
		this.getIfExists(employee, EMP_PREDEF, builder);

//        String key;
//		JSONArray addReqs = employee.getJSONArray(key);

		JSONArray additionals = employee.getJSONArray("ДополнительныеРеквизиты");
//		System.out.println(ANSI_CYAN + additionals);

		if (additionals.length() > 0) {
			for (int i = 0; i < additionals.length(); i++) {
				JSONObject additionalValue = additionals.getJSONObject(i);
				if (!(additionalValue.getString("Свойство_Key").contains("33bf089c-3f1a-11e9-80d8-005056bf070a"))
						&& (builder.build().getAttributeByName("УЗДляAD") == null)) {
					this.addAttr(builder, "УЗДляAD", "false");
				} else {
					if (additionalValue.getString("Значение").contains("49d40e42-3f1a-11e9-80d8-005056bf070a")) {
						ADAccountCount++;
						this.addAttr(builder, "УЗДляAD", "true");
					} else if (additionalValue.getString("Значение").contains("49d40e43-3f1a-11e9-80d8-005056bf070a")) {
						this.addAttr(builder, "УЗДляAD", "false");
					} else if (additionalValue.getString("Значение").contains("d4508728-4f96-11e9-80d8-005056bf070a")) {
						this.addAttr(builder, "УЗДляAD", "false");
					}
				}
			}
		} else {
			this.addAttr(builder, "УЗДляAD", "false");
		}

		boolean enable = !employee.getBoolean(EMP_IN_ARCHIVE);
		this.addAttr(builder, OperationalAttributes.ENABLE_NAME, enable);

//        System.out.format("%30s%55s%60s%60s%10s", "курремпДата", builder.build().getAttributeByName(EMP_DESCRIPTION).getValue(), builder.build().getAttributeByName(EMP_UID).getValue(),builder.build().getAttributeByName("УЗДляAD").getValue(), ADAccountCount + "\n");

		new HashMap<>();
		return builder.build();
	}

	private ConnectorObject convertEmployeeToConnectorObject(JSONObject employee) {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setUid(new Uid(employee.getString(EMP_UID)));
		builder.setName(employee.getString(EMP_UID));

		this.getIfExists(employee, EMP_UID, builder);
		this.getIfExists(employee, EMP_DEL_MARK, builder);
		this.getIfExists(employee, EMP_CODE, builder);
		this.getIfExists(employee, EMP_DESCRIPTION, builder);
		this.getIfExists(employee, EMP_PERS_KEY, builder);
		this.getIfExists(employee, EMP_HEAD_ORG_KEY, builder);
		this.getIfExists(employee, EMP_IN_ARCHIVE, builder);
		this.getIfExists(employee, EMP_HEAD_EMP_KEY, builder);
		this.getIfExists(employee, EMP_PREDEF, builder);

		JSONArray additionals = employee.getJSONArray("ДополнительныеРеквизиты");
//		System.out.println(ANSI_CYAN + additionals);

		if (additionals.length() > 0) {
			for (int i = 0; i < additionals.length(); i++) {
				JSONObject additionalValue = additionals.getJSONObject(i);
				if (!(additionalValue.getString("Свойство_Key").contains("33bf089c-3f1a-11e9-80d8-005056bf070a"))
						&& (builder.build().getAttributeByName("УЗДляAD") == null)) {
//                	System.out.println(builder.build().getAttributeByName(EMP_DESCRIPTION).getValue() + " - Нет дополнительного свойства, УЗ нулевое: " + (builder.build().getAttributeByName("УЗДляAD") == null));
					this.addAttr(builder, "УЗДляAD", "false");
				} else {
//                	System.out.println(builder.build().getAttributeByName(EMP_DESCRIPTION).getValue() + " - Есть дополнительные свойства");
					if (additionalValue.getString("Значение").contains("49d40e42-3f1a-11e9-80d8-005056bf070a")) {
						ADAccountCount++;
//                        System.out.println("bingo");
						this.addAttr(builder, "УЗДляAD", "true");
					} else if (additionalValue.getString("Значение").contains("49d40e43-3f1a-11e9-80d8-005056bf070a")) {
//                        System.out.println("huingo");
						this.addAttr(builder, "УЗДляAD", "false");
					} else if (additionalValue.getString("Значение").contains("d4508728-4f96-11e9-80d8-005056bf070a")) {
//                        System.out.println("ebingo");
						this.addAttr(builder, "УЗДляAD", "false");
					}
				}
			}
		} else {
			this.addAttr(builder, "УЗДляAD", "false");
		}

		boolean enable = !employee.getBoolean(EMP_IN_ARCHIVE);
		this.addAttr(builder, OperationalAttributes.ENABLE_NAME, enable);

//        System.out.format("%30s%55s%60s%60s%10s", "-----------", builder.build().getAttributeByName(EMP_DESCRIPTION).getValue(), builder.build().getAttributeByName(EMP_UID).getValue(),builder.build().getAttributeByName("УЗДляAD").getValue(), ADAccountCount + "\n");

		new HashMap<>();
		return builder.build();
	}

	private ConnectorObject convertPositionToConnectorObject(JSONObject position) {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setUid(new Uid(position.getString(POS_UID)));
		builder.setName(position.getString(POS_UID));

		this.getIfExists(position, POS_UID, builder);
		this.getIfExists(position, POS_DEL_MARK, builder);
		this.getIfExists(position, POS_DESCRIPTION, builder);
		this.getIfExists(position, POS_PREDEF, builder);

		new HashMap<>();
		LOG.ok("Builder.build: {0}", builder.build());
		return builder.build();
	}

	private ConnectorObject convertManagerToConnectorObject(JSONObject manager) {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setUid(new Uid(manager.getString(MAN_OU_KEY)));
		builder.setName(manager.getString(MAN_OU_KEY));

		this.getIfExists(manager, MAN_STAF_POS_KEY, builder);

		new HashMap<>();
		LOG.ok("Builder.build: {0}", builder.build());
		return builder.build();
	}

	private ConnectorObject convertEventToConnectorObject(JSONObject event) {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setUid(new Uid(event.getString(CED_EMP_KEY)));
		builder.setName(event.getString(CED_EMP_KEY));

		this.getIfExists(event, CED_PERS_KEY, builder);
		this.getIfExists(event, CED_EMP_KEY, builder);
		this.getIfExists(event, CED_HEAD_ORG_KEY, builder);
		this.getIfExists(event, CED_CUR_ORG_KEY, builder);
		this.getIfExists(event, CED_CUR_OU_KEY, builder);
		this.getIfExists(event, CED_CUR_POS_KEY, builder);
		this.getIfExists(event, CED_CUR_POS_IN_STAFF_KEY, builder);
		this.getIfExists(event, CED_IS_HEAD_EMP, builder);
		this.getIfExists(event, CED_HEAD_EMP, builder);
		this.getIfExists(event, CED_EVENT_TYPE, builder);
//        this.getIfExists(event, CED_EVENT_DATE, builder);
		ZonedDateTime dateValue = LocalDateTime.parse(event.getString(CED_EVENT_DATE), formatter).atZone(timeZone);
		this.addAttr(builder, CED_EVENT_DATE, dateValue);

		new HashMap<>();
		LOG.ok("Builder.build: {0}", builder.build());
		return builder.build();
	}

	private ConnectorObject convertEventToConnectorObject(JSONObject event, boolean fakeDismiss) {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setUid(new Uid(event.getString(CED_EMP_KEY)));
		builder.setName(event.getString(CED_EMP_KEY));

		this.getIfExists(event, CED_PERS_KEY, builder);
		this.getIfExists(event, CED_EMP_KEY, builder);
		this.getIfExists(event, CED_HEAD_ORG_KEY, builder);
		this.getIfExists(event, CED_CUR_ORG_KEY, builder);
		this.getIfExists(event, CED_CUR_OU_KEY, builder);
		this.getIfExists(event, CED_CUR_POS_KEY, builder);
		this.getIfExists(event, CED_CUR_POS_IN_STAFF_KEY, builder);
		this.getIfExists(event, CED_IS_HEAD_EMP, builder);
		this.getIfExists(event, CED_HEAD_EMP, builder);
		this.getIfExists(event, CED_EVENT_TYPE, builder);
//        this.getIfExists(event, CED_EVENT_DATE, builder);
		ZonedDateTime dateValue = LocalDateTime.parse(event.getString(CED_EVENT_DATE), formatter).atZone(timeZone);
		this.addAttr(builder, CED_EVENT_DATE, dateValue);
		this.addAttr(builder, "УволенДляПеревода", fakeDismiss);

		new HashMap<>();
		LOG.ok("Builder.build: {0}", builder.build());
		return builder.build();
	}

	private ConnectorObject convertOrganizationToConnectorObject(JSONObject organization) {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setUid(new Uid(organization.getString(ORG_UID)));
		builder.setName(organization.getString(ORG_UID));

		this.getIfExists(organization, ORG_UID, builder);
		this.getIfExists(organization, ORG_DEL_MARK, builder);
		this.getIfExists(organization, ORG_DESCRIPTION, builder);
		this.getIfExists(organization, ORG_HEAD_ORG_KEY, builder);
		this.getIfExists(organization, ORG_HAVE_SEPARATE_DIVISION, builder);
		this.getIfExists(organization, ORG_FULLNAME, builder);
		this.getIfExists(organization, ORG_SHORTNAME, builder);
		this.getIfExists(organization, ORG_IS_SEPARATE_DIVISION, builder);
		this.getIfExists(organization, ORG_PREFIX, builder);
		this.getIfExists(organization, ORG_LEGAL_PHYSICAL_PERSON, builder);

		new HashMap<>();
		LOG.ok("Builder.build: {0}", builder.build());
		return builder.build();
	}

	private ConnectorObject convertOrgUnitToConnectorObject(JSONObject orgUnit) throws IOException {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setUid(new Uid(orgUnit.getString(OU_UID)));
		builder.setName(orgUnit.getString(OU_UID));

		this.getIfExists(orgUnit, OU_UID, builder);
		this.getIfExists(orgUnit, OU_DEL_MARK, builder);
		this.getIfExists(orgUnit, OU_OWNER_KEY, builder);
		this.getIfExists(orgUnit, OU_PARENT_KEY, builder);
		this.getIfExists(orgUnit, OU_CODE, builder);
		this.getIfExists(orgUnit, OU_DESCRIPTION, builder);
		this.getIfExists(orgUnit, OU_IS_SEPARATE_DIVISION, builder);
		this.getIfExists(orgUnit, OU_FORMED, builder);
		this.getIfExists(orgUnit, OU_DISBANDED, builder);
		this.getIfExists(orgUnit, OU_HEAD_ORG, builder);
		this.getIfExists(orgUnit, OU_PREDEF, builder);

		new HashMap<>();
		LOG.ok("Builder.build: {0}", builder.build());
		return builder.build();
	}

	private String ukNesting(String nestingValue) {
		String newNestValue = "";
		String dubNestValue = "";
		String returnedNestingValue = "";
		newNestValue = nestingValue.replaceAll("ООО ", "");
		newNestValue = newNestValue.replaceAll("УК ", "");
		newNestValue = newNestValue.replaceAll("Управляющая Компания ", "");
		int newNestingValueLength = newNestValue.split(" ").length;

		if (newNestingValueLength > 1) {
			for (int i = 0; i < newNestingValueLength; i++) {
				String currNewNestValue[] = newNestValue.split(" ", 0);
				dubNestValue = dubNestValue + currNewNestValue[i].substring(0, 1).toUpperCase();
			}
			returnedNestingValue = dubNestValue;
		} else {
			returnedNestingValue = newNestValue.replaceAll("[^а-яА-Я0-9]", "");
		}

		return returnedNestingValue;
	}

	@SuppressWarnings("unlikely-arg-type")
	private ConnectorObject convertCompanyToConnectorObject(JSONObject company) throws IOException {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setUid(new Uid(company.getString(CS_COMPANY_UID)));
		builder.setName(company.getString(CS_COMPANY_UID));

		this.getIfExists(company, CS_COMPANY_UID, builder);
		this.getIfExists(company, CS_DEL_MARK, builder);
		this.getIfExists(company, CS_PARENT_KEY, builder);
		this.getIfExists(company, CS_CODE, builder);
		this.getIfExists(company, CS_DESCRIPTION, builder);
		this.getIfExists(company, CS_SOURCE, builder);
		this.getIfExists(company, CS_CORR_UL_STRUCTURE, builder);
		this.getIfExists(company, CS_PREDEF, builder);

		String nestingValue = company.getString(CS_DESCRIPTION).replaceAll("\"", "");
		nestingValue = nestingValue.replaceAll("-", " ");
		nestingValue = nestingValue.replaceAll("№", "");
		JSONObject currCompany = company;
		int nestingValueLength = nestingValue.split(" ").length;
		List<String> ukArrayList = new ArrayList<String>();

		if (company.getString(CS_PARENT_KEY).equals("69db2e8e-2332-11eb-bac0-005056aa6551") && nestingValueLength > 1) {

			nestingValue = ukNesting(nestingValue);
//        	String newNestValue = "";
//        	String dubNestValue = "";
//        	newNestValue = nestingValue.replaceAll("ООО ", "");
//        	newNestValue = newNestValue.replaceAll("УК ", "");
//         	newNestValue = newNestValue.replaceAll("Управляющая Компания ", "");
//         	int newNestingValueLength = newNestValue.split(" ").length;
//         	
//         	if (newNestingValueLength > 1) {
//				for (int i = 0; i < newNestingValueLength; i++) {
//					String currNewNestValue[] = newNestValue.split(" ", 0);
//					dubNestValue = dubNestValue + currNewNestValue[i].substring(0, 1).toUpperCase();
//				}
//			    nestingValue = dubNestValue;
//			} else {nestingValue = newNestValue.replaceAll("[^а-яА-Я0-9]", "");}

			ukArrayList.add(company.getString(CS_COMPANY_UID));

		} else if (!company.getString(CS_PARENT_KEY).equals("00000000-0000-0000-0000-000000000000")
				&& nestingValueLength > 1) {
			String newNestValue = "";
			for (int i = 0; i < nestingValueLength; i++) {
				String currNewNestValue[] = nestingValue.split(" ", 0);
				LOG.ok("\n\nCurrNewNestValue{0}: {1}\n\n", i, currNewNestValue[i]);
				if (currNewNestValue[i].length() > 1) {
					newNestValue = newNestValue + currNewNestValue[i].substring(0, 1).toUpperCase();// +
																									// currNewNestValue[i].substring(1,
																									// 2);
				} else if (currNewNestValue[i].length() > 0) {
					newNestValue = newNestValue + currNewNestValue[i].substring(0, 1).toUpperCase();
				}
			}
			nestingValue = newNestValue;
		}

		while (!currCompany.getString(CS_PARENT_KEY).equals("00000000-0000-0000-0000-000000000000")) {
			HttpGet nestedRequest = new HttpGet(
					((zup3Configuration) this.getConfiguration()).getServiceAddress() + COMPANYSTRUCTURE + GUID_PART_1
							+ currCompany.getString(CS_COMPANY_UID) + GUID_PART_2 + "/Parent" + REQ_FORMAT);
			currCompany = this.callORequest(nestedRequest);
			String currParentKey = currCompany.getString(CS_PARENT_KEY);
			String currentNestingValue1 = currCompany.getString(CS_DESCRIPTION).replaceAll("[^а-яА-Я0-9 ]", "");
			String finString = "";
			if (currParentKey.equals("00000000-0000-0000-0000-000000000000")) {
				String currentNestingValue = currCompany.getString(CS_DESCRIPTION).replaceAll("\"", "");
				currentNestingValue = currentNestingValue.replaceAll("-", " ");
				currentNestingValue = currentNestingValue.replaceAll("№", "");
				nestingValue = nestingValue + "/" + currentNestingValue;
				break;
			}
			String currentNestingValue = currCompany.getString(CS_DESCRIPTION).replaceAll("\"", "");
			currentNestingValue = currentNestingValue.replaceAll("-", " ");
			currentNestingValue = currentNestingValue.replaceAll("№", "");
			int currentNestingValueLength = currentNestingValue.split(" ").length;

			if (Arrays.asList(ukArrayList).contains(currParentKey)
					|| currParentKey.equals("69db2e8e-2332-11eb-bac0-005056aa6551")) {
				finString = ukNesting(currentNestingValue1);
				if (!currParentKey.equals("69db2e8e-2332-11eb-bac0-005056aa6551")) {
					ukArrayList.add(currCompany.getString(CS_COMPANY_UID));
				}
			} else if (currentNestingValueLength > 1) {
				for (int i = 0; i < currentNestingValueLength; i++) {
					String newCurNestValue[] = currentNestingValue.split(" ", 0);
					if (newCurNestValue[i].length() > 1) {
						finString = finString + newCurNestValue[i].substring(0, 1).toUpperCase();// +
																									// newCurNestValue[i].substring(1,
																									// 2);
					} else if (newCurNestValue[i].length() > 0) {
						finString = finString + newCurNestValue[i].substring(0, 1).toUpperCase();
					}
				}

			} else
				finString = currentNestingValue;

			nestingValue = nestingValue + "/" + finString;
		}

		this.addAttr(builder, "Nesting", nestingValue);

		new HashMap<>();
		LOG.ok("Builder.build: {0}", builder.build());
		return builder.build();
	}

	private ConnectorObject convertContactInfoToConnectorObject(JSONObject contactInfo) {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setUid(new Uid(contactInfo.getString(CIF_UID)));
		builder.setName(contactInfo.getString(CIF_UID));

		this.getIfExists(contactInfo, CIF_UID, builder);
		this.getIfExists(contactInfo, CIF_TYPE, builder);
		this.getIfExists(contactInfo, CIF_VALUE, builder);

		new HashMap<>();
		LOG.ok("Builder.build: {0}", builder.build());
		return builder.build();
	}

	private ConnectorObject convertEmpRoleToConnectorObject(JSONObject empRole) {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setUid(new Uid(empRole.getString(ER_EMP_KEY) + "-erempkey"));
		builder.setName(empRole.getString(ER_EMP_KEY) + "-erempkey");

		this.getIfExists(empRole, ER_EMP_KEY, builder);
		this.getIfExists(empRole, ER_EMP_ROLE, builder);

		new HashMap<>();
		LOG.ok("Builder.build: {0}", builder.build());
		return builder.build();
	}

	private ConnectorObject convertIndividualToConnectorObject(JSONObject individual) throws IOException {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setUid(new Uid(individual.getString(IND_UID)));
		builder.setName(individual.getString(IND_UID));

		this.getIfExists(individual, IND_PARENT_KEY, builder);
		this.getIfExists(individual, IND_IS_FOLDER, builder);
		this.getIfExists(individual, IND_UID, builder);
		this.getIfExists(individual, IND_DESCRIPTION, builder);
		this.getIfExists(individual, IND_ACCESS_GROUP_KEY, builder);
		this.getIfExists(individual, IND_GENDER, builder);
		this.getIfExists(individual, IND_FULLNAME, builder);
		this.getIfExists(individual, IND_MIDDLE_NAME, builder);
		this.getIfExists(individual, IND_SURNAME, builder);
		this.getIfExists(individual, IND_NAME, builder);
//        this.getIfExists(individual, IND_BIRTHDATE, builder);
		ZonedDateTime dateValue = LocalDateTime.parse(individual.getString(IND_BIRTHDATE), formatter).atZone(timeZone);
		this.addAttr(builder, IND_BIRTHDATE, dateValue);

		this.addAttr(builder, "borderDate", borderDate);

		this.getIfExists(individual, IND_INN, builder);
		this.getIfExists(individual, IND_SNILS, builder);

		new HashMap<>();
//        LOG.ok("Builder.build: {0}", builder.build());
//        System.out.format("%10s%55s", indCounter, builder.build().getAttributeByName(IND_DESCRIPTION).getValue() + "\n");
		indCounter++;
//        System.out.format("%55s%60s%30s", ANSI_BLUE + "Физлицо " + individual.getString(IND_DESCRIPTION), ANSI_CYAN + "айди - " + individual.getString(IND_UID), ANSI_GREEN + "порядковый номер " + indCounter + "\n");
		FileOutputStream fos = new FileOutputStream("F:\\conn-srvn\\OutToFile\\outZup.txt", true);
		fos.write((builder.build().toString() + "\n").getBytes());
		fos.close();

		return builder.build();
	}

	private ConnectorObject convertMainEmpOfIndividualToConnectorObject(JSONObject mainEmp) {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setUid(new Uid(mainEmp.getString(MEI_PERS_KEY) + "-emiperskey"));
		builder.setName(mainEmp.getString(MEI_PERS_KEY) + "-emiperskey");

		this.getIfExists(mainEmp, MEI_HEAD_ORG_KEY, builder);
		this.getIfExists(mainEmp, MEI_PERS_KEY, builder);
//        this.getIfExists(mainEmp, MEI_START_DATE, builder);
		ZonedDateTime dateValue = LocalDateTime.parse(mainEmp.getString(MEI_START_DATE), formatter).atZone(timeZone);
		this.addAttr(builder, MEI_START_DATE, dateValue);

//        this.getIfExists(mainEmp, MEI_END_DATE, builder);
		ZonedDateTime dateValue1 = LocalDateTime.parse(mainEmp.getString(MEI_END_DATE), formatter).atZone(timeZone);
		this.addAttr(builder, MEI_END_DATE, dateValue1);

		this.getIfExists(mainEmp, MEI_EMP_KEY, builder);

		new HashMap<>();
		LOG.ok("Builder.build: {0}", builder.build());
		return builder.build();
	}

	private ConnectorObject convertStaffToConnectorObject(JSONObject staff) {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setUid(new Uid(staff.getString(SL_UID)));
		builder.setName(staff.getString(SL_UID));

		this.getIfExists(staff, SL_UID, builder);
		this.getIfExists(staff, SL_DEL_MARK, builder);
		this.getIfExists(staff, SL_OWNER_KEY, builder);
		this.getIfExists(staff, SL_PARENT_KEY, builder);
		this.getIfExists(staff, SL_DESCRIPTION, builder);
		this.getIfExists(staff, SL_OU_KEY, builder);
		this.getIfExists(staff, SL_POSITION_KEY, builder);

		new HashMap<>();
		LOG.ok("Builder.build: {0}", builder.build());
		return builder.build();
	}

	private ConnectorObject convertStaffInCsToConnectorObject(JSONObject staff1) throws IOException {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setUid(new Uid(staff1.getString(StuffInCSPosition)));
		builder.setName(staff1.getString(StuffInCSPosition));

		this.getIfExists(staff1, StuffInCSOU, builder);

		HttpGet descriptionRequest = new HttpGet(((zup3Configuration) this.getConfiguration()).getServiceAddress()
				+ "/InformationRegister_МестоПозицииШтатногоРасписанияВСтруктуреПредприятия(guid'"
				+ staff1.getString(StuffInCSPosition) + "')/Позиция" + REQ_FORMAT);
		JSONObject descObject = this.callORequest(descriptionRequest);

		String currDescription = descObject.getString("Description");

		this.addAttr(builder, "Description", currDescription);

		new HashMap<>();
		LOG.ok("Builder.build: {0}", builder.build());
		return builder.build();
	}

	private ConnectorObject convertSubordinationToConnectorObject(JSONObject subordination) {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setUid(new Uid(subordination.getString(SUBO_ORG_KEY) + "-suboorgkey"));
		builder.setName(subordination.getString(SUBO_ORG_KEY) + "-suboorgkey");

		this.getIfExists(subordination, SUBO_OU_KEY, builder);
		this.getIfExists(subordination, SUBO_PAR_ORG_KEY, builder);
		this.getIfExists(subordination, SUBO_ORG_KEY, builder);

		new HashMap<>();
		LOG.ok("Builder.build: {0}", builder.build());
		return builder.build();
	}

	private ConnectorObject convertUserToConnectorObject(JSONObject user) {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setUid(new Uid(user.getString(USER_UID)));
		builder.setName(user.getString(USER_UID));

		this.getIfExists(user, USER_UID, builder);
		this.getIfExists(user, USER_DEL_MARK, builder);
		this.getIfExists(user, USER_DESCRIPTION, builder);
		this.getIfExists(user, USER_NOT_VALID, builder);
		this.getIfExists(user, USER_OU_KEY, builder);
		this.getIfExists(user, USER_PERS_KEY, builder);
		this.getIfExists(user, USER_SERVICE, builder);
		this.getIfExists(user, USER_PREPARED, builder);
		this.getIfExists(user, USER_BD_ID, builder);
		this.getIfExists(user, USER_PREDEF, builder);

		new HashMap<>();
		LOG.ok("Builder.build: {0}", builder.build());
		return builder.build();
	}

	private ConnectorObject convertPhotoToConnectorObject(JSONObject photo) {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setUid(new Uid(photo.getString(PHOTO_IND_KEY)));
		builder.setName(photo.getString(PHOTO_IND_KEY));

		this.getIfExists(photo, PHOTO_IND_KEY, builder);
		this.getIfExists(photo, PHOTO_DATA, builder);

		new HashMap<>();
		LOG.ok("Builder.build: {0}", builder.build());
		return builder.build();
	}

	private ConnectorObject convertMainJobToConnectorObject(JSONObject mainJob) {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setUid(new Uid(mainJob.getString(MAIN_JOB_INDIVIDUAL) + '_' + mainJob.getString(MAIN_JOB_EMP)));
		builder.setName(mainJob.getString(MAIN_JOB_INDIVIDUAL) + '_' + mainJob.getString(MAIN_JOB_EMP));

		this.getIfExists(mainJob, MAIN_JOB_EMP, builder);
//        this.getIfExists(mainJob, "Period", builder);
		ZonedDateTime dateValue = LocalDateTime.parse(mainJob.getString("Period"), formatter).atZone(timeZone);
		this.addAttr(builder, "Period", dateValue);

		this.getIfExists(mainJob, MAIN_JOB_HEAD_ORG, builder);
		this.getIfExists(mainJob, MAIN_JOB_INDIVIDUAL, builder);
		this.getIfExists(mainJob, MAIN_JOB_EMP_TYPE, builder);

		new HashMap<>();
		LOG.ok("Builder.build: {0}", builder.build());
		return builder.build();
	}

	private ConnectorObject convertGphContractToConnectorObject(JSONObject gphContract) {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setUid(new Uid(gphContract.getString(GPH_IND_KEY)));
		builder.setName(gphContract.getString(GPH_IND_KEY));

		this.getIfExists(gphContract, GPH_ORG, builder);
		this.getIfExists(gphContract, GPH_EMP_KEY, builder);
		this.getIfExists(gphContract, GPH_IND_KEY, builder);

		ZonedDateTime gphValidFrom = LocalDateTime.parse(gphContract.getString(GPH_VALID_FROM), formatter)
				.atZone(timeZone);
		this.addAttr(builder, GPH_VALID_FROM, gphValidFrom);

		ZonedDateTime gphValidTo = LocalDateTime.parse(gphContract.getString(GPH_VALID_TO), formatter).atZone(timeZone);
		this.addAttr(builder, GPH_VALID_TO, gphValidTo);

		new HashMap<>();
		LOG.ok("Builder.build: {0}", builder.build());
		return builder.build();
	}

	private ConnectorObject convertFIOToConnectorObject(JSONObject fio) {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setUid(new Uid(fio.getString(FIO_PERSONA)));
		builder.setName(fio.getString(FIO_PERSONA));

//		this.getIfExists(fio, FIO_PERIOD, builder);

		ZonedDateTime dateValue = LocalDateTime.parse(fio.getString(FIO_PERIOD), formatter).atZone(timeZone);
		this.addAttr(builder, FIO_PERIOD, dateValue);

		this.getIfExists(fio, FIO_PERSONA, builder);
		this.getIfExists(fio, FIO_FNAME, builder);
		this.getIfExists(fio, FIO_GNAME, builder);
		this.getIfExists(fio, FIO_ANAME, builder);

		new HashMap<>();
		LOG.ok("Builder.build: {0}", builder.build());
		return builder.build();
	}

	private ConnectorObject convertEmpStatusToConnectorObject(JSONObject statusRecordSet,
			ZonedDateTime empStatusStartDate, ZonedDateTime empStatusEndDate, ZonedDateTime empStatusEstimEndDate) {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setUid(new Uid(statusRecordSet.getString(EMP_STATUS_RECORDER)));
		builder.setName(statusRecordSet.getString(EMP_STATUS_RECORDER) + "--"
				+ statusRecordSet.getString(EMP_STATUS_RECORDER_TYPE));

		this.getIfExists(statusRecordSet, EMP_STATUS_RECORDER, builder);
		this.getIfExists(statusRecordSet, EMP_STATUS_RECORDER_TYPE, builder);
		this.getIfExists(statusRecordSet, EMP_STATUS_KEY, builder);
		this.getIfExists(statusRecordSet, EMP_STATUS_VALUE, builder);

		this.addAttr(builder, EMP_STATUS_START_DATE, empStatusStartDate);
		this.addAttr(builder, EMP_STATUS_END_DATE, empStatusEndDate);
		this.addAttr(builder, EMP_STATUS_ESTIMATED_END_DATE, empStatusEstimEndDate);

		new HashMap<>();
		LOG.ok("Builder.build: {0}", builder.build());
		return builder.build();
	}

	private void getIfExists(JSONObject object, String attrName, ConnectorObjectBuilder builder) {
//		    LOG.error("\n\n{0} - атрибут {1}, класс - {2}\n\n", object.get(IND_DESCRIPTION), attrName, object.get(attrName).getClass());
		if (object.has(attrName) && object.get(attrName) != null && object.get(attrName).toString().length() > 2
				&& !JSONObject.NULL.equals(object.get(attrName))) {
			if (object.get(attrName) instanceof String) {
				this.addAttr(builder, attrName, object.getString(attrName));
			} else if (object.get(attrName) instanceof Boolean) {
				this.addAttr(builder, attrName, object.getBoolean(attrName));
			} else if (object.get(attrName) instanceof Integer) {
				this.addAttr(builder, attrName, object.getInt(attrName));
			} else if (object.get(attrName) instanceof JSONArray) {
				this.addAttr(builder, attrName, object.getJSONArray(attrName).toString());
			}
		}
	}

	protected String getStringAttr(Set<Attribute> attributes, String attrName) throws InvalidAttributeValueException {
		return getAttr(attributes, attrName, String.class);
	}

	protected String getStringAttr(Set<Attribute> attributes, String attrName, String defaultVal)
			throws InvalidAttributeValueException {
		return getAttr(attributes, attrName, String.class, defaultVal);
	}

	protected String getStringAttr(Set<Attribute> attributes, String attrName, String defaultVal, String defaultVal2,
			boolean notNull) throws InvalidAttributeValueException {
		String ret = getAttr(attributes, attrName, String.class, defaultVal);
		if (notNull && ret == null) {
			if (defaultVal == null)
				return defaultVal2;
			return defaultVal;
		}
		return ret;
	}

	protected String getStringAttr(Set<Attribute> attributes, String attrName, String defaultVal, boolean notNull)
			throws InvalidAttributeValueException {
		String ret = getAttr(attributes, attrName, String.class, defaultVal);
		if (notNull && ret == null)
			return defaultVal;
		return ret;
	}

	protected <T> T getAttr(Set<Attribute> attributes, String attrName, Class<T> type)
			throws InvalidAttributeValueException {
		return getAttr(attributes, attrName, type, null);
	}

	protected <T> T getAttr(Set<Attribute> attributes, String attrName, Class<T> type, T defaultVal, boolean notNull)
			throws InvalidAttributeValueException {
		T ret = getAttr(attributes, attrName, type, defaultVal);
		if (notNull && ret == null) {
			return defaultVal;
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	public <T> T getAttr(Set<Attribute> attributes, String attrName, Class<T> type, T defaultVal)
			throws InvalidAttributeValueException {
		for (Attribute attr : attributes) {
			if (attrName.equals(attr.getName())) {
				List<Object> vals = attr.getValue();
				if (vals == null || vals.isEmpty()) {
					// set empty value
					return null;
				}
				if (vals.size() == 1) {
					Object val = vals.get(0);
					if (val == null) {
						// set empty value
						return null;
					}
					if (type.isAssignableFrom(val.getClass())) {
						return (T) val;
					}
					throw new InvalidAttributeValueException(
							"Неподдерживаемый тип " + val.getClass() + " для атрибута " + attrName + ", значение: ");
				}
				throw new InvalidAttributeValueException(
						"Больше, чем одно значение для атрибута " + attrName + ", значения: " + vals);
			}
		}
		// set default value when attrName not in changed attributes
		return defaultVal;
	}

	protected String[] getMultiValAttr(Set<Attribute> attributes, String attrName, String[] defaultVal) {
		for (Attribute attr : attributes) {
			if (attrName.equals(attr.getName())) {
				List<Object> vals = attr.getValue();
				if (vals.isEmpty()) {
					return new String[0];
				}
				String[] ret = new String[vals.size()];
				for (int i = 0; i < vals.size(); i++) {
					Object valAsObject = vals.get(i);
					if (valAsObject == null) {
						throw new InvalidAttributeValueException(
								"Значение " + null + " не должно быть пустым для атрибута " + attrName);
					}
					String val = (String) valAsObject;
					ret[i] = val;
				}
				return ret;
			}
		}
		return defaultVal;
	}

	protected <T> T addAttr(ConnectorObjectBuilder builder, String attrPref, String attrName, T attrVal) {
		if (attrVal != null) {
			builder.addAttribute(attrPref + attrName, attrVal);
		}
		return attrVal;
	}

	protected <T> T addStringAttr(ConnectorObjectBuilder builder, String attrPref, String attrName, T attrVal) {
		if (attrVal != null) {
			builder.addAttribute(attrPref + attrName, attrVal);
		}
		return attrVal;
	}

	@Override
	public void checkAlive() {
		this.test();
	}

}
