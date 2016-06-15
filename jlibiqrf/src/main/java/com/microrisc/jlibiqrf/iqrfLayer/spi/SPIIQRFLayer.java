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
package com.microrisc.jlibiqrf.iqrfLayer.spi;

import com.microrisc.jlibiqrf.iqrfLayer.AbstractIQRFLayer;
import com.microrisc.jlibiqrf.types.IQRFData;
import com.microrisc.jlibiqrf.types.IQRFError;
import com.microrisc.jlibiqrf.types.IQRFLayerException;
import com.microrisc.rpi.spi.SPI_Exception;
import com.microrisc.rpi.spi.iqrf.SPI_Master;
import com.microrisc.rpi.spi.iqrf.SPI_Status;
import com.microrisc.rpi.spi.iqrf.SimpleSPI_Master;
import java.util.LinkedList;
import java.util.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements IQRF layer using
 * {@code com.microrisc.rpi.spi.iqrf.SimpleSPI_Master} object.
 *
 * @author Rostislav Spinar
 * @author Martin Strouhal
 */
// December 2015 - redesigned for JLibIQRF
public final class SPIIQRFLayer extends AbstractIQRFLayer {

    /** Logger. */
    private static final Logger logger = LoggerFactory.getLogger(SPIIQRFLayer.class);

    /** SPI reader thread */
    private Thread spiReader = null;

    /** listener caller thread */
    private Thread listenerCaller = null;

    /** SPI master */
    private SPI_Master spiMaster = null;

    /** SPI-port name for connection. */
    private String portName = null;

    /** Data received from SPI. */
    private Queue<short[]> dataFromSPI = null;

    /** Synchronization to SPI access. */
    private final Object spiSynchro = new Object();

    /** Synchronization between socket reader thread and listener caller thread. */
    private final Object threadsSynchro = new Object();

    /** Default maximal size of received packets [in bytes]. */
    public static int MAX_RECEIVED_PACKET_SIZE = 128;

    /** Maximal size of received packets [in bytes]. */
    private int maxRecvPacketSize;

    /** Count of attempts if sending is unsuccessful. */
    private int MAX_SENDING_ATTEMPT_COUNT = 3;

    /** Timeout between attempts of sending data if previous attempt was
     * unsuccessful. Timeout is in seconds. */
    private int UNSUCCESSFUL_SENDING_TIMEOUT = 5;

    /** Reading data from SPI. */
    private class SPIReader extends Thread {

        @Override
        public void run() {
            short[] buffer = new short[maxRecvPacketSize];
            boolean newDataReceived = false;
            int dataLen = 0;

            while (true) {

                if (this.isInterrupted()) {
                    logger.info("SPI reader thread interrupted");
                    return;
                }

                try {
                    synchronized (spiSynchro) {
                        SPI_Status spiStatus = spiMaster.getSlaveStatus();
                        //logger.info("Reading thread SPI status: {}", spiStatus.getValue());

                        if (spiStatus.isDataReady()) {
                            logger.info("Data ready!");
                            if (spiStatus.getValue() == 0x40) {
                                dataLen = 64;
                            } else {
                                dataLen = spiStatus.getValue() - 0x40;
                            }

                            buffer = spiMaster.readData(dataLen);
                            newDataReceived = true;
                        }
                    }

                    // if new data has received add it into the queue
                    if (newDataReceived) {
                        logger.info("New data from SPI: {}", buffer);

                        synchronized (threadsSynchro) {
                            dataFromSPI.add(buffer);
                            threadsSynchro.notify();
                        }
                        newDataReceived = false;
                    }

                    Thread.sleep(10);
                } catch (SPI_Exception ex) {
                    logger.error("Error while receiving SPI interface: ", ex);
                } catch (InterruptedException ex) {
                    logger.warn("SPI reader thread interrupted while sleeping.");
                    return;
                }
            }
        }
    }

    /** Calling listener callback method - when new data has arrived from
     * socket. */
    private class ListenerCaller extends Thread {

        // already consumed data from socket
        private Queue<short[]> consumedData = new LinkedList<>();

        // consume data from spi and adds them into buffer
        private void consumeDataFromSPI() {
            while (!dataFromSPI.isEmpty()) {
                short[] packetData = dataFromSPI.poll();
                consumedData.add(packetData);
            }
        }

        /**
         * Frees up used resources.
         */
        private void freeResources() {
            consumedData.clear();
        }

