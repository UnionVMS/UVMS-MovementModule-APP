package eu.europa.ec.fisheries.uvms.movement.mapper;

import static eu.europa.ec.fisheries.uvms.movement.mapper.MovementModelToEntityMapper.createActivity;
import static eu.europa.ec.fisheries.uvms.movement.mapper.MovementModelToEntityMapper.createSegment;
import static eu.europa.ec.fisheries.uvms.movement.mapper.MovementModelToEntityMapper.mapNewMovementEntity;
import static eu.europa.ec.fisheries.uvms.movement.mapper.MovementModelToEntityMapper.mapToAreaType;
import static eu.europa.ec.fisheries.uvms.movement.mapper.MovementModelToEntityMapper.mapToMovementMetaData;
import static eu.europa.ec.fisheries.uvms.movement.mapper.MovementModelToEntityMapper.maptoArea;
import static eu.europa.ec.fisheries.uvms.movement.mapper.MovementModelToEntityMapper.updateSegment;
import static org.hamcrest.core.Is.is;

import java.util.UUID;

import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.europa.ec.fisheries.uvms.movement.arquillian.bean.util.TestUtil;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.MovementDaoMappingException;
import eu.europa.ec.fisheries.uvms.movement.entity.Activity;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.Movementmetadata;
import eu.europa.ec.fisheries.uvms.movement.entity.Segment;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.exception.GeometryUtilException;

/**
 * Created by roblar on 2017-03-31.
 */
public class MovementModelToEntityMapperTest extends Assert {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

	
    private final TestUtil testUtil = new TestUtil();

    @Test
    public void testMapNewMovementEntity_reportedSpeedIsNull() throws MovementDaoException, MovementDaoMappingException {

        //Given
        final String uuid = UUID.randomUUID().toString();

        final MovementType movementType = testUtil.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid);
        movementType.setReportedSpeed(null);

