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
package com.microrisc.jlibiqrf.bridge.json.simple;

/**
 * Helps identify data from IQRF network and holds information about it,
 * especially about DPA.
 *
 * @author Martin Strouhal
 */
public class DPAAddon {
    
    private final boolean dpa;
    private final int number;

    /** 
     * Creates instance of {@link DPAAddon} with specified number and dpa 
     * identification.
     * @param dpa is true if it's used DPA protocol
     * @param number of packet (each next packet should have increased this 
     * number)
     */
    public DPAAddon(boolean dpa, int number) {
        this.dpa = dpa;
        this.number = number;
    }

    /**
     * Returns true if it's used dpa protocol
     * @return if it's used dpa protocol
     */
    public boolean isDPA() {
        return dpa;
    }

    /**
     * Returns number of packet, see details in constructor.
     * @return number of packet
     */
    public int getNumber() {
        return number;
    }    
}
