/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.movement.model.util;


import java.text.ParseException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Date;  //leave be for now


import org.slf4j.LoggerFactory;

/**
 **/
public class DateUtil {

    final static org.slf4j.Logger LOG = LoggerFactory.getLogger(DateUtil.class);

    final static String FORMAT = "yyyy-MM-dd HH:mm:ss Z";

    final static long yearInSeconds = 31556926;


    public static Instant addYearToDate(Instant date, int years) {

        return date.plusSeconds(years * yearInSeconds);
    }

    public static Instant removeYearFromDate(Instant date, int years) {
        return date.minusSeconds(years * yearInSeconds);

    }


    public static String parseUTCDateToString(Instant date) {
        if(date == null){
            return null;
        }
        return parseDateToString(date, DateFormats.FORMAT.getFormat());

    }

    public static String parseDateToString(Instant date, String format){
        return date.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern(format));
    }

    public static String convertUtilDateToString(Date d, String format){
        Instant i = d.toInstant();
        return parseDateToString(i, format);
    }


    public static Instant nowUTC(){
        return Instant.now();
    }


    public static Instant getDateFromString(String inDate) throws ParseException {
        return convertDateTimeInUTC(inDate);

    }

    public static Instant getDateFromString(String inDate, String format){
        return ZonedDateTime.parse(inDate, DateTimeFormatter.ofPattern(format)).toInstant();   //goes via ZonedDateTime to make sure that it can handle formats other then ISO_INSTANT, for example formats other then 2011-12-03T10:15:30Z and does not cry in pain from a zone
    }



    //This one supports more different formats then getDateFromString
    public static Instant parseToUTCDate(String dateTimeInUTC){
        return convertDateTimeInUTC(dateTimeInUTC);
    }


    public static Instant addSecondsToDate(Instant inDate, int seconds) {
        return inDate.plusSeconds(seconds);
    }



    public static Instant convertDateTimeInUTC(String dateTimeInUTC){
        if(dateTimeInUTC == null){
            return null;
        }
        if(dateTimeInUTC.length() < 20){    //if there is no offset info, assume UTC and add it
            dateTimeInUTC = dateTimeInUTC.concat(" Z");
        }
        for (DateFormats format : DateFormats.values()) {
            Instant date = convertDateTimeInUTC(dateTimeInUTC, format.getFormat());
            if (date != null) {
                return date;
            }
        }
        LOG.error("Could not parse dateTimeInUTC: " + dateTimeInUTC + " with pattern any defined pattern.");
        return null;
    }

    public static Instant convertDateTimeInUTC(String dateTimeInUTC, String pattern){
        if (dateTimeInUTC != null) {
            try {
                return getDateFromString(dateTimeInUTC, pattern);
            } catch (DateTimeParseException e) {
                LOG.info("Could not parse dateTimeInUTC: " + dateTimeInUTC + " with pattern: " + pattern + ". Trying next pattern",e);
            }
        }
        return null;
    }

}