/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.plot3d;

import org.meteoinfo.shape.GraphicCollection;

/**
 *
 * @author Yaqiang Wang
 */
public class GraphicCollection3D extends GraphicCollection{
    
    private boolean fixZ;
    private double zValue;
    
    public GraphicCollection3D(){
        super();
        fixZ = false;
    }
    
    /**
     * Get if is 3D
     * @return Boolean
     */
    @Override
    public boolean is3D(){
        return true;
    }
    
    /**
     * Get if is fixed z graphics
     * @return Boolean
     */
    public boolean isFixZ(){
        return this.fixZ;
    }
    
    /**
     * Set if is fixed z graphics
     * @param value Boolean
     */
    public void setFixZ(boolean value){
        this.fixZ = value;
    }
    
    /**
     * Get fixed z value
     * @return Fixed z value
     */
    public double getZValue(){
        return this.zValue;
    }
    
    /**
     * Set fixed z value
     * @param value Fixed z value
     */
    public void setZValue(double value){
        this.zValue = value;
    }
}
