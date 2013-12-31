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
public class LayoutMapBeanInfo extends BaseBeanInfo {
    public LayoutMapBeanInfo(){
        super(LayoutMap.class);                
        addProperty("backColor").setCategory("General").setDisplayName("Background");
        addProperty("foreColor").setCategory("General").setDisplayName("Foreground");
        addProperty("drawNeatLine").setCategory("Neat Line").setDisplayName("Draw Neat Line");
        addProperty("neatLineColor").setCategory("Neat Line").setDisplayName("Neat Line Color");
        addProperty("neatLineSize").setCategory("Neat Line").setDisplayName("Neat Line Size");
        addProperty("drawGridLine").setCategory("Grid Line").setDisplayName("Draw Grid Line");
        addProperty("drawGridLabel").setCategory("Grid Line").setDisplayName("Draw Grid Label");
        addProperty("gridXDelt").setCategory("Grid Line").setDisplayName("Grid X Interval");
        addProperty("gridYDelt").setCategory("Grid Line").setDisplayName("Grid Y Interval");
        addProperty("gridXOrigin").setCategory("Grid Line").setDisplayName("Grid X Origin");
        addProperty("gridYOrigin").setCategory("Grid Line").setDisplayName("Grid Y Origin");
        addProperty("gridFont").setCategory("Grid Line").setDisplayName("Grid Label Font");
        addProperty("gridLabelShift").setCategory("Grid Line").setDisplayName("Grid Label Shift");
        addProperty("gridLabelPosition").setCategory("Grid Line").setDisplayName("Grid Label Position");
        addProperty("gridLineColor").setCategory("Grid Line").setDisplayName("Grid Line Color");
        addProperty("gridLineSize").setCategory("Grid Line").setDisplayName("Grid Line Size");
        addProperty("gridLineStyle").setCategory("Grid Line").setDisplayName("Grid Line Style");        
        addProperty("insideTickLine").setCategory("Grid Line").setDisplayName("Inside Tick Line");
        addProperty("tickLineLength").setCategory("Grid Line").setDisplayName("Tick Line Length");
        addProperty("left").setCategory("Location").setDisplayName("Left");
        addProperty("top").setCategory("Location").setDisplayName("Top");
        addProperty("width").setCategory("Location").setDisplayName("Width");
        addProperty("height").setCategory("Location").setDisplayName("Height");        
    }
}
