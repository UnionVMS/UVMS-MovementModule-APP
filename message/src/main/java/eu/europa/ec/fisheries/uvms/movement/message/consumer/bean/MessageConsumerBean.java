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

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import eu.europa.ec.fisheries.uvms.movement.message.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.schema.movement.module.v1.MovementBaseRequest;
import eu.europa.ec.fisheries.uvms.movement.message.constants.MessageConstants;
import eu.europa.ec.fisheries.uvms.movement.message.event.carrier.EventMessage;
import eu.europa.ec.fisheries.uvms.movement.model.exception.ModelMapperException;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.JAXBMarshaller;

@MessageDriven(mappedName = MessageConstants.COMPONENT_MESSAGE_IN_QUEUE, activationConfig = {
    @ActivationConfigProperty(propertyName = "messagingType", propertyValue = MessageConstants.CONNECTION_TYPE),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = MessageConstants.DESTINATION_TYPE_QUEUE),
    @ActivationConfigProperty(propertyName = "destination", propertyValue = MessageConstants.COMPONENT_MESSAGE_IN_QUEUE_NAME),
    @ActivationConfigProperty(propertyName = "destinationJndiName", propertyValue = MessageConstants.COMPONENT_MESSAGE_IN_QUEUE),
    @ActivationConfigProperty(propertyName = "connectionFactoryJndiName", propertyValue = MessageConstants.CONNECTION_FACTORY)

})
public class MessageConsumerBean implements MessageListener {

    final static Logger LOG = LoggerFactory.getLogger(MessageConsumerBean.class);

    @Inject
    @CreateMovementEvent
    Event<EventMessage> createMovementEvent;

    @Inject
    @CreateMovementBatchEvent
    Event<EventMessage> createMovementBatchEvent;

    @Inject
    @GetMovementMapByQueryEvent
    Event<EventMessage> getMovementByQueryEvent;

    @Inject
    @GetMovementListByQueryEvent
    Event<EventMessage> getMovementListByQueryEvent;

    @Inject
    @PingEvent
    Event<EventMessage> pingEvent;

    @Inject
    @ErrorEvent
    Event<EventMessage> errorEvent;

    @Inject
    @GetMovementListByAreaAndTimeIntervalEvent
    Event<EventMessage> getMovementListByAreaAndTimeIntervalEvent;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void onMessage(Message message) {

        TextMessage textMessage = null;

        try {

            textMessage = (TextMessage) message;

            LOG.info("Message received in movement");

            MovementBaseRequest request = JAXBMarshaller.unmarshallTextMessage(textMessage, MovementBaseRequest.class);

            if (request.getMethod() == null) {
                LOG.error("[ Request method is null ]");
                errorEvent.fire(new EventMessage(textMessage, "Error when receiving message in movement: "));
                return;
            }

            switch (request.getMethod()) {
                case MOVEMENT_LIST:
                    getMovementListByQueryEvent.fire(new EventMessage(textMessage));
                    break;
                case CREATE:
                    createMovementEvent.fire(new EventMessage(textMessage));
                    break;
                case CREATE_BATCH:
                    createMovementBatchEvent.fire(new EventMessage(textMessage));
                    break;
                case MOVEMENT_MAP:
                    getMovementByQueryEvent.fire(new EventMessage(textMessage));
                    break;
                case PING:
                    pingEvent.fire(new EventMessage(textMessage));
                    break;
                case MOVEMENT_LIST_BY_AREA_TIME_INTERVAL:
                    getMovementListByAreaAndTimeIntervalEvent.fire(new EventMessage(textMessage));
                    break;

                case GET_SEGMENT_BY_ID:
                case GET_TRIP_BY_ID:
                default:
                    LOG.error("[ Request method {} is not implemented ]", request.getMethod().name());
                    errorEvent.fire(new EventMessage(textMessage, "[ Request method " + request.getMethod().name() + "  is not implemented ]"));
            }

        } catch (ModelMapperException | NullPointerException | ClassCastException e) {
            LOG.error("[ Error when receiving message in movement: ] {}", e);
            errorEvent.fire(new EventMessage(textMessage, "Error when receiving message in movement: " + e.getMessage()));
        }
    }

}
