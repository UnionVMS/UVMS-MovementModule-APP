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

import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.TextMessage;

import eu.europa.ec.fisheries.uvms.movement.message.constants.ModuleQueue;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.schema.movement.common.v1.SimpleResponse;
import eu.europa.ec.fisheries.schema.movement.module.v1.CreateMovementBatchRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.CreateMovementBatchResponse;
import eu.europa.ec.fisheries.schema.movement.module.v1.CreateMovementRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.GetMovementListByAreaAndTimeIntervalRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.GetMovementListByQueryRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.GetMovementMapByQueryRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.PingResponse;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementMapByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.uvms.movement.message.event.ErrorEvent;
import eu.europa.ec.fisheries.uvms.movement.message.event.carrier.EventMessage;
import eu.europa.ec.fisheries.uvms.movement.message.exception.MovementMessageException;
import eu.europa.ec.fisheries.uvms.movement.message.producer.MessageProducer;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.MovementModuleResponseMapper;
import eu.europa.ec.fisheries.uvms.movement.service.bean.AuditService;
import eu.europa.ec.fisheries.uvms.movement.service.bean.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementEntityToModelMapper;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementModelToEntityMapper;

@Stateless
public class MovementEventBean {

    private static final Logger LOG = LoggerFactory.getLogger(MovementEventBean.class);
    
    private static final int MAXIMUM_REDELIVERIES = 6;

    @Inject
    private MovementService movementService;

    @Inject
    private MessageProducer messageProducer;
    
    @Inject
    private AuditService auditService;

    @Inject
    @ErrorEvent
    private Event<EventMessage> errorEvent;

    
    public void getMovementListByQuery(EventMessage eventMessage) {
        TextMessage jmsMessage = eventMessage.getJmsMessage();
        try {
            GetMovementListByQueryRequest request =(GetMovementListByQueryRequest) eventMessage.getRequest();
            GetMovementListByQueryResponse movementList = movementService.getList(request.getQuery());
            String responseString = MovementModuleResponseMapper.mapTogetMovementListByQueryResponse(movementList.getMovement());
            messageProducer.sendMessageBackToRecipient(jmsMessage, responseString);
            LOG.info("Response sent back to requestor on queue [ {} ]", jmsMessage!= null ? jmsMessage.getJMSReplyTo() : "Null!!!");
        } catch (MovementModelException | MovementMessageException | MovementServiceException | JMSException ex) {
            LOG.error("[ Error on getMovementListByQuery ] ", ex);
            if (maxRedeliveriesReached(jmsMessage)) {
                errorEvent.fire(new EventMessage(jmsMessage, ex.getMessage()));
            }
            throw new EJBException(ex);
        }
    }
    
    public void createMovement(EventMessage eventMessage) {
        TextMessage jmsMessage = eventMessage.getJmsMessage();
        try {
            CreateMovementRequest createMovementRequest = (CreateMovementRequest) eventMessage.getRequest();
            Movement movement = MovementModelToEntityMapper.mapNewMovementEntity(createMovementRequest.getMovement(), createMovementRequest.getUsername());
            Movement createdMovement = movementService.createMovement(movement);
            String responseString = MovementModuleResponseMapper.mapToCreateMovementResponse(MovementEntityToModelMapper.mapToMovementType(createdMovement));

            messageProducer.sendMessageBackToRecipient(jmsMessage, responseString);
            LOG.info("Response sent back to requestor on queue [ {} ]", jmsMessage!= null ? jmsMessage.getJMSReplyTo() : "Null!!!");
        } catch (EJBException | MovementMessageException | JMSException | MovementModelException | MovementServiceException ex) {
            LOG.error("[ Error when creating movement ] ", ex);
            if (maxRedeliveriesReached(jmsMessage)) {
                errorEvent.fire(new EventMessage(jmsMessage, ex.getMessage()));
            }
            throw new EJBException(ex);
        }
    }
    
