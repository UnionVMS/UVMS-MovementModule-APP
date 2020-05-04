package eu.europa.ec.fisheries.uvms.movement.service.mapper;

import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.service.MockData;
import eu.europa.ec.fisheries.uvms.movement.service.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static eu.europa.ec.fisheries.uvms.movement.service.mapper.MovementModelToEntityMapper.mapNewMovementEntity;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Created by roblar on 2017-03-31.
 */
@RunWith(Arquillian.class)
public class MovementModelToEntityMapperTest extends TransactionalTests {

    @Test
    @OperateOnDeployment("movementservice")
    public void testMapNewMovementEntity_reportedSpeedIsNull() {

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
    @OperateOnDeployment("movementservice")
    public void testMapNewMovementEntity_reportedCourseIsNull() {

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
    @OperateOnDeployment("movementservice")
    public void testMapNewMovementEntity_positionIsNull() {

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
    @OperateOnDeployment("movementservice")
    public void testMapNewMovementEntity_ifSourceIsNullThenMovementSourceTypeIs_INMARSATC() {

        //Given
        String uuid = UUID.randomUUID().toString();

        MovementType movementType = MockData.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid,0);
        movementType.setSource(null);

        //When
        Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertThat(movement.getSource(), is(MovementSourceType.INMARSAT_C));
        
    }

    @Test
    @OperateOnDeployment("movementservice")
    public void testMapNewMovementEntity_ifMovementTypeIsNullThenMovementTypeTypeIs_POS() {

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
    @OperateOnDeployment("movementservice")
    public void testMapNewMovementEntity_ifPositionTimeIsNullThenTimeStampIsSet() {

        //Given
        String uuid = UUID.randomUUID().toString();

        MovementType movementType = MockData.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid,0);
        movementType.setPositionTime(null);

        //When
        Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertNotNull(movement.getTimestamp());
        
    }


}
