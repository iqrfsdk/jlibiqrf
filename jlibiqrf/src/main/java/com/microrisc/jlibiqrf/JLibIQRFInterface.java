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
package com.microrisc.jlibiqrf;

/**
 * Provides interface of JLibIQRF.
 *
 * @author Martin Strouhal
 */
public interface JLibIQRFInterface {

    /** Result value of success operation in {@link JLibIQRF}. */
    public final int SUCCESS_OPERATION = 0;

    /**
     * Sends data to IQRF network.
     *
     * @param data which will be send
     * @return {@link JLibIQRFInterface#SUCCESS_OPERATION} if was operation
     * successful
     */
    public int sendData(short[] data);

    /**
     * Register {@link IQRFListener} which callback function will be called
     * when data from IQRF network will be received.
     *
     * @param listener to register
     */
    public void addIQRFListener(IQRFListener listener);

    /**
     * Destroy all thing on the end of work and terminate all threads.
     */
    public void destroy();
}
