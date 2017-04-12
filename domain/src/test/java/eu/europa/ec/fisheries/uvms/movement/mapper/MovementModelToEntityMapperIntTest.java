package eu.europa.ec.fisheries.uvms.movement.mapper;

import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaData;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaDataAreaType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.arquillian.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.arquillian.bean.util.TestUtil;
import eu.europa.ec.fisheries.uvms.movement.bean.MovementBatchModelBean;
import eu.europa.ec.fisheries.uvms.movement.dao.bean.MovementDaoBean;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.MovementDaoMappingException;
import eu.europa.ec.fisheries.uvms.movement.entity.Activity;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.MovementConnect;
import eu.europa.ec.fisheries.uvms.movement.entity.Movementmetadata;
import eu.europa.ec.fisheries.uvms.movement.entity.Segment;
import eu.europa.ec.fisheries.uvms.movement.entity.Track;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.exception.GeometryUtilException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDaoException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDuplicateException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static eu.europa.ec.fisheries.uvms.movement.mapper.MovementModelToEntityMapper.createActivity;
import static eu.europa.ec.fisheries.uvms.movement.mapper.MovementModelToEntityMapper.createSegment;
import static eu.europa.ec.fisheries.uvms.movement.mapper.MovementModelToEntityMapper.mapNewMovementEntity;
import static eu.europa.ec.fisheries.uvms.movement.mapper.MovementModelToEntityMapper.mapToAreaType;
import static eu.europa.ec.fisheries.uvms.movement.mapper.MovementModelToEntityMapper.mapToMovementMetaData;
import static eu.europa.ec.fisheries.uvms.movement.mapper.MovementModelToEntityMapper.maptoArea;
import static eu.europa.ec.fisheries.uvms.movement.mapper.MovementModelToEntityMapper.updateSegment;
import static eu.europa.ec.fisheries.uvms.movement.mapper.MovementModelToEntityMapper.updateTrack;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Created by roblar on 2017-03-31.
 */
@RunWith(Arquillian.class)
public class MovementModelToEntityMapperIntTest extends TransactionalTests {

    private final static Logger LOG = LoggerFactory.getLogger(MovementModelToEntityMapperIntTest.class);

    private TestUtil testUtil = new TestUtil();

    @Inject
    MovementDaoBean movementDaoBean;

    @Inject
    MovementBatchModelBean movementBatchModelBean;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    @OperateOnDeployment("normal")
    public void testMapNewMovementEntity_reportedSpeedIsNull() throws MovementDaoException, MovementDaoMappingException {

        //Given
        String uuid = UUID.randomUUID().toString();

        MovementType movementType = testUtil.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid);
        movementType.setReportedSpeed(null);

