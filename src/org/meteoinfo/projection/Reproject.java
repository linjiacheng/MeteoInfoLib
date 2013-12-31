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
package org.meteoinfo.projection;

import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.CoordinateTransformFactory;
import org.osgeo.proj4j.ProjCoordinate;

/**
 *
 * @author Yaqiang Wang
 */
public class Reproject {

    private static CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();

    /**
     * Reproject points
     *
     * @param points The points
     * @param source Source projection info
     * @param dest Destination projection info
     * @param startIndex Start index
     * @param numPoints Point number
     */
    public static void reprojectPoints(double[][] points, ProjectionInfo source, ProjectionInfo dest, int startIndex, int numPoints) {
        CoordinateTransform trans = ctFactory.createTransform(source.getCoordinateReferenceSystem(), dest.getCoordinateReferenceSystem());
        if (source.getProjectionName() == ProjectionNames.LongLat) {
            for (int i = startIndex; i < startIndex + numPoints; i++) {
                if (i >= points.length) {
                    break;
                }
                if (points[i][0] > 180.0) {
                    points[i][0] -= 360;
                }
            }
        }
        for (int i = startIndex; i < startIndex + numPoints; i++) {
            if (i >= points.length) {
                break;
            }
            ProjCoordinate p1 = new ProjCoordinate(points[i][0], points[i][1]);
            ProjCoordinate p2 = new ProjCoordinate();
            trans.transform(p1, p2);
            points[i][0] = p2.x;
            points[i][1] = p2.y;
        }
    }
}
