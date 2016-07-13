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
package com.microrisc.jlibiqrf.demos;

import com.microrisc.jlibiqrf.demos.config.BridgeConfiguration;
import com.microrisc.jlibiqrf.demos.iqrf.IQRFCommunicator;
import com.microrisc.jlibiqrf.demos.json.SimpleJsonConvertor;
import com.microrisc.jlibiqrf.demos.mqtt.MQTTCommunicator;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martin Strouhal
 */
public class Bridge {

    private static final Logger log = LoggerFactory.getLogger(Bridge.class);
    
    // queue for messages from MQTT
    private final Queue<MqttMessage> mqttMessages;
    // queue for data from IQRF network
    private final Queue<short[]> iqrfData;
    
    public Bridge(BridgeConfiguration config){
        log.debug("Bridge - init - start: config={}", config);
        mqttMessages = new LinkedList<>();
        iqrfData = new LinkedList<>();
        
        IQRFCommunicator iqrfCom = new IQRFCommunicator(this);
        iqrfCom.init();
        
        MQTTCommunicator mqttCom = new MQTTCommunicator(
                config.getClientId(),
                config.getCompleteAddress(),
                Arrays.asList(config.getSubscribedTopics()), this);
        mqttCom.connect();
        mqttCom.checkAndPublishDeviceData(config.getCheckingInterval());
        log.debug("Bridge - init - end");
    }

    public void addIQRFData(short[] data) {
        log.debug("addIQRFData - start: data={}", Arrays.toString(data));
        synchronized(iqrfData){
            iqrfData.add(data);
        }
        log.debug("addIQRFData - end");
    }

    public void addMqqtMessage(MqttMessage msg) {
        log.debug("addMqqtMessage - start: msg={}", msg);
        synchronized(mqttMessages){
            mqttMessages.add(msg);
        }
        log.debug("addMqqtMessage - end");
    }

    public boolean isAvailableMqttMessage(){
        synchronized(mqttMessages){
            return !mqttMessages.isEmpty();
        }
    }
    
    public short[] getAndRemoveMqttMessage() {
        log.debug("getAndRemoveMqttMessage - start");
        synchronized(mqttMessages){
            MqttMessage mqttMsg = mqttMessages.poll();
            String msg = new String(mqttMsg.getPayload());
            short[] result = SimpleJsonConvertor.getInstance().toIQRF(msg);
            log.debug("getAndRemoveMqttMessage - end: {}", Arrays.toString(result));
            return result;
        }
    }

    public boolean isAvailableIQRFData(){
        synchronized(iqrfData){
            return !iqrfData.isEmpty();
        }
    }
    
    public MqttMessage getAndRemoveIQRFData() {
        log.debug("getAndRemoveIQRFData - start");
        synchronized(iqrfData){
            short[] data = iqrfData.poll();
            Object objMsg = SimpleJsonConvertor.getInstance().toJson(data);
            if(objMsg instanceof MqttMessage){
                log.debug("getAndRemoveIQRFData - end: {}", objMsg);
                return (MqttMessage)objMsg;
            }else if(objMsg instanceof String){
                log.debug("getAndRemoveIQRFData - end: {}", objMsg);
                return new MqttMessage(((String) objMsg).getBytes());
            }else{
                log.error("JsonConvertor returned unsupported format of message.");
                return new MqttMessage(new String(Arrays.toString(data)).getBytes());
            }
        }
    }
}