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
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


import org.slf4j.LoggerFactory;

/**
 **/
public class DateUtil {

    final static org.slf4j.Logger LOG = LoggerFactory.getLogger(DateUtil.class);

    final static String FORMAT = "yyyy-MM-dd HH:mm:ss Z";

    /*public static java.sql.Timestamp getDateFromString(String inDate) throws ParseException {
        OffsetDateTime date = parseToUTCDate(inDate);
        return new java.sql.Timestamp(date.getTime());
    }*/

    public static OffsetDateTime addYearToDate(OffsetDateTime date, int years) {
        return date.plusYears(years);
    }

    public static OffsetDateTime removeYearFromDate(OffsetDateTime date, int years) {
        return date.minusYears(years);

    }


    public static String parseUTCDateToString(OffsetDateTime date) {
        if(date == null){
            return null;
        }
        return date.format(DateTimeFormatter.ofPattern(DateFormats.FORMAT.getFormat()));

    }

    /*public static Date parsePositionTime(XMLGregorianCalendar positionTime) {
        if (positionTime != null) {
            return positionTime.toGregorianCalendar().getTime();
        }
        return null;
    }*/

    /*public static XMLGregorianCalendar parsePositionTime(Date timestamp) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(timestamp);
        XMLGregorianCalendar xmlCalendar = null;
        try {
            xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
        } catch (DatatypeConfigurationException ex) {
        }
        return xmlCalendar;
    }*/

    public static OffsetDateTime nowUTC(){
        return OffsetDateTime.now(ZoneId.of("UTC"));
    }

    public static OffsetDateTime getDateFromString(String inDate) throws ParseException {
        return OffsetDateTime.parse(inDate, DateTimeFormatter.ofPattern(DateFormats.FORMAT.getFormat()));

    }

    public static OffsetDateTime parseToUTCDate(String dateTimeInUTC){
        return convertDateTimeInUTC(dateTimeInUTC);
    }


    /*public static XMLGregorianCalendar addSecondsToDate(XMLGregorianCalendar inDate, int seconds) {
        Date date = DateUtil.parsePositionTime(inDate);
        //DateTime newDateTime = new DateTime(date);
        //DateTime plusSeconds = newDateTime.plusSeconds(1);
        Date plusSeconds = new Date(date.getTime() + 1000L); //add one second
        return parsePositionTime(plusSeconds);
    }*/

    public static OffsetDateTime addSecondsToDate(OffsetDateTime inDate, int seconds) {
        return inDate.plusSeconds(seconds);
    }

    /*public static OffsetDateTime parsePositionTime(XMLGregorianCalendar positionTime) {
        if (positionTime != null) {
            return positionTime.toGregorianCalendar().getTime();
        }
        return null;
    }*/

    /*public static XMLGregorianCalendar parsePositionTime(Date timestamp) {
        if (timestamp != null) {
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(timestamp);
            XMLGregorianCalendar xmlCalendar = null;
            try {
                xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
            } catch (DatatypeConfigurationException ex) {
            	LOG.error("Error creating XMLGregorianCalendar with input: " + timestamp.toString() + " Error: " + ex.getMessage());
            	return null;
            }
            return xmlCalendar;
        } else {
            return null;
        }
    }*/


    public static OffsetDateTime convertDateTimeInUTC(String dateTimeInUTC){
        for (DateFormats format : DateFormats.values()) {
            OffsetDateTime date = convertDateTimeInUTC(dateTimeInUTC, format.getFormat());
            if (date != null) {
                return date;
            }
        }
        LOG.error("Could not parse dateTimeInUTC: " + dateTimeInUTC + " with pattern any defined pattern.");
        return null;
    }

    public static OffsetDateTime convertDateTimeInUTC(String dateTimeInUTC, String pattern){
        if (dateTimeInUTC != null) {
            try {
                return OffsetDateTime.parse(dateTimeInUTC, DateTimeFormatter.ofPattern(pattern));
            } catch (DateTimeParseException e) {
                LOG.info("Could not parse dateTimeInUTC: " + dateTimeInUTC + " with pattern: " + pattern + ". Trying next pattern");
            }
        }
        return null;
    }

    /*public static XMLGregorianCalendar getXMLGregorianCalendarInUTC(OffsetDateTime dateTimeInUTC){
        if (dateTimeInUTC != null) {
            //GregorianCalendar calendar = (GregorianCalendar) GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
            //SimpleDateFormat sdf = new SimpleDateFormat(DateFormats.DATE_TIME_PATTERN_UTC.getFormat());
            //sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        	GregorianCalendar output = new GregorianCalendar();
        	output.setTime(dateTimeInUTC);
        	output.setTimeZone(TimeZone.getTimeZone("UTC"));
        	try {
				return DatatypeFactory.newInstance().newXMLGregorianCalendar(output);
			} catch (DatatypeConfigurationException e) {
				// TODO Auto-generated catch block
				LOG.error("[ Error when getting XML Gregorian calendar. ] ", e);
				return null;
			}
            /*try {
                //Date theDate = sdf.parse(dateTimeInUTC.toString());
                //calendar.setTime(theDate);
                //return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
            } catch (DatatypeConfigurationException e) {
                LOG.error("[ Error when getting XML Gregorian calendar. ] ", e);
            } catch (ParseException e) {
                LOG.error("Could not parse dateTimeInUTC: "+dateTimeInUTC.toString()+ " with pattern: " + DateFormats.DATE_TIME_PATTERN_UTC.getFormat());
            }*/
       /* }
        return null;
    }*/
}