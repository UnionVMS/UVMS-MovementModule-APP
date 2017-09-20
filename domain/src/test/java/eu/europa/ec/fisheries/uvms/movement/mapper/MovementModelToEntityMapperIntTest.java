package eu.europa.ec.fisheries.uvms.movement.mapper;

import static eu.europa.ec.fisheries.uvms.movement.mapper.MovementModelToEntityMapper.createSegment;
import static eu.europa.ec.fisheries.uvms.movement.mapper.MovementModelToEntityMapper.updateSegment;
import static eu.europa.ec.fisheries.uvms.movement.mapper.MovementModelToEntityMapper.updateTrack;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.uvms.movement.arquillian.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.arquillian.bean.util.TestUtil;
import eu.europa.ec.fisheries.uvms.movement.bean.MovementBatchModelBean;
import eu.europa.ec.fisheries.uvms.movement.dao.bean.MovementDaoBean;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.MovementDaoMappingException;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.MovementConnect;
import eu.europa.ec.fisheries.uvms.movement.entity.Segment;
import eu.europa.ec.fisheries.uvms.movement.entity.Track;
import eu.europa.ec.fisheries.uvms.movement.exception.GeometryUtilException;

/**
 * Created by roblar on 2017-03-31.
 */
@RunWith(Arquillian.class)
public class MovementModelToEntityMapperIntTest extends TransactionalTests {

    private final static Logger LOG = LoggerFactory.getLogger(MovementModelToEntityMapperIntTest.class);

    private final TestUtil testUtil = new TestUtil();

    @Inject
    MovementDaoBean movementDaoBean;

    @Inject
    MovementBatchModelBean movementBatchModelBean;

    @Rule
    public ExpectedException thrown = ExpectedException.none();


    @Test
    @OperateOnDeployment("normal")
    public void testCreateSegment_mapFromTwoMovementsToSegment() throws MovementDaoException, MovementDaoMappingException, MovementModelException, MovementDuplicateException, GeometryUtilException {

        //Given: Set up two movements with different timestamps.

        final String connectId = UUID.randomUUID().toString();

        final Calendar cal = Calendar.getInstance();
        cal.set(1920, 06, 06);
        final Date date1 = cal.getTime();
        cal.set(1930, 06, 06);
        final Date date2 = cal.getTime();

        final Movement fromMovement = createMovement(0d, 0d, 0d, SegmentCategoryType.EXIT_PORT, connectId, "ONE", date1);
        final Movement toMovement = createMovement(1d, 1d, 0d, SegmentCategoryType.GAP, connectId, "TWO", date2);

        //When
        final Segment segment = createSegment(fromMovement, toMovement);

        //Then
        assertNotNull(segment);
        LOG.info(" [ testCreateSegment: Mapping from two Movements to a Segment is successful. ] ");
    }

    @Test
    @OperateOnDeployment("normal")
    public void testCreateTrack_mapFromSegmentToTrack() throws MovementDaoException, MovementDaoMappingException, MovementModelException, MovementDuplicateException, GeometryUtilException {

        //Given: Create a Segment.
        final String connectId = UUID.randomUUID().toString();

        final Calendar cal = Calendar.getInstance();
        cal.set(1920, 06, 06);
        final Date date1 = cal.getTime();
        cal.set(1930, 06, 06);
        final Date date2 = cal.getTime();

        final Movement fromMovement = createMovement(0d, 0d, 0d, SegmentCategoryType.EXIT_PORT, connectId, "ONE", date1);
        final Movement toMovement = createMovement(1d, 1d, 0d, SegmentCategoryType.GAP, connectId, "TWO", date2);

        final Segment segment = createSegment(fromMovement, toMovement);

        //When
        final Track track = MovementModelToEntityMapper.createTrack(segment);

        //Then
        assertNotNull(track);
        LOG.info(" [ testCreateTrack_mapFromSegmentToTrack: Mapping from a Segment to a Track is successful. ] ");
    }


