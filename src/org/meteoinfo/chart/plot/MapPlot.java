/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.plot;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.chart.ChartText;
import org.meteoinfo.chart.Location;
import org.meteoinfo.chart.axis.LonLatAxis;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import org.meteoinfo.global.PointF;
import org.meteoinfo.legend.LabelBreak;
import org.meteoinfo.legend.MapFrame;
import org.meteoinfo.map.GridLabel;
import org.meteoinfo.map.MapView;
import org.meteoinfo.projection.KnownCoordinateSystems;
import org.meteoinfo.projection.Reproject;
import org.meteoinfo.shape.Graphic;
import org.meteoinfo.shape.PointShape;

/**
 *
 * @author wyq
 */
public class MapPlot extends XY2DPlot {

    // <editor-fold desc="Variables">
    private MapFrame mapFrame;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public MapPlot() {
        super();
        this.setXAxis(new LonLatAxis("Longitude", true));
        this.setYAxis(new LonLatAxis("Latitude", false));
        this.getAxis(Location.RIGHT).setDrawTickLabel(false);
        this.setDrawNeatLine(true);
    }

    /**
     * Constructor
     *
     * @param mapView MapView
     */
    public MapPlot(MapView mapView) {
        this();
        this.setMapView(mapView, true);
        this.mapFrame = new MapFrame();
        this.mapFrame.setMapView(mapView);
    }

