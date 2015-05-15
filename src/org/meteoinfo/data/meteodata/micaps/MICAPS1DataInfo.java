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
import org.meteoinfo.data.meteodata.StationModel;
import org.meteoinfo.data.meteodata.StationModelData;
import org.meteoinfo.data.meteodata.Variable;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.MIMath;
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
import java.util.GregorianCalendar;
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
public class MICAPS1DataInfo extends DataInfo implements IStationDataInfo {

    // <editor-fold desc="Variables">
    private String _description;
    private boolean _isAutoStation = false;
    private List<String> _varList = new ArrayList<>();
    private int _stNum;
    private boolean _hasAllCols = false;
    private int _varNum;
    private final List<List<String>> _dataList = new ArrayList<>();
    private final List<String> _fieldList = new ArrayList<>();
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public MICAPS1DataInfo() {
        String[] items = new String[]{"Altitude", "Grade", "CloudCover", "WindDirection", "WindSpeed", "Pressure",
            "PressVar3h", "WeatherPast1", "WeatherPast2", "Precipitation6h", "LowCloudShape",
            "LowCloudAmount", "LowCloudHeight", "DewPoint", "Visibility", "WeatherNow",
            "Temperature", "MiddleCloudShape", "HighCloudShape"};
        _varList = Arrays.asList(items);
        _fieldList.addAll(Arrays.asList(new String[]{"Stid", "Longitude", "Latitude"}));
        _fieldList.addAll(_varList);
        this.setMissingValue(9999.0);
        this.setDataType(MeteoDataType.MICAPS_1);
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
            String aLine;
            String[] dataArray;
            List<String> aList;
            int n, dataNum;

            //Read file head            
            aLine = sr.readLine().trim();
            _description = aLine;
            if (aLine.contains("自动")) {
                _isAutoStation = true;
            }
            aLine = sr.readLine().trim();
            if (aLine.isEmpty()) {
                aLine = sr.readLine();
            }
            dataArray = aLine.split("\\s+");
            int year = Integer.parseInt(dataArray[0]);
            if (year < 100) {
                if (year < 50) {
                    year = 2000 + year;
                } else {
                    year = 1900 + year;
                }
            }
            Calendar cal = new GregorianCalendar(year, Integer.parseInt(dataArray[1]) - 1, Integer.parseInt(dataArray[2]), Integer.parseInt(dataArray[3]), 0, 0);
            Date time = cal.getTime();
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
                variables.add(var);
            }
            this.setVariables(variables);
            _stNum = Integer.parseInt(dataArray[4]);
            //Read data
            dataNum = 0;
            do {
                aLine = sr.readLine();
                if (aLine == null) {
                    break;
                }
                aLine = aLine.trim();
                dataArray = aLine.split("\\s+");
                aList = new ArrayList<>();
                aList.addAll(Arrays.asList(dataArray));
                for (n = 0; n <= 10; n++) {
                    if (aList.size() < 24) {
                        aLine = sr.readLine();
                        if (aLine == null) {
                            break;
                        }
                        dataArray = aLine.split("\\s+");
                        for (String str : dataArray) {
                            if (!str.isEmpty()) {
                                aList.add(str);
                            }
                        }
                    } else {
                        break;
                    }
                }

                if (aList.size() < 24) {
                    break;
                } else {
                    for (n = 0; n < 10; n++) {
                        aList.remove(aList.size() - 1);
                        if (aList.size() == 22) {
                            break;
                        }
                    }
                }

                if (dataNum == 0) {
                    if (dataArray.length == 26) {
                        _hasAllCols = true;
                    } else {
                        _hasAllCols = false;
                    }
                }

                dataNum++;
                if (dataNum == 1) {
                    _varNum = aList.size();
                }
                _dataList.add(aList);
            } while (aLine != null);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MICAPS1DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MICAPS1DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                sr.close();
            } catch (IOException ex) {
                Logger.getLogger(MICAPS1DataInfo.class.getName()).log(Level.SEVERE, null, ex);
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
        dataInfo += System.getProperty("line.separator") + "Station Number: " + _stNum;
        dataInfo += System.getProperty("line.separator") + "Number of Variables = " + String.valueOf(this.getVariableNum());
        for (int i = 0; i < this.getVariableNum(); i++){
            dataInfo += System.getProperty("line.separator") + "\t" + this.getVariableNames().get(i);
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
        varIdx += 3;

        String aStid;
        int i;
        float lon, lat, t;
        List<String> dataList;
        double[][] discreteData = new double[_dataList.size()][3];
        float minX, maxX, minY, maxY;
        minX = 0;
        maxX = 0;
        minY = 0;
        maxY = 0;
        List<String> stations = new ArrayList<String>();

        for (i = 0; i < _dataList.size(); i++) {
            dataList = _dataList.get(i);
            aStid = dataList.get(0);
            lon = Float.parseFloat(dataList.get(1));
            lat = Float.parseFloat(dataList.get(2));
            t = Float.parseFloat(dataList.get(varIdx));

            if (varIdx == 8) //Pressure
            {
                if (!MIMath.doubleEquals(t, this.getMissingValue())) {
                    if (t > 800) {
                        t = t / 10 + 900;
                    } else {
                        t = t / 10 + 1000;
                    }
                }
            }

            stations.add(aStid);
            discreteData[i][0] = lon;
            discreteData[i][1] = lat;
            discreteData[i][2] = t;

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

        StationData stData = new StationData();
        stData.data = discreteData;
        stData.stations = stations;
        stData.dataExtent = dataExtent;
        stData.missingValue = this.getMissingValue();

        return stData;
    }

    @Override
    public StationInfoData getStationInfoData(int timeIdx, int levelIdx) {
        StationInfoData stInfoData = new StationInfoData();
        stInfoData.setDataList(_dataList);
        stInfoData.setFields(_fieldList);
        stInfoData.setVariables(_varList);

        List<String> stations = new ArrayList<String>();
        int stNum = _dataList.size();
        for (int i = 0; i < stNum; i++) {
            stations.add(_dataList.get(i).get(0));
        }
        stInfoData.setStations(stations);

        return stInfoData;
    }

    @Override
    public StationModelData getStationModelData(int timeIdx, int levelIdx) {
        StationModelData smData = new StationModelData();
        int i;
        float lon, lat;
        String aStid;
        List<String> dataList;
        List<StationModel> smList = new ArrayList<StationModel>();        
        float minX, maxX, minY, maxY;
        minX = 0;
        maxX = 0;
        minY = 0;
        maxY = 0;

        for (i = 0; i < _dataList.size(); i++) {
            dataList = _dataList.get(i);
            aStid = dataList.get(0);
            lon = Float.parseFloat(dataList.get(1));
            lat = Float.parseFloat(dataList.get(2));

            StationModel sm = new StationModel();
            sm.setStationIdentifer(aStid);
            sm.setLongitude(lon);
            sm.setLatitude(lat);
            sm.setWindDirection(Double.parseDouble(dataList.get(6)));    //Wind direction
            sm.setWindSpeed(Double.parseDouble(dataList.get(7)));    //Wind speed
            sm.setVisibility(Double.parseDouble(dataList.get(17)));    //Visibility
            sm.setWeather(Double.parseDouble(dataList.get(18)));    //Weather
            sm.setCloudCover(Double.parseDouble(dataList.get(5)));    //Cloud cover
            sm.setTemperature(Double.parseDouble(dataList.get(19)));    //Temperature
            sm.setDewPoint(Double.parseDouble(dataList.get(16)));    //Dew point
            //Pressure
            double press = Double.parseDouble(dataList.get(8));
            if (MIMath.doubleEquals(press, this.getMissingValue())) {
                sm.setPressure(press);
            } else {
                if (press > 800) {
                    sm.setPressure(press / 10 + 900);
                } else {
                    sm.setPressure(press / 10 + 1000);
                }
            }
            smList.add(sm);

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

        smData.setData(smList);
        smData.setDataExtent(dataExtent);
        smData.setMissingValue(this.getMissingValue());

        return smData;
    }
    // </editor-fold>
}
