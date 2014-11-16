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

import org.meteoinfo.global.Extent;

/**
 *
 * @author Yaqiang Wang
 */
public class ProjectionManage {

    /**
     * Get global extent of a projection
     *
     * @param toProj To projection
     * @return Extent
     */
    public static Extent getProjectionGlobalExtent(ProjectionInfo toProj) {
        ProjectionInfo fromProj = KnownCoordinateSystems.geographic.world.WGS1984;
        double x, y, minX = Double.NaN, minY = Double.NaN, maxX = Double.NaN, maxY = Double.NaN;
        int si = -90;
        int ei = 90;
        switch (toProj.getProjectionName()) {
            case Lambert_Conformal_Conic:
                si = -80;
                break;
            case North_Polar_Stereographic_Azimuthal:
                si = 0;
                break;
            case South_Polar_Stereographic_Azimuthal:
                ei = 0;
                break;
        }
        for (int i = si; i <= ei; i++) {
            y = i;
            for (int j = -180; j <= 180; j++) {
                x = i;
                double[][] points = new double[1][];
                points[0] = new double[]{x, y};
                try {
                    Reproject.reprojectPoints(points, fromProj, toProj, 0, 1);
                    x = points[0][0];
                    y = points[0][1];
                    if (Double.isNaN(x) || Double.isNaN(y)) {
                        continue;
                    }
                    if (Double.isNaN(minX)) {
                        minX = x;
                        minY = y;
                    } else {
                        if (x < minX) {
                            minX = x;
                        }
                        if (y < minY) {
                            minY = y;
                        }
                    }
                    if (Double.isNaN(maxX)) {
                        maxX = x;
                        maxY = y;
                    } else {
                        if (x > maxX) {
                            maxX = x;
                        }
                        if (y > maxY) {
                            maxY = y;
                        }
                    }
                } catch (Exception e) {
                }
            }
        }

        Extent aExtent = new Extent();
        aExtent.minX = minX;
        aExtent.maxX = maxX;
        aExtent.minY = minY;
        aExtent.maxY = maxY;
        return aExtent;
    }

