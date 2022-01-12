package cdsosobist.connid.connectors.mira.rest.connector;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MiraTests {
	
	private static final Log LOG = Log.getLog(MiraTests.class);
	
	private static MiraConfiguration conf;
    private static MiraConnector conn;
    
    private final ObjectClass accountObjectClass = new ObjectClass(ObjectClass.ACCOUNT_NAME);
    
	@Before
	public void setUp() throws Exception {
        conf = new MiraConfiguration();
        conn = new MiraConnector();
        conn.init(conf);
	}
	
	@Test
	public void testPathBuilder() {
		LOG.ok(conn.md5Request("/caGroups/4"));
	}

    
	@Test
	public void testFindById() {
        ResultsHandler rh = connectorObject -> {return true;};
        MiraFilter filter = new MiraFilter();
        filter.byUid = "85";
        conn.executeQuery(accountObjectClass, filter, rh, null);

	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

}
