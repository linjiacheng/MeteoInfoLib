/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import org.meteoinfo.data.analysis.Statistics;
import org.meteoinfo.table.DataColumn;
import org.meteoinfo.table.DataRow;
import org.meteoinfo.table.DataTable;
import org.meteoinfo.global.util.GlobalUtil;

/**
 *
 * @author wyq
 */
public class TimeTableData extends TableData{

    // <editor-fold desc="Variables">
    //private int timeColIdx = 0;
    private String timeColName;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public TimeTableData() {
        DataColumn col = new DataColumn("Time", DataTypes.Date);
        dataTable.addColumn(col);
    }
    
    /**
     * Constructor
     * @param dataTable Data table
     */
    public TimeTableData(DataTable dataTable){
        super(dataTable);
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get time column name
     * @return Time column name
     */
    public String getTimeColName(){
        return this.timeColName;
    }
    
    /**
     * Set time column name
     * @param value Time column name
     */
    public void setTimeColName(String value){
        this.timeColName = value;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
   
    
    /**
     * Read data table from ASCII file
     *
     * @param fileName File name
     * @param timeColIdx Time column index
     * @param formatStr Time format string
     * @param dataColumns Data columns
     * @throws java.io.FileNotFoundException
     */
    public void readASCIIFile(String fileName, int timeColIdx, String formatStr, List<DataColumn> dataColumns) throws FileNotFoundException, IOException, Exception {
        DataTable dTable = new DataTable();
        dTable.addColumn("Time", DataTypes.Date);

        BufferedReader sr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "utf-8"));
        String title = sr.readLine().trim();
        //Determine separator
        String separator = GlobalUtil.getDelimiter(title);
        String[] titleArray = GlobalUtil.split(title, separator);
        if (titleArray.length < 2) {
            JOptionPane.showMessageDialog(null, "File Format Error!");
            sr.close();
        } else {
            //Get fields
            for (DataColumn col : dataColumns){
                dataTable.addColumn(col);
            }
            SimpleDateFormat format = new SimpleDateFormat(formatStr);
            List<Integer> dataIdxs = new ArrayList<>();
            String fieldName;
            for (int i = 0; i < titleArray.length; i++) {
                fieldName = titleArray[i];
                if (i == timeColIdx) {
                    dTable.getColumns().get(0).setColumnName(fieldName);
                    continue;
                }
                for (DataColumn col : dataColumns){
                    if (col.getDataType() != DataTypes.Date){
                        if (fieldName.equals(col.getColumnName()))
                            dataIdxs.add(i);
                    }
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
                dTable.setValue(rn, 0, format.parse(dataArray[timeColIdx]));
                int cn = 1;
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
     * @param timeColIdx Time column index
     * @param formatStr Time format string
     * @throws java.io.FileNotFoundException
     */
    public void readASCIIFile(String fileName, int timeColIdx, String formatStr) throws FileNotFoundException, IOException, Exception {
        DataTable dTable = new DataTable();
        dTable.addColumn("Time", DataTypes.Date);

        BufferedReader sr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "utf-8"));
        String title = sr.readLine().trim();
        //Determine separator
        String separator = GlobalUtil.getDelimiter(title);
        String[] titleArray = GlobalUtil.split(title, separator);
        if (titleArray.length < 2) {
            JOptionPane.showMessageDialog(null, "File Format Error!");
            sr.close();
        } else {
            //Get fields
            SimpleDateFormat format = new SimpleDateFormat(formatStr);
            List<Integer> dataIdxs = new ArrayList<>();
            String fieldName;
            for (int i = 0; i < titleArray.length; i++) {
                fieldName = titleArray[i];
                if (i == timeColIdx) {
                    dTable.getColumns().get(0).setColumnName(fieldName);                    
                } else{                
                    dTable.addColumn(fieldName, DataTypes.String);
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
                dTable.setValue(rn, 0, format.parse(dataArray[timeColIdx]));
                int cn = 1;
                for (int idx : dataIdxs) {
                    if (dataArray.length > idx)
                        dTable.setValue(rn, cn, dataArray[idx]);
                    else
                        dTable.setValue(rn, cn, "");
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
     * Get years
     * @return Year list
     */
    public List<Integer> getYears(){
        List<Integer> years = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        int year;
        for (DataRow row : dataTable.getRows()){
            cal.setTime((Date)row.getValue(0));
            year = cal.get(Calendar.YEAR);
            if (!years.contains(year))
                years.add(year);
        }
        
        return years;
    }
    
    /**
     * Get year months
     * @return Year month list
     */
    public List<String> getYearMonths(){
        List<String> yms = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
        String ym;
        for (DataRow row : dataTable.getRows()){
            ym = format.format((Date)row.getValue(this.timeColName));
            if (!yms.contains(ym))
                yms.add(ym);
        }
        
        return yms;
    }
    
    /**
     * Get data row list by year
     * @param year The year
     * @return Data row list
     */
    public List<DataRow> getDataByYear(int year){
        List<DataRow> rows = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        for (DataRow row : dataTable.getRows()){
            cal.setTime((Date)row.getValue(this.timeColName));
            if (cal.get(Calendar.YEAR) == year)
                rows.add(row);
        }
        
        return rows;
    }  
    
    /**
     * Get data row list by year
     * @param season The season
     * @return Data row list
     */
    public List<DataRow> getDataBySeason(String season){
        List<Integer> months = this.getMonthsBySeason(season);
        List<DataRow> rows = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        int month;
        for (DataRow row : dataTable.getRows()){
            cal.setTime((Date)row.getValue(this.timeColName));
            month = cal.get(Calendar.MONTH) + 1;
            if (months.contains(month))
                rows.add(row);
        }
        
        return rows;
    }  
    
    private List<Integer> getMonthsBySeason(String season){
        List<Integer> months = new ArrayList<>();
        if (season.equalsIgnoreCase("spring")){
            months.add(3);
            months.add(4);
            months.add(5);
        } else if (season.equalsIgnoreCase("summer")){
            months.add(6);
            months.add(7);
            months.add(8);
        } else if (season.equalsIgnoreCase("autumn")){
            months.add(9);
            months.add(10);
            months.add(11);
        } else if (season.equalsIgnoreCase("winter")){
            months.add(12);
            months.add(1);
            months.add(2);
        }
        
        return months;
    }
    
    /**
     * Get data row list by year and month
     * @param yearMonth The year and month
     * @return Data row list
     */
    public List<DataRow> getDataByYearMonth(String yearMonth){
        int year = Integer.parseInt(yearMonth.substring(0, 4));
        int month = Integer.parseInt(yearMonth.substring(4));
        return this.getDataByYearMonth(year, month);
    }      
    
    /**
     * Get data row list by year and month
     * @param year The year
     * @param month The month
     * @return Data row list
     */
    public List<DataRow> getDataByYearMonth(int year, int month){
        List<DataRow> rows = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        for (DataRow row : dataTable.getRows()){
            cal.setTime((Date)row.getValue(0));
            if (cal.get(Calendar.YEAR) == year) {
                if (cal.get(Calendar.MONTH) == month - 1) {
                    rows.add(row);
                }
            }
        }
        
        return rows;
    }      
    
    /**
     * Get data row list by month
     * @param month The month
     * @return Data row list
     */
    public List<DataRow> getDataByMonth(int month){
        List<DataRow> rows = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        for (DataRow row : dataTable.getRows()){
            cal.setTime((Date)row.getValue(this.timeColName));
            if (cal.get(Calendar.MONTH) == month - 1) {
                rows.add(row);
            }
        }
        
        return rows;
    }      
    
    /**
     * Get data row list by day of week
     * @param dow Day of week
     * @return Data row list
     */
    public List<DataRow> getDataByDayOfWeek(int dow){
        dow = dow + 1;
        if (dow == 8)
            dow = 1;
        
        List<DataRow> rows = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        for (DataRow row : dataTable.getRows()){
            cal.setTime((Date)row.getValue(this.timeColName));
            if (cal.get(Calendar.DAY_OF_WEEK) == dow) {
                rows.add(row);
            }
        }
        
        return rows;
    }
    
    /**
     * Get data row list by hour
     * @param hour The hour
     * @return Result data row list
     */
    public List<DataRow> getDataByHour(int hour){
        List<DataRow> rows = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        for (DataRow row : dataTable.getRows()){
            cal.setTime((Date)row.getValue(this.timeColName));
            if (cal.get(Calendar.HOUR_OF_DAY) == hour) {
                rows.add(row);
            }
        }
        
        return rows;
    }
    
    /**
     * Average year by year
     * @param cols The data columns
     * @return Result data table
     * @throws Exception 
     */
    public DataTable ave_Year(List<DataColumn> cols) throws Exception {
        DataTable rTable = new DataTable();
        rTable.addColumn("Year", DataTypes.Integer);
        for (DataColumn col : cols){
            rTable.addColumn(col.getColumnName(), DataTypes.Double);
        }
        
        List<Integer> years = this.getYears();
        for (int year : years){
            DataRow nRow = rTable.addRow();         
            nRow.setValue(0, year);
            List<DataRow> rows = this.getDataByYear(year);
            for (DataColumn col : cols){
                List<Double> values = this.getValidColumnValues(rows, col);
                nRow.setValue(col.getColumnName(), Statistics.mean(values));
            }
        }
        
        return rTable;
    }
    
    /**
     * Average month by month
     * @param cols The data columns
     * @return Result data table
     * @throws Exception 
     */
    public DataTable ave_Month(List<DataColumn> cols) throws Exception {
        DataTable rTable = new DataTable();
        rTable.addColumn("YearMonth", DataTypes.String);
        for (DataColumn col : cols){
            rTable.addColumn(col.getColumnName(), DataTypes.Double);
        }
        
        List<String> yms = this.getYearMonths();
        for (String ym : yms){
            DataRow nRow = rTable.addRow();         
            nRow.setValue(0, ym);
            List<DataRow> rows = this.getDataByYearMonth(ym);
            for (DataColumn col : cols){
                List<Double> values = this.getValidColumnValues(rows, col);
                nRow.setValue(col.getColumnName(), Statistics.mean(values));
            }
        }
        
        return rTable;
    }
    
    /**
     * Average monthly
     * @param cols The data columns
     * @return Result data table
     * @throws Exception 
     */
    public DataTable ave_MonthOfYear(List<DataColumn> cols) throws Exception {
        DataTable rTable = new DataTable();
        rTable.addColumn("Month", DataTypes.String);
        for (DataColumn col : cols){
            rTable.addColumn(col.getColumnName(), DataTypes.Double);
        }
        
        List<String> monthNames = Arrays.asList(new String[]{"Jan", "Feb","Mar", "Apr", "May",
            "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"});
        List<Integer> months = new ArrayList<>();
        int i;
        for (i = 1; i < 13; i++){
            months.add(i);
        }
        
        i = 0;
        for (int month : months){
            DataRow nRow = rTable.addRow();         
            nRow.setValue(0, monthNames.get(i));
            List<DataRow> rows = this.getDataByMonth(month);
            for (DataColumn col : cols){
                List<Double> values = this.getValidColumnValues(rows, col);
                nRow.setValue(col.getColumnName(), Statistics.mean(values));
            }
            i++;
        }
        
        return rTable;
    }
    
    /**
     * Average seasonal
     * @param cols The data columns
     * @return Result data table
     * @throws Exception 
     */
    public DataTable ave_SeasonOfYear(List<DataColumn> cols) throws Exception {
        DataTable rTable = new DataTable();
        rTable.addColumn("Season", DataTypes.String);
        for (DataColumn col : cols){
            rTable.addColumn(col.getColumnName(), DataTypes.Double);
        }
        
        List<String> seasons = Arrays.asList(new String[]{"Spring", "Summer", "Autumn", "Winter"});
        for (String season : seasons){
            DataRow nRow = rTable.addRow();         
            nRow.setValue(0, season);
            List<DataRow> rows = this.getDataBySeason(season);
            for (DataColumn col : cols){
                List<Double> values = this.getValidColumnValues(rows, col);
                nRow.setValue(col.getColumnName(), Statistics.mean(values));
            }
        }
        
        return rTable;
    }
    
    /**
     * Average by day of week
     * @param cols The data columns
     * @return Result data table
     * @throws Exception 
     */
    public DataTable ave_DayOfWeek(List<DataColumn> cols) throws Exception {
        DataTable rTable = new DataTable();
        rTable.addColumn("Day", DataTypes.String);
        for (DataColumn col : cols){
            rTable.addColumn(col.getColumnName(), DataTypes.Double);
        }
        
        List<String> dowNames = Arrays.asList(new String[]{"Sunday", "Monday","Tuesday", "Wednesday", "Thursday", "Friday",
            "Saturday"});
        List<Integer> dows = new ArrayList<>();
        dows.add(7);
        int i;
        for (i = 1; i < 7; i++){
            dows.add(i);
        }
        
        i = 0;
        for (int dow : dows){
            DataRow nRow = rTable.addRow();         
            nRow.setValue(0, dowNames.get(i));
            List<DataRow> rows = this.getDataByDayOfWeek(dow);
            for (DataColumn col : cols){
                List<Double> values = this.getValidColumnValues(rows, col);
                nRow.setValue(col.getColumnName(), Statistics.mean(values));
            }
            i++;
        }
        
        return rTable;
    }
    
    /**
     * Average by hour of day
     * @param cols The data columns
     * @return Result data table
     * @throws Exception 
     */
    public DataTable ave_HourOfDay(List<DataColumn> cols) throws Exception {
        DataTable rTable = new DataTable();
        rTable.addColumn("Hour", DataTypes.Integer);
        for (DataColumn col : cols){
            rTable.addColumn(col.getColumnName(), DataTypes.Double);
        }

        List<Integer> hours = new ArrayList<>();
        for (int i = 0; i < 24; i++){
            hours.add(i);
        }
        
        for (int hour : hours){
            DataRow nRow = rTable.addRow();         
            nRow.setValue(0, hour);
            List<DataRow> rows = this.getDataByHour(hour);
            for (DataColumn col : cols){
                List<Double> values = this.getValidColumnValues(rows, col);
                nRow.setValue(col.getColumnName(), Statistics.mean(values));
            }
        }
        
        return rTable;
    }

    // </editor-fold>
}
