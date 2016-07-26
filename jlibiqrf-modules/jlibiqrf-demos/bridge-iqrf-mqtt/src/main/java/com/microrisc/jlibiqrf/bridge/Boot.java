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
package com.microrisc.jlibiqrf.bridge;

import com.microrisc.jlibiqrf.bridge.config.BridgeConfiguration;
import com.microrisc.jlibiqrf.bridge.config.SimpleBridgeConfigurationLoader;

public class Boot {
    
    public static void main(String[] args) {
        BridgeConfiguration config = SimpleBridgeConfigurationLoader.getInstance().load("src/config/config.xml");
        //BridgeConfiguration config = SimpleBridgeConfigurationLoader.getInstance().load(args[0]);
        System.out.println("Loaded config: " + config.toString());
        
        Bridge bridge = new Bridge(config);
        System.out.println("Bridge started");
        
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                System.out.println("Bridge will be canceled.");
                bridge.destroy();
            }
        });
    }
    
}
