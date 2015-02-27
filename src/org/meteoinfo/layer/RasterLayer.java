 /* Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteoinfo.layer;

import com.l2fprod.common.beans.BaseBeanInfo;
import org.meteoinfo.data.GridData;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.legend.ColorBreak;
import org.meteoinfo.legend.LegendScheme;
import org.meteoinfo.shape.ShapeTypes;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 *
 * @author yaqiang
 */
public class RasterLayer extends ImageLayer {
    // <editor-fold desc="Variables">

    //private LegendScheme _legendScheme;
    private GridData _gridData;
    private GridData _originGridData = null;
    private boolean _isProjected = false;
    private List<Color> _colors;
    //private InterpolationMode _interpMode = InterpolationMode.NearestNeighbor;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public RasterLayer() {
        this.setLayerType(LayerTypes.RasterLayer);
        this.setShapeType(ShapeTypes.Image);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Set legend scheme
     *
     * @param ls Legend scheme
     */
    @Override
    public void setLegendScheme(LegendScheme ls) {
        super.setLegendScheme(ls);
        if (ls.getBreakNum() < 200) {
            updateImage(ls);
        } else {
            setPaletteByLegend();
        }
    }

    /**
     * Get grid data
     *
     * @return Grid data
     */
    public GridData getGridData() {
        return _gridData;
    }

    /**
     * Set grid data
     * @param gdata Grid data
     */
    public void setGridData(GridData gdata) {
        _gridData = gdata;
        updateGridData();
    }

    /**
     * Get if is projected
     *
     * @return Boolean
     */
    public boolean isProjected() {
        return _isProjected;
    }

    /**
     * Set if is projected
     *
     * @param istrue Boolean
     */
    public void setProjected(boolean istrue) {
        _isProjected = istrue;
    }

//        public InterpolationMode InterpMode
//        {
//            get { return _interpMode; }
//            set 
//            { 
//                _interpMode = value;
//                if (_interpMode == InterpolationMode.Invalid)
//                    _interpMode = InterpolationMode.NearestNeighbor;
//            }
//        }
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Get cell value by a point
     *
     * @param iIdx I index
     * @param jIdx J index
     * @return Cell value
     */
    public double getCellValue(int iIdx, int jIdx) {
        return _gridData.getDoubleValue(iIdx, jIdx);
    }

    /**
     * Update image by legend scheme
     *
     * @param als The legend scheme
     */
    public void updateImage(LegendScheme als) {
        BufferedImage image = getImageFromGridData(_gridData, als);
        this.setImage(image);
    }

    /**
     * Update image
     */
    public void updateImage() {
        int xNum = _gridData.getXNum();
        int yNum = _gridData.getYNum();
        byte[] imageBytes = new byte[xNum * yNum];
        for (int i = 0; i < yNum; i++) {
            for (int j = 0; j < xNum; j++) {
                int value = -1;
                int b;
                for (b = 0; b < this.getLegendScheme().getLegendBreaks().size() - 1; b++) {
                    ColorBreak aCB = this.getLegendScheme().getLegendBreaks().get(b);
                    if (aCB.getStartValue().toString().equals(aCB.getEndValue().toString())) {
                        if (_gridData.data[i][j] == Double.parseDouble(aCB.getStartValue().toString())) {
                            value = b;
                            break;
                        }
                    } else {
                        if (_gridData.data[i][j] >= Double.parseDouble(aCB.getStartValue().toString())
                                && _gridData.data[i][j] < Double.parseDouble(aCB.getEndValue().toString())) {
                            value = b;
                            break;
                        }
                    }
                }
                if (value == -1) {
                    value = b;
                    if (this.getLegendScheme().getLegendBreaks().get(this.getLegendScheme().getBreakNum() - 1).isNoData()) {
                        if (!MIMath.doubleEquals(_gridData.data[i][j], this.getLegendScheme().getUndefValue())) {
                            value = b - 1;
                        }
                    }
                }
                imageBytes[i * xNum + j] = (byte) value;
            }
        }

//            Image = DrawMeteoData.CreateBitmap(imageBytes, xNum, yNum);
//            List<Color> colors = LegendScheme.GetColors();
//            DrawMeteoData.SetPalette(colors, Image);
    }

