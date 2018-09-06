package eu.europa.ec.fisheries.uvms.movement.message.consumer.bean;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
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
public class MovementMessageConsumerBeanTest {

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
    
    @Ignore // Should work when background job is removed
    @Test
    @RunAsClient
    public void createMovementVerifyCalculatedData() throws Exception {
        MovementBaseType movementBaseType = MovementTestHelper.createMovementBaseType();
        CreateMovementResponse response = jmsHelper.createMovement(movementBaseType, "test user");
        MovementType createdMovement = response.getMovement();
        assertThat(createdMovement.getGuid(), is(notNullValue()));
        assertThat(createdMovement.getCalculatedSpeed(), is(notNullValue()));
        assertThat(createdMovement.getCalculatedCourse(), is(notNullValue()));
        assertThat(createdMovement.getWkt(), is(notNullValue()));
        assertThat(createdMovement.getMetaData(), is(notNullValue()));
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
        assertThat(metaData.getAreas().get(0).getTransitionType(), is(MovementTypeType.POS));
    }

    @Test
    @RunAsClient
    public void getMovementListByConnectId() throws Exception {
        MovementBaseType movementBaseType = MovementTestHelper.createMovementBaseType();
        CreateMovementResponse response = jmsHelper.createMovement(movementBaseType, "test user");
        MovementType createdMovement = response.getMovement();

        // This is needed as long as the background processing job exists
        Thread.sleep(5000);
        
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

        MovementBaseType movementBaseType1 = MovementTestHelper.createMovementBaseType();
        movementBaseType1.setConnectId(connectId);
        jmsHelper.createMovement(movementBaseType1, "test user");
        
        MovementBaseType movementBaseType2 = MovementTestHelper.createMovementBaseType();
        movementBaseType2.setConnectId(connectId);
        jmsHelper.createMovement(movementBaseType2, "test user");

        // This is needed as long as the background processing job exists
        Thread.sleep(5000);
        
        MovementQuery query = MovementTestHelper.createMovementQuery(true, false, false);
        ListCriteria criteria = new ListCriteria();
        criteria.setKey(SearchKey.CONNECT_ID);
        criteria.setValue(movementBaseType1.getConnectId());
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

        // This is needed as long as the background processing job exists
        Thread.sleep(5000);
        
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

        // This is needed as long as the background processing job exists
        Thread.sleep(5000);
        
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

        // This is needed as long as the background processing job exists
        Thread.sleep(5000);
        
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
        Date timestampBefore = new Date();
        
        MovementBaseType movementBaseType = MovementTestHelper.createMovementBaseType();
        CreateMovementResponse response = jmsHelper.createMovement(movementBaseType, "test user");
        MovementType createdMovement = response.getMovement();
        
        // This is needed as long as the background processing job exists
        Thread.sleep(5000);
        
        Date timestampAfter = new Date();

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
        Date timestampBefore = new Date();
        
        MovementBaseType movementBaseType1 = MovementTestHelper.createMovementBaseType();
        jmsHelper.createMovement(movementBaseType1, "test user");
        
        MovementBaseType movementBaseType2 = MovementTestHelper.createMovementBaseType();
        jmsHelper.createMovement(movementBaseType2, "test user");

        // This is needed as long as the background processing job exists
        Thread.sleep(5000);
        
        Date timestampAfter = new Date();
        
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
