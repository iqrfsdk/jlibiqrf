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
package com.microrisc.jlibiqrf.demos.iqrf;

import com.microrisc.jlibiqrf.IQRFListener;
import com.microrisc.jlibiqrf.JLibIQRF;
import com.microrisc.jlibiqrf.demos.Bridge;
import com.microrisc.jlibiqrf.iqrfLayer.cdc.CDCConfiguration;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martin Strouhal
 */
public class IQRFCommunicator implements IQRFListener {

    private static final Logger log = LoggerFactory.getLogger(IQRFCommunicator.class);
    
    private Bridge appLogic;
    private JLibIQRF iqrfLib;
    private CommunicatingThread comThread;
    
    /** Creates instance of {@link IQRFCommunicator}.
     * 
     * @param logic which will be used for communication
     */
    public IQRFCommunicator(Bridge logic) {
        appLogic = logic;
    }

    /**
     * Init IQRF communicator.
     */
    public void init() {
        JLibIQRF iqrf = JLibIQRF.init(new CDCConfiguration("COM5"));
        iqrf.addIQRFListener(this);
        iqrfLib = iqrf;
        comThread = new CommunicatingThread();
        comThread.start();
        log.info("IQRFCommunicator init completed and IQRF communication thread started.");
    }

    @Override
    public void onGetIQRFData(short[] data) {
        log.debug("onGetIQRFData - start: data={}", Arrays.toString(data));
        // send to mqtt
        appLogic.addIQRFData(data);
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
                
                if (appLogic.isAvailableMqttMessage()) {
                    log.debug("IQRF com thread found available mqtt message. It will be send into IQRF.");
                    short[] iqrfData = appLogic.getAndRemoveMqttMessage();
                    iqrfLib.sendData(iqrfData);
                } else {
                    try {
                        this.sleep(500);
                    } catch (InterruptedException ex) {
                        System.out.println(ex);
                        return;
                    }
                }
            }
        }
    }
}
