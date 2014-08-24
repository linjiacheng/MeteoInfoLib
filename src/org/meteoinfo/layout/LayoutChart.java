/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.layout;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.JFreeChart;
import org.meteoinfo.global.PointF;

/**
 *
 * @author yaqiang
 */
public class LayoutChart extends LayoutElement {

    // <editor-fold desc="Variables">
    private JFreeChart chart;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public LayoutChart() {
        super();
        this.setElementType(ElementType.LayoutChart);
        this.setResizeAbility(ResizeAbility.ResizeAll);
        this.setWidth(200);
        this.setHeight(150);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get chart
     *
     * @return The chart
     */
    public JFreeChart getChart() {
        return chart;
    }

    /**
     * Set chart
     *
     * @param value The chart
     */
    public void setChart(JFreeChart value) {
        chart = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    @Override
    public void paint(Graphics2D g) {
    }

    @Override
    public void paintOnLayout(Graphics2D g, PointF pageLocation, float zoom) {
        if (chart == null)
            return;
        
        PointF aP = pageToScreen(this.getLeft(), this.getTop(), pageLocation, zoom);
        Rectangle2D area = new Rectangle2D.Double(aP.X, aP.Y, this.getWidth() * zoom, this.getHeight() * zoom);
        chart.draw(g, area);
    }

    @Override
    public void moveUpdate() {
    }

    @Override
    public void resizeUpdate() {
    }
    // </editor-fold>       
}
