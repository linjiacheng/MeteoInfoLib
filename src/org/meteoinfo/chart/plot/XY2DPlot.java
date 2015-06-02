/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.plot;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import org.meteoinfo.chart.ChartText;
import org.meteoinfo.data.Dataset;
import org.meteoinfo.global.Extent;
import org.meteoinfo.layer.MapLayer;
import org.meteoinfo.map.MapView;

/**
 *
 * @author wyq
 */
public class XY2DPlot extends XYPlot {

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
        this.setMapView(mapView, false);
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
     * @param isGeoMap If is geo map
     */
    public void setMapView(MapView value, boolean isGeoMap){
        this.mapView = value;
        this.mapView.setGeoMap(isGeoMap);
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
        this.mapView.setViewExtent((Extent)this.getDrawExtent().clone());
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
    
    @Override
    public void updateLegendScheme(){
        
    }
    
    @Override
    public void addText(ChartText text){
        
    }
    
    /**
     * Add a layer
     * @param layer The layer 
     */
    public void addLayer(MapLayer layer){
        this.mapView.addLayer(layer);
        this.setDrawExtent(layer.getExtent());
    }
    
    /**
     * Add a layer
     * @param idx Index
     * @param layer Layer
     */
    public void addLayer(int idx, MapLayer layer){
        this.mapView.addLayer(idx, layer);
        this.setDrawExtent(layer.getExtent());
    }
    // </editor-fold>            
}
