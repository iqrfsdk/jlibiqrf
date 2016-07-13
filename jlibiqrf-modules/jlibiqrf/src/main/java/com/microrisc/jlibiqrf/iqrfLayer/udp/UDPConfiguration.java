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
package com.microrisc.jlibiqrf.iqrfLayer.udp;

import com.microrisc.jlibiqrf.configuration.IQRFCommunicationType;
import com.microrisc.jlibiqrf.configuration.IQRFConfiguration;
import com.microrisc.jlibiqrf.iqrfLayer.cdc.CDCConfiguration;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Encapsulates configuration of {@link UDPIQRFLayer}.
 *
 * @author Martin Strouhal
 */
@XmlRootElement(name = "udpConfiguration")
public class UDPConfiguration extends IQRFConfiguration {

    /** Type specifying {@link CDCConfiguration}. */
    public static final IQRFCommunicationType type = IQRFCommunicationType.UDP;

    /** Address of local device. */
    @XmlElement
    private final String localAddress;
    /** Port of local device. */
    @XmlElement
    private final int localPort;
    /** Address of remote IQRF device. */
    @XmlElement
    private final String remoteAddress;
    /** Port of remote IQRF device. */
    @XmlElement
    private final int remotePort;
    /** Maximum size of received packet. */
    @XmlElement
    private final int maxRecvPacketSize;
    /** Reception timeout while receiving, 0 is infinity. */
    @XmlElement
    private final int receptionTimeout;

    /**
     * Creates {@link UDPConfiguration} with specified parameters.
     *
     * @param localAddress of device, {@link UDPConfiguration#localAddress}
     * @param localPort of device, {@link UDPConfiguration#localPort}
     * @param remoteAddress of IQRF GW, {@link UDPConfiguration#remoteAddress}
     * @param remotePort of IQRF GW, {@link UDPConfiguration#remotePort}
     * @param maxRecvPacketSize in one message,
     * {@link UDPConfiguration#maxRecvPacketSize}
     * @param receptionTimeout while waiting for receive of data,
     * {@link UDPConfiguration#receptionTimeout}
     */
    public UDPConfiguration(String localAddress, int localPort, String remoteAddress,
            int remotePort, int maxRecvPacketSize, int receptionTimeout) {
        super(type);
        this.localAddress = localAddress;
        this.localPort = localPort;
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
        this.maxRecvPacketSize = maxRecvPacketSize;
        this.receptionTimeout = receptionTimeout;
    }

    /**
     * Creates {@link UDPConfiguration} with specified parameters and default
     * {@link UDPConfiguration#maxRecvPacketSize} and too
     * {@link UDPConfiguration#receptionTimeout}
     *
     * @param localAddress of device, {@link UDPConfiguration#localAddress}
     * @param localPort of device, {@link UDPConfiguration#localPort}
     * @param remoteAddress of IQRF GW, {@link UDPConfiguration#remoteAddress}
     * @param remotePort of IQRF GW, {@link UDPConfiguration#remotePort}
     */
    public UDPConfiguration(String localAddress, int localPort, String remoteAddress, int remotePort) {
        super(type);
        this.localAddress = localAddress;
        this.localPort = localPort;
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
        this.maxRecvPacketSize = UDPIQRFLayer.MAX_RECEIVED_PACKET_SIZE;
        this.receptionTimeout = UDPIQRFLayer.RECEPTION_TIMEOUT_DEFAULT;
    }

    /** Only for purpose of JAXB. */
    private UDPConfiguration(){
        super(type);
        localAddress = remoteAddress = null;
        localPort = remotePort = maxRecvPacketSize = receptionTimeout = 0;
    }
    
    /**
     * Returns {@link UDPConfiguration#localAddress}. 
     * @return address of local device
     */
    public String getLocalAddress() {
        return localAddress;
    }

    /**
     * Returns {@link UDPConfiguration#localPort}
     * @return port of local device
     */
    public int getLocalPort() {
        return localPort;
    }

    /**
     * Returns {@link UDPConfiguration#remoteAddress}
     * @return address of remote IQRF device
     */
    public String getRemoteAddress() {
        return remoteAddress;
    }

    /** 
     * Returns {@link UDPConfiguration#remotePort}
     * @return port of remote IQRF device
     */
    public int getRemotePort() {
        return remotePort;
    }

    /**
     * Returns {@link UDPConfiguration#maxRecvPacketSize}
     * @return maximum allowed size of received packet
     */
    public int getMaxRecvPacketSize() {
        return maxRecvPacketSize;
    }

    /**
     * Returns {@link UDPConfiguration#receptionTimeout}
     * @return timeout
     */
    public int getReceptionTimeout() {
        return receptionTimeout;
    }

   @Override
   public String toString() {
      return "UDPConfiguration{" + "localAddress=" + localAddress + ", localPort=" + localPort + ", remoteAddress=" + remoteAddress + ", remotePort=" + remotePort + ", maxRecvPacketSize=" + maxRecvPacketSize + ", receptionTimeout=" + receptionTimeout + '}';
   }
}