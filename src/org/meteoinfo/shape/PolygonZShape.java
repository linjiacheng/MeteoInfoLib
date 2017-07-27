/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.shape;

import org.meteoinfo.global.MIMath;

/**
 *
 * @author Yaqiang Wang
 */
public class PolygonZShape extends PolygonShape{
    @Override
    public ShapeTypes getShapeType(){
        return ShapeTypes.PolygonZ;
    }
    
    /**
     * Get Z Array
     *
     * @return Z value array
     */
    public double[] getZArray() {
        double[] zArray = new double[this.getPoints().size()];
        for (int i = 0; i < this.getPoints().size(); i++) {
            zArray[i] = ((PointZ)this.getPoints().get(i)).Z;
        }

        return zArray;
    }
    
    /**
     * Get Z range - min, max
     *
     * @return Z min, max
     */
    public double[] getZRange() {
        return MIMath.arrayMinMax(getZArray());
    }
    
    /**
     * Get M Array
     *
     * @return M value array
     */
    public double[] getMArray() {
        double[] mArray = new double[this.getPoints().size()];
        for (int i = 0; i < this.getPoints().size(); i++) {
            mArray[i] = ((PointZ)this.getPoints().get(i)).M;
        }

        return mArray;
    }
    
    /**
     * Get M range - min, max
     *
     * @return M min, max
     */
    public double[] getMRange() {
        return MIMath.arrayMinMax(getMArray());
    }
}
