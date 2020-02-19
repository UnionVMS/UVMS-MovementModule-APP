package eu.europa.ec.fisheries.uvms.movement.message.consumer.bean;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import java.math.BigInteger;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import javax.jms.Message;
import javax.jms.TextMessage;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.schema.movement.common.v1.ExceptionType;
import eu.europa.ec.fisheries.schema.movement.module.v1.CreateMovementBatchResponse;
import eu.europa.ec.fisheries.schema.movement.module.v1.CreateMovementResponse;
import eu.europa.ec.fisheries.schema.movement.module.v1.GetMovementListByQueryResponse;
import eu.europa.ec.fisheries.schema.movement.module.v1.PingResponse;
import eu.europa.ec.fisheries.schema.movement.search.v1.ListCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.MovementQuery;
import eu.europa.ec.fisheries.schema.movement.search.v1.RangeCriteria;
import eu.europa.ec.fisheries.schema.movement.search.v1.RangeKeyType;
import eu.europa.ec.fisheries.schema.movement.search.v1.SearchKey;
import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaData;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.movement.message.BuildMovementServiceTestDeployment;
import eu.europa.ec.fisheries.uvms.movement.message.JMSHelper;
import eu.europa.ec.fisheries.uvms.movement.message.MovementTestHelper;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.MovementModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.movement.model.util.DateUtil;

@RunWith(Arquillian.class)
public class MovementMessageConsumerBeanTest extends BuildMovementServiceTestDeployment {

    private JMSHelper jmsHelper = new JMSHelper();
    
    @Test
    @RunAsClient
    public void pingMovement() throws Exception {
        PingResponse pingResponse = jmsHelper.pingMovement();
        assertThat(pingResponse.getResponse(), is("pong"));
    }
 
    @Test
    @RunAsClient
    public void createMovementVerifyBasicData() throws Exception {
        MovementBaseType movementBaseType = MovementTestHelper.createMovementBaseType();
        CreateMovementResponse response = jmsHelper.createMovement(movementBaseType, "test user");
        MovementType createdMovement = response.getMovement();
        assertThat(createdMovement.getGuid(), is(notNullValue()));
        assertThat(createdMovement.getConnectId(), is(movementBaseType.getConnectId()));
        assertThat(createdMovement.getPosition().getLongitude(), is(movementBaseType.getPosition().getLongitude()));
        assertThat(createdMovement.getPosition().getLatitude(), is(movementBaseType.getPosition().getLatitude()));
        assertThat(createdMovement.getPositionTime(), is(movementBaseType.getPositionTime()));
    }
    
    @Test
    @RunAsClient
    public void createMovementVerifyAllBaseTypeData() throws Exception {
        MovementBaseType movementBaseType = MovementTestHelper.createMovementBaseType();
        CreateMovementResponse response = jmsHelper.createMovement(movementBaseType, "test user");
        MovementType createdMovement = response.getMovement();
        assertThat(createdMovement.getGuid(), is(notNullValue()));
        assertThat(createdMovement.getConnectId(), is(movementBaseType.getConnectId()));
        // Not working @ version 4.0.15
//        assertThat(createdMovement.getAssetId(), is(movementBaseType.getAssetId()));
        assertThat(createdMovement.getPosition().getLongitude(), is(movementBaseType.getPosition().getLongitude()));
        assertThat(createdMovement.getPosition().getLatitude(), is(movementBaseType.getPosition().getLatitude()));
        assertThat(createdMovement.getPosition().getAltitude(), is(movementBaseType.getPosition().getAltitude()));
        assertThat(createdMovement.getPositionTime(), is(movementBaseType.getPositionTime()));
        assertThat(createdMovement.getStatus(), is(movementBaseType.getStatus()));
        assertThat(createdMovement.getReportedSpeed(), is(movementBaseType.getReportedSpeed()));
        assertThat(createdMovement.getReportedCourse(), is(movementBaseType.getReportedCourse()));
        assertThat(createdMovement.getMovementType(), is(movementBaseType.getMovementType()));
        assertThat(createdMovement.getSource(), is(movementBaseType.getSource()));
        assertThat(createdMovement.getActivity(), is(movementBaseType.getActivity()));
        assertThat(createdMovement.getTripNumber(), is(movementBaseType.getTripNumber()));
        assertThat(createdMovement.getInternalReferenceNumber(), is(movementBaseType.getInternalReferenceNumber()));
    }
    
