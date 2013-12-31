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
package org.meteoinfo.data;

import org.meteoinfo.global.MIMath;
import java.util.Arrays;
import java.util.List;

/**
 * Template
 *
 * @author Yaqiang Wang
 */
public abstract class DataMath {
    // <editor-fold desc="Operator">

    /**
     * Tack add operator of two objects
     *
     * @param a Object a
     * @param b Object b
     * @return Result object
     */
    public static Object add(Object a, Object b) {
        if (a.getClass() == GridData.class) {
            if (b.getClass() == GridData.class) {
                return ((GridData) a).add((GridData) b);
            } else {
                return ((GridData) a).add((Double) b);
            }
        } else if (a.getClass() == StationData.class) {
            if (b.getClass() == StationData.class) {
                return ((StationData) a).add((StationData) b);
            } else {
                return ((StationData) a).add((Double) b);
            }
        } else {
            if (b.getClass() == GridData.class) {
                return ((GridData) b).add((Double) a);
            } else if (b.getClass() == StationData.class) {
                return ((StationData) b).add((Double) a);
            } else {
                return (Double) a + (Double) b;
            }
        }
    }

    /**
     * Take subtract operator of two objects
     *
     * @param a Object a
     * @param b Object b
     * @return Result object
     */
    public static Object subtract(Object a, Object b) {
        if (a.getClass() == GridData.class) {
            if (b.getClass() == GridData.class) {
                return ((GridData) a).subtract((GridData) b);
            } else {
                return ((GridData) a).subtract((Double) b);
            }
        } else if (a.getClass() == StationData.class) {
            if (b.getClass() == StationData.class) {
                return ((StationData) a).subtract((StationData) b);
            } else {
                return ((StationData) a).subtract((Double) b);
            }
        } else {
            if (b.getClass() == GridData.class) {
                return ((GridData) b).subtract((Double) a);
            } else if (b.getClass() == StationData.class) {
                return ((StationData) b).subtract((Double) a);
            } else {
                return (Double) a - (Double) b;
            }
        }
    }

    /**
     * Take multiply operator of two objects
     *
     * @param a Object a
     * @param b Object b
     * @return Result object
     */
    public static Object multiple(Object a, Object b) {
        if (a.getClass() == GridData.class) {
            if (b.getClass() == GridData.class) {
                return ((GridData) a).multiply((GridData) b);
            } else {
                return ((GridData) a).multiply((Double) b);
            }
        } else if (a.getClass() == StationData.class) {
            if (b.getClass() == StationData.class) {
                return ((StationData) a).multiply((StationData) b);
            } else {
                return ((StationData) a).multiply((Double) b);
            }
        } else {
            if (b.getClass() == GridData.class) {
                return ((GridData) b).multiply((Double) a);
            } else if (b.getClass() == StationData.class) {
                return ((StationData) b).multiply((Double) a);
            } else {
                return (Double) a * (Double) b;
            }
        }
    }

    /**
     * Take divide operator of two objects
     *
     * @param a Object a
     * @param b Object b
     * @return Result object
     */
    public static Object divide(Object a, Object b) {
        if (a.getClass() == GridData.class) {
            if (b.getClass() == GridData.class) {
                return ((GridData) a).divide((GridData) b);
            } else {
                return ((GridData) a).divide((Double) b);
            }
        } else if (a.getClass() == StationData.class) {
            if (b.getClass() == StationData.class) {
                return ((StationData) a).divide((StationData) b);
            } else {
                return ((StationData) a).divide((Double) b);
            }
        } else {
            if (b.getClass() == GridData.class) {
                return ((GridData) b).divide((Double) a);
            } else if (b.getClass() == StationData.class) {
                return ((StationData) b).divide((Double) a);
            } else {
                return (Double) a / (Double) b;
            }
        }
    }

    // </editor-fold>
    // <editor-fold desc="Wind U/V">
    private static double[] getUVFromDS(double windDir, double windSpeed) {
        double dir = windDir + 180;
        if (dir > 360) {
            dir = dir - 360;
        }

        dir = dir * Math.PI / 180;
        double U = windSpeed * Math.sin(dir);
        double V = windSpeed * Math.cos(dir);

        return new double[]{U, V};
    }

