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

import org.meteoinfo.drawing.Draw;
import org.meteoinfo.global.event.IMapViewUpdatedListener;
import org.meteoinfo.global.event.MapViewUpdatedEvent;
import org.meteoinfo.global.PointF;
import org.meteoinfo.layer.LayerDrawType;
import org.meteoinfo.layer.LayerTypes;
import org.meteoinfo.layer.MapLayer;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.legend.ChartBreak;
import org.meteoinfo.legend.ColorBreak;
import org.meteoinfo.legend.LegendScheme;
import org.meteoinfo.legend.LegendType;
import org.meteoinfo.legend.PointBreak;
import org.meteoinfo.legend.PolygonBreak;
import org.meteoinfo.legend.PolylineBreak;
import org.meteoinfo.shape.ShapeTypes;
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
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.legend.ChartTypes;

/**
 *
 * @author Yaqiang Wang
 */
public class LayoutLegend extends LayoutElement {
    // <editor-fold desc="Variables">

    private MapLayout _mapLayout;
    private static LayoutMap _layoutMap;
    private MapLayer _legendLayer;
    private boolean _isAntiAlias;
    private LayerUpdateTypes _layerUpdateType;
    private LegendStyles _legendStyle;
    private String _title;
    private Font _font;
    private Font _titleFont;
    private boolean _drawNeatLine;
    private Color _neatLineColor;
    private float _neatLineSize;
    private float _breakSpace;
    private float _topSpace;
    private float _leftSpace;
    private float _vBarWidth;
    private float _hBarHeight;
    private int _columnNum = 1;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     *
     * @param mapLayout Map layout
     * @param layoutMap Layout map
     */
    public LayoutLegend(MapLayout mapLayout, LayoutMap layoutMap) {
        super();
        this.setElementType(ElementType.LayoutLegend);
        this.setResizeAbility(ResizeAbility.None);

        _mapLayout = mapLayout;
        _layoutMap = layoutMap;
        _layoutMap.addMapViewUpdatedListener(new IMapViewUpdatedListener() {
            @Override
            public void mapViewUpdatedEvent(MapViewUpdatedEvent event) {
                onMapViewUpdated(event);
            }
        });
        _isAntiAlias = true;
        _layerUpdateType = LayerUpdateTypes.FirstMeteoLayer;
        _legendStyle = LegendStyles.Normal;
        _title = "";
        _drawNeatLine = false;
        _neatLineColor = Color.black;
        _neatLineSize = 1;
        _breakSpace = 3;
        _topSpace = 5;
        _leftSpace = 5;
        _vBarWidth = 10;
        _hBarHeight = 10;
        _font = new Font("宋体", Font.PLAIN, 12);
        _titleFont = new Font("Arial", Font.PLAIN, 12);
    }

