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
package com.microrisc.jlibiqrf.iqrfLayer.serial;

import com.microrisc.jlibiqrf.configuration.IQRFCommunicationType;
import com.microrisc.jlibiqrf.configuration.IQRFConfiguration;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Encapsulates configuration of {@link SerialIQRFLayerJssc}.
 *
 * @author Martin Strouhal
 */
@XmlRootElement(name = "serialConfiguration")
public class SerialConfiguration extends IQRFConfiguration {

    /** Type specifying {@link SerialConfiguration}. */
    public static final IQRFCommunicationType type = IQRFCommunicationType.SERIAL;

    public static final String PORT_AUTOCONF = "auto";
    /** Port on which communicating device via UART */
    @XmlElement
    private final String port;
    /** Baudrate of communication */
    @XmlElement
    private final int baudrate;

    /**
     * Creates {@link SerialConfiguration} with specified parameters
     *
     * @param port on which is communication processing
     * @param baudrate of communication
     */
    public SerialConfiguration(String port, int baudrate) {
        super(type);
        this.port = port;
        this.baudrate = baudrate;
    }

    /** Only for purpose of JAXB. */
    private SerialConfiguration() {
        super(type);
        port = null;
        baudrate = 0;
    }

    /**
     * Returns {@link SerialConfiguration#port}.
     *
     * @return port used for communication
     */
    public String getPort() {
        return port;
    }

    /**
     * Returns {@link SerialConfiguration#baudrate}.
     *
     * @return baudrate of communication
     */
    public int getBaudrate() {
        return baudrate;
    }

    @Override
    public String toString() {
        return "SerialConfiguration{" + "port=" + port + ", baudrate=" + baudrate + '}';
    }
}
