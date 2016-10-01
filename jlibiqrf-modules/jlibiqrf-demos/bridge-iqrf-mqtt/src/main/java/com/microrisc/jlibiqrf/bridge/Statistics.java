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
package com.microrisc.jlibiqrf.bridge;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides services for statistics recording and their converting to printable 
 * format.
 * 
 * @author Martin Strouhal
 */
 @JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE) 
public class Statistics { 
    
    @JsonProperty("receivedMessages")
    private int receivedMessages = 0;
    @JsonProperty("sentMessages")
    private int sentMessages = 0;
    @JsonProperty("mac")
    private final String mac = MACRecognizer.getMAC();    
   
    /** Record a new received message and increase counter. */
    public void increaseReceivedMessages(){
        receivedMessages++;
    }
    
    /** Record a new sent message and increase counter. */
    public void increaseSentMessages(){
        sentMessages++;
    }    
    
    /**
     * Returns statistics as json in String.
     * @return json as string
     */
    @JsonUnwrapped    
    public String getAsJson(){
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            return "{\"error\":\"Statics cannot be converted to json\"}";
        }
    }        
}