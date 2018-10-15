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
package eu.europa.ec.fisheries.uvms.movement.model.mapper;

import javax.xml.bind.DatatypeConverter;
import java.time.LocalDate;
import java.time.Instant;
import java.util.Date;  //leave be for now


public class XsdDateTimeConverter {

    public static Date unmarshal(String dateTime) {
        return DatatypeConverter.parseDate(dateTime).getTime();
    }

    public static String marshalDate(Date date) {
        Instant i = date.toInstant();
        return i.toString();
    }

    public static String marshalDateTime(Date dateTime) {
        Instant i = dateTime.toInstant();
        return i.toString();
    }


    //This is where we want to be, above is where we are
    public static LocalDate unmarshalDate(String v) {
        return LocalDate.parse(v);
    }

    public static Instant unmarshalDateTime(String v) {
        return Instant.parse(v);
    }

    public static String marshalDate(LocalDate v) {
        return v.toString();
    }

    public static String marshalDateTime(Instant v) {
        return v.toString();
    }
}
