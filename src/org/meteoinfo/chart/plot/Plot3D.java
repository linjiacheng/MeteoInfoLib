/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.plot;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Label;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import org.meteoinfo.chart.ChartLegend;
import org.meteoinfo.chart.ChartText;
import org.meteoinfo.chart.Margin;
import org.meteoinfo.chart.plot3d.DefaultSurfaceModel;
import org.meteoinfo.chart.plot3d.surface.Projector;
import org.meteoinfo.chart.plot3d.surface.SurfaceColor;
import org.meteoinfo.chart.plot3d.surface.SurfaceModel;
import org.meteoinfo.chart.plot3d.surface.SurfaceVertex;
import org.meteoinfo.data.Dataset;
import org.meteoinfo.drawing.Draw;

/**
 *
 * @author Yaqiang Wang
 */
public class Plot3D extends Plot {

    // <editor-fold desc="Variables">
    private JPanel panel;
    private ChartText title;
    private List<ChartLegend> legends;
    
    private SurfaceModel model; // the parent, Surface Plotter model
    private Projector projector; // the projector, controls the point of view
    private SurfaceVertex[][] surfaceVertex; // vertices array 
    private boolean data_available; // data availability flag
    private boolean interrupted; // interrupted flag
    private boolean critical; // for speed up
    private boolean printing; // printing flag
    private int prevwidth, prevheight; // canvas size
    private int printwidth, printheight; // print size
    private SurfaceVertex cop; // center of projection

    private int curve = 0;

    //private Graphics graphics; // the actual graphics used by all private
    // methods

    // setting variables
    private SurfaceModel.PlotType plot_type;

    private int calc_divisions;
    private int ycalc_divisions;
    private boolean plotfunc1, plotfunc2, plotboth;
    private boolean isBoxed, isMesh, isScaleBox, isDisplayXY, isDisplayZ,
            isDisplayGrids;
    private float xmin, xmax, ymin;
    private float ymax, zmin, zmax;

    private String xLabel = "X";
    private String yLabel = "Y";

    // constants
    private static final int TOP = 0;
    private static final int CENTER = 1;

    // for splitting polygons
    private static final int UPPER = 1;
    private static final int COINCIDE = 0;
    private static final int LOWER = -1;

    SurfaceColor colors;
    private Plot3D.JSurfaceChangesListener surfaceChangesListener;
    private static JPanel lastFocused;
    
    private boolean is_data_available; // holds the original data availability
    // flag
    private boolean dragged; // dragged flag
    private int click_x, click_y; // previous mouse cursor position
    
    private float color_factor;
    private int factor_x, factor_y; // conversion factors
    private int t_x, t_y, t_z; // determines ticks density
    private final SurfaceVertex values1[] = new SurfaceVertex[4];
    private final SurfaceVertex values2[] = new SurfaceVertex[4];
    private final int poly_x[] = new int[9];
    private final int poly_y[] = new int[9];
    private Point projection;
    private final SurfaceVertex upperpart[] = new SurfaceVertex[8];
    private final SurfaceVertex lowerpart[] = new SurfaceVertex[8];
    
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     * @param panel Parent panel
     */
    public Plot3D(JPanel panel){
        this(panel, new DefaultSurfaceModel());
    }
    
    /**
     * Constructor
     * @param panel Parent panel
     * @param model Surface model 
     */
    public Plot3D(JPanel panel, SurfaceModel model){
        this.panel = panel;
        this.legends = new ArrayList<>();
        surfaceChangesListener = new Plot3D.JSurfaceChangesListener();
        Plot3D.JSurfaceMouseListener my = new Plot3D.JSurfaceMouseListener();
        if (this.panel != null){
            this.panel.addMouseListener(my);
            this.panel.addMouseMotionListener(my);
            this.panel.addMouseWheelListener(my);
            this.panel.addFocusListener(new FocusAdapter() {

                @Override
                public void focusGained(FocusEvent e) {
                    // keep track of the last focused Jsurface to connect actions to them
                    lastFocused = Plot3D.this.panel;
                }
            });
        }
        setModel(model);
    }
    
    // </editor-fold>
    // <editor-fold desc="GetSet">
    
    public SurfaceModel getModel() {
        return model;
    }

    public void setModel(SurfaceModel model) {

        if (this.model != null) {
            model.removePropertyChangeListener(surfaceChangesListener);
        }
        if (this.model != null) {
            model.removeChangeListener(surfaceChangesListener);
        }

        if (model == null) {
            model = new DefaultSurfaceModel();
        }

        this.model = model;
        interrupted = false;
        data_available = false;
        printing = false;
        // contour = density = false;
        prevwidth = prevheight = -1;
        projector = model.getProjector();
        surfaceVertex = new SurfaceVertex[2][];

        model.addPropertyChangeListener(surfaceChangesListener);
        model.addChangeListener(surfaceChangesListener);
        init(); // fill all availables properties
    }
    
    /**
     * Get projector
     * @return The Projector
     */
    public Projector getProjector(){
        return this.projector;
    }
    
    /**
     * Sets the new vertices array of surface.
     * @param vertex Surface vertex values
     * @see #getValuesArray
     */
    public void setValuesArray(SurfaceVertex[][] vertex) {
        this.surfaceVertex = vertex;
    }
    
    /**
     * Get title
     *
     * @return Title
     */
    public ChartText getTitle() {
        return this.title;
    }

    /**
     * Set title
     *
     * @param value Title
     */
    public void setTitle(ChartText value) {
        this.title = value;
    }

    /**
     * Set title
     *
     * @param text Title text
     */
    public void setTitle(String text) {
        if (this.title == null) {
            this.title = new ChartText(text);
        } else {
            this.title.setText(text);
        }
    }
    
    /**
     * Get legends
     *
     * @return Legends
     */
    public List<ChartLegend> getLegends() {
        return this.legends;
    }

    /**
     * Get chart legend
     *
     * @param idx Index
     * @return Chart legend
     */
    public ChartLegend getLegend(int idx) {
        if (this.legends.isEmpty()) {
            return null;
        } else {
            return this.legends.get(idx);
        }
    }

    /**
     * Get chart legend
     *
     * @return Chart legend
     */
    public ChartLegend getLegend() {
        if (this.legends.isEmpty()) {
            return null;
        } else {
            return this.legends.get(this.legends.size() - 1);
        }
    }

