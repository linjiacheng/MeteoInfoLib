/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.plot;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import org.meteoinfo.data.Dataset;
import org.meteoinfo.data.XYDataset;
import org.meteoinfo.drawing.Draw;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointF;
import org.meteoinfo.legend.LegendScheme;
import org.meteoinfo.legend.PointBreak;
import org.meteoinfo.legend.PolygonBreak;
import org.meteoinfo.legend.PolylineBreak;
import org.meteoinfo.shape.ShapeTypes;

/**
 *
 * @author yaqiang
 */
public final class XY1DPlot extends XYPlot {

    // <editor-fold desc="Variables">
    private XYDataset dataset;
    private ChartPlotMethod chartPlotMethod;    
    private PolylineBreak[] lineBreaks;
    private PointBreak[] pointBreaks;
    private PolygonBreak[] polygonBreaks;
    private PointBreak[][] itemPointBreaks;
    private boolean useBreak2D;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public XY1DPlot() {
        super();

        this.chartPlotMethod = ChartPlotMethod.LINE;
        this.useBreak2D = false;
    }

    /**
     * Constructor
     *
     * @param dateset Dataset
     */
    public XY1DPlot(XYDataset dateset) {
        this();
        this.setDataset(dateset);
    }

    /**
     * Constructor
     *
     * @param orientation Plot orientation
     * @param dateset Dataset
     */
    public XY1DPlot(PlotOrientation orientation, XYDataset dateset) {
        this();
        this.setPlotOrientation(orientation);
        this.setDataset(dateset);
    }

    /**
     * Constructor
     *
     * @param isTime If x axis is time
     * @param dateset Dataset
     */
    public XY1DPlot(boolean isTime, XYDataset dateset) {
        this();
        this.getXAxis().setTimeAxis(isTime);
        this.setDataset(dateset);
    }

    /**
     * Constructor
     *
     * @param isTime If x axis is time
     * @param orientation Plot orientation
     * @param dateset Dataset
     */
    public XY1DPlot(boolean isTime, PlotOrientation orientation, XYDataset dateset) {
        this();
        this.getXAxis().setTimeAxis(isTime);
        this.setPlotOrientation(orientation);
        this.setDataset(dateset);
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    @Override
    public XYDataset getDataset() {
        return dataset;
    }

    @Override
    public void setDataset(Dataset value) {
        dataset = (XYDataset) value;
        Extent extent = this.getAutoExtent();
        this.setDrawExtent(extent);
        lineBreaks = new PolylineBreak[dataset.getSeriesCount()];
        pointBreaks = new PointBreak[dataset.getSeriesCount()];
        polygonBreaks = new PolygonBreak[dataset.getSeriesCount()];
        this.itemPointBreaks = new PointBreak[dataset.getSeriesCount()][dataset.getItemCount()];
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            lineBreaks[i] = new PolylineBreak();
            lineBreaks[i].setCaption(dataset.getSeriesKey(i));
            pointBreaks[i] = new PointBreak();
            pointBreaks[i].setCaption(dataset.getSeriesKey(i));
            polygonBreaks[i] = new PolygonBreak();
            polygonBreaks[i].setCaption(dataset.getSeriesKey(i));
        }
    }
    
    /**
     * Get chart plot method
     *
     * @return Chart plot method
     */
    public ChartPlotMethod getChartPlotMethod() {
        return this.chartPlotMethod;
    }

    /**
     * Set chart plot method
     *
     * @param value Chart plot method
     */
    public void setChartPlotMethod(ChartPlotMethod value) {
        this.chartPlotMethod = value;
    }
       
    @Override
    public PlotType getPlotType() {
        return PlotType.XY;
    }

    /**
     * Get polyline breaks
     *
     * @return Polyline breaks
     */
    public PolylineBreak[] getPolylineBreaks() {
        return this.lineBreaks;
    }

    /**
     * Get point breaks
     *
     * @return Point breaks
     */
    public PointBreak[] getPointBreaks() {
        return this.pointBreaks;
    }

    /**
     * If use item 2D point breaks
     *
     * @return Boolean
     */
    public boolean isUseBreak2D() {
        return this.useBreak2D;
    }

