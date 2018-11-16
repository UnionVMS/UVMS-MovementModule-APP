package eu.europa.ec.fisheries.uvms.movement.message.consumer.bean;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.jms.*;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.europa.ec.fisheries.schema.movement.search.v1.*;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;
import eu.europa.ec.fisheries.uvms.movement.service.entity.IncomingMovement;
import eu.europa.ec.fisheries.uvms.movementrules.model.dto.MovementDetails;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.schema.movement.module.v1.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.module.v1.PingResponse;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.movement.message.BuildMovementServiceTestDeployment;
import eu.europa.ec.fisheries.uvms.movement.message.JMSHelper;
import eu.europa.ec.fisheries.uvms.movement.message.MovementTestHelper;

@RunWith(Arquillian.class)
public class MovementMessageConsumerBeanTest extends BuildMovementServiceTestDeployment {

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    private ObjectMapper mapper = new ObjectMapper();

    JMSHelper jmsHelper;

    @PostConstruct
    public void init() {
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    }

    @Before
    public void cleanJMS() throws Exception {
        jmsHelper = new JMSHelper(connectionFactory);
        jmsHelper.clearQueue("UVMSMovementRulesEvent");
    }

    @Test
    @OperateOnDeployment("movement")
    public void pingMovement() throws Exception {
        PingResponse pingResponse = jmsHelper.pingMovement();
        assertThat(pingResponse.getResponse(), is("pong"));
    }

    @Test
    @OperateOnDeployment("movement")
    public void createMovementConcurrentProcessing() throws Exception {
        int numberOfPositions = 20;
        String connectId = UUID.randomUUID().toString();

        Instant timestamp = Instant.now().minusSeconds(3600);

        // Send positions to movement
        for (int i = 0; i < numberOfPositions; i++) {
            IncomingMovement im = MovementTestHelper.createIncomingMovement(0d,0d);
            im.setAssetHistoryId(connectId);
            im.setPositionTime(timestamp);
            timestamp = timestamp.plusSeconds(10);
            String json = mapper.writeValueAsString(im);
            jmsHelper.sendMovementMessage(json, connectId, "CREATE");
        }

        Instant maxTime = Instant.now().plusSeconds(30);
        while(jmsHelper.checkQueueSize(MessageConstants.QUEUE_EXCHANGE_EVENT_NAME) < 20) {
            if(Instant.now().isAfter(maxTime)) {
                break;
            }
            Thread.sleep(100);
        }

        MovementQuery query = MovementTestHelper.createMovementQuery(true, false, false);
        query.getPagination().setListSize(BigInteger.valueOf(100l));
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CONNECT_ID);
        criteria.setValue(connectId);
        query.getMovementSearchCriteria().add(criteria);

        GetMovementListByQueryResponse movementResponse = jmsHelper.getMovementListByQuery(query, connectId);
        List<MovementType> movements = movementResponse.getMovement();