    @Test
    @OperateOnDeployment("normal")
    public void testUpdateTrack_sizeOfMovementListInRelatedTrackHasChanged() throws MovementDuplicateException, MovementDaoException, MovementModelException, MovementDaoMappingException, GeometryUtilException {

        //Given: Create a Track.
        final String connectId = UUID.randomUUID().toString();

        final Calendar cal = Calendar.getInstance();
        cal.set(1920, 06, 06);
        final Date date1 = cal.getTime();
        cal.set(1930, 06, 06);
        final Date date2 = cal.getTime();

        final Movement fromMovement = createMovement(0d, 0d, 0d, SegmentCategoryType.EXIT_PORT, connectId, "ONE", date1);
        final Movement toMovement = createMovement(1d, 1d, 0d, SegmentCategoryType.GAP, connectId, "TWO", date2);

        final Segment segment = createSegment(fromMovement, toMovement);

        final Track trackBeforeUpdate = MovementModelToEntityMapper.createTrack(segment);

        cal.set(1940, 06, 06);
        final Date date3 = cal.getTime();
        final Movement currentMovement = createMovement(2d, 2d, 0d, SegmentCategoryType.GAP, connectId, "THREE", date3);

        final int movementListSizeBeforeUpdate = trackBeforeUpdate.getMovementList().size();

        //When
        updateTrack(trackBeforeUpdate, currentMovement, segment);
        final Track trackAfterUpdate = currentMovement.getTrack();

        final int movementListSizeAfterUpdate = trackAfterUpdate.getMovementList().size();

        //Then
        assertNotEquals(movementListSizeBeforeUpdate, movementListSizeAfterUpdate);
    }

    @Test
    @OperateOnDeployment("normal")
    public void testUpdateTrack_totalTimeAtSeaIncreasesWhenSegmentCategoryTypeIsNotEqualTo_ENTER_PORT_or_IN_PORT() throws MovementDuplicateException, MovementDaoException, MovementModelException, MovementDaoMappingException, GeometryUtilException {

        //Given: Create a Segment and set the segment category type to ENTER_PORT.
        final String connectId = UUID.randomUUID().toString();

        final Calendar cal = Calendar.getInstance();
        cal.set(1920, 06, 06);
        final Date date1 = cal.getTime();
        cal.set(1930, 06, 06);
        final Date date2 = cal.getTime();

        final Movement fromMovement = createMovement(0d, 0d, 0d, SegmentCategoryType.EXIT_PORT, connectId, "ONE", date1);
        final Movement toMovement = createMovement(1d, 1d, 0d, SegmentCategoryType.GAP, connectId, "TWO", date2);

        final Segment segment = createSegment(fromMovement, toMovement);

        final Track track = MovementModelToEntityMapper.createTrack(segment);
        final double totalTimeAtSeaBeforeUpdate = track.getTotalTimeAtSea();

        //When
        segment.setSegmentCategory(SegmentCategoryType.ENTER_PORT);
        updateTrack(track, toMovement, segment);
        final double totalTimeAtSeaAfterUpdate = track.getTotalTimeAtSea();

        //Then
        assertNotEquals(totalTimeAtSeaBeforeUpdate, totalTimeAtSeaAfterUpdate);
    }


    @Test
    @OperateOnDeployment("normal")
    public void testUpdateSegment_checkThatSegmentHasChanged() throws MovementDuplicateException, MovementDaoException, MovementModelException, MovementDaoMappingException, GeometryUtilException {

        //Given - A segment and two movements.
        final String connectId = UUID.randomUUID().toString();

        final Calendar cal = Calendar.getInstance();
        cal.set(1920, 06, 06);
        final Date date1 = cal.getTime();
        cal.set(1930, 06, 06);
        final Date date2 = cal.getTime();

        final Movement fromMovement = createMovement(0d, 0d, 0d, SegmentCategoryType.EXIT_PORT, connectId, "ONE", date1);
        final Movement toMovement = createMovement(1d, 1d, 0d, SegmentCategoryType.GAP, connectId, "TWO", date2);

        final Segment segmentBeforeUpdate = createSegment(fromMovement, toMovement);

        //When
        updateSegment(segmentBeforeUpdate, fromMovement, toMovement);
        final Segment segmentAfterUpdate = toMovement.getToSegment();

        //Then
        assertNotEquals(segmentBeforeUpdate, segmentAfterUpdate);
    }


    private Movement createMovement(final double longitude, final double latitude, final double altitude, final SegmentCategoryType segmentCategoryType, final String connectId, final String userName, final Date positionTime) throws MovementModelException, MovementDuplicateException, MovementDaoException {
        MovementType movementType = testUtil.createMovementType(longitude, latitude, altitude, segmentCategoryType, connectId);
        movementType.setPositionTime(positionTime);
        movementType = movementBatchModelBean.createMovement(movementType, userName);
        em.flush();
        Assert.assertNotNull(movementType.getConnectId());
        final MovementConnect movementConnect = movementDaoBean.getMovementConnectByConnectId(movementType.getConnectId());
        final List<Movement> movementList = movementConnect.getMovementList();
        Assert.assertNotNull(movementList);
        return movementList.get(movementList.size() - 1);
    }
}
