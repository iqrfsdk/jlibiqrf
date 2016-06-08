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
package com.microrisc.jlibiqrf.iqrfLayer.serial;

import com.microrisc.hdlcframing.v2.HDLC_DataTransformer;
import com.microrisc.hdlcframing.v2.HDLC_FormatException;
import com.microrisc.jlibiqrf.iqrfLayer.AbstractIQRFLayer;
import com.microrisc.jlibiqrf.types.IQRFData;
import com.microrisc.jlibiqrf.types.IQRFError;
import com.microrisc.jlibiqrf.types.IQRFLayerException;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements network layer using {@code SerialPort} object.
 * <p>
 * This is an user of jssc library and uses {@code SerialPort} object to read
 * and write data. All data comming from Serial interface is transformed from
 * HDLC packets and forwarder to user's registered network listener. All data
 * designated to underlaying network are transformed to HDLC frames and send via
 * {@code SerialPort.writeBytes} method.
 *
 * @author Rostislav Spinar
 * @author Martin Strouhal
 */
// December 2015 - redesigned for jlibiqrf
public final class SerialIQRFLayerJssc extends AbstractIQRFLayer {

    /** Logger. */
    private static final Logger logger = LoggerFactory.getLogger(SerialIQRFLayerJssc.class);

    /** listener caller thread */
    private Thread listenerCaller = null;

    /** Serial interface */
    private SerialPort serialPort = null;

    /** Serial-port name for connection. */
    private String portName = null;

    /** Serial-baudrate for connection. */
    private int serialBaudrate = 0;

    /** Data received from Serial. */
    private Queue<byte[]> dataFromSerial = null;

    /** Synchronization between socket reader thread and listener caller thread. */
    private final Object threadsSynchro = new Object();

    /** Reading data from Serial. */
    private class SerialReader implements SerialPortEventListener {

        // chunk delimiter
        private static final byte CHUNK_SEPAR = 0x7E;

        // remainder of last data read, which does not comprise a complete chunk
        byte[] dataRemainder = null;

        // reads data chunk from specified position from specified array
        private byte[] readDataChunk(int startPos, byte[] arr) {
            logger.debug("readDataChunk - start: startPos={}, arr={}", startPos, arr);
            if (arr[startPos] != CHUNK_SEPAR) {
                throw new IllegalStateException(
                        "Bad format of input data. It must begin with " + CHUNK_SEPAR + " byte."
                );
            }

            ByteArrayOutputStream dataChunk = new ByteArrayOutputStream();
            for (int pos = startPos; pos < arr.length; pos++) {
                dataChunk.write(arr[pos] & 0xFF);
                if (arr[pos] == CHUNK_SEPAR) {
                    if (pos != startPos) {
                        break;
                    }
                }
            }
            logger.debug("readDataChunk - end: " + Arrays.toString(dataChunk.toByteArray()));
            return dataChunk.toByteArray();
        }

        // writes specified data chunk into global storage
        private void writeDataChunkIntoGlobalStorage(byte[] dataChunk) {
            synchronized (threadsSynchro) {
                dataFromSerial.add(dataChunk);
            }
        }

