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

import com.microrisc.jlibiqrf.bridge.mqtt.PublishableMqttMessage;

/**
 * Provides converting IQRF data to json and vice versa.
 *
 * @author Martin Strouhal
 */
public interface JsonConvertor {

    /** Converts string with json data to IQRF data which can be send into IQRF.
     * network.
     *
     * @param json object is with data formatted as json
     * @return short array with individual bytes of IQRF data
     */
    short[] toIQRF(Object json);

    /**
     * Converts IQRF data from IQRF network into message with json data. The
     * message contains info about topic to publish too.
     *
     * @param iqrf is short array with individual bytes of IQRF data
     * @return object with data formatted as json, contains also info about 
     * topic to publish
     */
    PublishableMqttMessage toJson(short[] iqrf);   
    
}
