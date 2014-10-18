/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.plot;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;
import org.meteoinfo.data.StationData;

/**
 *
 * @author yaqiang
 */
public class PlotUtil {
    /**
     * Get XYDataset from two StationData
     * @param xdata X station data
     * @param ydata Y station data
     * @param seriesKey Series key
     * @return XYDataset XYDataset
     */
    public static XYDataset getXYDataset(StationData xdata, StationData ydata, String seriesKey){
        return new PlotXYDataset(xdata, ydata, seriesKey);
    }
    
    /**
     * Create scatter plot
     * @param title Title
     * @param xAxisLabel X axis label
     * @param yAxisLabel Y axis label
     * @param dataset XYDataset
     * @return JFreeChart
     */
    public static JFreeChart createScatterPlot(String title, String xAxisLabel, String yAxisLabel, XYDataset dataset){
        JFreeChart chart = ChartFactory.createScatterPlot(title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, true, false);
        XYPlot plot = (XYPlot)chart.getPlot();
        plot.setBackgroundPaint(null);
        plot.setAxisOffset(RectangleInsets.ZERO_INSETS);
        
        return chart;
    }
    
    /**
     * Save chart as PNG image file
     * @param fileName The file name
     * @param chart The chart
     * @param width Width
     * @param height Heigth
     */
    public static void saveChartAsPNG(String fileName, JFreeChart chart, int width, int height){
        try {
            ChartUtilities.saveChartAsPNG(new File(fileName), chart, width, height);
        } catch (IOException ex) {
            Logger.getLogger(PlotUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
