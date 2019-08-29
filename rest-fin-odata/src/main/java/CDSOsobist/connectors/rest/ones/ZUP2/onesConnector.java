

package CDSOsobist.connectors.rest.ones.ZUP2;

import com.evolveum.polygon.rest.AbstractRestConnector;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ByteArrayEntity;
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
import org.identityconnectors.framework.spi.operations.DeleteOp;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;


@ConnectorClass(
        displayNameKey = "connector.ones.display",
        configurationClass = onesConfiguration.class
)

public class onesConnector extends AbstractRestConnector<onesConfiguration> implements PoolableConnector, TestOp, SchemaOp, DeleteOp, SearchOp<onesFilter> {
    private static final Log LOG = Log.getLog(onesConnector.class);
    public onesConnector() {

    }

    private static final onesConnector connector = new onesConnector();
    private final onesConfiguration configuration = (onesConfiguration)this.getConfiguration();



    public static void main(String[] args) {
        connector.test();
    }



    @Override
    public void test() {
        LOG.ok("This getConfiguration: {0}, and (onesConfiguration)this.getConfiguration(): {1}", configuration, (onesConfiguration)this.getConfiguration());
        if (((onesConfiguration)this.getConfiguration()).getSkipTestConnection())
            LOG.ok("Тест отменен в конфигурации", new Object[0]);
        else {
            LOG.ok("Тестируем ресурс, запрашиваем пользователя по умолчанию", new Object[0]);

            try {
                LOG.ok("getConfiguration: {0}", (onesConfiguration)this.getConfiguration());
                HttpGet request = new HttpGet(((onesConfiguration)this.getConfiguration()).getServiceAddress() + ResourceHandler.EMPLOYEES + "(guid'48e229e8-dd1b-11df-a39e-e0cb4ed5f378')" + ResourceHandler.REQ_FORMAT);
                LOG.ok("Запрос: {0}", request);
                this.callORequest(request);
            } catch (IOException ex) {
                throw new ConnectorIOException("Ошибка при попытке тестирования ресурса" + ex.getMessage(), ex);
            }
        }
    }


    public void init(Configuration configuration) {
        super.init(configuration);
    }



    public Schema schema() {
        SchemaBuilder schemaBuilder = new SchemaBuilder(onesConnector.class);
        this.buildAccountObjectClass(schemaBuilder);
        this.buildPositionObjectClass(schemaBuilder);
        this.buildOrganisationObjectClass(schemaBuilder);
        this.buildOrgunitObjectClass(schemaBuilder);
        return schemaBuilder.build();
    }