    // </editor-fold>
    // <editor-fold desc="Events">
    public void onMapViewUpdated(MapViewUpdatedEvent e) {
        if (_layoutMap.getMapFrame().getMapView().getLayerNum() == 0) {
            return;
        }

        switch (_layerUpdateType) {
            case FirstExpandedLayer:
                for (int i = 0; i < _layoutMap.getMapFrame().getMapView().getLayerNum(); i++) {
                    MapLayer aLayer = _layoutMap.getMapFrame().getMapView().getLayers().
                            get(_layoutMap.getMapFrame().getMapView().getLayerNum() - 1 - i);                    
                    if (aLayer.hasLegendScheme()) {
                        if (aLayer.isVisible() && aLayer.isExpanded() && aLayer.getLegendScheme().getLegendType() != LegendType.SingleSymbol) {
                            this.setVisible(true);
                            this.setLegendLayer(aLayer);
                            break;
                        }
                    }
                }
                break;
            case FirstMeteoLayer:
                for (int i = 0; i < _layoutMap.getMapFrame().getMapView().getLayerNum(); i++) {
                    MapLayer aLayer = _layoutMap.getMapFrame().getMapView().getLayers().
                            get(_layoutMap.getMapFrame().getMapView().getLayerNum() - 1 - i);
                    if (aLayer.hasLegendScheme()) {
                        if (aLayer.isVisible() && aLayer.getLayerDrawType() != LayerDrawType.Map
                                && aLayer.getLegendScheme().getLegendType() != LegendType.SingleSymbol) {
                            this.setVisible(true);
                            this.setLegendLayer(aLayer);
                            break;
                        }
                    }
                }
                break;
            case LastAddedLayer:
                if (_layoutMap.getMapFrame().getMapView().getLastAddedLayer().hasLegendScheme()) {
                    this.setVisible(true);
                    this.setLegendLayer(_layoutMap.getMapFrame().getMapView().getLastAddedLayer());
                }
                break;
        }

        updateLegendSize();
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get layout map
     *
     * @return The layout map
     */
    public LayoutMap getLayoutMap() {
        return _layoutMap;
    }

    /**
     * Get legend layer
     *
     * @return The legend alyer
     */
    public MapLayer getLegendLayer() {
        return _legendLayer;
    }

    /**
     * Set legend layer
     *
     * @param layer The legend layer
     */
    public void setLegendLayer(MapLayer layer) {
        if (layer == null)
            return;
        
        _legendLayer = layer;
        String aStr = _legendLayer.getLayerName();
        if (aStr.contains("_")) {
            aStr = aStr.split("_")[1];
        }
        _title = aStr;
        updateLegendSize();
    }

    /**
     * Get legend layer name
     *
     * @return Legend layer name
     */
    public String getLayerName() {
        if (_legendLayer != null)
            return _legendLayer.getLayerName();
        else
            return "";
    }

    /**
     * Set legend layer name
     *
     * @param name Layer name
     */
    public void setLayerName(String name) {
        MapLayer aLayer = _layoutMap.getMapFrame().getMapView().getLayer(name);
        if (aLayer != null) {
            _legendLayer = aLayer;
        }
    }

    /**
     * Get layer update type
     *
     * @return Layer update type
     */
    public LayerUpdateTypes getLayerUpdateType() {
        return _layerUpdateType;
    }

    /**
     * Set layer update type
     *
     * @param type Layer update type
     */
    public void setLayerUpdateType(LayerUpdateTypes type) {
        _layerUpdateType = type;
    }

    /**
     * Get legend style
     *
     * @return Legend style
     */
    public LegendStyles getLegendStyle() {
        return _legendStyle;
    }

    /**
     * Set legend style
     *
     * @param style Legend style
     */
    public void setLegendStyle(LegendStyles style) {
        _legendStyle = style;
        if (this.isVisible()) {
            updateLegendSize();
        }
    }

    /**
     * Get title
     *
     * @return The title
     */
    public String getTitle() {
        return _title;
    }

    /**
     * Set title
     *
     * @param title The title
     */
    public void setTitle(String title) {
        _title = title;
        updateLegendSize();
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
    public Font getFont() {
        return _font;
    }

    /**
     * Set font
     *
     * @param font The font
     */
    public void setFont(Font font) {
        _font = font;
        _titleFont = new Font(_font.getFontName(), Font.PLAIN, _font.getSize() + 2);
        updateLegendSize();
    }

    /**
     * Get column number
     *
     * @return Column number
     */
    public int getColumnNumber() {
        return _columnNum;
    }

    /**
     * Set column number
     *
     * @param value Column number
     */
    public void setColumnNumber(int value) {
        _columnNum = value;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Paint graphics
     *
     * @param g Graphics2D
     * @param pageLocation Page location
     * @param zoom Zoom
     */
    public void paintGraphics(Graphics2D g, PointF pageLocation, float zoom) {
        if (_legendLayer == null) {
            return;
        }

        if (_legendLayer.getLayerType() == LayerTypes.ImageLayer) {
            return;
        }

        AffineTransform oldMatrix = g.getTransform();
        PointF aP = pageToScreen(this.getLeft(), this.getTop(), pageLocation, zoom);
        g.translate(aP.X, aP.Y);
        g.scale(zoom, zoom);
        if (this._isAntiAlias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        } else {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }

        //Draw background color
        g.setColor(this.getBackColor());
        g.fill(new Rectangle.Float(0, 0, this.getWidth() * zoom, this.getHeight() * zoom));

        switch (_legendStyle) {
            case Bar_Horizontal:
                drawHorizontalBarLegend(g, zoom);
                break;
            case Bar_Vertical:
                drawVerticalBarLegend(g, zoom);
                break;
            case Normal:
                drawNormalLegend(g, zoom);
                break;
        }

        //Draw neatline
        if (_drawNeatLine) {
            Rectangle.Float mapRect = new Rectangle.Float(_neatLineSize - 1, _neatLineSize - 1,
                    (this.getWidth() - _neatLineSize) * zoom, (this.getHeight() - _neatLineSize) * zoom);
            g.setColor(_neatLineColor);
            g.setStroke(new BasicStroke(_neatLineSize));
            g.draw(mapRect);
        }

        g.setTransform(oldMatrix);
    }

    private void drawChartLegend(Graphics2D g, float zoom, PointF aPoint) {
        VectorLayer aLayer = (VectorLayer) _legendLayer;
        ChartBreak aCB = ((ChartBreak) aLayer.getChartPoints().get(0).getLegend()).getSampleChartBreak();

        //Draw chart symbol
        aPoint.X = 5;
        aPoint.Y += aCB.getHeight();
        switch (aCB.getChartType()) {
            case BarChart:
                Draw.drawBarChartSymbol(aPoint, aCB, g, true);
                break;
            case PieChart:
                Draw.drawPieChartSymbol(aPoint, aCB, g);
                aPoint.Y += aCB.getHeight();
                break;
        }
        aPoint.Y += _breakSpace;

        //Draw breaks
        LegendScheme aLS = aCB.getLegendScheme();
        drawNormalLegend(g, zoom, aLS, aPoint, false);
    }

    private void drawNormalLegend(Graphics2D g, float zoom) {
        LegendScheme aLS = _legendLayer.getLegendScheme();
        PointF aP = new PointF(0, 0);
        drawNormalLegend(g, zoom, aLS, aP, true);
        float height = getBreakHeight(g) * zoom;
        aP.Y += height + _breakSpace;

        //Draw chart legend
        if (_legendLayer.getLayerType() == LayerTypes.VectorLayer) {
            if (((VectorLayer) _legendLayer).getChartSet().isDrawCharts()) {
                drawChartLegend(g, zoom, aP);
            }
        }
    }

    private void drawNormalLegend(Graphics2D g, float zoom, LegendScheme aLS, PointF aP, boolean drawTitle) {
        String caption = "";
        Dimension aSF;
        float leftSpace = _leftSpace * zoom;
        float topSpace = _topSpace * zoom;
        float breakSpace = _breakSpace * zoom;
        float height = getBreakHeight(g) * zoom;
        float width = height * 2;
        float colWidth = getBreakHeight(g) * 2 + getLabelWidth(g) + 10;

        //Draw title
        if (drawTitle) {
            Font tFont = new Font(_titleFont.getFontName(), _titleFont.getStyle(), (int) (_titleFont.getSize() * zoom));
            String Title = _title;
            aP.X = leftSpace;
            aP.Y = leftSpace;
            FontMetrics metrics = g.getFontMetrics(tFont);
            aSF = new Dimension(metrics.stringWidth(Title), metrics.getHeight());
            float titleHeight = aSF.height;
            g.setColor(this.getForeColor());
            g.setFont(tFont);
            g.drawString(_title, aP.X, aP.Y + aSF.height * 3 / 4);
            aP.Y += titleHeight + breakSpace - height / 2;
        }

        //Set columns
        int[] rowNums = new int[_columnNum];
        int ave = aLS.getVisibleBreakNum() / _columnNum;
        int num = 0;
        int i;
        for (i = 1; i < _columnNum; i++) {
            rowNums[i] = ave;
            num += ave;
        }
        rowNums[0] = aLS.getVisibleBreakNum() - num;

        //Draw legend                        
        Font lFont = new Font(this.getFont().getFontName(), this.getFont().getStyle(), (int) (this.getFont().getSize() * zoom));
        float sX = aP.X;
        float sY = aP.Y;
        i = 0;
        for (int col = 0; col < _columnNum; col++) {
            aP.X = width / 2 + leftSpace + col * colWidth;
            aP.Y = sY;
            for (int row = 0; row < rowNums[col]; row++) {
                if (!aLS.getLegendBreaks().get(i).isDrawShape()) {
                    continue;
                }

                aP.Y += height + breakSpace;
                boolean isVisible = true;
                switch (aLS.getShapeType()) {
                    case Point:
                        PointBreak aPB = (PointBreak) ((PointBreak) aLS.getLegendBreaks().get(i)).clone();
                        caption = aPB.getCaption();
                        aPB.setSize(aPB.getSize() * zoom);
                        Draw.drawPoint((PointF) aP.clone(), aPB, g);
                        break;
                    case Polyline:
                    case PolylineZ:
                        PolylineBreak aPLB = (PolylineBreak) aLS.getLegendBreaks().get(i);
                        caption = aPLB.getCaption();
                        Draw.drawPolylineSymbol((PointF) aP.clone(), width, height, aPLB, g);
                        break;
                    case Polygon:
                        PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(i);
                        caption = aPGB.getCaption();
                        Draw.drawPolygonSymbol((PointF) aP.clone(), width, height, aPGB, g);
                        break;
                    case Image:
                        ColorBreak aCB = aLS.getLegendBreaks().get(i);
                        caption = aCB.getCaption();
                        Draw.drawPolygonSymbol((PointF) aP.clone(), aCB.getColor(), Color.black, width,
                                height, true, true, g);
                        break;
                }

                PointF sP = new PointF(0, 0);
                sP.X = aP.X + width / 2;
                sP.Y = aP.Y;
                FontMetrics metrics = g.getFontMetrics(lFont);
                aSF = new Dimension(metrics.stringWidth(caption), metrics.getHeight());
                g.setColor(this.getForeColor());
                g.setFont(lFont);
                //g.drawString(caption, sP.X + 5, sP.Y + aSF.height / 3);
                g.drawString(caption, sP.X + 5, sP.Y + aSF.height / 4);

                i += 1;
            }
        }
    }

    private void drawVerticalBarLegend(Graphics2D g, float zoom) {
        LegendScheme aLS = _legendLayer.getLegendScheme();
        PointF aP = new PointF(0, 0);
        PointF sP = new PointF(0, 0);
        boolean DrawShape = true, DrawFill = true, DrawOutline = true;
        Color FillColor = Color.red, OutlineColor = this.getForeColor();
        String caption = "";
        Dimension aSF;

        int bNum = aLS.getBreakNum();
        if (aLS.getLegendBreaks().get(bNum - 1).isNoData()) {
            bNum -= 1;
        }

        float width = _vBarWidth * zoom;
        float height = (this.getHeight() - 5) * zoom / bNum;
        Font lFont = new Font(this.getFont().getFontName(), this.getFont().getStyle(), (int) (this.getFont().getSize() * zoom));

        for (int i = 0; i < bNum; i++) {
            switch (aLS.getShapeType()) {
                case Point:
                    PointBreak aPB = (PointBreak) aLS.getLegendBreaks().get(i);
                    DrawShape = aPB.isDrawShape();
                    DrawFill = aPB.getDrawFill();
                    FillColor = aPB.getColor();
                    if (aLS.getLegendType() == LegendType.UniqueValue) {
                        caption = aPB.getCaption();
                    } else {
                        caption = DataConvert.removeTailingZeros(aPB.getEndValue().toString());
                    }
                    break;
                case Polyline:
                    PolylineBreak aPLB = (PolylineBreak) aLS.getLegendBreaks().get(i);
                    DrawShape = aPLB.getDrawPolyline();
                    FillColor = aPLB.getColor();
                    if (aLS.getLegendType() == LegendType.UniqueValue) {
                        caption = aPLB.getCaption();
                    } else {
                        caption = DataConvert.removeTailingZeros(aPLB.getEndValue().toString());
                    }
                    break;
                case Polygon:
                    PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(i);
                    DrawShape = aPGB.isDrawShape();
                    DrawFill = aPGB.getDrawFill();
                    FillColor = aPGB.getColor();
                    if (aLS.getLegendType() == LegendType.UniqueValue) {
                        caption = aPGB.getCaption();
                    } else {
                        caption = DataConvert.removeTailingZeros(aPGB.getEndValue().toString());
                    }
                    break;
                case Image:
                    ColorBreak aCB = aLS.getLegendBreaks().get(i);
                    DrawShape = true;
                    DrawFill = true;
                    FillColor = aCB.getColor();
                    if (aLS.getLegendType() == LegendType.UniqueValue) {
                        caption = aCB.getCaption();
                    } else {
                        caption = DataConvert.removeTailingZeros(aCB.getEndValue().toString());
                    }
                    break;
            }

            aP.X = width / 2;
            aP.Y = i * height + height / 2;

            if (aLS.getLegendType() == LegendType.UniqueValue) {
                if (DrawShape) {
                    if (aLS.getShapeType() == ShapeTypes.Polygon) {
                        PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(i).clone();
                        aPGB.setDrawOutline(true);
                        aPGB.setOutlineColor(Color.black);
                        Draw.drawPolygonSymbol((PointF) aP.clone(), width, height, aPGB, g);
                    } else {
                        Draw.drawPolygonSymbol((PointF) aP.clone(), FillColor, OutlineColor, width,
                                height, DrawFill, DrawOutline, g);
                    }
                }

                sP.X = aP.X + width / 2 + 5;
                sP.Y = aP.Y;
                FontMetrics metrics = g.getFontMetrics(lFont);
                aSF = new Dimension(metrics.stringWidth(caption), metrics.getHeight());
                g.setColor(this.getForeColor());
                g.setFont(lFont);
                g.drawString(caption, sP.X, sP.Y + aSF.height / 2);
            } else {
                if (DrawShape) {
                    if (i == 0) {
                        PointF[] Points = new PointF[4];
                        Points[0] = new PointF();
                        Points[0].X = aP.X;
                        Points[0].Y = 0;
                        Points[1] = new PointF();
                        Points[1].X = 0;
                        Points[1].Y = height;
                        Points[2] = new PointF();
                        Points[2].X = width;
                        Points[2].Y = height;
                        Points[3] = new PointF();
                        Points[3].X = aP.X;
                        Points[3].Y = 0;
                        if (aLS.getShapeType() == ShapeTypes.Polygon) {
                            PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(i).clone();
                            aPGB.setDrawOutline(true);
                            aPGB.setOutlineColor(Color.black);
                            Draw.drawPolygon(Points, aPGB, g);
                        } else {
                            Draw.drawPolygon(Points, FillColor, OutlineColor, DrawFill, DrawOutline, g);
                        }
                    } else if (i == bNum - 1) {
                        PointF[] Points = new PointF[4];
                        Points[0] = new PointF();
                        Points[0].X = 0;
                        Points[0].Y = i * height;
                        Points[1] = new PointF();
                        Points[1].X = width;
                        Points[1].Y = i * height;
                        Points[2] = new PointF();
                        Points[2].X = aP.X;
                        Points[2].Y = i * height + height;
                        Points[3] = new PointF();
                        Points[3].X = 0;
                        Points[3].Y = i * height;
                        if (aLS.getShapeType() == ShapeTypes.Polygon) {
                            PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(i).clone();
                            aPGB.setDrawOutline(true);
                            aPGB.setOutlineColor(Color.black);
                            Draw.drawPolygon(Points, aPGB, g);
                        } else {
                            Draw.drawPolygon(Points, FillColor, OutlineColor, DrawFill, DrawOutline, g);
                        }
                    } else {
                        if (aLS.getShapeType() == ShapeTypes.Polygon) {
                            PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(i).clone();
                            aPGB.setDrawOutline(true);
                            aPGB.setOutlineColor(Color.black);
                            Draw.drawPolygonSymbol((PointF) aP.clone(), width, height, aPGB, g);
                        } else {
                            Draw.drawPolygonSymbol((PointF) aP.clone(), FillColor, OutlineColor, width,
                                    height, DrawFill, DrawOutline, g);
                        }
                    }
                }

                sP.X = aP.X + width / 2 + 5;
                sP.Y = aP.Y + height / 2;
                if (i < bNum - 1) {
                    FontMetrics metrics = g.getFontMetrics(lFont);
                    aSF = new Dimension(metrics.stringWidth(caption), metrics.getHeight());
                    g.setColor(this.getForeColor());
                    g.setFont(lFont);
                    g.drawString(caption, sP.X, sP.Y + aSF.height / 2);
                }
            }
        }
    }

    private void drawHorizontalBarLegend(Graphics2D g, float zoom) {
        LegendScheme aLS = _legendLayer.getLegendScheme();
        PointF aP = new PointF(0, 0);
        PointF sP = new PointF(0, 0);
        float width, height;
        boolean DrawShape = true, DrawFill = true, DrawOutline = true;
        Color FillColor = Color.red, OutlineColor = this.getForeColor();
        String caption = "";
        Dimension aSF;

        int bNum = aLS.getBreakNum();
        if (aLS.getLegendBreaks().get(bNum - 1).isNoData()) {
            bNum -= 1;
        }

        width = (this.getWidth() - 5) * zoom / bNum;
        height = _hBarHeight * zoom;
        Font lFont = new Font(this.getFont().getFontName(), this.getFont().getStyle(), (int) (this.getFont().getSize() * zoom));

        for (int i = 0; i < bNum; i++) {
            switch (aLS.getShapeType()) {
                case Point:
                    PointBreak aPB = (PointBreak) aLS.getLegendBreaks().get(i);
                    DrawShape = aPB.isDrawShape();
                    DrawFill = aPB.getDrawFill();
                    FillColor = aPB.getColor();
                    if (aLS.getLegendType() == LegendType.UniqueValue) {
                        caption = aPB.getCaption();
                    } else {                        
                        caption = DataConvert.removeTailingZeros(aPB.getEndValue().toString());
                    }
                    break;
                case Polyline:
                    PolylineBreak aPLB = (PolylineBreak) aLS.getLegendBreaks().get(i);
                    DrawShape = aPLB.getDrawPolyline();
                    FillColor = aPLB.getColor();
                    if (aLS.getLegendType() == LegendType.UniqueValue) {
                        caption = aPLB.getCaption();
                    } else {
                        caption = DataConvert.removeTailingZeros(aPLB.getEndValue().toString());
                    }
                    break;
                case Polygon:
                    PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(i);
                    DrawShape = aPGB.isDrawShape();
                    DrawFill = aPGB.getDrawFill();
                    FillColor = aPGB.getColor();
                    if (aLS.getLegendType() == LegendType.UniqueValue) {
                        caption = aPGB.getCaption();
                    } else {
                        caption = DataConvert.removeTailingZeros(aPGB.getEndValue().toString());
                    }
                    break;
                case Image:
                    ColorBreak aCB = aLS.getLegendBreaks().get(i);
                    DrawShape = true;
                    DrawFill = true;
                    FillColor = aCB.getColor();
                    if (aLS.getLegendType() == LegendType.UniqueValue) {
                        caption = aCB.getCaption();
                    } else {
                        caption = DataConvert.removeTailingZeros(aCB.getEndValue().toString());
                    }
                    break;
            }

            aP.X = i * width + width / 2;
            aP.Y = height / 2;

            if (aLS.getLegendType() == LegendType.UniqueValue) {
                if (DrawShape) {
                    if (aLS.getShapeType() == ShapeTypes.Polygon) {
                        PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(i).clone();
                        aPGB.setDrawOutline(true);
                        aPGB.setOutlineColor(Color.black);
                        Draw.drawPolygonSymbol((PointF) aP.clone(), width, height, aPGB, g);
                    } else {
                        Draw.drawPolygonSymbol((PointF) aP.clone(), FillColor, OutlineColor, width,
                                height, DrawFill, DrawOutline, g);
                    }
                }

                sP.X = aP.X;
                sP.Y = aP.Y + height / 2;
                FontMetrics metrics = g.getFontMetrics(lFont);
                aSF = new Dimension(metrics.stringWidth(caption), metrics.getHeight());
                g.setColor(this.getForeColor());
                g.setFont(lFont);
                g.drawString(caption, sP.X - aSF.width / 2, sP.Y + aSF.height);
            } else {
                if (DrawShape) {
                    if (i == 0) {
                        PointF[] Points = new PointF[4];
                        Points[0] = new PointF();
                        Points[0].X = 0;
                        Points[0].Y = aP.Y;
                        Points[1] = new PointF();
                        Points[1].X = width;
                        Points[1].Y = 0;
                        Points[2] = new PointF();
                        Points[2].X = width;
                        Points[2].Y = height;
                        Points[3] = new PointF();
                        Points[3].X = 0;
                        Points[3].Y = aP.Y;
                        if (aLS.getShapeType() == ShapeTypes.Polygon) {
                            PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(i).clone();
                            aPGB.setDrawOutline(true);
                            aPGB.setOutlineColor(Color.black);
                            Draw.drawPolygon(Points, aPGB, g);
                        } else {
                            Draw.drawPolygon(Points, FillColor, OutlineColor, DrawFill, DrawOutline, g);
                        }
                    } else if (i == bNum - 1) {
                        PointF[] Points = new PointF[4];
                        Points[0] = new PointF();
                        Points[0].X = i * width;
                        Points[0].Y = height;
                        Points[1] = new PointF();
                        Points[1].X = i * width;
                        Points[1].Y = 0;
                        Points[2] = new PointF();
                        Points[2].X = i * width + width;
                        Points[2].Y = aP.Y;
                        Points[3] = new PointF();
                        Points[3].X = i * width;
                        Points[3].Y = height;
                        if (aLS.getShapeType() == ShapeTypes.Polygon) {
                            PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(i).clone();
                            aPGB.setDrawOutline(true);
                            aPGB.setOutlineColor(Color.black);
                            Draw.drawPolygon(Points, aPGB, g);
                        } else {
                            Draw.drawPolygon(Points, FillColor, OutlineColor, DrawFill, DrawOutline, g);
                        }
                    } else {
                        if (aLS.getShapeType() == ShapeTypes.Polygon) {
                            PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(i).clone();
                            aPGB.setDrawOutline(true);
                            aPGB.setOutlineColor(Color.black);
                            Draw.drawPolygonSymbol((PointF) aP.clone(), width, height, aPGB, g);
                        } else {
                            Draw.drawPolygonSymbol((PointF) aP.clone(), FillColor, OutlineColor, width,
                                    height, DrawFill, DrawOutline, g);
                        }
                    }
                }

                sP.X = aP.X + width / 2;
                sP.Y = aP.Y + height / 2;
                if (i < bNum - 1) {
                    FontMetrics metrics = g.getFontMetrics(lFont);
                    aSF = new Dimension(metrics.stringWidth(caption), metrics.getHeight());
                    g.setColor(this.getForeColor());
                    g.setFont(lFont);
                    g.drawString(caption, sP.X - aSF.width / 2, sP.Y + aSF.height);
                }
            }
        }
    }

    private int getLabelWidth(Graphics2D g) {
        LegendScheme aLS = _legendLayer.getLegendScheme();
        float width = 0;
        String caption = "";
        Dimension aSF;
        int bNum = aLS.getBreakNum();
        FontMetrics metrics = g.getFontMetrics(_font);
        if (_legendStyle == LegendStyles.Normal) {            
            aSF = new Dimension(metrics.stringWidth(_title), metrics.getHeight());
            width = aSF.width;
        } else {
            if (aLS.getLegendBreaks().get(bNum - 1).isNoData()) {
                bNum -= 1;
            }
        }
        for (int i = 0; i < bNum; i++) {
            switch (aLS.getShapeType()) {
                case Point:
                    PointBreak aPB = (PointBreak) aLS.getLegendBreaks().get(i);
                    if (aLS.getLegendType() == LegendType.GraduatedColor && _legendStyle != LegendStyles.Normal) {
                        caption = DataConvert.removeTailingZeros(aPB.getEndValue().toString());
                    } else {
                        caption = aPB.getCaption();
                    }
                    break;
                case Polyline:
                    PolylineBreak aPLB = (PolylineBreak) aLS.getLegendBreaks().get(i);
                    if (aLS.getLegendType() == LegendType.GraduatedColor && _legendStyle != LegendStyles.Normal) {
                        caption = DataConvert.removeTailingZeros(aPLB.getEndValue().toString());
                    } else {
                        caption = aPLB.getCaption();
                    }
                    break;
                case Polygon:
                    PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(i);
                    if (aLS.getLegendType() == LegendType.GraduatedColor && _legendStyle != LegendStyles.Normal) {
                        caption = DataConvert.removeTailingZeros(aPGB.getEndValue().toString());
                    } else {
                        caption = aPGB.getCaption();
                    }
                    break;
                case Image:
                    ColorBreak aCB = aLS.getLegendBreaks().get(i);
                    if (aLS.getLegendType() == LegendType.GraduatedColor && _legendStyle != LegendStyles.Normal) {
                        caption = DataConvert.removeTailingZeros(aCB.getEndValue().toString());
                    } else {
                        caption = aCB.getCaption();
                    }
                    break;
            }

            boolean isValid = true;
            switch (aLS.getLegendType()) {
                case GraduatedColor:
                    if (_legendStyle != LegendStyles.Normal) {
                        if (i == bNum - 1) {
                            isValid = false;
                        }
                    }
                    break;
            }
            if (isValid) {
                float labwidth = metrics.stringWidth(caption);
                if (width < labwidth) {
                    width = labwidth;
                }
            }
        }
        
        if (_legendStyle == LegendStyles.Normal) {
            if (_legendLayer.getLayerType() == LayerTypes.VectorLayer) {
                if (((VectorLayer) _legendLayer).getChartSet().isDrawCharts()) {
                    ChartBreak aCB = ((ChartBreak) ((VectorLayer) _legendLayer).getChartPoints().get(0).getLegend()).getSampleChartBreak();
                    if (aCB.getChartType() == ChartTypes.BarChart){
                        LegendScheme ls = aCB.getLegendScheme();
                        for (ColorBreak cb : ls.getLegendBreaks()){
                            float labwidth = metrics.stringWidth(cb.getCaption());
                            if (width < labwidth)
                                width = metrics.stringWidth(cb.getCaption());
                        }
                    }
                }
            }
        }

        return (int) width;
    }

    private int getBreakHeight(Graphics2D g) {
        String title = _title;
        if ("".equals(title.trim())) {
            title = "Temp";
        }

        FontMetrics metrics = g.getFontMetrics(_font);
        Dimension aSF = new Dimension(metrics.stringWidth(title), metrics.getHeight());
        return aSF.height;
    }

    private int getTitleHeight(Graphics2D g) {
        FontMetrics metrics = g.getFontMetrics(_titleFont);
        Dimension aSF = new Dimension(metrics.stringWidth(_title), metrics.getHeight());
        return aSF.height;
    }

    /**
     * Update legend control size
     */
    public void updateLegendSize() {
        if (_legendLayer != null) {
            if (_legendLayer.getLegendScheme() == null) {
                return;
            }

            //Graphics2D g = (Graphics2D) _mapLayout.getGraphics();
            BufferedImage image = new BufferedImage(_mapLayout.getPageBounds().width, _mapLayout.getPageBounds().height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            int bNum = _legendLayer.getLegendScheme().getBreakNum();
            if (_legendLayer.getLegendScheme().getLegendBreaks().get(bNum - 1).isNoData()) {
                bNum -= 1;
            }

            switch (_legendStyle) {
                case Bar_Vertical:
                    this.setWidth(10 + getLabelWidth(g) + 5);
                    this.setHeight(bNum * 20);
                    break;
                case Bar_Horizontal:
                    this.setWidth(bNum * 30);
                    this.setHeight(30);
                    break;
                case Normal:
                    int aHeight = getBreakHeight(g);
                    int colWidth = aHeight * 2 + getLabelWidth(g) + 15;
                    this.setWidth(colWidth * _columnNum);

                    //Set columns
                    int[] rowNums = new int[_columnNum];
                    int ave = _legendLayer.getLegendScheme().getVisibleBreakNum() / _columnNum;
                    int num = 0;
                    int i;
                    for (i = 1; i < _columnNum; i++) {
                        rowNums[i] = ave;
                        num += ave;
                    }
                    rowNums[0] = _legendLayer.getLegendScheme().getVisibleBreakNum() - num;

                    this.setHeight((int) (rowNums[0] * (aHeight + _breakSpace)
                            + getTitleHeight(g) + _breakSpace * 2 + aHeight / 2 + 5));
                    if (_legendLayer.getLayerType() == LayerTypes.VectorLayer) {
                        VectorLayer aLayer = (VectorLayer) _legendLayer;
                        if (aLayer.getChartSet().isDrawCharts()) {
                            ChartBreak aCB = ((ChartBreak) aLayer.getChartPoints().get(0).getLegend()).getSampleChartBreak();
                            this.setHeight(this.getHeight() + (int) (_breakSpace * 2 + aCB.getHeight()
                                    + aCB.getLegendScheme().getBreakNum() * (aHeight + _breakSpace) + aHeight / 2 + 5));
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void paint(Graphics2D g) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void paintOnLayout(Graphics2D g, PointF pageLocation, float zoom) {
        if (this.isVisible()) {
            paintGraphics(g, pageLocation, zoom);
        }
    }

    @Override
    public void moveUpdate() {
    }

    @Override
    public void resizeUpdate() {
    }

    /**
     * Get layer names
     *
     * @return Layer names
     */
    public static List<String> getLayerNames() {
        List<String> layerNames = new ArrayList<String>();
        for (MapLayer aLayer : _layoutMap.getMapFrame().getMapView().getLayers()) {
            if (aLayer.getLayerType() == LayerTypes.VectorLayer || aLayer.getLayerType() == LayerTypes.RasterLayer) {
                layerNames.add(aLayer.getLayerName());
            }
        }

        return layerNames;
    }
    // </editor-fold>      

    // <editor-fold desc="BeanInfo">
    public class LayoutLegendBean {

        LayoutLegendBean() {
        }

        // <editor-fold desc="Get Set Methods">
        /**
         * Get layout map
         *
         * @return The layout map
         */
        public LayoutMap getLayoutMap() {
            return _layoutMap;
        }

        /**
         * Get legend layer
         *
         * @return The legend alyer
         */
        public MapLayer getLegendLayer() {
            return _legendLayer;
        }

        /**
         * Set legend layer
         *
         * @param layer The legend layer
         */
        public void setLegendLayer(MapLayer layer) {
            _legendLayer = layer;
            String aStr = _legendLayer.getLayerName();
            if (aStr.contains("_")) {
                aStr = aStr.split("_")[1];
            }
            _title = aStr;
            updateLegendSize();
        }

        /**
         * Get legend layer name
         *
         * @return Legend layer name
         */
        public String getLayerName() {
            return _legendLayer.getLayerName();
        }

        /**
         * Set legend layer name
         *
         * @param name Layer name
         */
        public void setLayerName(String name) {
            MapLayer aLayer = _layoutMap.getMapFrame().getMapView().getLayer(name);
            if (aLayer != null) {
                this.setLegendLayer(aLayer);
            }
        }

        /**
         * Get layer update type string
         *
         * @return Layer update type string
         */
        public String getLayerUpdateType() {
            return _layerUpdateType.toString();
        }

        /**
         * Set layer update type
         *
         * @param typeStr Layer update type string
         */
        public void setLayerUpdateType(String typeStr) {
            _layerUpdateType = LayerUpdateTypes.valueOf(typeStr);
        }

        /**
         * Get legend style string
         *
         * @return Legend style string
         */
        public String getLegendStyle() {
            return _legendStyle.toString();
        }

        /**
         * Set legend style
         *
         * @param style Legend style string
         */
        public void setLegendStyle(String style) {
            _legendStyle = LegendStyles.valueOf(style);
            if (isVisible()) {
                updateLegendSize();
            }
        }

        /**
         * Get title
         *
         * @return The title
         */
        public String getTitle() {
            return _title;
        }

        /**
         * Set title
         *
         * @param title The title
         */
        public void setTitle(String title) {
            _title = title;
            updateLegendSize();
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
        public Font getFont() {
            return _font;
        }

        /**
         * Set font
         *
         * @param font The font
         */
        public void setFont(Font font) {
            _font = font;
            _titleFont = new Font(_font.getFontName(), Font.PLAIN, _font.getSize() + 2);
            updateLegendSize();
        }

        /**
         * Get column number
         *
         * @return Column number
         */
        public int getColumnNumber() {
            return _columnNum;
        }

        /**
         * Set column number
         *
         * @param value Column number
         */
        public void setColumnNumber(int value) {
            _columnNum = value;
            if (isVisible()) {
                updateLegendSize();
            }
        }

        /**
         * Get background color
         *
         * @return Background color
         */
        public Color getBackColor() {
            return LayoutLegend.this.getBackColor();
        }

        /**
         * Set background color
         *
         * @param c Background color
         */
        public void setBackColor(Color c) {
            LayoutLegend.this.setBackColor(c);
        }

        /**
         * Get foreground color
         *
         * @return Foreground color
         */
        public Color getForeColor() {
            return LayoutLegend.this.getForeColor();
        }

        /**
         * Set foreground color
         *
         * @param c Foreground color
         */
        public void setForeColor(Color c) {
            LayoutLegend.this.setForeColor(c);
        }

        /**
         * Get left
         *
         * @return Left
         */
        public int getLeft() {
            return LayoutLegend.this.getLeft();
        }

        /**
         * Set left
         *
         * @param left Left
         */
        public void setLeft(int left) {
            LayoutLegend.this.setLeft(left);
        }

        /**
         * Get top
         *
         * @return Top
         */
        public int getTop() {
            return LayoutLegend.this.getTop();
        }

        /**
         * Set top
         *
         * @param top Top
         */
        public void setTop(int top) {
            LayoutLegend.this.setTop(top);
        }
        // </editor-fold>
    }

    public static class LayoutLegendBeanBeanInfo extends BaseBeanInfo {

        public LayoutLegendBeanBeanInfo() {
            super(LayoutLegendBean.class);
            ExtendedPropertyDescriptor e = addProperty("layerName");
            e.setCategory("General").setPropertyEditorClass(LayerNameEditor.class);
            e.setDisplayName("Layer Name");
            e.setShortDescription("The name of the layer of this legend");
            e = addProperty("layerUpdateType");
            e.setCategory("General").setDisplayName("Layer Update Type");
            e.setPropertyEditorClass(LayerUpdateTypeEditor.class);
            e = addProperty("legendStyle");
            e.setCategory("General").setDisplayName("Legend Style");
            e.setPropertyEditorClass(LegendStyleEditor.class);
            addProperty("title").setCategory("General").setDisplayName("Title");
            addProperty("font").setCategory("General").setDisplayName("Font");
            addProperty("backColor").setCategory("General").setDisplayName("Background");
            addProperty("foreColor").setCategory("General").setDisplayName("Foreground");
            addProperty("columnNumber").setCategory("General").setDisplayName("Column Number");
            addProperty("drawNeatLine").setCategory("Neat Line").setDisplayName("Draw Neat Line");
            addProperty("neatLineColor").setCategory("Neat Line").setDisplayName("Neat Line Color");
            addProperty("neatLineSize").setCategory("Neat Line").setDisplayName("Neat Line Size");
            addProperty("left").setCategory("Location").setDisplayName("Left");
            addProperty("top").setCategory("Location").setDisplayName("Top");
        }
    }

    public static class LayerNameEditor extends ComboBoxPropertyEditor {

        public LayerNameEditor() {
            super();
            String[] names = (String[]) getLayerNames().toArray(new String[0]);
            setAvailableValues(names);
//            Icon[] icons = new Icon[4];
//            Arrays.fill(icons, UIManager.getIcon("Tree.openIcon"));
//            setAvailableIcons(icons);
        }
    }

    public static class LayerUpdateTypeEditor extends ComboBoxPropertyEditor {

        public LayerUpdateTypeEditor() {
            super();
            LayerUpdateTypes[] lutypes = LayerUpdateTypes.values();
            String[] types = new String[lutypes.length];
            int i = 0;
            for (LayerUpdateTypes type : lutypes) {
                types[i] = type.toString();
                i += 1;
            }
            setAvailableValues(types);
        }
    }

    public static class LegendStyleEditor extends ComboBoxPropertyEditor {

        public LegendStyleEditor() {
            super();
            LegendStyles[] styles = LegendStyles.values();
            String[] values = new String[styles.length];
            int i = 0;
            for (LegendStyles s : styles) {
                values[i] = s.toString();
                i += 1;
            }
            setAvailableValues(values);
        }
    }
    // </editor-fold>
}
