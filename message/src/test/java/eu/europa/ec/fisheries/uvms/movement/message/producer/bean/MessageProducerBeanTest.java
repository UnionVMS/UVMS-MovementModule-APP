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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

/**
 **/
public class MessageProducerBeanTest {
/*
    @Mock
    Queue localDbQueue;

    @Mock
    Queue responseQueue;

    @Mock
    Queue auditQueue;

    @Mock
    Queue spatialQueue;

    @Mock
    Queue exchangeQueue;

    @Mock
    Queue configQueue;

    @Mock
    ConnectionFactory connectionFactory;

    @Mock
    Connection connection;

    @Mock
    Session session;


    @InjectMocks
    MessageProducerBean theBeanToTest;

    public MessageProducerBeanTest() {

    }

    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws JMSException {
        MockitoAnnotations.initMocks(this);
        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(session);
    }

    @After
    public void tearDown() {
    }

    /*@Test
     public void testConnectQueue() throws JMSException {
     theBeanToTest.getSession();
     verify(connectionFactory, times(1)).createConnection();
     verify(connection, times(1)).createSession(false, Session.AUTO_ACKNOWLEDGE);
     verify(connection, times(1)).start();
     }

     @Test(expected = MovementMessageException.class)
     public void testdisconnectQueueConnectionNull() throws JMSException, MovementMessageException {
     connection = null;
     theBeanToTest.disconnectQueue(connection);
     }

     @Test(expected = NullPointerException.class)
     public void testdisconnectQueueConnection() throws JMSException, MovementMessageException {
     theBeanToTest.disconnectQueue(connection);
     verify(connection, times(1)).stop();
     verify(connection, times(1)).close();
     }*/
    /**
     * Test of sendDataSourceMessage method, of class MessageProducerBean.
     */
    /* @Test
     public void testSendDataSourceMessage() throws Exception {
     String text = "";
     DataSourceQueue queue = null;
     EJBContainer container = javax.ejb.embeddable.EJBContainer.createEJBContainer();
     MessageProducer instance = (MessageProducer) container.getContext().lookup("java:global/classes/MessageProducerBean");
     String expResult = "";
     String result = instance.sendDataSourceMessage(text, queue);
     assertEquals(expResult, result);
     container.close();
     // TODO review the generated test code and remove the default call to fail.
     fail("The test case is a prototype.");
     }

     /**
     * Test of sendModuleMessage method, of class MessageProducerBean.
     */
    /*  @Test
     public void testSendModuleMessage() throws Exception {
     //System.out.println("sendModuleMessage");
     String text = "";
     ModuleQueue queue = null;
     EJBContainer container = javax.ejb.embeddable.EJBContainer.createEJBContainer();
     MessageProducer instance = (MessageProducer) container.getContext().lookup("java:global/classes/MessageProducerBean");
     String expResult = "";
     String result = instance.sendModuleMessage(text, queue);
     assertEquals(expResult, result);
     container.close();
     // TODO review the generated test code and remove the default call to fail.
     fail("The test case is a prototype.");
     }

     /**
     * Test of sendErrorMessageBackToRecipient method, of class
     * MessageProducerBean.
     */
    /* @Test
     public void testSendErrorMessageBackToRecipient() throws Exception {
     //System.out.println("sendErrorMessageBackToRecipient");
     EventMessage message = null;
     EJBContainer container = javax.ejb.embeddable.EJBContainer.createEJBContainer();
     MessageProducer instance = (MessageProducer) container.getContext().lookup("java:global/classes/MessageProducerBean");
     instance.sendErrorMessageBackToRecipient(message);
     container.close();
     // TODO review the generated test code and remove the default call to fail.
     fail("The test case is a prototype.");
     }

     /**
     * Test of sendMessageBackToRecipient method, of class MessageProducerBean.
     */
    /* @Test
     public void testSendMessageBackToRecipient() throws Exception {
     //System.out.println("sendMessageBackToRecipient");
     TextMessage requestMessage = null;
     String returnMessage = "";
     EJBContainer container = javax.ejb.embeddable.EJBContainer.createEJBContainer();
     MessageProducer instance = (MessageProducer) container.getContext().lookup("java:global/classes/MessageProducerBean");
     instance.sendMessageBackToRecipient(requestMessage, returnMessage);
     container.close();
     // TODO review the generated test code and remove the default call to fail.
     fail("The test case is a prototype.");
     }

     /**
     * Test of sendConfigMessage method, of class MessageProducerBean.
     */
    /*  @Test
     public void testSendConfigMessage() throws Exception {
     //System.out.println("sendConfigMessage");
     String text = "";
     EJBContainer container = javax.ejb.embeddable.EJBContainer.createEJBContainer();
     ConfigMessageProducer instance = (ConfigMessageProducer) container.getContext().lookup("java:global/classes/MessageProducerBean");
     String expResult = "";
     String result = instance.sendConfigMessage(text);
     assertEquals(expResult, result);
     container.close();
     // TODO review the generated test code and remove the default call to fail.
     fail("The test case is a prototype.");
     }

     */
}