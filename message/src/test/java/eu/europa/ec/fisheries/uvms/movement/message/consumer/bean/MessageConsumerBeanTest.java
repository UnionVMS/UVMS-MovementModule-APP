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

import eu.europa.ec.fisheries.schema.movement.module.v1.CreateMovementRequest;
import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.uvms.movement.message.event.carrier.EventMessage;
import eu.europa.ec.fisheries.uvms.movement.model.exception.ModelMarshallException;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.MovementModuleRequestMapper;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.enterprise.event.Event;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import org.junit.*;

import static org.junit.Assert.*;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

/**
 **/
public class MessageConsumerBeanTest {

    @Mock
    Event<EventMessage> createMovementEvent;

    @Mock
    Event<EventMessage> createMovementBatchEvent;

    @Mock
    Event<EventMessage> getMovementByQueryEvent;

    @Mock
    Event<EventMessage> getMovementListByQueryEvent;

    @Mock
    Event<EventMessage> pingEvent;

    @Mock
    Event<EventMessage> errorEvent;

    @InjectMocks
    MessageConsumerBean beanToTest;

    HashMap<String, Event<EventMessage>> events = new HashMap<>();

    public MessageConsumerBeanTest() {
        MockitoAnnotations.initMocks(this);

        events.put("pingEvent", pingEvent);
        events.put("errorEvent", errorEvent);
        events.put("getMovementListByQueryEvent", getMovementListByQueryEvent);
        events.put("getMovementByQueryEvent", getMovementByQueryEvent);
        events.put("createMovementBatchEvent", createMovementBatchEvent);
        events.put("createMovementEvent", createMovementEvent);

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

    // TODO: Cause exception because cannot be cast message to javax.jms.TextMessage
    @Ignore
    @Test
    public void testObjectMessageDoesNotWork() {
        ObjectMessage objectMessage = mock(ObjectMessage.class);
        assertNotNull(objectMessage);
        beanToTest.onMessage(objectMessage);
        verifyOnlyOneEventFired("errorEvent");
    }

    // TODO: Cause exception because cannot be cast message to javax.jms.TextMessage
    @Ignore
    @Test
    public void testStreamMessageDoesNotWork() {
        StreamMessage streamMessage = mock(StreamMessage.class);
        assertNotNull(streamMessage);
        beanToTest.onMessage(streamMessage);
        verifyOnlyOneEventFired("errorEvent");
    }

    // TODO: Cause exception because cannot be cast message to javax.jms.TextMessage
    @Ignore
    @Test
    public void testBytesMessageDoesNotWork() {
        BytesMessage bytesMessage = mock(BytesMessage.class);
        assertNotNull(bytesMessage);
        beanToTest.onMessage(bytesMessage);
        verifyOnlyOneEventFired("errorEvent");
    }

    // TODO: Cause exception because cannot be cast message to javax.jms.TextMessage
    @Ignore
    @Test
    public void testMapMessageDoesNotWork() {
        MapMessage mapMessage = mock(MapMessage.class);
        assertNotNull(mapMessage);
        beanToTest.onMessage(mapMessage);
        verifyOnlyOneEventFired("errorEvent");
    }
    // TODO: Cause exception because cannot be cast message to javax.jms.TextMessage
    @Ignore
    @Test
    public void testFaultyBaseRequest() throws JMSException {
        TextMessage message = mock(TextMessage.class);
        when(message.getText()).thenReturn("BalonyData");
        assertNotNull(message);
        beanToTest.onMessage(message);
        verifyOnlyOneEventFired("errorEvent");
    }

    @Test
    public void testNoMethodEnumInBaseRequestExists() throws JMSException, ModelMarshallException {
        String mapToCreateMovementRequest = mapToCreateMovementRequestWithoutMethod(null);
        TextMessage message = mock(TextMessage.class);
        when(message.getText()).thenReturn(mapToCreateMovementRequest);
        assertNotNull(message);
        beanToTest.onMessage(message);
        verifyOnlyOneEventFired("errorEvent");
    }

    @Test
    public void testMovementList() throws JMSException, ModelMarshallException {
        TextMessage message = mock(TextMessage.class);
        String request = MovementModuleRequestMapper.mapToGetMovementListByQueryRequest(null);
        when(message.getText()).thenReturn(request);
        assertNotNull(message);
        beanToTest.onMessage(message);
        verifyOnlyOneEventFired("getMovementListByQueryEvent");
    }

    @Test
    public void testMovementCreate() throws JMSException, ModelMarshallException {
        TextMessage message = mock(TextMessage.class);
        String request = MovementModuleRequestMapper.mapToCreateMovementRequest(null, "TEST");
        when(message.getText()).thenReturn(request);
        assertNotNull(message);
        beanToTest.onMessage(message);
        verifyOnlyOneEventFired("createMovementEvent");
    }

    @Test
    public void testMovementCreateBatch() throws JMSException, ModelMarshallException {
        TextMessage message = mock(TextMessage.class);
        String request = MovementModuleRequestMapper.mapToCreateMovementBatchRequest(null);
        when(message.getText()).thenReturn(request);
        assertNotNull(message);
        beanToTest.onMessage(message);
        verifyOnlyOneEventFired("createMovementBatchEvent");
    }

    @Test
    public void testMovementGetMap() throws JMSException, ModelMarshallException {
        TextMessage message = mock(TextMessage.class);
        String request = MovementModuleRequestMapper.mapToGetMovementMapByQueryRequest(null);
        when(message.getText()).thenReturn(request);
        assertNotNull(message);
        beanToTest.onMessage(message);
        verifyOnlyOneEventFired("getMovementByQueryEvent");
    }

    @Test
    public void testPing() throws JMSException, ModelMarshallException {
        TextMessage message = mock(TextMessage.class);
        String request = MovementModuleRequestMapper.mapToPingRequest(null);
        when(message.getText()).thenReturn(request);
        assertNotNull(message);
        beanToTest.onMessage(message);
        verifyOnlyOneEventFired("pingEvent");
    }

    private void verifyOnlyOneEventFired(String eventToFire) {
        for (Entry<String, Event<EventMessage>> entry : events.entrySet()) {
            if (!entry.getKey().equals(eventToFire)) {
                verify(entry.getValue(), never()).fire(any(EventMessage.class));
            } else {
                verify(entry.getValue(), times(1)).fire(any(EventMessage.class));
            }
        }
    }

    public static String mapToCreateMovementRequestWithoutMethod(MovementBaseType baseType) throws ModelMarshallException {
        CreateMovementRequest request = new CreateMovementRequest();
        request.setMovement(baseType);
        return JAXBMarshaller.marshallJaxBObjectToString(request);
    }

}