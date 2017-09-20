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
package eu.europa.ec.fisheries.uvms.movement.mapper.search;

/**
 **/
public class SearchValue {

    private boolean range = false;

    private SearchField field;
    private String value;
    private String toValue;
    private String fromValue;

    public SearchValue(final SearchField field, final String value) {
        this.field = field;
        this.value = value;
    }

    public SearchValue(final SearchField field, final String fromValue, final String toValue) {
        range = true;
        this.field = field;
        this.fromValue = fromValue;
        this.toValue = toValue;
    }

    public SearchField getField() {
        return field;
    }

    public void setField(final SearchField field) {
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public boolean isRange() {
        return range;
    }

    public void setRange(final boolean range) {
        this.range = range;
    }

    public String getToValue() {
        return toValue;
    }

    public void setToValue(final String toValue) {
        this.toValue = toValue;
    }

    public String getFromValue() {
        return fromValue;
    }

    public void setFromValue(final String fromValue) {
        this.fromValue = fromValue;
    }

}