        //When
        Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertNull(movement.getSpeed());
        LOG.info(" [ testMapNewMovementEntity: MovementType with reported speed not set maps to Movement with speed not set. ] ");
    }

    @Test
    @OperateOnDeployment("normal")
    public void testMapNewMovementEntity_reportedCourseIsNull() throws MovementDaoException, MovementDaoMappingException {

        //Given
        String uuid = UUID.randomUUID().toString();

        MovementType movementType = testUtil.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid);
        movementType.setReportedCourse(null);

        //When
        Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertNull(movement.getHeading());
        LOG.info(" [ testMapNewMovementEntity: MovementType with reported course not set maps to Movement with heading not set. ] ");
    }

    @Test
    @OperateOnDeployment("normal")
    public void testMapNewMovementEntity_positionIsNull() throws MovementDaoException, MovementDaoMappingException {

        //Given
        String uuid = UUID.randomUUID().toString();

        MovementType movementType = testUtil.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid);
        movementType.setPosition(null);

        //When
        Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertNull(movement.getLocation());
        LOG.info(" [ testMapNewMovementEntity: MovementType with position not set maps to Movement with location not set. ] ");
    }

    @Test
    @OperateOnDeployment("normal")
    public void testMapNewMovementEntity_ifSourceIsNullThenMovementSourceTypeIs_INMARSATC() throws MovementDaoException, MovementDaoMappingException {

        //Given
        String uuid = UUID.randomUUID().toString();

        MovementType movementType = testUtil.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid);
        movementType.setSource(null);

        //When
        Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertThat(movement.getMovementSource(), is(MovementSourceType.INMARSAT_C));
        LOG.info(" [ testMapNewMovementEntity: MovementType with source not set maps to Movement with MovementSourceType set to INMARSAT_C. ] ");
    }

    @Test
    @OperateOnDeployment("normal")
    public void testMapNewMovementEntity_ifMovementTypeIsNullThenMovementTypeTypeIs_POS() throws MovementDaoException, MovementDaoMappingException {

        //Given
        String uuid = UUID.randomUUID().toString();

        MovementType movementType = testUtil.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid);
        movementType.setMovementType(null);

        //When
        Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertThat(movement.getMovementType(), is(MovementTypeType.POS));
        LOG.info(" [ testMapNewMovementEntity: MovementType with MovementTypeType not set maps to Movement with MovementTypeType set to POS. ] ");
    }

    @Test
    @OperateOnDeployment("normal")
    public void testMapNewMovementEntity_ifPositionTimeIsNullThenTimeStampIsSet() throws MovementDaoException, MovementDaoMappingException {

        //Given
        String uuid = UUID.randomUUID().toString();

        MovementType movementType = testUtil.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid);
        movementType.setPositionTime(null);

        //When
        Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertNotNull(movement.getTimestamp());
        LOG.info(" [ testMapNewMovementEntity: MovementType with position time not set maps to Movement with timestamp set to current time. ] ");
    }

    @Test
    @OperateOnDeployment("normal")
    public void testMapNewMovementEntity_activityIsNull() throws MovementDaoException, MovementDaoMappingException {

        //Given
        String uuid = UUID.randomUUID().toString();

        MovementType movementType = testUtil.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid);
        movementType.setActivity(null);

        //When
        Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertNull(movement.getActivity());
        LOG.info(" [ testMapNewMovementEntity: MovementType with MovementActivityType not set maps to Movement with Activity not set. ] ");
    }

    @Test
    @OperateOnDeployment("normal")
    public void testMapNewMovementEntity_metaDataIsNull() throws MovementDaoException, MovementDaoMappingException  {

        //Given
        String uuid = UUID.randomUUID().toString();

        MovementType movementType = testUtil.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid);
        movementType.setMetaData(null);

        //When
        Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertNull(movement.getMetadata());
        LOG.info(" [ testMapNewMovementEntity: MovementType with MovementMetaData not set maps to Movement with Movementmetadata (spelled correct) not set. ] ");
    }

    @Test
    @OperateOnDeployment("normal")
    public void testMapToMovementMetaData_ifClosestPortIsNullThenClosestPortCodeAndDistanceAndRemoteIdAndNameAreNull() throws MovementDaoException, MovementDaoMappingException {

        //Given
        String uuid = UUID.randomUUID().toString();

        MovementType movementType = testUtil.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid);
        MovementMetaData movementMetaDataToBeMapped = movementType.getMetaData();

        movementMetaDataToBeMapped.setClosestPort(null);

        //When
        Movementmetadata mappedMovementMetaData = mapToMovementMetaData(movementMetaDataToBeMapped);

        //Then
        assertNull(mappedMovementMetaData.getClosestPortCode());
        assertNull(mappedMovementMetaData.getClosestPortDistance());
        assertNull(mappedMovementMetaData.getClosestPortRemoteId());
        assertNull(mappedMovementMetaData.getClosestPortName());

        LOG.info(" [ testMapToMovementMetaData: MovementMetaData with closest port not set maps to Movementmetadata (spelled correct) with closest port code, -distance, -remoteId, -and name not set. ] ");
    }

    @Test
    @OperateOnDeployment("normal")
    public void testMapToMovementMetaData_ifClosestCountryIsNullThenClosestCountryCodeAndDistanceAndRemoteIdAndNameAreNull() throws MovementDaoException, MovementDaoMappingException {

        //Given
        String uuid = UUID.randomUUID().toString();

        MovementType movementType = testUtil.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid);
        MovementMetaData movementMetaDataToBeMapped = movementType.getMetaData();

        movementMetaDataToBeMapped.setClosestCountry(null);

        //When
        Movementmetadata mappedMovementMetaData = mapToMovementMetaData(movementMetaDataToBeMapped);

        //Then
        assertNull(mappedMovementMetaData.getClosestCountryCode());
        assertNull(mappedMovementMetaData.getClosestCountryDistance());
        assertNull(mappedMovementMetaData.getClosestCountryRemoteId());
        assertNull(mappedMovementMetaData.getClosestCountryName());

        LOG.info(" [ testMapToMovementMetaData: MovementMetaData with closest country not set maps to Movementmetadata (spelled correct) with closest country code, -distance, -remoteId, -and name not set. ] ");
    }

    @Test
    @OperateOnDeployment("normal")
    public void testCreateActivity_mapFromMovementBaseTypeToActivity() throws MovementDaoException, MovementDaoMappingException {

        //Given
        String uuid = UUID.randomUUID().toString();
        MovementBaseType movementBaseType = testUtil.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid);

        //When
        Activity activity = createActivity(movementBaseType);

        //Then
        assertNotNull(activity);
        LOG.info(" [ testCreateActivity: Mapping from MovementBaseType to Activity is successful. ] ");
    }

    @Test
    @OperateOnDeployment("normal")
    public void testCreateSegment_mapFromTwoMovementsToSegment() throws MovementDaoException, MovementDaoMappingException, MovementModelException, MovementDuplicateException, GeometryUtilException {

        //Given: Set up two movements with different timestamps.

        String connectId = UUID.randomUUID().toString();

        Calendar cal = Calendar.getInstance();
        cal.set(1920, 06, 06);
        Date date1 = cal.getTime();
        cal.set(1930, 06, 06);
        Date date2 = cal.getTime();

        Movement fromMovement = createMovement(0d, 0d, 0d, SegmentCategoryType.EXIT_PORT, connectId, "ONE", date1);
        Movement toMovement = createMovement(1d, 1d, 0d, SegmentCategoryType.GAP, connectId, "TWO", date2);

        //When
        Segment segment = createSegment(fromMovement, toMovement);

        //Then
        assertNotNull(segment);
        LOG.info(" [ testCreateSegment: Mapping from two Movements to a Segment is successful. ] ");
    }

    @Test(expected = GeometryUtilException.class)
    @OperateOnDeployment("normal")
    public void testCreateSegment_emptyMovementsThrowsGeometryUtilException() throws MovementDaoMappingException, GeometryUtilException {

        Movement fromMovement = new Movement();
        Movement toMovement = new Movement();

        createSegment(fromMovement, toMovement);

        thrown.expect(GeometryUtilException.class);
        thrown.expectMessage("CurrentPosition is null!");
    }

    @Test
    @OperateOnDeployment("normal")
    public void testCreateTrack_mapFromSegmentToTrack() throws MovementDaoException, MovementDaoMappingException, MovementModelException, MovementDuplicateException, GeometryUtilException {

        //Given: Create a Segment.
        String connectId = UUID.randomUUID().toString();

        Calendar cal = Calendar.getInstance();
        cal.set(1920, 06, 06);
        Date date1 = cal.getTime();
        cal.set(1930, 06, 06);
        Date date2 = cal.getTime();

        Movement fromMovement = createMovement(0d, 0d, 0d, SegmentCategoryType.EXIT_PORT, connectId, "ONE", date1);
        Movement toMovement = createMovement(1d, 1d, 0d, SegmentCategoryType.GAP, connectId, "TWO", date2);

        Segment segment = createSegment(fromMovement, toMovement);

        //When
        Track track = MovementModelToEntityMapper.createTrack(segment);

        //Then
        assertNotNull(track);
        LOG.info(" [ testCreateTrack_mapFromSegmentToTrack: Mapping from a Segment to a Track is successful. ] ");
    }


    @Test
    @OperateOnDeployment("normal")
    public void testUpdateTrack_sizeOfMovementListInRelatedTrackHasChanged() throws MovementDuplicateException, MovementDaoException, MovementModelException, MovementDaoMappingException, GeometryUtilException {

        //Given: Create a Track.
        String connectId = UUID.randomUUID().toString();

        Calendar cal = Calendar.getInstance();
        cal.set(1920, 06, 06);
        Date date1 = cal.getTime();
        cal.set(1930, 06, 06);
        Date date2 = cal.getTime();

        Movement fromMovement = createMovement(0d, 0d, 0d, SegmentCategoryType.EXIT_PORT, connectId, "ONE", date1);
        Movement toMovement = createMovement(1d, 1d, 0d, SegmentCategoryType.GAP, connectId, "TWO", date2);

        Segment segment = createSegment(fromMovement, toMovement);

        Track trackBeforeUpdate = MovementModelToEntityMapper.createTrack(segment);

        cal.set(1940, 06, 06);
        Date date3 = cal.getTime();
        Movement currentMovement = createMovement(2d, 2d, 0d, SegmentCategoryType.GAP, connectId, "THREE", date3);

        int movementListSizeBeforeUpdate = trackBeforeUpdate.getMovementList().size();

        //When
        updateTrack(trackBeforeUpdate, currentMovement, segment);
        Track trackAfterUpdate = currentMovement.getTrack();

        int movementListSizeAfterUpdate = trackAfterUpdate.getMovementList().size();

        //Then
        assertNotEquals(movementListSizeBeforeUpdate, movementListSizeAfterUpdate);
    }

    @Test
    @OperateOnDeployment("normal")
    public void testUpdateTrack_totalTimeAtSeaIncreasesWhenSegmentCategoryTypeIsNotEqualTo_ENTER_PORT_or_IN_PORT() throws MovementDuplicateException, MovementDaoException, MovementModelException, MovementDaoMappingException, GeometryUtilException {

        //Given: Create a Segment and set the segment category type to ENTER_PORT.
        String connectId = UUID.randomUUID().toString();

        Calendar cal = Calendar.getInstance();
        cal.set(1920, 06, 06);
        Date date1 = cal.getTime();
        cal.set(1930, 06, 06);
        Date date2 = cal.getTime();

        Movement fromMovement = createMovement(0d, 0d, 0d, SegmentCategoryType.EXIT_PORT, connectId, "ONE", date1);
        Movement toMovement = createMovement(1d, 1d, 0d, SegmentCategoryType.GAP, connectId, "TWO", date2);

        Segment segment = createSegment(fromMovement, toMovement);

        Track track = MovementModelToEntityMapper.createTrack(segment);
        double totalTimeAtSeaBeforeUpdate = track.getTotalTimeAtSea();

        //When
        segment.setSegmentCategory(SegmentCategoryType.ENTER_PORT);
        updateTrack(track, toMovement, segment);
        double totalTimeAtSeaAfterUpdate = track.getTotalTimeAtSea();

        //Then
        assertNotEquals(totalTimeAtSeaBeforeUpdate, totalTimeAtSeaAfterUpdate);
    }

    @Test(expected = GeometryUtilException.class)
    @OperateOnDeployment("normal")
    public void testUpdateSegment_emptyMovementsThrowsGeometryUtilException() throws MovementDaoMappingException, GeometryUtilException {

        Movement fromMovement = new Movement();
        Movement toMovement = new Movement();

        Segment segment = createSegment(fromMovement, toMovement);

        updateSegment(segment, fromMovement, toMovement);

        thrown.expect(GeometryUtilException.class);
        thrown.expectMessage("CurrentPosition is null!");
    }

    @Test
    @OperateOnDeployment("normal")
    public void testUpdateSegment_checkThatSegmentHasChanged() throws MovementDuplicateException, MovementDaoException, MovementModelException, MovementDaoMappingException, GeometryUtilException {

        //Given - A segment and two movements.
        String connectId = UUID.randomUUID().toString();

        Calendar cal = Calendar.getInstance();
        cal.set(1920, 06, 06);
        Date date1 = cal.getTime();
        cal.set(1930, 06, 06);
        Date date2 = cal.getTime();

        Movement fromMovement = createMovement(0d, 0d, 0d, SegmentCategoryType.EXIT_PORT, connectId, "ONE", date1);
        Movement toMovement = createMovement(1d, 1d, 0d, SegmentCategoryType.GAP, connectId, "TWO", date2);

        Segment segmentBeforeUpdate = createSegment(fromMovement, toMovement);

        //When
        updateSegment(segmentBeforeUpdate, fromMovement, toMovement);
        Segment segmentAfterUpdate = toMovement.getToSegment();

        //Then
        assertNotEquals(segmentBeforeUpdate, segmentAfterUpdate);
    }

    @Test
    @OperateOnDeployment("normal")
    public void testMapToAreaType() {

        MovementMetaDataAreaType movementMetaDataAreaType = new MovementMetaDataAreaType();

        AreaType areaType = mapToAreaType(movementMetaDataAreaType);

        assertNotNull(areaType);
        assertThat(areaType.getUpdatedUser(), is("UVMS"));
        assertNotNull(areaType.getUpdatedTime());
    }

    @Test
    @OperateOnDeployment("normal")
    public void testMapToArea() {

        MovementMetaDataAreaType movementMetaDataAreaType = new MovementMetaDataAreaType();
        AreaType areaType = mapToAreaType(movementMetaDataAreaType);

        Area area = maptoArea(movementMetaDataAreaType, areaType);

        assertNotNull(area);
        assertThat(area.getAreaUpuser(), is("UVMS"));
        assertNotNull(area.getAreaUpdattim());
    }

    private Movement createMovement(double longitude, double latitude, double altitude, SegmentCategoryType segmentCategoryType, String connectId, String userName, Date positionTime) throws MovementModelException, MovementDuplicateException, MovementDaoException {
        MovementType movementType = testUtil.createMovementType(longitude, latitude, altitude, segmentCategoryType, connectId);
        movementType.setPositionTime(positionTime);
        movementType = movementBatchModelBean.createMovement(movementType, userName);
        em.flush();
        Assert.assertNotNull(movementType.getConnectId());
        MovementConnect movementConnect = movementDaoBean.getMovementConnectByConnectId(movementType.getConnectId());
        List<Movement> movementList = movementConnect.getMovementList();
        Assert.assertNotNull(movementList);
        return movementList.get(movementList.size() - 1);
    }
}
