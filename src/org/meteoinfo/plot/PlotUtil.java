/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.plot;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.PrintException;
import org.meteoinfo.chart.Chart;
import org.meteoinfo.chart.ChartPanel;
import org.meteoinfo.chart.plot.ChartPlotMethod;
import org.meteoinfo.chart.plot.XY1DPlot;
import org.meteoinfo.data.StationData;
import org.meteoinfo.data.XYArrayDataset;
import org.meteoinfo.data.XYDataset;

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
        return new XYArrayDataset(xdata, ydata, seriesKey);
    }
    
    /**
     * Create scatter plot
     * @param title Title
     * @param xAxisLabel X axis label
     * @param yAxisLabel Y axis label
     * @param dataset XYDataset
     * @return JFreeChart
     */
    public static Chart createScatterPlot(String title, String xAxisLabel, String yAxisLabel, XYDataset dataset){
        XY1DPlot plot = new XY1DPlot(dataset);
        plot.setChartPlotMethod(ChartPlotMethod.POINT);
        plot.getXAxis().setLabel(xAxisLabel);
        plot.getYAxis().setLabel(yAxisLabel);
        Chart chart = new Chart(title, plot);
        
        return chart;
    }
    
    /**
     * Save chart as PNG image file
     * @param fileName The file name
     * @param chart The chart
     * @param width Width
     * @param height Heigth
     */
    public static void exportToPicture(String fileName, Chart chart, int width, int height){
        try {
            ChartPanel cp = new ChartPanel(chart);
            cp.setSize(width, height);
            cp.exportToPicture(fileName);
        } catch (IOException ex) {
            Logger.getLogger(PlotUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (PrintException ex) {
            Logger.getLogger(PlotUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
