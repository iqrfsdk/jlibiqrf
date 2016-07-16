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
    @XmlElement( name = "MQTT_config")
    private final MQTTConfiguration mqttConfig;
    @XmlElement(name = "Checking_interval")
    private final Integer checkingInterval;
    @XmlElement(name = "Subscribed_topics")
    private final String[] subscribedTopics;
    @XmlElement(name = "JSON_convertor")
    private final Class jsonConvertor;

    /** For JAXB purpose only */
    private BridgeConfiguration(){
        iqrfConfig = null;
        checkingInterval = null;
        subscribedTopics = null;
        jsonConvertor = null;
        mqttConfig = null;
    }
    
    /** For ConfigurationBuilder only */
    private BridgeConfiguration(ConfigurationBuilder builder) {
        this.iqrfConfig = builder.iqrfConfig;
        this.checkingInterval = builder.checkingInterval;
        this.subscribedTopics = builder.subscribedTopics;
        this.jsonConvertor = builder.jsonConvertor;
        this.mqttConfig = builder.mqttConfig;
    }

    public IQRFConfiguration getIqrfConfig() {
        return iqrfConfig;
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

    public MQTTConfiguration getMqttConfig() {
        return mqttConfig;
    }

    @Override
    public String toString() {
        return "BridgeConfiguration{" + "iqrfConfig=" + iqrfConfig + ", mqttConfig=" + mqttConfig + ", checkingInterval=" + checkingInterval + ", subscribedTopics=" + subscribedTopics + ", jsonConvertor=" + jsonConvertor + '}';
    }
    
    public static class ConfigurationBuilder {

        private final int DEFAULT_CHECKING_INTERVAL = 1000;
        private final String[] DEFAULT_SUBSCRIBED_TOPICS = new String[]{"unknown/dpa/requests"};
        private final Class DEFAULT_JSON_CONVERTOR = SimpleJsonConvertor.class;
        
        private final IQRFConfiguration iqrfConfig;
        private final MQTTConfiguration mqttConfig;
        private int checkingInterval = DEFAULT_CHECKING_INTERVAL;
        private String[] subscribedTopics = DEFAULT_SUBSCRIBED_TOPICS;
        private Class jsonConvertor = DEFAULT_JSON_CONVERTOR;
        
        public ConfigurationBuilder(String iqrfConfigPath, MQTTConfiguration mqttConfig){
            this.iqrfConfig = SimpleIQRFConfigurationLoader.getInstance().load(iqrfConfigPath);
            this.mqttConfig = mqttConfig;
        }
        
        public ConfigurationBuilder(IQRFConfiguration config, MQTTConfiguration mqttConfig){
            this.iqrfConfig = config;
            this.mqttConfig = mqttConfig;
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