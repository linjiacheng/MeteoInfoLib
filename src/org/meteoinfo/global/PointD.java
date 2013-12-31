/*
 * Copyright 2012 Yaqiang Wang,
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

/**
 *
 * @author User
 */
public class PointD {
    // <editor-fold desc="Variables">

    public double X;
    public double Y;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public PointD() {
    }

    /**
     * Contructor
     * @param x
     * @param y 
     */
    public PointD(double x, double y) {
        X = x;
        Y = y;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Clone
     * 
     * @return PointD object
     */
    @Override
    public Object clone() {
        PointD aP = new PointD();
        aP.X = X;
        aP.Y = Y;
        return aP;
    }
    // </editor-fold>
}
