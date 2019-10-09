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

import static cdsosobist.connectors.rest.attributesHandler.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.operations.SearchApiOp;
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.ConnectorIOException;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationalAttributes;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.PoolableConnector;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.identityconnectors.framework.spi.operations.UpdateOp;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.evolveum.polygon.rest.AbstractRestConnector;

@ConnectorClass(displayNameKey = "connectorrxpt.connector.display", configurationClass = rxptConfiguration.class)

public class rxptConnector extends AbstractRestConnector<rxptConfiguration> implements PoolableConnector, TestOp, UpdateOp, SchemaOp, SearchOp<rxptFilter> {

    private static final Log LOG = Log.getLog(rxptConnector.class);

    private rxptConfiguration configuration = new rxptConfiguration();
    private tokenHandler th;

    ZoneId timeZone = ZoneId.systemDefault();
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

	public rxptConnector() {  	}

    @Override
    public rxptConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void init(Configuration configuration) {
        this.configuration = (rxptConfiguration)configuration;
        this.th = new tokenHandler();
    }

    @Override
    public void dispose() {
        configuration = null;
    }

    /**
     * Проверяем, жив ли разъем.
          *
          * <p>
          * Коннектор может провести в пуле большое количество времени, прежде чем
          * начнет использоваться. Этот метод предназначен для проверки работоспособности
     * коннектора и подтверждения того, что операции могут быть вызваны на нем
     * например, проверка того, что физическое соединение коннектора с ресурсом не
     * завершено по таймауту).
     * </p>
          *
          * <p>
          * Основное различие между этим методом и {@link TestOp # test ()} в том,
          * что этот метод должен делать только минимум, необходимый для проверки того,
          * что разъем еще жив. <code> TestOp.test () </code> делает больше:
          * Тщательная проверка среды, указанной в Конфигурации, и может из-за этого быть
     * намного медленнее.
     * </p>
          *
          * <p>
          * Этот метод можно вызывать часто.
     * </p>
          *
          * выбрасывает в RuntimeException, если соединитель больше не работает.
          */

    @Override
    public void checkAlive() {

    }

    /**
     * Описывает типы объектов, которые поддерживает {@link Connector}.
     * <p>
     *     Этот метод считается операцией, поскольку определение, поддерживаемое
     *     объектом, может потребовать информацию о конфигурации, чтобы позволить
     *     ему быть динамическым.
     *</p>
     * Специальный {@link Uid} атрибут никогда не должен появляться в схеме, так как он
     * не является истинным атрибутом объекта, скорее ссылкой на него.
     * Если объектный класс вашего ресурса имеет доступный для записи уникальный идентификатор id,
     * который отличается от его {@link Name}, то ваша схема должна содержать ресурс-специфический
     * атрибут, который представляет этот уникальный идентификатор. Например, объект учетной записи
     * Unix может содержать <I>unix_uid</I>.
     *
     * @return базовая схема, поддерживаемая этим {@link Connector}.
     */

    @Override
    public Schema schema() {
        SchemaBuilder schemaBuilder = new SchemaBuilder(rxptConnector.class);
        this.buildAccountObjectClass(schemaBuilder);
        this.buildSecondAccountObjectClass(schemaBuilder);
        this.buildOrganizationObjectClass(schemaBuilder);
        this.buildProviderObjectClass(schemaBuilder);
        this.buildContractObjectClass(schemaBuilder);
        return schemaBuilder.build();
    }

