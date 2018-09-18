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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;
import eu.europa.ec.fisheries.schema.movement.module.v1.CreateMovementResponse;
import eu.europa.ec.fisheries.schema.movement.module.v1.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.module.v1.PingResponse;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.MovementModuleRequestMapper;

public class JMSHelper {

    private static final long TIMEOUT = 20000;
    private static final String MOVEMENT_QUEUE = "UVMSMovementEvent";
    private static final String RESPONSE_QUEUE = "MovementTestQueue";

    private ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

    public PingResponse pingMovement() throws Exception {
        String pingRequest = MovementModuleRequestMapper.mapToPingRequest(null);
        String correlationId = sendMovementMessage(pingRequest);
        Message response = listenForResponse(correlationId);
        return JAXBMarshaller.unmarshallTextMessage((TextMessage) response, PingResponse.class);
    }

    public CreateMovementResponse createMovement(MovementBaseType movementBaseType, String username) throws Exception {
        String request = MovementModuleRequestMapper.mapToCreateMovementRequest(movementBaseType, username);
        String correlationId = sendMovementMessage(request);
        Message response = listenForResponse(correlationId);
        return JAXBMarshaller.unmarshallTextMessage((TextMessage) response, CreateMovementResponse.class);
    }
    
    public GetMovementListByQueryResponse getMovementListByQuery(MovementQuery movementQuery) throws Exception {
        String request = MovementModuleRequestMapper.mapToGetMovementListByQueryRequest(movementQuery);
        String correlationId = sendMovementMessage(request);
        Message response = listenForResponse(correlationId);
        return JAXBMarshaller.unmarshallTextMessage((TextMessage) response, GetMovementListByQueryResponse.class);
    }
    
    public String sendMovementMessage(String text) throws Exception {
        Connection connection = connectionFactory.createConnection();
        try {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue responseQueue = session.createQueue(RESPONSE_QUEUE);
            Queue assetQueue = session.createQueue(MOVEMENT_QUEUE);

            TextMessage message = session.createTextMessage();
            message.setJMSReplyTo(responseQueue);
            message.setText(text);

            session.createProducer(assetQueue).send(message);

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
        } finally {
            connection.close();
        }
    }
}