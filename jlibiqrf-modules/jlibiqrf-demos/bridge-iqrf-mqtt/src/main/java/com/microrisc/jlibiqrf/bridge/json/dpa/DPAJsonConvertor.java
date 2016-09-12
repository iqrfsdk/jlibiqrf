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

package com.microrisc.jlibiqrf.bridge.json.dpa;

import com.microrisc.jlibiqrf.bridge.mqtt.DPAReplyType;
import com.microrisc.jlibiqrf.bridge.mqtt.PublishableMqttMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microrisc.jlibiqrf.bridge.ArgumentChecker;
import com.microrisc.jlibiqrf.bridge.json.JsonConvertor;
import com.microrisc.jlibiqrf.types.IQRFData;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides convert services between iqrf data (short[]) and json data (String).
 * 
 * @author Martin Strouhal
 */
public class DPAJsonConvertor implements JsonConvertor {

    private static final Logger log = LoggerFactory.getLogger(DPAJsonConvertor.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final DPAJsonConvertor instance = new DPAJsonConvertor();
    
    /** Singleton */
    private DPAJsonConvertor(){}
    
    /** Returns instance of convertor
     * 
     * @return instance of {@link DPAJsonConvertor}
     */
    public static DPAJsonConvertor getInstance(){
        return instance;
    }

    /**
     * @throws IllegalArgumentException if data cannot be converted
     */
    @Override
    public IQRFData toIQRF(Object json) {
        log.debug("toIQRF - start: json={}", json);
        ArgumentChecker.checkNull(json);
        if (json instanceof String) {
            String jsonString = (String) json;
            try {
                DPAIQRFData data = mapper.readValue(jsonString, DPAIQRFData.class);
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
    public PublishableMqttMessage toJson(short[] iqrfData) {
        log.debug("toJson - start: iqrfData={}", Arrays.toString(iqrfData));
        ArgumentChecker.checkNull(iqrfData);
        
        ObjectNode parsedData = mapper.createObjectNode();
        parsedData.put("timestamp", new Timestamp(new Date().getTime()).toString());
        
        if(iqrfData.length >= 6){
            parseFoursome(parsedData, iqrfData);
        }else{
            log.warn("Unstandard message!");
            parsedData.put("unparseableData", Arrays.toString(iqrfData));
            parsedData.put("error", "Doesn't contains packet information!");
            log.debug("toJson - end:" + parsedData.toString());
            return new PublishableMqttMessage(DPAReplyType.ERROR, parsedData.toString().getBytes());
        }
        
        if(iqrfData.length > 7 && iqrfData[6] == 0xFF){    
            if (iqrfData.length == 11) {
                parseConfirmation(parsedData, iqrfData);
                log.debug("toJson - end:" + parsedData.toString());
                return new PublishableMqttMessage(DPAReplyType.CONFIRMATION, parsedData.toString().getBytes());
            }else{
                log.warn("Unstandard message!");
                parsedData.put("unparseableData", Arrays.toString(iqrfData));
                parsedData.put("error", "Invalid confirmation!");
                log.debug("toJson - end:" + parsedData.toString());
                return new PublishableMqttMessage(DPAReplyType.ERROR, parsedData.toString().getBytes());
            }
        }else{
            parseResponse(parsedData, iqrfData);
            log.debug("toJson - end:" + parsedData.toString());
            return new PublishableMqttMessage(DPAReplyType.RESPONSE, parsedData.toString().getBytes());   
        }                                        
    }
    
    /**
     * Returns int value of two element from array on specified index. Eg. for
     * elements 255 and 0 is returned 0xF0.
     */
    private int getTwoShortAsInt(short[] array, int index){
        int value = array[index+1];
        value <<=8;
        value += array[index];
        return value;
    }

    private void parseFoursome(ObjectNode parsedData, short[] iqrfData) {
        parsedData.put("nadr", getTwoShortAsInt(iqrfData, 0));
        parsedData.put("per", iqrfData[2]);
        parsedData.put("cmd", iqrfData[3]);
        parsedData.put("hwpid", getTwoShortAsInt(iqrfData, 4));
    }

    private void parseConfirmation(ObjectNode parsedData, short[] iqrfData) {
        parsedData.put("dpaValue", iqrfData[7]);
        parsedData.put("hops", iqrfData[8]);
        parsedData.put("timeslotLength", iqrfData[9]);
        parsedData.put("hopsResponse", iqrfData[10]);
    }

    private void parseResponse(ObjectNode parsedData, short[] iqrfData) {
        ArrayNode array = parsedData.putArray("data");
        for (int i = 6; iqrfData != null && i < iqrfData.length; i++) {
            array.add(iqrfData[i]);
        }
    }
}