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
import org.meteoinfo.chart.plot.Plot;
import org.meteoinfo.chart.plot.PlotType;
import org.meteoinfo.chart.plot.XY1DPlot;
import org.meteoinfo.global.PointF;

/**
 *
 * @author yaqiang
 */
public class Chart {

    // <editor-fold desc="Variables">
    private final Plot plot;
    private ChartTitle title;
    private ChartTitle subTitle;
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
     *
     * @param plot Plot
     */
    public Chart(Plot plot) {
        this.plot = plot;
        if (plot.getPlotType() == PlotType.XY)
            this.legend = new ChartLegend((XY1DPlot)plot);
        else {
            this.legend = null;            
        }
        this.drawLegend = false;
        this.background = Color.white;
        this.drawBackground = false;
        this.antiAlias = false;
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
            this.title = new ChartTitle(title);
        }
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get plot
     *
     * @return Plot
     */
    public Plot getPlot() {
        return plot;
    }

    /**
     * Get title
     *
     * @return Title
     */
    public ChartTitle getTitle() {
        return title;
    }

    /**
     * Set title
     *
     * @param value Title
     */
    public void setTitle(ChartTitle value) {
        title = value;
    }

    /**
     * Get sub title
     *
     * @return Sub title
     */
    public ChartTitle getSubTitle() {
        return subTitle;
    }

    /**
     * Set sub title
     *
     * @param value Sub title
     */
    public void setSubTitle(ChartTitle value) {
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
        
        if (plot != null) {
            this.plot.draw(g, plotArea);
        }
        
        //Draw legend
        if (this.drawLegend){
            Dimension dim = this.legend.getLegendDimension(g, new Dimension((int)area.getWidth(), (int)area.getHeight()));
            float x = 0;
            switch (this.legend.getPosition()){
                case TOP:
                    x = (float) area.getWidth() / 2 - dim.width / 2;
                    y += 5;
                    break;
                case BOTTOM:
                    x = (float) area.getWidth() / 2 - dim.width / 2;
                    y += plotArea.getHeight() + 5;
                    break;
                case LEFT:
                    x = 10;
                    y = (float) area.getHeight() / 2 - dim.height / 2;
                    break;
                case RIGHT:
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
        if (this.drawLegend){
            Dimension dim = this.legend.getLegendDimension(g, new Dimension((int)area.getWidth(), (int)area.getHeight()));
            switch (this.legend.getPosition()){
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
        pArea.setRect(left, top, area.getWidth() - left - right, area.getHeight() - top - bottom);

        return pArea;
    }

    /**
     * Get graph area
     *
     * @return Get graph area
     */
    public Rectangle2D getGraphArea() {
        Rectangle2D rect = this.plot.getGraphArea();
        double left = rect.getX() + this.plotArea.getX();
        double top = rect.getY() + this.plotArea.getY();
        return new Rectangle2D.Double(left, top, rect.getWidth(), rect.getHeight());
    }
    // </editor-fold>

}
