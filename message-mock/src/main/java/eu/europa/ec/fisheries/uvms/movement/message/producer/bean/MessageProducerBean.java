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

import java.util.UUID;

import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.uvms.config.exception.ConfigMessageException;
import eu.europa.ec.fisheries.uvms.config.message.ConfigMessageProducer;

@Stateless
public class MessageProducerBean implements MessageProducer, ConfigMessageProducer {

    public static final String MESSAGE_PRODUCER_METHODS_FAIL = "MESSAGE_PRODUCER_METHODS_FAIL";

    final static Logger LOG = LoggerFactory.getLogger(MessageProducerBean.class);

    private void shouldIFail() throws MovementMessageException {
        final String fail = System.getProperty(MESSAGE_PRODUCER_METHODS_FAIL, "false");
        if(!"false".equals(fail.toLowerCase())) {
            throw new MovementMessageException();
        }
    }

    @Override
    public String sendModuleMessage(final String text, final ModuleQueue queue) throws MovementMessageException {
        shouldIFail();
        LOG.info("sendModuleMessage (" + queue.name() + "): " + text);
        return UUID.randomUUID().toString();
    }

    public void sendErrorMessageBackToRecipient(@Observes @ErrorEvent final EventMessage message) throws MovementMessageException {
        shouldIFail();
        LOG.info("sendErrorMessageBackToRecipient: " + message.getErrorMessage());
    }

    public void sendMessageBackToRecipient(final TextMessage requestMessage, final String returnMessage) throws MovementMessageException {
        shouldIFail();
        LOG.info("sendMessageBackToRecipient: " + returnMessage);
    }

    @Override
    public String sendConfigMessage(final String text) throws ConfigMessageException {
        LOG.info("sendConfigMessage: " + text);
        return text;
    }
}