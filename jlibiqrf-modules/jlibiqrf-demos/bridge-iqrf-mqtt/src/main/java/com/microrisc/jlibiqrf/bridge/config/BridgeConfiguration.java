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
import com.microrisc.jlibiqrf.bridge.Bridge;
import com.microrisc.jlibiqrf.bridge.json.SimpleJsonConvertor;
import com.microrisc.jlibiqrf.configuration.IQRFConfiguration;
import com.microrisc.jlibiqrf.configuration.SimpleIQRFConfigurationLoader;
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
    @XmlElement(name = "MQTT_checking_interval", defaultValue = "1000")
    private final Integer mqttCheckingInterval;
    @XmlElement(name = "IQRF_checking_interval", defaultValue = "1")
    private final Integer iqrfCheckingInterval;
    @XmlElement(name = "JSON_convertor", defaultValue = "com.microrisc.jlibiqrf.bridge.json.SimpleJsonConvertor")
    private final String jsonConvertor;

    /** For JAXB purpose only */
    private BridgeConfiguration(){
        iqrfConfig = null;
        mqttCheckingInterval = iqrfCheckingInterval = null;
        jsonConvertor = null;
        mqttConfig = null;
    }
    
    /** For ConfigurationBuilder only */
    private BridgeConfiguration(ConfigurationBuilder builder) {
        this.iqrfConfig = builder.iqrfConfig;
        this.mqttCheckingInterval = builder.mqttCheckingInterval;
        this.iqrfCheckingInterval = builder.iqrfCheckingInterval;
        this.jsonConvertor = builder.jsonConvertor.getName();
        this.mqttConfig = builder.mqttConfig;
    }

    public IQRFConfiguration getIqrfConfig() {
        return iqrfConfig;
    }

    public Integer getMQTTCheckingInterval() {
        return mqttCheckingInterval;
    }

    public Integer getIQRFCheckingInterval() {
        return iqrfCheckingInterval;
    }

    public Class getJsonConvertor() {
        try {
            return Class.forName(jsonConvertor);
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("Badly loaded name of convertor class: " + ex.getMessage());
        }
    }

    public MQTTConfiguration getMqttConfig() {
        return mqttConfig;
    }

    @Override
    public String toString() {
        return "BridgeConfiguration{" + "iqrfConfig=" + iqrfConfig + ", mqttConfig=" + mqttConfig + ", mqttCheckingInterval=" + mqttCheckingInterval + ", iqrfCheckingInterval=" + iqrfCheckingInterval + ", jsonConvertor=" + jsonConvertor + '}';
    }
    
    public static class ConfigurationBuilder {

        private final int DEFAULT_MQTT_CHECKING_INTERVAL = 1000;
        private final int DEFAULT_IQRF_CHECKING_INTERVAL = 1;
        private final Class DEFAULT_JSON_CONVERTOR = SimpleJsonConvertor.class;
        
        private final IQRFConfiguration iqrfConfig;
        private final MQTTConfiguration mqttConfig;
        private int mqttCheckingInterval = DEFAULT_MQTT_CHECKING_INTERVAL;
        private int iqrfCheckingInterval = DEFAULT_IQRF_CHECKING_INTERVAL;
        private Class jsonConvertor = DEFAULT_JSON_CONVERTOR;
        
        public ConfigurationBuilder(String iqrfConfigPath, MQTTConfiguration mqttConfig){
            ArgumentChecker.checkNull(iqrfConfigPath);
            ArgumentChecker.checkNull(mqttConfig);
            this.iqrfConfig = SimpleIQRFConfigurationLoader.getInstance().load(iqrfConfigPath);
            this.mqttConfig = mqttConfig;
        }
        
        public ConfigurationBuilder(IQRFConfiguration config, MQTTConfiguration mqttConfig){
            ArgumentChecker.checkNull(config);
            ArgumentChecker.checkNull(mqttConfig);
            this.iqrfConfig = config;
            this.mqttConfig = mqttConfig;
        }
        
        public ConfigurationBuilder mqttCheckingInterval(int interval){
            ArgumentChecker.checkNegative(interval);
            this.mqttCheckingInterval = interval;
            return this;
        }
        
        public ConfigurationBuilder iqrfCheckingInterval(int interval){
            ArgumentChecker.checkNegative(interval);
            this.iqrfCheckingInterval = interval;
            return this;
        }
                
        public ConfigurationBuilder jsonConvertor(Class convertor){
            ArgumentChecker.checkNull(convertor);
            this.jsonConvertor = convertor;
            return this;
        }
        
        public BridgeConfiguration build(){
            return new BridgeConfiguration(this);
        }
    }
}