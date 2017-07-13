package org.meteoinfo.chart.plot3d.surface;

import org.meteoinfo.data.ArrayMath;
import org.meteoinfo.global.MIMath;
import ucar.ma2.Array;

public class ArraySurfaceModel extends AbstractSurfaceModel {

    SurfaceVertex[][] surfaceVertex;

    /**
     * Creates two surfaces using data from the array.
     *
     * @param xmin lower bound of x values
     * @param xmax upper bound of x values
     * @param ymin lower bound of y values
     * @param ymax upper bound of y values
     * @param size number of items in each dimensions (ie z1 = float[size][size]
     * )
     * @param z1 value matrix (null supported)
     * @param z2 secondary function value matrix (null supported)
     */
    public void setValues(float xmin, float xmax, float ymin, float ymax, int size, float[][] z1, float[][] z2) {
        setDataAvailable(false); // clean space
        setXMin(xmin);
        setXMax(xmax);
        setYMin(ymin);
        setYMax(ymax);
        setCalcDivisions(size - 1);

        final float stepx = (xMax - xMin) / calcDivisions;
        final float stepy = (yMax - yMin) / calcDivisions;
        final float xfactor = 20 / (xMax - xMin); // 20 aint magic: surface vertex requires a value in [-10 ; 10]
        final float yfactor = 20 / (yMax - yMin);

        final int total = (calcDivisions + 1) * (calcDivisions + 1); // compute total size
        surfaceVertex = new SurfaceVertex[2][total];

        for (int i = 0; i <= calcDivisions; i++) {
            for (int j = 0; j <= calcDivisions; j++) {
                int k = i * (calcDivisions + 1) + j;

                float xv = xMin + i * stepx;
                float yv = yMin + j * stepy;
                float v1 = z1 != null ? z1[i][j] : Float.NaN;
                if (Float.isInfinite(v1)) {
                    v1 = Float.NaN;
                }
                if (!Float.isNaN(v1)) {
                    if (Float.isNaN(z1Max) || (v1 > z1Max)) {
                        z1Max = v1;
                    } else if (Float.isNaN(z1Min) || (v1 < z1Min)) {
                        z1Min = v1;
                    }
                }

                surfaceVertex[0][k] = new SurfaceVertex((xv - xMin) * xfactor - 10, (yv - yMin) * yfactor - 10, v1);
                float v2 = z2 != null ? z2[i][j] : Float.NaN;
                if (Float.isInfinite(v2)) {
                    v2 = Float.NaN;
                }
                if (!Float.isNaN(v2)) {
                    if (Float.isNaN(z2Max) || (v2 > z2Max)) {
                        z2Max = v2;
                    } else if (Float.isNaN(z2Min) || (v2 < z2Min)) {
                        z2Min = v2;
                    }
                }

                surfaceVertex[1][k] = new SurfaceVertex((xv - xMin) * xfactor - 10, (yv - yMin) * yfactor - 10, v2);
            }
        }

        autoScale();
        setDataAvailable(true);
        fireStateChanged();
    }
    
