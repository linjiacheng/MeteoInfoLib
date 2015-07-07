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
package org.meteoinfo.data.meteodata.micaps;

import org.meteoinfo.data.StationData;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.data.meteodata.Dimension;
import org.meteoinfo.data.meteodata.DimensionType;
import org.meteoinfo.data.meteodata.IStationDataInfo;
import org.meteoinfo.data.meteodata.StationInfoData;
import org.meteoinfo.data.meteodata.StationModelData;
import org.meteoinfo.data.meteodata.Variable;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meteoinfo.data.meteodata.MeteoDataType;
import org.meteoinfo.global.util.DateUtil;
import ucar.ma2.Array;

/**
 *
 * @author yaqiang
 */
public class MICAPS120DataInfo extends DataInfo implements IStationDataInfo {

    // <editor-fold desc="Variables">
    private String _description;
    private List<String> _varList = new ArrayList<>();
    private List<String> _fieldList = new ArrayList<>();
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public MICAPS120DataInfo() {
        this.setMissingValue(9999.0);
        this.setDataType(MeteoDataType.MICAPS_120);
        String[] items = new String[]{"AQI", "Grade", "PM2.5", "PM10", "CO", "NO2", "O3", "O3_8h", "SO2"};
        _varList = Arrays.asList(items);
        _fieldList.addAll(Arrays.asList(new String[]{"Stid", "Latitude", "Longitude"}));
        _fieldList.addAll(_varList);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">    

    @Override
    public void readDataInfo(String fileName) {
        BufferedReader sr = null;
        try {

            this.setFileName(fileName);
            sr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "gbk"));

            //Read file head
            String aLine = sr.readLine().trim();
            _description = aLine;
            String dateStr = this._description.split("\\s+")[2];
            dateStr = dateStr.substring(dateStr.length() - 10);
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHH");
            Date time = format.parse(dateStr);

            //Set dimension and variables
            Dimension tdim = new Dimension(DimensionType.T);
            double[] values = new double[1];
            values[0] = DateUtil.toOADate(time);
            tdim.setValues(values);
            this.setTimeDimension(tdim);
            List<Variable> variables = new ArrayList<>();
            for (String vName : _varList) {
                Variable var = new Variable();
                var.setName(vName);
                var.setStation(true);
                var.setDimension(tdim);
                var.setFillValue(this.getMissingValue());
                variables.add(var);
            }
            this.setVariables(variables);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MICAPS120DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(MICAPS120DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MICAPS120DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(MICAPS120DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                sr.close();
            } catch (IOException ex) {
                Logger.getLogger(MICAPS120DataInfo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public String generateInfoText() {
        String dataInfo;
        dataInfo = "File Name: " + this.getFileName();
        dataInfo += System.getProperty("line.separator") + "Description: " + _description;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:00");
        dataInfo += System.getProperty("line.separator") + "Time: " + format.format(this.getTimes().get(0));
        dataInfo += System.getProperty("line.separator") + "Fields: ";
        for (String aField : _fieldList) {
            dataInfo += System.getProperty("line.separator") + "  " + aField;
        }

        return dataInfo;
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
        return null;
    }

    @Override
    public StationData getStationData(int timeIdx, int varIdx, int levelIdx) {
        try {
            StationData stData = new StationData();
            BufferedReader sr = new BufferedReader(new InputStreamReader(new FileInputStream(this.getFileName()), "gbk"));

            //Get real variable index
            varIdx = _fieldList.indexOf(_varList.get(varIdx));
            String[] dataArray;
            String stid;
            double lon, lat, value;
            sr.readLine();
            String line = sr.readLine();
            while (line != null) {
                if (line.isEmpty()) {
                    continue;
                }
                line = line.trim();
                dataArray = line.split("\\s+");
                stid = dataArray[0];
                lat = Double.parseDouble(dataArray[1]);
                lon = Double.parseDouble(dataArray[2]);
                value = Double.parseDouble(dataArray[varIdx]);
                stData.addData(stid, lon, lat, value);

                line = sr.readLine();
            }
            sr.close();

            stData.missingValue = this.getMissingValue();

            return stData;
        } catch (IOException ex) {
            Logger.getLogger(MICAPS120DataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public StationInfoData getStationInfoData(int timeIdx, int levelIdx) {
        BufferedReader sr = null;
        try {
            StationInfoData stInfoData = new StationInfoData();
            sr = new BufferedReader(new InputStreamReader(new FileInputStream(this.getFileName()), "gbk"));
            List<List<String>> dataList = new ArrayList<List<String>>();
            String[] dataArray;
            List<String> dList;
            sr.readLine();
            String line = sr.readLine();
            int i;
            while (line != null) {
                if (line.isEmpty()) {
                    continue;
                }
                line = line.trim();
                dataArray = line.split("\\s+");
                dList = new ArrayList<String>();
                i = 0;
                for (String d : dataArray) {
                    if (i == 2)
                        dList.add(1, d);
                    else
                        dList.add(d);
                    i++;
                }
                dataList.add(dList);

                line = sr.readLine();
            }

            stInfoData.setDataList(dataList);
            stInfoData.setFields(_fieldList);
            stInfoData.setVariables(_varList);
            List<String> stations = new ArrayList<String>();
            int stNum = dataList.size();
            for (i = 0; i < stNum; i++) {
                stations.add(dataList.get(i).get(0));
            }
            stInfoData.setStations(stations);

            return stInfoData;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MICAPS120DataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(MICAPS120DataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (IOException ex) {
            Logger.getLogger(MICAPS120DataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            try {
                sr.close();
            } catch (IOException ex) {
                Logger.getLogger(MICAPS120DataInfo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public StationModelData getStationModelData(int timeIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    // </editor-fold>
}
