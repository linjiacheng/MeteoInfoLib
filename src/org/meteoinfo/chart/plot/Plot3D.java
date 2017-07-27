/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Label;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.chart.ChartLegend;
import org.meteoinfo.chart.ChartText;
import org.meteoinfo.chart.LegendPosition;
import org.meteoinfo.chart.Margin;
import org.meteoinfo.chart.plot3d.surface.Projector;
import org.meteoinfo.data.Dataset;
import org.meteoinfo.drawing.Draw;
import org.meteoinfo.global.Extent3D;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointF;
import org.meteoinfo.legend.PointBreak;
import org.meteoinfo.legend.PolygonBreak;
import org.meteoinfo.legend.PolylineBreak;
import org.meteoinfo.shape.Graphic;
import org.meteoinfo.shape.GraphicCollection;
import org.meteoinfo.shape.PointZ;
import org.meteoinfo.shape.PointZShape;
import org.meteoinfo.shape.PolygonZ;
import org.meteoinfo.shape.PolygonZShape;
import org.meteoinfo.shape.PolylineZShape;
import org.meteoinfo.shape.Shape;

/**
 *
 * @author Yaqiang Wang
 */
public class Plot3D extends Plot {

    // <editor-fold desc="Variables">
    private GraphicCollection graphics;
    private ChartText title;
    private List<ChartLegend> legends;

    private final Projector projector; // the projector, controls the point of view
    private int prevwidth, prevheight; // canvas size

    private boolean isBoxed, isMesh, isScaleBox, isDisplayXY, isDisplayZ,
            isDisplayGrids;
    private float xmin, xmax, ymin;
    private float ymax, zmin, zmax;

    private Color boxColor = Color.getHSBColor(0f, 0f, 0.95f);
    private Color lineboxColor = Color.getHSBColor(0f, 0f, 0.5f);

    private String xLabel = "X";
    private String yLabel = "Y";

    // constants
    private static final int TOP = 0;
    private static final int CENTER = 1;
    private int factor_x, factor_y; // conversion factors
    private int t_x, t_y, t_z; // determines ticks density
    private final int poly_x[] = new int[9];
    private final int poly_y[] = new int[9];
    private Point projection;
    float xfactor;
    float yfactor;
    float zfactor;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public Plot3D() {
        this.legends = new ArrayList<>();
        projector = new Projector();
        projector.setDistance(70);
        projector.set2DScaling(15);
        projector.setRotationAngle(225);
        projector.setElevationAngle(10);
        this.graphics = new GraphicCollection();
        this.graphics.set3D(true);
    }

