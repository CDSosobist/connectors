package CDSOsobist.connectors.rest.ones.ZUP2;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

//import static CDSosobist.IDMConnectors.odata.EmployysHandler.*;
//import static CDSosobist.IDMConnectors.odata.PersonasHandler.PERSONA_UID;
//import static CDSosobist.IDMConnectors.odata.ResourceHandler.*;


@SuppressWarnings("unused")
public class TestClient {

    private static final Log LOG = Log.getLog(TestClient.class);

    private static onesConfiguration conf;
    private static onesConnector conn;

    private final ObjectClass accountObjectClass = new ObjectClass(ObjectClass.ACCOUNT_NAME);
    private final ObjectClass positionObjectClass = new ObjectClass("role");
    private final ObjectClass organisationObjectClass = new ObjectClass("Org");
    private final ObjectClass orgunitObjectClass = new ObjectClass("OrgUnit");



    @BeforeClass
    public static void setUp() throws Exception {
        String fileName = "test.properties";

        final Properties properties = new Properties();
        InputStream inputStream = TestClient.class.getClassLoader().getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new IOException("Ошибка, не могу найти " + fileName);
        }
        properties.load(inputStream);

        conf = new onesConfiguration();
        conf.setUsername(properties.getProperty("username"));
        conf.setPassword(new GuardedString(properties.getProperty("password").toCharArray()));
        conf.setServiceAddress(properties.getProperty("serviceAddress"));
        conf.setTrustAllCertificates(Boolean.parseBoolean(properties.getProperty("trustAllCertificates")));


        conn = new onesConnector();
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

        onesFilter filter = new onesFilter();
//        conn.executeQuery(accountObjectClass, filter, rh, null);
//        conn.executeQuery(positionObjectClass, filter, rh, null);
//        conn.executeQuery(organisationObjectClass, filter, rh, null);
        conn.executeQuery(orgunitObjectClass, filter, rh, null);
    }

}
