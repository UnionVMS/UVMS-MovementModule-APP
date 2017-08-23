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

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.schema.movement.common.v1.ExceptionType;
import eu.europa.ec.fisheries.uvms.config.constants.ConfigConstants;
import eu.europa.ec.fisheries.uvms.config.exception.ConfigMessageException;
import eu.europa.ec.fisheries.uvms.config.message.ConfigMessageProducer;
import eu.europa.ec.fisheries.uvms.message.JMSUtils;
import eu.europa.ec.fisheries.uvms.movement.message.constants.MessageConstants;
import eu.europa.ec.fisheries.uvms.movement.message.constants.ModuleQueue;
import eu.europa.ec.fisheries.uvms.movement.message.event.ErrorEvent;
import eu.europa.ec.fisheries.uvms.movement.message.event.carrier.EventMessage;
import eu.europa.ec.fisheries.uvms.movement.message.exception.MovementMessageException;
import eu.europa.ec.fisheries.uvms.movement.message.producer.MessageProducer;
import eu.europa.ec.fisheries.uvms.movement.model.exception.ModelMarshallException;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.JAXBMarshaller;

@Stateless
public class MessageProducerBean implements MessageProducer, ConfigMessageProducer {
	private Queue responseQueue;
	private Queue auditQueue;
	private Queue spatialQueue;
	private Queue exchangeQueue;
	private Queue configQueue;
	private Queue userQueue;
	private ConnectionFactory connectionFactory;

	final static Logger LOG = LoggerFactory.getLogger(MessageProducerBean.class);

	@PostConstruct
	public void init() {
		connectionFactory = JMSUtils.lookupConnectionFactory();
		responseQueue = JMSUtils.lookupQueue(MessageConstants.COMPONENT_RESPONSE_QUEUE);
		auditQueue = JMSUtils.lookupQueue(MessageConstants.AUDIT_MODULE_QUEUE);
		exchangeQueue = JMSUtils.lookupQueue(MessageConstants.EXCHANGE_MODULE_QUEUE);
		configQueue = JMSUtils.lookupQueue(ConfigConstants.CONFIG_MESSAGE_IN_QUEUE);
		spatialQueue = JMSUtils.lookupQueue(MessageConstants.SPATIAL_MODULE_QUEUE);
		userQueue = JMSUtils.lookupQueue(MessageConstants.USER_MODULE_QUEUE);
	}

	@Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public String sendModuleMessage(String text, ModuleQueue queue) throws MovementMessageException {

		Connection connection = null;

		try {
			connection = connectionFactory.createConnection();
			final Session session = JMSUtils.connectToQueue(connection);

			TextMessage message = session.createTextMessage();
			message.setJMSReplyTo(responseQueue);
			message.setText(text);
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
		} finally {
			JMSUtils.disconnectQueue(connection);
		}
	}

	public void sendErrorMessageBackToRecipient(@Observes @ErrorEvent EventMessage message)
			throws MovementMessageException {
		try {
			ExceptionType exception = new ExceptionType();
			int errorCode = 0;
			try {
				errorCode = Integer.parseInt(message.getErrorMessage());
			} catch (NumberFormatException e) {

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
	public void sendMessageBackToRecipient(TextMessage requestMessage, String returnMessage)
			throws MovementMessageException {
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

	/**
	 * Sends a response message to a reciever. The corralationId is the
	 * JMSMessage id provided in the message this metod responds to.
	 *
	 * @param responseQueue
	 *            The destinsation of the response
	 * @param textMessage
	 *            The actual message as a String representation of an XML
	 * @param correlationId
	 *            The correlationId to set on the message that is returned
	 * @return The JMSMessage id of the sent message
	 * @throws MovementMessageException
	 */
	protected String sendMessage(Destination responseQueue, String textMessage, String correlationId)
			throws MovementMessageException {
		return sendMessage(responseQueue, null, textMessage, correlationId, null, null, null);
	}

	/**
	 *
	 * Sends a message to a JMS destination
	 *
	 * @param toQueue
	 *            The destinsation of the message
	 * @param replyQueue
	 *            The destination that shis message should respond to when
	 *            arriving at the toQueue
	 * @param textMessage
	 *            The actual message as a String representation of an XML
	 * @param correlationId
	 *            The correlationId to set on the message that is returned
	 * @param deliveryMode
	 *            The delivery mode to use
	 * @param defaultPriority
	 *            The priority for this message
	 * @param timetoLive
	 *            The message's lifetime (in milliseconds)
	 * @return The JMSMessage id of the sent message
	 * @throws MovementMessageException
	 */
	private String sendMessage(Destination toQueue, Destination replyQueue, String textMessage, String correlationId,
			Integer deliveryMode, Integer defaultPriority, Long timetoLive) throws MovementMessageException {

		Connection connection = null;
		try {
			connection = connectionFactory.createConnection();
			final Session session = JMSUtils.connectToQueue(connection);

			TextMessage message = session.createTextMessage();
			message.setText(textMessage);
			message.setJMSReplyTo(replyQueue);
			message.setJMSDestination(toQueue);
			message.setJMSCorrelationID(correlationId);

			if (deliveryMode != null && defaultPriority != null && timetoLive != null) {
				getProducer(session, toQueue).send(message, deliveryMode, defaultPriority, timetoLive);
			} else {
				getProducer(session, toQueue).send(message);
			}
			return message.getJMSMessageID();
		} catch (JMSException ex) {
			throw new MovementMessageException("Error when sending message or closing JMS queue", ex);
		} finally {
        	JMSUtils.disconnectQueue(connection);
		}
	}

}