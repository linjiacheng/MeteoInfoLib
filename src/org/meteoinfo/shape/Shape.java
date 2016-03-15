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

import java.util.ArrayList;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.PointD;
import java.util.List;
import org.meteoinfo.jts.geom.Geometry;
import org.meteoinfo.jts.geom.GeometryFactory;
import org.meteoinfo.jts.operation.polygonize.Polygonizer;

/**
 * Shape class
 *
 * @author Yaqiang Wang
 */
public abstract class Shape implements Cloneable{
    // <editor-fold desc="Variables">

    //private ShapeTypes _shapeType;
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
        //_shapeType = ShapeTypes.Point;
        _visible = true;
        editing = false;
        _selected = false;
    }
    
    /**
     * Constructor
     * @param geometry Geometry
     */
    public Shape(Geometry geometry) {};

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get shape type
     *
     * @return Shape type
     */
    public abstract ShapeTypes getShapeType();

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
        if (this.getShapeType().isPolygon()) {
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
     * To geometry method
     * @param factory GeometryFactory
     * @return Geometry
     */
    public abstract Geometry toGeometry(GeometryFactory factory);
    
    /**
     * To geometry method
     * @return Geometry
     */
    public Geometry toGeometry(){
        return toGeometry(new GeometryFactory());
    };
    
    /**
     * Get intersection shape
     * @param b Other shape
     * @return Intersection shape
     */
    public Shape intersection(Shape b){
        Geometry g1 = this.toGeometry();
        Geometry g2 = b.toGeometry();
        Geometry g3 = g1.intersection(g2);
        switch (this.getShapeType()){
            case Point:
            case PointZ:
                return new PointShape(g3);
            case Polyline:
            case PolylineZ:
                return new PolylineShape(g3);
            case Polygon:
            case PolygonZ:
            case PolygonM:
                return new PolygonShape(g3);
            default:
                return null;
        }
    }
    
    /**
     * Get union shape
     * @param b Other shape
     * @return Union shape
     */
    public Shape union(Shape b){
        Geometry g1 = this.toGeometry();
        Geometry g2 = b.toGeometry();
        Geometry g3 = g1.union(g2);
        switch (this.getShapeType()){
            case Point:
            case PointZ:
                return new PointShape(g3);
            case Polyline:
            case PolylineZ:
                return new PolylineShape(g3);
            case Polygon:
            case PolygonZ:
            case PolygonM:
                return new PolygonShape(g3);
            default:
                return null;
        }
    }
    
    /**
     * Get difference shape
     * @param b Other shape
     * @return Difference shape
     */
    public Shape difference(Shape b){
        Geometry g1 = this.toGeometry();
        Geometry g2 = b.toGeometry();
        Geometry g3 = g1.difference(g2);
        switch (this.getShapeType()){
            case Point:
            case PointZ:
                return new PointShape(g3);
            case Polyline:
            case PolylineZ:
                return new PolylineShape(g3);
            case Polygon:
            case PolygonZ:
            case PolygonM:
                return new PolygonShape(g3);
            default:
                return null;
        }
    }
    
    /**
     * Get system difference shape
     * @param b Other shape
     * @return System difference shape
     */
    public Shape symDifference(Shape b){
        Geometry g1 = this.toGeometry();
        Geometry g2 = b.toGeometry();
        Geometry g3 = g1.symDifference(g2);
        switch (this.getShapeType()){
            case Point:
            case PointZ:
                return new PointShape(g3);
            case Polyline:
            case PolylineZ:
                return new PolylineShape(g3);
            case Polygon:
            case PolygonZ:
            case PolygonM:
                return new PolygonShape(g3);
            default:
                return null;
        }
    }
    
    /**
     * Get buffer shape
     * @param distance Distance
     * @return Intersection shape
     */
    public Shape buffer(double distance){
        Geometry g1 = this.toGeometry();
        Geometry g3 = g1.buffer(distance);
        switch (this.getShapeType()){
            case Point:
            case PointZ:
                return new PointShape(g3);
            case Polyline:
            case PolylineZ:
                return new PolylineShape(g3);
            case Polygon:
            case PolygonZ:
            case PolygonM:
                return new PolygonShape(g3);
            default:
                return null;
        }
    }
    
    /**
     * Get buffer shape
     * @return Intersection shape
     */
    public Shape convexHull(){
        Geometry g1 = this.toGeometry();
        Geometry g3 = g1.convexHull();
        switch (this.getShapeType()){
            case Point:
            case PointZ:
                return new PointShape(g3);
            case Polyline:
            case PolylineZ:
                return new PolylineShape(g3);
            case Polygon:
            case PolygonZ:
            case PolygonM:
                return new PolygonShape(g3);
            default:
                return null;
        }
    }
    
    /**
     * Split shape
     * @param line Split line
     * @return Splitted shapes
     */
    public List<PolygonShape> split(Shape line){
        Geometry g1 = this.toGeometry();
        Geometry g2 = line.toGeometry();
        if (g1.getGeometryType().equals("Polygon")){
            org.meteoinfo.jts.geom.Polygon polygon = (org.meteoinfo.jts.geom.Polygon)g1;
            Polygonizer polygonizer = new Polygonizer();
            Geometry polygons = polygon.getBoundary().union(g2);
            polygonizer.add(polygons);
            List polys = (List)polygonizer.getPolygons();   
            List<PolygonShape> polyShapes = new ArrayList<>();
            for (int i = 0; i < polys.size(); i++){
                org.meteoinfo.jts.geom.Polygon poly = (org.meteoinfo.jts.geom.Polygon)polys.get(i);
                //org.meteoinfo.jts.geom.Polygon rpoly = (org.meteoinfo.jts.geom.Polygon)g1.intersection(poly);
                polyShapes.add(new PolygonShape(poly));
            }
            return polyShapes;
        }
        
        return null;
    }

    /**
     * Clone
     *
     * @return Shape object
     */
    @Override
    public Object clone() {
        Shape o = null;
        try {
            o = (Shape)super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return o;
    }
    // </editor-fold>
}
