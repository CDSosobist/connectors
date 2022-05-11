package cdsosobist.connid.connectors.mira.rest.connector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.ParseException;
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


@SuppressWarnings("unused")
@ConnectorClass(configurationClass = MiraConfiguration.class, displayNameKey = "mira.connector.display")
public class MiraConnector extends AbstractRestConnector<MiraConfiguration> implements Connector, CreateOp, UpdateOp, DeleteOp, SchemaOp, SearchOp<MiraFilter>, TestOp {
	
	
	public static final Log LOG = Log.getLog(MiraConnector.class);
	private static final String LINE = "\r\n";
	
	ZoneId timeZone = ZoneId.systemDefault();
	DateTimeFormatter forIdm = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ");
	DateTimeFormatter forMira = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
	
	private String toIdm(String dateTimeFromMira) {
		ZonedDateTime toIdm = LocalDateTime.parse(dateTimeFromMira, forMira).atZone(ZoneId.systemDefault());
		return forIdm.format(toIdm);
	}
	
	private String toMira(String dateTimeFromIdm) {
		LocalDateTime toMira = LocalDateTime.parse(dateTimeFromIdm, forIdm);
		return forMira.format(toMira);
	}
	
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
		
		AttributeInfoBuilder attrPhotoBuilder = new AttributeInfoBuilder("jpegBase64Data");
		ociBuilder.addAttributeInfo(attrPhotoBuilder.build());
		
		AttributeInfoBuilder attrBirthDateBuilder = new AttributeInfoBuilder(PathsHandler.PERSBIRTHDATE);
		ociBuilder.addAttributeInfo(attrBirthDateBuilder.build());
		
		AttributeInfoBuilder attrWorkBeginBuilder = new AttributeInfoBuilder(PathsHandler.PERSWORKBEGINDATE);
		ociBuilder.addAttributeInfo(attrWorkBeginBuilder.build());
		
		AttributeInfoBuilder attrTitleBeginBuilder = new AttributeInfoBuilder(PathsHandler.PERSTITLEBEGINDATE);
		ociBuilder.addAttributeInfo(attrTitleBeginBuilder.build());
		
		AttributeInfoBuilder attrFileIdBuilder = new AttributeInfoBuilder("fileid");
		ociBuilder.addAttributeInfo(attrFileIdBuilder.build());
		
		AttributeInfoBuilder attrPersTypeBuilder = new AttributeInfoBuilder(PathsHandler.PERSTYPE);
		ociBuilder.addAttributeInfo(attrPersTypeBuilder.build());
		
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

		AttributeInfoBuilder attrOrgDirectorPersIdBuilder = new AttributeInfoBuilder(PathsHandler.ORGDIRECTORPERSID);
		ociBuilder.addAttributeInfo(attrOrgDirectorPersIdBuilder.build());

