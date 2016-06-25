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


import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Rostislav Spinar
 * @author Martin Strouhal
 */
public abstract class MQTTCommunicationHandler implements MqttCallback, CommunicationHandler<MqttMessage> {
    
    /** Logger.*/
    private static final Logger log = LoggerFactory.getLogger(MQTTCommunicationHandler.class);

    /** Default Quality of Service used for sent communication */
    public static final int DEFAULT_MQTT_QUALITY_OF_SERVICE = 2;
    
    /** Default value for {@link MQTTCommunicationHandler#timeoutInterval} */
    public static final int DEFAULT_TIMEOUT_INTERVAL = 50000;
    /** Default value for {@link MQTTCommunicationHandler#dataCheckAndPushInterval} */
    public static final int DEFAULT_CHECKANDPUSH_INTERVAL = 500;

    /** Address or hostname of Mqtt broker */
    protected String mqttBrokerEndPoint;
    /** Subscribed topics */
    protected List<String> subscribeTopics;
    /** Timeout for unsuccessful operating (eg. repeating of connecting) */
    protected int timeoutInterval;
    protected int dataCheckAndPushInterval = DEFAULT_CHECKANDPUSH_INTERVAL;
    
    private MqttClient client;
    /** ID of client used while Mqtt communication */
    private String clientId;
    private MqttConnectOptions options;
    private String clientWillTopic;

    /**
     * Constructor for the MQTTCommunicationHandler which takes in the owner,
     * type of the device and the MQTT Broker URL and the topic to subscribe.
     *
     * @param clientId the owner of the devices.
     * @param mqttBrokerEndPoint the IP/URL of the MQTT broker endpoint.
     * @param subscribeTopics the MQTT topics to which the client is to be
     * subscribed
     */
    protected MQTTCommunicationHandler(String clientId, String mqttBrokerEndPoint,
            List<String> subscribeTopics) {
        this.clientId = clientId;
        this.mqttBrokerEndPoint = mqttBrokerEndPoint;
        this.subscribeTopics = subscribeTopics;
        this.clientWillTopic = clientId + File.separator + "disconnection";
        this.timeoutInterval = DEFAULT_TIMEOUT_INTERVAL;
        this.initSubscriber();
    }
    
    /** Setter for {@link MQTTCommunicationHandler#timeoutInterval}.
     * 
     * @param timeoutInterval in [ms]
     */
    public void setTimeoutInterval(int timeoutInterval) {
        this.timeoutInterval = timeoutInterval;
    }

    /**
     * Initializes the MQTT-Client. Creates a client using the given MQTT-broker
     * endpoint and the clientId. Also sets the client's options parameter
     * with the clientWillTopic (in-case of connection failure) and other info.
     * Also sets the call-back this current class.
     */
    private void initSubscriber() {
        try {
            client = new MqttClient(this.mqttBrokerEndPoint, clientId, null);
            log.info("MQTT subscriber was created with ClientID : " + clientId);
        } catch (MqttException ex) {
            String errorMsg = "MQTT Client Error\n" + "\tReason:  " + ex.getReasonCode()
                    + "\n\tMessage: " + ex.getMessage() + "\n\tLocalMsg: "
                    + ex.getLocalizedMessage() + "\n\tCause: " + ex.getCause()
                    + "\n\tException: " + ex;
            log.error(errorMsg);
        }

        options = new MqttConnectOptions();
        options.setCleanSession(false);
        options.setWill(clientWillTopic, "Connection-Lost".getBytes(StandardCharsets.UTF_8), 2,
                true);
        client.setCallback(this);
    }
    
