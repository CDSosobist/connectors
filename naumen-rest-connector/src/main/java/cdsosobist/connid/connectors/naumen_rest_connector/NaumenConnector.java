/**
 * @author KoshelevRA
 *
 */
package cdsosobist.connid.connectors.naumen_rest_connector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorIOException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.DeleteOp;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.identityconnectors.framework.spi.operations.UpdateOp;
import org.json.JSONArray;
import org.json.JSONObject;

import com.evolveum.polygon.rest.AbstractRestConnector;

@ConnectorClass(configurationClass = NaumenConfiguration.class, displayNameKey = "sample.connector.display")
public class NaumenConnector extends AbstractRestConnector<NaumenConfiguration> implements Connector, CreateOp, UpdateOp, DeleteOp, SchemaOp, SearchOp<NaumenFilter>, TestOp {

	private final NaumenConfiguration configuration = new NaumenConfiguration();
	public static final Log LOG = Log.getLog(NaumenConnector.class);
	private static final String LINE = "\r\n";
	
	
	@Override
	public NaumenConfiguration getConfiguration() {
		//System.out.print("\n\nNaumen connector - запуск getConfiguration()\n\n");
		//System.out.print("\n\nNaumen connector - ServiceAddress is " + configuration.getServiceAddress() + "\n\n");
		return configuration;
	}
	
	@Override
	public void init(Configuration configuration) {
		//System.out.print("\n\nNaumen connector - запуск init(), \n\n");
		super.init(configuration);
	}
	
	@Override
	public Schema schema() {
		
		SchemaBuilder schemaBuilder = new SchemaBuilder(NaumenConnector.class);
		this.buildAccountObjectClass(schemaBuilder);
		this.buildOUOBjectClass(schemaBuilder);
		return schemaBuilder.build();
		
	}

