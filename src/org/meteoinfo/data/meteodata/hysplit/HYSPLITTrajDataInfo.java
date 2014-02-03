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
package org.meteoinfo.data.meteodata.hysplit;

import org.meteoinfo.data.mapdata.Field;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.data.meteodata.Dimension;
import org.meteoinfo.data.meteodata.DimensionType;
import org.meteoinfo.data.meteodata.TrajDataInfo;
import org.meteoinfo.data.meteodata.Variable;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import org.meteoinfo.global.table.DataTypes;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meteoinfo.layer.LayerDrawType;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.legend.LegendManage;
import org.meteoinfo.legend.LegendScheme;
import org.meteoinfo.legend.LegendType;
import org.meteoinfo.shape.PointShape;
import org.meteoinfo.shape.PolylineShape;
import org.meteoinfo.shape.ShapeTypes;

/**
 *
 * @author yaqiang
 */
public class HYSPLITTrajDataInfo extends DataInfo implements TrajDataInfo {
    // <editor-fold desc="Variables">
/// <summary>
    /// File name
    /// </summary>

    public List<String> FileNames;
    /// <summary>
    /// Number of meteorological files
    /// </summary>
    public List<Integer> MeteoFileNums;
    /// <summary>
    /// Number of trajectories
    /// </summary>
    public int TrajeoryNumber;
    /// <summary>
    /// Number of trajectories
    /// </summary>
    public List<Integer> TrajeoryNums;
    /// <summary>
    /// Trajectory direction - foreward or backward
    /// </summary>
    public List<String> TrajDirections;
    /// <summary>
    /// Vertical motion
    /// </summary>
    public List<String> VerticalMotions;
    /// <summary>
    /// Information list of trajectories
    /// </summary>
    public List<List<TrajectoryInfo>> TrajInfos;
    /// <summary>
    /// Number of variables
    /// </summary>
    public List<Integer> VarNums;
    /// <summary>
    /// Variable name list
    /// </summary>
    public List<List<String>> VarNames;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    public HYSPLITTrajDataInfo() {
        initVariables();
    }