    /**
     * Creates two surfaces using data from the array.
     *
     * @param xmin lower bound of x values
     * @param xmax upper bound of x values
     * @param ymin lower bound of y values
     * @param ymax upper bound of y values
     * @param z1 value matrix (null supported)
     * @param z2 secondary function value matrix (null supported)
     */
    public void setValues(float xmin, float xmax, float ymin, float ymax, float[][] z1, float[][] z2) {
        setDataAvailable(false); // clean space
        setXMin(xmin);
        setXMax(xmax);
        setYMin(ymin);
        setYMax(ymax);
        int yn = z1.length;
        int xn = z1[0].length;
        setCalcDivisions(xn - 1);
        setYCalcDivisions(yn - 1);

        final float stepx = (xMax - xMin) / calcDivisions;
        final float stepy = (yMax - yMin) / ycalcDivisions;
        final float xfactor = 20 / (xMax - xMin); // 20 aint magic: surface vertex requires a value in [-10 ; 10]
        final float yfactor = 20 / (yMax - yMin);

        final int total = (ycalcDivisions + 1) * (calcDivisions + 1); // compute total size
        surfaceVertex = new SurfaceVertex[2][total];

        for (int i = 0; i <= ycalcDivisions; i++) {
            for (int j = 0; j <= calcDivisions; j++) {
                int k = i * (calcDivisions + 1) + j;

                float xv = xMin + i * stepx;
                float yv = yMin + j * stepy;
                float v1 = z1 != null ? z1[i][j] : Float.NaN;
                if (Float.isInfinite(v1)) {
                    v1 = Float.NaN;
                }
                if (!Float.isNaN(v1)) {
                    if (Float.isNaN(z1Max) || (v1 > z1Max)) {
                        z1Max = v1;
                    } else if (Float.isNaN(z1Min) || (v1 < z1Min)) {
                        z1Min = v1;
                    }
                }

                surfaceVertex[0][k] = new SurfaceVertex((xv - xMin) * xfactor - 10, (yv - yMin) * yfactor - 10, v1);
                float v2 = z2 != null ? z2[i][j] : Float.NaN;
                if (Float.isInfinite(v2)) {
                    v2 = Float.NaN;
                }
                if (!Float.isNaN(v2)) {
                    if (Float.isNaN(z2Max) || (v2 > z2Max)) {
                        z2Max = v2;
                    } else if (Float.isNaN(z2Min) || (v2 < z2Min)) {
                        z2Min = v2;
                    }
                }

                surfaceVertex[1][k] = new SurfaceVertex((xv - xMin) * xfactor - 10, (yv - yMin) * yfactor - 10, v2);
            }
        }

        autoScale();
        setDataAvailable(true);
        fireStateChanged();
    }
    
    /**
     * Creates two surfaces using data from the array.
     *
     * @param xmin lower bound of x values
     * @param xmax upper bound of x values
     * @param ymin lower bound of y values
     * @param ymax upper bound of y values
     * @param z1 value matrix (null supported)
     * @param z2 secondary function value matrix (null supported)
     */
    public void setValues(float xmin, float xmax, float ymin, float ymax, Array z1, Array z2) {
        setDataAvailable(false); // clean space
        setXMin(xmin);
        setXMax(xmax);
        setYMin(ymin);
        setYMax(ymax);
        int yn = z1.getShape()[0];
        int xn = z1.getShape()[1];
        setCalcDivisions(xn - 1);
        setYCalcDivisions(yn - 1);

        final float stepx = (xMax - xMin) / calcDivisions;
        final float stepy = (yMax - yMin) / ycalcDivisions;
        final float xfactor = 20 / (xMax - xMin); // 20 aint magic: surface vertex requires a value in [-10 ; 10]
        final float yfactor = 20 / (yMax - yMin);

        final int total = (ycalcDivisions + 1) * (calcDivisions + 1); // compute total size
        surfaceVertex = new SurfaceVertex[2][total];

        for (int i = 0; i <= ycalcDivisions; i++) {
            for (int j = 0; j <= calcDivisions; j++) {
                int k = i * (calcDivisions + 1) + j;

                float xv = xMin + i * stepx;
                float yv = yMin + j * stepy;
                float v1 = z1 != null ? z1.getFloat(k) : Float.NaN;
                if (Float.isInfinite(v1)) {
                    v1 = Float.NaN;
                }
                if (!Float.isNaN(v1)) {
                    if (Float.isNaN(z1Max) || (v1 > z1Max)) {
                        z1Max = v1;
                    } else if (Float.isNaN(z1Min) || (v1 < z1Min)) {
                        z1Min = v1;
                    }
                }

                surfaceVertex[0][k] = new SurfaceVertex((xv - xMin) * xfactor - 10, (yv - yMin) * yfactor - 10, v1);
                float v2 = z2 != null ? z2.getFloat(k) : Float.NaN;
                if (Float.isInfinite(v2)) {
                    v2 = Float.NaN;
                }
                if (!Float.isNaN(v2)) {
                    if (Float.isNaN(z2Max) || (v2 > z2Max)) {
                        z2Max = v2;
                    } else if (Float.isNaN(z2Min) || (v2 < z2Min)) {
                        z2Min = v2;
                    }
                }

                surfaceVertex[1][k] = new SurfaceVertex((xv - xMin) * xfactor - 10, (yv - yMin) * yfactor - 10, v2);
            }
        }

        autoScale();
        setDataAvailable(true);
        fireStateChanged();
    }
    
