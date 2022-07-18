package cdsosobist.connid.connectors.naumen_rest_connector;

import java.util.HashSet;
import java.util.Set;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Uid;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class NaumenTests {
	
	private static NaumenConnector conn = new NaumenConnector();
	private static NaumenConfiguration conf = new NaumenConfiguration();
	
	private final ObjectClass accountObjectClass = new ObjectClass("__ACCOUNT__");
	private final ObjectClass ouObjectClass = new ObjectClass("NaumenOU");
	public static final Log LOG = Log.getLog(NaumenConnector.class);

	

	@Before
	public void setUp() throws Exception {
		conn.init(conf);
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}
	
	@Test
	public void testCreateUser() {
	Set<Attribute> set = new HashSet<>();
	set.add(AttributeBuilder.build("firstName" , "Тест"));
	set.add(AttributeBuilder.build("middleName" , "Тестович"));
	set.add(AttributeBuilder.build("lastName" , "Тестов"));
	set.add(AttributeBuilder.build("title" , "Тестов Т.Т."));
	set.add(AttributeBuilder.build("email" , "testovtt@test.test"));
	set.add(AttributeBuilder.build("parent" , "ou$2316892"));
	set.add(AttributeBuilder.build("post" , "Тестовый аккаунт, созданный Midpoint в ходе разработки интеграции"));
	@SuppressWarnings("unused")
	Uid userUid = conn.create(accountObjectClass, set, null);
	}
	
	@Test
	public void runTestOp() {
		conn.test();
	}

	@Test
	public void testFindById() {
        ResultsHandler rh = connectorObject -> {return true;};
        NaumenFilter filter = new NaumenFilter();
        filter.byUid = "employee$2324404";
//        filter.byUid = "ou$8725602";
        conn.executeQuery(accountObjectClass, filter, rh, null);
//        conn.executeQuery(ouObjectClass, filter, rh, null);
	}
	
	@Test
	public void testAll() {
		ResultsHandler rh = connectorObject -> {return true;};
        NaumenFilter filter = new NaumenFilter();
        conn.executeQuery(accountObjectClass, filter, rh, null);
//        conn.executeQuery(ouObjectClass, filter, rh, null);
	}

	@Test
	public void deleteObject() {
		Uid uid = new Uid("employee$19579300");
		conn.delete(accountObjectClass, uid, null);
	}
}
