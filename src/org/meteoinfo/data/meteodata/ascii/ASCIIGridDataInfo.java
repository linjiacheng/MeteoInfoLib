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

import org.meteoinfo.data.GridData;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.data.meteodata.Dimension;
import org.meteoinfo.data.meteodata.DimensionType;
import org.meteoinfo.data.meteodata.IGridDataInfo;
import org.meteoinfo.data.meteodata.Variable;
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
import org.meteoinfo.data.meteodata.MeteoDataType;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.ma2.Section;

/**
 *
 * @author yaqiang
 */
public class ASCIIGridDataInfo extends DataInfo implements IGridDataInfo {
    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public ASCIIGridDataInfo(){
        this.setDataType(MeteoDataType.ASCII_Grid);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">

    @Override
    public void readDataInfo(String fileName) {
        try {
            this.setFileName(fileName);
            
            BufferedReader sr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "utf-8"));
            double xllCorner, yllCorner, cellSize, nodata_value;
            int ncols, nrows, i;
            String aLine;
            String[] dataArray;
            
            aLine = sr.readLine();
            for (i = 1; i <= 5; i++) {
                aLine = aLine + " " + sr.readLine();
            }
            
            dataArray = aLine.split("\\s+");
            ncols = Integer.parseInt(dataArray[1]);
            nrows = Integer.parseInt(dataArray[3]);
            xllCorner = Double.parseDouble(dataArray[5]);
            yllCorner = Double.parseDouble(dataArray[7]);
            cellSize = Double.parseDouble(dataArray[9]);
            nodata_value = Double.parseDouble(dataArray[11]);
            
            this.setMissingValue(nodata_value);
            double[] X = new double[ncols];
            for (i = 0; i < ncols; i++) {
                X[i] = xllCorner + i * cellSize;
            }
            if (X[ncols - 1] + cellSize - X[0] == 360) {
                this.setGlobal(true);
            }
            
            double[] Y = new double[nrows];
            for (i = 0; i < nrows; i++) {
                Y[i] = yllCorner + i * cellSize;
            }
            
            Dimension xDim = new Dimension(DimensionType.X);
            xDim.setValues(X);
            this.setXDimension(xDim);
            Dimension yDim = new Dimension(DimensionType.Y);
            yDim.setValues(Y);
            this.setYDimension(yDim);
            
            List<Variable> variables = new ArrayList<>();
            Variable aVar = new Variable();
            aVar.setName("var");
            aVar.addDimension(yDim);
            aVar.addDimension(xDim);            
            variables.add(aVar);
            this.setVariables(variables);
            
            sr.close();
        } catch (IOException ex) {
            Logger.getLogger(ASCIIGridDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public String generateInfoText() {
        String dataInfo;
        dataInfo = "File Name: " + this.getFileName();
        dataInfo += System.getProperty("line.separator") + "Data Type: Sufer ASCII Grid";
        Dimension xdim = this.getXDimension();
        Dimension ydim = this.getYDimension();
        dataInfo += System.getProperty("line.separator") + "XNum = " + String.valueOf(xdim.getDimLength())
                + "  YNum = " + String.valueOf(ydim.getDimLength());
        dataInfo += System.getProperty("line.separator") + "XMin = " + String.valueOf(xdim.getValues()[0])
                + "  YMin = " + String.valueOf(ydim.getValues()[0]);
        dataInfo += System.getProperty("line.separator") + "XSize = " + String.valueOf(xdim.getValues()[1] - xdim.getValues()[0])
                + "  YSize = " + String.valueOf(ydim.getValues()[1] - ydim.getValues()[0]);
        dataInfo += System.getProperty("line.separator") + "UNDEF = " + String.valueOf(this.getMissingValue());

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
        try {
            Section section = new Section(origin, size, stride);
            Array dataArray = Array.factory(DataType.FLOAT, section.getShape());
            int rangeIdx = 0;
            Range yRange = section.getRange(rangeIdx++);
            Range xRange = section.getRange(rangeIdx);
            IndexIterator ii = dataArray.getIndexIterator();
            readXY(yRange, xRange, ii);

            return dataArray;
        } catch (InvalidRangeException ex) {
            Logger.getLogger(ASCIIGridDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private void readXY(Range yRange, Range xRange, IndexIterator ii) {
        try {
            int xNum = this.getXDimension().getDimLength();
            int yNum = this.getYDimension().getDimLength();
            float[][] theData = new float[yNum][xNum];
            BufferedReader sr = new BufferedReader(new FileReader(new File(this.getFileName())));
            String[] dataArray;
            int i, j;
            String aLine;
            
            for (i = 0; i < 6; i++) {
                sr.readLine();
            }
            
            List<String> dataList = new ArrayList<>();
            int col = 0;
            do {
                aLine = sr.readLine();
                if (aLine == null) {
                    break;
                }
                dataArray = aLine.trim().split("\\s+");
                dataList.addAll(Arrays.asList(dataArray));
                if (col == 0) {
                    if (!MIMath.isNumeric(dataList.get(0))) {
                        aLine = sr.readLine();
                        dataArray = aLine.trim().split("\\s+");
                        dataList = Arrays.asList(dataArray);
                    }
                }
                for (i = 0; i < 100; i++) {
                    if (dataList.size() < xNum) {
                        aLine = sr.readLine();
                        if (aLine == null) {
                            break;
                        }
                        dataArray = aLine.trim().split("\\s+");
                        dataList.addAll(Arrays.asList(dataArray));
                    } else {
                        break;
                    }
                }
                for (i = 0; i < xNum; i++) {
                    theData[col][i] = Float.parseFloat(dataList.get(i));
                }
                if (dataList.size() > xNum) {
                    dataList = dataList.subList(xNum, dataList.size() - 1);
                } else {
                    dataList = new ArrayList<>();
                }
                col += 1;
            } while (aLine != null);
            
            sr.close();
            
            float[] data = new float[yNum * xNum];
            for (i = 0; i < yNum; i++) {
                for (j = 0; j < xNum; j++) {
                    data[i * xNum + j] = theData[yNum - 1 - i][j];
                }
            }

            for (int y = yRange.first(); y <= yRange.last();
                    y += yRange.stride()) {
                for (int x = xRange.first(); x <= xRange.last();
                        x += xRange.stride()) {
                    int index = y * xNum + x;
                    ii.setFloatNext(data[index]);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ASCIIGridDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ASCIIGridDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public GridData getGridData_LonLat(int timeIdx, int varIdx, int levelIdx) {
        try {
            int xNum = this.getXDimension().getDimLength();
            int yNum = this.getYDimension().getDimLength();
            double[][] theData = new double[yNum][xNum];
            BufferedReader sr = new BufferedReader(new FileReader(new File(this.getFileName())));
            String[] dataArray;
            int i, j;
            String aLine;
            
            for (i = 0; i < 6; i++) {
                sr.readLine();
            }
            
            List<String> dataList = new ArrayList<>();
            int col = 0;
            do {
                aLine = sr.readLine();
                if (aLine == null) {
                    break;
                }
                dataArray = aLine.trim().split("\\s+");
                dataList.addAll(Arrays.asList(dataArray));
                if (col == 0) {
                    if (!MIMath.isNumeric(dataList.get(0))) {
                        aLine = sr.readLine();
                        dataArray = aLine.trim().split("\\s+");
                        dataList = Arrays.asList(dataArray);
                    }
                }
                for (i = 0; i < 100; i++) {
                    if (dataList.size() < xNum) {
                        aLine = sr.readLine();
                        if (aLine == null) {
                            break;
                        }
                        dataArray = aLine.trim().split("\\s+");
                        dataList.addAll(Arrays.asList(dataArray));
                    } else {
                        break;
                    }
                }
                for (i = 0; i < xNum; i++) {
                    theData[col][i] = Double.parseDouble(dataList.get(i));
                }
                if (dataList.size() > xNum) {
                    dataList = dataList.subList(xNum, dataList.size() - 1);
                } else {
                    dataList = new ArrayList<>();
                }
                col += 1;
            } while (aLine != null);
            
            sr.close();
            
            double[][] newGridData = new double[yNum][xNum];
            for (i = 0; i < yNum; i++) {
                for (j = 0; j < xNum; j++) {
                    newGridData[i][j] = theData[yNum - 1 - i][j];
                }
            }
            
            GridData aGridData = new GridData();
            aGridData.data = newGridData;
            aGridData.xArray = this.getXDimension().getValues();
            aGridData.yArray = this.getYDimension().getValues();
            aGridData.missingValue = this.getMissingValue();
            
            return aGridData;
        } catch (IOException ex) {
            Logger.getLogger(ASCIIGridDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
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
    // </editor-fold>
}
