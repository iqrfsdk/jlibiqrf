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

/**
 * Provides interface for loading of {@link IQRFConfiguration}
 *
 * @param <LoadingObject> from which will be config loaded
 * @param <SavingObject> into which will be config saved
 *
 * @author Martin Strouhal
 */
public interface XMLConfigurationLoader<LoadingObject, SavingObject> {

    /** Loads configuration.
     *
     * @param source from which is configuration loading
     * @return loaded configuration
     * @throws Exception if some error while loading has been occurred
     */
    public IQRFConfiguration load(LoadingObject source);

    /** Saves configuration.
     *
     * @param config to save
     * @param saveLocation location to which will be configuration saved
     * @throws Exception if some error while saving has been occurred
     */
    public void save(IQRFConfiguration config, SavingObject saveLocation);

}
