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
package com.microrisc.jlibiqrf.demos.config;

import com.microrisc.jlibiqrf.configuration.IQRFConfiguration;
import com.microrisc.jlibiqrf.configuration.SimpleIQRFConfigurationLoader;
import com.microrisc.jlibiqrf.demos.json.SimpleJsonConvertor;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Encapsulates configuration of {@link Bridge}.
 * 
 * @author Martin Strouhal
 */
@XmlRootElement(name = "Bridge_configuration")
public final class BridgeConfiguration {

    @XmlElement(name = "IQRF_config_path")
    @XmlJavaTypeAdapter(IQRFConfigurationAdapter.class)
    private final IQRFConfiguration iqrfConfig;
    @XmlElement(name = "Client_id")
    private final String clientId;
    @XmlElement(name = "MQTT_broker_protocol")
    private final String mqttBrokerProtocol;
    @XmlElement(name = "MQTT_broker_address")
    private final String mqttBrokerAddress;
    @XmlElement(name = "MQTT_broker_port")
    private final Integer mqttBrokerPort;
    @XmlElement(name = "Checking_interval")
    private final Integer checkingInterval;
    @XmlElement(name = "Subscribed_topics")
    private final String[] subscribedTopics;
    @XmlElement(name = "JSON_convertor")
    private final Class jsonConvertor;

    /** For JAXB purpose only */
    private BridgeConfiguration(){
        iqrfConfig = null;
        mqttBrokerProtocol = clientId = mqttBrokerAddress = null;
        mqttBrokerPort = checkingInterval = null;
        subscribedTopics = null;
        jsonConvertor = null;
    }
    
    /** For ConfigurationBuilder only */
    private BridgeConfiguration(ConfigurationBuilder builder) {
        this.iqrfConfig = builder.iqrfConfig;
        this.clientId = builder.clientId;
        this.mqttBrokerAddress = builder.mqttBrokerAddress;
        this.mqttBrokerPort = builder.mqttBrokerPort;
        this.checkingInterval = builder.checkingInterval;
        this.subscribedTopics = builder.subscribedTopics;
        this.jsonConvertor = builder.jsonConvertor;
        this.mqttBrokerProtocol = builder.mqttBrokerProtocol;
    }

    public IQRFConfiguration getIqrfConfig() {
        return iqrfConfig;
    }

    public String getClientId() {
        return clientId;
    }

    public String getMqttBrokerAddress() {
        return mqttBrokerAddress;
    }

    public Integer getMqttBrokerPort() {
        return mqttBrokerPort;
    }

    public Integer getCheckingInterval() {
        return checkingInterval;
    }

    public String[] getSubscribedTopics() {
        return subscribedTopics;
    }

    public Class getJsonConvertor() {
        return jsonConvertor;
    }

    public String getMqttBrokerProtocol() {
        return mqttBrokerProtocol;
    }

    public String getCompleteAddress(){
        return mqttBrokerProtocol + "://" + mqttBrokerAddress + ":" + mqttBrokerPort;
    }
    
    @Override
    public String toString() {
        return "BridgeConfiguration{" + "iqrfConfig=" + iqrfConfig + ", clientId=" + clientId + ", mqttBrokerProtocol=" + mqttBrokerProtocol + ", mqttBrokerAddress=" + mqttBrokerAddress + ", mqttBrokerPort=" + mqttBrokerPort + ", checkingInterval=" + checkingInterval + ", subscribedTopics=" + subscribedTopics + ", jsonConvertor=" + jsonConvertor + '}';
    }
    
    public static class ConfigurationBuilder {

        private final int DEFAULT_MQTT_BROKER_PORT = 0;
        private final int DEFAULT_CHECKING_INTERVAL = 1000;
        private final String[] DEFAULT_SUBSCRIBED_TOPICS = new String[]{"unknown/dpa/requests"};
        private final Class DEFAULT_JSON_CONVERTOR = SimpleJsonConvertor.class;
        private final String DEFAULT_MQTT_PROTOCOL = "tcp";
        
        private final IQRFConfiguration iqrfConfig;
        private String mqttBrokerProtocol = DEFAULT_MQTT_PROTOCOL;
        private final String mqttBrokerAddress;
        private String clientId;
        private int mqttBrokerPort = DEFAULT_MQTT_BROKER_PORT;
        private int checkingInterval = DEFAULT_CHECKING_INTERVAL;
        private String[] subscribedTopics = DEFAULT_SUBSCRIBED_TOPICS;
        private Class jsonConvertor = DEFAULT_JSON_CONVERTOR;
        
        public ConfigurationBuilder(String mqttBrokerAddress, String config){
            this.mqttBrokerAddress = mqttBrokerAddress;
            this.iqrfConfig = SimpleIQRFConfigurationLoader.getInstance().load(config);
        }
        
        public ConfigurationBuilder(String mqttBrokerAddress, IQRFConfiguration config){
            this.mqttBrokerAddress = mqttBrokerAddress;
            this.iqrfConfig = config;
        }
        
        public ConfigurationBuilder clientId(String clientId){
            this.clientId = clientId;
            return this;
        }
        
        public ConfigurationBuilder mqttProtocol(String protocol){
            this.mqttBrokerProtocol = protocol;
            return this;
        }
        
        public ConfigurationBuilder mqttBrokerPort(int port){
            this.mqttBrokerPort = port;
            return this;
        }
        
        public ConfigurationBuilder checkingInterval(int interval){
            this.checkingInterval = interval;
            return this;
        }
        
        public ConfigurationBuilder subscribedTopics(String[] topics){
            this.subscribedTopics = topics;
            return this;
        }
        
        public ConfigurationBuilder jsonConvertor(Class convertor){
            this.jsonConvertor = convertor;
            return this;
        }
        
        public BridgeConfiguration build(){
            return new BridgeConfiguration(this);
        }
    }
}