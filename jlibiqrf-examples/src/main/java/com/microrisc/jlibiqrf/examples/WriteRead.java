/*
 * Copyright 2015 MICRORISC s.r.o.
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
package com.microrisc.jlibiqrf.examples;

import com.microrisc.jlibiqrf.IQRFListener;
import com.microrisc.jlibiqrf.JLibIQRF;
import com.microrisc.jlibiqrf.configuration.IQRFConfiguration;
import com.microrisc.jlibiqrf.iqrfLayer.serial.SerialConfiguration;
import com.microrisc.jlibiqrf.types.IQRFError;
import java.util.Arrays;

/**
 * Example of using JLibIQRF.
 *
 * @author Martin Strouhal
 */
public class WriteRead implements IQRFListener {

    public static void main(String[] args) {
        
        // Creating one of possible configurations (depending on used communication type)
        IQRFConfiguration iqrfConfig;
        
        //iqrfConfig = new CDCConfiguration("COM5");
        //iqrfConfig = new SPIConfiguration("COM5");
        iqrfConfig = new SerialConfiguration("COM5", 9600);
        //iqrfConfig = new UDPConfiguration("10.0.0.142", 55000, "10.0.0.139", 55300);

        JLibIQRF lib = JLibIQRF.init(iqrfConfig);
        if (lib == null) {
            System.out.println("Some error has been occured while lib creating.");
            System.exit(1);
        }

        // Register listener receiving data from network
        lib.addActionListener(new WriteRead());

        // Send DPA read temperature request
        // NAdr=0x01 0x00 PNum=0x0A PCmd=0x00
        short[] temperatureRequest = {0x00, 0x00, 0x0A, 0x00, 0xFF, 0xFF};

        // Sending read temperature reguest
        int operationResult = lib.sendData(temperatureRequest);
        // Checking result of operation
        if (operationResult != JLibIQRF.SUCCESS_OPERATION) {
            // If some error has been occured, printing his description
            System.out.println(IQRFError.getDescription(operationResult));
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            System.out.println(ex);
        }

        // Terminate library and free up used resources
        lib.destroy();
    }

    @Override
    public void onGetIQRFData(short[] data) {
        System.out.println("Received data: " + Arrays.toString(data));

        // Checking if it's IQRF data
        if (data == null || data.length < 5) {
            return;
        }

        // Checking if it's response to GetTemperature
        if (data[2] == 0x0A && data[3] == 0x80) {
            // Getting integer part of temperature
            short value = data[8];

            // Getting temperature with fractial part
            short[] fullTemperatureValue = new short[2];
            System.arraycopy(data, 9, fullTemperatureValue, 0, 2);
            byte fractialPart = (byte) (fullTemperatureValue[0] & 0x0F);

            System.out.println("Temperature = " + value + "." + fractialPart + " °C");
        }
    }

}
