/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.plot;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.meteoinfo.data.ArrayMath;
import org.meteoinfo.data.ArrayUtil;
import org.meteoinfo.data.GridArray;
import org.meteoinfo.data.GridData;
import org.meteoinfo.data.XYListDataset;
import org.meteoinfo.data.analysis.Statistics;
import org.meteoinfo.drawing.ContourDraw;
import org.meteoinfo.drawing.Draw;
import org.meteoinfo.drawing.PointStyle;
import org.meteoinfo.geoprocess.GeoComputation;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import org.meteoinfo.legend.BarBreak;
import org.meteoinfo.legend.ColorBreak;
import org.meteoinfo.legend.LegendManage;
import org.meteoinfo.legend.LegendScheme;
import org.meteoinfo.legend.LegendType;
import org.meteoinfo.legend.LineStyles;
import org.meteoinfo.legend.PointBreak;
import org.meteoinfo.legend.PolygonBreak;
import org.meteoinfo.legend.PolylineBreak;
import org.meteoinfo.shape.ArcShape;
import org.meteoinfo.shape.BarShape;
import org.meteoinfo.shape.Graphic;
import org.meteoinfo.shape.GraphicCollection;
import org.meteoinfo.shape.ImageShape;
import org.meteoinfo.shape.PointShape;
import org.meteoinfo.shape.PolygonShape;
import org.meteoinfo.shape.PolylineErrorShape;
import org.meteoinfo.shape.PolylineShape;
import org.meteoinfo.shape.RectangleShape;
import org.meteoinfo.shape.Shape;
import org.meteoinfo.shape.ShapeTypes;
import org.meteoinfo.shape.WindArrow;
import org.meteoinfo.shape.WindBarb;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import wContour.Global.PolyLine;

/**
 *
 * @author Yaqiang Wang
 */
public class GraphicFactory {

    /**
     * Create LineString graphic
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param cb Color break
     * @return LineString graphic
     */
    public static GraphicCollection createLineString(Array xdata, Array ydata, ColorBreak cb) {
        GraphicCollection gc = new GraphicCollection();
        PolylineShape pls;
        List<PointD> points = new ArrayList<>();
        double x, y;
        for (int i = 0; i < xdata.getSize(); i++) {
            x = xdata.getDouble(i);
            y = ydata.getDouble(i);
            if (Double.isNaN(y) || Double.isNaN(x)) {
                if (points.isEmpty()) {
                    continue;
                }
                if (points.size() == 1) {
                    points.add((PointD) points.get(0).clone());
                }
                pls = new PolylineShape();
                pls.setPoints(points);
                gc.add(new Graphic(pls, cb));
                points = new ArrayList<>();
            } else {
                points.add(new PointD(x, y));
            }
        }
        if (points.size() == 1) {
            points.add((PointD) points.get(0).clone());
        }
        pls = new PolylineShape();
        pls.setPoints(points);
        gc.add(new Graphic(pls, cb));

        return gc;
    }

    /**
     * Create LineString graphic
     *
     * @param data Y data array
     * @param cbs Color breaks
     * @return LineString graphic
     */
    public static GraphicCollection createLineString(XYListDataset data, List<ColorBreak> cbs) {
        GraphicCollection gc = new GraphicCollection();
        PolylineShape pls;
        List<PointD> points;
        double x, y;
        for (int i = 0; i < data.getSeriesCount(); i++) {
            points = new ArrayList<>();
            for (int j = 0; j < data.getItemCount(i); j++) {
                x = data.getX(i, j);
                y = data.getY(i, j);
                points.add(new PointD(x, y));
            }
            pls = new PolylineShape();
            pls.setPoints(points);
            gc.add(new Graphic(pls, cbs.get(i)));
        }
        gc.setSingleLegend(false);

        return gc;
    }

    /**
     * Create error LineString graphic
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param xError X error array
     * @param yError Y error array
     * @param cb Color break
     * @return LineString graphic
     */
    public static GraphicCollection createErrorLineString(Array xdata, Array ydata, Array xError, Array yError, ColorBreak cb) {
        GraphicCollection gc = new GraphicCollection();
        PolylineErrorShape pls;
        List<PointD> points = new ArrayList<>();
        List<Number> xerrors = new ArrayList<>();
        List<Number> yerrors = new ArrayList<>();
        double x, y;
        for (int i = 0; i < xdata.getSize(); i++) {
            x = xdata.getDouble(i);
            y = ydata.getDouble(i);
            if (Double.isNaN(y) || Double.isNaN(x)) {
                if (points.isEmpty()) {
                    continue;
                }
                if (points.size() == 1) {
                    points.add((PointD) points.get(0).clone());
                }
                pls = new PolylineErrorShape();
                pls.setPoints(points);
                if (xError != null) {
                    pls.setXerror(xerrors);
                }
                if (yError != null) {
                    pls.setYerror(yerrors);
                }
                pls.updateExtent();
                gc.add(new Graphic(pls, cb));
                points = new ArrayList<>();
                xerrors = new ArrayList<>();
                yerrors = new ArrayList<>();
            } else {
                points.add(new PointD(x, y));
                if (xError != null) {
                    xerrors.add(xError.getDouble(i));
                }
                if (yError != null) {
                    yerrors.add(yError.getDouble(i));
                }
            }
        }
        if (points.size() == 1) {
            points.add((PointD) points.get(0).clone());
        }
        pls = new PolylineErrorShape();
        pls.setPoints(points);
        if (xError != null) {
            pls.setXerror(xerrors);
        }
        if (yError != null) {
            pls.setYerror(yerrors);
        }
        pls.updateExtent();
        gc.add(new Graphic(pls, cb));

        return gc;
    }

    /**
     * Create graphics
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param cb Color break
     * @return LineString graphic
     */
    public static GraphicCollection createGraphics(Array xdata, Array ydata, ColorBreak cb) {
        GraphicCollection graphics = new GraphicCollection();
        if (cb instanceof PolylineBreak) {
            graphics.add(createLineString(xdata, ydata, cb));
        } else {
            PointShape ps;
            for (int i = 0; i < xdata.getSize(); i++) {
                ps = new PointShape();
                ps.setPoint(new PointD(xdata.getDouble(i), ydata.getDouble(i)));
                graphics.add(new Graphic(ps, cb));
            }
        }
        return graphics;
    }

    /**
     * Create graphics
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param cbs Color breaks
     * @return LineString graphic
     */
    public static GraphicCollection createPoints(Array xdata, Array ydata, List<ColorBreak> cbs) {
        GraphicCollection graphics = new GraphicCollection();
        PointShape ps;
        if (cbs.size() == xdata.getSize()) {
            for (int i = 0; i < xdata.getSize(); i++) {
                ps = new PointShape();
                ps.setPoint(new PointD(xdata.getDouble(i), ydata.getDouble(i)));
                graphics.add(new Graphic(ps, cbs.get(i)));
            }
            graphics.setSingleLegend(false);
        } else {
            for (int i = 0; i < xdata.getSize(); i++) {
                ps = new PointShape();
                ps.setPoint(new PointD(xdata.getDouble(i), ydata.getDouble(i)));
                graphics.add(new Graphic(ps, cbs.get(0)));
            }
        }
        return graphics;
    }

