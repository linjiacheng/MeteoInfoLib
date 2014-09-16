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

import org.meteoinfo.data.DataMath;
import org.meteoinfo.data.GridData;
import org.meteoinfo.data.StationData;
import org.meteoinfo.data.mapdata.Field;
import org.meteoinfo.drawing.ContourDraw;
import org.meteoinfo.drawing.Draw;
import org.meteoinfo.drawing.MarkerType;
import org.meteoinfo.geoprocess.GeoComputation;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import org.meteoinfo.global.table.DataTypes;
import org.meteoinfo.layer.LayerDrawType;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.legend.LegendManage;
import org.meteoinfo.legend.LegendScheme;
import org.meteoinfo.shape.PointShape;
import org.meteoinfo.shape.PolygonShape;
import org.meteoinfo.shape.PolylineShape;
import org.meteoinfo.shape.ShapeTypes;
import org.meteoinfo.shape.WindArraw;
import org.meteoinfo.shape.WindBarb;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meteoinfo.layer.RasterLayer;
import org.meteoinfo.legend.LegendType;
import org.meteoinfo.legend.PointBreak;
import org.meteoinfo.shape.StationModelShape;

/**
 * Template
 *
 * @author Yaqiang Wang
 */
public class DrawMeteoData {
    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Create contour layer
     *
     * @param gridData Grid data
     * @param aLS Legend scheme
     * @param LName Layer name
     * @param fieldName Field name
     * @param isSmooth If smooth the contour lines
     * @return Vector layer
     */
    public static VectorLayer createContourLayer(GridData gridData, LegendScheme aLS, String LName, String fieldName, boolean isSmooth) {
        Object[] ccs = LegendManage.getContoursAndColors(aLS);
        double[] cValues = (double[]) ccs[0];

        int[][] S1 = new int[gridData.data.length][gridData.data[0].length];
        List<wContour.Global.Border> Borders = ContourDraw.tracingBorders(gridData.data, gridData.xArray, gridData.yArray, S1, gridData.missingValue);
        List<wContour.Global.PolyLine> ContourLines = ContourDraw.tracingContourLines(gridData.data,
                cValues, gridData.xArray, gridData.yArray, gridData.missingValue, Borders, S1);

        if (ContourLines.isEmpty()) {
            return null;
        }

        if (isSmooth) {
            ContourLines = wContour.Contour.smoothLines(ContourLines);
        }

        wContour.Global.PolyLine aLine;
        double aValue;
        VectorLayer aLayer = new VectorLayer(ShapeTypes.Polyline);
        Field aDC = new Field(fieldName, DataTypes.Double);
        aLayer.editAddField(aDC);

        for (int i = 0; i < ContourLines.size(); i++) {
            aLine = ContourLines.get(i);
            aValue = aLine.Value;

            PolylineShape aPolyline = new PolylineShape();
            PointD aPoint;
            List<PointD> pList = new ArrayList<PointD>();
            for (int j = 0; j < aLine.PointList.size(); j++) {
                aPoint = new PointD();
                aPoint.X = aLine.PointList.get(j).X;
                aPoint.Y = aLine.PointList.get(j).Y;
                pList.add(aPoint);
            }
            aPolyline.setPoints(pList);
            aPolyline.value = aValue;
            aPolyline.setExtent(MIMath.getPointsExtent(pList));

            int shapeNum = aLayer.getShapeNum();
            try {
                if (aLayer.editInsertShape(aPolyline, shapeNum)) {
                    aLayer.editCellValue(fieldName, shapeNum, aValue);
                }
            } catch (Exception ex) {
                Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        aLayer.setLayerName(LName);
        aLS.setFieldName(fieldName);
        aLayer.setLegendScheme((LegendScheme) aLS.clone());
        aLayer.setLayerDrawType(LayerDrawType.Contour);

        aLayer.getLabelSet().setDrawLabels(true);
        aLayer.getLabelSet().setDrawShadow(true);
        aLayer.getLabelSet().setShadowColor(Color.white);
        aLayer.getLabelSet().setYOffset(3);
        aLayer.getLabelSet().setFieldName(fieldName);
        aLayer.getLabelSet().setColorByLegend(true);
        aLayer.getLabelSet().setDynamicContourLabel(true);
        //aLayer.addLabels();

        return aLayer;
    }

    /**
     * Create shaded layer
     *
     * @param gridData Grid data
     * @param aLS Legend scheme
     * @param LName Layer name
     * @param fieldName Field name
     * @param isSmooth If smooth the contour lines
     * @return Vector layer
     */
    public static VectorLayer createShadedLayer(GridData gridData, LegendScheme aLS, String LName, String fieldName, boolean isSmooth) {
        List<wContour.Global.PolyLine> ContourLines;
        List<wContour.Global.Polygon> ContourPolygons;

        Object[] ccs = LegendManage.getContoursAndColors(aLS);
        double[] cValues = (double[]) ccs[0];

        double minData = 0;
        double maxData = 0;
        double[] maxmin = new double[2];
        gridData.getMaxMinValue(maxmin);
        maxData = maxmin[0];
        minData = maxmin[1];

        int[][] S1 = new int[gridData.data.length][gridData.data[0].length];
        List<wContour.Global.Border> Borders = ContourDraw.tracingBorders(gridData.data, gridData.xArray, gridData.yArray, S1, gridData.missingValue);
        ContourLines = ContourDraw.tracingContourLines(gridData.data,
                cValues, gridData.xArray, gridData.yArray, gridData.missingValue, Borders, S1);

        if (isSmooth) {
            ContourLines = wContour.Contour.smoothLines(ContourLines);
        }
        ContourPolygons = ContourDraw.tracingPolygons(gridData.data, ContourLines, Borders, cValues);

        wContour.Global.Polygon aPolygon;
        //Color aColor;
        double aValue;
        int valueIdx;
        VectorLayer aLayer = new VectorLayer(ShapeTypes.Polygon);
        Field aDC = new Field(fieldName + "_Low", DataTypes.Double);
        aLayer.editAddField(aDC);
        aDC = new Field(fieldName + "_High", DataTypes.Double);
        aLayer.editAddField(aDC);

        for (int i = 0; i < ContourPolygons.size(); i++) {
            aPolygon = ContourPolygons.get(i);
            aValue = aPolygon.LowValue;

            PointD aPoint;
            List<PointD> pList = new ArrayList<PointD>();
            for (int j = 0; j < aPolygon.OutLine.PointList.size(); j++) {
                aPoint = new PointD();
                aPoint.X = aPolygon.OutLine.PointList.get(j).X;
                aPoint.Y = aPolygon.OutLine.PointList.get(j).Y;
                pList.add(aPoint);
            }
            if (!GeoComputation.isClockwise(pList)) {
                Collections.reverse(pList);
            }

            PolygonShape aPolygonShape = new PolygonShape();
            aPolygonShape.setPoints(pList);
            aPolygonShape.setExtent(MIMath.getPointsExtent(pList));
            aPolygonShape.lowValue = aValue;
            if (aPolygon.HasHoles()) {
                for (int h = 0; h < aPolygon.HoleLines.size(); h++) {
                    pList = new ArrayList<PointD>();
                    for (int j = 0; j < aPolygon.HoleLines.get(h).PointList.size(); j++) {
                        aPoint = new PointD();
                        aPoint.X = aPolygon.HoleLines.get(h).PointList.get(j).X;
                        aPoint.Y = aPolygon.HoleLines.get(h).PointList.get(j).Y;
                        pList.add(aPoint);
                    }
                    aPolygonShape.addHole(pList, 0);
                }
            }

            valueIdx = Arrays.binarySearch(cValues, aValue);
            //valueIdx = Arrays.asList(cValues).indexOf(aValue);
            if (valueIdx == cValues.length - 1) {
                aPolygonShape.highValue = maxData;
            } else {
                aPolygonShape.highValue = cValues[valueIdx + 1];
            }

            if (!aPolygon.IsHighCenter) {
                aPolygonShape.highValue = aValue;
                if (valueIdx == 0) {
                    aPolygonShape.lowValue = minData;
                } else {
                    aPolygonShape.lowValue = cValues[valueIdx - 1];
                }
            }

            int shapeNum = aLayer.getShapeNum();
            try {
                if (aLayer.editInsertShape(aPolygonShape, shapeNum)) {
                    aLayer.editCellValue(fieldName + "_Low", shapeNum, aPolygonShape.lowValue);
                    aLayer.editCellValue(fieldName + "_High", shapeNum, aPolygonShape.highValue);
                }
            } catch (Exception ex) {
                Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        aLayer.setLayerName(LName);
        aLS.setFieldName(fieldName + "_Low");
        aLayer.setLegendScheme((LegendScheme) aLS.clone());
        aLayer.setLayerDrawType(LayerDrawType.Shaded);

        return aLayer;
    }

    /**
     * Create grid fill layer
     *
     * @param gridData Grid data
     * @param aLS Legend scheme
     * @param LName Layer name
     * @param fieldName Field name
     * @return Vector layer
     */
    public static VectorLayer createGridFillLayer(GridData gridData, LegendScheme aLS, String LName, String fieldName) {
        //generate grid points
        int i, j;
        PointD aPoint;

        List<PointD> PList;
        VectorLayer aLayer = new VectorLayer(ShapeTypes.Polygon);
        Field aDC = new Field(fieldName, DataTypes.Double);
        aLayer.editAddField(aDC);

        double XDelt = gridData.xArray[1] - gridData.xArray[0];
        double YDelt = gridData.yArray[1] - gridData.yArray[0];
        for (i = 0; i < gridData.getYNum(); i++) {
            for (j = 0; j < gridData.getXNum(); j++) {
                PList = new ArrayList<PointD>();
                aPoint = new PointD();
                aPoint.X = gridData.xArray[j] - XDelt / 2;
                aPoint.Y = gridData.yArray[i] - YDelt / 2;
                PList.add(aPoint);
                aPoint = new PointD();
                aPoint.X = gridData.xArray[j] - XDelt / 2;
                aPoint.Y = gridData.yArray[i] + YDelt / 2;
                PList.add(aPoint);
                aPoint = new PointD();
                aPoint.X = gridData.xArray[j] + XDelt / 2;
                aPoint.Y = gridData.yArray[i] + YDelt / 2;
                PList.add(aPoint);
                aPoint = new PointD();
                aPoint.X = gridData.xArray[j] + XDelt / 2;
                aPoint.Y = gridData.yArray[i] - YDelt / 2;
                PList.add(aPoint);
                PList.add(PList.get(0));

                PolygonShape aPGS = new PolygonShape();
                aPGS.lowValue = gridData.data[i][j];
                aPGS.highValue = aPGS.lowValue;
                aPGS.setPoints(PList);

                int shapeNum = aLayer.getShapeNum();
                try {
                    if (aLayer.editInsertShape(aPGS, shapeNum)) {
                        aLayer.editCellValue(fieldName, shapeNum, gridData.data[i][j]);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        aLayer.setLayerName(LName);
        aLS.setFieldName(fieldName);
        aLayer.setLegendScheme((LegendScheme) aLS.clone());
        aLayer.setLayerDrawType(LayerDrawType.GridFill);

        return aLayer;
    }

    /**
     * Create grid point layer
     *
     * @param gridData Grid data
     * @param aLS Legend scheme
     * @param LName Layer name
     * @param fieldName Field name
     * @return Vector layer
     */
    public static VectorLayer createGridPointLayer(GridData gridData, LegendScheme aLS, String LName, String fieldName) {
        //generate grid points
        int i, j;
        PointD aPoint;

        VectorLayer aLayer = new VectorLayer(ShapeTypes.Point);
        Field aDC = new Field(fieldName, DataTypes.Double);
        aLayer.editAddField(aDC);

        for (i = 0; i < gridData.getYNum(); i++) {
            for (j = 0; j < gridData.getXNum(); j++) {
                aPoint = new PointD();
                aPoint.X = gridData.xArray[j];
                aPoint.Y = gridData.yArray[i];
                PointShape aPointShape = new PointShape();
                aPointShape.setPoint(aPoint);
                aPointShape.setValue(gridData.data[i][j]);

                int shapeNum = aLayer.getShapeNum();
                try {
                    if (aLayer.editInsertShape(aPointShape, shapeNum)) {
                        aLayer.editCellValue(fieldName, shapeNum, gridData.data[i][j]);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        aLayer.setLayerName(LName);
        aLS.setFieldName(fieldName);
        aLayer.setLegendScheme((LegendScheme) aLS.clone());
        aLayer.setLayerDrawType(LayerDrawType.GridPoint);

        return aLayer;
    }

    /**
     * Create grid wind vector layer from U/V or direction/speed grid data
     *
     * @param uData U or wind direction grid data
     * @param vData V or wind speed grid data
     * @param gridData The grid data
     * @param aLS Legend scheme
     * @param ifColor If draw color wind
     * @param lName Layer name
     * @param isUV if is U/V
     * @return Vector layer
     */
    public static VectorLayer createGridVectorLayer(GridData uData, GridData vData, GridData gridData,
            LegendScheme aLS, boolean ifColor, String lName, boolean isUV) {
        GridData windDirData;
        GridData windSpeedData;
        if (isUV) {
            GridData[] uv = DataMath.getDSFromUV(uData, vData);
            windDirData = uv[0];
            windSpeedData = uv[1];
        } else {
            windDirData = uData;
            windSpeedData = vData;
        }

        int i, j;
        double windDir, windSpeed;
        float size = 6;
        PointD aPoint;
        int XNum = uData.xArray.length;
        int YNum = uData.yArray.length;

        String columnName = lName.split("_")[0];
        VectorLayer aLayer = new VectorLayer(ShapeTypes.WindArraw);
        //Add data column   
        if (isUV) {
            aLayer.editAddField("U", DataTypes.Float);
            aLayer.editAddField("V", DataTypes.Float);
        }
        aLayer.editAddField("WindDirection", DataTypes.Float);
        aLayer.editAddField("WindSpeed", DataTypes.Float);
        boolean ifAdd = true;
        if (aLayer.getFieldNames().contains(columnName)) {
            ifAdd = false;
        }
        if (ifColor && ifAdd) {
            aLayer.editAddField(columnName, DataTypes.Float);
        }

        for (i = 0; i < YNum; i++) {
            for (j = 0; j < XNum; j++) {
                windDir = windDirData.data[i][j];
                windSpeed = windSpeedData.data[i][j];
                if (!MIMath.doubleEquals(windDir, windDirData.missingValue)) {
                    if (!MIMath.doubleEquals(windSpeed, windSpeedData.missingValue)) {
                        aPoint = new PointD();
                        aPoint.X = uData.xArray[j];
                        aPoint.Y = uData.yArray[i];
                        WindArraw aArraw = new WindArraw();
                        aArraw.angle = windDir;
                        aArraw.length = (float) windSpeed;
                        aArraw.size = size;
                        aArraw.setPoint(aPoint);

                        if (ifColor) {
                            aArraw.setValue(gridData.data[i][j]);
                        }

                        int shapeNum = aLayer.getShapeNum();
                        try {
                            if (aLayer.editInsertShape(aArraw, shapeNum)) {
                                if (isUV) {
                                    aLayer.editCellValue("U", shapeNum, uData.data[i][j]);
                                    aLayer.editCellValue("V", shapeNum, vData.data[i][j]);
                                }
                                aLayer.editCellValue("WindDirection", shapeNum, aArraw.angle);
                                aLayer.editCellValue("WindSpeed", shapeNum, aArraw.length);
                                if (ifColor && ifAdd) {
                                    aLayer.editCellValue(columnName, shapeNum, gridData.data[i][j]);
                                }
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }

            }
        }

        aLayer.setLayerName(lName);
        if (ifColor && ifAdd) {
            aLS.setFieldName(columnName);
        } else {
            aLS.setFieldName("WindSpeed");
        }
        aLayer.setLegendScheme((LegendScheme) aLS.clone());
        aLayer.setLayerDrawType(LayerDrawType.Vector);

        return aLayer;
    }

    /**
     * Create grid barb layer from U/V or wind direction/speed grid data
     *
     * @param uData U grid data
     * @param vData V grid data
     * @param gridData Grid data
     * @param aLS Legend scheme
     * @param ifColor If is color
     * @param lName Layer name
     * @param isUV If is U/V
     * @return Vector layer
     */
    public static VectorLayer createGridBarbLayer(GridData uData, GridData vData, GridData gridData,
            LegendScheme aLS, boolean ifColor, String lName, boolean isUV) {
        GridData windDirData;
        GridData windSpeedData;
        if (isUV) {
            GridData[] wwData = DataMath.getDSFromUV(uData, vData);
            windDirData = wwData[0];
            windSpeedData = wwData[1];
        } else {
            windDirData = uData;
            windSpeedData = vData;
        }

        int i, j;
        WindBarb aWB;
        double windDir, windSpeed;
        PointD aPoint;
        int XNum = windDirData.xArray.length;
        int YNum = windDirData.yArray.length;
        String columnName = lName.split("_")[0];

        VectorLayer aLayer = new VectorLayer(ShapeTypes.WindBarb);
        //Add data column  
        if (isUV) {
            aLayer.editAddField("U", DataTypes.Float);
            aLayer.editAddField("V", DataTypes.Float);
        }
        aLayer.editAddField("WindDirection", DataTypes.Float);
        aLayer.editAddField("WindSpeed", DataTypes.Float);
        boolean ifAdd = true;
        if (aLayer.getFieldNames().contains(columnName)) {
            ifAdd = false;
        }
        if (ifColor && ifAdd) {
            aLayer.editAddField(columnName, DataTypes.Float);
        }

        for (i = 0; i < YNum; i++) {
            for (j = 0; j < XNum; j++) {
                windDir = windDirData.data[i][j];
                windSpeed = windSpeedData.data[i][j];
                if (!MIMath.doubleEquals(windDir, windDirData.missingValue)) {
                    if (!MIMath.doubleEquals(windSpeed, windSpeedData.missingValue)) {
                        aPoint = new PointD();
                        aPoint.X = windDirData.xArray[j];
                        aPoint.Y = windDirData.yArray[i];
                        aWB = Draw.calWindBarb((float) windDir, (float) windSpeed, 0, 10, aPoint);
                        if (ifColor) {
                            aWB.setValue(gridData.data[i][j]);
                        }

                        int shapeNum = aLayer.getShapeNum();
                        try {
                            if (aLayer.editInsertShape(aWB, shapeNum)) {
                                if (isUV) {
                                    aLayer.editCellValue("U", shapeNum, uData.data[i][j]);
                                    aLayer.editCellValue("V", shapeNum, vData.data[i][j]);
                                }
                                aLayer.editCellValue("WindDirection", shapeNum, aWB.angle);
                                aLayer.editCellValue("WindSpeed", shapeNum, aWB.windSpeed);
                                if (ifColor && ifAdd) {
                                    aLayer.editCellValue(columnName, shapeNum, gridData.data[i][j]);
                                }
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }

            }
        }

        aLayer.setLayerName(lName);
        if (ifColor && ifAdd) {
            aLS.setFieldName(columnName);
        } else {
            aLS.setFieldName("WindSpeed");
        }
        aLayer.setLegendScheme((LegendScheme) aLS.clone());
        aLayer.setLayerDrawType(LayerDrawType.Barb);

        return aLayer;
    }

    /**
     * Create streamline layer by U/V or wind direction/speed grid data
     *
     * @param uData U grid data
     * @param vData V grid data
     * @param density Density
     * @param aLS Legend scheme
     * @param lName Layer name
     * @param isUV If is U/V
     * @return Vector layer
     */
    public static VectorLayer createStreamlineLayer(GridData uData, GridData vData, int density, LegendScheme aLS,
            String lName, boolean isUV) {
        GridData uGridData;
        GridData vGridData;
        if (isUV) {
            uGridData = uData;
            vGridData = vData;
        } else {
            GridData[] uvData = DataMath.getUVFromDS(uData, vData);
            uGridData = uvData[0];
            vGridData = uvData[1];
        }

        List<wContour.Global.PolyLine> streamlines = wContour.Contour.tracingStreamline(uGridData.data, vGridData.data,
                uGridData.xArray, vGridData.yArray, uGridData.missingValue, density);

        wContour.Global.PolyLine aLine;
        VectorLayer aLayer = new VectorLayer(ShapeTypes.Polyline);
        aLayer.editAddField("ID", DataTypes.Integer);

        for (int i = 0; i < streamlines.size() - 1; i++) {
            aLine = streamlines.get(i);

            PolylineShape aPolyline = new PolylineShape();
            PointD aPoint;
            List<PointD> pList = new ArrayList<PointD>();
            for (int j = 0; j < aLine.PointList.size(); j++) {
                aPoint = new PointD();
                aPoint.X = (aLine.PointList.get(j)).X;
                aPoint.Y = (aLine.PointList.get(j)).Y;
                pList.add(aPoint);
            }
            aPolyline.setPoints(pList);
            aPolyline.value = density;

            int shapeNum = aLayer.getShapeNum();
            try {
                if (aLayer.editInsertShape(aPolyline, shapeNum)) {
                    aLayer.editCellValue("ID", shapeNum, shapeNum + 1);
                }
            } catch (Exception ex) {
                Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        aLayer.setLayerName(lName);
        aLS.setFieldName("ID");
        aLayer.setLegendScheme(aLS);
        aLayer.setLayerDrawType(LayerDrawType.Streamline);

        return aLayer;
    }

    /**
     * Create reaster layer
     *
     * @param GridData Grid data
     * @param LName Layer name
     * @param aLS Legend scheme
     * @return Raster layer
     */
    public static RasterLayer createRasterLayer(GridData gridData, String LName, LegendScheme aLS) {
        RasterLayer aRLayer = new RasterLayer();
        aRLayer.setGridData(gridData);
        aRLayer.setLegendScheme(aLS);
        aRLayer.setLayerName(LName);
        aRLayer.setVisible(true);
        aRLayer.setLayerDrawType(LayerDrawType.Raster);
        aRLayer.setMaskout(true);

        return aRLayer;
    }

    /**
     * Create raster layer
     *
     * @param GridData Grid data
     * @param LName Layer name
     * @param paletteFile Palette file name
     * @return Raster layer
     */
    public static RasterLayer createRasterLayer(GridData gridData, String LName, String paletteFile) {
        RasterLayer aRLayer = new RasterLayer();
        aRLayer.setGridData(gridData);
        aRLayer.setPalette(paletteFile);
        aRLayer.setLayerName(LName);
        aRLayer.setVisible(true);
        aRLayer.setLayerDrawType(LayerDrawType.Raster);
        aRLayer.setMaskout(true);

        return aRLayer;
    }

    /**
     * Create station point layer
     *
     * @param stationData Station data
     * @param aLS Legend scheme
     * @param LName Layer name
     * @param fieldName Field name
     * @return Vector layer
     */
    public static VectorLayer createSTPointLayer(StationData stationData, LegendScheme aLS, String LName, String fieldName) {
        int i;
        PointD aPoint;

        VectorLayer aLayer = new VectorLayer(ShapeTypes.Point);
        aLayer.editAddField("Stid", DataTypes.String);
        aLayer.editAddField(fieldName, DataTypes.Double);

        for (i = 0; i < stationData.data.length; i++) {
            aPoint = new PointD();
            aPoint.X = stationData.data[i][0];
            aPoint.Y = stationData.data[i][1];
            PointShape aPointShape = new PointShape();
            aPointShape.setPoint(aPoint);
            aPointShape.setValue(stationData.data[i][2]);

            int shapeNum = aLayer.getShapeNum();
            try {
                if (aLayer.editInsertShape(aPointShape, shapeNum)) {
                    aLayer.editCellValue("Stid", shapeNum, stationData.stations.get(i));
                    aLayer.editCellValue(fieldName, shapeNum, stationData.data[i][2]);
                }
            } catch (Exception ex) {
                Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        aLayer.setLayerName(LName);
        aLS.setFieldName(fieldName);
        aLayer.setLegendScheme((LegendScheme) aLS.clone());
        aLayer.setAvoidCollision(true);
        aLayer.setLayerDrawType(LayerDrawType.StationPoint);

        return aLayer;
    }

    /**
     * Create station info layer
     *
     * @param stInfoData Station info data
     * @param aLS Legend scheme
     * @param layerName Layer name
     * @return Station info layer
     */
    public static VectorLayer createSTInfoLayer(StationInfoData stInfoData, LegendScheme aLS, String layerName) {
        int i, j;
        PointD aPoint;
        VectorLayer aLayer = new VectorLayer(ShapeTypes.Point);
        for (i = 0; i < stInfoData.getFields().size(); i++) {
            String fieldName = stInfoData.getFields().get(i);
            DataTypes dtype = DataTypes.String;
            if (stInfoData.getVariables().contains(stInfoData.getFields().get(i))) {
                dtype = DataTypes.Double;
            }
            aLayer.editAddField(fieldName, dtype);
        }

        for (i = 0; i < stInfoData.getDataList().size(); i++) {
            List<String> dataList = stInfoData.getDataList().get(i);
            aPoint = new PointD();
            aPoint.X = Double.parseDouble(dataList.get(1));
            aPoint.Y = Double.parseDouble(dataList.get(2));
            PointShape aPointShape = new PointShape();
            aPointShape.setPoint(aPoint);

            int shapeNum = aLayer.getShapeNum();
            try {
                if (aLayer.editInsertShape(aPointShape, shapeNum)) {
                    for (j = 0; j < stInfoData.getFields().size(); j++) {
                        if (stInfoData.getVariables().contains(stInfoData.getFields().get(j))) {
                            aLayer.editCellValue(stInfoData.getFields().get(j), shapeNum, Double.parseDouble(dataList.get(j)));
                        } else {
                            aLayer.editCellValue(stInfoData.getFields().get(j), shapeNum, dataList.get(j));
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        aLayer.setLayerName(layerName);
        aLayer.setLegendScheme(aLS);
        aLayer.setAvoidCollision(true);
        aLayer.setLayerDrawType(LayerDrawType.StationPoint);

        return aLayer;
    }

    /**
     * Create station vector layer
     *
     * @param uData U station data
     * @param vData V station data
     * @param stData Station data
     * @param aLS Legend scheme
     * @param layerName Layer name
     * @param isUV If is U/V
     * @return Station vector layer
     */
    public static VectorLayer createSTVectorLayer(StationData uData, StationData vData, StationData stData,
            LegendScheme aLS, String layerName, boolean isUV) {
        StationData windDirData;
        StationData windSpeedData;
        if (isUV) {
            StationData[] dsData = DataMath.getDSFromUV(uData, vData);
            windDirData = dsData[0];
            windSpeedData = dsData[1];
        } else {
            windDirData = uData;
            windSpeedData = vData;
        }

        int i;
        float windDir, windSpeed;
        PointD aPoint;

        String columnName = layerName.split("_")[0];
        VectorLayer aLayer = new VectorLayer(ShapeTypes.WindArraw);
        //Add data column         
        if (isUV) {
            aLayer.editAddField("U", DataTypes.Float);
            aLayer.editAddField("V", DataTypes.Float);
        }
        aLayer.editAddField("WindDirection", DataTypes.Float);
        aLayer.editAddField("WindSpeed", DataTypes.Float);
        boolean ifAdd = true;
        if (aLayer.getFieldNames().contains(columnName)) {
            ifAdd = false;
        }
        if (ifAdd) {
            aLayer.editAddField(columnName, DataTypes.Float);
        }

        for (i = 0; i < windDirData.getStNum(); i++) {
            windDir = (float) windDirData.data[i][2];
            windSpeed = (float) windSpeedData.data[i][2];
            if (!MIMath.doubleEquals(windDir, windDirData.missingValue)) {
                if (!MIMath.doubleEquals(windSpeed, windSpeedData.missingValue)) {
                    aPoint = new PointD();
                    aPoint.X = windDirData.data[i][0];
                    aPoint.Y = windDirData.data[i][1];

                    WindArraw aArraw = new WindArraw();
                    aArraw.angle = windDir;
                    aArraw.length = windSpeed;
                    aArraw.size = 6;
                    aArraw.setPoint(aPoint);
                    aArraw.setValue(stData.data[i][2]);

                    int shapeNum = aLayer.getShapeNum();
                    try {
                        if (aLayer.editInsertShape(aArraw, shapeNum)) {
                            if (isUV) {
                                aLayer.editCellValue("U", shapeNum, uData.data[i][2]);
                                aLayer.editCellValue("V", shapeNum, vData.data[i][2]);
                            }
                            aLayer.editCellValue("WindDirection", shapeNum, windDir);
                            aLayer.editCellValue("WindSpeed", shapeNum, windSpeed);
                            if (ifAdd) {
                                aLayer.editCellValue(columnName, shapeNum, stData.data[i][2]);
                            }
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        aLayer.setLayerName(layerName);
        aLS.setFieldName(columnName);
        aLayer.setLegendScheme(aLS);
        aLayer.setLayerDrawType(LayerDrawType.Vector);

        return aLayer;
    }

    /**
     * Create station vector layer
     *
     * @param uData U station data
     * @param vData V station data
     * @param aLS Legend scheme
     * @param layerName Layer name
     * @param isUV If is U/V
     * @return Vector layer
     */
    public static VectorLayer createSTVectorLayer(StationData uData, StationData vData,
            LegendScheme aLS, String layerName, boolean isUV) {
        StationData windDirData;
        StationData windSpeedData;
        if (isUV) {
            StationData[] dsData = DataMath.getDSFromUV(uData, vData);
            windDirData = dsData[0];
            windSpeedData = dsData[1];
        } else {
            windDirData = uData;
            windSpeedData = vData;
        }

        int i;
        float windDir, windSpeed;
        PointD aPoint;

        String columnName = layerName.split("_")[0];
        VectorLayer aLayer = new VectorLayer(ShapeTypes.WindArraw);
        //Add data column         
        if (isUV) {
            aLayer.editAddField("U", DataTypes.Float);
            aLayer.editAddField("V", DataTypes.Float);
        }
        aLayer.editAddField("WindDirection", DataTypes.Float);
        aLayer.editAddField("WindSpeed", DataTypes.Float);

        for (i = 0; i < windDirData.getStNum(); i++) {
            windDir = (float) windDirData.data[i][2];
            windSpeed = (float) windSpeedData.data[i][2];
            if (!MIMath.doubleEquals(windDir, windDirData.missingValue)) {
                if (!MIMath.doubleEquals(windSpeed, windSpeedData.missingValue)) {
                    aPoint = new PointD();
                    aPoint.X = windDirData.data[i][0];
                    aPoint.Y = windDirData.data[i][1];

                    WindArraw aArraw = new WindArraw();
                    aArraw.angle = windDir;
                    aArraw.length = windSpeed;
                    aArraw.size = 6;
                    aArraw.setPoint(aPoint);

                    int shapeNum = aLayer.getShapeNum();
                    try {
                        if (aLayer.editInsertShape(aArraw, shapeNum)) {
                            if (isUV) {
                                aLayer.editCellValue("U", shapeNum, uData.data[i][2]);
                                aLayer.editCellValue("V", shapeNum, vData.data[i][2]);
                            }
                            aLayer.editCellValue("WindDirection", shapeNum, windDir);
                            aLayer.editCellValue("WindSpeed", shapeNum, windSpeed);
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        aLayer.setLayerName(layerName);
        aLS.setFieldName(columnName);
        aLayer.setLegendScheme(aLS);
        aLayer.setLayerDrawType(LayerDrawType.Vector);

        return aLayer;
    }

    /**
     * Create station barb layer from U/V or direction/speed station data
     *
     * @param uData U station data
     * @param vData V station data
     * @param stData Station data
     * @param aLS Legend scheme
     * @param layerName Layer name
     * @param isUV If is U/V
     * @return Station barb layer
     */
    public static VectorLayer createSTBarbLayer(StationData uData, StationData vData, StationData stData,
            LegendScheme aLS, String layerName, boolean isUV) {
        StationData windDirData;
        StationData windSpeedData;
        if (isUV) {
            StationData[] dsData = DataMath.getDSFromUV(uData, vData);
            windDirData = dsData[0];
            windSpeedData = dsData[1];
        } else {
            windDirData = uData;
            windSpeedData = vData;
        }

        int i;
        float windDir, windSpeed;
        PointD aPoint;

        String columnName = layerName.split("_")[0];
        VectorLayer aLayer = new VectorLayer(ShapeTypes.WindBarb);
        //Add data column        
        if (isUV) {
            aLayer.editAddField("U", DataTypes.Float);
            aLayer.editAddField("V", DataTypes.Float);
        }
        aLayer.editAddField("WindDirection", DataTypes.Float);
        aLayer.editAddField("WindSpeed", DataTypes.Float);
        boolean ifAdd = true;
        if (aLayer.getFieldNames().contains(columnName)) {
            ifAdd = false;
        }
        if (ifAdd) {
            aLayer.editAddField(columnName, DataTypes.Float);
        }

        for (i = 0; i < windDirData.getStNum(); i++) {
            windDir = (float) windDirData.data[i][2];
            windSpeed = (float) windSpeedData.data[i][2];
            if (windSpeed == 0) {
                continue;
            }

            if (!MIMath.doubleEquals(windDir, windDirData.missingValue)) {
                if (!MIMath.doubleEquals(windSpeed, windSpeedData.missingValue)) {
                    aPoint = new PointD();
                    aPoint.X = windDirData.data[i][0];
                    aPoint.Y = windDirData.data[i][1];
                    WindBarb aWB = Draw.calWindBarb(windDir, windSpeed, 0, 10, aPoint);
                    aWB.setValue(stData.data[i][2]);

                    int shapeNum = aLayer.getShapeNum();
                    try {
                        if (aLayer.editInsertShape(aWB, shapeNum)) {
                            if (isUV) {
                                aLayer.editCellValue("U", shapeNum, uData.data[i][2]);
                                aLayer.editCellValue("V", shapeNum, vData.data[i][2]);
                            }
                            aLayer.editCellValue("WindDirection", shapeNum, windDir);
                            aLayer.editCellValue("WindSpeed", shapeNum, windSpeed);
                            if (ifAdd) {
                                aLayer.editCellValue(columnName, shapeNum, stData.data[i][2]);
                            }
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        aLayer.setLayerName(layerName);
        aLS.setFieldName(columnName);
        aLayer.setLegendScheme(aLS);
        aLayer.setLayerDrawType(LayerDrawType.Barb);

        return aLayer;
    }

    /**
     * Create station barb layer from U/V or direction/speed station data
     *
     * @param uData U station data
     * @param vData V station data
     * @param aLS Legend scheme
     * @param layerName Layer name
     * @param isUV If is U/V
     * @return Station barb layer
     */
    public static VectorLayer createSTBarbLayer(StationData uData, StationData vData,
            LegendScheme aLS, String layerName, boolean isUV) {
        StationData windDirData;
        StationData windSpeedData;
        if (isUV) {
            StationData[] dsData = DataMath.getDSFromUV(uData, vData);
            windDirData = dsData[0];
            windSpeedData = dsData[1];
        } else {
            windDirData = uData;
            windSpeedData = vData;
        }

        int i;
        float windDir, windSpeed;
        PointD aPoint;

        String columnName = layerName.split("_")[0];
        VectorLayer aLayer = new VectorLayer(ShapeTypes.WindBarb);
        //Add data column        
        if (isUV) {
            aLayer.editAddField("U", DataTypes.Float);
            aLayer.editAddField("V", DataTypes.Float);
        }
        aLayer.editAddField("WindDirection", DataTypes.Float);
        aLayer.editAddField("WindSpeed", DataTypes.Float);

        for (i = 0; i < windDirData.getStNum(); i++) {
            windDir = (float) windDirData.data[i][2];
            windSpeed = (float) windSpeedData.data[i][2];
            if (windSpeed == 0) {
                continue;
            }

            if (!MIMath.doubleEquals(windDir, windDirData.missingValue)) {
                if (!MIMath.doubleEquals(windSpeed, windSpeedData.missingValue)) {
                    aPoint = new PointD();
                    aPoint.X = windDirData.data[i][0];
                    aPoint.Y = windDirData.data[i][1];
                    WindBarb aWB = Draw.calWindBarb(windDir, windSpeed, 0, 10, aPoint);

                    int shapeNum = aLayer.getShapeNum();
                    try {
                        if (aLayer.editInsertShape(aWB, shapeNum)) {
                            if (isUV) {
                                aLayer.editCellValue("U", shapeNum, uData.data[i][2]);
                                aLayer.editCellValue("V", shapeNum, vData.data[i][2]);
                            }
                            aLayer.editCellValue("WindDirection", shapeNum, windDir);
                            aLayer.editCellValue("WindSpeed", shapeNum, windSpeed);
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        aLayer.setLayerName(layerName);
        aLS.setFieldName(columnName);
        aLayer.setLegendScheme(aLS);
        aLayer.setLayerDrawType(LayerDrawType.Barb);

        return aLayer;
    }

    /**
     * Create station model layer
     *
     * @param stationModelData Station model data
     * @param aLS Legend scheme
     * @param layerName Layer name
     * @param isSurface If is surface
     * @return Station model layer
     */
    public static VectorLayer createStationModelLayer(StationModelData stationModelData,
            LegendScheme aLS, String layerName, boolean isSurface) {
        int i;
        StationModelShape aSM;
        float windDir, windSpeed;
        int weather, cCover, temp, dewPoint, pressure;
        PointD aPoint;

        VectorLayer aLayer = new VectorLayer(ShapeTypes.StationModel);
        aLayer.editAddField(new Field("WindDirection", DataTypes.Float));
        aLayer.editAddField(new Field("WindSpeed", DataTypes.Float));
        aLayer.editAddField(new Field("Weather", DataTypes.Integer));
        aLayer.editAddField(new Field("Temperature", DataTypes.Integer));
        aLayer.editAddField(new Field("DewPoint", DataTypes.Integer));
        aLayer.editAddField(new Field("Pressure", DataTypes.Integer));
        aLayer.editAddField(new Field("CloudCoverage", DataTypes.Integer));

        for (i = 0; i < stationModelData.getDataNum(); i++) {
            StationModel sm = stationModelData.getData().get(i);
            windDir = (float) sm.getWindDirection();
            windSpeed = (float) sm.getWindSpeed();
            if (!(MIMath.doubleEquals(windDir, stationModelData.getMissingValue()))) {
                if (!(MIMath.doubleEquals(windSpeed, stationModelData.getMissingValue()))) {
                    aPoint = new PointD();
                    aPoint.X = (float) sm.getLongitude();
                    aPoint.Y = (float) sm.getLatitude();
                    weather = (int) sm.getWeather();
                    cCover = (int) sm.getCloudCover();
                    temp = (int) sm.getTemperature();
                    dewPoint = (int) sm.getDewPoint();
                    pressure = (int) sm.getPressure();
                    if (isSurface) {
                        if (!(MIMath.doubleEquals(sm.getPressure(), stationModelData.getMissingValue()))) {
                            //pressure = (int)((stationModelData[9, i] - 1000) * 10);
                            String pStr = String.valueOf((int) (sm.getPressure() * 10));
                            if (pStr.length() < 3) {
                                pressure = (int) stationModelData.getMissingValue();
                            } else {
                                pStr = pStr.substring(pStr.length() - 3);
                                pressure = Integer.parseInt(pStr);
                            }
                        }
                    }

                    aSM = Draw.calStationModel(windDir, windSpeed, 0, 12, aPoint, weather,
                            temp, dewPoint, pressure, cCover);

                    int shapeNum = aLayer.getShapeNum();
                    try {
                        if (aLayer.editInsertShape(aSM, shapeNum)) {
                            aLayer.editCellValue("WindDirection", shapeNum, windDir);
                            aLayer.editCellValue("WindSpeed", shapeNum, windSpeed);
                            aLayer.editCellValue("Weather", shapeNum, weather);
                            aLayer.editCellValue("Temperature", shapeNum, temp);
                            aLayer.editCellValue("DewPoint", shapeNum, dewPoint);
                            aLayer.editCellValue("Pressure", shapeNum, pressure);
                            aLayer.editCellValue("CloudCoverage", shapeNum, cCover);
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        aLayer.setLayerName(layerName);
        aLS.setFieldName("");
        aLayer.setLegendScheme(aLS);
        aLayer.setAvoidCollision(true);
        aLayer.setLayerDrawType(LayerDrawType.StationModel);

        return aLayer;
    }

    /**
     * Create station weather symbol layer
     *
     * @param weatherData Weather station data
     * @param WeatherType Weatehr type
     * @param layerName Layer name
     * @return Weather symbol layer
     */
    public static VectorLayer createWeatherSymbolLayer(StationData weatherData, String WeatherType,
            String layerName) {
        List<Integer> wList = getWeatherTypes(WeatherType);

        return createWeatherSymbolLayer(weatherData, wList, layerName);
    }

    /**
     * Create weather symbol station layer
     *
     * @param weatherData Weather station data
     * @param wList Weather list
     * @param layerName Layer name
     * @return
     */
    public static VectorLayer createWeatherSymbolLayer(StationData weatherData, List<Integer> wList,
            String layerName) {
        LegendScheme aLS = createWeatherLegendScheme(wList);
        return createWeatherSymbolLayer(weatherData, wList, aLS, layerName);
    }

    /**
     * Create weather symbol station layer
     *
     * @param weatherData Weather station data
     * @param wList Weather list
     * @param aLS Legend scheme
     * @param layerName Layer name
     * @return Weather symbol layer
     */
    public static VectorLayer createWeatherSymbolLayer(StationData weatherData, List<Integer> wList,
            LegendScheme aLS, String layerName) {
        int i;
        int weather;
        PointD aPoint;

        String columnName = "Weather";
        VectorLayer aLayer = new VectorLayer(ShapeTypes.Point);
        aLayer.editAddField(columnName, DataTypes.Double);

        for (i = 0; i < weatherData.getStNum(); i++) {
            weather = (int) weatherData.data[i][2];
            if (!(MIMath.doubleEquals(weather, weatherData.missingValue))) {
                if (wList.contains(weather)) {
                    aPoint = new PointD();
                    aPoint.X = weatherData.data[i][0];
                    aPoint.Y = weatherData.data[i][1];
                    PointShape aPS = new PointShape();
                    aPS.setPoint(aPoint);
                    aPS.setValue(weather);

                    int shapeNum = aLayer.getShapeNum();
                    try {
                        if (aLayer.editInsertShape(aPS, shapeNum)) {
                            aLayer.editCellValue(columnName, shapeNum, weather);
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(DrawMeteoData.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }


        aLayer.setLayerName(layerName);
        aLS.setFieldName(columnName);
        aLayer.setLegendScheme(aLS);
        aLayer.setLayerDrawType(LayerDrawType.StationPoint);

        return aLayer;
    }

    private static LegendScheme createWeatherLegendScheme(List<Integer> wList) {
        LegendScheme aLS = new LegendScheme(ShapeTypes.Point);
        aLS.setLegendType(LegendType.UniqueValue);
        for (int w : wList) {
            PointBreak aPB = new PointBreak();
            aPB.setMarkerType(MarkerType.Character);
            aPB.setSize(20);
            aPB.setColor(Color.blue);
            aPB.setFontName("Weather");
            aPB.setStartValue(w);
            aPB.setEndValue(w);
            int charIdx = w + 28;
            if (w == 99) {
                charIdx = w + 97;
            }
            aPB.setCharIndex(charIdx);
            aPB.setCaption(String.valueOf(w));

            aLS.getLegendBreaks().add(aPB);
        }

        return aLS;
    }

    /**
     * Get weather list
     *
     * @param weatherType Weather type
     * @return Weather list
     */
    public static List<Integer> getWeatherTypes(String weatherType) {
        List<Integer> weatherList = new ArrayList<Integer>();
        int i;
        int[] weathers = new int[1];
        if (weatherType.equals("All Weather")) {
            weathers = new int[96];
            for (i = 4; i < 100; i++) {
                weathers[i - 4] = i;
            }
        } else if (weatherType.equals("SDS")) {
            weathers = new int[]{6, 7, 8, 9, 30, 31, 32, 33, 34, 35};
        } else if (weatherType.equals("SDS, Haze")) {
            weathers = new int[]{5, 6, 7, 8, 9, 30, 31, 32, 33, 34, 35};
        } else if (weatherType.equals("Smoke, Haze, Mist")) {
            weathers = new int[]{4, 5, 10};
        } else if (weatherType.equals("Smoke")) {
            weathers = new int[]{4};
        } else if (weatherType.equals("Haze")) {
            weathers = new int[]{5};
        } else if (weatherType.equals("Mist")) {
            weathers = new int[]{10};
        } else if (weatherType.equals("Fog")) {
            weathers = new int[10];
            for (i = 40; i < 50; i++) {
                weathers[i - 40] = i;
            }
        }


        for (int w : weathers) {
            weatherList.add(w);
        }

        return weatherList;
    }
    // </editor-fold>
}