
import com.microrisc.jlibiqrf.JLibIQRF;
import com.microrisc.jlibiqrf.configuration.IQRFConfiguration;
import com.microrisc.jlibiqrf.iqrfLayer.cdc.CDCConfiguration;
import com.microrisc.jlibiqrf.types.IQRFError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.microrisc.jlibiqrf.IQRFListener;

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

/**
 *  Simply test of JLibIQRF
 * 
 * @author Martin Strouhal
 */
public class Test implements IQRFListener {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(Test.class);

    public static void main(String[] args) throws InterruptedException {
        //-Djava.library.path=src/main/resources/natives/x64
        Test test = new Test();

        IQRFConfiguration iqrfConfig;
        iqrfConfig = new CDCConfiguration("COM5");
        //iqrfConfig = new SPIConfiguration("COM5");
        //iqrfConfig = new SerialConfiguration("COM5", 9600);
        //iqrfConfig = new UDPConfiguration("10.0.0.144", 55000, "10.0.0.139", 55300, UDPIQRFLayer.MAX_RECEIVED_PACKET_SIZE, 0);

        JLibIQRF iqrf = JLibIQRF.init(iqrfConfig);

        iqrf.addIQRFListener(test);

        int result = iqrf.sendData(new short[]{0x00, 0x00, 0x07, 0x01, 0xFF, 0xFF});
        if (result != JLibIQRF.SUCCESS_OPERATION) {
            System.out.println("Some error has been occurred.");
            System.out.println("Error description: " + IQRFError.getDescription(result));
        }

        Thread.sleep(5000);
        iqrf.destroy();
    }

    @Override
    public void onGetIQRFData(short[] data) {
        logger.info("received data: " + data.toString());
    }
}