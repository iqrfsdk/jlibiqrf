/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.microrisc.jlibiqrf.demos.json;

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
