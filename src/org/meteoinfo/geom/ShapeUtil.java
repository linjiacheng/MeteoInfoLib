/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.geom;

import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.global.PointD;

/**
 *
 * @author wyq
 */
public class ShapeUtil {
    /**
     * Create polygon shape
     * @param x_p X coordinate list
     * @param y_p Y coordinate list
     * @return Polygon shape
     */
    public static PolygonShape createPolygonShape(List<Number> x_p, List<Number> y_p){
        PolygonShape ps = new PolygonShape();
        List<PointD> points = new ArrayList<>();
        for (int i = 0; i < x_p.size(); i++) {
            points.add(new PointD(x_p.get(i).doubleValue(), y_p.get(i).doubleValue()));
        }
        if (!points.get(points.size() - 1).equals(points.get(0)))
            points.add((PointD)points.get(0).clone());
        ps.setPoints(points);
        
        return ps;
    }
    
    /**
     * Create polygon shape
     * @param xy X/Y coordinates
     * @return Polygon shape
     */
    public static PolygonShape createPolygonShape(List<List<Number>> xy){
        PolygonShape ps = new PolygonShape();
        List<PointD> points = new ArrayList<>();
        for (List<Number> xy1 : xy) {
            points.add(new PointD(xy1.get(0).doubleValue(), xy1.get(1).doubleValue()));
        }
        if (!points.get(points.size() - 1).equals(points.get(0)))
            points.add((PointD)points.get(0).clone());
        ps.setPoints(points);
        
        return ps;
    }
}
