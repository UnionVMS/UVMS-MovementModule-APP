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

import eu.europa.ec.fisheries.schema.exchange.movement.v1.MovementRefTypeType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.asset.client.AssetClient;
import eu.europa.ec.fisheries.uvms.asset.client.model.AssetMTEnrichmentRequest;
import eu.europa.ec.fisheries.uvms.asset.client.model.AssetMTEnrichmentResponse;
import eu.europa.ec.fisheries.uvms.movement.service.entity.IncomingMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.MovementConnect;
import eu.europa.ec.fisheries.uvms.movement.service.mapper.IncomingMovementMapper;
import eu.europa.ec.fisheries.uvms.movement.service.message.ExchangeBean;
import eu.europa.ec.fisheries.uvms.movement.service.message.MovementRulesBean;
import eu.europa.ec.fisheries.uvms.movement.service.util.CalculationUtil;
import eu.europa.ec.fisheries.uvms.movement.service.validation.MovementSanityValidatorBean;
import eu.europa.ec.fisheries.uvms.movementrules.model.dto.MovementDetails;
import eu.europa.ec.fisheries.uvms.movementrules.model.dto.VicinityInfoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Stateless
public class MovementCreateBean {

    private static final Logger LOG = LoggerFactory.getLogger(MovementCreateBean.class);

    @Inject
    private MovementSanityValidatorBean movementSanityValidatorBean;

    @Inject
    private MovementService movementService;

    @Inject
    private IncomingMovementBean incomingMovementBean;
    
    @Inject
    private AssetClient assetClient;

    @EJB
    private MovementRulesBean movementRulesBean;

    @EJB
    private ExchangeBean exchangeBean;

    public UUID processIncomingMovement(IncomingMovement incomingMovement) {
        try {
            if (incomingMovement.getUpdated() == null) {
                incomingMovement.setUpdated(Instant.now());
            }

            AssetMTEnrichmentRequest assetRequest = createAssetRequest(incomingMovement);
            AssetMTEnrichmentResponse assetResponse = assetClient.collectAssetMT(assetRequest);
            enrichIncomingMovement(incomingMovement, assetResponse);

            incomingMovementBean.checkAndSetDuplicate(incomingMovement);
            if (incomingMovement.isDuplicate() && 
                    incomingMovement.getMovementSourceType().equals(MovementSourceType.AIS.value())) {
                LOG.warn("Ignoring duplicate AIS position for {} ({}) with timestamp {}",
                        incomingMovement.getAssetName(), incomingMovement.getAssetMMSI(), incomingMovement.getPositionTime());
                return null;
            }

            Movement previousVms = null;
            MovementConnect movementConnect = null;
            if(incomingMovement.getAssetGuid() != null && !incomingMovement.getAssetGuid().isEmpty()) {
                MovementConnect newMovementConnect = IncomingMovementMapper.mapNewMovementConnect(incomingMovement, incomingMovement.getUpdatedBy());
                movementConnect = movementService.getOrCreateMovementConnectByConnectId(newMovementConnect);
                previousVms = getPreviousVms(incomingMovement, movementConnect);
            }

            if(MovementTypeType.EXI.value().equals(incomingMovement.getMovementType()) && previousVms != null){
                incomingMovement.setLongitude(previousVms.getLocation().getX());
                incomingMovement.setLatitude(previousVms.getLocation().getY());
            }

            UUID reportId = movementSanityValidatorBean.evaluateSanity(incomingMovement);
            if (reportId != null) {
                exchangeBean.sendAckToExchange(MovementRefTypeType.ALARM, reportId, incomingMovement.getAckResponseMessageId());
                return reportId;
            }

            Movement movement = IncomingMovementMapper.mapNewMovementEntity(incomingMovement, incomingMovement.getUpdatedBy());
            movement.setMovementConnect(movementConnect);
            
            if (previousVms != null && !MovementSourceType.AIS.value().equals(movement.getSource().value()) ) {
                movement.setCalculatedSpeed(CalculationUtil.getPositionCalculations(previousVms, movement).getAvgSpeed());
            }

            Movement createdMovement = movementService.createAndProcessMovement(movement);

            // send to MovementRules
            MovementDetails movementDetails = IncomingMovementMapper.mapMovementDetails(incomingMovement, createdMovement, assetResponse);
            int sumPositionReport = movementService.countNrOfMovementsLastDayForAsset(incomingMovement.getAssetGuid(), incomingMovement.getPositionTime());
            movementDetails.setSumPositionReport(sumPositionReport);
            List<VicinityInfoDTO> vicinityOf = movementService.getVicinityOf(createdMovement);
            movementDetails.setVicinityOf(vicinityOf);

            if (previousVms != null) {
                movementDetails.setPreviousVMSLatitude(previousVms.getLocation().getY());
                movementDetails.setPreviousVMSLongitude(previousVms.getLocation().getX());
            }

            movementRulesBean.send(movementDetails);
            // report ok to Exchange...
            // Tracer Id
            exchangeBean.sendAckToExchange(MovementRefTypeType.MOVEMENT, createdMovement.getId(), incomingMovement.getAckResponseMessageId());

            return null;
        } catch (Exception e) {
            throw new IllegalStateException("Could not process incoming movement", e);
        }
    }

    private Movement getPreviousVms(IncomingMovement movement, MovementConnect movementConnect) {
        if (MovementSourceType.AIS.value().equals(movement.getMovementSourceType())
                || movement.getPositionTime() == null) {
            return null;
        }
        Movement currentLatestVMS = movementConnect.getLatestVMS();
        if (currentLatestVMS != null &&
                currentLatestVMS.getTimestamp().isBefore(movement.getPositionTime())) {
            return currentLatestVMS;
        } else {
            return movementService.getPreviousVMS(movementConnect.getId(), movement.getPositionTime());
        }
    }

    private void enrichIncomingMovement(IncomingMovement im, AssetMTEnrichmentResponse response) {
        im.setMobileTerminalConnectId(response.getMobileTerminalConnectId());
        if (response.getMobileTerminalIsInactive() != null) {
            im.setMobileTerminalActive(!response.getMobileTerminalIsInactive());
        }
        im.setAssetGuid(response.getAssetUUID());
        im.setAssetHistoryId(response.getAssetHistoryId());

        im.setAssetName(response.getAssetName());
        im.setFlagState(response.getFlagstate());

        im.setAssetIRCS(im.getAssetIRCS() != null && !im.getAssetIRCS().isEmpty()
                ? im.getAssetIRCS() : response.getIrcs());
        im.setAssetCFR(im.getAssetCFR() != null && !im.getAssetCFR().isEmpty()
                ? im.getAssetCFR() : response.getCfr());
        im.setExternalMarking(im.getExternalMarking() != null && !im.getExternalMarking().isEmpty()
                ? im.getExternalMarking() : response.getExternalMarking());
    }

    private AssetMTEnrichmentRequest createAssetRequest(IncomingMovement ic) {
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
        req.setUser(ic.getUpdatedBy());

        return req;
    }
}
