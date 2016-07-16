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

import com.microrisc.jlibiqrf.demos.Bridge;
import com.microrisc.jlibiqrf.demos.config.MQTTConfiguration;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.sql.Timestamp;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Rostislav Spinar
 */
public class MQTTCommunicator implements MqttCallback {

    // Private instance variables
    private MqttClient client;
    private final MQTTConfiguration config;
    private MqttConnectOptions conOptions;
    private final Bridge bridge;
    
    private ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
    private ScheduledFuture<?> dataPushServiceHandler;
    
    private static final Logger log = LoggerFactory.getLogger(MQTTCommunicator.class);

    /**
     * Constructs an instance of the sample client wrapper
     *
     * @param MQTTConfig the configuration params of the server to connect to
     * @param bridge used for communication bridging
     * @throws MqttException
     */
    public MQTTCommunicator(MQTTConfiguration mqttConfig, Bridge bridge) throws MqttException {
        this.config = mqttConfig;
        this.bridge = bridge;
        
    	//This sample stores in a temporary directory... where messages temporarily
        // stored until the message has been delivered to the server.
        //..a real application ought to store them somewhere
        // where they are not likely to get deleted or tampered with
        String tmpDir = System.getProperty("java.io.tmpdir");
        MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);

        try {
            // Construct the connection options object that contains connection parameters
            // such as cleanSession and LWT
            conOptions = new MqttConnectOptions();
            conOptions.setCleanSession(config.isCleanSession());
            
            if(config.isSSL()){
                conOptions.setPassword(config.getPassword().toCharArray());
                conOptions.setUserName(config.getUserName());
                
                CertificateFactory cf = CertificateFactory.getInstance("X.509");

                InputStream certFileInputStream = fullStream(config.getCertFilePath());
                Certificate ca = cf.generateCertificate(certFileInputStream);

                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(null);
                keyStore.setCertificateEntry("ca", ca);

                TrustManagerFactory trustManagerFactory = TrustManagerFactory.
                        getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);

                SSLContext sslContext = SSLContext.getInstance("TLSv1");
                sslContext.init(null, trustManagerFactory.getTrustManagers(),
                                new SecureRandom());
                conOptions.setSocketFactory(sslContext.getSocketFactory());
            }

            // Construct an MQTT blocking mode client
            client = new MqttClient(config.getCompleteAddress(), 
                    mqttConfig.getClientId(), dataStore);

            // Set this wrapper as the callback handler
            client.setCallback(this);
            
            // Connect to the MQTT server
            log("Connecting to " + config.getCompleteAddress() + 
                    " with client ID " + client.getClientId());
            
