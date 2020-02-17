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

import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.service.clients.SpatialRestClient;
import eu.europa.ec.fisheries.uvms.movement.service.dto.SegmentCalculations;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Track;
import eu.europa.ec.fisheries.uvms.movement.service.util.CalculationUtil;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.time.Instant;

@Stateless
public class TrackService {

    @Inject
    private SpatialRestClient spatialClient;
    
    public void upsertTrack(Movement previous, Movement current) {
        SegmentCalculations positionCalculations = CalculationUtil.getPositionCalculations(previous, current);
        Track existingTrack = previous.getTrack();
        if (existingTrack == null) {        //if there is no tracks
            Track track = createNewTrack(positionCalculations);
            previous.setTrack(track);
            current.setTrack(track);
        } else {
            SegmentCategoryType segCat = spatialClient.getSegmentCategoryType(previous, current);
            switch (segCat) {     //if a segment is is an area transition out of a port, then create a new track, else add to the old one
                case EXIT_PORT:
                    Track track = createNewTrack(positionCalculations);
                    current.setTrack(track);
                    break;
                case GAP:
                case JUMP:
                case IN_PORT:
                case ENTER_PORT:
                case NULL_DUR:
                case ANCHORED:
                case LOW_SPEED:
                case OTHER:
                    updateTrack(existingTrack, positionCalculations, segCat);
                    current.setTrack(existingTrack);
                    break;
                default:
                    throw new IllegalArgumentException("SEGMENT CATEGORY " + segCat.name() + " IS NOT MAPPED");
            }
        }
    }
    
    private Track createNewTrack(SegmentCalculations positionCalculations) {
        Track track = new Track();
        track.setDistance(positionCalculations.getDistanceBetweenPoints());
        track.setDuration(positionCalculations.getDurationBetweenPoints());
        track.setUpdated(Instant.now());
        track.setUpdatedBy("UVMS");
        return track;
    }
    
    private void updateTrack(Track track, SegmentCalculations positionCalculations, SegmentCategoryType segCat)  {
        //add segments values to those of the track
        double calculatedDistance = track.getDistance() + positionCalculations.getDistanceBetweenPoints();
        track.setDistance(calculatedDistance);
        double calculatedDurationInSeconds = track.getDuration() + positionCalculations.getDurationBetweenPoints();
        track.setDuration(calculatedDurationInSeconds);

        if (!segCat.equals(SegmentCategoryType.ENTER_PORT) || !segCat.equals(SegmentCategoryType.IN_PORT)) {     //if we have not entered a port or are in a port, add to the total amount of time
            double totalTimeAtSea = track.getTotalTimeAtSea();                                                                                               //this makes it so that the last segment of a track is not counted towards the total time of a track
            track.setTotalTimeAtSea(totalTimeAtSea + positionCalculations.getDurationBetweenPoints());
        }
    }
}
