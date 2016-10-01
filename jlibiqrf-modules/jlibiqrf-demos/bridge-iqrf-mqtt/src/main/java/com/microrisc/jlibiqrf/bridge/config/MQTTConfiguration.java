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
 * The configuration can be loaded:
 *  - from xml file via {@link BridgeConfigurationLoader} together with {@link BridgeConfiguration}
 *  - can be built in code via {@link ConfigurationBuilder}
 * 
 * Note: default value is defined in annotation, but primary is pulled from 
 * {@link ConfigurationBuilder}. In annotations is for 
 * information purpose.
 * 
 * @author Martin Strouhal
 */
@XmlType( name = "MQTT_config")
public final class MQTTConfiguration {

    // confiugration items are described on github in documentation (https://github.com/iqrfsdk/jlibiqrf/wiki/Bridge-iqrf-mqtt-%28user-manual%29#bridge-configuration-items)
    // or below in configuration builder
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

    /** Getter for {@link MQTTConfiguration#protocol}
     * @return {@link MQTTConfiguration#protocol}
     */
    public String getProtocol() {
        return protocol;
    }

    /** Getter for {@link MQTTConfiguration#broker}
     * @return {@link MQTTConfiguration#broker}
     */
    public String getBroker() {
        return broker;
    }

    /** Getter for {@link MQTTConfiguration#port}
     * @return {@link MQTTConfiguration#port}
     */
    public long getPort() {
        return port;
    }

    /** Getter for {@link MQTTConfiguration#clientId}
     * @return {@link MQTTConfiguration#clientId}
     */
    public String getClientId() {
        return clientId;
    }

    /** Getter for {@link MQTTConfiguration#cleanSession}
     * @return {@link MQTTConfiguration#cleanSession}
     */
    public boolean isCleanSession() {
        return cleanSession;
    }

    /** Getter for {@link MQTTConfiguration#quiteMode}
     * @return {@link MQTTConfiguration#quiteMode}
     */
    public boolean isQuiteMode() {
        return quiteMode;
    }

    /** Getter for {@link MQTTConfiguration#ssl}
     * @return {@link MQTTConfiguration#ssl}
     */
    public boolean isSSL() {
        return ssl;
    }

    /** Getter for {@link MQTTConfiguration#certFilePath}
     * @return {@link MQTTConfiguration#certFilePath}
     * @throws IllegalStateException if ssl isn't enabled
     */
    public String getCertFilePath() {
        if(!ssl){
            throw new IllegalStateException("SSL isn't configured and it's disabled!");
        }
        return certFilePath;
    }

    /** Getter for {@link MQTTConfiguration#userName}
     * @return {@link MQTTConfiguration#userName}
     * @throws IllegalStateException if ssl isn't enabled
     */
    public String getUserName() {
        if(!ssl){
            throw new IllegalStateException("SSL isn't configured and it's disabled!");
        }
        return userName;
    }

    /** Getter for {@link MQTTConfiguration#password}
     * @return {@link MQTTConfiguration#password}
     * @throws IllegalStateException if ssl isn't enabled
     */
    public String getPassword() {
        if(!ssl){
            throw new IllegalStateException("SSL isn't configured and it's disabled!");
        }
        return password;
    }

    /** Getter for {@link MQTTConfiguration#topicPrefix}
     * @return {@link MQTTConfiguration#topicPrefix}
     */
    public String getTopicPrefix() {
        return topicPrefix + ((topicPrefix == "" || topicPrefix == null ) ? "" : '/');
    }
    
    /**
     * Returns complete address of mqtt broker with protocol and port as 
     * complete URL.
     * @return String
     */
    public String getCompleteAddress(){
        return protocol + "://" + broker + ":" + port;
    }

    @Override
    public String toString() {
        return "MQTTConfiguration{" + "broker=" + getCompleteAddress() + ", clientId=" + clientId + ", cleanSession=" + cleanSession + ", quiteMode=" + quiteMode + ", ssl=" + ssl + ", certFilePath=" + certFilePath + ", userName=" + userName + ", password=" + password + ", topicPrefix=" + topicPrefix + '}';
    }
    
