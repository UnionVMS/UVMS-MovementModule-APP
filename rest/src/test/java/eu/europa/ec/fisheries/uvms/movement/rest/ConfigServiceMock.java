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

import java.util.Arrays;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import eu.europa.ec.fisheries.schema.config.types.v1.PullSettingsStatus;
import eu.europa.ec.fisheries.schema.config.types.v1.SettingType;
import eu.europa.ec.fisheries.uvms.config.model.exception.ModelMarshallException;
import eu.europa.ec.fisheries.uvms.config.model.mapper.ModuleResponseMapper;
import eu.europa.ec.fisheries.uvms.movement.message.exception.MovementMessageException;
import eu.europa.ec.fisheries.uvms.movement.message.producer.MessageProducer;

@MessageDriven(mappedName = "jms/queue/UVMSConfigEvent", activationConfig = {
        @ActivationConfigProperty(propertyName = "messagingType", propertyValue = "javax.jms.MessageListener"), 
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"), 
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "UVMSConfigEvent")})
public class ConfigServiceMock implements MessageListener {
    
    @Inject
    MessageProducer messageProducer;

    @Override
    public void onMessage(Message message) {
        try {
            SettingType mockSetting = new SettingType();
            mockSetting.setKey("Key");
            mockSetting.setValue("Value");
            mockSetting.setDescription("From ConfigServiceMock.java");

            SettingType fluxLocalNationCodeMockSetting = new SettingType();
            mockSetting.setKey("flux_local_nation_code");
            mockSetting.setValue("XEU");
            mockSetting.setDescription("From ConfigServiceMock.java");

            String response = ModuleResponseMapper.toPullSettingsResponse(Arrays.asList(mockSetting, fluxLocalNationCodeMockSetting), PullSettingsStatus.OK);
            messageProducer.sendMessageBackToRecipient((TextMessage) message, response);
        } catch (ModelMarshallException | MovementMessageException e) {
        }
    }
}