    private void initVariables() {
        FileNames = new ArrayList<String>();
        MeteoFileNums = new ArrayList<Integer>();
        TrajeoryNums = new ArrayList<Integer>();
        TrajDirections = new ArrayList<String>();
        VerticalMotions = new ArrayList<String>();
        TrajInfos = new ArrayList<List<TrajectoryInfo>>();
        VarNums = new ArrayList<Integer>();
        VarNames = new ArrayList<List<String>>();
        TrajeoryNumber = 0;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">

    @Override
    public void readDataInfo(String fileName) {
        String[] trajFiles = new String[1];
        trajFiles[0] = fileName;
        try {
            readDataInfo(trajFiles);
        } catch (IOException ex) {
            Logger.getLogger(HYSPLITTrajDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void readDataInfo(String[] trajFiles) throws IOException {
        this.setFileName(trajFiles[0]);
        String aLine;
        String[] dataArray;
        int i, t;

        initVariables();
        List<Double> times = new ArrayList<Double>();

        for (t = 0; t < trajFiles.length; t++) {
            String aFile = trajFiles[t];
            FileNames.add(aFile);

            BufferedReader sr = new BufferedReader(new FileReader(new File(aFile)));

            //Record #1
            aLine = sr.readLine().trim();
            dataArray = aLine.split("\\s+");
            MeteoFileNums.add(Integer.parseInt(dataArray[0]));

            //Record #2
            for (i = 0; i < MeteoFileNums.get(t); i++) {
                sr.readLine();
            }

            //Record #3
            aLine = sr.readLine().trim();
            dataArray = aLine.split("\\s+");
            TrajeoryNums.add(Integer.parseInt(dataArray[0]));
            TrajeoryNumber += TrajeoryNums.get(t);
            TrajDirections.add(dataArray[1]);
            VerticalMotions.add(dataArray[2]);

            //Record #4  
            TrajectoryInfo aTrajInfo;
            List<TrajectoryInfo> trajInfoList = new ArrayList<TrajectoryInfo>();
            for (i = 0; i < TrajeoryNums.get(t); i++) {
                aLine = sr.readLine().trim();
                dataArray = aLine.split("\\s+");
                int y = Integer.parseInt(dataArray[0]);
                if (y < 100) {
                    if (y > 50) {
                        y = 1900 + y;
                    } else {
                        y = 2000 + y;
                    }
                }
                Calendar cal = new GregorianCalendar(y, Integer.parseInt(dataArray[1]) - 1,
                        Integer.parseInt(dataArray[2]), Integer.parseInt(dataArray[3]), 0, 0);

                if (times.isEmpty()) {
                    times.add(DataConvert.toOADate(cal.getTime()));
                }
                aTrajInfo = new TrajectoryInfo();
                aTrajInfo.startTime = cal.getTime();
                aTrajInfo.startLat = Float.parseFloat(dataArray[4]);
                aTrajInfo.startLon = Float.parseFloat(dataArray[5]);
                aTrajInfo.startHeight = Float.parseFloat(dataArray[6]);
                trajInfoList.add(aTrajInfo);
            }
            TrajInfos.add(trajInfoList);
            Dimension tdim = new Dimension(DimensionType.T);
            tdim.setValues(times);

            //Record #5
            aLine = sr.readLine().trim();
            dataArray = aLine.split("\\s+");
            VarNums.add(Integer.parseInt(dataArray[0]));
            List<String> varNameList = new ArrayList<String>();
            for (i = 0; i < VarNums.get(t); i++) {
                varNameList.add(dataArray[i + 1]);
            }
            VarNames.add(varNameList);

            Variable var = new Variable();
            var.setName("Traj");
            var.setStation(true);
            var.setDimension(tdim);
            List<Variable> variables = new ArrayList<Variable>();
            variables.add(var);
            this.setVariables(variables);

            sr.close();
        }
    }

    @Override
    public String generateInfoText() {
        String dataInfo = "";
        for (int t = 0; t < FileNames.size(); t++) {
            int i;
            dataInfo += "File Name: " + FileNames.get(t);
            dataInfo += System.getProperty("line.separator") + "Trajectory number = " + String.valueOf(TrajeoryNums.get(t));
            dataInfo += System.getProperty("line.separator") + "Trajectory direction = " + TrajDirections.get(t);
            dataInfo += System.getProperty("line.separator") + "Vertical motion =" + VerticalMotions.get(t);
            dataInfo += System.getProperty("line.separator") + "Number of diagnostic output variables = "
                    + String.valueOf(VarNums.get(t));
            dataInfo += System.getProperty("line.separator") + "Variables:";
            for (i = 0; i < VarNums.get(t); i++) {
                dataInfo += " " + VarNames.get(t).get(i);
            }
            dataInfo += System.getProperty("line.separator") + System.getProperty("line.separator") + "Trajectories:";
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:00");
            for (TrajectoryInfo aTrajInfo : TrajInfos.get(t)) {
                dataInfo += System.getProperty("line.separator") + "  " + format.format(aTrajInfo.startTime)
                        + "  " + String.valueOf(aTrajInfo.startLat) + "  " + String.valueOf(aTrajInfo.startLon)
                        + "  " + String.valueOf(aTrajInfo.startHeight);
            }

            if (t < FileNames.size() - 1) {
                dataInfo += System.getProperty("line.separator") + System.getProperty("line.separator")
                        + "******************************" + System.getProperty("line.separator");
            }
        }

        return dataInfo;
    }

    @Override
    public VectorLayer createTrajLineLayer() {
        VectorLayer aLayer = new VectorLayer(ShapeTypes.Polyline);
        aLayer.editAddField(new Field("TrajID", DataTypes.Integer));
        aLayer.editAddField(new Field("StartDate", DataTypes.String));
        aLayer.editAddField(new Field("StartLon", DataTypes.Double));
        aLayer.editAddField(new Field("StartLat", DataTypes.Double));
        aLayer.editAddField(new Field("StartHeight", DataTypes.Double));

        int TrajNum = 0;
        for (int t = 0; t < FileNames.size(); t++) {
            try {
                String aFile = FileNames.get(t);
                BufferedReader sr = new BufferedReader(new FileReader(new File(aFile)));
                String aLine;
                String[] dataArray;
                int i;

                //Record #1
                sr.readLine();

                //Record #2
                for (i = 0; i < MeteoFileNums.get(t); i++) {
                    sr.readLine();
                }

                //Record #3
                sr.readLine();

                //Record #4             
                for (i = 0; i < TrajeoryNums.get(t); i++) {
                    sr.readLine();
                }

                //Record #5
                sr.readLine();

                //Record #6
                int TrajIdx;
                List<PointD> pList;
                List<List<PointD>> PointList = new ArrayList<List<PointD>>();
                for (i = 0; i < TrajeoryNums.get(t); i++) {
                    pList = new ArrayList<PointD>();
                    PointList.add(pList);
                }
                PointD aPoint;
                //ArrayList polylines = new ArrayList();
                while (true) {
                    aLine = sr.readLine();
                    if (aLine == null) {
                        break;
                    }
                    if (aLine.isEmpty()) {
                        continue;
                    }
                    aLine = aLine.trim();
                    dataArray = aLine.split("\\s+");
                    TrajIdx = Integer.parseInt(dataArray[0]) - 1;
                    aPoint = new PointD();
                    aPoint.X = Double.parseDouble(dataArray[10]);
                    aPoint.Y = Double.parseDouble(dataArray[9]);

                    if (PointList.get(TrajIdx).size() > 1) {
                        PointD oldPoint = PointList.get(TrajIdx).get(PointList.get(TrajIdx).size() - 1);
                        if (Math.abs(aPoint.X - oldPoint.X) > 100) {
                            if (aPoint.X > oldPoint.X) {
                                aPoint.X -= 360;
                            } else {
                                aPoint.X += 360;
                            }
                        }
                    }
                    PointList.get(TrajIdx).add(aPoint);
                }

                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHH");
                for (i = 0; i < TrajeoryNums.get(t); i++) {
                    PolylineShape aPolyline = new PolylineShape();
                    TrajNum += 1;
                    aPolyline.value = TrajNum;
                    aPolyline.setPoints(PointList.get(i));
                    aPolyline.setExtent(MIMath.getPointsExtent(aPolyline.getPoints()));

                    int shapeNum = aLayer.getShapeNum();
                    if (aLayer.editInsertShape(aPolyline, shapeNum)) {
                        aLayer.editCellValue("TrajID", shapeNum, TrajNum);
                        aLayer.editCellValue("StartDate", shapeNum, format.format(TrajInfos.get(t).get(i).startTime));
                        aLayer.editCellValue("StartLat", shapeNum, TrajInfos.get(t).get(i).startLat);
                        aLayer.editCellValue("StartLon", shapeNum, TrajInfos.get(t).get(i).startLon);
                        aLayer.editCellValue("StartHeight", shapeNum, TrajInfos.get(t).get(i).startHeight);
                    }
                }

                sr.close();
            } catch (IOException ex) {
                Logger.getLogger(HYSPLITTrajDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(HYSPLITTrajDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        aLayer.setLayerName("Trajectory_Lines");
        aLayer.setLayerDrawType(LayerDrawType.TrajLine);
        //aLayer.LegendScheme = m_Legend.CreateSingleSymbolLegendScheme(Shape.ShapeType.Polyline, Color.Blue, 1.0F, 1, aDataInfo.TrajeoryNum);            
        aLayer.setVisible(true);
        //LegendScheme aLS = LegendManage.createUniqValueLegendScheme(aLayer, 1, TrajeoryNumber);
        aLayer.updateLegendScheme(LegendType.UniqueValue, "TrajID");
        //aLS.setFieldName("TrajID");
        //aLayer.setLegendScheme(aLS);

        return aLayer;
    }

    @Override
    public VectorLayer createTrajPointLayer() {
        VectorLayer aLayer = new VectorLayer(ShapeTypes.Point);
        aLayer.editAddField(new Field("TrajID", DataTypes.Integer));
        aLayer.editAddField(new Field("Date", DataTypes.String));
        aLayer.editAddField(new Field("Lon", DataTypes.Double));
        aLayer.editAddField(new Field("Lat", DataTypes.Double));
        aLayer.editAddField(new Field("Height", DataTypes.Double));
        aLayer.editAddField(new Field("Pressure", DataTypes.Double));
        boolean isMultiVar = false;
        if (VarNums.get(0) > 1) {
            isMultiVar = true;
            for (int v = 1; v < VarNums.get(0); v++) {
                aLayer.editAddField(new Field(VarNames.get(0).get(v), DataTypes.Double));
            }
        }

        int TrajNum = 0;
        for (int t = 0; t < FileNames.size(); t++) {
            try {
                String aFile = FileNames.get(t);
                BufferedReader sr = new BufferedReader(new FileReader(new File(aFile)));
                String aLine;
                String[] dataArray;
                int i;

                //Record #1
                aLine = sr.readLine();

                //Record #2
                for (i = 0; i < MeteoFileNums.get(t); i++) {
                    aLine = sr.readLine();
                }

                //Record #3
                aLine = sr.readLine();

                //Record #4             
                for (i = 0; i < TrajeoryNums.get(t); i++) {
                    aLine = sr.readLine();
                }

                //Record #5
                aLine = sr.readLine();

                //Record #6
                int TrajIdx;
                List<List<Object>> pList = new ArrayList<List<Object>>();
                List<List<List<Object>>> PointList = new ArrayList<List<List<Object>>>();
                for (i = 0; i < TrajeoryNums.get(t); i++) {
                    pList = new ArrayList<List<Object>>();
                    PointList.add(pList);
                }
                PointD aPoint = new PointD();
                ArrayList polylines = new ArrayList();
                Date aDate;
                double Height, Press;
                while (true) {
                    aLine = sr.readLine();
                    if (aLine == null) {
                        break;
                    }
                    if (aLine.isEmpty()) {
                        continue;
                    }
                    aLine = aLine.trim();
                    dataArray = aLine.split("\\s+");
                    List<Object> dList = new ArrayList<Object>();
                    TrajIdx = Integer.parseInt(dataArray[0]) - 1;
                    Calendar cal = new GregorianCalendar(Integer.parseInt(dataArray[2]), Integer.parseInt(dataArray[3]) - 1,
                            Integer.parseInt(dataArray[4]), Integer.parseInt(dataArray[5]), 0, 0);
                    aPoint = new PointD();
                    aPoint.X = Double.parseDouble(dataArray[10]);
                    aPoint.Y = Double.parseDouble(dataArray[9]);
                    Height = Double.parseDouble(dataArray[11]);
                    Press = Double.parseDouble(dataArray[12]);
                    dList.add(aPoint);
                    dList.add(cal.getTime());
                    dList.add(Height);
                    dList.add(Press);
                    if (isMultiVar) {
                        for (i = 13; i < dataArray.length; i++) {
                            dList.add(Double.parseDouble(dataArray[i]));
                        }
                    }
                    PointList.get(TrajIdx).add(dList);
                }

                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHH");
                for (i = 0; i < TrajeoryNums.get(t); i++) {
                    TrajNum += 1;
                    for (int j = 0; j < PointList.get(i).size(); j++) {
                        PointShape aPS = new PointShape();
                        aPS.setValue(TrajNum);
                        aPS.setPoint((PointD) PointList.get(i).get(j).get(0));
                        int shapeNum = aLayer.getShapeNum();
                        if (aLayer.editInsertShape(aPS, shapeNum)) {
                            aLayer.editCellValue("TrajID", shapeNum, TrajNum);
                            aLayer.editCellValue("Date", shapeNum, format.format((Date) PointList.get(i).get(j).get(1)));
                            aLayer.editCellValue("Lat", shapeNum, aPS.getPoint().Y);
                            aLayer.editCellValue("Lon", shapeNum, aPS.getPoint().X);
                            aLayer.editCellValue("Height", shapeNum, PointList.get(i).get(j).get(2));
                            aLayer.editCellValue("Pressure", shapeNum, PointList.get(i).get(j).get(3));
                            if (isMultiVar) {
                                for (int v = 1; v < VarNums.get(0); v++) {
                                    aLayer.editCellValue(VarNames.get(0).get(v), shapeNum, PointList.get(i).get(j).get(3 + v));
                                }
                            }
                        }
                    }
                }

                sr.close();
            } catch (IOException ex) {
                Logger.getLogger(HYSPLITTrajDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(HYSPLITTrajDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        aLayer.setLayerName("Trajectory_Points");
        aLayer.setLayerDrawType(LayerDrawType.TrajLine);
        //aLayer.LegendScheme = m_Legend.CreateSingleSymbolLegendScheme(Shape.ShapeType.Polyline, Color.Blue, 1.0F, 1, aDataInfo.TrajeoryNum);            
        aLayer.setVisible(true);
        LegendScheme aLS = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Point, Color.red, 5.0F);
        aLS.setFieldName("TrajID");
        aLayer.setLegendScheme(aLS);

        return aLayer;
    }

    @Override
    public VectorLayer createTrajStartPointLayer() {
        VectorLayer aLayer = new VectorLayer(ShapeTypes.Point);
        aLayer.editAddField(new Field("TrajID", DataTypes.Integer));
        aLayer.editAddField(new Field("StartDate", DataTypes.String));
        aLayer.editAddField(new Field("StartLon", DataTypes.Double));
        aLayer.editAddField(new Field("StartLat", DataTypes.Double));
        aLayer.editAddField(new Field("StartHeight", DataTypes.Double));

        int TrajNum = 0;
        for (int t = 0; t < FileNames.size(); t++) {
            try {
                String aFile = FileNames.get(t);
                BufferedReader sr = new BufferedReader(new FileReader(new File(aFile)));
                String aLine;
                String[] dataArray;
                int i;

                //Record #1
                aLine = sr.readLine();

                //Record #2
                for (i = 0; i < MeteoFileNums.get(t); i++) {
                    aLine = sr.readLine();
                }

                //Record #3
                aLine = sr.readLine();

                //Record #4             
                for (i = 0; i < TrajeoryNums.get(t); i++) {
                    aLine = sr.readLine();
                }

                //Record #5
                aLine = sr.readLine();

                //Record #6
                int TrajIdx;
                List<Object> pList = new ArrayList<Object>();
                List<PointD> PointList = new ArrayList<PointD>();
                PointD aPoint = new PointD();
                for (i = 0; i < TrajeoryNums.get(t); i++) {
                    PointList.add(aPoint);
                }

                ArrayList polylines = new ArrayList();
                while (true) {
                    aLine = sr.readLine();
                    if (aLine == null) {
                        break;
                    }
                    if (aLine.isEmpty()) {
                        continue;
                    }
                    aLine = aLine.trim();
                    dataArray = aLine.split("\\s+");
                    if (Float.parseFloat(dataArray[8]) == 0) {
                        TrajIdx = Integer.parseInt(dataArray[0]) - 1;
                        aPoint = new PointD();
                        aPoint.X = Double.parseDouble(dataArray[10]);
                        aPoint.Y = Double.parseDouble(dataArray[9]);
                        PointList.set(TrajIdx, aPoint);
                    }
                }

                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHH");
                for (i = 0; i < TrajeoryNums.get(t); i++) {
                    PointShape aPS = new PointShape();
                    TrajNum += 1;
                    aPS.setValue(TrajNum);
                    aPS.setPoint(PointList.get(i));

                    int shapeNum = aLayer.getShapeNum();
                    if (aLayer.editInsertShape(aPS, shapeNum)) {
                        aLayer.editCellValue("TrajID", shapeNum, TrajNum);
                        aLayer.editCellValue("StartDate", shapeNum, format.format(TrajInfos.get(t).get(i).startTime));
                        aLayer.editCellValue("StartLat", shapeNum, TrajInfos.get(t).get(i).startLat);
                        aLayer.editCellValue("StartLon", shapeNum, TrajInfos.get(t).get(i).startLon);
                        aLayer.editCellValue("StartHeight", shapeNum, TrajInfos.get(t).get(i).startHeight);
                    }
                }

                sr.close();
            } catch (IOException ex) {
                Logger.getLogger(HYSPLITTrajDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(HYSPLITTrajDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        aLayer.setLayerName("Trajectory_Start_Points");
        aLayer.setLayerDrawType(LayerDrawType.TrajPoint);
        aLayer.setVisible(true);
        LegendScheme aLS = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Point, Color.black, 8.0F);
        aLS.setFieldName("TrajID");
        aLayer.setLegendScheme(aLS);

        return aLayer;
    }
    // </editor-fold>
}
