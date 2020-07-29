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
package eu.europa.ec.fisheries.uvms.movement.model.mapper;

import eu.europa.ec.fisheries.uvms.commons.xml.AbstractJAXBMarshaller;
import eu.europa.ec.fisheries.uvms.commons.xml.JAXBRuntimeException;
import eu.europa.ec.fisheries.uvms.movement.model.exception.ErrorCode;

import java.util.HashMap;
import java.util.Map;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JAXBMarshaller extends AbstractJAXBMarshaller {

    private static final Logger LOG = LoggerFactory.getLogger(JAXBMarshaller.class);

    /**
     * Marshalls a JAXB Object to a XML String representation
     *
     * @param <T>
     * @param data
     * @return
     * @throws eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException
     */
    public static <T> String marshallJaxBObjectToString(T data) throws MovementModelException {
        try {
            Map<String,Object> properties = new HashMap<>();
            properties.put(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            return marshallToString(data, properties);
        } catch (JAXBException | JAXBRuntimeException ex) {
            throw new MovementModelException("[ Error when marshalling Object to String ]", ex, ErrorCode.MODEL_MARSHALL_ERROR);
        }
    }

    /**
     * Unmarshalls A textMessage to the desired Object. The object must be the
     * root object of the unmarchalled message!
     *
     * @param <R>
     * @param textMessage
     * @param clazz       pperException
     * @return
     * @throws eu.europa.ec.fisheries.uvms.movement.model.exception.MovementModelException
     */
    public static <R> R unmarshallTextMessage(TextMessage textMessage, Class<R> clazz) throws MovementModelException {
        try {
            return unmarshallTo(textMessage, clazz);
        } catch (NullPointerException | JMSException | JAXBException | JAXBRuntimeException ex) {
            throw new MovementModelException("[Error when unmarshalling response in ResponseMapper ]", ex, ErrorCode.MODEL_MARSHALL_ERROR);
        }
    }
}
