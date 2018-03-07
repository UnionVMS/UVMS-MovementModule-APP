package eu.europa.ec.fisheries.uvms.movement.arquillian.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import eu.europa.ec.fisheries.uvms.movement.arquillian.TransactionalTests;

import eu.europa.ec.fisheries.schema.movement.area.v1.AreaType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSegment;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.arquillian.BuildMovementTestDeployment;
import eu.europa.ec.fisheries.uvms.movement.bean.MovementDomainModelBean;
import eu.europa.ec.fisheries.uvms.movement.mapper.search.SearchField;
import eu.europa.ec.fisheries.uvms.movement.mapper.search.SearchValue;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDaoException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;

/**
 * Created by thofan on 2017-02-25.
 */

@RunWith(Arquillian.class)
public class MovementDomainModelBeanIntTest extends TransactionalTests {

    Random rnd = new Random();

    private final static String TEST_USER_NAME = "Arquillian";

    @EJB
    private MovementDomainModelBean movementDomainModelBean;


    /******************************************************************************************************************
     *   TEST FUNCTIONS
     ******************************************************************************************************************/


    @Test
    @OperateOnDeployment("normal")
    public void filterSegments() {
        try {

            // TODO should every end of ifstatement have a continue instead of checking for something that will never occur ? method keepSegment

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

            Assert.assertTrue(segments.size() == 1);
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }


    @Test
    @OperateOnDeployment("normal")
    public void getAreas() {
        try {
            List<AreaType> areas = movementDomainModelBean.getAreas();
            Assert.assertTrue(areas != null);
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    @OperateOnDeployment("normal")
    public void getLatestMovements_0() {
        try {
            List<MovementType> movementTypes = movementDomainModelBean.getLatestMovements(0);
            Assert.assertTrue(movementTypes != null);
        } catch (MovementModelException e) {
            Assert.fail();
        }
    }

    @Test
    @OperateOnDeployment("normal")
    public void getLatestMovements_5() {
        try {
            List<MovementType> movementTypes =movementDomainModelBean.getLatestMovements(5);
            Assert.assertTrue(movementTypes != null);
        } catch (MovementModelException e) {
            Assert.fail();
        }
    }

    @Test
    @OperateOnDeployment("normal")
    public void getLatestMovements_5000000() {
        try {
            List<MovementType> movementTypes =movementDomainModelBean.getLatestMovements(5000000);
            Assert.assertTrue(movementTypes != null);
        } catch (MovementModelException e) {
            Assert.fail();
        }
    }

    @Test
    @OperateOnDeployment("normal")
    public void getLatestMovements_NULL() {
        try {
            List<MovementType> movementTypes = movementDomainModelBean.getLatestMovements(null);
            Assert.assertTrue(movementTypes != null);
        } catch (MovementModelException e) {
            Assert.assertTrue(e != null);
        }
    }

    @Test
    @OperateOnDeployment("normal")
    public void getLatestMovements_neg5() {
        try {
            List<MovementType> movementTypes = movementDomainModelBean.getLatestMovements(-5);
            Assert.assertTrue(movementTypes != null);
        } catch (MovementModelException e) {
            Assert.assertTrue(e != null);
        }
    }

    @Test
    @OperateOnDeployment("normal")
    public void getLatestMovementsByConnectIds_crashOnEmpty() {
        try {
            List<String> connectIds = new ArrayList<>();
            List<MovementType> movementTypes = movementDomainModelBean.getLatestMovementsByConnectIds(connectIds);
            Assert.assertTrue(movementTypes != null);
        } catch (Exception e) {
            Assert.assertTrue(e != null);
        }
    }

    // @Test
    public void getMinimalMovementListByQuery() {
        try {
            movementDomainModelBean.getMinimalMovementListByQuery(null);
        } catch (MovementModelException e) {
            Assert.fail(e.toString());
        }
    }

    // @Test
    public void getMovementByGUID() {
        try {
            movementDomainModelBean.getMovementByGUID(null);
        } catch (MovementModelException e) {
            Assert.fail(e.toString());
        }
    }

    // @Test
    public void getMovementListByAreaAndTimeInterval() {
        try {
            movementDomainModelBean.getMovementListByAreaAndTimeInterval(null);
        } catch (MovementDaoException e) {
            Assert.fail(e.toString());
        }
    }

    // @Test
    public void getMovementListByQuery() {
        try {
            movementDomainModelBean.getMovementListByQuery(null);
        } catch (MovementModelException e) {
            Assert.fail(e.toString());
        }
    }

    // @Test
    public void getMovementMapByQuery() {
        try {
            movementDomainModelBean.getMovementMapByQuery(null);
        } catch (MovementModelException e) {
            Assert.fail(e.toString());
        }
    }

    // @Test
    public void keepSegment() {
        try {
            movementDomainModelBean.keepSegment(null, null);
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }

    // @Test
    public void removeTrackMismatches() {
        try {
            movementDomainModelBean.removeTrackMismatches(null, null);
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
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
        SearchValue searchValue = new SearchValue(SearchField.SEGMENT_DURATION, "10", "20");
        return searchValue;
    }

    private SearchValue createSearchValueLengthHelper() {
        SearchValue searchValue = new SearchValue(SearchField.SEGMENT_LENGTH, "1000", "2000");
        return searchValue;
    }

    private SearchValue createSearchValueSpeedHelper() {
        SearchValue searchValue = new SearchValue(SearchField.SEGMENT_SPEED, "100", "200");
        return searchValue;
    }


}
