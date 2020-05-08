/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.movement.service.bean;

import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;

/**
 * Link an incoming {@link MovementBaseType} to the created {@link Movement}.
 * This is to fill-in some data missing from the response,
 * but present in the request, e.g. asset id.
 */
public class MovementAndBaseType {
	private Movement movement;
	private MovementBaseType baseType;

	/**
	 * Default constructor.
	 */
	public MovementAndBaseType() {
		// NOOP
	}

	/**
	 * All args constructor.
	 */
	public MovementAndBaseType(Movement movement, MovementBaseType baseType) {
		this.movement = movement;
		this.baseType = baseType;
	}

	public Movement getMovement() {
		return movement;
	}
	public void setMovement(Movement movement) {
		this.movement = movement;
	}
	public MovementBaseType getBaseType() {
		return baseType;
	}
	public void setBaseType(MovementBaseType baseType) {
		this.baseType = baseType;
	}
}
