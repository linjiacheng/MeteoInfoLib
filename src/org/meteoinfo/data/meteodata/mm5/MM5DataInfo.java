/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.meteodata.mm5;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meteoinfo.data.GridData;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.data.meteodata.Dimension;
import org.meteoinfo.data.meteodata.DimensionType;
import org.meteoinfo.data.meteodata.IGridDataInfo;
import org.meteoinfo.data.meteodata.Variable;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.projection.KnownCoordinateSystems;
import org.meteoinfo.projection.ProjectionInfo;
import org.meteoinfo.projection.Reproject;

/**
 *
 * @author yaqiang
 */
public class MM5DataInfo extends DataInfo implements IGridDataInfo {

    // <editor-fold desc="Variables">
    private ByteOrder _byteOrder = ByteOrder.BIG_ENDIAN;
    private BigHeader _bigHeader = new BigHeader();
    List<SubHeader> _subHeaders = new ArrayList<SubHeader>();
    // </editor-fold>
    // <editor-fold desc="Constructor">
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">

    @Override
    public void readDataInfo(String fileName) {
        BigHeader bh = null;
        this.readDataInfo(fileName, bh);
    }

    /**
     * Read data info - the the data file has no big header
     *
     * @param fileName The data file name
     * @param bigHeaderFile The data file with BigHeader
     */
    public void readDataInfo(String fileName, String bigHeaderFile) {
        this.setFileName(fileName);
        try {
            RandomAccessFile br = new RandomAccessFile(bigHeaderFile, "r");
            //Read flag
            br.skipBytes(4);
            int flag = br.readInt();
            br.skipBytes(4);

            BigHeader bh = null;
            if (flag == 0) {    //Read big header
                bh = this.readBigHeader(br);
            }
            br.close();

            this.readDataInfo(fileName, bh);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MM5DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MM5DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Read data info - the the data file has no big header
     *
     * @param fileName The data file name
     * @param ebh Extra BigHeader
     */
    public void readDataInfo(String fileName, BigHeader ebh) {
        this.setFileName(fileName);
        try {
            RandomAccessFile br = new RandomAccessFile(fileName, "r");
            int flag;
            SubHeader sh;
            int xn = 0, yn = 0, zn = 0;
            //byte[] bytes;
            List<Variable> variables = new ArrayList<Variable>();
            int tn = 0;
            Dimension xdim = new Dimension(DimensionType.X);
            Dimension ydim = new Dimension(DimensionType.Y);
            Dimension zdim = new Dimension(DimensionType.Z);

            if (ebh != null) {
                this._bigHeader = ebh;
                xn = ebh.getXNum();
                yn = ebh.getYNum();
                zn = ebh.getZNum();
                float[] values = new float[zn];
                for (int i = 0; i < zn; i++) {
                    values[i] = i + 1;
                }
                zdim.setValues(values);
                String projStr = this.getProjectionInfo().toProj4String();
                int mapProj = ebh.getMapProj();
                switch (mapProj) {
                    case 1:
                        projStr = "+proj=lcc"
                                + " +lat_1=" + String.valueOf(ebh.getTrueLatSouth())
                                + " +lat_2=" + String.valueOf(ebh.getTrueLatNorth())
                                + " +lat_0=" + String.valueOf(ebh.getXLATC())
                                + " +lon_0=" + String.valueOf(ebh.getXLONC());
                        break;
                    case 2:
                        projStr = "+proj=stere"
                                + "+lat_0=" + String.valueOf(ebh.getXLATC())
                                + "+lon_0=" + String.valueOf(ebh.getXLONC());
                        break;
                    case 3:
                        projStr = "+proj=tmerc"
                                + "+lat_0=" + String.valueOf(ebh.getXLATC())
                                + "+lon_0=" + String.valueOf(ebh.getXLONC());
                        break;
                }
                this.setProjectionInfo(new ProjectionInfo(projStr));
                //Set X Y
                double[] X = new double[xn];
                double[] Y = new double[yn];
                float centeri = xn / 2.0f;
                float centerj = yn / 2.0f;
                getProjectedXY(this.getProjectionInfo(), ebh.getDeltaX(), centeri, centerj, ebh.getXLONC(),
                        ebh.getXLATC(), X, Y);
                xdim.setValues(X);
                ydim.setValues(Y);
                this.setXDimension(xdim);
                this.setYDimension(ydim);
            }

            List<Date> times = new ArrayList<Date>();
            Date ct;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            int shIdx = 0;
            while (true) {
                if (br.getFilePointer() >= br.length() - 100) {
                    break;
                }

                //Read flag
                br.skipBytes(4);
                flag = br.readInt();
                br.skipBytes(4);

                if (flag == 0) {    //Read big header
                    BigHeader bh = this.readBigHeader(br);
                    if (ebh == null) {
                        this._bigHeader = bh;
                        xn = bh.getXNum();
                        yn = bh.getYNum();
                        zn = bh.getZNum();
                        float[] values = new float[zn];
                        for (int i = 0; i < zn; i++) {
                            values[i] = i + 1;
                        }
                        zdim.setValues(values);
                        String projStr = this.getProjectionInfo().toProj4String();
                        int mapProj = bh.getMapProj();
                        switch (mapProj) {
                            case 1:
                                projStr = "+proj=lcc"
                                        + " +lat_1=" + String.valueOf(bh.getTrueLatSouth())
                                        + " +lat_2=" + String.valueOf(bh.getTrueLatNorth())
                                        + " +lat_0=" + String.valueOf(bh.getXLATC())
                                        + " +lon_0=" + String.valueOf(bh.getXLONC());
                                break;
                            case 2:
                                projStr = "+proj=stere"
                                        + "+lat_0=" + String.valueOf(bh.getXLATC())
                                        + "+lon_0=" + String.valueOf(bh.getXLONC());
                                break;
                            case 3:
                                projStr = "+proj=tmerc"
                                        + "+lat_0=" + String.valueOf(bh.getXLATC())
                                        + "+lon_0=" + String.valueOf(bh.getXLONC());
                                break;
                        }
                        this.setProjectionInfo(new ProjectionInfo(projStr));
                        //Set X Y
                        double[] X = new double[xn];
                        double[] Y = new double[yn];
                        float centeri = xn / 2.0f;
                        float centerj = yn / 2.0f;
                        getProjectedXY(this.getProjectionInfo(), bh.getDeltaX(), centeri, centerj, bh.getXLONC(),
                                bh.getXLATC(), X, Y);
                        xdim.setValues(X);
                        ydim.setValues(Y);
                        this.setXDimension(xdim);
                        this.setYDimension(ydim);
                    }
                } else if (flag == 1) {    //Read sub header
                    long pos = br.getFilePointer();
                    sh = this.readSubHeader(br);
                    sh.timeIndex = tn;
                    sh.position = pos;
                    sh.length = (int) (br.getFilePointer() - pos);
                    this._subHeaders.add(sh);
                    if (sh.ordering.equals("YXS") || sh.ordering.equals("YXP")) {
                        br.skipBytes(xn * yn * zn * 4 + 8);
                    } else if (sh.ordering.equals("YXW")) {
                        br.skipBytes(xn * yn * (zn + 1) * 4 + 8);
                    } else if (sh.ordering.equals("YX")) {
                        br.skipBytes(xn * yn * 4 + 8);
                    } else if (sh.ordering.equals("CA")) {
                        br.skipBytes(sh.end_index[0] * sh.end_index[1] * 4 + 8);
                    } else if (sh.ordering.equals("S")) {
                        br.skipBytes(zn * 4 + 8);
                    } else if (sh.ordering.equals("P")) {
                        br.skipBytes(zn * 4 + 8);
                    }

                    boolean isNewVar = true;
                    for (Variable var : variables) {
                        if (var.getName().equals(sh.name)) {
                            isNewVar = false;
                            break;
                        }
                    }
                    if (isNewVar) {
                        Variable var = new Variable();
                        var.setName(sh.name);
                        //var.addLevel(dh.level);
                        var.setUnits(sh.unit);
                        var.setDescription(sh.description);

                        if (sh.ordering.equals("YXS") || sh.ordering.equals("YXP")
                                || sh.ordering.equals("YXW") || sh.ordering.equals("YX")) {
                            var.setXDimension(xdim);
                            var.setYDimension(ydim);
                        }
                        if (sh.ordering.equals("YXS") || sh.ordering.equals("YXP")
                                || sh.ordering.equals("YXW") || sh.ordering.equals("S")
                                || sh.ordering.equals("P")) {
                            var.setZDimension(zdim);
                        }
                        variables.add(var);
                    }
                    if (shIdx == 0) {
                        ct = format.parse(sh.current_date);
                        times.add(ct);
                    }
                    shIdx += 1;
                } else if (flag == 2) {
                    tn += 1;
                    shIdx = 0;
                }
            }

            List<Double> values = new ArrayList<Double>();
            for (Date t : times) {
                values.add(DataConvert.toOADate(t));
            }
            Dimension tDim = new Dimension(DimensionType.T);
            tDim.setValues(values);
            this.setTimeDimension(tDim);

            for (Variable var : variables) {
                var.setTDimension(tDim);
                //var.updateZDimension();
            }

            this.setVariables(variables);

            br.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MM5DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MM5DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(MM5DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private BigHeader readBigHeader(RandomAccessFile br) throws IOException {
        BigHeader bh = new BigHeader();
        br.skipBytes(4);
        byte[] bytes = new byte[80];
        int i, j;
        for (i = 0; i < 20; i++) {
            for (j = 0; j < 50; j++) {
                bh.bhi[j][i] = br.readInt();
            }
        }
        for (i = 0; i < 20; i++) {
            for (j = 0; j < 20; j++) {
                bh.bhr[j][i] = br.readFloat();
            }
        }
        for (i = 0; i < 20; i++) {
            for (j = 0; j < 50; j++) {
                br.read(bytes);
                bh.bhic[j][i] = new String(bytes).trim();
            }
        }
        for (i = 0; i < 20; i++) {
            for (j = 0; j < 20; j++) {
                br.read(bytes);
                bh.bhrc[j][i] = new String(bytes).trim();
            }
        }

        br.skipBytes(4);

        return bh;
    }

    private SubHeader readSubHeader(RandomAccessFile br) throws IOException {
        SubHeader sh = new SubHeader();
        byte[] bytes = new byte[4];
        int i;
        br.skipBytes(4);
        sh.ndim = br.readInt();
        for (i = 0; i < 4; i++) {
            sh.start_index[i] = br.readInt();
        }
        for (i = 0; i < 4; i++) {
            sh.end_index[i] = br.readInt();
        }
        sh.xtime = br.readFloat();
        br.read(bytes);
        sh.staggering = new String(bytes).trim();
        br.read(bytes);
        sh.ordering = new String(bytes).trim();
        bytes = new byte[24];
        br.read(bytes);
        sh.current_date = new String(bytes).trim();
        bytes = new byte[9];
        br.read(bytes);
        sh.name = new String(bytes).trim();
        bytes = new byte[25];
        br.read(bytes);
        sh.unit = new String(bytes).trim();
        bytes = new byte[46];
        br.read(bytes);
        sh.description = new String(bytes).trim();

        br.skipBytes(4);

        return sh;
    }

    private void getProjectedXY(ProjectionInfo projInfo, float size,
            float sync_XP, float sync_YP, float sync_Lon, float sync_Lat,
            double[] X, double[] Y) {
        //Get sync X/Y
        ProjectionInfo fromProj = KnownCoordinateSystems.geographic.world.WGS1984;
        double sync_X, sync_Y;
        double[][] points = new double[1][];
        points[0] = new double[]{sync_Lon, sync_Lat};
        Reproject.reprojectPoints(points, fromProj, projInfo, 0, 1);
        sync_X = points[0][0];
        sync_Y = points[0][1];

        //Get integer sync X/Y            
        int i_XP, i_YP;
        double i_X, i_Y;
        i_XP = (int) sync_XP;
        if (sync_XP == i_XP) {
            i_X = sync_X;
        } else {
            i_X = sync_X - (sync_XP - i_XP) * size;
        }
        i_YP = (int) sync_YP;
        if (sync_YP == i_YP) {
            i_Y = sync_Y;
        } else {
            i_Y = sync_Y - (sync_YP - i_YP) * size;
        }

        //Get left bottom X/Y
        int nx, ny;
        nx = X.length;
        ny = Y.length;
        double xlb, ylb;
        xlb = i_X - (i_XP - 1) * size;
        ylb = i_Y - (i_YP - 1) * size;

        //Get X Y with orient 0
        int i;
        for (i = 0; i < nx; i++) {
            X[i] = xlb + i * size;
        }
        for (i = 0; i < ny; i++) {
            Y[i] = ylb + i * size;
        }
    }

    private SubHeader findSubHeader(String varName, int tIdx) {
        for (SubHeader sh : this._subHeaders) {
            if (sh.timeIndex == tIdx && sh.name.equals(varName)) {
                return sh;
            }
        }

        return this._subHeaders.get(0);
    }

    @Override
    public String generateInfoText() {
        String dataInfo;
        dataInfo = "File Name: " + this.getFileName();
        int i, j;
        for (i = 0; i < 50; i++) {
            for (j = 0; j < 20; j++) {
                dataInfo += System.getProperty("line.separator") + String.format("[%d][%d]", i + 1, j + 1) + " "
                        + this._bigHeader.bhic[i][j] + ": " + String.valueOf(this._bigHeader.bhi[i][j]);
            }
        }
        for (i = 0; i < 20; i++) {
            for (j = 0; j < 20; j++) {
                dataInfo += System.getProperty("line.separator") + String.format("[%d][%d]", i + 1, j + 1) + " "
                        + this._bigHeader.bhrc[i][j] + ": " + String.valueOf(this._bigHeader.bhr[i][j]);
            }
        }
//        dataInfo += System.getProperty("line.separator") + "Xsize = " + String.valueOf(this.getXDimension().getDimLength())
//                + "  Ysize = " + String.valueOf(this.getYDimension().getDimLength());               
//        dataInfo += System.getProperty("line.separator") + "Number of Variables = " + String.valueOf(this.getVariableNum());
//        for (String v : this.getVariableNames()) {
//            dataInfo += System.getProperty("line.separator") + v;
//        }

        return dataInfo;
    }

    @Override
    public GridData getGridData_LonLat(int timeIdx, int varIdx, int levelIdx) {
        try {
            RandomAccessFile br = new RandomAccessFile(this.getFileName(), "r");
            Variable var = this.getVariables().get(varIdx);
            Dimension xdim = var.getXDimension();
            Dimension ydim = var.getYDimension();
            int xn = xdim.getDimLength();
            int yn = ydim.getDimLength();
            SubHeader sh = this.findSubHeader(var.getName(), timeIdx);
            br.seek(sh.position + sh.length);
            int n = xn * yn;
            br.skipBytes(4);
            br.skipBytes(n * 4 * levelIdx);
            byte[] dataBytes = new byte[n * 4];
            br.read(dataBytes);
            br.close();

            int i, j;
            double[][] theData = new double[yn][xn];
            int start = 0;
            byte[] bytes = new byte[4];
            for (i = 0; i < xn; i++) {
                for (j = 0; j < yn; j++) {
                    System.arraycopy(dataBytes, start, bytes, 0, 4);
                    theData[j][i] = DataConvert.bytes2Float(bytes, _byteOrder);
                    start += 4;
                }
            }

            GridData gridData = new GridData();
            gridData.data = theData;
            gridData.missingValue = this.getMissingValue();
            gridData.xArray = xdim.getValues();
            gridData.yArray = ydim.getValues();

            return gridData;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MM5IMDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (IOException ex) {
            Logger.getLogger(MM5IMDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public GridData getGridData_TimeLat(int lonIdx, int varIdx, int levelIdx) {
        try {
            Variable var = this.getVariables().get(varIdx);
            Dimension xdim = var.getXDimension();
            Dimension ydim = var.getYDimension();
            int xNum = xdim.getDimLength();
            int yNum = ydim.getDimLength();
            int tNum = this.getTimeNum();
            double[][] theData = new double[tNum][yNum];
            RandomAccessFile br = new RandomAccessFile(this.getFileName(), "r");
            byte[] dataBytes;
            SubHeader sh;
            int i, j;

            for (int t = 0; t < tNum; t++) {
                sh = this.findSubHeader(var.getName(), t);
                br.seek(sh.position + sh.length);
                int n = xNum * yNum;
                br.skipBytes(4);
                br.skipBytes(n * 4 * levelIdx);

                //Read Data
                dataBytes = new byte[n * 4];
                br.read(dataBytes);
                int start = lonIdx * yNum * 4;
                byte[] bytes = new byte[4];
                for (j = 0; j < yNum; j++) {
                    System.arraycopy(dataBytes, start, bytes, 0, 4);
                    theData[t][j] = DataConvert.bytes2Float(bytes, _byteOrder);
                    start += 4;
                }
            }

            br.close();

            GridData gridData = new GridData();
            gridData.data = theData;
            gridData.missingValue = this.getMissingValue();
            gridData.xArray = ydim.getValues();
            gridData.yArray = new double[tNum];
            for (i = 0; i < tNum; i++) {
                gridData.yArray[i] = DataConvert.toOADate(this.getTimes().get(i));
            }

            return gridData;
        } catch (IOException ex) {
            Logger.getLogger(MM5DataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public GridData getGridData_TimeLon(int latIdx, int varIdx, int levelIdx) {
        try {
            Variable var = this.getVariables().get(varIdx);
            Dimension xdim = var.getXDimension();
            Dimension ydim = var.getYDimension();
            int xNum = xdim.getDimLength();
            int yNum = ydim.getDimLength();
            int tNum = this.getTimeNum();
            double[][] theData = new double[tNum][xNum];
            RandomAccessFile br = new RandomAccessFile(this.getFileName(), "r");
            byte[] dataBytes;
            SubHeader sh;
            int i, j;

            for (int t = 0; t < tNum; t++) {
                sh = this.findSubHeader(var.getName(), t);
                br.seek(sh.position + sh.length);
                int n = xNum * yNum;
                br.skipBytes(4);
                br.skipBytes(n * 4 * levelIdx);

                //Read Data
                dataBytes = new byte[n * 4];
                br.read(dataBytes);
                int start = 0;
                byte[] bytes = new byte[4];
                for (i = 0; i < xNum; i++) {
                    for (j = 0; j < yNum; j++) {
                        if (j == latIdx) {
                            System.arraycopy(dataBytes, start, bytes, 0, 4);
                            theData[t][i] = DataConvert.bytes2Float(bytes, _byteOrder);
                        }
                        start += 4;
                    }
                }
            }

            br.close();

            GridData gridData = new GridData();
            gridData.data = theData;
            gridData.missingValue = this.getMissingValue();
            gridData.xArray = xdim.getValues();
            gridData.yArray = new double[tNum];
            for (i = 0; i < tNum; i++) {
                gridData.yArray[i] = DataConvert.toOADate(this.getTimes().get(i));
            }

            return gridData;
        } catch (IOException ex) {
            Logger.getLogger(MM5DataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public GridData getGridData_LevelLat(int lonIdx, int varIdx, int timeIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_LevelLon(int latIdx, int varIdx, int timeIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_LevelTime(int latIdx, int varIdx, int lonIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_Time(int lonIdx, int latIdx, int varIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_Level(int lonIdx, int latIdx, int varIdx, int timeIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_Lon(int timeIdx, int latIdx, int varIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_Lat(int timeIdx, int lonIdx, int varIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    // </editor-fold>       
}
