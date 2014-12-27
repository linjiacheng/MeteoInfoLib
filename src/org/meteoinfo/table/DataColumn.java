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
package org.meteoinfo.table;

/**
 *
 * @author Yaqiang Wang
 */
public class DataColumn {

    private boolean readOnly;
    private DataTable table;
    private String columnName;
    private String captionName;
    private int columnIndex;
    private DataTypes dataType;
    private boolean joined = false;
    //private String dataTypeName;

    /**
     * Constructor
     */
    public DataColumn() {
        this("default1");
    }

    /**
     * Constructor
     *
     * @param dataType Data type
     */
    public DataColumn(DataTypes dataType) {
        this("default1", dataType);
    }

    /**
     * Constructor
     *
     * @param columnName Column name
     */
    public DataColumn(String columnName) {
        this(columnName, DataTypes.Integer);
    }

    /**
     * Constructor
     *
     * @param columnName Column name
     * @param dataType Data type
     */
    public DataColumn(String columnName, DataTypes dataType) {
        this.dataType = dataType;
        this.columnName = columnName;
    }

    /**
     * Get column name
     *
     * @return Column name
     */
    public String getColumnName() {
        return this.columnName;
    }

    /**
     * Set Column name
     *
     * @param columnName Column name
     */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    /**
     * Get caption name
     *
     * @return Caption name
     */
    public String getCaptionName() {
        return captionName;
    }

    /**
     * Set caption name
     *
     * @param captionName Caption name
     */
    public void setCaptionName(String captionName) {
        this.captionName = captionName;
    }

    /**
     * Get if is read only
     *
     * @return Boolean
     */
    public boolean isReadOnly() {
        return this.readOnly;
    }

    /**
     * Set if is read only
     *
     * @param readOnly Boolean
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
    
    /**
     * Get if is joined
     * @return Boolean
     */
    public boolean isJoined(){
        return this.joined;
    }
    
    /**
     * Set if is joined
     * @param value Boolean
     */
    public void setJoined(boolean value){
        this.joined = value;
    }

    /**
     * Get data table
     *
     * @return The data table
     */
    public DataTable getTable() {
        return this.table;
    }

    /**
     * Set data table
     *
     * @param table The data table
     */
    public void setTable(DataTable table) {
        this.table = table;
    }

    /**
     * Set data type
     *
     * @param dataType Data type
     */
    public void setDataType(DataTypes dataType) {
        this.dataType = dataType;
    }

    /**
     * Get data type
     *
     * @return The data type
     */
    public DataTypes getDataType() {
        return dataType;
    }

    /**
     * Set column index
     *
     * @param columnIndex Column index
     */
    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    /**
     * Get column index
     *
     * @return The column index
     */
    public int getColumnIndex() {
        return columnIndex;
    }

    /**
     * Get data type name
     *
     * @return Data type name
     */
    public String getDataTypeName() {
        return dataType.toString();
    }

    /**
     * Convert input data to current data type
     *
     * @param value Object value
     * @return Result object
     */
    public Object convertTo(Object value) {
        switch (this.dataType){
            case Integer:
                if (!(value instanceof Integer))                    
                    return Integer.valueOf(value.toString());
                break;
            case Double:
                if (!(value instanceof Double))
                    if (value == null)
                        return Double.NaN;
                    else
                        return Double.valueOf(value.toString());
                break;
            case Float:
                if (!(value instanceof Float))
                    if (value == null)
                        return Float.NaN;
                    else
                        return Float.valueOf(value.toString());
                break;            
        }
        
        if (value == null)
            return "";
        return value;
    }

    /**
     * Convert to string
     *
     * @return String
     */
    @Override
    public String toString() {
        return this.columnName;
    }
    
    /**
     * Clone
     * 
     * @return Cloned DataColumn object 
     */
    @Override
    public Object clone(){
        DataColumn col = new DataColumn();
        col.captionName = this.captionName;
        col.columnIndex = this.columnIndex;
        col.columnName = this.columnName;
        col.dataType = this.dataType;
        col.readOnly = this.readOnly;
        
        return col;
    }
}
