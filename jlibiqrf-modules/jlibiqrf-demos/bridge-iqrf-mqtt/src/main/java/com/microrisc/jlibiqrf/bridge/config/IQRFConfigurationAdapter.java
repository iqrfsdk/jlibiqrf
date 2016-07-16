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

import com.microrisc.jlibiqrf.configuration.IQRFConfiguration;
import com.microrisc.jlibiqrf.configuration.SimpleIQRFConfigurationLoader;
import java.text.SimpleDateFormat;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter for parsing filename of {@link IQRFConfiguration} and its loading and 
 * its saving too.
 * 
 * @author Martin Strouhal
 */
public class IQRFConfigurationAdapter extends XmlAdapter<String, IQRFConfiguration> {

    private static final Logger log = LoggerFactory.getLogger(IQRFConfigurationAdapter.class);
    
    @Override
    public IQRFConfiguration unmarshal(String xmlText) throws Exception {
        log.debug("unmarshal - start: xmlText={}", xmlText);
        IQRFConfiguration config = SimpleIQRFConfigurationLoader.getInstance().load(xmlText);
        config.setSavingLocation(xmlText);
        log.debug("unmarshal - end: " + config);
        return config;
    }

    @Override
    public String marshal(IQRFConfiguration config) throws Exception {
        log.debug("marshal - start: config={}", config);
        String savingLocation;
        if(config.getSavingLocation() != null)
            savingLocation = config.getSavingLocation();
        else
            savingLocation = getConfigName();
        
        SimpleIQRFConfigurationLoader.getInstance().save(config, savingLocation);
            
        log.debug("marshal - end: " + savingLocation);
        return savingLocation;    
    }
    
    private String getConfigName(){
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
        String time = timeFormat.format(System.currentTimeMillis());
        return "config-" + time;
    }
}