/* Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteoinfo.global;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

/**
 *
 * @author Yaqiang Wang
 */
public class DataConvert {

    /**
     * Byte array convert to float
     *
     * @param b Byte array
     * @param byteOrder Byte order
     * @return Float value
     */
    public static float bytes2float(byte[] b, ByteOrder byteOrder) {
        ByteBuffer buf = ByteBuffer.wrap(b);
        buf.order(byteOrder);
        return buf.getFloat();
    }
    
    /**
     * Byte array convert to integer
     * 
     * @param bytes Byte array
     * @param byteOrder Byte order
     * @return Integer value
     */
    public static int bytes2Int(byte[] bytes, ByteOrder byteOrder){
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        buf.order(byteOrder);
        return buf.getInt();
    }

    /**
     * Byte array convert to integer
     *
     * @param bytes byte array
     * @return Integer value
     */
    public static int bytes2Int(byte[] bytes) {
        int result = 0;
        for (int i = 0; i < 4; i++) {
            result = (result << 8) - Byte.MIN_VALUE + (int) bytes[i];
        }
        return result;
    }

    /**
     * Convert byte to int - byte in Java is signed
     *
     * @param b
     * @return
     */
    public static int byte2Int(byte b) {
        return b >= 0 ? (int) b : (int) (b + 256);
    }

    /**
     * Convert OA date to date
     *
     * @param oaDate OA date
     * @return
     */
    public static Date fromOADate(double oaDate) {
        Date date = new Date();
        //long t = (long)((oaDate - 25569) * 24 * 3600 * 1000);
        long t = (long) (oaDate * 1000000);
        date.setTime(t);
        return date;
    }

    /**
     * Convert date to OA date
     *
     * @param date Date
     * @return OA date
     */
    public static double toOADate(Date date) {
        double oaDate = date.getTime();
        //oaDate = oaDate / (24 * 3600 * 1000) + 25569;
        oaDate = oaDate / 1000000;

        return oaDate;
    }

    /**
     * Resize array
     * @param oldArray Old array
     * @param newSize New size
     * @return Resized array
     */
    public static Object resizeArray(Object oldArray, int newSize) {
        int oldSize = java.lang.reflect.Array.getLength(oldArray);
        Class elementType = oldArray.getClass().getComponentType();
        Object newArray = java.lang.reflect.Array.newInstance(
                elementType, newSize);
        int preserveLength = Math.min(oldSize, newSize);
        if (preserveLength > 0) {
            System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
        }
        return newArray;
    }
    
    /**
     * Resize double 2d array
     * @param oldArray Old array
     * @param newSize New size
     * @return Resized array
     */
    public static double[][] resizeArray2D(double[][] oldArray, int newSize){
        int ynum = oldArray.length;
        int xnum = oldArray[0].length;
        double[][] newArray = new double[newSize][xnum];
        for (int j = 0; j < ynum; j++){
            for (int i = 0; i < xnum; i++){
                newArray[j][i] = oldArray[j][i];
            }
        }
        return newArray;
    }
}
