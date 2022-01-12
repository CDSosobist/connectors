package cdsosobist.connid.connectors.mira.rest.connector;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MiraTests {
	
	private static final Log LOG = Log.getLog(MiraTests.class);
	
	private static MiraConfiguration conf;
    private static MiraConnector conn;
    
	@Before
	public void setUp() throws Exception {
        conf = new MiraConfiguration();
        conf.setServiceAddress("https://portaldev.cds.spb.ru");
        conf.setAppId(new GuardedString("system".toCharArray()));
        conf.setsKey(new GuardedString("d^1uC8M!".toCharArray()));
        
        conn = new MiraConnector();
        conn.init(conf);
	}
	
	@Test
	public void testPathBuilder() {
		LOG.ok(conn.md5Request("/caGroups/4"));
	}

    
	

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

}
