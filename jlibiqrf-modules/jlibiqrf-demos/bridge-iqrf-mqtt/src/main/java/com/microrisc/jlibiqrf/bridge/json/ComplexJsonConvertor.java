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
package com.microrisc.jlibiqrf.bridge.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microrisc.jlibiqrf.bridge.ArgumentChecker;
import com.microrisc.jlibiqrf.bridge.MACRecognizer;
import com.microrisc.jlibiqrf.bridge.mqtt.DPAReplyType;
import com.microrisc.jlibiqrf.bridge.mqtt.PublishableMqttMessage;
import com.microrisc.jlibiqrf.types.IQRFData;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martin Strouhal
 */
public class ComplexJsonConvertor implements JsonConvertor {

    private static final Logger log = LoggerFactory.getLogger(ComplexJsonConvertor.class);
    
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ComplexJsonConvertor instance = new ComplexJsonConvertor();
    private final String mac;
    
    /** Singleton */
    private ComplexJsonConvertor(){
        this.mac = MACRecognizer.getMAC();
    }
    
    /** Returns instance of convertor
     * 
     * @return instance of {@link ComplexJsonConvertor}
     */
    public static ComplexJsonConvertor getInstance(){
        return instance;
    }
    
    @Override
    public IQRFData toIQRF(Object json) {
        log.debug("toIQRF - start: json={}", json);
        ArgumentChecker.checkNull(json);
        if (json instanceof String) {
            String jsonString = (String) json;
            try {
                ComplexIQRFData data = mapper.readValue(jsonString, ComplexIQRFData.class);
                log.debug("toIQRF - end: {}", data);
                return data;
            } catch (IOException ex) {
                log.warn("Invalid Json data: " + ex.getMessage());
                throw new IllegalArgumentException("Invalid Json data");
            }
        } else {
            log.warn("Json object must be instance of String");
            throw new IllegalArgumentException("Json object must be instance of String");
        }

    }

    @Override
    public PublishableMqttMessage toJson(short[] iqrf) {
        log.debug("toJson - start: iqrf={}", Arrays.toString(iqrf));
        ArgumentChecker.checkNull(iqrf);
        
        ObjectNode parsedData = mapper.createObjectNode();
        parsedData.put("time", new Timestamp(new Date().getTime()).toString());
                
        StringBuilder payloadBuilder = new StringBuilder();
        for (int i = 0; i <iqrf.length; i++, payloadBuilder.append(".")) {
            payloadBuilder.append(iqrf[i]);
        }
        parsedData.put("payload", payloadBuilder.toString());
        
        parsedData.put("mac", mac);
        parsedData.put("size", iqrf.length);
        
        if(iqrf.length > 7 && iqrf[6] == 0xFF){    
            if (iqrf.length == 11) {
                log.debug("toJson - end:" + parsedData.toString());
                return new PublishableMqttMessage(DPAReplyType.CONFIRMATION, parsedData.toString().getBytes());
            }
        }
        log.debug("toJson - end:" + parsedData.toString());
        return new PublishableMqttMessage(DPAReplyType.RESPONSE, parsedData.toString().getBytes());                                                   
    }

}
