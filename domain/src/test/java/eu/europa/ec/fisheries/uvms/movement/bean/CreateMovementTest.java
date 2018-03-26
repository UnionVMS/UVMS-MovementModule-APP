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
package eu.europa.ec.fisheries.uvms.movement.bean;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.movement.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.MovementDaoMappingException;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.MovementConnect;
import eu.europa.ec.fisheries.uvms.movement.exception.GeometryUtilException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDaoException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.util.Date;

import static org.mockito.Mockito.when;

public class CreateMovementTest {

    @Mock
    private MovementDao dao;

    @InjectMocks
    private MovementDomainModelBean model;

    @Before
    public void setup() throws MovementDaoException {
        MockitoAnnotations.initMocks(this);

        Movement prevMovement = new Movement();
        Movement currentMovement = new Movement();

        //when(dao.getLatestMovement(any(String.class), any(Date.class))).thenReturn(prevMovement);
        //when(dao.getMovementConnectByConnectId(any(String.class))).thenReturn(getMovementConnect());
        //when(dao.craete(currentMovement)).thenReturn(currentMovement);
    }

    @Test
    public void createMovementFirstMovememt() throws GeometryUtilException, MovementDaoMappingException, MovementDaoException {

        MovementBatchModelBean test = Mockito.mock(MovementBatchModelBean.class);
        IncomingMovementBean proc = Mockito.mock(IncomingMovementBean.class);

        when(dao.create(Matchers.any()))
                .thenReturn(getMovementConnect())
                .thenReturn(getMovement(eu.europa.ec.fisheries.uvms.movement.util.DateUtil.nowUTC(), 1, 2));

        MovementType createMovement = createMovement(eu.europa.ec.fisheries.uvms.movement.util.DateUtil.nowUTC(), 1, 2);
        MovementType created = test.createMovement(createMovement, "TEST");

        //verify(model, atLeast(2)).splitSegment(any(Movement.class), any(Movement.class));
        //verify(model, times(3)).splitSegment(any(Movement.class), any(Movement.class));
        //verify(test, times(1)).getMovementConnect(any(String.class));
        //verify(proc, never()).splitSegment(any(Movement.class), any(Movement.class));
        //verify(proc, never()).addMovementBeforeFirst(any(Movement.class), any(Movement.class));
    }

    private Movement getMovement(Date timeStamp, double loong, double lat) {
        Movement movement = new Movement();
        movement.setTimestamp(timeStamp);

        Coordinate coordinate = new Coordinate(loong, lat);
        GeometryFactory factory = new GeometryFactory();
        Point point = factory.createPoint(coordinate);
        point.setSRID(4326);
        movement.setLocation(point);

        return movement;
    }

    private MovementConnect getMovementConnect() {
        MovementConnect connect = new MovementConnect();
        connect.setValue("GUID");
        return connect;
    }

    private MovementType createMovement(Date timeStamp, double loong, double lat) {
        MovementType mock = new MovementType();
        mock.setPositionTime(timeStamp);
        MovementPoint point = new MovementPoint();
        point.setLatitude(lat);
        point.setLongitude(loong);
        mock.setPosition(point);
        return mock;
    }

}
