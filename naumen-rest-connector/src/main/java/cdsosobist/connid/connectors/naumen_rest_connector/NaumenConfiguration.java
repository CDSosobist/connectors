package cdsosobist.connid.connectors.naumen_rest_connector;

import org.identityconnectors.common.security.GuardedString;

import com.evolveum.polygon.rest.AbstractRestConfiguration;

public class NaumenConfiguration extends AbstractRestConfiguration {
	
	private String serviceAddress = "https://sd.cds.spb.ru/sd";
	public String getServiceAddress() {return serviceAddress;}
	public void setServiceAddress(String serviceAddress) {this.serviceAddress = serviceAddress;}
	
	private GuardedString accessKey = new GuardedString("0049e228-358d-4ebd-ae8b-cc1965173a8c".toCharArray());
	public GuardedString getAccessKey() {return accessKey;}
	public void setAccessKey(GuardedString accessKey) {this.accessKey = accessKey;}
	
	
	@Override
	public void validate() {
		// TODO Auto-generated method stub
		
	}

}
