import com.microrisc.jlibiqrf.configuration.IQRFConfiguration;
import com.microrisc.jlibiqrf.demos.config.SimpleBridgeConfigurationLoader;
import com.microrisc.jlibiqrf.demos.config.BridgeConfiguration;
import com.microrisc.jlibiqrf.iqrfLayer.cdc.CDCConfiguration;

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

/**
 *
 * @author Martin Strouhal
 */
public class Configuration {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("start");
        
        IQRFConfiguration cdcConfig = new CDCConfiguration("COM5");
        
        BridgeConfiguration config = new BridgeConfiguration.ConfigurationBuilder("adresa", cdcConfig)
                .checkingInterval(10000)
                .clientId("client idčko")
                .build();
        
        System.out.println("before: " + config.toString());
        
        System.out.println("-------------------------------------------------");
        
        //  SimpleBridgeConfigurationLoader.getInstance().saveBridgeConfiguration(
        //        config, "src/config/config.xml");

        System.out.println("-------------------------------------------------");
        
        config = SimpleBridgeConfigurationLoader.getInstance().load("src/config/config.xml");
        System.out.println("after: " + config.toString());
        
        System.out.println(config);
        
        System.out.println("-------------------------------------------------");
        
        SimpleBridgeConfigurationLoader.getInstance().saveBridgeConfiguration(
                config, "src/config/config.xml");
        
        System.out.println("end");
    }
    
}
