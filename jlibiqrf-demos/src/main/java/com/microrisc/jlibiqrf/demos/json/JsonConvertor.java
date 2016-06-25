/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.microrisc.jlibiqrf.demos.json;

/**
 * Provides converting IQRF data to json and vice versa.
 *
 * @author Martin Strouhal
 */
public interface JsonConvertor {

    /** Converts string with json data to IQRF data which can be send into IQRF.
     * network.
     *
     * @param json object is with data formatted as json
     * @return short array with individual bytes of IQRF data
     */
    short[] toIQRF(Object json);

    /** Converts IQRF data from IQRF network into string with json data.
     *
     * @param iqrf is short array with individual bytes of IQRF data
     * @return object with data formatted as json
     */
    Object toJson(short[] iqrf);
    
}