    // </editor-fold>
    // <editor-fold desc="GetSet">
    /**
     * Get projector
     *
     * @return The Projector
     */
    public Projector getProjector() {
        return this.projector;
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

    /**
     * Set minimum x
     *
     * @param value Minimum x
     */
    public void setXMin(float value) {
        this.xmin = value;
    }

    /**
     * Set maximum x
     *
     * @param value Maximum x
     */
    public void setXMax(float value) {
        this.xmax = value;
    }

    /**
     * Set minimum y
     *
     * @param value Minimum y
     */
    public void setYMin(float value) {
        this.ymin = value;
    }

    /**
     * Set Maximum y
     *
     * @param value Maximum y
     */
    public void setYMax(float value) {
        this.ymax = value;
    }

    /**
     * Set minimum z
     *
     * @param value Minimum z
     */
    public void setZMin(float value) {
        this.zmin = value;
    }

    /**
     * Set maximum z
     *
     * @param value Maximum z
     */
    public void setZMax(float value) {
        this.zmax = value;
    }

    /**
     * Set display X/Y axis or not
     *
     * @param value Boolean
     */
    public void setDisplayXY(boolean value) {
        this.isDisplayXY = value;
    }

    /**
     * Set display Z axis or not
     *
     * @param value Boolean
     */
    public void setDisplayZ(boolean value) {
        this.isDisplayZ = value;
    }

    /**
     * Set display grids or not
     *
     * @param value Boolean
     */
    public void setDisplayGrids(boolean value) {
        this.isDisplayGrids = value;
    }

    /**
     * Set display box or not
     *
     * @param value Boolean
     */
    public void setBoxed(boolean value) {
        this.isBoxed = value;
    }

    /**
     * Set display mesh line or not
     *
     * @param value Boolean
     */
    public void setMesh(boolean value) {
        this.isMesh = value;
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

    /**
     * Add a graphic
     *
     * @param g Grahic
     */
    public void addGraphic(Graphic g) {
        this.graphics.add(g);
        Extent3D extent = (Extent3D) this.graphics.getExtent();
        double[] values = (double[]) (MIMath.getIntervalValues(extent.minX, extent.maxX, true).get(0));
        xmin = (float) values[0];
        xmax = (float) values[values.length - 1];
        values = (double[]) (MIMath.getIntervalValues(extent.minY, extent.maxY, true).get(0));
        ymin = (float) values[0];
        ymax = (float) values[values.length - 1];
        values = (double[]) (MIMath.getIntervalValues(extent.minZ, extent.maxZ, true).get(0));
        zmin = (float) values[0];
        zmax = (float) values[values.length - 1];
        this.projector.setZRange(zmin, zmax);
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
        this.setGraphArea(this.getPositionArea());

        //Draw title
        float y = this.drawTitle(g2, this.getGraphArea());

        //Set projection area
        Rectangle parea = this.getPositionArea(area).getBounds();
        if ((parea.width != prevwidth) || (parea.height != prevheight)) {
            prevwidth = parea.width;
            prevheight = parea.height;
        }
        projector.setProjectionArea(parea);

        //Draw box
        drawBoxGridsTicksLabels(g2, false);

        //Draw 3D graphics
        xfactor = 20f / (this.xmax - this.xmin); // 20 aint magic: surface vertex requires a value in [-10 ; 10]
        yfactor = 20f / (this.ymax - this.ymin);
        zfactor = 20f / (this.zmax - this.zmin);
        for (int m = 0; m < this.graphics.getNumGrahics(); m++) {
            Graphic graphic = this.graphics.get(m);
            Shape shape = graphic.getGraphicN(0).getShape();
            switch (shape.getShapeType()) {
                case Point:
                case PointZ:
                    this.drawPoints(g2, graphic);
                    break;
                case Polyline:
                case PolylineZ:
                    this.drawLineStrings(g2, graphic);
                    break;
                case Polygon:
                case PolygonZ:
                    this.drawPolygons(g2, graphic);
                    break;
            }
        }

        //Draw bounding box
        if (isBoxed) {
            drawBoundingBox(g2);
        }

        //Draw legend
        Rectangle2D rect = this.projector.getBounds();
        this.drawLegend(g2, area, rect, y);
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
            g.setFont(new Font("Arial", Font.PLAIN, 14));
        }
        return y;
    }

    private void drawPoints(Graphics2D g, Graphic graphic) {
        List<Double> dds = new ArrayList<>();
        List<Integer> order = new ArrayList<>();
        PointZ p;
        double d;
        boolean isIn;
        float angle = projector.getRotationAngle();
        boolean xdir = true;
        if (angle < 45 || angle > 135 && angle < 225 || angle > 315) {
            xdir = false;
        }
        if (xdir) {
            for (int i = 0; i < graphic.getNumGrahics(); i++) {
                Graphic gg = graphic.getGraphicN(i);
                PointZShape shape = (PointZShape) gg.getShape();
                p = (PointZ) shape.getPoint();
                d = p.X * projector.getSinRotationAngle();
                isIn = false;
                for (int j = 0; j < dds.size(); j++) {
                    if (d < dds.get(j)) {
                        dds.add(j, d);
                        order.add(j, i);
                        isIn = true;
                        break;
                    }
                }
                if (!isIn) {
                    dds.add(d);
                    order.add(i);
                }
            }
        } else {
            for (int i = 0; i < graphic.getNumGrahics(); i++) {
                Graphic gg = graphic.getGraphicN(i);
                PointZShape shape = (PointZShape) gg.getShape();
                p = (PointZ) shape.getPoint();
                d = p.Y * projector.getCosRotationAngle();
                isIn = false;
                for (int j = 0; j < dds.size(); j++) {
                    if (d < dds.get(j)) {
                        dds.add(j, d);
                        order.add(j, i);
                        isIn = true;
                        break;
                    }
                }
                if (!isIn) {
                    dds.add(d);
                    order.add(i);
                }
            }
        }

        for (int i : order) {
            Graphic gg = graphic.getGraphicN(i);
            PointZShape shape = (PointZShape) gg.getShape();
            PointBreak pb = (PointBreak) gg.getLegend();
            p = (PointZ) shape.getPoint();
            PointZ pp = new PointZ((p.X - xmin) * xfactor - 10, (p.Y - ymin) * yfactor - 10,
                    (p.Z - this.zmin) * zfactor - 10);
            projection = projector.project((float) pp.X, (float) pp.Y, (float) pp.Z);
            PointF pf = new PointF(projection.x, projection.y);
            Draw.drawPoint(pf, pb, g);
        }
    }

    private void drawLineStrings(Graphics2D g, Graphic graphic) {
        if (graphic.getNumGrahics() == 1) {
            Graphic gg = graphic.getGraphicN(0);
            PolylineZShape shape = (PolylineZShape)gg.getShape();
            PolylineBreak pb = (PolylineBreak)gg.getLegend();
            List<PointZ> ps = (List<PointZ>)shape.getPoints();
            PointF[] points = new PointF[ps.size()];
            PointZ p, pp;
            for (int i = 0; i < ps.size(); i++) {
                p = ps.get(i);
                pp = new PointZ((p.X - xmin) * xfactor - 10, (p.Y - ymin) * yfactor - 10,
                    (p.Z - this.zmin) * zfactor - 10);
                projection = projector.project((float) pp.X, (float) pp.Y, (float) pp.Z);
                points[i] = new PointF(projection.x, projection.y);
            }
            Draw.drawPolyline(points, pb, g);
        } else {
            List<Double> dds = new ArrayList<>();
            List<Integer> order = new ArrayList<>();
            PointZ p;
            double d;
            boolean isIn;
            float angle = projector.getRotationAngle();
            boolean xdir = true;
            if (angle < 45 || angle > 135 && angle < 225 || angle > 315) {
                xdir = false;
            }
            if (xdir) {
                for (int i = 0; i < graphic.getNumGrahics(); i++) {
                    Graphic gg = graphic.getGraphicN(i);
                    Shape shape = gg.getShape();
                    p = (PointZ) shape.getPoints().get(0);
                    d = p.X * projector.getSinRotationAngle();
                    isIn = false;
                    for (int j = 0; j < dds.size(); j++) {
                        if (d < dds.get(j)) {
                            dds.add(j, d);
                            order.add(j, i);
                            isIn = true;
                            break;
                        }
                    }
                    if (!isIn) {
                        dds.add(d);
                        order.add(i);
                    }
                }
            } else {
                for (int i = 0; i < graphic.getNumGrahics(); i++) {
                    Graphic gg = graphic.getGraphicN(i);
                    Shape shape = gg.getShape();
                    p = (PointZ) shape.getPoints().get(0);
                    d = p.Y * projector.getCosRotationAngle();
                    isIn = false;
                    for (int j = 0; j < dds.size(); j++) {
                        if (d < dds.get(j)) {
                            dds.add(j, d);
                            order.add(j, i);
                            isIn = true;
                            break;
                        }
                    }
                    if (!isIn) {
                        dds.add(d);
                        order.add(i);
                    }
                }
            }

            PointZ pp;
            for (int i : order) {
                Graphic gg = graphic.getGraphicN(i);
                PolylineZShape shape = (PolylineZShape)gg.getShape();
                PolylineBreak pb = (PolylineBreak)gg.getLegend();
                List<PointZ> ps = (List<PointZ>)shape.getPoints();
                PointF[] points = new PointF[ps.size()];
                for (int j = 0; j < ps.size(); j++) {
                    p = ps.get(j);
                    pp = new PointZ((p.X - xmin) * xfactor - 10, (p.Y - ymin) * yfactor - 10,
                        (p.Z - this.zmin) * zfactor - 10);
                    projection = projector.project((float) pp.X, (float) pp.Y, (float) pp.Z);
                    points[j] = new PointF(projection.x, projection.y);
                }
                Draw.drawPolyline(points, pb, g);
            }
        }
    }

    private void drawPolygons(Graphics2D g, Graphic graphic) {
        List<Double> dds = new ArrayList<>();
        List<Integer> order = new ArrayList<>();
        PointZ p;
        double d;
        boolean isIn;
        float angle = projector.getRotationAngle();
        boolean xdir = true;
        if (angle < 45 || angle > 135 && angle < 225 || angle > 315) {
            xdir = false;
        }
        if (xdir) {
            for (int i = 0; i < graphic.getNumGrahics(); i++) {
                Graphic gg = graphic.getGraphicN(i);
                Shape shape = gg.getShape();
                p = (PointZ) shape.getPoints().get(0);
                d = p.X * projector.getSinRotationAngle();
                isIn = false;
                for (int j = 0; j < dds.size(); j++) {
                    if (d < dds.get(j)) {
                        dds.add(j, d);
                        order.add(j, i);
                        isIn = true;
                        break;
                    }
                }
                if (!isIn) {
                    dds.add(d);
                    order.add(i);
                }
            }
        } else {
            for (int i = 0; i < graphic.getNumGrahics(); i++) {
                Graphic gg = graphic.getGraphicN(i);
                Shape shape = gg.getShape();
                p = (PointZ) shape.getPoints().get(0);
                d = p.Y * projector.getCosRotationAngle();
                isIn = false;
                for (int j = 0; j < dds.size(); j++) {
                    if (d < dds.get(j)) {
                        dds.add(j, d);
                        order.add(j, i);
                        isIn = true;
                        break;
                    }
                }
                if (!isIn) {
                    dds.add(d);
                    order.add(i);
                }
            }
        }

        for (int i : order) {
            Graphic gg = graphic.getGraphicN(i);
            Shape shape = gg.getShape();
            this.drawPolygonShape(g, (PolygonZShape) shape, (PolygonBreak) gg.getLegend());
        }
    }
    
    private void drawPolygonShape(Graphics2D g, PolygonZShape shape, PolygonBreak pb) {
        for (PolygonZ poly : (List<PolygonZ>)shape.getPolygons()) {
            drawPolygon(g, poly, pb);
        }
    }
    
    private List<PointF> drawPolygon(Graphics2D g, PolygonZ aPG, PolygonBreak aPGB) {
        int len = aPG.getOutLine().size();
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, len);
        PointZ p, pp;
        List<PointF> rPoints = new ArrayList<>();
        for (int i = 0; i < aPG.getOutLine().size(); i++) {
            p = ((List<PointZ>)aPG.getOutLine()).get(i);
            pp = new PointZ((p.X - xmin) * xfactor - 10, (p.Y - ymin) * yfactor - 10,
                    (p.Z - this.zmin) * zfactor - 10);
            projection = projector.project((float) pp.X, (float) pp.Y, (float) pp.Z);
            if (i == 0) {
                path.moveTo(projection.x, projection.y);
            } else {
                path.lineTo(projection.x, projection.y);
            }
            rPoints.add(new PointF(projection.x, projection.y));
        }

        List<PointZ> newPList;
        if (aPG.hasHole()) {
            for (int h = 0; h < aPG.getHoleLines().size(); h++) {
                newPList = (List<PointZ>)aPG.getHoleLines().get(h);
                for (int j = 0; j < newPList.size(); j++) {
                    p = newPList.get(j);
                    pp = new PointZ((p.X - xmin) * xfactor - 10, (p.Y - ymin) * yfactor - 10,
                        (p.Z - this.zmin) * zfactor - 10);
                    projection = projector.project((float) pp.X, (float) pp.Y, (float) pp.Z);
                    if (j == 0) {
                        path.moveTo(projection.x, projection.y);
                    } else {
                        path.lineTo(projection.x, projection.y);
                    }
                }
            }
        }
        path.closePath();

        if (aPGB.isDrawFill()) {
            Color aColor = aPGB.getColor();
            if (aPGB.isUsingHatchStyle()) {
                int size = aPGB.getStyleSize();
                BufferedImage bi = Draw.getHatchImage(aPGB.getStyle(), size, aPGB.getColor(), aPGB.getBackColor());
                Rectangle2D rect = new Rectangle2D.Double(0, 0, size, size);
                g.setPaint(new TexturePaint(bi, rect));
                g.fill(path);
            } else {
                g.setColor(aColor);
                g.fill(path);
            }
        }
        
        if (aPGB.isDrawOutline()) {
            BasicStroke pen = new BasicStroke(aPGB.getOutlineSize());
            g.setStroke(pen);
            g.setColor(aPGB.getOutlineColor());
            g.draw(path);
        }

        return rPoints;
    }

