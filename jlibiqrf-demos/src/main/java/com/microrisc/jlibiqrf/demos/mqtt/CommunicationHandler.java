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
 *  Interface for handling messages from MQTT remote server.
 * 
 * @author Rostislav Spinar
 * @author Martin Strouhal
 * @param <T> is type of messages
 */
public interface CommunicationHandler<T> {

    /** Connects to the server. */
    void connect();

    /** Returns true, if it's connected still.
     *
     * @return true, if it's connected
     */
    boolean isConnected();

    /** Prepare specified message to publish and publish it.
     * 
     * @param topic in which will be message published
     * @param message to publish
     */
    void processIncomingMessage(String topic, T message);

    /** Run process of regular checking and publishing messages.
     * 
     * @param checkInterval in which will be regularly checked, in [ms]
     */
    void checkAndPublishDeviceData(int checkInterval);

    /** Disconnects from the server. */
    void disconnect();
}
