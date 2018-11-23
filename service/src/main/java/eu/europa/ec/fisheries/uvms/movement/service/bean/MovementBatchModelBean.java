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
package eu.europa.ec.fisheries.uvms.movement.service.bean;

import java.util.UUID;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.MovementConnect;

@Stateless
public class MovementBatchModelBean {

    private static final Logger LOG = LoggerFactory.getLogger(MovementBatchModelBean.class);

    @Inject
    private MovementDao movementDao;

    public Movement createMovement(Movement movement) {
        if(movement.getMovementConnect().getValue() == null) {
            throw new IllegalArgumentException("No movementConnect ID");
        }
        UUID connectId = movement.getMovementConnect().getValue();
        try {
            MovementConnect moveConnect = getMovementConnectByConnectId(connectId);

            movement.setMovementConnect(moveConnect);
            return movementDao.createMovement(movement);
        } catch (Exception e) {
            throw new EJBException("Could not create movement.", e);
        }
    }
    
    public MovementConnect getMovementConnectByConnectId(UUID connectId) {
        MovementConnect movementConnect;
        
        if (connectId == null) {
            return null;
        }
        movementConnect = movementDao.getMovementConnectByConnectId(connectId);
        
        if (movementConnect == null) {
            LOG.info("Creating new MovementConnect");
            MovementConnect connect = new MovementConnect();
            connect.setUpdated(DateUtil.nowUTC());
            connect.setUpdatedBy("UVMS");
            connect.setValue(connectId);
            return movementDao.createMovementConnect(connect);
        }
        return movementConnect;
    }
}