    /**
     * Set chart legend
     *
     * @param value Legend
     */
    public void setLegend(ChartLegend value) {
        this.legends.clear();
        this.legends.add(value);
    }

    /**
     * Set legends
     *
     * @param value Legends
     */
    public void setLegends(List<ChartLegend> value) {
        this.legends = value;
    }
    
    // </editor-fold>
    // <editor-fold desc="Methods">
    
    /**
     * Sets the x and y ranges of calculated surface vertices. The ranges will
     * not affect surface appearance. They affect axes scale appearance.
     *
     * @param xmin the minimum x
     * @param xmax the maximum x
     * @param ymin the minimum y
     * @param ymax the maximum y
     */
    public void setRanges(float xmin, float xmax, float ymin, float ymax) {
        this.xmin = xmin;
        this.xmax = xmax;
        this.ymin = ymin;
        this.ymax = ymax;
    }

    /**
     * Gets the current x, y, and z ranges.
     *
     * @return array of x,y, and z ranges in order of xmin, xmax, ymin, ymax,
     * zmin, zmax
     */
    public float[] getRanges() {
        float[] ranges = new float[6];

        ranges[0] = xmin;
        ranges[1] = xmax;
        ranges[2] = ymin;
        ranges[3] = ymax;
        ranges[4] = zmin;
        ranges[5] = zmax;

        return ranges;
    }
    
    private void init() {
        colors = model.getColorModel();
        setRanges(model.getXMin(), model.getXMax(), model.getYMin(), model.getYMax());

        data_available = model.isDataAvailable();
        if (data_available) {
            setValuesArray(model.getSurfaceVertex());
        }

        plot_type = model.getPlotType();

        isBoxed = model.isBoxed();
        isMesh = model.isMesh();
        isScaleBox = model.isScaleBox();
        isDisplayXY = model.isDisplayXY();
        isDisplayZ = model.isDisplayZ();
        isDisplayGrids = model.isDisplayGrids();
        calc_divisions = model.getCalcDivisions();
        ycalc_divisions = model.getYCalcDivisions();
        plotfunc1 = model.isPlotFunction1();
        plotfunc2 = model.isPlotFunction2();
        plotboth = plotfunc1 && plotfunc2;
    }

    /**
     * Destroys the internal image. It will force <code>SurfaceCanvas</code> to
     * regenerate all images when the <code>paint</code> method is called.
     */
    public void destroyImage() {
        repaint();
    }
    
    private void repaint() {
        
    }
    
    private boolean is3D() {
        return (plot_type == SurfaceModel.PlotType.WIREFRAME || plot_type == SurfaceModel.PlotType.SURFACE);
    }
    
    /**
     * Add a legend
     *
     * @param legend The legend
     */
    public void addLegend(ChartLegend legend) {
        this.legends.add(legend);
    }

    /**
     * Remove a legend
     *
     * @param legend The legend
     */
    public void removeLegend(ChartLegend legend) {
        this.legends.remove(legend);
    }

    /**
     * Remove a legend by index
     *
     * @param idx The legend index
     */
    public void removeLegend(int idx) {
        this.legends.remove(idx);
    }
    
    /**
     * Get outer position area
     *
     * @param area Whole area
     * @return Position area
     */
    @Override
    public Rectangle2D getOuterPositionArea(Rectangle2D area) {
        Rectangle2D rect = this.getOuterPosition();
        double x = area.getWidth() * rect.getX() + area.getX();
        double y = area.getHeight() * (1 - rect.getHeight() - rect.getY()) + area.getY();
        double w = area.getWidth() * rect.getWidth();
        double h = area.getHeight() * rect.getHeight();
        return new Rectangle2D.Double(x, y, w, h);
    }

    @Override
    public Dataset getDataset() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDataset(Dataset dataset) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PlotType getPlotType() {
        return PlotType.XYZ;
    }

    @Override
    public void draw(Graphics2D g2, Rectangle2D area) {
        // backing buffer creation
        Rectangle parea = this.getPositionArea(area).getBounds();
        if ((parea.width != prevwidth) || (parea.height != prevheight)) {
            // model.setMessage("New image size: " + getBounds().width + "x" +
            // getBounds().height);            
            // if (Buffer != null) Buffer.flush();
            // Buffer = createImage(getBounds().width, getBounds().height);
            // if (graphics != null) graphics.dispose();
            // graphics = Buffer.getGraphics();
            prevwidth = parea.width;
            prevheight = parea.height;
        }
        projector.setProjectionArea(parea);                
        
        SurfaceVertex.invalidate();
        this.setGraphArea(this.getPositionArea());
        //Draw title
        float y = this.drawTitle(g2, this.getGraphArea());
        plotSurface(g2, parea);
    }
    
    float drawTitle(Graphics2D g, Rectangle2D graphArea) {
        float y = (float) graphArea.getY() - (float) this.getTightInset().getTop();
        if (title != null) {
            g.setColor(title.getColor());
            g.setFont(title.getFont());
            float x = (float) (graphArea.getX() + graphArea.getWidth() / 2);
            y += 5;
            for (String text : title.getTexts()) {
                Dimension dim = Draw.getStringDimension(text, g);
                y += dim.height;
                Draw.drawString(g, text, x - dim.width / 2, y);
                g.setFont(title.getFont());
                y += title.getLineSpace();
            }
        }
        return y;
    }
    
