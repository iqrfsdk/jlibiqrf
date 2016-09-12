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
package com.microrisc.jlibiqrf.bridge.json.simple;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microrisc.jlibiqrf.types.IQRFData;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Data usable for {@link SimpleJsonConvertor}.
 * 
 * @author Martin Strouhal
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimpleIQRFData implements IQRFData {

    private Logger log = LoggerFactory.getLogger(SimpleIQRFData.class);
    
    private short[] payload;    
    @JsonProperty("dpa")
    private boolean dpa;
    @JsonProperty("dpa-pn")
    private int packetNumber;    
    private int size = -1;
    @JsonProperty("mac")
    private String mac;
    @JsonProperty("time")
    private String time;
    
    // it needs after initialization by json parsing library CHECK of data parity - payload size same as size, and etc.
    
    @Override
    public short[] getData() {
        return payload;
    }     

    @JsonProperty("payload")
    public void setPayload(String payloadString) {
        log.debug("setPayload - start: payloadString={}", payloadString);
        String[] splittedArray = payloadString.split("\\.");
        payload = new short[splittedArray.length];
        for (int i = 0; i < splittedArray.length; i++) {            
            payload[i] = Short.parseShort(splittedArray[i], 16);
        }
        if(size == -1){
            size = payload.length;
        }else{
            if(size != payload.length){
                throw new IllegalArgumentException("Illegal json data. Size isn't correct.");
            }
        }
        log.debug("setPayload - end");
    }
    
    @JsonProperty("size")
    public void setSize(int size) {
        log.debug("setSize - start: size={}", size);
        if(payload == null){
            this.size = size;
        }else{
            if(size != payload.length){
                throw new IllegalArgumentException("Illegal json data. Size isn't correct.");
            }
        }
        log.debug("setSize - end");
    }

    @Override
    public String toString() {
        return "ComplexIQRFData{" + "payload=" + Arrays.toString(payload) + ", dpa=" + dpa + ", packetNumber=" + packetNumber + ", size=" + size + ", mac=" + mac + ", time=" + time + '}';
    }
}
