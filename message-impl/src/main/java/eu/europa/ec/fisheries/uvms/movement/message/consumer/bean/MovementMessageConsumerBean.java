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
package eu.europa.ec.fisheries.uvms.movement.message.consumer.bean;

import eu.europa.ec.fisheries.schema.movement.module.v1.MovementBaseRequest;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.movement.message.event.ErrorEvent;
import eu.europa.ec.fisheries.uvms.movement.message.event.carrier.EventMessage;
import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.JAXBMarshaller;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MessageDriven(mappedName = MessageConstants.QUEUE_MODULE_MOVEMENT, activationConfig = {
        @ActivationConfigProperty(propertyName = MessageConstants.MESSAGING_TYPE_STR, propertyValue = MessageConstants.CONNECTION_TYPE),
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_TYPE_STR, propertyValue = MessageConstants.DESTINATION_TYPE_QUEUE),
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_STR, propertyValue = MessageConstants.COMPONENT_MESSAGE_IN_QUEUE_NAME),
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_JNDI_NAME, propertyValue = MessageConstants.QUEUE_MODULE_MOVEMENT),
        @ActivationConfigProperty(propertyName = MessageConstants.CONNECTION_FACTORY_JNDI_NAME, propertyValue = MessageConstants.CONNECTION_FACTORY)

})
public class MovementMessageConsumerBean implements MessageListener {

    final static Logger LOG = LoggerFactory.getLogger(MovementMessageConsumerBean.class);

    @EJB
    private CreateMovementBean createMovementBean;

    @EJB
    private GetMovementListByQueryBean getMovementListByQueryBean;

    @EJB
    private CreateMovementBatchBean createMovementBatchBean;

    @EJB
    private GetMovementMapByQueryBean getMovementMapByQueryBean;

    @EJB
    private GetMovementListByAreaAndTimeIntervalBean getMovementListByAreaAndTimeIntervalBean;

    @EJB
    private PingBean pingBean;

    @Inject
    @ErrorEvent
    private Event<EventMessage> errorEvent;

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = null;
        try {
            textMessage = (TextMessage) message;
            LOG.debug("Message received in movement");
            MovementBaseRequest request = JAXBMarshaller.unmarshallTextMessage(textMessage, MovementBaseRequest.class);
            if (request.getMethod() == null) {
                LOG.error("[ Request method is null ]");
                errorEvent.fire(new EventMessage(textMessage, "Error when receiving message in movement: "));
                return;
            }
            switch (request.getMethod()) {
                case MOVEMENT_LIST:
                    getMovementListByQueryBean.getMovementListByQuery(textMessage);
                    break;
                case CREATE:
                    createMovementBean.createMovement(textMessage);
                    break;
                case CREATE_BATCH:
                    createMovementBatchBean.createMovementBatch(textMessage);
                    break;
                case MOVEMENT_MAP:
                    getMovementMapByQueryBean.getMovementMapByQuery(textMessage);
                    break;
                case PING:
                    pingBean.ping(textMessage);
                    break;
                case MOVEMENT_LIST_BY_AREA_TIME_INTERVAL:
                    getMovementListByAreaAndTimeIntervalBean.getMovementListByAreaAndTimeInterval(textMessage);
                    break;
                case GET_SEGMENT_BY_ID:
                case GET_TRIP_BY_ID:
                default:
                    LOG.error("[ Request method {} is not implemented ]", request.getMethod().name());
                    errorEvent.fire(new EventMessage(textMessage, "[ Request method " + request.getMethod().name() + "  is not implemented ]"));
            }
        } catch (NullPointerException | ClassCastException | MovementModelException e) {
            LOG.error("[ Error when receiving message in movement: ] {}", e);
            errorEvent.fire(new EventMessage(textMessage, "Error when receiving message in movement: " + e.getMessage()));
        }
    }
}
