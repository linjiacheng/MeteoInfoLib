/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.ui;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import org.meteoinfo.global.colors.ColorTable;

/**
 *
 * @author wyq
 */
public class ColorComboBoxModel extends AbstractListModel implements ComboBoxModel {

    ColorTable[] items;
    ColorTable item = null;

    /**
     * Constructor
     *
     * @param value Color tables
     */
    public ColorComboBoxModel(ColorTable[] value) {
        this.items = value;
    }

    @Override
    public Object getSelectedItem() {
        // TODO Auto-generated method stub  
        return this.item;
    }

    @Override
    public void setSelectedItem(Object item) {
        // TODO Auto-generated method stub  
        this.item = (ColorTable) item;
    }

    @Override
    public Object getElementAt(int index) {
        // TODO Auto-generated method stub  
        return this.items[index++];
    }

    @Override
    public int getSize() {
        // TODO Auto-generated method stub  
        return this.items.length;
    }

}
