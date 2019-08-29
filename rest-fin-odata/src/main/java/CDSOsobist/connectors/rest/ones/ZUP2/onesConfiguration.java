/*
 * Copyright (c) 2010-2014 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package CDSOsobist.connectors.rest.ones.ZUP2;

import com.evolveum.polygon.rest.AbstractRestConfiguration;
import org.apache.olingo.client.core.http.BasicAuthHttpClientFactory;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.spi.ConfigurationProperty;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class onesConfiguration extends AbstractRestConfiguration {
    private String username;
    private String serviceAddress;
    private GuardedString password;
    private Boolean trustAllCertificates;
    private Boolean skipTestConnection = false;


    public onesConfiguration() {}
    
    
    public String getUsername() {return this.username;}
    public void setUsername(String username) {this.username = username;}
    
    public String getServiceAddress() {return this.serviceAddress;}
    public void setServiceAddress(String serviceAddress) {this.serviceAddress = serviceAddress;}

    public GuardedString getPassword() {return password;}
    public void setPassword(GuardedString password) {this.password = password;}

    public Boolean getTrustAllCertificates() {return trustAllCertificates;}
    public void setTrustAllCertificates(Boolean trustAllCertificates) {this.trustAllCertificates = trustAllCertificates;}


    BasicAuthHttpClientFactory getYauth() {
        String password = "u5xXY3-WU";
        return new BasicAuthHttpClientFactory(username, password);
    }

    @SuppressWarnings("unused")
    private LinkedHashMap<String, Map<String, String>> parseEntities(String[] entities) {
        LinkedHashMap<String, Map<String, String>> entityMetadatas = new LinkedHashMap<>();
        if (entities == null || entities.length == 1 && StringUtil.isEmpty(entities[0])) {
            return entityMetadatas;
        } else {
            int var4 = entities.length;

            for (String tableDef : entities) {
                if (!StringUtil.isEmpty(tableDef)) {
                    Map<String, String> fields = new HashMap<>();
                    String[] entity = tableDef.split("=");
                    String entityName = entity[0];
                    String[] fieldsMetaDatas;
                    if (entityName.contains(":")) {
                        fieldsMetaDatas = entityName.split(":");
                        if (fieldsMetaDatas.length != 2) {
                            throw new ConfigurationException("please use correct vocabulary schema definition, for example: 'positions:2' ({vocabulary_org_name}:{vocabulary ID}), got: " + entityName);
                        }

                        entityName = fieldsMetaDatas[0];
                        String vid = fieldsMetaDatas[1];
                    }

                    if (entity.length == 1) {
                        entityMetadatas.put(entityName, fields);
                    } else {
                        if (entity.length != 2) {
                            throw new ConfigurationException("please use correct schema definition, for example: 'department=field_department:value,field_image:fid', got: " + tableDef);
                        }
                    }
                }
            }
            return entityMetadatas;
        }
    }

    @ConfigurationProperty(
            displayMessageKey = "y.config.skipTestConnection",
            helpMessageKey = "y.config.skipTestConnection.help"
    )

    public boolean getSkipTestConnection() {return this.skipTestConnection;}
}