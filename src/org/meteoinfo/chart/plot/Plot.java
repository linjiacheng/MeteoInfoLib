/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import org.meteoinfo.data.Dataset;

/**
 *
 * @author yaqiang
 */
public abstract class Plot {
    
    /** The default outline stroke. */
    public static final Stroke DEFAULT_OUTLINE_STROKE = new BasicStroke(0.5f,
            BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    /** The default outline color. */
    public static final Paint DEFAULT_OUTLINE_PAINT = Color.gray;

    /** The default foreground alpha transparency. */
    public static final float DEFAULT_FOREGROUND_ALPHA = 1.0f;

    /** The default background alpha transparency. */
    public static final float DEFAULT_BACKGROUND_ALPHA = 1.0f;

    /** The default background color. */
    public static final Paint DEFAULT_BACKGROUND_PAINT = Color.white;

    /** The minimum width at which the plot should be drawn. */
    public static final int MINIMUM_WIDTH_TO_DRAW = 10;

    /** The minimum height at which the plot should be drawn. */
    public static final int MINIMUM_HEIGHT_TO_DRAW = 10;

    /** A default box shape for legend items. */
    public static final Shape DEFAULT_LEGEND_ITEM_BOX
            = new Rectangle2D.Double(-4.0, -4.0, 8.0, 8.0);

    /** A default circle shape for legend items. */
    public static final Shape DEFAULT_LEGEND_ITEM_CIRCLE
            = new Ellipse2D.Double(-4.0, -4.0, 8.0, 8.0);   
    
    /** Column index as a sub plot. */
    public int columnIndex = 0;
    
    /** Row index as a sub plot. */
    public int rowIndex = 0;
    
    /**
     * Get dataset
     * @return Dataset
     */
    public abstract Dataset getDataset();
    
    /**
     * Set dataset
     * @param dataset Dataset
     */
    public abstract void setDataset(Dataset dataset);
    
    /**
     * Get plot type
     * @return Plot type
     */
    public abstract PlotType getPlotType();
    
    /**
     * Draw graphics
     * @param g2 Graphics2D
     * @param area Graphics area
     */
    public abstract void draw(Graphics2D g2, Rectangle2D area);
    
    /**
     * Get graphic area
     * @return Grahic area
     */
    public abstract Rectangle2D getGraphArea();        
        
}