        //When
        final Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertNull(movement.getSpeed());
    }

    @Test
    public void testMapNewMovementEntity_reportedCourseIsNull() throws MovementDaoException, MovementDaoMappingException {

        //Given
        final String uuid = UUID.randomUUID().toString();

        final MovementType movementType = testUtil.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid);
        movementType.setReportedCourse(null);

        //When
        final Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertNull(movement.getHeading());
    }

    @Test
    public void testMapNewMovementEntity_positionIsNull() throws MovementDaoException, MovementDaoMappingException {

        //Given
        final String uuid = UUID.randomUUID().toString();

        final MovementType movementType = testUtil.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid);
        movementType.setPosition(null);

        //When
        final Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertNull(movement.getLocation());
    }

    @Test
    public void testMapNewMovementEntity_ifSourceIsNullThenMovementSourceTypeIs_INMARSATC() throws MovementDaoException, MovementDaoMappingException {

        //Given
        final String uuid = UUID.randomUUID().toString();

        final MovementType movementType = testUtil.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid);
        movementType.setSource(null);

        //When
        final Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertThat(movement.getMovementSource(), is(MovementSourceType.INMARSAT_C));
    }

    @Test
    public void testMapNewMovementEntity_ifMovementTypeIsNullThenMovementTypeTypeIs_POS() throws MovementDaoException, MovementDaoMappingException {

        //Given
        final String uuid = UUID.randomUUID().toString();

        final MovementType movementType = testUtil.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid);
        movementType.setMovementType(null);

        //When
        final Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertThat(movement.getMovementType(), is(MovementTypeType.POS));
    }

    @Test
    public void testMapNewMovementEntity_ifPositionTimeIsNullThenTimeStampIsSet() throws MovementDaoException, MovementDaoMappingException {

        //Given
        final String uuid = UUID.randomUUID().toString();

        final MovementType movementType = testUtil.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid);
        movementType.setPositionTime(null);

        //When
        final Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertNotNull(movement.getTimestamp());
    }

    @Test
    public void testMapNewMovementEntity_activityIsNull() throws MovementDaoException, MovementDaoMappingException {

        //Given
        final String uuid = UUID.randomUUID().toString();

        final MovementType movementType = testUtil.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid);
        movementType.setActivity(null);

        //When
        final Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertNull(movement.getActivity());
    }

    @Test
    public void testMapNewMovementEntity_metaDataIsNull() throws MovementDaoException, MovementDaoMappingException  {

        //Given
        final String uuid = UUID.randomUUID().toString();

        final MovementType movementType = testUtil.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid);
        movementType.setMetaData(null);

        //When
        final Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertNull(movement.getMetadata());
    }

    @Test
    public void testMapToMovementMetaData_ifClosestPortIsNullThenClosestPortCodeAndDistanceAndRemoteIdAndNameAreNull() throws MovementDaoException, MovementDaoMappingException {

        //Given
        final String uuid = UUID.randomUUID().toString();

        final MovementType movementType = testUtil.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid);
        final MovementMetaData movementMetaDataToBeMapped = movementType.getMetaData();

        movementMetaDataToBeMapped.setClosestPort(null);

        //When
        final Movementmetadata mappedMovementMetaData = mapToMovementMetaData(movementMetaDataToBeMapped);

        //Then
        assertNull(mappedMovementMetaData.getClosestPortCode());
        assertNull(mappedMovementMetaData.getClosestPortDistance());
        assertNull(mappedMovementMetaData.getClosestPortRemoteId());
        assertNull(mappedMovementMetaData.getClosestPortName());

    }

    @Test
    public void testMapToMovementMetaData_ifClosestCountryIsNullThenClosestCountryCodeAndDistanceAndRemoteIdAndNameAreNull() throws MovementDaoException, MovementDaoMappingException {

        //Given
        final String uuid = UUID.randomUUID().toString();

        final MovementType movementType = testUtil.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid);
        final MovementMetaData movementMetaDataToBeMapped = movementType.getMetaData();

        movementMetaDataToBeMapped.setClosestCountry(null);

        //When
        final Movementmetadata mappedMovementMetaData = mapToMovementMetaData(movementMetaDataToBeMapped);

        //Then
        assertNull(mappedMovementMetaData.getClosestCountryCode());
        assertNull(mappedMovementMetaData.getClosestCountryDistance());
        assertNull(mappedMovementMetaData.getClosestCountryRemoteId());
        assertNull(mappedMovementMetaData.getClosestCountryName());

    }

    @Test
    public void testMapToAreaType() {

        final MovementMetaDataAreaType movementMetaDataAreaType = new MovementMetaDataAreaType();

        final AreaType areaType = mapToAreaType(movementMetaDataAreaType);

        assertNotNull(areaType);
        assertThat(areaType.getUpdatedUser(), is("UVMS"));
        assertNotNull(areaType.getUpdatedTime());
    }

    @Test
    public void testMapToArea() {

        final MovementMetaDataAreaType movementMetaDataAreaType = new MovementMetaDataAreaType();
        final AreaType areaType = mapToAreaType(movementMetaDataAreaType);

        final Area area = maptoArea(movementMetaDataAreaType, areaType);

        assertNotNull(area);
        assertThat(area.getAreaUpuser(), is("UVMS"));
        assertNotNull(area.getAreaUpdattim());
    }

    @Test(expected = GeometryUtilException.class)
    public void testUpdateSegment_emptyMovementsThrowsGeometryUtilException() throws MovementDaoMappingException, GeometryUtilException {

        final Movement fromMovement = new Movement();
        final Movement toMovement = new Movement();

        final Segment segment = createSegment(fromMovement, toMovement);

        updateSegment(segment, fromMovement, toMovement);

        thrown.expect(GeometryUtilException.class);
        thrown.expectMessage("CurrentPosition is null!");
    }
    
    @Test(expected = GeometryUtilException.class)
    public void testCreateSegment_emptyMovementsThrowsGeometryUtilException() throws MovementDaoMappingException, GeometryUtilException {

        final Movement fromMovement = new Movement();
        final Movement toMovement = new Movement();

        createSegment(fromMovement, toMovement);

        thrown.expect(GeometryUtilException.class);
        thrown.expectMessage("CurrentPosition is null!");
    }

    @Test
    public void testCreateActivity_mapFromMovementBaseTypeToActivity() throws MovementDaoException, MovementDaoMappingException {

        //Given
        final String uuid = UUID.randomUUID().toString();
        final MovementBaseType movementBaseType = testUtil.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid);

        //When
        final Activity activity = createActivity(movementBaseType);

        //Then
        assertNotNull(activity);
    }


}
