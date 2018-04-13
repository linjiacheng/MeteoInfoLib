/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.axis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.meteoinfo.chart.ChartText;
import org.meteoinfo.chart.Location;
import org.meteoinfo.chart.plot.AbstractPlot2D;
import org.meteoinfo.chart.plot.XAlign;
import org.meteoinfo.chart.plot.YAlign;
import org.meteoinfo.drawing.Draw;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.util.BigDecimalUtil;
import org.meteoinfo.global.util.DateUtil;
import org.meteoinfo.legend.LineStyles;

/**
 *
 * @author yaqiang
 */
public class Axis implements Cloneable {

    // <editor-fold desc="Variables">
    private boolean xAxis;
    private Location location;
    private ChartText label;
    private boolean visible;
    private boolean drawTickLine;
    private boolean drawTickLabel;
    private boolean drawLabel;
    private Color lineColor;
    private float lineWidth;
    private LineStyles lineStyle;
    //private Stroke lineStroke;
    private Color tickColor;
    private Stroke tickStroke;
    private int tickLength;
    private boolean insideTick;
    private Font tickLabelFont;
    private Color tickLabelColor;
    private float tickLabelAngle;
    private int tickLabelGap;
    private double tickStartValue;
    private double tickDeltaValue;
    private double minValue;
    private double maxValue;
    private double[] tickValues;
    //private boolean timeAxis;
    //private String timeFormat;
    //private TimeUnit timeUnit;
    private boolean inverse;
    private float shift;
    private List<Double> tickLocations;
    //private List<String> tickLabels;
    private List<ChartText> tickLabels;
    private boolean autoTick;
    private boolean minorTickVisible;
    private int minorTickNum;
    private int tickSpace;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public Axis() {
        this.xAxis = true;
        this.label = null;
        this.visible = true;
        this.drawTickLine = true;
        this.drawTickLabel = true;
        this.drawLabel = false;
        this.lineColor = Color.darkGray;
        this.lineWidth = 1.0f;
        this.lineStyle = LineStyles.SOLID;
        //this.lineStroke = new BasicStroke(1.0f);
        this.tickColor = Color.darkGray;
        this.tickStroke = new BasicStroke(1.0f);
        this.tickLength = 5;
        this.insideTick = true;
        this.tickLabelFont = new Font("Arial", Font.PLAIN, 14);
        this.tickLabelColor = Color.darkGray;
        this.tickLabelAngle = 0;
        this.tickLabelGap = 1;
        this.minValue = 0;
        this.maxValue = 1;
        this.updateTickValues();
        //this.timeAxis = false;
        //this.timeFormat = "yyyy-MM-dd";
        //this.timeUnit = TimeUnit.DAY;
        this.inverse = false;
        this.shift = 0;
        this.tickLocations = new ArrayList<>();
        this.tickLabels = new ArrayList<>();
        this.autoTick = true;
        this.minorTickVisible = false;
        this.minorTickNum = 5;
        this.tickSpace = 5;
    }

    /**
     * Constructor
     *
     * @param label Axis label
     */
    public Axis(ChartText label) {
        this();
        this.label = label;
    }

    /**
     * Constructor
     *
     * @param label Axis label
     */
    public Axis(String label) {
        this();
        this.label = new ChartText(label);
    }

    /**
     * Constructor
     *
     * @param label Axis label
     * @param xAxis If is x axis
     */
    public Axis(String label, boolean xAxis) {
        this(label);
        this.xAxis = xAxis;
        if (this.xAxis) {
            this.location = Location.BOTTOM;
        } else {
            this.location = Location.LEFT;
        }
    }

    /**
     * Constructor
     *
     * @param label Axis label
     * @param xAxis If is x axis
     */
    public Axis(ChartText label, boolean xAxis) {
        this(label);
        this.xAxis = xAxis;
        if (this.xAxis) {
            this.location = Location.BOTTOM;
        } else {
            this.location = Location.LEFT;
        }
    }

    /**
     * Constructor
     *
     * @param label Axis label
     * @param xAxis If is x axis
     * @param loc Location
     */
    public Axis(String label, boolean xAxis, Location loc) {
        this(label);
        this.xAxis = xAxis;
        this.location = loc;
    }

    /**
     * Constructor
     *
     * @param label Axis label
     * @param xAxis If is x axis
     * @param loc Location
     * @param drawTickLabel If draw tick label
     */
    public Axis(String label, boolean xAxis, Location loc, boolean drawTickLabel) {
        this(label);
        this.xAxis = xAxis;
        this.location = loc;
        this.drawTickLabel = drawTickLabel;
    }

