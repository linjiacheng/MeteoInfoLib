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
package org.meteoinfo.data.mapdata;

import org.meteoinfo.data.GridData;
import org.meteoinfo.data.mapdata.geotiff.GeoTiff;
import org.meteoinfo.data.meteodata.DrawMeteoData;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.GlobalUtil;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import org.meteoinfo.global.table.DataTypes;
import org.meteoinfo.layer.ImageLayer;
import org.meteoinfo.layer.LayerDrawType;
import org.meteoinfo.layer.MapLayer;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.layer.WorldFilePara;
import org.meteoinfo.legend.LegendManage;
import org.meteoinfo.shape.PolylineShape;
import org.meteoinfo.shape.ShapeTypes;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import org.meteoinfo.data.meteodata.bandraster.BILDataInfo;
import org.meteoinfo.global.table.DataColumn;
import org.meteoinfo.layer.RasterLayer;
import org.meteoinfo.legend.LegendScheme;
import org.meteoinfo.legend.LegendType;
import org.meteoinfo.projection.ProjectionInfo;
import org.meteoinfo.shape.PointShape;
import org.meteoinfo.shape.PolygonShape;
import org.meteoinfo.shape.Shape;
import static org.meteoinfo.shape.ShapeTypes.Point;
import static org.meteoinfo.shape.ShapeTypes.Polygon;
import static org.meteoinfo.shape.ShapeTypes.Polyline;

/**
 *
 * @author yaqiang
 */
public class MapDataManage {

    /**
     * Load a layer from a file
     *
     * @param aFile The file path
     * @return The layer
     */
    public static MapLayer loadLayer(String aFile) throws IOException, FileNotFoundException, Exception {
        MapLayer aLayer = null;
        //String a = "aa";
        if (new File(aFile).isFile()) {
            String ext = GlobalUtil.getFileExtension(aFile);
            if (ext.equals("dat")) {
                //aLayer = MapDataManage.ReadMapFile_MICAPS(aFile);
            } else if (ext.equals("shp")) {
                aLayer = readMapFile_ShapeFile(aFile);
            } else if (ext.equals("wmp")) {
                aLayer = readMapFile_WMP(aFile);
            } else if (ext.equals("bln")) {
                //aLayer = MapDataManage.ReadMapFile_BLN(aFile);
            } else if (ext.equals("bmp") || ext.equals("gif") || ext.equals("jpg") || ext.equals("png")) {
                aLayer = readImageFile(aFile);
            } else if (ext.equals("tif")) {
                aLayer = readGeoTiffFile(aFile);
            } else if (ext.equals("bil")) {
                aLayer = readBILFile(aFile);
            } else {
                aLayer = readMapFile_GrADS(aFile);
            }
        }

        return aLayer;
    }

    /**
     * Load a layer from a file with a certain projection
     *
     * @param aFile The file name
     * @param projInfo The projection
     * @return The layer
     * @throws IOException
     * @throws FileNotFoundException
     * @throws Exception
     */
    public static MapLayer loadLayer(String aFile, ProjectionInfo projInfo) throws IOException, FileNotFoundException, Exception {
        MapLayer layer = loadLayer(aFile);
        layer.setProjInfo(projInfo);

        return layer;
    }

    /**
     * Read shape file as map
     *
     * @param aFile File name
     * @return Vector layer
     */
    public static VectorLayer readMapFile_ShapeFile(String aFile) throws IOException, FileNotFoundException, Exception {
        VectorLayer aLayer = ShapeFileManage.loadShapeFile(aFile);

        return aLayer;
    }