    /**
     * Get wind U/V grid data from wind direction/speed grid data
     *
     * @param windDirData Wind directoin grid data
     * @param windSpeedData Wind speed grid data
     * @return U/V grid data
     */
    public static GridData[] getUVFromDS(GridData windDirData, GridData windSpeedData) {
        GridData uData = new GridData(windDirData);
        GridData vData = new GridData(windDirData);
        double[] uv;
        for (int i = 0; i < windDirData.getYNum(); i++) {
            for (int j = 0; j < windDirData.getXNum(); j++) {
                if (MIMath.doubleEquals(windDirData.data[i][j], windDirData.missingValue)
                        || MIMath.doubleEquals(windSpeedData.data[i][j], windSpeedData.missingValue)) {
                    uData.data[i][j] = uData.missingValue;
                    vData.data[i][j] = vData.missingValue;
                } else {
                    uv = getUVFromDS(windDirData.data[i][j], windSpeedData.data[i][j]);
                    uData.data[i][j] = uv[0];
                    vData.data[i][j] = uv[1];
                }
            }
        }

        return new GridData[]{uData, vData};
    }

    /**
     * Get wind U/V station data from wind direction/speed station data
     *
     * @param windDirData Wind direction station data
     * @param windSpeedData Wind speed station data
     * @return U/V station data
     */
    public static StationData[] getUVFromDS(StationData windDirData, StationData windSpeedData) {
        StationData uData = new StationData(windDirData);
        StationData vData = new StationData(windSpeedData);
        double[] uv;
        for (int i = 0; i < windDirData.getStNum(); i++) {
            if (MIMath.doubleEquals(windDirData.data[2][i], windDirData.missingValue)
                    || MIMath.doubleEquals(windSpeedData.data[2][i], windSpeedData.missingValue)) {
                uData.data[2][i] = uData.missingValue;
                vData.data[2][i] = vData.missingValue;
            } else {
                uv = getUVFromDS(windDirData.data[2][i], windSpeedData.data[2][i]);
                uData.data[2][i] = uv[0];
                vData.data[2][i] = uv[1];
            }
        }
        return new StationData[]{uData, vData};
    }

    private static double[] getDSFromUV(double U, double V) {
        double windSpeed = Math.sqrt(U * U + V * V);
        double windDir;
        if (windSpeed == 0) {
            windDir = 0;
        } else {
            windDir = Math.asin(U / windSpeed) * 180 / Math.PI;
            if (U < 0 && V < 0) {
                windDir = 180.0 - windDir;
            } else if (U > 0 && V < 0) {
                windDir = 180.0 - windDir;
            } else if (U < 0 && V > 0) {
                windDir = 360.0 + windDir;
            }
            windDir += 180;
            if (windDir >= 360) {
                windDir -= 360;
            }
        }

        return new double[]{windDir, windSpeed};
    }

    /**
     * Get wind direction/speed grid data from wind U/V grid data
     *
     * @param uData U grid data
     * @param vData V grid data
     * @return Wind direction/speed grid data
     */
    public static GridData[] getDSFromUV(GridData uData, GridData vData) {
        GridData windDirData = new GridData(uData);
        GridData windSpeedData = new GridData(uData);
        double[] ds;
        for (int i = 0; i < uData.getYNum(); i++) {
            for (int j = 0; j < uData.getXNum(); j++) {
                if (MIMath.doubleEquals(uData.data[i][j], uData.missingValue)
                        || MIMath.doubleEquals(vData.data[i][j], vData.missingValue)) {
                    windDirData.data[i][j] = windDirData.missingValue;
                    windSpeedData.data[i][j] = windSpeedData.missingValue;
                } else {
                    ds = getDSFromUV(uData.data[i][j], vData.data[i][j]);
                    windDirData.data[i][j] = ds[0];
                    windSpeedData.data[i][j] = ds[1];
                }
            }
        }

        return new GridData[]{windDirData, windSpeedData};
    }

    /**
     * Get wind direction/speed station data from wind U/V station data
     *
     * @param uData U station data
     * @param vData V station data
     * @return Wind direction/speed station data
     */
    public static StationData[] getDSFromUV(StationData uData, StationData vData) {
        StationData windDirData = new StationData(uData);
        StationData windSpeedData = new StationData(vData);
        double[] ds;
        for (int i = 0; i < windDirData.getStNum(); i++) {
            if (MIMath.doubleEquals(uData.data[2][i], uData.missingValue)
                    || MIMath.doubleEquals(vData.data[2][i], vData.missingValue)) {
                windDirData.data[2][i] = windDirData.missingValue;
                windSpeedData.data[2][i] = windSpeedData.missingValue;
            } else {
                ds = getDSFromUV(uData.data[2][i], vData.data[2][i]);
                windDirData.data[2][i] = ds[0];
                windSpeedData.data[2][i] = ds[1];
            }
        }

        return new StationData[]{windDirData, windSpeedData};
    }
    // </editor-fold>
    // <editor-fold desc="Fitting">

