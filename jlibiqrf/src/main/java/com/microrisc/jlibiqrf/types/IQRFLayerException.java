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
package com.microrisc.jlibiqrf.types;

/**
 * Indicates errors of IQRF communication.
 *
 * @author Martin Strouhal
 */
public class IQRFLayerException extends Exception {

    private final IQRFError error;

    /**
     * Creates exception with specified error.
     *
     * @param message (as normal exception)
     * @param error of occurred event
     */
    public IQRFLayerException(String message, IQRFError error) {
        super(message);
        this.error = error;
    }

    /**
     * Creates exception with specified error.
     *
     * @param cause (as normal exception)
     * @param error of occurred event
     */
    public IQRFLayerException(Throwable cause, IQRFError error) {
        super(cause);
        this.error = error;
    }

    /**
     * Returns ID of error which has been occurred in library.
     *
     * @return id of error, for more info see {@link IQRFError}
     */
    public int getErrorId() {
        return error.getErrorId();
    }
}
