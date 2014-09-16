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

import org.meteoinfo.data.mapdata.AttributeTable;
import org.meteoinfo.data.mapdata.Field;
import org.meteoinfo.data.mapdata.ShapeFileManage;
import org.meteoinfo.geoprocess.GeoComputation;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.GenericFileFilter;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import org.meteoinfo.global.PointF;
import org.meteoinfo.global.colors.ColorUtil;
import org.meteoinfo.global.table.DataColumn;
import org.meteoinfo.global.table.DataRow;
import org.meteoinfo.global.table.DataTypes;
import org.meteoinfo.legend.ChartBreak;
import org.meteoinfo.legend.ColorBreak;
import org.meteoinfo.legend.LabelBreak;
import org.meteoinfo.legend.LegendType;
import org.meteoinfo.legend.PolygonBreak;
import org.meteoinfo.legend.PolylineBreak;
import org.meteoinfo.shape.Graphic;
import org.meteoinfo.shape.PointShape;
import org.meteoinfo.shape.PolygonShape;
import org.meteoinfo.shape.PolylineShape;
import org.meteoinfo.shape.Shape;
import org.meteoinfo.shape.ShapeTypes;
import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import org.meteoinfo.global.table.DataColumnCollection;
import org.meteoinfo.global.table.DataTable;
import org.meteoinfo.legend.LegendManage;
import org.meteoinfo.legend.LegendScheme;
import org.meteoinfo.legend.PointBreak;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.meteoinfo.shape.PointZShape;
import org.meteoinfo.shape.Polygon;
import org.meteoinfo.shape.Polyline;
import org.meteoinfo.shape.PolylineZShape;

/**
 * Vector layer class
 *
 * @author yaqiang
 */
public class VectorLayer extends MapLayer {
    // <editor-fold desc="Variables">

    private boolean _isEditing;
    private boolean _avoidCollision;
    private List<Shape> _shapeList;
    private AttributeTable _attributeTable;
    private LabelSet _labelSet;
    private List<Graphic> _labelPoints;
    private ChartSet _chartSet;
    private List<Graphic> _chartPoints;
    //private int _numFields;
    private int _identiferShape;
    private float _drawingZoom = 1.0f;
    private List<Shape> _originShapes = null;
    private AttributeTable _originAttributeTable = null;
    private List<Graphic> _originLabelPoints = null;
    private List<Graphic> _originChartPoints = null;
    private boolean _projected = false;
    // </editor-fold>

    // <editor-fold desc="Constructor">
    /**
     * Constructor
     *
     * @param shapeType Shape type
     */
    public VectorLayer(ShapeTypes shapeType) {
        super();
        this.setLayerType(LayerTypes.VectorLayer);
        this.setShapeType(shapeType);
        _avoidCollision = false;
        _attributeTable = new AttributeTable();
        _labelSet = new LabelSet();
        _labelPoints = new ArrayList<Graphic>();
        _chartSet = new ChartSet();
        _chartPoints = new ArrayList<Graphic>();
        _shapeList = new ArrayList<Shape>();
        _isEditing = false;
    }
    // </editor-fold>

    // <editor-fold desc="Get Set Methods">
    /**
     * Get if avoid collision
     *
     * @return Boolean
     */
    public boolean getAvoidCollision() {
        return this._avoidCollision;
    }

    /**
     * Set if avoid collision
     *
     * @param istrue Boolean
     */
    public void setAvoidCollision(boolean istrue) {
        this._avoidCollision = istrue;
    }

    /**
     * Get shape number
     *
     * @return Shape number
     */
    public int getShapeNum() {
        return _shapeList.size();
    }

    /**
     * Get shape list
     *
     * @return Shape list
     */
    public List<? extends Shape> getShapes() {
        return _shapeList;
    }

    /**
     * Set shape list
     *
     * @param shape list
     */
    public void setShapes(List<? extends Shape> shapes) {
        _shapeList = (List<Shape>) shapes;
    }

    /**
     * Get attribute table
     *
     * @return The attribute table
     */
    public AttributeTable getAttributeTable() {
        return _attributeTable;
    }

    /**
     * Set attribute table
     *
     * @param table The attribute table
     */
    public void setAttributeTable(AttributeTable table) {
        _attributeTable = table;
    }

//    /**
//     * Get field number
//     *
//     * @return Field number
//     */
//    public int getFieldNum() {
//        return this._numFields;
//    }
//    /**
//     * Set field number
//     *
//     * @param fNum Field number
//     */
//    public void setFieldNum(int fNum) {
//        this._numFields = fNum;
//    }
    /**
     * Get identifer shape index
     *
     * @return The identifer shape index
     */
    public int getIdentiferShape() {
        return this._identiferShape;
    }

    /**
     * Set identifer shape index
     *
     * @param idx Identifer shape index
     */
    public void setIdentiferShape(int idx) {
        this._identiferShape = idx;
    }

    @Override
    public void setTransparency(int trans) {
        super.setTransparency(trans);
        switch (this.getShapeType()) {
            case Polygon:
                for (int i = 0; i < this.getLegendScheme().getBreakNum(); i++) {
                    PolygonBreak aPGB = (PolygonBreak) this.getLegendScheme().getLegendBreaks().get(i);
                    int alpha = (int) ((1 - (double) trans / 100.0) * 255);
                    Color aColor = aPGB.getColor();
                    aPGB.setColor(new Color(aColor.getRed(), aColor.getGreen(), aColor.getBlue(), alpha));
                }
                break;
        }
    }

    /**
     * Get label set
     *
     * @return Label set
     */
    public LabelSet getLabelSet() {
        return _labelSet;
    }

    /**
     * Set label set
     *
     * @param ls Label set
     */
    public void setLabelSet(LabelSet ls) {
        _labelSet = ls;
    }

    /**
     * Get label points
     *
     * @return The lable points
     */
    public List<Graphic> getLabelPoints() {
        return this._labelPoints;
    }

    /**
     * Set label points
     *
     * @param lps The lable points
     */
    public void setLabelPoints(List<Graphic> lps) {
        this._labelPoints = lps;
    }

    /**
     * Get chart set
     *
     * @return Chart set
     */
    public ChartSet getChartSet() {
        return _chartSet;
    }

    /**
     * Set chart set
     *
     * @param cs Chart set
     */
    public void setChartSet(ChartSet cs) {
        _chartSet = cs;
    }

    /**
     * Get chart points
     *
     * @return The chart points
     */
    public List<Graphic> getChartPoints() {
        return this._chartPoints;
    }

    /**
     * Set chart pints
     *
     * @param cps The chart points
     */
    public void setChartPoints(List<Graphic> cps) {
        this._chartPoints = cps;
    }

    /**
     * Get drawing zoom
     *
     * @return Drawing zoom
     */
    public float getDrawingZoom() {
        return this._drawingZoom;
    }

    /**
     * Set drawing zoom
     *
     * @param zoom Drawing zoom
     */
    public void setDrawingZoom(float zoom) {
        _drawingZoom = zoom;
    }

    /**
     * Get if is projected
     *
     * @return Boolean
     */
    public boolean isProjected() {
        return _projected;
    }

    /**
     * Set if is projected
     *
     * @param istrue Boolean
     */
    public void setProjected(boolean istrue) {
        _projected = istrue;
    }

