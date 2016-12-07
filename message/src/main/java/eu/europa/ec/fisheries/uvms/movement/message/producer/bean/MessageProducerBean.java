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

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;
import javax.jms.*;

import eu.europa.ec.fisheries.schema.movement.common.v1.ExceptionType;
import eu.europa.ec.fisheries.uvms.config.constants.ConfigConstants;
import eu.europa.ec.fisheries.uvms.config.exception.ConfigMessageException;
import eu.europa.ec.fisheries.uvms.config.message.ConfigMessageProducer;
import eu.europa.ec.fisheries.uvms.movement.message.constants.MessageConstants;
import eu.europa.ec.fisheries.uvms.movement.message.constants.ModuleQueue;
import eu.europa.ec.fisheries.uvms.movement.message.event.ErrorEvent;
import eu.europa.ec.fisheries.uvms.movement.message.event.carrier.EventMessage;
import eu.europa.ec.fisheries.uvms.movement.message.exception.MovementMessageException;
import eu.europa.ec.fisheries.uvms.movement.message.producer.AbstractProducer;
import eu.europa.ec.fisheries.uvms.movement.message.producer.MessageProducer;
import static eu.europa.ec.fisheries.uvms.movement.message.producer.bean.JMSConnectorBean.LOG;
import eu.europa.ec.fisheries.uvms.movement.model.exception.ModelMarshallException;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.JAXBMarshaller;
import javax.ejb.EJB;

@Stateless
public class MessageProducerBean extends AbstractProducer implements MessageProducer, ConfigMessageProducer {
    @Resource(mappedName = MessageConstants.COMPONENT_RESPONSE_QUEUE)
    private Queue responseQueue;

    @Resource(mappedName = MessageConstants.AUDIT_MODULE_QUEUE)
    private Queue auditQueue;

    @Resource(mappedName = MessageConstants.SPATIAL_MODULE_QUEUE)
    private Queue spatialQueue;

    @Resource(mappedName = MessageConstants.EXCHANGE_MODULE_QUEUE)
    private Queue exchangeQueue;

    @Resource(mappedName = ConfigConstants.CONFIG_MESSAGE_IN_QUEUE)
    private Queue configQueue;

    @Resource(mappedName = MessageConstants.USER_MODULE_QUEUE)
    private Queue userQueue;

    private static final int CONFIG_TTL = 30000;

    @EJB
    JMSConnectorBean connector;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public String sendModuleMessage(String text, ModuleQueue queue) throws MovementMessageException {
        try {
            Session session = connector.getNewSession();
            TextMessage message = session.createTextMessage();
            message.setJMSReplyTo(responseQueue);
            message.setText(text);
            javax.jms.MessageProducer producer;
            switch (queue) {
                case AUDIT:
                    getProducer(session, auditQueue).send(message);
                    break;
                case SPATIAL:
                    getProducer(session, spatialQueue).send(message);
                    break;
                case EXCHANGE:
                    getProducer(session, exchangeQueue).send(message);
                    break;
                case CONFIG:
                    getProducer(session, configQueue).send(message);
                    break;
                case USER:
                    getProducer(session, userQueue).send(message);
                    break;
                default:
                    throw new MovementMessageException("Queue not defined or implemented");
            }

            return message.getJMSMessageID();
        } catch (Exception e) {
            LOG.error("[ Error when sending data source message. ] {}", e.getMessage());
            throw new MovementMessageException(e.getMessage());
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void sendErrorMessageBackToRecipient(@Observes @ErrorEvent EventMessage message) throws MovementMessageException {
        try {
            ExceptionType exception = new ExceptionType();
            int errorCode = 0;
            try {
                errorCode = Integer.parseInt(message.getErrorMessage());
            } catch (NumberFormatException e){

            }
            exception.setCode(errorCode);
            exception.setFault(message.getErrorMessage());

            String text = JAXBMarshaller.marshallJaxBObjectToString(exception);
            sendMessage(message.getJmsMessage().getJMSReplyTo(), text, message.getJmsMessage().getJMSMessageID());
        } catch (JMSException | ModelMarshallException e) {
            LOG.error("[ Error when sending message. ] {}", e.getMessage());
            throw new MovementMessageException("[ Error when sending message. ]", e);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void sendMessageBackToRecipient(TextMessage requestMessage, String returnMessage) throws MovementMessageException {
        try {
            sendMessage(requestMessage.getJMSReplyTo(), returnMessage, requestMessage.getJMSMessageID());
        } catch (Exception e) {
            LOG.error("[ Error when sending message. ] {}", e.getMessage());
            throw new MovementMessageException("[ Error when sending message. ]", e);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public String sendConfigMessage(String text) throws ConfigMessageException {
        try {
            return sendModuleMessage(text, ModuleQueue.CONFIG);
        } catch (MovementMessageException e) {
            LOG.error("[ Error when sending config message. ] {}", e.getMessage());
            throw new ConfigMessageException("[ Error when sending config message. ]");
        }
    }

    private javax.jms.MessageProducer getProducer(Session session, Destination destination) throws JMSException {
        javax.jms.MessageProducer producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        producer.setTimeToLive(60000L);
        return producer;
    }

}