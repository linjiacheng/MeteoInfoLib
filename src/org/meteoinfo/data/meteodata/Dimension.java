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
package org.meteoinfo.data.meteodata;

import java.util.ArrayList;
import java.util.List;

/**
 * Template
 *
 * @author Yaqiang Wang
 */
public class Dimension {
    // <editor-fold desc="Variables">

    private ucar.nc2.Dimension _ncDimension = null;
    private String _dimName;
    private DimensionType _dimType;
    private List<Double> _dimValue;
    private int _dimId;
    private int _dimLength = 1;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public Dimension() {
        _dimType = DimensionType.Other;
        _dimValue = new ArrayList<Double>();
    }

    /**
     * Constructor
     *
     * @param dimType Dimension type
     */
    public Dimension(DimensionType dimType) {
        _dimType = dimType;
        _dimValue = new ArrayList<Double>();
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get NetCDF dimension
     * @return NetCDF dimension
     */
    public ucar.nc2.Dimension getNCDimension(){
        return _ncDimension;
    }
    
    /**
     * Set NetCDF dimension
     * @param dim NetCDF dimension
     */
    public void setNCDimension(ucar.nc2.Dimension dim){
        _ncDimension = dim;
    }
    
    /**
     * Get dimension length
     *
     * @return Dimension length
     */
    public int getDimLength() {
        return _dimLength;
    }

    /**
     * Set dimension length
     *
     * @param value Dimension length
     */
    public void setDimLength(int value) {
        _dimLength = value;
    }

    /**
     * Get dimension name
     *
     * @return Dimension name
     */
    public String getDimName() {
        return _dimName;
    }

    /**
     * Set dimension name
     *
     * @param value Dimension name
     */
    public void setDimName(String value) {
        _dimName = value;
    }

    /**
     * Get dimension type
     *
     * @return Dimension type
     */
    public DimensionType getDimType() {
        return _dimType;
    }

    /**
     * Set dimension type
     *
     * @param value Dimension type
     */
    public void setDimType(DimensionType value) {
        _dimType = value;
    }

    /**
     * Get dimension values
     *
     * @return Dimension values
     */
    public List<Double> getDimValue() {
        return _dimValue;
    }

    /**
     * Get dimension identifer
     *
     * @return Dimension identifer
     */
    public int getDimId() {
        return _dimId;
    }

    public void setDimId(int value) {
        _dimId = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Determine if two dimensions equals
     *
     * @param aDim The other dimension
     * @return If equals
     */
    public boolean equals(Dimension aDim) {
        if (!_dimName.equals(aDim.getDimName())) {
            return false;
        }
        if (_dimType != aDim.getDimType()) {
            return false;
        }
        if (_dimLength != aDim.getDimLength()) {
            return false;
        }

        return true;
    }
    
    /**
     * Get dimension value array
     * @return Value array
     */
    public double[] getValues(){
        double[] values = new double[_dimLength];
        for (int i = 0; i < _dimLength; i++){
            values[i] = _dimValue.get(i);
        }
        
        return values;
    }

    /**
     * Set dimension values
     *
     * @param values Values
     */
    public void setValues(List<Double> values) {
        _dimValue = values;
        _dimLength = _dimValue.size();
    }
    
    /**
     * Set dimension values
     *
     * @param values Values
     */
    public void setValues(double[] values){
        _dimValue = new ArrayList<Double>();
        for (double v : values){
            _dimValue.add(v);
        }
        _dimLength = _dimValue.size();
    }
    
    /**
     * Set dimension values
     *
     * @param values Values
     */
    public void setValues(float[] values){
        _dimValue = new ArrayList<Double>();
        for (double v : values){
            _dimValue.add(v);
        }
        _dimLength = _dimValue.size();
    }

    /**
     * Add a dimension value
     *
     * @param value The value
     */
    public void addValue(double value) {
        _dimValue.add(value);
        _dimLength = _dimValue.size();
    }
    
    /**
     * Get minimum dimension value
     * @return Minimum dimension value
     */
    public double getMinValue(){
        return _dimValue.get(0);
    }
    
    /**
     * Get maximum dimension value
     * @return Maximum dimension value
     */
    public double getMaxValue(){
        return _dimValue.get(_dimValue.size() - 1);
    }
    
    /**
     * Get delta value
     * @return Delta value
     */
    public double getDeltaValue(){
        return _dimValue.get(1) - _dimValue.get(0);
    }
    // </editor-fold>
}
