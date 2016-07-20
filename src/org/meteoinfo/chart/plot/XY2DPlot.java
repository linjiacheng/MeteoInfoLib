/* Copyright 2016 - Yaqiang Wang,
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
package org.meteoinfo.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.chart.ChartLegend;
import org.meteoinfo.chart.axis.TimeAxis;
import org.meteoinfo.data.Dataset;
import org.meteoinfo.drawing.Draw;
import static org.meteoinfo.drawing.Draw.getHatchImage;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import org.meteoinfo.global.PointF;
import org.meteoinfo.legend.BarBreak;
import org.meteoinfo.legend.ColorBreak;
import org.meteoinfo.legend.LegendScheme;
import org.meteoinfo.legend.PointBreak;
import org.meteoinfo.legend.PolygonBreak;
import org.meteoinfo.legend.PolylineBreak;
import org.meteoinfo.shape.BarShape;
import org.meteoinfo.shape.Graphic;
import org.meteoinfo.shape.GraphicCollection;
import org.meteoinfo.shape.ImageShape;
import org.meteoinfo.shape.PointShape;
import org.meteoinfo.shape.Polygon;
import org.meteoinfo.shape.PolygonShape;
import org.meteoinfo.shape.Polyline;
import org.meteoinfo.shape.PolylineShape;
import org.meteoinfo.shape.Shape;
import org.meteoinfo.shape.ShapeTypes;

/**
 *
 * @author Yaqiang Wang
 */
public class XY2DPlot extends XYPlot {

