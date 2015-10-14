/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.meteoinfo.data.mapdata.Field;
import org.meteoinfo.geoprocess.GeoComputation;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import org.meteoinfo.global.util.BigDecimalUtil;
import org.meteoinfo.global.util.GlobalUtil;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.legend.LegendScheme;
import org.meteoinfo.projection.KnownCoordinateSystems;
import org.meteoinfo.projection.ProjectionInfo;
import org.meteoinfo.projection.Reproject;
import org.meteoinfo.shape.PolygonShape;
import org.meteoinfo.shape.ShapeTypes;
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
            if (dataType.contains("%")) {
                dataType = dataType.split("%")[1];
            }
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

    /**
     * Get row number of a ASCII file
     *
     * @param fileName File name
     * @return Row number
     * @throws FileNotFoundException
     */
    public static int numASCIIRow(String fileName) throws FileNotFoundException {
        File f = new File(fileName);
        int lineNumber;
        try (Scanner fileScanner = new Scanner(f)) {
            lineNumber = 0;
            while (fileScanner.hasNextLine()) {
                fileScanner.nextLine();
                lineNumber++;
            }
        }

        return lineNumber;
    }

    /**
     * Get row number of a ASCII file
     *
     * @param fileName File name
     * @param delimiter
     * @param headerLines
     * @return Row number
     * @throws FileNotFoundException
     */
    public static int numASCIICol(String fileName, String delimiter, int headerLines) throws FileNotFoundException, IOException {
        String[] dataArray;
        try (BufferedReader sr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)))) {
            if (headerLines > 0) {
                for (int i = 0; i < headerLines; i++) {
                    sr.readLine();
                }
            }
            String line = sr.readLine();
            dataArray = GlobalUtil.split(line, delimiter);
        }

        return dataArray.length;
    }

    // </editor-fold>
    // <editor-fold desc="Create">
    /**
     * Create an array
     *
     * @param data Object
     * @return Array
     */
    public static Array array(Object data) { 
        if (data instanceof Number) {
            DataType dt = ArrayMath.getDataType(data);
            Array a = Array.factory(dt, new int[]{1});
            a.setObject(0, data);
            return a;
        } else {
            return null;
        }
    }
    
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
        if (dataType == DataType.FLOAT || dataType == DataType.DOUBLE) {
            for (int i = 0; i < length; i++) {
                a.setObject(i, BigDecimalUtil.add(BigDecimalUtil.mul(i, stepv), startv));
            }
        } else {
            for (int i = 0; i < length; i++) {
                a.setObject(i, i * stepv + startv);
            }
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
    public static Array arrayRange1(Number start, final int length, final Number step) {
        DataType dataType = ArrayUtil.objectsToType(new Object[]{
            start,
            step});
        double startv = start.doubleValue();
        double stepv = step.doubleValue();
        Array a = Array.factory(dataType, new int[]{length});
        if (dataType == DataType.FLOAT || dataType == DataType.DOUBLE) {
            for (int i = 0; i < length; i++) {
                a.setObject(i, BigDecimalUtil.add(BigDecimalUtil.mul(i, stepv), startv));
            }
        } else {
            for (int i = 0; i < length; i++) {
                a.setObject(i, i * stepv + startv);
            }
        }
        return a;
    }

    /**
     * Array line space
     *
     * @param start Start value
     * @param stop Stop value
     * @param n Number value
     * @param endpoint If stop is included
     * @return Array
     */
    public static Array lineSpace(Number start, Number stop, final int n, boolean endpoint) {
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
        double endv = n * stepv + startv;
        int nn = n;
        if (endpoint) {
            if (endv < stopv) {
                nn += 1;
            }
        } else {
            if (endv >= stopv) {
                nn -= 1;
            }
        }
        Array a = Array.factory(DataType.FLOAT, new int[]{n});
        for (int i = 0; i < n; i++) {
            a.setObject(i, BigDecimalUtil.add(BigDecimalUtil.mul(i, stepv), startv));
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
     * Get zero array
     *
     * @param shape Shape
     * @param dtype Data type
     * @return Array Result array
     */
    public static Array zeros(List<Integer> shape, String dtype) {
        DataType dt = toDataType(dtype);
        int[] ashape = new int[shape.size()];
        for (int i = 0; i < shape.size(); i++) {
            ashape[i] = shape.get(i);
        }
        Array a = Array.factory(dt, ashape);
        for (int i = 0; i < a.getSize(); i++) {
            a.setObject(i, 0);
        }

        return a;
    }

    /**
     * Get ones array
     *
     * @param n Number
     * @return Array Result array
     */
    public static Array ones(int n) {
        Array a = Array.factory(DataType.FLOAT, new int[]{n});
        for (int i = 0; i < n; i++) {
            a.setFloat(i, 1);
        }

        return a;
    }

    /**
     * Get ones array
     *
     * @param shape Shape
     * @param dtype Data type
     * @return Array Result array
     */
    public static Array ones(List<Integer> shape, String dtype) {
        DataType dt = toDataType(dtype);
        int[] ashape = new int[shape.size()];
        for (int i = 0; i < shape.size(); i++) {
            ashape[i] = shape.get(i);
        }
        Array a = Array.factory(dt, ashape);
        for (int i = 0; i < a.getSize(); i++) {
            a.setObject(i, 1);
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
    public static String convertToString(Array a) {
        StringBuilder sbuff = new StringBuilder();
        sbuff.append("array(");
        int ndim = a.getRank();
        if (ndim > 1) {
            sbuff.append("[");
        }
        int i = 0, n = 0;
        IndexIterator ii = a.getIndexIterator();
        int shapeIdx = ndim - 1;
        if (shapeIdx < 0) {
            sbuff.append("[");
            sbuff.append(ii.getObjectNext());
            sbuff.append("])");
            return sbuff.toString();
        }

        int len = a.getShape()[shapeIdx];
        while (ii.hasNext()) {
            if (i == 0) {
                if (n > 0) {
                    sbuff.append("\n      ");
                }
                sbuff.append("[");
            }
            Object data = ii.getObjectNext();
            sbuff.append(data);
            i += 1;
            if (i == len) {
                sbuff.append("]");
                len = a.getShape()[shapeIdx];
                i = 0;
            } else {
                sbuff.append(", ");
            }
            n += 1;
            if (n > 200) {
                sbuff.append("...]");
                break;
            }
        }
        if (ndim > 1) {
            sbuff.append("]");
        }
        sbuff.append(")");
        return sbuff.toString();
    }

    /**
     * Array to string
     *
     * @param a Array a
     * @return String
     */
    public static String toString_old(Array a) {
        StringBuilder sbuff = new StringBuilder();
        sbuff.append("array(");
        int ndim = a.getRank();
        if (ndim > 1) {
            sbuff.append("[");
        }
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
            if (i == len) {
                sbuff.append("]");
                len = a.getShape()[shapeIdx];
                i = 0;
            } else {
                sbuff.append(", ");
            }
        }
        if (ndim > 1) {
            sbuff.append("]");
        }
        return sbuff.toString();
    }

    // </editor-fold>
    // <editor-fold desc="Convert">
    /**
     * To data type - ucar.ma2
     *
     * @param dt Data type string
     * @return Data type
     */
    public static DataType toDataType(String dt) {
        if (dt.contains("%")) {
            dt = dt.split("%")[1];
        }
        switch (dt.toLowerCase()) {
            case "c":
            case "s":
            case "string":
                return DataType.STRING;
            case "i":
            case "int":
                return DataType.INT;
            case "f":
            case "float":
                return DataType.FLOAT;
            case "d":
            case "double":
                return DataType.DOUBLE;
            default:
                return DataType.OBJECT;
        }
    }

    // </editor-fold>
    // <editor-fold desc="Resample/Interpolate">
    /**
     * Mesh grid
     *
     * @param x X array - vector
     * @param y Y array - vector
     * @return Result arrays - matrix
     */
    public static Array[] meshgrid(Array x, Array y) {
        int xn = (int) x.getSize();
        int yn = (int) y.getSize();
        int[] shape = new int[]{yn, xn};
        Array rx = Array.factory(x.getDataType(), shape);
        Array ry = Array.factory(y.getDataType(), shape);
        for (int i = 0; i < yn; i++) {
            for (int j = 0; j < xn; j++) {
                rx.setObject(i * xn + j, x.getObject(j));
                ry.setObject(i * xn + j, y.getObject(i));
            }
        }

        return new Array[]{rx, ry};
    }

    /**
     * Create mesh polygon layer
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param ls Legend scheme
     * @param lonlim Longiutde limitation - to avoid the polygon cross -180/180
     * @return Mesh polygon layer
     */
    public static VectorLayer meshLayer(Array x_s, Array y_s, Array a, LegendScheme ls, double lonlim) {
        VectorLayer layer = new VectorLayer(ShapeTypes.Polygon);
        String fieldName = "Data";
        Field aDC = new Field(fieldName, DataTypes.Double);
        layer.editAddField(aDC);

        int[] shape = x_s.getShape();
        int colNum = shape[1];
        int rowNum = shape[0];
        double x1, x2, x3, x4;
        for (int i = 0; i < rowNum - 1; i++) {
            for (int j = 0; j < colNum - 1; j++) {
                x1 = x_s.getDouble(i * colNum + j);
                x2 = x_s.getDouble(i * colNum + j + 1);
                x3 = x_s.getDouble((i + 1) * colNum + j);
                x4 = x_s.getDouble((i + 1) * colNum + j + 1);
                if (lonlim > 0) {
                    if (Math.abs(x2 - x4) > lonlim || Math.abs(x1 - x4) > lonlim
                            || Math.abs(x3 - x4) > lonlim || Math.abs(x1 - x2) > lonlim
                            || Math.abs(x2 - x3) > lonlim) {
                        continue;
                    }
                }

                PolygonShape ps = new PolygonShape();
                List<PointD> points = new ArrayList<>();
                points.add(new PointD(x1, y_s.getDouble(i * colNum + j)));
                points.add(new PointD(x3, y_s.getDouble((i + 1) * colNum + j)));
                points.add(new PointD(x4, y_s.getDouble((i + 1) * colNum + j + 1)));
                points.add(new PointD(x2, y_s.getDouble(i * colNum + j + 1)));
                points.add((PointD) points.get(0).clone());
                ps.setPoints(points);
                ps.lowValue = a.getDouble(i * colNum + j);
                ps.highValue = ps.lowValue;
                int shapeNum = layer.getShapeNum();
                try {
                    if (layer.editInsertShape(ps, shapeNum)) {
                        layer.editCellValue(fieldName, shapeNum, ps.lowValue);
                    }
                } catch (Exception ex) {

                }
            }
        }
        layer.setLayerName("Mesh_Layer");
        ls.setFieldName(fieldName);
        layer.setLegendScheme(ls.convertTo(ShapeTypes.Polygon));

        return layer;
    }

    /**
     * Smooth with 5 points
     *
     * @param a Array
     * @param rowNum Row number
     * @param colNum Column number
     * @param unDefData Missing value
     * @return Result array
     */
    public static Array smooth5(Array a, int rowNum, int colNum, double unDefData) {
        Array r = Array.factory(a.getDataType(), a.getShape());
        double s = 0.5;
        for (int i = 1; i < rowNum - 1; i++) {
            for (int j = 1; j < colNum - 2; j++) {
                if (r.getDouble(i * colNum + j) == unDefData || r.getDouble((i + 1) * colNum + j) == unDefData || r.getDouble((i - 1) * colNum + j)
                        == unDefData || r.getDouble(i * colNum + j + 1) == unDefData || r.getDouble(i * colNum + j - 1) == unDefData) {
                    continue;
                }
                r.setDouble(i * colNum + j, r.getDouble(i * colNum + j) + s / 4 * (r.getDouble((i + 1) * colNum + j) + r.getDouble((i - 1) * colNum + j) + r.getDouble(i * colNum + j + 1)
                        + r.getDouble(i * colNum + j - 1) - 4 * r.getDouble(i * colNum + j)));
            }
        }

        return r;
    }

    /**
     * Interpolation with IDW radius method
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X grid X array
     * @param Y grid Y array
     * @param NeededPointNum needed at least point number
     * @param radius search radius
     * @param unDefData undefine data
     * @return interpolated grid data
     */
    public static Array interpolation_IDW_Radius(List<Number> x_s, List<Number> y_s, Array a,
            List<Number> X, List<Number> Y, int NeededPointNum, double radius, double unDefData) {
        int rowNum, colNum, pNum;
        colNum = X.size();
        rowNum = Y.size();
        pNum = x_s.size();
        //double[][] GCoords = new double[rowNum][colNum];
        Array r = Array.factory(DataType.DOUBLE, new int[]{rowNum, colNum});
        int i, j, p, vNum;
        double w, SV, SW;
        boolean ifPointGrid;
        double x, y, v;

        //---- Do interpolation
        for (i = 0; i < rowNum; i++) {
            for (j = 0; j < colNum; j++) {
                r.setDouble(i * colNum + j, unDefData);
                ifPointGrid = false;
                SV = 0;
                SW = 0;
                vNum = 0;
                for (p = 0; p < pNum; p++) {
                    v = a.getDouble(p);
                    if (MIMath.doubleEquals(v, unDefData)) {
                        continue;
                    }
                    x = x_s.get(p).doubleValue();
                    y = y_s.get(p).doubleValue();
                    if (x < X.get(j).doubleValue() - radius || x > X.get(j).doubleValue() + radius || y < Y.get(i).doubleValue() - radius
                            || y > Y.get(i).doubleValue() + radius) {
                        continue;
                    }

                    if (Math.pow(X.get(j).doubleValue() - x, 2) + Math.pow(Y.get(i).doubleValue() - y, 2) == 0) {
                        r.setDouble(i * colNum + j, v);
                        ifPointGrid = true;
                        break;
                    } else if (Math.sqrt(Math.pow(X.get(j).doubleValue() - x, 2)
                            + Math.pow(Y.get(i).doubleValue() - y, 2)) <= radius) {
                        w = 1 / (Math.pow(X.get(j).doubleValue() - x, 2) + Math.pow(Y.get(i).doubleValue() - y, 2));
                        SW = SW + w;
                        SV = SV + v * w;
                        vNum += 1;
                    }
                }

                if (!ifPointGrid) {
                    if (vNum >= NeededPointNum) {
                        r.setDouble(i * colNum + j, SV / SW);
                    }
                }
            }
        }

        //---- Smooth with 5 points
        r = smooth5(r, rowNum, colNum, unDefData);

        return r;
    }

    /**
     * Interpolation with IDW neighbor method
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X grid X array
     * @param Y grid Y array
     * @param NumberOfNearestNeighbors
     * @param unDefData undefine data
     * @return interpolated grid data
     */
    public static Array interpolation_IDW_Neighbor(List<Number> x_s, List<Number> y_s, Array a,
            List<Number> X, List<Number> Y, int NumberOfNearestNeighbors, double unDefData) {
        int rowNum, colNum, pNum;
        colNum = X.size();
        rowNum = Y.size();
        pNum = x_s.size();
        Array r = Array.factory(DataType.DOUBLE, new int[]{rowNum, colNum});
        int i, j, p, l, aP;
        double w, SV, SW, aMin;
        int points;
        points = NumberOfNearestNeighbors;
        double[] AllWeights = new double[pNum];
        double[][] NW = new double[2][points];
        int NWIdx;
        double x, y, v;

        //---- Do interpolation with IDW method 
        for (i = 0; i < rowNum; i++) {
            for (j = 0; j < colNum; j++) {
                r.setDouble(i * colNum + j, unDefData);
                SV = 0;
                SW = 0;
                NWIdx = 0;
                for (p = 0; p < pNum; p++) {
                    v = a.getDouble(p);
                    if (v == unDefData) {
                        AllWeights[p] = -1;
                        continue;
                    }
                    x = x_s.get(p).doubleValue();
                    y = y_s.get(p).doubleValue();
                    if (Math.pow(X.get(j).doubleValue() - x, 2) + Math.pow(Y.get(i).doubleValue() - y, 2) == 0) {
                        r.setDouble(i * colNum + j, v);
                        break;
                    } else {
                        w = 1 / (Math.pow(X.get(j).doubleValue() - x, 2) + Math.pow(Y.get(i).doubleValue() - y, 2));
                        AllWeights[p] = w;
                        if (NWIdx < points) {
                            NW[0][NWIdx] = w;
                            NW[1][NWIdx] = p;
                        }
                        NWIdx += 1;
                    }
                }

                if (r.getDouble(i * colNum + j) == unDefData) {
                    for (p = 0; p < pNum; p++) {
                        w = AllWeights[p];
                        if (w == -1) {
                            continue;
                        }

                        aMin = NW[0][0];
                        aP = 0;
                        for (l = 1; l < points; l++) {
                            if ((double) NW[0][l] < aMin) {
                                aMin = (double) NW[0][l];
                                aP = l;
                            }
                        }
                        if (w > aMin) {
                            NW[0][aP] = w;
                            NW[1][aP] = p;
                        }
                    }
                    for (p = 0; p < points; p++) {
                        SV += (double) NW[0][p] * a.getDouble((int) NW[1][p]);
                        SW += (double) NW[0][p];
                    }
                    r.setDouble(i * colNum + j, SV / SW);
                }
            }
        }

        //---- Smooth with 5 points
        r = smooth5(r, rowNum, colNum, unDefData);

        return r;
    }

    /**
     * Interpolate with nearest method
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X x coordinate
     * @param Y y coordinate
     * @param radius Radius
     * @param fill_value undefine value
     * @return grid data
     */
    public static Array interpolation_Nearest_1(List<Number> x_s, List<Number> y_s, Array a, List<Number> X, List<Number> Y,
            double radius, double fill_value) {
        int rowNum, colNum, pNum;
        colNum = X.size();
        rowNum = Y.size();
        pNum = x_s.size();
        Array rdata = Array.factory(DataType.DOUBLE, new int[]{rowNum, colNum});
        double gx, gy;
        double x, y, r, v, minr;
        int rr = (int) Math.ceil(radius);

        List<int[]> pIJ = getPointsIJ(x_s, y_s, X, Y);

        for (int i = 0; i < rowNum; i++) {
            gy = Y.get(i).doubleValue();
            for (int j = 0; j < colNum; j++) {
                rdata.setDouble(i * colNum + j, fill_value);
                gx = X.get(j).doubleValue();
                minr = Double.MAX_VALUE;
                List<Integer> pIdx = getPointsIdx(pIJ, i, j, rr);
                for (int p : pIdx) {
                    v = a.getDouble(p);
                    if (MIMath.doubleEquals(v, fill_value)) {
                        continue;
                    }

                    x = x_s.get(p).doubleValue();
                    y = y_s.get(p).doubleValue();
                    r = Math.sqrt((gx - x) * (gx - x) + (gy - y) * (gy - y));
                    if (r < minr) {
                        rdata.setDouble(i * colNum + j, v);
                        minr = r;
                    }
                }
            }
        }

        return rdata;
    }

    /**
     * Interpolate with nearest method
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X x coordinate
     * @param Y y coordinate
     * @param radius Radius
     * @param fill_value undefine value
     * @return grid data
     */
    public static Array interpolation_Nearest(List<Number> x_s, List<Number> y_s, Array a, List<Number> X, List<Number> Y,
            double radius, double fill_value) {
        int rowNum, colNum, pNum;
        colNum = X.size();
        rowNum = Y.size();
        pNum = x_s.size();
        Array rdata = Array.factory(DataType.DOUBLE, new int[]{rowNum, colNum});
        double gx, gy;
        double x, y, r, v, minr;

        for (int i = 0; i < rowNum; i++) {
            gy = Y.get(i).doubleValue();
            for (int j = 0; j < colNum; j++) {
                rdata.setDouble(i * colNum + j, fill_value);
                gx = X.get(j).doubleValue();
                minr = Double.MAX_VALUE;
                for (int p = 0; p < pNum; p++) {
                    v = a.getDouble(p);
                    if (MIMath.doubleEquals(v, fill_value)) {
                        continue;
                    }

                    x = x_s.get(p).doubleValue();
                    y = y_s.get(p).doubleValue();
                    if (Math.abs(gx - x) > radius || Math.abs(gy - y) > radius) {
                        continue;
                    }

                    r = Math.sqrt((gx - x) * (gx - x) + (gy - y) * (gy - y));
                    if (r < radius) {
                        if (r < minr) {
                            rdata.setDouble(i * colNum + j, v);
                            minr = r;
                        }
                    }
                }
            }
        }

        return rdata;
    }

    /**
     * Interpolate with inside method - The grid cell value is the average value
     * of the inside points or fill value if no inside point.
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X x coordinate
     * @param Y y coordinate
     * @param fill_value Fill value
     * @return grid data
     */
    public static Array interpolation_Inside(List<Number> x_s, List<Number> y_s, Array a, List<Number> X, List<Number> Y,
            double fill_value) {
        int rowNum, colNum, pNum;
        colNum = X.size();
        rowNum = Y.size();
        pNum = x_s.size();
        Array r = Array.factory(DataType.DOUBLE, new int[]{rowNum, colNum});
        double dX = X.get(1).doubleValue() - X.get(0).doubleValue();
        double dY = Y.get(1).doubleValue() - Y.get(0).doubleValue();
        int[][] pNums = new int[rowNum][colNum];
        double x, y, v;

        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                pNums[i][j] = 0;
                r.setDouble(i * colNum + j, 0.0);
            }
        }

        for (int p = 0; p < pNum; p++) {
            v = a.getDouble(p);
            if (MIMath.doubleEquals(v, fill_value)) {
                continue;
            }

            x = x_s.get(p).doubleValue();
            y = y_s.get(p).doubleValue();
            if (x < X.get(0).doubleValue() || x > X.get(colNum - 1).doubleValue()) {
                continue;
            }
            if (y < Y.get(0).doubleValue() || y > Y.get(rowNum - 1).doubleValue()) {
                continue;
            }

            int j = (int) ((x - X.get(0).doubleValue()) / dX);
            int i = (int) ((y - Y.get(0).doubleValue()) / dY);
            pNums[i][j] += 1;
            r.setDouble(i * colNum + j, r.getDouble(i * colNum + j) + v);
        }

        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                if (pNums[i][j] == 0) {
                    r.setDouble(i * colNum + j, fill_value);
                } else {
                    r.setDouble(i * colNum + j, r.getDouble(i * colNum + j) / pNums[i][j]);
                }
            }
        }

        return r;
    }

    private static List<int[]> getPointsIJ(List<Number> x_s, List<Number> y_s, List<Number> X, List<Number> Y) {
        int rowNum, colNum, pNum;
        colNum = X.size();
        rowNum = Y.size();
        pNum = x_s.size();
        double dX = X.get(1).doubleValue() - X.get(0).doubleValue();
        double dY = Y.get(1).doubleValue() - Y.get(0).doubleValue();
        List<int[]> pIndices = new ArrayList<>();
        double x, y;
        int i, j;
        for (int p = 0; p < pNum; p++) {
            x = x_s.get(p).doubleValue();
            y = y_s.get(p).doubleValue();
            if (x < X.get(0).doubleValue() || x > X.get(colNum - 1).doubleValue()) {
                continue;
            }
            if (y < Y.get(0).doubleValue() || y > Y.get(rowNum - 1).doubleValue()) {
                continue;
            }

            j = (int) ((x - X.get(0).doubleValue()) / dX);
            i = (int) ((y - Y.get(0).doubleValue()) / dY);
            pIndices.add(new int[]{i, j});
        }

        return pIndices;
    }

    private static List<Integer> getPointsIdx(List<int[]> pIJ, int ii, int jj, int radius) {
        List<Integer> pIdx = new ArrayList<>();
        int[] ij;
        int i, j;
        for (int p = 0; p < pIJ.size(); p++) {
            ij = pIJ.get(p);
            i = ij[0];
            j = ij[1];
            if (Math.abs(i - ii) <= radius && Math.abs(j - jj) <= radius) {
                pIdx.add(p);
            }
        }

        return pIdx;
    }

    /**
     * Interpolate with surface method
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X x coordinate
     * @param Y y coordinate
     * @param unDefData undefine value
     * @return grid data
     */
    public static Array interpolation_Surface_1(Array x_s, Array y_s, Array a, Array X, Array Y,
            double unDefData) {
        int rowNum, colNum, xn, yn;
        int[] shape = x_s.getShape();
        colNum = shape[1];
        rowNum = shape[0];
        xn = (int) X.getSize();
        yn = (int) Y.getSize();
        Array r = Array.factory(DataType.DOUBLE, new int[]{yn, xn});
        double x, y;

        PolygonShape[][] polygons = new PolygonShape[rowNum - 1][colNum - 1];
        PolygonShape ps;
        for (int i = 0; i < rowNum - 1; i++) {
            for (int j = 0; j < colNum - 1; j++) {
                ps = new PolygonShape();
                List<PointD> points = new ArrayList<>();
                points.add(new PointD(x_s.getDouble(i * colNum + j), y_s.getDouble(i * colNum + j)));
                points.add(new PointD(x_s.getDouble((i + 1) * colNum + j), y_s.getDouble((i + 1) * colNum + j)));
                points.add(new PointD(x_s.getDouble((i + 1) * colNum + j + 1), y_s.getDouble((i + 1) * colNum + j + 1)));
                points.add(new PointD(x_s.getDouble(i * colNum + j + 1), y_s.getDouble(i * colNum + j + 1)));
                points.add((PointD) points.get(0).clone());
                ps.setPoints(points);
                polygons[i][j] = ps;
            }
        }

        for (int i = 0; i < yn; i++) {
            for (int j = 0; j < xn; j++) {
                r.setDouble(i * xn + j, unDefData);
            }
        }

        double v;
        for (int i = 0; i < rowNum - 1; i++) {
            for (int j = 0; j < colNum - 1; j++) {
                ps = polygons[i][j];
                v = a.getDouble(i * colNum + j);
                for (int ii = 0; ii < yn; ii++) {
                    y = Y.getDouble(ii);
                    for (int jj = 0; jj < xn; jj++) {
                        x = X.getDouble(jj);
                        if (Double.isNaN(r.getDouble(ii * xn + jj)) || r.getDouble(ii * xn + jj) == unDefData) {
                            if (GeoComputation.pointInPolygon(ps, x, y)) {
                                r.setDouble(ii * xn + jj, v);
                            }
                        }
                    }
                }
            }
        }

        return r;
    }

    /**
     * Interpolate with surface method
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X x coordinate
     * @param Y y coordinate
     * @param unDefData undefine value
     * @return grid data
     */
    public static Array interpolation_Surface(Array x_s, Array y_s, Array a, Array X, Array Y,
            double unDefData) {
        int rowNum, colNum, xn, yn;
        int[] shape = x_s.getShape();
        colNum = shape[1];
        rowNum = shape[0];
        xn = (int) X.getSize();
        yn = (int) Y.getSize();
        Array r = Array.factory(DataType.DOUBLE, new int[]{yn, xn});
        double x, y;
        boolean isIn;

        PolygonShape[][] polygons = new PolygonShape[rowNum - 1][colNum - 1];
        for (int i = 0; i < rowNum - 1; i++) {
            for (int j = 0; j < colNum - 1; j++) {
                PolygonShape ps = new PolygonShape();
                List<PointD> points = new ArrayList<>();
                points.add(new PointD(x_s.getDouble(i * colNum + j), y_s.getDouble(i * colNum + j)));
                points.add(new PointD(x_s.getDouble((i + 1) * colNum + j), y_s.getDouble((i + 1) * colNum + j)));
                points.add(new PointD(x_s.getDouble((i + 1) * colNum + j + 1), y_s.getDouble((i + 1) * colNum + j + 1)));
                points.add(new PointD(x_s.getDouble(i * colNum + j + 1), y_s.getDouble(i * colNum + j + 1)));
                points.add((PointD) points.get(0).clone());
                ps.setPoints(points);
                polygons[i][j] = ps;
            }
        }

        for (int i = 0; i < yn; i++) {
            y = Y.getDouble(i);
            for (int j = 0; j < xn; j++) {
                x = X.getDouble(j);
                isIn = false;
                for (int ii = 0; ii < rowNum - 1; ii++) {
                    for (int jj = 0; jj < colNum - 1; jj++) {
                        if (GeoComputation.pointInPolygon(polygons[ii][jj], x, y)) {
                            r.setDouble(i * xn + j, a.getDouble(ii * colNum + jj));
                            isIn = true;
                            break;
                        }
                    }
                    if (isIn) {
                        break;
                    }
                }
                if (!isIn) {
                    r.setDouble(i * xn + j, unDefData);
                }
            }
        }

        return r;
    }

    /**
     * Cressman analysis
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param v_s scatter value array
     * @param X x array
     * @param Y y array
     * @param unDefData undefine data
     * @param radList radii list
     * @return result grid data
     */
    public static Array cressman(List<Number> x_s, List<Number> y_s, Array v_s, List<Number> X, List<Number> Y,
            double unDefData, List<Number> radList) {
        int xNum = X.size();
        int yNum = Y.size();
        int pNum = x_s.size();
        //double[][] gridData = new double[yNum][xNum];
        Array r = Array.factory(DataType.DOUBLE, new int[]{yNum, xNum});
        int irad = radList.size();
        int i, j;

        //Loop through each stn report and convert stn lat/lon to grid coordinates
        double xMin = X.get(0).doubleValue();
        double xMax;
        double yMin = Y.get(0).doubleValue();
        double yMax;
        double xDelt = X.get(1).doubleValue() - X.get(0).doubleValue();
        double yDelt = Y.get(1).doubleValue() - Y.get(0).doubleValue();
        double x, y;
        double sum;
        int stNum = 0;
        double[][] stationData = new double[pNum][3];
        for (i = 0; i < pNum; i++) {
            x = x_s.get(i).doubleValue();
            y = y_s.get(i).doubleValue();
            stationData[i][0] = (x - xMin) / xDelt;
            stationData[i][1] = (y - yMin) / yDelt;
            stationData[i][2] = v_s.getDouble(i);
            if (stationData[i][2] != unDefData) {
                //total += stationData[i][2];
                stNum += 1;
            }
        }
        //total = total / stNum;

        double HITOP = -999900000000000000000.0;
        double HIBOT = 999900000000000000000.0;
        double[][] TOP = new double[yNum][xNum];
        double[][] BOT = new double[yNum][xNum];
        for (i = 0; i < yNum; i++) {
            for (j = 0; j < xNum; j++) {
                TOP[i][j] = HITOP;
                BOT[i][j] = HIBOT;
            }
        }

        //Initial grid values are average of station reports within the first radius
        double rad;
        if (radList.size() > 0) {
            rad = radList.get(0).doubleValue();
        } else {
            rad = 4;
        }
        for (i = 0; i < yNum; i++) {
            y = (double) i;
            yMin = y - rad;
            yMax = y + rad;
            for (j = 0; j < xNum; j++) {
                x = (double) j;
                xMin = x - rad;
                xMax = x + rad;
                stNum = 0;
                sum = 0;
                for (int s = 0; s < pNum; s++) {
                    double val = stationData[s][2];
                    double sx = stationData[s][0];
                    double sy = stationData[s][1];
                    if (sx < 0 || sx >= xNum - 1 || sy < 0 || sy >= yNum - 1) {
                        continue;
                    }

                    if (val == unDefData || sx < xMin || sx > xMax || sy < yMin || sy > yMax) {
                        continue;
                    }

                    double dis = Math.sqrt(Math.pow(sx - x, 2) + Math.pow(sy - y, 2));
                    if (dis > rad) {
                        continue;
                    }

                    sum += val;
                    stNum += 1;
                    if (TOP[i][j] < val) {
                        TOP[i][j] = val;
                    }
                    if (BOT[i][j] > val) {
                        BOT[i][j] = val;
                    }
                }
                if (stNum == 0) {
                    r.setDouble(i * xNum + j, unDefData);
                } else {
                    r.setDouble(i * xNum + j, sum / stNum);
                }
            }
        }

        //Perform the objective analysis
        for (int p = 0; p < irad; p++) {
            rad = radList.get(p).doubleValue();
            for (i = 0; i < yNum; i++) {
                y = (double) i;
                yMin = y - rad;
                yMax = y + rad;
                for (j = 0; j < xNum; j++) {
                    if (r.getDouble(i * xNum + j) == unDefData) {
                        continue;
                    }

                    x = (double) j;
                    xMin = x - rad;
                    xMax = x + rad;
                    sum = 0;
                    double wSum = 0;
                    for (int s = 0; s < pNum; s++) {
                        double val = stationData[s][2];
                        double sx = stationData[s][0];
                        double sy = stationData[s][1];
                        if (sx < 0 || sx >= xNum - 1 || sy < 0 || sy >= yNum - 1) {
                            continue;
                        }

                        if (val == unDefData || sx < xMin || sx > xMax || sy < yMin || sy > yMax) {
                            continue;
                        }

                        double dis = Math.sqrt(Math.pow(sx - x, 2) + Math.pow(sy - y, 2));
                        if (dis > rad) {
                            continue;
                        }

                        int i1 = (int) sy;
                        int j1 = (int) sx;
                        int i2 = i1 + 1;
                        int j2 = j1 + 1;
                        double a = r.getDouble(i1 * xNum + j1);
                        double b = r.getDouble(i1 * xNum + j2);
                        double c = r.getDouble(i2 * xNum + j1);
                        double d = r.getDouble(i2 * xNum + j2);
                        List<Double> dList = new ArrayList<>();
                        if (a != unDefData) {
                            dList.add(a);
                        }
                        if (b != unDefData) {
                            dList.add(b);
                        }
                        if (c != unDefData) {
                            dList.add(c);
                        }
                        if (d != unDefData) {
                            dList.add(d);
                        }

                        double calVal;
                        if (dList.isEmpty()) {
                            continue;
                        } else if (dList.size() == 1) {
                            calVal = dList.get(0);
                        } else if (dList.size() <= 3) {
                            double aSum = 0;
                            for (double dd : dList) {
                                aSum += dd;
                            }
                            calVal = aSum / dList.size();
                        } else {
                            double x1val = a + (c - a) * (sy - i1);
                            double x2val = b + (d - b) * (sy - i1);
                            calVal = x1val + (x2val - x1val) * (sx - j1);
                        }
                        double eVal = val - calVal;
                        double w = (rad * rad - dis * dis) / (rad * rad + dis * dis);
                        sum += eVal * w;
                        wSum += w;
                    }
                    if (wSum < 0.000001) {
                        r.setDouble(i * xNum + j, unDefData);
                    } else {
                        double aData = r.getDouble(i * xNum + j) + sum / wSum;
                        r.setDouble(i * xNum + j, Math.max(BOT[i][j], Math.min(TOP[i][j], aData)));
                    }
                }
            }
        }

        //Return
        return r;
    }

    private static Array resample_Bilinear(Array a, List<Number> X, List<Number> Y, List<Number> newX, List<Number> newY) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        int i, j;
        int xn = newX.size();
        int yn = newY.size();
        double x, y;

        for (i = 0; i < yn; i++) {
            y = newY.get(i).doubleValue();
            for (j = 0; j < xn; j++) {
                x = newX.get(j).doubleValue();
                if (x < X.get(0).doubleValue() || x > X.get(X.size() - 1).doubleValue()) {
                    r.setDouble(i * xn + j, Double.NaN);
                } else if (y < Y.get(0).doubleValue() || y > Y.get(Y.size() - 1).doubleValue()) {
                    r.setDouble(i * xn + j, Double.NaN);
                } else {
                    r.setDouble(i * xn + j, 9999.0);
                    //newdata[i][j] = this.toStation(x, y);
                }
            }
        }

        return r;
    }

    /**
     * Interpolate array data
     *
     * @param a Array
     * @param X X coordinates
     * @param Y Y coordinates
     * @return Result array data
     */
    public Array interpolate(Array a, List<Number> X, List<Number> Y) {
        int nxNum = X.size() * 2 - 1;
        int nyNum = Y.size() * 2 - 1;
        List<Number> newX = new ArrayList<>();
        List<Number> newY = new ArrayList<>();
        int i;

        for (i = 0; i < nxNum; i++) {
            if (i % 2 == 0) {
                newX.add(X.get(i / 2).doubleValue());
            } else {
                newX.add((X.get((i - 1) / 2).doubleValue() + X.get((i - 1) / 2 + 1).doubleValue()) / 2);
            }
        }
        for (i = 0; i < nyNum; i++) {
            if (i % 2 == 0) {
                newY.add(Y.get(i / 2).doubleValue());
            } else {
                newY.add((Y.get((i - 1) / 2).doubleValue() + Y.get((i - 1) / 2 + 1).doubleValue()) / 2);
            }
        }

        return resample_Bilinear(a, X, Y, newX, newY);
    }

    // </editor-fold>    
    // <editor-fold desc="Projection">
    /**
     * Reproject
     *
     * @param x X array
     * @param y Y array
     * @param toProj To projection
     * @return Result arrays
     */
    public static Array[] reproject(Array x, Array y, ProjectionInfo toProj) {
        ProjectionInfo fromProj = KnownCoordinateSystems.geographic.world.WGS1984;
        return reproject(x, y, fromProj, toProj);
    }

    /**
     * Reproject
     *
     * @param x X array
     * @param y Y array
     * @param fromProj From projection
     * @param toProj To projection
     * @return Result arrays
     */
    public static Array[] reproject(Array x, Array y, ProjectionInfo fromProj, ProjectionInfo toProj) {
        Array rx = Array.factory(DataType.DOUBLE, x.getShape());
        Array ry = Array.factory(DataType.DOUBLE, x.getShape());
        int n = (int) x.getSize();
        double[][] points = new double[n][];
        for (int i = 0; i < n; i++) {
            points[i] = new double[]{x.getDouble(i), y.getDouble(i)};
        }
        Reproject.reprojectPoints(points, fromProj, toProj, 0, points.length);
        for (int i = 0; i < n; i++) {
            rx.setDouble(i, points[i][0]);
            ry.setDouble(i, points[i][1]);
        }

        return new Array[]{rx, ry};
    }
    // </editor-fold>
}