    /**
     * Constructor
     *
     * @param axis Axis
     */
    public Axis(Axis axis) {
        this(axis.getLabel(), axis.isXAxis());
        this.setAutoTick(axis.isAutoTick());
        this.setDrawLabel(axis.isDrawLabel());
        this.setDrawTickLabel(axis.isDrawTickLabel());
        this.setDrawTickLine(axis.isDrawTickLine());
        this.setInsideTick(axis.isInsideTick());
        this.setInverse(axis.isInverse());
        this.setLabelColor(axis.getLabelColor());
        this.setLineWidth(axis.getLineWidth());
        this.setLineStyle(axis.getLineStyle());
        //this.setLineStroke(axis.getLineStroke());
        this.setLocation(axis.getLocation());
        this.setMaxValue(axis.getMaxValue());
        this.setMinValue(axis.getMinValue());
        this.setMinorTickNum(axis.getMinorTickNum());
        this.setMinorTickVisible(axis.isMinorTickVisible());
        this.setShift(axis.getShift());
        this.setTickColor(axis.getTickColor());
        this.setTickDeltaValue(axis.getTickDeltaValue());
        this.setTickLabelColor(axis.getTickLabelColor());
        this.setTickLabelFont(axis.getTickLabelFont());
        this.setTickLength(axis.getTickLength());
        this.setVisible(axis.isVisible());
        
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get if is x axis
     *
     * @return Boolean
     */
    public boolean isXAxis() {
        return this.xAxis;
    }

    /**
     * Set if is x axis
     *
     * @param value Boolean
     */
    public void setXAxis(boolean value) {
        this.xAxis = value;
    }

    /**
     * Get location
     *
     * @return Location
     */
    public Location getLocation() {
        return this.location;
    }

    /**
     * Set location
     *
     * @param value Location
     */
    public void setLocation(Location value) {
        this.location = value;
    }

    /**
     * Get axis label
     *
     * @return Axis label
     */
    public ChartText getLabel() {
        return label;
    }

    /**
     * Set axis label
     *
     * @param value Axis label
     */
    public void setLabel(ChartText value) {
        label = value;
        if (label != null && (this.location == Location.BOTTOM || this.location == Location.LEFT)) {
            this.drawLabel = true;
        }
    }

    /**
     * Set axis label
     *
     * @param value Axis label
     */
    public void setLabel(String value) {
        ChartText text = new ChartText(value);
        setLabel(text);
    }

    /**
     * If is visible
     *
     * @return Boolean
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Set if is visible
     *
     * @param value Boolean
     */
    public void setVisible(boolean value) {
        visible = value;
    }

    /**
     * Get if draw tick lines
     *
     * @return Boolean
     */
    public boolean isDrawTickLine() {
        return this.drawTickLine;
    }

    /**
     * Set if draw tick lines
     *
     * @param value Boolean
     */
    public void setDrawTickLine(boolean value) {
        this.drawTickLine = value;
    }

    /**
     * Get is draw tick label
     *
     * @return Boolean
     */
    public boolean isDrawTickLabel() {
        return this.drawTickLabel;
    }

    /**
     * Set if draw tick label
     *
     * @param value Boolean
     */
    public void setDrawTickLabel(boolean value) {
        this.drawTickLabel = value;
    }

    /**
     * Get if draw label
     *
     * @return Boolean
     */
    public boolean isDrawLabel() {
        return this.drawLabel;
    }

    /**
     * Set if draw label
     *
     * @param value Boolean
     */
    public void setDrawLabel(boolean value) {
        this.drawLabel = value;
    }

    /**
     * Get line color
     *
     * @return Line color
     */
    public Color getLineColor() {
        return lineColor;
    }

    /**
     * Set line color
     *
     * @param value Line color
     */
    public void setLineColor(Color value) {
        lineColor = value;
    }

    /**
     * Get line width
     *
     * @return Line width
     */
    public float getLineWidth() {
        return this.lineWidth;
    }

    /**
     * Set line width
     *
     * @param value Line width
     */
    public void setLineWidth(float value) {
        this.lineWidth = value;
    }

    public LineStyles getLineStyle() {
        return this.lineStyle;
    }

    public void setLineStyle(LineStyles value) {
        this.lineStyle = value;
    }

    /**
     * Get line stroke
     *
     * @return Line stroke
     */
    public Stroke getLineStroke() {
        return new BasicStroke(this.lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10.0f, Draw.getDashPattern(lineStyle), 0.0f);
    }

//    /**
//     * Set line stroke
//     *
//     * @param value Line stroke
//     */
//    public void setLineStroke(Stroke value) {
//        lineStroke = value;
//    }
    /**
     * Get tick color
     *
     * @return Tick color
     */
    public Color getTickColor() {
        return tickColor;
    }

    /**
     * Set tick color
     *
     * @param value Tick color
     */
    public void setTickColor(Color value) {
        tickColor = value;
    }

    /**
     * Get tick stroke
     *
     * @return Tick stroke
     */
    public Stroke getTickStroke() {
        return tickStroke;
    }

    /**
     * Set tick stroke
     *
     * @param value Tick stroke
     */
    public void setTickStroke(Stroke value) {
        tickStroke = value;
    }

    /**
     * Get tick length
     *
     * @return Tick length
     */
    public int getTickLength() {
        return this.tickLength;
    }

    /**
     * Set tick length
     *
     * @param value Tick length
     */
    public void setTickLength(int value) {
        this.tickLength = value;
    }

    /**
     * Get if is inside tick
     *
     * @return Boolean
     */
    public boolean isInsideTick() {
        return this.insideTick;
    }

    /**
     * Set if is inside tick
     *
     * @param value Boolean
     */
    public void setInsideTick(boolean value) {
        this.insideTick = value;
    }

    /**
     * Get label font
     *
     * @return Label font
     */
    public Font getLabelFont() {
        return this.label.getFont();
    }

    /**
     * Set lable font
     *
     * @param value Lable font
     */
    public void setLabelFont(Font value) {
        this.label.setFont(value);
    }

    /**
     * Get label color
     *
     * @return Label color
     */
    public Color getLabelColor() {
        return this.label.getColor();
    }

    /**
     * Set label color
     *
     * @param value Label color
     */
    public void setLabelColor(Color value) {
        this.label.setColor(value);
    }

    /**
     * Get tick label font
     *
     * @return Tick label font
     */
    public Font getTickLabelFont() {
        return tickLabelFont;
    }

    /**
     * Set tick lable font
     *
     * @param value Tick lable font
     */
    public void setTickLabelFont(Font value) {
        tickLabelFont = value;
    }

    /**
     * Get tick label color
     *
     * @return Tick label color
     */
    public Color getTickLabelColor() {
        return tickLabelColor;
    }

    /**
     * Set tick label color
     *
     * @param value Tick label color
     */
    public void setTickLabelColor(Color value) {
        tickLabelColor = value;
    }

    /**
     * Get tick label angle
     *
     * @return Tick label angle
     */
    public float getTickLabelAngle() {
        return this.tickLabelAngle;
    }

    /**
     * Set tick label angle
     *
     * @param value Angle
     */
    public void setTickLabelAngle(float value) {
        this.tickLabelAngle = value;
    }

    /**
     * Get tick label gap
     *
     * @return Tick label gap
     */
    public int getTickLabelGap() {
        return this.tickLabelGap;
    }

    /**
     * Set tick label gap
     *
     * @param value Tick label gap
     */
    public void setTickLabelGap(int value) {
        this.tickLabelGap = value;
    }

    /**
     * Get tick start value
     *
     * @return Tick start value
     */
    public double getTickStartValue() {
        return this.tickStartValue;
    }

    /**
     * Set tick start value
     *
     * @param value Tick start value
     */
    public void setTickStartValue(double value) {
        this.tickStartValue = value;
    }

    /**
     * Get tick delta value
     *
     * @return Tick delta value
     */
    public double getTickDeltaValue() {
        return this.tickDeltaValue;
    }

    /**
     * Set tick delta value
     *
     * @param value Tick delta value
     */
    public void setTickDeltaValue(double value) {
        this.tickDeltaValue = value;
    }

    /**
     * Get minimum value
     *
     * @return Minimum value
     */
    public double getMinValue() {
        return this.minValue;
    }

    /**
     * Set minimum value
     *
     * @param value Minimum value
     */
    public void setMinValue(double value) {
        this.minValue = value;
    }

    /**
     * Get maximum value
     *
     * @return Maximum value
     */
    public double getMaxValue() {
        return this.maxValue;
    }

    /**
     * Set maximum value
     *
     * @param value Maximum value
     */
    public void setMaxValue(double value) {
        this.maxValue = value;
    }

    /**
     * Get tick values
     *
     * @return Tick values
     */
    public double[] getTickValues() {
        if (this.autoTick) {
            return this.tickValues;
        } else {
            List<Double> values = new ArrayList<>();
            for (double v : this.tickLocations) {
                if (v >= this.minValue && v <= this.maxValue) {
                    values.add(v);
                }
            }
            double[] vs = new double[values.size()];
            for (int i = 0; i < values.size(); i++) {
                vs[i] = values.get(i);
            }
            return vs;
        }
    }

    /**
     * Set tick values
     *
     * @param value Tick values
     */
    public void setTickValues(double[] value) {
        this.tickValues = value;
        if (value.length > 1) {
            this.tickDeltaValue = BigDecimalUtil.sub(value[1], value[0]);
        } else {
            this.tickDeltaValue = 0;
        }
    }

    /**
     * Set tick values
     *
     * @param value Tick value list
     */
    public void setTickValues(List<Double> value) {
        this.tickValues = new double[value.size()];
        for (int i = 0; i < value.size(); i++) {
            this.tickValues[i] = value.get(i);
        }
        if (value.size() > 1) {
            this.tickDeltaValue = BigDecimalUtil.sub(value.get(1), value.get(0));
        } else {
            this.tickDeltaValue = 0;
        }
    }

//    /**
//     * Get if is time axis
//     *
//     * @return Boolean
//     */
//    public boolean isTimeAxis() {
//        return this.timeAxis;
//    }
//
//    /**
//     * Set if is time axis
//     *
//     * @param value Boolean
//     */
//    public void setTimeAxis(boolean value) {
//        this.timeAxis = value;
//    }
//    /**
//     * Get time format
//     * @return Time format
//     */
//    public String getTimeFormat(){
//        return this.timeFormat;
//    }
//    
//    /**
//     * Set time format
//     * @param value 
//     */
//    public void setTimeFormat(String value){
//        this.timeFormat = value;
//    }
//    
//    /**
//     * Get time unit
//     * @return Time unit
//     */
//    public TimeUnit getTimeUnit(){
//        return this.timeUnit;
//    }
//    
//    /**
//     * Set time unit
//     * @param value Time unit
//     */
//    public void setTimeUnit(TimeUnit value){
//        this.timeUnit = value;
//    }
    /**
     * Get if is inverse
     *
     * @return Boolean
     */
    public boolean isInverse() {
        return this.inverse;
    }

    /**
     * Set if is inverse
     *
     * @param value Boolean
     */
    public void setInverse(boolean value) {
        this.inverse = value;
    }

    /**
     * Get shift
     *
     * @return Shift
     */
    public float getShift() {
        return this.shift;
    }

    /**
     * Set shift
     *
     * @param value Shift
     */
    public void setShift(float value) {
        this.shift = value;
    }

    /**
     * Tick locations
     *
     * @return Tick locations
     */
    public List<Double> getTickLocations() {
        return this.tickLocations;
    }

    /**
     * Set tick locations
     *
     * @param value Tick locations
     */
    public void setTickLocations(List<Number> value) {
        this.tickLocations.clear();
        this.tickLabels.clear();
        for (Number v : value) {
            this.tickLocations.add(v.doubleValue());
            this.tickLabels.add(new ChartText(String.valueOf(v)));
        }
        this.autoTick = false;
    }

    /**
     * Set tick locations
     *
     * @param value Tick locations
     */
    public void setTickLocations(double[] value) {
        this.tickLocations.clear();
        this.tickLabels.clear();
        String tick;
        for (double v : value) {
            this.tickLocations.add(v);
            tick = String.valueOf(v);
            tick = DataConvert.removeTailingZeros(tick);
            this.tickLabels.add(new ChartText(tick));
        }
        this.autoTick = false;
    }

    /**
     * Get tick labels
     *
     * @return Tick labels
     */
    public List<ChartText> getTickLabels() {
        return this.tickLabels;
    }

    /**
     * Get tick label text
     *
     * @return Tick label text
     */
    public List<String> getTickLabelText() {
        List<String> strs = new ArrayList<>();
        for (ChartText ct : this.tickLabels) {
            strs.add(ct.toString());
        }

        return strs;
    }

    /**
     * Set tick label text
     *
     * @param value Tick label text
     */
    public void setTickLabelText(List<String> value) {
        this.tickLabels = new ArrayList<>();
        for (String v : value) {
            this.tickLabels.add(new ChartText(v));
        }
        this.autoTick = false;
    }

    /**
     * Set tick labels.
     *
     * @param value Tick labels
     */
    public void setTickLabels(List<ChartText> value) {
        this.tickLabels = value;
    }

    /**
     * Set tick labels
     *
     * @param value Tick labels
     */
    public void setTickLabels_Number(List<Number> value) {
        this.tickLabels = new ArrayList<>();
        for (Number v : value) {
            this.tickLabels.add(new ChartText(v.toString()));
        }
        this.autoTick = false;
    }

    /**
     * Get if is auto tick labels
     *
     * @return Boolean
     */
    public boolean isAutoTick() {
        return this.autoTick;
    }

    /**
     * Set if auto tick labels
     *
     * @param value Boolean
     */
    public void setAutoTick(boolean value) {
        this.autoTick = value;
    }

    /**
     * Get if minor tick visible or not
     *
     * @return Boolean
     */
    public boolean isMinorTickVisible() {
        return this.minorTickVisible;
    }

    /**
     * Set if minor tick visible or not
     *
     * @param value Boolean
     */
    public void setMinorTickVisible(boolean value) {
        this.minorTickVisible = value;
    }

    /**
     * Get minor tick number
     *
     * @return Minor tick number
     */
    public int getMinorTickNum() {
        return this.minorTickNum;
    }

    /**
     * Set minor tick number
     *
     * @param value Minor tick number
     */
    public void setMinorTickNum(int value) {
        this.minorTickNum = value;
    }
    
    /**
     * Get tick space
     * @return Tick space
     */
    public int getTickSpace(){
        return this.tickSpace;
    }
    
    /**
     * Set tick space
     * @param value Tick space
     */
    public void setTickSpace(int value){
        this.tickSpace = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Set minimum and maximum values
     *
     * @param minValue Start value
     * @param maxValue End value
     */
    public void setMinMaxValue(double minValue, double maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        if (Double.isNaN(minValue) || Double.isNaN(maxValue)) {
            return;
        }

        updateTickValues();
//        if (this.timeAxis) {
//            this.updateTimeLabels();
//        } else {
//            tickValues = MIMath.getIntervalValues(minValue, maxValue);
//        }
    }

    /**
     * Update tick values
     */
    public void updateTickValues() {
        List<Object> r = MIMath.getIntervalValues1(minValue, maxValue);
        this.tickValues = (double[]) r.get(0);
        this.tickDeltaValue = (Double) r.get(1);
    }

//    /**
//     * Update time labels
//     */
//    public void updateTimeLabels() {
//        Date sdate = DateUtil.fromOADate(minValue);
//        Date edate = DateUtil.fromOADate(maxValue);
//        Calendar scal = Calendar.getInstance();
//        Calendar ecal = Calendar.getInstance();
//        scal.setTime(sdate);
//        ecal.setTime(edate);
//        Calendar sscal = Calendar.getInstance();
//        sscal.setTime(sdate);
//
//        List<Date> dates = new ArrayList<>();
//        scal.add(Calendar.YEAR, 5);
//        if (scal.before(ecal)) {
//            this.timeFormat = "yyyy";
//            this.timeUnit = TimeUnit.YEAR;
//            scal.setTime(sdate);
//            scal.set(Calendar.MONTH, 0);
//            scal.set(Calendar.DAY_OF_MONTH, 1);
//            scal.set(Calendar.HOUR_OF_DAY, 0);
//            scal.set(Calendar.MINUTE, 0);
//            scal.set(Calendar.SECOND, 0);
//            if (!scal.before(sscal)) {
//                dates.add(scal.getTime());
//            }
//            while (!scal.after(ecal)) {
//                scal.set(Calendar.YEAR, scal.get(Calendar.YEAR) + 1);
//                dates.add(scal.getTime());
//            }
//        } else {
//            scal.setTime(sdate);
//            scal.add(Calendar.MONTH, 5);
//            if (scal.before(ecal)) {
//                scal.setTime(sdate);
//                this.timeFormat = "M";
//                this.timeUnit = TimeUnit.MONTH;
//                scal.set(Calendar.DAY_OF_MONTH, 1);
//                scal.set(Calendar.HOUR_OF_DAY, 0);
//                scal.set(Calendar.MINUTE, 0);
//                scal.set(Calendar.SECOND, 0);
//                if (!scal.before(sscal)) {
//                    dates.add(scal.getTime());
//                }
//                while (!scal.after(ecal)) {
//                    scal.add(Calendar.MONTH, 1);
//                    if (!scal.before(sscal)) {
//                        dates.add(scal.getTime());
//                    }
//                }
//            } else {
//                scal.setTime(sdate);
//                scal.add(Calendar.DAY_OF_MONTH, 5);
//                if (scal.before(ecal)) {
//                    scal.setTime(sdate);
//                    this.timeFormat = "d";
//                    this.timeUnit = TimeUnit.DAY;
//                    scal.set(Calendar.HOUR_OF_DAY, 0);
//                    scal.set(Calendar.MINUTE, 0);
//                    scal.set(Calendar.SECOND, 0);
//                    if (!scal.before(sscal)) {
//                        dates.add(scal.getTime());
//                    }
//                    while (!scal.after(ecal)) {
//                        scal.add(Calendar.DAY_OF_MONTH, 1);
//                        if (!scal.before(sscal)) {
//                            dates.add(scal.getTime());
//                        }
//                    }
//                } else {
//                    scal.setTime(sdate);
//                    scal.add(Calendar.HOUR_OF_DAY, 5);
//                    if (scal.before(ecal)) {
//                        scal.setTime(sdate);
//                        this.timeFormat = "H";
//                        this.timeUnit = TimeUnit.HOUR;
//                        scal.set(Calendar.MINUTE, 0);
//                        scal.set(Calendar.SECOND, 0);
//                        if (!scal.before(sscal)) {
//                            dates.add(scal.getTime());
//                        }
//                        while (!scal.after(ecal)) {
//                            scal.add(Calendar.HOUR_OF_DAY, 1);
//                            if (!scal.before(sscal)) {
//                                dates.add(scal.getTime());
//                            }
//                        }
//                    } else {
//                        scal.setTime(sdate);
//                        scal.add(Calendar.MINUTE, 5);
//                        if (scal.before(ecal)) {
//                            scal.setTime(sdate);
//                            this.timeFormat = "HH:mm";
//                            this.timeUnit = TimeUnit.MINITUE;
//                            scal.set(Calendar.SECOND, 0);
//                            if (!scal.before(sscal)) {
//                                dates.add(scal.getTime());
//                            }
//                            while (!scal.after(ecal)) {
//                                scal.add(Calendar.MINUTE, 1);
//                                if (!scal.before(sscal)) {
//                                    dates.add(scal.getTime());
//                                }
//                            }
//                        } else {
//                            scal.setTime(sdate);
//                            this.timeFormat = "HH:mm:ss";
//                            this.timeUnit = TimeUnit.SECOND;
//                            if (!scal.before(sscal)) {
//                                dates.add(scal.getTime());
//                            }
//                            while (!scal.after(ecal)) {
//                                scal.add(Calendar.SECOND, 1);
//                                if (!scal.before(sscal)) {
//                                    dates.add(scal.getTime());
//                                }
//                            }                            
//                        }
//                    }
//                }
//            }
//        }
//
//        tickValues = new double[dates.size()];
//        for (int i = 0; i < dates.size(); i++) {
//            tickValues[i] = DateUtil.toOADate(dates.get(i));
//        }
//    }
    /**
     * Get tick labels
     *
     */
    public void updateTickLabels() {
        List<ChartText> tls = new ArrayList<>();
        String lab;
        if (this.autoTick) {
            if (this.getTickValues() == null) {
                return;
            }
            this.tickLocations = new ArrayList<>();
            for (double value : this.getTickValues()) {
                this.tickLocations.add(value);
                lab = String.valueOf(value);
                lab = DataConvert.removeTailingZeros(lab);
                tls.add(new ChartText(lab));
            }
        } else {
            for (int i = 0; i < this.tickLocations.size(); i++) {
                if (i >= this.tickLabels.size()) {
                    break;
                }
                double v = this.tickLocations.get(i);
                if (v >= this.minValue && v <= this.maxValue) {
                    tls.add(this.tickLabels.get(i));
                }
            }
        }

        this.tickLabels = tls;
    }

    /**
     * Get maximum label string length
     *
     * @param g Graphics2D
     * @return Maximum lable string length
     */
    public int getMaxLabelLength(Graphics2D g) {
        this.updateTickLabels();
        int max = 0;
        Dimension dim;
        int width, height;
        g.setFont(this.tickLabelFont);
        for (int i = 0; i < this.tickLabels.size(); i++) {
            ChartText lab = this.tickLabels.get(i);
            dim = Draw.getStringDimension(lab.getText(), g);
            width = dim.width;
            if (this.tickLabelAngle != 0) {
                width = (int) (dim.getWidth() * Math.cos(this.tickLabelAngle * Math.PI / 180));
                height = dim.height;
                width = Math.max(width, height);
            }
            if (max < width) {
                max = width;
            }
        }

        return max;
    }

    /**
     * Get label string with maximum length
     *
     * @return Maximum length lable string
     */
    public String getMaxLenLable() {
        this.updateTickLabels();
        if (this.tickLabels.isEmpty()) {
            return "1";
        }

        ChartText rlab = this.tickLabels.get(0);
        for (ChartText lab : this.tickLabels) {
            if (lab.getText().length() > rlab.getText().length()) {
                rlab = lab;
            }
        }

        return rlab.getText();
    }
    
    /**
     * Get tick label text with maximum length
     *
     * @return Maximum length tick label text
     */
    public ChartText getMaxLenText() {
        this.updateTickLabels();
        if (this.tickLabels.isEmpty()) {
            return new ChartText("1");
        }

        ChartText rlab = this.tickLabels.get(0);
        for (ChartText lab : this.tickLabels) {
            if (lab.getText().length() > rlab.getText().length()) {
                rlab = lab;
            }
        }

        return rlab;
    }

    /**
     * Get maximum tick label line number
     *
     * @return Maximum tick label line number
     */
    public int getMaxTickLableLines() {
        this.updateTickLabels();
        if (this.tickLabels.isEmpty()) {
            return 1;
        }

        int ln = this.tickLabels.get(0).getLineNum();
        for (ChartText lab : this.tickLabels) {
            if (lab.getLineNum() > ln) {
                ln = lab.getLineNum();
            }
        }

        return ln;
    }

    /**
     * Update lable gap
     *
     * @param g Graphics2D
     * @param rect The rectangle
     */
    public void updateLabelGap(Graphics2D g, Rectangle2D rect) {
        if (this.getTickValues() == null) {
            return;
        }

        double len;
        int n = this.getTickValues().length;
        int nn;
        if (this.xAxis) {
            len = rect.getWidth();
            int labLen = this.getMaxLabelLength(g);
            nn = (int) ((len * 0.8) / labLen);
        } else {
            len = rect.getHeight();
            FontMetrics metrics = g.getFontMetrics(this.label.getFont());
            nn = (int) (len / metrics.getHeight());
        }
        if (nn == 0) {
            nn = 1;
        }
        this.tickLabelGap = n / nn + 1;
    }

    /**
     * Update lable gap
     *
     * @param g Graphics2D
     * @param len Length
     * @return Label gap
     */
    public int getLabelGap(Graphics2D g, double len) {
        if (this.getTickValues() == null) {
            return 1;
        }

        int n = this.getTickValues().length;
        int nn;
        FontMetrics metrics = g.getFontMetrics(this.label.getFont());
        nn = (int) (len / metrics.getHeight());
        if (nn == 0) {
            nn = 1;
        }
        return n / nn + 1;
    }

    /**
     * Set color to all elements
     *
     * @param c Color
     */
    public void setColor_All(Color c) {
        this.lineColor = c;
        this.tickColor = c;
        this.tickLabelColor = c;
        this.label.setColor(c);
    }

    /**
     * Draw axis
     *
     * @param g Graphics2D
     * @param area Area
     * @param plot XYPlot
     */
    public void draw(Graphics2D g, Rectangle2D area, AbstractPlot2D plot) {
        if (plot.getDrawExtent() == null) {
            return;
        }
        if (this.xAxis) {
            this.drawXAxis(g, area, plot);
        } else {
            this.drawYAxis(g, area, plot);
        }
    }

    private void drawXAxis(Graphics2D g, Rectangle2D area, AbstractPlot2D plot) {
        double[] xy;
        double x, y;
        double miny = area.getY();
        double minx = area.getX();
        double maxx = area.getX() + area.getWidth();
        double maxy = area.getY() + area.getHeight();
        float labx, laby = (float)maxy;

        //Draw x axis
        //Draw axis line
        g.setColor(this.lineColor);
        g.setStroke(this.getLineStroke());
        if (this.location == Location.BOTTOM) {
            g.draw(new Line2D.Double(minx, maxy, maxx, maxy));
        } else {
            g.draw(new Line2D.Double(minx, miny, maxx, miny));
        }

        //Draw tick lines   
        int len = 0;
        if (this.drawTickLine) {
            g.setColor(this.tickColor);
            g.setStroke(this.tickStroke);
            g.setFont(this.tickLabelFont);
            String drawStr;
            len = this.tickLength;
            this.updateTickLabels();
            int n = 0;
            while (n < this.getTickValues().length) {
                double value = this.getTickValues()[n];

                if (value >= this.minValue && value <= this.maxValue) {
                    //Draw tick line
                    xy = plot.projToScreen(value, plot.getDrawExtent().minY, area);
                    x = xy[0];
                    x += minx;
                    if (this.location == Location.BOTTOM) {
                        if (this.insideTick) {
                            g.draw(new Line2D.Double(x, maxy, x, maxy - len));
                        } else {
                            g.draw(new Line2D.Double(x, maxy, x, maxy + len));
                        }
                    } else {
                        if (this.insideTick) {
                            g.draw(new Line2D.Double(x, miny, x, miny + len));
                        } else {
                            g.draw(new Line2D.Double(x, miny, x, miny - len));
                        }
                    }

                    //Draw tick label
                    if (this.drawTickLabel && n < this.tickLabels.size()) {
                        ChartText chartText = this.tickLabels.get(n);
                        g.setFont(tickLabelFont);
                        if (this.location == Location.BOTTOM) {
                            if (this.insideTick){
                                laby = (float)maxy;
                            } else {
                                laby = (float) (maxy + len);
                            }
                            laby += this.tickSpace;
                        } else {
                            if (this.insideTick){
                                laby = (float)miny;
                            } else {
                                laby = (float) (miny - len);
                            }
                            laby -= this.tickSpace;
                        }
                        Dimension dim = Draw.getStringDimension(chartText.getText(), this.tickLabelAngle, g);
                        labx = (float) x;
                        for (String dstr : chartText.getTexts()) {                            
                            if (this.location == Location.BOTTOM) {
                                Draw.drawString(g, labx, laby, dstr, XAlign.CENTER, YAlign.TOP, this.tickLabelAngle, true);
                                laby += dim.getHeight() + chartText.getLineSpace();                                
                            } else {
                                Draw.drawString(g, labx, laby, dstr, XAlign.CENTER, YAlign.BOTTOM, this.tickLabelAngle, true);
                                laby -= dim.getHeight() - chartText.getLineSpace();
                            }
                        }                        
                    }
                }
                n += this.getTickLabelGap();

                //Draw minor tick lines
                if (this.isMinorTickVisible()) {
                    int minorLen = len - 2;
                    double sp;
                    sp = this.tickDeltaValue * this.getTickLabelGap() / this.minorTickNum;
                    if (this instanceof LogAxis) {
                        if (n >= this.getTickValues().length) {
                            break;
                        }
                        sp = (this.getTickValues()[n] - this.getTickValues()[n - 1]) / this.minorTickNum;
                    }
                    List<Double> xx = new ArrayList<>();
                    if (n == 1) {
                        if (value > this.minValue + sp) {
                            double value1 = value;
                            for (int i = 0; i < this.minorTickNum - 1; i++) {
                                value1 = value1 - sp;
                                if (value1 <= this.minValue) {
                                    break;
                                }
                                xy = plot.projToScreen(value1, plot.getDrawExtent().minY, area);
                                x = xy[0];
//                            if (this.inverse) {
//                                x = area.getWidth() - x;
//                            }
                                x += minx;
                                xx.add(x);
                            }
                        }
                    }
                    for (int i = 0; i < this.minorTickNum - 1; i++) {
                        value = value + sp;
                        if (value >= this.maxValue) {
                            break;
                        } else if (value <= this.minValue) {
                            continue;
                        }
                        xy = plot.projToScreen(value, plot.getDrawExtent().minY, area);
                        x = xy[0];
//                    if (this.inverse) {
//                        x = area.getWidth() - x;
//                    }
                        x += minx;
                        xx.add(x);
                    }
                    for (int i = 0; i < xx.size(); i++) {
                        x = xx.get(i);
                        if (this.location == Location.BOTTOM) {
                            if (this.insideTick) {
                                g.draw(new Line2D.Double(x, maxy, x, maxy - minorLen));
                            } else {
                                g.draw(new Line2D.Double(x, maxy, x, maxy + minorLen));
                            }
                        } else if (this.insideTick) {
                            g.draw(new Line2D.Double(x, miny, x, miny + minorLen));
                        } else {
                            g.draw(new Line2D.Double(x, miny, x, miny - minorLen));
                        }
                    }
                }
            }
            //Time label - left
            if (this.drawTickLabel) {
                SimpleDateFormat format;
                if (this instanceof TimeAxis) {
                    TimeAxis tAxis = (TimeAxis) this;
                    if (tAxis.isVarFormat()) {
                        drawStr = null;
                        switch (tAxis.getTimeUnit()) {
                            case MONTH:
                                format = new SimpleDateFormat("yyyy");
                                Date cdate = DateUtil.fromOADate(this.getTickValues()[0]);
                                drawStr = format.format(cdate);
                                break;
                            case DAY:
                                format = new SimpleDateFormat("yyyy-MM");
                                cdate = DateUtil.fromOADate(this.getTickValues()[0]);
                                drawStr = format.format(cdate);
                                break;
                            case HOUR:
                            case MINUTE:
                            case SECOND:
                                format = new SimpleDateFormat("yyyy-MM-dd");
                                cdate = DateUtil.fromOADate(this.getTickValues()[0]);
                                drawStr = format.format(cdate);
                                break;
                        }
                        if (drawStr != null) {
                            labx = (float) minx;
                            laby = laby + this.tickSpace;
                            Draw.drawString(g, labx, laby, drawStr, XAlign.LEFT, YAlign.TOP, true);
                            laby += Draw.getStringDimension(drawStr, g).height;
                        }
                    }
                }
            }
        }

        //Draw label
        if (this.isDrawLabel()) {
            x = (maxx - minx) / 2 + minx;
            g.setFont(this.getLabelFont());
            g.setColor(this.getLabelColor());
            labx = (float)x;
            laby += this.tickSpace;
            this.label.draw(g, labx, laby);
        }
    }

    private void drawYAxis(Graphics2D g, Rectangle2D area, AbstractPlot2D plot) {
        double[] xy;
        double x, y, sx;
        double miny = area.getY();
        double minx = area.getX();
        double maxx = area.getX() + area.getWidth();
        double maxy = area.getY() + area.getHeight();
        float labx, laby;

        //Draw y axis
        //Draw axis line
        g.setColor(this.getLineColor());
        g.setStroke(this.getLineStroke());
        if (this.location == Location.LEFT) {
            sx = minx - shift;
            g.draw(new Line2D.Double(sx, maxy, sx, miny));
        } else {
            sx = maxx + shift;
            g.draw(new Line2D.Double(sx, maxy, sx, miny));
        }

        //Draw tick lines   
        int len = 0;
        if (this.drawTickLine) {
            g.setColor(this.getTickColor());
            g.setStroke(this.getTickStroke());
            g.setFont(this.getTickLabelFont());
            this.updateLabelGap(g, area);
            len = this.getTickLength();
            this.updateTickLabels();
            String drawStr;
            Dimension dim;
            int n = 0;
            while (n < this.getTickValues().length) {
                double value = this.getTickValues()[n];
                xy = plot.projToScreen(plot.getDrawExtent().minX, value, area);
                y = xy[1];
                y += area.getY();
                if (this.location == Location.LEFT) {
                    if (this.isInsideTick()) {
                        g.draw(new Line2D.Double(sx, y, sx + len, y));
                    } else {
                        g.draw(new Line2D.Double(sx, y, sx - len, y));
                    }
                } else if (this.isInsideTick()) {
                    g.draw(new Line2D.Double(sx, y, sx - len, y));
                } else {
                    g.draw(new Line2D.Double(sx, y, sx + len, y));
                }
                //Draw tick label
                if (this.drawTickLabel && n < this.tickLabels.size()) {
                    drawStr = this.tickLabels.get(n).getText();
                    g.setFont(this.tickLabelFont);
                    if (this.location == Location.LEFT) {
                        labx = (float) (sx - this.tickSpace);
                        if (!this.isInsideTick()) {
                            labx -= len;
                        }
                        laby = (float)y;
                        Draw.drawString(g, labx, laby, drawStr, XAlign.RIGHT, YAlign.CENTER, this.tickLabelAngle, true);
                    } else {
                        labx = (float) (sx + this.tickSpace);
                        if (!this.isInsideTick()) {
                            labx += len;
                        }
                        laby = (float)y;
                        Draw.drawString(g, labx, laby, drawStr, XAlign.LEFT, YAlign.CENTER, this.tickLabelAngle, true);
                    }                  
                }
                n += this.getTickLabelGap();

                //Draw minor tick lines
                if (this.isMinorTickVisible()) {
                    int minorLen = len - 2;
                    double sp;
                    sp = this.tickDeltaValue * this.getTickLabelGap() / this.minorTickNum;
                    if (this instanceof LogAxis) {
                        if (n >= this.getTickValues().length) {
                            break;
                        }
                        sp = (this.getTickValues()[n] - this.getTickValues()[n - 1]) / this.minorTickNum;
                    }
                    List<Double> yy = new ArrayList<>();
                    if (n == 1) {
                        if (value > this.minValue + sp) {
                            double value1 = value;
                            for (int i = 0; i < this.minorTickNum - 1; i++) {
                                value1 = value1 - sp;
                                if (value1 <= this.minValue) {
                                    break;
                                }
                                xy = plot.projToScreen(plot.getDrawExtent().minX, value1, area);
                                y = xy[1];
                                y += miny;
                                yy.add(y);
                            }
                        }
                    }
                    for (int i = 0; i < this.minorTickNum - 1; i++) {
                        value = value + sp;
                        if (value >= this.maxValue) {
                            break;
                        } else if (value <= this.minValue) {
                            continue;
                        }
                        xy = plot.projToScreen(plot.getDrawExtent().minX, value, area);
                        y = xy[1];
                        y += miny;
                        yy.add(y);
                    }
                    for (int i = 0; i < yy.size(); i++) {
                        y = yy.get(i);
                        if (this.location == Location.LEFT) {
                            if (this.isInsideTick()) {
                                g.draw(new Line2D.Double(sx, y, sx + minorLen, y));
                            } else {
                                g.draw(new Line2D.Double(sx, y, sx - minorLen, y));
                            }
                        } else if (this.isInsideTick()) {
                            g.draw(new Line2D.Double(sx, y, sx - minorLen, y));
                        } else {
                            g.draw(new Line2D.Double(sx, y, sx + minorLen, y));
                        }
                    }
                }
            }
        }

        //Draw label
        XAlign x_align = XAlign.RIGHT;
        YAlign y_align = YAlign.CENTER;
        if (this.isDrawLabel()) {            
            if (this.location == Location.LEFT) {
                x = sx - this.tickSpace - this.getMaxLabelLength(g);
                if (!this.isInsideTick()) {
                    x -= len;
                }
                y = (maxy - miny) / 2 + miny; 
            } else {
                x = sx + this.tickSpace + this.getMaxLabelLength(g) + 5;
                if (!this.isInsideTick()) {
                    x += len;
                }
                y = (maxy - miny) / 2 + miny;
                x_align = XAlign.LEFT;                
            }
            g.setFont(this.getLabelFont());
            Draw.drawString(g, (float)x, (float)y, this.label.getText(), x_align, 
                y_align, 90, this.label.isUseExternalFont());
        }
    }
    
    /**
     * Get x axis height
     * @param g Graphics2D
     * @return Axis height
     */
    public int getXAxisHeight(Graphics2D g) {
        if (!this.isVisible()) {
            return 0;
        }

        int height = 0;
        if (!this.insideTick){
            height += this.tickLength;
        }
        this.updateTickLabels();
        if (this.isDrawTickLabel() && this.tickLabels.size() > 0) {
            ChartText text = this.getMaxLenText();
            text.setAngle(this.tickLabelAngle);
            height += this.tickSpace + text.getTrueDimension(g).height;
            Dimension dim = Draw.getStringDimension("Test", this.getTickLabelAngle(), g);
            if (this instanceof TimeAxis) {
                height += dim.height + this.tickSpace;
            }
        }
        if (this.isDrawLabel()) {
            g.setFont(this.getLabelFont());
            Dimension dim = Draw.getStringDimension(this.getLabel().getText(), g);
            height += dim.height + this.tickSpace * 2;
        }

        return height;
    }

    /**
     * Get y axis width
     * @param g Graphics2D
     * @return Axis width
     */
    public int getYAxisWidth(Graphics2D g) {
        if (!this.isVisible()) {
            return 0;
        }

        int width = 0;
        if (this.isDrawTickLabel()) {
            width += this.getMaxLabelLength(g) + this.tickSpace + this.tickSpace;
        }
        if (!this.isInsideTick()) {
            width += this.getTickLength();
        }
        if (this.isDrawLabel()) {
            g.setFont(this.getLabelFont());
            Dimension dim = Draw.getStringDimension(this.getLabel().getText(), g);
            width += dim.height + 10 - this.tickSpace;
        }

        return width;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Axis o = null;
        try {
            o = (Axis) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return o;
    }

    // </editor-fold>
}
