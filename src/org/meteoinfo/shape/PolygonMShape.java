/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.shape;

import org.meteoinfo.global.MIMath;

/**
 *
 * @author yaqiang
 */
public class PolygonMShape extends PolygonShape{
    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public PolygonMShape(){
        super();
        this.setShapeType(ShapeTypes.PolygonM);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get M Array
     *
     * @return M value array
     */
    public double[] getMArray() {
        double[] mArray = new double[this.getPoints().size()];
        for (int i = 0; i < this.getPoints().size(); i++) {
            mArray[i] = ((PointM)this.getPoints().get(i)).M;
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
    // </editor-fold>
    // <editor-fold desc="Methods">
    // </editor-fold>
}