    /**
     * Creates a surface plot
     */
    private void plotSurface(Graphics2D g2, Rectangle2D area) {
        float zi, zx;
        int sx, sy;
        int start_lx, end_lx;
        int start_ly, end_ly;

        try {
            zi = model.getZMin();
            zx = model.getZMax();
            if (zi >= zx) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            return;
        }

        int plot_density = model.getDispDivisions();
        int yplot_density = model.getYDispDivisions();
        int multiple_factor = calc_divisions / plot_density;
        int ymultiple_factor = ycalc_divisions / yplot_density;
        // model.setDispDivisions(plot_density);

        Thread.yield();
        zmin = zi;
        zmax = zx;
        color_factor = 1f / (zmax - zmin);
        
        if (!printing) {
            g2.setColor(colors.getBackgroundColor());
            //g2.setColor(Color.cyan);
            g2.fill(area);
        }

        drawBoxGridsTicksLabels(g2, false);

        if (!plotfunc1 && !plotfunc2) {
            if (isBoxed) {
                drawBoundingBox(g2);
            }
            return;
        }

        projector.setZRange(zmin, zmax);

        // direction test
        float distance = projector.getDistance() * projector.getCosElevationAngle();

        // cop : center of projection
        // OMG there is a new SurfaceVertex every time !
        cop = new SurfaceVertex(distance * projector.getSinRotationAngle(), distance * projector.getCosRotationAngle(), projector.getDistance() * projector.getSinElevationAngle());
        cop.transform(projector);

        boolean inc_x = cop.x > 0;
        boolean inc_y = cop.y > 0;

        critical = false;

        if (inc_x) {
            start_lx = 0;
            end_lx = calc_divisions;
            sx = multiple_factor;
        } else {
            start_lx = calc_divisions;
            end_lx = 0;
            sx = -multiple_factor;
        }
        if (inc_y) {
            start_ly = 0;
            end_ly = ycalc_divisions;
            sy = ymultiple_factor;
        } else {
            start_ly = ycalc_divisions;
            end_ly = 0;
            sy = -ymultiple_factor;
        }

        if ((cop.x > 10) || (cop.x < -10)) {
            if ((cop.y > 10) || (cop.y < -10)) {
                plotArea(g2, start_lx, start_ly, end_lx, end_ly, sx, sy);
            } else { // split in y direction
                int split_y = (int) ((cop.y + 10) * yplot_density / 20) * ymultiple_factor;
                plotArea(g2, start_lx, 0, end_lx, split_y, sx, ymultiple_factor);
                plotArea(g2, start_lx, ycalc_divisions, end_lx, split_y, sx, -ymultiple_factor);
            }
        } else if ((cop.y > 10) || (cop.y < -10)) { // split in x direction
            int split_x = (int) ((cop.x + 10) * plot_density / 20) * multiple_factor;
            plotArea(g2, 0, start_ly, split_x, end_ly, multiple_factor, sy);
            plotArea(g2, calc_divisions, start_ly, split_x, end_ly, -multiple_factor, sy);
        } else { // split in both x and y directions
            int split_x = (int) ((cop.x + 10) * plot_density / 20) * multiple_factor;
            int split_y = (int) ((cop.y + 10) * yplot_density / 20) * ymultiple_factor;
            critical = true;
            plotArea(g2, 0, 0, split_x, split_y, multiple_factor, ymultiple_factor);
            plotArea(g2, 0, ycalc_divisions, split_x, split_y, multiple_factor, -ymultiple_factor);
            plotArea(g2, calc_divisions, 0, split_x, split_y, -multiple_factor, ymultiple_factor);
            plotArea(g2, calc_divisions, ycalc_divisions, split_x, split_y, -multiple_factor, -ymultiple_factor);
        }

        if (isBoxed) {
            drawBoundingBox(g2);
        }
    }
    
    /**
     * Draws the base plane. The base plane is the x-y plane.
     *
     * @param g the graphics context to draw.
     * @param x used to retrieve x coordinates of drawn plane from this method.
     * @param y used to retrieve y coordinates of drawn plane from this method.
     */
    private void drawBase(Graphics g, int[] x, int[] y) {
        Point p = projector.project(-10, -10, -10);
        x[0] = p.x;
        y[0] = p.y;
        p = projector.project(-10, 10, -10);
        x[1] = p.x;
        y[1] = p.y;
        p = projector.project(10, 10, -10);
        x[2] = p.x;
        y[2] = p.y;
        p = projector.project(10, -10, -10);
        x[3] = p.x;
        y[3] = p.y;
        x[4] = x[0];
        y[4] = y[0];

        g.setColor(colors.getBoxColor());
        g.fillPolygon(x, y, 4);

        g.setColor(colors.getLineBoxColor());
        g.drawPolygon(x, y, 5);
    }
    
    /**
     * Draws string at the specified coordinates with the specified alignment.
     *
     * @param g graphics context to draw
     * @param x the x coordinate
     * @param y the y coordinate
     * @param s the string to draw
     * @param x_align the alignment in x direction
     * @param y_align the alignment in y direction
     */
    private void outString(Graphics g, int x, int y, String s, int x_align, int y_align) {
        switch (y_align) {
            case TOP:
                y += g.getFontMetrics(g.getFont()).getAscent();
                break;
            case CENTER:
                y += g.getFontMetrics(g.getFont()).getAscent() / 2;
                break;
        }
        switch (x_align) {
            case Label.LEFT:
                g.drawString(s, x, y);
                break;
            case Label.RIGHT:
                g.drawString(s, x - g.getFontMetrics(g.getFont()).stringWidth(s), y);
                break;
            case Label.CENTER:
                g.drawString(s, x - g.getFontMetrics(g.getFont()).stringWidth(s) / 2, y);
                break;
        }
    }
    
    /**
     * Sets the axes scaling factor. Computes the proper axis lengths based on
     * the ratio of variable ranges. The axis lengths will also affect the size
     * of bounding box.
     */
    private void setAxesScale() {
        float scale_x, scale_y, scale_z, divisor;
        int longest;

        if (!isScaleBox) {
            projector.setScaling(1);
            t_x = t_y = t_z = 4;
            return;
        }

        scale_x = xmax - xmin;
        scale_y = ymax - ymin;
        scale_z = zmax - zmin;

        if (scale_x < scale_y) {
            if (scale_y < scale_z) {
                longest = 3;
                divisor = scale_z;
            } else {
                longest = 2;
                divisor = scale_y;
            }
        } else if (scale_x < scale_z) {
            longest = 3;
            divisor = scale_z;
        } else {
            longest = 1;
            divisor = scale_x;
        }
        scale_x /= divisor;
        scale_y /= divisor;
        scale_z /= divisor;

        if ((scale_x < 0.2f) || (scale_y < 0.2f) && (scale_z < 0.2f)) {
            switch (longest) {
                case 1:
                    if (scale_y < scale_z) {
                        scale_y /= scale_z;
                        scale_z = 1.0f;
                    } else {
                        scale_z /= scale_y;
                        scale_y = 1.0f;
                    }
                    break;
                case 2:
                    if (scale_x < scale_z) {
                        scale_x /= scale_z;
                        scale_z = 1.0f;
                    } else {
                        scale_z /= scale_x;
                        scale_x = 1.0f;
                    }
                    break;
                case 3:
                    if (scale_y < scale_x) {
                        scale_y /= scale_x;
                        scale_x = 1.0f;
                    } else {
                        scale_x /= scale_y;
                        scale_y = 1.0f;
                    }
                    break;
            }
        }
        if (scale_x < 0.2f) {
            scale_x = 1.0f;
        }
        projector.setXScaling(scale_x);
        if (scale_y < 0.2f) {
            scale_y = 1.0f;
        }
        projector.setYScaling(scale_y);
        if (scale_z < 0.2f) {
            scale_z = 1.0f;
        }
        projector.setZScaling(scale_z);

        if (scale_x < 0.5f) {
            t_x = 8;
        } else {
            t_x = 4;
        }
        if (scale_y < 0.5f) {
            t_y = 8;
        } else {
            t_y = 4;
        }
        if (scale_z < 0.5f) {
            t_z = 8;
        } else {
            t_z = 4;
        }
    }
    