    // <editor-fold desc="Variables">
    private GraphicCollection graphics;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    public XY2DPlot() {
        super();
        this.graphics = new GraphicCollection();
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get graphics
     *
     * @return Graphics
     */
    public GraphicCollection getGraphics() {
        return this.graphics;
    }

    /**
     * Set graphics
     *
     * @param value Graphics
     */
    public void setGraphics(GraphicCollection value) {
        this.graphics = value;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Add a graphic
     *
     * @param g Grahic
     */
    public void addGraphic(Graphic g) {
        this.graphics.add(g);
    }

    /**
     * Add graphic list
     *
     * @param gs Graphic list
     */
    public void addGraphics(List<Graphic> gs) {
        this.graphics.addAll(gs);
    }

    @Override
    void drawGraph(Graphics2D g, Rectangle2D area) {
        AffineTransform oldMatrix = g.getTransform();
        Rectangle oldRegion = g.getClipBounds();
        g.setClip(area);
        g.translate(area.getX(), area.getY());

        //Draw background
        if (this.isDrawBackground()) {
            g.setColor(this.getBackground());
            g.fill(new Rectangle2D.Double(0, 0, area.getWidth(), area.getHeight()));
        }

        int barIdx = 0;
        for (Graphic graphic : this.graphics.getGraphics()) {
            ColorBreak cb = graphic.getLegend();
            if (graphic.getGraphicN(0).getShape().getShapeType() == ShapeTypes.Bar) {
                this.drawBars(g, (GraphicCollection) graphic, barIdx, area);
                barIdx += 1;
            }
            for (int i = 0; i < graphic.getNumGrahics(); i++) {
                Graphic gg = graphic.getGraphicN(i);
                if (!graphic.isSingleLegend()) {
                    cb = gg.getLegend();
                }
                Shape shape = gg.getShape();
                switch (shape.getShapeType()) {
                    case Point:
                    case PointM:
                    case PointZ:
                        this.drawPoint(g, (PointShape) shape, (PointBreak) cb, area);
                        break;
                    case Polyline:
                    case PolylineZ:
                        if (cb instanceof PointBreak) {
                            this.drawPolyline(g, (PolylineShape) shape, (PointBreak) cb, area);
                        } else {
                            this.drawPolyline(g, (PolylineShape) shape, (PolylineBreak) cb, area);
                        }
                        break;
                    case Polygon:
                    case PolygonZ:
                        for (Polygon poly : ((PolygonShape) shape).getPolygons()) {
                            drawPolygon(g, poly, (PolygonBreak) cb, false, area);
                        }
                        break;
                    case Image:
                        this.drawImage(g, gg, area);
                        break;
                }
            }
        }

        g.setTransform(oldMatrix);
        g.setClip(oldRegion);
    }

    private void drawPoint(Graphics2D g, PointShape aPS, PointBreak aPB, Rectangle2D area) {
        PointD p = aPS.getPoint();
        double[] sXY = projToScreen(p.X, p.Y, area);
        PointF pf = new PointF((float) sXY[0], (float) sXY[1]);
        Draw.drawPoint(pf, aPB, g);
    }

    private void drawPolyline(Graphics2D g, PolylineShape aPLS, PointBreak aPB, Rectangle2D area) {
        for (Polyline aline : aPLS.getPolylines()) {
            double[] sXY;
            PointF p;
            for (int i = 0; i < aline.getPointList().size(); i++) {
                PointD wPoint = aline.getPointList().get(i);
                sXY = projToScreen(wPoint.X, wPoint.Y, area);
                p = new PointF((float) sXY[0], (float) sXY[1]);
                Draw.drawPoint(p, aPB, g);
            }
        }
    }

    private void drawPolyline(Graphics2D g, PolylineShape aPLS, PolylineBreak aPLB, Rectangle2D area) {
        for (Polyline aline : aPLS.getPolylines()) {
            double[] sXY;
            PointF[] points = new PointF[aline.getPointList().size()];
            for (int i = 0; i < aline.getPointList().size(); i++) {
                PointD wPoint = aline.getPointList().get(i);
                sXY = projToScreen(wPoint.X, wPoint.Y, area);
                points[i] = new PointF((float) sXY[0], (float) sXY[1]);
            }
            Draw.drawPolyline(points, aPLB, g);
        }
    }

    private List<PointF> drawPolygon(Graphics2D g, Polygon aPG, PolygonBreak aPGB,
            boolean isSelected, Rectangle2D area) {
        int len = aPG.getOutLine().size();
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, len);
        PointD wPoint;
        double[] sXY;
        List<PointF> rPoints = new ArrayList<>();
        for (int i = 0; i < aPG.getOutLine().size(); i++) {
            wPoint = aPG.getOutLine().get(i);
            sXY = projToScreen(wPoint.X, wPoint.Y, area);
            if (i == 0) {
                path.moveTo(sXY[0], sXY[1]);
            } else {
                path.lineTo(sXY[0], sXY[1]);
            }
            rPoints.add(new PointF((float) sXY[0], (float) sXY[1]));
        }

        List<PointD> newPList;
        if (aPG.hasHole()) {
            for (int h = 0; h < aPG.getHoleLines().size(); h++) {
                newPList = aPG.getHoleLines().get(h);
                for (int j = 0; j < newPList.size(); j++) {
                    wPoint = newPList.get(j);
                    sXY = projToScreen(wPoint.X, wPoint.Y, area);
                    if (j == 0) {
                        path.moveTo(sXY[0], sXY[1]);
                    } else {
                        path.lineTo(sXY[0], sXY[1]);
                    }
                }
            }
        }
        path.closePath();

        if (aPGB.isDrawFill()) {
            //int alpha = (int)((1 - (double)transparencyPerc / 100.0) * 255);
            //Color aColor = Color.FromArgb(alpha, aPGB.Color);
            Color aColor = aPGB.getColor();
            if (isSelected) {
                aColor = this.getSelectedColor();
            }
            if (aPGB.isUsingHatchStyle()) {
                int size = aPGB.getStyleSize();
                BufferedImage bi = getHatchImage(aPGB.getStyle(), size, aPGB.getColor(), aPGB.getBackColor());
                Rectangle2D rect = new Rectangle2D.Double(0, 0, size, size);
                g.setPaint(new TexturePaint(bi, rect));
                g.fill(path);
            } else {
                g.setColor(aColor);
                g.fill(path);
            }
        } else if (isSelected) {
            g.setColor(this.getSelectedColor());
            g.fill(path);
        }

        if (aPGB.isDrawOutline()) {
            BasicStroke pen = new BasicStroke(aPGB.getOutlineSize());
            g.setStroke(pen);
            g.setColor(aPGB.getOutlineColor());
            g.draw(path);
        }

        return rPoints;
    }

    private void drawBar(Graphics2D g, BarShape bar, BarBreak bb, float width, Rectangle2D area) {
        double[] xy;
        xy = this.projToScreen(0, 0, area);
        float y0 = (float) xy[1];
        width = (float) this.projXLength(width, area);
        xy = projToScreen(bar.getPoint().X, bar.getPoint().Y, area);
        double x = xy[0];
        double y = xy[1];
        float height;
        height = Math.abs((float) (y - y0));
        float yb = y0;
        if (y >= y0) {
            yb += height;
        }
        Draw.drawBar(new PointF((float) x, yb), width, height, bb, g, false, 5);
    }

    private int getBarSeriesNum() {
        int n = 0;
        for (Graphic g : this.graphics.getGraphics()) {
            if (g.getGraphicN(0).getShape().getShapeType() == ShapeTypes.Bar) {
                n += 1;
            }
        }
        return n;
    }

    private void drawBars(Graphics2D g, GraphicCollection bars, int barIdx, Rectangle2D area) {
        double[] xy;
        xy = this.projToScreen(0, 0, area);
        float y0 = (float) xy[1];
        int len = bars.getNumGrahics();
        PointF[] points = new PointF[len];
        for (int i = 0; i < len; i++) {
            BarShape bs = (BarShape) bars.getGraphicN(i).getShape();
            xy = this.projToScreen(bs.getPoint().X, bs.getPoint().Y, area);
            points[i] = new PointF((float) xy[0], (float) xy[1]);
        }
        float width;
        int barSeriesN = this.getBarSeriesNum();
        BarShape bs1 = (BarShape) bars.getGraphicN(0).getShape();
        if (bs1.isAutoWidth()) {
            if (len > 1) {
                width = (float) ((points[1].X - points[0].X) * 0.5) / barSeriesN;
            } else {
                width = (float) (area.getWidth() / 10) / barSeriesN;
            }
            float height;
            BarBreak bb;
            for (int i = 0; i < len; i++) {
                BarShape bs = (BarShape) bars.getGraphicN(i).getShape();
                bb = (BarBreak) bars.getGraphicN(i).getLegend();
                height = Math.abs((float) (points[i].Y - y0));
                float yBottom = y0;
                if (bs.isDrawBottom()) {
                    xy = this.projToScreen(bs.getPoint().X, bs.getBottom(), area);
                    yBottom = (float) xy[1];
                }
                float yb = yBottom;
                if (points[i].Y >= y0) {
                    yb += height;
                }
                Draw.drawBar(new PointF(points[i].X - width * barSeriesN / 2
                        + barIdx * width, yb), width, height, bb, g, false, 5);
                if (bs.isDrawError()) {
                    PointF p = (PointF) points[i].clone();
                    p.Y -= y0 - yBottom;
                    double elen = 6;
                    double error = bs.getError();
                    error = this.projYLength(error, area);
                    double x = p.X - width * barSeriesN / 2
                            + barIdx * width + width / 2;
                    g.setColor(bb.getErrorColor());
                    g.draw(new Line2D.Double(x, p.Y - error, x, p.Y + error));
                    g.draw(new Line2D.Double(x - (elen * 0.5), p.Y - error, x + (elen * 0.5), p.Y - error));
                    g.draw(new Line2D.Double(x - (elen * 0.5), p.Y + error, x + (elen * 0.5), p.Y + error));
                }
            }
        } else {
            width = (float) this.projXLength(bs1.getWidth(), area);
            float height;
            BarBreak bb;
            for (int i = 0; i < len; i++) {
                BarShape bs = (BarShape) bars.getGraphicN(i).getShape();
                bb = (BarBreak) bars.getGraphicN(i).getLegend();
                height = Math.abs((float) (points[i].Y - y0));
                float yBottom = y0;
                if (bs.isDrawBottom()) {
                    xy = this.projToScreen(bs.getPoint().X, bs.getBottom(), area);
                    yBottom = (float) xy[1];
                }
                float yb = yBottom;
                if (points[i].Y >= y0) {
                    yb += height;
                }
                Draw.drawBar(new PointF(points[i].X, yb), width, height, bb, g, false, 5);
                if (bs.isDrawError()) {
                    PointF p = (PointF) points[i].clone();
                    p.Y -= y0 - yBottom;
                    double elen = 6;
                    double error = bs.getError();
                    error = this.projYLength(error, area);
                    double x = p.X + width / 2;
                    g.setColor(bb.getErrorColor());
                    g.draw(new Line2D.Double(x, p.Y - error, x, p.Y + error));
                    g.draw(new Line2D.Double(x - (elen * 0.5), p.Y - error, x + (elen * 0.5), p.Y - error));
                    g.draw(new Line2D.Double(x - (elen * 0.5), p.Y + error, x + (elen * 0.5), p.Y + error));
                }
            }
        }

        //Draw baseline
        boolean drawBaseline = true;
        if (drawBaseline) {
            g.setColor(Color.black);
            g.draw(new Line2D.Double(0, y0, area.getWidth(), y0));
        }
    }

    private double getBarXInterval(int idx) {
        Graphic gg = this.graphics.get(idx);
        if (gg.getNumGrahics() == 1) {
            if (gg.getGraphicN(0).getShape().getPoints().get(0).X == 0) {
                return 1;
            } else {
                return gg.getGraphicN(0).getShape().getPoints().get(0).X / 10;
            }
        } else {
            return gg.getGraphicN(1).getShape().getPoints().get(0).X
                    - gg.getGraphicN(0).getShape().getPoints().get(0).X;
        }
    }

    private int getBarIndex() {
        int idx = -1;
        for (int i = 0; i < this.graphics.size(); i++) {
            if (this.graphics.get(i).getGraphicN(0).getShape().getShapeType() == ShapeTypes.Bar) {
                idx = i;
                break;
            }
        }
        return idx;
    }

    private void drawImage(Graphics2D g, Graphic igraphic, Rectangle2D area) {
        ImageShape ishape = (ImageShape) igraphic.getShape();
        BufferedImage image = ishape.getImage();
        double sx = ishape.getPoint().X, sy = ishape.getPoint().Y + image.getHeight();
        double[] xy1 = this.projToScreen(sx, sy, area);
        double[] xy2 = this.projToScreen(sx + image.getWidth(), ishape.getPoint().Y, area);
        int x = (int) xy1[0];
        int y = (int) xy1[1];
        int width = (int) (xy2[0] - xy1[0]);
        int height = (int) (xy2[1] - xy1[1]);
        g.drawImage(image, x, y, width, height, null);
    }

    @Override
    Extent getAutoExtent() {
        Extent extent = this.graphics.getExtent();
        if (extent.minX == extent.maxX) {
            extent.minX = extent.minX - Math.abs(extent.minX);
            extent.maxX = extent.maxX + Math.abs(extent.minX);
        }
        if (extent.minY == extent.maxY) {
            extent.minY = extent.minY - Math.abs(extent.minY);
            extent.maxY = extent.maxY + Math.abs(extent.maxY);
        }

        int barIdx = this.getBarIndex();
        if (barIdx >= 0) {
            double dx = getBarXInterval(barIdx);
            extent.minX -= dx;
            extent.maxX += dx;
        }
        double[] xValues;
        if (this.getXAxis() instanceof TimeAxis) {
            //if (this.getXAxis().isTimeAxis()) {
            xValues = (double[]) MIMath.getIntervalValues(extent.minX, extent.maxX, false).get(0);
            xValues[0] = extent.minX;
            xValues[xValues.length - 1] = extent.maxX;
        } else {
            xValues = (double[]) MIMath.getIntervalValues(extent.minX, extent.maxX, true).get(0);
        }
        double[] yValues = (double[]) MIMath.getIntervalValues(extent.minY, extent.maxY, true).get(0);
        if (this.getPlotOrientation() == PlotOrientation.VERTICAL) {
            return new Extent(xValues[0], xValues[xValues.length - 1], yValues[0], yValues[yValues.length - 1]);
        } else {
            return new Extent(yValues[0], yValues[yValues.length - 1], xValues[0], xValues[xValues.length - 1]);
        }
    }

    /**
     * Set auto extent
     */
    @Override
    public void setAutoExtent() {
        Extent extent = this.getAutoExtent();
        this.setDrawExtent(extent);
        this.setExtent((Extent) extent.clone());
    }

    @Override
    public void updateLegendScheme() {
        if (this.getLegend() == null) {
            this.setLegend(new ChartLegend(this.getLegendScheme()));
        } else {
            this.getLegend().setLegendScheme(this.getLegendScheme());
        }
    }

    @Override
    public Dataset getDataset() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDataset(Dataset dataset) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Get legend scheme
     *
     * @return Legend scheme
     */
    public LegendScheme getLegendScheme() {
        ShapeTypes stype = ShapeTypes.Polyline;
        LegendScheme ls = new LegendScheme(stype);
        for (Graphic g : this.graphics.getGraphics()) {
            ls.getLegendBreaks().add(g.getLegend());
        }
        return ls;
    }
    // </editor-fold>

}
