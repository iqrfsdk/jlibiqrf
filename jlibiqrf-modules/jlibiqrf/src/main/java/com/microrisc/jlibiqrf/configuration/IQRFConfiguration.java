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
package com.microrisc.jlibiqrf.configuration;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Encapsulating configuration of communication for this library. Subclasses can
 * encapsulate next settings of communication depending on the used technology
 * (CDC, SPI, and next).
 *
 * @author Martin Strouhal
 */
@XmlRootElement
public abstract class IQRFConfiguration {

    @XmlElement(name = "communication_type")
    private IQRFCommunicationType type;
    protected String savingLocation;

    /**
     * Creates abstract of IQRF_Configuration for specified
     * {@link IQRFCommunicationType}.
     *
     * @param type of used configuration
     */
    public IQRFConfiguration(IQRFCommunicationType type) {
        this.type = type;
    }

    /** Only for purpose of JAXB. */
    private IQRFConfiguration() {
    }

    /**
     * Returns type of used configuration.
     *
     * @return {@link IQRFCommunicationType}
     */

    public IQRFCommunicationType getIQRFCommunicationType() {
        return type;
    }

    public String getSavingLocation() {
        return savingLocation;
    }

    public void setSavingLocation(String savingLocation) {
        this.savingLocation = savingLocation;
    }

    @Override
    public String toString() {
        return "IQRFConfiguration{" + "type=" + type + ", savingLocation=" + savingLocation + '}';
    }

}