    /**
     * Summary the value array
     *
     * @param values Values
     * @return Summary
     */
    public static double sum(double[] values) {
        double sum = 0.0;
        for (int i = 0; i < values.length; i++) {
            sum = sum + values[i];
        }
        sum = sum / values.length;

        return sum;
    }

    // </editor-fold>
    // <editor-fold desc="Function">
    /**
     * Take abstract value
     *
     * @param a Object a
     * @return Result object
     */
    public static Object abs(Object a) {
        if (a.getClass() == GridData.class) {
            return ((GridData) a).abs();
        }
        if (a.getClass() == StationData.class) {
            return ((StationData) a).abs();
        } else {
            return Math.abs((Double) a);
        }
    }

    /**
     * Take anti-sine value
     *
     * @param a Object a
     * @return Result object
     */
    public static Object asin(Object a) {
        if (a.getClass() == GridData.class) {
            return ((GridData) a).asin();
        }
        if (a.getClass() == StationData.class) {
            return ((StationData) a).asin();
        } else {
            return Math.asin((Double) a);
        }
    }
    
    /**
     * Take anti-cosine value
     *
     * @param a Object a
     * @return Result object
     */
    public static Object acos(Object a) {
        if (a.getClass() == GridData.class) {
            return ((GridData) a).acos();
        }
        if (a.getClass() == StationData.class) {
            return ((StationData) a).acos();
        } else {
            return Math.acos((Double) a);
        }
    }
    
    /**
     * Take anti-tangent value
     *
     * @param a Object a
     * @return Result object
     */
    public static Object atan(Object a) {
        if (a.getClass() == GridData.class) {
            return ((GridData) a).atan();
        }
        if (a.getClass() == StationData.class) {
            return ((StationData) a).atan();
        } else {
            return Math.atan((Double) a);
        }
    }
    
    /**
     * Take sine value
     *
     * @param a Object a
     * @return Result object
     */
    public static Object sin(Object a) {
        if (a.getClass() == GridData.class) {
            return ((GridData) a).sin();
        }
        if (a.getClass() == StationData.class) {
            return ((StationData) a).sin();
        } else {
            return Math.sin((Double) a);
        }
    }
    
    /**
     * Take cosine value
     *
     * @param a Object a
     * @return Result object
     */
    public static Object cos(Object a) {
        if (a.getClass() == GridData.class) {
            return ((GridData) a).cos();
        }
        if (a.getClass() == StationData.class) {
            return ((StationData) a).cos();
        } else {
            return Math.cos((Double) a);
        }
    }
    
    /**
     * Take tangent value
     *
     * @param a Object a
     * @return Result object
     */
    public static Object tan(Object a) {
        if (a.getClass() == GridData.class) {
            return ((GridData) a).tan();
        }
        if (a.getClass() == StationData.class) {
            return ((StationData) a).tan();
        } else {
            return Math.tan((Double) a);
        }
    }
    
    /**
     * Take e base power value
     *
     * @param a Object a
     * @return Result object
     */
    public static Object exp(Object a) {
        if (a.getClass() == GridData.class) {
            return ((GridData) a).exp();
        }
        if (a.getClass() == StationData.class) {
            return ((StationData) a).exp();
        } else {
            return Math.exp((Double) a);
        }
    }
    
    /**
     * Take power value
     *
     * @param a Object a
     * @param p Power value
     * @return Result object
     */
    public static Object pow(Object a, double p) {
        if (a.getClass() == GridData.class) {
            return ((GridData) a).pow(p);
        }
        if (a.getClass() == StationData.class) {
            return ((StationData) a).pow(p);
        } else {
            return Math.pow((Double) a, p);
        }
    }
    
    /**
     * Take logrithm value
     *
     * @param a Object a
     * @return Result object
     */
    public static Object log(Object a) {
        if (a.getClass() == GridData.class) {
            return ((GridData) a).log();
        }
        if (a.getClass() == StationData.class) {
            return ((StationData) a).log();
        } else {
            return Math.log((Double) a);
        }
    }
    
    /**
     * Take 10 base logrithm value
     *
     * @param a Object a
     * @return Result object
     */
    public static Object log10(Object a) {
        if (a.getClass() == GridData.class) {
            return ((GridData) a).log10();
        }
        if (a.getClass() == StationData.class) {
            return ((StationData) a).log10();
        } else {
            return Math.log10((Double) a);
        }
    }
    
