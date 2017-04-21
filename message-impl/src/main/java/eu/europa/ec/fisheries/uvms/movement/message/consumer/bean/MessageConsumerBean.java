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

import javax.ejb.*;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import eu.europa.ec.fisheries.schema.movement.module.v1.CreateMovementRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.GetMovementListByQueryRequest;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.movement.message.event.*;
import eu.europa.ec.fisheries.uvms.movement.message.exception.MovementMessageException;
import eu.europa.ec.fisheries.uvms.movement.message.producer.MessageProducer;
import eu.europa.ec.fisheries.uvms.movement.model.exception.ModelMarshallException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDuplicateException;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.MovementModuleResponseMapper;
import eu.europa.ec.fisheries.uvms.movement.service.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
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
    @CreateMovementBatchEvent
    Event<EventMessage> createMovementBatchEvent;

    @Inject
    @GetMovementMapByQueryEvent
    Event<EventMessage> getMovementByQueryEvent;

    @Inject
    @PingEvent
    Event<EventMessage> pingEvent;

    @Inject
    @GetMovementListByAreaAndTimeIntervalEvent
    Event<EventMessage> getMovementListByAreaAndTimeIntervalEvent;

    @EJB
    private MovementService movementService;

    @EJB
    MessageProducer messageProducer;

    @Inject
    @ErrorEvent
    Event<EventMessage> errorEvent;

    @Override
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
                    getMovementListByQuery(textMessage);
                    break;
                case CREATE:
                    createMovement(textMessage);
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

    private void getMovementListByQuery(TextMessage textMessage) {
        try {
            GetMovementListByQueryRequest request = JAXBMarshaller.unmarshallTextMessage(textMessage, GetMovementListByQueryRequest.class);
            GetMovementListByQueryResponse movementList = movementService.getList(request.getQuery());
            String responseString = MovementModuleResponseMapper.mapTogetMovementListByQueryResponse(movementList.getMovement());
            messageProducer.sendMessageBackToRecipient(textMessage, responseString);

        } catch (MovementDuplicateException | ModelMarshallException | MovementMessageException | MovementServiceException ex) {
            LOG.error("[ Error when creating movement ] ", ex);
            EventMessage eventMessage = new EventMessage(textMessage, ex.getMessage());
            errorEvent.fire(eventMessage);
            // TODO: Rollback local tx (but still send error message to client), retries of JMS will NOT GIVE CORRECT FEEDBACK TO CLIENT AT THIS POINT
            throw new EJBException(ex);
        }
    }

    private void createMovement(TextMessage textMessage) throws ModelMarshallException {
        CreateMovementRequest createMovementRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, CreateMovementRequest.class);
        try {
            MovementType createdMovement = movementService.createMovement(createMovementRequest.getMovement(), createMovementRequest.getUsername());
            String responseString = MovementModuleResponseMapper.mapToCreateMovementResponse(createdMovement);
            messageProducer.sendMessageBackToRecipient(textMessage, responseString);

        } catch (MovementDuplicateException ex) {
            LOG.error("[ Error when creating movement ] ", ex);
            //409 is used to create a duplicate exception later in call chain.
            EventMessage eventMessage = new EventMessage(textMessage, "409");
            errorEvent.fire(eventMessage);
            // TODO: Rollback local tx (but still send error message to client), retries of JMS will NOT GIVE CORRECT FEEDBACK TO CLIENT AT THIS POINT
            throw new EJBException(ex);
        } catch (MovementServiceException | MovementMessageException ex) {
            LOG.error("[ Error when creating movement ] ", ex);
            EventMessage eventMessage = new EventMessage(textMessage, ex.getMessage());
            errorEvent.fire(eventMessage);
            throw new EJBException(ex);
        }
    }

}
