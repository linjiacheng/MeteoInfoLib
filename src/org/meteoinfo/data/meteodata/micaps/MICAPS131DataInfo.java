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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meteoinfo.data.GridArray;
import org.meteoinfo.data.GridData;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.data.meteodata.Dimension;
import org.meteoinfo.data.meteodata.DimensionType;
import org.meteoinfo.data.meteodata.IGridDataInfo;
import org.meteoinfo.data.meteodata.MeteoDataType;
import org.meteoinfo.data.meteodata.StationInfoData;
import org.meteoinfo.data.meteodata.Variable;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.util.DateUtil;
import org.meteoinfo.projection.KnownCoordinateSystems;
import org.meteoinfo.projection.ProjectionInfo;
import org.meteoinfo.projection.Reproject;
import ucar.ma2.Array;
import ucar.nc2.Attribute;

/**
 *
 * @author linjc
 */
public class MICAPS131DataInfo extends DataInfo implements IGridDataInfo {

    // <editor-fold desc="Variables">
    private String _description;
    private Date _time;
    private int _xNum;
    private int _yNum;
    private int _zNum;
    private double _xDelt;
    private double _yDelt;
    private double _lon_LT;
    private double _lat_LT;
    private double _lon_Center;
    private double _lat_Center;
    private double[] _xArray;
    private double[] _yArray;
    private List<Double> _zList;
    private StationInfoData _stationInfo;
    private List<Double[][]> _dataList;
    private double[][] _maxData;
    
    ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public MICAPS131DataInfo(){
        this.setDataType(MeteoDataType.MICAPS_131);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">

    	@Override
    public void readDataInfo(String fileName) {
        try {
            this.setFileName(fileName);
            RandomAccessFile br = new RandomAccessFile(fileName, "r");
            byte[] bytes = new byte[12];
            br.read(bytes);
            String micapsType = new String(bytes, "GBK").trim();
            bytes = new byte[38];
            br.read(bytes);
            this._description = new String(bytes, "GBK").trim();
            
            bytes = new byte[8];
            br.read(bytes);
            String flag = new String(bytes, "GBK").trim();
            bytes = new byte[8];
            br.read(bytes);
            String version = new String(bytes, "GBK").trim();
            
            bytes = new byte[2];
            br.read(bytes);
            int year = DataConvert.bytes2Int(bytes, byteOrder);
            br.read(bytes);
            int month = DataConvert.bytes2Int(bytes, byteOrder);
            br.read(bytes);
            int day = DataConvert.bytes2Int(bytes, byteOrder);
            br.read(bytes);
            int hour = DataConvert.bytes2Int(bytes, byteOrder);
            br.read(bytes);
            int minute = DataConvert.bytes2Int(bytes, byteOrder);
            Calendar cal = new GregorianCalendar(year, month - 1, day, hour, minute, 0);
            _time = cal.getTime();
            
            br.read(bytes);
            int interval = DataConvert.bytes2Int(bytes, byteOrder);
            
            br.read(bytes);
            this._xNum = DataConvert.bytes2Int(bytes, byteOrder);
            br.read(bytes);
            this._yNum = DataConvert.bytes2Int(bytes, byteOrder);
            br.read(bytes);
            this._zNum = DataConvert.bytes2Int(bytes, byteOrder);
            bytes = new byte[4];
            br.read(bytes);
            int radarCount = DataConvert.bytes2Int(bytes, byteOrder);
            
            br.read(bytes);
            this._lon_LT = DataConvert.bytes2Float(bytes, byteOrder);
            br.read(bytes);
            this._lat_LT = DataConvert.bytes2Float(bytes, byteOrder);
            br.read(bytes);
            this._lon_Center = DataConvert.bytes2Float(bytes, byteOrder);
            br.read(bytes);
            this._lat_Center = DataConvert.bytes2Float(bytes, byteOrder);
            br.read(bytes);
            this._xDelt = DataConvert.bytes2Float(bytes, byteOrder);
            br.read(bytes);
            this._yDelt = DataConvert.bytes2Float(bytes, byteOrder);

            this._zList = new ArrayList<Double>();
            for (int i = 0; i < 40; i++)
            {
            		br.read(bytes);
                double z = DataConvert.bytes2Float(bytes, byteOrder);
                if (z != 0)
                {
                		this._zList.add(z);
                }
            }

            //Read Station Info
            _stationInfo = new StationInfoData();
            _stationInfo.getFields().addAll(Arrays.asList("Longitude", "Latitude", "Altitude", "MosiacFlag"));
            bytes = new byte[16];
    			for (int i = 0; i < 20; i++)
            {
            		br.read(bytes);
                String stationName = new String(bytes, "GBK").trim();
                if (!stationName.isEmpty())
                {
                		_stationInfo.getStations().add(stationName);
                    List<String> fieldList = new ArrayList<String>();
                    fieldList.add("");
                    fieldList.add("");
                    fieldList.add("");
                    fieldList.add("");
                    _stationInfo.getDataList().add(fieldList);
                }
            }
    			bytes = new byte[4];
            for (int i = 0; i < 20; i++)
            {
            		br.read(bytes);
                double longitude = DataConvert.bytes2Float(bytes, byteOrder);
                if (i < radarCount)
                {
                		_stationInfo.getDataList().get(i).set(0, String.valueOf(longitude));
                }
            }
            for (int i = 0; i < 20; i++)
            {
            		br.read(bytes);
                double latitude = DataConvert.bytes2Float(bytes, byteOrder);
                if (i < radarCount)
                {
                		_stationInfo.getDataList().get(i).set(0, String.valueOf(latitude));
                }
            }
            for (int i = 0; i < 20; i++)
            {
            		br.read(bytes);
            		double altitude = DataConvert.bytes2Float(bytes, byteOrder);
                if (i < radarCount)
                {
                		_stationInfo.getDataList().get(i).set(0, String.valueOf(altitude));
                }
            }
            for (int i = 0; i < 20; i++)
            {
            		byte b = br.readByte();
                int mosiacFlag = DataConvert.byte2Int(b);
                if (i < radarCount)
                {
                		_stationInfo.getDataList().get(i).set(0, String.valueOf(mosiacFlag));
                }
            }
            
            bytes = new byte[2];
            br.read(bytes);
            int dataType = DataConvert.bytes2Int(bytes, byteOrder);
            br.read(bytes);
            int levelDim = DataConvert.bytes2Int(bytes, byteOrder);
            bytes = new byte[168];
            br.read(bytes);
            String reserved = new String(bytes, "GBK");

            _dataList = new ArrayList<Double[][]>();
            _maxData = new double[_yNum][_xNum];
            for (int i = 0; i < _zNum; i++)
            {
                Double[][] aData = new Double[_yNum][_xNum];
                for (int j = 0; j < _yNum; j++)
                {
                    for (int k = 0; k < _xNum; k++)
                    {
                    		byte b = br.readByte();
                        int datum = DataConvert.byte2Int(b);
                        
                        aData[_yNum - 1 - j][k] = (datum - 66) / 2.0;
                        
                        _maxData[_yNum - 1 - j][k] = Math.max(aData[_yNum - 1 - j][k], _maxData[_yNum - 1 - j][k]);
                    }
                }

                _dataList.add(aData);
            }
            
            //Get projection and coordinate
            this.setProjectionInfo(KnownCoordinateSystems.geographic.world.WGS1984);
            Object[] coords = this.calCoordinate(_lon_LT, _lat_LT, _xDelt, _yDelt, _xNum, _yNum);
            br.close();

            Dimension tdim = new Dimension(DimensionType.T);
            tdim.addValue(DateUtil.toOADate(_time));
            this.setTimeDimension(tdim);
            this.addDimension(tdim);
            Dimension zdim = new Dimension(DimensionType.Z);
            zdim.setValues(_zList);
            this.addDimension(zdim);
            this.setZDimension(zdim);
            Dimension ydim = new Dimension(DimensionType.Y);
            ydim.setValues((double[]) coords[1]);
            this.addDimension(ydim);
            this.setYDimension(ydim);
            Dimension xdim = new Dimension(DimensionType.X);
            xdim.setValues((double[]) coords[0]);
            this.addDimension(xdim);
            this.setXDimension(xdim);

            Variable var = new Variable();
            var.setName("var");
            var.setDimension(tdim);
            var.setDimension(zdim);
            var.setDimension(ydim);
            var.setDimension(xdim);
            this.addVariable(var);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MICAPS131DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MICAPS131DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Object[] calCoordinate(double lon_LT, double lat_LT, double xDelt, double yDelt,
            int xNum, int yNum) {
       
    		double[] X = new double[xNum];
        double[] Y = new double[yNum];
        int i;
        for (i = 0; i < xNum; i++) {
            X[i] = lon_LT + i * xDelt;
        }
        for (i = 0; i < yNum; i++) {
            Y[_yNum - 1 - i] = lat_LT - i * yDelt;
        }
        return new Object[]{X, Y};
    }
    
    /**
     * Get global attributes
     * @return Global attributes
     */
    @Override
    public List<Attribute> getGlobalAttributes(){
        return new ArrayList<>();
    }

    @Override
    public String generateInfoText() {
        String dataInfo = "";
        dataInfo += "File Name: " + this.getFileName();
        dataInfo += System.getProperty("line.separator") + "Description: " + _description;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        dataInfo += System.getProperty("line.separator") + "Time: " + format.format(_time);
        dataInfo += System.getProperty("line.separator") + "X number: " + String.valueOf(_xNum);
        dataInfo += System.getProperty("line.separator") + "Y number: " + String.valueOf(_yNum);
        dataInfo += System.getProperty("line.separator") + "Left-Top longitude: " + String.valueOf(_lon_LT);
        dataInfo += System.getProperty("line.separator") + "Left-Top latitude: " + String.valueOf(_lat_LT);
        dataInfo += System.getProperty("line.separator") + "Center longitude: " + String.valueOf(_lon_Center);
        dataInfo += System.getProperty("line.separator") + "Center latitude: " + String.valueOf(_lat_Center);

        return dataInfo;
    }
    
    /**
     * Read array data of a variable
     * 
     * @param varName Variable name
     * @return Array data
     */
    @Override
    public Array read(String varName){
        return null;
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
    
    /**
     * Get grid data
     *
     * @param varName Variable name
     * @return Grid data
     */
    @Override
    public GridArray getGridArray(String varName) {
        return null;    
    }

    @Override
    public GridData getGridData_LonLat(int timeIdx, int varIdx, int levelIdx) {
        GridData gridData = new GridData();
        double[][] gData = new double[_yNum][_xNum];
        for (int i = 0; i < _yNum; i++) {
            for (int j = 0; j < _xNum; j++) {
                gData[i][j] = getMissingValue();
                gData[i][j] = _dataList.get(levelIdx)[i][j];
            }
        }
        gridData.data = gData;
        gridData.xArray = this.getXDimension().getValues();
        gridData.yArray = this.getYDimension().getValues();

        return gridData;
    }

    @Override
    public GridData getGridData_TimeLat(int lonIdx, int varIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_TimeLon(int latIdx, int varIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_LevelLat(int lonIdx, int varIdx, int timeIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_LevelLon(int latIdx, int varIdx, int timeIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_LevelTime(int latIdx, int varIdx, int lonIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_Time(int lonIdx, int latIdx, int varIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_Level(int lonIdx, int latIdx, int varIdx, int timeIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_Lon(int timeIdx, int latIdx, int varIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_Lat(int timeIdx, int lonIdx, int varIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public GridData getMaxGridData() {
    		GridData gridData = new GridData();
    		
    		gridData.data = _maxData;
        gridData.xArray = this.getXDimension().getValues();
        gridData.yArray = this.getYDimension().getValues();

        return gridData;
	}
    // </editor-fold>   
}
