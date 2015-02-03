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
package org.meteoinfo.data.meteodata.netcdf;

import org.meteoinfo.data.GridData;
import org.meteoinfo.data.StationData;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.data.meteodata.Dimension;
import org.meteoinfo.data.meteodata.DimensionType;
import static org.meteoinfo.data.meteodata.DimensionType.T;
import static org.meteoinfo.data.meteodata.DimensionType.X;
import static org.meteoinfo.data.meteodata.DimensionType.Y;
import static org.meteoinfo.data.meteodata.DimensionType.Z;
import org.meteoinfo.data.meteodata.IGridDataInfo;
import org.meteoinfo.data.meteodata.IStationDataInfo;
import org.meteoinfo.data.meteodata.StationInfoData;
import org.meteoinfo.data.meteodata.StationModelData;
import org.meteoinfo.data.meteodata.Variable;
import static org.meteoinfo.data.meteodata.netcdf.TimeUnit.Day;
import static org.meteoinfo.data.meteodata.netcdf.TimeUnit.Hour;
import static org.meteoinfo.data.meteodata.netcdf.TimeUnit.Minute;
import static org.meteoinfo.data.meteodata.netcdf.TimeUnit.Month;
import static org.meteoinfo.data.meteodata.netcdf.TimeUnit.Second;
import static org.meteoinfo.data.meteodata.netcdf.TimeUnit.Year;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.MIMath;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meteoinfo.data.meteodata.MeteoDataType;
import org.meteoinfo.global.util.DateUtil;
import org.meteoinfo.projection.KnownCoordinateSystems;
import org.meteoinfo.projection.ProjectionInfo;
import org.meteoinfo.projection.Reproject;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author yaqiang
 */
public class NetCDFDataInfo extends DataInfo implements IGridDataInfo, IStationDataInfo {
    // <editor-fold desc="Variables">