        @Override
        public void run() {
            while (true) {
                if (this.isInterrupted()) {
                    logger.info("SPI caller thread interrupted");
                    freeResources();
                    return;
                }

                // consuming new data from SPI
                synchronized (threadsSynchro) {
                    // until are comng new data from SPI
                    while (dataFromSPI.isEmpty()) {
                        try {
                            threadsSynchro.wait();
                        } catch (InterruptedException ex) {
                            logger.warn("SPI caller thread interrupted while "
                                    + "waiting on data from SPI.");
                            freeResources();
                            return;
                        }
                    }
                    consumeDataFromSPI();
                }

                // remove data from queue and send it to listener
                while (!consumedData.isEmpty()) {
                    short[] userData = consumedData.poll();

                    if (iqrfListener != null) {
                        iqrfListener.onGetIQRFData(userData);
                    }
                }
            }
        }
    }

    // creates and starts threads
    private void createAndStartThreads() {
        spiReader = new SPIReader();
        spiReader.start();

        listenerCaller = new ListenerCaller();
        listenerCaller.start();
    }

    // terminates SPI reader and client caller threads
    private void terminateThreads() {
        logger.debug("terminateThreads - start:");

        // termination signal to socket reader thread
        spiReader.interrupt();

        // termination signal to listener caller thread
        listenerCaller.interrupt();

        // Waiting for threads to terminate. Cancelling worker threads has higher 
        // priority than main thread interruption. 
        while (spiReader.isAlive() || listenerCaller.isAlive()) {
            try {
                if (spiReader.isAlive()) {
                    spiReader.join();
                }

                if (listenerCaller.isAlive()) {
                    listenerCaller.join();
                }
            } catch (InterruptedException e) {
                // restoring interrupt status
                Thread.currentThread().interrupt();
                logger.warn("Termination - SPI IQRF Layer interrupted");
            }
        }

        logger.info("SPI IQRF Layer stopped.");
        logger.debug("terminateThreads - end");
    }

    private static String checkPortName(String portName) {
        if (portName == null) {
            throw new IllegalArgumentException("Port name cannot be null");
        }

        if (portName.equals("")) {
            throw new IllegalArgumentException("Port name cannot be empty string");
        }

        return portName;
    }

    /**
     * Creates new SPI iqrf layer object.
     *
     * @param portName SPI-port name for communication
     */
    public SPIIQRFLayer(String portName) {
        this.portName = checkPortName(portName);
    }

    @Override
    public void startIQRFLayer() throws IQRFLayerException {
        logger.debug("startIQRFLayer - start:");

        try {
            // initialization
            spiMaster = new SimpleSPI_Master(portName);
        } catch (SPI_Exception ex) {
            throw new IQRFLayerException(ex, IQRFError.INIT_ERROR);
        }

        // init queue of data comming from SPI
        dataFromSPI = new LinkedList<>();

        // creating and starting threads
        createAndStartThreads();

        logger.info("SPI IQRF layer started");
        logger.debug("startIQRFLayer - end");
    }

    @Override
    public void sendData(IQRFData iqrfData) throws IQRFLayerException {
        logger.debug("sendData - start: iqrfData={}", iqrfData);

        try {
            logger.info("Data will be sent to SPI...");

            // if attempt count negative, sending was succesful
            int attemptCount = 0;
            while (attemptCount < MAX_SENDING_ATTEMPT_COUNT || attemptCount < 0) {
                synchronized (spiSynchro) {
                    // getting slave status
                    SPI_Status spiStatus = spiMaster.getSlaveStatus();
                    logger.info("Writing thread SPI status: {}", spiStatus.getValue());

                    if (spiStatus.getValue() == SPI_Status.READY_COMM_MODE) {
                        // sending some data to device
                        spiMaster.sendData(iqrfData.getData());
                        logger.info("Data successfully sent to SPI");
                        break;
                    } else {
                        logger.info("Data not sent to SPI, module is not in READY_COMM_MODE.");
                    }
                }
                if (attemptCount < MAX_SENDING_ATTEMPT_COUNT) {
                    logger.info("Sending will be tried again after {} seconds.", UNSUCCESSFUL_SENDING_TIMEOUT);
                    try {
                        Thread.sleep(UNSUCCESSFUL_SENDING_TIMEOUT * 1000);
                    } catch (InterruptedException ex) {
                        logger.debug(ex.toString());
                    }
                }
            }
        } catch (SPI_Exception ex) {
            throw new IQRFLayerException(ex, IQRFError.SEND_ERROR);
        }
        logger.debug("sendData - end");
    }

    @Override
    public void destroy() {
        super.destroy();
        logger.debug("destroy - start: ");

        terminateThreads();
        dataFromSPI.clear();
        spiMaster.destroy();
        spiMaster = null;

        logger.info("Destroyed SPI IQRF layer.");
        logger.debug("destroy - end");
    }
}
