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

/**
 *
 * @author Yaqiang Wang
 */
public class GraphicCollection extends ArrayList<Graphic> {
    // <editor-fold desc="Variables">

    private Extent _extent = new Extent();
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public GraphicCollection() {
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get extent
     *
     * @return The extent
     */
    public Extent getExtent() {
        return _extent;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    private void updateExtent() {
        int i = 0;
        for (Graphic g : this) {
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
    @Override
    public boolean add(Graphic aGraphic) {
        boolean istrue = super.add(aGraphic);

        //Update extent
        if (this.size() == 1) {
            _extent = aGraphic.getShape().getExtent();
        } else {
            _extent = MIMath.getLagerExtent(_extent, aGraphic.getShape().getExtent());
        }

        return istrue;
    }

    /**
     * Inset a graphic
     *
     * @param index Index
     * @param aGraphic The graphic
     */
    @Override
    public void add(int index, Graphic aGraphic) {
        super.add(index, aGraphic);

        //Update extent
        if (this.size() == 1) {
            _extent = aGraphic.getShape().getExtent();
        } else {
            _extent = MIMath.getLagerExtent(_extent, aGraphic.getShape().getExtent());
        }
    }

    /**
     * Remove a graphic
     *
     * @param aGraphic The graphic
     * @return Boolean
     */
    @Override
    public boolean remove(Object aGraphic) {
        boolean istrue = super.remove(aGraphic);
        this.updateExtent();

        return istrue;
    }

    /**
     * Remove a graphic by index
     *
     * @param index The index
     * @return The removed graphic
     */
    @Override
    public Graphic remove(int index) {
        Graphic ag = super.remove(index);
        this.updateExtent();

        return ag;
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

        for (Graphic aGraphic : this) {
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
    // </editor-fold>
}