    /**
     * Здесь мы указываем коннектору, какие атрибуты будут иметь классы объекта, указанные нами
     * для построения схемы ресурса
     *
     * @param schemaBuilder Принимает Из описания схемы
     */
    private void buildAccountObjectClass(SchemaBuilder schemaBuilder) {
        ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();

        AttributeInfoBuilder attrEmpBirthDateBuilder = new AttributeInfoBuilder(EMP_BIRTH_DATE);
        attrEmpBirthDateBuilder.setType(ZonedDateTime.class);
        ociBuilder.addAttributeInfo(attrEmpBirthDateBuilder.build());

        AttributeInfoBuilder attrEmpAxUidBuilder = new AttributeInfoBuilder(EMP_AX_UID);
        ociBuilder.addAttributeInfo(attrEmpAxUidBuilder.build());

        AttributeInfoBuilder attrEmpFirstNameBuilder = new AttributeInfoBuilder(EMP_FIRST_NAME);
        ociBuilder.addAttributeInfo(attrEmpFirstNameBuilder.build());

        AttributeInfoBuilder attrEmpLastNameBuilder = new AttributeInfoBuilder(EMP_LAST_NAME);
        ociBuilder.addAttributeInfo(attrEmpLastNameBuilder.build());

        AttributeInfoBuilder attrEmpMiddleNameBuilder = new AttributeInfoBuilder(EMP_MIDDLE_NAME);
        ociBuilder.addAttributeInfo(attrEmpMiddleNameBuilder.build());

        AttributeInfoBuilder attrEmpFullNameBuilder = new AttributeInfoBuilder(EMP_FULL_NAME);
        ociBuilder.addAttributeInfo(attrEmpFullNameBuilder.build());

        AttributeInfoBuilder attrEmpINNBuilder = new AttributeInfoBuilder(EMP_INN);
        ociBuilder.addAttributeInfo(attrEmpINNBuilder.build());

        AttributeInfoBuilder attrEmpSNILSBuilder = new AttributeInfoBuilder(EMP_SNILS);
        ociBuilder.addAttributeInfo(attrEmpSNILSBuilder.build());

        AttributeInfoBuilder attrEmpMUNIDBuilder = new AttributeInfoBuilder(EMP_MUNID);
        ociBuilder.addAttributeInfo(attrEmpMUNIDBuilder.build());

        AttributeInfoBuilder attrEmp1CDataBuilder = new AttributeInfoBuilder(EMP_1CDATA);
        ociBuilder.addAttributeInfo(attrEmp1CDataBuilder.build());

        schemaBuilder.defineObjectClass(ociBuilder.build());
    }

    private void buildSecondAccountObjectClass(SchemaBuilder schemaBuilder) {
        ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
        ociBuilder.setType("SecondAccount");

        AttributeInfoBuilder attrEmpBirthDateBuilder = new AttributeInfoBuilder(EMP_BIRTH_DATE);
        attrEmpBirthDateBuilder.setType(ZonedDateTime.class);
        ociBuilder.addAttributeInfo(attrEmpBirthDateBuilder.build());

        AttributeInfoBuilder attrEmpAxUidBuilder = new AttributeInfoBuilder(EMP_AX_UID);
        ociBuilder.addAttributeInfo(attrEmpAxUidBuilder.build());

        AttributeInfoBuilder attrEmpFirstNameBuilder = new AttributeInfoBuilder(EMP_FIRST_NAME);
        ociBuilder.addAttributeInfo(attrEmpFirstNameBuilder.build());

        AttributeInfoBuilder attrEmpLastNameBuilder = new AttributeInfoBuilder(EMP_LAST_NAME);
        ociBuilder.addAttributeInfo(attrEmpLastNameBuilder.build());

        AttributeInfoBuilder attrEmpMiddleNameBuilder = new AttributeInfoBuilder(EMP_MIDDLE_NAME);
        ociBuilder.addAttributeInfo(attrEmpMiddleNameBuilder.build());

        AttributeInfoBuilder attrEmpFullNameBuilder = new AttributeInfoBuilder(EMP_FULL_NAME);
        ociBuilder.addAttributeInfo(attrEmpFullNameBuilder.build());

        AttributeInfoBuilder attrEmpINNBuilder = new AttributeInfoBuilder(EMP_INN);
        ociBuilder.addAttributeInfo(attrEmpINNBuilder.build());

        AttributeInfoBuilder attrEmpSNILSBuilder = new AttributeInfoBuilder(EMP_SNILS);
        ociBuilder.addAttributeInfo(attrEmpSNILSBuilder.build());

        AttributeInfoBuilder attrEmpMUNIDBuilder = new AttributeInfoBuilder(EMP_MUNID);
        ociBuilder.addAttributeInfo(attrEmpMUNIDBuilder.build());

        AttributeInfoBuilder attrEmp1CDataBuilder = new AttributeInfoBuilder(EMP_1CDATA);
        ociBuilder.addAttributeInfo(attrEmp1CDataBuilder.build());

        schemaBuilder.defineObjectClass(ociBuilder.build());
    }

