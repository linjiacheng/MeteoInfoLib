/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.plot;

import org.meteoinfo.legend.ColorBreak;
import org.meteoinfo.legend.PointBreak;
import org.meteoinfo.legend.PolygonBreak;
import org.meteoinfo.legend.PolylineBreak;

/**
 *
 * @author wyq
 */
public class SeriesLegend {
    // <editor-fold desc="Variables">
    private ColorBreak[] legendBreaks;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public SeriesLegend(){
        this.legendBreaks = new ColorBreak[1];
        this.legendBreaks[0] = new PolylineBreak();
    }
    
    /**
     * Constructor
     * @param cb ColorBreak
     */
    public SeriesLegend(ColorBreak cb){
        this.legendBreaks = new ColorBreak[1];
        this.legendBreaks[0] = cb;
    }
    
    /**
     * Constructor
     * @param n Break number
     */
    public SeriesLegend(int n){
        this.legendBreaks = new ColorBreak[n];        
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get if the legend is PointBreak
     * @return Boolean
     */
    public boolean isPoint(){
        return this.legendBreaks[0] instanceof PointBreak;
    }
    
    /**
     * Get if the legend is PolylineBreak
     * @return Boolean
     */
    public boolean isLine(){
        return this.legendBreaks[0] instanceof PolylineBreak;
    }
    
    /**
     * Get if the legend is PolygonBreak
     * @return Boolean
     */
    public boolean isPolygon(){
        return this.legendBreaks[0] instanceof PolygonBreak;
    }
    
    /**
     * Get if if mutiple legend breaks
     * @return 
     */
    public boolean isMutiple(){
        return this.legendBreaks.length > 1;
    }
    
    /**
     * Get a legend break
     * @return Legend break
     */
    public ColorBreak getLegendBreak(){
        return this.legendBreaks[0];
    }
    
    /**
     * Set legend break
     * @param cb Legend break
     */
    public void setLegendBreak(ColorBreak cb){
        this.legendBreaks = new ColorBreak[1];
        this.legendBreaks[0] = cb;
    }
    
    /**
     * Get a legend break
     * @param idx Index
     * @return Legend break
     */
    public ColorBreak getLegendBreak(int idx){
        return this.legendBreaks[idx];
    }
    
    /**
     * Set legend break
     * @param idx Index
     * @param cb Legend break
     */
    public void setLegendBreak(int idx, ColorBreak cb){
        this.legendBreaks[idx] = cb;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    // </editor-fold>
}