        public void serialEvent(SerialPortEvent event) {

            // if the event is not reception of bytes, nothing to do
            if (!event.isRXCHAR()) {
                return;
            }

            // if no data has been received, nothing to do
            int dataLen = event.getEventValue();
            if (dataLen <= 0) {
                return;
            }

            // local data buffer to store data for this call only
            byte buffer[] = null;
            try {
                buffer = serialPort.readBytes();
            } catch (SerialPortException ex) {
                System.out.println("Reading data failed: " + ex);
                return;
            }

            byte[] allDataArr = null;

            // if there is data remainder, it is needed to add it for processing
            if (dataRemainder != null) {
                allDataArr = new byte[dataRemainder.length + buffer.length];
                System.arraycopy(dataRemainder, 0, allDataArr, 0, dataRemainder.length);
                System.arraycopy(buffer, 0, allDataArr, dataRemainder.length, buffer.length);
                dataRemainder = null;
            } else {
                allDataArr = new byte[buffer.length];
                System.arraycopy(buffer, 0, allDataArr, 0, buffer.length);
            }

            // list of data chunks - each chunk is embraced by CHUNK_SEPAR bytes
            // with the exception of the last one - it can be without the last CHUNK_SEPAR
            List<byte[]> dataChunks = new LinkedList<>();

            int chunkPos = 0;
            while (chunkPos < (allDataArr.length - 1)) {
                byte[] dataChunk = readDataChunk(chunkPos, allDataArr);
                dataChunks.add(dataChunk);
                chunkPos += dataChunk.length;
            }

            // write all data chunks into the shared storage, with the exception
            // of the last one, if it is incomplete
            boolean lastIsComplete = false;
            for (int chunkId = 0; chunkId < dataChunks.size(); chunkId++) {
                byte[] dataChunk = dataChunks.get(chunkId);

                if (chunkId != (dataChunks.size() - 1)) {
                    writeDataChunkIntoGlobalStorage(dataChunk);
                } else if (dataChunk[dataChunk.length - 1] == CHUNK_SEPAR) {
                    writeDataChunkIntoGlobalStorage(dataChunk);
                    lastIsComplete = true;
                }
            }

            if (lastIsComplete) {
                logger.info("New data from serial interface: {}", dataFromSerial.toArray());

                synchronized (threadsSynchro) {
                    dataChunks.clear();
                    threadsSynchro.notify();
                }
            } else {
                dataRemainder = dataChunks.get(dataChunks.size() - 1);
                dataChunks.clear();
            }
        }
    }

    /**
     * Calling listener callback method - when new data has arrived from socket.
     */
    private class ListenerCaller extends Thread {

        // already consumed data from Serial
        private Queue<byte[]> consumedData = new LinkedList<byte[]>();

        // indicates, wheather new data are from Serial
        private boolean areDataFromSerial() {
            return !dataFromSerial.isEmpty();
        }

        // consume data from serial and adds them into buffer
        private void consumeDataFromSerial() {
            while (!dataFromSerial.isEmpty()) {
                byte[] packetData = dataFromSerial.poll();
                consumedData.add(packetData);
            }
        }

        /**
         * Frees up used resources.
         */
        private void freeResources() {
            consumedData.clear();
        }

        /**
         * Converts specified byte array to its short representations. All
         * negative values in the byte array will be converted to its positive
         * counterparts.
         */
        private short[] toShortArr(byte[] byteArr) {
            short[] shortArr = new short[byteArr.length];
            for (int i = 0; i < byteArr.length; i++) {
                shortArr[i] = (short) (byteArr[i] & 0xff);
            }
            return shortArr;
        }

        @Override
        public void run() {
            while (true) {
                if (this.isInterrupted()) {
                    logger.info("Serial caller thread interrupted");
                    freeResources();
                    return;
                }

                // consuming new data from Serial
                synchronized (threadsSynchro) {
                    while (!areDataFromSerial()) {
                        try {
                            threadsSynchro.wait();
                        } catch (InterruptedException ex) {
                            logger.warn("Serial caller thread interrupted while "
                                    + "waiting on data from Serial.");
                            freeResources();
                            return;
                        }
                    }
                    consumeDataFromSerial();
                }

                // remove data from queue and put send it to listener
                while (!consumedData.isEmpty()) {
                    short[] packetData = toShortArr(consumedData.poll());
                    logger.info("Converted data from Serial: {}", packetData);

                    short[] userData = null;
                    try {
                        userData = HDLC_DataTransformer.getDataFromFrame(packetData);
                    } catch (HDLC_FormatException e) {
                        logger.error("Error while reading data from HDLC format: ", e);
                        continue;
                    }

                    if (iqrfListener != null) {
                        if (userData != null && userData.length != 0
                                && userData[0] == 126 && userData[userData.length - 1] == 126) {
                            // cutting for serial delimiter (126)
                            short[] cutted = new short[userData.length-2];
                            System.arraycopy(userData, 1, cutted, 0, cutted.length);
                            iqrfListener.onGetIQRFData(cutted);
                        }else{
                            iqrfListener.onGetIQRFData(userData);
                        }

                    }
                }
            }
        }
    }

