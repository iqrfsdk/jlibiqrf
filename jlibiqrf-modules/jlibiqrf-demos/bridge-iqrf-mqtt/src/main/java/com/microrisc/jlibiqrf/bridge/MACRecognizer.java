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
package com.microrisc.jlibiqrf.bridge;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides funcionality of recognizing MAC address of NIC in device.
 * 
 * @author Martin Strouhal
 */
public class MACRecognizer {

    private static final Logger log = LoggerFactory.getLogger(MACRecognizer.class);
    
    /**
     * Returns mac address.
     * @return mac address as string (without double-dots)
     */
    public static String getMAC() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            log.debug("Current IP address: " + ip.getHostAddress());

            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
            while (networks.hasMoreElements()) {
                NetworkInterface network = networks.nextElement();
                byte[] mac = network.getHardwareAddress();

                if (mac != null) {
                    log.debug("Current MAC address: ");

                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X", mac[i]));
                    }
                    return sb.toString();
                }
            }
        } catch (UnknownHostException | SocketException e) {
            log.error("Cannot find MAC address! " + e.getMessage());
            return "000000000000";
        }
        log.error("Cannot find MAC address!");
        return "000000000000";
    }

}
