/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.plot;

import java.util.ArrayList;
import java.util.List;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.XYDataset;
import org.meteoinfo.data.StationData;
import org.meteoinfo.global.MIMath;

/**
 *
 * @author yaqiang
 */
public class PlotXYDataset extends AbstractXYDataset implements XYDataset {

    private double[][] xValues;
    private double[][] yValues;
    private int seriesCount;
    private int itemCount;
    private List<String> seriesKeys;
    
    /**
     * Constructor
     * @param xdata X station data
     * @param ydata Y station data
     * @param seriesKey Series key
     */
    public PlotXYDataset(StationData xdata, StationData ydata, String seriesKey){
        List<double[]> vdata = new ArrayList<double[]>();
        double v1, v2;
        for (int i = 0; i < xdata.getStNum(); i++){
            v1 = xdata.getValue(i);
            if (MIMath.doubleEquals(v1, xdata.missingValue))
                continue;
            v2 = ydata.getValue(i);
            if (MIMath.doubleEquals(v2, ydata.missingValue))
                continue;
            vdata.add(new double[]{v1, v2});
        }
        seriesCount = 1;
        seriesKeys = new ArrayList<String>();
        seriesKeys.add(seriesKey);
        itemCount = vdata.size();
        xValues = new double[seriesCount][itemCount];
        yValues = new double[seriesCount][itemCount];
        for (int i = 0; i < seriesCount; i++){
            for (int j = 0; j < itemCount; j++){
                xValues[i][j] = vdata.get(j)[0];
                yValues[i][j] = vdata.get(j)[1];
            }
        }
    }

    @Override
    public int getSeriesCount() {
        return this.seriesCount;
    }

    @Override
    public Comparable getSeriesKey(int i) {
        return seriesKeys.get(i);
    }

    @Override
    public int getItemCount(int i) {
        return itemCount;
    }

    @Override
    public Number getX(int seriesIdx, int itemIdx) {
        return xValues[seriesIdx][itemIdx];
    }

    @Override
    public Number getY(int seriesIdx, int itemIdx) {
        return yValues[seriesIdx][itemIdx];
    }

}
