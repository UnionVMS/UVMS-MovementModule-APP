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
package eu.europa.ec.fisheries.uvms.movement.dao;

import java.util.List;

import javax.ejb.Local;

import eu.europa.ec.fisheries.uvms.movement.entity.group.MovementFilterGroup;
import eu.europa.ec.fisheries.uvms.movement.exception.MovementDomainException;

@Local
public interface MovementSearchGroupDao {

    /**
     * Creates a new instance of the filter group in the database.
     * 
     * @param filterGroup a filter group
     * @return the created filter group
     * @throws MovementDomainException if unable to successfully create the filter group
     */
    MovementFilterGroup createMovementFilterGroup(MovementFilterGroup filterGroup) throws MovementDomainException;

    /**
     * @param groupId a group ID of a filter group
     * @return a filter group
     * @throws MovementDomainException if unable to successfully load the filter group from the database
     */
    MovementFilterGroup getMovementFilterGroupById(Integer groupId) throws MovementDomainException;

    /**
     * @param user a user
     * @return all filter groups belonging to the user
     * @throws MovementDomainException if unable to successfully load list of filter groups from the database
     */
    List<MovementFilterGroup> getMovementFilterGroupsByUser(String user) throws MovementDomainException;

    /**
     * Updates the filter group with the same ID.
     * 
     * @param filterGroup a filter group
     * @return the updated filter group
     * @throws MovementDomainException if unable to successfully update the filter group
     */
    MovementFilterGroup updateMovementFilterGroup(MovementFilterGroup filterGroup) throws MovementDomainException;

    /**
     * 
     * @param filterGroup a filter group
     * @return the filter group deleted from the database
     * @throws MovementDomainException if unable to successfully delete the filter group from the database
     */
    MovementFilterGroup deleteMovementFilterGroup(MovementFilterGroup filterGroup) throws MovementDomainException;

}