    private void buildAccountObjectClass(SchemaBuilder schemaBuilder) {
        ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();

        AttributeInfoBuilder attrMailBuilder = new AttributeInfoBuilder(EmployysHandler.PERS_EMAIL);
        ociBuilder.addAttributeInfo(attrMailBuilder.build());

        AttributeInfoBuilder attrEmpName = new AttributeInfoBuilder(EmployysHandler.EMP_NAME);
        ociBuilder.addAttributeInfo(attrEmpName.build());

        AttributeInfoBuilder attrEmpUid = new AttributeInfoBuilder(EmployysHandler.EMP_UID);
        ociBuilder.addAttributeInfo(attrEmpUid.build());

        AttributeInfoBuilder attrEmpDelMark = new AttributeInfoBuilder(EmployysHandler.EMP_DEL_MARK);
        attrEmpDelMark.setType(boolean.class);
        ociBuilder.addAttributeInfo(attrEmpDelMark.build());

        AttributeInfoBuilder attrEmpParentKey = new AttributeInfoBuilder(EmployysHandler.EMP_PARENT_KEY);
        ociBuilder.addAttributeInfo(attrEmpParentKey.build());

        AttributeInfoBuilder attrEmpNumber = new AttributeInfoBuilder(EmployysHandler.EMP_NUMBER);
        ociBuilder.addAttributeInfo(attrEmpNumber.build());

        AttributeInfoBuilder attrEmpPersKey = new AttributeInfoBuilder(EmployysHandler.EMP_PERS_KEY);
        ociBuilder.addAttributeInfo(attrEmpPersKey.build());

        AttributeInfoBuilder attrEmpStatus = new AttributeInfoBuilder(EmployysHandler.EMP_STATUS);
        attrEmpStatus.setType(boolean.class);
        ociBuilder.addAttributeInfo(attrEmpStatus.build());

        AttributeInfoBuilder attrEmpOrgKey = new AttributeInfoBuilder(EmployysHandler.EMP_ORG_KEY);
        ociBuilder.addAttributeInfo(attrEmpOrgKey.build());

        AttributeInfoBuilder attrEmpOpKey = new AttributeInfoBuilder(EmployysHandler.EMP_OP_KEY);
        ociBuilder.addAttributeInfo(attrEmpOpKey.build());

        AttributeInfoBuilder attrEmpContrType = new AttributeInfoBuilder(EmployysHandler.EMP_CONTR_TYPE);
        ociBuilder.addAttributeInfo(attrEmpContrType.build());

        AttributeInfoBuilder attrEmpEmpltType = new AttributeInfoBuilder(EmployysHandler.EMP_EMPLT_TYPE);
        ociBuilder.addAttributeInfo(attrEmpEmpltType.build());

        AttributeInfoBuilder attrEmpContrNumb = new AttributeInfoBuilder(EmployysHandler.EMP_CONTR_NUMB);
        ociBuilder.addAttributeInfo(attrEmpContrNumb.build());

        AttributeInfoBuilder attrEmpContrDate = new AttributeInfoBuilder(EmployysHandler.EMP_CONTR_DATE);
        ociBuilder.addAttributeInfo(attrEmpContrDate.build());

        AttributeInfoBuilder attrEmpSheduleKey = new AttributeInfoBuilder(EmployysHandler.EMP_SHEDULE_KEY);
        ociBuilder.addAttributeInfo(attrEmpSheduleKey.build());

        AttributeInfoBuilder attrEmpOrgUnitKey = new AttributeInfoBuilder(EmployysHandler.EMP_ORG_UNIT_KEY);
        ociBuilder.addAttributeInfo(attrEmpOrgUnitKey.build());

        AttributeInfoBuilder attrEmpPositionKey = new AttributeInfoBuilder(EmployysHandler.EMP_POSITION_KEY);
        ociBuilder.addAttributeInfo(attrEmpPositionKey.build());

        AttributeInfoBuilder attrEmpRatesNum = new AttributeInfoBuilder(EmployysHandler.EMP_RATES_NUM);
        ociBuilder.addAttributeInfo(attrEmpRatesNum.build());

        AttributeInfoBuilder attrEmpStartDate = new AttributeInfoBuilder(EmployysHandler.EMP_START_DATE);
        ociBuilder.addAttributeInfo(attrEmpStartDate.build());

        AttributeInfoBuilder attrEmpEndDate = new AttributeInfoBuilder(EmployysHandler.EMP_END_DATE);
        ociBuilder.addAttributeInfo(attrEmpEndDate.build());

        AttributeInfoBuilder attrEmpProbation = new AttributeInfoBuilder(EmployysHandler.EMP_PROBATION);
        ociBuilder.addAttributeInfo(attrEmpProbation.build());

        AttributeInfoBuilder attrEmpNamePostfix = new AttributeInfoBuilder(EmployysHandler.EMP_NAME_POSTFIX);
        ociBuilder.addAttributeInfo(attrEmpNamePostfix.build());

        AttributeInfoBuilder attrEmpCurrOpKey = new AttributeInfoBuilder(EmployysHandler.EMP_CURR_OP_KEY);
        ociBuilder.addAttributeInfo(attrEmpCurrOpKey.build());

        AttributeInfoBuilder attrEmpCurrOrgUnitKey = new AttributeInfoBuilder(EmployysHandler.EMP_CURR_ORG_UNIT_KEY);
        ociBuilder.addAttributeInfo(attrEmpCurrOrgUnitKey.build());

        AttributeInfoBuilder attrEmpCurrPositionKey = new AttributeInfoBuilder(EmployysHandler.EMP_CURR_POSITION_KEY);
        ociBuilder.addAttributeInfo(attrEmpCurrPositionKey.build());

        AttributeInfoBuilder attrEmpEmpltDate = new AttributeInfoBuilder(EmployysHandler.EMP_EMPLT_DATE);
        ociBuilder.addAttributeInfo(attrEmpEmpltDate.build());

        AttributeInfoBuilder attrEmpDismDate = new AttributeInfoBuilder(EmployysHandler.EMP_DISM_DATE);
        ociBuilder.addAttributeInfo(attrEmpDismDate.build());

        AttributeInfoBuilder attrEmpCurrCompanyUnitKey = new AttributeInfoBuilder(EmployysHandler.EMP_CURR_COMPANY_UNIT_KEY);
        ociBuilder.addAttributeInfo(attrEmpCurrCompanyUnitKey.build());

        AttributeInfoBuilder attrEmpCurrCompanyPositionKey = new AttributeInfoBuilder(EmployysHandler.EMP_CURR_COMPANY_POSITION_KEY);
        ociBuilder.addAttributeInfo(attrEmpCurrCompanyPositionKey.build());

        AttributeInfoBuilder attrEmpCompanyEmpltDate = new AttributeInfoBuilder(EmployysHandler.EMP_COMPANY_EMPLT_DATE);
        ociBuilder.addAttributeInfo(attrEmpCompanyEmpltDate.build());

        AttributeInfoBuilder attrEmpCompanyDismDate = new AttributeInfoBuilder(EmployysHandler.EMP_COMPANY_DISM_DATE);
        ociBuilder.addAttributeInfo(attrEmpCompanyDismDate.build());

        AttributeInfoBuilder attrEmpAnnualHolidays = new AttributeInfoBuilder(EmployysHandler.EMP_ANNUAL_HOLIDAYS);
        attrEmpAnnualHolidays.setMultiValued(true);
        attrEmpAnnualHolidays.setReturnedByDefault(false);
        ociBuilder.addAttributeInfo(attrEmpAnnualHolidays.build());

        AttributeInfoBuilder attrEmpMail = new AttributeInfoBuilder("mail");
        ociBuilder.addAttributeInfo(attrEmpMail.build());


        AttributeInfoBuilder attrEmpManagerOf = new AttributeInfoBuilder("managerOf");
        attrEmpManagerOf.setMultiValued(true);
        ociBuilder.addAttributeInfo(attrEmpManagerOf.build());


        AttributeInfoBuilder attrEmpManagerOfType = new AttributeInfoBuilder("managerOfType");
        attrEmpManagerOfType.setMultiValued(true);
        ociBuilder.addAttributeInfo(attrEmpManagerOfType.build());


        ociBuilder.addAttributeInfo(OperationalAttributeInfos.ENABLE);

        schemaBuilder.defineObjectClass(ociBuilder.build());
    }

