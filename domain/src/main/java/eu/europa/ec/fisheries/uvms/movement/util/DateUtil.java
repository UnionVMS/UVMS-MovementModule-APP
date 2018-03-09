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
package eu.europa.ec.fisheries.uvms.movement.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.LoggerFactory;

/**
 **/
public class DateUtil {

    final static org.slf4j.Logger LOG = LoggerFactory.getLogger(DateUtil.class);

    public static java.sql.Timestamp getDateFromString(String inDate) throws ParseException {
    	Date date = convertDateTimeInUTC(inDate);
    	return new java.sql.Timestamp(date.getTime());

    }

    public static Date parseToUTCDate(String dateTimeInUTC){
    	return convertDateTimeInUTC(dateTimeInUTC);
    }


    public static String parseUTCDateToString(Date date) {
        String dateString = null;
        if (date != null) {
            DateFormat df = new SimpleDateFormat(DateFormats.FORMAT.getFormat());
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            dateString = df.format(date);
        }
        return dateString;
    }

    public static XMLGregorianCalendar addSecondsToDate(XMLGregorianCalendar inDate, int seconds) {
        Date date = DateUtil.parsePositionTime(inDate);
        //DateTime newDateTime = new DateTime(date);
        //DateTime plusSeconds = newDateTime.plusSeconds(1);
        Date plusSeconds = new Date(date.getTime() + 1000L); //add one second
        return parsePositionTime(plusSeconds);
    }

    public static Date addSecondsToDate(Date inDate, int seconds) {
        DateTime newDateTime = new DateTime(inDate);
        DateTime plusSeconds = newDateTime.plusSeconds(1);
        return plusSeconds.toDate();
    }

    public static Date parsePositionTime(XMLGregorianCalendar positionTime) { //TODO REWORK SO THAT IT DOES NOT INVOLVE BLOODY TIMEZONES
        if (positionTime != null) {
            DateTimeZone localTZ = DateTimeZone.getDefault();
            long eventMillsInUTCTimeZone = localTZ.convertLocalToUTC(positionTime.toGregorianCalendar().getTime().getTime(), false);
            DateTime evenDateTimeInUTCTimeZone = new DateTime(eventMillsInUTCTimeZone);
            return evenDateTimeInUTCTimeZone.toDate();
        }
        return null;
    }

    public static XMLGregorianCalendar parsePositionTime(Date timestamp) {
        if (timestamp != null) {
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(timestamp);
            XMLGregorianCalendar xmlCalendar = null;
            try {
                xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
            } catch (DatatypeConfigurationException ex) {
            }
            return xmlCalendar;
        } else {
            return null;
        }
    }

    public static Date nowUTC(){
        return new DateTime(DateTimeZone.UTC).toLocalDateTime().toDate();
    }

    public static Date convertDateTimeInUTC(String dateTimeInUTC){
        for (DateFormats format : DateFormats.values()) {
            Date date = convertDateTimeInUTC(dateTimeInUTC, format.getFormat());
            if (date != null) {
                return date;
            }
        }
        LOG.error("Could not parse dateTimeInUTC: " + dateTimeInUTC + " with pattern any defined pattern.");
        return null;
    }

    public static Date convertDateTimeInUTC(String dateTimeInUTC, String pattern){
        if (dateTimeInUTC != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                Date date = sdf.parse(dateTimeInUTC);
                return date;
            } catch (java.text.ParseException e) {
                LOG.info("Could not parse dateTimeInUTC: " + dateTimeInUTC + " with pattern: " + pattern + ". Trying next pattern");
            }
        }
        return null;
    }

    public static XMLGregorianCalendar getXMLGregorianCalendarInUTC(Date dateTimeInUTC){
        if (dateTimeInUTC != null) {
            GregorianCalendar calendar = (GregorianCalendar) GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat sdf = new SimpleDateFormat(DateFormats.DATE_TIME_PATTERN_UTC.getFormat());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                Date theDate = sdf.parse(dateTimeInUTC.toString());
                calendar.setTime(theDate);
                return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
            } catch (DatatypeConfigurationException e) {
                LOG.error("[ Error when getting XML Gregorian calendar. ] ", e);
            } catch (ParseException e) {
                LOG.error("Could not parse dateTimeInUTC: "+dateTimeInUTC.toString()+ " with pattern: " + DateFormats.DATE_TIME_PATTERN_UTC.getFormat());
            }
        }
        return null;
    }


}