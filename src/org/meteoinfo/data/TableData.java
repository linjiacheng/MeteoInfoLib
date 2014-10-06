 /* Copyright 2014 Yaqiang Wang,
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
package org.meteoinfo.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.meteoinfo.data.analysis.Statistics;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.util.GlobalUtil;
import org.meteoinfo.table.ColumnData;
import org.meteoinfo.table.DataColumn;
import org.meteoinfo.table.DataRow;
import org.meteoinfo.table.DataTable;
import org.meteoinfo.table.DataTypes;

/**
 *
 * @author Yaqiang Wang
 */
public class TableData {
    // <editor-fold desc="Variables">
    protected DataTable dataTable = new DataTable();
    protected double missingValue = -9999.0;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public TableData(){
        
    }
    
    /**
     * Constructor
     * @param dataTable The data table
     */
    public TableData(DataTable dataTable){
        this.dataTable = dataTable;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get data table
     *
     * @return Data table
     */
    public DataTable getDataTable() {
        return dataTable;
    }
    
    /**
     * Set data table
     * @param value Data table
     */
    public void setDataTable(DataTable value){
        dataTable = value;
    }
    
    /**
     * Get missing value
     * @return Missing value
     */
    public double getMissingValue(){
        return missingValue;
    }
    
    /**
     * Set missing value
     * @param value Missing value
     */
    public void setMissingValue(double value){
        missingValue = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
     /**
     * Add column
     *
     * @param col The column
     */
    public void addColumn(DataColumn col) {
        dataTable.addColumn(col);
    }

    /**
     * Add column
     *
     * @param colName Column name
     * @param dataType Data type
     */
    public void addColumn(String colName, DataTypes dataType) {
        try {
            dataTable.addColumn(colName, dataType);
        } catch (Exception ex) {
            Logger.getLogger(TimeSeriesData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Add data row
     */
    public void addRow() {
        try {
            dataTable.addRow();
        } catch (Exception ex) {
            Logger.getLogger(TimeSeriesData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Add data row
     *
     * @param row Data row
     */
    public void addRow(DataRow row) {
        try {
            dataTable.addRow(row);
        } catch (Exception ex) {
            Logger.getLogger(TimeSeriesData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Get data columns
     * @return Data columns
     */
    public List<DataColumn> getDataColumns(){
        List<DataColumn> cols = new ArrayList<DataColumn>();
        for (DataColumn col : dataTable.getColumns()){
            if (col.getDataType() != DataTypes.Date)
                cols.add(col);
        }
        
        return cols;
    }
    
    /**
     * Get data columns by names
     * @param colNames Data column names
     * @return Data columns
     */
    public List<DataColumn> findColumns(List<String> colNames){
        List<DataColumn> cols = new ArrayList<DataColumn>();
        for (DataColumn col : dataTable.getColumns()){
            for (String colName : colNames){
                if (col.getColumnName().equals(colName)){
                    cols.add(col);
                    break;
                }
            }
        }
        
        return cols;
    }
    
    /**
     * Get column values
     * @param col The data column
     * @return Value list
     */
    public List<Double> getValidColumnValues(DataColumn col){
        return this.getValidColumnValues(dataTable.getRows(), col);
    }
    
    /**
     * Get column values
     * @param rows The data row list
     * @param col The data column
     * @return Column values
     */
    public List<Double> getValidColumnValues(List<DataRow> rows, DataColumn col){
        List<Double> values = new ArrayList<Double>();
        String colName = col.getColumnName();
        String vstr;
        double value = Double.NaN;
        for (DataRow row : rows){
            switch (col.getDataType()){
                case Integer:
                    value = (double)(Integer)row.getValue(colName);
                    break;
                case Float:
                    value = (double)(Float)row.getValue(colName);                    
                    break;
                case Double:
                    value = (Double)row.getValue(colName);
                    break;
                case String:
                    vstr = (String)row.getValue(colName);
                    if (!vstr.isEmpty())
                        value = Double.parseDouble(vstr);
                    else
                        value = Double.NaN;
                    break;
            }
            if (!Double.isNaN(value))
                values.add(value);
        }
        
        return values;
    }
    
    /**
     * Get column data
     * @param colName The column name
     * @return Column data
     */
    public ColumnData getColumnData(String colName){
        return dataTable.getColumnData(colName);
    }
    
    /**
     * Get column data
     * @param col The data column
     * @return Column data
     */
    public ColumnData getColumnData(DataColumn col) {
        return dataTable.getColumnData(col);
    }
    
    /**
     * Get column data
     * @param rows The data row list
     * @param col The data column
     * @return Column values
     */
    public ColumnData getColumnData(List<DataRow> rows, DataColumn col){
        return dataTable.getColumnData(rows, null);
    }
    
    /**
     * Convert a data column to double data type
     * @param colName The data column name
     */
    public void columnToDouble(String colName){
        DataColumn col = dataTable.findColumn(colName);
        DataTypes oldType = col.getDataType();
        col.setDataType(DataTypes.Double);
        Object value;
        for (DataRow row : dataTable.getRows()){
            value = row.getValue(colName);
            switch (oldType){
                case Integer:
                    row.setValue(col, (double)(Integer)value);
                    break;
                case Float:
                    row.setValue(col, (double)(Float)value);
                    break;
                case String:
                    if (MIMath.isNumeric((String)value))
                        row.setValue(col, Double.parseDouble((String)value));
                    else
                        row.setValue(col, Double.NaN);
                    break;
            }            
        }
    }
    
    /**
     * Get average data table
     * @param dataColumns
     * @return Average data table
     * @throws java.lang.Exception
     */
    public DataTable statistics(List<DataColumn> dataColumns) throws Exception{
        DataTable rTable = new DataTable();
        rTable.addColumn("Type", DataTypes.String); 
        rTable.addColumn("Mean", DataTypes.Double);
        rTable.addColumn("Minimum", DataTypes.Double);
        rTable.addColumn("Q1", DataTypes.Double);
        rTable.addColumn("Meadian", DataTypes.Double);
        rTable.addColumn("Q3", DataTypes.Double);
        rTable.addColumn("Maximum", DataTypes.Double);
        rTable.addColumn("StdDev", DataTypes.Double);
        rTable.addColumn("Count", DataTypes.Integer);
        
        int i = 0;
        for (DataColumn col : dataColumns){
            List<Double> values = this.getValidColumnValues(col);
            double mean = Statistics.mean(values);
            double min = Statistics.minimum(values);
            double q1 = Statistics.quantile(values, 1);
            double meadian = Statistics.median(values);
            double q3 = Statistics.quantile(values, 3);
            double max = Statistics.maximum(values);
            double sd = Statistics.standardDeviation(values);
            int n = values.size();
            
            rTable.addRow();
            rTable.setValue(i, 0, col.getColumnName());
            rTable.setValue(i, 1, mean);
            rTable.setValue(i, 2, min);
            rTable.setValue(i, 3, q1);
            rTable.setValue(i, 4, meadian);
            rTable.setValue(i, 5, q3);
            rTable.setValue(i, 6, max);
            rTable.setValue(i, 7, sd);
            rTable.setValue(i, 8, n);
            
            i++;
        }                
        
        return rTable;
    }        
    
    /**
     * Get average data table
     * @return Average data table
     * @throws java.lang.Exception
     */
    public DataTable statistics() throws Exception{
        return this.statistics(this.getDataColumns());
    }
    
    /**
     * Read data table from ASCII file
     *
     * @param fileName File name
     * @param dataColumns Data columns
     * @throws java.io.FileNotFoundException
     */
    public void readASCIIFile(String fileName, List<DataColumn> dataColumns) throws FileNotFoundException, IOException, Exception {
        DataTable dTable = new DataTable();

        BufferedReader sr = new BufferedReader(new FileReader(new File(fileName)));
        String title = sr.readLine().trim();
        //Determine separator
        String separator = GlobalUtil.getSeparator(title);
        String[] titleArray = GlobalUtil.split(title, separator);
        if (titleArray.length < 2) {
            JOptionPane.showMessageDialog(null, "File Format Error!");
            sr.close();
        } else {
            //Get fields
            for (DataColumn col : dataColumns){
                dTable.addColumn(col);
            }
            List<Integer> dataIdxs = new ArrayList<Integer>();
            String fieldName;
            for (int i = 0; i < titleArray.length; i++) {
                fieldName = titleArray[i];
                for (DataColumn col : dataColumns){
                    if (fieldName.equals(col.getColumnName()))
                        dataIdxs.add(i);
                }
            }

            String[] dataArray;
            int rn = 0;
            String line = sr.readLine();
            while (line != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                dataArray = GlobalUtil.split(line, separator);
                dTable.addRow();
                int cn = 0;
                for (int idx : dataIdxs) {
                    dTable.setValue(rn, cn, dataArray[idx]);
                    cn++;
                }

                rn += 1;
                line = sr.readLine();
            }

            dataTable = dTable;
            sr.close();
        }
    }
    
    /**
     * Read data table from ASCII file
     *
     * @param fileName File name
     * @throws java.io.FileNotFoundException
     */
    public void readASCIIFile(String fileName) throws FileNotFoundException, IOException, Exception {
        DataTable dTable = new DataTable();

        BufferedReader sr = new BufferedReader(new FileReader(new File(fileName)));
        String title = sr.readLine().trim();
        //Determine separator
        String separator = GlobalUtil.getSeparator(title);
        String[] titleArray = GlobalUtil.split(title, separator);
        if (titleArray.length < 2) {
            JOptionPane.showMessageDialog(null, "File Format Error!");
            sr.close();
        } else {
            //Get fields
            List<Integer> dataIdxs = new ArrayList<Integer>();
            String fieldName;
            for (int i = 0; i < titleArray.length; i++) {
                fieldName = titleArray[i];
                dTable.addColumn(fieldName, DataTypes.String);
                dataIdxs.add(i);
            }

            String[] dataArray;
            int rn = 0;
            String line = sr.readLine();
            while (line != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                dataArray = GlobalUtil.split(line, separator);
                dTable.addRow();
                int cn = 0;
                for (int idx : dataIdxs) {
                    dTable.setValue(rn, cn, dataArray[idx]);
                    cn++;
                }

                rn += 1;
                line = sr.readLine();
            }

            dataTable = dTable;
            sr.close();
        }
    }       
    
    /**
     * Convert a multi rows data table to single row data table
     * @param inTable Input data table - multi rows
     * @param firstColName The new first column name
     * @param firstColValue The new first column value
     * @return Result data table
     * @throws Exception 
     */
    public DataTable toSingleRowTable(DataTable inTable, String firstColName, String firstColValue) throws Exception{
        DataTable rTable = new DataTable();
        rTable.addColumn(firstColName, DataTypes.String);
        List<Object> values = new ArrayList<Object>();
        int r = 0;
        for (DataRow row : inTable.getRows()){
            String rowName = row.getValue(0).toString();
            int i = 0;
            for (DataColumn col : inTable.getColumns()){
                if (i > 0){
                    DataColumn newCol = new DataColumn(rowName + "_" + col.getColumnName(), col.getDataType());
                    rTable.addColumn(newCol);
                    values.add(inTable.getValue(r, col.getColumnName()));
                }
                i++;
            }
            r++;
        }
        
        rTable.addRow();
        rTable.setValue(0, 0, firstColValue);
        int i = 1;
        for (Object value : values){
            rTable.setValue(0, i, value);
            i++;
        }
        
        return rTable;
    }
    // </editor-fold>
}