	private void buildAccountObjectClass(SchemaBuilder schemaBuilder) {
		
		ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
		
		AttributeInfoBuilder attrPersemailBuilder = new AttributeInfoBuilder(PathsHandler.PERSEMAIL);
		ociBuilder.addAttributeInfo(attrPersemailBuilder.build());
			
		AttributeInfoBuilder attrPersisEmployeeActiveBuilder = new AttributeInfoBuilder(PathsHandler.PERSISACTIVE);
		ociBuilder.addAttributeInfo(attrPersisEmployeeActiveBuilder.build());
		attrPersisEmployeeActiveBuilder.setType(Boolean.class);
			
		AttributeInfoBuilder attrPersinternalPhoneNumberBuilder = new AttributeInfoBuilder(PathsHandler.PERSINTERNALPHONE);
		ociBuilder.addAttributeInfo(attrPersinternalPhoneNumberBuilder.build());
			
		AttributeInfoBuilder attrPerscityPhoneNumberBuilder = new AttributeInfoBuilder(PathsHandler.PERSCITYPHONE);
		ociBuilder.addAttributeInfo(attrPerscityPhoneNumberBuilder.build());
			
		AttributeInfoBuilder attrPersremovalDateBuilder = new AttributeInfoBuilder(PathsHandler.PERSREMOVALDATE);
		ociBuilder.addAttributeInfo(attrPersremovalDateBuilder.build());
			
		AttributeInfoBuilder attrPerslastModifiedDateBuilder = new AttributeInfoBuilder(PathsHandler.PERSMODIFDATE);
		ociBuilder.addAttributeInfo(attrPerslastModifiedDateBuilder.build());
			
		AttributeInfoBuilder attrPersdateOfBirthBuilder = new AttributeInfoBuilder(PathsHandler.PERSBIRTHDATE);
		ociBuilder.addAttributeInfo(attrPersdateOfBirthBuilder.build());
			
		AttributeInfoBuilder attrPerscreationDateBuilder = new AttributeInfoBuilder(PathsHandler.PERSCREATIONDATE);
		ociBuilder.addAttributeInfo(attrPerscreationDateBuilder.build());
			
		AttributeInfoBuilder attrPerspostBuilder = new AttributeInfoBuilder(PathsHandler.PERSPOSITION);
		ociBuilder.addAttributeInfo(attrPerspostBuilder.build());
			
		AttributeInfoBuilder attrPershomePhoneNumberBuilder = new AttributeInfoBuilder(PathsHandler.PERSHOMEPHONE);
		ociBuilder.addAttributeInfo(attrPershomePhoneNumberBuilder.build());
			
		AttributeInfoBuilder attrPersisEmployeeLockedBuilder = new AttributeInfoBuilder(PathsHandler.PERSISLOCKED);
		ociBuilder.addAttributeInfo(attrPersisEmployeeLockedBuilder.build());
		attrPersisEmployeeLockedBuilder.setType(Boolean.class);
			
		AttributeInfoBuilder attrPersfirstNameBuilder = new AttributeInfoBuilder(PathsHandler.PERSFIRSTNAME);
		ociBuilder.addAttributeInfo(attrPersfirstNameBuilder.build());
			
		AttributeInfoBuilder attrPersperformerBuilder = new AttributeInfoBuilder(PathsHandler.PERSISPERFORMER);
		ociBuilder.addAttributeInfo(attrPersperformerBuilder.build());
		attrPersperformerBuilder.setType(Boolean.class);
			
		AttributeInfoBuilder attrPersprivateCodeBuilder = new AttributeInfoBuilder(PathsHandler.PERSPRIVCODE);
		ociBuilder.addAttributeInfo(attrPersprivateCodeBuilder.build());
			
		AttributeInfoBuilder attrPersloginBuilder = new AttributeInfoBuilder(PathsHandler.PERSLOGIN);
		ociBuilder.addAttributeInfo(attrPersloginBuilder.build());
			
		AttributeInfoBuilder attrPersmobilePhoneNumberBuilder = new AttributeInfoBuilder(PathsHandler.PERSMOBILEPHONE);
		ociBuilder.addAttributeInfo(attrPersmobilePhoneNumberBuilder.build());
			
		AttributeInfoBuilder attrPersnumberBuilder = new AttributeInfoBuilder(PathsHandler.PERSNUMBER);
		ociBuilder.addAttributeInfo(attrPersnumberBuilder.build());
			
		AttributeInfoBuilder attrPersparentBuilder = new AttributeInfoBuilder(PathsHandler.PERSPARENT);
		ociBuilder.addAttributeInfo(attrPersparentBuilder.build());
			
		AttributeInfoBuilder attrPersmiddleNameBuilder = new AttributeInfoBuilder(PathsHandler.PERSMIDDLENAME);
		ociBuilder.addAttributeInfo(attrPersmiddleNameBuilder.build());
			
		AttributeInfoBuilder attrPerspasswordBuilder = new AttributeInfoBuilder(PathsHandler.PERSPASSWORD);
		ociBuilder.addAttributeInfo(attrPerspasswordBuilder.build());
			
		AttributeInfoBuilder attrPersremovedBuilder = new AttributeInfoBuilder(PathsHandler.PERSISREMOVED);
		ociBuilder.addAttributeInfo(attrPersremovedBuilder.build());
		attrPersremovedBuilder.setType(Boolean.class);
			
		AttributeInfoBuilder attrPerscommentAuthorAliasBuilder = new AttributeInfoBuilder(PathsHandler.PERSALIAS);
		ociBuilder.addAttributeInfo(attrPerscommentAuthorAliasBuilder.build());
			
		AttributeInfoBuilder attrPersimmediateSupervisorBuilder = new AttributeInfoBuilder(PathsHandler.PERSMANAGER);
		ociBuilder.addAttributeInfo(attrPersimmediateSupervisorBuilder.build());
			
		AttributeInfoBuilder attrPersemployeeForIntegrationBuilder = new AttributeInfoBuilder(PathsHandler.PERSISINTEGRATION);
		ociBuilder.addAttributeInfo(attrPersemployeeForIntegrationBuilder.build());
		attrPersemployeeForIntegrationBuilder.setType(Boolean.class);
			
		AttributeInfoBuilder attrPersphonesIndexBuilder = new AttributeInfoBuilder(PathsHandler.PERSPHONESINDEX);
		ociBuilder.addAttributeInfo(attrPersphonesIndexBuilder.build());
			
		AttributeInfoBuilder attrPersmetaClassBuilder = new AttributeInfoBuilder(PathsHandler.PERSMETACLASS);
		ociBuilder.addAttributeInfo(attrPersmetaClassBuilder.build());
			
		AttributeInfoBuilder attrPerstitleBuilder = new AttributeInfoBuilder(PathsHandler.PERSFULLNAME);
		ociBuilder.addAttributeInfo(attrPerstitleBuilder.build());
			
		AttributeInfoBuilder attrPerslastNameBuilder = new AttributeInfoBuilder(PathsHandler.PERSLASTNAME);
		ociBuilder.addAttributeInfo(attrPerslastNameBuilder.build());
			
		AttributeInfoBuilder attrPersimageBuilder = new AttributeInfoBuilder(PathsHandler.PERSIMAGE);
		ociBuilder.addAttributeInfo(attrPersimageBuilder.build());
			
		AttributeInfoBuilder attrPerssysUserStorageBuilder = new AttributeInfoBuilder(PathsHandler.PERSSTORAGE);
		ociBuilder.addAttributeInfo(attrPerssysUserStorageBuilder.build());
			
		AttributeInfoBuilder attrPersidHolderBuilder = new AttributeInfoBuilder(PathsHandler.PERSIDHOLDER);
		ociBuilder.addAttributeInfo(attrPersidHolderBuilder.build());
			
		AttributeInfoBuilder attrPersexternalLinksBuilder = new AttributeInfoBuilder(PathsHandler.PERSEXTLINKS);
		ociBuilder.addAttributeInfo(attrPersexternalLinksBuilder.build());
			
		AttributeInfoBuilder attrPersforResetPassBuilder = new AttributeInfoBuilder(PathsHandler.PERSFORRESTPASS);
		ociBuilder.addAttributeInfo(attrPersforResetPassBuilder.build());
		attrPersforResetPassBuilder.setType(Boolean.class);
			
		AttributeInfoBuilder attrPerstelegramIdBuilder = new AttributeInfoBuilder(PathsHandler.PERSTELEGRAMID);
		ociBuilder.addAttributeInfo(attrPerstelegramIdBuilder.build());
			
		AttributeInfoBuilder attrPersiconBuilder = new AttributeInfoBuilder(PathsHandler.PERSICON);
		ociBuilder.addAttributeInfo(attrPersiconBuilder.build());
			
		AttributeInfoBuilder attrPerstelegramBuilder = new AttributeInfoBuilder(PathsHandler.PERSTELEGRAMLOGIN);
		ociBuilder.addAttributeInfo(attrPerstelegramBuilder.build());
			
		AttributeInfoBuilder attrPersisGenPassBuilder = new AttributeInfoBuilder(PathsHandler.PERSNEEDGENPASS);
		ociBuilder.addAttributeInfo(attrPersisGenPassBuilder.build());
		attrPersisGenPassBuilder.setType(Boolean.class);
		
		AttributeInfoBuilder attrPersEmpIDBuilder = new AttributeInfoBuilder(PathsHandler.PERSEMPID);
		ociBuilder.addAttributeInfo(attrPersEmpIDBuilder.build());
			
		AttributeInfoBuilder attrPersFLIDBuilder = new AttributeInfoBuilder(PathsHandler.PERSFLID);
		ociBuilder.addAttributeInfo(attrPersFLIDBuilder.build());
			
		AttributeInfoBuilder attrPersCorpPhoneBuilder = new AttributeInfoBuilder(PathsHandler.PERSCORPPHONE);
		ociBuilder.addAttributeInfo(attrPersCorpPhoneBuilder.build());
			
		schemaBuilder.defineObjectClass(ociBuilder.build());
	}