    @Test
    @RunAsClient
    public void createMovementVerifyNullAltitudeData() throws Exception {
        MovementBaseType movementBaseType = MovementTestHelper.createMovementBaseType();
        movementBaseType.getPosition().setAltitude(null);
        CreateMovementResponse response = jmsHelper.createMovement(movementBaseType, "test user");
        MovementType createdMovement = response.getMovement();
        assertThat(createdMovement.getGuid(), is(notNullValue()));
        assertThat(createdMovement.getPosition().getAltitude(), is(movementBaseType.getPosition().getAltitude()));
    }
    
    @Test
    @RunAsClient
    public void createMovementVerifyBasicMetaData() throws Exception {
        MovementBaseType movementBaseType = MovementTestHelper.createMovementBaseType();
        CreateMovementResponse response = jmsHelper.createMovement(movementBaseType, "test user");
        MovementType createdMovement = response.getMovement();
        assertThat(createdMovement.getGuid(), is(notNullValue()));
        assertThat(createdMovement.getWkt(), is(notNullValue()));
        assertThat(createdMovement.getMetaData(), is(notNullValue()));
    }
    
    @Test
    @RunAsClient
    public void createMovementVerifyCalculatedData() throws Exception {
        String connectId = UUID.randomUUID().toString();
        
        MovementBaseType movementBaseType = MovementTestHelper.createMovementBaseType(0d, 1d);
        movementBaseType.setConnectId(connectId);
        movementBaseType.setPositionTime(Date.from(Instant.now().minusSeconds(10)));
        jmsHelper.createMovement(movementBaseType, "test user");
        
        MovementBaseType movementBaseType2 = MovementTestHelper.createMovementBaseType(0d, 2d);
        movementBaseType2.setConnectId(connectId);
        CreateMovementResponse response = jmsHelper.createMovement(movementBaseType2, "test user");
        MovementType createdMovement = response.getMovement();
        
        assertThat(createdMovement.getGuid(), is(notNullValue()));
        assertThat(createdMovement.getCalculatedSpeed(), is(notNullValue()));
        assertThat(createdMovement.getCalculatedCourse(), is(notNullValue()));
    }
    
    @Test
    @RunAsClient
    public void createMovementVerifyBasicSegment() throws Exception {
        String connectId = UUID.randomUUID().toString();
        
        MovementBaseType movementBaseType1 = MovementTestHelper.createMovementBaseType(0d, 0d);
        movementBaseType1.setPositionTime(Date.from(Instant.now().minusSeconds(10)));
        movementBaseType1.setConnectId(connectId);
        jmsHelper.createMovement(movementBaseType1, "test user");
        
        MovementBaseType movementBaseType2 = MovementTestHelper.createMovementBaseType(0d, 1d);
        movementBaseType2.setConnectId(connectId);
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
    }
    
    /* Test areas are defined in SpatialModuleMock */
    @Test
    @RunAsClient
    public void createMovementVerifyBasicAreaData() throws Exception {
        MovementBaseType movementBaseType = MovementTestHelper.createMovementBaseType();
        CreateMovementResponse response = jmsHelper.createMovement(movementBaseType, "test user");
        MovementType createdMovement = response.getMovement();
        assertThat(createdMovement.getGuid(), is(notNullValue()));
        assertThat(createdMovement.getMetaData(), is(notNullValue()));
        MovementMetaData metaData = createdMovement.getMetaData();
        assertThat(metaData.getClosestCountry().getCode(), is("SWE"));
        assertThat(metaData.getClosestPort().getCode(), is("GOT"));
        assertThat(metaData.getAreas().size(), is(2));
        assertThat(metaData.getAreas().get(0).getTransitionType(), is(MovementTypeType.ENT));
    }

