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
package com.microrisc.jlibiqrf.demos.config;

/**
 * Provides loading and saving of {@link BridgeConfiguration}.
 *
 * @author Martin Strouhal
 */
public interface BridgeConfigurationLoader {

    /**
     * Loads configuration from file with specified path.
     *
     * @param path of file form which will be configuration loaded
     * @return loaded config as {@link BridgeConfiguration}
     */
    public BridgeConfiguration load(String path);

    /**
     * Saves specified configuration to file with specified path.
     *
     * @param config to save
     * @param path of file to which will be config saved
     */
    public void saveBridgeConfiguration(BridgeConfiguration config, String path);

}
