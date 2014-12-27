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
import java.util.List;

/**
 * Shape class
 *
 * @author Yaqiang Wang
 */
public class Shape {
    // <editor-fold desc="Variables">

    private ShapeTypes _shapeType;
    private boolean _visible;
    private boolean _selected;
    private boolean editing;
    private Extent _extent = new Extent();
    private int _legendIndex = 0;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Contructor
     */
    public Shape() {
        _shapeType = ShapeTypes.Point;
        _visible = true;
        editing = false;
        _selected = false;
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get shape type
     *
     * @return Shape type
     */
    public ShapeTypes getShapeType() {
        return _shapeType;
    }

    /**
     * Set shape type
     *
     * @param aType shape type
     */
    public void setShapeType(ShapeTypes aType) {
        _shapeType = aType;
    }

    /**
     * Get if visible
     *
     * @return is visible or not
     */
    public boolean isVisible() {
        return _visible;
    }

    /**
     * Set visible
     *
     * @param isTrue True or not
     */
    public void setVisible(boolean isTrue) {
        _visible = isTrue;
    }

    /**
     * Get if the shape is selected
     *
     * @return Boolean
     */
    public boolean isSelected() {
        return _selected;
    }

    /**
     * Set selected
     *
     * @param isTrue True or not
     */
    public void setSelected(boolean isTrue) {
        _selected = isTrue;
    }

    /**
     * Get if is editing
     *
     * @return Boolean
     */
    public boolean isEditing() {
        return editing;
    }

    /**
     * Set if is editing
     *
     * @param value Boolean
     */
    public void setEditing(boolean value) {
        editing = value;
    }

    /**
     * Get extent
     *
     * @return extent Extent
     */
    public Extent getExtent() {
        return _extent;
    }

    /**
     * Set extent
     *
     * @param aExtent Extent
     */
    public void setExtent(Extent aExtent) {
        _extent = aExtent;
    }

    /**
     * Get legend index
     *
     * @return Legend index
     */
    public int getLegendIndex() {
        return _legendIndex;
    }

    /**
     * Set legend index
     *
     * @param value Legend index
     */
    public void setLegendIndex(int value) {
        _legendIndex = value;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Get points
     *
     * @return point list
     */
    public List<? extends PointD> getPoints() {
        return null;
    }

    /**
     * Set points
     *
     * @param points point list
     */
    public void setPoints(List<? extends PointD> points) {
    }
    
    /**
     * Add a vertice
     * @param vIdx Vertice index
     * @param vertice The vertice
     */
    public void addVertice(int vIdx, PointD vertice){        
    }
    
    /**
     * Remove a vertice
     * @param vIdx Vertice index
     */
    public void removeVerice(int vIdx){        
    }

    /**
     * Vertice edited update
     *
     * @param vIdx Vertice index
     * @param newX New X
     * @param newY New Y
     */
    public void moveVertice(int vIdx, double newX, double newY) {
        List<PointD> points = (List<PointD>) getPoints();
        if (_shapeType.isPolygon()) {
            int last = points.size() - 1;
            if (vIdx == 0) {
                if (points.get(0).X == points.get(last).X && points.get(0).Y == points.get(last).Y) {
                    points.get(last).X = newX;
                    points.get(last).Y = newY;
                }
            } else if (vIdx == last) {
                if (points.get(0).X == points.get(last).X && points.get(0).Y == points.get(last).Y) {
                    points.get(0).X = newX;
                    points.get(0).Y = newY;
                }
            }
        }

        PointD aP = points.get(vIdx);
        aP.X = newX;
        aP.Y = newY;
        //points.set(vIdx, aP);
        setPoints(points);
    }

    /**
     * Clone
     *
     * @return Shape object
     */
    @Override
    public Object clone() {
        Shape aShape = new Shape();
        aShape.setShapeType(this._shapeType);
        aShape.setVisible(_visible);
        aShape.setExtent(_extent);
        aShape.setSelected(_visible);
        aShape.setLegendIndex(_legendIndex);
        return aShape;
    }
    // </editor-fold>
}
