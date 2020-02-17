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
package eu.europa.ec.fisheries.uvms.movement.service.message;

import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetId;
import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetIdList;
import eu.europa.ec.fisheries.schema.exchange.movement.asset.v1.AssetIdType;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.*;
import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.uvms.commons.date.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.time.Instant;
import java.util.Date;

/**
 *  code copied  and made standalone - from NAF Plugin
 *
 **/
public class NafMessageResponseMapper {


    public static final String DELIMITER = "//";
    public static final String SUBDELIMITER = "/";
    public static final String START_RECORD = "SR";
    public static final String END_RECORD = "ER";
    public static final String FROM = "FR";
    public static final String TO = "AD";
    public static final String TYPE_OF_MESSAGE = "TM";
    public static final String DATE = "DA";
    public static final String TIME = "TI";
    public static final String INTERNAL_REFERENCE_NUMBER = "IR";
    public static final String FLAG = "FS";
    public static final String RADIO_CALL_SIGN = "RC";
    public static final String VESSEL_NAME = "NA";
    public static final String EXTERNAL_MARK = "XR";
    public static final String LATITUDE_DECIMAL = "LT";
    public static final String LONGITUDE_DECIMAL = "LG";
    public static final String SPEED = "SP";
    public static final String COURSE = "CO";
    public static final String TEST_RECORD = "TEST";
    public static final String ACTIVITY = "AC";
    public static final String IMO_NUMBER = "IM";
    public static final String TRIP_NUMBER = "TN";
    public static final String LATITUDE = "LA";
    public static final String LONGITUDE = "LO";

    
    final static Logger LOG = LoggerFactory.getLogger(NafMessageResponseMapper.class);
    
    static String dateString;
    static String timeString;
    
    public static SetReportMovementType mapToMovementType(String nafMessage, String pluginName) throws Exception {
        dateString = "";
        timeString = "";
        
        SetReportMovementType movementType = new SetReportMovementType();
        if (nafMessage != null) {
            try {
                nafMessage = URLDecoder.decode(nafMessage, "UTF-8");
                String[] parts = nafMessage.split(DELIMITER);
                if (START_RECORD.equals(parts[1]) &&
                        END_RECORD.equals(parts[parts.length - 1])) {
                    MovementBaseType movement = new MovementBaseType();
                    movement.setComChannelType(MovementComChannelType.NAF);
                    movement.setSource(MovementSourceType.NAF);

                    for (int i = 2; i < parts.length - 1; i++) {
                        handleEntry(parts[i], movement);
                    }

                    mapDateTime(movement);

                    movementType.setMovement(movement);
                    movementType.setPluginType(PluginType.NAF);
                    movementType.setPluginName(pluginName);
                    movementType.setTimestamp(new Date());
                }
            } catch (UnsupportedEncodingException e) {
                throw new Exception(e.getMessage());
            }
        }
        return movementType;
    }

    static void handleEntry(String part, MovementBaseType movement) {
        if (part != null) {
            String[] keyValuePair = part.split(SUBDELIMITER);
            if (keyValuePair.length == 2) {
                mapEntry(keyValuePair, movement);
            }
        }
    }

    static void mapEntry(String[] keyValuePair, MovementBaseType movement) throws NumberFormatException {
        String key = keyValuePair[0];
        if (key != null) {
            String value = keyValuePair[1];
            switch (key) {
                case RADIO_CALL_SIGN:
                    mapIRCS(value, movement);
                    break;
                case TRIP_NUMBER:
                    mapTripNumber(value, movement);
                    break;
                case VESSEL_NAME:
                    movement.setAssetName(value);
                    break;
                case INTERNAL_REFERENCE_NUMBER:
                    movement.setInternalReferenceNumber(value);
                    mapCFR(value, movement);
                    break;
                case EXTERNAL_MARK:
                    movement.setExternalMarking(value);
                    break;
                case LATITUDE:
                case LATITUDE_DECIMAL:
                    mapLatitude(movement, value, key);
                    break;
                case LONGITUDE:
                case LONGITUDE_DECIMAL:
                    mapLongitude(movement, value, key);
                    break;
                case SPEED:
                    mapSpeed(movement, value);
                    break;
                case COURSE:
                    movement.setReportedCourse(Double.parseDouble(value));
                    break;
                case DATE:
                    dateString = value;
                    break;
                case TIME:
                    timeString = value;
                    break;
                case ACTIVITY:
                    mapActivity(movement, value);
                case FLAG:
                    movement.setFlagState(value);
                    break;
                case TYPE_OF_MESSAGE:
                    movement.setMovementType(MovementTypeType.valueOf(value));
                case TO:
                default:
                    break;
            }
        }
    }