    private BufferedImage getImageFromGridData(GridData gdata, LegendScheme als) {
        int width, height, breakNum;
        width = gdata.getXNum();
        height = gdata.getYNum();
        breakNum = als.getBreakNum();
        double[] breakValue = new double[breakNum];
        Color[] breakColor = new Color[breakNum];
        Color undefColor = Color.white;
        for (int i = 0; i < breakNum; i++) {
            breakValue[i] = Double.parseDouble(als.getLegendBreaks().get(i).getEndValue().toString());
            breakColor[i] = als.getLegendBreaks().get(i).getColor();
            if (als.getLegendBreaks().get(i).isNoData()) {
                undefColor = als.getLegendBreaks().get(i).getColor();
            }
        }
        Color defaultColor = breakColor[breakNum - 1];    //默认颜色为最后一个颜色
        BufferedImage aImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        double oneValue;
        Color oneColor;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                //oneValue = gdata.data[i][j];
                oneValue = gdata.getDoubleValue(i, j);
                if (MIMath.doubleEquals(oneValue, gdata.missingValue)) {
                    oneColor = undefColor;
                } else {
                    oneColor = defaultColor;
                    //循环只到breakNum-1 是因为最后一个LegendBreaks的EndValue和StartValue是一样的
                    for (int k = 0; k < breakNum - 1; k++) {
                        if (oneValue < breakValue[k]) {
                            oneColor = breakColor[k];
                            break;
                        }
                    }
                }
                aImage.setRGB(j, height - i - 1, oneColor.getRGB());
            }
        }

        return aImage;
    }
    
    private BufferedImage getImageFromGridData(GridData gdata, List<Color> colors) {
        int width, height;
        width = gdata.getXNum();
        height = gdata.getYNum();
        BufferedImage aImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int oneValue;
        Color oneColor;
        int n = colors.size();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                oneValue = (int)gdata.data[i][j];
                oneColor = colors.get(oneValue);                
                aImage.setRGB(j, height - i - 1, oneColor.getRGB());
            }
        }

        return aImage;
    }

    private BufferedImage getImageFromGridData_back(GridData gdata, LegendScheme als) {
        int width, height, breakNum;
        width = gdata.getXNum();
        height = gdata.getYNum();
        breakNum = als.getBreakNum();
        double[] breakValue = new double[breakNum];
        Color[] breakColor = new Color[breakNum];
        for (int i = 0; i < breakNum; i++) {
            breakValue[i] = Double.parseDouble(als.getLegendBreaks().get(i).getEndValue().toString());
            breakColor[i] = als.getLegendBreaks().get(i).getColor();
        }
        Color defaultColor = breakColor[breakNum - 1];    //默认颜色为最后一个颜色
        BufferedImage aImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                double oneValue = gdata.data[i][j];
                Color oneColor = defaultColor;
                //循环只到breakNum-1 是因为最后一个LegendBreaks的EndValue和StartValue是一样的
                for (int k = 0; k < breakNum - 1; k++) {
                    if (oneValue < breakValue[k]) {
                        oneColor = breakColor[k];
                        break;
                    }
                }
                aImage.setRGB(j, height - i - 1, oneColor.getRGB());
            }
        }

        return aImage;
    }

    /**
     * Set color palette to a image from a palette file
     *
     * @param aFile File path
     */
    @Override
    public void setPalette(String aFile) {
        List<Color> colors = this.getColorsFromPaletteFile(aFile);
        BufferedImage image = this.getImageFromGridData(_gridData, colors);
        this.setImage(image);
        
        LegendScheme ls = new LegendScheme(ShapeTypes.Image);
        ls.importFromPaletteFile_Unique(aFile);
        this.setLegendScheme(ls);
    }

    /**
     * Set color palette by legend scheme
     */
    public void setPaletteByLegend() {
        _colors = this.getLegendScheme().getColors();
        BufferedImage image = this.getImageFromGridData(_gridData, _colors);
        this.setImage(image);
    }

    /**
     * Update grid data
     */
    public void updateGridData() {
        WorldFilePara aWFP = new WorldFilePara();

        //aWFP.xUL = _gridData.xArray[0];
        //aWFP.yUL = _gridData.yArray[_gridData.getYNum() - 1];
        aWFP.xUL = _gridData.xArray[0] - _gridData.getXDelt() / 2;
        aWFP.yUL = _gridData.yArray[_gridData.getYNum() - 1] + _gridData.getYDelt() / 2;
        aWFP.xScale = _gridData.getXDelt();
        aWFP.yScale = -_gridData.getYDelt();

        aWFP.xRotate = 0;
        aWFP.yRotate = 0;

        this.setWorldFilePara(aWFP);

        updateExtent();
    }

    private void updateExtent() {
        double XBR, YBR;
        XBR = _gridData.getXNum() * this.getWorldFilePara().xScale + this.getWorldFilePara().xUL;
        YBR = _gridData.getYNum() * this.getWorldFilePara().yScale + this.getWorldFilePara().yUL;
        Extent aExtent = new Extent();
        aExtent.minX = this.getWorldFilePara().xUL;
        aExtent.minY = YBR;
        aExtent.maxX = XBR;
        aExtent.maxY = this.getWorldFilePara().yUL;
        this.setExtent(aExtent);
    }

    /**
     * Update origin data
     */
    public void updateOriginData() {
        _originGridData = (GridData) _gridData.clone();
        _isProjected = true;
    }

    /**
     * Get origin data
     */
    public void getOriginData() {
        _gridData = (GridData) _originGridData.clone();
    }

    // </editor-fold>

    // <editor-fold desc="BeanInfo">
    public class RasterLayerBean {

        RasterLayerBean() {
        }

        // <editor-fold desc="Get Set Methods">
        /**
         * Get layer type
         *
         * @return Layer type
         */
        public LayerTypes getLayerType() {
            return RasterLayer.this.getLayerType();
        }

        /**
         * Set layer type
         *
         * @param lt Layer type
         */
        public void setLayerType(LayerTypes lt) {
            RasterLayer.this.setLayerType(lt);
        }

        /**
         * Get layer draw type
         *
         * @return Layer draw type
         */
        public LayerDrawType getLayerDrawType() {
            return RasterLayer.this.getLayerDrawType();
        }

        /**
         * Set layer draw type
         *
         * @param ldt Layer draw type
         */
        public void setLayerDrawType(LayerDrawType ldt) {
            RasterLayer.this.setLayerDrawType(ldt);
        }

        /**
         * Get file name
         *
         * @return File name
         */
        public String getFileName() {
            return RasterLayer.this.getFileName();
        }

        /**
         * Set file name
         *
         * @param fn File name
         */
        public void setFileName(String fn) {
            RasterLayer.this.setFileName(fn);
        }

        /**
         * Get layer handle
         *
         * @return Layer handle
         */
        public int getHandle() {
            return RasterLayer.this.getHandle();
        }

        /**
         * Get layer name
         *
         * @return Layer name
         */
        public String getLayerName() {
            return RasterLayer.this.getLayerName();
        }

        /**
         * Set layer name
         *
         * @param name Layer name
         */
        public void setLayerName(String name) {
            RasterLayer.this.setLayerName(name);
        }

        /**
         * Get if is maskout
         *
         * @return If is maskout
         */
        public boolean isMaskout() {
            return RasterLayer.this.isMaskout();
        }

        /**
         * Set if maskout
         *
         * @param value If maskout
         */
        public void setMaskout(boolean value) {
            RasterLayer.this.setMaskout(value);
        }

        /**
         * Get if is visible
         *
         * @return If is visible
         */
        public boolean isVisible() {
            return RasterLayer.this.isVisible();
        }

        /**
         * Set if is visible
         *
         * @param value If is visible
         */
        public void setVisible(boolean value) {
            RasterLayer.this.setVisible(value);
        }
        // </editor-fold>
    }

    public static class RasterLayerBeanBeanInfo extends BaseBeanInfo {

        public RasterLayerBeanBeanInfo() {
            super(RasterLayer.RasterLayerBean.class);
            addProperty("fileName").setCategory("Read only").setReadOnly().setDisplayName("File name");
            addProperty("layerType").setCategory("Read only").setReadOnly().setDisplayName("Layer type");
            addProperty("layerDrawType").setCategory("Read only").setReadOnly().setDisplayName("Layer draw type");
            addProperty("handle").setCategory("Read only").setReadOnly().setDisplayName("Handle");
            addProperty("layerName").setCategory("Editable").setDisplayName("Layer name");
            addProperty("visible").setCategory("Editable").setDisplayName("Visible");
            addProperty("maskout").setCategory("Editable").setDisplayName("Is maskout");
        }
    }
    // </editor-fold>
}
