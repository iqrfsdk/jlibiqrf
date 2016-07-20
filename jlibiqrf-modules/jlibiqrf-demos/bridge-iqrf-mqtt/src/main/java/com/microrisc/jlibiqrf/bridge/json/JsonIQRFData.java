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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microrisc.jlibiqrf.bridge.ArgumentChecker;

/**
 *
 * @author Martin Strouhal
 */
final class JsonIQRFData implements IQRFData {

    @JsonProperty("nadr")
    private int address;
    @JsonProperty("per")
    private short peripheral;
    @JsonProperty("cmd")
    private short command;
    @JsonProperty("hwpid")
    private int hardwareProfiles;
    @JsonProperty("data")
    private short[] data;

    @Override
    public short[] getIQRFData() {
        short[] result = new short[6 + data.length];
        result = saveIntToShort(result, 0, address);
        result[2] = peripheral;
        result[3] = command;
        result = saveIntToShort(result, 4, hardwareProfiles);
        System.arraycopy(data, 0, result, 6, data.length);
        return result;
    }

    private short[] saveIntToShort(short[] array, int index, int value) {
        ArgumentChecker.checkNull(array);
        ArgumentChecker.checkNegative(index);
        
        array[index] = (short) (value & 0xFF);
        value >>= 8;
        array[index + 1] = (short) (value & 0xFF);
        return array;
    }
}
