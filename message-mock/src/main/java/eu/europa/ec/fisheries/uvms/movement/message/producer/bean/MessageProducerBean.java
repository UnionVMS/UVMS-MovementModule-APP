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
package eu.europa.ec.fisheries.uvms.movement.message.producer.bean;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;
import javax.jms.*;
import eu.europa.ec.fisheries.uvms.config.exception.ConfigMessageException;
import eu.europa.ec.fisheries.uvms.config.message.ConfigMessageProducer;
import eu.europa.ec.fisheries.uvms.movement.message.constants.ModuleQueue;
import eu.europa.ec.fisheries.uvms.movement.message.event.ErrorEvent;
import eu.europa.ec.fisheries.uvms.movement.message.event.carrier.EventMessage;
import eu.europa.ec.fisheries.uvms.movement.message.exception.MovementMessageException;
import eu.europa.ec.fisheries.uvms.movement.message.producer.MessageProducer;

import java.util.UUID;

@Stateless
public class MessageProducerBean implements MessageProducer, ConfigMessageProducer {
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public String sendModuleMessage(String text, ModuleQueue queue) throws MovementMessageException {
        return UUID.randomUUID().toString();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void sendErrorMessageBackToRecipient(@Observes @ErrorEvent EventMessage message) throws MovementMessageException {
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void sendMessageBackToRecipient(TextMessage requestMessage, String returnMessage) throws MovementMessageException {
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public String sendConfigMessage(String text) throws ConfigMessageException {
        return text;
    }
}