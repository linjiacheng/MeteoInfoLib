 /* Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteoinfo.layout;

import com.l2fprod.common.beans.BaseBeanInfo;

/**
 *
 * @author Yaqiang
 */
public class LayoutScaleBarBeanInfo extends BaseBeanInfo {
    public LayoutScaleBarBeanInfo(){
        super(LayoutScaleBar.class);          
        addProperty("scaleBarType").setCategory("General").setDisplayName("Scale Bar Type");
        addProperty("backColor").setCategory("General").setDisplayName("Background");
        addProperty("foreColor").setCategory("General").setDisplayName("Foreground");
        addProperty("font").setCategory("General").setDisplayName("Font");
        addProperty("drawScaleText").setCategory("General").setDisplayName("Draw Scale Text");
        addProperty("drawNeatLine").setCategory("Neat Line").setDisplayName("Draw Neat Line");
        addProperty("neatLineColor").setCategory("Neat Line").setDisplayName("Neat Line Color");
        addProperty("neatLineSize").setCategory("Neat Line").setDisplayName("Neat Line Size");
        addProperty("left").setCategory("Location").setDisplayName("Left");
        addProperty("top").setCategory("Location").setDisplayName("Top");
        addProperty("width").setCategory("Location").setDisplayName("Width");
        addProperty("height").setCategory("Location").setDisplayName("Height");
    }
}
