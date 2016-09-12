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
 *
 * @author Martin Strouhal
 */
public class DPAStateWatcher {

    private final Queue<DPAAddon> dpaAddons = new LinkedList<>();
    private DPAAddon lastDPAAddon = new DPAAddon(false, -1);

    public void addDPAAddon(DPAAddon addon) {
        dpaAddons.add(addon);
    }

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
