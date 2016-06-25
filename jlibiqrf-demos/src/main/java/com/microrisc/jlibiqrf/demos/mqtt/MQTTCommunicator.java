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

package com.microrisc.jlibiqrf.demos.mqtt;

import com.microrisc.jlibiqrf.demos.AppLogic;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Rostislav Spinar
 */
public class MQTTCommunicator extends MQTTCommunicationHandler {
    
    /** Logger.*/
    private static final Logger log = LoggerFactory.getLogger(MQTTCommunicator.class);
    
    private ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
    private ScheduledFuture<?> dataPushServiceHandler;
    private AppLogic appLogic;
    
    /** Creates instance of {@link MQTTCommunicator}.
     * 
     * @param clientId which will be used for connection
     * @param mqttBrokerEndPoint like address for connection
     * @param subscribeTopics which will be subscribed
     * @param logic which us used for central communication
     */
    public MQTTCommunicator(String clientId, String mqttBrokerEndPoint, 
            List<String> subscribeTopics, AppLogic logic
    ) {
        super(clientId, mqttBrokerEndPoint, subscribeTopics);
        appLogic = logic;
        
    }
    
    /** Returns {@link MQTTCommunicator#dataPushServiceHandler}.
     * 
     * @return instance of data push service handler
     */
    public ScheduledFuture<?> getDataPushServiceHandler() {
        return dataPushServiceHandler;
    }
    
    @Override
    public void connect() {
        Runnable connector = new Runnable() {
            public void run() {
                // start up if not up and running already
                while (!isConnected()) {
                    try {
                        connectToBroker();
                        subscribeToBroker();
                        checkAndPublishDeviceData(dataCheckAndPushInterval);

                    } catch (CommunicationHandlerException e) {
                        log.warn("Connection/Subscription to MQTT Broker at: "
                                + mqttBrokerEndPoint + " failed");

                        try {
                            log.debug("Going to sleep during connecting pause");
                            Thread.currentThread().sleep(timeoutInterval);
                            log.debug("Going from sleep during connectin pause");
                        } catch (InterruptedException ex) {
                            log.error("MQTT-Subscriber: Thread Sleep Interrupt Exception");
                        }
                    }
                }
            }
        };

        Thread connectorThread = new Thread(connector);
        connectorThread.setName("ConnectorThread");
        connectorThread.setDaemon(true);
        connectorThread.start();
    }
    
    @Override
    public void checkAndPublishDeviceData(int checkInterval) {
        Runnable checkAndPushDataRunnable = new Runnable() {
            @Override
            public void run() {
                if(appLogic.isAvailableIQRFData()){
                    log.debug("MQTT com thread found available iqrf data. Data will be send to mqtt broker.");
                    MqttMessage msg = appLogic.getAndRemoveIQRFData();
                    
                    System.out.println("msg: " + msg);
                    
                    // publish data
                    publishDeviceData("coordinator-mid/dpa/requests", new String(msg.getPayload()));
                    
                    log.debug("Data published");
                }
            }
        };

        dataPushServiceHandler = service.scheduleAtFixedRate(checkAndPushDataRunnable, checkInterval,
                checkInterval, TimeUnit.MILLISECONDS);
    }
 
    /** Publish specified message into the specified topic.
     * 
     * @param topic in which will be message published
     * @param message to publish
     */
    private void publishDeviceData(String topic, String message) {        
        
        MqttMessage pushMessage = new MqttMessage();
        pushMessage.setPayload(message.getBytes(StandardCharsets.UTF_8));
        pushMessage.setQos(DEFAULT_MQTT_QUALITY_OF_SERVICE);
        pushMessage.setRetained(true);

        try {
            publishToBroker(topic, pushMessage);
            log.info("Message: '" + pushMessage
                    + "' published to MQTT Queue at ["
                    + mqttBrokerEndPoint
                    + "] under topic [" + topic + "]");

        } catch (CommunicationHandlerException e) {
            log.warn("Data publish attempt to topic - ["
                    + topic + "] failed for payload ["
                    + message + "]");
        }
    }
    
    @Override
    public void processIncomingMessage(String topic, MqttMessage message) {
        appLogic.addMqqtMessage(message);

        log.info("Message: '" + new String(message.getPayload())
                + "' published to internal DPA Queue with topic [" + topic + "]");
    }
    
    @Override
    public void disconnect() {
        Runnable stopConnection = new Runnable() {
            public void run() {
                while (isConnected()) {
                    try {
                        dataPushServiceHandler.cancel(true);
                        closeConnection();

                    } catch (MqttException e) {
                        if (log.isDebugEnabled()) {
                            log.warn("Unable to 'STOP' MQTT connection at broker at: "
                                    + mqttBrokerEndPoint);
                        }

                        try {
                            Thread.sleep(timeoutInterval);
                        } catch (InterruptedException e1) {
                            log.error("MQTT-Terminator: Thread Sleep Interrupt Exception");
                        }
                    }
                }
            }
        };

        Thread terminatorThread = new Thread(stopConnection);
        terminatorThread.setName("TerminatorThread");
        terminatorThread.setDaemon(true);
        terminatorThread.start();
    }
}