	private void buildOUOBjectClass(SchemaBuilder schemaBuilder) {

		ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
		ociBuilder.setType("NaumenOU");
		
		AttributeInfoBuilder attrOuparentBuilder = new AttributeInfoBuilder(PathsHandler.OUPARENT);
		ociBuilder.addAttributeInfo(attrOuparentBuilder.build());
			
		AttributeInfoBuilder attrOuremovalDateBuilder = new AttributeInfoBuilder(PathsHandler.OUREMOVDATE);
		ociBuilder.addAttributeInfo(attrOuremovalDateBuilder.build());
			
		AttributeInfoBuilder attrOulastModifiedDateBuilder = new AttributeInfoBuilder(PathsHandler.OUMODIFYDATE);
		ociBuilder.addAttributeInfo(attrOulastModifiedDateBuilder.build());
			
		AttributeInfoBuilder attrOucreationDateBuilder = new AttributeInfoBuilder(PathsHandler.OUCREATIONDATE);
		ociBuilder.addAttributeInfo(attrOucreationDateBuilder.build());
			
		AttributeInfoBuilder attrOutitleBuilder = new AttributeInfoBuilder(PathsHandler.OUNAME);
		ociBuilder.addAttributeInfo(attrOutitleBuilder.build());
			
		AttributeInfoBuilder attrOunumberBuilder = new AttributeInfoBuilder(PathsHandler.OUNUMBER);
		ociBuilder.addAttributeInfo(attrOunumberBuilder.build());
			
		AttributeInfoBuilder attrOuremovedBuilder = new AttributeInfoBuilder(PathsHandler.OUISREMOVED);
		ociBuilder.addAttributeInfo(attrOuremovedBuilder.build());
		attrOuremovedBuilder.setType(Boolean.class);
			
		AttributeInfoBuilder attrOuheadBuilder = new AttributeInfoBuilder(PathsHandler.OUMANAGER);
		ociBuilder.addAttributeInfo(attrOuheadBuilder.build());
			
		AttributeInfoBuilder attrOumetaClassBuilder = new AttributeInfoBuilder(PathsHandler.OUMETACLASS);
		ociBuilder.addAttributeInfo(attrOumetaClassBuilder.build());
			
		AttributeInfoBuilder attrOudstngshdBuilder = new AttributeInfoBuilder(PathsHandler.OUDN);
		ociBuilder.addAttributeInfo(attrOudstngshdBuilder.build());
			
		AttributeInfoBuilder attrOuidHolderBuilder = new AttributeInfoBuilder(PathsHandler.OUIDHOLDER);
		ociBuilder.addAttributeInfo(attrOuidHolderBuilder.build());
			
		AttributeInfoBuilder attrOuadressBuilder = new AttributeInfoBuilder(PathsHandler.OUADDRESS);
		ociBuilder.addAttributeInfo(attrOuadressBuilder.build());
			
		AttributeInfoBuilder attrOuiconBuilder = new AttributeInfoBuilder(PathsHandler.OUICON);
		ociBuilder.addAttributeInfo(attrOuiconBuilder.build());
		
		schemaBuilder.defineObjectClass(ociBuilder.build());
	}
	
	

