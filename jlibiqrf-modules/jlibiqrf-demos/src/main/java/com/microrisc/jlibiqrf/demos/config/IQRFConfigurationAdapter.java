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
import com.microrisc.jlibiqrf.configuration.SimpleXMLConfigurationLoader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.Charset;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martin Strouhal
 */
public class IQRFConfigurationAdapter extends XmlAdapter<String, IQRFConfiguration> {

    private static final Logger log = LoggerFactory.getLogger(IQRFConfigurationAdapter.class);
    
    @Override
    public IQRFConfiguration unmarshal(String xmlText) throws Exception {
        log.debug("unmarshal - start: xmlText={}", xmlText);
//        PipedInputStream input = new PipedInputStream();
//        PipedOutputStream output = new PipedOutputStream(input);
//        output.write(xmlText.getBytes(Charset.forName("utf-8")), 0, xmlText.length());
//        output.flush();
//        output.close();
        IQRFConfiguration config = SimpleXMLConfigurationLoader.getInstance().load(xmlText);
//        input.close();
        log.debug("unmarshal - end: " + config);
        return config;
    }

    @Override
    public String marshal(IQRFConfiguration config) throws Exception {
        log.debug("marshal - start: config={}", config);
        PipedInputStream input = new PipedInputStream();
        PipedOutputStream output = new PipedOutputStream(input);
        IQRFConfigurationLoader.getInstance().save(config, output);
        
        output.close();
        byte[] textAsBytes = new byte[input.available()];
        input.read(textAsBytes);
        input.close();
        
        String xmlText = new String(textAsBytes, Charset.forName("utf-8"));
        
        log.debug("marshal - end: " + xmlText);
        return xmlText;    
    }
}