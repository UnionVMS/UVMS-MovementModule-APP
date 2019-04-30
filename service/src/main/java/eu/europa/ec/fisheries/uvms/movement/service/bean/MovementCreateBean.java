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
package eu.europa.ec.fisheries.uvms.movement.service.bean;

import java.time.Instant;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementRefTypeType;
import eu.europa.ec.fisheries.uvms.asset.client.AssetClient;
import eu.europa.ec.fisheries.uvms.asset.client.model.AssetMTEnrichmentRequest;
import eu.europa.ec.fisheries.uvms.asset.client.model.AssetMTEnrichmentResponse;
import eu.europa.ec.fisheries.uvms.movement.service.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.service.entity.IncomingMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.IncomingMovementMapper;
import eu.europa.ec.fisheries.uvms.movement.service.message.ExchangeBean;
import eu.europa.ec.fisheries.uvms.movement.service.message.MovementRulesBean;
import eu.europa.ec.fisheries.uvms.movement.service.validation.MovementSanityValidatorBean;
import eu.europa.ec.fisheries.uvms.movementrules.model.dto.MovementDetails;

@Stateless
public class MovementCreateBean {

    @Inject
    private MovementSanityValidatorBean movementSanityValidatorBean;

    @Inject
    private MovementService movementService;

    @Inject
    private IncomingMovementBean incomingMovementBean;

    @Inject
    private MovementDao dao;
    
    @EJB
    private AssetClient assetClient;

    @EJB
    private MovementRulesBean movementRulesBean;

    @EJB
    private ExchangeBean exchangeBean;

    public void processIncomingMovement(IncomingMovement incomingMovement) {
        try {
            if (incomingMovement.getUpdated() == null) {
                incomingMovement.setUpdated(Instant.now());
            }

            AssetMTEnrichmentRequest request = createRequest(incomingMovement, incomingMovement.getUpdatedBy());
            AssetMTEnrichmentResponse response = assetClient.collectAssetMT(request);
            enrichIncomingMovement(incomingMovement, response);

            incomingMovementBean.checkAndSetDuplicate(incomingMovement);
            String reportId = movementSanityValidatorBean.evaluateSanity(incomingMovement);
            if (reportId != null) {
                exchangeBean.sendAckToExchange(MovementRefTypeType.ALARM, reportId, incomingMovement.getAckResponseMessageId());
                return;
            }

            Movement movement = IncomingMovementMapper.mapNewMovementEntity(incomingMovement, incomingMovement
                    .getUpdatedBy());
            Movement createdMovement = movementService.createAndProcessMovement(movement);

            // send to MovementRules
            MovementDetails movementDetails = IncomingMovementMapper.mapMovementDetails(incomingMovement, createdMovement, response);
            int sumPositionReport = movementService.countNrOfMovementsLastDayForAsset(incomingMovement.getAssetHistoryId(), incomingMovement.getPositionTime());
            // List<VicinityInfoDTO> vicinityOf = dao.getVicinityOfMovement(createdMovement);
            movementDetails.setSumPositionReport(sumPositionReport);
            // movementDetails.setVicinityOf(vicinityOf);

            movementRulesBean.send(movementDetails);
            // report ok to Exchange...
            // Tracer Id
            exchangeBean.sendAckToExchange(MovementRefTypeType.MOVEMENT, createdMovement.getId().toString(), incomingMovement.getAckResponseMessageId());
        } catch (Exception e) {
            throw new IllegalStateException("Could not process incoming movement", e);
        }
    }

    private void enrichIncomingMovement(IncomingMovement im, AssetMTEnrichmentResponse response) {
        im.setMobileTerminalConnectId(response.getMobileTerminalConnectId());
        im.setAssetGuid(response.getAssetUUID());
        im.setAssetHistoryId(response.getAssetHistoryId());

        im.setAssetName(response.getAssetName());
        im.setFlagState(response.getFlagstate());
    }

    private AssetMTEnrichmentRequest createRequest(IncomingMovement ic, String username) {
        // OBS OBS OBS
        // missing in AssetId
        // GFCM, UVI, ACCAT = > belg req

        AssetMTEnrichmentRequest req = new AssetMTEnrichmentRequest();
        req.setAssetName(ic.getAssetName());
        req.setExternalMarking(ic.getExternalMarking());
        req.setFlagState(ic.getFlagState());
        req.setIrcsValue(ic.getAssetIRCS());
        req.setCfrValue(ic.getAssetCFR());
        if (ic.getAssetGuid() != null) {
            req.setIdValue(UUID.fromString(ic.getAssetGuid()));
        }
        req.setImoValue(ic.getAssetIMO());
        req.setMmsiValue(ic.getAssetMMSI());

        req.setDnidValue(ic.getMobileTerminalDNID());
        req.setSerialNumberValue(ic.getMobileTerminalSerialNumber());
        req.setLesValue(ic.getMobileTerminalLES());
        req.setMemberNumberValue(ic.getMobileTerminalMemberNumber());

        req.setTranspondertypeValue(ic.getMovementSourceType());
        req.setPluginType(ic.getPluginType());
        req.setUser(username);

        return req;
    }
}
