/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.EventListenerList;
import org.meteoinfo.chart.plot.XYPlot;
import org.meteoinfo.global.Extent;

/**
 *
 * @author yaqiang
 */
public class ChartPanel extends JPanel {

    // <editor-fold desc="Variables">
    private final EventListenerList listeners = new EventListenerList();
    private BufferedImage mapBitmap = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
    private Chart chart;
    private Point mouseDownPoint = new Point(0, 0);
    private Point mouseLastPos = new Point(0, 0);
    private boolean dragMode = false;
    private JPopupMenu popupMenu;
    private MouseMode mouseMode = MouseMode.ZOOM;
    private List<int[]> selectedPoints;
    // </editor-fold>

    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public ChartPanel() {
        super();
        this.setBackground(Color.white);
        this.setSize(200, 200);
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                onComponentResized(e);
            }
        });
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onMouseClicked(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                onMousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                onMouseReleased(e);
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                onMouseMoved(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                onMouseDragged(e);
            }
        });
        this.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                //onMouseWheelMoved(e);
            }
        });

        popupMenu = new JPopupMenu();
        JMenuItem undoZoom = new JMenuItem("Undo zoom");
        undoZoom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onUndoZoomClick(e);
            }
        });
        popupMenu.add(undoZoom);
        
        this.chart = null;
    }

    /**
     * Constructor
     *
     * @param chart Chart
     */
    public ChartPanel(Chart chart) {
        this();
        this.chart = chart;
    }

    // </editor-fold>
    // <editor-fold desc="Get set methods">
    /**
     * Get chart
     *
     * @return Chart
     */
    public Chart getChart() {
        return chart;
    }

    /**
     * Set chart
     *
     * @param value
     */
    public void setChart(Chart value) {
        chart = value;
    }

    /**
     * Get popup menu
     *
     * @return Popup menu
     */
    public JPopupMenu getPopupMenu() {
        return this.popupMenu;
    }

    /**
     * Get mouse mode
     *
     * @return Mouse mode
     */
    public MouseMode getMouseMode() {
        return this.mouseMode;
    }

    /**
     * Set mouse mode
     *
     * @param value Mouse mode
     */
    public void setMouseMode(MouseMode value) {
        this.mouseMode = value;
        switch (this.mouseMode) {
            case SELECT:
                this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                break;
            default:
                this.setCursor(Cursor.getDefaultCursor());
                break;
        }
    }

    /**
     * Get selected chart points
     *
     * @return Selected chart points
     */
    public List<int[]> getSelectedPoints() {
        return this.selectedPoints;
    }
    // </editor-fold>

    // <editor-fold desc="Events">
    public void addPointSelectedListener(IPointSelectedListener listener) {
        this.listeners.add(IPointSelectedListener.class, listener);
    }

    public void removePointSelectedListener(IPointSelectedListener listener) {
        this.listeners.remove(IPointSelectedListener.class, listener);
    }

    public void firePointSelectedEvent() {
        firePointSelectedEvent(new PointSelectedEvent(this));
    }

    private void firePointSelectedEvent(PointSelectedEvent event) {
        Object[] listeners = this.listeners.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == IPointSelectedListener.class) {
                ((IPointSelectedListener) listeners[i + 1]).pointSelectedEvent(event);
            }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Method">
    /**
     * Paint component
     *
     * @param g Graphics
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        //this.setBackground(Color.white);
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(mapBitmap, null, 0, 0);

        //Draw dynamic graphics
        if (this.dragMode) {
            int aWidth = Math.abs(mouseLastPos.x - mouseDownPoint.x);
            int aHeight = Math.abs(mouseLastPos.y - mouseDownPoint.y);
            int aX = Math.min(mouseLastPos.x, mouseDownPoint.x);
            int aY = Math.min(mouseLastPos.y, mouseDownPoint.y);
            g2.setColor(this.getForeground());
            float dash1[] = {2.0f};
            g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f));
            g2.draw(new Rectangle(aX, aY, aWidth, aHeight));
        }
    }

    /**
     * Paint graphics
     */
    public void paintGraphics() {
        this.mapBitmap = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);

        if (this.chart != null) {
            Graphics2D g = this.mapBitmap.createGraphics();
            Rectangle2D chartArea = new Rectangle2D.Double(0.0, 0.0, this.mapBitmap.getWidth(), this.mapBitmap.getHeight());
            this.chart.draw(g, chartArea);
        }
        this.repaint();
    }

    void onComponentResized(ComponentEvent e) {
        if (this.chart != null) {
            this.paintGraphics();
        }
    }

    void onMousePressed(MouseEvent e) {
        mouseDownPoint.x = e.getX();
        mouseDownPoint.y = e.getY();
        mouseLastPos = (Point) mouseDownPoint.clone();
    }

    void onMouseMoved(MouseEvent e) {
        this.dragMode = false;
    }

    void onMouseReleased(MouseEvent e) {
        this.dragMode = false;
        switch (this.mouseMode) {
            case ZOOM:
                if (Math.abs(mouseLastPos.x - mouseDownPoint.x) > 5) {
                    XYPlot plot = (XYPlot) this.chart.getPlot();
                    Rectangle2D graphArea = this.chart.getGraphArea();
                    if (graphArea.contains(mouseDownPoint.x, mouseDownPoint.y) || graphArea.contains(mouseLastPos.x, mouseLastPos.y)) {
                        double[] xy1 = plot.screenToProj(mouseDownPoint.x - graphArea.getX(), mouseDownPoint.y - graphArea.getY(), graphArea);
                        double[] xy2 = plot.screenToProj(mouseLastPos.x - graphArea.getX(), mouseLastPos.y - graphArea.getY(), graphArea);
                        Extent extent = new Extent();
                        extent.minX = Math.min(xy1[0], xy2[0]);
                        extent.maxX = Math.max(xy1[0], xy2[0]);
                        extent.minY = Math.min(xy1[1], xy2[1]);
                        extent.maxY = Math.max(xy1[1], xy2[1]);
                        if (plot.getXAxis().isInverse()){
                            Extent drawExtent = plot.getDrawExtent();
                            double minx, maxx;
                            minx = drawExtent.getWidth() - (extent.maxX - drawExtent.minX) + drawExtent.minX;
                            maxx = drawExtent.getWidth() - (extent.minX - drawExtent.minX) + drawExtent.minX;
                            extent.minX = minx;
                            extent.maxX = maxx;
                        }
                        if (plot.getYAxis().isInverse()){
                            Extent drawExtent = plot.getDrawExtent();
                            double miny, maxy;
                            miny = drawExtent.getHeight()- (extent.maxY - drawExtent.minY) + drawExtent.minY;
                            maxy = drawExtent.getHeight()- (extent.minY - drawExtent.minY) + drawExtent.minY;
                            extent.minY = miny;
                            extent.maxY = maxy;
                        }
                        plot.setDrawExtent(extent);
                        this.paintGraphics();
                    }
                }
                break;
            case SELECT:
                if (Math.abs(mouseLastPos.x - mouseDownPoint.x) > 5) {
                    XYPlot plot = (XYPlot) this.chart.getPlot();
                    Rectangle2D graphArea = this.chart.getGraphArea();
                    if (graphArea.contains(mouseDownPoint.x, mouseDownPoint.y) || graphArea.contains(mouseLastPos.x, mouseLastPos.y)) {
                        double[] xy1 = plot.screenToProj(mouseDownPoint.x - graphArea.getX(), mouseDownPoint.y - graphArea.getY(), graphArea);
                        double[] xy2 = plot.screenToProj(mouseLastPos.x - graphArea.getX(), mouseLastPos.y - graphArea.getY(), graphArea);
                        Extent extent = new Extent();
                        extent.minX = Math.min(xy1[0], xy2[0]);
                        extent.maxX = Math.max(xy1[0], xy2[0]);
                        extent.minY = Math.min(xy1[1], xy2[1]);
                        extent.maxY = Math.max(xy1[1], xy2[1]);
                        this.selectedPoints = plot.getDataset().selectPoints(extent);
                        this.firePointSelectedEvent();
                        this.paintGraphics();
                    }
                }
                break;
        }
    }

    void onMouseDragged(MouseEvent e) {
        this.dragMode = true;
        mouseLastPos.x = e.getX();
        mouseLastPos.y = e.getY();

        this.repaint();
    }

    void onMouseClicked(MouseEvent e) {
        int clickTimes = e.getClickCount();
        if (clickTimes == 1) {
            if (e.getButton() == MouseEvent.BUTTON3) {
                popupMenu.show(this, e.getX(), e.getY());
            }
        }
    }

    private void onUndoZoomClick(ActionEvent e) {
        XYPlot plot = (XYPlot) this.chart.getPlot();
        plot.setAutoExtent();
        this.paintGraphics();
    }
    // </editor-fold>
}
