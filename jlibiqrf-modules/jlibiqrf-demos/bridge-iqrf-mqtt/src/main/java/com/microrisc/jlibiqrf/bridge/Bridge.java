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

import com.microrisc.jlibiqrf.bridge.config.BridgeConfiguration;
import com.microrisc.jlibiqrf.bridge.iqrf.IQRFCommunicator;
import com.microrisc.jlibiqrf.bridge.json.JsonConvertor;
import com.microrisc.jlibiqrf.bridge.mqtt.MQTTCommunicator;
import com.microrisc.jlibiqrf.bridge.mqtt.PublishableMqttMessage;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides main functionality for bridging data. There is implemented mechanism 
 * for safe thread adding and removing of data.
 *
 * @author Martin Strouhal
 */
public class Bridge {

    private static final Logger log = LoggerFactory.getLogger(Bridge.class);
    
    // queue for messages from MQTT
    private final Queue<short[]> mqttMessages;
    // queue for data from IQRF network
    private final Queue<PublishableMqttMessage> iqrfData;
    
    private final MQTTCommunicator mqttCommunicator;
    private final IQRFCommunicator iqrfCommunicator;
    private final JsonConvertor convertor;
    
    /**
     * Inits {@link Bridge} with specified configuration.
     * 
     * @param config which will be used for initialization
     */
    public Bridge(BridgeConfiguration config) {
        
        log.debug("Bridge - init - start: config={}", config);
        ArgumentChecker.checkNull(config);
        
        mqttMessages = new LinkedList<>();
        iqrfData = new LinkedList<>();
        
        iqrfCommunicator = new IQRFCommunicator(this);
        iqrfCommunicator.init(config);
        
        // in previous versions used commented code
        String mid = /*iqrfCommunicator.readCoordinatorMID()*/"unknown";
        log.info("MID of Coordinator is " + mid);
        
        try {
            if(JsonConvertor.class.isAssignableFrom(config.getJsonConvertor())){
                convertor = (JsonConvertor)config.getJsonConvertor().getMethod("getInstance", null).invoke(null, null);
            }else{
                throw new IllegalArgumentException("Convertor must be child of JsonConvertor.");
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
        
        try {
            mqttCommunicator = new MQTTCommunicator(config.getMqttConfig(), this, mid);
            mqttCommunicator.subscribe(0);
            mqttCommunicator.checkAndPublishDeviceData(config.getMQTTCheckingInterval());
        } catch (MqttException ex) {
            log.error(ex.getMessage());
            throw new RuntimeException(ex);
        }
        
        log.debug("Bridge - init - end");
    }

    /**
     * Adds iqrf data to bridging and converts it to correct format before can 
     * be added to queue to mqtt publish.
     * @param data to add
     */
    public void addIQRFData(short[] data) {
        log.debug("addIQRFData - start: data={}", Arrays.toString(data));
        synchronized(iqrfData){
            PublishableMqttMessage msgToPublish = convertor.toJson(data);
            iqrfData.add(msgToPublish);
        }
        log.debug("addIQRFData - end");
    }

    /**
     * Adds received message to bridging and converts it to correct format 
     * before can be added to queue to iqrf sending.
     * @param msg received message
     */    
    public void addMqqtMessage(MqttMessage msg) {
        log.debug("addMqqtMessage - start: msg={}", msg);
        synchronized(mqttMessages){            
            String msgContent = new String(msg.getPayload());
            try{
                short[] result = convertor.toIQRF(msgContent).getData();
                mqttMessages.add(result);
            }catch(IllegalArgumentException ex){
                log.error("Error while parsing: " + ex.getMessage());                
                // TODO send resonse to server?
            }
        }
        log.debug("addMqqtMessage - end");
    }

    /**
     * Returns availability of mqtt data.
     * @return true, if are data available
     */
    public boolean isAvailableMqttMessage(){
        synchronized(mqttMessages){
            return !mqttMessages.isEmpty();
        }
    }
    
    /**
     * Gets and removes mqtt data from bridge. For checking of data availability 
     * use {@link Bridge#isAvailableMQTTData() }
     * @return mqtt data converted as short array or null in case, that data 
     * cannot be parsed
     */
    public short[] getAndRemoveMqttMessage() {
        log.debug("getAndRemoveMqttMessage - start");
        synchronized(mqttMessages){
            short[] result = mqttMessages.poll();
            log.debug("getAndRemoveMqttMessage - end: {}", Arrays.toString(result));
            return result;
        }
    }

    /**
     * Returns availability of iqrf data.
     * @return true, if are data available
     */
    public boolean isAvailableIQRFData(){
        synchronized(iqrfData){
            return !iqrfData.isEmpty();
        }
    }
    
    /**
     * Gets and removes iqrf data from bridge. For checking of data availability 
     * use {@link Bridge#isAvailableIQRFData() }
     * @return iqrf data converted as {@link PublishableMqttMessage}
     */
    public PublishableMqttMessage getAndRemoveIQRFData() {
        log.debug("getAndRemoveIQRFData - start");
        synchronized(iqrfData){
            PublishableMqttMessage msgToPublish = iqrfData.poll();            
            return msgToPublish;            
        }
    }
    
    /**
     * Free up resources.
     */
    public void destroy(){
        iqrfCommunicator.destroy();
        mqttCommunicator.destroy();
    }
}