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

    @SuppressWarnings("unused")
	private final ObjectClass accountObjectClass = new ObjectClass(ObjectClass.ACCOUNT_NAME);
    @SuppressWarnings("unused")
	private final ObjectClass positionObjectClass = new ObjectClass("Position");
    @SuppressWarnings("unused")
    private final ObjectClass orgObjectClass = new ObjectClass("Org");
    @SuppressWarnings("unused")
    private final ObjectClass orgUnitObjectClass = new ObjectClass("OrgUnit");
    @SuppressWarnings("unused")
    private final ObjectClass companyStructureObjectClass = new ObjectClass("CompanyStructure");
    @SuppressWarnings("unused")
    private final ObjectClass contactInfoObjectClass = new ObjectClass("ContactInfo");
    @SuppressWarnings("unused")
    private final ObjectClass currEmpDataObjectClass = new ObjectClass("CurrEmpData");
    @SuppressWarnings("unused")
    private final ObjectClass empRoleObjectClass = new ObjectClass("EmpRole");
    @SuppressWarnings("unused")
    private final ObjectClass individualObjectClass = new ObjectClass("Individual");
    @SuppressWarnings("unused")
    private final ObjectClass mainEmpOfIndividualsObjectClass = new ObjectClass("MainEmpOfIndividuals");
    @SuppressWarnings("unused")
    private final ObjectClass staffListObjectClass = new ObjectClass("StaffList");
    @SuppressWarnings("unused")
    private final ObjectClass StaffInCSObjectClass = new ObjectClass("StaffInCS");
    @SuppressWarnings("unused")
    private final ObjectClass subOfOrgObjectClass = new ObjectClass("SubOfOrg");
    @SuppressWarnings("unused")
    private final ObjectClass userObjectClass = new ObjectClass("User");
    @SuppressWarnings("unused")
    private final ObjectClass empHistorySliceObjectClass = new ObjectClass("EmpHistorySlice");
    @SuppressWarnings("unused")
    private final ObjectClass ManagerObjectClass = new ObjectClass("Manager");
    @SuppressWarnings("unused")
    private final ObjectClass photoObjectClass = new ObjectClass("Photo");
    @SuppressWarnings("unused")
    private final ObjectClass mainJobObjectClass = new ObjectClass("MainJob");

    @SuppressWarnings("unused")
	private final ObjectClass fIOChangeObjectClass = new ObjectClass("FIO");

    @SuppressWarnings("unused")
	private final ObjectClass empStatusObjectClass = new ObjectClass("EmpStatus");



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
      filter.byUid = "dda126fa-ffef-11eb-baed-005056aa6551";

      conn.executeQuery(empHistorySliceObjectClass, filter, rh, null);

  }

  @Test
  public void testFindAll() {

      ResultsHandler rh = connectorObject -> {
//          LOG.ok("Result: {0}", connectorObject);
          return true;
      };

      zup3Filter filter = new zup3Filter();
//      conn.executeQuery(accountObjectClass, filter, rh, null);
//      conn.executeQuery(positionObjectClass, filter, rh, null);
//      conn.executeQuery(orgObjectClass, filter, rh, null);
//      conn.executeQuery(orgUnitObjectClass, filter, rh, null);
//      conn.executeQuery(companyStructureObjectClass, filter, rh, null);
//      conn.executeQuery(contactInfoObjectClass, filter, rh, null);
//      conn.executeQuery(currEmpDataObjectClass, filter, rh, null);
//      conn.executeQuery(empRoleObjectClass, filter, rh, null);
      conn.executeQuery(individualObjectClass, filter, rh, null);
//      conn.executeQuery(fIOChangeObjectClass, filter, rh, null);
//      conn.executeQuery(mainEmpOfIndividualsObjectClass, filter, rh, null);
//      conn.executeQuery(staffListObjectClass, filter, rh, null);
//      conn.executeQuery(StaffInCSObjectClass, filter, rh, null);
//      conn.executeQuery(empHistorySliceObjectClass, filter, rh, null);
//      conn.executeQuery(subOfOrgObjectClass, filter, rh, null);
//      conn.executeQuery(ManagerObjectClass, filter, rh, null);
//      conn.executeQuery(userObjectClass, filter, rh, null);
//      conn.executeQuery(photoObjectClass, filter, rh, null);
//      conn.executeQuery(mainJobObjectClass, filter, rh, null);
//      conn.executeQuery(empStatusObjectClass, filter, rh, null);
}
}