    private void drawPolygon(Graphics2D g, PolygonZShape shape, PolygonBreak pb) {
        int count, loop, index;
        float z, result;
        boolean low1, low2;
        boolean valid1, valid2;
        List<PointZ> points = (List<PointZ>) shape.getPoints();
        int verticescount = points.size();
        if (verticescount < 3) {
            return;
        }

        List<PointZ> vertex = new ArrayList<>();
        for (PointZ p : points) {
            vertex.add(new PointZ((p.X - xmin) * xfactor - 10, (p.Y - ymin) * yfactor - 10, p.Z));
        }

        count = 0;
        z = 0.0f;
        // line_color = colors.getLineColor();
        low1 = (vertex.get(0).Z < zmin);
        valid1 = !low1 && (vertex.get(0).Z <= zmax);
        index = 1;
        PointZ p;
        for (loop = 0; loop < verticescount; loop++) {
            low2 = (vertex.get(index).Z < zmin);
            valid2 = !low2 && (vertex.get(index).Z <= zmax);
            if ((valid1 || valid2) || (low1 ^ low2)) {
                if (!valid1) {
                    if (low1) {
                        result = zmin;
                    } else {
                        result = zmax;
                    }
                    double ratio = (result - vertex.get(index).Z) / (vertex.get(loop).Z - vertex.get(index).Z);
                    float new_x = (float) (ratio * (vertex.get(loop).X - vertex.get(index).X) + vertex.get(index).X);
                    float new_y = (float) (ratio * (vertex.get(loop).Y - vertex.get(index).Y) + vertex.get(index).Y);
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
                    p = vertex.get(index);
                    projection = projector.project((float) p.X, (float) p.Y, ((float) p.Z - this.zmin) * zfactor - 10);
                    //projection = vertex.get(index).projection(projector);
                    poly_x[count] = projection.x;
                    poly_y[count] = projection.y;
                    count++;
                    z += vertex.get(index).Z;
                } else {
                    if (low2) {
                        result = zmin;
                    } else {
                        result = zmax;
                    }
                    double ratio = (result - vertex.get(loop).Z) / (vertex.get(index).Z - vertex.get(loop).Z);
                    float new_x = (float) (ratio * (vertex.get(index).X - vertex.get(loop).X) + vertex.get(loop).X);
                    float new_y = (float) (ratio * (vertex.get(index).Y - vertex.get(loop).Y) + vertex.get(loop).Y);
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
            //z = (z / count - zmin) * color_factor;
            g.setColor(pb.getColor());
            //g2.setColor(colors.getPolygonColor(curve, z));
            g.fillPolygon(poly_x, poly_y, count);
            g.setColor(pb.getOutlineColor());
            //g2.setColor(colors.getLineColor(1, z));
            if (isMesh) {

                poly_x[count] = poly_x[0];
                poly_y[count] = poly_y[0];
                count++;
                g.drawPolygon(poly_x, poly_y, count);
            }
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

        g.setColor(this.boxColor);
        g.fillPolygon(x, y, 4);

        g.setColor(this.lineboxColor);
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

                g.setColor(this.boxColor);
                g.fillPolygon(x, y, 4);

                g.setColor(this.lineboxColor);
                g.drawPolygon(x, y, 5);

                projection = projector.project(-factor_x * 10, factor_y * 10, 10);
                x[2] = projection.x;
                y[2] = projection.y;
                projection = projector.project(-factor_x * 10, factor_y * 10, -10);
                x[3] = projection.x;
                y[3] = projection.y;
                x[4] = x[0];
                y[4] = y[0];

                g.setColor(this.boxColor);
                g.fillPolygon(x, y, 4);

                g.setColor(this.lineboxColor);
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
        g2.setColor(this.lineboxColor);
        projection = projector.project(-factor_x * 10, factor_y * 10, 10);
        g2.drawLine(startingpoint.x, startingpoint.y, projection.x, projection.y);
        projection = projector.project(factor_x * 10, -factor_y * 10, 10);
        g2.drawLine(startingpoint.x, startingpoint.y, projection.x, projection.y);
        projection = projector.project(factor_x * 10, factor_y * 10, -10);
        g2.drawLine(startingpoint.x, startingpoint.y, projection.x, projection.y);
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

    void drawLegend(Graphics2D g, Rectangle2D area, Rectangle2D graphArea, float y) {
        if (!this.legends.isEmpty()) {
            Object rendering = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (ChartLegend legend : this.legends) {
                if (legend.isColorbar()) {
                    if (legend.getPlotOrientation() == PlotOrientation.VERTICAL) {
                        legend.setHeight((int) (graphArea.getHeight() * legend.getShrink()));
                    } else {
                        legend.setWidth((int) (graphArea.getWidth() * legend.getShrink()));
                    }
                }
                if (legend.getPosition() == LegendPosition.CUSTOM) {
                    legend.getLegendDimension(g, new Dimension((int) area.getWidth(), (int) area.getHeight()));
                    float x = (float) (area.getWidth() * legend.getX());
                    y = (float) (area.getHeight() * (1 - (this.getLegend().getHeight() / area.getHeight())
                            - this.getLegend().getY()));
                    legend.draw(g, new PointF(x, y));
                } else {
                    this.drawLegendScheme(legend, g, graphArea, y);
                }
            }
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, rendering);
        }
    }

    void drawLegendScheme(ChartLegend legend, Graphics2D g, Rectangle2D area, float y) {
        g.setStroke(new BasicStroke(1));
        g.setFont(legend.getTickFont());
        Dimension dim = legend.getLegendDimension(g, new Dimension((int) area.getWidth(), (int) area.getHeight()));
        float x = 0;
        //Rectangle2D graphArea = this.getPositionArea();
        switch (legend.getPosition()) {
            case UPPER_CENTER_OUTSIDE:
                x = (float) (area.getX() + area.getWidth() / 2 - dim.width / 2);
                y += 5;
                break;
            case LOWER_CENTER_OUTSIDE:
                x = (float) (area.getX() + area.getWidth() / 2 - dim.width / 2);
                y = (float) (area.getY() + area.getHeight() + 10);
                break;
            case LEFT_OUTSIDE:
                x = 10;
                y = (float) area.getHeight() / 2 - dim.height / 2;
                break;
            case RIGHT_OUTSIDE:
                x = (float) area.getX() + (float) area.getWidth() + 10 + 40;
                y = (float) area.getY() + (float) area.getHeight() / 2 - dim.height / 2;
                break;
            case UPPER_CENTER:
                x = (float) (area.getX() + area.getWidth() / 2 - dim.width / 2);
                y = (float) area.getY() + 10;
                break;
            case UPPER_RIGHT:
                x = (float) (area.getX() + area.getWidth()) - dim.width - 10;
                y = (float) area.getY() + 10;
                break;
            case LOWER_CENTER:
                x = (float) (area.getX() + area.getWidth() / 2 - dim.width / 2);
                y = (float) (area.getY() + area.getHeight()) - dim.height - 10;
                break;
            case LOWER_RIGHT:
                x = (float) (area.getX() + area.getWidth()) - dim.width - 10;
                y = (float) (area.getY() + area.getHeight()) - dim.height - 10;
                break;
            case UPPER_LEFT:
                x = (float) area.getX() + 10;
                y = (float) area.getY() + 10;
                break;
            case LOWER_LEFT:
                x = (float) area.getX() + 10;
                y = (float) (area.getY() + area.getHeight()) - dim.height - 10;
                break;
        }
        legend.draw(g, new PointF(x, y));
    }

    // </editor-fold>
}