    @Ignore // Second position in same area should have transition type POS
    @Test
    @RunAsClient
    public void createMovementVerifyAreaTransitionType() throws Exception {
        String connectId = UUID.randomUUID().toString();
        
        MovementBaseType movementBaseType1 = MovementTestHelper.createMovementBaseType(0d, 0d);
        movementBaseType1.setPositionTime(Date.from(Instant.now().minusSeconds(10)));
        movementBaseType1.setConnectId(connectId);
        CreateMovementResponse response = jmsHelper.createMovement(movementBaseType1, "test user");
        MovementType createdMovement = response.getMovement();
        
        assertThat(createdMovement.getGuid(), is(notNullValue()));
        assertThat(createdMovement.getMetaData(), is(notNullValue()));
        MovementMetaData metaData = createdMovement.getMetaData();
        assertThat(metaData.getAreas().get(0).getTransitionType(), is(MovementTypeType.ENT));
        
        MovementBaseType movementBaseType2 = MovementTestHelper.createMovementBaseType(0d, 1d);
        movementBaseType2.setConnectId(connectId);
        CreateMovementResponse response2 = jmsHelper.createMovement(movementBaseType2, "test user");
        MovementType createdMovement2 = response2.getMovement();
        
        assertThat(createdMovement2.getGuid(), is(notNullValue()));
        assertThat(createdMovement2.getMetaData(), is(notNullValue()));
        MovementMetaData metaData2 = createdMovement2.getMetaData();
        assertThat(metaData2.getAreas().get(0).getTransitionType(), is(MovementTypeType.POS));
    }
    
    @Test
    @RunAsClient
    public void createMovementBatchTest() throws Exception {
        MovementBaseType m1 = MovementTestHelper.createMovementBaseType(0d, 0d);
        MovementBaseType m2 = MovementTestHelper.createMovementBaseType(0d, 1d);
        MovementBaseType m3 = MovementTestHelper.createMovementBaseType(0d, 2d);
        CreateMovementBatchResponse response = jmsHelper.createMovementBatch(Arrays.asList(m1, m2, m3), "test user");
        List<MovementType> createdMovement = response.getMovements();
        assertThat(createdMovement.size(), is(3));
        assertThat(createdMovement.get(0).getMetaData().getAreas().get(0).getCode(), is("AREA0"));
        assertThat(createdMovement.get(1).getMetaData().getAreas().get(0).getCode(), is("AREA1"));
        assertThat(createdMovement.get(2).getMetaData().getAreas().get(0).getCode(), is("AREA2"));
    }

    @Test
    @RunAsClient
    public void getMovementListByConnectId() throws Exception {
        MovementBaseType movementBaseType = MovementTestHelper.createMovementBaseType();
        CreateMovementResponse response = jmsHelper.createMovement(movementBaseType, "test user");
        MovementType createdMovement = response.getMovement();

        MovementQuery query = MovementTestHelper.createMovementQuery(true, false, false);
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CONNECT_ID);
        criteria.setValue(movementBaseType.getConnectId());
        query.getMovementSearchCriteria().add(criteria);
        
