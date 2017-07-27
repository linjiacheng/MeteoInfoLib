/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.plot3d;

import org.meteoinfo.legend.LegendScheme;

/**
 *
 * @author Yaqiang Wang
 */
public class Graphic3D {
    private Object graphic;
    private LegendScheme legend;
    
    /**
     * Constructor
     * @param graphic The grahpic
     */
    public Graphic3D(Object graphic){
        this.graphic = graphic;
    }
    
    /**
     * Constructor
     * @param graphic The graphic
     * @param ls The legend scheme
     */
    public Graphic3D(Object graphic, LegendScheme ls){
        this.legend = ls;
        this.graphic = graphic;
    }
    
    /**
     * Get graphic
     * @return The graphic
     */
    public Object getGraphic(){
        return this.graphic;
    }
    
    /**
     * Set graphic
     * @param value The graphic 
     */
    public void setGraphic(Object value){
        this.graphic = value;
    }
    
    /**
     * Get legend
     * @return The legend
     */
    public LegendScheme getLegend(){
        return this.legend;
    }
    
    /**
     * Set legend
     * @param value The legend
     */
    public void setLegend(LegendScheme value){
        this.legend = value;
    }
}
