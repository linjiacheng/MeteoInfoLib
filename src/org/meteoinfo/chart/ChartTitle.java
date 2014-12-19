/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart;

import java.awt.Color;
import java.awt.Font;

/**
 *
 * @author yaqiang
 */
public class ChartTitle {
    // <editor-fold desc="Variables">
    private Font font;
    private String text;
    private Color color;
    // </editor-fold>    
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public ChartTitle(){
        font = new Font("Arial", Font.BOLD, 14);
        color = Color.black;
    }
    
    /**
     * Constructor
     * @param text Text
     */
    public ChartTitle(String text){
        this();
        this.text = text;
    }
    
    /**
     * Constructor
     * @param text Text
     * @param font Font
     */
    public ChartTitle(String text, Font font){
        this.text = text;
        this.font = font;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get text
     * @return Text
     */
    public String getText(){
        return text;
    }
    
    /**
     * Set text
     * @param value Text
     */
    public void setText(String value){
        text = value;
    }
    
    /**
     * Get font
     * @return Font
     */
    public Font getFont(){
        return font;
    }
    
    /**
     * Set font
     * @param value Font
     */
    public void setFont(Font value){
        font = value;
    }
    
    /**
     * Get title color
     * @return Title color
     */
    public Color getColor(){
        return color;
    }
    
    /**
     * Set title color
     * @param value Title color
     */
    public void setColor(Color value){
        this.color = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    // </editor-fold>
}
