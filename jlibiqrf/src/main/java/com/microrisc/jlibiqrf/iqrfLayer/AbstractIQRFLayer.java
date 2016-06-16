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
package com.microrisc.jlibiqrf.iqrfLayer;

import com.microrisc.jlibiqrf.IQRFListener;
import com.microrisc.jlibiqrf.types.IQRFData;
import com.microrisc.jlibiqrf.types.IQRFLayerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for implemented IQRF layer mediating communication with IQRF
 * devices.
 *
 * @author Martin Strouhal
 */
public abstract class AbstractIQRFLayer {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(AbstractIQRFLayer.class);

    /** Registered iqrf listener. */
    protected IQRFListener iqrfListener = null;

    /**
     * Initialize IQRF layer.
     *
     * @throws IQRFLayerException if some error has been occurred
     */
    public abstract void startIQRFLayer() throws IQRFLayerException;

    /**
     * Register {@link IQRFListener} for IQRF layer.
     *
     * @param listener to register
     */
    public void registerListener(IQRFListener listener) {
        this.iqrfListener = listener;
        logger.info("Listener registred. Actual listener is {}", iqrfListener);
    }

    /**
     * Unregister {@link IQRFListener} from IQRF layer.
     */
    public void unregisterListener() {
        this.iqrfListener = null;
        logger.info("Listener unregistred.");
    }

    /**
     * Destroy system resource of {@link AbstractIQRFLayer}.
     */
    public void destroy() {
        unregisterListener();
    }

    /**
     * Send data into specified IQRF layer.
     *
     * @param data to send
     * @throws IQRFLayerException if some error has been occurred
     */
    public abstract void sendData(IQRFData data) throws IQRFLayerException;

}
