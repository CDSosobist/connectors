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

package cdsosobist.connectors.rest;

import org.identityconnectors.common.security.GuardedString;

import com.evolveum.polygon.rest.AbstractRestConfiguration;

public class zup3Configuration extends AbstractRestConfiguration {

    private String userName;
    private String serviceAddress;
    private GuardedString password;
    private Boolean skipTestConnection = false;


    public zup3Configuration() {}

    public String getUserName() {return this.userName;}
    public void setUserName(String userName) {this.userName = userName;}

    public String getServiceAddress() {return serviceAddress;}
    public void setServiceAddress(String serviceAddress) {this.serviceAddress = serviceAddress;}

    public GuardedString getPassword() {return password;}
    public void setPassword(GuardedString password) {this.password = password;}

    public Boolean getSkipTestConnection() {return skipTestConnection;}
    public void setSkipTestConnection(Boolean skipTestConnection) {this.skipTestConnection = skipTestConnection;}
}