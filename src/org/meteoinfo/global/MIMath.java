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

import org.meteoinfo.data.mapdata.Field;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import org.meteoinfo.shape.Shape;

/**
 * MeteoInfo Math class
 *
 * @author Yaqiang Wang
 */
public class MIMath {

    /**
     * Determine if two double data equal
     *
     * @param a double a
     * @param b double b
     * @return boolean
     */
    public static boolean doubleEquals(double a, double b) {
        double difference = Math.abs(a * 0.00001);
        if (Math.abs(a - b) <= difference) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if two double data equal
     *
     * @param a double a
     * @param b double b
     * @return boolean
     */
    public static boolean doubleEquals_Abs(double a, double b) {
        if (Math.abs(a - b) < 0.0000001) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get mininum and maximum values
     *
     * @param S Data array
     * @param unDef Undefined data
     * @return Minimum and Maximum data array
     */
    public static double[] getMinMaxValue(double[] S, double unDef) {
        int i, validNum;
        double min = unDef, max = unDef;

        validNum = 0;
        for (i = 0; i < S.length; i++) {
            if (!(doubleEquals(S[i], unDef))) {
                validNum++;
                if (validNum == 1) {
                    min = S[i];
                    max = min;
                } else {
                    if (S[i] < min) {
                        min = S[i];
                    }
                    if (S[i] > max) {
                        max = S[i];
                    }
                }
            }
        }

        return new double[]{min, max};
    }
    
    /**
     * Get mininum and maximum values
     *
     * @param S Data list
     * @param unDef Undefined data
     * @return Minimum and Maximum data array
     */
    public static double[] getMinMaxValue(List<Double> S, double unDef) {
        int i, validNum;
        double min = unDef, max = unDef;

        validNum = 0;
        for (i = 0; i < S.size(); i++) {
            if (!(doubleEquals(S.get(i), unDef))) {
                validNum++;
                if (validNum == 1) {
                    min = S.get(i);
                    max = min;
                } else {
                    if (S.get(i) < min) {
                        min = S.get(i);
                    }
                    if (S.get(i) > max) {
                        max = S.get(i);
                    }
                }
            }
        }

        return new double[]{min, max};
    }

    /**
     * Determine if a string is digital
     *
     * @param strNumber the string
     * @return Boolean
     */
    public static boolean isNumeric(String strNumber) {
        try {
            Double.parseDouble(strNumber);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Determine if a field is numeric
     *
     * @param field The field
     * @return Boolean
     */
    public static boolean isNumeric(Field field) {
        switch (field.getDataType()) {
            case Integer:
            case Float:
            case Double:
            case Decimal:
                return true;
            default:
                return false;
        }
    }

    /**
     * Get extent from point list
     *
     * @param PList point list
     * @return extent
     */
    public static Extent getPointsExtent(List<? extends PointD> PList) {
        Extent cET = new Extent();
        for (int i = 0; i < PList.size(); i++) {
            PointD aP = PList.get(i);
            if (i == 0) {
                cET.minX = aP.X;
                cET.maxX = aP.X;
                cET.minY = aP.Y;
                cET.maxY = aP.Y;
            } else {
                if (cET.minX > aP.X) {
                    cET.minX = aP.X;
                } else if (cET.maxX < aP.X) {
                    cET.maxX = aP.X;
                }

                if (cET.minY > aP.Y) {
                    cET.minY = aP.Y;
                } else if (cET.maxY < aP.Y) {
                    cET.maxY = aP.Y;
                }
            }
        }

        return cET;
    }

    /**
     * Get extent from PointF array
     *
     * @param PList PointF array
     * @return The extent
     */
    public static Extent getPointFsExtent(PointF[] PList) {
        Extent cET = new Extent();
        for (int i = 0; i < PList.length; i++) {
            PointF aP = (PointF) PList[i];
            if (i == 0) {
                cET.minX = aP.X;
                cET.maxX = aP.X;
                cET.minY = aP.Y;
                cET.maxY = aP.Y;
            } else {
                if (cET.minX > aP.X) {
                    cET.minX = aP.X;
                } else if (cET.maxX < aP.X) {
                    cET.maxX = aP.X;
                }

                if (cET.minY > aP.Y) {
                    cET.minY = aP.Y;
                } else if (cET.maxY < aP.Y) {
                    cET.maxY = aP.Y;
                }
            }
        }

        return cET;
    }

    /**
     * Get extent from PointF list
     *
     * @param PList PointF list
     * @return The extent
     */
    public static Extent getPointFsExtent(List<PointF> PList) {
        Extent cET = new Extent();
        for (int i = 0; i < PList.size(); i++) {
            PointF aP = (PointF) PList.get(i);
            if (i == 0) {
                cET.minX = aP.X;
                cET.maxX = aP.X;
                cET.minY = aP.Y;
                cET.maxY = aP.Y;
            } else {
                if (cET.minX > aP.X) {
                    cET.minX = aP.X;
                } else if (cET.maxX < aP.X) {
                    cET.maxX = aP.X;
                }

                if (cET.minY > aP.Y) {
                    cET.minY = aP.Y;
                } else if (cET.maxY < aP.Y) {
                    cET.maxY = aP.Y;
                }
            }
        }

        return cET;
    }

    /**
     * Array reverse
     *
     * @param points PointD array
     */
    public static void arrayReverse(PointD[] points) {
        int left = 0;          // index of leftmost element
        int right = points.length - 1; // index of rightmost element

        while (left < right) {
            // exchange the left and right elements
            PointD temp = points[left];
            points[left] = points[right];
            points[right] = temp;

            // move the bounds toward the center
            left++;
            right--;
        }
    }
    
    /**
     * Array reverse
     *
     * @param values Double array
     */
    public static void arrayReverse(double[] values) {
        int left = 0;          // index of leftmost element
        int right = values.length - 1; // index of rightmost element

        while (left < right) {
            // exchange the left and right elements
            double temp = values[left];
            values[left] = values[right];
            values[right] = temp;

            // move the bounds toward the center
            left++;
            right--;
        }
    }

    /**
     * Get min, max of an array
     *
     * @param values array
     * @return Min, max
     */
    public static double[] arrayMinMax(double[] values) {
        double min = values[0];
        double max = values[0];

        for (double value : values) {
            min = Math.min(value, min);
            max = Math.max(value, max);
        }

        return new double[]{min, max};
    }
    
    /**
     * Get min, max of an array
     *
     * @param values array
     * @return Min, max
     */
    public static double[] arrayMinMax(Double[] values) {
        double min = values[0];
        double max = values[0];

        for (double value : values) {
            min = Math.min(value, min);
            max = Math.max(value, max);
        }

        return new double[]{min, max};
    }

    /**
     * Get cross point of two line segments
     *
     * @param aP1 point 1 of line a
     * @param aP2 point 2 of line a
     * @param bP1 point 1 of line b
     * @param bP2 point 2 of line b
     * @return cross point
     */
    public static PointF getCrossPoint(PointF aP1, PointF aP2, PointF bP1, PointF bP2) {
        PointF IPoint = new PointF(0, 0);
        PointF p1, p2, q1, q2;
        double tempLeft, tempRight;

        double XP1 = (bP1.X - aP1.X) * (aP2.Y - aP1.Y)
                - (aP2.X - aP1.X) * (bP1.Y - aP1.Y);
        double XP2 = (bP2.X - aP1.X) * (aP2.Y - aP1.Y)
                - (aP2.X - aP1.X) * (bP2.Y - aP1.Y);
        if (XP1 == 0) {
            IPoint = bP1;
        } else if (XP2 == 0) {
            IPoint = bP2;
        } else {
            p1 = aP1;
            p2 = aP2;
            q1 = bP1;
            q2 = bP2;

            tempLeft = (q2.X - q1.X) * (p1.Y - p2.Y) - (p2.X - p1.X) * (q1.Y - q2.Y);
            tempRight = (p1.Y - q1.Y) * (p2.X - p1.X) * (q2.X - q1.X) + q1.X * (q2.Y - q1.Y) * (p2.X - p1.X) - p1.X * (p2.Y - p1.Y) * (q2.X - q1.X);
            IPoint.X = (float) (tempRight / tempLeft);

            tempLeft = (p1.X - p2.X) * (q2.Y - q1.Y) - (p2.Y - p1.Y) * (q1.X - q2.X);
            tempRight = p2.Y * (p1.X - p2.X) * (q2.Y - q1.Y) + (q2.X - p2.X) * (q2.Y - q1.Y) * (p1.Y - p2.Y) - q2.Y * (q1.X - q2.X) * (p2.Y - p1.Y);
            IPoint.Y = (float) (tempRight / tempLeft);
        }

        return IPoint;
    }

    /**
     * Shift extent with longitude
     *
     * @param aET Input extent
     * @param lonShift Longitude shift
     * @return Output extent
     */
    public static Extent shiftExtentLon(Extent aET, double lonShift) {
        Extent cET = new Extent();
        cET.minX = aET.minX + lonShift;
        cET.maxX = aET.maxX + lonShift;
        cET.minY = aET.minY;
        cET.maxY = aET.maxY;

        return cET;
    }

    /**
     * Get maximum extent from two extent
     *
     * @param aET Extent a
     * @param bET Extent b
     * @return Maximum extent
     */
    public static Extent getLagerExtent(Extent aET, Extent bET) {
        Extent cET = new Extent();
        cET.minX = Math.min(aET.minX, bET.minX);
        cET.minY = Math.min(aET.minY, bET.minY);
        cET.maxX = Math.max(aET.maxX, bET.maxX);
        cET.maxY = Math.max(aET.maxY, bET.maxY);

        return cET;
    }

    /**
     * Get Minimum extent from two extent
     *
     * @param aET Extent a
     * @param bET Extent b
     * @return Minimum extent
     */
    public static Extent getSmallerExtent(Extent aET, Extent bET) {
        Extent cET = new Extent();
        cET.minX = Math.max(aET.minX, bET.minX);
        cET.minY = Math.max(aET.minY, bET.minY);
        cET.maxX = Math.min(aET.maxX, bET.maxX);
        cET.maxY = Math.min(aET.maxY, bET.maxY);

        return cET;
    }
    
    /**
     * Get extent of the shapes
     * @param shapes
     * @return Extent
     */
    public static Extent getExtent(List<? extends Shape> shapes){
        Extent extent = (Extent)shapes.get(0).getExtent().clone();
        double minx = extent.minX;
        double maxx = extent.maxX;
        double miny = extent.minY;
        double maxy = extent.maxY;
        Extent ext;
        for (int i = 1; i < shapes.size(); i++){
            ext = shapes.get(i).getExtent();
            if (minx > ext.minX)
                minx = ext.minX;
            if (maxx < ext.maxX)
                maxx = ext.maxX;
            if (miny > ext.minY)
                miny = ext.minY;
            if (maxy < ext.maxY)
                maxy = ext.maxY;
        }
        
        extent.minX = minx;
        extent.maxX = maxx;
        extent.minY = miny;
        extent.maxY = maxy;
        
        return extent;
    }

    /**
     * Determine if two extent cross each other
     *
     * @param aET Extent
     * @param bET Extent
     * @return Boolean
     */
    public static Boolean isExtentCross(Extent aET, Extent bET) {
        if (aET.maxX < bET.minX || aET.maxY < bET.minY || bET.maxX < aET.minX || bET.maxY < aET.minY) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Determine if a point is in an Extent
     *
     * @param aP The point
     * @param aET The extent
     * @return Boolean
     */
    public static boolean pointInExtent(PointD aP, Extent aET) {
        if (aP.X >= aET.minX && aP.X <= aET.maxX && aP.Y >= aET.minY && aP.Y <= aET.maxY) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if a point is in an Extent
     *
     * @param aP The point
     * @param aET The extent
     * @return Boolean
     */
    public static boolean pointInExtent(PointF aP, Extent aET) {
        if (aP.X >= aET.minX && aP.X <= aET.maxX && aP.Y >= aET.minY && aP.Y <= aET.maxY) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if a point is in a rectangel
     *
     * @param aP The point
     * @param aRect The rectangel
     * @return Boolean
     */
    public static boolean pointInRectangle(PointF aP, Rectangle aRect) {
        if (aP.X > aRect.x && aP.X < aRect.x + aRect.width && aP.Y > aRect.y && aP.Y < aRect.y + aRect.height) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if a point is in a rectangel
     *
     * @param aP The point
     * @param aRect The rectangel
     * @return Boolean
     */
    public static boolean pointInRectangle(Point aP, Rectangle aRect) {
        if (aP.x > aRect.x && aP.x < aRect.x + aRect.width && aP.y > aRect.y && aP.y < aRect.y + aRect.height) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if a point is in a rectangel
     *
     * @param aP The point
     * @param aRect The rectangel
     * @return Boolean
     */
    public static boolean pointInRectangle(PointD aP, Rectangle aRect) {
        if (aP.X > aRect.x && aP.X < aRect.x + aRect.width && aP.Y > aRect.y && aP.Y < aRect.y + aRect.height) {
            return true;
        } else {
            return false;
        }
    }

//    /**
//     * Determin if a point is in a polygon
//     *
//     * @param poly Polygon coordinate list
//     * @param aPoint The point
//     * @return Boolean
//     */
//    public static boolean pointInPolygon(List<PointD> poly, PointD aPoint) {
//        double xNew, yNew, xOld, yOld;
//        double x1, y1, x2, y2;
//        int i;
//        boolean inside = false;
//        int nPoints = poly.size();
//
//        if (nPoints < 3) {
//            return false;
//        }
//
//        xOld = poly.get(nPoints - 1).X;
//        yOld = poly.get(nPoints - 1).Y;
//        for (i = 0; i < nPoints; i++) {
//            xNew = poly.get(i).X;
//            yNew = poly.get(i).Y;
//            if (xNew > xOld) {
//                x1 = xOld;
//                x2 = xNew;
//                y1 = yOld;
//                y2 = yNew;
//            } else {
//                x1 = xNew;
//                x2 = xOld;
//                y1 = yNew;
//                y2 = yOld;
//            }
//
//            //---- edge "open" at left end
//            if ((xNew < aPoint.X) == (aPoint.X <= xOld)
//                    && (aPoint.Y - y1) * (x2 - x1) < (y2 - y1) * (aPoint.X - x1)) {
//                inside = !inside;
//            }
//
//            xOld = xNew;
//            yOld = yNew;
//        }
//
//        return inside;
//    }
//
//    /**
//     * Determin if a point is in a polygon
//     *
//     * @param aPGS The polygon
//     * @param aPoint The point
//     * @return Boolean
//     */
//    public static boolean pointInPolygon(PolygonShape aPGS, PointD aPoint) {
//        if (aPoint.X < aPGS.getExtent().minX || aPoint.X > aPGS.getExtent().maxX
//                || aPoint.Y < aPGS.getExtent().minY || aPoint.Y > aPGS.getExtent().maxY) {
//            return false;
//        }
//
//        boolean inside = false;
//        for (int p = 0; p < aPGS.numParts; p++) {
//            List<PointD> pList = new ArrayList<PointD>();
//            if (p == aPGS.numParts - 1) {
//                for (int pp = aPGS.parts[p]; pp < aPGS.numPoints; pp++) {
//                    pList.add(aPGS.getPoints().get(pp));
//                }
//            } else {
//                for (int pp = aPGS.parts[p]; pp < aPGS.parts[p + 1]; pp++) {
//                    pList.add(aPGS.getPoints().get(pp));
//                }
//            }
//            if (pointInPolygon(pList, aPoint)) {
//                inside = true;
//                break;
//            }
//        }
//
//        return inside;
//    }
//
//    /**
//     * Determin if a point is in a polygon
//     *
//     * @param aPGS The polygon
//     * @param x The x
//     * @param y The y
//     * @return Boolean
//     */
//    public static boolean pointInPolygon(PolygonShape aPGS, double x, double y) {
//        PointD aPoint = new PointD(x, y);
//        return pointInPolygon(aPGS, aPoint);
//    }

    /**
     * Judge if a rectangle include another
     *
     * @param aRect a rectangle
     * @param bRect b rectangle
     * @return Boolean
     */
    public static boolean isInclude(Rectangle aRect, Rectangle bRect) {
        if (aRect.width >= bRect.width && aRect.height >= bRect.height) {
            if (aRect.x <= bRect.x && (aRect.x + aRect.width) >= (bRect.x + bRect.width)
                    && aRect.y <= bRect.y && (aRect.y + aRect.height) >= (bRect.y + bRect.height)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Calculate ellipse coordinate by angle
     *
     * @param x0 Center x
     * @param y0 Center y
     * @param a Major semi axis
     * @param b Minor semi axis
     * @param angle Angle
     * @return Coordinate
     */
    public static PointF calEllipseCoordByAngle(double x0, double y0, double a, double b, double angle) {
        double dx, dy;
        dx = Math.sqrt((a * a * b * b) / (b * b + a * a * Math.tan(angle) * Math.tan(angle)));
        dy = dx * Math.tan(angle);

        double x, y;
        if (angle <= Math.PI / 2) {
            x = x0 + dx;
            y = y0 + dy;
        } else if (angle <= Math.PI) {
            x = x0 - dx;
            y = y0 - dy;
        } else if (angle <= Math.PI * 1.5) {
            x = x0 - dx;
            y = y0 - dy;
        } else {
            x = x0 + dx;
            y = y0 + dy;
        }

        PointF aP = new PointF((float) x, (float) y);
        return aP;
    }        

    /**
     * Get decimal number of a double data for ToString() format
     *
     * @param aData Data
     * @return Decimal number
     */
    public static int getDecimalNum(double aData) {
        if (aData - (int) aData == 0) {
            return 0;
        }

        int dNum;
        int aE = (int)Math.floor(Math.log10(aData));

        if (aE >= 0) {
            dNum = 2;
        } else {
            dNum = Math.abs(aE);
        }

        return dNum;
    }

    /**
     * Longitude distance
     *
     * @param lon1 Longitude 1
     * @param lon2 Longitude 2
     * @return Longitude distance
     */
    public static float lonDistance(float lon1, float lon2) {
        if (Math.abs(lon1 - lon2) > 180) {
            if (lon1 > lon2) {
                lon2 += 360;
            } else {
                lon1 += 360;
            }
        }

        return Math.abs(lon1 - lon2);
    }

    /**
     * Add longitude
     *
     * @param lon1 Longitude 1
     * @param delta Delta
     * @return Longitude
     */
    public static float lonAdd(float lon1, float delta) {
        float lon = lon1 + delta;
        if (lon > 180) {
            lon -= 360;
        }
        if (lon < -180) {
            lon += 360;
        }

        return lon;
    }
}
