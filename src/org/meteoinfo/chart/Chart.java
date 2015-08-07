/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.chart.plot.Plot;
import org.meteoinfo.chart.plot.XY2DPlot;
import org.meteoinfo.global.PointF;

/**
 *
 * @author yaqiang
 */
public class Chart {

    // <editor-fold desc="Variables">
    private List<Plot> plots;
    private int rowNum;
    private int columnNum;
    private ChartText title;
    private ChartText subTitle;
    private ChartLegend legend;
    private Color background;
    private boolean drawBackground;
    private boolean drawLegend;
    private Rectangle2D plotArea;
    private boolean antiAlias;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public Chart() {
        this.drawLegend = false;
        this.background = Color.white;
        this.drawBackground = true;
        this.antiAlias = false;
        this.rowNum = 1;
        this.columnNum = 1;
        this.plots = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param plot Plot
     */
    public Chart(Plot plot) {
        this();
        this.plots.add(plot);
    }

    /**
     * Constructor
     *
     * @param title Title
     * @param plot Plot
     */
    public Chart(String title, Plot plot) {
        this(plot);
        if (title == null) {
            this.title = null;
        } else {
            this.title = new ChartText(title);
        }
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get plot
     *
     * @return Plot
     */
    public List<Plot> getPlots() {
        return plots;
    }

    /**
     * Get the first plot
     *
     * @return Plot
     */
    public Plot getPlot() {
        if (this.plots.isEmpty()) {
            return null;
        }

        return this.plots.get(0);
    }

    /**
     * Get row number of sub plots
     *
     * @return Row number of sub plots
     */
    public int getRowNum() {
        return this.rowNum;
    }

    /**
     * Set row number of sub plots
     *
     * @param value Row number of sub plots
     */
    public void setRowNum(int value) {
        this.rowNum = value;
    }

    /**
     * Get column number of sub plots
     *
     * @return Column number of sub plots
     */
    public int getColumnNum() {
        return this.columnNum;
    }

    /**
     * Set column number of sub plots
     *
     * @param value Column number of sub plots
     */
    public void setColumnNum(int value) {
        this.columnNum = value;
    }

    /**
     * Get title
     *
     * @return Title
     */
    public ChartText getTitle() {
        return title;
    }

    /**
     * Set title
     *
     * @param value Title
     */
    public void setTitle(ChartText value) {
        title = value;
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

    /**
     * Get chart legend
     *
     * @return Chart legend
     */
    public ChartLegend getLegend() {
        return this.legend;
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
     * Get plot area
     *
     * @return Plot area
     */
    public Rectangle2D getPlotArea() {
        return this.plotArea;
    }

    /**
     * Get if is anti-alias
     *
     * @return Boolean
     */
    public boolean isAntiAlias() {
        return this.antiAlias;
    }

    /**
     * Set if is anti-alias
     *
     * @param value Boolean
     */
    public void setAntiAlias(boolean value) {
        this.antiAlias = value;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Draw plot
     *
     * @param g Graphics2D
     * @param area Drawing area
     */
    public void draw(Graphics2D g, Rectangle2D area) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (antiAlias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            //g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        } else {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            //g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT);
        }

        AffineTransform oldMatrix = g.getTransform();
        Rectangle oldRegion = g.getClipBounds();
        g.setClip(area);
        g.translate(area.getX(), area.getY());

        //Draw background
        if (this.drawBackground) {
            g.setColor(background);
            g.fill(new Rectangle2D.Double(0, 0, area.getWidth(), area.getHeight()));
        }

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

        //Draw plot
        plotArea = this.getPlotArea(g, area);
        if (plotArea.getWidth() < 20 || plotArea.getHeight() < 20) {
            g.setTransform(oldMatrix);
            g.setClip(oldRegion);
            return;
        }

        if (this.plots.size() > 0) {
            double zoom = this.getPositionAreaZoom(g, area);
            for (Plot plot : this.plots) {
                plot.setPositionAreaZoom(zoom);
                //Rectangle2D subPlotArea = this.getSubPlotArea(g, plot, plotArea);                
                //Rectangle2D subPlotArea = plot.getOuterPositionArea();
                //plot.draw(g, subPlotArea);
                if (plot instanceof XY2DPlot){
                    ((XY2DPlot)plot).setAntialias(this.antiAlias);
                }
                plot.draw(g, area);
            }
        }

        //Draw legend
        if (this.drawLegend) {
            Dimension dim = this.legend.getLegendDimension(g, new Dimension((int) area.getWidth(), (int) area.getHeight()));
            float x = 0;
            switch (this.legend.getPosition()) {
                case UPPER_CENTER_OUTSIDE:
                    x = (float) area.getWidth() / 2 - dim.width / 2;
                    y += 5;
                    break;
                case LOWER_CENTER_OUTSIDE:
                    x = (float) area.getWidth() / 2 - dim.width / 2;
                    y += plotArea.getHeight() + 5;
                    break;
                case LEFT_OUTSIDE:
                    x = 10;
                    y = (float) area.getHeight() / 2 - dim.height / 2;
                    break;
                case RIGHT_OUTSIDE:
                    x = (float) plotArea.getWidth() + 10;
                    y = (float) area.getHeight() / 2 - dim.height / 2;
                    break;
            }
            this.legend.draw(g, new PointF(x, y));
        }

        g.setTransform(oldMatrix);
        g.setClip(oldRegion);
    }

    private Rectangle2D getPlotArea(Graphics2D g, Rectangle2D area) {
        Rectangle2D pArea = new Rectangle2D.Double();
        int top = 5;
        int left = 0;
        int right = 0;
        int bottom = 0;
        if (this.title != null) {
            FontMetrics metrics = g.getFontMetrics(this.title.getFont());
            top += metrics.getHeight() + 10;
        }
        if (this.drawLegend) {
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
        pArea.setRect(left, top, area.getWidth() - left - right, area.getHeight() - top - bottom);

        return pArea;
    }

    private double getPositionAreaZoom(Graphics2D g, Rectangle2D area) {
        double zoom = 1.0;
        for (Plot plot : this.plots) {
            if (plot.isSubPlot) {
                double rowHeight = area.getHeight() / this.rowNum;
                double colWidth = area.getWidth() / this.columnNum;
                double x = area.getX() + plot.columnIndex * colWidth;
                double y = area.getY() + plot.rowIndex * rowHeight;
                Rectangle2D subPlotArea = new Rectangle2D.Double(x, y, colWidth, rowHeight);
                plot.setOuterPositionArea(subPlotArea);
                plot.updatePosition(area, subPlotArea);
                Rectangle2D positionArea = plot.getPositionArea(g, area);
                plot.setPositionArea(positionArea);
                Margin tightInset = plot.getTightInset(g, positionArea);
                plot.setTightInset(tightInset);
                double zoom1 = plot.updatePostionAreaZoom();
                if (zoom1 < zoom) {
                    zoom = zoom1;
                }
            } else {
                plot.setOuterPositionArea(area);
                Rectangle2D positionArea = plot.getPositionArea(g, area);
                plot.setPositionArea(positionArea);
                Margin tightInset = plot.getTightInset(g, positionArea);
                plot.setTightInset(tightInset);
                double zoom1 = plot.updatePostionAreaZoom();
                if (zoom1 < zoom) {
                    zoom = zoom1;
                }
            }
        }

        return zoom;
    }

    private Rectangle2D getSubPlotArea(Graphics2D g, Plot plot, Rectangle2D area) {
        if (plot.isSubPlot) {
            double rowHeight = area.getHeight() / this.rowNum;
            double colWidth = area.getWidth() / this.columnNum;
            double x = area.getX() + plot.columnIndex * colWidth;
            double y = area.getY() + plot.rowIndex * rowHeight;
            Rectangle2D subPlotArea = new Rectangle2D.Double(x, y, colWidth, rowHeight);
            plot.setOuterPositionArea(subPlotArea);
            plot.updatePosition(area, subPlotArea);
            Rectangle2D positionArea = plot.getPositionArea(g, area);
            plot.setPositionArea(positionArea);
            Margin tightInset = plot.getTightInset(g, positionArea);
            plot.setTightInset(tightInset);
            double zoom = plot.updatePostionAreaZoom();
            plot.setPositionAreaZoom(zoom);
            return subPlotArea;
        } else {
            plot.setOuterPositionArea(area);
            Rectangle2D positionArea = plot.getPositionArea(g, area);
            plot.setPositionArea(positionArea);
            Margin tightInset = plot.getTightInset(g, positionArea);
            plot.setTightInset(tightInset);
            double zoom = plot.updatePostionAreaZoom();
            plot.setPositionAreaZoom(zoom);
            //return tightInset.getArea(positionArea);
            return area;
        }
    }

    /**
     * Get graph area
     *
     * @return Get graph area
     */
    public Rectangle2D getGraphArea() {
        Rectangle2D rect = this.plots.get(0).getPositionArea();
        double left = rect.getX() + this.plotArea.getX();
        double top = rect.getY() + this.plotArea.getY();
        return new Rectangle2D.Double(left, top, rect.getWidth(), rect.getHeight());
    }
    
    /**
     * Find a plot by point
     * @param x X
     * @param y Y 
     * @return Plot
     */
    public Plot findPlot(int x, int y){
        for (Plot plot : this.plots){
            Rectangle2D area = plot.getPositionArea();
            if (area.contains(x, y)){
                return plot;
            }
        }
        
        return null;
    }

    /**
     * Clear plots
     */
    public void clearPlots() {
        this.plots.clear();
    }

    /**
     * Remove a plot
     *
     * @param plot The plot
     */
    public void removePlot(Plot plot) {
        this.plots.remove(plot);
    }

    /**
     * Add a plot
     *
     * @param plot Plot
     */
    public void addPlot(Plot plot) {
        this.plots.add(plot);
    }

    /**
     * Set plot
     *
     * @param plot Plot
     */
    public void setPlot(Plot plot) {
        this.plots.clear();
        this.plots.add(plot);
    }

    /**
     * Get plot by plot index
     *
     * @param plotIdx Plot index - begin with 1
     * @return
     */
    public Plot getPlot(int plotIdx) {
        for (Plot plot : this.plots) {
            int pIdx = plot.columnIndex * this.columnNum + plot.rowIndex + 1;
            if (pIdx == plotIdx) {
                return plot;
            }
        }

        return null;
    }
    // </editor-fold>

}