    /**
     * Override set legend scheme
     *
     * @param value Legend scheme
     */
    @Override
    public void setLegendScheme(LegendScheme value) {
        super.setLegendScheme(value);
        List<String> fieldNames = this._attributeTable.getTable().getColumnNames();
        switch (value.getLegendType()) {
            case UniqueValue:
            case GraduatedColor:
                if (!fieldNames.contains(value.getFieldName())) {
                    String fName = "";
                    if (fieldNames.size() > 0) {
                        fName = fieldNames.get(0);
                    }
                    LegendScheme ls = this.createLegendScheme(LegendType.SingleSymbol, fName);
                    super.setLegendScheme(ls);
                }
                break;
        }
        updateLegendIndexes();
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    // <editor-fold desc="Chart">
    /**
     * Update charts properties
     */
    public void updateChartsProp() {
        for (Graphic chartG : _chartPoints) {
            ChartBreak aCP = (ChartBreak) chartG.getLegend();
            aCP.setLegendScheme(_chartSet.getLegendScheme());
            aCP.setMinSize(_chartSet.getMinSize());
            aCP.setMaxSize(_chartSet.getMaxSize());
            aCP.setMinValue(_chartSet.getMinValue());
            aCP.setMaxValue(_chartSet.getMaxValue());
            aCP.setBarWidth(_chartSet.getBarWidth());
            aCP.setAlignType(_chartSet.getAlignType());
            aCP.setView3D(_chartSet.isView3D());
            aCP.setThickness(_chartSet.getThickness());
        }
    }

    /**
     * Add charts
     */
    public void addCharts() {
        List<Shape> shapeList = new ArrayList<Shape>(this._shapeList);
        int shapeIdx = -1;
        PointD aPoint = new PointD();

        List<Integer> selShapeIdx = getSelectedShapeIndexes();
        boolean isShapeSel = true;
        if (selShapeIdx.isEmpty()) {
            isShapeSel = false;
        }
        for (Shape aShape : shapeList) {
            shapeIdx += 1;
            if (isShapeSel) {
                if (!aShape.isSelected()) {
                    continue;
                }
            }

            PointShape aPS = new PointShape();
            switch (this.getShapeType()) {
                case Point:
                case PointM:
                case PointZ:
                    aPS.setPoint(((PointShape) aShape).getPoint());
                    break;
                case Polyline:
                case PolylineM:
                case PolylineZ:
                    int pIdx = ((PolylineShape) aShape).getPoints().size() / 2;
                    aPS.setPoint(((PolylineShape) aShape).getPoints().get(pIdx - 1));
                    break;
                case Polygon:
                case PolygonM:
                    Extent aExtent = aShape.getExtent();
                    aPoint.X = (aExtent.minX + aExtent.maxX) / 2;
                    aPoint.Y = (aExtent.minY + aExtent.maxY) / 2;
                    aPS.setPoint(aPoint);
                    break;
            }

            ChartBreak aCP = new ChartBreak(_chartSet.getChartType());
            for (String fn : _chartSet.getFieldNames()) {
                aCP.getChartData().add(Float.parseFloat(getCellValue(fn, shapeIdx).toString()));
            }
            aCP.setXShift(_chartSet.getXShift());
            aCP.setYShift(_chartSet.getYShift());
            aCP.setLegendScheme(_chartSet.getLegendScheme());
            aCP.setMinSize(_chartSet.getMinSize());
            aCP.setMaxSize(_chartSet.getMaxSize());
            aCP.setMinValue(_chartSet.getMinValue());
            aCP.setMaxValue(_chartSet.getMaxValue());
            aCP.setBarWidth(_chartSet.getBarWidth());
            aCP.setAlignType(_chartSet.getAlignType());
            aCP.setView3D(_chartSet.isView3D());
            aCP.setThickness(_chartSet.getThickness());
            aCP.setShapeIndex(shapeIdx);

            Graphic aGraphic = new Graphic(aPS, aCP);
            addChart(aGraphic);
        }
        _chartSet.setDrawCharts(true);
    }

    /**
     * Add a chart point
     *
     * @param aCP Chart point
     */
    public void addChart(Graphic aCP) {
        _chartPoints.add(aCP);
    }

    /**
     * Remove all charts
     */
    public void removeCharts() {
        _chartPoints.clear();
        _chartSet.setDrawCharts(false);
    }

    // </editor-fold>
    // <editor-fold desc="Shape">
    /**
     * Add a shape
     *
     * @param aShape Shape
     */
    public void addShape(Shape aShape) {
        _shapeList.add(aShape);
        updateLayerExtent(aShape);
    }
    
    /**
     * Select shapes
     *
     * @param aExtent The extent
     * @return Selected shapes
     */
    public List<Integer> selectShapes(Extent extent){
        return this.selectShapes(extent, false);
    }

    /**
     * Select shapes
     *
     * @param aExtent The extent
     * @param isSingleSel If just select one shape
     * @return Selected shapes
     */
    public List<Integer> selectShapes(Extent aExtent, boolean isSingleSel) {
        return this.selectShapes(aExtent, _shapeList, isSingleSel);
    }
    
    /**
     * Select shapes
     *
     * @param aExtent The extent
     * @param shapes The shape list to be selected
     * @param isSingleSel If just select one shape
     * @return Selected shapes
     */
    public List<Integer> selectShapes(Extent aExtent, List<Shape> shapes, boolean isSingleSel) {
        List<Integer> selectedShapes = new ArrayList<Integer>();
        int i, j;
        PointD sp = aExtent.getCenterPoint();

        switch (this.getShapeType()) {
            case Point:
            case PointM:
            case PointZ:
            case WindArraw:
            case WindBarb:
            case WeatherSymbol:
            case StationModel:
                for (i = 0; i < shapes.size(); i++) {
                    PointShape aPS = (PointShape) shapes.get(i);
                    if (MIMath.pointInExtent(aPS.getPoint(), aExtent)) {
                        selectedShapes.add(_shapeList.indexOf(aPS));
                        if (isSingleSel) {
                            break;
                        }
                    }
                }
                break;
            case Polyline:
            case PolylineM:
            case PolylineZ:
                Object sel;
                List<Double> dislist = new ArrayList<Double>();
                for (i = 0; i < shapes.size(); i++) {
                    PolylineShape aPLS = (PolylineShape) shapes.get(i);
                    if (MIMath.isExtentCross(aExtent, aPLS.getExtent())) {
                        sel = GeoComputation.selectPolylineShape(sp, aPLS, aExtent.getWidth() / 2);
                        if (sel != null) {
                            if (dislist.size() > 0){
                                for (j = 0; j < dislist.size(); j++){
                                    if ((Double)sel < dislist.get(j)){
                                        selectedShapes.add(j, _shapeList.indexOf(aPLS));
                                        dislist.add(j, (Double)sel);
                                        break;
                                    }
                                }
                            } else {
                                selectedShapes.add(_shapeList.indexOf(aPLS));    
                                dislist.add((Double)sel);
                            }
                            if (isSingleSel) {
                                break;
                            }
                        }
                    }
                }
                break;
            case Polygon:
            case PolygonM:
                for (i = shapes.size() - 1; i >= 0; i--) {
                    PolygonShape aPGS = (PolygonShape) shapes.get(i);
                    if (isSingleSel) {
                        if (GeoComputation.pointInPolygon(aPGS, sp)) {
                            selectedShapes.add(_shapeList.indexOf(aPGS));
                            break;
                        }
                    } else {
                        if (MIMath.isExtentCross(aExtent, aPGS.getExtent())) {
                            for (j = 0; j < aPGS.getPolygons().get(0).getOutLine().size(); j++) {
                                if (MIMath.pointInExtent(aPGS.getPolygons().get(0).getOutLine().get(j), aExtent)) {
                                    selectedShapes.add(_shapeList.indexOf(aPGS));
                                    break;
                                }
                            }
                        }
                    }
                }
                break;
        }

        return selectedShapes;
    }

    /**
     * Select shapes by a polygon shape
     *
     * @param polygonShape The polygon shape
     * @return Selected shape indexes
     */
    public List<Integer> selectShapes(PolygonShape polygonShape) {
        List<Integer> selIdxs = new ArrayList<Integer>();
        for (int i = 0; i < _shapeList.size(); i++) {
            boolean isIn = false;
            List<PointD> points = (List<PointD>) _shapeList.get(i).getPoints();
            for (PointD aPoint : points) {
                if (GeoComputation.pointInPolygon(polygonShape, aPoint)) {
                    isIn = true;
                    break;
                }
            }

            if (isIn) {
                _shapeList.get(i).setSelected(true);
                selIdxs.add(i);
            }
        }

        return selIdxs;
    }
    
    /**
     * Get selected shapes
     * @return Selected shapes
     */
    public List<Shape> getSelectedShapes(){
        List<Shape> selShapes = new ArrayList<Shape>();
        for (Shape shape : _shapeList){
            if (shape.isSelected())
                selShapes.add(shape);
        }
        
        return selShapes;
    }

    /**
     * Get selected shape index list
     *
     * @return Index list
     */
    public List<Integer> getSelectedShapeIndexes() {
        List<Integer> selIndexes = new ArrayList<Integer>();
        for (int i = 0; i < this.getShapeNum(); i++) {
            if (_shapeList.get(i).isSelected()) {
                selIndexes.add(i);
            }
        }

        return selIndexes;
    }
    
    /**
     * Get visible shapes
     * @return The visible shapes
     */
    public List<Shape> getVisibleShapes(){
        List<Shape> visShapes = new ArrayList<Shape>();
        for (Shape shape : _shapeList){
            if (shape.isVisible())
                visShapes.add(shape);
        }
        
        return visShapes;
    }

    /**
     * Clear selected shapes
     */
    public void clearSelectedShapes() {
        for (Shape aShape : _shapeList) {
            if (aShape.isSelected()) {
                aShape.setSelected(false);
            }
        }
    }

    /**
     * Get if has selected shape
     *
     * @return Boolean
     */
    public boolean hasSelectedShapes() {
        for (Shape shape : _shapeList) {
            if (shape.isSelected()) {
                return true;
            }
        }

        return false;
    }

    // </editor-fold>
    // <editor-fold desc="Attribute Table">
    /**
     * Get field number
     *
     * @return Field number
     */
    public int getFieldNumber() {
        return _attributeTable.getTable().getColumns().size();
    }

    /**
     * Get field name by index
     *
     * @param FieldIndex Field index
     * @return Field name
     */
    public String getFieldName(int FieldIndex) {
        return _attributeTable.getTable().getColumns().get(FieldIndex).getColumnName();
    }

    /**
     * Get field name list
     *
     * @return The field name list
     */
    public List<String> getFieldNames() {
        List<String> FNList = new ArrayList<String>();
        for (int i = 0; i < this.getFieldNumber(); i++) {
            FNList.add(getFieldName(i));
        }
        return FNList;
    }
    
    /**
     * Get fields
     * @return Fields
     */
    public List<Field> getFields(){
        DataColumnCollection cols = _attributeTable.getTable().getColumns();
        List<Field> fields = new ArrayList<Field>();
        for (DataColumn col : cols){
            fields.add(new Field(col));
        }
        
        return fields;
    }

    /**
     * Get field by index
     *
     * @param idx The field index
     * @return The field
     */
    public Field getField(int idx) {
        return (Field) _attributeTable.getTable().getColumns().get(idx);
    }

    /**
     * Get field by field name
     *
     * @param fieldName Field name
     * @return The field
     */
    public Field getField(String fieldName) {
        return (Field) _attributeTable.getTable().getColumns().get(fieldName);
    }

    /**
     * Get field index by name
     *
     * @param fieldName The field name
     * @return Field index
     */
    public int getFieldIdxByName(String fieldName) {
        int fieldIdx = -1;
        for (int i = 0; i < this.getFieldNumber(); i++) {
            if (_attributeTable.getTable().getColumns().get(i).getColumnName().equals(fieldName)) {
                fieldIdx = i;
                break;
            }
        }

        return fieldIdx;
    }

    /**
     * Get cell value
     *
     * @param fieldIndex Field index
     * @param shapeIndex Shape index
     * @return Cell value
     */
    public Object getCellValue(int fieldIndex, int shapeIndex) {
        return _attributeTable.getTable().getValue(shapeIndex, fieldIndex);
    }

    /**
     * Get cell value
     *
     * @param fieldName Field name
     * @param shapeIndex Shape index
     * @return Cell value
     */
    public Object getCellValue(String fieldName, int shapeIndex) {
        return _attributeTable.getTable().getValue(shapeIndex, fieldName);
    }

    /**
     * Edit: Add a field
     *
     * @param aField The field
     */
    public void editAddField(DataColumn aField) {
        for (int i = 0; i < this.getFieldNumber(); i++) {
            if (aField.getColumnName().equals(_attributeTable.getTable().getColumns().get(i).getColumnName())) {
                aField.setColumnName(aField.getColumnName() + "_1");
            }
        }
        _attributeTable.getTable().getColumns().add(aField);
    }

    /**
     * Edit: Add a field
     *
     * @param fieldName Field name
     * @param fieldType Field data type
     */
    public void editAddField(String fieldName, DataTypes fieldType) {
        Field aField = new Field(fieldName, fieldType);
        editAddField(aField);
    }        

    private void insertRecord(int position) throws Exception {
        DataRow aDR = _attributeTable.getTable().newRow();
        _attributeTable.getTable().getRows().add(position, aDR);
    }

    /**
     * Edit: Edit cell value
     *
     * @param fieldName Field name
     * @param shapeIndex Shape index
     * @param value The data value
     */
    public void editCellValue(String fieldName, int shapeIndex, Object value) {
        _attributeTable.getTable().getRows().get(shapeIndex).setValue(fieldName, value);
    }

    /**
     * Edit: Edit cell value
     *
     * @param fieldIndex Field index
     * @param shapeIndex Shape index
     * @param value Data value
     */
    public void editCellValue(int fieldIndex, int shapeIndex, Object value) {
        _attributeTable.getTable().getRows().get(shapeIndex).setValue(fieldIndex, value);
    }

    /**
     * Get minimum data value of a field
     *
     * @param fieldName Field name
     * @return Minimum data
     */
    public double getMinValue(String fieldName) {
        if (MIMath.isNumeric((Field) _attributeTable.getTable().getColumns().get(fieldName))) {
            double min = 0;
            int dNum = 0;
            for (int i = 0; i < this.getShapeNum(); i++) {
                double aValue = Double.parseDouble(getCellValue(fieldName, i).toString());
                if (Math.abs(aValue / this.getLegendScheme().getUndefValue() - 1) < 0.01) {
                    continue;
                }

                if (dNum == 0) {
                    min = aValue;
                } else {
                    if (min > aValue) {
                        min = aValue;
                    }
                }
                dNum += 1;
            }
            return min;
        } else {
            return 0;
        }
    }
    // </editor-fold>

    // <editor-fold desc="Edit shape">
    /**
     * Edit: Insert shape
     *
     * @param aShape The shape
     * @param position The position index
     * @return If success
     */
    public boolean editInsertShape(Shape aShape, int position) throws Exception {
        _shapeList.add(position, aShape);
        insertRecord(position);
        updateLayerExtent(aShape);

        return true;
    }

    private void updateLayerExtent(Shape aShape) {
        if (this.getShapeNum() == 1) {
            this.setExtent((Extent) aShape.getExtent().clone());
        } else {
            this.setExtent(MIMath.getLagerExtent(this.getExtent(), aShape.getExtent()));
        }
    }

    private void updateLayerExtent() {
        if (this.getShapeNum() == 1) {
            this.setExtent((Extent) _shapeList.get(0).getExtent().clone());
        } else {
            this.setExtent((Extent) _shapeList.get(0).getExtent().clone());
            for (int i = 1; i < this.getShapeNum(); i++) {
                this.setExtent(MIMath.getLagerExtent(this.getExtent(), _shapeList.get(i).getExtent()));
            }

        }
    }

    /**
     * Save layer as a shape file
     */
    public void saveFile() {
        File aFile = new File(this.getFileName());
        if (aFile.exists()) {
            saveFile(aFile.getAbsolutePath());
        } else {
            JFileChooser aDlg = new JFileChooser();
            String curDir = System.getProperty("user.dir");
            aDlg.setCurrentDirectory(new File(curDir));
            String[] fileExts = {"shp"};
            GenericFileFilter pFileFilter = new GenericFileFilter(fileExts, "Shape File (*.shp)");
            aDlg.setFileFilter(pFileFilter);
            aDlg.setAcceptAllFileFilterUsed(false);
            if (JFileChooser.APPROVE_OPTION == aDlg.showSaveDialog(null)) {
                aFile = aDlg.getSelectedFile();
                System.setProperty("user.dir", aFile.getParent());
                String extent = ((GenericFileFilter) aDlg.getFileFilter()).getFileExtent();
                String fileName = aFile.getAbsolutePath();
                if (!fileName.substring(fileName.length() - extent.length()).equals(extent)) {
                    fileName = fileName + "." + extent;
                }
                saveFile(fileName);
            }
        }
    }

    /**
     * Save layer as a shape file
     *
     * @param shpfilepath Shape file path
     */
    public void saveFile(String shpfilepath) {
        this.setFileName(shpfilepath);
        File aFile = new File(shpfilepath);
        if (aFile.exists()) {
            String shxfilepath = shpfilepath.replace(shpfilepath.substring(shpfilepath.lastIndexOf(".")), ".shx");
            String dbffilepath = shpfilepath.replace(shpfilepath.substring(shpfilepath.lastIndexOf(".")), ".dbf");
            String projfilepath = shpfilepath.replace(shpfilepath.substring(shpfilepath.lastIndexOf(".")), ".prj");
            new File(shxfilepath).delete();
            new File(dbffilepath).delete();
            new File(projfilepath).delete();
            aFile.delete();
        }
        try {
            ShapeFileManage.saveShapeFile(shpfilepath, this);
        } catch (IOException ex) {
            Logger.getLogger(VectorLayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Clip the layer by a clipping layer
     *
     * @param clipLayer Clipping layer
     * @param onlySel If only using selected shapes in clipping layer
     * @return Clipped result layer
     */
    public VectorLayer clip(VectorLayer clipLayer, boolean onlySel) {
        List<Shape> shapes = new ArrayList<Shape>();
        if (onlySel) {
            for (Shape aShape : clipLayer.getShapes()) {
                if (aShape.isSelected()) {
                    shapes.add(aShape);
                }
            }
        } else {
            shapes = (List<Shape>) clipLayer.getShapes();
        }

        VectorLayer newLayer = (VectorLayer) this.cloneValue();
        DataTable aTable = new DataTable();
        for (DataColumn aDC : this.getAttributeTable().getTable().getColumns()) {
            Field bDC = new Field(aDC.getColumnName(), aDC.getDataType());
            aTable.getColumns().add(bDC);
        }

        newLayer.setShapes(new ArrayList<Shape>());
        for (Shape aShape : shapes) {
            PolygonShape aPGS = (PolygonShape) aShape;
            for (int i = 0; i < this.getShapeNum(); i++) {
                Shape bShape = this.getShapes().get(i);
                DataRow aDR = this.getAttributeTable().getTable().getRows().get(i);
                for (Polygon aPolygon : aPGS.getPolygons()) {
                    Shape clipShape = GeoComputation.clipShape(bShape, aPolygon.getOutLine());
                    if (clipShape != null) {
                        newLayer.addShape(clipShape);
                        try {
                            aTable.addRow((DataRow) aDR.clone());
                        } catch (Exception ex) {
                            Logger.getLogger(VectorLayer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }

        newLayer.getAttributeTable().setTable(aTable);
        newLayer.setLegendScheme((LegendScheme) this.getLegendScheme().clone());
        newLayer.setTransparency(this.getTransparency());

        return newLayer;
    }
    // </editor-fold>

    // <editor-fold desc="Labels">   
    /**
     * Add label point
     *
     * @param aLP Label point
     */
    public void addLabel(Graphic aLP) {
        _labelPoints.add(aLP);
    }

    /**
     * Remove all labels
     */
    public void removeLabels() {
        _labelPoints.clear();
        _labelSet.setDrawLabels(false);
    }

    /**
     * Add labels
     */
    public void addLabels() {
        addLabelsByColor();

        _labelSet.setDrawLabels(true);
    }

    /**
     * Add labels
     */
    private void addLabelsByColor() {
        int shapeIdx = -1;
        PointD aPoint;

        String dFormat = "%1$.1f";
        boolean isData = false;
        if (MIMath.isNumeric((Field) _attributeTable.getTable().getColumns().get(_labelSet.getFieldName()))) {
            if (_labelSet.isAutoDecimal()) {
                double min = getMinValue(_labelSet.getFieldName());
                _labelSet.setDecimalDigits(MIMath.getDecimalNum(min));
            }

            dFormat = "%1$." + String.valueOf(_labelSet.getDecimalDigits()) + "f";
            isData = true;
        }

        List<Integer> selShapeIdx = getSelectedShapeIndexes();
        boolean isShapeSel = true;
        if (selShapeIdx.isEmpty()) {
            isShapeSel = false;
        }
        for (Shape aShape : _shapeList) {
            shapeIdx += 1;
            if (isShapeSel) {
                if (!aShape.isSelected()) {
                    continue;
                }
            }

            ColorBreak aCB = null;
            if (this.getLegendScheme().getLegendType() == LegendType.SingleSymbol) {
                aCB = this.getLegendScheme().getLegendBreaks().get(0);
            } else {
                if (this.getLegendScheme().getFieldName() != null) {
                    String vStr = getCellValue(this.getLegendScheme().getFieldName(), shapeIdx).toString().trim();
                    aCB = getColorBreak(vStr);
                }
            }
            if (aCB == null) {
                continue;
            }
            if (!aCB.isDrawShape()) {
                continue;
            }

            PointShape aPS = new PointShape();
            switch (this.getShapeType()) {
                case Point:
                case PointM:
                case PointZ:
                    aPS.setPoint((PointD) ((PointShape) aShape).getPoint().clone());
                    break;
                case Polyline:
                case PolylineM:
                case PolylineZ:
                    int pIdx = ((PolylineShape) aShape).getPoints().size() / 2;
                    aPS.setPoint((PointD) ((PolylineShape) aShape).getPoints().get(pIdx - 1).clone());
                    break;
                case Polygon:
                case PolygonM:
                    Extent aExtent = aShape.getExtent();
                    aPoint = new PointD();
                    aPoint.X = ((aExtent.minX + aExtent.maxX) / 2);
                    aPoint.Y = ((aExtent.minY + aExtent.maxY) / 2);
                    aPS.setPoint(aPoint);
                    break;
            }

            LabelBreak aLP = new LabelBreak();
            if (isData) {
                aLP.setText(String.format(dFormat, Double.parseDouble(getCellValue(_labelSet.getFieldName(), shapeIdx).toString())));
            } else {
                aLP.setText(getCellValue(_labelSet.getFieldName(), shapeIdx).toString());
            }

            if (_labelSet.isColorByLegend()) {
                aLP.setColor(aCB.getColor());
            } else {
                aLP.setColor(_labelSet.getLabelColor());
            }
            aLP.setFont(_labelSet.getLabelFont());
            aLP.setAlignType(_labelSet.getLabelAlignType());
            aLP.setYShift(_labelSet.getYOffset());
            aLP.setXShift(_labelSet.getXOffset());
            Graphic aGraphic = new Graphic(aPS, aLP);
            addLabel(aGraphic);
        }
    }

    private ColorBreak getColorBreak(String vStr) {
        ColorBreak aCB = null;
        switch (this.getLegendScheme().getLegendType()) {
            case SingleSymbol:
                aCB = this.getLegendScheme().getLegendBreaks().get(0);
                break;
            case UniqueValue:
                for (int j = 0; j < this.getLegendScheme().getLegendBreaks().size(); j++) {
                    if (vStr.equals(this.getLegendScheme().getLegendBreaks().get(j).getStartValue().toString())) {
                        aCB = this.getLegendScheme().getLegendBreaks().get(j);
                        break;
                    }
                }
                break;
            case GraduatedColor:
                double value;
                if ("".equals(vStr) || vStr == null) {
                    value = 0;
                } else {
                    value = Double.parseDouble(vStr);
                }
                int blNum = 0;
                for (int j = 0; j < this.getLegendScheme().getLegendBreaks().size(); j++) {
                    ColorBreak aPB = this.getLegendScheme().getLegendBreaks().get(j);
                    blNum += 1;
                    if (value == Double.parseDouble(aPB.getStartValue().toString()) || (value > Double.parseDouble(aPB.getStartValue().toString())
                            && value < Double.parseDouble(aPB.getEndValue().toString()))
                            || (blNum == this.getLegendScheme().getLegendBreaks().size()
                            && value == Double.parseDouble(aPB.getEndValue().toString()))) {
                        aCB = aPB;
                        break;
                    }
                }
                break;
        }

        return aCB;
    }

    /**
     * Add labels of contour layer dynamicly
     *
     * @param sExtent View extent of MapView
     */
    public void addLabelsContourDynamic(Extent sExtent) {
        String dFormat = "%1$.1f";
        if (_labelSet.isAutoDecimal()) {
            double min = getMinValue(_labelSet.getFieldName());
            _labelSet.setDecimalDigits(MIMath.getDecimalNum(min));
        }

        dFormat = "%1$." + String.valueOf(_labelSet.getDecimalDigits()) + "f";
        int shapeIdx = 0;
        for (Shape aShape : _shapeList) {
            PolylineShape aPLS = (PolylineShape) aShape;
            Extent IExtent = aPLS.getExtent();
            if (IExtent.maxX - IExtent.minX > (sExtent.maxX - sExtent.minX) / 10
                    || IExtent.maxY - IExtent.minY > (sExtent.maxY - sExtent.minY) / 10) {
                LabelBreak aLP = new LabelBreak();
                int pIdx = aPLS.getPoints().size() / 2;
                PointF aPoint = new PointF(0, 0);
                PointShape aPS = new PointShape();
                aPS.setPoint(aPLS.getPoints().get(pIdx - 1));
                aLP.setText(String.format(dFormat, Double.parseDouble(getCellValue(_labelSet.getFieldName(), shapeIdx).toString())));
                aLP.setFont(_labelSet.getLabelFont());
                aLP.setAlignType(_labelSet.getLabelAlignType());
                aLP.setYShift(_labelSet.getYOffset());

                String vStr;
                PolylineBreak aPLB;
                switch (this.getLegendScheme().getLegendType()) {
                    case SingleSymbol:
                        aPLB = (PolylineBreak) this.getLegendScheme().getLegendBreaks().get(0);
                        aLP.setColor(aPLB.getColor());
                        break;
                    case UniqueValue:
                        vStr = getCellValue(_labelSet.getFieldName(), shapeIdx).toString();
                        for (int j = 0; j < this.getLegendScheme().getLegendBreaks().size(); j++) {
                            aPLB = (PolylineBreak) this.getLegendScheme().getLegendBreaks().get(j);
                            if (vStr.equals(aPLB.getStartValue().toString())) {
                                aLP.setColor(aPLB.getColor());
                            }
                        }
                        break;
                    case GraduatedColor:
                        vStr = getCellValue(_labelSet.getFieldName(), shapeIdx).toString();
                        double value = Double.parseDouble(vStr);
                        int blNum = 0;
                        for (int j = 0; j < this.getLegendScheme().getLegendBreaks().size(); j++) {
                            aPLB = (PolylineBreak) this.getLegendScheme().getLegendBreaks().get(j);
                            blNum += 1;
                            if (value == Double.parseDouble(aPLB.getStartValue().toString())
                                    || (value > Double.parseDouble(aPLB.getStartValue().toString())
                                    && value < Double.parseDouble(aPLB.getEndValue().toString()))
                                    || (blNum == this.getLegendScheme().getLegendBreaks().size()
                                    && value == Double.parseDouble(aPLB.getEndValue().toString()))) {
                                aLP.setColor(aPLB.getColor());
                            }
                        }
                        break;
                }

                Graphic aGraphic = new Graphic(aPS, aLP);
                addLabel(aGraphic);
            }
            shapeIdx += 1;
        }

        _labelSet.setDrawLabels(true);
    }
    // </editor-fold>

    // <editor-fold desc="Projection">
    /**
     * Update data to origion set
     */
    public void updateOriginData() {
        _originAttributeTable = (AttributeTable) _attributeTable.clone();
        _originShapes = new ArrayList<Shape>();
        for (Shape aShape : _shapeList) {
            _originShapes.add((Shape) aShape.clone());
        }

        _originLabelPoints = new ArrayList<Graphic>(_labelPoints);
        _originChartPoints = new ArrayList<Graphic>(_chartPoints);
        _projected = true;
    }

    /**
     * Get origin data
     */
    public void getOriginData() {
        _attributeTable = (AttributeTable) _originAttributeTable.clone();
        _shapeList = new ArrayList<Shape>();
        for (Shape aShape : _originShapes) {
            _shapeList.add((Shape) aShape.clone());
        }

        _labelPoints = _originLabelPoints;
        _chartPoints = _originChartPoints;
        updateExtent();
    }

    // </editor-fold>
    // <editor-fold desc="Convert">
    /**
     * Save as KML (Google Earth data format) file
     *
     * @param fileName KML file name
     */
    public void saveAsKMLFile(String fileName) {
        switch (getShapeType()) {
            case Polygon:
            case PolygonM:
                saveAsKMLFile_Polygon(fileName);
                break;
            case Polyline:
            case PolylineM:
            case PolylineZ:
                saveAsKMLFile_Polyline(fileName);
                break;
            case Point:
            case PointM:
            case PointZ:
                saveAsKMLFile_Point(fileName);
                break;
        }
    }

    /**
     * Save as KML (Google Earth data format) file - Polygon
     *
     * @param fileName KML file name
     */
    private void saveAsKMLFile_Polygon(String fileName) {
        try {
            // Create XML text file
            SAXTransformerFactory fac = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
            TransformerHandler handler = fac.newTransformerHandler();
            Transformer transformer = handler.getTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            OutputStream outStream = new FileOutputStream(fileName);
            Result resultxml = new StreamResult(outStream);
            handler.setResult(resultxml);
            AttributesImpl atts = new AttributesImpl();

            //Write KML
            handler.startDocument();
            atts.addAttribute("", "", "xmlns", String.class.getName(), "http://www.opengis.net/kml/2.2");
            handler.startElement("", "", "kml", atts);
            atts.clear();
            handler.startElement("", "", "Document", atts);
            handler.startElement("", "", "Name", atts);
            handler.characters(fileName.toCharArray(), 0, fileName.length());
            handler.endElement("", "", "Name");    //Name

            //Write styles
            int styleNum = 0;
            String str;
            for (ColorBreak cb : this.getLegendScheme().getLegendBreaks()) {
                //StyleMap
                atts.addAttribute("", "", "id", "", "pg" + String.valueOf(styleNum));
                handler.startElement("", "", "StyleMap", atts);
                atts.clear();
                handler.startElement("", "", "Pair", atts);
                handler.startElement("", "", "key", atts);
                str = "normal";
                handler.characters(str.toCharArray(), 0, str.length());
                handler.endElement("", "", "key");    //key
                handler.startElement("", "", "styleUrl", atts);
                str = "#pgn" + String.valueOf(styleNum);
                handler.characters(str.toCharArray(), 0, str.length());
                handler.endElement("", "", "styleUrl");    //styleUrl
                handler.endElement("", "", "Pair");    //Pair   

                handler.startElement("", "", "Pair", atts);
                handler.startElement("", "", "key", atts);
                str = "highlight";
                handler.characters(str.toCharArray(), 0, str.length());
                handler.endElement("", "", "key");    //key
                handler.startElement("", "", "styleUrl", atts);
                str = "#pgn";
                handler.characters(str.toCharArray(), 0, str.length());
                handler.endElement("", "", "styleUrl");    //styleUrl
                handler.endElement("", "", "Pair");    //Pair  
                handler.endElement("", "", "StyleMap");    //StyleMap

                //Normal style
                PolygonBreak pgb = (PolygonBreak) cb;
                atts.addAttribute("", "", "id", "", "pgn" + String.valueOf(styleNum));
                handler.startElement("", "", "Style", atts);
                atts.clear();
                handler.startElement("", "", "LineStyle", atts);
                handler.startElement("", "", "color", atts);
                str = ColorUtil.toKMLColor(pgb.getOutlineColor());
                handler.characters(str.toCharArray(), 0, str.length());
                handler.endElement("", "", "color");    //color
                handler.endElement("", "", "LineStyle");    //LineStyle
                handler.startElement("", "", "PolyStyle", atts);
                handler.startElement("", "", "color", atts);
                str = ColorUtil.toKMLColor(pgb.getColor());
                handler.characters(str.toCharArray(), 0, str.length());
                handler.endElement("", "", "color");    //color
                handler.startElement("", "", "fill", atts);
                str = "1";
                if (!pgb.getDrawFill()) {
                    str = "0";
                }
                handler.characters(str.toCharArray(), 0, str.length());
                handler.endElement("", "", "fill");    //fill
                handler.endElement("", "", "PolyStyle");    //PolyStyle
                handler.endElement("", "", "Style");    //Style

                styleNum += 1;
            }

            // Highlight style - shared by all elements
            atts.addAttribute("", "", "id", "", "pgh");
            handler.startElement("", "", "Style", atts);
            atts.clear();
            handler.startElement("", "", "LineStyle", atts);
            handler.startElement("", "", "color", atts);
            str = "00000000";
            handler.characters(str.toCharArray(), 0, str.length());
            handler.endElement("", "", "color");    //color
            handler.endElement("", "", "LineStyle");    //LineStyle
            handler.startElement("", "", "PolyStyle", atts);
            handler.startElement("", "", "color", atts);
            str = "a0ff00ff";
            handler.characters(str.toCharArray(), 0, str.length());
            handler.endElement("", "", "color");    //color
            handler.startElement("", "", "fill", atts);
            str = "1";
            handler.characters(str.toCharArray(), 0, str.length());
            handler.endElement("", "", "fill");    //fill
            handler.endElement("", "", "PolyStyle");    //PolyStyle
            handler.endElement("", "", "Style");    //Style

            //Write shape coordinates
            handler.startElement("", "", "Folder", atts);
            handler.startElement("", "", "name", atts);
            str = fileName;
            handler.characters(str.toCharArray(), 0, str.length());
            handler.endElement("", "", "name");    //name
            handler.startElement("", "", "description", atts);
            str = "Generated using MeteoInfo";
            handler.characters(str.toCharArray(), 0, str.length());
            handler.endElement("", "", "description");    //description

            boolean hasSelShape = this.hasSelectedShapes();
            for (Shape shp : _shapeList) {
                PolygonShape pgs = (PolygonShape) shp;
                if (hasSelShape) {
                    if (!pgs.isSelected()) {
                        continue;
                    }
                }
                double currentLevel = pgs.lowValue;
                int levelNum = pgs.getLegendIndex();

                for (Polygon polygon : pgs.getPolygons()) {
                    handler.startElement("", "", "Placemark", atts);
                    handler.startElement("", "", "name", atts);
                    str = "Level " + String.valueOf(currentLevel);
                    handler.characters(str.toCharArray(), 0, str.length());
                    handler.endElement("", "", "name");    //name
                    handler.startElement("", "", "description", atts);
                    str = "Level " + String.valueOf(currentLevel);
                    handler.characters(str.toCharArray(), 0, str.length());
                    handler.endElement("", "", "description");    //description
                    handler.startElement("", "", "styleUrl", atts);
                    str = "#pg" + String.valueOf(levelNum);
                    handler.characters(str.toCharArray(), 0, str.length());
                    handler.endElement("", "", "styleUrl");
                    handler.startElement("", "", "Polygon", atts);
                    handler.startElement("", "", "outerBoundaryIs", atts);
                    handler.startElement("", "", "LinearRing", atts);
                    handler.startElement("", "", "coordinates", atts);
                    for (PointD point : polygon.getOutLine()) {
                        str = String.valueOf(point.X) + "," + String.valueOf(point.Y) + " ";
                        handler.characters(str.toCharArray(), 0, str.length());
                    }
                    handler.endElement("", "", "coordinates");    //coordinates
                    handler.endElement("", "", "LinearRing");    //LinearRing
                    handler.endElement("", "", "outerBoundaryIs");    //outerBoundaryIs

                    // If Fill=true then add innerBoundaryIs for the contour 'holes'
                    if (((PolygonBreak) this.getLegendScheme().getLegendBreaks().get(levelNum)).getDrawFill()) {
                        if (polygon.hasHole()) {
                            for (List<PointD> hole : polygon.getHoleLines()) {
                                handler.startElement("", "", "innerBoundaryIs", atts);
                                handler.startElement("", "", "LinearRing", atts);
                                handler.startElement("", "", "coordinates", atts);
                                for (PointD point : hole) {
                                    str = String.valueOf(point.X) + "," + String.valueOf(point.Y) + " ";
                                    handler.characters(str.toCharArray(), 0, str.length());
                                }
                                handler.endElement("", "", "coordinates");    //coordinates
                                handler.endElement("", "", "LinearRing");    //LinearRing
                                handler.endElement("", "", "innerBoundaryIs");    //innerBoundaryIs
                            }
                        }
                    }

                    handler.endElement("", "", "Polygon");    //Polygon
                    handler.endElement("", "", "Placemark");    //Placemark
                }
            }

            handler.endElement("", "", "Folder");    //Folder
            handler.endElement("", "", "Document");    //Document
            handler.endElement("", "", "kml");    //kml

            //End the document
            handler.endDocument();

            //Close
            outStream.close();

        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Save as KML (Google Earth data format) file - Polyline
     *
     * @param fileName KML file name
     */
    private void saveAsKMLFile_Polyline(String fileName) {
        try {
            // Create XML text file
            SAXTransformerFactory fac = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
            TransformerHandler handler = fac.newTransformerHandler();
            Transformer transformer = handler.getTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            OutputStream outStream = new FileOutputStream(fileName);
            Result resultxml = new StreamResult(outStream);
            handler.setResult(resultxml);
            AttributesImpl atts = new AttributesImpl();

            //Write KML
            handler.startDocument();
            atts.addAttribute("", "", "xmlns", String.class.getName(), "http://www.opengis.net/kml/2.2");
            handler.startElement("", "", "kml", atts);
            atts.clear();
            handler.startElement("", "", "Document", atts);
            handler.startElement("", "", "Name", atts);
            handler.characters(fileName.toCharArray(), 0, fileName.length());
            handler.endElement("", "", "Name");    //Name

            //Write styles
            int styleNum = 0;
            String str;
            for (ColorBreak cb : this.getLegendScheme().getLegendBreaks()) {
                //StyleMap
                atts.addAttribute("", "", "id", "", "pg" + String.valueOf(styleNum));
                handler.startElement("", "", "StyleMap", atts);
                atts.clear();
                handler.startElement("", "", "Pair", atts);
                handler.startElement("", "", "key", atts);
                str = "normal";
                handler.characters(str.toCharArray(), 0, str.length());
                handler.endElement("", "", "key");    //key
                handler.startElement("", "", "styleUrl", atts);
                str = "#pgn" + String.valueOf(styleNum);
                handler.characters(str.toCharArray(), 0, str.length());
                handler.endElement("", "", "styleUrl");    //styleUrl
                handler.endElement("", "", "Pair");    //Pair   

                handler.startElement("", "", "Pair", atts);
                handler.startElement("", "", "key", atts);
                str = "highlight";
                handler.characters(str.toCharArray(), 0, str.length());
                handler.endElement("", "", "key");    //key
                handler.startElement("", "", "styleUrl", atts);
                str = "#pgn";
                handler.characters(str.toCharArray(), 0, str.length());
                handler.endElement("", "", "styleUrl");    //styleUrl
                handler.endElement("", "", "Pair");    //Pair  
                handler.endElement("", "", "StyleMap");    //StyleMap

                //Normal style
                PolylineBreak pgb = (PolylineBreak) cb;
                atts.addAttribute("", "", "id", "", "pgn" + String.valueOf(styleNum));
                handler.startElement("", "", "Style", atts);
                atts.clear();
                handler.startElement("", "", "LineStyle", atts);
                handler.startElement("", "", "color", atts);
                str = ColorUtil.toKMLColor(pgb.getColor());
                handler.characters(str.toCharArray(), 0, str.length());
                handler.endElement("", "", "color");    //color
                handler.endElement("", "", "LineStyle");    //LineStyle                
                handler.endElement("", "", "Style");    //Style

                styleNum += 1;
            }

            // Highlight style - shared by all elements
            atts.addAttribute("", "", "id", "", "pgh");
            handler.startElement("", "", "Style", atts);
            atts.clear();
            handler.startElement("", "", "LineStyle", atts);
            handler.startElement("", "", "color", atts);
            str = "00000000";
            handler.characters(str.toCharArray(), 0, str.length());
            handler.endElement("", "", "color");    //color
            handler.endElement("", "", "LineStyle");    //LineStyle            
            handler.endElement("", "", "Style");    //Style

            //Write shape coordinates
            handler.startElement("", "", "Folder", atts);
            handler.startElement("", "", "name", atts);
            str = fileName;
            handler.characters(str.toCharArray(), 0, str.length());
            handler.endElement("", "", "name");    //name
            handler.startElement("", "", "description", atts);
            str = "Generated using MeteoInfo";
            handler.characters(str.toCharArray(), 0, str.length());
            handler.endElement("", "", "description");    //description

            boolean hasSelShape = this.hasSelectedShapes();
            for (Shape shp : _shapeList) {
                PolylineShape pgs = (PolylineShape) shp;
                if (hasSelShape) {
                    if (!pgs.isSelected()) {
                        continue;
                    }
                }
                double currentLevel = pgs.value;
                int levelNum = pgs.getLegendIndex();
                int i = 0;
                for (Polyline line : pgs.getPolylines()) {
                    handler.startElement("", "", "Placemark", atts);
                    handler.startElement("", "", "name", atts);
                    str = "Level " + String.valueOf(currentLevel);
                    handler.characters(str.toCharArray(), 0, str.length());
                    handler.endElement("", "", "name");    //name
                    handler.startElement("", "", "description", atts);
                    str = "Level " + String.valueOf(currentLevel);
                    handler.characters(str.toCharArray(), 0, str.length());
                    handler.endElement("", "", "description");    //description
                    handler.startElement("", "", "styleUrl", atts);
                    str = "#pg" + String.valueOf(levelNum);
                    handler.characters(str.toCharArray(), 0, str.length());
                    handler.endElement("", "", "styleUrl");    //styleUrl
                    handler.startElement("", "", "LineString", atts);
                    handler.startElement("", "", "coordinates", atts);
                    for (PointD point : line.getPointList()) {
                        str = String.valueOf(point.X) + "," + String.valueOf(point.Y);
                        if (this.getShapeType() == ShapeTypes.PolylineZ) {
                            str = str + "," + String.valueOf(((PolylineZShape) shp).getZArray()[i]);
                        }
                        str = str + " ";
                        handler.characters(str.toCharArray(), 0, str.length());
                        i += 1;
                    }
                    handler.endElement("", "", "coordinates");    //coordinates                    
                    handler.endElement("", "", "LineString");    //LineString
                    handler.endElement("", "", "Placemark");    //Placemark
                }
            }

            handler.endElement("", "", "Folder");    //Folder
            handler.endElement("", "", "Document");    //Document
            handler.endElement("", "", "kml");    //kml

            //End the document
            handler.endDocument();

            //Close
            outStream.close();

        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save as KML (Google Earth data format) file - Point
     *
     * @param fileName KML file name
     */
    private void saveAsKMLFile_Point(String fileName) {
        try {
            // Create XML text file
            SAXTransformerFactory fac = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
            TransformerHandler handler = fac.newTransformerHandler();
            Transformer transformer = handler.getTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            OutputStream outStream = new FileOutputStream(fileName);
            Result resultxml = new StreamResult(outStream);
            handler.setResult(resultxml);
            AttributesImpl atts = new AttributesImpl();

            //Write KML
            handler.startDocument();
            atts.addAttribute("", "", "xmlns", String.class.getName(), "http://www.opengis.net/kml/2.2");
            handler.startElement("", "", "kml", atts);
            atts.clear();
            handler.startElement("", "", "Document", atts);
            handler.startElement("", "", "Name", atts);
            handler.characters(fileName.toCharArray(), 0, fileName.length());
            handler.endElement("", "", "Name");    //Name

            //Write styles
            int styleNum = 0;
            String str;
            for (ColorBreak cb : this.getLegendScheme().getLegendBreaks()) {
                //StyleMap
                atts.addAttribute("", "", "id", "", "pg" + String.valueOf(styleNum));
                handler.startElement("", "", "StyleMap", atts);
                atts.clear();
                handler.startElement("", "", "Pair", atts);
                handler.startElement("", "", "key", atts);
                str = "normal";
                handler.characters(str.toCharArray(), 0, str.length());
                handler.endElement("", "", "key");    //key
                handler.startElement("", "", "styleUrl", atts);
                str = "#pgn" + String.valueOf(styleNum);
                handler.characters(str.toCharArray(), 0, str.length());
                handler.endElement("", "", "styleUrl");    //styleUrl
                handler.endElement("", "", "Pair");    //Pair   

                handler.startElement("", "", "Pair", atts);
                handler.startElement("", "", "key", atts);
                str = "highlight";
                handler.characters(str.toCharArray(), 0, str.length());
                handler.endElement("", "", "key");    //key
                handler.startElement("", "", "styleUrl", atts);
                str = "#pgn";
                handler.characters(str.toCharArray(), 0, str.length());
                handler.endElement("", "", "styleUrl");    //styleUrl
                handler.endElement("", "", "Pair");    //Pair  
                handler.endElement("", "", "StyleMap");    //StyleMap

                //Normal style
                PointBreak pgb = (PointBreak) cb;
                atts.addAttribute("", "", "id", "", "pgn" + String.valueOf(styleNum));
                handler.startElement("", "", "Style", atts);
                atts.clear();
                handler.startElement("", "", "BalloonStyle", atts);
                handler.startElement("", "", "bgColor", atts);
                str = ColorUtil.toKMLColor(pgb.getColor());
                handler.characters(str.toCharArray(), 0, str.length());
                handler.endElement("", "", "color");    //color
                handler.endElement("", "", "BalloonStyle");    //BalloonStyle                
                handler.endElement("", "", "Style");    //Style

                styleNum += 1;
            }

            // Highlight style - shared by all elements
            atts.addAttribute("", "", "id", "", "pgh");
            handler.startElement("", "", "Style", atts);
            atts.clear();
            handler.startElement("", "", "BalloonStyle", atts);
            handler.startElement("", "", "color", atts);
            str = "00000000";
            handler.characters(str.toCharArray(), 0, str.length());
            handler.endElement("", "", "color");    //color
            handler.endElement("", "", "BalloonStyle");    //BalloonStyle            
            handler.endElement("", "", "Style");    //Style

            //Write shape coordinates
            handler.startElement("", "", "Folder", atts);
            handler.startElement("", "", "name", atts);
            str = fileName;
            handler.characters(str.toCharArray(), 0, str.length());
            handler.endElement("", "", "name");    //name
            handler.startElement("", "", "description", atts);
            str = "Generated using MeteoInfo";
            handler.characters(str.toCharArray(), 0, str.length());
            handler.endElement("", "", "description");    //description

            boolean hasSelShape = this.hasSelectedShapes();
            int shapIdx = 0;
            for (Shape shp : _shapeList) {
                PointShape pgs = (PointShape) shp;
                if (hasSelShape) {
                    if (!pgs.isSelected()) {
                        shapIdx += 1;
                        continue;
                    }
                }
                double currentLevel = pgs.getValue();
                int levelNum = pgs.getLegendIndex();

                handler.startElement("", "", "Placemark", atts);
                if (this.getLabelSet().isDrawLabels()) {
                    handler.startElement("", "", "name", atts);
                    str = this.getCellValue(this.getLabelSet().getFieldName(), shapIdx).toString();
                    handler.characters(str.toCharArray(), 0, str.length());
                    handler.endElement("", "", "name");    //name
                }
                handler.startElement("", "", "description", atts);
                str = "Level " + String.valueOf(currentLevel);
                handler.characters(str.toCharArray(), 0, str.length());
                handler.endElement("", "", "description");    //description
                handler.startElement("", "", "styleUrl", atts);
                str = "#pg" + String.valueOf(levelNum);
                handler.characters(str.toCharArray(), 0, str.length());
                handler.endElement("", "", "styleUrl");    //sytleUrl
                handler.startElement("", "", "Point", atts);
                handler.startElement("", "", "coordinates", atts);
                str = String.valueOf(pgs.getPoint().X) + "," + String.valueOf(pgs.getPoint().Y);
                if (this.getShapeType() == ShapeTypes.PointZ) {
                    str = str + "," + String.valueOf(((PointZShape) shp).Z);
                }
                handler.characters(str.toCharArray(), 0, str.length());
                handler.endElement("", "", "coordinates");    //coordinates                    
                handler.endElement("", "", "Point");    //Point
                handler.endElement("", "", "Placemark");    //Placemark
            }

            handler.endElement("", "", "Folder");    //Folder
            handler.endElement("", "", "Document");    //Document
            handler.endElement("", "", "kml");    //kml

            //End the document
            handler.endDocument();

            //Close
            outStream.close();

        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // </editor-fold>

    // <editor-fold desc="Other">
    /**
     * Update extent
     */
    public void updateExtent() {
        for (int i = 0; i < _shapeList.size(); i++) {
            if (i == 0) {
                this.setExtent((Extent) _shapeList.get(i).getExtent().clone());
            } else {
                this.setExtent(MIMath.getLagerExtent(this.getExtent(), _shapeList.get(i).getExtent()));
            }
        }
    }

    /**
     * Update legend scheme
     *
     * @param aLT Legend type
     * @param fieldName Field name
     */
    public void updateLegendScheme(LegendType aLT, String fieldName) {
        this.setLegendScheme(createLegendScheme(aLT, fieldName));
    }

    /**
     * Update legend scheme -> update legend indexes of the shapes
     */
    public void updateLegendIndexes() {
        LegendScheme ls = this.getLegendScheme();
        switch (ls.getLegendType()) {
            case UniqueValue:
                int shapeIdx = 0;
                if (this.getField(ls.getFieldName()).isNumeric()) {
                    for (Shape aShape : this.getShapes()) {
                        String vStr = this.getCellValue(ls.getFieldName(), shapeIdx).toString();
                        aShape.setLegendIndex(-1);                    
                        for (int i = 0; i < ls.getBreakNum(); i++) {
                            if (MIMath.doubleEquals(Double.parseDouble(ls.getLegendBreaks().get(i).getStartValue().toString()),
                                    Double.parseDouble(vStr))) {
                                aShape.setLegendIndex(i);
                            }
                        }
                        shapeIdx += 1;
                    }
                } else {
                    for (Shape aShape : this.getShapes()) {
                        String vStr = this.getCellValue(ls.getFieldName(), shapeIdx).toString();
                        aShape.setLegendIndex(-1);                    
                        for (int i = 0; i < ls.getBreakNum(); i++) {
                            if (vStr.equals(ls.getLegendBreaks().get(i).getStartValue().toString())) {
                                aShape.setLegendIndex(i);
                            }
                        }
                        shapeIdx += 1;
                    }
                }
                break;
            case GraduatedColor:
                shapeIdx = 0;
                for (Shape aShape : this.getShapes()) {
                    aShape.setLegendIndex(-1);
                    String vStr = this.getCellValue(ls.getFieldName(), shapeIdx).toString();
                    double v = Double.parseDouble(vStr);
                    int blNum = 0;
                    for (int i = 0; i < ls.getBreakNum(); i++) {
                        ColorBreak cb = ls.getLegendBreaks().get(i);
                        blNum += 1;
                        if (MIMath.doubleEquals(v, Double.parseDouble(cb.getStartValue().toString()))
                                || (v > Double.parseDouble(cb.getStartValue().toString())
                                && v < Double.parseDouble(cb.getEndValue().toString()))
                                || (blNum == ls.getBreakNum() && v == Double.parseDouble(cb.getEndValue().toString()))) {
                            aShape.setLegendIndex(i);
                        }
                    }
                    shapeIdx += 1;
                }
                break;
            default:
                for (Shape aShape : this.getShapes()) {
                    aShape.setLegendIndex(0);
                }
                break;
        }
    }

    /**
     * Create legend scheme
     *
     * @param aLT Legend type
     * @param fieldName Field name
     * @return Legend scheme
     */
    public LegendScheme createLegendScheme(LegendType aLT, String fieldName) {
        double min, max;
        ShapeTypes aST = this.getShapeType();
        LegendScheme aLS = new LegendScheme(this.getShapeType());

        min = aLS.getMinValue();
        max = aLS.getMaxValue();
        switch (aLT) {
            case SingleSymbol:
                Color aColor = Color.black;
                float size = 1.0F;
                switch (aST) {
                    case Point:
                    case PointM:
                    case PointZ:
                        aColor = Color.black;
                        size = 5;
                        break;
                    case Polyline:
                    case PolylineM:
                    case PolylineZ:
                        aColor = Color.black;
                        break;
                    case Polygon:
                    case PolygonM:
                    case Image:
                        aColor = new Color(255, 251, 195);
                        break;
                }

                aLS = LegendManage.createSingleSymbolLegendScheme(aST, aColor, size);
                break;
            case UniqueValue:
                Color[] colors;
                List<String> valueList = new ArrayList<String>();
                boolean isDateField = false;
                DataTypes colType = this.getAttributeTable().getTable().getColumns().get(fieldName).getDataType();
                if (colType == DataTypes.Date) {
                    isDateField = true;
                }

                List<String> captions = new ArrayList<String>();
                SimpleDateFormat format = new SimpleDateFormat("yyyy/M/d");

                for (int i = 0; i < this.getAttributeTable().getTable().getRows().size(); i++) {
                    Object value = this.getAttributeTable().getTable().getRows().get(i).getValue(fieldName);
                    if (!valueList.contains(value.toString())) {
                        valueList.add(value.toString());
                        if (isDateField) {
                            captions.add(format.format((Date) value));
                        }
                    }
                }

//                if (valueList.size() == 1) {
//                    JOptionPane.showMessageDialog(null, "The values of all shapes are same!");
//                    break;
//                }

                if (valueList.size() <= 13) {
                    colors = LegendManage.createRainBowColors(valueList.size());
                } else {
                    colors = LegendManage.createRandomColors(valueList.size());
                }
                Color[] newcolors = new Color[colors.length + 1];
                newcolors[0] = Color.white;
                for (int i = 1; i < newcolors.length; i++) {
                    newcolors[i] = colors[i - 1];
                }

                if (isDateField) {
                    aLS = LegendManage.createUniqValueLegendScheme(valueList, captions, newcolors, aST, min,
                            max, aLS.getHasNoData(), aLS.getUndefValue());
                } else {
                    aLS = LegendManage.createUniqValueLegendScheme(valueList, newcolors,
                            aST, min, max, aLS.getHasNoData(), aLS.getUndefValue());
                }

                aLS.setFieldName(fieldName);
                break;
            case GraduatedColor:
                double[] S = new double[this.getAttributeTable().getTable().getRows().size()];
                for (int i = 0; i < S.length; i++) {
                    S[i] = Double.parseDouble(this.getAttributeTable().getTable().getRows().get(i).getValue(fieldName).toString());
                }
                double[] minmax = MIMath.getMinMaxValue(S, aLS.getUndefValue());
                min = minmax[0];
                max = minmax[1];

                if (min == max) {
                    JOptionPane.showMessageDialog(null, "The values of all shapes are same!");
                    break;
                }

                double[] CValues;
                CValues = LegendManage.createContourValues(min, max);
                colors = LegendManage.createRainBowColors(CValues.length + 1);

                aLS = LegendManage.createGraduatedLegendScheme(CValues, colors,
                        aST, min, max, aLS.getHasNoData(), aLS.getUndefValue());
                aLS.setFieldName(fieldName);
                break;
        }

        return aLS;
    }

    /**
     * Clone VectorLayer object
     *
     * @return VectorLayer object
     */
    @Override
    public Object clone() {
        VectorLayer aLayer = new VectorLayer(this.getShapeType());
        aLayer.setExtent((Extent) this.getExtent().clone());
        aLayer.setFileName(this.getFileName());
        aLayer.setHandle(this.getHandle());
        aLayer.setLayerName(this.getLayerName());
        aLayer.setProjInfo(this.getProjInfo());
        aLayer.setLegendScheme((LegendScheme) this.getLegendScheme().clone());
        if (_projected) {
            for (int i = 0; i < _originShapes.size(); i++) {
                aLayer.addShape((Shape) _originShapes.get(i).clone());
            }
        } else {
            for (int i = 0; i < _shapeList.size(); i++) {
                aLayer.addShape((Shape) _shapeList.get(i).clone());
            }
        }
        aLayer.setTransparency(this.getTransparency());
        aLayer.setLayerDrawType(this.getLayerDrawType());
        aLayer.setVisible(this.isVisible());
        aLayer.setLabelSet(_labelSet);
        aLayer.setExpanded(this.isExpanded());
        aLayer.setAvoidCollision(this._avoidCollision);
        aLayer.setMaskout(this.isMaskout());
        aLayer.setTag(this.getTag());

        if (_projected) {
            aLayer.setAttributeTable((AttributeTable) _originAttributeTable.clone());
        } else {
            aLayer.setAttributeTable((AttributeTable) _attributeTable.clone());
        }

        return aLayer;
    }

    /**
     * Clone VectorLayer object - without attribute table
     *
     * @return
     */
    public Object cloneShapes() {
        VectorLayer aLayer = new VectorLayer(this.getShapeType());
        aLayer.setExtent((Extent) this.getExtent().clone());
        //aLayer.setFileName(this.getFileName());
        //aLayer.setHandle(this.getHandle());
        aLayer.setLayerName(this.getLayerName());
        aLayer.setProjInfo(this.getProjInfo());
        aLayer.setLegendScheme((LegendScheme) this.getLegendScheme().clone());
        if (_projected) {
            for (int i = 0; i < _originShapes.size(); i++) {
                aLayer.addShape((Shape) _originShapes.get(i).clone());
            }
        } else {
            for (int i = 0; i < _shapeList.size(); i++) {
                aLayer.addShape((Shape) _shapeList.get(i).clone());
            }
        }
        aLayer.setTransparency(this.getTransparency());
        aLayer.setLayerDrawType(this.getLayerDrawType());
        aLayer.setVisible(this.isVisible());
        aLayer.setLabelSet(_labelSet);
        aLayer.setExpanded(this.isExpanded());
        aLayer.setAvoidCollision(this._avoidCollision);
        aLayer.setMaskout(this.isMaskout());
        //aLayer.setTag(this.getTag());

        return aLayer;
    }

    /**
     * Clone VectorLayer object - only parameters
     *
     * @return
     */
    public Object cloneValue() {
        VectorLayer aLayer = new VectorLayer(this.getShapeType());
        //aLayer.setExtent((Extent) this.getExtent().clone());
        //aLayer.setFileName(this.getFileName());
        //aLayer.setHandle(this.getHandle());
        aLayer.setLayerName(this.getLayerName());
        aLayer.setProjInfo(this.getProjInfo());
        //aLayer.setLegendScheme((LegendScheme) this.getLegendScheme().clone());        
        //aLayer.setTransparency(this.getTransparency());
        aLayer.setLayerDrawType(this.getLayerDrawType());
        aLayer.setVisible(this.isVisible());
        aLayer.setLabelSet(_labelSet);
        aLayer.setExpanded(this.isExpanded());
        aLayer.setAvoidCollision(this._avoidCollision);
        aLayer.setMaskout(this.isMaskout());
        //aLayer.setTag(this.getTag());

        return aLayer;
    }
    // </editor-fold>
    // </editor-fold>
}