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
package com.microrisc.jlibiqrf.demos;

import com.microrisc.jlibiqrf.demos.config.BridgeConfiguration;
import com.microrisc.jlibiqrf.demos.config.SimpleBridgeConfigurationLoader;

/**
 *
 * @author Martin Strouhal
 */
public class Main {
    
    public static void main(String[] args) {
        BridgeConfiguration config = SimpleBridgeConfigurationLoader.getInstance().load("src/config/config.xml");
        System.out.println("Loaded config: " + config.toString());
        
        Bridge bridge = new Bridge(config);
    }
    
}
