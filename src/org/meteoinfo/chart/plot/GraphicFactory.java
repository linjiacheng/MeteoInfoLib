/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.plot;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.data.GridArray;
import org.meteoinfo.data.GridData;
import org.meteoinfo.drawing.ContourDraw;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import org.meteoinfo.legend.BarBreak;
import org.meteoinfo.legend.ColorBreak;
import org.meteoinfo.legend.LegendManage;
import org.meteoinfo.legend.LegendScheme;
import org.meteoinfo.legend.LegendType;
import org.meteoinfo.legend.PolylineBreak;
import org.meteoinfo.shape.BarShape;
import org.meteoinfo.shape.Graphic;
import org.meteoinfo.shape.GraphicCollection;
import org.meteoinfo.shape.ImageShape;
import org.meteoinfo.shape.PointShape;
import org.meteoinfo.shape.PolylineShape;
import org.meteoinfo.shape.Shape;
import org.meteoinfo.shape.ShapeTypes;
import ucar.ma2.Array;

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
    public static GraphicCollection createBars(Array xdata, Array ydata, boolean autoWidth,
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
            aPolyline.value = v;
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

        return graphics;
    }
}
