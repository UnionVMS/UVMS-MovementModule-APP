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
import java.util.Calendar;
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

    public static java.sql.Timestamp getDateFromString(final String inDate) throws ParseException {
        final Date date = parseToUTCDate(inDate);
        return new java.sql.Timestamp(date.getTime());
    }

    public static Date parseToUTCDate(final String dateTimeInUTC){
        for (final DateFormats format : DateFormats.values()) {
            final DateTimeFormatter formatter = DateTimeFormat.forPattern(format.getFormat());
            final DateTime dateTime = formatter.withZoneUTC().parseDateTime(dateTimeInUTC);
            if (dateTime != null) {
                return dateTime.toLocalDateTime().toDate();
            }
        }
        LOG.error("Could not parse dateTimeInUTC: " + dateTimeInUTC + " with pattern any defined pattern.");
        return null;
    }

    public static Date parseToUTCDate_a_more_stable_if_you_want_to_work_like_this(final String dateTimeInUTC) {
        try {
            for (final DateFormats format : DateFormats.values()) {
                try {
                    final DateTimeFormatter formatter = DateTimeFormat.forPattern(format.getFormat());
                    final DateTime dateTime = formatter.withZoneUTC().parseDateTime(dateTimeInUTC);
                    if (dateTime != null) {
                        return dateTime.toLocalDateTime().toDate();
                    }
                } catch (final RuntimeException e) {
                    LOG.error("Could not parse dateTimeInUTC: " + dateTimeInUTC + " with pattern any defined pattern.");
                    continue;
                }
            }
        } catch (final RuntimeException e) {
            LOG.error("Could not parse dateTimeInUTC: " + dateTimeInUTC + " with pattern any defined pattern.");
            return null;
        }
        LOG.error("Could not parse dateTimeInUTC: " + dateTimeInUTC + " with pattern any defined pattern.");
        return null;
    }


    public static String parseUTCDateToString(final Date date) {
        String dateString = null;
        if (date != null) {
            final DateFormat df = new SimpleDateFormat(DateFormats.FORMAT.getFormat());
            dateString = df.format(date);
        }
        return dateString;
    }

    public static XMLGregorianCalendar addSecondsToDate(final XMLGregorianCalendar inDate, final int seconds) {
        final Date date = DateUtil.parsePositionTime(inDate);
        final DateTime newDateTime = new DateTime(date);
        final DateTime plusSeconds = newDateTime.plusSeconds(1);
        return parsePositionTime(plusSeconds.toDate());
    }

    public static Date addSecondsToDate(final Date inDate, final int seconds) {
        final DateTime newDateTime = new DateTime(inDate);
        final DateTime plusSeconds = newDateTime.plusSeconds(1);
        return plusSeconds.toDate();
    }

    public static Date parsePositionTime(final XMLGregorianCalendar positionTime) {
        if (positionTime != null) {
            final DateTimeZone localTZ = DateTimeZone.getDefault();
            final long eventMillsInUTCTimeZone = localTZ.convertLocalToUTC(positionTime.toGregorianCalendar().getTime().getTime(), false);
            final DateTime evenDateTimeInUTCTimeZone = new DateTime(eventMillsInUTCTimeZone);
            return evenDateTimeInUTCTimeZone.toDate();
        }
        return null;
    }

    public static XMLGregorianCalendar parsePositionTime(final Date timestamp) {
        if (timestamp != null) {
            final GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(timestamp);
            XMLGregorianCalendar xmlCalendar = null;
            try {
                xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
            } catch (final DatatypeConfigurationException ex) {
            }
            return xmlCalendar;
        } else {
            return null;
        }
    }

    public static Date nowUTC(){
        return new DateTime(DateTimeZone.UTC).toLocalDateTime().toDate();
    }

    public static Date convertDateTimeInUTC(final String dateTimeInUTC){
        for (final DateFormats format : DateFormats.values()) {
            final Date date = convertDateTimeInUTC(dateTimeInUTC, format.getFormat());
            if (date != null) {
                return date;
            }
        }
        LOG.error("Could not parse dateTimeInUTC: " + dateTimeInUTC + " with pattern any defined pattern.");
        return null;
    }

    public static Date convertDateTimeInUTC(final String dateTimeInUTC, final String pattern){
        if (dateTimeInUTC != null) {
            final SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            try {
                final Date theDate = sdf.parse(dateTimeInUTC);
                final DateTimeZone localTZ = DateTimeZone.getDefault();
                final long eventMillsInUTCTimeZone = localTZ.convertLocalToUTC(theDate.getTime(), false);
                final DateTime evenDateTimeInUTCTimeZone = new DateTime(eventMillsInUTCTimeZone);
                return evenDateTimeInUTCTimeZone.toDate();
            } catch (final java.text.ParseException e) {
                LOG.info("Could not parse dateTimeInUTC: " + dateTimeInUTC + " with pattern: " + pattern + ". Trying next pattern");
            }
        }
        return null;
    }

    public static XMLGregorianCalendar getXMLGregorianCalendarInUTC(final Date dateTimeInUTC){
        if (dateTimeInUTC != null) {
            final GregorianCalendar calendar = (GregorianCalendar) Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            final SimpleDateFormat sdf = new SimpleDateFormat(DateFormats.DATE_TIME_PATTERN_UTC.getFormat());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                final Date theDate = sdf.parse(dateTimeInUTC.toString());
                calendar.setTime(theDate);
                return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
            } catch (final DatatypeConfigurationException e) {
                LOG.error("[ Error when getting XML Gregorian calendar. ] ", e);
            } catch (final ParseException e) {
                LOG.error("Could not parse dateTimeInUTC: "+dateTimeInUTC.toString()+ " with pattern: " + DateFormats.DATE_TIME_PATTERN_UTC.getFormat());
            }
        }
        return null;
    }


}