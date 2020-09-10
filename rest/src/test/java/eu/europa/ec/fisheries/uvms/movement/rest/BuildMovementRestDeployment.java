/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.movement.rest;

import eu.europa.ec.fisheries.uvms.movement.service.util.JsonBConfiguratorMovement;
import eu.europa.ec.fisheries.uvms.rest.security.InternalRestTokenHandler;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import eu.europa.ec.mare.usm.jwt.JwtTokenHandler;
import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.File;
import java.util.Arrays;

@ArquillianSuiteDeployment
public abstract class BuildMovementRestDeployment {

    @Inject
    private JwtTokenHandler tokenHandler;

    @Inject
    private InternalRestTokenHandler internalRestTokenHandler;

    private String token;

    @Deployment(name = "movement", order = 2)
    public static Archive<?> createDeployment() {

        WebArchive testWar = ShrinkWrap.create(WebArchive.class, "test.war");

        File[] files = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeAndTestDependencies().resolve()
                .withTransitivity().asFile();
        testWar.addAsLibraries(files);
        
        testWar.addAsLibraries(Maven.configureResolver().loadPomFromFile("pom.xml")
                .resolve("eu.europa.ec.fisheries.uvms.movement:movement-service")
                .withTransitivity().asFile());

        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movement.rest");

        testWar.delete("/WEB-INF/web.xml");
        testWar.addAsWebInfResource("mock-web.xml", "web.xml");
        
        return testWar;
    }

    @Deployment(name = "uvms", order = 1)
    public static Archive<?> createSpatialMock() {

        WebArchive testWar = ShrinkWrap.create(WebArchive.class, "unionvms.war");

        File[] files = Maven.configureResolver().loadPomFromFile("pom.xml")
                .resolve("eu.europa.ec.fisheries.uvms.asset:asset-client",
                        "eu.europa.ec.fisheries.uvms.asset:asset-model",
                        "eu.europa.ec.fisheries.uvms.movement:movement-model",
                        "eu.europa.ec.fisheries.uvms:usm4uvms",
                        "eu.europa.ec.fisheries.uvms.commons:uvms-commons-message")
                .withTransitivity().asFile();
        testWar.addAsLibraries(files);

        testWar.addClass(UnionVMSMock.class);
        testWar.addClass(SpatialModuleMock.class);
        testWar.addClass(AssetMTRestMock.class);

        return testWar;
    }
    
    protected WebTarget getWebTarget() {

        Client client = ClientBuilder.newClient();
        client.register(JsonBConfiguratorMovement.class);
        return client.target("http://localhost:8080/test/rest");
    }

    protected String getToken() {
        if (token == null) {
            token = tokenHandler.createToken("user", 
                    Arrays.asList(UnionVMSFeature.manageManualMovements.getFeatureId(), 
                            UnionVMSFeature.viewMovements.getFeatureId(),
                            UnionVMSFeature.viewManualMovements.getFeatureId(),
                            UnionVMSFeature.manageAlarmsHoldingTable.getFeatureId(),
                            UnionVMSFeature.viewAlarmsHoldingTable.getFeatureId()));
        }
        return token;
    }

    protected String getTokenInternalRest() {
        return internalRestTokenHandler.createAndFetchToken("user");
    }
}
