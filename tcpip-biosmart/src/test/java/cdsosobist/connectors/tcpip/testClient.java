package cdsosobist.connectors.tcpip;


import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

import static cdsosobist.connectors.tcpip.AttributeHandler.*;

public class testClient {
    private static final Log LOG = Log.getLog(testClient.class);

    private static biosmartConfiguration conf;
    private static biosmartConnector conn;

    ObjectClass accountObjectClass = new ObjectClass(ObjectClass.ACCOUNT_NAME);
    ObjectClass accessGroupObjectClass = new ObjectClass("AccessGroup");
    ObjectClass organizationObjectClass = new ObjectClass("Organization");
    ObjectClass positionObjectClass = new ObjectClass("Position");


    @BeforeClass
    public static void setUp() throws Exception {
//        String fileName = "test.properties";
//
//        final Properties properties = new Properties();
//        InputStream inputStream = testClient.class.getClassLoader().getResourceAsStream(fileName);
//        if (inputStream == null) {
//            throw new IOException("Ошибка, не могу найти " + fileName);
//        }
//        properties.load(inputStream);

        conf = new biosmartConfiguration();
//        conf.setHostname("scds-biosmart03");
//        conf.setPort(60005);
//
        conn = new biosmartConnector();
        conn.init(conf);
    }


    @Test
    public void testConn() {
        conn.test();
    }


    @Test
    public void testFindAll() {

        ResultsHandler rh = connectorObject -> {
            System.out.println(connectorObject);
            return true;
        };

        biosmartFilter filter = new biosmartFilter();
        conn.executeQuery(accountObjectClass, filter, rh, null);
//        conn.executeQuery(accessGroupObjectClass, null, rh, null);
//        conn.executeQuery(organizationObjectClass, null, rh, null);
//        conn.executeQuery(positionObjectClass, null, rh, null);
    }

    @Test
    public void testCreateUser() {
        Set<Attribute> set = new HashSet<>();
        String randName = "Ержон";
        set.add(AttributeBuilder.build(Name.NAME, randName));

        set.add(AttributeBuilder.build(ACC_FNAME, "Ержон"));
        set.add(AttributeBuilder.build(ACC_LNAME, "Эчпочмаков"));
        set.add(AttributeBuilder.build(ACC_MNAME, "Бибигонович"));
        set.add(AttributeBuilder.build(ACC_BEG_DATE, "01.01.2019"));
        set.add(AttributeBuilder.build(ACC_BIRTH_DATE, "01.01.1980"));
        set.add(AttributeBuilder.build(ACC_END_DATE, "31.12.2020"));
        set.add(AttributeBuilder.build(ACC_GENDER, "1"));
        set.add(AttributeBuilder.build(ACC_ORG_ID, "bs67200098"));
        set.add(AttributeBuilder.build(ACC_PHOTO, "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"));
        set.add(AttributeBuilder.build(EXT_ID, "111111-1111-111111-11-111"));

        Uid userUid = conn.create(accountObjectClass, set, null);
        LOG.ok("New user Uid is: {0}, name: {1}", userUid.getUidValue(), randName);
    }


    @Test
    public void testCreateOrganization() {
        Set<Attribute> set = new HashSet<>();
        String randName = "Фабрика-ибабрика";

        set.add(AttributeBuilder.build(ORG_NAME, randName));

        Uid orgUid = conn.create(organizationObjectClass, set, null);
        LOG.ok("New organization Uid is: {0}, name is: {1}", orgUid.getUidValue(), randName);
    }


    @Test
    public void testUpdateOrganization() {
        Set<Attribute> set = new HashSet<>();
        String gguid = "12abd10e-5074-4234-8d52-7cb88ab3032a";

        set.add(AttributeBuilder.build(ORG_NAME, "Фабрика-шмабрика"));
        set.add(AttributeBuilder.build(ORG_UID, "pst00033"));

        Uid uid = new Uid(gguid);

        Uid orgUid = conn.update(organizationObjectClass, uid, set, null);
    }


    @Test
    public void testUpdateUser() {
        Set<Attribute> set = new HashSet<>();
        String gguid = "9e585d20-8a72-4c72-96ff-0e167e6f21a4";
        set.add(AttributeBuilder.build(Name.NAME, gguid));
        set.add(AttributeBuilder.build(Uid.NAME, gguid));

        set.add(AttributeBuilder.build(ACC_PHOTO, ""));
        set.add(AttributeBuilder.build(POLICY, "bs67200122"));
//        set.add(AttributeBuilder.build(EXT_ID, "777555333"));


        Uid uid = new Uid(gguid);

        Uid userUid = conn.update(accountObjectClass, uid, set, null);


    }

    @Test
    public void testUpdateOtherObject() {
        Set<Attribute> set = new HashSet<>();
        String gname = "Тестовая организация";
        String gguid = "6ad42ef8-da64-48df-a12c-9497b65c9a3a";

        set.add(AttributeBuilder.build(Name.NAME, gguid));
        set.add(AttributeBuilder.build(Uid.NAME, gguid));
        set.add(AttributeBuilder.build(EXT_ID, "000001"));

        set.add(AttributeBuilder.build("name", gname));

        Uid uid = new Uid(gguid);
        Name name = new Name(gname);
        Uid userUid = conn.update(organizationObjectClass, uid, set,null);


    }

    @Test
    public void testDeleteUser() {

        String gguid = "bs67200147";

        Uid uid = new Uid(gguid);
        conn.delete(accountObjectClass, uid, null);
    }
}
