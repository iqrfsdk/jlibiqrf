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
import com.microrisc.jlibiqrf.iqrfLayer.cdc.CDCConfiguration;
import com.microrisc.jlibiqrf.iqrfLayer.serial.SerialConfiguration;
import com.microrisc.jlibiqrf.iqrfLayer.spi.SPIConfiguration;
import com.microrisc.jlibiqrf.iqrfLayer.udp.UDPConfiguration;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Martin Strouhal
 */
final class IQRFConfigurationLoader implements
        com.microrisc.jlibiqrf.configuration.XMLConfigurationLoader<PipedInputStream, PipedOutputStream> {

    private static final Class[] configurationObjects = new Class[]{
        IQRFConfiguration.class, SPIConfiguration.class, CDCConfiguration.class,
        SerialConfiguration.class, UDPConfiguration.class};

    private static IQRFConfigurationLoader instance = new IQRFConfigurationLoader();

    private IQRFConfigurationLoader() {
    }

    public static IQRFConfigurationLoader getInstance() {
        return instance;
    }
    
    @Override
    public IQRFConfiguration load(PipedInputStream source) {

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(configurationObjects);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            IQRFConfiguration config = (IQRFConfiguration) jaxbUnmarshaller.unmarshal(source);
            return config;

        } catch (JAXBException e) {
            e.printStackTrace();
            throw new RuntimeException("Configuration cannot be loaded: " + e);
        }
    }

    @Override
    public void save(IQRFConfiguration config, PipedOutputStream saveLocation) {
        try {
            Class implementedClass = null;
            switch (config.getIQRFCommunicationType()) {
                case CDC:
                    implementedClass = CDCConfiguration.class;
                    break;
                case SPI:
                    implementedClass = SPIConfiguration.class;
                    break;
                case SERIAL:
                    implementedClass = SerialConfiguration.class;
                    break;
                case UDP:
                    implementedClass = UDPConfiguration.class;
                    break;
            }
            JAXBContext jaxbContext = JAXBContext.newInstance(
                    IQRFConfiguration.class, implementedClass);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            
            System.out.println("debug ----");
            jaxbMarshaller.marshal(config, System.out);
            System.out.println("--- debug");
            
            jaxbMarshaller.marshal(config, saveLocation);

        } catch (JAXBException e) {
            e.printStackTrace();
            throw new RuntimeException("Configuration cannot be saved: " + e);
        }
    }   
}
