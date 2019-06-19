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

import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.impl.AbstractProducer;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;

@Stateless
public class AuditProducer extends AbstractProducer {

    @Resource(mappedName = "java:/jms/queue/UVMSMovement")
    private Queue replyToQueue;

    @Resource(mappedName =  "java:/" + MessageConstants.QUEUE_AUDIT_EVENT)
    private Destination destination;

    public String sendModuleMessage(String text) throws JMSException {
        return sendModuleMessage(text, replyToQueue);
    }
    
    @Override
    public Destination getDestination() {
        return destination;
    }
}
