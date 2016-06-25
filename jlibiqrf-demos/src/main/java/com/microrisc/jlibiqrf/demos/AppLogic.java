/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.microrisc.jlibiqrf.demos;

import com.microrisc.jlibiqrf.demos.iqrf.IQRFCommunicator;
import com.microrisc.jlibiqrf.demos.json.TestJsonConvertor;
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
public class AppLogic {

    private static final Logger log = LoggerFactory.getLogger(AppLogic.class);
    
    // queue for messages from MQTT
    private final Queue<MqttMessage> mqttMessages;
    // queue for data from IQRF network
    private final Queue<short[]> iqrfData;
    private final static LinkedList<String> topics = new LinkedList<>(
            Arrays.asList(new String[]{"coordinator-mid/dpa/requests", 
                "coordinator-mid/dpa/confirmations", "coordinator-mid/dpa/responses"
            }));

    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        AppLogic logic = new AppLogic();
     //Thread.sleep(5000);
     //   logic.addIQRFData(new short[]{0x00, 0x01, 0x06, 0x84, 0xFF, 0xFF});
    }
    
    public AppLogic(){
        mqttMessages = new LinkedList<>();
        iqrfData = new LinkedList<>();
        
        IQRFCommunicator iqrfCom = new IQRFCommunicator(this);
        iqrfCom.init();
        
        MQTTCommunicator mqttCom = new MQTTCommunicator("4c774931de1c4e54b27e186eff89ba56",
                "tcp://192.168.153.2:1883", topics, this);
        mqttCom.connect();
        mqttCom.checkAndPublishDeviceData(1000);
        
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
            MqttMessage msg = mqttMessages.poll();
            short[] result = TestJsonConvertor.getInstance().toIQRF(msg);
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
            Object objMsg = TestJsonConvertor.getInstance().toJson(data);
            if(objMsg instanceof MqttMessage){
                log.debug("getAndRemoveIQRFData - end: {}", objMsg);
                return (MqttMessage)objMsg;
            }else{
                log.error("JsonConvertor returned unsupported format of message.");
                return new MqttMessage(new String(Arrays.toString(data)).getBytes());
            }
        }
    }
}