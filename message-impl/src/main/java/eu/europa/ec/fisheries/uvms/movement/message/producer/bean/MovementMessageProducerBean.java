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
package eu.europa.ec.fisheries.uvms.movement.message.producer.bean;

import eu.europa.ec.fisheries.schema.movement.common.v1.ExceptionType;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.commons.message.impl.AbstractProducer;
import eu.europa.ec.fisheries.uvms.commons.message.impl.JMSUtils;
import eu.europa.ec.fisheries.uvms.config.constants.ConfigHelper;
import eu.europa.ec.fisheries.uvms.movement.message.constants.ModuleQueue;
import eu.europa.ec.fisheries.uvms.movement.message.event.ErrorEvent;
import eu.europa.ec.fisheries.uvms.movement.message.event.carrier.EventMessage;
import eu.europa.ec.fisheries.uvms.movement.message.exception.MovementMessageException;
import eu.europa.ec.fisheries.uvms.movement.message.producer.MessageProducer;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.JAXBMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;
import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.jms.Topic;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Stateless
public class MovementMessageProducerBean extends AbstractProducer implements MessageProducer {

    private static final Logger LOG = LoggerFactory.getLogger(MovementMessageProducerBean.class);

    @EJB
    private ConfigHelper configHelper;

    private Queue auditQueue;
    private Queue spatialQueue;
    private Queue exchangeQueue;
    private Queue configQueue;
    private Queue userQueue;
    private Queue subscriptionDataQueue;
    private Queue rulesQueue;
    private Topic movementTopic;

    @PostConstruct
    public void init() {
        auditQueue = JMSUtils.lookupQueue(MessageConstants.QUEUE_AUDIT_EVENT);
        exchangeQueue = JMSUtils.lookupQueue(MessageConstants.QUEUE_EXCHANGE_EVENT);
        configQueue = JMSUtils.lookupQueue(MessageConstants.QUEUE_CONFIG);
        spatialQueue = JMSUtils.lookupQueue(MessageConstants.QUEUE_MODULE_SPATIAL);
        userQueue = JMSUtils.lookupQueue(MessageConstants.QUEUE_USER_IN);
        subscriptionDataQueue = JMSUtils.lookupQueue(MessageConstants.QUEUE_SUBSCRIPTION_DATA);
        rulesQueue = JMSUtils.lookupQueue(MessageConstants.QUEUE_MODULE_RULES);
        movementTopic = JMSUtils.lookupTopic("jms/topic/EventMessageTopic");

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public String sendModuleMessage(String text, ModuleQueue queue) throws MovementMessageException {
        try {
            String corrId;
            final Destination movementQueue = getDestination();
            switch (queue) {
                case AUDIT:
                    corrId = sendMessageToSpecificQueue(text, auditQueue, movementQueue);
                    break;
                case SPATIAL:
                    corrId = sendMessageToSpecificQueue(text, spatialQueue, movementQueue);
                    break;
                case EXCHANGE:
                    corrId = sendMessageToSpecificQueue(text, exchangeQueue, movementQueue);
                    break;
                case CONFIG:
                    corrId = sendMessageToSpecificQueue(text, configQueue, movementQueue);
                    break;
                case USER:
                    corrId = sendMessageToSpecificQueue(text, userQueue, movementQueue);
                    break;
                case SUBSCRIPTION_DATA:
                    corrId = sendMessageToSpecificQueueSameTx(text, subscriptionDataQueue, movementQueue, Collections.singletonMap(MessageConstants.JMS_SUBSCRIPTION_SOURCE_PROPERTY, configHelper.getModuleName()));
                    break;
                case MOVEMENT_TOPIC:
                    Map<String, String> params = new HashMap<>();
                    params.put("ServiceName", "MOVEMENT");
                    params.put(MessageConstants.JMS_SUBSCRIPTION_SOURCE_PROPERTY, configHelper.getModuleName());
                    corrId = sendMessageToSpecificQueueSameTx(text, movementTopic, movementQueue, params);
                    break;
                case RULES:
                    corrId = sendMessageToSpecificQueueSameTx(text, rulesQueue, movementQueue);
                    break;
                default:
                    throw new MovementMessageException("Queue not defined or implemented");
            }
            return corrId;
        } catch (MessageException e) {
            throw new MovementMessageException("Error when sending data source message",e);
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void sendErrorMessageBackToRecipient(@Observes @ErrorEvent EventMessage message) throws MovementMessageException {
        try {
            ExceptionType exception = new ExceptionType();
            int errorCode = 0;
            try {
                errorCode = Integer.parseInt(message.getErrorMessage());
            } catch (NumberFormatException e) {
                LOG.error("[ERROR] NumberFormatException while truying to parse int from errorMessage!",e);
            }
            exception.setCode(errorCode);
            exception.setFault(message.getErrorMessage());
            String text = JAXBMarshaller.marshallJaxBObjectToString(exception);
            sendResponseMessageToSender(message.getJmsMessage(), text);
        } catch (MessageException | MovementModelException e) {
            throw new MovementMessageException("[ Error when sending message. ]", e);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void sendMessageBackToRecipient(TextMessage requestMessage, String returnMessage) throws MovementMessageException {
        try {
            sendResponseMessageToSender(requestMessage, returnMessage);
        } catch (Exception e) {
            throw new MovementMessageException("[ Error when sending message. ]", e);
        }
    }

    @Override
    public String getDestinationName() {
        return MessageConstants.QUEUE_MOVEMENT;
    }
}
