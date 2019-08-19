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
package eu.europa.ec.fisheries.uvms.movement.service.message;

import java.util.Arrays;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.*;

import eu.europa.ec.fisheries.schema.config.types.v1.PullSettingsStatus;
import eu.europa.ec.fisheries.schema.config.types.v1.SettingType;
import eu.europa.ec.fisheries.uvms.commons.message.context.MappedDiagnosticContext;
import eu.europa.ec.fisheries.uvms.config.model.exception.ModelMarshallException;
import eu.europa.ec.fisheries.uvms.config.model.mapper.ModuleResponseMapper;

@MessageDriven(mappedName = "jms/queue/UVMSConfigEvent", activationConfig = {
        @ActivationConfigProperty(propertyName = "messagingType", propertyValue = "javax.jms.MessageListener"), 
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"), 
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "UVMSConfigEvent")})
public class ConfigServiceMock implements MessageListener {
    
    @Inject
    @JMSConnectionFactory("java:/JmsXA")
    private JMSContext context;
    
    @Override
    public void onMessage(Message message) {
        try {
            SettingType mockSetting = new SettingType();
            mockSetting.setKey("maxDistance");
            mockSetting.setValue("500");
            mockSetting.setDescription("Set in ConfigServiceMock.java");
            String response = ModuleResponseMapper.toPullSettingsResponse(Arrays.asList(mockSetting), PullSettingsStatus.OK);

            TextMessage responseMessage = this.context.createTextMessage(response);
            responseMessage.setJMSCorrelationID(message.getJMSMessageID());
            MappedDiagnosticContext.addThreadMappedDiagnosticContextToMessageProperties(responseMessage);
            this.context.createProducer().send(message.getJMSReplyTo(), responseMessage);
        } catch (ModelMarshallException | JMSException e) {
        }
    }
}
