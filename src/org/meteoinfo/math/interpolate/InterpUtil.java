/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.math.interpolate;

import org.apache.commons.math3.analysis.BivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.BicubicInterpolator;
import org.apache.commons.math3.analysis.interpolation.BicubicSplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.BivariateGridInterpolator;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.meteoinfo.data.ArrayUtil;
import ucar.ma2.Array;
import ucar.ma2.DataType;

/**
 *
 * @author Yaqiang Wang
 */
public class InterpUtil {

    /**
     * Make linear interpolation function - PolynomialSplineFunction
     * @param x X data
     * @param y Y data
     * @return Linear interpolation function
     */
    public static PolynomialSplineFunction linearInterpFunc(Array x, Array y) {
        double[] xd = (double[]) ArrayUtil.copyToNDJavaArray(x);
        double[] yd = (double[]) ArrayUtil.copyToNDJavaArray(y);
        LinearInterpolator li = new LinearInterpolator(); 
        PolynomialSplineFunction psf = li.interpolate(xd, yd);

        return psf;
    }
    
    /**
     * Make interpolation function
     * @param x X data
     * @param y Y data
     * @param kind Specifies the kind of interpolation as a string (‘linear’, 'spline').
     * @return Interpolation function
     */
    public static UnivariateFunction getInterpFunc(Array x, Array y, String kind) {
        double[] xd = (double[]) ArrayUtil.copyToNDJavaArray(x);
        double[] yd = (double[]) ArrayUtil.copyToNDJavaArray(y);
        UnivariateInterpolator li;
        switch (kind) {
            case "spline":
            case "cubic":
                li = new SplineInterpolator();
                break;
            default:
                li = new LinearInterpolator();
                break;
        } 
        UnivariateFunction psf = li.interpolate(xd, yd);

        return psf;
    }
    
    /**
     * Make interpolation function
     * @param x X data
     * @param y Y data
     * @param z Z data
     * @param kind Specifies the kind of interpolation as a string (‘linear’, 'spline').
     * @return Interpolation function
     */
    public static BivariateFunction getInterpFunc(Array x, Array y, Array z, String kind) {
        double[] xd = (double[]) ArrayUtil.copyToNDJavaArray(x);
        double[] yd = (double[]) ArrayUtil.copyToNDJavaArray(y);
        double[][] zd = (double[][]) ArrayUtil.copyToNDJavaArray(z);
        BivariateGridInterpolator li;
        switch (kind) {
            default:
                li = new BicubicInterpolator();
                break;
        } 
        BivariateFunction func = li.interpolate(xd, yd, zd);

        return func;
    }
    
    /**
     * Compute the value of the function
     * @param func The function
     * @param x Input data
     * @return Function value
     */
    public static Array evaluate(UnivariateFunction func, Array x) {
        Array r = Array.factory(DataType.DOUBLE, x.getShape());
        for (int i = 0; i < r.getSize(); i++)
            r.setDouble(i, func.value(x.getDouble(i)));
        
        return r;
    }
    
    /**
     * Compute the value of the function
     * @param func The function
     * @param x Input data
     * @return Function value
     */
    public static double evaluate(UnivariateFunction func, Number x) {
        return func.value(x.doubleValue());
    }
    
    /**
     * Compute the value of the function
     * @param func The function
     * @param x Input x data
     * @param y Input y data
     * @return Function value
     */
    public static Array evaluate(BivariateFunction func, Array x, Array y) {
        Array r = Array.factory(DataType.DOUBLE, x.getShape());
        for (int i = 0; i < r.getSize(); i++)
            r.setDouble(i, func.value(x.getDouble(i), y.getDouble(i)));
        
        return r;
    }
    
    /**
     * Compute the value of the function
     * @param func The function
     * @param x Input x data
     * @param y Input y data
     * @return Function value
     */
    public static double evaluate(BivariateFunction func, Number x, Number y) {
        return func.value(x.doubleValue(), y.doubleValue());
    }
}
