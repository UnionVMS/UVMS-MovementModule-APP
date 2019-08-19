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
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.TextMessage;

import eu.europa.ec.fisheries.uvms.commons.message.context.MappedDiagnosticContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.schema.movement.module.v1.GetMovementListByQueryRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.GetMovementMapByQueryRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.PingResponse;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementMapByQueryResponse;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.MovementModuleResponseMapper;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementService;

@Stateless
public class MovementEventBean {

    private static final Logger LOG = LoggerFactory.getLogger(MovementEventBean.class);
    
    private static final int MAXIMUM_REDELIVERIES = 6;

    @Inject
    private MovementService movementService;

    @Inject
    @JMSConnectionFactory("java:/JmsXA")
    private JMSContext context;

    @Inject
    @ErrorEvent
    private Event<EventMessage> errorEvent;

    
    public void getMovementListByQuery(TextMessage jmsMessage) {
        try {
            GetMovementListByQueryRequest request = JAXBMarshaller.unmarshallTextMessage(jmsMessage, GetMovementListByQueryRequest.class);
            GetMovementListByQueryResponse movementList = movementService.getList(request.getQuery());
            String responseString = MovementModuleResponseMapper.mapTogetMovementListByQueryResponse(movementList.getMovement());
            sendResponseMessageToSender(jmsMessage, responseString);
            LOG.info("Response sent back to requestor on queue [ {} ]", jmsMessage!= null ? jmsMessage.getJMSReplyTo() : "Null!!!");
        } catch (Exception ex) {
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

            sendResponseMessageToSender(jmsMessage, responseString);
            LOG.info("Response sent back to requestor on queue [ {} ]", jmsMessage!= null ? jmsMessage.getJMSReplyTo() : "Null!!!");
        } catch (Exception ex) {
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
            sendResponseMessageToSender(message, JAXBMarshaller.marshallJaxBObjectToString(
                    pingResponse));
        } catch (Exception e) {
            LOG.error("Could not send ping reply", e);
        }
    }

    public void sendResponseMessageToSender(TextMessage message, String text) throws JMSException {
        TextMessage responseMessage = this.context.createTextMessage(text);
        responseMessage.setJMSCorrelationID(message.getJMSMessageID());
        MappedDiagnosticContext.addThreadMappedDiagnosticContextToMessageProperties(responseMessage);
        this.context.createProducer().send(message.getJMSReplyTo(), responseMessage);
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
