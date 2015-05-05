/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data;

import java.util.ArrayList;
import java.util.List;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;

/**
 *
 * @author wyq
 */
public class ArrayMath {

    private static DataType getDataType(Object o) {
        if (o instanceof Integer) {
            return DataType.INT;
        } else if (o instanceof Float) {
            return DataType.FLOAT;
        } else if (o instanceof Double) {
            return DataType.DOUBLE;
        } else {
            return DataType.OBJECT;
        }
    }

    private static DataType commonType(DataType aType, DataType bType) {
        short anb = ArrayMath.typeToNBytes(aType);
        short bnb = ArrayMath.typeToNBytes(bType);
        return (anb > bnb) ? aType : bType;
    }

    /**
     * Return the number of bytes per element for the given typecode.
     */
    private static short typeToNBytes(final DataType dataType) {
        switch (dataType) {
            case BYTE:
                return 1;
            case SHORT:
                return 2;
            case INT:
            case FLOAT:
                return 4;
            case LONG:
            case DOUBLE:
                return 8;
            case OBJECT:
                return 0;
            default:
                System.out.println("internal error in typeToNBytes");
                return -1;
        }
    }

    /**
     * Array add
     *
     * @param a Array a
     * @param b Array b
     * @return Added array
     */
    public static Array add(Array a, Array b) {
        DataType type = ArrayMath.commonType(a.getDataType(), b.getDataType());
        switch (type) {
            case SHORT:
            case INT:
                return ArrayMath.addInt(a, b);
            case FLOAT:
                return ArrayMath.addFloat(a, b);
            case DOUBLE:
                return ArrayMath.addDouble(a, b);
        }
        return null;
    }

    /**
     * Array add
     *
     * @param a Array a
     * @param b Number b
     * @return Added array
     */
    public static Array add(Array a, Number b) {
        DataType bType = ArrayMath.getDataType(b);
        DataType type = ArrayMath.commonType(a.getDataType(), bType);
        switch (type) {
            case SHORT:
            case INT:
                return ArrayMath.addInt(a, b.intValue());
            case FLOAT:
                return ArrayMath.addFloat(a, b.floatValue());
            case DOUBLE:
                return ArrayMath.addDouble(a, b.doubleValue());
        }
        return null;
    }

    private static Array addInt(Array a, Array b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setInt(i, a.getInt(i) + b.getInt(i));
        }

