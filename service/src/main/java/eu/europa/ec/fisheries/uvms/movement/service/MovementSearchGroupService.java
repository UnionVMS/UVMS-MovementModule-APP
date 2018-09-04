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
package eu.europa.ec.fisheries.uvms.movement.service;

import java.util.List;

import javax.ejb.Local;

import eu.europa.ec.fisheries.schema.movement.search.v1.MovementSearchGroup;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;

@Local
public interface MovementSearchGroupService {

    MovementSearchGroup createMovementSearchGroup(MovementSearchGroup data, String username) throws MovementServiceException;

    MovementSearchGroup getMovementSearchGroup(Long id) throws MovementServiceException;

    List<MovementSearchGroup> getMovementSearchGroupsByUser(String user) throws MovementServiceException;

    MovementSearchGroup updateMovementSearchGroup(MovementSearchGroup searchGroup, String username) throws MovementServiceException;

    MovementSearchGroup deleteMovementSearchGroup(Long id) throws MovementServiceException;

}
