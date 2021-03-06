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

package com.microrisc.jlibiqrf.bridge.mqtt;

import com.microrisc.jlibiqrf.bridge.ArgumentChecker;
import com.microrisc.jlibiqrf.bridge.Bridge;
import com.microrisc.jlibiqrf.bridge.MACRecognizer;
import com.microrisc.jlibiqrf.bridge.config.MQTTConfiguration;
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
 * MQTT wrapper implementation customized for using in Bridge.
 * 
 * @author Rostislav Spinar
 * @author Martin Strouhal
 */
public class MQTTCommunicator implements MqttCallback {

    // Private instance variables
    private MqttClient client;
    private final MQTTConfiguration config;
    private MqttConnectOptions conOptions;
    private final Bridge bridge;
    @Deprecated
    private final String mid;
    private String mac;
    private String statsTopicName;
    
    private ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
    private ScheduledFuture<?> dataPushServiceHandler;
    
    // in ms
    private static final int DEFAULT_RECONNECTION_SLEEP_TIME = 3000;
    private Runnable reconnectionRunnable = new Runnable() {
        @Override
        public void run() {
            while(client != null && !client.isConnected()){            
                // Connect to the MQTT server
                log("Reconnecting to " + config.getCompleteAddress() + " with client ID " + client.getClientId());

                conOptions = new MqttConnectOptions();
                conOptions.setCleanSession(false);

                try {
                    client.connect(conOptions);
                } catch (MqttException ex) {
                    log("Reconnecting to " + config.getCompleteAddress() + " with client "
                            + "ID " + client.getClientId() + "failed! " + ex.getMessage());
                }
                try {
                    Thread.sleep(DEFAULT_RECONNECTION_SLEEP_TIME);
                } catch (InterruptedException ex) {
                    log.warn(ex.toString());                    
                }
            }
            reconnectionThread = null;
            log("Connected");        
        }
    };
    private Thread reconnectionThread;
    
    private static final Logger log = LoggerFactory.getLogger(MQTTCommunicator.class);

