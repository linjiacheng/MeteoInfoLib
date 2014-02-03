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
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.meteoinfo.data.meteodata.bandraster.BILDataInfo;
import org.meteoinfo.layer.RasterLayer;
import org.meteoinfo.legend.LegendScheme;
import org.meteoinfo.legend.LegendType;

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
                //aLayer = MapDataManage.ReadMapFile_WMP(aFile);
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
     * @param fileName BIL file name
     * @return Raster layer
     */
    public static RasterLayer readBILFile(String fileName){
        BILDataInfo dataInfo = new BILDataInfo();
        dataInfo.readDataInfo(fileName);
        GridData gData = dataInfo.getGridData_LonLat(0, 0, 0);
        LegendScheme aLS = LegendManage.createLegendSchemeFromGridData(gData, LegendType.GraduatedColor,
                    ShapeTypes.Image);
            RasterLayer aLayer = DrawMeteoData.createRasterLayer(gData, new File(fileName).getName(), aLS);

            return aLayer;
    }
}
