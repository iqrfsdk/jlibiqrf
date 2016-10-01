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

import java.util.LinkedList;
import java.util.Queue;

/**
 * Provides functionality for watching {@link DPAAddon}. It requires very exact 
 * watching, in other case can mechanisms stand not working and there isn't 
 * functionality for preventation before this state.
 * 
 * @author Martin Strouhal
 */
public class DPAStateWatcher {

    private final Queue<DPAAddon> dpaAddons = new LinkedList<>();
    private DPAAddon lastDPAAddon = new DPAAddon(false, -1);

    /** 
     * Add to queue {@link DPAAddon} which will be returned after previous DPA 
     * addons.
     * @param addon to add
     */
    public void addDPAAddon(DPAAddon addon) {
        dpaAddons.add(addon);
    }

    /**
     * Returns {@link DPAAddon} which should be for required purpose (in case
     * correct using). Otherwise can stand watching mechanism not working.
     * <br><b>Note:</b> When isn't in queue any addon, is returned last
     * previously returned addon. If any addon doesn't exist, it is returned
     * default addon - see declaration of {@link DPAStateWatcher#lastDPAAddon}
     *
     * @return {@link DPAAddon}
     */
    public DPAAddon getDPAAddon() {
        DPAAddon addon = dpaAddons.poll();
        if (addon == null) {
            return lastDPAAddon;
        } else {
            lastDPAAddon = addon;
            return addon;
        }
    }
}
