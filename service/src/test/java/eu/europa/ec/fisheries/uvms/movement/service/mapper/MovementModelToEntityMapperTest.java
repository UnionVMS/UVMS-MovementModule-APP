package eu.europa.ec.fisheries.uvms.movement.service.mapper;

import static eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementModelToEntityMapper.createActivity;
import static eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementModelToEntityMapper.mapNewMovementEntity;
import static eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementModelToEntityMapper.mapToAreaType;
import static eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementModelToEntityMapper.mapToMovementMetaData;
import static eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementModelToEntityMapper.maptoArea;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.UUID;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaData;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaDataAreaType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.service.MockData;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Activity;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movementmetadata;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;

/**
 * Created by roblar on 2017-03-31.
 */
@RunWith(Arquillian.class)
public class MovementModelToEntityMapperTest extends TransactionalTests {

    @Test
    public void testMapNewMovementEntity_reportedSpeedIsNull() throws MovementServiceException {

        //Given
        String uuid = UUID.randomUUID().toString();

        MovementType movementType = MockData.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid,0);
        movementType.setReportedSpeed(null);

        //When
        Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertNull(movement.getSpeed());
        
    }

    @Test
    public void testMapNewMovementEntity_reportedCourseIsNull() throws MovementServiceException {

        //Given
        String uuid = UUID.randomUUID().toString();

        MovementType movementType = MockData.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid,0);
        movementType.setReportedCourse(null);

        //When
        Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertNull(movement.getHeading());
    }

    @Test
    public void testMapNewMovementEntity_positionIsNull() throws MovementServiceException {

        //Given
        String uuid = UUID.randomUUID().toString();

        MovementType movementType = MockData.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid,0);
        movementType.setPosition(null);

        //When
        Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertNull(movement.getLocation());
    }

    @Test
    public void testMapNewMovementEntity_ifSourceIsNullThenMovementSourceTypeIs_INMARSATC() throws MovementServiceException {

        //Given
        String uuid = UUID.randomUUID().toString();

        MovementType movementType = MockData.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid,0);
        movementType.setSource(null);

        //When
        Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertThat(movement.getMovementSource(), is(MovementSourceType.INMARSAT_C));
        
    }

    @Test
    public void testMapNewMovementEntity_ifMovementTypeIsNullThenMovementTypeTypeIs_POS() throws MovementServiceException {

        //Given
        String uuid = UUID.randomUUID().toString();

        MovementType movementType = MockData.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid,0);
        movementType.setMovementType(null);

        //When
        Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertThat(movement.getMovementType(), is(MovementTypeType.POS));
    }

    @Test
    public void testMapNewMovementEntity_ifPositionTimeIsNullThenTimeStampIsSet() throws MovementServiceException {

        //Given
        String uuid = UUID.randomUUID().toString();

        MovementType movementType = MockData.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid,0);
        movementType.setPositionTime(null);

        //When
        Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertNotNull(movement.getTimestamp());
        
    }

    @Test
    public void testMapNewMovementEntity_activityIsNull() throws MovementServiceException {

        //Given
        String uuid = UUID.randomUUID().toString();

        MovementType movementType = MockData.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid,0);
        movementType.setActivity(null);

        //When
        Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertNull(movement.getActivity());
    }

    @Test
    public void testMapNewMovementEntity_metaDataIsNull() throws MovementServiceException  {

        //Given
        String uuid = UUID.randomUUID().toString();

        MovementType movementType = MockData.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid,0);
        movementType.setMetaData(null);

        //When
        Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertNull(movement.getMetadata());
    }

    @Test
    public void testMapToMovementMetaData_ifClosestPortIsNullThenClosestPortCodeAndDistanceAndRemoteIdAndNameAreNull() {

        //Given
        String uuid = UUID.randomUUID().toString();

        MovementType movementType = MockData.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid,0);
        MovementMetaData movementMetaDataToBeMapped = movementType.getMetaData();

        movementMetaDataToBeMapped.setClosestPort(null);

        //When
        Movementmetadata mappedMovementMetaData = mapToMovementMetaData(movementMetaDataToBeMapped);

        //Then
        assertNull(mappedMovementMetaData.getClosestPortCode());
        assertNull(mappedMovementMetaData.getClosestPortDistance());
        assertNull(mappedMovementMetaData.getClosestPortRemoteId());
        assertNull(mappedMovementMetaData.getClosestPortName());
    }

    @Test
    public void testMapToMovementMetaData_ifClosestCountryIsNullThenClosestCountryCodeAndDistanceAndRemoteIdAndNameAreNull() {

        //Given
        String uuid = UUID.randomUUID().toString();

        MovementType movementType = MockData.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid,0);
        MovementMetaData movementMetaDataToBeMapped = movementType.getMetaData();

        movementMetaDataToBeMapped.setClosestCountry(null);

        //When
        Movementmetadata mappedMovementMetaData = mapToMovementMetaData(movementMetaDataToBeMapped);

        //Then
        assertNull(mappedMovementMetaData.getClosestCountryCode());
        assertNull(mappedMovementMetaData.getClosestCountryDistance());
        assertNull(mappedMovementMetaData.getClosestCountryRemoteId());
        assertNull(mappedMovementMetaData.getClosestCountryName());
    }

    @Test
    public void testMapToAreaType() {

        MovementMetaDataAreaType movementMetaDataAreaType = new MovementMetaDataAreaType();

        AreaType areaType = mapToAreaType(movementMetaDataAreaType);

        assertNotNull(areaType);
        assertThat(areaType.getUpdatedUser(), is("UVMS"));
        assertNotNull(areaType.getUpdatedTime());
        
        try {
			areaType = mapToAreaType(null);
			fail("shpould result in a nullpointer");
		} catch (NullPointerException e) {
			assertTrue(true);
		}
    }

    @Test
    public void testMapToArea() {

        MovementMetaDataAreaType movementMetaDataAreaType = new MovementMetaDataAreaType();
        AreaType areaType = mapToAreaType(movementMetaDataAreaType);

        Area area = maptoArea(movementMetaDataAreaType, areaType);

        assertNotNull(area);
        assertThat(area.getAreaUpuser(), is("UVMS"));
        assertNotNull(area.getAreaUpdattim());
        
        try {
        	area = maptoArea(null, areaType);
        	fail("should result in a nullpointer");
		} catch (Exception e) {
			assertTrue(true);
		}
        
    }

    @Test
    public void testCreateActivity_mapFromMovementBaseTypeToActivity() throws MovementServiceException {

        //Given
        String uuid = UUID.randomUUID().toString();
        MovementBaseType movementBaseType = MockData.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid,0);

        //When
        Activity activity = createActivity(movementBaseType);

        //Then
        assertNotNull(activity);
    }
}