    /**
     * Provides interface for dynamic building of configuration in code.
     * See online documentation of configuration too: https://github.com/iqrfsdk/jlibiqrf/wiki/Bridge-iqrf-mqtt-%28user-manual%29#bridge-configuration-items
     */
    public static class ConfigurationBuilder {

        // definition of default values
        private final int DEFAULT_PORT = 1883;
        private final String DEFAULT_PROTOCOL = "tcp";
        private final boolean DEFAULT_SSL = false;
        private final boolean DEFAULT_CLEAN_SESSION = true;
        private final boolean DEFAULT_QUITE_MODE = false;
        private final String DEFAULT_TOPIC_PREFIX = "";
        
        // configuration items are described in building methods and on github 
        // in documentation - https://github.com/iqrfsdk/jlibiqrf/wiki/Bridge-iqrf-mqtt-%28user-manual%29#bridge-configuration-items
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
        
        /**
         * Creates instance {@link ConfigurationBuilder} and configure mqtt
         * broker, client id is generated random (can be reconfigured later)
         *
         * @param broker which will be used in config - it's mandatory
         */
        public ConfigurationBuilder(String broker){
            ArgumentChecker.checkNull(broker);
            this.broker = broker;
            Random r = new Random();
            this.clientId = UUID.randomUUID().toString().replace("-", "");
        }
        
        /**
         * Configure client id.
         *
         * @param clientId is ID representing the bridge on MQTT broker. With
         * this client id it will be sent and received all mqtt messages.
         * <br>Default value: randomly generated text
         * @return configuration object
         */
        public ConfigurationBuilder clientId(String clientId){
            ArgumentChecker.checkNull(clientId);
            this.clientId = clientId;
            return this;
        }
        
        /**
         * Configure protocol.
         *
         * @param protocol used for communication with MQTT broker.
         * <br>Default value: tcp
         * @return configuration object
         */
        public ConfigurationBuilder protocol(String protocol){
            ArgumentChecker.checkNull(protocol);
            this.protocol = protocol;
            return this;
        }
        
        /**
         * Configure port.
         *
         * @param port used for communication with MQTT broker.
         * <br>Default value: 1883
         * @return configuration object
         */
        public ConfigurationBuilder port(int port){
            ArgumentChecker.checkNegative(port);
            this.port = port;
            return this;
        }
        
        /**
         * Configure clean session
         *
         * @param cleanSession is usable for setting whether the client and
         * server should remember state across restarts and reconnects. In case
         * that value is false it is applying:
         * <ul><li> Message delivery will be reliable meeting the specified QOS
         * even if the client, server or connection are restarted.</li>
         *     <li>The server will treat a subscription as durable.</li></ul>
         * <br>Default value: false
         * @return configuration object
         */
        public ConfigurationBuilder cleanSession(boolean cleanSession){
            this.cleanSession = cleanSession;
            return this;
        }
        
        /**
         * Configure quite mode.
         *
         * @param quiteMode if quite mode is true, it means, that logs from MQTT Communicator aren’t printed into console.
         * <br>Default value: false
         * @return configuration object
         */
        public ConfigurationBuilder quiteMode(boolean quiteMode){
            this.quiteMode = quiteMode;
            return this;
        }
        
        /**
         * Configure ssl. Defaultly is SSL disabled (false)
         *
         * @param certFilePath is path to SSL certificate.
         * @param username used for SSL authentication.
         * @param password used for SSL authentication.
         * @return configuration object
         */
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
        
        /**
         * Configure topic prefix.
         *
         * @param topicPrefix is prefix in name of topic. This prefix is added before gateway. So in final name of topic is like ROOT/gateway...
         * <br>Default value: ""
         * @return configuration object
         */
        public ConfigurationBuilder topicPrefix(String topicPrefix){
            this.topicPrefix = topicPrefix;
            return this;
        }
        
        /**
         * Build {@link MQTTConfiguration} from configuration builder object.
         * @return {@link MQTTConfiguration} 
         */
        public MQTTConfiguration build(){
            return new MQTTConfiguration(this);
        }
    }
}