package com.microrisc.jlibiqrf.examples;

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

import com.microrisc.jlibiqrf.configuration.IQRFConfiguration;
import com.microrisc.jlibiqrf.configuration.SimpleXMLConfigurationLoader;
import com.microrisc.jlibiqrf.iqrfLayer.cdc.CDCConfiguration;
import com.microrisc.jlibiqrf.iqrfLayer.serial.SerialConfiguration;
import com.microrisc.jlibiqrf.iqrfLayer.spi.SPIConfiguration;
import com.microrisc.jlibiqrf.iqrfLayer.udp.UDPConfiguration;

/**
 * Test of working with configuration.
 * @author Martin Strouhal
 */
public class ConfigurationTest {

    public static void main(String[] args) {
        IQRFConfiguration config;
        
        config = new UDPConfiguration("local", 60, "remotr", 80);
        SimpleXMLConfigurationLoader.getInstance().save(config, "udp-config.xml");
        System.out.println("UDP configuration saved.");
        
        config = new SPIConfiguration("COM5");
        SimpleXMLConfigurationLoader.getInstance().save(config, "spi-config.xml");
        System.out.println("SPI configuration saved.");
        
        config = new CDCConfiguration("COM5");
        SimpleXMLConfigurationLoader.getInstance().save(config, "cdc-config.xml");
        System.out.println("CDC configuration saved.");
        
        config = new SerialConfiguration("COM5", 9600);
        SimpleXMLConfigurationLoader.getInstance().save(config, "serial-config.xml");
        System.out.println("Serial configuration saved.");

    }
}
