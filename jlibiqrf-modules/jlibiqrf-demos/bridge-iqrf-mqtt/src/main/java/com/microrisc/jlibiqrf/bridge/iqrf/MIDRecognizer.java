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

package com.microrisc.jlibiqrf.bridge.iqrf;

import com.microrisc.jlibiqrf.IQRFListener;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implementation of {@link IQRFListener} implements service which is 
 * listening incoming data and from it identify MID of coordinator. If it's MID
 * recognized, it pulled to queue specified in constructor.
 * 
 * @author Martin Strouhal
 */
class MIDRecognizer implements IQRFListener {

    private final static Logger log = LoggerFactory.getLogger(MIDRecognizer.class);
    private final BlockingQueue queue;
    
    /**
     * Creates instance of {@link MIDRecognizer}. Recognized MID is pulled into
     * queue.
     * @param queue into which will be pulled recognized MID
     */
    public MIDRecognizer(BlockingQueue queue) {
        this.queue = queue;
    }
    
    // listen and recognize if data contains MID
    @Override
    public void onGetIQRFData(short[] shorts) {
        log.debug("onGetIQRFData - start: " + Arrays.toString(shorts));
        final short[] startSequence = new short[]{0x00, 0x00, 0x2, 0x80};
        final int moduleIdStartPos = 8;
        final int moduleIdLength = 4;
        if(shorts.length >= startSequence.length + moduleIdLength &&
                shorts.length > moduleIdStartPos -1 + moduleIdLength){
            String mid = "";
            for (int i = moduleIdLength; i > 0; i--) {
                mid = mid + getDoublePlaceHex(shorts[moduleIdStartPos + i - 1]);
            }
            queue.add(mid);
        }else{
            log.warn("Unrecognized MID: " + Arrays.toString(shorts));
        }
        log.debug("onGetIQRFData - end");
    }
    
    /**
     * Converts number to hex string formated to two char places, so each string 
     * contains 2 hex symbols.
     * @param number to convert
     * @return double place hex string
     */
    private String getDoublePlaceHex(short number){
        String finalVal = Integer.toHexString(number);
        if(finalVal.length() == 1){
            finalVal = "0" + finalVal;
        }
        return finalVal;
    }    
}
