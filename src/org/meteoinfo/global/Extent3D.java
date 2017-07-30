/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.global;

/**
 *
 * @author Yaqiang Wang
 */
public class Extent3D extends Extent{
    public double minZ;
    public double maxZ;
    
    /**
     * Constructor
     */
    public Extent3D(){
        
    }
    
    /**
     * Constructor
     *
     * @param xMin Minimum X
     * @param xMax Maximum X
     * @param yMin Minimum Y
     * @param yMax Maximum Y
     * @param zMin Minimum Z
     * @param zMax Maximum Z
     */
    public Extent3D(double xMin, double xMax, double yMin, double yMax, double zMin, double zMax) {
        super(xMin, xMax, yMin, yMax);
        minZ = zMin;
        maxZ = zMax;
    }
    
    /**
     * Get is 3D or not
     * @return false
     */
    @Override
    public boolean is3D(){
        return true;
    }
    
    /**
     * Tests whether this extent intersects the second extent.
     * @param extent The second extent
     * @return Boolean
     */
    @Override
    public boolean intersects(Extent extent) {
        Extent3D bET = (Extent3D)extent;
        return !(maxX < bET.minX || maxY < bET.minY || maxZ < bET.maxZ ||
                bET.maxX < minX || bET.maxY < minY || bET.maxZ < minZ);
    }

}
