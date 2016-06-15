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

import com.microrisc.jlibiqrf.iqrfLayer.AbstractIQRFLayerFactory;
import com.microrisc.jlibiqrf.types.IQRFLayerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SPI factory for creation of iqrf layers, which are bound to SPI port.
 *
 * @author Rostislav Spinar
 * @author Martin Strouhal
 */
// December 2015 - redesigned for JLibIQRF
public class SPIIQRFLayerFactory
        extends AbstractIQRFLayerFactory<SPIConfiguration, SPIIQRFLayer> {

    /** Logger. */
    private static final Logger logger = LoggerFactory.getLogger(SPIIQRFLayerFactory.class);

    /**
     * Creates SPI iqrf layer - according to specified iqrf layer
     * parameters and version.
     *
     * @return SPI iqrf layer
     */
    private SPIIQRFLayer createSPIIQRFLayer(SPIConfiguration spiConfiguration)
            throws IQRFLayerException {
        logger.debug("createSPIIQRFLayer - start: spiConfiguration={}", spiConfiguration);
        String portName = spiConfiguration.getPort();
        if (portName.equals(SPIConfiguration.PORT_AUTOCONF)) {
            throw new UnsupportedOperationException("Autoconf isn't currently supported.");
        }

        SPIIQRFLayer layer = new SPIIQRFLayer(portName);
        logger.debug("createSPIIQRFLayer - end: " + layer);
        return layer;
    }

    @Override
    public SPIIQRFLayer getIQRFLayer(SPIConfiguration config) throws IQRFLayerException {
        return createSPIIQRFLayer(config);
    }
}