    /**
     * Creates two surfaces using data from the array.
     *
     * @param x X array
     * @param y Y array
     * @param z1 value matrix (null supported)
     * @param z2 secondary function value matrix (null supported)
     */
    public void setValues(Array x, Array y, Array z1, Array z2) {
        setDataAvailable(false); // clean space
        double xmin = ArrayMath.getMinimum(x);
        double xmax = ArrayMath.getMaximum(x);
        double ymin = ArrayMath.getMinimum(y);
        double ymax = ArrayMath.getMaximum(y);
        double[] values = (double[])(MIMath.getIntervalValues(xmin, xmax, true).get(0));
        xmin = values[0];
        xmax = values[values.length - 1];
        values = (double[])(MIMath.getIntervalValues(ymin, ymax, true).get(0));
        ymin = values[0];
        ymax = values[values.length - 1];
        setXMin((float)xmin);
        setXMax((float)xmax);
        setYMin((float)ymin);
        setYMax((float)ymax);
        int yn = z1.getShape()[0];
        int xn = z1.getShape()[1];
        setCalcDivisions(xn - 1);
        setYCalcDivisions(yn - 1);

        final float xfactor = 20f / (xMax - xMin); // 20 aint magic: surface vertex requires a value in [-10 ; 10]
        final float yfactor = 20f / (yMax - yMin);

        final int total = (ycalcDivisions + 1) * (calcDivisions + 1); // compute total size
        surfaceVertex = new SurfaceVertex[2][total];

        for (int i = 0; i < yn; i++) {
            for (int j = 0; j < xn; j++) {
                int k = i * xn + j;

                float xv = x.getFloat(k);
                float yv = y.getFloat(k);
                float v1 = z1 != null ? z1.getFloat(k) : Float.NaN;
                if (Float.isInfinite(v1)) {
                    v1 = Float.NaN;
                }
                if (!Float.isNaN(v1)) {
                    if (Float.isNaN(z1Max) || (v1 > z1Max)) {
                        z1Max = v1;
                    } else if (Float.isNaN(z1Min) || (v1 < z1Min)) {
                        z1Min = v1;
                    }
                }

                surfaceVertex[0][k] = new SurfaceVertex((xv - xMin) * xfactor - 10, (yv - yMin) * yfactor - 10, v1);
                float v2 = z2 != null ? z2.getFloat(k) : Float.NaN;
                if (Float.isInfinite(v2)) {
                    v2 = Float.NaN;
                }
                if (!Float.isNaN(v2)) {
                    if (Float.isNaN(z2Max) || (v2 > z2Max)) {
                        z2Max = v2;
                    } else if (Float.isNaN(z2Min) || (v2 < z2Min)) {
                        z2Min = v2;
                    }
                }

                surfaceVertex[1][k] = new SurfaceVertex((xv - xMin) * xfactor - 10, (yv - yMin) * yfactor - 10, v2);
            }
        }
        values = (double[])(MIMath.getIntervalValues(z1Min, z1Max, true).get(0));
        z1Min = (float)values[0];
        z1Max = (float)values[values.length - 1];
        if (z2 != null){
            values =(double[])(MIMath.getIntervalValues(z2Min, z2Max, true).get(0));
            z2Min = (float)values[0];
            z2Max = (float)values[values.length - 1];
        }

        autoScale();
        setDataAvailable(true);
        fireStateChanged();
    }

    @Override
    public SurfaceVertex[][] getSurfaceVertex() {
        return surfaceVertex;
    }

}