    /**
     * Get projected extent
     *
     * @param fromProj From projection
     * @param toProj To projection
     * @param X X coordinate
     * @param Y Y coordinate
     * @return Extent
     */
    public static Extent getProjectionExtent(ProjectionInfo fromProj, ProjectionInfo toProj, double[] X, double[] Y) {
        double x, y, minX = Double.NaN, minY = Double.NaN, maxX = Double.NaN, maxY = Double.NaN;
        int i, j;        
        int minXI = X.length, minYI = Y.length, maxXI = -1, maxYI = -1;
        for (i = 0; i < Y.length; i++) {
            for (j = 0; j < X.length; j++) {
                double[][] points = new double[1][];
                points[0] = new double[]{X[j], Y[i]};
                try {
                    Reproject.reprojectPoints(points, fromProj, toProj, 0, 1);
                    x = points[0][0];
                    y = points[0][1];
                    if (Double.isNaN(x) || Double.isNaN(y)) {
                        continue;
                    }

                    if (Double.isNaN(minY)) {
                        minY = y;
                        minYI = i;
                    }
                    if (i == minYI){
                        if (y < minY)
                            minY = y;
                    } else if (i > minYI)
                        break;
                } catch (Exception e) {
                }
            }
        }
        
        for (i = Y.length - 1; i >= 0; i--) {
            for (j = 0; j < X.length; j++) {
                double[][] points = new double[1][];
                points[0] = new double[]{X[j], Y[i]};
                try {
                    Reproject.reprojectPoints(points, fromProj, toProj, 0, 1);
                    x = points[0][0];
                    y = points[0][1];
                    if (Double.isNaN(x) || Double.isNaN(y)) {
                        continue;
                    }

                    if (Double.isNaN(maxY)) {
                        maxY = y;
                        maxYI = i;
                    }
                    if (i == maxYI){
                        if (y > maxY)
                            maxY = y;
                    } else if (i < maxYI)
                        break;
                } catch (Exception e) {
                }
            }
        }
        
        for (j = 0; j < X.length; j++) {
            for (i = 0; i < Y.length; i++) {
                double[][] points = new double[1][];
                points[0] = new double[]{X[j], Y[i]};
                try {
                    Reproject.reprojectPoints(points, fromProj, toProj, 0, 1);
                    x = points[0][0];
                    y = points[0][1];
                    if (Double.isNaN(x) || Double.isNaN(y)) {
                        continue;
                    }

                    if (Double.isNaN(minX)) {
                        minX = x;
                        minXI = j;
                    }
                    if (j == minXI){
                        if (x < minX)
                            minX = x;
                    } else if (j > minXI)
                        break;
                } catch (Exception e) {
                }
            }
        }
        
        for (j = X.length - 1; j >= 0; j--) {
            for (i = 0; i < Y.length; i++) {
                double[][] points = new double[1][];
                points[0] = new double[]{X[j], Y[i]};
                try {
                    Reproject.reprojectPoints(points, fromProj, toProj, 0, 1);
                    x = points[0][0];
                    y = points[0][1];
                    if (Double.isNaN(x) || Double.isNaN(y)) {
                        continue;
                    }

                    if (Double.isNaN(maxX)) {
                        maxX = x;
                        maxXI = j;
                    }
                    if (j == maxXI){
                        if (x > maxX)
                            maxX = x;
                    } else if (j < maxXI)
                        break;
                } catch (Exception e) {
                }
            }
        }

        if (Double.isNaN(minX))
            return null;
        if (Double.isNaN(minY))
            return null;
        if (Double.isNaN(maxX))
            return null;
        if (Double.isNaN(maxY))
            return null;
                    
        if (toProj.isLonLat()){
            if (maxX < minX && maxX < 0)
                maxX += 360;
        }
        Extent aExtent = new Extent();
        aExtent.minX = minX;
        aExtent.maxX = maxX;
        aExtent.minY = minY;
        aExtent.maxY = maxY;

        return aExtent;
    }
    
    /**
     * Get projected extent
     *
     * @param fromProj From projection
     * @param toProj To projection
     * @param X X coordinate
     * @param Y Y coordinate
     * @return Extent
     */
    public static Extent getProjectionExtent_bak2(ProjectionInfo fromProj, ProjectionInfo toProj, double[] X, double[] Y) {
        double x, y, minX = Double.NaN, minY = Double.NaN, maxX = Double.NaN, maxY = Double.NaN;
        int i, j;        
        for (i = 0; i < Y.length; i++) {
            for (j = 0; j < X.length; j++) {
                double[][] points = new double[1][];
                points[0] = new double[]{X[j], Y[i]};
                try {
                    Reproject.reprojectPoints(points, fromProj, toProj, 0, 1);
                    x = points[0][0];
                    y = points[0][1];
                    if (Double.isNaN(x) || Double.isNaN(y)) {
                        continue;
                    }

                    if (Double.isNaN(minX)) {
                        minX = x;
                        minY = y;
                        maxX = x;
                        maxY = y;
                    } else {
                        if (x < minX) {
                            minX = x;
                        } else if (x > maxX)
                            maxX = x;
                        if (y < minY) {
                            minY = y;
                        } else if (y > maxY)
                            maxY = y;
                    }
                } catch (Exception e) {
                }
            }
        }

        Extent aExtent = new Extent();
        aExtent.minX = minX;
        aExtent.maxX = maxX;
        aExtent.minY = minY;
        aExtent.maxY = maxY;

        return aExtent;
    }