    static void mapTripNumber(String value, MovementBaseType movement) {
        try {
            Double tripNumber = Double.valueOf(value);
            movement.setTripNumber(tripNumber);
        } catch (NumberFormatException e) {
            LOG.error("Received malformed TN: {}", value);
        }
    }

    static void mapActivity(MovementBaseType movement, String value) {
        MovementActivityType activity = new MovementActivityType();
        activity.setMessageType(MovementActivityTypeType.valueOf(value));
        movement.setActivity(activity);
    }

    static void mapSpeed(MovementBaseType movement, String value) throws NumberFormatException {
        BigDecimal bd = new BigDecimal(Double.valueOf(value) / 10).setScale(4, RoundingMode.HALF_EVEN);
        double speed = bd.doubleValue();
        movement.setReportedSpeed(speed);
    }

    static void mapDateTime(MovementBaseType movement) {
        if ((dateString == null || dateString.isEmpty()) ||
                (timeString == null || timeString.isEmpty())) {
            return;
        }
        while (timeString.length() < 4) {
            timeString = "0" + timeString;
        }
        Instant date = DateUtils.convertDateWithPattern(dateString + " " + timeString + " UTC", DATE_TIME_FORMAT);
        movement.setPositionTime(Date.from(date));
    }

    static void mapIRCS(String value, MovementBaseType movement) {
        AssetId assetId = new AssetId();
        AssetIdList ircs = new AssetIdList();
        ircs.setIdType(AssetIdType.IRCS);
        ircs.setValue(value);
        assetId.getAssetIdList().add(ircs);
        movement.setAssetId(assetId);
        movement.setIrcs(value);
    }

    static void mapCFR(String value, MovementBaseType movement) {
        AssetIdList cfr = new AssetIdList();
        cfr.setIdType(AssetIdType.CFR);
        cfr.setValue(value);
        movement.getAssetId().getAssetIdList().add(cfr);
    }

    static MovementPoint getMovementPoint(MovementBaseType movement) {
        MovementPoint pos = movement.getPosition();
        if (pos == null) {
            pos = new MovementPoint();
        }
        return pos;
    }

    static void mapLongitude(MovementBaseType movement, String value, String key) throws NumberFormatException {
        MovementPoint pos = getMovementPoint(movement);
        if (LONGITUDE_DECIMAL.equals(key)) {
            pos.setLongitude(Double.valueOf(value));
        } else {
            double decimalDegrees = positionStringToDecimalDegrees(value);
            if (value.charAt(0) == 'W') {
                decimalDegrees *= -1;
            }
            getMovementPoint(movement).setLongitude(decimalDegrees);
        }
        movement.setPosition(pos);
    }

    static void mapLatitude(MovementBaseType movement, String value, String key) throws NumberFormatException {
        MovementPoint pos = getMovementPoint(movement);
        if (LATITUDE_DECIMAL.equals(key)) {
            pos.setLatitude(Double.valueOf(value));
        } else {
            double decimalDegrees = positionStringToDecimalDegrees(value);
            if (value.charAt(0) == 'S') {
                decimalDegrees *= -1;
            }
            pos.setLatitude(decimalDegrees);
        }
        movement.setPosition(pos);
    }

    static double positionStringToDecimalDegrees(String value) {
        double deg = (charToDouble(value.charAt(1)) * 10) + charToDouble(value.charAt(2));
        double min = (charToDouble(value.charAt(3)) * 10) + charToDouble(value.charAt(4));
        double decimalDegrees = deg + (min / 60);
        BigDecimal bd = new BigDecimal(decimalDegrees).setScale(4, RoundingMode.HALF_EVEN);
        decimalDegrees = bd.doubleValue();
        return Double.valueOf(decimalDegrees);
    }
    
    static double charToDouble(char val) {
        String str = "" + val;
        return Double.valueOf(str);
    }


    private final static String DATE_TIME_FORMAT = "yyyyMMdd HHmm z";
    /*private static Date parseToUTC(String format, String dateString) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(format);
        DateTime dateTime = formatter.withZoneUTC().parseDateTime(dateString);
        return dateTime.toLocalDateTime().toDate();

    }

    public static Date parseToUTCDateTime(String dateString) {
        return parseToUTC(DATE_TIME_FORMAT, dateString);
    }*/


}