        return r;
    }

    private static Array addInt(Array a, int b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setInt(i, a.getInt(i) + b);
        }

        return r;
    }

    private static Array addFloat(Array a, Array b) {
        Array r = Array.factory(DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setFloat(i, a.getFloat(i) + b.getFloat(i));
        }

        return r;
    }

    private static Array addFloat(Array a, float b) {
        Array r = Array.factory(DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setFloat(i, a.getFloat(i) + b);
        }

        return r;
    }

    private static Array addDouble(Array a, Array b) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, a.getDouble(i) + b.getDouble(i));
        }

        return r;
    }

    private static Array addDouble(Array a, double b) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, a.getDouble(i) + b);
        }

        return r;
    }

    /**
     * Array subtract
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array sub(Array a, Array b) {
        DataType type = ArrayMath.commonType(a.getDataType(), b.getDataType());
        switch (type) {
            case SHORT:
            case INT:
                return ArrayMath.subInt(a, b);
            case FLOAT:
                return ArrayMath.subFloat(a, b);
            case DOUBLE:
                return ArrayMath.subDouble(a, b);
        }
        return null;
    }

    /**
     * Array subtract
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array sub(Array a, Number b) {
        DataType bType = ArrayMath.getDataType(b);
        DataType type = ArrayMath.commonType(a.getDataType(), bType);
        switch (type) {
            case SHORT:
            case INT:
                return ArrayMath.subInt(a, b.intValue());
            case FLOAT:
                return ArrayMath.subFloat(a, b.floatValue());
            case DOUBLE:
                return ArrayMath.subDouble(a, b.doubleValue());
        }
        return null;
    }

    /**
     * Array subtract
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array sub(Number b, Array a) {
        DataType bType = ArrayMath.getDataType(b);
        DataType type = ArrayMath.commonType(a.getDataType(), bType);
        switch (type) {
            case SHORT:
            case INT:
                return ArrayMath.subInt(b.intValue(), a);
            case FLOAT:
                return ArrayMath.subFloat(b.floatValue(), a);
            case DOUBLE:
                return ArrayMath.subDouble(b.doubleValue(), a);
        }
        return null;
    }

    private static Array subInt(Array a, Array b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setInt(i, a.getInt(i) - b.getInt(i));
        }

        return r;
    }

    private static Array subInt(Array a, int b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setInt(i, a.getInt(i) - b);
        }

        return r;
    }

    private static Array subInt(int b, Array a) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setInt(i, b - a.getInt(i));
        }

        return r;
    }

    private static Array subFloat(Array a, Array b) {
        Array r = Array.factory(DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setFloat(i, a.getFloat(i) - b.getFloat(i));
        }

        return r;
    }

    private static Array subFloat(Array a, float b) {
        Array r = Array.factory(DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setFloat(i, a.getFloat(i) - b);
        }

        return r;
    }

    private static Array subFloat(float b, Array a) {
        Array r = Array.factory(DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setFloat(i, b - a.getFloat(i));
        }

        return r;
    }

    private static Array subDouble(Array a, Array b) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, a.getDouble(i) - b.getDouble(i));
        }

        return r;
    }

    private static Array subDouble(Array a, double b) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, a.getDouble(i) - b);
        }

        return r;
    }

    private static Array subDouble(double b, Array a) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, b - a.getDouble(i));
        }

        return r;
    }

    /**
     * Array mutiply
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array mul(Array a, Array b) {
        DataType type = ArrayMath.commonType(a.getDataType(), b.getDataType());
        switch (type) {
            case SHORT:
            case INT:
                return ArrayMath.mulInt(a, b);
            case FLOAT:
                return ArrayMath.mulFloat(a, b);
            case DOUBLE:
                return ArrayMath.mulDouble(a, b);
        }
        return null;
    }

    /**
     * Array multiply
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array mul(Array a, Number b) {
        DataType bType = ArrayMath.getDataType(b);
        DataType type = ArrayMath.commonType(a.getDataType(), bType);
        switch (type) {
            case SHORT:
            case INT:
                return ArrayMath.mulInt(a, b.intValue());
            case FLOAT:
                return ArrayMath.mulFloat(a, b.floatValue());
            case DOUBLE:
                return ArrayMath.mulDouble(a, b.doubleValue());
        }
        return null;
    }

    private static Array mulInt(Array a, Array b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setInt(i, a.getInt(i) * b.getInt(i));
        }

        return r;
    }

    private static Array mulInt(Array a, int b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setInt(i, a.getInt(i) * b);
        }

        return r;
    }

    private static Array mulFloat(Array a, Array b) {
        Array r = Array.factory(DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setFloat(i, a.getFloat(i) * b.getFloat(i));
        }

        return r;
    }

    private static Array mulFloat(Array a, float b) {
        Array r = Array.factory(DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setFloat(i, a.getFloat(i) * b);
        }

        return r;
    }

    private static Array mulDouble(Array a, Array b) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, a.getDouble(i) * b.getDouble(i));
        }

        return r;
    }

    private static Array mulDouble(Array a, double b) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, a.getDouble(i) * b);
        }

        return r;
    }

    /**
     * Array divide
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array div(Array a, Array b) {
        DataType type = ArrayMath.commonType(a.getDataType(), b.getDataType());
        switch (type) {
            case SHORT:
            case INT:
                return ArrayMath.divInt(a, b);
            case FLOAT:
                return ArrayMath.divFloat(a, b);
            case DOUBLE:
                return ArrayMath.divDouble(a, b);
        }
        return null;
    }

    /**
     * Array divide
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array div(Array a, Number b) {
        DataType bType = ArrayMath.getDataType(b);
        DataType type = ArrayMath.commonType(a.getDataType(), bType);
        switch (type) {
            case SHORT:
            case INT:
                return ArrayMath.divInt(a, b.intValue());
            case FLOAT:
                return ArrayMath.divFloat(a, b.floatValue());
            case DOUBLE:
                return ArrayMath.divDouble(a, b.doubleValue());
        }
        return null;
    }

    /**
     * Array divide
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array div(Number b, Array a) {
        DataType bType = ArrayMath.getDataType(b);
        DataType type = ArrayMath.commonType(a.getDataType(), bType);
        switch (type) {
            case SHORT:
            case INT:
                return ArrayMath.divInt(b.intValue(), a);
            case FLOAT:
                return ArrayMath.divFloat(b.floatValue(), a);
            case DOUBLE:
                return ArrayMath.divDouble(b.doubleValue(), a);
        }
        return null;
    }

    private static Array divInt(Array a, Array b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setInt(i, a.getInt(i) / b.getInt(i));
        }

        return r;
    }

    private static Array divInt(Array a, int b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setInt(i, a.getInt(i) / b);
        }

        return r;
    }

    private static Array divInt(int b, Array a) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setInt(i, b / a.getInt(i));
        }

        return r;
    }

    private static Array divFloat(Array a, Array b) {
        Array r = Array.factory(DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setFloat(i, a.getFloat(i) / b.getFloat(i));
        }

        return r;
    }

    private static Array divFloat(Array a, float b) {
        Array r = Array.factory(DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setFloat(i, a.getFloat(i) / b);
        }

        return r;
    }

    private static Array divFloat(float b, Array a) {
        Array r = Array.factory(DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setFloat(i, b / a.getFloat(i));
        }

        return r;
    }

    private static Array divDouble(Array a, Array b) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, a.getDouble(i) / b.getDouble(i));
        }

        return r;
    }

    private static Array divDouble(Array a, double b) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, a.getDouble(i) / b);
        }

        return r;
    }

    private static Array divDouble(double b, Array a) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, b / a.getDouble(i));
        }

        return r;
    }

    /**
     * Array pow function
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array pow(Array a, Number b) {
        DataType bType = ArrayMath.getDataType(b);
        DataType type = ArrayMath.commonType(a.getDataType(), bType);
        switch (type) {
            case SHORT:
            case INT:
                return ArrayMath.powInt(a, b.intValue());
            case FLOAT:
            case DOUBLE:
                return ArrayMath.powDouble(a, b.doubleValue());
        }
        return null;
    }

    private static Array powInt(Array a, int b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setInt(i, (int) Math.pow(a.getInt(i), b));
        }

        return r;
    }

    private static Array powDouble(Array a, double b) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, Math.pow(a.getInt(i), b));
        }

        return r;
    }

    /**
     * Array absolute
     *
     * @param a Array a
     * @return Result array
     */
    public static Array abs(Array a) {
        Array r = Array.factory(a.getDataType(), a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, Math.abs(r.getDouble(i)));
        }

        return r;
    }

    /**
     * Section array
     *
     * @param a Array a
     * @param origin Origin array
     * @param size Size array
     * @param stride Stride array
     * @return Result array
     * @throws InvalidRangeException
     */
    public static Array section(Array a, int[] origin, int[] size, int[] stride) throws InvalidRangeException {
        return a.section(origin, size, stride);
    }

    /**
     * Get minimum value
     *
     * @param a Array a
     * @return Minimum value
     */
    public static double getMinimum(Array a) {
        IndexIterator iter = a.getIndexIterator();
        double min = 1.7976931348623157E+308D;
        while (iter.hasNext()) {
            double val = iter.getDoubleNext();
            if (!Double.isNaN(val)) {
                if (val < min) {
                    min = val;
                }
            }
        }
        return min;
    }

    /**
     * Get maximum value
     *
     * @param a Array a
     * @return
     */
    public static double getMaximum(Array a) {
        IndexIterator iter = a.getIndexIterator();
        double max = -1.797693134862316E+307D;
        while (iter.hasNext()) {
            double val = iter.getDoubleNext();
            if (!Double.isNaN(val)) {
                if (val > max) {
                    max = val;
                }
            }
        }
        return max;
    }

    /**
     * Summarize array
     *
     * @param a Array a
     * @return Summarize value
     */
    public static double sumDouble(Array a) {
        double sum = 0.0D;
        IndexIterator iterA = a.getIndexIterator();
        while (iterA.hasNext()) {
            sum += iterA.getDoubleNext();
        }
        return sum;
    }

    /**
     * Summarize array skip missing value
     *
     * @param a Array a
     * @param missingValue Missing value
     * @return Summarize value
     */
    public static double sumDouble(Array a, double missingValue) {
        double sum = 0.0D;
        IndexIterator iterA = a.getIndexIterator();
        while (iterA.hasNext()) {
            double val = iterA.getDoubleNext();
            if ((val != missingValue) && (!Double.isNaN(val))) {
                sum += val;
            }
        }
        return sum;
    }

    /**
     * Average array
     *
     * @param a Array a
     * @return Average value
     */
    public static double aveDouble(Array a) {
        double sum = ArrayMath.sumDouble(a);
        return sum / a.getSize();
    }

    /**
     * Average array skip missing value
     *
     * @param a Array a
     * @param missingValue Missing value
     * @return Average value
     */
    public static double aveDouble(Array a, double missingValue) {
        double sum = 0.0D;
        int n = 0;
        IndexIterator iterA = a.getIndexIterator();
        while (iterA.hasNext()) {
            double val = iterA.getDoubleNext();
            if ((val != missingValue) && (!Double.isNaN(val))) {
                sum += val;
                n += 1;
            }
        }
        return sum / n;
    }

    /**
     * As number list
     * @param a Array a
     * @return Result number list
     */
    public static List<Number> asList(Array a) {
        IndexIterator iterA = a.getIndexIterator();
        List<Number> r = new ArrayList<>();
        switch (a.getDataType()) {
            case SHORT:
            case INT:                
                while (iterA.hasNext()) {
                    r.add(iterA.getIntNext());
                }
            case FLOAT:
                while (iterA.hasNext()) {
                    r.add(iterA.getFloatNext());
                }
            case DOUBLE:
                while (iterA.hasNext()) {
                    r.add(iterA.getDoubleNext());
                }
        }
        return r;
    }
}
