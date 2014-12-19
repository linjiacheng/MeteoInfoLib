/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data;

import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.PointD;

/**
 *
 * @author yaqiang
 */
public class XYSeriesData extends ArrayList<PointD> {
    private String key;
    
    /**
     * Constructor
     */
    public XYSeriesData(){
        super();        
    }
    
    /**
     * Constructor
     * @param key Key
     */
    public XYSeriesData(String key){
        super();
        this.key = key;
    }
    
    /**
     * Constructor
     * @param key Series key
     * @param values Series values
     */
    public XYSeriesData(String key, List<PointD> values){
        super();
        this.key = key;
        this.addAll(values);
    }
    
    /**
     * Get series key
     * @return Series key
     */
    public String getKey(){
        return key;
    }
    
    /**
     * Set series key
     * @param value Series key
     */
    public void setKey(String value){
        key = value;
    }        
        
}
