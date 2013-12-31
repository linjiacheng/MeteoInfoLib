/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.global.ui;

import javax.swing.JCheckBox;

/**
 *
 * @author yaqiang
 */
public class CheckBoxListEntry extends JCheckBox {

    private Object value = null;
    private boolean red = false;

    public CheckBoxListEntry(Object itemValue, boolean selected) {
        super(itemValue == null ? "" : "" + itemValue, selected);
        setValue(itemValue);
    }

    @Override
    public boolean isSelected() {
        return super.isSelected();
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isRed() {
        return red;
    }

    public void setRed(boolean red) {
        this.red = red;
    }
}
