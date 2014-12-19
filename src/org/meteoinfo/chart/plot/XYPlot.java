/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.meteoinfo.chart.axis.Axis;
import org.meteoinfo.data.Dataset;
import org.meteoinfo.data.XYArrayDataset;
import org.meteoinfo.data.XYDataset;
import org.meteoinfo.drawing.Draw;
import static org.meteoinfo.drawing.Draw.getDashPattern;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.PointF;
import org.meteoinfo.global.util.DateUtil;
import org.meteoinfo.legend.LegendScheme;
import org.meteoinfo.legend.PointBreak;
import org.meteoinfo.legend.PolygonBreak;
import org.meteoinfo.legend.PolylineBreak;
import org.meteoinfo.shape.ShapeTypes;

/**
 *
 * @author yaqiang
 */
public class XYPlot extends Plot {

    // <editor-fold desc="Variables">
    private XYDataset dataset;
    private ChartPlotMethod chartPlotMethod;
    private Color background;
    private boolean drawBackground;
    private PolylineBreak[] lineBreaks;
    private PointBreak[] pointBreaks;
    private PolygonBreak[] polygonBreaks;
    private PointBreak[][] itemPointBreaks;
    private boolean useBreak2D;
    private Extent drawExtent;
    private final Axis xAxis;
    private final Axis yAxis;
    private Rectangle2D graphArea;
    private PlotOrientation orientation;
    private GridLine gridLine;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public XYPlot() {
        this.background = Color.white;
        this.drawBackground = false;
        this.xAxis = new Axis("X", true);
        this.yAxis = new Axis("Y", false);
        this.chartPlotMethod = ChartPlotMethod.LINE;
        this.useBreak2D = false;
        this.orientation = PlotOrientation.VERTICAL;
        this.gridLine = new GridLine();
    }

    /**
     * Constructor
     *
     * @param dateset Dataset
     */
    public XYPlot(XYDataset dateset) {
        this();
        this.setDataset(dateset);
    }

    /**
     * Constructor
     *
     * @param orientation Plot orientation
     * @param dateset Dataset
     */
    public XYPlot(PlotOrientation orientation, XYDataset dateset) {
        this();
        this.orientation = orientation;
        this.setDataset(dateset);
    }

    /**
     * Constructor
     *
     * @param isTime If x axis is time
     * @param dateset Dataset
     */
    public XYPlot(boolean isTime, XYDataset dateset) {
        this();
        this.xAxis.setTimeAxis(isTime);
        this.setDataset(dateset);
    }

