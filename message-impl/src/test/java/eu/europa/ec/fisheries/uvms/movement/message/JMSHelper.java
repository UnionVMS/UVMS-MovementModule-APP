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
package eu.europa.ec.fisheries.uvms.movement.message;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import eu.europa.ec.fisheries.schema.movement.module.v1.CreateMovementBatchResponse;
import eu.europa.ec.fisheries.schema.movement.module.v1.CreateMovementResponse;
import eu.europa.ec.fisheries.schema.movement.module.v1.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.module.v1.PingResponse;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.MovementModuleRequestMapper;

public class JMSHelper {

	private static final Logger LOG = LoggerFactory.getLogger(JMSHelper.class);

    private static final long TIMEOUT = 20000;
    private static final String MOVEMENT_QUEUE = "UVMSMovementEvent";
    private static final String PROPERTIES_FILE = "jms.properties";
    private static final String DEFAULT_AMQ_HOST = "activemq";
    private static final String DEFAULT_AMQ_PORT = "61616";
    private static final String DEFAULT_AMQ_USER = "admin";
    private static final String DEFAULT_AMQ_PWD = "admin";
    private Properties properties = new Properties();
    public static final String RESPONSE_QUEUE = "MovementTestQueue";

    private ConnectionFactory connectionFactory;
    
    public JMSHelper() {
    	InputStream is = getClass().getResourceAsStream("/"+PROPERTIES_FILE);
        if (is != null) {
            try {
				properties.load(is);
            } catch (IOException e) {
            	LOG.warn("Failed to load class-path resource:'{}'. Using default values", PROPERTIES_FILE, e);
            }
        } else {
        	LOG.debug("Class-path resource: '{}' does not exist. Using default values", PROPERTIES_FILE);
        }
        
        connectionFactory = new ActiveMQConnectionFactory(
        		properties.getProperty("activemq_user", DEFAULT_AMQ_USER),
        		properties.getProperty("activemq_pwd", DEFAULT_AMQ_PWD),
        		"tcp://"+properties.getProperty("activemq_host", DEFAULT_AMQ_HOST)+
        		":"+ properties.getProperty("activemq_port", DEFAULT_AMQ_PORT));
    }
    

    public PingResponse pingMovement() throws Exception {
        String pingRequest = MovementModuleRequestMapper.mapToPingRequest(null);
        String correlationId = sendMovementMessage(pingRequest, null);
        Message response = listenForResponse(correlationId);
        return JAXBMarshaller.unmarshallTextMessage((TextMessage) response, PingResponse.class);
    }

    public CreateMovementResponse createMovement(MovementBaseType movementBaseType, String username) throws Exception {
        String request = MovementModuleRequestMapper.mapToCreateMovementRequest(movementBaseType, username);
        String correlationId = sendMovementMessage(request, movementBaseType.getConnectId());
        Message response = listenForResponse(correlationId);
        return JAXBMarshaller.unmarshallTextMessage((TextMessage) response, CreateMovementResponse.class);
    }
    
    public CreateMovementBatchResponse createMovementBatch(List<MovementBaseType> movementBaseType, String username) throws Exception {
        String request = MovementModuleRequestMapper.mapToCreateMovementBatchRequest(movementBaseType, username);
        String correlationId = sendMovementMessage(request, movementBaseType.get(0).getConnectId());
        Message response = listenForResponse(correlationId);
        return JAXBMarshaller.unmarshallTextMessage((TextMessage) response, CreateMovementBatchResponse.class);
    }
    
    public GetMovementListByQueryResponse getMovementListByQuery(MovementQuery movementQuery, String groupId) throws Exception {
        String request = MovementModuleRequestMapper.mapToGetMovementListByQueryRequest(movementQuery);
        String correlationId = sendMovementMessage(request, groupId);
        Message response = listenForResponse(correlationId);
        return JAXBMarshaller.unmarshallTextMessage((TextMessage) response, GetMovementListByQueryResponse.class);
    }
    
    public String sendMovementMessage(String text, String groupId) throws Exception {
        Connection connection = connectionFactory.createConnection();
        try {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue responseQueue = session.createQueue(RESPONSE_QUEUE);
            Queue movementQueue = session.createQueue(MOVEMENT_QUEUE);

            TextMessage message = session.createTextMessage();
            message.setStringProperty("JMSXGroupID", groupId);
            message.setJMSReplyTo(responseQueue);
            message.setText(text);

            session.createProducer(movementQueue).send(message);

            return message.getJMSMessageID();
        } finally {
            connection.close();
        }
    }

    public Message listenForResponse(String correlationId) throws Exception {
        Connection connection = connectionFactory.createConnection();
        try {
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue responseQueue = session.createQueue(RESPONSE_QUEUE);

            return session.createConsumer(responseQueue, "JMSCorrelationID='" + correlationId + "'")
                          .receive(TIMEOUT);
        } catch(Exception e) {
            LOG.error("Failed listening to response",e);
            return null;
        } finally {
            connection.close();
        }
    }
    
    public int checkQueueSize(String queue) throws Exception {
        int messages = 0;
        Connection connection = connectionFactory.createConnection();
        try {
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue responseQueue = session.createQueue(queue);

            QueueBrowser browser = session.createBrowser(responseQueue);
            
            Enumeration enumeration = browser.getEnumeration();
            while(enumeration.hasMoreElements()) {
                enumeration.nextElement();
                messages++;
            }
        } finally {
            connection.close();
        }
        return messages;
    }
}
