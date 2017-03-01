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

import java.math.BigInteger;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import eu.europa.ec.fisheries.uvms.movement.model.MovementSearchGroupDomainModel;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementDuplicateException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.schema.movement.search.v1.MovementSearchGroup;
import eu.europa.ec.fisheries.uvms.movement.service.MovementSearchGroupService;
import eu.europa.ec.fisheries.uvms.movement.service.exception.MovementServiceException;
import eu.europa.ec.fisheries.uvms.movement.service.validation.MovementGroupValidator;

@Stateless
public class MovementSearchGroupServiceBean implements MovementSearchGroupService {

    private final static Logger LOG = LoggerFactory.getLogger(MovementSearchGroupServiceBean.class);

    //@EJB(lookup = LookupConstant.SEARCH_GROUP_MODEL)
    @EJB
    MovementSearchGroupDomainModel groupModel;

    //TODO SET AS PARAMETER
    private static final Long CREATE_MOVEMENT_TIMEOUT = 10000L;

    @Override
    public MovementSearchGroup createMovementSearchGroup(MovementSearchGroup searchGroup, String username) throws MovementServiceException, MovementDuplicateException {
        try {
            if(searchGroup.getName() == null || searchGroup.getName().isEmpty()){
                LOG.error("[ Error when creating movement search group. Search group must have a name]");
                throw new MovementServiceException("Search group must have a name]");
            }
            if(username == null){
                LOG.error("[Cannot create the MovementSearchGroup when username is null]");
                throw new MovementServiceException("Create MovementSearchGroup must have username set, cannot be null");
            }
            if (MovementGroupValidator.isMovementGroupOk(searchGroup)) {
                return groupModel.createMovementSearchGroup(searchGroup, username);
            } else {
                throw new MovementServiceException("One or several movement types are misspelled or non existant. Allowed values are: [ " + MovementGroupValidator.ALLOWED_FIELD_VALUES + " ]");
            }
        } catch (MovementModelException e) {
            LOG.error("[ Error when creating movement search group. ] {}", e.getMessage());
            throw new MovementServiceException("Error when creating movement search group", e);
        }
    }

    @Override
    public MovementSearchGroup getMovementSearchGroup(Long id) throws MovementServiceException, MovementDuplicateException {
        try {
            return groupModel.getMovementSearchGroup(BigInteger.valueOf(id));
        } catch (MovementModelException e) {
            LOG.error("[ Error when getting movement search group. ] {}", e.getMessage());
            throw new MovementServiceException("[ Error when getting movement search group. ]", e);
        }
    }

    @Override
    public List<MovementSearchGroup> getMovementSearchGroupsByUser(String user) throws MovementServiceException, MovementDuplicateException {
        try {
            return groupModel.getMovementSearchGroupsByUser(user);
        } catch (MovementModelException e) {
            LOG.error("[ Error when getting movement search groups by user. ] {}", e.getMessage());
            throw new MovementServiceException("[ Error when getting movement search groups by user. ]", e);
        }
    }

    @Override
    public MovementSearchGroup updateMovementSearchGroup(MovementSearchGroup searchGroup, String username) throws MovementServiceException, MovementDuplicateException {
        if(searchGroup.getId() == null || username == null){
            LOG.error("[Cannot update the MovementSearchGroup when it has no id set or username is null]");
            throw new MovementServiceException("Error when updating movement search group. MovementSearchGroup has no id set or the username is null");
        }
        try {
            return groupModel.updateMovementSearchGroup(searchGroup, username);
        } catch (MovementModelException e) {
            LOG.error("[ Error when updating movement search group. ] {}", e.getMessage());
            throw new MovementServiceException("[ Error when updating movement search group. ] " + e.getMessage(), e);
        }
    }

    @Override
    public MovementSearchGroup deleteMovementSearchGroup(Long id) throws MovementServiceException, MovementDuplicateException {
        try {
            return groupModel.deleteMovementSearchGroup(BigInteger.valueOf(id));
        } catch (MovementModelException e) {
            LOG.error("[ Error when deleting movement search group. ] {}", e.getMessage());
            throw new MovementServiceException("[ Error when deleting movement search group. ]", e);
        }
    }
}