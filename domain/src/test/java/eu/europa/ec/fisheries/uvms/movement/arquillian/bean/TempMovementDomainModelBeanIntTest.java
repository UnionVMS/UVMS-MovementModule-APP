package eu.europa.ec.fisheries.uvms.movement.arquillian.bean;

import java.util.UUID;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import eu.europa.ec.fisheries.schema.movement.asset.v1.VesselType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.movement.v1.TempMovementStateEnum;
import eu.europa.ec.fisheries.schema.movement.v1.TempMovementType;
import eu.europa.ec.fisheries.uvms.movement.arquillian.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.model.TempMovementDomainModel;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;


@RunWith(Arquillian.class)
public class TempMovementDomainModelBeanIntTest extends TransactionalTests {

	@EJB
	TempMovementDomainModel tempMovementDomainModel;
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	
    @Test
    @OperateOnDeployment("normal")
    public void createTempMovementNullTempMovementCheckFailureTest() throws MovementModelException {
    	thrown.expect(MovementModelException.class);
        thrown.expectMessage("Could not create temp movement. TempMovementType is null, aborting mapping");
        
        String username = TempMovementDomainModelBeanIntTest.class.getSimpleName() + UUID.randomUUID().toString();
		tempMovementDomainModel.createTempMovement(null, username);
    }

    @Test
    @OperateOnDeployment("normal")
    public void createTempMovementNullUsernameCheckFailureTest() throws MovementModelException {
    	thrown.expect(MovementModelException.class);
        thrown.expectMessage("Could not create temp movement. TempMovementType is null, aborting mapping");
		tempMovementDomainModel.createTempMovement(new TempMovementType(),null);
    }

    @Test
    @OperateOnDeployment("normal")
    public void createTempMovementSuccessTest() throws MovementModelException {
        String username = TempMovementDomainModelBeanIntTest.class.getSimpleName();
        
		TempMovementType tempMovementType = createTestTempMovementType(10d, 10d, 10d);
		TempMovementType createTempMovement = tempMovementDomainModel.createTempMovement(tempMovementType,username);
        em.flush();
        Assert.assertNotNull(createTempMovement); 
        Assert.assertNotNull(createTempMovement.getUpdatedTime()); 
        Assert.assertNotNull(createTempMovement.getGuid()); 
       
        Assert.assertEquals(tempMovementType.getSpeed(), createTempMovement.getSpeed());
        Assert.assertEquals(TempMovementStateEnum.DRAFT, createTempMovement.getState());   
        Assert.assertEquals(tempMovementType.getStatus(), createTempMovement.getStatus());   
        
        Assert.assertEquals(tempMovementType.getAsset(), createTempMovement.getAsset());
        
        Assert.assertEquals(tempMovementType.getPosition().getLongitude(), createTempMovement.getPosition().getLongitude());
        Assert.assertEquals(tempMovementType.getPosition().getLatitude(), createTempMovement.getPosition().getLatitude());
        Assert.assertNull(createTempMovement.getPosition().getAltitude());                   
    }

	private TempMovementType createTestTempMovementType(double longitude, double latitude, double altitude) {
		TempMovementType tempMovementType = new TempMovementType();
		VesselType vesselType = new VesselType();
		vesselType.setCfr("Cfr");
		vesselType.setExtMarking("ExtMarking");
		vesselType.setFlagState("SE");
		vesselType.setIrcs("ircs");
		vesselType.setName("name");
		
		tempMovementType.setAsset(vesselType);
		tempMovementType.setCourse(90.0);
		
        MovementPoint movementPoint = new MovementPoint();
        movementPoint.setLongitude(longitude);
        movementPoint.setLatitude(latitude);
        movementPoint.setAltitude(altitude);
		
		tempMovementType.setPosition(movementPoint);
		tempMovementType.setSpeed(10.0);
		tempMovementType.setStatus("status");
		tempMovementType.setTime("2017-01-27 11:12:13 -0500");
		tempMovementType.setUpdatedTime(null);
		return tempMovementType;
	}

}
