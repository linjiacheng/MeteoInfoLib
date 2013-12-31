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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author yaqiang
 */
public class StationInfoData {
    // <editor-fold desc="Variables">

    private List<String> _fields = new ArrayList<String>();
    private List<String> _variables = new ArrayList<String>();
    private List<List<String>> _dataList = new ArrayList<List<String>>();
    private List<String> _stations = new ArrayList<String>();
    // </editor-fold>
    // <editor-fold desc="Constructor">
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get varaible names
     *
     * @return Variable names
     */
    public List<String> getVariables() {
        return _variables;
    }

    public void setVariables(List<String> value) {
        _variables = value;
    }

    /**
     * Get field names
     *
     * @return Field names
     */
    public List<String> getFields() {
        return _fields;
    }

    /**
     * Set field names
     *
     * @param value Field names
     */
    public void setFields(List<String> value) {
        _fields = value;
    }

    /**
     * Get station identifer list
     *
     * @return Station identifer list
     */
    public List<String> getStations() {
        return _stations;
    }

    /**
     * Set station identifer list
     *
     * @param value Station identifer list
     */
    public void setStations(List<String> value) {
        _stations = value;
    }

    /**
     * Get data list - the first three columns are stid, lon and lat
     *
     * @return Data list
     */
    public List<List<String>> getDataList() {
        return _dataList;
    }

    /**
     * Set data list
     *
     * @param value Data list
     */
    public void setDataList(List<List<String>> value) {
        _dataList = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Save the station info data to CSV file
     *
     * @param fileName File path
     */
    public void saveAsCSVFile(String fileName) throws IOException {
        BufferedWriter sr = new BufferedWriter(new FileWriter(fileName));

        String aStr = "";
        for (int i = 0; i < _fields.size(); i++) {
            if (i == 0) {
                aStr = _fields.get(i);
            } else {
                aStr += "," + _fields.get(i);
            }
        }
        sr.write(aStr);
        sr.newLine();
        sr.flush();
        for (int i = 0; i < _dataList.size(); i++) {
            List<String> dList = _dataList.get(i);
            for (int j = 0; j < dList.size(); j++) {
                if (j == 0) {
                    aStr = dList.get(j);
                } else {
                    aStr += "," + dList.get(j);
                }
            }
            sr.write(aStr);
            sr.newLine();
            sr.flush();
        }

        sr.close();
    }
    // </editor-fold>
}
