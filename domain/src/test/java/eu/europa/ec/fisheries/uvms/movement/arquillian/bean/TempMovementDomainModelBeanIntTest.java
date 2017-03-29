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
import eu.europa.ec.fisheries.uvms.movement.model.exception.InputArgumentException;
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

    @Test
    @OperateOnDeployment("normal")
    public void archiveTempMovementNullGuidCheckFailureTest() throws MovementModelException {
    	thrown.expect(InputArgumentException.class);
        thrown.expectMessage("Non valid id of temp movement to update");
        
        String username = TempMovementDomainModelBeanIntTest.class.getSimpleName() + UUID.randomUUID().toString();
		tempMovementDomainModel.archiveTempMovement(null, username);
    }


    @Test
    @OperateOnDeployment("normal")
    public void archiveTempMovementNullUsernameCheckFailureTest() throws MovementModelException {
    	thrown.expect(MovementModelException.class);
        thrown.expectMessage("Could not set temp movement state.");
		tempMovementDomainModel.archiveTempMovement("guid",null);
    }
    
    @Test
    @OperateOnDeployment("normal")
    public void archiveTempMovementSuccessTest() throws MovementModelException {
        String username = TempMovementDomainModelBeanIntTest.class.getSimpleName();
        
		TempMovementType tempMovementType = createTestTempMovementType(10d, 10d, 10d);
		TempMovementType createTempMovement = tempMovementDomainModel.createTempMovement(tempMovementType,username);
        em.flush();
       
        TempMovementType archiveTempMovement = tempMovementDomainModel.archiveTempMovement(createTempMovement.getGuid(), username);
        em.flush();
        Assert.assertNotNull(archiveTempMovement); 
        Assert.assertNotNull(archiveTempMovement.getUpdatedTime()); 
        Assert.assertNotNull(archiveTempMovement.getGuid()); 
        Assert.assertEquals(TempMovementStateEnum.DELETED, archiveTempMovement.getState());   
    }        
    
    
    

    @Test
    @OperateOnDeployment("normal")
    public void sendTempMovementNullGuidCheckFailureTest() throws MovementModelException {
    	thrown.expect(InputArgumentException.class);
        thrown.expectMessage("Non valid id of temp movement to update");
        
        String username = TempMovementDomainModelBeanIntTest.class.getSimpleName() + UUID.randomUUID().toString();
		tempMovementDomainModel.sendTempMovement(null, username);
    }


    @Test
    @OperateOnDeployment("normal")
    public void sendTempMovementNullUsernameCheckFailureTest() throws MovementModelException {
    	thrown.expect(MovementModelException.class);
        thrown.expectMessage("Could not set temp movement state.");
		tempMovementDomainModel.sendTempMovement("guid",null);
    }
    
    @Test
    @OperateOnDeployment("normal")
    public void sendTempMovementSuccessTest() throws MovementModelException {
        String username = TempMovementDomainModelBeanIntTest.class.getSimpleName();
        
		TempMovementType tempMovementType = createTestTempMovementType(10d, 10d, 10d);
		TempMovementType createTempMovement = tempMovementDomainModel.createTempMovement(tempMovementType,username);
        em.flush();
       
        TempMovementType sendTempMovement = tempMovementDomainModel.sendTempMovement(createTempMovement.getGuid(), username);
        em.flush();
        Assert.assertNotNull(sendTempMovement); 
        Assert.assertNotNull(sendTempMovement.getUpdatedTime()); 
        Assert.assertNotNull(sendTempMovement.getGuid()); 
        Assert.assertEquals(TempMovementStateEnum.SENT, sendTempMovement.getState());   
    }        

    
    @Test
    @OperateOnDeployment("normal")
    public void updateTempMovementNullTempMovementCheckFailureTest() throws MovementModelException {
    	thrown.expect(InputArgumentException.class);
        thrown.expectMessage("No temp movement to update");
        
        String username = TempMovementDomainModelBeanIntTest.class.getSimpleName() + UUID.randomUUID().toString();
		tempMovementDomainModel.updateTempMovement(null, username);
    }

    @Test
    @OperateOnDeployment("normal")
    public void updateTempMovementNullUsernameCheckFailureTest() throws MovementModelException {
    	thrown.expect(InputArgumentException.class);
        thrown.expectMessage("Non valid id of temp movement to update");
		tempMovementDomainModel.updateTempMovement(new TempMovementType(),null);
    }

    @Test
    @OperateOnDeployment("normal")
    public void updateTempMovementSuccessTest() throws MovementModelException {
        String username = TempMovementDomainModelBeanIntTest.class.getSimpleName();
        
		TempMovementType tempMovementType = createTestTempMovementType(10d, 10d, 10d);
		TempMovementType createTempMovement = tempMovementDomainModel.createTempMovement(tempMovementType,username);
        em.flush();
             
        createTempMovement.setSpeed(25d);
        
        TempMovementType updateTempMovement = tempMovementDomainModel.updateTempMovement(createTempMovement,username);        
        
        Assert.assertNotNull(updateTempMovement); 
        Assert.assertNotNull(updateTempMovement.getUpdatedTime()); 
        Assert.assertNotNull(updateTempMovement.getGuid()); 
       
        Assert.assertEquals(createTempMovement.getSpeed(), updateTempMovement.getSpeed());
        Assert.assertEquals(createTempMovement.getState(), updateTempMovement.getState());   
        Assert.assertEquals(createTempMovement.getStatus(), updateTempMovement.getStatus());   
        
        Assert.assertEquals(createTempMovement.getAsset(), updateTempMovement.getAsset());
        
        Assert.assertEquals(createTempMovement.getPosition().getLongitude(), updateTempMovement.getPosition().getLongitude());
        Assert.assertEquals(createTempMovement.getPosition().getLatitude(), updateTempMovement.getPosition().getLatitude());
    }    

    
    @Test
    @OperateOnDeployment("normal")
    public void getTempMovementSuccessTest() throws MovementModelException {
        String username = TempMovementDomainModelBeanIntTest.class.getSimpleName();
        
		TempMovementType tempMovementType = createTestTempMovementType(10d, 10d, 10d);
		TempMovementType createTempMovement = tempMovementDomainModel.createTempMovement(tempMovementType,username);
        em.flush();
                     
        TempMovementType getTempMovement = tempMovementDomainModel.getTempMovement(createTempMovement.getGuid());
        Assert.assertEquals(createTempMovement, getTempMovement);
    }
    
    @Test
    @OperateOnDeployment("normal")
    public void getTempMovementNullGuidCheckFailureTest() throws MovementModelException {
    	thrown.expect(InputArgumentException.class);
        thrown.expectMessage("TempMovement GUID cannot be null");
		tempMovementDomainModel.getTempMovement(null);
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
