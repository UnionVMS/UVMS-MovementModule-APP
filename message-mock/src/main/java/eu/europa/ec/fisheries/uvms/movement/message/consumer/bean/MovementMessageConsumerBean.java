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

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.uvms.commons.message.impl.JMSUtils;
import eu.europa.ec.fisheries.uvms.config.exception.ConfigMessageException;
import eu.europa.ec.fisheries.uvms.config.message.ConfigMessageConsumer;
import eu.europa.ec.fisheries.uvms.movement.message.constants.MessageConstants;
import eu.europa.ec.fisheries.uvms.movement.message.consumer.MessageConsumer;
import eu.europa.ec.fisheries.uvms.movement.message.exception.MovementMessageException;

@Stateless
public class MovementMessageConsumerBean implements MessageConsumer, ConfigMessageConsumer {

    final static Logger LOG = LoggerFactory.getLogger(MovementMessageConsumerBean.class);

    private static final long TIMEOUT = 30000;

    private Queue responseQueue;

    private ConnectionFactory connectionFactory;

    @PostConstruct
    private void init() {
       connectionFactory = JMSUtils.lookupConnectionFactory();
       responseQueue = JMSUtils.lookupQueue(MessageConstants.COMPONENT_RESPONSE_QUEUE);
    }

    @Override
    public <T> T getMessage(String correlationId, Class type, Long timeout) throws MovementMessageException {
    	if (correlationId == null || correlationId.isEmpty()) {
    		LOG.error("[ No CorrelationID provided when listening to JMS message, aborting ]");
    		throw new MovementMessageException("No CorrelationID provided!");
    	}
    	
    	Connection connection=null;
    	try {
    		            
            connection = connectionFactory.createConnection();
            final Session session = JMSUtils.connectToQueue(connection);

            LOG.debug(" Movement module created listener and listens to JMS message with CorrelationID: " + correlationId);
            T response = (T) session.createConsumer(responseQueue, "JMSCorrelationID='" + correlationId + "'").receive(timeout);
            if (response == null) {
                throw new MovementMessageException("[ Timeout reached or message null in MovementMessageConsumerBean. ]");
            }

            return response;
        } catch (Exception e) {
            LOG.error("[ Error when getting message ] {}", e.getMessage());
            throw new MovementMessageException("Error when retrieving message: ", e);
        } finally {
        	JMSUtils.disconnectQueue(connection);
        }
    }

    @Override
    public <T> T getConfigMessage(String correlationId, Class type) throws ConfigMessageException {
        try {
            return getMessage(correlationId, type, TIMEOUT);
        } catch (MovementMessageException e) {
            LOG.error("[ Error when getting message ] {}", e.getMessage());
            throw new ConfigMessageException("Error when retrieving message: ");
        } 
    }

}