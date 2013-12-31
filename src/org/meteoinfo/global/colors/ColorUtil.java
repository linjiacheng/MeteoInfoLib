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
package org.meteoinfo.global.colors;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * ColorUtiles class
 *
 * @author Yaqiang
 */
public class ColorUtil {
    // <editor-fold desc="Variables">

    private final static Map colorNames;

    static {
        // color names.
        colorNames = new HashMap();
        colorNames.put("aliceblue", new Color(0xF0F8FF));
        colorNames.put("antiquewhite", new Color(0xFAEBD7));
        colorNames.put("black", new Color(0x000000));
        colorNames.put("green", new Color(0x008000));
        colorNames.put("silver", new Color(0xC0C0C0));
        colorNames.put("lime", new Color(0x00FF00));
        colorNames.put("gray", new Color(0x808080));
        colorNames.put("darkgray", new Color(0xA9A9A9));
        colorNames.put("olive", new Color(0x808000));
        colorNames.put("white", new Color(0xFFFFFF));
        colorNames.put("yellow", new Color(0xFFFF00));
        colorNames.put("maroon", new Color(0x800000));
        colorNames.put("navy", new Color(0x000080));
        colorNames.put("red", new Color(0xFF0000));
        colorNames.put("blue", new Color(0x0000FF));
        colorNames.put("purple", new Color(0x800080));
        colorNames.put("teal", new Color(0x008080));
        colorNames.put("fuchsia", new Color(0xFF00FF));
        colorNames.put("aqua", new Color(0x00FFFF));
        colorNames.put("transparent", new Color(0, 0, 0, 0));
    }
    // </editor-fold>
    // <editor-fold desc="Constructor">
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Convert a color to hex string
     *
     * @param color a color
     * @return Hex string
     */
    public static String toHexEncoding(Color color) {
        String A, R, G, B;
        StringBuilder sb = new StringBuilder();

        A = Integer.toHexString(color.getAlpha());
        R = Integer.toHexString(color.getRed());
        G = Integer.toHexString(color.getGreen());
        B = Integer.toHexString(color.getBlue());

        A = A.length() == 1 ? "0" + A : A;
        R = R.length() == 1 ? "0" + R : R;
        G = G.length() == 1 ? "0" + G : G;
        B = B.length() == 1 ? "0" + B : B;

        sb.append("0x");
        sb.append(A);
        sb.append(R);
        sb.append(G);
        sb.append(B);

        return sb.toString();
    }

    /**
     * Parse hex string to color
     *
     * @param c hex string
     * @return Color
     */
    public static Color parseToColor(final String c) {
        //Color convertedColor = (Color) colorNames.get(c.trim().toLowerCase());
        Color convertedColor = Color.white;
        try {
            WebColor webColor = WebColor.valueOf(c.trim());
            convertedColor = WebColor.valueOf(c.trim()).getColor();
        } catch (IllegalArgumentException e) {
            try {
                if (c.length() == 10) {
                    String aStr = c.substring(2, 4);
                    String cStr = c.substring(0, 2) + c.substring(4);
                    int alpha = Integer.parseInt(aStr, 16);
                    //int rgb = Integer.parseInt(cStr);
                    convertedColor = Color.decode(cStr);
                    //convertedColor = new Color(rgb);
                    convertedColor = new Color(convertedColor.getRed(), convertedColor.getGreen(), convertedColor.getBlue(), alpha);
                } else {
                    //convertedColor = new Color(Integer.parseInt(c, 16));
                    convertedColor = Color.decode(c);
                }
            } catch (NumberFormatException ne) {
                // codes to deal with this exception
                //convertedColor = Color.white;
            }
        }

        return convertedColor;
    }

    /**
     * Modifies an existing brightness level of a color
     *
     * @param c The color
     * @param brightness The brightness
     * @return Adjusted color
     */
    public static Color modifyBrightness(Color c, float brightness) {
        float hsbVals[] = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        return Color.getHSBColor(hsbVals[0], hsbVals[1], brightness * hsbVals[2]);
    }

    /**
     * Convert color to KML color string - AABBGGRR
     *
     * @param color The color
     * @return KML color string
     */
    public static String toKMLColor(Color color) {
        String A, R, G, B;
        StringBuilder sb = new StringBuilder();

        A = Integer.toHexString(color.getAlpha()).toUpperCase();
        R = Integer.toHexString(color.getRed()).toUpperCase();
        G = Integer.toHexString(color.getGreen()).toUpperCase();
        B = Integer.toHexString(color.getBlue()).toUpperCase();

        A = A.length() == 1 ? "0" + A : A;
        R = R.length() == 1 ? "0" + R : R;
        G = G.length() == 1 ? "0" + G : G;
        B = B.length() == 1 ? "0" + B : B;

        //sb.append("0x");
        sb.append(A);
        sb.append(B);
        sb.append(G);
        sb.append(R);

        return sb.toString();
    }
    // </editor-fold>
}