    /**
     * Draws float at the specified coordinates with the specified alignment.
     *
     * @param g graphics context to draw
     * @param x the x coordinate
     * @param y the y coordinate
     * @param f the float to draw
     * @param x_align the alignment in x direction
     * @param y_align the alignment in y direction
     */
    private void outFloat(Graphics g, int x, int y, float f, int x_align, int y_align) {
        // String s = Float.toString(f);
        String s = format(f);
        outString(g, x, y, s, x_align, y_align);
    }
    
    private String format(float f) {
        return String.format("%.3G", f);
    }
    
    /**
     * Draws non-surface parts, i.e: bounding box, axis grids, axis ticks, axis
     * labels, base plane.
     *
     * @param g the graphics context to draw
     * @param draw_axes if <code>true</code>, only draws base plane and z axis
     */
    private void drawBoxGridsTicksLabels(Graphics g, boolean draw_axes) {
        Point projection, tickpos;
        boolean x_left = false, y_left = false;
        int x[], y[], i;

        x = new int[5];
        y = new int[5];
        if (projector == null) {
            return;
        }

        if (draw_axes) {
            drawBase(g, x, y);
            projection = projector.project(0, 0, -10);
            x[0] = projection.x;
            y[0] = projection.y;
            projection = projector.project(10.5f, 0, -10);
            g.drawLine(x[0], y[0], projection.x, projection.y);
            if (projection.x < x[0]) {
                outString(g, (int) (1.05 * (projection.x - x[0])) + x[0], (int) (1.05 * (projection.y - y[0])) + y[0], "x", Label.RIGHT, TOP);
            } else {
                outString(g, (int) (1.05 * (projection.x - x[0])) + x[0], (int) (1.05 * (projection.y - y[0])) + y[0], "x", Label.LEFT, TOP);
            }
            projection = projector.project(0, 11.5f, -10);
            g.drawLine(x[0], y[0], projection.x, projection.y);
            if (projection.x < x[0]) {
                outString(g, (int) (1.05 * (projection.x - x[0])) + x[0], (int) (1.05 * (projection.y - y[0])) + y[0], "y", Label.RIGHT, TOP);
            } else {
                outString(g, (int) (1.05 * (projection.x - x[0])) + x[0], (int) (1.05 * (projection.y - y[0])) + y[0], "y", Label.LEFT, TOP);
            }
            projection = projector.project(0, 0, 10.5f);
            g.drawLine(x[0], y[0], projection.x, projection.y);
            outString(g, (int) (1.05 * (projection.x - x[0])) + x[0], (int) (1.05 * (projection.y - y[0])) + y[0], "z", Label.CENTER, CENTER);
        } else {
            factor_x = factor_y = 1;
            projection = projector.project(0, 0, -10);
            x[0] = projection.x;
            projection = projector.project(10.5f, 0, -10);
            y_left = projection.x > x[0];
            i = projection.y;
            projection = projector.project(-10.5f, 0, -10);
            if (projection.y > i) {
                factor_x = -1;
                y_left = projection.x > x[0];
            }
            projection = projector.project(0, 10.5f, -10);
            x_left = projection.x > x[0];
            i = projection.y;
            projection = projector.project(0, -10.5f, -10);
            if (projection.y > i) {
                factor_y = -1;
                x_left = projection.x > x[0];
            }
            setAxesScale();
            drawBase(g, x, y);

            if (isBoxed) {
                projection = projector.project(-factor_x * 10, -factor_y * 10, -10);
                x[0] = projection.x;
                y[0] = projection.y;
                projection = projector.project(-factor_x * 10, -factor_y * 10, 10);
                x[1] = projection.x;
                y[1] = projection.y;
                projection = projector.project(factor_x * 10, -factor_y * 10, 10);
                x[2] = projection.x;
                y[2] = projection.y;
                projection = projector.project(factor_x * 10, -factor_y * 10, -10);
                x[3] = projection.x;
                y[3] = projection.y;
                x[4] = x[0];
                y[4] = y[0];

                g.setColor(colors.getBoxColor());
                g.fillPolygon(x, y, 4);

                g.setColor(colors.getLineBoxColor());
                g.drawPolygon(x, y, 5);

                projection = projector.project(-factor_x * 10, factor_y * 10, 10);
                x[2] = projection.x;
                y[2] = projection.y;
                projection = projector.project(-factor_x * 10, factor_y * 10, -10);
                x[3] = projection.x;
                y[3] = projection.y;
                x[4] = x[0];
                y[4] = y[0];

                g.setColor(colors.getBoxColor());
                g.fillPolygon(x, y, 4);

                g.setColor(colors.getLineBoxColor());
                g.drawPolygon(x, y, 5);
            } else if (isDisplayZ) {
                projection = projector.project(factor_x * 10, -factor_y * 10, -10);
                x[0] = projection.x;
                y[0] = projection.y;
                projection = projector.project(factor_x * 10, -factor_y * 10, 10);
                g.drawLine(x[0], y[0], projection.x, projection.y);

                projection = projector.project(-factor_x * 10, factor_y * 10, -10);
                x[0] = projection.x;
                y[0] = projection.y;
                projection = projector.project(-factor_x * 10, factor_y * 10, 10);
                g.drawLine(x[0], y[0], projection.x, projection.y);
            }

            for (i = -9; i <= 9; i++) {
                if (isDisplayXY || isDisplayGrids) {
                    if (!isDisplayGrids || (i % (t_y / 2) == 0) || isDisplayXY) {
                        if (isDisplayGrids && (i % t_y == 0)) {
                            projection = projector.project(-factor_x * 10, i, -10);
                        } else if (i % t_y != 0) {
                            projection = projector.project(factor_x * 9.8f, i, -10);
                        } else {
                            projection = projector.project(factor_x * 9.5f, i, -10);
                        }
                        tickpos = projector.project(factor_x * 10, i, -10);
                        g.drawLine(projection.x, projection.y, tickpos.x, tickpos.y);
                        if ((i % t_y == 0) && isDisplayXY) {
                            tickpos = projector.project(factor_x * 10.5f, i, -10);
                            if (y_left) {
                                outFloat(g, tickpos.x, tickpos.y, (float) ((double) (i + 10) / 20 * (ymax - ymin) + ymin), Label.LEFT, TOP);
                            } else {
                                outFloat(g, tickpos.x, tickpos.y, (float) ((double) (i + 10) / 20 * (ymax - ymin) + ymin), Label.RIGHT, TOP);
                            }
                        }
                    }
                    if (!isDisplayGrids || (i % (t_x / 2) == 0) || isDisplayXY) {
                        if (isDisplayGrids && (i % t_x == 0)) {
                            projection = projector.project(i, -factor_y * 10, -10);
                        } else if (i % t_x != 0) {
                            projection = projector.project(i, factor_y * 9.8f, -10);
                        } else {
                            projection = projector.project(i, factor_y * 9.5f, -10);
                        }
                        tickpos = projector.project(i, factor_y * 10, -10);
                        g.drawLine(projection.x, projection.y, tickpos.x, tickpos.y);
                        if ((i % t_x == 0) && isDisplayXY) {
                            tickpos = projector.project(i, factor_y * 10.5f, -10);
                            if (x_left) {
                                outFloat(g, tickpos.x, tickpos.y, (float) ((double) (i + 10) / 20 * (xmax - xmin) + xmin), Label.LEFT, TOP);
                            } else {
                                outFloat(g, tickpos.x, tickpos.y, (float) ((double) (i + 10) / 20 * (xmax - xmin) + xmin), Label.RIGHT, TOP);
                            }
                        }
                    }
                }

                if (isDisplayXY) {
                    tickpos = projector.project(0, factor_y * 14, -10);
                    outString(g, tickpos.x, tickpos.y, xLabel, Label.CENTER, TOP);
                    tickpos = projector.project(factor_x * 14, 0, -10);
                    outString(g, tickpos.x, tickpos.y, yLabel, Label.CENTER, TOP);
                }

                // z grids and ticks
                if (isDisplayZ || (isDisplayGrids && isBoxed)) {
                    if (!isDisplayGrids || (i % (t_z / 2) == 0) || isDisplayZ) {
                        if (isBoxed && isDisplayGrids && (i % t_z == 0)) {
                            projection = projector.project(-factor_x * 10, -factor_y * 10, i);
                            tickpos = projector.project(-factor_x * 10, factor_y * 10, i);
                        } else {
                            if (i % t_z == 0) {
                                projection = projector.project(-factor_x * 10, factor_y * 9.5f, i);
                            } else {
                                projection = projector.project(-factor_x * 10, factor_y * 9.8f, i);
                            }
                            tickpos = projector.project(-factor_x * 10, factor_y * 10, i);
                        }
                        g.drawLine(projection.x, projection.y, tickpos.x, tickpos.y);
                        if (isDisplayZ) {
                            tickpos = projector.project(-factor_x * 10, factor_y * 10.5f, i);
                            if (i % t_z == 0) {
                                if (x_left) {
                                    outFloat(g, tickpos.x, tickpos.y, (float) ((double) (i + 10) / 20 * (zmax - zmin) + zmin), Label.LEFT, CENTER);
                                } else {
                                    outFloat(g, tickpos.x, tickpos.y, (float) ((double) (i + 10) / 20 * (zmax - zmin) + zmin), Label.RIGHT, CENTER);
                                }
                            }
                        }
                        if (isDisplayGrids && isBoxed && (i % t_z == 0)) {
                            projection = projector.project(-factor_x * 10, -factor_y * 10, i);
                            tickpos = projector.project(factor_x * 10, -factor_y * 10, i);
                        } else {
                            if (i % t_z == 0) {
                                projection = projector.project(factor_x * 9.5f, -factor_y * 10, i);
                            } else {
                                projection = projector.project(factor_x * 9.8f, -factor_y * 10, i);
                            }
                            tickpos = projector.project(factor_x * 10, -factor_y * 10, i);
                        }
                        g.drawLine(projection.x, projection.y, tickpos.x, tickpos.y);
                        if (isDisplayZ) {
                            tickpos = projector.project(factor_x * 10.5f, -factor_y * 10, i);
                            if (i % t_z == 0) {
                                if (y_left) {
                                    outFloat(g, tickpos.x, tickpos.y, (float) ((double) (i + 10) / 20 * (zmax - zmin) + zmin), Label.LEFT, CENTER);
                                } else {
                                    outFloat(g, tickpos.x, tickpos.y, (float) ((double) (i + 10) / 20 * (zmax - zmin) + zmin), Label.RIGHT, CENTER);
                                }
                            }
                        }
                        if (isDisplayGrids && isBoxed) {
                            if (i % t_y == 0) {
                                projection = projector.project(-factor_x * 10, i, -10);
                                tickpos = projector.project(-factor_x * 10, i, 10);
                                g.drawLine(projection.x, projection.y, tickpos.x, tickpos.y);
                            }
                            if (i % t_x == 0) {
                                projection = projector.project(i, -factor_y * 10, -10);
                                tickpos = projector.project(i, -factor_y * 10, 10);
                                g.drawLine(projection.x, projection.y, tickpos.x, tickpos.y);
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Draws the bounding box of surface.
     */
    private void drawBoundingBox(Graphics2D g2) {
        Point startingpoint, projection;

        startingpoint = projector.project(factor_x * 10, factor_y * 10, 10);
        g2.setColor(colors.getLineBoxColor());
        projection = projector.project(-factor_x * 10, factor_y * 10, 10);
        g2.drawLine(startingpoint.x, startingpoint.y, projection.x, projection.y);
        projection = projector.project(factor_x * 10, -factor_y * 10, 10);
        g2.drawLine(startingpoint.x, startingpoint.y, projection.x, projection.y);
        projection = projector.project(factor_x * 10, factor_y * 10, -10);
        g2.drawLine(startingpoint.x, startingpoint.y, projection.x, projection.y);
    }
    
    /**
     * Determines whether a plane is plottable, i.e: does not have invalid
     * surfaceVertex.
     *
     * @return <code>true</code> if the plane is plottable, <code>false</code>
     * otherwise
     * @param values vertices array of the plane
     */
    private boolean plottable(SurfaceVertex[] values) {
        return (!values[0].isInvalid() && !values[1].isInvalid() && !values[2].isInvalid() && !values[3].isInvalid());
    }
    
    /**
     * Plots an area of group of planes
     *
     * @param start_lx start index in x direction
     * @param start_ly start index in y direction
     * @param end_lx end index in x direction
     * @param end_ly end index in y direction
     * @param sx step in x direction
     * @param sy step in y direction
     */
    private void plotArea(Graphics2D g2, int start_lx, int start_ly, int end_lx, int end_ly, int sx, int sy) {

        start_ly *= calc_divisions + 1;
        sy *= calc_divisions + 1;
        end_ly *= calc_divisions + 1;

        int lx = start_lx;
        int ly = start_ly;

        while (ly != end_ly) {
            if (plotfunc1) {
                values1[1] = surfaceVertex[0][lx + ly];
                values1[2] = surfaceVertex[0][lx + ly + sy];
            }
            if (plotfunc2) {
                values2[1] = surfaceVertex[1][lx + ly];
                values2[2] = surfaceVertex[1][lx + ly + sy];
            }

            while (lx != end_lx) {
                Thread.yield();
                if (plotfunc1) {
                    values1[0] = values1[1];
                    values1[1] = surfaceVertex[0][lx + sx + ly];
                    values1[3] = values1[2];
                    values1[2] = surfaceVertex[0][lx + sx + ly + sy];
                }
                if (plotfunc2) {
                    values2[0] = values2[1];
                    values2[1] = surfaceVertex[1][lx + sx + ly];
                    values2[3] = values2[2];
                    values2[2] = surfaceVertex[1][lx + sx + ly + sy];
                }
                if (!plotboth) {
                    if (plotfunc1) {
                        curve = 1;
                        if (plottable(values1)) {
                            plotPlane(g2, values1, 4);
                        }
                    } else {
                        curve = 2;
                        if (plottable(values2)) {
                            plotPlane(g2, values2, 4);
                        }
                    }
                } else if (plottable(values1)) {
                    if (plottable(values2)) {
                        splitPlotPlane(g2, values1, values2);
                    } else {
                        curve = 1;
                        plotPlane(g2, values1, 4);
                    }
                } else if (plottable(values2)) {
                    curve = 2;
                    plotPlane(g2, values2, 4);
                }
                lx += sx;
            }
            ly += sy;
            lx = start_lx;
        }
    }
    
    /**
     * Plots a single plane
     *
     * @param surfaceVertex vertices array of the plane
     * @param verticescount number of vertices to process
     */
    private void plotPlane(Graphics2D g2, SurfaceVertex[] vertex, int verticescount) {
        int count, loop, index;
        float z, result;
        boolean low1, low2;
        boolean valid1, valid2;
        if (verticescount < 3) {
            return;
        }
        count = 0;
        z = 0.0f;
        // line_color = colors.getLineColor();
        low1 = (vertex[0].z < zmin);
        valid1 = !low1 && (vertex[0].z <= zmax);
        index = 1;
        for (loop = 0; loop < verticescount; loop++) {
            low2 = (vertex[index].z < zmin);
            valid2 = !low2 && (vertex[index].z <= zmax);
            if ((valid1 || valid2) || (low1 ^ low2)) {
                if (!valid1) {
                    if (low1) {
                        result = zmin;
                    } else {
                        result = zmax;
                    }
                    float ratio = (result - vertex[index].z) / (vertex[loop].z - vertex[index].z);
                    float new_x = ratio * (vertex[loop].x - vertex[index].x) + vertex[index].x;
                    float new_y = ratio * (vertex[loop].y - vertex[index].y) + vertex[index].y;
                    if (low1) {
                        projection = projector.project(new_x, new_y, -10);
                    } else {
                        projection = projector.project(new_x, new_y, 10);
                    }
                    poly_x[count] = projection.x;
                    poly_y[count] = projection.y;
                    count++;
                    z += result;
                }
                if (valid2) {
                    projection = vertex[index].projection(projector);
                    poly_x[count] = projection.x;
                    poly_y[count] = projection.y;
                    count++;
                    z += vertex[index].z;
                } else {
                    if (low2) {
                        result = zmin;
                    } else {
                        result = zmax;
                    }
                    float ratio = (result - vertex[loop].z) / (vertex[index].z - vertex[loop].z);
                    float new_x = ratio * (vertex[index].x - vertex[loop].x) + vertex[loop].x;
                    float new_y = ratio * (vertex[index].y - vertex[loop].y) + vertex[loop].y;
                    if (low2) {
                        projection = projector.project(new_x, new_y, -10);
                    } else {
                        projection = projector.project(new_x, new_y, 10);
                    }
                    poly_x[count] = projection.x;
                    poly_y[count] = projection.y;
                    count++;
                    z += result;
                }
            }
            if (++index == verticescount) {
                index = 0;
            }
            valid1 = valid2;
            low1 = low2;
        }
        if (count > 0) {
            z = (z / count - zmin) * color_factor;
            g2.setColor(colors.getPolygonColor(curve, z));
            g2.fillPolygon(poly_x, poly_y, count);
            g2.setColor(colors.getLineColor(1, z));
            if (isMesh) {

                poly_x[count] = poly_x[0];
                poly_y[count] = poly_y[0];
                count++;
                g2.drawPolygon(poly_x, poly_y, count);
            }
        }
    }
    
    /**
     * Given two vertices array of plane, intersects and plots them. Splits one
     * of the planes if needed.
     *
     * @param values1 vertices array of first plane
     * @param values2 vertices array of second plane
     */
    private void splitPlotPlane(Graphics2D g2, SurfaceVertex[] values1, SurfaceVertex[] values2) {
        int trackposition = COINCIDE;
        int uppercount = 0, lowercount = 0;
        boolean coincide = true;
        boolean upper_first = false;
        float factor, xi, yi, zi;
        int i = 0, j = 0;

        for (int counter = 0; counter <= 4; counter++) {
            if (values1[i].z < values2[i].z) {
                coincide = false;
                if (trackposition == COINCIDE) {
                    trackposition = UPPER;
                    upperpart[uppercount++] = values2[i];
                } else if (trackposition != UPPER) {

                    // intersects
                    factor = (values1[i].z - values2[i].z) / (values1[i].z - values2[i].z + values2[j].z - values1[j].z);
                    if (values1[i].x == values1[j].x) {

                        // intersects in y direction
                        yi = factor * (values1[j].y - values1[i].y) + values1[i].y;
                        xi = values1[i].x;
                    } else {

                        // intersects in x direction
                        xi = factor * (values1[j].x - values1[i].x) + values1[i].x;
                        yi = values1[i].y;
                    }
                    zi = factor * (values2[j].z - values2[i].z) + values2[i].z;

                    upperpart[uppercount++] = lowerpart[lowercount++] = new SurfaceVertex(xi, yi, zi);
                    upperpart[uppercount++] = values2[i];

                    trackposition = UPPER;
                } else {
                    upperpart[uppercount++] = values2[i];
                }

            } else if (values1[i].z > values2[i].z) {
                coincide = false;
                if (trackposition == COINCIDE) {
                    trackposition = LOWER;
                    lowerpart[lowercount++] = values2[i];
                } else if (trackposition != LOWER) {

                    // intersects
                    factor = (values1[i].z - values2[i].z) / (values1[i].z - values2[i].z + values2[j].z - values1[j].z);
                    if (values1[i].x == values1[j].x) {

                        // intersects in y direction
                        yi = factor * (values1[j].y - values1[i].y) + values1[i].y;
                        xi = values1[i].x;
                    } else {

                        // intersects in x direction
                        xi = factor * (values1[j].x - values1[i].x) + values1[i].x;
                        yi = values1[i].y;
                    }
                    zi = factor * (values2[j].z - values2[i].z) + values2[i].z;

                    lowerpart[lowercount++] = upperpart[uppercount++] = new SurfaceVertex(xi, yi, zi);
                    lowerpart[lowercount++] = values2[i];

                    trackposition = LOWER;
                } else {
                    lowerpart[lowercount++] = values2[i];
                }
            } else {
                upperpart[uppercount++] = values2[i];
                lowerpart[lowercount++] = values2[i];
                trackposition = COINCIDE;
            }

            j = i;
            i = (i + 1) % 4;
        }

        if (coincide) { // the two planes completely coincide
            plotPlane(g2, values1, 4);
        } else {
            if (critical) {
                upper_first = false;
            } else /*
				 * Priority Determination:
				 * 
				 * Theory: if center of projection (c.o.p) is above plane
				 * 0-1-2-3, then the plane below plane 0-1-2-3 should be plotted
				 * first, otherwise the plane above plane 0-1-2-3 should be
				 * plotted first. Task: calculate the height of plane 0-1-2-3 at
				 * the projection of c.o.p on plane xy (point c) and compare it
				 * with the height of c.o.p
				 * 
				 * To complete the task, we first calculate the height of plane
				 * 0-1-2-3 at point P,Q. Fortunately, this is just a
				 * 2-dimensional problem. The plane height at point P can be
				 * calculate using line equation on plane 3-2-P. (plane 1-2-Q
				 * for point Q) The next job is to calculate the plane height at
				 * point R. Again this is an easy job. Because R is always in
				 * the middle of P and Q, the plane height at R can be
				 * calculated by using this formula:
				 * 
				 * zR = (zP + zQ) / 2
				 * 
				 * Note that point P, Q, R in 3-D space are ON the plane 0-1-2-3
				 * The final job is to calculate the plane height at point c.
				 * This the same with previous job:
				 * 
				 * zR = (z2 + zc) / 2
				 * 
				 * zc = 2 * zR - z2
				 * 
				 * 
				 * --plane xy--
				 * 
				 * | | -----------0------3-------------------- | | | | c :
				 * center of projection -----------1------2---------Q----------
				 * | | | | | R | | | | | P---------c | | | |
				 * 
				 * 
				 * thus, the Java instructions for priority test might look like
				 * this:
				 * 
				 * float zP,zQ,zR;
				 * 
				 * zP = (values1[2].z-values1[3].z)*(cop.x-values1[3].x)/
				 * (values1[2].x-values1[3].x)+values1[3].z; zQ =
				 * (values1[2].z-values1[1].z)*(cop.y-values1[1].y)/
				 * (values1[2].y-values1[1].y)+values1[1].z; zR = (zP+zQ)/2;
				 * 
				 * // upper_first = 2 * zR - z2 > cop.z; upper_first = zP + zQ -
				 * z2 > cop.z;
				 * 
				 * 
				 * But, using new variables zP, zQ, and zR is not a good idea.
				 * It is better to calculate zR in a single instruction to speed
				 * up the calculation, even only by a litte. We are in hurry !
				 * :)
             */ if (values1[1].x == values1[2].x) {
                upper_first = (values1[2].z - values1[3].z) * (cop.x - values1[3].x) / (values1[2].x - values1[3].x) + values1[3].z + (values1[2].z - values1[1].z) * (cop.y - values1[1].y) / (values1[2].y - values1[1].y) + values1[1].z - values1[2].z > cop.z;
            } else {
                upper_first = (values1[2].z - values1[1].z) * (cop.x - values1[1].x) / (values1[2].x - values1[1].x) + values1[1].z + (values1[2].z - values1[3].z) * (cop.y - values1[3].y) / (values1[2].y - values1[3].y) + values1[3].z - values1[2].z > cop.z;
            }

            // there is a problem in drawing two curves in the same draw only
            // for
            // dualshade, dual color mode !
            // other modes would have the same color for the secant segment
            if (lowercount < 3) {
                if (upper_first) {
                    curve = 2;// color = dualshadeColorSecondHue;
                    plotPlane(g2, upperpart, uppercount);
                    curve = 1;// color = dualshadeColorFirstHue;
                    plotPlane(g2, values1, 4);
                } else {
                    curve = 1;// color = dualshadeColorFirstHue;
                    plotPlane(g2, values1, 4);
                    curve = 2;// color = dualshadeColorSecondHue;
                    plotPlane(g2, upperpart, uppercount);
                }
            } else if (uppercount < 3) {
                if (upper_first) {
                    curve = 1;// color = dualshadeColorFirstHue;
                    plotPlane(g2, values1, 4);
                    curve = 2;// color = dualshadeColorSecondHue;
                    plotPlane(g2, lowerpart, lowercount);
                } else {
                    curve = 2;// color = dualshadeColorSecondHue;
                    plotPlane(g2, lowerpart, lowercount);
                    curve = 1;// color = dualshadeColorFirstHue;
                    plotPlane(g2, values1, 4);
                }
            } else if (upper_first) {
                curve = 2;// color =
                // dualshadeColorSecondHue;//dualshadeColorFirstHue;;
                plotPlane(g2, upperpart, uppercount);
                curve = 1;// color = dualshadeColorFirstHue;
                plotPlane(g2, values1, 4);
                curve = 2;// color =
                // dualshadeColorSecondHue;//dualshadeColorFirstHue;;
                plotPlane(g2, lowerpart, lowercount);
            } else {
                curve = 2;// color =
                // dualshadeColorSecondHue;//dualshadeColorFirstHue;;
                plotPlane(g2, lowerpart, lowercount);
                curve = 1;// color = dualshadeColorFirstHue;
                plotPlane(g2, values1, 4);
                curve = 2;// color =
                // dualshadeColorSecondHue;//dualshadeColorFirstHue;;
                plotPlane(g2, upperpart, uppercount);
            }
        }
    }

    /**
     * Get position area
     *
     * @param area Whole area
     * @return Position area
     */
    @Override
    public Rectangle2D getPositionArea(Rectangle2D area) {
        double x = area.getWidth() * this.getPosition().getX() + area.getX();
        double y = area.getHeight() * (1 - this.getPosition().getHeight() - this.getPosition().getY()) + area.getY();
        double w = area.getWidth() * this.getPosition().getWidth();
        double h = area.getHeight() * this.getPosition().getHeight();
        return new Rectangle2D.Double(x, y, w, h);
    }

    /**
     * Get tight inset area
     *
     * @param g Graphics2D
     * @param positionArea Position area
     * @return Tight inset area
     */
    @Override
    public Margin getTightInset(Graphics2D g, Rectangle2D positionArea) {
        int left = 2, bottom = 2, right = 2, top = 5;
        int space = 2;

        if (this.title != null) {
            top += this.title.getHeight(g) + 10;
        }

        if (!this.legends.isEmpty()) {
            ChartLegend legend = this.getLegend();
            Dimension dim = legend.getLegendDimension(g, new Dimension((int) positionArea.getWidth(),
                    (int) positionArea.getHeight()));
            switch (legend.getPosition()) {
                case UPPER_CENTER_OUTSIDE:
                    top += dim.height + 10;
                    break;
                case LOWER_CENTER_OUTSIDE:
                    bottom += dim.height + 10;
                    break;
                case LEFT_OUTSIDE:
                    left += dim.width + 10;
                    break;
                case RIGHT_OUTSIDE:
                    right += dim.width + 10;
                    break;
            }
        }

        //Get x axis space
        //bottom += this.getXAxisHeight(g, space);

        //Get y axis space
        //left += this.getYAxisWidth(g, space);

        //Set right space
//        if (this.getXAxis().isVisible()) {
//            if (this.getXAxis().isDrawTickLabel()) {
//                right += this.getXAxis().getMaxLabelLength(g) / 2;
//            }
//        }

        return new Margin(left, right, top, bottom);
    }
    
    // </editor-fold>
    // <editor-fold desc="Events">
    class JSurfaceMouseListener extends MouseAdapter implements MouseMotionListener, MouseWheelListener {

        int i = 0;

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            float new_value = 0.0f;
            float old_value = projector.get2DScaling();
            new_value = old_value * (1 - e.getScrollAmount() * e.getWheelRotation() / 10f);
            if (new_value > 60.0f) {
                new_value = 60.0f;
            }
            if (new_value < 2.0f) {
                new_value = 2.0f;
            }
            if (new_value != old_value) {
                projector.set2DScaling(new_value);
                repaint();
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();

            click_x = x;
            click_y = y;
        }

        /**
         * <code>mouseUp<code> event handler. Regenerates image if dragging operations
         * have been done with the delay regeneration flag set on.
         *
         * @param e
         *            the event
         * @param x the x coordinate of cursor
         * @param y the y coordinate of cursor
         */
        @Override
        public void mouseReleased(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();

            if (!is3D()) {
                return;
            }
            if (model.isExpectDelay() && dragged) {
                destroyImage();
                data_available = is_data_available;
                repaint();
                dragged = false;
            }

        }

        /**
         * <code>mouseDrag<code> event handler. Tracks dragging operations.
         * Checks the delay regeneration flag and does proper actions.
         *
         * @param e
         *            the event
         * @param x the x coordinate of cursor
         * @param y the y coordinate of cursor
         */
        @Override
        public void mouseMoved(MouseEvent e) {
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            // System.out.println("dragged"+x+","+y);

            float new_value = 0.0f;

            if (!is3D()) {
                return;
            }
            // if (!thread.isAlive() || !data_available) {
            if (e.isControlDown()) {
                projector.set2D_xTranslation(projector.get2D_xTranslation() + (x - click_x));
                projector.set2D_yTranslation(projector.get2D_yTranslation() + (y - click_y));
            } else if (e.isShiftDown()) {
                new_value = projector.get2DScaling() + (y - click_y) * 0.5f;
                if (new_value > 60.0f) {
                    new_value = 60.0f;
                }
                if (new_value < 2.0f) {
                    new_value = 2.0f;
                }
                projector.set2DScaling(new_value);
            } else {
                new_value = projector.getRotationAngle() + (x - click_x);
                while (new_value > 360) {
                    new_value -= 360;
                }
                while (new_value < 0) {
                    new_value += 360;
                }
                projector.setRotationAngle(new_value);
                new_value = projector.getElevationAngle() + (y - click_y);
                if (new_value > 90) {
                    new_value = 90;
                } else if (new_value < 0) {
                    new_value = 0;
                }
                projector.setElevationAngle(new_value);
            }
            if (!model.isExpectDelay()) {
                repaint();
            } else {
                if (!dragged) {
                    is_data_available = data_available;
                    dragged = true;
                }
                data_available = false;
                repaint();
            }

            click_x = x;
            click_y = y;
        }

    }
    
    class JSurfaceChangesListener implements PropertyChangeListener, javax.swing.event.ChangeListener {

        @Override
        public void stateChanged(javax.swing.event.ChangeEvent e) {
            destroyImage();
        }

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent pe) {
            init();
            destroyImage();
        }
    }
    // </editor-fold>
}
