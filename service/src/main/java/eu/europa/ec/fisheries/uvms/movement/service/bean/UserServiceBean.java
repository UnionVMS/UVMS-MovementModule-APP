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

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.TextMessage;

import eu.europa.ec.fisheries.uvms.movement.service.message.MovementConsumerBean;
import eu.europa.ec.fisheries.uvms.movement.service.message.MovementMessageProducerBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.movement.service.message.ModuleQueue;
import eu.europa.ec.fisheries.uvms.user.model.exception.ModelMarshallException;
import eu.europa.ec.fisheries.uvms.user.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.user.model.mapper.UserModuleRequestMapper;
import eu.europa.ec.fisheries.wsdl.user.module.GetContactDetailResponse;
import eu.europa.ec.fisheries.wsdl.user.module.GetOrganisationResponse;

@Stateless
public class UserServiceBean {

    private static final Logger LOG = LoggerFactory.getLogger(UserServiceBean.class);
    private static final Long JMS_TIMEOUT = 60000L;

    @Inject
    private MovementConsumerBean consumer;

    @Inject
    private MovementMessageProducerBean producer;

    public String getUserNationality(String username) {
        try {
            String organizationName = getUserOrganizationName(username);
            return getOrganizationNation(organizationName);
        } catch (ModelMarshallException | MessageException e) {
            LOG.error("[ Error when getting user nationality. ] {}", e);
            throw new RuntimeException("Error when getting user nationality.", e);
        }
    }

    private String getUserOrganizationName(String username) throws ModelMarshallException, MessageException {
        String request = UserModuleRequestMapper.mapToGetContactDetailsRequest(username);
        String messageId = producer.sendModuleMessage(request, ModuleQueue.USER);
        TextMessage response = consumer.getMessage(messageId, TextMessage.class, JMS_TIMEOUT);
        GetContactDetailResponse contactDetails = JAXBMarshaller.unmarshallTextMessage(response, GetContactDetailResponse.class);
        return contactDetails.getContactDetails().getOrganisationName();
    }

    private String getOrganizationNation(String organizationName) throws eu.europa.ec.fisheries.uvms.user.model.exception.ModelMarshallException, MessageException {
        String request = UserModuleRequestMapper.mapToGetOrganisationRequest(organizationName);
        String messageId = producer.sendModuleMessage(request, ModuleQueue.USER);
        TextMessage response = consumer.getMessage(messageId, TextMessage.class, JMS_TIMEOUT);
        GetOrganisationResponse organization = JAXBMarshaller.unmarshallTextMessage(response, GetOrganisationResponse.class);
        return organization.getOrganisation().getNation();
    }
}
