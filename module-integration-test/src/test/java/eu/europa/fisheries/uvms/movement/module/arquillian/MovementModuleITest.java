package eu.europa.fisheries.uvms.movement.module.arquillian;

import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
public class MovementModuleITest extends BuildMovementModuleTestDeployment {

    @Test
    @OperateOnDeployment("movementmodule")
    public void validateEarModuleDeploymentTest()  {
    }

}
