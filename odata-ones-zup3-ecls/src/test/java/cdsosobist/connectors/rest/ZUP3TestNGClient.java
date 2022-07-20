package cdsosobist.connectors.rest;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class ZUP3TestNGClient {
	
    private static final Log LOG = Log.getLog(testClient.class);

    private static zup3Configuration conf;
    private static zup3Connector conn;

    private final ObjectClass empHistorySliceObjectClass = new ObjectClass("EmpHistorySlice");
    private final ObjectClass gphContractObjectClass = new ObjectClass("GPH");
    @SuppressWarnings("unused")
    private final ObjectClass mainJobObjectClass = new ObjectClass("MainJob");
    
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
      LOG.ok("\n\n\nUsername - {0}\n\n\n", properties.getProperty("username"));
      conf.setPassword(new GuardedString(properties.getProperty("password").toCharArray()));
      conf.setServiceAddress(properties.getProperty("serviceAddress"));
      conf.setTrustAllCertificates(Boolean.parseBoolean(properties.getProperty("trustAllCertificates")));


      conn = new zup3Connector();
      conn.init(conf);
  }
	
  @Test
  public void testByUid() {

      ResultsHandler rh = connectorObject -> {
          return true;
      };

      zup3Filter filter = new zup3Filter();
      filter.byUid = "75a1b600-7360-11e7-80c3-005056bf108e";

      conn.executeQuery(mainJobObjectClass, filter, rh, null);

  }

  @Test
  public void testAll() {

      ResultsHandler rh = connectorObject -> {
          return true;
      };

      zup3Filter filter = new zup3Filter();

      conn.executeQuery(mainJobObjectClass, filter, rh, null);

  }

}