    /**
     * Constructor
     *
     * @param mapFrame MapFrame
     */
    public MapPlot(MapFrame mapFrame) {
        this();
        this.mapFrame = mapFrame;
        this.setMapView(mapFrame.getMapView(), true);
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get map frame
     * @return Map frame
     */
    public MapFrame getMapFrame(){
        return this.mapFrame;
    }
    
    /**
     * Set map frame
     * @param value Map frame
     */
    public void setMapFrame(MapFrame value){
        this.mapFrame = value;
        this.setMapView(mapFrame.getMapView(), true);
    }
    
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Set all axis visible or not
     * @param value Boolean
     */
    @Override
    public void setAxisOn(boolean value){
        super.setAxisOn(value);
        this.mapFrame.setDrawGridTickLine(value);
        this.mapFrame.setDrawGridLabel(value);
    }
    
    /**
     * Set longitude/latitude extent
     *
     * @param extent Extent
     */
    public void setLonLatExtent(Extent extent) {
        if (this.getMapView().getProjection().isLonLatMap()){
            super.setDrawExtent(extent);
        } else {
            this.getMapView().zoomToExtentLonLatEx(extent);
        }
    }
    
     @Override
    public void addText(ChartText text) {
        if (this.getMapView().getProjection().isLonLatMap()){
            super.addText(text);
        } else {
            PointShape ps = new PointShape();
            PointD lonlatp = new PointD(text.getX(), text.getY());
            PointD xyp = Reproject.reprojectPoint(lonlatp, KnownCoordinateSystems.geographic.world.WGS1984, 
                    this.getMapView().getProjection().getProjInfo());
            ps.setPoint(xyp);
            LabelBreak lb = new LabelBreak();
            lb.setText(text.getText());
            lb.setFont(text.getFont());
            lb.setColor(text.getColor());
            Graphic aGraphic = new Graphic(ps, lb);
            this.getMapView().addGraphic(aGraphic);
        }
    }
    
//    /**
//     * Add a layer
//     * @param idx Index
//     * @param layer Layer
//     */
//    public void addLayer(int idx, MapLayer layer){
//        this.mapFrame.addLayer(idx, layer);
//    }
//    
//    /**
//     * Add a layer
//     * @param layer Layer 
//     */
//    public void addLayer(MapLayer layer){
//        this.mapFrame.addLayer(layer);
//    }
//    
//    /**
//     * Set extent
//     *
//     * @param extent Extent
//     */
//    public void setExtent(Extent extent) {
//        this.mapFrame.getMapView().setViewExtent(extent);
//    }
    
    /**
     * Get position area
     * @param g Graphic2D
     * @param area Whole area
     * @return Graphic area
     */
    @Override
    public Rectangle2D getPositionArea(Graphics2D g, Rectangle2D area) {
        Rectangle2D plotArea = super.getPositionArea(g, area);
        MapView mapView = this.mapFrame.getMapView();
        mapView.setViewExtent((Extent)this.getDrawExtent().clone());
        Extent extent = mapView.getViewExtent();
        double width = extent.getWidth();
        double height = extent.getHeight();
        double scaleFactor = mapView.getXYScaleFactor();
        if (width / height / scaleFactor > plotArea.getWidth() / plotArea.getHeight()){
            double h = plotArea.getWidth() * height * scaleFactor / width;
            double delta = plotArea.getHeight() - h;
            plotArea.setRect(plotArea.getX(), plotArea.getY() + delta / 2, plotArea.getWidth(), h);
        } else {
            double w = width * plotArea.getHeight() / height / scaleFactor;
            double delta = plotArea.getWidth() - w;
            plotArea.setRect(plotArea.getX() + delta / 2, plotArea.getY(), w, plotArea.getHeight());
        }
        
        return plotArea;
    }
    
//    @Override
//    public void drawGraph(Graphics2D g, Rectangle2D area) {
//        MapView mapView = this.mapFrame.getMapView();
//        mapView.setViewExtent(this.getDrawExtent());
//        Extent extent = mapView.getViewExtent();
//        double width = extent.getWidth();
//        double height = extent.getHeight();
//        double scaleFactor = mapView.getXYScaleFactor();
//        if (width / height / scaleFactor > area.getWidth() / area.getHeight()){
//            double h = area.getWidth() * height * scaleFactor / width;
//            double delta = area.getHeight() - h;
//            area.setRect(area.getX(), area.getY() + delta / 2, area.getWidth(), h);
//        } else {
//            double w = width * area.getHeight() / height / scaleFactor;
//            double delta = area.getWidth() - w;
//            area.setRect(area.getX() + delta / 2, area.getY(), w, area.getHeight());
//        }
//        mapView.paintGraphics(g, area);
//    }

    @Override
    void drawAxis(Graphics2D g, Rectangle2D area) {
        if (this.mapFrame.getMapView().getProjection().isLonLatMap()){
            super.drawAxis(g, area);
            return;
        }
        
        //Draw lon/lat grid labels
        if (this.mapFrame.isDrawGridLabel()) {
            List<Extent> extentList = new ArrayList<>();
            Extent maxExtent = new Extent();
            Extent aExtent;
            Dimension aSF;
            g.setColor(this.mapFrame.getGridLineColor());
            g.setStroke(new BasicStroke(this.mapFrame.getGridLineSize()));
            String drawStr;
            PointF sP = new PointF(0, 0);
            PointF eP = new PointF(0, 0);
            Font font = new Font(this.mapFrame.getGridFont().getFontName(), this.mapFrame.getGridFont().getStyle(), (int) (this.mapFrame.getGridFont().getSize()));
            g.setFont(font);
            float labX, labY;
            int len = mapFrame.getTickLineLength();
            int space = len + mapFrame.getGridLabelShift();
            if (mapFrame.isInsideTickLine()) {
                space = mapFrame.getGridLabelShift();
            }

            for (int i = 0; i < mapFrame.getMapView().getGridLabels().size(); i++) {
                GridLabel aGL = mapFrame.getMapView().getGridLabels().get(i);
                switch (mapFrame.getGridLabelPosition()) {
                    case LeftBottom:
                        switch (aGL.getLabDirection()) {
                            case East:
                            case North:
                                continue;
                        }
                        break;
                    case LeftUp:
                        switch (aGL.getLabDirection()) {
                            case East:
                            case South:
                                continue;
                        }
                        break;
                    case RightBottom:
                        switch (aGL.getLabDirection()) {
                            case Weast:
                            case North:
                                continue;
                        }
                        break;
                    case RightUp:
                        switch (aGL.getLabDirection()) {
                            case Weast:
                            case South:
                                continue;
                        }
                        break;
                }

                labX = (float) aGL.getLabPoint().X;
                labY = (float) aGL.getLabPoint().Y;
                labX = labX + (float) area.getX();
                labY = labY + (float) area.getY();
                sP.X = labX;
                sP.Y = labY;

                drawStr = aGL.getLabString();
                //if (this.drawDegreeSymbol) {
                    if (drawStr.endsWith("E") || drawStr.endsWith("W") || drawStr.endsWith("N") || drawStr.endsWith("S")) {
                        drawStr = drawStr.substring(0, drawStr.length() - 1) + String.valueOf((char) 186) + drawStr.substring(drawStr.length() - 1);
                    } else {
                        drawStr = drawStr + String.valueOf((char) 186);
                    }
                //}
                FontMetrics metrics = g.getFontMetrics(font);
                aSF = new Dimension(metrics.stringWidth(drawStr), metrics.getHeight());
                switch (aGL.getLabDirection()) {
                    case South:
                        labX = labX - aSF.width / 2;
                        labY = labY + aSF.height * 3 / 4 + space;
                        eP.X = sP.X;
                        if (mapFrame.isInsideTickLine()) {
                            eP.Y = sP.Y - len;
                        } else {
                            eP.Y = sP.Y + len;
                        }
                        break;
                    case Weast:
                        labX = labX - aSF.width - space;
                        labY = labY + aSF.height / 3;
                        eP.Y = sP.Y;
                        if (mapFrame.isInsideTickLine()) {
                            eP.X = sP.X + len;
                        } else {
                            eP.X = sP.X - len;
                        }
                        break;
                    case North:
                        labX = labX - aSF.width / 2;
                        //labY = labY - aSF.height / 3 - space;
                        labY = labY - space;
                        eP.X = sP.X;
                        if (mapFrame.isInsideTickLine()) {
                            eP.Y = sP.Y + len;
                        } else {
                            eP.Y = sP.Y - len;
                        }
                        break;
                    case East:
                        labX = labX + space;
                        labY = labY + aSF.height / 3;
                        eP.Y = sP.Y;
                        if (mapFrame.isInsideTickLine()) {
                            eP.X = sP.X - len;
                        } else {
                            eP.X = sP.X + len;
                        }
                        break;
                }

                boolean ifDraw = true;
                aExtent = new Extent();
                aExtent.minX = labX;
                aExtent.maxX = labX + aSF.width;
                aExtent.minY = labY - aSF.height;
                aExtent.maxY = labY;

                //Judge extent                                        
                if (extentList.isEmpty()) {
                    maxExtent = (Extent) aExtent.clone();
                    extentList.add((Extent) aExtent.clone());
                } else {
                    if (!MIMath.isExtentCross(aExtent, maxExtent)) {
                        extentList.add((Extent) aExtent.clone());
                        maxExtent = MIMath.getLagerExtent(maxExtent, aExtent);
                    } else {
                        for (int j = 0; j < extentList.size(); j++) {
                            if (MIMath.isExtentCross(aExtent, extentList.get(j))) {
                                ifDraw = false;
                                break;
                            }
                        }
                        if (ifDraw) {
                            extentList.add(aExtent);
                            maxExtent = MIMath.getLagerExtent(maxExtent, aExtent);
                        }
                    }
                }

                if (ifDraw) {
                    g.setColor(mapFrame.getGridLineColor());
                    g.draw(new Line2D.Float(sP.X, sP.Y, eP.X, eP.Y));
                    g.setColor(this.mapFrame.getForeColor());
                    g.drawString(drawStr, labX, labY);
                }
            }
        }
    }
    // </editor-fold>
}
