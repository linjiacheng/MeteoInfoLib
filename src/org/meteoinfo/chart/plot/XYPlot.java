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
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.meteoinfo.chart.ChartLegend;
import org.meteoinfo.chart.ChartText;
import org.meteoinfo.chart.LegendPosition;
import org.meteoinfo.chart.Location;
import org.meteoinfo.chart.Margin;
import org.meteoinfo.chart.axis.Axis;
import org.meteoinfo.chart.axis.TimeAxis;
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
    private final Map<Location, Axis> axises;
    //private Axis xAxis;
    //private Axis yAxis;
    private Location xAxisLocation;
    private Location yAxisLocation;
    private PlotOrientation orientation;
    private final GridLine gridLine;
    private boolean drawTopAxis;
    private boolean drawRightAxis;
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
        super();
        this.background = Color.white;
        this.drawBackground = false;
        //this.xAxis = new Axis("X", true);
        //this.yAxis = new Axis("Y", false);
        this.axises = new HashMap<>();
        this.axises.put(Location.BOTTOM, new Axis("X", true, Location.BOTTOM));
        this.axises.put(Location.LEFT, new Axis("Y", false, Location.LEFT));
        this.axises.put(Location.TOP, new Axis("X", true, Location.TOP, false));
        this.axises.put(Location.RIGHT, new Axis("Y", false, Location.RIGHT, false));
        this.xAxisLocation = Location.BOTTOM;
        this.yAxisLocation = Location.RIGHT;
        this.orientation = PlotOrientation.VERTICAL;
        this.gridLine = new GridLine();
        this.drawTopAxis = true;
        this.drawRightAxis = true;
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
        this.updateLegendScheme();
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
        this.getAxis(Location.BOTTOM).setMinMaxValue(extent.minX, extent.maxX);
        this.getAxis(Location.TOP).setMinMaxValue(extent.minX, extent.maxX);
        this.getAxis(Location.LEFT).setMinMaxValue(extent.minY, extent.maxY);
        this.getAxis(Location.RIGHT).setMinMaxValue(extent.minY, extent.maxY);
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
     * Get bottom x axis
     *
     * @return Bottom x aixs
     */
    public Axis getXAxis() {
        return this.axises.get(Location.BOTTOM);
    }
    
    /**
     * Set x axis
     * @param axis Axis 
     */
    public void setXAxis(Axis axis) {
        axis.setLocation(Location.BOTTOM);
        this.axises.put(Location.BOTTOM, axis);
        Axis topAxis = (Axis)axis.clone();
        topAxis.setLocation(Location.TOP);
        this.axises.put(Location.TOP, topAxis);
    }

    /**
     * Get left y axis
     *
     * @return Left y axis
     */
    public Axis getYAxis() {
        return this.axises.get(Location.LEFT);
    }
    
    /**
     * Set y axis
     * @param axis Axis 
     */
    public void setYAxis(Axis axis) {
        axis.setLocation(Location.LEFT);
        this.axises.put(Location.LEFT, axis);
        Axis rightAxis = (Axis)axis.clone();
        rightAxis.setLocation(Location.RIGHT);
        this.axises.put(Location.RIGHT, rightAxis);
    }

    /**
     * Get axis
     *
     * @param loc Axis location
     * @return Axis
     */
    public Axis getAxis(Location loc) {
        return this.axises.get(loc);
    }

    /**
     * Get x axis location
     *
     * @return X axis location
     */
    public Location getXAxisLocation() {
        return this.xAxisLocation;
    }

    /**
     * Set x axis location
     *
     * @param value X axis location
     */
    public void setXAxisLocation(Location value) {
        this.xAxisLocation = value;
    }

    /**
     * Get y axis location
     *
     * @return Y axis location
     */
    public Location getYAxisLocation() {
        return this.yAxisLocation;
    }

    /**
     * Set y axis location
     *
     * @param value Y axis location
     */
    public void setYAxisLocation(Location value) {
        this.yAxisLocation = value;
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
     * get if draw top axis
     *
     * @return Boolean
     */
    public boolean isDrawTopAxis() {
        return this.drawTopAxis;
    }

    /**
     * Set if draw top right axis
     *
     * @param value Boolean
     */
    public void setDrawTopAxis(boolean value) {
        this.drawTopAxis = value;
    }

    /**
     * get if draw right axis
     *
     * @return Boolean
     */
    public boolean isDrawRightAxis() {
        return this.drawRightAxis;
    }

    /**
     * Set if draw right axis
     *
     * @param value Boolean
     */
    public void setDrawRightAxis(boolean value) {
        this.drawRightAxis = value;
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

//        AffineTransform oldMatrix = g.getTransform();
//        Rectangle oldRegion = g.getClipBounds();
//        g.setClip(area);
//        g.translate(area.getX(), area.getY());
        //Get graphic area
        //graphArea = this.getGraphArea(g, area);
        //graphArea = this.getPositionArea(g, area);
        Rectangle2D graphArea = this.getPositionArea(this.getPositionAreaZoom());

        //Draw title
        //float y = 5;
        float y = (float) graphArea.getY();
        if (title != null) {
            g.setColor(title.getColor());
            g.setFont(title.getFont());
            //float x = (float) area.getWidth() / 2;
            float x = (float) (graphArea.getX() + graphArea.getWidth() / 2);
            //FontMetrics metrics = g.getFontMetrics(title.getFont());
            Dimension dim = Draw.getStringDimension(title.getText(), g);
            x -= dim.width / 2;
            //y += metrics.getHeight();
            y -= dim.height * 2 / 3;
            //g.drawString(title.getText(), x, y);
            Draw.drawString(g, title.getText(), x, y);
            //y += 5;
        }

//        //Update legend scheme
//        this.updateLegendScheme();

        //Draw grid lines        
        if (graphArea.getWidth() < 10 || graphArea.getHeight() < 10) {
//            g.setTransform(oldMatrix);
//            g.setClip(oldRegion);
            return;
        }

        this.drawGridLine(g, graphArea);

        //Draw graph        
        this.drawGraph(g, graphArea);

//        g.setStroke(new BasicStroke(1.0f));
//        g.setColor(Color.gray);
//        g.draw(graphArea);

        //Draw axis
        this.drawAxis(g, graphArea);

        //Draw legend
        if (this.drawLegend && this.getLegend() != null) {
            switch (this.legend.getPosition()) {
                case UPPER_CENTER_OUTSIDE:
                case LOWER_CENTER_OUTSIDE:
                    this.legend.setPlotOrientation(PlotOrientation.HORIZONTAL);
                    break;
                default:
                    this.legend.setPlotOrientation(PlotOrientation.VERTICAL);
                    break;
            }
            if (this.legend.isColorbar()) {
                if (this.legend.getPlotOrientation() == PlotOrientation.VERTICAL) {
                    this.legend.setHeight((int) graphArea.getHeight());
                } else {
                    this.legend.setWidth((int) graphArea.getWidth());
                }
            }
            if (this.legend.getPosition() == LegendPosition.CUSTOM) {
                float x = (float)(area.getWidth() * this.legend.getX());
                y = (float)(area.getHeight() * (1 - (this.getLegend().getHeight() / area.getHeight())
                        - this.getLegend().getY()));
                this.legend.draw(g, new PointF(x, y));
            } else {
                this.drawLegendScheme(g, graphArea, y);
            }
        }

//        g.setTransform(oldMatrix);
//        g.setClip(oldRegion);
    }

    /**
     * Get tight inset area
     *
     * @param g Graphics2D
     * @param positionArea Position area
     * @return Tight inset area
     */
    @Override
    public Margin getTightInset(Graphics2D g, Rectangle2D positionArea) {
        int left = 0, bottom = 0, right = 0, top = 0;
        int space = 1;

        if (this.title != null) {
            FontMetrics metrics = g.getFontMetrics(this.title.getFont());
            top += metrics.getHeight() + 10;
        }

        if (this.drawLegend && this.getLegend() != null) {
            Dimension dim = this.legend.getLegendDimension(g, new Dimension((int) positionArea.getWidth(),
                    (int) positionArea.getHeight()));
            switch (this.legend.getPosition()) {
                case UPPER_CENTER_OUTSIDE:
                    top += dim.height + 10;
                    break;
                case LOWER_CENTER_OUTSIDE:
                    bottom += dim.height + 10;
                    break;
                case LEFT_OUTSIDE:
                    left += dim.width + 10;
                    break;
                case RIGHT_OUTSIDE:
                    right += dim.width + 10;
                    break;
            }
        }

        //Get x axis space
        bottom += this.getXAxisHeight(g, space);

        //Get y axis space
        left += this.getYAxisWidth(g, space);

        //Set right space
        right += this.getXAxis().getMaxLabelLength(g) / 2;

        return new Margin(left, right, top, bottom);
    }

    /**
     * Get tight inset area
     *
     * @param g Graphics2D
     * @param positionArea Position area
     * @return Tight inset area
     */
    public Rectangle2D getTightInsetArea(Graphics2D g, Rectangle2D positionArea) {
        int left = 0, bottom = 0, right = 5, top = 5;
        int space = 1;

        if (this.title != null) {
            FontMetrics metrics = g.getFontMetrics(this.title.getFont());
            top += metrics.getHeight() + 10;
        }

        if (this.drawLegend && this.getLegend() != null) {
            Dimension dim = this.legend.getLegendDimension(g, new Dimension((int) positionArea.getWidth(),
                    (int) positionArea.getHeight()));
            switch (this.legend.getPosition()) {
                case UPPER_CENTER_OUTSIDE:
                    top += dim.height + 10;
                    break;
                case LOWER_CENTER_OUTSIDE:
                    bottom += dim.height + 10;
                    break;
                case LEFT_OUTSIDE:
                    left += dim.width + 10;
                    break;
                case RIGHT_OUTSIDE:
                    right += dim.width + 10;
                    break;
            }
        }

        //Get x axis space
        bottom += this.getXAxisHeight(g, space);

        //Get y axis space
        left += this.getYAxisWidth(g, space);

        //Set right space
        right += this.getXAxis().getMaxLabelLength(g) / 2;

        double x = positionArea.getX() - left;
        double y = positionArea.getY() - top;
        double w = positionArea.getWidth() + left + right;
        double h = positionArea.getHeight() + top + bottom;

        return new Rectangle2D.Double(x, y, w, h);
    }

    /**
     * Get graphic area
     *
     * @param g Graphic2D
     * @param area Whole area
     * @return Graphic area
     */
    @Override
    public Rectangle2D getPositionArea(Graphics2D g, Rectangle2D area) {
        double x = area.getWidth() * this.getPosition().getX() + area.getX();
        double y = area.getHeight() * (1 - this.getPosition().getHeight() - this.getPosition().getY()) + area.getY();
        double w = area.getWidth() * this.getPosition().getWidth();
        double h = area.getHeight() * this.getPosition().getHeight();
        return new Rectangle2D.Double(x, y, w, h);
    }

    /**
     * Get graphic area
     *
     * @param g Graphic2D
     * @param area Whole area
     * @return Graphic area
     */
    public Rectangle2D getGraphArea(Graphics2D g, Rectangle2D area) {
        int left = 0, bottom = 0, right = 5, top = 5;
        int space = 1;

        if (this.title != null) {
            FontMetrics metrics = g.getFontMetrics(this.title.getFont());
            top += metrics.getHeight() + 10;
        }

        if (this.drawLegend && this.getLegend() != null) {
            Dimension dim = this.legend.getLegendDimension(g, new Dimension((int) area.getWidth(), (int) area.getHeight()));
            switch (this.legend.getPosition()) {
                case UPPER_CENTER_OUTSIDE:
                    top += dim.height + 10;
                    break;
                case LOWER_CENTER_OUTSIDE:
                    bottom += dim.height + 10;
                    break;
                case LEFT_OUTSIDE:
                    left += dim.width + 10;
                    break;
                case RIGHT_OUTSIDE:
                    right += dim.width + 10;
                    break;
            }
        }

        //Get x axis space
        bottom += this.getXAxisHeight(g, space);

        //Get y axis space
        left += this.getYAxisWidth(g, space);

        //Set right space
        right += this.getXAxis().getMaxLabelLength(g) / 2;

        //Set area
        Rectangle2D plotArea = new Rectangle2D.Double(left, top,
                area.getWidth() - left - right, area.getHeight() - top - bottom);
        return plotArea;
    }

    int getXAxisHeight(Graphics2D g, int space) {
        Axis xAxis = this.getXAxis();
        FontMetrics metrics = g.getFontMetrics(xAxis.getTickLabelFont());
        int height = metrics.getHeight();
        if (!xAxis.isInsideTick())
            height += xAxis.getTickLength() + space;
        metrics = g.getFontMetrics(xAxis.getLabelFont());
        height += metrics.getHeight();
        if (xAxis.isDrawLabel()){
            Dimension dim = Draw.getStringDimension(xAxis.getLabel(), g);
            height += dim.height + space;
        }
        //height += 10;

        return height;
    }

    int getYAxisWidth(Graphics2D g, int space) {
        int width = this.getYAxis().getMaxLabelLength(g);
        if (!this.getYAxis().isInsideTick())
            width += this.getYAxis().getTickLength() + space;
        FontMetrics metrics = g.getFontMetrics(this.getYAxis().getLabelFont());
        width += metrics.getHeight();
        //width += 10;

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
            this.getXAxis().updateLabelGap(g, area);
            int n = 0;
            while (n < this.getXAxis().getTickValues().length) {
                double value = this.getXAxis().getTickValues()[n];
                xy = this.projToScreen(value, this.drawExtent.minY, area);
                x = xy[0];
                if (this.getXAxis().isInverse()) {
                    x = area.getWidth() - x;
                }
                x += minx;
                g.draw(new Line2D.Double(x, maxy, x, miny));

                n += this.getXAxis().getTickLabelGap();
            }
        }

        //Draw y grid lines
        if (this.gridLine.isDrawYLine()) {
            this.getYAxis().updateLabelGap(g, area);
            int n = 0;
            while (n < this.getYAxis().getTickValues().length) {
                double value = this.getYAxis().getTickValues()[n];
                xy = this.projToScreen(this.drawExtent.minX, value, area);
                y = xy[1];
                if (this.getYAxis().isInverse()) {
                    y = area.getHeight() - y;
                }
                y += area.getY();
                g.draw(new Line2D.Double(minx, y, maxx, y));

                n += this.getYAxis().getTickLabelGap();
            }
        }
    }

    abstract void drawGraph(Graphics2D g, Rectangle2D area);  
    
    void drawAxis(Graphics2D g, Rectangle2D area) {
        for (Location loc : this.axises.keySet()){
            Axis axis = this.axises.get(loc);
            if (axis.isVisible()) {
                axis.updateLabelGap(g, area);                
                axis.draw(g, area, this);
            }
        }        
    }

    void drawAxis_old(Graphics2D g, Rectangle2D area) {
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
        g.setColor(this.getXAxis().getLineColor());
        g.setStroke(this.getXAxis().getLineStroke());
        g.draw(new Line2D.Double(minx, maxy, maxx, maxy));
        if (this.drawTopAxis) {
            g.draw(new Line2D.Double(minx, miny, maxx, miny));
        }
        //Draw tick lines   
        g.setColor(this.getXAxis().getTickColor());
        g.setStroke(this.getXAxis().getTickStroke());
        g.setFont(this.getXAxis().getTickLabelFont());
        FontMetrics metrics = g.getFontMetrics(this.getXAxis().getTickLabelFont());
        String drawStr;
        Dimension dim;
        this.getXAxis().updateLabelGap(g, area);
        int len = this.getXAxis().getTickLength();
        List<String> tickLabels = this.getXAxis().getTickLabels();
        int n = 0;
        while (n < this.getXAxis().getTickValues().length) {
            double value = this.getXAxis().getTickValues()[n];
            xy = this.projToScreen(value, this.drawExtent.minY, area);
            x = xy[0];
            if (this.getXAxis().isInverse()) {
                x = area.getWidth() - x;
            }
            x += minx;
            if (this.getXAxis().isInsideTick()) {
                g.draw(new Line2D.Double(x, maxy, x, maxy - len));
            } else {
                g.draw(new Line2D.Double(x, maxy, x, maxy + len));
            }
            if (this.drawTopAxis) {
                if (this.getXAxis().isInsideTick()) {
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
            n += this.getXAxis().getTickLabelGap();
        }
        //Time label - left
        SimpleDateFormat format;
        if (this.getXAxis() instanceof TimeAxis) {
            TimeAxis tAxis = (TimeAxis) this.getXAxis();
            //if (this.getXAxis().isTimeAxis()) {
            drawStr = null;
            switch (tAxis.getTimeUnit()) {
                case MONTH:
                    format = new SimpleDateFormat("yyyy");
                    Date cdate = DateUtil.fromOADate(this.getXAxis().getTickValues()[0]);
                    drawStr = format.format(cdate);
                    break;
                case DAY:
                    format = new SimpleDateFormat("yyyy-MM");
                    cdate = DateUtil.fromOADate(this.getXAxis().getTickValues()[0]);
                    drawStr = format.format(cdate);
                    break;
                case HOUR:
                case MINITUE:
                case SECOND:
                    format = new SimpleDateFormat("yyyy-MM-dd");
                    cdate = DateUtil.fromOADate(this.getXAxis().getTickValues()[0]);
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
        if (this.getXAxis().isDrawLabel()) {
            x = (maxx - minx) / 2 + minx;
            y = maxy + space + metrics.getHeight() + 5;
            g.setFont(this.getXAxis().getLabelFont());
            g.setColor(this.getXAxis().getLabelColor());
            //metrics = g.getFontMetrics(this.getXAxis().getLabelFont());
            //dim = new Dimension(metrics.stringWidth(this.getXAxis().getLabel()), metrics.getHeight());
            dim = Draw.getStringDimension(this.getXAxis().getLabel(), g);
            labx = (float) (x - dim.width / 2);
            laby = (float) (y + dim.height * 3 / 4);
            if (!this.getXAxis().isInsideTick()) {
                laby += len;
            }
            //g.drawString(this.getXAxis().getLabel(), labx, laby);
            Draw.drawString(g, this.getXAxis().getLabel(), labx, laby);
        }

        //Draw y axis
        //Draw axis line
        g.setColor(this.getYAxis().getLineColor());
        g.setStroke(this.getYAxis().getLineStroke());
        g.draw(new Line2D.Double(minx, maxy, minx, miny));
        if (this.drawRightAxis) {
            g.draw(new Line2D.Double(maxx, maxy, maxx, miny));
        }
        //Draw tick lines   
        g.setColor(this.getYAxis().getTickColor());
        g.setStroke(this.getYAxis().getTickStroke());
        g.setFont(this.getYAxis().getTickLabelFont());
        metrics = g.getFontMetrics(this.getYAxis().getTickLabelFont());
        this.getYAxis().updateLabelGap(g, area);
        len = this.getYAxis().getTickLength();
        tickLabels = this.getYAxis().getTickLabels();
        n = 0;
        while (n < this.getYAxis().getTickValues().length) {
            double value = this.getYAxis().getTickValues()[n];
            xy = this.projToScreen(this.drawExtent.minX, value, area);
            y = xy[1];
            if (this.getYAxis().isInverse()) {
                y = area.getHeight() - y;
            }
            y += area.getY();
            if (this.getYAxis().isInsideTick()) {
                g.draw(new Line2D.Double(minx, y, minx + len, y));
            } else {
                g.draw(new Line2D.Double(minx, y, minx - len, y));
            }
            if (this.drawRightAxis) {
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
            n += this.getYAxis().getTickLabelGap();
        }
        //Draw label
        if (this.getYAxis().isDrawLabel()) {
            g.setFont(this.getYAxis().getLabelFont());
            dim = Draw.getStringDimension(this.getYAxis().getLabel(), g);
            //metrics = g.getFontMetrics(this.getYAxis().getLabelFont());
            x = minx - space - this.getYAxis().getMaxLabelLength(g) - dim.height - 5;
            if (!this.getYAxis().isInsideTick()) {
                x -= len;
            }
            y = (maxy - miny) / 2 + miny;
            //x = g.getTransform().getTranslateX() + x;
            //y = g.getTransform().getTranslateY() + y;
            //Draw.drawLabelPoint((float)x, (float)y, this.getYAxis().getLabelFont(), this.getYAxis().getLabel(), 
            //        this.getYAxis().getLabelColor(), -90, g, null);
            Draw.drawLabelPoint_270((float) x, (float) y, this.getYAxis().getLabelFont(), this.getYAxis().getLabel(),
                    this.getYAxis().getLabelColor(), g, null);
        }
    }

    void drawLegendScheme(Graphics2D g, Rectangle2D area, float y) {
        g.setFont(this.legend.getLabelFont());
        Dimension dim = this.legend.getLegendDimension(g, new Dimension((int) area.getWidth(), (int) area.getHeight()));
        float x = 0;
        //Rectangle2D graphArea = this.getPositionArea();
        switch (this.legend.getPosition()) {
            case UPPER_CENTER_OUTSIDE:
                x = (float) area.getWidth() / 2 - dim.width / 2;
                y += 5;
                break;
            case LOWER_CENTER_OUTSIDE:
                x = (float) area.getWidth() / 2 - dim.width / 2;
                y += area.getHeight() + this.getXAxisHeight(g, 1) + 5;
                break;
            case LEFT_OUTSIDE:
                x = 10;
                y = (float) area.getHeight() / 2 - dim.height / 2;
                break;
            case RIGHT_OUTSIDE:
                x = (float) area.getX() + (float) area.getWidth() + 10;
                y = (float) area.getY() + (float) area.getHeight() / 2 - dim.height / 2;
                break;
            case UPPER_RIGHT:
                x = (float) area.getWidth() - dim.width;
                y = y + 20;
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

    abstract void addText(ChartText text);
    // </editor-fold>
}