        movements.sort((m1, m2) -> m1.getPositionTime().compareTo(m2.getPositionTime()));
        MovementType previous = null;
        for (MovementType movementType : movements) {
            if (previous == null) {
                previous = movementType;
            } else {
                assertFalse(Collections.disjoint(previous.getSegmentIds(), movementType.getSegmentIds()));
                previous = movementType;
            }
        }
    }

    @Test
    @OperateOnDeployment("movement")
    public void createMovementVerifyBasicData() throws Exception {
        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetGuid(null);
        incomingMovement.setAssetHistoryId(null);
        MovementDetails movementDetails = sendIncomingMovementAndWaitForResponse(incomingMovement);

        assertNotNull(movementDetails);
        assertNotNull(movementDetails.getMovementGuid());
        assertNotNull(movementDetails.getAssetGuid());
        assertNotNull(movementDetails.getConnectId());

        assertThat(movementDetails.getLongitude(), is(incomingMovement.getLongitude()));
        assertThat(movementDetails.getLatitude(), is(incomingMovement.getLatitude()));
        assertEquals(movementDetails.getPositionTime().getTime(), incomingMovement.getPositionTime().toEpochMilli());
    }
    

    @Test
    @OperateOnDeployment("movement")
    public void createMovementVerifyAllBaseTypeData() throws Exception {
        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetGuid(null);
        incomingMovement.setAssetHistoryId(null);
        MovementDetails movementDetails = sendIncomingMovementAndWaitForResponse(incomingMovement);

        assertThat(movementDetails.getMovementGuid(), is(notNullValue()));
        assertNotNull(movementDetails.getConnectId());
        assertNotNull(movementDetails.getAssetGuid());

        assertThat(movementDetails.getLongitude(), is(incomingMovement.getLongitude()));
        assertThat(movementDetails.getLatitude(), is(incomingMovement.getLatitude()));
        assertEquals(incomingMovement.getAltitude(), movementDetails.getAltitude());
        assertEquals(movementDetails.getPositionTime().getTime(), incomingMovement.getPositionTime().toEpochMilli());
        assertThat(movementDetails.getStatusCode(), is(incomingMovement.getStatus()));
        assertThat(movementDetails.getReportedSpeed(), is(incomingMovement.getReportedSpeed()));
        assertThat(movementDetails.getReportedCourse(), is(incomingMovement.getReportedCourse()));
        assertThat(movementDetails.getMovementType(), is(incomingMovement.getMovementType()));
        assertThat(movementDetails.getSource(), is(incomingMovement.getMovementSourceType()));
        assertThat(movementDetails.getActivityMessageId(), is(incomingMovement.getActivityMessageId()));
        assertThat(movementDetails.getTripNumber(), is(incomingMovement.getTripNumber()));
        assertThat(movementDetails.getInternalReferenceNumber(), is(incomingMovement.getInternalReferenceNumber()));
    }


    @Test
    @OperateOnDeployment("movement")
    public void createMovementVerifyNullAltitudeData() throws Exception {
        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetGuid(null);
        incomingMovement.setAssetHistoryId(null);
        incomingMovement.setAltitude(null);
        MovementDetails movementDetails = sendIncomingMovementAndWaitForResponse(incomingMovement);
        assertThat(movementDetails.getMovementGuid(), is(notNullValue()));
        assertThat(movementDetails.getAltitude(), is(incomingMovement.getAltitude()));
    }


    @Test
    @OperateOnDeployment("movement")
    public void createMovementVerifyWKTData() throws Exception {
        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetGuid(null);
        incomingMovement.setAssetHistoryId(null);
        MovementDetails movementDetails = sendIncomingMovementAndWaitForResponse(incomingMovement);
        assertThat(movementDetails.getMovementGuid(), is(notNullValue()));
        assertThat(movementDetails.getWkt(), is(notNullValue()));
    }


    @Test
    @Ignore   //TODO: we need to say to movement that these two movements are on the same ship
    @OperateOnDeployment("movement")
    public void createMovementVerifyCalculatedData() throws Exception {
        /*String connectId = UUID.randomUUID().toString();

        MovementBaseType movementBaseType = MovementTestHelper.createIncomingMovementType(0d, 1d);
        movementBaseType.setAssetHistoryId(connectId);
        movementBaseType.setPositionTime(Date.from(Instant.now().minusSeconds(10)));
        jmsHelper.createMovement(movementBaseType, "test user");
        
        MovementBaseType movementBaseType2 = MovementTestHelper.createIncomingMovementType(0d, 2d);
        movementBaseType2.setAssetHistoryId(connectId);
        CreateMovementResponse response = jmsHelper.createMovement(movementBaseType2, "test user");
        MovementType createdMovement = response.getMovement();
        
        assertThat(createdMovement.getGuid(), is(notNullValue()));
        assertThat(createdMovement.getCalculatedSpeed(), is(notNullValue()));
        assertThat(createdMovement.getCalculatedCourse(), is(notNullValue()));*/
    }


    @Test
    @Ignore   //TODO: we need to say to movement that these two movements are on the same ship
    @OperateOnDeployment("movement")
    public void createMovementVerifyBasicSegment() throws Exception {
        /*JMSHelper jmsHelper = new JMSHelper(connectionFactory);
        String connectId = UUID.randomUUID().toString();
        
        MovementBaseType movementBaseType1 = MovementTestHelper.createIncomingMovementType(0d, 0d);
        movementBaseType1.setPositionTime(Date.from(Instant.now().minusSeconds(10)));
        movementBaseType1.setAssetHistoryId(connectId);
        jmsHelper.createMovement(movementBaseType1, "test user");
        
        MovementBaseType movementBaseType2 = MovementTestHelper.createIncomingMovementType(0d, 1d);
        movementBaseType2.setAssetHistoryId(connectId);

        //System.out.println("Now");
        //Thread.sleep(1000 * 60 * 5);
        jmsHelper.createMovement(movementBaseType2, "test user");
        
        MovementQuery query = MovementTestHelper.createMovementQuery(true, false, false);
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CONNECT_ID);
        criteria.setValue(connectId);
        query.getMovementSearchCriteria().add(criteria);
        GetMovementListByQueryResponse movementList = jmsHelper.getMovementListByQuery(query, connectId);
        List<MovementType> movements = movementList.getMovement();
        
        assertThat(movements.size(), is(2));
        assertThat(movements.get(0).getSegmentIds(), is(movements.get(1).getSegmentIds()));
        */
    }

    
    @Test
    @Ignore   //TODO: This one needs create batch functionality
    @OperateOnDeployment("movement")
    public void createMovementBatchTest() throws Exception {
        /*
        JMSHelper jmsHelper = new JMSHelper(connectionFactory);
        MovementBaseType m1 = MovementTestHelper.createIncomingMovementType(0d, 0d);
        MovementBaseType m2 = MovementTestHelper.createIncomingMovementType(0d, 1d);
        MovementBaseType m3 = MovementTestHelper.createIncomingMovementType(0d, 2d);
        CreateMovementBatchResponse response = jmsHelper.createMovementBatch(Arrays.asList(m1, m2, m3), "test user");
        List<MovementType> createdMovement = response.getMovements();
        assertThat(createdMovement.size(), is(3));*/
    }


    @Test
    @OperateOnDeployment("movement")
    public void getMovementListByConnectId() throws Exception {
        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetGuid(null);
        incomingMovement.setAssetHistoryId(null);
        MovementDetails movementDetails = sendIncomingMovementAndWaitForResponse(incomingMovement);


        MovementQuery query = MovementTestHelper.createMovementQuery(true, false, false);
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CONNECT_ID);
        criteria.setValue(movementDetails.getConnectId());
        query.getMovementSearchCriteria().add(criteria);

        GetMovementListByQueryResponse listByQueryResponse = jmsHelper.getMovementListByQuery(query, movementDetails.getConnectId());
        List<MovementType> movements = listByQueryResponse.getMovement();
        assertThat(movements.size(), is(1));
        assertThat(movements.get(0).getConnectId(), is(movementDetails.getConnectId()));
        assertThat(movements.get(0).getPosition().getLongitude(), is(incomingMovement.getLongitude()));
        assertThat(movements.get(0).getPosition().getLatitude(), is(incomingMovement.getLatitude()));
        assertThat(movements.get(0).getPositionTime().getTime(), is(incomingMovement.getPositionTime().toEpochMilli()));
    }


    @Test
    @Ignore  //TODO: This test needs the two incoming movements to be on the same ship
    @OperateOnDeployment("movement")
    public void getMovementListByConnectIdTwoPositions() throws Exception {
      /*  JMSHelper jmsHelper = new JMSHelper(connectionFactory);
        String connectId = UUID.randomUUID().toString();
        Instant timestamp = Instant.now();

        MovementBaseType movementBaseType1 = MovementTestHelper.createIncomingMovementType();
        movementBaseType1.setAssetHistoryId(connectId);
        movementBaseType1.setPositionTime(Date.from(timestamp));
        jmsHelper.createMovement(movementBaseType1, "test user");
        
        MovementBaseType movementBaseType2 = MovementTestHelper.createIncomingMovementType();
        movementBaseType2.setAssetHistoryId(connectId);
        movementBaseType1.setPositionTime(Date.from(timestamp.plusSeconds(10)));
        jmsHelper.createMovement(movementBaseType2, "test user");

        MovementQuery query = MovementTestHelper.createMovementQuery(true, false, false);
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CONNECT_ID);
        criteria.setValue(connectId);
        query.getMovementSearchCriteria().add(criteria);

        GetMovementListByQueryResponse listByQueryResponse = jmsHelper.getMovementListByQuery(query, connectId);
        List<MovementType> movements = listByQueryResponse.getMovement();
        assertThat(movements.size(), is(2));*/
    }

    @Test
    @OperateOnDeployment("movement")
    public void getMovementListByConnectIdDifferentIds() throws Exception {
        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetGuid(null);
        incomingMovement.setAssetHistoryId(null);
        MovementDetails movementDetails = sendIncomingMovementAndWaitForResponse(incomingMovement);

        MovementDetails movementDetails2 = sendIncomingMovementAndWaitForResponse(incomingMovement);


        MovementQuery query = MovementTestHelper.createMovementQuery(true, false, false);
        ListCriteria criteria1 = new ListCriteria();
        criteria1.setKey(SearchKey.CONNECT_ID);
        criteria1.setValue(movementDetails.getConnectId());
        query.getMovementSearchCriteria().add(criteria1);
        ListCriteria criteria2 = new ListCriteria();
        criteria2.setKey(SearchKey.CONNECT_ID);
        criteria2.setValue(movementDetails2.getConnectId());
        query.getMovementSearchCriteria().add(criteria2);

        GetMovementListByQueryResponse listByQueryResponse = jmsHelper.getMovementListByQuery(query, "Grouping");
        List<MovementType> movements = listByQueryResponse.getMovement();
        assertThat(movements.size(), is(2));
    }
    

    @Ignore //TODO: This should work when query searches guid instead of id
    @Test
    @OperateOnDeployment("movement")
    public void getMovementListByMovementId() throws Exception {
        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetGuid(null);
        incomingMovement.setAssetHistoryId(null);
        MovementDetails movementDetails = sendIncomingMovementAndWaitForResponse(incomingMovement);

        MovementQuery query = MovementTestHelper.createMovementQuery(true, false, false);
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.MOVEMENT_ID);
        criteria.setValue(movementDetails.getMovementGuid());
        query.getMovementSearchCriteria().add(criteria);
        
        GetMovementListByQueryResponse listByQueryResponse = jmsHelper.getMovementListByQuery(query, "Grouping");
        List<MovementType> movements = listByQueryResponse.getMovement();
        assertThat(movements.size(), is(1));
        assertThat(movements.get(0).getConnectId(), is(movementDetails.getConnectId()));
        assertThat(movements.get(0).getPosition().getLongitude(), is(movementDetails.getLongitude()));
        assertThat(movements.get(0).getPosition().getLatitude(), is(movementDetails.getLatitude()));
        assertThat(movements.get(0).getPositionTime().getTime(), is(movementDetails.getPositionTime().getTime()));
    }
    
    @Ignore //TODO: This should work when query searches guid instead of id
    @Test
    @OperateOnDeployment("movement")
    public void getMovementListByMovementIdTwoMovements() throws Exception {
        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetGuid(null);
        incomingMovement.setAssetHistoryId(null);
        MovementDetails movementDetails = sendIncomingMovementAndWaitForResponse(incomingMovement);

        IncomingMovement incomingMovement2 = MovementTestHelper.createIncomingMovement(3d, 4d);
        incomingMovement2.setAssetGuid(null);
        incomingMovement2.setAssetHistoryId(null);
        MovementDetails movementDetails2 = sendIncomingMovementAndWaitForResponse(incomingMovement2);

        MovementQuery query = MovementTestHelper.createMovementQuery(true, false, false);
        ListCriteria criteria1 = new ListCriteria();
        criteria1.setKey(SearchKey.MOVEMENT_ID);
        criteria1.setValue(movementDetails.getMovementGuid());
        query.getMovementSearchCriteria().add(criteria1);
        ListCriteria criteria2 = new ListCriteria();
        criteria2.setKey(SearchKey.MOVEMENT_ID);
        criteria2.setValue(movementDetails2.getMovementGuid());
        query.getMovementSearchCriteria().add(criteria2);
        
        GetMovementListByQueryResponse listByQueryResponse = jmsHelper.getMovementListByQuery(query, "Grouping");
        List<MovementType> movements = listByQueryResponse.getMovement();
        assertThat(movements.size(), is(2));
    }
    
    @Test
    @OperateOnDeployment("movement")
    public void getMovementListByDateFromRange() throws Exception {
        Instant timestampBefore = Instant.now().minusSeconds(1);

        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetGuid(null);
        incomingMovement.setAssetHistoryId(null);
        MovementDetails movementDetails = sendIncomingMovementAndWaitForResponse(incomingMovement);
        
        Instant timestampAfter = Instant.now().plusSeconds(1);

        MovementQuery query = MovementTestHelper.createMovementQuery(true, false, false);
        RangeCriteria criteria = new RangeCriteria();
        criteria.setKey(RangeKeyType.DATE);
        criteria.setFrom(DateUtil.parseUTCDateToString(timestampBefore));
        criteria.setTo(DateUtil.parseUTCDateToString(timestampAfter));
        query.getMovementRangeSearchCriteria().add(criteria);
        ListCriteria criteria1 = new ListCriteria();
        criteria1.setKey(SearchKey.CONNECT_ID);
        criteria1.setValue(movementDetails.getConnectId());
        query.getMovementSearchCriteria().add(criteria1);
        
        GetMovementListByQueryResponse listByQueryResponse = jmsHelper.getMovementListByQuery(query, "Grouping");
        List<MovementType> movements = listByQueryResponse.getMovement();
        assertThat(movements.size(), is(1));
        assertThat(movements.get(0).getConnectId(), is(movementDetails.getConnectId()));
        assertThat(movements.get(0).getPosition().getLongitude(), is(movementDetails.getLongitude()));
        assertThat(movements.get(0).getPosition().getLatitude(), is(movementDetails.getLatitude()));
        assertThat(movements.get(0).getPositionTime(), is(movementDetails.getPositionTime()));
    }


    @Test
    @OperateOnDeployment("movement")
    public void getMovementListByDateTwoMovements() throws Exception {
        Instant timestampBefore = Instant.now().minusSeconds(60);

        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetGuid(null);
        incomingMovement.setAssetHistoryId(null);
        MovementDetails movementDetails = sendIncomingMovementAndWaitForResponse(incomingMovement);

        IncomingMovement incomingMovement2 = MovementTestHelper.createIncomingMovementType();
        incomingMovement2.setAssetGuid(null);
        incomingMovement2.setAssetHistoryId(null);
        MovementDetails movementDetails2 = sendIncomingMovementAndWaitForResponse(incomingMovement2);

        Instant timestampAfter = Instant.now().plusSeconds(60);
        
        MovementQuery query = MovementTestHelper.createMovementQuery(true, false, false);
        RangeCriteria criteria = new RangeCriteria();
        criteria.setKey(RangeKeyType.DATE);
        criteria.setFrom(DateUtil.parseUTCDateToString(timestampBefore));
        criteria.setTo(DateUtil.parseUTCDateToString(timestampAfter));
        query.getMovementRangeSearchCriteria().add(criteria);
        ListCriteria criteria1 = new ListCriteria();
        criteria1.setKey(SearchKey.CONNECT_ID);
        criteria1.setValue(movementDetails.getConnectId());
        query.getMovementSearchCriteria().add(criteria1);
        ListCriteria criteria2 = new ListCriteria();
        criteria2.setKey(SearchKey.CONNECT_ID);
        criteria2.setValue(movementDetails2.getConnectId());
        query.getMovementSearchCriteria().add(criteria2);
        
        GetMovementListByQueryResponse listByQueryResponse = jmsHelper.getMovementListByQuery(query, "Grouping");
        List<MovementType> movements = listByQueryResponse.getMovement();
        assertThat(movements.size(), is(2));
    }



    @Test
    @Ignore  //TODO: This test needs to be able to set so that two different movements have the same ship
    @OperateOnDeployment("movement")
    public void createMovementConcurrentProcessingTwoConnectIds() throws Exception {
        int numberOfPositions = 20;
        String connectId1 = UUID.randomUUID().toString();   //these two needs to be connected to the movemetns so that they are are treated as the same ship
        String connectId2 = UUID.randomUUID().toString();
        
        List<String> correlationIds = new ArrayList<>();
        
        Instant timestamp = Instant.now();
        
        // Send positions to movement
        for (int i = 0; i < numberOfPositions; i++) {
            IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
            incomingMovement.setAssetGuid(null);
            incomingMovement.setAssetHistoryId(null);
            incomingMovement.setPositionTime(timestamp);
            String json = mapper.writeValueAsString(incomingMovement);
            jmsHelper.sendMovementMessage(json, "Grouping:1", "CREATE");

            IncomingMovement incomingMovement2 = MovementTestHelper.createIncomingMovementType();
            incomingMovement2.setAssetGuid(null);
            incomingMovement2.setAssetHistoryId(null);
            incomingMovement2.setPositionTime(timestamp);
            String json2 = mapper.writeValueAsString(incomingMovement2);
            jmsHelper.sendMovementMessage(json2, "Grouping:2", "CREATE");


            timestamp = timestamp.plusSeconds(10);
        }


        // Check responses
        for (int i = 0; i < numberOfPositions; i++) {
            TextMessage response = (TextMessage) jmsHelper.listenOnMRQueue();
            String jsonResponse = response.getText();
            MovementDetails movementDetails = mapper.readValue(jsonResponse, MovementDetails.class);

            response = (TextMessage) jmsHelper.listenOnMRQueue();
            jsonResponse = response.getText();
            movementDetails = mapper.readValue(jsonResponse, MovementDetails.class);
        }
        
        MovementQuery query = MovementTestHelper.createMovementQuery(true, false, false);
        query.getPagination().setListSize(BigInteger.valueOf(100l));
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CONNECT_ID);
        criteria.setValue(connectId1);
        query.getMovementSearchCriteria().add(criteria);

        GetMovementListByQueryResponse movementResponse = jmsHelper.getMovementListByQuery(query, connectId1);
        List<MovementType> movements = movementResponse.getMovement();

        movements.sort((m1, m2) -> m1.getPositionTime().compareTo(m2.getPositionTime()));
        MovementType previous = null;
        for (MovementType movementType : movements) {
            if (previous == null) {
                previous = movementType;
            } else {
                assertFalse(Collections.disjoint(previous.getSegmentIds(), movementType.getSegmentIds()));
                previous = movementType;
            }
        }
        
        MovementQuery query2 = MovementTestHelper.createMovementQuery(true, false, false);
        query2.getPagination().setListSize(BigInteger.valueOf(100l));
        ListCriteria criteria2 = new ListCriteria();
        criteria2.setKey(SearchKey.CONNECT_ID);
        criteria2.setValue(connectId2);
        query2.getMovementSearchCriteria().add(criteria2);

        GetMovementListByQueryResponse movementResponse2 = jmsHelper.getMovementListByQuery(query2, connectId2);
        List<MovementType> movements2 = movementResponse2.getMovement();

        movements2.sort((m1, m2) -> m1.getPositionTime().compareTo(m2.getPositionTime()));
        MovementType previous2 = null;
        for (MovementType movementType : movements2) {
            if (previous2 == null) {
                previous2 = movementType;
            } else {
                assertFalse(Collections.disjoint(previous2.getSegmentIds(), movementType.getSegmentIds()));
                previous2 = movementType;
            }
        }
    }

    @Test
    @OperateOnDeployment("movement")
    public void testMaxRedeliveries() throws Exception {
        int dlqBefore = jmsHelper.checkQueueSize("ActiveMQ.DLQ");
        int responseQueueBefore = jmsHelper.checkQueueSize(JMSHelper.RESPONSE_QUEUE);

        IncomingMovement incomingMovement = MovementTestHelper.createIncomingMovementType();
        incomingMovement.setAssetGuid(null);
        incomingMovement.setAssetHistoryId(null);
        incomingMovement.setPluginType(null);
        String json = mapper.writeValueAsString(incomingMovement);
        jmsHelper.sendMovementMessage(json, incomingMovement.getAssetHistoryId(), "CREATE");   //grouping on null.....


        // Wait until message have been moved to DQL
        Thread.sleep(2500);
        
        int dlqAfter = jmsHelper.checkQueueSize("ActiveMQ.DLQ");
        int responseQueueAfter = jmsHelper.checkQueueSize(JMSHelper.RESPONSE_QUEUE);
        
        assertThat(dlqAfter, is(dlqBefore + 1));
        assertThat(responseQueueBefore, is(responseQueueAfter));
    }

    private MovementDetails sendIncomingMovementAndWaitForResponse(IncomingMovement incomingMovement) throws Exception{
        String json = mapper.writeValueAsString(incomingMovement);
        jmsHelper.sendMovementMessage(json, incomingMovement.getAssetHistoryId(), "CREATE");   //grouping on null.....

        TextMessage response = (TextMessage) jmsHelper.listenOnMRQueue();
        String jsonResponse = response.getText();
        MovementDetails movementDetails = mapper.readValue(jsonResponse, MovementDetails.class);
        return movementDetails;
    }
}
