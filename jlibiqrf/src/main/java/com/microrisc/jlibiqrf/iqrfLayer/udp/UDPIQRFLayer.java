/* 
 * Copyright 2015 MICRORISC s.r.o.
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
package com.microrisc.jlibiqrf.iqrfLayer.udp;

import com.microrisc.jlibiqrf.iqrfLayer.AbstractIQRFLayer;
import com.microrisc.jlibiqrf.iqrfLayer.udp.gweth.GWETH_DataTransformer;
import com.microrisc.jlibiqrf.types.IQRFData;
import com.microrisc.jlibiqrf.types.IQRFError;
import com.microrisc.jlibiqrf.types.IQRFLayerException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements network layer built on UDP communication. The layer acts as a UDP
 * client. This network layer is specialized to only usage with GW-ETH-01
 * gateway.
 * <p>
 * Only one specified network is supported. The specification of supported
 * network is done in constructor using information from user ( remote address
 * and port ) - from this information is created connection information for the
 * layer. If a connection information supplied by incoming request is not equal
 * to the one of this layer, error is issued.
 *
 * @author Michal Konopa
 * @author Martin Strouhal
 */
// December 2015 - redesigned for JLibIQRF
public final class UDPIQRFLayer extends AbstractIQRFLayer {

    /** Logger. */
    private static final Logger logger = LoggerFactory.getLogger(UDPIQRFLayer.class);

    /** Local IP address to bind. */
    private InetAddress localAddress = null;

    /** Local port number. */
    private int localPort = -1;

    /** Server IP address to which the requests will be sent. */
    private InetAddress targetAddress = null;

    /** Remote port number. */
    private int remotePort = -1;

    /** Socket for communication with server. */
    private DatagramSocket socket = null;

    /** Synchronization to socket access. */
    private final Object socketSynchro = new Object();


    /** Default timeout [in ms] of blocking waiting for reception of packet from
     * the socket. */
    public static int RECEPTION_TIMEOUT_DEFAULT = 100;

    /** Default maximal size of received packets [in bytes]. */
    public static int MAX_RECEIVED_PACKET_SIZE = 500;

    /** Timeout [in ms] of blocking waiting for reception of packet from the
     * socket. */
    private int receptionTimeout;

    /** Maximal size of received packets [in bytes]. */
    private int maxRecvPacketSize;


    /** Data received from socket. */
    private Queue<short[]> dataFromSocket = null;

    /** Synchronization between socket reader thread and listener caller thread. */
    private final Object threadSynchro = new Object();


    /**
     * Reading data from connected socket.
     */
    private class SocketReader extends Thread {

