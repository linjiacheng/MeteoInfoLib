/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.plot;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import org.meteoinfo.data.Dataset;
import org.meteoinfo.global.Extent;
import org.meteoinfo.map.MapView;

/**
 *
 * @author wyq
 */
public final class XY2DPlot extends XYPlot {

    // <editor-fold desc="Variables">
    private MapView mapView;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public XY2DPlot() {
        super();
    }
    
    /**
     * Constructor
     * @param mapView Map view
     */
    public XY2DPlot(MapView mapView){
        this();
        this.setMapView(mapView);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    @Override
    public Dataset getDataset() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDataset(Dataset dataset) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Get map view
     * @return Map view
     */
    public MapView getMapView(){
        return this.mapView;
    }
    
    /**
     * Set map view
     * @param value Map view
     */
    public void setMapView(MapView value){
        this.mapView = value;
        this.mapView.setGeoMap(false);
        Extent extent = this.getAutoExtent();
        this.setDrawExtent(extent);
    }

    @Override
    public PlotType getPlotType() {
        return PlotType.XY2D;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    
    @Override
    void drawGraph(Graphics2D g, Rectangle2D area) {
        this.mapView.paintGraphics(g, area);
    }
    
    /**
     * Get auto extent
     *
     * @return Auto extent
     */
    @Override
    public Extent getAutoExtent() {
        return this.mapView.getLayersWholeExtent();
    }
    // </editor-fold>            
}
