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
public class ChartText {
    // <editor-fold desc="Variables">
    private Font font;
    private String text;
    private Color color;
    private float x;
    private float y;
    // </editor-fold>    
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public ChartText(){
        font = new Font("Arial", Font.BOLD, 14);
        color = Color.black;
    }
    
    /**
     * Constructor
     * @param text Text
     */
    public ChartText(String text){
        this();
        this.text = text;
    }
    
    /**
     * Constructor
     * @param text Text
     * @param font Font
     */
    public ChartText(String text, Font font){
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
    
    /**
     * Get x
     * @return X
     */
    public float getX(){
        return this.x;
    }
    
    /**
     * Set x
     * @param value X
     */
    public void setX(float value){
        this.x = value;
    }
    
    /**
     * Get y
     * @return Y
     */
    public float getY(){
        return this.y;
    }
    
    /**
     * Set y
     * @param value Y
     */
    public void setY(float value) {
        this.y = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    // </editor-fold>
}