    private void buildPositionObjectClass(SchemaBuilder schemaBuilder) {
        ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
        ociBuilder.setType("role");

        AttributeInfoBuilder attrPosNameBuilder = new AttributeInfoBuilder("pos_" + PositionsHandler.POSITION_NAME);
        ociBuilder.addAttributeInfo(attrPosNameBuilder.build());

        AttributeInfoBuilder attrPosUidBuilder = new AttributeInfoBuilder("pos_" + PositionsHandler.POSITION_UID);
        ociBuilder.addAttributeInfo(attrPosUidBuilder.build());

        AttributeInfoBuilder attrPosCatBuilder = new AttributeInfoBuilder("pos_" + PositionsHandler.POSITION_CATEGORY);
        ociBuilder.addAttributeInfo(attrPosCatBuilder.build());

        AttributeInfoBuilder attrPosDelMarkBuilder = new AttributeInfoBuilder("pos_" + PositionsHandler.POSITION_DEL_MARK);
        attrPosDelMarkBuilder.setType(boolean.class);
        ociBuilder.addAttributeInfo(attrPosDelMarkBuilder.build());

        AttributeInfoBuilder attrPosPreDefBuilder = new AttributeInfoBuilder("pos_" + PositionsHandler.POSITION_PREDEF);
        attrPosPreDefBuilder.setType(boolean.class);
        ociBuilder.addAttributeInfo(attrPosPreDefBuilder.build());

        AttributeInfoBuilder attrPosCodeBuilder = new AttributeInfoBuilder("pos_" + PositionsHandler.POSITION_CODE);
        ociBuilder.addAttributeInfo(attrPosCodeBuilder.build());

        AttributeInfoBuilder attrPosStatusBuilder = new AttributeInfoBuilder("pos_" + PositionsHandler.POSITION_STATUS);
        ociBuilder.addAttributeInfo(attrPosStatusBuilder.build());



        schemaBuilder.defineObjectClass(ociBuilder.build());
    }

    private void buildOrganisationObjectClass(SchemaBuilder schemaBuilder) {
        ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
        ociBuilder.setType("Org");

        AttributeInfoBuilder attrOrgNameBuilder = new AttributeInfoBuilder("org_" + OrganisationsHandler.ORG_NAME);
        ociBuilder.addAttributeInfo(attrOrgNameBuilder.build());


        AttributeInfoBuilder attrOrgFullnameBuilder = new AttributeInfoBuilder("org_" + OrganisationsHandler.ORG_FULLNAME);
        ociBuilder.addAttributeInfo(attrOrgFullnameBuilder.build());


        AttributeInfoBuilder attrOrgCodeBuilder = new AttributeInfoBuilder("org_" + OrganisationsHandler.ORG_CODE);
        ociBuilder.addAttributeInfo(attrOrgCodeBuilder.build());


        AttributeInfoBuilder attrOrgDelMarkBuilder = new AttributeInfoBuilder("org_" + OrganisationsHandler.ORG_DEL_MARK);
        ociBuilder.addAttributeInfo(attrOrgDelMarkBuilder.build());


        AttributeInfoBuilder attrOrgDescrBuilder = new AttributeInfoBuilder("org_" + OrganisationsHandler.ORG_DESCR);
        ociBuilder.addAttributeInfo(attrOrgDescrBuilder.build());


        AttributeInfoBuilder attrOrgHeadOrgKeyBuilder = new AttributeInfoBuilder("org_" + OrganisationsHandler.ORG_HEAD_ORG_KEY);
        ociBuilder.addAttributeInfo(attrOrgHeadOrgKeyBuilder.build());


        AttributeInfoBuilder attrOrgPredefBuilder = new AttributeInfoBuilder("org_" + OrganisationsHandler.ORG_PREDEF);
        ociBuilder.addAttributeInfo(attrOrgPredefBuilder.build());


        AttributeInfoBuilder attrOrgPrefixBuilder = new AttributeInfoBuilder("org_" + OrganisationsHandler.ORG_PREFIX);
        ociBuilder.addAttributeInfo(attrOrgPrefixBuilder.build());


        AttributeInfoBuilder attrOrgUidBuilder = new AttributeInfoBuilder("org_" + OrganisationsHandler.ORG_UID);
        ociBuilder.addAttributeInfo(attrOrgUidBuilder.build());


        schemaBuilder.defineObjectClass(ociBuilder.build());
    }

