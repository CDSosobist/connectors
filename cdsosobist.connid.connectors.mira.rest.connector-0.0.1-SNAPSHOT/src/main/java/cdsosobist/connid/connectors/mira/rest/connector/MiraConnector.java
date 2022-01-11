package cdsosobist.connid.connectors.mira.rest.connector;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import org.apache.http.client.methods.*;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.AuthenticateOp;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.DeleteOp;
import org.identityconnectors.framework.spi.operations.ResolveUsernameOp;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.SyncOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.identityconnectors.framework.spi.operations.UpdateAttributeValuesOp;
import org.identityconnectors.framework.spi.operations.UpdateOp;
import org.identityconnectors.framework.common.objects.*;

import com.evolveum.polygon.rest.AbstractRestConnector;


@ConnectorClass(displayNameKey = "mira.connector.display", configurationClass = MiraConfiguration.class)
public class MiraConnector extends AbstractRestConnector<MiraConfiguration> implements Connector, CreateOp, UpdateOp, UpdateAttributeValuesOp, DeleteOp, AuthenticateOp,
		ResolveUsernameOp, SchemaOp, SyncOp, TestOp, SearchOp<MiraFilter> {
	
	
	public static final Log LOG = Log.getLog(MiraConnector.class);
	
	public MiraConnector() throws IOException {}
	
	private final MiraConfiguration configuration = new MiraConfiguration();
	
	@Override
	public MiraConfiguration getConfiguration() {
		return configuration;
	}
	
	@Override
    public void init(Configuration configuration)
    {
        super.init(configuration);
    }

    @Override
	public Schema schema() {
    	SchemaBuilder schemaBuilder = new SchemaBuilder(MiraConnector.class);
    	this.buildAccountObjectClass(schemaBuilder);
    	this.buildPersonGroupObjectClass(schemaBuilder);
    	this.buildRoleObjectClass(schemaBuilder);
    	this.buildOrganizationObjectClass(schemaBuilder);
    	this.buildOrganizationGroupObjectClass(schemaBuilder);
		return schemaBuilder.build();
	}

	private void buildAccountObjectClass(SchemaBuilder schemaBuilder) {
		ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
		
		AttributeInfoBuilder attrPersMiraIdBuilder = new AttributeInfoBuilder(PathsHandler.PERSMIRAID);
		ociBuilder.addAttributeInfo(attrPersMiraIdBuilder.build());

		AttributeInfoBuilder attrPersLastnameBuilder = new AttributeInfoBuilder(PathsHandler.PERSLASTNAME);
		ociBuilder.addAttributeInfo(attrPersLastnameBuilder.build());

		AttributeInfoBuilder attrPersFirstnameBuilder = new AttributeInfoBuilder(PathsHandler.PERSFIRSTNAME);
		ociBuilder.addAttributeInfo(attrPersFirstnameBuilder.build());

		AttributeInfoBuilder attrPersSurnameBuilder = new AttributeInfoBuilder(PathsHandler.PERSSURNAME);
		ociBuilder.addAttributeInfo(attrPersSurnameBuilder.build());

		AttributeInfoBuilder attrPersIsUserBuilder = new AttributeInfoBuilder(PathsHandler.PERSISUSER);
		ociBuilder.addAttributeInfo(attrPersIsUserBuilder.build());

		AttributeInfoBuilder attrPersMiraLoginBuilder = new AttributeInfoBuilder(PathsHandler.PERSMIRALOGIN);
		ociBuilder.addAttributeInfo(attrPersMiraLoginBuilder.build());

		AttributeInfoBuilder attrPersMiraPwdBuilder = new AttributeInfoBuilder(PathsHandler.PERSMIRAPWD);
		ociBuilder.addAttributeInfo(attrPersMiraPwdBuilder.build());

		AttributeInfoBuilder attrPersOuIdBuilder = new AttributeInfoBuilder(PathsHandler.PERSOUID);
		ociBuilder.addAttributeInfo(attrPersOuIdBuilder.build());

		AttributeInfoBuilder attrPersOuNameBuilder = new AttributeInfoBuilder(PathsHandler.PERSOUNAME);
		ociBuilder.addAttributeInfo(attrPersOuNameBuilder.build());

		AttributeInfoBuilder attrPersTitleIdBuilder = new AttributeInfoBuilder(PathsHandler.PERSTITLEID);
		ociBuilder.addAttributeInfo(attrPersTitleIdBuilder.build());

		AttributeInfoBuilder attrPersTitleNameBuilder = new AttributeInfoBuilder(PathsHandler.PERSTITLENAME);
		ociBuilder.addAttributeInfo(attrPersTitleNameBuilder.build());

		AttributeInfoBuilder attrPersSexBuilder = new AttributeInfoBuilder(PathsHandler.PERSSEX);
		ociBuilder.addAttributeInfo(attrPersSexBuilder.build());

		AttributeInfoBuilder attrPersMailBuilder = new AttributeInfoBuilder(PathsHandler.PERSMAIL);
		ociBuilder.addAttributeInfo(attrPersMailBuilder.build());

		AttributeInfoBuilder attrPersStatusBuilder = new AttributeInfoBuilder(PathsHandler.PERSSTATUS);
		ociBuilder.addAttributeInfo(attrPersStatusBuilder.build());

		AttributeInfoBuilder attrPersExtIdBuilder = new AttributeInfoBuilder(PathsHandler.PERSEXTID);
		ociBuilder.addAttributeInfo(attrPersExtIdBuilder.build());
		
		schemaBuilder.defineObjectClass(ociBuilder.build());
	}

	private void buildPersonGroupObjectClass(SchemaBuilder schemaBuilder) {
		ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
		ociBuilder.setType("PersonsGroup");
		
		AttributeInfoBuilder attrPersGroupMiraIdBuilder = new AttributeInfoBuilder(PathsHandler.PERSGROUPMIRAID);
		ociBuilder.addAttributeInfo(attrPersGroupMiraIdBuilder.build());

		AttributeInfoBuilder attrPersGroupNameBuilder = new AttributeInfoBuilder(PathsHandler.PERSGROUPNAME);
		ociBuilder.addAttributeInfo(attrPersGroupNameBuilder.build());

		AttributeInfoBuilder attrPersGroupDescrBuilder = new AttributeInfoBuilder(PathsHandler.PERSGROUPDESCR);
		ociBuilder.addAttributeInfo(attrPersGroupDescrBuilder.build());

		AttributeInfoBuilder attrPersGroupParentIdBuilder = new AttributeInfoBuilder(PathsHandler.PERSGROUPPARENTID);
		ociBuilder.addAttributeInfo(attrPersGroupParentIdBuilder.build());

		AttributeInfoBuilder attrPersGroupParentNameBuilder = new AttributeInfoBuilder(PathsHandler.PERSGROUPPARENTNAME);
		ociBuilder.addAttributeInfo(attrPersGroupParentNameBuilder.build());

		AttributeInfoBuilder attrPersGroupKindBuilder = new AttributeInfoBuilder(PathsHandler.PERSGROUPKIND);
		ociBuilder.addAttributeInfo(attrPersGroupKindBuilder.build());

		schemaBuilder.defineObjectClass(ociBuilder.build());
	}

	private void buildRoleObjectClass(SchemaBuilder schemaBuilder) {
		ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
		ociBuilder.setType("Role");
		
		AttributeInfoBuilder attrRoleMiraIdBuilder = new AttributeInfoBuilder(PathsHandler.ROLEMIRAID);
		ociBuilder.addAttributeInfo(attrRoleMiraIdBuilder.build());

		AttributeInfoBuilder attrRoleProfileIdBuilder = new AttributeInfoBuilder(PathsHandler.ROLEPROFILEID);
		ociBuilder.addAttributeInfo(attrRoleProfileIdBuilder.build());

		AttributeInfoBuilder attrRoleNameBuilder = new AttributeInfoBuilder(PathsHandler.ROLENAME);
		ociBuilder.addAttributeInfo(attrRoleNameBuilder.build());

		AttributeInfoBuilder attrSysRoleNameBuilder = new AttributeInfoBuilder(PathsHandler.SYSROLENAME);
		ociBuilder.addAttributeInfo(attrSysRoleNameBuilder.build());

		AttributeInfoBuilder attrRoleIsDefaultBuilder = new AttributeInfoBuilder(PathsHandler.ROLEISDEFAULT);
		ociBuilder.addAttributeInfo(attrRoleIsDefaultBuilder.build());
		
		schemaBuilder.defineObjectClass(ociBuilder.build());
	}

	private void buildOrganizationObjectClass(SchemaBuilder schemaBuilder) {
		ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
		ociBuilder.setType("Organization");
		
		AttributeInfoBuilder attrOrgMiraIdBuilder = new AttributeInfoBuilder(PathsHandler.ORGMIRAID);
		ociBuilder.addAttributeInfo(attrOrgMiraIdBuilder.build());

		AttributeInfoBuilder attrOrgNameBuilder = new AttributeInfoBuilder(PathsHandler.ORGNAME);
		ociBuilder.addAttributeInfo(attrOrgNameBuilder.build());

		AttributeInfoBuilder attrOrgParentIdBuilder = new AttributeInfoBuilder(PathsHandler.ORGPARENTID);
		ociBuilder.addAttributeInfo(attrOrgParentIdBuilder.build());

		AttributeInfoBuilder attrOrgShortNameBuilder = new AttributeInfoBuilder(PathsHandler.ORGSHORTNAME);
		ociBuilder.addAttributeInfo(attrOrgShortNameBuilder.build());

		AttributeInfoBuilder attrOrgExtIdBuilder = new AttributeInfoBuilder(PathsHandler.ORGEXTID);
		ociBuilder.addAttributeInfo(attrOrgExtIdBuilder.build());

		schemaBuilder.defineObjectClass(ociBuilder.build());
	}

	private void buildOrganizationGroupObjectClass(SchemaBuilder schemaBuilder) {
		ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
		ociBuilder.setType("Organization");
		
		AttributeInfoBuilder attrOrgGroupMiraIdBuilder = new AttributeInfoBuilder(PathsHandler.ORGGROUPMIRAID);
		ociBuilder.addAttributeInfo(attrOrgGroupMiraIdBuilder.build());

		AttributeInfoBuilder attrOrgGroupNameBuilder = new AttributeInfoBuilder(PathsHandler.ORGGROUPNAME);
		ociBuilder.addAttributeInfo(attrOrgGroupNameBuilder.build());

		AttributeInfoBuilder attrOrgGroupDescrBuilder = new AttributeInfoBuilder(PathsHandler.ORGGROUPDESCR);
		ociBuilder.addAttributeInfo(attrOrgGroupDescrBuilder.build());

		AttributeInfoBuilder attrOrgGroupParentIdBuilder = new AttributeInfoBuilder(PathsHandler.ORGGROUPPARENTID);
		ociBuilder.addAttributeInfo(attrOrgGroupParentIdBuilder.build());

		AttributeInfoBuilder attrOrgGroupParentNameBuilder = new AttributeInfoBuilder(PathsHandler.ORGGROUPPARENTNAME);
		ociBuilder.addAttributeInfo(attrOrgGroupParentNameBuilder.build());

		AttributeInfoBuilder attrOrgGroupKindBuilder = new AttributeInfoBuilder(PathsHandler.ORGGROUPKIND);
		ociBuilder.addAttributeInfo(attrOrgGroupKindBuilder.build());

		schemaBuilder.defineObjectClass(ociBuilder.build());		
	}
	
	public void md5Request(String requestPath) {
		//TODO Изменить public на private
		final StringBuilder sb = new StringBuilder();
		final StringBuilder sb1 = new StringBuilder();
		configuration.getAppId().access(chars -> sb.append(new String(chars)));
		configuration.getsKey().access(chars -> sb1.append(new String(chars)));
		String preRequest = configuration.getServiceAddress() + PathsHandler.MAINPATH + requestPath + PathsHandler.APPIDPATH + sb.toString();
		String preHash = PathsHandler.SKEYPATH + sb1.toString();
		LOG.error(preRequest + preHash);
		LOG.error(preRequest + PathsHandler.SIGNPATH + pathToHash(preRequest + preHash));
//		System.out.println(preRequest);
//		return preRequest;
	}

	private static String pathToHash(String preRequest) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(preRequest.getBytes());
			BigInteger no = new BigInteger(1, messageDigest);
			String hashText = no.toString(16);
			while (hashText.length() < 32) {
                hashText = "0" + hashText;
            }
            return hashText.toUpperCase();
		} catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
	}

	@Override
	public void test() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sync(ObjectClass objectClass, SyncToken token, SyncResultsHandler handler, OperationOptions options) {
		// TODO Auto-generated method stub

	}

	@Override
	public SyncToken getLatestSyncToken(ObjectClass objectClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uid resolveUsername(ObjectClass objectClass, String username, OperationOptions options) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uid authenticate(ObjectClass objectClass, String username, GuardedString password,
			OperationOptions options) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(ObjectClass objectClass, Uid uid, OperationOptions options) {
		// TODO Auto-generated method stub

	}

	@Override
	public Uid addAttributeValues(ObjectClass objclass, Uid uid, Set<Attribute> valuesToAdd, OperationOptions options) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uid removeAttributeValues(ObjectClass objclass, Uid uid, Set<Attribute> valuesToRemove,
			OperationOptions options) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uid update(ObjectClass objectClass, Uid uid, Set<Attribute> replaceAttributes, OperationOptions options) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uid create(ObjectClass objectClass, Set<Attribute> createAttributes, OperationOptions options) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public FilterTranslator<MiraFilter> createFilterTranslator(ObjectClass objectClass, OperationOptions options) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void executeQuery(ObjectClass objectClass, MiraFilter query, ResultsHandler handler,
			OperationOptions options) {
		// TODO Auto-generated method stub
		
	}

}
