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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microrisc.jlibiqrf.bridge.ArgumentChecker;
import com.microrisc.jlibiqrf.bridge.MACRecognizer;
import com.microrisc.jlibiqrf.bridge.json.JsonConvertor;
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
 * Provides convert services between iqrf data (short[]) and json data (String).
 * See the description on github wiki: https://github.com/iqrfsdk/jlibiqrf/wiki/SimpleJsonConvertor
 *
 * @author Martin Strouhal
 */
public class SimpleJsonConvertor implements JsonConvertor {

    private static final Logger log = LoggerFactory.getLogger(SimpleJsonConvertor.class);

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final SimpleJsonConvertor instance = new SimpleJsonConvertor();
    private final DPAStateWatcher dpaWatcher = new DPAStateWatcher();
    private final String mac;

    /**
     * Singleton
     */
    private SimpleJsonConvertor() {
        this.mac = MACRecognizer.getMAC();
    }

    /**
     * Returns instance of convertor
     *
     * @return instance of {@link SimpleJsonConvertor}
     */
    public static SimpleJsonConvertor getInstance() {
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
                SimpleIQRFData data = mapper.readValue(jsonString, SimpleIQRFData.class);

                // analyze it
                DPAAddon addon = new DPAAddon(false, 0);
                dpaWatcher.addDPAAddon(addon);

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

        StringBuilder payloadBuilder = new StringBuilder();
        for (int i = 0; i < iqrf.length; i++) {
            payloadBuilder.append(String.format("%02x", iqrf[i]));
            if (i < iqrf.length - 1) {
                payloadBuilder.append(".");
            }
        }
        parsedData.put("payload", payloadBuilder.toString().toUpperCase());
        DPAAddon addon = dpaWatcher.getDPAAddon();
        parsedData.put("dpa", addon.isDPA());
        parsedData.put("dpa-pn", addon.getNumber());
        parsedData.put("size", iqrf.length);
        parsedData.put("mac", mac);
        parsedData.put("time", new Timestamp(new Date().getTime()).toString());
        parsedData.put("timestamp", System.currentTimeMillis());

        log.debug("toJson - end:" + parsedData.toString());
        return new PublishableMqttMessage(DPAReplyType.RESPONSE, parsedData.toString().getBytes());
    }
}