    /**
     * Read GrADS map file
     *
     * @param aFile The file path
     * @return The layer
     */
    public static VectorLayer readMapFile_GrADS(String aFile) throws FileNotFoundException, IOException, Exception {
        DataInputStream br = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(aFile))));
        int i, lineNum;
        byte b;
        short N, lType;
        double lon, lat;
        byte[] bytes;

        PointD aPoint;
        List<PointD> pList = new ArrayList<PointD>();

        VectorLayer aLayer = new VectorLayer(ShapeTypes.Polyline);
        String columnName = "Value";
        Field aDC = new Field(columnName, DataTypes.Integer);
        aLayer.editAddField(aDC);

        lineNum = 0;
        do {
            b = br.readByte();    // 1-data, 2-skip
            if ("2".equals(Byte.toString(b))) {
                br.skipBytes(18);
                continue;
            }
            b = br.readByte();    // Line type: country, river ...
            lType = b;
            b = br.readByte();   // Point number
            N = b;
            for (i = 0; i < N; i++) {
                bytes = new byte[3];
                br.read(bytes);    //Longitude
                int val = 0;
                for (int bb = 0; bb < 3; bb++) {
                    val <<= 8;
                    val |= (int) bytes[bb] & 0xFF;
                }
                lon = val / 10000.0;

                bytes = new byte[3];
                br.read(bytes);    //Latitude
                val = 0;
                for (int bb = 0; bb < 3; bb++) {
                    val <<= 8;
                    val |= (int) bytes[bb] & 0xFF;
                }
                lat = val / 10000.0 - 90.0;

                aPoint = new PointD();
                aPoint.X = lon;
                aPoint.Y = lat;
                pList.add(aPoint);
            }
            if (pList.size() > 1) {
                PolylineShape aPolyline = new PolylineShape();
                aPolyline.value = lineNum;
                aPolyline.setPoints(pList);
                aPolyline.setExtent(MIMath.getPointsExtent(pList));
                aPolyline.setPartNum(1);
                aPolyline.parts = new int[1];
                aPolyline.parts[0] = 0;

                int shapeNum = aLayer.getShapeNum();
                if (aLayer.editInsertShape(aPolyline, shapeNum)) {
                    aLayer.editCellValue(columnName, shapeNum, lineNum);
                }

                lineNum++;
            }
            pList = new ArrayList<PointD>();

        } while (br.available() > 0);

        br.close();

        aLayer.setLayerName(new File(aFile).getName());
        aLayer.setFileName(aFile);
        aLayer.setLayerDrawType(LayerDrawType.Map);
        aLayer.setLegendScheme(LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Polyline, Color.darkGray, 1.0F));
        aLayer.setVisible(true);

        return aLayer;
    }

    /**
     * Read image file
     *
     * @param aFile File path
     * @return Image layer
     */
    public static ImageLayer readImageFile(String aFile) throws IOException {
        String oEx = aFile.substring(aFile.lastIndexOf("."));
        String last = oEx.substring(oEx.length() - 1);
        String sEx = oEx.substring(0, oEx.length() - 2) + last;
        sEx = sEx + "w";
        String wFile = aFile.replace(oEx, sEx);
        BufferedImage aImage = ImageIO.read(new File(aFile));
        ImageLayer aImageLayer = new ImageLayer();
        aImageLayer.setFileName(aFile);
        aImageLayer.setWorldFileName(wFile);
        aImageLayer.setImage(aImage);
        aImageLayer.setLayerName(new File(aFile).getName());
        aImageLayer.setVisible(true);
        if (new File(wFile).exists()) {
            aImageLayer.readImageWorldFile(wFile);
        } else {
            WorldFilePara aWFP = new WorldFilePara();
            aWFP.xUL = 0;
            aWFP.yUL = 90;
            aWFP.xScale = 0.05;
            aWFP.yScale = -0.05;
            aWFP.xRotate = 0;
            aWFP.yRotate = 0;
            aImageLayer.setWorldFilePara(aWFP);
            aImageLayer.writeImageWorldFile(wFile, aImageLayer.getWorldFilePara());
        }

        double XBR, YBR;
        XBR = aImageLayer.getImage().getWidth() * aImageLayer.getWorldFilePara().xScale + aImageLayer.getWorldFilePara().xUL;
        YBR = aImageLayer.getImage().getHeight() * aImageLayer.getWorldFilePara().yScale + aImageLayer.getWorldFilePara().yUL;
        Extent aExtent = new Extent();
        aExtent.minX = aImageLayer.getWorldFilePara().xUL;
        aExtent.minY = YBR;
        aExtent.maxX = XBR;
        aExtent.maxY = aImageLayer.getWorldFilePara().yUL;
        aImageLayer.setExtent(aExtent);
        aImageLayer.setLayerDrawType(LayerDrawType.Image);
        aImageLayer.setMaskout(true);

        return aImageLayer;
    }

    /**
     * Create a raster layer from geotiff file
     *
     * @param fileName File path
     * @return Raster layer
     */
    public static RasterLayer readGeoTiffFile(String fileName) {
        try {
            GeoTiff geoTiff = new GeoTiff(fileName);
            geoTiff.read();
            GridData gData = geoTiff.getGridData();
            LegendScheme aLS = LegendManage.createLegendSchemeFromGridData(gData, LegendType.GraduatedColor,
                    ShapeTypes.Image);
            RasterLayer aLayer = DrawMeteoData.createRasterLayer(gData, new File(fileName).getName(), aLS);

            return aLayer;
        } catch (IOException ex) {
            Logger.getLogger(MapDataManage.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Read BIL file and create a raster layer
     *
     * @param fileName BIL file name
     * @return Raster layer
     */
    public static RasterLayer readBILFile(String fileName) {
        BILDataInfo dataInfo = new BILDataInfo();
        dataInfo.readDataInfo(fileName);
        GridData gData = dataInfo.getGridData_LonLat(0, 0, 0);
        LegendScheme aLS = LegendManage.createLegendSchemeFromGridData(gData, LegendType.GraduatedColor,
                ShapeTypes.Image);
        RasterLayer aLayer = DrawMeteoData.createRasterLayer(gData, new File(fileName).getName(), aLS);

        return aLayer;
    }

    /**
     * Read WMP file
     *
     * @param fileName The file name
     * @return Created vector layer
     */
    public static VectorLayer readMapFile_WMP(String fileName) throws IOException, Exception {
        BufferedReader sr = null;
        try {
            File file = new File(fileName);
            sr = new BufferedReader(new FileReader(file));
            String aLine;
            String shapeType;
            String[] dataArray;
            int shapeNum;
            int i, j, pNum;
            List<PointD> pList = new ArrayList<PointD>();
            PointD aPoint;
            boolean IsTrue = false;
            String columnName = "Value";
            VectorLayer aLayer = new VectorLayer(ShapeTypes.Point);
            //Read shape type
            shapeType = sr.readLine().trim().toLowerCase();
            //Read shape number
            shapeNum = Integer.parseInt(sr.readLine());
            if (shapeType.equals("point")) {
                aLayer = new VectorLayer(ShapeTypes.Point);
                aLayer.editAddField(columnName, DataTypes.Integer);

                for (i = 0; i < shapeNum; i++) {
                    aLine = sr.readLine();
                    dataArray = aLine.split(",");
                    aPoint = new PointD();
                    aPoint.X = Double.parseDouble(dataArray[0]);
                    aPoint.Y = Double.parseDouble(dataArray[1]);
                    pList.add(aPoint);
                    PointShape aPS = new PointShape();
                    aPS.setValue(i);
                    aPS.setPoint(aPoint);

                    int sNum = aLayer.getShapeNum();
                    if (aLayer.editInsertShape(aPS, sNum)) {
                        aLayer.editCellValue(columnName, sNum, i);
                    }
                }

                aLayer.setLayerName(file.getName());
                aLayer.setFileName(fileName);
                aLayer.setLayerDrawType(LayerDrawType.Map);
                aLayer.setLegendScheme(LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Point, Color.black, 5));
                aLayer.setVisible(true);
                IsTrue = true;
            } else if (shapeType.equals("polyline")) {
                aLayer = new VectorLayer(ShapeTypes.Polyline);
                aLayer.editAddField(columnName, DataTypes.Integer);

                for (i = 0; i < shapeNum; i++) {
                    pNum = Integer.parseInt(sr.readLine());
                    pList = new ArrayList<PointD>();
                    for (j = 0; j < pNum; j++) {
                        aLine = sr.readLine();
                        dataArray = aLine.split(",");
                        aPoint = new PointD();
                        aPoint.X = Double.parseDouble(dataArray[0]);
                        aPoint.Y = Double.parseDouble(dataArray[1]);
                        pList.add(aPoint);
                    }
                    PolylineShape aPLS = new PolylineShape();
                    aPLS.value = i;
                    aPLS.setExtent(MIMath.getPointsExtent(pList));
                    aPLS.setPoints(pList);

                    int sNum = aLayer.getShapeNum();
                    if (aLayer.editInsertShape(aPLS, sNum)) {
                        aLayer.editCellValue(columnName, sNum, i);
                    }
                }

                aLayer.setLayerName(file.getName());
                aLayer.setFileName(fileName);
                aLayer.setLayerDrawType(LayerDrawType.Map);
                aLayer.setLegendScheme(LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Polyline, Color.darkGray, 1.0F));
                aLayer.setVisible(true);
                IsTrue = true;
            } else if (shapeType.equals("polygon")) {
                aLayer = new VectorLayer(ShapeTypes.Polygon);
                aLayer.editAddField(columnName, DataTypes.Integer);

                ArrayList polygons = new ArrayList();
                for (i = 0; i < shapeNum; i++) {
                    pNum = Integer.parseInt(sr.readLine());
                    pList = new ArrayList<PointD>();
                    for (j = 0; j < pNum; j++) {
                        aLine = sr.readLine();
                        dataArray = aLine.split(",");
                        aPoint = new PointD();
                        aPoint.X = Double.parseDouble(dataArray[0]);
                        aPoint.Y = Double.parseDouble(dataArray[1]);
                        pList.add(aPoint);
                    }
                    PolygonShape aPGS = new PolygonShape();
                    aPGS.lowValue = i;
                    aPGS.highValue = i;
                    aPGS.setExtent(MIMath.getPointsExtent(pList));
                    aPGS.setPoints(pList);

                    int sNum = aLayer.getShapeNum();
                    if (aLayer.editInsertShape(aPGS, sNum)) {
                        aLayer.editCellValue(columnName, sNum, i);
                    }
                }

                aLayer.setLayerName(file.getName());
                aLayer.setFileName(fileName);
                aLayer.setLayerDrawType(LayerDrawType.Map);
                aLayer.setLegendScheme(LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Polygon, new Color(255, 251, 195), 1.0F));
                aLayer.setVisible(true);
                IsTrue = true;
            } else {
                JOptionPane.showMessageDialog(null, "Shape type is invalid!" + System.getProperty("line.separator")
                        + shapeType);
                IsTrue = false;
            }
            sr.close();
            if (IsTrue) {
                return aLayer;
            } else {
                return null;
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MapDataManage.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            try {
                sr.close();
            } catch (IOException ex) {
                Logger.getLogger(MapDataManage.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Write WMP file
     *
     * @param fileName The file name
     * @param shapes Shapes
     */
    public static void writeMapFile_WMP(String fileName, List<Shape> shapes) {
        BufferedWriter sw = null;
        try {
            File file = new File(fileName);
            sw = new BufferedWriter(new FileWriter(file));
            int shpNum = shapes.size();
            int i;
            switch (shapes.get(0).getShapeType()) {
                case Point:
                    sw.write("Point");
                    sw.newLine();
                    sw.write(String.valueOf(shpNum));
                    sw.newLine();
                    PointShape aPS;
                    for (i = 0; i < shpNum; i++) {
                        aPS = (PointShape) shapes.get(i);
                        if (aPS.isSelected()) {
                            sw.write(String.valueOf(aPS.getPoint().X) + "," + String.valueOf(aPS.getPoint().Y));
                            sw.newLine();
                        }
                    }
                    break;
                case Polyline:
                    sw.write("Polyline");
                    sw.newLine();
                    int shapeNum = 0;
                    PolylineShape aPLS;
                    for (i = 0; i < shpNum; i++) {
                        aPLS = (PolylineShape) shapes.get(i);
                        shapeNum += aPLS.getPartNum();
                    }
                    sw.write(String.valueOf(shapeNum));
                    sw.newLine();

                    shapeNum = 0;
                    for (i = 0; i < shpNum; i++) {
                        aPLS = (PolylineShape) shapes.get(i);
                        PointD[] Pointps;
                        for (int p = 0; p < aPLS.getPartNum(); p++) {
                            if (p == aPLS.getPartNum() - 1) {
                                Pointps = new PointD[aPLS.getPointNum() - aPLS.parts[p]];
                                for (int pp = aPLS.parts[p]; pp < aPLS.getPointNum(); pp++) {
                                    Pointps[pp - aPLS.parts[p]] = (PointD) aPLS.getPoints().get(pp);
                                }
                            } else {
                                Pointps = new PointD[aPLS.parts[p + 1] - aPLS.parts[p]];
                                for (int pp = aPLS.parts[p]; pp < aPLS.parts[p + 1]; pp++) {
                                    Pointps[pp - aPLS.parts[p]] = (PointD) aPLS.getPoints().get(pp);
                                }
                            }
                            sw.write(String.valueOf(Pointps.length));
                            sw.newLine();
                            for (PointD aPoint : Pointps) {
                                sw.write(String.valueOf(aPoint.X) + "," + String.valueOf(aPoint.Y));
                                sw.newLine();
                            }
                            shapeNum += 1;
                        }
                    }
                    break;
                case Polygon:
                    sw.write("Polygon");
                    sw.newLine();
                    shapeNum = 0;
                    PolygonShape aPGS;
                    for (i = 0; i < shpNum; i++) {
                        aPGS = (PolygonShape) shapes.get(i);
                        shapeNum += aPGS.getPartNum();
                    }
                    sw.write(String.valueOf(shapeNum));
                    sw.newLine();

                    shapeNum = 0;
                    for (i = 0; i < shpNum; i++) {
                        aPGS = (PolygonShape) shapes.get(i);

                        PointD[] Pointps;
                        for (int p = 0; p < aPGS.getPartNum(); p++) {
                            if (p == aPGS.getPartNum() - 1) {
                                Pointps = new PointD[aPGS.getPointNum() - aPGS.parts[p]];
                                for (int pp = aPGS.parts[p]; pp < aPGS.getPointNum(); pp++) {
                                    Pointps[pp - aPGS.parts[p]] = (PointD) aPGS.getPoints().get(pp);
                                }
                            } else {
                                Pointps = new PointD[aPGS.parts[p + 1] - aPGS.parts[p]];
                                for (int pp = aPGS.parts[p]; pp < aPGS.parts[p + 1]; pp++) {
                                    Pointps[pp - aPGS.parts[p]] = (PointD) aPGS.getPoints().get(pp);
                                }
                            }
                            sw.write(String.valueOf(Pointps.length));
                            sw.newLine();
                            for (PointD aPoint : Pointps) {
                                sw.write(String.valueOf(aPoint.X) + "," + String.valueOf(aPoint.Y));
                                sw.newLine();
                            }
                            shapeNum += 1;
                        }
                    }
                    break;
            }
            sw.close();
        } catch (IOException ex) {
            Logger.getLogger(MapDataManage.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                sw.close();
            } catch (IOException ex) {
                Logger.getLogger(MapDataManage.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
