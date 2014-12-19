/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart;

/**
 *
 * @author yaqiang
 */
public enum LegendPosition {
    TOP,
    LEFT,
    RIGHT,
    BOTTOM,
    CUSTOM;
    
    /**
     * If the position is custom
     * @return Boolean
     */
    public boolean isCustom(){
        switch (this){
            case CUSTOM:
                return true;
            default:
                return false;
        }
    }
}
