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
package com.microrisc.jlibiqrf.iqrfLayer.cdc;

import com.microrisc.jlibiqrf.iqrfLayer.AbstractIQRFLayerFactory;
import com.microrisc.jlibiqrf.types.IQRFLayerException;
import com.microrisc.jlibiqrf.types.IQRFError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CDC factory for creation of IQRF layers, which are bound to USB CDC.
 *
 * @author Michal Konopa
 * @author Martin Strouhal
 */
// December 2015 - redesigned for jlibiqrf
public class CDCIQRFLayerFactory extends AbstractIQRFLayerFactory<CDCConfiguration, CDCIQRFLayer> {

    /** Logger. */
    private static final Logger logger = LoggerFactory.getLogger(CDCIQRFLayerFactory.class);

    /**
     * Creates CDC IQRF layer - according to specified configuration.
     *
     * @param iqrfConfig configuration of communication
     * @return CDC network layer
     */
    private CDCIQRFLayer createCDCNetworkLayer(CDCConfiguration cdcConfig)
            throws IQRFLayerException {
        logger.debug("createCDCNetworkLayer - start: cdcConfig=" + cdcConfig);
        String portName = cdcConfig.getPort();
        if (portName.equals(CDCConfiguration.PORT_AUTOCONF)) {
            //portName = getAutoconfiguredPortName( networkParams.connectionStorage );
            throw new UnsupportedOperationException("Autoconf isn't currently supported.");
        }

        try {
            CDCIQRFLayer layer = new CDCIQRFLayer(portName);
            logger.debug("createCDCNetworkLayer - end: " + layer);
            return layer;
        } catch (Exception ex) {
            throw new IQRFLayerException(ex, IQRFError.INIT_ERROR);
        }
    }

    @Override
    public CDCIQRFLayer getIQRFLayer(CDCConfiguration config) throws IQRFLayerException {
        return createCDCNetworkLayer(config);
    }
}