        // extracts data from specified packet and returns it
        private short[] extractDataFromSocket(DatagramPacket packet) {
            byte[] packetData = packet.getData();
            short[] extractedData = new short[packet.getLength()];

            for (int item = 0; item < packet.getLength(); item++) {
                extractedData[item] = (short) (packetData[item] & 0xFF);
            }
            return extractedData;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[maxRecvPacketSize];
            DatagramPacket recvPacket = new DatagramPacket(buffer, buffer.length);
            boolean newDataReceived = false;

            while (true) {
                if (this.isInterrupted()) {
                    logger.info("Socket reader thread interrupted");
                    return;
                }

                // receive data from socket
                try {
                    synchronized (socketSynchro) {
                        socket.receive(recvPacket);
                    }
                    newDataReceived = true;
                } catch (SocketTimeoutException ex) {
                    logger.debug("Timeout expired");
                } catch (IOException ex) {
                    logger.error("Error while receiving message from socket", ex);
                }

                // if new data has received, extract it from the packet and 
                // add it into queue
                if (newDataReceived) {
                    short[] extractedData = extractDataFromSocket(recvPacket);
                    logger.info("New data from socket: {}", extractedData);

                    synchronized (threadSynchro) {
                        dataFromSocket.add(extractedData);
                        threadSynchro.notify();
                    }
                }

                newDataReceived = false;

                // sleep for a while to get other threads more processor time 
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    logger.warn("Socket reader thread interrupted while sleeping");
                    return;
                }
            }
        }
    }

    /**
     * Calling listener callback method - when new data has arrived from socket.
     */
    private class ListenerCaller extends Thread {

        // already consumed data from socket
        private Queue<short[]> consumedData = new LinkedList<>();

        // indicates, wheather new data are in socket
        private boolean areDataInSocket() {
            if (dataFromSocket.isEmpty()) {
                return false;
            }
            return true;
        }

        // consume data from socket and adds them into buffer
        private void consumeDataFromSocket() {
            while (!dataFromSocket.isEmpty()) {
                short[] packetData = dataFromSocket.poll();
                consumedData.add(packetData);
            }
        }

        /** Frees up used resources. */
        private void freeResources() {
            consumedData.clear();
        }

        @Override
        public void run() {
            while (true) {
                if (this.isInterrupted()) {
                    logger.info("Client caller thread interrupted");
                    freeResources();
                    return;
                }

                // consuming new data from socket
                synchronized (threadSynchro) {
                    while (!areDataInSocket()) {
                        try {
                            threadSynchro.wait();
                        } catch (InterruptedException ex) {
                            logger.warn("Client caller thread interrupted while "
                                    + "waiting on data from socket.");
                            freeResources();
                            return;
                        }
                    }
                    consumeDataFromSocket();
                }

                // remove data from queue and put send it to listener
                while (!consumedData.isEmpty()) {
                    short[] packetData = consumedData.poll();

                    if (iqrfListener == null) {
                        continue;
                    }

                    boolean isAsync = false;
                    short[] userData = null;

                    try {
                        isAsync = GWETH_DataTransformer.isAsynchronousMessage(packetData);
                        if (isAsync) {
                            userData = GWETH_DataTransformer.getDataFromMessage(packetData);
                        }
                    } catch (Exception e) {
                        logger.error("Error while getting data from message: " + e.getMessage());
                        continue;
                    }

                    if (iqrfListener != null) {
                        iqrfListener.onGetIQRFData(userData);
                    }
                }
            }
        }
    }


    // socket reader thread;
    private Thread socketReader = null;

    // listener caller thread
    private Thread listenerCaller = null;

    // creates and starts threads
    private void createAndStartThreads() {
        socketReader = new SocketReader();
        socketReader.start();

        listenerCaller = new ListenerCaller();
        listenerCaller.start();
    }

    // terminates socket reader and client caller threads
    private void terminateThreads() {
        logger.debug("terminateThreads - start:");

        // termination signal to socket reader thread
        socketReader.interrupt();

        // termination signal to listener caller thread
        listenerCaller.interrupt();

        // Waiting for threads to terminate. Cancelling worker threads has higher 
        // priority than main thread interruption. 
        while (socketReader.isAlive() || listenerCaller.isAlive()) {
            try {
                if (socketReader.isAlive()) {
                    socketReader.join();
                }

                if (listenerCaller.isAlive()) {
                    listenerCaller.join();
                }
            } catch (InterruptedException e) {
                // restoring interrupt status
                Thread.currentThread().interrupt();
                logger.warn("Termination - UDP Client Network Layer interrupted");
            }
        }

        logger.info("UDP Client Network Layer stopped.");
        logger.debug("terminateThreads - end");
    }

    private static int checkMaxRecvPacketSize(int maxRecvPacketSize) {
        if (maxRecvPacketSize <= 0) {
            throw new IllegalArgumentException("Maximal size of received packet "
                    + "cannot be less then or equal to 0");
        }
        return maxRecvPacketSize;
    }

    private static int checkReceptionTimeout(int receptionTimeout) {
        if (receptionTimeout < 0) {
            throw new IllegalArgumentException("Reception timeout cannot be less then 0");
        }
        return receptionTimeout;
    }


    /**
     * Creates new UDP client network layer object.
     *
     * @param localHostName local host name, or {@code null} for the loopback
     * address
     * @param localPort local port number
     * @param remoteHostName remote host name
     * @param remotePort remote port number
     * @param maxRecvPacketSize maximal size of received packets [in bytes].
     * @param receptionTimeout timeout [in ms] of blocking waiting for reception
     * of packet from the socket. {@code 0} means infinity waiting.
     */
    public UDPIQRFLayer(
            String localHostName,
            int localPort,
            String remoteHostName,
            int remotePort,
            int maxRecvPacketSize,
            int receptionTimeout
    ) {
        try {
            this.localAddress = InetAddress.getByName(localHostName);
        } catch (UnknownHostException ex) {
            throw new IllegalArgumentException("Hostname not valid: " + ex.getMessage());
        }
        this.localPort = localPort;

        try {
            this.targetAddress = InetAddress.getByName(remoteHostName);
        } catch (UnknownHostException ex) {
            throw new IllegalArgumentException("Target name not valid: " + ex.getMessage());
        }
        this.remotePort = remotePort;

        this.maxRecvPacketSize = checkMaxRecvPacketSize(maxRecvPacketSize);
        this.receptionTimeout = checkReceptionTimeout(receptionTimeout);
    }

    /**
     * Creates new UDP client network layer object. Maximal received packet size
     * is limited to {@code MAX_RECEIVED_PACKET_SIZE} and blocking waiting for
     * packet reception is set to {@code RECEPTION_TIMEOUT_DEFAULT}.
     *
     * @param localHostName local host name, or {@code null} for the loopback
     * address
     * @param localPort local port number
     * @param remoteHostName remote host name
     * @param remotePort remote port number
     */
    public UDPIQRFLayer(
            String localHostName,
            int localPort,
            String remoteHostName,
            int remotePort
    ) {
        this(localHostName, localPort, remoteHostName, remotePort,
                MAX_RECEIVED_PACKET_SIZE, RECEPTION_TIMEOUT_DEFAULT);
    }

    @Override
    public void startIQRFLayer() throws IQRFLayerException {
        logger.debug("startIQRFLayer - start:");

        try {
            socket = new DatagramSocket(localPort, localAddress);
            socket.setSoTimeout(receptionTimeout);
        } catch (IOException ex) {
            throw new IQRFLayerException(ex, IQRFError.INIT_ERROR);
        }

        // init queue of data comming from socket
        dataFromSocket = new LinkedList<>();

        // creating and starting threads
        createAndStartThreads();

        logger.info("IQRF UDP layer started");
        logger.debug("startIQRFLayer - end");
    }

    @Override
    public void sendData(IQRFData iqrfData) throws IQRFLayerException {
        logger.debug("sendData - start: IQRFData={}", iqrfData);

        // transforms request's data to protocol format defined by GW
        short[] dataForGW = GWETH_DataTransformer.transformRequestData(iqrfData.getData());

        byte[] buf = new byte[dataForGW.length];
        for (int item = 0; item < dataForGW.length; item++) {
            buf[item] = (byte) (dataForGW[item] & 0xFF);
        }

        try {
            DatagramPacket packet = new DatagramPacket(buf, buf.length, targetAddress, remotePort);

            logger.info("Data will be sent to socket...");
            synchronized (socketSynchro) {
                socket.send(packet);
            }
            logger.info("Data successfully sent to socket");
        } catch (IOException ex) {
            logger.error("Sending data to socket failed: " + ex.getMessage());
            throw new IQRFLayerException(ex, IQRFError.SEND_ERROR);
        }

        logger.debug("sendData - end");
    }

    @Override
    public void destroy() {
        super.destroy();
        logger.debug("destroy - start: ");

        terminateThreads();
        socket.close();
        dataFromSocket.clear();

        logger.info("Destroyed");
        logger.debug("destroy - end");
    }
}
