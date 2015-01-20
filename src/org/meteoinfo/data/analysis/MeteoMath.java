/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.analysis;

/**
 *
 * @author yaqiang
 */
public class MeteoMath {

    /**
     * Calculate saturation vapor pressure
     *
     * @param tc Ari temperature
     * @return Saturation vapor pressure
     */
    public static double cal_Es(double tc) {
        double pol, pol1;
        pol = 0.99999683 + tc * (-0.90826951e-02
                + tc * (0.78736169e-04 + tc * (-0.61117958e-06
                + tc * (0.43884187e-08 + tc * (-0.29883885e-10
                + tc * (0.21874425e-12 + tc * (-0.17892321e-14
                + tc * (0.11112018e-16 + tc * (-0.30994571e-19)))))))));
        pol1 = 6.1078 / Math.pow(pol, 8);
        return pol1;
    }

    /**
     * Calculate dewpoint from actual water vapor pressure
     * @param e Actural water vapor pressure
     * @return Dewpoint
     */
    public static double cal_Tdc(double e) {
        double lu, x, dnm, fac, t, edp, dtdew, dt;
        if (e <= 0.06 || e >= 1013.) {
            lu = 9999.;
            return lu;
        }
        x = Math.log(e / 6.1078);
        dnm = 17.269388 - x;
        t = 237.3 * x / dnm;
        fac = 1. / (e * dnm);
        edp = cal_Es(t);
        dtdew = (t + 237.3) * fac;
        dt = dtdew * (e - edp);
        t = t + dt;
        while (Math.abs(dt) >= 1.e-4) {
            edp = cal_Es(t);
            dtdew = (t + 237.3) * fac;
            dt = dtdew * (e - edp);
            t = t + dt;
        }
        return t;
    }
    
    /**
     * Calculate dewpoint from temperature and relative humidity
     * @param t Temperature
     * @param rh Relative humidity
     * @return Dewpoint
     */
    public static double cal_Tdc(double t, double rh){
        double esw = cal_Es(t);
        double e = esw * (rh / 100);
        return cal_Tdc(e);
    }
    
    /**
     * Calculate relative humidity
     * @param t Temperature
     * @param tdc Dewpoint
     * @return Relative humidity as percent (i.e. 80%)
     */
    public static double cal_RH(double t, double tdc){
        double esw1 = cal_Es(t);
        double esw2 = cal_Es(tdc);
        return esw2 / esw1 * 100;
    }
    
    /**
     * Calculate relative humidity
     * @param tc Temperature
     * @param tdc Dewpoint temperature
     * @return Relative humidity as percent (i.e. 80%)
     */
    public static double cal_RH_1(double tc, double tdc){
        double es = cal_Es(tc);
        double e = cal_Es(tdc);
        return e / es * 100;
    }
    
    /**
     * Calculate dewpoint temperature
     * @param e Actual vapor pressure
     * @return Dewpoint temperature (celsius)
     */
    public static double cal_Tdc_1(double e){
        return (-430.22 + 237.7 * Math.log(e)) / (-Math.log(e) + 19.08);
    }
    
    /**
     * Calculate dewpoint temperature
     * @param tc Air temperature
     * @param rh Relative humidity
     * @return Dewpoint temperature (celsius)
     */
    public static double cal_Tdc_1(double tc, double rh){
        double es = cal_Es(tc);
        double e = cal_E(es, rh);
        return cal_Tdc(e);
    }
    
    /**
     * Calculate celsius temperature from fahrenheit temperature
     * @param tf Fahrenheit temperature
     * @return Celsius temperature
     */
    public static double cal_Celsius(double tf){
        return 5.0 / 9.0 * (tf - 32.0);
    }
    
    /**
     * Calculate fahrenheit temperature from celsius temperature
     * @param tc Celsius temperature
     * @return Fahrenheit temperature
     */
    public static double cal_Fahrenheit(double tc){
        return (9.0 / 5.0) * tc + 32;
    }
    
    /**
     * Calculate saturation vapor pressure (Es)
     * @param tc Air temperature
     * @return Saturation vapor pressure
     */
    public static double cal_Es_1(double tc){
        return 6.11 * Math.pow(10.0, (7.5 * tc / (237.7 + tc)));
    }
    
    /**
     * Calculate actual vapor pressure (E) of the air
     * @param es Saturation vapor pressure
     * @param rh Relative humidity
     * @return Actual vapor pressure
     */
    public static double cal_E(double es, double rh){
        return (rh * es) / 100;
    }        
       
}
