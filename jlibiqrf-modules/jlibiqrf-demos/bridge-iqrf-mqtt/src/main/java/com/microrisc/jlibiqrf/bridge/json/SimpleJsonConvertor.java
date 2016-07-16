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
import java.io.IOException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Provides convert services between iqrf data (short[]) and json data (String).
 * 
 * @author Martin Strouhal
 */
public class SimpleJsonConvertor implements JsonConvertor {

    private static final Logger log = LoggerFactory.getLogger(SimpleJsonConvertor.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final SimpleJsonConvertor instance = new SimpleJsonConvertor();
    
    /** Singleton */
    private SimpleJsonConvertor(){}
    
    /** Returns instance of convertor
     * 
     * @return instance of {@link SimpleJsonConvertor}
     */
    public static SimpleJsonConvertor getInstance(){
        return instance;
    }

    /**
     * @throws IllegalArgumentException if data cannot be converted
     */
    @Override
    public short[] toIQRF(Object json) {
        log.debug("toIQRF - start: json={}", json);
        if (json instanceof String) {
            String jsonString = (String) json;
            try {
                JsonIQRFData data = mapper.readValue(jsonString, JsonIQRFData.class);
                log.debug("toIQRF - end: {}", Arrays.toString(data.getIQRFData()));
                return data.getIQRFData();
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
    public Object toJson(short[] iqrf) {
        log.debug("toJson - start: iqrf={}", Arrays.toString(iqrf));
        ObjectNode iqrfData = mapper.createObjectNode();
        iqrfData.put("nadr", getTwoShortAsInt(iqrf, 0));
        iqrfData.put("per", iqrf[2]);
        iqrfData.put("cmd", iqrf[3]);
        iqrfData.put("hwpid", getTwoShortAsInt(iqrf, 4));
        log.debug("toJson - end:" + iqrfData.toString());
        return iqrfData.toString();
    }
    
    /**
     * Returns int value of two element from array on specified index. Eg. for
     * elements 255 and 0 is returned 0xF0.
     */
    private int getTwoShortAsInt(short[] array, int index){
        int value = array[index];
        value <<=8;
        value += array[index+1];
        return value;
    }
}
