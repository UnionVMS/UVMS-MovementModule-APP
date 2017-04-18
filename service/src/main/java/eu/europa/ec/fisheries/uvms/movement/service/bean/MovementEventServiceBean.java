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

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import eu.europa.ec.fisheries.schema.movement.module.v1.*;
import eu.europa.ec.fisheries.schema.movement.module.v1.GetMovementListByAreaAndTimeIntervalRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.GetMovementMapByQueryRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.MovementBaseRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.PingResponse;
import eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementMapByQueryResponse;
import eu.europa.ec.fisheries.uvms.movement.message.event.*;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDuplicateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.uvms.movement.message.event.carrier.EventMessage;
import eu.europa.ec.fisheries.uvms.movement.message.exception.MovementMessageException;
import eu.europa.ec.fisheries.uvms.movement.message.producer.MessageProducer;
import eu.europa.ec.fisheries.uvms.movement.model.exception.ModelMarshallException;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.MovementModuleResponseMapper;
import eu.europa.ec.fisheries.uvms.movement.service.EventService;
import eu.europa.ec.fisheries.uvms.movement.service.MovementService;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;

/**
 **/
@LocalBean
@Stateless
public class MovementEventServiceBean implements EventService {

    final static Logger LOG = LoggerFactory.getLogger(MovementEventServiceBean.class);

    @Inject
    @ErrorEvent
    Event<EventMessage> errorEvent;

    @EJB
    MessageProducer messageProducer;

    @EJB
    MovementService movementService;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void getMovementMapByQuery(@Observes @GetMovementMapByQueryEvent EventMessage message) {
        LOG.info("Get Movement By Query Received.. processing request in MovementEventServiceBean");
        try {

            MovementBaseRequest baseRequest = JAXBMarshaller.unmarshallTextMessage(message.getJmsMessage(), MovementBaseRequest.class);

            if (baseRequest.getMethod() != MovementModuleMethod.MOVEMENT_MAP) {
                message.setErrorMessage(" [ Error, Get Movement By Query invoked but it is not the intended method, caller is trying: " + baseRequest.getMethod().name() + " ]");
                errorEvent.fire(message);
            }

            GetMovementMapByQueryRequest request = JAXBMarshaller.unmarshallTextMessage(message.getJmsMessage(), GetMovementMapByQueryRequest.class);
            GetMovementMapByQueryResponse movementList = movementService.getMapByQuery(request.getQuery());
            String responseString = MovementModuleResponseMapper.mapToMovementMapResponse(movementList.getMovementMap());
            messageProducer.sendMessageBackToRecipient(message.getJmsMessage(), responseString);

        } catch (MovementDuplicateException | ModelMarshallException | MovementMessageException | MovementServiceException ex) {
            LOG.error("[ Error when creating movement ] ", ex);
            errorEvent.fire(message);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void ping(@Observes @PingEvent EventMessage message) {
        try {
            PingResponse pingResponse = new PingResponse();
            pingResponse.setResponse("pong");
            messageProducer.sendMessageBackToRecipient(message.getJmsMessage(), JAXBMarshaller.marshallJaxBObjectToString(pingResponse));
        } catch (ModelMarshallException | MovementMessageException e) {
            LOG.error("[ Error when responding to ping. ] ", e);
            errorEvent.fire(message);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void getMovementListByAreaAndTimeInterval(@Observes @GetMovementListByAreaAndTimeIntervalEvent EventMessage message) {
        LOG.info("Get Movement By Query Received.. processing request in MovementEventServiceBean");
        try {
            MovementBaseRequest baseRequest = JAXBMarshaller.unmarshallTextMessage(message.getJmsMessage(), MovementBaseRequest.class);
            if (baseRequest.getMethod() != MovementModuleMethod.MOVEMENT_LIST_BY_AREA_TIME_INTERVAL) {
                message.setErrorMessage(" [ Error, Get Movement by area and time interval invoked but it is not the intended method, caller is trying: " + baseRequest.getMethod().name() + " ]");
                errorEvent.fire(message);
            }
            GetMovementListByAreaAndTimeIntervalRequest request = JAXBMarshaller.unmarshallTextMessage(message.getJmsMessage(), GetMovementListByAreaAndTimeIntervalRequest.class);
            eu.europa.ec.fisheries.schema.movement.source.v1.GetMovementListByAreaAndTimeIntervalResponse response = movementService.getMovementListByAreaAndTimeInterval(request.getMovementAreaAndTimeIntervalCriteria());
            String responseString = MovementModuleResponseMapper.mapTogetMovementListByAreaAndTimeIntervalResponse(response.getMovement());
            messageProducer.sendMessageBackToRecipient(message.getJmsMessage(), responseString);
        } catch (MovementDuplicateException | ModelMarshallException | MovementMessageException | MovementServiceException ex) {
            LOG.error("[ Error when creating movement ] ", ex);
            errorEvent.fire(message);
        }
    }

}