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

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * Implements {@link BridgeConfigurationLoader} for loading and saving 
 * configuration from / into file.
 * 
 * @author Martin Strouhal
 */
public class SimpleBridgeConfigurationLoader implements 
        BridgeConfigurationLoader<String, String> {

    private static final SimpleBridgeConfigurationLoader instance = new SimpleBridgeConfigurationLoader();
    
    private SimpleBridgeConfigurationLoader(){}
    
    public static SimpleBridgeConfigurationLoader getInstance(){
        return instance;
    }
    
    @Override
    public BridgeConfiguration load(String path) {
        try {

            File file = new File(path);
            JAXBContext jaxbContext = JAXBContext.newInstance(BridgeConfiguration.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            BridgeConfiguration config = (BridgeConfiguration) jaxbUnmarshaller.unmarshal(file);
            return config;

        } catch (JAXBException e) {
            e.printStackTrace();
            throw new RuntimeException("Configuration cannot be loaded: " + e);
        }
    }

    @Override
    public void saveBridgeConfiguration(BridgeConfiguration config, String path) {
        try {
            File file = new File(path);
            JAXBContext jaxbContext = JAXBContext.newInstance(BridgeConfiguration.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal(config, file);

        } catch (JAXBException e) {
            e.printStackTrace();
            throw new RuntimeException("Configuration cannot be saved: " + e);
        }
    }
}