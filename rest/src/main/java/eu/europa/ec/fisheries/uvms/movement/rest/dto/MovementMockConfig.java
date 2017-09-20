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
package eu.europa.ec.fisheries.uvms.movement.rest.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovementMockConfig {

    enum MovementConfig {

        STATUS,
        TIME_SPAN,
        MESSAGE_TYPE,
        SPEED_SPAN,
		ACTIVITY_TYPE,
		CATEGORY_TYPE
    }

    public static Map<String, List<String>> getValues() {
        final Map<String, List<String>> configValues = new HashMap<>();
        for (final MovementConfig config : MovementConfig.values()) {
            configValues.put(config.name(), getValues(config));
        }
        return configValues;
    }

    private static List<String> getValues(final MovementConfig config) {
        switch (config) {
            case STATUS:
                return getStatus();
            case TIME_SPAN:
                return getTimeSpan();
            case MESSAGE_TYPE:
                return getMessageType();
            case SPEED_SPAN:
                return getSpeedSpan();
			case ACTIVITY_TYPE:
                return getActivityType();
			case CATEGORY_TYPE:
				return getCategoryType();
        }
        return new ArrayList<>();
    }

    private static List<String> getSpeedSpan() {
        final List<String> values = new ArrayList<>();
        values.add("0-5");
        values.add("6-10");
        values.add("11-15");
        values.add("16-20");
        values.add("21-25");
        return values;
    }

    private static List<String> getMessageType() {
        final List<String> values = new ArrayList<>();
        for (final MovementTypeType type : MovementTypeType.values()) {
            values.add(type.value());
        }
        return values;
    }
	
	private static List<String> getActivityType() {
		final List<String> values = new ArrayList<>();
		for (final MovementActivityTypeType type : MovementActivityTypeType.values()) {
			values.add(type.value());
		}
		return values;
	}
	
	private static List<String> getCategoryType() {
		final List<String> values = new ArrayList<>();
		for (final SegmentCategoryType type : SegmentCategoryType.values()) {
			values.add(type.value());
		}
		return values;
	}

    private static List<String> getStatus() {
        final List<String> values = new ArrayList<>();
        values.add("10");
        values.add("11");
        values.add("30");
        values.add("31");
        values.add("32");
        values.add("33");
        values.add("34");
        values.add("35");
        values.add("36");
        values.add("64");
        values.add("66");
        values.add("68");
        values.add("69");
        values.add("88");
        return values;
    }

    private static List<String> getTimeSpan() {
        final List<String> values = new ArrayList<>();
        values.add("24");
        values.add("48");
        values.add("96");
        return values;
    }

}