    /**
     * Connects to the MQTT-Broker and if successfully established connection.
     *
     * @throws CommunicationHandlerException in the event of 'Connecting to' the
     * MQTT broker fails.
     */
    protected void connectToBroker() throws CommunicationHandlerException {
        log.debug("connectToBroker - start");
        try {
            client.connect(options);

            if (log.isDebugEnabled()) {
                log.debug("Subscriber connected to queue at: " + this.mqttBrokerEndPoint);
            }
        } catch (MqttSecurityException ex) {
            String errorMsg = "MQTT Security Exception when connecting to queue\n" + "\tReason: "
                    + " "
                    + ex.getReasonCode() + "\n\tMessage: " + ex.getMessage()
                    + "\n\tLocalMsg: " + ex.getLocalizedMessage() + "\n\tCause: "
                    + ex.getCause() + "\n\tException: " + ex;
            if (log.isDebugEnabled()) {
                log.debug(errorMsg);
            }
            throw new CommunicationHandlerException(errorMsg, ex);

        } catch (MqttException ex) {
            String errorMsg = "MQTT Exception when connecting to queue\n" + "\tReason:  "
                    + ex.getReasonCode() + "\n\tMessage: " + ex.getMessage()
                    + "\n\tLocalMsg: " + ex.getLocalizedMessage() + "\n\tCause: "
                    + ex.getCause() + "\n\tException: " + ex;
            if (log.isDebugEnabled()) {
                log.debug(errorMsg);
            }
            throw new CommunicationHandlerException(errorMsg, ex);
        }
        log.debug("connectToBroker - end");
    }
    
    /**
     * Subscribes to the MQTT-Topics specific to this MQTTClient. (The MQTT-Topics
     * specific to the device is taken in as a constructor parameter of this
     * class) .
     *
     * @throws CommunicationHandlerException in the event of 'Subscribing to'
     * the MQTT broker fails.
     */
    protected void subscribeToBroker() throws CommunicationHandlerException {
        
        Iterator it = subscribeTopics.iterator();
        
        String subscribeTopic = "";
        while(it.hasNext()) {
            
            subscribeTopic = (String)it.next();
            
            try {
                client.subscribe(subscribeTopic, 2);
                log.info("Subscriber '" + clientId + "' subscribed to topic: " + subscribeTopic);
            } catch (MqttException ex) {
                String errorMsg = "MQTT Exception when trying to subscribe to topic: "
                        + subscribeTopics + "\n\tReason:  " + ex.getReasonCode()
                        + "\n\tMessage: " + ex.getMessage() + "\n\tLocalMsg: "
                        + ex.getLocalizedMessage() + "\n\tCause: " + ex.getCause()
                        + "\n\tException: " + ex;
                if (log.isDebugEnabled()) {
                    log.debug(errorMsg);
                }

                throw new CommunicationHandlerException(errorMsg, ex);
            }
        }
    }
    
    /**
     * This method is used to publish data and reply-messages for the control 
     * signals received. Invocation of this method calls its overloaded-method 
     * with a QoS equal to that of the default value.
     *
     * @param topic the topic to which the reply message is to be published.
     * @param payload the reply-message (payload) of the MQTT publish action.
     * 
     * @throws CommunicationHandlerException if some error has been occurred
     */
    protected void publishToBroker(String topic, String payload)
            throws CommunicationHandlerException {
        publishToBroker(topic, payload, DEFAULT_MQTT_QUALITY_OF_SERVICE, true);
    }
    
    /**
     * This is an overloaded method that publishes MQTT data and reply-messages 
     * for control signals received form the IoT-WebServer.
     *
     * @param topic the topic to which the reply message is to be published
     * @param payload the reply-message (payload) of the MQTT publish action.
     * @param qos the Quality-of-Service of the current publish action. Could be
     * 0(At-most once), 1(At-least once) or 2(Exactly once)
     * @param retained whether or not this message should be retained by the server
     * 
     * @throws CommunicationHandlerException if some error has been occurred
     */
    protected void publishToBroker(String topic, String payload, int qos, boolean retained)
            throws CommunicationHandlerException {
        try {
            client.publish(topic, payload.getBytes(StandardCharsets.UTF_8), qos, retained);
            if (log.isDebugEnabled()) {
                log.debug("Message: " + payload + " to MQTT topic [" + topic
                        + "] published successfully");
            }
        } catch (MqttException ex) {
            String errorMsg
                    = "MQTT Client Error" + "\n\tReason:  " + ex.getReasonCode() + "\n\tMessage: "
                    + ex.getMessage() + "\n\tLocalMsg: " + ex.getLocalizedMessage()
                    + "\n\tCause: " + ex.getCause() + "\n\tException: " + ex;
            log.info(errorMsg);
            throw new CommunicationHandlerException(errorMsg, ex);
        }
    }

