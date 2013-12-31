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
package org.meteoinfo.global;

import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author yaqiang
 */
public class DateUtil {
    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Add days to a date
     * @param sDate Start date
     * @param days Days
     * @return Added date
     */
    public static Date addDays(Date sDate, float days){
        Calendar cal = Calendar.getInstance();
        cal.setTime(sDate);
        int intDays = (int)days;
        cal.add(Calendar.DAY_OF_YEAR, intDays);
        int hours = (int)((days - intDays) * 24);
        cal.add(Calendar.HOUR, hours);
        
        return cal.getTime();
    }
    // </editor-fold>
}
