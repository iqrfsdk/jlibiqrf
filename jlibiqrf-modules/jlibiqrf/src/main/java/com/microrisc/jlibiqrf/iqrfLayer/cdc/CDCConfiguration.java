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

import com.microrisc.jlibiqrf.configuration.IQRFCommunicationType;
import com.microrisc.jlibiqrf.configuration.IQRFConfiguration;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Encapsulates configuration of {@link CDCIQRFLayer}.
 *
 * @author Martin Strouhal
 */
@XmlRootElement(name = "cdcConfiguration")
public class CDCConfiguration extends IQRFConfiguration {

    /** Type specifying {@link CDCConfiguration}. */
    public static final IQRFCommunicationType type = IQRFCommunicationType.CDC;

    public static final String PORT_AUTOCONF = "auto";
    
    /** Port on which communicating device via CDC */
    @XmlElement
    private final String port;

    /**
     * Creates {@link CDCConfiguration} with specified parameters.
     *
     * @param port on which is communication processing
     */
    public CDCConfiguration(String port) {
        super(type);
        this.port = port;
    }

    /** Only for purpose of JAXB. */
    private CDCConfiguration() {
        super(type);
        port = null;
    }

    /**
     * Returns {@link CDCConfiguration#port}
     *
     * @return port used for communication
     */
    public String getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "CDCConfiguration{" + "port=" + port + '}';
    }
}
