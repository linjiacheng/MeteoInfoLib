/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data;

import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.MIMath;

/**
 *
 * @author wyq
 */
public class XYListDataset extends XYDataset {
    // <editor-fold desc="Variables">
    private List<double[]> xValues;
    private List<double[]> yValues;
    private List<String> seriesKeys;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public XYListDataset(){
        xValues = new ArrayList<double[]>();
        yValues = new ArrayList<double[]>();
        seriesKeys = new ArrayList<String>();
    }
    
    /**
     * Constructor
     * @param xdata X station data
     * @param ydata Y station data
     * @param seriesKey Series key
     */
    public XYListDataset(StationData xdata, StationData ydata, String seriesKey){
        this();
        List<double[]> vdata = new ArrayList<double[]>();
        double v1, v2;
        for (int i = 0; i < xdata.getStNum(); i++) {
            v1 = xdata.getValue(i);
            if (MIMath.doubleEquals(v1, xdata.missingValue)) {
                continue;
            }
            v2 = ydata.getValue(i);
            if (MIMath.doubleEquals(v2, ydata.missingValue)) {
                continue;
            }
            vdata.add(new double[]{v1, v2});
        }
        
        int n = vdata.size();
        double[] xvs = new double[n];
        double[] yvs = new double[n];
        for (int i = 0; i < n; i++){
            xvs[i] = vdata.get(i)[0];
            yvs[i] = vdata.get(i)[1];
        }
        this.xValues.add(xvs);
        this.yValues.add(yvs);
        this.seriesKeys.add(seriesKey);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
   
    @Override
    public int getSeriesCount() {
        return this.xValues.size();
    }
    
    @Override
    public String getSeriesKey(int seriesIdx) {
        return this.seriesKeys.get(seriesIdx);
    }
    
    /**
     * Get series keys
     * @return Series keys
     */
    @Override
    public List<String> getSeriesKeys(){
        return this.seriesKeys;
    }
    
    /**
     * Set series keys
     * @param value Series keys
     */
    @Override
    public void setSeriesKeys(List<String> value){
        this.seriesKeys = value;
    }
    
    @Override
    public int getItemCount(){
        int n = this.getItemCount(0);
        if (this.getSeriesCount() > 1){
            for (int i = 1; i < this.getSeriesCount(); i++){
                int nn = this.getItemCount(i);
                if (n < nn)
                    n = nn;
            }
        }
        
        return n;
    }

    public int getItemCount(int seriesIdx) {        
        return this.xValues.get(seriesIdx).length;
    }
    
    @Override
    public double[] getXValues(int seriesIdx){
        return this.xValues.get(seriesIdx);
    }
    
    @Override
    public double[] getYValues(int seriesIdx){
        return this.yValues.get(seriesIdx);
    }

    @Override
    public double getX(int seriesIdx, int itemIdx) {
        return this.xValues.get(seriesIdx)[itemIdx];
    }

    @Override
    public double getY(int seriesIdx, int itemIdx) {
        return this.yValues.get(seriesIdx)[itemIdx];
    }
    
    @Override
    public void setX(int seriesIdx, int itemIdx, double value){
        this.xValues.get(seriesIdx)[itemIdx] = value;
    }
    
    @Override
    public void setY(int seriesIdx, int itemIdx, double value){
        this.yValues.get(seriesIdx)[itemIdx] = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Add a series data 
     * @param seriesKey Series key
     * @param xvs X value array
     * @param yvs Y value array
     */
    public void addSeries(String seriesKey, double[] xvs, double[] yvs){
        this.seriesKeys.add(seriesKey);
        this.xValues.add(xvs);
        this.yValues.add(yvs);
    }
    
    /**
     * Add a series data 
     * @param seriesKey Series key
     * @param xvs X value array
     * @param yvs Y value array
     */
    public void addSeries(String seriesKey, List<Number> xvs, List<Number> yvs){
        double[] nxvs = new double[xvs.size()];
        double[] nyvs = new double[yvs.size()];
        for (int i = 0; i < xvs.size(); i++)
            nxvs[i] = Double.parseDouble(xvs.get(i).toString());
        for (int i = 0; i < yvs.size(); i++)
            nyvs[i] = Double.parseDouble(yvs.get(i).toString());
        
        this.addSeries(seriesKey, nxvs, nyvs);
    }
    
    /**
     * Remove a series data
     * @param seriesIdx Series data
     */
    public void removeSeries(int seriesIdx){
        this.seriesKeys.remove(seriesIdx);
        this.xValues.remove(seriesIdx);
        this.yValues.remove(seriesIdx);
    }
    
    /**
     * Remove a series data
     * @param seriesKey Series key
     */
    public void removeSeries(String seriesKey){
        int idx = this.seriesKeys.indexOf(seriesKey);
        if (idx >= 0){
            this.removeSeries(idx);
        }
    }
    
    /**
     * Get data extent
     * @return Data extent
     */
    @Override
    public Extent getDataExtent() {
        Extent cET = new Extent();
        double x, y;
        int n = 0;
        for (int i = 0; i < this.getSeriesCount(); i++) {
            for (int j = 0; j < this.getItemCount(i); j++) {                
                x = xValues.get(i)[j];
                y = yValues.get(i)[j];
                if (MIMath.doubleEquals(y, this.getMissingValue()) || MIMath.doubleEquals(x, this.getMissingValue()))
                    continue;
                if (n == 0) {
                    cET.minX = x;
                    cET.maxX = x;
                    cET.minY = y;
                    cET.maxY = y;
                } else {
                    if (cET.minX > x) {
                        cET.minX = x;
                    } else if (cET.maxX < x) {
                        cET.maxX = x;
                    }

                    if (cET.minY > y) {
                        cET.minY = y;
                    } else if (cET.maxY < y) {
                        cET.maxY = y;
                    }
                }
                n ++;
            }
        }

        return cET;
    }
    
    /**
     * Get missing value index list
     * @param seriesIdx Series index
     * @return Missing value index list
     */
    @Override
    public List<Integer> getMissingValueIndex(int seriesIdx){
        return new ArrayList<Integer>();
    }
    
    /**
     * Select data points
     * @param extent Selection extent
     * @return Selected data points
     */
    @Override
    public List<int[]> selectPoints(Extent extent){
        List<int[]> selIdxs = new ArrayList<int[]>();
        double x, y;
        for (int i = 0; i < this.getSeriesCount(); i++){
            for (int j = 0; j < this.getItemCount(i); j++){
                x = this.getX(i, j);
                if (x >= extent.minX && x <= extent.maxX){
                    y = this.getY(i, j);
                    if (y >= extent.minY && y <= extent.maxY){
                        selIdxs.add(new int[]{i, j});
                    }
                }                    
            }
        }
        
        return selIdxs;
    }
    // </editor-fold>               
}
