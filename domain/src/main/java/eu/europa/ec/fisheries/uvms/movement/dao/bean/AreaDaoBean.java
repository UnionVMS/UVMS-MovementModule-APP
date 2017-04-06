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
package eu.europa.ec.fisheries.uvms.movement.dao.bean;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import eu.europa.ec.fisheries.uvms.movement.constant.UvmsConstants;
import eu.europa.ec.fisheries.uvms.movement.dao.Dao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.uvms.movement.dao.AreaDao;
import eu.europa.ec.fisheries.uvms.movement.dao.exception.AreaDaoException;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Area;

@LocalBean
@Stateless
public class AreaDaoBean extends Dao implements AreaDao {

    final static Logger LOG = LoggerFactory.getLogger(AreaDaoBean.class);

    @Override
    public List<Area> getAreas() throws AreaDaoException {
        try {
            return em.createNamedQuery(UvmsConstants.AREA_FIND_ALL, Area.class).getResultList();
        } catch (Exception e) {
            LOG.error(" [ Error when getting areas. ] {}", e.getMessage());
            throw new AreaDaoException(" [ Error when getting areas. ]", e);
        }
    }

    @Override
    public Area createMovementArea(Area area) throws AreaDaoException {
        try {
            em.persist(area);
            return area;
        } catch (Exception e) {
            LOG.error("[ Error when creating movement area. ] {}", e.getMessage());
            throw new AreaDaoException(" [ Error when creating movement area. ] ", e);
        }
    }

    @Override
    public void flushMovementAreas() throws AreaDaoException {

        try {
            em.flush();
        } catch (Exception e) {
            LOG.error(" [ Error when flushing movement area. ] ");
            throw new AreaDaoException(" [ Error when flushing movement area. ] ", e);
        }
    }

    @Override
    public Area readMovementAreaById(Long id) {
        return em.find(Area.class, id);
        }
}