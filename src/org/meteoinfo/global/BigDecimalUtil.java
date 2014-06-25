/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.global;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 *
 * @author wyq
 */
public class BigDecimalUtil {
    /**
     * Add
     * @param d1
     * @param d2
     * @return 
     */
    public static double add(double d1,double d2){  
        BigDecimal b1=new BigDecimal(Double.toString(d1));  
        BigDecimal b2=new BigDecimal(Double.toString(d2));  
        return b1.add(b2).doubleValue();            
    }  
      
    /**
     * Substract
     * @param d1
     * @param d2
     * @return 
     */
    public static double sub(double d1,double d2){  
        BigDecimal b1=new BigDecimal(Double.toString(d1));  
        BigDecimal b2=new BigDecimal(Double.toString(d2));  
        return b1.subtract(b2).doubleValue();            
    }  
      
    /**
     * Multiply
     * @param d1
     * @param d2
     * @return 
     */
    public static double mul(double d1,double d2){  
        BigDecimal b1=new BigDecimal(Double.toString(d1));  
        BigDecimal b2=new BigDecimal(Double.toString(d2));  
        return b1.multiply(b2).doubleValue();            
    }  
      
    /**
     * Divide
     * @param d1
     * @param d2
     * @return 
     */
    public static double div(double d1,double d2){  
        return div(d1,d2,20);            
    }  
      
    /**
     * Divide
     * @param d1
     * @param d2
     * @param scale
     * @return 
     */
    public static double div(double d1,double d2,int scale){  
        if(scale<0){  
            throw new IllegalArgumentException("The scale must be a positive integer or zero");  
        }  
        BigDecimal b1=new BigDecimal(Double.toString(d1));  
        BigDecimal b2=new BigDecimal(Double.toString(d2));  
        return b1.divide(b2,scale,BigDecimal.ROUND_HALF_UP).doubleValue();            
    }  
    
    /**
     * Power
     * @param d1
     * @param d2
     * @return Power value
     */
    public static double pow(double d1,int d2){  
        BigDecimal b1=new BigDecimal(Double.toString(d1));   
        return b1.pow(d2, new MathContext(10)).doubleValue();            
    }  
}
