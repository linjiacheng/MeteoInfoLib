/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.meteoinfo.data.mapdata.Field;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.table.DataColumn;
import org.meteoinfo.global.table.DataRow;
import org.meteoinfo.global.table.DataTable;
import org.meteoinfo.global.table.DataTypes;
import org.meteoinfo.global.util.GlobalUtil;

/**
 *
 * @author wyq
 */
public class TimeSeriesData {

    // <editor-fold desc="Variables">
    private DataTable dataTable = new DataTable();

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public TimeSeriesData() {
        DataColumn col = new DataColumn("Time", DataTypes.Date);
        dataTable.addColumn(col);
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

        BufferedReader sr = new BufferedReader(new FileReader(new File(fileName)));
        String title = sr.readLine().trim();
        //Determine separator
        String separator = GlobalUtil.getSeparator(title);
        String[] titleArray = GlobalUtil.split(title, separator);
        if (titleArray.length <= 2) {
            JOptionPane.showMessageDialog(null, "File Format Error!");
            sr.close();
        } else {
            //Get fields
            String line = sr.readLine().trim();    //Second line
            String[] dataArray = GlobalUtil.split(line, separator);
            if (dataArray.length != titleArray.length) {
                JOptionPane.showMessageDialog(null, "File Format Error!");
                sr.close();
                return;
            }
            SimpleDateFormat format = new SimpleDateFormat(formatStr);
            List<Integer> dataIdxs = new ArrayList<Integer>();
            String fieldName;
            DataTypes dataType;
            int rn = 0;
            for (int i = 0; i < dataArray.length; i++) {
                fieldName = titleArray[i];
                if (i == timeColIdx) {
                    dTable.getColumns().get(0).setColumnName(fieldName);
                    continue;
                }
                if (MIMath.isNumeric(dataArray[i])) {
                    dataType = DataTypes.Double;
                    dTable.addColumn(fieldName, dataType);
                    dataIdxs.add(i);
                }
            }
            dTable.addRow();
            dTable.setValue(rn, 0, format.parse(dataArray[timeColIdx]));
            int cn = 0;
            for (int idx : dataIdxs) {
                dTable.setValue(rn, cn, Double.parseDouble(dataArray[idx]));
                cn++;
            }

            rn += 1;
            line = sr.readLine();
            while (line != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                dataArray = GlobalUtil.split(line, separator);
                dTable.addRow();
                dTable.setValue(rn, 0, format.parse(dataArray[timeColIdx]));
                cn = 0;
                for (int idx : dataIdxs) {
                    dTable.setValue(rn, cn, Double.parseDouble(dataArray[idx]));
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
     * Get average data table
     * @return Average data table
     */
    public DataTable average(){
        DataTable rTable = new DataTable();
        for (DataColumn col : dataTable.getColumns()){
            DataColumn ncol = new DataColumn(col.getColumnName(), col.getDataType());
            rTable.addColumn(ncol);
        }
        
        return rTable;
    }

    // </editor-fold>
}