    /**
     * Set if use item 2D point breaks
     *
     * @param value Boolean
     */
    public void setUseBeak2D(boolean value) {
        this.useBreak2D = value;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">    
    
    @Override
    void drawGraph(Graphics2D g, Rectangle2D area) {
        AffineTransform oldMatrix = g.getTransform();
        Rectangle oldRegion = g.getClipBounds();
        g.setClip(area);
        g.translate(area.getX(), area.getY());
        
        //Draw background
        if (this.isDrawBackground()){
            g.setColor(this.getBackground());
            g.fill(new Rectangle2D.Double(0, 0, area.getWidth(), area.getHeight()));
        }

        double[] xy;
        for (int i = 0; i < this.dataset.getSeriesCount(); i++) {
            int len = this.dataset.getItemCount(i);
            PointF[] points = new PointF[len];
            List<Integer> mvIdx = this.dataset.getMissingValueIndex(i);
            if (this.getPlotOrientation() == PlotOrientation.VERTICAL) {
                for (int j = 0; j < len; j++) {
                    xy = this.projToScreen(this.dataset.getX(i, j), this.dataset.getY(i, j), area);
                    points[j] = new PointF((float) xy[0], (float) xy[1]);
                }
            } else {
                for (int j = 0; j < len; j++) {
                    xy = this.projToScreen(this.dataset.getY(i, j), this.dataset.getX(i, j), area);
                    points[j] = new PointF((float) xy[0], (float) xy[1]);
                }
            }
            if (this.getYAxis().isInverse()) {
                PointF[] npoints = new PointF[len];
                PointF p;
                float y;
                for (int j = 0; j < len; j++) {
                    p = points[len - j - 1];
                    y = (float) area.getHeight() - p.Y;
                    npoints[j] = new PointF(p.X, y);
                }
                points = npoints;
            }
            if (this.getXAxis().isInverse()) {
                PointF[] npoints = new PointF[len];
                PointF p;
                float x;
                for (int j = 0; j < len; j++) {
                    p = points[len - j - 1];
                    x = (float) area.getWidth() - p.X;
                    npoints[j] = new PointF(x, p.Y);
                }
                points = npoints;
            }

            switch (this.chartPlotMethod) {
                case LINE:
                    this.lineBreaks[i].setDrawSymbol(false);
                    if (mvIdx.isEmpty()) {
                        Draw.drawPolyline(points, this.lineBreaks[i], g);
                    } else {
                        Draw.drawPolyline(points, this.lineBreaks[i], g, mvIdx);
                    }
                    break;
                case POINT:
                    if (this.useBreak2D) {
                        for (int j = 0; j < len; j++) {
                            if (!mvIdx.contains(j)) {
                                Draw.drawPoint(points[j], this.itemPointBreaks[i][j], g);
                            }
                        }
                    } else {
                        for (int j = 0; j < len; j++) {
                            if (!mvIdx.contains(j)) {
                                Draw.drawPoint(points[j], this.pointBreaks[i], g);
                            }
                        }
                    }
                    break;
                case LINE_POINT:
                    this.lineBreaks[i].setDrawSymbol(true);
                    if (mvIdx.isEmpty()) {
                        Draw.drawPolyline(points, this.lineBreaks[i], g);
                    } else {
                        Draw.drawPolyline(points, this.lineBreaks[i], g, mvIdx);
                    }
                    break;
                case BAR:
                    int width;
                    if (points.length > 1) {
                        width = (int) ((points[1].X - points[0].X) * 0.5) / this.dataset.getSeriesCount();
                    } else {
                        width = (int) (area.getWidth() / 10) / this.dataset.getSeriesCount();
                    }
                    int height;
                    for (int j = 0; j < len; j++) {
                        if (!mvIdx.contains(j)) {
                            height = (int) (area.getHeight() - points[j].Y);
                            Draw.drawBar(new PointF(points[j].X - width * this.dataset.getSeriesCount() / 2
                                    + i * width, (int) area.getHeight()), width, height, this.polygonBreaks[i], g, false, 5);
                        }
                    }
                    break;
            }
        }

        g.setTransform(oldMatrix);
        g.setClip(oldRegion);
    }

    /**
     * Get a item point break
     *
     * @param seriesIdx Series index
     * @param itemIdx Item index
     * @return Item point break;
     */
    public PointBreak getItemPointBreak(int seriesIdx, int itemIdx) {
        return this.itemPointBreaks[seriesIdx][itemIdx];
    }

    /**
     * Set item point break
     *
     * @param seriesIdx Series index
     * @param itemIdx Item index
     * @param pb Item point break
     */
    public void setItemPointBreak(int seriesIdx, int itemIdx, PointBreak pb) {
        this.itemPointBreaks[seriesIdx][itemIdx] = pb;
    }

    /**
     * Get polyline break
     *
     * @param seriesIdx Series index
     * @return Polyline break
     */
    public PolylineBreak getPolylineBreak(int seriesIdx) {
        return this.lineBreaks[seriesIdx];
    }

    /**
     * Set polyline break
     *
     * @param seriesIdx Series index
     * @param plb Polyline break
     */
    public void setPolylineBreak(int seriesIdx, PolylineBreak plb) {
        this.lineBreaks[seriesIdx] = plb;
    }

    /**
     * Get point break
     *
     * @param seriesIdx Series index
     * @return Point break
     */
    public PointBreak getPointBreak(int seriesIdx) {
        return this.pointBreaks[seriesIdx];
    }

    /**
     * Set point break
     *
     * @param seriesIdx Series index
     * @param pb Point break
     */
    public void setPointBreak(int seriesIdx, PointBreak pb) {
        this.pointBreaks[seriesIdx] = pb;
    }
    
    /**
     * Get polygon break
     * @param seriesIdx Series index
     * @return Polygon break
     */
    public PolygonBreak getPolygonBreak(int seriesIdx){
        return this.polygonBreaks[seriesIdx];
    }
    
    /**
     * Set polygon break
     * @param seriesIdx Series index
     * @param pgb Polygon break
     */
    public void setPolygonBreak(int seriesIdx, PolygonBreak pgb){
        this.polygonBreaks[seriesIdx] = pgb;
    }

    /**
     * Get auto extent
     *
     * @return Auto extent
     */
    @Override
    public Extent getAutoExtent() {
        Extent extent = dataset.getDataExtent();
        //double xgap = extent.getWidth() / Math.min(dataset.getItemCount(), 50);
        //double ygap = extent.getHeight() / Math.min(dataset.getItemCount(), 50);
        //extent = extent.extend(xgap, ygap);
        double[] xValues;
        if (this.getXAxis().isTimeAxis()) {
            xValues = MIMath.getIntervalValues(extent.minX, extent.maxX, false);
            xValues[0] = extent.minX;
            xValues[xValues.length - 1] = extent.maxX;
        } else {
            xValues = MIMath.getIntervalValues(extent.minX, extent.maxX, true);
        }
        double[] yValues = MIMath.getIntervalValues(extent.minY, extent.maxY, true);        
        if (this.getPlotOrientation() == PlotOrientation.VERTICAL) {
            return new Extent(xValues[0], xValues[xValues.length - 1], yValues[0], yValues[yValues.length - 1]);
        } else {
            return new Extent(yValues[0], yValues[yValues.length - 1], xValues[0], xValues[xValues.length - 1]);
        }
    }

    /**
     * Set auto extent
     */
    public void setAutoExtent() {
        Extent extent = this.getAutoExtent();
        this.setDrawExtent(extent);
    }
    
    /**
     * Get legend scheme
     * @return Legend scheme
     */
    public LegendScheme getLegendScheme() {
        LegendScheme ls = null;
        switch (this.chartPlotMethod){
            case LINE:
            case LINE_POINT:
                ls = new LegendScheme(ShapeTypes.Polyline);
                ls.getLegendBreaks().addAll(Arrays.asList(this.lineBreaks));
                break;
            case POINT:
                ls = new LegendScheme(ShapeTypes.Point);
                ls.getLegendBreaks().addAll(Arrays.asList(this.pointBreaks));
                break;
            case BAR:
                ls = new LegendScheme(ShapeTypes.Polygon);
                ls.getLegendBreaks().addAll(Arrays.asList(this.polygonBreaks));
                break;
        }
        
        return ls;
    }

    // </editor-fold>   
}
