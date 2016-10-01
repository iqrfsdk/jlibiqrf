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
import com.microrisc.jlibiqrf.bridge.json.JsonConvertor;
import com.microrisc.jlibiqrf.bridge.json.simple.SimpleJsonConvertor;
import com.microrisc.jlibiqrf.bridge.mqtt.MQTTCommunicator;
import com.microrisc.jlibiqrf.configuration.IQRFConfiguration;
import com.microrisc.jlibiqrf.configuration.SimpleIQRFConfigurationLoader;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Encapsulates configuration of {@link Bridge}.
 * The configuration can be loaded:
 *  - from xml file via {@link BridgeConfigurationLoader}
 *  - can be built in code via {@link ConfigurationBuilder}
 * 
 * Note: default value is defined in annotation, but primary is pulled from 
 * {@link BridgeConfiguration.ConfigurationBuilder}. In annotations is for 
 * information purpose.
 * 
 * @author Martin Strouhal
 */
@XmlRootElement(name = "Bridge_configuration")
public final class BridgeConfiguration {

    /** Specify used configuration of jLibIQRF for communication with IQRF. 
     * See {@link IQRFConfiguration} */
    @XmlElement(name = "IQRF_config_path")
    @XmlJavaTypeAdapter(IQRFConfigurationAdapter.class)
    private final IQRFConfiguration iqrfConfig;
    
    /** Specify configuration of MQTT part communicating with MQTT broker. See 
     * details inside configuration class and secondly usage in 
     * {@link MQTTCommunicator}. */
    @XmlElement( name = "MQTT_config")
    
    private final MQTTConfiguration mqttConfig;
    
    /** Specify how often is thread in {@link MQTTCommunicator} checking for new
     * messages. (in milliseconds) */
    @XmlElement(name = "MQTT_checking_interval", defaultValue = "1000")
    private final Integer mqttCheckingInterval;
    
    /** Specify how often is thread in {@link IQRFCommunicator} checking for new
     * messages. (in seconds) */
    @XmlElement(name = "IQRF_checking_interval", defaultValue = "1")
    private final Integer iqrfCheckingInterval;
    
    /** Specify which implementation of {@link JsonConvertor} will be used for 
     * messages converting. See possible implementations of {@link JsonConvertor}. */
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

    /**
     * Getter for {@link BridgeConfiguration#iqrfConfig}
     */
    public IQRFConfiguration getIqrfConfig() {
        return iqrfConfig;
    }

    /**
     * Getter for {@link BridgeConfiguration#mqttCheckingInterval}
     */
    public Integer getMQTTCheckingInterval() {
        return mqttCheckingInterval;
    }

    /**
     * Getter for {@link BridgeConfiguration#iqrfCheckingInterval}
     */
    public Integer getIQRFCheckingInterval() {
        return iqrfCheckingInterval;
    }

    /**
     * Getter for {@link BridgeConfiguration#jsonConvertor}
     */
    public Class getJsonConvertor() {
        try {
            return Class.forName(jsonConvertor);
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("Badly loaded name of convertor class: " + ex.getMessage());
        }
    }

    /**
     * Getter for {@link BridgeConfiguration#mqttConfig}
     */    
    public MQTTConfiguration getMqttConfig() {
        return mqttConfig;
    }

    @Override
    public String toString() {
        return "BridgeConfiguration{" + "iqrfConfig=" + iqrfConfig + ", mqttConfig=" + mqttConfig + ", mqttCheckingInterval=" + mqttCheckingInterval + ", iqrfCheckingInterval=" + iqrfCheckingInterval + ", jsonConvertor=" + jsonConvertor + '}';
    }
    
    /**
     * Provides interface for dynamic building of configuration in code.
     */
    public static class ConfigurationBuilder {

        // default values for properties in BridgeConfiguration
        private final int DEFAULT_MQTT_CHECKING_INTERVAL = 1000;
        private final int DEFAULT_IQRF_CHECKING_INTERVAL = 1;
        private final Class DEFAULT_JSON_CONVERTOR = SimpleJsonConvertor.class;
        
        // declaration of fields (in some cases with initialization by default values
        private final IQRFConfiguration iqrfConfig;
        private final MQTTConfiguration mqttConfig;
        private int mqttCheckingInterval = DEFAULT_MQTT_CHECKING_INTERVAL;
        private int iqrfCheckingInterval = DEFAULT_IQRF_CHECKING_INTERVAL;
        private Class jsonConvertor = DEFAULT_JSON_CONVERTOR;
        
        /**
         * Provides interface for creating bridge configuration with loading of
         * IQRF configuration from specified file. For dynamic building of
         * IQRFConfiguration inside Bridge configuration see 
         * {@link ConfigurationBuilder#ConfigurationBuilder(com.microrisc.jlibiqrf.configuration.IQRFConfiguration, com.microrisc.jlibiqrf.bridge.config.MQTTConfiguration)
         *
         * @param iqrfConfigPath path to iqrf config file
         * @param mqttConfig mqtt configuration object
         */
        public ConfigurationBuilder(String iqrfConfigPath, MQTTConfiguration mqttConfig){
            ArgumentChecker.checkNull(iqrfConfigPath);
            ArgumentChecker.checkNull(mqttConfig);
            this.iqrfConfig = SimpleIQRFConfigurationLoader.getInstance().load(iqrfConfigPath);
            this.mqttConfig = mqttConfig;
        }

        /**
         * Provides interface for creating bridge configuration with dynamic iqrf configuration. For loading of
         * IQRF configuration from specified file inside builder see 
         * {@link ConfigurationBuilder#ConfigurationBuilder(java.lang.String, com.microrisc.jlibiqrf.bridge.config.MQTTConfiguration) 
         *
         * @param iqrfConfigPath path to iqrf config file
         * @param mqttConfig mqtt configuration object
         */        
        public ConfigurationBuilder(IQRFConfiguration config, MQTTConfiguration mqttConfig){
            ArgumentChecker.checkNull(config);
            ArgumentChecker.checkNull(mqttConfig);
            this.iqrfConfig = config;
            this.mqttConfig = mqttConfig;
        }
        
        /**
         * Setter for {@link BridgeConfiguration#mqttCheckingInterval}
         */
        public ConfigurationBuilder mqttCheckingInterval(int interval){
            ArgumentChecker.checkNegative(interval);
            this.mqttCheckingInterval = interval;
            return this;
        }

        /**
         * Setter for {@link BridgeConfiguration#iqrfCheckingInterval}
         */        
        public ConfigurationBuilder iqrfCheckingInterval(int interval){
            ArgumentChecker.checkNegative(interval);
            this.iqrfCheckingInterval = interval;
            return this;
        }
                
        /**
         * Setter for {@link BridgeConfiguration#jsonConvertor}
         */
        public ConfigurationBuilder jsonConvertor(Class convertor){
            ArgumentChecker.checkNull(convertor);
            this.jsonConvertor = convertor;
            return this;
        }
        
        /**
         * Build configuration object
         * @return {@link BridgeConfiguration}
         */
        public BridgeConfiguration build(){
            return new BridgeConfiguration(this);
        }
    }
}