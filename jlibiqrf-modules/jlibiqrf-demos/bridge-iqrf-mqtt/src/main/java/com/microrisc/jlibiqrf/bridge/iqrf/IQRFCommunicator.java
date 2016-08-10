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
package com.microrisc.jlibiqrf.bridge.iqrf;

import com.microrisc.jlibiqrf.IQRFListener;
import com.microrisc.jlibiqrf.JLibIQRF;
import com.microrisc.jlibiqrf.bridge.ArgumentChecker;
import com.microrisc.jlibiqrf.bridge.Bridge;
import com.microrisc.jlibiqrf.bridge.config.BridgeConfiguration;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martin Strouhal
 */
public class IQRFCommunicator implements IQRFListener {

    private static final Logger log = LoggerFactory.getLogger(IQRFCommunicator.class);
    
    private Bridge bridge;
    private JLibIQRF iqrfLib;
    private CommunicatingThread comThread;
    private int iqrfTimeout;
    
    /** Creates instance of {@link IQRFCommunicator}.
     * 
     * @param bridge which will be used for controlling of communication
     */
    public IQRFCommunicator(Bridge bridge) {
        ArgumentChecker.checkNull(bridge);
        this.bridge = bridge;
    }

    /**
     * Init IQRF communicator.
     * 
     * @param config containing details about network to connect
     */
    public void init(BridgeConfiguration config) {
        ArgumentChecker.checkNull(config);
        
        iqrfTimeout = config.getIQRFCheckingInterval();
        JLibIQRF iqrf = JLibIQRF.init(config.getIqrfConfig());
        iqrf.addIQRFListener(this);
        iqrfLib = iqrf;
        
        comThread = new CommunicatingThread();
        comThread.start();
        
        log.info("IQRFCommunicator init completed and IQRF communication thread started.");
    }
    
    @Deprecated
    public String readCoordinatorMID(){
        final short[] readOSInfoCmd = new short[]{0x00, 0x00, 0x02, 0x00, 0xFF, 0xFF};        
        BlockingQueue<String> queue = new LinkedBlockingQueue();
        MIDRecognizer recognizer = new MIDRecognizer(queue);
        iqrfLib.addIQRFListener(recognizer);
        iqrfLib.sendData(readOSInfoCmd);
        
        String mid = null;
        try {
            mid = queue.poll(10, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            log.warn(ex.getMessage());
        }
        if(mid == null){
            log.warn("Response from coordinator cannot be processed!");
            mid = "unknown";
        }
        
        iqrfLib.addIQRFListener(this);
        
        return mid;
    }

    @Override
    public void onGetIQRFData(short[] data) {
        log.debug("onGetIQRFData - start: data={}", Arrays.toString(data));
        // send to mqtt
        bridge.addIQRFData(data);
        log.debug("onGetIQRFData - end");
    }

    private class CommunicatingThread extends Thread {

        @Override
        public void run() {
            // getting iqrf data from applogic and their sending
            while (true) {
                if(this.isInterrupted()){
                    log.warn("IQRF communicating thread was interrupted!");
                    return;
                }
                
                if (bridge.isAvailableMqttMessage()) {
                    log.debug("IQRF com thread found available mqtt message. It will be send into IQRF.");
                    short[] iqrfData = bridge.getAndRemoveMqttMessage();
                    if (iqrfData == null) {
                        log.warn("Data are null. Data won't be send into IQRF network");
                    } else {
                        iqrfLib.sendData(iqrfData);
                    }
                } else {
                    try {
                        this.sleep(iqrfTimeout);
                    } catch (InterruptedException ex) {
                        System.out.println(ex);
                        return;
                    }
                }
            }
        }
    }
    
    /** Free-up resources. */
    public void destroy(){
        comThread.interrupt();
        iqrfLib.destroy();
    }
}
