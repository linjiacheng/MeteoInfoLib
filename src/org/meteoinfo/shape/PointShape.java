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
package org.meteoinfo.shape;

import org.meteoinfo.global.Extent;
import org.meteoinfo.global.PointD;
import java.util.ArrayList;
import java.util.List;

/**
 * Point shape class
 * 
 * @author Yaqiang Wang
 */
public class PointShape extends Shape {
    // <editor-fold desc="Variables">

    private PointD _point = new PointD();
    private double _value;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    public PointShape() {
        this.setShapeType(ShapeTypes.Point);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get point
     * 
     * @return point
     */
    public PointD getPoint() {
        return _point;
    }

    /**
     * Set point
     * 
     * @param aPoint point
     */
    public void setPoint(PointD aPoint) {
        _point = aPoint;
        Extent aExtent = new Extent();
        aExtent.minX = _point.X;
        aExtent.maxX = _point.X;
        aExtent.minY = _point.Y;
        aExtent.maxY = _point.Y;
        this.setExtent(aExtent);
    }

    /**
     * Get value
     * 
     * @return value
     */
    public double getValue() {
        return _value;
    }

    /**
     * Set value
     * 
     * @param value value
     */
    public void setValue(double value) {
        _value = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Get points
     * 
     * @return point list
     */
    @Override
    public List<PointD> getPoints() {
        List<PointD> pList = new ArrayList<PointD>();
        pList.add(_point);

        return pList;
    }

    /**
     * Set points
     * 
     * @param points point list
     */
    @Override
    public void setPoints(List<? extends PointD> points) {
        setPoint(points.get(0));
    }

    /**
     * Clone
     * @return PointShape
     */
    @Override
    public Object clone() {
        PointShape aPS = new PointShape();
        aPS.setPoint(_point);
        aPS.setValue(_value);
        aPS.setVisible(this.isVisible());
        aPS.setSelected(this.isSelected());
        aPS.setLegendIndex(this.getLegendIndex());

        return aPS;
    }
    // </editor-fold>
}