            client.connect(conOptions);
            log("Connected");

        } catch (MqttException e) {
            e.printStackTrace();
            log("Unable to set up client: " + e.toString());
            throw new RuntimeException("MQTT communicator cannot be created.");
        } catch (CertificateException e) {
            e.printStackTrace();
            log("Unable to set up client - certificate exception: " + e.toString());
            throw new RuntimeException("MQTT communicator cannot be created.");
        } catch (IOException e) {
            e.printStackTrace();
            log("Unable to set up client - certificate exception in input stream: " + e.toString());
            throw new RuntimeException("MQTT communicator cannot be created.");
        } catch (KeyStoreException e) {
            e.printStackTrace();
            log("Unable to set up client - certificate exception in key store: " + e.toString());
            throw new RuntimeException("MQTT communicator cannot be created.");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            log("Unable to set up client - certificate exception in loading key store: " + e.toString());
            throw new RuntimeException("MQTT communicator cannot be created.");
        } catch (KeyManagementException e) {
            e.printStackTrace();
            log("Unable to set up client - certificate exception in ssl context: " + e.toString());
            throw new RuntimeException("MQTT communicator cannot be created.");
        }
    }

    /** Runs thread which is checking new messages to publish.
     * 
     * @param checkInterval how often is checking (in ms)
     */
    public void checkAndPublishDeviceData(int checkInterval) {
        Runnable checkAndPushDataRunnable = new Runnable() {
            @Override
            public void run() {
                if(bridge.isAvailableIQRFData()){
                    log.debug("MQTT com thread found available iqrf data. Data will be send to mqtt broker.");
                    MqttMessage msg = bridge.getAndRemoveIQRFData();
                    
                    System.out.println("msg: " + msg);
                    
                    try {
                        // publish data
                        publish("coordinator-mid/dpa/responses", 0, msg.getPayload());
                    } catch (MqttException ex) {
                        log.error(ex.getMessage());
                    }
                    
                    log.debug("Data published");
                }
            }
        };

        dataPushServiceHandler = service.scheduleAtFixedRate(checkAndPushDataRunnable, checkInterval,
                checkInterval, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Publish / send a message to an MQTT server
     *
     * @param topicName the name of the topic to publish to
     * @param qos the quality of service to delivery the message at (0,1,2)
     * @param payload the set of bytes to send to the MQTT server
     * @throws MqttException
     */
    public void publish(String topicName, int qos, byte[] payload) throws MqttException {

        // Connect to the MQTT server
        //log("Connecting to " + brokerUrl + " with client ID " + client.getClientId());
        //client.connect(conOpt);
        //log("Connected");

        String time = new Timestamp(System.currentTimeMillis()).toString();
        log("Publishing at: " + time + " to topic \"" + topicName + "\" qos " + qos);

        // Create and configure a message
        MqttMessage message = new MqttMessage(payload);
        message.setQos(qos);

    	// Send the message to the server, control is not returned until
        // it has been delivered to the server meeting the specified
        // quality of service.
        client.publish(topicName, message);

        // Disconnect the client
        //client.disconnect();
        //log("Disconnected");
    }

    /**
     * Subscribe to a topic on an MQTT server Once subscribed this method waits
     * for the messages to arrive from the server that match the subscription.
     * It continues listening for messages until the enter key is pressed.
     *
     * @param topicName to subscribe to (can be wild carded)
     * @param qos the maximum quality of service to receive messages at for this
     * subscription
     * @throws MqttException
     */
    public void subscribe(String topicName, int qos) throws MqttException {
        
        // Connect to the MQTT server
        //client.connect(conOpt);
        //log("Connected to " + brokerUrl + " with client ID " + client.getClientId());

    	// Subscribe to the requested topic
        // The QoS specified is the maximum level that messages will be sent to the client at.
        // For instance if QoS 1 is specified, any messages originally published at QoS 2 will
        // be downgraded to 1 when delivering to the client but messages published at 1 and 0
        // will be received at the same level they were published at.
        log("Subscribing to topic \"" + topicName + "\" qos " + qos);
        client.subscribe(topicName, qos);

        // Disconnect the client from the server
        //client.disconnect();
        //log("Disconnected");
    }

    /**
     * Utility method to handle logging. If 'quietMode' is set, this method does
     * nothing
     *
     * @param message the message to log
     */
    private void log(String message) {
        if (!config.isQuiteMode()) {
            System.out.println(message);
        }
    }

    /**
     * @see MqttCallback#connectionLost(Throwable)
     */
    public void connectionLost(Throwable cause) {
	
        // Called when the connection to the server has been lost.
        // An application may choose to implement reconnection
        // logic at this point. This sample simply exits.
        log("Connection to " + config.getCompleteAddress() + " lost!" + cause);

        // Connect to the MQTT server
        log("Reconnecting to " + config.getCompleteAddress() + " with client ID " + client.getClientId());
        
        conOptions = new MqttConnectOptions();
        conOptions.setCleanSession(false);
        
        try {
            client.connect(conOptions);
        } catch (MqttException ex) {
            log("Reconnecting to " + config.getCompleteAddress() + " with client "
                    + "ID " + client.getClientId() + "failed!" + ex.getMessage());
        }
        
        log("Connected");
    }

    /**
     * @see MqttCallback#deliveryComplete(IMqttDeliveryToken)
     */
    public void deliveryComplete(IMqttDeliveryToken token) {
        
        // Called when a message has been delivered to the
        // server. The token passed in here is the same one
        // that was passed to or returned from the original call to publish.
        // This allows applications to perform asynchronous
        // delivery without blocking until delivery completes.
        //
        // This sample demonstrates asynchronous deliver and
        // uses the token.waitForCompletion() call in the main thread which
        // blocks until the delivery has completed.
        // Additionally the deliveryComplete method will be called if
        // the callback is set on the client
        //
        // If the connection to the server breaks before delivery has completed
        // delivery of a message will complete after the client has re-connected.
        // The getPendingTokens method will provide tokens for any messages
        // that are still to be delivered.
    }

    /**
     * @see MqttCallback#messageArrived(String, MqttMessage)
     */
    public void messageArrived(String topic, MqttMessage message) throws MqttException {
	
        // Called when a message arrives from the server that matches any
        // subscription made by the client
        
        String time = new Timestamp(System.currentTimeMillis()).toString();
        System.out.println("Time:\t" + time
                           + "  Topic:\t" + topic
                           + "  Message:\t" + new String(message.getPayload())
                           + "  QoS:\t" + message.getQos());
        
        // get data as string
        final String msg = new String(message.getPayload());
        
        //String resultToBeSent = null;
        //resultToBeSent = OpenGateway.sendDPAWebRequest(topic, msg);

        bridge.addMqqtMessage(message);

        log.info("Message: '" + new String(message.getPayload())
                + "' published to internal DPA Queue with topic [" + topic + "]");

/*
        if(resultToBeSent != null) {
            publish(Topics.ACTUATORS_RESPONSES_LEDS, 2, resultToBeSent.getBytes());
        }
*/
        
    }
    
    /**
     * <p>Creates an InputStream from a file, and fills it with the complete
     * file. Thus, available() on the returned InputStream will return the
     * full number of bytes the file contains</p>
     * @param fname The filename
     * @return The filled InputStream
     * @exception IOException, if the Streams couldn't be created.
     **/
    private InputStream fullStream( String fname ) throws IOException {
        InputStream is = this.getClass().getResourceAsStream(fname);
        //FileInputStream fis = new FileInputStream(fname);
        
        DataInputStream dis = new DataInputStream(is);
        byte[] bytes = new byte[dis.available()];
        dis.readFully(bytes);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        return bais;
    }
    
    /** Free-up resources. */
    public void destroy(){
        if(!dataPushServiceHandler.isCancelled()) {
            dataPushServiceHandler.cancel(false);
        }
        if (!client.isConnected()) {
            try {
                client.disconnect();
            } catch (MqttException ex) {
                log.warn(ex.getMessage());
            }
        }
    }
}