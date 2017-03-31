package eu.europa.ec.fisheries.uvms.movement.mapper;

import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.arquillian.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.arquillian.bean.util.TestUtil;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.MovementDaoMappingException;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDaoException;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static eu.europa.ec.fisheries.uvms.movement.mapper.MovementModelToEntityMapper.mapNewMovementEntity;
import static org.hamcrest.core.Is.is;
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
        LOG.info(" [ MovementModelToEntityMapperIntTest.testMapNewMovementEntity: MovementType with reported speed not set maps to Movement with speed not set. ] ");
    }

    @Test
    @OperateOnDeployment("normal")
    public void testMapNewMovementEntity_reportedCourseIsNull() throws MovementDaoException, MovementDaoMappingException  {

        //Given
        String uuid = UUID.randomUUID().toString();

        MovementType movementType = testUtil.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid);
        movementType.setReportedCourse(null);

        //When
        Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertNull(movement.getHeading());
        LOG.info(" [ MovementModelToEntityMapperIntTest.testMapNewMovementEntity: MovementType with reported course not set maps to Movement with heading not set. ] ");
    }

    @Test
    @OperateOnDeployment("normal")
    public void testMapNewMovementEntity_positionIsNull() throws MovementDaoException, MovementDaoMappingException  {

        //Given
        String uuid = UUID.randomUUID().toString();

        MovementType movementType = testUtil.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid);
        movementType.setPosition(null);

        //When
        Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertNull(movement.getLocation());
        LOG.info(" [ MovementModelToEntityMapperIntTest.testMapNewMovementEntity: MovementType with position not set maps to Movement with location not set. ] ");
    }

    @Test
    @OperateOnDeployment("normal")
    public void testMapNewMovementEntity_ifSourceIsNullThenMovementSourceTypeIs_INMARSATC() throws MovementDaoException, MovementDaoMappingException  {

        //Given
        String uuid = UUID.randomUUID().toString();

        MovementType movementType = testUtil.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid);
        movementType.setSource(null);

        //When
        Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertThat(movement.getMovementSource(), is(MovementSourceType.INMARSAT_C));
        LOG.info(" [ MovementModelToEntityMapperIntTest.testMapNewMovementEntity: MovementType with source not set maps to Movement with MovementSourceType set to INMARSAT_C. ] ");
    }

    @Test
    @OperateOnDeployment("normal")
    public void testMapNewMovementEntity_ifMovementTypeIsNullThenMovementTypeTypeIs_POS() throws MovementDaoException, MovementDaoMappingException  {

        //Given
        String uuid = UUID.randomUUID().toString();

        MovementType movementType = testUtil.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid);
        movementType.setMovementType(null);

        //When
        Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertThat(movement.getMovementType(), is(MovementTypeType.POS));
        LOG.info(" [ MovementModelToEntityMapperIntTest.testMapNewMovementEntity: MovementType with MovementTypeType not set maps to Movement with MovementTypeType set to POS. ] ");
    }

    @Test
    @OperateOnDeployment("normal")
    public void testMapNewMovementEntity_ifPositionTimeIsNullThenTimeStampIsSet() throws MovementDaoException, MovementDaoMappingException  {

        //Given
        String uuid = UUID.randomUUID().toString();

        MovementType movementType = testUtil.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid);
        movementType.setPositionTime(null);

        //When
        Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertNotNull(movement.getTimestamp());
        LOG.info(" [ MovementModelToEntityMapperIntTest.testMapNewMovementEntity: MovementType with position time not set maps to Movement with timestamp set to current time. ] ");
    }

    @Test
    @OperateOnDeployment("normal")
    public void testMapNewMovementEntity_activityIsNull() throws MovementDaoException, MovementDaoMappingException  {

        //Given
        String uuid = UUID.randomUUID().toString();

        MovementType movementType = testUtil.createMovementType(1d, 1d, 0, SegmentCategoryType.EXIT_PORT, uuid);
        movementType.setActivity(null);

        //When
        Movement movement = mapNewMovementEntity(movementType, "testUser");

        //Then
        assertNull(movement.getActivity());
        LOG.info(" [ MovementModelToEntityMapperIntTest.testMapNewMovementEntity: MovementType with MovementActivityType not set maps to Movement with Activity not set. ] ");
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
        LOG.info(" [ MovementModelToEntityMapperIntTest.testMapNewMovementEntity: MovementType with MovementMetaData not set maps to Movement with Movementmetadata (spelled correct) not set. ] ");
    }
}
