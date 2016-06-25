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

package com.microrisc.jlibiqrf.demos.mqtt;

/**
 *
 * @author Rostislav Spinar
 */
public class CommunicationHandlerException extends Exception {

    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public final void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public CommunicationHandlerException(String msg, Exception nestedEx) {
        super(msg, nestedEx);
        setErrorMessage(msg);
    }

    public CommunicationHandlerException(String message, Throwable cause) {
        super(message, cause);
        setErrorMessage(message);
    }

    public CommunicationHandlerException(String msg) {
        super(msg);
        setErrorMessage(msg);
    }

    public CommunicationHandlerException() {
        super();
    }

    public CommunicationHandlerException(Throwable cause) {
        super(cause);
    }
}
