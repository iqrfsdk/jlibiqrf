/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.microrisc.jlibiqrf.demos.iqrf;

import com.microrisc.jlibiqrf.IQRFListener;
import com.microrisc.jlibiqrf.JLibIQRF;
import com.microrisc.jlibiqrf.demos.AppLogic;
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
    
    private AppLogic appLogic;
    private JLibIQRF iqrfLib;
    private CommunicatingThread comThread;
    
    /** Creates instance of {@link IQRFCommunicator}.
     * 
     * @param logic which will be used for communication
     */
    public IQRFCommunicator(AppLogic logic) {
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