    /**
     * Take squre root value
     *
     * @param a Object a
     * @return Result object
     */
    public static Object sqrt(Object a) {
        if (a.getClass() == GridData.class) {
            return ((GridData) a).sqrt();
        }
        if (a.getClass() == StationData.class) {
            return ((StationData) a).sqrt();
        } else {
            return Math.sqrt((Double) a);
        }
    }
    // </editor-fold>
    // <editor-fold desc="Statistics">
    /**
     * Determine the least square trend equation - linear fitting
     *
     * @param xData X data array
     * @param yData Y data array
     * @return Result array - y intercept and slope
     */
    public static double[] leastSquareTrend(double[] xData, double[] yData) {
        int n = xData.length;
        double sumX = 0.0;
        double sumY = 0.0;
        double sumSquareX = 0.0;
        double sumXY = 0.0;
        for (int i = 0; i < n; i++) {
            sumX += xData[i];
            sumY += yData[i];
            sumSquareX += xData[i] * xData[i];
            sumXY += xData[i] * yData[i];
        }

        double a = (sumSquareX * sumY - sumX * sumXY) / (n * sumSquareX - sumX * sumX);
        double b = (n * sumXY - sumX * sumY) / (n * sumSquareX - sumX * sumX);

        return new double[]{a, b};
    }

    /**
     * Determine the least square trend equation - linear fitting
     *
     * @param dataList Grid data list
     * @param xData X data array
     * @return Result grid data - slop
     */
    public static GridData leastSquareTrend(List<GridData> dataList, double[] xData) {
        int n = dataList.size();
        double[] yData = new double[n];
        double missingValue = dataList.get(0).missingValue;
        GridData rData = new GridData(dataList.get(0));
        rData = rData.setValue(missingValue);
        double value;
        boolean ifcal;
        for (int i = 0; i < dataList.get(0).getYNum(); i++) {
            for (int j = 0; j < dataList.get(0).getXNum(); j++) {
                ifcal = true;
                for (int d = 0; d < n; d++) {
                    value = dataList.get(d).data[i][j];
                    if (MIMath.doubleEquals(value, dataList.get(d).missingValue)) {
                        ifcal = false;
                        break;
                    }
                    yData[d] = dataList.get(d).data[i][j];
                }
                if (ifcal) {
                    rData.data[i][j] = leastSquareTrend(xData, yData)[1];
                }
            }
        }

        return rData;
    }

    /**
     * Mann-Kendall trend statistics
     *
     * @param ts Input data array
     * @return Result array - z (trend)/beta (change value per unit time)
     */
    public static double[] mann_Kendall_Trend(double[] ts) {
        int i, j, s = 0, k = 0;
        int n = ts.length;
        double[] differ = new double[n * (n - 1) / 2];
        double z, beta;

        //Calculate z
        for (i = 0; i < n - 1; i++) {
            for (j = i + 1; j < n; j++) {
                if (ts[j] > ts[i]) {
                    s = s + 1;
                } else if (ts[j] < ts[i]) {
                    s = s - 1;
                }
                differ[k] = (ts[j] - ts[i]) / (j - i);
                k += 1;
            }
        }

        double var = n * (n - 1) * (2 * n + 5) / 18;

        if (s > 0) {
            z = (double) (s - 1) / Math.sqrt(var);
        } else if (s == 0) {
            z = 0;
        } else {
            z = (double) (s + 1) / Math.sqrt(var);
        }

        //Calculate beta
        Arrays.sort(differ);
        if (k % 2 == 0) {
            beta = (differ[k / 2] + differ[k / 2 + 1]) / 2;
        } else {
            beta = differ[k / 2 + 1];
        }

        return new double[]{z, beta};
    }

    /**
     * Mann-Kendall trend statistics
     *
     * @param ts Input data array
     * @return Result array - z (trend)/beta (change value per unit time)
     */
    public static double[] mann_Kendall_Trend_1(double[] ts) {
        int i, j, s = 0, k = 0;
        int n = ts.length;
        int p[] = new int[n - 1];
        int psum = 0;
        double[] differ = new double[n * (n - 1) / 2];
        double beta;

        //Calculate z
        for (i = 0; i < n - 1; i++) {
            s = 0;
            for (j = i + 1; j < n; j++) {
                if (ts[j] > ts[i]) {
                    s = s + 1;
                }
                differ[k] = (ts[j] - ts[i]) / (j - i);
                k += 1;
            }
            p[i] = s;
            psum += s;
        }

        double t = 4.0 * psum / (n * (n - 1)) - 1.0;
        double var = 2.0 * (2.0 * n + 5.0) / (9.0 * n * (n - 1));

        double u = t / Math.sqrt(var);

        //Calculate beta
        Arrays.sort(differ);
        if (k % 2 == 0) {
            beta = (differ[k / 2] + differ[k / 2 + 1]) / 2;
        } else {
            beta = differ[k / 2 + 1];
        }

        return new double[]{u, beta};
    }
    // </editor-fold>
}
