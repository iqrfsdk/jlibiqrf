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

import com.microrisc.jlibiqrf.configuration.IQRFConfiguration;

/**
 * Base class for IQRF layer factories.
 *
 * @author Martin Strouhal
 * @param <T> configuration of {@link AbstractIQRFLayer} which specify parameters of communication
 * @param <U> {@link AbstractIQRFLayer} for which is factory defined
 */
public abstract class AbstractIQRFLayerFactory<T extends IQRFConfiguration, U extends AbstractIQRFLayer> {

    /**
     * Returns network layer implementation.
     *
     * @param configuration configuration to use
     * @return network layer implementation
     * @throws Exception if an error has occurred
     */
    public abstract U getIQRFLayer(T configuration) throws Exception;
}
