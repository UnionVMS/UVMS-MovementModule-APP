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
package eu.europa.ec.fisheries.uvms.movement.message.consumer.bean;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import eu.europa.ec.fisheries.uvms.commons.message.context.PropagateFluxEnvelopeData;
import eu.europa.ec.fisheries.schema.movement.module.v1.MovementBaseRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.MovementModuleMethod;
import eu.europa.ec.fisheries.uvms.movement.message.event.ErrorEvent;
import eu.europa.ec.fisheries.uvms.movement.message.event.carrier.EventMessage;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.JAXBMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MovementMessageConsumerBean implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(MovementMessageConsumerBean.class);

    @Inject
    private MovementEventBean movementEventBean;

    @Inject
    @ErrorEvent
    private Event<EventMessage> errorEvent;

    @Override
    @PropagateFluxEnvelopeData
    public void onMessage(Message message) {
        TextMessage textMessage = null;
        try {
            textMessage = (TextMessage) message;
            MovementBaseRequest request = JAXBMarshaller.unmarshallTextMessage(textMessage, MovementBaseRequest.class);
            MovementModuleMethod movementMethod = request.getMethod();
            LOG.info("Message received in movement with method [ {} ]", movementMethod);
            if (movementMethod == null) {
                LOG.error("[ Request method is null ]");
                errorEvent.fire(new EventMessage(textMessage, "Error when receiving message in movement: "));
                return;
            }
            EventMessage eventMessage = new EventMessage(textMessage,request);

            switch (movementMethod) {
                case MOVEMENT_LIST:
                    movementEventBean.getMovementListByQuery(eventMessage);
                    break;
                case CREATE:
                    movementEventBean.createMovement(eventMessage);
                    break;
                case CREATE_BATCH:
                    movementEventBean.createMovementBatch(eventMessage);
                    break;
                case MOVEMENT_MAP:
                    movementEventBean.getMovementMapByQuery(eventMessage);
                    break;
                case PING:
                    movementEventBean.ping(textMessage);
                    break;
                case MOVEMENT_LIST_BY_AREA_TIME_INTERVAL:
                    movementEventBean.getMovementListByAreaAndTimeInterval(eventMessage);
                    break;
                case FIND_CONNECT_IDS_BY_DATE_AND_GEOMETRY:
                    movementEventBean.findConnectIdsByDateAndGeometry(eventMessage);
                    break;
                case FORWARD_POSITION:
                    movementEventBean.forwardPosition(eventMessage);
                    break;
                case FIND_RAW_MOVEMENTS:
                    movementEventBean.findRawMovements(eventMessage);
                    break;
                case GET_SEGMENT_BY_ID:
                case GET_TRIP_BY_ID:
                default:
                    LOG.error("[ Request method {} is not implemented ]", movementMethod.name());
                    errorEvent.fire(new EventMessage(textMessage, "[ Request method " + movementMethod.name() + "  is not implemented ]"));
            }
        } catch (NullPointerException | ClassCastException | MovementModelException e) {
            LOG.error("[ Error when receiving message in movement: ]", e);
            errorEvent.fire(new EventMessage(textMessage, "Error when receiving message in movement: " + e.getMessage()));
        }
    }
}
