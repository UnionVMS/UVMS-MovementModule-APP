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
package eu.europa.ec.fisheries.uvms.movement.message.producer;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Session;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 **/
public class AbstractProducerTest {
    
    public AbstractProducerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of sendMessage method, of class AbstractProducer.
     */
    @Test
    public void testSendMessage_5args() throws Exception {
        /*System.out.println("sendMessage");
        Destination toQueue = null;
        String textMessag = "";
        int deliveryMode = 0;
        int defultPriority = 0;
        long timeToLive = 0L;
        AbstractProducer instance = new AbstractProducerImpl();
        String expResult = "";
        String result = instance.sendMessage(toQueue, textMessag, deliveryMode, defultPriority, timeToLive);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");*/
    }

    /**
     * Test of sendMessage method, of class AbstractProducer.
     */
    @Test
    public void testSendMessage_3args_1() throws Exception {
        /*System.out.println("sendMessage");
        Destination responseQueue = null;
        String textMessage = "";
        String correlationId = "";
        AbstractProducer instance = new AbstractProducerImpl();
        String expResult = "";
        String result = instance.sendMessage(responseQueue, textMessage, correlationId);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");*/
    }

    /**
     * Test of sendMessage method, of class AbstractProducer.
     */
    @Test
    public void testSendMessage_3args_2() throws Exception {
        /*System.out.println("sendMessage");
        Destination toQueue = null;
        Destination replyQueue = null;
        String textMessage = "";
        AbstractProducer instance = new AbstractProducerImpl();
        String expResult = "";
        String result = instance.sendMessage(toQueue, replyQueue, textMessage);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");*/
    }

    /**
     * Test of getSession method, of class AbstractProducer.
     */
    @Test
    public void testGetSession() throws Exception {
        /*System.out.println("getSession");
        Connection connection = null;
        AbstractProducer instance = new AbstractProducerImpl();
        Session expResult = null;
        Session result = instance.getSession(connection);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");*/
    }

    /**
     * Test of createConnection method, of class AbstractProducer.
     */
    @Test
    public void testCreateConnection() throws Exception {
        /*System.out.println("createConnection");
        AbstractProducer instance = new AbstractProducerImpl();
        Connection expResult = null;
        Connection result = instance.createConnection();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");*/
    }

    public class AbstractProducerImpl extends AbstractProducer {
    }
    
}