		schemaBuilder.defineObjectClass(ociBuilder.build());
	}

	private void buildOrganizationGroupObjectClass(SchemaBuilder schemaBuilder) {
		ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
		ociBuilder.setType("OrganizationGroup");
		
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
	
	private String md5Request(String requestPath, int offset, Boolean isPost, String beanAddFields) {
		final StringBuilder sb = new StringBuilder();
		final StringBuilder sb1 = new StringBuilder();
		configuration.getAppId().access(chars -> sb.append(new String(chars)));
		configuration.getsKey().access(chars -> sb1.append(new String(chars)));
		String preRequest;
		if (isPost) {
			preRequest = configuration.getServiceAddress() + PathsHandler.MAINPATH + requestPath + "?" + PathsHandler.APPIDPATH + sb.toString();
		} else if (beanAddFields != null) {
			preRequest = configuration.getServiceAddress() + PathsHandler.MAINPATH + requestPath + "?bean_add_fields=" + beanAddFields + "&limit=200" + "&offset=" + offset + "&" + PathsHandler.APPIDPATH + sb.toString();
		} else {
			preRequest = configuration.getServiceAddress() + PathsHandler.MAINPATH + requestPath + "?limit=200" + "&offset=" + offset + "&" + PathsHandler.APPIDPATH + sb.toString();
		}
		String preHash = PathsHandler.SKEYPATH + sb1.toString();
		return preRequest + PathsHandler.SIGNPATH + pathToHash(preRequest + preHash);
	}
	
	private String md5UpdateRequest(String requestPath, String updParameters, String encodedUpdParameters) {
		final StringBuilder sb = new StringBuilder();
		final StringBuilder sb1 = new StringBuilder();
		configuration.getAppId().access(chars -> sb.append(new String(chars)));
		configuration.getsKey().access(chars -> sb1.append(new String(chars)));
		String preRequest = configuration.getServiceAddress() + PathsHandler.MAINPATH + requestPath + "?" + updParameters + "&" + PathsHandler.APPIDPATH + sb.toString();
		String encodedPreRequest = configuration.getServiceAddress() + PathsHandler.MAINPATH + requestPath + "?" + encodedUpdParameters.replaceAll("%3D", "=") + "&" + PathsHandler.APPIDPATH + sb.toString();
		String preHash = PathsHandler.SKEYPATH + sb1.toString();
		return encodedPreRequest + PathsHandler.SIGNPATH + pathToHash(preRequest + preHash);
	}
	
	private String md5FilteredRequest(String requestPath, String filterName, String filterValue) {
		final StringBuilder sb = new StringBuilder();
		final StringBuilder sb1 = new StringBuilder();
		configuration.getAppId().access(chars -> sb.append(new String(chars)));
		configuration.getsKey().access(chars -> sb1.append(new String(chars)));
		String preRequest = configuration.getServiceAddress() + PathsHandler.MAINPATH + requestPath + "?filter=" + filterName + "==" + filterValue + "&" + PathsHandler.APPIDPATH + sb.toString();
		String preHash = PathsHandler.SKEYPATH + sb1.toString();
		String request = preRequest + PathsHandler.SIGNPATH + pathToHash(preRequest + preHash);
		return request.replaceAll(filterName + "=" + filterValue, filterName + "%3D%3D" + filterValue);
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
	public void executeQuery(ObjectClass objectClass, MiraFilter query, ResultsHandler handler,
			OperationOptions options) {
		if (objectClass.is("__ACCOUNT__")) {
			try {
				this.handlePersons(query, handler, options);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} else if (objectClass.is("PersonsGroup")) {
			try {
				this.handlePersonsGroups(query, handler, options);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} else if (objectClass.is("Role")) {
			try {
				this.handleRoles(query, handler, options);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} else if (objectClass.is("Organization")) {
			try {
				this.handleOrganizations(query, handler, options);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} else if (objectClass.is("OrganizationGroup")) {
			try {
				this.handleOrganizationsGroups(query, handler, options);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
	private String contentRange(URL reqUrl) throws IOException {
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
		String contentRange = sslConn.getHeaderField("Content-Range");
		return contentRange;
	}

	private String findId (URL reqUrl, String uidValuePath) throws IOException {
		String response = "";
		String findId = null;
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
        
        if (response.length() > 2) {
        	JSONArray respJSArray = new JSONArray(response);
			findId = respJSArray.getJSONObject(0).getString(uidValuePath);
		}
        return findId;
	}

	private void handlePersons(MiraFilter filter, ResultsHandler handler, OperationOptions options) throws ParseException, IOException {
		
		String reqPath;
		JSONArray fullArray = new JSONArray();
		String beanAddFields = PathsHandler.PERSBIRTHDATE + "," + PathsHandler.PERSWORKBEGINDATE + "," + PathsHandler.PERSTITLEBEGINDATE + "," + "fileid" + "," + PathsHandler.PERSTYPE;
		
		
		if (filter != null && filter.byUid != null) {
			reqPath = PathsHandler.PATHTOPERSONS + "/" + filter.byUid;
			URL reqUrl = new URL(md5Request(reqPath, 0, false, beanAddFields));
			JSONObject person = this.trustedJsonObjRequest(reqUrl, "GET", null, null);
			String currPersUid = person.getString(PathsHandler.PERSMIRAID);
			ConnectorObject connectorObject = this.convertPersonToConnectorObject(1, person, currPersUid);
			boolean finish = !handler.handle(connectorObject);
            if (finish) {
                return;
            }
			
		} else {
			int offset = 0;
			reqPath = PathsHandler.PATHTOPERSONS;
			URL reqUrl = new URL(md5Request(reqPath, offset, false, null));
			Scanner sc = new Scanner(contentRange(reqUrl).replaceAll("[\\D]", " "));
			int i = 1;
			int firstPosition = 0;
			int fullLength = 0;
			while (sc.hasNextInt()) {
				if (i == 1) {
					firstPosition = sc.nextInt();
				} else if (i == 2) {
					sc.nextInt();
				} else if (i == 3) {
					fullLength = sc.nextInt();
				}
				i++;
				
			}
			while (firstPosition < fullLength) {
				reqUrl = new URL(md5Request(reqPath, offset, false, beanAddFields));
				String forScanner = contentRange(reqUrl).replaceAll("[\\D]", " ");
				sc = new Scanner(forScanner);
				int j = 1;
				while (sc.hasNextInt()) {
					if (j == 1) {
						firstPosition = sc.nextInt();
					} else if (j == 2) {
						sc.nextInt();
					} else if (j == 3) {
						fullLength = sc.nextInt();
					}
					j++;
				}
				JSONArray persons = this.trustedJsonArrRequest(reqUrl);
				for (int l = 0; l < persons.length(); l++) {
					fullArray.put(persons.get(l));
				}
				offset = offset + 201;
				}
			
			for (int k = 0; k < fullArray.length(); k++) {
				JSONObject person = fullArray.getJSONObject(k);
				String currPersUid = person.getString(PathsHandler.PERSMIRAID);
				ConnectorObject connectorObject = this.convertPersonToConnectorObject(k, person, currPersUid);
				boolean finish = !handler.handle(connectorObject);
				if (finish) {
					return;
					}
				}

			
			sc.close();
			}
		}
	
	private void handlePersonsGroups(MiraFilter filter, ResultsHandler handler, OperationOptions options) throws ParseException, IOException {
		String reqPath;
		JSONArray fullArray = new JSONArray();
		
		if (filter != null && filter.byUid != null) {
			reqPath = PathsHandler.PATHTOPERSGROUPS + "/" + filter.byUid;
			URL reqUrl = new URL(md5Request(reqPath, 0, false, null));
			JSONObject personGroup = this.trustedJsonObjRequest(reqUrl, "GET", null, null);
			String currPersGroupUid = personGroup.getString(PathsHandler.PERSGROUPMIRAID);
			ConnectorObject connectorObject = this.convertPersonsGroupToConnectorObject(personGroup, currPersGroupUid);
			boolean finish = !handler.handle(connectorObject);
            if (finish) {
                return;
            }
			
		} else {
			int offset = 0;
			reqPath = PathsHandler.PATHTOPERSGROUPS;
			URL reqUrl = new URL(md5Request(reqPath, offset, false, null));
			Scanner sc = new Scanner(contentRange(reqUrl).replaceAll("[\\D]", " "));
			int i = 1;
			int firstPosition = 0;
			int fullLength = 0;
			while (sc.hasNextInt()) {
				if (i == 1) {
					firstPosition = sc.nextInt();
				} else if (i == 2) {
					sc.nextInt();
				} else if (i == 3) {
					fullLength = sc.nextInt();
				}
				i++;
				
			}
			while (firstPosition < fullLength) {
				reqUrl = new URL(md5Request(reqPath, offset, false, null));
				String forScanner = contentRange(reqUrl).replaceAll("[\\D]", " ");
				sc = new Scanner(forScanner);
				int j = 1;
				while (sc.hasNextInt()) {
					if (j == 1) {
						firstPosition = sc.nextInt();
					} else if (j == 2) {
						sc.nextInt();
					} else if (j == 3) {
						fullLength = sc.nextInt();
					}
					j++;
				}
				JSONArray personGroupss = this.trustedJsonArrRequest(reqUrl);
				for (int l = 0; l < personGroupss.length(); l++) {
					fullArray.put(personGroupss.get(l));
				}
				
				offset = offset + 201;
				}
			for (int k = 0; k < fullArray.length(); k++) {
				JSONObject personGroup = fullArray.getJSONObject(k);
				String currPersGroupUid = personGroup.getString(PathsHandler.PERSGROUPMIRAID);
				ConnectorObject connectorObject = this.convertPersonsGroupToConnectorObject(personGroup, currPersGroupUid);
				boolean finish = !handler.handle(connectorObject);
				if (finish) {
					return;
					}
				}
			sc.close();
			}
		}

	private void handleRoles(MiraFilter filter, ResultsHandler handler, OperationOptions options) throws ParseException, IOException {
		String reqPath;
		JSONArray fullArray = new JSONArray();
		
		if (filter != null && filter.byUid != null) {
			reqPath = PathsHandler.PATHTOROLES + "/" + filter.byUid;
			URL reqUrl = new URL(md5Request(reqPath, 0, false, null));
			JSONObject role = this.trustedJsonObjRequest(reqUrl, "GET", null, null);
			String currRoleUid = role.getString(PathsHandler.ROLEMIRAID);
			ConnectorObject connectorObject = this.convertRoleToConnectorObject(role, currRoleUid);
			boolean finish = !handler.handle(connectorObject);
            if (finish) {
                return;
            }
			
		} else {
			int offset = 0;
			reqPath = PathsHandler.PATHTOROLES;
			URL reqUrl = new URL(md5Request(reqPath, offset, false, null));
			Scanner sc = new Scanner(contentRange(reqUrl).replaceAll("[\\D]", " "));
			int i = 1;
			int firstPosition = 0;
			int fullLength = 0;
			while (sc.hasNextInt()) {
				if (i == 1) {
					firstPosition = sc.nextInt();
				} else if (i == 2) {
					sc.nextInt();
				} else if (i == 3) {
					fullLength = sc.nextInt();
				}
				i++;
				
			}
			while (firstPosition < fullLength) {
				reqUrl = new URL(md5Request(reqPath, offset, false, null));
				String forScanner = contentRange(reqUrl).replaceAll("[\\D]", " ");
				sc = new Scanner(forScanner);
				int j = 1;
				while (sc.hasNextInt()) {
					if (j == 1) {
						firstPosition = sc.nextInt();
					} else if (j == 2) {
						sc.nextInt();
					} else if (j == 3) {
						fullLength = sc.nextInt();
					}
					j++;
				}
				JSONArray roles = this.trustedJsonArrRequest(reqUrl);
				for (int l = 0; l < roles.length(); l++) {
					fullArray.put(roles.get(l));
				}
				

				offset = offset + 201;
				}
			for (int k = 0; k < fullArray.length(); k++) {
				JSONObject role = fullArray.getJSONObject(k);
				String currRoleUid = role.getString(PathsHandler.ROLEMIRAID);
				ConnectorObject connectorObject = this.convertRoleToConnectorObject(role, currRoleUid);
				boolean finish = !handler.handle(connectorObject);
				if (finish) {
					return;
					}
				}
			sc.close();
			}
		}

	private void handleOrganizations(MiraFilter filter, ResultsHandler handler, OperationOptions options) throws ParseException, IOException {
		String reqPath;
		JSONArray fullArray = new JSONArray();
		String beanAddFields = PathsHandler.ORGDIRECTORPERSID;
		
		if (filter != null && filter.byUid != null) {
			reqPath = PathsHandler.PATHTOORGS + "/" + filter.byUid;
			URL reqUrl = new URL(md5Request(reqPath, 0, false, beanAddFields));
			JSONObject organization = this.trustedJsonObjRequest(reqUrl, "GET", null, null);
			String currOrgUid = organization.getString(PathsHandler.ORGMIRAID);
			ConnectorObject connectorObject = this.convertOrgToConnectorObject(organization, currOrgUid);
			boolean finish = !handler.handle(connectorObject);
            if (finish) {
                return;
            }
			
		} else {
			int offset = 0;
			reqPath = PathsHandler.PATHTOORGS;
			URL reqUrl = new URL(md5Request(reqPath, offset, false, null));
			Scanner sc = new Scanner(contentRange(reqUrl).replaceAll("[\\D]", " "));
			int i = 1;
			int firstPosition = 0;
			int fullLength = 0;
			while (sc.hasNextInt()) {
				if (i == 1) {
					firstPosition = sc.nextInt();
				} else if (i == 2) {
					sc.nextInt();
				} else if (i == 3) {
					fullLength = sc.nextInt();
				}
				i++;
				
			}
			while (firstPosition < fullLength) {
				reqUrl = new URL(md5Request(reqPath, offset, false, beanAddFields));
				String forScanner = contentRange(reqUrl).replaceAll("[\\D]", " ");
				sc = new Scanner(forScanner);
				int j = 1;
				while (sc.hasNextInt()) {
					if (j == 1) {
						firstPosition = sc.nextInt();
					} else if (j == 2) {
						sc.nextInt();
					} else if (j == 3) {
						fullLength = sc.nextInt();
					}
					j++;
				}
				JSONArray organizations = this.trustedJsonArrRequest(reqUrl);
				for (int l = 0; l < organizations.length(); l++) {
					fullArray.put(organizations.get(l));
				}

				offset = offset + 201;
				}
			for (int k = 0; k < fullArray.length(); k++) {
				JSONObject organization = fullArray.getJSONObject(k);
				String currOrgUid = organization.getString(PathsHandler.ORGMIRAID);
				ConnectorObject connectorObject = this.convertOrgToConnectorObject(organization, currOrgUid);
				boolean finish = !handler.handle(connectorObject);
				if (finish) {
					return;
					}
				}

			sc.close();
			}
		}

	private void handleOrganizationsGroups(MiraFilter filter, ResultsHandler handler, OperationOptions options) throws ParseException, IOException {
		String reqPath;
		JSONArray fullArray = new JSONArray();
		
		if (filter != null && filter.byUid != null) {
			reqPath = PathsHandler.PATHTOORGGROUPS + "/" + filter.byUid;
			URL reqUrl = new URL(md5Request(reqPath, 0, false, null));
			JSONObject orgsGroup = this.trustedJsonObjRequest(reqUrl, "GET", null, null);
			String currOrgsGroupUid = orgsGroup.getString(PathsHandler.ORGGROUPMIRAID);
			ConnectorObject connectorObject = this.convertOrgsGroupToConnectorObject(orgsGroup, currOrgsGroupUid);
			boolean finish = !handler.handle(connectorObject);
            if (finish) {
                return;
            }
			
		} else {
			int offset = 0;
			reqPath = PathsHandler.PATHTOORGGROUPS;
			URL reqUrl = new URL(md5Request(reqPath, offset, false, null));
			Scanner sc = new Scanner(contentRange(reqUrl).replaceAll("[\\D]", " "));
			int i = 1;
			int firstPosition = 0;
			int fullLength = 0;
			while (sc.hasNextInt()) {
				if (i == 1) {
					firstPosition = sc.nextInt();
				} else if (i == 2) {
					sc.nextInt();
				} else if (i == 3) {
					fullLength = sc.nextInt();
				}
				i++;
				
			}
			while (firstPosition < fullLength) {
				reqUrl = new URL(md5Request(reqPath, offset, false, null));
				String forScanner = contentRange(reqUrl).replaceAll("[\\D]", " ");
				sc = new Scanner(forScanner);
				int j = 1;
				while (sc.hasNextInt()) {
					if (j == 1) {
						firstPosition = sc.nextInt();
					} else if (j == 2) {
						sc.nextInt();
					} else if (j == 3) {
						fullLength = sc.nextInt();
					}
					j++;
				}
				JSONArray orgsGroups = this.trustedJsonArrRequest(reqUrl);
				for (int l = 0; l < orgsGroups.length(); l++) {
					fullArray.put(orgsGroups.get(l));
				}				
				offset = offset + 201;
				}
			for (int k = 0; k < fullArray.length(); k++) {
				JSONObject orgsGroup = fullArray.getJSONObject(k);
				String currOrgsGroupUid = orgsGroup.getString(PathsHandler.ORGGROUPMIRAID);
				ConnectorObject connectorObject = this.convertOrgsGroupToConnectorObject(orgsGroup, currOrgsGroupUid);
				boolean finish = !handler.handle(connectorObject);
				if (finish) {
					return;
					}
				}

			sc.close();
			}
		}

	private ConnectorObject convertPersonToConnectorObject(int k, JSONObject person, String currPersUid) {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setUid(currPersUid);
		builder.setName(currPersUid);
		
		this.getIfExists(person, PathsHandler.PERSLASTNAME, builder);
		this.getIfExists(person, PathsHandler.PERSFIRSTNAME, builder);
		this.getIfExists(person, PathsHandler.PERSSURNAME, builder);
		this.getIfExists(person, PathsHandler.PERSISUSER, builder);
		this.getIfExists(person, PathsHandler.PERSMIRALOGIN, builder);
		this.getIfExists(person, PathsHandler.PERSMIRAPWD, builder);
		this.getIfExists(person, PathsHandler.PERSOUID, builder);
		this.getIfExists(person, PathsHandler.PERSOUNAME, builder);
		this.getIfExists(person, PathsHandler.PERSTITLEID, builder);
		this.getIfExists(person, PathsHandler.PERSTITLENAME, builder);
		this.getIfExists(person, PathsHandler.PERSSEX, builder);
		this.getIfExists(person, PathsHandler.PERSMAIL, builder);
		this.getIfExists(person, PathsHandler.PERSSTATUS, builder);
		this.getIfExists(person, PathsHandler.PERSEXTID, builder);

		if (person.has(PathsHandler.PERSBIRTHDATE) && person.get(PathsHandler.PERSBIRTHDATE) != null && !JSONObject.NULL.equals(person.get(PathsHandler.PERSBIRTHDATE))) {
			this.addAttr(builder, PathsHandler.PERSBIRTHDATE, toIdm(person.getString(PathsHandler.PERSBIRTHDATE)));
		}
		
		if (person.has(PathsHandler.PERSWORKBEGINDATE) && person.get(PathsHandler.PERSWORKBEGINDATE) != null && !JSONObject.NULL.equals(person.get(PathsHandler.PERSWORKBEGINDATE))) {
			this.addAttr(builder, PathsHandler.PERSWORKBEGINDATE, toIdm(person.getString(PathsHandler.PERSWORKBEGINDATE)));
		}
		
		if (person.has(PathsHandler.PERSTITLEBEGINDATE) && person.get(PathsHandler.PERSTITLEBEGINDATE) != null && !JSONObject.NULL.equals(person.get(PathsHandler.PERSTITLEBEGINDATE))) {
			this.addAttr(builder, PathsHandler.PERSTITLEBEGINDATE, toIdm(person.getString(PathsHandler.PERSTITLEBEGINDATE)));
		}
		
		this.getIfExists(person, "fileid", builder);
		this.getIfExists(person, PathsHandler.PERSTYPE, builder);	
		
		new HashMap<>();
        LOG.ok("Build {0} - {1}", k, builder.build());
        return builder.build();

	}

	private ConnectorObject convertPersonsGroupToConnectorObject(JSONObject personGroup, String currPersGroupUid) {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setUid(currPersGroupUid);
		builder.setName(currPersGroupUid);
		
		this.getIfExists(personGroup, PathsHandler.PATHTOPERSGROUPS, builder);
		this.getIfExists(personGroup, PathsHandler.PERSGROUPNAME, builder);
		this.getIfExists(personGroup, PathsHandler.PERSGROUPDESCR, builder);
		this.getIfExists(personGroup, PathsHandler.PERSGROUPPARENTID, builder);
		this.getIfExists(personGroup, PathsHandler.PERSGROUPPARENTNAME, builder);
		this.getIfExists(personGroup, PathsHandler.PERSGROUPKIND, builder);
		
		new HashMap<>();
		LOG.ok("Builder.build: {0}", builder.build());
        return builder.build();
	}

	private ConnectorObject convertRoleToConnectorObject(JSONObject role, String currRoleUid) {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setUid(currRoleUid);
		builder.setName(currRoleUid);
		
		this.getIfExists(role, PathsHandler.ROLEPROFILEID, builder);
		this.getIfExists(role, PathsHandler.ROLENAME, builder);
		this.getIfExists(role, PathsHandler.SYSROLENAME, builder);
		this.getIfExists(role, PathsHandler.ROLEISDEFAULT, builder);
		
		new HashMap<>();
//		LOG.ok("Builder.build: {0}", builder.build());
        return builder.build();
	}

	private ConnectorObject convertOrgToConnectorObject(JSONObject organization, String currOrgUid) {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setUid(currOrgUid);
		builder.setName(currOrgUid);
		
		this.getIfExists(organization, PathsHandler.ORGNAME, builder);
		this.getIfExists(organization, PathsHandler.ORGPARENTID, builder);
		this.getIfExists(organization, PathsHandler.ORGSHORTNAME, builder);
		this.getIfExists(organization, PathsHandler.ORGEXTID, builder);
		this.getIfExists(organization, PathsHandler.ORGDIRECTORPERSID, builder);

		new HashMap<>();
		LOG.ok("Builder.build: {0}", builder.build());
        return builder.build();
	}

	private ConnectorObject convertOrgsGroupToConnectorObject(JSONObject orgsGroup, String currOrgsGroupUid) {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setUid(currOrgsGroupUid);
		builder.setName(currOrgsGroupUid);
		
		this.getIfExists(orgsGroup, PathsHandler.ORGGROUPNAME, builder);
		this.getIfExists(orgsGroup, PathsHandler.ORGGROUPDESCR, builder);
		this.getIfExists(orgsGroup, PathsHandler.ORGGROUPPARENTID, builder);
		this.getIfExists(orgsGroup, PathsHandler.ORGGROUPPARENTNAME, builder);
		this.getIfExists(orgsGroup, PathsHandler.ORGGROUPKIND, builder);

		new HashMap<>();
//		LOG.ok("Builder.build: {0}", builder.build());
        return builder.build();
	}

	private void getIfExists(JSONObject object, String attrName, ConnectorObjectBuilder builder) {
		if (object.has(attrName) && object.get(attrName) != null && !JSONObject.NULL.equals(object.get(attrName))) {
			if (object.get(attrName) instanceof String) {
                this.addAttr(builder, attrName, object.getString(attrName));
            } else if (object.get(attrName) instanceof Boolean) {
                this.addAttr(builder, attrName, object.getBoolean(attrName));
            }
		}
		
	}

	@Override
	public Uid create(ObjectClass objectClass, Set<Attribute> createAttributes, OperationOptions options) {
		String uidValue = null;
		
		if (objectClass.is("__ACCOUNT__")) {
			String codeFrom1c = this.getStringAttr(createAttributes, PathsHandler.PERSEXTID);
			LOG.ok("\n\nЗаходим в создание физлица\n");
			try {
				uidValue = this.createObject(PathsHandler.PATHTOPERSONS, createAttributes, PathsHandler.PERSMIRAID, PathsHandler.PERSEXTID, codeFrom1c);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (objectClass.is("PersonsGroup")) {
			try {
				uidValue = this.createObject(PathsHandler.PATHTOPERSGROUPS, createAttributes, PathsHandler.PERSGROUPMIRAID, null, null);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else if (objectClass.is("Role")) {
			try {
				uidValue = this.createObject(PathsHandler.PATHTOROLES, createAttributes, PathsHandler.ROLEMIRAID, null, null);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else if (objectClass.is("Organization")) {
			String codeFrom1c = this.getStringAttr(createAttributes, PathsHandler.ORGEXTID);
			try {
				uidValue = this.createObject(PathsHandler.PATHTOORGS, createAttributes, PathsHandler.ORGMIRAID, PathsHandler.ORGEXTID, codeFrom1c);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else if (objectClass.is("OrganizationGroup")) {
			try {
				uidValue = this.createObject(PathsHandler.PATHTOORGGROUPS, createAttributes, PathsHandler.ORGGROUPMIRAID, null, null);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		return new Uid(uidValue);
	}

	@Override
	public Uid update(ObjectClass objectClass, Uid uid, Set<Attribute> replaceAttributes, OperationOptions options) {
		String uidValue = null;
		
		if (objectClass.is("__ACCOUNT__")) {
			String codeFrom1c = this.getStringAttr(replaceAttributes, PathsHandler.PERSEXTID);
			try {
				uidValue = this.updateObject(uid, PathsHandler.PATHTOPERSONS, replaceAttributes, PathsHandler.PERSMIRAID, codeFrom1c);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (objectClass.is("PersonsGroup")) {
			try {
				uidValue = this.updateObject(uid, PathsHandler.PATHTOPERSGROUPS, replaceAttributes, PathsHandler.PERSGROUPMIRAID, null);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else if (objectClass.is("Role")) {
			try {
				uidValue = this.updateObject(uid, PathsHandler.PATHTOROLES, replaceAttributes, PathsHandler.ROLEMIRAID, null);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else if (objectClass.is("Organization")) {
			try {
				uidValue = this.updateObject(uid, PathsHandler.PATHTOORGS, replaceAttributes, PathsHandler.ORGMIRAID, null);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else if (objectClass.is("OrganizationGroup")) {
			try {
				uidValue = this.updateObject(uid, PathsHandler.PATHTOORGGROUPS, replaceAttributes, PathsHandler.ORGGROUPMIRAID, null);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		return new Uid(uidValue);
	}

	@Override
	public void delete(ObjectClass objectClass, Uid uid, OperationOptions options) {
//		LOG.ok("\n\nКласс объекта - {0}\n", objectClass);
		if (objectClass.is("__ACCOUNT__")) {
			try {
				this.deleteObject(uid, PathsHandler.PATHTOPERSONS);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (objectClass.is("PersonsGroup")) {
			try {
				this.deleteObject(uid, PathsHandler.PATHTOPERSGROUPS);
			} catch (IOException e) {
				e.printStackTrace();
			}			
		} else if (objectClass.is("Role")) {
			try {
				this.deleteObject(uid, PathsHandler.PATHTOROLES);
			} catch (IOException e) {
				e.printStackTrace();
			}			
		} else if (objectClass.is("Organization")) {
			try {
				this.deleteObject(uid, PathsHandler.PATHTOORGS);
			} catch (IOException e) {
				e.printStackTrace();
			}			
		} else if (objectClass.is("OrganizationGroup")) {
			try {
				this.deleteObject(uid, PathsHandler.PATHTOORGGROUPS);
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}		
	}

	private JSONObject trustedJsonObjRequest(URL reqUrl, String method, Set<Attribute> attributes, String guidFrom1c) throws IOException {
		String response = "";
		String preResponse = "";
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
		sslConn.setRequestMethod(method);
		sslConn.setDoInput(true);
		if(attributes != null && !attributes.isEmpty()) {
			sslConn.setDoOutput(true);
			sslConn.setRequestProperty("Content-Type", "multipart/form-data;boundary=yklmn");
			Iterator<Attribute> attrIterator = attributes.iterator();
			OutputStream os = sslConn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
			while (attrIterator.hasNext()) {
				Attribute attr = attrIterator.next();
				if (attr.getValue().toString().length() > 2) {
					writer.append("--yklmn").append(LINE);
					if (attr.getName().contains("jpegBase64Data")) {
						writer.append("Content-Disposition: form-data; name=\"fileid\"; filename=\"" + guidFrom1c + ".jpg\"").append(LINE);
						writer.append("Content-Type: image/jpeg").append(LINE);
						writer.append("Content-Transfer-Encoding: binary").append(LINE);
						writer.append(LINE);
						String encodedPhoto = attr.getValue().toString().replaceAll("\\[", "").replaceAll("\\]", "");
						byte[] decodedPhoto = Base64.getDecoder().decode(encodedPhoto);
				        writer.flush();
				        try {
				        	os.write(decodedPhoto);
				        	} catch (Exception e) {
				        		System.out.println(e.getMessage());
						}
						writer.append(LINE);
						writer.flush();
					} else if (attr.getName().contains(PathsHandler.PERSBIRTHDATE) || attr.getName().contains(PathsHandler.PERSWORKBEGINDATE) || attr.getName().contains(PathsHandler.PERSTITLEBEGINDATE)) {
						writer.append("Content-Disposition: form-data; name=\"" + attr.getName() + "\"").append(LINE);
						writer.append("Content-Type: text/plain; charset=UTF8").append(LINE);
						writer.append(LINE);
						writer.append(toMira(attr.getValue().toString().replaceAll("\\[", "").replaceAll("\\]", ""))).append(LINE);
						writer.flush();
					}else {
						writer.append("Content-Disposition: form-data; name=\"" + attr.getName() + "\"").append(LINE);
						writer.append("Content-Type: text/plain; charset=UTF8").append(LINE);
						writer.append(LINE);
						writer.append(attr.getValue().toString().replaceAll("\\[", "").replaceAll("\\]", "")).append(LINE);
						writer.flush();
					}
					
				}
			}
			
			writer.append("--yklmn--");
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
        preResponse = result.toString("UTF8");
		int responseCode = sslConn.getResponseCode();
		if (responseCode == 304) {throw new ConnectorIOException("Объект не обновлен: код " + responseCode);}
		else if (responseCode == 200 || responseCode == 201) {
        	response = preResponse;
        }
        else {
            response="{}";

        }
		
		if (method.contains("DELETE")) {
			response="{}";
		}
//        LOG.ok("\n\nResponse - {0}\n", response);
        JSONObject respJoBj = new JSONObject(response);
//        LOG.ok("\n\nrespJoBj - {0}\n", respJoBj);
        return respJoBj;
}

	private JSONArray trustedJsonArrRequest(URL reqUrl) throws IOException {
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
		sslConn.setRequestMethod("GET");
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
        JSONArray respJArr = new JSONArray(response);
        return respJArr;
}

	private String createObject(String pathToModule, Set<Attribute> attributes, String uidValuePath, String filterName, String codeFrom1c) throws IOException {
		String uidValue = null;
		
		if (codeFrom1c != null) {
			URL requestUrl = new URL(md5FilteredRequest(pathToModule, filterName, codeFrom1c));
			String findId = findId(requestUrl, uidValuePath);
			if (findId != null) {
				uidValue = findId;
			} else {
				JSONObject result = trustedJsonObjRequest(requestUrl, "POST", attributes, codeFrom1c);
				uidValue = result.getString(uidValuePath);				
			}
		} else if (attributes != null && !attributes.isEmpty()) {
			URL requestUrl = new URL(md5Request(pathToModule, 0, true, null));
			JSONObject result = trustedJsonObjRequest(requestUrl, "POST", attributes, null);
			uidValue = result.getString(uidValuePath);
		}
		
//		LOG.ok("Uid - {0}", uidValue);

		return uidValue;
	}
		
	private String updateObject(Uid uid, String pathtoModule, Set<Attribute> replaceAttributes, String objectId, String codeFrom1c) throws IOException {
		if (replaceAttributes != null && !replaceAttributes.isEmpty()) {
			String uidValue = null;
			String reqPath = pathtoModule + "/" + uid.getUidValue();
			StringBuilder attrReqBuilder = new StringBuilder();
			StringBuilder encodedAttrReqBuilder = new StringBuilder();
			Set<String> treeSetAttr = new TreeSet<>();
			Iterator<Attribute> attrIterator = replaceAttributes.iterator();
			while (attrIterator.hasNext()) {
				Attribute attr = (Attribute) attrIterator.next();
				if(!attr.getValue().isEmpty() && attr.getValue() != null && attr.getValue().toString().length() > 2) {
					String paramValue = null;
					if (attr.getName().contains(PathsHandler.PERSBIRTHDATE) || attr.getName().contains(PathsHandler.PERSWORKBEGINDATE) || attr.getName().contains(PathsHandler.PERSTITLEBEGINDATE)) {
						paramValue = toMira(attr.getValue().toString().replaceAll("\\[", "").replaceAll("\\]", ""));
						treeSetAttr.add(attr.getName() + "=" + paramValue);
					} else if (attr.getName().contains("jpegBase64Data")) {
						String beanAddFields = "fileid";
						Set<Attribute> setForPhoto = new HashSet<>();
						setForPhoto.add(AttributeBuilder.build("pfirstname", codeFrom1c));
						setForPhoto.add(AttributeBuilder.build("jpegBase64Data", attr.getValue().toString().replaceAll("\\[", "").replaceAll("\\]", "")));
//						URL requestUrl = new URL(md5FilteredRequest(PathsHandler.PATHTOPERSONS, PathsHandler.PERSEXTID, codeFrom1c));
						URL requestUrl = new URL(md5Request(PathsHandler.PATHTOPERSONS, 0, false, beanAddFields));
						System.out.println("\n\nRequest URL: " + requestUrl + "\n");
						JSONObject result = trustedJsonObjRequest(requestUrl, "POST", setForPhoto, codeFrom1c);
						System.out.println("JSONObject result : " + result);
						Uid tempUserUidValue = new Uid(result.getString(PathsHandler.PERSMIRAID));
						String photoUidValue = result.getString("fileid");
						deleteObject(tempUserUidValue, PathsHandler.PATHTOPERSONS);
						treeSetAttr.add("fileid=" + photoUidValue);
					} else {
						paramValue = attr.getValue().toString().replaceAll("\\[", "").replaceAll("\\]", "");
						treeSetAttr.add(attr.getName() + "=" + paramValue);
					}
					
				}
			}
			Iterator<String> treeIterator = treeSetAttr.iterator();
			while (treeIterator.hasNext()) {
				String currIterator = treeIterator.next();
				attrReqBuilder.append(currIterator);
				encodedAttrReqBuilder.append(URLEncoder.encode(currIterator, StandardCharsets.UTF_8.toString()));
				if(treeIterator.hasNext()) {
					attrReqBuilder.append("&");
					encodedAttrReqBuilder.append("&");}
			}
			String reqUrlWithoutEncoding = md5UpdateRequest(reqPath, attrReqBuilder.toString(), encodedAttrReqBuilder.toString());
			LOG.ok("\n\nСтрока запроса - {0}\n\n", reqUrlWithoutEncoding);
			URL reqUrl = new URL(reqUrlWithoutEncoding);
			JSONObject result = trustedJsonObjRequest(reqUrl, "PUT", null, codeFrom1c);
			uidValue = result.getString(objectId);
			
//			LOG.ok("Uid - {0}", uidValue);
			
		return uidValue;
		
		} else {return null;}
	}

	private void deleteObject(Uid uid, String pathtoModule) throws IOException {
		String reqPath = pathtoModule + "/" + uid.getUidValue();
		URL reqUrl = new URL(md5Request(reqPath, 0, false, null));
		
		trustedJsonObjRequest(reqUrl, "DELETE", null, null);
	}

	@Override
	public void dispose() {

	}

	@Override
	public FilterTranslator<MiraFilter> createFilterTranslator(ObjectClass objectClass, OperationOptions options) {
		return new miraFilterTranslator();
	}

	@Override
	public void test() {
		String reqPath = PathsHandler.PATHTOPERSONS + "/3";
		URL reqUrl = null;
		try {
			reqUrl = new URL(md5Request(reqPath, 0, false, null));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		try {
			@SuppressWarnings("unused")
			JSONObject person = this.trustedJsonObjRequest(reqUrl, "GET", null, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