    public void createMovementBatch(EventMessage eventMessage) {
        TextMessage jmsMessage = eventMessage.getJmsMessage();
        try {
            CreateMovementBatchRequest request = (CreateMovementBatchRequest) eventMessage.getRequest();
            List<Movement> movements = new ArrayList<>();
            for (MovementBaseType movementBaseType : request.getMovement()) {
                movements.add(MovementModelToEntityMapper.mapNewMovementEntity(movementBaseType, request.getUsername()));
            }
            List<Movement> movementBatch = movementService.createMovementBatch(movements);
            SimpleResponse simpleResponse = CollectionUtils.isNotEmpty(movementBatch) ? SimpleResponse.OK : SimpleResponse.NOK;
            auditService.sendMovementBatchCreatedAudit(simpleResponse.name(), request.getUsername());
            CreateMovementBatchResponse createMovementBatchResponse = new CreateMovementBatchResponse();
            createMovementBatchResponse.setResponse(simpleResponse);
            createMovementBatchResponse.getMovements().addAll(MovementEntityToModelMapper.mapToMovementType(movementBatch));
            String responseString = MovementModuleResponseMapper.mapToCreateMovementBatchResponse(createMovementBatchResponse);
            messageProducer.sendModuleMessage(responseString, ModuleQueue.SUBSCRIPTION_DATA);
            messageProducer.sendMessageBackToRecipient(jmsMessage, responseString);
            LOG.info("Response sent back to requestor on queue [ {} ]", jmsMessage!= null ? jmsMessage.getJMSReplyTo() : "Null!!!");
        } catch (EJBException | MovementMessageException | JMSException | MovementModelException | MovementServiceException ex) {
            LOG.error("[ Error when creating movement batch ] ", ex);
            if (maxRedeliveriesReached(jmsMessage)) {
                errorEvent.fire(new EventMessage(jmsMessage, "Error when receiving message in movement: " + ex.getMessage()));
            }            
            throw new EJBException(ex);
        }
    }
    
    public void getMovementMapByQuery(EventMessage eventMessage) {
        TextMessage jmsMessage = eventMessage.getJmsMessage();
        try {
            LOG.debug("Get Movement By Query Received.. processing request in MovementEventServiceBean");
            GetMovementMapByQueryRequest request = (GetMovementMapByQueryRequest) eventMessage.getRequest();
            GetMovementMapByQueryResponse movementList = movementService.getMapByQuery(request.getQuery());
            String responseString = MovementModuleResponseMapper.mapToMovementMapResponse(movementList.getMovementMap());

            messageProducer.sendMessageBackToRecipient(jmsMessage, responseString);
            LOG.info("Response sent back to requestor on queue [ {} ]", jmsMessage!= null ? jmsMessage.getJMSReplyTo() : "Null!!!");
        } catch (MovementModelException | MovementMessageException | MovementServiceException | JMSException ex) {
            LOG.error("[ Error when creating getMovementMapByQuery ] ", ex);
            if (maxRedeliveriesReached(jmsMessage)) {
                errorEvent.fire(new EventMessage(jmsMessage, ex.getMessage()));
            }
            throw new EJBException(ex);
        }
    }
    
    public void ping(TextMessage message) {
        try {
            PingResponse pingResponse = new PingResponse();
            pingResponse.setResponse("pong");
            messageProducer.sendMessageBackToRecipient(message, JAXBMarshaller.marshallJaxBObjectToString(pingResponse));
        } catch (MovementMessageException | MovementModelException e) {
            LOG.error("[ Error when responding to ping. ] ", e);
            errorEvent.fire(new EventMessage(message, "Error when responding to ping CD ..SSS: " + e.getMessage()));
        }
    }
    
    public void getMovementListByAreaAndTimeInterval(EventMessage eventMessage) {
        TextMessage jmsMessage = eventMessage.getJmsMessage();
        try {
            GetMovementListByAreaAndTimeIntervalRequest request = (GetMovementListByAreaAndTimeIntervalRequest) eventMessage.getRequest();
            eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByAreaAndTimeIntervalResponse response = movementService.getMovementListByAreaAndTimeInterval(request.getMovementAreaAndTimeIntervalCriteria());
            String responseString = MovementModuleResponseMapper.mapTogetMovementListByAreaAndTimeIntervalResponse(response.getMovement());
            messageProducer.sendMessageBackToRecipient(jmsMessage, responseString);
        } catch (MovementMessageException | MovementModelException ex) {
            LOG.error("[ Error in GetMovementListByAreaAndTimeIntervalBean.getMovementListByAreaAndTimeInterval ] ", ex);
            if (maxRedeliveriesReached(jmsMessage)) {
                errorEvent.fire(new EventMessage(jmsMessage, ex.getMessage()));
            }
            throw new EJBException(ex);
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