    private void buildOrganizationObjectClass(SchemaBuilder schemaBuilder) {
        ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
        ociBuilder.setType("Organization");

        AttributeInfoBuilder attrOrgUidBuilder = new AttributeInfoBuilder(ORG_UID);
        ociBuilder.addAttributeInfo(attrOrgUidBuilder.build());

        AttributeInfoBuilder attrOrgNameBuilder = new AttributeInfoBuilder(ORG_NAME);
        ociBuilder.addAttributeInfo(attrOrgNameBuilder.build());

        schemaBuilder.defineObjectClass(ociBuilder.build());
    }


    private void buildProviderObjectClass(SchemaBuilder schemaBuilder) {
        ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
        ociBuilder.setType("Provider");


        AttributeInfoBuilder attrProvUidBuilder = new AttributeInfoBuilder(PROV_UID);
        ociBuilder.addAttributeInfo(attrProvUidBuilder.build());

        AttributeInfoBuilder attrProvNameBuilder = new AttributeInfoBuilder(PROV_NAME);
        ociBuilder.addAttributeInfo(attrProvNameBuilder.build());

        schemaBuilder.defineObjectClass(ociBuilder.build());
    }

    private void buildContractObjectClass(SchemaBuilder schemaBuilder) {
        ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
        ociBuilder.setType("Contract");
        

        AttributeInfoBuilder attrContrAxUidBuilder = new AttributeInfoBuilder(CONTR_AX_UID);
        ociBuilder.addAttributeInfo(attrContrAxUidBuilder.build());

        AttributeInfoBuilder attrContrOrgUidBuilder = new AttributeInfoBuilder(CONTR_ORG_UID);
        ociBuilder.addAttributeInfo(attrContrOrgUidBuilder.build());

        AttributeInfoBuilder attrContrAxEmpUidBuilder = new AttributeInfoBuilder(CONTR_AX_EMP_UID);
        ociBuilder.addAttributeInfo(attrContrAxEmpUidBuilder.build());

        AttributeInfoBuilder attrContrProvUidBuilder = new AttributeInfoBuilder(CONTR_PROV_UID);
        ociBuilder.addAttributeInfo(attrContrProvUidBuilder.build());

        AttributeInfoBuilder attrContrFromBuilder = new AttributeInfoBuilder(CONTR_FROM);
        attrContrFromBuilder.setType(ZonedDateTime.class);
        ociBuilder.addAttributeInfo(attrContrFromBuilder.build());

        AttributeInfoBuilder attrContrToBuilder = new AttributeInfoBuilder(CONTR_TO);
        attrContrToBuilder.setType(ZonedDateTime.class);
        ociBuilder.addAttributeInfo(attrContrToBuilder.build());

        AttributeInfoBuilder attrContrUidBuilder = new AttributeInfoBuilder(CONTR_UID);
        ociBuilder.addAttributeInfo(attrContrUidBuilder.build());

        AttributeInfoBuilder attrContrNumBuilder = new AttributeInfoBuilder(CONTR_NUM);
        ociBuilder.addAttributeInfo(attrContrNumBuilder.build());

        AttributeInfoBuilder attrContrDateBuilder = new AttributeInfoBuilder(CONTR_DATE);
        attrContrDateBuilder.setType(ZonedDateTime.class);
        ociBuilder.addAttributeInfo(attrContrDateBuilder.build());

        AttributeInfoBuilder attrContr1cUidBuilder = new AttributeInfoBuilder(CONTR_1C_UID);
        ociBuilder.addAttributeInfo(attrContr1cUidBuilder.build());

        AttributeInfoBuilder attrContr1cDataBuilder = new AttributeInfoBuilder(CONTR_1CDATA);
        attrContr1cDataBuilder.setMultiValued(true);
        ociBuilder.addAttributeInfo(attrContr1cDataBuilder.build());

        schemaBuilder.defineObjectClass(ociBuilder.build());
    }


    /**
     * Создает транслятор фильтра, который преобразует указанный {@link rxptFilter} в один или несколько собственных запросов.
     * Каждый из этих собственных запросов будет впоследствии передан в <code>executeQuery()</code>.
     *
     * @param objectClass Класс объекта для поиска. Никогда не должен быть null
     *
     * @param options дополнительные параметры, влияющие на способ выполнения этой операции.
     *
     * Если вызывающая сторона принимает значение NULL, платформа преобразует это в пустой набор параметров, поэтому SPI не
     * нужно беспокоиться о том, что это когда-либо будет NULL.
     *
     * @return Фильтр-переводчик. Это не должно быть <code>null</code>.
     * Возвращаемое значение <code>null</code> заставит API (<code>SearchApiOp</code>) выдать {@link NullPointerException}.
     */

