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
package org.meteoinfo.data.meteodata.ascii;

import org.meteoinfo.data.StationData;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.data.meteodata.IStationDataInfo;
import org.meteoinfo.data.meteodata.StationInfoData;
import org.meteoinfo.data.meteodata.StationModelData;
import org.meteoinfo.data.meteodata.Variable;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.MIMath;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author yaqiang
 */
public class LonLatStationDataInfo extends DataInfo implements IStationDataInfo {
    // <editor-fold desc="Variables">

    private List<String> _fields = new ArrayList<String>();
    // </editor-fold>
    // <editor-fold desc="Constructor">
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">

    @Override
    public void readDataInfo(String fileName) {
        BufferedReader sr = null;
        try {
            this.setFileName(fileName);

            sr = new BufferedReader(new FileReader(new File(fileName)));
            String[] dataArray, fieldArray;
            String aLine = sr.readLine();    //Title
            fieldArray = aLine.split(",");
            if (fieldArray.length < 3) {
                JOptionPane.showMessageDialog(null, "The data should have at least four fields!");
                return;
            }
            _fields = Arrays.asList(fieldArray);

            //Judge field type
            aLine = sr.readLine();    //First line
            dataArray = aLine.split(",");
            List<Variable> variables = new ArrayList<Variable>();
            for (int i = 3; i < dataArray.length; i++) {
                if (MIMath.isNumeric(dataArray[i])) {
                    Variable var = new Variable();
                    var.setName(fieldArray[i]);
                    var.setStation(true);
                    variables.add(var);
                }
            }
            this.setVariables(variables);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LonLatStationDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LonLatStationDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                sr.close();
            } catch (IOException ex) {
                Logger.getLogger(LonLatStationDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public String generateInfoText() {
        String dataInfo;
        dataInfo = "File Name: " + this.getFileName();
        //dataInfo += System.getProperty("line.separator") + "Station Number: " + StationNum;
        dataInfo += System.getProperty("line.separator") + "Fields: ";
        for (String aField : _fields) {
            dataInfo += System.getProperty("line.separator") + "  " + aField;
        }

        return dataInfo;
    }

    @Override
    public StationData getStationData(int timeIdx, int varIdx, int levelIdx) {
        try {
            List<String[]> dataList = new ArrayList<String[]>();
            BufferedReader sr = new BufferedReader(new FileReader(new File(this.getFileName())));
            sr.readLine();
            String line = sr.readLine();
            while (line != null) {
                if (line.isEmpty()) {
                    line = sr.readLine();
                    continue;
                }
                dataList.add(line.split(","));
                line = sr.readLine();
            }
            sr.close();

            StationData stationData = new StationData();
            List<String> stations = new ArrayList<String>();
            String stName;
            int i;
            double lon, lat, t;

            String[] dataArray;
            double[][] discreteData = new double[dataList.size()][3];
            double minX, maxX, minY, maxY;
            minX = 0;
            maxX = 0;
            minY = 0;
            maxY = 0;

            //Get real variable index
            int vIdx = _fields.indexOf(this.getVariables().get(varIdx).getName());

            for (i = 0; i < dataList.size(); i++) {
                dataArray = dataList.get(i);
                stName = dataArray[0];
                lon = Double.parseDouble(dataArray[1]);
                lat = Double.parseDouble(dataArray[2]);
                t = Double.parseDouble(dataArray[vIdx]);
                discreteData[i][0] = lon;
                discreteData[i][1] = lat;
                discreteData[i][2] = t;
                stations.add(stName);

                if (i == 0) {
                    minX = lon;
                    maxX = minX;
                    minY = lat;
                    maxY = minY;
                } else {
                    if (minX > lon) {
                        minX = lon;
                    } else if (maxX < lon) {
                        maxX = lon;
                    }
                    if (minY > lat) {
                        minY = lat;
                    } else if (maxY < lat) {
                        maxY = lat;
                    }
                }
            }
            Extent dataExtent = new Extent();
            dataExtent.minX = minX;
            dataExtent.maxX = maxX;
            dataExtent.minY = minY;
            dataExtent.maxY = maxY;

            stationData.data = discreteData;
            stationData.dataExtent = dataExtent;
            stationData.stations = stations;

            return stationData;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LonLatStationDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (IOException ex) {
            Logger.getLogger(LonLatStationDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public StationInfoData getStationInfoData(int timeIdx, int levelIdx) {
        BufferedReader sr = null;
        try {
            sr = new BufferedReader(new InputStreamReader(new FileInputStream(this.getFileName()), "gbk"));
            List<List<String>> dataList = new ArrayList<List<String>>();
            sr.readLine();
            String line = sr.readLine();
            while (line != null) {
                if (line.isEmpty()) {
                    line = sr.readLine();
                    continue;
                }
                List<String> aList = Arrays.asList(line.split(","));
                dataList.add(aList);
                line = sr.readLine();
            }
            sr.close();
            StationInfoData stInfoData = new StationInfoData();
            stInfoData.setDataList(dataList);
            stInfoData.setFields(_fields);
            stInfoData.setVariables(this.getVariableNames());
            return stInfoData;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LonLatStationDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LonLatStationDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                sr.close();
            } catch (IOException ex) {
                Logger.getLogger(LonLatStationDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    @Override
    public StationModelData getStationModelData(int timeIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    // </editor-fold>
}
