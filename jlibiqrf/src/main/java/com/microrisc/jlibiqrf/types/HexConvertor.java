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
package com.microrisc.jlibiqrf.types;

/**
 * Converts primitive data types into hex format.
 *
 * @author Martin Strouhal
 */
public class HexConvertor {

    private final static char[] hexSymbols = "0123456789ABCDEF".toCharArray();
    
    /**
     * Convert specified array into characters (so char[] is 2x bigger than
     * short[]).
     *
     * @param array to convert
     * @return 2x bigger converted array containg hex values (each number as 2
     * chars).
     */
    // inspirated on stackoverlow - stackoverflow.com/questions/9655181/
    public static char[] shortArrayToHex(short[] array) {
        if (array == null) {
            return new char[]{};
        }
        char[] hexChars = new char[array.length * 2];
        for (int i = 0; i < array.length; i++) {
            int oneNumber = array[i] & 0xFF; // round on 2 places
            hexChars[i * 2] = hexSymbols[oneNumber >>> 4]; // convert first symbol
            hexChars[i * 2 + 1] = hexSymbols[oneNumber & 0x0F]; // convert second
        }
        return hexChars;
    }

    /**
     * Returns converted array as string with hex values int '[' and ']'
     * separated by ','
     *
     * @param array to convert
     * @return string with hex values
     */
    public static String getShortArrayAsStringWithHex(short[] array) {
        char[] hex = shortArrayToHex(array);
        String convertedString = "";
        for (int i = 0; i < hex.length; i += 2) {
            convertedString += "0x" + hex[i] + hex[i + 1] + ", ";
        }
        int sLength = convertedString.length();
        return convertedString.substring(0, (sLength < 2) ? 0 : sLength - 2);
    }
}