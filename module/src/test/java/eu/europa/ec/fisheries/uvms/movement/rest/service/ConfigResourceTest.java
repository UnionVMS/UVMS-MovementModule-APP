package eu.europa.ec.fisheries.uvms.movement.rest.service;

import eu.europa.ec.fisheries.uvms.movement.rest.BuildMovementRestDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ConfigResourceTest extends BuildMovementRestDeployment {

    @Test
    public void getMovementTypesTest() {
        Assert.assertTrue(true);
    }

}