    /**
     * Constructs an instance of the client wrapper
     *
     * @param mqttConfig the configuration params of the server to connect to
     * @param bridge used for communication bridging
     * @throws MqttException if some error has been occurred
     */
    public MQTTCommunicator(MQTTConfiguration mqttConfig, Bridge bridge, String mid) throws MqttException {
        ArgumentChecker.checkNull(mqttConfig);
        ArgumentChecker.checkNull(bridge);
        ArgumentChecker.checkNull(mid);
        
        this.config = mqttConfig;
        this.bridge = bridge;
        this.mid = mid;   
        
        this.mac = MACRecognizer.getMAC();
        this.statsTopicName = config.getTopicPrefix() + "gateway/" + mac + "/stats";
        
        log.info("Used MAC address " + mac);
        
    	// in this tmpDir are messages temporarily stored until the message has been delivered to the server.
        String tmpDir = System.getProperty("java.io.tmpdir");
        log.info("As java.io.tmpdir used " + tmpDir);
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
        ArgumentChecker.checkNegative(checkInterval);
        Runnable checkAndPushDataRunnable = new Runnable() {
            @Override
            public void run() {
                if(bridge.isAvailableIQRFData()){
                    log.debug("MQTT com thread found available iqrf data. Data will be send to mqtt broker.");
                    PublishableMqttMessage msg = bridge.getAndRemoveIQRFData();
                    
                    
                    try {
                        // publish data
                        /*
                        OLD system dividing msgs into confirmation and response channel
                        if(msg.getType() == DPAReplyType.CONFIRMATION){
                            publish(mid + "/dpa/confirmations", 0, msg.getPayload());
                        }else if(msg.getType() == DPAReplyType.RESPONSE){
                            publish(mid + "/dpa/responses", 0, msg.getPayload());
                        }else{
                            log.warn("Unsupported DPA reply type! Message will be published to responses.");
                            publish(mid + "/dpa/responses", 0, msg.getPayload());
                        }*/
                        
                        publish(config.getTopicPrefix() + "gateway/" + mac + "/rx", 0, msg.getPayload());
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
        ArgumentChecker.checkNull(topicName);
        ArgumentChecker.checkInterval(qos, 0, 2);
        ArgumentChecker.checkNull(payload);
        
        String time = new Timestamp(System.currentTimeMillis()).toString();
        log("Publishing at: " + time + " to topic \"" + topicName + "\" qos " + qos);

        // Create and configure a message
        MqttMessage message = new MqttMessage(payload);
        message.setQos(qos);

    	// Send the message to the server, control is not returned until
        // it has been delivered to the server meeting the specified
        // quality of service.
        client.publish(topicName, message);
    }
    
    /**
     * Subscribe to a topic by MAC address on an MQTT server Once subscribed 
     * this method waits for the messages to arrive from the server that match 
     * the subscription.
     * It continues listening for messages until the enter key is pressed.
     *
     * @param qos the maximum quality of service to receive messages at for this
     * subscription
     * @throws MqttException
     */
    public void subscribeBridgeDefault(int qos) throws MqttException {
        subscribe(config.getTopicPrefix() + "gateway/" + mac + "/tx", qos);
        subscribe(config.getTopicPrefix() + "gateway/" + mac + "/stats", qos);
    }
    
    /**
     * Subscribe to a topic on an MQTT server Once subscribed this method waits
     * for the messages to arrive from the server that match the subscription.
     * It continues listening for messages until the enter key is pressed.
     *
     * @param topicName to subscribeBridgeDefault to (can be wild carded)
     * @param qos the maximum quality of service to receive messages at for this
     * subscription
     * @throws MqttException
     */
    public void subscribe(String topicName, int qos) throws MqttException {
        ArgumentChecker.checkNull(topicName);
        ArgumentChecker.checkInterval(qos, 0, 2);
        
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
    @Override
    public void connectionLost(Throwable cause) {
        log.debug("connectionLost - start: cause=" + cause.getMessage());        
	// Called when the connection to the server has been lost.
        // An application may choose to implement reconnection
        // logic at this point. This sample simply exits.
        log("Connection to " + config.getCompleteAddress() + " lost! " + cause);
        reconnectionThread = new Thread(reconnectionRunnable);
        reconnectionThread.start();
        log.debug("connectionLost - end");               
    }

    /**
     * @see MqttCallback#deliveryComplete(IMqttDeliveryToken)
     */
    @Override
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
     * @throws org.eclipse.paho.client.mqttv3.MqttException
     * @see MqttCallback#messageArrived(String, MqttMessage)
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws MqttException {
	ArgumentChecker.checkNull(message);
        
        // Called when a message arrives from the server that matches any
        // subscription made by the client
        
        if(topic.compareTo(statsTopicName) == 0){            
                sendStatistics();
        }else{
            String time = new Timestamp(System.currentTimeMillis()).toString();
            System.out.println("Time:\t" + time
                               + "  Topic:\t" + topic
                               + "  Message:\t" + new String(message.getPayload())
                               + "  QoS:\t" + message.getQos());

            bridge.addMqqtMessage(message);

            log.info("Message: '" + new String(message.getPayload())
                    + "' published to internal DPA Queue with topic [" + topic + "]");
        }
    }    
    
    /**
     * <p>Creates an InputStream from a file, and fills it with the complete
     * file. Thus, available() on the returned InputStream will return the
     * full number of bytes the file contains</p>
     * @param fname The filename
     * @return The filled InputStream
     * @exception IOException, if the Streams couldn't be created.
     **/
    private InputStream fullStream(String fname) throws IOException {
        ArgumentChecker.checkNull(fname);
        InputStream is = this.getClass().getResourceAsStream(fname);
        //FileInputStream fis = new FileInputStream(fname);
        
        DataInputStream dis = new DataInputStream(is);
        byte[] bytes = new byte[dis.available()];
        dis.readFully(bytes);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        return bais;
    }

    private void sendStatistics(){
        String stats = bridge.getStatistics().getAsJson();
        try {
            publish(statsTopicName, 0, stats.getBytes());
        } catch (MqttException ex) {
            log.warn("Statistics sending was unsuccessful: " + ex);
        }
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
        if(reconnectionThread != null){
            reconnectionThread.interrupt();
        }
    }
}