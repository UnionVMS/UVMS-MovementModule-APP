# MovementModule

##Module description/purpose

The main purpose of the Movement module is the storage and retrieval of position reports regarding movements for ships. 

To save valuable calculation capacity for modules requesting position data some enriching and calculations is done before persisting the position reports. Enriching of areas, harbors, and countries etc. where the position is currently located is done by querying the spatial module and passing the longitude and latitude from the position report.

One important thing to understand about the movement module are movements, segments and tracks. 

All movements are calculated to be a part of a segment and a track. Calculations can then be made to control the reported speeds and courses to the actual speed and courses that are calculated. More of this can be read in the Javadoc. 

The movement module can also store searches that a user makes on positions for later retrieval so the user don´t have to fill all fields again when making a search. 

The movement module has functionality for storing temporary ( manual ) positions. These positions can be created saved and retrieved for later processing. When the user wants to he or she can execute the movement. When the movement is executed it is sent to the exchange module who in turn processes the position report.

##JMS-Queue dependencies

The jndi name example is taken from wildfly8.2 application server

|Name             |JNDI name example                |Description                                |
|-----------------|---------------------------------|-------------------------------------------|
|UVMSMovementEvent|java:/jms/queue/UVMSMovementEvent|Request queue to movement main module      |
|UVMSMovement     |java:/jms/queue/UVMSMovement     |Response queue to movement main module     |
|UVMSMovementModel|java:/jms/queue/UVMSMovementModel|Request queue to the Database module       |
|UVMSAuditEvent   |java:/jms/queue/UVMSAuditEvent   |Request queue to the Audit main module     |
|UVMSSpatialEvent |java:/jms/queue/UVMSSpatialEvent |Request queue to the Spatial main module   |
|UVMSExchangeEvent|java:/jms/queue/UVMSExchangeEvent|Request queue to the Exchange main module  |

##Datasource dependencies

The jndi name example is taken from wildfly8.2 application server

|Name             |JNDI name example                   |
|-----------------|------------------------------------|
|uvms_movement    |java:jboss/datasources/uvms_movement|

## Related Repositories

* https://github.com/UnionVMS/UVMS-MovementModule-DB
* https://github.com/UnionVMS/UVMS-MovementModule-MODEL