    /**
     * Create graphics
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param zdata Z data array
     * @param ls Legend scheme
     * @return LineString graphic
     */
    public static GraphicCollection createPoints(Array xdata, Array ydata, Array zdata, LegendScheme ls) {
        GraphicCollection graphics = new GraphicCollection();
        PointShape ps;
        double z;
        ColorBreak cb;
        for (int i = 0; i < xdata.getSize(); i++) {
            ps = new PointShape();
            ps.setPoint(new PointD(xdata.getDouble(i), ydata.getDouble(i)));
            z = zdata.getDouble(i);
            cb = ls.getLegenBreak(z);
            graphics.add(new Graphic(ps, cb));
        }
        graphics.setSingleLegend(false);
        return graphics;
    }

    /**
     * Add polygons
     *
     * @param xa X coordinate array
     * @param ya Y coordinate array
     * @param pgb PolygonBreak
     * @return Graphics
     */
    public static GraphicCollection createPolygons(Array xa, Array ya, PolygonBreak pgb) {
        GraphicCollection graphics = new GraphicCollection();
        double x, y;
        int n = (int)xa.getSize();
        PolygonShape pgs;
        PointD p;
        List<PointD> points = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            x = xa.getDouble(i);
            y = ya.getDouble(i);
            if (Double.isNaN(x)) {
                if (points.size() > 2) {
                    pgs = new PolygonShape();
                    pgs.setPoints(points);
                    Graphic aGraphic = new Graphic(pgs, pgb);
                    graphics.add(aGraphic);
                }
                points = new ArrayList<>();
            } else {
                p = new PointD(x, y);
                points.add(p);
            }
        }
        if (points.size() > 2) {
            pgs = new PolygonShape();
            pgs.setPoints(points);
            Graphic aGraphic = new Graphic(pgs, pgb);
            graphics.add(aGraphic);
        }
        return graphics;
    }
    
    /**
     * Create rectangle graphic
     * @param pos Rectangle position
     * @param curvature Curvature
     * @param pgb Polygon break
     * @return 
     */
    public static Graphic createRectangle(List<Number> pos, List<Number> curvature, PolygonBreak pgb){
        RectangleShape rect = new RectangleShape(pos.get(0).doubleValue(), pos.get(1).doubleValue(), 
                pos.get(2).doubleValue(), pos.get(3).doubleValue());
        if (curvature != null){
            rect.setRoundX(curvature.get(0).doubleValue());
            rect.setRoundY(curvature.get(1).doubleValue());
        }
        Graphic graphic = new Graphic(rect, pgb);
        return graphic;
    }
    
    /**
     * Create bar graphics
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param autoWidth Is auto width or not
     * @param widths Width
     * @param drawError Is draw error or not
     * @param error Error
     * @param drawBottom Is draw bottom or not
     * @param bottom Bottom
     * @param bbs Bar breaks
     * @return Bar graphics
     */
    public static GraphicCollection createBars(Array xdata, Array ydata, boolean autoWidth,
            Array widths, boolean drawError, Array error, boolean drawBottom, Array bottom,
            List<BarBreak> bbs) {
        GraphicCollection graphics = new GraphicCollection();
        int n = (int) xdata.getSize();
        double x, y;
        BarBreak bb = bbs.get(0);
        PolylineBreak ebreak = new PolylineBreak();
        ebreak.setColor(bb.getErrorColor());
        ebreak.setSize(bb.getErrorSize());
        double width = widths.getDouble(0);
        if (autoWidth && xdata.getSize() > 1){
            width = (xdata.getDouble(1) - xdata.getDouble(0)) * width;
        }
        double bot = 0;
        if (drawBottom){
            bot = bottom.getDouble(0);
        }
        double miny = 0;
        boolean baseLine = false;
        for (int i = 0; i < n; i++) {
            x = xdata.getDouble(i);
            y = ydata.getDouble(i);
            // Add bar
            if (drawBottom) {
                if (bottom.getSize() > i)
                    bot = bottom.getDouble(i);
                miny = bot;
                y += miny;
            }
            if (y < miny) {
                baseLine = true;
            }
            if (widths.getSize() > 1 && widths.getSize() > i){
                width = widths.getDouble(i);
            }
            List<PointD> pList = new ArrayList<>();
            pList.add(new PointD(x, miny));
            pList.add(new PointD(x, y));
            pList.add(new PointD(x + width, y));
            pList.add(new PointD(x + width, miny));
            pList.add(new PointD(x, miny));
            PolygonShape pgs = new PolygonShape();
            pgs.setPoints(pList);
            if (bbs.size() > i) {
                bb = bbs.get(i);
            }
            graphics.add(new Graphic(pgs, bb));

            if (drawError) {
                //Add error line
                double e = error.getDouble(i);
                pList = new ArrayList<>();
                pList.add(new PointD(x + width * 0.5, y - e));
                pList.add(new PointD(x + width * 0.5, y + e));
                PolylineShape pls = new PolylineShape();
                pls.setPoints(pList);
                graphics.add(new Graphic(pls, ebreak));
                //Add cap
                pList = new ArrayList<>();
                pList.add(new PointD(x + width * 0.25, y - e));
                pList.add(new PointD(x + width * 0.75, y - e));
                pls = new PolylineShape();
                pls.setPoints(pList);
                graphics.add(new Graphic(pls, ebreak));
                pList = new ArrayList<>();
                pList.add(new PointD(x + width * 0.25, y + e));
                pList.add(new PointD(x + width * 0.75, y + e));
                pls = new PolylineShape();
                pls.setPoints(pList);
                graphics.add(new Graphic(pls, ebreak));
            }
        }

        if (baseLine) {
            List<PointD> pList = new ArrayList<>();
            double x1 = xdata.getDouble(0);
            double x2 = xdata.getDouble((int) xdata.getSize() - 1);
            x1 -= (x2 - x1);
            x2 += (x2 - x1);
            pList.add(new PointD(x1, miny));
            pList.add(new PointD(x2, miny));
            PolylineShape pls = new PolylineShape();
            pls.setPoints(pList);
            ebreak = new PolylineBreak();     
            ebreak.setColor(Color.black);
            graphics.add(new Graphic(pls, ebreak));
        }
        graphics.setSingleLegend(false);

        return graphics;
    }
    
    /**
     * Create bar graphics
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param autoWidth Is auto width or not
     * @param widths Width
     * @param drawError Is draw error or not
     * @param error Error
     * @param drawBottom Is draw bottom or not
     * @param bottom Bottom
     * @param bbs Bar breaks
     * @return Bar graphics
     */
    public static GraphicCollection createBars1(Array xdata, Array ydata, boolean autoWidth,
            Array widths, boolean drawError, Array error, boolean drawBottom, Array bottom,
            List<BarBreak> bbs) {
        GraphicCollection graphics = new GraphicCollection();
        int n = (int) xdata.getSize();
        double x, y;
        BarBreak bb = bbs.get(0);
        PolylineBreak ebreak = new PolylineBreak();
        ebreak.setColor(bb.getErrorColor());
        ebreak.setSize(bb.getErrorSize());
        double width = widths.getDouble(0);
        if (autoWidth && xdata.getSize() > 1){
            width = (xdata.getDouble(1) - xdata.getDouble(0)) * width;
        }
        double bot = 0;
        if (drawBottom){
            bot = bottom.getDouble(0);
        }
        double miny = 0;
        boolean baseLine = false;
        for (int i = 0; i < n; i++) {
            x = xdata.getDouble(i);
            y = ydata.getDouble(i);
            // Add bar
            if (drawBottom) {
                if (bottom.getSize() > i)
                    bot = bottom.getDouble(i);
                miny = bot;
                y += miny;
            }
            if (y < miny) {
                baseLine = true;
            }
            if (widths.getSize() > 1 && widths.getSize() > i){
                width = widths.getDouble(i);
            }
            List<PointD> pList = new ArrayList<>();
            pList.add(new PointD(x, miny));
            for (double x1 = x; x1 < x + width; x1 += width / 100){
                pList.add(new PointD(x1, y));
            }
            pList.add(new PointD(x + width, y));
            for (double x1 = x + width; x1 > x; x1 -= width / 20){
                pList.add(new PointD(x1, miny));
            }
            pList.add(new PointD(x, miny));
            PolygonShape pgs = new PolygonShape();
            pgs.setPoints(pList);
            if (bbs.size() > i) {
                bb = bbs.get(i);
            }
            graphics.add(new Graphic(pgs, bb));

            if (drawError) {
                //Add error line
                double e = error.getDouble(i);
                pList = new ArrayList<>();
                pList.add(new PointD(x + width * 0.5, y - e));
                pList.add(new PointD(x + width * 0.5, y + e));
                PolylineShape pls = new PolylineShape();
                pls.setPoints(pList);
                graphics.add(new Graphic(pls, ebreak));
                //Add cap
                pList = new ArrayList<>();
                pList.add(new PointD(x + width * 0.25, y - e));
                pList.add(new PointD(x + width * 0.75, y - e));
                pls = new PolylineShape();
                pls.setPoints(pList);
                graphics.add(new Graphic(pls, ebreak));
                pList = new ArrayList<>();
                pList.add(new PointD(x + width * 0.25, y + e));
                pList.add(new PointD(x + width * 0.75, y + e));
                pls = new PolylineShape();
                pls.setPoints(pList);
                graphics.add(new Graphic(pls, ebreak));
            }
        }

        if (baseLine) {
            List<PointD> pList = new ArrayList<>();
            double x1 = xdata.getDouble(0);
            double x2 = xdata.getDouble((int) xdata.getSize() - 1);
            x1 -= (x2 - x1);
            x2 += (x2 - x1);
            pList.add(new PointD(x1, miny));
            pList.add(new PointD(x2, miny));
            PolylineShape pls = new PolylineShape();
            pls.setPoints(pList);
            ebreak = new PolylineBreak();     
            ebreak.setColor(Color.black);
            graphics.add(new Graphic(pls, ebreak));
        }
        graphics.setSingleLegend(false);

        return graphics;
    }

    /**
     * Create bar graphics
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param autoWidth Is auto width or not
     * @param width Width
     * @param drawError Is draw error or not
     * @param error Error
     * @param drawBottom Is draw bottom or not
     * @param bottom Bottom
     * @param bbs Bar breaks
     * @return Bar graphics
     */
    public static GraphicCollection createBars_bak(Array xdata, Array ydata, boolean autoWidth,
            double width, boolean drawError, Array error, boolean drawBottom, Array bottom,
            List<BarBreak> bbs) {
        GraphicCollection graphics = new GraphicCollection();
        int n = (int) xdata.getSize();
        double x, y;
        BarBreak bb = bbs.get(0);
        for (int i = 0; i < n; i++) {
            x = xdata.getDouble(i);
            y = ydata.getDouble(i);
            BarShape bs = new BarShape();
            bs.setPoint(new PointD(x, y));
            bs.setAutoWidth(autoWidth);
            bs.setWidth(width);
            bs.setDrawError(drawError);
            if (drawError) {
                bs.setError(error.getDouble(i));
            }
            bs.setDrawBottom(drawBottom);
            if (drawBottom) {
                bs.setBottom(bottom.getDouble(i));
            }
            if (bbs.size() > i) {
                bb = bbs.get(i);
            }
            graphics.add(new Graphic(bs, bb));
        }
        if (bbs.size() == 1) {
            graphics.setSingleLegend(true);
        } else {
            graphics.setSingleLegend(false);
        }

        return graphics;
    }

    /**
     * Create histogram bar graphics
     *
     * @param data The data array
     * @param nbin Bin number
     * @param bbs Bar breaks
     * @return Bar graphics
     */
    public static GraphicCollection createHistBars(Array data, int nbin,
            List<BarBreak> bbs) {
        List<Array> r = ArrayUtil.histogram(data, nbin);
        Array xdata = r.get(1);
        Array ydata = r.get(0);
        GraphicCollection graphics = new GraphicCollection();
        int n = (int) ydata.getSize();
        double x, y, width;
        BarBreak bb = bbs.get(0);
        for (int i = 0; i < n; i++) {
            x = (xdata.getDouble(i + 1) + xdata.getDouble(i)) * 0.5;
            width = xdata.getDouble(i + 1) - xdata.getDouble(i);
            y = ydata.getDouble(i);
            BarShape bs = new BarShape();
            bs.setPoint(new PointD(x, y));
            bs.setAutoWidth(false);
            bs.setWidth(width);
            bs.setDrawBottom(false);
            if (bbs.size() > i) {
                bb = bbs.get(i);
            }
            graphics.add(new Graphic(bs, bb));
        }
        if (bbs.size() == 1) {
            graphics.setSingleLegend(true);
        } else {
            graphics.setSingleLegend(false);
        }

        return graphics;
    }
    
    /**
     * Create image
     *
     * @param x X data array
     * @param y Y data array
     * @param gdata data array
     * @return Image graphic
     */
    public static Graphic createImage(Array x, Array y, Array gdata) {
        int width, height;
        width = (int) x.getSize();
        height = (int) y.getSize();
        Color undefColor = Color.white;
        BufferedImage aImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Color color;
        Index index = gdata.getIndex();
        boolean isAlpha = gdata.getShape()[2] == 4;
        if (gdata.getDataType() == DataType.FLOAT || gdata.getDataType() == DataType.DOUBLE){
            float r, g, b;
            if (isAlpha) {
                float a;
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        r = gdata.getFloat(index.set(i, j, 0));
                        g = gdata.getFloat(index.set(i, j, 1));
                        b = gdata.getFloat(index.set(i, j, 2));
                        a = gdata.getFloat(index.set(i, j, 3));
                        if (Double.isNaN(r) || Double.isNaN(g) || Double.isNaN(b) || Double.isNaN(a)) {
                            color = undefColor;
                        } else {
                            color = new Color(r, g, b, a);
                        }
                        aImage.setRGB(j, height - i - 1, color.getRGB());
                    }
                }
            } else {
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        r = gdata.getFloat(index.set(i, j, 0));
                        g = gdata.getFloat(index.set(i, j, 1));
                        b = gdata.getFloat(index.set(i, j, 2));
                        if (Double.isNaN(r) || Double.isNaN(g) || Double.isNaN(b)) {
                            color = undefColor;
                        } else {
                            color = new Color(r, g, b);
                        }
                        aImage.setRGB(j, height - i - 1, color.getRGB());
                    }
                }
            }
        } else {
            int r, g, b;
            if (isAlpha) {
                int a;
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        r = gdata.getInt(index.set(i, j, 0));
                        g = gdata.getInt(index.set(i, j, 1));
                        b = gdata.getInt(index.set(i, j, 2));
                        a = gdata.getInt(index.set(i, j, 3));
                        if (Double.isNaN(r) || Double.isNaN(g) || Double.isNaN(b) || Double.isNaN(a)) {
                            color = undefColor;
                        } else {
                            color = new Color(r, g, b, a);
                        }
                        aImage.setRGB(j, height - i - 1, color.getRGB());
                    }
                }
            } else {
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        r = gdata.getInt(index.set(i, j, 0));
                        g = gdata.getInt(index.set(i, j, 1));
                        b = gdata.getInt(index.set(i, j, 2));
                        if (Double.isNaN(r) || Double.isNaN(g) || Double.isNaN(b)) {
                            color = undefColor;
                        } else {
                            color = new Color(r, g, b);
                        }
                        aImage.setRGB(j, height - i - 1, color.getRGB());
                    }
                }
            }
        }

        ImageShape ishape = new ImageShape();
        ishape.setPoint(new PointD(x.getDouble(0), y.getDouble(0)));
        ishape.setImage(aImage);
        ishape.setExtent(new Extent(x.getDouble(0), x.getDouble((int) x.getSize() - 1),
                y.getDouble(0), y.getDouble((int) y.getSize() - 1)));
        return new Graphic(ishape, new ColorBreak());
    }
    
    /**
     * Create image
     *
     * @param x X data array
     * @param y Y data array
     * @param data data array
     * @return Image graphic
     */
    public static Graphic createImage(Array x, Array y, List<Array> data) {
        int width, height;
        width = (int) x.getSize();
        height = (int) y.getSize();
        Color undefColor = Color.white;
        BufferedImage aImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Color color;
        boolean isAlpha = data.size() == 4;
        Array rdata = data.get(0);
        Array gdata = data.get(1);
        Array bdata = data.get(2);
        Index rindex = rdata.getIndex();
        Index gindex = gdata.getIndex();
        Index bindex = bdata.getIndex();
        if (rdata.getDataType() == DataType.FLOAT || rdata.getDataType() == DataType.DOUBLE){
            float r, g, b;
            if (isAlpha) {
                float a;
                Array adata = data.get(3);
                Index aindex = adata.getIndex();
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        r = rdata.getFloat(rindex.set(i, j));
                        g = gdata.getFloat(gindex.set(i, j));
                        b = bdata.getFloat(bindex.set(i, j));
                        a = adata.getFloat(aindex.set(i, j));
                        if (Double.isNaN(r) || Double.isNaN(g) || Double.isNaN(b) || Double.isNaN(a)) {
                            color = undefColor;
                        } else {
                            color = new Color(r, g, b, a);
                        }
                        aImage.setRGB(j, height - i - 1, color.getRGB());
                    }
                }
            } else {
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        r = rdata.getFloat(rindex.set(i, j));
                        g = gdata.getFloat(gindex.set(i, j));
                        b = bdata.getFloat(bindex.set(i, j));
                        if (Double.isNaN(r) || Double.isNaN(g) || Double.isNaN(b)) {
                            color = undefColor;
                        } else {
                            color = new Color(r, g, b);
                        }
                        aImage.setRGB(j, height - i - 1, color.getRGB());
                    }
                }
            }
        } else {
            int r, g, b;
            if (isAlpha) {
                int a;
                Array adata = data.get(3);
                Index aindex = adata.getIndex();
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        r = rdata.getInt(rindex.set(i, j));
                        g = gdata.getInt(gindex.set(i, j));
                        b = bdata.getInt(bindex.set(i, j));
                        a = adata.getInt(aindex.set(i, j));
                        if (Double.isNaN(r) || Double.isNaN(g) || Double.isNaN(b) || Double.isNaN(a)) {
                            color = undefColor;
                        } else {
                            color = new Color(r, g, b, a);
                        }
                        aImage.setRGB(j, height - i - 1, color.getRGB());
                    }
                }
            } else {
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        r = rdata.getInt(rindex.set(i, j));
                        g = gdata.getInt(gindex.set(i, j));
                        b = bdata.getInt(bindex.set(i, j));
                        if (Double.isNaN(r) || Double.isNaN(g) || Double.isNaN(b)) {
                            color = undefColor;
                        } else {
                            color = new Color(r, g, b);
                        }
                        aImage.setRGB(j, height - i - 1, color.getRGB());
                    }
                }
            }
        }

        ImageShape ishape = new ImageShape();
        ishape.setPoint(new PointD(x.getDouble(0), y.getDouble(0)));
        ishape.setImage(aImage);
        ishape.setExtent(new Extent(x.getDouble(0), x.getDouble((int) x.getSize() - 1),
                y.getDouble(0), y.getDouble((int) y.getSize() - 1)));
        return new Graphic(ishape, new ColorBreak());
    }

    /**
     * Create image
     *
     * @param x X data array
     * @param y Y data array
     * @param gdata Grid data array
     * @param ls Legend scheme
     * @return Image graphic
     */
    public static Graphic createImage(Array x, Array y, Array gdata, LegendScheme ls) {
        int width, height, breakNum;
        width = (int) x.getSize();
        height = (int) y.getSize();
        breakNum = ls.getBreakNum();
        double[] breakValue = new double[breakNum];
        Color[] breakColor = new Color[breakNum];
        Color undefColor = Color.white;
        for (int i = 0; i < breakNum; i++) {
            breakValue[i] = Double.parseDouble(ls.getLegendBreaks().get(i).getEndValue().toString());
            breakColor[i] = ls.getLegendBreaks().get(i).getColor();
            if (ls.getLegendBreaks().get(i).isNoData()) {
                undefColor = ls.getLegendBreaks().get(i).getColor();
            }
        }
        Color defaultColor = breakColor[breakNum - 1];    //默认颜色为最后一个颜色
        BufferedImage aImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        double oneValue;
        Color oneColor;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                //oneValue = gdata.data[i][j];
                oneValue = gdata.getDouble(i * width + j);
                if (Double.isNaN(oneValue)) {
                    oneColor = undefColor;
                } else {
                    oneColor = defaultColor;
                    if (ls.getLegendType() == LegendType.GraduatedColor) {
                        //循环只到breakNum-1 是因为最后一个LegendBreaks的EndValue和StartValue是一样的
                        for (int k = 0; k < breakNum - 1; k++) {
                            if (oneValue < breakValue[k]) {
                                oneColor = breakColor[k];
                                break;
                            }
                        }
                    } else {
                        for (int k = 0; k < breakNum - 1; k++) {
                            if (oneValue == breakValue[k]) {
                                oneColor = breakColor[k];
                                break;
                            }
                        }
                    }
                }
                aImage.setRGB(j, height - i - 1, oneColor.getRGB());
            }
        }

        ImageShape ishape = new ImageShape();
        ishape.setPoint(new PointD(x.getDouble(0), y.getDouble(0)));
        ishape.setImage(aImage);
        ishape.setExtent(new Extent(x.getDouble(0), x.getDouble((int) x.getSize() - 1),
                y.getDouble(0), y.getDouble((int) y.getSize() - 1)));
        return new Graphic(ishape, new ColorBreak());
    }

    /**
     * Create image
     *
     * @param gdata Grid data array
     * @param ls Legend scheme
     * @return Image graphic
     */
    public static Graphic createImage(GridArray gdata, LegendScheme ls) {
        int width, height, breakNum;
        width = gdata.getXNum();
        height = gdata.getYNum();
        breakNum = ls.getBreakNum();
        double[] breakValue = new double[breakNum];
        Color[] breakColor = new Color[breakNum];
        Color undefColor = Color.white;
        for (int i = 0; i < breakNum; i++) {
            breakValue[i] = Double.parseDouble(ls.getLegendBreaks().get(i).getEndValue().toString());
            breakColor[i] = ls.getLegendBreaks().get(i).getColor();
            if (ls.getLegendBreaks().get(i).isNoData()) {
                undefColor = ls.getLegendBreaks().get(i).getColor();
            }
        }
        Color defaultColor = breakColor[breakNum - 1];    //默认颜色为最后一个颜色
        BufferedImage aImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        double oneValue;
        Color oneColor;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                //oneValue = gdata.data[i][j];
                oneValue = gdata.getDoubleValue(i, j);
                if (Double.isNaN(oneValue) || MIMath.doubleEquals(oneValue, gdata.missingValue)) {
                    oneColor = undefColor;
                } else {
                    oneColor = defaultColor;
                    if (ls.getLegendType() == LegendType.GraduatedColor) {
                        //循环只到breakNum-1 是因为最后一个LegendBreaks的EndValue和StartValue是一样的
                        for (int k = 0; k < breakNum - 1; k++) {
                            if (oneValue < breakValue[k]) {
                                oneColor = breakColor[k];
                                break;
                            }
                        }
                    } else {
                        for (int k = 0; k < breakNum - 1; k++) {
                            if (oneValue == breakValue[k]) {
                                oneColor = breakColor[k];
                                break;
                            }
                        }
                    }
                }
                aImage.setRGB(j, height - i - 1, oneColor.getRGB());
            }
        }

        ImageShape ishape = new ImageShape();
        ishape.setPoint(new PointD(gdata.xArray[0], gdata.yArray[0]));
        ishape.setImage(aImage);
        ishape.setExtent(new Extent(gdata.xArray[0], gdata.xArray[gdata.xArray.length - 1],
                gdata.yArray[0], gdata.yArray[gdata.yArray.length - 1]));
        return new Graphic(ishape, new ColorBreak());
    }

    /**
     * Create contour lines
     *
     * @param gridData Grid data
     * @param ls Legend scheme
     * @param isSmooth Is smooth or not
     * @return Contour lines
     */
    public static GraphicCollection createContourLines(GridData gridData, LegendScheme ls, boolean isSmooth) {
        ls = ls.convertTo(ShapeTypes.Polyline);
        Object[] ccs = LegendManage.getContoursAndColors(ls);
        double[] cValues = (double[]) ccs[0];

        int[][] S1 = new int[gridData.data.length][gridData.data[0].length];
        Object[] cbs = ContourDraw.tracingContourLines(gridData.data,
                cValues, gridData.xArray, gridData.yArray, gridData.missingValue, S1);
        List<wContour.Global.PolyLine> ContourLines = (List<wContour.Global.PolyLine>) cbs[0];

        if (ContourLines.isEmpty()) {
            return null;
        }

        if (isSmooth) {
            ContourLines = wContour.Contour.smoothLines(ContourLines);
        }

        wContour.Global.PolyLine aLine;
        double v;
        ColorBreak cbb = ls.getLegenBreak(0);
        GraphicCollection graphics = new GraphicCollection();
        for (int i = 0; i < ContourLines.size(); i++) {
            aLine = ContourLines.get(i);
            v = aLine.Value;

            PolylineShape aPolyline = new PolylineShape();
            PointD aPoint;
            List<PointD> pList = new ArrayList<>();
            for (int j = 0; j < aLine.PointList.size(); j++) {
                aPoint = new PointD();
                aPoint.X = aLine.PointList.get(j).X;
                aPoint.Y = aLine.PointList.get(j).Y;
                pList.add(aPoint);
            }
            aPolyline.setPoints(pList);
            aPolyline.setValue(v);
            aPolyline.setExtent(MIMath.getPointsExtent(pList));

            switch (ls.getLegendType()) {
                case UniqueValue:
                    for (int j = 0; j < ls.getBreakNum(); j++) {
                        ColorBreak cb = ls.getLegendBreaks().get(j);
                        if (MIMath.doubleEquals(v, Double.parseDouble(cb.getStartValue().toString()))) {
                            cbb = cb;
                            break;
                        }
                    }
                    break;
                case GraduatedColor:
                    int blNum = 0;
                    for (int j = 0; j < ls.getBreakNum(); j++) {
                        ColorBreak cb = ls.getLegendBreaks().get(j);
                        blNum += 1;
                        if (MIMath.doubleEquals(v, Double.parseDouble(cb.getStartValue().toString()))
                                || (v > Double.parseDouble(cb.getStartValue().toString())
                                && v < Double.parseDouble(cb.getEndValue().toString()))
                                || (blNum == ls.getBreakNum() && v == Double.parseDouble(cb.getEndValue().toString()))) {
                            cbb = cb;
                            break;
                        }
                    }
                    break;
            }
            graphics.add(new Graphic(aPolyline, cbb));
        }
        graphics.setSingleLegend(false);
        graphics.setLegendScheme(ls);

        return graphics;
    }

    /**
     * Create contour polygons
     *
     * @param gridData Grid data
     * @param ls Legend scheme
     * @param isSmooth Is smooth or not
     * @return Contour polygons
     */
    public static GraphicCollection createContourPolygons(GridData gridData, LegendScheme ls, boolean isSmooth) {
        ls = ls.convertTo(ShapeTypes.Polygon);
        Object[] ccs = LegendManage.getContoursAndColors(ls);
        double[] cValues = (double[]) ccs[0];

        double minData;
        double maxData;
        double[] maxmin = new double[2];
        gridData.getMaxMinValue(maxmin);
        maxData = maxmin[0];
        minData = maxmin[1];

        int[][] S1 = new int[gridData.data.length][gridData.data[0].length];
        Object[] cbs = ContourDraw.tracingContourLines(gridData.data,
                cValues, gridData.xArray, gridData.yArray, gridData.missingValue, S1);
        List<wContour.Global.PolyLine> contourLines = (List<wContour.Global.PolyLine>) cbs[0];
        List<wContour.Global.Border> borders = (List<wContour.Global.Border>) cbs[1];

        if (isSmooth) {
            contourLines = wContour.Contour.smoothLines(contourLines);
        }
        List<wContour.Global.Polygon> contourPolygons = ContourDraw.tracingPolygons(gridData.data, contourLines, borders, cValues);

        double v;
        ColorBreak cbb = ls.getLegenBreak(0);
        GraphicCollection graphics = new GraphicCollection();
        for (int i = 0; i < contourPolygons.size(); i++) {
            wContour.Global.Polygon poly = contourPolygons.get(i);
            v = poly.LowValue;
            PointD aPoint;
            List<PointD> pList = new ArrayList<>();
            for (wContour.Global.PointD pointList : poly.OutLine.PointList) {
                aPoint = new PointD();
                aPoint.X = pointList.X;
                aPoint.Y = pointList.Y;
                pList.add(aPoint);
            }
            if (!GeoComputation.isClockwise(pList)) {
                Collections.reverse(pList);
            }
            PolygonShape aPolygonShape = new PolygonShape();
            aPolygonShape.setPoints(pList);
            aPolygonShape.setExtent(MIMath.getPointsExtent(pList));
            aPolygonShape.lowValue = v;
            if (poly.HasHoles()) {
                for (PolyLine holeLine : poly.HoleLines) {
                    pList = new ArrayList<>();
                    for (wContour.Global.PointD pointList : holeLine.PointList) {
                        aPoint = new PointD();
                        aPoint.X = pointList.X;
                        aPoint.Y = pointList.Y;
                        pList.add(aPoint);
                    }
                    aPolygonShape.addHole(pList, 0);
                }
            }
            int valueIdx = Arrays.binarySearch(cValues, v);
            //valueIdx = Arrays.asList(cValues).indexOf(aValue);            
            if (valueIdx == cValues.length - 1) {
                aPolygonShape.highValue = maxData;
            } else {
                aPolygonShape.highValue = cValues[valueIdx + 1];
            }
//            if (!aPolygon.IsBorder) {
//                if (!aPolygon.IsHighCenter) {
//                    aPolygonShape.highValue = aValue;
//                    if (valueIdx == 0) {
//                        aPolygonShape.lowValue = minData;
//                    } else {
//                        aPolygonShape.lowValue = cValues[valueIdx - 1];
//                    }
//                }
//            }
            if (!poly.IsHighCenter && poly.HighValue == poly.LowValue) {
                aPolygonShape.highValue = v;
                if (valueIdx == 0) {
                    aPolygonShape.lowValue = minData;
                } else {
                    aPolygonShape.lowValue = cValues[valueIdx - 1];
                }
            }

            v = aPolygonShape.lowValue;
            switch (ls.getLegendType()) {
                case UniqueValue:
                    for (int j = 0; j < ls.getBreakNum(); j++) {
                        ColorBreak cb = ls.getLegendBreaks().get(j);
                        if (MIMath.doubleEquals(v, Double.parseDouble(cb.getStartValue().toString()))) {
                            cbb = cb;
                            break;
                        }
                    }
                    break;
                case GraduatedColor:
                    int blNum = 0;
                    for (int j = 0; j < ls.getBreakNum(); j++) {
                        ColorBreak cb = ls.getLegendBreaks().get(j);
                        blNum += 1;
                        if (MIMath.doubleEquals(v, Double.parseDouble(cb.getStartValue().toString()))
                                || (v > Double.parseDouble(cb.getStartValue().toString())
                                && v < Double.parseDouble(cb.getEndValue().toString()))
                                || (blNum == ls.getBreakNum() && v == Double.parseDouble(cb.getEndValue().toString()))) {
                            cbb = cb;
                            break;
                        }
                    }
                    break;
            }
            graphics.add(new Graphic(aPolygonShape, cbb));
        }
        graphics.setSingleLegend(false);
        graphics.setLegendScheme(ls);

        return graphics;
    }

    /**
     * Create fill between polygons
     *
     * @param xdata X data array
     * @param y1data Y1 data array
     * @param y2data Y2 data array
     * @param where Where data array
     * @param pb Polygon break
     * @return GraphicCollection
     */
    public static GraphicCollection createFillBetweenPolygons(Array xdata, Array y1data,
            Array y2data, Array where, PolygonBreak pb) {
        GraphicCollection gc = new GraphicCollection();
        int len = (int) xdata.getSize();
        if (where == null) {
            PolygonShape pgs = new PolygonShape();
            List<PointD> points = new ArrayList<>();
            for (int i = 0; i < len; i++) {
                points.add(new PointD(xdata.getDouble(i), y1data.getDouble(i)));
            }
            for (int i = len - 1; i >= 0; i--) {
                points.add(new PointD(xdata.getDouble(i), y2data.getDouble(i)));
            }
            pgs.setPoints(points);
            Graphic graphic = new Graphic(pgs, pb);
            gc.add(graphic);
        } else {
            boolean ob = false;
            List<List<Integer>> idxs = new ArrayList<>();
            List<Integer> idx = new ArrayList<>();
            for (int j = 0; j < len; j++) {
                if (where.getInt(j) == 1) {
                    if (!ob) {
                        idx = new ArrayList<>();
                    }
                    idx.add(j);
                } else if (ob) {
                    idxs.add(idx);
                }
                ob = where.getInt(j) == 1;
            }
            for (List<Integer> index : idxs) {
                int nn = index.size();
                if (nn >= 2) {
                    PolygonShape pgs = new PolygonShape();
                    List<PointD> points = new ArrayList<>();
                    int ii;
                    for (int j = 0; j < nn; j++) {
                        ii = index.get(j);
                        points.add(new PointD(xdata.getDouble(ii), y1data.getDouble(ii)));
                    }
                    for (int j = 0; j < nn; j++) {
                        ii = index.get(nn - j - 1);
                        points.add(new PointD(xdata.getDouble(ii), y2data.getDouble(ii)));
                    }
                    pgs.setPoints(points);
                    Graphic graphic = new Graphic(pgs, pb);
                    gc.add(graphic);
                }
            }
        }

        return gc;
    }

    /**
     * Create wind barbs
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param udata U/WindDirection data array
     * @param vdata V/WindSpeed data array
     * @param cdata Colored data array
     * @param ls Legend scheme
     * @param isUV Is U/V or not
     * @return GraphicCollection
     */
    public static GraphicCollection createBarbs(Array xdata, Array ydata, Array udata, Array vdata,
            Array cdata, LegendScheme ls, boolean isUV) {
        GraphicCollection gc = new GraphicCollection();
        Array windDirData;
        Array windSpeedData;
        if (isUV) {
            Array[] wwData = ArrayMath.uv2ds(udata, vdata);
            windDirData = wwData[0];
            windSpeedData = wwData[1];
        } else {
            windDirData = udata;
            windSpeedData = vdata;
        }

        ShapeTypes sts = ls.getShapeType();
        ls = ls.convertTo(ShapeTypes.Point);
        if (sts != ShapeTypes.Point) {
            for (int i = 0; i < ls.getBreakNum(); i++) {
                ((PointBreak) ls.getLegendBreaks().get(i)).setSize(10);
            }
        }

        int i, j;
        WindBarb aWB;
        double windDir, windSpeed;
        PointD aPoint;
        ColorBreak cb;
        double v;
        int dn = (int) xdata.getSize();
        for (i = 0; i < dn; i++) {
            windDir = windDirData.getDouble(i);
            windSpeed = windSpeedData.getDouble(i);
            if (!Double.isNaN(windDir)) {
                if (!Double.isNaN(windSpeed)) {
                    aPoint = new PointD();
                    aPoint.X = xdata.getDouble(i);
                    aPoint.Y = ydata.getDouble(i);
                    aWB = Draw.calWindBarb((float) windDir, (float) windSpeed, 0, 10, aPoint);
                    if (cdata == null) {
                        cb = ls.getLegendBreaks().get(0);
                    } else {
                        v = cdata.getDouble(i);
                        aWB.setValue(v);
                        cb = ls.getLegenBreak(v);
                    }
                    Graphic graphic = new Graphic(aWB, cb);
                    gc.add(graphic);
                }
            }
        }

        gc.setLegendScheme(ls);
        if (cdata != null) {
            gc.setSingleLegend(false);
        }

        return gc;
    }

    /**
     * Create wind arrows
     *
     * @param xdata X data array
     * @param ydata Y data array
     * @param udata U/WindDirection data array
     * @param vdata V/WindSpeed data array
     * @param cdata Colored data array
     * @param ls Legend scheme
     * @param isUV Is U/V or not
     * @return GraphicCollection
     */
    public static GraphicCollection createArrows(Array xdata, Array ydata, Array udata, Array vdata,
            Array cdata, LegendScheme ls, boolean isUV) {
        GraphicCollection gc = new GraphicCollection();
        Array windDirData;
        Array windSpeedData;
        if (isUV) {
            Array[] wwData = ArrayMath.uv2ds(udata, vdata);
            windDirData = wwData[0];
            windSpeedData = wwData[1];
        } else {
            windDirData = udata;
            windSpeedData = vdata;
        }

        ShapeTypes sts = ls.getShapeType();
        ls = ls.convertTo(ShapeTypes.Point);
        if (sts != ShapeTypes.Point) {
            for (int i = 0; i < ls.getBreakNum(); i++) {
                ((PointBreak) ls.getLegendBreaks().get(i)).setSize(10);
            }
        }

        int i, j;
        WindArrow wa;
        double windDir, windSpeed;
        PointD aPoint;
        ColorBreak cb;
        double v;
        int dn = (int) xdata.getSize();
        float size = 6;
        for (i = 0; i < dn; i++) {
            windDir = windDirData.getDouble(i);
            windSpeed = windSpeedData.getDouble(i);
            if (!Double.isNaN(windDir)) {
                if (!Double.isNaN(windSpeed)) {
                    aPoint = new PointD();
                    aPoint.X = xdata.getDouble(i);
                    aPoint.Y = ydata.getDouble(i);
                    wa = new WindArrow();
                    wa.angle = windDir;
                    wa.length = (float) windSpeed;
                    wa.size = size;
                    wa.setPoint(aPoint);
                    if (cdata == null) {
                        cb = ls.getLegendBreaks().get(0);
                    } else {
                        v = cdata.getDouble(i);
                        wa.setValue(v);
                        cb = ls.getLegenBreak(v);
                    }
                    Graphic graphic = new Graphic(wa, cb);
                    gc.add(graphic);
                }
            }
        }

        gc.setLegendScheme(ls);
        if (cdata != null) {
            gc.setSingleLegend(false);
        }

        return gc;
    }

    /**
     * Create pie arc polygons
     *
     * @param xdata X data array
     * @param colors Colors
     * @param labels Labels
     * @param startAngle Start angle
     * @param explode Explode
     * @param labelFont Label font
     * @param labelColor Label color
     * @param autopct
     * @return GraphicCollection
     */
    public static GraphicCollection createPieArcs(Array xdata, List<Color> colors,
            List<String> labels, float startAngle, List<Number> explode, Font labelFont,
            Color labelColor, String autopct) {
        GraphicCollection gc = new GraphicCollection();
        double sum = ArrayMath.sumDouble(xdata);
        double v;
        int n = (int) xdata.getSize();
        float sweepAngle;
        NumberFormat nf = NumberFormat.getPercentInstance();
        // nf.setMinimumFractionDigits(2);
        for (int i = 0; i < n; i++) {
            v = xdata.getDouble(i);
            if (sum > 1) {
                v = v / sum;
            }
            sweepAngle = (float) (360.0 * v);
            ArcShape aShape = new ArcShape();
            aShape.setStartAngle(startAngle);
            aShape.setSweepAngle(sweepAngle);
            List<PointD> points = new ArrayList<>();
            points.add(new PointD(0, 0));
            points.add(new PointD(0, 1));
            points.add(new PointD(1, 1));
            points.add(new PointD(1, 0));
            points.add(new PointD(0, 0));
            aShape.setPoints(points);
            if (explode != null) {
                aShape.setExplode(explode.get(i).floatValue());
            }
            PolygonBreak pgb = new PolygonBreak();
            pgb.setColor(colors.get(i));
            if (labels == null) {
                if (autopct == null) {
                    pgb.setCaption(nf.format(v));
                } else {
                    pgb.setCaption(String.format(autopct, v * 100));
                }
            } else {
                pgb.setCaption(labels.get(i));
            }
            Graphic graphic = new Graphic(aShape, pgb);
            gc.add(graphic);
            startAngle += sweepAngle;
        }
        gc.setSingleLegend(false);
        gc.getLabelSet().setLabelFont(labelFont);
        gc.getLabelSet().setLabelColor(labelColor);

        return gc;
    }

    /**
     * Create box graphics
     *
     * @param xdata X data array list
     * @param positions Box position list
     * @param widths Box width list
     * @param showcaps Show caps or not
     * @param showfliers Show fliers or not
     * @param showmeans Show means or not
     * @param boxBreak Box polygon break
     * @param medianBreak Meandian line break
     * @param whiskerBreak Whisker line break
     * @param capBreak Whisker cap line break
     * @param meanBreak Mean point break
     * @param flierBreak Flier point break
     * @return GraphicCollection
     */
    public static GraphicCollection createBox(List<Array> xdata, List<Number> positions, List<Number> widths,
            boolean showcaps, boolean showfliers, boolean showmeans, PolygonBreak boxBreak,
            PolylineBreak medianBreak, PolylineBreak whiskerBreak, PolylineBreak capBreak,
            ColorBreak meanBreak, PointBreak flierBreak) {
        GraphicCollection gc = new GraphicCollection();
        int n = xdata.size();
        if (positions == null) {
            positions = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                positions.add(i + 1);
            }
        }
        if (widths == null) {
            widths = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                widths.add(0.5);
            }
        }
        double v, width;
        if (boxBreak == null) {
            boxBreak = new PolygonBreak();
            boxBreak.setDrawFill(false);
            boxBreak.setOutlineColor(Color.blue);
        }
        if (medianBreak == null) {
            medianBreak = new PolylineBreak();
            medianBreak.setColor(Color.red);
        }
        if (whiskerBreak == null) {
            whiskerBreak = new PolylineBreak();
            whiskerBreak.setColor(Color.black);
            whiskerBreak.setStyle(LineStyles.Dash);
        }
        if (capBreak == null) {
            capBreak = new PolylineBreak();
            capBreak.setColor(Color.black);
        }
        if (flierBreak == null) {
            flierBreak = new PointBreak();
            flierBreak.setStyle(PointStyle.Plus);
        }
        if (meanBreak == null) {
            meanBreak = new PointBreak();
            ((PointBreak) meanBreak).setStyle(PointStyle.Square);
            ((PointBreak) meanBreak).setColor(Color.red);
            ((PointBreak) meanBreak).setOutlineColor(Color.black);
        }

        for (int i = 0; i < n; i++) {
            Array a = xdata.get(i);
            v = positions.get(i).doubleValue();
            width = widths.get(i).doubleValue();
            //Add box polygon
            double q1 = Statistics.quantile(a, 1);
            double q3 = Statistics.quantile(a, 3);
            double median = Statistics.quantile(a, 2);
            double mind = ArrayMath.getMinimum(a);
            double maxd = ArrayMath.getMaximum(a);
            double mino = q1 - (q3 - q1) * 1.5;
            double maxo = q3 + (q3 - q1) * 1.5;
            List<PointD> pList = new ArrayList<>();
            pList.add(new PointD(v - width * 0.5, q1));
            pList.add(new PointD(v - width * 0.5, q3));
            pList.add(new PointD(v + width * 0.5, q3));
            pList.add(new PointD(v + width * 0.5, q1));
            pList.add(new PointD(v - width * 0.5, q1));
            PolygonShape pgs = new PolygonShape();
            pgs.setPoints(pList);
            gc.add(new Graphic(pgs, boxBreak));

            //Add meadian line
            pList = new ArrayList<>();
            pList.add(new PointD(v - width * 0.5, median));
            pList.add(new PointD(v + width * 0.5, median));
            PolylineShape pls = new PolylineShape();
            pls.setPoints(pList);
            gc.add(new Graphic(pls, medianBreak));

            //Add low whisker line
            double min = Math.max(mino, mind);
            pList = new ArrayList<>();
            pList.add(new PointD(v, q1));
            pList.add(new PointD(v, min));
            pls = new PolylineShape();
            pls.setPoints(pList);
            gc.add(new Graphic(pls, whiskerBreak));
            //Add cap
            if (showcaps) {
                pList = new ArrayList<>();
                pList.add(new PointD(v - width * 0.25, min));
                pList.add(new PointD(v + width * 0.25, min));
                pls = new PolylineShape();
                pls.setPoints(pList);
                gc.add(new Graphic(pls, capBreak));
            }
            //Add low fliers
            if (showfliers) {
                if (mino > mind) {
                    for (int j = 0; j < a.getSize(); j++) {
                        if (a.getDouble(j) < mino) {
                            PointShape ps = new PointShape();
                            ps.setPoint(new PointD(v, a.getDouble(j)));
                            gc.add(new Graphic(ps, flierBreak));
                        }
                    }
                }
            }

            //Add high whisker line
            double max = Math.min(maxo, maxd);
            pList = new ArrayList<>();
            pList.add(new PointD(v, q3));
            pList.add(new PointD(v, max));
            pls = new PolylineShape();
            pls.setPoints(pList);
            gc.add(new Graphic(pls, whiskerBreak));
            //Add cap
            if (showcaps) {
                pList = new ArrayList<>();
                pList.add(new PointD(v - width * 0.25, max));
                pList.add(new PointD(v + width * 0.25, max));
                pls = new PolylineShape();
                pls.setPoints(pList);
                gc.add(new Graphic(pls, capBreak));
            }
            //Add high fliers
            if (showfliers) {
                if (maxo < maxd) {
                    for (int j = 0; j < a.getSize(); j++) {
                        if (a.getDouble(j) > maxo) {
                            PointShape ps = new PointShape();
                            ps.setPoint(new PointD(v, a.getDouble(j)));
                            gc.add(new Graphic(ps, flierBreak));
                        }
                    }
                }
            }

            //Add mean line
            if (showmeans) {
                double mean = ArrayMath.mean(a);
                PointShape ps = new PointShape();
                ps.setPoint(new PointD(v, mean));
                gc.add(new Graphic(ps, meanBreak));
            }
        }
        gc.setSingleLegend(false);

        return gc;
    }

    /**
     * Convert graphics from polar to cartesian coordinate
     *
     * @param graphics Graphics
     */
    public static void polarToCartesian(GraphicCollection graphics) {
        for (int m = 0; m < graphics.getNumGrahics(); m++) {
            Graphic graphic = graphics.get(m);
            for (int i = 0; i < graphic.getNumGrahics(); i++) {
                Graphic gg = graphic.getGraphicN(i);
                Shape shape = gg.getShape();
                List<PointD> points = new ArrayList<>();
                for (PointD p : shape.getPoints()) {
                    double[] xy = MIMath.polarToCartesian(p.X, p.Y);
                    points.add(new PointD(xy[0], xy[1]));
                }
                shape.setPoints(points);
            }
        }
        graphics.updateExtent();
    }

}
