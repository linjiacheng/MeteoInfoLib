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
package org.meteoinfo.chart;

import org.meteoinfo.drawing.Draw;
import org.meteoinfo.global.PointF;
import org.meteoinfo.legend.ColorBreak;
import org.meteoinfo.legend.LegendScheme;
import org.meteoinfo.legend.PointBreak;
import org.meteoinfo.legend.PolygonBreak;
import org.meteoinfo.legend.PolylineBreak;
import com.l2fprod.common.beans.BaseBeanInfo;
import com.l2fprod.common.beans.ExtendedPropertyDescriptor;
import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import org.meteoinfo.chart.plot.PlotOrientation;
import org.meteoinfo.chart.plot.XY1DPlot;

/**
 *
 * @author Yaqiang Wang
 */
public class ChartLegend {
    // <editor-fold desc="Variables">

    //private final XY1DPlot plot;
    private LegendScheme legendScheme;
    private LegendPosition position;
    private PlotOrientation orientation;
    private Color background;
    private boolean drawBackground;
    private int width;
    private int height;
    private Font labelFont;
    private Color labelColor;
    private boolean _drawNeatLine;
    private Color _neatLineColor;
    private float _neatLineSize;
    private float _breakSpace;
    private float _topSpace;
    private float _leftSpace;
    private float _vBarWidth;
    private float _hBarHeight;
    private int rowColNum = 1;
    private Dimension symbolDimension;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     *
     * @param ls LegendScheme
     */
    public ChartLegend(LegendScheme ls) {
        //this.plot = plot;
        this.legendScheme = ls;
        this.position = LegendPosition.BOTTOM;
        this.orientation = PlotOrientation.HORIZONTAL;
        this.background = Color.white;
        this.drawBackground = false;
        _drawNeatLine = true;
        _neatLineColor = Color.black;
        _neatLineSize = 1;
        _breakSpace = 3;
        _topSpace = 5;
        _leftSpace = 5;
        _vBarWidth = 10;
        _hBarHeight = 10;
        labelFont = new Font("宋体", Font.PLAIN, 12);
        this.labelColor = Color.black;
        this.symbolDimension = new Dimension(16, 10);
    }

    // </editor-fold>
    // <editor-fold desc="Events">
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get legend scheme
     *
     * @return Legend scheme
     */
    public LegendScheme getLegendScheme() {
        return this.legendScheme;
    }

    /**
     * Set legend scheme
     *
     * @param value Legend scheme
     */
    public void setLegendScheme(LegendScheme value) {
        this.legendScheme = value;
    }

    /**
     * Get legend position
     *
     * @return Legend position
     */
    public LegendPosition getPosition() {
        return this.position;
    }

    /**
     * Set legend position
     *
     * @param value Legend position
     */
    public void setPosition(LegendPosition value) {
        this.position = value;
    }

    /**
     * Get plot orientation
     *
     * @return Plot orientation
     */
    public PlotOrientation getPlotOrientation() {
        return this.orientation;
    }

    /**
     * Set plot orientation
     *
     * @param value Plot orientation
     */
    public void setPlotOrientation(PlotOrientation value) {
        this.orientation = value;
    }

    /**
     * Get background
     *
     * @return Background
     */
    public Color getBackground() {
        return this.background;
    }

    /**
     * Set background
     *
     * @param value Background
     */
    public void setBackground(Color value) {
        this.background = value;
    }

    /**
     * Get if draw background
     *
     * @return Boolean
     */
    public boolean isDrawBackground() {
        return this.drawBackground;
    }

    /**
     * Set if draw background
     *
     * @param value Boolean
     */
    public void setDrawBackground(boolean value) {
        this.drawBackground = value;
    }

    /**
     * Get if draw neat line
     *
     * @return If draw neat line
     */
    public boolean isDrawNeatLine() {
        return _drawNeatLine;
    }

    /**
     * Set if draw neat line
     *
     * @param istrue If draw neat line
     */
    public void setDrawNeatLine(boolean istrue) {
        _drawNeatLine = istrue;
    }

    /**
     * Get neat line color
     *
     * @return Neat line color
     */
    public Color getNeatLineColor() {
        return _neatLineColor;
    }

    /**
     * Set neat line color
     *
     * @param color Neat line color
     */
    public void setNeatLineColor(Color color) {
        _neatLineColor = color;
    }

    /**
     * Get neat line size
     *
     * @return Neat line size
     */
    public float getNeatLineSize() {
        return _neatLineSize;
    }

