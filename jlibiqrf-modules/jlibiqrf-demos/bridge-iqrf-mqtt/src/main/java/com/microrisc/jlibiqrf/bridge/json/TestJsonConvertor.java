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
package com.microrisc.jlibiqrf.bridge.json;

import java.util.Arrays;

/**
 * For testing purpose only!
 * 
 * @author Martin Strouhal
 */
public class TestJsonConvertor implements JsonConvertor {
    
    @Override
    public short[] toIQRF(Object json) {
        return new short[]{0x00, 0x00, 0x06, 0x01, 0xFF, 0xFF};
    }

    @Override
    public Object toJson(short[] iqrf) {
        return Arrays.toString(iqrf);
    }
    
    private static TestJsonConvertor instance = new TestJsonConvertor();
    
    private TestJsonConvertor(){}
    
    /** Returns instance of {@link TestJsonConvertor}.
     * 
     * @return instance of {@link TestJsonConvertor}
     */
    public static TestJsonConvertor getInstance(){
        return instance;
    }
}
