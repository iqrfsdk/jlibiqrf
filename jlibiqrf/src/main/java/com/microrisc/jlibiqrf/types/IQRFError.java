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

import java.util.HashMap;
import java.util.Map;

/**
 *  Error that can may occur in {@link IQRFLayerException}.
 * 
 * @author Martin Strouhal
 */
public enum IQRFError {
    /** Identify the general error occurred in JlibIQRF. */
    GENERAL_ERROR(1, "General error while JLibIQRF using."),
    /** Identify error occurred while sending data. */
    SEND_ERROR(2, "Error while data was sending."),
    /** Identify error occurred while initialization. */
    INIT_ERROR(3, "Error while initialization.");
    
    private static final Map<Integer, IQRFError> map = new HashMap<Integer,IQRFError>();
    
    static{
        for (IQRFError error : IQRFError.values()) {
            map.put(error.getErrorId(), error);
        }
    }
    
    private final int errorId;
    private final String description;
    
    private IQRFError(int errorId, String description){
        this.errorId = errorId;
        this.description = description;
    }

    /**
     * Returns error id.
     * @return id of error
     */
    public int getErrorId() {
        return errorId;
    }
    
    /**
     * Return description of specified error id.
     * @param errorId for which will be returned description of error
     * @return description of error as {@link String}
     */
    public static String getDescription(int errorId){
        return map.get(errorId).description;
    }
}
