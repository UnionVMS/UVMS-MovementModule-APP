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
import javax.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.uvms.movement.dao.AreaDao;
import eu.europa.ec.fisheries.uvms.movement.dao.Dao;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Area;

@LocalBean
@Stateless
public class AreaDaoBean extends Dao implements AreaDao {

    private static final Logger LOG = LoggerFactory.getLogger(AreaDaoBean.class);

    @Override
    public List<Area> getAreas() {
        TypedQuery<Area> namedQuery = em.createNamedQuery(Area.FIND_ALL, Area.class);
        return namedQuery.getResultList();
    }

    @Override
    public Area createMovementArea(Area area) {
        em.persist(area);
        return area;
    }

    @Override
    public void flushMovementAreas() {
        em.flush();
    }

    @Override
    public Area readMovementAreaById(Long id) {
        return em.find(Area.class, id);
        }
}