    private void buildOrgunitObjectClass(SchemaBuilder schemaBuilder) {
        ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
        ociBuilder.setType("OrgUnit");

        AttributeInfoBuilder attrOrgunitNameBuilder = new AttributeInfoBuilder("ou_" + OrganisationsHandler.ORGUNIT_NAME);
        ociBuilder.addAttributeInfo(attrOrgunitNameBuilder.build());


        AttributeInfoBuilder attrOrgunitUidBuilder = new AttributeInfoBuilder("ou_" + OrganisationsHandler.ORGUNIT_UID);
        ociBuilder.addAttributeInfo(attrOrgunitUidBuilder.build());


        AttributeInfoBuilder attrOrgunitDelMarkBuilder = new AttributeInfoBuilder("ou_" + OrganisationsHandler.ORGUNIT_DEL_MARK);
        ociBuilder.addAttributeInfo(attrOrgunitDelMarkBuilder.build());


        AttributeInfoBuilder attrOrgunitPredefBuilder = new AttributeInfoBuilder("ou_" + OrganisationsHandler.ORGUNIT_PREDEF);
        ociBuilder.addAttributeInfo(attrOrgunitPredefBuilder.build());


        AttributeInfoBuilder attrOrgunitOwnerKeyBuilder = new AttributeInfoBuilder("ou_" + OrganisationsHandler.ORGUNIT_OWNER_KEY);
        ociBuilder.addAttributeInfo(attrOrgunitOwnerKeyBuilder.build());


        AttributeInfoBuilder attrOrgunitParentKeyBuilder = new AttributeInfoBuilder("ou_" + OrganisationsHandler.ORGUNIT_PARENT_KEY);
        ociBuilder.addAttributeInfo(attrOrgunitParentKeyBuilder.build());


        AttributeInfoBuilder attrOrgunitCodeBuilder = new AttributeInfoBuilder("ou_" + OrganisationsHandler.ORGUNIT_CODE);
        ociBuilder.addAttributeInfo(attrOrgunitCodeBuilder.build());


        AttributeInfoBuilder attrOrgunitOrderBuilder = new AttributeInfoBuilder("ou_" + OrganisationsHandler.ORGUNIT_ORDER);
        ociBuilder.addAttributeInfo(attrOrgunitOrderBuilder.build());


        AttributeInfoBuilder attrOrgunitStatusBuilder = new AttributeInfoBuilder("ou_" + OrganisationsHandler.ORGUNIT_STATUS);
        ociBuilder.addAttributeInfo(attrOrgunitStatusBuilder.build());


        ociBuilder.addAttributeInfo(OperationalAttributeInfos.ENABLE);

        schemaBuilder.defineObjectClass(ociBuilder.build());
    }