        GetMovementListByQueryResponse listByQueryResponse = jmsHelper.getMovementListByQuery(query, createdMovement.getConnectId());
        List<MovementType> movements = listByQueryResponse.getMovement();
        assertThat(movements.size(), is(1));
        assertThat(movements.get(0).getConnectId(), is(createdMovement.getConnectId()));
        assertThat(movements.get(0).getPosition().getLongitude(), is(createdMovement.getPosition().getLongitude()));
        assertThat(movements.get(0).getPosition().getLatitude(), is(createdMovement.getPosition().getLatitude()));
        assertThat(movements.get(0).getPositionTime(), is(createdMovement.getPositionTime()));
    }
    
    @Test
    @RunAsClient
    public void getMovementListByConnectIdTwoPositions() throws Exception {
        String connectId = UUID.randomUUID().toString();
        Instant timestamp = Instant.now();

        MovementBaseType movementBaseType1 = MovementTestHelper.createMovementBaseType();
        movementBaseType1.setConnectId(connectId);
        movementBaseType1.setPositionTime(Date.from(timestamp));
        jmsHelper.createMovement(movementBaseType1, "test user");
        
        MovementBaseType movementBaseType2 = MovementTestHelper.createMovementBaseType();
        movementBaseType2.setConnectId(connectId);
        movementBaseType1.setPositionTime(Date.from(timestamp.plusSeconds(10)));
        jmsHelper.createMovement(movementBaseType2, "test user");

        MovementQuery query = MovementTestHelper.createMovementQuery(true, false, false);
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CONNECT_ID);
        criteria.setValue(connectId);
        query.getMovementSearchCriteria().add(criteria);

        GetMovementListByQueryResponse listByQueryResponse = jmsHelper.getMovementListByQuery(query, connectId);
        List<MovementType> movements = listByQueryResponse.getMovement();
        assertThat(movements.size(), is(2));
    }
    
    @Test
    @RunAsClient
    public void getMovementListByConnectIdDifferentIds() throws Exception {
        MovementBaseType movementBaseType1 = MovementTestHelper.createMovementBaseType();
        jmsHelper.createMovement(movementBaseType1, "test user");
        
        MovementBaseType movementBaseType2 = MovementTestHelper.createMovementBaseType();
        jmsHelper.createMovement(movementBaseType2, "test user");

        MovementQuery query = MovementTestHelper.createMovementQuery(true, false, false);
        ListCriteria criteria1 = new ListCriteria();
        criteria1.setKey(SearchKey.CONNECT_ID);
        criteria1.setValue(movementBaseType1.getConnectId());
        query.getMovementSearchCriteria().add(criteria1);
        ListCriteria criteria2 = new ListCriteria();
        criteria2.setKey(SearchKey.CONNECT_ID);
        criteria2.setValue(movementBaseType2.getConnectId());
        query.getMovementSearchCriteria().add(criteria2);
        
        GetMovementListByQueryResponse listByQueryResponse = jmsHelper.getMovementListByQuery(query, movementBaseType2.getConnectId());
        List<MovementType> movements = listByQueryResponse.getMovement();
        assertThat(movements.size(), is(2));
    }
    
    
    @Ignore // This should work when query searches guid instead of id
    @Test
    @RunAsClient
    public void getMovementListByMovementId() throws Exception {
        MovementBaseType movementBaseType = MovementTestHelper.createMovementBaseType();
        CreateMovementResponse response = jmsHelper.createMovement(movementBaseType, "test user");
        MovementType createdMovement = response.getMovement();

        MovementQuery query = MovementTestHelper.createMovementQuery(true, false, false);
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.MOVEMENT_ID);
        criteria.setValue(movementBaseType.getGuid());
        query.getMovementSearchCriteria().add(criteria);
        
        GetMovementListByQueryResponse listByQueryResponse = jmsHelper.getMovementListByQuery(query, createdMovement.getConnectId());
        List<MovementType> movements = listByQueryResponse.getMovement();
        assertThat(movements.size(), is(1));
        assertThat(movements.get(0).getConnectId(), is(createdMovement.getConnectId()));
        assertThat(movements.get(0).getPosition().getLongitude(), is(createdMovement.getPosition().getLongitude()));
        assertThat(movements.get(0).getPosition().getLatitude(), is(createdMovement.getPosition().getLatitude()));
        assertThat(movements.get(0).getPositionTime(), is(createdMovement.getPositionTime()));
    }
    
    @Ignore // This should work when query searches guid instead of id
    @Test
    @RunAsClient
    public void getMovementListByMovementIdTwoMovements() throws Exception {
        MovementBaseType movementBaseType1 = MovementTestHelper.createMovementBaseType();
        jmsHelper.createMovement(movementBaseType1, "test user");
        
        MovementBaseType movementBaseType2 = MovementTestHelper.createMovementBaseType();
        jmsHelper.createMovement(movementBaseType2, "test user");

        MovementQuery query = MovementTestHelper.createMovementQuery(true, false, false);
        ListCriteria criteria1 = new ListCriteria();
        criteria1.setKey(SearchKey.MOVEMENT_ID);
        criteria1.setValue(movementBaseType1.getGuid());
        query.getMovementSearchCriteria().add(criteria1);
        ListCriteria criteria2 = new ListCriteria();
        criteria2.setKey(SearchKey.MOVEMENT_ID);
        criteria2.setValue(movementBaseType2.getGuid());
        query.getMovementSearchCriteria().add(criteria2);
        
        GetMovementListByQueryResponse listByQueryResponse = jmsHelper.getMovementListByQuery(query, movementBaseType2.getConnectId());
        List<MovementType> movements = listByQueryResponse.getMovement();
        assertThat(movements.size(), is(2));
    }
    
    @Test
    @RunAsClient
    public void getMovementListByDateFromRange() throws Exception {
        Instant timestampBefore = Instant.now().minusSeconds(1);
        
        MovementBaseType movementBaseType = MovementTestHelper.createMovementBaseType();
        CreateMovementResponse response = jmsHelper.createMovement(movementBaseType, "test user");
        MovementType createdMovement = response.getMovement();
        
        Instant timestampAfter = Instant.now().plusSeconds(1);

        MovementQuery query = MovementTestHelper.createMovementQuery(true, false, false);
        RangeCriteria criteria = new RangeCriteria();
        criteria.setKey(RangeKeyType.DATE);
        criteria.setFrom(DateUtil.parseUTCDateToString(timestampBefore));
        criteria.setTo(DateUtil.parseUTCDateToString(timestampAfter));
        query.getMovementRangeSearchCriteria().add(criteria);
        ListCriteria criteria1 = new ListCriteria();
        criteria1.setKey(SearchKey.CONNECT_ID);
        criteria1.setValue(movementBaseType.getConnectId());
        query.getMovementSearchCriteria().add(criteria1);
        
        GetMovementListByQueryResponse listByQueryResponse = jmsHelper.getMovementListByQuery(query, createdMovement.getConnectId());
        List<MovementType> movements = listByQueryResponse.getMovement();
        assertThat(movements.size(), is(1));
        assertThat(movements.get(0).getConnectId(), is(createdMovement.getConnectId()));
        assertThat(movements.get(0).getPosition().getLongitude(), is(createdMovement.getPosition().getLongitude()));
        assertThat(movements.get(0).getPosition().getLatitude(), is(createdMovement.getPosition().getLatitude()));
        assertThat(movements.get(0).getPositionTime(), is(createdMovement.getPositionTime()));
    }
    
    @Test
    @RunAsClient
    public void getMovementListByDateTwoMovements() throws Exception {
        Instant timestampBefore = Instant.now().minusSeconds(60);
        
        MovementBaseType movementBaseType1 = MovementTestHelper.createMovementBaseType();
        jmsHelper.createMovement(movementBaseType1, "test user");
        
        MovementBaseType movementBaseType2 = MovementTestHelper.createMovementBaseType();
        jmsHelper.createMovement(movementBaseType2, "test user");

        Instant timestampAfter = Instant.now().plusSeconds(60);
        
        MovementQuery query = MovementTestHelper.createMovementQuery(true, false, false);
        RangeCriteria criteria = new RangeCriteria();
        criteria.setKey(RangeKeyType.DATE);
        criteria.setFrom(DateUtil.parseUTCDateToString(timestampBefore));
        criteria.setTo(DateUtil.parseUTCDateToString(timestampAfter));
        query.getMovementRangeSearchCriteria().add(criteria);
        ListCriteria criteria1 = new ListCriteria();
        criteria1.setKey(SearchKey.CONNECT_ID);
        criteria1.setValue(movementBaseType1.getConnectId());
        query.getMovementSearchCriteria().add(criteria1);
        ListCriteria criteria2 = new ListCriteria();
        criteria2.setKey(SearchKey.CONNECT_ID);
        criteria2.setValue(movementBaseType2.getConnectId());
        query.getMovementSearchCriteria().add(criteria2);
        
        GetMovementListByQueryResponse listByQueryResponse = jmsHelper.getMovementListByQuery(query, movementBaseType2.getConnectId());
        List<MovementType> movements = listByQueryResponse.getMovement();
        assertThat(movements.size(), is(2));
    }
    
    @Test
    @RunAsClient
    public void createMovementConcurrentProcessing() throws Exception {
        int numberOfPositions = 20;
        String connectId = UUID.randomUUID().toString();
        
        List<String> correlationIds = new ArrayList<>();
        
        Instant timestamp = Instant.now();
        
        // Send positions to movement
        for (int i = 0; i < numberOfPositions; i++) {
            MovementBaseType movementBaseType = MovementTestHelper.createMovementBaseType();
            movementBaseType.setConnectId(connectId);
            movementBaseType.setPositionTime(Date.from(timestamp));
            timestamp = timestamp.plusSeconds(10);
            String request = MovementModuleRequestMapper.mapToCreateMovementRequest(movementBaseType, "Test");
            String correlationId = jmsHelper.sendMovementMessage(request, connectId);
            correlationIds.add(correlationId);
        }
        
        // Check responses
        for (String correlationId : correlationIds) {
            Message response = jmsHelper.listenForResponse(correlationId);
            JAXBMarshaller.unmarshallTextMessage((TextMessage) response, CreateMovementResponse.class);
        }
        
        MovementQuery query = MovementTestHelper.createMovementQuery(true, false, false);
        query.getPagination().setListSize(BigInteger.valueOf(100l));
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CONNECT_ID);
        criteria.setValue(connectId);
        query.getMovementSearchCriteria().add(criteria);

        GetMovementListByQueryResponse movementResponse = jmsHelper.getMovementListByQuery(query, connectId);
        List<MovementType> movements = movementResponse.getMovement();

        movements.sort(Comparator.comparing(MovementBaseType::getPositionTime));
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
    @RunAsClient
    public void createMovementConcurrentProcessingTwoConnectIds() throws Exception {
        int numberOfPositions = 20;
        String connectId1 = UUID.randomUUID().toString();
        String connectId2 = UUID.randomUUID().toString();
        
        List<String> correlationIds = new ArrayList<>();
        
        Instant timestamp = Instant.now();
        
        // Send positions to movement
        for (int i = 0; i < numberOfPositions; i++) {
            MovementBaseType movementBaseTypeC1 = MovementTestHelper.createMovementBaseType();
            movementBaseTypeC1.setConnectId(connectId1);
            movementBaseTypeC1.setPositionTime(Date.from(timestamp));
            String request = MovementModuleRequestMapper.mapToCreateMovementRequest(movementBaseTypeC1, "Test");
            String correlationId = jmsHelper.sendMovementMessage(request, connectId1);
            correlationIds.add(correlationId);
            
            MovementBaseType movementBaseTypeC2 = MovementTestHelper.createMovementBaseType();
            movementBaseTypeC2.setConnectId(connectId2);
            movementBaseTypeC2.setPositionTime(Date.from(timestamp));
            String request2 = MovementModuleRequestMapper.mapToCreateMovementRequest(movementBaseTypeC2, "Test");
            String correlationId2 = jmsHelper.sendMovementMessage(request2, connectId2);
            correlationIds.add(correlationId2);

            timestamp = timestamp.plusSeconds(10);
        }
        
        // Check responses
        for (String correlationId : correlationIds) {
            Message response = jmsHelper.listenForResponse(correlationId);
            JAXBMarshaller.unmarshallTextMessage((TextMessage) response, CreateMovementResponse.class);
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
    @RunAsClient
    public void testMaxRedeliveries() throws Exception {
        int dlqBefore = jmsHelper.checkQueueSize("ActiveMQ.DLQ");
        int responseQueueBefore = jmsHelper.checkQueueSize(JMSHelper.RESPONSE_QUEUE);
        
        MovementBaseType movementBaseType = MovementTestHelper.createMovementBaseType();
        movementBaseType.setPosition(null);
        String request = MovementModuleRequestMapper.mapToCreateMovementRequest(movementBaseType, "Test");
        String correlationId = jmsHelper.sendMovementMessage(request, movementBaseType.getConnectId());
        
        Message response = jmsHelper.listenForResponse(correlationId);
        JAXBMarshaller.unmarshallTextMessage((TextMessage) response, ExceptionType.class);
        
        // Wait until message have been moved to DQL
        Thread.sleep(2500);
        
        int dlqAfter = jmsHelper.checkQueueSize("ActiveMQ.DLQ");
        int responseQueueAfter = jmsHelper.checkQueueSize(JMSHelper.RESPONSE_QUEUE);
        
        assertThat(dlqAfter, is(dlqBefore + 1));
        assertThat(responseQueueBefore, is(responseQueueAfter));
    }
}
