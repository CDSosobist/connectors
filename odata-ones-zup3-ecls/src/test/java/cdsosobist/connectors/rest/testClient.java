package cdsosobist.connectors.rest;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class testClient {
    private static final Log LOG = Log.getLog(testClient.class);

    private static zup3Configuration conf;
    private static zup3Connector conn;

    private final ObjectClass accountObjectClass = new ObjectClass(ObjectClass.ACCOUNT_NAME);
    private final ObjectClass positionObjectClass = new ObjectClass("Position");
    private final ObjectClass orgObjectClass = new ObjectClass("Org");
    private final ObjectClass orgUnitObjectClass = new ObjectClass("OrgUnit");
    private final ObjectClass companyStructureObjectClass = new ObjectClass("CompanyStructure");
    private final ObjectClass contactInfoObjectClass = new ObjectClass("ContactInfo");
    private final ObjectClass currEmpDataObjectClass = new ObjectClass("CurrEmpData");
    private final ObjectClass empRoleObjectClass = new ObjectClass("EmpRole");
    private final ObjectClass individualObjectClass = new ObjectClass("Individual");
    private final ObjectClass mainEmpOfIndividualsObjectClass = new ObjectClass("MainEmpOfIndividuals");
    private final ObjectClass staffListObjectClass = new ObjectClass("StaffList");
    private final ObjectClass subOfOrgObjectClass = new ObjectClass("SubOfOrg");
    private final ObjectClass userObjectClass = new ObjectClass("User");



    @BeforeClass
    public static void setUp() throws Exception {
        String fileName = "test.properties";

        final Properties properties = new Properties();
        InputStream inputStream = testClient.class.getClassLoader().getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new IOException("Ошибка, не могу найти " + fileName);
        }
        properties.load(inputStream);

        conf = new zup3Configuration();
        conf.setUsername(properties.getProperty("username"));
        conf.setPassword(new GuardedString(properties.getProperty("password").toCharArray()));
        conf.setServiceAddress(properties.getProperty("serviceAddress"));
        conf.setTrustAllCertificates(Boolean.parseBoolean(properties.getProperty("trustAllCertificates")));


        conn = new zup3Connector();
        conn.init(conf);
    }

    @Test
    public void testConn() {
        LOG.ok("Conf: {0}", conf);
        conn.test();
    }

    @Test
    public void testFindAll() {

        ResultsHandler rh = connectorObject -> {
//            LOG.ok("Result: {0}", connectorObject);
            return true;
        };

        zup3Filter filter = new zup3Filter();
        conn.executeQuery(accountObjectClass, filter, rh, null);
//        conn.executeQuery(positionObjectClass, filter, rh, null);
//        conn.executeQuery(orgObjectClass, filter, rh, null);
//        conn.executeQuery(orgUnitObjectClass, filter, rh, null);
//        conn.executeQuery(companyStructureObjectClass, filter, rh, null);
//        conn.executeQuery(contactInfoObjectClass, filter, rh, null);
//        conn.executeQuery(currEmpDataObjectClass, filter, rh, null);
//        conn.executeQuery(empRoleObjectClass, filter, rh, null);
//        conn.executeQuery(individualObjectClass, filter, rh, null);
//        conn.executeQuery(mainEmpOfIndividualsObjectClass, filter, rh, null);
//        conn.executeQuery(staffListObjectClass, filter, rh, null);
//        conn.executeQuery(subOfOrgObjectClass, filter, rh, null);
//        conn.executeQuery(userObjectClass, filter, rh, null);
    }

}
