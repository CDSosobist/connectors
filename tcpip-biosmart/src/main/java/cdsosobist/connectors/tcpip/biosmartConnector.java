/*
 * Copyright (c) 2019 CDSOsobist
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

package cdsosobist.connectors.tcpip;


import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.PoolableConnector;
import org.identityconnectors.framework.spi.operations.*;

import javax.xml.bind.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.nio.CharBuffer;
import java.util.*;

import static cdsosobist.connectors.tcpip.AttributeHandler.*;

@ConnectorClass(displayNameKey = "biosmart.connector.display", configurationClass = biosmartConfiguration.class)

public class biosmartConnector implements PoolableConnector, TestOp, CreateOp, UpdateOp, SchemaOp, DeleteOp, SearchOp<biosmartFilter>, Connector {

    private static final Log LOG = Log.getLog(biosmartConnector.class);

    private biosmartConfiguration configuration = new biosmartConfiguration();

    private CharBuffer tmpBuffer = CharBuffer.allocate(40960000);

    public biosmartConnector() {
    }

    @Override
    public biosmartConfiguration getConfiguration() {return configuration;}

    @Override
    public void test() {
        if (getConfiguration().isSkipTestConnection()) {
            LOG.ok("Тестирование отключено вами в конфигурации коннектора");
        } else {
            LOG.ok("Тестируем, запрашиваем список зон доступа");
            try (Socket socket = new Socket(this.configuration.getHostname(), this.configuration.getPort())) {

                socket.setSoTimeout(1000);

                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
                writer.println("<KRECEPT>" + "<REQUEST type=\"2\">" + "<RECORD operation=\"0\"/>" + "</REQUEST>" + "</KRECEPT>" + "\0");
                InputStream input = socket.getInputStream();


                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                String line;


                while ((line = reader.readLine()) != null) {
                    LOG.ok(line);
                    if (line.endsWith("</KRECEPT>")) {
                        break;
                    }
                }


            } catch (UnknownHostException e) {
                System.out.println("Сервер не найден: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("Ошибка при тестировании коннектора: " + e.getMessage());
            }
        }
    }

    @Override
    public void init(Configuration configuration) {
        this.configuration = (biosmartConfiguration)configuration;
        LOG.ok("Инициация прошла успешно!");
    }

    @Override
    public Schema schema() {
        SchemaBuilder schemaBuilder = new SchemaBuilder(biosmartConnector.class);

        this.buildAccountObjectClass(schemaBuilder);
        this.buildAccessGroupObjectClass(schemaBuilder);
        this.buildOrganizationObjectClass(schemaBuilder);
        this.builPositionObjectClass(schemaBuilder);
//
        return schemaBuilder.build();

    }

    private void buildAccountObjectClass(SchemaBuilder schemaBuilder) {
        ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();

        AttributeInfoBuilder attrAccUidBuilder = new AttributeInfoBuilder(ACC_UID);
        ociBuilder.addAttributeInfo(attrAccUidBuilder.build());

        AttributeInfoBuilder attrAccFnameBuilder = new AttributeInfoBuilder(ACC_FNAME);
        ociBuilder.addAttributeInfo(attrAccFnameBuilder.build());

        AttributeInfoBuilder attrAccLnameBuilder = new AttributeInfoBuilder(ACC_LNAME);
        ociBuilder.addAttributeInfo(attrAccLnameBuilder.build());

        AttributeInfoBuilder attrAccMnameBuilder = new AttributeInfoBuilder(ACC_MNAME);
        ociBuilder.addAttributeInfo(attrAccMnameBuilder.build());

        AttributeInfoBuilder attrAccCnumBuilder = new AttributeInfoBuilder(ACC_CNUM);
        ociBuilder.addAttributeInfo(attrAccCnumBuilder.build());

        AttributeInfoBuilder attrAccOrgIdBuilder = new AttributeInfoBuilder(ACC_ORG_ID);
        ociBuilder.addAttributeInfo(attrAccOrgIdBuilder.build());

        AttributeInfoBuilder attrAccDepIdBuilder = new AttributeInfoBuilder(ACC_DEP_ID);
        ociBuilder.addAttributeInfo(attrAccDepIdBuilder.build());

        AttributeInfoBuilder attrAccJobIdBuilder = new AttributeInfoBuilder(ACC_JOB_ID);
        ociBuilder.addAttributeInfo(attrAccJobIdBuilder.build());

        AttributeInfoBuilder attrAccBegDateBuilder = new AttributeInfoBuilder(ACC_BEG_DATE);
        ociBuilder.addAttributeInfo(attrAccBegDateBuilder.build());

        AttributeInfoBuilder attrAccEndDateBuilder = new AttributeInfoBuilder(ACC_END_DATE);
        ociBuilder.addAttributeInfo(attrAccEndDateBuilder.build());

        AttributeInfoBuilder attrAccBirthDateBuilder = new AttributeInfoBuilder(ACC_BIRTH_DATE);
        ociBuilder.addAttributeInfo(attrAccBirthDateBuilder.build());

        AttributeInfoBuilder attrAccGenderBuilder = new AttributeInfoBuilder(ACC_GENDER);
        ociBuilder.addAttributeInfo(attrAccGenderBuilder.build());

        AttributeInfoBuilder attrAccPhotoBuilder = new AttributeInfoBuilder(ACC_PHOTO);
        attrAccPhotoBuilder.setType(byte.class);
        ociBuilder.addAttributeInfo(attrAccPhotoBuilder.build());

        AttributeInfoBuilder attrAccExtIdBuilder = new AttributeInfoBuilder(EXT_ID);
        ociBuilder.addAttributeInfo(attrAccExtIdBuilder.build());
        
        AttributeInfoBuilder attrAccPolicyBuilder = new AttributeInfoBuilder(POLICY);
        ociBuilder.addAttributeInfo(attrAccPolicyBuilder.build());

        ociBuilder.addAttributeInfo(OperationalAttributeInfos.ENABLE);

        schemaBuilder.defineObjectClass(ociBuilder.build());
    }

    private void buildAccessGroupObjectClass(SchemaBuilder schemaBuilder) {
        ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
        ociBuilder.setType("AccessGroup");

        AttributeInfoBuilder attrAgUidBuilder = new AttributeInfoBuilder(AG_UID);
        ociBuilder.addAttributeInfo(attrAgUidBuilder.build());

        AttributeInfoBuilder attrAgNameBuilder = new AttributeInfoBuilder(AG_NAME);
        ociBuilder.addAttributeInfo(attrAgNameBuilder.build());

        AttributeInfoBuilder attrAgExtIdBuilder = new AttributeInfoBuilder(EXT_ID);
        ociBuilder.addAttributeInfo(attrAgExtIdBuilder.build());

        schemaBuilder.defineObjectClass(ociBuilder.build());
    }

    private void buildOrganizationObjectClass(SchemaBuilder schemaBuilder) {
        ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
        ociBuilder.setType("Organization");

        AttributeInfoBuilder attrOrgUidBuilder = new AttributeInfoBuilder(ORG_UID);
        ociBuilder.addAttributeInfo(attrOrgUidBuilder.build());

        AttributeInfoBuilder attrOrgNameBuilder = new AttributeInfoBuilder(ORG_NAME);
        ociBuilder.addAttributeInfo(attrOrgNameBuilder.build());

        AttributeInfoBuilder attrOrgExtIdBuilder = new AttributeInfoBuilder(EXT_ID);
        ociBuilder.addAttributeInfo(attrOrgExtIdBuilder.build());

        schemaBuilder.defineObjectClass(ociBuilder.build());
    }

    private void builPositionObjectClass(SchemaBuilder schemaBuilder) {
        ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
        ociBuilder.setType("Position");

        AttributeInfoBuilder attrPosUidBuilder = new AttributeInfoBuilder(POS_UID);
        ociBuilder.addAttributeInfo(attrPosUidBuilder.build());

        AttributeInfoBuilder attrPosNameBuilder = new AttributeInfoBuilder(POS_NAME);
        ociBuilder.addAttributeInfo(attrPosNameBuilder.build());

        AttributeInfoBuilder attrPosExtIdBuilder = new AttributeInfoBuilder(EXT_ID);
        ociBuilder.addAttributeInfo(attrPosExtIdBuilder.build());

        schemaBuilder.defineObjectClass(ociBuilder.build());
    }

    private Krecept handleKrecept(String filter, Integer requestType, Integer recordOperation, RecordFields fields) {

        Request request = new Request();

            if (requestType == 1) {

            } else if (requestType == 2) {

            } else if (requestType == 3) {

                Records accessGroupRecords = new Records();
                if (filter != null) {
                    accessGroupRecords.setId(filter);
                }
                accessGroupRecords.setOperation(recordOperation);

                request.setType(requestType);
                request.setRecords(new ArrayList<>());
                request.getRecords().add(accessGroupRecords);


            } else if (requestType == 4) {

            } else if (requestType == 5) {

                Records accountRecord = new Records();
                if (filter != null) {
                    accountRecord.setId(filter);
                }
                accountRecord.setOperation(recordOperation);
                accountRecord.setRecordFields(new ArrayList<>());

                request.setType(requestType);
                request.setRecords(new ArrayList<>());
                request.getRecords().add(accountRecord);

            } else if (requestType == 6) {

                Records organizationRecords = new Records();
                if (filter != null) {
                    organizationRecords.setId(filter);
                }
                organizationRecords.setOperation(recordOperation);

                request.setType(requestType);
                request.setRecords(new ArrayList<>(recordOperation));
                request.getRecords().add(organizationRecords);

            } else if (requestType == 7) {

                Records positionRecords = new Records();
                if (filter != null) {
                    positionRecords.setId(filter);
                }
                positionRecords.setOperation(recordOperation);

                request.setType(requestType);
                request.setRecords(new ArrayList<>(recordOperation));
                request.getRecords().add(positionRecords);


            } else if (requestType == 12) {
            	
            	Records accPolicyRecords = new Records();
            	if (filter != null) {
					accPolicyRecords.setId(filter);
				}
            	accPolicyRecords.setOperation(recordOperation);
            	
            	request.setType(requestType);
            	request.setRecords(new ArrayList<>(recordOperation));
            	request.getRecords().add(accPolicyRecords);

            }
            
        Krecept krecept = new Krecept();
        krecept.setRequest(request);

        return krecept;
    }

    @Override
    public void dispose() {
        configuration = null;
    }

    @Override
    public void checkAlive() {

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
    public FilterTranslator createFilterTranslator(ObjectClass objectClass, OperationOptions operationOptions) {
        return new biosmartFilterTranslator();
    }

    @Override
    public void executeQuery(ObjectClass objectClass, biosmartFilter biosmartFilter, ResultsHandler resultsHandler, OperationOptions operationOptions) {
        LOG.info("\n\n\nObjectClass: {0}\nbiosmartFilter: {1}\nResultHandler: {2}\nOperationOptions: {3}\n\n\n", objectClass, biosmartFilter,resultsHandler, operationOptions);
        JAXBContext context = null;

        if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            if (biosmartFilter != null && biosmartFilter.byUid != null) {
                try { context = JAXBContext.newInstance(Krecept.class); } catch (JAXBException e) { e.printStackTrace(); }
                bsGet(Objects.requireNonNull(context), biosmartFilter.byUid, 5,0);
                try { this.handleAccounts(context, resultsHandler); } catch (Exception e) { e.printStackTrace(); }
            } else
                if (biosmartFilter != null && biosmartFilter.byBirthDate != null) {
                    try { context = JAXBContext.newInstance(Krecept.class); } catch (JAXBException e) { e.printStackTrace(); }
                    bsGet(Objects.requireNonNull(context), biosmartFilter.byBirthDate, 5,0);
                    try { this.handleAccounts(context, resultsHandler); } catch (Exception e) { e.printStackTrace(); }
                } else
                    if (biosmartFilter != null && biosmartFilter.byClockNum != null) {
                        try { context = JAXBContext.newInstance(Krecept.class); } catch (JAXBException e) { e.printStackTrace(); }
                        bsGet(Objects.requireNonNull(context), biosmartFilter.byClockNum, 5,0);
                        try { this.handleAccounts(context, resultsHandler); } catch (Exception e) { e.printStackTrace(); }
                    } else
                        try { context = JAXBContext.newInstance(Krecept.class); } catch (JAXBException e) { e.printStackTrace(); }
                        bsGet(Objects.requireNonNull(context), null, 5,0);
                        try { this.handleAccounts(context, resultsHandler); } catch (Exception e) { e.printStackTrace(); }
        } else


            if (objectClass.is("AccessGroup")) {
                if (biosmartFilter != null && biosmartFilter.byUid != null) {
                    try { context = JAXBContext.newInstance(Krecept.class); } catch (JAXBException e) { e.printStackTrace(); }
                    bsGet(Objects.requireNonNull(context), biosmartFilter.byUid, 3,0);
                    try { this.handleAccessGroups(context, resultsHandler); } catch (Exception e) { e.printStackTrace(); }
                } else
                    if (biosmartFilter != null && biosmartFilter.byName != null) {
                        try { context = JAXBContext.newInstance(Krecept.class); } catch (JAXBException e) { e.printStackTrace(); }
                        bsGet(Objects.requireNonNull(context), biosmartFilter.byName, 3,0);
                        try { this.handleAccessGroups(context, resultsHandler); } catch (Exception e) { e.printStackTrace(); }
                    } else
                        try { context = JAXBContext.newInstance(Krecept.class); } catch (JAXBException e) { e.printStackTrace(); }
                        bsGet(Objects.requireNonNull(context), null,3,0);
                        try { this.handleAccessGroups(context, resultsHandler); } catch (Exception e) { e.printStackTrace(); }
            } else


                if (objectClass.is("Organization")) {
                    if (biosmartFilter != null && biosmartFilter.byUid != null) {
                        try { context = JAXBContext.newInstance(Krecept.class); } catch (JAXBException e) { e.printStackTrace(); }
                        bsGet(Objects.requireNonNull(context), biosmartFilter.byUid, 6,0);
                        try { this.handleOrganizations(context, resultsHandler); } catch (Exception e) { e.printStackTrace(); }
                    } else
                        if (biosmartFilter != null && biosmartFilter.byName != null) {
                            try { context = JAXBContext.newInstance(Krecept.class); } catch (JAXBException e) { e.printStackTrace(); }
                            bsGet(Objects.requireNonNull(context), biosmartFilter.byName, 6,0);
                            try { this.handleOrganizations(context, resultsHandler); } catch (Exception e) { e.printStackTrace(); }
                        } else
                            try { context = JAXBContext.newInstance(Krecept.class); } catch (JAXBException e) { e.printStackTrace(); }
                            bsGet(Objects.requireNonNull(context), null,6,0);
                            try { this.handleOrganizations(context, resultsHandler); } catch (Exception e) { e.printStackTrace(); }
                } else

                    if (objectClass.is("Position")) {
                        if (biosmartFilter != null && biosmartFilter.byUid != null) {
                            try { context = JAXBContext.newInstance(Krecept.class); } catch (JAXBException e) { e.printStackTrace(); }
                            bsGet(Objects.requireNonNull(context), biosmartFilter.byUid, 7,0);
                            try { this.handlePositions(context, resultsHandler); } catch (Exception e) { e.printStackTrace(); }
                        } else
                            if (biosmartFilter != null && biosmartFilter.byName != null) {
                                try { context = JAXBContext.newInstance(Krecept.class); } catch (JAXBException e) { e.printStackTrace(); }
                                bsGet(Objects.requireNonNull(context), biosmartFilter.byName, 7,0);
                                try { this.handlePositions(context, resultsHandler); } catch (Exception e) { e.printStackTrace(); }
                            } else
                                try { context = JAXBContext.newInstance(Krecept.class); } catch (JAXBException e) { e.printStackTrace(); }
                                bsGet(Objects.requireNonNull(context), null,7,0);
                                try { this.handlePositions(context, resultsHandler); } catch (Exception e) { e.printStackTrace(); }
                    }
    }

    private void handleAccounts(JAXBContext context, ResultsHandler handler) throws JAXBException {

        Unmarshaller unmarshaller = context.createUnmarshaller();
        StringReader readerFromTmpBuffer = new StringReader(tmpBuffer.toString());
        Krecept krcFromInput = (Krecept) unmarshaller.unmarshal(readerFromTmpBuffer);
        List<Records> records = krcFromInput.getAnswer().getRecords();


        for (Records record : records) {

            Iterator<RecordFields> accFieldsIterator = record.getRecordFields().listIterator();

            ConnectorObject connectorObject = this.convertAccountToConnectorObject(record, accFieldsIterator, context);

            boolean finish = !handler.handle(connectorObject);
            if (finish) {
                return;
            }

        }
        ((Buffer)tmpBuffer).clear();
        getConfiguration();
    }

    private String handleNewUid(JAXBContext context) throws JAXBException {
        String newUid = null;
        Unmarshaller unmarshaller = context.createUnmarshaller();
        StringReader readerFromTmpBuffer = new StringReader(tmpBuffer.toString());
        Krecept krcFromInput = (Krecept) unmarshaller.unmarshal(readerFromTmpBuffer);
        List<Records> records = krcFromInput.getAnswer().getRecords();

        for (Records record : records) {
            newUid = record.getId();
        }

        return newUid;

    }



    private void handleAccessGroups(JAXBContext context, ResultsHandler handler) throws JAXBException {

        Unmarshaller unmarshaller = context.createUnmarshaller();
        StringReader readerFromTmpBuffer = new StringReader(tmpBuffer.toString());
        Krecept krcFromInput = (Krecept) unmarshaller.unmarshal(readerFromTmpBuffer);
        List<Records> accessGroupRecords = krcFromInput.getAnswer().getRecords();

        for (Records record : accessGroupRecords) {
            ConnectorObject connectorObject = this.convertAccessGroupToConnectorObject(record);
            handler.handle(connectorObject);

            boolean finish = !handler.handle(connectorObject);
            if (finish) {
                return;
            }
        }
        ((Buffer)tmpBuffer).clear();
        getConfiguration();

    }


    private void handleOrganizations(JAXBContext context, ResultsHandler handler) throws JAXBException {

        Unmarshaller unmarshaller = context.createUnmarshaller();
        StringReader readerFromTmpBuffer = new StringReader(tmpBuffer.toString());
        Krecept krcFromInput = (Krecept) unmarshaller.unmarshal(readerFromTmpBuffer);
        List<Records> organizationRecords = krcFromInput.getAnswer().getRecords();

        for (Records record : organizationRecords) {
            ConnectorObject connectorObject = this.convertOrganizationToConnectorObject(record);
            handler.handle(connectorObject);

            boolean finish = !handler.handle(connectorObject);
            if (finish) {
                return;
            }
        }
        ((Buffer)tmpBuffer).clear();
        getConfiguration();
    }


    private void handlePositions(JAXBContext context, ResultsHandler handler) throws JAXBException {

        Unmarshaller unmarshaller = context.createUnmarshaller();
        StringReader readerFromTmpBuffer = new StringReader(tmpBuffer.toString());
        Krecept krcFromInput = (Krecept) unmarshaller.unmarshal(readerFromTmpBuffer);
        List<Records> positionRecords = krcFromInput.getAnswer().getRecords();

        for (Records record : positionRecords) {
            ConnectorObject connectorObject = this.convertPositionToConnectorObject(record);
            handler.handle(connectorObject);

            boolean finish = !handler.handle(connectorObject);
            if (finish) {
                return;
            }
        }
        ((Buffer)tmpBuffer).clear();
        getConfiguration();
    }

    private ConnectorObject convertAccountToConnectorObject(Records record, Iterator<RecordFields> accFieldsIterator, JAXBContext context) throws JAXBException {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        builder.setUid(record.getId());
        builder.setName(record.getId());
        
        String accId = record.getId();
        String policy = accPolicyGet(context, accId);


        while (accFieldsIterator.hasNext()) {

            RecordFields currField = accFieldsIterator.next();
            if (currField.getXmlFieldName().contentEquals(ACC_FNAME)) {
                builder.addAttribute(ACC_FNAME, currField.getXmlFieldValue());
            } else if (currField.getXmlFieldName().contentEquals(ACC_LNAME)) {
                builder.addAttribute(ACC_LNAME, currField.getXmlFieldValue());
            } else if (currField.getXmlFieldName().contentEquals(ACC_MNAME)) {
                builder.addAttribute(ACC_MNAME, currField.getXmlFieldValue());
            } else if (currField.getXmlFieldName().contentEquals(ACC_CNUM)) {
                builder.addAttribute(ACC_CNUM, currField.getXmlFieldValue());
            } else if (currField.getXmlFieldName().contentEquals(ACC_ORG_ID)) {
                builder.addAttribute(ACC_ORG_ID, currField.getXmlFieldValue());
            } else if (currField.getXmlFieldName().contentEquals(ACC_DEP_ID)) {
                builder.addAttribute(ACC_DEP_ID, currField.getXmlFieldValue());
            } else if (currField.getXmlFieldName().contentEquals(ACC_JOB_ID)) {
                builder.addAttribute(ACC_JOB_ID, currField.getXmlFieldValue());
            } else if (currField.getXmlFieldName().contentEquals(ACC_BEG_DATE)) {
                builder.addAttribute(ACC_BEG_DATE, currField.getXmlFieldValue());
            } else if (currField.getXmlFieldName().contentEquals(ACC_END_DATE)) {
                builder.addAttribute(ACC_END_DATE, currField.getXmlFieldValue());
            } else if (currField.getXmlFieldName().contentEquals(ACC_BIRTH_DATE)) {
                builder.addAttribute(ACC_BIRTH_DATE, currField.getXmlFieldValue());
            } else if (currField.getXmlFieldName().contentEquals(ACC_GENDER)) {
                builder.addAttribute(ACC_GENDER, currField.getXmlFieldValue());
            } else if (currField.getXmlFieldName().contentEquals(ACC_PHOTO)) {
                builder.addAttribute(ACC_PHOTO, currField.getXmlFieldValue());
            } 
            
        }	builder.addAttribute(POLICY, policy);
        
        

        new HashMap<>();
//        LOG.ok("Builder.build: {0}", builder.build());
        return builder.build();
    }


    private ConnectorObject convertAccessGroupToConnectorObject(Records record) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        builder.setUid(record.getId());
        builder.setName(record.getId());


        builder.addAttribute(AG_UID, record.getId());
        builder.addAttribute(AG_NAME, record.getName());


        new HashMap<>();
        return builder.build();
    }


    private ConnectorObject convertOrganizationToConnectorObject(Records record) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        builder.setUid(record.getId());
        builder.setName(record.getId());



        builder.addAttribute(ORG_UID, record.getId());
        builder.addAttribute(ORG_NAME, record.getName());

        new HashMap<>();
        return builder.build();
    }


    private ConnectorObject convertPositionToConnectorObject(Records record) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        builder.setUid(record.getId());
        builder.setName(record.getId());



        builder.addAttribute(POS_UID, record.getId());
        builder.addAttribute(POS_NAME, record.getName());

        new HashMap<>();
        return builder.build();
    }


    private String accPolicyGet(JAXBContext context, String accId) throws JAXBException {
    	String reqString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KRECEPT><REQUEST type=\"12\"><RECORD id=\"" + accId + "\" operation=\"0\"></RECORD></REQUEST></KRECEPT>";
    	
    	try {
			Socket socket = new Socket(configuration.getHostname(), configuration.getPort());
			OutputStream output = socket.getOutputStream();
			PrintWriter writer = new PrintWriter(output, true);
			writer.println(reqString + "\0");
			DataInputStream input = new DataInputStream(socket.getInputStream());
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			
			String line;
			
			((Buffer)tmpBuffer).clear();
            while ((line = reader.readLine()) != null) {

                if (line.contains("</KRECEPT>")) {
                    tmpBuffer.put(line);
                    socket.close();
                    break;
                }
                tmpBuffer.put(line);
            } ((Buffer)tmpBuffer).flip();

        } catch (IOException ex) {
            ex.printStackTrace();
        }


        Unmarshaller unmarshaller = context.createUnmarshaller();
        StringReader readerFromTmpBuffer = new StringReader(tmpBuffer.toString());
        Krecept krcFromInput = (Krecept) unmarshaller.unmarshal(readerFromTmpBuffer);
        Records record = krcFromInput.getAnswer().getRecords().get(0);
        String policyValue = record.getRecordFields().get(0).getXmlFieldValue();
        
        ((Buffer)tmpBuffer).clear();
        
        return policyValue;


}
    
    
    private void accPolicyPut(String accId, String newPolicyValue) throws JAXBException {
    	String reqString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KRECEPT><REQUEST type=\"12\"><RECORD id=\"" + accId + "\" operation=\"2\"><FIELD name=\"policy\">" + newPolicyValue + "</FIELD></RECORD></REQUEST></KRECEPT>";

    	System.out.println(reqString);

    	try {
			Socket socket = new Socket(configuration.getHostname(), configuration.getPort());
			OutputStream output = socket.getOutputStream();
			PrintWriter writer = new PrintWriter(output, true);
			writer.println(reqString + "\0");
			DataInputStream input = new DataInputStream(socket.getInputStream());
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			
			String line;
			
			((Buffer)tmpBuffer).clear();
            while ((line = reader.readLine()) != null) {
            	
            	System.out.println(line);

                if (line.contains("</KRECEPT>")) {
                    tmpBuffer.put(line);
                    socket.close();
                    break;
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
}
    
    
    private void bsGet(JAXBContext context, String filter,  int requestType, int recordOperation) {

        Krecept krecept = handleKrecept(filter, requestType, recordOperation, null);
        try {
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setProperty("com.sun.xml.bind.xmlHeaders", "<?xml version=\"1.0\" encoding=\"UTF8\"?>");

            marshaller.marshal(krecept, System.out);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            marshaller.marshal(krecept, outputStream);
            String outputXml = (outputStream.toString());

            try {
                Socket socket = new Socket(configuration.getHostname(), configuration.getPort());
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
                writer.println(outputXml + "\0");
                DataInputStream input = new DataInputStream(socket.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

//                System.setOut(toBuffer);

                String line;

                ((Buffer)tmpBuffer).clear();

                while ((line = reader.readLine()) != null) {

                    if (line.contains("</KRECEPT>")) {
                        tmpBuffer.put(line);
//                        System.out.println(line);
                        socket.close();
                        break;
                    }
                    tmpBuffer.put(line);
//                    System.out.println(line);
                } ((Buffer)tmpBuffer).flip();
//                System.out.println("540, tmpBuffer: " + tmpBuffer);
//                System.out.println("\nBuffer: " +  tmpBuffer.toString() + "\n");

            } catch (IOException ex) {
                ex.printStackTrace();
            }

//            System.setOut(sonsol);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Uid create(ObjectClass objectClass, Set<Attribute> set, OperationOptions operationOptions) {
        System.out.println("Update object: " + objectClass + ", set: " + set + ", operationOptions: " + operationOptions);
        if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            try {
                Uid newUid = this.createOrUpdateUser(null, set, 1);
                LOG.ok("\n\n\nNewUid: {0}\n\n\n", newUid);
                return newUid;
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        } else if (objectClass.is("AccessGroup")){
            try {
                Uid newUid = this.createOrUpdateOtherObjClass(null, set, 3, 1);
                return newUid;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (objectClass.is("Organization")){
            try {
                Uid newUid = this.createOrUpdateOtherObjClass(null, set, 6, 1);
                return newUid;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }  else if (objectClass.is("Position")){
            try {
                Uid newUid = this.createOrUpdateOtherObjClass(null, set, 7, 1);
                return newUid;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private Uid createOrUpdateUser(Uid uid, Set<Attribute> attributes, int operation) throws JAXBException, NullPointerException {
        String strExtId = null;
        String attrPolicyValue = null;
        
        LOG.ok("createOrUpdateUser, Uid: {0}, attributes: {1}", uid, attributes);
        
        if (attributes == null || attributes.isEmpty()) { LOG.ok("request ignored, empty attributes"); return uid; }
        
        boolean create = uid == null;
        JAXBContext context = null;

        Records record = new Records();

        if (!create) { record.setId(uid.getUidValue()); }
        
        
        try { strExtId = AttributeUtil.find(EXT_ID, attributes).getValue().toString().replaceAll("^\\[|\\]$", ""); }
        catch (Exception e) { strExtId = null; }
        
        try { attrPolicyValue = AttributeUtil.find(POLICY, attributes).getValue().toString().replaceAll("^\\[|\\]$", "");}
        catch (Exception e) {}
        

        if (strExtId != null) {
            LOG.error("\n\n\nsetExtId: {0}\n\n\n", strExtId);
            record.setExtId(strExtId);
        }

        record.setOperation(operation);
        record.setRecordFields(new ArrayList<>());

        this.putFieldIfExist(attributes, record, ACC_FNAME);
        this.putFieldIfExist(attributes, record, ACC_LNAME);
        this.putFieldIfExist(attributes, record, ACC_MNAME);
        this.putFieldIfExist(attributes, record, ACC_BEG_DATE);
        this.putFieldIfExist(attributes, record, ACC_BIRTH_DATE);
        this.putFieldIfExist(attributes, record, ACC_CNUM);
        this.putFieldIfExist(attributes, record, ACC_DEP_ID);
        this.putFieldIfExist(attributes, record, ACC_END_DATE);
        this.putFieldIfExist(attributes, record, ACC_GENDER);
        this.putFieldIfExist(attributes, record, ACC_JOB_ID);
        this.putFieldIfExist(attributes, record, ACC_ORG_ID);
        this.putFieldIfExist(attributes, record, ACC_PHOTO);
        
        System.out.println(attrPolicyValue);
        System.out.println(attrPolicyValue == null);
        

        if (!create && (attrPolicyValue != null) && !(attrPolicyValue.contains("empty"))) {accPolicyPut(uid.getUidValue(), attrPolicyValue);}
        if (attrPolicyValue.contains("empty")) {accPolicyPut(uid.getUidValue(), null);}

        Request request = new Request();
        request.setType(5);
        request.setRecords(new ArrayList<>());
        request.getRecords().add(record);

        Krecept krecept = new Krecept();
        krecept.setRequest(request);

        try { context = JAXBContext.newInstance(Krecept.class); }
        catch (JAXBException e) { e.printStackTrace(); }

        bsPut(Objects.requireNonNull(context), krecept);

        if (create) {
            JAXBContext context1 = null;
            try { context1 = JAXBContext.newInstance(Krecept.class); } catch (JAXBException e) { e.printStackTrace(); }
            Uid newUid = new Uid(this.handleNewUid(context1));
            LOG.ok("\n\n\nNewUid: {0}", newUid);
            return newUid;
        } return uid;
        }


    private Uid createOrUpdateOtherObjClass(Uid uid, Set<Attribute> attributes, int requesType, int operation) throws JAXBException, NullPointerException {
        
    	String strExtId = null;
    	
    	String nameValue = null;

        String tmpUuid = UUID.randomUUID().toString();
    	
    	boolean create = uid == null;
 
    	JAXBContext context = null;
    	
        Records records = new Records();
        
        try {
		nameValue = AttributeUtil.find("name", attributes).getValue().toString().replaceAll("^\\[|\\]$", "");	
		} catch (Exception e) {
			nameValue = "***Cant find nameValue***";
		}
        
        if (!create) { records.setId(uid.getUidValue()); } else { records.setId(tmpUuid); }

        try { strExtId = AttributeUtil.find(EXT_ID, attributes).getValue().toString().replaceAll("^\\[|\\]$", ""); }
        catch (Exception e) { strExtId = null; }
        

        if (strExtId != null) { records.setExtId(strExtId); }

        records.setOperation(operation);
        records.setName(nameValue);
        Request request = new Request();
        request.setType(requesType);
        request.setRecords(new ArrayList<>());
        request.getRecords().add(records);
        Krecept krecept = new Krecept();
        krecept.setRequest(request);
        try {
            context = JAXBContext.newInstance(Krecept.class);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        bsPut(Objects.requireNonNull(context), krecept);
        if (create) {
            JAXBContext context1 = null;
            try { context1 = JAXBContext.newInstance(Krecept.class); } catch (JAXBException e) { e.printStackTrace(); }
            Uid newUid = new Uid(this.handleNewUid(context1));
            return newUid;
        }
        return uid;
    }

    private void putFieldIfExist(Set<Attribute> set, Records record, String attrName) {

        RecordFields recFields = new RecordFields();

        Attribute attribute = AttributeUtil.find(attrName, set);

        if (attribute != null) {
            recFields.setXmlFieldName(attribute.getName());
            recFields.setXmlFieldValue(attribute.getValue().toString().replaceAll("^\\[|\\]$", ""));
            record.getRecordFields().add(recFields);
        }
    }

    private void bsPut(JAXBContext context, Krecept krecept) {
        try {
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setProperty("com.sun.xml.bind.xmlHeaders", "<?xml version=\"1.0\" encoding=\"UTF8\"?>");

//            marshaller.marshal(krecept, System.out);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            marshaller.marshal(krecept, outputStream);
            String outputXml = (outputStream.toString());

            LOG.ok("\n\n{0}", outputXml);

            try {
                Socket socket = new Socket(configuration.getHostname(), configuration.getPort());
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
                writer.println(outputXml + "\0");
                DataInputStream input = new DataInputStream(socket.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                String line;

                ((Buffer)tmpBuffer).clear();

                while ((line = reader.readLine()) != null) {

                    if (line.contains("</KRECEPT>")) {
                        tmpBuffer.put(line);
//                        LOG.ok("BuffLine: {0]", line);
                        socket.close();
                        break;
                    }
                    tmpBuffer.put(line);
//                    LOG.ok("BuffLine: {0]", line);
                } ((Buffer)tmpBuffer).flip();

//                System.out.println("\nBuffer: " +  tmpBuffer.toString() + "\n");

            } catch (IOException ex) {
                ex.printStackTrace();
            }

        } catch (JAXBException e) {
            e.printStackTrace();
        }

    }



    @Override
    public Uid update(ObjectClass objectClass, Uid uid, Set<Attribute> attributes, OperationOptions operationOptions) {
        System.out.println("Update object: " + objectClass + ", uid: " + uid + ", set: " + attributes + ", operationOptions: " + operationOptions);
        if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            try {
                return this.createOrUpdateUser(uid, attributes, 2);
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        } else if (objectClass.is("AccessGroup")){
            try {
                return this.createOrUpdateOtherObjClass(uid, attributes,3, 2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (objectClass.is("Organization")){
            try {
                return this.createOrUpdateOtherObjClass(uid, attributes,6, 2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }  else if (objectClass.is("Position")){
            try {
                return this.createOrUpdateOtherObjClass(uid, attributes,7, 2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } return null;
    }


    @Override
    public void delete(ObjectClass objectClass, Uid uid, OperationOptions operationOptions) {
        System.out.println("Delete object: " + objectClass + ", uid: " + uid + ", operationOptions: " + operationOptions);
        if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            this.deleteUser(uid,3);
        } else if (objectClass.is("AccessGroup")){
            this.deleteOtherObjClass(uid, 3,3);
        } else if (objectClass.is("Organization")){
            this.deleteOtherObjClass(uid, 6,3);
        } else if (objectClass.is("Position")){
            this.deleteOtherObjClass(uid, 7,3);
        }
    }

    private Uid deleteUser(Uid uid, int operation) {
        String uidValue =  uid.getUidValue();
        JAXBContext context = null;
        Records record = new Records();
        record.setId(uidValue);
        record.setOperation(operation);

        Request request = new Request();
        request.setType(5);
        request.setRecords(new ArrayList<>());
        request.getRecords().add(record);

        Krecept krecept = new Krecept();
        krecept.setRequest(request);

        try {
            context = JAXBContext.newInstance(Krecept.class);
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        bsPut(Objects.requireNonNull(context), krecept);

        return uid;
    }

    private Uid deleteOtherObjClass(Uid uid, int requestType, int operation) {
        String uidValue =  uid.getUidValue();
        JAXBContext context = null;
        Records record = new Records();
        record.setId(uidValue);
        record.setOperation(operation);

        Request request = new Request();
        request.setType(requestType);
        request.setRecords(new ArrayList<>());
        request.getRecords().add(record);

        Krecept krecept = new Krecept();
        krecept.setRequest(request);

        try {
            context = JAXBContext.newInstance(Krecept.class);
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        bsPut(Objects.requireNonNull(context), krecept);

        return uid;
    }

}


