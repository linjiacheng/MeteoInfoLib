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

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meteoinfo.data.mapdata.Field;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.data.meteodata.Dimension;
import org.meteoinfo.data.meteodata.DimensionType;
import org.meteoinfo.data.meteodata.MeteoDataType;
import org.meteoinfo.data.meteodata.TrajDataInfo;
import org.meteoinfo.data.meteodata.Variable;
import org.meteoinfo.data.meteodata.hysplit.TrajectoryInfo;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import org.meteoinfo.global.table.DataTypes;
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
public class MICAPS7DataInfo extends DataInfo implements TrajDataInfo {

    // <editor-fold desc="Variables">
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

    /**
     * Constructor
     */
    public MICAPS7DataInfo() {
        this.setDataType(MeteoDataType.MICAPS_7);
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
            Logger.getLogger(MICAPS7DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void readDataInfo(String[] trajFiles) throws IOException {
        this.setFileName(trajFiles[0]);
        String aLine;
        String[] dataArray;
        int t;

        initVariables();
        List<Double> times = new ArrayList<Double>();

        for (t = 0; t < trajFiles.length; t++) {
            String aFile = trajFiles[t];
            FileNames.add(aFile);

            BufferedReader sr = new BufferedReader(new FileReader(new File(aFile)));

            TrajectoryInfo aTrajInfo = new TrajectoryInfo();
            List<TrajectoryInfo> trajInfoList = new ArrayList<TrajectoryInfo>();
            sr.readLine();
            aLine = sr.readLine();
            int trajIdx = -1;
            int trajNum = 0;
            while (aLine != null) {
                if (aLine.trim().isEmpty()) {
                    aLine = sr.readLine();
                    continue;
                }

                dataArray = aLine.split("\\s+");
                if (dataArray.length == 4) {
                    aTrajInfo = new TrajectoryInfo();
                    aTrajInfo.trajName = dataArray[0];
                    aTrajInfo.trajID = dataArray[1];
                    aTrajInfo.trajCenter = dataArray[2];
                    trajIdx = -1;
                    trajNum += 1;
                } else if (dataArray.length == 13) {
                    trajIdx += 1;
                    if (trajIdx == 0) {
                        int year = Integer.parseInt(dataArray[0]);
                        if (year < 100) {
                            if (year < 50) {
                                year = 2000 + year;
                            } else {
                                year = 1900 + year;
                            }
                        }
                        Calendar cal = new GregorianCalendar(year, Integer.parseInt(dataArray[1]) - 1,
                                Integer.parseInt(dataArray[2]), Integer.parseInt(dataArray[3]), 0, 0);
                        if (times.isEmpty()) {
                            times.add(DataConvert.toOADate(cal.getTime()));
                        }

                        aTrajInfo.startTime = cal.getTime();
                        aTrajInfo.startLat = Float.parseFloat(dataArray[6]);
                        aTrajInfo.startLon = Float.parseFloat(dataArray[5]);
                        trajInfoList.add(aTrajInfo);
                    }
                }
                aLine = sr.readLine();
            }
            TrajeoryNums.add(trajNum);
            TrajeoryNumber += TrajeoryNums.get(t);
            TrajInfos.add(trajInfoList);

            Dimension tdim = new Dimension(DimensionType.T);
            tdim.setValues(times);
            this.setTimeDimension(tdim);

            sr.close();

            Variable var = new Variable();
            var.setName("Traj");
            var.setStation(true);
            var.setDimension(tdim);
            List<Variable> variables = new ArrayList<Variable>();
            variables.add(var);
            this.setVariables(variables);

        }
    }

    @Override
    public String generateInfoText() {
        String dataInfo = "";
        for (int t = 0; t < FileNames.size(); t++) {
            int i;
            dataInfo += "File Name: " + FileNames.get(t);
            dataInfo += System.getProperty("line.separator") + "Typhoon number = " + String.valueOf(TrajeoryNums.get(t));
            dataInfo += System.getProperty("line.separator") + System.getProperty("line.separator") + "Typhoons:";
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:00");
            for (TrajectoryInfo aTrajInfo : TrajInfos.get(t)) {
                dataInfo += System.getProperty("line.separator") + "  " + aTrajInfo.trajName + " "
                        + aTrajInfo.trajID + " " + aTrajInfo.trajCenter + " " + format.format(aTrajInfo.startTime)
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
        aLayer.editAddField(new Field("TrajIndex", DataTypes.Integer));
        aLayer.editAddField(new Field("TrajName", DataTypes.String));
        aLayer.editAddField(new Field("TrajID", DataTypes.String));
        aLayer.editAddField(new Field("TrajCenter", DataTypes.String));
        aLayer.editAddField(new Field("StartDate", DataTypes.String));
        aLayer.editAddField(new Field("StartLon", DataTypes.Double));
        aLayer.editAddField(new Field("StartLat", DataTypes.Double));
        aLayer.editAddField(new Field("StartHeight", DataTypes.Double));

        int TrajNum = 0;
        for (int t = 0; t < FileNames.size(); t++) {
            BufferedReader sr = null;
            try {
                String aFile = FileNames.get(t);
                sr = new BufferedReader(new FileReader(new File(aFile)));
                String aLine;
                String[] dataArray;
                int i;
                int TrajIdx = -1;
                List<PointD> pList;
                List<List<PointD>> PointList = new ArrayList<List<PointD>>();
                for (i = 0; i < TrajeoryNums.get(t); i++) {
                    pList = new ArrayList<PointD>();
                    PointList.add(pList);
                }
                PointD aPoint;
                sr.readLine();
                aLine = sr.readLine();
                while (aLine != null) {
                    if (aLine.trim().isEmpty()) {
                        aLine = sr.readLine();
                        continue;
                    }
                    dataArray = aLine.split("\\s+");
                    if (dataArray.length == 4) {
                        TrajIdx += 1;
                    } else if (dataArray.length == 13) {
                        aPoint = new PointD();
                        aPoint.X = Double.parseDouble(dataArray[5]);
                        aPoint.Y = Double.parseDouble(dataArray[6]);
                        PointList.get(TrajIdx).add(aPoint);
                    }

                    aLine = sr.readLine();
                }
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHH");
                for (i = 0; i < TrajeoryNums.get(t); i++) {
                    PolylineShape aPolyline = new PolylineShape();
                    //aPolyline.value = aDataInfo.TrajInfos[i].StartTime.ToBinary();
                    TrajNum += 1;
                    aPolyline.value = TrajNum;
                    aPolyline.setPoints(PointList.get(i));
                    aPolyline.setExtent(MIMath.getPointsExtent(aPolyline.getPoints()));

                    int shapeNum = aLayer.getShapeNum();
                    if (aLayer.editInsertShape(aPolyline, shapeNum)) {
                        aLayer.editCellValue("TrajIndex", shapeNum, TrajNum);
                        aLayer.editCellValue("TrajName", shapeNum, TrajInfos.get(t).get(i).trajName);
                        aLayer.editCellValue("TrajID", shapeNum, TrajInfos.get(t).get(i).trajID);
                        aLayer.editCellValue("TrajCenter", shapeNum, TrajInfos.get(t).get(i).trajCenter);
                        aLayer.editCellValue("StartDate", shapeNum, format.format(TrajInfos.get(t).get(i).startTime));
                        aLayer.editCellValue("StartLat", shapeNum, TrajInfos.get(t).get(i).startLat);
                        aLayer.editCellValue("StartLon", shapeNum, TrajInfos.get(t).get(i).startLon);
                        aLayer.editCellValue("StartHeight", shapeNum, TrajInfos.get(t).get(i).startHeight);
                    }
                }
                sr.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MICAPS7DataInfo.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MICAPS7DataInfo.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(MICAPS7DataInfo.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    sr.close();
                } catch (IOException ex) {
                    Logger.getLogger(MICAPS7DataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        aLayer.setLayerName("Typhoon_Lines");
        aLayer.setLayerDrawType(LayerDrawType.TrajLine);
        aLayer.setVisible(true);
        aLayer.updateLegendScheme(LegendType.UniqueValue, "TrajID");

        return aLayer;
    }

    @Override
    public VectorLayer createTrajPointLayer() {
        VectorLayer aLayer = new VectorLayer(ShapeTypes.Point);
        aLayer.editAddField(new Field("TrajID", DataTypes.Integer));
        aLayer.editAddField(new Field("Date", DataTypes.String));
        aLayer.editAddField(new Field("PreHour", DataTypes.Integer));
        aLayer.editAddField(new Field("Lon", DataTypes.Double));
        aLayer.editAddField(new Field("Lat", DataTypes.Double));
        aLayer.editAddField(new Field("WindSpeed", DataTypes.Double));
        aLayer.editAddField(new Field("Radius_W7", DataTypes.Double));
        aLayer.editAddField(new Field("Radius_W10", DataTypes.Double));
        aLayer.editAddField(new Field("MoveDir", DataTypes.Double));
        aLayer.editAddField(new Field("MoveSpeed", DataTypes.Double));

        int TrajNum = 0;
        for (int t = 0; t < FileNames.size(); t++) {
            BufferedReader sr = null;
            try {
                String aFile = FileNames.get(t);
                sr = new BufferedReader(new FileReader(new File(aFile)));
                String aLine;
                String[] dataArray;
                int i;

                //
                int TrajIdx = -1;
                List<List<Object>> pList;
                List<List<List<Object>>> PointList = new ArrayList<List<List<Object>>>();
                for (i = 0; i < TrajeoryNums.get(t); i++) {
                    pList = new ArrayList<List<Object>>();
                    PointList.add(pList);
                }
                PointD aPoint;
                sr.readLine();
                aLine = sr.readLine();
                while (aLine != null) {
                    if (aLine.trim().isEmpty()) {
                        aLine = sr.readLine();
                        continue;
                    }
                    dataArray = aLine.split("\\s+");
                    if (dataArray.length == 4) {
                        TrajIdx += 1;
                    } else if (dataArray.length == 13) {
                        List<Object> dList = new ArrayList<Object>();
                        Calendar cal = new GregorianCalendar(Integer.parseInt(dataArray[0]), Integer.parseInt(dataArray[1]) - 1,
                                Integer.parseInt(dataArray[2]), Integer.parseInt(dataArray[3]), 0, 0);
                        aPoint = new PointD();
                        aPoint.X = Double.parseDouble(dataArray[5]);
                        aPoint.Y = Double.parseDouble(dataArray[6]);
                        dList.add(aPoint);
                        dList.add(cal.getTime());
                        dList.add(Integer.parseInt(dataArray[4]));
                        for (int d = 0; d < 5; d++) {
                            dList.add(Double.parseDouble(dataArray[d + 7]));
                        }
                        PointList.get(TrajIdx).add(dList);
                    }

                    aLine = sr.readLine();
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
                            aLayer.editCellValue("Date", shapeNum, format.format(PointList.get(i).get(j).get(1)));
                            aLayer.editCellValue("PreHour", shapeNum, Integer.parseInt(PointList.get(i).get(j).get(2).toString()));
                            aLayer.editCellValue("Lat", shapeNum, aPS.getPoint().Y);
                            aLayer.editCellValue("Lon", shapeNum, aPS.getPoint().X);
                            aLayer.editCellValue("WindSpeed", shapeNum, Double.parseDouble(PointList.get(i).get(j).get(3).toString()));
                            aLayer.editCellValue("Radius_W7", shapeNum, Double.parseDouble(PointList.get(i).get(j).get(4).toString()));
                            aLayer.editCellValue("Radius_W10", shapeNum, Double.parseDouble(PointList.get(i).get(j).get(5).toString()));
                            aLayer.editCellValue("MoveDir", shapeNum, Double.parseDouble(PointList.get(i).get(j).get(6).toString()));
                            aLayer.editCellValue("MoveSpeed", shapeNum, Double.parseDouble(PointList.get(i).get(j).get(7).toString()));
                        }
                    }
                }
                sr.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MICAPS7DataInfo.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MICAPS7DataInfo.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(MICAPS7DataInfo.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    sr.close();
                } catch (IOException ex) {
                    Logger.getLogger(MICAPS7DataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        aLayer.setLayerName("Typhoon_Points");
        aLayer.setLayerDrawType(LayerDrawType.TrajPoint);
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
            BufferedReader sr = null;
            try {
                String aFile = FileNames.get(t);
                sr = new BufferedReader(new FileReader(new File(aFile)));
                String aLine;
                String[] dataArray;
                int i;

                //
                int TrajIdx = -1;
                List<PointD> PointList = new ArrayList<PointD>();
                PointD aPoint;
                for (i = 0; i < TrajeoryNums.get(t); i++) {
                    PointList.add(new PointD());
                }
                sr.readLine();
                aLine = sr.readLine();
                boolean IsFirstTraj = false;
                while (aLine != null) {
                    if (aLine.trim().isEmpty()) {
                        aLine = sr.readLine();
                        continue;
                    }
                    dataArray = aLine.split("\\s+");
                    if (dataArray.length == 4) {
                        TrajIdx += 1;
                        IsFirstTraj = true;
                    } else if (dataArray.length == 13) {
                        if (IsFirstTraj) {
                            aPoint = new PointD();
                            aPoint.X = Double.parseDouble(dataArray[5]);
                            aPoint.Y = Double.parseDouble(dataArray[6]);
                            PointList.set(TrajIdx, aPoint);
                            IsFirstTraj = false;
                        }
                    }

                    aLine = sr.readLine();
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
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MICAPS7DataInfo.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MICAPS7DataInfo.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(MICAPS7DataInfo.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    sr.close();
                } catch (IOException ex) {
                    Logger.getLogger(MICAPS7DataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        aLayer.setLayerName("Typhoon_Start_Points");
        aLayer.setLayerDrawType(LayerDrawType.TrajPoint);
        aLayer.setVisible(true);
        LegendScheme aLS = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Point, Color.black, 8.0F);
        aLS.setFieldName("TrajID");
        aLayer.setLegendScheme(aLS);

        return aLayer;
    }

    /**
     * Get a trajectory points data
     *
     * @param aTrajIdx The trajectory index
     * @return A trajectory points data
     */
    public List<List<Object>> getATrajData(int aTrajIdx) {
        List<List<Object>> trajPointsData = new ArrayList<List<Object>>();

        boolean ifExit = false;
        for (int t = 0; t < FileNames.size(); t++) {
            BufferedReader sr = null;
            try {
                String aFile = FileNames.get(t);
                sr = new BufferedReader(new FileReader(new File(aFile)));
                String aLine;
                String[] dataArray;
                //
                int TrajIdx = -1;
                PointD aPoint;
                sr.readLine();
                aLine = sr.readLine();
                while (aLine != null) {
                    if (aLine.trim().isEmpty()) {
                        aLine = sr.readLine();
                        continue;
                    }
                    dataArray = aLine.split("\\s+");
                    switch (dataArray.length) {
                        case 4:
                            TrajIdx += 1;
                            if (TrajIdx > aTrajIdx) {
                                ifExit = true;
                            }

                            break;
                        case 13:
                            if (TrajIdx == aTrajIdx) {
                                List<Object> dList = new ArrayList<Object>();
                                Calendar cal = new GregorianCalendar(Integer.parseInt(dataArray[0]), Integer.parseInt(dataArray[1]) - 1,
                                        Integer.parseInt(dataArray[2]), Integer.parseInt(dataArray[3]), 0, 0);
                                aPoint = new PointD();
                                aPoint.X = Double.parseDouble(dataArray[5]);
                                aPoint.Y = Double.parseDouble(dataArray[6]);
                                dList.add(aPoint);
                                dList.add(cal.getTime());
                                dList.add(Double.parseDouble(dataArray[7]));

                                trajPointsData.add(dList);
                            }
                            break;
                    }
                    if (ifExit) {
                        break;
                    }

                    aLine = sr.readLine();
                }
                sr.close();
                if (ifExit) {
                    break;
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MICAPS7DataInfo.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MICAPS7DataInfo.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    sr.close();
                } catch (IOException ex) {
                    Logger.getLogger(MICAPS7DataInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return trajPointsData;
    }
    // </editor-fold>
}
