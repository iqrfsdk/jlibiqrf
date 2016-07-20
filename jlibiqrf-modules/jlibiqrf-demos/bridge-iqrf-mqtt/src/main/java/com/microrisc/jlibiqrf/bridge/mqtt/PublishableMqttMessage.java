/*
 * Copyright 2016 user.
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
package com.microrisc.jlibiqrf.bridge.mqtt;

import com.microrisc.jlibiqrf.bridge.ArgumentChecker;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 *
 * @author user
 */
public class PublishableMqttMessage extends MqttMessage {
    
     private final DPAReplyType type;

    public PublishableMqttMessage(DPAReplyType type, byte[] payload) {
        super(payload);
        ArgumentChecker.checkNull(type);
        this.type = type;
    }

    public DPAReplyType getType() {
        return type;
    }
}