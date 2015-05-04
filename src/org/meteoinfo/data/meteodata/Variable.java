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
import java.util.Date;
import java.util.List;
import org.meteoinfo.global.util.DateUtil;

/**
 *
 * @author Yaqiang Wang
 */
public class Variable {
    // <editor-fold desc="Variables">
    /// <summary>
    /// Parameter number
    /// </summary>

    public int Number;
    private String _name;
    private int _levelType;
    private List<Double> _levels;
    private String _units;
    private String _description;
    private List<Dimension> _dimensions = new ArrayList<Dimension>();
    private String _hdfPath;
    private boolean _isStation = false;
    private boolean _isSwath = false;
    //private NetCDF4.NcType _ncType;
    private List<Attribute> _attributes = new ArrayList<Attribute>();
    private int _attNumber;
    private int _varId;
    private boolean _isCoordVar = false;
    private List<Integer> _levelIdxs = new ArrayList<Integer>();
    private List<Integer> _varInLevelIdxs = new ArrayList<Integer>();
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public Variable() {
        _name = "Undef";
        _levels = new ArrayList<Double>();
        _units = "Undef";
        _description = "Undef";
    }

    /**
     * Constructor
     *
     * @param aNum Parameter number
     * @param aName The name
     * @param aDesc The description
     * @param aUnit The units
     */
    public Variable(int aNum, String aName, String aDesc, String aUnit) {
        Number = aNum;
        _name = aName;
        _description = aDesc;
        _units = aUnit;
        _levels = new ArrayList<Double>();
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get name
     *
     * @return Name
     */
    public String getName() {
        return _name;
    }

    /**
     * Set name
     *
     * @param value Name
     */
    public void setName(String value) {
        _name = value;
    }

    /**
     * Get level type
     *
     * @return Level type
     */
    public int getLevelType() {
        return _levelType;
    }

    /**
     * Set level type
     *
     * @param value Level type
     */
    public void setLevelType(int value) {
        _levelType = value;
    }

    /**
     * Get levels
     *
     * @return Levels
     */
    public List<Double> getLevels() {
        //return _levels;
        Dimension zDim = this.getZDimension();
        if (zDim == null) {
            return _levels;
        } else {
            return zDim.getDimValue();
        }
    }

    /**
     * Set levels
     *
     * @param value Levels
     */
    public void setLevels(List<Double> value) {
        _levels = value;
        this.updateZDimension();
    }

    /**
     * Set units
     *
     * @return Units
     */
    public String getUnits() {
        return _units;
    }

    /**
     * Set units
     *
     * @param value Units
     */
    public void setUnits(String value) {
        _units = value;
    }

    /**
     * Get description
     *
     * @return Description
     */
    public String getDescription() {
        return _description;
    }

    /**
     * Set description
     *
     * @param value Description
     */
    public void setDescription(String value) {
        _description = value;
    }

    /**
     * Get dimension number
     *
     * @return Dimension number
     */
    public int getDimNumber() {
        return _dimensions.size();
    }

    /**
     * Get level number
     *
     * @return Level number
     */
    public int getLevelNum() {
        //return _levels.size();
        Dimension zDim = this.getZDimension();
        if (zDim == null) {
            return 0;
        } else {
            return zDim.getDimLength();
        }
    }

    /**
     * Get HDF path
     *
     * @return HDF path
     */
    public String getHDFPath() {
        return _hdfPath;
    }

    /**
     * Set HDF path
     *
     * @param value HDF path
     */
    public void setHDFPath(String value) {
        _hdfPath = value;
    }

    /**
     * Get dimensions
     *
     * @return Dimensions
     */
    public List<Dimension> getDimensions() {
        return _dimensions;
    }
    
    /**
     * Get dimension
     * @param idx Index
     * @return Dimension
     */
    public Dimension getDimension(int idx){
        return _dimensions.get(idx);
    }

    /**
     * Set dimensions
     *
     * @param value Dimensions
     */
    public void setDimensions(List<Dimension> value) {
        _dimensions = value;
    }

    /**
     * Get X dimension
     *
     * @return X dimension
     */
    public Dimension getXDimension() {
        return getDimension(DimensionType.X);
    }

    /**
     * Set X dimension
     *
     * @param value X dimension
     */
    public void setXDimension(Dimension value) {
        setDimension(value, DimensionType.X);
    }

    /**
     * Get Y dimension
     *
     * @return Y dimension
     */
    public Dimension getYDimension() {
        return getDimension(DimensionType.Y);
    }

    /**
     * Set Y dimension
     *
     * @param value Y dimension
     */
    public void setYDimension(Dimension value) {
        setDimension(value, DimensionType.Y);
    }

    /**
     * Get Z dimension
     *
     * @return Z dimension
     */
     public Dimension getZDimension() {
        return getDimension(DimensionType.Z);
    }

    /**
     * Set Z dimension
     *
     * @param value Z dimension
     */
    public void setZDimension(Dimension value) {
        setDimension(value, DimensionType.Z);
    }

    /**
     * Get T dimension
     *
     * @return T dimension
     */
    public Dimension getTDimension() {
        return getDimension(DimensionType.T);
    }

    /**
     * Set T dimension
     *
     * @param value T dimension
     */
    public void setTDimension(Dimension value) {
        setDimension(value, DimensionType.T);
    }

    /**
     * Get dimension identifers
     *
     * @return Dimension identifers
     */
    public int[] getDimIds() {
        int[] dimids = new int[_dimensions.size()];
        for (int i = 0; i < _dimensions.size(); i++) {
            dimids[i] = _dimensions.get(i).getDimId();
        }

        return dimids;
    }

    /**
     * Get if the variable is station data set
     *
     * @return Boolean
     */
    public boolean isStation() {
        return _isStation;
    }

    /**
     * Set if the variable is station data set
     *
     * @param value Boolean
     */
    public void setStation(boolean value) {
        _isStation = value;
    }

    /**
     * Get if the variable is swath data set
     *
     * @return Boolean
     */
    public boolean isSwath() {
        return _isSwath;
    }

    /**
     * Set if the variable is swath data set
     *
     * @param value Boolean
     */
    public void setSwath(boolean value) {
        _isSwath = value;
    }

    /**
     * Get if the variable is plottable (has both X and Y dimension)
     *
     * @return Boolean
     */
    public boolean isPlottable() {
        if (_isStation) {
            return true;
        }
        if (this.getXDimension() == null) {
            return false;
        }
        if (this.getYDimension() == null) {
            return false;
        }

        return true;
    }

//        /// <summary>
//        /// Get of set NC type
//        /// </summary>
//        public NetCDF4.NcType NCType
//        {
//            get { return _ncType; }
//            set { _ncType = value; }
//        }
    /**
     * Get attributes
     *
     * @return Attributes
     */
    public List<Attribute> getAttributes() {
        return _attributes;
    }

    /**
     * Set attributes
     *
     * @param value Attributes
     */
    public void setAttributes(List<Attribute> value) {
        _attributes = value;
    }

    /**
     * Get attribute number
     *
     * @return Attribute number
     */
    public int getAttNumber() {
        return _attNumber;
    }

    /**
     * Set attribute number
     *
     * @param value Attribute number
     */
    public void setAttNumber(int value) {
        _attNumber = value;
    }

    /**
     * Get variable identifer
     *
     * @return Variable identifer
     */
    public int getVarId() {
        return _varId;
    }

    /**
     * Set variable identifer
     *
     * @param value Variable identifer
     */
    public void setVarId(int value) {
        _varId = value;
    }

    /**
     * Get if the variable is coordinate variable
     *
     * @return Boolean
     */
    public boolean isCoorVar() {
        return _isCoordVar;
    }

    /**
     * Set if the variable is coordinate variable
     *
     * @param value Boolean
     */
    public void setCoorVar(boolean value) {
        _isCoordVar = value;
    }

    /**
     * Get level index list - for ARL data
     *
     * @return Level index list
     */
    public List<Integer> getLevelIdxs() {
        return _levelIdxs;
    }

    /**
     * Set level index list
     *
     * @param value Level index list
     */
    public void setLevelIdxs(List<Integer> value) {
        _levelIdxs = value;
    }

    /**
     * Get variable index in level index list - for ARL data
     *
     * @return Variable index
     */
    public List<Integer> getVarInLevelIdxs() {
        return _varInLevelIdxs;
    }

    /**
     * Set variable index in level index list - for ARL data
     *
     * @param value Variable index
     */
    public void setVarInLevelIdxs(List<Integer> value) {
        _varInLevelIdxs = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Clone
     *
     * @return Parameter object
     */
    @Override
    public Object clone() {
        Variable aPar = new Variable();
        aPar.Number = Number;
        aPar.setName(_name);
        aPar.setUnits(_units);
        aPar.setDescription(_description);
        aPar.setLevelType(_levelType);

        //aPar.getAttributes().addAll(_attributes);
        aPar.getDimensions().addAll(_dimensions);
        aPar.setCoorVar(_isCoordVar);
        aPar.getLevels().addAll(_levels);
        aPar.setAttNumber(_attNumber);
        //aPar.NCType = _ncType;
        aPar.setVarId(_varId);

        return aPar;
    }

    /**
     * Determine if two parameter are equal
     *
     * @param aVar The variable
     * @return If equal
     */
    public boolean equals(Variable aVar) {
        if (!_name.equals(aVar.getName())) {
            return false;
        }
        if (Number != aVar.Number) {
            return false;
        }
        if (!_description.equals(aVar.getDescription())) {
            return false;
        }
        if (!_units.equals(aVar.getUnits())) {
            return false;
        }

        return true;
    }

    /**
     * Determine if two parameter are totally equal
     *
     * @param aVar The variable
     * @return If equal
     */
    public boolean tEquals(Variable aVar) {
        if (!_name.equals(aVar.getName())) {
            return false;
        }
        if (Number != aVar.Number) {
            return false;
        }
        if (!_description.equals(aVar.getDescription())) {
            return false;
        }
        if (!_units.equals(aVar.getUnits())) {
            return false;
        }
        if (_levelType != aVar.getLevelType()) {
            return false;
        }

        return true;
    }

    /**
     * Add a level
     *
     * @param levelValue Level value
     */
    public void addLevel(double levelValue) {
        if (!_levels.contains(levelValue)) {
            _levels.add(levelValue);
        }
    }

    /**
     * Get true level number
     *
     * @return True level number
     */
    public int getTrueLevelNumber() {
        if (getLevelNum() == 0) {
            return 1;
        } else {
            return getLevelNum();
        }
    }

    /**
     * Get dimension by type
     *
     * @param dimType Dimension type
     * @return Dimension
     */
    public Dimension getDimension(DimensionType dimType) {
        Dimension aDim = null;
        for (int i = 0; i < getDimNumber(); i++) {
            if (_dimensions.get(i).getDimType() == dimType) {
                aDim = _dimensions.get(i);
                break;
            }
        }

        return aDim;
    }

    /**
     * Set dimension
     *
     * @param aDim The dimension
     */
    public void setDimension(Dimension aDim) {
        if (aDim == null)
            return;
        
        if (aDim.getDimType() == DimensionType.Other) {
            _dimensions.add(aDim);
        } else {
            boolean hasDim = false;
            for (int i = 0; i < getDimNumber(); i++) {
                if (_dimensions.get(i).getDimType() == aDim.getDimType()) {
                    _dimensions.set(i, aDim);
                    hasDim = true;
                    break;
                }
            }

            if (!hasDim) {
                _dimensions.add(aDim);
            }
        }
    }

    /**
     * Set dimension by dimension type
     *
     * @param aDim The dimension
     * @param dimType Dimension type
     */
    public void setDimension(Dimension aDim, DimensionType dimType) {
        if (aDim.getDimType() == dimType) {
            setDimension(aDim);
        }
    }

    /**
     * Get index of a dimension
     *
     * @param aDim The dimension
     * @return Index
     */
    public int getDimIndex(Dimension aDim) {
        int idx = -1;
        for (int i = 0; i < getDimNumber(); i++) {
            if (aDim.equals(_dimensions.get(i))) {
                idx = i;
                break;
            }
        }

        return idx;
    }
    
    /**
     * Get dimension length
     * @param idx Dimension index
     * @return Dimension length
     */
    public int getDimLength(int idx) {
        return this._dimensions.get(idx).getDimLength();
    }

    /**
     * Determine if has Xtrack dimension
     *
     * @return Boolean
     */
    public boolean hasXtrackDimension() {
        boolean has = false;
        for (int i = 0; i < getDimNumber(); i++) {
            if (_dimensions.get(i).getDimType() == DimensionType.Xtrack) {
                has = true;
                break;
            }
        }

        return has;
    }

    /**
     * Determine if the variable has a dimension
     *
     * @param dimId Dimension identifer
     * @return Boolean
     */
    public boolean hasDimension(int dimId) {
        for (Dimension aDim : _dimensions) {
            if (aDim.getDimId() == dimId) {
                return true;
            }
        }

        return false;
    }
    
    /**
     * If the dimensions equales with another variable
     * @param var Another variable
     * @return Boolean
     */
    public boolean dimensionEquales(Variable var){
        if (this.getDimNumber() != var.getDimNumber())
            return false;
        for (int i = 0; i < this.getDimNumber(); i++){
            Dimension adim = this._dimensions.get(i);
            Dimension bdim = var.getDimensions().get(i);
            if (!adim.getDimName().equals(bdim.getDimName()))
                return false;
        }
        
        return true;
    }
    
    /**
     * If the dimensions size equales with another variable
     * @param var Another variable
     * @return Boolean
     */
    public boolean dimensionSizeEquals(Variable var){
        if (this.getDimNumber() != var.getDimNumber())
            return false;
        
        for (int i = 0; i < this.getDimNumber(); i++){
            Dimension adim = this._dimensions.get(i);
            Dimension bdim = var.getDimensions().get(i);
            if (adim.getDimLength() != bdim.getDimLength())
                return false;
        }
        
        return true;
    }
    
    /**
     * If the dimensions contains the diemsions of another variable
     * @param var Another variable
     * @return Boolean
     */
    public boolean dimensionContains(Variable var){
        if (this.getDimNumber() < var.getDimNumber())
            return false;
        
        for (int i = 0; i < var.getDimNumber(); i++){
            Dimension adim = this._dimensions.get(i);
            Dimension bdim = var.getDimensions().get(i);
            if (adim.getDimLength() != bdim.getDimLength())
                return false;
        }
        
        return true;
    }
    
    /**
     * Get level dimension for SWATH data variable
     * @param var Variable
     * @return Dimension
     */
    public Dimension getLevelDimension(Variable var){   
        if (this.getDimNumber() > var.getDimNumber()) {
            for (int i = var.getDimNumber(); i < this.getDimNumber(); i++) {
                Dimension dim = this._dimensions.get(i);
                if (dim.getDimType() == DimensionType.Other)
                    return dim;
            }
        }
        
        return null;
    }

    /**
     * Get times
     *
     * @return Times
     */
    public List<Date> getTimes() {
        Dimension tDim = this.getTDimension();
        if (tDim == null) {
            return null;
        }

        List<Double> values = tDim.getDimValue();
        List<Date> times = new ArrayList<Date>();
        for (Double v : values) {
            times.add(DateUtil.fromOADate(v));
        }

        return times;
    }

    /**
     * Get attribute index by name, return -1 if the name not exist.
     *
     * @param attName Attribute name
     * @return Attribute index
     */
    public int getAttributeIndex(String attName) {
        int idx = -1;
        for (int i = 0; i < _attributes.size(); i++) {
            if (_attributes.get(i).attName.equalsIgnoreCase(attName)) {
                idx = i;
                break;
            }
        }

        return idx;
    }

    /**
     * Get attribute value string by name
     *
     * @param attName Attribute name
     * @return Attribute value string
     */
    public String getAttributeString(String attName) {
        String attStr = "";
        for (Attribute aAtt : _attributes) {
            if (aAtt.attName.equalsIgnoreCase(attName)) {
                attStr = aAtt.toString();
            }
        }

        return attStr;
    }

    /**
     * Add a dimension
     *
     * @param dim Dimension
     */
    public void addDimension(Dimension dim) {
        _dimensions.add(dim);
    }
    
    /**
     * Add a dimension
     * @param idx Index
     * @param dim Dimension
     */
    public void addDimension(int idx, Dimension dim){
        _dimensions.add(idx, dim);
    }

    /**
     * Add attribute
     *
     * @param attName Attribute name
     * @param attValue Attribute value
     */
    public void addAttribute(String attName, String attValue) {
        Attribute aAtt = new Attribute();
        //aAtt.NCType = NetCDF4.NcType.NC_CHAR;
        aAtt.attName = attName;
        aAtt.attValue = attValue;
        aAtt.attLen = attValue.length();

        _attributes.add(aAtt);
        _attNumber = _attributes.size();
    }

    /**
     * Add attribute
     *
     * @param attName Attribute name
     * @param attValue Attribute name
     */
    public void addAttribute(String attName, double attValue) {
        Attribute aAtt = new Attribute();
        //aAtt.NCType = NetCDF4.NcType.NC_DOUBLE;
        aAtt.attName = attName;
        aAtt.attValue = attValue;
        aAtt.attLen = 1;

        _attributes.add(aAtt);
        _attNumber = _attributes.size();
    }

    /**
     * Update z dimension from levels
     */
    public void updateZDimension() {
        if (_levels.size() > 0) {
            Dimension zdim = new Dimension(DimensionType.Z);
            zdim.setValues(_levels);
            this.setZDimension(zdim);
        }
    }
    // </editor-fold>
}
