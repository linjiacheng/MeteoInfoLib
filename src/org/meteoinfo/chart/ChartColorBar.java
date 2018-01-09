/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.drawing.Draw;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.global.PointF;
import org.meteoinfo.legend.ColorBreak;
import org.meteoinfo.legend.LegendScheme;
import org.meteoinfo.legend.LegendType;
import org.meteoinfo.legend.PointBreak;
import org.meteoinfo.legend.PolygonBreak;
import org.meteoinfo.legend.PolylineBreak;
import org.meteoinfo.shape.ShapeTypes;

/**
 *
 * @author Yaqiang Wang
 */
public class ChartColorBar extends ChartLegend {

    // <editor-fold desc="Variables">
    private List<Double> tickLocations;
    private List<ChartText> tickLabels;
    private boolean autoTick;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     *
     * @param ls LegendScheme
     */
    public ChartColorBar(LegendScheme ls) {
        super(ls);

        this.tickLocations = new ArrayList<>();
        this.tickLabels = new ArrayList<>();
        this.autoTick = true;
    }

    // </editor-fold>
    /**
     * Tick locations
     *
     * @return Tick locations
     */
    public List<Double> getTickLocations() {
        return this.tickLocations;
    }

    /**
     * Set tick locations
     *
     * @param value Tick locations
     */
    public void setTickLocations(List<Number> value) {
        this.tickLocations.clear();
        this.tickLabels.clear();
        for (Number v : value) {
            this.tickLocations.add(v.doubleValue());
            this.tickLabels.add(new ChartText(String.valueOf(v)));
        }
        this.autoTick = false;
    }

    /**
     * Set tick locations
     *
     * @param value Tick locations
     */
    public void setTickLocations(double[] value) {
        this.tickLocations.clear();
        //this.tickLabels.clear();
        for (double v : value) {
            this.tickLocations.add(v);
            //this.tickLabels.add(new ChartText(String.valueOf(v)));
        }
        this.autoTick = false;
    }

    /**
     * Get tick labels
     *
     * @return Tick labels
     */
    public List<ChartText> getTickLabels() {
        return this.tickLabels;
    }

    /**
     * Get tick label text
     *
     * @return Tick label text
     */
    public List<String> getTickLabelText() {
        List<String> strs = new ArrayList<>();
        for (ChartText ct : this.tickLabels) {
            strs.add(ct.toString());
        }

        return strs;
    }

    /**
     * Set tick label text
     *
     * @param value Tick label text
     */
    public void setTickLabelText(List<String> value) {
        this.tickLabels = new ArrayList<>();
        for (String v : value) {
            this.tickLabels.add(new ChartText(v));
        }
        this.autoTick = false;
    }

    /**
     * Set tick labels.
     *
     * @param value Tick labels
     */
    public void setTickLabels(List<ChartText> value) {
        this.tickLabels = value;
    }

    /**
     * Set tick labels
     *
     * @param value Tick labels
     */
    public void setTickLabels_Number(List<Number> value) {
        this.tickLabels = new ArrayList<>();
        for (Number v : value) {
            this.tickLabels.add(new ChartText(v.toString()));
        }
        this.autoTick = false;
    }

    /**
     * Get if is auto tick labels
     *
     * @return Boolean
     */
    public boolean isAutoTick() {
        return this.autoTick;
    }

    /**
     * Set if auto tick labels
     *
     * @param value Boolean
     */
    public void setAutoTick(boolean value) {
        this.autoTick = value;
    }

    // <editor-fold desc="Method">
    /**
     * Draw legend
     *
     * @param g Graphics2D
     * @param point Start point
     */
    @Override
    public void draw(Graphics2D g, PointF point) {

        AffineTransform oldMatrix = g.getTransform();
        g.translate(point.X + this.xshift, point.Y + this.yshift);

        //Draw background color
        if (this.drawBackground) {
            g.setColor(this.background);
            g.fill(new Rectangle.Float(0, 0, this.width, this.height));
        }

        //Draw legend
        g.setStroke(new BasicStroke(1));
        switch (this.orientation) {
            case HORIZONTAL:
                this.drawHorizontalBarLegend(g, legendScheme);
                break;
            case VERTICAL:
                this.drawVerticalBarLegend(g, legendScheme);
                break;
        }

        //Draw neatline
        if (drawNeatLine) {
            Rectangle.Float mapRect = new Rectangle.Float(0, 0, this.width, this.height);
            g.setColor(neatLineColor);
            g.setStroke(new BasicStroke(neatLineSize));
            g.draw(mapRect);
        }

        g.setTransform(oldMatrix);
    }

