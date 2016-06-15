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
package com.microrisc.jlibiqrf.iqrfLayer.serial;

import com.microrisc.jlibiqrf.iqrfLayer.AbstractIQRFLayerFactory;
import com.microrisc.jlibiqrf.types.IQRFLayerException;

/**
 * Serial factory for creation of iqrf layers, which are bound to serial
 * port.
 *
 * @author Rostislav Spinar
 * @author Martin Strouhal
 */
// December 2015 - redesigned for jlibiqrf
public final class SerialIQRFLayerFactory extends AbstractIQRFLayerFactory<SerialConfiguration, SerialIQRFLayerJssc> {

    /**
     * Creates serial iqrf layer - according to specified iqrf layer
     * parameters and version.
     *
     * @param config configuration for iqrf layer
     * @return serial iqrf layer
     */
    private SerialIQRFLayerJssc createSerialIQRFLayer(SerialConfiguration config)
            throws IQRFLayerException {
        String portName = config.getPort();
        if (portName.equals(SerialConfiguration.PORT_AUTOCONF)) {
            throw new UnsupportedOperationException("Autoconf isn't currently supported.");
        }

        return new SerialIQRFLayerJssc(config.getPort(), config.getBaudrate());
    }

    @Override
    public SerialIQRFLayerJssc getIQRFLayer(SerialConfiguration config) 
            throws IQRFLayerException {
        return createSerialIQRFLayer(config);
    }
}
