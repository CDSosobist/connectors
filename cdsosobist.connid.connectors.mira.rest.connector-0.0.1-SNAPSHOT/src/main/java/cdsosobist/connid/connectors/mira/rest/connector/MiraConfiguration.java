package cdsosobist.connid.connectors.mira.rest.connector;

import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.security.GuardedString;

import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;

import com.evolveum.polygon.rest.AbstractRestConfiguration;

@SuppressWarnings("unused")
public class MiraConfiguration extends AbstractRestConfiguration {
	
	private String serviceAddress = "https://portaldev.cds.spb.ru";
	private GuardedString appId = new GuardedString("system".toCharArray());
	private GuardedString sKey = new GuardedString("d^1uC8M!".toCharArray());
	//TODO Убрать тестовые значения
	
	public String getServiceAddress() {return serviceAddress;}
	public void setServiceAddress(String serviceAddress) {this.serviceAddress = serviceAddress;}
	
	public GuardedString getAppId() {return appId;}
	public void setAppId(GuardedString appId) {this.appId = appId;}
	
	public GuardedString getsKey() {return sKey;}
	public void setsKey(GuardedString sKey) {this.sKey = sKey;}
	
	@Override
	public void validate() {
		if (StringUtil.isBlank(serviceAddress)) {
			throw new ConfigurationException("Сервисный адрес не может быть пустым!");
		}
	}

}
