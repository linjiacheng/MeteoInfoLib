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
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.meteoinfo.legend.ColorBreak;

/**
 *
 * @author Yaqiang Wang
 */
public class GraphicCollection extends Graphic implements Iterator {
    // <editor-fold desc="Variables">
    List<Graphic> graphics = new ArrayList<>();
    private Extent _extent = new Extent();
    private boolean singleLegend = true;
    private int index;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public GraphicCollection() {
        this.index = 0;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get graphic list
     * @return Graphic list
     */
    @Override
    public List<Graphic> getGraphics(){
        return this.graphics;
    }
    
    /**
     * Set graphic list
     * @param value Graphic list
     */
    public void setGraphics(List<Graphic> value){
        this.graphics = value;
    }

    /**
     * Get extent
     *
     * @return The extent
     */
    @Override
    public Extent getExtent() {
        return _extent;
    }
    
    /**
     * Get is single legend or not
     * @return Boolean
     */
    @Override
    public boolean isSingleLegend(){
        return this.singleLegend;
    }
    
    /**
     * Set is single legend or not
     * @param value Boolean
     */
    public void setSingleLegend(boolean value){
        this.singleLegend = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Update extent
     */
    public void updateExtent() {
        int i = 0;
        for (Graphic g : this.graphics) {
            if (i == 0) {
                _extent = g.getShape().getExtent();
            } else {
                _extent = MIMath.getLagerExtent(_extent, g.getShape().getExtent());
            }

            i += 1;
        }
    }

    /**
     * Add a graphic
     *
     * @param aGraphic The graphic
     * @return Boolean
     */
    public boolean add(Graphic aGraphic) {
        boolean istrue = this.graphics.add(aGraphic);
        
        //Update extent
        if (this.graphics.size() == 1) {
            _extent = aGraphic.getExtent();
        } else {
            _extent = MIMath.getLagerExtent(_extent, aGraphic.getExtent());
        }

        return istrue;
    }

    /**
     * Inset a graphic
     *
     * @param index Index
     * @param aGraphic The graphic
     */
    public void add(int index, Graphic aGraphic) {
        this.graphics.add(index, aGraphic);

        //Update extent
        if (this.graphics.size() == 1) {
            _extent = aGraphic.getShape().getExtent();
        } else {
            _extent = MIMath.getLagerExtent(_extent, aGraphic.getShape().getExtent());
        }
    }
    
    /**
     * Get a graphic by index
     * @param idx Index
     * @return Graphic
     */
    public Graphic get(int idx){
        return this.graphics.get(idx);
    }
    
    /**
     * Index of
     * @param g Graphic
     * @return Index
     */
    public int indexOf(Graphic g){
        return this.graphics.indexOf(g);
    }
    
    /**
     * Contains or not
     * @param g Graphic
     * @return Boolean
     */
    public boolean contains(Graphic g){
        return this.graphics.contains(g);
    }
    
    /**
     * Get graphic list size
     * @return Gaphic list size
     */
    public int size(){
        return this.graphics.size();
    }
    
    /**
     * Get is empty or not
     * @return Boolean
     */
    public boolean isEmpty(){
        return this.graphics.isEmpty();
    }
    
    /**
     * Get graphics number
     * @return 1
     */
    @Override
    public int getNumGrahics(){
        return this.size();
    }
    
    /**
     * Get Graphic by index
     * @param idx Index
     * @return Graphic
     */
    @Override
    public Graphic getGraphicN(int idx){
        return this.get(idx);
    }

    /**
     * Remove a graphic
     *
     * @param aGraphic The graphic
     * @return Boolean
     */
    public boolean remove(Graphic aGraphic) {
        boolean istrue = this.graphics.remove(aGraphic);
        this.updateExtent();

        return istrue;
    }

    /**
     * Remove a graphic by index
     *
     * @param index The index
     * @return The removed graphic
     */
    public Graphic remove(int index) {
        Graphic ag = this.graphics.remove(index);
        this.updateExtent();

        return ag;
    }
    
    /**
     * Clear graphics
     */
    public void clear(){
        this.graphics.clear();
    }
    
    /**
     * Add all
     * @param gs Graphic list
     */
    public void addAll(List<Graphic> gs){
        this.graphics.addAll(gs);
    }
    
    /**
     * Remove all
     * @param gs Graphic list
     */
    public void removeAll(List<Graphic> gs){
        this.graphics.removeAll(gs);
    }
    
    /**
     * Get legend
     * @return Legend
     */
    @Override
    public ColorBreak getLegend(){
        return this.graphics.get(0).getLegend();
    }

    /// <summary>
    /// Select graphics by an extent
    /// </summary>
    /// <param name="aExtent">extent</param>
    /// <param name="selectedGraphics">ref selected graphics</param>
    /// <returns>if selected</returns>
    public GraphicCollection selectGraphics(Extent aExtent) {
        GraphicCollection selectedGraphics = new GraphicCollection();
        int i, j;
        PointD aPoint = new PointD();
        aPoint.X = (aExtent.minX + aExtent.maxX) / 2;
        aPoint.Y = (aExtent.minY + aExtent.maxY) / 2;

        for (Graphic aGraphic : this.graphics) {
            switch (aGraphic.getShape().getShapeType()) {
                case Point:
                    PointShape aPS = (PointShape) aGraphic.getShape();
                    if (MIMath.pointInExtent(aPS.getPoint(), aExtent)) {
                        selectedGraphics.add(aGraphic);
                    }
                    break;
                case Polyline:
                case PolylineZ:
                    PolylineShape aPLS = (PolylineShape) aGraphic.getShape();
                    if (MIMath.isExtentCross(aExtent, aPLS.getExtent())) {
                        for (j = 0; j < aPLS.getPoints().size(); j++) {
                            aPoint = aPLS.getPoints().get(j);
                            if (MIMath.pointInExtent(aPoint, aExtent)) {
                                selectedGraphics.add(aGraphic);
                                break;
                            }
                        }
                    }
                    break;
                case Polygon:
                case Rectangle:
                    PolygonShape aPGS = (PolygonShape) aGraphic.getShape();
                    if (!(aPGS.getPartNum() > 1)) {
                        if (GeoComputation.pointInPolygon(aPGS.getPoints(), aPoint)) {
                            selectedGraphics.add(aGraphic);
                        }
                    } else {
                        for (int p = 0; p < aPGS.getPartNum(); p++) {
                            ArrayList pList = new ArrayList();
                            if (p == aPGS.getPartNum() - 1) {
                                for (int pp = aPGS.parts[p]; pp < aPGS.getPointNum(); pp++) {
                                    pList.add(aPGS.getPoints().get(pp));
                                }
                            } else {
                                for (int pp = aPGS.parts[p]; pp < aPGS.parts[p + 1]; pp++) {
                                    pList.add(aPGS.getPoints().get(pp));
                                }
                            }
                            if (GeoComputation.pointInPolygon(pList, aPoint)) {
                                selectedGraphics.add(aGraphic);
                                break;
                            }
                        }
                    }
                    break;
            }
        }

        return selectedGraphics;
    }
    
    @Override
    public boolean hasNext() {
        return index < this.size() - 1;
    }

    @Override
    public Object next() {
        if (index >= this.size()) {
            throw new NoSuchElementException();
        }
        
        return this.get(index++);
    }
    // </editor-fold>
}
