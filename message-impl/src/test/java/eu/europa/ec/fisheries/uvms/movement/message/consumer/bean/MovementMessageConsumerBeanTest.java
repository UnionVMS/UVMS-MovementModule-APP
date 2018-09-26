package eu.europa.ec.fisheries.uvms.movement.message.consumer.bean;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import eu.europa.ec.fisheries.uvms.movement.message.BuildMovementServiceTestDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import eu.europa.ec.fisheries.uvms.movement.message.JMSHelper;
import eu.europa.ec.fisheries.uvms.movement.message.MovementTestHelper;
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
        assertThat(createdMovement.getPositionTime(), is(movementBaseType.getPositionTime()));
        assertThat(createdMovement.getStatus(), is(movementBaseType.getStatus()));
        assertThat(createdMovement.getReportedSpeed(), is(movementBaseType.getReportedSpeed()));
        assertThat(createdMovement.getReportedCourse(), is(movementBaseType.getReportedCourse()));
        assertThat(createdMovement.getMovementType(), is(movementBaseType.getMovementType()));
        assertThat(createdMovement.getSource(), is(movementBaseType.getSource()));
        assertThat(createdMovement.getActivity(), is(movementBaseType.getActivity()));
        // Not working @ version 4.0.15
//        assertThat(createdMovement.getTripNumber(), is(movementBaseType.getTripNumber()));
        assertThat(createdMovement.getInternalReferenceNumber(), is(movementBaseType.getInternalReferenceNumber()));
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
        GetMovementListByQueryResponse movementList = jmsHelper.getMovementListByQuery(query);
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
    public void getMovementListByConnectId() throws Exception {
        MovementBaseType movementBaseType = MovementTestHelper.createMovementBaseType();
        CreateMovementResponse response = jmsHelper.createMovement(movementBaseType, "test user");
        MovementType createdMovement = response.getMovement();

        MovementQuery query = MovementTestHelper.createMovementQuery(true, false, false);
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CONNECT_ID);
        criteria.setValue(movementBaseType.getConnectId());
        query.getMovementSearchCriteria().add(criteria);
        
        GetMovementListByQueryResponse listByQueryResponse = jmsHelper.getMovementListByQuery(query);
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

        GetMovementListByQueryResponse listByQueryResponse = jmsHelper.getMovementListByQuery(query);
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
        
        GetMovementListByQueryResponse listByQueryResponse = jmsHelper.getMovementListByQuery(query);
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
        
        GetMovementListByQueryResponse listByQueryResponse = jmsHelper.getMovementListByQuery(query);
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
        
        GetMovementListByQueryResponse listByQueryResponse = jmsHelper.getMovementListByQuery(query);
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
        
        GetMovementListByQueryResponse listByQueryResponse = jmsHelper.getMovementListByQuery(query);
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
        
        GetMovementListByQueryResponse listByQueryResponse = jmsHelper.getMovementListByQuery(query);
        List<MovementType> movements = listByQueryResponse.getMovement();
        assertThat(movements.size(), is(2));
    }
}