	@Override
	public void executeQuery(ObjectClass objectClass, NaumenFilter query, ResultsHandler handler,
			OperationOptions options) {
		
		//System.out.print("\n\nNaumen connector - перешёл в executeQuery:\n	objectClass - " + objectClass +"\n	query - " + query + "\n	handler - " + handler + "\n	options - " + options + "\n\n");
		
		if (objectClass.is("__ACCOUNT__")) {
			try {
				this.handleAccounts(query, handler, options);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (objectClass.is("NaumenOU")) {
			try {
				this.handleOUs(query, handler, options);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	private void handleAccounts(NaumenFilter filter, ResultsHandler handler, OperationOptions options) throws IOException {
		
		//System.out.print("\n\nNaumen connector - перешёл в handleAccounts\n\n");
		
		String reqPath;
		JSONArray allAccounts = new JSONArray();
		JSONObject currAccount = new JSONObject();
		final StringBuilder sb = new StringBuilder();
		configuration.getAccessKey().access(chars -> sb.append(new String(chars)));
		
		if (filter != null && filter.byUid != null ) {
			reqPath = configuration.getServiceAddress() + PathsHandler.getByUUID + filter.byUid + '?' + PathsHandler.accessKeyPath + sb.toString();
			try {
				currAccount = JOReq(reqPath);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			ConnectorObject connectorObject = this.convertAccountToConnObject(currAccount);
			boolean finish = !handler.handle(connectorObject);
            if (finish) {
                return;
            }
			
		} else {
			reqPath = configuration.getServiceAddress() + PathsHandler.getAllAccs + '?' + PathsHandler.accessKeyPath + sb.toString();
			allAccounts = JAReq(reqPath);
			for (int i = 0; i < allAccounts.length(); i++) {
				currAccount = allAccounts.getJSONObject(i);
				ConnectorObject connectorObject = this.convertAccountToConnObject(currAccount);
				boolean finish = !handler.handle(connectorObject);
	            if (finish) {
	                return;
	            }
			}			
		}		
	}

	private void handleOUs(NaumenFilter filter, ResultsHandler handler, OperationOptions options) throws IOException {
		
		String reqPath;
		JSONArray allOUs = new JSONArray();
		JSONObject currOU = new JSONObject();
		final StringBuilder sb = new StringBuilder();
		configuration.getAccessKey().access(chars -> sb.append(new String(chars)));
		
		if (filter != null && filter.byUid != null ) {
			reqPath = configuration.getServiceAddress() + PathsHandler.getByUUID + filter.byUid + '?' + PathsHandler.accessKeyPath + sb.toString();
			try {
				currOU = JOReq(reqPath);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			ConnectorObject connectorObject = this.convertOUToConnObject(currOU);
			boolean finish = !handler.handle(connectorObject);
            if (finish) {
                return;
            }
			
		} else {
			reqPath = configuration.getServiceAddress() + PathsHandler.getAllOUs + '?' + PathsHandler.accessKeyPath + sb.toString();
			allOUs = JAReq(reqPath);
			for (int i = 0; i < allOUs.length(); i++) {
				currOU = allOUs.getJSONObject(i);
				ConnectorObject connectorObject = this.convertOUToConnObject(currOU);
				boolean finish = !handler.handle(connectorObject);
	            if (finish) {
	                return;
	            }
			}			
		}		
	}

	private ConnectorObject convertAccountToConnObject(JSONObject currAccount) {
		
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		
		builder.setUid(currAccount.getString(PathsHandler.PERSUUID));
		builder.setName(currAccount.getString(PathsHandler.PERSUUID));
		
		this.getStringIfExists(currAccount, PathsHandler.PERSEMAIL, builder);
		this.getStringIfExists(currAccount, PathsHandler.PERSINTERNALPHONE, builder);
		this.getStringIfExists(currAccount, PathsHandler.PERSCITYPHONE, builder);
		this.getStringIfExists(currAccount, PathsHandler.PERSPOSITION, builder);
		this.getStringIfExists(currAccount, PathsHandler.PERSHOMEPHONE, builder);
		this.getStringIfExists(currAccount, PathsHandler.PERSFIRSTNAME, builder);
		this.getStringIfExists(currAccount, PathsHandler.PERSPRIVCODE, builder);
		this.getStringIfExists(currAccount, PathsHandler.PERSLOGIN, builder);
		this.getStringIfExists(currAccount, PathsHandler.PERSMOBILEPHONE, builder);
		this.getStringIfExists(currAccount, PathsHandler.PERSNUMBER, builder);
		this.getStringIfExists(currAccount, PathsHandler.PERSMIDDLENAME, builder);
		this.getStringIfExists(currAccount, PathsHandler.PERSPASSWORD, builder);
		this.getStringIfExists(currAccount, PathsHandler.PERSALIAS, builder);
		this.getStringIfExists(currAccount, PathsHandler.PERSPHONESINDEX, builder);
		this.getStringIfExists(currAccount, PathsHandler.PERSMETACLASS, builder);
		this.getStringIfExists(currAccount, PathsHandler.PERSFULLNAME, builder);
		this.getStringIfExists(currAccount, PathsHandler.PERSLASTNAME, builder);
		this.getStringIfExists(currAccount, PathsHandler.PERSSTORAGE, builder);
		this.getStringIfExists(currAccount, PathsHandler.PERSIDHOLDER, builder);
		this.getStringIfExists(currAccount, PathsHandler.PERSEXTLINKS, builder);
		this.getStringIfExists(currAccount, PathsHandler.PERSTELEGRAMID, builder);
		this.getStringIfExists(currAccount, PathsHandler.PERSTELEGRAMLOGIN, builder);
		this.getStringIfExists(currAccount, PathsHandler.PERSEMPID, builder);
		this.getStringIfExists(currAccount, PathsHandler.PERSFLID, builder);
		this.getStringIfExists(currAccount, PathsHandler.PERSCORPPHONE, builder);
		
		this.getDateTimeIfExists(currAccount, PathsHandler.PERSREMOVALDATE, builder);
		this.getDateTimeIfExists(currAccount, PathsHandler.PERSMODIFDATE, builder);
		this.getDateTimeIfExists(currAccount, PathsHandler.PERSCREATIONDATE, builder);

		this.getDateIfExists(currAccount, PathsHandler.PERSBIRTHDATE, builder);
	
		this.getBooleanIfExists(currAccount, PathsHandler.PERSISACTIVE, builder);
		this.getBooleanIfExists(currAccount, PathsHandler.PERSISLOCKED, builder);
		this.getBooleanIfExists(currAccount, PathsHandler.PERSISPERFORMER, builder);
		this.getBooleanIfExists(currAccount, PathsHandler.PERSISREMOVED, builder);
		this.getBooleanIfExists(currAccount, PathsHandler.PERSISINTEGRATION, builder);
		this.getBooleanIfExists(currAccount, PathsHandler.PERSFORRESTPASS, builder);
		this.getBooleanIfExists(currAccount, PathsHandler.PERSNEEDGENPASS, builder);
		
		this.getLinkIfExists(currAccount, PathsHandler.PERSPARENT, builder);
		this.getLinkIfExists(currAccount, PathsHandler.PERSMANAGER, builder);
		this.getLinkIfExists(currAccount, PathsHandler.PERSICON, builder);
		
		
		//TODO Разобраться с фотографией и группами пользователя
		
		new HashMap<>();
		
//		//System.out.print("\n\n" + builder.build() + "\n\n");
        return builder.build();
	}

	private ConnectorObject convertOUToConnObject(JSONObject currOU) {
		
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		
		builder.setUid(currOU.getString(PathsHandler.OUUUID));
		builder.setName(currOU.getString(PathsHandler.OUUUID));
		
		this.getStringIfExists(currOU, PathsHandler.OUNAME, builder);
		this.getStringIfExists(currOU, PathsHandler.OUNUMBER, builder);
		this.getStringIfExists(currOU, PathsHandler.OUMETACLASS, builder);
		this.getStringIfExists(currOU, PathsHandler.OUDN, builder);
		this.getStringIfExists(currOU, PathsHandler.OUIDHOLDER, builder);
		this.getStringIfExists(currOU, PathsHandler.OUADDRESS, builder);
		
		this.getDateTimeIfExists(currOU, PathsHandler.OUREMOVDATE, builder);
		this.getDateTimeIfExists(currOU, PathsHandler.OUMODIFYDATE, builder);
		this.getDateTimeIfExists(currOU, PathsHandler.OUCREATIONDATE, builder);

		this.getBooleanIfExists(currOU, PathsHandler.OUISREMOVED, builder);

		this.getLinkIfExists(currOU, PathsHandler.OUPARENT, builder);
		this.getLinkIfExists(currOU, PathsHandler.OUMANAGER, builder);
		this.getLinkIfExists(currOU, PathsHandler.OUICON, builder);
		
		new HashMap<>();
		LOG.ok("Builder.build: {0}", builder.build());
        return builder.build();
	}

	private void getStringIfExists(JSONObject object, String attrName, ConnectorObjectBuilder builder) {
		if (object.has(attrName) && object.get(attrName) != null && !JSONObject.NULL.equals(object.get(attrName))) {
			this.addAttr(builder, attrName, object.getString(attrName));
		}
	}

	private void getBooleanIfExists(JSONObject object, String attrName, ConnectorObjectBuilder builder) {
		if (object.has(attrName) && object.get(attrName) != null && !JSONObject.NULL.equals(object.get(attrName))) {
			this.addAttr(builder, attrName, object.getBoolean(attrName));
		}
	}

	private void getDateTimeIfExists(JSONObject object, String attrName, ConnectorObjectBuilder builder) {
		if (object.has(attrName) && object.get(attrName) != null && !JSONObject.NULL.equals(object.get(attrName))) {
			String currDate = object.getString(attrName);
			String finDate = (currDate.replace(" ", "T").replace(".", "-") + ".000+03:00");
			this.addAttr(builder, attrName, finDate);
		}
	}

	private void getDateIfExists(JSONObject object, String attrName, ConnectorObjectBuilder builder) {
		if (object.has(attrName) && object.get(attrName) != null && !JSONObject.NULL.equals(object.get(attrName))) {
			String currDate = object.getString(attrName);
			String finDate = (currDate.replace(".", "-").substring(0, 10) + "T00:00:00.000+03:00");
			this.addAttr(builder, attrName, finDate);
		}
	}

	private void getLinkIfExists(JSONObject object, String attrName, ConnectorObjectBuilder builder) {
		if (object.has(attrName) && object.get(attrName) != null && !JSONObject.NULL.equals(object.get(attrName))) {
			JSONObject linksPool = object.getJSONObject(attrName); //new JSONObject(object.getString(attrName));
			String objLink = linksPool.getString("UUID");			
			this.addAttr(builder, attrName, objLink);
            }
	}

	private JSONObject JOReq(String reqPath) throws IOException {
		
		URL reqUrl = new URL(reqPath);
		String response = "";
		
		TrustManager[] trustAllCerts = new TrustManager[] {
				new X509TrustManager() {
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return new X509Certificate[0];
						}
					public void checkClientTrusted(
						java.security.cert.X509Certificate[] certs, String authType) {} 
			        public void checkServerTrusted( 
			            java.security.cert.X509Certificate[] certs, String authType) {}
			    }
		};
		
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
		
		HttpsURLConnection sslConn = (HttpsURLConnection) reqUrl.openConnection();
		sslConn.setDoInput(true);
		int responseCode=sslConn.getResponseCode();
        if (responseCode == 200) {
            String line;
            BufferedReader br=new BufferedReader(new InputStreamReader(sslConn.getInputStream()));
            while ((line=br.readLine()) != null) {
                response+=line;
            }
        }
        else {
            response="{}";
        }
        return new JSONObject(response);
	}


	private JSONArray JAReq(String reqPath) throws IOException {
		
		URL reqUrl = new URL(reqPath);
		String response = "";
		
		TrustManager[] trustAllCerts = new TrustManager[] {
				new X509TrustManager() {
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return new X509Certificate[0];
						}
					public void checkClientTrusted(
						java.security.cert.X509Certificate[] certs, String authType) {} 
			        public void checkServerTrusted( 
			            java.security.cert.X509Certificate[] certs, String authType) {}
			    }
		};
		
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
		
		HttpsURLConnection sslConn = (HttpsURLConnection) reqUrl.openConnection();
		sslConn.setDoInput(true);
		int responseCode=sslConn.getResponseCode();
        if (responseCode == 200) {
            String line;
            BufferedReader br=new BufferedReader(new InputStreamReader(sslConn.getInputStream()));
            while ((line=br.readLine()) != null) {
                response+=line;
            }
        }
        else {
            response="[]";
        }
        return new JSONArray(response);
	}
	

	
	@Override
	public Uid create(ObjectClass objectClass, Set<Attribute> createAttributes, OperationOptions options) {
		String uidValue = null;
		if (objectClass.is("__ACCOUNT__")) {
			try {
				uidValue = this.createObject(PathsHandler.createAccount, createAttributes);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (objectClass.is("NaumenOU")) {
			try {
				uidValue = this.createObject(PathsHandler.createOU, createAttributes);
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		return new Uid(uidValue);		
	}

	private String createObject(String creationType, Set<Attribute> createAttributes) throws IOException {
		final StringBuilder sb = new StringBuilder();
		configuration.getAccessKey().access(chars -> sb.append(new String(chars)));
		
		String reqPath = configuration.getServiceAddress() + creationType + "?" + PathsHandler.accessKeyPath + sb.toString();
		URL reqUrl = new URL(reqPath);
		String response = "";
		
		TrustManager[] trustAllCerts = new TrustManager[] {
				new X509TrustManager() {
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return new X509Certificate[0];
						}
					public void checkClientTrusted(
						java.security.cert.X509Certificate[] certs, String authType) {} 
			        public void checkServerTrusted( 
			            java.security.cert.X509Certificate[] certs, String authType) {}
			    }
		};
		
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
		
		HttpsURLConnection sslConn = (HttpsURLConnection) reqUrl.openConnection();
		sslConn.setRequestMethod("POST");
		sslConn.setDoInput(true);
		if(createAttributes != null && !createAttributes.isEmpty()) {
			sslConn.setDoOutput(true);
			sslConn.setRequestProperty("Content-Type", "multipart/form-data;boundary=yklmn");
			Iterator<Attribute> attrIterator = createAttributes.iterator();
			OutputStream os = sslConn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
			while (attrIterator.hasNext()) {
				Attribute attr = attrIterator.next();
				if (attr.getValue().toString().length() > 2) {
					writer.append("--yklmn").append(LINE);
					//System.out.print("\n\n\n--yklmn" + LINE);
					if (attr.getName().contains(PathsHandler.PERSBIRTHDATE)) {
						writer.append("Content-Disposition: form-data; name=\"" + attr.getName() + "\"").append(LINE);
						writer.append("Content-Type: text/plain; charset=UTF8").append(LINE);
						writer.append(LINE);
						writer.append(attr.getValue().toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("-",".").substring(0, 10)).append(LINE);
						writer.flush();
					}else {
						writer.append("Content-Disposition: form-data; name=\"" + attr.getName() + "\"").append(LINE);
						//System.out.print("Content-Disposition: form-data; name=\"" + attr.getName() + "\"" + LINE);
						writer.append("Content-Type: text/plain; charset=UTF8").append(LINE);
						//System.out.print("Content-Type: text/plain; charset=UTF8" + LINE + LINE);
						writer.append(LINE);
						writer.append(attr.getValue().toString().replaceAll("\\[", "").replaceAll("\\]", "")).append(LINE);
						//System.out.print(attr.getValue().toString().replaceAll("\\[", "").replaceAll("\\]", "") + LINE);
						writer.flush();
					}
					
				}
			}
			
			writer.append("--yklmn--");
			//System.out.print("--yklmn--\n\n\n" + sslConn.getCipherSuite() + "\n\n\n");
            writer.flush();
            writer.close();
            os.close();            

		}
		
		ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = sslConn.getInputStream().read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        String preResponse = result.toString("UTF8");
		int responseCode = sslConn.getResponseCode();
		if (responseCode == 304) {throw new ConnectorIOException("Объект не обновлен: код " + responseCode);}
		else if (responseCode == 200 || responseCode == 201) {
        	response = preResponse;
        }
        else {
            response="{}";

        }
        LOG.ok("\n\nResponse - {0}\n", response);
        JSONObject respJoBj = new JSONObject(response);
        LOG.ok("\n\nUUID созданного сотрудника - {0}\n\n", respJoBj.getString("UUID"));
        return respJoBj.getString("UUID");
	}

	@Override
	public void test() {
		//System.out.print("\n\n~~~ Запускаем тест ~~~\n\n");
		ObjectClass accountObjectClass = new ObjectClass("__ACCOUNT__");
		ResultsHandler rh = connectorObject -> {return true;};
		NaumenFilter filter = new NaumenFilter();
		filter.byUid = "employee$2324404";
		this.executeQuery(accountObjectClass, filter, rh, null);
	}

	@Override
	public FilterTranslator<NaumenFilter> createFilterTranslator(ObjectClass objectClass, OperationOptions options) {
		return new NaumenFilterTranslator();
	}

	@Override
	public void delete(ObjectClass objectClass, Uid uid, OperationOptions options) {
		
		Set<Attribute> set = new HashSet<>();
		set.add(AttributeBuilder.build("removed" , "true"));
		try {
			this.updateObject(uid, set);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public Uid update(ObjectClass objectClass, Uid uid, Set<Attribute> replaceAttributes, OperationOptions options) {
		String uidValue = null;
		try {
			uidValue = this.updateObject(uid, replaceAttributes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new Uid(uidValue);
	}
	
	
		
		private String updateObject(Uid uid, Set<Attribute> replaceAttributes) throws IOException {
			
		final StringBuilder sb = new StringBuilder();
		configuration.getAccessKey().access(chars -> sb.append(new String(chars)));
		
		String updReqPath = configuration.getServiceAddress() + PathsHandler.updateObject + uid.getUidValue() + "?"  + PathsHandler.accessKeyPath + sb.toString();
		
		URL reqUrl = new URL(updReqPath);
		String response = "";
		
		TrustManager[] trustAllCerts = new TrustManager[] {
				new X509TrustManager() {
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return new X509Certificate[0];
						}
					public void checkClientTrusted(
						java.security.cert.X509Certificate[] certs, String authType) {} 
			        public void checkServerTrusted( 
			            java.security.cert.X509Certificate[] certs, String authType) {}
			    }
		};
		
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
		
		HttpsURLConnection sslConn = (HttpsURLConnection) reqUrl.openConnection();
		sslConn.setRequestMethod("POST");
		sslConn.setDoInput(true);
		if(replaceAttributes != null && !replaceAttributes.isEmpty()) {
			sslConn.setDoOutput(true);
			sslConn.setRequestProperty("Content-Type", "multipart/form-data;boundary=yklmn");
			Iterator<Attribute> attrIterator = replaceAttributes.iterator();
			OutputStream os = sslConn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
			while (attrIterator.hasNext()) {
				Attribute attr = attrIterator.next();
				if (attr.getValue().toString().length() > 2) {
					writer.append("--yklmn").append(LINE);
					//System.out.print("\n\n\n--yklmn" + LINE);
					if (attr.getName().contains(PathsHandler.PERSBIRTHDATE)) {
						writer.append("Content-Disposition: form-data; name=\"" + attr.getName() + "\"").append(LINE);
						writer.append("Content-Type: text/plain; charset=UTF8").append(LINE);
						writer.append(LINE);
						writer.append(attr.getValue().toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("-",".").substring(0, 10)).append(LINE);
						writer.flush();
					}else {
						writer.append("Content-Disposition: form-data; name=\"" + attr.getName() + "\"").append(LINE);
						//System.out.print("Content-Disposition: form-data; name=\"" + attr.getName() + "\"" + LINE);
						writer.append("Content-Type: text/plain; charset=UTF8").append(LINE);
						//System.out.print("Content-Type: text/plain; charset=UTF8" + LINE + LINE);
						writer.append(LINE);
						writer.append(attr.getValue().toString().replaceAll("\\[", "").replaceAll("\\]", "")).append(LINE);
						//System.out.print(attr.getValue().toString().replaceAll("\\[", "").replaceAll("\\]", "") + LINE);
						writer.flush();
					}
					
				}
			}
			
			writer.append("--yklmn--");
			//System.out.print("--yklmn--\n\n\n" + sslConn.getCipherSuite() + "\n\n\n");
            writer.flush();
            writer.close();
            os.close();            

		}
		
		ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = sslConn.getInputStream().read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        String preResponse = result.toString("UTF8");
		int responseCode = sslConn.getResponseCode();
		if (responseCode == 304) {throw new ConnectorIOException("Объект не обновлен: код " + responseCode);}
		else if (responseCode == 200 || responseCode == 201) {
        	response = preResponse;
        }
        else {
            response="{}";

        }
        LOG.ok("\n\nResponse - {0}\n", response);
        JSONObject respJoBj = new JSONObject(response);
        LOG.ok("\n\nUUID созданного сотрудника - {0}\n\n", respJoBj.getString("UUID"));
        
        return respJoBj.getString("UUID");
	}

	
	
	

}