    private String _fileTypeStr;
    private String _fileTypeId;
    private Conventions _convention = Conventions.CF;
    private List<ucar.nc2.Variable> _variables = new ArrayList<ucar.nc2.Variable>();
    private List<ucar.nc2.Dimension> _dimensions = new ArrayList<ucar.nc2.Dimension>();
    private List<Dimension> _miDims = new ArrayList<Dimension>();
    private List<ucar.nc2.Attribute> _gAtts = new ArrayList<ucar.nc2.Attribute>();
    private ucar.nc2.Variable _xVar = null;
    private ucar.nc2.Variable _yVar = null;
    private ucar.nc2.Variable _levelVar = null;
    private ucar.nc2.Variable _timeVar = null;
    private boolean _isHDFEOS = false;
    private boolean _isSWATH = false;
    private boolean _isPROFILE = false;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public NetCDFDataInfo() {
        this.setDataType(MeteoDataType.NetCDF);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get MeteoInfo dimensions
     *
     * @return MeteoInfo dimensions
     */
    @Override
    public List<Dimension> getDimensions() {
        return this._miDims;
    }

    /**
     * Get ucar nc2 dimensions
     *
     * @return Ucar nc2 dimensions
     */
    public List<ucar.nc2.Dimension> getNCDimensions() {
        return this._dimensions;
    }

    /**
     * Get global attributes
     *
     * @return Global attributes
     */
    public List<ucar.nc2.Attribute> getGlobalAttributes() {
        return this._gAtts;
    }

    /**
     * Get ucar nc2 variables
     *
     * @return Ucar nc2 variables
     */
    public List<ucar.nc2.Variable> getNCVariables() {
        return this._variables;
    }

    /**
     * Get file type identifer
     *
     * @return File type identifer
     */
    public String getFileTypeId() {
        return _fileTypeId;
    }

    /**
     * Get if is HDF EOS data
     *
     * @return Boolean
     */
    public boolean isHDFEOS() {
        return _isHDFEOS;
    }

    /**
     * Get if is SWATH
     *
     * @return Boolean
     */
    public boolean isSWATH() {
        if (_isSWATH || _isPROFILE) {
            return true;
        } else {
            return false;
        }
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    // <editor-fold desc="Read Data">
    @Override
    public void readDataInfo(String fileName) {
        this.setFileName(fileName);
        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(fileName);
            _fileTypeStr = ncfile.getFileTypeDescription();
            _fileTypeId = ncfile.getFileTypeId();
//            if (_fileTypeId.equals("GRIB2")){
//                ncfile.getIosp().
//            }
            
            //Read variables
            _variables = ncfile.getVariables();

            //Read dimensions
            _dimensions = ncfile.getDimensions();
            if (_fileTypeId.equals("HDF5-EOS") || _fileTypeId.equals("HDF4-EOS")) {
                _isHDFEOS = true;
                List<String> dimNames = new ArrayList<String>();
                for (ucar.nc2.Variable var : _variables){
                    for (ucar.nc2.Dimension dim : var.getDimensions()){
                        String dimName = dim.getShortName();
                        if (dimName == null)
                            continue;
                        if (!dimNames.contains(dimName))
                            dimNames.add(dimName);
                    }
                }
                for (ucar.nc2.Dimension dim : _dimensions) {
                    if (dim.getShortName().contains("_")) {
                        for (String dimName : dimNames){
                            if (dim.getShortName().contains(dimName))
                                dim.setShortName(dimName);
                        }
//                        String newName;
//                        //int idx = dim.getShortName().lastIndexOf("_");
//                        int idx = dim.getShortName().indexOf("Data_Fields_");
//                        if (idx >= 0) {
//                            newName = dim.getShortName().substring(idx + 12);
//                        } else {
//                            idx = dim.getShortName().lastIndexOf("_");
//                            newName = dim.getShortName().substring(idx + 1);
//                        }
//                        dim.setShortName(newName);
                    }
                }
            }
            _miDims = new ArrayList<Dimension>();
            for (ucar.nc2.Dimension dim : _dimensions) {
                Dimension ndim = new Dimension();
                ndim.setNCDimension(dim);
                ndim.setDimName(dim.getShortName());
                if (dim.getShortName().equals("nXtrack")) {
                    ndim.setDimType(DimensionType.Xtrack);
                }
                ndim.setDimLength(dim.getLength());
                _miDims.add(ndim);
            }

            //Read global attribute
            _gAtts = ncfile.getGlobalAttributes();
            String featureType = this.getGlobalAttStr("featureType");
            if (featureType.equals("SWATH")) {
                _isSWATH = true;
            } else if (featureType.equals("PROFILE")) {
                _isPROFILE = true;
            }

            //Get convention
            _convention = this.getConvention();            

            //Get projection
            this.getProjection();

            //Get dimensions values
            getDimensionValues(ncfile);

            //Get variables
            List<Variable> vars = new ArrayList<Variable>();
            //List<Dimension> coorDims = new ArrayList<Dimension>();
            for (ucar.nc2.Variable var : _variables) {
                Variable nvar = new Variable();
                nvar.setName(var.getShortName());
                //nvar.setCoorVar(var.isCoordinateVariable());
                nvar.setCoorVar(var.getRank() <= 1);
                if (_isSWATH || _isPROFILE)
                    nvar.setStation(true);                      
                
                for (ucar.nc2.Dimension dim : var.getDimensions()) {
                    //Dimension ndim = this.getCoordDimension(dim);
                    int idx = this.getDimensionIndex(dim);
                    if (idx >= 0) {
                        Dimension ndim = _miDims.get(idx);
                        nvar.addDimension(ndim);
                    }
//                    else {
//                        Dimension ndim = new Dimension(DimensionType.Other);
//                        ndim.setDimName(dim.getShortName());
//                        ndim.setDimLength(dim.getLength());
//                    }
                }

                vars.add(nvar);
            }
            this.setVariables(vars);

            //
            //getVariableLevels();
        } catch (IOException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (null != ncfile) {
                try {
                    ncfile.close();
                } catch (IOException ex) {
                    Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private int getDimensionIndex(ucar.nc2.Dimension dim) {
        String name2 = dim.getShortName();
        for (int i = 0; i < _dimensions.size(); i++) {
            ucar.nc2.Dimension idim = _dimensions.get(i);
            if (idim.getShortName().equals(name2)) {
                return i;
            }
        }

        for (int i = 0; i < _dimensions.size(); i++) {
            ucar.nc2.Dimension idim = _dimensions.get(i);
            if (idim.getLength() == (dim.getLength())) {
                String name1 = idim.getShortName();
                int len1 = name1.length();
                int len2 = name2.length();
                if (len1 > len2){
                    if (name1.substring(len1 - len2).equals(name2)) {
                        return i;
                    }
                }
            }
        }

        return -1;
    }

    /**
     * Find netCDF dimension by name
     *
     * @param dimName Dimension name
     * @return NetCDF dimension
     */
    public ucar.nc2.Dimension findNCDimension(String dimName) {
        for (ucar.nc2.Dimension dim : this._dimensions) {
            if (dim.getShortName().equalsIgnoreCase(dimName)) {
                return dim;
            }
        }

        return null;
    }

    /**
     * Find dimension by name
     *
     * @param dimName Dimension name
     * @return Dimension
     */
    public Dimension findDimension(String dimName) {
        for (Dimension dim : this._miDims) {
            if (dim.getDimName().equalsIgnoreCase(dimName)) {
                return dim;
            }
        }
        return null;
    }

    private ucar.nc2.Attribute findGlobalAttribute(String attName) {
        for (ucar.nc2.Attribute att : this._gAtts) {
            if (att.getShortName().equalsIgnoreCase(attName)) {
                return att;
            }
        }

        return null;
    }

    private Dimension findCoordDimension(ucar.nc2.Dimension dim) {
        if (dim.getShortName().equals(this.getXDimension().getDimName())) {
            return this.getXDimension();
        } else if (dim.getShortName().equals(this.getYDimension().getDimName())) {
            return this.getYDimension();
        } else if (dim.getShortName().equals(this.getZDimension().getDimName())) {
            return this.getZDimension();
        } else if (dim.getShortName().equals(this.getTimeDimension().getDimName())) {
            return this.getTimeDimension();
        } else {
            return null;
        }
    }

    private Conventions getConvention() {
        Conventions convention = _convention;
        boolean isIOAPI = false;
        boolean isWRFOUT = false;
        //List<String> WRFStrings = new ArrayList<String>();

        for (ucar.nc2.Attribute aAtts : _gAtts) {
            if (aAtts.getShortName().toLowerCase().equals("ioapi_version")) {
                isIOAPI = true;
                break;
            }
            if (aAtts.getShortName().toUpperCase().equals("TITLE")) {
                String title = aAtts.getStringValue();
                if (title.toUpperCase().contains("OUTPUT FROM WRF") || title.toUpperCase().contains("OUTPUT FROM GEOGRID")
                        || title.toUpperCase().contains("OUTPUT FROM GRIDGEN") || title.toUpperCase().contains("OUTPUT FROM METGRID")) {
                    isWRFOUT = true;
                    break;
                }
                if (title.toUpperCase().contains("OUTPUT FROM") && title.toUpperCase().contains("WRF")) {
                    isWRFOUT = true;
                    break;
                }
            }
            if (aAtts.getShortName().toUpperCase().equals("WEST-EAST_GRID_DIMENSION")) {
                isWRFOUT = true;
                break;
            }
        }

        if (isIOAPI) {
            convention = Conventions.IOAPI;
        }

        if (isWRFOUT) {
            convention = Conventions.WRFOUT;
        }

        return convention;
    }

    private void getProjection() {
        String projStr = this.getProjectionInfo().toProj4String();
        switch (_convention) {
            case CF:
                projStr = getProjection_CF();
                break;
            case IOAPI:
                projStr = getProjection_IOAPI();
                break;
            case WRFOUT:
                projStr = getProjection_WRFOUT();
                break;
        }
        if (!projStr.equals(this.getProjectionInfo().toProj4String())) {
            this.setProjectionInfo(new ProjectionInfo(projStr));
        }
    }

    private String getProjection_CF() {
        String projStr = this.getProjectionInfo().toProj4String();
        if (this._isHDFEOS) {
            ucar.nc2.Variable pVar = null;
            for (ucar.nc2.Variable aVar : _variables) {
                if (aVar.getShortName().equals("_HDFEOS_CRS")) {
                    pVar = aVar;
                }
            }

            if (pVar != null) {
                ucar.nc2.Attribute projAtt = pVar.findAttributeIgnoreCase("Projection");
                String proj = projAtt.getStringValue();
                if (proj.contains("GCTP_GEO")) {
                } else {
                    ucar.nc2.Attribute paraAtt = pVar.findAttributeIgnoreCase("ProjParams");
                    Array params = paraAtt.getValues();
                    if (proj.contains("GCTP_SNSOID")) {
                        projStr = "+proj=sinu"
                                + "+lon_0=" + params.getObject(4).toString();
                    } else if (proj.contains("GCTP_CEA")) {
                        projStr = "+proj=cea"
                                + "+lon_0=" + params.getObject(4).toString()
                                + "+lat_ts=" + String.valueOf(params.getDouble(5) / 1000000);
                        //+ "+x_0=" + params.getObject(6).toString()
                        //+ "+y_0=" + params.getObject(7).toString();                        
                    }
                }

                ucar.nc2.Attribute ulAtt = pVar.findAttributeIgnoreCase("UpperLeftPointMtrs");
                ucar.nc2.Attribute lrAtt = pVar.findAttributeIgnoreCase("LowerRightMtrs");
                double xmin = ulAtt.getValues().getDouble(0);
                double ymax = ulAtt.getValues().getDouble(1);
                double xmax = lrAtt.getValues().getDouble(0);
                double ymin = lrAtt.getValues().getDouble(1);
                if (proj.contains("GCTP_GEO")) {
                    if (Math.abs(xmax) > 1000000) {
                        xmin = xmin / 1000000;
                        xmax = xmax / 1000000;
                        ymin = ymin / 1000000;
                        ymax = ymax / 1000000;
                    }
                }
                if (ymin > ymax) {
                    double temp = ymax;
                    ymax = ymin;
                    ymin = temp;
                    if (this._fileTypeId.equals("HDF5-EOS")) {
                        this.setYReverse(true);
                    }
                } else {
                    if (!this._fileTypeId.equals("HDF5-EOS")) {
                        this.setYReverse(true);
                    }
                }

                Dimension xDim = this.findDimension("XDim");
                Dimension yDim = this.findDimension("YDim");
                int xnum = xDim.getDimLength();
                int ynum = yDim.getDimLength();
                double xdelt = (xmax - xmin) / (xnum - 1);
                double ydelt = (ymax - ymin) / (ynum - 1);
                double[] X = new double[xnum];
                for (int i = 0; i < xnum; i++) {
                    X[i] = xmin + xdelt * i;
                }
                xDim.setDimType(DimensionType.X);
                xDim.setValues(X);
                this.setXDimension(xDim);
                double[] Y = new double[ynum];
                for (int i = 0; i < ynum; i++) {
                    Y[i] = ymin + ydelt * i;
                }
                yDim.setDimType(DimensionType.Y);
                yDim.setValues(Y);
                this.setYDimension(yDim);
            }
        } else {
            ucar.nc2.Variable pVar = null;
            int pvIdx = -1;
            for (ucar.nc2.Variable aVarS : _variables) {
                ucar.nc2.Attribute att = aVarS.findAttribute("grid_mapping_name");
                if (att != null) {
                    pVar = aVarS;
                    pvIdx = aVarS.getAttributes().indexOf(att);
                    break;
                }
            }

            if (pVar != null) {
                ucar.nc2.Attribute pAtt = pVar.getAttributes().get(pvIdx);
                String attStr = pAtt.getStringValue();
                if (attStr.equals("albers_conical_equal_area")) {
                    //Two standard parallels condition need to be considered  
                    Array values = pVar.findAttribute("standard_parallel").getValues();
                    String sp1 = String.valueOf(values.getDouble(0));
                    String sp2 = "";
                    if (values.getSize() == 2) {
                        sp2 = String.valueOf(values.getDouble(1));
                    }
                    projStr = "+proj=aea"
                            + "+lat_1=" + sp1;
                    if (!sp2.isEmpty()) {
                        projStr += "+lat_2=" + sp2;
                    }

                    projStr += "+lon_0=" + pVar.findAttribute("longitude_of_central_meridian").getValue(0).toString()
                            + "+lat_0=" + pVar.findAttribute("latitude_of_projection_origin").getValue(0).toString();
                    if (pVar.findAttribute("false_easting") != null) {
                        projStr += "+x_0=" + pVar.findAttribute("false_easting").getValue(0).toString();
                    }
                    if (pVar.findAttribute("false_northing") != null) {
                        projStr += "+y_0=" + pVar.findAttribute("false_northing").getValue(0).toString();
                    }
                } else if (attStr.equals("azimuthal_equidistant")) {
                    projStr = "+proj=aeqd"
                            + "+lon_0=" + pVar.findAttribute("longitude_of_projection_origin").getValue(0).toString()
                            + "+lat_0=" + pVar.findAttribute("latitude_of_projection_origin").getValue(0).toString();
                    if (pVar.findAttribute("false_easting") != null) {
                        projStr += "+x_0=" + pVar.findAttribute("false_easting").getValue(0).toString();
                    }
                    if (pVar.findAttribute("false_northing") != null) {
                        projStr += "+y_0=" + pVar.findAttribute("false_northing").getValue(0).toString();
                    }
                } else if (attStr.equals("lambert_azimuthal_equal_area")) {
                    projStr = "+proj=laea"
                            + "+lon_0=" + pVar.findAttribute("longitude_of_projection_origin").getValue(0).toString()
                            + "+lat_0=" + pVar.findAttribute("latitude_of_projection_origin").getValue(0).toString();
                    if (pVar.findAttribute("false_easting") != null) {
                        projStr += "+x_0=" + pVar.findAttribute("false_easting").getValue(0).toString();
                    }
                    if (pVar.findAttribute("false_northing") != null) {
                        projStr += "+y_0=" + pVar.findAttribute("false_northing").getValue(0).toString();
                    }
                } else if (attStr.equals("lambert_conformal_conic")) {
                    //Two standard parallels condition need to be considered
                    Array values = pVar.findAttribute("standard_parallel").getValues();
                    String sp1 = String.valueOf(values.getDouble(0));
                    String sp2 = "";
                    if (values.getSize() == 2) {
                        sp2 = String.valueOf(values.getDouble(1));
                    }
                    projStr = "+proj=lcc"
                            + "+lat_1=" + sp1;
                    if (!sp2.isEmpty()) {
                        projStr += "+lat_2=" + sp2;
                    }

                    projStr += "+lon_0=" + pVar.findAttribute("longitude_of_central_meridian").getValue(0).toString()
                            + "+lat_0=" + pVar.findAttribute("latitude_of_projection_origin").getValue(0).toString();
                    if (pVar.findAttribute("false_easting") != null) {
                        projStr += "+x_0=" + pVar.findAttribute("false_easting").getValue(0).toString();
                    }
                    if (pVar.findAttribute("false_northing") != null) {
                        projStr += "+y_0=" + pVar.findAttribute("false_northing").getValue(0).toString();
                    }
                } else if (attStr.equals("lambert_cylindrical_equal_area")) {
                    projStr = "+proj=cea"
                            + "+lon_0=" + pVar.findAttribute("longitude_of_central_meridian").getValue(0).toString();

                    if (pVar.findAttribute("standard_parallel") != null) {
                        projStr += "+lat_ts=" + pVar.findAttribute("standard_parallel").getValue(0).toString();
                    } else if (pVar.findAttribute("scale_factor_at_projection_origin") != null) {
                        projStr += "+k_0=" + pVar.findAttribute("scale_factor_at_projection_origin").getValue(0).toString();
                    }

                    if (pVar.findAttribute("false_easting") != null) {
                        projStr += "+x_0=" + pVar.findAttribute("false_easting").getValue(0).toString();
                    }
                    if (pVar.findAttribute("false_northing") != null) {
                        projStr += "+y_0=" + pVar.findAttribute("false_northing").getValue(0).toString();
                    }
                } else if (attStr.equals("mercator")) {
                    projStr = "+proj=merc"
                            + "+lon_0=" + pVar.findAttribute("longitude_of_projection_origin").getValue(0).toString();

                    if (pVar.findAttribute("standard_parallel") != null) {
                        projStr += "+lat_ts=" + pVar.findAttribute("standard_parallel").getValue(0).toString();
                    } else if (pVar.findAttribute("scale_factor_at_projection_origin") != null) {
                        projStr += "+k_0=" + pVar.findAttribute("scale_factor_at_projection_origin").getValue(0).toString();
                    }

                    if (pVar.findAttribute("false_easting") != null) {
                        projStr += "+x_0=" + pVar.findAttribute("false_easting").getValue(0).toString();
                    }
                    if (pVar.findAttribute("false_northing") != null) {
                        projStr += "+y_0=" + pVar.findAttribute("false_northing").getValue(0).toString();
                    }
                } else if (attStr.equals("orthographic")) {
                    projStr = "+proj=ortho"
                            + "+lon_0=" + pVar.findAttribute("longitude_of_projection_origin").getValue(0).toString()
                            + "+lat_0=" + pVar.findAttribute("latitude_of_projection_origin").getValue(0).toString();
                    if (pVar.findAttribute("false_easting") != null) {
                        projStr += "+x_0=" + pVar.findAttribute("false_easting").getValue(0).toString();
                    }
                    if (pVar.findAttribute("false_northing") != null) {
                        projStr += "+y_0=" + pVar.findAttribute("false_northing").getValue(0).toString();
                    }
                } else if (attStr.equals("polar_stereographic")) {
                    projStr = "+proj=stere"
                            + "+lon_0=" + pVar.findAttribute("longitude_of_projection_origin").getValue(0).toString()
                            + "+lat_0=" + pVar.findAttribute("latitude_of_projection_origin").getValue(0).toString();

                    if (pVar.findAttribute("standard_parallel") != null) {
                        projStr += "+lat_ts=" + pVar.findAttribute("standard_parallel").getValue(0).toString();
                    }
                    if (pVar.findAttribute("scaling_factor") != null) {
                        projStr += "+k=" + pVar.findAttribute("scaling_factor").getValue(0).toString();
                    } else if (pVar.findAttribute("standard_parallel") != null) {
                        String stPs = pVar.findAttribute("standard_parallel").getValue(0).toString();
                        //projStr += "+lat_ts=" + stPs;                                    
                        double k0 = ProjectionInfo.calScaleFactorFromStandardParallel(Double.parseDouble(stPs));
                        projStr += "+k=" + String.format("%1$.2f", k0);
                    } else if (pVar.findAttribute("scale_factor_at_projection_origin") != null) {
                        projStr += "+k_0=" + pVar.findAttribute("scale_factor_at_projection_origin").getValue(0).toString();
                    }

                    if (pVar.findAttribute("false_easting") != null) {
                        projStr += "+x_0=" + pVar.findAttribute("false_easting").getValue(0).toString();
                    }
                    if (pVar.findAttribute("false_northing") != null) {
                        projStr += "+y_0=" + pVar.findAttribute("false_northing").getValue(0).toString();
                    }
                } else if (attStr.equals("rotated_latitude_longitude")) {
                } else if (attStr.equals("stereographic")) {
                    projStr = "+proj=stere"
                            + "+lon_0=" + pVar.findAttribute("longitude_of_projection_origin").getValue(0).toString()
                            + "+lat_0=" + pVar.findAttribute("latitude_of_projection_origin").getValue(0).toString()
                            + "+k_0=" + pVar.findAttribute("scale_factor_at_projection_origin").getValue(0).toString();
                    if (pVar.findAttribute("false_easting") != null) {
                        projStr += "+x_0=" + pVar.findAttribute("false_easting").getValue(0).toString();
                    }
                    if (pVar.findAttribute("false_northing") != null) {
                        projStr += "+y_0=" + pVar.findAttribute("false_northing").getValue(0).toString();
                    }
                } else if (attStr.equals("transverse_mercator")) {
                    projStr = "+proj=tmerc"
                            + "+lon_0=" + pVar.findAttribute("longitude_of_central_meridian").getValue(0).toString()
                            + "+lat_0=" + pVar.findAttribute("latitude_of_projection_origin").getValue(0).toString()
                            + "+k_0=" + pVar.findAttribute("scale_factor_at_central_meridian").getValue(0).toString();
                    if (pVar.findAttribute("false_easting") != null) {
                        projStr += "+x_0=" + pVar.findAttribute("false_easting").getValue(0).toString();
                    }
                    if (pVar.findAttribute("false_northing") != null) {
                        projStr += "+y_0=" + pVar.findAttribute("false_northing").getValue(0).toString();
                    }
                } else if (attStr.equals("vertical_perspective")) {
                    projStr = "+proj=geos"
                            + "+lon_0=" + pVar.findAttribute("longitude_of_projection_origin").getValue(0).toString()
                            + "+lat_0=" + pVar.findAttribute("latitude_of_projection_origin").getValue(0).toString()
                            + "+h=" + pVar.findAttribute("perspective_point_height").getValue(0).toString();
                    if (pVar.findAttribute("false_easting") != null) {
                        projStr += "+x_0=" + pVar.findAttribute("false_easting").getValue(0).toString();
                    }
                    if (pVar.findAttribute("false_northing") != null) {
                        projStr += "+y_0=" + pVar.findAttribute("false_northing").getValue(0).toString();
                    }
                }
            }
        }

        return projStr;
    }

    private String getProjection_IOAPI() {
        String projStr = this.getProjectionInfo().toProj4String();
        int gridType = Integer.parseInt(getGlobalAttStr("GDTYP"));
        switch (gridType) {
            case 1:    //Lat-Lon
                break;
            case 2:    //Lambert conformal conic
                projStr = "+proj=lcc"
                        + "+lon_0=" + getGlobalAttStr("P_GAM")
                        + "+lat_0=" + getGlobalAttStr("YCENT")
                        + "+lat_1=" + getGlobalAttStr("P_ALP")
                        + "+lat_2=" + getGlobalAttStr("P_BET");
                break;
            case 3:    //General Mercator (hotine oblique mercator)
                projStr = "+proj=omerc"
                        + "+lat_0=" + getGlobalAttStr("P_ALP")
                        + "+lonc=" + getGlobalAttStr("P_BET")
                        + "+alpha=" + getGlobalAttStr("P_GAM");
                break;
            case 4:    //tangent stereographic
                projStr = "+proj=stere"
                        + "+lon_0=" + getGlobalAttStr("P_BET")
                        + "+lat_0=" + getGlobalAttStr("P_ALP");
                break;
            case 5:    //UTM
                projStr = "+proj=utm"
                        + "+zone=" + getGlobalAttStr("P_ALP");
                break;
            case 6:    //polar secant stereographic
                String lat0 = "90";
                if (getGlobalAttStr("P_ALP").substring(0, 1).equals("-")) {
                    lat0 = "-90";
                }
                projStr = "+proj=stere"
                        + "+lon_0=" + getGlobalAttStr("P_GAM")
                        + "+lat_0=" + lat0;
                String stPs = getGlobalAttStr("P_BET");
                //projStr += "+lat_ts=" + stPs;                                    
                double k0 = ProjectionInfo.calScaleFactorFromStandardParallel(Double.parseDouble(stPs));
                projStr += "+k=" + String.valueOf(k0);
                break;
            case 7:    //Equatorial Mercator
                projStr = "+proj=merc"
                        + "+lat_ts=" + getGlobalAttStr("P_ALP")
                        + "+lon_0=" + getGlobalAttStr("P_GAM");
                break;
            case 8:    //Transverse Mercator 
                projStr = "+proj=tmerc"
                        + "+lon_0=" + getGlobalAttStr("P_GAM")
                        + "+lat_0=" + getGlobalAttStr("P_ALP");
                break;
            case 9:    //Albers Equal-Area Conic
                projStr = "+proj=aea"
                        + "+lat_1=" + getGlobalAttStr("P_ALP")
                        + "+lat_2=" + getGlobalAttStr("P_BET")
                        + "+lat_0=" + getGlobalAttStr("YCENT")
                        + "+lon_0=" + getGlobalAttStr("P_GAM");
                break;
            case 10:    //Lambert Azimuthal Equal-Area
                projStr = "+proj=laea"
                        + "+lat_0=" + getGlobalAttStr("P_ALP")
                        + "+lon_0=" + getGlobalAttStr("P_GAM");
                break;
        }

        return projStr;
    }

    private String getProjection_WRFOUT() {
        String projStr = this.getProjectionInfo().toProj4String();
        int mapProj = Integer.parseInt(getGlobalAttStr("MAP_PROJ"));
        switch (mapProj) {
            case 1:    //Lambert conformal
                projStr = "+proj=lcc"
                        + "+lon_0=" + getGlobalAttStr("STAND_LON")
                        + "+lat_0=" + getGlobalAttStr("CEN_LAT")
                        + "+lat_1=" + getGlobalAttStr("TRUELAT1")
                        + "+lat_2=" + getGlobalAttStr("TRUELAT2");
                break;
            case 2:    //Polar Stereographic
                String lat0 = "90";
                if (getGlobalAttStr("POLE_LAT").substring(0, 1).equals("-")) {
                    lat0 = "-90";
                }
                projStr = "+proj=stere"
                        + "+lon_0=" + getGlobalAttStr("STAND_LON")
                        + "+lat_0=" + lat0;
                String stPs = getGlobalAttStr("CEN_LAT");
                //projStr += "+lat_ts=" + stPs;                                    
                double k0 = ProjectionInfo.calScaleFactorFromStandardParallel(Double.parseDouble(stPs));
                projStr += "+k=" + String.valueOf(k0);
                break;
            case 3:    //Mercator
                projStr = "+proj=merc"
                        + "+lat_ts=" + getGlobalAttStr("CEN_LAT")
                        + "+lon_0=" + getGlobalAttStr("STAND_LON");
                break;
        }

        return projStr;
    }

    private String getGlobalAttStr(String attName) {
        String attStr = "";
        for (ucar.nc2.Attribute aAttS : this._gAtts) {
            if (aAttS.getShortName().equals(attName)) {
                attStr = aAttS.getValue(0).toString();
                break;
            }
        }

        return attStr;
    }

    private List<Date> getTimes(ucar.nc2.Variable aVar, double[] values) {
        //Get start time
        String unitsStr;
        int i;
        List<Date> times = new ArrayList<Date>();
        ucar.nc2.Attribute unitAtt = aVar.findAttribute("units");
        if (unitAtt == null) {
            Calendar cal = Calendar.getInstance();
            cal.set(1985, 0, 1, 0, 0);
            Date sTime = cal.getTime();
            for (double v : values) {
                cal.add(Calendar.HOUR, (int) v);
                times.add(cal.getTime());
                cal.setTime(sTime);
            }
            return times;
        }

        unitsStr = unitAtt.getStringValue();
        if (unitsStr.contains("as")) {
            //Get data time
            double[] DTimes = values;
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            for (i = 0; i < DTimes.length; i++) {
                String md = String.valueOf((int) DTimes[i]);
                if (md.length() <= 3) {
                    md = "0" + md;
                }
                try {
                    //times.Add(DateTime.ParseExact("2001" + md, "yyyyMMdd", null));
                    times.add(format.parse(md));
                } catch (ParseException ex) {
                    Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            Calendar cal = Calendar.getInstance();
            TimeUnit aTU;
            Date sTime = new Date();
            if (unitsStr.equalsIgnoreCase("month")) {
                aTU = TimeUnit.Month;
                cal.setTime(sTime);
                cal.set(cal.get(Calendar.YEAR), 0, 1, 0, 0, 0);
                sTime = cal.getTime();
            } else {
                aTU = this.getTimeUnit(unitsStr);
                sTime = this.getStartTime(unitsStr);
                cal.setTime(sTime);
            }
            //getPSDTimeInfo(unitsStr, sTime, aTU);                        

            //Get data time
            double[] DTimes = values;
            for (i = 0; i < values.length; i++) {
                switch (aTU) {
                    case Year:
                        cal.add(Calendar.YEAR, (int) DTimes[i]);
                        times.add(cal.getTime());
                        break;
                    case Month:
                        if (unitsStr.equalsIgnoreCase("month")) {
                            cal.add(Calendar.MONTH, (int) DTimes[i] - 1);
                        } else {
                            cal.add(Calendar.MONTH, (int) DTimes[i]);
                        }
                        times.add(cal.getTime());
                        break;
                    case Day:
                        //cal.add(Calendar.DAY_OF_YEAR, (int) DTimes[i]);                        
                        //times.add(cal.getTime());
                        times.add(DateUtil.addDays(sTime, (float) DTimes[i]));
                        break;
                    case Hour:
                        if (cal.get(Calendar.YEAR) == 1 && cal.get(Calendar.MONTH) == 1
                                && cal.get(Calendar.DAY_OF_MONTH) == 1 && DTimes[i] > 48) {
                            cal.add(Calendar.HOUR, (int) DTimes[i] - 48);
                            times.add(cal.getTime());
                        } else {
                            cal.add(Calendar.HOUR, (int) DTimes[i]);
                            times.add(cal.getTime());
                        }
                        break;
                    case Minute:
                        cal.add(Calendar.MINUTE, (int) DTimes[i]);
                        times.add(cal.getTime());
                        break;
                    case Second:
                        cal.add(Calendar.SECOND, (int) DTimes[i]);
                        times.add(cal.getTime());
                        break;
                }
                cal.setTime(sTime);
            }
        }

        return times;
    }

    private boolean getDimensionValues(NetcdfFile ncfile) throws IOException, ParseException {
        switch (_convention) {
            case CF:
                if (this._isHDFEOS) {
                    getDimValues_HDFEOS_SWATH();
                } else {
                    getDimValues_CF();
                    if (this.getXDimension() == null || this.getYDimension() == null) {
                        if (this.findNCVariable("Longitude") != null && this.findNCVariable("Latitude") != null) {
                            this._isSWATH = true;
                        }
                    }
                }
                break;
            case IOAPI:
                getDimValues_IOAPI();
                break;
            case WRFOUT:
                getDimValues_WRF(ncfile);
                break;
            default:
                return false;
        }

        return true;
    }

    private void getDimValues_HDFEOS_SWATH() throws IOException {
        if (this._isSWATH || this._isPROFILE) {
            ucar.nc2.Variable var = this.findNCVariable("Pressure");
            if (var != null) {
                Array darray = var.read();
                int n = (int) darray.getSize();
                double[] values = new double[n];
                for (int i = 0; i < n; i++) {
                    values[i] = darray.getDouble(i);
                }
                Dimension zDim = this.findDimension(var.getDimension(0).getShortName());
                zDim.setDimType(DimensionType.Z);
                zDim.setValues(values);
                this.setZDimension(zDim);
            } else {
                //var = this.getNCVariable("")
            }

            var = this.findNCVariable("Time");
            if (var != null) {
                Array darray = var.read();
//                int n = (int) darray.getSize();
//                double[] values = new double[n];
//                for (int i = 0; i < n; i++) {
//                    values[i] = darray.getDouble(i);
//                }
//                List<Date> times = this.getTimes(var, values);
//                List<Double> ts = new ArrayList<Double>();
//                for (Date t : times) {
//                    ts.add(DataConvert.toOADate(t));
//                }

                int n = 1;
                double[] values = new double[n];
                for (int i = 0; i < n; i++) {
                    values[i] = darray.getDouble(i);
                }
                List<Date> times = this.getTimes(var, values);
                List<Double> ts = new ArrayList<Double>();
                for (Date t : times) {
                    ts.add(DateUtil.toOADate(t));
                }

                Dimension tDim = this.findDimension(var.getDimension(0).getShortName());
                if (tDim != null) {
                    tDim.setDimType(DimensionType.T);
                    tDim.setValues(ts);
                    this.setTimeDimension(tDim);
                }
            }
        }
    }

    private void getDimValues_CF() throws IOException {
        for (ucar.nc2.Variable var : _variables) {
            if (var.getRank() == 1) {
                if (var.getDataType() == DataType.STRING)
                    continue;
                
                int idx = this.getDimensionIndex(var.getDimension(0));
                if (idx == -1) {
                    continue;
                }
                Dimension dim = this._miDims.get(idx);
                if (dim.getDimType() != DimensionType.Other) {
                    continue;
                }

                DimensionType dimType = getDimType(var);
                dim.setDimType(dimType);
                Array darray = var.read();                
                double[] values = new double[(int) darray.getSize()];
                BigDecimal b;
                for (int i = 0; i < values.length; i++) {
                    b = new BigDecimal(Float.toString(darray.getFloat(i)));
                    values[i] = b.doubleValue();
                }
                //aDim.setValues(values);
                switch (dimType) {
                    case X:
                        double[] X = values;
                        if (X[0] > X[1]) {
                            MIMath.arrayReverse(X);
                            this.setXReverse(true);
                        }
                        double XDelt = X[1] - X[0];
                        if (this.getProjectionInfo().isLonLat()) {
                            if (X[X.length - 1] + XDelt
                                    - X[0] == 360) {
                                this.setGlobal(true);
                            }
                        } else {
                            ucar.nc2.Attribute unitAtt = var.findAttribute("units");
                            if (unitAtt != null) {
                                if (unitAtt.getStringValue().trim().toLowerCase().equals("km")) {
                                    for (int i = 0; i < X.length; i++) {
                                        X[i] = X[i] * 1000;
                                    }
                                }
                            }
                        }
                        dim.setValues(X);
                        this.setXDimension(dim);
                        break;
                    case Y:
                        double[] Y = values;
                        if (Y[0] > Y[1]) {
                            MIMath.arrayReverse(Y);
                            this.setYReverse(true);
                        }
                        if (!this.getProjectionInfo().isLonLat()) {
                            ucar.nc2.Attribute unitAtt = var.findAttribute("units");
                            if (unitAtt != null) {
                                if (unitAtt.getStringValue().trim().toLowerCase().equals("km")) {
                                    for (int i = 0; i < Y.length; i++) {
                                        Y[i] = Y[i] * 1000;
                                    }
                                }
                            }
                        }
                        dim.setValues(Y);
                        this.setYDimension(dim);
                        break;
                    case Z:
                        double[] levels = values;
                        dim.setValues(levels);
                        this.setZDimension(dim);
                        break;
                    case T:
                        List<Date> times = this.getTimes(var, values);
                        List<Double> ts = new ArrayList<Double>();
                        for (Date t : times) {
                            ts.add(DateUtil.toOADate(t));
                        }
                        dim.setValues(ts);
                        this.setTimeDimension(dim);
                        break;
                    default:
                        dim.setValues(values);
                        break;
                }
            }
        }
    }

    private void getDimValues_CF_old() throws IOException {
        List<ucar.nc2.Variable> oneDimVars = new ArrayList<ucar.nc2.Variable>();
        for (ucar.nc2.Variable aVar : _variables) {
            if (aVar.getRank() == 1) {
                oneDimVars.add(aVar);
            }
        }

        for (Dimension aDim : _miDims) {
            boolean isFind = false;
            ucar.nc2.Variable aVar = null;
            for (ucar.nc2.Variable var : oneDimVars) {
                if (aDim.getDimName().equals(var.getShortName())) {
                    isFind = true;
                    aVar = var;
                    break;
                }
            }

            if (!isFind) {
                for (ucar.nc2.Variable var : oneDimVars) {
                    if (aDim.getDimName().equals(var.getDimensions().get(0).getShortName())) {
                        isFind = true;
                        aVar = var;
                        break;
                    }
                }
            }

            if (isFind) {
                //aVar.setCoorVar(true);
                DimensionType dimType = getDimType(aVar);
                aDim.setDimType(dimType);
                Array darray = aVar.read();
                double[] values = new double[(int) darray.getSize()];
                BigDecimal b;
                for (int i = 0; i < values.length; i++) {
                    b = new BigDecimal(Float.toString(darray.getFloat(i)));
                    values[i] = b.doubleValue();
                }
                //aDim.setValues(values);
                switch (dimType) {
                    case X:
                        double[] X = values;
                        if (X[0] > X[1]) {
                            MIMath.arrayReverse(X);
                            this.setXReverse(true);
                        }
                        double XDelt = X[1] - X[0];
                        if (this.getProjectionInfo().isLonLat()) {
                            if (X[X.length - 1] + XDelt
                                    - X[0] == 360) {
                                this.setGlobal(true);
                            }
                        } else {
                            ucar.nc2.Attribute unitAtt = aVar.findAttribute("units");
                            if (unitAtt != null) {
                                if (unitAtt.getStringValue().trim().toLowerCase().equals("km")) {
                                    for (int i = 0; i < X.length; i++) {
                                        X[i] = X[i] * 1000;
                                    }
                                }
                            }
                        }
                        aDim.setValues(X);
                        this.setXDimension(aDim);
                        break;
                    case Y:
                        double[] Y = values;
                        if (Y[0] > Y[1]) {
                            MIMath.arrayReverse(Y);
                            this.setYReverse(true);
                        }
                        if (!this.getProjectionInfo().isLonLat()) {
                            ucar.nc2.Attribute unitAtt = aVar.findAttribute("units");
                            if (unitAtt != null) {
                                if (unitAtt.getStringValue().trim().toLowerCase().equals("km")) {
                                    for (int i = 0; i < Y.length; i++) {
                                        Y[i] = Y[i] * 1000;
                                    }
                                }
                            }
                        }
                        aDim.setValues(Y);
                        this.setYDimension(aDim);
                        break;
                    case Z:
                        double[] levels = values;
                        aDim.setValues(levels);
                        this.setZDimension(aDim);
                        break;
                    case T:
                        List<Date> times = this.getTimes(aVar, values);
                        List<Double> ts = new ArrayList<Double>();
                        for (Date t : times) {
                            ts.add(DateUtil.toOADate(t));
                        }
                        aDim.setValues(ts);
                        this.setTimeDimension(aDim);
                        break;
                    default:
                        aDim.setValues(values);
                        break;
                }
            }
        }
    }

    private void getDimValues_IOAPI() {
        int i;

        //Get times
        String sDateStr = getGlobalAttStr("SDATE");
        String sTimeStr = getGlobalAttStr("STIME");
        int len = sTimeStr.length();
        Calendar cal = Calendar.getInstance();
        cal.set(Integer.parseInt(sDateStr.substring(0, 4)), 0, 1, 0, 0, 0);
        cal.add(Calendar.DAY_OF_YEAR, Integer.parseInt(sDateStr.substring(4)) - 1);
        if (sTimeStr.length() <= 2) {
            cal.add(Calendar.SECOND, Integer.parseInt(sTimeStr));
        } else if (sTimeStr.length() <= 4) {
            cal.add(Calendar.MINUTE, Integer.parseInt(sTimeStr.substring(0, len - 2)));
            cal.add(Calendar.SECOND, Integer.parseInt(sTimeStr.substring(len - 2)));
        } else {
            cal.add(Calendar.HOUR, Integer.parseInt(sTimeStr.substring(0, len - 4)));
            cal.add(Calendar.MINUTE, Integer.parseInt(sTimeStr.substring(len - 4, len - 2)));
            cal.add(Calendar.SECOND, Integer.parseInt(sTimeStr.substring(len - 2)));
        }
        int tNum = getDimensionLength("TSTEP");
        sTimeStr = getGlobalAttStr("TSTEP");
        len = sTimeStr.length();
        List<Date> times = new ArrayList<Date>();
        times.add(cal.getTime());
        for (i = 1; i < tNum; i++) {
            if (sTimeStr.length() <= 2) {
                cal.add(Calendar.SECOND, Integer.parseInt(sTimeStr));
            } else if (sTimeStr.length() <= 4) {
                cal.add(Calendar.MINUTE, Integer.parseInt(sTimeStr.substring(0, len - 2)));
                cal.add(Calendar.SECOND, Integer.parseInt(sTimeStr.substring(len - 2)));
            } else {
                cal.add(Calendar.HOUR, Integer.parseInt(sTimeStr.substring(0, len - 4)));
                cal.add(Calendar.MINUTE, Integer.parseInt(sTimeStr.substring(len - 4, len - 2)));
                cal.add(Calendar.SECOND, Integer.parseInt(sTimeStr.substring(len - 2)));
            }
            times.add(cal.getTime());
        }
        List<Double> values = new ArrayList<Double>();
        for (Date t : times) {
            values.add(DateUtil.toOADate(t));
        }
        Dimension tDim = this.findDimension("TSTEP");
        if (tDim != null) {
            tDim.setDimType(DimensionType.T);
            tDim.setValues(values);
            this.setTimeDimension(tDim);
        }

        //Get levels
        ucar.nc2.Attribute levAtt = this.findGlobalAttribute("VGLVLS");
        Array array = levAtt.getValues();
        int znum = (int) array.getSize() - 1;
        double[] levels = new double[znum];
        for (i = 0; i < znum; i++) {
            levels[i] = (array.getDouble(i) + array.getDouble(i + 1)) / 2;
        }
//        String[] levStrs = levStr.split(",");      
//        for (i = 0; i < levStrs.length - 1; i++) {
//            levels[i] = (Double.parseDouble(levStrs[i].trim()) + Double.parseDouble(levStrs[i + 1].trim())) / 2;
//        }
        Dimension zDim = this.findDimension("LAY");
        if (zDim != null) {
            zDim.setDimType(DimensionType.Z);
            zDim.setValues(levels);
            this.setZDimension(zDim);
        }

        //Get X array
        int xNum = Integer.parseInt(getGlobalAttStr("NCOLS"));
        double[] X = new double[xNum];
        double sx = Double.parseDouble(getGlobalAttStr("XORIG"));
        double XDelt = Double.parseDouble(getGlobalAttStr("XCELL"));
        for (i = 0; i < xNum; i++) {
            X[i] = sx + XDelt * i;
        }
        Dimension xDim = this.findDimension("COL");
        if (xDim != null) {
            xDim.setDimType(DimensionType.X);
            xDim.setValues(X);
            this.setXDimension(xDim);
        }

        //Get Y array
        int yNum = Integer.parseInt(getGlobalAttStr("NROWS"));
        double[] Y = new double[yNum];
        double sy = Double.parseDouble(getGlobalAttStr("YORIG"));
        double YDelt = Double.parseDouble(getGlobalAttStr("YCELL"));
        for (i = 0; i < yNum; i++) {
            Y[i] = sy + YDelt * i;
        }
        Dimension yDim = this.findDimension("ROW");
        if (yDim != null) {
            yDim.setDimType(DimensionType.Y);
            yDim.setValues(Y);
            this.setYDimension(yDim);
        }
    }

    private void getDimValues_WRF(NetcdfFile ncfile) throws ParseException, IOException {
        int dimLen, i;
        double orgLon = 0,
                orgLat = 0,
                orgX = 0,
                orgY = 0;
        Dimension xDim = this.findDimension("west_east");
        Dimension yDim = this.findDimension("south_north");
        xDim.setDimType(DimensionType.X);
        yDim.setDimType(DimensionType.Y);
        int xNum = xDim.getDimLength();
        int yNum = yDim.getDimLength();

        //List<String> varNameList = this.getVariableNames();
        _yVar = ncfile.findVariable("XLAT");
        if (_yVar == null) {
            _yVar = ncfile.findVariable("XLAT_M");
        }

        _xVar = ncfile.findVariable("XLONG");
        if (_xVar == null) {
            _xVar = ncfile.findVariable("XLONG_M");
        }

        _levelVar = ncfile.findVariable("ZNU");

        //Get X/Y Array
        double dx = Double.parseDouble(getGlobalAttStr("DX"));
        double dy = Double.parseDouble(getGlobalAttStr("DY"));
        ProjectionInfo fromProj = KnownCoordinateSystems.geographic.world.WGS1984;
        double[][] points = new double[1][];
        if (_yVar != null && _xVar != null) {
            dimLen = yNum;
            Array yarray = _yVar.read().reduce();
            double[] xlat = new double[dimLen];
            for (i = 0; i < dimLen; i++) {
                xlat[i] = yarray.getDouble(i);
            }
            orgLat = xlat[0];

            dimLen = xNum;
            Array xarray = _xVar.read().reduce();
            double[] xlon = new double[dimLen];
            for (i = 0; i < dimLen; i++) {
                xlon[i] = xarray.getDouble(i);
            }
            orgLon = xlon[0];

            points[0] = new double[]{orgLon, orgLat};
            Reproject.reprojectPoints(points, fromProj, this.getProjectionInfo(), 0, 1);
            orgX = points[0][0];
            orgY = points[0][1];
        } else {
            double clon = Double.parseDouble(getGlobalAttStr("CEN_LON"));
            double clat = Double.parseDouble(getGlobalAttStr("CEN_LAT"));
            points[0] = new double[]{clon, clat};
            Reproject.reprojectPoints(points, fromProj, this.getProjectionInfo(), 0, 1);
            double cx = points[0][0];
            double cy = points[0][1];
            orgX = cx - dx * xNum * 0.5;
            orgY = cy - dy * yNum * 0.5;
        }
        double[] X = new double[xNum];
        for (i = 0; i < xNum; i++) {
            X[i] = orgX + dx * i;
        }
        xDim.setValues(X);
        this.setXDimension(xDim);
        double[] Y = new double[yNum];
        for (i = 0; i < yNum; i++) {
            Y[i] = orgY + dy * i;
        }
        yDim.setValues(Y);
        this.setYDimension(yDim);

        //Get levels
        Dimension zDim = this.findDimension("bottom_up");
        if (zDim == null) {
            zDim = this.findDimension("bottom_top");
        }
        if (zDim != null) {
            int lNum = zDim.getDimLength();
            if (_levelVar != null) {
                dimLen = lNum;
                Array larray = _levelVar.read().reduce();
                double[] levels = new double[lNum];
                for (i = 0; i < lNum; i++) {
                    if (i < dimLen) {
                        levels[i] = larray.getDouble(i);
                    } else {
                        break;
                    }
                }
                zDim.setDimType(DimensionType.Z);
                //zDim.setDimName(_levelVar.getShortName());
                zDim.setValues(levels);
                this.setZDimension(zDim);
            }
        }

        zDim = this.findDimension("bottom_up_stag");
        if (zDim == null) {
            zDim = this.findDimension("bottom_top_stag");
        }
        if (zDim != null) {
            int lNum = zDim.getDimLength();
            ucar.nc2.Variable levelVar = ncfile.findVariable("ZNW");
            if (levelVar != null) {
                dimLen = lNum;
                Array larray = levelVar.read().reduce();
                double[] levels = new double[lNum];
                for (i = 0; i < lNum; i++) {
                    if (i < dimLen) {
                        levels[i] = larray.getDouble(i);
                    } else {
                        break;
                    }
                }
                zDim.setDimType(DimensionType.Z);
                //zDim.setDimName(_levelVar.getShortName());
                zDim.setValues(levels);
                //this.setZDimension(zDim);
            }
        }

        zDim = this.findDimension("soil_layers_stag");
        if (zDim != null) {
            int lNum = zDim.getDimLength();
            ucar.nc2.Variable levelVar = ncfile.findVariable("ZS");
            if (levelVar != null) {
                dimLen = lNum;
                Array larray = levelVar.read().reduce();
                double[] levels = new double[lNum];
                for (i = 0; i < lNum; i++) {
                    if (i < dimLen) {
                        levels[i] = larray.getDouble(i);
                    } else {
                        break;
                    }
                }
                zDim.setDimType(DimensionType.Z);
                //zDim.setDimName(_levelVar.getShortName());
                zDim.setValues(levels);
                //this.setZDimension(zDim);
            }
        }

        for (ucar.nc2.Variable aVarS : _variables) {
            //dimLen = aVarS.getShape(0);
            //Get times
            if (aVarS.getShortName().toLowerCase().equals("times") && aVarS.getRank() == 2) {
                Dimension tDim = this.findDimension("Time");
                int tNum = tDim.getDimLength();
                ucar.nc2.Dimension tsDim = ncfile.findDimension("DateStrLen");
                int strLen = tsDim.getLength();
                char[] charData = new char[tNum * strLen];
                Array tarray = aVarS.read();
                for (i = 0; i < tNum * strLen; i++) {
                    charData[i] = tarray.getChar(i);
                }

                String tStr;
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                List<Date> times = new ArrayList<Date>();
                for (i = 0; i < tNum; i++) {
                    StringBuilder timeStr = new StringBuilder();
                    for (int j = 0; j < strLen; j++) {
                        timeStr.append(charData[i * strLen + j]);
                    }
                    tStr = timeStr.toString();
                    if (tStr.contains("0000-00-00")) {
                        tStr = "0001-01-01_00:00:00";
                    }
                    times.add(format.parse(tStr));
                }
                List<Double> values = new ArrayList<Double>();
                for (Date t : times) {
                    values.add(DateUtil.toOADate(t));
                }
                tDim.setDimType(DimensionType.T);
                //tDim.setDimName("times");
                tDim.setValues(values);
                this.setTimeDimension(tDim);
                break;
            }
        }

        Dimension xsdim = findDimension("west_east_stag");
        Dimension ysdim = findDimension("south_north_stag");
        if (xsdim != null && ysdim != null) {
            xsdim.setDimType(DimensionType.X);
            double[] nX = new double[xNum + 1];
            double norgX = orgX - dx * 0.5;
            for (i = 0; i <= xNum; i++) {
                nX[i] = norgX + dx * i;
            }
            xsdim.setValues(nX);

            ysdim.setDimType(DimensionType.Y);
            double[] nY = new double[yNum + 1];
            double norgY = orgY - dx * 0.5;
            for (i = 0; i <= yNum; i++) {
                nY[i] = norgY + dy * i;
            }
            ysdim.setValues(nY);
        }
    }

    private int getDimensionLength(String dimName) {
        for (ucar.nc2.Dimension aDimS : _dimensions) {
            if (aDimS.getShortName().equals(dimName)) {
                return aDimS.getLength();
            }
        }

        return -1;
    }

    private int getVarLength(Variable aVarS) {
        int dataLen = 1;
        for (int i = 0; i < aVarS.getDimNumber(); i++) {
            dataLen = dataLen * aVarS.getDimensions().get(i).getDimLength();
        }

        return dataLen;
    }

    /**
     * Find netCDF variable by name
     *
     * @param name Variable name
     * @return NetCDF variable
     */
    public ucar.nc2.Variable findNCVariable(String name) {
        for (ucar.nc2.Variable var : this._variables) {
            if (var.getShortName().equalsIgnoreCase(name)) {
                return var;
            }
        }

        return null;
    }

    private DimensionType getDimType(ucar.nc2.Variable aVar) {
        String sName;
        DimensionType dimType = DimensionType.Other;
        if (_fileTypeId.equals("HDF5-EOS")) {
            sName = aVar.getShortName().toLowerCase();
            if (sName.equals("longitude")) {
                dimType = DimensionType.X;
            } else if (sName.equals("latitude")) {
                dimType = DimensionType.Y;
            } else if (sName.equals("pressure")) {
                dimType = DimensionType.Z;
            } else if (sName.equals("time")) {
                dimType = DimensionType.T;
            }
        } else {
            if (aVar.findAttributeIgnoreCase("standard_name") != null) {
                ucar.nc2.Attribute axisAtt = aVar.findAttributeIgnoreCase("standard_name");
                sName = axisAtt.getStringValue().trim().toLowerCase();
                if (sName.equals("longitude") || sName.equals("projection_x_coordinate") || sName.equals("longitude_east")) {
                    dimType = DimensionType.X;
                } else if (sName.equals("latitude") || sName.equals("projection_y_coordinate") || sName.equals("latitude_north")) {
                    dimType = DimensionType.Y;
                } else if (sName.equals("time")) {
                    dimType = DimensionType.T;
                } else if (sName.equals("level")) {
                    dimType = DimensionType.Z;
                }
            }
            if (dimType == DimensionType.Other) {
                if (aVar.findAttributeIgnoreCase("long_name") != null) {
                    ucar.nc2.Attribute axisAtt = aVar.findAttributeIgnoreCase("long_name");
                    sName = axisAtt.getStringValue().trim().toLowerCase();
                    if (sName.equals("longitude") || sName.equals("coordinate longitude") || sName.equals("x")) {
                        dimType = DimensionType.X;
                    } else if (sName.equals("latitude") || sName.equals("coordinate latitude") || sName.equals("y")) {
                        dimType = DimensionType.Y;
                    } else if (sName.equals("time") || sName.equals("initial time")) {
                        dimType = DimensionType.T;
                    } else if (sName.equals("level") || sName.equals("pressure_level") || sName.equals("isobaric surface")) {
                        dimType = DimensionType.Z;
                    } else {
                        if (sName.contains("level") || sName.contains("depths")) {
                            dimType = DimensionType.Z;
                        }
                    }
                }
            }
            if (dimType == DimensionType.Other) {
                if (aVar.findAttributeIgnoreCase("axis") != null) {
                    ucar.nc2.Attribute axisAtt = aVar.findAttributeIgnoreCase("axis");
                    sName = axisAtt.getStringValue().trim().toLowerCase();
                    if (sName.equals("x")) {
                        dimType = DimensionType.X;
                    } else if (sName.equals("y")) {
                        dimType = DimensionType.Y;
                    } else if (sName.equals("z")) {
                        dimType = DimensionType.Z;
                    } else if (sName.equals("t")) {
                        dimType = DimensionType.T;
                    }
                }
            }
            if (dimType == DimensionType.Other) {
                if (aVar.findAttributeIgnoreCase("GRIB_level_type") != null) {
                    dimType = DimensionType.Z;
                }
            }
            if (dimType == DimensionType.Other) {
                if (aVar.findAttributeIgnoreCase("Grib2_level_type") != null) {
                    dimType = DimensionType.Z;
                }
            }
            if (dimType == DimensionType.Other) {
                if (aVar.findAttributeIgnoreCase("hybrid_layer") != null) {
                    dimType = DimensionType.Z;
                }
            }
            if (dimType == DimensionType.Other) {
                if (aVar.findAttributeIgnoreCase("unitsCategory") != null) {
                    sName = aVar.findAttributeIgnoreCase("unitsCategory").getStringValue().trim().toLowerCase();
                    if (sName.equals("longitude")) {
                        dimType = DimensionType.X;
                    } else if (sName.equals("latitude")) {
                        dimType = DimensionType.Y;
                    }
                }
            }
            if (dimType == DimensionType.Other) {
                String vName = aVar.getShortName().toLowerCase();
                if (vName.equals("lon") || vName.equals("longitude") || vName.equals("x")) {
                    dimType = DimensionType.X;
                } else if (vName.equals("lat") || vName.equals("latitude") || vName.equals("y")) {
                    dimType = DimensionType.Y;
                } else if (vName.equals("time")) {
                    dimType = DimensionType.T;
                } else if (vName.equals("level") || vName.equals("lev") || vName.equals("height") || vName.equals("isobaric")) {
                    dimType = DimensionType.Z;
                }
            }
        }

        return dimType;
    }

    private TimeUnit getTimeUnit(String tStr) {
        TimeUnit aTU = TimeUnit.Second;
        tStr = tStr.trim();
        String tu;
        String[] dataArray;
        int i;

        dataArray = tStr.split("\\s+");

        if (dataArray.length < 2) {
            return aTU;
        }

        //Get time unit
        tu = dataArray[0];
        if (tu.toLowerCase().contains("second")) {
            aTU = TimeUnit.Second;
        } else {
            if (tu.length() == 1) {
                String str = tu.toLowerCase();
                if (str.equals("y")) {
                    aTU = TimeUnit.Year;
                } else if (str.equals("d")) {
                    aTU = TimeUnit.Day;
                } else if (str.equals("h")) {
                    aTU = TimeUnit.Hour;
                } else if (str.equals("s")) {
                    aTU = TimeUnit.Second;
                }
            } else {
                String str = tu.toLowerCase().substring(0, 2);
                if (str.equals("yr") || str.equals("ye")) {
                    aTU = TimeUnit.Year;
                } else if (str.equals("mo")) {
                    aTU = TimeUnit.Month;
                } else if (str.equals("da")) {
                    aTU = TimeUnit.Day;
                } else if (str.equals("hr") || str.equals("ho")) {
                    aTU = TimeUnit.Hour;
                } else if (str.equals("mi")) {
                    aTU = TimeUnit.Minute;
                } else if (str.equals("se")) {
                    aTU = TimeUnit.Second;
                }
            }
        }

        return aTU;
    }

    private Date getStartTime(String tStr) {
        Date sTime = new Date();
        tStr = tStr.trim();
        String[] dataArray;

        dataArray = tStr.split("\\s+");

        if (dataArray.length < 2) {
            return sTime;
        }

        //Get start time
        String ST;
        ST = dataArray[2];
        if (ST.contains("T")) {
            dataArray = Arrays.copyOf(dataArray, dataArray.length + 1);
            dataArray[dataArray.length - 1] = ST.split("T")[1];
            ST = ST.split("T")[0];
        }
        int year = 2000, month = 1, day = 1;
        int hour = 0, min = 0, sec = 0;
        if (ST.contains("-")) {
            year = Integer.parseInt(ST.split("-")[0]);
            month = Integer.parseInt(ST.split("-")[1]);
            day = Integer.parseInt(ST.split("-")[2]);
            if (dataArray.length >= 4) {
                String hmsStr = dataArray[3];
                hmsStr = hmsStr.replace("0.0", "00");
                try {
                    hour = Integer.parseInt(hmsStr.split(":")[0]);
                    min = Integer.parseInt(hmsStr.split(":")[1]);
                    sec = Integer.parseInt(hmsStr.split(":")[2]);
                } catch (Exception e) {
                }
            }
        } else {
            if (ST.contains(":")) {
                String hmsStr = ST;
                hmsStr = hmsStr.replace("0.0", "00");
                try {
                    hour = Integer.parseInt(hmsStr.split(":")[0]);
                    min = Integer.parseInt(hmsStr.split(":")[1]);
                    sec = Integer.parseInt(hmsStr.split(":")[2]);
                } catch (Exception e) {
                }
            }
        }

        if (year == 0) {
            year = 1;
        }

        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, hour, min, sec);
        sTime = cal.getTime();

        return sTime;
    }

    @Override
    public String generateInfoText() {
        String dataInfo;
        int i, j;
        ucar.nc2.Attribute aAttS;
        dataInfo = "File Name: " + this.getFileName();
        dataInfo += System.getProperty("line.separator") + "File type: " + _fileTypeStr + " (" + _fileTypeId + ")";
        dataInfo += System.getProperty("line.separator") + "Dimensions: " + _dimensions.size();
        for (i = 0; i < _dimensions.size(); i++) {
            dataInfo += System.getProperty("line.separator") + "\t" + _dimensions.get(i).getShortName() + " = "
                    + String.valueOf(_dimensions.get(i).getLength()) + ";";
        }

        Dimension xdim = this.getXDimension();
        if (xdim != null) {
            dataInfo += System.getProperty("line.separator") + "X Dimension: Xmin = " + String.valueOf(xdim.getMinValue())
                    + "; Xmax = " + String.valueOf(xdim.getMaxValue()) + "; Xsize = "
                    + String.valueOf(xdim.getDimLength()) + "; Xdelta = " + String.valueOf(xdim.getDeltaValue());
        }
        Dimension ydim = this.getYDimension();
        if (ydim != null) {
            dataInfo += System.getProperty("line.separator") + "Y Dimension: Ymin = " + String.valueOf(ydim.getMinValue())
                    + "; Ymax = " + String.valueOf(ydim.getMaxValue()) + "; Ysize = "
                    + String.valueOf(ydim.getDimLength()) + "; Ydelta = " + String.valueOf(ydim.getDeltaValue());
        }

        dataInfo += System.getProperty("line.separator") + "Global Attributes: " + this._gAtts;
        for (i = 0; i < _gAtts.size(); i++) {
            aAttS = _gAtts.get(i);
            dataInfo += System.getProperty("line.separator") + "\t: " + aAttS.toString();
        }

        dataInfo += System.getProperty("line.separator") + "Variations: " + _variables.size();
        for (i = 0; i < _variables.size(); i++) {
            dataInfo += System.getProperty("line.separator") + "\t" + _variables.get(i).getDataType().toString()
                    + " " + _variables.get(i).getShortName() + "(";
            List<ucar.nc2.Dimension> dims = _variables.get(i).getDimensions();
            for (j = 0; j < dims.size(); j++) {
                dataInfo += dims.get(j).getShortName() + ",";
            }
            dataInfo = dataInfo.substring(0, dataInfo.length() - 1);
            dataInfo += ");";
            List<ucar.nc2.Attribute> atts = _variables.get(i).getAttributes();
            for (j = 0; j < atts.size(); j++) {
                aAttS = atts.get(j);
                dataInfo += System.getProperty("line.separator") + "\t" + "\t" + _variables.get(i).getShortName()
                        + ": " + aAttS.toString();
            }
        }

        for (ucar.nc2.Dimension dim : _dimensions) {
            if (dim.isUnlimited()) {
                dataInfo += System.getProperty("line.separator") + "Unlimited dimension: " + dim.getShortName();
            }
            break;
        }

        return dataInfo;
    }

    private int getTrueVarIndex(int varIdx) {
        int tVarIdx = varIdx;
        for (int i = 0; i < this.getVariables().size(); i++) {
            Variable var = this.getVariables().get(i);
            if (tVarIdx > i || (tVarIdx == i && var.isPlottable())) {
                break;
            }

            if (!var.isPlottable()) {
                tVarIdx += 1;
            }
        }

        return tVarIdx;
    }

    private double[] getPackData(ucar.nc2.Variable var) {
        double add_offset, scale_factor, missingValue = this.getMissingValue();
        add_offset = 0;
        scale_factor = 1;
        for (int i = 0; i < var.getAttributes().size(); i++) {
            ucar.nc2.Attribute att = var.getAttributes().get(i);
            String attName = att.getShortName();
            if (attName.equals("add_offset")) {
                add_offset = Double.parseDouble(att.getValue(0).toString());
            }

            if (attName.equals("scale_factor")) {
                scale_factor = Double.parseDouble(att.getValue(0).toString());
            }

            if (attName.equals("missing_value")) {
                missingValue = Double.parseDouble(att.getValue(0).toString());
            }

            //MODIS NetCDF data
            if (attName.equals("_FillValue")) {
                missingValue = Double.parseDouble(att.getValue(0).toString());
            }
        }

        //Adjust undefine data
        if (Double.isNaN(missingValue)) {
            missingValue = this.getMissingValue();
        } else {
            missingValue = missingValue * scale_factor + add_offset;
        }

        return new double[]{add_offset, scale_factor, missingValue};
    }

    @Override
    public GridData getGridData_LonLat(int timeIdx, int varIdx, int levelIdx) {
        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(this.getFileName());

            int i, j;
            //int tVarIdx = this.getTrueVarIndex(varIdx);
            int tVarIdx = varIdx;
            //ucar.nc2.Variable var = _variables.get(tVarIdx);
            //var = ncfile.findVariable(var.getShortName());
            ucar.nc2.Variable var = ncfile.getVariables().get(tVarIdx);

            //Get pack info
            double add_offset, scale_factor, missingValue;
            double[] packData = this.getPackData(var);
            add_offset = packData[0];
            scale_factor = packData[1];
            missingValue = packData[2];

            //Read grid data
            Variable nvar = this.getVariables().get(tVarIdx);
            int xNum, yNum;
            xNum = nvar.getXDimension().getDimLength();
            yNum = nvar.getYDimension().getDimLength();
            double[][] gridData = new double[yNum][xNum];

            int rank = var.getRank();
            int[] origin = new int[rank];
            int[] size = new int[rank];
            int xdimIdx = 0;
            int ydimIdx = 0;
            for (i = 0; i < rank; i++) {
                ucar.nc2.Dimension dim = var.getDimension(i);
                Dimension ndim = nvar.getDimensions().get(i);
                switch (ndim.getDimType()) {
                    case T:
                        origin[i] = timeIdx;
                        size[i] = 1;
                        break;
                    case Z:
                        origin[i] = levelIdx;
                        size[i] = 1;
                        break;
                    case Y:
                        origin[i] = 0;
                        size[i] = yNum;
                        ydimIdx = i;
                        break;
                    case X:
                        origin[i] = 0;
                        size[i] = xNum;
                        xdimIdx = i;
                        break;
                    default:
                        origin[i] = 0;
                        size[i] = 1;
                        break;
                }
            }

            Array data2D = var.read(origin, size).reduce();

            if (ydimIdx < xdimIdx) {
                for (i = 0; i < yNum; i++) {
                    for (j = 0; j < xNum; j++) {
                        gridData[i][j] = data2D.getDouble(i * xNum + j) * scale_factor + add_offset;
                    }
                }
            } else {
                for (i = 0; i < yNum; i++) {
                    for (j = 0; j < xNum; j++) {
                        gridData[i][j] = data2D.getDouble(j * yNum + i) * scale_factor + add_offset;
                    }
                }
            }

            GridData aGridData = new GridData();
            aGridData.data = gridData;
            aGridData.xArray = nvar.getXDimension().getValues();
            aGridData.yArray = nvar.getYDimension().getValues();
            aGridData.missingValue = missingValue;

            if (this.isYReverse()) {
                aGridData.yReverse();
            }

            if (this._convention == Conventions.WRFOUT) {
                if (nvar.getName().equals("U")) {
                    aGridData.setXStagger(true);
                }

                if (nvar.getName().equals("V")) {
                    aGridData.setYStagger(true);
                }
            }

            return aGridData;
        } catch (IOException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (InvalidRangeException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (null != ncfile) {
                try {
                    ncfile.close();
                } catch (IOException ex) {
                    Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public GridData getGridData_TimeLat(int lonIdx, int varIdx, int levelIdx) {
        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(this.getFileName());

            int xNum, yNum;
            xNum = this.getYDimension().getDimLength();
            yNum = this.getTimeDimension().getDimLength();
            double[][] gridData = new double[yNum][xNum];

            int i, j;
            //int tVarIdx = this.getTrueVarIndex(varIdx);
            int tVarIdx = varIdx;
            ucar.nc2.Variable var = _variables.get(tVarIdx);
            var = ncfile.findVariable(var.getShortName());

            //Get pack info            
            double add_offset, scale_factor, missingValue;
            double[] packData = this.getPackData(var);
            add_offset = packData[0];
            scale_factor = packData[1];
            missingValue = packData[2];

            //Read grid data
            Variable nvar = this.getVariables().get(tVarIdx);
            int rank = var.getRank();
            int[] origin = new int[rank];
            int[] size = new int[rank];
            for (i = 0; i < rank; i++) {
                ucar.nc2.Dimension dim = var.getDimension(i);
                Dimension ndim = nvar.getDimensions().get(i);
                switch (ndim.getDimType()) {
                    case T:
                        origin[i] = 0;
                        size[i] = yNum;
                        break;
                    case Z:
                        origin[i] = levelIdx;
                        size[i] = 1;
                        break;
                    case Y:
                        origin[i] = 0;
                        size[i] = xNum;
                        break;
                    case X:
                        origin[i] = lonIdx;
                        size[i] = 1;
                        break;
                    default:
                        origin[i] = 0;
                        size[i] = 1;
                        break;
                }
            }

            Array data2D = var.read(origin, size).reduce();

            for (i = 0; i < yNum; i++) {
                for (j = 0; j < xNum; j++) {
                    gridData[i][j] = data2D.getDouble(i * xNum + j) * scale_factor + add_offset;
                }
            }

            GridData aGridData = new GridData();
            aGridData.data = gridData;
            aGridData.xArray = this.getYDimension().getValues();
            aGridData.yArray = this.getTimeDimension().getValues();
            aGridData.missingValue = missingValue;

            if (this.isYReverse()) {
                aGridData.yReverse();
            }

            return aGridData;
        } catch (IOException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (InvalidRangeException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (null != ncfile) {
                try {
                    ncfile.close();
                } catch (IOException ex) {
                    Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public GridData getGridData_TimeLon(int latIdx, int varIdx, int levelIdx) {
        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(this.getFileName());

            int xNum, yNum;
            xNum = this.getXDimension().getDimLength();
            yNum = this.getTimeDimension().getDimLength();
            double[][] gridData = new double[yNum][xNum];

            int i, j;
            //int tVarIdx = this.getTrueVarIndex(varIdx);
            int tVarIdx = varIdx;
            ucar.nc2.Variable var = _variables.get(tVarIdx);
            var = ncfile.findVariable(var.getShortName());

            //Get pack info            
            double add_offset, scale_factor, missingValue;
            double[] packData = this.getPackData(var);
            add_offset = packData[0];
            scale_factor = packData[1];
            missingValue = packData[2];

            //Read grid data
            Variable nvar = this.getVariables().get(tVarIdx);
            int rank = var.getRank();
            int[] origin = new int[rank];
            int[] size = new int[rank];
            for (i = 0; i < rank; i++) {
                ucar.nc2.Dimension dim = var.getDimension(i);
                Dimension ndim = nvar.getDimensions().get(i);
                switch (ndim.getDimType()) {
                    case T:
                        origin[i] = 0;
                        size[i] = yNum;
                        break;
                    case Z:
                        origin[i] = levelIdx;
                        size[i] = 1;
                        break;
                    case Y:
                        origin[i] = latIdx;
                        size[i] = 1;
                        break;
                    case X:
                        origin[i] = 0;
                        size[i] = xNum;
                        break;
                    default:
                        origin[i] = 0;
                        size[i] = 1;
                        break;
                }
            }

            Array data2D = var.read(origin, size).reduce();

            for (i = 0; i < yNum; i++) {
                for (j = 0; j < xNum; j++) {
                    gridData[i][j] = data2D.getDouble(i * xNum + j) * scale_factor + add_offset;
                }
            }

            GridData aGridData = new GridData();
            aGridData.data = gridData;
            aGridData.xArray = this.getYDimension().getValues();
            aGridData.yArray = this.getTimeDimension().getValues();
            aGridData.missingValue = missingValue;

            return aGridData;
        } catch (IOException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (InvalidRangeException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (null != ncfile) {
                try {
                    ncfile.close();
                } catch (IOException ex) {
                    Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public GridData getGridData_LevelLat(int lonIdx, int varIdx, int timeIdx) {
        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(this.getFileName());

            int xNum, yNum;
            xNum = this.getYDimension().getDimLength();
            yNum = this.getZDimension().getDimLength();
            double[][] gridData = new double[yNum][xNum];

            int i, j;
            //int tVarIdx = this.getTrueVarIndex(varIdx);
            int tVarIdx = varIdx;
            ucar.nc2.Variable var = _variables.get(tVarIdx);
            var = ncfile.findVariable(var.getShortName());

            //Get pack info            
            double add_offset, scale_factor, missingValue;
            double[] packData = this.getPackData(var);
            add_offset = packData[0];
            scale_factor = packData[1];
            missingValue = packData[2];

            //Read grid data
            Variable nvar = this.getVariables().get(tVarIdx);
            int rank = var.getRank();
            int[] origin = new int[rank];
            int[] size = new int[rank];
            for (i = 0; i < rank; i++) {
                ucar.nc2.Dimension dim = var.getDimension(i);
                Dimension ndim = nvar.getDimensions().get(i);
                switch (ndim.getDimType()) {
                    case T:
                        origin[i] = timeIdx;
                        size[i] = 1;
                        break;
                    case Z:
                        origin[i] = 0;
                        size[i] = yNum;
                        break;
                    case Y:
                        origin[i] = 0;
                        size[i] = xNum;
                        break;
                    case X:
                        origin[i] = lonIdx;
                        size[i] = 1;
                        break;
                    default:
                        origin[i] = 0;
                        size[i] = 1;
                        break;
                }
            }

            Array data2D = var.read(origin, size).reduce();

            for (i = 0; i < yNum; i++) {
                for (j = 0; j < xNum; j++) {
                    gridData[i][j] = data2D.getDouble(i * xNum + j) * scale_factor + add_offset;
                }
            }

            GridData aGridData = new GridData();
            aGridData.data = gridData;
            aGridData.xArray = this.getYDimension().getValues();
            aGridData.yArray = this.getZDimension().getValues();
            aGridData.missingValue = missingValue;

            if (this.isYReverse()) {
                aGridData.yReverse();
            }

            return aGridData;
        } catch (IOException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (InvalidRangeException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (null != ncfile) {
                try {
                    ncfile.close();
                } catch (IOException ex) {
                    Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public GridData getGridData_LevelLon(int latIdx, int varIdx, int timeIdx) {
        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(this.getFileName());

            int xNum, yNum;
            xNum = this.getXDimension().getDimLength();
            yNum = this.getZDimension().getDimLength();
            double[][] gridData = new double[yNum][xNum];

            int i, j;
            //int tVarIdx = this.getTrueVarIndex(varIdx);
            int tVarIdx = varIdx;
            ucar.nc2.Variable var = _variables.get(tVarIdx);
            var = ncfile.findVariable(var.getShortName());

            //Get pack info            
            double add_offset, scale_factor, missingValue;
            double[] packData = this.getPackData(var);
            add_offset = packData[0];
            scale_factor = packData[1];
            missingValue = packData[2];

            //Read grid data
            Variable nvar = this.getVariables().get(tVarIdx);
            int rank = var.getRank();
            int[] origin = new int[rank];
            int[] size = new int[rank];
            for (i = 0; i < rank; i++) {
                ucar.nc2.Dimension dim = var.getDimension(i);
                Dimension ndim = nvar.getDimensions().get(i);
                switch (ndim.getDimType()) {
                    case T:
                        origin[i] = timeIdx;
                        size[i] = 1;
                        break;
                    case Z:
                        origin[i] = 0;
                        size[i] = yNum;
                        break;
                    case Y:
                        origin[i] = latIdx;
                        size[i] = 1;
                        break;
                    case X:
                        origin[i] = 0;
                        size[i] = xNum;
                        break;
                    default:
                        origin[i] = 0;
                        size[i] = 1;
                        break;
                }
            }

            Array data2D = var.read(origin, size).reduce();

            for (i = 0; i < yNum; i++) {
                for (j = 0; j < xNum; j++) {
                    gridData[i][j] = data2D.getDouble(i * xNum + j) * scale_factor + add_offset;
                }
            }

            GridData aGridData = new GridData();
            aGridData.data = gridData;
            aGridData.xArray = this.getYDimension().getValues();
            aGridData.yArray = this.getTimeDimension().getValues();
            aGridData.missingValue = missingValue;

            return aGridData;
        } catch (IOException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (InvalidRangeException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (null != ncfile) {
                try {
                    ncfile.close();
                } catch (IOException ex) {
                    Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public GridData getGridData_LevelTime(int latIdx, int varIdx, int lonIdx) {
        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(this.getFileName());

            int xNum, yNum;
            xNum = this.getTimeDimension().getDimLength();
            yNum = this.getZDimension().getDimLength();
            double[][] gridData = new double[yNum][xNum];

            int i, j;
            //int tVarIdx = this.getTrueVarIndex(varIdx);
            int tVarIdx = varIdx;
            ucar.nc2.Variable var = _variables.get(tVarIdx);
            var = ncfile.findVariable(var.getShortName());

            //Get pack info            
            double add_offset, scale_factor, missingValue;
            double[] packData = this.getPackData(var);
            add_offset = packData[0];
            scale_factor = packData[1];
            missingValue = packData[2];

            //Read grid data
            Variable nvar = this.getVariables().get(tVarIdx);
            int rank = var.getRank();
            int[] origin = new int[rank];
            int[] size = new int[rank];
            for (i = 0; i < rank; i++) {
                ucar.nc2.Dimension dim = var.getDimension(i);
                Dimension ndim = nvar.getDimensions().get(i);
                switch (ndim.getDimType()) {
                    case T:
                        origin[i] = 0;
                        size[i] = xNum;
                        break;
                    case Z:
                        origin[i] = 0;
                        size[i] = yNum;
                        break;
                    case Y:
                        origin[i] = latIdx;
                        size[i] = 1;
                        break;
                    case X:
                        origin[i] = lonIdx;
                        size[i] = 1;
                        break;
                    default:
                        origin[i] = 0;
                        size[i] = 1;
                        break;
                }
            }

            Array data2D = var.read(origin, size).reduce();

            for (i = 0; i < yNum; i++) {
                for (j = 0; j < xNum; j++) {
                    gridData[i][j] = data2D.getDouble(i * xNum + j) * scale_factor + add_offset;
                }
            }

            GridData aGridData = new GridData();
            aGridData.data = gridData;
            aGridData.xArray = this.getYDimension().getValues();
            aGridData.yArray = this.getTimeDimension().getValues();
            aGridData.missingValue = missingValue;

            return aGridData;
        } catch (IOException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (InvalidRangeException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (null != ncfile) {
                try {
                    ncfile.close();
                } catch (IOException ex) {
                    Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public GridData getGridData_Time(int lonIdx, int latIdx, int varIdx, int levelIdx) {
        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(this.getFileName());

            int dNum = this.getTimeDimension().getDimLength();
            double[][] gridData = new double[1][dNum];

            int i, j;
            //int tVarIdx = this.getTrueVarIndex(varIdx);
            int tVarIdx = varIdx;
            ucar.nc2.Variable var = _variables.get(tVarIdx);
            var = ncfile.findVariable(var.getShortName());

            //Get pack info            
            double add_offset, scale_factor, missingValue;
            double[] packData = this.getPackData(var);
            add_offset = packData[0];
            scale_factor = packData[1];
            missingValue = packData[2];

            //Read grid data
            Variable nvar = this.getVariables().get(tVarIdx);
            int rank = var.getRank();
            int[] origin = new int[rank];
            int[] size = new int[rank];
            for (i = 0; i < rank; i++) {
                ucar.nc2.Dimension dim = var.getDimension(i);
                Dimension ndim = nvar.getDimensions().get(i);
                switch (ndim.getDimType()) {
                    case T:
                        origin[i] = 0;
                        size[i] = dNum;
                        break;
                    case Z:
                        origin[i] = levelIdx;
                        size[i] = 1;
                        break;
                    case Y:
                        origin[i] = latIdx;
                        size[i] = 1;
                        break;
                    case X:
                        origin[i] = lonIdx;
                        size[i] = 1;
                        break;
                    default:
                        origin[i] = 0;
                        size[i] = 1;
                        break;
                }
            }

            Array data1D = var.read(origin, size).reduce();

            for (i = 0; i < dNum; i++) {
                gridData[0][i] = data1D.getDouble(i) * scale_factor + add_offset;
            }

            GridData aGridData = new GridData();
            aGridData.data = gridData;
            aGridData.xArray = this.getTimeDimension().getValues();
            aGridData.yArray = new double[1];
            aGridData.missingValue = missingValue;

            return aGridData;
        } catch (IOException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (InvalidRangeException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (null != ncfile) {
                try {
                    ncfile.close();
                } catch (IOException ex) {
                    Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public GridData getGridData_Level(int lonIdx, int latIdx, int varIdx, int timeIdx) {
        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(this.getFileName());

            int dNum = this.getZDimension().getDimLength();
            double[][] gridData = new double[1][dNum];

            int i, j;
            //int tVarIdx = this.getTrueVarIndex(varIdx);
            int tVarIdx = varIdx;
            ucar.nc2.Variable var = _variables.get(tVarIdx);
            var = ncfile.findVariable(var.getShortName());

            //Get pack info            
            double add_offset, scale_factor, missingValue;
            double[] packData = this.getPackData(var);
            add_offset = packData[0];
            scale_factor = packData[1];
            missingValue = packData[2];

            //Read grid data
            Variable nvar = this.getVariables().get(tVarIdx);
            int rank = var.getRank();
            int[] origin = new int[rank];
            int[] size = new int[rank];
            for (i = 0; i < rank; i++) {
                ucar.nc2.Dimension dim = var.getDimension(i);
                Dimension ndim = nvar.getDimensions().get(i);
                switch (ndim.getDimType()) {
                    case T:
                        origin[i] = timeIdx;
                        size[i] = 1;
                        break;
                    case Z:
                        origin[i] = 0;
                        size[i] = dNum;
                        break;
                    case Y:
                        origin[i] = latIdx;
                        size[i] = 1;
                        break;
                    case X:
                        origin[i] = lonIdx;
                        size[i] = 1;
                        break;
                    default:
                        origin[i] = 0;
                        size[i] = 1;
                        break;
                }
            }

            Array data1D = var.read(origin, size).reduce();

            for (i = 0; i < dNum; i++) {
                gridData[0][i] = data1D.getDouble(i) * scale_factor + add_offset;
            }

            GridData aGridData = new GridData();
            aGridData.data = gridData;
            aGridData.xArray = this.getZDimension().getValues();
            aGridData.yArray = new double[1];
            aGridData.missingValue = missingValue;

            return aGridData;
        } catch (IOException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (InvalidRangeException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (null != ncfile) {
                try {
                    ncfile.close();
                } catch (IOException ex) {
                    Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public GridData getGridData_Lon(int timeIdx, int latIdx, int varIdx, int levelIdx) {
        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(this.getFileName());

            int dNum = this.getXDimension().getDimLength();
            double[][] gridData = new double[1][dNum];

            int i, j;
            //int tVarIdx = this.getTrueVarIndex(varIdx);
            int tVarIdx = varIdx;
            ucar.nc2.Variable var = _variables.get(tVarIdx);
            var = ncfile.findVariable(var.getShortName());

            //Get pack info            
            double add_offset, scale_factor, missingValue;
            double[] packData = this.getPackData(var);
            add_offset = packData[0];
            scale_factor = packData[1];
            missingValue = packData[2];

            //Read grid data
            Variable nvar = this.getVariables().get(tVarIdx);
            int rank = var.getRank();
            int[] origin = new int[rank];
            int[] size = new int[rank];
            for (i = 0; i < rank; i++) {
                ucar.nc2.Dimension dim = var.getDimension(i);
                Dimension ndim = nvar.getDimensions().get(i);
                switch (ndim.getDimType()) {
                    case T:
                        origin[i] = timeIdx;
                        size[i] = 1;
                        break;
                    case Z:
                        origin[i] = levelIdx;
                        size[i] = 1;
                        break;
                    case Y:
                        origin[i] = latIdx;
                        size[i] = 1;
                        break;
                    case X:
                        origin[i] = 0;
                        size[i] = dNum;
                        break;
                    default:
                        origin[i] = 0;
                        size[i] = 1;
                        break;
                }
            }

            Array data1D = var.read(origin, size).reduce();

            for (i = 0; i < dNum; i++) {
                gridData[0][i] = data1D.getDouble(i) * scale_factor + add_offset;
            }

            GridData aGridData = new GridData();
            aGridData.data = gridData;
            aGridData.xArray = this.getXDimension().getValues();
            aGridData.yArray = new double[1];
            aGridData.missingValue = missingValue;

            return aGridData;
        } catch (IOException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (InvalidRangeException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (null != ncfile) {
                try {
                    ncfile.close();
                } catch (IOException ex) {
                    Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public GridData getGridData_Lat(int timeIdx, int lonIdx, int varIdx, int levelIdx) {
        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(this.getFileName());

            int dNum = this.getYDimension().getDimLength();
            double[][] gridData = new double[1][dNum];

            int i, j;
            //int tVarIdx = this.getTrueVarIndex(varIdx);
            int tVarIdx = varIdx;
            ucar.nc2.Variable var = _variables.get(tVarIdx);
            var = ncfile.findVariable(var.getShortName());

            //Get pack info            
            double add_offset, scale_factor, missingValue;
            double[] packData = this.getPackData(var);
            add_offset = packData[0];
            scale_factor = packData[1];
            missingValue = packData[2];

            //Read grid data
            Variable nvar = this.getVariables().get(tVarIdx);
            int rank = var.getRank();
            int[] origin = new int[rank];
            int[] size = new int[rank];
            for (i = 0; i < rank; i++) {
                ucar.nc2.Dimension dim = var.getDimension(i);
                Dimension ndim = nvar.getDimensions().get(i);
                switch (ndim.getDimType()) {
                    case T:
                        origin[i] = timeIdx;
                        size[i] = 1;
                        break;
                    case Z:
                        origin[i] = levelIdx;
                        size[i] = 1;
                        break;
                    case Y:
                        origin[i] = 0;
                        size[i] = dNum;
                        break;
                    case X:
                        origin[i] = lonIdx;
                        size[i] = 1;
                        break;
                    default:
                        origin[i] = 0;
                        size[i] = 1;
                        break;
                }
            }

            Array data1D = var.read(origin, size).reduce();

            for (i = 0; i < dNum; i++) {
                gridData[0][i] = data1D.getDouble(i) * scale_factor + add_offset;
            }

            GridData aGridData = new GridData();
            aGridData.data = gridData;
            aGridData.xArray = this.getYDimension().getValues();
            aGridData.yArray = new double[1];
            aGridData.missingValue = missingValue;

            return aGridData;
        } catch (IOException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (InvalidRangeException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (null != ncfile) {
                try {
                    ncfile.close();
                } catch (IOException ex) {
                    Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public StationData getStationData(int timeIdx, int varIdx, int levelIdx) {
        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(this.getFileName());

            int i, j;
            int tVarIdx = varIdx;
            ucar.nc2.Variable var = ncfile.getVariables().get(tVarIdx);

            //Get long/lat data
            //ucar.nc2.Variable lonvar = ncfile.findVariable("Longitude");
            ucar.nc2.Variable lonvar = this.findNCVariable("Longitude");
            //ucar.nc2.Variable latvar = ncfile.findVariable("Latitude");
            ucar.nc2.Variable latvar = this.findNCVariable("Latitude");
            lonvar = ncfile.getVariables().get(this._variables.indexOf(lonvar));
            latvar = ncfile.getVariables().get(this._variables.indexOf(latvar));
            Array lonarray = lonvar.read();
            Array latarray = latvar.read();
            int stNum = (int) lonarray.getSize();
            List<ucar.nc2.Dimension> lldims = lonvar.getDimensions();

            //Get pack info
            double add_offset, scale_factor, missingValue;
            double[] packData = this.getPackData(var);
            add_offset = packData[0];
            scale_factor = packData[1];
            missingValue = packData[2];

            //Read data
            Variable nvar = this.getVariables().get(tVarIdx);
            int rank = var.getRank();
            int[] origin = new int[rank];
            int[] size = new int[rank];
            if (rank == lonvar.getRank()) {
                for (i = 0; i < rank; i++) {
                    ucar.nc2.Dimension dim = var.getDimension(i);
                    origin[i] = 0;
                    size[i] = dim.getLength();
                }
            } else {
                for (i = 0; i < rank; i++) {
                    Dimension ndim = nvar.getDimensions().get(i);
                    ucar.nc2.Dimension dim = var.getDimension(i);
                    switch (ndim.getDimType()) {
                        case T:
                            origin[i] = 0;
                            size[i] = dim.getLength();
                            break;
                        case Xtrack:
                            origin[i] = levelIdx;
                            size[i] = dim.getLength();
                            break;
                        default:
                            if (lldims.contains(dim)) {
                                origin[i] = 0;
                                size[i] = dim.getLength();
                            } else {
                                origin[i] = levelIdx;
                                size[i] = 1;
                            }
                            break;
                    }
                }
            }

            Array darray = var.read(origin, size).reduce();

            double minx, maxx, miny, maxy;
            minx = maxx = lonarray.getDouble(0);
            miny = maxy = latarray.getDouble(0);
            double lon, lat, value;
            double[][] discretedData = new double[stNum][3];
            StationData stData = new StationData();
            //int n = 1;
            for (i = 0; i < stNum; i++) {
                lon = lonarray.getDouble(i);
                lat = latarray.getDouble(i);
                value = darray.getDouble(i) * scale_factor + add_offset;
//                if (MIMath.doubleEquals(value, missingValue)){
//                    continue;
//                }
//                stData.addData(String.valueOf(n), lon, lat, value);
                discretedData[i][0] = lon;
                discretedData[i][1] = lat;
                discretedData[i][2] = value;
                if (minx > lon) {
                    minx = lon;
                } else if (maxx < lon) {
                    maxx = lon;
                }
                if (miny > lat) {
                    miny = lat;
                } else if (maxy < lat) {
                    maxy = lat;
                }
            }

            stData.data = discretedData;
            stData.dataExtent = new Extent(minx, maxx, miny, maxy);
            stData.missingValue = missingValue;
            List<String> stations = new ArrayList<String>();
            for (i = 0; i < stNum; i++) {
                stations.add((String.valueOf(i + 1)));
            }
            stData.stations = stations;

            return stData;
        } catch (IOException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (InvalidRangeException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (null != ncfile) {
                try {
                    ncfile.close();
                } catch (IOException ex) {
                    Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public StationInfoData getStationInfoData(int timeIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public StationModelData getStationModelData(int timeIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Read array data of the variable
     *
     * @param varName Variable name
     * @return Array data
     */
    public Array read(String varName) {
        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(this.getFileName());
            ucar.nc2.Variable var = ncfile.findVariable(varName);

            Array data = var.read();

            return data;
        } catch (IOException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (null != ncfile) {
                try {
                    ncfile.close();
                } catch (IOException ex) {
                    Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * Read array data of the variable
     *
     * @param varName Variable name
     * @param origin The origin array
     * @param size The size array
     * @return Array data
     */
    public Array read(String varName, int[] origin, int[] size) {
        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(this.getFileName());
            ucar.nc2.Variable var = ncfile.findVariable(varName);

            Array data = var.read(origin, size).reduce();

            return data;
        } catch (IOException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (InvalidRangeException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (null != ncfile) {
                try {
                    ncfile.close();
                } catch (IOException ex) {
                    Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * Convert Array to GridData
     *
     * @param array The Array
     * @param xdim X dimension
     * @param ydim Y dimension
     * @return The grid data
     */
    public static GridData arrayToGrid(Array array, Dimension xdim, Dimension ydim) {
        int yNum = ydim.getDimLength();
        int xNum = xdim.getDimLength();
        double[][] gridData = new double[yNum][xNum];
        for (int i = 0; i < yNum; i++) {
            for (int j = 0; j < xNum; j++) {
                gridData[i][j] = array.getDouble(j * yNum + i);
            }
        }

        GridData aGridData = new GridData();
        aGridData.data = gridData;
        aGridData.xArray = xdim.getValues();
        aGridData.yArray = ydim.getValues();

        return aGridData;
    }

    /**
     * Convert grid data to NetCDF array 2D
     *
     * @param gData Grid data
     * @return NetCDF array 2D
     */
    public static Array gridToArray2D(GridData gData) {
        Array a = Array.factory(gData.data);
        int[] shape = new int[2];
        shape[0] = gData.getYNum();
        shape[1] = gData.getXNum();
        a = a.reshape(shape);

        return a;
    }

    /**
     * Convert grid data to NetCDF array 3D
     *
     * @param gData Grid data
     * @return NetCDF array 3D
     */
    public static Array gridToArray3D(GridData gData) {
        Array a = Array.factory(gData.data);
        int[] shape = new int[3];
        shape[0] = 1;
        shape[1] = gData.getYNum();
        shape[2] = gData.getXNum();
        a = a.reshape(shape);

        return a;
    }

    /**
     * Convert grid data to NetCDF array 4D
     *
     * @param gData Grid data
     * @return NetCDF array 4D
     */
    public static Array gridToArray4D(GridData gData) {
        Array a = Array.factory(gData.data);
        int[] shape = new int[4];
        shape[0] = 1;
        shape[1] = 1;
        shape[2] = gData.getYNum();
        shape[3] = gData.getXNum();
        a = a.reshape(shape);

        return a;
    }
    // </editor-fold>
    // <editor-fold desc="Write Data">
    //public ucar.nc2.Dimension 
    // </editor-fold>
    // </editor-fold>
}
