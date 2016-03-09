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
package org.meteoinfo.geom;

import org.meteoinfo.geoprocess.GeoComputation;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Polygon class
 * 
 * @author Yaqiang Wang
 */
public class Polygon {
    // <editor-fold desc="Variables">

    private List<PointD> _outLine;
    private List<List<PointD>> _holeLines;
    private Extent _extent;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    public Polygon() {
        _outLine = new ArrayList<>();
        _holeLines = new ArrayList<>();
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get outLine
     * 
     * @return outLine point list
     */
    public List<PointD> getOutLine() {
        return _outLine;
    }

    /**
     * Set outLine point list
     * 
     * @param outLine outLine point list
     */
    public void setOutLine(List<PointD> outLine) {
        _outLine = outLine;
        _extent = MIMath.getPointsExtent(outLine);
    }

    /**
     * Get hole lines
     * 
     * @return hole lines
     */
    public List<List<PointD>> getHoleLines() {
        return _holeLines;
    }

    /**
     * Set hole lines
     * 
     * @param holeLines hole lines list
     */
    public void setHoleLines(List<List<PointD>> holeLines) {
        _holeLines = holeLines;
    }

    /**
     * Get extent
     * 
     * @return extent
     */
    public Extent getExtent() {
        return _extent;
    }

    /**
     * Get rings
     * 
     * @return Rings
     */
    public List<List<PointD>> getRings() {
        List<List<PointD>> rings = new ArrayList<>();
        rings.add(_outLine);
        if (hasHole()) {
            rings.addAll(_holeLines);
        }

        return rings;
    }

    /**
     * Determine if the polygon has hole
     * 
     * @return boolean
     */
    public boolean hasHole() {
        return (_holeLines.size() > 0);
    }

    /**
     * Get ring number - outline number + holeline number
     * 
     * @return ring number
     */
    public int getRingNumber() {
        return _holeLines.size() + 1;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Add a hole line
     * 
     * @param points point list
     */
    public void addHole(List<PointD> points) {
        if (GeoComputation.isClockwise(points)) {
            Collections.reverse(points);
        }
        _holeLines.add(points);
    }
    // </editor-fold>
}
