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
import org.meteoinfo.data.meteodata.IGridDataInfo;
import org.meteoinfo.data.meteodata.IStationDataInfo;
import org.meteoinfo.data.meteodata.StationInfoData;
import org.meteoinfo.data.meteodata.StationModelData;
import org.meteoinfo.data.meteodata.Variable;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.meteoinfo.data.ArrayMath;
import org.meteoinfo.data.GridArray;
import org.meteoinfo.data.meteodata.MeteoDataType;
import org.meteoinfo.global.util.DateUtil;
import org.meteoinfo.projection.KnownCoordinateSystems;
import org.meteoinfo.projection.ProjectionInfo;
import org.meteoinfo.projection.Reproject;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.ma2.Section;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author yaqiang
 */
public class NetCDFDataInfo extends DataInfo implements IGridDataInfo, IStationDataInfo {
    // <editor-fold desc="Variables">

    private String _fileTypeStr;
    private String _fileTypeId;
    private Conventions _convention = Conventions.CF;
    private NetcdfFile ncfile = null;
    private boolean keepOpen = false;
    private List<ucar.nc2.Variable> _variables = new ArrayList<>();
    private List<ucar.nc2.Dimension> _dimensions = new ArrayList<>();
    private List<Dimension> _miDims = new ArrayList<>();
    private List<ucar.nc2.Attribute> _gAtts = new ArrayList<>();
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
     * Get netCDF file
     * @return NetCDF file
     */
    public NetcdfFile getFile(){
        return this.ncfile;
    }

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
    @Override
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
        try {
            //ncfile = NetcdfFile.open(fileName);
            ncfile = NetcdfDataset.openFile(fileName, null);
            readDataInfo();
        } catch (IOException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Read data info for mixed GRIB-1 and GRIB-2 data file
     * @param fileName File name
     * @param mdt Meteo data type
     */
    public void readDataInfo(String fileName, MeteoDataType mdt) {
        this.setFileName(fileName);
        String iospClassName = "ucar.nc2.grib.collection.Grib2Iosp";
        switch (mdt){
            case GRIB1:
                iospClassName = "ucar.nc2.grib.collection.Grib1Iosp";
                break;
        }
        try {
            ncfile = NetcdfFile.open(fileName, iospClassName, 0, null, null);
            readDataInfo();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | IOException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    private void readDataInfo() {
        try {
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
                List<String> dimNames = new ArrayList<>();
                for (ucar.nc2.Variable var : _variables) {
                    for (ucar.nc2.Dimension dim : var.getDimensions()) {
                        String dimName = dim.getShortName();
                        if (dimName == null) {
                            continue;
                        }
                        if (!dimNames.contains(dimName)) {
                            dimNames.add(dimName);
                        }
                    }
                }
                for (ucar.nc2.Dimension dim : _dimensions) {
                    if (dim.getShortName().contains("_")) {
                        for (String dimName : dimNames) {
                            if (dim.getShortName().contains(dimName)) {
                                dim.setShortName(dimName);
                            }
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
            _miDims = new ArrayList<>();
            for (ucar.nc2.Dimension dim : _dimensions) {
                Dimension ndim = new Dimension(dim);
                if (dim.getShortName().equals("nXtrack")) {
                    ndim.setDimType(DimensionType.Xtrack);
                }
                _miDims.add(ndim);
            }

            //Read global attribute
            _gAtts = ncfile.getGlobalAttributes();
            String featureType = this.getGlobalAttStr("featureType");
            switch (featureType) {
                case "SWATH":
                    _isSWATH = true;
                    break;
                case "PROFILE":
                    _isPROFILE = true;
                    break;
            }

            //Get convention
            _convention = this.getConvention();

            //Get projection
            this.getProjection();

            //Get dimensions values
            getDimensionValues(ncfile);

            //Get variables
            List<Variable> vars = new ArrayList<>();
            //List<Dimension> coorDims = new ArrayList<Dimension>();
            for (ucar.nc2.Variable var : _variables) {
                Variable nvar = new Variable(var);
                //nvar.setName(var.getShortName());
                //nvar.setCoorVar(var.isCoordinateVariable());
                nvar.setDimVar(var.getRank() <= 1);
                if (_isSWATH || _isPROFILE) {
                    nvar.setStation(true);
                }

                nvar.getDimensions().clear();
                for (ucar.nc2.Dimension dim : var.getDimensions()) {
                    //Dimension ndim = this.getCoordDimension(dim);
                    int idx = this.getDimensionIndex(dim);
                    if (idx >= 0) {
                        Dimension ndim = _miDims.get(idx);
                        nvar.addDimension(ndim);
                    } else {
                        Dimension ndim = new Dimension(dim, DimensionType.Other);
                        nvar.addDimension(ndim);
                    }
                }

                //nvar.setAttributes(var.getAttributes());
                double[] packData = this.getPackData(var);
                nvar.setAddOffset(packData[0]);
                nvar.setScaleFactor(packData[1]);
                nvar.setFillValue(packData[2]);

                vars.add(nvar);
            }
            this.setVariables(vars);

            //
            //getVariableLevels();
        } catch (IOException | ParseException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (null != ncfile) {
                try {
                    ncfile.close();
                    ncfile = null;
                } catch (IOException ex) {
                    Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    @Override
    public void readDataInfo(String fileName, boolean keepOpen) {
        this.setFileName(fileName);
        try {
            //ncfile = NetcdfFile.open(fileName);
            ncfile = NetcdfDataset.openFile(fileName, null);
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
                List<String> dimNames = new ArrayList<>();
                for (ucar.nc2.Variable var : _variables) {
                    for (ucar.nc2.Dimension dim : var.getDimensions()) {
                        String dimName = dim.getShortName();
                        if (dimName == null) {
                            continue;
                        }
                        if (!dimNames.contains(dimName)) {
                            dimNames.add(dimName);
                        }
                    }
                }
                for (ucar.nc2.Dimension dim : _dimensions) {
                    if (dim.getShortName().contains("_")) {
                        for (String dimName : dimNames) {
                            if (dim.getShortName().contains(dimName)) {
                                dim.setShortName(dimName);
                            }
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
            _miDims = new ArrayList<>();
            for (ucar.nc2.Dimension dim : _dimensions) {
                Dimension ndim = new Dimension(dim);
                if (dim.getShortName().equals("nXtrack")) {
                    ndim.setDimType(DimensionType.Xtrack);
                }
                _miDims.add(ndim);
            }

            //Read global attribute
            _gAtts = ncfile.getGlobalAttributes();
            String featureType = this.getGlobalAttStr("featureType");
            switch (featureType) {
                case "SWATH":
                    _isSWATH = true;
                    break;
                case "PROFILE":
                    _isPROFILE = true;
                    break;
            }

            //Get convention
            _convention = this.getConvention();

            //Get projection
            this.getProjection();

            //Get dimensions values
            getDimensionValues(ncfile);

            //Get variables
            List<Variable> vars = new ArrayList<>();
            //List<Dimension> coorDims = new ArrayList<Dimension>();
            for (ucar.nc2.Variable var : _variables) {
                Variable nvar = new Variable(var);
                //nvar.setName(var.getShortName());
                //nvar.setCoorVar(var.isCoordinateVariable());
                nvar.setDimVar(var.getRank() <= 1);
                if (_isSWATH || _isPROFILE) {
                    nvar.setStation(true);
                }

                nvar.getDimensions().clear();
                for (ucar.nc2.Dimension dim : var.getDimensions()) {
                    //Dimension ndim = this.getCoordDimension(dim);
                    int idx = this.getDimensionIndex(dim);
                    if (idx >= 0) {
                        Dimension ndim = _miDims.get(idx);
                        nvar.addDimension(ndim);
                    } else {
                        Dimension ndim = new Dimension(dim, DimensionType.Other);
                        nvar.addDimension(ndim);
                    }
                }

                //nvar.setAttributes(var.getAttributes());
                double[] packData = this.getPackData(var);
                nvar.setAddOffset(packData[0]);
                nvar.setScaleFactor(packData[1]);
                nvar.setFillValue(packData[2]);

                vars.add(nvar);
            }
            this.setVariables(vars);

            //
            //getVariableLevels();
        } catch (IOException | ParseException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            this.keepOpen = keepOpen;
            if (!keepOpen){
                if (null != ncfile) {
                    try {
                        ncfile.close();
                        ncfile = null;
                    } catch (IOException ex) {
                        Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        this.keepOpen = true;
    }

    private int getDimensionIndex(ucar.nc2.Dimension dim) {
        String name2 = dim.getShortName();
        if (name2 == null) {
            return -1;
        }

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
                if (len1 > len2) {
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
            if (dim.getShortName().equalsIgnoreCase(dimName)) {
                return dim;
            }
        }
        return null;
    }

    /**
     * Find global attribute
     *
     * @param attName Attribute name
     * @return Global attribute
     */
    public ucar.nc2.Attribute findGlobalAttribute(String attName) {
        for (ucar.nc2.Attribute att : this._gAtts) {
            if (att.getShortName().equalsIgnoreCase(attName)) {
                return att;
            }
        }

        return null;
    }

    private Dimension findCoordDimension(ucar.nc2.Dimension dim) {
        if (dim.getShortName().equals(this.getXDimension().getShortName())) {
            return this.getXDimension();
        } else if (dim.getShortName().equals(this.getYDimension().getShortName())) {
            return this.getYDimension();
        } else if (dim.getShortName().equals(this.getZDimension().getShortName())) {
            return this.getZDimension();
        } else if (dim.getShortName().equals(this.getTimeDimension().getShortName())) {
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
                if (!this.getGlobalAttStr("MAP_PROJ").isEmpty()) {
                    isWRFOUT = true;
                    break;
                }
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
        ProjectionInfo projInfo = this.getProjectionInfo();
        switch (_convention) {
            case CF:
                projInfo = getProjection_CF();
                break;
            case IOAPI:
                projInfo = getProjection_IOAPI();
                break;
            case WRFOUT:
                projInfo = getProjection_WRFOUT();
                break;
        }
        this.setProjectionInfo(projInfo);
    }

    private ProjectionInfo getProjection_CF() {
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
                } else if (!this._fileTypeId.equals("HDF5-EOS")) {
                    this.setYReverse(true);
                }

                Dimension xDim = this.findDimension("XDim");
                Dimension yDim = this.findDimension("YDim");
                int xnum = xDim.getLength();
                int ynum = yDim.getLength();
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
                switch (attStr) {
                    case "albers_conical_equal_area": {
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
                        break;
                    }
                    case "azimuthal_equidistant":
                        projStr = "+proj=aeqd"
                                + "+lon_0=" + pVar.findAttribute("longitude_of_projection_origin").getValue(0).toString()
                                + "+lat_0=" + pVar.findAttribute("latitude_of_projection_origin").getValue(0).toString();
                        if (pVar.findAttribute("false_easting") != null) {
                            projStr += "+x_0=" + pVar.findAttribute("false_easting").getValue(0).toString();
                        }
                        if (pVar.findAttribute("false_northing") != null) {
                            projStr += "+y_0=" + pVar.findAttribute("false_northing").getValue(0).toString();
                        }
                        break;
                    case "lambert_azimuthal_equal_area":
                        projStr = "+proj=laea"
                                + "+lon_0=" + pVar.findAttribute("longitude_of_projection_origin").getValue(0).toString()
                                + "+lat_0=" + pVar.findAttribute("latitude_of_projection_origin").getValue(0).toString();
                        if (pVar.findAttribute("false_easting") != null) {
                            projStr += "+x_0=" + pVar.findAttribute("false_easting").getValue(0).toString();
                        }
                        if (pVar.findAttribute("false_northing") != null) {
                            projStr += "+y_0=" + pVar.findAttribute("false_northing").getValue(0).toString();
                        }
                        break;
                    case "lambert_conformal_conic": {
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
                        break;
                    }
                    case "lambert_cylindrical_equal_area":
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
                        break;
                    case "mercator":
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
                        break;
                    case "orthographic":
                        projStr = "+proj=ortho"
                                + "+lon_0=" + pVar.findAttribute("longitude_of_projection_origin").getValue(0).toString()
                                + "+lat_0=" + pVar.findAttribute("latitude_of_projection_origin").getValue(0).toString();
                        if (pVar.findAttribute("false_easting") != null) {
                            projStr += "+x_0=" + pVar.findAttribute("false_easting").getValue(0).toString();
                        }
                        if (pVar.findAttribute("false_northing") != null) {
                            projStr += "+y_0=" + pVar.findAttribute("false_northing").getValue(0).toString();
                        }
                        break;
                    case "polar_stereographic":
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
                        break;
                    case "rotated_latitude_longitude":
                        break;
                    case "stereographic":
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
                        break;
                    case "transverse_mercator":
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
                        break;
                    case "vertical_perspective":
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
                        break;
                }
            }
        }

        try {
            ProjectionInfo projInfo = new ProjectionInfo(projStr);
            return projInfo;
        } catch (Exception e){
            return KnownCoordinateSystems.geographic.world.WGS1984;
        }
    }

    private ProjectionInfo getProjection_IOAPI() {
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

        return new ProjectionInfo(projStr);
    }

    private ProjectionInfo getProjection_WRFOUT() {
        String projStr = this.getProjectionInfo().toProj4String();
        String pstr = this.getGlobalAttStr("MAP_PROJ");
        if (pstr.isEmpty()) {
            return this.getProjectionInfo();
        }

        int mapProj = Integer.parseInt(pstr);
        String lon_0 = getGlobalAttStr("STAND_LON");
        if (lon_0.isEmpty())
            lon_0 = getGlobalAttStr("CEN_LON");
        switch (mapProj) {
            case 1:    //Lambert conformal
                projStr = "+proj=lcc"
                        + "+lon_0=" + lon_0
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
                        + "+lon_0=" + lon_0
                        + "+lat_0=" + lat0;
                String stPs = getGlobalAttStr("CEN_LAT");
                //projStr += "+lat_ts=" + stPs;                                    
                double k0 = ProjectionInfo.calScaleFactorFromStandardParallel(Double.parseDouble(stPs));
                projStr += "+k=" + String.valueOf(k0);
                break;
            case 3:    //Mercator
                projStr = "+proj=merc"
                        + "+lat_ts=" + getGlobalAttStr("CEN_LAT")
                        + "+lon_0=" + lon_0;
                break;
        }

        ProjectionInfo projInfo = new ProjectionInfo(projStr);
        double clon = Double.parseDouble(this.getGlobalAttStr("CEN_LON"));
        double clat = Double.parseDouble(this.getGlobalAttStr("CEN_LAT"));
        projInfo.setCenterLat(clat);
        projInfo.setCenterLon(clon);
        return projInfo;
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
        List<Date> times = new ArrayList<>();
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
            if (unitsStr.contains("%")){
                return null;
            }
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
            if (var != null && var.getDimensions().size() == 1) {
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

                int n = (int)darray.getSize();
                double[] values = new double[n];
                for (int i = 0; i < n; i++) {
                    values[i] = darray.getDouble(i);
                }
                List<Date> times = this.getTimes(var, values);
                List<Double> ts = new ArrayList<>();
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
                if (!var.getDataType().isNumeric())
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
                if (values.length > 1) {
                    if (values[0] > values[1]) {
                        switch (dimType) {
                            case X:
                                this.setXReverse(true);
                                dim.setReverse(true);
                                MIMath.arrayReverse(values);
                                break;
                            case Y:
                                this.setYReverse(true);
                                dim.setReverse(true);
                                MIMath.arrayReverse(values);
                                break;
                        }
                    }
                }
                //aDim.setValues(values);
                switch (dimType) {
                    case X:
                        double[] X = values;
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
                        if (times != null){
                            List<Double> ts = new ArrayList<>();
                            for (Date t : times) {
                                ts.add(DateUtil.toOADate(t));
                            }
                            dim.setValues(ts);
                        }
                        this.setTimeDimension(dim);
                        break;
                    default:
                        dim.setValues(values);
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
        try {
            cal.set(Integer.parseInt(sDateStr.substring(0, 4)), 0, 1, 0, 0, 0);
            if (MIMath.isNumeric(sDateStr.substring(4)))
                cal.add(Calendar.DAY_OF_YEAR, Integer.parseInt(sDateStr.substring(4)) - 1);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
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
        List<Date> times = new ArrayList<>();
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
        List<Double> values = new ArrayList<>();
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
        double orgLon, orgLat, orgX, orgY;
        Dimension xDim = this.findDimension("west_east");
        Dimension yDim = this.findDimension("south_north");
        if (xDim == null || yDim == null){
            return;
        }
        xDim.setDimType(DimensionType.X);
        yDim.setDimType(DimensionType.Y);
        int xNum = xDim.getLength();
        int yNum = yDim.getLength();

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
            int lNum = zDim.getLength();
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
            int lNum = zDim.getLength();
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
            int lNum = zDim.getLength();
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
                int tNum = tDim.getLength();
                ucar.nc2.Dimension tsDim = ncfile.findDimension("DateStrLen");
                int strLen = tsDim.getLength();
                char[] charData = new char[tNum * strLen];
                Array tarray = aVarS.read();
                for (i = 0; i < tNum * strLen; i++) {
                    charData[i] = tarray.getChar(i);
                }

                String tStr;
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                List<Date> times = new ArrayList<>();
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
                List<Double> values = new ArrayList<>();
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
            dataLen = dataLen * aVarS.getDimensions().get(i).getLength();
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
            switch (sName) {
                case "longitude":
                    dimType = DimensionType.X;
                    break;
                case "latitude":
                    dimType = DimensionType.Y;
                    break;
                case "pressure":
                    dimType = DimensionType.Z;
                    break;
                case "time":
                    dimType = DimensionType.T;
                    break;
            }
        } else {
            if (aVar.findAttributeIgnoreCase("standard_name") != null) {
                ucar.nc2.Attribute axisAtt = aVar.findAttributeIgnoreCase("standard_name");
                sName = axisAtt.getStringValue().trim().toLowerCase();
                switch (sName) {
                    case "longitude":
                    case "projection_x_coordinate":
                    case "longitude_east":
                        dimType = DimensionType.X;
                        break;
                    case "latitude":
                    case "projection_y_coordinate":
                    case "latitude_north":
                        dimType = DimensionType.Y;
                        break;
                    case "time":
                        dimType = DimensionType.T;
                        break;
                    case "level":
                        dimType = DimensionType.Z;
                        break;
                }
            }
            if (dimType == DimensionType.Other) {
                if (aVar.findAttributeIgnoreCase("long_name") != null) {
                    ucar.nc2.Attribute axisAtt = aVar.findAttributeIgnoreCase("long_name");
                    sName = axisAtt.getStringValue().trim().toLowerCase();
                    switch (sName) {
                        case "longitude":
                        case "coordinate longitude":
                        case "x":
                            dimType = DimensionType.X;
                            break;
                        case "latitude":
                        case "coordinate latitude":
                        case "y":
                            dimType = DimensionType.Y;
                            break;
                        case "time":
                        case "initial time":
                            dimType = DimensionType.T;
                            break;
                        case "level":
                        case "pressure":
                        case "pressure_level":
                        case "isobaric surface":
                            dimType = DimensionType.Z;
                            break;
                        default:
                            if (sName.contains("level") || sName.contains("depths")) {
                                dimType = DimensionType.Z;
                            }
                            break;
                    }
                }
            }
            if (dimType == DimensionType.Other) {
                if (aVar.findAttributeIgnoreCase("axis") != null) {
                    ucar.nc2.Attribute axisAtt = aVar.findAttributeIgnoreCase("axis");
                    sName = axisAtt.getStringValue().trim().toLowerCase();
                    switch (sName) {
                        case "x":
                            dimType = DimensionType.X;
                            break;
                        case "y":
                            dimType = DimensionType.Y;
                            break;
                        case "z":
                            dimType = DimensionType.Z;
                            break;
                        case "t":
                            dimType = DimensionType.T;
                            break;
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
                    switch (sName) {
                        case "longitude":
                            dimType = DimensionType.X;
                            break;
                        case "latitude":
                            dimType = DimensionType.Y;
                            break;
                    }
                }
            }
            if (dimType == DimensionType.Other) {
                String vName = aVar.getShortName().toLowerCase();
                switch (vName) {
                    case "lon":
                    case "longitude":
                    case "x":
                        dimType = DimensionType.X;
                        break;
                    case "lat":
                    case "latitude":
                    case "y":
                        dimType = DimensionType.Y;
                        break;
                    case "time":
                        dimType = DimensionType.T;
                        break;
                    case "level":
                    case "lev":
                    case "height":
                    case "isobaric":
                    case "pressure":
                    case "depth":
                        dimType = DimensionType.Z;
                        break;
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
        } else if (tu.length() == 1) {
            String str = tu.toLowerCase();
            switch (str) {
                case "y":
                    aTU = TimeUnit.Year;
                    break;
                case "d":
                    aTU = TimeUnit.Day;
                    break;
                case "h":
                    aTU = TimeUnit.Hour;
                    break;
                case "s":
                    aTU = TimeUnit.Second;
                    break;
            }
        } else {
            String str = tu.toLowerCase().substring(0, 2);
            switch (str) {
                case "yr":
                case "ye":
                    aTU = TimeUnit.Year;
                    break;
                case "mo":
                    aTU = TimeUnit.Month;
                    break;
                case "da":
                    aTU = TimeUnit.Day;
                    break;
                case "hr":
                case "ho":
                    aTU = TimeUnit.Hour;
                    break;
                case "mi":
                    aTU = TimeUnit.Minute;
                    break;
                case "se":
                    aTU = TimeUnit.Second;
                    break;
            }
        }

        return aTU;
    }

    private Date getStartTime(String tStr) {
        Date sTime = new Date();
        tStr = tStr.trim();
        String[] dataArray;

        dataArray = tStr.split("\\s+");

        if (dataArray.length < 3) {
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
            String[] darray1 = ST.split("-");
            year = Integer.parseInt(darray1[0]);
            month = Integer.parseInt(darray1[1]);
            if (darray1[2].length() > 2)
                darray1[2] = darray1[2].substring(0, 2);
            day = Integer.parseInt(darray1[2]);
            if (dataArray.length >= 4) {
                String hmsStr = dataArray[3];
                hmsStr = hmsStr.replace("0.0", "00");
                try {
                    hour = Integer.parseInt(hmsStr.split(":")[0]);
                    min = Integer.parseInt(hmsStr.split(":")[1]);
                    sec = Integer.parseInt(hmsStr.split(":")[2]);
                } catch (NumberFormatException e) {
                }
            }
        } else if (ST.contains(":")) {
            String hmsStr = ST;
            hmsStr = hmsStr.replace("0.0", "00");
            try {
                hour = Integer.parseInt(hmsStr.split(":")[0]);
                min = Integer.parseInt(hmsStr.split(":")[1]);
                sec = Integer.parseInt(hmsStr.split(":")[2]);
            } catch (Exception e) {
            }
        }

        if (year == 0) {
            year = 1;
        }

        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, hour, min, sec);
        cal.set(Calendar.MILLISECOND, 0);
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
                    + String.valueOf(xdim.getLength()) + "; Xdelta = " + String.valueOf(xdim.getDeltaValue());
        }
        Dimension ydim = this.getYDimension();
        if (ydim != null) {
            dataInfo += System.getProperty("line.separator") + "Y Dimension: Ymin = " + String.valueOf(ydim.getMinValue())
                    + "; Ymax = " + String.valueOf(ydim.getMaxValue()) + "; Ysize = "
                    + String.valueOf(ydim.getLength()) + "; Ydelta = " + String.valueOf(ydim.getDeltaValue());
        }

        dataInfo += System.getProperty("line.separator") + "Global Attributes: ";
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

//        //Adjust undefine data
//        if (Double.isNaN(missingValue)) {
//            missingValue = this.getMissingValue();
//        } else {
//            missingValue = missingValue * scale_factor + add_offset;
//        }
        return new double[]{add_offset, scale_factor, missingValue};
    }
    
    /**
     * Get grid data
     *
     * @param varName Variable name
     * @return Grid data
     */
    @Override
    public GridArray getGridArray(String varName) {
        return null;    
    }

    @Override
    public GridData getGridData_LonLat(int timeIdx, int varIdx, int levelIdx) {
        try {
            if (ncfile == null)
                ncfile = NetcdfDataset.openFile(this.getFileName(), null);

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
            xNum = nvar.getXDimension().getLength();
            yNum = nvar.getYDimension().getLength();
            double[][] gridData = new double[yNum][xNum];

            int rank = var.getRank();
            int[] origin = new int[rank];
            int[] size = new int[rank];
            int xdimIdx = 0;
            int ydimIdx = 0;
            for (i = 0; i < rank; i++) {
                ucar.nc2.Dimension dim = var.getDimension(i);
                Dimension ndim = (Dimension)nvar.getDimension(i);
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

            double v;
            if (ydimIdx < xdimIdx) {
                for (i = 0; i < yNum; i++) {
                    for (j = 0; j < xNum; j++) {
                        v = data2D.getDouble(i * xNum + j);
                        if (v == missingValue) {
                            gridData[i][j] = v;
                        } else {
                            gridData[i][j] = v * scale_factor + add_offset;
                        }
                    }
                }
            } else {
                for (i = 0; i < yNum; i++) {
                    for (j = 0; j < xNum; j++) {
                        v = data2D.getDouble(j * yNum + i);
                        if (v == missingValue) {
                            gridData[i][j] = v;
                        } else {
                            gridData[i][j] = v * scale_factor + add_offset;
                        }
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
        } catch (IOException | InvalidRangeException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (!this.keepOpen && null != ncfile) {
                try {
                    ncfile.close();
                    ncfile = null;
                } catch (IOException ex) {
                    Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public GridData getGridData_TimeLat(int lonIdx, int varIdx, int levelIdx) {
        try {
            if (ncfile == null)
                ncfile = NetcdfDataset.openFile(this.getFileName(), null);            

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
            int xNum, yNum;
            xNum = nvar.getYDimension().getLength();
            yNum = nvar.getTDimension().getLength();
            double[][] gridData = new double[yNum][xNum];
            int rank = var.getRank();
            int[] origin = new int[rank];
            int[] size = new int[rank];
            for (i = 0; i < rank; i++) {
                Dimension ndim = (Dimension)nvar.getDimension(i);
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

            double v;
            for (i = 0; i < yNum; i++) {
                for (j = 0; j < xNum; j++) {
                    v = data2D.getDouble(i * xNum + j);
                    if (v == missingValue) {
                        gridData[i][j] = v;
                    } else {
                        gridData[i][j] = v * scale_factor + add_offset;
                    }
                }
            }

            GridData aGridData = new GridData();
            aGridData.data = gridData;
            aGridData.xArray = nvar.getYDimension().getValues();
            aGridData.yArray = nvar.getTDimension().getValues();
            aGridData.missingValue = missingValue;

            if (this.isYReverse()) {
                aGridData.xReverse();
            }

            return aGridData;
        } catch (IOException | InvalidRangeException ex) {
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
        try {
            if (ncfile == null)
                ncfile = NetcdfDataset.openFile(this.getFileName(), null);            

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
            int xNum, yNum;
            xNum = nvar.getXDimension().getLength();
            yNum = nvar.getTDimension().getLength();
            double[][] gridData = new double[yNum][xNum];
            int rank = var.getRank();
            int[] origin = new int[rank];
            int[] size = new int[rank];
            for (i = 0; i < rank; i++) {
                Dimension ndim = (Dimension)nvar.getDimension(i);
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

            double v;
            for (i = 0; i < yNum; i++) {
                for (j = 0; j < xNum; j++) {
                    v = data2D.getDouble(i * xNum + j);
                    if (v == missingValue) {
                        gridData[i][j] = v;
                    } else {
                        gridData[i][j] = v * scale_factor + add_offset;
                    }
                }
            }

            GridData aGridData = new GridData();
            aGridData.data = gridData;
            aGridData.xArray = nvar.getYDimension().getValues();
            aGridData.yArray = nvar.getTDimension().getValues();
            aGridData.missingValue = missingValue;

            return aGridData;
        } catch (IOException | InvalidRangeException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (!this.keepOpen && null != ncfile) {
                try {
                    ncfile.close();
                    ncfile = null;
                } catch (IOException ex) {
                    Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public GridData getGridData_LevelLat(int lonIdx, int varIdx, int timeIdx) {
        try {
            if (ncfile == null)
                ncfile = NetcdfDataset.openFile(this.getFileName(), null);            

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
            int xNum, yNum;
            xNum = nvar.getYDimension().getLength();
            yNum = nvar.getZDimension().getLength();
            double[][] gridData = new double[yNum][xNum];
            
            int rank = var.getRank();
            int[] origin = new int[rank];
            int[] size = new int[rank];
            for (i = 0; i < rank; i++) {
                Dimension ndim = (Dimension)nvar.getDimension(i);
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

            double v;
            for (i = 0; i < yNum; i++) {
                for (j = 0; j < xNum; j++) {
                    v = data2D.getDouble(i * xNum + j);
                    if (v == missingValue) {
                        gridData[i][j] = v;
                    } else {
                        gridData[i][j] = v * scale_factor + add_offset;
                    }
                }
            }

            GridData aGridData = new GridData();
            aGridData.data = gridData;
            aGridData.xArray = nvar.getYDimension().getValues();
            aGridData.yArray = nvar.getZDimension().getValues();
            aGridData.missingValue = missingValue;
            
            if (this.isYReverse()){
                aGridData.xReverse();
            }

            return aGridData;
        } catch (IOException | InvalidRangeException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (!this.keepOpen && null != ncfile) {
                try {
                    ncfile.close();
                    ncfile = null;
                } catch (IOException ex) {
                    Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public GridData getGridData_LevelLon(int latIdx, int varIdx, int timeIdx) {
        try {
            if (ncfile == null)
                ncfile = NetcdfDataset.openFile(this.getFileName(), null);            

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
            int xNum, yNum;
            xNum = nvar.getXDimension().getLength();
            yNum = nvar.getZDimension().getLength();
            double[][] gridData = new double[yNum][xNum];
            int rank = var.getRank();
            int[] origin = new int[rank];
            int[] size = new int[rank];
            for (i = 0; i < rank; i++) {
                Dimension ndim = (Dimension)nvar.getDimension(i);
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

            double v;
            for (i = 0; i < yNum; i++) {
                for (j = 0; j < xNum; j++) {
                    v = data2D.getDouble(i * xNum + j);
                    if (v == missingValue) {
                        gridData[i][j] = v;
                    } else {
                        gridData[i][j] = v * scale_factor + add_offset;
                    }
                }
            }

            GridData aGridData = new GridData();
            aGridData.data = gridData;
            aGridData.xArray = nvar.getXDimension().getValues();
            aGridData.yArray = nvar.getZDimension().getValues();
            aGridData.missingValue = missingValue;

            return aGridData;
        } catch (IOException | InvalidRangeException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (!this.keepOpen && null != ncfile) {
                try {
                    ncfile.close();
                    ncfile = null;
                } catch (IOException ex) {
                    Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public GridData getGridData_LevelTime(int latIdx, int varIdx, int lonIdx) {
        try {
            if (ncfile == null)
                ncfile = NetcdfDataset.openFile(this.getFileName(), null);            

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
            int xNum, yNum;
            xNum = nvar.getTDimension().getLength();
            yNum = nvar.getZDimension().getLength();
            double[][] gridData = new double[yNum][xNum];
            int rank = var.getRank();
            int[] origin = new int[rank];
            int[] size = new int[rank];
            for (i = 0; i < rank; i++) {
                Dimension ndim = (Dimension)nvar.getDimension(i);
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
            if (data2D.getShape()[0] == xNum){
                data2D = data2D.transpose(0, 1);
            }

            Index index = data2D.getIndex();
            double v;
            for (i = 0; i < yNum; i++) {
                for (j = 0; j < xNum; j++) {
                    v = data2D.getDouble(index);
                    if (v == missingValue) {
                        gridData[i][j] = v;
                    } else {
                        gridData[i][j] = v * scale_factor + add_offset;
                    }
                    index.incr();
                }
            }

            GridData aGridData = new GridData();
            aGridData.data = gridData;
            aGridData.xArray = nvar.getTDimension().getValues();
            aGridData.yArray = nvar.getZDimension().getValues();
            aGridData.missingValue = missingValue;

            return aGridData;
        } catch (IOException | InvalidRangeException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (!this.keepOpen && null != ncfile) {
                try {
                    ncfile.close();
                    ncfile = null;
                } catch (IOException ex) {
                    Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public GridData getGridData_Time(int lonIdx, int latIdx, int varIdx, int levelIdx) {
        try {
            if (ncfile == null)
                ncfile = NetcdfDataset.openFile(this.getFileName(), null);

            int i;
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
            int dNum = nvar.getTDimension().getLength();
            double[][] gridData = new double[1][dNum];
            int rank = var.getRank();
            int[] origin = new int[rank];
            int[] size = new int[rank];
            for (i = 0; i < rank; i++) {
                Dimension ndim = (Dimension)nvar.getDimension(i);
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

            double v;
            for (i = 0; i < dNum; i++) {
                v = data1D.getDouble(i);
                if (v == missingValue) {
                    gridData[0][1] = v;
                } else {
                    gridData[0][i] = v * scale_factor + add_offset;
                }
            }

            GridData aGridData = new GridData();
            aGridData.data = gridData;
            aGridData.xArray = nvar.getTDimension().getValues();
            aGridData.yArray = new double[1];
            aGridData.missingValue = missingValue;

            return aGridData;
        } catch (IOException | InvalidRangeException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (!this.keepOpen && null != ncfile) {
                try {
                    ncfile.close();
                    ncfile = null;
                } catch (IOException ex) {
                    Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public GridData getGridData_Level(int lonIdx, int latIdx, int varIdx, int timeIdx) {
        try {
            if (ncfile == null)
                ncfile = NetcdfDataset.openFile(this.getFileName(), null);            

            int i;
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
            int dNum = nvar.getZDimension().getLength();
            double[][] gridData = new double[1][dNum];
            int rank = var.getRank();
            int[] origin = new int[rank];
            int[] size = new int[rank];
            for (i = 0; i < rank; i++) {
                Dimension ndim = (Dimension)nvar.getDimension(i);
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

            double v;
            for (i = 0; i < dNum; i++) {
                v = data1D.getDouble(i);
                if (v == missingValue) {
                    gridData[0][1] = v;
                } else {
                    gridData[0][i] = v * scale_factor + add_offset;
                }
            }

            GridData aGridData = new GridData();
            aGridData.data = gridData;
            aGridData.xArray = nvar.getZDimension().getValues();
            aGridData.yArray = new double[1];
            aGridData.missingValue = missingValue;

            return aGridData;
        } catch (IOException | InvalidRangeException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (!this.keepOpen && null != ncfile) {
                try {
                    ncfile.close();
                    ncfile = null;
                } catch (IOException ex) {
                    Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public GridData getGridData_Lon(int timeIdx, int latIdx, int varIdx, int levelIdx) {
        try {
            if (ncfile == null)
                ncfile = NetcdfDataset.openFile(this.getFileName(), null);            

            int i;
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
            int dNum = nvar.getXDimension().getLength();
            double[][] gridData = new double[1][dNum];
            int rank = var.getRank();
            int[] origin = new int[rank];
            int[] size = new int[rank];
            for (i = 0; i < rank; i++) {
                Dimension ndim = (Dimension)nvar.getDimension(i);
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

            double v;
            for (i = 0; i < dNum; i++) {
                v = data1D.getDouble(i);
                if (v == missingValue) {
                    gridData[0][i] = v;
                } else {
                    gridData[0][i] = v * scale_factor + add_offset;
                }
            }

            GridData aGridData = new GridData();
            aGridData.data = gridData;
            aGridData.xArray = nvar.getXDimension().getValues();
            aGridData.yArray = new double[1];
            aGridData.missingValue = missingValue;

            return aGridData;
        } catch (IOException | InvalidRangeException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (!this.keepOpen && null != ncfile) {
                try {
                    ncfile.close();
                    ncfile = null;
                } catch (IOException ex) {
                    Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public GridData getGridData_Lat(int timeIdx, int lonIdx, int varIdx, int levelIdx) {
        try {
            if (ncfile == null)
                ncfile = NetcdfDataset.openFile(this.getFileName(), null);            

            int i;
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
            int dNum = nvar.getYDimension().getLength();
            double[][] gridData = new double[1][dNum];
            int rank = var.getRank();
            int[] origin = new int[rank];
            int[] size = new int[rank];
            for (i = 0; i < rank; i++) {
                Dimension ndim = (Dimension)nvar.getDimension(i);
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

            double v;
            for (i = 0; i < dNum; i++) {
                v = data1D.getDouble(i);
                if (v == missingValue) {
                    gridData[0][i] = v;
                } else {
                    gridData[0][i] = v * scale_factor + add_offset;
                }
            }

            GridData aGridData = new GridData();
            aGridData.data = gridData;
            aGridData.xArray = nvar.getYDimension().getValues();
            aGridData.yArray = new double[1];
            aGridData.missingValue = missingValue;

            return aGridData;
        } catch (IOException | InvalidRangeException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (!this.keepOpen && null != ncfile) {
                try {
                    ncfile.close();
                    ncfile = null;
                } catch (IOException ex) {
                    Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public StationData getStationData(int timeIdx, int varIdx, int levelIdx) {
        try {
            if (ncfile == null)
                ncfile = NetcdfFile.open(this.getFileName());

            int i;
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
                    Dimension ndim = (Dimension)nvar.getDimension(i);
                    ucar.nc2.Dimension dim = var.getDimension(i);
                    switch (ndim.getDimType()) {
                        case T:
                            if (this._isPROFILE){
                                origin[i] = timeIdx;
                                size[i] = ndim.getLength();
                            } else {
                                origin[i] = timeIdx;
                                size[i] = 1;
                            }
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
            double v;
            for (i = 0; i < stNum; i++) {
                lon = lonarray.getDouble(i);
                lat = latarray.getDouble(i);
                v = darray.getDouble(i);
                if (v == missingValue) {
                    value = v;
                } else {
                    value = v * scale_factor + add_offset;
                }
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
            List<String> stations = new ArrayList<>();
            for (i = 0; i < stNum; i++) {
                stations.add((String.valueOf(i + 1)));
            }
            stData.stations = stations;

            return stData;
        } catch (IOException | InvalidRangeException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (!this.keepOpen && null != ncfile) {
                try {
                    ncfile.close();
                    ncfile = null;
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
    @Override
    public Array read(String varName) {
        try {
            if (ncfile == null)
                ncfile = NetcdfFile.open(this.getFileName());
            ucar.nc2.Variable var = ncfile.findVariable(varName);

            Array data = var.read();

            return data;
        } catch (IOException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (!this.keepOpen && null != ncfile) {
                try {
                    ncfile.close();
                    ncfile = null;
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
     * @param stride The stride array
     * @return Array data
     */
    @Override
    public Array read(String varName, int[] origin, int[] size, int[] stride) {
        try {
            if (ncfile == null)
                ncfile = NetcdfDataset.openFile(this.getFileName(), null);
            ucar.nc2.Variable var = ncfile.findVariable(varName);
            if (var == null) {
                List<ucar.nc2.Variable> vars = ncfile.getVariables();
                for (ucar.nc2.Variable v : vars) {
                    if (v.getShortName().equals(varName)) {
                        var = v;
                        break;
                    }
                }
            }

            if (var == null) {
                System.out.println("Variable not exist: " + varName);
                return null;
            }

            boolean negStride = false;
            for (int s : stride) {
                if (s < 0) {
                    negStride = true;
                    break;
                }
            }

            Array data;
            if (negStride) {
                int[] pStride = new int[stride.length];
                List<Integer> flips = new ArrayList<>();
                for (int i = 0; i < stride.length; i++) {
                    pStride[i] = Math.abs(stride[i]);
                    if (stride[i] < 0) {
                        flips.add(i);
                    }
                }
                Section section = new Section(origin, size, pStride);
                Array r = var.read(section);
                for (int i : flips) {
                    r = r.flip(i);
                }
                data = Array.factory(r.getDataType(), r.getShape());
                MAMath.copy(data, r);
            } else {
                Section section = new Section(origin, size, stride);
                data = var.read(section);
            }

            //Get pack info
            double add_offset, scale_factor, missingValue;
            double[] packData = this.getPackData(var);
            add_offset = packData[0];
            scale_factor = packData[1];
            missingValue = packData[2];
            if (add_offset != 0 || scale_factor != 1) {
                //ArrayMath.fill_value = missingValue;
                data = ArrayMath.add(ArrayMath.mul(data, scale_factor), add_offset);
            }

            return data;
        } catch (IOException | InvalidRangeException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (!this.keepOpen && null != ncfile) {
                try {
                    ncfile.close();
                    ncfile = null;
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
        try {
            if (ncfile == null)
                ncfile = NetcdfFile.open(this.getFileName());
            ucar.nc2.Variable var = ncfile.findVariable(varName);

            Section section = new Section(origin, size);
            Array data = var.read(section);

            return data;
        } catch (IOException | InvalidRangeException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (!this.keepOpen && null != ncfile) {
                try {
                    ncfile.close();
                    ncfile = null;
                } catch (IOException ex) {
                    Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public Array read(String varName, String key){
        try {
            if (ncfile == null)
                ncfile = NetcdfFile.open(this.getFileName());
            ucar.nc2.Variable var = ncfile.findVariable(varName);

            Array data = var.read(key);

            return data;
        } catch (IOException | InvalidRangeException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (!this.keepOpen && null != ncfile) {
                try {
                    ncfile.close();
                    ncfile = null;
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
        int yNum = ydim.getLength();
        int xNum = xdim.getLength();
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
    /**
     * Create netCDF file
     *
     * @param fileName File name
     * @return NetcdfFileWriter
     * @throws java.io.IOException
     */
    public NetcdfFileWriter createNCFile(String fileName) throws IOException {
        NetcdfFileWriter ncfilew = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, fileName);

        //Define dimensions
        for (Dimension dim : this._miDims) {
            ncfilew.addDimension(null, dim.getShortName(), dim.getLength(), dim.isShared(),
                    dim.isUnlimited(), dim.isVariableLength());
        }

        //Define global attributes
        for (ucar.nc2.Attribute attr : this._gAtts) {
            ncfilew.addGroupAttribute(null, attr);
        }

        //Define variables
        for (ucar.nc2.Variable var : this._variables) {
            ucar.nc2.Variable nvar = ncfilew.addVariable(null, var.getShortName(), var.getDataType(), var.getDimensions());
            for (ucar.nc2.Attribute attr : var.getAttributes()) {
                nvar.addAttribute(attr);
            }
        }

        //Create netCDF file
        ncfilew.create();

        return ncfilew;
    }

    /**
     * Join netCDF data files
     *
     * @param inFiles Input netCDF data files
     * @param outFile Output netCDF data file
     * @param tDimName Time dimension name
     */
    public static void joinDataFiles(List<String> inFiles, String outFile, String tDimName) {
        //Check number of selected files
        int fNum = inFiles.size();
        if (fNum < 2) {
            JOptionPane.showMessageDialog(null, "There should be at least two files!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //Check top two files to decide joining time or variables
        String aFile = inFiles.get(0);
        String bFile = inFiles.get(1);

        NetCDFDataInfo aDataInfo = new NetCDFDataInfo();
        NetCDFDataInfo bDataInfo = new NetCDFDataInfo();

        aDataInfo.readDataInfo(aFile);
        bDataInfo.readDataInfo(bFile);

        //If can be joined
        int dataJoinType = getDataJoinType(aDataInfo, bDataInfo, tDimName);
        if (dataJoinType == 0) {
            JOptionPane.showMessageDialog(null, "Data dimensions are not same!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //Join data
        if (dataJoinType == 2) //Join variables
        {
            try {
                joinDataFiles_Variable(inFiles, outFile);
            } catch (IOException | InvalidRangeException ex) {
                Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else //Join time
        {
            try {
                joinDataFiles_Time(inFiles, outFile, tDimName);
            } catch (IOException | InvalidRangeException | ParseException ex) {
                Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Join data files by time
     *
     * @param inFiles Input nc files
     * @param outFile Output nc file
     * @param timeDimStr Time dimension name
     * @throws java.io.IOException
     * @throws ucar.ma2.InvalidRangeException
     * @throws java.text.ParseException
     */
    public static void joinDataFiles_Time(List<String> inFiles, String outFile, String timeDimStr) throws IOException, InvalidRangeException, ParseException {
        //Check number of selected files
        int fNum = inFiles.size();
        if (fNum < 2) {
            JOptionPane.showMessageDialog(null, "There should be at least two files!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //Open the first file
        int i;
        String aFile = inFiles.get(0);
        NetCDFDataInfo aDataInfo = new NetCDFDataInfo();
        aDataInfo.readDataInfo(aFile);

        switch (aDataInfo.getConvention()) {
            case WRFOUT:
                timeDimStr = "Time";
                break;
        }

        //Change time dimension as unlimit
        for (i = 0; i < aDataInfo.getDimensions().size(); i++) {
            Dimension aDimS = aDataInfo.getDimensions().get(i);
            if (aDimS.getShortName().equals(timeDimStr)) {
                aDimS.setUnlimited(true);
                break;
            }
        }

        List<String> varNames = new ArrayList<>();
        for (Variable var : aDataInfo.getVariables()) {
            if (MIMath.isNumeric(var.getName().substring(0, 1))) {
                var.setName('V' + var.getName());
            }
            varNames.add(var.getName());
        }

        //Create output nc file and write the data of the first file
        NetcdfFileWriter ncfilew = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, outFile);

        //Define dimensions
        List<ucar.nc2.Dimension> dims = new ArrayList<>();
        for (Dimension dim : aDataInfo._miDims) {
            if (dim.getShortName().equals(timeDimStr)) {
                dims.add(ncfilew.addUnlimitedDimension(dim.getShortName()));
            } else {
                dims.add(ncfilew.addDimension(null, dim.getShortName(), dim.getLength()));
            }
        }

        //Define global attributes
        for (ucar.nc2.Attribute attr : aDataInfo._gAtts) {
            ncfilew.addGroupAttribute(null, attr);
        }

        //Define variables
        for (ucar.nc2.Variable var : aDataInfo._variables) {
            List<ucar.nc2.Dimension> vdims = new ArrayList<>();
            for (ucar.nc2.Dimension dim : var.getDimensions()) {
                for (ucar.nc2.Dimension vdim : dims) {
                    if (vdim.getShortName().equals(dim.getShortName())) {
                        vdims.add(vdim);
                        break;
                    }
                }
            }
            ucar.nc2.Variable nvar = ncfilew.addVariable(null, var.getShortName(), var.getDataType(), vdims);
            if (var.getDimensions().size() == 1 && var.getDimensions().get(0).getShortName().equals(timeDimStr)) {
                nvar.addAttribute(new ucar.nc2.Attribute("units", "hours since 1800-1-1 00:00:00"));
                nvar.addAttribute(new ucar.nc2.Attribute("long_name", "Time"));
                nvar.addAttribute(new ucar.nc2.Attribute("standard_name", "time"));
                nvar.addAttribute(new ucar.nc2.Attribute("axis", "T"));
            } else {
                for (ucar.nc2.Attribute attr : var.getAttributes()) {
                    nvar.addAttribute(attr);
                }
            }
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-M-d HH:mm:ss");
        Date sTime = format.parse("1800-1-1 00:00:00");

        //Create netCDF file
        ncfilew.create();

        //Join data                        
        //Add data
        int tDimNum = 0;
        for (i = 0; i < fNum; i++) {
            aFile = inFiles.get(i);
            aDataInfo = new NetCDFDataInfo();
            aDataInfo.readDataInfo(aFile);

            if (i == 0) {
                for (ucar.nc2.Variable var : ncfilew.getNetcdfFile().getVariables()) {
                    List<String> dimNames = new ArrayList<>();
                    for (ucar.nc2.Dimension dim : var.getDimensions()) {
                        dimNames.add(dim.getShortName());
                    }
                    if (!dimNames.contains(timeDimStr)) {
                        Array varaData = aDataInfo.read(var.getShortName());
                        ncfilew.write(var, varaData);
                    }
                }
            }

            for (ucar.nc2.Variable var : ncfilew.getNetcdfFile().getVariables()) {
                if (!varNames.contains(var.getShortName())) {
                    continue;
                }

                List<String> dimNames = new ArrayList<>();
                for (ucar.nc2.Dimension dim : var.getDimensions()) {
                    dimNames.add(dim.getShortName());
                }
                if (!dimNames.contains(timeDimStr)) {
                    continue;
                }

                ucar.nc2.Variable dvar = aDataInfo.findNCVariable(var.getShortName());
                int tDimIdx = dimNames.indexOf(timeDimStr);
                int dimNum = var.getDimensions().size();
                int[] start = new int[dimNum];
                int[] count = new int[dimNum];
                if (dimNum == 4) {
                    start[2] = 0;
                    count[2] = dvar.getDimensions().get(2).getLength();
                    start[3] = 0;
                    count[3] = dvar.getDimensions().get(3).getLength();
                    for (int d1 = 0; d1 < dvar.getDimensions().get(0).getLength(); d1++) {
                        start[0] = d1;
                        count[0] = 1;
                        for (int d2 = 0; d2 < dvar.getDimensions().get(1).getLength(); d2++) {
                            start[0] = d1;
                            start[1] = d2;
                            count[1] = 1;

                            Array varaData = aDataInfo.read(dvar.getShortName(), start, count);
                            start[tDimIdx] += tDimNum;
                            ncfilew.write(var, start, varaData);
                        }
                    }
                } else if (dimNum == 3) {
                    start[1] = 0;
                    count[1] = dvar.getDimensions().get(1).getLength();
                    start[2] = 0;
                    count[2] = dvar.getDimensions().get(2).getLength();
                    for (int d1 = 0; d1 < dvar.getDimensions().get(0).getLength(); d1++) {
                        start[0] = d1;
                        count[0] = 1;

                        Array varaData = aDataInfo.read(dvar.getShortName(), start, count);
                        start[tDimIdx] += tDimNum;
                        ncfilew.write(var, start, varaData);
                    }
                } else {
                    for (int v = 0; v < dvar.getDimensions().size(); v++) {
                        start[v] = 0;
                        count[v] = dvar.getDimension(v).getLength();
                    }
                    Array varaData = aDataInfo.read(dvar.getShortName());
                    start[tDimIdx] += tDimNum;
                    if (dimNum == 1) {
                        List<Integer> times = aDataInfo.getTimeValues(sTime, "hours");
                        varaData = Array.factory(dvar.getDataType(), dvar.getShape());
                        for (int j = 0; j < times.size(); j++) {
                            varaData.setDouble(j, times.get(j));
                        }
                        if (i > 0) {
                            var.getDimension(0).setLength(var.getDimension(0).getLength() + varaData.getShape()[0]);
                        }
                    }
                    ncfilew.write(var, start, varaData);
                }
            }
            tDimNum += aDataInfo.findDimension(timeDimStr).getLength();
        }

        //Close data file
        ncfilew.flush();
        ncfilew.close();
    }

    /**
     * Join data files by variable
     *
     * @param inFiles Input nc files
     * @param outFile Output nc file
     * @throws java.io.IOException
     * @throws ucar.ma2.InvalidRangeException
     */
    public static void joinDataFiles_Variable(List<String> inFiles, String outFile) throws IOException, InvalidRangeException {
        //Check number of selected files
        int fNum = inFiles.size();
        if (fNum < 2) {
            JOptionPane.showMessageDialog(null, "There should be at least two files!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //Open a file
        String aFile = inFiles.get(0);
        NetCDFDataInfo aDataInfo = new NetCDFDataInfo();
        aDataInfo.readDataInfo(aFile);

        //Create nc file writer
        NetcdfFileWriter ncfile = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, outFile);

        //Define dimensions
        for (Dimension dim : aDataInfo._miDims) {
            ncfile.addDimension(null, dim.getShortName(), dim.getLength(), dim.isShared(),
                    dim.isUnlimited(), dim.isVariableLength());
        }

        //Define global attributes
        for (ucar.nc2.Attribute attr : aDataInfo._gAtts) {
            ncfile.addGroupAttribute(null, attr);
        }

        //Define variables
        List<ucar.nc2.Variable> nvars = new ArrayList<>();
        for (ucar.nc2.Variable var : aDataInfo._variables) {
            ucar.nc2.Variable nvar = ncfile.addVariable(null, var.getShortName(), var.getDataType(), var.getDimensions());
            for (ucar.nc2.Attribute attr : var.getAttributes()) {
                nvar.addAttribute(attr);
            }
            nvars.add(nvar);
        }
        
        //Add variables from more files
        List<List<ucar.nc2.Variable>> mvars = new ArrayList<>();
        List<NetCDFDataInfo> mncf = new ArrayList<>();
        List<String> varNames = aDataInfo.getVariableNames();
        for (int i = 1; i < fNum; i++){
            NetCDFDataInfo df = new NetCDFDataInfo();
            df.readDataInfo(inFiles.get(i));
            List<ucar.nc2.Variable> vars = new ArrayList<>();
            for (ucar.nc2.Variable var : df._variables) {
                if (!varNames.contains(var.getShortName())){
                    ucar.nc2.Variable nvar = ncfile.addVariable(null, var.getShortName(), var.getDataType(), var.getDimensions());
                    for (ucar.nc2.Attribute attr : var.getAttributes()) {
                        nvar.addAttribute(attr);
                    }
                    vars.add(nvar); 
                    varNames.add(var.getShortName());
                }
            }
            mncf.add(df);
            mvars.add(vars);
        }

        //Create netCDF file
        ncfile.create();
        
        //Write variable data
        for (ucar.nc2.Variable nvar : nvars) {
            ncfile.write(nvar, aDataInfo.read(nvar.getShortName()));            
        }        

        //Add data in more files
        for (int i = 0; i < mncf.size(); i++) {
            List<ucar.nc2.Variable> vars = mvars.get(i);
            if (vars.isEmpty()){
                continue;
            }
            NetCDFDataInfo df = mncf.get(i);
            for (ucar.nc2.Variable nvar : vars){
                ncfile.write(nvar, df.read(nvar.getShortName()));
            }
        }

        //Close data file
        ncfile.flush();
        ncfile.close();
    }

    private static int getDataJoinType(NetCDFDataInfo aDataInfo, NetCDFDataInfo bDataInfo, String tDimName) {
        //If same dimension number
        int ndims = aDataInfo.getDimensions().size();
        if (ndims != bDataInfo.getDimensions().size()) {
            return 0;  //Can't be joined
        }

        //If same dimensions
        int i;
        boolean IsSame = true;
        boolean IsJoinVar = true;
        for (i = 0; i < ndims; i++) {
            Dimension aDim = aDataInfo.getDimensions().get(i);
            Dimension bDim = bDataInfo.getDimensions().get(i);
            if (!aDim.getShortName().equals(bDim.getShortName())) {
                IsSame = false;
                break;
            }
            if (aDim.getShortName().toLowerCase().equals(tDimName)) {
                if (aDim.getLength() != bDim.getLength()) {
                    IsJoinVar = false;
                }

                double t1, t2;
                for (int j = 0; j < aDataInfo.getTimeNum(); j++) {
                    t1 = aDataInfo.getTimeValue(j);
                    t2 = bDataInfo.getTimeValue(j);
                    if (t1 != t2) {
                        IsJoinVar = false;
                        break;
                    }
                }
            } else if (aDim.getLength() != bDim.getLength()) {
                IsSame = false;
                break;
            }
        }
        if (!IsSame) {
            return 0;    //Can't be joined
        }

        if (IsJoinVar) {
            return 2;    //Can join variable
        } else {
            if (aDataInfo.getVariableNum() != bDataInfo.getVariableNum()) {
                return 0;
            }

            IsSame = true;
            for (i = 0; i < aDataInfo.getVariableNum(); i++) {
                Variable aVarS = aDataInfo.getVariables().get(i);
                Variable bVarS = bDataInfo.getVariables().get(i);
                if (!aVarS.getName().equals(bVarS.getName()) || aVarS.getDimNumber() != bVarS.getDimNumber()) {
                    IsSame = false;
                }
            }
            if (IsSame) {
                return 1;    //Can join time
            } else {
                return 0;
            }
        }
    }

    /**
     * Add time dimension
     *
     * @param inFile Input nc file
     * @param outFile Output nc file
     * @param aTime Time
     */
    public static void addTimeDimension(String inFile, String outFile, Date aTime) {
        try {
            addTimeDimension(inFile, outFile, aTime, "days");
        } catch (ParseException | IOException | InvalidRangeException ex) {
            Logger.getLogger(NetCDFDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Add time dimension
     *
     * @param inFile Input nc file
     * @param outFile Output nc file
     * @param aTime Time
     * @param timeUnit Time unit (days, hours, minutes, seconds)
     * @throws ParseException
     * @throws IOException
     * @throws InvalidRangeException
     */
    public static void addTimeDimension(String inFile, String outFile, Date aTime, String timeUnit) throws ParseException, IOException, InvalidRangeException {
        //Set data info
        NetCDFDataInfo aDataInfo = new NetCDFDataInfo();
        aDataInfo.readDataInfo(inFile);

        //Check variables if time included
        List<String> varList = aDataInfo.getVariableNames();
        if (varList.contains("time")) {
            return;
        }

        //set start time of the data
        SimpleDateFormat format = new SimpleDateFormat("yyyy-M-d HH:mm:ss");
        Date sTime = format.parse("1800-1-1 00:00:00");
        int tvalue = DataInfo.getTimeValue(aTime, sTime, timeUnit.toLowerCase());

        NetcdfFileWriter ncfilew = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, outFile);

        //Define dimensions
        for (Dimension dim : aDataInfo._miDims) {
            ncfilew.addDimension(null, dim.getShortName(), dim.getLength(), dim.isShared(),
                    dim.isUnlimited(), dim.isVariableLength());
        }
        ucar.nc2.Dimension tdim = ncfilew.addDimension(null, "time", 1);

        //Define global attributes
        for (ucar.nc2.Attribute attr : aDataInfo._gAtts) {
            ncfilew.addGroupAttribute(null, attr);
        }

        //Define variables
        for (ucar.nc2.Variable var : aDataInfo._variables) {
            List<ucar.nc2.Dimension> dims = var.getDimensions();
            if (dims.size() > 1) {
                dims.add(0, tdim);
            }
            ucar.nc2.Variable nvar = ncfilew.addVariable(null, var.getShortName(), var.getDataType(), dims);
            for (ucar.nc2.Attribute attr : var.getAttributes()) {
                nvar.addAttribute(attr);
            }
        }
        List<ucar.nc2.Dimension> dims = new ArrayList<>();
        dims.add(tdim);
        ucar.nc2.Variable tvar = ncfilew.addVariable(null, "time", DataType.INT, dims);
        tvar.addAttribute(new ucar.nc2.Attribute("units", timeUnit.toLowerCase() + " since 1800-1-1 00:00:00"));
        tvar.addAttribute(new ucar.nc2.Attribute("long_name", "Time"));
        tvar.addAttribute(new ucar.nc2.Attribute("standard_name", "time"));
        tvar.addAttribute(new ucar.nc2.Attribute("axis", "T"));

        //Create netCDF file
        ncfilew.create();

        //Add data
        for (ucar.nc2.Variable var : aDataInfo._variables) {
            int dimNum = var.getDimensions().size();
            int[] start = new int[dimNum];
            int[] count = new int[dimNum];
            if (dimNum == 4) {
                start[2] = 0;
                count[2] = var.getDimensions().get(2).getLength();
                start[3] = 0;
                count[3] = var.getDimensions().get(3).getLength();
                for (int d1 = 0; d1 < var.getDimensions().get(0).getLength(); d1++) {
                    start[0] = d1;
                    count[0] = 1;
                    for (int d2 = 0; d2 < var.getDimensions().get(1).getLength(); d2++) {
                        start[0] = d1;
                        start[1] = d2;
                        count[1] = 1;

                        Array varaData = aDataInfo.read(var.getShortName(), start, count);
                        ncfilew.write(var, start, varaData);
                    }
                }
            } else if (dimNum == 3) {
                start[1] = 0;
                count[1] = var.getDimensions().get(1).getLength();
                start[2] = 0;
                count[2] = var.getDimensions().get(2).getLength();
                for (int d1 = 0; d1 < var.getDimensions().get(0).getLength(); d1++) {
                    start[0] = d1;
                    count[0] = 1;

                    Array varaData = aDataInfo.read(var.getShortName(), start, count);
                    ncfilew.write(var, start, varaData);
                }
            } else {
                for (int v = 0; v < var.getDimensions().size(); v++) {
                    start[v] = 0;
                    count[v] = var.getDimension(v).getLength();
                }
                Array varaData = aDataInfo.read(var.getShortName(), start, count);
                ncfilew.write(var, start, varaData);
            }
        }
        Array timeValue = new ArrayInt.D1(tvalue);
        ncfilew.write(tvar, timeValue);

        //Close data file
        ncfilew.flush();
        ncfilew.close();
    }
    // </editor-fold>
    // </editor-fold>
}