    /**
     * Constructor
     *
     * @param isTime If x axis is time
     * @param orientation Plot orientation
     * @param dateset Dataset
     */
    public XYPlot(boolean isTime, PlotOrientation orientation, XYDataset dateset) {
        this();
        this.xAxis.setTimeAxis(isTime);
        this.orientation = orientation;
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
     * Get draw extent
     *
     * @return Draw extent
     */
    public Extent getDrawExtent() {
        return this.drawExtent;
    }

    /**
     * Set draw extent
     *
     * @param extent Extent
     */
    public void setDrawExtent(Extent extent) {
        this.drawExtent = extent;
        this.xAxis.setMinMaxValue(extent.minX, extent.maxX);
        this.yAxis.setMinMaxValue(extent.minY, extent.maxY);
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
    
    /**
     * Get background
     *
     * @return Background
     */
    public Color getBackground() {
        return this.background;
    }

    /**
     * Set background
     *
     * @param value Background
     */
    public void setBackground(Color value) {
        this.background = value;
    }
    
    /**
     * Get if draw background
     * @return Boolean
     */
    public boolean isDrawBackground(){
        return this.drawBackground;
    }
    
    /**
     * Set if draw background
     * @param value Boolean
     */
    public void setDrawBackground(boolean value){
        this.drawBackground = value;
    }

    @Override
    public PlotType getPlotType() {
        return PlotType.XY;
    }

    /**
     * Get x axis
     *
     * @return X aixs
     */
    public Axis getXAxis() {
        return this.xAxis;
    }

    /**
     * Get y axis
     *
     * @return Y axis
     */
    public Axis getYAxis() {
        return this.yAxis;
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

    /**
     * Get graph area
     *
     * @return Graph area
     */
    @Override
    public Rectangle2D getGraphArea() {
        return this.graphArea;
    }

    /**
     * Get plot orientation
     *
     * @return Plot orientation
     */
    public PlotOrientation getPlotOrientation() {
        return this.orientation;
    }

    /**
     * Set plot orientation
     *
     * @param value Plot orientation
     */
    public void setPlotOrientation(PlotOrientation value) {
        this.orientation = value;
    }
    
    /**
     * Get grid line
     * @return Grid line
     */
    public GridLine getGridLine(){
        return this.gridLine;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Draw plot
     *
     * @param g Graphics2D
     * @param area Drawing area
     */
    @Override
    public void draw(Graphics2D g, Rectangle2D area) {
        // if the plot area is too small, just return...
        boolean b1 = (area.getWidth() <= MINIMUM_WIDTH_TO_DRAW);
        boolean b2 = (area.getHeight() <= MINIMUM_HEIGHT_TO_DRAW);
        if (b1 || b2) {
            return;
        }

        AffineTransform oldMatrix = g.getTransform();
        Rectangle oldRegion = g.getClipBounds();
        g.setClip(area);
        g.translate(area.getX(), area.getY());
        
        //Draw grid lines
        graphArea = this.getGraphArea(g, area);
        if (graphArea.getWidth() < 10 || graphArea.getHeight() < 10){
            g.setTransform(oldMatrix);
            g.setClip(oldRegion);        
            return;
        }
        
        this.drawGridLine(g, graphArea);

        //Draw graph        
        this.drawGraph(g, graphArea);

        g.setStroke(new BasicStroke(1.0f));
        g.setColor(Color.gray);
        g.draw(graphArea);

        //Draw axis
        this.drawAxis(g, graphArea);

        g.setTransform(oldMatrix);
        g.setClip(oldRegion);
    }

    private Rectangle2D getGraphArea(Graphics2D g, Rectangle2D area) {
        int left, bottom, right = 10, top = 5;
        int space = 1;

        //Get x axis space
        FontMetrics metrics = g.getFontMetrics(this.xAxis.getTickLabelFont());
        bottom = metrics.getHeight();
        bottom += this.xAxis.getTickLength() + space;
        metrics = g.getFontMetrics(this.xAxis.getLabelFont());
        bottom += metrics.getHeight() + 10;

        //Get y axis space
        left = this.yAxis.getMaxLabelLength(g) + this.yAxis.getTickLength() + space;
        metrics = g.getFontMetrics(this.yAxis.getLabelFont());
        left += metrics.getHeight() + 10;

        //Set area
        Rectangle2D plotArea = new Rectangle2D.Double(left, top,
                area.getWidth() - left - right, area.getHeight() - top - bottom);
        return plotArea;
    }

    private void drawGridLine(Graphics2D g, Rectangle2D area){
        if (!this.gridLine.isDrawXLine() && !this.gridLine.isDrawYLine())
            return;
        
        double[] xy;
        double x, y;
        double miny = area.getY();
        double minx = area.getX();
        double maxx = area.getX() + area.getWidth();
        double maxy = area.getY() + area.getHeight();
        float labx, laby;
        int space = 2;

        float[] dashPattern = getDashPattern(this.gridLine.getStyle());
        g.setColor(this.gridLine.getColor());
        g.setStroke(new BasicStroke(this.gridLine.getSize(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10.0f, dashPattern, 0.0f));
        
        //Draw x grid lines
        if (this.gridLine.isDrawXLine()){            
            this.xAxis.updateLabelGap(g, area);
            int n = 0;
            while (n < this.xAxis.getTickValues().length) {
                double value = this.xAxis.getTickValues()[n];
                xy = this.projToScreen(value, this.drawExtent.minY, area);
                x = xy[0];
                if (this.xAxis.isInverse()) {
                    x = area.getWidth() - x;
                }
                x += minx;
                g.draw(new Line2D.Double(x, maxy, x, miny));
                
                n += this.xAxis.getTickLabelGap();
            }            
        }

        //Draw y grid lines
        if (this.gridLine.isDrawYLine()){            
            this.yAxis.updateLabelGap(g, area);
            int n = 0;
            while (n < this.yAxis.getTickValues().length) {
                double value = this.yAxis.getTickValues()[n];
                xy = this.projToScreen(this.drawExtent.minX, value, area);
                y = xy[1];
                if (this.yAxis.isInverse()) {
                    y = area.getHeight() - y;
                }
                y += area.getY();
                g.draw(new Line2D.Double(minx, y, maxx, y));
                
                n += this.yAxis.getTickLabelGap();
            }            
        }
    }
    
    private void drawGraph(Graphics2D g, Rectangle2D area) {
        AffineTransform oldMatrix = g.getTransform();
        Rectangle oldRegion = g.getClipBounds();
        g.setClip(area);
        g.translate(area.getX(), area.getY());
        
        //Draw background
        if (this.drawBackground){
            g.setColor(background);
            g.fill(new Rectangle2D.Double(0, 0, area.getWidth(), area.getHeight()));
        }

        double[] xy;
        for (int i = 0; i < this.dataset.getSeriesCount(); i++) {
            int len = this.dataset.getItemCount(i);
            PointF[] points = new PointF[len];
            List<Integer> mvIdx = this.dataset.getMissingValueIndex(i);
            if (this.orientation == PlotOrientation.VERTICAL) {
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
            if (this.yAxis.isInverse()) {
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
            if (this.xAxis.isInverse()) {
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

    private void drawAxis(Graphics2D g, Rectangle2D area) {
        double[] xy;
        double x, y;
        double miny = area.getY();
        double minx = area.getX();
        double maxx = area.getX() + area.getWidth();
        double maxy = area.getY() + area.getHeight();
        float labx, laby;
        int space = 2;

        //Draw x axis
        //Draw axis line
        g.setColor(this.xAxis.getLineColor());
        g.setStroke(this.xAxis.getLineStroke());
        g.draw(new Line2D.Double(minx, maxy, maxx, maxy));
        //Draw tick lines   
        g.setColor(this.xAxis.getTickColor());
        g.setStroke(this.xAxis.getTickStroke());
        g.setFont(this.xAxis.getTickLabelFont());
        FontMetrics metrics = g.getFontMetrics(this.xAxis.getTickLabelFont());
        String drawStr;
        Dimension dim;
        this.xAxis.updateLabelGap(g, area);
        int len = this.xAxis.getTickLength();
        List<String> tickLabels = this.xAxis.getTickLabels();
        int n = 0;
        while (n < this.xAxis.getTickValues().length) {
            double value = this.xAxis.getTickValues()[n];
            xy = this.projToScreen(value, this.drawExtent.minY, area);
            x = xy[0];
            if (this.xAxis.isInverse()) {
                x = area.getWidth() - x;
            }
            x += minx;
            g.draw(new Line2D.Double(x, maxy, x, maxy + len));
            //Draw tick label
            drawStr = tickLabels.get(n);
            dim = new Dimension(metrics.stringWidth(drawStr), metrics.getHeight());
            labx = (float) (x - dim.width / 2);
            laby = (float) (maxy + len + dim.height * 3 / 4 + space);
            g.drawString(drawStr, labx, laby);
            n += this.xAxis.getTickLabelGap();
        }
        //Time label - left
        SimpleDateFormat format;
        if (this.xAxis.isTimeAxis()) {
            drawStr = null;
            switch (this.xAxis.getTimeUnit()) {
                case MONTH:
                    format = new SimpleDateFormat("yyyy");
                    Date cdate = DateUtil.fromOADate(this.xAxis.getTickValues()[0]);
                    drawStr = format.format(cdate);
                    break;
                case DAY:
                    format = new SimpleDateFormat("yyyy-MM");
                    cdate = DateUtil.fromOADate(this.xAxis.getTickValues()[0]);
                    drawStr = format.format(cdate);
                    break;
                case HOUR:
                case MINITUE:
                case SECOND:
                    format = new SimpleDateFormat("yyyy-MM-dd");
                    cdate = DateUtil.fromOADate(this.xAxis.getTickValues()[0]);
                    drawStr = format.format(cdate);
                    break;
            }            
            if (drawStr != null) {
                labx = (float) minx;
                laby = (float) (maxy + len + metrics.getHeight() * 2 + space);
                g.drawString(drawStr, labx, laby);
            }
        }
        //Draw label
        if (this.xAxis.isDrawLabel()){
            x = (maxx - minx) / 2 + minx;
            y = maxy + len + space + metrics.getHeight() + 5;
            metrics = g.getFontMetrics(this.xAxis.getLabelFont());
            dim = new Dimension(metrics.stringWidth(this.xAxis.getLabel()), metrics.getHeight());
            labx = (float) (x - dim.width / 2);
            laby = (float) (y + dim.height * 3 / 4);
            g.setFont(this.xAxis.getLabelFont());
            g.setColor(this.xAxis.getLabelColor());
            g.drawString(this.xAxis.getLabel(), labx, laby);
        }

        //Draw y axis
        //Draw axis line
        g.setColor(this.yAxis.getLineColor());
        g.setStroke(this.yAxis.getLineStroke());
        g.draw(new Line2D.Double(minx, maxy, minx, miny));
        //Draw tick lines   
        g.setColor(this.yAxis.getTickColor());
        g.setStroke(this.yAxis.getTickStroke());
        g.setFont(this.yAxis.getTickLabelFont());
        metrics = g.getFontMetrics(this.yAxis.getTickLabelFont());
        this.yAxis.updateLabelGap(g, area);
        len = this.yAxis.getTickLength();
        tickLabels = this.yAxis.getTickLabels();
        n = 0;
        while (n < this.yAxis.getTickValues().length) {
            double value = this.yAxis.getTickValues()[n];
            xy = this.projToScreen(this.drawExtent.minX, value, area);
            y = xy[1];
            if (this.yAxis.isInverse()) {
                y = area.getHeight() - y;
            }
            y += area.getY();
            g.draw(new Line2D.Double(minx, y, minx - len, y));
            //Draw tick label
            drawStr = tickLabels.get(n);
            dim = new Dimension(metrics.stringWidth(drawStr), metrics.getHeight());
            labx = (float) (minx - len - dim.width - space);
            laby = (float) (y + dim.height / 3);
            g.drawString(drawStr, labx, laby);
            n += this.yAxis.getTickLabelGap();
        }
        //Draw label
        if (this.yAxis.isDrawLabel()) {
            metrics = g.getFontMetrics(this.yAxis.getLabelFont());
            x = minx - len - space - this.getYAxis().getMaxLabelLength(g) - metrics.getHeight() - 5;
            y = (maxy - miny) / 2 + miny;
            //x = g.getTransform().getTranslateX() + x;
            //y = g.getTransform().getTranslateY() + y;
            //Draw.drawLabelPoint((float)x, (float)y, this.yAxis.getLabelFont(), this.yAxis.getLabel(), 
            //        this.yAxis.getLabelColor(), -90, g, null);
            Draw.drawLabelPoint_270((float) x, (float) y, this.yAxis.getLabelFont(), this.yAxis.getLabel(),
                    this.yAxis.getLabelColor(), g, null);
        }
    }

    /**
     * Convert coordinate from map to screen
     *
     * @param projX Map X
     * @param projY Map Y
     * @param area Drawing area
     * @return Screen X/Y array
     */
    public double[] projToScreen(double projX, double projY, Rectangle2D area) {
        double scaleX = area.getWidth() / drawExtent.getWidth();
        double scaleY = area.getHeight() / drawExtent.getHeight();
        double screenX = (projX - drawExtent.minX) * scaleX;
        double screenY = (drawExtent.maxY - projY) * scaleY;

        return new double[]{screenX, screenY};
    }

    /**
     * Convert coordinate from screen to map
     *
     * @param screenX Screen X
     * @param screenY Screen Y
     * @param area Area
     * @return Projected X/Y
     */
    public double[] screenToProj(double screenX, double screenY, Rectangle2D area) {
        double scaleX = area.getWidth() / drawExtent.getWidth();
        double scaleY = area.getHeight() / drawExtent.getHeight();
        double projX = screenX / scaleX + drawExtent.minX;
        double projY = drawExtent.maxY - screenY / scaleY;

        return new double[]{projX, projY};
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
    public Extent getAutoExtent() {
        Extent extent = dataset.getDataExtent();
        double xgap = extent.getWidth() / Math.min(dataset.getItemCount(), 50);
        double ygap = extent.getHeight() / Math.min(dataset.getItemCount(), 50);
        extent = extent.extend(xgap, ygap);
        if (this.orientation == PlotOrientation.VERTICAL) {
            return extent;
        } else {
            return new Extent(extent.minY, extent.maxY, extent.minX, extent.maxX);
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
