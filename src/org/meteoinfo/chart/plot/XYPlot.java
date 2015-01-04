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
import java.util.Date;
import java.util.List;
import org.meteoinfo.chart.ChartLegend;
import org.meteoinfo.chart.ChartText;
import org.meteoinfo.chart.axis.Axis;
import static org.meteoinfo.chart.plot.Plot.MINIMUM_HEIGHT_TO_DRAW;
import static org.meteoinfo.chart.plot.Plot.MINIMUM_WIDTH_TO_DRAW;
import org.meteoinfo.drawing.Draw;
import static org.meteoinfo.drawing.Draw.getDashPattern;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.PointF;
import org.meteoinfo.global.util.DateUtil;

/**
 *
 * @author wyq
 */
public abstract class XYPlot extends Plot {

    // <editor-fold desc="Variables">
    private Color background;
    private boolean drawBackground;
    private Extent drawExtent;
    private final Axis xAxis;
    private final Axis yAxis;
    private Rectangle2D graphArea;
    private PlotOrientation orientation;
    private GridLine gridLine;
    private boolean drawTopRightAxis;
    private ChartText title;
    private ChartText subTitle;
    private ChartLegend legend;
    private boolean drawLegend;

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
        this.orientation = PlotOrientation.VERTICAL;
        this.gridLine = new GridLine();
        this.drawTopRightAxis = true;
    }
    // </editor-fold>

    // <editor-fold desc="Get Set Methods">
    /**
     * Get title
     *
     * @return Title
     */
    public ChartText getTitle() {
        return this.title;
    }

    /**
     * Set title
     *
     * @param value Title
     */
    public void setTitle(ChartText value) {
        this.title = value;
    }

    /**
     * Set title
     *
     * @param text Title text
     */
    public void setTitle(String text) {
        if (this.title == null) {
            this.title = new ChartText(text);
        } else {
            this.title.setText(text);
        }
    }

    /**
     * Get sub title
     *
     * @return Sub title
     */
    public ChartText getSubTitle() {
        return subTitle;
    }

    /**
     * Set sub title
     *
     * @param value Sub title
     */
    public void setSubTitle(ChartText value) {
        subTitle = value;
    }

    /**
     * Get chart legend
     *
     * @return Chart legend
     */
    public ChartLegend getLegend() {
        return this.legend;
    }

    /**
     * Set chart legend
     *
     * @param value Legend
     */
    public void setLegend(ChartLegend value) {
        this.legend = value;
    }

    /**
     * Get if draw legend
     *
     * @return If draw legend
     */
    public boolean isDrawLegend() {
        return this.drawLegend;
    }

    /**
     * Set if draw legend
     *
     * @param value Boolean
     */
    public void setDrawLegend(boolean value) {
        this.drawLegend = value;
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
     *
     * @return Boolean
     */
    public boolean isDrawBackground() {
        return this.drawBackground;
    }

    /**
     * Set if draw background
     *
     * @param value Boolean
     */
    public void setDrawBackground(boolean value) {
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
     * Get graph area
     *
     * @return Graph area
     */
    @Override
    public Rectangle2D getGraphArea() {
        return this.graphArea;
    }

    /**
     * Set graph area
     *
     * @param area Graph area
     */
    public void setGraphArea(Rectangle2D area) {
        this.graphArea = area;
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
     *
     * @return Grid line
     */
    public GridLine getGridLine() {
        return this.gridLine;
    }

    /**
     * get if draw top right axis
     *
     * @return Boolean
     */
    public boolean isDrawTopRightAxis() {
        return this.drawTopRightAxis;
    }

    /**
     * Set if draw top right axis
     *
     * @param value Boolean
     */
    public void setDrawTopRightAxis(boolean value) {
        this.drawTopRightAxis = value;
    }
    // </editor-fold>

    // <editor-fold desc="Method">
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

        //Draw title
        float y = 5;
        if (title != null) {
            g.setColor(title.getColor());
            g.setFont(title.getFont());
            float x = (float) area.getWidth() / 2;
            FontMetrics metrics = g.getFontMetrics(title.getFont());
            x -= metrics.stringWidth(title.getText()) / 2;
            y += metrics.getHeight();
            g.drawString(title.getText(), x, y);
            y += 5;
        }
        
        //Update legend scheme
        this.updateLegendScheme();

        //Draw grid lines
        graphArea = this.getGraphArea(g, area);
        if (graphArea.getWidth() < 10 || graphArea.getHeight() < 10) {
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

        //Draw legend
        if (this.drawLegend && this.getLegend() != null) {
            this.drawLegendScheme(g, area, y);
        }

        g.setTransform(oldMatrix);
        g.setClip(oldRegion);
    }

    Rectangle2D getGraphArea(Graphics2D g, Rectangle2D area) {
        int left = 0, bottom = 0, right = 10, top = 5;
        int space = 1;

        if (this.title != null) {
            FontMetrics metrics = g.getFontMetrics(this.title.getFont());
            top += metrics.getHeight() + 10;
        }

        if (this.drawLegend && this.getLegend() != null) {
            Dimension dim = this.legend.getLegendDimension(g, new Dimension((int) area.getWidth(), (int) area.getHeight()));
            switch (this.legend.getPosition()) {
                case TOP:
                    top += dim.height + 10;
                    break;
                case BOTTOM:
                    bottom += dim.height + 10;
                    break;
                case LEFT:
                    left += dim.width + 10;
                    break;
                case RIGHT:
                    right += dim.width + 10;
                    break;
            }
        }

        //Get x axis space
        bottom += this.getXAxisHeight(g, space);

        //Get y axis space
        left += this.getYAxisWidth(g, space);

        //Set area
        Rectangle2D plotArea = new Rectangle2D.Double(left, top,
                area.getWidth() - left - right, area.getHeight() - top - bottom);
        return plotArea;
    }
    
    int getXAxisHeight(Graphics2D g, int space) {
        FontMetrics metrics = g.getFontMetrics(this.xAxis.getTickLabelFont());
        int height = metrics.getHeight();
        height += this.xAxis.getTickLength() + space;
        metrics = g.getFontMetrics(this.xAxis.getLabelFont());
        height += metrics.getHeight() + 10;
        
        return height;
    }
    
    int getYAxisWidth(Graphics2D g, int space){
        int width = this.yAxis.getMaxLabelLength(g) + this.yAxis.getTickLength() + space;
        FontMetrics metrics = g.getFontMetrics(this.yAxis.getLabelFont());
        width += metrics.getHeight() + 10;
        
        return width;
    }

    void drawGridLine(Graphics2D g, Rectangle2D area) {
        if (!this.gridLine.isDrawXLine() && !this.gridLine.isDrawYLine()) {
            return;
        }

        double[] xy;
        double x, y;
        double miny = area.getY();
        double minx = area.getX();
        double maxx = area.getX() + area.getWidth();
        double maxy = area.getY() + area.getHeight();

        float[] dashPattern = getDashPattern(this.gridLine.getStyle());
        g.setColor(this.gridLine.getColor());
        g.setStroke(new BasicStroke(this.gridLine.getSize(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10.0f, dashPattern, 0.0f));

        //Draw x grid lines
        if (this.gridLine.isDrawXLine()) {
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
        if (this.gridLine.isDrawYLine()) {
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

    abstract void drawGraph(Graphics2D g, Rectangle2D area);

    void drawAxis(Graphics2D g, Rectangle2D area) {
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
        if (this.drawTopRightAxis) {
            g.draw(new Line2D.Double(minx, miny, maxx, miny));
        }
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
            if (this.xAxis.isInsideTick()) {
                g.draw(new Line2D.Double(x, maxy, x, maxy - len));
            } else {
                g.draw(new Line2D.Double(x, maxy, x, maxy + len));
            }
            if (this.drawTopRightAxis) {
                if (this.xAxis.isInsideTick()) {
                    g.draw(new Line2D.Double(x, miny, x, miny + len));
                } else {
                    g.draw(new Line2D.Double(x, miny, x, miny - len));
                }
            }
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
                laby = (float) (maxy + metrics.getHeight() * 2 + space);
                if (!this.getXAxis().isInsideTick()) {
                    laby += len;
                }
                g.drawString(drawStr, labx, laby);
            }
        }
        //Draw label
        if (this.xAxis.isDrawLabel()) {
            x = (maxx - minx) / 2 + minx;
            y = maxy + space + metrics.getHeight() + 5;
            metrics = g.getFontMetrics(this.xAxis.getLabelFont());
            dim = new Dimension(metrics.stringWidth(this.xAxis.getLabel()), metrics.getHeight());
            labx = (float) (x - dim.width / 2);
            laby = (float) (y + dim.height * 3 / 4);
            if (!this.xAxis.isInsideTick()) {
                laby += len;
            }
            g.setFont(this.xAxis.getLabelFont());
            g.setColor(this.xAxis.getLabelColor());
            g.drawString(this.xAxis.getLabel(), labx, laby);
        }

        //Draw y axis
        //Draw axis line
        g.setColor(this.yAxis.getLineColor());
        g.setStroke(this.yAxis.getLineStroke());
        g.draw(new Line2D.Double(minx, maxy, minx, miny));
        if (this.drawTopRightAxis) {
            g.draw(new Line2D.Double(maxx, maxy, maxx, miny));
        }
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
            if (this.getYAxis().isInsideTick()) {
                g.draw(new Line2D.Double(minx, y, minx + len, y));
            } else {
                g.draw(new Line2D.Double(minx, y, minx - len, y));
            }
            if (this.drawTopRightAxis) {
                if (this.getYAxis().isInsideTick()) {
                    g.draw(new Line2D.Double(maxx, y, maxx - len, y));
                } else {
                    g.draw(new Line2D.Double(maxx, y, maxx + len, y));
                }
            }
            //Draw tick label
            drawStr = tickLabels.get(n);
            dim = new Dimension(metrics.stringWidth(drawStr), metrics.getHeight());
            labx = (float) (minx - dim.width - space - space);
            if (!this.getYAxis().isInsideTick()) {
                labx -= len;
            }
            laby = (float) (y + dim.height / 3);
            g.drawString(drawStr, labx, laby);
            n += this.yAxis.getTickLabelGap();
        }
        //Draw label
        if (this.yAxis.isDrawLabel()) {
            metrics = g.getFontMetrics(this.yAxis.getLabelFont());
            x = minx - space - this.getYAxis().getMaxLabelLength(g) - metrics.getHeight() - 5;
            if (!this.getYAxis().isInsideTick()) {
                x -= len;
            }
            y = (maxy - miny) / 2 + miny;
            //x = g.getTransform().getTranslateX() + x;
            //y = g.getTransform().getTranslateY() + y;
            //Draw.drawLabelPoint((float)x, (float)y, this.yAxis.getLabelFont(), this.yAxis.getLabel(), 
            //        this.yAxis.getLabelColor(), -90, g, null);
            Draw.drawLabelPoint_270((float) x, (float) y, this.yAxis.getLabelFont(), this.yAxis.getLabel(),
                    this.yAxis.getLabelColor(), g, null);
        }
    }

    void drawLegendScheme(Graphics2D g, Rectangle2D area, float y) {
        Dimension dim = this.legend.getLegendDimension(g, new Dimension((int) area.getWidth(), (int) area.getHeight()));
        float x = 0;
        switch (this.legend.getPosition()) {
            case TOP:
                x = (float) area.getWidth() / 2 - dim.width / 2;
                y += 5;
                break;
            case BOTTOM:
                x = (float) area.getWidth() / 2 - dim.width / 2;
                y += graphArea.getHeight() + this.getXAxisHeight(g, 1) + 5;
                break;
            case LEFT:
                x = 10;
                y = (float) area.getHeight() / 2 - dim.height / 2;
                break;
            case RIGHT:
                x = (float) graphArea.getWidth() + 10;
                y = (float) area.getHeight() / 2 - dim.height / 2;
                break;
        }
        this.legend.draw(g, new PointF(x, y));
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

    abstract Extent getAutoExtent();
    
    abstract void updateLegendScheme();
    // </editor-fold>
}
