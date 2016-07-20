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
import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements {@link BridgeConfigurationLoader} for loading and saving 
 * configuration from / into file.
 * 
 * @author Martin Strouhal
 */
public class SimpleBridgeConfigurationLoader implements 
        BridgeConfigurationLoader<String, String> {

    private static final Logger log = LoggerFactory.getLogger(SimpleBridgeConfigurationLoader.class);
    private static final SimpleBridgeConfigurationLoader instance = new SimpleBridgeConfigurationLoader();
    
    private SimpleBridgeConfigurationLoader(){}
    
    public static SimpleBridgeConfigurationLoader getInstance(){
        return instance;
    }
    
    /**
     * @throws RuntimeException if some error has been occurred while loading
     */
    @Override
    public BridgeConfiguration load(String path) {
        ArgumentChecker.checkNull(path);
        try {
            File file = new File(path);
            JAXBContext jaxbContext = JAXBContext.newInstance(BridgeConfiguration.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            BridgeConfiguration config = (BridgeConfiguration) jaxbUnmarshaller.unmarshal(file);
            return config;

        } catch (JAXBException e) {
            log.warn(e.toString());
            throw new RuntimeException("Configuration cannot be loaded: " + e);
        }
    }

    /**
     * @throws RuntimeException if some error has been occurred while saving
     */
    @Override
    public void saveBridgeConfiguration(BridgeConfiguration config, String path) {
        ArgumentChecker.checkNull(path);
        ArgumentChecker.checkNull(config);
        try {
            File file = new File(path);
            JAXBContext jaxbContext = JAXBContext.newInstance(BridgeConfiguration.class, MQTTConfiguration.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal(config, file);

        } catch (JAXBException e) {
            log.warn(e.toString());
            throw new RuntimeException("Configuration cannot be saved: " + e);
        }
    }
}