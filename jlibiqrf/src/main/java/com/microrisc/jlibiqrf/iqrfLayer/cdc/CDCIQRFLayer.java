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
package com.microrisc.jlibiqrf.iqrfLayer.cdc;

import com.microrisc.cdc.J_AsyncMsgListener;
import com.microrisc.cdc.J_CDCImpl;
import com.microrisc.cdc.J_CDCImplException;
import com.microrisc.jlibiqrf.iqrfLayer.AbstractIQRFLayer;
import com.microrisc.jlibiqrf.types.HexConvertor;
import com.microrisc.jlibiqrf.types.IQRFData;
import com.microrisc.jlibiqrf.types.IQRFError;
import com.microrisc.jlibiqrf.types.IQRFLayerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements network layer using {@code J_CDCImpl} object.
 * <p>
 * This registers itself like an asynchronous listener of {@code J_CDCImpl}
 * object. <br>
 * All data coming from CDC interface is forwarder to user's registered iqrf
 * listener. All data designated to underlying iqrf network are forwarded to
 * J_CDCImpl's {@code J_CDCImpl} method.
 *
 * @author Michal Konopa
 * @author Martin Strouhal
 */
// December 2015 - redesigned for jlibiqrf
public final class CDCIQRFLayer extends AbstractIQRFLayer implements J_AsyncMsgListener {

    /** Logger. */
    private static final Logger logger = LoggerFactory.getLogger(CDCIQRFLayer.class);

    private static String checkPortName(String portName) {
        if (portName == null) {
            throw new IllegalArgumentException("Port name cannot be null");
        }

        if (portName.equals("")) {
            throw new IllegalArgumentException("Port name cannot be empty string");
        }
        return portName;
    }


    /** Reference to CDC-object for communication. */
    private J_CDCImpl cdcImpl = null;

    /**
     * Creates CDC network layer object.
     *
     * @param portName COM-port name for communication
     * @throws com.microrisc.cdc.J_CDCImplException if some exception has
     * occurred during creating of CDC network layer
     */
    public CDCIQRFLayer(String portName) throws J_CDCImplException, Exception {
        checkPortName(portName);
        this.cdcImpl = new J_CDCImpl(portName);
        logger.debug("CDCIQRFLayer created: portName=" + portName);
    }

    /**
     * Starts receiving data from CDC interface. This methods last at least for
     * 5 seconds due to reseting connected USB device.
     *
     * @throws com.microrisc.jlibiqrf.types.IQRFLayerException if some error has
     * been occurred
     */
    @Override
    public void startIQRFLayer() throws IQRFLayerException {
        logger.debug("startIQRFLayer - start");

        cdcImpl.registerAsyncListener(this);

        /*
        final long RESET_DELAY = 5000;
        final long MARGIN_DELAY = 5000;
        
        try {
            // reseting GW 
            // 5 s after receiving of this command USB device is reset - program must deal with this !!!
            cdcImpl.resetUSBDevice();
            Thread.sleep(RESET_DELAY + MARGIN_DELAY);
        } catch (Exception ex) {
            logger.error("Cannot reset USB device: " + ex.getMessage());
            throw new NetworkLayerException(ex);
        }
         */
        logger.debug("startIQRFLayer - end");
    }

    @Override
    public void sendData(IQRFData iqrfData) throws IQRFLayerException {
        logger.debug("sendData - start: iqrfData={}", iqrfData);

        try {
            cdcImpl.sendData(iqrfData.getData());
        } catch (Exception ex) {
            throw new IQRFLayerException(ex, IQRFError.SEND_ERROR);
        }

        logger.debug("sendData - end");
    }

    @Override
    public void destroy() {
        super.destroy();
        logger.debug("destroy - start: ");

        cdcImpl.unregisterAsyncListener();
        logger.info("CDC Listener unregistered");

        cdcImpl.destroy();
        cdcImpl = null;

        logger.info("Destroyed CDCIQRFLayer");
        logger.debug("destroy - end");
    }

    @Override
    public void onGetMessage(short[] data) {
        logger.debug("onGetMessage - start: data={}", HexConvertor.getShortArrayAsStringWithHex(data));

        if (iqrfListener != null) {
            iqrfListener.onGetIQRFData(data);
        }

        logger.debug("onGetMessage - end");
    }

   @Override
   public String toString() {
      return "CDCIQRFLayer{" + "cdcImpl=" + cdcImpl + '}';
   }
}
