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
package eu.europa.ec.fisheries.uvms.movement.service.dao;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.service.entity.area.AreaType;
import eu.europa.ec.fisheries.uvms.movement.service.exception.ErrorCode;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceRuntimeException;

@Stateless
public class AreaDao {
    
    private static final Logger LOG = LoggerFactory.getLogger(AreaDao.class);

    @PersistenceContext
    private EntityManager em;
    
    public List<Area> getAreas() {
        TypedQuery<Area> namedQuery = em.createNamedQuery(Area.FIND_ALL, Area.class);
        return namedQuery.getResultList();
    }

    public Area createMovementArea(Area area) {
        em.persist(area);
        return area;
    }
    
    public AreaType createAreaType(AreaType area) {
        em.persist(area);
        return area;
    }

    public void flushMovementAreas() {
        em.flush();
    }

    public Area readMovementAreaById(Long id) {
        return em.find(Area.class, id);
    }

    public AreaType getAreaTypeByCode(String code) {
        try {
            TypedQuery<AreaType> query = em.createNamedQuery(AreaType.FIND_BY_CODE, AreaType.class);
            query.setParameter("code", code);
            return query.getSingleResult();
        } catch (NoResultException e) {
            LOG.debug("No result when retrieving AreaType By code: {}", code);
            return null;
        }
    }

    public Area getAreaByRemoteIdAndCode(String code, String remoteId) {
        try {
            if (code == null || code.isEmpty()) {
                throw new MovementServiceRuntimeException("No valid input parameters to method getAreaByRemoteIdAndCode",
                        ErrorCode.ILLEGAL_ARGUMENT_ERROR);
            }
            TypedQuery<Area> query = em.createNamedQuery(Area.FIND_BY_CODE, Area.class);
            query.setParameter("code", code);
            return query.getSingleResult();
        } catch (NoResultException e) {
            LOG.debug("Could not get AreaType By code: {} and remoteId: {}", code, remoteId);
            return null;
        }
    }
    
}