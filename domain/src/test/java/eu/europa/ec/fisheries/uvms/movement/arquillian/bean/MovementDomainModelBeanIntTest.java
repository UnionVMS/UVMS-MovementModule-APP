package eu.europa.ec.fisheries.uvms.movement.arquillian.bean;

import eu.europa.ec.fisheries.schema.movement.area.v1.AreaType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSegment;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.arquillian.BuildMovementTestDeployment;
import eu.europa.ec.fisheries.uvms.movement.bean.MovementDomainModelBean;
import eu.europa.ec.fisheries.uvms.movement.mapper.search.SearchField;
import eu.europa.ec.fisheries.uvms.movement.mapper.search.SearchValue;
import eu.europa.ec.fisheries.uvms.movement.model.exception.InputArgumentException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDaoException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;
import javax.inject.Inject;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Created by thofan on 2017-02-25.
 */

@RunWith(Arquillian.class)
public class MovementDomainModelBeanIntTest extends BuildMovementTestDeployment {

    private Random rnd = new Random();

    private final static String TEST_USER_NAME = "Arquillian";

    @Inject
    private UserTransaction userTransaction;

    @EJB
    private MovementDomainModelBean movementDomainModelBean;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /******************************************************************************************************************
     *   SETUP FUNCTIONS
     ******************************************************************************************************************/

    @Before
    public void before() throws SystemException, NotSupportedException {
        userTransaction.begin();
    }

    @After
    public void after() throws SystemException {
        userTransaction.rollback();
    }

    /******************************************************************************************************************
     *   TEST FUNCTIONS
     ******************************************************************************************************************/

    @Test
    @OperateOnDeployment("normal")
    public void filterSegments() throws MovementModelException {

        List<SearchValue> searchKeyValuesRange = new ArrayList<>();
        searchKeyValuesRange.add(createSearchValueDurationHelper());
        searchKeyValuesRange.add(createSearchValueLengthHelper());
        searchKeyValuesRange.add(createSearchValueSpeedHelper());

        // just try to satisfy all paths in the filter
        List<MovementSegment> movementSegments = new ArrayList<>();
        movementSegments.add(createMovementSegmentDurationHelper(9,900, 90));
        movementSegments.add(createMovementSegmentDurationHelper(15,1500, 150));
        movementSegments.add(createMovementSegmentDurationHelper(21,2100, 210));

        ArrayList<MovementSegment> segments = movementDomainModelBean.filterSegments(movementSegments, searchKeyValuesRange);
        assertEquals(1,segments.size());
    }

    @Test
    @OperateOnDeployment("normal")
    public void getAreas() throws MovementModelException {
        List<AreaType> areas = movementDomainModelBean.getAreas();
        assertNotNull(areas);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getLatestMovements_0() throws MovementModelException {
        List<MovementType> movementTypes = movementDomainModelBean.getLatestMovements(0);
        assertNotNull(movementTypes);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getLatestMovements_5() throws MovementModelException {
        List<MovementType> movementTypes =movementDomainModelBean.getLatestMovements(5);
        assertNotNull(movementTypes);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getLatestMovements_5000000() throws MovementModelException {
        List<MovementType> movementTypes =movementDomainModelBean.getLatestMovements(5000000);
        assertNotNull(movementTypes);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getLatestMovements_NULL() throws MovementModelException {
        expectedException.expect(MovementModelException.class);

        movementDomainModelBean.getLatestMovements(null);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getLatestMovements_neg5() throws MovementModelException {
        expectedException.expect(MovementModelException.class);

        movementDomainModelBean.getLatestMovements(-5);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getLatestMovementsByConnectIds_crashOnEmpty() throws MovementModelException {
        expectedException.expect(MovementModelException.class);
        expectedException.expectMessage("[ Error when getting Movement by GUID. ]");

        List<String> connectIds = new ArrayList<>();
        movementDomainModelBean.getLatestMovementsByConnectIds(connectIds);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMinimalMovementListByQuery_NULL() throws MovementModelException {
        expectedException.expect(InputArgumentException.class);
        expectedException.expectMessage("Movement list query is null");

        movementDomainModelBean.getMinimalMovementListByQuery(null);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMovementByGUID_NULL() throws MovementModelException {
        expectedException.expect(EJBTransactionRolledbackException.class);

        movementDomainModelBean.getMovementByGUID(null);
    }

     @Test
     @OperateOnDeployment("normal")
    public void getMovementListByAreaAndTimeInterval_NULL() throws MovementDaoException {
        expectedException.expect(NullPointerException.class);

         List<MovementType> movementTypeList = movementDomainModelBean.getMovementListByAreaAndTimeInterval(null);
         movementTypeList.get(0);
     }

    @Test
    @OperateOnDeployment("normal")
    public void getMovementListByQuery_NULL() throws MovementModelException {
        expectedException.expect(InputArgumentException.class);
        expectedException.expectMessage("Movement list query is null");

        movementDomainModelBean.getMovementListByQuery(null);
    }

    @Test
    @OperateOnDeployment("normal")
    public void getMovementMapByQuery_NULL() throws MovementModelException {
        expectedException.expect(InputArgumentException.class);
        expectedException.expectMessage("Movement list query is null");

        movementDomainModelBean.getMovementMapByQuery(null);
    }

    @Test
    @OperateOnDeployment("normal")
    public void keepSegment_NULL() throws MovementModelException {
        expectedException.expect(InputArgumentException.class);
        expectedException.expectMessage("MovementSegment or SearchValue list is null");

        movementDomainModelBean.keepSegment(null, null);
    }

    @Test
    @OperateOnDeployment("normal")
    public void removeTrackMismatches_NULL() throws MovementModelException {
        expectedException.expect(InputArgumentException.class);
        expectedException.expectMessage("MovementTrack list or Movement list is null");

        movementDomainModelBean.removeTrackMismatches(null, null);
    }

    /******************************************************************************************************************
     *   HELPER FUNCTIONS
     ******************************************************************************************************************/

    private MovementSegment createMovementSegmentDurationHelper(double durationValue, double distanceValue, double speedValue) {
        MovementSegment movementSegment = new MovementSegment();
        movementSegment.setId(String.valueOf(rnd.nextInt(1000)));
        movementSegment.setTrackId("TrackId_42");
        movementSegment.setCategory(SegmentCategoryType.OTHER);
        movementSegment.setDistance(distanceValue);
        movementSegment.setDuration(durationValue);
        movementSegment.setSpeedOverGround(speedValue);
        movementSegment.setCourseOverGround(0.6);
        movementSegment.setWkt("wkt");
        return movementSegment;
    }

    private SearchValue createSearchValueDurationHelper() {
        return new SearchValue(SearchField.SEGMENT_DURATION, "10", "20");
    }

    private SearchValue createSearchValueLengthHelper() {
        return new SearchValue(SearchField.SEGMENT_LENGTH, "1000", "2000");
    }

    private SearchValue createSearchValueSpeedHelper() {
        return new SearchValue(SearchField.SEGMENT_SPEED, "100", "200");
    }
}
