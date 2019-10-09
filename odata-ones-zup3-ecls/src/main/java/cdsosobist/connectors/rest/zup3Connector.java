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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.ZoneId;


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


@ConnectorClass(displayNameKey = "zup3.connector.display", configurationClass = zup3Configuration.class)
public class zup3Connector extends AbstractRestConnector<zup3Configuration> implements PoolableConnector, TestOp, SchemaOp, SearchOp<zup3Filter> {

    private static final Log LOG = Log.getLog(zup3Connector.class);

    public zup3Connector() throws IOException {

    }

    private final zup3Configuration configuration = (zup3Configuration)this.getConfiguration();

//    public CurrentEmpDataCache currentEmpDataCache;

    ZoneId timeZone = ZoneId.systemDefault();
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");



    @Override
    public void test() {
        LOG.ok("This getConfiguration: {0}, and (zup3Configuration)this.getConfiguration(): {1}", configuration, (zup3Configuration)this.getConfiguration());
        if (((zup3Configuration)this.getConfiguration()).getSkipTestConnection())
            LOG.ok("Тест отменен в конфигурации", new Object[0]);
        else {
            LOG.ok("Тестируем ресурс, запрашиваем пользователя по умолчанию", new Object[0]);

            try {
                LOG.ok("getConfiguration: {0}", (zup3Configuration)this.getConfiguration());
                HttpGet request = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + REQ_FORMAT);
                LOG.ok("Запрос: {0}", request);
                this.callORequest(request);
            } catch (IOException ex) {
                throw new ConnectorIOException("Ошибка при попытке тестирования ресурса" + ex.getMessage(), ex);
            }
        }
    }


    @Override
    public void init(Configuration configuration)
    {
        super.init(configuration);
    }

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

        AttributeInfoBuilder attrPredefDataNameBuilder = new AttributeInfoBuilder(IND_PREDEF_DATA_NAME);
        ociBuilder.addAttributeInfo(attrPredefDataNameBuilder.build());

        AttributeInfoBuilder attrIndUidBuilder = new AttributeInfoBuilder(IND_UID);
        ociBuilder.addAttributeInfo(attrIndUidBuilder.build());

        AttributeInfoBuilder attrIndDescriptionBuilder = new AttributeInfoBuilder(IND_DESCRIPTION);
        ociBuilder.addAttributeInfo(attrIndDescriptionBuilder.build());

        AttributeInfoBuilder attrPredefBuilder = new AttributeInfoBuilder(IND_PREDEF);
        attrPredefBuilder.setType(boolean.class);
        ociBuilder.addAttributeInfo(attrPredefBuilder.build());

        AttributeInfoBuilder attrIndAccessGroupKeyBuilder = new AttributeInfoBuilder(IND_ACCESS_GROUP_KEY);
        ociBuilder.addAttributeInfo(attrIndAccessGroupKeyBuilder.build());

        AttributeInfoBuilder attrIndDelMarkBuilder = new AttributeInfoBuilder(IND_DEL_MARK);
        attrIndDelMarkBuilder.setType(boolean.class);
        ociBuilder.addAttributeInfo(attrIndDelMarkBuilder.build());

        AttributeInfoBuilder attrIndGenderBuilder = new AttributeInfoBuilder(IND_GENDER);
        ociBuilder.addAttributeInfo(attrIndGenderBuilder.build());

        AttributeInfoBuilder attrIndFullNameBuilder = new AttributeInfoBuilder(IND_FULLNAME);
        ociBuilder.addAttributeInfo(attrIndFullNameBuilder.build());

        AttributeInfoBuilder attrIndMiddleNameBuilder = new AttributeInfoBuilder(IND_MIDDLE_NAME);
        ociBuilder.addAttributeInfo(attrIndMiddleNameBuilder.build());

        AttributeInfoBuilder attrIndSurNameBuilder = new AttributeInfoBuilder(IND_SURNAME);
        ociBuilder.addAttributeInfo(attrIndSurNameBuilder.build());

        AttributeInfoBuilder attrIndNameInitialsBuilder = new AttributeInfoBuilder(IND_NAME_INITIALS);
        ociBuilder.addAttributeInfo(attrIndNameInitialsBuilder.build());

        AttributeInfoBuilder attrIndNameBuilder = new AttributeInfoBuilder(IND_NAME);
        ociBuilder.addAttributeInfo(attrIndNameBuilder.build());

        AttributeInfoBuilder attrIndBirthdateBuilder = new AttributeInfoBuilder(IND_BIRTHDATE);
        attrIndBirthdateBuilder.setType(ZonedDateTime.class);
        ociBuilder.addAttributeInfo(attrIndBirthdateBuilder.build());

        AttributeInfoBuilder attrIndINNBuilder = new AttributeInfoBuilder(IND_INN);
        ociBuilder.addAttributeInfo(attrIndINNBuilder.build());

        AttributeInfoBuilder attrIndSNILSBuilder = new AttributeInfoBuilder(IND_SNILS);
        ociBuilder.addAttributeInfo(attrIndSNILSBuilder.build());

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

        AttributeInfoBuilder attrPosExcludedFromStafflistBuilder = new AttributeInfoBuilder(POS_EXCLUDED_FROM_STAFFLIST);
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
        if (((zup3Configuration)this.getConfiguration()).getPassword() != null) {
            ((zup3Configuration)this.getConfiguration()).getPassword().access(chars -> sb.append(new String(chars)));
            byte[] credentials = Base64.getEncoder().encode((((zup3Configuration)this.getConfiguration()).getUsername() + ":" + sb.toString()).getBytes(StandardCharsets.UTF_8));
            request.setHeader("Authorization", "Basic " + new String(credentials, StandardCharsets.UTF_8));
        } else LOG.error("getPassword: {0}", ((zup3Configuration)this.getConfiguration()).getPassword());
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

            if (!result.contains("There is no user with ID") && !result.contains("There is no term with ID") && !result.contains("There is no ")) {
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
                                throw new InvalidAttributeValueException("Missing mandatory attribute " + key + ", full message: " + err);
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


    public FilterTranslator<zup3Filter> createFilterTranslator(ObjectClass objectClass, OperationOptions operationOptions) {
        return new zup3FilterTranslator();
    }

    public void executeQuery(ObjectClass objectClass, zup3Filter query, ResultsHandler handler, OperationOptions options) {
        LOG.info("\n\n\nObjectClass: {0}\n\n\n", objectClass);
        if (objectClass.is("__ACCOUNT__")) {
            HttpGet request = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + EMPLOYEES + REQ_FORMAT);
            try {
                this.handleEmployees(request, query, handler, options);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (objectClass.is("EmpHistorySlice")) {
            HttpGet request = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + CURRENT_EMP_DATA + REQ_FORMAT);
            try {
                this.handleEmpHistorySlice(request, handler, options);
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else if (objectClass.is("Position")) {
            HttpGet request = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + POSITIONS + REQ_FORMAT);
            try {
                this.handlePositions(request, query, handler, options);
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else if (objectClass.is("Manager")) {
            HttpGet request = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + MANAGERS + REQ_FORMAT);
            try {
                this.handleManagers(request, query, handler, options);
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else if (objectClass.is("Org")) {

            HttpGet request = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + ORGANIZATIONS + REQ_FORMAT);
            try {
                this.handleOrganisations(request, query, handler, options);
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else if (objectClass.is("OrgUnit")) {

            HttpGet request = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + ORGUNITS + REQ_FORMAT);
            try {
                this.handleOrgunits(request, query, handler, options);
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else if (objectClass.is("CompanyStructure")) {

            HttpGet request = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + COMPANYSTRUCTURE + REQ_FORMAT);
            try {
                this.handleCompanyStructure(request, query, handler, options);
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else if (objectClass.is("ContactInfo")) {

            HttpGet request = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + CONTACT_INFO + REQ_FORMAT);
            try {
                this.handleContactInfo(request, handler, options);
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else if (objectClass.is("EmpRole")) {

            HttpGet request = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + EMP_ROLES + REQ_FORMAT);
            try {
                this.handleEmpRoles(request, handler, options);
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else if (objectClass.is("Individual")) {

            HttpGet request = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + INDIVIDUALS + REQ_FORMAT);
            try {
                this.handleIndividuals(request, query, handler, options);
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else if (objectClass.is("MainEmpOfIndividuals")) {

            HttpGet request = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + MAIN_EMP_OF_INDIVIDUALS + REQ_FORMAT);
            try {
                this.handleMainEmpOfIndividuals(request, handler, options);
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else if (objectClass.is("StaffList")) {

            HttpGet request = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + STAFFLIST + REQ_FORMAT);
            try {
                this.handleStaffList(request, query, handler, options);
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else if (objectClass.is("StaffInCS")) {

            HttpGet request = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + STAFF_IN_CS + REQ_FORMAT);
            try {
                this.handleStaffInCS(request, query, handler, options);
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else if (objectClass.is("SubOfOrg")) {

            HttpGet request = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + SUBORDINATION_OF_ORGANIZATIONS + REQ_FORMAT);
            try {
                this.handleSubOfOrgs(request, handler, options);
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else if (objectClass.is("User")) {

            HttpGet request = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + USERS + REQ_FORMAT);
            try {
                this.handleUsers(request, query, handler, options);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (objectClass.is("Photo")) {

            HttpGet request = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + PHOTOS + REQ_FORMAT);
            try {
                this.handlePhotos(request, handler, options);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (objectClass.is("MainJob")) {

            HttpGet request = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + MAIN_JOB + REQ_FORMAT);
            try {
                this.handleMainJobs(request, handler, options);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    private void handleEmployees(HttpGet request, zup3Filter filter, ResultsHandler handler, OperationOptions options) throws IOException {

        CurrentEmpDataCache currentEmpDataCache = new CurrentEmpDataCache(zup3Connector.this);


        if (filter != null && filter.byUid != null) {
        	HttpGet filterRequest = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + EMPLOYEES + GUID_PART_1 + filter.byUid + GUID_PART_2 + REQ_FORMAT);
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
        
        
			JSONArray employees = this.callRequest(request);
			
			for(int i = 0; i < employees.length(); ++i) {
				JSONObject employee = employees.getJSONObject(i);
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
				}
			getConfiguration();
			employees.length();
			}
        }

    private void handlePositions(HttpGet request, zup3Filter filter, ResultsHandler handler, OperationOptions options) throws IOException {

    	if (filter != null && filter.byUid != null) {
			
    		HttpGet filteredRequest = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + POSITIONS + GUID_PART_1 + filter.byUid + GUID_PART_2 + REQ_FORMAT);
    		JSONObject position = this.callORequest(filteredRequest);
			ConnectorObject connectorObject = this.convertPositionToConnectorObject(position);
			boolean finish = !handler.handle(connectorObject);
			if (finish) {
				return;
				}
    	} else {
    	
			JSONArray positions = this.callRequest(request);
			
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

    private void handleManagers(HttpGet request, zup3Filter filter, ResultsHandler handler, OperationOptions options) throws IOException {
        
    	if (filter != null && filter.byUid != null) {
    		
    		HttpGet filteredRequest = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + MANAGERS + GUID_PART_1 + filter.byUid + GUID_PART_2 + REQ_FORMAT);
    		JSONObject manager = this.callORequest(filteredRequest);
			ConnectorObject connectorObject = this.convertManagerToConnectorObject(manager);
			boolean finish = !handler.handle(connectorObject);
			if (finish) {
				return;
				}

    	} else {
    	
			JSONArray managers = this.callRequest(request);
			
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

    private void handleEmpHistorySlice(HttpGet request, ResultsHandler handler, OperationOptions options) throws IOException {
        JSONArray events = this.callRequest(request);

        for (int i = 0; i < events.length(); ++i) {
            String mainJobIndKey = events.getJSONObject(i).getString(MAIN_JOB_INDIVIDUAL);
            HttpGet lastEventRequest = new HttpGet((((zup3Configuration)this.getConfiguration()).getServiceAddress() + CURRENT_EMP_DATA + REQ_FORMAT + LASTEVENT_REQ_1 + INFOREG_REQ_2 + mainJobIndKey + FIND_LAST_EVENT));
            JSONObject event = this.callRequest(lastEventRequest).getJSONObject(0);
            ConnectorObject connectorObject = this.convertEventToConnectorObject(event);
            boolean finish = !handler.handle(connectorObject);
            if (finish) {
                return;
            }
        }
    }

    private void handleOrganisations(HttpGet request, zup3Filter filter, ResultsHandler handler, OperationOptions options) throws IOException {
        
    	if (filter != null && filter.byUid != null) {
    		
    		HttpGet filteredRequest = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + ORGANIZATIONS + GUID_PART_1 + filter.byUid + GUID_PART_2 + REQ_FORMAT);
    		JSONObject organization = this.callORequest(filteredRequest);
			ConnectorObject connectorObject = this.convertOrganizationToConnectorObject(organization);
			boolean finish = !handler.handle(connectorObject);
			if (finish) {
				return;
				}

    	} else {
    	
    	
			JSONArray organizations = this.callRequest(request);
			
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

    private void handleOrgunits(HttpGet request, zup3Filter filter, ResultsHandler handler, OperationOptions options) throws IOException {
        
    	if (filter != null && filter.byUid != null) {
    		
    		HttpGet filteredRequest = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + ORGUNITS + GUID_PART_1 + filter.byUid + GUID_PART_2 + REQ_FORMAT);
    		JSONObject orgUnit = this.callORequest(filteredRequest);
			ConnectorObject connectorObject = this.convertOrgUnitToConnectorObject(orgUnit);
			boolean finish = !handler.handle(connectorObject);
			if (finish) {
				return;
				}
    					
		} else {
    	
			JSONArray orgUnits = this.callRequest(request);
			
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

    private void handleCompanyStructure(HttpGet request, zup3Filter filter, ResultsHandler handler, OperationOptions options) throws IOException {

    	if (filter != null && filter.byUid != null) {
    		
    		HttpGet filteredRequest = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + COMPANYSTRUCTURE + GUID_PART_1 + filter.byUid + GUID_PART_2 + REQ_FORMAT);
    		JSONObject company = this.callORequest(filteredRequest);
			ConnectorObject connectorObject = this.convertCompanyToConnectorObject(company);
			boolean finish = !handler.handle(connectorObject);
			if (finish) {
				return;
				}
			
		} else {
    	
			JSONArray companies = this.callRequest(request);
			
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

    private void handleContactInfo(HttpGet request, ResultsHandler handler, OperationOptions options) throws IOException {
        JSONArray contacts = this.callRequest(request);

        for (int i = 0; i < contacts.length(); i++) {
            JSONObject contactInfo = contacts.getJSONObject(i);
            getConfiguration();
            HttpGet requestContactInfoDetail = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + CONTACT_INFO + REQ_FORMAT + INFOREG_REQ_1 + CIF_UID + INFOREG_REQ_2 + contactInfo.getString(CIF_UID) + INFOREG_REQ_3);
            JSONArray contactInfos = this.callRequest(requestContactInfoDetail);
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
        JSONArray empRoles = this.callRequest(request);

        for (int i = 0; i < empRoles.length(); i++) {
            JSONObject empRole = empRoles.getJSONObject(i);
            getConfiguration();
            HttpGet requestEmpRolesDetail = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + EMP_ROLES + REQ_FORMAT + INFOREG_REQ_1 + ER_EMP_KEY + INFOREG_REQ_2 + empRole.getString(ER_EMP_KEY) + INFOREG_REQ_3);
            JSONArray roles = this.callRequest(requestEmpRolesDetail);
            for (int j = 0; j <roles.length(); j++) {
                JSONObject role = roles.getJSONObject(j);
                ConnectorObject connectorObject = this.convertEmpRoleToConnectorObject(role);
                boolean finish = !handler.handle(connectorObject);
                if (finish) {
                    return;
                }
            }
        }
    }

    private void handleIndividuals(HttpGet request, zup3Filter filter, ResultsHandler handler, OperationOptions options) throws IOException {
    	
    	if (filter != null && filter.byUid != null) {
    		
    		HttpGet filteredRequest = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + INDIVIDUALS + GUID_PART_1 + filter.byUid + GUID_PART_2 + REQ_FORMAT);
    		JSONObject individual = this.callORequest(filteredRequest);

    		if (individual.get(IND_IS_FOLDER).toString().matches("false")) {
			ConnectorObject connectorObject = this.convertIndividualToConnectorObject(individual);
			
			boolean finish = !handler.handle(connectorObject);
			if (finish) {
				return;
				}
    		}
		} else {
    	
			JSONArray individuals = this.callRequest(request);
			
			for (int i = 0; i < individuals.length(); i++) {
				JSONObject individual = individuals.getJSONObject(i);
	    		if (individual.get(IND_IS_FOLDER).toString().matches("false")) {
				ConnectorObject connectorObject = this.convertIndividualToConnectorObject(individual);
				boolean finish = !handler.handle(connectorObject);
				if (finish) {
					return;
					}
				}
			}
		}
    }

    private void handleMainEmpOfIndividuals(HttpGet request, ResultsHandler handler, OperationOptions options) throws IOException {
        JSONArray mainEmps = this.callRequest(request);

        for (int i = 0; i < mainEmps.length(); i++) {
            JSONObject mainEmp = mainEmps.getJSONObject(i);
            getConfiguration();
            HttpGet requestEmpDetail = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + MAIN_EMP_OF_INDIVIDUALS + REQ_FORMAT + INFOREG_REQ_1 + MEI_PERS_KEY + INFOREG_REQ_2 + mainEmp.getString(MEI_PERS_KEY) + INFOREG_REQ_3);
            JSONArray emps = this.callRequest(requestEmpDetail);
            for (int j = 0; j < emps.length(); j++) {
                JSONObject emp = emps.getJSONObject(j);
                ConnectorObject connectorObject = this.convertMainEmpOfIndividualToConnectorObject(emp);
                boolean finish = !handler.handle(connectorObject);
                if (finish) {
                    return;
                }
            }
        }
    }

    private void handleStaffList(HttpGet request, zup3Filter filter, ResultsHandler handler, OperationOptions options) throws IOException {
    	
    	if (filter != null && filter.byUid != null) {
    		
    		HttpGet filteredRequest = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + STAFFLIST + GUID_PART_1 + filter.byUid + GUID_PART_2 + REQ_FORMAT);
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

    private void handleStaffInCS(HttpGet request, zup3Filter filter, ResultsHandler handler, OperationOptions options) throws IOException {
    	
    	if (filter != null && filter.byUid != null) {
    		
    		HttpGet filteredRequest = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + STAFF_IN_CS + GUID_PART_1 + filter.byUid + GUID_PART_2 + REQ_FORMAT);
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

    private void handleUsers(HttpGet request, zup3Filter filter, ResultsHandler handler, OperationOptions options) throws IOException {
    	
    	if (filter != null && filter.byUid != null) {
    		
    		HttpGet filteredRequest = new HttpGet(((zup3Configuration)this.getConfiguration()).getServiceAddress() + USERS + GUID_PART_1 + filter.byUid + GUID_PART_2 + REQ_FORMAT);
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

    private void handlePhotos(HttpGet request, ResultsHandler handler, OperationOptions options) throws IOException {
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

    private void handleMainJobs(HttpGet request, ResultsHandler handler, OperationOptions options) throws IOException {
        JSONArray mainJobs = this.callRequest(request);

        for (int i = 0; i < mainJobs.length(); i++) {
            JSONObject mainJob = mainJobs.getJSONObject(i);
            ConnectorObject connectorObject = this.convertMainJobToConnectorObject(mainJob);
            boolean finish = !handler.handle(connectorObject);
            if (finish) {
                return;
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

        boolean enable = !employee.getBoolean(EMP_IN_ARCHIVE);
        this.addAttr(builder, OperationalAttributes.ENABLE_NAME, enable);

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

        boolean enable = !employee.getBoolean(EMP_IN_ARCHIVE);
        this.addAttr(builder, OperationalAttributes.ENABLE_NAME, enable);

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

    private ConnectorObject convertIndividualToConnectorObject(JSONObject individual) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        builder.setUid(new Uid(individual.getString(IND_UID)));
        builder.setName(individual.getString(IND_UID));

        this.getIfExists(individual, IND_PARENT_KEY, builder);
        this.getIfExists(individual, IND_IS_FOLDER, builder);
        this.getIfExists(individual, IND_PREDEF_DATA_NAME, builder);
        this.getIfExists(individual, IND_UID, builder);
        this.getIfExists(individual, IND_DESCRIPTION, builder);
        this.getIfExists(individual, IND_PREDEF, builder);
        this.getIfExists(individual, IND_ACCESS_GROUP_KEY, builder);
        this.getIfExists(individual, IND_DEL_MARK, builder);
        this.getIfExists(individual, IND_GENDER, builder);
        this.getIfExists(individual, IND_FULLNAME, builder);
        this.getIfExists(individual, IND_MIDDLE_NAME, builder);
        this.getIfExists(individual, IND_SURNAME, builder);
        this.getIfExists(individual, IND_NAME_INITIALS, builder);
        this.getIfExists(individual, IND_NAME, builder);
//        this.getIfExists(individual, IND_BIRTHDATE, builder);
		ZonedDateTime dateValue = LocalDateTime.parse(individual.getString(IND_BIRTHDATE), formatter).atZone(timeZone);
		this.addAttr(builder, IND_BIRTHDATE, dateValue);
        
        this.getIfExists(individual, IND_INN, builder);
        this.getIfExists(individual, IND_SNILS, builder);

        new HashMap<>();
        LOG.ok("Builder.build: {0}", builder.build());
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

        this.getIfExists(staff,SL_UID, builder);
        this.getIfExists(staff,SL_DEL_MARK, builder);
        this.getIfExists(staff,SL_OWNER_KEY, builder);
        this.getIfExists(staff,SL_PARENT_KEY, builder);
        this.getIfExists(staff,SL_DESCRIPTION, builder);
        this.getIfExists(staff,SL_OU_KEY, builder);
        this.getIfExists(staff,SL_POSITION_KEY, builder);

        new HashMap<>();
        LOG.ok("Builder.build: {0}", builder.build());
        return builder.build();
    }

    private ConnectorObject convertStaffInCsToConnectorObject(JSONObject staff1) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        builder.setUid(new Uid(staff1.getString(StuffInCSPosition)));
        builder.setName(staff1.getString(StuffInCSPosition));

        this.getIfExists(staff1,StuffInCSOU, builder);

        new HashMap<>();
        LOG.ok("Builder.build: {0}", builder.build());
        return builder.build();
    }

    private ConnectorObject convertSubordinationToConnectorObject(JSONObject subordination) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        builder.setUid(new Uid(subordination.getString(SUBO_ORG_KEY) + "-suboorgkey"));
        builder.setName(subordination.getString(SUBO_ORG_KEY) + "-suboorgkey");

        this.getIfExists(subordination,SUBO_OU_KEY, builder);
        this.getIfExists(subordination,SUBO_PAR_ORG_KEY, builder);
        this.getIfExists(subordination,SUBO_ORG_KEY, builder);

        new HashMap<>();
        LOG.ok("Builder.build: {0}", builder.build());
        return builder.build();
    }

    private ConnectorObject convertUserToConnectorObject(JSONObject user) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        builder.setUid(new Uid(user.getString(USER_UID)));
        builder.setName(user.getString(USER_UID));

        this.getIfExists(user,USER_UID, builder);
        this.getIfExists(user,USER_DEL_MARK, builder);
        this.getIfExists(user,USER_DESCRIPTION, builder);
        this.getIfExists(user,USER_NOT_VALID, builder);
        this.getIfExists(user,USER_OU_KEY, builder);
        this.getIfExists(user,USER_PERS_KEY, builder);
        this.getIfExists(user,USER_SERVICE, builder);
        this.getIfExists(user,USER_PREPARED, builder);
        this.getIfExists(user,USER_BD_ID, builder);
        this.getIfExists(user,USER_PREDEF, builder);

        new HashMap<>();
        LOG.ok("Builder.build: {0}", builder.build());
        return builder.build();
    }

    private ConnectorObject convertPhotoToConnectorObject(JSONObject photo) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        builder.setUid(new Uid(photo.getString(PHOTO_IND_KEY)));
        builder.setName(photo.getString(PHOTO_IND_KEY));

        this.getIfExists(photo,PHOTO_IND_KEY, builder);
        this.getIfExists(photo,PHOTO_DATA, builder);

        new HashMap<>();
        LOG.ok("Builder.build: {0}", builder.build());
        return builder.build();
    }

    private ConnectorObject convertMainJobToConnectorObject(JSONObject mainJob) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        builder.setUid(new Uid(mainJob.getString(MAIN_JOB_EMP)));
        builder.setName(mainJob.getString(MAIN_JOB_EMP));

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

    private void getIfExists(JSONObject object, String attrName, ConnectorObjectBuilder builder) {
        if (object.has(attrName) && object.get(attrName) != null && !JSONObject.NULL.equals(object.get(attrName))) {
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

    protected String getStringAttr(Set<Attribute> attributes, String attrName, String defaultVal) throws InvalidAttributeValueException {
        return getAttr(attributes, attrName, String.class, defaultVal);
    }

    protected String getStringAttr(Set<Attribute> attributes, String attrName, String defaultVal, String defaultVal2, boolean notNull) throws InvalidAttributeValueException {
        String ret = getAttr(attributes, attrName, String.class, defaultVal);
        if (notNull && ret == null) {
            if (defaultVal == null)
                return defaultVal2;
            return defaultVal;
        }
        return ret;
    }

    protected String getStringAttr(Set<Attribute> attributes, String attrName, String defaultVal, boolean notNull) throws InvalidAttributeValueException {
        String ret = getAttr(attributes, attrName, String.class, defaultVal);
        if (notNull && ret == null)
            return defaultVal;
        return ret;
    }

    protected <T> T getAttr(Set<Attribute> attributes, String attrName, Class<T> type) throws InvalidAttributeValueException {
        return getAttr(attributes, attrName, type, null);
    }

    protected <T> T getAttr(Set<Attribute> attributes, String attrName, Class<T> type, T defaultVal, boolean notNull) throws InvalidAttributeValueException {
        T ret = getAttr(attributes, attrName, type, defaultVal);
        if (notNull && ret == null) {
            return defaultVal;
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttr(Set<Attribute> attributes, String attrName, Class<T> type, T defaultVal) throws InvalidAttributeValueException {
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
                    throw new InvalidAttributeValueException("Неподдерживаемый тип " + val.getClass() + " для атрибута " + attrName + ", значение: ");
                }
                throw new InvalidAttributeValueException("Больше, чем одно значение для атрибута " + attrName + ", значения: " + vals);
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
                        throw new InvalidAttributeValueException("Значение " + null + " не должно быть пустым для атрибута " + attrName);
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
