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
package com.microrisc.jlibiqrf;

import com.microrisc.jlibiqrf.configuration.IQRFCommunicationType;
import com.microrisc.jlibiqrf.configuration.IQRFConfiguration;
import com.microrisc.jlibiqrf.iqrfLayer.AbstractIQRFLayer;
import com.microrisc.jlibiqrf.iqrfLayer.cdc.CDCConfiguration;
import com.microrisc.jlibiqrf.iqrfLayer.cdc.CDCIQRFLayerFactory;
import com.microrisc.jlibiqrf.iqrfLayer.serial.SerialConfiguration;
import com.microrisc.jlibiqrf.iqrfLayer.serial.SerialIQRFLayerFactory;
import com.microrisc.jlibiqrf.iqrfLayer.spi.SPIConfiguration;
import com.microrisc.jlibiqrf.iqrfLayer.spi.SPIIQRFLayerFactory;
import com.microrisc.jlibiqrf.iqrfLayer.udp.UDPConfiguration;
import com.microrisc.jlibiqrf.iqrfLayer.udp.UDPIQRFLayerFactory;
import com.microrisc.jlibiqrf.types.BaseIQRFData;
import com.microrisc.jlibiqrf.types.IQRFData;
import com.microrisc.jlibiqrf.types.IQRFLayerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides united public interface for communication via libraries like
 * JLibCDC, JLibRPi-SPI and etc.
 *
 * @author Martin Strouhal
 */
public class JLibIQRF implements JLibIQRFInterface {

    /** Logger. */
    private static final Logger logger = LoggerFactory.getLogger(JLibIQRF.class);

    /**
     * Returns a new {@link JLibIQRF} with specified config.
     * @param config for creating {@link JLibIQRF}
     * @return {@code null} if some error has been occurred while creating
     */
    public static JLibIQRF init(IQRFConfiguration config) {
        logger.debug("init - start: config=" + config);
        AbstractIQRFLayer layer = null;
        try {
            layer = identifyAndCreateIQRFLayer(config);
        } catch (IQRFLayerException ex) {
            logger.error(ex.getMessage());
            return null;
        }
        try {
            layer.startIQRFLayer();
        } catch (IQRFLayerException ex) {
            logger.error(ex.getMessage());
            return null;
        }
        logger.debug("init - end: " + layer);
        return new JLibIQRF(layer);
    }

    private static AbstractIQRFLayer identifyAndCreateIQRFLayer(IQRFConfiguration config) throws IQRFLayerException {
        logger.debug("identifyAndCreateIQRFLayer - start: config={}", config);
        AbstractIQRFLayer layer;
        if (config.getIQRFCommunicationType() == CDCConfiguration.type) {
            CDCConfiguration cdcConfig = (CDCConfiguration) config;
            CDCIQRFLayerFactory cdcFactory = new CDCIQRFLayerFactory();
            layer = cdcFactory.getIQRFLayer(cdcConfig);
        } else if(config.getIQRFCommunicationType() == SPIConfiguration.type){
            SPIConfiguration spiConfig = (SPIConfiguration)config;
            SPIIQRFLayerFactory spiFactory = new SPIIQRFLayerFactory();
            layer = spiFactory.getIQRFLayer(spiConfig);
        }else if(config.getIQRFCommunicationType() == SerialConfiguration.type){
            SerialConfiguration serialConfig = (SerialConfiguration)config;
            SerialIQRFLayerFactory serialFactory = new SerialIQRFLayerFactory();
            layer = serialFactory.getIQRFLayer(serialConfig);
        }else if(config.getIQRFCommunicationType() == IQRFCommunicationType.UDP){
            UDPConfiguration udpConfig = (UDPConfiguration)config;
            UDPIQRFLayerFactory udpFactory = new UDPIQRFLayerFactory();
            layer = udpFactory.getIQRFLayer(udpConfig);
        }else{
            throw new UnsupportedOperationException("Currently isn't supported "
                    + config.getIQRFCommunicationType());
        }
        logger.debug("identifyAndCreateIQRFLayer - end: " + layer);
        return layer;
    }

    
    private final AbstractIQRFLayer iqrfLayer;

    /** *  Creates instance of {@link JLibIQRF} with specified communication
     * {@link AbstractIQRFLayer layer}
     *
     * @param layer used in {@link JLibIQRF}
     */
    private JLibIQRF(AbstractIQRFLayer layer) {
        iqrfLayer = layer;
    }

    @Override
    public int sendData(short[] data) {
        IQRFData iqrfData = new BaseIQRFData(data);
        try {
            iqrfLayer.sendData(iqrfData);
        } catch (IQRFLayerException ex) {
            return ex.getErrorId();
        }
        return SUCCESS_OPERATION;
    }

    @Override
    public void addIQRFListener(IQRFListener listener) {
        iqrfLayer.registerListener(listener);
    }

    @Override
    public void destroy() {
        iqrfLayer.destroy();
        logger.info("IQRF layer destroyed.");
    }

   @Override
   public String toString() {
      return "JLibIQRF{" + "iqrfLayer=" + iqrfLayer + '}';
   }
}