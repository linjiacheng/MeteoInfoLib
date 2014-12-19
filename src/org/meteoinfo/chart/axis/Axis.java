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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.meteoinfo.drawing.Draw;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.util.DateUtil;

/**
 *
 * @author yaqiang
 */
public class Axis {

    // <editor-fold desc="Variables">
    private boolean xAxis;
    private String label;
    private boolean visible;
    private boolean drawLabel;
    private Color lineColor;
    private Stroke lineStroke;
    private Color tickColor;
    private Stroke tickStroke;
    private int tickLength;
    private Font labelFont;
    private Color labelColor;
    private Font tickLabelFont;
    private Color tickLabelColor;
    private int tickLabelGap;
    private double tickStartValue;
    private double tickDeltaValue;
    private double minValue;
    private double maxValue;
    private double[] tickValues;
    private boolean timeAxis;
    private String timeFormat;
    private TimeUnit timeUnit;
    private boolean inverse;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     *
     * @param label Axis label
     */
    public Axis(String label) {
        this.xAxis = true;
        this.label = label;
        this.visible = true;
        this.drawLabel = true;
        this.lineColor = Color.black;
        this.lineStroke = new BasicStroke(1.0f);
        this.tickColor = Color.black;
        this.tickStroke = new BasicStroke(1.0f);
        this.tickLength = 3;
        this.labelFont = new Font("Arial", Font.PLAIN, 14);
        this.labelColor = Color.black;
        this.tickLabelFont = new Font("Arial", Font.PLAIN, 12);
        this.tickLabelColor = Color.black;
        this.tickLabelGap = 1;
        this.timeAxis = false;
        this.timeFormat = "yyyy-MM-dd";
        this.timeUnit = TimeUnit.DAY;
        this.inverse = false;
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
     * Get axis label
     *
     * @return Axis label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Set axis label
     *
     * @param value Axis label
     */
    public void setLabel(String value) {
        label = value;
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
     * Set line coloe
     *
     * @param value Line color
     */
    public void setLineColor(Color value) {
        lineColor = value;
    }

    /**
     * Get line stroke
     *
     * @return Line stroke
     */
    public Stroke getLineStroke() {
        return lineStroke;
    }

    /**
     * Set line stroke
     *
     * @param value Line stroke
     */
    public void setLineStroke(Stroke value) {
        lineStroke = value;
    }

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
     * Get label font
     *
     * @return Label font
     */
    public Font getLabelFont() {
        return labelFont;
    }

    /**
     * Set lable font
     *
     * @param value Lable font
     */
    public void setLabelFont(Font value) {
        labelFont = value;
    }

    /**
     * Get label color
     *
     * @return Label color
     */
    public Color getLabelColor() {
        return labelColor;
    }

    /**
     * Set label color
     *
     * @param value Label color
     */
    public void setLabelColor(Color value) {
        labelColor = value;
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
        return this.tickValues;
    }

    /**
     * Get if is time axis
     *
     * @return Boolean
     */
    public boolean isTimeAxis() {
        return this.timeAxis;
    }

    /**
     * Set if is time axis
     *
     * @param value Boolean
     */
    public void setTimeAxis(boolean value) {
        this.timeAxis = value;
    }
    
    /**
     * Get time format
     * @return Time format
     */
    public String getTimeFormat(){
        return this.timeFormat;
    }
    
    /**
     * Set time format
     * @param value 
     */
    public void setTimeFormat(String value){
        this.timeFormat = value;
    }
    
    /**
     * Get time unit
     * @return Time unit
     */
    public TimeUnit getTimeUnit(){
        return this.timeUnit;
    }
    
    /**
     * Set time unit
     * @param value Time unit
     */
    public void setTimeUnit(TimeUnit value){
        this.timeUnit = value;
    }

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
        if (this.timeAxis) {
            this.updateTimeLabels();
        } else {
            tickValues = MIMath.createContourValues(minValue, maxValue);
        }
    }

    /**
     * Update time labels
     */
    public void updateTimeLabels_back() {
        Date sdate = DateUtil.fromOADate(minValue);
        Date edate = DateUtil.fromOADate(maxValue);
        Calendar scal = Calendar.getInstance();
        Calendar ecal = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        scal.setTime(sdate);
        ecal.setTime(edate);
        Calendar sscal = Calendar.getInstance();
        sscal.setTime(sdate);
        boolean sameYear = false;
        boolean sameMonth = false;
        boolean sameDay = false;
        boolean sameHour = false;
        if (scal.get(Calendar.YEAR) == ecal.get(Calendar.YEAR)) {
            sameYear = true;
        }
        if (scal.get(Calendar.MONTH) == ecal.get(Calendar.MONTH)) {
            sameMonth = true;
        }
        if (scal.get(Calendar.DAY_OF_YEAR) == ecal.get(Calendar.DAY_OF_YEAR)) {
            sameDay = true;
        }
        if (scal.get(Calendar.HOUR_OF_DAY) == ecal.get(Calendar.HOUR_OF_DAY)) {
            sameHour = true;
        }

        List<Date> dates = new ArrayList<Date>();
        if (sameYear) {
            if (sameMonth) {
                if (sameDay) {
                    if (sameHour) {
                        cal.setTime(scal.getTime());
                        cal.add(Calendar.MINUTE, 5);
                        if (cal.before(ecal)) {
                            this.timeFormat = "HH:mm";
                            scal.set(Calendar.MINUTE, scal.get(Calendar.MINUTE) + 1);
                            scal.set(Calendar.SECOND, 0);
                            dates.add(scal.getTime());
                            while (scal.before(ecal)) {
                                scal.set(Calendar.MINUTE, scal.get(Calendar.MINUTE) + 1);
                                dates.add(scal.getTime());
                            }
                        } else {
                            this.timeFormat = "HH:mm:ss";
                            scal.set(Calendar.MINUTE, 0);
                            scal.set(Calendar.SECOND, 0);
                            if (scal.after(sscal)) {
                                dates.add(scal.getTime());
                            }
                            while (scal.before(ecal)) {
                                scal.set(Calendar.SECOND, scal.get(Calendar.SECOND) + 10);
                                if (scal.after(sscal)) {
                                    dates.add(scal.getTime());
                                }
                            }
                        }
                    } else {
                        this.timeFormat = "HH:mm";
                        scal.set(Calendar.HOUR_OF_DAY, scal.get(Calendar.HOUR_OF_DAY) + 1);
                        scal.set(Calendar.MINUTE, 0);
                        scal.set(Calendar.SECOND, 0);
                        dates.add(scal.getTime());
                        while (scal.before(ecal)) {
                            scal.set(Calendar.HOUR_OF_DAY, scal.get(Calendar.HOUR_OF_DAY) + 1);
                            dates.add(scal.getTime());
                        }
                    }
                } else {
                    cal.setTime(scal.getTime());
                    cal.add(Calendar.DAY_OF_MONTH, 5);
                    if (cal.before(ecal)) {
                        this.timeFormat = "MM/dd";
                        scal.set(Calendar.DAY_OF_MONTH, scal.get(Calendar.DAY_OF_MONTH) + 1);
                        scal.set(Calendar.HOUR_OF_DAY, 0);
                        scal.set(Calendar.MINUTE, 0);
                        scal.set(Calendar.SECOND, 0);
                        dates.add(scal.getTime());
                        while (scal.before(ecal)) {
                            scal.set(Calendar.DAY_OF_MONTH, scal.get(Calendar.DAY_OF_MONTH) + 1);
                            dates.add(scal.getTime());
                        }
                    } else {
                        this.timeFormat = "HH:mm";
                        scal.set(Calendar.HOUR_OF_DAY, 0);
                        scal.set(Calendar.MINUTE, 0);
                        scal.set(Calendar.SECOND, 0);
                        if (scal.after(sscal)) {
                            dates.add(scal.getTime());
                        }
                        while (scal.before(ecal)) {
                            scal.set(Calendar.HOUR_OF_DAY, scal.get(Calendar.HOUR_OF_DAY) + 6);
                            if (scal.after(sscal)) {
                                dates.add(scal.getTime());
                            }
                        }
                    }
                }
            } else {
                this.timeFormat = "MM";
                scal.set(Calendar.MONTH, scal.get(Calendar.MONTH) + 1);
                scal.set(Calendar.DAY_OF_MONTH, 1);
                scal.set(Calendar.HOUR_OF_DAY, 0);
                scal.set(Calendar.MINUTE, 0);
                scal.set(Calendar.SECOND, 0);
                dates.add(scal.getTime());
                while (scal.before(ecal)) {
                    scal.set(Calendar.MONTH, scal.get(Calendar.MONTH) + 1);
                    dates.add(scal.getTime());
                }
            }
        } else {
            this.timeFormat = "yyyy";
            scal.set(Calendar.YEAR, scal.get(Calendar.YEAR) + 1);
            scal.set(Calendar.MONTH, 0);
            scal.set(Calendar.DAY_OF_MONTH, 1);
            scal.set(Calendar.HOUR_OF_DAY, 0);
            scal.set(Calendar.MINUTE, 0);
            scal.set(Calendar.SECOND, 0);
            dates.add(scal.getTime());
            while (scal.before(ecal)) {
                scal.set(Calendar.YEAR, scal.get(Calendar.YEAR) + 1);
                dates.add(scal.getTime());
            }
        }

        tickValues = new double[dates.size()];
        for (int i = 0; i < dates.size(); i++) {
            tickValues[i] = DateUtil.toOADate(dates.get(i));
        }
    }

    /**
     * Update time labels
     */
    public void updateTimeLabels() {
        Date sdate = DateUtil.fromOADate(minValue);
        Date edate = DateUtil.fromOADate(maxValue);
        Calendar scal = Calendar.getInstance();
        Calendar ecal = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        scal.setTime(sdate);
        ecal.setTime(edate);
        Calendar sscal = Calendar.getInstance();
        sscal.setTime(sdate);

        List<Date> dates = new ArrayList<Date>();
        scal.add(Calendar.YEAR, 5);
        if (scal.before(ecal)) {
            this.timeFormat = "yyyy";
            this.timeUnit = TimeUnit.YEAR;
            scal.setTime(sdate);
            scal.set(Calendar.MONTH, 0);
            scal.set(Calendar.DAY_OF_MONTH, 1);
            scal.set(Calendar.HOUR_OF_DAY, 0);
            scal.set(Calendar.MINUTE, 0);
            scal.set(Calendar.SECOND, 0);
            if (scal.after(sscal)) {
                dates.add(scal.getTime());
            }
            while (scal.before(ecal)) {
                scal.set(Calendar.YEAR, scal.get(Calendar.YEAR) + 1);
                dates.add(scal.getTime());
            }
        } else {
            scal.setTime(sdate);
            scal.add(Calendar.MONTH, 5);
            if (scal.before(ecal)) {
                scal.setTime(sdate);
                this.timeFormat = "M";
                this.timeUnit = TimeUnit.MONTH;
                scal.set(Calendar.DAY_OF_MONTH, 1);
                scal.set(Calendar.HOUR_OF_DAY, 0);
                scal.set(Calendar.MINUTE, 0);
                scal.set(Calendar.SECOND, 0);
                if (scal.after(sscal)) {
                    dates.add(scal.getTime());
                }
                while (scal.before(ecal)) {
                    scal.add(Calendar.MONTH, 1);
                    if (scal.after(sscal)) {
                        dates.add(scal.getTime());
                    }
                }
            } else {
                scal.setTime(sdate);
                scal.add(Calendar.DAY_OF_MONTH, 5);
                if (scal.before(ecal)) {
                    scal.setTime(sdate);
                    this.timeFormat = "d";
                    this.timeUnit = TimeUnit.DAY;
                    scal.set(Calendar.HOUR_OF_DAY, 0);
                    scal.set(Calendar.MINUTE, 0);
                    scal.set(Calendar.SECOND, 0);
                    if (scal.after(sscal)) {
                        dates.add(scal.getTime());
                    }
                    while (scal.before(ecal)) {
                        scal.add(Calendar.DAY_OF_MONTH, 1);
                        if (scal.after(sscal)) {
                            dates.add(scal.getTime());
                        }
                    }
                } else {
                    scal.setTime(sdate);
                    scal.add(Calendar.HOUR_OF_DAY, 5);
                    if (scal.before(ecal)) {
                        scal.setTime(sdate);
                        this.timeFormat = "H";
                        this.timeUnit = TimeUnit.HOUR;
                        scal.set(Calendar.MINUTE, 0);
                        scal.set(Calendar.SECOND, 0);
                        if (scal.after(sscal)) {
                            dates.add(scal.getTime());
                        }
                        while (scal.before(ecal)) {
                            scal.add(Calendar.HOUR_OF_DAY, 1);
                            if (scal.after(sscal)) {
                                dates.add(scal.getTime());
                            }
                        }
                    } else {
                        scal.setTime(sdate);
                        scal.add(Calendar.MINUTE, 5);
                        if (scal.before(ecal)) {
                            scal.setTime(sdate);
                            this.timeFormat = "HH:mm";
                            this.timeUnit = TimeUnit.MINITUE;
                            scal.set(Calendar.SECOND, 0);
                            if (scal.after(sscal)) {
                                dates.add(scal.getTime());
                            }
                            while (scal.before(ecal)) {
                                scal.add(Calendar.MINUTE, 1);
                                if (scal.after(sscal)) {
                                    dates.add(scal.getTime());
                                }
                            }
                        } else {
                            scal.setTime(sdate);
                            this.timeFormat = "HH:mm:ss";
                            this.timeUnit = TimeUnit.SECOND;
                            if (scal.after(sscal)) {
                                dates.add(scal.getTime());
                            }
                            while (scal.before(ecal)) {
                                scal.add(Calendar.SECOND, 1);
                                if (scal.after(sscal)) {
                                    dates.add(scal.getTime());
                                }
                            }                            
                        }
                    }
                }
            }
        }

        tickValues = new double[dates.size()];
        for (int i = 0; i < dates.size(); i++) {
            tickValues[i] = DateUtil.toOADate(dates.get(i));
        }
    }

    /**
     * Get tick labels
     *
     * @return Tick labels
     */
    public List<String> getTickLabels() {
        List<String> tls = new ArrayList<String>();
        String lab;
        if (this.timeAxis) {
            SimpleDateFormat format = new SimpleDateFormat(this.timeFormat);
            Date date;
            for (double value : this.getTickValues()) {
                date = DateUtil.fromOADate(value);
                lab = format.format(date);
                tls.add(lab);
            }
        } else {
            for (double value : this.getTickValues()) {
                lab = String.valueOf(value);
                lab = DataConvert.removeTailingZeros(lab);
                tls.add(lab);
            }
        }

        return tls;
    }

    /**
     * Get maximum label string length
     *
     * @param g Graphics2D
     * @return Maximum lable string length
     */
    public int getMaxLabelLength(Graphics2D g) {
        FontMetrics metrics = g.getFontMetrics(this.tickLabelFont);
        List<String> tls = this.getTickLabels();
        int max = 0;
        int width;
        for (String lab : tls) {
            width = metrics.stringWidth(lab);
            if (max < width) {
                max = width;
            }
        }

        return max;
    }

    /**
     * Update lable gap
     *
     * @param g Graphics2D
     * @param rect The rectangle
     */
    public void updateLabelGap(Graphics2D g, Rectangle2D rect) {
        double len;
        int n = this.tickValues.length;
        int nn;
        if (this.xAxis) {
            len = rect.getWidth();
            int labLen = this.getMaxLabelLength(g);
            nn = (int) ((len * 0.8) / labLen);
        } else {
            len = rect.getHeight();
            FontMetrics metrics = g.getFontMetrics(labelFont);
            nn = (int) (len / metrics.getHeight());
        }
        this.tickLabelGap = n / nn + 1;
    }
        
    // </editor-fold>
}