    @Override
    public FilterTranslator<rxptFilter> createFilterTranslator(ObjectClass objectClass, OperationOptions options) {
        return new rxptFilterTranslator();
    }

    /**
     * ConnectorFacade вызывает этот метод один раз для каждого собственного запроса, который
     * {@link #createFilterTranslator (ObjectClass, OperationOptions)} создает в ответ на <code>Filter</code>,
     * переданный в {@link SearchApiOp #search}.
     * <Р>
     * Если <code>FilterTranslator</code> создает более одного собственного запроса, то ConnectorFacade автоматически
     * объединит результаты каждого запроса и удалит любые дубликаты. Обратите внимание, что это подразумевает
     * структуру данных в памяти, которая содержит набор значений Uid, поэтому использование памяти в случае нескольких
     * запросов будет равно O(N), где N - количество результатов. Поэтому важно, чтобы FilterTranslator для каждого
     * коннектора реализовывал OR, если это возможно.
     *
     * @param objectClass  Класс объекта для поиска. Никогда не должен быть NULL
     * @param filter        Собственный запрос для запуска. Значение NULL означает «возвращать каждый экземпляр заданного класса объекта».
     * @param handler      Результаты должны быть возвращены этому обработчику
     * @param options      Дополнительные параметры, влияющие на способ выполнения этой операции.
     */