    private void callRequest(HttpEntityEnclosingRequestBase request, JSONObject jo) throws IOException {
        request.setHeader("Content-Type", "application/json");
        this.authHeader(request);
        HttpEntity entity = new ByteArrayEntity(jo.toString().getBytes(StandardCharsets.UTF_8));
        request.setEntity(entity);
        CloseableHttpResponse response = this.execute(request);
        this.processYResponseErrors(response);
        String result = EntityUtils.toString(response.getEntity());
        this.closeResponse(response);
        new JSONObject(result);
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
        if (((onesConfiguration)this.getConfiguration()).getPassword() != null) {
            ((onesConfiguration)this.getConfiguration()).getPassword().access(chars -> sb.append(new String(chars)));
            byte[] credentials = Base64.getEncoder().encode((((onesConfiguration)this.getConfiguration()).getUsername() + ":" + sb.toString()).getBytes(StandardCharsets.UTF_8));
            request.setHeader("Authorization", "Basic " + new String(credentials, StandardCharsets.UTF_8));
        } else LOG.error("getPassword: {0}", ((onesConfiguration)this.getConfiguration()).getPassword());
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
                } else if (err.has(EmployysHandler.EMP_UID)) {
                    this.closeResponse(response);
                    throw new AlreadyExistsException(err.getString(EmployysHandler.EMP_UID));
                } else if (err.has(EmployysHandler.EMP_UID)) {
                    this.closeResponse(response);
                    throw new AlreadyExistsException(err.getString(EmployysHandler.EMP_UID));
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

    public void checkAlive() {
        this.test();
    }

    public void delete(ObjectClass objectClass, Uid uid, OperationOptions operationOptions) {
        try {
            JSONObject jo = new JSONObject();
            jo.put(EmployysHandler.EMP_STATUS, "false");
            HttpPut request = new HttpPut(((onesConfiguration)this.getConfiguration()).getServiceAddress() + ResourceHandler.EMPLOYEES + "(guid'" + uid.getUidValue() + "')" + ResourceHandler.REQ_FORMAT);
            this.callRequest(request, jo);

        } catch (IOException var7) {
            throw new ConnectorIOException(var7.getMessage(), var7);
        }
    }

    public FilterTranslator<onesFilter> createFilterTranslator(ObjectClass objectClass, OperationOptions operationOptions) {
        return new onesFilterTranslator();
    }

    public void executeQuery(ObjectClass objectClass, onesFilter query, ResultsHandler handler, OperationOptions options) {
        LOG.info("\n\n\nObjectClass: {0}\n\n\n", objectClass);
        if (objectClass.is("__ACCOUNT__")) {
            HttpGet request = new HttpGet(((onesConfiguration)this.getConfiguration()).getServiceAddress() + ResourceHandler.EMPLOYEES + ResourceHandler.REQ_FORMAT);
            try {
                this.handleUsers(request, handler, options);
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else if (objectClass.is("role")) {
            HttpGet request = new HttpGet(((onesConfiguration)this.getConfiguration()).getServiceAddress() + ResourceHandler.POSITIONS + ResourceHandler.REQ_FORMAT);
            try {
                this.handlePositions(request, handler, options);
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else if (objectClass.is("Org")) {

            HttpGet request = new HttpGet(((onesConfiguration)this.getConfiguration()).getServiceAddress() + ResourceHandler.ORGANISATIONS + ResourceHandler.REQ_FORMAT);
            try {
                this.handleOrganisations(request, handler, options);
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else if (objectClass.is("OrgUnit")) {

            HttpGet request = new HttpGet(((onesConfiguration)this.getConfiguration()).getServiceAddress() + ResourceHandler.ORGUNITS + ResourceHandler.REQ_FORMAT);
            try {
                this.handleOrgunits(request, handler, options);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    private void handleUsers(HttpGet request, ResultsHandler handler, OperationOptions options) throws IOException {
        JSONArray users = this.callRequest(request);

        for(int i = 0; i < users.length(); ++i) {
            JSONObject user = users.getJSONObject(i);
            getConfiguration();
            HttpGet requestUserDetail = new HttpGet(((onesConfiguration)this.getConfiguration()).getServiceAddress() + ResourceHandler.EMPLOYEES + ResourceHandler.EMP_DETAILS_1 + user.getString(EmployysHandler.EMP_UID) + ResourceHandler.EMP_DETAILS_2 + ResourceHandler.REQ_FORMAT);
            user = this.callORequest(requestUserDetail);

            if (!user.getBoolean("IsFolder") && user.get(EmployysHandler.EMP_STATUS).toString().matches("true")) {
                ConnectorObject connectorObject = this.convertUserToConnectorObject(user);
                boolean finish = !handler.handle(connectorObject);
                if (finish) {
                    return;
                }
            }
        }
        getConfiguration();
        users.length();
    }



    private void handlePositions(HttpGet request, ResultsHandler handler, OperationOptions options) throws IOException {
        JSONArray positions = this.callRequest(request);

        for (int i = 0; i < positions.length(); i++) {
            JSONObject position = positions.getJSONObject(i);
            getConfiguration();
            HttpGet requestPositionDetail = new HttpGet(((onesConfiguration)this.getConfiguration()).getServiceAddress() + ResourceHandler.POSITIONS + ResourceHandler.EMP_DETAILS_1 + position.getString(PositionsHandler.POSITION_UID) + ResourceHandler.EMP_DETAILS_2 + ResourceHandler.REQ_FORMAT);
            position = this.callORequest(requestPositionDetail);

            ConnectorObject connectorObject = this.convertPositionToConnectorObject(Objects.requireNonNull(position));
            boolean finish = !handler.handle(connectorObject);
            if (finish) {
                return;
            }
        }
    }

    private void handleOrganisations(HttpGet request, ResultsHandler handler, OperationOptions options) throws IOException {
        JSONArray organisations = this.callRequest(request);

        for (int i = 0; i < organisations.length(); i++) {
            JSONObject organisation = organisations.getJSONObject(i);
            getConfiguration();
            HttpGet requestOrganisationDetail = new HttpGet(((onesConfiguration)this.getConfiguration()).getServiceAddress() + ResourceHandler.ORGANISATIONS + ResourceHandler.EMP_DETAILS_1 + organisation.getString(OrganisationsHandler.ORG_UID) + ResourceHandler.EMP_DETAILS_2 + ResourceHandler.REQ_FORMAT);
            organisation = this.callORequest(requestOrganisationDetail);

            ConnectorObject connectorObject = this.convertOrganisationToConnectorObject(Objects.requireNonNull(organisation));
            boolean finish = !handler.handle(connectorObject);
            if (finish) {
                return;
            }
        }
    }

    private void handleOrgunits(HttpGet request, ResultsHandler handler, OperationOptions options) throws IOException {
        JSONArray orgunits = this.callRequest(request);

        for (int i = 0; i < orgunits.length(); i++) {
            JSONObject orgunit = orgunits.getJSONObject(i);
            getConfiguration();
            HttpGet requestOrgunitDetail = new HttpGet(((onesConfiguration)this.getConfiguration()).getServiceAddress() + ResourceHandler.ORGUNITS + ResourceHandler.EMP_DETAILS_1 + orgunit.getString(OrganisationsHandler.ORGUNIT_UID) + ResourceHandler.EMP_DETAILS_2 + ResourceHandler.REQ_FORMAT);
            orgunit = this.callORequest(requestOrgunitDetail);

            if (orgunit.getString(OrganisationsHandler.ORGUNIT_NAME) != null && orgunit.getString(OrganisationsHandler.ORGUNIT_NAME).length() > 3) {
                ConnectorObject connectorObject = this.convertOrgunitToConnectorObject(Objects.requireNonNull(orgunit), i);
                boolean finish = !handler.handle(connectorObject);
                if (finish) {
                    return;
                }
            }
        }
    }

    private ConnectorObject convertUserToConnectorObject(JSONObject user) throws IOException {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        builder.setUid(new Uid(user.getString(EmployysHandler.EMP_UID)));
        builder.setName(user.getString(EmployysHandler.EMP_UID));

        this.getIfExists(user, EmployysHandler.EMP_NAME, builder);
        this.getIfExists(user, EmployysHandler.EMP_UID, builder);
        this.getIfExists(user, EmployysHandler.EMP_DEL_MARK, builder);
        this.getIfExists(user, EmployysHandler.EMP_PARENT_KEY, builder);
        this.getIfExists(user, EmployysHandler.EMP_NUMBER, builder);
        this.getIfExists(user, EmployysHandler.EMP_PERS_KEY, builder);
        this.getIfExists(user, EmployysHandler.EMP_STATUS, builder);
        this.getIfExists(user, EmployysHandler.EMP_ORG_KEY, builder);
        this.getIfExists(user, EmployysHandler.EMP_OP_KEY, builder);
        this.getIfExists(user, EmployysHandler.EMP_CONTR_TYPE, builder);
        this.getIfExists(user, EmployysHandler.EMP_EMPLT_TYPE, builder);
        this.getIfExists(user, EmployysHandler.EMP_CONTR_NUMB, builder);
        this.getIfExists(user, EmployysHandler.EMP_CONTR_DATE, builder);
        this.getIfExists(user, EmployysHandler.EMP_SHEDULE_KEY, builder);
        this.getIfExists(user, EmployysHandler.EMP_ORG_UNIT_KEY, builder);
        this.getIfExists(user, EmployysHandler.EMP_POSITION_KEY, builder);
        this.getIfExists(user, EmployysHandler.EMP_RATES_NUM, builder);
        this.getIfExists(user, EmployysHandler.EMP_START_DATE, builder);
        this.getIfExists(user, EmployysHandler.EMP_END_DATE, builder);
        this.getIfExists(user, EmployysHandler.EMP_PROBATION, builder);
        this.getIfExists(user, EmployysHandler.EMP_NAME_POSTFIX, builder);
        this.getIfExists(user, EmployysHandler.EMP_CURR_OP_KEY, builder);
        this.getIfExists(user, EmployysHandler.EMP_CURR_ORG_UNIT_KEY, builder);
        this.getIfExists(user, EmployysHandler.EMP_CURR_POSITION_KEY, builder);
        this.getIfExists(user, EmployysHandler.EMP_EMPLT_DATE, builder);
        this.getIfExists(user, EmployysHandler.EMP_DISM_DATE, builder);
        this.getIfExists(user, EmployysHandler.EMP_CURR_COMPANY_UNIT_KEY, builder);
        this.getIfExists(user, EmployysHandler.EMP_CURR_COMPANY_POSITION_KEY, builder);
        this.getIfExists(user, EmployysHandler.EMP_COMPANY_EMPLT_DATE, builder);
        this.getIfExists(user, EmployysHandler.EMP_COMPANY_DISM_DATE, builder);
        this.getIfExists(user, EmployysHandler.EMP_ANNUAL_HOLIDAYS, builder);

        boolean enable = user.getBoolean(EmployysHandler.EMP_STATUS);
        this.addAttr(builder, OperationalAttributes.ENABLE_NAME, enable);

        if (user.has(EmployysHandler.EMP_PERS_KEY) && user.get(EmployysHandler.EMP_PERS_KEY).toString().length() > 0 && !user.get(EmployysHandler.EMP_PERS_KEY).toString().matches("null")) {
            HttpGet requestMail = new HttpGet(((onesConfiguration)this.getConfiguration()).getServiceAddress() + EmployysHandler.CONTACTS + ResourceHandler.REQ_FORMAT + ResourceHandler.FIND_MAIL_REF_1 + user.get(EmployysHandler.EMP_PERS_KEY).toString() + ResourceHandler.FIND_MAIL_REF_2 );
            if (Objects.requireNonNull(this.callORequest(requestMail)).getJSONArray("value").toString().length() > 3) {
                JSONObject mail = Objects.requireNonNull(this.callORequest(requestMail)).getJSONArray("value").getJSONObject(0);
                this.getMailIfExist(mail, builder);
            }

            HttpGet requestManagerOf = new HttpGet(((onesConfiguration)this.getConfiguration()).getServiceAddress() + ResourceHandler.MANAGERS + ResourceHandler.REQ_FORMAT + ResourceHandler.FIND_MANAGE_REF_1 + user.get(EmployysHandler.EMP_PERS_KEY).toString() + ResourceHandler.FIND_MANAGE_REF_2);
            if (Objects.requireNonNull(this.callORequest(requestManagerOf)).getJSONArray("value").toString().length() > 3) {

                JSONArray ja = this.callORequest(requestManagerOf).getJSONArray("value");
                List<String> managerOfArray = new ArrayList<>();

                for (int i = 0; i < ja.length(); ++i) {
                    JSONObject managerOf = Objects.requireNonNull(ja.getJSONObject(i));

                    if (managerOf.getString(ResourceHandler.MANAGEROF_TYPE).equals("Руководитель")) {
                        managerOfArray.add(managerOf.getString(ResourceHandler.MANAFEROF_OU_VALUE));
                        LOG.ok("MANAGEROF: {0}", managerOf.getString(ResourceHandler.MANAFEROF_OU_VALUE));
                    } else LOG.error("ManagerType условие: {0}, managerType: {1}", managerOf.getString(ResourceHandler.MANAGEROF_TYPE) == "Руководитель", managerOf.getString(ResourceHandler.MANAGEROF_TYPE));

                }
                builder.addAttribute("managerOf", managerOfArray.toArray());
            }


        }

        new HashMap<>();
        LOG.ok("Builder.build: {0}", builder.build());
        return builder.build();
    }

    private ConnectorObject convertPositionToConnectorObject(JSONObject position) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        String attrPref = "pos_";
        builder.setUid(new Uid(position.getString(PositionsHandler.POSITION_UID)));
        if (position.has(PositionsHandler.POSITION_NAME)) {
            builder.setName(position.getString(PositionsHandler.POSITION_UID));
        }

        this.getNotUserIfExists(position, attrPref, PositionsHandler.POSITION_NAME, builder);
        this.getNotUserIfExists(position, attrPref, PositionsHandler.POSITION_UID, builder);
        this.getNotUserIfExists(position, attrPref, PositionsHandler.POSITION_CATEGORY, builder);
        this.getNotUserIfExists(position, attrPref, PositionsHandler.POSITION_CODE, builder);
        this.getNotUserIfExists(position, attrPref, PositionsHandler.POSITION_DEL_MARK, builder);
        this.getNotUserIfExists(position, attrPref, PositionsHandler.POSITION_PREDEF, builder);
        this.getNotUserIfExists(position, attrPref, PositionsHandler.POSITION_STATUS, builder);

        if (position.has(PositionsHandler.POSITION_STATUS)) {
            boolean enable = position.getBoolean(PositionsHandler.POSITION_STATUS);
            this.addAttr(builder, OperationalAttributes.ENABLE_NAME, enable);
        }

        new HashMap<>();
        LOG.ok("Builder.build: {0}", builder.build());
        return builder.build();
    }

    private ConnectorObject convertOrganisationToConnectorObject(JSONObject organisation) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        String attrPref = "org_";
        builder.setUid(new Uid(organisation.getString(OrganisationsHandler.ORG_UID)));
        if (organisation.has(OrganisationsHandler.ORG_NAME)) {
            builder.setName(organisation.getString(OrganisationsHandler.ORG_UID));
        }

        this.getNotUserIfExists(organisation, attrPref, OrganisationsHandler.ORG_NAME, builder);
        this.getNotUserIfExists(organisation, attrPref, OrganisationsHandler.ORG_UID, builder);
        this.getNotUserIfExists(organisation, attrPref, OrganisationsHandler.ORG_FULLNAME, builder);
        this.getNotUserIfExists(organisation, attrPref, OrganisationsHandler.ORG_CODE, builder);
        this.getNotUserIfExists(organisation, attrPref, OrganisationsHandler.ORG_DEL_MARK, builder);
        this.getNotUserIfExists(organisation, attrPref, OrganisationsHandler.ORG_DESCR, builder);
        this.getNotUserIfExists(organisation, attrPref, OrganisationsHandler.ORG_HEAD_ORG_KEY, builder);
        this.getNotUserIfExists(organisation, attrPref, OrganisationsHandler.ORG_PREDEF, builder);
        this.getNotUserIfExists(organisation, attrPref, OrganisationsHandler.ORG_PREFIX, builder);

        new HashMap<>();
        LOG.ok("Builder.build: {0}", builder.build());
        return builder.build();
    }

    private ConnectorObject convertOrgunitToConnectorObject(JSONObject orgunit, int i) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        String attrPref = "ou_";
        builder.setUid(new Uid(orgunit.getString(OrganisationsHandler.ORGUNIT_UID)));
        builder.setName(orgunit.getString(OrganisationsHandler.ORGUNIT_UID));

        this.getNotUserIfExists(orgunit, attrPref, OrganisationsHandler.ORGUNIT_NAME, builder);
        this.getNotUserIfExists(orgunit, attrPref, OrganisationsHandler.ORGUNIT_UID, builder);
        this.getNotUserIfExists(orgunit, attrPref, OrganisationsHandler.ORGUNIT_CODE, builder);
        this.getNotUserIfExists(orgunit, attrPref, OrganisationsHandler.ORGUNIT_DEL_MARK, builder);
        this.getNotUserIfExists(orgunit, attrPref, OrganisationsHandler.ORGUNIT_ORDER, builder);
        this.getNotUserIfExists(orgunit, attrPref, OrganisationsHandler.ORGUNIT_OWNER_KEY, builder);
        this.getNotUserIfExists(orgunit, attrPref, OrganisationsHandler.ORGUNIT_PARENT_KEY, builder);
        this.getNotUserIfExists(orgunit, attrPref, OrganisationsHandler.ORGUNIT_PREDEF, builder);
        this.getNotUserIfExists(orgunit, attrPref, OrganisationsHandler.ORGUNIT_STATUS, builder);


        if (orgunit.has(OrganisationsHandler.ORGUNIT_STATUS)) {
            boolean enable = orgunit.getBoolean(OrganisationsHandler.ORGUNIT_STATUS);
            this.addAttr(builder, OperationalAttributes.ENABLE_NAME, enable);
        }

        new HashMap<>();
        LOG.ok("Builder.build: {0}", builder.build());
        return builder.build();
    }

    private void getIfExists(JSONObject object, String attrName, ConnectorObjectBuilder builder) {
        if (object.has(attrName) && object.get(attrName) != null && !JSONObject.NULL.equals(object.get(attrName))) {
            if (!object.getBoolean("IsFolder")) {
                if (object.get(attrName) instanceof String) {
                    this.addAttr(builder, attrName, object.getString(attrName));
                } else if (object.get(attrName) instanceof Boolean) {
                    this.addAttr(builder, attrName, object.getBoolean(attrName));
                } else if (object.get(attrName) instanceof Integer) {
                    this.addAttr(builder, attrName, object.getInt(attrName));
                } else if (object.get(attrName) instanceof JSONArray) {
                    this.addAttr(builder, attrName, object.getJSONArray(attrName).toString());
                }
            } //else LOG.info("Это папка");
        }
    }

    private void getMailIfExist(JSONObject object, ConnectorObjectBuilder builder) {
        if (object.has(EmployysHandler.CONTACTS_TYPE_EMAIL_VALUE) && object.get(EmployysHandler.CONTACTS_TYPE_EMAIL_VALUE) != null && !JSONObject.NULL.equals(object.get(EmployysHandler.CONTACTS_TYPE_EMAIL_VALUE))) {
            this.addAttr(builder, "mail", object.get(EmployysHandler.CONTACTS_TYPE_EMAIL_VALUE).toString());
        }
    }


    private void getNotUserIfExists(JSONObject object, String attrPref, String attrName, ConnectorObjectBuilder builder) {
        if (object.has(attrName) && object.get(attrName) != null && !JSONObject.NULL.equals(object.get(attrName))) {
            if (object.get(attrName) instanceof String) {
                this.addStringAttr(builder, attrPref, attrName, object.getString(attrName).replaceAll("\"", ""));
            } else if (object.get(attrName) instanceof Boolean) {
                this.addAttr(builder, attrPref, attrName, object.getBoolean(attrName));
            } else if (object.get(attrName) instanceof Integer) {
                this.addAttr(builder, attrPref, attrName, object.getInt(attrName));
            } else if (object.get(attrName) instanceof JSONArray) {
                this.addAttr(builder, attrPref, attrName, object.getJSONArray(attrName).toString());
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


}