    /** Publish message to Mqtt broker.
     * 
     * @param topic in which will be data published
     * @param message to publish
     * @throws CommunicationHandlerException if some error has been occurred
     */
    protected void publishToBroker(String topic, MqttMessage message)
            throws CommunicationHandlerException {
        try {
            client.publish(topic, message);
            if (log.isDebugEnabled()) {
                log.debug("Message: " + message.toString() + " to MQTT topic [" + topic
                        + "] published successfully");
            }
        } catch (MqttException ex) {
            String errorMsg
                    = "MQTT Client Error" + "\n\tReason:  " + ex.getReasonCode() + "\n\tMessage: "
                    + ex.getMessage() + "\n\tLocalMsg: " + ex.getLocalizedMessage()
                    + "\n\tCause: " + ex.getCause() + "\n\tException: " + ex;
            log.info(errorMsg);
            throw new CommunicationHandlerException(errorMsg, ex);
        }
    }
    
    /**
     * Checks whether the connection to the MQTT-Broker persists.
     *
     * @return true if the client is connected to the MQTT-Broker, else false.
     */
    @Override
    public boolean isConnected() {
        return client.isConnected();
    }
    
    /**
     * Callback method which is triggered once the MQTT client loses its
     * connection to the broker. Spawns a new thread that executes necessary
     * actions to try and reconnect to the endpoint.
     *
     * @param throwable a Throwable Object containing the details as to why the
     * failure occurred.
     */
    @Override
    public void connectionLost(Throwable throwable) {
        log.warn("Lost Connection for client: " + this.clientId
                + " to " + this.mqttBrokerEndPoint + ".\nThis was due to - "
                + throwable.getMessage());

        Thread reconnectThread = new Thread() {
            public void run() {
                connect();
            }
        };
        reconnectThread.setDaemon(true);
        reconnectThread.start();
    }
        
    /**
     * Callback method which is triggered upon receiving a MQTT Message from the
     * broker. Spawns a new thread that executes any actions to be taken with
     * the received message.
     *
     * @param topic the MQTT-Topic to which the received message was published
     * to and the client was subscribed to.
     * @param mqttMessage the actual MQTT-Message that was received from the
     * broker.
     */
    @Override
    public void messageArrived(final String topic, final MqttMessage mqttMessage) {
        if (log.isDebugEnabled()) {
            log.info("Got an MQTT message '" + mqttMessage.toString() + "' for topic '" + topic
                    + "'.");
        }

        Thread messageProcessorThread = new Thread() {
            public void run() {
                processIncomingMessage(topic, mqttMessage);
            }
        };
        messageProcessorThread.setDaemon(true);
        messageProcessorThread.start();
    }


    /**
     * Callback method which gets triggered upon successful completion of a
     * message delivery to the broker.
     *
     * @param iMqttDeliveryToken the MQTT-DeliveryToken which includes the
     * details about the specific message delivery.
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        String message = "";
        try {
            message = iMqttDeliveryToken.getMessage().toString();
        } catch (MqttException e) {
            log.error(
                    "Error occurred whilst trying to read the message from the MQTT delivery "
                    + "token.");
        }
        String topic = iMqttDeliveryToken.getTopics()[0];
        String client = iMqttDeliveryToken.getClient().getClientId();

        if (log.isDebugEnabled()) {
            log.debug("Message - '" + message + "' of client [" + client + "] for the topic ("
                    + topic
                    + ") was delivered successfully.");
        }
    }

    /**
     * Closes the connection to the MQTT Broker.
     * 
     * @throws org.eclipse.paho.client.mqttv3.MqttException if some error has 
     * been occurred
     */
    public void closeConnection() throws MqttException {
        if (client != null && isConnected()) {
            client.disconnect();
        }
    }
}