    private void drawHorizontalBarLegend(Graphics2D g, LegendScheme aLS) {
        PointF aP = new PointF(0, 0);
        PointF sP = new PointF(0, 0);
        boolean DrawShape = true, DrawFill = true, DrawOutline = false;
        Color FillColor = Color.red, OutlineColor = Color.black;
        String caption;
        Dimension aSF;

        int bNum = aLS.getBreakNum();
        if (aLS.getLegendBreaks().get(bNum - 1).isNoData()) {
            bNum -= 1;
        }

        List<Integer> labelIdxs = new ArrayList<>();
        if (this.autoTick) {
            int tickGap = this.getTickGap(g);
            int sIdx = (bNum % tickGap) / 2;
            int labNum = bNum - 1;
            if (aLS.getLegendType() == LegendType.UniqueValue) {
                labNum += 1;
            } else if (this.drawMinLabel) {
                sIdx = 0;
                labNum = bNum;
            }
            while (sIdx < labNum) {
                labelIdxs.add(sIdx);
                sIdx += tickGap;
            }
        } else {
            for (int i = 0; i < bNum; i++) {
                ColorBreak cb = aLS.getLegendBreaks().get(i);
                double v = Double.parseDouble(cb.getEndValue().toString());
                if (this.tickLocations.contains(v)) {
                    labelIdxs.add(i);
                }
            }
        }

        this._hBarHeight = (float) this.width / this.aspect;
        _vBarWidth = (float) this.width / bNum;
        aP.X = -_vBarWidth / 2;
        int idx;
        for (int i = 0; i < bNum; i++) {
            idx = i;
            switch (aLS.getShapeType()) {
                case Point:
                    PointBreak aPB = (PointBreak) aLS.getLegendBreaks().get(idx);
                    DrawShape = aPB.isDrawShape();
                    DrawFill = aPB.getDrawFill();
                    FillColor = aPB.getColor();
                    break;
                case Polyline:
                    PolylineBreak aPLB = (PolylineBreak) aLS.getLegendBreaks().get(idx);
                    DrawShape = aPLB.getDrawPolyline();
                    FillColor = aPLB.getColor();
                    break;
                case Polygon:
                    PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx);
                    DrawShape = aPGB.isDrawShape();
                    DrawFill = aPGB.isDrawFill();
                    FillColor = aPGB.getColor();
                    break;
                case Image:
                    ColorBreak aCB = aLS.getLegendBreaks().get(idx);
                    DrawShape = true;
                    DrawFill = true;
                    FillColor = aCB.getColor();
                    break;
            }

            aP.X += _vBarWidth;
            aP.Y = _hBarHeight / 2;

            if (DrawShape) {
                if (this.extendRect) {
                    if (aLS.getShapeType() == ShapeTypes.Polygon) {
                        PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx).clone();
                        aPGB.setDrawOutline(false);
                        Draw.drawPolygonSymbol((PointF) aP.clone(), _vBarWidth, _hBarHeight, aPGB, g);
                    } else {
                        Draw.drawPolygonSymbol((PointF) aP.clone(), FillColor, OutlineColor, _vBarWidth,
                                _hBarHeight, DrawFill, DrawOutline, g);
                    }
                } else {
                    float extendw = _vBarWidth;
                    if (this.autoExtendFrac) {
                        extendw = _hBarHeight;
                    }
                    if (i == 0) {
                        PointF[] Points = new PointF[4];
                        Points[0] = new PointF();
                        Points[0].X = _vBarWidth - extendw;
                        Points[0].Y = aP.Y;
                        Points[1] = new PointF();
                        Points[1].X = _vBarWidth;
                        Points[1].Y = 0;
                        Points[2] = new PointF();
                        Points[2].X = _vBarWidth;
                        Points[2].Y = _hBarHeight;
                        Points[3] = new PointF();
                        Points[3].X = _vBarWidth - extendw;
                        Points[3].Y = aP.Y;
                        if (aLS.getShapeType() == ShapeTypes.Polygon) {
                            PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx).clone();
                            aPGB.setDrawOutline(false);
                            Draw.drawPolygon(Points, aPGB, g);
                        } else {
                            Draw.drawPolygon(Points, FillColor, OutlineColor, DrawFill, DrawOutline, g);
                        }
                    } else if (i == bNum - 1) {
                        PointF[] Points = new PointF[4];
                        Points[0] = new PointF();
                        Points[0].X = i * _vBarWidth - 1.0f;
                        Points[0].Y = _hBarHeight;
                        Points[1] = new PointF();
                        Points[1].X = i * _vBarWidth - 1.0f;
                        Points[1].Y = 0;
                        Points[2] = new PointF();
                        Points[2].X = i * _vBarWidth + extendw;
                        Points[2].Y = aP.Y;
                        Points[3] = new PointF();
                        Points[3].X = i * _vBarWidth - 1.0f;
                        Points[3].Y = _hBarHeight;
                        if (aLS.getShapeType() == ShapeTypes.Polygon) {
                            PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx).clone();
                            aPGB.setDrawOutline(false);
                            Draw.drawPolygon(Points, aPGB, g);
                        } else {
                            Draw.drawPolygon(Points, FillColor, OutlineColor, DrawFill, DrawOutline, g);
                        }
                    } else if (aLS.getShapeType() == ShapeTypes.Polygon) {
                        PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx).clone();
                        aPGB.setDrawOutline(false);
                        Draw.drawPolygonSymbol((PointF) aP.clone(), _vBarWidth, _hBarHeight, aPGB, g);
                    } else {
                        Draw.drawPolygonSymbol((PointF) aP.clone(), FillColor, OutlineColor, _vBarWidth,
                                _hBarHeight, DrawFill, DrawOutline, g);
                    }
                }
            }
        }
        //Draw neatline
        g.setColor(Color.black);
        if (this.extendRect) {
            g.draw(new Rectangle.Float(0, 0, this._vBarWidth * bNum, this._hBarHeight));
        } else {
            float extendw = _vBarWidth;
            if (this.autoExtendFrac) {
                extendw = _hBarHeight;
            }
            Polygon p = new Polygon();
            p.addPoint((int) (_vBarWidth - extendw), (int) (this._hBarHeight / 2));
            p.addPoint((int) this._vBarWidth, 0);
            p.addPoint((int) (this._vBarWidth * (bNum - 1)), 0);
            p.addPoint((int) (this._vBarWidth * (bNum - 1) + extendw), (int) (this._hBarHeight / 2));
            p.addPoint((int) (this._vBarWidth * (bNum - 1)), (int) this._hBarHeight);
            p.addPoint((int) this._vBarWidth, (int) this._hBarHeight);
            g.drawPolygon(p);
        }
        //Draw tick and label
        aP.X = -_vBarWidth / 2;
        int labLen = (int) (this._hBarHeight / 3);
        if (labLen < 5) {
            labLen = 5;
            if (this._hBarHeight < 5) {
                labLen = (int) this._hBarHeight;
            }
        }
        g.setFont(tickFont);
        g.setColor(Color.black);
        idx = 0;
        for (int i = 0; i < bNum; i++) {
            aP.X += _vBarWidth;
            aP.Y = _hBarHeight / 2;
            if (labelIdxs.contains(i)) {
                ColorBreak cb = aLS.getLegendBreaks().get(i);
                if (aLS.getLegendType() == LegendType.UniqueValue) {
                    caption = cb.getCaption();
                } else {
                    caption = DataConvert.removeTailingZeros(cb.getEndValue().toString());
                    if (!this.autoTick) {
                        if (this.tickLabels.size() > idx){
                            caption = this.tickLabels.get(idx).getText();
                        }
                    }
                }
                aSF = Draw.getStringDimension(caption, g);
                if (aLS.getLegendType() == LegendType.UniqueValue) {
                    sP.X = aP.X;
                    sP.Y = aP.Y + _hBarHeight / 2 + 5;
                    Draw.drawString(g, caption, sP.X - aSF.width / 2, sP.Y + aSF.height * 3 / 4);
                } else {
                    sP.X = aP.X + _vBarWidth / 2;
                    sP.Y = aP.Y + _hBarHeight / 2;
                    g.draw(new Line2D.Float(sP.X, sP.Y, sP.X, sP.Y - labLen));
                    sP.Y = sP.Y + 5;
                    if (this.autoTick){
                        if (i < bNum - 1) {
                            Draw.drawString(g, caption, sP.X - aSF.width / 2, sP.Y + aSF.height * 3 / 4);
                            if (this.drawMinLabel && i == 0) {
                                caption = DataConvert.removeTailingZeros(cb.getStartValue().toString());
                                Draw.drawString(g, caption, sP.X - aSF.width / 2 - this._vBarWidth, sP.Y + aSF.height * 3 / 4);
                            }
                        } else if (this.drawMaxLabel) {
                            Draw.drawString(g, caption, sP.X - aSF.width / 2, sP.Y + aSF.height * 3 / 4);
                        }
                    } else {
                        if (i == 0)
                            Draw.drawString(g, caption, sP.X - aSF.width / 2 - this._vBarWidth, sP.Y + aSF.height * 3 / 4);
                        else
                            Draw.drawString(g, caption, sP.X - aSF.width / 2, sP.Y + aSF.height * 3 / 4);
                    }
                }
                idx += 1;
            }
        }

        //Draw label
        if (this.label != null) {
            g.setFont(this.label.getFont());
            g.setColor(this.label.getColor());
            Dimension dim = Draw.getStringDimension(this.label.getText(), g);
            switch (this.labelLocation) {
                case "top":
                case "right":
                    x = this.width + 10;
                    y = this._hBarHeight * 0.5f + dim.height * 0.25f;
                    break;
                case "left":
                case "bottom":
                    x = -(dim.width + 10);
                    y = this._hBarHeight * 0.5f + dim.height * 0.25f;
                    break;
                default:
                    x = this.width * 0.5f - dim.width * 0.5f;
                    y = this.height - dim.height * 0.25f - 2;
                    break;
            }
            Draw.drawString(g, label.getText(), x, y, label.isUseExternalFont());
        }
    }

    private void drawVerticalBarLegend(Graphics2D g, LegendScheme aLS) {
        PointF aP = new PointF(0, 0);
        PointF sP = new PointF(0, 0);
        boolean DrawShape = true, DrawFill = true, DrawOutline = false;
        Color FillColor = Color.red, OutlineColor = Color.black;
        String caption;
        Dimension aSF;

        int bNum = aLS.getBreakNum();
        if (aLS.getLegendBreaks().get(bNum - 1).isNoData()) {
            bNum -= 1;
        }

        List<Integer> labelIdxs = new ArrayList<>();
        if (this.autoTick) {
            int tickGap = this.getTickGap(g);
            int sIdx = (bNum % tickGap) / 2;
            int labNum = bNum - 1;
            if (aLS.getLegendType() == LegendType.UniqueValue) {
                labNum += 1;
            } else if (this.drawMinLabel) {
                sIdx = 0;
                labNum = bNum;
            }
            while (sIdx < labNum) {
                labelIdxs.add(sIdx);
                sIdx += tickGap;
            }
        } else {
            for (int i = 0; i < bNum; i++) {
                ColorBreak cb = aLS.getLegendBreaks().get(i);
                double v = Double.parseDouble(cb.getEndValue().toString());
                if (this.tickLocations.contains(v)) {
                    labelIdxs.add(i);
                }
            }
        }

        this._vBarWidth = (float) this.height / this.aspect;
        _hBarHeight = (float) this.height / bNum;
        aP.Y = this.height + _hBarHeight / 2;
        int idx;
        for (int i = 0; i < bNum; i++) {
            idx = i;
            switch (aLS.getShapeType()) {
                case Point:
                    PointBreak aPB = (PointBreak) aLS.getLegendBreaks().get(idx);
                    DrawShape = aPB.isDrawShape();
                    DrawFill = aPB.getDrawFill();
                    FillColor = aPB.getColor();
                    break;
                case Polyline:
                    PolylineBreak aPLB = (PolylineBreak) aLS.getLegendBreaks().get(idx);
                    DrawShape = aPLB.getDrawPolyline();
                    FillColor = aPLB.getColor();
                    break;
                case Polygon:
                    PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx);
                    DrawShape = aPGB.isDrawShape();
                    DrawFill = aPGB.isDrawFill();
                    FillColor = aPGB.getColor();
                    break;
                case Image:
                    ColorBreak aCB = aLS.getLegendBreaks().get(idx);
                    DrawShape = true;
                    DrawFill = true;
                    FillColor = aCB.getColor();
                    break;
            }

            aP.X = _vBarWidth / 2;
            aP.Y = aP.Y - _hBarHeight;

            if (DrawShape) {
                if (this.extendRect) {
                    if (aLS.getShapeType() == ShapeTypes.Polygon) {
                        PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx).clone();
                        aPGB.setDrawOutline(false);
                        Draw.drawPolygonSymbol((PointF) aP.clone(), _vBarWidth, _hBarHeight, aPGB, g);
                    } else {
                        Draw.drawPolygonSymbol((PointF) aP.clone(), FillColor, OutlineColor, _vBarWidth,
                                _hBarHeight, DrawFill, DrawOutline, g);
                    }
                } else if (i == 0) {
                    PointF[] Points = new PointF[4];
                    Points[0] = new PointF();
                    Points[0].X = aP.X;
                    Points[0].Y = this.height;
                    Points[1] = new PointF();
                    Points[1].X = 0;
                    Points[1].Y = aP.Y - _hBarHeight / 2 - 1.0f;
                    Points[2] = new PointF();
                    Points[2].X = _vBarWidth;
                    Points[2].Y = aP.Y - _hBarHeight / 2 - 1.0f;
                    Points[3] = new PointF();
                    Points[3].X = aP.X;
                    Points[3].Y = this.height;
                    if (aLS.getShapeType() == ShapeTypes.Polygon) {
                        PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx).clone();
                        aPGB.setDrawOutline(false);
                        Draw.drawPolygon(Points, aPGB, g);
                    } else {
                        Draw.drawPolygon(Points, FillColor, OutlineColor, DrawFill, DrawOutline, g);
                    }
                } else if (i == bNum - 1) {
                    PointF[] Points = new PointF[4];
                    Points[0] = new PointF();
                    Points[0].X = 0;
                    Points[0].Y = _hBarHeight;
                    Points[1] = new PointF();
                    Points[1].X = _vBarWidth;
                    Points[1].Y = _hBarHeight;
                    Points[2] = new PointF();
                    Points[2].X = aP.X;
                    Points[2].Y = 0;
                    Points[3] = new PointF();
                    Points[3].X = 0;
                    Points[3].Y = _hBarHeight;
                    if (aLS.getShapeType() == ShapeTypes.Polygon) {
                        PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx).clone();
                        aPGB.setDrawOutline(false);
                        Draw.drawPolygon(Points, aPGB, g);
                    } else {
                        Draw.drawPolygon(Points, FillColor, OutlineColor, DrawFill, DrawOutline, g);
                    }
                } else if (aLS.getShapeType() == ShapeTypes.Polygon) {
                    PolygonBreak aPGB = (PolygonBreak) aLS.getLegendBreaks().get(idx).clone();
                    aPGB.setDrawOutline(false);
                    Draw.drawPolygonSymbol((PointF) aP.clone(), _vBarWidth, _hBarHeight, aPGB, g);
                } else {
                    Draw.drawPolygonSymbol((PointF) aP.clone(), FillColor, OutlineColor, _vBarWidth,
                            _hBarHeight, DrawFill, DrawOutline, g);
                }
            }
        }
        //Draw neatline
        g.setColor(Color.black);
        if (this.extendRect) {
            g.draw(new Rectangle.Float(0, 0, this._vBarWidth, this._hBarHeight * bNum));
        } else {
            Polygon p = new Polygon();
            p.addPoint((int) (this._vBarWidth / 2), 0);
            p.addPoint(0, (int) this._hBarHeight);
            p.addPoint(0, (int) (this._hBarHeight * (bNum - 1)));
            p.addPoint((int) (this._vBarWidth / 2), (int) (this._hBarHeight * bNum));
            p.addPoint((int) this._vBarWidth, (int) (this._hBarHeight * (bNum - 1)));
            p.addPoint((int) this._vBarWidth, (int) this._hBarHeight);
            g.drawPolygon(p);
        }
        //Draw ticks
        aP.Y = this.height + _hBarHeight / 2;
        int labLen = (int) (this._vBarWidth / 3);
        if (labLen < 5) {
            labLen = 5;
            if (this._vBarWidth < 5) {
                labLen = (int) this._vBarWidth;
            }
        }
        g.setFont(tickFont);
        idx = 0;
        for (int i = 0; i < bNum; i++) {
            aP.X = _vBarWidth / 2;
            aP.Y = aP.Y - _hBarHeight;
            if (labelIdxs.contains(i)) {
                ColorBreak cb = aLS.getLegendBreaks().get(i);
                if (aLS.getLegendType() == LegendType.UniqueValue) {
                    caption = cb.getCaption();
                } else {
                    caption = DataConvert.removeTailingZeros(cb.getEndValue().toString());
                    if (!this.autoTick) {
                        if (this.tickLabels.size() > idx){
                            caption = this.tickLabels.get(idx).getText();
                        }
                    }
                }
                aSF = Draw.getStringDimension(caption, g);
                if (aLS.getLegendType() == LegendType.UniqueValue) {
                    sP.X = aP.X + _vBarWidth / 2 + 5;
                    sP.Y = aP.Y;
                    g.setColor(Color.black);
                    Draw.drawString(g, caption, sP.X, sP.Y + aSF.height / 4);
                } else {
                    sP.X = aP.X + _vBarWidth / 2;
                    sP.Y = aP.Y - _hBarHeight / 2;
                    g.draw(new Line2D.Float(sP.X - labLen, sP.Y, sP.X, sP.Y));
                    sP.X = sP.X + 5;
                    if (this.autoTick){
                        if (i < bNum - 1) {
                            Draw.drawString(g, caption, sP.X, sP.Y + aSF.height / 4);
                            if (this.drawMinLabel && i == 0) {
                                caption = DataConvert.removeTailingZeros(cb.getStartValue().toString());
                                Draw.drawString(g, caption, sP.X, sP.Y + aSF.height / 4 + this._hBarHeight);
                            }
                        } else if (this.drawMaxLabel) {
                            Draw.drawString(g, caption, sP.X, sP.Y + aSF.height / 4);
                        }
                    } else {
                        if (i == 0)
                            Draw.drawString(g, caption, sP.X, sP.Y + aSF.height / 4 + this._hBarHeight);
                        else
                            Draw.drawString(g, caption, sP.X, sP.Y + aSF.height / 4);
                    }
                }
                idx += 1;
            }
        }
        //Draw label
        if (this.label != null) {
            g.setFont(this.label.getFont());
            Dimension dim = Draw.getStringDimension(this.label.getText(), g);
            switch (this.labelLocation) {
                case "top":
                case "right":
                    x = dim.width * 0.5f;
                    y = -(dim.height * 0.5f + 5);
                    Draw.drawLabelPoint(x, y, this.getLabelFont(), label.getText(), this.getLabelColor(), 0,
                            g, null, this.label.isUseExternalFont());
                    break;
                case "bottom":
                case "left":
                    x = dim.width * 0.5f;
                    y = this.height + dim.height + 5;
                    Draw.drawLabelPoint(x, y, this.getLabelFont(), label.getText(), this.getLabelColor(), 0,
                            g, null, this.label.isUseExternalFont());
                    break;
                default:
                    x = this.width - dim.height * 0.5f - 2;
                    y = this.height * 0.5f;
                    Draw.drawLabelPoint_270((float) x, (float) y, this.getLabelFont(), this.label.getText(),
                            this.getLabelColor(), g, null, this.label.isUseExternalFont());
                    break;
            }
        }
    }
    // </editor-fold>
}
