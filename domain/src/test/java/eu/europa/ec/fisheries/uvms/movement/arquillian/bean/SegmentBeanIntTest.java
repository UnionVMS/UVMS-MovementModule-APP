package eu.europa.ec.fisheries.uvms.movement.arquillian.bean;

import eu.europa.ec.fisheries.uvms.movement.arquillian.BuildMovementTestDeployment;
import eu.europa.ec.fisheries.uvms.movement.arquillian.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.bean.SegmentBean;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;

@RunWith(Arquillian.class)
public class SegmentBeanIntTest extends TransactionalTests {

    @EJB
    SegmentBean segmentBean;

    @Deployment
    public static Archive<?> createDeployment() {
        return BuildMovementTestDeployment.createDeployment();
    }


    @Test
    public void testMe() {
        Assert.assertTrue(true);
    }
}
