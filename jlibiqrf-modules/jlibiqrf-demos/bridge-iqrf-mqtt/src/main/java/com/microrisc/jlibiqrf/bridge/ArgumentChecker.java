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

/**
 * Provides advanced functionality for checking input data in parameters via
 * static methods. In case of illegal data is {@link IllegalArgumentException}
 * thrown. Interfaces provides advanced logging like using variable names of
 * checked data - each method has overloaded method which ever call second
 * method with string parameter in which can be defined variable name of checked
 * data. In case, that it is used function without string name, is used
 * internally only word "Value".
 *
 * @author Martin Strouhal
 */
public class ArgumentChecker {
    
    public static void checkNull(Object object, String nameOfObject){
        if(object == null){
            throw new IllegalArgumentException(nameOfObject + " cannot be null");
        }
    }
    
    public static void checkNull(Object object){
        checkNull(object, "Object");
    }
    
    public static void checkNegative(double number, String nameOfVal){
        if(number < 0){
            throw new IllegalArgumentException(nameOfVal + " cannot be negative.");
        }
    }
    
    public static void checkNegative(double number){
        checkNegative(number, "Value");
    }
    
    public static void checkInterval(double valueToCheck, double startInterval, 
            double endInterval, String nameOfValue){
        if(valueToCheck < startInterval || valueToCheck > endInterval){
            throw new IllegalArgumentException(nameOfValue + " must be in "
                    + "interval <" + startInterval + ", " + endInterval + ">");
        }
    }

    public static void checkInterval(double valueToCheck, double startInterval, 
            double endInterval){
        checkInterval(valueToCheck, startInterval, endInterval, "Value");
    }    
    
}