    // creates and starts threads
    private void createAndStartThreads() {
        listenerCaller = new ListenerCaller();
        listenerCaller.start();
    }

    // terminates Serial reader and client caller threads
    private void terminateThreads() {
        logger.debug("terminateThreads - start:");

        // termination signal to listener caller thread
        listenerCaller.interrupt();

        // Waiting for threads to terminate. Cancelling worker threads has higher 
        // priority than main thread interruption. 
        while (listenerCaller.isAlive()) {
            try {
                if (listenerCaller.isAlive()) {
                    listenerCaller.join();
                }
            } catch (InterruptedException e) {
                // restoring interrupt status
                Thread.currentThread().interrupt();
                logger.warn("Termination - Serial Network Layer interrupted");
            }
        }

        logger.info("Serial Network Layer stopped.");
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

    private static int checkSerialBaudrate(int serialBaudrate) {
        if (serialBaudrate <= 0) {
            throw new IllegalArgumentException("Baudrate must be positive number");
        }
        return serialBaudrate;
    }

    /**
     * Creates new Serial network layer object.
     *
     * @param portName Serial-port name for communication
     * @param serialBaudrate baud rate
     */
    public SerialIQRFLayerJssc(String portName, int serialBaudrate) {
        this.portName = checkPortName(portName);
        this.serialBaudrate = checkSerialBaudrate(serialBaudrate);
    }

    @Override
    public void startIQRFLayer() throws IQRFLayerException {
        logger.debug("startIQRFLayer - start:");

        serialPort = new SerialPort(portName);

        try {
            serialPort.openPort();
            serialPort.setParams(
                    serialBaudrate,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE
            );
            int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;
            serialPort.setEventsMask(mask);
            serialPort.addEventListener(new SerialReader());
        } catch (SerialPortException ex) {
            throw new IQRFLayerException(ex, IQRFError.INIT_ERROR);
        }

        // init queue of data comming from Serial
        //dataFromSerial = new LinkedList<byte[]>();
        dataFromSerial = new ConcurrentLinkedDeque<>();

        // creating and starting threads
        createAndStartThreads();

        logger.info("Receiving data started");
        logger.debug("startIQRFLayer - end");
    }

    @Override
    public void sendData(IQRFData iqrfData) throws IQRFLayerException {
        logger.debug("sendData - start: iqrfData={}", iqrfData);

        // transforms request's data to Serial protocol format
        short[] dataForSerial = HDLC_DataTransformer.transformToHLDCFormat(iqrfData.getData());

        byte[] buffer = new byte[dataForSerial.length];
        for (int index = 0; index < dataForSerial.length; index++) {
            buffer[index] = (byte) dataForSerial[index];
        }

        try {
            logger.info("Data will be sent to Serial...");
            logger.debug("Data to send {}", Arrays.toString(buffer));
            // int[] uniData = new int[]{0x7E, 0x02, 0x00, 0x06, 0x01, 0xFF, 0xFF, 0x5A, 0x7E};
            boolean result = serialPort.writeBytes(buffer);
            //boolean result = serialPort.writeIntArray(uniData);
            logger.info("Data sending result is " + result);
        } catch (SerialPortException ex) {
            throw new IQRFLayerException(ex, IQRFError.SEND_ERROR);
        }
        logger.debug("sendData - end");
    }

    @Override
    public void destroy() {
        super.destroy();
        logger.debug("destroy - start: ");

        terminateThreads();
        dataFromSerial.clear();

        try {
            serialPort.closePort();
        } catch (SerialPortException ex) {
            logger.error("Error while closing SerialPort", ex);
        }
        serialPort = null;

        logger.info("Destroyed");
        logger.debug("destroy - end");
    }

   @Override
   public String toString() {
      return "SerialIQRFLayerJssc{" + "listenerCaller=" + listenerCaller + ", serialPort=" + serialPort + ", portName=" + portName + ", serialBaudrate=" + serialBaudrate + ", dataFromSerial=" + dataFromSerial + ", threadsSynchro=" + threadsSynchro + '}';
   }
}
