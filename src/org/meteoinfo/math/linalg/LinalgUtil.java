/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.math.linalg;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.meteoinfo.data.ArrayUtil;
import ucar.ma2.Array;
import ucar.ma2.DataType;

/**
 *
 * @author Yaqiang Wang
 */
public class LinalgUtil {
    /**
     * Solve a linear matrix equation, or system of linear scalar equations.
     * @param a Coefficient matrix.
     * @param b Ordinate or “dependent variable” values.
     * @return Solution to the system a x = b. Returned shape is identical to b.
     */
    public static Array solve(Array a, Array b){
        Array r = Array.factory(DataType.DOUBLE, b.getShape());
        double[][] aa = (double[][])ArrayUtil.copyToNDJavaArray(a);
        RealMatrix coefficients = new Array2DRowRealMatrix(aa, false);
        DecompositionSolver solver = new LUDecomposition(coefficients).getSolver();
        double[] bb = (double[])ArrayUtil.copyToNDJavaArray(b);
        RealVector constants = new ArrayRealVector(bb, false);
        RealVector solution = solver.solve(constants);
        for (int i = 0; i < r.getSize(); i++){
            r.setDouble(i, solution.getEntry(i));
        }
        
        return r;
    }
}