    /**
     * Get projected extent
     *
     * @param fromProj From projection
     * @param toProj To projection
     * @param X X coordinate
     * @param Y Y coordinate
     * @return Extent
     */
    public static Extent getProjectionExtent_bak(ProjectionInfo fromProj, ProjectionInfo toProj, double[] X, double[] Y) {
        double x, y, minX = Double.NaN, minY = Double.NaN, maxX = Double.NaN, maxY = Double.NaN;
        int i;
        for (i = 0; i < Y.length; i++) {
            switch (toProj.getProjectionName()) {
                case Lambert_Conformal_Conic:
                    if (Y[i] < -80) {
                        continue;
                    }
                    break;
                case North_Polar_Stereographic_Azimuthal:
                    if (Y[i] < 0) {
                        continue;
                    }
                    break;
                case South_Polar_Stereographic_Azimuthal:
                    if (Y[i] > 0) {
                        continue;
                    }
                    break;
            }
            double[][] points = new double[1][];
            points[0] = new double[]{X[0], Y[i]};
            try {
                Reproject.reprojectPoints(points, fromProj, toProj, 0, 1);
                x = points[0][0];
                y = points[0][1];
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }

                if (Double.isNaN(minX)) {
                    minX = x;
                    minY = y;
                } else {
                    if (x < minX) {
                        minX = x;
                    }
                    if (y < minY) {
                        minY = y;
                    }
                }
            } catch (Exception e) {
                continue;
            }

            points[0] = new double[]{X[X.length - 1], Y[i]};
            try {
                Reproject.reprojectPoints(points, fromProj, toProj, 0, 1);
                x = points[0][0];
                y = points[0][1];
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }

                if (Double.isNaN(maxX)) {
                    maxY = y;
                    maxX = x;
                } else {
                    if (x > maxX) {
                        maxX = x;
                    }
                    if (y > maxY) {
                        maxY = y;
                    }
                }
            } catch (Exception e) {
            }
        }

        int yIdx = 0;
        int eyIdx = Y.length - 1;
        switch (toProj.getProjectionName()) {
            case Lambert_Conformal_Conic:
                for (i = 0; i < Y.length; i++) {
                    if (Y[i] >= -80) {
                        yIdx = i;
                        break;
                    }
                }
                break;
            case North_Polar_Stereographic_Azimuthal:
                for (i = 0; i < Y.length; i++) {
                    if (Y[i] >= 0) {
                        yIdx = i;
                        break;
                    }
                }
                break;
            case South_Polar_Stereographic_Azimuthal:
                for (i = 0; i < Y.length; i++) {
                    if (Y[i] > 0) {
                        eyIdx = i - 1;
                        break;
                    }
                }
                break;
        }
        if (eyIdx < 0) {
            eyIdx = 0;
        }

        for (i = 0; i < X.length; i++) {
            double[][] points = new double[1][];
            points[0] = new double[]{X[i], Y[yIdx]};
            try {
                Reproject.reprojectPoints(points, fromProj, toProj, 0, 1);
                x = points[0][0];
                y = points[0][1];
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }

                if (Double.isNaN(minX)) {
                    minX = x;
                    minY = y;
                } else {
                    if (x < minX) {
                        minX = x;
                    }
                    if (y < minY) {
                        minY = y;
                    }
                }
            } catch (Exception e) {
                continue;
            }

            points[0] = new double[]{X[i], Y[eyIdx]};
            try {
                Reproject.reprojectPoints(points, fromProj, toProj, 0, 1);
                x = points[0][0];
                y = points[0][1];
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }

                if (Double.isNaN(maxX)) {
                    maxX = x;
                    maxY = y;
                } else {
                    if (x > maxX) {
                        maxX = x;
                    }
                    if (y > maxY) {
                        maxY = y;
                    }
                }
            } catch (Exception e) {
            }
        }

        Extent aExtent = new Extent();
        aExtent.minX = minX;
        aExtent.maxX = maxX;
        aExtent.minY = minY;
        aExtent.maxY = maxY;

        return aExtent;
    }
}
