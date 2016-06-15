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
package com.microrisc.jlibiqrf.iqrfLayer.udp;

import com.microrisc.jlibiqrf.iqrfLayer.AbstractIQRFLayerFactory;
import com.microrisc.jlibiqrf.types.IQRFLayerException;

/**
 * Factory for creation of network layers, which are bound to UDP GW-ETH.
 * <p>
 * Two types of UDP network layer is supported: <br>
 * 1. bound to single network: supports only one specified network <br>
 * 2. multinetwork: is able to work with arbitrary UDP network <br>
 *
 * If user want to create type 1 of network, it must correctly specify <b>remote
 * address</b>
 * and <b>remote port</b> through configuration settings: <br>
 * <b>networkLayer.type.udp.remoteaddress</b> and
 * <b>networkLayer.type.udp.remoteport</b>. If <b>neither</b> of the two
 * settings are specified, type 2 of network layer is created.
 *
 * <p>
 * Particular network layer version to create is specified by configuration key:
 * <b>networkLayer.type.udp.version</b>. If no such key is present in
 * configuration properties, version of "01" is assumed.
 *
 * @author Michal Konopa
 * @author Martin Strouhal
 */
// December 2015 - redesigned for JLibIQRF
public class UDPIQRFLayerFactory extends AbstractIQRFLayerFactory<UDPConfiguration, UDPIQRFLayer> {

    private UDPIQRFLayer createClientSingleNetworkLayer(UDPConfiguration config) {
        return new UDPIQRFLayer(config.getLocalAddress(), config.getLocalPort(),
                config.getRemoteAddress(), config.getRemotePort(),
                config.getMaxRecvPacketSize(), config.getReceptionTimeout()
        );
    }

    @Override
    public UDPIQRFLayer getIQRFLayer(UDPConfiguration config) throws IQRFLayerException {
        return createClientSingleNetworkLayer(config);
    }
}
