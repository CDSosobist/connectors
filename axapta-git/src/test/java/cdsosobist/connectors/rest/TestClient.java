package cdsosobist.connectors.rest;

public class TestClient {

    private static final Log LOG = Log.getLog(testClient.class);

    private static axaptaConnector conn;
    private static tokenHandler th = new tokenHandler();

    private final ObjectClass accountObjectClass = new ObjectClass(ObjectClass.ACCOUNT_NAME);
    private final ObjectClass organizationObjectClass = new ObjectClass("Organization");
    private final ObjectClass providerObjectClass = new ObjectClass("Provider");
    private final ObjectClass contractObjectClass = new ObjectClass("Contract");

    @BeforeClass
    public static void setUp() {
        axaptaConfiguration conf = new axaptaConfiguration();
        GuardedString tmPass = new GuardedString(("<thtpjdsqrheukzr1!").toCharArray());
        LOG.ok("tmPass: {0}", tmPass);
        conf.setUsername("koshelevra");
        LOG.ok("userName: {0}", conf.getUsername());
        conf.setPassword(tmPass);
        conf.setServiceAddress("axapi.cds.spb.ru");
        conn = new axaptaConnector();
        conn.init(conf);
    }

    @Test
    public void testTokenGetting() throws IOException {
        setUp();
//        LOG.ok("conn: {0}", conn);
        LOG.ok("TokenValue: {0}", conn.getCurrToken());
    }


    @Test
    public void testFindAll() {

        setUp();

        ResultsHandler rh = connectorObject -> {
//            LOG.ok("Result: {0}", connectorObject);
            return true;
        };

        axaptaFilter filter = new axaptaFilter();
//        conn.executeQuery(accountObjectClass, filter, rh, null);
//        conn.executeQuery(providerObjectClass, filter, rh, null);
//        conn.executeQuery(organizationObjectClass, filter, rh, null);
        conn.executeQuery(contractObjectClass, filter, rh, null);
    }

}