    @Override
    public void executeQuery(ObjectClass objectClass, rxptFilter filter, ResultsHandler handler, OperationOptions options) {
        LOG.info("\n\n\nObjectClass: {0}\n\n\n", objectClass);
        if (objectClass.is(ObjectClass.ACCOUNT_NAME) || objectClass.is("SecondAccount") || objectClass.is("ThirdAccount") || objectClass.is("FourthAccount")) { //Запускаем разные обработчики в зависимости от класса объекта, в данном случае класс объекта - аккаунт
            try {
                this.handleEmployees(handler, filter); //Вызываем обработчик класса Employee (аккаунт)
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (objectClass.is("Organization")) {
            try {
                this.handleOrganizations(handler);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (objectClass.is("Provider")) {
            try {
                this.handleProviders(handler);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (objectClass.is("Contract") || objectClass.is("SecondContract") || objectClass.is("ThirdContract") || objectClass.is("FourthContract") || objectClass.is("FifthContract") || objectClass.is("SixthContract") || objectClass.is("SeventhContract") || objectClass.is("EighthContract") || objectClass.is("NinthContract") || objectClass.is("TenthContract")) {
            try {
                this.handleContracts(handler, filter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Здесь мы предварительно обрабатываем класс Employee, получаем массив со всеми сотрудниками и в цикле обрабатываем каждого
     * @param handler       Обработчик результата Midpoint
     * @throws IOException
     */
    private void handleEmployees(ResultsHandler handler, rxptFilter filter) throws IOException {
        JSONArray employees;

        if (filter != null && filter.byUid != null) {
            employees = this.CallRequest(EMPLOYEES + "?" + EMP_AX_UID + "=" + filter.byUid);
        } else if (filter != null && filter.byName != null) {
            employees = this.CallRequest(EMPLOYEES + "?" + EMP_AX_UID + "=" + filter.byUid);
        } else {employees = this.CallRequest(EMPLOYEES);} //Получаем массив из нужного каталога (EMPLOYEE)

        for (int i = 0; i < employees.length(); i++) { //запускаем цикл обработки
            JSONObject employee = employees.getJSONObject(i); //получаем JSON объект из элемента массива
//            String strFromZup = employee.getString("1сData");
            getConfiguration();

            ConnectorObject connectorObject = this.convertUserToConnectorObject(employee); //передаем JSON объект в обработчик для конвертации его в объект коннектора
            boolean finish = !handler.handle(connectorObject);
            if (finish) {
                return;
            }
        }
        LOG.ok("\n\n\nКоличество тыщтынбеков: {0}\n\n\n", employees.length());
    }

    private void handleOrganizations(ResultsHandler handler) throws IOException {
        JSONArray organizations = this.CallRequest(ORGANIZATIONS);

        for (int i = 0; i < organizations.length(); i++) {
            JSONObject organization = organizations.getJSONObject(i);
            getConfiguration();

            ConnectorObject connectorObject = this.convertOrganizationToConnectorObject(organization);
            boolean finish = !handler.handle(connectorObject);
            if (finish) {
                return;
            }
        }
        LOG.ok("\n\n\nКоличество организаций: {0}\n\n\n", organizations.length());
    }

    private void handleProviders(ResultsHandler handler) throws IOException {
        JSONArray Providers = this.CallRequest(PROVIDERS);

        for (int i = 0; i < Providers.length(); i++) {
            JSONObject provider = Providers.getJSONObject(i);
            getConfiguration();

            ConnectorObject connectorObject = this.convertProviderToConnectorObject(provider);
            boolean finish = !handler.handle(connectorObject);
            if (finish) {
                return;
            }
        }
        LOG.ok("\n\n\nКоличество провайдеров: {0}\n\n\n", Providers.length());
    }

    private void handleContracts(ResultsHandler handler, rxptFilter filter) throws IOException {
        JSONArray Contracts = null;

        if (filter != null && filter.byUid != null) {
        	Contracts = this.CallRequest(CONTRACTS + "?" + CONTR_AX_UID + "=" + filter.byUid);
        } else if (filter != null && filter.byName != null) {
        	Contracts = this.CallRequest(CONTRACTS + "?" + CONTR_AX_UID + "=" + filter.byUid);
        } else {Contracts = this.CallRequest(CONTRACTS);}

        for (int i = 0; i < Contracts.length(); i++) {
            JSONObject contract = Contracts.getJSONObject(i);
            getConfiguration();

            ConnectorObject connectorObject = this.convertContractToConnectorObject(contract);
            boolean finish = !handler.handle(connectorObject);
            if (finish) {
                return;
            }
        }
        LOG.ok("\n\n\nКоличество договоров: {0}\n\n\n", Contracts.length());
    }

    /**
     * Здесь мы запрашиваем JSONArray со всеми объектами ресурса согласно классу объекта в запросе.
     * Из-за особенностей аксапты вместо штатного REST функционала пришлось использовать механизмы
     * HttpURLConnection и InputStream с BufferedReader
     */
    private JSONArray CallRequest(String catalog) throws IOException {
    	
    	if (configuration.getTrustAllCertificates() == true) {
    		
    		TrustManager[] trustAllCerts = new TrustManager[] {
    				new X509TrustManager() {
						
						@Override
						public X509Certificate[] getAcceptedIssuers() {
							// TODO Auto-generated method stub
							return null;
						}
						
						@Override
						public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
							// TODO Auto-generated method stub
							
						}
    				}
    	};
			
					try {
						SSLContext sc = SSLContext.getInstance("SSL");
						sc.init(null, trustAllCerts, new java.security.SecureRandom());
						HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
					} catch (Exception e) {
						// TODO: handle exception
					}
    		}
			
		

        URL url = new URL(configuration.getServiceProtocol() + configuration.getServiceAddress() + catalog);
    	LOG.ok("\n\n\nУРЛо: {0}\n\n\n", url);
        HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
        conn.setRequestProperty("Authorization", th.getTokenType() + " " + th.getTokenValue());
        conn.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String output;
        StringBuffer response = new StringBuffer();
        while ((output = in.readLine()) != null) { response.append(output); }
        in.close();
        return new JSONArray(response.toString());
    }

    /**
     * Конвертируем JSONObject в объект коннектора
     * @param employee      Передаваемый в обработчик JSON объект
     * @return
     */
    private ConnectorObject convertUserToConnectorObject(JSONObject employee) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        builder.setUid(new Uid(employee.getString(EMP_AX_UID)));
        builder.setName(employee.getString(EMP_AX_UID));

        this.getIfExist(employee, EMP_AX_UID, builder);
//        this.getIfExist(employee, EMP_BIRTH_DATE, builder);
		ZonedDateTime dateValue = LocalDateTime.parse(employee.getString(EMP_BIRTH_DATE) + "T00:00:00", formatter).atZone(timeZone);
		this.addAttr(builder, EMP_BIRTH_DATE, dateValue);
        
        
        this.getIfExist(employee, EMP_FULL_NAME, builder);
        this.getIfExist(employee, EMP_FIRST_NAME, builder);
        this.getIfExist(employee, EMP_MIDDLE_NAME, builder);
        this.getIfExist(employee, EMP_LAST_NAME, builder);
        this.getIfExist(employee, EMP_INN, builder);
        this.getIfExist(employee, EMP_SNILS, builder);
        this.getIfExist(employee, EMP_MUNID, builder);
        this.getIfExist(employee, EMP_1CDATA, builder);

        new HashMap<>();
        LOG.ok("Builder.build: {0}", builder.build());
        return builder.build();
    }


    private ConnectorObject convertOrganizationToConnectorObject(JSONObject organization) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        builder.setUid(new Uid(organization.getString(ORG_UID)));
        builder.setName(organization.getString(ORG_UID));

        this.getIfExist(organization, ORG_NAME, builder);
        this.getIfExist(organization, ORG_UID, builder);

        new HashMap<>();
        LOG.ok("Builder.build: {0}", builder.build());
        return builder.build();
    }


    private ConnectorObject convertProviderToConnectorObject(JSONObject provider) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        builder.setUid(new Uid(provider.getString(PROV_UID)));
        builder.setName(provider.getString(PROV_UID));

        this.getIfExist(provider, PROV_NAME, builder);
        this.getIfExist(provider, PROV_UID, builder);

        new HashMap<>();
        LOG.ok("Builder.build: {0}", builder.build());
        return builder.build();
    }

    private ConnectorObject convertContractToConnectorObject(JSONObject contract) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        builder.setUid(new Uid(contract.getString(CONTR_AX_UID)));
        builder.setName(contract.getString(CONTR_AX_UID));

        this.getIfExist(contract, CONTR_AX_UID, builder);
        this.getIfExist(contract, CONTR_ORG_UID, builder);
        this.getIfExist(contract, CONTR_AX_EMP_UID, builder);
        this.getIfExist(contract, CONTR_PROV_UID, builder);
//        this.getIfExist(contract, CONTR_FROM, builder);
		ZonedDateTime dateValue = LocalDateTime.parse(contract.getString(CONTR_FROM) + "T00:00:00", formatter).atZone(timeZone);
		this.addAttr(builder, CONTR_FROM, dateValue);
        
        
//        this.getIfExist(contract, CONTR_TO, builder);
		ZonedDateTime dateValue1 = LocalDateTime.parse(contract.getString(CONTR_TO) + "T00:00:00", formatter).atZone(timeZone);
		this.addAttr(builder, CONTR_TO, dateValue1);
        
        
        this.getIfExist(contract, CONTR_UID, builder);
        this.getIfExist(contract, CONTR_NUM, builder);
//        this.getIfExist(contract, CONTR_DATE, builder);
		ZonedDateTime dateValue2 = LocalDateTime.parse(contract.getString(CONTR_DATE) + "T00:00:00", formatter).atZone(timeZone);
		this.addAttr(builder, CONTR_DATE, dateValue2);
        
        
        this.getIfExist(contract, CONTR_1C_UID, builder);
        this.get1cDataIfExist(contract, CONTR_1CDATA, builder);

        new HashMap<>();
        LOG.ok("Builder.build: {0}", builder.build());
        return builder.build();
    }

    /**
     * Заполняем поля объекта коннектора, если они заполнены в исходном объекте
     * @param object        Исходный объект
     * @param attrName      Имя атрибута
     * @param builder       Построитель объекта коннектора
     */
    private void getIfExist(JSONObject object, String attrName, ConnectorObjectBuilder builder) {
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

    private void get1cDataIfExist(JSONObject object, String attrName, ConnectorObjectBuilder builder) {
        if (object.has(attrName) && object.get(attrName) != null && !JSONObject.NULL.equals(object.get(attrName))) {

            String dataString = object.get(attrName).toString().replaceAll("\\[|\\]|\"", "");
            List<String> onesData = Arrays.asList(dataString.split("\\s*,\\s*"));
            builder.addAttribute(attrName, onesData.toArray());
        }
    }

    /**
     * Проверяем {@link Configuration} коннектора.
     *
     * @throws RuntimeException, если конфигурация недействительна или тест не пройден.
     * 						  	Для имплементаций рекомендуется создавать наиболее конкретные
     * 						  	доступные исключения. Если конкретное исключение недоступно,
     * 						  	реализации могут генерировать {@link ConnectorException}.
     */

    @Override
    public void test() {

    }

    /**
     * Получаем токен авторизации аксапты на текущую сессию коннектора
     * @param configuration
     * @return
     * @throws IOException
     */


    /**
     * Здесь мы обрабатываем распространенные ответы сервера (в текущей конфигурации не используется из-за особенностей аксапты)
     * @param response
     */
    @SuppressWarnings("unused")
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
                } else if (err.has(EMP_FULL_NAME)) {
                    this.closeResponse(response);
                    throw new AlreadyExistsException(err.getString(EMP_FULL_NAME));
                } else if (err.has(EMP_1C_UID)) {
                    this.closeResponse(response);
                    throw new AlreadyExistsException(err.getString(EMP_1C_UID));
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

    /**
     * Update the object specified by the {@link ObjectClass} and {@link Uid},
     * replacing the current values of each attribute with the values provided.
     * <p>
     * For each input attribute, replace all of the current values of that
     * attribute in the target object with the values of that attribute.
     * <p>
     * If the target object does not currently contain an attribute that the
     * input set contains, then add this attribute (along with the provided
     * values) to the target object.
     * <p>
     * If the value of an attribute in the input set is {@code null}, then do
     * one of the following, depending on which is most appropriate for the
     * target:
     * <ul>
     * <li>If possible, <em>remove</em> that attribute from the target object
     * entirely.</li>
     * <li>Otherwise, <em>replace all of the current values</em> of that
     * attribute in the target object with a single value of {@code null}.</li>
     * </ul>
     *
     * @param objectClass       the type of object to modify. Will never be null.
     * @param uid               the uid of the object to modify. Will never be null.
     * @param replaceAttributes set of new {@link Attribute}. the values in this set represent
     *                          the new, merged values to be applied to the object. This set
     *                          may also include
     *                          {@link OperationalAttributes
     *                          operational attributes}. Will never be null.
     * @param options           additional options that impact the way this operation is run.
     *                          Will never be null.
     * @return the {@link Uid} of the updated object in case the update changes
     * the formation of the unique identifier.
     * @throws UnknownUidException if the {@link Uid} does not exist on the resource.
     */
    @Override
    public Uid update(ObjectClass objectClass, Uid uid, Set<Attribute> replaceAttributes, OperationOptions options) {
        if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            try {
                return this.createOrUpdateUser(uid, replaceAttributes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return uid;
    }

    private Uid createOrUpdateUser(Uid uid, Set<Attribute> attributes) throws IOException {
        if (attributes != null && !attributes.isEmpty()) {
            @SuppressWarnings("unused")
			boolean create = uid == null;
//            JSONObject jObject = new JSONObject();
//
//            String name = this.getStringAttr(attributes, Name.NAME);
//            if (create && StringUtil.isBlank(name)) {
//                throw new InvalidAttributeValueException("Отсутствует необходимый атрибут " + Name.NAME);
//            } else {
//                if (name != null) {
//                    jObject.put("name", name);
//                }
//            }
//
//            this.putFieldIfExist(attributes, EMP_AX_UID, jObject);
//            this.putFieldIfExist(attributes, EMP_BIRTH_DATE, jObject);
//            this.putFieldIfExist(attributes, EMP_FULL_NAME, jObject);
//            this.putFieldIfExist(attributes, EMP_FIRST_NAME, jObject);
//            this.putFieldIfExist(attributes, EMP_MIDDLE_NAME, jObject);
//            this.putFieldIfExist(attributes, EMP_LAST_NAME, jObject);
//            this.putFieldIfExist(attributes, EMP_INN, jObject);
//            this.putFieldIfExist(attributes, EMP_SNILS, jObject);
//            this.putFieldIfExist(attributes, EMP_MUNID, jObject);
//
            //Оченно временное решение, только ради BIOSMART
            URL url = new URL(configuration.getServiceProtocol() + configuration.getServiceAddress() + "/api/ProviderEmployee/setMunID");

            Map<String,Object> params = new LinkedHashMap<>();
            params.put(EMP_AX_UID, uid.getUidValue());
            params.put(EMP_MUNID,  this.getStringAttr(attributes, EMP_MUNID));

            LOG.ok("Params: {0}", params);

            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String,Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            LOG.error("\n\n\n\n\n\n\n\n\nPostdata: {0}\n\n\n\n\n\n\n\n\n", postData);

            HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
            conn.setRequestProperty("Authorization", th.getTokenType() + " " + th.getTokenValue());
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);

            LOG.ok("\n\n\nRequest: {0}", conn.getContent());



        }

        return uid;
    }

}