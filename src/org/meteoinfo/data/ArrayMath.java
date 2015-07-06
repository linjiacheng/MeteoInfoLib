/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data;

import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.geoprocess.GeoComputation;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.shape.PolygonShape;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.ma2.Range;

/**
 *
 * @author wyq
 */
public class ArrayMath {

    // <editor-fold desc="Data type">
    /**
     * Get data type
     *
     * @param o Object
     * @return Data type
     */
    public static DataType getDataType(Object o) {
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
        if (aType == bType) {
            return aType;
        }

        short anb = ArrayMath.typeToNBytes(aType);
        short bnb = ArrayMath.typeToNBytes(bType);
        if (anb == bnb) {
            switch (aType) {
                case INT:
                case LONG:
                    return bType;
                case FLOAT:
                case DOUBLE:
                    return aType;
            }
        }

        return (anb > bnb) ? aType : bType;
    }

    /**
     * Return the number of bytes per element for the given typecode.
     *
     * @param dataType Data type
     * @return Bytes number
     */
    public static short typeToNBytes(final DataType dataType) {
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

    // </editor-fold>
    // <editor-fold desc="Arithmetic">
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
            r.setDouble(i, Math.pow(a.getDouble(i), b));
        }

        return r;
    }

    /**
     * Sqrt function
     *
     * @param a Array a
     * @return Result array
     */
    public static Array sqrt(Array a) {
        return ArrayMath.pow(a, 0.5);
    }

    /**
     * Exponent function
     *
     * @param a Array a
     * @return Result array
     */
    public static Array exp(Array a) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, Math.exp(a.getDouble(i)));
        }

        return r;
    }

    /**
     * Log function
     *
     * @param a Array a
     * @return Result array
     */
    public static Array log(Array a) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, Math.log(a.getDouble(i)));
        }

        return r;
    }

    /**
     * Log10 function
     *
     * @param a Array a
     * @return Result array
     */
    public static Array log10(Array a) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, Math.log10(a.getDouble(i)));
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
            r.setDouble(i, Math.abs(a.getDouble(i)));
        }

        return r;
    }

    /**
     * Array equal
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array equal(Array a, Array b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) == b.getDouble(i)) {
                r.setDouble(i, 1);
            } else {
                r.setDouble(i, 0);
            }
        }

        return r;
    }

    /**
     * Array equal
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array equal(Array a, Number b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) == b.doubleValue()) {
                r.setDouble(i, 1);
            } else {
                r.setDouble(i, 0);
            }
        }

        return r;
    }

    /**
     * Array less than
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array lessThan(Array a, Array b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) < b.getDouble(i)) {
                r.setDouble(i, 1);
            } else {
                r.setDouble(i, 0);
            }
        }

        return r;
    }

    /**
     * Array less than
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array lessThan(Array a, Number b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) < b.doubleValue()) {
                r.setDouble(i, 1);
            } else {
                r.setDouble(i, 0);
            }
        }

        return r;
    }

    /**
     * Array less than or equal
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array lessThanOrEqual(Array a, Array b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) <= b.getDouble(i)) {
                r.setDouble(i, 1);
            } else {
                r.setDouble(i, 0);
            }
        }

        return r;
    }

    /**
     * Array less than or equal
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array lessThanOrEqual(Array a, Number b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) <= b.doubleValue()) {
                r.setDouble(i, 1);
            } else {
                r.setDouble(i, 0);
            }
        }

        return r;
    }

    /**
     * Array greater than
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array greaterThan(Array a, Array b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) > b.getDouble(i)) {
                r.setDouble(i, 1);
            } else {
                r.setDouble(i, 0);
            }
        }

        return r;
    }

    /**
     * Array greater than
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array greaterThan(Array a, Number b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) > b.doubleValue()) {
                r.setDouble(i, 1);
            } else {
                r.setDouble(i, 0);
            }
        }

        return r;
    }

    /**
     * Array greater than or equal
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array greaterThanOrEqual(Array a, Array b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) >= b.getDouble(i)) {
                r.setDouble(i, 1);
            } else {
                r.setDouble(i, 0);
            }
        }

        return r;
    }

    /**
     * Array greater than or equal
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array greaterThanOrEqual(Array a, Number b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) >= b.doubleValue()) {
                r.setDouble(i, 1);
            } else {
                r.setDouble(i, 0);
            }
        }

        return r;
    }

    /**
     * Array not equal
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array notEqual(Array a, Array b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) != b.getDouble(i)) {
                r.setDouble(i, 1);
            } else {
                r.setDouble(i, 0);
            }
        }

        return r;
    }

    /**
     * Array not equal
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array notEqual(Array a, Number b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) != b.doubleValue()) {
                r.setDouble(i, 1);
            } else {
                r.setDouble(i, 0);
            }
        }

        return r;
    }

    // </editor-fold>
    // <editor-fold desc="Circular function">
    /**
     * Sine function
     *
     * @param a Array a
     * @return Result array
     */
    public static Array sin(Array a) {
        Array r = Array.factory(a.getDataType() == DataType.DOUBLE ? DataType.DOUBLE : DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, Math.sin(a.getDouble(i)));
        }

        return r;
    }

    /**
     * Cosine function
     *
     * @param a Array a
     * @return Result array
     */
    public static Array cos(Array a) {
        Array r = Array.factory(a.getDataType() == DataType.DOUBLE ? DataType.DOUBLE : DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, Math.cos(a.getDouble(i)));
        }

        return r;
    }

    // </editor-fold>
    // <editor-fold desc="Section">
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
        Array r = a.section(origin, size, stride);
        Array rr = Array.factory(r.getDataType(), r.getShape());
        MAMath.copy(rr, r);
        return rr;
    }

    /**
     * Section array
     *
     * @param a Array a
     * @param ranges Ranges
     * @return Result array
     * @throws InvalidRangeException
     */
    public static Array section(Array a, List<Range> ranges) throws InvalidRangeException {
        Array r = a.section(ranges);
        Array rr = Array.factory(r.getDataType(), r.getShape());
        MAMath.copy(rr, r);
        return rr;
    }

    /**
     * Set section
     *
     * @param a Array a
     * @param ranges Ranges
     * @param v Value
     * @throws InvalidRangeException
     */
    public static void setSection(Array a, List<Range> ranges, Number v) throws InvalidRangeException {
        Array r = a.section(ranges);
        IndexIterator iter = r.getIndexIterator();
        while (iter.hasNext()) {
            iter.setObjectNext(v);
        }
        a = Array.factory(a.getDataType(), a.getShape(), r.getStorage());
        r = null;
    }

    // </editor-fold>
    // <editor-fold desc="Statistics">
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
     * Get minimum value
     *
     * @param a Array a
     * @param missingv Missing value
     * @return Minimum value
     */
    public static double getMinimum(Array a, double missingv) {
        IndexIterator iter = a.getIndexIterator();
        double min = 1.7976931348623157E+308D;
        while (iter.hasNext()) {
            double val = iter.getDoubleNext();
            if (!MIMath.doubleEquals(val, missingv)) {
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
     * @param missingv Missing value
     * @return
     */
    public static double getMaximum(Array a, double missingv) {
        IndexIterator iter = a.getIndexIterator();
        double max = -1.797693134862316E+307D;
        while (iter.hasNext()) {
            double val = iter.getDoubleNext();
            if (!MIMath.doubleEquals(val, missingv)) {
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

    // </editor-fold>
    // <editor-fold desc="Convert">
    /**
     * Set missing value to NaN
     *
     * @param a Array a
     * @param missingv Missing value
     */
    public static void missingToNaN(Array a, Number missingv) {
        IndexIterator iterA = a.getIndexIterator();
        switch (a.getDataType()) {
            case INT:
            case FLOAT:
                while (iterA.hasNext()) {
                    float val = iterA.getFloatNext();
                    if (val == missingv.floatValue()) {
                        iterA.setFloatCurrent(Float.NaN);
                    }
                }
            default:
                while (iterA.hasNext()) {
                    double val = iterA.getDoubleNext();
                    if (MIMath.doubleEquals(val, missingv.doubleValue())) {
                        iterA.setDoubleCurrent(Double.NaN);
                    }
                }
        }
    }

    /**
     * Set value
     *
     * @param a Array a
     * @param b Array b - 0/1 data
     * @param value Value
     */
    public static void setValue(Array a, Array b, Number value) {
        for (int i = 0; i < a.getSize(); i++) {
            if (b.getInt(i) == 1) {
                a.setObject(i, value);
            }
        }
    }

    /**
     * As number list
     *
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

    // </editor-fold>       
    // <editor-fold desc="Location">
    /**
     * In polygon function
     *
     * @param a Array a
     * @param x X dimension values
     * @param y Y dimension values
     * @param layer Polygon vector layer
     * @return Result array with cell values of 1 inside polygons and -1 outside
     * polygons
     */
    public static Array inPolygon(Array a, List<Number> x, List<Number> y, VectorLayer layer) {
        List<PolygonShape> polygons = (List<PolygonShape>) layer.getShapes();
        return ArrayMath.inPolygon(a, x, y, polygons);
    }

    /**
     * In polygon function
     *
     * @param a Array a
     * @param x X dimension values
     * @param y Y dimension values
     * @param polygons PolygonShape list
     * @return Result array with cell values of 1 inside polygons and -1 outside
     * polygons
     */
    public static Array inPolygon(Array a, List<Number> x, List<Number> y, List<PolygonShape> polygons) {
        if (a.getRank() == 2) {
            int xNum = x.size();
            int yNum = y.size();

            Array r = Array.factory(DataType.INT, a.getShape());
            for (int i = 0; i < yNum; i++) {
                for (int j = 0; j < xNum; j++) {
                    if (GeoComputation.pointInPolygons(polygons, new PointD(x.get(j).doubleValue(), y.get(i).doubleValue()))) {
                        r.setInt(i * xNum + j, 1);
                    } else {
                        r.setInt(i * xNum + j, -1);
                    }
                }
            }

            return r;
        } else if (a.getRank() == 1) {
            int n = x.size();
            Array r = Array.factory(DataType.INT, a.getShape());
            for (int i = 0; i < n; i++) {
                if (GeoComputation.pointInPolygons(polygons, new PointD(x.get(i).doubleValue(), y.get(i).doubleValue()))) {
                    r.setInt(i, 1);
                } else {
                    r.setInt(i, -1);
                }
            }

            return r;
        }

        return null;
    }

    /**
     * In polygon function
     *
     * @param a Array a
     * @param x X dimension values
     * @param y Y dimension values
     * @param x_p X coordinate of the polygon
     * @param y_p Y coordinate of the polygon
     * @return Result array with cell values of 1 inside polygons and -1 outside
     * polygons
     */
    public static Array inPolygon(Array a, List<Number> x, List<Number> y, List<Number> x_p, List<Number> y_p) {
        PolygonShape ps = new PolygonShape();
        List<PointD> points = new ArrayList<>();
        for (int i = 0; i < x_p.size(); i++){
            points.add(new PointD(x_p.get(i).doubleValue(), y_p.get(i).doubleValue()));
        }
        ps.setPoints(points);
        List<PolygonShape> shapes = new ArrayList<>();
        shapes.add(ps);
        
        return inPolygon(a, x, y, shapes);
    }

    /**
     * Maskout function
     *
     * @param a Array a
     * @param x X dimension values
     * @param y Y dimension values
     * @param layer VectorLayer
     * @param missingValue Missing value
     * @return Result array with cell values of missing outside polygons
     */
    public static Array maskout(Array a, List<Number> x, List<Number> y, VectorLayer layer, Number missingValue) {
        List<PolygonShape> polygons = (List<PolygonShape>) layer.getShapes();
        return ArrayMath.maskout(a, x, y, polygons, missingValue);
    }
    
    /**
     * Maskout function
     *
     * @param a Array a
     * @param x X dimension values
     * @param y Y dimension values
     * @param polygon Polygon shape
     * @param missingValue Missing value
     * @return Result array with cell values of missing outside polygons
     */
    public static Array maskout(Array a, List<Number> x, List<Number> y, PolygonShape polygon, Number missingValue) {
        List<PolygonShape> polygons = new ArrayList<>();
        polygons.add(polygon);
        return ArrayMath.maskout(a, x, y, polygons, missingValue);
    }

    /**
     * Maskout function
     *
     * @param a Array a
     * @param x X dimension values
     * @param y Y dimension values
     * @param polygons PolygonShape list
     * @param missingValue Missing value
     * @return Result array with cell values of missing outside polygons
     */
    public static Array maskout(Array a, List<Number> x, List<Number> y, List<PolygonShape> polygons, Number missingValue) {
        int xNum = x.size();
        int yNum = y.size();

        Array r = Array.factory(a.getDataType(), a.getShape());
        int idx;
        for (int i = 0; i < yNum; i++) {
            for (int j = 0; j < xNum; j++) {
                idx = i * xNum + j;
                if (GeoComputation.pointInPolygons(polygons, new PointD(x.get(j).doubleValue(), y.get(i).doubleValue()))) {
                    r.setObject(idx, a.getObject(idx));
                } else {
                    r.setObject(idx, missingValue);
                }
            }
        }

        return r;
    }

    /**
     * Maskout function
     *
     * @param a Array a
     * @param m Array mask
     * @param missingValue Missing value
     * @return Result array
     */
    public static Array maskout(Array a, Array m, Number missingValue) {
        Array r = Array.factory(a.getDataType(), a.getShape());
        int n = (int) a.getSize();
        for (int i = 0; i < n; i++) {
            if (m.getDouble(i) < 0) {
                r.setObject(i, missingValue);
            } else {
                r.setObject(i, a.getObject(i));
            }
        }

        return r;
    }

    // </editor-fold>
    // <editor-fold desc="Regress">
    /**
     * Get correlation coefficient How well did the forecast values correspond
     * to the observed values? Range: -1 to 1. Perfect score: 1.
     *
     * @param xData X data array
     * @param yData Y data array
     * @return Correlation coefficent
     */
    public static float getR(List<Number> xData, List<Number> yData) {
        int n = xData.size();
        double x_sum = 0;
        double y_sum = 0;
        for (int i = 0; i < n; i++) {
            x_sum += xData.get(i).doubleValue();
            y_sum += yData.get(i).doubleValue();
        }
        double sx_sum = 0.0;
        double sy_sum = 0.0;
        double xy_sum = 0.0;
        for (int i = 0; i < n; i++) {
            sx_sum += xData.get(i).doubleValue() * xData.get(i).doubleValue();
            sy_sum += yData.get(i).doubleValue() * yData.get(i).doubleValue();
            xy_sum += xData.get(i).doubleValue() * yData.get(i).doubleValue();
        }

        double r = (n * xy_sum - x_sum * y_sum) / (Math.sqrt(n * sx_sum - x_sum * x_sum) * Math.sqrt(n * sy_sum - y_sum * y_sum));
        return (float) r;
    }

    /**
     * Determine the least square trend equation - linear fitting
     *
     * @param xData X data array
     * @param yData Y data array
     * @return Result array - y intercept and slope
     */
    public static double[] leastSquareTrend(List<Number> xData, List<Number> yData) {
        int n = xData.size();
        double sumX = 0.0;
        double sumY = 0.0;
        double sumSquareX = 0.0;
        double sumXY = 0.0;
        for (int i = 0; i < n; i++) {
            sumX += xData.get(i).doubleValue();
            sumY += yData.get(i).doubleValue();
            sumSquareX += xData.get(i).doubleValue() * xData.get(i).doubleValue();
            sumXY += xData.get(i).doubleValue() * yData.get(i).doubleValue();
        }

        double a = (sumSquareX * sumY - sumX * sumXY) / (n * sumSquareX - sumX * sumX);
        double b = (n * sumXY - sumX * sumY) / (n * sumSquareX - sumX * sumX);

        return new double[]{a, b};
    }

    /**
     * Linear regress
     *
     * @param xData X data array
     * @param yData Y data array
     * @return Result array - y intercept, slope and correlation coefficent
     */
    public static double[] lineRegress(List<Number> xData, List<Number> yData) {
        int n = xData.size();
        double x_sum = 0;
        double y_sum = 0;
        double sx_sum = 0.0;
        double sy_sum = 0.0;
        double xy_sum = 0.0;
        for (int i = 0; i < n; i++) {
            x_sum += xData.get(i).doubleValue();
            y_sum += yData.get(i).doubleValue();
            sx_sum += xData.get(i).doubleValue() * xData.get(i).doubleValue();
            sy_sum += yData.get(i).doubleValue() * yData.get(i).doubleValue();
            xy_sum += xData.get(i).doubleValue() * yData.get(i).doubleValue();
        }

        double r = (n * xy_sum - x_sum * y_sum) / (Math.sqrt(n * sx_sum - x_sum * x_sum) * Math.sqrt(n * sy_sum - y_sum * y_sum));
        double a = (sx_sum * y_sum - x_sum * xy_sum) / (n * sx_sum - x_sum * x_sum);
        double b = (n * xy_sum - x_sum * y_sum) / (n * sx_sum - x_sum * x_sum);

        return new double[]{a, b, r};
    }

    /**
     * Linear regress
     *
     * @param xData X data array
     * @param yData Y data array
     * @return Result array - y intercept, slope and correlation coefficent
     */
    public static double[] lineRegress(Array xData, Array yData) {
        double x_sum = 0;
        double y_sum = 0;
        double sx_sum = 0.0;
        double sy_sum = 0.0;
        double xy_sum = 0.0;
        int n = 0;
        for (int i = 0; i < xData.getSize(); i++) {
            if (Double.isNaN(xData.getDouble(i))) {
                continue;
            }
            if (Double.isNaN(yData.getDouble(i))) {
                continue;
            }
            x_sum += xData.getDouble(i);
            y_sum += yData.getDouble(i);
            sx_sum += xData.getDouble(i) * xData.getDouble(i);
            sy_sum += yData.getDouble(i) * yData.getDouble(i);
            xy_sum += xData.getDouble(i) * yData.getDouble(i);
            n += 1;
        }

        double r = (n * xy_sum - x_sum * y_sum) / (Math.sqrt(n * sx_sum - x_sum * x_sum) * Math.sqrt(n * sy_sum - y_sum * y_sum));
        double intercept = (sx_sum * y_sum - x_sum * xy_sum) / (n * sx_sum - x_sum * x_sum);
        double slope = (n * xy_sum - x_sum * y_sum) / (n * sx_sum - x_sum * x_sum);

        return new double[]{slope, intercept, r};
    }

    /**
     * Evaluate a polynomial at specific values. If p is of length N, this
     * function returns the value: p[0]*x**(N-1) + p[1]*x**(N-2) + ... +
     * p[N-2]*x + p[N-1]
     *
     * @param p array_like or poly1d object
     * @param x array_like or poly1d object
     * @return ndarray or poly1d
     */
    public static Array polyVal(List<Number> p, Array x) {
        int n = p.size();
        Array r = Array.factory(DataType.DOUBLE, x.getShape());
        for (int i = 0; i < x.getSize(); i++) {
            double val = x.getDouble(i);
            double rval = 0.0;
            for (int j = 0; j < n; j++) {
                rval += p.get(j).doubleValue() * Math.pow(val, n - j - 1);
            }
            r.setDouble(i, rval);
        }

        return r;
    }

    // </editor-fold>    
    // <editor-fold desc="Meteo">

    /**
     * Performs a centered difference operation on a grid data in the x or y
     * direction
     *
     * @param data The grid data
     * @param isX If is x direction
     * @return Result grid data
     */
    public static Array cdiff(Array data, boolean isX) {
        int xnum = data.getShape()[1];
        int ynum = data.getShape()[0];
        Array r = Array.factory(DataType.DOUBLE, data.getShape());
        for (int i = 0; i < ynum; i++) {
            for (int j = 0; j < xnum; j++) {
                if (i == 0 || i == ynum - 1 || j == 0 || j == xnum - 1) {
                    r.setDouble(i * xnum + j, Double.NaN);
                } else {
                    double a, b;
                    if (isX) {
                        a = data.getDouble(i * xnum + j + 1);
                        b = data.getDouble(i * xnum + j - 1);
                    } else {
                        a = data.getDouble((i + 1) * xnum + j);
                        b = data.getDouble((i - 1) * xnum + j);
                    }
                    if (Double.isNaN(a) || Double.isNaN(b)) {
                        r.setDouble(i * xnum + j, Double.NaN);
                    } else {
                        r.setDouble(i * xnum + j, a - b);
                    }
                }
            }
        }

        return r;
    }

    /**
     * Calculates the vertical component of the curl (ie, vorticity)
     *
     * @param uData U component
     * @param vData V component
     * @param xx X dimension value
     * @param yy Y dimension value
     * @return Curl
     */
    public static Array hcurl(Array uData, Array vData, List<Number> xx, List<Number> yy) {
        Array lonData = Array.factory(DataType.DOUBLE, uData.getShape());
        Array latData = Array.factory(DataType.DOUBLE, vData.getShape());
        int[] shape = uData.getShape();
        int ynum = shape[0];
        int xnum = shape[1];
        int i, j;
        for (i = 0; i < ynum; i++) {
            for (j = 0; j < xnum; j++) {
                lonData.setDouble(i * xnum + j, xx.get(j).doubleValue());
                latData.setDouble(i * xnum + j, yy.get(i).doubleValue());
            }
        }
        Array dv = cdiff(vData, true);
        Array dx = mul(cdiff(lonData, true), Math.PI / 180);
        Array du = cdiff(mul(uData, cos(mul(latData, Math.PI / 180))), false);
        Array dy = mul(cdiff(latData, false), Math.PI / 180);
        Array gData = div(sub(div(dv, dx), div(du, dy)), mul(cos(mul(latData, Math.PI / 180)), 6.37e6));

        return gData;
    }

    /**
     * Calculates the horizontal divergence using finite differencing
     *
     * @param uData U component
     * @param vData V component
     * @param xx X dimension value
     * @param yy Y dimension value
     * @return Divergence
     */
    public static Array hdivg(Array uData, Array vData, List<Number> xx, List<Number> yy) {
        Array lonData = Array.factory(DataType.DOUBLE, uData.getShape());
        Array latData = Array.factory(DataType.DOUBLE, vData.getShape());
        int[] shape = uData.getShape();
        int ynum = shape[0];
        int xnum = shape[1];
        int i, j;
        for (i = 0; i < ynum; i++) {
            for (j = 0; j < xnum; j++) {
                lonData.setDouble(i * xnum + j, xx.get(j).doubleValue());
                latData.setDouble(i * xnum + j, yy.get(i).doubleValue());
            }
        }
        Array du = cdiff(uData, true);
        Array dx = mul(cdiff(lonData, true), Math.PI / 180);
        Array dv = cdiff(mul(vData, cos(mul(latData, Math.PI / 180))), false);
        Array dy = mul(cdiff(latData, false), Math.PI / 180);
        Array gData = div(add(div(du, dx), div(dv, dy)), mul(cos(mul(latData, Math.PI / 180)), 6.37e6));

        return gData;
    }

    /**
     * Take magnitude value from U/V grid data
     *
     * @param uData U grid data
     * @param vData V grid data
     * @return Magnitude grid data
     */
    public static Array magnitude(Array uData, Array vData) {
        int[] shape = uData.getShape();
        int xNum = shape[1];
        int yNum = shape[0];
        int idx;

        Array r = Array.factory(DataType.DOUBLE, shape);
        for (int i = 0; i < yNum; i++) {
            for (int j = 0; j < xNum; j++) {
                idx = i * xNum + j;
                if (Double.isNaN(uData.getDouble(idx)) || Double.isNaN(vData.getDouble(idx))) {
                    r.setDouble(idx, Double.NaN);
                } else {
                    r.setDouble(idx, Math.sqrt(Math.pow(uData.getDouble(idx), 2) + Math.pow(vData.getDouble(idx), 2)));
                }
            }
        }

        return r;
    }
    // </editor-fold>
}
