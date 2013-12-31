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
package org.meteoinfo.projection;

import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.datum.Datum;
import org.osgeo.proj4j.proj.Projection;

/**
 *
 * @author Yaqiang Wang
 */
public class ProjectionInfo {
    // <editor-fold desc="Variables">

    private CoordinateReferenceSystem _crs;
    private ProjectionNames _projName;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public ProjectionInfo() {
    }

    /**
     * Constructor
     *
     * @param crs CoordinateReferenceSystem
     */
    public ProjectionInfo(CoordinateReferenceSystem crs) {
        _crs = crs;
        setProjectionName(_crs);
    }

    /**
     * Constructor
     *
     * @param proj4Str Proj4 string
     */
    public ProjectionInfo(String proj4Str) {
        CRSFactory crsFactory = new CRSFactory();
        proj4Str = proj4Str.replace("+", " +");
        proj4Str = proj4Str.trim();
        _crs = crsFactory.createFromParameters("custom", proj4Str);
        setProjectionName(_crs);
    }

    /**
     * Constructor
     *
     * @param name Name
     * @param params Parameters
     * @param datum Datum
     * @param proj Projection
     */
    public ProjectionInfo(String name, String[] params, Datum datum, Projection proj) {
        _crs = new CoordinateReferenceSystem(name, params, datum, proj);
        setProjectionName(_crs);
    }

    private void setProjectionName(CoordinateReferenceSystem crs) {
        Projection proj = crs.getProjection();
        if (proj.toString().equalsIgnoreCase("Stereographic Azimuthal")) {
            if (proj.getProjectionLatitudeDegrees() == 90) {
                _projName = ProjectionNames.North_Polar_Stereographic_Azimuthal;
            } else if (proj.getProjectionLatitudeDegrees() == -90) {
                _projName = ProjectionNames.South_Polar_Stereographic_Azimuthal;
            } else {
                _projName = ProjectionNames.Oblique_Stereographic_Alternative;
            }
        } else {
            String nameStr = proj.toString().replace(" ", "_");
            _projName = ProjectionNames.valueOf(nameStr);
        }
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get CoordinateReferenceSystem
     *
     * @return CoordinateReferenceSystem
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return _crs;
    }

    /**
     * Get projection name
     *
     * @return Projection name
     */
    public ProjectionNames getProjectionName() {
        return _projName;
    }
    
    /**
     * Get if is Lon/Lat projection
     * 
     * @return Boolean
     */
    public boolean isLonLat(){
        return _projName == ProjectionNames.LongLat;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Get proj4 string
     *
     * @return Proj4 string
     */
    public String toProj4String() {
        return _crs.getParameterString();
    }
    
    /**
     * Get Esri projection string
     * @return Esri projection string
     */
    public String toEsriString(){
        return _crs.toEsriString();
    }
    
    /**
     * Determine if the projection is same with another projection
     * 
     * @param projInfo Projection info
     * @return Boolean
     */
    public boolean equals(ProjectionInfo projInfo){
        if (this._projName == ProjectionNames.LongLat && projInfo._projName == ProjectionNames.LongLat)
            return true;
        else {
            if (this.toProj4String().equals(projInfo.toProj4String()))
                return true;
            else
                return false;
        }            
    }

    /**
     * Calculate scale factor from standard parallel
     *
     * @param stP Standard parallel
     * @return Scale factor
     */
    public static double calScaleFactorFromStandardParallel(double stP) {
        double e = 0.081819191;
        stP = Math.PI * stP / 180;
        double tF;
        if (stP > 0) {
            tF = Math.tan(Math.PI / 4.0 - stP / 2.0) * (Math.pow((1.0 + e * Math.sin(stP)) / (1.0 - e * Math.sin(stP)), e / 2.0));
        } else {
            tF = Math.tan(Math.PI / 4.0 + stP / 2.0) / (Math.pow((1.0 + e * Math.sin(stP)) / (1.0 - e * Math.sin(stP)), e / 2.0));
        }

        double mF = Math.cos(stP) / Math.pow(1.0 - e * e * Math.pow(Math.sin(stP), 2.0), 0.5);
        double k0 = mF * (Math.pow(Math.pow(1.0 + e, 1.0 + e) * Math.pow(1.0 - e, 1.0 - e), 0.5)) / (2.0 * tF);

        return k0;
    }
    // </editor-fold>
}
