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
package eu.europa.ec.fisheries.uvms.movement.service.message;

import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.schema.movement.module.v1.GetMovementListByQueryRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.GetMovementMapByQueryRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.PingResponse;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementMapByQueryResponse;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.MovementModuleResponseMapper;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;

@Stateless
public class MovementEventBean {

    private static final Logger LOG = LoggerFactory.getLogger(MovementEventBean.class);
    
    private static final int MAXIMUM_REDELIVERIES = 6;

    @Inject
    private MovementService movementService;

    @Inject
    private MovementMessageProducerBean messageProducer;

    @Inject
    @ErrorEvent
    private Event<EventMessage> errorEvent;

    
    public void getMovementListByQuery(TextMessage jmsMessage) {
        try {
            GetMovementListByQueryRequest request = JAXBMarshaller.unmarshallTextMessage(jmsMessage, GetMovementListByQueryRequest.class);
            GetMovementListByQueryResponse movementList = movementService.getList(request.getQuery());
            String responseString = MovementModuleResponseMapper.mapTogetMovementListByQueryResponse(movementList.getMovement());
            messageProducer.sendMessageBackToRecipient(jmsMessage, responseString);
            LOG.info("Response sent back to requestor on queue [ {} ]", jmsMessage!= null ? jmsMessage.getJMSReplyTo() : "Null!!!");
        } catch (MovementModelException  | MovementServiceException | JMSException ex) {
            LOG.error("[ Error on getMovementListByQuery ] ", ex);
            if (maxRedeliveriesReached(jmsMessage)) {
                EventMessage eventMessage = new EventMessage(jmsMessage, ex.getMessage());
                errorEvent.fire(eventMessage);
            }
            throw new EJBException(ex);
        }
    }
    
    public void getMovementMapByQuery(TextMessage jmsMessage) {
        try {
            LOG.info("Get Movement By Query Received.. processing request in MovementEventServiceBean : {}", jmsMessage.getText());
            GetMovementMapByQueryRequest request = JAXBMarshaller.unmarshallTextMessage(jmsMessage, GetMovementMapByQueryRequest.class);
            GetMovementMapByQueryResponse movementList = movementService.getMapByQuery(request.getQuery());
            String responseString = MovementModuleResponseMapper.mapToMovementMapResponse(movementList.getMovementMap());

            messageProducer.sendMessageBackToRecipient(jmsMessage, responseString);
            LOG.info("Response sent back to requestor on queue [ {} ]", jmsMessage!= null ? jmsMessage.getJMSReplyTo() : "Null!!!");
        } catch (MovementModelException  | MovementServiceException | JMSException ex) {
            LOG.error("[ Error when creating getMovementMapByQuery ] ", ex);
            if (maxRedeliveriesReached(jmsMessage)) {
                EventMessage eventMessage = new EventMessage(jmsMessage, ex.getMessage());
                errorEvent.fire(eventMessage);
            }
            throw new EJBException(ex);
        }
    }
    
    public void ping(TextMessage message) {
        try {
            PingResponse pingResponse = new PingResponse();
            pingResponse.setResponse("pong");
            messageProducer.sendMessageBackToRecipient(message, JAXBMarshaller.marshallJaxBObjectToString(pingResponse));
        } catch (MovementModelException e) {
            LOG.error("[ Error when responding to ping. ] ", e);
            errorEvent.fire(new EventMessage(message, "Error when responding to ping CD ..SSS: " + e.getMessage()));
        }
    }

    
    private boolean maxRedeliveriesReached(TextMessage message) {
        try {
            if (message != null) {
                return message.getIntProperty("JMSXDeliveryCount") > MAXIMUM_REDELIVERIES;
            }
            return false;
        } catch (JMSException e) {
            return false;
        }
    }
}
