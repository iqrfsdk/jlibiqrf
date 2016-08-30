/*
 * Copyright 2016 MICRORISC s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microrisc.jlibiqrf.bridge.config;

import com.microrisc.jlibiqrf.bridge.ArgumentChecker;
import java.util.Random;
import java.util.UUID;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Encapsulates configuration of MQTT.
 * 
 * @author Martin Strouhal
 */
@XmlType( name = "MQTT_config")
public final class MQTTConfiguration {

    @XmlElement(name = "Protocol", defaultValue = "tcp")
    private final String protocol;
    @XmlElement(name = "Broker")
    private final String broker;
    @XmlElement(name = "Port", defaultValue = "1883")
    private final long port;
    @XmlElement(name = "Client_id")
    private final String clientId;
    @XmlElement(name = "Clean_session", defaultValue = "false")
    private final boolean cleanSession;
    @XmlElement(name = "Quite_mode", defaultValue = "false")
    private final boolean quiteMode;
    @XmlElement(name = "SSL", defaultValue = "false")
    private final boolean ssl;
    @XmlElement(name = "Certificate_file_path")
    private final String certFilePath;
    @XmlElement(name = "Username")
    private final String userName;
    @XmlElement(name = "Password")
    private final String password;
    @XmlElement(name = "Topic_prefix")
    private final String topicPrefix;

    /** For JAXB purpose only */
    private MQTTConfiguration(){
        protocol = broker = clientId = certFilePath = userName = password = null;
        topicPrefix = "";
        port = 0;
        cleanSession = quiteMode = ssl = false;
    }
    
    /** For ConfigurationBuilder only */
    private MQTTConfiguration(ConfigurationBuilder builder) {
        this.protocol = builder.protocol;
        this.broker = builder.broker;
        this.port = builder.port;
        this.clientId = builder.clientId;
        this.cleanSession = builder.cleanSession;
        this.quiteMode = builder.quiteMode;
        this.ssl = builder.ssl;
        this.certFilePath = builder.certFilePath;
        this.userName = builder.userName;
        this.password = builder.password;
        this.topicPrefix = builder.topicPrefix;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getBroker() {
        return broker;
    }

    public long getPort() {
        return port;
    }

    public String getClientId() {
        return clientId;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public boolean isQuiteMode() {
        return quiteMode;
    }

    public boolean isSSL() {
        return ssl;
    }

    public String getCertFilePath() {
        if(!ssl){
            throw new IllegalStateException("SSL isn't configured and it's disabled!");
        }
        return certFilePath;
    }

    public String getUserName() {
        if(!ssl){
            throw new IllegalStateException("SSL isn't configured and it's disabled!");
        }
        return userName;
    }

    public String getPassword() {
        if(!ssl){
            throw new IllegalStateException("SSL isn't configured and it's disabled!");
        }
        return password;
    }

    public String getTopicPrefix() {
        return topicPrefix + ((topicPrefix == "" || topicPrefix == null ) ? "" : '/');
    }
    
    public String getCompleteAddress(){
        return protocol + "://" + broker + ":" + port;
    }

    @Override
    public String toString() {
        return "MQTTConfiguration{" + "broker=" + getCompleteAddress() + ", clientId=" + clientId + ", cleanSession=" + cleanSession + ", quiteMode=" + quiteMode + ", ssl=" + ssl + ", certFilePath=" + certFilePath + ", userName=" + userName + ", password=" + password + ", topicPrefix=" + topicPrefix + '}';
    }
    
    public static class ConfigurationBuilder {

        private final int DEFAULT_PORT = 1883;
        private final String DEFAULT_PROTOCOL = "tcp";
        private final boolean DEFAULT_SSL = false;
        private final boolean DEFAULT_CLEAN_SESSION = true;
        private final boolean DEFAULT_QUITE_MODE = false;
        private final String DEFAULT_TOPIC_PREFIX = "";
        
        private String protocol = DEFAULT_PROTOCOL;
        private final String broker;
        private long port = DEFAULT_PORT;
        private String clientId;
        private boolean cleanSession = DEFAULT_CLEAN_SESSION;
        private boolean quiteMode = DEFAULT_QUITE_MODE;
        private boolean ssl = DEFAULT_SSL;
        private String certFilePath;
        private String userName;
        private String password;
        private String topicPrefix = DEFAULT_TOPIC_PREFIX;
        
        public ConfigurationBuilder(String broker){
            ArgumentChecker.checkNull(broker);
            this.broker = broker;
            Random r = new Random();
            this.clientId = UUID.randomUUID().toString().replace("-", "");
        }
        
        
        public ConfigurationBuilder clientId(String clientId){
            ArgumentChecker.checkNull(clientId);
            this.clientId = clientId;
            return this;
        }
        
        public ConfigurationBuilder protocol(String protocol){
            ArgumentChecker.checkNull(protocol);
            this.protocol = protocol;
            return this;
        }
        
        public ConfigurationBuilder port(int port){
            ArgumentChecker.checkNegative(port);
            this.port = port;
            return this;
        }
        
        public ConfigurationBuilder cleanSession(boolean cleanSession){
            this.cleanSession = cleanSession;
            return this;
        }
        
        public ConfigurationBuilder quiteMode(boolean quiteMode){
            this.quiteMode = quiteMode;
            return this;
        }
        
        public ConfigurationBuilder ssl(String certFilePath, String username, 
                String password)
        {
            ArgumentChecker.checkNull(certFilePath);
            ArgumentChecker.checkNull(username);
            ArgumentChecker.checkNull(password);
            this.ssl = true;
            this.certFilePath = certFilePath;
            this.userName = username;
            this.password = password;
            return this;
        }
        
        public ConfigurationBuilder topicPrefix(String topicPrefix){
            this.topicPrefix = topicPrefix;
            return this;
        }
        
        public MQTTConfiguration build(){
            return new MQTTConfiguration(this);
        }
    }
}