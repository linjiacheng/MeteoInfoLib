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
package org.meteoinfo.shape;

import org.meteoinfo.geoprocess.GeoComputation;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PolygonShape class
 * 
 * @author Yaqiang Wang
 */
public class PolygonShape extends Shape implements Cloneable{
    // <editor-fold desc="Variables">

    private List<PointD> _points;
    private List<Polygon> _polygons;
    /**
     * Start value
     */
    public double lowValue;
    /**
     * End value
     */
    public double highValue;
    /**
     * Part number
     */
    private int _numParts;
    /**
     * Part array
     */
    public int[] parts;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public PolygonShape() {
        this.setShapeType(ShapeTypes.Polygon);
        _points = new ArrayList<>();
        _numParts = 1;
        parts = new int[1];
        parts[0] = 0;
        _polygons = new ArrayList<>();
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get points
     * @return point list
     */
    @Override
    public List<PointD> getPoints() {
        return _points;
    }

    /**
     * Set points
     * 
     * @param points point list
     */
    @Override
    public void setPoints(List<? extends PointD> points) {
        _points = (List<PointD>)points;
        this.setExtent(MIMath.getPointsExtent(_points));
        updatePolygons();
    }
    
    /**
     * Get part number
     * @return Part number
     */
    public int getPartNum(){
        return this._numParts;
    }
    
    /**
     * Set part number
     * @param value Part number
     */
    public void setPartNum(int value){
        this._numParts = value;
    }
    
    /**
     * Get point number
     * @return Point number
     */
    public int getPointNum(){
        return this._points.size();
    }

    /**
     * Get polygons
     * 
     * @return polygon list
     */
    public List<Polygon> getPolygons() {
        return _polygons;
    }

    /**
     * Set polygons
     * 
     * @param polygons polygon list
     */
    public void setPolygons(List<Polygon> polygons) {
        _polygons = polygons;
        updatePartsPoints();
    }

    /**
     * Get area
     * 
     * @return area
     */
    public double getArea() {
        double area = 0.0;
        for (Polygon aPG : _polygons) {
            area += GeoComputation.getArea(aPG.getOutLine());
            for (List<PointD> hole : aPG.getHoleLines()) {
                area -= GeoComputation.getArea(hole);
            }
        }

        return area;
    }

    /**
     * Get spherical area
     * 
     * @return spherical area
     */
    public double getSphericalArea() {
        double area = 0.0;
        for (Polygon aPG : _polygons) {
            area += GeoComputation.sphericalPolygonArea(aPG.getOutLine());
            for (List<PointD> hole : aPG.getHoleLines()) {
                area -= GeoComputation.sphericalPolygonArea(hole);
            }
        }

        return area;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    private void updatePolygons() {
        _polygons = new ArrayList<>();
        if (_numParts == 1) {
            Polygon aPolygon = new Polygon();
            aPolygon.setOutLine(_points);
            _polygons.add(aPolygon);
        } else {
            PointD[] Pointps;
            Polygon aPolygon = null;
            int numPoints = this.getPointNum();
            for (int p = 0; p < _numParts; p++) {
                if (p == _numParts - 1) {
                    Pointps = new PointD[numPoints - parts[p]];
                    for (int pp = parts[p]; pp < numPoints; pp++) {
                        Pointps[pp - parts[p]] = _points.get(pp);
                    }
                } else {
                    Pointps = new PointD[parts[p + 1] - parts[p]];
                    for (int pp = parts[p]; pp < parts[p + 1]; pp++) {
                        Pointps[pp - parts[p]] = _points.get(pp);
                    }
                }

                if (GeoComputation.isClockwise(Pointps)) {
                    if (p > 0) {
                        _polygons.add(aPolygon);
                    }

                    aPolygon = new Polygon();
                    aPolygon.setOutLine(Arrays.asList(Pointps));
                } else {
                    if (aPolygon == null) {
                        MIMath.arrayReverse(Pointps);
                        aPolygon = new Polygon();
                        aPolygon.setOutLine(Arrays.asList(Pointps));
                    } else {
                        aPolygon.addHole(Arrays.asList(Pointps));
                    }
                }
            }
            _polygons.add(aPolygon);
        }
    }

    private void updatePartsPoints() {
        _numParts = 0;
        _points = new ArrayList<>();
        List<Integer> partList = new ArrayList<>();
        for (int i = 0; i < _polygons.size(); i++) {
            _numParts += _polygons.get(i).getRingNumber();
            for (int j = 0; j < _polygons.get(i).getRingNumber(); j++) {
                partList.add(_points.size());
                _points.addAll(_polygons.get(i).getRings().get(j));                
            }
        }
        parts = new int[partList.size()];
        for (int i = 0; i < partList.size(); i++) {
            parts[i] = partList.get(i);
        }
        this.setExtent(MIMath.getPointsExtent(_points));
    }

    /**
     * Add a hole line
     * 
     * @param points point list
     * @param polygonIdx polygon index
     */
    public void addHole(List<PointD> points, int polygonIdx) {
        Polygon aPolygon = _polygons.get(polygonIdx);
        aPolygon.addHole(points);

        updatePartsPoints();
    }
    
    /**
     * Get part index
     * @param vIdx The vertice index
     * @return Part index
     */
    public int getPartIndex(int vIdx){
        if (_numParts == 1)
            return 0;
        else {
            for (int p = 1; p < _numParts; p++) {
                if (vIdx < parts[p])
                    return p - 1;
            }
            return _numParts - 1;
        }
    }
    
    /**
     * Add a vertice
     * @param vIdx Vertice index
     * @param vertice The vertice
     */
    @Override
    public void addVertice(int vIdx, PointD vertice){     
        int partIdx = getPartIndex(vIdx);
        if (partIdx < _numParts - 1)
            parts[partIdx + 1] += 1;
        
        ((List<PointD>)_points).add(vIdx, vertice);
        this.setExtent(MIMath.getPointsExtent(_points));
        this.updatePolygons();
    }
    
    /**
     * Remove a vertice
     * @param vIdx Vertice index
     */
    @Override
    public void removeVerice(int vIdx){      
        int partIdx = getPartIndex(vIdx);
        if (partIdx < _numParts - 1)
            parts[partIdx + 1] -= 1;
        
        ((List<PointD>)_points).remove(vIdx);
        this.setExtent(MIMath.getPointsExtent(_points));
        this.updatePolygons();
    }

    @Override
    public Object clone() {
        return (PolygonShape)super.clone();
    }
    
    /** 
     * Clone
     * @return PolygonShape
     */
    public Object clone_old() {
        PolygonShape aPGS = new PolygonShape();
        aPGS.setExtent(this.getExtent());
        aPGS.highValue = highValue;
        aPGS.lowValue = lowValue;
        aPGS._numParts = _numParts;
        aPGS.parts = (int[]) parts.clone();
        aPGS.setPoints(_points);
        aPGS.setVisible(this.isVisible());
        aPGS.setSelected(this.isSelected());
        aPGS.setLegendIndex(this.getLegendIndex());

        return aPGS;
    }

    /**
     * Clone polygon shape with values
     * 
     * @return new polygon shape
     */
    public PolygonShape valueClone() {
        PolygonShape aPGS = new PolygonShape();
        aPGS.highValue = highValue;
        aPGS.lowValue = lowValue;
        aPGS.setVisible(this.isVisible());
        aPGS.setSelected(this.isSelected());
        aPGS.setLegendIndex(this.getLegendIndex());

        return aPGS;
    }
    // </editor-fold>
}