    /**
     * Set neat line size
     *
     * @param size Neat line size
     */
    public void setNeatLineSize(float size) {
        _neatLineSize = size;
    }

    /**
     * Get font
     *
     * @return The font
     */
    public Font getLabelFont() {
        return labelFont;
    }

    /**
     * Set font
     *
     * @param font The font
     */
    public void setLabelFont(Font font) {
        labelFont = font;
    }

    /**
     * Get column number
     *
     * @return Column number
     */
    public int getColumnNumber() {
        return rowColNum;
    }

    /**
     * Set column number
     *
     * @param value Column number
     */
    public void setColumnNumber(int value) {
        rowColNum = value;
    }

    /**
     * Get symbol dimension
     *
     * @return Symbol dimension
     */
    public Dimension getSymbolDimension() {
        return this.symbolDimension;
    }

    /**
     * Set symbol dimension
     *
     * @param value Symbol dimension
     */
    public void setSymbolDimension(Dimension value) {
        this.symbolDimension = value;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Draw legend
     *
     * @param g Graphics2D
     * @param point Start point
     */
    public void draw(Graphics2D g, PointF point) {

        AffineTransform oldMatrix = g.getTransform();
        g.translate(point.X, point.Y);

        //Draw background color
        if (this.drawBackground) {
            g.setColor(this.background);
            g.fill(new Rectangle.Float(0, 0, this.width, this.height));
        }

        //Draw legend
        switch (this.orientation) {
            case HORIZONTAL:
                drawHorizontalLegend(g, legendScheme);
                break;
            case VERTICAL:
                this.drawVerticalLegend(g, legendScheme);
                break;
        }

        //Draw neatline
        if (_drawNeatLine) {
            Rectangle.Float mapRect = new Rectangle.Float(0, 0, this.width, this.height);
            g.setColor(_neatLineColor);
            g.setStroke(new BasicStroke(_neatLineSize));
            g.draw(mapRect);
        }

        g.setTransform(oldMatrix);
    }

    private void drawVerticalLegend(Graphics2D g, LegendScheme aLS) {
        String caption = "";
        Dimension aSF;
        float leftSpace = _leftSpace;
        float breakSpace = _breakSpace;
        float breakHeight = this.getBreakHeight(g);
        float symbolHeight = this.symbolDimension.height;
        float symbolWidth = this.symbolDimension.width;
        float colWidth = symbolWidth + getMaxLabelWidth(g) + 10;

        //Set columns
        int[] rowNums = new int[rowColNum];
        int ave = aLS.getVisibleBreakNum() / rowColNum;
        if (ave * rowColNum < aLS.getBreakNum()) {
            ave += 1;
        }
        int num = 0;
        int i;
        for (i = 1; i < rowColNum; i++) {
            rowNums[i] = ave;
            num += ave;
        }
        rowNums[0] = aLS.getVisibleBreakNum() - num;

        //Draw legend                        
        Font lFont = new Font(this.labelFont.getFontName(), this.labelFont.getStyle(), (int) (this.labelFont.getSize()));
        float x, y;
        i = 0;
        for (int col = 0; col < rowColNum; col++) {
            x = symbolWidth / 2 + leftSpace + col * colWidth;
            y = 0;
            for (int row = 0; row < rowNums[col]; row++) {
                if (!aLS.getLegendBreaks().get(i).isDrawShape()) {
                    continue;
                }

                y += breakHeight + breakSpace;
                switch (aLS.getShapeType()) {
                    case Point:
                        PointBreak aPB = (PointBreak) ((PointBreak) aLS.getLegendBreaks().get(i)).clone();
                        caption = aPB.getCaption();
                        aPB.setSize(aPB.getSize());
                        Draw.drawPoint(new PointF(x, y), aPB, g);
                        break;
                    case Polyline:
                    case PolylineZ:
                        PolylineBreak aPLB = (PolylineBreak) aLS.getLegendBreaks().get(i);
                        caption = aPLB.getCaption();
                        Draw.drawPolylineSymbol_S(new PointF(x, y), symbolWidth, symbolHeight, aPLB, g);
                        break;
                    case Polygon:
                        PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(i);
                        caption = aPGB.getCaption();
                        Draw.drawPolygonSymbol(new PointF(x, y), symbolHeight * 8 / 10, symbolHeight * 8 / 10, aPGB, g);
                        break;
                    case Image:
                        ColorBreak aCB = aLS.getLegendBreaks().get(i);
                        caption = aCB.getCaption();
                        Draw.drawPolygonSymbol(new PointF(x, y), aCB.getColor(), Color.black, symbolWidth,
                                symbolHeight, true, true, g);
                        break;
                }

                PointF sP = new PointF(0, 0);
                sP.X = x + symbolWidth / 2;
                sP.Y = y;
                FontMetrics metrics = g.getFontMetrics(lFont);
                aSF = new Dimension(metrics.stringWidth(caption), metrics.getHeight());
                g.setColor(this.labelColor);
                g.setFont(lFont);
                //g.drawString(caption, sP.X + 5, sP.Y + aSF.height / 3);
                g.drawString(caption, sP.X + 5, sP.Y + aSF.height / 4);

                i += 1;
            }
        }
    }

    private void drawHorizontalLegend(Graphics2D g, LegendScheme aLS) {
        String caption;
        float breakHeight = this.getBreakHeight(g);
        float symbolHeight = this.symbolDimension.height;
        float symbolWidth = this.symbolDimension.width;
        FontMetrics metrics = g.getFontMetrics(labelFont);

        //Set columns
        int[] colNums = new int[rowColNum];
        int ave = aLS.getVisibleBreakNum() / rowColNum;
        if (ave * rowColNum < aLS.getBreakNum()) {
            ave += 1;
        }
        int num = 0;
        int i;
        for (i = 0; i < rowColNum - 1; i++) {
            colNums[i] = ave;
            num += ave;
        }
        colNums[rowColNum - 1] = aLS.getVisibleBreakNum() - num;

        //Draw legend                        
        float x, y;
        y = this._breakSpace + breakHeight / 2;
        i = 0;
        for (int row = 0; row < rowColNum; row++) {
            x = this.symbolDimension.width / 2 + 5;
            for (int col = 0; col < colNums[row]; col++) {
                if (i >= aLS.getBreakNum()) {
                    break;
                }

                ColorBreak cb = aLS.getLegendBreaks().get(i);
                if (!cb.isDrawShape()) {
                    continue;
                }
                caption = aLS.getLegendBreaks().get(i).getCaption();
                if (cb instanceof PointBreak) {
                    PointBreak aPB = (PointBreak) cb;
                    Draw.drawPoint(new PointF(x, y), aPB, g);
                } else if (cb instanceof PolylineBreak) {
                    PolylineBreak aPLB = (PolylineBreak) cb;
                    Draw.drawPolylineSymbol_S(new PointF(x, y), symbolWidth, symbolHeight, aPLB, g);
                } else if (cb instanceof PolygonBreak) {
                    Draw.drawPolygonSymbol(new PointF(x, y), cb.getColor(), Color.black, symbolWidth,
                            symbolHeight, true, true, g);
                }
//                switch (aLS.getShapeType()) {
//                    case Point:
//                        PointBreak aPB = (PointBreak) cb;
//                        Draw.drawPoint(new PointF(x, y), aPB, g);
//                        break;
//                    case Polyline:
//                    case PolylineZ:
//                        PolylineBreak aPLB = (PolylineBreak) cb;
//                        Draw.drawPolylineSymbol_S(new PointF(x, y), symbolWidth, symbolHeight, aPLB, g);
//                        break;
//                    case Polygon:
//                        PolygonBreak aPGB = (PolygonBreak) cb;
//                        Draw.drawPolygonSymbol(new PointF(x, y), symbolHeight * 8 / 10, symbolHeight * 8 / 10, aPGB, g);
//                        break;
//                    case Image:
//                        Draw.drawPolygonSymbol(new PointF(x, y), cb.getColor(), Color.black, symbolWidth,
//                                symbolHeight, true, true, g);
//                        break;
//                }

                PointF sP = new PointF(0, 0);
                sP.X = x + symbolWidth / 2;
                sP.Y = y;
                g.setColor(this.labelColor);
                g.setFont(this.labelFont);
                //g.drawString(caption, sP.X + 5, sP.Y + aSF.height / 3);
                g.drawString(caption, sP.X + 5, sP.Y + metrics.getHeight() / 4);

                x += this.symbolDimension.width + metrics.stringWidth(caption) + 15;
                i += 1;
            }
            y += breakHeight + this._breakSpace * 2;
        }
    }

    private int getMaxLabelWidth(Graphics2D g) {
        String caption = "";
        Dimension aSF;
        int bNum = legendScheme.getBreakNum();
        FontMetrics metrics = g.getFontMetrics(labelFont);
        aSF = new Dimension(metrics.stringWidth(caption), metrics.getHeight());
        int labWidth = aSF.width;
        for (int i = 0; i < bNum; i++) {
            caption = legendScheme.getLegendBreaks().get(i).getCaption();
            boolean isValid = true;
            if (isValid) {
                int labwidth = metrics.stringWidth(caption);
                if (labWidth < labwidth) {
                    labWidth = labwidth;
                }
            }
        }

        return labWidth;
    }

    private int getBreakHeight(Graphics2D g) {
        FontMetrics metrics = g.getFontMetrics(labelFont);
        return Math.max(metrics.getHeight(), this.symbolDimension.height);
    }

    /**
     * Get legend dimension
     *
     * @param g Graphics2D
     * @param limitDim Limit dimension
     * @return Legend dimension
     */
    public Dimension getLegendDimension(Graphics2D g, Dimension limitDim) {
        if (legendScheme != null) {
            int breakHeight = getBreakHeight(g);
            switch (this.orientation) {
                case VERTICAL:
                    //Get column number
                    int tHeight = (int) (legendScheme.getBreakNum() * (breakHeight + _breakSpace)
                            + _breakSpace * 2 + breakHeight / 2 + 5);
                    rowColNum = 1;
                    if (tHeight > limitDim.height * 10 / 8) {
                        rowColNum = tHeight / (limitDim.height * 10 / 8) + 1;
                        if (rowColNum == 1) {
                            rowColNum = 2;
                        } else {
                            int n = legendScheme.getBreakNum() / rowColNum;
                            int m = legendScheme.getBreakNum() % rowColNum;
                            if (m != 0) {
                                if (m <= n) {
                                    rowColNum += 1;
                                } else {
                                    rowColNum += 2;
                                }
                            } else {
                                if (rowColNum * (limitDim.width * 8 / 10) < tHeight) {
                                    rowColNum += 1;
                                }
                            }
                        }
                    }

                    //Get width
                    int colWidth = this.symbolDimension.width + getMaxLabelWidth(g) + 15;
                    this.width = colWidth * rowColNum;

                    //Get height
                    int[] rowNums = new int[rowColNum];
                    int ave = legendScheme.getBreakNum() / rowColNum;
                    if (ave * rowColNum < legendScheme.getBreakNum()) {
                        ave += 1;
                    }
                    int num = 0;
                    int i;
                    for (i = 0; i < rowColNum - 1; i++) {
                        rowNums[i] = ave;
                        num += ave;
                    }
                    rowNums[rowColNum - 1] = legendScheme.getBreakNum() - num;

                    this.height = (int) (rowNums[0] * (breakHeight + _breakSpace)
                            + _breakSpace * 2 + breakHeight / 2 + 5);
                    break;
                case HORIZONTAL:
                    //Get row number
                    int breakWidth = this.symbolDimension.width + this.getMaxLabelWidth(g) + 15;
                    int tWidth = breakWidth * legendScheme.getBreakNum();
                    rowColNum = 1;
                    if (tWidth > limitDim.width * 8 / 10) {
                        rowColNum = tWidth / (limitDim.width * 8 / 10);
                        if (rowColNum == 1) {
                            rowColNum = 2;
                        } else {
                            int n = legendScheme.getBreakNum() / rowColNum;
                            int m = legendScheme.getBreakNum() % rowColNum;
                            if (m != 0) {
                                if (m <= n) {
                                    rowColNum += 1;
                                } else {
                                    rowColNum += 2;
                                }
                            } else {
                                if (rowColNum * (limitDim.width * 8 / 10) < tWidth) {
                                    rowColNum += 1;
                                }
                            }
                        }
                    }

                    //Get height
                    this.height = (int) (breakHeight + this._breakSpace * 2) * this.rowColNum;

                    //Get width
                    FontMetrics metrics = g.getFontMetrics(labelFont);
                    ave = legendScheme.getBreakNum() / rowColNum;
                    if (ave * rowColNum < legendScheme.getBreakNum()) {
                        ave += 1;
                    }
                    num = 0;
                    int maxWidth = 0;
                    int tempWidth = 0;
                    for (i = 0; i < legendScheme.getBreakNum(); i++) {
                        if (num < ave) {
                            tempWidth += this.symbolDimension.width + 15
                                    + metrics.stringWidth(legendScheme.getLegendBreaks().get(i).getCaption());
                            num += 1;
                        } else {
                            if (maxWidth < tempWidth) {
                                maxWidth = tempWidth;
                            }
                            tempWidth = metrics.stringWidth(legendScheme.getLegendBreaks().get(i).getCaption()) + 15;
                            num = 1;
                        }
                    }
                    if (maxWidth < tempWidth) {
                        maxWidth = tempWidth;
                    }
                    if (maxWidth > limitDim.width) {
                        maxWidth = limitDim.width * 8 / 10;
                    }
                    this.width = maxWidth;
                    break;
            }
        }

        return new Dimension(this.width, this.height);
    }

    // </editor-fold>      
    // <editor-fold desc="BeanInfo">
    public class LayoutLegendBean {

        LayoutLegendBean() {
        }

        // <editor-fold desc="Get Set Methods">        
        /**
         * Get if draw neat line
         *
         * @return If draw neat line
         */
        public boolean isDrawNeatLine() {
            return _drawNeatLine;
        }

        /**
         * Set if draw neat line
         *
         * @param istrue If draw neat line
         */
        public void setDrawNeatLine(boolean istrue) {
            _drawNeatLine = istrue;
        }

        /**
         * Get neat line color
         *
         * @return Neat line color
         */
        public Color getNeatLineColor() {
            return _neatLineColor;
        }

        /**
         * Set neat line color
         *
         * @param color Neat line color
         */
        public void setNeatLineColor(Color color) {
            _neatLineColor = color;
        }

        /**
         * Get neat line size
         *
         * @return Neat line size
         */
        public float getNeatLineSize() {
            return _neatLineSize;
        }

        /**
         * Set neat line size
         *
         * @param size Neat line size
         */
        public void setNeatLineSize(float size) {
            _neatLineSize = size;
        }

        /**
         * Get font
         *
         * @return The font
         */
        public Font getLabelFont() {
            return labelFont;
        }

        /**
         * Set font
         *
         * @param font The font
         */
        public void setLabelFont(Font font) {
            labelFont = font;
        }

        /**
         * Get column number
         *
         * @return Column number
         */
        public int getColumnNumber() {
            return rowColNum;
        }

        /**
         * Set column number
         *
         * @param value Column number
         */
        public void setColumnNumber(int value) {
            rowColNum = value;
        }

        /**
         * Get is draw background
         *
         * @return Boolean
         */
        public boolean isDrawBackground() {
            return drawBackground;
        }

        /**
         * Set is draw background
         *
         * @param value Boolean
         */
        public void setDrawBackground(boolean value) {
            drawBackground = value;
        }

        /**
         * Get background color
         *
         * @return Background color
         */
        public Color getBackground() {
            return background;
        }

        /**
         * Set background color
         *
         * @param c Background color
         */
        public void setBackground(Color c) {
            background = c;
        }

        // </editor-fold>
    }

    public static class LayoutLegendBeanBeanInfo extends BaseBeanInfo {

        public LayoutLegendBeanBeanInfo() {
            super(LayoutLegendBean.class);
            ExtendedPropertyDescriptor e = addProperty("plotOrientation");
            e.setCategory("General").setDisplayName("Plot orientation");
            e.setPropertyEditorClass(PlotOrientationEditor.class);
            addProperty("labelFont").setCategory("General").setDisplayName("Label Font");
            addProperty("drawBackground").setCategory("General").setDisplayName("Draw Background");
            addProperty("background").setCategory("General").setDisplayName("Background");
            addProperty("columnNumber").setCategory("General").setDisplayName("Column Number");
            addProperty("drawNeatLine").setCategory("Neat Line").setDisplayName("Draw Neat Line");
            addProperty("neatLineColor").setCategory("Neat Line").setDisplayName("Neat Line Color");
            addProperty("neatLineSize").setCategory("Neat Line").setDisplayName("Neat Line Size");
        }
    }

    public static class PlotOrientationEditor extends ComboBoxPropertyEditor {

        public PlotOrientationEditor() {
            super();
            PlotOrientation[] orientations = PlotOrientation.values();
            String[] types = new String[orientations.length];
            int i = 0;
            for (PlotOrientation type : orientations) {
                types[i] = type.toString();
                i += 1;
            }
            setAvailableValues(types);
        }
    }

    // </editor-fold>
}
