/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import org.meteoinfo.global.util.GlobalUtil;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;

/**
 *
 * @author yaqiang
 */
public class ArrayUtil {

    // <editor-fold desc="File">
    /**
     * Read ASCII data file to an array
     *
     * @param fileName File name
     * @param delimiter Delimiter
     * @param headerLines Headerline number
     * @param dataType Data type string
     * @param shape Shape
     * @return Result array
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static Array readASCIIFile(String fileName, String delimiter, int headerLines, String dataType, List<Integer> shape) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        BufferedReader sr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        if (headerLines > 0) {
            for (int i = 0; i < headerLines; i++) {
                sr.readLine();
            }
        }

        DataType dt = DataType.DOUBLE;
        if (dataType != null) {
            dataType = dataType.split("%")[1];
            dt = ArrayUtil.toDataType(dataType);
        }

        int i;
        int[] ss = new int[shape.size()];
        for (i = 0; i < shape.size(); i++) {
            ss[i] = shape.get(i);
        }
        Array a = Array.factory(dt, ss);

        String[] dataArray;
        i = 0;
        String line = sr.readLine();
        while (line != null) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            dataArray = GlobalUtil.split(line, delimiter);
            for (String dstr : dataArray) {
                a.setDouble(i, Double.parseDouble(dstr));
                i += 1;
            }

            line = sr.readLine();
        }
        sr.close();

        return a;
    }
    
    // </editor-fold>

    // <editor-fold desc="Create">
    /**
     * Create an array
     *
     * @param data Array like data
     * @return Array
     */
    public static Array array(List<Object> data) {
        Object d0 = data.get(0);
        if (d0 instanceof Number) {
            DataType dt = ArrayUtil.objectsToType(data);
            Array a = Array.factory(dt, new int[]{data.size()});
            for (int i = 0; i < data.size(); i++) {
                a.setObject(i, data.get(i));
            }
            return a;
        } else if (d0 instanceof List) {
            int ndim = data.size();
            int len = ((List) d0).size();
            DataType dt = DataType.FLOAT;
            Array a = Array.factory(dt, new int[]{ndim, len});
            for (int i = 0; i < ndim; i++) {
                List<Object> d = (List) data.get(i);
                for (int j = 0; j < len; j++) {
                    a.setObject(i * len + j, d.get(j));
                }
            }
            return a;
        } else {
            return null;
        }
    }

    /**
     * Array range
     *
     * @param start Start value
     * @param stop Stop value
     * @param step Step value
     * @return Array
     */
    public static Array arrayRange(Number start, Number stop, final Number step) {
        if (stop == null) {
            stop = start;
            start = 0;
        }
        DataType dataType = ArrayUtil.objectsToType(new Object[]{
            start,
            stop,
            step});
        double startv = start.doubleValue();
        double stopv = stop.doubleValue();
        double stepv = step.doubleValue();
        final int length = Math.max(0, (int) Math.ceil((stopv
                - startv) / stepv));
        Array a = Array.factory(dataType, new int[]{length});
        for (int i = 0; i < length; i++) {
            a.setObject(i, i * stepv + startv);
        }
        return a;
    }

    /**
     * Array range
     *
     * @param start Start value
     * @param length Length
     * @param step Step value
     * @return Array
     */
    public static Array arrayRange(Number start, final int length, final Number step) {
        DataType dataType = ArrayUtil.objectsToType(new Object[]{
            start,
            step});
        double startv = start.doubleValue();
        double stepv = step.doubleValue();
        Array a = Array.factory(dataType, new int[]{length});
        for (int i = 0; i < length; i++) {
            a.setObject(i, i * stepv + startv);
        }
        return a;
    }

    /**
     * Array line space
     *
     * @param start Start value
     * @param stop Stop value
     * @param n Number value
     * @return Array
     */
    public static Array lineSpace(Number start, Number stop, final int n) {
        if (stop == null) {
            stop = start;
            start = 0;
        }
//        DataType dataType = ArrayMath.objectsToType(new Object[]{
//            start,
//            stop});
        double startv = start.doubleValue();
        double stopv = stop.doubleValue();
        double stepv = (stopv - startv) / (n - 1);
        Array a = Array.factory(DataType.FLOAT, new int[]{n});
        for (int i = 0; i < n; i++) {
            a.setObject(i, i * stepv + startv);
        }
        return a;
    }

    /**
     * Get zero array
     *
     * @param n Number
     * @return Array
     */
    public static Array zeros(int n) {
        Array a = Array.factory(DataType.FLOAT, new int[]{n});
        for (int i = 0; i < n; i++) {
            a.setFloat(i, 0);
        }

        return a;
    }

    /**
     * Get ones array
     *
     * @param n Number
     * @return Array
     */
    public static Array ones(int n) {
        Array a = Array.factory(DataType.FLOAT, new int[]{n});
        for (int i = 0; i < n; i++) {
            a.setFloat(i, 1);
        }

        return a;
    }

    private static DataType objectsToType(final Object[] objects) {
        if (objects.length == 0) {
            return DataType.INT;
        }
        short new_sz, sz = -1;
        DataType dataType = DataType.INT;
        for (final Object o : objects) {
            final DataType _type = ArrayMath.getDataType(o);
            new_sz = ArrayMath.typeToNBytes(_type);
            if (new_sz > sz) {
                dataType = _type;
            }
        }
        return dataType;
    }

    private static DataType objectsToType(final List<Object> objects) {
        if (objects.isEmpty()) {
            return DataType.INT;
        }
        short new_sz, sz = -1;
        DataType dataType = DataType.INT;
        for (final Object o : objects) {
            final DataType _type = ArrayMath.getDataType(o);
            new_sz = ArrayMath.typeToNBytes(_type);
            if (new_sz > sz) {
                dataType = _type;
            }
        }
        return dataType;
    }

    // </editor-fold>
    // <editor-fold desc="Output">
    /**
     * Array to string
     *
     * @param a Array a
     * @return String
     */
    public static String toString(Array a) {
        StringBuilder sbuff = new StringBuilder();        
        sbuff.append("array(");
        int ndim = a.getRank();
        if (ndim > 1)
            sbuff.append("[");
        int i = 0;
        int shapeIdx = ndim - 1;
        int len = a.getShape()[shapeIdx];
        IndexIterator ii = a.getIndexIterator();
        while (ii.hasNext()) {
            if (i == 0) {                          
                sbuff.append("[");
            }
            Object data = ii.getObjectNext();
            sbuff.append(data);            
            i += 1;
            if (i == len){
                sbuff.append("]");
                len = a.getShape()[shapeIdx];
                i = 0;
            } else {
                sbuff.append(", ");
            }
        }
        if (ndim > 1)
            sbuff.append("]");
        return sbuff.toString();
    }
    // </editor-fold>
    
    // <editor-fold desc="Convert">
    /**
     * To data type - ucar.ma2
     * @param dt Data type string
     * @return Data type
     */
    public static DataType toDataType(String dt) {
        switch (dt) {
            case "C":
                return DataType.STRING;
            case "i":
                return DataType.INT;
            case "f":
                return DataType.FLOAT;
            case "d":
                return DataType.DOUBLE;
            default:
                return DataType.OBJECT;
        }
    }
    
    /**
     * To data type - MeteoInfo
     * @param dt Data type string
     * @return Data type
     */
    public static DataTypes toDataTypes(String dt){
        switch (dt) {
            case "C":
                return DataTypes.String;
            case "i":
                return DataTypes.Integer;
            case "f":
                return DataTypes.Float;
            case "d":
                return DataTypes.Double;
            default:
                return DataTypes.String;
        }
    }
    // </editor-fold>
}
